package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.eptsreports.reporting.library.queries.QualityImprovement2020Queries;
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
  public CohortDefinition getCat7MOHIV202171Definition(String type) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("7.1 Numerator and denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "A",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMOHArtStartDate(),
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));
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
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));
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
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));
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
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));
    cd.addSearch(
        "B41",
        EptsReportUtils.map(
            this.qualityImprovement2020CohortQueries.getB4And1(),
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));
    cd.addSearch(
        "B42",
        EptsReportUtils.map(
            this.qualityImprovement2020CohortQueries.getB4And2(),
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));
    cd.addSearch(
        "C",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPregnantORBreastfeeding(
                commonMetadata.getPregnantConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));
    cd.addSearch(
        "D",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPregnantORBreastfeeding(
                commonMetadata.getBreastfeeding().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));

    cd.addSearch(
        "E",
        EptsReportUtils.map(
            QualityImprovement2020Queries.getTransferredInPatients(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
                hivMetadata.getPatientFoundYesConcept().getConceptId(),
                hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
                hivMetadata.getArtStatus().getConceptId()),
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));

    cd.addSearch(
        "F",
        EptsReportUtils.map(
            commonCohortQueries.getTranferredOutPatients(),
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "G",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getPatientsWithProphylaxyDuringRevisionPeriod(),
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));

    cd.addSearch(
        "H",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getPatientsWithTBDiagActive(),
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));

    cd.addSearch(
        "I",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getPatientsWithTBSymtoms(),
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));

    cd.addSearch(
        "J",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getPatientsWithTBTreatment(),
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));
    if (type.equals("DEN")) {
      cd.setCompositionString(
          "A AND NOT B1 AND NOT B2 AND NOT B3 AND NOT C AND NOT D AND NOT E AND NOT F");

    } else if (type.equals("NUM")) {
      cd.setCompositionString(
          "A AND NOT B1 AND NOT B2 AND NOT B3 AND (B41 OR B42) AND NOT C AND NOT D AND NOT E AND NOT F");
    }
    return cd;
  }

  /**
   * Get CAT 7 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period Section
   * 7.2 (endDateRevision)
   *
   * @return @{@link org.openmrs.module.reporting.cohort.definition.CohortDefinition}
   */
  public CohortDefinition getCat7MOHIV202172Definition(String type) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(" MI 7.2 Numerator and denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "A",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMOHArtStartDate(),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));
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
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));
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
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));
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
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));
    cd.addSearch(
        "B41",
        EptsReportUtils.map(
            this.qualityImprovement2020CohortQueries.getB4And1(),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));
    cd.addSearch(
        "B42",
        EptsReportUtils.map(
            this.qualityImprovement2020CohortQueries.getB4And2(),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));
    cd.addSearch(
        "C",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPregnantORBreastfeeding(
                commonMetadata.getPregnantConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));
    cd.addSearch(
        "D",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPregnantORBreastfeeding(
                commonMetadata.getBreastfeeding().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));

    cd.addSearch(
        "E",
        EptsReportUtils.map(
            QualityImprovement2020Queries.getTransferredInPatients(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
                hivMetadata.getPatientFoundYesConcept().getConceptId(),
                hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
                hivMetadata.getArtStatus().getConceptId()),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));

    cd.addSearch(
        "F",
        EptsReportUtils.map(
            commonCohortQueries.getTranferredOutPatients(),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "G",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getGNew(),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));

    cd.addSearch(
        "H",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getPatientsWithTBDiagActive(),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));

    cd.addSearch(
        "I",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getPatientsWithTBSymtoms(),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));

    cd.addSearch(
        "J",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getPatientsWithTBTreatment(),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));
    if (type.equals("DEN")) {
      cd.setCompositionString(
          "A AND NOT B1 AND NOT B2 AND NOT B3 AND (B41 OR B42) AND NOT C AND NOT D AND NOT E AND NOT F AND NOT H AND NOT I AND NOT J");

    } else if (type.equals("NUM")) {
      cd.setCompositionString(
          "A AND NOT B1 AND NOT B2 AND NOT B3 AND (B41 OR B42) AND NOT C AND NOT D AND NOT E AND NOT F AND G AND NOT H AND NOT I AND NOT J");
    }
    return cd;
  }

  /**
   * Get CAT 7 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period Section
   * 7.3 (endDateRevision)
   *
   * @return @{@link org.openmrs.module.reporting.cohort.definition.CohortDefinition}
   */
  public CohortDefinition getCat7MOHIV202173Definition(String type) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 7.3 Numerator and denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "A",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMOHArtStartDate(),
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));
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
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));
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
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));
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
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));
    cd.addSearch(
        "B41",
        EptsReportUtils.map(
            this.qualityImprovement2020CohortQueries.getB4And1(),
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));
    cd.addSearch(
        "B42",
        EptsReportUtils.map(
            this.qualityImprovement2020CohortQueries.getB4And2(),
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));
    cd.addSearch(
        "C",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPregnantORBreastfeeding(
                commonMetadata.getPregnantConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));
    cd.addSearch(
        "D",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPregnantORBreastfeeding(
                commonMetadata.getBreastfeeding().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));

    cd.addSearch(
        "E",
        EptsReportUtils.map(
            QualityImprovement2020Queries.getTransferredInPatients(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
                hivMetadata.getPatientFoundYesConcept().getConceptId(),
                hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
                hivMetadata.getArtStatus().getConceptId()),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));

    cd.addSearch(
        "F",
        EptsReportUtils.map(
            commonCohortQueries.getTranferredOutPatients(),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "G",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getPatientsWithProphylaxyDuringRevisionPeriod(),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));

    cd.addSearch(
        "H",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getPatientsWithTBDiagActive(),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));

    cd.addSearch(
        "I",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getPatientsWithTBSymtoms(),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));

    cd.addSearch(
        "J",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getPatientsWithTBTreatment(),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));
    if (type.equals("DEN")) {
      cd.setCompositionString(
          "A AND NOT B1 AND NOT B2 AND NOT B3 AND NOT C AND NOT D AND NOT E AND NOT F");

    } else if (type.equals("NUM")) {
      cd.setCompositionString(
          "A AND NOT B1 AND NOT B2 AND NOT B3 AND (B41 OR B42) AND NOT C AND NOT D AND NOT E AND NOT F");
    }
    return cd;
  }

  /**
   * Get CAT 7 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period Section
   * 7.4 (endDateRevision)
   *
   * @return @{@link org.openmrs.module.reporting.cohort.definition.CohortDefinition}
   */
  public CohortDefinition getCat7MOHIV202174Definition(String type) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 7.4 Numerator and denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "A",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMOHArtStartDate(),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));
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
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));
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
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));
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
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));
    cd.addSearch(
        "B41",
        EptsReportUtils.map(
            this.qualityImprovement2020CohortQueries.getB4And1(),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));
    cd.addSearch(
        "B42",
        EptsReportUtils.map(
            this.qualityImprovement2020CohortQueries.getB4And2(),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));
    cd.addSearch(
        "C",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPregnantORBreastfeeding(
                commonMetadata.getPregnantConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));
    cd.addSearch(
        "D",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPregnantORBreastfeeding(
                commonMetadata.getBreastfeeding().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));

    cd.addSearch(
        "E",
        EptsReportUtils.map(
            QualityImprovement2020Queries.getTransferredInPatients(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
                hivMetadata.getPatientFoundYesConcept().getConceptId(),
                hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
                hivMetadata.getArtStatus().getConceptId()),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));

    cd.addSearch(
        "F",
        EptsReportUtils.map(
            commonCohortQueries.getTranferredOutPatients(),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "G",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getGNew(),
            "startDate=${revisionEndDate-8m+1d},endDate=${evisionEndDate-7m},location=${location}"));

    cd.addSearch(
        "H",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getPatientsWithTBDiagActive(),
            "startDate=${revisionEndDate-8m+1d},endDate=${evisionEndDate-7m},location=${location}"));

    cd.addSearch(
        "I",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getPatientsWithTBSymtoms(),
            "startDate=${revisionEndDate-8m+1d},endDate=${evisionEndDate-7m},location=${location}"));

    cd.addSearch(
        "J",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getPatientsWithTBTreatment(),
            "startDate=${revisionEndDate-8m+1d},endDate=${evisionEndDate-7m},location=${location}"));
    if (type.equals("DEN")) {
      cd.setCompositionString(
          "A AND NOT B1 AND NOT B2 AND NOT B3 AND  (B41 OR  B42) AND NOT C AND NOT D AND NOT E AND NOT F AND NOT H AND NOT I AND NOT J");

    } else if (type.equals("NUM")) {
      cd.setCompositionString(
          "A AND NOT B1 AND NOT B2 AND NOT B3 AND  (B41 OR B42) AND NOT C AND NOT D AND NOT E AND NOT F AND G AND NOT H AND NOT I AND NOT J ");
    }
    return cd;
  }

  /**
   * Get CAT 7 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period Section
   * 7.5 (endDateRevision)
   *
   * @return @{@link org.openmrs.module.reporting.cohort.definition.CohortDefinition}
   */
  public CohortDefinition getCat7MOHIV202175Definition(String type) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 7.5 Numerator and denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "A",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMOHArtStartDate(),
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));
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
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));
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
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));
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
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));
    cd.addSearch(
        "B41",
        EptsReportUtils.map(
            this.qualityImprovement2020CohortQueries.getB4And1(),
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));
    cd.addSearch(
        "B42",
        EptsReportUtils.map(
            this.qualityImprovement2020CohortQueries.getB4And2(),
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));
    cd.addSearch(
        "C",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPregnantORBreastfeeding(
                commonMetadata.getPregnantConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));
    cd.addSearch(
        "D",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPregnantORBreastfeeding(
                commonMetadata.getBreastfeeding().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));

    cd.addSearch(
        "E",
        EptsReportUtils.map(
            QualityImprovement2020Queries.getTransferredInPatients(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
                hivMetadata.getPatientFoundYesConcept().getConceptId(),
                hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
                hivMetadata.getArtStatus().getConceptId()),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));

    cd.addSearch(
        "F",
        EptsReportUtils.map(
            commonCohortQueries.getTranferredOutPatients(),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},revisionEndDate=${revisionEndDate},location=${location}"));

    if (type.equals("DEN")) {
      cd.setCompositionString(
          "A AND NOT B1 AND NOT B2 AND NOT B3 AND C AND NOT D AND NOT E AND NOT F");

    } else if (type.equals("NUM")) {
      cd.setCompositionString(
          "A AND NOT B1 AND NOT B2 AND NOT B3 AND (B41 OR B42) AND C AND NOT D AND NOT E AND NOT F");
    }
    return cd;
  }

  /**
   * Get CAT 7 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period Section
   * 7.6 (endDateRevision)
   *
   * @return @{@link org.openmrs.module.reporting.cohort.definition.CohortDefinition}
   */
  public CohortDefinition getCat7MOHIV202176Definition(String type) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 7.6 Numerator and denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "A",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMOHArtStartDate(),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));
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
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));
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
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));
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
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));
    cd.addSearch(
        "B41",
        EptsReportUtils.map(
            this.qualityImprovement2020CohortQueries.getB4And1(),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));
    cd.addSearch(
        "B42",
        EptsReportUtils.map(
            this.qualityImprovement2020CohortQueries.getB4And2(),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));
    cd.addSearch(
        "C",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPregnantORBreastfeeding(
                commonMetadata.getPregnantConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));
    cd.addSearch(
        "D",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPregnantORBreastfeeding(
                commonMetadata.getBreastfeeding().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));

    cd.addSearch(
        "E",
        EptsReportUtils.map(
            QualityImprovement2020Queries.getTransferredInPatients(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
                hivMetadata.getPatientFoundYesConcept().getConceptId(),
                hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
                hivMetadata.getArtStatus().getConceptId()),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));

    cd.addSearch(
        "F",
        EptsReportUtils.map(
            commonCohortQueries.getTranferredOutPatients(),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "G",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getGNew(),
            "startDate=${revisionEndDate-8m+1d},endDate=${evisionEndDate-7m},location=${location}"));

    cd.addSearch(
        "H",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getPatientsWithTBDiagActive(),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));

    cd.addSearch(
        "I",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getPatientsWithTBSymtoms(),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));

    cd.addSearch(
        "J",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getPatientsWithTBTreatment(),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}"));
    if (type.equals("DEN")) {
      cd.setCompositionString(
          "A AND NOT B1 AND NOT B2 AND NOT B3 AND  (B41 OR B42) AND  C AND NOT D AND NOT E AND NOT F AND NOT H AND NOT I AND NOT J");

    } else if (type.equals("NUM")) {
      cd.setCompositionString(
          "A AND NOT B1 AND NOT B2 AND NOT B3 AND (B41 OR B42) AND C AND NOT D AND NOT E AND NOT F AND G AND NOT H AND NOT I AND NOT J");
    }
    return cd;
  }
}
