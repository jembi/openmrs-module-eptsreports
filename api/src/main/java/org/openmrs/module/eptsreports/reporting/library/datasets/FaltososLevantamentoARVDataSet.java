package org.openmrs.module.eptsreports.reporting.library.datasets;

import org.openmrs.module.eptsreports.reporting.library.cohorts.FaltososLevantamentoARVCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.dimensions.AgeDimensionCohortInterface;
import org.openmrs.module.eptsreports.reporting.library.dimensions.EptsCommonDimension;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FaltososLevantamentoARVDataSet extends BaseDataSet {

  private FaltososLevantamentoARVCohortQueries faltososLevantamentoARVCohortQueries;
  private EptsGeneralIndicator eptsGeneralIndicator;
  private EptsCommonDimension eptsCommonDimension;
  private AgeDimensionCohortInterface ageDimensionCohortInterface;

  private String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";

  @Autowired
  public FaltososLevantamentoARVDataSet(
      FaltososLevantamentoARVCohortQueries faltososLevantamentoARVCohortQueries,
      EptsGeneralIndicator eptsGeneralIndicator) {
    this.eptsGeneralIndicator = eptsGeneralIndicator;
    this.faltososLevantamentoARVCohortQueries = faltososLevantamentoARVCohortQueries;
  }

  public DataSetDefinition constructDataSet() {

    CohortIndicatorDataSetDefinition dataSetDefinition = new CohortIndicatorDataSetDefinition();
    dataSetDefinition.setName("FALTOSOS AO LEVANTAMENTO DE ARV");
    dataSetDefinition.addParameters(getParameters());

    /*    dataSetDefinition.addDimension(
    "age",
    EptsReportUtils.map(
        eptsCommonDimension.age(ageDimensionCohortInterface),
        "effectiveDate=${generationDate}"));*/

    // dataSetDefinition.addDimension("gender", EptsReportUtils.map(eptsCommonDimension.gender(),
    // ""));

    CohortIndicator ciFaltosoDenominator =
        eptsGeneralIndicator.getIndicator(
            "denominator",
            EptsReportUtils.map(faltososLevantamentoARVCohortQueries.getDenominator(), mappings));

    CohortIndicator ciFaltososNumerador =
        eptsGeneralIndicator.getIndicator(
            "numerator",
            EptsReportUtils.map(faltososLevantamentoARVCohortQueries.getNumerator(), mappings));

    dataSetDefinition.addColumn(
        "denominator", "DENOMINANTOR", EptsReportUtils.map(ciFaltosoDenominator, mappings), "");

    dataSetDefinition.addColumn(
        "numerator", "NUMERATOR", EptsReportUtils.map(ciFaltososNumerador, mappings), "");

    return dataSetDefinition;
  }
}
