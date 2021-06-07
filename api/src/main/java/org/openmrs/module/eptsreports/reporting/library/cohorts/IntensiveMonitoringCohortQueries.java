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
   * Get CAT 13 Part-1 DENOMINADOR Monitoria Intensiva MQHIV 2021 for the selected location and
   * reporting period Section (endDateRevision)
   *
   * @return @{@link org.openmrs.module.reporting.cohort.definition.CohortDefinition}
   */
  public CohortDefinition getMICat13Part1DEN(Integer level) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13 Part-1 Denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MI13DEN",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQ13(true, level),
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));
    cd.setCompositionString("MI13DEN");
    return cd;
  }

  /**
   * Get CAT 13 Part-1 NUMERADOR Monitoria Intensiva MQHIV 2021 for the selected location and
   * reporting period Section (endDateRevision)
   *
   * @return @{@link org.openmrs.module.reporting.cohort.definition.CohortDefinition}
   */
  public CohortDefinition getMICat13Part1NUM(Integer level) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13 Part-1 Numerador");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MI13NUM",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQ13(false, level),
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));
    cd.setCompositionString("MI13NUM");
    return cd;
  }
}
