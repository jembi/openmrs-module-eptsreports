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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.calculation.txcurr.LessThan3MonthsOfArvDispensationCalculation;
import org.openmrs.module.eptsreports.reporting.calculation.txcurr.SixMonthsAndAboveOnArvDispensationCalculation;
import org.openmrs.module.eptsreports.reporting.calculation.txcurr.ThreeToFiveMonthsOnArtDispensationCalculation;
import org.openmrs.module.eptsreports.reporting.cohort.definition.CalculationCohortDefinition;
import org.openmrs.module.eptsreports.reporting.library.queries.CommonQueries;
import org.openmrs.module.eptsreports.reporting.library.queries.TXCurrQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsQueriesUtil;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.definition.library.DocumentedDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Defines all of the TxCurrCohortQueries we want to expose for EPTS */
@Component
public class TxCurrCohortQueries {

  private static final int OLD_SPEC_ABANDONMENT_DAYS = 60;

  private static final int CURRENT_SPEC_ABANDONMENT_DAYS = 31;

  private final String oldArtStartPeriod = " '2023-12-21' ";

  @Autowired private HivMetadata hivMetadata;

  @Autowired private CommonMetadata commonMetadata;

  @Autowired private HivCohortQueries hivCohortQueries;

  @Autowired private GenericCohortQueries genericCohortQueries;

  @Autowired private CommonQueries commonQueries;

  @Autowired private TxTbMonthlyCascadeCohortQueries txTbMonthlyCascadeCohortQueries;

  /**
   * @param cohortName Cohort name
   * @param currentSpec - true for is current and false the opposit
   * @return TxCurr composition cohort definition
   */
  @DocumentedDefinition(value = "getTxCurrCompositionCohort")
  public CohortDefinition getTxCurrCompositionCohort(String cohortName, boolean currentSpec) {

    final int abandonmentDays =
        currentSpec ? CURRENT_SPEC_ABANDONMENT_DAYS : OLD_SPEC_ABANDONMENT_DAYS;

    CompositionCohortDefinition txCurrComposition = new CompositionCohortDefinition();
    txCurrComposition.setName(cohortName);

    txCurrComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    txCurrComposition.addParameter(new Parameter("location", "location", Location.class));
    txCurrComposition.addParameter(new Parameter("locations", "location", Location.class));

    CohortDefinition inARTProgramAtEndDate =
        genericCohortQueries.createInProgram("InARTProgram", hivMetadata.getARTProgram());
    txCurrComposition
        .getSearches()
        .put(
            "111",
            EptsReportUtils.map(
                inARTProgramAtEndDate, "onOrBefore=${onOrBefore},locations=${location}"));

    txCurrComposition
        .getSearches()
        .put(
            "2",
            EptsReportUtils.map(
                hivCohortQueries.getPatientWithHistoricalDrugStartDateObsBeforeOrOnEndDate(),
                "onOrBefore=${onOrBefore},location=${location}"));

    txCurrComposition
        .getSearches()
        .put(
            "3",
            EptsReportUtils.map(
                getPatientEnrolledInArtProgramByEndReportingPeriod(),
                "onOrBefore=${onOrBefore},location=${location}"));

    txCurrComposition
        .getSearches()
        .put(
            "4",
            EptsReportUtils.map(
                getPatientWithFirstDrugPickupEncounterBeforeOrOnEndDate(),
                "onOrBefore=${onOrBefore},location=${location}"));

    txCurrComposition
        .getSearches()
        .put(
            "555",
            EptsReportUtils.map(
                getPatientsWhoLeftARTProgramBeforeOrOnEndDate(),
                "onOrBefore=${onOrBefore},location=${location}"));
    // section 2
    txCurrComposition
        .getSearches()
        .put(
            "666",
            EptsReportUtils.map(
                getPatientsThatMissedNexPickup(),
                String.format(
                    "onOrBefore=${onOrBefore},location=${location},abandonmentDays=%s",
                    abandonmentDays)));
    txCurrComposition
        .getSearches()
        .put(
            "suspended",
            EptsReportUtils.map(
                getPatientsWhoStoppedOrSuspendedTreatment(),
                "onOrBefore=${onOrBefore},location=${location}"));
    txCurrComposition
        .getSearches()
        .put(
            "777",
            EptsReportUtils.map(
                getPatientsThatDidNotMissNextConsultation(),
                String.format(
                    "onOrBefore=${onOrBefore},location=${location},abandonmentDays=%s",
                    abandonmentDays)));
    txCurrComposition
        .getSearches()
        .put(
            "died",
            EptsReportUtils.map(
                getPatientsWhoAreDead(), "onOrBefore=${onOrBefore},location=${location}"));
    txCurrComposition
        .getSearches()
        .put(
            "888",
            EptsReportUtils.map(
                getPatientsReportedAsAbandonmentButStillInPeriod(),
                String.format(
                    "onOrBefore=${onOrBefore},location=${location},abandonmentDays=%s",
                    abandonmentDays)));

    txCurrComposition
        .getSearches()
        .put(
            "transferredOut",
            EptsReportUtils.map(
                getPatientsWhoAreTransferredOutToAnotherHf(),
                "onOrBefore=${onOrBefore},location=${location}"));

    // section 3
    txCurrComposition
        .getSearches()
        .put(
            "13",
            EptsReportUtils.map(
                getPatientHavingLastScheduledDrugPickupDateDaysBeforeEndDate(28),
                "onOrBefore=${onOrBefore},location=${location}"));
    txCurrComposition
        .getSearches()
        .put(
            "withoutPickup",
            EptsReportUtils.map(
                getNumberOfPatientsWhoAbandonedArtWithoutPickup(),
                "endDate=${onOrBefore},location=${location}"));
    txCurrComposition
        .getSearches()
        .put(
            "14",
            EptsReportUtils.map(
                getPatientWithoutScheduledDrugPickupDateMasterCardAmdArtPickup(),
                "onOrBefore=${onOrBefore},location=${location}"));

    txCurrComposition
        .getSearches()
        .put(
            "mostRecentSchedule",
            EptsReportUtils.map(
                getTransferredOutBetweenNextPickupDateFilaAndRecepcaoLevantou(),
                "onOrBefore=${onOrBefore},location=${location}"));

    txCurrComposition
        .getSearches()
        .put(
            "startedArtBeforeDecember2023",
            EptsReportUtils.map(
                getPatientsWhoEverInitiatedTreatmentBeforeDecember2023(),
                "endDate=${onOrBefore},location=${location}"));

    txCurrComposition
        .getSearches()
        .put(
            "startedArtAfterDecember2023",
            EptsReportUtils.map(
                getPatientsWhoStartedArtAfterDecember2023AndHasDrugPickupByReportEndDate(),
                "endDate=${onOrBefore},location=${location}"));

    txCurrComposition
        .getSearches()
        .put(
            "transferredIn",
            EptsReportUtils.map(
                getPatientsWhoAreTransferredIn(), "endDate=${onOrBefore},location=${location}"));

    String compositionString;
    if (currentSpec) {
      compositionString =
          "(startedArtBeforeDecember2023 OR startedArtAfterDecember2023 OR transferredIn) AND NOT (suspended OR died OR (transferredOut AND mostRecentSchedule) OR 13 OR 14 OR withoutPickup) ";

    } else {
      compositionString = "(111 OR 2 OR 3 OR 4) AND (NOT (555 OR (666 AND (NOT (777 OR 888)))))";
    }

    txCurrComposition.setCompositionString(compositionString);
    return txCurrComposition;
  }

  /**
   * All patients whose earliest ART start date from pick-up and clinical sources (CURR_FR4.1) falls
   * before (<) 21 December 2023 and this date falls by the end of the reporting period.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoEverInitiatedTreatmentBeforeDecember2023() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients With Art Start Date Before December 21st 2023");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Facility", Location.class));

    String query =
        "       SELECT start_art.patient_id "
            + " FROM ( "
            + commonQueries.getARTStartDate(true)
            + "       ) start_art "
            + " WHERE start_art.first_pickup < "
            + oldArtStartPeriod;

    sqlCohortDefinition.setQuery(query);
    return sqlCohortDefinition;
  }

  /**
   * All patients whose earliest ART start date from pick-up and clinical sources (CURR_FR4.1) falls
   * before (<) 21 December 2023 and this date falls by the end of the reporting period.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoEverInitiatedTreatmentAfterDecember2023() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients With Art Start Date After December 21st 2023");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Facility", Location.class));

    String query =
        "       SELECT start_art.patient_id "
            + " FROM ( "
            + commonQueries.getARTStartDate(true)
            + "       ) start_art "
            + " WHERE start_art.first_pickup >= "
            + oldArtStartPeriod;

    sqlCohortDefinition.setQuery(query);
    return sqlCohortDefinition;
  }

  public CohortDefinition getPatientsWhoAreTransferredIn() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.setName("Patient Who Ever Started Art");

    cd.addSearch(
        "transferredInProgram",
        EptsReportUtils.map(
            txTbMonthlyCascadeCohortQueries.getPatientsTransferredInFromProgram(),
            "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "transferredInFichaResumo",
        EptsReportUtils.map(
            txTbMonthlyCascadeCohortQueries.getPatientsTransferredInFromFichaResumo(),
            "endDate=${endDate},location=${location}"));

    cd.setCompositionString("transferredInProgram OR transferredInFichaResumo");
    return cd;
  }

  /**
   * Patients whose first ever drug pick-up date between the following sources falls by the end of
   * the reporting endDate
   * <li>Drug pick-up date registered on (FILA)
   * <li>Drug pick-up date registered on (Recepção Levantou ARV) – Master Card
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsFirstDrugPickupEver() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients First Drug Pick-up Date Ever ");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Facility", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    valuesMap.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    valuesMap.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());

    String query =
        new EptsQueriesUtil()
            .patientIdQueryBuilder(CommonQueries.getFirstDrugPickOnFilaOrRecepcaoLevantouQuery())
            .getQuery();

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * All patients whose earliest ART start date (CURR_FR4.1) falls on or after (>=) 21 December 2023
   * AND
   *
   * <p>whose first ever drug pick-up date between the following sources falls by the end of the
   * reporting period:
   * <li>Drug pick-up date registered on (FILA)
   * <li>Drug pick-up date registered on (Recepção Levantou ARV) – Master Card
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition
      getPatientsWhoStartedArtAfterDecember2023AndHasDrugPickupByReportEndDate() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.setName("Patient Who Ever Started Art");

    cd.addSearch(
        "startedAfterDecember2023",
        EptsReportUtils.map(
            getPatientsWhoEverInitiatedTreatmentAfterDecember2023(),
            "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "drugPickupEver",
        EptsReportUtils.map(
            getPatientsFirstDrugPickupEver(), "endDate=${endDate},location=${location}"));

    cd.setCompositionString("startedAfterDecember2023 AND drugPickupEver");
    return cd;
  }

  @DocumentedDefinition("TX_CURR base cohort")
  public CohortDefinition getTxCurrBaseCohort() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Facility", Location.class));
    cd.setName("TX_CURR base cohort");
    CohortDefinition baseCohort = genericCohortQueries.getBaseCohort();
    CohortDefinition txCurr = getTxCurrCompositionCohort("tx_curr", true);

    cd.addSearch("baseCohort", Mapped.mapStraightThrough(baseCohort));

    String txCurrMappings = "onOrBefore=${endDate},location=${location},locations=${location}";
    cd.addSearch("txCurr", EptsReportUtils.map(txCurr, txCurrMappings));

    cd.setCompositionString("baseCohort AND txCurr");
    return cd;
  }

  /**
   * <b>Description: 1 –</b> Number of patients registered with Start Drugs
   *
   * <p><b>Technical Specs</b>
   *
   * <blockquote>
   *
   * Cohort of patients registered as START DRUGS <b>(concept_id = 1255)</b> in ARV PLAN
   * <b>(concept_id = 1256)</b>
   *
   * <p>In the first drug pickup <b>(encounterType_id = 18)</b> or follow up consultation for adults
   * and children <b>(encounterType_id = 6 and 5)</b> before or on End Date <b>encounter_datetime <=
   * endDate</b>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  @DocumentedDefinition(value = "patientWithSTARTDRUGSObs")
  public CohortDefinition getPatientWithSTARTDRUGSObsBeforeOrOnEndDate() {
    SqlCohortDefinition patientWithSTARTDRUGSObs = new SqlCohortDefinition();
    patientWithSTARTDRUGSObs.setName("patientWithSTARTDRUGSObs");

    patientWithSTARTDRUGSObs.setQuery(
        TXCurrQueries.getPatientWithSTARTDRUGSObsBeforeOrOnEndDate(
            hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId(),
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getARVPlanConcept().getConceptId(),
            hivMetadata.getStartDrugsConcept().getConceptId()));
    patientWithSTARTDRUGSObs.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    patientWithSTARTDRUGSObs.addParameter(new Parameter("location", "location", Location.class));
    return patientWithSTARTDRUGSObs;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <b>3 –</b> Number of patients enrolled in ART Program <b>(program_id = 2)</b> by end of
   * reporting period.
   *
   * <p>Table: patient_program and date_enrolled <= endDate
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  @DocumentedDefinition(value = "patientEnrolledInArtProgramByEndReportingPeriod")
  public CohortDefinition getPatientEnrolledInArtProgramByEndReportingPeriod() {
    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("patientEnrolledInArtProgramByEndReportingPeriod");

    definition.setQuery(
        TXCurrQueries.getPatientEnrolledInArtProgramByEndReportingPeriod(
            hivMetadata.getARTProgram().getProgramId()));

    definition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));

    return definition;
  }

  /**
   * <b>Techincal Specs</b>
   *
   * <blockquote>
   *
   * <b>4 –</b> Cohort of patients with first drug pickup <b>(encounterType_id = 18)</b> before or
   * on End Date <b>encounter_datetime <= endDate</b>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  @DocumentedDefinition(value = "patientWithFirstDrugPickupEncounter")
  public CohortDefinition getPatientWithFirstDrugPickupEncounterBeforeOrOnEndDate() {
    SqlCohortDefinition patientWithFirstDrugPickupEncounter = new SqlCohortDefinition();
    patientWithFirstDrugPickupEncounter.setName("patientWithFirstDrugPickupEncounter");

    patientWithFirstDrugPickupEncounter.setQuery(
        TXCurrQueries.getPatientWithFirstDrugPickupEncounterBeforeOrOnEndDate(
            hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId()));
    patientWithFirstDrugPickupEncounter.addParameter(
        new Parameter("onOrBefore", "onOrBefore", Date.class));
    patientWithFirstDrugPickupEncounter.addParameter(
        new Parameter("location", "location", Location.class));
    return patientWithFirstDrugPickupEncounter;
  }

  /**
   * <b>Techincal Specs</b>
   *
   * <blockquote>
   *
   * <b>5 –</b> All patients who have picked up drugs (Recepção Levantou ARV) – Master Card
   * <b>(encounterType_id = 52)</b>
   *
   * <p>The earliest “Data de Levantamento” <b>(concept_id = 23866)</b> by end of reporting period
   * <b>(obs value_datetime) <= endDate</b>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  @DocumentedDefinition(value = "patientsWhoHavePickedUpDrugsMasterCardByEndReporingPeriod")
  public CohortDefinition getPatientsWhoHavePickedUpDrugsMasterCardByEndReporingPeriod() {
    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("patientsWhoHavePickedUpDrugsMasterCardByEndReporingPeriod");

    String query =
        "select p.patient_id "
            + "  from patient p "
            + "  inner join encounter e on  e.patient_id=p.patient_id "
            + "  inner join obs o on  o.encounter_id=e.encounter_id "
            + "  where  e.encounter_type = %s and o.concept_id = %s "
            + "  and o.value_datetime <= :onOrBefore and e.location_id = :location "
            + "  and p.voided =0 and e.voided=0  and o.voided = 0 group by p.patient_id";

    definition.setQuery(
        String.format(
            query,
            hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId(),
            hivMetadata.getArtDatePickupMasterCard().getConceptId()));

    definition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));

    return definition;
  }

  /**
   * <b>Description:</b>
   *
   * <p><b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <b>6 –</b> All deaths <b>(Patient_State.state = 10)</b>, Transferred-out
   * <b>(Patient_State.state = 7)</b> and Suspensions <b>(Patient_State.state = 8)</b>
   *
   * <p>Registered in Patient Program State by reporting end date <b>Patient_State.start_date <=
   * endDate Patient_state.end_date</b> is null
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  @DocumentedDefinition(
      value = "patientsDeadTransferredOutSuspensionsInProgramStateByReportingEndDate")
  public CohortDefinition
      getPatientsDeadTransferredOutSuspensionsInProgramStateByReportingEndDate() {

    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("patientsDeadTransferredOutSuspensionsInProgramStateByReportingEndDate");

    definition.setQuery(
        TXCurrQueries.getPatientsDeadTransferredOutSuspensionsInProgramStateByReportingEndDate(
            hivMetadata.getARTProgram().getProgramId(),
            hivMetadata
                .getTransferredOutToAnotherHealthFacilityWorkflowState()
                .getProgramWorkflowStateId(),
            hivMetadata.getSuspendedTreatmentWorkflowState().getProgramWorkflowStateId(),
            hivMetadata.getArtDeadWorkflowState().getProgramWorkflowStateId()));

    definition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));

    return definition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <b>7 –</b> All deaths registered in Patient Demographics by reporting end date Person.Dead=1
   * and death_date <= :endDate
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  @DocumentedDefinition(value = "deadPatientsInDemographiscByReportingEndDate")
  public CohortDefinition getDeadPatientsInDemographiscByReportingEndDate() {
    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("deadPatientsInDemographiscByReportingEndDate");

    definition.setQuery(
        TXCurrQueries.getDeadPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate());
    definition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));

    return definition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <b>8 –</b> All deaths <b>(concpet_id = 1383)</b> registered in Last Home Visit Card
   * <b>(EncounterType_ids in 21, 36, 37)</b>
   *
   * <p>Where Patient not found <b>(Concept_id = 2003)</b> and NO <b>(concept_id = 1066)</b> With
   * Reason of Not Finding <b>(concept_id = 2031)</b> by reporting end date Last
   * <b>encounter_datetime <= endDate</b>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  @DocumentedDefinition(value = "patientDeathRegisteredInLastHomeVisitCardByReportingEndDate")
  public CohortDefinition getPatientDeathRegisteredInLastHomeVisitCardByReportingEndDate() {
    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("patientDeathRegisteredInLastHomeVisitCardByReportingEndDate");

    definition.setQuery(
        TXCurrQueries.getPatientDeathRegisteredInLastHomeVisitCardByReportingEndDate(
            hivMetadata.getBuscaActivaEncounterType().getEncounterTypeId(),
            hivMetadata.getVisitaApoioReintegracaoParteAEncounterType().getEncounterTypeId(),
            hivMetadata.getVisitaApoioReintegracaoParteBEncounterType().getEncounterTypeId(),
            hivMetadata.getReasonPatientNotFound().getConceptId(),
            hivMetadata.getReasonPatientNotFoundByActivist2ndVisitConcept().getConceptId(),
            hivMetadata.getReasonPatientNotFoundByActivist3rdVisitConcept().getConceptId(),
            hivMetadata.getPatientHasDiedConcept().getConceptId()));

    definition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));

    return definition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <b>9 –</b> All deaths registered in Ficha Resumo and Ficha Clinica of Master Card by reporting
   * end date Encounter Type ID= 6 or 53 Estado de Permanencia (Concept Id 6272) = Dead (Concept ID
   * 1366) Encounter_datetime <= endDate
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  @DocumentedDefinition(value = "deadPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate")
  public CohortDefinition getDeadPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate() {
    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("deadPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate");

    definition.setQuery(
        TXCurrQueries.getDeadPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getStateOfStayPriorArtPatientConcept().getConceptId(),
            hivMetadata.getStateOfStayOfArtPatient().getConceptId(),
            hivMetadata.getPatientHasDiedConcept().getConceptId()));

    definition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));

    return definition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <b>10 –</b> All transferred-outs registered in Ficha Resumo and Ficha Clinica of Master Card
   * <b>(encounterType_id = 6 OR 53)</b>, Estado de Permanencia <b>(concept_id = 6272)</b>
   *
   * <p>With Transferred-out <b>(concept_id = 1706)</b> by reporting end date <b>(encounter_datetime
   * <= endDate)</b>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  @DocumentedDefinition(
      value = "transferredOutPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate")
  public CohortDefinition
      getTransferredOutPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate() {
    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("transferredOutPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate");

    definition.setQuery(
        TXCurrQueries.getTransferredOutPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getStateOfStayOfArtPatient().getConceptId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getStateOfStayPriorArtPatientConcept().getConceptId(),
            hivMetadata.getTransferredOutConcept().getConceptId()));

    definition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));

    return definition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <b>11 –</b> All suspensions registered in Ficha Resumo and Ficha Clinica of Master Card
   * <b>(encounterType_id = 6 OR 53)</b>, Estado de Permanencia <b>(concept_id = 6272)</b>
   *
   * <p>With Suspended treatment <b>(concept_id = 1709)</b> by reporting end date
   * <b>(encounter_datetime <= endDate)</b>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  @DocumentedDefinition(
      value = "patientSuspendedInFichaResumeAndClinicaOfMasterCardByReportEndDate")
  public CohortDefinition getPatientSuspendedInFichaResumeAndClinicaOfMasterCardByReportEndDate() {
    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("patientSuspendedInFichaResumeAndClinicaOfMasterCardByReportEndDate");

    definition.setQuery(
        TXCurrQueries.getPatientSuspendedInFichaResumeAndClinicaOfMasterCardByReportEndDate(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getStateOfStayOfArtPatient().getConceptId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getStateOfStayPriorArtPatientConcept().getConceptId(),
            hivMetadata.getSuspendedTreatmentConcept().getConceptId()));

    definition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));

    return definition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <b>Patients who experienced Interruption in Treatment (IIT)</b>
   *
   * <p>All patients with the most recent date between next scheduled drug pickup date (FILA) and 30
   * days after last ART pickup date (Ficha Recepção – Levantou ARVs) and adding 28 days and this
   * date being less than reporting end date
   *
   * </blockquote>
   *
   * @param numDays number of days to add
   * @return {@link CohortDefinition}
   */
  @DocumentedDefinition(value = "patientHavingLastScheduledDrugPickupDate")
  public CohortDefinition getPatientHavingLastScheduledDrugPickupDateDaysBeforeEndDate(
      int numDays) {
    SqlCohortDefinition definition = new SqlCohortDefinition();

    definition.setName("patientHavingLastScheduledDrugPickupDate");

    definition.setQuery(
        TXCurrQueries.getPatientHavingLastScheduledDrugPickupDate(
            hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId(),
            hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId(),
            commonMetadata.getReturnVisitDateConcept().getConceptId(),
            hivMetadata.getArtDatePickupMasterCard().getConceptId(),
            hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId(),
            numDays));

    definition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));

    return definition;
  }

  /**
   * <b>Description:</b> Number of patients without scheduled Date Drug and ART Pickup
   *
   * <p><b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <b>14 –</b> All patients who do not have the next scheduled drug pick update <b>(concept_id
   * 5096 = RETURN VISIT DATE FOR ARV DRUG)</b>
   *
   * <p>And ART Pickup date (<b>(concept_id = 23866)</b> – Recepção – Levantou ARV).
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  @DocumentedDefinition(value = "patientWithoutScheduledDrugPickupDateMasterCardAmdArtPickup")
  public CohortDefinition getPatientWithoutScheduledDrugPickupDateMasterCardAmdArtPickup() {
    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("patientWithoutScheduledDrugPickupDateMasterCardAmdArtPickup");

    definition.setQuery(
        TXCurrQueries.getPatientWithoutScheduledDrugPickupDateMasterCardAmdArtPickup(
            hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId(),
            hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId(),
            hivMetadata.getArtDatePickupMasterCard().getConceptId()));

    definition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));

    return definition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <b>555 –</b> Cohort of patients who left ART program <b>(program_id = 2)</b> before or on end
   * date(4) patient state<b>.start Date <= endDate</b>.
   *
   * <p>Includes: dead, transferred to, stopped and abandoned <b>(patient state_id = 10, 7, 8 or
   * 9)</b> respectively
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  @DocumentedDefinition(value = "leftARTProgramBeforeOrOnEndDate")
  public CohortDefinition getPatientsWhoLeftARTProgramBeforeOrOnEndDate() {
    SqlCohortDefinition leftARTProgramBeforeOrOnEndDate = new SqlCohortDefinition();
    leftARTProgramBeforeOrOnEndDate.setName("leftARTProgramBeforeOrOnEndDate");

    String leftARTProgramQueryString =
        "select p.patient_id from patient p inner join patient_program pg on p.patient_id=pg.patient_id "
            + " inner join patient_state ps on pg.patient_program_id=ps.patient_program_id "
            + " where pg.voided=0 and ps.voided=0 and p.voided=0 and pg.program_id=%s"
            + "  and ps.state in (%s) and ps.start_date<=:onOrBefore and pg.location_id=:location group by p.patient_id";

    String abandonStates =
        StringUtils.join(
            Arrays.asList(
                hivMetadata
                    .getTransferredOutToAnotherHealthFacilityWorkflowState()
                    .getProgramWorkflowStateId(),
                hivMetadata.getSuspendedTreatmentWorkflowState().getProgramWorkflowStateId(),
                hivMetadata.getPatientHasDiedWorkflowState().getProgramWorkflowStateId(),
                hivMetadata.getAbandonedWorkflowState().getProgramWorkflowStateId()),
            ',');

    leftARTProgramBeforeOrOnEndDate.setQuery(
        String.format(
            leftARTProgramQueryString, hivMetadata.getARTProgram().getProgramId(), abandonStates));

    leftARTProgramBeforeOrOnEndDate.addParameter(
        new Parameter("onOrBefore", "onOrBefore", Date.class));
    leftARTProgramBeforeOrOnEndDate.addParameter(
        new Parameter("location", "location", Location.class));
    return leftARTProgramBeforeOrOnEndDate;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <b>666 –</b> Cohort of patients that from the date scheduled for next drug pickup <b>(concept
   * 5096=RETURN VISIT DATE FOR ARV DRUG)</b> until end date have completed 28 days and have not
   * returned
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  @DocumentedDefinition(value = "patientsThatMissedNexPickup")
  private CohortDefinition getPatientsThatMissedNexPickup() {
    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("patientsThatMissedNexPickup");
    String query =
        "SELECT patient_id FROM (SELECT p.patient_id,max(encounter_datetime) encounter_datetime FROM patient p INNER JOIN encounter e on e.patient_id=p.patient_id WHERE p.voided=0 AND e.voided=0 AND e.encounter_type=%s"
            + "  AND e.location_id=:location AND e.encounter_datetime<=:onOrBefore group by p.patient_id ) max_frida INNER JOIN obs o on o.person_id=max_frida.patient_id WHERE max_frida.encounter_datetime=o.obs_datetime AND o.voided=0 AND o.concept_id=%s"
            + "  AND o.location_id=:location AND datediff(:onOrBefore,o.value_datetime)>=:abandonmentDays";
    definition.setQuery(
        String.format(
            query,
            hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId(),
            hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId()));
    definition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));
    definition.addParameter(new Parameter("abandonmentDays", "abandonmentDays", Integer.class));
    return definition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <b>777 –</b> Cohort of patients that from the date scheduled for next follow up consultation
   * <b>(concept 1410=RETURN VISIT DATE)</b> until the end date have not completed 28 days
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  @DocumentedDefinition(value = "patientsThatDidNotMissNextConsultation")
  private CohortDefinition getPatientsThatDidNotMissNextConsultation() {
    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("patientsThatDidNotMissNextConsultation");
    String query =
        "SELECT patient_id FROM "
            + " (SELECT p.patient_id,max(encounter_datetime) encounter_datetime "
            + " FROM patient p INNER JOIN encounter e ON e.patient_id=p.patient_id "
            + " WHERE p.voided=0 AND e.voided=0 AND e.encounter_type in (%d, %d) "
            + " AND e.location_id=:location AND e.encounter_datetime<=:onOrBefore group by p.patient_id ) max_mov "
            + " INNER JOIN obs o ON o.person_id=max_mov.patient_id "
            + " WHERE max_mov.encounter_datetime=o.obs_datetime AND o.voided=0 AND o.concept_id=%d "
            + " AND o.location_id=:location AND DATEDIFF(:onOrBefore,o.value_datetime)<:abandonmentDays";
    definition.setQuery(
        String.format(
            query,
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getReturnVisitDateConcept().getConceptId()));
    definition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));
    definition.addParameter(new Parameter("abandonmentDays", "abandonmentDays", Integer.class));
    return definition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <b>888 –</b> Cohort of patients that were registered as abandonment <b>(patient state_id =
   * 9)</b> </b> but from the date scheduled for next drug pick up <b>(concept 5096=RETURN VISIT
   * DATE)</b> until the end date have not completed 28 days
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  @DocumentedDefinition(value = "patientsReportedAsAbandonmentButStillInPeriod")
  private CohortDefinition getPatientsReportedAsAbandonmentButStillInPeriod() {
    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("patientsReportedAsAbandonmentButStillInPeriod");
    String query =
        "SELECT abandono.patient_id FROM (SELECT pg.patient_id FROM patient p INNER JOIN patient_program pg ON p.patient_id=pg.patient_id INNER JOIN patient_state ps ON pg.patient_program_id=ps.patient_program_id WHERE pg.voided=0 AND ps.voided=0 AND p.voided=0 AND pg.program_id=%d "
            + " AND ps.state=%d "
            + " AND ps.start_date<=:onOrBefore AND location_id=:location )abandono INNER JOIN ( SELECT max_frida.patient_id,max_frida.encounter_datetime,o.value_datetime FROM ( SELECT p.patient_id,max(encounter_datetime) encounter_datetime FROM patient p INNER JOIN encounter e ON e.patient_id=p.patient_id WHERE p.voided=0 AND e.voided=0 AND e.encounter_type=%d "
            + " AND e.location_id=:location AND e.encounter_datetime<=:onOrBefore group by p.patient_id ) max_frida INNER JOIN obs o ON o.person_id=max_frida.patient_id WHERE max_frida.encounter_datetime=o.obs_datetime AND o.voided=0 AND o.concept_id=%d "
            + " AND o.location_id=:location ) ultimo_fila ON abandono.patient_id=ultimo_fila.patient_id WHERE datediff(:onOrBefore,ultimo_fila.value_datetime)<:abandonmentDays";
    definition.setQuery(
        String.format(
            query,
            hivMetadata.getARTProgram().getProgramId(),
            hivMetadata.getAbandonedWorkflowState().getProgramWorkflowStateId(),
            hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId(),
            hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId()));
    definition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));
    definition.addParameter(new Parameter("abandonmentDays", "abandonmentDays", Integer.class));
    return definition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <b>12 –</b> Except all patients who after the most recent date from 2.1 to 2.6, have a drugs
   * pick up or consultation <b>(encounterType_id= 6, 9, 18)</b>
   *
   * <p>And encounter_datetime > the most recent date or "Drug pickup" <b>(encounterType_id =
   * 52)</b> and “Data de Levantamento” <b>(Concept Id 23866 value_datetime) > the most recent
   * date</b>.
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  @DocumentedDefinition(value = "patientWhoAfterMostRecentDateHaveDrusPickupOrConsultation")
  public CohortDefinition getPatientWhoAfterMostRecentDateHaveDrusPickupOrConsultation() {
    SqlCohortDefinition definition = new SqlCohortDefinition();

    definition.setName("patientWhoAfterMostRecentDateHaveDrusPickupOrConsultation");

    definition.setQuery(
        TXCurrQueries.getPatientWhoAfterMostRecentDateHaveDrusPickupOrConsultation(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId(),
            hivMetadata.getArtDatePickupMasterCard().getConceptId()));

    definition.addParameter(new Parameter("location", "location", Location.class));

    return definition;
  }

  /**
   * <b>Patients who Stopped/Suspended the treatment</b>
   * <li>Patients enrolled on ART Program (Service TARV – Tratamento) with the following last state:
   *     Suspension or;
   * <li>Patients whose most recently informed “Mudança no Estado de Permanência TARV” is “Suspenso”
   *     on Ficha Resumo or Ficha Clinica – Mastercard.
   *
   *     <p><b>Note:</b>
   *
   *     <p>Patients who are “marked” as suspended who have an ARV pick-up registered in FILA after
   *     the date the patient was “marked” as suspended will be evaluated for IIT definition
   *     (CURR_FR5) or active status if above conditions are not met.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoStoppedOrSuspendedTreatment() {
    SqlCohortDefinition defintion = new SqlCohortDefinition();

    defintion.setName("Patients who Stopped/Suspended the treatment");
    defintion.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    defintion.addParameter(new Parameter("location", "location", Location.class));

    defintion.setQuery(
        TXCurrQueries.getPatientsWhoStoppedOrSuspendedTreatmentQuery(
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getMasterCardEncounterType(),
            hivMetadata.getARTProgram(),
            hivMetadata.getSuspendedTreatmentWorkflowState(),
            hivMetadata.getStateOfStayOfPreArtPatient(),
            hivMetadata.getSuspendedTreatmentConcept(),
            hivMetadata.getStateOfStayOfArtPatient(),
            hivMetadata.getARVPharmaciaEncounterType()));

    return defintion;
  }

  /**
   * <b>Patients who are dead</b>
   * <li>Patients enrolled on ART Program (Service TARV – Tratamento) with the following last state:
   *     “Died” or
   * <li>Patients whose most recently informed “Mudança no Estado de Permanência TARV” is Died on
   *     Ficha Resumo or Ficha Clinica – Mastercard or
   * <li>Patients who have been marked as dead as the reason for which the patient was not found at
   *     home during t he visit (Motivo de não encontrar o Paciente) on the last Home Visit Card by
   *     reporting end date. Use the “data da visita” when the patient was marked dead on the last
   *     Home Visit Card as the reference date or
   * <li>Patients who have been marked dead in the demographic section <b>Note:</b>
   *
   *     <p>Patients who are “marked” as dead who have an ARV pick-up registered in FILA or have a
   *     clinical consultation (Ficha Clínica) after the date the patient was “marked” as dead will
   *     be evaluated for IIT definition (CURR_FR5) or active status if above conditions are not
   *     met.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoAreDead() {
    SqlCohortDefinition defintion = new SqlCohortDefinition();

    defintion.setName("Patients who are dead");
    defintion.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    defintion.addParameter(new Parameter("location", "location", Location.class));

    defintion.setQuery(
        TXCurrQueries.getPatientsWhoAreDeadQuery(
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getARVPharmaciaEncounterType(),
            hivMetadata.getMasterCardEncounterType(),
            hivMetadata.getArtDeadWorkflowState(),
            hivMetadata.getBuscaActivaEncounterType(),
            hivMetadata.getReasonPatientNotFound(),
            hivMetadata.getReasonPatientNotFoundByActivist2ndVisitConcept(),
            hivMetadata.getReasonPatientNotFoundByActivist3rdVisitConcept(),
            hivMetadata.getPatientHasDiedConcept(),
            hivMetadata.getStateOfStayOfPreArtPatient(),
            hivMetadata.getARTProgram(),
            hivMetadata.getStateOfStayOfArtPatient()));

    return defintion;
  }

  /**
   * <b>Patients who are Transferred Out to another HF</b>
   * <li>Patients enrolled on ART Program (Service TARV- Tratamento) with the following last state:
   *     “Transferred Out” or
   * <li>Patients whose most recently informed “Mudança no Estado de Permanência TARV” is
   *     Transferred Out on Ficha Clinica ou Ficha Resumo – Master Card.
   * <li>Patients who have REASON PATIENT MISSED VISIT (MOTIVOS DA FALTA) as “Transferido para outra
   *     US” or “Auto-transferência” marked in the last Home Visit Card by reporting end date. Use
   *     the “data da visita” when the patient reason was marked on the Home Visit Card as the
   *     reference date
   * <li>The system will consider patient as transferred out as above defined only if the most
   *     recent date between (next scheduled ART pick-up on FILA + 1 day) and (the most recent ART
   *     pickup date on Ficha Recepção – Levantou ARVs + 31 days) falls by the end of the reporting
   *     period <b>Note:</b>
   *
   *     <p>Patients who are “marked” as transferred out who have an ARV pick-up registered in FILA
   *     after the date the patient was “marked” as transferred out will be evaluated for IIT
   *     definition (CURR_FR5).
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoAreTransferredOutToAnotherHf() {
    SqlCohortDefinition defintion = new SqlCohortDefinition();

    defintion.setName("Patients who are Transferred Out to another HF");
    defintion.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    defintion.addParameter(new Parameter("location", "location", Location.class));

    defintion.setQuery(
        TXCurrQueries.getPatientsWhoAreTransferredOutToAnotherHfQuery(
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getARVPharmaciaEncounterType(),
            hivMetadata.getMasterCardEncounterType(),
            hivMetadata.getTransferredOutToAnotherHealthFacilityWorkflowState(),
            hivMetadata.getBuscaActivaEncounterType(),
            hivMetadata.getStateOfStayOfPreArtPatient(),
            hivMetadata.getTransferredOutConcept(),
            hivMetadata.getARTProgram(),
            hivMetadata.getDefaultingMotiveConcept(),
            hivMetadata.getAutoTransferConcept(),
            hivMetadata.getStateOfStayOfArtPatient()));

    return defintion;
  }

  public CohortDefinition getPatientsWhoAreTransferredOutToAnotherHfIIT() {
    SqlCohortDefinition defintion = new SqlCohortDefinition();

    defintion.setName("Patients who are Transferred Out to another HF");
    defintion.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    defintion.addParameter(new Parameter("endDate", "endDate", Date.class));
    defintion.addParameter(new Parameter("location", "location", Location.class));

    defintion.setQuery(
        TXCurrQueries.getPatientsWhoAreTransferredOutToAnotherHfQueryIIT(
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getARVPharmaciaEncounterType(),
            hivMetadata.getMasterCardEncounterType(),
            hivMetadata.getTransferredOutToAnotherHealthFacilityWorkflowState(),
            hivMetadata.getBuscaActivaEncounterType(),
            hivMetadata.getStateOfStayOfPreArtPatient(),
            hivMetadata.getTransferredOutConcept(),
            hivMetadata.getARTProgram(),
            hivMetadata.getDefaultingMotiveConcept(),
            hivMetadata.getAutoTransferConcept(),
            hivMetadata.getStateOfStayOfArtPatient()));

    return defintion;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <b>12 –</b> Except all patients who after the most recent date from 2.1 to 2.6, have a drugs
   * pick up or consultation <b>(encounterType_id= 6, 9, 18)</b>
   *
   * <p>And encounter_datetime > the most recent date or "Drug pickup" <b>(encounterType_id =
   * 52)</b> and “Data de Levantamento” <b>(concept_id = 23866) value_datetime) > the most recent
   * date</b>.
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  @DocumentedDefinition(
      value = "patientWhoAfterMostRecentDateHaveDrusPickupOrConsultationComposition")
  public CohortDefinition
      getPatientWhoAfterMostRecentDateHaveDrugPickupOrConsultationComposition() {
    SqlCohortDefinition defintion = new SqlCohortDefinition();

    defintion.setName("patientWhoAfterMostRecentDateHaveDrusPickupOrConsultationComposition");

    defintion.setQuery(
        TXCurrQueries.getPatientWhoAfterMostRecentDateHaveDrusPickupOrConsultationComposition(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata
                .getTransferredOutToAnotherHealthFacilityWorkflowState()
                .getProgramWorkflowStateId(),
            hivMetadata.getSuspendedTreatmentWorkflowState().getProgramWorkflowStateId(),
            hivMetadata.getArtDeadWorkflowState().getProgramWorkflowStateId(),
            hivMetadata.getBuscaActivaEncounterType().getEncounterTypeId(),
            hivMetadata.getVisitaApoioReintegracaoParteAEncounterType().getEncounterTypeId(),
            hivMetadata.getVisitaApoioReintegracaoParteBEncounterType().getEncounterTypeId(),
            hivMetadata.getReasonPatientNotFound().getConceptId(),
            hivMetadata.getReasonPatientNotFoundByActivist2ndVisitConcept().getConceptId(),
            hivMetadata.getReasonPatientNotFoundByActivist3rdVisitConcept().getConceptId(),
            hivMetadata.getPatientHasDiedConcept().getConceptId(),
            hivMetadata.getStateOfStayOfPreArtPatient().getConceptId(),
            hivMetadata.getTransferredOutConcept().getConceptId(),
            hivMetadata.getSuspendedTreatmentConcept().getConceptId(),
            hivMetadata.getARTProgram().getProgramId(),
            hivMetadata.getDefaultingMotiveConcept().getConceptId(),
            hivMetadata.getAutoTransferConcept().getConceptId(),
            hivMetadata.getStateOfStayOfArtPatient().getConceptId()));

    defintion.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    defintion.addParameter(new Parameter("location", "location", Location.class));

    return defintion;
  }

  @DocumentedDefinition("<3, 3-5, >6 months of ARVs Dispensed")
  public CohortDefinition getPatientsWithMonthsRangeOfArvDispensationQuantity(String range) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("<3, 3-5, >6 months of ARVs Dispensed");
    cd.addParameter(new Parameter("onOrBefore", "On or before Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CalculationCohortDefinition cd1 =
        new CalculationCohortDefinition(
            "<3 months on ART",
            Context.getRegisteredComponents(LessThan3MonthsOfArvDispensationCalculation.class)
                .get(0));
    cd1.addParameter(new Parameter("onOrBefore", "On or before Date", Date.class));
    cd1.addParameter(new Parameter("location", "Location", Location.class));
    CalculationCohortDefinition cd2 =
        new CalculationCohortDefinition(
            "3-5 months of ARVs dispensed",
            Context.getRegisteredComponents(ThreeToFiveMonthsOnArtDispensationCalculation.class)
                .get(0));
    cd2.addParameter(new Parameter("onOrBefore", "On or before Date", Date.class));
    cd2.addParameter(new Parameter("location", "Location", Location.class));
    CalculationCohortDefinition cd3 =
        new CalculationCohortDefinition(
            "6 or more months of ARV dispensed",
            Context.getRegisteredComponents(SixMonthsAndAboveOnArvDispensationCalculation.class)
                .get(0));
    cd3.addParameter(new Parameter("onOrBefore", "On or before Date", Date.class));
    cd3.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch("1", EptsReportUtils.map(cd1, "onOrBefore=${onOrBefore},location=${location}"));
    cd.addSearch("2", EptsReportUtils.map(cd2, "onOrBefore=${onOrBefore},location=${location}"));
    cd.addSearch("3", EptsReportUtils.map(cd3, "onOrBefore=${onOrBefore},location=${location}"));
    if (range.equals("<3")) {
      cd.setCompositionString("1 AND NOT (2 OR 3)");
    } else if (range.equals("3-5")) {
      cd.setCompositionString("2 AND NOT 3");
    } else if (range.equals(">6")) {
      cd.setCompositionString("3");
    }
    return cd;
  }

  @DocumentedDefinition(
      "patients whose next ART pick-up is scheduled for >173 days after the date of their last ART drug pick-up")
  private CohortDefinition getPatientsWithNextPickupMoreThan173Days() {
    return getPatientsWithNextPickupBetweenDaysAfterLastPharmacyEncounter(173, null);
  }

  @DocumentedDefinition(
      "Patients whose next ART pick-up is scheduled for 83-173 days after the date of their last ART drug pick-up")
  private CohortDefinition getPatientsWithNextPickup83to173Days() {
    return getPatientsWithNextPickupBetweenDaysAfterLastPharmacyEncounter(83, 173);
  }

  @DocumentedDefinition(
      "Patients whose next ART pick-up is scheduled for <83 days after the date of their last ART drug pick-up")
  private CohortDefinition getPatientsWithNextPickupLessThan83days() {
    return getPatientsWithNextPickupBetweenDaysAfterLastPharmacyEncounter(null, 83);
  }

  /**
   * <b>Technical Specs</b>
   *
   * <p>Number of patients with less than 3 Monthly Type of Dispensation <b>(concept_id = 23739)</b>
   * on ART
   *
   * @return {@link SqlCohortDefinition}
   */
  @DocumentedDefinition("For <3 months of ARVs dispense to active patient’s on ART ")
  public CohortDefinition getPatientsWithLessThan3MonthlyTypeOfDispensation() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("For <3 months of ARVs dispense to active patient’s on ART ");
    String sqlQuery =
        "SELECT p.patient_id "
            + " 		FROM patient p "
            + "	    INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "	    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "	     "
            + "   			 INNER JOIN ( SELECT last_encounter.patient_id, MAX(encounter_date) encounter_date "
            + "		             FROM    (SELECT "
            + "		                     p.patient_id, MAX(e.encounter_datetime) AS encounter_date "
            + "		                 FROM patient p "
            + "		                 INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "		                 INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "		                 WHERE   e.encounter_type = ${18} AND p.voided = 0 "
            + "		                     	 AND o.concept_id = ${5096} "
            + "		                     	 AND o.value_datetime IS NOT NULL "
            + "		                         AND e.voided = 0 "
            + "		                         AND e.location_id = :location "
            + "		                         AND DATE(e.encounter_datetime) <= :onOrBefore "
            + "		                 GROUP BY p.patient_id "
            + "		                 UNION "
            + "		                 SELECT p.patient_id, MAX(e.encounter_datetime) encounter_date "
            + "		                 FROM patient p "
            + "		                 INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "		                 INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "		                 WHERE "
            + "		                     e.encounter_type = ${6} "
            + "		                         AND o.concept_id = ${23739} "
            + "		                         AND p.voided = 0 "
            + "		                         AND o.voided = 0 "
            + "		                         AND e.voided = 0 "
            + "		                         AND e.location_id = :location "
            + "		                         AND DATE(e.encounter_datetime) <= :onOrBefore "
            + "		                 GROUP BY p.patient_id) AS last_encounter GROUP BY last_encounter.patient_id "
            + "		           ) recent_dispensation ON recent_dispensation.patient_id = p.patient_id "
            + "                   WHERE  DATE(e.encounter_datetime) <= :onOrBefore "
            + "						AND e.voided = 0 "
            + "                        AND p.voided = 0 "
            + "                        AND o.voided = 0 "
            + "                         "
            + "						AND (              "
            + "						   (e.encounter_type = ${18} "
            + "								AND o.concept_id = ${5096} "
            + "								AND o.value_datetime IS NOT NULL "
            + "								AND TIMESTAMPDIFF(DAY, DATE(e.encounter_datetime), o.value_datetime) < 83 "
            + "								AND e.encounter_datetime = recent_dispensation.encounter_date) "
            + "								OR ( "
            + "									e.encounter_type = ${6} "
            + "									AND o.concept_id = ${23739} "
            + "									AND o.value_coded = ${1098} "
            + "									AND e.encounter_datetime = recent_dispensation.encounter_date "
            + "									AND NOT  EXISTS (		SELECT en.encounter_id "
            + "																FROM encounter en "
            + "                                                                INNER JOIN obs oo ON oo.encounter_id = en.encounter_id "
            + "																WHERE en.encounter_type = ${18} "
            + "																	AND DATE (en.encounter_datetime) = DATE(recent_dispensation.encounter_date) "
            + "																	AND DATE(en.encounter_datetime) <= :onOrBefore "
            + "																	AND en.patient_id = recent_dispensation.patient_id "
            + "                                                                 AND oo.concept_id = ${5096} "
            + "                                                                 AND oo.value_datetime IS NOT NULL "
            + "                                                                 AND oo.voided = 0 "
            + "																	AND e.voided = 0 "
            + "																GROUP BY en.encounter_id "
            + "															) "
            + "								) "
            + "					) "
            + " GROUP BY p.patient_id";

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());
    valuesMap.put("23739", hivMetadata.getTypeOfDispensationConcept().getConceptId());
    valuesMap.put("23730", hivMetadata.getQuarterlyDispensation().getConceptId());
    valuesMap.put("23888", hivMetadata.getSemiannualDispensation().getConceptId());
    valuesMap.put("1098", hivMetadata.getMonthlyConcept().getConceptId());
    valuesMap.put("165322", hivMetadata.getMdcState().getConceptId());
    valuesMap.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());

    StringSubstitutor sub = new StringSubstitutor(valuesMap);
    cd.setQuery(sub.replace(sqlQuery));
    cd.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    return cd;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * Identify the most recent source between Ficha Clinica and FILA according to the following
   * criteria: Most recent Ficha Clínica with Tipo de Dispensa informed by reporting end date; The
   * next scheduled pick-up date (>= report start date) informed on the most recent FILA by
   * reporting end date;
   *
   * <p>For 3-5 months ARV Dispensing disaggregation: oIf FILA is prioritized: next pick up
   * registered is scheduled for 83 - 173 days after their last ART pick-up date (next scheduled
   * pick up date – last pick up date >=83 days and <=173 days). oIf Ficha Clinica is prioritized
   * (most recent Ficha Clinica with Tipo de Dispensa Informed): Tipo de Dispensa = DT
   *
   * <p>Note: In the query below, we use two maximums in the same select because we want the most
   * recent scheduling date registered in the most recent pick up on file.
   *
   * </blockquote>
   *
   * @return {@link SqlCohortDefinition}
   */
  @DocumentedDefinition(
      "Patients marked as DT on Ficha Clinica Mastercard on last Tipo de Levantamento")
  public SqlCohortDefinition getPatientsWithQuarterlyTypeOfDispensation() {
    SqlCohortDefinition patientsWithQuarterlyTypeOfDispensation = new SqlCohortDefinition();
    patientsWithQuarterlyTypeOfDispensation.setName(
        "Patients marked as (DT) Quartely Dispensation");

    String sqlQuery =
        "SELECT p.patient_id "
            + "FROM patient p "
            + "    INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "    INNER JOIN obs oo ON oo.encounter_id = e.encounter_id "
            + "    INNER JOIN ( "
            + "        SELECT "
            + "            last_encounter.patient_id, MAX(last_encounter.recent_date) encounter_date,  MAX(last_encounter.dispensation_date) dispensation_date "
            + "        FROM (  "
            + "                SELECT e.patient_id, MAX(last_pickup.recent_date) recent_date, MAX(o.value_datetime) AS dispensation_date "
            + "                FROM encounter e "
            + "                    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                  INNER JOIN ( "
            + "                             SELECT e.patient_id, MAX(e.encounter_datetime) AS recent_date "
            + "                                 FROM encounter e "
            + "                                      INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                                 WHERE "
            + "                                     e.encounter_type = ${18} "
            + "                                     AND o.concept_id = ${5096} "
            + "                                     AND o.value_datetime IS NOT NULL "
            + "                                     AND e.voided = 0 "
            + "                                     AND o.voided =0 "
            + "                                     AND e.location_id = :location "
            + "                                     AND DATE(e.encounter_datetime) <= :onOrBefore "
            + "                                     GROUP BY e.patient_id "
            + "                                ) last_pickup ON e.patient_id = last_pickup.patient_id"
            + "                WHERE "
            + "                e.encounter_type = ${18} "
            + "                AND o.concept_id = ${5096} "
            + "                AND o.value_datetime >= :onOrAfter "
            + "                AND e.voided = 0 "
            + "                AND o.voided =0 "
            + "                AND e.location_id = :location "
            + "                AND DATE(e.encounter_datetime) = last_pickup.recent_date "
            + "                GROUP BY e.patient_id "
            + "                UNION "
            + "                SELECT e.patient_id, '' recent_date, MAX(e.encounter_datetime) dispensation_date "
            + "                FROM encounter e "
            + "                    INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                WHERE "
            + "                    e.encounter_type = ${6} "
            + "                    AND o.concept_id = ${23739} "
            + "                    AND o.voided = 0 "
            + "                    AND e.voided = 0 "
            + "                    AND e.location_id = :location "
            + "                    AND DATE(e.encounter_datetime) <= :onOrBefore "
            + "                GROUP BY e.patient_id "
            + "            ) AS last_encounter "
            + "        GROUP BY "
            + "            last_encounter.patient_id "
            + "    ) recent_dispensation ON recent_dispensation.patient_id = p.patient_id "
            + "WHERE "
            + "    DATE(e.encounter_datetime) <= :onOrBefore "
            + "    AND e.voided = 0 "
            + "    AND e.location_id = :location "
            + "    AND p.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND oo.voided = 0 "
            + "    AND ( ( "
            + "            e.encounter_type = ${18} "
            + "            AND e.encounter_datetime <= :onOrBefore "
            + "            AND e.encounter_datetime = recent_dispensation.encounter_date "
            + "            AND o.concept_id = ${5096} "
            + "            AND o.value_datetime = recent_dispensation.dispensation_date "
            + "            AND TIMESTAMPDIFF( DAY,DATE(e.encounter_datetime), o.value_datetime ) >= 83 "
            + "            AND TIMESTAMPDIFF( DAY, DATE(e.encounter_datetime), o.value_datetime) <= 173 "
            + "        ) "
            + "        OR ( "
            + "            e.encounter_type = ${6} "
            + "            AND o.concept_id = ${23739} "
            + "            AND o.value_coded = ${23720} "
            + "            AND e.encounter_datetime = recent_dispensation.dispensation_date "
            + "            AND NOT EXISTS ( SELECT en.encounter_id "
            + "                FROM encounter en "
            + "                    INNER JOIN obs oo ON oo.encounter_id = en.encounter_id "
            + "                WHERE "
            + "                    en.encounter_type = 18 "
            + "                    AND DATE (en.encounter_datetime) = recent_dispensation.encounter_date "
            + "                    AND en.patient_id = recent_dispensation.patient_id "
            + "                    AND oo.concept_id = ${5096} "
            + "                    AND oo.value_datetime = recent_dispensation.dispensation_date "
            + "                    AND oo.voided = 0 "
            + "                    AND e.voided = 0 "
            + "                GROUP BY "
            + "                    en.encounter_id "
            + "            ) "
            + "        ) "
            + "    ) "
            + "GROUP BY p.patient_id";

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());
    valuesMap.put("23739", hivMetadata.getTypeOfDispensationConcept().getConceptId());
    valuesMap.put("23730", hivMetadata.getQuarterlyDispensation().getConceptId());
    valuesMap.put("23888", hivMetadata.getSemiannualDispensation().getConceptId());
    valuesMap.put("23720", hivMetadata.getQuarterlyConcept().getConceptId());
    valuesMap.put("1256", hivMetadata.getStartDrugsConcept().getConceptId());
    valuesMap.put("1257", hivMetadata.getContinueRegimenConcept().getConceptId());

    StringSubstitutor sub = new StringSubstitutor(valuesMap);
    patientsWithQuarterlyTypeOfDispensation.setQuery(sub.replace(sqlQuery));

    patientsWithQuarterlyTypeOfDispensation.addParameter(
        new Parameter("onOrBefore", "Before Date", Date.class));
    patientsWithQuarterlyTypeOfDispensation.addParameter(
        new Parameter("onOrAfter", "After Date", Date.class));
    patientsWithQuarterlyTypeOfDispensation.addParameter(
        new Parameter("location", "Location", Location.class));
    return patientsWithQuarterlyTypeOfDispensation;
  }

  /**
   * All patients who do not have the next scheduled drug pick up date on their last drug pick-up
   * (FILA) nor any ART pickup date registered on Ficha Recepção – Levantou ARVs or FILA
   *
   * @return CohortDefinition
   */
  public CohortDefinition getNumberOfPatientsWhoAbandonedArtWithoutPickup() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Number of patients who Abandoned the ART");
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addParameter(new Parameter("endDate", "specifiedDate", Date.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());

    String sql =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       LEFT JOIN(SELECT e.patient_id "
            + "                 FROM   encounter e "
            + "                 WHERE  e.encounter_type = ${18} "
            + "                        AND e.encounter_datetime <= :endDate "
            + "                        AND e.voided = 0 "
            + "                        AND e.location_id = :location "
            + "                 GROUP  BY e.patient_id) fila ON fila.patient_id = p.patient_id "
            + "       LEFT JOIN("
            + "                   SELECT e.patient_id "
            + "                   FROM encounter e "
            + "                   INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                   INNER JOIN ("
            + "                   SELECT e.patient_id, MAX(e.encounter_datetime) encounter_date "
            + "                 FROM   encounter e "
            + "                 WHERE  e.encounter_type = ${18} "
            + "                        AND e.encounter_datetime <= :endDate "
            + "                        AND e.voided = 0 "
            + "                        AND e.location_id = :location "
            + "                 GROUP  BY e.patient_id ) recent_fila ON recent_fila.patient_id = e.patient_id "
            + "                   WHERE e.encounter_type = ${18} "
            + "                   AND e.encounter_datetime = recent_fila.encounter_date"
            + "                   AND o.concept_id = ${5096} "
            + "                   AND o.value_datetime IS NOT NULL"
            + ") next ON next.patient_id = p.patient_id "
            + "       LEFT JOIN(SELECT e.patient_id "
            + "                 FROM   encounter e "
            + "                        INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                 WHERE  e.encounter_type = ${52} "
            + "                        AND o.concept_id = ${23866} "
            + "                        AND o.value_datetime <= :endDate "
            + "                AND o.voided = 0                  "
            + "                        AND e.voided = 0 "
            + "                        AND e.location_id = :location "
            + "                 GROUP  BY e.patient_id) arv_pickup ON arv_pickup.patient_id = p.patient_id "
            + "WHERE arv_pickup.patient_id IS NULL  "
            + "     AND ( fila.patient_id IS NULL OR  next.patient_id IS NULL ) "
            + "        AND p.voided = 0                  "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    cd.setQuery(sb.replace(sql));
    return cd;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * Identify the most recent source between Ficha Clinica and FILA according to the following
   * criteria: Most recent Ficha Clínica with Tipo de Dispensa informed by reporting end date; oThe
   * next scheduled pick-up date (>= report start date) informed on the most recent FILA by
   * reporting end date;
   *
   * <p>For >6 months ARV Dispensing disaggregation: oIf FILA is prioritized: next pick up
   * registered is scheduled >173 days after their last ART pick-up date (next scheduled pick up
   * date – last pick up date >173 days). oIf Ficha Clinica is prioritized (most recent Ficha
   * Clínica with Tipo de Dispensa Informed): Tipo de Dispensa = DS
   *
   * <p>Note: In the query below, we use two maximums in the same select because we want the most
   * recent scheduling date registered in the most recent pick up on file.
   *
   * </blockquote>
   *
   * @return {@link SqlCohortDefinition}
   */
  @DocumentedDefinition(
      "Patients marked as DS on Ficha Clinica Mastercard on last Tipo de Levantamento")
  public SqlCohortDefinition getPatientsWithSemiAnnualTypeOfDispensation() {
    SqlCohortDefinition patientsWithSemiAnnualTypeOfDispensation = new SqlCohortDefinition();
    patientsWithSemiAnnualTypeOfDispensation.setName(
        "Patients marked as DS on Ficha Clinica Mastercard on last Tipo de Levantamento");
    String sqlQuery =
        "SELECT p.patient_id "
            + "FROM patient p "
            + "    INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "    INNER JOIN obs oo ON oo.encounter_id = e.encounter_id "
            + "    INNER JOIN ( "
            + "        SELECT last_encounter.patient_id,MAX(last_encounter.encounter_date) encounter_date, MAX(last_encounter.dispensation_date) dispensation_date "
            + "        FROM ( "
            + "                SELECT e.patient_id, MAX(last_pickup.recent_date) encounter_date, MAX(o.value_datetime) AS dispensation_date "
            + "                FROM encounter e "
            + "                    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                  INNER JOIN ( "
            + "                             SELECT e.patient_id, MAX(e.encounter_datetime) AS recent_date "
            + "                                 FROM encounter e "
            + "                                      INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                                 WHERE "
            + "                                     e.encounter_type = ${18} "
            + "                                     AND o.concept_id = ${5096} "
            + "                                     AND o.value_datetime IS NOT NULL "
            + "                                     AND e.voided = 0 "
            + "                                     AND o.voided =0 "
            + "                                     AND e.location_id = :location "
            + "                                     AND DATE(e.encounter_datetime) <= :onOrBefore "
            + "                                     GROUP BY e.patient_id "
            + "                                ) last_pickup ON e.patient_id = last_pickup.patient_id"
            + "                WHERE "
            + "                e.encounter_type = ${18} "
            + "                AND o.concept_id = ${5096} "
            + "                AND o.value_datetime >= :onOrAfter "
            + "                AND e.voided = 0 "
            + "                AND o.voided = 0 "
            + "                AND e.location_id = :location "
            + "                AND DATE(e.encounter_datetime) = last_pickup.recent_date "
            + "                GROUP BY e.patient_id "
            + "                UNION "
            + "                SELECT p.patient_id, '' recent_date, MAX(e.encounter_datetime) dispensation_date "
            + "                FROM patient p "
            + "                    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                    INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                WHERE "
            + "                    e.encounter_type = ${6} "
            + "                    AND o.concept_id = ${23739} "
            + "                    AND p.voided = 0 "
            + "                    AND o.voided = 0 "
            + "                    AND e.voided = 0 "
            + "                    AND e.location_id = :location "
            + "                    AND DATE(e.encounter_datetime) <= :onOrBefore "
            + "                GROUP BY "
            + "                    p.patient_id "
            + "            ) AS last_encounter "
            + "        GROUP BY last_encounter.patient_id "
            + "    ) recent_dispensation ON recent_dispensation.patient_id = p.patient_id "
            + "WHERE "
            + "    DATE(e.encounter_datetime) <= :onOrBefore "
            + "    AND e.voided = 0 "
            + "    AND e.location_id = :location "
            + "    AND p.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND oo.voided = 0 "
            + "    AND ( ( "
            + "            e.encounter_type = ${18} "
            + "            AND e.encounter_datetime <= :onOrBefore "
            + "            AND e.encounter_datetime = recent_dispensation.encounter_date "
            + "            AND o.concept_id = ${5096} "
            + "            AND o.value_datetime = recent_dispensation.dispensation_date "
            + "            AND TIMESTAMPDIFF( DAY, DATE(e.encounter_datetime), o.value_datetime ) > 173 "
            + "        ) "
            + "        OR ( "
            + "            e.encounter_type = ${6} "
            + "            AND o.concept_id = ${23739} "
            + "            AND o.value_coded = ${23888} "
            + "            AND e.encounter_datetime = recent_dispensation.dispensation_date "
            + "            AND NOT EXISTS ( "
            + "                SELECT en.encounter_id "
            + "                FROM encounter en "
            + "                    INNER JOIN obs oo ON oo.encounter_id = en.encounter_id "
            + "                WHERE "
            + "                    en.encounter_type = ${18} "
            + "                    AND DATE (en.encounter_datetime) = recent_dispensation.encounter_date "
            + "                    AND en.patient_id = recent_dispensation.patient_id "
            + "                    AND oo.concept_id = ${5096} "
            + "                    AND oo.value_datetime = recent_dispensation.dispensation_date "
            + "                    AND oo.voided = 0 "
            + "                    AND e.voided = 0 "
            + "                GROUP BY en.encounter_id "
            + "            ) "
            + "        ) "
            + "    ) "
            + "GROUP BY p.patient_id";

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());
    valuesMap.put("23739", hivMetadata.getTypeOfDispensationConcept().getConceptId());
    valuesMap.put("23730", hivMetadata.getQuarterlyDispensation().getConceptId());
    valuesMap.put("23888", hivMetadata.getSemiannualDispensation().getConceptId());
    valuesMap.put("165314", hivMetadata.getAnnualArvDispensationConcept().getConceptId());

    StringSubstitutor sub = new StringSubstitutor(valuesMap);

    patientsWithSemiAnnualTypeOfDispensation.setQuery(sub.replace(sqlQuery));

    patientsWithSemiAnnualTypeOfDispensation.addParameter(
        new Parameter("onOrBefore", "Before Date", Date.class));
    patientsWithSemiAnnualTypeOfDispensation.addParameter(
        new Parameter("onOrAfter", "After Date", Date.class));
    patientsWithSemiAnnualTypeOfDispensation.addParameter(
        new Parameter("location", "Location", Location.class));
    System.out.println(patientsWithSemiAnnualTypeOfDispensation.getQuery());
    return patientsWithSemiAnnualTypeOfDispensation;
  }

  /**
   * <b>Description:</b> Dispensation Compositions, Monthly, Quartely and Semi-annual Dispensations
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition lessThan3MonthsDispensationComposition() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Get patients with Monthly ARV Dispensation");
    cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
    cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition quarterlyDispensation = getPatientsWithQuarterlyTypeOfDispensation();
    CohortDefinition semiAnnualDispensation = getPatientsWithSemiAnnualTypeOfDispensation();
    CohortDefinition txcurr = getTxCurrCompositionCohort("compositionCohort", true);

    cd.addSearch("quarterly", Mapped.mapStraightThrough(quarterlyDispensation));
    cd.addSearch("semiAnnual", Mapped.mapStraightThrough(semiAnnualDispensation));
    cd.addSearch("txcurr", Mapped.mapStraightThrough(txcurr));

    cd.setCompositionString("txcurr AND NOT (quarterly OR semiAnnual)");
    return cd;
  }

  /**
   * <b>Description:</b> Number of patients with Quartely Dispensation (excluding Semi-annual)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition quarterlyDispensationComposition() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Get patients with Quarterly ARV Dispensation");
    cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
    cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition quarterlyDispensation = getPatientsWithQuarterlyTypeOfDispensation();
    CohortDefinition semiAnnualDispensation = getPatientsWithSemiAnnualTypeOfDispensation();

    cd.addSearch("quarterly", Mapped.mapStraightThrough(quarterlyDispensation));
    cd.addSearch("semiAnnual", Mapped.mapStraightThrough(semiAnnualDispensation));

    cd.setCompositionString("quarterly  AND NOT semiAnnual");
    return cd;
  }

  /**
   * <b>Description:</b> Number of patients with Semi-annual Dispensation
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition semiAnnualDispensationComposition() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Get patients with Semi Annual ARV Dispensation");
    cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
    cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition semiAnnualDispensation = getPatientsWithSemiAnnualTypeOfDispensation();

    cd.addSearch("semiAnnual", Mapped.mapStraightThrough(semiAnnualDispensation));

    cd.setCompositionString("semiAnnual");
    return cd;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * All Patients with next pickup <b>(concept_id = 5096 RETURN VISIT DATE FOR ARV DRUG)</b> between
   * days after last drug pickup <b>(encounterType_id = 18)</b>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  private CohortDefinition getPatientsWithNextPickupBetweenDaysAfterLastPharmacyEncounter(
      Integer minDays, Integer maxDays) {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    StringBuilder sb =
        new StringBuilder()
            .append("select p.patient_id ")
            .append("from patient p ")
            .append(
                "         join (select e.patient_id, e.encounter_datetime, max(o.value_datetime) value_datetime ")
            .append("               from encounter e ")
            .append(
                "                        join (select patient_id, max(encounter_datetime) encounter_datetime ")
            .append("                              from encounter e ")
            .append("                              where e.voided = 0 ")
            .append("                                and e.encounter_type = %d ")
            .append("                                and e.encounter_datetime <= :onOrBefore ")
            .append("                                and e.location_id = :location ")
            .append("                              group by patient_id) last ")
            .append("                             on e.patient_id = last.patient_id ")
            .append(
                "                                 and e.encounter_datetime = last.encounter_datetime ")
            .append("                        join obs o on e.encounter_id = o.encounter_id ")
            .append("               where e.voided = 0 ")
            .append("                 and o.voided = 0 ")
            .append("                 and o.concept_id = %d ")
            .append("                 and e.location_id = :location ")
            .append("                 and e.encounter_type = %d ")
            .append("               group by e.patient_id, e.encounter_datetime) last_obs ")
            .append("              on p.patient_id = last_obs.patient_id ");

    if (minDays != null && maxDays != null) {
      sb.append(
              "  where timestampdiff(DAY, last_obs.encounter_datetime, last_obs.value_datetime) BETWEEN ")
          .append(minDays)
          .append(" AND ")
          .append(maxDays);
    } else if (minDays == null) {
      sb.append(
              "  where timestampdiff(DAY, last_obs.encounter_datetime, last_obs.value_datetime) < ")
          .append(maxDays);
    } else {
      sb.append(
              "  where timestampdiff(DAY, last_obs.encounter_datetime, last_obs.value_datetime) > ")
          .append(minDays);
    }
    sb.append(" and p.voided = 0");

    cd.setQuery(
        String.format(
            sb.toString(),
            hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId(),
            hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId(),
            hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId()));

    return cd;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <b>15 - 2.7 = 15 since version 4</b>
   *
   * <p>All Patients transferred-outs <b>(concept_id = 1706)</b> registered in Last Home Visit Card
   * <b>(encounterType_id = 21)</b>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  @DocumentedDefinition(value = "Patients Transfered Out In Last Home Visit Card")
  public CohortDefinition getPatientsTransferedOutInLastHomeVisitCard() {
    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("Patients Transfered Out In Last Home Visit Card");

    definition.setQuery(
        TXCurrQueries.getPatientsTransferedOutInLastHomeVisitCard(
            hivMetadata.getBuscaActivaEncounterType().getEncounterTypeId(),
            hivMetadata.getDefaultingMotiveConcept().getConceptId(),
            hivMetadata.getTransferredOutConcept().getConceptId(),
            hivMetadata.getAutoTransferConcept().getConceptId()));

    definition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));

    return definition;
  }

  /**
   * The system will consider patient as transferred out as above defined only if the most recent
   * date between (next scheduled ART pick-up on FILA + 1 day) and (the most recent ART pickup date
   * on Ficha Recepção – Levantou ARVs + 31 days) falls during the reporting period.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getTransferredOutBetweenNextPickupDateFilaAndRecepcaoLevantou() {

    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName(
        "Patients Transfered Out between (next scheduled ART pick-up on FILA + 1 day) "
            + "and (the most recent ART pickup date on Ficha Recepção – Levantou ARVs + 31 days");

    definition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    valuesMap.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    valuesMap.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());
    valuesMap.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());

    String query =
        "SELECT final.patient_id FROM  ( "
            + "SELECT considered_transferred.patient_id, max(considered_transferred.value_datetime) as max_date "
            + "FROM ( "
            + "               SELECT     p.patient_id, "
            + "                          date_add(max(o.value_datetime), interval 1 day) AS value_datetime "
            + "               FROM       patient p "
            + "                              INNER JOIN encounter e "
            + "                                         ON         e.patient_id=p.patient_id "
            + "                              INNER JOIN obs o "
            + "                                         ON         o.encounter_id=e.encounter_id "
            + "               WHERE      p.voided = 0 "
            + "                 AND        e.voided = 0 "
            + "                 AND        o.voided = 0 "
            + "                 AND        e.encounter_type = ${18} "
            + "                 AND        o.concept_id = ${5096} "
            + "                 AND        e.encounter_datetime <= :onOrBefore "
            + "                 AND        e.location_id = :location "
            + "               GROUP BY   p.patient_id "
            + " UNION "
            + "               SELECT     p.patient_id, "
            + "                          date_add(max(o.value_datetime), interval 31 day)  AS value_datetime "
            + "               FROM       patient p "
            + "                              INNER JOIN encounter e "
            + "                                         ON         e.patient_id=p.patient_id "
            + "                              INNER JOIN obs o "
            + "                                         ON         o.encounter_id=e.encounter_id "
            + "               WHERE      p.voided = 0 "
            + "                 AND        e.voided = 0 "
            + "                 AND        o.voided = 0 "
            + "                 AND        e.encounter_type = ${52} "
            + "                 AND        o.concept_id = ${23866} "
            + "                 AND        o.value_datetime <= :onOrBefore "
            + "                 AND        e.location_id = :location "
            + "               GROUP BY   p.patient_id "
            + " )  considered_transferred "
            + " GROUP BY considered_transferred.patient_id "
            + " ) final "
            + " WHERE final.max_date <= :onOrBefore  ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    definition.setQuery(stringSubstitutor.replace(query));

    return definition;
  }

  /**
   * <b>Description:</b>
   *
   * <p><b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <b>6 –</b> All deaths <b>(Patient_State.state = 10)</b>, Transferred-out
   * <b>(Patient_State.state = 7)</b> and Suspensions <b>(Patient_State.state = 8)</b>
   *
   * <p>Registered in Patient Program State by reporting end date <b>Patient_State.start_date <=
   * endDate Patient_state.end_date</b> is null
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  @DocumentedDefinition(
      value = "patientsDeadTransferredOutSuspensionsInProgramStateByReportingEndDate")
  public CohortDefinition getTransferredOutOnProgramEnrollment() {

    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("patientsDeadTransferredOutSuspensionsInProgramStateByReportingEndDate");

    definition.setQuery(
        TXCurrQueries.getPatientsTransferredOutOnProgramEnrollment(
            hivMetadata.getARTProgram().getProgramId(),
            Arrays.asList(
                hivMetadata
                    .getTransferredOutToAnotherHealthFacilityWorkflowState()
                    .getProgramWorkflowStateId())));

    definition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));

    return definition;
  }

  /**
   * <b>Description:</b>
   *
   * <p><b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <b>6 –</b> All deaths <b>(Patient_State.state = 10)</b>, Transferred-out
   * <b>(Patient_State.state = 7)</b> and Suspensions <b>(Patient_State.state = 8)</b>
   *
   * <p>Registered in Patient Program State by reporting end date <b>Patient_State.start_date <=
   * endDate Patient_state.end_date</b> is null
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsSuspendedOrDeadOnProgramEnrollment() {

    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("patientsDeadSuspensionsInProgramStateByReportingEndDate");

    definition.setQuery(
        TXCurrQueries.getPatientsTransferredOutOnProgramEnrollment(
            hivMetadata.getARTProgram().getProgramId(),
            Arrays.asList(
                hivMetadata.getSuspendedTreatmentWorkflowState().getProgramWorkflowStateId(),
                hivMetadata.getArtDeadWorkflowState().getProgramWorkflowStateId())));

    definition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));

    return definition;
  }
}
