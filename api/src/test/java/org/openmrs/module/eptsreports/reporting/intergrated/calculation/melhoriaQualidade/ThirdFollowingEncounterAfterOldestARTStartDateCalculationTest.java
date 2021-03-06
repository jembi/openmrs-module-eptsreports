package org.openmrs.module.eptsreports.reporting.intergrated.calculation.melhoriaQualidade;

import java.util.*;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculation;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.eptsreports.reporting.calculation.melhoriaQualidade.ThirdFollowingEncounterAfterOldestARTStartDateCalculation;
import org.openmrs.module.eptsreports.reporting.intergrated.calculation.BasePatientCalculationTest;

public class ThirdFollowingEncounterAfterOldestARTStartDateCalculationTest
    extends BasePatientCalculationTest {

  @Before
  public void init() throws Exception {
    executeDataSet("thirdFollowingEncounterAfterOldestARTStartDateCalculation.xml");
  }

  @Override
  public PatientCalculation getCalculation() {
    return Context.getRegisteredComponents(
            ThirdFollowingEncounterAfterOldestARTStartDateCalculation.class)
        .get(0);
  }

  @Override
  public Collection<Integer> getCohort() {
    return Arrays.asList(new Integer[] {});
  }

  @Override
  public CalculationResultMap getResult() {
    return new CalculationResultMap();
  }

  @Test
  public void evaluateShoulGetThirdApssConsultationAfterThe2ndApssConsultation() {

    PatientCalculationContext context = getEvaluationContext();
    Calendar endDate = DateUtils.truncate(Calendar.getInstance(), Calendar.DAY_OF_MONTH);
    Calendar startDate = DateUtils.truncate(Calendar.getInstance(), Calendar.DAY_OF_MONTH);

    endDate.set(2019, Calendar.DECEMBER, 20);
    startDate.set(2019, Calendar.SEPTEMBER, 21);

    context.addToCache("onOrBefore", endDate.getTime());
    context.addToCache("onOrAfter", startDate.getTime());

    Map<String, Object> parameterValues = new HashMap<>();
    parameterValues.put("lowerBoundary", 20);
    parameterValues.put("upperBoundary", 33);
    parameterValues.put("considerTransferredIn", false);
    parameterValues.put("considerPharmacyEncounter", true);
    final int patientId = 1001;
    CalculationResultMap results =
        service.evaluate(Arrays.asList(patientId), getCalculation(), parameterValues, context);
    SimpleResult result = (SimpleResult) results.get(patientId);
    Assert.assertNotNull(result);

    Calendar expectedResultCalendar =
        DateUtils.truncate(Calendar.getInstance(), Calendar.DAY_OF_MONTH);
    expectedResultCalendar.set(2019, Calendar.DECEMBER, 12);
    Assert.assertEquals(expectedResultCalendar.getTime(), result.getValue());
  }
}
