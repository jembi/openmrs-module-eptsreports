package org.openmrs.module.eptsreports.reporting.library.datasets;

import org.openmrs.module.eptsreports.reporting.library.cohorts.AdvancedDiseaseAndTBCascadeCohortQueries;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AdvancedDiseaseAndTbCascadeDataset extends BaseDataSet {

  @Autowired
  private AdvancedDiseaseAndTBCascadeCohortQueries advancedDiseaseAndTBCascadeCohortQueries;

  public DataSetDefinition constructAdvancedDiseaseAndTbCascadeDataset() {

    CohortIndicatorDataSetDefinition dataSetDefinition = new CohortIndicatorDataSetDefinition();
    dataSetDefinition.setName("TB7 Dataset");

    return dataSetDefinition;
  }
}
