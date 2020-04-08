package org.openmrs.module.eptsreports.reporting.library.datasets;

import static org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils.map;
import static org.openmrs.module.reporting.evaluation.parameter.Mapped.mapStraightThrough;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.cohort.definition.EptsQuarterlyCohortDefinition;
import org.openmrs.module.eptsreports.reporting.library.cohorts.GenericCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.HivCohortQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResumoTrimestralDataSetDefinition extends BaseDataSet {

  public static final String NO_DIMENSION_OPTIONS = "";

  public static final String A =
      "Nº de pacientes que iniciou TARV nesta unidade sanitária durante o mês";

  private static final String B =
      "Nº de pacientes Transferidos de (+) outras US em TARV durante o mês";

  private static final String C =
      "Nº de pacientes Transferidos para (-) outras US em TARV durante o mês";

  private static final String D = "Actual Cohort during the month((A+B) - C) - Total";

  private GenericCohortQueries genericCohortQueries;

  private HivCohortQueries hivCohortQueries;

  @Autowired
  public ResumoTrimestralDataSetDefinition(
      GenericCohortQueries genericCohortQueries, HivCohortQueries hivCohortQueries) {
    this.genericCohortQueries = genericCohortQueries;
    this.hivCohortQueries = hivCohortQueries;
  }

  public DataSetDefinition constructResumoTrimestralDataset() {
    CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
    dsd.setName("Resumo trimestral data set");
    dsd.addParameters(getParameters());
    dsd.addColumn("Am1", A, getA(EptsQuarterlyCohortDefinition.Month.M1), NO_DIMENSION_OPTIONS);
    dsd.addColumn("Am2", A, getA(EptsQuarterlyCohortDefinition.Month.M2), NO_DIMENSION_OPTIONS);
    dsd.addColumn("Am3", A, getA(EptsQuarterlyCohortDefinition.Month.M3), NO_DIMENSION_OPTIONS);
    dsd.addColumn("Bm1", B, getB(EptsQuarterlyCohortDefinition.Month.M1), NO_DIMENSION_OPTIONS);
    dsd.addColumn("Bm2", B, getB(EptsQuarterlyCohortDefinition.Month.M2), NO_DIMENSION_OPTIONS);
    dsd.addColumn("Bm3", B, getB(EptsQuarterlyCohortDefinition.Month.M3), NO_DIMENSION_OPTIONS);
    dsd.addColumn("Cm1", C, getC(EptsQuarterlyCohortDefinition.Month.M1), NO_DIMENSION_OPTIONS);
    dsd.addColumn("Cm2", C, getC(EptsQuarterlyCohortDefinition.Month.M2), NO_DIMENSION_OPTIONS);
    dsd.addColumn("Cm3", C, getC(EptsQuarterlyCohortDefinition.Month.M3), NO_DIMENSION_OPTIONS);
    dsd.addColumn(
        "DmT",
        D,
        map(
            getCohortIndicator(
                "DmT", map(getD(), "year=${year-1},quarter=${quarter},location=${location}")),
            "year=${year},quarter=${quarter},location=${location}"),
        NO_DIMENSION_OPTIONS);
    return dsd;
  }

  private Mapped<CohortIndicator> getA(EptsQuarterlyCohortDefinition.Month month) {
    CohortDefinition startedArt = genericCohortQueries.getStartedArtOnPeriod(false, true);
    CohortDefinition transferredIn =
        hivCohortQueries.getPatientsTransferredFromOtherHealthFacility();
    CompositionCohortDefinition wrap = new CompositionCohortDefinition();
    wrap.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    wrap.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    wrap.addParameter(new Parameter("location", "location", Location.class));
    wrap.addSearch("startedArt", mapStraightThrough(startedArt));
    wrap.addSearch("transferredIn", mapStraightThrough(transferredIn));
    wrap.setCompositionString("startedArt NOT transferredIn");
    CohortDefinition quarterly = getQuarterlyCohort(wrap, month);
    String mappings = "year=${year-1},quarter=${quarter},location=${location}";
    return mapStraightThrough(getCohortIndicator(A, map(quarterly, mappings)));
  }

  private Mapped<CohortIndicator> getB(EptsQuarterlyCohortDefinition.Month month) {
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
    CohortDefinition quarterly = getQuarterlyCohort(wrap, month);
    String mappings = "year=${year-1},quarter=${quarter},location=${location}";
    return mapStraightThrough(getCohortIndicator(B, map(quarterly, mappings)));
  }

  private Mapped<CohortIndicator> getC(EptsQuarterlyCohortDefinition.Month month) {
    CohortDefinition startedArt = genericCohortQueries.getStartedArtOnPeriod(false, true);
    CohortDefinition transferredOut = hivCohortQueries.getPatientsTransferredOut();
    CompositionCohortDefinition wrap = new CompositionCohortDefinition();
    wrap.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    wrap.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    wrap.addParameter(new Parameter("location", "location", Location.class));
    wrap.addSearch("startedArt", mapStraightThrough(startedArt));
    wrap.addSearch("transferredOut", mapStraightThrough(transferredOut));
    wrap.setCompositionString("startedArt AND transferredOut");
    CohortDefinition quarterly = getQuarterlyCohort(wrap, month);
    String mappings = "year=${year-1},quarter=${quarter},location=${location}";
    return mapStraightThrough(getCohortIndicator(C, map(quarterly, mappings)));
  }

  private CohortIndicator getCohortIndicator(String name, Mapped<CohortDefinition> cohort) {
    CohortIndicator indicator = new CohortIndicator(name);
    indicator.setCohortDefinition(cohort);
    indicator.addParameters(getParameters());
    return indicator;
  }

  private EptsQuarterlyCohortDefinition getQuarterlyCohort(
      CohortDefinition wrap, EptsQuarterlyCohortDefinition.Month month) {
    EptsQuarterlyCohortDefinition cd = new EptsQuarterlyCohortDefinition(wrap, month);
    cd.addParameters(getParameters());
    return cd;
  }

  @Override
  public List<Parameter> getParameters() {
    List<Parameter> parameters = new ArrayList<>();
    parameters.add(new Parameter("year", "Year", Integer.class));
    parameters.add(
        new Parameter("quarter", "Quarter", EptsQuarterlyCohortDefinition.Quarter.class));
    parameters.add(ReportingConstants.LOCATION_PARAMETER);
    return parameters;
  }

  private CohortDefinition getD() {
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
            getQuarterlyCohort(cdA, EptsQuarterlyCohortDefinition.Month.M1),
            "year=${year},quarter=${quarter},location=${location}"));
    wrap.addSearch(
        "A2",
        EptsReportUtils.map(
            getQuarterlyCohort(cdA, EptsQuarterlyCohortDefinition.Month.M2),
            "year=${year},quarter=${quarter},location=${location}"));
    wrap.addSearch(
        "A3",
        EptsReportUtils.map(
            getQuarterlyCohort(cdA, EptsQuarterlyCohortDefinition.Month.M3),
            "year=${year},quarter=${quarter},location=${location}"));

    wrap.addSearch(
        "B1",
        EptsReportUtils.map(
            getQuarterlyCohort(cdB, EptsQuarterlyCohortDefinition.Month.M1),
            "year=${year},quarter=${quarter},location=${location}"));
    wrap.addSearch(
        "B2",
        EptsReportUtils.map(
            getQuarterlyCohort(cdB, EptsQuarterlyCohortDefinition.Month.M2),
            "year=${year},quarter=${quarter},location=${location}"));
    wrap.addSearch(
        "B3",
        EptsReportUtils.map(
            getQuarterlyCohort(cdB, EptsQuarterlyCohortDefinition.Month.M3),
            "year=${year},quarter=${quarter},location=${location}"));
    wrap.addSearch(
        "C1",
        EptsReportUtils.map(
            getQuarterlyCohort(cdC, EptsQuarterlyCohortDefinition.Month.M1),
            "year=${year},quarter=${quarter},location=${location}"));
    wrap.addSearch(
        "C2",
        EptsReportUtils.map(
            getQuarterlyCohort(cdC, EptsQuarterlyCohortDefinition.Month.M2),
            "year=${year},quarter=${quarter},location=${location}"));
    wrap.addSearch(
        "C3",
        EptsReportUtils.map(
            getQuarterlyCohort(cdC, EptsQuarterlyCohortDefinition.Month.M3),
            "year=${year},quarter=${quarter},location=${location}"));

    wrap.setCompositionString("(A1 OR A2 OR A3 OR B1 OR B2 OR B3) AND NOT (C1 OR C2 OR C3)");

    return wrap;
  }
}
