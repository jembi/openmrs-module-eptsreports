package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.Arrays;
import java.util.List;
import org.openmrs.module.eptsreports.reporting.library.cohorts.TXCXCACohortQueries;
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
public class TXCXCADataset extends BaseDataSet {

  @Autowired private EptsCommonDimension eptsCommonDimension;

  @Autowired private EptsGeneralIndicator eptsGeneralIndicator;

  @Autowired private TXCXCACohortQueries cxcatxCohortQueries;

  @Autowired
  @Qualifier("commonAgeDimensionCohort")
  private AgeDimensionCohortInterface ageDimensionCohort;

  public DataSetDefinition constructTXCXCASCRNDataset() {
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
            "TOTALTX", EptsReportUtils.map(cxcatxCohortQueries.getTotal(), mappings));

    dsd.addColumn("TOTALTX", "Total TX", EptsReportUtils.map(total, mappings), "");

    /** First Time Screened - FTS */
    CohortIndicator f1rstTimeScreened =
        eptsGeneralIndicator.getIndicator(
            "FTSTX", EptsReportUtils.map(cxcatxCohortQueries.getFirstTimeScreened(), mappings));
    dsd.addColumn(
        "FTSTX", "First Time Screened", EptsReportUtils.map(f1rstTimeScreened, mappings), "");

    // First Time Screened (B5) - FTSNB5 (Cryotherapy)
    CohortIndicator firstTimeScreenedWithCryotherapy =
        eptsGeneralIndicator.getIndicator(
            "FTSNB5TX",
            EptsReportUtils.map(
                this.cxcatxCohortQueries.getFirstTimeScreenedPatientsWithCryotherapy(), mappings));
    addRow(
        dsd,
        "FTSNB5TX",
        "First Time Screened B5 - Cryotherapy",
        EptsReportUtils.map(firstTimeScreenedWithCryotherapy, mappings),
        getColumnsForAge());

    // First Time Screened (B6) - FTSB6 (Thermocoagulation)
    CohortIndicator firstTimeScreenedWithThermocoagulation =
        eptsGeneralIndicator.getIndicator(
            "FTSB6TX",
            EptsReportUtils.map(
                this.cxcatxCohortQueries.getFirstTimeScreenedPatientsWithThermocoagulation(),
                mappings));
    addRow(
        dsd,
        "FTSB6TX",
        "First Time Screened B6 -Thermocoagulation",
        EptsReportUtils.map(firstTimeScreenedWithThermocoagulation, mappings),
        getColumnsForAge());

    // First Time Screened (B7) - FTSB7 (LEEP/Conization)
    CohortIndicator f1rstTimeScreenedWithLeepOrConization =
        eptsGeneralIndicator.getIndicator(
            "FTSB7TX",
            EptsReportUtils.map(
                this.cxcatxCohortQueries.getFirstTimeScreenedPatientsWithLeepOrConization(),
                mappings));
    addRow(
        dsd,
        "FTSB7TX",
        "First Time Screened B7 - LEEP/Conization",
        EptsReportUtils.map(f1rstTimeScreenedWithLeepOrConization, mappings),
        getColumnsForAge());

    /** Rescreened after previous negative - RAPN */
    CohortIndicator rescreenedAfterPreviousNegative =
        eptsGeneralIndicator.getIndicator(
            "RAPNTX",
            EptsReportUtils.map(
                cxcatxCohortQueries.getRescreenedAfterPreviousNegative(), mappings));
    dsd.addColumn(
        "RAPNTX",
        "Rescreened after previous negative",
        EptsReportUtils.map(rescreenedAfterPreviousNegative, mappings),
        "");

    // Rescreened after previous negative (B5) - RAPNB5 (Crytherapy)
    CohortIndicator rescreenedAfterPreviousNegativeWithCrytherapy =
        eptsGeneralIndicator.getIndicator(
            "RAPNB5TX",
            EptsReportUtils.map(
                this.cxcatxCohortQueries
                    .getRescreenedAfterPreviousNegativePatientsWithCryotherapy(),
                mappings));
    addRow(
        dsd,
        "RAPNB5TX",
        "Rescreened after previous negative With Crytherapy",
        EptsReportUtils.map(rescreenedAfterPreviousNegativeWithCrytherapy, mappings),
        getColumnsForAge());

    // Rescreened after previous negative (B6) - RAPNB6 (Thermocoagulation)
    CohortIndicator rescreenedAfterPreviousNegativeWithThermocoagulation =
        eptsGeneralIndicator.getIndicator(
            "RAPNB6TX",
            EptsReportUtils.map(
                this.cxcatxCohortQueries
                    .getRescreenedAfterPreviousNegativePatientsWithThermocoagulation(),
                mappings));
    addRow(
        dsd,
        "RAPNB6TX",
        "Rescreened after previous negative With Thermocoagulation",
        EptsReportUtils.map(rescreenedAfterPreviousNegativeWithThermocoagulation, mappings),
        getColumnsForAge());

    // Rescreened after previous negative (B7) - RAPNB7 (LeepOrConization)
    CohortIndicator rescreenedAfterPreviousNegativeWithLeepOrConization =
        eptsGeneralIndicator.getIndicator(
            "RAPNB7TX",
            EptsReportUtils.map(
                this.cxcatxCohortQueries
                    .getRescreenedAfterPreviousNegativePatientsWithLeepOrConization(),
                mappings));
    addRow(
        dsd,
        "RAPNB7TX",
        "Rescreened after previous negative With Leep Or Conization",
        EptsReportUtils.map(rescreenedAfterPreviousNegativeWithLeepOrConization, mappings),
        getColumnsForAge());

    /** Post-Treatment follow-up - PTFU */
    CohortIndicator postTreatmentFollowUp =
        eptsGeneralIndicator.getIndicator(
            "PTFUTX",
            EptsReportUtils.map(
                cxcatxCohortQueries.getPatientsWithPostTreatmentFollowUp(), mappings));
    dsd.addColumn(
        "PTFUTX",
        "Post-Treatment follow-up",
        EptsReportUtils.map(postTreatmentFollowUp, mappings),
        "");

    // Post-Treatment follow-up (B5) - PTFUB5 (Crytherapy)
    CohortIndicator postTreatmentFollowUpWithCrytherapy =
        eptsGeneralIndicator.getIndicator(
            "PTFUB5TX",
            EptsReportUtils.map(
                this.cxcatxCohortQueries.getPostTreatmentFollowUpPatientsWithCryotherapy(),
                mappings));
    addRow(
        dsd,
        "PTFUB5TX",
        "Post-Treatment follow-up B5",
        EptsReportUtils.map(postTreatmentFollowUpWithCrytherapy, mappings),
        getColumnsForAge());

    // Post-Treatment follow-up (B6) - PTFUB6 (Thermocoagulation)
    CohortIndicator postTreatmentFollowUpWithThermocoagulation =
        eptsGeneralIndicator.getIndicator(
            "PTFUB6TX",
            EptsReportUtils.map(
                this.cxcatxCohortQueries.getPostTreatmentFollowUpPatientsWithThermocoagulation(),
                mappings));
    addRow(
        dsd,
        "PTFUB6TX",
        "Post-Treatment follow-up B6",
        EptsReportUtils.map(postTreatmentFollowUpWithThermocoagulation, mappings),
        getColumnsForAge());

    // Post-Treatment follow-up (B7) - PTFUB7 (LeepOrConization)
    CohortIndicator postTreatmentFollowUpWithLeepOrConization =
        eptsGeneralIndicator.getIndicator(
            "PTFUB7TX",
            EptsReportUtils.map(
                this.cxcatxCohortQueries.getPostTreatmentFollowUpPatientsWithLeepOrConization(),
                mappings));
    addRow(
        dsd,
        "PTFUB7TX",
        "Post-Treatment follow-up B7",
        EptsReportUtils.map(postTreatmentFollowUpWithLeepOrConization, mappings),
        getColumnsForAge());

    /** Rescreened after previous positive - RAPP */
    CohortIndicator rescreenedAfterPreviousPositive =
        eptsGeneralIndicator.getIndicator(
            "RAPPTX",
            EptsReportUtils.map(
                cxcatxCohortQueries.getRescreenedAfterPreviousPositive(), mappings));
    dsd.addColumn(
        "RAPPTX",
        "Rescreened after previous positive",
        EptsReportUtils.map(rescreenedAfterPreviousPositive, mappings),
        "");

    // Rescreened after previous positive (B5) - RAPPB5 (Cryotherapy)
    CohortIndicator rescreenedAfterPreviousPositiveWithCryotherapy =
        eptsGeneralIndicator.getIndicator(
            "RAPPB5TX",
            EptsReportUtils.map(
                cxcatxCohortQueries.getRescreenedAfterPreviousPositiveWithCryotherapy(), mappings));
    addRow(
        dsd,
        "RAPPB5TX",
        "Rescreened after previous positive B5",
        EptsReportUtils.map(rescreenedAfterPreviousPositiveWithCryotherapy, mappings),
        getColumnsForAge());

    // Rescreened after previous positive (B6) - RAPPB6 (Thermocoagulation)
    CohortIndicator rescreenedAfterPreviousPositiveWithThermocoagulation =
        eptsGeneralIndicator.getIndicator(
            "RAPPB6TX",
            EptsReportUtils.map(
                cxcatxCohortQueries.getRescreenedAfterPreviousPositiveWithThermocoagulation(),
                mappings));
    addRow(
        dsd,
        "RAPPB6TX",
        "Rescreened after previous positive B6",
        EptsReportUtils.map(rescreenedAfterPreviousPositiveWithThermocoagulation, mappings),
        getColumnsForAge());

    // Rescreened after previous positive (B7) - RAPPB7 (LeepOrConization)
    CohortIndicator rescreenedAfterPreviousPositiveWithLeepOrConization =
        eptsGeneralIndicator.getIndicator(
            "RAPPB7TX",
            EptsReportUtils.map(
                cxcatxCohortQueries
                    .getRescreenedAfterPreviousPositivePatientsWithLeepOrConization(),
                mappings));
    addRow(
        dsd,
        "RAPPB7TX",
        "Rescreened after previous positive B7",
        EptsReportUtils.map(rescreenedAfterPreviousPositiveWithLeepOrConization, mappings),
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
