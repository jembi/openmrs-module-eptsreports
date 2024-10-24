package org.openmrs.module.eptsreports.reporting.reports;

import java.io.IOException;
import java.util.*;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.library.datasets.DatimCodeDatasetDefinition;
import org.openmrs.module.eptsreports.reporting.library.datasets.SismaCodeDatasetDefinition;
import org.openmrs.module.eptsreports.reporting.library.datasets.TPTListOfPatientsEligibleDataSet;
import org.openmrs.module.eptsreports.reporting.library.datasets.TPTTotalListOfPatientsEligibleDataSet;
import org.openmrs.module.eptsreports.reporting.reports.manager.EptsDataExportManager;
import org.openmrs.module.reporting.ReportingException;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;

@Deprecated
public class SetupTPTListOfPatientsEligibleReport extends EptsDataExportManager {

  private TPTListOfPatientsEligibleDataSet tptListOfPatientsEligibleDataSet;
  private TPTTotalListOfPatientsEligibleDataSet tptTotalListOfPatientsEligibleDataSet;

  @Autowired
  public SetupTPTListOfPatientsEligibleReport(
      TPTListOfPatientsEligibleDataSet tptListOfPatientsEligibleDataSet,
      TPTTotalListOfPatientsEligibleDataSet tptTotalListOfPatientsEligibleDataSet) {

    this.tptListOfPatientsEligibleDataSet = tptListOfPatientsEligibleDataSet;
    this.tptTotalListOfPatientsEligibleDataSet = tptTotalListOfPatientsEligibleDataSet;
  }

  @Override
  public String getExcelDesignUuid() {
    return "5e17f214-af0f-11eb-852c-ef10820bc4bj";
  }

  @Override
  public String getUuid() {
    return "6ee04286-af0f-11eb-afea-e3389adce11h";
  }

  @Override
  public String getName() {
    return "TB2: List of Patients who are Eligible for TPT";
  }

  @Override
  public String getDescription() {
    return "This report provides a line listing of patient records who eligible TPT";
  }

  @Override
  public ReportDefinition constructReportDefinition() {
    ReportDefinition rd = new ReportDefinition();
    rd.setUuid(getUuid());
    rd.setName(getName());
    rd.setDescription(getDescription());
    rd.addParameters(getParameters());

    rd.addDataSetDefinition(
        "TOTAL",
        Mapped.mapStraightThrough(tptTotalListOfPatientsEligibleDataSet.constructDataset()));
    try {
      rd.addDataSetDefinition(
          "TPT", Mapped.mapStraightThrough(tptListOfPatientsEligibleDataSet.constructDataset()));
    } catch (EvaluationException e) {
      e.printStackTrace();
    }
    rd.addDataSetDefinition("DT", Mapped.mapStraightThrough(new DatimCodeDatasetDefinition()));
    rd.addDataSetDefinition("SM", Mapped.mapStraightThrough(new SismaCodeDatasetDefinition()));
    return rd;
  }

  @Override
  public String getVersion() {
    return "1.0-SNAPSHOT";
  }

  @Override
  public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
    ReportDesign reportDesign = null;
    try {
      reportDesign =
          createXlsReportDesign(
              reportDefinition,
              "Template_List_Patients_Eligibles_TPT_v1.3.xls",
              "TB2: List of Patients active on ART who are Eligible for TPT",
              getExcelDesignUuid(),
              null);
      Properties props = new Properties();
      props.put("repeatingSections", "sheet:1,row:5,dataset:TPT");
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
        new Parameter("endDate", "End date", Date.class),
        new Parameter("location", "Location", Location.class));
  }
}
