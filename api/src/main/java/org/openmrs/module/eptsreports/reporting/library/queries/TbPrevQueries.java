package org.openmrs.module.eptsreports.reporting.library.queries;

import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.utils.EptsQueriesUtil;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TbPrevQueries {

  @Autowired private CommonQueries commonQueries;

  public static String getRegimeTPTOrOutrasPrescricoes(
      EncounterType encounterType, Concept question, List<Concept> answers, Integer boundary) {

    List<Integer> answerIds = new ArrayList<>();

    for (Concept concept : answers) {
      answerIds.add(concept.getConceptId());
    }

    String query =
        " SELECT distinct p.patient_id "
            + " FROM  patient p  "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id  "
            + " INNER JOIN obs o ON o.encounter_id = e.encounter_id  "
            + " INNER JOIN (SELECT  p.patient_id, MIN(e.encounter_datetime) first_pickup_date "
            + "             FROM    patient p  "
            + "             INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "             INNER JOIN obs o ON o.encounter_id = e.encounter_id  "
            + "             WHERE   p.voided = 0  "
            + "                 AND e.voided = 0  "
            + "                 AND o.voided = 0  "
            + "                 AND e.location_id = :location "
            + "                 AND e.encounter_type = ${encounterType} "
            + "                 AND o.concept_id = ${question} "
            + "                 AND o.value_coded IN (${answers}) "
            + "                 AND e.encounter_datetime >= :startDate "
            + "                 AND e.encounter_datetime <= :endDate "
            + "             GROUP BY p.patient_id) AS inh  on inh.patient_id = p.patient_id "
            + " WHERE p.voided = 0 "
            + "    and e.voided = 0 "
            + "    and o.voided = 0 "
            + "    and p.patient_id NOT IN ( SELECT patient_id  "
            + "                             FROM patient p "
            + "                             WHERE 	 p.voided = 0  "
            + "                                  AND e.voided = 0  "
            + "                                  AND o.voided = 0  "
            + "                                  AND e.location_id = :location "
            + "                                  AND e.encounter_type = ${encounterType} "
            + "                                  AND o.concept_id = ${question} "
            + "                                  AND o.value_coded IN (${answers}) "
            + "                                  AND e.encounter_datetime >= DATE_SUB(inh.first_pickup_date, INTERVAL "
            + boundary
            + " MONTH)  "
            + "                                  AND e.encounter_datetime < inh.first_pickup_date) ";

    Map<String, String> map = new HashMap<>();
    map.put("encounterType", String.valueOf(encounterType.getEncounterTypeId()));
    map.put("question", String.valueOf(question.getConceptId()));
    map.put("answers", StringUtils.join(answerIds, ","));

    StringSubstitutor sb = new StringSubstitutor(map);

    return sb.replace(query);
  }

  public String get3HPStartOnFichaResumo() {
    return "SELECT p.patient_id, o2.obs_datetime AS start_date "
        + "FROM   patient p "
        + "       INNER JOIN encounter e ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o ON e.encounter_id = o.encounter_id "
        + "       INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id "
        + "WHERE  p.voided = 0 AND e.voided = 0 AND o.voided = 0 AND o2.voided = 0"
        + "       AND e.location_id = :location "
        + "       AND e.encounter_type = 53 "
        + "       AND ( (o.concept_id = 23985 AND o.value_coded = 23954) "
        + "        AND (o2.concept_id = 165308 AND o2.value_coded = 1256 "
        + "        AND o2.obs_datetime BETWEEN DATE_SUB(:startDate, INTERVAL 6 MONTH) AND DATE_SUB(:endDate, INTERVAL 6 MONTH) ) )";
  }

  public String getStartDateOf3HPOnFichaClinica() {
    return "SELECT p.patient_id, o2.obs_datetime AS start_date "
        + "FROM   patient p "
        + "       INNER JOIN encounter e ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o ON e.encounter_id = o.encounter_id "
        + "       INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id "
        + "WHERE  p.voided = 0 AND e.voided = 0 AND o.voided = 0 AND o2.voided = 0 "
        + "       AND e.location_id = :location "
        + "       AND e.encounter_type = 6"
        + "       AND (o.concept_id = 23985 AND o.value_coded = 23954)  "
        + "       AND (o2.concept_id = 165308 AND o2.value_coded = 1256 "
        + "       AND o2.obs_datetime BETWEEN DATE_SUB(:startDate, INTERVAL 6 MONTH) AND DATE_SUB(:endDate, INTERVAL 6 MONTH) ) ";
  }

  public String getStartDateOfDT3HPOnFichaClinica() {
    return "SELECT p.patient_id, e.encounter_datetime AS start_date "
        + "FROM   patient p "
        + "       INNER JOIN encounter e ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o ON e.encounter_id = o.encounter_id "
        + "WHERE  p.voided = 0 "
        + "       AND e.voided = 0 "
        + "       AND o.voided = 0 "
        + "       AND e.location_id = :location "
        + "       AND e.encounter_type = 6 "
        + "       AND o.concept_id = 1719 "
        + "       AND o.value_coded = 165307 "
        + "       AND e.encounter_datetime BETWEEN DATE_SUB(:startDate, INTERVAL 6 MONTH) AND DATE_SUB(:endDate, INTERVAL 6 MONTH) ";
  }

  public String getStartDateOf3hpPiridoxinaOnFilt() {

    return "SELECT filt.patient_id, filt_3hp_start_date AS start_date "
        + "FROM  (SELECT p.patient_id, "
        + "              Min(o2.obs_datetime) filt_3hp_start_date "
        + "       FROM   patient p "
        + "              INNER JOIN encounter e "
        + "                      ON p.patient_id = e.patient_id "
        + "              INNER JOIN obs o "
        + "                      ON e.encounter_id = o.encounter_id "
        + "              INNER JOIN obs o2 "
        + "                      ON e.encounter_id = o2.encounter_id "
        + "       WHERE  p.voided = 0 "
        + "              AND e.voided = 0 "
        + "              AND o.voided = 0 "
        + "              AND o2.voided = 0 "
        + "              AND e.location_id = :location "
        + "              AND e.encounter_type = 60 "
        + "              AND ( ( o.concept_id = 23985 "
        + "                      AND o.value_coded IN ( 23954, 23984 ) ) "
        + "                    AND ( o2.concept_id = 23987 "
        + "                          AND ( o2.value_coded IN ( 1257, 1267 ) "
        + "                                 OR o2.value_coded IS NULL ) "
        + "                          AND o2.obs_datetime BETWEEN DATE_SUB(:startDate, INTERVAL 6 MONTH) AND DATE_SUB(:endDate, INTERVAL 6 MONTH) ) ) "
        + "       GROUP  BY p.patient_id) filt "
        + "WHERE  NOT EXISTS (SELECT ee.encounter_id "
        + "                   FROM   encounter ee "
        + "                          INNER JOIN obs o "
        + "                                  ON ee.encounter_id = o.encounter_id "
        + "                          INNER JOIN obs o2 "
        + "                                  ON ee.encounter_id = o2.encounter_id "
        + "                   WHERE  ee.voided = 0 "
        + "                          AND o.voided = 0 "
        + "                          AND ee.patient_id = filt.patient_id "
        + "                          AND ee.encounter_type = 60 "
        + "                          AND ee.location_id = :location "
        + "                          AND ( ( o.concept_id = 23985 "
        + "                                  AND o.value_coded IN ( 23954, 23984 ) ) "
        + "                                AND ( o2.concept_id = 23987 "
        + "                                      AND ( o2.value_coded IN ( 1256, 1705 ) "
        + "                                             OR o2.value_coded IS NULL ) "
        + "                                      AND o2.obs_datetime >= "
        + "                                          Date_sub(filt.filt_3hp_start_date, "
        + "                                          INTERVAL 4 month "
        + "                                          ) "
        + "                                      AND o2.obs_datetime <= "
        + "                                          filt.filt_3hp_start_date ) "
        + "                              )) "
        + "       AND NOT EXISTS (SELECT ee.encounter_id "
        + "                       FROM   encounter ee "
        + "                              INNER JOIN obs o "
        + "                                      ON ee.encounter_id = o.encounter_id "
        + "                              INNER JOIN obs o2 "
        + "                                      ON ee.encounter_id = o2.encounter_id "
        + "                       WHERE  ee.voided = 0 "
        + "                              AND o.voided = 0 "
        + "                              AND ee.encounter_type = 6 "
        + "                              AND ee.location_id = :location "
        + "                              AND ee.patient_id = filt.patient_id "
        + "                              AND o.concept_id = 1719 "
        + "                              AND o.value_coded = 165307 "
        + "                              AND ee.encounter_datetime >= "
        + "                                  Date_sub(filt.filt_3hp_start_date, "
        + "                                  INTERVAL "
        + "                                  4 month "
        + "                                  ) "
        + "                              AND ee.encounter_datetime <= "
        + "                                  filt.filt_3hp_start_date) "
        + "       AND NOT EXISTS (SELECT ee.encounter_id "
        + "                       FROM   encounter ee "
        + "                              INNER JOIN obs o "
        + "                                      ON ee.encounter_id = o.encounter_id "
        + "                              INNER JOIN obs oo "
        + "                                      ON ee.encounter_id = oo.encounter_id "
        + "                       WHERE  ee.voided = 0 "
        + "                              AND o.voided = 0 "
        + "                              AND ee.encounter_type = 53 "
        + "                              AND ee.location_id = :location "
        + "                              AND ee.patient_id = filt.patient_id "
        + "                              AND o.concept_id = 23985 "
        + "                              AND o.value_coded = 23954 "
        + "                              AND oo.concept_id = 165308 "
        + "                              AND oo.value_coded = 1256 "
        + "                              AND oo.obs_datetime >= "
        + "                                  Date_sub(filt.filt_3hp_start_date, "
        + "                                  INTERVAL 4 month) "
        + "                              AND oo.obs_datetime <= filt.filt_3hp_start_date) ";
  }

  public String getINHStartDate() {
    return "SELECT p.patient_id, o2.obs_datetime AS start_date "
        + "FROM   patient p "
        + "       INNER JOIN encounter e ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o ON e.encounter_id = o.encounter_id "
        + "       INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id "
        + "WHERE  p.voided = 0 AND e.voided = 0 AND o.voided = 0 AND o2.voided = 0"
        + "       AND e.location_id = :location "
        + "       AND e.encounter_type = 53 "
        + "       AND ( (o.concept_id = 23985 AND o.value_coded = 656) "
        + "        AND (o2.concept_id = 165308 AND o2.value_coded = 1256 "
        + "        AND o2.obs_datetime BETWEEN DATE_SUB(:startDate, INTERVAL 6 MONTH) AND DATE_SUB(:endDate, INTERVAL 6 MONTH) ) )";
  }

  public String getStartDateINHOnFichaClinica() {
    return "SELECT p.patient_id, o2.obs_datetime AS start_date "
        + "FROM   patient p "
        + "       INNER JOIN encounter e ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o ON e.encounter_id = o.encounter_id "
        + "       INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id "
        + "WHERE  p.voided = 0 AND e.voided = 0 AND o.voided = 0 AND o2.voided = 0 "
        + "       AND e.location_id = :location "
        + "       AND e.encounter_type IN (6,9) "
        + "       AND (o.concept_id = 23985 AND o.value_coded = 656)  "
        + "       AND (o2.concept_id = 165308 AND o2.value_coded = 1256 "
        + "       AND o2.obs_datetime BETWEEN DATE_SUB(:startDate, INTERVAL 6 MONTH) AND DATE_SUB(:endDate, INTERVAL 6 MONTH) ) ";
  }

  public String getStartDateOfINHOnFilt() {

    return "SELECT filt.patient_id, filt_3hp_start_date AS start_date "
        + "FROM  (SELECT p.patient_id, "
        + "              Min(o2.obs_datetime) filt_3hp_start_date "
        + "       FROM   patient p "
        + "              INNER JOIN encounter e "
        + "                      ON p.patient_id = e.patient_id "
        + "              INNER JOIN obs o "
        + "                      ON e.encounter_id = o.encounter_id "
        + "              INNER JOIN obs o2 "
        + "                      ON e.encounter_id = o2.encounter_id "
        + "       WHERE  p.voided = 0 "
        + "              AND e.voided = 0 "
        + "              AND o.voided = 0 "
        + "              AND o2.voided = 0 "
        + "              AND e.location_id = :location "
        + "              AND e.encounter_type = 60 "
        + "              AND ( ( o.concept_id = 23985 "
        + "                      AND o.value_coded IN ( 656, 23982 ) ) "
        + "                    AND ( o2.concept_id = 23987 "
        + "                          AND ( o2.value_coded IN ( 1257 ) "
        + "                                 OR o2.value_coded IS NULL ) "
        + "                          AND o2.obs_datetime BETWEEN DATE_SUB(:startDate, INTERVAL 6 MONTH) AND DATE_SUB(:endDate, INTERVAL 6 MONTH) ) ) "
        + "       GROUP  BY p.patient_id) filt "
        + "WHERE NOT EXISTS (SELECT ee.encounter_id "
        + "                       FROM   encounter ee "
        + "                              INNER JOIN obs o "
        + "                                      ON ee.encounter_id = o.encounter_id "
        + "                              INNER JOIN obs o2 "
        + "                                      ON ee.encounter_id = o2.encounter_id "
        + "                       WHERE  ee.voided = 0 "
        + "                              AND o.voided = 0 "
        + "                              AND ee.encounter_type IN (6,9) "
        + "                              AND ee.location_id = :location "
        + "                              AND ee.patient_id = filt.patient_id "
        + "                              AND o.concept_id = 23985 "
        + "                              AND o.value_coded = 656 "
        + "                              AND o2.concept_id = 165308 "
        + "                              AND o2.value_coded = 1256 "
        + "                              AND ee.encounter_datetime >= "
        + "                                  Date_sub(filt.filt_3hp_start_date, "
        + "                                  INTERVAL "
        + "                                  7 month "
        + "                                  ) "
        + "                              AND ee.encounter_datetime <= "
        + "                                  filt.filt_3hp_start_date) "
        + "       AND NOT EXISTS (SELECT ee.encounter_id "
        + "                       FROM   encounter ee "
        + "                              INNER JOIN obs o "
        + "                                      ON ee.encounter_id = o.encounter_id "
        + "                              INNER JOIN obs oo "
        + "                                      ON ee.encounter_id = oo.encounter_id "
        + "                       WHERE  ee.voided = 0 "
        + "                              AND o.voided = 0 "
        + "                              AND ee.encounter_type = 53 "
        + "                              AND ee.location_id = :location "
        + "                              AND ee.patient_id = filt.patient_id "
        + "                              AND o.concept_id = 23985 "
        + "                              AND o.value_coded = 656 "
        + "                              AND oo.concept_id = 165308 "
        + "                              AND oo.value_coded = 1256 "
        + "                              AND oo.obs_datetime >= "
        + "                                  Date_sub(filt.filt_3hp_start_date, "
        + "                                  INTERVAL 7 month) "
        + "                              AND oo.obs_datetime <= filt.filt_3hp_start_date) ";
  }

  public String getINHStartDate4InhAndSeguimentoOnFilt() {
    return "SELECT p.patient_id, o2.obs_datetime AS start_date "
        + "FROM   patient p "
        + "       INNER JOIN encounter e ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o ON e.encounter_id = o.encounter_id "
        + "       INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id "
        + "WHERE  p.voided = 0 AND e.voided = 0 AND o.voided = 0 AND o2.voided = 0 "
        + "       AND e.location_id = :location "
        + "       AND e.encounter_type = 60 "
        + "       AND o.concept_id = 23985 AND o.value_coded IN (656, 23982) "
        + "       AND o2.concept_id = 23987 AND o2.value_coded IN ( 1256, 1705) "
        + "       AND o2.obs_datetime BETWEEN DATE_SUB(:startDate, INTERVAL 6 MONTH) AND DATE_SUB(:endDate, INTERVAL 6 MONTH) ";
  }

  public String getCompleted3HPOnFichaResumo() {
    return "SELECT p.patient_id, o2.obs_datetime AS complete_date "
        + "FROM   patient p "
        + "       INNER JOIN encounter e ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o ON e.encounter_id = o.encounter_id "
        + "       INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id "
        + "WHERE  p.voided = 0 AND e.voided = 0 AND o.voided = 0 AND o2.voided = 0"
        + "       AND e.location_id = :location "
        + "       AND e.encounter_type = 53 "
        + "       AND ( (o.concept_id = 23985 AND o.value_coded = 23954) "
        + "        AND (o2.concept_id = 165308 AND o2.value_coded = 1267 "
        + "        AND o2.obs_datetime BETWEEN DATE_SUB(:startDate, INTERVAL 6 MONTH) AND DATE_SUB(:endDate, INTERVAL 6 MONTH) ) )";
  }

  public CohortDefinition getPatientsWhoCompleted3HPAtLeast86Days() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients who completed 3HP - At Least 86 Days ");
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));

    String completed3hpAllSources =
        new EptsQueriesUtil()
            .unionBuilder(getCompletedDateOf3HPOnFichaClinica())
            .union(getCompleted3HPOnFichaResumo())
            .union(getCompletedDateOfDT3HPOnFichaClinica())
            .buildQuery();

    String query =
        " SELECT tpt_completed.patient_id "
            + " FROM (        "
            + "              SELECT patient_id,  MAX(complete_date) complete_date       "
            + "              FROM (     "
            + "                   "
            + completed3hpAllSources
            + "                ) recent_3hp "
            + "                 GROUP BY recent_3hp.patient_id  "
            + "                ) tpt_completed "
            + " INNER JOIN ( "
            + "                SELECT patient_id,  MIN(start_date) start_date             "
            + "                FROM (             "
            + "              "
            + get3HPStartDateQuery()
            + "               ) start "
            + "               GROUP BY start.patient_id"
            + "            ) start on start.patient_id = tpt_completed.patient_id "
            + "WHERE TIMESTAMPDIFF(DAY,start.start_date, tpt_completed.complete_date) >= 86 "
            + "GROUP BY tpt_completed.patient_id ";

    sqlCohortDefinition.setQuery(query);

    return sqlCohortDefinition;
  }

  public String getCompletedDateOf3HPOnFichaClinica() {
    return "SELECT p.patient_id, o2.obs_datetime AS complete_date "
        + "FROM   patient p "
        + "       INNER JOIN encounter e ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o ON e.encounter_id = o.encounter_id "
        + "       INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id "
        + "WHERE  p.voided = 0 AND e.voided = 0 AND o.voided = 0 AND o2.voided = 0 "
        + "       AND e.location_id = :location "
        + "       AND e.encounter_type = 6"
        + "       AND (o.concept_id = 23985 AND o.value_coded = 23954)  "
        + "       AND (o2.concept_id = 165308 AND o2.value_coded = 1267 "
        + "       AND o2.obs_datetime BETWEEN DATE_SUB(:startDate, INTERVAL 6 MONTH) AND DATE_SUB(:endDate, INTERVAL 6 MONTH) ) ";
  }

  public String getCompletedDateOfDT3HPOnFichaClinica() {
    return "SELECT p.patient_id, e.encounter_datetime AS complete_date "
        + "FROM   patient p "
        + "       INNER JOIN encounter e ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o ON e.encounter_id = o.encounter_id "
        + "WHERE  p.voided = 0 "
        + "       AND e.voided = 0 "
        + "       AND o.voided = 0 "
        + "       AND e.location_id = :location "
        + "       AND e.encounter_type = 6 "
        + "       AND o.concept_id = 1719 "
        + "       AND o.value_coded = 165307 "
        + "       AND e.encounter_datetime BETWEEN DATE_SUB(:startDate, INTERVAL 6 MONTH) AND DATE_SUB(:endDate, INTERVAL 6 MONTH) ";
  }

  // Patients with one of the following combinations marked in Ficha Clínica - Mastercard and/or
  // FILT:
  public CohortDefinition getAtLeast3ConsultationOnFichaClinica() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("At least 1 consultation registered on Ficha Clínica ");
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));

    String query =
        "SELECT patient_id "
            + "FROM   (SELECT profilaxy.patient_id,COUNT(obs_datetime) encounters "
            + "        FROM   (SELECT p.patient_id, o2.obs_datetime "
            + "                FROM   patient p "
            + "                       INNER JOIN encounter e "
            + "                               ON p.patient_id = e.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON e.encounter_id = o.encounter_id "
            + "                       INNER JOIN obs o2 "
            + "                               ON e.encounter_id = o2.encounter_id "
            + "                WHERE  p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o2.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                       AND e.encounter_type = 6 "
            + "                       AND ( o.concept_id = 23985  AND o.value_coded = 23954 ) "
            + "                       AND ( o2.concept_id = 165308 AND o2.value_coded IN ( 1256, 1257 ) )) profilaxy "
            + "               INNER JOIN (SELECT patient_id,MIN(start_date) start_date "
            + "                           FROM   ("
            + get3HPStartDateQuery()
            + "                                  )tpt "
            + "                           GROUP  BY tpt.patient_id) tpt_start ON tpt_start.patient_id = profilaxy.patient_id "
            + "        WHERE  profilaxy.obs_datetime BETWEEN tpt_start.start_date AND DATE_ADD(tpt_start.start_date, INTERVAL 4 MONTH) "
            + "        GROUP  BY profilaxy.patient_id) three_encounters "
            + "WHERE  three_encounters.encounters >= 3";

    sqlCohortDefinition.setQuery(query);

    return sqlCohortDefinition;
  }

  public CohortDefinition getAtLeastOne3HPOnFilt() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("At least 1 FILT with 3HP Trimestral ");
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "       INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "       INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id "
            + "WHERE  p.voided = 0 AND e.voided = 0 AND o.voided = 0 AND o2.voided = 0 "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_type = 60"
            + "       AND (o.concept_id = 23985 AND o.value_coded IN (23954, 23984))  "
            + "       AND (o2.concept_id = 23986 AND o2.value_coded = 23720 "
            + "       AND o2.obs_datetime BETWEEN DATE_SUB(:startDate, INTERVAL 6 MONTH) AND DATE_SUB(:endDate, INTERVAL 6 MONTH) ) "
            + "GROUP BY p.patient_id ";

    sqlCohortDefinition.setQuery(query);

    return sqlCohortDefinition;
  }

  public CohortDefinition getAtLeast3ConsultarionWithDispensaMensalOnFilt() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("At least 3 FILTs with 3HP Mensal ");
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));

    String query =
        "SELECT patient_id "
            + "FROM   (SELECT profilaxy.patient_id,COUNT(obs_datetime) encounters "
            + "        FROM   (SELECT p.patient_id, o2.obs_datetime "
            + "                FROM   patient p "
            + "                       INNER JOIN encounter e "
            + "                               ON p.patient_id = e.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON e.encounter_id = o.encounter_id "
            + "                       INNER JOIN obs o2 "
            + "                               ON e.encounter_id = o2.encounter_id "
            + "                WHERE  p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o2.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                       AND e.encounter_type = 60 "
            + "                       AND ( o.concept_id = 23985 AND o.value_coded IN (23954, 23984) ) "
            + "                       AND ( o2.concept_id = 23986 AND o2.value_coded = 1098 )) profilaxy "
            + "               INNER JOIN (SELECT patient_id,MIN(start_date) start_date "
            + "                           FROM   ("
            + get3HPStartDateQuery()
            + "                                  )tpt "
            + "                           GROUP  BY tpt.patient_id) tpt_start ON tpt_start.patient_id = profilaxy.patient_id "
            + "        WHERE  profilaxy.obs_datetime BETWEEN tpt_start.start_date AND DATE_ADD(tpt_start.start_date, INTERVAL 4 MONTH) "
            + "        GROUP  BY profilaxy.patient_id) three_encounters "
            + "WHERE  three_encounters.encounters >= 3";

    sqlCohortDefinition.setQuery(query);

    return sqlCohortDefinition;
  }

  public CohortDefinition getAtLeast1ConsultarionWithDT3HPOnFichaClinica() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("At least 1 consultation registered on Ficha Clínica ");
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));

    String query =
        " SELECT profilaxy.patient_id "
            + "        FROM   (SELECT p.patient_id, e.encounter_datetime "
            + "                FROM   patient p "
            + "                       INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                       INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                WHERE  p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                       AND e.encounter_type = 6 "
            + "                       AND o.concept_id = 1719 AND o.value_coded = 165307  "
            + "                       ) profilaxy "
            + "               INNER JOIN (SELECT patient_id,MIN(start_date) start_date "
            + "                           FROM   ("
            + get3HPStartDateQuery()
            + "                                  )tpt "
            + "                           GROUP  BY tpt.patient_id) tpt_start ON tpt_start.patient_id = profilaxy.patient_id "
            + "        WHERE  profilaxy.encounter_datetime BETWEEN tpt_start.start_date AND DATE_ADD(tpt_start.start_date, INTERVAL 4 MONTH) "
            + "        GROUP  BY profilaxy.patient_id ";

    sqlCohortDefinition.setQuery(query);

    return sqlCohortDefinition;
  }

  // The system will identify from all patients who started INH Regimen (TB_PREV_FR4) those who
  // completed the treatment until reporting end date as following:

  public String getCompletedIPTOnFichaResumo() {
    return "SELECT p.patient_id, o2.obs_datetime AS complete_date  "
        + "FROM   patient p "
        + "       INNER JOIN encounter e ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o ON e.encounter_id = o.encounter_id "
        + "       INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id "
        + "WHERE  p.voided = 0 AND e.voided = 0 AND o.voided = 0 AND o2.voided = 0"
        + "       AND e.location_id = :location "
        + "       AND e.encounter_type = 53 "
        + "       AND ( (o.concept_id = 23985 AND o.value_coded = 656) "
        + "        AND (o2.concept_id = 165308 AND o2.value_coded = 1267 "
        + "        AND o2.obs_datetime BETWEEN DATE_SUB(:startDate, INTERVAL 6 MONTH) AND DATE_SUB(:endDate, INTERVAL 6 MONTH) ) )";
  }

  public String getCompletedDateOfIPTOnFichaClinica() {
    return "SELECT p.patient_id, o2.obs_datetime AS complete_date  "
        + "FROM   patient p "
        + "       INNER JOIN encounter e ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o ON e.encounter_id = o.encounter_id "
        + "       INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id "
        + "WHERE  p.voided = 0 AND e.voided = 0 AND o.voided = 0 AND o2.voided = 0 "
        + "       AND e.location_id = :location "
        + "       AND e.encounter_type IN (6,9) "
        + "       AND (o.concept_id = 23985 AND o.value_coded = 656)  "
        + "       AND (o2.concept_id = 165308 AND o2.value_coded = 1267 "
        + "       AND o2.obs_datetime BETWEEN DATE_SUB(:startDate, INTERVAL 6 MONTH) AND DATE_SUB(:endDate, INTERVAL 6 MONTH) ) ";
  }

  public CohortDefinition getPatientsWhoCompletedINHAtLeast173Days() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients who completed IPT - At Least 173 Days ");
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));

    String completedInhAllSources =
        new EptsQueriesUtil()
            .unionBuilder(getCompletedIPTOnFichaResumo())
            .union(getCompletedDateOfIPTOnFichaClinica())
            .buildQuery();

    String query =
        " SELECT tpt_completed.patient_id "
            + " FROM (        "
            + "              SELECT patient_id,  MAX(complete_date) complete_date       "
            + "              FROM (     "
            + "                   "
            + completedInhAllSources
            + "                ) recent_3hp "
            + "                 GROUP BY recent_3hp.patient_id  "
            + "                ) tpt_completed "
            + " INNER JOIN ( "
            + "                SELECT patient_id,  MIN(start_date) start_date             "
            + "                FROM (             "
            + "              "
            + getIPTStartDateQuery()
            + "               ) start "
            + "               GROUP BY start.patient_id"
            + "            ) start on start.patient_id = tpt_completed.patient_id "
            + "WHERE TIMESTAMPDIFF(DAY,start.start_date, tpt_completed.complete_date) >= 173 "
            + "GROUP BY tpt_completed.patient_id ";

    sqlCohortDefinition.setQuery(query);

    return sqlCohortDefinition;
  }

  public CohortDefinition getAtLeast5ConsultarionINHOnFichaClinica() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("At least 1 consultation registered on Ficha Clínica ");
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));

    String query =
        "SELECT patient_id "
            + "FROM   (SELECT profilaxy.patient_id,COUNT(obs_datetime) encounters "
            + "        FROM   (SELECT p.patient_id, o2.obs_datetime "
            + "                FROM   patient p "
            + "                       INNER JOIN encounter e "
            + "                               ON p.patient_id = e.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON e.encounter_id = o.encounter_id "
            + "                       INNER JOIN obs o2 "
            + "                               ON e.encounter_id = o2.encounter_id "
            + "                WHERE  p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o2.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                       AND e.encounter_type IN (6,9) "
            + "                       AND ( o.concept_id = 23985  AND o.value_coded = 656 ) "
            + "                       AND ( o2.concept_id = 165308 AND o2.value_coded IN ( 1256, 1257 ) )) profilaxy "
            + "               INNER JOIN (SELECT patient_id,MIN(start_date) start_date "
            + "                           FROM   ("
            + getIPTStartDateQuery()
            + "                                  )tpt "
            + "                           GROUP  BY tpt.patient_id) tpt_start ON tpt_start.patient_id = profilaxy.patient_id "
            + "        WHERE  profilaxy.obs_datetime < DATE_ADD(tpt_start.start_date, INTERVAL 7 MONTH) "
            + "        GROUP  BY profilaxy.patient_id) three_encounters "
            + "WHERE  three_encounters.encounters >= 3";

    sqlCohortDefinition.setQuery(query);

    return sqlCohortDefinition;
  }

  public CohortDefinition getAtLeast6ConsultarionWithINHDispensaMensalOnFilt() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("At least 6 FILT with INH Mensal ");
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));

    String query =
        "SELECT patient_id "
            + "FROM   (SELECT profilaxy.patient_id,COUNT(obs_datetime) encounters "
            + "        FROM   (SELECT p.patient_id, o2.obs_datetime "
            + "                FROM   patient p "
            + "                       INNER JOIN encounter e "
            + "                               ON p.patient_id = e.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON e.encounter_id = o.encounter_id "
            + "                       INNER JOIN obs o2 "
            + "                               ON e.encounter_id = o2.encounter_id "
            + "                WHERE  p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o2.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                       AND e.encounter_type = 60 "
            + "                       AND ( o.concept_id = 23985 AND o.value_coded IN (656, 23982) ) "
            + "                       AND ( o2.concept_id = 23986 AND o2.value_coded = 1098 )) profilaxy "
            + "               INNER JOIN (SELECT patient_id,MIN(start_date) start_date "
            + "                           FROM   ("
            + getIPTStartDateQuery()
            + "                                  )tpt "
            + "                           GROUP  BY tpt.patient_id) tpt_start ON tpt_start.patient_id = profilaxy.patient_id "
            + "        WHERE  profilaxy.obs_datetime BETWEEN tpt_start.start_date AND DATE_ADD(tpt_start.start_date, INTERVAL 4 MONTH) "
            + "        GROUP  BY profilaxy.patient_id) three_encounters "
            + "WHERE  three_encounters.encounters >= 6";

    sqlCohortDefinition.setQuery(query);

    return sqlCohortDefinition;
  }

  public CohortDefinition getAtLeast2ConsultarionOfDTINHOnFichaClinica() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "At least 2 consultations registered on Ficha Clínica with DT-INH ");
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));

    String query =
        "SELECT patient_id "
            + "FROM   (SELECT profilaxy.patient_id,COUNT(obs_datetime) encounters "
            + "        FROM   (SELECT p.patient_id, o2.obs_datetime "
            + "                FROM   patient p "
            + "                       INNER JOIN encounter e "
            + "                               ON p.patient_id = e.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON e.encounter_id = o.encounter_id "
            + "                       INNER JOIN obs o2 "
            + "                               ON e.encounter_id = o2.encounter_id "
            + "                WHERE  p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o2.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                       AND e.encounter_type IN (6,9) "
            + "                       AND ( o.concept_id = 23985  AND o.value_coded = 656 ) "
            + "                       AND ( o2.concept_id = 165308 AND o2.value_coded IN ( 1256, 1257 ) )) profilaxy "
            + "               INNER JOIN (SELECT patient_id,MIN(start_date) start_date "
            + "                           FROM   ("
            + getIPTStartDateQuery()
            + "                                  )tpt "
            + "                           GROUP  BY tpt.patient_id) tpt_start ON tpt_start.patient_id = profilaxy.patient_id "
            + "        WHERE  profilaxy.obs_datetime BETWEEN tpt_start.start_date AND DATE_ADD(tpt_start.start_date, INTERVAL 5 MONTH) "
            + "        GROUP  BY profilaxy.patient_id) three_encounters "
            + "WHERE  three_encounters.encounters >= 2";

    sqlCohortDefinition.setQuery(query);

    return sqlCohortDefinition;
  }

  public CohortDefinition getAtLeast2ConsultarionWithINHDispensaTrimestralOnFilt() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("At least 2 FILT with DT-INH  ");
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));

    String query =
        "SELECT patient_id "
            + "FROM   (SELECT profilaxy.patient_id,COUNT(obs_datetime) encounters "
            + "        FROM   (SELECT p.patient_id, o2.obs_datetime "
            + "                FROM   patient p "
            + "                       INNER JOIN encounter e "
            + "                               ON p.patient_id = e.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON e.encounter_id = o.encounter_id "
            + "                       INNER JOIN obs o2 "
            + "                               ON e.encounter_id = o2.encounter_id "
            + "                WHERE  p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o2.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                       AND e.encounter_type = 60 "
            + "                       AND ( o.concept_id = 23985 AND o.value_coded IN (656, 23982) ) "
            + "                       AND ( o2.concept_id = 23986 AND o2.value_coded = 23985 )) profilaxy "
            + "               INNER JOIN (SELECT patient_id,MIN(start_date) start_date "
            + "                           FROM   ("
            + getIPTStartDateQuery()
            + "                                  )tpt "
            + "                           GROUP  BY tpt.patient_id) tpt_start ON tpt_start.patient_id = profilaxy.patient_id "
            + "        WHERE  profilaxy.obs_datetime BETWEEN tpt_start.start_date AND DATE_ADD(tpt_start.start_date, INTERVAL 5 MONTH) "
            + "        GROUP  BY profilaxy.patient_id) three_encounters "
            + "WHERE  three_encounters.encounters >= 2";

    sqlCohortDefinition.setQuery(query);

    return sqlCohortDefinition;
  }

  public CohortDefinition getAtLeast3ConsultarionOfINHOnFichaClinica() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("At least 3 consultations registered on Ficha Clínica with INH  ");
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));

    String query =
        "SELECT patient_id "
            + "FROM   (SELECT profilaxy.patient_id,COUNT(obs_datetime) encounters "
            + "        FROM   (SELECT p.patient_id, o2.obs_datetime "
            + "                FROM   patient p "
            + "                       INNER JOIN encounter e "
            + "                               ON p.patient_id = e.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON e.encounter_id = o.encounter_id "
            + "                       INNER JOIN obs o2 "
            + "                               ON e.encounter_id = o2.encounter_id "
            + "                WHERE  p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o2.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                       AND e.encounter_type IN (6,9) "
            + "                       AND ( o.concept_id = 23985  AND o.value_coded = 656 ) "
            + "                       AND ( o2.concept_id = 165308 AND o2.value_coded IN ( 1256, 1257 ) )) profilaxy "
            + "               INNER JOIN (SELECT patient_id,MIN(start_date) start_date "
            + "                           FROM   ("
            + getIPTStartDateQuery()
            + "                                  )tpt "
            + "                           GROUP  BY tpt.patient_id) tpt_start ON tpt_start.patient_id = profilaxy.patient_id "
            + "        WHERE  profilaxy.obs_datetime BETWEEN tpt_start.start_date AND DATE_ADD(tpt_start.start_date, INTERVAL 7 MONTH) "
            + "        GROUP  BY profilaxy.patient_id) three_encounters "
            + "WHERE  three_encounters.encounters >= 3";

    sqlCohortDefinition.setQuery(query);

    return sqlCohortDefinition;
  }

  public CohortDefinition getAtLeast1ConsultarionWithDTINHOnFichaClinica() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("1 Ficha Clínica com DT-INH ");
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));

    String query =
        " SELECT profilaxy.patient_id "
            + "        FROM   (SELECT p.patient_id, e.encounter_datetime "
            + "                FROM   patient p "
            + "                       INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                       INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                WHERE  p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                       AND e.encounter_type = 6 "
            + "                       AND o.concept_id = 1719 AND o.value_coded = 23955  "
            + "                       ) profilaxy "
            + "               INNER JOIN (SELECT patient_id,MIN(start_date) start_date "
            + "                           FROM   ("
            + getIPTStartDateQuery()
            + "                                  )tpt "
            + "                           GROUP  BY tpt.patient_id) tpt_start ON tpt_start.patient_id = profilaxy.patient_id "
            + "        WHERE  profilaxy.encounter_datetime BETWEEN tpt_start.start_date AND DATE_ADD(tpt_start.start_date, INTERVAL 7 MONTH) "
            + "        GROUP  BY profilaxy.patient_id ";

    sqlCohortDefinition.setQuery(query);

    return sqlCohortDefinition;
  }

  public CohortDefinition getAtLeast3ConsultarionWithINHDispensaMensalOnFilt() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("At least 3 FILT with INH Mensal  ");
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));

    String query =
        "SELECT patient_id "
            + "FROM   (SELECT profilaxy.patient_id,COUNT(obs_datetime) encounters "
            + "        FROM   (SELECT p.patient_id, o2.obs_datetime "
            + "                FROM   patient p "
            + "                       INNER JOIN encounter e "
            + "                               ON p.patient_id = e.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON e.encounter_id = o.encounter_id "
            + "                       INNER JOIN obs o2 "
            + "                               ON e.encounter_id = o2.encounter_id "
            + "                WHERE  p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o2.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                       AND e.encounter_type = 60 "
            + "                       AND ( o.concept_id = 23985 AND o.value_coded IN (656, 23982) ) "
            + "                       AND ( o2.concept_id = 23986 AND o2.value_coded = 1098 )) profilaxy "
            + "               INNER JOIN (SELECT patient_id,MIN(start_date) start_date "
            + "                           FROM   ("
            + getIPTStartDateQuery()
            + "                                  )tpt "
            + "                           GROUP  BY tpt.patient_id) tpt_start ON tpt_start.patient_id = profilaxy.patient_id "
            + "        WHERE  profilaxy.obs_datetime BETWEEN tpt_start.start_date AND DATE_ADD(tpt_start.start_date, INTERVAL 7 MONTH) "
            + "        GROUP  BY profilaxy.patient_id) three_encounters "
            + "WHERE  three_encounters.encounters >= 3";

    sqlCohortDefinition.setQuery(query);

    return sqlCohortDefinition;
  }

  public CohortDefinition getAtLeast1ConsultarionWithDTINHDispensaTrimestralOnFilt() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" 1 FILT with DT-INH  ");
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));

    String query =
        "SELECT patient_id "
            + "FROM   (SELECT profilaxy.patient_id,COUNT(obs_datetime) encounters "
            + "        FROM   (SELECT p.patient_id, o2.obs_datetime "
            + "                FROM   patient p "
            + "                       INNER JOIN encounter e "
            + "                               ON p.patient_id = e.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON e.encounter_id = o.encounter_id "
            + "                       INNER JOIN obs o2 "
            + "                               ON e.encounter_id = o2.encounter_id "
            + "                WHERE  p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o2.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                       AND e.encounter_type = 60 "
            + "                       AND ( o.concept_id = 23985 AND o.value_coded IN (656, 23982) ) "
            + "                       AND ( o2.concept_id = 23986 AND o2.value_coded = 23989 )) profilaxy "
            + "               INNER JOIN (SELECT patient_id,MIN(start_date) start_date "
            + "                           FROM   ("
            + getIPTStartDateQuery()
            + "                                  )tpt "
            + "                           GROUP  BY tpt.patient_id) tpt_start ON tpt_start.patient_id = profilaxy.patient_id "
            + "        WHERE  profilaxy.obs_datetime BETWEEN tpt_start.start_date AND DATE_ADD(tpt_start.start_date, INTERVAL 7 MONTH) "
            + "        GROUP  BY profilaxy.patient_id) three_encounters "
            + "WHERE  three_encounters.encounters >= 3";

    sqlCohortDefinition.setQuery(query);

    return sqlCohortDefinition;
  }

  public String getTPTStartDateQuery() {
    EptsQueriesUtil eptsQueriesUtil = new EptsQueriesUtil();
    String tptQuery =
        eptsQueriesUtil
            .unionBuilder(getStartDateOf3hpPiridoxinaOnFilt())
            .union(getStartDateOf3HPOnFichaClinica())
            .union(getStartDateOfDT3HPOnFichaClinica())
            .union(get3HPStartOnFichaResumo())
            .union(getStartDateOfINHOnFilt())
            .union(getStartDateINHOnFichaClinica())
            .union(getINHStartDate())
            .union(getINHStartDate4InhAndSeguimentoOnFilt())
            .buildQuery();

    return tptQuery;
  }

  public String get3HPStartDateQuery() {
    EptsQueriesUtil eptsQueriesUtil = new EptsQueriesUtil();
    String tptQuery =
        eptsQueriesUtil
            .unionBuilder(getStartDateOf3hpPiridoxinaOnFilt())
            .union(getStartDateOf3HPOnFichaClinica())
            .union(getStartDateOfDT3HPOnFichaClinica())
            .union(get3HPStartOnFichaResumo())
            .buildQuery();

    return tptQuery;
  }

  public String getIPTStartDateQuery() {
    EptsQueriesUtil eptsQueriesUtil = new EptsQueriesUtil();
    String tptQuery =
        eptsQueriesUtil
            .unionBuilder(getStartDateOfINHOnFilt())
            .union(getStartDateINHOnFichaClinica())
            .union(getINHStartDate())
            .union(getINHStartDate4InhAndSeguimentoOnFilt())
            .buildQuery();

    return tptQuery;
  }

  public CohortDefinition getPatientsWhoStartedTptAndNewOnArt() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Get Patients Who Started TPT and New on ART ");
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));

    String query =
        "SELECT patient_id FROM ("
            + " SELECT tpt_start.patient_id, MIN(tpt_start.start_date) AS start_date "
            + " FROM (        "
            + getTPTStartDateQuery()
            + "                ) tpt_start "
            + " INNER JOIN ( "
            + commonQueries.getARTStartDate(true)
            + " ) art on art.patient_id = tpt_start.patient_id "
            + " WHERE tpt_start.start_date BETWEEN art.first_pickup AND DATE_ADD(art.first_pickup, INTERVAL 6 MONTH) "
            + " OR tpt_start.start_date < art.first_pickup "
            + "GROUP BY tpt_start.patient_id "
            + " ) tpt";

    sqlCohortDefinition.setQuery(query);

    return sqlCohortDefinition;
  }

  public CohortDefinition getPatientsWhoStartedTptPreviouslyOnArt() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Get Patients Who Started TPT and Previously on ART ");
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));

    String query =
        "SELECT patient_id FROM ("
            + " SELECT tpt_start.patient_id, MIN(tpt_start.start_date) AS start_date "
            + " FROM (        "
            + getTPTStartDateQuery()
            + "                ) tpt_start "
            + " INNER JOIN ( "
            + commonQueries.getARTStartDate(true)
            + " ) art on art.patient_id = tpt_start.patient_id "
            + " WHERE tpt_start.start_date > art.first_pickup AND DATE_ADD(art.first_pickup, INTERVAL 6 MONTH) "
            + "GROUP BY tpt_start.patient_id "
            + " ) tpt";

    sqlCohortDefinition.setQuery(query);

    return sqlCohortDefinition;
  }
}
