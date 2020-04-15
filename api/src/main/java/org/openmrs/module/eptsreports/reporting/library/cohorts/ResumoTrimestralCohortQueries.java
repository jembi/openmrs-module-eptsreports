package org.openmrs.module.eptsreports.reporting.library.cohorts;

import static org.openmrs.module.reporting.evaluation.parameter.Mapped.mapStraightThrough;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.cohort.definition.EptsQuarterlyCohortDefinition;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.AllPatientsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResumoTrimestralCohortQueries {

  private GenericCohortQueries genericCohortQueries;

  private HivCohortQueries hivCohortQueries;

  @Autowired
  public ResumoTrimestralCohortQueries(
      GenericCohortQueries genericCohortQueries, HivCohortQueries hivCohortQueries) {
    this.genericCohortQueries = genericCohortQueries;
    this.hivCohortQueries = hivCohortQueries;
  }

  /** @return Nº de pacientes que iniciou TARV nesta unidade sanitária durante o mês */
  public CohortDefinition getA() {
    CohortDefinition startedArt = genericCohortQueries.getStartedArtOnPeriod(false, true);
    CohortDefinition transferredIn =
        hivCohortQueries.getPatientsTransferredFromOtherHealthFacility();
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    cd.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addSearch("startedArt", mapStraightThrough(startedArt));
    cd.addSearch("transferredIn", mapStraightThrough(transferredIn));
    cd.setCompositionString("startedArt NOT transferredIn");
    return cd;
  }

  /** @return Nº de pacientes Transferidos de (+) outras US em TARV durante o mês */
  public CohortDefinition getB() {
    CohortDefinition startedArt = genericCohortQueries.getStartedArtOnPeriod(false, true);
    CohortDefinition transferredIn =
        hivCohortQueries.getPatientsTransferredFromOtherHealthFacility();
    CompositionCohortDefinition wrap = new CompositionCohortDefinition();
    wrap.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    wrap.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    wrap.addParameter(new Parameter("location", "location", Location.class));
    wrap.addSearch("startedArt", mapStraightThrough(startedArt));
    wrap.addSearch("transferredIn", mapStraightThrough(transferredIn));
    wrap.setCompositionString("startedArt AND transferredIn");
    return wrap;
  }

  /** @return Nº de pacientes Transferidos para (-) outras US em TARV durante o mês */
  public CohortDefinition getC() {
    CohortDefinition startedArt = genericCohortQueries.getStartedArtOnPeriod(false, true);
    CohortDefinition transferredOut = hivCohortQueries.getPatientsTransferredOut();
    CompositionCohortDefinition wrap = new CompositionCohortDefinition();
    wrap.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    wrap.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    wrap.addParameter(new Parameter("location", "location", Location.class));
    wrap.addSearch("startedArt", mapStraightThrough(startedArt));
    wrap.addSearch("transferredOut", mapStraightThrough(transferredOut));
    wrap.setCompositionString("startedArt AND transferredOut");
    return wrap;
  }

  /** @return Number of patients who is in the 1st line treatment during the cohort month */
  public CohortDefinition getE() {
    AllPatientsCohortDefinition cd = new AllPatientsCohortDefinition();
    cd.setParameters(getParameters());
    return cd;
  }

  /**
   * @return Number of patients in Cohort who completed 12 months ARV treatment in the 1st line
   *     treatment who received one Viral load result
   */
  public CohortDefinition getF() {
    AllPatientsCohortDefinition cd = new AllPatientsCohortDefinition();
    cd.setParameters(getParameters());
    return cd;
  }

  /** @return Number of patients who is in the 2nd line treatment during the cohort month */
  public CohortDefinition getG() {
    AllPatientsCohortDefinition cd = new AllPatientsCohortDefinition();
    cd.setParameters(getParameters());
    return cd;
  }

  /**
   * @return Number of patients in Cohort who completed 12 months ARV treatment in the 2nd line
   *     treatment who received one Viral load result
   */
  public CohortDefinition getH() {
    AllPatientsCohortDefinition cd = new AllPatientsCohortDefinition();
    cd.setParameters(getParameters());
    return cd;
  }

  /** @return Number of Suspended patients in the actual cohort */
  public CohortDefinition getI() {
    AllPatientsCohortDefinition cd = new AllPatientsCohortDefinition();
    cd.setParameters(getParameters());
    return cd;
  }

  /** @return Number of Abandoned Patients in the actual cohort */
  public CohortDefinition getJ() {
    AllPatientsCohortDefinition cd = new AllPatientsCohortDefinition();
    cd.setParameters(getParameters());
    return cd;
  }

  /** @return Number of Deceased patients in the actual cohort */
  public CohortDefinition getL() {
    AllPatientsCohortDefinition cd = new AllPatientsCohortDefinition();
    cd.setParameters(getParameters());
    return cd;
  }

  private List<Parameter> getParameters() {
    return Arrays.asList(
        new Parameter("onOrAfter", "Start date", Date.class),
        new Parameter("onOrBefore", "End date", Date.class),
        new Parameter("location", "Location", Location.class));
  }

  /** @return ((A+B) - C) */
  public CohortDefinition getD(List<Parameter> getParameters) {
    CompositionCohortDefinition cdA = new CompositionCohortDefinition();
    cdA.setName("Indicators A");
    cdA.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    cdA.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    cdA.addParameter(new Parameter("location", "location", Location.class));
    cdA.addSearch(
        "startedArtA",
        EptsReportUtils.map(
            genericCohortQueries.getStartedArtOnPeriod(false, true),
            "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));
    cdA.addSearch(
        "transferredInA",
        EptsReportUtils.map(
            hivCohortQueries.getPatientsTransferredFromOtherHealthFacility(),
            "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));
    cdA.setCompositionString("startedArtA AND NOT transferredInA");
    // get indicators B
    CompositionCohortDefinition cdB = new CompositionCohortDefinition();
    cdB.setName("indicators B");
    cdB.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    cdB.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    cdB.addParameter(new Parameter("location", "location", Location.class));
    cdB.addSearch(
        "startedArtB",
        EptsReportUtils.map(
            genericCohortQueries.getStartedArtOnPeriod(false, true),
            "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));
    cdB.addSearch(
        "transferredInB",
        EptsReportUtils.map(
            hivCohortQueries.getPatientsTransferredFromOtherHealthFacility(),
            "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));
    cdB.setCompositionString("startedArtB AND transferredInB");
    // get indicators C
    CompositionCohortDefinition cdC = new CompositionCohortDefinition();
    cdC.setName("indicator C");
    cdC.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    cdC.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    cdC.addParameter(new Parameter("location", "location", Location.class));
    cdC.addSearch(
        "startedArtC",
        EptsReportUtils.map(
            genericCohortQueries.getStartedArtOnPeriod(false, true),
            "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));
    cdC.addSearch(
        "transferredOutC",
        EptsReportUtils.map(
            hivCohortQueries.getPatientsTransferredOut(),
            "onOrBefore=${onOrBefore},location=${location}"));
    cdC.setCompositionString("startedArtC AND transferredOutC");

    // create another composition to combine the quarter
    CompositionCohortDefinition wrap = new CompositionCohortDefinition();
    wrap.setName("Combine values for the quarter - D");
    wrap.addParameters(getParameters());
    wrap.addSearch(
        "A1",
        EptsReportUtils.map(
            getQuarterlyCohort(getParameters, cdA, EptsQuarterlyCohortDefinition.Month.M1),
            "year=${year},quarter=${quarter},location=${location}"));
    wrap.addSearch(
        "A2",
        EptsReportUtils.map(
            getQuarterlyCohort(getParameters, cdA, EptsQuarterlyCohortDefinition.Month.M2),
            "year=${year},quarter=${quarter},location=${location}"));
    wrap.addSearch(
        "A3",
        EptsReportUtils.map(
            getQuarterlyCohort(getParameters, cdA, EptsQuarterlyCohortDefinition.Month.M3),
            "year=${year},quarter=${quarter},location=${location}"));

    wrap.addSearch(
        "B1",
        EptsReportUtils.map(
            getQuarterlyCohort(getParameters, cdB, EptsQuarterlyCohortDefinition.Month.M1),
            "year=${year},quarter=${quarter},location=${location}"));
    wrap.addSearch(
        "B2",
        EptsReportUtils.map(
            getQuarterlyCohort(getParameters, cdB, EptsQuarterlyCohortDefinition.Month.M2),
            "year=${year},quarter=${quarter},location=${location}"));
    wrap.addSearch(
        "B3",
        EptsReportUtils.map(
            getQuarterlyCohort(getParameters, cdB, EptsQuarterlyCohortDefinition.Month.M3),
            "year=${year},quarter=${quarter},location=${location}"));
    wrap.addSearch(
        "C1",
        EptsReportUtils.map(
            getQuarterlyCohort(getParameters, cdC, EptsQuarterlyCohortDefinition.Month.M1),
            "year=${year},quarter=${quarter},location=${location}"));
    wrap.addSearch(
        "C2",
        EptsReportUtils.map(
            getQuarterlyCohort(getParameters, cdC, EptsQuarterlyCohortDefinition.Month.M2),
            "year=${year},quarter=${quarter},location=${location}"));
    wrap.addSearch(
        "C3",
        EptsReportUtils.map(
            getQuarterlyCohort(getParameters, cdC, EptsQuarterlyCohortDefinition.Month.M3),
            "year=${year},quarter=${quarter},location=${location}"));

    wrap.setCompositionString("(A1 OR A2 OR A3 OR B1 OR B2 OR B3) AND NOT (C1 OR C2 OR C3)");

    return wrap;
  }

  public EptsQuarterlyCohortDefinition getQuarterlyCohort(
      List<Parameter> getParameters,
      CohortDefinition wrap,
      EptsQuarterlyCohortDefinition.Month month) {
    EptsQuarterlyCohortDefinition cd = new EptsQuarterlyCohortDefinition(wrap, month);
    cd.addParameters(getParameters);
    return cd;
  }
}
