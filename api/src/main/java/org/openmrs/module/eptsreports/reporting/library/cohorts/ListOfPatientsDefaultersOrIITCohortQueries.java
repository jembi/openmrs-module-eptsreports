package org.openmrs.module.eptsreports.reporting.library.cohorts;

import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
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

    @Autowired private CommonCohortQueries commonCohortQueries;

    @Autowired private GenericCohortQueries genericCohortQueries;

    @Autowired private HivMetadata hivMetadata;

    public CohortDefinition getE1() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("startDate", "StartDate", Date.class));
        cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
        cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
        cd.addParameter(new Parameter("location", "Location", Location.class));

        CohortDefinition e12 = commonCohortQueries.getTranferredOutPatients();



        cd.addSearch("A", EptsReportUtils.map(e12, ""));


        cd.setCompositionString(
                "(A)");

        return cd;
    }
    public CohortDefinition getE2() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("startDate", "StartDate", Date.class));
        cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
        cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
        cd.addParameter(new Parameter("location", "Location", Location.class));

        CohortDefinition e21 = getPatientsInArtCareWhoDied();
        CohortDefinition e22 = txCurrCohortQueries.getDeadPatientsInDemographiscByReportingEndDate();
        CohortDefinition e23 = txCurrCohortQueries.getPatientDeathRegisteredInLastHomeVisitCardByReportingEndDate();
        CohortDefinition e24 = txCurrCohortQueries.getDeadPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate();


        cd.addSearch("A", EptsReportUtils.map(e22, ""));
        cd.addSearch("B", EptsReportUtils.map(e23, ""));
        cd.addSearch("C", EptsReportUtils.map(e24, ""));
        cd.addSearch("D", EptsReportUtils.map(e21, ""));


        cd.setCompositionString(
                "(A OR B)");

        return cd;
    }

    public CohortDefinition getPatientsInArtCareWhoDied() {
        Program hivCareProgram = hivMetadata.getARTProgram();
        ProgramWorkflowState dead = hivMetadata.getArtDeadWorkflowState();
        return genericCohortQueries.getPatientsBasedOnPatientStatesBeforeDate(
                hivCareProgram.getProgramId(), dead.getProgramWorkflowStateId());
    }
}
