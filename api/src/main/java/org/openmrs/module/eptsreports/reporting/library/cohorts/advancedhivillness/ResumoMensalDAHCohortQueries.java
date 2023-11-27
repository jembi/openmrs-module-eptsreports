package org.openmrs.module.eptsreports.reporting.library.cohorts.advancedhivillness;

import static org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils.map;

import java.util.*;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ResumoMensalCohortQueries;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResumoMensalDAHCohortQueries {

  private final ListOfPatientsInAdvancedHivIllnessCohortQueries
      listOfPatientsInAdvancedHivIllnessCohortQueries;
  private final HivMetadata hivMetadata;

  private final ResumoMensalCohortQueries resumoMensalCohortQueries;

  @Autowired
  public ResumoMensalDAHCohortQueries(
      ListOfPatientsInAdvancedHivIllnessCohortQueries
          listOfPatientsInAdvancedHivIllnessCohortQueries,
      HivMetadata hivMetadata,
      ResumoMensalCohortQueries resumoMensalCohortQueries) {
    this.listOfPatientsInAdvancedHivIllnessCohortQueries =
        listOfPatientsInAdvancedHivIllnessCohortQueries;
    this.hivMetadata = hivMetadata;
    this.resumoMensalCohortQueries = resumoMensalCohortQueries;
  }

  /**
   * <b>Relatório- Indicador 0 Utentes em DAH até o fim do mês anterior</b>
   * <li>Com registo de “Data de Início no Modelo de DAH”, na Ficha de DAH, ocorrida até fim do mês
   *     anterior [“Data de Início no Modelo de DAH” <= “Data Início” menos (-) 1 dia].
   *
   *     <p>Excluindo todos os utentes
   * <li>Com registo de pelo menos um motivo (Óbito/ Abandono/ Transferido Para) e “Data de Saída de
   *     TARV na US” (secção J), na Ficha de DAH, ocorrida após a data mais recente da “Data de
   *     Início no Modelo de DAH” e até o fim do mês anterior [“Data de Saída de TARV na US” >=
   *     “Última Data de Início no Modelo de DAH” e <= “Data Início” menos (-) 1 dia] ou
   * <li>Com registo de “Data de Saída” (secção I), registada na Ficha de DAH e ocorrida após a data
   *     mais recente da “Data de Início no Modelo de DAH” e até o fim do mês anterior [“Data de
   *     Saída de TARV na US” >= “Última Data de Início no Modelo de DAH” e <= “Data Início” menos
   *     (-) 1 dia]
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoStartedFollowupOnDAHComposition() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Número total de activos em DAH em TARV,  até ao fim do mês anterior");
    cd.addParameters(getCohortParameters());

    cd.addSearch(
        "onDAH",
        map(
            listOfPatientsInAdvancedHivIllnessCohortQueries.getPatientsWhoStartedFollowupOnDAH(
                false),
            "startDate=${startDate-1d},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "leftTreatment",
        map(
            getPatientsWhoLeftFollowupOnDAHByTheEndOfPreviousMonth(),
            "startDate=${startDate-1d},location=${location}"));

    cd.setCompositionString("onDAH AND NOT leftTreatment");
    return cd;
  }

  /**
   * <b>Relatórios-Indicador 1 - Inícios TARV e Início DAH</b>
   * <li>Com registo de “Data de Início no Modelo de DAH”, na Ficha de DAH, ocorrida durante o
   *     período (“Data de Início no Modelo de DAH”>= “Data Início” e <= “Data Fim”)
   *
   *     <p>Filtrando todos os utentes
   * <li>Registados como “Novo Início" no campo “Situação do TARV no início do seguimento” (Secção
   *     A) da Ficha de DAH que tem o registo de “Data de Início no Modelo de DAH” ocorrida durante
   *     o período (“Data de Início no Modelo de DAH”>= “Data Início” e <= “Data Fim”)
   * <li>Caso não exista o registo da “Situação do TARV no Início do Seguimento” na Ficha de DAH que
   *     tem o registo de “Data de Início no Modelo de DAH” ocorrida durante o período (“Data de
   *     Início no Modelo de DAH”>= “Data Início” e <= “Data Fim”), considerar os utentes incluídos
   *     no indicador B1-Nº de utentes que iniciaram TARV nesta unidade sanitária durante o mês, do
   *     relatório “Resumo Mensal de HIV/SIDA” durante o período de compreendido entre “Data Início”
   *     menos (–) 2 meses e “Data Fim”.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoAreNewInArtAndStartedFollowupDuringTheMonthComposition() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Número total de activos em DAH em TARV,  até ao fim do mês anterior");
    cd.addParameters(getCohortParameters());

    cd.addSearch(
        "onDAHDuringPeriod",
        map(
            listOfPatientsInAdvancedHivIllnessCohortQueries.getPatientsWhoStartedFollowupOnDAH(
                true),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "newOnArt",
        map(
            getPatientsArtSituationOnDAH(hivMetadata.getStartDrugs()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "B1",
        map(
            resumoMensalCohortQueries
                .getPatientsWhoInitiatedTarvAtThisFacilityDuringCurrentMonthB1(),
            "startDate=${startDate-2m},endDate=${endDate},location=${location}"));

    cd.setCompositionString("onDAHDuringPeriod AND (newOnArt OR B1)");
    return cd;
  }

  /**
   * <b>Relatório-Indicador 2 - Reinícios TARV e Início DAH</b>
   * <li>Com registo de “Data de Início no Modelo de DAH”, na Ficha de DAH, ocorrida durante o
   *     período (“Data de Início no Modelo de DAH”>= “Data Início” e <= “Data Fim”)
   *
   *     <p>Filtrando todos os utentes
   * <li>Registados como “Reinício" no campo “Situação do TARV no início do seguimento” (Secção A)
   *     da Ficha de DAH que tem o registo de “Data de Início no Modelo de DAH” ocorrida durante o
   *     período (“Data de Início no Modelo de DAH”>= “Data Início” e <= “Data Fim”)
   * <li>Caso não exista o registo da “Situação do TARV no Início do Seguimento” na Ficha de DAH que
   *     tem o registo de “Data de Início no Modelo de DAH” ocorrida durante o período (“Data de
   *     Início no Modelo de DAH”>= “Data Início” e <= “Data Fim”), considerar os utentes incluídos
   *     no indicador B3-Nº de reinícios TARV durante o mês, do relatório “Resumo Mensal de
   *     HIV/SIDA” durante o período compreendido entre “Data Início” menos (–) 2 meses e “Data Fim”
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoRestartedArtAndStartedFollowupDuringTheMonthComposition() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Relatório-Indicador 2 - Reinícios TARV e Início DAH");
    cd.addParameters(getCohortParameters());

    cd.addSearch(
        "onDAHDuringPeriod",
        map(
            listOfPatientsInAdvancedHivIllnessCohortQueries.getPatientsWhoStartedFollowupOnDAH(
                true),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "restartedArt",
        map(
            getPatientsArtSituationOnDAH(hivMetadata.getRestartConcept()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "B3",
        map(
            resumoMensalCohortQueries.getPatientsRestartedTarvtB3(),
            "startDate=${startDate-2m},endDate=${endDate},location=${location}"));

    cd.setCompositionString("onDAHDuringPeriod AND (restartedArt OR B3)");
    return cd;
  }

  /**
   * <b>Relatório-Indicador 3 – Activos em TARV e Início DAH</b>
   * <li>Com registo de “Data de Início no Modelo de DAH”, na Ficha de DAH, ocorrida durante o
   *     período (“Data de Início no Modelo de DAH”>= “Data Início” e <= “Data Fim”)
   *
   *     <p>Filtrando todos os utentes
   * <li>Registados como “Em TARV" no campo “Situação do TARV no início do seguimento” (Secção A) da
   *     Ficha de DAH que tem o registo de “Data de Início no Modelo de DAH” ocorrida durante o
   *     período (“Data de Início no Modelo de DAH”>= “Data Início” e <= “Data Fim”)
   * <li>Caso não exista o registo da “Situação do TARV no Início do Seguimento” na Ficha de DAH que
   *     tem o registo de “Data de Início no Modelo de DAH” ocorrida durante o período (“Data de
   *     Início no Modelo de DAH”>= “Data Início” e <= “Data Fim”), considerar os utentes incluídos
   *     no indicador B12 - Nº de utentes activos em TARV até o fim do mês anterior, do relatório
   *     “Resumo Mensal de HIV/SIDA”
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoAreInTarvAndStartedFollowupDuringTheMonthComposition() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Relatório-Indicador 3 – Activos em TARV e Início DAH");
    cd.addParameters(getCohortParameters());

    cd.addSearch(
        "onDAHDuringPeriod",
        map(
            listOfPatientsInAdvancedHivIllnessCohortQueries.getPatientsWhoStartedFollowupOnDAH(
                true),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "onArt",
        map(
            getPatientsArtSituationOnDAH(hivMetadata.getArtStatus()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "B12",
        map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfPreviousMonthB12(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("onDAHDuringPeriod AND (onArt OR B12)");
    return cd;
  }

  /**
   *
   * <li>Com registo de pelo menos um motivo (Óbito/ Abandono/ Transferido Para) e “Data de Saída de
   *     TARV na US” (secção J), na Ficha de DAH, ocorrida após a data mais recente da “Data de
   *     Início no Modelo de DAH” e até o fim do mês anterior [“Data de Saída de TARV na US” >=
   *     “Última Data de Início no Modelo de DAH” e <= “Data Início” menos (-) 1 dia] ou
   * <li>Com registo de “Data de Saída” (secção I), registada na Ficha de DAH e ocorrida após a data
   *     mais recente da “Data de Início no Modelo de DAH” e até o fim do mês anterior [“Data de
   *     Saída de TARV na US” >= “Última Data de Início no Modelo de DAH” e <= “Data Início” menos
   *     (-) 1 dia]
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoLeftFollowupOnDAHByTheEndOfPreviousMonth() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        " utentes que saíram do seguimento para DAH até o fim do mês anterior");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("90", hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId());
    map.put("1366", hivMetadata.getPatientHasDiedConcept().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1707", hivMetadata.getAbandonedConcept().getConceptId());
    map.put("1708", hivMetadata.getExitFromArvTreatmentConcept().getConceptId());
    map.put("165386", hivMetadata.getExitDateFromArvTreatmentConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM "
            + "    patient p INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "              INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "              INNER JOIN ( "
            + "        SELECT p.patient_id, MAX(e.encounter_datetime) last_date "
            + "        FROM "
            + "            patient p INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                      INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "        WHERE p.voided = 0 AND e.voided = 0 AND o.voided = 0 "
            + "          AND e.encounter_type = ${90} "
            + "          AND e.encounter_datetime <= :startDate "
            + "          AND e.location_id = :location "
            + "        GROUP BY p.patient_id "
            + "    ) last_dah ON last_dah.patient_id = p.patient_id "
            + "WHERE p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND e.encounter_type = ${90} "
            + "  AND ( "
            + "        (o.concept_id = ${1708} "
            + "            AND o.value_coded IN (${1366},${1706},${1707}) "
            + "            AND o.obs_datetime >= last_dah.last_date) "
            + "        OR  (o.concept_id = ${165386} "
            + "        AND o.value_datetime >= last_dah.last_date "
            + "        AND o.value_datetime <= :startDate) "
            + "    ) "
            + "  AND e.location_id = :location "
            + "GROUP BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(substitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   *
   * <li>Registados como “Novo Início" no campo “Situação do TARV no início do seguimento” (Secção
   *     A) da Ficha de DAH que tem o registo de “Data de Início no Modelo de DAH” ocorrida durante
   *     o período (“Data de Início no Modelo de DAH”>= “Data Início” e <= “Data Fim”)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsArtSituationOnDAH(Concept artSituaationConcept) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "Número de utentes Registados como “Novo Início no campo “Situação do TARV no início do seguimento” (Secção A) da Ficha de DAH que tem o registo de “Data de Início no Modelo de DAH”");
    sqlCohortDefinition.addParameters(getCohortParameters());

    Map<String, Integer> map = new HashMap<>();
    map.put("90", hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId());
    map.put("1255", hivMetadata.getARVPlanConcept().getConceptId());
    map.put("artSituationConcept", artSituaationConcept.getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM "
            + "    patient p INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "              INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "              INNER JOIN ( "
            + "                        SELECT p.patient_id, MAX(e.encounter_datetime) as last_date "
            + "                         FROM "
            + "                             patient p INNER JOIN encounter e ON p.patient_id= e.patient_id "
            + "                                       INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "                         WHERE p.voided = 0 AND e.voided = 0 AND o.voided = 0 "
            + "                           AND e.encounter_type = ${90} "
            + "                           AND e.encounter_datetime >= :startDate "
            + "                           AND e.encounter_datetime <= :endDate "
            + "                           AND e.location_id = :location "
            + "                         GROUP BY p.patient_id "
            + "    ) last_dah ON last_dah.patient_id = p.patient_id "
            + "WHERE p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND e.encounter_type = ${90} "
            + "  AND o.concept_id = ${1255} "
            + "  AND o.value_coded = ${artSituationConcept} "
            + "  AND e.encounter_datetime = last_dah.last_date "
            + "  AND e.location_id = :location "
            + "GROUP BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(substitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>Relatório – Indicador 4 – Saídas do seguimento de DAH</b>
   * <li>Com registo de pelo menos um motivo (Óbito/ Abandono/ Transferido Para) e “Data de Saída de
   *     TARV na US” (secção J), na Ficha de DAH, ocorrida durante o período (“Data de Saída de TARV
   *     na US” >= “Data Início” e <= “Data Fim”) ou
   * <li>Com registo de “Data de Saída” (secção I), registada na Ficha de DAH e ocorrida durante o
   *     período (>= “Data Início” e <= “Data Fim”)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoLeftFollowupOnDAHByDuringMonth() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Indicador 4 – Saídas do seguimento de DAH");
    sqlCohortDefinition.addParameters(getCohortParameters());

    Map<String, Integer> map = new HashMap<>();
    map.put("90", hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId());
    map.put("1366", hivMetadata.getPatientHasDiedConcept().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1707", hivMetadata.getAbandonedConcept().getConceptId());
    map.put("1708", hivMetadata.getExitFromArvTreatmentConcept().getConceptId());
    map.put("165386", hivMetadata.getExitDateFromArvTreatmentConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM "
            + "    patient p INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "              INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "WHERE p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND e.encounter_type = ${90} "
            + "  AND ( "
            + "        (o.concept_id = ${1708} "
            + "            AND o.value_coded IN (${1366},${1706},${1707}) "
            + "            AND o.obs_datetime >= :startDate "
            + "            AND o.obs_datetime <= :endDate) "
            + "        OR  (o.concept_id = ${165386} "
            + "        AND o.value_datetime >= :startDate "
            + "        AND o.value_datetime <= :endDate) "
            + "    ) "
            + "  AND e.location_id = :location "
            + "GROUP BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(substitutor.replace(query));

    return sqlCohortDefinition;
  }

  private List<Parameter> getCohortParameters() {
    return Arrays.asList(
        ReportingConstants.START_DATE_PARAMETER,
        ReportingConstants.END_DATE_PARAMETER,
        ReportingConstants.LOCATION_PARAMETER);
  }
}
