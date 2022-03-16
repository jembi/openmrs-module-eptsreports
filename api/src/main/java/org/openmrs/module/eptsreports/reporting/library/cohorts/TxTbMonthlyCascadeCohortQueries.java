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
public class TxTbMonthlyCascadeCohortQueries {

  @Autowired private IntensiveMonitoringCohortQueries intensiveMonitoringCohortQueries;

  @Autowired private TxCurrCohortQueries txCurrCohortQueries;

  @Autowired private TXTBCohortQueries txtbCohortQueries;

  public CohortDefinition getTxCurr() {
    CompositionCohortDefinition chd = new CompositionCohortDefinition();
    chd.addParameter(new Parameter("endDate", "End Date", Date.class));
    chd.addParameter(new Parameter("location", "Facility", Location.class));

    chd.addSearch(
        "txCurr",
        EptsReportUtils.map(
            txCurrCohortQueries.getTxCurrCompositionCohort("tx_curr", true),
            "onOrBefore=${endDate},location=${location},locations=${location}"));

    chd.setCompositionString("txCurr");
    return chd;
  }
}
