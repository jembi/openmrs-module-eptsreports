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

  private final String MAPPING =
      "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}";

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
   * Get CAT 7.1, 7.3, 7.5 Monitoria Intensiva MQHIV 2021 for the selected location and reporting
   * period Section 7.1 (endDateRevision)
   *
   * @return @{@link org.openmrs.module.reporting.cohort.definition.CohortDefinition}
   */
  public CohortDefinition getCat7DenMOHIV202171Definition(Integer level, String type) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 7.1, 7.3, 7.5 numerator and denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MI71DEN",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQ7A(level),
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));
    cd.addSearch(
        "MI71NUM",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQ7B(level),
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));
    if ("DEN".equals(type)) {
      cd.setCompositionString("MI71DEN");
    } else if ("NUM".equals(type)) {
      cd.setCompositionString("MI71NUM");
    }
    return cd;
  }

  /**
   * Get CAT 7.2, 7.4, 7.6 Monitoria Intensiva MQHIV 2021 for the selected location and reporting
   * period Section 7.1 (endDateRevision)
   *
   * @return @{@link org.openmrs.module.reporting.cohort.definition.CohortDefinition}
   */
  public CohortDefinition getCat7DenMOHIV202172Definition(Integer level, String type) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 7.2, 7.4, 7.6 numerator and denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MI72DEN",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQ7A(level),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));
    cd.addSearch(
        "MI72NUM",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQ7B(level),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));
    if ("DEN".equals(type)) {
      cd.setCompositionString("MI72DEN");
    } else if ("NUM".equals(type)) {
      cd.setCompositionString("MI72NUM");
    }
    return cd;
  }

  /**
   * Get CAT 13.15, 13.16 and 13.17 Monitoria Intensiva MQHIV 2021 for the selected location and
   * reporting period Section 7.1 (endDateRevision)
   *
   * @return @{@link org.openmrs.module.reporting.cohort.definition.CohortDefinition}
   */
  public CohortDefinition getMICat13Part2(Integer level, String type) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.15, 13.16 and 13.17 numerator and denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    // DENOMINATOR
    if (level == 15) {
      cd.addSearch(
          "MI13DEN15",
          EptsReportUtils.map(
              qualityImprovement2020CohortQueries.getgetMQC13P2DenMGInIncluisionPeriod(), MAPPING));
    } else if (level == 16) {
      cd.addSearch(
          "MI13DEN16",
          EptsReportUtils.map(
              qualityImprovement2020CohortQueries.getgetMQC13P2DenMGInIncluisionPeriod33Month(),
              MAPPING));
    } else if (level == 17) {
      cd.addSearch(
          "MI13DEN17",
          EptsReportUtils.map(
              qualityImprovement2020CohortQueries.getMQC13P2DenMGInIncluisionPeriod33Days(),
              MAPPING));
    }

    // NUMERATOR
    if (level == 15) {
      cd.addSearch(
          "MI13NUM15",
          EptsReportUtils.map(qualityImprovement2020CohortQueries.getMQC13P2Num1(), MAPPING));
    } else if (level == 16) {
      cd.addSearch(
          "MI13NUM16",
          EptsReportUtils.map(qualityImprovement2020CohortQueries.getMQC13P2Num2(), MAPPING));
    } else if (level == 17) {
      cd.addSearch(
          "MI13NUM17",
          EptsReportUtils.map(qualityImprovement2020CohortQueries.getMQC13P2Num3(), MAPPING));
    }

    if ("DEN15".equals(type)) {
      cd.setCompositionString("MI13DEN15");
    } else if ("DEN16".equals(type)) {
      cd.setCompositionString("MI13DEN16");
    } else if ("DEN17".equals(type)) {
      cd.setCompositionString("MI13DEN17");
    } else if ("NUM15".equals(type)) {
      cd.setCompositionString("MI13NUM15");
    } else if ("NUM16".equals(type)) {
      cd.setCompositionString("MI13NUM16");
    } else if ("NUM17".equals(type)) {
      cd.setCompositionString("MI13NUM17");
    }
    return cd;
  }
}
