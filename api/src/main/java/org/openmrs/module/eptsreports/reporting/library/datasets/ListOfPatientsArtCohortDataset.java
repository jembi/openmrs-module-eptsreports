package org.openmrs.module.eptsreports.reporting.library.datasets;

import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsArtCohortDataset extends BaseDataSet {

  public DataSetDefinition contructDataset() {

    PatientDataSetDefinition pdd = new PatientDataSetDefinition();

    pdd.setName("ART Cohort List");
    return pdd;
  }
}
