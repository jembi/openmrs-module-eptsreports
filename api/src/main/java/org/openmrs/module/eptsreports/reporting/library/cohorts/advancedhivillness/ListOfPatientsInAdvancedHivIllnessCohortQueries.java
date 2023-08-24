package org.openmrs.module.eptsreports.reporting.library.cohorts.advancedhivillness;

import java.util.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.library.queries.advancedhivillness.ListOfPatientsOnAdvancedHivIllnessQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsQueriesUtil;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsInAdvancedHivIllnessCohortQueries {

  private final HivMetadata hivMetadata;

  private final CommonMetadata commonMetadata;

  private final ListOfPatientsOnAdvancedHivIllnessQueries listOfPatientsOnAdvancedHivIllnessQueries;

  String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";

  @Autowired
  public ListOfPatientsInAdvancedHivIllnessCohortQueries(
      HivMetadata hivMetadata,
      CommonMetadata commonMetadata,
      ListOfPatientsOnAdvancedHivIllnessQueries listOfPatientsOnAdvancedHivIllnessQueries) {
    this.hivMetadata = hivMetadata;
    this.commonMetadata = commonMetadata;
    this.listOfPatientsOnAdvancedHivIllnessQueries = listOfPatientsOnAdvancedHivIllnessQueries;
  }

  /**
   * <b>Lista de Utentes em seguimento do Modelo de DAH ou com critérios para iniciar o seguimento
   * do Modelo de DAH</b>
   * <li>Utentes que iniciaram o seguimento do Modelo de DAH OR
   * <li>Utentes com critério de CD4 para início de seguimento no Modelo de DAH OR
   * <li>Utentes com critério de Estadiamento para início de seguimento do Modelo dee DAH
   *
   * @see #getPatientsWhoStartedFollowupOnDAH(boolean) ()
   * @see #getPatientsWithCD4CriteriaToStartFollowupOnDAH()
   * @see #getPatientsWithCriterioEstadiamentoInicioSeguimento()
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsOnFollowupOrWithCriteriaToStartFollowupOfDAH() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName(
        "Lista de Utentes em seguimento do Modelo de DAH ou com critérios para iniciar o seguimento do Modelo de DAH");
    cd.addParameters(getCohortParameters());

    cd.addSearch(
        "STARTEDFOLLOWUP", EptsReportUtils.map(getPatientsWhoStartedFollowupOnDAH(true), mappings));

    cd.addSearch(
        "CD4", EptsReportUtils.map(getPatientsWithCD4CriteriaToStartFollowupOnDAH(), mappings));

    cd.addSearch(
        "ESTADIO",
        EptsReportUtils.map(getPatientsWithCriterioEstadiamentoInicioSeguimento(), mappings));

    // EXCLUSIONS

    cd.addSearch(
        "FOLLOWUPBEFORESTARTDATE",
        EptsReportUtils.map(getPatientsWhoStartedFollowupOnDAH(false), mappings));

    cd.addSearch(
        "TRANSFERREDOUT",
        EptsReportUtils.map(
            getPatientsTransferredOutByTheEndOfPeriod(),
            "endDate=${endDate},location=${location}"));

    cd.setCompositionString(
        "(STARTEDFOLLOWUP OR CD4 OR ESTADIO) AND NOT (FOLLOWUPBEFORESTARTDATE OR TRANSFERREDOUT)");

    return cd;
  }

  /**
   * <b>Total de Utentes Eligiveis a MDS de DAH</b>
   * <li>Utentes com critério de CD4 para início de seguimento no Modelo de DAH OR
   * <li>Utentes com critério de Estadiamento para início de seguimento do Modelo dee DAH
   *
   * @see #getPatientsWithCD4CriteriaToStartFollowupOnDAH()
   * @see #getPatientsWithCriterioEstadiamentoInicioSeguimento()
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getTotalOfPatientsWithCriteriaToStartFollowupOfDAH() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Lista de Utentes com critérios para iniciar o seguimento do Modelo de DAH");
    cd.addParameters(getCohortParameters());

    cd.addSearch(
        "CD4", EptsReportUtils.map(getPatientsWithCD4CriteriaToStartFollowupOnDAH(), mappings));

    cd.addSearch(
        "ESTADIO",
        EptsReportUtils.map(getPatientsWithCriterioEstadiamentoInicioSeguimento(), mappings));

    cd.setCompositionString("CD4 OR ESTADIO");

    return cd;
  }

  /**
   * <b>DAH FR3 - Utentes que iniciaram o seguimento no Modelo de DAH </b>
   *
   * <p>Utentes com registo do “Início de Seguimento no Modelo de Doença Avançada” (Data Início DAH
   * - encounter_datetime) na Ficha de DAH (encounter type = 90) durante o período de avaliação, ou
   * seja, “Data Início DAH” >= “Data Início Avaliação” e “Data Início DAH” <= “Data Fim Avaliação”
   *
   * @param duringThePeriod Flag to change the evaluation period
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoStartedFollowupOnDAH(boolean duringThePeriod) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Utentes que Iniciaram o seguimento na Ficha DAH");
    sqlCohortDefinition.addParameters(getCohortParameters());

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("90", hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId());

    String query =
        " SELECT p.patient_id "
            + "FROM "
            + "    patient p INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "WHERE p.voided = 0 AND e.voided = 0 "
            + "  AND e.encounter_type = ${90} ";
    query +=
        duringThePeriod
            ? "  AND e.encounter_datetime >= :startDate "
                + "  AND e.encounter_datetime <= :endDate "
            : " AND e.encounter_datetime < :startDate ";

    query += "  AND e.location_id = :location " + "GROUP BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(substitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>DAH FR4 - Utentes com critério de CD4 para início de seguimento no Modelo de DAH </b>
   *
   * @see ListOfPatientsOnAdvancedHivIllnessQueries#getCd4ResultOverOrEqualTo5years() OR
   * @see ListOfPatientsOnAdvancedHivIllnessQueries#getCd4ResultBetweenOneAnd5years() OR
   * @see ListOfPatientsOnAdvancedHivIllnessQueries#getCd4ResultBellowOneYear()
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithCD4CriteriaToStartFollowupOnDAH() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        " Utentes com critério de CD4 para início de seguimento no Modelo de DAH");
    sqlCohortDefinition.addParameters(getCohortParameters());

    Map<String, Integer> valuesMap = getStringIntegerMap();

    EptsQueriesUtil eptsQueriesUtil = new EptsQueriesUtil();

    String Cd4ResultOverOrEqualTo5yearsPid =
        eptsQueriesUtil
            .patientIdQueryBuilder(
                listOfPatientsOnAdvancedHivIllnessQueries.getCd4ResultOverOrEqualTo5years())
            .getQuery();

    String getCd4ResultBetweenOneAnd5yearsPid =
        eptsQueriesUtil
            .patientIdQueryBuilder(
                listOfPatientsOnAdvancedHivIllnessQueries.getCd4ResultBetweenOneAnd5years())
            .getQuery();

    String getCd4ResultBellowOneYearPid =
        eptsQueriesUtil
            .patientIdQueryBuilder(
                listOfPatientsOnAdvancedHivIllnessQueries.getCd4ResultBellowOneYear())
            .getQuery();

    String query =
        eptsQueriesUtil
            .unionBuilder(Cd4ResultOverOrEqualTo5yearsPid)
            .union(getCd4ResultBetweenOneAnd5yearsPid)
            .union(getCd4ResultBellowOneYearPid)
            .buildQuery();

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(substitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b> Utentes com critério de Estadiamento para início de seguimento no Modelo de DAH</b>
   * <li>pelo menos um registo da lista representativa de Estadio IV (DAH_RF28), no campo de
   *     “Infecções Oportunistas incluindo Sarcoma de Kaposi e outras doenças”, na “Ficha Clínica –
   *     Ficha Mestra”, registada durante o período de avaliação (“Data Consulta” >= “Data Início” e
   *     “Data Consulta” <= “Data Fim”) ou
   * <li>pelo menos um registo da lista representativa de Estadio III (DAH_RF27), no campo de
   *     “Infecções Oportunistas incluindo Sarcoma de Kaposi e outras doenças”, na “Ficha Clínica –
   *     Ficha Mestra”, registada durante o período de avaliação (“Data Consulta” >= “Data Início” e
   *     “Data Consulta” <= “Data Fim”)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithCriterioEstadiamentoInicioSeguimento() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" Utentes com Infecções Oportunistas");

    sqlCohortDefinition.addParameters(getCohortParameters());

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("1406", hivMetadata.getOtherDiagnosis().getConceptId());
    valuesMap.put("5018", hivMetadata.getChronicDiarrheaConcept().getConceptId());
    valuesMap.put("3", hivMetadata.getAnemiaConcept().getConceptId());
    valuesMap.put("5945", hivMetadata.getFeverConcept().getConceptId());
    valuesMap.put("43", hivMetadata.getPneumoniaConcept().getConceptId());
    valuesMap.put("60", hivMetadata.getMeningitisConcept().getConceptId());
    valuesMap.put("126", hivMetadata.getGingivitisConcept().getConceptId());
    valuesMap.put("6783", hivMetadata.getEstomatiteUlcerativaNecrotizanteConcept().getConceptId());
    valuesMap.put("5334", hivMetadata.getCandidiaseOralConcept().getConceptId());
    valuesMap.put("1294", hivMetadata.getCryptococcalMeningitisConcept().getConceptId());
    valuesMap.put("1570", hivMetadata.getCervicalCancerConcept().getConceptId());
    valuesMap.put("5340", hivMetadata.getCandidiaseEsofagicaConcept().getConceptId());
    valuesMap.put("5344", hivMetadata.getHerpesSimplesConcept().getConceptId());
    valuesMap.put("14656", hivMetadata.getCachexiaConcept().getConceptId());
    valuesMap.put("7180", hivMetadata.getToxoplasmoseConcept().getConceptId());
    valuesMap.put(
        "6990", hivMetadata.getHivDiseaseResultingInEncephalopathyConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM "
            + "    patient p INNER JOIN encounter e ON p.patient_id= e.patient_id "
            + "              INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "WHERE p.voided = 0 AND e.voided = 0 AND o.voided = 0 "
            + "  AND e.encounter_type = ${6} "
            + "  AND o.concept_id = ${1406} "
            + "  AND o.value_coded IN (${3},${43},${126},${60},${1294},${1570},${5018},${5334},${5344},${5340},${5945},${6783},${6990},${7180},${14656}) "
            + "  AND e.encounter_datetime >= :startDate "
            + "  AND e.encounter_datetime <= :endDate "
            + "  AND e.location_id = :location";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(substitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getARTStartDate() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Data Início TARV (1º levantamento)");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    valuesMap.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    valuesMap.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    valuesMap.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    valuesMap.put("23865", hivMetadata.getArtPickupConcept().getConceptId());

    String query =
    listOfPatientsOnAdvancedHivIllnessQueries.getArtStartDateQuery();

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b> Data de Último Levantamento TARV</b>
   * <li>A data mais recente de Levantamento de TARV, decorrida no período de avaliação, registrado
   *     no FILA (“Data de Levantamento”) ou “Ficha Recepção/ Levantou ARVs?” com “Levantou ARV” =
   *     Sim (“Data de Levantamento”) "data mais recente">= “DataInicioAvaliação” "data mais
   *     recente" <= “DataFimAvaliação”
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getLastARVPickupDate() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Data de Último Levantamento TARV");
    sqlPatientDataDefinition.addParameters(getCohortParameters());

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    valuesMap.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    valuesMap.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    valuesMap.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    valuesMap.put("23865", hivMetadata.getArtPickupConcept().getConceptId());

    String query =
        " SELECT pickup.patient_id, MAX(pickup.pickup_date) max_pickup_date FROM ( "
            + " SELECT p.patient_id, MAX(e.encounter_datetime) pickup_date FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + " WHERE e.encounter_type = ${18} "
            + " AND e.encounter_datetime >= :startDate "
            + " AND e.encounter_datetime <= :endDate "
            + " AND e.voided = 0 "
            + " AND p.voided = 0 "
            + " AND e.location_id = :location "
            + " GROUP BY p.patient_id "
            + " UNION "
            + "    SELECT p.patient_id,  MAX(o.value_datetime) AS pickup_date FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + " INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                         INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id  "
            + "                         AND o.person_id = o2.person_id "
            + " WHERE e.encounter_type = ${52} "
            + " AND o.concept_id = ${23866} "
            + "                 AND o.value_datetime >= :startDate "
            + "                 AND o.value_datetime <= :endDate "
            + "                 AND o.voided = 0 "
            + "                 AND o2.concept_id = ${23865} "
            + "                 AND o2.value_coded = ${1065} "
            + "                 AND o2.voided = 0 "
            + " AND e.voided = 0 "
            + " AND p.voided = 0 "
            + " AND e.location_id = :location                 "
            + " GROUP BY p.patient_id "
            + " ) pickup  "
            + " GROUP BY pickup.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b> Situação TARV no Início do Seguimento de DAH </b>
   * <li>O registo mais recente de “Situação de TARV” (Novo Inicio, Reinicio, em TARV, pré-TARV), na
   *     “Ficha de Doença Avançada por HIV”, registada até o fim do período de avaliação.
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getLastARVSituation() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Situação TARV no Início do Seguimento de DAH");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("90", hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId());
    valuesMap.put("1256", hivMetadata.getStartDrugs().getConceptId());
    valuesMap.put("1255", hivMetadata.getARVPlanConcept().getConceptId());
    valuesMap.put("1705", hivMetadata.getRestartConcept().getConceptId());
    valuesMap.put("6275", hivMetadata.getPreTarvConcept().getConceptId());
    valuesMap.put("6276", hivMetadata.getArtStatus().getConceptId());

    String query =
        "SELECT e.patient_id, o.value_coded AS situation "
            + "FROM encounter e "
            + "         INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "         INNER JOIN     (SELECT p.patient_id, MAX(e.encounter_datetime) as start_date "
            + "                         FROM "
            + "                             patient p INNER JOIN encounter e ON p.patient_id= e.patient_id "
            + "                                       INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "                         WHERE p.voided = 0 AND e.voided = 0 AND o.voided = 0 "
            + "                           AND e.encounter_type = ${90} "
            + "                           AND o.concept_id = ${1255} "
            + "                           AND o.value_coded IN (${1256},${1705},${6275},${6276}) "
            + "                           AND e.encounter_datetime <= :endDate "
            + "                           AND e.location_id = :location "
            + "                         GROUP BY p.patient_id)last_situation ON last_situation.patient_id = e.patient_id "
            + "WHERE e.voided = 0 AND o.voided = 0 "
            + "  AND e.encounter_type = ${90} "
            + "  AND o.concept_id = ${1255} "
            + "  AND o.value_coded IN (${1256},${1705},${6275},${6276}) "
            + "  AND e.encounter_datetime = last_situation.start_date "
            + "  AND e.location_id = :location "
            + "GROUP BY e.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>Data de Início de Seguimento de DAH</b>
   * <li>A data mais recente de “Início de Seguimento no Modelo de Doença Avançada”, registada até o
   *     fim do período de avaliação, na “Ficha de Doença Avançada por HIV”
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getFollowupStartDateDAH() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Data de Início de Seguimento de DAH");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("90", hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId());

    String query =
        " SELECT p.patient_id, MAX(e.encounter_datetime) AS most_recent "
            + "FROM "
            + "    patient p INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "WHERE p.voided = 0 AND e.voided = 0 "
            + "  AND e.encounter_type = ${90} "
            + "  AND e.encounter_datetime <= :endDate "
            + "  AND e.location_id = :location "
            + "GROUP BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(substitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /** @return {@link DataDefinition} */
  public DataDefinition getDateOfEstadioOnPeriod() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(" Data de Registo de Estadio");
    sqlPatientDataDefinition.addParameters(getCohortParameters());

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("90", hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId());
    valuesMap.put("1206", hivMetadata.getWho3AdultStageConcept().getConceptId());
    valuesMap.put("1207", hivMetadata.getWho4AdultStageConcept().getConceptId());
    valuesMap.put("5356", hivMetadata.getcurrentWhoHivStageConcept().getConceptId());

    String query =
        "SELECT p.patient_id, "
            + "               Min(e.encounter_datetime) AS consultation_date "
            + "        FROM   patient p "
            + "               INNER JOIN encounter e "
            + "                       ON p.patient_id = e.patient_id "
            + "               INNER JOIN obs o "
            + "                       ON e.encounter_id = o.encounter_id "
            + "        WHERE  p.voided = 0 "
            + "               AND e.voided = 0 "
            + "               AND o.voided = 0 "
            + "               AND e.encounter_type IN ( ${6}, ${90} ) "
            + "               AND o.concept_id = ${5356} "
            + "               AND o.value_coded IN ( ${1206}, ${1207} ) "
            + "               AND e.encounter_datetime >= :startDate "
            + "               AND e.encounter_datetime <= :endDate "
            + "               AND e.location_id = :location "
            + "        GROUP  BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(substitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /** @return {@link DataDefinition} */
  public DataDefinition getResultOfEstadioDuringPeriod() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Infecções Estadio OMS");
    sqlPatientDataDefinition.addParameters(getCohortParameters());

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("90", hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId());
    valuesMap.put("1206", hivMetadata.getWho3AdultStageConcept().getConceptId());
    valuesMap.put("1207", hivMetadata.getWho4AdultStageConcept().getConceptId());
    valuesMap.put("5356", hivMetadata.getcurrentWhoHivStageConcept().getConceptId());

    String query =
        "SELECT e.patient_id, o.value_coded as estadio "
            + "FROM encounter e "
            + "         INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "         INNER JOIN     ( "
            + "    SELECT p.patient_id, "
            + "                        Min(e.encounter_datetime) AS consultation_date "
            + "                             FROM   patient p "
            + "                                    INNER JOIN encounter e "
            + "                                            ON p.patient_id = e.patient_id "
            + "                                    INNER JOIN obs o "
            + "                                            ON e.encounter_id = o.encounter_id "
            + "                             WHERE  p.voided = 0 "
            + "                                    AND e.voided = 0 "
            + "                                    AND o.voided = 0 "
            + "                                    AND e.encounter_type IN ( ${6}, ${90} ) "
            + "                                    AND o.concept_id = ${5356} "
            + "                                    AND o.value_coded IN ( ${1206}, ${1207} ) "
            + "                                    AND e.encounter_datetime >= :startDate "
            + "                                    AND e.encounter_datetime <= :endDate "
            + "                                    AND e.location_id = :location "
            + "                             GROUP  BY p.patient_id "
            + ")first_consultation ON first_consultation.patient_id = e.patient_id "
            + "WHERE e.voided = 0 AND o.voided = 0 "
            + "  AND e.encounter_type IN ( ${6}, ${90} ) "
            + "  AND o.concept_id = ${5356} "
            + "  AND o.value_coded IN (${1206}, ${1207}) "
            + "  AND e.encounter_datetime = first_consultation.consultation_date "
            + "  AND e.location_id = :location "
            + "GROUP BY e.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(substitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b> A data do registo do resultado de CD4 (absoluto) durante o período de avaliação </b>
   *
   * @see ListOfPatientsOnAdvancedHivIllnessQueries#getCd4ResultOverOrEqualTo5years() OR
   * @see ListOfPatientsOnAdvancedHivIllnessQueries#getCd4ResultBetweenOneAnd5years() OR
   * @see ListOfPatientsOnAdvancedHivIllnessQueries#getCd4ResultBellowOneYear()
   * @return {@link CohortDefinition}
   */
  public DataDefinition getCd4ResultDate() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Data de resultado de CD4 Absoluto");
    sqlPatientDataDefinition.addParameters(getCohortParameters());

    Map<String, Integer> valuesMap = getStringIntegerMap();

    EptsQueriesUtil eptsQueriesUtil = new EptsQueriesUtil();

    String query =
        eptsQueriesUtil
            .unionBuilder(
                listOfPatientsOnAdvancedHivIllnessQueries.getCd4ResultOverOrEqualTo5years())
            .union(listOfPatientsOnAdvancedHivIllnessQueries.getCd4ResultBetweenOneAnd5years())
            .union(listOfPatientsOnAdvancedHivIllnessQueries.getCd4ResultBellowOneYear())
            .buildQuery();

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b> Resultado de CD4 (absoluto) durante o período de avaliação </b>
   *
   * @see ListOfPatientsOnAdvancedHivIllnessQueries#getCd4ResultOverOrEqualTo5years() OR
   * @see ListOfPatientsOnAdvancedHivIllnessQueries#getCd4ResultBetweenOneAnd5years() OR
   * @see ListOfPatientsOnAdvancedHivIllnessQueries#getCd4ResultBellowOneYear()
   * @return {@link CohortDefinition}
   */
  public DataDefinition getCd4Result() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Resultado de CD4 Absoluto");
    sqlPatientDataDefinition.addParameters(getCohortParameters());

    Map<String, Integer> valuesMap = getStringIntegerMap();

    EptsQueriesUtil eptsQueriesUtil = new EptsQueriesUtil();

    String query =
        eptsQueriesUtil
            .unionBuilder(listOfPatientsOnAdvancedHivIllnessQueries.getCd4ResultOverOrEqualTo5y())
            .union(listOfPatientsOnAdvancedHivIllnessQueries.getCd4ResultBetweenOneAnd5y())
            .union(listOfPatientsOnAdvancedHivIllnessQueries.getCd4ResultBellow1y())
            .buildQuery();

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>Data de Registo Mais recente de Estadio OMS - Column O</b>
   * <li>A data de registo mais recente de “Estadio OMS”, na “Ficha Clínica (Ficha Mestra)”,
   *     ocorrido até o fim do período de avaliação.
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getDateOfMostRecentEstadioByEndOfPeriod() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(
        "Data Mais recente de Registo de Estadio ate o fim do periodo");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("1204", hivMetadata.getWhoStageIConcept().getConceptId());
    valuesMap.put("1205", hivMetadata.getWhoStageIIConcept().getConceptId());
    valuesMap.put("1206", hivMetadata.getWho3AdultStageConcept().getConceptId());
    valuesMap.put("1207", hivMetadata.getWho4AdultStageConcept().getConceptId());
    valuesMap.put("5356", hivMetadata.getcurrentWhoHivStageConcept().getConceptId());

    String query =
        "SELECT p.patient_id, "
            + "               MAX(e.encounter_datetime) AS consultation_date "
            + "        FROM   patient p "
            + "               INNER JOIN encounter e "
            + "                       ON p.patient_id = e.patient_id "
            + "               INNER JOIN obs o "
            + "                       ON e.encounter_id = o.encounter_id "
            + "        WHERE  p.voided = 0 "
            + "               AND e.voided = 0 "
            + "               AND o.voided = 0 "
            + "               AND e.encounter_type = ${6} "
            + "               AND o.concept_id = ${5356} "
            + "               AND o.value_coded IN ( ${1204}, ${1205}, ${1206}, ${1207} ) "
            + "               AND e.encounter_datetime <= :endDate "
            + "               AND e.location_id = :location "
            + "        GROUP  BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(substitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>Estadio OMS no registo mais recente ate o fim do periodo - Column P</b>
   * <li>O registo mais recente de “Estadio OMS”, na “Ficha Clínica (Ficha Mestra)”, ocorrido até o
   *     fim do período de avaliação.
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getResultOfMostRecentEstadioByEndOfPeriod() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Estadio OMS no registo mais recente ate o fim do periodo");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("1204", hivMetadata.getWhoStageIConcept().getConceptId());
    valuesMap.put("1205", hivMetadata.getWhoStageIIConcept().getConceptId());
    valuesMap.put("1206", hivMetadata.getWho3AdultStageConcept().getConceptId());
    valuesMap.put("1207", hivMetadata.getWho4AdultStageConcept().getConceptId());
    valuesMap.put("5356", hivMetadata.getcurrentWhoHivStageConcept().getConceptId());

    String query =
        "SELECT e.patient_id, o.value_coded as estadio "
            + "FROM encounter e "
            + "         INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "         INNER JOIN     ( "
            + "SELECT p.patient_id, "
            + "               MAX(e.encounter_datetime) AS consultation_date "
            + "        FROM   patient p "
            + "               INNER JOIN encounter e "
            + "                       ON p.patient_id = e.patient_id "
            + "               INNER JOIN obs o "
            + "                       ON e.encounter_id = o.encounter_id "
            + "        WHERE  p.voided = 0 "
            + "               AND e.voided = 0 "
            + "               AND o.voided = 0 "
            + "               AND e.encounter_type = ${6} "
            + "               AND o.concept_id = ${5356} "
            + "               AND o.value_coded IN ( ${1204}, ${1205}, ${1206}, ${1207} ) "
            + "               AND e.encounter_datetime <= :endDate "
            + "               AND e.location_id = :location "
            + "        GROUP  BY p.patient_id "
            + ")last_consultation ON last_consultation.patient_id = e.patient_id "
            + "WHERE e.voided = 0 AND o.voided = 0 "
            + "  AND e.encounter_type = ${6} "
            + "  AND o.concept_id = ${5356} "
            + "  AND o.value_coded IN ( ${1204}, ${1205}, ${1206}, ${1207} ) "
            + "  AND e.encounter_datetime = last_consultation.consultation_date "
            + "  AND e.location_id = :location "
            + "GROUP BY e.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(substitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>Motivo de Mudança de Estadiamento Clínico - 1</b>
   * <li>O primeiro registo de Diagnóstico TB Activo= “S” decorrido ate o fim do período de
   *     avaliação OR um registo de Infecções Oportunistas na data mais recente do estadiamento
   *     clínico decorrido no período de avaliação
   *
   * @see #getDateOfMostRecentEstadioByEndOfPeriod()
   * @return {@link DataDefinition}
   */
  public DataDefinition getReasonToChangeEstadio1() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Motivo de Mudança de Estadiamento Clínico - 1");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("1065", hivMetadata.getYesConcept().getConceptId());
    valuesMap.put("1204", hivMetadata.getWhoStageIConcept().getConceptId());
    valuesMap.put("1205", hivMetadata.getWhoStageIIConcept().getConceptId());
    valuesMap.put("1206", hivMetadata.getWho3AdultStageConcept().getConceptId());
    valuesMap.put("1207", hivMetadata.getWho4AdultStageConcept().getConceptId());
    valuesMap.put("1406", hivMetadata.getOtherDiagnosis().getConceptId());
    valuesMap.put("5356", hivMetadata.getcurrentWhoHivStageConcept().getConceptId());
    valuesMap.put("23761", hivMetadata.getActiveTBConcept().getConceptId());

    String query =
        " SELECT tb.patient_id, 'TB' AS motivo_mudanca FROM ( "
            + "SELECT p.patient_id, "
            + "               MIN(e.encounter_datetime) AS consultation_date "
            + "        FROM   patient p "
            + "               INNER JOIN encounter e "
            + "                       ON p.patient_id = e.patient_id "
            + "               INNER JOIN obs o "
            + "                       ON e.encounter_id = o.encounter_id "
            + "        WHERE  p.voided = 0 "
            + "               AND e.voided = 0 "
            + "               AND o.voided = 0 "
            + "               AND e.encounter_type = ${6} "
            + "               AND o.concept_id = ${23761} "
            + "               AND o.value_coded = ${1065} "
            + "               AND e.encounter_datetime <= :endDate "
            + "               AND e.location_id = :location "
            + "        GROUP  BY p.patient_id "
            + " ) tb "
            + " UNION "
            + "SELECT p.patient_id, 'Infeccoes Oportunistas' AS motivo_mudanca "
            + "FROM "
            + "    patient p INNER JOIN encounter e ON p.patient_id= e.patient_id "
            + "              INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "              INNER JOIN ( "
            + "        SELECT p.patient_id, "
            + "               MAX(e.encounter_datetime) AS consultation_date "
            + "        FROM   patient p "
            + "                   INNER JOIN encounter e "
            + "                              ON p.patient_id = e.patient_id "
            + "                   INNER JOIN obs o "
            + "                              ON e.encounter_id = o.encounter_id "
            + "        WHERE  p.voided = 0 "
            + "          AND e.voided = 0 "
            + "          AND o.voided = 0 "
            + "          AND e.encounter_type = ${6} "
            + "          AND o.concept_id = ${5356} "
            + "          AND o.value_coded IN ( ${1204}, ${1205}, ${1206}, ${1207} ) "
            + "          AND e.encounter_datetime <= :endDate "
            + "          AND e.location_id = :location "
            + "        GROUP  BY p.patient_id "
            + "    ) estadio ON estadio.patient_id = p.patient_id "
            + "WHERE p.voided = 0 AND e.voided = 0 AND o.voided = 0 "
            + "  AND e.encounter_type = ${6} "
            + "  AND o.concept_id = ${1406} "
            + "  AND o.value_coded IS NOT NULL "
            + "  AND e.encounter_datetime = estadio.consultation_date "
            + "  AND e.location_id = :location";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(substitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>Motivo de Mudança de Estadiamento Clínico - 2</b>
   * <li>O segundo registo de Diagnóstico TB Activo= “S” decorrido ate o fim do período de avaliação
   *     OR um registo de Infecções Oportunistas na data mais recente do estadiamento clínico
   *     decorrido no período de avaliação
   *
   * @see #getDateOfMostRecentEstadioByEndOfPeriod()
   * @return {@link DataDefinition}
   */
  public DataDefinition getReasonToChangeEstadio2() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Motivo de Mudança de Estadiamento Clínico - 2");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("1065", hivMetadata.getYesConcept().getConceptId());
    valuesMap.put("1204", hivMetadata.getWhoStageIConcept().getConceptId());
    valuesMap.put("1205", hivMetadata.getWhoStageIIConcept().getConceptId());
    valuesMap.put("1206", hivMetadata.getWho3AdultStageConcept().getConceptId());
    valuesMap.put("1207", hivMetadata.getWho4AdultStageConcept().getConceptId());
    valuesMap.put("1406", hivMetadata.getOtherDiagnosis().getConceptId());
    valuesMap.put("5356", hivMetadata.getcurrentWhoHivStageConcept().getConceptId());
    valuesMap.put("23761", hivMetadata.getActiveTBConcept().getConceptId());

    String query =
        " SELECT tb.patient_id, 'TB' AS motivo_mudanca FROM ( "
            + " SELECT p.patient_id, "
            + "               MIN(e.encounter_datetime) AS consultation_date "
            + "        FROM   patient p "
            + "               INNER JOIN encounter e "
            + "                       ON p.patient_id = e.patient_id "
            + "               INNER JOIN obs o "
            + "                       ON e.encounter_id = o.encounter_id "
            + " INNER JOIN ( "
            + " SELECT p.patient_id, "
            + "               MIN(e.encounter_datetime) AS consultation_date "
            + "        FROM   patient p "
            + "               INNER JOIN encounter e "
            + "                       ON p.patient_id = e.patient_id "
            + "               INNER JOIN obs o "
            + "                       ON e.encounter_id = o.encounter_id "
            + "        WHERE  p.voided = 0 "
            + "               AND e.voided = 0 "
            + "               AND o.voided = 0 "
            + "               AND e.encounter_type = ${6} "
            + "               AND o.concept_id = ${23761} "
            + "               AND o.value_coded = ${1065} "
            + "               AND e.encounter_datetime <= :endDate "
            + "               AND e.location_id = :location "
            + "        GROUP  BY p.patient_id "
            + " ) first_tb ON first_tb.patient_id = p.patient_id "
            + "        WHERE  p.voided = 0 "
            + "               AND e.voided = 0 "
            + "               AND o.voided = 0 "
            + "               AND e.encounter_type = ${6} "
            + "               AND o.concept_id = ${23761} "
            + "               AND o.value_coded = ${1065} "
            + "               AND e.encounter_datetime > first_tb.consultation_date "
            + "               AND e.encounter_datetime <= :endDate "
            + "               AND e.location_id = :location "
            + "        GROUP  BY p.patient_id "
            + " ) tb "
            + " UNION "
            + "SELECT p.patient_id, 'Infeccoes Oportunistas' AS motivo_mudanca "
            + "FROM "
            + "    patient p INNER JOIN encounter e ON p.patient_id= e.patient_id "
            + "              INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "              INNER JOIN ( "
            + "        SELECT p.patient_id, "
            + "               MAX(e.encounter_datetime) AS consultation_date "
            + "        FROM   patient p "
            + "                   INNER JOIN encounter e "
            + "                              ON p.patient_id = e.patient_id "
            + "                   INNER JOIN obs o "
            + "                              ON e.encounter_id = o.encounter_id "
            + "        WHERE  p.voided = 0 "
            + "          AND e.voided = 0 "
            + "          AND o.voided = 0 "
            + "          AND e.encounter_type = ${6} "
            + "          AND o.concept_id = ${5356} "
            + "          AND o.value_coded IN ( ${1204}, ${1205}, ${1206}, ${1207} ) "
            + "          AND e.encounter_datetime <= :endDate "
            + "          AND e.location_id = :location "
            + "        GROUP  BY p.patient_id "
            + "    ) estadio ON estadio.patient_id = p.patient_id "
            + "WHERE p.voided = 0 AND e.voided = 0 AND o.voided = 0 "
            + "  AND e.encounter_type = ${6} "
            + "  AND o.concept_id = ${1406} "
            + "  AND o.value_coded IS NOT NULL "
            + "  AND e.encounter_datetime = estadio.consultation_date "
            + "  AND e.location_id = :location";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(substitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b> Resultado do Último CD4 </b>
   * <li>O registo mais recente de resultado de CD4 (absoluto) ocorrido até o fim do período de
   *     avaliação, registado na “Ficha Clínica – Ficha Mestra” ou “Ficha Resumo – Ficha Mestra” ou
   *     “Ficha e-Lab” ou “Ficha de Laboratório” ou “Ficha de Doença Avançada por HIV”
   *
   * @return {@link CohortDefinition}
   */
  public DataDefinition getLastCd4Result() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Resultado do Último CD4 Absoluto");
    sqlPatientDataDefinition.addParameters(getCohortParameters());

    Map<String, Integer> valuesMap = getStringIntegerMap();

    String query =
        " SELECT ps.person_id, o.value_numeric AS cd4_result "
            + " FROM   person ps "
            + "       INNER JOIN encounter e "
            + "               ON ps.person_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + " INNER JOIN ( "
            + listOfPatientsOnAdvancedHivIllnessQueries
                .getPatientsWithCD4AbsoluteResultOnPeriodQuery(true)
            + " ) last_cd4 ON last_cd4.person_id = ps.person_id "
            + "WHERE  ps.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND ( ( ( e.encounter_type IN ( ${6}, ${13}, ${51} ) "
            + "                 AND o.concept_id = ${1695} ) "
            + "                OR ( e.encounter_type = ${90} "
            + "                     AND o.concept_id = ${165389} "
            + "                     AND o.value_coded = ${1695} ) ) "
            + "             AND o.value_numeric IS NOT NULL "
            + "             AND e.encounter_datetime = last_cd4.most_recent ) "
            + "       AND e.location_id = :location"
            + " UNION "
            + " SELECT ps.person_id, o.value_numeric AS cd4_result "
            + " FROM "
            + "    person ps INNER JOIN encounter e ON ps.person_id= e.patient_id "
            + "              INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + " INNER JOIN ( "
            + listOfPatientsOnAdvancedHivIllnessQueries
                .getPatientsWithCD4AbsoluteResultFichaResumoOnPeriodQuery(true)
            + " ) last_cd4 ON last_cd4.person_id = ps.person_id "
            + "WHERE ps.voided = 0 AND e.voided = 0 AND o.voided = 0 "
            + "  AND e.encounter_type = ${53} "
            + "  AND o.concept_id = ${1695} "
            + "  AND o.value_numeric IS NOT NULL "
            + "  AND o.obs_datetime = last_cd4.most_recent "
            + "  AND e.location_id = :location";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b> Data do Último CD4 </b>
   * <li>A data do registo mais recente de resultado de CD4 (absoluto) ocorrido até o fim do período
   *     de avaliação, registado na “Ficha Clínica – Ficha Mestra” ou “Ficha Resumo – Ficha Mestra”
   *     ou “Ficha e-Lab” ou “Ficha de Laboratório” ou “Ficha de Doença Avançada por HIV”
   *
   * @return {@link CohortDefinition}
   */
  public DataDefinition getLastCd4ResultDate() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Resultado do Último CD4 Absoluto");
    sqlPatientDataDefinition.addParameters(getCohortParameters());

    Map<String, Integer> valuesMap = getStringIntegerMap();

    EptsQueriesUtil eptsQueriesUtil = new EptsQueriesUtil();

    String query =
        eptsQueriesUtil
            .unionBuilder(
                listOfPatientsOnAdvancedHivIllnessQueries
                    .getPatientsWithCD4AbsoluteResultOnPeriodQuery(true))
            .union(
                listOfPatientsOnAdvancedHivIllnessQueries
                    .getPatientsWithCD4AbsoluteResultFichaResumoOnPeriodQuery(true))
            .buildQuery();

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b> Resultado do Penúltimo CD4 </b>
   * <li>O registo que antecede o registo mais recente de resultado de CD4 (absoluto) ocorrido até o
   *     fim do período de avaliação, registado na “Ficha Clínica – Ficha Mestra” ou “Ficha Resumo –
   *     Ficha Mestra” ou “Ficha e-Lab” ou “Ficha de Laboratório” ou “Ficha de Doença Avançada por
   *     HIV”
   *
   * @return {@link CohortDefinition}
   */
  public DataDefinition getLastCd4ResultBeforeMostRecentCd4() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Resultado do Penúltimo CD4 Absoluto");
    sqlPatientDataDefinition.addParameters(getCohortParameters());

    Map<String, Integer> valuesMap = getStringIntegerMap();

    String query =
        " SELECT result.person_id, result.value_numeric FROM ( "
            + listOfPatientsOnAdvancedHivIllnessQueries.getLastCd4OrResultDateBeforeMostRecentCd4()
            + " ) result ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b> Data do Resultado do Penúltimo CD4 </b>
   * <li>A data do registo que antecede o registo mais recente de resultado de CD4 (absoluto)
   *     ocorrido até o fim do período de avaliação, registado na “Ficha Clínica – Ficha Mestra” ou
   *     “Ficha Resumo – Ficha Mestra” ou “Ficha e-Lab” ou “Ficha de Laboratório” ou “Ficha de
   *     Doença Avançada por HIV”
   *
   * @return {@link CohortDefinition}
   */
  public DataDefinition getLastCd4ResultDateBeforeMostRecentCd4() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Data do Resultado do Penúltimo CD4 Absoluto");
    sqlPatientDataDefinition.addParameters(getCohortParameters());

    Map<String, Integer> valuesMap = getStringIntegerMap();

    String query =
        " SELECT result.person_id, result.second_cd4_result FROM ( "
            + listOfPatientsOnAdvancedHivIllnessQueries.getLastCd4OrResultDateBeforeMostRecentCd4()
            + " ) result ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>Resultado da Última Carga Viral</b>
   * <li>O registo mais recente de resultado do Carga Viral (quantitativo), na “Ficha Clínica –
   *     Ficha Mestra” ou “Ficha Resumo – Ficha Mestra” ou “Ficha e-Lab” ou “Ficha de Laboratório”,
   *     ocorrido até o fim do período de avaliação,
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getMostRecentVLResult() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Resultado da Última Carga Viral");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        " SELECT vl_result.patient_id, vl_result.viral_load FROM ( "
            + listOfPatientsOnAdvancedHivIllnessQueries.getVLoadResultAndMostRecent()
            + " ) AS vl_result GROUP BY vl_result.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>Data da Última Carga Viral</b>
   * <li>A data do registo mais recente de resultado de Carga Viral (quantitativo) na “Ficha Clínica
   *     – Ficha Mestra” ou “Ficha Resumo – Ficha Mestra” ou “Ficha e-Lab” ou “Ficha de
   *     Laboratório”, ocorrido até o fim do período de avaliação
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getMostRecentVLResultDate() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Data da Última Carga Viral");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        " SELECT result_date.patient_id, MAX(result_date.most_recent) FROM ( "
            + listOfPatientsOnAdvancedHivIllnessQueries.getVLoadResultAndMostRecent()
            + " ) AS result_date GROUP BY result_date.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>Resultado da Penúltima Carga Viral</b>
   * <li>O registro que antecede o registo mais recente de resultado de Carga Viral (quantitativo)
   *     na “Ficha Clínica – Ficha Mestra” ou “Ficha Resumo – Ficha Mestra” ou “Ficha e-Lab” ou
   *     “Ficha de Laboratório”, ocorrido até o fim do período de avaliação
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getLastVLResultBeforeMostRecentVLResultDate() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Resultado da Penúltima Carga Viral");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        " SELECT vl_result.patient_id, vl_result.viral_load FROM ( "
            + " SELECT p.patient_id, o.value_numeric AS viral_load, MAX(o.obs_datetime) AS most_recent FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "         INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "         INNER JOIN ( "
            + listOfPatientsOnAdvancedHivIllnessQueries.getVLoadResultAndMostRecent()
            + "         ) last_vl ON last_vl.patient_id = p.patient_id "
            + " WHERE e.encounter_type = ${53} "
            + " AND (o.concept_id = ${856} AND o.value_numeric IS NOT NULL) "
            + " AND o.obs_datetime < last_vl.most_recent "
            + " AND e.location_id = :location "
            + " AND e.voided = 0 "
            + " AND p.voided = 0 "
            + " AND o.voided = 0 "
            + " GROUP BY p.patient_id "
            + " UNION "
            + " SELECT p.patient_id, o.value_numeric AS viral_load, MAX(e.encounter_datetime) AS most_recent FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "         INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "         INNER JOIN ( "
            + listOfPatientsOnAdvancedHivIllnessQueries.getVLoadResultAndMostRecent()
            + "         ) last_vl ON last_vl.patient_id = p.patient_id "
            + " WHERE e.encounter_type IN(${6},${9},${13},${51}) "
            + " AND (o.concept_id = ${856} AND o.value_numeric IS NOT NULL) "
            + " AND e.encounter_datetime < last_vl.most_recent "
            + " AND e.location_id = :location "
            + " AND e.voided = 0 "
            + " AND p.voided = 0 "
            + " AND o.voided = 0 "
            + " GROUP BY p.patient_id "
            + " ) AS vl_result GROUP BY vl_result.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>Data da Penúltima Carga Viral</b>
   * <li>A data do registo que antecede o registo mais recente de Carga Viral (quantitativo) na
   *     “Ficha Clínica – Ficha Mestra” ou “Ficha Resumo – Ficha Mestra” ou “Ficha e-Lab” ou “Ficha
   *     de Laboratório”, ocorrido até o fim do período de avaliação
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getLastVLResultDateBeforeMostRecentVLResultDate() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Resultado da Penúltima Carga Viral");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        " SELECT result_date.patient_id, MAX(result_date.most_recent) FROM ( "
            + " SELECT p.patient_id, o.value_numeric AS viral_load, MAX(o.obs_datetime) AS most_recent FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "         INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "         INNER JOIN ( "
            + listOfPatientsOnAdvancedHivIllnessQueries.getVLoadResultAndMostRecent()
            + "         ) last_vl ON last_vl.patient_id = p.patient_id "
            + " WHERE e.encounter_type = ${53} "
            + " AND (o.concept_id = ${856} AND o.value_numeric IS NOT NULL) "
            + " AND o.obs_datetime < last_vl.most_recent "
            + " AND e.location_id = :location "
            + " AND e.voided = 0 "
            + " AND p.voided = 0 "
            + " AND o.voided = 0 "
            + " GROUP BY p.patient_id "
            + " UNION "
            + " SELECT p.patient_id, o.value_numeric AS viral_load, MAX(e.encounter_datetime) AS most_recent FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "         INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "         INNER JOIN ( "
            + listOfPatientsOnAdvancedHivIllnessQueries.getVLoadResultAndMostRecent()
            + "         ) last_vl ON last_vl.patient_id = p.patient_id "
            + " WHERE e.encounter_type IN(${6},${9},${13},${51}) "
            + " AND (o.concept_id = ${856} AND o.value_numeric IS NOT NULL) "
            + " AND e.encounter_datetime < last_vl.most_recent "
            + " AND e.location_id = :location "
            + " AND e.voided = 0 "
            + " AND p.voided = 0 "
            + " AND o.voided = 0 "
            + " GROUP BY p.patient_id "
            + " ) AS result_date GROUP BY result_date.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>Resultado do Última TB-LAM / Data do Último TB-LAM </b>
   * <li>O registo mais recente de resultado TB-LAM= “positivo, negativo, NA, NF”, na “Ficha Clínica
   *     –Ficha Mestra” ou “Ficha de Laboratório” ou “Ficha de Doença Avançada por HIV” ocorrido até
   *     o fim do período de avaliação. or
   *
   *     <p>A data do registo mais recente de resultado de TB-LAM , na “Ficha Clínica –Ficha Mestra”
   *     ou “Ficha de Laboratório” ou “Ficha de Doença Avançada por HIV” ocorrido até o fim do
   *     período de avaliação.
   *
   * @param encounterTypeList EncounterTypes to be evaluated
   * @param examConceptList Exam Concepts
   * @param resultConceptList Result Concepts
   * @param examResult Flag to return Exam result or Date
   * @return {@link DataDefinition}
   */
  public DataDefinition getTbLaboratoryResearchResults(
      List<Integer> encounterTypeList,
      List<Integer> examConceptList,
      List<Integer> resultConceptList,
      boolean examResult) {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Resultados de Investigacoes laboratoriais");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, String> valuesMap = new HashMap<>();
    valuesMap.put("encounterType", StringUtils.join(encounterTypeList, ","));
    valuesMap.put("examConcept", StringUtils.join(examConceptList, ","));
    valuesMap.put("resultConcept", StringUtils.join(resultConceptList, ","));

    String fromSQL =
        "  FROM ( "
            + " SELECT p.patient_id, o.value_coded, MAX(e.encounter_datetime) AS recent_date "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  e.encounter_type IN ( ${encounterType} ) "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id IN ( ${examConcept} ) "
            + "       AND o.value_coded IN ( ${resultConcept} ) "
            + "       AND e.encounter_datetime <= :endDate "
            + "       AND p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + " GROUP BY p.patient_id) exam_result ";

    String query =
        examResult
            ? " SELECT exam_result.patient_id, exam_result.value_coded ".concat(fromSQL)
            : " SELECT exam_result.patient_id, exam_result.recent_date ".concat(fromSQL);

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b> Utentes Transferidos para Outra US</b>
   * <li>utentes registados como ‘Suspensos’ (último estado de inscrição) no programa SERVIÇO TARV
   *     TRATAMENTO com “Data de Suspensão” <= “Data Fim” ou
   * <li>utentes com registo do último estado [“Mudança Estado Permanência TARV” (Coluna 21) = “T”
   *     (Transferido Para) na Ficha Clínica com “Data da Consulta Actual” (Coluna 1, durante a qual
   *     se fez o registo da mudança do estado de permanência TARV) <= “Data Fim”; ou
   * <li>utentes com último registo de “Mudança Estado Permanência TARV” = “Transferido Para” na
   *     Ficha Resumo com “Data da Transferência” <= “Data Fim”;
   *
   *     <p>excepto os utentes que tenham tido uma consulta clínica (Ficha Clínica) ou levantamento
   *     de ARV (FILA) após a “Data de Transferência” (a data mais recente entre os critérios acima
   *     identificados) e até “Data Fim”;
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsTransferredOutByTheEndOfPeriod() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName("get Patients Transferred Out by end of the period ");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put(
        "7",
        hivMetadata
            .getTransferredOutToAnotherHealthFacilityWorkflowState()
            .getProgramWorkflowStateId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());

    String query =
        listOfPatientsOnAdvancedHivIllnessQueries.getPatientsWhoSuspendedTarvOrAreTransferredOut(
            hivMetadata
                .getTransferredOutToAnotherHealthFacilityWorkflowState()
                .getProgramWorkflowStateId(),
            hivMetadata.getTransferredOutConcept().getConceptId(),
            true,
            true);

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b> Data de Último Levantamento TARV</b> Resposta = Sim, se o utente está
   * <li>inscrito como “Transferido de” (1o estado de inscrição) no serviço TARV-CUIDADOS (inscrição
   *     programa Pré-TARV) com “Data de Transferência” <= “Data Fim”; ou
   * <li>inscrito como “Transferido de” (1o estado de inscrição) no serviço TARV-TRATAMENTO
   *     (inscrição programa TARV) com “Data de Transferência” <= “Data Fim”; ou
   * <li>registado no formulário “Ficha de Resumo” como “Transferido de outra US”, opção “Pré-TARV”
   *     ou “TARV” selecionada; e com “Data de Abertura da Ficha na US” “<= “Data Fim”; Resposta=
   *     Não, se o utente não está nos criterios acima
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getPatientsTransferredInByTheEndOfPeriod() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();

    sqlPatientDataDefinition.setName("get Patients Transferred In by end of the period ");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("1", hivMetadata.getHIVCareProgram().getProgramId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put(
        "28",
        hivMetadata
            .getArtCareTransferredFromOtherHealthFacilityWorkflowState()
            .getProgramWorkflowStateId());
    map.put(
        "29",
        hivMetadata
            .getTransferredFromOtherHealthFacilityWorkflowState()
            .getProgramWorkflowStateId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6275", hivMetadata.getPreTarvConcept().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        " SELECT transferred_in.patient_id, transferred_in.transferred_date AS result"
            + " FROM (  "
            + "    SELECT p.patient_id , ps.start_date AS transferred_date  "
            + "    FROM patient p   "
            + "        INNER JOIN patient_program pg   "
            + "            ON p.patient_id=pg.patient_id   "
            + "        INNER JOIN patient_state ps   "
            + "            ON pg.patient_program_id=ps.patient_program_id   "
            + "    WHERE pg.voided=0   "
            + "        AND ps.voided=0   "
            + "        AND p.voided=0   "
            + "        AND pg.program_id IN (${1}, ${2} )  "
            + "        AND ps.state IN (${28}, ${29})   "
            + "        AND ps.end_date is null "
            + "        AND ps.start_date <= :endDate   "
            + "        AND pg.location_id= :location   "
            + "         GROUP BY p.patient_id  "
            + "    UNION  "
            + "    SELECT  e.patient_id , o3.obs_datetime AS transferred_date  "
            + "    FROM encounter e   "
            + "        INNER JOIN obs o   "
            + "            ON o.encounter_id=e.encounter_id   "
            + "        INNER JOIN obs o2   "
            + "            ON o2.encounter_id=e.encounter_id   "
            + "        INNER JOIN obs o3   "
            + "            ON o3.encounter_id=e.encounter_id   "
            + "    WHERE e.voided = 0   "
            + "        AND o.voided = 0   "
            + "        AND o2.voided = 0   "
            + "        AND o3.voided = 0   "
            + "        AND e.encounter_type = ${53}  "
            + "        AND ( o.concept_id = ${1369}  AND o.value_coded = ${1065} )  "
            + "        AND ( o2.concept_id = ${6300} AND o2.value_coded IN (${6275}, ${6276} ) ) "
            + "        AND ( o3.concept_id = ${23891} AND o3.obs_datetime <= :endDate ) "
            + "        AND e.location_id =  :location  "
            + "         GROUP BY e.patient_id  "
            + ") transferred_in   "
            + " GROUP BY transferred_in.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    String mappedQuery = stringSubstitutor.replace(query);

    sqlPatientDataDefinition.setQuery(mappedQuery);

    return sqlPatientDataDefinition;
  }

  /**
   * <b> Último Estado de Permanência TARV </b>
   * <li>
   *     Resposta = “Abandono”, os utentes em TARV que abandonaram o tratamento (DAH_RF24)
   * </li>
   * <li>
   *     Resposta = “Óbito”, os utentes em TARV que foram óbito (DAH_RF25)
   * </li>
   * <li>
   *    Resposta = “Suspenso”, os utentes em TARV que suspenderam o tratamento (DAH_RF24)
   * </li>
   * <li>
   *    Resposta = “Activo”, os utentes activos em TARV (DAH_RF26)
   * </li>
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getLastStateOfStayOnTarv() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();

    sqlPatientDataDefinition.setName("get Patients Transferred In by end of the period ");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));

    //    ADD CONCEPTS HERE
    Map<String, Integer> map = new HashMap<>();
    map.put("1", hivMetadata.getHIVCareProgram().getProgramId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put(
        "28",
        hivMetadata
            .getArtCareTransferredFromOtherHealthFacilityWorkflowState()
            .getProgramWorkflowStateId());
    map.put(
        "29",
        hivMetadata
            .getTransferredFromOtherHealthFacilityWorkflowState()
            .getProgramWorkflowStateId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6275", hivMetadata.getPreTarvConcept().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        new EptsQueriesUtil()
            .unionBuilder(
                listOfPatientsOnAdvancedHivIllnessQueries.getPatientsWhoAbandonedTarvQuery())
            .union(listOfPatientsOnAdvancedHivIllnessQueries.getPatientsWhoDied())
            .union(
                listOfPatientsOnAdvancedHivIllnessQueries
                    .getPatientsWhoSuspendedTarvOrAreTransferredOut(
                        hivMetadata
                            .getSuspendedTreatmentWorkflowState()
                            .getProgramWorkflowStateId(),
                        hivMetadata.getSuspendedTreatmentConcept().getConceptId(),
                        false,
                        false))
            .buildQuery();

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  private Map<String, Integer> getStringIntegerMap() {
    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("90", hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId());
    valuesMap.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    valuesMap.put("165389", hivMetadata.getCD4LabsetConcept().getConceptId());
    return valuesMap;
  }

  /** Add parameters to Cohort Definitions */
  private List<Parameter> getCohortParameters() {
    return Arrays.asList(
        ReportingConstants.START_DATE_PARAMETER,
        ReportingConstants.END_DATE_PARAMETER,
        ReportingConstants.LOCATION_PARAMETER);
  }
}
