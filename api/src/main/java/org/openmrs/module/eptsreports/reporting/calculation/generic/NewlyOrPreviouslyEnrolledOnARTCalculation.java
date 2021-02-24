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

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Months;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.eptsreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.eptsreports.reporting.calculation.BooleanResult;
import org.openmrs.module.eptsreports.reporting.calculation.common.EPTSCalculationService;
import org.openmrs.module.eptsreports.reporting.library.queries.TbPrevQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsCalculationUtils;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Patients Newly Enrolled on ART: The patients from TB_PREV_DENOMINATOR (2 AND 3) who falls under
 * “Patients Newly Enrolled on ART” are patients with the earliest IPT start Date (obtained based in
 * different set of identified fields/sources defined in 3a) within 6 months of the earliest ART
 * start Date (obtained based in different set of identified fields/sources defined in 2a): (Art
 * Start Date minus IPT Start Date <= 6months)
 *
 * <p>Patients Previously Enrolled on ART: The patients from TB_PREV_DENOMINATOR (2 AND 3) who falls
 * under “Patients Previously Enrolled on ART” are patients with the earliest TPI start Date
 * (obtained based in different set of identified fields/sources defined in 3a) within 6 months of
 * the earliest ART start Date (obtained based in different set of identified fields/sources defined
 * in 2a): (Art Start Date minus TPI Start Date > 6months)
 *
 * @return a CulculationResultMap
 */
@Component
public class NewlyOrPreviouslyEnrolledOnARTCalculation extends AbstractPatientCalculation {

  private static final int MINIMUM_DURATION_IN_MONTHS = 6;

  private static final String ON_OR_AFTER = "onOrAfter";

  private static final String ON_OR_BEFORE = "onOrBefore";

  @Autowired private HivMetadata hivMetadata;

  @Autowired private TbMetadata tbMetadata;

  @Autowired private EPTSCalculationService ePTSCalculationService;

  @SuppressWarnings("unused")
  @Override
  public CalculationResultMap evaluate(
      Collection<Integer> cohort,
      Map<String, Object> parameterValues,
      PatientCalculationContext context) {
    CalculationResultMap map = new CalculationResultMap();
    boolean isNewlyEnrolledOnArtSearch =
        getBooleanParameter(parameterValues, "isNewlyEnrolledOnArtSearch");
    Location location = (Location) context.getFromCache("location");
    Date startDate = (Date) parameterValues.get(ON_OR_AFTER);
    Date endDate = (Date) parameterValues.get(ON_OR_BEFORE);

    if (startDate == null) {
      startDate = (Date) context.getFromCache(ON_OR_AFTER);
    }

    if (endDate == null) {
      endDate = (Date) context.getFromCache(ON_OR_BEFORE);
    }

    // Start ART date is always checked against endDate, not endDate - 6m
    parameterValues.put("onOrBefore", addMonths(endDate, 6));
    CalculationResultMap artStartDates =
        calculate(
            Context.getRegisteredComponents(InitialArtStartDateCalculation.class).get(0),
            cohort,
            parameterValues,
            context);
    CalculationResultMap startProfilaxiaObservations =
        ePTSCalculationService.firstObs(
            hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept(),
            null,
            location,
            false,
            startDate,
            endDate,
            null,
            cohort,
            context);
    CalculationResultMap startDrugsObservations =
        ePTSCalculationService.getObs(
            hivMetadata.getIsoniazidUsageConcept(),
            Arrays.asList(hivMetadata.getAdultoSeguimentoEncounterType()),
            cohort,
            Arrays.asList(location),
            Arrays.asList(hivMetadata.getStartDrugs()),
            TimeQualifier.FIRST,
            startDate,
            endDate,
            context);
    CalculationResultMap tptIsoniazidMap = getTptIsoniazid(cohort, context);
    CalculationResultMap outrasPrescricoesIsoniazidMap =
        getOutrasPrescricoesIsoniazid(cohort, context);
    CalculationResultMap outrasPrescricoes3HPMap = getOutrasPrescricoes3HP(cohort, context);
    CalculationResultMap tpt3HPMap = getTpt3HP(cohort, context);

    if (endDate != null) {
      for (Integer patientId : cohort) {
        Date artStartDate =
            InitialArtStartDateCalculation.getArtStartDate(patientId, artStartDates);
        Obs seguimentoOrFichaResumo =
            EptsCalculationUtils.resultForPatient(startProfilaxiaObservations, patientId);
        Obs fichaClinicaMasterCardStartDrugsObs =
            EptsCalculationUtils.resultForPatient(startDrugsObservations, patientId);
        Obs tptIsoniazidObs = EptsCalculationUtils.resultForPatient(tptIsoniazidMap, patientId);
        Obs outrasPrescricoesIsoniazidObs =
            EptsCalculationUtils.resultForPatient(outrasPrescricoesIsoniazidMap, patientId);
        Obs outrasPrescricoes3HPObs =
            EptsCalculationUtils.resultForPatient(outrasPrescricoes3HPMap, patientId);
        Obs tpt3HPMapObs = EptsCalculationUtils.resultForPatient(tpt3HPMap, patientId);

        if ((seguimentoOrFichaResumo == null && fichaClinicaMasterCardStartDrugsObs == null
                || tptIsoniazidObs == null
                || outrasPrescricoesIsoniazidObs == null
                || outrasPrescricoes3HPObs == null
                || tpt3HPMapObs == null)
            || artStartDate == null) {
          continue;
        }

        DateTime artStartDateTime = new DateTime(artStartDate.getTime());
        DateTime iptStartDateTime =
            new DateTime(
                getEarliestIptStartDate(
                        seguimentoOrFichaResumo,
                        fichaClinicaMasterCardStartDrugsObs,
                        tptIsoniazidObs,
                        outrasPrescricoesIsoniazidObs,
                        outrasPrescricoes3HPObs,
                        tpt3HPMapObs)
                    .getTime());
        boolean isDiffMoreThanSix =
            isDateDiffGreaterThanSixMonths(artStartDateTime, iptStartDateTime);
        if (artStartDate != null
            && artStartDate.compareTo(endDate) <= 0
            && isDiffMoreThanSix == false
            && isNewlyEnrolledOnArtSearch == true) {
          map.put(patientId, new BooleanResult(true, this));
        }
        if (artStartDate != null
            && artStartDate.compareTo(endDate) <= 0
            && isDiffMoreThanSix == true
            && isNewlyEnrolledOnArtSearch == false) {
          map.put(patientId, new BooleanResult(true, this));
        }
      }
      return map;
    } else {
      throw new IllegalArgumentException(String.format("Parameter %s must be set", ON_OR_BEFORE));
    }
  }

  private Date getDateFromObs(Obs obs) {
    if (obs != null) {
      return obs.getValueDatetime();
    }
    return null;
  }
  /**
   * Gets the earliest treatment start date by comparing the drugs start date obs from Seguimento
   * (adults and children)” or “Ficha Resumo” and “Ficha Clinica-MasterCard”
   *
   * @param seguimentoOrFichaResumoDate The earliest drug start date obs from Seguimento (adults and
   *     children)” or “Ficha Resumo”
   * @param fichaClinicaMasterCardDate The earliest drug start date obs from Ficha
   *     Clinica-MasterCard
   * @return
   */
  private Date getEarliestIptStartDate(
      Obs seguimentoOrFichaResumoDate,
      Obs fichaClinicaMasterCardDate,
      Obs tptIsoniazidDate,
      Obs outrasPrescricoesIsoniazidDate,
      Obs outrasPrescricoes3HPDate,
      Obs tpt3HPMapDate) {
    if (seguimentoOrFichaResumoDate != null
        && fichaClinicaMasterCardDate != null
        && tptIsoniazidDate != null
        && outrasPrescricoesIsoniazidDate != null
        && outrasPrescricoes3HPDate != null) {
      List<Date> dates =
          Arrays.asList(
              getDateFromObs(seguimentoOrFichaResumoDate),
              fichaClinicaMasterCardDate.getObsDatetime(),
              tptIsoniazidDate.getObsDatetime(),
              outrasPrescricoesIsoniazidDate.getObsDatetime(),
              outrasPrescricoes3HPDate.getObsDatetime(),
              tpt3HPMapDate.getObsDatetime());
      return Collections.min(dates);
    } else {
      if (getDateFromObs(seguimentoOrFichaResumoDate) != null) {
        return getDateFromObs(seguimentoOrFichaResumoDate);
      } else if (tptIsoniazidDate.getObsDatetime() != null) {
        return tptIsoniazidDate.getObsDatetime();
      } else if (outrasPrescricoesIsoniazidDate.getObsDatetime() != null) {
        return outrasPrescricoesIsoniazidDate.getObsDatetime();
      } else if (outrasPrescricoes3HPDate.getObsDatetime() != null) {
        return outrasPrescricoes3HPDate.getObsDatetime();
      } else if (tpt3HPMapDate.getObsDatetime() != null) {
        return tpt3HPMapDate.getObsDatetime();
      } else {
        return fichaClinicaMasterCardDate.getObsDatetime();
      }
    }
  }
  /**
   * Checks if the difference between ART start date and IPT start date is greater than six months,
   * considering days if the difference in months is equal to 6 months
   *
   * @param artStartDateTime The ART start date
   * @param iptStartDateTime The IPT start date
   * @return true if the difference is greater to six months, false otherwise.
   */
  public boolean isDateDiffGreaterThanSixMonths(
      DateTime artStartDateTime, DateTime iptStartDateTime) {
    int artMinusIptStartDate =
        Months.monthsBetween(new DateTime(artStartDateTime), new DateTime(iptStartDateTime))
            .getMonths();
    if (artMinusIptStartDate > MINIMUM_DURATION_IN_MONTHS) {
      return true;
    }
    if (artMinusIptStartDate
        == MINIMUM_DURATION_IN_MONTHS) { // Check if there are some days after the six months (eg. 6
      // Months and 4 days)
      DateTime newEnd = iptStartDateTime.minusMonths(artMinusIptStartDate);
      int days = Days.daysBetween(artStartDateTime, newEnd).getDays();
      if (days > 0) {
        return true;
      }
    }
    return false;
  }
  /**
   * Adds a number of months to the passed-in date
   *
   * @param date the date to increment
   * @param monthsToAdd the number of months to add
   * @return date incremented by {monthsToAdd} months
   */
  public static Date addMonths(Date date, int monthsToAdd) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.add(Calendar.MONTH, monthsToAdd);
    return cal.getTime();
  }

  private boolean getBooleanParameter(Map<String, Object> parameterValues, String parameterName) {
    Boolean parameterValue = null;
    if (parameterValues != null) {
      parameterValue = (Boolean) parameterValues.get(parameterName);
    }
    if (parameterValue == null) {
      parameterValue = true;
    }
    return parameterValue;
  }

  /**
   * <b>Description:</b> Patients who have Regime de TPT with the values (“Isoniazida” or
   * “Isoniazida + Piridoxina”) marked on the first pick-up date on Ficha de Levantamento de TPT
   * (FILT) during the previous reporting period (INH Start Date) and no other INH values
   * (“Isoniazida” or “Isoniazida + Piridoxina”) marked on FILT in the 7 months prior to the INH
   * Start Date or
   *
   * <p><b>Technical Specs</b>
   *
   * </blockquote>
   *
   * @return {@link CalculationResultMap}
   */
  private CalculationResultMap getTptIsoniazid(
      Collection<Integer> cohort, PatientCalculationContext context) {
    Map<String, Object> params = new HashMap<>();
    params.put("onOrAfter", context.getFromCache("onOrAfter"));
    params.put("onOrBefore", context.getFromCache("onOrBefore"));
    params.put("location", context.getFromCache("location"));

    SqlPatientDataDefinition def = new SqlPatientDataDefinition();
    def.addParameter(new Parameter("startDate", "onOrAfter", Date.class));
    def.addParameter(new Parameter("endDate", "onOrBefore", Date.class));
    def.addParameter(new Parameter("location", "location", Location.class));
    def.setQuery(
        TbPrevQueries.getRegimeTPTOrOutrasPrescricoes(
            tbMetadata.getRegimeTPTEncounterType(),
            tbMetadata.getRegimeTPTConcept(),
            Arrays.asList(
                tbMetadata.getIsoniazidConcept(), tbMetadata.getIsoniazidePiridoxinaConcept()),
            7));

    return EptsCalculationUtils.evaluateWithReporting(def, cohort, params, null, context);
  }

  /**
   * <b>Description:</b> Patients who have Outras Prescrições with the values (DT-INH) marked on
   * Ficha Clínica - Mastercard during the previous reporting period (INH Start Date) and no other
   * DT-INH values marked on Ficha Clinica in the 7 months prior to the INH Start Date
   *
   * <p><b>Technical Specs</b>
   *
   * </blockquote>
   *
   * @return {@link CalculationResultMap}
   */
  private CalculationResultMap getOutrasPrescricoesIsoniazid(
      Collection<Integer> cohort, PatientCalculationContext context) {
    Map<String, Object> params = new HashMap<>();
    params.put("onOrAfter", context.getFromCache("onOrAfter"));
    params.put("onOrBefore", context.getFromCache("onOrBefore"));
    params.put("location", context.getFromCache("location"));

    SqlPatientDataDefinition def = new SqlPatientDataDefinition();
    def.addParameter(new Parameter("startDate", "onOrAfter", Date.class));
    def.addParameter(new Parameter("endDate", "onOrBefore", Date.class));
    def.addParameter(new Parameter("location", "location", Location.class));
    def.setQuery(
        TbPrevQueries.getRegimeTPTOrOutrasPrescricoes(
            hivMetadata.getAdultoSeguimentoEncounterType(),
            tbMetadata.getTreatmentPrescribedConcept(),
            Arrays.asList(tbMetadata.getDtINHConcept()),
            7));

    return EptsCalculationUtils.evaluateWithReporting(def, cohort, params, null, context);
  }

  /**
   * <b>Description:</b> Patients who have Outras Prescrições with the value “3HP” marked on Ficha
   * Clínica - Mastercard during the previous reporting period (3HP Start Date) and no other 3HP
   * prescriptions marked on Ficha-Clinica in the 4 months prior to the 3HP Start Date
   *
   * <p><b>Technical Specs</b>
   *
   * </blockquote>
   *
   * @return {@link CalculationResultMap}
   */
  private CalculationResultMap getOutrasPrescricoes3HP(
      Collection<Integer> cohort, PatientCalculationContext context) {
    Map<String, Object> params = new HashMap<>();
    params.put("onOrAfter", context.getFromCache("onOrAfter"));
    params.put("onOrBefore", context.getFromCache("onOrBefore"));
    params.put("location", context.getFromCache("location"));

    SqlPatientDataDefinition def = new SqlPatientDataDefinition();
    def.addParameter(new Parameter("startDate", "onOrAfter", Date.class));
    def.addParameter(new Parameter("endDate", "onOrBefore", Date.class));
    def.addParameter(new Parameter("location", "location", Location.class));
    def.setQuery(
        TbPrevQueries.getRegimeTPTOrOutrasPrescricoes(
            hivMetadata.getAdultoSeguimentoEncounterType(),
            tbMetadata.getTreatmentPrescribedConcept(),
            Arrays.asList(tbMetadata.get3HPConcept()),
            4));

    return EptsCalculationUtils.evaluateWithReporting(def, cohort, params, null, context);
  }

  /**
   * <b>Description:</b> Patients who have Regime de TPT with the values “3HP or 3HP + Piridoxina”
   * marked on the first pick-up date on Ficha de Levantamento de TPT (FILT) during the previous
   * reporting period (3HP Start Date) and no other 3HP pick-ups marked on FILT in the 4 months
   * prior to the 3HP Start Date
   *
   * <p><b>Technical Specs</b>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  private CalculationResultMap getTpt3HP(
      Collection<Integer> cohort, PatientCalculationContext context) {
    Map<String, Object> params = new HashMap<>();
    params.put("onOrAfter", context.getFromCache("onOrAfter"));
    params.put("onOrBefore", context.getFromCache("onOrBefore"));
    params.put("location", context.getFromCache("location"));

    SqlPatientDataDefinition def = new SqlPatientDataDefinition();
    def.addParameter(new Parameter("startDate", "onOrAfter", Date.class));
    def.addParameter(new Parameter("endDate", "onOrBefore", Date.class));
    def.addParameter(new Parameter("location", "location", Location.class));
    def.setQuery(
        TbPrevQueries.getRegimeTPTOrOutrasPrescricoes(
            tbMetadata.getRegimeTPTEncounterType(),
            tbMetadata.getRegimeTPTConcept(),
            Arrays.asList(tbMetadata.get3HPConcept(), tbMetadata.get3HPPiridoxinaConcept()),
            4));

    return EptsCalculationUtils.evaluateWithReporting(def, cohort, params, null, context);
  }
}
