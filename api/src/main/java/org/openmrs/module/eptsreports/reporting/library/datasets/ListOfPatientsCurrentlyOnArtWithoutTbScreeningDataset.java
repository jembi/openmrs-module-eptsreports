package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.Date;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ListOfPatientsCurrentlyOnArtWithoutTbScreenigCohortQueries;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsCurrentlyOnArtWithoutTbScreeningDataset extends BaseDataSet {

  private ListOfPatientsCurrentlyOnArtWithoutTbScreenigCohortQueries
      listOfPatientsCurrentlyOnArtWithoutTbScreenig;

  @Autowired
  public ListOfPatientsCurrentlyOnArtWithoutTbScreeningDataset(
      ListOfPatientsCurrentlyOnArtWithoutTbScreenigCohortQueries
          listOfPatientsCurrentlyOnArtWithoutTbScreenig) {

    this.listOfPatientsCurrentlyOnArtWithoutTbScreenig =
        listOfPatientsCurrentlyOnArtWithoutTbScreenig;
  }

  public DataSetDefinition constructListOfPatientsDataset() {
    PatientDataSetDefinition patientDefinition = new PatientDataSetDefinition();

    patientDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    patientDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    patientDefinition.addParameter(new Parameter("location", "Location", Location.class));

    return patientDefinition;
  }
}
