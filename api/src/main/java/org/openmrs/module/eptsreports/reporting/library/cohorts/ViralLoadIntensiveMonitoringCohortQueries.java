package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This Cohort Query makes several unions of variety queries in {@link
 * IntensiveMonitoringCohortQueries }
 */
@Component
public class ViralLoadIntensiveMonitoringCohortQueries {

  private IntensiveMonitoringCohortQueries intensiveMonitoringCohortQueries;

  private HivMetadata hivMetadata;

  private CommonCohortQueries commonCohortQueries;

  private TxCurrCohortQueries txCurrCohortQueries;

  private String MAPPING =
      "evaluationPeriodStartDate=${endDate-12m+1d}, evaluationPeriodEndDate=${endDate-11m},location=${location}";

  @Autowired
  public ViralLoadIntensiveMonitoringCohortQueries(
      IntensiveMonitoringCohortQueries intensiveMonitoringCohortQueries,
      HivMetadata hivMetadata,
      CommonCohortQueries commonCohortQueries,
      TxCurrCohortQueries txCurrCohortQueries) {
    this.intensiveMonitoringCohortQueries = intensiveMonitoringCohortQueries;
    this.hivMetadata = hivMetadata;
    this.commonCohortQueries = commonCohortQueries;
    this.txCurrCohortQueries = txCurrCohortQueries;
  }

  /**
   * <b>Indicator 1 Denominator:</b> <br>
   * Number of patients in the 1st line of ART who had a clinical consultation in the review period
   * (data collection) and who were eligible to a VL request” <br>
   * Select all from the Denominator of MI report categories13.1, 13.6, 13.7, 13.8 (union all
   * specified categories)
   */
  public CohortDefinition getTotalIndicator1Den() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition mi13den1 = intensiveMonitoringCohortQueries.getCat13Den(1, false);
    CohortDefinition mi13den6 = intensiveMonitoringCohortQueries.getCat13Den(6, false);
    CohortDefinition mi13den7 = intensiveMonitoringCohortQueries.getCat13Den(7, false);
    CohortDefinition mi13den8 = intensiveMonitoringCohortQueries.getCat13Den(8, false);

    cd.addSearch(
        "1", EptsReportUtils.map(mi13den1, "revisionEndDate=${endDate},location=${location}"));
    cd.addSearch(
        "6", EptsReportUtils.map(mi13den6, "revisionEndDate=${endDate},location=${location}"));
    cd.addSearch(
        "7", EptsReportUtils.map(mi13den7, "revisionEndDate=${endDate},location=${location}"));
    cd.addSearch(
        "8", EptsReportUtils.map(mi13den8, "revisionEndDate=${endDate},location=${location}"));

    cd.setCompositionString("1 OR 6 OR 7 OR 8");
    return cd;
  }

  /**
   * <b>Indicator 1 Numerator:</b> <br>
   * Number of patients in the 1st line of ART who had a clinical consultation during the review
   * period (data collection), were eligible to a VL request and with a record of a VL request made
   * by the clinician <br>
   * Select all from the Numerator of MI report categories13.1, 13.6, 13.7, 13.8 (union all
   * specified categories)
   */
  public CohortDefinition getTotalIndicator1Num() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition mi13num1 = intensiveMonitoringCohortQueries.getCat13Den(1, true);
    CohortDefinition mi13num6 = intensiveMonitoringCohortQueries.getCat13Den(6, true);
    CohortDefinition mi13num7 = intensiveMonitoringCohortQueries.getCat13Den(7, true);
    CohortDefinition mi13num8 = intensiveMonitoringCohortQueries.getCat13Den(8, true);

    cd.addSearch(
        "1", EptsReportUtils.map(mi13num1, "revisionEndDate=${endDate},location=${location}"));
    cd.addSearch(
        "6", EptsReportUtils.map(mi13num6, "revisionEndDate=${endDate},location=${location}"));
    cd.addSearch(
        "7", EptsReportUtils.map(mi13num7, "revisionEndDate=${endDate},location=${location}"));
    cd.addSearch(
        "8", EptsReportUtils.map(mi13num8, "revisionEndDate=${endDate},location=${location}"));

    cd.setCompositionString("1 OR 6 OR 7 OR 8");
    return cd;
  }

  /**
   * <b>Indicator 2 Denominator:</b> <br>
   * Number of patients who started 1st-line ART or new 1st-line regimen in the month of evaluation
   * <br>
   * Select all from the Denominator of MI report categories 13.2, 13.9, 13.10, 13.11 (union all
   * specified categories)
   */
  public CohortDefinition getTotalIndicator2Den() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition mi13den2 = intensiveMonitoringCohortQueries.getMI13DEN2(2);
    CohortDefinition mi13den9 = intensiveMonitoringCohortQueries.getMI13DEN9(9);
    CohortDefinition mi13den10 = intensiveMonitoringCohortQueries.getMI13DEN10(10);
    CohortDefinition mi13den11 = intensiveMonitoringCohortQueries.getMI13DEN11(11);

    cd.addSearch(
        "2", EptsReportUtils.map(mi13den2, "revisionEndDate=${endDate},location=${location}"));
    cd.addSearch(
        "9", EptsReportUtils.map(mi13den9, "revisionEndDate=${endDate},location=${location}"));
    cd.addSearch(
        "10", EptsReportUtils.map(mi13den10, "revisionEndDate=${endDate},location=${location}"));
    cd.addSearch(
        "11", EptsReportUtils.map(mi13den11, "revisionEndDate=${endDate},location=${location}"));

    cd.setCompositionString("2 OR 9 OR 10 OR 11");
    return cd;
  }

  /**
   * <b>Indicator 2 Numerator:</b> <br>
   * Number of patients in the 1st line of ART who received the VL result between the sixth and
   * ninth month after starting ART <br>
   * Select all from the Numerator of MI report categories 13.2, 13.9, 13.10, 13.11 (union all
   * specified categories
   */
  public CohortDefinition getTotalIndicator2Num() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition mi13num2 = intensiveMonitoringCohortQueries.getMI13NUM2(2);
    CohortDefinition mi13num9 = intensiveMonitoringCohortQueries.getMI13NUM9(9);
    CohortDefinition mi13num10 = intensiveMonitoringCohortQueries.getMI13NUM10(10);
    CohortDefinition mi13num11 = intensiveMonitoringCohortQueries.getMI13NUM11(11);

    cd.addSearch(
        "2", EptsReportUtils.map(mi13num2, "revisionEndDate=${endDate},location=${location}"));
    cd.addSearch(
        "9", EptsReportUtils.map(mi13num9, "revisionEndDate=${endDate},location=${location}"));
    cd.addSearch(
        "10", EptsReportUtils.map(mi13num10, "revisionEndDate=${endDate},location=${location}"));
    cd.addSearch(
        "11", EptsReportUtils.map(mi13num11, "revisionEndDate=${endDate},location=${location}"));

    cd.setCompositionString("2 OR 9 OR 10 OR 11");
    return cd;
  }

  /**
   * <b>Indicator 3 Denominator:</b> <br>
   * NNumber of patients in the 1st line of ART with a VL result above 1000 in the month of
   * evaluation <br>
   * Select all from the Denominator of MI report categories 13.3, 13.12 (union all specified
   * categories
   */
  public CohortDefinition getTotalIndicator3Den() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition mi13den3 = intensiveMonitoringCohortQueries.getMICat13Part4(3, false);
    CohortDefinition mi13den12 = intensiveMonitoringCohortQueries.getMICat13Part4(12, false);

    cd.addSearch(
        "3", EptsReportUtils.map(mi13den3, "revisionEndDate=${endDate},location=${location}"));
    cd.addSearch(
        "12", EptsReportUtils.map(mi13den12, "revisionEndDate=${endDate},location=${location}"));

    cd.setCompositionString("3 OR 12");
    return cd;
  }

  /**
   * <b>Indicator 3 Numerator:</b> <br>
   * Number of patients in the 1st line of ART with a record of VL request between the 3rd and 4th
   * month after receiving the last CV result above 1000 and having 3 consecutive sessions of
   * APSS/PP <br>
   * Select all from the Numerator of MI report categories 13.3, 13.12 (union all specified
   * categories3
   */
  public CohortDefinition getTotalIndicator3Num() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition mi13num3 = intensiveMonitoringCohortQueries.getMICat13Part4(3, true);
    CohortDefinition mi13num12 = intensiveMonitoringCohortQueries.getMICat13Part4(12, true);

    cd.addSearch(
        "3", EptsReportUtils.map(mi13num3, "revisionEndDate=${endDate},location=${location}"));
    cd.addSearch(
        "12", EptsReportUtils.map(mi13num12, "revisionEndDate=${endDate},location=${location}"));

    cd.setCompositionString("3 OR 12");
    return cd;
  }

  /**
   * <b>Indicator 4 Denominator:</b> <br>
   * Number of patients on second line ART eligible for VL <br>
   * Select all from the Denominator of MI report categories 13.4, 13.13 (union all specified
   * categories
   */
  public CohortDefinition getTotalIndicator4Den() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition mi13den4 = intensiveMonitoringCohortQueries.getCat13Den(4, false);
    CohortDefinition mi13den13 = intensiveMonitoringCohortQueries.getCat13Den(13, false);

    cd.addSearch(
        "4", EptsReportUtils.map(mi13den4, "revisionEndDate=${endDate},location=${location}"));
    cd.addSearch(
        "13", EptsReportUtils.map(mi13den13, "revisionEndDate=${endDate},location=${location}"));

    cd.setCompositionString("4 OR 13");
    return cd;
  }

  /**
   * <b>Indicator 4 Numerator:</b> <br>
   * Number of patients on second line ART eligible for VL with a VL request made by the clinician
   * <br>
   * Select all from the Numerator of MI report categories 13.4, 13.13 (union all specified
   * categories
   */
  public CohortDefinition getTotalIndicator4Num() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition mi13num4 = intensiveMonitoringCohortQueries.getCat13Den(4, true);
    CohortDefinition mi13num13 = intensiveMonitoringCohortQueries.getCat13Den(13, true);

    cd.addSearch(
        "4", EptsReportUtils.map(mi13num4, "revisionEndDate=${endDate},location=${location}"));
    cd.addSearch(
        "13", EptsReportUtils.map(mi13num13, "revisionEndDate=${endDate},location=${location}"));

    cd.setCompositionString("4 OR 13");
    return cd;
  }

  /**
   * <b>Indicator 5 Denominator:</b> <br>
   * Number of patients with a record of starting the 2nd line of ART in the month of evaluation
   * <br>
   * Select all from the Denominator of MI report categories 13.5, 13.14 (union all specified
   * categories
   */
  public CohortDefinition getTotalIndicator5Den() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition mi13den5 = intensiveMonitoringCohortQueries.getMI13DEN5(5);
    CohortDefinition mi13den14 = intensiveMonitoringCohortQueries.getMI13DEN14(14);

    cd.addSearch(
        "5", EptsReportUtils.map(mi13den5, "revisionEndDate=${endDate},location=${location}"));
    cd.addSearch(
        "14", EptsReportUtils.map(mi13den14, "revisionEndDate=${endDate},location=${location}"));

    cd.setCompositionString("5 OR 14");
    return cd;
  }

  /**
   * <b>Indicator 5 Numerator:</b> <br>
   * Number of patients with a record of starting 2nd line ART in the month of evaluation and who
   * received the VL result between the sixth and ninth month after starting 2nd line ART <br>
   * Select all from the Numerator of MI report categories 13.5, 13.14 (union all specified
   * categories
   */
  public CohortDefinition getTotalIndicator5Num() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition mi13num4 = intensiveMonitoringCohortQueries.getMI13NUM5(5);
    CohortDefinition mi13num13 = intensiveMonitoringCohortQueries.getMI13NUM14(14);

    cd.addSearch(
        "5", EptsReportUtils.map(mi13num4, "revisionEndDate=${endDate},location=${location}"));
    cd.addSearch(
        "14", EptsReportUtils.map(mi13num13, "revisionEndDate=${endDate},location=${location}"));

    cd.setCompositionString("5 OR 14");
    return cd;
  }

  public CohortDefinition getPartOneOfXQuery() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.addParameter(
        new Parameter("evaluationPeriodStartDate", "evaluationPeriodStartDate", Date.class));
    sqlCohortDefinition.addParameter(
        new Parameter("evaluationPeriodEndDate", "evaluationPeriodEndDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("6332", hivMetadata.getBreastfeeding().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());

    String sql =
        "SELECT p.patient_id "
            + "FROM patient p "
            + "INNER JOIN ( "
            + "SELECT min_vl.patient_id , MIN(min_datetime) final_min_date "
            + "FROM ( "
            + "         SELECT p.patient_id, MIN(e.encounter_datetime) AS min_datetime "
            + "         FROM patient p "
            + "                  INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                  INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "         WHERE p.voided = 0 "
            + "           AND o.voided = 0 "
            + "           AND e.voided = 0 "
            + "           AND e.encounter_type = ${6} "
            + "           AND o.concept_id = ${856} "
            + "           AND o.value_numeric >= 1000 "
            + "           AND e.encounter_datetime BETWEEN :evaluationPeriodStartDate AND :evaluationPeriodEndDate  "
            + "           AND e.location_id = :location"
            + "         GROUP BY p.patient_id "
            + "         UNION "
            + "         SELECT p.patient_id, MIN(o.obs_datetime) AS min_datetime "
            + "         FROM patient p "
            + "                  INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                  INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "         WHERE p.voided = 0 "
            + "           AND o.voided = 0 "
            + "           AND e.voided = 0 "
            + "           AND e.encounter_type = ${53} "
            + "           AND o.concept_id = ${856} "
            + "           AND o.value_numeric >= 1000 "
            + "           AND o.obs_datetime BETWEEN :evaluationPeriodStartDate AND :evaluationPeriodEndDate "
            + "           AND e.location_id = :location "
            + "        GROUP BY p.patient_id "
            + "     )min_vl "
            + "GROUP BY min_vl.patient_id) final_min ON final_min.patient_id = p.patient_id "
            + "WHERE p.patient_id NOT IN ( "
            + "                            SELECT p.patient_id "
            + "                            FROM patient p "
            + "                                     INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                                     INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                            WHERE p.voided = 0 "
            + "                              AND o.voided = 0 "
            + "                              AND e.voided = 0 "
            + "                              AND e.encounter_type = ${53} "
            + "                              AND o.concept_id IN (${1982},${6332}) "
            + "                              AND o.value_coded = ${1065} "
            + "                              AND o.obs_datetime = final_min.final_min_date "
            + "                            UNION "
            + "                            SELECT p.patient_id "
            + "                            FROM patient p "
            + "                                     INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                                     INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                            WHERE p.voided = 0 "
            + "                              AND o.voided = 0 "
            + "                              AND e.voided = 0 "
            + "                              AND e.encounter_type = ${6} "
            + "                              AND o.concept_id IN (${1982},${6332}) "
            + "                              AND o.value_coded = ${1065} "
            + "                              AND e.encounter_datetime = final_min.final_min_date)";

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(sql));
    return sqlCohortDefinition;
  }

  public CohortDefinition getFirstLineArt() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.addParameter(
        new Parameter("evaluationPeriodStartDate", "evaluationPeriodStartDate", Date.class));
    sqlCohortDefinition.addParameter(
        new Parameter("evaluationPeriodEndDate", "evaluationPeriodEndDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("21151", hivMetadata.getTherapeuticLineConcept().getConceptId());
    map.put("21150", hivMetadata.getFirstLineConcept().getConceptId());

    String sql =
        "SELECT patient_id  "
            + "FROM ( "
            + "SELECT p.patient_id, MAX(encounter_datetime) AS last_date "
            + "FROM patient p "
            + "    INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "    INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "WHERE p.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND e.encounter_type = ${6} "
            + "  AND o.concept_id = ${21151} "
            + "  AND o.value_coded = ${21150} "
            + "  AND e.encounter_datetime BETWEEN :evaluationPeriodStartDate AND :evaluationPeriodEndDate "
            + "  AND e.location_id = :location) ";

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(sql));

    return sqlCohortDefinition;
  }

  public CohortDefinition getComposedXQuery() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.addParameter(
        new Parameter("evaluationPeriodStartDate", "evaluationPeriodStartDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("evaluationPeriodEndDate", "evaluationPeriodEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    compositionCohortDefinition.addSearch(
        "1",
        EptsReportUtils.map(
            this.getPartOneOfXQuery(),
            "evaluationPeriodStartDate=${evaluationPeriodStartDate-7m},evaluationPeriodEndDate=${evaluationPeriodEndDate-7m},location=${location}"));

    compositionCohortDefinition.addSearch(
        "2",
        EptsReportUtils.map(
            this.getFirstLineArt(),
            "evaluationPeriodStartDate=${evaluationPeriodStartDate-7m},evaluationPeriodEndDate=${evaluationPeriodEndDate-7m},location=${location}"));

    compositionCohortDefinition.addSearch(
        "transferredIn",
        EptsReportUtils.map(
            this.commonCohortQueries.getMohTransferredInPatients(),
            "onOrBefore=${evaluationPeriodEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "transferredOut",
        EptsReportUtils.map(
            this.commonCohortQueries.getMohTransferredOutPatientsByEndOfPeriod(),
            "onOrBefore=${evaluationPeriodEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "dead",
        EptsReportUtils.map(
            this.getDeadPatients(),
            "evaluationPeriodEndDate=${evaluationPeriodEndDate},location=${location}"));

    compositionCohortDefinition.setCompositionString(
        "(1 AND 2) AND NOT ( transferredIn OR transferredOut OR dead)");

    return compositionCohortDefinition;
  }

  private CohortDefinition getDeadPatients() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.addParameter(
        new Parameter("evaluationPeriodEndDate", "evaluationPeriodEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    compositionCohortDefinition.addSearch(
        "1",
        EptsReportUtils.map(
            this.txCurrCohortQueries
                .getPatientsDeadTransferredOutSuspensionsInProgramStateByReportingEndDate(),
            "onOrBefore=${evaluationPeriodEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "2",
        EptsReportUtils.map(
            this.txCurrCohortQueries.getDeadPatientsInDemographiscByReportingEndDate(),
            "onOrBefore=${onOrBefore},location=${location}"));

    compositionCohortDefinition.addSearch(
        "3",
        EptsReportUtils.map(
            this.txCurrCohortQueries
                .getPatientDeathRegisteredInLastHomeVisitCardByReportingEndDate(),
            "onOrBefore=${evaluationPeriodEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "4",
        EptsReportUtils.map(
            this.txCurrCohortQueries
                .getDeadPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate(),
            "onOrBefore=${evaluationPeriodEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "5",
        EptsReportUtils.map(
            this.txCurrCohortQueries
                .getTransferredOutPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate(),
            "onOrBefore=${evaluationPeriodEndDate},location=${location}"));

    compositionCohortDefinition.setCompositionString("1 OR 2 OR 3 OR 4 OR 5");

    return compositionCohortDefinition;
  }

  /**
   * All patients with 3 APSS/PP consultations (encounter type 35) within 99 days of first VL date
   * >= 1000 (check Y section: if VL is from encounter type 6 use encounter datetime, if VL is from
   * encounter type 53 use obs datetime) as VL DateY as follows:
   *
   * <p>One APSS/PP (encounter type 35) in the same day as VL DateY
   *
   * @return
   */
  public CohortDefinition getZpart1() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.addParameter(
        new Parameter("evaluationPeriodStartDate", "evaluationPeriodStartDate", Date.class));
    cd.addParameter(
        new Parameter("evaluationPeriodEndDate", "evaluationPeriodEndDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("6332", hivMetadata.getBreastfeeding().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    String query =
        " SELECT p.patient_id FROM   patient p  "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN (SELECT p.patient_id "
            + "  FROM patient p "
            + "  INNER JOIN ( "
            + "  SELECT min_vl.patient_id , MIN(min_datetime) final_min_date "
            + "  FROM ( "
            + "         SELECT p.patient_id, MIN(e.encounter_datetime) AS min_datetime "
            + "         FROM patient p "
            + "                  INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                  INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "         WHERE p.voided = 0 "
            + "           AND o.voided = 0 "
            + "           AND e.voided = 0 "
            + "           AND e.encounter_type = ${6} "
            + "           AND o.concept_id = ${856} "
            + "           AND o.value_numeric >= 1000 "
            + "           AND e.encounter_datetime BETWEEN :evaluationPeriodStartDate AND :evaluationPeriodEndDate  "
            + "           AND e.location_id = :location"
            + "         GROUP BY p.patient_id "
            + "         UNION "
            + "         SELECT p.patient_id, MIN(o.obs_datetime) AS min_datetime "
            + "         FROM patient p "
            + "                  INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                  INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "         WHERE p.voided = 0 "
            + "           AND o.voided = 0 "
            + "           AND e.voided = 0 "
            + "           AND e.encounter_type = ${53} "
            + "           AND o.concept_id = ${856} "
            + "           AND o.value_numeric >= 1000 "
            + "           AND o.obs_datetime BETWEEN :evaluationPeriodStartDate AND :evaluationPeriodEndDate "
            + "           AND e.location_id = :location "
            + "        GROUP BY p.patient_id "
            + "     )min_vl "
            + "    GROUP BY min_vl.patient_id) final_min ON final_min.patient_id = p.patient_id "
            + "    WHERE p.patient_id NOT IN ( "
            + "                 SELECT p.patient_id "
            + "                 FROM patient p "
            + "                          INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                          INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                 WHERE p.voided = 0 "
            + "                   AND o.voided = 0 "
            + "                   AND e.voided = 0 "
            + "                   AND e.encounter_type = ${53} "
            + "                   AND o.concept_id IN (${1982},${6332}) "
            + "                   AND o.value_coded = ${1065} "
            + "                   AND o.obs_datetime = final_min.final_min_date "
            + "                 UNION "
            + "                 SELECT p.patient_id "
            + "                 FROM patient p "
            + "                          INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                          INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                 WHERE p.voided = 0 "
            + "                   AND o.voided = 0 "
            + "                   AND e.voided = 0 "
            + "                   AND e.encounter_type = ${6} "
            + "                   AND o.concept_id IN (${1982},${6332}) "
            + "                   AND o.value_coded = ${1065} "
            + "                   AND e.encounter_datetime = final_min.final_min_date))AS vlDateY    "
            + "         ON vlDateY.patient_id = p.patient_id  "
            + "     WHERE  e.encounter_type = ${35} "
            + "         AND e.voided = 0 "
            + "         AND p.voided = 0 "
            + "         AND e.encounter_datetime = vlDateY.final_min_date "
            + "         GROUP BY p.patient_id ";

    StringSubstitutor sb = new StringSubstitutor(map);
    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   * All patients with 3 APSS/PP consultations (encounter type 35) within 99 days of first VL date
   * >= 1000 (check Y section: if VL is from encounter type 6 use encounter datetime, if VL is from
   * encounter type 53 use obs datetime) as VL DateY as follows:
   *
   * <p>Another APSS/PP (encounter type 35) occurred between 20 to 33 days after the First VL
   * Date>=1000 as VL DateY ( encounter datetime[2nd apss/pp] >= VL DateY + 20 days and <= VL DateY
   * + 33 days ) and
   *
   * @return
   */
  public CohortDefinition getZpart2() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.addParameter(
        new Parameter("evaluationPeriodStartDate", "evaluationPeriodStartDate", Date.class));
    cd.addParameter(
        new Parameter("evaluationPeriodEndDate", "evaluationPeriodEndDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("6332", hivMetadata.getBreastfeeding().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    String query =
        "SELECT patient_id FROM                                                                                                                                                                                                  "
            + "(SELECT p.patient_id, MIN(e.encounter_datetime) AS first_occur FROM   patient p  "
            + "       INNER JOIN encounter e ON p.patient_id = e.patient_id  "
            + "       INNER JOIN (SELECT p.patient_id, final_min.final_min_date  "
            + "            FROM patient p    "
            + "            INNER JOIN (  "
            + "            SELECT min_vl.patient_id , MIN(min_datetime) final_min_date   "
            + "            FROM (    "
            + "                     SELECT p.patient_id, MIN(e.encounter_datetime) AS min_datetime   "
            + "                     FROM patient p   "
            + "                              INNER JOIN encounter e ON e.patient_id = p.patient_id   "
            + "                              INNER JOIN obs o ON e.encounter_id = o.encounter_id     "
            + "                     WHERE p.voided = 0   "
            + "                       AND o.voided = 0   "
            + "                       AND e.voided = 0   "
            + "                       AND e.encounter_type = ${6}   "
            + "                       AND o.concept_id = ${856}     "
            + "                       AND o.value_numeric >= 1000    "
            + "                       AND e.encounter_datetime BETWEEN :evaluationPeriodStartDate AND :evaluationPeriodEndDate   "
            + "                       AND e.location_id = :location    "
            + "                     GROUP BY p.patient_id    "
            + "                     UNION    "
            + "                     SELECT p.patient_id, MIN(o.obs_datetime) AS min_datetime     "
            + "                     FROM patient p   "
            + "                              INNER JOIN encounter e ON e.patient_id = p.patient_id   "
            + "                              INNER JOIN obs o ON e.encounter_id = o.encounter_id     "
            + "                     WHERE p.voided = 0   "
            + "                       AND o.voided = 0   "
            + "                       AND e.voided = 0   "
            + "                       AND e.encounter_type = ${53}  "
            + "                       AND o.concept_id = ${856}     "
            + "                       AND o.value_numeric >= 1000    "
            + "                       AND o.obs_datetime BETWEEN :evaluationPeriodStartDate AND :evaluationPeriodEndDate "
            + "                       AND e.location_id = :location    "
            + "                    GROUP BY p.patient_id     "
            + "                 )min_vl  "
            + "          GROUP BY min_vl.patient_id) final_min ON final_min.patient_id = p.patient_id    "
            + "          WHERE p.patient_id NOT IN (     "
            + "            SELECT p.patient_id   "
            + "            FROM patient p    "
            + "                     INNER JOIN encounter e ON e.patient_id = p.patient_id    "
            + "                     INNER JOIN obs o ON e.encounter_id = o.encounter_id  "
            + "            WHERE p.voided = 0    "
            + "              AND o.voided = 0    "
            + "              AND e.voided = 0    "
            + "              AND e.encounter_type = ${53}   "
            + "              AND o.concept_id IN (${1982},${6332})     "
            + "              AND o.value_coded = ${1065}    "
            + "              AND o.obs_datetime = final_min.final_min_date   "
            + "            UNION     "
            + "            SELECT p.patient_id   "
            + "            FROM patient p    "
            + "                     INNER JOIN encounter e ON e.patient_id = p.patient_id    "
            + "                     INNER JOIN obs o ON e.encounter_id = o.encounter_id  "
            + "            WHERE p.voided = 0    "
            + "              AND o.voided = 0    "
            + "              AND e.voided = 0    "
            + "              AND e.encounter_type = ${6}    "
            + "              AND o.concept_id IN (${1982},${6332})     "
            + "              AND o.value_coded = ${1065}    "
            + "              AND e.encounter_datetime = final_min.final_min_date))AS vlDateY "
            + "               ON vlDateY.patient_id = p.patient_id   "
            + "        WHERE  e.encounter_type = ${35}  "
            + "            AND e.voided = 0  "
            + "            AND p.voided = 0  "
            + "            AND e.encounter_datetime >= DATE_ADD(vlDateY.final_min_date, interval 20 DAY)     "
            + "            AND e.encounter_datetime <= DATE_ADD(vlDateY.final_min_date, interval 33 DAY) "
            + "            GROUP BY p.patient_id )secondApss;";

    StringSubstitutor sb = new StringSubstitutor(map);
    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   * All patients with 3 APSS/PP consultations (encounter type 35) within 99 days of first VL date
   * >= 1000 (check Y section: if VL is from encounter type 6 use encounter datetime, if VL is from
   * encounter type 53 use obs datetime) as VL DateY as follows:
   *
   * <p>Another APSS/PP (encounter type 35) occurred between 20 to 33 days after the 2nd apss/pp (
   * encounter datetime[3rd apss/pp] >= 2nd apss/pp + 20 days and <= 2nd apss/pp + 33 days ) and
   *
   * @return
   */
  public CohortDefinition getZpart3() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.addParameter(
        new Parameter("evaluationPeriodStartDate", "evaluationPeriodStartDate", Date.class));
    cd.addParameter(
        new Parameter("evaluationPeriodEndDate", "evaluationPeriodEndDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("6332", hivMetadata.getBreastfeeding().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    String query =
        " SELECT p.patient_id FROM   patient p                                                                                                                                                          "
            + "     INNER JOIN encounter e ON p.patient_id = e.patient_id    "
            + "     INNER JOIN (SELECT p.patient_id, MIN(e.encounter_datetime) AS first_occur FROM   patient p   "
            + "     INNER JOIN encounter e ON p.patient_id = e.patient_id    "
            + "     INNER JOIN (SELECT p.patient_id, final_min.final_min_date    "
            + "          FROM patient p  "
            + "          INNER JOIN (    "
            + "          SELECT min_vl.patient_id , MIN(min_datetime) final_min_date     "
            + "          FROM (  "
            + "                   SELECT p.patient_id, MIN(e.encounter_datetime) AS min_datetime     "
            + "                   FROM patient p     "
            + "                            INNER JOIN encounter e ON e.patient_id = p.patient_id     "
            + "                            INNER JOIN obs o ON e.encounter_id = o.encounter_id   "
            + "                   WHERE p.voided = 0     "
            + "                     AND o.voided = 0     "
            + "                     AND e.voided = 0     "
            + "                     AND e.encounter_type = ${6}     "
            + "                     AND o.concept_id = ${856}   "
            + "                     AND o.value_numeric >= 1000  "
            + "                     AND e.encounter_datetime BETWEEN :evaluationPeriodStartDate AND :evaluationPeriodEndDate  "
            + "                     AND e.location_id = :location  "
            + "                   GROUP BY p.patient_id  "
            + "                   UNION  "
            + "                   SELECT p.patient_id, MIN(o.obs_datetime) AS min_datetime   "
            + "                   FROM patient p     "
            + "                            INNER JOIN encounter e ON e.patient_id = p.patient_id     "
            + "                            INNER JOIN obs o ON e.encounter_id = o.encounter_id   "
            + "                   WHERE p.voided = 0     "
            + "                     AND o.voided = 0     "
            + "                     AND e.voided = 0     "
            + "                     AND e.encounter_type = ${53}    "
            + "                     AND o.concept_id = ${856}   "
            + "                     AND o.value_numeric >= 1000  "
            + "                     AND o.obs_datetime BETWEEN :evaluationPeriodStartDate AND :evaluationPeriodEndDate"
            + "                     AND e.location_id = :location  "
            + "                  GROUP BY p.patient_id   "
            + "               )min_vl    "
            + "        GROUP BY min_vl.patient_id) final_min ON final_min.patient_id = p.patient_id  "
            + "        WHERE p.patient_id NOT IN (   "
            + "          SELECT p.patient_id     "
            + "          FROM patient p  "
            + "                   INNER JOIN encounter e ON e.patient_id = p.patient_id  "
            + "                   INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "          WHERE p.voided = 0  "
            + "            AND o.voided = 0  "
            + "            AND e.voided = 0  "
            + "            AND e.encounter_type = ${53}     "
            + "            AND o.concept_id IN (${1982},${6332})   "
            + "            AND o.value_coded = ${1065}  "
            + "            AND o.obs_datetime = final_min.final_min_date     "
            + "          UNION   "
            + "          SELECT p.patient_id     "
            + "          FROM patient p  "
            + "                   INNER JOIN encounter e ON e.patient_id = p.patient_id  "
            + "                   INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "          WHERE p.voided = 0  "
            + "            AND o.voided = 0  "
            + "            AND e.voided = 0  "
            + "            AND e.encounter_type = ${6}  "
            + "            AND o.concept_id IN (${1982},${6332})   "
            + "            AND o.value_coded = ${1065}  "
            + "            AND e.encounter_datetime = final_min.final_min_date))AS vlDateY   "
            + "             ON vlDateY.patient_id = p.patient_id "
            + "      WHERE  e.encounter_type = ${35}    "
            + "          AND e.voided = 0    "
            + "          AND p.voided = 0    "
            + "          AND e.encounter_datetime >= DATE_ADD(vlDateY.final_min_date, INTERVAL 20 DAY)   "
            + "          AND e.encounter_datetime <= DATE_ADD(vlDateY.final_min_date, INTERVAL 33 DAY)   "
            + "          GROUP BY p.patient_id ) AS secondApss on secondApss.patient_id = e.patient_id   "
            + "     WHERE    "
            + "     e.voided = 0 "
            + "          AND p.voided = 0 AND  e.encounter_type = ${35} "
            + "          AND e.encounter_datetime >= DATE_ADD(secondApss.first_occur, INTERVAL 20 DAY)   "
            + "          AND e.encounter_datetime <= DATE_ADD(secondApss.first_occur, INTERVAL 33 DAY)   "
            + "          GROUP BY p.patient_id ";

    StringSubstitutor sb = new StringSubstitutor(map);
    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   * All patients with 3 APSS/PP consultations (encounter type 35) within 99 days of first VL date
   * >= 1000 (check Y section: if VL is from encounter type 6 use encounter datetime, if VL is from
   * encounter type 53 use obs datetime) as VL DateY as follows:
   *
   * <p>Another APSS/PP (encounter type 35) occurred between 20 to 33 days after the 2nd apss/pp (
   * encounter datetime[3rd apss/pp] >= 2nd apss/pp + 20 days and <= 2nd apss/pp + 33 days ) and
   *
   * @return
   */
  public CohortDefinition getZpart4() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.addParameter(
        new Parameter("evaluationPeriodStartDate", "evaluationPeriodStartDate", Date.class));
    cd.addParameter(
        new Parameter("evaluationPeriodEndDate", "evaluationPeriodEndDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("6332", hivMetadata.getBreastfeeding().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());

    String query =
        "   SELECT p.patient_id FROM   patient p   "
            + " INNER JOIN encounter e ON p.patient_id = e.patient_id    "
            + " INNER JOIN obs o on e.encounter_id = o.encounter_id  "
            + " INNER JOIN (SELECT p.patient_id, final_min.final_min_date as vlDate  "
            + "      FROM patient p  "
            + "      INNER JOIN (    "
            + "      SELECT min_vl.patient_id , MIN(min_datetime) final_min_date     "
            + "      FROM (  "
            + "               SELECT p.patient_id, MIN(e.encounter_datetime) AS min_datetime     "
            + "               FROM patient p     "
            + "                        INNER JOIN encounter e ON e.patient_id = p.patient_id     "
            + "                        INNER JOIN obs o ON e.encounter_id = o.encounter_id   "
            + "               WHERE p.voided = 0     "
            + "                 AND o.voided = 0     "
            + "                 AND e.voided = 0     "
            + "                 AND e.encounter_type = ${6}     "
            + "                 AND o.concept_id = ${856}   "
            + "                 AND o.value_numeric >= 1000  "
            + "                 AND e.encounter_datetime BETWEEN :evaluationPeriodStartDate AND :evaluationPeriodEndDate   "
            + "                 AND e.location_id = :location  "
            + "               GROUP BY p.patient_id  "
            + "               UNION  "
            + "               SELECT p.patient_id, MIN(o.obs_datetime) AS min_datetime   "
            + "               FROM patient p     "
            + "                        INNER JOIN encounter e ON e.patient_id = p.patient_id     "
            + "                        INNER JOIN obs o ON e.encounter_id = o.encounter_id   "
            + "               WHERE p.voided = 0     "
            + "                 AND o.voided = 0     "
            + "                 AND e.voided = 0     "
            + "                 AND e.encounter_type = ${53}    "
            + "                 AND o.concept_id = ${856}   "
            + "                 AND o.value_numeric >= 1000  "
            + "                 AND o.obs_datetime BETWEEN :evaluationPeriodStartDate AND :evaluationPeriodEndDate "
            + "                 AND e.location_id = :location  "
            + "              GROUP BY p.patient_id   "
            + "           )min_vl    "
            + "      GROUP BY min_vl.patient_id) final_min ON final_min.patient_id = p.patient_id    "
            + "      WHERE p.patient_id NOT IN (     "
            + "           SELECT p.patient_id    "
            + "           FROM patient p     "
            + "                    INNER JOIN encounter e ON e.patient_id = p.patient_id     "
            + "                    INNER JOIN obs o ON e.encounter_id = o.encounter_id   "
            + "           WHERE p.voided = 0     "
            + "             AND o.voided = 0     "
            + "             AND e.voided = 0     "
            + "             AND e.encounter_type = ${53}    "
            + "             AND o.concept_id IN (${1982},${6332})  "
            + "             AND o.value_coded = ${1065}     "
            + "             AND o.obs_datetime = final_min.final_min_date    "
            + "           UNION  "
            + "           SELECT p.patient_id    "
            + "           FROM patient p     "
            + "                    INNER JOIN encounter e ON e.patient_id = p.patient_id     "
            + "                    INNER JOIN obs o ON e.encounter_id = o.encounter_id   "
            + "           WHERE p.voided = 0     "
            + "             AND o.voided = 0     "
            + "             AND e.voided = 0     "
            + "             AND e.encounter_type = ${6}     "
            + "             AND o.concept_id IN (${1982},${6332})  "
            + "             AND o.value_coded = ${1065}     "
            + "             AND e.encounter_datetime = final_min.final_min_date))AS vlDateY  "
            + "         ON vlDateY.patient_id = p.patient_id "
            + "  WHERE  e.encounter_type = ${6} "
            + "      AND e.voided = 0    "
            + "      AND p.voided = 0    "
            + "      AND o.concept_id = ${23722}    "
            + "      AND o.value_coded = ${856} "
            + "      AND e.encounter_datetime BETWEEN    "
            + "      DATE_ADD(vlDateY.vlDate, INTERVAL 80 DAY)   "
            + "      AND DATE_ADD(vlDateY.vlDate, INTERVAL 130 DAY)  "
            + "      GROUP BY p.patient_id ";

    StringSubstitutor sb = new StringSubstitutor(map);
    cd.setQuery(sb.replace(query));

    return cd;
  }

  private CohortDefinition getComposedZQuery() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.addParameter(
        new Parameter("evaluationPeriodStartDate", "evaluationPeriodStartDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("evaluationPeriodEndDate", "evaluationPeriodEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    compositionCohortDefinition.addSearch("Z1", EptsReportUtils.map(getZpart1(), MAPPING));

    compositionCohortDefinition.addSearch("Z2", EptsReportUtils.map(getZpart2(), MAPPING));

    compositionCohortDefinition.addSearch("Z3", EptsReportUtils.map(getZpart3(), MAPPING));

    compositionCohortDefinition.addSearch("Z4", EptsReportUtils.map(getZpart4(), MAPPING));

    compositionCohortDefinition.setCompositionString("Z1 AND Z2 AND Z3 AND Z4");

    return compositionCohortDefinition;
  }
}
