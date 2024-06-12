package org.openmrs.module.eptsreports.reporting.library.datasets.list;

import org.openmrs.module.eptsreports.reporting.library.datasets.BaseDataSet;
import org.openmrs.module.reporting.data.person.definition.PersonIdDataDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.springframework.stereotype.Component;

@Component
public class ListOfChildrenEnrolledInCCRDataset extends BaseDataSet {

  public DataSetDefinition listOfChildrenEnrolledInCCRColumnsDataset() {
    PatientDataSetDefinition patientDataSetDefinition = new PatientDataSetDefinition();

    patientDataSetDefinition.setName("List of Children Enrolled in CCR Columns DataSet");
    patientDataSetDefinition.setParameters(getParameters());

    patientDataSetDefinition.addColumn("id", new PersonIdDataDefinition(), "");

    return patientDataSetDefinition;
  }

  public DataSetDefinition listOfChildrenEnrolledInCCRTotalsDataset() {

    CohortIndicatorDataSetDefinition dataSetDefinition = new CohortIndicatorDataSetDefinition();
    dataSetDefinition.setName("List of Children Enrolled in CCR Totals DataSet");

    dataSetDefinition.addParameters(getParameters());

    return dataSetDefinition;
  }
}
