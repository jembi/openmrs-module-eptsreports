package org.openmrs.module.eptsreports.reporting.library.datasets;

import org.openmrs.module.eptsreports.reporting.library.cohorts.IntensiveMonitoringCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.TXTBCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.TxCurrCohortQueries;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TxTbMonthlyCascadeDataset extends BaseDataSet {
  @Autowired private IntensiveMonitoringCohortQueries intensiveMonitoringCohortQueries;

  @Autowired private TxCurrCohortQueries txCurrCohortQueries;

  @Autowired private TXTBCohortQueries txtbCohortQueries;

  public DataSetDefinition constructTXTBMonthlyDataset() {

    CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();

    dsd.setName("TX_TB Monthly Cascade Dataset");

    return dsd;
  }
}
