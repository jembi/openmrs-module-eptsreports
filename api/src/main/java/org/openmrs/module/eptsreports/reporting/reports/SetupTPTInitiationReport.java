package org.openmrs.module.eptsreports.reporting.reports;

import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.library.cohorts.GenericCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.data.quality.SummaryEc20DataQualityCohorts;
import org.openmrs.module.eptsreports.reporting.library.datasets.TPTInitiationDataset;
import org.openmrs.module.eptsreports.reporting.library.datasets.data.quality.Ec20PatientListDataset;
import org.openmrs.module.eptsreports.reporting.library.datasets.data.quality.SummaryEc20DataQualityDataset;
import org.openmrs.module.eptsreports.reporting.reports.manager.EptsDataExportManager;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.ReportingException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.util.*;


@Component
public class SetupTPTInitiationReport extends EptsDataExportManager {


  private TPTInitiationDataset tptInitiationDataset;


  @Autowired
  protected GenericCohortQueries genericCohortQueries;

  @Autowired
  public SetupTPTInitiationReport(
      TPTInitiationDataset tptInitiationDataset) {
    this.tptInitiationDataset = tptInitiationDataset;
  }

  @Override
  public String getExcelDesignUuid() {
    return "0638ab60-bf2c-11e9-b8ed-7b0ec2ec93ad";
  }

  @Override
  public String getUuid() {
    return "10b6239c-bf2c-11e9-a2e5-63fb38259292";
  }

  @Override
  public String getName() {
    return "TPT Initiation Report";
  }

  @Override
  public String getDescription() {
    return "This report provides a line listing of patient records who initiated TPT";
  }

  @Override
  public ReportDefinition constructReportDefinition() {
    ReportDefinition rd = new ReportDefinition();
    rd.setUuid(getUuid());
    rd.setName(getName());
    rd.setDescription(getDescription());
    rd.addParameters(getParameters());

    rd.setBaseCohortDefinition(
        EptsReportUtils.map(
                genericCohortQueries.getBaseCohort(), "startDate=${startDate}, endDate=${endDate},location=${location}"));
    rd.addDataSetDefinition(
            "ALL", Mapped.mapStraightThrough(tptInitiationDataset.constructDatset()));
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
              "TPT_Initiation_Report.xls",
              "TPT Initiation Report",
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
            new Parameter("startDate", "Data Inicial Inclusão", Date.class),
            new Parameter("endDate", "Data Final Inclusão", Date.class),
            new Parameter("location", "Unidade Sanitária", Location.class));
  }
}



