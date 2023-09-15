package org.openmrs.module.eptsreports.reporting.library.cohorts.advancedhivillness;

import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ResumoMensalCohortQueries;
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

  private final TbMetadata tbMetadata;

  private final ListOfPatientsOnAdvancedHivIllnessQueries listOfPatientsOnAdvancedHivIllnessQueries;

  private final ResumoMensalCohortQueries resumoMensalCohortQueries;

  private final String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";

  @Autowired
  public ListOfPatientsInAdvancedHivIllnessCohortQueries(
      HivMetadata hivMetadata,
      CommonMetadata commonMetadata,
      TbMetadata tbMetadata,
      ListOfPatientsOnAdvancedHivIllnessQueries listOfPatientsOnAdvancedHivIllnessQueries,
      ResumoMensalCohortQueries resumoMensalCohortQueries) {
    this.hivMetadata = hivMetadata;
    this.commonMetadata = commonMetadata;
    this.tbMetadata = tbMetadata;
    this.listOfPatientsOnAdvancedHivIllnessQueries = listOfPatientsOnAdvancedHivIllnessQueries;
    this.resumoMensalCohortQueries = resumoMensalCohortQueries;
  }

  /**
   * <b>Lista de Utentes em seguimento do Modelo de DAH ou com critérios para iniciar o seguimento
   * do Modelo de DAH</b>
   * <li>Utentes que iniciaram o seguimento do Modelo de DAH OR
   * <li>Utentes com critério de CD4 para início de seguimento no Modelo de DAH OR
   * <li>Utentes com critério de Estadiamento para início de seguimento do Modelo dee DAH Excluindo
   *     todos os utentes que:
   * <li>tenham iniciado um Modelo de DAH antes da data de início do período de avaliação
   * <li>tenham sido transferidos para outra unidade sanitária até o fim do período de avaliação
   *
   * @see #getPatientsWhoStartedFollowupOnDAH(boolean) getPatientsWhoStartedFollowupOnDAH
   * @see #getPatientsWithCD4CriteriaToStartFollowupOnDAH()
   *     getPatientsWithCD4CriteriaToStartFollowupOnDAH
   * @see #getPatientsWithCriterioEstadiamentoInicioSeguimento()
   *     getPatientsWithCriterioEstadiamentoInicioSeguimento
   * @see #getPatientsWhoStartedFollowupOnDAH(boolean) getPatientsWhoStartedFollowupOnDAH
   * @see #getPatientsTransferredOutByTheEndOfPeriod() getPatientsTransferredOutByTheEndOfPeriod
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
   *     getPatientsWithCD4CriteriaToStartFollowupOnDAH
   * @see #getPatientsWithCriterioEstadiamentoInicioSeguimento()
   *     getPatientsWithCriterioEstadiamentoInicioSeguimento
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

    // EXCLUSIONS
    cd.addSearch(
        "FOLLOWUPBEFORESTARTDATE",
        EptsReportUtils.map(getPatientsWhoStartedFollowupOnDAH(false), mappings));

    cd.addSearch(
        "TRANSFERREDOUT",
        EptsReportUtils.map(
            getPatientsTransferredOutByTheEndOfPeriod(),
            "endDate=${endDate},location=${location}"));

    cd.setCompositionString("(CD4 OR ESTADIO) AND NOT (FOLLOWUPBEFORESTARTDATE OR TRANSFERREDOUT)");

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

    Map<String, Integer> map = new HashMap<>();
    map.put("90", hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId());

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

    StringSubstitutor substitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(substitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>DAH FR4 - Utentes com critério de CD4 para início de seguimento no Modelo de DAH </b>
   *
   * @see ListOfPatientsOnAdvancedHivIllnessQueries#getCd4ResultOverOrEqualTo5years()
   *     getCd4ResultOverOrEqualTo5years OR
   * @see ListOfPatientsOnAdvancedHivIllnessQueries#getCd4ResultBetweenOneAnd5years()
   *     getCd4ResultBetweenOneAnd5years OR
   * @see ListOfPatientsOnAdvancedHivIllnessQueries#getCd4ResultBellowOneYear()
   *     getCd4ResultBellowOneYear
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithCD4CriteriaToStartFollowupOnDAH() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        " Utentes com critério de CD4 para início de seguimento no Modelo de DAH");
    sqlCohortDefinition.addParameters(getCohortParameters());

    Map<String, Integer> map = getStringIntegerMap();

    String Cd4ResultOverOrEqualTo5yearsPid =
        new EptsQueriesUtil()
            .patientIdQueryBuilder(
                listOfPatientsOnAdvancedHivIllnessQueries.getCd4ResultOverOrEqualTo5years())
            .getQuery();

    String getCd4ResultBetweenOneAnd5yearsPid =
        new EptsQueriesUtil()
            .patientIdQueryBuilder(
                listOfPatientsOnAdvancedHivIllnessQueries.getCd4ResultBetweenOneAnd5years())
            .getQuery();

    String getCd4ResultBellowOneYearPid =
        new EptsQueriesUtil()
            .patientIdQueryBuilder(
                listOfPatientsOnAdvancedHivIllnessQueries.getCd4ResultBellowOneYear())
            .getQuery();

    String query =
        new EptsQueriesUtil()
            .unionBuilder(Cd4ResultOverOrEqualTo5yearsPid)
            .union(getCd4ResultBetweenOneAnd5yearsPid)
            .union(getCd4ResultBellowOneYearPid)
            .buildQuery();

    StringSubstitutor substitutor = new StringSubstitutor(map);

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

    Map<String, Integer> map = getIntegerMapForEstadioQueries();

    String query =
        "SELECT DISTINCT p.patient_id "
            + "FROM "
            + "    patient p INNER JOIN encounter e ON p.patient_id= e.patient_id "
            + "              INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "WHERE p.voided = 0 AND e.voided = 0 AND o.voided = 0 "
            + "  AND e.encounter_type = ${6} "
            + "  AND o.concept_id = ${1406} "
            + "  AND o.value_coded IN (${3},${42},${43},${60},${126},${507},${1294},${1570},${5018},${5042},${5334},${5344},${5340},${5945},${6783},${6990},${7180},${14656}) "
            + "  AND e.encounter_datetime >= :startDate "
            + "  AND e.encounter_datetime <= :endDate "
            + "  AND e.location_id = :location";

    StringSubstitutor substitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(substitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * @see ResumoMensalCohortQueries#getPatientStartedTarvBeforeQuery() getArtStartDateQuery
   * @return {@link DataDefinition}
   */
  public DataDefinition getARTStartDate() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Data Início TARV (1º levantamento)");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());

    String query = resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery();

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

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

    Map<String, Integer> map = new HashMap<>();
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    String query =
        " SELECT p.patient_id, MAX(e.encounter_datetime) pickup_date "
            + " FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + " WHERE e.encounter_type = ${18} "
            + " AND e.encounter_datetime >= :startDate "
            + " AND e.encounter_datetime <= :endDate "
            + " AND e.voided = 0 "
            + " AND p.voided = 0 "
            + " AND e.location_id = :location "
            + " GROUP BY p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b> Situação TARV no Início do Seguimento de DAH </b>
   * <li>O registo mais recente de “Situação de TARV” (Novo Inicio, Reinicio, em TARV, pré-TARV), na
   *     “Ficha de Doença Avançada por HIV”, registada durante o período de avaliação.
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getLastARVSituation() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Situação TARV no Início do Seguimento de DAH");
    sqlPatientDataDefinition.addParameters(getCohortParameters());

    Map<String, Integer> map = new HashMap<>();
    map.put("90", hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("1255", hivMetadata.getARVPlanConcept().getConceptId());
    map.put("1705", hivMetadata.getRestartConcept().getConceptId());
    map.put("6275", hivMetadata.getPreTarvConcept().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());

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
            + "                           AND e.encounter_datetime <= :startDate "
            + "                           AND e.encounter_datetime <= :endDate "
            + "                           AND e.location_id = :location "
            + "                         GROUP BY p.patient_id)followup ON followup.patient_id = e.patient_id "
            + "WHERE e.voided = 0 AND o.voided = 0 "
            + "  AND e.encounter_type = ${90} "
            + "  AND o.concept_id = ${1255} "
            + "  AND o.value_coded IN (${1256},${1705},${6275},${6276}) "
            + "  AND e.encounter_datetime = followup.start_date "
            + "  AND e.location_id = :location "
            + "GROUP BY e.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>Data de Início de Seguimento de DAH</b>
   * <li>A data mais recente de “Início de Seguimento no Modelo de Doença Avançada”, registada
   *     durante o período de avaliação, na “Ficha de Doença Avançada por HIV”
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getFollowupStartDateDAH() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Data de Início de Seguimento de DAH");
    sqlPatientDataDefinition.addParameters(getCohortParameters());

    Map<String, Integer> map = new HashMap<>();
    map.put("90", hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId());

    String query =
        " SELECT p.patient_id, MAX(e.encounter_datetime) AS most_recent "
            + "FROM "
            + "    patient p INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "WHERE p.voided = 0 AND e.voided = 0 "
            + "  AND e.encounter_type = ${90} "
            + "  AND e.encounter_datetime >= :startDate "
            + "  AND e.encounter_datetime <= :endDate "
            + "  AND e.location_id = :location "
            + "GROUP BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(substitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>Último Registo de Estadiamento Clínico (Data de Registo )</b>
   *
   * @see ListOfPatientsOnAdvancedHivIllnessQueries#getFirstEstadioQuery() getFirstEstadioQuery
   * @return {@link DataDefinition}
   */
  public DataDefinition getDateOfEstadioByTheEndOfPeriod() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(" Data de Registo de Estadio");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = getIntegerMapForEstadioQueries();

    String query = listOfPatientsOnAdvancedHivIllnessQueries.getFirstEstadioQuery();

    StringSubstitutor substitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(substitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>Estadiamento Clínico</b>
   * <li>Resposta = Estadio IV, caso exista o registo de infecção representativa de Estadio IV
   *     registada na Ficha Clinica – Ficha Mestra (DAH_RF28) até o fim do período de avaliação.
   * <li>Resposta = Estadio III, caso não exista registo de infecção representativa de Estadio IV
   *     (DAH_RF28) até o fim do período de avaliação e exista registo de infecção representativa de
   *     Estadio III (DAH_RF27) até o fim do período de avaliação.
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getResultOfEstadioByTheEndOfPeriod() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Infecções Estadio OMS");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = getIntegerMapForEstadioQueries();

    String query =
        "SELECT e.patient_id, 'ESTADIO IV' "
            + "FROM encounter e "
            + "         INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "         INNER JOIN     ( "
            + listOfPatientsOnAdvancedHivIllnessQueries.getFirstEstadioQuery()
            + ")first_consultation ON first_consultation.patient_id = e.patient_id "
            + "WHERE e.voided = 0 AND o.voided = 0 "
            + "  AND e.encounter_type = ${6} "
            + "  AND o.concept_id = ${1406} "
            + "  AND o.value_coded IN (${507},${1294},${1570},${5042},${5334},${5344},${5340},${6990},${7180},${14656}) "
            + "  AND e.encounter_datetime = first_consultation.consultation_date "
            + "  AND e.location_id = :location "
            + "GROUP BY e.patient_id"
            + " UNION "
            + " SELECT e.patient_id, 'ESTADIO III' "
            + "FROM encounter e "
            + "         INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "         INNER JOIN ( "
            + listOfPatientsOnAdvancedHivIllnessQueries.getFirstEstadioQuery()
            + ")first_consultation ON first_consultation.patient_id = e.patient_id "
            + "WHERE e.voided = 0 AND o.voided = 0 "
            + "  AND e.encounter_type = ${6} "
            + "  AND o.concept_id = ${1406} "
            + "  AND o.value_coded IN (${3},${42},${43},${60},${126},${5018},${5334},${5945},${6783}) "
            + "  AND e.encounter_datetime = first_consultation.consultation_date "
            + "  AND e.location_id = :location "
            + "GROUP BY e.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(substitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b> A data do registo do resultado de CD4 (absoluto) durante o período de avaliação </b>
   *
   * @see ListOfPatientsOnAdvancedHivIllnessQueries#getCd4ResultOverOrEqualTo5years()
   *     getCd4ResultOverOrEqualTo5years OR
   * @see ListOfPatientsOnAdvancedHivIllnessQueries#getCd4ResultBetweenOneAnd5years()
   *     getCd4ResultBetweenOneAnd5years OR
   * @see ListOfPatientsOnAdvancedHivIllnessQueries#getCd4ResultBellowOneYear()
   *     getCd4ResultBellowOneYear
   * @return {@link CohortDefinition}
   */
  public DataDefinition getCd4ResultDate() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Data de resultado de CD4 Absoluto");
    sqlPatientDataDefinition.addParameters(getCohortParameters());

    Map<String, Integer> map = getStringIntegerMap();

    String query =
        new EptsQueriesUtil()
            .unionBuilder(
                listOfPatientsOnAdvancedHivIllnessQueries.getCd4ResultOverOrEqualTo5years())
            .union(listOfPatientsOnAdvancedHivIllnessQueries.getCd4ResultBetweenOneAnd5years())
            .union(listOfPatientsOnAdvancedHivIllnessQueries.getCd4ResultBellowOneYear())
            .buildQuery();

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b> Resultado de CD4 (absoluto) durante o período de avaliação </b>
   *
   * @see ListOfPatientsOnAdvancedHivIllnessQueries#getCd4ResultOverOrEqualTo5years()
   *     getCd4ResultOverOrEqualTo5years OR
   * @see ListOfPatientsOnAdvancedHivIllnessQueries#getCd4ResultBetweenOneAnd5years()
   *     getCd4ResultBetweenOneAnd5years OR
   * @see ListOfPatientsOnAdvancedHivIllnessQueries#getCd4ResultBellowOneYear()
   *     getCd4ResultBellowOneYear
   * @return {@link CohortDefinition}
   */
  public DataDefinition getCd4Result() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Resultado de CD4 Absoluto");
    sqlPatientDataDefinition.addParameters(getCohortParameters());

    Map<String, Integer> map = getStringIntegerMap();

    String query =
        new EptsQueriesUtil()
            .unionBuilder(listOfPatientsOnAdvancedHivIllnessQueries.getCd4ResultOverOrEqualTo5y())
            .union(listOfPatientsOnAdvancedHivIllnessQueries.getCd4ResultBetweenOneAnd5y())
            .union(listOfPatientsOnAdvancedHivIllnessQueries.getCd4ResultBellow1y())
            .buildQuery();

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>Motivo de Mudança de Estadiamento Clínico - 1</b>
   * <li>Caso exista registo na Ficha Clinica – Ficha Mestra de infecção representativa de Estadio
   *     IV até o fim do período de avaliação, o sistema irá listar a primeira infecção
   *     representativa de Estadio IV até o fim do período de avaliação.
   * <li>Caso não exista registo de infecção representativa na Ficha Clinica – Ficha Mestra de
   *     Estadio IV até o fim do período de avaliação, e exista o registo de infecção representativa
   *     de Estadio III, o sistema irá listar a primeira infecção representativa de Estadio IIIaté o
   *     fim do período de avaliação.
   *
   * @see #getResultOfEstadioByTheEndOfPeriod() getResultOfEstadioByTheEndOfPeriod
   * @return {@link DataDefinition}
   */
  public DataDefinition getReasonToChangeEstadio1() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Motivo de Mudança de Estadiamento Clínico - 1");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = getIntegerMapForEstadioQueries();

    String query =
        " SELECT e.patient_id, o.value_coded AS estadio "
            + "FROM encounter e "
            + "         INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "         INNER JOIN     ( "
            + listOfPatientsOnAdvancedHivIllnessQueries.getFirstEstadioQuery()
            + ")first_consultation ON first_consultation.patient_id = e.patient_id "
            + "WHERE e.voided = 0 AND o.voided = 0 "
            + "  AND e.encounter_type = ${6} "
            + "  AND o.concept_id = ${1406} "
            + "  AND o.value_coded IN (${60},${507},${1294},${1570},${5042},${5334},${5344},${5340},${6990},${7180},${14656}) "
            + "  AND e.encounter_datetime = first_consultation.consultation_date "
            + "  AND e.location_id = :location "
            + "GROUP BY e.patient_id"
            + " UNION "
            + " SELECT e.patient_id, o.value_coded AS estadio "
            + "FROM encounter e "
            + "         INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "         INNER JOIN ( "
            + listOfPatientsOnAdvancedHivIllnessQueries.getFirstEstadioQuery()
            + ")first_consultation ON first_consultation.patient_id = e.patient_id "
            + "WHERE e.voided = 0 AND o.voided = 0 "
            + "  AND e.encounter_type = ${6} "
            + "  AND o.concept_id = ${1406} "
            + "  AND o.value_coded IN (${3},${42},${43},${126},${1570},${5018},${5334},${5945},${6783}) "
            + "  AND e.encounter_datetime = first_consultation.consultation_date "
            + "  AND e.location_id = :location "
            + "GROUP BY e.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(substitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>Motivo de Mudança de Estadiamento Clínico - 2</b>
   * <li>Caso exista registo de infecção representativa de Estadio IV Ficha Clinica – Ficha Mestra
   *     (DAH_RF28) até o fim do período de avaliação, o sistema irá listar a segunda infecção
   *     representativa de Estadio IV (DAH_RF28) até o fim do período de avaliação.
   * <li>Caso não exista registo de infecção na Ficha Clinica – Ficha Mestra representativa de
   *     Estadio IV (DAH_RF28) até o fim do período de avaliação, e exista o registo de infecção
   *     representativa de Estadio III (DAH_RF27), o sistema irá listar a segunda infecção
   *     representativa de Estadio III (DAH_RF27) até o fim do período de avaliação.
   *
   * @see #getDateOfEstadioByTheEndOfPeriod() getDateOfEstadioByTheEndOfPeriod
   * @return {@link DataDefinition}
   */
  public DataDefinition getReasonToChangeEstadio2() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Motivo de Mudança de Estadiamento Clínico - 2");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = getIntegerMapForEstadioQueries();

    String query =
        " SELECT e.patient_id, "
            + "(SELECT o.value_coded "
            + "           FROM obs o WHERE o.encounter_id = first_consultation.encounter_id "
            + "           AND o.concept_id = 1406 AND o.voided = 0 "
            + "           LIMIT 1,1 ) AS estadio "
            + "FROM encounter e "
            + "         INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "         INNER JOIN     ( "
            + listOfPatientsOnAdvancedHivIllnessQueries.getFirstEstadioQueryWithEncounterId()
            + ")first_consultation ON first_consultation.patient_id = e.patient_id "
            + "WHERE e.voided = 0 AND o.voided = 0 "
            + "  AND e.encounter_type = ${6} "
            + "  AND o.concept_id = ${1406} "
            + "  AND o.value_coded IN (${60},${507},${1294},${1570},${5042},${5334},${5344},${5340},${6990},${7180},${14656}) "
            + "  AND e.encounter_datetime = first_consultation.consultation_date "
            + "  AND e.location_id = :location "
            + " GROUP BY e.patient_id"
            + " UNION "
            + " SELECT e.patient_id, "
            + "(SELECT o.value_coded "
            + "           FROM obs o WHERE o.encounter_id = first_consultation.encounter_id "
            + "           AND o.concept_id = ${1406} AND o.voided = 0 "
            + "           AND o.value_coded IN (${3},${42},${43},${126},${1570},${5018},${5334},${5945},${6783}) "
            + "           LIMIT 1,1 ) AS estadio "
            + "FROM encounter e "
            + "         INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "         INNER JOIN ( "
            + listOfPatientsOnAdvancedHivIllnessQueries.getFirstEstadioQueryWithEncounterId()
            + ")first_consultation ON first_consultation.patient_id = e.patient_id "
            + "WHERE e.voided = 0 AND o.voided = 0 "
            + "  AND e.encounter_type = ${6} "
            + "  AND o.concept_id = ${1406} "
            + "  AND o.value_coded IN (${3},${42},${43},${126},${1570},${5018},${5334},${5945},${6783}) "
            + "  AND e.encounter_datetime = first_consultation.consultation_date "
            + "  AND e.location_id = :location "
            + "GROUP BY e.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(map);

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

    Map<String, Integer> map = getStringIntegerMap();

    String query =
        " SELECT ps.person_id, o.value_numeric AS cd4_result "
            + " FROM   person ps "
            + "       INNER JOIN encounter e "
            + "               ON ps.person_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + " INNER JOIN ( "
            + " SELECT result.person_id, Max(result.most_recent) AS most_recent FROM ( "
            + new EptsQueriesUtil()
                .unionBuilder(
                    listOfPatientsOnAdvancedHivIllnessQueries
                        .getPatientsWithCD4AbsoluteResultOnPeriodQuery(true))
                .union(
                    listOfPatientsOnAdvancedHivIllnessQueries
                        .getPatientsWithCD4AbsoluteResultFichaResumoOnPeriodQuery(true))
                .buildQuery()
            + " ) result GROUP BY result.person_id "
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
            + "             AND DATE(e.encounter_datetime) = last_cd4.most_recent ) "
            + "       AND e.location_id = :location"
            + " UNION "
            + " SELECT ps.person_id, o.value_numeric AS cd4_result "
            + " FROM "
            + "    person ps INNER JOIN encounter e ON ps.person_id= e.patient_id "
            + "              INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + " INNER JOIN ( "
            + " SELECT result.person_id, Max(result.most_recent) AS most_recent FROM ( "
            + new EptsQueriesUtil()
                .unionBuilder(
                    listOfPatientsOnAdvancedHivIllnessQueries
                        .getPatientsWithCD4AbsoluteResultOnPeriodQuery(true))
                .union(
                    listOfPatientsOnAdvancedHivIllnessQueries
                        .getPatientsWithCD4AbsoluteResultFichaResumoOnPeriodQuery(true))
                .buildQuery()
            + " ) result GROUP BY result.person_id "
            + " ) last_cd4 ON last_cd4.person_id = ps.person_id "
            + "WHERE ps.voided = 0 AND e.voided = 0 AND o.voided = 0 "
            + "  AND e.encounter_type = ${53} "
            + "  AND o.concept_id = ${1695} "
            + "  AND o.value_numeric IS NOT NULL "
            + "  AND o.obs_datetime = last_cd4.most_recent "
            + "  AND e.location_id = :location";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

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

    Map<String, Integer> map = getStringIntegerMap();

    String query =
        " SELECT result.person_id, Max(result.most_recent) AS most_recent FROM ( "
            + new EptsQueriesUtil()
                .unionBuilder(
                    listOfPatientsOnAdvancedHivIllnessQueries
                        .getPatientsWithCD4AbsoluteResultOnPeriodQuery(true))
                .union(
                    listOfPatientsOnAdvancedHivIllnessQueries
                        .getPatientsWithCD4AbsoluteResultFichaResumoOnPeriodQuery(true))
                .buildQuery()
            + " ) result GROUP BY result.person_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

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

    Map<String, Integer> map = getStringIntegerMap();

    String query =
        " SELECT result.person_id, result.value_numeric FROM ( "
            + listOfPatientsOnAdvancedHivIllnessQueries.getLastCd4OrResultDateBeforeMostRecentCd4()
            + " ) result ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

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

    Map<String, Integer> map = getStringIntegerMap();

    String query =
        " SELECT result.person_id, MAX(result.second_cd4_result) FROM ( "
            + listOfPatientsOnAdvancedHivIllnessQueries.getLastCd4OrResultDateBeforeMostRecentCd4()
            + " ) result GROUP BY result.person_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

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

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        " SELECT vl_result.patient_id, vl_result.viral_load FROM ( "
            + listOfPatientsOnAdvancedHivIllnessQueries.getVLoadResultAndMostRecent()
            + " ) AS vl_result GROUP BY vl_result.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

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

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        " SELECT result_date.patient_id, MAX(result_date.most_recent) FROM ( "
            + listOfPatientsOnAdvancedHivIllnessQueries.getVLoadResultAndMostRecent()
            + " ) AS result_date GROUP BY result_date.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

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

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        " SELECT second_result.patient_id, second_result.viral_load FROM ( "
            + listOfPatientsOnAdvancedHivIllnessQueries
                .getSecondVLResultOrResultDateBeforeMostRecent()
            + " ) AS second_result GROUP BY second_result.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

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

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        " SELECT second_result_date.patient_id, MAX(second_result_date.second_vl) FROM ( "
            + listOfPatientsOnAdvancedHivIllnessQueries
                .getSecondVLResultOrResultDateBeforeMostRecent()
            + " ) AS second_result_date GROUP BY second_result_date.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

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
      List<EncounterType> encounterTypeList,
      List<Concept> examConceptList,
      List<Concept> resultConceptList,
      boolean examResult) {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Resultados de Investigacoes laboratoriais");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    List<Integer> encounterTypeIdsList =
        encounterTypeList.stream()
            .map(EncounterType::getEncounterTypeId)
            .collect(Collectors.toList());

    List<Integer> examConceptIdsList =
        examConceptList.stream().map(Concept::getConceptId).collect(Collectors.toList());

    List<Integer> resultConceptIdsList =
        resultConceptList.stream().map(Concept::getConceptId).collect(Collectors.toList());

    Map<String, String> map = new HashMap<>();
    map.put("encounterType", StringUtils.join(encounterTypeIdsList, ","));
    map.put("examConcept", StringUtils.join(examConceptIdsList, ","));
    map.put("resultConcept", StringUtils.join(resultConceptIdsList, ","));

    String fromSQL =
        "  FROM ( "
            + " SELECT p.patient_id, o.value_coded, MAX(DATE(e.encounter_datetime)) AS recent_date "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  e.encounter_type IN ( ${encounterType} ) "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id IN ( ${examConcept} ) "
            + "       AND o.value_coded IN ( ${resultConcept} ) "
            + "       AND DATE(e.encounter_datetime) <= :endDate "
            + "       AND p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + " GROUP BY p.patient_id,o.value_coded) exam_result ";

    String query =
        examResult
            ? " SELECT exam_result.patient_id, exam_result.value_coded ".concat(fromSQL)
            : " SELECT exam_result.patient_id, exam_result.recent_date ".concat(fromSQL);

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

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
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());

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
        "SELECT transferred_in.patient_id, "
            + "       transferred_in.transferred_date AS result "
            + "FROM   (SELECT p.patient_id, "
            + "               transferred_date "
            + "        FROM   patient p "
            + "               INNER JOIN patient_program pg ON p.patient_id = pg.patient_id "
            + "               INNER JOIN patient_state ps ON pg.patient_program_id = ps.patient_program_id "
            + "               INNER JOIN(SELECT p.patient_id, "
            + "                                 Min(ps.start_date) AS transferred_date "
            + "                          FROM   patient p "
            + "                                 INNER JOIN patient_program pg ON p.patient_id = pg.patient_id "
            + "                                 INNER JOIN patient_state ps    ON   pg.patient_program_id = ps.patient_program_id "
            + "                          WHERE  pg.voided = 0 "
            + "                                 AND ps.voided = 0 "
            + "                                 AND p.voided = 0 "
            + "                                 AND pg.program_id IN ( ${1}, ${2} ) "
            + "                                 AND ps.start_date <= :endDate "
            + "                                 AND pg.location_id = :location "
            + "                          GROUP  BY p.patient_id) pps "
            + "                       ON pps.patient_id = p.patient_id "
            + "        WHERE  pg.voided = 0 "
            + "               AND ps.voided = 0 "
            + "               AND p.voided = 0 "
            + "               AND pg.program_id IN ( ${1}, ${2} ) "
            + "               AND ps.state IN ( ${28}, ${29} ) "
            + "               AND ps.start_date = pps. transferred_date "
            + "               AND pg.location_id = :location "
            + "        GROUP  BY p.patient_id "
            + "        UNION "
            + "        SELECT e.patient_id, "
            + "               o3.obs_datetime AS transferred_date "
            + "        FROM   encounter e "
            + "               INNER JOIN obs o  ON o.encounter_id = e.encounter_id "
            + "               INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "               INNER JOIN obs o3 ON o3.encounter_id = e.encounter_id "
            + "        WHERE  e.voided = 0 "
            + "               AND o.voided = 0 "
            + "               AND o2.voided = 0 "
            + "               AND o3.voided = 0 "
            + "               AND e.encounter_type = ${53} "
            + "               AND ( o.concept_id = ${1369} AND o.value_coded = ${1065} ) "
            + "               AND ( o2.concept_id = ${6300} "
            + "                     AND o2.value_coded IN ( ${6275}, ${6276} ) ) "
            + "               AND ( o3.concept_id = ${23891} AND o3.obs_datetime <= :endDate ) "
            + "               AND e.location_id = :location "
            + "        GROUP  BY e.patient_id) transferred_in "
            + "GROUP  BY transferred_in.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    String mappedQuery = stringSubstitutor.replace(query);

    sqlPatientDataDefinition.setQuery(mappedQuery);

    return sqlPatientDataDefinition;
  }

  /**
   * <b> Último Estado de Permanência TARV </b>
   * <li>Resposta = “Abandono”, os utentes em TARV que abandonaram o tratamento
   * <li>Resposta = “Óbito”, os utentes em TARV que foram óbito
   * <li>Resposta = “Suspenso”, os utentes em TARV que suspenderam o tratamento
   * <li>Resposta = “Activo”, os utentes activos em TARV
   *
   * @see ListOfPatientsOnAdvancedHivIllnessQueries#getPatientsWhoAbandonedTarvQuery(boolean)
   *     getPatientsWhoAbandonedTarvQuery
   * @see ListOfPatientsOnAdvancedHivIllnessQueries#getPatientsWhoDied(boolean) getPatientsWhoDied
   * @see
   *     ListOfPatientsOnAdvancedHivIllnessQueries#getPatientsWhoSuspendedTarvOrAreTransferredOut(int,
   *     int, boolean, boolean) getPatientsWhoSuspendedTarvOrAreTransferredOut
   * @see ListOfPatientsOnAdvancedHivIllnessQueries#getPatientsActiveOnTarv()
   *     getPatientsActiveOnTarv
   * @return {@link DataDefinition}
   */
  public DataDefinition getLastStateOfStayOnTarv() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();

    sqlPatientDataDefinition.setName("Get the Last State of stay ");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("1", hivMetadata.getHIVCareProgram().getProgramId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("10", hivMetadata.getArtDeadWorkflowState().getProgramWorkflowStateId());
    map.put("8", hivMetadata.getSuspendedTreatmentWorkflowState().getProgramWorkflowStateId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
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
    map.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6275", hivMetadata.getPreTarvConcept().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1366", hivMetadata.getPatientHasDiedConcept().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("1709", hivMetadata.getSuspendedTreatmentConcept().getConceptId());

    String query =
        new EptsQueriesUtil()
            .unionBuilder(
                listOfPatientsOnAdvancedHivIllnessQueries.getPatientsWhoAbandonedTarvQuery(true))
            .union(listOfPatientsOnAdvancedHivIllnessQueries.getPatientsWhoDied(true))
            .union(
                listOfPatientsOnAdvancedHivIllnessQueries
                    .getPatientsWhoSuspendedTarvOrAreTransferredOut(
                        hivMetadata
                            .getSuspendedTreatmentWorkflowState()
                            .getProgramWorkflowStateId(),
                        hivMetadata.getSuspendedTreatmentConcept().getConceptId(),
                        false,
                        false))
            .union(listOfPatientsOnAdvancedHivIllnessQueries.getPatientsActiveOnTarv())
            .buildQuery();

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>Get the NID of the patient on Demografic Module</b>
   * <li>Nota: para os utentes com mais de uma informação demográfica (NID) e nenhum marcado como
   *     preferencial, o sistema irá seleccionar a informação mais recente (não anulada e
   *     disponível) registada no módulo demográfico
   *
   * @param identifierType the identifier type id for NID
   * @return {@link DataDefinition}
   */
  public DataDefinition getNID(int identifierType) {
    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();
    spdd.setName("Get Patient NID");

    String sql =
        " SELECT p.patient_id,pi.identifier  FROM patient p INNER JOIN patient_identifier pi ON p.patient_id=pi.patient_id "
            + " INNER JOIN patient_identifier_type pit ON pit.patient_identifier_type_id=pi.identifier_type "
            + " WHERE p.voided=0 AND pi.voided=0 AND pit.retired=0 AND pi.preferred = 1 AND pit.patient_identifier_type_id ="
            + identifierType
            + " GROUP BY p.patient_id"
            + " UNION "
            + " SELECT p.patient_id,pi.identifier  FROM patient p INNER JOIN patient_identifier pi ON p.patient_id=pi.patient_id "
            + " INNER JOIN patient_identifier_type pit ON pit.patient_identifier_type_id=pi.identifier_type "
            + " WHERE p.voided=0 AND pi.voided=0 AND pit.retired=0 AND pit.patient_identifier_type_id ="
            + identifierType
            + " GROUP BY p.patient_id";

    spdd.setQuery(sql);
    return spdd;
  }

  private Map<String, Integer> getStringIntegerMap() {
    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("90", hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("165389", hivMetadata.getCD4LabsetConcept().getConceptId());
    return map;
  }

  private Map<String, Integer> getIntegerMapForEstadioQueries() {
    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1406", hivMetadata.getOtherDiagnosis().getConceptId());
    map.put("5018", hivMetadata.getChronicDiarrheaConcept().getConceptId());
    map.put("3", hivMetadata.getAnemiaConcept().getConceptId());
    map.put("5945", hivMetadata.getFeverConcept().getConceptId());
    map.put("43", hivMetadata.getPneumoniaConcept().getConceptId());
    map.put("60", hivMetadata.getMeningitisConcept().getConceptId());
    map.put("126", hivMetadata.getGingivitisConcept().getConceptId());
    map.put("6783", hivMetadata.getEstomatiteUlcerativaNecrotizanteConcept().getConceptId());
    map.put("5334", hivMetadata.getCandidiaseOralConcept().getConceptId());
    map.put("1294", hivMetadata.getCryptococcalMeningitisConcept().getConceptId());
    map.put("1570", hivMetadata.getCervicalCancerConcept().getConceptId());
    map.put("5340", hivMetadata.getCandidiaseEsofagicaConcept().getConceptId());
    map.put("5344", hivMetadata.getHerpesSimplesConcept().getConceptId());
    map.put("14656", hivMetadata.getCachexiaConcept().getConceptId());
    map.put("7180", hivMetadata.getToxoplasmoseConcept().getConceptId());
    map.put("6990", hivMetadata.getHivDiseaseResultingInEncephalopathyConcept().getConceptId());
    map.put("507", commonMetadata.getKaposiSarcomaConcept().getConceptId());
    map.put("5042", tbMetadata.getExtraPulmonaryTbConcept().getConceptId());
    map.put("42", tbMetadata.getPulmonaryTB().getConceptId());
    return map;
  }

  /** Add parameters to Cohort Definitions */
  private List<Parameter> getCohortParameters() {
    return Arrays.asList(
        ReportingConstants.START_DATE_PARAMETER,
        ReportingConstants.END_DATE_PARAMETER,
        ReportingConstants.LOCATION_PARAMETER);
  }
}
