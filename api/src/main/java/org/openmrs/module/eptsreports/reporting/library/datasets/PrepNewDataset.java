package org.openmrs.module.eptsreports.reporting.library.datasets;

import org.openmrs.module.eptsreports.reporting.library.cohorts.PrepNewCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PrepNewDataset extends BaseDataSet {

  private PrepNewCohortQueries prepNewCohortQueries;

  private EptsGeneralIndicator eptsGeneralIndicator;

  @Autowired
  public PrepNewDataset(
      PrepNewCohortQueries prepNewCohortQueries, EptsGeneralIndicator eptsGeneralIndicator) {
    this.prepNewCohortQueries = prepNewCohortQueries;
    this.eptsGeneralIndicator = eptsGeneralIndicator;
  }

  /**
   * <b>Description:</b> Constructs PrEP New Dataset
   *
   * @return
   */
  public DataSetDefinition constructPrepNewDataset() {

    CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
    dsd.setName("PrEP New Dataset");
    dsd.addParameters(getParameters());
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";

    // start building the datasets
    // get the column for the totals
    dsd.addColumn(
        "P1",
        "Total Clients PrEP",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "Total Clients Who Newly Initiated PrEP",
                EptsReportUtils.map(
                    prepNewCohortQueries.getClientsWhoNewlyInitiatedPrep(), mappings)),
            mappings),
        "");


    return dsd;
  }
}
