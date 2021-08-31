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
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsEligibleForVLCohortQueries {

  private TxCurrCohortQueries txCurrCohortQueries;
  private HivMetadata hivMetadata;

  @Autowired
  public ListOfPatientsEligibleForVLCohortQueries(
      TxCurrCohortQueries txCurrCohortQueries, HivMetadata hivMetadata) {

    this.txCurrCohortQueries = txCurrCohortQueries;
    this.hivMetadata = hivMetadata;
  }

  public CohortDefinition getBaseCohort() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("base cohort");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition txcurr = this.txCurrCohortQueries.getTxCurrCompositionCohort("txcurr", true);
    CohortDefinition chdX1 = getLastNextScheduledConsultationDate();
    CohortDefinition chdX2 = getLastNextScheduledPickUpDate();
     CohortDefinition chdX3 = getLastNextScheduledPickUpDateWithMostRecentDataLevantamento();
    CohortDefinition chdVL1 = null;
    CohortDefinition chdVL2 = getPatientsWithVLLessThan1000() ;
    CohortDefinition chdVL3 = getPatientsWithMostRecentVLQuantitativeResult();
    CohortDefinition chdVL4 = null;
    CohortDefinition chdVL5 = getPatientsWithRecentVLIgualOrGreaterThan1000() ;
    CohortDefinition chdVL6 = null;
    CohortDefinition chdVL7 = getPatientsWhoDontHaveAnyViralLoad();
    //E1 - Exclude all pregnant
    //E2 Exclude all breastfeeding


    cd.addSearch(
        "txcurr", EptsReportUtils.map(txcurr, "onOrBefore=${endDate},location=${location}"));

    cd.addSearch("X1", EptsReportUtils.map(chdX1, "onOrBefore=${endDate},location=${location}"));
    cd.addSearch("X2", EptsReportUtils.map(chdX2, "onOrBefore=${endDate},location=${location}"));
    cd.addSearch("X3", EptsReportUtils.map(chdX3, "onOrBefore=${endDate},location=${location}"));
    cd.addSearch("VL1", EptsReportUtils.map(chdVL1, "onOrBefore=${endDate},location=${location}"));
    cd.addSearch("VL2", EptsReportUtils.map(chdVL2, "onOrBefore=${endDate},location=${location}"));
    cd.addSearch("VL3", EptsReportUtils.map(chdVL3, "onOrBefore=${endDate},location=${location}"));
    cd.addSearch("VL4", EptsReportUtils.map(chdVL4, "onOrBefore=${endDate},location=${location}"));
    cd.addSearch("VL5", EptsReportUtils.map(chdVL5, "onOrBefore=${endDate},location=${location}"));
    cd.addSearch("VL6", EptsReportUtils.map(chdVL6, "onOrBefore=${endDate},location=${location}"));
    cd.addSearch("VL7", EptsReportUtils.map(chdVL7, "onOrBefore=${endDate},location=${location}"));

    cd.setCompositionString(
        "txcurr AND (X1 OR X2 OR X3) AND VL1 (( (VL2 OR VL3) AND VL4) OR (VL5 AND VL6) OR VL7) AND NOT (E1 OR E2)");

    return cd;
  }
  // X1
  public CohortDefinition getLastNextScheduledConsultationDate() {

      SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("All patients with data da proxima consulta");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("1410", hivMetadata.getReturnVisitDateConcept().getConceptId());

    String query =
        "  SELECT p.patient_id"
            + "             FROM   patient p   "
            + "                     INNER JOIN encounter e   "
            + "                                     ON p.patient_id = e.patient_id   "
            + "                     INNER JOIN obs o   "
            + "                                     ON e.encounter_id = o.encounter_id   "
            + "     INNER JOIN  "
            + "       ( SELECT p.patient_id, MAX(e.encounter_datetime) as encounter_datetime  "
            + "      FROM  patient p   "
            + "       INNER JOIN encounter e  ON p.patient_id = e.patient_id  "
            + "      WHERE p.voided = 0  "
            + "       AND e.voided = 0   "
            + "       AND e.location_id = :location  "
            + "       AND e.encounter_datetime <= :endDate  "
            + "       AND e.encounter_type IN (${6}, ${9})  "
            + "      GROUP BY p.patient_id  "
            + "       )max_encounter ON p.patient_id=max_encounter.patient_id "
            + "             WHERE  p.voided = 0   "
            + "                     AND e.voided = 0   "
            + "                     AND o.voided = 0   "
            + "                     AND e.location_id = :location "
            + "                     AND e.encounter_type IN (${6}, ${9})  "
            + "                     AND o.concept_id = ${1410}   "
            + "                     AND e.encounter_datetime <= :endDate  "
            + "                     AND max_encounter.encounter_datetime = e.encounter_datetime  "
            + "             GROUP BY p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }
  // X2
  public CohortDefinition getLastNextScheduledPickUpDate() {

      SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
      sqlCohortDefinition.setName(
        "All patients with Data do próximo levantamento from most recent FILA");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    valuesMap.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());

    String query =
        " SELECT p.patient_id"
            + "FROM   patient p"
            + "       INNER JOIN encounter e"
            + "               ON e.patient_id = p.patient_id"
            + "       INNER JOIN obs o"
            + "               ON o.encounter_id = e.encounter_id"
            + "       INNER JOIN (SELECT p.patient_id,"
            + "                          e.encounter_id,"
            + "                          Max(e.encounter_datetime) most_recent"
            + "                   FROM   patient p"
            + "                          INNER JOIN encounter e"
            + "                                  ON p.patient_id = e.patient_id"
            + "                   WHERE  e.encounter_type = 18"
            + "                          AND e.encounter_datetime <= :endDate"
            + "                          AND e.location_id = :location"
            + "                          AND e.voided = 0"
            + "                          AND p.voided = 0"
            + "                   GROUP  BY p.patient_id) last_scheduled"
            + "               ON last_scheduled.patient_id = p.patient_id"
            + "WHERE  last_scheduled.most_recent = e.encounter_datetime"
            + "       AND e.voided = 0"
            + "       AND e.location_id = :location"
            + "       AND e.encounter_type = ${18}"
            + "       AND p.voided = 0"
            + "       AND o.voided = 0"
            + "       AND o.concept_id = ${5096}"
            + "       AND o.value_datetime BETWEEN :startDate AND :endDate"
            + "GROUP  BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  // X3
  public CohortDefinition getLastNextScheduledPickUpDateWithMostRecentDataLevantamento() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("All patients with most recent Data de Levantamento");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    valuesMap.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    valuesMap.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    valuesMap.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());

    String query =
        " SELECT p.patient_id FROM patient p"
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "INNER JOIN"
            + "(SELECT p.patient_id, MAX(o.value_datetime) AS last_obs, o.obs_datetime"
            + "FROM encounter e INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "INNER JOIN patient p ON p.patient_id = e.patient_id"
            + "WHERE e.encounter_type = ${52}"
            + "AND o.concept_id = ${23866}"
            + "AND e.voided = 0"
            + "AND e.location_id = :location"
            + "        AND e.voided = 0 "
            + "        AND p.voided = 0"
            + "        AND o.voided = 0"
            + "GROUP BY p.patient_id) most_recent"
            + "ON most_recent.patient_id = e.patient_id"
            + "WHERE p.voided = 0"
            + "  AND e.encounter_type = ${52}"
            + "  AND e.location_id = :location"
            + "  AND o.concept_id = ${23865}"
            + "  AND o.value_coded = ${1065}"
            + "  AND o.obs_datetime = most_recent.obs_datetime"
            + "  AND o.voided = 0"
            + "  AND e.voided = 0"
            + "  AND p.voided = 0"
            + "  AND DATE_ADD(most_recent.last_obs, INTERVAL 30 DAY) BETWEEN :startDate AND :endDate"
            + "  AND most_recent.last_obs <= :endDate"
            + "GROUP BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  // VL2
  public CohortDefinition getPatientsWithVLLessThan1000() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("All patients with most recent Data de Levantamento");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String query =
        "     SELECT patient_id FROM("
            + "SELECT most_recent.patient_id, MAX(recent_datetime) FROM("
            + "SELECT p.patient_id, MAX(e.encounter_datetime) recent_datetime"
            + "FROM patient p"
            + "                INNER JOIN encounter e ON p.patient_id = e.patient_id"
            + "                INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "                WHERE e.encounter_type IN (${13},${6},${9},${51})"
            + "                AND e.encounter_datetime <= :startDate"
            + "                AND o.concept_id = ${856}"
            + "                AND o.value_numeric < ${1000}"
            + "                AND e.location_id = :location"
            + "                AND e.voided = 0"
            + "                AND o.voided = 0"
            + "                AND p.voided = 0"
            + "                GROUP BY p.patient_id"
            + "                "
            + "                UNION"
            + "                "
            + "               SELECT p.patient_id, MAX(o.obs_datetime) recent_datetime"
            + "                FROM patient p "
            + "                INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "                INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "                WHERE e.encounter_type = ${53}"
            + "                AND o.concept_id = ${856}"
            + "                AND o.obs_datetime <= :startDate"
            + "                AND e.location_id = :location"
            + "                AND e.voided = 0"
            + "                AND p.voided = 0"
            + "                AND o.voided = 0   "
            + "                GROUP BY p.patient_id"
            + "                "
            + ") AS most_recent GROUP BY most_recent.patient_id"
            + "            "
            + "            "
            + ") AS recent_vl"
            + "    GROUP BY recent_vl.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  // VL3
  public CohortDefinition getPatientsWithMostRecentVLQuantitativeResult() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("All patients with most recent Data de Levantamento");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        " SELECT max.patient_id FROM ("
            + "    SELECT max_val_result.patient_id, MAX(max_val_result.max_vl) FROM ("
            + "        SELECT  p.patient_id, MAX(e.encounter_datetime) AS max_vl FROM patient p"
            + "            INNER JOIN encounter e on p.patient_id = e.patient_id"
            + "            INNER JOIN obs o ON e.encounter_id = o.encounter_id"
            + "        WHERE p.voided =0"
            + "            AND e.voided =0"
            + "            AND o.voided = 0"
            + "            AND e.location_id = :location"
            + "            AND o.concept_id = ${1305}"
            + "            AND e.encounter_type IN (${13},${6},${9},${51})"
            + "            AND e.encounter_datetime <= :startDate"
            + "            AND o.value_coded IS NOT NULL"
            + "        GROUP BY p.patient_id"
            + "        UNION"
            + "        SELECT  p.patient_id, MAX(o.obs_datetime) AS max_vl FROM patient p"
            + "            INNER JOIN encounter e on p.patient_id = e.patient_id"
            + "            INNER JOIN obs o ON e.encounter_id = o.encounter_id"
            + "        WHERE p.voided =0"
            + "            AND e.voided =0"
            + "            AND e.location_id = :location"
            + "            AND o.voided = 0"
            + "            AND o.concept_id = ${1305}"
            + "            AND e.encounter_type = ${53}"
            + "            AND o.obs_datetime <=  :startDate"
            + "            AND o.value_coded IS NOT NULL"
            + "        GROUP BY p.patient_id) AS max_val_result GROUP BY max_val_result.patient_id) AS max";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  // VL5
  public CohortDefinition getPatientsWithRecentVLIgualOrGreaterThan1000() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("All patients with most recent Data de Levantamento");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String query =
        "SELECT patient_id  FROM("
            + "SELECT patient_id, MAX(recent_date) FROM("
            + "SELECT p.patient_id, MAX(o.obs_datetime) recent_date FROM patient p"
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "        INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "WHERE e.encounter_type = ${53}"
            + "AND o.concept_id = ${856}"
            + "AND o.value_numeric >= ${1000}"
            + "AND o.obs_datetime <= :startDate"
            + "AND e.location_id = :location"
            + "AND e.voided = 0"
            + "AND p.voided = 0"
            + "AND o.voided = 0"
            + "GROUP BY patient_id"
            + ""
            + "UNION"
            + ""
            + "SELECT p.patient_id, MAX(e.encounter_datetime) recent_date FROM patient p"
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "WHERE e.encounter_type IN(${13}, ${6},${9}, ${51})"
            + "AND o.concept_id = ${856}"
            + "AND o.value_numeric >= ${1000}"
            + "AND e.encounter_datetime <= :startDate"
            + "AND e.location_id = :location"
            + "AND e.voided = 0"
            + "AND p.voided = 0"
            + "AND o.voided = 0"
            + "GROUP BY p.patient_id"
            + ") AS most_recent_vl "
            + "GROUP BY patient_id"
            + ")patients_vl5";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  // VL7
  public CohortDefinition getPatientsWhoDontHaveAnyViralLoad() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("All patients with most recent Data de Levantamento");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        " SELECT match_patient.patient_id FROM("
            + "             SELECT most_recent.patient_id, MAX(most_recent.max_date) FROM("
            + "             SELECT p.patient_id, MAX(o.obs_datetime) max_date FROM patient p"
            + "             INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "                 INNER JOIN obs o ON o.encounter_id = e.encounter_id    "
            + "             WHERE e.encounter_type = ${53}"
            + "             AND ((o.concept_id = ${856} AND o.value_numeric IS NOT NULL) OR (o.concept_id = ${1305} AND o.value_coded IS NOT NULL)) "
            + "             AND o.obs_datetime <= :startDate"
            + "             AND e.location_id = :location"
            + "             AND e.voided = 0"
            + "             AND p.voided = 0"
            + "             AND o.voided = 0"
            + "             GROUP BY p.patient_id"
            + "             "
            + "             UNION"
            + "             "
            + "             SELECT p.patient_id, MAX(e.encounter_datetime) max_date FROM patient p"
            + "             INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "                 INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "                 WHERE e.encounter_type IN(${13},${6},${9},${51})"
            + "                 AND e.encounter_datetime <= :startDate"
            + "                 AND ((o.concept_id = ${856} AND o.value_numeric IS NOT NULL ) OR (o.concept_id = ${1305} AND o.value_coded IS NOT NULL) )"
            + "                 AND e.location_id = :location"
            + "                 AND e.voided = 0"
            + "                 AND p.voided = 0"
            + "                 AND o.voided = 0"
            + "             GROUP BY patient_id) AS most_recent GROUP BY most_recent.patient_id"
            + "             "
            + "             ) AS match_patient GROUP BY match_patient.patient_id"
            + "        ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  // 10
  public CohortDefinition getPatientsAndMostRecentVLResultDate() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("All patients with most recent Data de Levantamento");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        "SELECT result_date.patient_id, MAX(result_date.most_recent) FROM ("
            + "SELECT p.patient_id, MAX(o.obs_datetime) most_recent FROM patient p"
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "        INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "WHERE e.encounter_type = ${53}"
            + "AND ((o.concept_id = ${856} AND o.value_numeric > 0) OR (o.concept_id = ${1305} AND o.value_coded IS NOT NULL))"
            + "AND o.obs_datetime <= :startDate"
            + "AND e.location_id = :location"
            + "AND e.voided = 0"
            + "AND p.voided = 0"
            + "AND o.voided = 0"
            + "GROUP BY p.patient_id"
            + ""
            + "UNION"
            + ""
            + "SELECT p.patient_id, MAX(e.encounter_datetime) most_recent FROM patient p"
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "        INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "WHERE e.encounter_type IN(${6},${9},${13},${51})"
            + "AND ((o.concept_id = ${856} AND o.value_numeric > 0) OR (o.concept_id = ${1305} AND o.value_coded IS NOT NULL))"
            + "AND e.encounter_datetime <= :startDate"
            + "AND e.location_id = :location"
            + "AND e.voided = 0"
            + "AND p.voided = 0"
            + "AND o.voided = 0"
            + "GROUP BY p.patient_id"
            + ") AS result_date GROUP BY result_date.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  // 11
  public CohortDefinition getPatientsAndMostRecentViralLoad() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("All patients with most recent Data de Levantamento");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        " SELECT recent_vl.patient_id, recent_vl.viral_load FROM("
            + "SELECT p.patient_id, MAX(o.obs_datetime) most_recent, o.value_numeric viral_load FROM patient p"
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "        INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "WHERE e.encounter_type = ${53}"
            + "AND ((o.concept_id = ${856} AND o.value_numeric > 0) OR (o.concept_id = ${1305} AND o.value_coded IS NOT NULL))"
            + "AND o.obs_datetime <= :startDate"
            + "AND e.location_id = :location"
            + "AND e.voided = 0"
            + "AND p.voided = 0"
            + "AND o.voided = 0"
            + "GROUP BY p.patient_id"
            + "UNION"
            + "SELECT p.patient_id, MAX(e.encounter_datetime), o.value_numeric viral_load FROM patient p"
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "        INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "WHERE e.encounter_type IN(${6},${9},${13},${51})"
            + "AND ((o.concept_id = ${856} AND o.value_numeric > 0) OR (o.concept_id = ${1305} AND o.value_coded IS NOT NULL))"
            + "AND e.encounter_datetime <= :startDate"
            + "AND e.location_id = :location"
            + "AND e.voided = 0"
            + "AND p.voided = 0"
            + "AND o.voided = 0"
            + "GROUP BY p.patient_id"
            + ") AS recent_vl GROUP BY recent_vl.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getPatientsAndLastFollowUpConsultationDate() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("All patients with most recent Data de Levantamento");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());

    String query =
        " SELECT p.patient_id, MAX(e.encounter_datetime) FROM patient p"
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "        INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "WHERE e.encounter_type IN(${6},${9})"
            + "AND e.encounter_datetime <= :startDate"
            + "AND e.location_id = :location"
            + "AND e.voided = 0"
            + "AND p.voided = 0"
            + "AND o.voided = 0"
            + ""
            + "GROUP BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  // 13
  public CohortDefinition getPatientsAndLastFollowUpNextConsultationDate() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("All patients with most recent Data de Levantamento");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("1410", hivMetadata.getReturnVisitDateConcept().getConceptId());

    String query =
        " SELECT p.patient_id, MAX(o.value_datetime) FROM patient p"
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "        INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "WHERE e.encounter_type IN(${6},${9})"
            + "AND o.concept_id = ${1410}"
            + "AND e.encounter_datetime <= :startDate"
            + "AND e.voided = 0"
            + "AND e.location_id = :location"
            + "AND p.voided = 0"
            + "AND o.voided = 0"
            + "GROUP BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  // 14
  public CohortDefinition getPatientsAndLastDrugPickUpDateOnFila() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("All patients with most recent Data de Levantamento");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());

    String query =
        "SELECT p.patient_id, MAX(e.encounter_datetime) FROM patient p"
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id   "
            + "WHERE e.encounter_type = ${18}"
            + "AND e.encounter_datetime <= :startDate"
            + "AND e.voided = 0"
            + "AND e.location_id = :location"
            + "AND p.voided = 0"
            + "GROUP BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }
  // 15
  public CohortDefinition getPatientsAndLastDrugPickUpDateOnFichaMestre() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("All patients with most recent Data de Levantamento");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    valuesMap.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    valuesMap.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    valuesMap.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());

    String query =
        "SELECT last_ficha.patient_id, last_ficha.max_date FROM("
            + "SELECT p.patient_id, MAX(e.encounter_datetime) encounter_date, MAX(obs_value.value_datetime) max_date  FROM patient p"
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "        INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "        INNER JOIN obs obs_value ON obs_value.encounter_id = e.encounter_id "
            + "        AND o.person_id = obs_value.person_id"
            + "WHERE e.encounter_type = ${52}"
            + "AND o.concept_id = ${23865}"
            + "AND o.value_coded = ${1065}"
            + "AND obs_value.concept_id = ${23866}"
            + "AND obs_value.value_datetime <= :startDate"
            + "AND e.voided = 0"
            + "AND e.location_id = :location"
            + "AND p.voided = 0"
            + "AND o.voided = 0"
            + "GROUP BY p.patient_id) AS last_ficha GROUP BY last_ficha.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  // 16
  public CohortDefinition getPatientsAndNextDrugPickUpDateOnFila() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("All patients with most recent Data de Levantamento");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    valuesMap.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());

    String query =
        "SELECT next_pickup.patient_id, next_pickup.next_date FROM ("
            + "SELECT p.patient_id, MAX(e.encounter_datetime), o.value_datetime AS next_date FROM patient p"
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "        INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "WHERE e.encounter_type = ${18}"
            + "AND o.concept_id = ${5096}"
            + "AND e.encounter_datetime <= :startDate"
            + "AND e.voided = 0"
            + "AND e.location_id = :location"
            + "AND p.voided = 0"
            + "AND o.voided = 0"
            + "GROUP BY p.patient_id) next_pickup "
            + "GROUP BY next_pickup.patient_id;";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getPatientsAndLastDrugPickUpOnFila() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("All patients with most recent Data de Levantamento");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    valuesMap.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    valuesMap.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    valuesMap.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());

    String query =
        "SELECT last_ficha.patient_id, last_ficha.max_date FROM("
            + "SELECT p.patient_id, MAX(e.encounter_datetime) encounter_date, MAX(obs_value.value_datetime) max_date  FROM patient p"
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "        INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "        INNER JOIN obs obs_value ON obs_value.encounter_id = e.encounter_id "
            + "        AND o.person_id = obs_value.person_id"
            + "WHERE e.encounter_type = ${52}"
            + "AND o.concept_id = ${23865}"
            + "AND o.value_coded = ${1065}"
            + "AND obs_value.concept_id = ${23866}"
            + "AND obs_value.value_datetime <= :startDate"
            + "AND e.voided = 0"
            + "AND e.location_id = :location"
            + "AND p.voided = 0"
            + "AND o.voided = 0"
            + "GROUP BY p.patient_id) AS last_ficha GROUP BY last_ficha.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  // 17
  public CohortDefinition getPatientsAndNextpickUpDateOnFichaMestre() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("All patients with most recent Data de Levantamento");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    valuesMap.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    valuesMap.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    valuesMap.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());

    String query =
        "SELECT most_recent.patient_id, most_recent.return_date FROM ("
            + "SELECT p.patient_id, MAX(e.encounter_datetime), DATE_ADD(obs_value.value_datetime, INTERVAL 30 DAY) return_date FROM patient p"
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "        INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "        INNER JOIN obs obs_value ON obs_value.encounter_id = e.encounter_id AND obs_value.person_id = o.person_id"
            + "WHERE e.encounter_type = ${52}"
            + "AND o.concept_id = ${23865}"
            + "AND o.value_coded = ${1065}"
            + "AND obs_value.concept_id = ${23866}"
            + "AND obs_value.value_datetime <= :startDate"
            + "AND e.voided = 0"
            + "AND e.location_id = :location"
            + "AND p.voided = 0"
            + "AND o.voided = 0"
            + "GROUP BY p.patient_id)"
            + "AS most_recent"
            + "GROUP BY most_recent.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getPatientsAndNumberOfAPSSAndPPAfterHadVLGreaterThan1000() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("All patients with most recent Data de Levantamento");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String query =
        "SELECT "
            + "    p.patient_id, COUNT(e.encounter_id)"
            + "FROM"
            + "    patient p"
            + "        INNER JOIN"
            + "    encounter e ON e.patient_id = p.patient_id"
            + "        INNER JOIN"
            + "    (SELECT "
            + "        p.patient_id, MAX(o.obs_datetime) recent_date"
            + "    FROM"
            + "        patient p"
            + "    INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "    WHERE"
            + "        e.encounter_type = ${53}"
            + "            AND o.concept_id = ${856}"
            + "            AND o.value_numeric >= 1000"
            + "            AND o.obs_datetime <= :startDate"
            + "            AND e.location_id = :location"
            + "            AND e.voided = 0"
            + "            AND p.voided = 0"
            + "            AND o.voided = 0"
            + "    GROUP BY patient_id UNION SELECT "
            + "        p.patient_id, MAX(e.encounter_datetime) recent_date"
            + "    FROM"
            + "        patient p"
            + "    INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "    WHERE"
            + "        e.encounter_type IN (${13} , ${6}, ${9}, ${51})"
            + "            AND o.concept_id = ${856}"
            + "            AND o.value_numeric >= 1000"
            + "            AND e.encounter_datetime <= :startDate"
            + "            AND e.location_id = :location"
            + "            AND e.voided = 0"
            + "            AND p.voided = 0"
            + "            AND o.voided = 0"
            + "    GROUP BY p.patient_id) AS most_recent_vl ON most_recent_vl.patient_id = p.patient_id"
            + "WHERE"
            + "    e.encounter_type = ${35}"
            + "        AND e.encounter_datetime >= most_recent_vl.recent_date"
            + "        AND e.encounter_datetime <= :startDate"
            + "        AND e.location_id = :location"
            + "        AND e.voided = 0"
            + "        AND p.voided = 0"
            + "GROUP BY patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }
}
