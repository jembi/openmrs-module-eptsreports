package org.openmrs.module.eptsreports.reporting.library.datasets;

import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.springframework.stereotype.Component;

@Component
public class TotalChildrenAdolescentARTWithoutFullDisclosureDataset extends BaseDataSet {

  public DataSetDefinition constructTotalChildrenAdolescentARTWithoutFullDisclosureDataset() {
    CohortIndicatorDataSetDefinition dataSetDefinition = new CohortIndicatorDataSetDefinition();
    return dataSetDefinition;
  }
}
