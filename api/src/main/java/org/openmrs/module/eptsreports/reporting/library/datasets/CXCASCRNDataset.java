package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.Arrays;
import java.util.List;
import org.openmrs.module.eptsreports.reporting.library.cohorts.CXCASCRNCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.dimensions.AgeDimensionCohortInterface;
import org.openmrs.module.eptsreports.reporting.library.dimensions.EptsCommonDimension;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class CXCASCRNDataset extends BaseDataSet {

  @Autowired private EptsCommonDimension eptsCommonDimension;

  @Autowired private EptsGeneralIndicator eptsGeneralIndicator;

  @Autowired private CXCASCRNCohortQueries cxcascrnCohortQueries;

  @Autowired
  @Qualifier("commonAgeDimensionCohort")
  private AgeDimensionCohortInterface ageDimensionCohort;

  public DataSetDefinition constructCXCASCRNDataset() {
    CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
    dsd.setName("CXCASCRN Dataset");
    dsd.addParameters(getParameters());

    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";

    dsd.addDimension(
        "age",
        EptsReportUtils.map(
            eptsCommonDimension.age(ageDimensionCohort), "effectiveDate=${endDate}"));

    /** Total */
    CohortIndicator total =
        eptsGeneralIndicator.getIndicator(
            "TOTAL", EptsReportUtils.map(cxcascrnCohortQueries.getTotal(), mappings));

    dsd.addColumn("TOTAL", "Total Screeeing", EptsReportUtils.map(total, mappings), "");

    /** 1st Time Screened - FTS */
    CohortIndicator f1rstTimeScreened =
        eptsGeneralIndicator.getIndicator(
            "FTS",
            EptsReportUtils.map(cxcascrnCohortQueries.get1stTimeScreenedPatients(), mappings));
    dsd.addColumn("FTS", "1st Time Screened", EptsReportUtils.map(f1rstTimeScreened, mappings), "");

    // 1st Time Screened (NEGATIVE) - FTSN
    CohortIndicator f1rstTimeScreenedNegative =
        eptsGeneralIndicator.getIndicator(
            "FTSN",
            EptsReportUtils.map(
                cxcascrnCohortQueries.get1stTimeScreenedPatientsWithNegativeResult(), mappings));
    addRow(
        dsd,
        "FTSN",
        "1st Time Screened NEGATIVE",
        EptsReportUtils.map(f1rstTimeScreenedNegative, mappings),
        getColumnsForAge());

    // 1st Time Screened (POSITIVE) - FTSP
    CohortIndicator f1rstTimeScreenedPositive =
        eptsGeneralIndicator.getIndicator(
            "FTSP",
            EptsReportUtils.map(
                cxcascrnCohortQueries.get1stTimeScreenedPatientsWithPositiveResult(), mappings));
    addRow(
        dsd,
        "FTSP",
        "1st Time Screened POSITIVE",
        EptsReportUtils.map(f1rstTimeScreenedPositive, mappings),
        getColumnsForAge());

    // 1st Time Screened (SUSPECTED) - FTSS
    CohortIndicator f1rstTimeScreenedSuspected =
        eptsGeneralIndicator.getIndicator(
            "FTSS",
            EptsReportUtils.map(
                cxcascrnCohortQueries.get1stTimeScreenedPatientsWithSuspectedCancerResult(),
                mappings));
    addRow(
        dsd,
        "FTSS",
        "1st Time Screened SUSPECTED",
        EptsReportUtils.map(f1rstTimeScreenedSuspected, mappings),
        getColumnsForAge());

    /** Rescreened after previous negative - RAPN */
    CohortIndicator rescreenedAfterPreviousNegative =
        eptsGeneralIndicator.getIndicator(
            "RAPN",
            EptsReportUtils.map(
                cxcascrnCohortQueries.getPatientsRescreenedAfterPreviousNegative(), mappings));
    dsd.addColumn(
        "RAPN",
        "Rescreened after previous negative",
        EptsReportUtils.map(rescreenedAfterPreviousNegative, mappings),
        "");

    // Rescreened after previous negative (NEGATIVE) - RAPNN
    CohortIndicator rescreenedAfterPreviousNegativeNegative =
        eptsGeneralIndicator.getIndicator(
            "RAPNN",
            EptsReportUtils.map(
                cxcascrnCohortQueries
                    .getPatientsRescreenedAfterPreviousNegativeWithNegativeResutOnReportingPeriod(),
                mappings));
    addRow(
        dsd,
        "RAPNN",
        "Rescreened after previous negative NEGATIVE",
        EptsReportUtils.map(rescreenedAfterPreviousNegativeNegative, mappings),
        getColumnsForAge());

    // Rescreened after previous negative (POSITIVE) - RAPNP
    CohortIndicator rescreenedAfterPreviousNegativePositive =
        eptsGeneralIndicator.getIndicator(
            "RAPNP",
            EptsReportUtils.map(
                cxcascrnCohortQueries
                    .getPatientsRescreenedAfterPreviousNegativeWithPositiveResutOnReportingPeriod(),
                mappings));
    addRow(
        dsd,
        "RAPNP",
        "Rescreened after previous negative POSITIVE",
        EptsReportUtils.map(rescreenedAfterPreviousNegativePositive, mappings),
        getColumnsForAge());

    // Rescreened after previous negative (SUSPECTED) - RAPNS
    CohortIndicator rescreenedAfterPreviousNegativeSuspected =
        eptsGeneralIndicator.getIndicator(
            "RAPNS",
            EptsReportUtils.map(
                cxcascrnCohortQueries
                    .getPatientsRescreenedAfterPreviousNegativeWithSuspectedCancerResutOnReportingPeriod(),
                mappings));
    addRow(
        dsd,
        "RAPNS",
        "Rescreened after previous negative SUSPECTED",
        EptsReportUtils.map(rescreenedAfterPreviousNegativeSuspected, mappings),
        getColumnsForAge());

    /** Post-Treatment follow-up - PTFU */
    CohortIndicator postTreatmentFollowUp =
        eptsGeneralIndicator.getIndicator(
            "PTFU",
            EptsReportUtils.map(
                cxcascrnCohortQueries.getPatientsWithPostTreatmentFollowUp(), mappings));
    dsd.addColumn(
        "PTFU",
        "Post-Treatment follow-up",
        EptsReportUtils.map(postTreatmentFollowUp, mappings),
        "");

    // Post-Treatment follow-up (NEGATIVE) - PTFUN
    CohortIndicator postTreatmentFollowUpNegative =
        eptsGeneralIndicator.getIndicator(
            "PTFUN",
            EptsReportUtils.map(
                cxcascrnCohortQueries.getPatientsWithPostTreatmentFollowUpWithNegativeResult(),
                mappings));
    addRow(
        dsd,
        "PTFUN",
        "Post-Treatment follow-up NEGATIVE",
        EptsReportUtils.map(postTreatmentFollowUpNegative, mappings),
        getColumnsForAge());

    // Post-Treatment follow-up (POSITIVE) - PTFUP
    CohortIndicator postTreatmentFollowUpPositive =
        eptsGeneralIndicator.getIndicator(
            "PTFUP",
            EptsReportUtils.map(
                cxcascrnCohortQueries.getPatientsWithPostTreatmentFollowUpAndHavePositiveResult(),
                mappings));
    addRow(
        dsd,
        "PTFUP",
        "Post-Treatment follow-up POSITIVE",
        EptsReportUtils.map(postTreatmentFollowUpPositive, mappings),
        getColumnsForAge());

    // Post-Treatment follow-up (SUSPECTED) - PTFUS
    CohortIndicator postTreatmentFollowUpSuspected =
        eptsGeneralIndicator.getIndicator(
            "PTFUS",
            EptsReportUtils.map(
                cxcascrnCohortQueries
                    .getPatientsWithPostTreatmentFollowUpWithSuspectedCancerResult(),
                mappings));
    addRow(
        dsd,
        "PTFUS",
        "Post-Treatment follow-up SUSPECTED",
        EptsReportUtils.map(postTreatmentFollowUpSuspected, mappings),
        getColumnsForAge());

    /** Rescreened after previous positive - RAPP */
    CohortIndicator rescreenedAfterPreviousPositive =
        eptsGeneralIndicator.getIndicator(
            "RAPP",
            EptsReportUtils.map(
                cxcascrnCohortQueries.getPatientsWhoRescreenedAfterPreviousPositive(), mappings));
    dsd.addColumn(
        "RAPP",
        "Rescreened after previous positive",
        EptsReportUtils.map(rescreenedAfterPreviousPositive, mappings),
        "");

    // Rescreened after previous positive (NEGATIVE) - RAPPN
    CohortIndicator rescreenedAfterPreviousPositiveNegative =
        eptsGeneralIndicator.getIndicator(
            "RAPPN",
            EptsReportUtils.map(
                cxcascrnCohortQueries
                    .getPatientsWhoRescreenedAfterPreviousPositiveAndWithNegativeResult(),
                mappings));
    addRow(
        dsd,
        "RAPPN",
        "Rescreened After Previous Positive Negative",
        EptsReportUtils.map(rescreenedAfterPreviousPositiveNegative, mappings),
        getColumnsForAge());

    // Rescreened after previous positive (POSITIVE) - RAPPP
    CohortIndicator rescreenedAfterPreviousPositivePositive =
        eptsGeneralIndicator.getIndicator(
            "RAPPP",
            EptsReportUtils.map(
                cxcascrnCohortQueries
                    .getPatientsWhoRescreenedAfterPreviousPositiveAndWithPositiveResult(),
                mappings));
    addRow(
        dsd,
        "RAPPP",
        "Rescreened after previous positive POSITIVE",
        EptsReportUtils.map(rescreenedAfterPreviousPositivePositive, mappings),
        getColumnsForAge());

    // Rescreened after previous positive (SUSPECTED) - RAPPS
    CohortIndicator rescreenedAfterPreviousPositiveSuspected =
        eptsGeneralIndicator.getIndicator(
            "RAPPS",
            EptsReportUtils.map(
                cxcascrnCohortQueries
                    .getPatientsWhoRescreenedAfterPreviousPositiveAndWithSuspectedCancerResult(),
                mappings));
    addRow(
        dsd,
        "RAPPS",
        "Rescreened after previous positive SUSPECTED",
        EptsReportUtils.map(rescreenedAfterPreviousPositiveSuspected, mappings),
        getColumnsForAge());

    return dsd;
  }

  /**
   * <b>Description:</b> Creates Desagregation based on Age and Gender
   *
   * @return
   */
  private List<ColumnParameters> getColumnsForAge() {

    ColumnParameters fifteenTo19 =
        new ColumnParameters("fifteenTo19", "15 - 19", "age=15-19", "01");
    ColumnParameters twentyTo24 = new ColumnParameters("twentyTo24", "20 - 24", "age=20-24", "02");
    ColumnParameters twenty5To29 =
        new ColumnParameters("twenty4To29", "25 - 29", "age=25-29", "03");
    ColumnParameters thirtyTo34 = new ColumnParameters("thirtyTo34", "30 - 34", "age=30-34", "04");
    ColumnParameters thirty5To39 =
        new ColumnParameters("thirty5To39", "35 - 39", "age=35-39", "05");
    ColumnParameters foutyTo44 = new ColumnParameters("foutyTo44", "40 - 44", "age=40-44", "06");
    ColumnParameters fouty5To49 = new ColumnParameters("fouty5To49", "45 - 49", "age=45-49", "07");
    ColumnParameters fiftyTo54 = new ColumnParameters("fiftyTo54", "50 - 54", "age=50-54", "08");
    ColumnParameters fifty5To59 = new ColumnParameters("fifty5To59", "55 - 59", "age=55-59", "09");
    ColumnParameters sixtyTo64 = new ColumnParameters("sixtyTo64", "60 - 64", "age=60-64", "10");
    ColumnParameters above65 = new ColumnParameters("above65", "65+", "age=65+", "11");
    ColumnParameters unknown = new ColumnParameters("unknown", "Unknown age", "age=UK", "12");
    ColumnParameters total = new ColumnParameters("totals", "Totals", "", "13");

    return Arrays.asList(
        fifteenTo19,
        twentyTo24,
        twenty5To29,
        thirtyTo34,
        thirty5To39,
        foutyTo44,
        fouty5To49,
        fiftyTo54,
        fifty5To59,
        sixtyTo64,
        above65,
        unknown,
        total);
  }
}
