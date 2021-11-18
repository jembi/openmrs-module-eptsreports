package org.openmrs.module.eptsreports.reporting.library.datasets;

import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.springframework.stereotype.Component;

@Component
public class TxPvlsBySourceDataset extends BaseDataSet {

  public DataSetDefinition getPvlsLabFsr() {
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";

    CohortIndicatorDataSetDefinition dataSetDefinition = new CohortIndicatorDataSetDefinition();
    dataSetDefinition.setName("PLF");
    dataSetDefinition.setDescription("PVLS_LAB_FSR");
    dataSetDefinition.addParameters(getParameters());
    return dataSetDefinition;
  }

  public DataSetDefinition getPvlFichaMestre() {
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";

    CohortIndicatorDataSetDefinition dataSetDefinition = new CohortIndicatorDataSetDefinition();
    dataSetDefinition.setName("PFM");
    dataSetDefinition.setDescription("PVLS_Ficha_Mestre");
    dataSetDefinition.addParameters(getParameters());
    return dataSetDefinition;
  }
}
