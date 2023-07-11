package org.openmrs.module.eptsreports.reporting.reports;

import java.io.IOException;
import java.util.*;
import org.openmrs.module.eptsreports.reporting.library.datasets.DatimCodeDatasetDefinition;
import org.openmrs.module.eptsreports.reporting.library.datasets.ListOfPatientsWithMdcEvaluationCohortDataset;
import org.openmrs.module.eptsreports.reporting.library.datasets.SismaCodeDatasetDefinition;
import org.openmrs.module.eptsreports.reporting.reports.manager.EptsDataExportManager;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.ReportingException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;

@Deprecated
public class SetupListOfPatientsWithMdcEvaluation extends EptsDataExportManager {

  @Autowired
  private ListOfPatientsWithMdcEvaluationCohortDataset listOfPatientsWithMdcEvaluationCohortDataset;

  @Autowired
  public SetupListOfPatientsWithMdcEvaluation(
      ListOfPatientsWithMdcEvaluationCohortDataset listOfPatientsWithMdcevaluationCohortDataset) {
    this.listOfPatientsWithMdcEvaluationCohortDataset =
        listOfPatientsWithMdcevaluationCohortDataset;
  }

  @Override
  public String getUuid() {
    return "51c16c68-1653-11ee-abf0-cbd73a11f3ee";
  }

  @Override
  public String getName() {
    return "List of Patients with MDS Evaluation Cohort Report";
  }

  @Override
  public String getDescription() {
    return "Lista de Pacientes com MDS Evaluation";
  }

  @Override
  public String getVersion() {
    return "1.0-SNAPSHOT";
  }

  @Override
  public String getExcelDesignUuid() {
    return "86268baa-1653-11ee-847b-cfb02184395c";
  }

  @Override
  public ReportDefinition constructReportDefinition() {
    ReportDefinition rd = new ReportDefinition();
    rd.setUuid(getUuid());
    rd.setName(getName());
    rd.setDescription(getDescription());
    rd.addParameters(getParameters());
    rd.addDataSetDefinition("DT", Mapped.mapStraightThrough(new DatimCodeDatasetDefinition()));
    rd.addDataSetDefinition("SM", Mapped.mapStraightThrough(new SismaCodeDatasetDefinition()));
    rd.addDataSetDefinition(
        "MDC",
        Mapped.mapStraightThrough(listOfPatientsWithMdcEvaluationCohortDataset.contructDataset()));

    return rd;
  }

  @Override
  public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
    ReportDesign reportDesign = null;
    try {
      reportDesign =
          createXlsReportDesign(
              reportDefinition,
              "SESP_MDS_Evaluation_V1.0.xls",
              "List of Patients with MDS Evaluation Cohort Report",
              getExcelDesignUuid(),
              null);
      Properties props = new Properties();
      props.put("repeatingSections", "sheet:1,row:13,dataset:MDS");
      props.put("sortWeight", "5000");
      reportDesign.setProperties(props);
    } catch (IOException e) {
      throw new ReportingException(e.toString());
    }

    return Arrays.asList(reportDesign);
  }

  @Override
  public List<Parameter> getParameters() {
    List<Parameter> parameters = new ArrayList<>();
    parameters.add(ReportingConstants.START_DATE_PARAMETER);
    parameters.add(ReportingConstants.END_DATE_PARAMETER);
    parameters.add(ReportingConstants.LOCATION_PARAMETER);
    return parameters;
  }
}
