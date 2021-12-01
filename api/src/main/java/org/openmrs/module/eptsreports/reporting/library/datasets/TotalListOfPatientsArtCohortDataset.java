package org.openmrs.module.eptsreports.reporting.library.datasets;

import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.springframework.stereotype.Component;

@Component
public class TotalListOfPatientsArtCohortDataset extends BaseDataSet {

  public DataSetDefinition contructDataset() {
    CohortIndicatorDataSetDefinition dataSetDefinition = new CohortIndicatorDataSetDefinition();
    dataSetDefinition.setName("ART Cohort Total");
    dataSetDefinition.addParameters(getParameters());

    return dataSetDefinition;
  }
}
