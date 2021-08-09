package org.openmrs.module.eptsreports.reporting.intergrated.library.cohorts;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.intergrated.utils.DefinitionsTest;
import org.openmrs.module.eptsreports.reporting.library.cohorts.QualityImprovement2020CohortQueries;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;

public class QualityImprovement2020CohortQueriesTest extends DefinitionsTest {

  @Autowired private QualityImprovement2020CohortQueries qualityImprovement2020CohortQueries;

  @Before
  public void setup() throws Exception {
    executeDataSet("qualityImprovement2020.xml");
  }

  @Test
  public void getMQC3D1patientsShouldPass() throws EvaluationException {
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQC3D1();

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
            new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
            this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertNotNull(evaluatedCohort.getMemberIds());
    Set<Integer> patients = evaluatedCohort.getMemberIds();
    for (Integer i : patients) {
      System.out.println(i);
    }
    System.out.println("size:=> " + patients.size());

   assertEquals(3, evaluatedCohort.getMemberIds().size());
   assertTrue(evaluatedCohort.getMemberIds().contains(1013));

    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  @Ignore("Same methods are already tested on MQC3N1")
  public void getMQC3N1patientsShouldPass() throws EvaluationException {
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQC3N1();

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
            new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
            this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

//    assertEquals(1, evaluatedCohort.getMemberIds().size());
//    assertTrue(evaluatedCohort.getMemberIds().contains(1003));

    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  public void getInfantPatientsEnrolledInTarv2020SampleShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ5A(true);

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("locationList", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
//  @Ignore("Same methods are already tested on MQ5Den1")
  public void getPregnantPatientEnrolledInTARV2020ServiceShouldPass() throws EvaluationException {
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ5B(false);

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

//    assertEquals(1, evaluatedCohort.getMemberIds().size());
//    assertTrue(evaluatedCohort.getMemberIds().contains(1003));

      assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  public void getMQ6patientsShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ6A(1);

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  public void getMQ7patientsShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ7A(1);

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  public void getMQ7patientsShouldPass3() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ7A(5);

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  public void getB2_13() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getB2_13();

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  public void getMQ12patientsShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ12DEN(1);

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  @Ignore("Functions used in queries not supported by H2")
  public void getMQ13patientsShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ13(true, 1);

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertNotNull(evaluatedCohort.getMemberIds());
  }

  /** ADDED UNIT TESTS */
  @Test
  public void getMQC4D1ShouldPass() throws EvaluationException {
    System.out.println("...............INITIATING TESTS...............");
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQC4D1();

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertNotNull(evaluatedCohort.getMemberIds());

  }

  @Test
  public void getMQC4D2ShouldPass() throws EvaluationException {
    System.out.println("...............INITIATING TESTS - CAT4 DEN2...............");
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQC4D2();

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertNotNull(evaluatedCohort.getMemberIds());

  }

  @Test
  public void getMQC4N1ShouldPass() throws EvaluationException {
    System.out.println("...............INITIATING TESTS - CAT4 NUM1...............");
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQC4N1();

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertNotNull(evaluatedCohort.getMemberIds());

  }

  // CAT 6 NUMERATOR AND DENOMINATOR
  @Test
  public void getMQ6AShouldPass() throws EvaluationException {
    System.out.println("...............INITIATING TESTS - CAT6 DEN...............");
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ6A(1);

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

     assertNotNull(evaluatedCohort.getMemberIds());

  }

  @Test
  public void getMQ6ShouldPass() throws EvaluationException {
    System.out.println("...............INITIATING TESTS - CAT6 NUM...............");
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ6NUM(1);

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  public void getMQ7AShouldPass() throws EvaluationException {
    System.out.println("...............INITIATING TESTS - CAT7 DEN...............");
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ7A(1);

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertNotNull(evaluatedCohort.getMemberIds());

  }

  @Test
  public void getMQ7BShouldPass() throws EvaluationException {
    System.out.println("...............INITIATING TESTS - CAT7 NUM...............");
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ7B(1);

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertNotNull(evaluatedCohort.getMemberIds());

  }

  @Test
  public void getMQ11DENShouldPass() throws EvaluationException {
    System.out.println("...............INITIATING TESTS - CAT11 DEN...............");
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQC11DEN(2, "MQ");

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertNotNull(evaluatedCohort.getMemberIds());

    Set<Integer> patients = evaluatedCohort.getMemberIds();
    for (Integer i : patients) {
      System.out.println(i);
    }
    System.out.println("size:=> " + patients.size());

  }

  @Test
//  @Ignore("Functions used in queries not supported by H2")
  public void getMQ12NUMShouldPass() throws EvaluationException {
    System.out.println("...............INITIATING TESTS - CAT12 NUM...............");
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ12NUM(1);

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  public void getMQ12DENShouldPass() throws EvaluationException {
    System.out.println("...............INITIATING TESTS - CAT12 DEN...............");
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ12DEN(10);

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertNotNull(evaluatedCohort.getMemberIds());

  }

  @Test
  public void getMQ12P2DENShouldPass() throws EvaluationException {
    System.out.println("...............INITIATING TESTS - CAT12P2 DEN...............");
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ12NumeratorP2(11);

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
            new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
            this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  @Ignore("Functions used in queries not supported by H2")
  public void getMQ13DENShouldPass() throws EvaluationException {
    System.out.println("...............INITIATING TESTS - CAT13 DEN...............");
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ13(true, 1);

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
  }
  @Test
  public void getMQ13P3DENShouldPass() throws EvaluationException {
    System.out.println("...............INITIATING TESTS - CAT13P3 DEN...............");
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQC13P3DEN(2);

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
            new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
            this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertNotNull(evaluatedCohort.getMemberIds());
    Set<Integer> patients = evaluatedCohort.getMemberIds();
    for (Integer i : patients) {
      System.out.println(i);
    }
    System.out.println("size:=> " + patients.size());

  }



  @Test
  @Ignore("Functions used in queries not supported by H2")
  public void getMQ15DENShouldPass() throws EvaluationException {
    System.out.println("...............INITIATING TESTS - CAT15 DEN...............");
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ15DEN(1);

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    // assertEquals(1, evaluatedCohort.getMemberIds().size());
    // assertTrue(evaluatedCohort.getMemberIds().contains(1011));
    assertNotNull(evaluatedCohort.getMemberIds());
    Set<Integer> patients = evaluatedCohort.getMemberIds();
    for (Integer i : patients) {
      System.out.println(i);
    }
    System.out.println("size:=> " + patients.size());
  }

  private Date getrevisionEndDate() {
    return DateUtil.getDateTime(2021, 1, 20);
  }

  @Override
  public Date getStartDate() {
    return DateUtil.getDateTime(2020, 1, 21);
  }

  @Override
  public Date getEndDate() {
    return DateUtil.getDateTime(2020, 4, 20);
  }
}
