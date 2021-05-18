package org.openmrs.module.eptsreports.reporting.reports;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.openmrs.module.eptsreports.reporting.library.datasets.listing.PatientListingDataset;
import org.openmrs.module.eptsreports.reporting.reports.manager.EptsDataExportManager;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.ReportingException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SetupPatientListingSampleReport extends EptsDataExportManager {

  private PatientListingDataset patientListingDataset;

  @Autowired
  public SetupPatientListingSampleReport(PatientListingDataset patientListingDataset) {
    this.patientListingDataset = patientListingDataset;
  }

  @Override
  public String getExcelDesignUuid() {
    return "c827a0ec-b710-11eb-b466-0721bfb958f8";
  }

  @Override
  public String getUuid() {
    return "bd1f32c8-b710-11eb-97f2-37619f14a020";
  }

  @Override
  public String getName() {
    return "Test report";
  }

  @Override
  public String getDescription() {
    return "Test patient report";
  }

  @Override
  public ReportDefinition constructReportDefinition() {
    ReportDefinition rd = new ReportDefinition();
    rd.setUuid(getUuid());
    rd.setName(getName());
    rd.setDescription(getDescription());
    rd.addParameters(getDataParameters());
    rd.addDataSetDefinition(
        "test", Mapped.mapStraightThrough(patientListingDataset.getPatientListForIpt()));
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
              reportDefinition, "test.xls", "Test patient list Report", getExcelDesignUuid(), null);
      Properties props = new Properties();
      props.put("repeatingSections", "sheet:1,row:5,dataset:test");
      props.put("sortWeight", "5000");
      reportDesign.setProperties(props);
    } catch (IOException e) {
      throw new ReportingException(e.toString());
    }

    return Arrays.asList(reportDesign);
  }

  private List<Parameter> getDataParameters() {
    List<Parameter> parameters = new ArrayList<>();
    parameters.add(ReportingConstants.START_DATE_PARAMETER);
    parameters.add(ReportingConstants.END_DATE_PARAMETER);
    parameters.add(ReportingConstants.LOCATION_PARAMETER);
    return parameters;
  }
}
