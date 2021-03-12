package org.openmrs.module.eptsreports.reporting.library.datasets;

import org.openmrs.module.eptsreports.reporting.library.cohorts.TPTCompletionCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.TxCurrCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.dimensions.AgeDimensionCohortInterface;
import org.openmrs.module.eptsreports.reporting.library.dimensions.EptsCommonDimension;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TPTCompletionDataSet extends BaseDataSet {

  @Autowired private EptsCommonDimension eptsCommonDimension;

  @Autowired private EptsGeneralIndicator eptsGeneralIndicator;

  @Autowired private TPTCompletionCohortQueries tPTCompletionCohortQueries;

  @Autowired private TxCurrCohortQueries txCurrCohortQueries;

  @Autowired
  @Qualifier("commonAgeDimensionCohort")
  private AgeDimensionCohortInterface ageDimensionCohort;

  public DataSetDefinition constructTPTCompletionDataSet() {

    CohortIndicatorDataSetDefinition dataSetDefinition = new CohortIndicatorDataSetDefinition();
    dataSetDefinition.setName("TPT Completion Cascade Report");
    dataSetDefinition.addParameters(getParameters());
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";

    /* add dimensions */
    dataSetDefinition.addDimension(
        "age",
        EptsReportUtils.map(
            eptsCommonDimension.age(ageDimensionCohort), "effectiveDate=${endDate}"));

    dataSetDefinition.addDimension(
        "ageInMonths",
        EptsReportUtils.map(eptsCommonDimension.ageInMonths(), "effectiveDate=${endDate}"));

    dataSetDefinition.addDimension(
        "ageBasedOnArt",
        EptsReportUtils.map(
            eptsCommonDimension.ageBasedOnArtStartDateMOH(),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));

    CohortDefinition txCurrCompositionCohort =
        txCurrCohortQueries.getTxCurrCompositionCohort("compositionCohort", true);

    CohortIndicator txCurrIndicator =
        eptsGeneralIndicator.getIndicator(
            "patientInYearRangeEnrolledInHIVStartedARTIndicator",
            EptsReportUtils.map(
                txCurrCompositionCohort, "onOrBefore=${endDate},location=${location}"));

    CohortIndicator txCurrWithTPTCompIndicator =
        eptsGeneralIndicator.getIndicator(
            "TX_CURRwithTPTCompletion",
            EptsReportUtils.map(tPTCompletionCohortQueries.getTxCurrWithTPTCompletion(), mappings));

    dataSetDefinition.addColumn(
        "TXCURR", "TX_CURR: Currently on ART", EptsReportUtils.map(txCurrIndicator, mappings), "");

    dataSetDefinition.addColumn(
        "TXCURRTPTCOMP",
        "TX_CURR with TPT Completion",
        EptsReportUtils.map(txCurrWithTPTCompIndicator, mappings),
        "");

    return dataSetDefinition;
  }
}
