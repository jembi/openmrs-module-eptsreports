package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.library.cohorts.*;
import org.openmrs.module.eptsreports.reporting.library.dimensions.*;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class AdvancedDiseaseAndTbCascadeDataset extends BaseDataSet {

  @Autowired
  private AdvancedDiseaseAndTBCascadeCohortQueries advancedDiseaseAndTBCascadeCohortQueries;

  @Autowired private EptsGeneralIndicator eptsGeneralIndicator;
  @Autowired private EptsCommonDimension eptsCommonDimension;
  @Autowired private AdvanceDiseaseAndTbCascadeDimension advanceDiseaseAndTbCascadeDimension;

  @Autowired
  @Qualifier("commonAgeDimensionCohort")
  private AgeDimensionCohortInterface ageDimensionCohort;

  private final String reportingPeriod =
      "startDate=${endDate-2m+1d},endDate=${generationDate},location=${location}";
  private final String inclusionPeriod =
      "startDate=${endDate-2m+1d},endDate=${endDate-1m},location=${location}";

  private final String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";

  public DataSetDefinition constructAdvancedDiseaseAndTbCascadeDataset() {

    CohortIndicatorDataSetDefinition dataSetDefinition = new CohortIndicatorDataSetDefinition();
    dataSetDefinition.setName("TB7 Dataset");
    dataSetDefinition.addParameters(getParameters());

    dataSetDefinition.addDimension(
        "age",
        EptsReportUtils.map(
            eptsCommonDimension.age(ageDimensionCohort), "effectiveDate=${endDate}"));

    dataSetDefinition.addDimension("gender", EptsReportUtils.map(eptsCommonDimension.gender(), ""));

    dataSetDefinition.addDimension(
        "grade",
        EptsReportUtils.map(
            advanceDiseaseAndTbCascadeDimension.getPatientWithPositiveTbLamAndGradeDimension(),
            reportingPeriod));

    dataSetDefinition.addDimension(
        "cd4Eligibility",
        EptsReportUtils.map(
            advanceDiseaseAndTbCascadeDimension.getCd4EligibilityDisaggregations(), mappings));

    // -----------------CASCADE 1-------------------

    // TB_DA_FR13 - Number of clients eligible for CD4 count during inclusion period (Cascade 1)
    CohortIndicator eligibleCd4Ind =
        eptsGeneralIndicator.getIndicator(
            "eligibleCd4Ind",
            EptsReportUtils.map(
                advancedDiseaseAndTBCascadeCohortQueries.getClientsEligibleForCd4(), mappings));

    dataSetDefinition.addColumn(
        "eligibleCd4Total",
        "ClientsEligible4Cd4Total",
        EptsReportUtils.map(eligibleCd4Ind, mappings),
        "");

    addRow(
        dataSetDefinition,
        "eligibleCd4",
        "ClientsEligibleCd4",
        EptsReportUtils.map(eligibleCd4Ind, mappings),
        dissagregations());

    // TB_DA_FR16 - Number of clients eligible for CD4 count who have a CD4 count within 33 days
    // (Cascade 1)
    CohortIndicator cd4CountInd =
        eptsGeneralIndicator.getIndicator(
            "cd4CountInd",
            EptsReportUtils.map(
                advancedDiseaseAndTBCascadeCohortQueries.getClientsWithCd4Count(), mappings));

    dataSetDefinition.addColumn(
        "cd4CountTotal", "ClientsWithCd4Total", EptsReportUtils.map(cd4CountInd, mappings), "");

    addRow(
        dataSetDefinition,
        "cd4Count",
        "ClientsWithCd4Count",
        EptsReportUtils.map(cd4CountInd, mappings),
        dissagregations());

    // TB_DA_FR17 - Number of eligible clients with CD4 count during inclusion period showing severe
    // immunodepression (Cascade 1)
    CohortIndicator severeIndicator =
        eptsGeneralIndicator.getIndicator(
            "severeIndicator",
            EptsReportUtils.map(
                advancedDiseaseAndTBCascadeCohortQueries.getClientsWithSevereImmunosuppression(),
                mappings));

    dataSetDefinition.addColumn(
        "severeImmunosuppressionTotal",
        "ClientsWithSevereImmunosuppressionTotal",
        EptsReportUtils.map(severeIndicator, mappings),
        "");

    addRow(
        dataSetDefinition,
        "severeImmunosuppression",
        "ClientsWithSevereImmunosuppression",
        EptsReportUtils.map(severeIndicator, mappings),
        dissagregations());

    // TB_DA_FR18 - Number of clients eligible for CD4 with CD4 count showing severe
    // immunosuppression and who have a TB LAM result by report generation date (Cascade 1)
    CohortIndicator severeTbLam =
        eptsGeneralIndicator.getIndicator(
            "severeTbLamInd",
            EptsReportUtils.map(
                advancedDiseaseAndTBCascadeCohortQueries
                    .getClientsWithSevereImmunosuppressionAndTbLamResult(),
                mappings));

    dataSetDefinition.addColumn(
        "severeTbLamTotal",
        "ClientsWithSevereImmunosuppressionTbLamTotal",
        EptsReportUtils.map(severeTbLam, mappings),
        "");

    addRow(
        dataSetDefinition,
        "severeTbLam",
        "ClientsWithSevereImmunosuppressionAndTbLam",
        EptsReportUtils.map(severeTbLam, mappings),
        dissagregations());

    // -----------------CASCADE 2-------------------

    // TB_DA_FR19 - Number of clients with CD4 count showing immunosuppression and with TB LAM
    // results during inclusion period
    CohortIndicator severeWithTbLam =
        eptsGeneralIndicator.getIndicator(
            "severeWithTbLam",
            EptsReportUtils.map(
                advancedDiseaseAndTBCascadeCohortQueries
                    .getClientsWithSevereImmunosuppressionAndWithTbLamResult(),
                mappings));
    addRow(
        dataSetDefinition,
        "severeWithTbLam",
        "ClientsWithSevereImmunosuppressionAndWithTbLam",
        EptsReportUtils.map(severeWithTbLam, mappings),
        dissagregations());

    dataSetDefinition.addColumn(
        "severeWithoutTbLamTotal",
        "ClientsWithSevereImmunodepressionWithoutTbLamTotal",
        EptsReportUtils.map(severeWithTbLam, mappings),
        "");

    // TB_DA_FR20 - Number of clients with positive TB LAM result during the inclusion period
    CohortIndicator severeWithTbLamPositive =
        eptsGeneralIndicator.getIndicator(
            "severeWithTbLamPositive",
            EptsReportUtils.map(
                advancedDiseaseAndTBCascadeCohortQueries
                    .getClientsWithSevereImmunodepressionAndWithTbLamPositiveResult(),
                mappings));
    addRow(
        dataSetDefinition,
        "severeWithTbLamPositive",
        "ClientsWithSevereImmunosuppressionAndWithTbLamPositive",
        EptsReportUtils.map(severeWithTbLamPositive, mappings),
        dissagregations());

    dataSetDefinition.addColumn(
        "severeWithoutTbLamPositiveTotal",
        "ClientsWithSevereImmunodepressionWithoutTbLamPositiveTotal",
        EptsReportUtils.map(severeWithTbLamPositive, mappings),
        "");

    // TB_DA_FR21 Number of clients with positive TB LAM result during the inclusion period but not
    // tested with GeneXpert by report generation date
    CohortIndicator severeWithTbLamPositiveWithNoTestGeneXpert =
        eptsGeneralIndicator.getIndicator(
            "severeWithTbLamPositiveWithNoTestGeneXpert",
            EptsReportUtils.map(
                advancedDiseaseAndTBCascadeCohortQueries
                    .getClientsWithSevereImmunodepressionAndWithTbLamPositiveResultAndNotTestedForGeneXpert(),
                mappings));
    addRow(
        dataSetDefinition,
        "severeWithTbLamPositiveWithNoTestGeneXpert",
        "ClientsWithSevereWithTbLamPositiveWithNoTestGeneXpert",
        EptsReportUtils.map(severeWithTbLamPositiveWithNoTestGeneXpert, mappings),
        dissagregations());

    dataSetDefinition.addColumn(
        "severeWithTbLamPositiveWithNoTestGeneXpertTotal",
        "ClientsWithSevereWithTbLamPositiveWithNoTestGeneXpertTotal",
        EptsReportUtils.map(severeWithTbLamPositiveWithNoTestGeneXpert, mappings),
        "");

    // TB_DA_FR22 - Clients with positive TB LAM result during the inclusion period and also tested
    // with GeneXpert by report generation date
    CohortIndicator severeWithTbLamPositiveWithGeneXpertTest =
        eptsGeneralIndicator.getIndicator(
            "severeWithTbLamPositiveWithGeneXpertTest",
            EptsReportUtils.map(
                advancedDiseaseAndTBCascadeCohortQueries
                    .getClientsWithSevereImmunodepressionAndWithTbLamPositiveResultAndNotTestedForGeneXpert(),
                mappings));
    addRow(
        dataSetDefinition,
        "severeWithTbLamPositiveWithGeneXpertTest",
        "ClientsWithSevereWithTbLamPositiveWithGeneXpertTest",
        EptsReportUtils.map(severeWithTbLamPositiveWithGeneXpertTest, mappings),
        dissagregations());

    dataSetDefinition.addColumn(
        "severeWithTbLamPositiveWithGeneXpertTestTotal",
        "ClientsWithSevereWithTbLamPositiveWithGeneXpertTestTotal",
        EptsReportUtils.map(severeWithTbLamPositiveWithGeneXpertTest, mappings),
        "");

    // Number of clients with CD4 count during inclusion period without severe immunodepression
    // (TB_DA_FR20)
    CohortIndicator withoutImmunodepression =
        eptsGeneralIndicator.getIndicator(
            "withoutImmunodepression",
            EptsReportUtils.map(
                advancedDiseaseAndTBCascadeCohortQueries.getClientsWithoutSevereImmunodepression(),
                mappings));

    dataSetDefinition.addColumn(
        "withoutImmunodepressionTotal",
        "ClientsWithoutSevereImmunodepressionTotal",
        EptsReportUtils.map(withoutImmunodepression, inclusionPeriod),
        "");

    // without severe immunodepression (TB_DA_FR20) Without TB LAM
    CohortIndicator withoutImmunodepressionWithoutTbLam =
        eptsGeneralIndicator.getIndicator(
            "withoutImmunodepressionWithoutTbLam",
            EptsReportUtils.map(
                advancedDiseaseAndTBCascadeCohortQueries
                    .getClientsWithoutSevereImmunodepressionAndWithoutTbLamResult(),
                mappings));

    dataSetDefinition.addColumn(
        "withoutImmunodepressionWithoutTbLamTotal",
        "ClientsWithoutSevereImmunodepressionAndWithoutTbLam",
        EptsReportUtils.map(withoutImmunodepressionWithoutTbLam, inclusionPeriod),
        "");
    // Without severe immunodepression (TB_DA_FR20) With TB LAM
    CohortIndicator withoutImmunodepressionWithTbLam =
        eptsGeneralIndicator.getIndicator(
            "withoutImmunodepressionWithTbLam",
            EptsReportUtils.map(
                advancedDiseaseAndTBCascadeCohortQueries
                    .getClientsWithoutSevereImmunodepressionAndWithTbLamResult(),
                mappings));

    dataSetDefinition.addColumn(
        "withoutImmunodepressionWithTbLamTotal",
        "ClientsWithoutSevereImmunodepressionAndWithTbLamTotal",
        EptsReportUtils.map(withoutImmunodepressionWithTbLam, inclusionPeriod),
        "");

    addRow(
        dataSetDefinition,
        "withoutImmunodepressionWithTbLam",
        "ClientsWithSevereImmunodepressionAndWithTbLam",
        EptsReportUtils.map(withoutImmunodepressionWithTbLam, inclusionPeriod),
        dissagregations());
    // Number of clients without CD4 count but with TB LAM results during inclusion period
    // (TB_DA_FR21)

    CohortIndicator withoutCd4Tblam =
        eptsGeneralIndicator.getIndicator(
            "withoutCd4TbLamInd",
            EptsReportUtils.map(
                advancedDiseaseAndTBCascadeCohortQueries.getClientsWithoutCd4CountButWithTbLam(),
                mappings));

    dataSetDefinition.addColumn(
        "withoutCd4WithTbLamTotal",
        "ClientsWithoutCd4CountButWithTbLamTotal",
        EptsReportUtils.map(withoutCd4Tblam, inclusionPeriod),
        "");

    addRow(
        dataSetDefinition,
        "withoutCd4WithTbLam",
        "ClientsWithoutCd4CountWithButWithTbLam",
        EptsReportUtils.map(withoutCd4Tblam, inclusionPeriod),
        dissagregations());
    // Number of clients with TB LAM results by report generation date (TB_DA_FR22)
    CohortIndicator withTbLam =
        eptsGeneralIndicator.getIndicator(
            "withTbLamInd",
            EptsReportUtils.map(
                advancedDiseaseAndTBCascadeCohortQueries.getClientsWithAnyTbLam(), mappings));

    dataSetDefinition.addColumn(
        "withTbLamTotal",
        "ClientsWithLamTotal",
        EptsReportUtils.map(withTbLam, reportingPeriod),
        "");

    // TB LAM (TB_DA_FR22) Positive
    CohortIndicator positiveTbLam =
        eptsGeneralIndicator.getIndicator(
            "positiveTbLamInd",
            EptsReportUtils.map(
                advancedDiseaseAndTBCascadeCohortQueries.getClientsWithTbLamPositive(), mappings));

    dataSetDefinition.addColumn(
        "positiveTbLamTotal",
        "ClientsWithPositiveTbLamTotal",
        EptsReportUtils.map(positiveTbLam, reportingPeriod),
        "");

    addRow(
        dataSetDefinition,
        "positiveTbLam",
        "ClientsWithPositiveTbLam",
        EptsReportUtils.map(positiveTbLam, reportingPeriod),
        dissagregations());

    // TB LAM (TB_DA_FR22) Negative
    CohortIndicator negativeTbLam =
        eptsGeneralIndicator.getIndicator(
            "negativeTbLamInd",
            EptsReportUtils.map(
                advancedDiseaseAndTBCascadeCohortQueries.getClientsWithTbLamNegative(), mappings));

    dataSetDefinition.addColumn(
        "negativeTbLamTotal",
        "ClientsWithNegativeTbLamTotal",
        EptsReportUtils.map(negativeTbLam, reportingPeriod),
        "");

    // Clients with positive TB LAM but NOT tested by GeneXpert (TB_DA_FR23)
    CohortIndicator positiveTbLamNotGen =
        eptsGeneralIndicator.getIndicator(
            "positiveTbLamNotGenInd",
            EptsReportUtils.map(
                advancedDiseaseAndTBCascadeCohortQueries
                    .getClientsWithTbLamPositiveButNotTestedGeneXPert(),
                mappings));

    dataSetDefinition.addColumn(
        "positiveTbLamNotGen",
        "ClientsWithPositiveButNotTestedForGenex",
        EptsReportUtils.map(positiveTbLamNotGen, reportingPeriod),
        "");

    // Clients with positive TB LAM and also tested by GeneXpert (TB_DA_FR24)
    CohortIndicator positiveTbLamAndGen =
        eptsGeneralIndicator.getIndicator(
            "positiveTbLamAndGenInd",
            EptsReportUtils.map(
                advancedDiseaseAndTBCascadeCohortQueries
                    .getClientsWithTbLamPositiveTestedGeneXPert(),
                mappings));

    dataSetDefinition.addColumn(
        "positiveTbLamAndGen",
        "ClientsWithPositiveTestedForGenex",
        EptsReportUtils.map(positiveTbLamAndGen, reportingPeriod),
        "");
    // Clients with positive TB LAM and GeneXpert positive for TB (TB_DA_FR25)
    CohortIndicator positiveTbLamPositiveGen =
        eptsGeneralIndicator.getIndicator(
            "positiveTbLamPositiveGenInd",
            EptsReportUtils.map(
                advancedDiseaseAndTBCascadeCohortQueries
                    .getClientsWithTbLamPositiveTestedPositiveGeneXPert(),
                mappings));

    dataSetDefinition.addColumn(
        "positiveTbLamPositiveGen",
        "ClientsWithPositiveTestedPositiveForGenexTb",
        EptsReportUtils.map(positiveTbLamPositiveGen, reportingPeriod),
        "");

    // Clients with positive TB LAM and on TB treatment by report generation date (TB_DA_FR26)

    CohortIndicator positiveTbLamOnTreatmentInd =
        eptsGeneralIndicator.getIndicator(
            "positiveTbLamOnTreatmentInd ",
            EptsReportUtils.map(
                advancedDiseaseAndTBCascadeCohortQueries.getClientsWithTbLamPositiveOnTbTreatment(),
                mappings));

    dataSetDefinition.addColumn(
        "positiveTbLamOnTreatmentTotal",
        "ClientsWithPositiveTbLamOnTbTreatment",
        EptsReportUtils.map(positiveTbLamOnTreatmentInd, reportingPeriod),
        "");

    addRow(
        dataSetDefinition,
        "positiveTbLamOnTreatment",
        "ClientsWithPositiveTbLamOnTbTreatment",
        EptsReportUtils.map(positiveTbLamOnTreatmentInd, reportingPeriod),
        dissagregations());

    return dataSetDefinition;
  }

  private List<ColumnParameters> dissagregations() {
    return Arrays.asList(
        new ColumnParameters(
            "<1Females",
            "<1 year - Female",
            EptsCommonDimensionKey.of(DimensionKeyForAge.bellowOneYear)
                .and(DimensionKeyForGender.female)
                .getDimensions(),
            "F1"),
        new ColumnParameters(
            ">=1-4Females",
            "1-4 years Females",
            EptsCommonDimensionKey.of(DimensionKeyForAge.betweenOneAnd4Years)
                .and(DimensionKeyForGender.female)
                .getDimensions(),
            "F4"),
        new ColumnParameters(
            ">=5-9Females",
            "5-9 years Female",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between5And9Years)
                .and(DimensionKeyForGender.female)
                .getDimensions(),
            "F9"),
        new ColumnParameters(
            ">=10-14Females",
            "10-14 years Female",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between10And14Years)
                .and(DimensionKeyForGender.female)
                .getDimensions(),
            "F14"),
        new ColumnParameters(
            ">=15-19Females",
            "15-19 years Female",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between15And19Years)
                .and(DimensionKeyForGender.female)
                .getDimensions(),
            "F19"),
        new ColumnParameters(
            ">=20Females",
            "20+ years Female",
            EptsCommonDimensionKey.of(DimensionKeyForAge.overOrEqualTo20Years)
                .and(DimensionKeyForGender.female)
                .getDimensions(),
            "F20"),
        new ColumnParameters(
            "UkFemales",
            "UK age Females",
            EptsCommonDimensionKey.of(DimensionKeyForAge.unknown)
                .and(DimensionKeyForGender.female)
                .getDimensions(),
            "FUN"),
        new ColumnParameters(
            "<1Males",
            "<1 years Male",
            EptsCommonDimensionKey.of(DimensionKeyForAge.bellowOneYear)
                .and(DimensionKeyForGender.male)
                .getDimensions(),
            "M1"),
        new ColumnParameters(
            ">=1-4Masculino",
            "1-4 years Male",
            EptsCommonDimensionKey.of(DimensionKeyForAge.betweenOneAnd4Years)
                .and(DimensionKeyForGender.male)
                .getDimensions(),
            "M4"),
        new ColumnParameters(
            ">=5-9Masculino",
            "5-9 years Male",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between5And9Years)
                .and(DimensionKeyForGender.male)
                .getDimensions(),
            "M9"),
        new ColumnParameters(
            ">=10-14Masculino",
            "10-14 years Male",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between10And14Years)
                .and(DimensionKeyForGender.male)
                .getDimensions(),
            "M14"),
        new ColumnParameters(
            ">=15-19Masculino",
            "15-19 years Male",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between15And19Years)
                .and(DimensionKeyForGender.male)
                .getDimensions(),
            "M19"),
        new ColumnParameters(
            ">=20Masculino",
            "20+ years Male",
            EptsCommonDimensionKey.of(DimensionKeyForAge.overOrEqualTo20Years)
                .and(DimensionKeyForGender.male)
                .getDimensions(),
            "M20"),
        new ColumnParameters(
            "UkMasculino",
            "UK years Male",
            EptsCommonDimensionKey.of(DimensionKeyForAge.unknown)
                .and(DimensionKeyForGender.male)
                .getDimensions(),
            "MUN"),
        new ColumnParameters("Grade4+", "Grade 4+", "grade=4+", "4"),
        new ColumnParameters("Grade3+", "Grade 3+", "grade=3+", "3"),
        new ColumnParameters("Grade2+", "Grade 2+", "grade=2+", "2"),
        new ColumnParameters("Grade1+", "Grade 1+", "grade=1+", "1"),
        new ColumnParameters(
            "GradeNotReporte", "Grade Not Reported", "grade=notReported", "notReported"),
        new ColumnParameters(
            "cd4Eligibility-initArt",
            "Initiated Art Disaggregation",
            "cd4Eligibility=initArt",
            "clientInitiatedArt"),
        new ColumnParameters(
            "cd4Eligibility-pregnantClient",
            "Pregnant Disaggregation",
            "cd4Eligibility=pregnantClient",
            "pregnantDisaggregation"),
        new ColumnParameters(
            "cd4Eligibility-consecutiveVl",
            "Consecutive Vl Disaggregation",
            "cd4Eligibility=consecutiveVl",
            "consecutiveVlDisaggregation"),
        new ColumnParameters(
            "cd4Eligibility-reinitArt",
            "Reinitiated Art Disaggregation",
            "cd4Eligibility=reinitArt",
            "reinitArtDisaggregation"));
  }

  @Override
  public List<Parameter> getParameters() {
    return Arrays.asList(
        new Parameter("endDate", "endDate", Date.class),
        new Parameter("location", "Location", Location.class));
  }
}
