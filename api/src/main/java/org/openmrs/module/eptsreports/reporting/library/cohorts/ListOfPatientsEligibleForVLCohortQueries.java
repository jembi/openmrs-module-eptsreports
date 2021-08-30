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
    CohortDefinition chdX1 = getLastNextScheduledPickUpDate();
    CohortDefinition chdX2 = getLastNextScheduledPickUpDate();

    cd.addSearch(
        "txcurr", EptsReportUtils.map(txcurr, "onOrBefore=${endDate},location=${location}"));

    cd.addSearch("x1", EptsReportUtils.map(chdX1, "onOrBefore=${endDate},location=${location}"));

    cd.setCompositionString("txcurr AND x1");

    return cd;
  }

  public CohortDefinition getLastNextScheduledConsultationDate() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "All patients from Ficha Clinica with “Carga Viral” registered with numeric value > 1000 during the Inclusion period");
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
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("1410", hivMetadata.getReturnVisitDateConcept().getConceptId());

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

  public CohortDefinition getLastNextScheduledPickUpDateWithMostRecentDataLevantamento() {

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
    valuesMap.put("1410", hivMetadata.getReturnVisitDateConcept().getConceptId());

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
    valuesMap.put("1410", hivMetadata.getReturnVisitDateConcept().getConceptId());

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
    valuesMap.put("1410", hivMetadata.getReturnVisitDateConcept().getConceptId());

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
    valuesMap.put("1410", hivMetadata.getReturnVisitDateConcept().getConceptId());

    String query =
        "SELECT match_patient.patient_id FROM("
            + "SELECT most_recent.patient_id, MAX(most_recent.max_date) FROM("
            + "SELECT p.patient_id, MAX(o.obs_datetime) max_date FROM patient p"
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id    "
            + "WHERE e.encounter_type = ${53}"
            + "AND ((o.concept_id = ${856} AND o.value_numeric IS NULL) OR (o.concept_id = ${1305} AND o.value_coded IS NULL)) "
            + "AND o.obs_datetime <= :startDate"
            + "AND e.location_id = :location"
            + "AND e.voided = 0"
            + "AND p.voided = 0"
            + "AND o.voided = 0"
            + "GROUP BY p.patient_id"
            + ""
            + "UNION"
            + ""
            + "SELECT p.patient_id, MAX(e.encounter_datetime) max_date FROM patient p"
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "    WHERE e.encounter_type IN(${13},${6},${9},${51})"
            + "    AND e.encounter_datetime <= :startDate"
            + "    AND ((o.concept_id = ${856} AND o.value_numeric IS NULL ) OR (o.concept_id = ${1305} AND o.value_coded IS NULL) )"
            + "    AND e.location_id = :location"
            + "    AND e.voided = 0"
            + "    AND p.voided = 0"
            + "    AND o.voided = 0"
            + "GROUP BY patient_id) AS most_recent GROUP BY most_recent.patient_id"
            + ""
            + ") AS match_patient GROUP BY match_patient.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }
}
