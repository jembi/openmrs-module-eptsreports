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
        "cd4",
        EptsReportUtils.map(
            advanceDiseaseAndTbCascadeDimension.getPatientsAbsoluteCd4Count(), inclusionPeriod));

    CohortIndicator eligibleCd4Ind =
        eptsGeneralIndicator.getIndicator(
            "eligibleCd4Ind",
            EptsReportUtils.map(
                advancedDiseaseAndTBCascadeCohortQueries.getClientsEligibleForCd4(),
                inclusionPeriod));

    addRow(
        dataSetDefinition,
        "eligibleCd4",
        "ClientsEligibleCd4",
        EptsReportUtils.map(eligibleCd4Ind, inclusionPeriod),
        dissagregations());

    dataSetDefinition.addColumn(
        "eligibleCd4Total",
        "ClientsEligible4Cd4Total",
        EptsReportUtils.map(eligibleCd4Ind, inclusionPeriod),
        "");

    CohortIndicator cd4CountInd =
        eptsGeneralIndicator.getIndicator(
            "cd4CountInd",
            EptsReportUtils.map(
                advancedDiseaseAndTBCascadeCohortQueries.getClientsWithCd4Count(),
                inclusionPeriod));

    addRow(
        dataSetDefinition,
        "cd4Count",
        "ClientsWithCd4Count",
        EptsReportUtils.map(cd4CountInd, inclusionPeriod),
        dissagregations());

    dataSetDefinition.addColumn(
        "cd4CountTotal",
        "ClientsWithCd4Total",
        EptsReportUtils.map(cd4CountInd, inclusionPeriod),
        "");

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
        // CD4 Dimensions
        new ColumnParameters(
            "CD4<750Female<1", "CD4<750 Female <1 ", "age=<1|gender=F|cd4=750-", "CF1"),
        new ColumnParameters(
            "CD4<500Female1-4", "CD4<500 Female 1-4 ", "age=1-4|gender=F|cd4=500-", "CF4"),
        new ColumnParameters(
            "CD4<200Female5-9", "CD4<200 Female 5-9 ", "age=5-9|gender=F|cd4=200-", "CF9"),
        new ColumnParameters(
            "CD4<200Female10-14", "CD4<200 Female 10-14 ", "age=10-14|gender=F|cd4=200-", "CF14"),
        new ColumnParameters(
            "CD4<200Female15-19", "CD4<200 Female 15-19 ", "age=15-19|gender=F|cd4=200-", "CF19"),
        new ColumnParameters(
            "CD4<200Female20+", "CD4<200 Female 20+ ", "age=20+|gender=F|cd4=200-", "CF20"),
        new ColumnParameters(
            "CD4<750Male<1", "CD4<750 Male <1 ", "age=<1|gender=M|cd4=750-", "CM1"),
        new ColumnParameters(
            "CD4<500Male1-4", "CD4<500 Male 1-4 ", "age=1-4|gender=M|cd4=500-", "CM4"),
        new ColumnParameters(
            "CD4<200Male5-9", "CD4<200 Male 5-9 ", "age=5-9|gender=M|cd4=200-", "CM9"),
        new ColumnParameters(
            "CD4<200Male10-14", "CD4<200 Male 10-14 ", "age=10-14|gender=M|cd4=200-", "CM14"),
        new ColumnParameters(
            "CD4<200Male15-19", "CD4<200 Male 15-19 ", "age=15-19|gender=M|cd4=200-", "CM19"),
        new ColumnParameters(
            "CD4<200Male20+", "CD4<200 Male 20+ ", "age=20+|gender=M|cd4=200-", "CM20"));
  }

  @Override
  public List<Parameter> getParameters() {
    return Arrays.asList(
        new Parameter("endDate", "endDate", Date.class),
        new Parameter("location", "Location", Location.class));
  }
}
