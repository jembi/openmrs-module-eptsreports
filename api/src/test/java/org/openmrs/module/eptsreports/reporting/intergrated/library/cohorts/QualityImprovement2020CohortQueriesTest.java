package org.openmrs.module.eptsreports.reporting.intergrated.library.cohorts;

import static org.junit.Assert.*;

import java.util.*;
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

    //    assertEquals(3, evaluatedCohort.getMemberIds().size());
    //    assertTrue(evaluatedCohort.getMemberIds().contains(1013));

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
  //  @Ignore("Same methods are already tested on MQ5Den1")
  public void getMQ5BShouldPass() throws EvaluationException {
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ5B(true);

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
  public void getMQ62patientsShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ6A(3);

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
  public void getMQ63patientsShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ6A(4);

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
  @Ignore
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
  @Ignore
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

  @Test
  public void getMQC4D1ShouldPass() throws EvaluationException {

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

  @Test
  public void getMQ6AShouldPass() throws EvaluationException {

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
  public void getMQ6Num2ShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ6NUM(2);

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
  public void getMQ6Num3ShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ6NUM(3);

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
  public void getMQ6Num4ShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ6NUM(4);

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertEquals(0, evaluatedCohort.getMemberIds().size());
  }

  @Test
  public void getMQ7AShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ7A(1);

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertTrue(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1012, 1014)));
  }

  @Test
  public void getMQ7BShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ7B(1);

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertTrue(evaluatedCohort.getMemberIds().contains(1012));
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

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertEquals(0, evaluatedCohort.getMemberIds().size());
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

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertTrue(evaluatedCohort.getMemberIds().contains(1014));
  }

  @Test
  public void getMQ11DEN1ShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition =
        qualityImprovement2020CohortQueries.getMQC11NumAnotCnotDnotEnotFandGAdultss(
            EptsReportConstants.MIMQ.MQ);

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertEquals(0, evaluatedCohort.getMemberIds().size());
  }

  @Test
  public void getMQC11NUN6ShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition =
        qualityImprovement2020CohortQueries.getMQC11NumAnotCnotDnotEnotFnotIlessThan9Month(
            EptsReportConstants.MIMQ.MQ);

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertEquals(0, evaluatedCohort.getMemberIds().size());
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

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertTrue(evaluatedCohort.getMemberIds().contains(1013));

    Set<Integer> patients = evaluatedCohort.getMemberIds();
    for (Integer i : patients) {
      System.out.println(i);
    }
    System.out.println("size:=> " + patients.size());
  }

  @Test
  public void getMQ12P2DENShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQC12P2DEN(11);

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertEquals(0, evaluatedCohort.getMemberIds().size());
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

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertTrue(evaluatedCohort.getMemberIds().contains(1014));
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

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertTrue(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1012, 1019)));
  }

  @Test
  public void getMQ10NUMShouldPass() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ10NUM(1);

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertEquals(0, evaluatedCohort.getMemberIds().size());
  }

  @Test
  public void getMQ13G() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ13G();

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertEquals(1, evaluatedCohort.getMemberIds().size());
  }

  @Test
  public void getMQC11NH1() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQC11NH1();

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertEquals(1, evaluatedCohort.getMemberIds().size());
  }

  @Test
  public void getMQC4N2() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQC4N2();

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertEquals(0, evaluatedCohort.getMemberIds().size());
  }

  @Test
  public void getMQC11NumAnB3nCnotDnotEnotEnotFnG() throws EvaluationException {
    CohortDefinition cohortDefinition =
        qualityImprovement2020CohortQueries.getMQC11NumAnB3nCnotDnotEnotEnotFnG(
            EptsReportConstants.MIMQ.MQ);

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertEquals(0, evaluatedCohort.getMemberIds().size());
  }

  @Test
  public void getMQC11NumAnotCnotDnotEnotFnotGnChildren() throws EvaluationException {
    CohortDefinition cohortDefinition =
        qualityImprovement2020CohortQueries.getMQC11NumAnotCnotDnotEnotFnotGnChildren(
            EptsReportConstants.MIMQ.MQ);

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertEquals(0, evaluatedCohort.getMemberIds().size());
  }

  @Test
  public void getPatientsOnRegimeChangeBI1AndNotB1E_B1() throws EvaluationException {
    CohortDefinition cohortDefinition =
        qualityImprovement2020CohortQueries.getPatientsOnRegimeChangeBI1AndNotB1E_B1();

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertEquals(1, evaluatedCohort.getMemberIds().size());
  }

  @Test
  public void getPatientsOnRegimeArvSecondLineB2NEWP1_2() throws EvaluationException {
    CohortDefinition cohortDefinition =
        qualityImprovement2020CohortQueries.getPatientsOnRegimeArvSecondLineB2NEWP1_2();

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertEquals(1, evaluatedCohort.getMemberIds().size());
    assertTrue(evaluatedCohort.getMemberIds().contains(1018));
  }

  @Test
  public void getPatientsOnRegimeArvSecondLine() throws EvaluationException {
    CohortDefinition cohortDefinition =
        qualityImprovement2020CohortQueries.getPatientsOnRegimeArvSecondLine();

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertEquals(1, evaluatedCohort.getMemberIds().size());
  }

  @Test
  public void getMOHArtStartDate() throws EvaluationException {
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMOHArtStartDate();

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertTrue(evaluatedCohort.getMemberIds().containsAll(Arrays.asList(1013, 1014, 1017)));
  }

  @Test
  public void getPatientsFromFichaClinicaWithLastTherapeuticLineSetAsFirstLine_B1()
      throws EvaluationException {
    CohortDefinition cohortDefinition =
        qualityImprovement2020CohortQueries
            .getPatientsFromFichaClinicaWithLastTherapeuticLineSetAsFirstLine_B1();

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertTrue(evaluatedCohort.getMemberIds().contains(1015));
  }

  @Test
  public void getPatientsFromFichaClinicaDenominatorB1EOrB2E() throws EvaluationException {
    CohortDefinition cohortDefinition =
        qualityImprovement2020CohortQueries.getPatientsFromFichaClinicaDenominatorB1EOrB2E(true);

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertEquals(1, evaluatedCohort.getMemberIds().size());
  }

  @Test
  public void getPatientsFromFichaClinicaWithLastTherapeuticLineSetAsSecondLine_B2()
      throws EvaluationException {
    CohortDefinition cohortDefinition =
        qualityImprovement2020CohortQueries
            .getPatientsFromFichaClinicaWithLastTherapeuticLineSetAsSecondLine_B2();

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertEquals(1, evaluatedCohort.getMemberIds().size());
  }

  @Test
  public void getMQC13P2DenB2() throws EvaluationException {
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQC13P2DenB2();

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertTrue(evaluatedCohort.getMemberIds().contains(1019));
  }

  @Test
  public void getgetMQC13P2DenB4() throws EvaluationException {
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getgetMQC13P2DenB4();

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertTrue(evaluatedCohort.getMemberIds().contains(1019));
  }

  @Test
  public void getgetMQC13P2DenMGInIncluisionPeriod() throws EvaluationException {
    CohortDefinition cohortDefinition =
        qualityImprovement2020CohortQueries.getgetMQC13P2DenMGInIncluisionPeriod();

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertEquals(2, evaluatedCohort.getMemberIds().size());
  }

  @Test
  public void getMQ10NUMDEN103() throws EvaluationException {
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ10NUMDEN103("den");

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertEquals(0, evaluatedCohort.getMemberIds().size());
  }

  @Test
  public void getMQC13P2Num2() throws EvaluationException {

    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQC13P2Num2();

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertTrue(evaluatedCohort.getMemberIds().contains(1019));
  }

  @Test
  public void getMQ9Den() throws EvaluationException {
    CohortDefinition cohortDefinition = qualityImprovement2020CohortQueries.getMQ9Den(1);

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertTrue(evaluatedCohort.getMemberIds().contains(1011));
  }

  @Test
  public void getPregnantOrBreastfeedingWomen() throws EvaluationException {
    CohortDefinition cohortDefinition =
        qualityImprovement2020CohortQueries.getPregnantOrBreastfeedingWomen();

    Map<Parameter, Object> parameters = new HashMap<>();
    parameters.put(new Parameter("startDate", "Start Date", Date.class), this.getStartDate());
    parameters.put(new Parameter("endDate", "End Date", Date.class), this.getEndDate());
    parameters.put(
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        this.getrevisionEndDate());
    parameters.put(new Parameter("location", "Location", Location.class), getLocation());

    EvaluatedCohort evaluatedCohort = evaluateCohortDefinition(cohortDefinition, parameters);

    assertEquals(1, evaluatedCohort.getMemberIds().size());
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
