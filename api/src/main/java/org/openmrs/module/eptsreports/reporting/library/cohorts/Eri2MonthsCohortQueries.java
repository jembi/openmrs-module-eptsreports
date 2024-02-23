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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.library.queries.CommonQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Eri2MonthsCohortQueries {

  @Autowired private HivMetadata hivMetadata;

  @Autowired private GenericCohortQueries genericCohortQueries;

  @Autowired private EriCohortQueries eriCohortQueries;

  @Autowired private HivCohortQueries hivCohortQueries;

  /**
   *
   *
   * <ul>
   *   <li>Patients who do not havea drug pick up (FILA or Recepção Levantou ARV – Master Card)
   *       during the following period:
   *       <p>>= ART Initiation date + 5 days
   *       <p><= ART Initiation date + 33 days
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getAllPatientsWhoHaveDrugsPickUpBetween5To33DaysOfArtInitiationDate() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patients who picked up drugs between 5 to 33 days after treatment start date");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());

    CommonQueries commonQueries = new CommonQueries(new CommonMetadata(), new HivMetadata());

    String query =
        "SELECT inicio_real.patient_id "
            + "FROM   ( "
            + commonQueries.getARTStartDate(true)
            + "        ) AS inicio_real "
            + "WHERE  inicio_real.first_pickup BETWEEN :startDate AND :endDate "
            + "  AND EXISTS (SELECT e.patient_id "
            + "              FROM   encounter e "
            + "                         INNER JOIN obs o "
            + "                                    ON e.encounter_id = o.encounter_id "
            + "              WHERE  e.patient_id = inicio_real.patient_id "
            + "                AND e.voided = 0 "
            + "                AND o.voided = 0 "
            + "                AND e.location_id = :location "
            + "                AND ( ( e.encounter_type = ${18} "
            + "                  AND e.encounter_datetime BETWEEN "
            + "                            inicio_real.first_pickup + INTERVAL 5 "
            + "                                DAY "
            + "                            AND "
            + "                            inicio_real.first_pickup + INTERVAL "
            + "                                33 "
            + "                                DAY ) "
            + "                  OR ( e.encounter_type = ${52} "
            + "                      AND o.concept_id = ${23866} "
            + "                      AND o.value_datetime BETWEEN "
            + "                           inicio_real.first_pickup "
            + "                               + "
            + "                           INTERVAL 5 "
            + "                               DAY "
            + "                           AND "
            + "                           inicio_real.first_pickup + INTERVAL "
            + "                               33 "
            + "                               DAY ) ))";

    StringSubstitutor sb = new StringSubstitutor(map);

    String replacedQuery = sb.replace(query);

    cd.setQuery(replacedQuery);

    return cd;
  }

  /**
   * A and B and C
   *
   * @return CohortDefinition
   */
  public CohortDefinition getAllPatientsWhoStartedArtAndPickedDrugsOnTheirNextVisit() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Patients who  picked up drugs during their second visit and had initiated ART");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("reportingEndDate", "Reporting End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addSearch(
        "initiatedArt",
        EptsReportUtils.map(
            eriCohortQueries.getAllPatientsWhoInitiatedArt(),
            "cohortStartDate=${startDate},cohortEndDate=${endDate},reportingEndDate=${reportingEndDate},location=${location}"));
    cd.addSearch(
        "pickedDrugs",
        EptsReportUtils.map(
            getAllPatientsWhoHaveDrugsPickUpBetween5To33DaysOfArtInitiationDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.setCompositionString("initiatedArt AND pickedDrugs");
    return cd;
  }

  /**
   * <b>ERM2M_FR7</b> Patients who are Alive & not Transferred-out and Did not pick up drugs (5-33
   * days)
   *
   * <p>The system will identify patients who are Alive & not Transferred-out and Did not pick up
   * drugs (5-33 days):
   *
   * <ul>
   *   <li>Patients who do not havea drug pick up (FILA or Recepção Levantou ARV – Master Card)
   *       during the following period:
   *       <ul>
   *         <p>>= ART Initiation date + 5 days
   *         <p><= ART Initiation date + 33 days
   *   </ul>
   * </ul>
   *
   * <p>The system will exclude the following patients:
   *
   * <ul>
   *   <li>Patients who are dead by the end of the reporting period (ERM2M_FR8).
   *   <li>Patients that are transferred out by the end of the reporting period (ERM2M_FR10).
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition
      getPatientsWhoAreAliveAndNotTransferredOutAndDidNotPickUpDrugsBetween5to33DaysAfterArtStartDate() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Patients who did not pick up drugs during their second visit");
    cd.addParameter(new Parameter("cohortStartDate", "Cohort Start Date", Date.class));
    cd.addParameter(new Parameter("cohortEndDate", "Cohort End Date", Date.class));
    cd.addParameter(new Parameter("reportingEndDate", "Reporting End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addSearch(
        "initiatedArt",
        EptsReportUtils.map(
            eriCohortQueries.getAllPatientsWhoInitiatedArt(),
            "cohortStartDate=${cohortStartDate},cohortEndDate=${cohortEndDate},reportingEndDate=${reportingEndDate},location=${location}"));
    cd.addSearch(
        "pickedDrugs",
        EptsReportUtils.map(
            getAllPatientsWhoHaveDrugsPickUpBetween5To33DaysOfArtInitiationDate(),
            "startDate=${cohortStartDate},endDate=${cohortEndDate},location=${location}"));
    cd.addSearch(
        "dead",
        EptsReportUtils.map(
            genericCohortQueries.getDeceasedPatients(),
            "onOrBefore=${reportingEndDate},location=${location}"));
    cd.addSearch(
        "transfers",
        EptsReportUtils.map(
            hivCohortQueries.getPatientsTransferredOut(),
            "onOrBefore=${reportingEndDate},location=${location}"));
    cd.setCompositionString("initiatedArt AND NOT (pickedDrugs OR dead OR transfers)");
    return cd;
  }

  /**
   * Get patients who picked up drugs on their second visit A and B and C and NOt (dead and
   * tarnsfers)
   *
   * @return CohortDefinition
   */
  public CohortDefinition
      getPatientsWhoAreAliveAndNoteTransferredOutAndPickedUpDrugsBetween5to33DaysAfterArtStartDate() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Patients who  picked up drugs during their second visit");
    cd.addParameter(new Parameter("cohortStartDate", "Cohort Start Date", Date.class));
    cd.addParameter(new Parameter("cohortEndDate", "Cohort End Date", Date.class));
    cd.addParameter(new Parameter("reportingEndDate", "Reporting End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addSearch(
        "pickedDrugsAndStartedART",
        EptsReportUtils.map(
            getAllPatientsWhoStartedArtAndPickedDrugsOnTheirNextVisit(),
            "startDate=${cohortStartDate},endDate=${cohortEndDate},reportingEndDate=${reportingEndDate},location=${location}"));
    cd.addSearch(
        "dead",
        EptsReportUtils.map(
            genericCohortQueries.getDeceasedPatients(),
            "onOrBefore=${reportingEndDate},location=${location}"));
    cd.addSearch(
        "transfers",
        EptsReportUtils.map(
            hivCohortQueries.getPatientsTransferredOut(),
            "onOrBefore=${reportingEndDate},location=${location}"));
    cd.setCompositionString("pickedDrugsAndStartedART AND NOT (dead OR transfers)");
    return cd;
  }

  /**
   * Get all patients who initiated ART(A), less transfer ins(B) intersected with those who picked
   * up drugs in 33 days AND who died within the reporting period
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoInitiatedArtAndDead() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Patients who died during period");
    cd.addParameter(new Parameter("cohortStartDate", "Cohort Start Date", Date.class));
    cd.addParameter(new Parameter("cohortEndDate", "Cohort End Date", Date.class));
    cd.addParameter(new Parameter("reportingEndDate", "Reporting End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addSearch(
        "initiatedArtAndNotTransferIns",
        EptsReportUtils.map(
            eriCohortQueries.getAllPatientsWhoInitiatedArt(),
            "cohortStartDate=${cohortStartDate},cohortEndDate=${cohortEndDate},reportingEndDate=${reportingEndDate},location=${location}"));
    cd.addSearch(
        "dead",
        EptsReportUtils.map(
            genericCohortQueries.getDeceasedPatients(),
            "onOrBefore=${reportingEndDate},location=${location}"));
    cd.setCompositionString("initiatedArtAndNotTransferIns AND dead");
    return cd;
  }

  /**
   * Get all patients who initiated ART(A), less transfer ins(B) intersected with those who picked
   * up drugs in 33 days AND those who suspended treatment within the reporting period
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoInitiatedArtButSuspendedTreatment() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Patients who suspended treatment");
    cd.addParameter(new Parameter("cohortStartDate", "Cohort Start Date", Date.class));
    cd.addParameter(new Parameter("cohortEndDate", "Cohort End Date", Date.class));
    cd.addParameter(new Parameter("reportingEndDate", "Reporting End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addSearch(
        "initiatedArtAndNotTransferIns",
        EptsReportUtils.map(
            eriCohortQueries.getAllPatientsWhoInitiatedArt(),
            "cohortStartDate=${cohortStartDate},cohortEndDate=${cohortEndDate},reportingEndDate=${reportingEndDate},location=${location}"));
    cd.addSearch(
        "suspendedTreatment",
        EptsReportUtils.map(
            genericCohortQueries.getPatientsBasedOnPatientStates(
                hivMetadata.getARTProgram().getProgramId(),
                hivMetadata.getSuspendedTreatmentWorkflowState().getProgramWorkflowStateId()),
            "startDate=${cohortStartDate},endDate=${reportingEndDate},location=${location}"));
    cd.setCompositionString("initiatedArtAndNotTransferIns AND suspendedTreatment");
    return cd;
  }

  /**
   * Get all patients who initiated ART(A), less transfer ins(B) intersected with those who picked
   * up drugs in 33 days AND those who suspended treatment within the reporting period
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoInitiatedArtButTransferredOut() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Patients who transferred out during period");
    cd.addParameter(new Parameter("cohortStartDate", "Cohort Start Date", Date.class));
    cd.addParameter(new Parameter("cohortEndDate", "Cohort End Date", Date.class));
    cd.addParameter(new Parameter("reportingEndDate", "Reporting End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addSearch(
        "initiatedArtAndNotTransferIns",
        EptsReportUtils.map(
            eriCohortQueries.getAllPatientsWhoInitiatedArt(),
            "cohortStartDate=${cohortStartDate},cohortEndDate=${cohortEndDate},reportingEndDate=${reportingEndDate},location=${location}"));
    cd.addSearch(
        "transferredOut",
        EptsReportUtils.map(
            hivCohortQueries.getPatientsTransferredOut(),
            "onOrBefore=${reportingEndDate},location=${location}"));
    cd.setCompositionString("initiatedArtAndNotTransferIns AND transferredOut");
    return cd;
  }
}
