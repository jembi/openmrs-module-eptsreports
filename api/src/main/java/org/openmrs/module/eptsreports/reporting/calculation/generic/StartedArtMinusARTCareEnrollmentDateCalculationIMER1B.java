/*
 * The contents of this file are subject to the OpenMRS Public License Version
 * 1.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 *
 * Copyright (C) OpenMRS, LLC. All Rights Reserved.
 */
package org.openmrs.module.eptsreports.reporting.calculation.generic;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.Days;
import org.joda.time.Interval;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.eptsreports.reporting.calculation.BooleanResult;
import org.openmrs.module.eptsreports.reporting.calculation.melhoriaQualidade.MohMQInitiatedARTDuringTheInclusionPeriodCalculation;
import org.openmrs.module.eptsreports.reporting.utils.EptsCalculationUtils;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.stereotype.Component;

/** Returns patients who initiated ART within 15 days from the ART Care Enrollment */
@Component
public class StartedArtMinusARTCareEnrollmentDateCalculationIMER1B
    extends AbstractPatientCalculation {

  private static final String ON_OR_BEFORE = "onOrBefore";
  private final int INTERVAL_BETWEEN_ART_START_DATE_MINUS_PATIENT_ART_ENROLLMENTDATE = 33;

  @Override
  public CalculationResultMap evaluate(
      Collection<Integer> cohort,
      Map<String, Object> parameterValues,
      PatientCalculationContext context) {
    CalculationResultMap map = new CalculationResultMap();
    CalculationResultMap artStartDates =
        calculate(
            Context.getRegisteredComponents(
                    MohMQInitiatedARTDuringTheInclusionPeriodCalculation.class)
                .get(0),
            cohort,
            parameterValues,
            context);
    CalculationResultMap preARTCareEnrollment = getARTCareEnrollment(cohort, context);

    Date endDate = (Date) parameterValues.get(ON_OR_BEFORE);

    if (endDate == null) {
      endDate = (Date) context.getFromCache(ON_OR_BEFORE);
    }

    if (endDate != null) {
      for (Integer patientId : cohort) {
        boolean lessOrEqualTo15Days = false;
        Date artStartDate =
            InitialArtStartDateCalculation.getArtStartDate(patientId, artStartDates);
        Date preARTCareEnrollmentDate = (Date) preARTCareEnrollment.get(patientId);

        if (artStartDate != null && preARTCareEnrollmentDate != null) {
          int days =
              Days.daysIn(new Interval(artStartDate.getTime(), preARTCareEnrollmentDate.getTime()))
                  .getDays();

          if (days <= INTERVAL_BETWEEN_ART_START_DATE_MINUS_PATIENT_ART_ENROLLMENTDATE) {
            lessOrEqualTo15Days = true;
          }
        }

        map.put(patientId, new BooleanResult(lessOrEqualTo15Days, this));
      }
      return map;
    } else {
      throw new IllegalArgumentException(String.format("Parameter %s must be set", ON_OR_BEFORE));
    }
  }

  private CalculationResultMap getARTCareEnrollment(
      Collection<Integer> cohort, PatientCalculationContext context) {
    HivMetadata hivMetadata = Context.getRegisteredComponents(HivMetadata.class).get(0);
    SqlPatientDataDefinition def = new SqlPatientDataDefinition();
    def.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    def.addParameter(new Parameter("location", "location", Location.class));
    String sql =
        " SELECT final.patient_id, final.mindate as  mdate "
            + "    FROM  "
            + "        (  "
            + "        SELECT earliest_date.patient_id ,MIN(earliest_date.min_date)  as  mindate "
            + "        FROM  "
            + "            (  "
            + "                SELECT p.patient_id, MIN(pp.date_enrolled) AS min_date  "
            + "                FROM patient p  "
            + "                    INNER JOIN patient_program pp  "
            + "                        ON pp.patient_id = p.patient_id  "
            + "                    INNER JOIN program pg  "
            + "                        ON pg.program_id = pp.program_id  "
            + "                WHERE  "
            + "                    p.voided = 0  "
            + "                    AND pp.voided = 0  "
            + "                    AND pp.date_enrolled <= :endDate "
            + "                    AND pg.program_id = ${1}  "
            + "                    AND pp.location_id = :location  "
            + "                GROUP BY p.patient_id  "
            + "                UNION  "
            + "                SELECT p.patient_id, MIN(o.value_datetime) AS min_date  "
            + "                FROM patient p  "
            + "                    INNER JOIN encounter e  "
            + "                        ON e.patient_id = p.patient_id  "
            + "                    INNER JOIN obs o  "
            + "                        ON o.encounter_id = e.encounter_id  "
            + "                WHERE   "
            + "                    p.voided =0  "
            + "                    AND e.voided = 0  "
            + "                    AND o.voided = 0  "
            + "                    AND e.encounter_type = ${53}  "
            + "                    AND e.location_id = :location  "
            + "                    AND o.concept_id = ${23808} "
            + "                    AND o.value_datetime <= :endDate  "
            + "                GROUP BY p.patient_id  "
            + "            ) as earliest_date  "
            + "        GROUP BY earliest_date.patient_id  "
            + "        ) as final   "
            + "    WHERE final.mindate   "
            + "        BETWEEN :startDate AND :endDate  ";

    def.setSql(
        String.format(
            sql,
            hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId(),
            hivMetadata.getArtPickupConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId(),
            hivMetadata.getArtDatePickupMasterCard().getConceptId()));

    Map<String, Object> params = new HashMap<>();
    params.put("location", context.getFromCache("location"));
    params.put("onOrBefore", context.getFromCache("onOrBefore"));
    return EptsCalculationUtils.evaluateWithReporting(def, cohort, params, null, context);
  }
}
