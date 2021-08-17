package org.openmrs.module.eptsreports.reporting.library.cohorts;

import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class ListOfPatientsDefaultersOrIITCohortQueries {

    @Autowired
    private TxCurrCohortQueries txCurrCohortQueries;

    public CohortDefinition getE() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("startDate", "StartDate", Date.class));
        cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
        cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
        cd.addParameter(new Parameter("location", "Location", Location.class));

        CohortDefinition e22 = txCurrCohortQueries.getDeadPatientsInDemographiscByReportingEndDate();
        CohortDefinition e23 = txCurrCohortQueries.getPatientDeathRegisteredInLastHomeVisitCardByReportingEndDate();
        CohortDefinition e24 = txCurrCohortQueries.getDeadPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate();


        cd.addSearch("A", EptsReportUtils.map(e22, ""));
        cd.addSearch("B", EptsReportUtils.map(e23, ""));
        cd.addSearch("C", EptsReportUtils.map(e24, ""));


        cd.setCompositionString(
                "(A OR B)");

        return cd;
    }
}
