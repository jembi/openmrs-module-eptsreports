package org.openmrs.module.eptsreports.reporting.library.datasets;

import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ListOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ListOfPatientsCurrentlyOnArtWithoutTbScreeningDataset extends BaseDataSet {

  private ListOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries
      listOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries;

  @Autowired
  public ListOfPatientsCurrentlyOnArtWithoutTbScreeningDataset(
      ListOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries
          listOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries) {

    this.listOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries =
        listOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries;
  }

  public DataSetDefinition constructListOfPatientsDataset() {
    PatientDataSetDefinition patientDefinition = new PatientDataSetDefinition();

    patientDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    patientDefinition.addParameter(new Parameter("location", "Location", Location.class));

    return patientDefinition;
  }
}
