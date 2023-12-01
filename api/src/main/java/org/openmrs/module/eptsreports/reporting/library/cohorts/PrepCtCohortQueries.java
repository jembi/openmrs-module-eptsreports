package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.library.queries.PrepCtQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PrepCtCohortQueries {

  private HivMetadata hivMetadata;
  private PrepNewCohortQueries prepNewCohortQueries;
  private CommonMetadata commonMetadata;
  private GenderCohortQueries genderCohortQueries;

  @Autowired
  public PrepCtCohortQueries(
      HivMetadata hivMetadata,
      PrepNewCohortQueries prepNewCohortQueries,
      CommonMetadata commonMetadata,
      GenderCohortQueries genderCohortQueries) {
    this.hivMetadata = hivMetadata;
    this.prepNewCohortQueries = prepNewCohortQueries;
    this.commonMetadata = commonMetadata;
    this.genderCohortQueries = genderCohortQueries;
  }

  public CohortDefinition getPREPCTNumerator() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Patients on Prep CT Numerator");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "A",
        EptsReportUtils.map(
            getPatientsListInitiatedOnPREP(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "B",
        EptsReportUtils.map(
            getPatientsTransferredInFromAnotherHFByEndOfPreviousReportingPeriod(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "C",
        EptsReportUtils.map(
            getPatientsTransferredInFromAnotherHFDuringReportingPeriod(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "D",
        EptsReportUtils.map(
            getPatientsReInitiatedToPrEP(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "E",
        EptsReportUtils.map(
            getPatientsOnContinuedPrEP(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "F",
        EptsReportUtils.map(
            prepNewCohortQueries.getClientsWhoNewlyInitiatedPrep(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("(A or B or C or D or E) AND NOT F");

    return cd;
  }

  public CohortDefinition getPatientsTransferredInFromAnotherHFByEndOfPreviousReportingPeriod() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Patients with Negative test Results on PrEP during the reporting period");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "1",
        EptsReportUtils.map(
            getPatientsTransferredInFromAnotherHFByEndOfPreviousReportingPeriod1(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "2",
        EptsReportUtils.map(
            getPatientsTransferredInFromAnotherHFByEndOfPreviousReportingPeriod2(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("1 OR 2");

    return cd;
  }

  /**
   * <b>Description:</b> Number of patients who initiated PREP by end of previous reporting period
   * date as follows: All clients who have “O utente esta iniciar pela 1a vez a PrEP Data” (Concept
   * id 165296 from encounter type 80) and value coded equal to “Start drugs” (concept id 1256) and
   * value_datetime < start date;
   *
   * @return
   */
  public CohortDefinition getPatientsListInitiatedOnPREP() {
    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("Patients initiated to PREP by end of previous reporting period");

    definition.setQuery(
        PrepCtQueries.getPatientsListInitiatedOnPREP(
            hivMetadata.getInitialStatusPrepUserConcept().getConceptId(),
            hivMetadata.getPrepInicialEncounterType().getEncounterTypeId(),
            hivMetadata.getStartDrugs().getConceptId(),
            hivMetadata.getPrepStartDateConcept().getConceptId(),
            hivMetadata.getPrepSeguimentoEncounterType().getEncounterTypeId()));

    definition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    definition.addParameter(new Parameter("endDate", "End Date", Date.class));
    definition.addParameter(new Parameter("location", "Location", Location.class));

    return definition;
  }

  /**
   * <b>Description:</b> Number of patients who were transferred-in from another HF by end of the
   * previous reporting period: All clients who are enrolled in PrEP Program (PREP) (program id 25)
   * and have the first historical state as “Transferido de outra US” (patient state id= 76) in the
   * client chart by start of the reporting period;
   *
   * @return
   */
  public CohortDefinition getPatientsTransferredInFromAnotherHFByEndOfPreviousReportingPeriod1() {
    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName(
        "Patients who were transferred-in from another HF by end of the previous reporting period part 1");

    definition.setQuery(
        PrepCtQueries.getPatientsTransferredInFromAnotherHFByEndOfPreviousReportingPeriod1(
            hivMetadata.getPrepProgram().getProgramId(),
            hivMetadata.getStateOfStayOnPrepProgram().getConceptId()));

    definition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    definition.addParameter(new Parameter("endDate", "End Date", Date.class));
    definition.addParameter(new Parameter("location", "Location", Location.class));

    return definition;
  }

  /**
   * <b>Description:</b> Number of patients who were transferred-in from another HF by end of the
   * previous reporting period: or All clients who have marked “Transferido de outra US”(concept id
   * 1594 value coded 1369) in the first Ficha de Consulta Inicial PrEP(encounter type 80,
   * Min(encounter datetime)) registered in the system by the start of the reporting period.
   *
   * @return
   */
  public CohortDefinition getPatientsTransferredInFromAnotherHFByEndOfPreviousReportingPeriod2() {
    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName(
        "Patients who were transferred-in from another HF by end of the previous reporting period part 2");

    definition.setQuery(
        PrepCtQueries.getPatientsTransferredInFromAnotherHFByEndOfPreviousReportingPeriod2(
            hivMetadata.getReferalTypeConcept().getConceptId(),
            hivMetadata.getTransferredFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPrepInicialEncounterType().getEncounterTypeId()));

    definition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    definition.addParameter(new Parameter("endDate", "End Date", Date.class));
    definition.addParameter(new Parameter("location", "Location", Location.class));

    return definition;
  }

  /**
   * <b>Description:</b> Number of patients who Transferred in during the reporting period: All
   * clients who are enrolled in PrEP Program (PREP)(program id 25) and have the first historical
   * state as “Transferido de outra US” (patient state id= 76) in the client chart during the
   * reporting period; or All clients who have marked “Transferido de outra US”(concept id 1594
   * value coded 1369) in the first Ficha de Consulta Inicial PrEP(encounter type 80, Min(encounter
   * datetime)) registered in the system during the reporting period.
   *
   * @return
   */
  public CohortDefinition getPatientsTransferredInFromAnotherHFDuringReportingPeriod() {
    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("Patients who Transferred in during the reporting period");

    definition.setQuery(
        PrepCtQueries.getPatientsTransferredInFromAnotherHFDuringReportingPeriod(
            hivMetadata.getReferalTypeConcept().getConceptId(),
            hivMetadata.getTransferredFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPrepInicialEncounterType().getEncounterTypeId(),
            hivMetadata.getPrepProgram().getProgramId(),
            hivMetadata.getStateOfStayOnPrepProgram().getConceptId()));

    definition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    definition.addParameter(new Parameter("endDate", "End Date", Date.class));
    definition.addParameter(new Parameter("location", "Location", Location.class));

    return definition;
  }

  /**
   * <b>Description:</b> Re-initiated PrEP during the reporting period All clients who have “O
   * utente está a Retomar PrEP” (Concept id 165296 from encounter type 80) value coded “RESTART”
   * (concept id 1705) and value datetime between start date and end date;
   *
   * @return
   */
  public CohortDefinition getPatientsReInitiatedToPrEP() {
    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("Patients Re-initiated to PrEP during the reporting period");

    definition.setQuery(
        PrepCtQueries.getPatientsReInitiatedToPrEP(
            hivMetadata.getInitialStatusPrepUserConcept().getConceptId(),
            hivMetadata.getRestartConcept().getConceptId(),
            hivMetadata.getPrepInicialEncounterType().getEncounterTypeId()));

    definition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    definition.addParameter(new Parameter("endDate", "End Date", Date.class));
    definition.addParameter(new Parameter("location", "Location", Location.class));

    return definition;
  }

  /**
   * <b>Description:</b> Continued PrEP during the reporting period All clients who have “O utente
   * está a Continuar PrEP” (Concept id 165296 from encounter type 80) value coded “CONTINUE REGIMEN
   * ” (concept id 1257) and value datetime between start date and end date;
   *
   * @return
   */
  public CohortDefinition getPatientsOnContinuedPrEP() {
    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("Patients Continued on PrEP during the reporting period");

    definition.setQuery(
        PrepCtQueries.getPatientsOnContinuedPrEP(
            hivMetadata.getInitialStatusPrepUserConcept().getConceptId(),
            hivMetadata.getContinueRegimenConcept().getConceptId(),
            hivMetadata.getPrepInicialEncounterType().getEncounterTypeId()));

    definition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    definition.addParameter(new Parameter("endDate", "End Date", Date.class));
    definition.addParameter(new Parameter("location", "Location", Location.class));

    return definition;
  }

  /**
   * <b>Description:</b> Positive Test Results: Clients with the field “Resultado do Teste”(concept
   * id 1040) with value “Positivo” (concept id 703) on Ficha de Consulta de Seguimento
   * PrEP(encounter type 81) registered during the reporting period;
   *
   * @return
   */
  public CohortDefinition getPositiveTestResults() {
    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("Patients with Positive test Results on PrEP during the reporting period");

    definition.setQuery(
        PrepCtQueries.getPositiveTestResults(
            hivMetadata.getHivRapidTest1QualitativeConcept().getConceptId(),
            commonMetadata.getPositive().getConceptId(),
            hivMetadata.getPrepSeguimentoEncounterType().getEncounterTypeId()));

    definition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    definition.addParameter(new Parameter("endDate", "End Date", Date.class));
    definition.addParameter(new Parameter("location", "Location", Location.class));

    return definition;
  }

  /**
   * <b>Description:</b> Negative Test Results: Clients with the field “Resultado do Teste” (concept
   * id 1040) with value “Negativo” (concept id 664) on Ficha de Consulta de Seguimento PrEP
   * (encounter type 81) registered during the reporting period;
   *
   * @return
   */
  public CohortDefinition getNegativeTestResults() {
    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("Patients with Negative test Results on PrEP during the reporting period");

    definition.setQuery(
        PrepCtQueries.getNegativeTestResults(
            hivMetadata.getHivRapidTest1QualitativeConcept().getConceptId(),
            commonMetadata.getNegative().getConceptId(),
            hivMetadata.getPrepSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getPrepSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getDateOfInitialHivTestConcept().getConceptId()));

    definition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    definition.addParameter(new Parameter("endDate", "End Date", Date.class));
    definition.addParameter(new Parameter("location", "Location", Location.class));

    return definition;
  }

  /**
   * <b>Description:</b> Other Test Results: Clients with the field “Resultado do Teste” (concept id
   * 1040) with value “Indeterminado” (concept id 1138) on Ficha de Consulta de Seguimento PrEP
   * (encounter type 81) registered during the reporting period;
   *
   * @return
   */
  public CohortDefinition getOtherTestResults() {
    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("Patients with Other test Results Part 1 Prep");

    definition.setQuery(
        PrepCtQueries.getOtherTestResults(
            hivMetadata.getHivRapidTest1QualitativeConcept().getConceptId(),
            commonMetadata.getIndeterminate().getConceptId(),
            hivMetadata.getPrepSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getPrepInicialEncounterType().getEncounterTypeId(),
            hivMetadata.getDateOfHivTestWithNegativeResultsPrepUuidConcept().getConceptId()));

    definition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    definition.addParameter(new Parameter("endDate", "End Date", Date.class));
    definition.addParameter(new Parameter("location", "Location", Location.class));

    return definition;
  }

  public CohortDefinition getkeypop() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patients who are Key population ");
    cd.addParameter(new Parameter("onOrBefore", "Start Date", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "end Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("80", hivMetadata.getPrepInicialEncounterType().getEncounterTypeId());
    map.put("81", hivMetadata.getPrepSeguimentoEncounterType().getEncounterTypeId());
    map.put("23703", hivMetadata.getKeyPopulationConcept().getConceptId());
    map.put("20454", hivMetadata.getDrugUseConcept().getConceptId());
    map.put("1901", hivMetadata.getSexWorkerConcept().getConceptId());
    map.put("165205", hivMetadata.getTransGenderConcept().getConceptId());
    map.put("1377", hivMetadata.getHomosexualConcept().getConceptId());
    map.put("20426", hivMetadata.getImprisonmentConcept().getConceptId());

    String query =
        " SELECT p.patient_id "
            + " FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  e.voided = 0 "
            + "       AND p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND e.encounter_type IN (${80}, ${81}) "
            + "       AND o.concept_id = ${23703} "
            + "       AND o.value_coded IN ( ${20454}, ${1901}, ${165205}, ${1377}, ${20426} ) "
            + "       AND e.encounter_datetime BETWEEN :onOrAfter AND :onOrBefore "
            + " GROUP  BY p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  public CohortDefinition getPatientsWhoAreKeypopulation(List<Concept> answers) {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patients who are Key population ");
    cd.addParameter(new Parameter("onOrBefore", "Start Date", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "end Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("80", hivMetadata.getPrepInicialEncounterType().getEncounterTypeId());
    map.put("81", hivMetadata.getPrepSeguimentoEncounterType().getEncounterTypeId());
    map.put("23703", hivMetadata.getKeyPopulationConcept().getConceptId());
    map.put("20454", hivMetadata.getDrugUseConcept().getConceptId());
    map.put("1901", hivMetadata.getSexWorkerConcept().getConceptId());
    map.put("165205", hivMetadata.getTransGenderConcept().getConceptId());
    map.put("1377", hivMetadata.getHomosexualConcept().getConceptId());
    map.put("20426", hivMetadata.getImprisonmentConcept().getConceptId());
    List<Integer> concepts =
        answers.stream().map(concept -> concept.getConceptId()).collect(Collectors.toList());
    String answer = StringUtils.join(concepts, ",");

    String query =
        " SELECT p.patient_id "
            + " FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  e.voided = 0 "
            + "       AND p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND e.encounter_type IN (${80}, ${81}) "
            + "       AND o.concept_id = ${23703} "
            + "       AND o.value_coded IN ( "
            + answer
            + " ) "
            + "       AND e.encounter_datetime BETWEEN :onOrAfter AND :onOrBefore "
            + " GROUP  BY p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  public CohortDefinition getPatientsOnTargetGroup(List<Concept> answers) {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patients who are on Target Group ");
    cd.addParameter(new Parameter("onOrBefore", "Start Date", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "end Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("80", hivMetadata.getPrepInicialEncounterType().getEncounterTypeId());
    map.put("81", hivMetadata.getPrepSeguimentoEncounterType().getEncounterTypeId());
    map.put("23703", hivMetadata.getKeyPopulationConcept().getConceptId());
    map.put("20454", hivMetadata.getDrugUseConcept().getConceptId());
    map.put("1901", hivMetadata.getSexWorkerConcept().getConceptId());
    map.put("165205", hivMetadata.getTransGenderConcept().getConceptId());
    map.put("1377", hivMetadata.getHomosexualConcept().getConceptId());
    map.put("20426", hivMetadata.getImprisonmentConcept().getConceptId());
    map.put("165196", hivMetadata.getPrepTargetGroupConcept().getConceptId());
    List<Integer> concepts =
        answers.stream().map(concept -> concept.getConceptId()).collect(Collectors.toList());
    String answer = StringUtils.join(concepts, ",");

    String query =
        " SELECT p.patient_id "
            + " FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  e.voided = 0 "
            + "       AND p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND e.encounter_type = ${80} "
            + "       AND o.concept_id = ${165196} "
            + "       AND o.value_coded IN ( "
            + answer
            + " ) "
            + "       AND e.encounter_datetime BETWEEN :onOrAfter AND :onOrBefore "
            + " GROUP  BY p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  public CohortDefinition getPatientsWhoAreHomosexual() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Homosexuals");
    cd.addParameter(new Parameter("onOrBefore", "Start Date", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "end Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition HSM =
        getPatientsWhoAreKeypopulation(Arrays.asList(hivMetadata.getHomosexualConcept()));
    CohortDefinition exclusion =
        getPatientsWhoAreKeypopulation(
            Arrays.asList(
                hivMetadata.getDrugUseConcept(),
                hivMetadata.getSexWorkerConcept(),
                hivMetadata.getTransGenderConcept()));

    CohortDefinition male = genderCohortQueries.maleCohort();

    cd.addSearch("male", EptsReportUtils.map(male, ""));

    cd.addSearch(
        "homosexual",
        EptsReportUtils.map(
            HSM, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.addSearch(
        "exclusion",
        EptsReportUtils.map(
            exclusion, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.setCompositionString("(homosexual AND male) AND NOT exclusion");

    return cd;
  }

  public CohortDefinition getPatientsWhoArePrisoner() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Prisoners");
    cd.addParameter(new Parameter("onOrBefore", "Start Date", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "end Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition prisoner =
        getPatientsWhoAreKeypopulation(Arrays.asList(hivMetadata.getImprisonmentConcept()));
    CohortDefinition exclusion =
        getPatientsWhoAreKeypopulation(
            Arrays.asList(
                hivMetadata.getDrugUseConcept(),
                hivMetadata.getHomosexualConcept(),
                hivMetadata.getSexWorkerConcept(),
                hivMetadata.getTransGenderConcept()));

    cd.addSearch(
        "prisoner",
        EptsReportUtils.map(
            prisoner, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.addSearch(
        "exclusion",
        EptsReportUtils.map(
            exclusion, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.setCompositionString("prisoner AND NOT exclusion");

    return cd;
  }

  public CohortDefinition getPatientsWhoAreTransgender() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Transgender");
    cd.addParameter(new Parameter("onOrBefore", "Start Date", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "end Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition trans =
        getPatientsWhoAreKeypopulation(Arrays.asList(hivMetadata.getTransGenderConcept()));
    CohortDefinition exclusion =
        getPatientsWhoAreKeypopulation(
            Arrays.asList(hivMetadata.getDrugUseConcept(), hivMetadata.getSexWorkerConcept()));

    cd.addSearch(
        "transgender",
        EptsReportUtils.map(
            trans, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.addSearch(
        "exclusion",
        EptsReportUtils.map(
            exclusion, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.setCompositionString("transgender AND NOT exclusion");

    return cd;
  }

  public CohortDefinition getPatientsWhoAreOutro() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Outro");
    cd.addParameter(new Parameter("onOrBefore", "Start Date", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "end Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition outro =
        getPatientsWhoAreKeypopulation(Arrays.asList(hivMetadata.getOtherOrNonCodedConcept()));
    CohortDefinition exclusion =
        getPatientsWhoAreKeypopulation(
            Arrays.asList(
                hivMetadata.getImprisonmentConcept(),
                hivMetadata.getDrugUseConcept(),
                hivMetadata.getHomosexualConcept(),
                hivMetadata.getSexWorkerConcept(),
                hivMetadata.getTransGenderConcept()));

    cd.addSearch(
        "outro",
        EptsReportUtils.map(
            outro, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.addSearch(
        "exclusion",
        EptsReportUtils.map(
            exclusion, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.setCompositionString("outro AND NOT exclusion ");

    return cd;
  }

  public CohortDefinition getFemalePatientsWhoAreSexWorker() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Sex Worker");
    cd.addParameter(new Parameter("onOrBefore", "Start Date", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "end Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition SW =
        getPatientsWhoAreKeypopulation(Arrays.asList(hivMetadata.getSexWorkerConcept()));
    CohortDefinition onDrugs =
        getPatientsWhoAreKeypopulation(Arrays.asList(hivMetadata.getDrugUseConcept()));
    CohortDefinition female = genderCohortQueries.femaleCohort();

    cd.addSearch(
        "drugUser",
        EptsReportUtils.map(
            onDrugs, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.addSearch("female", EptsReportUtils.map(female, ""));

    cd.addSearch(
        "sexWorker",
        EptsReportUtils.map(
            SW, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.setCompositionString("(sexWorker AND female) AND NOT drugUser");

    return cd;
  }

  public CohortDefinition getPatientsWhoAreSerodiscordantCouples() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Serodiscordant Couples");
    cd.addParameter(new Parameter("onOrBefore", "Start Date", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "end Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition Serodiscordant =
        getPatientsOnTargetGroup(Arrays.asList(hivMetadata.getCoupleResultsAreDifferentConcept()));
    CohortDefinition Keypop = getkeypop();

    cd.addSearch(
        "SerodiscordanteCouple",
        EptsReportUtils.map(
            Serodiscordant,
            "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.addSearch(
        "KeyPopulation",
        EptsReportUtils.map(
            Keypop, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.setCompositionString("SerodiscordanteCouple AND NOT KeyPopulation");

    return cd;
  }

  public CohortDefinition getPatientsWhoAreTruckDrivers() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Truck Drivers");
    cd.addParameter(new Parameter("onOrBefore", "Start Date", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "end Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition TruckDriver =
        getPatientsOnTargetGroup(Arrays.asList(hivMetadata.getDriverConcept()));
    CohortDefinition Serodiscordant =
        getPatientsOnTargetGroup(Arrays.asList(hivMetadata.getCoupleResultsAreDifferentConcept()));
    CohortDefinition Keypop = getkeypop();

    cd.addSearch(
        "driver",
        EptsReportUtils.map(
            TruckDriver, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.addSearch(
        "serodiscordantCouple",
        EptsReportUtils.map(
            Serodiscordant,
            "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.addSearch(
        "KeyPopulation",
        EptsReportUtils.map(
            Keypop, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.setCompositionString("driver AND NOT (serodiscordantCouple OR KeyPopulation)");

    return cd;
  }

  public CohortDefinition getPatientsWhoAreMiners() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Miners");
    cd.addParameter(new Parameter("onOrBefore", "Start Date", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "end Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition miner = getPatientsOnTargetGroup(Arrays.asList(hivMetadata.getMinerConcept()));
    CohortDefinition exclusion =
        getPatientsOnTargetGroup(
            Arrays.asList(
                hivMetadata.getCoupleResultsAreDifferentConcept(), hivMetadata.getDriverConcept()));
    CohortDefinition Keypop = getkeypop();

    cd.addSearch(
        "miner",
        EptsReportUtils.map(
            miner, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.addSearch(
        "exclusion",
        EptsReportUtils.map(
            exclusion, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.addSearch(
        "KeyPopulation",
        EptsReportUtils.map(
            Keypop, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.setCompositionString("miner AND NOT (exclusion OR KeyPopulation)");

    return cd;
  }

  public CohortDefinition getPatientsWhoAreMilitary() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Military");
    cd.addParameter(new Parameter("onOrBefore", "Start Date", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "end Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition military =
        getPatientsOnTargetGroup(Arrays.asList(hivMetadata.getMilitaryOrPoliceConcept()));
    CohortDefinition exclusion =
        getPatientsOnTargetGroup(
            Arrays.asList(
                hivMetadata.getCoupleResultsAreDifferentConcept(),
                hivMetadata.getDriverConcept(),
                hivMetadata.getMinerConcept()));
    CohortDefinition Keypop = getkeypop();

    cd.addSearch(
        "military",
        EptsReportUtils.map(
            military, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.addSearch(
        "KeyPopulation",
        EptsReportUtils.map(
            Keypop, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.addSearch(
        "exclusion",
        EptsReportUtils.map(
            exclusion, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.setCompositionString("military AND NOT (exclusion OR KeyPopulation)");

    return cd;
  }

  public CohortDefinition getPatientsWhoAreBreastfeeding() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Breastfeeding");
    cd.addParameter(new Parameter("onOrBefore", "Start Date", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "end Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition breastfeeding =
        getPatientsOnTargetGroup(Arrays.asList(hivMetadata.getBreastfeeding()));
    CohortDefinition Keypop = getkeypop();
    List<Concept> conceptExcluions =
        Arrays.asList(
            hivMetadata.getDriverConcept(),
            hivMetadata.getMinerConcept(),
            hivMetadata.getMilitaryOrPoliceConcept(),
            hivMetadata.getCoupleResultsAreDifferentConcept());
    CohortDefinition exclusion = getPatientsOnTargetGroup(conceptExcluions);

    cd.addSearch(
        "breastfeedingWoman",
        EptsReportUtils.map(
            breastfeeding, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.addSearch(
        "KeyPopulation",
        EptsReportUtils.map(
            Keypop, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.addSearch(
        "exclusion",
        EptsReportUtils.map(
            exclusion, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.setCompositionString("breastfeedingWoman AND NOT (exclusion OR KeyPopulation)");

    return cd;
  }

  public CohortDefinition getPatientsWhoArePregnant() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Pregnant Woman");
    cd.addParameter(new Parameter("onOrBefore", "Start Date", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "end Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition Pregnant =
        getPatientsOnTargetGroup(Arrays.asList(hivMetadata.getPregnantConcept()));
    CohortDefinition Keypop = getkeypop();
    List<Concept> conceptExcluions =
        Arrays.asList(
            hivMetadata.getDriverConcept(),
            hivMetadata.getMinerConcept(),
            hivMetadata.getMilitaryOrPoliceConcept(),
            hivMetadata.getBreastfeeding(),
            hivMetadata.getCoupleResultsAreDifferentConcept());
    CohortDefinition exclusion = getPatientsOnTargetGroup(conceptExcluions);

    cd.addSearch(
        "PregnantWoman",
        EptsReportUtils.map(
            Pregnant, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.addSearch(
        "Exclusion",
        EptsReportUtils.map(
            exclusion, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.addSearch(
        "KeyPopulation",
        EptsReportUtils.map(
            Keypop, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.setCompositionString("PregnantWoman AND NOT (Exclusion OR KeyPopulation)");

    return cd;
  }

  public CohortDefinition getPatientsWhoAreAdolescentAndYouth() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("adolescents And Youth At Risk");
    cd.addParameter(new Parameter("onOrBefore", "Start Date", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "end Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition AYR =
        getPatientsOnTargetGroup(Arrays.asList(hivMetadata.getAdolescentsAndYouthAtRiskConcept()));
    CohortDefinition exclusions =
        getPatientsOnTargetGroup(
            Arrays.asList(
                hivMetadata.getCoupleResultsAreDifferentConcept(),
                hivMetadata.getDriverConcept(),
                hivMetadata.getMinerConcept(),
                hivMetadata.getMilitaryOrPoliceConcept(),
                hivMetadata.getBreastfeeding(),
                hivMetadata.getPregnantConcept()));
    CohortDefinition Keypop = getkeypop();

    cd.addSearch(
        "adolescentAndYouth",
        EptsReportUtils.map(
            AYR, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.addSearch(
        "exclusions",
        EptsReportUtils.map(
            exclusions, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.addSearch(
        "KeyPopulation",
        EptsReportUtils.map(
            Keypop, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.setCompositionString("adolescentAndYouth AND NOT (exclusions OR KeyPopulation)");

    return cd;
  }
}
