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
package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.*;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Defines all the PMTCT-HEI Cohort Definition instances we want to expose for EPTS */
@Component
public class PmtctHeiCohortQueries {

  private HivMetadata hivMetadata;
  private PmtctEidCohortQueries pmtctEidCohortQueries;

  @Autowired
  public PmtctHeiCohortQueries(
      HivMetadata hivMetadata, PmtctEidCohortQueries pmtctEidCohortQueries) {
    this.hivMetadata = hivMetadata;
    this.pmtctEidCohortQueries = pmtctEidCohortQueries;
  }

  /**
   * HEI_FR5 <b> Infants who have a virologic HIV test result returned during the reporting period
   * </b>
   *
   * <p>The system will identify infants who have a virologic HIV test result returned by selecting
   * all infants who have a Laboratório Geral Form with
   * <li>One of the following HIV DNA PCR results selected:
   *
   *     <ul>
   *       <li>Pos
   *           <p>OR
   *       <li>Ned
   *     </ul>
   *
   *     <p>AND
   * <li>One of the following EID collection types (tipo de colheita – DPI) selected:
   *
   *     <ul>
   *       <li>First collection < 9 months (Primeira colheita <9m)
   *           <p>OR
   *       <li>Following collection < 9 months (Colheita seguinte <9m)
   *           <p>OR
   *       <li>First collection 9-17 months (Primeira colheita 9-17 meses)
   *           <p>OR
   *       <li>Following collection 9-17 months (Colheita seguinte 9-17 meses)
   *     </ul>
   *
   *     <p>AND
   * <li>The result date (data de resultado) on the respective Laboratório Geral Form falls between
   *     report start date and report end date.
   *
   *     <p>AND
   * <li>The age of the child on specimen collection date entered on the respective Laboratório
   *     Geral Form is less than or equal to 12 months (365 days).
   *
   *     <p><b> Note:</b>
   *
   *     <p>For infants with more than one Laboratório Geral Form complying with these three above
   *     mentioned criteria, the Laboratório Geral Form with the most recent result date falling
   *     between reporting start date and reporting end date will be considered, as long as the age
   *     of the child at specimen collection date was 12 months or less. If the child was older than
   *     12 months according to the specimen collection date on the Laboratório Geral Form with most
   *     recent result date falling in the reporting period, the system will consider the form with
   *     previous result date falling during the reporting period, as long as the child was not
   *     older than 12 months at specimen collection date.
   *
   *     <p>For infants who have two Laboratório Geral Forms complying with the three above
   *     mentioned criteria with discrepant results on the same, most recent, date during the
   *     reporting period, the positive PCR results will be considered.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoHaveVirologivHivTestResult() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "Infants who have a virologic HIV test result returned during the reporting period");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("23821", hivMetadata.getSampleCollectionDateAndTime().getConceptId());
    map.put("165502", hivMetadata.getSampleEarlyInfantDiagnosisConcept().getConceptId());
    map.put("165503", hivMetadata.getFirstSampleInLessThan9MonthsConcept().getConceptId());
    map.put("165506", hivMetadata.getNextSampleInLessThan9MonthsConcept().getConceptId());
    map.put("165507", hivMetadata.getFirstSampleBetween9To17MonthsConcept().getConceptId());
    map.put("165510", hivMetadata.getFollowingtSampleBetween9To17MonthsConcept().getConceptId());
    map.put("1030", hivMetadata.getHivPCRQualitativeConceptUuid().getConceptId());
    map.put("703", hivMetadata.getPositive().getConceptId());
    map.put("664", hivMetadata.getNegative().getConceptId());

    String query =
        "SELECT "
            + "    patient_id "
            + "FROM "
            + "    ( "
            + "        SELECT "
            + "            p.patient_id, "
            + "            MAX(o2.value_datetime) AS pcr_result_date "
            + "        FROM "
            + "            patient p "
            + "                INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "                INNER JOIN obs o3 ON o3.encounter_id = e.encounter_id "
            + "                INNER JOIN ( "
            + "                SELECT "
            + "                    p.patient_id, "
            + "                    ps.birthdate AS birtday "
            + "                FROM "
            + "                    patient p "
            + "                        INNER JOIN person ps ON p.patient_id = ps.person_id "
            + "                WHERE "
            + "                    p.voided = 0 "
            + "                  AND ps.voided = 0 "
            + "            ) pat ON pat.patient_id = p.patient_id "
            + "        WHERE "
            + "            p.voided = 0 "
            + "          AND e.voided = 0 "
            + "          AND o.voided = 0 "
            + "          AND o2.voided = 0 "
            + "          AND o3.voided = 0 "
            + "          AND e.location_id = :location "
            + "          AND e.encounter_type = ${13} "
            + "          AND e.encounter_datetime >= :startDate "
            + "          AND e.encounter_datetime <= :endDate "
            + "          AND ( "
            + "            ( "
            + "                o.concept_id = ${23821} "
            + "                    AND o.value_datetime <= DATE_ADD(pat.birtday, INTERVAL 365 DAY) "
            + "                ) "
            + "                AND ( "
            + "                o2.concept_id = ${1030} "
            + "                    AND o2.value_coded IN (${703}, ${664}) "
            + "                ) "
            + "                AND ( "
            + "                o3.concept_id = ${165502} "
            + "                    AND o3.value_coded IN (${165503}, ${165506}, ${165507}, ${165510}) "
            + "                ) "
            + "            ) "
            + "        GROUP BY "
            + "            p.patient_id "
            + "    ) virologic_hiv;";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * identify Infants who underwent sample collection for first or second virologic HIV test
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoUnderwentFirstOrSecondSampleCollectionForVirologicHivTest(
      boolean firstSample) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Underwent a sample collection for a virologic HIV test");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("23821", hivMetadata.getSampleCollectionDateAndTime().getConceptId());
    map.put("165502", hivMetadata.getSampleEarlyInfantDiagnosisConcept().getConceptId());
    map.put("165503", hivMetadata.getFirstSampleInLessThan9MonthsConcept().getConceptId());
    map.put("165506", hivMetadata.getNextSampleInLessThan9MonthsConcept().getConceptId());
    map.put("165507", hivMetadata.getFirstSampleBetween9To17MonthsConcept().getConceptId());
    map.put("165510", hivMetadata.getFollowingtSampleBetween9To17MonthsConcept().getConceptId());

    String query =
        "SELECT p.patient_id, "
            + "       Min(o.value_datetime) AS collection_date "
            + "FROM patient p "
            + "         INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "         INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "         INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "WHERE p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND o2.voided = 0 "
            + "  AND e.location_id = :location "
            + "  AND e.encounter_type = ${13} "
            + "  AND ( (o.concept_id = ${23821} "
            + "    AND o.value_datetime >= :startDate "
            + "    AND o.value_datetime <= :endDate ) "
            + "    AND (o2.concept_id = ${165502} ";
    if (firstSample) {
      query = query + "        AND o2.value_coded IN (${165503}, ${165507}) ) ) ";
    } else {
      query = query + "        AND o2.value_coded IN (${165506}, ${165510}) ) ) ";
    }
    query = query + "GROUP BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * HEI _FR2 <b> Indicator numerator </b>
   *
   * <p>The system will generate the PMTCT_HEI indicator numerator as the number of HIV-exposed
   * infants, with a virologic HIV test result returned in the reporting period, whose diagnostic
   * sample was collected by 12 months of age by considering all infants who:
   * <li>Are enrolled in CCR by report end date (HEI_FR4)
   *
   *     <p>AND
   * <li>Received a virologic HIV test result during the reporting period (HEI_FR5)
   *
   *     <p>The system will exclude the following patients:
   * <li>All patients with results returned who were older than 12 months of age at sample
   *     collection date (HEI_FR6)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPmtctHeiNumerator() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("PMTCT-HEI");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";
    String mappings1 = "endDate=${endDate},location=${location}";

    cd.addSearch(
        "infantsInCcr",
        EptsReportUtils.map(
            pmtctEidCohortQueries.getInfantsEnrolledInCcrByReportEndDate(), mappings1));

    cd.addSearch(
        "sampleCollectionHiv",
        EptsReportUtils.map(getPatientsWhoHaveVirologivHivTestResult(), mappings));

    cd.addSearch(
        "infantAge", EptsReportUtils.map(pmtctEidCohortQueries.getInfantAge(0, 365), mappings));

    cd.setCompositionString("infantsInCcr AND sampleCollectionHiv AND infantAge");
    return cd;
  }

  /**
   * HEI_FR7 | HEI_FR8 <b> Positive | Negative virologic HIV test result </b>
   *
   * <p>The system will include all infants who have “Pos” | “Neg” HIV DNA PCR result selected on
   * the Laboratório Geral Form <b>(HEI_FR5)</b> in the positive disaggregates.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPositveOrNegativePcrResult(boolean posiveResult) {
    SqlCohortDefinition scd = new SqlCohortDefinition();
    scd.setName("Positive Or Negative virologic HIV test result");
    scd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    scd.addParameter(new Parameter("endDate", "End Date", Date.class));
    scd.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("23821", hivMetadata.getSampleCollectionDateAndTime().getConceptId());
    map.put("165502", hivMetadata.getSampleEarlyInfantDiagnosisConcept().getConceptId());
    map.put("165503", hivMetadata.getFirstSampleInLessThan9MonthsConcept().getConceptId());
    map.put("165506", hivMetadata.getNextSampleInLessThan9MonthsConcept().getConceptId());
    map.put("165507", hivMetadata.getFirstSampleBetween9To17MonthsConcept().getConceptId());
    map.put("165510", hivMetadata.getFollowingtSampleBetween9To17MonthsConcept().getConceptId());
    map.put("1030", hivMetadata.getHivPCRQualitativeConceptUuid().getConceptId());
    map.put("703", hivMetadata.getPositive().getConceptId());
    map.put("664", hivMetadata.getNegative().getConceptId());

    String query =
        "SELECT "
            + "  p.patient_id "
            + "FROM "
            + "  patient p "
            + "  INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "  INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "  INNER JOIN ( "
            + "    SELECT "
            + "      p.patient_id, "
            + "      MAX(o.value_datetime) AS pcr_result_date, "
            + "      o2.value_coded AS pcr_result "
            + "    FROM "
            + "      patient p "
            + "      INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "      INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "      INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "      INNER JOIN obs o3 ON o3.encounter_id = e.encounter_id "
            + "      INNER JOIN ( "
            + "        SELECT "
            + "          p.patient_id, "
            + "          ps.birthdate AS birtday "
            + "        FROM "
            + "          patient p "
            + "          INNER JOIN person ps ON p.patient_id = ps.person_id "
            + "        WHERE "
            + "          p.voided = 0 "
            + "          AND ps.voided = 0 "
            + "      ) pat ON pat.patient_id = p.patient_id "
            + "    WHERE "
            + "      p.voided = 0 "
            + "      AND e.voided = 0 "
            + "      AND o.voided = 0 "
            + "      AND o2.voided = 0 "
            + "      AND o3.voided = 0 "
            + "      AND e.encounter_type = ${13} "
            + "      AND e.encounter_datetime >= :startDate "
            + "      AND e.encounter_datetime <= :endDate "
            + "      AND e.location_id = :location "
            + "      AND ( "
            + "        ( "
            + "          o.concept_id = ${23821} "
            + "          AND o.value_datetime <= DATE_ADD(pat.birtday, INTERVAL 365 DAY) "
            + "        ) "
            + "        AND ( "
            + "          o2.concept_id = ${1030} "
            + "          AND o2.value_coded IN (${703}, ${664}) "
            + "        ) "
            + "        AND ( "
            + "          o3.concept_id = ${165502} "
            + "          AND o3.value_coded IN (${165503}, ${165506}, ${165507}, ${165510}) "
            + "        ) "
            + "      ) "
            + "    GROUP BY "
            + "      p.patient_id "
            + "  ) virologic_hiv ON virologic_hiv.patient_id = p.patient_id "
            + "  AND p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND e.encounter_type = ${13} "
            + "  AND e.encounter_datetime >= :startDate "
            + "  AND e.encounter_datetime <= :endDate "
            + "  AND e.location_id = :location "
            + "  AND o.concept_id = ${1030} ";
    if (posiveResult) {
      query = query + "  AND virologic_hiv.pcr_result = ${703}";
    } else {
      query = query + "  AND virologic_hiv.pcr_result = ${664}";
    }

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    scd.setQuery(stringSubstitutor.replace(query));

    return scd;
  }
}
