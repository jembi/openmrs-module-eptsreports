package org.openmrs.module.eptsreports.reporting.library.datasets;

import org.openmrs.module.eptsreports.reporting.library.cohorts.IMER1DenominatorCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IMER1DenominaorDataSet extends BaseDataSet {

  private final String MAPPINGS =
      "startDate=${endDate-1m-1d},endDate=${endDate-1m},location=${location}";

  private EptsGeneralIndicator eptsGeneralIndicator;

  private IMER1DenominatorCohortQueries imer1DenominatorCohortQueries;

  @Autowired
  public IMER1DenominaorDataSet(
      IMER1DenominatorCohortQueries imer1DenominatorCohortQueries,
      EptsGeneralIndicator eptsGeneralIndicator) {
    this.imer1DenominatorCohortQueries = imer1DenominatorCohortQueries;
    this.eptsGeneralIndicator = eptsGeneralIndicator;
  }

  public DataSetDefinition constructIMER1DenominaorDataSet() {

    CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();

    CohortIndicator all =
        eptsGeneralIndicator.getIndicator(
            "ALL", EptsReportUtils.map(imer1DenominatorCohortQueries.getAllPatients(), MAPPINGS));

    CohortIndicator breastfeeding =
        eptsGeneralIndicator.getIndicator(
            "BREASTFEEDING",
            EptsReportUtils.map(imer1DenominatorCohortQueries.getBreastfeedingWoman(), MAPPINGS));

    CohortIndicator pregnant =
        eptsGeneralIndicator.getIndicator(
            "PREGNANT",
            EptsReportUtils.map(imer1DenominatorCohortQueries.getPregnantWomen(), MAPPINGS));

    CohortIndicator adults =
        eptsGeneralIndicator.getIndicator(
            "ADULTS", EptsReportUtils.map(imer1DenominatorCohortQueries.getAdults(), MAPPINGS));

    CohortIndicator children =
        eptsGeneralIndicator.getIndicator(
            "CHILDREN",
            EptsReportUtils.map(imer1DenominatorCohortQueries.getChildreen(), MAPPINGS));

    dsd.addColumn("ALL", "ALL Patients", EptsReportUtils.map(all, MAPPINGS), "");
    dsd.addColumn("BREASTFEEDING", "", EptsReportUtils.map(breastfeeding, MAPPINGS), "");
    dsd.addColumn("PREGNANT", "", EptsReportUtils.map(pregnant, MAPPINGS), "");
    dsd.addColumn("ADULTS", "", EptsReportUtils.map(adults, MAPPINGS), "");
    dsd.addColumn("CHILDREN", "", EptsReportUtils.map(children, MAPPINGS), "");

    return dsd;
  }
}
