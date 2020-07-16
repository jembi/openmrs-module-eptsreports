package org.openmrs.module.eptsreports.reporting.intergrated.calculation.pvls;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculation;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.eptsreports.reporting.calculation.pvls.BreastfeedingDateCalculation;
import org.openmrs.module.eptsreports.reporting.intergrated.calculation.BasePatientCalculationTest;

@Ignore
public class BreastFeedingDateCalculationTest extends BasePatientCalculationTest {

  @Override
  public PatientCalculation getCalculation() {
    return Context.getRegisteredComponents(BreastfeedingDateCalculation.class).get(0);
  }

  @Override
  public Collection<Integer> getCohort() {
    return Arrays.asList(new Integer[] {7, 90, 91, 501});
  }

  @Override
  public CalculationResultMap getResult() {

    PatientCalculation calculation = getCalculation();
    CalculationResultMap map = new CalculationResultMap();

    PatientCalculationContext evaluationContext = getEvaluationContext();
    evaluationContext.setNow(testsHelper.getDate("2019-02-28 00:00:00.0"));

    // Date marked as breastFeeding : 2018-01-21
    map.put(
        501,
        new SimpleResult(
            new Timestamp(testsHelper.getDate("2018-01-21 00:00:00.0").getTime()),
            calculation,
            evaluationContext));
    // BreastfeedingCalculation.hasHIVStartDate(2018-06-20 00:00:00.0, List<Obs>)
    map.put(
        7,
        new SimpleResult(
            new Timestamp(testsHelper.getDate("2018-06-20 00:00:00.0").getTime()),
            calculation,
            evaluationContext));
    // BreastfeedingCalculation.hasDeliveryDate(2018-07-01 00:00:00.0, List<Obs>)
    map.put(
        91,
        new SimpleResult(
            new Timestamp(testsHelper.getDate("2018-07-01 00:00:00.0").getTime()),
            calculation,
            evaluationContext));
    /// BreastfeedingCalculation.isBreastFeedingInProgram(2018-08-10, List<PatientState>)
    map.put(
        90,
        new SimpleResult(
            new Timestamp(testsHelper.getDate("2018-08-30 00:00:00.0").getTime()),
            calculation,
            evaluationContext));
    return map;
  }

  @Before
  public void initialise() throws Exception {
    executeDataSet("pvlsTest.xml");
  }
  @Test
  public void test() {
	  
	   CalculationResultMap calculationResultMap  = 
			   service.evaluate(Arrays.asList(1777001), this.getCalculation());
  }
}
