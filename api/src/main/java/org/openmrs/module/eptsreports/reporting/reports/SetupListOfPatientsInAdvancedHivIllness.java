package org.openmrs.module.eptsreports.reporting.reports;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.library.datasets.DatimCodeDatasetDefinition;
import org.openmrs.module.eptsreports.reporting.library.datasets.ListOfPatientsInAdvancedHivIllnessDataset;
import org.openmrs.module.eptsreports.reporting.library.datasets.SismaCodeDatasetDefinition;
import org.openmrs.module.eptsreports.reporting.reports.manager.EptsDataExportManager;
import org.openmrs.module.reporting.ReportingException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;

@Deprecated
public class SetupListOfPatientsInAdvancedHivIllness extends EptsDataExportManager {

  private ListOfPatientsInAdvancedHivIllnessDataset listOfPatientsInAdvancedHivIllnessDataset;

  @Autowired
  public SetupListOfPatientsInAdvancedHivIllness(
      ListOfPatientsInAdvancedHivIllnessDataset listOfPatientsInAdvancedHivIllnessDataset) {
    this.listOfPatientsInAdvancedHivIllnessDataset = listOfPatientsInAdvancedHivIllnessDataset;
  }

  @Override
  public String getExcelDesignUuid() {
    return "e2c8f0de-254c-11ee-a1ea-efacb3b24cbb";
  }

  @Override
  public String getUuid() {
    return "ecbbb4dc-254c-11ee-831a-b73756804347";
  }

  @Override
  public String getName() {
    return "Lista de Utentes em Doença Avançada por HIV (DAH)";
  }

  @Override
  public String getDescription() {
    return "Este relatório gera o número agregado e a lista de utentes em Doença Avançada por HIV";
  }

  @Override
  public String getVersion() {
    return "1.0-SNAPSHOT";
  }

  @Override
  public ReportDefinition constructReportDefinition() {
    ReportDefinition reportDefinition = new ReportDefinition();

    reportDefinition.setName(getName());
    reportDefinition.setUuid(getUuid());
    reportDefinition.setDescription(getDescription());
    reportDefinition.addParameters(getParameters());

    // Datim Codes
    reportDefinition.addDataSetDefinition(
        "DT", Mapped.mapStraightThrough(new DatimCodeDatasetDefinition()));
    reportDefinition.addDataSetDefinition(
        "SM", Mapped.mapStraightThrough(new SismaCodeDatasetDefinition()));

    // Cohort datasets
    reportDefinition.addDataSetDefinition(
        "DAH",
        Mapped.mapStraightThrough(
            listOfPatientsInAdvancedHivIllnessDataset
                .listOfPatientsInAdvancedHivIllnessColumnsDataset()));

    reportDefinition.addDataSetDefinition(
        "TOTAL",
        Mapped.mapStraightThrough(
            listOfPatientsInAdvancedHivIllnessDataset
                .listOfPatientsInAdvancedHivIllnessTotalsDataset()));

    return reportDefinition;
  }

  @Override
  public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {

    ReportDesign reportDesign = null;

    try {
      reportDesign =
          createXlsReportDesign(
              reportDefinition,
              "Template_Lista_Utentes_DAH_v1.6.xls",
              "Lista de Utentes em Doença Avançada por HIV (DAH)",
              getExcelDesignUuid(),
              null);
      Properties props = new Properties();
      props.put("repeatingSections", "sheet:1,row:10,dataset:DAH");
      props.put("sortWeight", "5000");
      reportDesign.setProperties(props);
    } catch (IOException e) {
      throw new ReportingException(e.toString());
    }

    return Arrays.asList(reportDesign);
  }

  @Override
  public List<Parameter> getParameters() {
    return Arrays.asList(
        new Parameter("startDate", "Data de Inicio", Date.class),
        new Parameter("endDate", "Data de Fim", Date.class),
        new Parameter("location", "Location", Location.class));
  }
}
