package org.openmrs.module.eptsreports.reporting.library.cohorts;

import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TxTbMonthlyCascadeCohortQueries {

  @Autowired private IntensiveMonitoringCohortQueries intensiveMonitoringCohortQueries;

  @Autowired private TxCurrCohortQueries txCurrCohortQueries;

  @Autowired private TXTBCohortQueries txtbCohortQueries;

  public CohortDefinition getTxCurr() {

    CohortDefinition txCurr = txCurrCohortQueries.getTxCurrBaseCohort();
    return txCurr;
  }
}
