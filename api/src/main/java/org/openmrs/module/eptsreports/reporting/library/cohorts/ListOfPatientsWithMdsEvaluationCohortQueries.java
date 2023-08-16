package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.eptsreports.reporting.library.queries.ListOfPatientsWithMdsEvaluationQueries;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsWithMdsEvaluationCohortQueries {

  private final HivMetadata hivMetadata;

  private final TbMetadata tbMetadata;

  private final CommonMetadata commonMetadata;

  String inclusionStartMonthAndDay = "'-01-21'";
  String inclusionEndMonthAndDay = "'-06-20'";

  @Autowired
  public ListOfPatientsWithMdsEvaluationCohortQueries(
      HivMetadata hivMetadata, TbMetadata tbMetadata, CommonMetadata commonMetadata) {
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
  public CohortDefinition getPatientsInitiatedART12Or24Months() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients who initiated the ART between the cohort period");
    sqlCohortDefinition.addParameter(
        new Parameter("evaluationYear", "evaluationYear", Integer.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

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
        "SELECT art_patient.patient_id "
            + " FROM   ( "
            + "      SELECT art_patient_12.patient_id "
            + "     FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 1)
            + "     ) art_patient_12 "
            + " WHERE  art_patient_12.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 1)
            + " )"
            + " UNION "
            + "     SELECT art_patient_24.patient_id "
            + "     FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 2)
            + "     ) art_patient_24 "
            + " WHERE  art_patient_24.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 2)
            + " )"
            + " ) art_patient";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getLivetest() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("LIVETEST");
    sqlCohortDefinition.addParameter(
        new Parameter("evaluationYear", "evaluationYear", Integer.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());

    String query =
        "                   SELECT cd4_2.patient_id "
            + "                   FROM   patient cd4_2 "
            + "                          INNER JOIN encounter enc2 "
            + "                                 ON enc2.patient_id = cd4_2.patient_id "
            + "                          INNER JOIN obs obs2 "
            + "                                 ON enc2.encounter_id = obs2.encounter_id "
            + "                          INNER JOIN ( "
            + "                   SELECT cd4_1.patient_id, "
            + "                          Min(obs11.value_numeric) AS second_cd4_result "
            + "                   FROM   patient cd4_1 "
            + "                          INNER JOIN encounter enc11 "
            + "                                 ON enc11.patient_id = cd4_1.patient_id "
            + "                          INNER JOIN obs obs11 "
            + "                                 ON enc11.encounter_id = obs11.encounter_id "
            + "                          INNER JOIN ( "
            + "                   SELECT p1.patient_id, "
            + "                          Min(obs1.value_numeric) AS first_cd4_result "
            + "                   FROM   patient p1 "
            + "                          INNER JOIN encounter enc1 "
            + "                                 ON enc1.patient_id = p1.patient_id "
            + "                          INNER JOIN obs obs1 "
            + "                                 ON enc1.encounter_id = obs1.encounter_id "
            + "                          INNER JOIN (SELECT pa.patient_id, "
            + "                                             Min(enc.encounter_datetime) AS "
            + "                                             encounter_date "
            + "                                      FROM   patient pa "
            + "                                             INNER JOIN encounter enc "
            + "                                                     ON enc.patient_id = "
            + "                                                        pa.patient_id "
            + "                                             INNER JOIN obs "
            + "                                                     ON obs.encounter_id = "
            + "                                                        enc.encounter_id "
            + "                                             INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.art_start AS art_encounter "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 1)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 1)
            + " ) "
            + " ) art ON art.patient_id = enc.patient_id "
            + "       WHERE  pa.voided = 0 "
            + "       AND enc.voided = 0 "
            + "       AND obs.voided = 0 "
            + "       AND enc.encounter_type = ${6} "
            + "       AND enc.location_id = :location "
            + "       AND obs.concept_id = ${1695} "
            + "       AND obs.value_numeric IS NOT NULL "
            + "       AND enc.encounter_datetime >= DATE_ADD(art.art_encounter, INTERVAL 33 DAY) "
            + "       AND enc.encounter_datetime <= DATE_ADD(art.art_encounter, INTERVAL 12 MONTH) "
            + "       GROUP  BY pa.patient_id) first_cd4_result "
            + "       ON first_cd4_result.patient_id = p1.patient_id "
            + "       WHERE  p1.voided = 0 "
            + "       AND enc1.voided = 0 "
            + "       AND obs1.voided = 0 "
            + "       AND enc1.encounter_type = ${6} "
            + "       AND obs1.concept_id = ${1695} "
            + "       AND obs1.value_numeric IS NOT NULL "
            + "       GROUP BY p1.patient_id) first_result "
            + "       ON first_result.patient_id = cd4_1.patient_id "
            + "       WHERE  cd4_1.voided = 0 "
            + "       AND enc11.voided = 0"
            + "       AND obs11.voided = 0"
            + "       AND enc11.location_id = :location "
            + "       AND enc11.encounter_type = ${6} "
            + "       AND obs11.concept_id = ${1695} "
            + "       AND obs11.value_numeric IS NOT NULL "
            + "       GROUP BY cd4_1.patient_id) second_result "
            + "               ON cd4_2.patient_id = second_result.patient_id "
            + "WHERE  cd4_2.voided = 0 "
            + "       AND cd4_2.second_cd4_result > cd4_1.first_cd4_result"
            + "       AND enc2.location_id = :location "
            + "       AND enc2.encounter_type = ${6} "
            + "       AND obs2.concept_id = ${1695} "
            + "       AND obs2.value_numeric IS NOT NULL "
            + "GROUP  BY cd4_2.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    System.out.println(sqlCohortDefinition.getQuery());

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
        "SELECT art_patient.patient_id, art_patient.coort "
            + " FROM   ( "
            + "      SELECT art_patient_12.patient_id, '12 Meses' AS coort "
            + "     FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 1)
            + "     ) art_patient_12 "
            + " WHERE  art_patient_12.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 1)
            + " )"
            + " UNION "
            + "     SELECT art_patient_24.patient_id, '24 Meses' AS coort "
            + "     FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 2)
            + "     ) art_patient_24 "
            + " WHERE  art_patient_24.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 2)
            + " )"
            + " ) art_patient";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

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
    map.put("23761", hivMetadata.getActiveTBConcept().getConceptId());
    map.put("23758", hivMetadata.getTBSymptomsConcept().getConceptId());
    map.put("1766", tbMetadata.getObservationTB().getConceptId());
    map.put("1763", tbMetadata.getFeverLastingMoraThan3Weeks().getConceptId());
    map.put("1764", tbMetadata.getWeightLossOfMoreThan3KgInLastMonth().getConceptId());
    map.put("1762", tbMetadata.getNightsWeatsLastingMoraThan3Weeks().getConceptId());
    map.put("1760", tbMetadata.getCoughLastingMoraThan3Weeks().getConceptId());
    map.put("23760", tbMetadata.getAsthenia().getConceptId());
    map.put("1765", tbMetadata.getCohabitantBeingTreatedForTB().getConceptId());
    map.put("161", tbMetadata.getLymphadenopathy().getConceptId());
    map.put("1268", hivMetadata.getTBTreatmentPlanConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("1257", hivMetadata.getContinueRegimenConcept().getConceptId());
    map.put("1267", hivMetadata.getCompletedConcept().getConceptId());
    map.put("1406", hivMetadata.getOtherDiagnosis().getConceptId());
    map.put("42", tbMetadata.getPulmonaryTB().getConceptId());
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
            + "FROM   ( "
            + "         SELECT     p.patient_id, "
            + "                    e.encounter_datetime AS encounter_date "
            + "         FROM       patient p "
            + "         INNER JOIN encounter e "
            + "         ON         e.patient_id = p.patient_id "
            + "         INNER JOIN obs o "
            + "         ON         o.encounter_id = e.encounter_id "
            + "         INNER JOIN "
            + "                    ( "
            + "                           SELECT art_patient.patient_id "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 1)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 1)
            + " ) "
            + " ) art ON art.patient_id = e.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND e.encounter_type = ${6} "
            + " AND e.location_id = :location "
            + "  AND o.concept_id = ${23761} "
            + "  AND o.value_coded = ${1065} "
            + " AND e.encounter_datetime BETWEEN "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + "        ) ,INTERVAL 1 YEAR) "
            + " AND "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) ,INTERVAL 1 YEAR) "
            + " GROUP  BY p.patient_id "
            + " UNION "
            + "         SELECT     p.patient_id, "
            + "                    e.encounter_datetime AS encounter_date "
            + "         FROM       patient p "
            + "         INNER JOIN encounter e "
            + "         ON         e.patient_id = p.patient_id "
            + "         INNER JOIN obs o "
            + "         ON         o.encounter_id = e.encounter_id "
            + "         INNER JOIN "
            + "                    ( "
            + "                           SELECT art_patient.patient_id "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 2)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 2)
            + " ) "
            + " ) art ON art.patient_id = e.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND e.encounter_type = ${6} "
            + " AND e.location_id = :location "
            + "  AND o.concept_id = ${23761} "
            + "  AND o.value_coded = ${1065} "
            + " AND e.encounter_datetime BETWEEN "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + "        ) ,INTERVAL 2 YEAR) "
            + " AND "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) ,INTERVAL 2 YEAR) "
            + " GROUP  BY p.patient_id "
            + " UNION "
            + "         SELECT     p.patient_id, "
            + "                    e.encounter_datetime AS encounter_date "
            + "         FROM       patient p "
            + "         INNER JOIN encounter e "
            + "         ON         e.patient_id = p.patient_id "
            + "         INNER JOIN obs o "
            + "         ON         o.encounter_id = e.encounter_id "
            + "         INNER JOIN "
            + "                    ( "
            + "                           SELECT art_patient.patient_id "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 1)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 1)
            + " ) "
            + " ) art ON art.patient_id = e.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND e.encounter_type = ${6} "
            + " AND e.location_id = :location "
            + "  AND o.concept_id = ${23758} "
            + "  AND o.value_coded = ${1065} "
            + " AND e.encounter_datetime BETWEEN "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + "        ) ,INTERVAL 1 YEAR) "
            + " AND "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) ,INTERVAL 1 YEAR) "
            + " GROUP  BY p.patient_id "
            + " UNION "
            + "         SELECT     p.patient_id, "
            + "                    e.encounter_datetime AS encounter_date "
            + "         FROM       patient p "
            + "         INNER JOIN encounter e "
            + "         ON         e.patient_id = p.patient_id "
            + "         INNER JOIN obs o "
            + "         ON         o.encounter_id = e.encounter_id "
            + "         INNER JOIN "
            + "                    ( "
            + "                           SELECT art_patient.patient_id "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 2)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 2)
            + " ) "
            + " ) art ON art.patient_id = e.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND e.encounter_type = ${6} "
            + " AND e.location_id = :location "
            + "  AND o.concept_id = ${23758} "
            + "  AND o.value_coded = ${1065} "
            + " AND e.encounter_datetime BETWEEN "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + "        ) ,INTERVAL 2 YEAR) "
            + " AND "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) ,INTERVAL 2 YEAR) "
            + " GROUP  BY p.patient_id "
            + " UNION "
            + "         SELECT     p.patient_id, "
            + "                    e.encounter_datetime AS encounter_date "
            + "         FROM       patient p "
            + "         INNER JOIN encounter e "
            + "         ON         e.patient_id = p.patient_id "
            + "         INNER JOIN obs o "
            + "         ON         o.encounter_id = e.encounter_id "
            + "         INNER JOIN "
            + "                    ( "
            + "                           SELECT art_patient.patient_id "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 1)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 1)
            + " ) "
            + " ) art ON art.patient_id = e.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND e.encounter_type = ${6} "
            + " AND e.location_id = :location "
            + "               AND ( o.concept_id = ${1766} "
            + "                     AND o.value_coded IN( ${1763}, ${1764}, ${1762}, ${1760}, "
            + "                                           ${23760}, ${1765}, ${161} ) ) "
            + " AND e.encounter_datetime BETWEEN "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + "        ) ,INTERVAL 1 YEAR) "
            + " AND "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) ,INTERVAL 1 YEAR) "
            + " GROUP  BY p.patient_id "
            + " UNION "
            + "         SELECT     p.patient_id, "
            + "                    e.encounter_datetime AS encounter_date "
            + "         FROM       patient p "
            + "         INNER JOIN encounter e "
            + "         ON         e.patient_id = p.patient_id "
            + "         INNER JOIN obs o "
            + "         ON         o.encounter_id = e.encounter_id "
            + "         INNER JOIN "
            + "                    ( "
            + "                           SELECT art_patient.patient_id "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 2)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 2)
            + " ) "
            + " ) art ON art.patient_id = e.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND e.encounter_type = ${6} "
            + " AND e.location_id = :location "
            + "               AND ( o.concept_id = ${1766} "
            + "                     AND o.value_coded IN( ${1763}, ${1764}, ${1762}, ${1760}, "
            + "                                           ${23760}, ${1765}, ${161} ) ) "
            + " AND e.encounter_datetime BETWEEN "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + "        ) ,INTERVAL 2 YEAR) "
            + " AND "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) ,INTERVAL 2 YEAR) "
            + " GROUP  BY p.patient_id "
            + " UNION "
            + "         SELECT     p.patient_id, "
            + "                    o.obs_datetime AS encounter_date "
            + "         FROM       patient p "
            + "         INNER JOIN encounter e "
            + "         ON         e.patient_id = p.patient_id "
            + "         INNER JOIN obs o "
            + "         ON         o.encounter_id = e.encounter_id "
            + "         INNER JOIN "
            + "                    ( "
            + "                           SELECT art_patient.patient_id "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 1)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 1)
            + " ) "
            + " ) art ON art.patient_id = e.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND e.encounter_type = ${6} "
            + " AND e.location_id = :location "
            + "               AND o.concept_id = ${1268} "
            + "               AND o.value_coded IN ( ${1256}, ${1257}, ${1267} ) "
            + " AND o.obs_datetime BETWEEN "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + "        ) ,INTERVAL 1 YEAR) "
            + " AND "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) ,INTERVAL 1 YEAR) "
            + " GROUP  BY p.patient_id "
            + " UNION "
            + "         SELECT     p.patient_id, "
            + "                    o.obs_datetime AS encounter_date "
            + "         FROM       patient p "
            + "         INNER JOIN encounter e "
            + "         ON         e.patient_id = p.patient_id "
            + "         INNER JOIN obs o "
            + "         ON         o.encounter_id = e.encounter_id "
            + "         INNER JOIN "
            + "                    ( "
            + "                           SELECT art_patient.patient_id "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 2)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 2)
            + " ) "
            + " ) art ON art.patient_id = e.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND e.encounter_type = ${6} "
            + " AND e.location_id = :location "
            + "               AND o.concept_id = ${1268} "
            + "               AND o.value_coded IN ( ${1256}, ${1257}, ${1267} ) "
            + " AND o.obs_datetime BETWEEN "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + "        ) ,INTERVAL 2 YEAR) "
            + " AND "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) ,INTERVAL 2 YEAR) "
            + " GROUP  BY p.patient_id "
            + " UNION "
            + "         SELECT     p.patient_id, "
            + "                    o.obs_datetime AS encounter_date "
            + "         FROM       patient p "
            + "         INNER JOIN encounter e "
            + "         ON         e.patient_id = p.patient_id "
            + "         INNER JOIN obs o "
            + "         ON         o.encounter_id = e.encounter_id "
            + "         INNER JOIN "
            + "                    ( "
            + "                           SELECT art_patient.patient_id "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 1)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 1)
            + " ) "
            + " ) art ON art.patient_id = e.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND e.encounter_type = ${53} "
            + " AND e.location_id = :location "
            + "               AND o.concept_id = ${1406} "
            + "               AND o.value_coded IN ( ${42} ) "
            + " AND o.obs_datetime BETWEEN "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + "        ) ,INTERVAL 1 YEAR) "
            + " AND "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) ,INTERVAL 1 YEAR) "
            + " GROUP  BY p.patient_id "
            + " UNION "
            + "         SELECT     p.patient_id, "
            + "                    o.obs_datetime AS encounter_date "
            + "         FROM       patient p "
            + "         INNER JOIN encounter e "
            + "         ON         e.patient_id = p.patient_id "
            + "         INNER JOIN obs o "
            + "         ON         o.encounter_id = e.encounter_id "
            + "         INNER JOIN "
            + "                    ( "
            + "                           SELECT art_patient.patient_id "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 2)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 2)
            + " ) "
            + " ) art ON art.patient_id = e.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND e.encounter_type = ${53} "
            + " AND e.location_id = :location "
            + "               AND o.concept_id = ${1406} "
            + "               AND o.value_coded IN ( ${42} ) "
            + " AND o.obs_datetime BETWEEN "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + "        ) ,INTERVAL 2 YEAR) "
            + " AND "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) ,INTERVAL 2 YEAR) "
            + " GROUP  BY p.patient_id "
            + " UNION "
            + "         SELECT     p.patient_id, "
            + "                    o2.value_datetime AS encounter_date "
            + "         FROM       patient p "
            + "         INNER JOIN encounter e "
            + "         ON         e.patient_id = p.patient_id "
            + "         INNER JOIN obs o "
            + "         ON         o.encounter_id = e.encounter_id "
            + "         INNER JOIN obs o2 "
            + "         ON         o2.encounter_id = e.encounter_id "
            + "         INNER JOIN "
            + "                    ( "
            + "                           SELECT art_patient.patient_id "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 1)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 1)
            + " ) "
            + " ) art ON art.patient_id = e.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND o2.voided = 0 "
            + " AND e.encounter_type = ${53} "
            + " AND e.location_id = :location "
            + " AND ( ( o.concept_id = ${23985} "
            + " AND o.value_coded IN ( ${23954}, ${656}, ${165305}, ${165306} ) ) "
            + " AND ( o2.concept_id = ${165308} "
            + " AND o2.value_coded IN ( ${1256} ) ) ) "
            + " AND o2.value_datetime BETWEEN "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + "        ) ,INTERVAL 1 YEAR) "
            + " AND "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) ,INTERVAL 1 YEAR) "
            + " GROUP  BY p.patient_id "
            + " UNION "
            + "         SELECT     p.patient_id, "
            + "                    o2.value_datetime AS encounter_date "
            + "         FROM       patient p "
            + "         INNER JOIN encounter e "
            + "         ON         e.patient_id = p.patient_id "
            + "         INNER JOIN obs o "
            + "         ON         o.encounter_id = e.encounter_id "
            + "         INNER JOIN obs o2 "
            + "         ON         o2.encounter_id = e.encounter_id "
            + "         INNER JOIN "
            + "                    ( "
            + "                           SELECT art_patient.patient_id "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 2)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 2)
            + " ) "
            + " ) art ON art.patient_id = e.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND o2.voided = 0 "
            + " AND e.encounter_type = ${53} "
            + " AND e.location_id = :location "
            + " AND ( ( o.concept_id = ${23985} "
            + " AND o.value_coded IN ( ${23954}, ${656}, ${165305}, ${165306} ) ) "
            + " AND ( o2.concept_id = ${165308} "
            + " AND o2.value_coded IN ( ${1256} ) ) ) "
            + " AND o2.value_datetime BETWEEN "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + "        ) ,INTERVAL 1 YEAR) "
            + " AND "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) ,INTERVAL 1 YEAR) "
            + " GROUP  BY p.patient_id "
            + "               ) AS final_query";

    StringSubstitutor substitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(substitutor.replace(sql));
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
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());

    String query =
        "         SELECT     p.patient_id, "
            + "                    MIN(e.encounter_datetime) AS encounter_date "
            + "         FROM       patient p "
            + "         INNER JOIN encounter e "
            + "         ON         e.patient_id = p.patient_id "
            + "         INNER JOIN obs o "
            + "         ON         o.encounter_id = e.encounter_id "
            + "         INNER JOIN obs o2 "
            + "         ON         o2.encounter_id = e.encounter_id "
            + "         INNER JOIN "
            + "                    ( "
            + "                           SELECT art_patient.patient_id "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 1)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 1)
            + " ) "
            + " ) art ON art.patient_id = e.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND o2.voided = 0 "
            + " AND e.encounter_type = ${6} "
            + " AND e.location_id = :location "
            + " AND ( ( o.concept_id = ${23985} "
            + " AND o.value_coded IN ( ${23954}, ${656}, ${165305}, ${165306} ) ) "
            + " AND ( o2.concept_id = ${165308} "
            + " AND o2.value_coded IN ( ${1256} ) ) ) "
            + " AND e.encounter_datetime BETWEEN "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + "        ) ,INTERVAL 1 YEAR) "
            + " AND "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) ,INTERVAL 1 YEAR) "
            + " GROUP  BY p.patient_id "
            + " UNION "
            + "         SELECT     p.patient_id, "
            + "                    MIN(e.encounter_datetime) AS encounter_date "
            + "         FROM       patient p "
            + "         INNER JOIN encounter e "
            + "         ON         e.patient_id = p.patient_id "
            + "         INNER JOIN obs o "
            + "         ON         o.encounter_id = e.encounter_id "
            + "         INNER JOIN obs o2 "
            + "         ON         o2.encounter_id = e.encounter_id "
            + "         INNER JOIN "
            + "                    ( "
            + "                           SELECT art_patient.patient_id "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 2)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 2)
            + " ) "
            + " ) art ON art.patient_id = e.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND o2.voided = 0 "
            + " AND e.encounter_type = ${6} "
            + " AND e.location_id = :location "
            + " AND ( ( o.concept_id = ${23985} "
            + " AND o.value_coded IN ( ${23954}, ${656}, ${165305}, ${165306} ) ) "
            + " AND ( o2.concept_id = ${165308} "
            + " AND o2.value_coded IN ( ${1256} ) ) ) "
            + " AND e.encounter_datetime BETWEEN "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + "        ) ,INTERVAL 2 YEAR) "
            + " AND "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) ,INTERVAL 2 YEAR) "
            + " GROUP  BY p.patient_id "
            + " UNION "
            + "        SELECT     p.patient_id, "
            + "                    MIN(o2.obs_datetime) AS encounter_date "
            + "         FROM       patient p "
            + "         INNER JOIN encounter e "
            + "         ON         e.patient_id = p.patient_id "
            + "         INNER JOIN obs o "
            + "         ON         o.encounter_id = e.encounter_id "
            + "         INNER JOIN obs o2 "
            + "         ON         o2.encounter_id = e.encounter_id "
            + "         INNER JOIN "
            + "                    ( "
            + "                           SELECT art_patient.patient_id "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 1)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 1)
            + " ) "
            + " ) art ON art.patient_id = e.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND o2.voided = 0 "
            + " AND e.encounter_type = ${53} "
            + " AND e.location_id = :location "
            + " AND ( ( o.concept_id = ${23985} "
            + " AND o.value_coded IN ( ${23954}, ${656}, ${165305}, ${165306} ) ) "
            + " AND ( o2.concept_id = ${165308} "
            + " AND o2.value_coded IN ( ${1256} ) ) ) "
            + " AND o2.obs_datetime BETWEEN "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + "        ) ,INTERVAL 1 YEAR) "
            + " AND "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) ,INTERVAL 1 YEAR) "
            + " GROUP  BY p.patient_id "
            + " UNION "
            + "         SELECT     p.patient_id, "
            + "                    MIN(o2.obs_datetime) AS encounter_date "
            + "         FROM       patient p "
            + "         INNER JOIN encounter e "
            + "         ON         e.patient_id = p.patient_id "
            + "         INNER JOIN obs o "
            + "         ON         o.encounter_id = e.encounter_id "
            + "         INNER JOIN obs o2 "
            + "         ON         o2.encounter_id = e.encounter_id "
            + "         INNER JOIN "
            + "                    ( "
            + "                           SELECT art_patient.patient_id "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 2)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 2)
            + " ) "
            + " ) art ON art.patient_id = e.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND o2.voided = 0 "
            + " AND e.encounter_type = ${53} "
            + " AND e.location_id = :location "
            + " AND ( ( o.concept_id = ${23985} "
            + " AND o.value_coded IN ( ${23954}, ${656}, ${165305}, ${165306} ) ) "
            + " AND ( o2.concept_id = ${165308} "
            + " AND o2.value_coded IN ( ${1256} ) ) ) "
            + " AND o2.obs_datetime BETWEEN "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + "        ) ,INTERVAL 2 YEAR) "
            + " AND "
            + "  DATE_SUB( CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) ,INTERVAL 2 YEAR) "
            + " GROUP  BY p.patient_id ";

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
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());

    String query =
        "         SELECT     p.patient_id, "
            + "                    MIN(e.encounter_datetime) AS encounter_date "
            + "         FROM       patient p "
            + "         INNER JOIN encounter e "
            + "         ON         e.patient_id = p.patient_id "
            + "         INNER JOIN obs o "
            + "         ON         o.encounter_id = e.encounter_id "
            + "         INNER JOIN "
            + "                    ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.art_start AS art_encounter "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 1)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 1)
            + " ) "
            + " ) art ON art.patient_id = e.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND e.encounter_type = ${6} "
            + " AND e.location_id = :location "
            + " AND o.concept_id = ${1695} "
            + " AND e.encounter_datetime >= art.art_encounter "
            + " AND e.encounter_datetime <= DATE_ADD(art.art_encounter, INTERVAL 33 DAY) "
            + " UNION "
            + "         SELECT     p.patient_id, "
            + "                    MIN(e.encounter_datetime) AS encounter_date "
            + "         FROM       patient p "
            + "         INNER JOIN encounter e "
            + "         ON         e.patient_id = p.patient_id "
            + "         INNER JOIN obs o "
            + "         ON         o.encounter_id = e.encounter_id "
            + "         INNER JOIN "
            + "                    ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.art_start AS art_encounter "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 2)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 2)
            + " ) "
            + " ) art ON art.patient_id = e.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND e.encounter_type = ${6} "
            + " AND e.location_id = :location "
            + " AND o.concept_id = ${1695} "
            + " AND e.encounter_datetime >= art.art_encounter "
            + " AND e.encounter_datetime <= DATE_ADD(art.art_encounter, INTERVAL 33 DAY) "
            + " GROUP  BY p.patient_id ";

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
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());

    String query =
        "SELECT pt.patient_id, "
            + "       o.value_numeric AS cd4_result "
            + "FROM   patient pt "
            + "       INNER JOIN (SELECT pa.patient_id, "
            + "                          Min(enc.encounter_datetime) AS encounter_date "
            + "                   FROM   patient pa "
            + "                          INNER JOIN encounter enc "
            + "                                  ON enc.patient_id = pa.patient_id "
            + "                          INNER JOIN obs "
            + "                                  ON obs.encounter_id = enc.encounter_id "
            + "                          INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.art_start AS art_encounter "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 1)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 1)
            + " ) "
            + " ) art ON art.patient_id = enc.patient_id "
            + "                   WHERE  pa.voided = 0 "
            + "                          AND enc.voided = 0 "
            + "                          AND obs.voided = 0 "
            + "                          AND enc.encounter_type = ${6} "
            + "                          AND enc.location_id = :location "
            + "                          AND obs.concept_id = ${1695} "
            + "                          AND obs.value_numeric IS NOT NULL "
            + "                          AND enc.encounter_datetime >= art.art_encounter "
            + "                          AND enc.encounter_datetime <= DATE_ADD( "
            + "                              art.art_encounter, "
            + "                                                        INTERVAL 33 day) "
            + "                   GROUP  BY pa.patient_id "
            + "                   UNION "
            + "                   SELECT ee.patient_id, "
            + "                          Min(ee.encounter_datetime) AS encounter_date "
            + "                   FROM   encounter ee "
            + "                          INNER JOIN (SELECT pa.patient_id, "
            + "                                             Min(enc.encounter_datetime) AS "
            + "                                             encounter_date "
            + "                                      FROM   patient pa "
            + "                                             INNER JOIN encounter enc "
            + "                                                     ON enc.patient_id = "
            + "                                                        pa.patient_id "
            + "                                             INNER JOIN obs "
            + "                                                     ON obs.encounter_id = "
            + "                                                        enc.encounter_id "
            + "                                             INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.art_start AS art_encounter "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 1)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 1)
            + " ) "
            + " ) art ON art.patient_id = enc.patient_id "
            + "       WHERE  pa.voided = 0 "
            + "       AND enc.voided = 0 "
            + "       AND obs.voided = 0 "
            + "       AND enc.encounter_type = ${6} "
            + "       AND enc.location_id = :location "
            + "       AND obs.concept_id = ${1695} "
            + "       AND obs.value_numeric IS NOT NULL "
            + "       AND enc.encounter_datetime >= art.art_encounter "
            + "       AND enc.encounter_datetime <= DATE_ADD( "
            + "       art.art_encounter, "
            + "          INTERVAL 33 day) "
            + "       GROUP  BY pa.patient_id) first_encounter "
            + "       ON first_encounter.patient_id = ee.patient_id "
            + "       WHERE  ee.voided = 0 "
            + "       AND ee.encounter_type = ${6} "
            + "       AND ee.encounter_datetime > first_encounter.encounter_date) second_encounter "
            + "               ON pt.patient_id = second_encounter.patient_id "
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
            + "       AND enc.location_id = :location "
            + "GROUP  BY pt.patient_id "
            + "UNION "
            + "SELECT pt.patient_id, "
            + "       o.value_numeric AS cd4_result "
            + "FROM   patient pt "
            + "       INNER JOIN (SELECT pa.patient_id, "
            + "                          Min(enc.encounter_datetime) AS encounter_date "
            + "                   FROM   patient pa "
            + "                          INNER JOIN encounter enc "
            + "                                  ON enc.patient_id = pa.patient_id "
            + "                          INNER JOIN obs "
            + "                                  ON obs.encounter_id = enc.encounter_id "
            + "                          INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.art_start AS art_encounter "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 2)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 2)
            + " ) "
            + " ) art ON art.patient_id = enc.patient_id "
            + "                   WHERE  pa.voided = 0 "
            + "                          AND enc.voided = 0 "
            + "                          AND obs.voided = 0 "
            + "                          AND enc.encounter_type = ${6} "
            + "                          AND enc.location_id = :location "
            + "                          AND obs.concept_id = ${1695} "
            + "                          AND obs.value_numeric IS NOT NULL "
            + "                          AND enc.encounter_datetime >= art.art_encounter "
            + "                          AND enc.encounter_datetime <= DATE_ADD( "
            + "                              art.art_encounter, "
            + "                                                        INTERVAL 33 day) "
            + "                   GROUP  BY pa.patient_id "
            + "                   UNION "
            + "                   SELECT ee.patient_id, "
            + "                          Min(ee.encounter_datetime) AS encounter_date "
            + "                   FROM   encounter ee "
            + "                          INNER JOIN (SELECT pa.patient_id, "
            + "                                             Min(enc.encounter_datetime) AS "
            + "                                             encounter_date "
            + "                                      FROM   patient pa "
            + "                                             INNER JOIN encounter enc "
            + "                                                     ON enc.patient_id = "
            + "                                                        pa.patient_id "
            + "                                             INNER JOIN obs "
            + "                                                     ON obs.encounter_id = "
            + "                                                        enc.encounter_id "
            + "                                             INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.art_start AS art_encounter "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 2)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 2)
            + " ) "
            + " ) art ON art.patient_id = enc.patient_id "
            + "       WHERE  pa.voided = 0 "
            + "       AND enc.voided = 0 "
            + "       AND obs.voided = 0 "
            + "       AND enc.encounter_type = ${6} "
            + "       AND enc.location_id = :location "
            + "       AND obs.concept_id = ${1695} "
            + "       AND obs.value_numeric IS NOT NULL "
            + "       AND enc.encounter_datetime >= art.art_encounter "
            + "       AND enc.encounter_datetime <= DATE_ADD( "
            + "       art.art_encounter, "
            + "          INTERVAL 33 day) "
            + "       GROUP  BY pa.patient_id) first_encounter "
            + "       ON first_encounter.patient_id = ee.patient_id "
            + "       WHERE  ee.voided = 0 "
            + "       AND ee.encounter_type = ${6} "
            + "       AND ee.encounter_datetime > first_encounter.encounter_date) second_encounter "
            + "               ON pt.patient_id = second_encounter.patient_id "
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
            + "       AND enc.location_id = :location "
            + "GROUP  BY pt.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

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
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());

    String query =
        "SELECT p.patient_id, "
            + "                    Min(e.encounter_datetime) AS encounter_date "
            + "             FROM   patient p "
            + "                    INNER JOIN encounter e "
            + "                            ON e.patient_id = p.patient_id "
            + "                    INNER JOIN obs o "
            + "                            ON o.encounter_id = e.encounter_id "
            + "                    INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.art_start AS art_encounter "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 1)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 1)
            + " ) "
            + " ) art ON art.patient_id = e.patient_id "
            + "       WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${6} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${23722} "
            + "       AND o.value_coded = ${856} "
            + "       AND e.encounter_datetime >= art.art_encounter "
            + "             GROUP  BY p.patient_id "
            + "UNION "
            + "SELECT p.patient_id, "
            + "                    Min(e.encounter_datetime) AS encounter_date "
            + "             FROM   patient p "
            + "                    INNER JOIN encounter e "
            + "                            ON e.patient_id = p.patient_id "
            + "                    INNER JOIN obs o "
            + "                            ON o.encounter_id = e.encounter_id "
            + "                    INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.art_start AS art_encounter "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 2)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 2)
            + " ) "
            + " ) art ON art.patient_id = e.patient_id "
            + "       WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${6} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${23722} "
            + "       AND o.value_coded = ${856} "
            + "       AND e.encounter_datetime >= art.art_encounter "
            + "             GROUP  BY p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>Data do pedido da CV de seguimento- C.1 (coluna AP)</b>
   *
   * <p>O sistema irá determinar a Data do Pedido da CV de seguimento do utente identificando a data
   * da consulta clínica (Ficha Clínica), após o início TARV (Data Início TARV), na qual foi
   * efectuado o registo do segundo Pedido de Carga Viral..<br>
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
  public DataDefinition getSecondtViralLoad() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Data do pedido da CV de seguimento");
    sqlPatientDataDefinition.addParameter(
        new Parameter("evaluationYear", "evaluationYear", Integer.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String query =
        "                   SELECT ee2.patient_id, "
            + "                          Min(ee2.encounter_datetime) AS encounter_date "
            + "                   FROM   encounter ee2 "
            + "                   INNER JOIN ( "
            + "                   SELECT ee.patient_id, "
            + "                          Min(ee.encounter_datetime) AS encounter_date "
            + "                   FROM   encounter ee "
            + "                          INNER JOIN (SELECT pa.patient_id, "
            + "                                             Min(enc.encounter_datetime) AS "
            + "                                             encounter_date "
            + "                                      FROM   patient pa "
            + "                                             INNER JOIN encounter enc "
            + "                                                     ON enc.patient_id = "
            + "                                                        pa.patient_id "
            + "                                             INNER JOIN obs "
            + "                                                     ON obs.encounter_id = "
            + "                                                        enc.encounter_id "
            + "                                             INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.art_start AS art_encounter "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 1)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 1)
            + " ) "
            + " ) art ON art.patient_id = enc.patient_id "
            + "       WHERE  pa.voided = 0 "
            + "       AND enc.voided = 0 "
            + "       AND obs.voided = 0 "
            + "       AND enc.encounter_type = ${6} "
            + "       AND enc.location_id = :location "
            + "       AND obs.concept_id = ${23722} "
            + "       AND obs.value_coded = ${856} "
            + "       AND enc.encounter_datetime >= art.art_encounter "
            + "       GROUP  BY pa.patient_id) first_encounter "
            + "       ON first_encounter.patient_id = ee.patient_id "
            + "       WHERE  ee.voided = 0 "
            + "       AND ee.encounter_type = ${6} "
            + "       AND ee.encounter_datetime > first_encounter.encounter_date "
            + "       GROUP BY ee.patient_id) second_encounter "
            + "               ON ee2.patient_id = second_encounter.patient_id "
            + "WHERE  ee2.voided = 0 "
            + "       AND ee2.encounter_type = ${6} "
            + "       AND ee2.location_id = :location "
            + "GROUP  BY ee2.patient_id "
            + "UNION "
            + "                   SELECT ee2.patient_id, "
            + "                          Min(ee2.encounter_datetime) AS encounter_date "
            + "                   FROM   encounter ee2 "
            + "                   INNER JOIN ( "
            + "                   SELECT ee.patient_id, "
            + "                          Min(ee.encounter_datetime) AS encounter_date "
            + "                   FROM   encounter ee "
            + "                          INNER JOIN (SELECT pa.patient_id, "
            + "                                             Min(enc.encounter_datetime) AS "
            + "                                             encounter_date "
            + "                                      FROM   patient pa "
            + "                                             INNER JOIN encounter enc "
            + "                                                     ON enc.patient_id = "
            + "                                                        pa.patient_id "
            + "                                             INNER JOIN obs "
            + "                                                     ON obs.encounter_id = "
            + "                                                        enc.encounter_id "
            + "                                             INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.art_start AS art_encounter "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 2)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 2)
            + " ) "
            + " ) art ON art.patient_id = enc.patient_id "
            + "       WHERE  pa.voided = 0 "
            + "       AND enc.voided = 0 "
            + "       AND obs.voided = 0 "
            + "       AND enc.encounter_type = ${6} "
            + "       AND enc.location_id = :location "
            + "       AND obs.concept_id = ${23722} "
            + "       AND obs.value_coded = ${856} "
            + "       AND enc.encounter_datetime >= art.art_encounter "
            + "       GROUP  BY pa.patient_id) first_encounter "
            + "       ON first_encounter.patient_id = ee.patient_id "
            + "       WHERE  ee.voided = 0 "
            + "       AND ee.encounter_type = ${6} "
            + "       AND ee.encounter_datetime > first_encounter.encounter_date "
            + "       GROUP BY ee.patient_id) second_encounter "
            + "               ON ee2.patient_id = second_encounter.patient_id "
            + "WHERE  ee2.voided = 0 "
            + "       AND ee2.encounter_type = ${6} "
            + "       AND ee2.location_id = :location "
            + "GROUP  BY ee2.patient_id ";

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
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());

    String query =
        "                   SELECT cd4_2.patient_id, "
            + "                          Min(obs2.value_numeric) AS second_cd4_result "
            + "                   FROM   patient cd4_2 "
            + "                          INNER JOIN encounter enc2 "
            + "                                 ON enc2.patient_id = cd4_2.patient_id "
            + "                          INNER JOIN obs obs2 "
            + "                                 ON enc2.encounter_id = obs2.encounter_id "
            + "                          INNER JOIN ( "
            + "                   SELECT cd4_1.patient_id, "
            + "                          Min(obs1.value_numeric) AS first_cd4_result "
            + "                   FROM   patient cd4_1 "
            + "                          INNER JOIN encounter enc1 "
            + "                                 ON enc1.patient_id = cd4_1.patient_id "
            + "                          INNER JOIN obs obs1 "
            + "                                 ON enc1.encounter_id = obs1.encounter_id "
            + "                          INNER JOIN (SELECT pa.patient_id, "
            + "                                             Min(enc.encounter_datetime) AS "
            + "                                             encounter_date "
            + "                                      FROM   patient pa "
            + "                                             INNER JOIN encounter enc "
            + "                                                     ON enc.patient_id = "
            + "                                                        pa.patient_id "
            + "                                             INNER JOIN obs "
            + "                                                     ON obs.encounter_id = "
            + "                                                        enc.encounter_id "
            + "                                             INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.art_start AS art_encounter "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 1)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 1)
            + " ) "
            + " ) art ON art.patient_id = enc.patient_id "
            + "       WHERE  pa.voided = 0 "
            + "       AND enc.voided = 0 "
            + "       AND obs.voided = 0 "
            + "       AND enc.encounter_type = ${6} "
            + "       AND enc.location_id = :location "
            + "       AND obs.concept_id = ${1695} "
            + "       AND obs.value_numeric IS NOT NULL "
            + "       AND enc.encounter_datetime >= DATE_ADD(art.art_encounter, INTERVAL 33 DAY) "
            + "       AND enc.encounter_datetime <= DATE_ADD(art.art_encounter, INTERVAL 12 MONTH) "
            + "       GROUP  BY pa.patient_id) first_cd4_result "
            + "       ON first_cd4_result.patient_id = cd4_1.patient_id "
            + "       WHERE  cd4_1.voided = 0 "
            + "       AND enc1.voided = 0 "
            + "       AND obs1.voided = 0 "
            + "       AND enc1.encounter_type = ${6} "
            + "       AND obs1.concept_id = ${1695} "
            + "       AND obs1.value_numeric IS NOT NULL "
            + "       AND enc1.encounter_datetime > first_cd4_result.encounter_date "
            + "       GROUP BY cd4_1.patient_id) second_cd4_result "
            + "       ON second_cd4_result.patient_id = cd4_2.patient_id "
            + "       WHERE  cd4_2.voided = 0 "
            + "       AND enc2.voided = 0"
            + "       AND obs2.voided = 0"
            + "       AND enc2.location_id = :location "
            + "       AND enc2.encounter_type = ${6} "
            + "       AND obs2.concept_id = ${1695} "
            + "       AND obs2.value_numeric IS NOT NULL "
            + "       GROUP BY cd4_2.patient_id "
            + "UNION "
            + "                   SELECT cd4_2.patient_id, "
            + "                          Min(obs2.value_numeric) AS second_cd4_result "
            + "                   FROM   patient cd4_2 "
            + "                          INNER JOIN encounter enc2 "
            + "                                 ON enc2.patient_id = cd4_2.patient_id "
            + "                          INNER JOIN obs obs2 "
            + "                                 ON enc2.encounter_id = obs2.encounter_id "
            + "                          INNER JOIN ( "
            + "                   SELECT cd4_1.patient_id, "
            + "                          Min(obs1.value_numeric) AS first_cd4_result "
            + "                   FROM   patient cd4_1 "
            + "                          INNER JOIN encounter enc1 "
            + "                                 ON enc1.patient_id = cd4_1.patient_id "
            + "                          INNER JOIN obs obs1 "
            + "                                 ON enc1.encounter_id = obs1.encounter_id "
            + "                          INNER JOIN (SELECT pa.patient_id, "
            + "                                             Min(enc.encounter_datetime) AS "
            + "                                             encounter_date "
            + "                                      FROM   patient pa "
            + "                                             INNER JOIN encounter enc "
            + "                                                     ON enc.patient_id = "
            + "                                                        pa.patient_id "
            + "                                             INNER JOIN obs "
            + "                                                     ON obs.encounter_id = "
            + "                                                        enc.encounter_id "
            + "                                             INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.art_start AS art_encounter "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 2)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 2)
            + " ) "
            + " ) art ON art.patient_id = enc.patient_id "
            + "       WHERE  pa.voided = 0 "
            + "       AND enc.voided = 0 "
            + "       AND obs.voided = 0 "
            + "       AND enc.encounter_type = ${6} "
            + "       AND enc.location_id = :location "
            + "       AND obs.concept_id = ${1695} "
            + "       AND obs.value_numeric IS NOT NULL "
            + "       AND enc.encounter_datetime >= DATE_ADD(art.art_encounter, INTERVAL 33 DAY) "
            + "       AND enc.encounter_datetime <= DATE_ADD(art.art_encounter, INTERVAL 12 MONTH) "
            + "       GROUP  BY pa.patient_id) first_cd4_result "
            + "       ON first_cd4_result.patient_id = cd4_1.patient_id "
            + "       WHERE  cd4_1.voided = 0 "
            + "       AND enc1.voided = 0 "
            + "       AND obs1.voided = 0 "
            + "       AND enc1.encounter_type = ${6} "
            + "       AND obs1.concept_id = ${1695} "
            + "       AND obs1.value_numeric IS NOT NULL "
            + "       AND enc1.encounter_datetime > first_cd4_result.encounter_date "
            + "       GROUP BY cd4_1.patient_id) second_cd4_result "
            + "       ON second_cd4_result.patient_id = cd4_2.patient_id "
            + "       WHERE  cd4_2.voided = 0 "
            + "       AND enc2.voided = 0"
            + "       AND obs2.voided = 0"
            + "       AND enc2.location_id = :location "
            + "       AND enc2.encounter_type = ${6} "
            + "       AND obs2.concept_id = ${1695} "
            + "       AND obs2.value_numeric IS NOT NULL "
            + "       GROUP BY cd4_2.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF35 - Resultado do CD4 feito entre 12˚ e 24˚ mês de TARV- C.4 (Coluna AS)</b>
   *
   * <p>O sistema irá determinar o Resultado do CD4 do utente seleccionando o resultado do CD4 mais
   * recente registado na consulta Clínica (Ficha Clínica) entre 12 e 24 meses do Início TARV (“Data
   * da Consulta” <= “Data Início TARV” + 12 meses e >= “Data Início TARV” + 24 meses). .
   *
   * <p>Nota 1: Nota 1: A “Data Início TARV” é definida no RF46. <br>
   * <br>
   *
   * <p>Nota 2: O utente a ser considerado nesta definição iniciou TARV ou na coorte de 12 meses ou
   * na coorte de 24 meses, conforme definido no RF4.
   *
   * @return {DataDefinition}
   */
  public DataDefinition getCd4ResultSectionC() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Resultado do CD4 feito entre 12˚ e 24˚ mês de TARV");
    sqlPatientDataDefinition.addParameter(
        new Parameter("evaluationYear", "evaluationYear", Integer.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());

    String query =
        "       SELECT cd4.patient_id, "
            + "         obs.value_numeric AS cd4_result "
            + "  FROM   patient cd4 "
            + "         INNER JOIN encounter enc "
            + "                ON enc.patient_id = cd4.patient_id "
            + "         INNER JOIN obs  "
            + "                ON enc.encounter_id = obs.encounter_id "
            + "      INNER JOIN (SELECT pa.patient_id, "
            + "    MAX(enc.encounter_datetime) AS "
            + "                encounter_date "
            + "    FROM   patient pa "
            + "           INNER JOIN encounter enc "
            + "                   ON enc.patient_id = "
            + "                      pa.patient_id "
            + "           INNER JOIN obs "
            + "                   ON obs.encounter_id = "
            + "                      enc.encounter_id "
            + "           INNER JOIN ( "
            + "     SELECT art_patient.patient_id, "
            + "            art_patient.art_start AS art_encounter "
            + "     FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 1)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 1)
            + " ) "
            + " ) art ON art.patient_id = enc.patient_id "
            + "       WHERE  pa.voided = 0 "
            + "       AND enc.voided = 0 "
            + "       AND obs.voided = 0 "
            + "       AND enc.encounter_type = ${6} "
            + "       AND enc.location_id = :location "
            + "       AND obs.concept_id = ${1695} "
            + "       AND obs.value_numeric IS NOT NULL "
            + "       AND enc.encounter_datetime >= DATE_ADD(art.art_encounter, INTERVAL 12 MONTH) "
            + "       AND enc.encounter_datetime <= DATE_ADD(art.art_encounter, INTERVAL 24 MONTH) "
            + "       GROUP  BY pa.patient_id) most_recent_cd4 "
            + "       ON most_recent_cd4.patient_id = cd4.patient_id "
            + "       WHERE  cd4.voided = 0 "
            + "       AND enc.voided = 0 "
            + "       AND obs.voided = 0 "
            + "       AND enc.encounter_type = ${6} "
            + "       AND obs.concept_id = ${1695} "
            + "       AND obs.value_numeric IS NOT NULL "
            + "       GROUP BY cd4.patient_id "
            + "UNION "
            + "       SELECT cd4.patient_id, "
            + "         obs.value_numeric AS cd4_result "
            + "  FROM   patient cd4 "
            + "         INNER JOIN encounter enc "
            + "                ON enc.patient_id = cd4.patient_id "
            + "         INNER JOIN obs  "
            + "                ON enc.encounter_id = obs.encounter_id "
            + "      INNER JOIN (SELECT pa.patient_id, "
            + "    MAX(enc.encounter_datetime) AS "
            + "                encounter_date "
            + "    FROM   patient pa "
            + "           INNER JOIN encounter enc "
            + "                   ON enc.patient_id = "
            + "                      pa.patient_id "
            + "           INNER JOIN obs "
            + "                   ON obs.encounter_id = "
            + "                      enc.encounter_id "
            + "           INNER JOIN ( "
            + "     SELECT art_patient.patient_id, "
            + "            art_patient.art_start AS art_encounter "
            + "     FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 2)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 2)
            + " ) "
            + " ) art ON art.patient_id = enc.patient_id "
            + "       WHERE  pa.voided = 0 "
            + "       AND enc.voided = 0 "
            + "       AND obs.voided = 0 "
            + "       AND enc.encounter_type = ${6} "
            + "       AND enc.location_id = :location "
            + "       AND obs.concept_id = ${1695} "
            + "       AND obs.value_numeric IS NOT NULL "
            + "       AND enc.encounter_datetime >= DATE_ADD(art.art_encounter, INTERVAL 12 MONTH) "
            + "       AND enc.encounter_datetime <= DATE_ADD(art.art_encounter, INTERVAL 24 MONTH) "
            + "       GROUP  BY pa.patient_id) most_recent_cd4 "
            + "       ON most_recent_cd4.patient_id = cd4.patient_id "
            + "       WHERE  cd4.voided = 0 "
            + "       AND enc.voided = 0 "
            + "       AND obs.voided = 0 "
            + "       AND enc.encounter_type = ${6} "
            + "       AND obs.concept_id = ${1695} "
            + "       AND obs.value_numeric IS NOT NULL "
            + "       GROUP BY cd4.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF21 - Teve registo de boa adesão em TODAS consultas entre 1˚ e 3˚ mês de TARV? - B.5
   * (Coluna N)</b><br>
   * <br>
   *
   * <p>O sistema irá determinar se o utente “Teve registo de boa adesão em TODAS consultas entre 1˚
   * e 3˚ mês de TARV?” com as seguintes respostas:
   *
   * <p>Resposta= Sim, se o utente teve o registo do campo de "Seguimento da Adesão - Adesão ao
   * TARV” com resposta = “BOA” em todas as consultas de APSS/PP decorridas entre 33 dias e 3 meses
   * do Início TARV (“Data da Consulta APSS/PP” >= “Data Início TARV” + 33 dias e <= “Data Início
   * TARV” + 3 meses) * <br>
   * <br>
   *
   * <p>Resposta= Não, se o utente teve pelo menos um registo no campo de "Seguimento da Adesão -
   * Adesão ao TARV” com resposta = “RISCO” ou “MÁ” decorrida entre 33 dias e 3 meses do Início TARV
   * (“Data da Consulta APSS/PP” >= “Data Início TARV” + 33 dias e <= “Data Início TARV” + 3 meses)
   * <br>
   * <br>
   *
   * <p>Nota 1: Caso o utente não satisfaça o critério definido para resposta = Sim e resposta =
   * Não, o sistema não irá preencher nenhuma informação. * <br>
   * <br>
   *
   * <p>Nota 2: A “Data Início TARV” é a data registada na Ficha Resumo (“Data do Início TARV”).
   * <br>
   * <br>
   *
   * <p>Nota 3: O utente a ser considerado nesta definição iniciou TARV ou na coorte de 12 meses ou
   * na coorte de 24 meses, conforme definido no RF4.
   *
   * @return {DataDefinition}
   */
  public DataDefinition getPatientsWithGoodAdhesion() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(
        "Teve registo de boa adesão em TODAS consultas entre 1˚ e 3˚ mês de TARV?; (coluna N) – Resposta = Sim ou Não");
    sqlPatientDataDefinition.addParameter(
        new Parameter("evaluationYear", "evaluationYear", Integer.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());
    map.put("6223", hivMetadata.getAdherenceEvaluationConcept().getConceptId());
    map.put("1383", hivMetadata.getPatientIsDead().getConceptId());
    map.put("1749", hivMetadata.getArvAdherenceRiskConcept().getConceptId());
    map.put("1385", hivMetadata.getBadConcept().getConceptId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());

    String query =
        "SELECT final_query.patient_id, "
            + "       CASE "
            + "              WHEN final_query.encounter_date IS NULL THEN 'Não' "
            + "              WHEN final_query.encounter_date IS NOT NULL THEN 'Sim' "
            + "              ELSE '' "
            + "       end "
            + "FROM   ( "
            + "                  SELECT     p.patient_id, "
            + "                             e.encounter_datetime AS encounter_date "
            + "                  FROM       patient p "
            + "                  INNER JOIN encounter e "
            + "                  ON         e.patient_id = p.patient_id "
            + "                  INNER JOIN obs o "
            + "                  ON         o.encounter_id = e.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.art_start AS art_encounter "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 1)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 1)
            + " ) "
            + "                             ) art "
            + "                  ON         art.patient_id = p.patient_id "
            + "                  WHERE      p.voided = 0 "
            + "                  AND        o.voided = 0 "
            + "                  AND        e.encounter_type = ${35} "
            + "                  AND        e.location_id = :location "
            + "                  AND        e.encounter_datetime >= DATE_ADD( art.art_encounter, INTERVAL 33 DAY) "
            + "                  AND        e.encounter_datetime <= DATE_ADD( art.art_encounter, INTERVAL 3 MONTH) "
            + "                  AND        o.concept_id = ${6223} "
            + "                  AND        o.value_coded IN ( ${1383}, "
            + "                                               ${1749}, "
            + "                                               ${1385} ) "
            + "                  GROUP BY   p.patient_id "
            + "                  UNION "
            + "                  SELECT     p.patient_id, "
            + "                             e.encounter_datetime AS encounter_date "
            + "                  FROM       patient p "
            + "                  INNER JOIN encounter e "
            + "                  ON         e.patient_id = p.patient_id "
            + "                  INNER JOIN obs o "
            + "                  ON         o.encounter_id = e.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.art_start AS art_encounter "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 2)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 2)
            + " ) "
            + "                             ) art "
            + "                  ON         art.patient_id = p.patient_id "
            + "                  WHERE      p.voided = 0 "
            + "                  AND        o.voided = 0 "
            + "                  AND        e.encounter_type = ${35} "
            + "                  AND        e.location_id = :location "
            + "                  AND        e.encounter_datetime >= DATE_ADD( art.art_encounter, INTERVAL 33 DAY) "
            + "                  AND        e.encounter_datetime <= DATE_ADD( art.art_encounter, INTERVAL 3 MONTH) "
            + "                  AND        o.concept_id = ${6223} "
            + "                  AND        o.value_coded IN ( ${1383}, "
            + "                                               ${1749}, "
            + "                                               ${1385} ) "
            + "                  GROUP BY   p.patient_id ) AS final_query";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF36 - Teve registo de boa adesão em TODAS consultas entre 12˚ e 24˚ mês de TARV?- C5
   * (coluna AT)</b><br>
   * <br>
   *
   * <p>O sistema irá determinar se o utente “Teve registo de boa adesão em TODAS consultas entre 1˚
   * e 3˚ mês de TARV?” com as seguintes respostas:
   *
   * <p>Resposta= Sim, se o utente teve o registo do campo de "Seguimento da Adesão - Adesão ao
   * TARV” com resposta = “BOA” em todas as consultas de APSS/PP decorridas entre 12 e 24 meses do
   * Início TARV (“Data da Consulta APSS/PP” >= “Data Início TARV” + 12 meses e <= “Data Início
   * TARV” + 24 meses)<br>
   * <br>
   *
   * <p>Resposta= Não, se o utente teve pelo menos um registo no campo de "Seguimento da Adesão -
   * Adesão ao TARV” com resposta = “RISCO” ou “MÁ” decorrida entre 12 e 24 meses do Início TARV
   * (“Data da Consulta APSS/PP” >= “Data Início TARV” + 12 meses e <= “Data Início TARV” + 24
   * meses) <br>
   * <br>
   *
   * <p>Nota 1: Caso o utente não satisfaça o critério definido para resposta = Sim e resposta =
   * Não, o sistema não irá preencher nenhuma informação. <br>
   * <br>
   *
   * <p>Nota 2: A “Data Início TARV” é definida no RF46. <br>
   * <br>
   *
   * <p>Nota 3: O utente a ser considerado nesta definição iniciou TARV ou na coorte de 12 meses ou
   * na coorte de 24 meses, conforme definido no RF4.
   *
   * @return {DataDefinition}
   */
  public DataDefinition getPatientsWithGoodAdhesionAfterAYearInTarv() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(
        "Registo de boa adesão em TODAS consultas entre 12˚ e 24˚ mês de TARV");
    sqlPatientDataDefinition.addParameter(
        new Parameter("evaluationYear", "evaluationYear", Integer.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());
    map.put("6223", hivMetadata.getAdherenceEvaluationConcept().getConceptId());
    map.put("1383", hivMetadata.getPatientIsDead().getConceptId());
    map.put("1749", hivMetadata.getArvAdherenceRiskConcept().getConceptId());
    map.put("1385", hivMetadata.getBadConcept().getConceptId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());

    String query =
        "SELECT final_query.patient_id, "
            + "       CASE "
            + "              WHEN final_query.encounter_date IS NULL THEN 'Não' "
            + "              WHEN final_query.encounter_date IS NOT NULL THEN 'Sim' "
            + "              ELSE '' "
            + "       end "
            + "FROM   ( "
            + "                  SELECT     p.patient_id, "
            + "                             e.encounter_datetime AS encounter_date "
            + "                  FROM       patient p "
            + "                  INNER JOIN encounter e "
            + "                  ON         e.patient_id = p.patient_id "
            + "                  INNER JOIN obs o "
            + "                  ON         o.encounter_id = e.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.art_start AS art_encounter "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 1)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 1)
            + " ) "
            + "                             ) art "
            + "                  ON         art.patient_id = p.patient_id "
            + "                  WHERE      p.voided = 0 "
            + "                  AND        o.voided = 0 "
            + "                  AND        e.encounter_type = ${35} "
            + "                  AND        e.location_id = :location "
            + "                  AND        e.encounter_datetime >= DATE_ADD( art.art_encounter, INTERVAL 12 MONTH) "
            + "                  AND        e.encounter_datetime <= DATE_ADD( art.art_encounter, INTERVAL 24 MONTH) "
            + "                  AND        o.concept_id = ${6223} "
            + "                  AND        o.value_coded IN ( ${1383}, "
            + "                                               ${1749}, "
            + "                                               ${1385} ) "
            + "                  GROUP BY   p.patient_id "
            + "                  UNION "
            + "                  SELECT     p.patient_id, "
            + "                             e.encounter_datetime AS encounter_date "
            + "                  FROM       patient p "
            + "                  INNER JOIN encounter e "
            + "                  ON         e.patient_id = p.patient_id "
            + "                  INNER JOIN obs o "
            + "                  ON         o.encounter_id = e.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.art_start AS art_encounter "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 2)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 2)
            + " ) "
            + "                             ) art "
            + "                  ON         art.patient_id = p.patient_id "
            + "                  WHERE      p.voided = 0 "
            + "                  AND        o.voided = 0 "
            + "                  AND        e.encounter_type = ${35} "
            + "                  AND        e.location_id = :location "
            + "                  AND        e.encounter_datetime >= DATE_ADD( art.art_encounter, INTERVAL 12 MONTH) "
            + "                  AND        e.encounter_datetime <= DATE_ADD( art.art_encounter, INTERVAL 24 MONTH) "
            + "                  AND        o.concept_id = ${6223} "
            + "                  AND        o.value_coded IN ( ${1383}, "
            + "                                               ${1749}, "
            + "                                               ${1385} ) "
            + "                  GROUP BY   p.patient_id ) AS final_query";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF22 - Esteve grávida ou foi lactante entre 3˚ e 9º mês de TARV?- B.6 (Coluna O)</b><br>
   * <br>
   *
   * <p>O sistema irá identificar se o utente “Esteve grávida ou foi lactante entre 3˚ e 9º mês de
   * TARV?” com as seguintes respostas:
   *
   * <p>Resposta= Sim, se o utente é do sexo feminino, com idade > 9 anos (RF10) e que teve um
   * registo como “Grávida=G” ou “Lactante=L” numa consulta clínica (“Ficha Clinica”) decorrida
   * entre 3 meses a 9 meses do início TARV (Data da Consulta >= “Data Início TARV” + 3 meses e <=
   * “Data Início TARV” + 9 meses) <br>
   * <br>
   *
   * <p>Resposta= Não, se o utente é do sexo feminino, com idade > 9 anos (RF10) e que não teve um
   * registo como “Grávida=G” ou “Lactante=L” numa consulta clínica (“Ficha Clinica”) decorrida
   * entre 3 meses a 9 meses do início TARV (Data da Consulta >= “Data Início TARV” + 3 meses e <=
   * “Data Início TARV” + 9 meses) <br>
   * <br>
   *
   * <p>Nota 1: Caso o utente não satisfaça o critério definido para resposta = Sim e resposta =
   * Não, o sistema não irá preencher nenhuma informação. <br>
   * <br>
   *
   * <p>Nota 2: A “Data Início TARV” é a data registada na Ficha Resumo (“Data do Início TARV”).
   * <br>
   * <br>
   *
   * <p>Nota 3: O utente a ser considerado nesta definição iniciou TARV ou na coorte de 12 meses ou
   * na coorte de 24 meses, conforme definido no RF4.
   *
   * @return {DataDefinition}
   */
  public DataDefinition getPatientsPregnantBreastfeeding3MonthsTarv(
      int minnumberYears, int maxnumberYearsYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(
        "B6- Esteve grávida ou foi lactante entre 3˚ e 9º mês de TARV?: (coluna M)- Resposta = Sim ou Não");
    sqlPatientDataDefinition.addParameter(
        new Parameter("evaluationYear", "evaluationYear", Integer.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("6332", hivMetadata.getBreastfeeding().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());

    String query =
        "SELECT final_query.patient_id, "
            + "       CASE "
            + "              WHEN final_query.encounter_date IS NULL THEN 'Não' "
            + "              WHEN final_query.encounter_date IS NOT NULL THEN 'Sim' "
            + "              ELSE '' "
            + "       end "
            + "FROM   ( "
            + "                  SELECT     p.patient_id, "
            + "                             e.encounter_datetime AS encounter_date "
            + "                  FROM       patient p "
            + "                  INNER JOIN encounter e "
            + "                  ON         e.patient_id = p.patient_id "
            + "                  INNER JOIN obs o "
            + "                  ON         o.encounter_id = e.encounter_id "
            + "                  INNER JOIN obs o2 "
            + "                  ON         o2.encounter_id = e.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.art_start AS art_encounter "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 1)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 1)
            + " ) "
            + "                             ) art "
            + "                  ON         art.patient_id = p.patient_id "
            + "                  WHERE      p.voided = 0 "
            + "                  AND        o.voided = 0 "
            + "                  AND        o2.voided = 0 "
            + "                  AND        e.encounter_type = ${6} "
            + "                  AND        e.location_id = :location "
            + "                  AND        e.encounter_datetime >= date_add( art.art_encounter, INTERVAL "
            + minnumberYears
            + " MONTH ) "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + maxnumberYearsYears
            + " MONTH ) "
            + "                  AND        o.concept_id = ${1982} "
            + "                  AND        o.value_coded IN ( ${1065}, "
            + "                                               ${1066} ) "
            + "                  AND        o2.concept_id = ${6332} "
            + "                  AND        o2.value_coded IN ( ${1065}, "
            + "                                               ${1066} ) "
            + "                  GROUP BY   p.patient_id "
            + "                  UNION "
            + "                  SELECT     p.patient_id, "
            + "                             e.encounter_datetime AS encounter_date "
            + "                  FROM       patient p "
            + "                  INNER JOIN encounter e "
            + "                  ON         e.patient_id = p.patient_id "
            + "                  INNER JOIN obs o "
            + "                  ON         o.encounter_id = e.encounter_id "
            + "                  INNER JOIN obs o2 "
            + "                  ON         o2.encounter_id = e.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.art_start AS art_encounter "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 2)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 2)
            + " ) "
            + "                             ) art "
            + "                  ON         art.patient_id = p.patient_id "
            + "                  WHERE      p.voided = 0 "
            + "                  AND        o.voided = 0 "
            + "                  AND        o2.voided = 0 "
            + "                  AND        e.encounter_type = ${6} "
            + "                  AND        e.location_id = :location "
            + "                  AND        e.encounter_datetime >= date_add( art.art_encounter, INTERVAL "
            + minnumberYears
            + " MONTH ) "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + maxnumberYearsYears
            + " MONTH ) "
            + "                  AND        o.concept_id = ${1982} "
            + "                  AND        o.value_coded IN ( ${1065}, "
            + "                                               ${1066} ) "
            + "                  AND        o2.concept_id = ${6332} "
            + "                  AND        o2.value_coded IN ( ${1065}, "
            + "                                               ${1066} ) "
            + "                  GROUP BY   p.patient_id ) AS final_query";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF23 - Teve TB entre o 3˚ e 9 mês de TARV- B.8 (Coluna Q)</b><br>
   * <br>
   *
   * <p>O sistema irá determinar se o utente “Teve TB entre o 3˚ e 9 mês de TARV? com as seguintes
   * respostas:
   *
   * <p>Resposta= Sim, se o utente teve um dos seguintes registos em pelo menos uma consulta clínica
   * (Ficha Clínica) decorrida entre 3 a 9 meses após o início TARV (Data da Consulta >= “Data
   * Início TARV” + 3 meses e <= “Data Início TARV” + 9 meses): “Tem Sintomas=” com resposta = “Sim”
   * ou “Quais Sintomas de TB?” com resposta = “Febre” ou “Emagrecimento” ou "Sudorese noturna” ou
   * “Tosse com ou sem sangue” ou “Astenia” ou “Contacto recente com TB” ou “Diagnóstico de TB
   * Activa?” com resposta= SIM <br>
   * <br>
   *
   * <p>Resposta= Não, se o utente não teve nenhum dos seguintes registos numa consulta clínica
   * (Ficha Clínica) decorrida entre 3 a 9 meses após o início TARV (Data da Consulta >= “Data
   * Início TARV” + 3 meses e <= “Data Início TARV” + 9 meses): “Tem Sintomas=” com resposta = “Sim”
   * ou “Quais Sintomas de TB?” com resposta = “Febre” ou “Emagrecimento” ou "Sudorese noturna” ou
   * “Tosse com ou sem sangue” ou “Astenia” ou “Contacto recente com TB” “Diagnóstico de TB Activa?”
   * com resposta= SIM <br>
   * <br>
   *
   * <p>Nota 1: caso o utente não satisfaça o critério definido para resposta = Sim e resposta =
   * Não, o sistema não irá preencher nenhuma informação.<br>
   * <br>
   *
   * <p>Nota 2: A “Data Início TARV” é a data registada na Ficha Resumo (“Data do Início TARV”).
   * <br>
   * <br>
   *
   * <p>Nota 3: O utente a ser considerado nesta definição iniciou TARV ou na coorte de 12 meses ou
   * na coorte de 24 meses, conforme definido no RF4.
   *
   * @return {DataDefinition}
   */
  public DataDefinition getPatientsWithTbThirdToNineMonth(
      int minnumberYears, int maxnumberYearsYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(
        "B8-Teve TB nos 1˚s 12 meses de TARV: (coluna Q) - Resposta = Sim ou Não (RF23)");
    sqlPatientDataDefinition.addParameter(
        new Parameter("evaluationYear", "evaluationYear", Integer.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("23761", hivMetadata.getActiveTBConcept().getConceptId());
    map.put("23758", hivMetadata.getTBSymptomsConcept().getConceptId());
    map.put("1766", tbMetadata.getObservationTB().getConceptId());
    map.put("1763", tbMetadata.getFeverLastingMoraThan3Weeks().getConceptId());
    map.put("1764", tbMetadata.getWeightLossOfMoreThan3KgInLastMonth().getConceptId());
    map.put("1762", tbMetadata.getNightsWeatsLastingMoraThan3Weeks().getConceptId());
    map.put("1760", tbMetadata.getCoughLastingMoraThan3Weeks().getConceptId());
    map.put("23760", tbMetadata.getAsthenia().getConceptId());
    map.put("1765", tbMetadata.getCohabitantBeingTreatedForTB().getConceptId());
    map.put("161", tbMetadata.getLymphadenopathy().getConceptId());

    String query =
        "SELECT final_query.patient_id, "
            + "       CASE "
            + "              WHEN final_query.encounter_date IS NULL THEN 'Não' "
            + "              WHEN final_query.encounter_date IS NOT NULL THEN 'Sim' "
            + "              ELSE '' "
            + "       end "
            + "FROM   ( "
            + "                  SELECT     p.patient_id, "
            + "                             e.encounter_datetime AS encounter_date "
            + "                  FROM       patient p "
            + "                  INNER JOIN encounter e "
            + "                  ON         e.patient_id = p.patient_id "
            + "                  INNER JOIN obs o "
            + "                  ON         o.encounter_id = e.encounter_id "
            + "                  INNER JOIN obs o2 "
            + "                  ON         o2.encounter_id = e.encounter_id "
            + "                  INNER JOIN obs o3 "
            + "                  ON         o3.encounter_id = e.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.art_start AS art_encounter "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 1)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 1)
            + " ) "
            + "                             ) art "
            + "                  ON         art.patient_id = p.patient_id "
            + "                  WHERE      p.voided = 0 "
            + "                  AND        o.voided = 0 "
            + "                  AND        o2.voided = 0 "
            + "                  AND        o3.voided = 0 "
            + "                  AND        e.encounter_type = ${6} "
            + "                  AND        e.location_id = :location "
            + "                  AND        e.encounter_datetime >= date_add( art.art_encounter, INTERVAL "
            + minnumberYears
            + " MONTH ) "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + maxnumberYearsYears
            + " MONTH ) "
            + "                  AND    (   ( o.concept_id = ${23758} "
            + "                               AND o.value_coded IN ( ${1065} ) ) "
            + "                  OR         ( o2.concept_id = ${1766} "
            + "                                 AND  o2.value_coded IN ( ${1763}, "
            + "                                                  ${1764}, ${1762}, "
            + "                                                  ${1760}, ${23760}, "
            + "                                                  ${1765}, ${161} ) ) "
            + "                  OR        ( o3.concept_id = ${23761} "
            + "                               AND o3.value_coded IN ( ${1065} ) ) ) "
            + "                  GROUP BY   p.patient_id "
            + "                  UNION "
            + "                  SELECT     p.patient_id, "
            + "                             e.encounter_datetime AS encounter_date "
            + "                  FROM       patient p "
            + "                  INNER JOIN encounter e "
            + "                  ON         e.patient_id = p.patient_id "
            + "                  INNER JOIN obs o "
            + "                  ON         o.encounter_id = e.encounter_id "
            + "                  INNER JOIN obs o2 "
            + "                  ON         o2.encounter_id = e.encounter_id "
            + "                  INNER JOIN obs o3 "
            + "                  ON         o3.encounter_id = e.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.art_start AS art_encounter "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 2)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 2)
            + " ) "
            + "                             ) art "
            + "                  ON         art.patient_id = p.patient_id "
            + "                  WHERE      p.voided = 0 "
            + "                  AND        o.voided = 0 "
            + "                  AND        o2.voided = 0 "
            + "                  AND        o3.voided = 0 "
            + "                  AND        e.encounter_type = ${6} "
            + "                  AND        e.location_id = :location "
            + "                  AND        e.encounter_datetime >= date_add( art.art_encounter, INTERVAL "
            + minnumberYears
            + " MONTH ) "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + maxnumberYearsYears
            + " MONTH ) "
            + "                  AND    (   ( o.concept_id = ${23758} "
            + "                               AND o.value_coded IN ( ${1065} ) ) "
            + "                  OR         ( o2.concept_id = ${1766} "
            + "                                 AND  o2.value_coded IN ( ${1763}, "
            + "                                                  ${1764}, ${1762}, "
            + "                                                  ${1760}, ${23760}, "
            + "                                                  ${1765}, ${161} ) ) "
            + "                  OR        ( o3.concept_id = ${23761} "
            + "                               AND o3.value_coded IN ( ${1065} ) ) ) "
            + "                  GROUP BY   p.patient_id ) AS final_query";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF24 - Data de inscrição no MDS - B.9 (Coluna R)</b><br>
   * <br>
   *
   * <p>O sistema irá determinar a “Data de inscrição no MDS” seleccionando a data de consulta com
   * registo de pelo menos um campo de “Modelos diferenciados de Cuidados - MDS” (MDS 1, MDS 2, MDS
   * 3, MDS 4 ou MDS 5) com resposta = “INICIO”, numa consulta clínica decorrida entre 3 a 9 meses
   * do Início TARV (Data da Consulta >= “Data Início TARV” + 3 meses e <= “Data Início TARV” + 9
   * meses). <br>
   * <br>
   *
   * <p>Nota 1: Nota 1: caso exista mais que uma consulta clínica com registo do início no MDS, o
   * sistema irá considerar o registo mais antigo<br>
   * <br>
   *
   * <p>Nota 2: caso o utente não satisfaça o critério definido, o sistema não irá preencher nenhuma
   * informação.<br>
   * <br>
   *
   * <p>Nota 3: A “Data Início TARV” é a data registada na Ficha Resumo (“Data do Início TARV”).
   * <br>
   * <br>
   *
   * <p>Nota 4: O utente a ser considerado nesta definição iniciou TARV ou na coorte de 12 meses ou
   * na coorte de 24 meses, conforme definido no RF4. <br>
   * <br>
   *
   * @return {DataDefinition}
   */
  public DataDefinition getMdsDate() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(
        "B9- Data de inscrição no MDS: (coluna R) - Resposta = Data de Inscrição (RF24)");
    sqlPatientDataDefinition.addParameter(
        new Parameter("evaluationYear", "evaluationYear", Integer.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());
    map.put("165322", hivMetadata.getMdcState().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());

    String query =
        "                  SELECT     p.patient_id, "
            + "                             MIN(e.encounter_datetime) AS encounter_date "
            + "                  FROM       patient p "
            + "                  INNER JOIN encounter e "
            + "                  ON         e.patient_id = p.patient_id "
            + "                  INNER JOIN obs otype "
            + "                  ON         otype.encounter_id = e.encounter_id "
            + "                  INNER JOIN obs ostate "
            + "                  ON         ostate.encounter_id = e.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.art_start AS art_encounter "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 1)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 1)
            + " ) "
            + "                             ) art "
            + "                  ON         art.patient_id = p.patient_id "
            + "                  WHERE      p.voided = 0 "
            + "                  AND        otype.voided = 0 "
            + "                  AND        ostate.voided = 0 "
            + "                  AND        e.encounter_type = ${6} "
            + "                  AND        e.location_id = :location "
            + "                  AND        e.encounter_datetime >= date_add( art.art_encounter, INTERVAL 3 MONTH) "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL 9 MONTH) "
            + "                  AND    (   ( otype.concept_id = ${165174} "
            + "                               AND otype.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate.concept_id = ${165322} "
            + "                                 AND  ostate.value_coded IN (${1256}) ) ) "
            + "                  AND  otype.obs_group_id = ostate.obs_group_id "
            + "                  GROUP BY   p.patient_id "
            + "                  UNION "
            + "                  SELECT     p.patient_id, "
            + "                             MIN(e.encounter_datetime) AS encounter_date "
            + "                  FROM       patient p "
            + "                  INNER JOIN encounter e "
            + "                  ON         e.patient_id = p.patient_id "
            + "                  INNER JOIN obs otype "
            + "                  ON         otype.encounter_id = e.encounter_id "
            + "                  INNER JOIN obs ostate "
            + "                  ON         ostate.encounter_id = e.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.art_start AS art_encounter "
            + "                           FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientsInitiatedART12Or24Months(
                inclusionStartMonthAndDay, inclusionEndMonthAndDay, 2)
            + "                           ) art_patient "
            + " WHERE  art_patient.patient_id  "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getTranferredPatients(
                inclusionEndMonthAndDay, 2)
            + " ) "
            + "                             ) art "
            + "                  ON         art.patient_id = p.patient_id "
            + "                  WHERE      p.voided = 0 "
            + "                  AND        otype.voided = 0 "
            + "                  AND        ostate.voided = 0 "
            + "                  AND        e.encounter_type = ${6} "
            + "                  AND        e.location_id = :location "
            + "                  AND        e.encounter_datetime >= date_add( art.art_encounter, INTERVAL 3 MONTH) "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL 9 MONTH) "
            + "                  AND    (   ( otype.concept_id = ${165174} "
            + "                               AND otype.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate.concept_id = ${165322} "
            + "                                 AND  ostate.value_coded IN (${1256}) ) ) "
            + "                  AND  otype.obs_group_id = ostate.obs_group_id "
            + "                  GROUP BY   p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }
}
