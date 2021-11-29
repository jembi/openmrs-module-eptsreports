package org.openmrs.module.eptsreports.reporting.library.datasets;

import org.openmrs.module.eptsreports.reporting.library.cohorts.TxPvlsBySourceClinicalOrFichaResumoCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.TxPvlsBySourceLabOrFsrCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.dimensions.AgeDimensionCohortInterface;
import org.openmrs.module.eptsreports.reporting.library.dimensions.EptsCommonDimension;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TxPvlsBySourceDataset extends BaseDataSet {

  private TxPvlsBySourceLabOrFsrCohortQueries txPvlsBySourceLabOrFsrCohortQueries;
  private EptsCommonDimension eptsCommonDimension;
  private EptsGeneralIndicator eptsGeneralIndicator;
  private AgeDimensionCohortInterface ageDimensionCohortInterface;

  private TxPvlsBySourceClinicalOrFichaResumoCohortQueries
      txPvlsBySourceClinicalOrFichaResumoCohortQueries;

  @Autowired
  public TxPvlsBySourceDataset(
      TxPvlsBySourceLabOrFsrCohortQueries txPvlsBySourceLabOrFsrCohortQueries,
      TxPvlsBySourceClinicalOrFichaResumoCohortQueries
          txPvlsBySourceClinicalOrFichaResumoCohortQueries,
      EptsCommonDimension eptsCommonDimension,
      EptsGeneralIndicator eptsGeneralIndicator,
      @Qualifier("commonAgeDimensionCohort")
          AgeDimensionCohortInterface ageDimensionCohortInterface) {
    this.txPvlsBySourceLabOrFsrCohortQueries = txPvlsBySourceLabOrFsrCohortQueries;
    this.txPvlsBySourceClinicalOrFichaResumoCohortQueries =
        txPvlsBySourceClinicalOrFichaResumoCohortQueries;
    this.eptsCommonDimension = eptsCommonDimension;
    this.eptsGeneralIndicator = eptsGeneralIndicator;
    this.ageDimensionCohortInterface = ageDimensionCohortInterface;
  }

  String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";
  String mappingsKp = "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}";

  public DataSetDefinition getPvlsLabFsr() {
    CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
    dsd.setName("PLF");
    dsd.setDescription("PVLS_LAB_FSR");
    dsd.addParameters(getParameters());
    // Tie dimensions to this data definition
    dsd.addDimension("gender", EptsReportUtils.map(eptsCommonDimension.gender(), ""));
    dsd.addDimension(
        "query", EptsReportUtils.map(eptsCommonDimension.maternityDimension(), mappings));
    dsd.addDimension(
        "age",
        EptsReportUtils.map(
            eptsCommonDimension.age(ageDimensionCohortInterface), "effectiveDate=${endDate}"));
    dsd.addDimension(
        "rt",
        EptsReportUtils.map(
            eptsCommonDimension.getViralLoadRoutineTargetReasonsDimension(), mappings));
    dsd.addDimension(
        "KP", EptsReportUtils.map(eptsCommonDimension.getKeyPopsDimension(), mappingsKp));

    addNumeratorColumnsForLabAndFsr(dsd, mappings);
    addDenominatorColumnsForLabAndFsr(dsd, mappings);
    return dsd;
  }

  public DataSetDefinition getPvlFichaMestre() {
    CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
    dsd.setName("PFM");
    dsd.setDescription("PVLS_Ficha_Mestre");
    dsd.addParameters(getParameters());
    // Tie dimensions to this data definition
    dsd.addDimension("gender", EptsReportUtils.map(eptsCommonDimension.gender(), ""));
    dsd.addDimension(
        "query", EptsReportUtils.map(eptsCommonDimension.maternityDimension(), mappings));
    dsd.addDimension(
        "age",
        EptsReportUtils.map(
            eptsCommonDimension.age(ageDimensionCohortInterface), "effectiveDate=${endDate}"));
    dsd.addDimension(
        "rt",
        EptsReportUtils.map(
            eptsCommonDimension.getViralLoadRoutineTargetReasonsDimension(), mappings));
    dsd.addDimension(
        "KP", EptsReportUtils.map(eptsCommonDimension.getKeyPopsDimension(), mappingsKp));
    addDenominatorColumnsForClinicalForms(dsd, mappings);
    addNumeratorColumnsForClinicalForms(dsd, mappings);
    return dsd;
  }

  private void addDenominatorColumnsForLabAndFsr(
      CohortIndicatorDataSetDefinition dsd, String mappings) {}

  private void addNumeratorColumnsForLabAndFsr(
      CohortIndicatorDataSetDefinition dsd, String mappings) {}

  private void addDenominatorColumnsForClinicalForms(
      CohortIndicatorDataSetDefinition dsd, String mappings) {}

  private void addNumeratorColumnsForClinicalForms(
      CohortIndicatorDataSetDefinition dsd, String mappings) {}
}
