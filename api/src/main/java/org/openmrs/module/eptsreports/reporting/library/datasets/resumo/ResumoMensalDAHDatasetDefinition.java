package org.openmrs.module.eptsreports.reporting.library.datasets.resumo;

import static org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils.map;
import static org.openmrs.module.reporting.evaluation.parameter.Mapped.mapStraightThrough;

import org.openmrs.module.eptsreports.reporting.library.cohorts.advancedhivillness.ListOfPatientsInAdvancedHivIllnessCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.advancedhivillness.ResumoMensalDAHCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.datasets.BaseDataSet;
import org.openmrs.module.eptsreports.reporting.library.dimensions.AgeDimensionCohortInterface;
import org.openmrs.module.eptsreports.reporting.library.dimensions.EptsCommonDimension;
import org.openmrs.module.eptsreports.reporting.library.disaggregations.ResumoMensalDAHDisaggregations;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ResumoMensalDAHDatasetDefinition extends BaseDataSet {

  private final EptsGeneralIndicator eptsGeneralIndicator;
  private final EptsCommonDimension eptsCommonDimension;
  private final ResumoMensalDAHCohortQueries resumoMensalDAHCohortQueries;
  private final ResumoMensalDAHDisaggregations resumoMensalDAHDisaggregations;
  private final ListOfPatientsInAdvancedHivIllnessCohortQueries
      listOfPatientsInAdvancedHivIllnessCohortQueries;

  @Autowired
  @Qualifier("commonAgeDimensionCohort")
  private AgeDimensionCohortInterface ageDimensionCohort;

  @Autowired
  public ResumoMensalDAHDatasetDefinition(
      EptsGeneralIndicator eptsGeneralIndicator,
      EptsCommonDimension eptsCommonDimension,
      ResumoMensalDAHCohortQueries resumoMensalDAHCohortQueries,
      ResumoMensalDAHDisaggregations resumoMensalDAHDisaggregations,
      ListOfPatientsInAdvancedHivIllnessCohortQueries
          listOfPatientsInAdvancedHivIllnessCohortQueries) {
    this.eptsGeneralIndicator = eptsGeneralIndicator;
    this.eptsCommonDimension = eptsCommonDimension;
    this.resumoMensalDAHCohortQueries = resumoMensalDAHCohortQueries;
    this.resumoMensalDAHDisaggregations = resumoMensalDAHDisaggregations;
    this.listOfPatientsInAdvancedHivIllnessCohortQueries =
        listOfPatientsInAdvancedHivIllnessCohortQueries;
  }

  public DataSetDefinition constructResumoMensalDAHDataset() {

    CohortIndicatorDataSetDefinition dd = new CohortIndicatorDataSetDefinition();

    dd.setName("Dataset Resumo Mensal de DAH");
    dd.addParameters(getParameters());

    // DIMENSIONS
    dd.addDimension("gender", map(eptsCommonDimension.gender(), ""));
    dd.addDimension(
        "age", map(eptsCommonDimension.age(ageDimensionCohort), "effectiveDate=${endDate}"));

    // INDICATOR 0
    dd.addColumn(
        "TOTALI0",
        "Indicador 0: Número total de activos em DAH em TARV,  até ao fim do mês anterior",
        getPatientsWhoStartedFollowupOnDAH(),
        "");

    addRow(
        dd,
        "I0",
        "Número total de activos em DAH em TARV,  até ao fim do mês anterior",
        getPatientsWhoStartedFollowupOnDAH(),
        resumoMensalDAHDisaggregations.getColumnDisaggregations());

    // INDICATOR 1
    dd.addColumn(
        "TOTALI1",
        "Indicador 1 - Inícios TARV e Início DAH",
        getPatientsWhoAreNewInArtAndStartedFollowupDuringTheMonth(),
        "");

    addRow(
        dd,
        "I1",
        "Indicador 1 - Inícios TARV e Início DAH",
        getPatientsWhoAreNewInArtAndStartedFollowupDuringTheMonth(),
        resumoMensalDAHDisaggregations.getColumnDisaggregations());

    // INDICATOR 2
    dd.addColumn(
        "TOTALI2",
        "Indicador 2 - Reinícios TARV e Início DAH",
        getPatientsWhoRestartedArtAndStartedFollowupDuringTheMonth(),
        "");

    addRow(
        dd,
        "I2",
        "Indicador 2 - Reinícios TARV e Início DAH",
        getPatientsWhoRestartedArtAndStartedFollowupDuringTheMonth(),
        resumoMensalDAHDisaggregations.getColumnDisaggregations());

    // INDICATOR 3
    dd.addColumn(
        "TOTALI3",
        "Indicador 3 – Activos em TARV e Início DAH",
        getPatientsWhoAreInTarvAndStartedFollowupDuringTheMonth(),
        "");

    addRow(
        dd,
        "I3",
        "Indicador 3 – Activos em TARV e Início DAH",
        getPatientsWhoAreInTarvAndStartedFollowupDuringTheMonth(),
        resumoMensalDAHDisaggregations.getColumnDisaggregations());

    // INDICATOR 4
    dd.addColumn(
        "TOTALI4",
        "Indicador 4 – Saídas do seguimento de DAH",
        getPatientsWhoLeftFollowupOnDAHByDuringMonth(),
        "");

    addRow(
        dd,
        "I4",
        "Indicador 4 – Saídas do seguimento de DAH",
        getPatientsWhoLeftFollowupOnDAHByDuringMonth(),
        resumoMensalDAHDisaggregations.getColumnDisaggregations());

    // INDICATOR 5 IS A DISAGREGATIONCALCULATION
    // PERFORMED DIRECTLY ON THE TEMPLATE

    // INDICATOR 6
    dd.addColumn(
        "TOTALI6",
        "Indicador 6 –Óbitos na Coorte de 6 meses",
        getPatientsWhoAreMarkedAsDeadDOnSixMonthsCohort(),
        "");

    addRow(
        dd,
        "I6",
        "Indicador 6 –Óbitos na Coorte de 6 meses",
        getPatientsWhoAreMarkedAsDeadDOnSixMonthsCohort(),
        resumoMensalDAHDisaggregations.getColumnDisaggregations());

    // INDICATOR 7
    dd.addColumn(
        "TOTALI7",
        "Indicador 7 - Novos Inscritos DAH na coorte de 6 meses",
        getPatientsWhoAreNewOnArtOnSixMonthsCohort(),
        "");

    addRow(
        dd,
        "I7",
        "Indicador 7 - Novos Inscritos DAH na coorte de 6 meses",
        getPatientsWhoAreNewOnArtOnSixMonthsCohort(),
        resumoMensalDAHDisaggregations.getColumnDisaggregations());

    // INDICATOR 8
    dd.addColumn("TOTALI8", "Indicador 8 – Pedido de CD4", getPatientsWhoHaveCd4Request(), "");

    addRow(
        dd,
        "I8",
        "Indicador 8 – Pedido de CD4",
        getPatientsWhoHaveCd4Request(),
        resumoMensalDAHDisaggregations.getColumnDisaggregations()); // TO BE DEFINED

    // INDICATOR 9
    dd.addColumn("TOTALI9", "Indicador 9 – Resultado de CD4", getPatientsWhoHaveCd4Results(), "");

    addRow(
        dd,
        "I9",
        "Indicador 9 – Resultado de CD4",
        getPatientsWhoHaveCd4Results(),
        resumoMensalDAHDisaggregations.getColumnDisaggregations() // TO BE DEFINED
        );

    // INDICATOR 10
    dd.addColumn(
        "TOTALI10", "Indicador 10 - Resultado de CD4 baixo", getPatientsWithLowCd4Results(), "");

    addRow(
        dd,
        "I10",
        "Indicador 10 - Resultado de CD4 baixo",
        getPatientsWithLowCd4Results(),
        resumoMensalDAHDisaggregations.getColumnDisaggregations() // TO BE DEFINED
        );

    // INDICATOR 11
    dd.addColumn("TOTALI11", "Indicador 11 Resultado TB LAM", getPatientsWithLowTBLAMResults(), "");

    addRow(
        dd,
        "I11",
        "Indicador 11 Resultado TB LAM",
        getPatientsWithLowTBLAMResults(),
        resumoMensalDAHDisaggregations.getColumnDisaggregations() // TO BE DEFINED
        );

    // INDICATOR 12
    dd.addColumn(
        "TOTALI12",
        "Indicador 12 Resultado de TB LAM Positivo",
        getPatientsWithPositiveTBLAMResults(),
        "");

    addRow(
        dd,
        "I12",
        "Indicador 12 Resultado de TB LAM Positivo",
        getPatientsWithPositiveTBLAMResults(),
        resumoMensalDAHDisaggregations.getColumnDisaggregations() // TO BE DEFINED
        );

    // INDICATOR 13
    dd.addColumn(
        "TOTALI13",
        "Indicador 13 CD4 Baixo e Resultado de CrAg Sérico",
        getPatientsWithLowCd4AndCragResults(),
        "");

    addRow(
        dd,
        "I13",
        "Indicador 13 CD4 Baixo e Resultado de CrAg Sérico",
        getPatientsWithLowCd4AndCragResults(),
        resumoMensalDAHDisaggregations.getColumnDisaggregations() // TO BE DEFINED
        );

    // INDICATOR 14
    dd.addColumn(
        "TOTALI14",
        "Indicador 14 CD4 Baixo e Resultado de CrAg Sérico Positivo",
        getPatientsWithLowCd4AndPositiveCragResults(),
        "");

    addRow(
        dd,
        "I14",
        "Indicador 14 CD4 Baixo e Resultado de CrAg Sérico Positivo",
        getPatientsWithLowCd4AndPositiveCragResults(),
        resumoMensalDAHDisaggregations.getColumnDisaggregations() // TO BE DEFINED
        );

    // INDICATOR 15
    dd.addColumn(
        "TOTALI15",
        "Indicador 15 Utentes CrAg sérico Positivo e registo de CrAg no LCR",
        getPatientsWithPositiveOrNegativeOnCragLCRResults(),
        "");

    addRow(
        dd,
        "I15",
        "Indicador 15 Utentes CrAg sérico Positivo e registo de CrAg no LCR",
        getPatientsWithPositiveOrNegativeOnCragLCRResults(),
        resumoMensalDAHDisaggregations.getColumnDisaggregations() // TO BE DEFINED
        );

    return dd;
  }

  private Mapped<CohortIndicator> getPatientsWhoStartedFollowupOnDAH() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Indicador 0: Número total de activos em DAH em TARV,  até ao fim do mês anterior",
            mapStraightThrough(
                resumoMensalDAHCohortQueries.getPatientsWhoStartedFollowupOnDAHComposition())));
  }

  private Mapped<CohortIndicator> getPatientsWhoAreNewInArtAndStartedFollowupDuringTheMonth() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Relatórios: Indicador 1 - Inícios TARV e Início DAH",
            mapStraightThrough(
                resumoMensalDAHCohortQueries
                    .getPatientsWhoAreNewInArtAndStartedFollowupDuringTheMonthComposition())));
  }

  private Mapped<CohortIndicator> getPatientsWhoRestartedArtAndStartedFollowupDuringTheMonth() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Relatório-Indicador 2 - Reinícios TARV e Início DAH",
            mapStraightThrough(
                resumoMensalDAHCohortQueries
                    .getPatientsWhoRestartedArtAndStartedFollowupDuringTheMonthComposition())));
  }

  private Mapped<CohortIndicator> getPatientsWhoAreInTarvAndStartedFollowupDuringTheMonth() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Relatório-Indicador 3 – Activos em TARV e Início DAH",
            mapStraightThrough(
                resumoMensalDAHCohortQueries
                    .getPatientsWhoAreInTarvAndStartedFollowupDuringTheMonthComposition())));
  }

  private Mapped<CohortIndicator> getPatientsWhoLeftFollowupOnDAHByDuringMonth() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Relatório – Indicador 4 – Saídas do seguimento de DAH",
            mapStraightThrough(
                resumoMensalDAHCohortQueries.getPatientsWhoLeftFollowupOnDAHByDuringMonth())));
  }

  private Mapped<CohortIndicator> getPatientsWhoAreMarkedAsDeadDOnSixMonthsCohort() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Relatório- Indicador 6 –Óbitos na Coorte de 6 meses",
            mapStraightThrough(
                resumoMensalDAHCohortQueries
                    .getPatientsWhoAreMarkedAsDeadDOnSixMonthsCohortComposition())));
  }

  private Mapped<CohortIndicator> getPatientsWhoAreNewOnArtOnSixMonthsCohort() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Relatório- Indicador 7 - Novos Inscritos DAH na coorte de 6 meses",
            map(
                listOfPatientsInAdvancedHivIllnessCohortQueries.getPatientsWhoStartedFollowupOnDAH(
                    true),
                "startDate=${startDate-7m},endDate=${startDate-6m-1d},location=${location}")));
  }

  private Mapped<CohortIndicator> getPatientsWhoHaveCd4Request() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Relatório- Indicador 8 – Pedido de CD4",
            mapStraightThrough(resumoMensalDAHCohortQueries.getPatientsWhoHaveCd4Request())));
  }

  private Mapped<CohortIndicator> getPatientsWhoHaveCd4Results() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Relatório- Indicador 9 – Resultado de CD4",
            mapStraightThrough(resumoMensalDAHCohortQueries.getPatientsWhoHaveCd4Results())));
  }

  private Mapped<CohortIndicator> getPatientsWithLowCd4Results() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Relatório – Indicador 10 Resultado de CD4 baixo",
            mapStraightThrough(resumoMensalDAHCohortQueries.getPatientsWithLowCd4Results())));
  }

  private Mapped<CohortIndicator> getPatientsWithLowTBLAMResults() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Relatório – Indicador 11 Resultado TB LAM",
            mapStraightThrough(resumoMensalDAHCohortQueries.getPatientsWithTBLAMResults())));
  }

  private Mapped<CohortIndicator> getPatientsWithPositiveTBLAMResults() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Relatório – Indicador 12 Resultado de TB LAM Positivo",
            mapStraightThrough(
                resumoMensalDAHCohortQueries.getPatientsWithPositiveTBLAMResults())));
  }

  private Mapped<CohortIndicator> getPatientsWithLowCd4AndCragResults() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Relatório – Indicador 13 CD4 Baixo e Resultado de CrAg Sérico",
            mapStraightThrough(
                resumoMensalDAHCohortQueries.getPatientsWithLowCd4AndCragResults())));
  }

  private Mapped<CohortIndicator> getPatientsWithLowCd4AndPositiveCragResults() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Relatório – Indicador 14 CD4 Baixo e Resultado de CrAg Sérico Positivo",
            mapStraightThrough(
                resumoMensalDAHCohortQueries.getPatientsWithLowCd4AndPositiveCragResults())));
  }

  private Mapped<CohortIndicator> getPatientsWithPositiveOrNegativeOnCragLCRResults() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Indicador 15 Utentes CrAg sérico Positivo e registo de CrAg no LCR",
            mapStraightThrough(
                resumoMensalDAHCohortQueries.getPatientsWithPositiveOrNegativeOnCragLCRResults())));
  }
}
