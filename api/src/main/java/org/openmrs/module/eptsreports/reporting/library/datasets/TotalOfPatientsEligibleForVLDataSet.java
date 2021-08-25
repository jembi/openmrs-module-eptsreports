package org.openmrs.module.eptsreports.reporting.library.datasets;

import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.springframework.stereotype.Component;

@Component
public class TotalOfPatientsEligibleForVLDataSet extends BaseDataSet {

  public DataSetDefinition constructDataSet() {

    CohortIndicatorDataSetDefinition dataSetDefinition = new CohortIndicatorDataSetDefinition();
    dataSetDefinition.setName("Total of patients eligible for VL report");
    dataSetDefinition.addParameters(getParameters());

    return dataSetDefinition;
  }
}
