package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.library.cohorts.TxTbMonthlyCascadeCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.dimensions.AgeDimensionCohortInterface;
import org.openmrs.module.eptsreports.reporting.library.dimensions.DimensionKeyForAge;
import org.openmrs.module.eptsreports.reporting.library.dimensions.DimensionKeyForGender;
import org.openmrs.module.eptsreports.reporting.library.dimensions.EptsCommonDimension;
import org.openmrs.module.eptsreports.reporting.library.dimensions.EptsCommonDimensionKey;
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
public class TxTbMonthlyCascadeDataset extends BaseDataSet {

  @Autowired private TxTbMonthlyCascadeCohortQueries txTbMonthlyCascadeCohortQueries;

  @Autowired private EptsGeneralIndicator eptsGeneralIndicator;
  @Autowired private EptsCommonDimension eptsCommonDimension;

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
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));

    cohortIndicatorDefinition.addColumn(
        "TXCURRTOTAL",
        "TXCURR TOTAL",
        EptsReportUtils.map(TXCURR, "endDate=${endDate},location=${location}"),
        "");

    CohortIndicator NEWART =
        eptsGeneralIndicator.getIndicator(
            "NEWART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getPatientsOnTxCurrAndNewOnArt(),
                "endDate=${endDate},location=${location}"));

    cohortIndicatorDefinition.addColumn(
        "NEWART",
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
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));

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
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));

    cohortIndicatorDefinition.addColumn(
        "TXCLINICALART",
        "TXCLINICALART",
        EptsReportUtils.map(TXCLINICALART, "endDate=${endDate},location=${location}"),
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
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));

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
                    TxTbMonthlyCascadeCohortQueries.TxTbComposition.TXTB_DENOMINATOR),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));

    cohortIndicatorDefinition.addColumn(
        "TXTBDENTOTAL",
        "TXTBDENTOTAL",
        EptsReportUtils.map(TXTBDENTOTAL, "endDate=${endDate},location=${location}"),
        "");

    CohortIndicator TXTBDEN =
        eptsGeneralIndicator.getIndicator(
            "TXTBDEN",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getTxTbDenominatorCohort(
                    TxTbMonthlyCascadeCohortQueries.TxTbComposition.TXTB_AND_NEWART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));

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
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));

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
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));

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
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "TXTBTXCURRNEW",
        "TTXTBTXCURRNEW",
        EptsReportUtils.map(TXTBTXCURRNEW, "endDate=${endDate},location=${location}"),
        "");
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
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "TXTBTXCURRPREV",
        "TXTBTXCURRPREV",
        EptsReportUtils.map(TXTBTXCURRPREV, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "TXTBTXCURRPREV",
        "TXTBTXCURRPREV",
        EptsReportUtils.map(TXTBTXCURRPREV, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // 4a. Positive TB screening or specimen sent or notified with TB: Number of ART patients who
    // were screened positive for TB in the last six months

    CohortIndicator POSITIVESCREENING =
        eptsGeneralIndicator.getIndicator(
            "POSITIVESCREENING",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getTxTbDenominatorCohort(
                    TxTbMonthlyCascadeCohortQueries.TxTbComposition.POSITIVESCREENING),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));

    cohortIndicatorDefinition.addColumn(
        "POSITIVESCREENING",
        "POSITIVESCREENING",
        EptsReportUtils.map(POSITIVESCREENING, "endDate=${endDate},location=${location}"),
        "");

    CohortIndicator POSITIVENEWART =
        eptsGeneralIndicator.getIndicator(
            "POSITIVENEWART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getTxTbDenominatorCohort(
                    TxTbMonthlyCascadeCohortQueries.TxTbComposition.POSITIVESCREENING_AND_NEWART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "POSITIVENEWART",
        "POSITIVENEWART",
        EptsReportUtils.map(POSITIVENEWART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "POSITIVENEWART",
        "POSITIVENEWART",
        EptsReportUtils.map(POSITIVENEWART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    CohortIndicator POSITIVEPREVIOUSLYRT =
        eptsGeneralIndicator.getIndicator(
            "POSITIVEPREVIOUSLYRT",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getTxTbDenominatorCohort(
                    TxTbMonthlyCascadeCohortQueries.TxTbComposition
                        .POSITIVESCREENING_AND_PREVIOUSLYRT),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "POSITIVEPREVIOUSLYRT",
        "POSITIVEPREVIOUSLYRT",
        EptsReportUtils.map(POSITIVEPREVIOUSLYRT, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "POSITIVEPREVIOUSLYRT",
        "POSITIVEPREVIOUSLYRT",
        EptsReportUtils.map(POSITIVEPREVIOUSLYRT, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());
    // 4b. Negative TB screening: Number of ART patients who were screened negative for TB in the
    // last six months

    CohortIndicator NEGATIVESCREENING =
        eptsGeneralIndicator.getIndicator(
            "NEGATIVESCREENING",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getTxTbDenominatorCohort(
                    TxTbMonthlyCascadeCohortQueries.TxTbComposition.NEGATIVESCREENING),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));

    cohortIndicatorDefinition.addColumn(
        "NEGATIVESCREENING",
        "NEGATIVESCREENING",
        EptsReportUtils.map(NEGATIVESCREENING, "endDate=${endDate},location=${location}"),
        "");

    CohortIndicator NEGATIVENEWART =
        eptsGeneralIndicator.getIndicator(
            "NEGATIVENEWART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getTxTbDenominatorCohort(
                    TxTbMonthlyCascadeCohortQueries.TxTbComposition.NEGATIVESCREENING_AND_NEWART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "NEGATIVENEWART",
        "NEGATIVENEWART",
        EptsReportUtils.map(NEGATIVENEWART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "NEGATIVENEWART",
        "NEGATIVENEWART",
        EptsReportUtils.map(NEGATIVENEWART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    CohortIndicator NEGATIVEPREVIOUSLYRT =
        eptsGeneralIndicator.getIndicator(
            "NEGATIVEPREVIOUSLYRT",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getTxTbDenominatorCohort(
                    TxTbMonthlyCascadeCohortQueries.TxTbComposition
                        .NEGATIVESCREENING_AND_PREVIOUSLYRT),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "NEGATIVEPREVIOUSLYRT",
        "NEGATIVEPREVIOUSLYRT",
        EptsReportUtils.map(NEGATIVEPREVIOUSLYRT, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "NEGATIVEPREVIOUSLYRT",
        "NEGATIVEPREVIOUSLYRT",
        EptsReportUtils.map(NEGATIVEPREVIOUSLYRT, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());
    // 8a. TX_TB numerator (screened patients who initiated TB treatment): Number of ART patients
    // screened for TB in the last 6 months who initiated TB treatment (TX_TB numerator)

    CohortIndicator NUMERATORTOTAL =
        eptsGeneralIndicator.getIndicator(
            "NUMERATORTOTAL ",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getTxTbNumeratorCohort(
                    TxTbMonthlyCascadeCohortQueries.TxTbComposition.NUMERATOR),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));

    cohortIndicatorDefinition.addColumn(
        "NUMERATORTOTAL",
        "NUMERATORTOTAL",
        EptsReportUtils.map(NUMERATORTOTAL, "endDate=${endDate},location=${location}"),
        "");

    CohortIndicator NUMERATORNEWART =
        eptsGeneralIndicator.getIndicator(
            "NUMERATORNEWART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getTxTbNumeratorCohort(
                    TxTbMonthlyCascadeCohortQueries.TxTbComposition.NUMERATOR_AND_NEWART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "NUMERATORNEWART",
        "NUMERATORNEWART",
        EptsReportUtils.map(NUMERATORNEWART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "NUMERATORNEWART",
        "NUMERATORNEWART",
        EptsReportUtils.map(NUMERATORNEWART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    CohortIndicator NUMERATORPREVIOUSLYRT =
        eptsGeneralIndicator.getIndicator(
            "NUMERATORPREVIOUSLYRT ",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getTxTbNumeratorCohort(
                    TxTbMonthlyCascadeCohortQueries.TxTbComposition.NUMERATOR_AND_PREVIOUSLYART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "NUMERATORPREVIOUSLYRT",
        "NUMERATORPREVIOUSLYRT",
        EptsReportUtils.map(NUMERATORPREVIOUSLYRT, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "NUMERATORPREVIOUSLYRT",
        "NUMERATORPREVIOUSLYRT",
        EptsReportUtils.map(NUMERATORPREVIOUSLYRT, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());
    // 8b. TX_TB numeratornot died not transferred-out (screened patients who initiated TB
    // treatment): Number of ART patients currently receiving ART (TX_CURR), screened for TB in the
    // last 6 months who initiated TB treatment and are thus not dead or not Transferred-out

    CohortIndicator NUMERATORTXCURRTOTAL =
        eptsGeneralIndicator.getIndicator(
            "NUMERATORTXCURRTOTAL",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getTxTbNumeratorCohort(
                    TxTbMonthlyCascadeCohortQueries.TxTbComposition.NUMERATOR_AND_TXCURR),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));

    cohortIndicatorDefinition.addColumn(
        "NUMERATORTXCURRTOTAL",
        "NUMERATORTXCURRTOTAL",
        EptsReportUtils.map(NUMERATORTXCURRTOTAL, "endDate=${endDate},location=${location}"),
        "");

    CohortIndicator NUMTXCURRNEWART =
        eptsGeneralIndicator.getIndicator(
            "NUMTXCURRNEWART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getTxTbNumeratorCohort(
                    TxTbMonthlyCascadeCohortQueries.TxTbComposition
                        .NUMERATOR_AND_TXCURR_AND_NEWART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "NUMTXCURRNEWART",
        "NUMTXCURRNEWART ",
        EptsReportUtils.map(NUMTXCURRNEWART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "NUMTXCURRNEWART",
        "NUMTXCURRNEWART ",
        EptsReportUtils.map(NUMTXCURRNEWART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    CohortIndicator NUMTXCURPREVART =
        eptsGeneralIndicator.getIndicator(
            "NUMTXCURPREVART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getTxTbNumeratorCohort(
                    TxTbMonthlyCascadeCohortQueries.TxTbComposition
                        .NUMERATOR_AND_TXCURR_PREVIOUSLYART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "NUMTXCURPREVART",
        "NUMTXCURPREVART",
        EptsReportUtils.map(NUMTXCURPREVART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "NUMTXCURPREVART",
        "NUMTXCURPREVART",
        EptsReportUtils.map(NUMTXCURPREVART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());
    // Indicator 5

    CohortIndicator FIVE =
        eptsGeneralIndicator.getIndicator(
            "FIVE",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition.FIVE),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "FIVE", "FIVE", EptsReportUtils.map(FIVE, "endDate=${endDate},location=${location}"), "");

    // ind. FIVE - SMEAR ONLY
    CohortIndicator FIVESEMEAR =
        eptsGeneralIndicator.getIndicator(
            "FIVESEMEAR",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition.FIVE_AND_SEMEAR),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "FIVESEMEAR",
        "FIVESEMEAR",
        EptsReportUtils.map(FIVESEMEAR, "endDate=${endDate},location=${location}"),
        "");

    // FIVE SMEAR ONLY - NEW ON ART
    CohortIndicator FIVESEMEARNEWART =
        eptsGeneralIndicator.getIndicator(
            "FIVESEMEARNEWART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .FIVE_AND_SEMEAR_AND_NEWART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "FIVESEMEARNEWART",
        "FIVESEMEARNEWART",
        EptsReportUtils.map(FIVESEMEARNEWART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "FIVESEMEARNEWART",
        "FIVESEMEARNEWART",
        EptsReportUtils.map(FIVESEMEARNEWART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // FIVE SMEAR ONLY - ALREADY ON ART
    CohortIndicator FIVESEMEARALREADYART =
        eptsGeneralIndicator.getIndicator(
            " FIVESEMEARALREADYART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .FIVE_AND_SEMEAR_AND_PREVIOUSLYART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "FIVESEMEARALREADYART",
        "FIVESEMEARALREADYART",
        EptsReportUtils.map(FIVESEMEARALREADYART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "FIVESEMEARALREADYART",
        "FIVESEMEARALREADYART",
        EptsReportUtils.map(FIVESEMEARALREADYART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // ind. Five - MWRD
    CohortIndicator FIVEMWRD =
        eptsGeneralIndicator.getIndicator(
            "FIVEMWRD",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition.FIVE_AND_MWRD),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "FIVEMWRD",
        "FIVEMWRD",
        EptsReportUtils.map(FIVEMWRD, "endDate=${endDate},location=${location}"),
        "");

    // FIVE MWRD - NEW ON ART
    CohortIndicator FIVEMWRDNEWART =
        eptsGeneralIndicator.getIndicator(
            "FIVEMWRDNEWART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .FIVE_AND_MWRD_AND_NEWART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "FIVEMWRDNEWART",
        "FIVEMWRDNEWART",
        EptsReportUtils.map(FIVEMWRDNEWART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "FIVEMWRDNEWART",
        "FIVEMWRDNEWART",
        EptsReportUtils.map(FIVEMWRDNEWART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // FIVE MWRD - ALREADY ON ART
    CohortIndicator FIVEMWRDALREADYART =
        eptsGeneralIndicator.getIndicator(
            "FIVEMWRDALREADYART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .FIVE_AND_MWRD_AND_PREVIOUSLYART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "FIVEMWRDALREADYART",
        "FIVEMWRDALREADYART",
        EptsReportUtils.map(FIVEMWRDALREADYART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "FIVEMWRDALREADYART",
        "FIVEMWRDALREADYART",
        EptsReportUtils.map(FIVEMWRDALREADYART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // ind. Five - TBLAM
    CohortIndicator FIVETBLAM =
        eptsGeneralIndicator.getIndicator(
            "FIVETBLAM",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition.FIVE_AND_TBLAM),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "FIVETBLAM",
        "FIVETBLAM",
        EptsReportUtils.map(FIVETBLAM, "endDate=${endDate},location=${location}"),
        "");

    // FIVE TBLAM - NEW ON ART
    CohortIndicator FIVETBLAMNEWART =
        eptsGeneralIndicator.getIndicator(
            "FIVETBLAMNEWART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .FIVE_AND_TBLAM_AND_NEWART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "FIVETBLAMNEWART",
        "FIVETBLAMNEWART",
        EptsReportUtils.map(FIVETBLAMNEWART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "FIVETBLAMNEWART",
        "FIVETBLAMNEWART",
        EptsReportUtils.map(FIVETBLAMNEWART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // FIVE TBLAM - ALREADY ON ART
    CohortIndicator FIVETBLAMALREADYART =
        eptsGeneralIndicator.getIndicator(
            "FIVETBLAMALREADYART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .FIVE_AND_TBLAM_AND_PREVIOUSLYART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "FIVETBLAMALREADYART",
        "FIVETBLAMALREADYART",
        EptsReportUtils.map(FIVETBLAMALREADYART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "FIVETBLAMALREADYART",
        "FIVETBLAMALREADYART",
        EptsReportUtils.map(FIVETBLAMALREADYART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // ind. Five - OTHER
    CohortIndicator FIVETOTHER =
        eptsGeneralIndicator.getIndicator(
            "FIVETOTHER",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition.FIVE_AND_OTHER),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "FIVETOTHER",
        "FIVETOTHER",
        EptsReportUtils.map(FIVETOTHER, "endDate=${endDate},location=${location}"),
        "");

    // FIVE OTHER - NEW ON ART
    CohortIndicator FIVEOTHERNEWART =
        eptsGeneralIndicator.getIndicator(
            "FIVEOTHERNEWART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .FIVE_AND_OTHER_AND_NEWART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "FIVEOTHERNEWART",
        "FIVEOTHERNEWART",
        EptsReportUtils.map(FIVEOTHERNEWART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "FIVEOTHERNEWART",
        "FIVEOTHERNEWART",
        EptsReportUtils.map(FIVEOTHERNEWART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // FIVE OTHER - ALREADY ON ART
    CohortIndicator FIVEOTHERALREADYART =
        eptsGeneralIndicator.getIndicator(
            " FIVEOTHERALREADYART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .FIVE_AND_OTHER_AND_PREVIOUSLYART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "FIVEOTHERALREADYART",
        "FIVEOTHERALREADYART",
        EptsReportUtils.map(FIVEOTHERALREADYART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "FIVEOTHERALREADYART",
        "FIVEOTHERALREADYART",
        EptsReportUtils.map(FIVEOTHERALREADYART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // ind. SIX A
    CohortIndicator SIXA =
        eptsGeneralIndicator.getIndicator(
            "SIXA",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition.SIXA),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SIXA", "SIXA", EptsReportUtils.map(SIXA, "endDate=${endDate},location=${location}"), "");

    // SIX A - SMEAR ONLY
    CohortIndicator SIXASEMEAR =
        eptsGeneralIndicator.getIndicator(
            "SIXASEMEAR",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition.SIXA_AND_SEMEAR),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SIXASEMEAR",
        "SIXASEMEARA",
        EptsReportUtils.map(SIXASEMEAR, "endDate=${endDate},location=${location}"),
        "");

    // SIX A SMEAR ONLY - NEW ON ART
    CohortIndicator SIXASEMEARNEWART =
        eptsGeneralIndicator.getIndicator(
            "SIXASEMEARNEWART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .SIXA_AND_SEMEAR_AND_NEWART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SIXASEMEARNEWART",
        "SIXASEMEARNEWART",
        EptsReportUtils.map(SIXASEMEARNEWART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "SIXASEMEARNEWART",
        "SIXASEMEARNEWART",
        EptsReportUtils.map(SIXASEMEARNEWART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // SIX A SMEAR ONLY - ALREADY ON ART
    CohortIndicator SIXASEMEARALREADYART =
        eptsGeneralIndicator.getIndicator(
            "SIXASEMEARALREADYART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .SIXA_AND_SEMEAR_AND_PREVIOUSLYART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SIXASEMEARALREADYART",
        "SIXASEMEARALREADYART",
        EptsReportUtils.map(SIXASEMEARALREADYART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "SIXASEMEARALREADYART",
        "SIXASEMEARALREADYART",
        EptsReportUtils.map(SIXASEMEARALREADYART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // SIXA - MWRD
    CohortIndicator SIXAMWRD =
        eptsGeneralIndicator.getIndicator(
            "SIXAMWRD",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition.SIXA_AND_MWRD),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SIXAMWRD",
        "SIXAMWRD",
        EptsReportUtils.map(SIXAMWRD, "endDate=${endDate},location=${location}"),
        "");

    // SIXA MWRD - NEW ON ART
    CohortIndicator SIXAMWRDNEWART =
        eptsGeneralIndicator.getIndicator(
            "SIXAMWRDNEWART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .SIXA_AND_MWRD_AND_NEWART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SIXAMWRDNEWART",
        "SIXAMWRDNEWART",
        EptsReportUtils.map(SIXAMWRDNEWART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "SIXAMWRDNEWART",
        "SIXAMWRDNEWART",
        EptsReportUtils.map(SIXAMWRDNEWART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // SIXA MWRD - ALREADY ON ART
    CohortIndicator SIXAMWRDALREADYART =
        eptsGeneralIndicator.getIndicator(
            "SIXAMWRDALREADYART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .SIXA_AND_MWRD_AND_PREVIOUSLYART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SIXAMWRDALREADYART",
        "SIXAMWRDALREADYART",
        EptsReportUtils.map(SIXAMWRDALREADYART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "SIXAMWRDALREADYART",
        "SIXAMWRDALREADYART",
        EptsReportUtils.map(SIXAMWRDALREADYART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // SIXA - TBLAM
    CohortIndicator SIXATBLAM =
        eptsGeneralIndicator.getIndicator(
            "SIXATBLAM",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition.SIXA_AND_TBLAM),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SIXATBLAM",
        "SIXATBLAM",
        EptsReportUtils.map(SIXATBLAM, "endDate=${endDate},location=${location}"),
        "");

    // SIXA TBLAM - NEW ON ART
    CohortIndicator SIXATBLAMNEWART =
        eptsGeneralIndicator.getIndicator(
            "SIXATBLAMNEWART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .SIXA_AND_TBLAM_AND_NEWART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SIXATBLAMNEWART",
        "SIXATBLAMNEWART",
        EptsReportUtils.map(SIXATBLAMNEWART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "SIXATBLAMNEWART",
        "SIXATBLAMNEWART",
        EptsReportUtils.map(SIXATBLAMNEWART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // SIXA TBLAM - ALREADY ON ART
    CohortIndicator SIXATBLAMALREADYART =
        eptsGeneralIndicator.getIndicator(
            "SIXATBLAMALREADYART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .SIXA_AND_TBLAM_AND_PREVIOUSLYART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SIXATBLAMALREADYART",
        "SIXATBLAMALREADYART",
        EptsReportUtils.map(SIXATBLAMALREADYART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "SIXATBLAMALREADYART",
        "SIXATBLAMALREADYART",
        EptsReportUtils.map(SIXATBLAMALREADYART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // SIXA - OTHER
    CohortIndicator SIXAOHTER =
        eptsGeneralIndicator.getIndicator(
            "SIXAOHTER",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition.SIXA_AND_OTHER),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SIXAOHTER",
        "SIXAOHTER",
        EptsReportUtils.map(SIXAOHTER, "endDate=${endDate},location=${location}"),
        "");

    // SIXA OTHER - NEW ON ART
    CohortIndicator SIXAOTHERNEWART =
        eptsGeneralIndicator.getIndicator(
            "SIXAOTHERNEWART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .SIXA_AND_OTHER_AND_NEWART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SIXAOTHERNEWART",
        "SIXAOTHERNEWART",
        EptsReportUtils.map(SIXAOTHERNEWART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "SIXAOTHERNEWART",
        "SIXAOTHERNEWART",
        EptsReportUtils.map(SIXAOTHERNEWART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // SIXA OTHER - ALREADY ON ART
    CohortIndicator SIXAOTHERALREADYART =
        eptsGeneralIndicator.getIndicator(
            " SIXAOTHERALREADYART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .SIXA_AND_OTHER_AND_PREVIOUSLYART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SIXAOTHERALREADYART",
        "SIXAOTHERALREADYART",
        EptsReportUtils.map(SIXAOTHERALREADYART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "SIXAOTHERALREADYART",
        "SIXAOTHERALREADYART",
        EptsReportUtils.map(SIXAOTHERALREADYART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // ind. SIX B
    CohortIndicator SIXB =
        eptsGeneralIndicator.getIndicator(
            "SIXB",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition.SIXB),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SIXB", "SIXB", EptsReportUtils.map(SIXB, "endDate=${endDate},location=${location}"), "");

    // SIX B - SMEAR ONLY
    CohortIndicator SIXBSEMEAR =
        eptsGeneralIndicator.getIndicator(
            "SIXBSEMEAR",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition.SIXB_AND_SEMEAR),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SIXBSEMEAR",
        "SIXBSEMEAR",
        EptsReportUtils.map(SIXBSEMEAR, "endDate=${endDate},location=${location}"),
        "");

    // SIX B SMEAR ONLY - NEW ON ART
    CohortIndicator SIXBSEMEARNEWART =
        eptsGeneralIndicator.getIndicator(
            "SIXBSEMEARNEWART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .SIXB_AND_SEMEAR_AND_NEWART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SIXBSEMEARNEWART",
        "SIXBSEMEARNEWART",
        EptsReportUtils.map(SIXBSEMEARNEWART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "SIXBSEMEARNEWART",
        "SIXBSEMEARNEWART",
        EptsReportUtils.map(SIXBSEMEARNEWART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // SIX B SMEAR ONLY - ALREADY ON ART
    CohortIndicator SIXBSEMEARALREADYART =
        eptsGeneralIndicator.getIndicator(
            "SIXBSEMEARALREADYART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .SIXB_AND_SEMEAR_AND_PREVIOUSLYART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SIXBSEMEARALREADYART",
        "SIXBSEMEARALREADYART",
        EptsReportUtils.map(SIXBSEMEARALREADYART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "SIXBSEMEARALREADYART",
        "SIXBSEMEARALREADYART",
        EptsReportUtils.map(SIXBSEMEARALREADYART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // SIXB -MWRD
    CohortIndicator SIXBMWRD =
        eptsGeneralIndicator.getIndicator(
            "SIXBMWRD",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition.SIXB_AND_MWRD),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SIXBMWRD",
        "SIXBMWRD",
        EptsReportUtils.map(SIXBMWRD, "endDate=${endDate},location=${location}"),
        "");

    // SIXB MWRD - NEW ON ART
    CohortIndicator SIXBMWRDNEWART =
        eptsGeneralIndicator.getIndicator(
            "SIXBMWRDNEWART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .SIXB_AND_MWRD_AND_NEWART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SIXBMWRDNEWART",
        "SIXBMWRDNEWART",
        EptsReportUtils.map(SIXBMWRDNEWART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "SIXBMWRDNEWART",
        "SIXBMWRDNEWART",
        EptsReportUtils.map(SIXBMWRDNEWART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // SIXB MWRD - ALREADY ON ART
    CohortIndicator SIXBMWRDALREADYART =
        eptsGeneralIndicator.getIndicator(
            "SIXBMWRDALREADYART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .SIXB_AND_MWRD_AND_PREVIOUSLYART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SIXBMWRDALREADYART",
        "SIXBMWRDALREADYART",
        EptsReportUtils.map(SIXBMWRDALREADYART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "SIXBMWRDALREADYART",
        "SIXBMWRDALREADYART",
        EptsReportUtils.map(SIXBMWRDALREADYART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // SIXB - TBLAM
    CohortIndicator SIXBTBLAM =
        eptsGeneralIndicator.getIndicator(
            "SIXBTBLAM",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition.SIXB_AND_TBLAM),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SIXBTBLAM",
        "SIXBTBLAM",
        EptsReportUtils.map(SIXBTBLAM, "endDate=${endDate},location=${location}"),
        "");

    // SIXB TBLAM - NEW ON ART
    CohortIndicator SIXBTBLAMNEWART =
        eptsGeneralIndicator.getIndicator(
            "SIXBTBLAMNEWART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .SIXB_AND_TBLAM_AND_NEWART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SIXBTBLAMNEWART",
        "SIXBTBLAMNEWART",
        EptsReportUtils.map(SIXBTBLAMNEWART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "SIXBTBLAMNEWART",
        "SIXBTBLAMNEWART",
        EptsReportUtils.map(SIXBTBLAMNEWART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // SIXB TBLAM - ALREADY ON ART
    CohortIndicator SIXBTBLAMALREADYART =
        eptsGeneralIndicator.getIndicator(
            "SIXBTBLAMALREADYART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .SIXB_AND_TBLAM_AND_PREVIOUSLYART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SIXBTBLAMALREADYART",
        "SIXBTBLAMALREADYART",
        EptsReportUtils.map(SIXBTBLAMALREADYART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "SIXBTBLAMALREADYART",
        "SIXBTBLAMALREADYART",
        EptsReportUtils.map(SIXBTBLAMALREADYART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // SIXB - OTHER
    CohortIndicator SIXBOTHER =
        eptsGeneralIndicator.getIndicator(
            "SIXBOTHER",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition.SIXB_AND_OTHER),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SIXBOTHER",
        "SIXBOTHER",
        EptsReportUtils.map(SIXBOTHER, "endDate=${endDate},location=${location}"),
        "");

    // SIXB OTHER - NEW ON ART
    CohortIndicator SIXBOTHERNEWART =
        eptsGeneralIndicator.getIndicator(
            "SIXBOTHERNEWART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .SIXB_AND_OTHER_AND_NEWART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SIXBOTHERNEWART",
        "SIXBOTHERNEWART",
        EptsReportUtils.map(SIXBOTHERNEWART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "SIXBOTHERNEWART",
        "SIXBOTHERNEWART",
        EptsReportUtils.map(SIXBOTHERNEWART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // SIXB OTHER - ALREADY ON ART
    CohortIndicator SIXBOTHERALREADYART =
        eptsGeneralIndicator.getIndicator(
            " SIXBOTHERALREADYART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .SIXB_AND_OTHER_AND_PREVIOUSLYART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SIXBOTHERALREADYART",
        "SIXBOTHERALREADYART",
        EptsReportUtils.map(SIXBOTHERALREADYART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "SIXBOTHERALREADYART",
        "SIXBOTHERALREADYART",
        EptsReportUtils.map(SIXBOTHERALREADYART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // ind. SEVEN
    CohortIndicator SEVEN =
        eptsGeneralIndicator.getIndicator(
            "SEVEN",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition.SEVEN),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SEVEN",
        "SEVEN",
        EptsReportUtils.map(SEVEN, "endDate=${endDate},location=${location}"),
        "");

    // SEVEN - SMEAR ONLY
    CohortIndicator SEVENSEMEAR =
        eptsGeneralIndicator.getIndicator(
            "SEVENSEMEAR",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition.SEVEN_AND_SEMEAR),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SEVENSEMEAR",
        "SEVENSEMEAR",
        EptsReportUtils.map(SEVENSEMEAR, "endDate=${endDate},location=${location}"),
        "");

    // SEVEN SMEAR ONLY - NEW ON ART
    CohortIndicator SEVENSEMEARNEWART =
        eptsGeneralIndicator.getIndicator(
            "SEVENSEMEARNEWART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .SEVEN_AND_SEMEAR_AND_NEWART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SEVENSEMEARNEWART",
        "SEVENSEMEARNEWART",
        EptsReportUtils.map(SEVENSEMEARNEWART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "SEVENSEMEARNEWART",
        "SEVENSEMEARNEWART",
        EptsReportUtils.map(SEVENSEMEARNEWART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // SEVEN SMEAR ONLY - ALREADY ON ART
    CohortIndicator SEVENSEMEARALREADYART =
        eptsGeneralIndicator.getIndicator(
            "SEVENSEMEARALREADYART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .SEVEN_AND_SEMEAR_AND_PREVIOUSLYART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SEVENSEMEARALREADYART",
        "SEVENSEMEARALREADYART",
        EptsReportUtils.map(SEVENSEMEARALREADYART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "SEVENSEMEARALREADYART",
        "SEVENSEMEARALREADYART",
        EptsReportUtils.map(SEVENSEMEARALREADYART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // SEVEN - MWRD
    CohortIndicator SEVENMWRD =
        eptsGeneralIndicator.getIndicator(
            "SEVENMWRD",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition.SEVEN_AND_MWRD),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SEVENMWRD",
        "SEVENMWRD",
        EptsReportUtils.map(SEVENMWRD, "endDate=${endDate},location=${location}"),
        "");

    // SEVEN MWRD - NEW ON ART
    CohortIndicator SEVENMWRDNEWART =
        eptsGeneralIndicator.getIndicator(
            "SEVENMWRDNEWART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .SEVEN_AND_MWRD_AND_NEWART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SEVENMWRDNEWART",
        "SEVENMWRDNEWART",
        EptsReportUtils.map(SEVENMWRDNEWART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "SEVENMWRDNEWART",
        "SEVENMWRDNEWART",
        EptsReportUtils.map(SEVENMWRDNEWART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // SEVEN MWRD - ALREADY ON ART
    CohortIndicator SEVENMWRDALREADYART =
        eptsGeneralIndicator.getIndicator(
            "SEVENMWRDALREADYART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .SEVEN_AND_MWRD_AND_PREVIOUSLYART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SEVENMWRDALREADYART",
        "SEVENMWRDALREADYART",
        EptsReportUtils.map(SEVENMWRDALREADYART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "SEVENMWRDALREADYART",
        "SEVENMWRDALREADYART",
        EptsReportUtils.map(SEVENMWRDALREADYART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // SEVEN - TBLAM
    CohortIndicator SEVENTBLAM =
        eptsGeneralIndicator.getIndicator(
            "SEVENTBLAM",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition.SEVEN_AND_TBLAM),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SEVENTBLAM",
        "SEVENTBLAM",
        EptsReportUtils.map(SEVENTBLAM, "endDate=${endDate},location=${location}"),
        "");

    // SEVEN TBLAM - NEW ON ART
    CohortIndicator SEVENTBLAMNEWART =
        eptsGeneralIndicator.getIndicator(
            "SEVENTBLAMNEWART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .SEVEN_AND_TBLAM_AND_NEWART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SEVENTBLAMNEWART",
        "SEVENTBLAMNEWART",
        EptsReportUtils.map(SEVENTBLAMNEWART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "SEVENTBLAMNEWART",
        "SEVENTBLAMNEWART",
        EptsReportUtils.map(SEVENTBLAMNEWART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // SEVEN TBLAM - ALREADY ON ART
    CohortIndicator SEVENTBLAMALREADYART =
        eptsGeneralIndicator.getIndicator(
            "SEVENTBLAMALREADYART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .SEVEN_AND_TBLAM_AND_PREVIOUSLYART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SEVENTBLAMALREADYART",
        "SEVENTBLAMALREADYART",
        EptsReportUtils.map(SEVENTBLAMALREADYART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "SEVENTBLAMALREADYART",
        "SEVENTBLAMALREADYART",
        EptsReportUtils.map(SEVENTBLAMALREADYART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // SEVEN - OTHER
    CohortIndicator SEVENOTHER =
        eptsGeneralIndicator.getIndicator(
            "SEVENOTHER",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition.SEVEN_AND_OTHER),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SEVENOTHER",
        "SEVENOTHER",
        EptsReportUtils.map(SEVENOTHER, "endDate=${endDate},location=${location}"),
        "");

    // SEVEN OTHER - NEW ON ART
    CohortIndicator SEVENOTHERNEWART =
        eptsGeneralIndicator.getIndicator(
            "SEVENOTHERNEWART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .SEVEN_AND_OTHER_AND_NEWART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SEVENOTHERNEWART",
        "SEVENOTHERNEWART",
        EptsReportUtils.map(SEVENOTHERNEWART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "SEVENOTHERNEWART",
        "SEVENOTHERNEWART",
        EptsReportUtils.map(SEVENOTHERNEWART, "endDate=${endDate},location=${location}"),
        getSexAndAgeDimension());

    // SEVEN OTHER - ALREADY ON ART
    CohortIndicator SEVENOTHERALREADYART =
        eptsGeneralIndicator.getIndicator(
            "SEVENOTHERALREADYART",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.get5And6and7(
                    TxTbMonthlyCascadeCohortQueries.SemearTbLamGXPertComposition
                        .SEVEN_AND_OTHER_AND_PREVIOUSLYART),
                "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));
    cohortIndicatorDefinition.addColumn(
        "SEVENOTHERALREADYART",
        "SEVENOTHERALREADYART",
        EptsReportUtils.map(SEVENOTHERALREADYART, "endDate=${endDate},location=${location}"),
        "");
    addRow(
        cohortIndicatorDefinition,
        "SEVENOTHERALREADYART",
        "SEVENOTHERALREADYART",
        EptsReportUtils.map(SEVENOTHERALREADYART, "endDate=${endDate},location=${location}"),
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
