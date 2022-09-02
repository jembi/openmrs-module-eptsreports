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
public class ListOfPatientsWhoPickedupArvDuringPeriodCohortQueries {

    private final TxCurrCohortQueries txCurrCohortQueries;

    @Autowired

    public ListOfPatientsWhoPickedupArvDuringPeriodCohortQueries(TxCurrCohortQueries txCurrCohortQueries) {
        this.txCurrCohortQueries = txCurrCohortQueries;
    }

    public CohortDefinition getBaseCohort() {

        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.setName("Base Cohort for List of patients who picked up ARV during the period");
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.addParameter(new Parameter("location", "Location", Location.class));

        CohortDefinition txcurr = this.txCurrCohortQueries.getTxCurrCompositionCohort("txcurr", true);

        cd.addSearch(
                "txcurr", EptsReportUtils.map(txcurr, "onOrBefore=${startDate},location=${location}"));

        cd.setCompositionString("txcurr");
        return cd;
    }

}
