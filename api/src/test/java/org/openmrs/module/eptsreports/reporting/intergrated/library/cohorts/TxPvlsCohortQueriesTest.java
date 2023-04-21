package org.openmrs.module.eptsreports.reporting.intergrated.library.cohorts;

import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
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

  public void setup() throws Exception {
    new TxPvlsCohortQueries(new HivMetadata(), new CommonMetadata());
    executeDataSet("TxPvlsCohortQueriesTest.xml");
  }

  @Override
  protected Date getEndDate() {
    return DateUtil.getDateTime(2020, 6, 27);
  }

  @Override
  protected Location getLocation() {
    return Context.getLocationService().getLocation(400);
  }

  @Override
  protected void setParameters(
      Date startDate, Date endDate, Location location, EvaluationContext context) {
    context.addParameterValue("endDate", endDate);
    context.addParameterValue("location", location);
  }

  public void getPregnantWoman() throws EvaluationException {

    CohortDefinition cd = txPvlsCohortQueries.getPregnantWoman();

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cd, parameters);

    assertNotNull(evaluatedCohort.getMemberIds());
  }
}
