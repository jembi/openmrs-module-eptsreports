package org.openmrs.module.eptsreports.reporting.reports;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.openmrs.module.eptsreports.reporting.reports.manager.EptsDataExportManager;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.ReportDesign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.openmrs.module.reporting.ReportingException;

@Component
public class SetupListOfPatientsArtCohort extends EptsDataExportManager {

    @Override
    public String getUuid() {
        return "43b45d8e-5295-11ec-8b4e-cbc266e30840";
    }

    @Override
    public String getName() {
        return "List of Patients ART Cohort Report";
    }

    @Override
    public String getDescription() {
        return "Lista de Pacientes na Coorte de TARV";
    }

    @Override
    public String getVersion() {
        return "1.0-SNAPSHOT";
    }

    @Override
    public String getExcelDesignUuid() {
        return "69ca9d94-5295-11ec-b149-a7f66a2d0b20";
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.addParameters(getParameters());

        rd.addDataSetDefinition("TOTAL",
                Mapped.mapStraightThrough(totalListOfPatientsArtCohortDataset.contructDataset()));
        try {
            rd.addDataSetDefinition("ART", Mapped.mapStraightThrough(listOfPatientsArtCohortDataset.contructDataset()));
        } catch (Exception e) {
            e.printStackTrace();

        }

        return rd;
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        ReportDesign reportDesign = null;
        try {
            reportDesign = createXlsReportDesign(
                    reportDefinition,
                    "Template_List_Patients_ART_Cohort_v0.3.xls",
                    "List of Patients ART Cohort Report",
                    getExcelDesignUuid(),
                    null);
            Properties props = new Properties();
            props.put("repeatingSections", "sheet:1,row:7,dataset:ART");
            props.put("sortWeight", "5000");
            reportDesign.setProperties(props);
        } catch (IOException e) {
            throw new ReportingException(e.toString());
        }

        return Arrays.asList(reportDesign);
    }

    // @Override
    // public List<Parameter> getParameters() {
    // return Arrays.asList(
    // new Parameter("endDate", "Cohort", Date.class),
    // new Parameter("location", "Location", Location.class));
    // }

}
