package org.openmrs.module.eptsreports.reporting.reports;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.openmrs.module.eptsreports.reporting.library.cohorts.GenericCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.datasets.AdvancedDiseaseAndTbCascadeDataset;
import org.openmrs.module.eptsreports.reporting.library.datasets.DatimCodeDatasetDefinition;
import org.openmrs.module.eptsreports.reporting.library.datasets.SismaCodeDatasetDefinition;
import org.openmrs.module.eptsreports.reporting.reports.manager.EptsDataExportManager;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;

@Deprecated
public class SetupAdvancedDiseaseAndTbCascadeReport extends EptsDataExportManager {

  @Autowired private GenericCohortQueries genericCohortQueries;

  @Autowired private AdvancedDiseaseAndTbCascadeDataset advancedDiseaseAndTbCascadeDataset;

  @Override
  public String getExcelDesignUuid() {
    return "6346b61a-35c4-11ee-8eb8-db7d1e195f23";
  }

  @Override
  public String getUuid() {
    return "7ed9b224-35c4-11ee-b0c4-b7b6911c528b";
  }

  @Override
  public String getName() {
    return "TB7: Relatório Cascata de Doença Avançada por HIV e TB";
  }

  @Override
  public String getDescription() {
    return "O Relatório Cascata de Doença Avançada e TB gera o número de utentes de acordo com cada uma das 9 etapas da cascata clínica de Doença Avançada por HIV e TB";
  }

  @Override
  public String getVersion() {
    return "1.0-SNAPSHOT";
  }

  @Override
  public ReportDefinition constructReportDefinition() {
    ReportDefinition reportDefinition = new ReportDefinition();
    reportDefinition.setUuid(getUuid());
    reportDefinition.setName(getName());
    reportDefinition.setDescription(getDescription());
    reportDefinition.setParameters(getParameters());

    reportDefinition.addDataSetDefinition(
        "DT", Mapped.mapStraightThrough(new DatimCodeDatasetDefinition()));
    reportDefinition.addDataSetDefinition(
        "SM", Mapped.mapStraightThrough(new SismaCodeDatasetDefinition()));
    reportDefinition.addDataSetDefinition(
        "AD",
        Mapped.mapStraightThrough(
            advancedDiseaseAndTbCascadeDataset.constructAdvancedDiseaseAndTbCascadeDataset()));
    reportDefinition.setBaseCohortDefinition(
        EptsReportUtils.map(
            genericCohortQueries.getBaseCohort(), "endDate=${endDate},location=${location}"));

    return reportDefinition;
  }

  @Override
  public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
    ReportDesign reportDesign;
    try {
      reportDesign =
          createXlsReportDesign(
              reportDefinition,
              "Template_TB7 Relatorio da Cascata de DAH e TB_v12.xls",
              "TB7: Relatório Cascata de Doença Avançada por HIV e TB",
              getExcelDesignUuid(),
              null);
      Properties props = new Properties();
      props.put("sortWeight", "5000");
      reportDesign.setProperties(props);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return Arrays.asList(reportDesign);
  }

  public List<Parameter> getParameters() {
    List<Parameter> parameters = new ArrayList<>();
    parameters.add(ReportingConstants.END_DATE_PARAMETER);
    parameters.add(ReportingConstants.LOCATION_PARAMETER);
    return parameters;
  }
}
