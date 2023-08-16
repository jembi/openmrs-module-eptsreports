package org.openmrs.module.eptsreports.reporting.library.dimensions;

import java.util.Date;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.library.cohorts.AdvancedDiseaseAndTBCascadeCohortQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AdvanceDiseaseAndTbCascadeDimension {

  private AdvancedDiseaseAndTBCascadeCohortQueries advancedDiseaseAndTBCascadeCohortQueries;

  @Autowired
  public AdvanceDiseaseAndTbCascadeDimension(
      AdvancedDiseaseAndTBCascadeCohortQueries advancedDiseaseAndTBCascadeCohortQueries) {

    this.advancedDiseaseAndTBCascadeCohortQueries = advancedDiseaseAndTBCascadeCohortQueries;
  }

  public CohortDefinitionDimension getPatientWithPositiveTbLamAndGradeDimension() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.addParameter(new Parameter("startDate", "startDate", Date.class));
    dim.addParameter(new Parameter("endDate", "endDate", Date.class));
    dim.addParameter(new Parameter("location", "location", Location.class));
    dim.setName("Patients having Positive TB LAM and Grade 4+");

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
}
