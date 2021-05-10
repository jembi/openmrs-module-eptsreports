package org.openmrs.module.eptsreports.reporting.library.datasets;

import org.openmrs.module.eptsreports.reporting.library.cohorts.TPTCompletionCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TPTListOfPatientsEligibleDataSet extends BaseDataSet {
  @Autowired private EptsGeneralIndicator eptsGeneralIndicator;
  @Autowired private TPTCompletionCohortQueries tPTCompletionCohortQueries;

  public DataSetDefinition constructDataset() {

    CohortIndicatorDataSetDefinition dataSetDefinition = new CohortIndicatorDataSetDefinition();
    dataSetDefinition.setName("TPT");
    dataSetDefinition.addParameters(getParameters());
    String mappings = "endDate=${endDate},location=${location}";

    CohortIndicator pregnantOrBreastfeeding =
        eptsGeneralIndicator.getIndicator(
            "BORP",
            EptsReportUtils.map(
                tPTCompletionCohortQueries.getTxCurrWithTPTInLast7Months(), mappings));
    dataSetDefinition.addColumn(
        "BORP",
        "pregnant Or Breastfeeding",
        EptsReportUtils.map(pregnantOrBreastfeeding, mappings),
        "");

    CohortIndicator lastSegmentDate =
        eptsGeneralIndicator.getIndicator(
            "LSD",
            EptsReportUtils.map(
                tPTCompletionCohortQueries.getTxCurrWithTPTInLast7Months(), mappings));
    dataSetDefinition.addColumn(
        "LSD", "Last Segment Date", EptsReportUtils.map(lastSegmentDate, mappings), "");

    CohortIndicator dateOfNextConsultation =
        eptsGeneralIndicator.getIndicator(
            "DONC",
            EptsReportUtils.map(
                tPTCompletionCohortQueries.getTxCurrWithTPTInLast7Months(), mappings));
    dataSetDefinition.addColumn(
        "DONC",
        "Date Of Next Consultation",
        EptsReportUtils.map(dateOfNextConsultation, mappings),
        "");

    CohortIndicator totalPatientsEligibleForTPT =
        eptsGeneralIndicator.getIndicator(
            "EDP",
            EptsReportUtils.map(
                tPTCompletionCohortQueries.getTxCurrWithTPTInLast7Months(), mappings));
    dataSetDefinition.addColumn(
        "EDP",
        "Total Patients Eligible For TPT",
        EptsReportUtils.map(totalPatientsEligibleForTPT, mappings),
        "");

    CohortIndicator endDatePeriod =
        eptsGeneralIndicator.getIndicator(
            "EDP",
            EptsReportUtils.map(
                tPTCompletionCohortQueries.getTxCurrWithTPTInLast7Months(), mappings));
    dataSetDefinition.addColumn(
        "EDP", "end date period", EptsReportUtils.map(endDatePeriod, mappings), "");

    return dataSetDefinition;
  }
}
