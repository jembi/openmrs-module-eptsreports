package org.openmrs.module.eptsreports.reporting.reports;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.library.cohorts.GenericCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.datasets.DatimCodeDatasetDefinition;
import org.openmrs.module.eptsreports.reporting.library.datasets.LocationDataSetDefinition;
import org.openmrs.module.eptsreports.reporting.reports.manager.EptsDataExportManager;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.ReportingException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;

@Deprecated
public class SetupResumoMensalDAHReport extends EptsDataExportManager {

  private GenericCohortQueries genericCohortQueries;

  @Autowired
  public SetupResumoMensalDAHReport(GenericCohortQueries genericCohortQueries) {
    this.genericCohortQueries = genericCohortQueries;
  }

  @Override
  public String getUuid() {
    return "369d817e-89df-11ee-9998-d387830ce643";
  }

  @Override
  public String getName() {
    return "Resumo Mensal de DAH";
  }

  @Override
  public String getDescription() {
    return "Este relatório apresenta os dados do Resumo Mensal da Unidade Sanitária "
        + "para a Doença Avançada por HIV, provenientes da ferramenta Ficha de DAH e Ficha Mestra no sistema.";
  }

  @Override
  public String getVersion() {
    return "1.0-SNAPSHOT";
  }

  @Override
  public String getExcelDesignUuid() {
    return "a288d9c4-89df-11ee-b960-272d3d62348d";
  }

  @Override
  public ReportDefinition constructReportDefinition() {

    ReportDefinition rd = new ReportDefinition();
    rd.setName(getName());
    rd.setDescription(getDescription());
    rd.setUuid(getUuid());
    rd.addParameters(getParameters());

    rd.addDataSetDefinition("DT", Mapped.mapStraightThrough(new DatimCodeDatasetDefinition()));
    rd.addDataSetDefinition("HF", Mapped.mapStraightThrough(new LocationDataSetDefinition()));

    // Report Base Cohort
    rd.setBaseCohortDefinition(
        EptsReportUtils.map(
            genericCohortQueries.getBaseCohort(), "endDate=${endDate},location=${location}"));

    return rd;
  }

  @Override
  public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
    ReportDesign reportDesign = null;
    try {
      reportDesign =
          createXlsReportDesign(
              reportDefinition,
                  "Resumo_Mensal_DAH_v2.0.1.xls",
              "Resumo Mensal de DAH",
              getExcelDesignUuid(),
              null);
      Properties props = new Properties();
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
        new Parameter("startDate", "Data Início", Date.class),
        new Parameter("endDate", "Data Fim", Date.class),
        new Parameter("location", "Unidade Sanitária", Location.class));
  }
}