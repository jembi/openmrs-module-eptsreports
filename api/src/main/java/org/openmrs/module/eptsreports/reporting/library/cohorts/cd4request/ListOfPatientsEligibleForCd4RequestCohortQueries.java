package org.openmrs.module.eptsreports.reporting.library.cohorts.cd4request;

import static org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils.map;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.eptsreports.reporting.library.cohorts.GenericCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ResumoMensalCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.advancedhivillness.ListOfPatientsInAdvancedHivIllnessCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.queries.advancedhivillness.ListOfPatientsOnAdvancedHivIllnessQueries;
import org.openmrs.module.eptsreports.reporting.library.queries.cd4request.ListOfPatientsEligibleForCd4RequestQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsQueriesUtil;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsEligibleForCd4RequestCohortQueries {

  private final HivMetadata hivMetadata;
  private final CommonMetadata commonMetadata;
  private final TbMetadata tbMetadata;
  private final ResumoMensalCohortQueries resumoMensalCohortQueries;
  private final ListOfPatientsInAdvancedHivIllnessCohortQueries
      listOfPatientsInAdvancedHivIllnessCohortQueries;
  private final ListOfPatientsOnAdvancedHivIllnessQueries listOfPatientsOnAdvancedHivIllnessQueries;

  private final GenericCohortQueries genericCohortQueries;

  String MAPPING = "startDate=${startDate},endDate=${endDate},location=${location}";
  String MAPPING2 =
      "startDate=${startDate},endDate=${endDate},generationDate=${generationDate},location=${location}";
  String MAPPING3 =
      "startDate=${startDate},endDate=${generationDate},generationDate=${generationDate},location=${location}";
  String MAPPING5 = "endDate=${endDate},generationDate=${generationDate},location=${location}";

  @Autowired
  public ListOfPatientsEligibleForCd4RequestCohortQueries(
      HivMetadata hivMetadata,
      CommonMetadata commonMetadata,
      TbMetadata tbMetadata,
      ResumoMensalCohortQueries resumoMensalCohortQueries,
      ListOfPatientsInAdvancedHivIllnessCohortQueries
          listOfPatientsInAdvancedHivIllnessCohortQueries,
      ListOfPatientsOnAdvancedHivIllnessQueries listOfPatientsOnAdvancedHivIllnessQueries,
      GenericCohortQueries genericCohortQueries) {
    this.hivMetadata = hivMetadata;
    this.commonMetadata = commonMetadata;
    this.tbMetadata = tbMetadata;
    this.resumoMensalCohortQueries = resumoMensalCohortQueries;
    this.listOfPatientsInAdvancedHivIllnessCohortQueries =
        listOfPatientsInAdvancedHivIllnessCohortQueries;
    this.listOfPatientsOnAdvancedHivIllnessQueries = listOfPatientsOnAdvancedHivIllnessQueries;
    this.genericCohortQueries = genericCohortQueries;
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
   * <p>Têm condição activa de estadiamento clínico III ou IV durante o período de reporte
   * (CD4_RF6); ou
   *
   * <p>São elegíveis ao pedido de CD4 de seguimento (CD4_RF7); ou
   *
   * <p>São mulheres grávidas e elegíveis ao pedido de CD4 (CD4_RF8). <br>
   * Excluindo todos os utentes que: <br>
   *
   * <p>Tenham sido transferidos para outra unidade sanitária até a data geração do relatório
   * (DAH_RF22);
   *
   * <p>Tenham registo de óbito até a data geração do relatório (DAH_RF23)
   *
   * @see #getPatientWhoInitiatedTarvDuringPeriodC1() Iniciaram TARV
   * @see #getPatientWhoRestartedTarvAndEligibleForCd4RequestC2() Reiniciaram TARV
   * @see #getPatientsWithTwoHighVlResultsC3() Receberam dois resultados de CV alta
   * @see #getPatientWithEstadiamentoIIIorIVC4() Condição activa de estadiamento clinico
   * @see #getPatientEligibleForCd4FollowupC5() Elegiveis ao pedido de CD4 Seguimento
   * @see #getPatientPregnantEligibleForCd4RequestC6() Elegiveis ao pedido de CD4 Seguimento
   * @see ResumoMensalCohortQueries#getTranferredOutPatients() Tenham sido transferidos para outra
   *     unidade sanitária
   * @see #getTransferredOutPatientsByGenerationDate() Tenham registo de óbito até a data geração do
   *     relatório
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
    CohortDefinition estadio = getPatientWithEstadiamentoIIIorIVC4();
    CohortDefinition eligibleForCd4Followup = getPatientEligibleForCd4FollowupC5();
    CohortDefinition pregnant = getPatientPregnantEligibleForCd4RequestC6();
    CohortDefinition transferredOut = resumoMensalCohortQueries.getTranferredOutPatients();
    CohortDefinition died = getTransferredOutPatientsByGenerationDate();

    compositionCohortDefinition.addSearch("STARTED", map(started, MAPPING2));
    compositionCohortDefinition.addSearch("RESTARTED", map(restarted, MAPPING2));
    compositionCohortDefinition.addSearch("HIGHVL", map(receivedHighVl, MAPPING5));
    compositionCohortDefinition.addSearch("ESTADIO", map(estadio, MAPPING2));
    compositionCohortDefinition.addSearch("ELIGIBLECD4", map(eligibleForCd4Followup, MAPPING5));
    compositionCohortDefinition.addSearch("PREGNANT", map(pregnant, MAPPING2));
    compositionCohortDefinition.addSearch(
        "TRANSFERREDOUT",
        map(
            transferredOut,
            "startDate=${startDate},endDate=${endDate},onOrBefore=${generationDate},location=${location}"));
    compositionCohortDefinition.addSearch(
        "DIED", map(died, "endDate=${generationDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "BASECOHORT",
        EptsReportUtils.map(
            genericCohortQueries.getBaseCohort(), "endDate=${endDate},location=${location}"));

    compositionCohortDefinition.setCompositionString(
        "((STARTED OR RESTARTED OR HIGHVL OR ESTADIO OR ELIGIBLECD4 OR PREGNANT) AND BASECOHORT) AND NOT (TRANSFERREDOUT OR DIED)");

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

    compositionCohortDefinition.addSearch(
        "LASTVL", map(lastVlResult, "endDate=${endDate-6m},location=${location}"));
    compositionCohortDefinition.addSearch(
        "SECONDVL", map(secondVlResult, "endDate=${endDate-6m},location=${location}"));
    compositionCohortDefinition.addSearch(
        "LASTCD4",
        map(
            lastCd4ResultAfterLastVl,
            "endDate=${endDate-6m},generationDate=${generationDate},location=${location}"));

    compositionCohortDefinition.setCompositionString("(LASTVL AND SECONDVL) AND NOT LASTCD4");

    return compositionCohortDefinition;
  }

  /**
   * <b>C4 - Utentes com critério de Estadiamento Clínico III ou IV</b>
   * <li>pelo menos um registo da lista representativa de Estadio IV (DAH_RF28), no campo de
   *     “Infecções Oportunistas incluindo Sarcoma de Kaposi e outras doenças”, na “Ficha Clínica –
   *     Ficha Mestra”, registada durante o período de avaliação (“Data Consulta” >= “Data Início” e
   *     “Data Consulta” <= “Data Fim”) ou
   * <li>pelo menos um registo da lista representativa de Estadio III (DAH_RF27), no campo de
   *     “Infecções Oportunistas incluindo Sarcoma de Kaposi e outras doenças”, na “Ficha Clínica –
   *     Ficha Mestra”, registada durante o período de avaliação (“Data Consulta” >= “Data Início” e
   *     “Data Consulta” <= “Data Fim”)
   *
   *     <p>excluindo todos os utentes que tiveram registo do resultado de CD4 numa consulta (Ficha
   *     Clínica – Ficha Mestra) ocorrida entre a “Data Consulta Estadio III_IV” e a data geração do
   *     relatório.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientWithEstadiamentoIIIorIVC4() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName(
        "C4 - Utentes com critério de Estadiamento Clínico III ou IV");
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("generationDate", "generationDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition estadio =
        listOfPatientsInAdvancedHivIllnessCohortQueries
            .getPatientsWithCriterioEstadiamentoInicioSeguimento();

    CohortDefinition cd4ResultByEstadioDate = getPatientsWithCd4ResultsOnEstadioDate();

    compositionCohortDefinition.addSearch("ESTADIO", map(estadio, MAPPING));

    compositionCohortDefinition.addSearch("CD4RESULT", map(cd4ResultByEstadioDate, MAPPING2));

    compositionCohortDefinition.setCompositionString("ESTADIO AND NOT CD4RESULT");

    return compositionCohortDefinition;
  }

  /**
   * <b>C5 - Utentes elegíveis ao CD4 Seguimento</b>
   *
   * <p>incluindo todos os utentes com registo do último resultado do CD4 registado numa consulta
   * clínica (Ficha Clínica – Ficha Mestra) que ocorreu 12 meses antes do fim do período de reporte,
   * e cujo resultado absoluto é < 350 ou resultado percentual é < 30% (“Data Último Resultado CD4”
   * <= “Data Início” – 12 meses e “Último Resultado CD4 Absoluto” <350 ou “Último Resultado CD4
   * Percentual” < 30);
   *
   * <p>excluindo os utentes que tiveram registo do resultado de CD4 numa consulta clínica (Ficha
   * Clínica – Ficha Mestra) ocorrida entre “Data Último Resultado CD4” + 1dia e a data geração do
   * relatório
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientEligibleForCd4FollowupC5() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName("C5 - Utentes elegíveis ao CD4 Seguimento");
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("generationDate", "generationDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition cd4LessThan350 = getPatientsWithCd4ResultsLessThan350();

    CohortDefinition cd4ResulstOnCd4Date = getPatientsWithCd4ResultsOnLastCd4Date();

    compositionCohortDefinition.addSearch(
        "CD4LESS350", map(cd4LessThan350, "endDate=${endDate-12m},location=${location}"));

    compositionCohortDefinition.addSearch(
        "CD4RESULT",
        map(
            cd4ResulstOnCd4Date,
            "endDate=${endDate-12m},generationDate=${generationDate},location=${location}"));

    compositionCohortDefinition.setCompositionString("CD4LESS350 AND NOT CD4RESULT");

    return compositionCohortDefinition;
  }

  /**
   * <b>C6 - Utentes mulheres grávidas elegíveis ao CD4</b>
   *
   * <p>incluindo todos os utentes do sexo feminino com registo de “Grávida” = “Sim” numa consulta
   * clínica (Ficha Clínica – Ficha Mestra) ocorrida nos últimos 9 meses do fim do período de
   * reporte (>= “Data Fim” - 9 meses e “Data Fim”). Nota: a “Data da Primeira Consulta Gravidez” é
   * a data da consulta na qual ocorreu o primeiro registo de “Grávida” = “Sim” nos últimos 9 meses
   * do fim do período de reporte.
   *
   * <p>excluindo as utentes que tiveram registo de resultado de CD4 numa consulta clínica (Ficha
   * Clínica – Ficha Mestra) ocorrida entre “Data de Primeira Consulta Gravidez” e a data geração do
   * relatório.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientPregnantEligibleForCd4RequestC6() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName("C6 - Utentes mulheres grávidas elegíveis ao CD4");
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("generationDate", "generationDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition pregnantOnLast9Months = getPatientsPregnantOnLast9Months();

    CohortDefinition cd4ResultsOnPregnancyDate = getPatientsWithCd4ResultsOnPregnancyDate();

    compositionCohortDefinition.addSearch(
        "PREGNANT9MONTHS",
        map(
            pregnantOnLast9Months,
            "startDate=${endDate-9m},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "CD4RESULT",
        map(
            cd4ResultsOnPregnancyDate,
            "startDate=${endDate-9m},endDate=${endDate},generationDate=${generationDate},location=${location}"));

    compositionCohortDefinition.setCompositionString("PREGNANT9MONTHS AND NOT CD4RESULT");

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
                        ? "       AND enc.encounter_datetime >= :startDate "
                            + "       AND enc.encounter_datetime <= :endDate "
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
            + " ) last_vl ON last_vl.patient_id = pa.patient_id "
            + "WHERE  pa.voided = 0 "
            + "  AND enc.voided = 0 "
            + "  AND obs.voided = 0 "
            + "  AND enc.encounter_type = ${6} "
            + "  AND ( "
            + "        (obs.concept_id = ${1695} AND obs.value_numeric IS NOT NULL) "
            + "        OR "
            + "        (obs.concept_id = ${730} AND obs.value_numeric IS NOT NULL) "
            + "      ) "
            + "       AND enc.encounter_datetime >= last_vl.most_recent "
            + "  AND enc.encounter_datetime <= :generationDate "
            + "  AND enc.location_id = :location "
            + "GROUP BY pa.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * todos os utentes que tiveram registo do resultado de CD4 numa consulta (Ficha Clínica – Ficha
   * Mestra) ocorrida entre a “Data Consulta Estadio III/IV” e a data geração do relatório
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithCd4ResultsOnEstadioDate() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("resultado do CD4 apos Data Último Resultado CV");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("generationDate", "generationDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

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
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("730", hivMetadata.getCD4PercentConcept().getConceptId());

    String query =
        "SELECT pa.patient_id "
            + "FROM "
            + "    patient pa "
            + "        INNER JOIN encounter enc "
            + "                   ON enc.patient_id =  pa.patient_id "
            + "        INNER JOIN obs "
            + "                   ON obs.encounter_id = enc.encounter_id "
            + " INNER JOIN ( "
            + ListOfPatientsEligibleForCd4RequestQueries.getEstadioOmsQuery()
            + " ) estadio ON estadio.patient_id = pa.patient_id "
            + "WHERE  pa.voided = 0 "
            + "  AND enc.voided = 0 "
            + "  AND obs.voided = 0 "
            + "  AND enc.encounter_type = ${6} "
            + "  AND ( "
            + "        (obs.concept_id = ${1695} AND obs.value_numeric IS NOT NULL) "
            + "        OR "
            + "        (obs.concept_id = ${730} AND obs.value_numeric IS NOT NULL) "
            + "      ) "
            + "       AND enc.encounter_datetime >= estadio.first_date "
            + "  AND enc.encounter_datetime <= :generationDate "
            + "  AND enc.location_id = :location "
            + "GROUP BY pa.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * incluindo todos os utentes com registo do último resultado do CD4 registado numa consulta
   * clínica (Ficha Clínica – Ficha Mestra) que ocorreu 12 meses antes do fim do período de reporte,
   * e cujo resultado absoluto é < 350 ou resultado percentual é < 30% (“Data Último Resultado CD4”
   * <= “Data Início” – 12 meses e “Último Resultado CD4 Absoluto” <350 ou “Último Resultado CD4
   * Percentual” < 30);
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithCd4ResultsLessThan350() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("resultado do CD4 apos Data Último Resultado CV");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("730", hivMetadata.getCD4PercentConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("165515", hivMetadata.getCD4SemiQuantitativeConcept().getConceptId());

    String query =
        "SELECT pa.patient_id "
            + "FROM "
            + "    patient pa "
            + "        INNER JOIN encounter enc "
            + "                   ON enc.patient_id =  pa.patient_id "
            + "        INNER JOIN obs "
            + "                   ON obs.encounter_id = enc.encounter_id "
            + " INNER JOIN ( "
            + ListOfPatientsEligibleForCd4RequestQueries.getLastCd4ResultDateQuery()
            + " ) cd4_date ON cd4_date.patient_id = pa.patient_id "
            + "WHERE  pa.voided = 0 "
            + "  AND enc.voided = 0 "
            + "  AND obs.voided = 0 "
            + "  AND enc.encounter_type = ${6} "
            + "  AND ( "
            + "        (obs.concept_id = ${1695} "
            + "             AND obs.value_numeric IS NOT NULL "
            + "             AND obs.value_numeric < 350) "
            + "        OR "
            + "        (obs.concept_id = ${730} "
            + "             AND obs.value_numeric IS NOT NULL "
            + "             AND obs.value_numeric < 30) "
            + "        OR "
            + "        (obs.concept_id = ${165515} "
            + "             AND obs.value_numeric IS NOT NULL "
            + "             AND obs.value_numeric < 200) "
            + "      ) "
            + "       AND enc.encounter_datetime = cd4_date.last_cd4 "
            + "  AND enc.location_id = :location "
            + "GROUP BY pa.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * utentes que tiveram registo do resultado de CD4 numa consulta clínica (Ficha Clínica – Ficha
   * Mestra) ocorrida entre “Data Último Resultado CD4” + 1dia e a data geração do relatório
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithCd4ResultsOnLastCd4Date() {
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
            + ListOfPatientsEligibleForCd4RequestQueries.getLastCd4ResultDateQuery()
            + " ) cd4_date ON cd4_date.patient_id = pa.patient_id "
            + "WHERE  pa.voided = 0 "
            + "  AND enc.voided = 0 "
            + "  AND obs.voided = 0 "
            + "  AND enc.encounter_type = ${6} "
            + "  AND ( "
            + "        (obs.concept_id = ${1695} AND obs.value_numeric IS NOT NULL) "
            + "        OR "
            + "        (obs.concept_id = ${730} AND obs.value_numeric IS NOT NULL) "
            + "      ) "
            + "       AND enc.encounter_datetime >= DATE_ADD(cd4_date.last_cd4, INTERVAL 1 DAY) "
            + "  AND enc.encounter_datetime <= :generationDate "
            + "  AND enc.location_id = :location "
            + "GROUP BY pa.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * incluindo todos os utentes do sexo feminino com registo de “Grávida” = “Sim” numa consulta
   * clínica (Ficha Clínica – Ficha Mestra) ocorrida nos últimos 9 meses do fim do período de
   * reporte (>= “Data Fim” - 9 meses e “Data Fim”). Nota: a “Data da Primeira Consulta Gravidez” é
   * a data da consulta na qual ocorreu o primeiro registo de “Grávida” = “Sim” nos últimos 9 meses
   * do fim do período de reporte
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsPregnantOnLast9Months() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Utentes mulheres grávidas");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());

    String pregnacyDateQuery = ListOfPatientsEligibleForCd4RequestQueries.getPregnancyQuery();
    String query = new EptsQueriesUtil().patientIdQueryBuilder(pregnacyDateQuery).getQuery();

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * utentes que tiveram registo de resultado de CD4 numa consulta clínica (Ficha Clínica – Ficha
   * Mestra) ocorrida entre “Data de Primeira Consulta Gravidez” e a data geração do relatório
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithCd4ResultsOnPregnancyDate() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "utentes que tiveram registo de resultado de CD4 Primeira Gravidez");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("generationDate", "generationDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("730", hivMetadata.getCD4PercentConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());

    String query =
        "SELECT pa.patient_id "
            + "FROM "
            + "    patient pa "
            + "        INNER JOIN encounter enc "
            + "                   ON enc.patient_id =  pa.patient_id "
            + "        INNER JOIN obs "
            + "                   ON obs.encounter_id = enc.encounter_id "
            + " INNER JOIN ( "
            + ListOfPatientsEligibleForCd4RequestQueries.getPregnancyQuery()
            + " ) pregnant ON pregnant.patient_id = pa.patient_id "
            + "WHERE  pa.voided = 0 "
            + "  AND enc.voided = 0 "
            + "  AND obs.voided = 0 "
            + "  AND enc.encounter_type = ${6} "
            + "  AND ( "
            + "        (obs.concept_id = ${1695} "
            + "             AND obs.value_numeric IS NOT NULL) "
            + "        OR "
            + "        (obs.concept_id = ${730} "
            + "             AND obs.value_numeric IS NOT NULL ) "
            + "      ) "
            + "  AND enc.encounter_datetime >= pregnant.pregnancy_date "
            + "  AND enc.encounter_datetime <= :generationDate "
            + "  AND enc.location_id = :location "
            + "GROUP BY pa.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>Utentes em TARV com registo de Óbito</b>
   * <li>Utentes com registo de “Óbito” (último estado de inscrição) no programa SERVIÇO TARV
   *     TRATAMENTO até a data geração do relatório (“Data de Óbito” <= Data Geração Relatório”;
   * <li>Utentes com registo do último estado [“Mudança Estado Permanência TARV” (Coluna 21) = “O”
   *     (Óbito) na Ficha Clínica com “Data da Consulta Actual” (Coluna 1, durante a qual se fez o
   *     registo da mudança do estado de permanência TARV) <= “Data Geração Relatório”; ou
   * <li>Utentes com último registo de “Mudança Estado Permanência TARV” = “Óbito” na Ficha Resumo
   *     com “Data de Óbito” <= “Data Geração Relatório”; ou
   * <li>Utentes com registo de “Óbito” nos “Dados Demográficos do Utente com “Data de Óbito” <=
   *     “Data Geração Relatório”;
   * <li>excepto os utentes que tenham tido um levantamento de ARV (FILA) ou uma consulta (Ficha
   *     Clínica) após a “Data de Óbito” (a data mais recente entre os critérios acima
   *     identificados) e até a “Data Geração do Relatório”.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getTransferredOutPatientsByGenerationDate() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Utentes em TARV com registo de Óbito");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("10", hivMetadata.getArtDeadWorkflowState().getProgramWorkflowStateId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1366", hivMetadata.getPatientHasDiedConcept().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("1", hivMetadata.getHIVCareProgram().getProgramId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());

    String query = listOfPatientsOnAdvancedHivIllnessQueries.getPatientsWhoDied(false);

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getSummaryCompositionC1() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName(
        "Definition to apply base cohort exclusions to each Summary Indicator");
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("generationDate", "generationDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition transferredOut = resumoMensalCohortQueries.getTranferredOutPatients();
    CohortDefinition died = getTransferredOutPatientsByGenerationDate();

    compositionCohortDefinition.addSearch(
        "C1",
        map(
            getPatientWhoInitiatedTarvDuringPeriodC1(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "BASECOHORT",
        EptsReportUtils.map(
            genericCohortQueries.getBaseCohort(), "endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "TRANSFERREDOUT",
        map(
            transferredOut,
            "startDate=${startDate},endDate=${endDate},onOrBefore=${generationDate},location=${location}"));
    compositionCohortDefinition.addSearch(
        "DIED", map(died, "endDate=${generationDate},location=${location}"));

    compositionCohortDefinition.setCompositionString(
        "(C1 AND BASECOHORT) AND NOT (TRANSFERREDOUT OR DIED)");

    return compositionCohortDefinition;
  }

  public CohortDefinition getSummaryCompositionC2() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName("Summary Indicator FOR C2");
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("generationDate", "generationDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition transferredOut = resumoMensalCohortQueries.getTranferredOutPatients();
    CohortDefinition died = getTransferredOutPatientsByGenerationDate();

    compositionCohortDefinition.addSearch(
        "C2",
        map(
            getPatientWhoRestartedTarvAndEligibleForCd4RequestC2(),
            "startDate=${startDate},endDate=${endDate},generationDate=${generationDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "C1",
        map(
            getPatientWhoInitiatedTarvDuringPeriodC1(),
            "startDate=${startDate},endDate=${endDate},generationDate=${generationDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "TRANSFERREDOUT",
        map(
            transferredOut,
            "startDate=${startDate},endDate=${endDate},onOrBefore=${generationDate},location=${location}"));
    compositionCohortDefinition.addSearch(
        "DIED", map(died, "endDate=${generationDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "BASECOHORT",
        EptsReportUtils.map(
            genericCohortQueries.getBaseCohort(), "endDate=${endDate},location=${location}"));

    compositionCohortDefinition.setCompositionString(
        "((C2 AND BASECOHORT) AND NOT (TRANSFERREDOUT OR DIED)) AND NOT C1");

    return compositionCohortDefinition;
  }

  public CohortDefinition getSummaryCompositionC3() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName("Summary Indicator FOR C3");
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("generationDate", "generationDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition transferredOut = resumoMensalCohortQueries.getTranferredOutPatients();
    CohortDefinition died = getTransferredOutPatientsByGenerationDate();

    compositionCohortDefinition.addSearch(
        "C1",
        map(
            getPatientWhoInitiatedTarvDuringPeriodC1(),
            "startDate=${startDate},endDate=${endDate},generationDate=${generationDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "C2",
        map(
            getPatientWhoRestartedTarvAndEligibleForCd4RequestC2(),
            "startDate=${startDate},endDate=${endDate},generationDate=${generationDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "C3",
        map(
            getPatientsWithTwoHighVlResultsC3(),
            "endDate=${endDate},generationDate=${generationDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "TRANSFERREDOUT",
        map(
            transferredOut,
            "startDate=${startDate},endDate=${endDate},onOrBefore=${generationDate},location=${location}"));
    compositionCohortDefinition.addSearch(
        "DIED", map(died, "endDate=${generationDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "BASECOHORT",
        EptsReportUtils.map(
            genericCohortQueries.getBaseCohort(), "endDate=${endDate},location=${location}"));

    compositionCohortDefinition.setCompositionString(
        "((C3 AND BASECOHORT) AND NOT (TRANSFERREDOUT OR DIED)) AND NOT (C1 OR C2)");

    return compositionCohortDefinition;
  }

  public CohortDefinition getSummaryCompositionC4() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName("Summary Indicator FOR C4");
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("generationDate", "generationDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition transferredOut = resumoMensalCohortQueries.getTranferredOutPatients();
    CohortDefinition died = getTransferredOutPatientsByGenerationDate();

    compositionCohortDefinition.addSearch(
        "C1",
        map(
            getPatientWhoInitiatedTarvDuringPeriodC1(),
            "startDate=${startDate},endDate=${endDate},generationDate=${generationDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "C3",
        map(
            getPatientsWithTwoHighVlResultsC3(),
            "endDate=${endDate},generationDate=${generationDate},location=${location}"));
    compositionCohortDefinition.addSearch(
        "C2",
        map(
            getPatientWhoRestartedTarvAndEligibleForCd4RequestC2(),
            "startDate=${startDate},endDate=${endDate},generationDate=${generationDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "C4",
        map(
            getPatientWithEstadiamentoIIIorIVC4(),
            "startDate=${startDate},endDate=${endDate},generationDate=${generationDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "TRANSFERREDOUT",
        map(
            transferredOut,
            "startDate=${startDate},endDate=${endDate},onOrBefore=${generationDate},location=${location}"));
    compositionCohortDefinition.addSearch(
        "DIED", map(died, "endDate=${generationDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "BASECOHORT",
        EptsReportUtils.map(
            genericCohortQueries.getBaseCohort(), "endDate=${endDate},location=${location}"));

    compositionCohortDefinition.setCompositionString(
        "((C4 AND BASECOHORT) AND NOT (TRANSFERREDOUT OR DIED)) AND NOT (C1 OR C2 OR C3)");

    return compositionCohortDefinition;
  }

  public CohortDefinition getSummaryCompositionC5() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName("Summary Indicator FOR C5");
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("generationDate", "generationDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition transferredOut = resumoMensalCohortQueries.getTranferredOutPatients();
    CohortDefinition died = getTransferredOutPatientsByGenerationDate();

    compositionCohortDefinition.addSearch(
        "C1",
        map(
            getPatientWhoInitiatedTarvDuringPeriodC1(),
            "startDate=${startDate},endDate=${endDate},generationDate=${generationDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "C2",
        map(
            getPatientWhoRestartedTarvAndEligibleForCd4RequestC2(),
            "startDate=${startDate},endDate=${endDate},generationDate=${generationDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "C3",
        map(
            getPatientsWithTwoHighVlResultsC3(),
            "endDate=${endDate},generationDate=${generationDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "C4",
        map(
            getPatientWithEstadiamentoIIIorIVC4(),
            "startDate=${startDate},endDate=${endDate},generationDate=${generationDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "C5",
        map(
            getPatientEligibleForCd4FollowupC5(),
            "endDate=${endDate},generationDate=${generationDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "TRANSFERREDOUT",
        map(
            transferredOut,
            "startDate=${startDate},endDate=${endDate},onOrBefore=${generationDate},location=${location}"));
    compositionCohortDefinition.addSearch(
        "DIED", map(died, "endDate=${generationDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "BASECOHORT",
        EptsReportUtils.map(
            genericCohortQueries.getBaseCohort(), "endDate=${endDate},location=${location}"));

    compositionCohortDefinition.setCompositionString(
        "((C5 AND BASECOHORT) AND NOT (TRANSFERREDOUT OR DIED)) AND NOT (C1 OR C2 OR C3 OR  C4)");

    return compositionCohortDefinition;
  }

  public CohortDefinition getSummaryCompositionC6() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName("Summary Indicator FOR C6");
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("generationDate", "generationDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition transferredOut = resumoMensalCohortQueries.getTranferredOutPatients();
    CohortDefinition died = getTransferredOutPatientsByGenerationDate();

    compositionCohortDefinition.addSearch(
        "C1",
        map(
            getPatientWhoInitiatedTarvDuringPeriodC1(),
            "startDate=${startDate},endDate=${endDate},generationDate=${generationDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "C2",
        map(
            getPatientWhoRestartedTarvAndEligibleForCd4RequestC2(),
            "startDate=${startDate},endDate=${endDate},generationDate=${generationDate},location=${location}"));
    compositionCohortDefinition.addSearch(
        "C3",
        map(
            getPatientsWithTwoHighVlResultsC3(),
            "endDate=${endDate},generationDate=${generationDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "C4",
        map(
            getPatientWithEstadiamentoIIIorIVC4(),
            "startDate=${startDate},endDate=${endDate},generationDate=${generationDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "C5",
        map(
            getPatientEligibleForCd4FollowupC5(),
            "endDate=${endDate},generationDate=${generationDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "C6",
        map(
            getPatientPregnantEligibleForCd4RequestC6(),
            "startDate=${startDate},endDate=${endDate},generationDate=${generationDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "TRANSFERREDOUT",
        map(
            transferredOut,
            "startDate=${startDate},endDate=${endDate},onOrBefore=${generationDate},location=${location}"));
    compositionCohortDefinition.addSearch(
        "DIED", map(died, "endDate=${generationDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "BASECOHORT",
        EptsReportUtils.map(
            genericCohortQueries.getBaseCohort(), "endDate=${endDate},location=${location}"));

    compositionCohortDefinition.setCompositionString(
        "((C6 AND BASECOHORT) AND NOT (TRANSFERREDOUT OR DIED)) AND NOT (C1 OR C2 OR C3 OR C4 OR C5)");

    return compositionCohortDefinition;
  }
}
