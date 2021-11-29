package org.openmrs.module.eptsreports.reporting.library.datasets;

import org.openmrs.module.eptsreports.reporting.library.cohorts.TxPvlsBySourceClinicalOrFichaResumoCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.TxPvlsBySourceLabOrFsrCohortQueries;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TxPvlsBySourceDataset extends BaseDataSet {

  private TxPvlsBySourceLabOrFsrCohortQueries txPvlsBySourceLabOrFsrCohortQueries;

  private TxPvlsBySourceClinicalOrFichaResumoCohortQueries
      txPvlsBySourceClinicalOrFichaResumoCohortQueries;

  @Autowired
  public TxPvlsBySourceDataset(
      TxPvlsBySourceLabOrFsrCohortQueries txPvlsBySourceLabOrFsrCohortQueries,
      TxPvlsBySourceClinicalOrFichaResumoCohortQueries
          txPvlsBySourceClinicalOrFichaResumoCohortQueries) {
    this.txPvlsBySourceLabOrFsrCohortQueries = txPvlsBySourceLabOrFsrCohortQueries;
    this.txPvlsBySourceClinicalOrFichaResumoCohortQueries =
        txPvlsBySourceClinicalOrFichaResumoCohortQueries;
  }

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
