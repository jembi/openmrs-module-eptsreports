package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Date;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IntensiveMonitoringCohortQueries {

  private HivMetadata hivMetadata;

  private CommonMetadata commonMetadata;

  private CommonCohortQueries commonCohortQueries;

  private TbMetadata tbMetadata;

  private QualityImprovement2020CohortQueries qualityImprovement2020CohortQueries;

  @Autowired
  public IntensiveMonitoringCohortQueries(
      HivMetadata hivMetadata,
      CommonMetadata commonMetadata,
      CommonCohortQueries commonCohortQueries,
      TbMetadata tbMetadata,
      QualityImprovement2020CohortQueries qualityImprovement2020CohortQueries) {
    this.hivMetadata = hivMetadata;
    this.commonMetadata = commonMetadata;
    this.commonCohortQueries = commonCohortQueries;
    this.tbMetadata = tbMetadata;
    this.qualityImprovement2020CohortQueries = qualityImprovement2020CohortQueries;
  }

  /**
   * Get CAT 7 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period Section
   * 7.1 (endDateRevision)
   *
   * @return @{@link org.openmrs.module.reporting.cohort.definition.CohortDefinition}
   */
  public CohortDefinition getCat7DenMOHIV202171Definition(Integer level) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 7.1 to 7.6 Denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MIDEN",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQ7A(level),
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));
    cd.setCompositionString("MIDEN");
    return cd;
  }

  /**
   * Get CAT 13 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period
   * Section 13.2 Denominator (endDateRevision)
   *
   * @return @{@link org.openmrs.module.reporting.cohort.definition.CohortDefinition}
   */
  public CohortDefinition getMI13DEN2(Integer indicator) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.2 Denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MI13DEN2",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQC13P3DEN(indicator),
            "startDate=${revisionEndDate-10m+1d},endDate=${revisionEndDate-9m},location=${location}"));
    cd.setCompositionString("MI13DEN2");
    return cd;
  }

  /**
   * Get CAT 13 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period
   * Section 13.2 Numerator (endDateRevision)
   *
   * @return @{@link org.openmrs.module.reporting.cohort.definition.CohortDefinition}
   */
  public CohortDefinition getMI13NUM2(Integer indicator) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.2 Numerator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MI13NUM2",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQC13P3NUM(indicator),
            "startDate=${revisionEndDate-10m+1d},endDate=${revisionEndDate-9m},location=${location}"));
    cd.setCompositionString("MI13NUM2");
    return cd;
  }

  /**
   * Get CAT 13 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period
   * Section 13.5 Denominator (endDateRevision)
   *
   * @return @{@link org.openmrs.module.reporting.cohort.definition.CohortDefinition}
   */
  public CohortDefinition getMI13DEN5(Integer indicator) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.5 Denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MI13DEN5",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQC13P3DEN(indicator),
            "startDate=${revisionEndDate-10m+1d},endDate=${revisionEndDate-9m},location=${location}"));
    cd.setCompositionString("MI13DEN5");
    return cd;
  }

  /**
   * Get CAT 13 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period
   * Section 13.5 Numerator (endDateRevision)
   *
   * @return @{@link org.openmrs.module.reporting.cohort.definition.CohortDefinition}
   */
  public CohortDefinition getMI13NUM5(Integer indicator) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.5 Numerator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MI13NUM5",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQC13P3NUM(indicator),
            "startDate=${revisionEndDate-10m+1d},endDate=${revisionEndDate-9m},location=${location}"));
    cd.setCompositionString("MI13NUM5");
    return cd;
  }

  /**
   * Get CAT 13 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period
   * Section 13.9 Denominator (endDateRevision)
   *
   * @return @{@link org.openmrs.module.reporting.cohort.definition.CohortDefinition}
   */
  public CohortDefinition getMI13DEN9(Integer indicator) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.9 Denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MI13DEN9",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQC13P3DEN(indicator),
            "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},location=${location}"));
    cd.setCompositionString("MI13DEN9");
    return cd;
  }

  /**
   * Get CAT 13 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period
   * Section 13.9 Numerator (endDateRevision)
   *
   * @return @{@link org.openmrs.module.reporting.cohort.definition.CohortDefinition}
   */
  public CohortDefinition getMI13NUM9(Integer indicator) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.9 Numerator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MI13NUM9",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQC13P3NUM(indicator),
            "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},location=${location}"));
    cd.setCompositionString("MI13NUM9");
    return cd;
  }

  /**
   * Get CAT 13 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period
   * Section 13.10 Denominator (endDateRevision)
   *
   * @return @{@link org.openmrs.module.reporting.cohort.definition.CohortDefinition}
   */
  public CohortDefinition getMI13DEN10(Integer indicator) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.10 Denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MI13DEN10",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQC13P3DEN(indicator),
            "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},location=${location}"));
    cd.setCompositionString("MI13DEN10");
    return cd;
  }

  /**
   * Get CAT 13 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period
   * Section 13.10 Numerator (endDateRevision)
   *
   * @return @{@link org.openmrs.module.reporting.cohort.definition.CohortDefinition}
   */
  public CohortDefinition getMI13NUM10(Integer indicator) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.10 Numerator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MI13NUM10",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQC13P3NUM(indicator),
            "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},location=${location}"));
    cd.setCompositionString("MI13NUM10");
    return cd;
  }

  /**
   * Get CAT 13 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period
   * Section 13.11 Denominator (endDateRevision)
   *
   * @return @{@link org.openmrs.module.reporting.cohort.definition.CohortDefinition}
   */
  public CohortDefinition getMI13DEN11(Integer indicator) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.11 Denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MI13DEN11",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQC13P3DEN(indicator),
            "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},location=${location}"));
    cd.setCompositionString("MI13DEN11");
    return cd;
  }

  /**
   * Get CAT 13 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period
   * Section 13.11 Numerator (endDateRevision)
   *
   * @return @{@link org.openmrs.module.reporting.cohort.definition.CohortDefinition}
   */
  public CohortDefinition getMI13NUM11(Integer indicator) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.11 Numerator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MI13NUM11",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQC13P3NUM(indicator),
            "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},location=${location}"));
    cd.setCompositionString("MI13NUM11");
    return cd;
  }

  /**
   * Get CAT 13 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period
   * Section 13.14 Denominator (endDateRevision)
   *
   * @return @{@link org.openmrs.module.reporting.cohort.definition.CohortDefinition}
   */
  public CohortDefinition getMI13DEN14(Integer indicator) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.14 Denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MI13DEN14",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQC13P3DEN(indicator),
            "startDate=${revisionEndDate-10m+1d},endDate=${revisionEndDate-9m},location=${location}"));
    cd.setCompositionString("MI13DEN14");
    return cd;
  }

  /**
   * Get CAT 13 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period
   * Section 13.14 Numerator (endDateRevision)
   *
   * @return @{@link org.openmrs.module.reporting.cohort.definition.CohortDefinition}
   */
  public CohortDefinition getMI13NUM14(Integer indicator) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.14 Numerator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MI13NUM14",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQC13P3NUM(indicator),
            "startDate=${revisionEndDate-10m+1d},endDate=${revisionEndDate-9m},location=${location}"));
    cd.setCompositionString("MI13NUM14");
    return cd;
  }
}
