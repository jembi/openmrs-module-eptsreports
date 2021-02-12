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

  @Autowired
  @Qualifier("commonAgeDimensionCohort")
  private AgeDimensionCohortInterface ageDimensionCohort;

  @Autowired private EptsGeneralIndicator eptsGeneralIndicator;

  @Autowired private CXCASCRNCohortQueries cxcascrnCohortQueries;

  private final String MAPPING = "startDate=${startDate},endDate=${endDate},location=${location}";

  public DataSetDefinition constructCXCASCRNDataset() {
    CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();

    dsd.addDimension(
        "age",
        EptsReportUtils.map(
            eptsCommonDimension.age(ageDimensionCohort), "effectiveDate=${endDate}"));

    // Total
    CohortIndicator total =
        eptsGeneralIndicator.getIndicator(
            "TOTAL", EptsReportUtils.map(cxcascrnCohortQueries.getTotal(), MAPPING));

    dsd.addColumn("TOTAL", "Total", EptsReportUtils.map(total, MAPPING), "");

    /** 1st Time Screened - FTS */
    CohortIndicator f1rstTimeScreened =
        eptsGeneralIndicator.getIndicator(
            "FTS",
            EptsReportUtils.map(
                cxcascrnCohortQueries.get1stTimeScreened(CXCASCRNCohortQueries.CXCASCRNResult.ALL),
                MAPPING));
    dsd.addColumn("FTS", "1st Time Screened", EptsReportUtils.map(f1rstTimeScreened, MAPPING), "");

    // 1st Time Screened NEGATIVE - FTSN
    CohortIndicator f1rstTimeScreenedNegative =
        eptsGeneralIndicator.getIndicator(
            "FTSN",
            EptsReportUtils.map(
                cxcascrnCohortQueries.get1stTimeScreened(
                    CXCASCRNCohortQueries.CXCASCRNResult.NEGATIVE),
                MAPPING));
    addRow(
        dsd,
        "FTSN",
        "1st Time Screened NEGATIVE",
        EptsReportUtils.map(f1rstTimeScreenedNegative, MAPPING),
        getColumnsForAge());

    // 1st Time Screened POSITIVE - FTSP
    CohortIndicator f1rstTimeScreenedPositive =
        eptsGeneralIndicator.getIndicator(
            "FTSP",
            EptsReportUtils.map(
                cxcascrnCohortQueries.get1stTimeScreened(
                    CXCASCRNCohortQueries.CXCASCRNResult.POSITIVE),
                MAPPING));
    addRow(
        dsd,
        "FTSP",
        "1st Time Screened POSITIVE",
        EptsReportUtils.map(f1rstTimeScreenedPositive, MAPPING),
        getColumnsForAge());

    // 1st Time Screened SUSPECTED - FTSS
    CohortIndicator f1rstTimeScreenedSuspected =
        eptsGeneralIndicator.getIndicator(
            "FTSS",
            EptsReportUtils.map(
                cxcascrnCohortQueries.get1stTimeScreened(
                    CXCASCRNCohortQueries.CXCASCRNResult.SUSPECTED),
                MAPPING));
    addRow(
        dsd,
        "FTSS",
        "1st Time Screened SUSPECTED",
        EptsReportUtils.map(f1rstTimeScreenedSuspected, MAPPING),
        getColumnsForAge());

    /** Rescreened after previous negative - RAPN */
    CohortIndicator rescreenedAfterPreviousNegative =
        eptsGeneralIndicator.getIndicator(
            "RAPN",
            EptsReportUtils.map(
                cxcascrnCohortQueries.getRescreenedAfterPreviousNegative(
                    CXCASCRNCohortQueries.CXCASCRNResult.ALL),
                MAPPING));
    dsd.addColumn(
        "RAPN",
        "Rescreened after previous negative",
        EptsReportUtils.map(rescreenedAfterPreviousNegative, MAPPING),
        "");

    // Rescreened after previous negative NEGATIVE - RAPNN
    CohortIndicator rescreenedAfterPreviousNegativeNegative =
        eptsGeneralIndicator.getIndicator(
            "RAPNN",
            EptsReportUtils.map(
                cxcascrnCohortQueries.getRescreenedAfterPreviousNegative(
                    CXCASCRNCohortQueries.CXCASCRNResult.NEGATIVE),
                MAPPING));
    addRow(
        dsd,
        "RAPNN",
        "Rescreened after previous negative NEGATIVE",
        EptsReportUtils.map(rescreenedAfterPreviousNegativeNegative, MAPPING),
        getColumnsForAge());

    // Rescreened after previous negative POSITIVE - RAPNP
    CohortIndicator rescreenedAfterPreviousNegativePositive =
        eptsGeneralIndicator.getIndicator(
            "RAPNP",
            EptsReportUtils.map(
                cxcascrnCohortQueries.getRescreenedAfterPreviousNegative(
                    CXCASCRNCohortQueries.CXCASCRNResult.POSITIVE),
                MAPPING));
    addRow(
        dsd,
        "RAPNP",
        "Rescreened after previous negative POSITIVE",
        EptsReportUtils.map(rescreenedAfterPreviousNegativePositive, MAPPING),
        getColumnsForAge());

    // Rescreened after previous negative SUSPECTED - RAPNS
    CohortIndicator rescreenedAfterPreviousNegativeSuspected =
        eptsGeneralIndicator.getIndicator(
            "RAPNS",
            EptsReportUtils.map(
                cxcascrnCohortQueries.getRescreenedAfterPreviousNegative(
                    CXCASCRNCohortQueries.CXCASCRNResult.SUSPECTED),
                MAPPING));
    addRow(
        dsd,
        "RAPNS",
        "Rescreened after previous negative SUSPECTED",
        EptsReportUtils.map(rescreenedAfterPreviousNegativeSuspected, MAPPING),
        getColumnsForAge());

    /** Post-Treatment follow-up - PTFU */
    CohortIndicator postTreatmentFollowUp =
        eptsGeneralIndicator.getIndicator(
            "PTFU",
            EptsReportUtils.map(
                cxcascrnCohortQueries.getPostTreatmentFollowUp(
                    CXCASCRNCohortQueries.CXCASCRNResult.ALL),
                MAPPING));
    dsd.addColumn(
        "PTFU",
        "Post-Treatment follow-up",
        EptsReportUtils.map(postTreatmentFollowUp, MAPPING),
        "");

    // Post-Treatment follow-up NEGATIVE - PTFUN
    CohortIndicator postTreatmentFollowUpNegative =
        eptsGeneralIndicator.getIndicator(
            "PTFUN",
            EptsReportUtils.map(
                cxcascrnCohortQueries.getPostTreatmentFollowUp(
                    CXCASCRNCohortQueries.CXCASCRNResult.NEGATIVE),
                MAPPING));
    addRow(
        dsd,
        "PTFUN",
        "Post-Treatment follow-up NEGATIVE",
        EptsReportUtils.map(postTreatmentFollowUpNegative, MAPPING),
        getColumnsForAge());

    // Post-Treatment follow-up POSITIVE - PTFUP
    CohortIndicator postTreatmentFollowUpPositive =
        eptsGeneralIndicator.getIndicator(
            "PTFUP",
            EptsReportUtils.map(
                cxcascrnCohortQueries.getPostTreatmentFollowUp(
                    CXCASCRNCohortQueries.CXCASCRNResult.POSITIVE),
                MAPPING));
    addRow(
        dsd,
        "PTFUP",
        "Post-Treatment follow-up POSITIVE",
        EptsReportUtils.map(postTreatmentFollowUpPositive, MAPPING),
        getColumnsForAge());

    // Post-Treatment follow-up SUSPECTED - PTFUS
    CohortIndicator postTreatmentFollowUpSuspected =
        eptsGeneralIndicator.getIndicator(
            "PTFUS",
            EptsReportUtils.map(
                cxcascrnCohortQueries.getPostTreatmentFollowUp(
                    CXCASCRNCohortQueries.CXCASCRNResult.SUSPECTED),
                MAPPING));
    addRow(
        dsd,
        "PTFUS",
        "Post-Treatment follow-up SUSPECTED",
        EptsReportUtils.map(postTreatmentFollowUpSuspected, MAPPING),
        getColumnsForAge());

    /** Rescreened after previous positive - RAPP */
    CohortIndicator rescreenedAfterPreviousPositive =
        eptsGeneralIndicator.getIndicator(
            "RAPP",
            EptsReportUtils.map(
                cxcascrnCohortQueries.getRescreenedAfterPreviousPositive(
                    CXCASCRNCohortQueries.CXCASCRNResult.ALL),
                MAPPING));
    dsd.addColumn(
        "RAPP",
        "Rescreened after previous positive",
        EptsReportUtils.map(rescreenedAfterPreviousPositive, MAPPING),
        "");

    // Rescreened after previous positive NEGATIVE - RAPPN
    CohortIndicator rescreenedAfterPreviousPositiveNegative =
        eptsGeneralIndicator.getIndicator(
            "RAPPN",
            EptsReportUtils.map(
                cxcascrnCohortQueries.getRescreenedAfterPreviousPositive(
                    CXCASCRNCohortQueries.CXCASCRNResult.NEGATIVE),
                MAPPING));
    addRow(
        dsd,
        "PTFUN",
        "Post-Treatment follow-up NEGATIVE",
        EptsReportUtils.map(rescreenedAfterPreviousPositiveNegative, MAPPING),
        getColumnsForAge());

    // Rescreened after previous positive POSITIVE - RAPPP
    CohortIndicator rescreenedAfterPreviousPositivePositive =
        eptsGeneralIndicator.getIndicator(
            "RAPPP",
            EptsReportUtils.map(
                cxcascrnCohortQueries.getRescreenedAfterPreviousPositive(
                    CXCASCRNCohortQueries.CXCASCRNResult.POSITIVE),
                MAPPING));
    addRow(
        dsd,
        "RAPPP",
        "Rescreened after previous positive POSITIVE",
        EptsReportUtils.map(rescreenedAfterPreviousPositivePositive, MAPPING),
        getColumnsForAge());

    // Rescreened after previous positive Suspected - RAPPS
    CohortIndicator rescreenedAfterPreviousPositiveSuspected =
        eptsGeneralIndicator.getIndicator(
            "RAPPS",
            EptsReportUtils.map(
                cxcascrnCohortQueries.getRescreenedAfterPreviousPositive(
                    CXCASCRNCohortQueries.CXCASCRNResult.SUSPECTED),
                MAPPING));
    addRow(
        dsd,
        "RAPPS",
        "Rescreened after previous positive SUSPECTED",
        EptsReportUtils.map(rescreenedAfterPreviousPositiveSuspected, MAPPING),
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
        new ColumnParameters("fifteenTo19", "15 - 19 female", "age=15-19", "01");
    ColumnParameters twentyTo24 =
        new ColumnParameters("twentyTo24", "20 - 24 female", "age=20-24", "02");
    ColumnParameters twenty5To29 =
        new ColumnParameters("twenty4To29", "25 - 29 female", "age=25-29", "03");
    ColumnParameters thirtyTo34 =
        new ColumnParameters("thirtyTo34", "30 - 34 female", "age=30-34", "04");
    ColumnParameters thirty5To39 =
        new ColumnParameters("thirty5To39", "35 - 39 female", "age=35-39", "05");
    ColumnParameters foutyTo44 =
        new ColumnParameters("foutyTo44", "40 - 44 female", "age=40-44", "06");
    ColumnParameters fouty5To49 =
        new ColumnParameters("fouty5To49", "45 - 49 female", "age=45-49", "07");
    ColumnParameters above50 = new ColumnParameters("above50", "50+ female", "age=50+", "08");
    ColumnParameters unknown =
        new ColumnParameters("unknown", "Unknown age female", "age=UK", "09");
    ColumnParameters total = new ColumnParameters("totals", "Totals", "", "10");

    return Arrays.asList(
        fifteenTo19,
        twentyTo24,
        twenty5To29,
        thirtyTo34,
        thirty5To39,
        foutyTo44,
        fouty5To49,
        above50,
        unknown,
        total);
  }
}
