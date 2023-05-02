package org.openmrs.module.eptsreports.reporting.intergrated.library.cohorts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Ignore;
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

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

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
    // executeDataSet("metadata.xml");
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
  public void getBreastfeedingPatientsShouldPass() throws EvaluationException {
    CohortDefinition cd = txPvlsCohortQueries.getBreastfeedingPatients();

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cd, parameters);

    assertNotNull(evaluatedCohort.getMemberIds());
  }


  @Test
  public void getPatientsWhoAreBreastfeedingShoudPass() throws EvaluationException {

    CohortDefinition cohort = txPvlsCohortQueries.getBreastfeedingPatients();

    Map<Parameter, Object> parameters = new HashMap<>();

    parameters.put(new Parameter("onOrAfter", "onOrAfter", Date.class), this.getStartDate());
    parameters.put(new Parameter("onOrBefore", "onOrBefore", Date.class), this.getEndDate());
    parameters.put(new Parameter("locationList", "Location", Location.class), this.getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohort, parameters);

    assertEquals(2, evaluatedCohort.getMemberIds().size());


    assertTrue(evaluatedCohort.getMemberIds().contains(1000));


    assertFalse(evaluatedCohort.getMemberIds().contains(1002));


    assertTrue(evaluatedCohort.getMemberIds().contains(1001));

  }

  @Test
  @Ignore("Test not supported by H2")
  public void getBreastfeedingShoudPass() throws EvaluationException {

    CohortDefinition cohort = txPvlsCohortQueries.getBreastfeedingPatients();

    Map<Parameter, Object> parameters = new HashMap<>();

    parameters.put(new Parameter("onOrAfter", "onOrAfter", Date.class), this.getStartDate());
    parameters.put(new Parameter("onOrBefore", "onOrBefore", Date.class), this.getEndDate());
    parameters.put(new Parameter("locationList", "Location", Location.class), this.getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohort, parameters);

    assertFalse(evaluatedCohort.getMemberIds().contains(1002));

  }


  @Test
  @Ignore("Test not supported by H2")
  public void getSpecificBreastfeedingPatientShoudPass() throws EvaluationException {

    CohortDefinition cohort = txPvlsCohortQueries.getBreastfeedingPatients();

    Map<Parameter, Object> parameters = new HashMap<>();

    parameters.put(new Parameter("onOrAfter", "onOrAfter", Date.class), this.getStartDate());
    parameters.put(new Parameter("onOrBefore", "onOrBefore", Date.class), this.getEndDate());
    parameters.put(new Parameter("locationList", "Location", Location.class), this.getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohort, parameters);

    assertTrue(evaluatedCohort.getMemberIds().contains(1000));

  }

  @Test
  public void getNumberOfPatientsWhoInitiatedPreTarvByEndOfPreviousMonthA1ShouldPass()
          throws EvaluationException {
    CohortDefinition cd =
            txPvlsCohortQueries.getBreastfeedingPatients();

    HashMap<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), this.getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cd, parameters);

    assertEquals(1, evaluatedCohort.getMemberIds().size());
    assertTrue(evaluatedCohort.getMemberIds().contains(1000));
  }




}
