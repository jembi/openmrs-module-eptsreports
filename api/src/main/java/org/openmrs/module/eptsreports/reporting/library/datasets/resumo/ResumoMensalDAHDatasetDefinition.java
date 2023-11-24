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
  private EptsCommonDimension eptsCommonDimension;
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
        "Relatórios: Indicador 1 - Inícios TARV e Início DAH",
        getPatientsWhoAreNewInArtAndStartedFollowupDuringTheMonth(),
        "");

    addRow(
        dd,
        "I1",
        "Relatórios: Indicador 1 - Inícios TARV e Início DAH",
        getPatientsWhoAreNewInArtAndStartedFollowupDuringTheMonth(),
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
}
