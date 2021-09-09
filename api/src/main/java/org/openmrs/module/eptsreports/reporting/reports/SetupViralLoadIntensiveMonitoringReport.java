package org.openmrs.module.eptsreports.reporting.reports;

import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.library.cohorts.GenericCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.datasets.IntensiveMonitoringDataSet;
import org.openmrs.module.eptsreports.reporting.reports.manager.EptsDataExportManager;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class SetupViralLoadIntensiveMonitoringReport extends EptsDataExportManager {


    private GenericCohortQueries genericCohortQueries;

    private IntensiveMonitoringDataSet intensiveMonitoringDataSet;

    @Autowired
    public SetupViralLoadIntensiveMonitoringReport(
            IntensiveMonitoringDataSet intensiveMonitoringDataSet,
            GenericCohortQueries genericCohortQueries) {
        this.genericCohortQueries = genericCohortQueries;
        this.intensiveMonitoringDataSet = intensiveMonitoringDataSet;


    }

    @Override
    public String getExcelDesignUuid() {
        return "00f759a8-1071-11ec-a94a-f3caea915cae";
    }

    @Override
    public String getUuid() {
        return "f3723f64-1070-11ec-a7a2-c38badf8edf0";
    }

    @Override
    public String getName() {
        return "Viral Load Intensive Monitoring Report";
    }

    @Override
    public String getDescription() {
        return "This report provides list of Viral Load Intensive Monitoring";
    }

    @Override
    public String getVersion() {
        return "1.0-SNAPSHOT";
    }

    @Override
    public ReportDefinition constructReportDefinition() {

        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());
        rd.addDataSetDefinition("VLIM",
                EptsReportUtils.map(this.intensiveMonitoringDataSet.constructIntensiveMonitoringDataSet(),
                        "revisionEndDate=${endDate},localtion=${localtion}"));

        rd.setBaseCohortDefinition(
                EptsReportUtils.map(
                        genericCohortQueries.getBaseCohort(),
                        "endDate=${endDate},location=${location}"));
        return rd;
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        ReportDesign reportDesign = null;
        try {
            reportDesign = createXlsReportDesign(
                    reportDefinition,
                    "Template_MI_2021_v1.7.xls",
                    "Viral Load Intensive Monitoring ",
                    getExcelDesignUuid(),
                    null);
            Properties properties = new Properties();
            properties.put("sortWeight", "5000");
            reportDesign.setProperties(properties);
        }catch (IOException e){
            throw new RuntimeException(e);
        }

        return Arrays.asList(reportDesign);
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> parameters  = new ArrayList<>();
        parameters.add(new Parameter("endDate","End Date", Date.class));
        parameters.add(new Parameter("location","Location", Location.class));

        return parameters;
    }
}
