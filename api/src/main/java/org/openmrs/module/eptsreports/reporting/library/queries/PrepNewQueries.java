package org.openmrs.module.eptsreports.reporting.library.queries;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;

public class PrepNewQueries {

  /**
   * <b>A: For the selected Location and reporting period (start Date and endDate) the system will
   * identify clients (>= 15 years old) who newly initiated PrEP as following</b>
   *
   * <p>All clients who have “O utente esta iniciar pela 1a vez a PrEP Data” (Concept id 165296 from
   * encounter type 80) value coded “Start drugs” (concept id 1256) and value datetime <=end date;
   * or
   *
   * @param initialStatusPrep
   * @param prepInicialEncounterType
   * @param startDrugsConcept
   * @param prepStartDateConcept
   * @return
   */
  public static String getA(
      int initialStatusPrep,
      int prepInicialEncounterType,
      int startDrugsConcept,
      int prepStartDateConcept) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName("Get Clients who Initiated PrEP");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();

    valuesMap.put("165296", initialStatusPrep);
    valuesMap.put("80", prepInicialEncounterType);
    valuesMap.put("1256", startDrugsConcept);
    valuesMap.put("165211", prepStartDateConcept);

    String query =
        "SELECT patient_id FROM ( "
            + "    SELECT p.patient_id, min(tbl.earliest_date) AS  earliest_date_ever "
            + "FROM   patient p "
            + "           INNER JOIN encounter e "
            + "                      ON p.patient_id = e.patient_id "
            + "           INNER JOIN obs o "
            + "                      ON e.encounter_id = o.encounter_id "
            + "           INNER JOIN (SELECT p.patient_id, "
            + "                              o.obs_datetime AS earliest_date "
            + "                       FROM   patient p "
            + "                                  INNER JOIN encounter e "
            + "                                             ON p.patient_id = e.patient_id "
            + "                                  INNER JOIN obs o "
            + "                                             ON e.encounter_id = o.encounter_id "
            + "                       WHERE  p.voided = 0 "
            + "                         AND e.voided = 0 "
            + "                         AND o.voided = 0 "
            + "                         AND e.encounter_type = ${80} "
            + "                         AND o.concept_id = ${165296} "
            + "                         AND o.value_coded = ${1256}"
            + "                         AND e.location_id = :location "
            + "                         AND o.obs_datetime <= :endDate "
            + "                       GROUP  BY p.patient_id "
            + "                       UNION "
            + "                       SELECT p.patient_id, "
            + "                              o.value_datetime AS earliest_date "
            + "                       FROM   patient p "
            + "                                  INNER JOIN encounter e "
            + "                                             ON p.patient_id = e.patient_id "
            + "                                  INNER JOIN obs o "
            + "                                             ON e.encounter_id = o.encounter_id "
            + "                       WHERE  p.voided = 0 "
            + "                         AND e.voided = 0 "
            + "                         AND o.voided = 0 "
            + "                         AND e.encounter_type = ${80} "
            + "                         AND o.concept_id = ${165211} "
            + "                         AND e.location_id = :location "
            + "                         AND o.value_datetime <= :endDate "
            + "                       GROUP  BY p.patient_id) tbl "
            + "                      ON tbl.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND e.location_id = :location "
            + "GROUP  BY p.patient_id "
            + "    ) clients "
            + "WHERE "
            + "      clients.earliest_date_ever BETWEEN :startDate AND :endDate";

    StringSubstitutor sb = new StringSubstitutor(valuesMap);
    return sb.replace(query);
  }

  /**
   * <b>B: Exclude all clients who have been transferred in as follows </b>
   *
   * <p>All clients who are enrolled in PrEP Program (PREP)(program id 25) and have the first
   * historical state as “Transferido de outra US” (patient state id= 76) in the client chart by end
   * of the reporting period; or
   *
   * <p>All clients who have marked “Transferido de outra US”(concept id 1594 value coded 1369) in
   * the first Ficha de Consulta Inicial PrEP(encounter type 80, Min(encounter datetime)) registered
   * in the system–by end of the reporting period.
   *
   * @param prepProgram
   * @param stateOfStayOnPrepProgram
   * @param prepInicialEncounterType
   * @param referalType
   * @param transferredFromOtherFacility
   * @return
   */
  public static String getB(
      int prepProgram,
      int stateOfStayOnPrepProgram,
      int prepInicialEncounterType,
      int referalType,
      int transferredFromOtherFacility) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName("Get all clients who are Transferred in");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();

    valuesMap.put("25", prepProgram);
    valuesMap.put("76", stateOfStayOnPrepProgram);
    valuesMap.put("80", prepInicialEncounterType);
    valuesMap.put("1594", referalType);
    valuesMap.put("1369", transferredFromOtherFacility);

    String query =
        "SELECT patient_id FROM ( "
            + "                           SELECT p.patient_id, ps.start_date AS start_date "
            + "                           FROM   patient p "
            + "                                      INNER JOIN patient_program pg "
            + "                                                 ON p.patient_id = pg.patient_id "
            + "                                      INNER JOIN patient_state ps "
            + "                                                 ON pg.patient_program_id = "
            + "                                                    ps.patient_program_id "
            + "                           WHERE  pg.program_id = ${25} AND ps.voided = 0 "
            + "                             AND pg.voided = 0 "
            + "                             AND ps.state = ${76} "
            + "                             AND ps.start_date < :endDate "
            + "                             AND pg.location_id = :location "
            + "                           GROUP  BY p.patient_id "
            + "                           UNION "
            + "                           SELECT p.patient_id, "
            + "                                  Min(e.encounter_datetime) AS start_date "
            + "                           FROM   patient p "
            + "                                      INNER JOIN encounter e "
            + "                                                 ON p.patient_id = e.patient_id "
            + "                                      INNER JOIN obs o "
            + "                                                 ON e.encounter_id = o.encounter_id "
            + "                           WHERE  p.voided = 0 "
            + "                             AND e.voided = 0 "
            + "                             AND o.voided = 0 "
            + "                             AND e.encounter_type = ${80} "
            + "                             AND o.concept_id = ${1594} "
            + "                             AND o.value_coded = ${1369} "
            + "                             AND e.location_id = :location "
            + "                             AND e.encounter_datetime < :endDate "
            + "                           GROUP  BY p.patient_id "
            + "                       ) transferred ";

    StringSubstitutor sb = new StringSubstitutor(valuesMap);
    return sb.replace(query);
  }

  /**
   * <b>Get Clients Age based on PrEP Start date</b>
   *
   * @param initialStatusPrep
   * @param prepInicialEncounterType
   * @param startDrugsConcept
   * @param prepStartDateConcept
   * @param minAge
   * @param maxAge
   * @return
   */
  public static String getPatientAgeBasedOnPrepStartDate(
      int initialStatusPrep,
      int prepInicialEncounterType,
      int startDrugsConcept,
      int prepStartDateConcept,
      int minAge,
      int maxAge) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName("Get Clients Age based on PrEP Start date");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();

    valuesMap.put("165296", initialStatusPrep);
    valuesMap.put("80", prepInicialEncounterType);
    valuesMap.put("1256", startDrugsConcept);
    valuesMap.put("165211", prepStartDateConcept);
    valuesMap.put("minAge", minAge);
    valuesMap.put("maxAge", maxAge);

    String query =
        "SELECT patient_id "
            + "FROM   (SELECT pat.patient_id, "
            + "               Timestampdiff(year, pn.birthdate, clients.earliest_date_ever) AS "
            + "               age "
            + "        FROM   patient pat "
            + "               INNER JOIN person pn "
            + "                       ON pat.patient_id = pn.person_id "
            + "               INNER JOIN (SELECT patient_id, "
            + "                                  earliest_date_ever "
            + "                           FROM   (SELECT p.patient_id, "
            + "                                          Min(tbl.earliest_date) AS "
            + "                                          earliest_date_ever "
            + "                                   FROM   patient p "
            + "                                          INNER JOIN encounter e "
            + "                                                  ON p.patient_id = e.patient_id "
            + "                                          INNER JOIN obs o "
            + "                                                  ON e.encounter_id = "
            + "                                                     o.encounter_id "
            + "                                          INNER JOIN ( "
            + "                                          SELECT "
            + "                                          p.patient_id, "
            + "                                          o.obs_datetime AS earliest_date "
            + "                                                      FROM   patient p "
            + "                                          INNER JOIN encounter e "
            + "                                                  ON p.patient_id = "
            + "                                                     e.patient_id "
            + "                                          INNER JOIN obs o "
            + "                                                  ON e.encounter_id "
            + "                                                     = "
            + "                                                     o.encounter_id "
            + "                                                      WHERE  p.voided = 0 "
            + "                                                             AND e.voided = 0 "
            + "                                                             AND o.voided = 0 "
            + "                                                             AND "
            + "                                          e.encounter_type = 80 "
            + "                                                             AND "
            + "                                          o.concept_id = ${165296} "
            + "                                                             AND "
            + "                                          o.value_coded = ${1256} "
            + "                                                             AND "
            + "                                          e.location_id = :location "
            + "                                                             AND "
            + "                                          o.obs_datetime <= "
            + "                                          :endDate "
            + "                                                      GROUP  BY p.patient_id "
            + "                                                      UNION "
            + "                                          SELECT p.patient_id, "
            + "                                                 o.value_datetime AS "
            + "                                                 earliest_date "
            + "                                          FROM   patient p "
            + "                                          INNER JOIN encounter e "
            + "                                                  ON p.patient_id = "
            + "                                                     e.patient_id "
            + "                                          INNER JOIN obs o "
            + "                                                  ON e.encounter_id "
            + "                                                     = "
            + "                                                     o.encounter_id "
            + "                                                      WHERE  p.voided = 0 "
            + "                                                             AND e.voided = 0 "
            + "                                                             AND o.voided = 0 "
            + "                                                             AND "
            + "                                          e.encounter_type = ${80} "
            + "                                                             AND "
            + "                                          o.concept_id = ${165211} "
            + "                                                             AND "
            + "                                          e.location_id = :location "
            + "                                                             AND "
            + "                                          o.value_datetime <= "
            + "                                          :endDate "
            + "                                                      GROUP  BY p.patient_id) "
            + "                                                     tbl "
            + "                                                  ON tbl.patient_id = "
            + "                                                     p.patient_id "
            + "                                   WHERE  p.voided = 0 "
            + "                                          AND e.voided = 0 "
            + "                                          AND o.voided = 0 "
            + "                                          AND e.location_id = :location "
            + "                                   GROUP  BY p.patient_id) clients "
            + "                           WHERE  clients.earliest_date_ever BETWEEN "
            + "                                  :startDate AND :endDate) clients "
            + "                       ON pat.patient_id = clients.patient_id) fin "
            + "WHERE  fin.age BETWEEN ${minAge} AND ${maxAge}";

    StringSubstitutor sb = new StringSubstitutor(valuesMap);
    return sb.replace(query);
  }

  /**
   * <b>Description:</b> Clients with the field “Mulher Grávida” marked on section C “Estado da *
   * Mulher” with the value “Sim” on the oldest Ficha de Consulta Inicial PrEP with a Data de Início
   * * PrEP registered during the reporting period OR Clients with the field “Mulher Grávida” marked
   * * on the “Grupo de Risco” table on the oldest Ficha de Consulta Inicial PrEP with a Data de *
   * Início PrEP registered during the reporting period
   *
   * @param prepIncialEncounterType
   * @param initialStatusOfPrEPUserConceptId
   * @param prepTargetGroupConceptId
   * @param startDrugsConceptId
   * @param pregnantConceptId
   * @param yesConceptId
   * @param breastfeedingConceptId
   * @param prepStartDateConceptId
   * @return
   */
  public static String pregnantPatientsBasedOnPrepNew(
      int prepIncialEncounterType,
      int initialStatusOfPrEPUserConceptId,
      int prepTargetGroupConceptId,
      int startDrugsConceptId,
      int pregnantConceptId,
      int yesConceptId,
      int breastfeedingConceptId,
      int prepStartDateConceptId) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Get Clients who are pregnant Based on PrEP New A");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("80", prepIncialEncounterType);
    map.put("1982", pregnantConceptId);
    map.put("1065", yesConceptId);
    map.put("165211", prepStartDateConceptId);
    map.put("165296", initialStatusOfPrEPUserConceptId);
    map.put("1256", startDrugsConceptId);
    map.put("165196", prepTargetGroupConceptId);
    map.put("6332", breastfeedingConceptId);

    String query =
        "SELECT p.patient_id FROM patient p  "
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id  "
            + "INNER JOIN obs o ON o.encounter_id  = e.encounter_id  "
            + "INNER JOIN person pe ON pe.person_id=p.patient_id "
            + "INNER JOIN  "
            + "( "
            + " SELECT p.patient_id, MIN(e.encounter_datetime) pregnant_date "
            + " FROM patient p  "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id  "
            + " INNER JOIN obs o ON o.encounter_id  = e.encounter_id  "
            + " INNER JOIN obs o2 ON o2.encounter_id  = e.encounter_id  "
            + " WHERE p.voided  = 0  "
            + " AND e.voided = 0  "
            + " AND o.voided = 0  "
            + " AND e.location_id = :location "
            + " AND e.encounter_type = ${80} "
            + " AND ( "
            + "  ( "
            + "   ( "
            + "    (o.concept_id = ${1982} AND o.value_coded = ${1065} ) "
            + "    AND (o2.concept_id = ${165211} AND o2.value_datetime BETWEEN :startDate AND :endDate) "
            + "   ) "
            + "   OR "
            + "   ( "
            + "    (o.concept_id = ${1982} AND o.value_coded = ${1065} ) "
            + "    AND ( ( o2.concept_id = ${165296} AND o2.value_coded = ${1256} ) AND o2.obs_datetime BETWEEN :startDate AND :endDate ) "
            + "   ) "
            + "   OR "
            + "   ( "
            + "    (o.concept_id = ${165196} AND o.value_coded = ${1982} ) "
            + "    AND (o2.concept_id = ${165211} AND o2.value_datetime BETWEEN :startDate AND :endDate) "
            + "   ) "
            + "   OR "
            + "   ( "
            + "    (o.concept_id = ${165196} AND o.value_coded = ${1982} ) "
            + "    AND ( ( o2.concept_id = ${165296} AND o2.value_coded = ${1256} ) AND o2.obs_datetime BETWEEN :startDate AND :endDate ) "
            + "   ) "
            + "  ) "
            + "  OR "
            + "  ( "
            + "   ( "
            + "    (o.concept_id = ${6332} AND o.value_coded = ${1065} ) "
            + "    AND (o2.concept_id = ${165211} AND o2.value_datetime BETWEEN :startDate AND :endDate) "
            + "   ) "
            + "   OR "
            + "   ( "
            + "    (o.concept_id = ${6332} AND o.value_coded = ${1065} ) "
            + "    AND ( ( o2.concept_id = ${165296} AND o2.value_coded = ${1256} ) AND o2.obs_datetime BETWEEN :startDate AND :endDate ) "
            + "   ) "
            + "   OR "
            + "   ( "
            + "    (o.concept_id = ${165196} AND o.value_coded = ${6332} ) "
            + "    AND (o2.concept_id = 165211 AND o2.value_datetime BETWEEN :startDate AND :endDate) "
            + "   ) "
            + "   OR "
            + "   ( "
            + "    (o.concept_id = ${165196} AND o.value_coded = ${6332} ) "
            + "    AND ( ( o2.concept_id = ${165296} AND o2.value_coded = ${1256} ) AND o2.obs_datetime BETWEEN :startDate AND :endDate ) "
            + "   ) "
            + "  ) "
            + " ) "
            + " GROUP BY p.patient_id  "
            + ") pregnant "
            + "ON p.patient_id = pregnant.patient_id "
            + "WHERE p.voided  = 0  "
            + "AND e.voided = 0  "
            + "AND o.voided = 0  "
            + "AND pe.voided = 0  "
            + "AND e.location_id = :location "
            + "AND e.encounter_type = ${80} "
            + "AND pe.gender = 'F' "
            + "AND ( "
            + " (o.concept_id = ${1982} AND o.value_coded = ${1065} ) "
            + "    OR "
            + "    (o.concept_id = ${165196} AND o.value_coded = ${1982} ) "
            + ") "
            + "AND e.encounter_datetime = pregnant.pregnant_date "
            + "GROUP BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    return stringSubstitutor.replace(query);
  }

  /**
   * <b>Description:</b> Clients with the field “Se mulher, indique o estado de Gravidez/Lactação”
   * marked with value “Lactante” on the oldest Ficha de Consulta de Seguimento PrEP during the
   * reporting period OR Clients with the field “Mulher Lactante” marked on the “Grupo de Risco”
   * table on the oldest Ficha de Consulta Inicial PrEP during the reporting period
   *
   * @param prepIncialEncounterType
   * @param initialStatusOfPrEPUserConceptId
   * @param prepTargetGroupConceptId
   * @param pregnantConceptId
   * @param yesConceptId
   * @param breastfeedingConceptId
   * @param prepStartDateConceptId
   * @return
   */
  public static String breastfeedingPatientsBasedOnPrepNew(
      int prepIncialEncounterType,
      int initialStatusOfPrEPUserConceptId,
      int prepTargetGroupConceptId,
      int startDrugsConceptId,
      int pregnantConceptId,
      int yesConceptId,
      int breastfeedingConceptId,
      int prepStartDateConceptId) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Get Clients who are Breastfeeding Based on PrEP New");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("80", prepIncialEncounterType);
    map.put("1982", pregnantConceptId);
    map.put("1065", yesConceptId);
    map.put("165211", prepStartDateConceptId);
    map.put("165296", initialStatusOfPrEPUserConceptId);
    map.put("1256", startDrugsConceptId);
    map.put("165196", prepTargetGroupConceptId);
    map.put("6332", breastfeedingConceptId);

    String query =
        "SELECT p.patient_id FROM patient p "
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "INNER JOIN obs o ON o.encounter_id  = e.encounter_id "
            + "INNER JOIN person pe ON pe.person_id=p.patient_id "
            + "INNER JOIN "
            + "("
            + " SELECT p.patient_id, MIN(e.encounter_datetime) breastfeeding_date"
            + " FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + " INNER JOIN obs o ON o.encounter_id  = e.encounter_id "
            + " INNER JOIN obs o2 ON o2.encounter_id  = e.encounter_id "
            + " WHERE p.voided  = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND e.location_id = :location "
            + " AND e.encounter_type = ${80} "
            + " AND ( "
            + "  ( "
            + "   ( "
            + "    (o.concept_id = ${1982} AND o.value_coded = ${1065} ) "
            + "    AND (o2.concept_id = ${165211} AND o2.value_datetime BETWEEN :startDate AND :endDate) "
            + "   ) "
            + "   OR "
            + "   ( "
            + "    (o.concept_id = ${1982} AND o.value_coded = ${1065} ) "
            + "    AND ( ( o2.concept_id = ${165296} AND o2.value_coded = ${1256} ) AND o2.obs_datetime BETWEEN :startDate AND :endDate ) "
            + "   ) "
            + "   OR "
            + "   ( "
            + "    (o.concept_id = ${165196} AND o.value_coded = ${1982} ) "
            + "    AND (o2.concept_id = ${165211} AND o2.value_datetime BETWEEN :startDate AND :endDate) "
            + "   ) "
            + "   OR "
            + "   ( "
            + "    (o.concept_id = ${165196} AND o.value_coded = ${1982} )"
            + "    AND ( ( o2.concept_id = ${165296} AND o2.value_coded = ${1256} ) AND o2.obs_datetime BETWEEN :startDate AND :endDate ) "
            + "   ) "
            + "  ) "
            + "  OR "
            + "  ( "
            + "   ( "
            + "    (o.concept_id = ${6332} AND o.value_coded = ${1065} ) "
            + "    AND (o2.concept_id = ${165211} AND o2.value_datetime BETWEEN :startDate AND :endDate) "
            + "   ) "
            + "   OR "
            + "   ( "
            + "    (o.concept_id = ${6332} AND o.value_coded = ${1065} )"
            + "    AND ( ( o2.concept_id = ${165296} AND o2.value_coded = ${1256} ) AND o2.obs_datetime BETWEEN :startDate AND :endDate ) "
            + "   ) "
            + "   OR "
            + "   ( "
            + "    (o.concept_id = ${165196} AND o.value_coded = ${6332} ) "
            + "    AND (o2.concept_id = ${165211} AND o2.value_datetime BETWEEN :startDate AND :endDate) "
            + "   ) "
            + "   OR "
            + "   ( "
            + "    (o.concept_id = ${165196} AND o.value_coded = ${6332} ) "
            + "    AND ( ( o2.concept_id = ${165296} AND o2.value_coded = ${1256} ) AND o2.obs_datetime BETWEEN :startDate AND :endDate ) "
            + "   ) "
            + "  ) "
            + " ) "
            + " GROUP BY p.patient_id  "
            + ") breastfeeding "
            + "ON p.patient_id = breastfeeding.patient_id "
            + "WHERE p.voided  = 0  "
            + "AND e.voided = 0  "
            + "AND o.voided = 0  "
            + "AND pe.voided = 0  "
            + "AND e.location_id = :location "
            + "AND e.encounter_type = ${80} "
            + "AND pe.gender = 'F' "
            + "AND ( "
            + " (o.concept_id = ${1982} AND o.value_coded = ${1065} ) "
            + "    OR "
            + "    (o.concept_id = ${165196} AND o.value_coded = ${1982} ) "
            + ") "
            + "AND e.encounter_datetime = breastfeeding.breastfeeding_date "
            + "AND p.patient_id NOT IN ( "
            + " SELECT p.patient_id FROM patient p  "
            + "  INNER JOIN encounter e ON e.patient_id = p.patient_id  "
            + "  INNER JOIN obs o ON o.encounter_id  = e.encounter_id "
            + "    WHERE p.voided  = 0  "
            + "  AND e.voided = 0  "
            + "  AND o.voided = 0  "
            + "  AND e.location_id = :location "
            + "  AND e.encounter_type = ${80} "
            + "  AND ( "
            + "   (o.concept_id = ${1982} AND o.value_coded = ${1065} ) "
            + "   OR "
            + "   (o.concept_id = ${165196} AND o.value_coded = ${1982} ) "
            + "  ) "
            + "        AND e.encounter_datetime = breastfeeding.breastfeeding_date "
            + ") "
            + "GROUP BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    return stringSubstitutor.replace(query);
  }
}
