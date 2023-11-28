package org.openmrs.module.eptsreports.reporting.library.datasets.resumo;

import static org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils.map;
import static org.openmrs.module.reporting.evaluation.parameter.Mapped.mapStraightThrough;

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

  @Autowired
  @Qualifier("commonAgeDimensionCohort")
  private AgeDimensionCohortInterface ageDimensionCohort;

  @Autowired
  public ResumoMensalDAHDatasetDefinition(
      EptsGeneralIndicator eptsGeneralIndicator,
      EptsCommonDimension eptsCommonDimension,
      ResumoMensalDAHCohortQueries resumoMensalDAHCohortQueries,
      ResumoMensalDAHDisaggregations resumoMensalDAHDisaggregations) {
    this.eptsGeneralIndicator = eptsGeneralIndicator;
    this.eptsCommonDimension = eptsCommonDimension;
    this.resumoMensalDAHCohortQueries = resumoMensalDAHCohortQueries;
    this.resumoMensalDAHDisaggregations = resumoMensalDAHDisaggregations;
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

    //INDICATOR 5 IS A DISAGREGATIONCALCULATION
    // PERFORMED DIRECTLY ON THE TEMPLATE

    //INDICATOR 6
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
                resumoMensalDAHCohortQueries.getPatientsWhoAreMarkedAsDeadDOnSixMonthsCohortComposition())));
  }
}
