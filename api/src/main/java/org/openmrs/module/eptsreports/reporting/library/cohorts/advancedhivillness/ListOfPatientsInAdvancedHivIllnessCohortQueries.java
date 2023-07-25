package org.openmrs.module.eptsreports.reporting.library.cohorts.advancedhivillness;

import java.util.*;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
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
  String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";

  @Autowired
  public ListOfPatientsInAdvancedHivIllnessCohortQueries(HivMetadata hivMetadata) {
    this.hivMetadata = hivMetadata;
  }

  /**
   * <b>Lista de Utentes em seguimento do Modelo de DAH ou com critérios para iniciar o seguimento
   * do Modelo de DAH</b>
   * <li>Utentes que iniciaram o seguimento do Modelo de DAH OR
   * <li>Utentes com critério de CD4 para início de seguimento no Modelo de DAH OR
   * <li>Utentes com critério de Estadiamento para início de seguimento do Modelo dee DAH
   *
   * @see #getPatientsWhoStartedFollowupOnDAH()
   * @see #getPatientsWithCD4CriteriaToStartFollowupOnDAH()
   * @see #getPatientsWithCriterioEstadiamentoInicioSeguimento()
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsOnFollowupOrWithCriteriaToStartFollowupOfDAH() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName(
        "Lista de Utentes em seguimento do Modelo de DAH ou com critérios para iniciar o seguimento do Modelo de DA");
    cd.addParameters(getCohortParameters());

    cd.addSearch(
        "STARTEDFOLLOWUP", EptsReportUtils.map(getPatientsWhoStartedFollowupOnDAH(), mappings));

    cd.addSearch(
        "CD4", EptsReportUtils.map(getPatientsWithCD4CriteriaToStartFollowupOnDAH(), mappings));

    cd.addSearch(
        "ESTADIO",
        EptsReportUtils.map(getPatientsWithCriterioEstadiamentoInicioSeguimento(), mappings));

    cd.setCompositionString("STARTEDFOLLOWUP OR CD4 OR ESTADIO");

    return cd;
  }

  /**
   * <b>DAH FR3 - Utentes que iniciaram o seguimento no Modelo de DAH </b>
   *
   * <p>Utentes com registo do “Início de Seguimento no Modelo de Doença Avançada” (Data Início DAH
   * - encounter_datetime) na Ficha de DAH (encounter type = 90) durante o período de avaliação, ou
   * seja, “Data Início DAH” >= “Data Início Avaliação” e “Data Início DAH” <= “Data Fim Avaliação”
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoStartedFollowupOnDAH() {

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
            + "  AND e.encounter_type = ${90} "
            + "  AND e.encounter_datetime >= :startDate "
            + "  AND e.encounter_datetime <= :endDate "
            + "  AND e.location_id = :location "
            + "GROUP BY p.patient_id";

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

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("90", hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId());
    valuesMap.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    valuesMap.put("165389", hivMetadata.getCD4LabsetConcept().getConceptId());

    EptsQueriesUtil eptsQueriesUtil = new EptsQueriesUtil();

    String query =
        eptsQueriesUtil
            .unionBuilder(
                ListOfPatientsOnAdvancedHivIllnessQueries.getCd4ResultOverOrEqualTo5years())
            .union(ListOfPatientsOnAdvancedHivIllnessQueries.getCd4ResultBetweenOneAnd5years())
            .union(ListOfPatientsOnAdvancedHivIllnessQueries.getCd4ResultBellowOneYear())
            .buildQuery();

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(substitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>Utentes com critério de Estadiamento para início de seguimento do Modelo dee DAH</b>
   *
   * @see #getPatientsWithEstadioOnPeriod() OR
   * @see #getPatientsWithOpportunisticDiseases()
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithCriterioEstadiamentoInicioSeguimento() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Utentes com critério de Estadiamento para início de seguimento do Modelo dee DAH");
    cd.addParameters(getCohortParameters());

    cd.addSearch("ESTADIO", EptsReportUtils.map(getPatientsWithEstadioOnPeriod(), mappings));

    cd.addSearch("DISEASES", EptsReportUtils.map(getPatientsWithOpportunisticDiseases(), mappings));

    cd.setCompositionString("ESTADIO OR DISEASES");

    return cd;
  }

  /**
   *
   * <li>Utentes com primeiro registo de “Estadio III” ou primeiro registo de “Estadio IV” decorrido
   *     durante o período de avaliação (“Data Consulta” >= “Data Início Avaliação” e “Data
   *     Consulta” <= “Data Fim Avaliação”)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithEstadioOnPeriod() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" Utentes com  Estadio III ou Estadio IV");
    sqlCohortDefinition.addParameters(getCohortParameters());

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("90", hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId());
    valuesMap.put("1206", hivMetadata.getWho3AdultStageConcept().getConceptId());
    valuesMap.put("1207", hivMetadata.getWho4AdultStageConcept().getConceptId());
    valuesMap.put("5356", hivMetadata.getcurrentWhoHivStageConcept().getConceptId());

    String query =
        "SELECT estadio.patient_id "
            + "FROM   (SELECT p.patient_id, "
            + "               Min(e.encounter_datetime) AS most_recent "
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
            + "        GROUP  BY p.patient_id) estadio";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(substitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   *
   * <li>Utentes com pelo menos um registo de “Infecções Oportunistas” da seguinte lista
   *     representativa de Estadio III ou Estadio IV, na “Ficha Clínica – Ficha Mestra” durante o
   *     período de avaliação (“Data Consulta” >= “Data Início Avaliação” e “Data Consulta” <= “Data
   *     Fim Avaliação”)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithOpportunisticDiseases() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" Utentes com Infecções Oportunistas");

    sqlCohortDefinition.addParameters(getCohortParameters());

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("1406", hivMetadata.getOtherDiagnosis().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM "
            + "    patient p INNER JOIN encounter e ON p.patient_id= e.patient_id "
            + "              INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "WHERE p.voided = 0 AND e.voided = 0 AND o.voided = 0 "
            + "  AND e.encounter_type = ${6} "
            + "  AND o.concept_id = ${1406} "
            + "  AND o.value_coded IS NOT NULL "
            + "  AND e.encounter_datetime >= :startDate "
            + "  AND e.encounter_datetime <= :endDate "
            + "  AND e.location_id = :location";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(substitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b> Data Início TARV (1º levantamento)</b>
   * <li>1º levantamento de ARVs registado no FILA (“Data de Levantamento”) ou
   * <li>1º levantamento registado na “Ficha Recepção/ Levantou ARVs?” com “Levantou ARV” = Sim
   *     (“Data de Levantamento”) sendo a data mais antiga dos critérios acima <= “Data Fim do
   *     Relatório”
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
        " SELECT art.patient_id, MIN(art.art_date) min_art_date FROM ( "
            + " SELECT p.patient_id, MIN(e.encounter_datetime) art_date FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + " WHERE e.encounter_type = ${18} "
            + " AND e.encounter_datetime <= :endDate "
            + " AND e.voided = 0 "
            + " AND p.voided = 0 "
            + " AND e.location_id = :location "
            + " GROUP BY p.patient_id "
            + " UNION "
            + "    SELECT p.patient_id,  MIN(o.value_datetime) AS art_date FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + " INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                         INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id  "
            + "                         AND o.person_id = o2.person_id "
            + " WHERE e.encounter_type = ${52} "
            + " AND o.concept_id = ${23866} "
            + "                 AND o.value_datetime <= :endDate "
            + "                 AND o.voided = 0 "
            + "                 AND o2.concept_id = ${23865} "
            + "                 AND o2.value_coded = ${1065} "
            + "                 AND o2.voided = 0 "
            + " AND e.voided = 0 "
            + " AND p.voided = 0 "
            + " AND e.location_id = :location                 "
            + " GROUP BY p.patient_id "
            + " ) art  "
            + " GROUP BY art.patient_id ";

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

  /**
   * @see #getPatientsWithEstadioOnPeriod
   * @return {@link DataDefinition}
   */
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

  /**
   * @see #getPatientsWithEstadioOnPeriod
   * @return {@link DataDefinition}
   */
  public DataDefinition getResultOfEstadioOnPeriod() {

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

  /** Add parameters to Cohort Definitions */
  private List<Parameter> getCohortParameters() {
    return Arrays.asList(
        ReportingConstants.START_DATE_PARAMETER,
        ReportingConstants.END_DATE_PARAMETER,
        ReportingConstants.LOCATION_PARAMETER);
  }
}
