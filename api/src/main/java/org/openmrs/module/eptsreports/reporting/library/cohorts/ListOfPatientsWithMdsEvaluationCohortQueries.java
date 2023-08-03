package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.eptsreports.reporting.library.queries.CommonQueries;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsWithMdsEvaluationCohortQueries {

  private CommonQueries commonQueries;

  private HivMetadata hivMetadata;

  private TbMetadata tbMetadata;

  private CommonMetadata commonMetadata;

  String inclusionStartMonthAndDay = "'-01-21'";
  String inclusionEndMonthAndDay = "'-06-20'";

  @Autowired
  public ListOfPatientsWithMdsEvaluationCohortQueries(
      CommonQueries commonQueries,
      HivMetadata hivMetadata,
      TbMetadata tbMetadata,
      CommonMetadata commonMetadata) {
    this.commonQueries = commonQueries;
    this.hivMetadata = hivMetadata;
    this.tbMetadata = tbMetadata;
    this.commonMetadata = commonMetadata;
  }

  /**
   * <b>RF9 - Relatório – Informação do utente - Secção A1 a A9</b>
   *
   * <p>A2- Coorte: (coluna B) – Resposta = 12 meses, caso o utente tenha iniciado TARV na coorte de
   * 12 meses, ou Resposta = 24 meses, caso o utente tenha iniciado TARV na coorte de 24 meses
   * (RF4).
   *
   * @return {CohortDefinition}
   */
  public CohortDefinition getPatientsInitiatedART() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients who initiated the ART between the cohort period");
    sqlCohortDefinition.addParameter(
        new Parameter("evaluationYear", "evaluationYear", Integer.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    String arvStart = commonQueries.getArtStartDateOnFichaResumo();

    String query =
        "SELECT patient_id FROM ( "
            + arvStart
            + " ) initiated_art"
            + "   WHERE initiated_art.art_start BETWEEN CONCAT(:evaluationYear, "
            + inclusionStartMonthAndDay
            + " ) AND "
            + "  CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + " ) "
            + "   GROUP BY patient_id";

    sqlCohortDefinition.setQuery(query);
    return sqlCohortDefinition;
  }

  /**
   * <b>RF9 - Relatório – Informação do utente - Secção A1 a A9</b>
   *
   * <p>A2- Coorte: (coluna B) – Resposta = 12 meses, caso o utente tenha iniciado TARV na coorte de
   * 12 meses, ou Resposta = 24 meses, caso o utente tenha iniciado TARV na coorte de 24 meses
   * (RF4).
   *
   * @return {DataDefinition}
   */
  public DataDefinition getCoort12Or24Months() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("A.2 - Coorte: – Resposta = 12 meses ou Resposta = 24 meses.");
    sqlPatientDataDefinition.addParameter(
        new Parameter("evaluationYear", "evaluationYear", Integer.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());

    String query =
        "SELECT final_query.patient_id, "
            + "       CASE "
            + "         WHEN final_query.art_start_date BETWEEN Date_sub( "
            + "              final_query.art_start_date, INTERVAL 12 month) AND "
            + "final_query.art_start_date THEN '12 Meses' "
            + "		  WHEN final_query.art_start_date BETWEEN Date_sub( "
            + "               final_query.art_start_date, INTERVAL 24 month) AND "
            + "Date_sub(final_query.art_start_date, INTERVAL 12 month) "
            + "						   THEN '24 Meses' "
            + "ELSE '' "
            + "end "
            + "FROM   (SELECT art_patient.patient_id, "
            + "               art_patient.art_start AS art_start_date "
            + "        FROM   (SELECT p.patient_id, "
            + "                       MIN(o.value_datetime) art_start "
            + "                FROM   patient p "
            + "                       INNER JOIN encounter e "
            + "                               ON e.patient_id = p.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON o.encounter_id = e.encounter_id "
            + "                WHERE  e.encounter_type = ${53} "
            + "                       AND o.concept_id = ${1190} "
            + "                       AND e.location_id = :location "
            + "                       AND o.value_datetime BETWEEN "
            + "  CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + "        ) "
            + "          AND "
            + "  CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) "
            + "                       AND p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                GROUP  BY p.patient_id) art_patient "
            + "        WHERE  art_patient.patient_id NOT IN "
            + "               (SELECT p.patient_id "
            + "                FROM   patient p "
            + "                       INNER JOIN encounter e "
            + "                               ON p.patient_id = "
            + "                                  e.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON e.encounter_id = "
            + "                                  o.encounter_id "
            + "                       INNER JOIN obs o2 "
            + "                               ON e.encounter_id = "
            + "                                  o2.encounter_id "
            + "                WHERE  p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o2.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                       AND e.encounter_type = ${53} "
            + "                       AND o.concept_id = ${1369} "
            + "                       AND o.value_coded = ${1065} "
            + "                       AND o2.concept_id = ${6300} "
            + "                       AND o2.value_coded = ${6276} "
            + "                UNION "
            + "                SELECT p.patient_id "
            + "                FROM   patient p "
            + "                       INNER JOIN encounter e "
            + "                               ON p.patient_id = "
            + "                                  e.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON e.encounter_id = "
            + "                                  o.encounter_id "
            + "                WHERE  p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                       AND e.encounter_type = ${6} "
            + "                       AND o.concept_id = ${6273} "
            + "                       AND o.value_coded = ${1706} "
            + "                UNION "
            + "                SELECT obs.patient_id "
            + "                FROM "
            + "               (SELECT p.patient_id, "
            + "                       MAX(o.obs_datetime) "
            + "                       AS "
            + "                       obs_date "
            + "                FROM   patient p "
            + "                                              INNER JOIN encounter e "
            + "                                                      ON p.patient_id = "
            + "                                                         e.patient_id "
            + "                                              INNER JOIN obs o "
            + "                                                      ON e.encounter_id "
            + "                                                         = "
            + "                                                         o.encounter_id "
            + "                                                      WHERE  p.voided = 0 "
            + "                                                             AND e.voided = 0 "
            + "                                                             AND o.voided = 0 "
            + "                                                             AND e.location_id = "
            + "                                                                 :location "
            + "                                                             AND "
            + "               e.encounter_type = ${53} "
            + "                                                             AND "
            + "               o.concept_id = ${6272} "
            + "                                                             AND "
            + "               o.value_coded = ${1706} "
            + "                                                             AND "
            + "               o.obs_datetime <= "
            + "  CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) "
            + "                                                      GROUP  BY "
            + "               p.patient_id)obs)) AS "
            + "       final_query";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF9 - Relatório – Informação do utente - Secção A1 a A9</b>
   *
   * <p>A2- Coorte: (coluna B) – Resposta = 12 meses, caso o utente tenha iniciado TARV na coorte de
   * 12 meses, ou Resposta = 24 meses, caso o utente tenha iniciado TARV na coorte de 24 meses
   * (RF4).
   *
   * @return {DataDefinition}
   */
  public DataDefinition getCoort12Or24() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("A.2 - Coorte: – Resposta = 12 meses ou Resposta = 24 meses.");
    sqlPatientDataDefinition.addParameter(
        new Parameter("evaluationYear", "evaluationYear", Integer.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());

    String arvStart = commonQueries.getArtStartDateOnFichaResumo();

    String query =
        "SELECT patient_id, Min(o.value_datetime) art_start FROM ( "
            + arvStart
            + " ) initiated_art"
            + "   WHERE initiated_art.art_start BETWEEN CONCAT(:evaluationYear, "
            + inclusionStartMonthAndDay
            + " ) AND "
            + "  CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + " ) "
            + "   GROUP BY patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF9 - Relatório – Informação do utente - Secção A1 a A9</b>
   *
   * <p>A5- Data início TARV: (coluna E) – Resposta = Data do Início TARV. A “Data do Início TARV” é
   * a data registada na Ficha Resumo (Data do Início TARV). Nota: caso exista mais que uma “Ficha
   * de Resumo” com “Data do Início TARV” diferente, deve ser considerada a data mais antiga.
   *
   * @return {DataDefinition}
   */
  public DataDefinition getArtStartDate() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("A.5 - ART Start Date");
    sqlPatientDataDefinition.addParameter(
        new Parameter("evaluationYear", "evaluationYear", Integer.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());

    String query =
        "SELECT p.patient_id, "
            + "       Min(o.value_datetime) art_start "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  e.encounter_type = ${53} "
            + "       AND o.concept_id = ${1190} "
            + "       AND e.location_id = :location "
            + "       AND p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "GROUP  BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * O sistema irá determinar a idade do utente na Data Início TARV, ou seja, irá calcular a idade
   * com base na seguinte fórmula: Idade = Data Início TARV - Data de Nascimento
   *
   * <p>Nota 1: A idade será calculada em anos.
   *
   * <p>Nota 2: A “Data Início TARV” é a data registada na Ficha Resumo (“Data do Início TARV”).
   *
   * <p>Nota 3: caso exista mais que uma “Ficha de Resumo” com “Data do Início TARV” diferente, deve
   * ser considerada a data mais antiga.
   *
   * @return DataDefinition
   */
  public DataDefinition getAgeOnMOHArtStartDate() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Age on MOH ART start date");
    sqlPatientDataDefinition.addParameter(
        new Parameter("evaluationYear", "evaluationYear", Integer.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());

    String query =
        "SELECT p.patient_id, FLOOR(DATEDIFF(A1.first_start_drugs,ps.birthdate)/365) AS age "
            + "FROM patient p "
            + "     INNER JOIN ( "
            + "           SELECT p.patient_id, MIN(o.value_datetime) as first_start_drugs "
            + "           FROM patient p "
            + "                INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "           WHERE p.voided = 0 AND e.voided = 0 AND o.voided = 0 "
            + "             AND e.encounter_type = ${53} and o.concept_id = ${1190} "
            + "             AND e.location_id = :location "
            + "             AND o.value_datetime <= "
            + "  CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + " ) "
            + "           GROUP BY p.patient_id ) AS A1 ON p.patient_id = A1.patient_id "
            + "  INNER JOIN person ps ON p.patient_id=ps.person_id WHERE p.voided=0 AND ps.voided=0 ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF13 - Data início do TPT - A.7 (coluna G)</b>
   *
   * <p>A7- Data de início do TPT: (coluna G) – Resposta = Data de Início TPT (RF13)
   *
   * <p>O sistema irá determinar a “Data Início do TPT” do utente selecionando a data mais antiga
   * dos seguintes critérios:
   *
   * <p>registo de “Profilaxia TPT” (resposta = “INH” ou “3HP” ou “1HP” ou “LFX”) e o respectivo
   * “Estado de Profilaxia TPT” (resposta = "Início") na Ficha Clínica (“Data Consulta”) durante o
   * período de inclusão;
   *
   * <p>registo de “Última profilaxia TPT” (resposta = “INH” ou ”3HP” ou “1HP” ou “LFX”) com “Data
   * Início” registada na Ficha Resumo durante o período de inclusão;
   *
   * @return {DataDefinition}
   */
  public DataDefinition getTptInitiationDate() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Data de início do TPT.");
    sqlPatientDataDefinition.addParameter(
        new Parameter("evaluationYear", "evaluationYear", Integer.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("23985", tbMetadata.getRegimeTPTConcept().getConceptId());
    map.put("23954", tbMetadata.get3HPConcept().getConceptId());
    map.put("656", tbMetadata.getIsoniazidConcept().getConceptId());
    map.put("165305", tbMetadata.get1HPConcept().getConceptId());
    map.put("165306", tbMetadata.getLFXConcept().getConceptId());
    map.put("165308", tbMetadata.getDataEstadoDaProfilaxiaConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());

    String query =
        "SELECT p.patient_id, "
            + "       Min(e.encounter_datetime) AS data_inicio_tpt "
            + "FROM   patient p "
            + "       JOIN encounter e "
            + "         ON e.patient_id = p.patient_id "
            + "       JOIN obs o "
            + "         ON o.encounter_id = e.encounter_id "
            + "       JOIN obs o2 "
            + "         ON e.encounter_id = o2.encounter_id "
            + "WHERE  e.encounter_type = ${6} "
            + "       AND e.location_id = :location "
            + "       AND p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND o2.voided = 0 "
            + "       AND ( ( o.concept_id = ${23985} "
            + "               AND o.value_coded IN ( ${23954}, ${656}, ${165305}, ${165306} ) ) "
            + "             AND ( o2.concept_id = ${165308} "
            + "                   AND o2.value_coded IN ( ${1256} ) ) ) "
            + "       AND e.encounter_datetime BETWEEN CONCAT(:evaluationYear, "
            + inclusionStartMonthAndDay
            + " ) AND "
            + "  CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + " ) "
            + "GROUP  BY p.patient_id "
            + "UNION "
            + "SELECT p.patient_id, "
            + "       Min(o2.obs_datetime) AS data_inicio_tpt "
            + "FROM   patient p "
            + "       JOIN encounter e "
            + "         ON e.patient_id = p.patient_id "
            + "       JOIN obs o "
            + "         ON o.encounter_id = e.encounter_id "
            + "       JOIN obs o2 "
            + "         ON e.encounter_id = o2.encounter_id "
            + "WHERE  e.encounter_type = ${53} "
            + "       AND e.location_id = :location "
            + "       AND p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND o2.voided = 0 "
            + "       AND ( ( o.concept_id = ${23985} "
            + "               AND o.value_coded IN ( ${23954}, ${656}, ${165305}, ${165306} ) ) "
            + "             AND ( o2.concept_id = ${165308} "
            + "                   AND o2.value_coded IN ( ${1256} ) ) ) "
            + "       AND e.encounter_datetime BETWEEN CONCAT(:evaluationYear, "
            + inclusionStartMonthAndDay
            + " ) AND "
            + "  CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + " ) "
            + "GROUP  BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF14 - Data de registo de resultado do CD4 inicial - A.8 (Coluna H)</b>
   *
   * <p>A8- Data de registo do resultado de CD4 inicial: (coluna H) Resposta = Data do resultado de
   * CD4 inicial (RF14)
   *
   * <p>O sistema irá determinar a “Data de Registo de Resultado do CD4 Inicial” identificando a
   * consulta (Ficha Clínica) na qual foi efectuado o registo do resultado do CD4 inicial
   * (“Investigações - Resultados Laboratoriais") ocorrido entre 0 e 33 dias após o Início TARV
   * durante o período de inclusão.
   *
   * @return {DataDefinition}
   */
  public DataDefinition getCd4ResultDate() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(
        "Identificação de Data de registo do resultado de CD4 inicial\n");
    sqlPatientDataDefinition.addParameter(
        new Parameter("evaluationYear", "evaluationYear", Integer.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());

    String arvStart = commonQueries.getArtStartDateOnFichaResumo();

    String query =
        "SELECT pa.patient_id, "
            + "       Min(enc.encounter_datetime) AS data_consulta "
            + "FROM   patient pa "
            + "       INNER JOIN encounter enc "
            + "               ON enc.patient_id = pa.patient_id "
            + "       INNER JOIN obs "
            + "               ON obs.encounter_id = enc.encounter_id "
            + "       INNER JOIN (SELECT art.patient_id, "
            + "                          art.art_start "
            + "                   FROM   ( "
            + arvStart
            + "                   ) art "
            + "                   WHERE  art.art_start >= "
            + "  CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + " ) "
            + "                          AND art.art_start <= "
            + "  CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + " ) "
            + "                   GROUP  BY art.patient_id) first_art "
            + "               ON first_art.patient_id = pa.patient_id "
            + "WHERE  pa.voided = 0 "
            + "       AND enc.voided = 0 "
            + "       AND obs.voided = 0 "
            + "       AND enc.encounter_type = ${6} "
            + "       AND obs.concept_id = ${1695} "
            + "       AND obs.value_numeric IS NOT NULL "
            + "       AND enc.encounter_datetime >= first_art.art_start "
            + "       AND enc.encounter_datetime <= Date_add(first_art.art_start, "
            + "                                     INTERVAL 33 day) "
            + "       AND enc.location_id = :location "
            + "GROUP  BY pa.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF14 - Data de registo de resultado do CD4 inicial - A.9 (Coluna I)</b>
   *
   * <p>A9- Resultado do CD4 Inicial: (coluna I) - Resposta = Resultado de CD4 inicial (RF15)
   *
   * <p>O sistema irá identificar o resultado do CD4 inicial registrado em “Investigações-
   * Resultados Laboratoriais" da primeira ou segunda Ficha Clínica ocorrido entre 0 e 33 dias
   * depois da Data de Início TARV, durante o período de inclusão.
   *
   * @return {DataDefinition}
   */
  public DataDefinition getCd4Result() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Resultado do CD4 inicial - A.9 (Coluna I)");
    sqlPatientDataDefinition.addParameter(
        new Parameter("evaluationYear", "evaluationYear", Integer.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());

    String arvStart = commonQueries.getArtStartDateOnFichaResumo();

    String query =
        "SELECT pt.patient_id, "
            + "       o.value_numeric AS cd4_result "
            + "FROM   patient pt "
            + "       INNER JOIN (SELECT pa.patient_id, "
            + "                          Min(enc.encounter_datetime) AS data_consulta "
            + "                   FROM   patient pa "
            + "                          INNER JOIN encounter enc "
            + "                                  ON enc.patient_id = pa.patient_id "
            + "                          INNER JOIN obs "
            + "                                  ON obs.encounter_id = enc.encounter_id "
            + "                          INNER JOIN (SELECT art.patient_id, "
            + "                                             art.art_start "
            + "                                      FROM   ( "
            + arvStart
            + "                                      ) art "
            + "                                      WHERE  art.art_start >= "
            + "  CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + " ) "
            + "                                             AND art.art_start <= "
            + "  CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + " ) "
            + "                                      GROUP  BY art.patient_id) first_art "
            + "                                  ON first_art.patient_id = pa.patient_id "
            + "                   WHERE  pa.voided = 0 "
            + "                          AND enc.voided = 0 "
            + "                          AND obs.voided = 0 "
            + "                          AND enc.encounter_type = ${6} "
            + "                          AND obs.concept_id = ${1695} "
            + "                          AND obs.value_numeric IS NOT NULL "
            + "                          AND enc.encounter_datetime >= first_art.art_start "
            + "                          AND enc.encounter_datetime <= Date_add( "
            + "                              first_art.art_start, "
            + "                                                        INTERVAL 33 day) "
            + "                          AND enc.location_id = :location "
            + "                   GROUP  BY pa.patient_id "
            + "                   UNION "
            + "                   SELECT ee.patient_id, "
            + "                          Min(ee.encounter_datetime) "
            + "                   FROM   encounter ee "
            + "                          INNER JOIN (SELECT pa.patient_id, "
            + "                                             Min(enc.encounter_datetime) AS "
            + "                                             data_consulta "
            + "                                      FROM   patient pa "
            + "                                             INNER JOIN encounter enc "
            + "                                                     ON enc.patient_id = "
            + "                                                        pa.patient_id "
            + "                                             INNER JOIN obs "
            + "                                                     ON obs.encounter_id = "
            + "                                                        enc.encounter_id "
            + "                                             INNER JOIN (SELECT art.patient_id, "
            + "                                                                art.art_start "
            + "                                                         FROM "
            + "                                             ("
            + arvStart
            + ") art "
            + "       WHERE  art.art_start >= "
            + "  CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + " ) "
            + "       AND art.art_start <= "
            + "  CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + " ) "
            + "       GROUP  BY art.patient_id) first_art "
            + "       ON first_art.patient_id = pa.patient_id "
            + "       WHERE  pa.voided = 0 "
            + "       AND enc.voided = 0 "
            + "       AND obs.voided = 0 "
            + "       AND enc.encounter_type = ${6} "
            + "       AND obs.concept_id = ${1695} "
            + "       AND obs.value_numeric IS NOT NULL "
            + "       AND enc.encounter_datetime >= first_art.art_start "
            + "       AND enc.encounter_datetime <= Date_add( "
            + "       first_art.art_start, "
            + "          INTERVAL 33 day) "
            + "       AND enc.location_id = :location "
            + "       GROUP  BY pa.patient_id) minn_encounter "
            + "       ON minn_encounter.patient_id = ee.patient_id "
            + "       WHERE  ee.voided = 0 "
            + "       AND ee.encounter_type = ${6} "
            + "       AND ee.encounter_datetime > minn_encounter.data_consulta) min_encounter "
            + "               ON pt.patient_id = min_encounter.patient_id "
            + "       INNER JOIN encounter enc "
            + "               ON enc.patient_id = pt.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = enc.encounter_id "
            + "WHERE  pt.voided = 0 "
            + "       AND enc.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND enc.encounter_type = ${6} "
            + "       AND o.concept_id = ${1695} "
            + "       AND o.value_numeric IS NOT NULL "
            + "       AND enc.encounter_datetime >= "
            + "  CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + " ) "
            + "       AND enc.encounter_datetime <= "
            + "  CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + " ) "
            + "       AND enc.location_id = :location "
            + "GROUP  BY pt.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF12 - Critérios de Não Elegibilidade ao TPT</b>
   *
   * <p>O sistema irá determinar se o utente não é elegível ao TPT se o utente tiver:
   *
   * <ul>
   *   <li>o registo de “Diagnóstico TB Activa” (resposta = “Sim”) numa Ficha Clínica (“Data
   *       Consulta”) registada no período de inclusão <br>
   *   <li>o registo de “Tem Sintomas TB? (resposta = “Sim”) numa Ficha Clínica (“Data Consulta”)
   *       registada no período de inclusão <br>
   *   <li>o registo de “Quais Sintomas de TB?” (resposta = “Febre” ou “Emagrecimento” ou "Sudorese
   *       noturna” ou “Tosse com ou sem sangue” ou “Astenia” ou “Contacto recente com TB” numa
   *       Ficha Clínica (“Data Consulta”) registada no período de inclusão <br>
   *   <li>o registo de “Tratamento TB” (resposta = “Início”, “Contínua”, “Fim”) na Ficha Clínica
   *       com “Data de Tratamento” decorrida no período de inclusão <br>
   *   <li>o registo de “TB” nas “Condições médicas Importantes” na Ficha Resumo com “Data”
   *       decorrida no período de inclusão; <br>
   *   <li>o registo de “Última profilaxia TPT” (resposta = “INH” ou “3HP” ou “1HP” ou “LFX”) na
   *       Ficha Resumo com “Data Início” decorrida durante o período de inclusão. <br>
   * </ul>
   *
   * <p>Nota: O período de inclusão deverá ser definido da seguinte forma:
   *
   * <ul>
   *   <li>Utentes que iniciaram TARV na coorte de 12 meses: <br>
   *       Data Início Inclusão = “21 de Janeiro” de “Ano de Avaliação” menos (-) 1 ano <br>
   *       Data Fim Inclusão = “20 de Junho” de “Ano de Avaliação” menos (-) 1 ano <br>
   *       <br>
   *   <li>Utentes que iniciaram TARV na coorte de 24 meses: <br>
   *       Data Início Inclusão = “21 de Janeiro” de “Ano de Avaliação” menos (-) 2 anos <br>
   *       Data Fim Inclusão = “20 de Junho” de “Ano de Avaliação” menos (-) 2 anos <br>
   * </ul>
   *
   * @return {DataDefinition}
   */
  public DataDefinition getPatientsTptNotEligible() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Elegível ao TPT no Início do TARV: (coluna F)");
    sqlPatientDataDefinition.addParameter(
        new Parameter("evaluationYear", "evaluationYear", Integer.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1406", hivMetadata.getOtherDiagnosis().getConceptId());
    map.put("42", tbMetadata.getPulmonaryTB().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1268", hivMetadata.getTBTreatmentPlanConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("1257", hivMetadata.getContinueRegimenConcept().getConceptId());
    map.put("1267", hivMetadata.getCompletedConcept().getConceptId());
    map.put("23758", hivMetadata.getTBSymptomsConcept().getConceptId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    map.put("23761", hivMetadata.getActiveTBConcept().getConceptId());
    map.put("1766", tbMetadata.getObservationTB().getConceptId());
    map.put("1763", tbMetadata.getFeverLastingMoraThan3Weeks().getConceptId());
    map.put("1764", tbMetadata.getWeightLossOfMoreThan3KgInLastMonth().getConceptId());
    map.put("1762", tbMetadata.getNightsWeatsLastingMoraThan3Weeks().getConceptId());
    map.put("1760", tbMetadata.getCoughLastingMoraThan3Weeks().getConceptId());
    map.put("23760", tbMetadata.getAsthenia().getConceptId());
    map.put("1765", tbMetadata.getCohabitantBeingTreatedForTB().getConceptId());
    map.put("161", tbMetadata.getLymphadenopathy().getConceptId());
    map.put("23985", tbMetadata.getRegimeTPTConcept().getConceptId());
    map.put("23954", tbMetadata.get3HPConcept().getConceptId());
    map.put("656", tbMetadata.getIsoniazidConcept().getConceptId());
    map.put("165305", tbMetadata.get1HPConcept().getConceptId());
    map.put("165306", tbMetadata.getLFXConcept().getConceptId());
    map.put("165308", tbMetadata.getDataEstadoDaProfilaxiaConcept().getConceptId());

    String sql =
        "SELECT final_query.patient_id, "
            + "       CASE "
            + "         WHEN final_query.encounter_date IS NULL THEN 'Sim' "
            + "         WHEN final_query.encounter_date IS NOT NULL THEN 'Não' "
            + "         ELSE '' "
            + "       end "
            + "FROM   (SELECT p.patient_id, "
            + "               o.obs_datetime AS encounter_date "
            + "        FROM   patient p "
            + "               INNER JOIN encounter e "
            + "                       ON e.patient_id = p.patient_id "
            + "               INNER JOIN obs o "
            + "                       ON o.encounter_id = e.encounter_id "
            + "        WHERE  e.encounter_type = ${53} "
            + "               AND e.location_id = :location "
            + "               AND o.obs_datetime BETWEEN "
            + "  CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + "        ) "
            + "          AND "
            + "  CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) "
            + "               AND o.concept_id = ${1406} "
            + "               AND o.value_coded = ${42} "
            + "               AND p.voided = 0 "
            + "               AND e.voided = 0 "
            + "               AND o.voided = 0 "
            + "        UNION "
            + "        SELECT p.patient_id, "
            + "               o.obs_datetime AS encounter_date "
            + "        FROM   patient p "
            + "               INNER JOIN encounter e "
            + "                       ON e.patient_id = p.patient_id "
            + "               INNER JOIN obs o "
            + "                       ON o.encounter_id = e.encounter_id "
            + "        WHERE  e.encounter_type = ${6} "
            + "               AND e.location_id = :location "
            + "               AND o.obs_datetime BETWEEN "
            + "  CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + "        ) "
            + "          AND "
            + "  CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) "
            + "               AND o.concept_id = ${1268} "
            + "               AND o.value_coded IN ( ${1256}, ${1257}, ${1267} ) "
            + "               AND p.voided = 0 "
            + "               AND e.voided = 0 "
            + "               AND o.voided = 0 "
            + "        UNION "
            + "        SELECT p.patient_id, "
            + "               e.encounter_datetime AS encounter_date "
            + "        FROM   patient p "
            + "               INNER JOIN encounter e "
            + "                       ON e.patient_id = p.patient_id "
            + "               INNER JOIN obs o "
            + "                       ON o.encounter_id = e.encounter_id "
            + "        WHERE  e.encounter_type = ${6} "
            + "               AND e.location_id = :location "
            + "               AND e.encounter_datetime BETWEEN "
            + "  CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + "        ) "
            + "          AND "
            + "  CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) "
            + "               AND o.concept_id = ${23758} "
            + "               AND o.value_coded IN( ${1065} ) "
            + "               AND p.voided = 0 "
            + "               AND e.voided = 0 "
            + "               AND o.voided = 0 "
            + "        GROUP  BY p.patient_id "
            + "        UNION "
            + "        SELECT p.patient_id, "
            + "               e.encounter_datetime AS encounter_date "
            + "        FROM   patient p "
            + "               INNER JOIN encounter e "
            + "                       ON e.patient_id = p.patient_id "
            + "               INNER JOIN obs o "
            + "                       ON o.encounter_id = e.encounter_id "
            + "        WHERE  e.encounter_type = ${6} "
            + "               AND e.location_id = :location "
            + "               AND e.encounter_datetime BETWEEN "
            + "  CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + "        ) "
            + "          AND "
            + "  CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) "
            + "               AND o.concept_id = ${23761} "
            + "               AND o.value_coded IN( ${1065} ) "
            + "               AND p.voided = 0 "
            + "               AND e.voided = 0 "
            + "               AND o.voided = 0 "
            + "        GROUP  BY p.patient_id "
            + "        UNION "
            + "        SELECT p.patient_id, "
            + "               e.encounter_datetime AS encounter_date "
            + "        FROM   patient p "
            + "               INNER JOIN encounter e "
            + "                       ON e.patient_id = p.patient_id "
            + "               INNER JOIN obs o "
            + "                       ON o.encounter_id = e.encounter_id "
            + "        WHERE  e.encounter_type = ${6} "
            + "               AND e.location_id = :location "
            + "               AND e.encounter_datetime BETWEEN "
            + "  CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + "        ) "
            + "          AND "
            + "  CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) "
            + "               AND ( o.concept_id = ${1766} "
            + "                     AND o.value_coded IN( ${1763}, ${1764}, ${1762}, ${1760}, "
            + "                                           ${23760}, ${1765}, ${161} ) ) "
            + "               AND p.voided = 0 "
            + "               AND e.voided = 0 "
            + "               AND o.voided = 0 "
            + "        GROUP  BY p.patient_id "
            + "        UNION "
            + "        SELECT p.patient_id, "
            + "               o2.obs_datetime AS encounter_date "
            + "        FROM   patient p "
            + "               INNER JOIN encounter e "
            + "                       ON e.patient_id = p.patient_id "
            + "               INNER JOIN obs o "
            + "                       ON o.encounter_id = e.encounter_id "
            + "               INNER JOIN obs o2 "
            + "                       ON o2.encounter_id = e.encounter_id "
            + "        WHERE  e.encounter_type = ${53} "
            + "               AND e.location_id = :location "
            + "               AND o2.obs_datetime BETWEEN "
            + "  CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + "        ) "
            + "          AND "
            + "  CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) "
            + "               AND ( o.concept_id = ${23985} "
            + "                     AND o.value_coded IN ( ${23954}, ${656}, ${165305}, ${165306} ) ) "
            + "               AND ( o2.concept_id = ${165308} "
            + "                     AND o2.value_coded = ${1256} ) "
            + "               AND p.voided = 0 "
            + "               AND e.voided = 0 "
            + "               AND o.voided = 0 "
            + "               AND o2.voided = 0) AS final_query";

    StringSubstitutor substitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(substitutor.replace(sql));
    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF17 - Data do pedido da 1a CV - B.1 (Coluna J)</b>
   *
   * <p>O sistema irá determinar a Data do Pedido da 1ª Carga Viral do utente identificando a data
   * da primeira consulta clínica (Ficha Clínica), após o início TARV (Data Início TARV), na qual
   * foi efectuado o registo do Pedido de Carga Viral.<br>
   * <br>
   *
   * <p>Nota 1: A “Data Início TARV” é a data registada na Ficha Resumo (“Data do Início TARV”).
   * <br>
   * <br>
   *
   * <p>Nota 2: O utente a ser considerado nesta definição iniciou TARV ou na coorte de 12 meses ou
   * na coorte de 24 meses, conforme definido no RF4.
   *
   * @return {DataDefinition}
   */
  public DataDefinition getFirstViralLoad() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Data do pedido da 1a CV");
    sqlPatientDataDefinition.addParameter(
        new Parameter("evaluationYear", "evaluationYear", Integer.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String query =
        "SELECT p.patient_id, "
            + "                    Min(e.encounter_datetime) AS first_vl "
            + "             FROM   patient p "
            + "                    INNER JOIN encounter e "
            + "                            ON e.patient_id = p.patient_id "
            + "                    INNER JOIN obs o "
            + "                            ON o.encounter_id = e.encounter_id "
            + "                    INNER JOIN ( "
            + "								SELECT p.patient_id, "
            + "                    Min(o.value_datetime) art_start "
            + "             FROM   patient p "
            + "                    INNER JOIN encounter e "
            + "                            ON e.patient_id = p.patient_id "
            + "                    INNER JOIN obs o "
            + "                            ON o.encounter_id = e.encounter_id "
            + "             WHERE  e.encounter_type = ${53} "
            + "                    AND o.concept_id = ${1190} "
            + "                    AND o.value_datetime BETWEEN "
            + "  CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + "        ) "
            + "          AND "
            + "  CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) "
            + "                    AND e.location_id = :location "
            + "                    AND p.voided = 0 "
            + "                    AND e.voided = 0 "
            + "                    AND o.voided = 0 "
            + "             GROUP  BY p.patient_id "
            + "							   ) art "
            + "             WHERE  e.encounter_type = ${6} "
            + "                    AND o.concept_id = ${23722} "
            + "                    AND o.value_coded = ${856} "
            + "                    AND e.location_id = :location "
            + "                    AND e.encounter_datetime >= art.art_start "
            + "                    AND p.voided = 0 "
            + "                    AND e.voided = 0 "
            + "                    AND o.voided = 0 "
            + "             GROUP  BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF18 - Data do resultado da 1a CV- B.2 (Coluna K)</b>
   *
   * <p>O sistema irá determinar a Data do Resultado da 1ª Carga Viral do utente identificando a
   * data da primeira consulta clínica (Ficha Clínica), após o início TARV (Data Início TARV), na
   * qual foi efectuado o registo de resultado da Carga Viral. <br>
   * <br>
   *
   * <p>Nota 1: A “Data Início TARV” é a data registada na Ficha Resumo (“Data do Início TARV”).
   * <br>
   * <br>
   *
   * <p>Nota 2: O utente a ser considerado nesta definição iniciou TARV ou na coorte de 12 meses ou
   * na coorte de 24 meses, conforme definido no RF4.
   *
   * @return {DataDefinition}
   */
  public DataDefinition getFirstViralLoadResultDate() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("B2- Data de registo do resultado da 1ª CV");
    sqlPatientDataDefinition.addParameter(
        new Parameter("evaluationYear", "evaluationYear", Integer.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        "SELECT     p.patient_id, "
            + "           MIN(e.encounter_datetime) AS first_vl_date  "
            + "FROM       patient p "
            + "INNER JOIN encounter e "
            + "ON         e.patient_id = p.patient_id "
            + "INNER JOIN obs o "
            + "ON         o.encounter_id = e.encounter_id "
            + "INNER JOIN "
            + "           ( "
            + "                      SELECT     p.patient_id, "
            + "                                 Min(o.value_datetime) art_start "
            + "                      FROM       patient p "
            + "                      INNER JOIN encounter e "
            + "                      ON         e.patient_id = p.patient_id "
            + "                      INNER JOIN obs o "
            + "                      ON         o.encounter_id = e.encounter_id "
            + "                      WHERE      e.encounter_type = ${53} "
            + "                      AND        o.concept_id = ${1190} "
            + "                      AND        o.value_datetime BETWEEN "
            + "  CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + "        ) "
            + "          AND "
            + "  CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) "
            + "                      AND        e.location_id = :location "
            + "                      AND        p.voided = 0 "
            + "                      AND        e.voided = 0 "
            + "                      AND        o.voided = 0 "
            + "                      GROUP BY   p.patient_id ) art "
            + "where      e.encounter_type = ${6} "
            + "AND        ( ( "
            + "                                 o.concept_id= ${856} ) "
            + "           OR         ( "
            + "                                 o.concept_id = ${1305} "
            + "                      AND        o.value_coded IS NOT NULL)) "
            + "AND        e.location_id = :location "
            + "AND        e.encounter_datetime >= art.art_start "
            + "AND        p.voided = 0 "
            + "AND        e.voided = 0 "
            + "AND        o.voided = 0 "
            + "GROUP BY   p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF18 - Data do resultado da 1a CV- B.2 (Coluna K)</b>
   *
   * <p>O sistema irá determinar o Resultado da 1ª Carga Viral do utente seleccionando o primeiro
   * resultado de Carga Viral registado na primeira consulta clínica (Ficha Clínica) após o início
   * TARV (Data Início TARV). <br>
   * <br>
   *
   * <p>Nota 1: A “Data Início TARV” é a data registada na Ficha Resumo (“Data do Início TARV”).
   * <br>
   * <br>
   *
   * <p>Nota 2: O utente a ser considerado nesta definição iniciou TARV ou na coorte de 12 meses ou
   * na coorte de 24 meses, conforme definido no RF4.
   *
   * @return {DataDefinition}
   */
  public DataDefinition getFirstViralLoadResult() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("B3- Resultado da 1ª CV");
    sqlPatientDataDefinition.addParameter(
        new Parameter("evaluationYear", "evaluationYear", Integer.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        "SELECT     p.patient_id, "
            + "           o.value_numeric AS first_vl_result  "
            + "FROM       patient p "
            + "INNER JOIN encounter e "
            + "ON         e.patient_id = p.patient_id "
            + "INNER JOIN obs o "
            + "ON         o.encounter_id = e.encounter_id "
            + "INNER JOIN "
            + "           ( "
            + "                      SELECT     p.patient_id, "
            + "                                 Min(o.value_datetime) art_start "
            + "                      FROM       patient p "
            + "                      INNER JOIN encounter e "
            + "                      ON         e.patient_id = p.patient_id "
            + "                      INNER JOIN obs o "
            + "                      ON         o.encounter_id = e.encounter_id "
            + "                      WHERE      e.encounter_type = ${53} "
            + "                      AND        o.concept_id = ${1190} "
            + "                      AND        o.value_datetime BETWEEN "
            + "  CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + "        ) "
            + "          AND "
            + "  CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) "
            + "                      AND        e.location_id = :location "
            + "                      AND        p.voided = 0 "
            + "                      AND        e.voided = 0 "
            + "                      AND        o.voided = 0 "
            + "                      GROUP BY   p.patient_id ) art "
            + "where      e.encounter_type = ${6} "
            + "AND        ( ( "
            + "                                 o.concept_id= ${856} ) "
            + "           OR         ( "
            + "                                 o.concept_id = ${1305} "
            + "                      AND        o.value_coded IS NOT NULL)) "
            + "AND        e.location_id = :location "
            + "AND        e.encounter_datetime >= art.art_start "
            + "AND        p.voided = 0 "
            + "AND        e.voided = 0 "
            + "AND        o.voided = 0 "
            + "GROUP BY   p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF20 - Resultado do 2˚ CD4 (2˚ CD4 feito nos 1˚s 12 meses de TARV) - B.4 (Coluna M)</b>
   *
   * <p>O sistema irá determinar o Resultado do 2º CD4 do utente seleccionando o segundo resultado
   * do CD4 registado na consulta Clínica (Ficha Clínica) e este resultado deve estar entre 33 dias
   * e 12 meses do Início TARV (“Data da Consulta com resultado CD4” >= “Data Início TARV” + 33 dias
   * e <= “Data Início TARV” + 12 meses).
   *
   * <p>Nota 1: A “Data Início TARV” é a data registada na Ficha Resumo (“Data do Início TARV”).
   * <br>
   * <br>
   *
   * <p>Nota 2: O utente a ser considerado nesta definição iniciou TARV ou na coorte de 12 meses ou
   * na coorte de 24 meses, conforme definido no RF4.
   *
   * @return {DataDefinition}
   */
  public DataDefinition getSecondCd4Result() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(
        "Resultado do 2˚ CD4 (2˚ CD4 feito nos 1˚s 12 meses de TARV) - B.4 (Coluna M)");
    sqlPatientDataDefinition.addParameter(
        new Parameter("evaluationYear", "evaluationYear", Integer.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());

    String query =
        "SELECT pt.patient_id, "
            + "                    o.value_numeric as second_cd4_result "
            + "             FROM   patient pt "
            + "                    INNER JOIN encounter enc "
            + "                            ON enc.patient_id = pt.patient_id "
            + "                    INNER JOIN obs o "
            + "                            ON o.encounter_id = enc.encounter_id "
            + "                    INNER JOIN (SELECT pa.patient_id, "
            + "                                       Min(enc.encounter_datetime) AS data_consulta "
            + "                                FROM   patient pa "
            + "                                       INNER JOIN encounter enc "
            + "                                               ON enc.patient_id = pa.patient_id "
            + "                                       INNER JOIN obs "
            + "                                               ON obs.encounter_id = enc.encounter_id "
            + "                                       INNER JOIN ( "
            + "             SELECT p.patient_id, "
            + "                    Min(o.value_datetime) art_start "
            + "             FROM   patient p "
            + "                    INNER JOIN encounter e "
            + "                            ON e.patient_id = p.patient_id "
            + "                    INNER JOIN obs o "
            + "                            ON o.encounter_id = e.encounter_id "
            + "             WHERE  e.encounter_type = ${53} "
            + "                    AND o.concept_id = ${1190} "
            + "                    AND e.location_id = :location "
            + "                    AND o.value_datetime >= "
            + "  CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + " ) "
            + "                    AND o.value_datetime <= "
            + "  CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + " ) "
            + "                    AND p.voided = 0 "
            + "                    AND e.voided = 0 "
            + "                    AND o.voided = 0 "
            + "             GROUP  BY p.patient_id  ) first_art "
            + "                                               ON first_art.patient_id = pa.patient_id "
            + "                                WHERE  pa.voided = 0 "
            + "                                       AND enc.voided = 0 "
            + "                                       AND obs.voided = 0 "
            + "                                       AND enc.encounter_type = ${6} "
            + "                                       AND obs.concept_id = ${1695} "
            + "                                       AND obs.value_numeric IS NOT NULL "
            + "                                       AND enc.encounter_datetime >= first_art.art_start "
            + "                                       AND enc.encounter_datetime <= Date_add( "
            + "                                           first_art.art_start, "
            + "                                                                     INTERVAL 33 day) "
            + "                                       AND enc.location_id = :location "
            + "                                GROUP  BY pa.patient_id "
            + "                                UNION "
            + "                                SELECT ee.patient_id, "
            + "                                       Min(ee.encounter_datetime) "
            + "                                FROM   encounter ee "
            + "                                       INNER JOIN (SELECT pa.patient_id, "
            + "                                                          Min(enc.encounter_datetime) AS "
            + "                                                          data_consulta "
            + "                                                   FROM   patient pa "
            + "                                                          INNER JOIN encounter enc "
            + "                                                                  ON enc.patient_id = "
            + "                                                                     pa.patient_id "
            + "                                                          INNER JOIN obs "
            + "                                                                  ON obs.encounter_id = "
            + "                                                                     enc.encounter_id "
            + "                                                          INNER JOIN ( "
            + "             SELECT p.patient_id, "
            + "                    Min(o.value_datetime) art_start "
            + "             FROM   patient p "
            + "                    INNER JOIN encounter e "
            + "                            ON e.patient_id = p.patient_id "
            + "                    INNER JOIN obs o "
            + "                            ON o.encounter_id = e.encounter_id "
            + "             WHERE  e.encounter_type = ${53} "
            + "                    AND o.concept_id = ${1190} "
            + "                    AND e.location_id = :location "
            + "                    AND o.value_datetime >= "
            + "  CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + " ) "
            + "                    AND o.value_datetime <= "
            + "  CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + " ) "
            + "                    AND p.voided = 0 "
            + "                    AND e.voided = 0 "
            + "                    AND o.voided = 0 "
            + "             GROUP  BY p.patient_id  ) first_art "
            + "                    ON first_art.patient_id = pa.patient_id "
            + "                    WHERE  pa.voided = 0 "
            + "                    AND enc.voided = 0 "
            + "                    AND obs.voided = 0 "
            + "                    AND enc.encounter_type = ${6} "
            + "                    AND obs.concept_id = ${1695} "
            + "                    AND obs.value_numeric IS NOT NULL "
            + "                    AND enc.encounter_datetime >= first_art.art_start "
            + "                    AND enc.encounter_datetime <= Date_add( "
            + "                    first_art.art_start, "
            + "                       INTERVAL 33 day) "
            + "                    AND enc.location_id = :location "
            + "                    GROUP  BY pa.patient_id) first_encounter "
            + "                    ON first_encounter.patient_id = ee.patient_id "
            + "                    WHERE  ee.voided = 0 "
            + "                    AND ee.encounter_type = ${6} "
            + "                    AND ee.encounter_datetime > first_encounter.data_consulta) second_encounter "
            + "                            ON pt.patient_id = second_encounter.patient_id "
            + "             WHERE  pt.voided = 0 "
            + "                    AND enc.voided = 0 "
            + "                    AND o.voided = 0 "
            + "                    AND enc.encounter_type = ${6} "
            + "                    AND o.concept_id = ${1695} "
            + "                    AND o.value_numeric IS NOT NULL "
            + "                    AND enc.encounter_datetime >= "
            + "  CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + " ) "
            + "                    AND enc.encounter_datetime <= "
            + "  CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + " ) "
            + "                    AND enc.location_id = :location "
            + "             GROUP  BY pt.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }
}
