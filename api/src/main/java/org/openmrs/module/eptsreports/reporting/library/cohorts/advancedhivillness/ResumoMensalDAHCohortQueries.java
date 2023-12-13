package org.openmrs.module.eptsreports.reporting.library.cohorts.advancedhivillness;

import static org.openmrs.module.eptsreports.reporting.library.cohorts.AdvancedDiseaseAndTBCascadeCohortQueries.*;
import static org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils.map;
import static org.openmrs.module.reporting.evaluation.parameter.Mapped.mapStraightThrough;

import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.eptsreports.reporting.library.cohorts.AgeCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.IntensiveMonitoringCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ResumoMensalCohortQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
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

  private final TbMetadata tbMetadata;

  private final ResumoMensalCohortQueries resumoMensalCohortQueries;

  private final IntensiveMonitoringCohortQueries intensiveMonitoringCohortQueries;

  private AgeCohortQueries ageCohortQueries;

  @Autowired
  public ResumoMensalDAHCohortQueries(
      ListOfPatientsInAdvancedHivIllnessCohortQueries
          listOfPatientsInAdvancedHivIllnessCohortQueries,
      HivMetadata hivMetadata,
      TbMetadata tbMetadata,
      ResumoMensalCohortQueries resumoMensalCohortQueries,
      IntensiveMonitoringCohortQueries intensiveMonitoringCohortQueries,
      AgeCohortQueries ageCohortQueries) {
    this.listOfPatientsInAdvancedHivIllnessCohortQueries =
        listOfPatientsInAdvancedHivIllnessCohortQueries;
    this.hivMetadata = hivMetadata;
    this.tbMetadata = tbMetadata;
    this.resumoMensalCohortQueries = resumoMensalCohortQueries;
    this.intensiveMonitoringCohortQueries = intensiveMonitoringCohortQueries;
    this.ageCohortQueries = ageCohortQueries;
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
        mapStraightThrough(
            listOfPatientsInAdvancedHivIllnessCohortQueries.getPatientsWhoStartedFollowupOnDAH(
                false)));

    cd.addSearch(
        "leftTreatment",
        mapStraightThrough(getPatientsWhoLeftFollowupOnDAHByTheEndOfPreviousMonth()));

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
            getB1IfPatientDontHaveTarvSituationOnDah(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

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

    cd.addSearch(
        "I1",
        map(
            getPatientsWhoAreNewInArtAndStartedFollowupDuringTheMonthComposition(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("(onDAHDuringPeriod AND (restartedArt OR B3)) AND NOT I1");
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

    cd.addSearch(
        "I1",
        map(
            getPatientsWhoAreNewInArtAndStartedFollowupDuringTheMonthComposition(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "I2",
        map(
            getPatientsWhoRestartedArtAndStartedFollowupDuringTheMonthComposition(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("(onDAHDuringPeriod AND (onArt OR B12)) AND NOT (I1 OR I2)");
    return cd;
  }

  /**
   * <b>Relatório- Indicador 6 –Óbitos na Coorte de 6 meses</b>
   * <li>Com registo de “Data de Início no Modelo de DAH”, na Ficha de DAH, ocorrida na coorte de 6
   *     meses (“Data de Início no Modelo de DAH”>= “Data Início” - 7 meses e <= “Data Início” – 6
   *     meses – 1 dia)
   *
   *     <p>Filtrando todos os utentes
   * <li>Com registo de “Data de Saída de TARV na US” >= “Data de Início no Modelo de DAH” (>= “Data
   *     Início” - 7 meses e <= “Data Início” – 6 meses – 1 dia) e <= “Data fim” e “Motivo de Saída”
   *     = “Óbito” ou
   * <li>Com registo de óbito (Indicador B.8 do relatório Resumo Mensal de HIV/SIDA) até a data fim
   *     do período
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoAreMarkedAsDeadDOnSixMonthsCohortComposition() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Relatório- Indicador 6 –Óbitos na Coorte de 6 meses");
    cd.addParameters(getCohortParameters());

    cd.addSearch(
        "onDAHDuringPeriod",
        map(
            listOfPatientsInAdvancedHivIllnessCohortQueries.getPatientsWhoStartedFollowupOnDAH(
                true),
            "startDate=${startDate-7m},endDate=${startDate-6m-1d},location=${location}"));

    cd.addSearch(
        "diedAfterStartDah",
        map(
            getPatientsWhoAreMarkedAsDeadDOnSixMonthsCohortAndAfterFollowupDate(),
            "startDate=${startDate-7m},endDate=${startDate-6m-1d},reportEndDate=${endDate},location=${location}"));

    cd.addSearch(
        "B8",
        map(
            resumoMensalCohortQueries.getPatientsWhoDiedB8(true),
            "onOrAfter=${startDate},onOrBefore=${startDate-6m-1d},locationList=${location}"));

    cd.setCompositionString("onDAHDuringPeriod AND (diedAfterStartDah OR B8)");
    return cd;
  }

  /**
   * <b>Relatório – Indicador 10 Resultado de CD4 baixo</b>
   *
   * <p>Incluindo todos os utentes
   * <li>que tiveram registo do “Resultado de CD4” secção B (Exames Laboratoriais à entrada e de
   *     seguimento) na Ficha de DAH, com respectiva “Data de CD4” ocorrida durante o período (>=
   *     “Data Início” e <= “Data Fim”) ou
   * <li>com registo de "CD4 – Resultados Laboratoriais” (Coluna 16) na “Ficha Clínica” com “Data de
   *     Consulta” ocorrida durante o período (>= “Data Início” e <= “Data Fim”)
   *
   *     <p>Filtrando os utentes com o respectivo “Resultado de CD4” (identificado nos critérios
   *     acima definidos) de acordo com a seguinte definição:
   * <li>< 750 para os utentes com idade < 1 ano
   * <li>< 500 para os utentes com idade entre 1 a 4anos
   * <li><200 para os utentes com idade >= 5 anos
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithLowCd4Results() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Relatório – Indicador 10 Resultado de CD4 baixo");
    cd.addParameters(getCohortParameters());

    cd.addSearch("haveCd4Results", mapStraightThrough(getPatientsWhoHaveCd4Results()));

    cd.addSearch(
        "cd4ByAgeAndResult", mapStraightThrough(getPatientsWithCD4BasedOnAgeAndCd4Results()));

    cd.setCompositionString("haveCd4Results AND cd4ByAgeAndResult");
    return cd;
  }

  /**
   * <b>Relatório – Indicador 11 Resultado TB LAM</b>
   * <li>Incluindo todos os utentes com resultado de CD4 baixo durante o período compreendido entre
   *     “Data Início” menos (-) 1 mês e “Data Fim” (seguindo os critérios definidos no Indicador 10
   *     – RF16 com período diferente)
   *
   *     <p>Filtrando todos os utentes
   * <li>que tiveram registo de "TB LAM urina” registada na secção B (Exames Laboratoriais à e
   *     ntrada e de seguimento) da Ficha de DAH, com a respectiva “Data de TB LAM” ocorrida durante
   *     o período (>= “Data Início” e <= “Data Fim”) e resposta igual a “Pos” ou “Neg”, ou
   * <li>que tiveram registo do "TB LAM – Resultados Laboratoriais” (Coluna 16) ”), na “Ficha
   *     Clínica” com “Data de Consulta” ocorrida durante o período (>= “Data Início” e <= “Data
   *     Fim) e resultado igual a “Positivo” ou “Negativo”.
   *
   * @see #getPatientsWhoHaveCd4Results
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithTBLAMResults() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Relatório – Indicador 11 Resultado TB LAM");
    cd.addParameters(getCohortParameters());

    cd.addSearch(
        "haveCd4Results",
        map(
            getPatientsWhoHaveCd4Results(),
            "startDate=${startDate-1m},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "tbLamResults",
        mapStraightThrough(
            getPatientsWithPositiveOrNegativeTestResults(
                Collections.singletonList(tbMetadata.getTestTBLAM()),
                Arrays.asList(hivMetadata.getPositive(), hivMetadata.getNegative()))));

    cd.setCompositionString("haveCd4Results AND tbLamResults");
    return cd;
  }

  /**
   * <b> Relatório – Indicador 12 Resultado de TB LAM Positivo</b>
   * <li>Incluindo todos os utentes do indicador 11 – RF17
   *
   *     <p>Filtrando os utentes
   * <li>que tiveram registo de "TB LAM urina” registada na secção B (Exames Laboratoriais à e
   *     ntrada e de seguimento) da Ficha de DAH, com a respectiva “Data de TB LAM” ocorrida durante
   *     o período (>= “Data Início” e <= “Data Fim”) e resposta igual a “Pos”, ou
   * <li>que tiveram registo do "TB LAM – Resultados Laboratoriais” (Coluna 16) ”), na “Ficha
   *     Clínica” com “Data de Consulta” ocorrida durante o período (>= “Data Início” e <= “Data
   *     Fim) e resultado igual a “Positivo”.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithPositiveTBLAMResults() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Relatório – Indicador 12 Resultado de TB LAM Positivo");
    cd.addParameters(getCohortParameters());

    cd.addSearch("tbLamResults", mapStraightThrough(getPatientsWithTBLAMResults()));

    cd.addSearch(
        "tbLamPositive",
        mapStraightThrough(
            getPatientsWithPositiveOrNegativeTestResults(
                Collections.singletonList(tbMetadata.getTestTBLAM()),
                Collections.singletonList(hivMetadata.getPositive()))));

    cd.setCompositionString("tbLamResults AND tbLamPositive");
    return cd;
  }

  /**
   * <b> Relatório – Indicador 13 CD4 Baixo e Resultado de CrAg Sérico</b>
   * <li>Incluindo todos os utentes com resultado de CD4 baixo durante o período compreendido entre
   *     “Data Início” menos (-) 1 mês e “Data Fim” (seguindo os critérios definidos no Indicador 10
   *     – RF16 com período diferente)
   *
   *     <p>Filtrando os utentes
   * <li>que tiveram registo de "CrAg Soro” na secção B (Exames Laboratoriais à entrada e de
   *     seguimento) da FDAH e “Data de CrAg Soro” ocorrida durante o período (>= “Data Início” e <=
   *     “Data Fim”) e com resposta igual a “Pos”, “Neg” ou
   * <li>que tiveram registo de "CrAg – Resultados Laboratoriais” (Coluna 16) na “Ficha Clínica” e
   *     “Data de Consulta” ocorrida durante o período (>= “Data Início” e <= “Data Fim”) e com
   *     resultado igual a “Positivo” ou “Negativo”.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithLowCd4AndCragResults() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Relatório – Indicador 13 CD4 Baixo e Resultado de CrAg Sérico");
    cd.addParameters(getCohortParameters());

    cd.addSearch(
        "haveCd4Results",
        map(
            getPatientsWhoHaveCd4Results(),
            "startDate=${startDate-1m},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "cragResults",
        mapStraightThrough(
            getPatientsWithPositiveOrNegativeTestResults(
                Arrays.asList(
                    hivMetadata.getCragSoroLabsetConcept(), hivMetadata.getCragSoroConcept()),
                Arrays.asList(hivMetadata.getPositive(), hivMetadata.getNegative()))));

    cd.setCompositionString("haveCd4Results AND cragResults");
    return cd;
  }

  /**
   * <b>Indicador 14 CD4 Baixo e Resultado de CrAg Sérico Positivo</b>
   * <li>Incluindo todos os utentes do indicador 13 – RF19
   *
   *     <p>Filtrando os utentes
   * <li>que tiveram registo de "CrAg Soro” registada na secção B (Exames Laboratoriais à entrada e
   *     de seguimento) da FDAH e “Data de CrAg Soro” ocorrida durante o período (>= “Data Início” e
   *     <= “Data Fim”) com resposta igual “Pos”, ou
   * <li>que tiveram registo de "CrAg – Resultados Laboratoriais” (Coluna 16) na “Ficha Clínica” e
   *     “Data de Consulta” ocorrida durante o período (>= “Data Início” e <= “Data Fim”) com
   *     resultado igual a “Positivo”
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithLowCd4AndPositiveCragResults() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Indicador 14 CD4 Baixo e Resultado de CrAg Sérico Positivo");
    cd.addParameters(getCohortParameters());

    cd.addSearch("cragResults", mapStraightThrough(getPatientsWithLowCd4AndCragResults()));

    cd.addSearch(
        "cragPositive",
        mapStraightThrough(
            getPatientsWithPositiveOrNegativeTestResults(
                Arrays.asList(
                    hivMetadata.getCragSoroLabsetConcept(), hivMetadata.getCragSoroConcept()),
                Collections.singletonList(hivMetadata.getPositive()))));

    cd.setCompositionString("cragResults AND cragPositive");
    return cd;
  }

  /**
   * <b>Indicador 15 Utentes CrAg sérico Positivo e registo de CrAg no LCR</b>
   * <li>Incluindo todos os utentes do indicador 14 – RF20
   *
   *     <p>Filtrando os utentes
   * <li>que tiveram registo de "CrAg LCR” registada na secção B (Exames Laboratoriais à entrada e
   *     de seguimento) da FDAH e “Data de CrAg LCR” ocorrida durante o período (>= “Data Início” e
   *     <= “Data Fim”) e assinalado com resultado = “Pos” ou “Neg”.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithPositiveOrNegativeOnCragLCRResults() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Indicador 15 Utentes CrAg sérico Positivo e registo de CrAg no LCR");
    cd.addParameters(getCohortParameters());

    cd.addSearch("cragPositive", mapStraightThrough(getPatientsWithLowCd4AndPositiveCragResults()));

    cd.addSearch(
        "cragResults",
        mapStraightThrough(
            getPatientsWithPositiveOrNegativeCragLCRResults(
                Arrays.asList(hivMetadata.getPositive(), hivMetadata.getNegative()))));

    cd.setCompositionString("cragPositive AND cragResults");
    return cd;
  }

  /**
   * <b>Relatório – Indicador 16 CrAg sérico positivo e início de MCC Preventivo</b>
   * <li>Incluindo todos os utentes do indicador 14 – RF18
   * <li>Filtrando os utentes que tiveram registo de “Tratamento Preventivo de MCC” na secção D
   *     (Tratamento Preventivo da Meningite Criptocócica) da Ficha de DAH com “Data Início de
   *     Indução” ocorrida durante o período (>= “Data Início” e <= “Data Fim)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithPositiveCragResultsAndStartedMcc() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Indicador 16 CrAg sérico positivo e início de MCC Preventivo");
    cd.addParameters(getCohortParameters());

    cd.addSearch("cragPositive", mapStraightThrough(getPatientsWithLowCd4AndPositiveCragResults()));

    cd.addSearch("mccPreventivo", mapStraightThrough(getPatientsWhoStartedMccPreventivo()));

    cd.setCompositionString("cragPositive AND mccPreventivo");
    return cd;
  }

  /**
   * <b>Relatório – Indicador 17 CrAg LCR positivo e início de MCC</b>
   * <li>Incluindo todos os utentes do indicador 14 – RF20
   * <li>Filtrando os utentes que tiveram registo de "CrAg LCR” registada na secção B (Exames
   *     Laboratoriais à entrada e de seguimento) da FDAH e “Data de CrAg LCR” ocorrida durante o
   *     período (>= “Data Início” e <= “Data Fim”) e assinalado com resultado = “Pos”.
   * <li>Filtrando os utentes que tiveram registo de “Tratamento de MCC” na secção C (Tratamento da
   *     Meningite Criptocócica) da Ficha de DAH com a “Data de Anfotericina lipossômica (Dose
   *     Única)” ocorrida durante o período (>= “Data Início” e <= “Data Fim)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithPositiveCragLcrResultsAndStartedMcc() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Indicador 17 CrAg LCR positivo e início de MCC");
    cd.addParameters(getCohortParameters());

    cd.addSearch("cragPositive", mapStraightThrough(getPatientsWithLowCd4AndPositiveCragResults()));

    cd.addSearch(
        "cragLCRPositive",
        mapStraightThrough(
            getPatientsWithPositiveOrNegativeCragLCRResults(
                Collections.singletonList(hivMetadata.getPositive()))));

    cd.addSearch("mmcTreatment", mapStraightThrough(getPatientsInMccTretament()));

    cd.setCompositionString("cragPositive AND cragLCRPositive AND mmcTreatment");
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
            + "            AND o.obs_datetime >= last_dah.last_date "
            + "            AND o.obs_datetime <= :startDate) "
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

  /**
   *
   * <li>Com registo de “Data de Saída de TARV na US” >= “Data de Início no Modelo de DAH” (>= “Data
   *     Início” - 7 meses e <= “Data Início” – 6 meses – 1 dia) e <= “Data fim” e “Motivo de Saída”
   *     = “Óbito”
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoAreMarkedAsDeadDOnSixMonthsCohortAndAfterFollowupDate() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("reportEndDate", "reportEndDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("90", hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId());
    map.put("1708", hivMetadata.getExitFromArvTreatmentConcept().getConceptId());
    map.put("1366", hivMetadata.getPatientHasDiedConcept().getConceptId());
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
            + "  AND e.encounter_datetime >= :startDate "
            + "  AND e.encounter_datetime <= :endDate "
            + "          AND e.location_id = :location "
            + "        GROUP BY p.patient_id "
            + "    ) last_dah ON last_dah.patient_id = p.patient_id "
            + "WHERE p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND e.encounter_type = ${90} "
            + "  AND ( "
            + "        (o.concept_id = ${1708} "
            + "            AND o.value_coded = ${1366} "
            + "            AND o.obs_datetime >= last_dah.last_date "
            + "            AND o.obs_datetime <= :reportEndDate) "
            + "        OR  (o.concept_id = ${165386} "
            + "        AND o.value_datetime >= last_dah.last_date "
            + "        AND o.value_datetime <= :reportEndDate) "
            + "    ) "
            + "  AND e.location_id = :location "
            + "GROUP BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(substitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>Relatório- Indicador 8 – Pedido de CD4</b>
   * <li>Incluindo todos os utentesque tiveram registo de "CD4 – Pedido” (Coluna 15) na “Ficha
   *     Clínica” com “Data de Consulta” ocorrida durante o período (>= “Data Início” e <= “Data
   *     Fim”)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoHaveCd4Request() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Relatório- Indicador 8 – Pedido de CD4");
    sqlCohortDefinition.addParameters(getCohortParameters());

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
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
            + "       AND o.value_coded = ${1695} "
            + "       AND e.encounter_datetime >= :startDate "
            + "       AND e.encounter_datetime <= :endDate "
            + " GROUP BY p.patient_id ";

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));
    return sqlCohortDefinition;
  }

  /**
   * <b>Relatório- Indicador 9 – Resultado de CD4</b>
   * <li>que tiveram registo do “Resultado de CD4” secção B (Exames Laboratoriais à entrada e de
   *     seguimento) na Ficha de DAH, com respectiva “Data de CD4” ocorrida durante o período (>=
   *     “Data Início” e <= “Data Fim”) ou
   * <li>com registo de "CD4 – Resultados Laboratoriais” (Coluna 16) na “Ficha Clínica” com “Data de
   *     Consulta” ocorrida durante o período (>= “Data Início” e <= “Data Fim”)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoHaveCd4Results() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Relatório- Indicador 9 – Resultado de CD4");
    sqlCohortDefinition.addParameters(getCohortParameters());

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("90", hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM "
            + "    patient p INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "              INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "WHERE p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND e.location_id = :location "
            + "  AND o.concept_id = ${1695} "
            + "  AND o.value_numeric IS NOT NULL "
            + " AND ( "
            + "  ( e.encounter_type = ${90} "
            + " AND o.obs_datetime >= :startDate "
            + "  AND o.obs_datetime <= :endDate)"
            + "OR "
            + " ( e.encounter_type = ${6} "
            + " AND e.encounter_datetime >= :startDate "
            + "  AND e.encounter_datetime <= :endDate) "
            + " ) "
            + "GROUP BY p.patient_id ";

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));
    return sqlCohortDefinition;
  }

  /**
   * Filtrando os utentes com o respectivo “Resultado de CD4” (identificado nos critérios acima
   * definidos) de acordo com a seguinte definição:
   * <li>< 750 para os utentes com idade < 1 ano
   * <li>< 500 para os utentes com idade entre 1 a 4anos
   * <li><200 para os utentes com idade >= 5 anos
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithCD4BasedOnAgeAndCd4Results() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        " “Resultado de CD4” (identificado nos critérios acima definidos) de acordo com a seguinte definição");
    cd.addParameters(getCohortParameters());

    CohortDefinition cd200overOrEqualTo5years =
        getPatientsWithCD4BasedOnAgeAndCd4(Cd4CountComparison.LessThanOrEqualTo200mm3, 5, null);
    CohortDefinition cd500betweenOneAnd5years =
        getPatientsWithCD4BasedOnAgeAndCd4(Cd4CountComparison.LessThanOrEqualTo500mm3, 1, 4);
    CohortDefinition cd750bellowOneYear =
        getPatientsWithCD4BasedOnAgeAndCd4(Cd4CountComparison.LessThanOrEqualTo750mm3, null, 1);

    cd.addSearch("cd4Under200", mapStraightThrough(cd200overOrEqualTo5years));
    cd.addSearch("cd4Under500", mapStraightThrough(cd500betweenOneAnd5years));
    cd.addSearch("cd4Under750", mapStraightThrough(cd750bellowOneYear));

    cd.setCompositionString("cd4Under200 OR cd4Under500 OR cd4Under750");
    return cd;
  }

  /**
   * @param cd4 Amount of Cd4 for each range of age
   * @param minAge Minimum age to check
   * @param maxAge Maximum age to check
   * @return {@link CohortDefinition}
   */
  private CohortDefinition getPatientsWithCD4BasedOnAgeAndCd4(
      Cd4CountComparison cd4, Integer minAge, Integer maxAge) {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Cd4 Result and Age Combination");
    cd.addParameters(getCohortParameters());

    CohortDefinition cd4Result = getCd4Result(cd4);
    CohortDefinition age = ageCohortQueries.createXtoYAgeCohort("Age", minAge, maxAge);

    cd.addSearch("cd4Result", mapStraightThrough(cd4Result));
    cd.addSearch("age", EptsReportUtils.map(age, "effectiveDate=${endDate}"));

    cd.setCompositionString("cd4Result AND age");
    return cd;
  }
  /**
   * Filtrando todos os utentes
   * <li>que tiveram registo de "TB LAM urina” registada na secção B (Exames Laboratoriais à e
   *     ntrada e de seguimento) da Ficha de DAH, com a respectiva “Data de TB LAM” ocorrida durante
   *     o período (>= “Data Início” e <= “Data Fim”) e resposta igual a “Pos” ou “Neg”, ou
   * <li>que tiveram registo do "TB LAM – Resultados Laboratoriais” (Coluna 16) ”), na “Ficha
   *     Clínica” com “Data de Consulta” ocorrida durante o período (>= “Data Início” e <= “Data
   *     Fim) e resultado igual a “Positivo” ou “Negativo”.
   *
   * @param resultConceptList List of result concepts to be checked
   * @param examConceptList List of result exams to be checked
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithPositiveOrNegativeTestResults(
      List<Concept> examConceptList, List<Concept> resultConceptList) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Utentes que tiveram resultado de TBLAM");
    sqlCohortDefinition.addParameters(getCohortParameters());

    List<Integer> examConceptIdsList =
        examConceptList.stream().map(Concept::getConceptId).collect(Collectors.toList());

    List<Integer> resultConceptIdsList =
        resultConceptList.stream().map(Concept::getConceptId).collect(Collectors.toList());

    Map<String, String> map = new HashMap<>();
    map.put(
        "6", String.valueOf(hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId()));
    map.put(
        "90",
        String.valueOf(hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId()));
    map.put("examConcept", StringUtils.join(examConceptIdsList, ","));
    map.put("resultConcept", StringUtils.join(resultConceptIdsList, ","));

    String query =
        "SELECT p.patient_id "
            + "FROM "
            + "    patient p INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "              INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "WHERE p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND e.location_id = :location "
            + "  AND o.concept_id IN ( ${examConcept} ) "
            + "  AND o.value_coded IN (${resultConcept}) "
            + "  AND ( "
            + "        ( e.encounter_type = ${90} AND "
            + " o.obs_datetime >= :startDate "
            + "            AND o.obs_datetime <= :endDate) "
            + "        OR "
            + "        ( e.encounter_type = ${6} AND "
            + " e.encounter_datetime >= :startDate "
            + "            AND e.encounter_datetime <= :endDate) "
            + "    ) "
            + "GROUP BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(substitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   *
   * <li>Utentes que tiveram registo de "CrAg LCR” registada na secção B (Exames Laboratoriais à
   *     entrada e de seguimento) da FDAH e “Data de CrAg LCR” ocorrida durante o período (>= “Data
   *     Início” e <= “Data Fim”) e assinalado com resultado = “Pos” ou “Neg”.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithPositiveOrNegativeCragLCRResults(
      List<Concept> resultConceptList) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Utentes que tiveram resultado de Crag no LCR");
    sqlCohortDefinition.addParameters(getCohortParameters());

    List<Integer> resultConceptIdsList =
        resultConceptList.stream().map(Concept::getConceptId).collect(Collectors.toList());
    Map<String, String> map = new HashMap<>();
    map.put(
        "90",
        String.valueOf(hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId()));
    map.put("165362", String.valueOf(hivMetadata.getCragLCRConcept().getConceptId()));
    map.put("resultConcept", StringUtils.join(resultConceptIdsList, ","));

    String query =
        "SELECT p.patient_id "
            + "FROM "
            + "    patient p INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "              INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "WHERE p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND e.location_id = :location "
            + "  AND e.encounter_type = ${90} "
            + "  AND o.concept_id = ${165362} "
            + "  AND o.value_coded IN (${resultConcept}) "
            + "  AND o.obs_datetime >= :startDate "
            + "  AND o.obs_datetime <= :endDate "
            + "GROUP BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(substitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   *
   * <li>Filtrando os utentes que tiveram registo de “Tratamento Preventivo de MCC” na secção D
   *     (Tratamento Preventivo da Meningite Criptocócica) da Ficha de DAH com “Data Início de
   *     Indução” ocorrida durante o período (>= “Data Início” e <= “Data Fim)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoStartedMccPreventivo() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Utentes que Iniciaram MCC Preventivo");
    sqlCohortDefinition.addParameters(getCohortParameters());

    Map<String, Integer> map = new HashMap<>();
    map.put("90", hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId());
    map.put("165393", hivMetadata.getTreatmentStartDateConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM "
            + "    patient p INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "              INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "WHERE p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND e.location_id = :location "
            + "  AND e.encounter_type = ${90} "
            + "  AND o.concept_id = ${165393} "
            + "  AND o.value_datetime >= :startDate "
            + "  AND o.value_datetime <= :endDate "
            + "GROUP BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(substitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   *
   * <li>Filtrando os utentes que tiveram registo de “Tratamento de MCC” na secção C (Tratamento da
   *     Meningite Criptocócica) da Ficha de DAH com a “Data de Anfotericina lipossômica (Dose
   *     Única)” ocorrida durante o período (>= “Data Início” e <= “Data Fim)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsInMccTretament() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Utentes em Tratamento de MCC");
    sqlCohortDefinition.addParameters(getCohortParameters());

    Map<String, Integer> map = new HashMap<>();
    map.put("90", hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId());
    map.put("165363", hivMetadata.getInducaoAnfotericinaLipossomicaConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM "
            + "    patient p INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "              INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "WHERE p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND e.location_id = :location "
            + "  AND e.encounter_type = ${90} "
            + "  AND o.concept_id = ${165363} "
            + "  AND o.value_datetime >= :startDate "
            + "  AND o.value_datetime <= :endDate "
            + "GROUP BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(substitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b> Relatório – Indicador 18 SK e Indicação de quimioterapia</b>
   * <li>Incluindo todos os utentes que tiveram registo de "Data de Diagnóstico SK” registada na
   *     secção H (Sarcoma de Kaposi (SK)) da Ficha de DAH, com “Data do Diagnóstico de SK” ocorrida
   *     durante o período (>= “Data Início” e <= “Data Fim”) e com o campo “Indicação para
   *     quimioterapia (QT) = “Sim” registado na Ficha de DAH, onde ocorreu o diagnóstico.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithSarcomaSKAndQuimiotherapyIndication() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Utentes com SK e indiccacao de Quimioterapia");
    sqlCohortDefinition.addParameters(getCohortParameters());

    Map<String, Integer> map = new HashMap<>();
    map.put("90", hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1413", hivMetadata.getDateOfDiagnosisOfKaposiSarcomaConcept().getConceptId());
    map.put("20294", hivMetadata.getOutraQuimioterapiaConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM "
            + "    patient p INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "              INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "              INNER JOIN obs o2 on e.encounter_id = o2.encounter_id "
            + "WHERE p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND o2.voided = 0 "
            + "  AND e.location_id = :location "
            + "  AND e.encounter_type = ${90} "
            + "  AND o.concept_id = ${1413} "
            + "  AND o.value_datetime >= :startDate "
            + "  AND o.value_datetime <= :endDate "
            + "  AND o2.concept_id = ${20294} "
            + "  AND o2.value_coded = ${1065} "
            + "GROUP BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(substitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>Relatório – Indicador 19 SK e Início de quimioterapia</b>
   * <li>Incluindo todos os utentes com o campo “Data de Inicio de QT – Ciclo 1 (C1)” ocorrida
   *     durante o período (>= “Data Início” e <= “Data Fim”), registados na Ficha de DAH.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithSarcomaSKAndStartedQuimiotherapy() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Utentes com SK e Inicaram Quimioterapia");
    sqlCohortDefinition.addParameters(getCohortParameters());

    Map<String, Integer> map = new HashMap<>();
    map.put("90", hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId());
    map.put("165379", hivMetadata.getStartDateForQuimiotherapyConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM "
            + "    patient p INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "              INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "WHERE p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND e.location_id = :location "
            + "  AND e.encounter_type = ${90} "
            + "  AND o.concept_id = ${165379} "
            + "  AND o.value_datetime >= :startDate "
            + "  AND o.value_datetime <= :endDate "
            + "GROUP BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(substitutor.replace(query));

    return sqlCohortDefinition;
  }

  // DISAGGREGATIONS

  /**
   * <b>Relatório Desagregação - Novos inícios TARV</b>
   *
   * <p>Incluindo todos os utentes
   * <li>Registados como “Novo Início" no campo “Situação do TARV no início do seguimento” (Secção
   *     A) da Ficha de DAH que tem o registo de “Data de Início no Modelo de DAH” ocorrida durante
   *     o período (“Data de Início no Modelo de DAH”>= “Data Início” e <= “Data Fim”)
   * <li>Caso não exista o registo da “Situação do TARV no Início do Seguimento” na Ficha de DAH que
   *     tem o registo de “Data de Início no Modelo de DAH” ocorrida durante o período (“Data de
   *     Início no Modelo de DAH”>= “Data Início” e <= “Data Fim”), considerar os utentes incluídos
   *     no indicador B1-Nº de utentes que iniciaram TARV nesta unidade sanitária durante o mês, do
   *     relatório “Resumo Mensal de HIV/SIDA” durante o período de compreendido entre “Data Início”
   *     menos (–) 2 meses e “Data Fim”.
   * <li>Excluindo:Todas as mulheres com registo de grávida, conforme definido no RF29 RF29:
   *     selecionando todos os utentes do sexo feminino, independentemente da idade, e registados
   *     como “Grávida=Sim” (Coluna 3) na “Ficha Clínica” e “Data de Consulta” ocorrida durante o
   *     período (>= “Data Início” – 3 meses e <= “Data Fim”).
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoAreNewInArtDisaggregation() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("utentes novos inícios de TARV, para desagregação dos indicadores 8 a 19");
    cd.addParameters(getCohortParameters());

    cd.addSearch(
        "newOnArt",
        map(
            getPatientsArtSituationOnDAH(hivMetadata.getStartDrugs()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "B1",
        map(
            getB1IfPatientDontHaveTarvSituationOnDah(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "PREGNANT",
        map(
            intensiveMonitoringCohortQueries.getMI15C(),
            "startDate=${startDate-3m},endDate=${endDate},location=${location}"));

    cd.setCompositionString("(newOnArt OR B1) AND NOT PREGNANT");
    return cd;
  }

  /**
   * <b>Relatório Desagregação - Reinícios TARV</b>
   *
   * <p>Incluindo todos os utentes
   * <li>Registados como “Reinício" no campo “Situação do TARV no início do seguimento” (Secção A)
   *     da Ficha de DAH que tem o registo de “Data de Início no Modelo de DAH” ocorrida durante o
   *     período (“Data de Início no Modelo de DAH”>= “Data Início” e <= “Data Fim”)
   * <li>Caso não exista o registo da “Situação do TARV no Início do Seguimento” na Ficha de DAH que
   *     tem o registo de “Data de Início no Modelo de DAH” ocorrida durante o período (“Data de
   *     Início no Modelo de DAH”>= “Data Início” e <= “Data Fim”), considerar os utentes incluídos
   *     no indicador B3-Nº de reinícios TARV durante o mês, do relatório “Resumo Mensal de
   *     HIV/SIDA” durante o período compreendido entre “Data Início” menos (–) 2 meses e “Data Fim”
   * <li>Excluindo:Todas as mulheres com registo de grávida, conforme definido no RF29 RF29:
   *     selecionando todos os utentes do sexo feminino, independentemente da idade, e registados
   *     como “Grávida=Sim” (Coluna 3) na “Ficha Clínica” e “Data de Consulta” ocorrida durante o
   *     período (>= “Data Início” – 3 meses e <= “Data Fim”).
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoRestartedArtDisaggregation() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("utentes Reinícios TARV, para desagregação dos indicadores 8 a 19");
    cd.addParameters(getCohortParameters());

    cd.addSearch(
        "restartedArt",
        map(
            getPatientsArtSituationOnDAH(hivMetadata.getRestartConcept()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "B3P1",
        map(
            resumoMensalCohortQueries.getPatientsRestartedTarvtB3(),
            "startDate=${startDate-2m},endDate=${endDate-2m},location=${location}"));

    cd.addSearch(
        "B3P2",
        map(
            resumoMensalCohortQueries.getPatientsRestartedTarvtB3(),
            "startDate=${startDate-1m},endDate=${endDate-1m},location=${location}"));

    cd.addSearch(
        "B3P3",
        map(
            resumoMensalCohortQueries.getPatientsRestartedTarvtB3(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "PREGNANT",
        map(
            intensiveMonitoringCohortQueries.getMI15C(),
            "startDate=${startDate-3m},endDate=${endDate},location=${location}"));

    cd.addSearch("newArt", mapStraightThrough(getPatientsWhoAreNewInArtDisaggregation()));

    cd.setCompositionString(
        "((restartedArt OR B3P1 OR B3P2 OR B3P3) AND NOT PREGNANT) AND NOT newArt");
    return cd;
  }

  /**
   * <b>Relatório Desagregação - Activos TARV</b>
   *
   * <p>Incluindo todos os utentes
   * <li>Registados como “Em TARV" no campo “Situação do TARV no início do seguimento” (Secção A) da
   *     Ficha de DAH que tem o registo de “Data de Início no Modelo de DAH” ocorrida durante o
   *     período (“Data de Início no Modelo de DAH”>= “Data Início” e <= “Data Fim”)
   * <li>Caso não exista o registo da “Situação do TARV no Início do Seguimento” na Ficha de DAH que
   *     tem o registo de “Data de Início no Modelo de DAH” ocorrida durante o período (“Data de
   *     Início no Modelo de DAH”>= “Data Início” e <= “Data Fim”), considerar os utentes incluídos
   *     no indicador B12 - Nº de utentes activos em TARV até o fim do mês anterior, do relatório
   *     “Resumo Mensal de HIV/SIDA”
   * <li>Excluindo:Todas as mulheres com registo de grávida, conforme definido no RF29 RF29:
   *     selecionando todos os utentes do sexo feminino, independentemente da idade, e registados
   *     como “Grávida=Sim” (Coluna 3) na “Ficha Clínica” e “Data de Consulta” ocorrida durante o
   *     período (>= “Data Início” – 3 meses e <= “Data Fim”).
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoAreInTarvDisaggregation() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("utentes Activos em TARV, para desagregação dos indicadores 8 a 19");
    cd.addParameters(getCohortParameters());

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

    cd.addSearch(
        "PREGNANT",
        map(
            intensiveMonitoringCohortQueries.getMI15C(),
            "startDate=${startDate-3m},endDate=${endDate},location=${location}"));

    cd.addSearch("restarted", mapStraightThrough(getPatientsWhoRestartedArtDisaggregation()));

    cd.addSearch("newArt", mapStraightThrough(getPatientsWhoAreNewInArtDisaggregation()));

    cd.setCompositionString("((onArt OR B12) AND NOT PREGNANT) AND NOT (restarted OR newArt)");
    return cd;
  }

  /**
   * <li>Caso não exista o registo da “Situação do TARV no Início do Seguimento” na Ficha de DAH que
   *     tem o registo de “Data de Início no Modelo de DAH” ocorrida durante o período (“Data de
   *     Início no Modelo de DAH”>= “Data Início” e <= “Data Fim”), considerar os utentes incluídos
   *     no indicador B1-Nº de utentes que iniciaram TARV nesta unidade sanitária durante o mês, do
   *     relatório “Resumo Mensal de HIV/SIDA” durante o período de compreendido entre “Data Início”
   *     menos (–) 2 meses e “Data Fim”.
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getB1IfPatientDontHaveTarvSituationOnDah() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Patients Who dont have Situacao de Tarv On Ficha de DAH and Are from B1 Resumo mensal");
    cd.addParameters(getCohortParameters());

    cd.addSearch(
        "B1",
        map(
            resumoMensalCohortQueries
                .getPatientsWhoInitiatedTarvAtThisFacilityDuringCurrentMonthB1(),
            "startDate=${startDate-2m},endDate=${endDate},location=${location}"));

    cd.addSearch("noTarvSituation", mapStraightThrough(getPatientsWhoDontArtSituationOnDAH()));

    cd.setCompositionString("B1 AND noTarvSituation");
    return cd;
  }

  /**
   * @param cd4 Amount of Cd4 for each range of age
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getCd4Result(Cd4CountComparison cd4) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Resultado de CD4");
    sqlCohortDefinition.addParameters(getCohortParameters());

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("90", hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());

    String query =
        "  SELECT ps.person_id FROM   person ps "
            + "       INNER JOIN encounter e "
            + "               ON ps.person_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + "WHERE  ps.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND o.concept_id = ${1695}  "
            + "       AND ".concat(cd4.getProposition())
            + " AND ( "
            + "  ( e.encounter_type = ${90} "
            + " AND o.obs_datetime >= :startDate "
            + "  AND o.obs_datetime <= :endDate)"
            + "OR "
            + " ( e.encounter_type = ${6} "
            + " AND e.encounter_datetime >= :startDate "
            + "  AND e.encounter_datetime <= :endDate) "
            + " ) "
            + "  AND e.location_id = :location"
            + "  GROUP BY ps.person_id ";

    StringSubstitutor substitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(substitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * Get Patients who do not have
   * “Situação do TARV no início do seguimento” (Secção A) da
   * Ficha de DAH que tem o registo de “Data de Início no Modelo de DAH” ocorrida durante
   * o período (“Data de Início no Modelo de DAH”>= “Data Início” e <= “Data Fim”)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoDontArtSituationOnDAH() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients who do not have Situacao de Tarv Marked on DAH”");
    sqlCohortDefinition.addParameters(getCohortParameters());

    Map<String, Integer> map = new HashMap<>();
    map.put("90", hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId());
    map.put("1255", hivMetadata.getARVPlanConcept().getConceptId());

    String query =
        "SELECT pp.patient_id  "
            + "FROM  "
            + "    patient pp INNER JOIN encounter e ON pp.patient_id = e.patient_id  "
            + "               INNER JOIN (  "
            + "        SELECT p.patient_id, MAX(e.encounter_datetime) as last_date  "
            + "        FROM  "
            + "            patient p INNER JOIN encounter e ON p.patient_id= e.patient_id  "
            + "                      INNER JOIN obs o on e.encounter_id = o.encounter_id  "
            + "        WHERE p.voided = 0 AND e.voided = 0 AND o.voided = 0  "
            + "          AND e.encounter_type = ${90}   "
            + "          AND e.encounter_datetime >= :startDate  "
            + "          AND e.encounter_datetime <= :endDate  "
            + "          AND e.location_id = :location  "
            + "        GROUP BY p.patient_id  "
            + "    ) last_dah ON last_dah.patient_id = pp.patient_id  "
            + "WHERE pp.voided = 0  "
            + "  AND e.voided = 0  "
            + "  AND e.encounter_type = ${90}  "
            + "  AND e.encounter_datetime = last_dah.last_date  "
            + "  AND e.location_id = :location  "
            + "  AND pp.patient_id NOT IN (  "
            + "    SELECT p.patient_id  "
            + "    FROM  "
            + "        patient p INNER JOIN encounter e ON p.patient_id = e.patient_id  "
            + "                  INNER JOIN obs o on e.encounter_id = o.encounter_id  "
            + "                  INNER JOIN (  "
            + "            SELECT p.patient_id, MAX(e.encounter_datetime) as last_date  "
            + "            FROM  "
            + "                patient p INNER JOIN encounter e ON p.patient_id= e.patient_id  "
            + "                          INNER JOIN obs o on e.encounter_id = o.encounter_id  "
            + "            WHERE p.voided = 0 AND e.voided = 0 AND o.voided = 0  "
            + "              AND e.encounter_type = ${90}  "
            + "              AND e.encounter_datetime >= :startDate  "
            + "              AND e.encounter_datetime <= :endDate  "
            + "              AND e.location_id = :location  "
            + "            GROUP BY p.patient_id  "
            + "        ) last_dah ON last_dah.patient_id = p.patient_id  "
            + "    WHERE p.voided = 0  "
            + "      AND e.voided = 0  "
            + "      AND o.voided = 0  "
            + "      AND p.patient_id = pp.patient_id  "
            + "      AND e.encounter_type = ${90}  "
            + "      AND o.concept_id = ${1255}  "
            + "      AND o.value_coded IS NOT NULL  "
            + "      AND e.encounter_datetime = last_dah.last_date  "
            + "      AND e.location_id = :location  "
            + "    GROUP BY p.patient_id  "
            + ")  "
            + "GROUP BY pp.patient_id ";

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
