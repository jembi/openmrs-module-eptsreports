package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.Arrays;
import java.util.List;
import org.openmrs.module.eptsreports.reporting.library.cohorts.PrepCtCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.PrepNewCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.dimensions.EptsCommonDimension;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PrepOtherDisaggregationDataset extends BaseDataSet {
  private PrepCtCohortQueries prepCtCohortQueries;
  private EptsGeneralIndicator eptsGeneralIndicator;
  private EptsCommonDimension eptsCommonDimension;

  private PrepNewCohortQueries prepNewCohortQueries;

  @Autowired
  public PrepOtherDisaggregationDataset(
      PrepCtCohortQueries prepCtCohortQueries,
      EptsGeneralIndicator eptsGeneralIndicator,
      EptsCommonDimension eptsCommonDimension,
      PrepNewCohortQueries prepNewCohortQueries) {
    this.prepCtCohortQueries = prepCtCohortQueries;
    this.eptsGeneralIndicator = eptsGeneralIndicator;
    this.eptsCommonDimension = eptsCommonDimension;
    this.prepNewCohortQueries = prepNewCohortQueries;
  }

  /**
   * <b>Description:</b> Constructs Prep Other Disaggregation Dataset
   *
   * @return
   */
  public DataSetDefinition constructPrepOtherDisaggregationDataset() {

    CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
    dsd.setName("Prep Other Disaggregation Dataset");
    dsd.addParameters(getParameters());
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";
    String mappingsKp = "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}";

    dsd.addDimension(
        "KP", EptsReportUtils.map(eptsCommonDimension.getKeyPopsDimensionForPrep(), mappingsKp));

    dsd.addDimension(
        "prepInterruption",
        EptsReportUtils.map(
            eptsCommonDimension.getClientsWithReasonForPrepInterruptionDisaggregation(), mappings));

    dsd.addColumn(
        "NEWMSW",
        "PREP NEW: Male sex workers",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "Male sex workers",
                EptsReportUtils.map(
                    prepNewCohortQueries.getClientsWhoNewlyInitiatedPrep(), mappings)),
            mappings),
        "KP=MSW");

    dsd.addColumn(
        "CTMSW",
        "PREP CT: Male sex workers",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "Male sex workers",
                EptsReportUtils.map(prepCtCohortQueries.getPREPCTNumerator(), mappings)),
            mappings),
        "KP=MSW");

    addRow(
        dsd,
        "PREPCT",
        "Prep Interruption Reasons",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "Prep Interruption Reasons",
                EptsReportUtils.map(prepCtCohortQueries.getPREPCTNumerator(), mappings)),
            mappings),
        getColumnsForAgeAndGender());

    return dsd;
  }

  /**
   * <b>Description:</b> Creates disaggregation based on Age and Gender
   *
   * @return
   */
  private List<ColumnParameters> getColumnsForAgeAndGender() {

    // PrepInterruption Reasons
    ColumnParameters hivInfected =
        new ColumnParameters("hivinfected", "HIV Infected", "prepInterruption=hivInfected", "1");
    ColumnParameters adverseReaction =
        new ColumnParameters(
            "adversereaction", "Adverse Reaction", "prepInterruption=adverseReaction", "2");
    ColumnParameters noMoreSubstantialRisks =
        new ColumnParameters(
            "nomoresubstantialrisks",
            "No More Substantial Risks",
            "prepInterruption=noMoreSubstantialRisks",
            "3");
    ColumnParameters userPreference =
        new ColumnParameters(
            "userpreference", "User Preference", "prepInterruption=userPreference", "4");
    ColumnParameters otherReasons =
        new ColumnParameters("other", "Other (PrepInterruption)", "prepInterruption=other", "5");

    return Arrays.asList(
        hivInfected, adverseReaction, noMoreSubstantialRisks, userPreference, otherReasons);
  }
}
