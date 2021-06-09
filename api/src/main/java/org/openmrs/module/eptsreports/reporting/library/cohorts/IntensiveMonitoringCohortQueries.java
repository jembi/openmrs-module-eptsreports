package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
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
   * MEPTS-862_MI_REPORT_CAT13_P2 Get CAT 13.15, 13.16 and 13.17 P2 for Numerator and Denominator
   * Monitoria Intensiva MQHIV 2021 for the selected location and reporting period Section
   * (endDateRevision)
   *
   * @return @{@link org.openmrs.module.reporting.cohort.definition.CohortDefinition}
   */
  public CohortDefinition getMICat13Part2(Integer level, String type) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.15, 13.16 and 13.17 Numerator and Denominator");
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

  /* Get CAT 13.1, 13.4, 13.6, 13.7, 13.8, 13.13 Monitoria Intensiva MQHIV 2021 for the selected
   * location and reporting period Section (endDateRevision)
   *
   * @return @{@link org.openmrs.module.reporting.cohort.definition.CohortDefinition}
   */
  public CohortDefinition getCat13Den(Integer level, String type) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.1, 13.4, 13.6, 13.7, 13.8, 13.13 numerator and denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    String mapp =
        "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}";
    cd.addSearch(
        "MI13DEN",
        EptsReportUtils.map(qualityImprovement2020CohortQueries.getMQ13(true, level), mapp));
    cd.addSearch(
        "MI13NUM",
        EptsReportUtils.map(qualityImprovement2020CohortQueries.getMQ13(false, level), mapp));
    if ("DEN".equals(type)) {
      cd.setCompositionString("MI13DEN");
    } else if ("NUM".equals(type)) {
      cd.setCompositionString("MI13NUM");
    }
    return cd;
  }

  /**
   * Get CAT 12.1, 12.2, 12.5, 12.6, 12.9, 12.10 Monitoria Intensiva MQHIV 2021 for the selected
   * location and reporting period (endDateRevision)
   *
   * @return @{@link org.openmrs.module.reporting.cohort.definition.CohortDefinition}
   */
  public CohortDefinition getCat12P1DenNum(Integer level, String type) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 12.1, 12.2, 12.5, 12.6, 12.9, 12.10 numerator and denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    String MAPPING = "";
    if (level == 1 || level == 5 || level == 9)
      MAPPING =
          "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},location=${location}";
    else
      MAPPING =
          "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},location=${location}";
    cd.addSearch(
        "MI12P1DEN",
        EptsReportUtils.map(qualityImprovement2020CohortQueries.getMQ12DEN(level), MAPPING));
    cd.addSearch(
        "MI12P1NUM",
        EptsReportUtils.map(qualityImprovement2020CohortQueries.getMQ12NUM(level), MAPPING));
    if ("DEN".equals(type)) {
      cd.setCompositionString("MI12P1DEN");
    } else if ("NUM".equals(type)) {
      cd.setCompositionString("MI12P1NUM");
    }
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

  /**
   * A - Select all patients with Last Clinical Consultation (encounter type 6, encounter_datetime)
   * occurred during the inclusion period (encounter_datetime>= startDateInclusion and <=
   * endDateInclusion
   */
  public CohortDefinition getMI15A() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patients From Ficha Clinica");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());

    String query =
        "SELECT p.patient_id "
            + " FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + " WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_type = ${6} "
            + "       AND  e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "       GROUP BY p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * B2 - Select all patients with the earliest “Data de Início TARV” (concept_id 1190, not null,
   * value_datetime) recorded in Ficha Resumo (encounter type 53, obs_datetime) and “Last
   * Consultation Date” (encounter_datetime from A) minus “Data de Início TARV” (concept id 1190
   * value_datetime) is greater than (>) 21 months
   */
  public CohortDefinition getMI15B2() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("B2 Patients From Ficha Clinica");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());

    String query =
        "SELECT tabela.patient_id "
            + " FROM "
            + " (SELECT p.patient_id, min(o.value_datetime) value_datetime "
            + " FROM patient p INNER JOIN encounter e on e.patient_id=p.patient_id "
            + " INNER JOIN obs o ON o.encounter_id=e.encounter_id "
            + " WHERE o.voided=0 AND e.voided=0 AND p.voided=0 AND "
            + " o.concept_id=${1190} AND o.value_datetime is not NULL AND "
            + " e.encounter_type=${53} AND "
            + " e.location_id= :location "
            + " GROUP by p.patient_id) tabela "
            + "  WHERE timestampdiff(month,tabela.value_datetime,( "
            + " SELECT MAX(e.encounter_datetime) "
            + " FROM   patient pp INNER JOIN encounter e "
            + " ON e.patient_id = pp.patient_id "
            + " WHERE  pp.voided = 0 AND e.voided = 0 "
            + " AND tabela.patient_id=pp.patient_id "
            + " AND e.location_id = :location "
            + " AND e.encounter_type = ${6} "
            + " AND  e.encounter_datetime BETWEEN :startDate AND :endDate))>21 ";
    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * D - All female patients registered as “Breastfeeding” (concept_id 6332, value_coded equal to
   * concept_id 1065) in Ficha Clínica (encounter type 6, encounter_datetime) occurred during the
   * following period (encounter_datetime >= startDate and <= endDate)
   *
   * @return
   */
  public CohortDefinition getMI15D() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patients From Ficha Clinica");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6332", hivMetadata.getBreastfeeding().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());

    String query =
        "SELECT p.patient_id FROM patient p  "
            + " INNER JOIN encounter e ON e.patient_id=p.patient_id  "
            + " INNER JOIN obs o ON o.encounter_id=e.encounter_id "
            + " WHERE p.voided = 0 AND e.voided=0 AND e.location_id=:location "
            + " AND e.encounter_type= ${6} AND e.encounter_datetime BETWEEN :startDate AND :endDate  "
            + " AND o.concept_id = ${6332} AND o.value_coded= ${1065} "
            + " GROUP BY p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   *
   *
   * <ul>
   *   <li>B1 – Select all patients with the earliest “Data de Início TARV” (concept_id 1190, not
   *       Tnull, value_datetime) recorded in Ficha Resumo (encounter type 53) and “Last
   *       Consultation Date” (encounter_datetime from A) minus “ Data de Início TARV” (concept id
   *       1190 value_datetime) is greater than (>) 3 months.
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMI15B1() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patients From Ficha Clinica");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());

    String query =
        "SELECT tabela.patient_id "
            + " FROM   (SELECT p.patient_id,"
            + "               Min(o.value_datetime) value_datetime "
            + "        FROM   patient p "
            + "               INNER JOIN encounter e "
            + "                       ON e.patient_id = p.patient_id "
            + "               INNER JOIN obs o "
            + "                       ON o.encounter_id = e.encounter_id "
            + "        WHERE  o.voided = 0 "
            + "               AND e.voided = 0 "
            + "               AND p.voided = 0 "
            + "               AND o.concept_id = ${1190} "
            + "               AND o.value_datetime IS NOT NULL "
            + "               AND e.encounter_type = ${53} "
            + "               AND e.location_id = :location "
            + "        GROUP  BY p.patient_id) tabela "
            + " WHERE  Timestampdiff(month, tabela.value_datetime, "
            + "       (SELECT Max(e.encounter_datetime) "
            + "        FROM   patient pp "
            + "       INNER JOIN "
            + "       encounter e "
            + "               ON e.patient_id = pp.patient_id "
            + " WHERE  pp.voided = 0 "
            + "             AND e.voided = 0 "
            + "             AND tabela.patient_id = pp.patient_id "
            + "             AND e.location_id = :location "
            + "             AND e.encounter_type = ${6}"
            + "             AND e.encounter_datetime BETWEEN :startDate AND :endDate)) > 3 ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   *
   *
   * <ul>
   *   <li>G - Select all patients with the last (Viral Load Result (concept id 856) )and the result
   *       is >= 1000 (value_numeric) registered on Ficha Clinica (encounter type 6) before “Last
   *       Consultation Date” (encounter_datetime from A).
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMI15G() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patients From Ficha Clinica");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String query =
        " SELECT p.patient_id "
            + " FROM   patient p "
            + "       INNER JOIN encounter ee "
            + "               ON ee.patient_id = p.patient_id "
            + "       INNER JOIN obs oo"
            + "               ON oo.encounter_id = ee.encounter_id "
            + " WHERE  p.voided = 0 "
            + "       AND ee.voided = 0 "
            + "       AND oo.voided = 0 "
            + "       AND ee.location_id = :location "
            + "       AND ee.encounter_type = ${6} "
            + "       AND oo.concept_id = ${856} "
            + "       AND oo.value_numeric >= 1000 "
            + "       AND ee.encounter_datetime <= (SELECT "
            + "           Max(e.encounter_datetime) AS last_consultation_date "
            + "                                     FROM   encounter e "
            + "                                     WHERE  e.voided = 0 "
            + "                                            AND e.location_id = :location "
            + "                                            AND e.encounter_type = ${6} "
            + "                                            AND e.patient_id = p.patient_id "
            + "                                            AND e.encounter_datetime BETWEEN "
            + "                                                :startDate AND :endDate "
            + "                                     LIMIT  1)  ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * C- All female patients registered as “Pregnant” (concept_id 1982, value_coded equal to
   * concept_id 1065) in Ficha Clínica (encounter type 6, encounter_datetime) occurred during the
   * following period (encounter_datetime >= startDate and <= endDate)
   *
   * @return
   */
  public CohortDefinition getMI15C() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patients From Ficha Clinica");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());

    String query =
        "SELECT p.patient_id FROM patient p  "
            + " INNER JOIN encounter e ON e.patient_id=p.patient_id  "
            + " INNER JOIN obs o ON o.encounter_id=e.encounter_id "
            + " WHERE p.voided = 0 AND e.voided=0 AND e.location_id=:location "
            + " AND e.encounter_type= ${6} AND e.encounter_datetime BETWEEN :startDate AND :endDate  "
            + " AND o.concept_id = ${1982} AND o.value_coded= ${1065} "
            + " GROUP BY p.patient_id ";
    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   *
   *
   * <ul>
   *   <li>H - Select all patients with Viral Load result (concept id 856, value_numeric) >= 1000 on
   *       registered in Ficha Clinica (encounter type 6) on “Last Consultation” (encounter_datetime
   *       from A)
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMI15H() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patients From Ficha Clinica");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String query =
        " SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter ee "
            + "               ON ee.patient_id = p.patient_id "
            + "       INNER JOIN obs oo "
            + "               ON oo.encounter_id = ee.encounter_id "
            + " WHERE  p.voided = 0 "
            + "       AND ee.voided = 0 "
            + "       AND oo.voided = 0 "
            + "       AND ee.location_id = :location "
            + "       AND ee.encounter_type = ${6} "
            + "       AND oo.concept_id = ${856} "
            + "       AND oo.value_numeric >= 1000 "
            + "       AND ee.encounter_datetime = (SELECT "
            + "           Max(e.encounter_datetime) AS last_consultation_date "
            + "                                    FROM   patient p "
            + "                                           INNER JOIN encounter e "
            + "                                                   ON e.patient_id = "
            + "                                                      p.patient_id "
            + "                                    WHERE  p.voided = 0 "
            + "                                           AND e.voided = 0 "
            + "                                           AND e.location_id = :location "
            + "                                           AND e.encounter_type = ${6} "
            + "                                           AND e.encounter_datetime BETWEEN "
            + "                                               :startDate AND :endDate "
            + "                                    LIMIT  1)";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }
  /**
   * J - Select all patients with at least one of the following models registered in Ficha Clinica
   * (encounter type 6, encounter_datetime) before “ Last Consultation Date” (encounter_datetime
   * from A): Last record of GAAC (concept id 23724) and the response is “Iniciar” (value_coded,
   * concept id 1256) or “Continua” (value_coded, concept id 1257) Last record of DT (concept id
   * 23730) and the response is “Iniciar” (value_coded, concept id 1256) or “Continua” (value_coded,
   * concept id 1257) Last record of DS (concept id 23888) and the response is “Iniciar”
   * (value_coded, concept id 1256) or “Continua” (value_coded, concept id 1257) Last record of FR
   * (concept id 23729) and the response is “ Iniciar” (value_coded, concept id 1256) or “Continua”
   * (value_coded, concept id 1257) Last record of DC (concept id 23731) and the response is
   * “Iniciar” (value_coded, concept id 1256) or “Continua” (value_coded, concept id 1257)
   */
  public CohortDefinition getMI15J() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patients From Ficha Clinica");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1257", hivMetadata.getContinueRegimenConcept().getConceptId());
    map.put("23724", hivMetadata.getGaac().getConceptId());
    map.put("23730", hivMetadata.getQuarterlyDispensation().getConceptId());
    map.put("23888", hivMetadata.getSemiannualDispensation().getConceptId());
    map.put("23729", hivMetadata.getRapidFlow().getConceptId());
    map.put("23731", hivMetadata.getCommunityDispensation().getConceptId());

    String query =
        " SELECT p.patient_id  "
            + "FROM   patient p "
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                        WHERE  p.voided = 0 "
            + "                        AND o.voided = 0 "
            + "                        AND e.voided = 0 "
            + "                        AND e.location_id = :location "
            + "                        AND e.encounter_type = ${6} "
            + "                        AND ((o.concept_id = ${23724} AND o.value_coded = ${1257}) "
            + "                                    OR ( EXISTS( SELECT o.person_id FROM obs o  "
            + "                                    WHERE o.encounter_id = e.encounter_id AND o.concept_id = ${23730} AND o.value_coded = ${1257}) "
            + "                                    OR EXISTS( SELECT o.person_id FROM obs o  "
            + "                                    WHERE o.encounter_id = e.encounter_id AND o.concept_id = ${23888} AND o.value_coded = ${1257}) "
            + "                                    OR EXISTS (SELECT  o.person_id FROM obs o  "
            + "                                    WHERE o.encounter_id = e.encounter_id AND o.concept_id = ${23729} AND o.value_coded =${1257}) "
            + "                                    OR EXISTS (SELECT o.person_id FROM obs o  "
            + "                                    WHERE o.encounter_id = e.encounter_id AND o.concept_id = ${23731} AND o.value_coded =${1257} )) "
            + "                        ) "
            + "                        AND e.encounter_datetime < ( SELECT MAX(e.encounter_datetime) as last_consult "
            + "                        FROM   patient p "
            + "                            INNER JOIN encounter e "
            + "                                    ON e.patient_id = p.patient_id "
            + "                        WHERE  p.voided = 0 "
            + "                            AND e.voided = 0 "
            + "                            AND e.location_id = :location "
            + "                            AND e.encounter_type = ${6} "
            + "                            AND  e.encounter_datetime BETWEEN :startDate AND :endDate) ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }
  /**
   * F - Select all patients with the last CD4 result (concept id 1695) and the result is <= 200
   * (value_numeric) registered on Ficha Clinica (encounter type 6) before “Last Consultation Date”
   * (encounter_datetime from A).
   */
  public CohortDefinition getMI15F() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patients From Ficha Clinica");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());

    String query =
        " SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter ee "
            + "               ON ee.patient_id = p.patient_id "
            + "       INNER JOIN obs oo "
            + "               ON oo.encounter_id = ee.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND ee.voided = 0 "
            + "       AND oo.voided = 0 "
            + "       AND ee.location_id = :location "
            + "       AND ee.encounter_type = ${6} "
            + "       AND oo.concept_id = ${1695} "
            + "       AND oo.value_numeric <= 200 "
            + "       AND ee.encounter_datetime <= (SELECT "
            + "           Max(e.encounter_datetime) AS last_consultation_date "
            + "                                     FROM   encounter e "
            + "                                     WHERE  e.voided = 0 "
            + "                                            AND e.location_id = :location "
            + "                                            AND e.encounter_type = ${6} "
            + "                                            AND e.patient_id = p.patient_id "
            + "                                            AND e.encounter_datetime BETWEEN "
            + "                                                :startDate AND :endDate LIMIT  1) ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }
  /**
   * K - Select all patients with at least one of the following models registered in Ficha Clinica
   * (encounter type 6, encounter_datetime) on “ Last Consultation Date” (encounter_datetime from
   * A): GAAC (concept id 23724) = “Iniciar” (value_coded, concept id 1256) DT (concept id 23730) =
   * “Iniciar” (value_coded, concept id 1256) DS (concept id 23888) = “Iniciar” (value_coded,
   * concept id 1256) FR (concept id 23729) = “Iniciar” (value_coded, concept id 1256) DC (concept
   * id 23731) = “Iniciar” (value_coded, concept id 1256)
   */
  public CohortDefinition getMI15k() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patients From Ficha Clinica");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("23724", hivMetadata.getGaac().getConceptId());
    map.put("23730", hivMetadata.getQuarterlyDispensation().getConceptId());
    map.put("23888", hivMetadata.getSemiannualDispensation().getConceptId());
    map.put("23729", hivMetadata.getRapidFlow().getConceptId());
    map.put("23731", hivMetadata.getCommunityDispensation().getConceptId());

    String query =
        " SELECT p.patient_id  "
            + "FROM   patient p "
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                        WHERE  p.voided = 0 "
            + "                        AND o.voided = 0 "
            + "                        AND e.voided = 0 "
            + "                        AND e.location_id = :location "
            + "                        AND e.encounter_type = ${6} "
            + "                        AND ((o.concept_id = ${23724} AND o.value_coded = ${1256}) "
            + "                                    OR ( EXISTS( SELECT o.person_id FROM obs o  "
            + "                                    WHERE o.encounter_id = e.encounter_id AND o.concept_id = ${23730} AND o.value_coded = ${1256}) "
            + "                                    OR EXISTS( SELECT o.person_id FROM obs o  "
            + "                                    WHERE o.encounter_id = e.encounter_id AND o.concept_id = ${23888} AND o.value_coded = ${1256}) "
            + "                                    OR EXISTS (SELECT  o.person_id FROM obs o  "
            + "                                    WHERE o.encounter_id = e.encounter_id AND o.concept_id = ${23729} AND o.value_coded =${1256}) "
            + "                                    OR EXISTS (SELECT o.person_id FROM obs o  "
            + "                                    WHERE o.encounter_id = e.encounter_id AND o.concept_id = ${23731} AND o.value_coded =${1256} )) "
            + "                        ) "
            + "                        AND e.encounter_datetime < ( SELECT MAX(e.encounter_datetime) as last_consult "
            + "                        FROM   patient p "
            + "                            INNER JOIN encounter e "
            + "                                    ON e.patient_id = p.patient_id "
            + "                        WHERE  p.voided = 0 "
            + "                            AND e.voided = 0 "
            + "                            AND e.location_id = :location "
            + "                            AND e.encounter_type = ${6} "
            + "                            AND  e.encounter_datetime BETWEEN :startDate AND :endDate) ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }
  /**
   * L - Select all patients with at least one of the following models registered in Ficha Clinica
   * (encounter type 6, encounter_datetime) on “ Last Consultation Date” (encounter_datetime from
   * A): GAAC (concept id 23724) = “Fim” (value_coded, concept id 1267) DT (concept id 23730) =
   * “Fim” (value_coded, concept id 1267) DS (concept id 23888) = “Fim” (value_coded, concept id
   * 1267) FR (concept id 23729) = “Fim” (value_coded, concept id 1267) DC (concept id 23731) =
   * “Fim” (value_coded, concept id 1267)
   */
  public CohortDefinition getMI15L() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patients From Ficha Clinica");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1267", hivMetadata.getCompletedConcept().getConceptId());
    map.put("23724", hivMetadata.getGaac().getConceptId());
    map.put("23730", hivMetadata.getQuarterlyDispensation().getConceptId());
    map.put("23888", hivMetadata.getSemiannualDispensation().getConceptId());
    map.put("23729", hivMetadata.getRapidFlow().getConceptId());
    map.put("23731", hivMetadata.getCommunityDispensation().getConceptId());

    String query =
        "SELECT p.patient_id  "
            + "FROM   patient p "
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                        WHERE  p.voided = 0 "
            + "                        AND o.voided = 0 "
            + "                        AND e.voided = 0 "
            + "                        AND e.location_id = :location "
            + "                        AND e.encounter_type = ${6} "
            + "                        AND ((o.concept_id = ${23724} AND o.value_coded = ${1267}) "
            + "                                    OR ( EXISTS( SELECT o.person_id FROM obs o  "
            + "                                    WHERE o.encounter_id = e.encounter_id AND o.concept_id = ${23730} AND o.value_coded = ${1267}) "
            + "                                    OR EXISTS( SELECT o.person_id FROM obs o  "
            + "                                    WHERE o.encounter_id = e.encounter_id AND o.concept_id = ${23888} AND o.value_coded = ${1267}) "
            + "                                    OR EXISTS (SELECT  o.person_id FROM obs o  "
            + "                                    WHERE o.encounter_id = e.encounter_id AND o.concept_id = ${23729} AND o.value_coded =${1267}) "
            + "                                    OR EXISTS (SELECT o.person_id FROM obs o  "
            + "                                    WHERE o.encounter_id = e.encounter_id AND o.concept_id = ${23731} AND o.value_coded =${1267} )) "
            + "                        ) "
            + "                        AND e.encounter_datetime < ( SELECT MAX(e.encounter_datetime) as last_consult "
            + "                        FROM   patient p "
            + "                            INNER JOIN encounter e "
            + "                                    ON e.patient_id = p.patient_id "
            + "                        WHERE  p.voided = 0 "
            + "                            AND e.voided = 0 "
            + "                            AND e.location_id = :location "
            + "                            AND e.encounter_type = ${6} "
            + "                            AND  e.encounter_datetime BETWEEN :startDate AND :endDate) ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * <b>E - Select all patients with the following Clinical Consultations or ARV Drugs Pick Ups:</b>
   *
   * <ul>
   *   <li>at least one Clinical Consultation (encounter type 6, encounter_datetime) or at least one
   *       ARV Pickup (encounter type 52, value_datetime(concept_id 23866)) between “Last Clinical
   *       Consultation” (encounter_datetime from A) minus 30 days and “Last Clinical Consultation”
   *       (encounter_datetime from A) minus 1 day
   *   <li>at least one Clinical Consultation (encounter type 6, encounter_datetime) or at least one
   *       ARV Pickup (encounter type 52, value_datetime(concept_id 23866)) between “Last Clinical
   *       Consultation” (encounter_datetime from A) minus 60 days and “Last Clinical Consultation”
   *       (encounter_datetime from A) minus 31 days
   *   <li>at least one Clinical Consultation (encounter type 6, encounter_datetime) or at least one
   *       ARV Pickup (encounter type 52, value_datetime(concept_id 23866)) between “Last Clinical
   *       Consultation” (encounter_datetime from A) minus 90 days and “Last Clinical Consultation”
   *       (encounter_datetime from A) minus 61 days
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMI15E(int upper, int lower) {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patients From Ficha Clinica");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("upper", upper);
    map.put("lower", lower);

    String query =
        "SELECT juncao.patient_id "
            + " FROM ( "
            + "         SELECT p.patient_id, e.encounter_datetime AS encounter_date "
            + "         FROM patient p "
            + "                  INNER JOIN encounter e on p.patient_id = e.patient_id "
            + "         WHERE p.voided = 0 "
            + "           AND e.voided = 0 "
            + " AND e.location_id =:location "
            + "           AND e.encounter_type = ${6} "
            + "         UNION "
            + "         SELECT p.patient_id, o.value_datetime AS encounter_date "
            + "         FROM patient p "
            + "            INNER JOIN encounter e on p.patient_id = e.patient_id "
            + "            INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "         WHERE p.voided = 0 "
            + "           AND e.voided = 0 "
            + "           AND o.voided = 0 "
            + " AND e.location_id =:location "
            + "           AND o.concept_id = ${23866} "
            + "           AND e.encounter_type = ${52} "
            + "     ) AS juncao "
            + "    INNER JOIN ( "
            + "                SELECT p.patient_id, MAX(e.encounter_datetime) AS encounter_datetime "
            + "                FROM  patient p "
            + "                    INNER JOIN encounter e on p.patient_id = e.patient_id "
            + "                WHERE p.voided =0 "
            + "                    AND  e.voided = 0 "
            + " AND e.location_id =:location "
            + "                    AND e.encounter_type = ${6} "
            + "                    AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "                GROUP BY p.patient_id "
            + "                 )  AS max_ficha on juncao.patient_id = max_ficha.patient_id "
            + "WHERE juncao.encounter_date "
            + "    BETWEEN DATE_SUB(max_ficha.encounter_datetime, INTERVAL ${upper} DAY) "
            + "        AND DATE_SUB(max_ficha.encounter_datetime, INTERVAL  ${lower} DAY)";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }
}
