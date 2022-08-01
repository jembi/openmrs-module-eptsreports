package org.openmrs.module.eptsreports.reporting.library.datasets;

import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.springframework.stereotype.Component;

@Component
public class ListChildrenAdolescentARTWithoutFullDisclosureDataset extends BaseDataSet {

  public DataSetDefinition constructListChildrenAdolescentARTWithoutFullDisclosureDataset() {

    PatientDataSetDefinition pdsd = new PatientDataSetDefinition();

    return pdsd;
  }
}
