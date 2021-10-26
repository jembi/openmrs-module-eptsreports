package org.openmrs.module.eptsreports.reporting.intergrated.library.cohorts;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.intergrated.utils.DefinitionsTest;
import org.openmrs.module.eptsreports.reporting.library.cohorts.QualityImprovement2020CohortQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportConstants;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class QualityImprovement2020CohortQueriesTest extends DefinitionsTest {

  @Autowired private QualityImprovement2020CohortQueries qualityImprovement2020CohortQueries;

  @Before
  public void setup() throws Exception {
    executeDataSet("qualityImprovement2020.xml");
  }

  @Test
  public void getMQC3D1patientsShouldPass() throws EvaluationException {
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQC3D1();

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertEquals(6, evaluatedCohort.getMemberIds().size());
    assertTrue(evaluatedCohort.getMemberIds().contains(1013));
    assertFalse(evaluatedCohort.getMemberIds().contains(1016));
  }

  @Test
  @Ignore("Same methods are already tested on MQC3N1")
  public void getMQC3N1patientsShouldPass() throws EvaluationException {
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQC3N1();

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    //    assertEquals(1, evaluatedCohort.getMemberIds().size());
    //    assertTrue(evaluatedCohort.getMemberIds().contains(1003));

    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  public void getInfantPatientsEnrolledInTarv2020SampleShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ5A(true);

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  @Ignore("Same methods are already tested on MQ5Den1")
  public void getPregnantPatientEnrolledInTARV2020ServiceShouldPass() throws EvaluationException {
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ5B(false);

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    //    assertEquals(1, evaluatedCohort.getMemberIds().size());
    //    assertTrue(evaluatedCohort.getMemberIds().contains(1003));

    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  //  @Ignore("Same methods are already tested on MQ5Den1")
  public void getMQ5BShouldPass() throws EvaluationException {
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ5B(true);

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    //    assertEquals(1, evaluatedCohort.getMemberIds().size());
    //    assertTrue(evaluatedCohort.getMemberIds().contains(1003));

    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  public void getMQ6patientsShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ6A(1);

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
    assertTrue(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1011, 1019, 1012)));
    assertFalse(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1007, 1013, 1017)));
  }

  @Test
  public void getMQ62patientsShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ6A(3);

    Map<Parameter, Object> parameters = getParameters();
    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
    assertTrue(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1013, 1017)));
    assertFalse(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1006, 1008, 1019)));
  }

  @Test
  public void getMQ63patientsShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ6A(4);
    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  public void getMQ7patientsShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ7A(1);

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertNotNull(evaluatedCohort.getMemberIds());
    assertEquals(3, evaluatedCohort.getMemberIds().size());
    assertTrue(evaluatedCohort.getMemberIds().contains(1014));
    assertFalse(evaluatedCohort.getMemberIds().contains(1013));
  }

  @Test
  public void getMQ7patientsShouldPass3() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ7A(5);

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
    assertTrue(evaluatedCohort.getMemberIds().contains(1017));
    assertFalse(evaluatedCohort.getMemberIds().contains(1006));
  }

  @Test
  @Ignore("Functions used in queries not supported by H2")
  public void getMQ7Den2patientsShouldPass3() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ7A(2);

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
  public void getMQ7Den6patientsShouldPass3() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ7A(6);

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

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
    assertTrue(evaluatedCohort.getMemberIds().contains(1015));
    assertFalse(evaluatedCohort.getMemberIds().contains(1019));
  }

  @Test
  public void getMQ12patientsShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ12DEN(1);

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
    assertTrue(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1011, 1012, 1019)));
    assertFalse(evaluatedCohort.getMemberIds().contains(1013));
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

  @Test
  public void getMQC4D1ShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQC4D1();

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
    assertTrue(evaluatedCohort.getMemberIds().contains(1014));
    assertFalse(evaluatedCohort.getMemberIds().contains(1019));
  }

  @Test
  public void getMQC4D2ShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQC4D2();

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertNotNull(evaluatedCohort.getMemberIds());
    assertTrue(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1013, 1017)));
    assertFalse(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1006, 1014)));
  }

  @Test
  public void getMQC4N1ShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQC4N1();

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    System.out.println(evaluatedCohort.getMemberIds());
  }

  @Test
  public void getMQ6AShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ6A(1);

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
    assertTrue(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1011, 1012, 1019)));
    assertFalse(evaluatedCohort.getMemberIds().contains(1017));
  }

  @Test
  public void getMQ6ShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ6NUM(1);

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  public void getMQ6Num2ShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ6NUM(2);

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
    assertTrue(evaluatedCohort.getMemberIds().contains(1011));
    assertFalse(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1006, 1013, 1017)));
  }

  @Test
  public void getMQ6Num3ShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ6NUM(3);

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  public void getMQ6Num4ShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ6NUM(4);

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  public void getMQ7AShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ7A(1);

    Map<Parameter, Object> parameters = getParameters();
    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
    assertTrue(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1012, 1014, 1019)));
    assertFalse(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1007, 1013, 1017)));
  }

  @Test
  public void getMQ7BShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ7B(1);
    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
    assertTrue(evaluatedCohort.getMemberIds().contains(1012));
    assertFalse(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1012, 1013, 1019)));
  }

  @Test
  @Ignore("Functions used in queries not supported by H2")
  public void getMQ7B2ShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ7B(2);

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
  public void getMQ7B5ShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ7B(5);

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  @Ignore("Functions used in queries not supported by H2")
  public void getMQ7B6ShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ7B(6);

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

    CohortDefinition cohortDefinition =
        qualityImprovement2020CohortQueries.getMQC11DEN(2, EptsReportConstants.MIMQ.MQ);

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
    assertTrue(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1014, 1015)));
    assertFalse(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1006, 1013, 1016)));
  }

  @Test
  public void getMQ11DEN1ShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition =
        qualityImprovement2020CohortQueries.getMQC11NumAnotCnotDnotEnotFandGAdultss(
            EptsReportConstants.MIMQ.MQ);

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  public void getMQC11NUN6ShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition =
        qualityImprovement2020CohortQueries.getMQC11NumAnotCnotDnotEnotFnotIlessThan9Month(
            EptsReportConstants.MIMQ.MQ);

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  @Ignore("Functions used in queries not supported by H2")
  public void getMQ12NUMShouldPass() throws EvaluationException {

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

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ12DEN(10);

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
    assertTrue(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1013, 1017)));
    assertFalse(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1011, 1014, 1008)));
  }

  @Test
  public void getMQ12P2DENShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQC12P2DEN(11);

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, getParameters());
    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  @Ignore("Functions used in queries not supported by H2")
  public void getMQ13DENShouldPass() throws EvaluationException {

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
  @Ignore("Functions used in queries not supported by H2")
  public void getMQ13P3DENShouldPass() throws EvaluationException {

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
  }

  @Test
  public void getMQ13P4NUNShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ13P4(true, 3);

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, getParameters());
    assertNotNull(evaluatedCohort.getMemberIds());
    assertTrue(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1014, 1015)));
    assertFalse(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1006, 1011, 1016)));
  }

  @Test
  @Ignore("Functions used in queries not supported by H2")
  public void getMQ15DENShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ15DEN(1);

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
  public void getMQ10DENShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ10Den(true);

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, getParameters());
    assertNotNull(evaluatedCohort.getMemberIds());
    assertTrue(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1011, 1012, 1019)));
    assertFalse(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1013, 1018, 1014)));
  }

  @Test
  public void getMQ10NUMShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ10NUM(1);

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  public void getMQ13G() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ13G();

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
    assertTrue(evaluatedCohort.getMemberIds().contains(1019));
    assertFalse(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1007, 1011, 1018)));
  }

  @Test
  public void getMQC11NH1() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQC11NH1();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, getParameters());
    assertNotNull(evaluatedCohort.getMemberIds());
    assertTrue(evaluatedCohort.getMemberIds().contains(1015));
    assertFalse(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1011, 1014, 1016)));
  }

  @Test
  public void getMQC4N2() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQC4N2();

    Map<Parameter, Object> parameters = getParameters();
    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  public void getMQC11NumAnB3nCnotDnotEnotEnotFnG() throws EvaluationException {
    CohortDefinition cohortDefinition =
        qualityImprovement2020CohortQueries.getMQC11NumAnB3nCnotDnotEnotEnotFnG(
            EptsReportConstants.MIMQ.MQ);
    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, getParameters());
    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  public void getMQC11NumAnotCnotDnotEnotFnotGnChildren() throws EvaluationException {
    CohortDefinition cohortDefinition =
        qualityImprovement2020CohortQueries.getMQC11NumAnotCnotDnotEnotFnotGnChildren(
            EptsReportConstants.MIMQ.MQ);

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, getParameters());
    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  public void getPatientsOnRegimeChangeBI1AndNotB1E_B1() throws EvaluationException {
    CohortDefinition cohortDefinition =
        qualityImprovement2020CohortQueries.getPatientsOnRegimeChangeBI1AndNotB1E_B1();

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
    assertTrue(evaluatedCohort.getMemberIds().contains(1011));
    assertFalse(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1000, 1009, 1012)));
  }

  @Test
  public void getPatientsOnRegimeArvSecondLineB2NEWP1_2() throws EvaluationException {
    CohortDefinition cohortDefinition =
        qualityImprovement2020CohortQueries.getPatientsOnRegimeArvSecondLineB2NEWP1_2();
    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, getParameters());

    assertEquals(1, evaluatedCohort.getMemberIds().size());
    assertTrue(evaluatedCohort.getMemberIds().contains(1018));
  }

  @Test
  public void getPatientsOnRegimeArvSecondLine() throws EvaluationException {
    CohortDefinition cohortDefinition =
        qualityImprovement2020CohortQueries.getPatientsOnRegimeArvSecondLine();

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
    assertTrue(evaluatedCohort.getMemberIds().contains(1018));
    assertFalse(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1011, 1016, 1019, 1006)));
  }

  @Test
  public void getMOHArtStartDate() throws EvaluationException {
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMOHArtStartDate();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, getParameters());
    assertNotNull(evaluatedCohort.getMemberIds());
    assertTrue(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1012, 1013, 1017)));
  }

  @Test
  public void getPatientsFromFichaClinicaWithLastTherapeuticLineSetAsFirstLine_B1()
      throws EvaluationException {
    CohortDefinition cohortDefinition =
        qualityImprovement2020CohortQueries
            .getPatientsFromFichaClinicaWithLastTherapeuticLineSetAsFirstLine_B1();

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
    assertTrue(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1014, 1015, 1017)));
    assertFalse(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1006, 1014, 1016)));
  }

  @Test
  public void getPatientsFromFichaClinicaDenominatorB1EOrB2E() throws EvaluationException {
    CohortDefinition cohortDefinition =
        qualityImprovement2020CohortQueries.getPatientsFromFichaClinicaDenominatorB1EOrB2E(true);

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, getParameters());
    assertNotNull(evaluatedCohort.getMemberIds());
    assertTrue(evaluatedCohort.getMemberIds().contains(1015));
    assertFalse(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1010, 1011, 1014, 1016)));
  }

  @Test
  public void getPatientsFromFichaClinicaWithLastTherapeuticLineSetAsSecondLine_B2()
      throws EvaluationException {
    CohortDefinition cohortDefinition =
        qualityImprovement2020CohortQueries
            .getPatientsFromFichaClinicaWithLastTherapeuticLineSetAsSecondLine_B2();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, getParameters());

    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  public void getMQC13P2DenB2() throws EvaluationException {
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQC13P2DenB2();
    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, getParameters());
    assertNotNull(evaluatedCohort.getMemberIds());
    assertTrue(evaluatedCohort.getMemberIds().contains(1019));
    assertFalse(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1008, 1009, 1018)));
  }

  @Test
  public void getgetMQC13P2DenB4() throws EvaluationException {
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getgetMQC13P2DenB4();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, getParameters());
    assertNotNull(evaluatedCohort.getMemberIds());
    assertTrue(evaluatedCohort.getMemberIds().contains(1019));
    assertFalse(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1011, 1006, 1018)));
  }

  @Test
  public void getgetMQC13P2DenMGInIncluisionPeriod() throws EvaluationException {
    CohortDefinition cohortDefinition =
        qualityImprovement2020CohortQueries.getgetMQC13P2DenMGInIncluisionPeriod();

    Map<Parameter, Object> parameters = getParameters();
    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
    assertTrue(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1013, 1017)));
    assertFalse(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1011, 1012, 1014, 1018)));
  }

  @Test
  public void getMQ10NUMDEN103() throws EvaluationException {
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ10NUMDEN103("den");

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, getParameters());
    assertNotNull(evaluatedCohort.getMemberIds());
  }

  @Test
  public void getMQC13P2Num2() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQC13P2Num2();

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
    assertTrue(evaluatedCohort.getMemberIds().contains(1019));
    assertFalse(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1007, 1011, 1017, 1020)));
  }

  @Test
  public void getMQ9Den() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ9Den(1);

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, getParameters());
    assertNotNull(evaluatedCohort.getMemberIds());
    assertTrue(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1011, 1012, 1019)));
    assertFalse(
        evaluatedCohort
            .getMemberIds()
            .containsAll(Arrays.asList(1010, 1012, 1013, 1014, 1017, 1018)));
  }

  @Test
  public void getPregnantOrBreastfeedingWomen() throws EvaluationException {

    CohortDefinition cohortDefinition =
        qualityImprovement2020CohortQueries.getPregnantOrBreastfeedingWomen();

    Map<Parameter, Object> parameters = getParameters();

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);
    assertNotNull(evaluatedCohort.getMemberIds());
    assertTrue(evaluatedCohort.getMemberIds().contains(1019));
    assertFalse(
        evaluatedCohort
            .getMemberIds()
            .containsAll(Arrays.asList(1006, 1008, 1013, 1014, 1017, 1018)));
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

  private Map<Parameter, Object> getParameters() {

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    return parameters;
  }
}
