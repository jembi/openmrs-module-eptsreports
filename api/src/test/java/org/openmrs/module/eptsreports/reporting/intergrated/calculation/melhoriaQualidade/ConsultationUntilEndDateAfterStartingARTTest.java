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
import org.openmrs.module.eptsreports.reporting.calculation.melhoriaQualidade.ConsultationUntilEndDateAfterStartingART;
import org.openmrs.module.eptsreports.reporting.intergrated.calculation.BasePatientCalculationTest;

public class ConsultationUntilEndDateAfterStartingARTTest extends BasePatientCalculationTest {

  @Before
  public void init() throws Exception {
    executeDataSet("consultationUntilEndDateAfterStartingART.xml");
  }

  @Override
  public PatientCalculation getCalculation() {
    return Context.getRegisteredComponents(ConsultationUntilEndDateAfterStartingART.class).get(0);
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

    endDate.set(2019, Calendar.DECEMBER, 20);

    context.addToCache("onOrBefore", endDate.getTime());

    Map<String, Object> parameterValues = new HashMap<>();

    parameterValues.put("considerTransferredIn", false);
    parameterValues.put("considerPharmacyEncounter", true);
    final int patientId = 1001;
    CalculationResultMap results =
        service.evaluate(Arrays.asList(patientId), getCalculation(), parameterValues, context);
    SimpleResult result = (SimpleResult) results.get(patientId);
    Assert.assertNotNull(result);

    Assert.assertFalse(result.isEmpty());
    Assert.assertTrue((Boolean) result.getValue());
  }
}
