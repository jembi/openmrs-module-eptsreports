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

import static org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils.map;
import static org.openmrs.module.reporting.evaluation.parameter.Mapped.mapStraightThrough;

import java.util.*;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.library.queries.BreastfeedingQueries;
import org.openmrs.module.eptsreports.reporting.library.queries.PregnantQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.DateObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Defines all of the TxNew Cohort Definition instances we want to expose for EPTS */
@Component
public class TxNewCohortQueries {

  @Autowired private HivMetadata hivMetadata;

  @Autowired private CommonMetadata commonMetadata;

  @Autowired private GenericCohortQueries genericCohorts;

  @Autowired private ResumoMensalCohortQueries resumoMensalCohortQueries;

  @Autowired
  private AdvancedDiseaseAndTBCascadeCohortQueries advancedDiseaseAndTBCascadeCohortQueries;

  /**
   * <b>Description:</b> Patients with updated date of departure in the ART Service
   *
   * <p><b>Technical Specs</b>
   *
   * <blockquote>
   *
   * Have date of delivery <b>(obs conceppt_id = 5599)</b> updated in the ART <b>(encounterType_id =
   * 5 and 6)</b>. Note that the 'Start Date' and 'End Date' parameters refer to the date of
   * delivery and not the date of registration (update)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithUpdatedDepartureInART() {
    DateObsCohortDefinition cd = new DateObsCohortDefinition();
    cd.setName("patientsWithUpdatedDepartureInART");
    cd.setQuestion(commonMetadata.getPriorDeliveryDateConcept());
    cd.setTimeModifier(BaseObsCohortDefinition.TimeModifier.ANY);

    List<EncounterType> encounterTypes = new ArrayList<>();
    encounterTypes.add(hivMetadata.getAdultoSeguimentoEncounterType());
    encounterTypes.add(hivMetadata.getARVAdultInitialEncounterType());
    cd.setEncounterTypeList(encounterTypes);

    cd.setOperator1(RangeComparator.GREATER_EQUAL);
    cd.setOperator2(RangeComparator.LESS_EQUAL);

    cd.addParameter(new Parameter("value1", "After Date", Date.class));
    cd.addParameter(new Parameter("value2", "Before Date", Date.class));

    cd.addParameter(new Parameter("locationList", "Location", Location.class));

    return cd;
  }

  /**
   * <b>Description:</b> Prengancy Patients enrolled in the ART Service
   *
   * <p><b>Technical Specs</b>
   *
   * <blockquote>
   *
   * Marked pregnant <b>(obs concept_id = 1982)</b> int the initial <b>(encounterType_id = 5)</b> or
   * follow-up <b>(encounterType_id = 6)</b> in Ficha resumo <b>(encounterType = 53)</b>
   * consultation.
   *
   * <p>Have “Number of weeks Pregnant <b>(obs concept_id = 1279)</b>” and have "Pregnancy Due Date
   * <b>(obs concept_id = 1600)</b>" in the initial or follow-up between start and end date
   * <b>(encounter_datetime)</b>.
   *
   * <p>Enrolled in PTV(ETV) program <b>(program_id = 8)</b> between start and end date
   * <b>(patient_program date_enrolled)</b>.
   *
   * </blockquote>
   *
   * @param dsd If it's true, subtract the 'End Date' by an interval of 9 months
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsPregnantEnrolledOnART(boolean dsd) {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("patientsPregnantEnrolledOnART");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.setQuery(
        PregnantQueries.getPregnantWhileOnArt(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getNumberOfWeeksPregnant().getConceptId(),
            hivMetadata.getPregnancyDueDate().getConceptId(),
            hivMetadata.getARVAdultInitialEncounterType().getEncounterTypeId(),
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getFsrEncounterType().getEncounterTypeId(),
            hivMetadata.getSampleCollectionDateAndTime().getConceptId(),
            hivMetadata.getPtvEtvProgram().getProgramId(),
            hivMetadata.getCriteriaForArtStart().getConceptId(),
            hivMetadata.getBPlusConcept().getConceptId(),
            hivMetadata.getARVStartDateConcept().getConceptId(),
            commonMetadata.getPriorDeliveryDateConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId(),
            hivMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getPatientGaveBirthWorkflowState().getProgramWorkflowStateId(),
            dsd));
    return cd;
  }

  /**
   * <b>Description:</b> Women who gave birth 2 years ago
   *
   * <p><b>Technical Specs</b>
   *
   * <blockquote>
   *
   * enrolled in the PTV(ETV) program <b>(program_id = 8)</b> and have been updated as a childbirth
   * within 2 years of the reference date <b>(program_workflow_state_id = 27)</b>.
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoGaveBirthWithinReportingPeriod() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("patientsWhoGaveBirthWithinReportingPeriod");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.setQuery(
        BreastfeedingQueries.getPatientsWhoGaveBirthWithinReportingPeriod(
            hivMetadata.getPtvEtvProgram().getProgramId(), 27));

    return cd;
  }

  /**
   * <b>Description:</b> Breastfeeding enrolled on ART Service
   *
   * <p><b>Technical Specs</b>
   *
   * <blockquote>
   *
   * Have the “Delivery date <b>(obs concept_id = 5599)</b>” registered in the initial
   * <b>(encounterType_id = 5)</b> or follow-up <b>(encounterType_id = 6)</b> consultation and where
   * the delivery date is <b>( obs value_datetime )&gt;=startDate and &lt;=endDate</b>.
   *
   * <p>Have started ART for being breastfeeding <b>(concept_id = 6332)</b> as specified in
   * “CRITÉRIO PARA INÍCIO DE TARV <b>(concept_id = 6634)</b>” in the initial or follow-up
   * consultations between start and end date <b>(encounter_datetime)</b>.
   *
   * <p>Enrolled in PTV(ETV) program <b>(program_id = 8)</b> between start and end date <b>( patient
   * state id= 27 and start_date)</b>.
   *
   * <p>Have registered as breastfeeding in Ficha Resumo – Master Card <b>(encounterType_id =
   * 58)</b> between start and end date <b>(encounter_datetime)</b>.</b>
   *
   * </blockquote>
   *
   * @param dsd If it's true, subtract the 'End Date' by an interval of 18 months
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getTxNewBreastfeedingComposition(boolean dsd) {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("patientsBreastfeedingEnrolledOnART");
    cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
    cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.setQuery(
        PregnantQueries.getBreastfeedingWhileOnArt(
            commonMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getYesConcept().getConceptId(),
            hivMetadata.getPriorDeliveryDateConcept().getConceptId(),
            hivMetadata.getPregnancyDueDate().getConceptId(),
            hivMetadata.getARVAdultInitialEncounterType().getEncounterTypeId(),
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getCriteriaForArtStart().getConceptId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getPtvEtvProgram().getProgramId(),
            hivMetadata.getPatientGaveBirthWorkflowState().getProgramWorkflowStateId(),
            hivMetadata.getHistoricalDrugStartDateConcept().getConceptId(),
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getNumberOfWeeksPregnant().getConceptId(),
            hivMetadata.getBPlusConcept().getConceptId(),
            hivMetadata.getSampleCollectionDateAndTime().getConceptId(),
            hivMetadata.getFsrEncounterType().getEncounterTypeId(),
            dsd));
    return cd;
  }

  /**
   * <b>Description:</b> Patients who started ART on Period
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getTxNewCompositionCohort(String cohortName) {
    CompositionCohortDefinition txNewComposition = new CompositionCohortDefinition();
    txNewComposition.setName(cohortName);
    txNewComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    txNewComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    txNewComposition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition startedART = getPatientsStartedArtOnFilaOrArvPickupDuringThePeriod();
    CohortDefinition transferredIn =
        resumoMensalCohortQueries
            .getNumberOfPatientsTransferredInFromOtherHealthFacilitiesDuringCurrentMonthB2E();

    txNewComposition.getSearches().put("startedART", mapStraightThrough(startedART));
    txNewComposition.getSearches().put("transferredIn", mapStraightThrough(transferredIn));

    txNewComposition.setCompositionString("startedART NOT transferredIn");
    return txNewComposition;
  }

  /**
   * All patients whose earliest ART start date (NEW_FR4.1) falls on or after (>=) 21 December 2023.
   *
   * <p>AND whose first ever drug pick-up date between the following sources falls during the
   * reporting period:
   *
   * <ul>
   *   <li>Drug pick-up date registered on (FILA)
   *   <li>Drug pick-up date registered on (Recepção Levantou ARV) – Master Card
   * </ul>
   *
   * @return String
   */
  public String getPatientsArtStartOnFilaOrArvPickup() {

    return "       SELECT first.patient_id, MIN(first.pickup_date) first_pickup "
        + "       FROM ( "
        + "             SELECT p.patient_id, MIN(e.encounter_datetime) AS pickup_date "
        + "                FROM patient p "
        + "             INNER JOIN encounter e ON e.patient_id = p.patient_id "
        + "             INNER JOIN ( "
        + "                   SELECT p.patient_id, MIN(e.encounter_datetime) AS pickup_date "
        + "                   FROM patient p "
        + "                   INNER JOIN encounter e ON e.patient_id = p.patient_id "
        + "                 WHERE e.encounter_type = ${18} "
        + "                    AND e.voided = 0 "
        + "                    AND p.voided = 0 "
        + "                    AND e.location_id = :location "
        + "                 GROUP BY p.patient_id "
        + "                        ) first_ever ON first_ever.patient_id = p.patient_id "
        + "             WHERE p.voided = 0  "
        + "               AND e.voided = 0 "
        + "               AND e.encounter_type = ${18} "
        + "               AND e.location_id = :location "
        + "               AND e.encounter_datetime BETWEEN :startDate AND :endDate "
        + "       GROUP BY p.patient_id "
        + "       UNION "
        + "       SELECT p.patient_id, MIN(o.value_datetime) AS pickup_date "
        + "       FROM patient p "
        + "       INNER JOIN encounter e ON e.patient_id = p.patient_id "
        + "       INNER JOIN obs o ON o.encounter_id = e.encounter_id "
        + "       INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id  "
        + " INNER JOIN ( "
        + "       SELECT p.patient_id, MIN(o.value_datetime) AS pickup_date "
        + "       FROM patient p "
        + "       INNER JOIN encounter e ON e.patient_id = p.patient_id "
        + "       INNER JOIN obs o ON o.encounter_id = e.encounter_id "
        + "       WHERE e.encounter_type = ${52} "
        + "           AND o.concept_id = ${23866} "
        + "           AND o.voided = 0 "
        + "           AND e.location_id = :location "
        + "           AND e.voided = 0 "
        + "           AND p.voided = 0 "
        + "       GROUP BY p.patient_id "
        + " ) first_ever_pickup ON first_ever_pickup.patient_id = p.patient_id  "
        + "       WHERE e.encounter_type = ${52} "
        + "           AND o.concept_id = ${23866} "
        + "           AND o.value_datetime BETWEEN :startDate AND :endDate "
        + "           AND o.voided = 0 "
        + "           AND e.location_id = :location "
        + "           AND e.voided = 0 "
        + "           AND p.voided = 0 "
        + "       GROUP BY p.patient_id "
        + "        ) first "
        + "     GROUP BY first.patient_id ";
  }

  /**
   * <b>NEW_FR4: Patients who initiated ART during the reporting period</b>
   *
   * <p>All patients whose earliest ART start date (NEW_FR4.1) falls on or after (>=) 21 December
   * 2023.
   *
   * <p>AND whose first ever drug pick-up date between the following sources falls during the
   * reporting period:
   *
   * <ul>
   *   <li>Drug pick-up date registered on (FILA)
   *   <li>Drug pick-up date registered on (Recepção Levantou ARV) – Master Card
   * </ul>
   *
   * <p>AND excluding patients with an earliest ART start date from pick-up and clinical sources
   * (NEW_FR4.1) that falls before (<) 21 December 2023.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsStartedArtOnFilaOrArvPickupDuringThePeriod() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Number of patientes who initiated TARV - Fila and ARV Pickup");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("1255", hivMetadata.getARVPlanConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());

    String query =
        " SELECT new_art.patient_id "
            + " FROM ( "
            + " SELECT patient_id "
            + " FROM ( "
            + getPatientsStartedArtClinicalAndPickUp()
            + "       ) start "
            + " WHERE start.min_art_date >= '2023-12-21' "
            + " UNION "
            + " SELECT patient_id "
            + " FROM ( "
            + getPatientsArtStartOnFilaOrArvPickup()
            + "       ) start "
            + " WHERE start.first_pickup >= '2023-12-21' "
            + " ) new_art";

    StringSubstitutor sb = new StringSubstitutor(map);
    cd.setQuery(sb.replace(query));
    return cd;
  }

  /**
   * <b>NEW_FR4.1: Patient’s earliest ART start date from pick-up and clinical sources </b>
   *
   * <p>The system will identify the patient ART start date by selecting the earliest date from the
   * following sources:
   *
   * <ul>
   *   <li>First ever drug pick up date registered on (FILA)
   *   <li>Drug initiation date (ARV PLAN = START DRUGS) during the pharmacy or clinical visits
   *       First start drugs date set in in clinical tools (Ficha de Seguimento Adulto and Ficha de
   *       Seguimento Pediatria) or Ficha Resumo - Master Card.
   *   <li>Date of enrollment in ART Program
   *   <li>First ever drug pick-up date registered on (Recepção Levantou ARV) – Master Card
   * </ul>
   *
   * <p><b>Note: </b>Ensure that the ART start date is truly the first occurrence ever for the
   * patient. This is particularly important for patients that have different ART start dates
   * registered in the system.
   *
   * @return {@link CohortDefinition}
   */
  public String getPatientsStartedArtClinicalAndPickUp() {
    return "SELECT art.patient_id, "
        + "       Min(art.art_date) min_art_date "
        + "FROM   (SELECT p.patient_id, "
        + "               Min(e.encounter_datetime) art_date "
        + "        FROM   patient p "
        + "               INNER JOIN encounter e "
        + "                       ON e.patient_id = p.patient_id "
        + "        WHERE  e.encounter_type = ${18} "
        + "               AND e.encounter_datetime <= :endDate "
        + "               AND e.voided = 0 "
        + "               AND p.voided = 0 "
        + "               AND e.location_id = :location "
        + "        GROUP  BY p.patient_id "
        + "        UNION "
        + "        SELECT p.patient_id, "
        + "               Min(e.encounter_datetime) art_date "
        + "        FROM   patient p "
        + "               INNER JOIN encounter e "
        + "                       ON p.patient_id = e.patient_id "
        + "               INNER JOIN obs o "
        + "                       ON e.encounter_id = o.encounter_id "
        + "        WHERE  p.voided = 0 "
        + "               AND e.voided = 0 "
        + "               AND o.voided = 0 "
        + "               AND e.encounter_type IN ( ${6}, ${9}, ${18} ) "
        + "               AND o.concept_id = ${1255} "
        + "               AND o.value_coded = ${1256} "
        + "               AND e.encounter_datetime <= :endDate "
        + "               AND e.location_id = :location "
        + "        GROUP  BY p.patient_id "
        + "        UNION "
        + "        SELECT p.patient_id, "
        + "               Min(o.value_datetime) min_date "
        + "        FROM   patient p "
        + "               INNER JOIN encounter e "
        + "                       ON e.patient_id = p.patient_id "
        + "               INNER JOIN obs o "
        + "                       ON o.encounter_id = e.encounter_id "
        + "        WHERE  e.encounter_type IN( ${6}, ${9}, ${18}, ${53} ) "
        + "               AND o.concept_id = ${1190} "
        + "               AND e.location_id = :location "
        + "               AND o.value_datetime <= :endDate "
        + "               AND e.voided = 0 "
        + "               AND p.voided = 0 "
        + "        GROUP  BY p.patient_id "
        + "        UNION "
        + "        SELECT p.patient_id, "
        + "               ps.start_date AS art_date "
        + "        FROM   patient p "
        + "               INNER JOIN patient_program pg "
        + "                       ON p.patient_id = pg.patient_id "
        + "               INNER JOIN patient_state ps "
        + "                       ON pg.patient_program_id = ps.patient_program_id "
        + "        WHERE  pg.location_id = :location "
        + "               AND pg.program_id = ${2} "
        + "               AND ps.start_date <= :endDate "
        + "        UNION "
        + "        SELECT p.patient_id, "
        + "               Min(o.value_datetime) AS art_date "
        + "        FROM   patient p "
        + "               INNER JOIN encounter e "
        + "                       ON e.patient_id = p.patient_id "
        + "               INNER JOIN obs o "
        + "                       ON o.encounter_id = e.encounter_id "
        + "        WHERE  e.encounter_type = ${52} "
        + "               AND o.concept_id = ${23866} "
        + "               AND o.value_datetime <= :endDate "
        + "               AND o.voided = 0 "
        + "               AND e.location_id = :location "
        + "               AND e.voided = 0 "
        + "               AND p.voided = 0 "
        + "        GROUP  BY p.patient_id) art "
        + "GROUP  BY art.patient_id";
  }

  /**
   * <b>NEW_FR4.1: Patient’s earliest ART start date from pick-up and clinical sources </b>
   *
   * <p>The system will identify the patient ART start date by selecting the earliest date from the
   * following sources:
   *
   * <ul>
   *   <li>First ever drug pick up date registered on (FILA)
   *   <li>Drug initiation date (ARV PLAN = START DRUGS) during the pharmacy or clinical visits
   *       First start drugs date set in in clinical tools (Ficha de Seguimento Adulto and Ficha de
   *       Seguimento Pediatria) or Ficha Resumo - Master Card.
   *   <li>Date of enrollment in ART Program
   *   <li>First ever drug pick-up date registered on (Recepção Levantou ARV) – Master Card
   * </ul>
   *
   * <p><b>Note: </b>Ensure that the ART start date is truly the first occurrence ever for the
   * patient. This is particularly important for patients that have different ART start dates
   * registered in the system.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsStartedArtClinicalAndPickUpSources() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patient’s earliest ART start date from pick-up and clinical sources");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("1255", hivMetadata.getARVPlanConcept().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());

    String query =
        "       SELECT patient_id "
            + " FROM ( "
            + getPatientsStartedArtClinicalAndPickUp()
            + "       ) start "
            + " WHERE start.min_art_date <= '2023-12-21' ";

    StringSubstitutor sb = new StringSubstitutor(map);
    cd.setQuery(sb.replace(query));
    return cd;
  }

  /**
   * <b>Patients with an absolute CD4 result <200/mm3 registered in the following sources:</b>
   *
   * <ul>
   *   <li>CD4 absolute value at ART initiation marked on Ficha Resumo OR
   *   <li>Last CD4 absolute value marked on Ficha Resumo OR
   *   <li>CD4 absolute result marked in the Investigações - Resultados Laboratoriais section on
   *       Ficha Clínica OR
   *   <li>CD4 absolute result registered on the Lab Form OR
   *   <li>CD4 absolute result registered on the e-Lab Form
   * </ul>
   *
   * <p>The system will consider the oldest CD4 result date falling between patient ART Start Date -
   * 90 days and ART Start Date + 28 days from the different sources listed above for the evaluation
   * of the result (< 200).
   *
   * <p><b>Notes: </b>For the CD4 at ART initiation registered on Ficha Resumo, the “ART Start Date”
   * that is registered on the same Ficha Resumo will be considered as the CD4 result date. For
   * clients who have CD4 results ≥200/mm3 and <200/mm3 on the same, oldest date, the CD4 result
   * <200/mm3 will be prioritized.
   *
   * @param cd4CountComparison
   */
  public CohortDefinition getCd4Result(
      AdvancedDiseaseAndTBCascadeCohortQueries.Cd4CountComparison cd4CountComparison) {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Number of patientes who initiated TARV - Fila and ARV Pickup");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("23896", hivMetadata.getArtInitiationCd4Concept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON p.patient_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "       INNER JOIN ( "
            + "       SELECT start.patient_id, "
            + "              start.first_pickup AS first_pickup "
            + "             FROM ( "
            + getPatientsArtStartOnFilaOrArvPickup()
            + "       ) start "
            + " WHERE start.first_pickup >= '2023-12-21' "
            + "       ) art"
            + "               ON art.patient_id = p.patient_id "
            + "       INNER JOIN (SELECT oldest_cd4.patient_id, "
            + "                          Min(cd4_date) oldest_date "
            + "                   FROM   (SELECT e.patient_id, "
            + "                                  Date(e.encounter_datetime) cd4_date "
            + "                           FROM   encounter e "
            + "                                  INNER JOIN obs o "
            + "                                          ON o.encounter_id = e.encounter_id "
            + "                           WHERE  e.encounter_type IN ( ${6}, ${13}, ${51} ) "
            + "                                  AND e.location_id = :location "
            + "                                  AND Date(e.encounter_datetime) <= :endDate "
            + "                                  AND o.concept_id = ${1695} "
            + "                                  AND e.voided = 0 "
            + "                                  AND o.voided = 0 "
            + "                           UNION "
            + "                           SELECT e.patient_id, "
            + "                                  o.obs_datetime AS cd4_date "
            + "                           FROM   encounter e "
            + "                                  INNER JOIN obs o "
            + "                                          ON o.encounter_id = e.encounter_id "
            + "                           WHERE  e.encounter_type = ${53} "
            + "                                  AND e.location_id = :location "
            + "                                  AND e.voided = 0 "
            + "                                  AND o.voided = 0 "
            + "                                  AND o.concept_id = ${1695} "
            + "                                   OR o.concept_id = ${23896} "
            + "                                  AND o.obs_datetime <= :endDate) oldest_cd4 "
            + "                   GROUP  BY oldest_cd4.patient_id) cd4 "
            + "               ON cd4.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${1695} "
            + "       AND  ".concat(cd4CountComparison.getProposition())
            + "       AND ( ( Date(e.encounter_datetime) BETWEEN DATE_SUB(art.first_pickup, "
            + "                                                  INTERVAL 90 day) "
            + "                                          AND DATE_ADD(art.first_pickup, INTERVAL 28 day) "
            + "               AND e.encounter_type IN ( ${6}, ${13}, ${51} ) ) "
            + "              OR ( Date(o.obs_datetime) BETWEEN Date_sub(art.first_pickup, "
            + "                                                INTERVAL 90 day) "
            + "                                        AND Date_add(art.first_pickup, INTERVAL 28 day) "
            + "                   AND e.encounter_type = ${53} ) ) "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  /** <b> Patient Disaggregation- CD4 result <200/mm3 </b> */
  public CohortDefinition getCd4ResultLessThan200() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("CD4 Result");
    cd.addParameter(new Parameter("location", "Facility", Location.class));
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    cd.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    String mapping1 = "startDate=${startDate},endDate=${endDate},location=${location}";
    String mapping2 = "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}";

    CohortDefinition txnew = getTxNewCompositionCohort("patientEnrolledInART");

    CohortDefinition cd4Under200 =
        getCd4Result(
            AdvancedDiseaseAndTBCascadeCohortQueries.Cd4CountComparison.LessThanOrEqualTo200mm3);

    cd.addSearch("txnew", EptsReportUtils.map(txnew, mapping2));
    cd.addSearch("cd4Under200", EptsReportUtils.map(cd4Under200, mapping1));

    cd.setCompositionString("txnew AND cd4Under200 ");

    return cd;
  }

  /** <b>Patient Disaggregation- CD4 result ≥200/mm3 </b> */
  public CohortDefinition cd4ResultGreaterThan200() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("CD4 Result");
    cd.addParameter(new Parameter("location", "Facility", Location.class));
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    cd.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    String mapping1 = "startDate=${startDate},endDate=${endDate},location=${location}";
    String mapping2 = "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}";

    CohortDefinition txnew = getTxNewCompositionCohort("patientEnrolledInART");

    CohortDefinition cd4Above200 =
        getCd4Result(
            AdvancedDiseaseAndTBCascadeCohortQueries.Cd4CountComparison.GreaterThanOrEqualTo200mm3);

    cd.addSearch("txnew", EptsReportUtils.map(txnew, mapping2));

    cd.addSearch("cd4Above200", EptsReportUtils.map(cd4Above200, mapping1));

    cd.setCompositionString("txnew AND cd4Above200");

    return cd;
  }

  /** <b>Patient Disaggregation- Unknown CD4</b> */
  public CohortDefinition unknownCd4Result() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("CD4 Result");
    cd.addParameter(new Parameter("location", "Facility", Location.class));
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    cd.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    String mapping1 = "startDate=${startDate},endDate=${endDate},location=${location}";
    String mapping2 = "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}";

    CohortDefinition txnew = getTxNewCompositionCohort("patientEnrolledInART");

    CohortDefinition cd4Under200 =
        getCd4Result(
            AdvancedDiseaseAndTBCascadeCohortQueries.Cd4CountComparison.LessThanOrEqualTo200mm3);

    CohortDefinition cd4Above200 =
        getCd4Result(
            AdvancedDiseaseAndTBCascadeCohortQueries.Cd4CountComparison.GreaterThanOrEqualTo200mm3);

    cd.addSearch("txnew", EptsReportUtils.map(txnew, mapping2));
    cd.addSearch("cd4Under200", EptsReportUtils.map(cd4Under200, mapping1));
    cd.addSearch("cd4Above200", EptsReportUtils.map(cd4Above200, mapping1));

    cd.setCompositionString("txnew AND NOT (cd4Under200 OR cd4Above200)");

    return cd;
  }
}
