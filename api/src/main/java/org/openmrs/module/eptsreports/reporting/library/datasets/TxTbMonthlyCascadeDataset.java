package org.openmrs.module.eptsreports.reporting.library.datasets;

import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.library.cohorts.TXTBCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.TxTbMonthlyCascadeCohortQueries;
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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class TxTbMonthlyCascadeDataset extends BaseDataSet {

  @Autowired private TxTbMonthlyCascadeCohortQueries txTbMonthlyCascadeCohortQueries;

  @Autowired private EptsGeneralIndicator eptsGeneralIndicator;
  @Autowired private EptsCommonDimension eptsCommonDimension;

  @Autowired private TXTBCohortQueries txtbCohortQueries;

  @Autowired
  @Qualifier("commonAgeDimensionCohort")
  private AgeDimensionCohortInterface ageDimensionCohort;

  public DataSetDefinition constructTXTBMonthlyDataset() {

    CohortIndicatorDataSetDefinition cohortIndicatorDefinition =
        new CohortIndicatorDataSetDefinition();
    cohortIndicatorDefinition.setName("TX_TB Monthly Cascade Dataset");
    cohortIndicatorDefinition.addParameters(getParameters());

    cohortIndicatorDefinition.addDimension(
        "age",
        EptsReportUtils.map(
            eptsCommonDimension.age(ageDimensionCohort), "effectiveDate=${endDate}"));
    cohortIndicatorDefinition.addDimension(
        "gender", EptsReportUtils.map(eptsCommonDimension.gender(), ""));

    CohortIndicator TXCURR =
        eptsGeneralIndicator.getIndicator(
            "TXCURRTOTAL",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getTxCurrOrTxCurrWithClinicalConsultation(
                    TxTbMonthlyCascadeCohortQueries.TxCurrComposition.TXCURR),
                "startDate=${endDate-6m},endDate=${endDate},location=${location}"));

    cohortIndicatorDefinition.addColumn(
        "TXCURRTOTAL",
        "TXCURR TOTAL",
        EptsReportUtils.map(TXCURR, "endDate=${endDate},location=${location}"),
        "");

    CohortIndicator NEWART =
        eptsGeneralIndicator.getIndicator(
            "NEWART ",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getPatientsOnTxCurrAndNewOnArt(),
                "endDate=${endDate},location=${location}"));

    cohortIndicatorDefinition.addColumn(
        "NEWART ",
        "NEWART",
        EptsReportUtils.map(NEWART, "endDate=${endDate},location=${location}"),
        "");

    addRow(
        cohortIndicatorDefinition,
        "NEWART",
        "New on ART",
        EptsReportUtils.map(NEWART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());
    // Previously On ART
    CohortIndicator PREVIOUSLYART =
        eptsGeneralIndicator.getIndicator(
            "PREVIOUSLYART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getPatientsOnTxCurrAndPreviouslyOnArt(),
                "endDate=${endDate},location=${location}"));

    cohortIndicatorDefinition.addColumn(
        "PREVIOUSLYART",
        "PREVIOUSLYART",
        EptsReportUtils.map(PREVIOUSLYART, "endDate=${endDate},location=${location}"),
        "");

    addRow(
        cohortIndicatorDefinition,
        "PREVIOUSLYART",
        "PREVIOUSLYART",
        EptsReportUtils.map(PREVIOUSLYART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // 2.  With Clinical Consultation
    CohortIndicator TXCLINICAL =
        eptsGeneralIndicator.getIndicator(
            "TXCLINICAL",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getTxCurrOrTxCurrWithClinicalConsultation(
                    TxTbMonthlyCascadeCohortQueries.TxCurrComposition.TXCURR_AND_CLINICAL),
                "startDate=${endDate-6m},endDate=${endDate},location=${location}"));

    cohortIndicatorDefinition.addColumn(
        "TXCLINICAL",
        "TXCLINICAL",
        EptsReportUtils.map(TXCLINICAL, "endDate=${endDate},location=${location}"),
        "");

    CohortIndicator TXCLINICALART =
        eptsGeneralIndicator.getIndicator(
            "TXCLINICALART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getTxCurrOrTxCurrWithClinicalConsultation(
                    TxTbMonthlyCascadeCohortQueries.TxCurrComposition
                        .TXCURR_AND_CLINICAL_AND_NEWART),
                "startDate=${endDate-6m},endDate=${endDate},location=${location}"));

    cohortIndicatorDefinition.addColumn(
        "TXCLINICALART",
        "TXCLINICALART",
        EptsReportUtils.map(NEWART, "endDate=${endDate},location=${location}"),
        "");

    addRow(
        cohortIndicatorDefinition,
        "TXCLINICALART",
        "TXCLINICALART",
        EptsReportUtils.map(TXCLINICALART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    //

    CohortIndicator TXCLINICALPREV =
        eptsGeneralIndicator.getIndicator(
            "TXCLINICALPREV",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getTxCurrOrTxCurrWithClinicalConsultation(
                    TxTbMonthlyCascadeCohortQueries.TxCurrComposition
                        .TXCURR_AND_CLINICAL_AND_PREVIUSLYART),
                "startDate=${endDate-6m},endDate=${endDate},location=${location}"));

    cohortIndicatorDefinition.addColumn(
        "TXCLINICALPREV",
        "TXCLINICALPREV",
        EptsReportUtils.map(TXCLINICALPREV, "endDate=${endDate},location=${location}"),
        "");

    addRow(
        cohortIndicatorDefinition,
        "TXCLINICALPREV",
        "TXCLINICALPREV",
        EptsReportUtils.map(TXCLINICALPREV, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // 3.a TX_TB denominator: Number of patients on ART who were screened for TB in the last 6
    // months

    CohortIndicator TXTBDENTOTAL =
        eptsGeneralIndicator.getIndicator(
            "TXTBDENTOTAL",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getTxTbDenominatorCohort(
                    TxTbMonthlyCascadeCohortQueries.TxTbComposition.TXTB),
                "startDate=${endDate-6m},endDate=${endDate},location=${location}"));

    cohortIndicatorDefinition.addColumn(
        "TXTBDENTOTAL",
        "TXTBDENTOTAL",
        EptsReportUtils.map(TXCURR, "endDate=${endDate},location=${location}"),
        "");

    CohortIndicator TXTBDEN =
        eptsGeneralIndicator.getIndicator(
            "TXTBDEN",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getTxTbDenominatorCohort(
                    TxTbMonthlyCascadeCohortQueries.TxTbComposition.TXTB_AND_NEWART),
                "startDate=${endDate-6m},endDate=${endDate},location=${location}"));

    cohortIndicatorDefinition.addColumn(
        "TXTBDEN",
        "TXTBDEN",
        EptsReportUtils.map(TXTBDEN, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "TXTBDEN",
        "TXTBDEN",
        EptsReportUtils.map(TXTBDEN, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // previously
    CohortIndicator TXTBDENPREV =
        eptsGeneralIndicator.getIndicator(
            "TXTBDENPREV",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getTxTbDenominatorCohort(
                    TxTbMonthlyCascadeCohortQueries.TxTbComposition.TXTB_AND_PREVIOUSLYART),
                "startDate=${endDate-6m},endDate=${endDate},location=${location}"));

    cohortIndicatorDefinition.addColumn(
        "TXTBDENPREV",
        "TXTBDENPREV",
        EptsReportUtils.map(TXTBDENPREV, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "TXTBDENPREV",
        "TXTBDENPREV",
        EptsReportUtils.map(TXTBDENPREV, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // 3.b.TX_TB denominator not died not transferred-out: Number of patients on ART who were
    // screened for TB in the last 6 months who are not dead or not Transferred-out

    CohortIndicator TXTBTXCURR =
        eptsGeneralIndicator.getIndicator(
            "TXTBTXCURR",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getTxTbDenominatorCohort(
                    TxTbMonthlyCascadeCohortQueries.TxTbComposition.TXTB_AND_TXCURR),
                "startDate=${endDate-6m},endDate=${endDate},location=${location}"));

    cohortIndicatorDefinition.addColumn(
        "TXTBTXCURR",
        "TXTBTXCURR",
        EptsReportUtils.map(TXTBTXCURR, "endDate=${endDate},location=${location}"),
        "");

    CohortIndicator TXTBTXCURRNEW =
        eptsGeneralIndicator.getIndicator(
            "TXTBTXCURRNEW",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getTxTbDenominatorCohort(
                    TxTbMonthlyCascadeCohortQueries.TxTbComposition.TXTB_AND_TXCURR_AND_NEWART),
                "startDate=${endDate-6m},endDate=${endDate},location=${location}"));
    addRow(
        cohortIndicatorDefinition,
        "TXTBTXCURRNEW",
        "TXTBTXCURRNEW",
        EptsReportUtils.map(TXTBTXCURRNEW, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    CohortIndicator TXTBTXCURRPREV =
        eptsGeneralIndicator.getIndicator(
            "TXTBTXCURRPREV",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getTxTbDenominatorCohort(
                    TxTbMonthlyCascadeCohortQueries.TxTbComposition
                        .TXTB_AND_TXCURR_AND_PREVIOUSLYART),
                "startDate=${endDate-6m},endDate=${endDate},location=${location}"));
    addRow(
        cohortIndicatorDefinition,
        "TXTBTXCURRPREV",
        "TXTBTXCURRPREV",
        EptsReportUtils.map(TXTBTXCURRPREV, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    return cohortIndicatorDefinition;
  }

  private List<ColumnParameters> getSexAndAgeDimension() {
    ColumnParameters female =
        new ColumnParameters(
            "female",
            "female",
            EptsCommonDimensionKey.of(DimensionKeyForGender.female).getDimensions(),
            "female");

    ColumnParameters over15Female =
        new ColumnParameters(
            "above15Female",
            "above 15 female",
            EptsCommonDimensionKey.of(DimensionKeyForGender.female)
                .and(DimensionKeyForAge.overOrEqualTo15Years)
                .getDimensions(),
            "above15Female");

    ColumnParameters bellow15Female =
        new ColumnParameters(
            "bellow15Female",
            "bellow15Female",
            EptsCommonDimensionKey.of(DimensionKeyForGender.female)
                .and(DimensionKeyForAge.bellow15Years)
                .getDimensions(),
            "bellow15Female");

    ColumnParameters unknownAgeFemale =
        new ColumnParameters(
            "unknownAgeFemale",
            "unknownAgeFemale",
            EptsCommonDimensionKey.of(DimensionKeyForGender.female)
                .and(DimensionKeyForAge.unknown)
                .getDimensions(),
            "unknownAgeFemale");

    ColumnParameters male =
        new ColumnParameters(
            "male",
            "male",
            EptsCommonDimensionKey.of(DimensionKeyForGender.male).getDimensions(),
            "male");

    ColumnParameters over15Male =
        new ColumnParameters(
            "above15Male",
            "above15Male",
            EptsCommonDimensionKey.of(DimensionKeyForGender.male)
                .and(DimensionKeyForAge.overOrEqualTo15Years)
                .getDimensions(),
            "above15Male");

    ColumnParameters bellow15Male =
        new ColumnParameters(
            "bellow15Male",
            "bellow15Male",
            EptsCommonDimensionKey.of(DimensionKeyForGender.male)
                .and(DimensionKeyForAge.bellow15Years)
                .getDimensions(),
            "bellow15Male");

    ColumnParameters unknownAgeMale =
        new ColumnParameters(
            "unknownAgeMale",
            "unknownAgeMale",
            EptsCommonDimensionKey.of(DimensionKeyForGender.male)
                .and(DimensionKeyForAge.unknown)
                .getDimensions(),
            "unknownAgeMale");

    return Arrays.asList(
        bellow15Female,
        over15Female,
        unknownAgeFemale,
        female,
        bellow15Male,
        over15Male,
        unknownAgeMale,
        male);
  }

  @Override
  public List<Parameter> getParameters() {
    return Arrays.asList(
        new Parameter("endDate", "endDate", Date.class),
        new Parameter("location", "Location", Location.class));
  }
}
