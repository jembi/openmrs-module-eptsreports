package org.openmrs.module.eptsreports.reporting.library.cohorts.cd4request;

import static org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils.map;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ResumoMensalCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.queries.cd4request.ListOfPatientsEligibleForCd4RequestQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsQueriesUtil;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsEligibleForCd4RequestCohortQueries {

  private final HivMetadata hivMetadata;
  private final ResumoMensalCohortQueries resumoMensalCohortQueries;

  String MAPPING = "startDate=${startDate},endDate=${endDate},location=${location}";
  String MAPPING2 =
      "startDate=${startDate},endDate=${endDate},generationDate=${generationDate},location=${location}";
  String MAPPING3 =
      "startDate=${startDate},endDate=${generationDate},generationDate=${generationDate},location=${location}";
  String MAPPING4 = "startDate=${startDate},generationDate=${generationDate},,location=${location}";

  @Autowired
  public ListOfPatientsEligibleForCd4RequestCohortQueries(
      HivMetadata hivMetadata, ResumoMensalCohortQueries resumoMensalCohortQueries) {
    this.hivMetadata = hivMetadata;
    this.resumoMensalCohortQueries = resumoMensalCohortQueries;
  }

  /**
   * <b>Relatório – Lista de Utentes com critérios de elegibilidade para pedido de CD4 durante o
   * período</b>
   *
   * <p>Iniciaram TARV durante o período de reporte (CD4_RF3); ou
   *
   * <p>Reiniciaram TARV durante o período de reporte (CD4_RF4); ou
   *
   * <p>Receberam dois resultados de CV alta (CD4_RF5); ou
   *
   * @see #getPatientWhoInitiatedTarvDuringPeriodC1() Iniciaram TARV
   * @see #getPatientWhoRestartedTarvAndEligibleForCd4RequestC2() Reiniciaram TARV
   * @see #getPatientsWithTwoHighVlResultsC3() Receberam dois resultados de CV alta
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsEligibleForCd4RequestComposition() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName(
        "Relatório – Lista de Utentes com critérios de elegibilidade para pedido de CD4 durante o períodoo");
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("generationDate", "generationDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition started = getPatientWhoInitiatedTarvDuringPeriodC1();
    CohortDefinition restarted = getPatientWhoRestartedTarvAndEligibleForCd4RequestC2();
    CohortDefinition receivedHighVl = getPatientsWithTwoHighVlResultsC3();

    compositionCohortDefinition.addSearch("STARTED", map(started, MAPPING2));
    compositionCohortDefinition.addSearch("RESTARTED", map(restarted, MAPPING2));
    compositionCohortDefinition.addSearch("HIGHVL", map(receivedHighVl, MAPPING4));

    compositionCohortDefinition.setCompositionString(" STARTED OR RESTARTED OR HIGHVL");

    return compositionCohortDefinition;
  }

  /**
   * <b>C1 - Utentes que iniciaram TARV durante o período</b>
   *
   * <p>incluindo todos os utentes do indicador B1 do relatório “Resumo Mensal de HIV/SIDA” (Nº de
   * utentes que iniciaram TARV durante o mês) para o período do relatório correspondente a “Data
   * Início” e “Data Fim”.
   *
   * <p>excluindo todos os utentes que tiveram registo de resultado de CD4 numa consulta clínica
   * (Ficha Clínica –Ficha Mestra) até a data geração do relatório; Nota 1: a definição do indicador
   * B1 (Nº de utentes que iniciaram TARV durante o mês) encontra-se detalhada no documento de
   * requisitos do relatório “Resumo Mensal de HIV/SIDA”.
   *
   * @see ResumoMensalCohortQueries#getPatientsWhoInitiatedTarvAtThisFacilityDuringCurrentMonthB1()
   *     Resumo Mensal B1
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientWhoInitiatedTarvDuringPeriodC1() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName("C1 - Utentes que iniciaram TARV durante o período");
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("generationDate", "generationDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition rmB1 =
        resumoMensalCohortQueries.getPatientsWhoInitiatedTarvAtThisFacilityDuringCurrentMonthB1();

    CohortDefinition cd4ResultByReportGenerationDate =
        getPatientsWithCd4ResultsOnRestartedTarvDate(false);

    compositionCohortDefinition.addSearch("B1", map(rmB1, MAPPING));

    compositionCohortDefinition.addSearch(
        "CD4RESULT", map(cd4ResultByReportGenerationDate, MAPPING2));

    compositionCohortDefinition.setCompositionString("B1 AND NOT CD4RESULT");

    return compositionCohortDefinition;
  }

  /**
   * <b>C2 - Utentes reinício TARV elegíveis ao CD4</b>
   *
   * <p>Incluindo todos os utentes que tiveram registo de “Mudança de Estado de Permanência” =
   * “Reinício” numa consulta clínica (Ficha Clínica – Ficha Mestra) ocorrida durante o período de
   * reporte (“Data Consulta Reinício” >= “Data Início” e <= “Data Fim”). Nota: no caso de existirem
   * mais de uma consulta com registo de Reinício durante o período de reporte, o sistema irá
   * considerar a primeira ocorrência como “Data Consulta Reinício”
   *
   * <p>Incluindo todos os utentes que tiveram registo de “Mudança de Estado de Permanência” =
   * “Reinício” na Ficha Resumo- Ficha Mestra ocorrida durante o período de reporte (“Data Mudança
   * de Estado Reinício” >= “Data Início” e <= “Data Fim”). Nota: no caso de existirem mais de uma
   * data com registo de Reinício durante o período de reporte, o sistema irá considerar a primeira
   * ocorrência como “Data Mudança de Estado Reinício”.
   *
   * <p>Excluindo todos os utentes que tiveram registo de resultado de CD4 numa consulta clínica
   * (Ficha Clínica – Ficha Mestra) durante o período compreendido entre “Data Início” e a data
   * geração do relatório.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientWhoRestartedTarvAndEligibleForCd4RequestC2() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName("C2 - Utentes reinício TARV elegíveis ao CD4");
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("generationDate", "generationDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition restarted = getPatientsWithRestartedStateOfStay();

    CohortDefinition cd4ResultByReportGenerationDate =
        getPatientsWithCd4ResultsOnRestartedTarvDate(true);

    compositionCohortDefinition.addSearch("RESTARTED", map(restarted, MAPPING));

    compositionCohortDefinition.addSearch(
        "CD4RESULT", map(cd4ResultByReportGenerationDate, MAPPING3));

    compositionCohortDefinition.setCompositionString("RESTARTED AND NOT CD4RESULT");

    return compositionCohortDefinition;
  }

  /**
   * <b>C3 - Utentes com 2 CV Altas</b>
   *
   * <p>incluindo todos os utentes com registo do último “Resultado de CV” numa consulta clínica
   * (Ficha Clínica – Ficha Mestra) ocorrida 6 meses antes do período de reporte, cujo resultado é >
   * 1000cps/ml (“Data Último Resultado CV” <= “Data Fim” – 6 meses e “Último Resultado CV”
   * >1000cps/ml);
   *
   * <p>filtrando as utentes que tiveram registo do penúltimo “Resultado de CV” numa consulta
   * clínica (Ficha Clínica – Ficha Mestra) ocorrida 6 meses antes do período de reporte, e cujo
   * resultado também é >1000 cps/ml (“Data Penúltimo Resultado CV” <= “Data Fim” – 6 meses e
   * “Penúltimo Resultado CV” >1000cps/ml);
   *
   * <p>exluindo as utentes que tiveram registo do resultado do CD4 numa consulta clínica (Ficha
   * Clínica – Ficha Mestra) ocorrida entre “Data Último Resultado CV” e a data geração do
   * relatório.
   *
   * @see #getPatientsWithVlResultGreaterThan1000Copies() Ultimo Resultado VL > 1000
   * @see #getPatientsWithSecondVlResultGreaterThan1000Copies() Penultimo Resultado VL > 1000
   * @see #getPatientsWithCd4ResultsOnLastVlDate() Ultimo Resultado CD4 apos Ultimo Resultado VL
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithTwoHighVlResultsC3() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName("C3 - Utentes com 2 CV Altas");
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("generationDate", "generationDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition lastVlResult = getPatientsWithVlResultGreaterThan1000Copies();
    CohortDefinition secondVlResult = getPatientsWithSecondVlResultGreaterThan1000Copies();
    CohortDefinition lastCd4ResultAfterLastVl = getPatientsWithCd4ResultsOnLastVlDate();

    compositionCohortDefinition.addSearch("LASTVL", Mapped.mapStraightThrough(lastVlResult));
    compositionCohortDefinition.addSearch("SECONDVL", Mapped.mapStraightThrough(secondVlResult));
    compositionCohortDefinition.addSearch(
        "LASTCD4", Mapped.mapStraightThrough(lastCd4ResultAfterLastVl));

    compositionCohortDefinition.setCompositionString("(LASTVL AND SECONDVL) AND NOT LASTCD4");

    return compositionCohortDefinition;
  }

  /**
   * <b>todos os utentes que tiveram registo de resultado de CD4 numa consulta clínica (Ficha
   * Clínica – Ficha Mestra) até a data (geração do relatório / período do relatório ) </b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithCd4ResultsOnRestartedTarvDate(Boolean duringPeriod) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "todos os utentes que tiveram registo de resultado de CD4 numa consulta"
            + " clínica (Ficha Clínica –Ficha Mestra) até a data geração do relatório");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("generationDate", "generationDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("730", hivMetadata.getCD4PercentConcept().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("1705", hivMetadata.getRestartConcept().getConceptId());

    String query =
        "SELECT pa.patient_id "
            + "FROM "
            + "    patient pa "
            + "        INNER JOIN encounter enc "
            + "                   ON enc.patient_id =  pa.patient_id "
            + "        INNER JOIN obs "
            + "                   ON obs.encounter_id = enc.encounter_id "
            + "WHERE  pa.voided = 0 "
            + "  AND enc.voided = 0 "
            + "  AND obs.voided = 0 "
            + "  AND enc.encounter_type = ${6} "
            + "  AND ( "
            + "        (obs.concept_id = ${1695} AND obs.value_numeric IS NOT NULL) "
            + "        OR "
            + "        (obs.concept_id = ${730} AND obs.value_numeric IS NOT NULL) "
            + "      ) "
                .concat(
                    duringPeriod
                        ? "       AND e.encounter_datetime >= :startDate "
                            + "       AND e.encounter_datetime <= :endDate "
                        : "  AND enc.encounter_datetime <= :generationDate ")
            + "  AND enc.location_id = :location "
            + "GROUP BY pa.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * Incluindo todos os utentes que tiveram registo de “Mudança de Estado de Permanência” =
   * “Reinício” numa consulta clínica (Ficha Clínica – Ficha Mestra) ocorrida durante o período de
   * reporte (“Data Consulta Reinício” >= “Data Início” e <= “Data Fim”). Nota: no caso de existirem
   * mais de uma consulta com registo de Reinício durante o período de reporte, o sistema irá
   * considerar a primeira ocorrência como “Data Consulta Reinício”. ou
   *
   * <p>Incluindo todos os utentes que tiveram registo de “Mudança de Estado de Permanência” =
   * “Reinício” na Ficha Resumo- Ficha Mestra ocorrida durante o período de reporte (“Data Mudança
   * de Estado Reinício” >= “Data Início” e <= “Data Fim”). Nota: no caso de existirem mais de uma
   * data com registo de Reinício durante o período de reporte, o sistema irá considerar a primeira
   * ocorrência como “Data Mudança de Estado Reinício”.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithRestartedStateOfStay() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1705", hivMetadata.getRestartConcept().getConceptId());

    String query =
        " SELECT p.patient_id "
            + "FROM   patient p "
            + "           INNER JOIN encounter e "
            + "                      ON e.patient_id = p.patient_id "
            + "           INNER JOIN obs o "
            + "                      ON o.encounter_id = e.encounter_id "
            + "INNER JOIN ( "
            + "             SELECT p.patient_id, "
            + "                    MIN(e.encounter_datetime) AS first_date "
            + "             FROM   patient p "
            + "                        INNER JOIN encounter e "
            + "                                   ON e.patient_id = p.patient_id "
            + "                        INNER JOIN obs o "
            + "                                   ON o.encounter_id = e.encounter_id "
            + "             WHERE "
            + "                 p.voided = 0 "
            + "               AND e.voided = 0 "
            + "               AND o.voided = 0 "
            + "               AND e.encounter_type = ${6} "
            + "               AND e.location_id = :location "
            + "               AND o.concept_id = ${6273} "
            + "               AND o.value_coded IS NOT NULL "
            + "               AND e.encounter_datetime >= :startDate "
            + "               AND e.encounter_datetime <= :endDate "
            + "             GROUP  BY p.patient_id "
            + ") first_state ON first_state.patient_id = p.patient_id "
            + "WHERE   p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND e.encounter_type = ${6} "
            + "  AND e.location_id = :location "
            + "  AND o.concept_id = ${6273} "
            + "  AND o.value_coded = ${1705} "
            + "  AND e.encounter_datetime = first_state.first_date "
            + "GROUP  BY p.patient_id "
            + "UNION "
            + "SELECT p.patient_id "
            + "FROM   patient p "
            + "           INNER JOIN encounter e "
            + "                      ON e.patient_id = p.patient_id "
            + "           INNER JOIN obs o "
            + "                      ON o.encounter_id = e.encounter_id "
            + "           INNER JOIN ( "
            + "             SELECT p.patient_id, "
            + "                    MIN(o.obs_datetime) AS first_date "
            + "             FROM   patient p "
            + "                        INNER JOIN encounter e "
            + "                                   ON e.patient_id = p.patient_id "
            + "                        INNER JOIN obs o "
            + "                                   ON o.encounter_id = e.encounter_id "
            + "             WHERE "
            + "                 p.voided = 0 "
            + "               AND e.voided = 0 "
            + "               AND o.voided = 0 "
            + "               AND e.encounter_type = ${53} "
            + "               AND e.location_id = :location "
            + "               AND o.concept_id = ${6272} "
            + "               AND o.value_coded IS NOT NULL "
            + "               AND e.encounter_datetime >= :startDate "
            + "               AND e.encounter_datetime <= :endDate "
            + "             GROUP  BY p.patient_id "
            + ") first_state ON first_state.patient_id = p.patient_id "
            + "WHERE   p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND e.encounter_type = ${53} "
            + "  AND e.location_id = :location "
            + "  AND o.concept_id = ${6272} "
            + "  AND o.value_coded = ${1705} "
            + "  AND o.obs_datetime = first_state.first_date "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * incluindo todos os utentes com registo do último “Resultado de CV” numa consulta clínica (Ficha
   * Clínica – Ficha Mestra) ocorrida 6 meses antes do período de reporte, cujo resultado é >
   * 1000cps/ml (“Data Último Resultado CV” <= “Data Fim” – 6 meses e “Último Resultado CV”
   * >1000cps/ml);
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithVlResultGreaterThan1000Copies() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("utentes com registo do último “Resultado de CV” > 1000cps/ml");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String lastVlQuery = ListOfPatientsEligibleForCd4RequestQueries.getLastVlResult();
    String query = new EptsQueriesUtil().patientIdQueryBuilder(lastVlQuery).getQuery();

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * utentes que tiveram registo do penúltimo “Resultado de CV” numa consulta clínica (Ficha Clínica
   * – Ficha Mestra) ocorrida 6 meses antes do período de reporte, e cujo resultado também é >1000
   * cps/ml (“Data Penúltimo Resultado CV” <= “Data Fim” – 6 meses e “Penúltimo Resultado CV”
   * >1000cps/ml)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithSecondVlResultGreaterThan1000Copies() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("utentes com registo do penúltimo “Resultado de CV” > 1000cps/ml");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String secondVlQuery = ListOfPatientsEligibleForCd4RequestQueries.getSecondVlResult();
    String query = new EptsQueriesUtil().patientIdQueryBuilder(secondVlQuery).getQuery();

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b> utentes que tiveram registo do resultado do CD4 numa consulta clínica (Ficha Clínica –
   * Ficha Mestra) ocorrida entre “Data Último Resultado CV” e a data geração do relatório </b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithCd4ResultsOnLastVlDate() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("resultado do CD4 apos Data Último Resultado CV");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("generationDate", "generationDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("730", hivMetadata.getCD4PercentConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String query =
        "SELECT pa.patient_id "
            + "FROM "
            + "    patient pa "
            + "        INNER JOIN encounter enc "
            + "                   ON enc.patient_id =  pa.patient_id "
            + "        INNER JOIN obs "
            + "                   ON obs.encounter_id = enc.encounter_id "
            + " INNER JOIN ( "
            + ListOfPatientsEligibleForCd4RequestQueries.getLastVlResultDate()
            + " ) last_vl ON last_vl.patient_id = p.patient_id "
            + "WHERE  pa.voided = 0 "
            + "  AND enc.voided = 0 "
            + "  AND obs.voided = 0 "
            + "  AND enc.encounter_type = ${6} "
            + "  AND ( "
            + "        (obs.concept_id = ${1695} AND obs.value_numeric IS NOT NULL) "
            + "        OR "
            + "        (obs.concept_id = ${730} AND obs.value_numeric IS NOT NULL) "
            + "      ) "
            + "       AND e.encounter_datetime >= last_vl.most_recent "
            + "  AND enc.encounter_datetime <= :generationDate "
            + "  AND enc.location_id = :location "
            + "GROUP BY pa.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }
}
