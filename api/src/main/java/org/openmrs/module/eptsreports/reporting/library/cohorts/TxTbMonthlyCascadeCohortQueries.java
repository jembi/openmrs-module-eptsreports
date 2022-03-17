package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.*;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TxTbMonthlyCascadeCohortQueries {

  @Autowired private IntensiveMonitoringCohortQueries intensiveMonitoringCohortQueries;

  @Autowired private TxCurrCohortQueries txCurrCohortQueries;

  @Autowired private TXTBCohortQueries txtbCohortQueries;

  @Autowired private HivMetadata hivMetadata;
  @Autowired private TbMetadata tbMetadata;

  public CohortDefinition getTxCurrOrTxCurrWithClinicalConsultation(
      Indicator1and2Composition indicator1and2Composition) {
    CompositionCohortDefinition chd = new CompositionCohortDefinition();
    chd.addParameter(new Parameter("startDate", "startDate", Date.class));
    chd.addParameter(new Parameter("endDate", "endDate", Date.class));
    chd.addParameter(new Parameter("location", "location", Location.class));
    chd.setName(indicator1and2Composition.getName());

    chd.addSearch(
        Indicator1and2Composition.TXCURR.getKey(),
        EptsReportUtils.map(
            txCurrCohortQueries.getTxCurrCompositionCohort("tx_curr", true),
            "onOrBefore=${endDate},location=${location}"));

    chd.addSearch(
        Indicator1and2Composition.TXCURR_AND_CLINICAL_CONSULTATION.getKey(),
        EptsReportUtils.map(
            getPatientsWithClinicalConsultationInLast6Months(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    chd.setCompositionString(indicator1and2Composition.getCompositionString());
    return chd;
  }

  public CohortDefinition getPatientsNewOnArt() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Patients New on ART (A)");
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition startedArtLast6Months = getPatientsStartedArtLast6MonthsFromEndDate();
    CohortDefinition transferredFromProgram = getPatientsTransferredInFromProgram();
    CohortDefinition transferredFromFichaResumo = getPatientsTransferredInFromFichaResumo();

    cd.addSearch(
        "startedArtLast6Months",
        EptsReportUtils.map(startedArtLast6Months, "endDate=${endDate},location=${location}"));
    cd.addSearch(
        "transferredFromProgram",
        EptsReportUtils.map(transferredFromProgram, "endDate=${endDate},location=${location}"));
    cd.addSearch(
        "transferredFromFichaResumo",
        EptsReportUtils.map(transferredFromFichaResumo, "endDate=${endDate},location=${location}"));

    cd.setCompositionString(
        "startedArtLast6Months AND NOT(transferredFromProgram OR transferredFromFichaResumo)");
    return cd;
  }

  public CohortDefinition getSmearMicroscopy() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Smear Microscopy");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition exameBaciloscopia = getPatientsWithBaciloscopiaResult();
    CohortDefinition haveBKTestRequest = getPatientsHaveBKTestRequest();
    CohortDefinition haveBKTestResult = getPatientsHaveBKTestResult();
    CohortDefinition dontHaveGENEXPERTInLabForm = getPatientsHaveGENEXPERTResultInLaboratotyForm();
    CohortDefinition dontHaveGeneXpertPositive = getPatientsDontHaveGeneXpert();
    CohortDefinition dontHaveApplication4LabResearch = getPatientsDontHaveApplication4LabResearch();

    cd.addSearch(
        "exameBaciloscopia",
        EptsReportUtils.map(
            exameBaciloscopia, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "haveBKTestRequest",
        EptsReportUtils.map(
            haveBKTestRequest, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "haveBKTestResult",
        EptsReportUtils.map(
            haveBKTestResult, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "dontHaveGENEXPERTInLabForm",
        EptsReportUtils.map(
            dontHaveGENEXPERTInLabForm,
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "dontHaveGeneXpertPositive",
        EptsReportUtils.map(
            dontHaveGeneXpertPositive,
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "dontHaveApplication4LabResearch",
        EptsReportUtils.map(
            dontHaveApplication4LabResearch,
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString(
        "exameBaciloscopia OR haveBKTestRequest OR (haveBKTestResult AND dontHaveGENEXPERTInLabForm) AND dontHaveGeneXpertPositive AND dontHaveApplication4LabResearch ");

    return cd;
  }

  public CohortDefinition getPatientsWithClinicalConsultationInLast6Months() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("With clinical consultation in last 6 months ");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND e.encounter_type = ${6} "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate"
            + "GROUP BY p.patient_id";
    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getPatientsStartedArtLast6MonthsFromEndDate() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients New on ART  ");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    String patientsOnArtQuery = getPatientsOnArt(true);

    String query =
        "SELECT patient_id "
            + "FROM   ( "
            + patientsOnArtQuery
            + " ) new_art "
            + "WHERE  new_art.art_date BETWEEN Date_add(:endDate, INTERVAL -6 month) AND :endDate";

    sqlCohortDefinition.setQuery(query);

    return sqlCohortDefinition;
  }

  public CohortDefinition getPatientsPreviouslyOnArt() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Patients Previously on ART (B)");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Date.class));

    CohortDefinition onArtBeforeEndDate = getPatientsOnArtBeforeEndDate();
    CohortDefinition newOnArt = getPatientsNewOnArt();
    cd.addSearch(
        "newOnArt", EptsReportUtils.map(newOnArt, "endDate=${endDate},location=${location}"));
    cd.addSearch(
        "onArtBeforeEndDate",
        EptsReportUtils.map(onArtBeforeEndDate, "endDate=${endDate},location=${location}"));
    cd.setCompositionString("onArtBeforeEndDate AND NOT newOnArt");
    return cd;
  }

  public CohortDefinition getPatientsTransferredInFromProgram() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Transferred-in from patient program");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("2", hivMetadata.getARTProgram().getProgramId());

    String query =
        ""
            + "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN patient_program pg "
            + "               ON pg.patient_id = p.patient_id "
            + "       INNER JOIN patient_state ps "
            + "               ON ps.patient_program_id = pg.patient_program_id "
            + "WHERE  p.voided = 0 "
            + "       AND pg.voided = 0 "
            + "       AND ps.voided = 0 "
            + "       AND pg.program_id = ${2} "
            + "       AND pg.location_id = :location "
            + "       AND ps.state = 29 "
            + "       AND ps.start_date <= :endDate ";

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));
    return sqlCohortDefinition;
  }

  public CohortDefinition getPatientsTransferredInFromFichaResumo() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Transferred-in from patient program");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1369", hivMetadata.getTransferredFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "       INNER JOIN obs o2 "
            + "               ON o2.encounter_id = e.encounter_id "
            + "       INNER JOIN obs o3 "
            + "               ON o3.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND o2.voided = 0 "
            + "       AND o3.voided = 0 "
            + "       AND e.encounter_type = ${53} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${1369} "
            + "       AND o.value_coded = ${1065} "
            + "       AND o.obs_datetime <= :endDate "
            + "       AND o2.concept_id = ${6300} "
            + "       AND o2.value_coded = ${6276} "
            + "       AND o.obs_datetime <> o2.obs_datetime "
            + "       AND o3.concept_id = ${23891} "
            + "       AND o3.value_datetime <= :endDate "
            + "GROUP  BY patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));
    return sqlCohortDefinition;
  }

  public CohortDefinition getPatientsOnArtBeforeEndDate() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients Previously on ART - Before End Date");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    String patientsOnArt = getPatientsOnArt(false);
    sqlCohortDefinition.setQuery(patientsOnArt);
    return sqlCohortDefinition;
  }

  /**
   * Encounter Type ID = 13 EXAME BACILOSCOPIA (concept id 307) Answers Value_coded (concept id in
   * [664, 703]) encounter_datetime >= startDate and <=endDate
   *
   * @return CortDefinition
   */
  public CohortDefinition getPatientsWithBaciloscopiaResult() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "Have any value registered for resultado baciloscopia in the laboratory form");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("307", hivMetadata.getResultForBasiloscopia().getConceptId());
    map.put("664", tbMetadata.getNegativeConcept().getConceptId());
    map.put("703", tbMetadata.getPositiveConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0"
            + "       AND e.encounter_type = ${13} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${307} "
            + "       AND o.value_coded IN ( ${664}, ${703} ) "
            + "       AND e.encounter_datetime >= :startDate "
            + "       AND e.encounter_datetime <= :endDate "
            + " GROUP BY p.patient_id ";
    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));
    return sqlCohortDefinition;
  }

  /**
   * have a ‘BK Test” request registered in the investigacoes –ficha clinica – mastercard; Encounter
   * Type ID = 6 APPLICATION FOR LABORATORY RESEARCH (concept id 23722) Answers: BK Test (concept id
   * 307) encounter_datetime >= startDate and <=endDate
   *
   * @return
   */
  public CohortDefinition getPatientsHaveBKTestRequest() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Have a BK Test request registered in the investigaco");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("307", hivMetadata.getResultForBasiloscopia().getConceptId());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());
    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0"
            + "       AND e.encounter_type = ${6} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${23722} "
            + "       AND o.value_coded = ${307} "
            + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + " GROUP BY p.patient_id ";
    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));
    return sqlCohortDefinition;
  }

  /**
   * Have a BK Test result registered in the investigacoes – resultados laboratoriais ficha clinica
   * – mastercard Encounter Type ID = 6 BK TEST (concept id 307) Answer Positive (concept id 703) or
   * Negative (concept id 664) encounter_datetime >= startDate and <=endDate
   *
   * @return
   */
  public CohortDefinition getPatientsHaveBKTestResult() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Have a BK Test result registered in the investigacoes");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("307", hivMetadata.getResultForBasiloscopia().getConceptId());
    map.put("703", tbMetadata.getPositiveConcept().getConceptId());
    map.put("664", tbMetadata.getNegativeConcept().getConceptId());
    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${6} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${307} "
            + "       AND o.value_coded IN(${703}, ${664}) "
            + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + " GROUP BY p.patient_id ";

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));
    return sqlCohortDefinition;
  }

  /**
   * Do not have a GENEXPERT Result registered in the laboratory form Encounter Type ID = 13 Teste
   * TB GENEXPERT (concept id 23723) Value_coded (664 – Negative, 703 – Positive) encounter_datetime
   * >= startDate and <=endDate
   *
   * @return
   */
  public CohortDefinition getPatientsHaveGENEXPERTResultInLaboratotyForm() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Do not have a GENEXPERT Result registered in the laboratory form");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("307", hivMetadata.getResultForBasiloscopia().getConceptId());
    map.put("23723", tbMetadata.getTBGenexpertTestConcept().getConceptId());
    map.put("703", tbMetadata.getPositiveConcept().getConceptId());
    map.put("664", tbMetadata.getNegativeConcept().getConceptId());
    String query =
        ""
            + "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${13} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${23723} "
            + "       AND o.value_coded  IN (${703}, ${664}) "
            + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate"
            + " GROUP BY p.patient_id ";
    ;

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));
    return sqlCohortDefinition;
  }

  /**
   * Do not have GeneXpert( concept id 23723) with any value coded Positive (concept id not in 703)
   * or Negative (concept id not in 664) registered in Ficha Clínica-Mastercard (encounter type 6,
   * encounter_datetime >= startDate and <=endDate )
   *
   * @return
   */
  public CohortDefinition getPatientsDontHaveGeneXpert() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "Do not have GeneXpert( concept id 23723) with any value coded Positive or Negative");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("23723", tbMetadata.getTBGenexpertTestConcept().getConceptId());
    map.put("703", tbMetadata.getPositiveConcept().getConceptId());
    map.put("664", tbMetadata.getNegativeConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided =0 "
            + "       AND e.encounter_type = ${6} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${23723} "
            + "       AND o.value_coded  NOT IN(${703}, ${664}) "
            + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + " GROUP BY p.patient_id ";
    ;

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));
    return sqlCohortDefinition;
  }

  /**
   * Do no have APPLICATION FOR LABORATORY RESEARCH (concept id 23722) value coded GeneXpert(
   * concept id not in 23723) in Ficha Clínica-Mastercard (encounter type 6, encounter_datetime >=
   * startDate and <=endDate)
   *
   * @return
   */
  public CohortDefinition getPatientsDontHaveApplication4LabResearch() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Do no have APPLICATION FOR LABORATORY RESEARCH");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("23723", tbMetadata.getTBGenexpertTestConcept().getConceptId());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${6} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${23722} "
            + "       AND o.value_coded <> ${23723} "
            + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + " GROUP BY p.patient_id ";

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));
    return sqlCohortDefinition;
  }

  /**
   * have a ‘GeneXpert Positivo ’ registered in the investigações – resultados laboratoriais - ficha
   * clínica – mastercard: * Encounter Type ID = 6 ● TB GENEXPERT TEST (concept id 23723) value
   * coded Positive (concept id 703) ● encounter_datetime >= startDate and <=endDate; or ■ have a
   * ‘GeneXpert Negativo ’ registered in the investigações – resultados laboratoriais - ficha
   * clínica – mastercard: ● Encounter Type ID = 6 ● TB GENEXPERT TEST (concept id 23723) Answer
   * Negative (concept id 664) ● encounter_datetime >= startDate and <=endDate; or ■ Have a
   * GeneXpert request registered in the investigacoes –ficha clinica – mastercard; ● Encounter Type
   * ID = 6 ● APPLICATION FOR LABORATORY RESEARCH (concept id 23722) value coded TB GENEXPERT TEST
   * (concept id 23723) ● encounter_datetime >= startDate and <=endDate; or ■ have a GENEXPERT
   * Result registered in the laboratory form ● Encounter Type ID = 13 ● Teste TB GENEXPERT (concept
   * id 23723) Value_coded (concept id 664 – Negative, concept id 703 – Positive) ●
   * encounter_datetime >= startDate and <=endDate ■ have a XpertMTB Result registered in the
   * laboratory form ● Encounter Type ID = 13 ● Teste XpertMTB (concept id 165189) Value_coded
   * (concept id 1065 – yes, concept id 1066 – no) ● encounter_datetime >= startDate and
   *
   * @return
   */
  public CohortDefinition getPatientsGeneXpertMtbRif() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("GeneXpert MTB/RIF");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("23723", tbMetadata.getTBGenexpertTestConcept().getConceptId());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("703", tbMetadata.getPositiveConcept().getConceptId());
    map.put("664", tbMetadata.getNegativeConcept().getConceptId());
    map.put("165189", tbMetadata.getTestXpertMtbUuidConcept().getConceptId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${6} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${23723} "
            + "       AND o.value_coded = ${703} "
            + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "GROUP  BY p.patient_id "
            + "UNION "
            + "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${6} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${23723} "
            + "       AND o.value_coded = ${664} "
            + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "GROUP  BY p.patient_id "
            + "UNION "
            + "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${6} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${23722} "
            + "       AND o.value_coded = ${23723} "
            + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "GROUP  BY p.patient_id "
            + "UNION "
            + "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${13} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${23723} "
            + "       AND o.value_coded IN ( ${664}, ${703} ) "
            + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "GROUP  BY p.patient_id "
            + "UNION "
            + "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${13} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = 165189 "
            + "       AND o.value_coded IN( ${1065}, ${1066} ) "
            + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));
    return sqlCohortDefinition;
  }

  public CohortDefinition getPatientsHaveTBLAM() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("GeneXpert MTB/RIF");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("23723", tbMetadata.getTBGenexpertTestConcept().getConceptId());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("703", tbMetadata.getPositiveConcept().getConceptId());
    map.put("664", tbMetadata.getNegativeConcept().getConceptId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("23951", tbMetadata.getTestTBLAM().getConceptId());
    map.put("1138", tbMetadata.getIndeterminate().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${6} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${23722} "
            + "       AND o.value_coded = ${23951} "
            + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "GROUP  BY p.patient_id "
            + "UNION "
            + "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${6} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${23951} "
            + "       AND o.value_coded IN ( ${703}, ${664} ) "
            + "       AND e.encounter_datetime BETWEEN :startDate AND endDate "
            + "GROUP  BY p.patient_id "
            + "UNION "
            + "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${13} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${23951} "
            + "       AND o.value_coded IN ( ${703}, ${664}, ${1138} ) "
            + "       AND e.encounter_datetime BETWEEN :startDate AND :endDatse "
            + "GROUP  BY p.patient_id";
    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));
    return sqlCohortDefinition;
  }

  private String getPatientsOnArt(boolean selectArtDate) {
    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());
    valuesMap.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    valuesMap.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    valuesMap.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    valuesMap.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    valuesMap.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    valuesMap.put("1255", hivMetadata.getARVPlanConcept().getConceptId());
    valuesMap.put("1410", hivMetadata.getReturnVisitDateConcept().getConceptId());
    valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    valuesMap.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());
    valuesMap.put("1256", hivMetadata.getStartDrugs().getConceptId());
    valuesMap.put("2", hivMetadata.getARTProgram().getProgramId());

    String fromSql =
        "        FROM   (SELECT p.patient_id, "
            + "                       Min(e.encounter_datetime) art_date "
            + "                FROM   patient p "
            + "                       INNER JOIN encounter e "
            + "                               ON e.patient_id = p.patient_id "
            + "                WHERE  e.encounter_type = ${18} "
            + "                       AND e.encounter_datetime <= :endDate "
            + "                       AND e.voided = 0 "
            + "                       AND p.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                GROUP  BY p.patient_id "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       Min(e.encounter_datetime) art_date "
            + "                FROM   patient p "
            + "                       INNER JOIN encounter e "
            + "                               ON p.patient_id = e.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON e.encounter_id = o.encounter_id "
            + "                WHERE  p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND e.encounter_type IN ( ${6}, ${9}, ${18} ) "
            + "                       AND o.concept_id = ${1255} "
            + "                       AND o.value_coded = ${1256} "
            + "                       AND e.encounter_datetime <= :endDate "
            + "                       AND e.location_id = :location "
            + "                GROUP  BY p.patient_id "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       historical.min_date AS art_date "
            + "                FROM   patient p "
            + "                       INNER JOIN encounter e "
            + "                               ON e.patient_id = p.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON o.encounter_id = e.encounter_id "
            + "                       INNER JOIN(SELECT p.patient_id, "
            + "                                         e.encounter_id, "
            + "                                         Min(o.value_datetime) min_date "
            + "                                  FROM   patient p "
            + "                                         INNER JOIN encounter e "
            + "                                                 ON e.patient_id = p.patient_id "
            + "                                         INNER JOIN obs o "
            + "                                                 ON o.encounter_id = "
            + "                                                    e.encounter_id "
            + "                                  WHERE  e.encounter_type IN( ${6}, ${9}, ${18}, ${53} ) "
            + "                                         AND o.concept_id = ${1190} "
            + "                                         AND e.location_id = :location "
            + "                                         AND o.value_datetime <= :endDate "
            + "                                         AND e.voided = 0 "
            + "                                         AND p.voided = 0 "
            + "                                  GROUP  BY p.patient_id) historical "
            + "                               ON historical.patient_id = p.patient_id "
            + "                WHERE  e.encounter_type IN( ${6}, ${9}, ${18}, ${53} ) "
            + "                       AND o.concept_id = ${1190} "
            + "                       AND e.location_id = :location "
            + "                       AND o.value_datetime <= :endDate "
            + "                       AND e.voided = 0 "
            + "                       AND p.voided = 0 "
            + "                       AND historical.encounter_id = e.encounter_id "
            + "                       AND o.value_datetime = historical.min_date "
            + "                GROUP  BY p.patient_id "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       ps.start_date AS art_date "
            + "                FROM   patient p "
            + "                       INNER JOIN patient_program pg "
            + "                               ON p.patient_id = pg.patient_id "
            + "                       INNER JOIN patient_state ps "
            + "                               ON pg.patient_program_id = ps.patient_program_id "
            + "                WHERE  pg.location_id = :location "
            + "                       AND pg.voided = 0 "
            + "                       AND pg.program_id = ${2} "
            + "                       AND pg.date_enrolled <= :endDate "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       Min(o.value_datetime) AS art_date "
            + "                FROM   patient p "
            + "                       INNER JOIN encounter e "
            + "                               ON e.patient_id = p.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON o.encounter_id = e.encounter_id "
            + "                       INNER JOIN obs oyes "
            + "                               ON oyes.encounter_id = e.encounter_id "
            + "                                  AND o.person_id = oyes.person_id "
            + "                WHERE  e.encounter_type = ${52} "
            + "                       AND o.concept_id = ${23866} "
            + "                       AND o.value_datetime <= :endDate "
            + "                       AND o.voided = 0 "
            + "                       AND oyes.concept_id = ${23865} "
            + "                       AND oyes.value_coded = ${1065} "
            + "                       AND oyes.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                       AND e.voided = 0 "
            + "                       AND p.voided = 0 "
            + "                GROUP  BY p.patient_id) art "
            + "        GROUP  BY art.patient_id";
    String query =
        selectArtDate
            ? "SELECT art.patient_id, Min(art.art_date) art_date ".concat(fromSql)
            : "SELECT art.patient_id ".concat(fromSql);

    StringSubstitutor sb = new StringSubstitutor(valuesMap);

    return sb.replace(query);
  }

  public enum Indicator1and2Composition {
    TXCURR {
      @Override
      public String getKey() {
        return "TXCURR";
      }

      @Override
      public String getCompositionString() {
        return getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    TXCURR_AND_CLINICAL_CONSULTATION {
      @Override
      public String getKey() {
        return "CLINICAL";
      }

      @Override
      public String getCompositionString() {
        return TXCURR.getKey() + " AND " + getKey();
      }

      @Override
      public String getName() {
        return "TX_CURR with clinical consultation in last 6 months ";
      }
    };

    public abstract String getKey();

    public abstract String getCompositionString();

    public abstract String getName();
  }

  private List<Parameter> getParameters() {
    return Arrays.asList(
        new Parameter("startDate", "startDate", Date.class),
        new Parameter("endDate", "endDate", Date.class),
        new Parameter("location", "location", Date.class));
  }
}
