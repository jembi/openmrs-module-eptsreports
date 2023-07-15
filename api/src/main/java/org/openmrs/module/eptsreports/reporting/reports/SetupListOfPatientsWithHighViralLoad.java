package org.openmrs.module.eptsreports.reporting.reports;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.library.datasets.DatimCodeDatasetDefinition;
import org.openmrs.module.eptsreports.reporting.library.datasets.ListOfPatientsWithHighViralLoadCohortDataset;
import org.openmrs.module.eptsreports.reporting.library.datasets.SemanaCorrenteDatasetOfHighViralLoadCohortDataset;
import org.openmrs.module.eptsreports.reporting.library.datasets.SismaCodeDatasetDefinition;
import org.openmrs.module.eptsreports.reporting.reports.manager.EptsDataExportManager;
import org.openmrs.module.reporting.ReportingException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Deprecated
public class SetupListOfPatientsWithHighViralLoad extends EptsDataExportManager {

  private SemanaCorrenteDatasetOfHighViralLoadCohortDataset
      semanaCorrenteDatasetOfHighViralLoadCohortDataset;
  private ListOfPatientsWithHighViralLoadCohortDataset listOfPatientsWithHighViralLoadCohortDataset;

  @Autowired
  public SetupListOfPatientsWithHighViralLoad(
      SemanaCorrenteDatasetOfHighViralLoadCohortDataset
          semanaCorrenteDatasetOfHighViralLoadCohortDataset,
      ListOfPatientsWithHighViralLoadCohortDataset listOfPatientsWithHighViralLoadCohortDataset) {
    this.semanaCorrenteDatasetOfHighViralLoadCohortDataset =
        semanaCorrenteDatasetOfHighViralLoadCohortDataset;
    this.listOfPatientsWithHighViralLoadCohortDataset =
        listOfPatientsWithHighViralLoadCohortDataset;
  }

  @Override
  public String getUuid() {
    return "471b83a4-7ae3-11ed-b983-67166207dca4";
  }

  @Override
  public String getName() {
    return "List of Patients with High Viral Load Cohort Report";
  }

  @Override
  public String getDescription() {
    return "Lista de Pacientes com Carga Viral Alta";
  }

  @Override
  public String getVersion() {
    return "1.0-SNAPSHOT";
  }

  @Override
  public String getExcelDesignUuid() {
    return "9fea8c6e-7ae3-11ed-bba2-33d0afb9d8f4";
  }

  @Override
  public ReportDefinition constructReportDefinition() {
    ReportDefinition rd = new ReportDefinition();
    rd.setUuid(getUuid());
    rd.setName(getName());
    rd.setDescription(getDescription());
    rd.addParameters(getParameters());
    rd.addDataSetDefinition("DT", Mapped.mapStraightThrough(new DatimCodeDatasetDefinition()));
    rd.addDataSetDefinition("SISMA", Mapped.mapStraightThrough(new SismaCodeDatasetDefinition()));
    rd.addDataSetDefinition(
        "HIGHVL",
        Mapped.mapStraightThrough(listOfPatientsWithHighViralLoadCohortDataset.contructDataset()));
    rd.addDataSetDefinition(
        "SMNC",
        Mapped.mapStraightThrough(
            semanaCorrenteDatasetOfHighViralLoadCohortDataset.contructDataset()));

    return rd;
  }

  @Override
  public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
    ReportDesign reportDesign = null;
    try {
      reportDesign =
          createXlsReportDesign(
              reportDefinition,
              "ListPatientWithHighViralLoad_v1.0.xls",
              "List of Patients with High Viral Load Cohort Report",
              getExcelDesignUuid(),
              null);
      Properties props = new Properties();
      props.put("repeatingSections", "sheet:1,row:13,dataset:HIGHVL");
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
        new Parameter("startDate", "Cohort Start Date", Date.class),
        new Parameter("endDate", "Cohort End Date", Date.class),
        new Parameter("evaluationDate", "Evaluation Date", Date.class),
        new Parameter("location", "Location", Location.class));
  }
}
