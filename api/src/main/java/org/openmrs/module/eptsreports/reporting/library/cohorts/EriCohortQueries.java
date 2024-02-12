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
public class EriCohortQueries {

  @Autowired private TxNewCohortQueries txNewCohortQueries;

  @Autowired private GenericCohortQueries genericCohortQueries;

  @Autowired private CommonCohortQueries commonCohortQueries;

  @Autowired private ResumoMensalCohortQueries resumoMensalCohortQueries;

  @Autowired private HivMetadata hivMetadata;

  /**
   * Get all patients who initiated ART 2 months from ART initiation less transfer ins return the
   * patient who initiated ART A and B
   *
   * @return CohortDefinition
   */
  public CohortDefinition getAllPatientsWhoInitiatedArt() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("All patients who initiated ART less transfer ins");
    cd.addParameter(new Parameter("cohortStartDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("cohortEndDate", "End Date", Date.class));
    cd.addParameter(new Parameter("reportingEndDate", "Reporting End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition startedArtOnPeriod = getPatientsArtStartDate();

    CohortDefinition transferIns = getPatientWhoHaveBeenTransferredIn();

    String artMappings =
        "startDate=${cohortStartDate},endDate=${cohortEndDate},location=${location}";
    cd.addSearch("initiatedArt", EptsReportUtils.map(startedArtOnPeriod, artMappings));

    String transferInMappings = "onOrBefore=${reportingEndDate},location=${location}";
    cd.addSearch("transferIns", EptsReportUtils.map(transferIns, transferInMappings));

    cd.setCompositionString("initiatedArt AND NOT transferIns");

    return cd;
  }

  /**
   * Get all patients who initiated ART 2 months from ART initiation less transferredIn between
   * cohort startDate and reportingEndDate
   *
   * @return CohortDefinition
   */
  public CohortDefinition getAllPatientsWhoInitiatedArtNOTTransferredInBeforeReportingEndDate() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("All patients who initiated ART less transfer ins");
    cd.addParameter(new Parameter("cohortStartDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("cohortEndDate", "End Date", Date.class));
    cd.addParameter(new Parameter("reportingEndDate", "Reporting End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition startedArtOnPeriod = genericCohortQueries.getStartedArtOnPeriod(false, true);
    CohortDefinition transferIns = commonCohortQueries.getMohTransferredInPatients();

    String mappings =
        "onOrAfter=${cohortStartDate},onOrBefore=${cohortEndDate},location=${location}";
    cd.addSearch("initiatedArt", EptsReportUtils.map(startedArtOnPeriod, mappings));

    String transferInMappings =
        "onOrAfter=${cohortStartDate},onOrBefore=${reportingEndDate},location=${location}";
    cd.addSearch("transferIns", EptsReportUtils.map(transferIns, transferInMappings));

    cd.setCompositionString("initiatedArt AND NOT transferIns");

    return cd;
  }

  /**
   * Get pregnant women who have more than 2 months retention on ART
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPregnantWomenRetainedOnArt() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Pregnant women retain on ART for more than 2 months from ART initiation date");
    cd.addParameter(new Parameter("cohortStartDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("cohortEndDate", "End Date", Date.class));
    cd.addParameter(new Parameter("reportingEndDate", "Reporting End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addSearch(
        "initiatedART",
        EptsReportUtils.map(
            getAllPatientsWhoInitiatedArt(),
            "cohortStartDate=${cohortStartDate},cohortEndDate=${cohortEndDate},reportingEndDate=${reportingEndDate},location=${location}"));
    cd.addSearch(
        "pregnant",
        EptsReportUtils.map(
            txNewCohortQueries.getPatientsPregnantEnrolledOnART(false),
            "startDate=${cohortStartDate},endDate=${cohortEndDate},location=${location}"));
    cd.setCompositionString("initiatedART AND pregnant");
    return cd;
  }

  /**
   * Get breastfeeding women who have more than 2 months ART retention
   *
   * @return CohortDefinition
   */
  public CohortDefinition getBreastfeedingWomenRetained() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Breastfeeding women retain on ART for more than 2 months from ART initiation date");
    cd.addParameter(new Parameter("cohortStartDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("cohortEndDate", "End Date", Date.class));
    cd.addParameter(new Parameter("reportingEndDate", "Reporting End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addSearch(
        "initiatedART",
        EptsReportUtils.map(
            getAllPatientsWhoInitiatedArt(),
            "cohortStartDate=${cohortStartDate},cohortEndDate=${cohortEndDate},reportingEndDate=${reportingEndDate},location=${location}"));
    cd.addSearch(
        "breastfeeding",
        EptsReportUtils.map(
            txNewCohortQueries.getTxNewBreastfeedingComposition(false),
            "onOrAfter=${cohortStartDate},onOrBefore=${cohortEndDate},location=${location}"));
    cd.setCompositionString("initiatedART AND breastfeeding");
    return cd;
  }

  /**
   * Get Children (0-14, excluding pregnant and breastfeeding women)
   *
   * @return CohortDefinition
   */
  public CohortDefinition getChildrenRetained() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Children having ART retention for than 2 months");
    cd.addParameter(new Parameter("cohortStartDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("cohortEndDate", "End Date", Date.class));
    cd.addParameter(new Parameter("reportingEndDate", "Reporting End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addSearch(
        "initiatedART",
        EptsReportUtils.map(
            getAllPatientsWhoInitiatedArt(),
            "cohortStartDate=${cohortStartDate},cohortEndDate=${cohortEndDate},reportingEndDate=${reportingEndDate},location=${location}"));
    cd.addSearch(
        "children",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnArtStartDate(null, 14, false),
            "onOrBefore=${cohortEndDate},location=${location}"));
    cd.addSearch(
        "pregnant",
        EptsReportUtils.map(
            txNewCohortQueries.getPatientsPregnantEnrolledOnART(false),
            "startDate=${cohortStartDate},endDate=${cohortEndDate},location=${location}"));
    cd.addSearch(
        "breastfeeding",
        EptsReportUtils.map(
            txNewCohortQueries.getTxNewBreastfeedingComposition(false),
            "onOrAfter=${cohortStartDate},onOrBefore=${cohortEndDate},location=${location}"));
    cd.setCompositionString("(initiatedART AND children) AND NOT (pregnant OR breastfeeding)");
    return cd;
  }

  /**
   * Get Adults (15+, excluding pregnant and breastfeeding women)
   *
   * @return CohortDefinition
   */
  public CohortDefinition getAdultsRetained() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Adults having ART retention for than 2 months");
    cd.addParameter(new Parameter("cohortStartDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("cohortEndDate", "End Date", Date.class));
    cd.addParameter(new Parameter("reportingEndDate", "Reporting End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addSearch(
        "initiatedART",
        EptsReportUtils.map(
            getAllPatientsWhoInitiatedArt(),
            "cohortStartDate=${cohortStartDate},cohortEndDate=${cohortEndDate},reportingEndDate=${reportingEndDate},location=${location}"));
    cd.addSearch(
        "adults",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnArtStartDate(15, null, false),
            "onOrBefore=${cohortEndDate},location=${location}"));
    cd.addSearch(
        "pregnant",
        EptsReportUtils.map(
            txNewCohortQueries.getPatientsPregnantEnrolledOnART(false),
            "startDate=${cohortStartDate},endDate=${cohortEndDate},location=${location}"));
    cd.addSearch(
        "breastfeeding",
        EptsReportUtils.map(
            txNewCohortQueries.getTxNewBreastfeedingComposition(false),
            "onOrAfter=${cohortStartDate},onOrBefore=${cohortEndDate},location=${location}"));
    cd.setCompositionString("(initiatedART AND adults) AND NOT (pregnant OR breastfeeding)");
    return cd;
  }

  /**
   * <b>IM-ER4_FR6.1:</b> Patient’s earliest ART start date from pick-up and clinical sources
   *
   * <p>The system will identify the patient ART start date by selecting the earliest date from the
   * following sources:
   *
   * <ul>
   *   <li>First ever drug pick-up date registered on (FILA)
   *   <li>Drug initiation date (ARV PLAN = START DRUGS) during the pharmacy or clinical visits
   *   <li>First start drug date set in Clinical tools (Ficha de Seguimento Adulto and Ficha de
   *       Seguimento Pediatria) or Ficha Resumo - Master Card.
   *   <li>Date of enrollment in ART Program
   *   <li>First ever drug pick-up date registered on (Recepção Levantou ARV) – Master Card
   * </ul>
   *
   * <p><b>Note:</b>Ensure that the ART start date is truly the first occurrence ever for the
   * patient. This is particularly important for patients that have different ART start dates
   * registered in the system.
   */
  public CohortDefinition getPatientsArtStartDate() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patient’s earliest ART start date from pick-up and clinical sources");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CommonQueries commonQueries = new CommonQueries(new CommonMetadata(), new HivMetadata());

    String query =
        "       SELECT patient_id "
            + " FROM ( "
            + commonQueries.getARTStartDate(true)
            + "       ) start "
            + " WHERE start.first_pickup BETWEEN :startDate AND :endDate ";

    cd.setQuery(query);
    return cd;
  }

  /**
   * <b>IM-ER4_FR7:</b> Patients who have been transferred in – to be excluded
   *
   * <p>The system will identify patients who have been transferred in as follows:
   *
   * <ul>
   *   <li>All patients who are enrolled in ARV Program (Serviço TARV- Tratamento) and have as state
   *       of patient “Transfer from another facility” in the patient chart prior to the reporting
   *       end date
   *   <li>All patients who have filled “Transferido de outra US” and checked “Em TARV” in Ficha
   *       Resumo – Master Card, with MasterCard file opening Date prior to the reporting end date.
   * </ul>
   */
  public CohortDefinition getPatientWhoHaveBeenTransferredIn() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patients who have been transferred in");
    cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1369", hivMetadata.getTransferredFromOtherFacilityConcept().getConceptId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "           JOIN encounter e "
            + "                ON p.patient_id = e.patient_id "
            + "           JOIN obs transf "
            + "                ON transf.encounter_id = e.encounter_id "
            + "           JOIN obs type "
            + "                ON type.encounter_id = e.encounter_id "
            + "           JOIN obs opening "
            + "                ON opening.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND e.encounter_type = ${53} "
            + "  AND e.location_id = :location "
            + "  AND transf.voided = 0 "
            + "  AND transf.concept_id = ${1369} "
            + "  AND transf.value_coded = ${1065} "
            + "  AND type.voided = 0 "
            + "  AND type.concept_id = ${6300} "
            + "  AND type.value_coded = ${6276} "
            + "  AND opening.voided = 0 "
            + "  AND opening.concept_id = ${23891} "
            + "  AND opening.value_datetime <= :onOrBefore "
            + "UNION "
            + "SELECT p.patient_id "
            + "FROM   patient p "
            + "           JOIN patient_program pp "
            + "                ON p.patient_id = pp.patient_id "
            + "           JOIN patient_state ps "
            + "                ON pp.patient_program_id = ps.patient_program_id "
            + "WHERE  pp.voided = 0 "
            + "  AND ps.voided = 0 "
            + "  AND p.voided = 0 "
            + "  AND pp.program_id = ${2} "
            + "  AND ps.state = ${29} "
            + "  AND location_id = :location "
            + "  AND ps.start_date <= :onOrBefore ";

    StringSubstitutor sb = new StringSubstitutor(map);
    cd.setQuery(sb.replace(query));
    return cd;
  }
}
