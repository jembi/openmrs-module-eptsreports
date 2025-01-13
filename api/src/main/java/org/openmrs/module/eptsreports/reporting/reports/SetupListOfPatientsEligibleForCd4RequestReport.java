package org.openmrs.module.eptsreports.reporting.reports;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.library.datasets.DatimCodeDatasetDefinition;
import org.openmrs.module.eptsreports.reporting.library.datasets.SismaCodeDatasetDefinition;
import org.openmrs.module.eptsreports.reporting.library.datasets.list.ListOfPatientsEligibleForCd4RequestDataset;
import org.openmrs.module.eptsreports.reporting.reports.manager.EptsDataExportManager;
import org.openmrs.module.reporting.ReportingException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;

@Deprecated
public class SetupListOfPatientsEligibleForCd4RequestReport extends EptsDataExportManager {

  private final ListOfPatientsEligibleForCd4RequestDataset
      listOfPatientsEligibleForCd4RequestDataset;

  @Autowired
  public SetupListOfPatientsEligibleForCd4RequestReport(
      ListOfPatientsEligibleForCd4RequestDataset listOfPatientsEligibleForCd4RequestDataset) {
    this.listOfPatientsEligibleForCd4RequestDataset = listOfPatientsEligibleForCd4RequestDataset;
  }

  @Override
  public String getExcelDesignUuid() {
    return "d2e70e1a-21dd-11ef-ab8e-ef45f1aa9463";
  }

  @Override
  public String getUuid() {
    return "dc134d00-21dd-11ef-91b9-7b13271d49df";
  }

  @Override
  public String getName() {
    return "Lista de Utentes Elegíveis ao Pedido de CD4";
  }

  @Override
  public String getDescription() {
    return "Este relatório gera o número agregado e a lista de utentes elegíveis ao pedido de CD4 durante o "
        + "período de reporte.";
  }

  @Override
  public String getVersion() {
    return "3.0.1-SNAPSHOT";
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
        "ELIGIBLE",
        Mapped.mapStraightThrough(
            listOfPatientsEligibleForCd4RequestDataset
                .listOfPatientsEligibleForCd4RequestColumnsDataset()));

    reportDefinition.addDataSetDefinition(
        "TOTAL",
        Mapped.mapStraightThrough(
            listOfPatientsEligibleForCd4RequestDataset
                .listOfPatientsEligibleForCd4RequestTotalsDataset()));

    return reportDefinition;
  }

  @Override
  public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {

    ReportDesign reportDesign;

    try {
      reportDesign =
          createXlsReportDesign(
              reportDefinition,
              "Template_Utentes_Elegiveis_Pedido_CD4.xls",
              "Lista de Utentes Elegíveis ao Pedido de CD4",
              getExcelDesignUuid(),
              null);
      Properties props = new Properties();
      props.put("repeatingSections", "sheet:1,row:10,dataset:ELIGIBLE");
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
