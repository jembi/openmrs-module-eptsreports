package org.openmrs.module.eptsreports.reporting.intergrated.library.cohorts;

import static org.junit.Assert.*;
import java.util.*;
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
  private EvaluationContext context;

  @Before
  public void setup() throws Exception {
    executeDataSet("TxPvlsCohortQueriesTest.xml");
  }

  @Override
  protected Date getEndDate() {
    return DateUtil.getDateTime(2022, 6, 27);
  }

  @Override
  protected Location getLocation() {
    return Context.getLocationService().getLocation(399);
  }


  @Override
  protected void setParameters(
      Date startDate, Date endDate, Location location, EvaluationContext context) {
    context.addParameterValue("startDate", startDate);

    context.addParameterValue("endDate", endDate);
    context.addParameterValue("location", location);
  }

  @Test

  public void getPregnantWomanShouldPassAny() throws EvaluationException {

    CohortDefinition cd = txPvlsCohortQueries.getPregnantWoman();

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("endDate", "End Date", Date.class),getEndDate());
    parameters.put(new Parameter("location", "Location", Location.class),getLocation());
    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cd, parameters);

    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  public void getPregnantWomanShouldFail() throws EvaluationException {

    CohortDefinition cd = txPvlsCohortQueries.getPregnantWoman();

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("endDate", "End Date", Date.class),getEndDate());

    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cd, parameters);

    assertEquals(1, evaluatedCohort.getMemberIds().size());
    assertFalse(evaluatedCohort.getMemberIds().contains(1022));
  }

  @Test
  public void getPregnantWomanShouldPass() throws EvaluationException {

    CohortDefinition cd = txPvlsCohortQueries.getPregnantWoman();

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("endDate", "End Date", Date.class),getEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cd, parameters);

    assertEquals(1, evaluatedCohort.getMemberIds().size());
    assertTrue(evaluatedCohort.getMemberIds().contains(1020));

  }
}
