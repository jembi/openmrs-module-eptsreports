package org.openmrs.module.eptsreports.reporting.library.datasets;

import org.openmrs.module.eptsreports.reporting.library.cohorts.ListOfPatientsDefaultersOrIITCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TotalListOfPatientsDefaultersOrIITTemplateDataSet extends BaseDataSet {

  @Autowired ListOfPatientsDefaultersOrIITCohortQueries listOfPatientsDefaultersOrIITCohortQueries;

  @Autowired private EptsGeneralIndicator eptsGeneralIndicator;

  public DataSetDefinition constructDataSet() {

    CohortIndicatorDataSetDefinition dataSetDefinition = new CohortIndicatorDataSetDefinition();
    dataSetDefinition.setName("Lista de Pacientes Faltosos ou Abandonos ao TARV");
    dataSetDefinition.addParameters(getParameters());

    String mappings = "endDate=${endDate},location=${location},minDay=${minDay},maxDay=${maxDay}";

    CohortIndicator Total =
        eptsGeneralIndicator.getIndicator(
            "total",
            EptsReportUtils.map(
                listOfPatientsDefaultersOrIITCohortQueries.getBaseCohort(), mappings));

    dataSetDefinition.addColumn(
        "total",
        "Total de Pacientes Faltosos ou Abandonos ao TARV",
        EptsReportUtils.map(Total, mappings),
        "");

    return dataSetDefinition;
  }
}
