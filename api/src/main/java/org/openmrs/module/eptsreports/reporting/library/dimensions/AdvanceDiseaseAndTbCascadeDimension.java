package org.openmrs.module.eptsreports.reporting.library.dimensions;

import java.util.Date;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.library.cohorts.AdvancedDiseaseAndTBCascadeCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ListOfPatientsArtCohortCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.TxNewCohortQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AdvanceDiseaseAndTbCascadeDimension {

  private AdvancedDiseaseAndTBCascadeCohortQueries advancedDiseaseAndTBCascadeCohortQueries;
  private ListOfPatientsArtCohortCohortQueries listOfPatientsArtCohortCohortQueries;
  private TxNewCohortQueries txNewCohortQueries;

  private final String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";

  private final String pregnancyPeriod =
      "startDate=${endDate-8m},endDate=${endDate},location=${location}";

  @Autowired
  public AdvanceDiseaseAndTbCascadeDimension(
      AdvancedDiseaseAndTBCascadeCohortQueries advancedDiseaseAndTBCascadeCohortQueries,
      ListOfPatientsArtCohortCohortQueries listOfPatientsArtCohortCohortQueries,
      TxNewCohortQueries txNewCohortQueries) {

    this.advancedDiseaseAndTBCascadeCohortQueries = advancedDiseaseAndTBCascadeCohortQueries;
    this.listOfPatientsArtCohortCohortQueries = listOfPatientsArtCohortCohortQueries;
    this.txNewCohortQueries = txNewCohortQueries;
  }

  public CohortDefinitionDimension getPatientWithPositiveTbLamAndGradeDimension() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.addParameter(new Parameter("startDate", "startDate", Date.class));
    dim.addParameter(new Parameter("endDate", "endDate", Date.class));
    dim.addParameter(new Parameter("location", "location", Location.class));
    dim.setName("Patients having Positive TB LAM and Grade - Dimension");

    dim.addCohortDefinition(
        "4+",
        EptsReportUtils.map(
            advancedDiseaseAndTBCascadeCohortQueries.getPatientsWithPositiveTbLamAndGradeFourPlus(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    dim.addCohortDefinition(
        "3+",
        EptsReportUtils.map(
            advancedDiseaseAndTBCascadeCohortQueries
                .getPatientsWithPositiveTbLamAndGradeThreePlus(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    dim.addCohortDefinition(
        "2+",
        EptsReportUtils.map(
            advancedDiseaseAndTBCascadeCohortQueries.getPatientsWithPositiveTbLamAndGradeTwoPlus(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    dim.addCohortDefinition(
        "1+",
        EptsReportUtils.map(
            advancedDiseaseAndTBCascadeCohortQueries.getPatientsWithPositiveTbLamAndGradeOnePlus(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    dim.addCohortDefinition(
        "notReported",
        EptsReportUtils.map(
            advancedDiseaseAndTBCascadeCohortQueries
                .getPatientsWithPositiveTbLamAndGradeNotReported(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    return dim;
  }

  public CohortDefinitionDimension getPatientsAbsoluteCd4Count() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.addParameter(new Parameter("startDate", "startDate", Date.class));
    dim.addParameter(new Parameter("endDate", "endDate", Date.class));
    dim.addParameter(new Parameter("location", "location", Location.class));
    dim.setName("Clients with CD4 count ");

    dim.addCohortDefinition(
        "200-",
        EptsReportUtils.map(
            advancedDiseaseAndTBCascadeCohortQueries.getPatientsWithAbsoluteCd4Count(
                AdvancedDiseaseAndTBCascadeCohortQueries.Cd4CountComparison
                    .LessThanOrEqualTo200mm3),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    dim.addCohortDefinition(
        "500-",
        EptsReportUtils.map(
            advancedDiseaseAndTBCascadeCohortQueries.getPatientsWithAbsoluteCd4Count(
                AdvancedDiseaseAndTBCascadeCohortQueries.Cd4CountComparison
                    .LessThanOrEqualTo500mm3),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    dim.addCohortDefinition(
        "750-",
        EptsReportUtils.map(
            advancedDiseaseAndTBCascadeCohortQueries.getPatientsWithAbsoluteCd4Count(
                AdvancedDiseaseAndTBCascadeCohortQueries.Cd4CountComparison
                    .LessThanOrEqualTo750mm3),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    dim.addCohortDefinition(
        "200+",
        EptsReportUtils.map(
            advancedDiseaseAndTBCascadeCohortQueries.getPatientsWithAbsoluteCd4Count(
                AdvancedDiseaseAndTBCascadeCohortQueries.Cd4CountComparison
                    .GreaterThanOrEqualTo200mm3),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    dim.addCohortDefinition(
        "500+",
        EptsReportUtils.map(
            advancedDiseaseAndTBCascadeCohortQueries.getPatientsWithAbsoluteCd4Count(
                AdvancedDiseaseAndTBCascadeCohortQueries.Cd4CountComparison
                    .GreaterThanOrEqualTo500mm3),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    dim.addCohortDefinition(
        "750+",
        EptsReportUtils.map(
            advancedDiseaseAndTBCascadeCohortQueries.getPatientsWithAbsoluteCd4Count(
                AdvancedDiseaseAndTBCascadeCohortQueries.Cd4CountComparison
                    .GreaterThanOrEqualTo750mm3),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    return dim;
  }

  public CohortDefinitionDimension getCd4EligibilityDisaggregations() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.addParameter(new Parameter("startDate", "startDate", Date.class));
    dim.addParameter(new Parameter("endDate", "endDate", Date.class));
    dim.addParameter(new Parameter("location", "location", Location.class));
    dim.setName("Clients with CD4 count Eligibbility");

    dim.addCohortDefinition(
        "initArt",
        EptsReportUtils.map(
            listOfPatientsArtCohortCohortQueries.getPatientsInitiatedART(), mappings));

    dim.addCohortDefinition(
        "pregnantClient",
        EptsReportUtils.map(
            advancedDiseaseAndTBCascadeCohortQueries.getPregnantsWithoutCD4Composition(),
            mappings));

    dim.addCohortDefinition(
        "consecutiveVl",
        EptsReportUtils.map(
            advancedDiseaseAndTBCascadeCohortQueries
                .getPatientsWithTwoConsecutiveVLGreaterThan1000(),
            mappings));

    dim.addCohortDefinition(
        "reinitArt",
        EptsReportUtils.map(
            advancedDiseaseAndTBCascadeCohortQueries.getPatientsWhoReinitiatedArt(), mappings));
    return dim;
  }
}
