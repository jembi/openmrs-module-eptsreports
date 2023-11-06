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
   * @param
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
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());

    String query =
        "       SELECT patient_id "
            + " FROM ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "       ) start "
            + " WHERE start.first_pickup >= '2023-12-21' ";

    StringSubstitutor sb = new StringSubstitutor(map);
    cd.setQuery(sb.replace(query));
    return cd;
  }

  public CohortDefinition getCd4Result(
      AdvancedDiseaseAndTBCascadeCohortQueries.Cd4CountComparison cd4CountComparison) {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Number of patientes who initiated TARV - Fila and ARV Pickup");
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
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
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
            + "                                  AND o.value_numeric IS NOT NULL "
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
            + "                                  AND o.value_numeric IS NOT NULL "
            + "                                      AND o.obs_datetime <= :endDate) oldest_cd4 "
            + "                   GROUP  BY oldest_cd4.patient_id) cd4 "
            + "               ON cd4.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${1695} "
            + "       AND  "
                .concat(cd4CountComparison.getProposition())
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

    System.out.println(sb.replace(query));

    return cd;
  }

  public CohortDefinition getCd4ResultLessThan200() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("CD4 Result");
    cd.addParameter(new Parameter("location", "Facility", Location.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));

    String mapping1 = "endDate=${endDate},location=${location}";

    CohortDefinition txnew = getPatientsStartedArtOnFilaOrArvPickupDuringThePeriod();

    CohortDefinition cd200AgeFiveOrOver =
        getCd4Result(
            AdvancedDiseaseAndTBCascadeCohortQueries.Cd4CountComparison.LessThanOrEqualTo200mm3);

    cd.addSearch("txnew", EptsReportUtils.map(txnew, mapping1));
    cd.addSearch("cd4Under200", EptsReportUtils.map(cd200AgeFiveOrOver, mapping1));

    cd.setCompositionString("txnew AND cd4Under200 ");

    return cd;
  }

  public CohortDefinition cd4ResultGreaterThan200() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("CD4 Result");
    cd.addParameter(new Parameter("location", "Facility", Location.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));

    String mapping1 = "endDate=${endDate},location=${location}";

    CohortDefinition txnew = getPatientsStartedArtOnFilaOrArvPickupDuringThePeriod();

    CohortDefinition cd200AgeFiveOrOver =
        getCd4Result(
            AdvancedDiseaseAndTBCascadeCohortQueries.Cd4CountComparison.GreaterThanOrEqualTo200mm3);

    cd.addSearch("txnew", EptsReportUtils.map(txnew, mapping1));

    cd.addSearch("cd4Above200", EptsReportUtils.map(cd200AgeFiveOrOver, mapping1));

    cd.setCompositionString("txnew AND cd4Above200");

    return cd;
  }

  public CohortDefinition unknownCd4Result() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("CD4 Result");
    cd.addParameter(new Parameter("location", "Facility", Location.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));

    String mapping1 = "endDate=${endDate},location=${location}";

    CohortDefinition txnew = getPatientsStartedArtOnFilaOrArvPickupDuringThePeriod();

    CohortDefinition cd4Under200 =
        getCd4Result(
            AdvancedDiseaseAndTBCascadeCohortQueries.Cd4CountComparison.LessThanOrEqualTo200mm3);

    CohortDefinition cd4Above200 =
        getCd4Result(
            AdvancedDiseaseAndTBCascadeCohortQueries.Cd4CountComparison.GreaterThanOrEqualTo200mm3);

    cd.addSearch("txnew", EptsReportUtils.map(txnew, mapping1));
    cd.addSearch("cd4Under200", EptsReportUtils.map(cd4Under200, mapping1));
    cd.addSearch("cd4Above200", EptsReportUtils.map(cd4Above200, mapping1));

    cd.setCompositionString("txnew AND NOT (cd4Under200 OR cd4Above200)");

    return cd;
  }
}
