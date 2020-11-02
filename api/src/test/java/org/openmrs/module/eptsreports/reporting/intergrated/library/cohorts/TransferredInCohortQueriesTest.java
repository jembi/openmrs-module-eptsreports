package org.openmrs.module.eptsreports.reporting.intergrated.library.cohorts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.reporting.intergrated.utils.DefinitionsTest;
import org.openmrs.module.eptsreports.reporting.library.cohorts.TransferredInCohortQueries;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;

public class TransferredInCohortQueriesTest extends DefinitionsTest {

  @Autowired private TransferredInCohortQueries transferredInCohortQueries;

  @Before
  public void setup() throws Exception {
    executeDataSet("ResumoMensalTest.xml");
  }

  @Test
  public void getNumberOfPatientsTransferredInOnPeriodShouldReturn()
      throws EvaluationException {
    CohortDefinition cd =
        transferredInCohortQueries.getTransferredInPatients();

    HashMap<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("onOrAfter", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("onOrBefore", "End Date", Date.class), this.getEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), this.getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cd, parameters);

    assertEquals(1, evaluatedCohort.getMemberIds().size());
    assertTrue(evaluatedCohort.getMemberIds().contains(12475));
  }

  @Override
  protected Date getStartDate() {
    return DateUtil.getDateTime(2018, 6, 21);
  }

  @Override
  protected Date getEndDate() {
    return DateUtil.getDateTime(2018, 7, 20);
  }

  @Override
  protected Location getLocation() {
    return Context.getLocationService().getLocation(6);
  }

  @Override
  protected void setParameters(
      Date startDate, Date endDate, Location location, EvaluationContext context) {

    context.addParameterValue("startDate", startDate);
    context.addParameterValue("onOrBefore", endDate);
    context.addParameterValue("location", location);
  }
}
