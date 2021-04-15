package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Arrays;
import java.util.Collections;
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

  private GenericCohortQueries genericCohortQueries;

  private HivMetadata hivMetadata;

  private CommonMetadata commonMetadata;

  private GenderCohortQueries genderCohortQueries;

  private AgeCohortQueries ageCohortQueries;

  private ResumoMensalCohortQueries resumoMensalCohortQueries;

  private CommonCohortQueries commonCohortQueries;

  private TbMetadata tbMetadata;

  private TxPvlsCohortQueries txPvls;

  private QualityImprovement2020CohortQueries qualityImprovement2020CohortQueries;

  private final String MAPPING = "startDate=${startDate},endDate=${endDate},location=${location}";
  private final String MAPPING1 =
      "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}";
  private final String MAPPING2 =
      "startDate=${revisionEndDate-14m},endDate=${revisionEndDate-11m},location=${location}";

  @Autowired
  public IntensiveMonitoringCohortQueries(
      GenericCohortQueries genericCohortQueries,
      HivMetadata hivMetadata,
      CommonMetadata commonMetadata,
      GenderCohortQueries genderCohortQueries,
      ResumoMensalCohortQueries resumoMensalCohortQueries,
      CommonCohortQueries commonCohortQueries,
      TbMetadata tbMetadata,
      TxPvlsCohortQueries txPvls,
      AgeCohortQueries ageCohortQueries,
      QualityImprovement2020CohortQueries qualityImprovement2020CohortQueries) {
    this.genericCohortQueries = genericCohortQueries;
    this.hivMetadata = hivMetadata;
    this.commonMetadata = commonMetadata;
    this.genderCohortQueries = genderCohortQueries;
    this.resumoMensalCohortQueries = resumoMensalCohortQueries;
    this.commonCohortQueries = commonCohortQueries;
    this.tbMetadata = tbMetadata;
    this.txPvls = txPvls;
    this.ageCohortQueries = ageCohortQueries;
    this.qualityImprovement2020CohortQueries = qualityImprovement2020CohortQueries;
  }

  /**
   * Get CAT 7 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period
   * (endDateRevision)
   *
   * @return @{@link org.openmrs.module.reporting.cohort.definition.CohortDefinition}
   */
  public CohortDefinition getCat7MOHIV2021Definition() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addSearch(
        "A",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMOHArtStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "B1",
        EptsReportUtils.map(
            commonCohortQueries.getMohMQPatientsOnCondition(
                false,
                false,
                "once",
                hivMetadata.getAdultoSeguimentoEncounterType(),
                hivMetadata.getActiveTBConcept(),
                Collections.singletonList(hivMetadata.getYesConcept()),
                null,
                null),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "B2",
        EptsReportUtils.map(
            commonCohortQueries.getMohMQPatientsOnCondition(
                false,
                false,
                "once",
                hivMetadata.getAdultoSeguimentoEncounterType(),
                tbMetadata.getHasTbSymptomsConcept(),
                Collections.singletonList(hivMetadata.getYesConcept()),
                null,
                null),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "B3",
        EptsReportUtils.map(
            commonCohortQueries.getMohMQPatientsOnCondition(
                false,
                false,
                "once",
                hivMetadata.getAdultoSeguimentoEncounterType(),
                tbMetadata.getTBTreatmentPlanConcept(),
                Arrays.asList(
                    tbMetadata.getStartDrugsConcept(),
                    hivMetadata.getContinueRegimenConcept(),
                    hivMetadata.getCompletedConcept()),
                null,
                null),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "B4",
        EptsReportUtils.map(
            commonCohortQueries.getMohMQPatientsOnCondition(
                false,
                false,
                "once",
                hivMetadata.getAdultoSeguimentoEncounterType(),
                hivMetadata.getIsoniazidUsageConcept(),
                Collections.singletonList(hivMetadata.getStartDrugs()),
                null,
                null),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    return cd;
  }
}
