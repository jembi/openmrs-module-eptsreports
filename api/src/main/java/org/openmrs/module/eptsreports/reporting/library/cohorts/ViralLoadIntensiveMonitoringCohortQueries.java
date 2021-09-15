package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
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

  private final int  VIRAL_LOAD_1000_COPIES = 1000;

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
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("6332", hivMetadata.getBreastfeeding().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());

    String sql =
        " SELECT p.patient_id "
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
            + "           AND e.encounter_datetime BETWEEN :startDate AND :endDate  "
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
            + "           AND o.obs_datetime BETWEEN :startDate AND :endDate "
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
    sqlCohortDefinition.setName("Linha Terapeutica");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("21151", hivMetadata.getTherapeuticLineConcept().getConceptId());
    map.put("21150", hivMetadata.getFirstLineConcept().getConceptId());

    String sql =
        " SELECT p.patient_id  "
            + " FROM patient p INNER  JOIN ("
            + " SELECT p.patient_id , MAX(encounter_datetime) AS last_date "
            + " FROM patient p "
            + "    INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "    INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + " WHERE p.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND e.encounter_type = ${6} "
            + "  AND o.concept_id = ${21151} "
            + "  AND o.value_coded = ${21150} "
            + "  AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "  AND e.location_id = :location )  ultima ON ultima.patient_id = p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(sql));

    return sqlCohortDefinition;
  }

  public CohortDefinition getComposedXQuery() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    compositionCohortDefinition.addSearch(
        "1",
        EptsReportUtils.map(
            this.getPartOneOfXQuery(),
            "startDate=${startDate-7m},endDate=${endDate-7m},location=${location}"));

    compositionCohortDefinition.addSearch(
        "2",
        EptsReportUtils.map(
            this.getFirstLineArt(),
            "startDate=${startDate-7m},endDate=${endDate-7m},location=${location}"));

    compositionCohortDefinition.addSearch(
        "transferredIn",
        EptsReportUtils.map(
            this.commonCohortQueries.getMohTransferredInPatients(),
            "onOrBefore=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "transferredOut",
        EptsReportUtils.map(
            this.commonCohortQueries.getMohTransferredOutPatientsByEndOfPeriod(),
            "onOrBefore=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "dead",
        EptsReportUtils.map(this.getDeadPatients(), "endDate=${endDate},location=${location}"));

    compositionCohortDefinition.setCompositionString(
        "(1 AND 2) AND NOT ( transferredIn OR transferredOut OR dead)");

    return compositionCohortDefinition;
  }

  public CohortDefinition getDeadPatients() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    compositionCohortDefinition.addSearch(
        "1",
        EptsReportUtils.map(
            this.txCurrCohortQueries
                .getPatientsDeadTransferredOutSuspensionsInProgramStateByReportingEndDate(),
            "onOrBefore=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "2",
        EptsReportUtils.map(
            this.txCurrCohortQueries.getDeadPatientsInDemographiscByReportingEndDate(),
            "onOrBefore=${endDate}"));

    compositionCohortDefinition.addSearch(
        "3",
        EptsReportUtils.map(
            this.txCurrCohortQueries
                .getPatientDeathRegisteredInLastHomeVisitCardByReportingEndDate(),
            "onOrBefore=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "4",
        EptsReportUtils.map(
            this.txCurrCohortQueries
                .getDeadPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate(),
            "onOrBefore=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "5",
        EptsReportUtils.map(
            this.txCurrCohortQueries
                .getTransferredOutPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate(),
            "onOrBefore=${endDate},location=${location}"));

    compositionCohortDefinition.setCompositionString("1 OR 2 OR 3 OR 4 OR 5");

    return compositionCohortDefinition;
  }

  public CohortDefinition getFirstAPSSInTheSameDayOfXQuery() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("6332", hivMetadata.getBreastfeeding().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    String sql =
        "SELECT p.patient_id "
            + "FROM patient p "
            + "    INNER JOIN encounter e on p.patient_id = e.patient_id "
            + "    INNER JOIN  (  "
            + " SELECT p.patient_id, final_min.final_min_date "
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
            + "           AND e.encounter_datetime BETWEEN :startDate AND :endDate  "
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
            + "           AND o.obs_datetime BETWEEN :startDate AND :endDate "
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
            + "                              AND e.encounter_datetime = final_min.final_min_date)"
            + " ) AS xquery ON xquery.patient_id=p.patient_id "
            + " WHERE p.voided = 0 "
            + "   AND e.voided = 0"
            + "   AND e.encounter_type = ${35} "
            + "   AND e.encounter_datetime = xquery.final_min_date "
            + "   AND e.location_id = :location ";

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(sql));
    return sqlCohortDefinition;
  }

  public CohortDefinition getAPSSInIn20To33DaysAfterXQuery() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("6332", hivMetadata.getBreastfeeding().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    String sql =
        "SELECT p.patient_id "
            + "FROM patient p "
            + "    INNER JOIN encounter e on p.patient_id = e.patient_id "
            + "    INNER JOIN  (  "
            + " SELECT p.patient_id, final_min.final_min_date "
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
            + "           AND e.encounter_datetime BETWEEN :startDate AND :endDate  "
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
            + "           AND o.obs_datetime BETWEEN :startDate AND :endDate "
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
            + "                              AND e.encounter_datetime = final_min.final_min_date)"
            + " ) AS xquery ON xquery.patient_id=p.patient_id "
            + " WHERE p.voided = 0 "
            + "   AND e.voided = 0"
            + "   AND e.encounter_type = ${35} "
            + "   AND TIMESTAMPDIFF(DAY, xquery.final_min_date, e.encounter_datetime) BETWEEN 20 AND 33 "
            + "   AND e.location_id = :location ";

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(sql));
    return sqlCohortDefinition;
  }

  public CohortDefinition getAPSSIn20To33DaysAfter2ndAPSSConsultation() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("6332", hivMetadata.getBreastfeeding().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    String sql =
        " SELECT p.patient_id"
            + " FROM patient p "
            + "    INNER JOIN encounter e on p.patient_id = e.patient_id "
            + "    INNER JOIN  ( "
            + " SELECT p.patient_id, MIN(e.encounter_datetime) min_apss "
            + " FROM patient p "
            + "    INNER JOIN encounter e on p.patient_id = e.patient_id "
            + "    INNER JOIN  (  "
            + " SELECT p.patient_id, final_min.final_min_date "
            + " FROM patient p "
            + " INNER JOIN ( "
            + " SELECT min_vl.patient_id , MIN(min_datetime) final_min_date "
            + " FROM ( "
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
            + "           AND e.encounter_datetime BETWEEN :startDate AND :endDate  "
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
            + "           AND o.obs_datetime BETWEEN :startDate AND :endDate "
            + "           AND e.location_id = :location "
            + "        GROUP BY p.patient_id "
            + "     )min_vl "
            + " GROUP BY min_vl.patient_id) final_min ON final_min.patient_id = p.patient_id "
            + " WHERE p.patient_id NOT IN ( "
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
            + "                              AND e.encounter_datetime = final_min.final_min_date)"
            + " ) AS xquery ON xquery.patient_id=p.patient_id "
            + " WHERE p.voided = 0 "
            + "   AND e.voided = 0"
            + "   AND e.encounter_type = ${35} "
            + "   AND TIMESTAMPDIFF(DAY, xquery.final_min_date, e.encounter_datetime) BETWEEN 20 AND 33 "
            + "   AND e.location_id = :location "
            + " GROUP BY p.patient_id) AS second_apss on second_apss.patient_id= p.patient_id"
            + "  WHERE p.voided = 0 "
            + "                      AND e.voided = 0"
            + "                       AND e.encounter_type = ${35} "
            + "                        AND TIMESTAMPDIFF(DAY, second_apss.min_apss, e.encounter_datetime) BETWEEN 20 AND 33 "
            + "                        AND e.location_id = :location ";

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(sql));
    return sqlCohortDefinition;
  }

  public CohortDefinition getPedidoCargaViralBetweenLowerBoundAnduppperBoundXQuery(
      int lowerBound,
      int upperBound,
      EncounterType encounterType,
      int value,
      Concept concept,
      ValueType valueType,
      boolean additional) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("6332", hivMetadata.getBreastfeeding().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());
    map.put("value", value);
    map.put("encounterType", encounterType.getEncounterTypeId());
    map.put("question", concept.getConceptId());
    map.put("lowerBound", lowerBound);
    map.put("upperBound", upperBound);

    StringBuilder sql = new StringBuilder();
    sql.append(" SELECT p.patient_id ");
    sql.append(" FROM patient p ");
    sql.append("    INNER JOIN encounter e on p.patient_id = e.patient_id ");
    sql.append("    INNER JOIN obs o ON e.encounter_id = o.encounter_id ");
    if(additional){
      sql.append("    INNER JOIN obs o1 ON e.encounter_id = o1.encounter_id ");
    }
    sql.append("    INNER JOIN  (  ");
    sql.append(" SELECT p.patient_id, final_min.final_min_date ");
    sql.append(" FROM patient p ");
    sql.append(" INNER JOIN ( ");
    sql.append(" SELECT min_vl.patient_id , MIN(min_datetime) final_min_date ");
    sql.append(" FROM ( ");
    sql.append("         SELECT p.patient_id, MIN(e.encounter_datetime) AS min_datetime ");
    sql.append("         FROM patient p ");
    sql.append("                  INNER JOIN encounter e ON e.patient_id = p.patient_id ");
    sql.append("                  INNER JOIN obs o ON e.encounter_id = o.encounter_id ");
    sql.append("         WHERE p.voided = 0 ");
    sql.append("           AND o.voided = 0 ");
    sql.append("           AND e.voided = 0 ");
    sql.append("           AND e.encounter_type = ${6} ");
    sql.append("           AND o.concept_id = ${856} ");
    sql.append("           AND o.value_numeric >= 1000 ");
    sql.append("           AND e.encounter_datetime BETWEEN :startDate AND :endDate  ");
    sql.append("           AND e.location_id = :location");
    sql.append("         GROUP BY p.patient_id ");
    sql.append("         UNION ");
    sql.append("         SELECT p.patient_id, MIN(o.obs_datetime) AS min_datetime ");
    sql.append("         FROM patient p ");
    sql.append("                  INNER JOIN encounter e ON e.patient_id = p.patient_id ");
    sql.append("                  INNER JOIN obs o ON e.encounter_id = o.encounter_id ");
    sql.append("         WHERE p.voided = 0 ");
    sql.append("           AND o.voided = 0 ");
    sql.append("           AND e.voided = 0 ");
    sql.append("           AND e.encounter_type = ${53} ");
    sql.append("           AND o.concept_id = ${856} ");
    sql.append("           AND o.value_numeric >= 1000 ");
    sql.append("           AND o.obs_datetime BETWEEN :startDate AND :endDate ");
    sql.append("           AND e.location_id = :location ");
    sql.append("        GROUP BY p.patient_id ");
    sql.append("     )min_vl ");
    sql.append(" GROUP BY min_vl.patient_id) final_min ON final_min.patient_id = p.patient_id ");
    sql.append(" WHERE p.patient_id NOT IN ( ");
    sql.append("                            SELECT p.patient_id ");
    sql.append("                            FROM patient p ");
    sql.append(
        "                                     INNER JOIN encounter e ON e.patient_id = p.patient_id ");
    sql.append(
        "                                     INNER JOIN obs o ON e.encounter_id = o.encounter_id ");
    sql.append("                            WHERE p.voided = 0 ");
    sql.append("                              AND o.voided = 0 ");
    sql.append("                              AND e.voided = 0 ");
    sql.append("                              AND e.encounter_type = ${53} ");
    sql.append("                              AND o.concept_id IN (${1982},${6332}) ");
    sql.append("                              AND o.value_coded = ${1065} ");
    sql.append("                              AND o.obs_datetime = final_min.final_min_date ");
    sql.append("                            UNION ");
    sql.append("                            SELECT p.patient_id ");
    sql.append("                            FROM patient p ");
    sql.append(
        "                                     INNER JOIN encounter e ON e.patient_id = p.patient_id ");
    sql.append(
        "                                     INNER JOIN obs o ON e.encounter_id = o.encounter_id ");
    sql.append("                            WHERE p.voided = 0 ");
    sql.append("                              AND o.voided = 0 ");
    sql.append("                              AND e.voided = 0 ");
    sql.append("                              AND e.encounter_type = ${6} ");
    sql.append("                              AND o.concept_id IN (${1982},${6332}) ");
    sql.append("                              AND o.value_coded = ${1065} ");
    sql.append(
        "                              AND e.encounter_datetime = final_min.final_min_date)");
    sql.append(" ) AS xquery ON xquery.patient_id=p.patient_id ");
    sql.append(" WHERE p.voided = 0 ");
    sql.append("   AND e.voided = 0 ");
    if (additional){
      sql.append("   AND o1.voided = 0 ");
      sql.append("   AND o1.concept_id = ${1305} ");
      sql.append("   AND o1.value_coded IS NOT NULL ");
    }
    sql.append("   AND o.concept_id =  ${question}   ");
    if (valueType == ValueType.VALUE_CODED) {
      sql.append("   AND o.value_coded =  ${value}   ");
    }
    if (valueType == ValueType.VALUE_NUMERIC) {
      sql.append("   AND o.value_numeric >=  ${value}   ");
    }
    if (encounterType.equals(hivMetadata.getAdultoSeguimentoEncounterType())) {
      sql.append(
          "   AND e.encounter_datetime BETWEEN DATE_ADD(xquery.final_min_date, INTERVAL ${lowerBound} DAY) AND DATE_ADD(xquery.final_min_date, INTERVAL ${upperBound} DAY)");
    }
    if (encounterType.equals(hivMetadata.getMasterCardEncounterType())) {
      sql.append(
          "   AND o.obs_datetime BETWEEN DATE_ADD(xquery.final_min_date, INTERVAL ${lowerBound} DAY) AND DATE_ADD(xquery.final_min_date, INTERVAL ${upperBound} DAY)");
    }
    sql.append("   AND e.encounter_type = ${encounterType} ");
    sql.append("   AND e.location_id = :location ");

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(sql));
    return sqlCohortDefinition;
  }

  public CohortDefinition getDenominator10() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";

    compositionCohortDefinition.addSearch(
        "A", EptsReportUtils.map(this.getFirstAPSSInTheSameDayOfXQuery(), mappings));

    compositionCohortDefinition.addSearch(
        "B", EptsReportUtils.map(this.getAPSSInIn20To33DaysAfterXQuery(), mappings));

    compositionCohortDefinition.addSearch(
        "C", EptsReportUtils.map(this.getAPSSIn20To33DaysAfter2ndAPSSConsultation(), mappings));

    compositionCohortDefinition.addSearch(
        "D",
        EptsReportUtils.map(
            this.getPedidoCargaViralBetweenLowerBoundAnduppperBoundXQuery(
                80,
                130,
                hivMetadata.getAdultoSeguimentoEncounterType(),
                hivMetadata.getHivViralLoadConcept().getConceptId(),
                hivMetadata.getApplicationForLaboratoryResearch(),
                ValueType.VALUE_CODED,false),
            mappings));
    ;
    compositionCohortDefinition.addSearch(
        "transferredIn",
        EptsReportUtils.map(
            this.commonCohortQueries.getMohTransferredInPatients(),
            "onOrBefore=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "transferredOut",
        EptsReportUtils.map(
            this.commonCohortQueries.getMohTransferredOutPatientsByEndOfPeriod(),
            "onOrBefore=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "dead",
        EptsReportUtils.map(this.getDeadPatients(), "endDate=${endDate},location=${location}"));

    compositionCohortDefinition.setCompositionString(
        "(A AND B AND C AND D) AND NOT (transferredIn OR transferredOut OR dead) ");

    return compositionCohortDefinition;
  }

  public CohortDefinition getNumerator10() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    compositionCohortDefinition.addSearch(
        "denominator10",
        EptsReportUtils.map(
            getDenominator10(), "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "A",
        EptsReportUtils.map(
            this.getPedidoCargaViralBetweenLowerBoundAnduppperBoundXQuery(
                110,
                160,
                hivMetadata.getAdultoSeguimentoEncounterType(),
                    VIRAL_LOAD_1000_COPIES,
                hivMetadata.getHivViralLoadConcept(),
                ValueType.VALUE_CODED,false),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "B",
        EptsReportUtils.map(
            this.getPedidoCargaViralBetweenLowerBoundAnduppperBoundXQuery(
                110,
                160,
                hivMetadata.getMasterCardEncounterType(),
                    VIRAL_LOAD_1000_COPIES,
                hivMetadata.getHivViralLoadConcept(),
                ValueType.VALUE_CODED,false),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.setCompositionString("denominator10 AND A AND B");

    return compositionCohortDefinition;
  }


  public CohortDefinition getNumerator11() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    compositionCohortDefinition.addSearch(
            "A",
            EptsReportUtils.map(
                    this.getPedidoCargaViralBetweenLowerBoundAnduppperBoundXQuery(
                            110,
                            160,
                            hivMetadata.getAdultoSeguimentoEncounterType(),
                            VIRAL_LOAD_1000_COPIES,
                            hivMetadata.getHivViralLoadConcept(),
                            ValueType.VALUE_CODED,
                            true),
                    "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
            "B",
            EptsReportUtils.map(
                    this.getPedidoCargaViralBetweenLowerBoundAnduppperBoundXQuery(
                            110,
                            160,
                            hivMetadata.getMasterCardEncounterType(),
                            VIRAL_LOAD_1000_COPIES,
                            hivMetadata.getHivViralLoadConcept(),
                            ValueType.VALUE_CODED,false),
                    "startDate=${startDate},endDate=${endDate},location=${location}"));
    compositionCohortDefinition.setCompositionString("A AND B ");

    compositionCohortDefinition.addSearch(
            "D",
            EptsReportUtils.map(
                    this.getPedidoCargaViralBetweenLowerBoundAnduppperBoundXQuery(
                            80,
                            130,
                            hivMetadata.getAdultoSeguimentoEncounterType(),
                            hivMetadata.getHivViralLoadConcept().getConceptId(),
                            hivMetadata.getApplicationForLaboratoryResearch(),
                            ValueType.VALUE_CODED,false),
                    "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
            "denominator10",
            EptsReportUtils.map(
                    getDenominator10(), "startDate=${startDate},endDate=${endDate},location=${location}"));


    compositionCohortDefinition.setCompositionString("A AND B AND D AND denominator10 ");

    return compositionCohortDefinition;
  }

  public CohortDefinition getDenominator11() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    compositionCohortDefinition.addSearch(
            "denominator10",
            EptsReportUtils.map(
                    getDenominator10(), "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.setCompositionString("denominator10");

    return compositionCohortDefinition;
  }

  enum ValueType {
    VALUE_NUMERIC,
    VALUE_CODED
  }
}
