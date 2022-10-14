package org.openmrs.module.eptsreports.reporting.library.datasets.resumo;

import org.openmrs.module.eptsreports.reporting.library.datasets.BaseDataSet;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.SimpleIndicatorDataSetDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NonCohortIndicatorsDatasetDefinition extends BaseDataSet {

  private final EptsGeneralIndicator eptsGeneralIndicator;

  @Autowired
  public NonCohortIndicatorsDatasetDefinition(EptsGeneralIndicator eptsGeneralIndicator) {
    this.eptsGeneralIndicator = eptsGeneralIndicator;
  }

  public DataSetDefinition constructNonCohortDataset() {
    SimpleIndicatorDataSetDefinition nonCohortDsd = new SimpleIndicatorDataSetDefinition();
    nonCohortDsd.setName("Non-cohort DSD");
    nonCohortDsd.addParameters(getParameters());
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";
    nonCohortDsd.addColumn(
            "NON",
            "Total patients encounter",
            EptsReportUtils.map(
                    eptsGeneralIndicator.nonCohortIndicators(),
                    mappings));
    return nonCohortDsd;
  }
}
