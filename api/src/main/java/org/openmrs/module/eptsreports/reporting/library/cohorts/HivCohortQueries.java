/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.eptsreports.reporting.library.cohorts;

import static org.openmrs.module.eptsreports.reporting.calculation.generic.TargetGroupCalculation.TIPO;
import static org.openmrs.module.eptsreports.reporting.calculation.generic.TargetGroupCalculation.TargetGroup.ADOLESCENT_AND_YOUTH;
import static org.openmrs.module.eptsreports.reporting.calculation.generic.TargetGroupCalculation.TargetGroup.BREASTFEEDING;
import static org.openmrs.module.eptsreports.reporting.calculation.generic.TargetGroupCalculation.TargetGroup.MILITARY;
import static org.openmrs.module.eptsreports.reporting.calculation.generic.TargetGroupCalculation.TargetGroup.MINER;
import static org.openmrs.module.eptsreports.reporting.calculation.generic.TargetGroupCalculation.TargetGroup.PREGNANT;
import static org.openmrs.module.eptsreports.reporting.calculation.generic.TargetGroupCalculation.TargetGroup.SERODISCORDANT;
import static org.openmrs.module.eptsreports.reporting.calculation.generic.TargetGroupCalculation.TargetGroup.TRUCK_DRIVER;

import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.calculation.generic.TargetGroupCalculation;
import org.openmrs.module.eptsreports.reporting.cohort.definition.CalculationCohortDefinition;
import org.openmrs.module.eptsreports.reporting.library.queries.ResumoMensalQueries;
import org.openmrs.module.eptsreports.reporting.library.queries.ViralLoadQueries;
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

/** Defines all of the HivCohortDefinition instances we want to expose for EPTS */
@Component
public class HivCohortQueries {

  @Autowired private HivMetadata hivMetadata;

  @Autowired private GenericCohortQueries genericCohortQueires;

  @Autowired private GenderCohortQueries genderCohortQueries;
  @Autowired private PrepCtCohortQueries prepCtCohortQueries;

  /**
   * Adult and pediatric patients on ART with suppressed viral load results (<1,000 copies/ml)
   * documented in the medical records and /or supporting laboratory results within the past 12
   * months
   *
   * @return CohortDefinition
   */
  @DocumentedDefinition(value = "suppressedViralLoadWithin12Months")
  public CohortDefinition getPatientsWithSuppressedViralLoadWithin12Months() {
    SqlCohortDefinition sql = new SqlCohortDefinition();
    sql.setName("suppressedViralLoadWithin12Months");
    sql.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sql.addParameter(new Parameter("endDate", "End Date", Date.class));
    sql.addParameter(new Parameter("location", "Location", Location.class));
    sql.setQuery(ViralLoadQueries.getPatientsWithViralLoadSuppression());
    return sql;
  }

  /**
   * <b>Description:</b> Number of adult and pediatric ART patients with a viral load result
   * documented in the patient medical record and/ or laboratory records in the past 12 months.
   *
   * @return {@link CohortDefinition}
   */
  @DocumentedDefinition(value = "viralLoadWithin12Months")
  public CohortDefinition getPatientsViralLoadWithin12Months() {
    SqlCohortDefinition sql = new SqlCohortDefinition();
    sql.setName("viralLoadWithin12Months");
    sql.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sql.addParameter(new Parameter("endDate", "End Date", Date.class));
    sql.addParameter(new Parameter("location", "Location", Location.class));

    List<EncounterType> encounters =
        Arrays.asList(
            hivMetadata.getMisauLaboratorioEncounterType(),
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getPediatriaSeguimentoEncounterType(),
            hivMetadata.getMasterCardEncounterType(),
            hivMetadata.getFsrEncounterType());
    String vlQuery =
        new EptsQueriesUtil()
            .patientIdQueryBuilder(
                ViralLoadQueries.getPatientsHavingViralLoadInLast12Months(encounters))
            .getQuery();
    sql.setQuery(vlQuery);
    return sql;
  }

  /**
   * <b>Description:</b> Number of adult and pediatric ART patients with a viral load result
   * documented in the patient medical record and/ or laboratory records in the past 12 months.
   *
   * @return {@link CohortDefinition}
   */
  @DocumentedDefinition(value = "viralLoadWithin12Months")
  public CohortDefinition getPatientsViralLoadWithin12MonthsBySource() {
    SqlCohortDefinition sql = new SqlCohortDefinition();
    sql.setName("viralLoadWithin12Months");
    sql.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sql.addParameter(new Parameter("endDate", "End Date", Date.class));
    sql.addParameter(new Parameter("location", "Location", Location.class));
    sql.setQuery(ViralLoadQueries.getPatientsHavingViralLoadInLast12MonthsBySource());
    return sql;
  }

  /**
   * Adult and pediatric patients on ART who have re-initiated the treatment.
   *
   * @return CohortDefinition
   */
  @DocumentedDefinition(value = "restartedTreatment")
  public CohortDefinition getPatientsWhoRestartedTreatment() {
    return genericCohortQueires.hasCodedObs(
        hivMetadata.getARVPlanConcept(),
        Collections.singletonList(hivMetadata.getRestartConcept()));
  }

  public CohortDefinition getPatientsInArtCareTransferredOutToAnotherHealthFacility() {
    Program hivCareProgram = hivMetadata.getHIVCareProgram();
    ProgramWorkflowState state =
        hivMetadata.getArtCareTransferredOutToAnotherHealthFacilityWorkflowState();
    return genericCohortQueires.getPatientsBasedOnPatientStatesBeforeDate(
        hivCareProgram.getProgramId(), state.getProgramWorkflowStateId());
  }

  /**
   * <b>Description: 2.</b> Number of Patients with historical drug start Date Obs
   *
   * <p><b>Technical Specs</b>
   *
   * <blockquote>
   *
   * Cohort of patients with START DATE <b>(concept_id = 1190)</b> filled in drug pickup
   * <b>(encounterType_id 18)</b> or follow up consultation for adults and children
   * <b>(encounterType_ids = 6 and 5)</b> and obs startDate <= endDate
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  @DocumentedDefinition(value = "patientWithHistoricalDrugStartDateObs")
  public CohortDefinition getPatientWithHistoricalDrugStartDateObsBeforeOrOnEndDate() {
    SqlCohortDefinition patientWithHistoricalDrugStartDateObs = new SqlCohortDefinition();
    patientWithHistoricalDrugStartDateObs.setName("patientWithHistoricalDrugStartDateObs");

    Map<String, Integer> map = new HashMap<>();

    map.put(
        "aRVPharmaciaEncounterType",
        hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put(
        "adultoSeguimentoEncounterType",
        hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put(
        "aRVPediatriaSeguimentoEncounterType",
        hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put(
        "masterCardEncounterType", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put(
        "historicalDrugStartDateConcept",
        hivMetadata.getHistoricalDrugStartDateConcept().getConceptId());
    String query =
        "SELECT p.patient_id  "
            + "FROM patient p  "
            + "    INNER JOIN encounter e  "
            + "        ON p.patient_id=e.patient_id  "
            + "    INNER JOIN obs o  "
            + "        ON e.encounter_id=o.encounter_id  "
            + "WHERE p.voided=0 and e.voided= 0  "
            + "    AND o.voided=0  "
            + "    AND e.encounter_type IN (${aRVPharmaciaEncounterType}, ${adultoSeguimentoEncounterType}, ${aRVPediatriaSeguimentoEncounterType},${masterCardEncounterType})  "
            + "    AND o.concept_id= ${historicalDrugStartDateConcept}  "
            + "    AND o.value_datetime IS NOT NULL  "
            + "    AND o.value_datetime <= :onOrBefore  "
            + "    AND e.location_id= :location "
            + "GROUP BY p.patient_id";
    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    patientWithHistoricalDrugStartDateObs.setQuery(stringSubstitutor.replace(query));

    patientWithHistoricalDrugStartDateObs.addParameter(
        new Parameter("onOrBefore", "onOrBefore", Date.class));
    patientWithHistoricalDrugStartDateObs.addParameter(
        new Parameter("location", "location", Location.class));
    return patientWithHistoricalDrugStartDateObs;
  }

  public CohortDefinition getPatientsInArtCareWhoAbandoned() {
    Program hivCareProgram = hivMetadata.getHIVCareProgram();
    ProgramWorkflowState abandoned = hivMetadata.getArtCareAbandonedWorkflowState();
    return genericCohortQueires.getPatientsBasedOnPatientStatesBeforeDate(
        hivCareProgram.getProgramId(), abandoned.getProgramWorkflowStateId());
  }

  public CohortDefinition getPatientsInArtCareWhoDied() {
    Program hivCareProgram = hivMetadata.getHIVCareProgram();
    ProgramWorkflowState dead = hivMetadata.getArtCareDeadWorkflowState();
    return genericCohortQueires.getPatientsBasedOnPatientStatesBeforeDate(
        hivCareProgram.getProgramId(), dead.getProgramWorkflowStateId());
  }

  public CohortDefinition getPatientsInArtCareWhoInitiatedArt() {
    Program hivCareProgram = hivMetadata.getHIVCareProgram();
    ProgramWorkflowState initiatedArt = hivMetadata.getArtCareInitiatedWorkflowState();
    return genericCohortQueires.getPatientsBasedOnPatientStatesBeforeDate(
        hivCareProgram.getProgramId(), initiatedArt.getProgramWorkflowStateId());
  }

  public CohortDefinition getPatientsInArtWhoSuspendedTreatment() {
    ProgramWorkflowState suspendedTreatment = hivMetadata.getArtSuspendedTreatmentWorkflowState();
    Program artProgram = hivMetadata.getARTProgram();
    return genericCohortQueires.getPatientsBasedOnPatientStatesBeforeDate(
        artProgram.getProgramId(), suspendedTreatment.getProgramWorkflowStateId());
  }

  public CohortDefinition getPatientsInArtTransferredOutToAnotherHealthFacility() {
    Program artProgram = hivMetadata.getARTProgram();
    ProgramWorkflowState state =
        hivMetadata.getArtTransferredOutToAnotherHealthFacilityWorkflowState();
    return genericCohortQueires.getPatientsBasedOnPatientStatesBeforeDate(
        artProgram.getProgramId(), state.getProgramWorkflowStateId());
  }

  public CohortDefinition getPatientsInArtWhoDied() {
    Program artProgram = hivMetadata.getARTProgram();
    ProgramWorkflowState dead = hivMetadata.getArtDeadWorkflowState();
    return genericCohortQueires.getPatientsBasedOnPatientStatesBeforeDate(
        artProgram.getProgramId(), dead.getProgramWorkflowStateId());
  }

  public CohortDefinition getPatientsInArtWhoAbandoned() {
    Program artProgram = hivMetadata.getARTProgram();
    ProgramWorkflowState state = hivMetadata.getArtAbandonedWorkflowState();
    return genericCohortQueires.getPatientsBasedOnPatientStatesBeforeDate(
        artProgram.getProgramId(), state.getProgramWorkflowStateId());
  }

  /**
   * Get People who inject drugs (PID)
   *
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getDrugUserKeyPopCohort() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("People who inject drugs");
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    cd.addSearch(
        "PID",
        EptsReportUtils.map(
            getKeyPopulationDisag(
                hivMetadata.getDrugUseConcept(), KeyPopulationGenderSelection.ALL),
            "endDate=${endDate},location=${location}"));
    cd.setCompositionString("PID");

    return cd;
  }

  /**
   * Get male patients who have sex with men
   *
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getMaleHomosexualKeyPopDefinition() {

    CompositionCohortDefinition comp = new CompositionCohortDefinition();
    comp.setName("Homosexual Key Pop");
    comp.addParameter(new Parameter("endDate", "end Date", Date.class));
    comp.addParameter(new Parameter("location", "location", Location.class));
    comp.addSearch(
        "HOMOSEXUAL",
        Mapped.mapStraightThrough(
            getKeyPopulationDisag(
                hivMetadata.getHomosexualConcept(),
                HivCohortQueries.KeyPopulationGenderSelection.MALE)));
    comp.addSearch("PID", Mapped.mapStraightThrough(getDrugUserKeyPopCohort()));
    comp.setCompositionString("HOMOSEXUAL AND NOT PID");
    return comp;
  }

  public CohortDefinition getImprisonmentKeyPopCohort() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("People in prison and other closed settings");

    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addSearch("SW", Mapped.mapStraightThrough(getSexWorkerKeyPopCohortDefinition()));

    cd.addSearch("HOMOSEXUAL", Mapped.mapStraightThrough(getMaleHomosexualKeyPopDefinition()));

    cd.addSearch("PID", Mapped.mapStraightThrough(getDrugUserKeyPopCohort()));

    cd.addSearch(
        "REC",
        Mapped.mapStraightThrough(
            getKeyPopulationDisag(
                hivMetadata.getImprisonmentConcept(),
                HivCohortQueries.KeyPopulationGenderSelection.ALL)));

    cd.setCompositionString("REC AND NOT (SW OR HOMOSEXUAL OR PID)");

    return cd;
  }

  public CohortDefinition getTransgenderKeyPopCohortDefinition() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("People in prison and other closed settings");

    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    cd.addSearch(
        "TRANSGENDER",
        Mapped.mapStraightThrough(
            getKeyPopulationDisag(
                hivMetadata.getTransGenderConcept(),
                HivCohortQueries.KeyPopulationGenderSelection.ALL)));

    cd.addSearch("SW", Mapped.mapStraightThrough(getSexWorkerKeyPopCohortDefinition()));

    cd.addSearch("HOMOSEXUAL", Mapped.mapStraightThrough(getMaleHomosexualKeyPopDefinition()));

    cd.addSearch("PID", Mapped.mapStraightThrough(getDrugUserKeyPopCohort()));

    cd.addSearch("REC", Mapped.mapStraightThrough(getImprisonmentKeyPopCohort()));

    cd.setCompositionString("TRANSGENDER AND NOT (REC OR SW OR HOMOSEXUAL OR PID)");

    return cd;
  }

  /**
   * Get only female patients who are sex workers
   *
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getFemaleSexWorkersKeyPopCohortDefinition() {
    CompositionCohortDefinition comp = new CompositionCohortDefinition();
    comp.setName("Only female sex workers");
    comp.addParameter(new Parameter("endDate", "endDate", Date.class));
    comp.addParameter(new Parameter("location", "location", Location.class));
    comp.addSearch(
        "CSW",
        Mapped.mapStraightThrough(
            getKeyPopulationDisag(
                hivMetadata.getSexWorkerConcept(), KeyPopulationGenderSelection.FEMALE)));

    comp.addSearch("PID", Mapped.mapStraightThrough(getDrugUserKeyPopCohort()));

    comp.setCompositionString("CSW AND NOT PID");
    return comp;
  }

  /**
   * Get All patients who are sex workers (male and female)
   *
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getSexWorkerKeyPopCohortDefinition() {
    CompositionCohortDefinition comp = new CompositionCohortDefinition();
    comp.setName("Get Patients marked as Sex Workers (male and female) Key Population ");
    comp.addParameter(new Parameter("endDate", "endDate", Date.class));
    comp.addParameter(new Parameter("location", "location", Location.class));
    comp.addSearch("FSW", Mapped.mapStraightThrough(getFemaleSexWorkersKeyPopCohortDefinition()));

    comp.addSearch("MSW", Mapped.mapStraightThrough(getMaleSexWorkersKeyPopCohortDefinition()));

    comp.setCompositionString("MSW OR FSW");

    return comp;
  }

  /**
   * Get only male patients who are sex workers
   *
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getMaleSexWorkersKeyPopCohortDefinition() {
    CompositionCohortDefinition comp = new CompositionCohortDefinition();
    comp.setName("Only Male sex workers");
    comp.addParameter(new Parameter("endDate", "endDate", Date.class));
    comp.addParameter(new Parameter("location", "location", Location.class));

    comp.addSearch("HOMOSEXUAL", Mapped.mapStraightThrough(getMaleHomosexualKeyPopDefinition()));

    comp.addSearch("PID", Mapped.mapStraightThrough(getDrugUserKeyPopCohort()));

    comp.addSearch(
        "MSW",
        Mapped.mapStraightThrough(
            getKeyPopulationDisag(
                hivMetadata.getSexWorkerConcept(), KeyPopulationGenderSelection.MALE)));

    comp.setCompositionString("MSW AND NOT (HOMOSEXUAL OR PID)");
    return comp;
  }

  public CohortDefinition getPatientsTransferredOut() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Get patients who are transferred out - disaggretation");
    cd.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    Integer transferredOut = hivMetadata.getTransferredOutConcept().getConceptId();
    Integer transferOutState =
        hivMetadata
            .getTransferredOutToAnotherHealthFacilityWorkflowState()
            .getProgramWorkflowStateId();

    CohortDefinition transferred = getPatientsTransferredOut(transferredOut, transferOutState);

    CohortDefinition artPickup = getTransferredOutBetweenNextPickupDateFilaAndRecepcaoLevantou();

    cd.addSearch(
        "transferredOut",
        EptsReportUtils.map(transferred, "onOrBefore=${onOrBefore},location=${location}"));

    cd.addSearch(
        "arvPickup", EptsReportUtils.map(artPickup, "endDate=${onOrBefore},location=${location}"));
    cd.setCompositionString("transferredOut AND arvPickup");

    return cd;
  }

  public CohortDefinition getPatientsWhoStoppedTreatment() {
    Integer suspended = hivMetadata.getSuspendedTreatmentConcept().getConceptId();
    Integer suspendedState =
        hivMetadata.getSuspendedTreatmentWorkflowState().getProgramWorkflowStateId();
    return getPatientsSuspendedOrStopTreatment(suspended, suspendedState);
  }

  public CohortDefinition getTransferredInViaProgram(boolean hasStartDate) {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("transferredFromOtherHealthFacility");
    cd.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    cd.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    String query =
        "select p.patient_id from patient p "
            + "inner join patient_program pg on p.patient_id=pg.patient_id "
            + "inner join patient_state ps on pg.patient_program_id=ps.patient_program_id "
            + "where pg.voided=0 and ps.voided=0 and p.voided=0 and pg.program_id=${artProgram}"
            + " and ps.state=${transferStateId}"
            + " and ps.start_date=pg.date_enrolled ";
    if (hasStartDate) {
      query = query + "and ps.start_date between :onOrAfter and :onOrBefore ";
    } else {
      query = query + "and ps.start_date <= :onOrBefore ";
    }
    query = query + "and location_id= :location group by p.patient_id";

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("artProgram", hivMetadata.getARTProgram().getProgramId());
    valuesMap.put(
        "transferStateId",
        hivMetadata
            .getTransferredFromOtherHealthFacilityWorkflowState()
            .getProgramWorkflowStateId());

    StringSubstitutor sub = new StringSubstitutor(valuesMap);
    cd.setQuery(sub.replace(query));

    return cd;
  }

  public CohortDefinition getTransferredInViaMastercard() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.setQuery(
        ResumoMensalQueries.getPatientsTransferredFromAnotherHealthFacilityByEndOfPreviousMonth(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId(),
            hivMetadata.getARTProgram().getProgramId(),
            hivMetadata
                .getArtTransferredFromOtherHealthFacilityWorkflowState()
                .getProgramWorkflowStateId()));
    return cd;
  }

  private CohortDefinition getPatientsTransferredOut(int transferedOutConcept, int patientStateId) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName("get Patients Transferred Out by end of the period ");
    sqlCohortDefinition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put(
        "adultoSeguimentoEncounterType",
        hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put(
        "pediatriaSeguimentoEncounterType",
        hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put(
        "pharmaciaEncounterType", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());

    map.put(
        "masterCardEncounterType", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put(
        "stateOfStayOfPreArtPatient", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("transferedOutConcept", transferedOutConcept);
    map.put("autoTransferConcept", hivMetadata.getAutoTransferConcept().getConceptId());
    map.put("stateOfStayOfArtPatient", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("artProgram", hivMetadata.getARTProgram().getProgramId());
    map.put("transferredOutToAnotherHealthFacilityWorkflowState", patientStateId);
    map.put("defaultingMotiveConcept", hivMetadata.getDefaultingMotiveConcept().getConceptId());
    map.put(
        "buscaActivaEncounterType", hivMetadata.getBuscaActivaEncounterType().getEncounterTypeId());

    String query =
        "  SELECT mostrecent.patient_id "
            + "FROM ("
            + " SELECT lastest.patient_id ,Max(lastest.last_date) as  last_date "
            + " FROM (  "
            + "    SELECT p.patient_id ,ps.start_date AS last_date  "
            + "    FROM patient p   "
            + "        INNER JOIN patient_program pg   "
            + "            ON p.patient_id=pg.patient_id   "
            + "        INNER JOIN patient_state ps   "
            + "            ON pg.patient_program_id=ps.patient_program_id   "
            + "    WHERE pg.voided=0   "
            + "        AND ps.voided=0   "
            + "        AND p.voided=0   "
            + "        AND pg.program_id= ${artProgram}  "
            + "        AND ps.state = ${transferredOutToAnotherHealthFacilityWorkflowState}   "
            + "        AND ps.end_date is null   "
            + "        AND ps.start_date <= :onOrBefore   "
            + "        AND pg.location_id= :location   "
            + "         GROUP BY p.patient_id  "
            + "  "
            + "    UNION  "
            + "  "
            + "    SELECT  p.patient_id,  Max(e.encounter_datetime) AS last_date  "
            + "    FROM patient p    "
            + "        INNER JOIN encounter e   "
            + "            ON e.patient_id=p.patient_id   "
            + "        INNER JOIN obs o   "
            + "            ON o.encounter_id=e.encounter_id   "
            + "    WHERE  p.voided = 0   "
            + "        AND e.voided = 0   "
            + "        AND o.voided = 0   "
            + "        AND e.encounter_type = ${adultoSeguimentoEncounterType}   "
            + "        AND o.concept_id = ${stateOfStayOfArtPatient}  "
            + "        AND o.value_coded =  ${transferedOutConcept}   "
            + "        AND e.encounter_datetime <= :onOrBefore   "
            + "        AND e.location_id =  :location   "
            + "         GROUP BY p.patient_id  "
            + "  "
            + "    UNION   "
            + "  "
            + "    SELECT  p.patient_id , Max(o.obs_datetime) AS last_date  "
            + "    FROM patient p    "
            + "        INNER JOIN encounter e   "
            + "            ON e.patient_id=p.patient_id   "
            + "        INNER JOIN obs o   "
            + "            ON o.encounter_id=e.encounter_id   "
            + "    WHERE  p.voided = 0   "
            + "        AND e.voided = 0   "
            + "        AND o.voided = 0   "
            + "        AND e.encounter_type = ${masterCardEncounterType}  "
            + "        AND o.concept_id = ${stateOfStayOfPreArtPatient}  "
            + "        AND o.value_coded = ${transferedOutConcept}   "
            + "        AND o.obs_datetime <= :onOrBefore   "
            + "        AND e.location_id =  :location  "
            + "         GROUP BY p.patient_id  "
            + "  "
            + "    UNION   "
            + "  "
            + "    SELECT  p.patient_id , Max(e.encounter_datetime) AS last_date  "
            + "    FROM patient p    "
            + "        INNER JOIN encounter e   "
            + "            ON e.patient_id=p.patient_id   "
            + "        INNER JOIN obs o   "
            + "            ON o.encounter_id=e.encounter_id   "
            + "    WHERE  p.voided = 0   "
            + "        AND e.voided = 0   "
            + "        AND o.voided = 0   "
            + "        AND e.encounter_type= ${buscaActivaEncounterType}  "
            + "        AND o.concept_id = ${defaultingMotiveConcept}  "
            + "        AND o.value_coded IN (${transferedOutConcept} ,${autoTransferConcept})  "
            + "        AND e.encounter_datetime <= :onOrBefore   "
            + "        AND e.location_id =  :location  "
            + "         GROUP BY p.patient_id  "
            + "  "
            + ") lastest   "
            + "  "
            + "WHERE lastest.patient_id NOT  IN("
            + " "
            + "  			     SELECT  p.patient_id    "
            + "	                 FROM patient p      "
            + "	                     INNER JOIN encounter e     "
            + "	                         ON e.patient_id=p.patient_id     "
            + "	                 WHERE  p.voided = 0     "
            + "	                     AND e.voided = 0     "
            + "	                     AND e.encounter_type IN (${adultoSeguimentoEncounterType},"
            + "${pediatriaSeguimentoEncounterType},"
            + "${pharmaciaEncounterType})    "
            + "	                     AND DATEDIFF (e.encounter_datetime, lastest.last_date)>0 "
            + "                      AND DATEDIFF (e.encounter_datetime, :onOrBefore )<=0    "
            + "	                     AND e.location_id =  :location    "
            + "	                 GROUP BY p.patient_id "
            + ")  "
            + " GROUP BY lastest.patient_id"
            + " )mostrecent "
            + " GROUP BY mostrecent.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    String mappedQuery = stringSubstitutor.replace(query);

    sqlCohortDefinition.setQuery(mappedQuery);

    return sqlCohortDefinition;
  }

  private CohortDefinition getPatientsSuspendedOrStopTreatment(
      int suspendedConcept, int patientStateId) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName(" Patients Who have Suspended or Stopped Treatment");
    sqlCohortDefinition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put(
        "adultoSeguimentoEncounterType",
        hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put(
        "pediatriaSeguimentoEncounterType",
        hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put(
        "pharmaciaEncounterType", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put(
        "masterCardEncounterType", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put(
        "stateOfStayOfPreArtPatient", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("suspendedConcept", suspendedConcept);
    map.put("stateOfStayOfArtPatient", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("artProgram", hivMetadata.getARTProgram().getProgramId());
    map.put("suspendedTreatmentWorkflowState", patientStateId);

    String query =
        "  SELECT mostrecent.patient_id "
            + "FROM ("
            + " SELECT lastest.patient_id ,Max(lastest.last_date) as  last_date "
            + " FROM (  "
            + "    SELECT p.patient_id ,ps.start_date AS last_date  "
            + "    FROM patient p   "
            + "        INNER JOIN patient_program pg   "
            + "            ON p.patient_id=pg.patient_id   "
            + "        INNER JOIN patient_state ps   "
            + "            ON pg.patient_program_id=ps.patient_program_id   "
            + "    WHERE pg.voided=0   "
            + "        AND ps.voided=0   "
            + "        AND p.voided=0   "
            + "        AND pg.program_id= ${artProgram}  "
            + "        AND ps.state = ${suspendedTreatmentWorkflowState}   "
            + "        AND ps.end_date is null   "
            + "        AND ps.start_date <= :onOrBefore   "
            + "        AND pg.location_id= :location   "
            + "         GROUP BY p.patient_id  "
            + "  "
            + "    UNION  "
            + "  "
            + "    SELECT  p.patient_id,  Max(o.obs_datetime) AS last_date  "
            + "    FROM patient p    "
            + "        INNER JOIN encounter e   "
            + "            ON e.patient_id=p.patient_id   "
            + "        INNER JOIN obs o   "
            + "            ON o.encounter_id=e.encounter_id   "
            + "    WHERE  p.voided = 0   "
            + "        AND e.voided = 0   "
            + "        AND o.voided = 0   "
            + "        AND e.encounter_type = ${adultoSeguimentoEncounterType}   "
            + "        AND o.concept_id = ${stateOfStayOfArtPatient}  "
            + "        AND o.value_coded =  ${suspendedConcept}   "
            + "        AND o.obs_datetime <= :onOrBefore   "
            + "        AND e.location_id =  :location   "
            + "         GROUP BY p.patient_id  "
            + "  "
            + "    UNION   "
            + "  "
            + "    SELECT  p.patient_id , Max(e.encounter_datetime) AS last_date  "
            + "    FROM patient p    "
            + "        INNER JOIN encounter e   "
            + "            ON e.patient_id=p.patient_id   "
            + "        INNER JOIN obs o   "
            + "            ON o.encounter_id=e.encounter_id   "
            + "    WHERE  p.voided = 0   "
            + "        AND e.voided = 0   "
            + "        AND o.voided = 0   "
            + "        AND e.encounter_type = ${masterCardEncounterType}  "
            + "        AND o.concept_id = ${stateOfStayOfPreArtPatient}  "
            + "        AND o.value_coded = ${suspendedConcept}   "
            + "        AND e.encounter_datetime <= :onOrBefore   "
            + "        AND e.location_id =  :location  "
            + "         GROUP BY p.patient_id  "
            + "  "
            + ") lastest   "
            + "  "
            + "WHERE lastest.patient_id NOT  IN("
            + " "
            + "  			     SELECT  p.patient_id    "
            + "	                 FROM patient p      "
            + "	                     INNER JOIN encounter e     "
            + "	                         ON e.patient_id=p.patient_id     "
            + "	                 WHERE  p.voided = 0     "
            + "	                     AND e.voided = 0     "
            + "	                     AND e.encounter_type IN (${adultoSeguimentoEncounterType},"
            + "${pediatriaSeguimentoEncounterType},"
            + "${pharmaciaEncounterType})    "
            + "	                     AND DATEDIFF (e.encounter_datetime, lastest.last_date)>0 "
            + "                      AND DATEDIFF (e.encounter_datetime, :onOrBefore )<=0    "
            + "	                     AND e.location_id =  :location    "
            + "	                 GROUP BY p.patient_id "
            + ")  "
            + " GROUP BY lastest.patient_id"
            + " )mostrecent "
            + " GROUP BY mostrecent.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    String mappedQuery = stringSubstitutor.replace(query);

    sqlCohortDefinition.setQuery(mappedQuery);

    return sqlCohortDefinition;
  }

  // TARGET GROUP SECTION
  public CohortDefinition getAdolescentsAndYouthTargetGroupCohort() {
    CalculationCohortDefinition cd = new CalculationCohortDefinition();
    cd.setCalculation(Context.getRegisteredComponents(TargetGroupCalculation.class).get(0));
    cd.setName("Adolescents and Youth at Risk Target Group");
    cd.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addCalculationParameter(TIPO, ADOLESCENT_AND_YOUTH);
    return cd;
  }

  public CohortDefinition getPregnantWomanTargetGroupCohort() {
    CalculationCohortDefinition cd = new CalculationCohortDefinition();
    cd.setCalculation(Context.getRegisteredComponents(TargetGroupCalculation.class).get(0));
    cd.setName("Pregnant Woman at Risk");
    cd.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addCalculationParameter(TIPO, PREGNANT);
    return cd;
  }

  public CohortDefinition getBreastfeedingTargetGroupCohort() {
    CalculationCohortDefinition cd = new CalculationCohortDefinition();
    cd.setCalculation(Context.getRegisteredComponents(TargetGroupCalculation.class).get(0));
    cd.setName("Breastfeeding woman at Risk Target Group");
    cd.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addCalculationParameter(TIPO, BREASTFEEDING);
    return cd;
  }

  /**
   * Get Pregnant Woman at Risk (Female)
   *
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getPregnantWomanTargetGroupDefinition() {

    CompositionCohortDefinition comp = new CompositionCohortDefinition();
    comp.setName("Only Pregnant Woman at Risk Target Group");
    comp.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    comp.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    comp.addParameter(new Parameter("location", "location", Location.class));
    comp.addSearch(
        "pregnant",
        EptsReportUtils.map(
            getPregnantWomanTargetGroupCohort(),
            "onOrBefore=${onOrBefore},onOrAfter=${onOrAfter},location=${location}"));
    comp.addSearch("F", EptsReportUtils.map(genderCohortQueries.femaleCohort(), ""));
    comp.setCompositionString("pregnant AND F");
    return comp;
  }

  /**
   * Get Breastfeeding Woman at Risk (Female)
   *
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getBreastfeedingWomanTargetGroupDefinition() {

    CompositionCohortDefinition comp = new CompositionCohortDefinition();
    comp.setName("Only Breastfeeding Woman at Risk Target Group");
    comp.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    comp.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    comp.addParameter(new Parameter("location", "location", Location.class));
    comp.addSearch(
        "breastfeeding",
        EptsReportUtils.map(
            getBreastfeedingTargetGroupCohort(),
            "onOrBefore=${onOrBefore},onOrAfter=${onOrAfter},location=${location}"));
    comp.addSearch("F", EptsReportUtils.map(genderCohortQueries.femaleCohort(), ""));
    comp.setCompositionString("breastfeeding AND F");
    return comp;
  }

  public CohortDefinition getMilitaryTargetGroupCohort() {
    CalculationCohortDefinition cd = new CalculationCohortDefinition();
    cd.setCalculation(Context.getRegisteredComponents(TargetGroupCalculation.class).get(0));
    cd.setName("Military Target Group");
    cd.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addCalculationParameter(TIPO, MILITARY);
    return cd;
  }

  public CohortDefinition getMinerTargetGroupCohort() {
    CalculationCohortDefinition cd = new CalculationCohortDefinition();
    cd.setCalculation(Context.getRegisteredComponents(TargetGroupCalculation.class).get(0));
    cd.setName("Miner Target Group");
    cd.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addCalculationParameter(TIPO, MINER);
    return cd;
  }

  public CohortDefinition getTruckDriverTargetGroupCohort() {
    CalculationCohortDefinition cd = new CalculationCohortDefinition();
    cd.setCalculation(Context.getRegisteredComponents(TargetGroupCalculation.class).get(0));
    cd.setName("Long Course Truck Driver Target Group");
    cd.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addCalculationParameter(TIPO, TRUCK_DRIVER);
    return cd;
  }

  public CohortDefinition getSerodiscordantCouplesTargetGroupCohort() {
    CalculationCohortDefinition cd = new CalculationCohortDefinition();
    cd.setCalculation(Context.getRegisteredComponents(TargetGroupCalculation.class).get(0));
    cd.setName("Serodiscordant Couples Target Group");
    cd.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addCalculationParameter(TIPO, SERODISCORDANT);
    return cd;
  }

  /**
   * The system will consider patient as transferred out as above defined only if the most recent
   * date between (next scheduled ART pick-up on FILA + 1 day) and (the most recent ART pickup date
   * on Ficha Recepção – Levantou ARVs + 31 days) falls by end of the reporting period.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getTransferredOutBetweenNextPickupDateFilaAndRecepcaoLevantou() {

    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName(
        "Patients Transfered Out between (next scheduled ART pick-up on FILA + 1 day) "
            + "and (the most recent ART pickup date on Ficha Recepção – Levantou ARVs + 31 days");
    definition.addParameter(new Parameter("endDate", "endDate", Date.class));
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
            + "                 AND        e.encounter_datetime <= :endDate "
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
            + "                 AND        o.value_datetime  <= :endDate  "
            + "                 AND        e.location_id = :location "
            + "               GROUP BY   p.patient_id "
            + " )  considered_transferred "
            + " GROUP BY considered_transferred.patient_id "
            + " ) final "
            + " WHERE  final.max_date  <= :endDate  ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    definition.setQuery(stringSubstitutor.replace(query));

    return definition;
  }

  /** Enumeration to return the Gender or Gender Composition formats for Key Population */
  public enum KeyPopulationGenderSelection {
    MALE {
      @Override
      public String getGender() {
        return " 'M' ";
      }
    },

    FEMALE {
      @Override
      public String getGender() {
        return " 'F' ";
      }
    },

    ALL {
      @Override
      public String getGender() {
        return MALE.getGender() + "," + FEMALE.getGender();
      }
    };

    public abstract String getGender();
  }

  /**
   * Method to return the Correspondent value of Key Population Concept on Demographic module
   *
   * @param concept Key Population Concept
   * @return {@link String}
   */
  private String getKeyPopulationValueBasedOnConceptForPersonAttribute(Concept concept) {

    Map<Concept, String> map = new HashMap<>();
    map.put(hivMetadata.getHomosexualConcept(), "'MSM','HSH'");
    map.put(hivMetadata.getDrugUseConcept(), "'PID'");
    map.put(hivMetadata.getImprisonmentConcept(), "'PRISONER','RC','REC'");
    map.put(hivMetadata.getSexWorkerConcept(), "'CSW','TS','MTS','FSW','MSW'");
    map.put(hivMetadata.getTransGenderConcept(), "'TG'");
    map.put(hivMetadata.getOtherOrNonCodedConcept(), "'OUTRO'");

    return map.get(concept);
  }

  private String getApplicableKeyPopFor(KeyPopulationGenderSelection gender) {

    List<Integer> keyPops =
        Arrays.asList(
            hivMetadata.getDrugUseConcept().getConceptId(),
            hivMetadata.getImprisonmentConcept().getConceptId(),
            hivMetadata.getTransGenderConcept().getConceptId(),
            hivMetadata.getSexWorkerConcept().getConceptId());
    if (gender == KeyPopulationGenderSelection.MALE || gender == KeyPopulationGenderSelection.ALL) {

      keyPops.add(hivMetadata.getHomosexualConcept().getConceptId());
    }

    return StringUtils.join(",");
  }

  public CohortDefinition getKeyPopulationDisag(
      Concept keyPopConcept, KeyPopulationGenderSelection gender) {

    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("Get all Patients Marked as Key Population");
    definition.addParameter(new Parameter("endDate", "endDate", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put(
        "17", hivMetadata.getIdentificadorDefinidoLocalmente01().getPersonAttributeTypeId());
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("23703", hivMetadata.getKeyPopulationConcept().getConceptId());
    valuesMap.put("5622", hivMetadata.getOtherOrNonCodedConcept().getConceptId());
    valuesMap.put("keypop", keyPopConcept.getConceptId());

    String query =
        "SELECT p.person_id "
            + "FROM   person p "
            + "       LEFT JOIN encounter e "
            + "              ON e.patient_id = p.person_id "
            + "       LEFT JOIN obs o "
            + "              ON o.encounter_id = e.encounter_id "
            + "       LEFT JOIN person_attribute pa "
            + "              ON p.person_id = pa.person_id "
            + "       LEFT JOIN person_attribute_type pat "
            + "              ON pa.person_attribute_type_id = pat.person_attribute_type_id "
            + "       INNER JOIN (SELECT last_kp.patient_id, "
            + "                          Max(last_kp.last_date) AS most_recent "
            + "                   FROM   (SELECT p.person_id          AS patient_id, "
            + "                                  Max(pa.date_created) AS last_date "
            + "                           FROM   person p "
            + "                                  INNER JOIN person_attribute pa "
            + "                                          ON p.person_id = pa.person_id "
            + "                                  INNER JOIN person_attribute_type pat "
            + "                                          ON pa.person_attribute_type_id = "
            + "                                             pat.person_attribute_type_id "
            + "                           WHERE  p.voided = 0 "
            + "                                  AND pa.person_attribute_type_id = ${17} "
            + "                                  AND pa.value IN ( 'HSH', 'PID','MTS','REC','MSM','HSH','PRISONER','RC','CSW','TS','MTS','FSW','MSW','HTS') "
            + "                           GROUP  BY p.person_id "
            + "                           UNION "
            + "                           SELECT p.patient_id AS patient_id, "
            + "                                  Max(e.encounter_datetime) AS last_date "
            + "                           FROM   patient p "
            + "                                  INNER JOIN encounter e "
            + "                                          ON e.patient_id = p.patient_id "
            + "                                  INNER JOIN obs o "
            + "                                          ON o.encounter_id = e.encounter_id "
            + "                           WHERE  e.voided = 0 "
            + "                                  AND p.voided = 0 "
            + "                                  AND o.voided = 0 "
            + "                                  AND e.location_id = :location "
            + "                                  AND e.encounter_type IN ( ${6}, ${35} ) "
            + "                                  AND o.concept_id = ${23703} "
            + "                                  AND o.value_coded <> ${5622}  "
            + "                                  AND e.encounter_datetime <= :endDate "
            + "                           GROUP  BY p.patient_id) last_kp "
            + "                   GROUP  BY last_kp.patient_id) kp_result "
            + "               ON kp_result.patient_id = p.person_id "
            + "WHERE  ( e.voided = 0 "
            + "         AND p.voided = 0 "
            + "         AND e.location_id = :location "
            + "         AND o.voided = 0 "
            + "         AND p.gender IN ( "
            + gender.getGender()
            + " ) "
            + "         AND o.concept_id = ${23703} "
            + "         AND o.value_coded = ${keypop} "
            + "         AND e.encounter_datetime = kp_result.most_recent "
            + "         AND ( ( e.encounter_type = ${6} ) "
            + "                OR ( e.encounter_type = ${35} "
            + "                     AND NOT EXISTS(SELECT e2.patient_id "
            + "                                    FROM   encounter e2 "
            + "                                           INNER JOIN obs o2 "
            + "                                                   ON e2.encounter_id = "
            + "                                                      o2.encounter_id "
            + "                                    WHERE  e2.encounter_type = ${6} "
            + "                                           AND o2.concept_id = ${23703} "
            + "                                           AND o2.value_coded <> ${5622} "
            + "                                           AND e2.encounter_datetime = "
            + "                                               kp_result.most_recent "
            + "                                           AND e2.patient_id = p.person_id) ) ) "
            + "       ) "
            + "        OR ( p.voided = 0 "
            + "             AND pa.person_attribute_type_id = ${17} "
            + "         AND p.gender IN ( "
            + gender.getGender()
            + " ) "
            + "             AND pa.value IN ( "
            + getKeyPopulationValueBasedOnConceptForPersonAttribute(keyPopConcept)
            + " ) "
            + "             AND pa.date_created = kp_result.most_recent "
            + "             AND NOT EXISTS(SELECT e2.patient_id "
            + "                            FROM   encounter e2 "
            + "                                   INNER JOIN obs o2 "
            + "                                           ON e2.encounter_id = o2.encounter_id "
            + "                            WHERE  e2.encounter_type IN ( ${6}, ${35} ) "
            + "                                   AND o2.concept_id = ${23703} "
            + "                                   AND o2.value_coded <> ${5622} "
            + "                                   AND e2.encounter_datetime = "
            + "                                       kp_result.most_recent "
            + "                                   AND e2.patient_id = p.person_id) ) "
            + "GROUP  BY p.person_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    definition.setQuery(stringSubstitutor.replace(query));

    return definition;
  }
}
