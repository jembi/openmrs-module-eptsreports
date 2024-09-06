package org.openmrs.module.eptsreports.reporting.reports;

import java.io.IOException;
import java.util.*;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.library.datasets.*;
import org.openmrs.module.eptsreports.reporting.reports.manager.EptsDataExportManager;
import org.openmrs.module.reporting.ReportingException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;

@Deprecated
public class SetupListOfPatientsWithMdsEvaluation extends EptsDataExportManager {

  @Autowired
  private ListOfPatientsWithMdsEvaluationCohortDataset listOfPatientsWithMdsEvaluationCohortDataset;
  private ListOfPatientsWithMds24To36CohortDataset listOfPatientsWithMds24To36CohortDataset;
  private ListOfPatientsWithMds36CohortDataset listOfPatientsWithMds36CohortDataset;

  @Autowired
  public SetupListOfPatientsWithMdsEvaluation(
      ListOfPatientsWithMdsEvaluationCohortDataset listOfPatientsWithMdsEvaluationCohortDataset,
      ListOfPatientsWithMds24To36CohortDataset listOfPatientsWithMds24To36CohortDataset,
      ListOfPatientsWithMds36CohortDataset listOfPatientsWithMds36CohortDataset) {
    this.listOfPatientsWithMdsEvaluationCohortDataset =
        listOfPatientsWithMdsEvaluationCohortDataset;
    this.listOfPatientsWithMds24To36CohortDataset = listOfPatientsWithMds24To36CohortDataset;
    this.listOfPatientsWithMds36CohortDataset = listOfPatientsWithMds36CohortDataset;
  }

  @Override
  public String getUuid() {
    return "51c16c68-1653-11ee-abf0-cbd73a11f3ee";
  }

  @Override
  public String getName() {
    return "Relatório de Avaliação de MDS";
  }

  @Override
  public String getDescription() {
    return "Avaliação da Implementação de MDS";
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
        "MDS",
        Mapped.mapStraightThrough(listOfPatientsWithMdsEvaluationCohortDataset.contructDataset()));
    rd.addDataSetDefinition(
        "MDS24TO236TCOHORT",
        Mapped.mapStraightThrough(listOfPatientsWithMds24To36CohortDataset.contructDataset()));
    rd.addDataSetDefinition(
        "MDS36COHORT",
        Mapped.mapStraightThrough(listOfPatientsWithMds36CohortDataset.contructDataset()));

    return rd;
  }

  @Override
  public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
    ReportDesign reportDesign = null;
    try {
      reportDesign =
          createXlsReportDesign(
              reportDefinition,
              "MISAU_SESP_MASC_MDS_v3.1.xls",
              "Relatório de Avaliação de MDS",
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
    return Arrays.asList(
        new Parameter("evaluationYear", "Ano de Avaliação", Integer.class),
        new Parameter("location", "Unidade Sanitária", Location.class));
  }
}
