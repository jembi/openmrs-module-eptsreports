package org.openmrs.module.eptsreports.reporting.reports;

import java.io.IOException;
import java.util.*;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.library.datasets.DatimCodeDatasetDefinition;
import org.openmrs.module.eptsreports.reporting.library.datasets.SismaCodeDatasetDefinition;
import org.openmrs.module.eptsreports.reporting.library.datasets.list.ListOfChildrenEnrolledInCCRDataset;
import org.openmrs.module.eptsreports.reporting.reports.manager.EptsDataExportManager;
import org.openmrs.module.reporting.ReportingException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;

@Deprecated
public class SetupListOfChildrenEnrolledInCCRReport extends EptsDataExportManager {

  private final ListOfChildrenEnrolledInCCRDataset listOfChildrenEnrolledInCCRDataset;

  @Autowired
  public SetupListOfChildrenEnrolledInCCRReport(
      ListOfChildrenEnrolledInCCRDataset listOfChildrenEnrolledInCCRDataset) {
    this.listOfChildrenEnrolledInCCRDataset = listOfChildrenEnrolledInCCRDataset;
  }

  @Override
  public String getExcelDesignUuid() {
    return "27af6b66-28a2-11ef-a438-57c285b9b2c7";
  }

  @Override
  public String getUuid() {
    return "2e2b53d8-28a2-11ef-8d7d-13666a8776b4";
  }

  @Override
  public String getName() {
    return "CCR1: Lista de Crianças Inscritas em CCR";
  }

  @Override
  public String getVersion() {
    return "3.0.1-SNAPSHOT";
  }

  @Override
  public String getDescription() {
    return "Este relatório gera o número agregado e a lista de crianças inscritas nos serviços "
        + "de CCR da Unidade Sanitária entre as datas de início e fim informadas.";
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
        "CCR",
        Mapped.mapStraightThrough(
            listOfChildrenEnrolledInCCRDataset.listOfChildrenEnrolledInCCRColumnsDataset()));

    reportDefinition.addDataSetDefinition(
        "TOTAL",
        Mapped.mapStraightThrough(
            listOfChildrenEnrolledInCCRDataset.listOfChildrenEnrolledInCCRTotalsDataset()));

    return reportDefinition;
  }

  @Override
  public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {

    ReportDesign reportDesign;

    try {
      reportDesign =
          createXlsReportDesign(
              reportDefinition,
              "Template_List_CCR_Patients_v1.0.xls",
              "CCR1: Lista de Crianças Inscritas em CCR",
              getExcelDesignUuid(),
              null);
      Properties props = new Properties();
      props.put("repeatingSections", "sheet:1,row:11,dataset:CCR");
      props.put("sortWeight", "5000");
      reportDesign.setProperties(props);
    } catch (IOException e) {
      throw new ReportingException(e.toString());
    }

    return Collections.singletonList(reportDesign);
  }

  @Override
  public List<Parameter> getParameters() {
    return Arrays.asList(
        new Parameter("startDate", "Data Início", Date.class),
        new Parameter("endDate", "Data Fim", Date.class),
        new Parameter("location", "Unidade Sanitária", Location.class));
  }
}
