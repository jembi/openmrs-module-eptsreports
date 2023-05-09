package org.openmrs.module.eptsreports.reporting.intergrated.library.cohorts;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.reporting.intergrated.utils.DefinitionsTest;
import org.openmrs.module.eptsreports.reporting.library.cohorts.TxPvlsCohortQueries;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;

public class TxPvlsCohortQueriesTest extends DefinitionsTest {

  @Autowired private TxPvlsCohortQueries txPvlsCohortQueries;

  @Override
  protected Date getEndDate() {
    return DateUtil.getDateTime(2022, 9, 20);
  }

  @Override
  protected Location getLocation() {
    return Context.getLocationService().getLocation(399);
  }

  @Before
  public void setup() throws Exception {
    executeDataSet("TxPvslCohortQueriesTeste.xml");
  }

  @Override
  protected void setParameters(
      Date startDate, Date endDate, Location location, EvaluationContext context) {
    context.addParameterValue("startDate", startDate);
    context.addParameterValue("endDate", endDate);
    context.addParameterValue("location", location);
  }

  @Test
  public void getWomanWhoAreBreastfeedingShouldNOTpass() throws EvaluationException {
    CohortDefinition cd = txPvlsCohortQueries.getBreastfeedingPatients();

    HashMap<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), getEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cd, parameters);

    assertEquals(2, evaluatedCohort.getMemberIds().size());
    assertFalse(evaluatedCohort.getMemberIds().contains(1002));
  }

  @Test
  public void getNumberOfPatientshouldPass() throws EvaluationException {
    CohortDefinition cd = txPvlsCohortQueries.getBreastfeedingPatients();

    HashMap<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), getEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cd, parameters);

    assertEquals(2, evaluatedCohort.getMemberIds().size());
    assertTrue(evaluatedCohort.getMemberIds().contains(1000));
    assertTrue(evaluatedCohort.getMemberIds().contains(1001));
    assertFalse(evaluatedCohort.getMemberIds().contains(1002));
  }
}
