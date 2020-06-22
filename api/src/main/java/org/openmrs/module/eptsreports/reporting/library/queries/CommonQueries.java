package org.openmrs.module.eptsreports.reporting.library.queries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringSubstitutor;

public class CommonQueries {

  /**
   * Patients on TB Treatment
   *
   * @param adultoSeguimentoEncounterTypeId
   * @param arvPediatriaSeguimentoEncounterTypeId
   * @param tbStartDateConceptId
   * @param tbEndDateConceptId
   * @param tbProgramId
   * @param patientStateId
   * @param activeTBConceptId
   * @param yesConceptId
   * @param tbTreatmentPlanConceptId
   * @param startDrugsConceptId
   * @param continueRegimenConceptId
   */
  public static String getPatientsOnTbTreatmentQuery(
      int adultoSeguimentoEncounterTypeId,
      int arvPediatriaSeguimentoEncounterTypeId,
      int tbStartDateConceptId,
      int tbEndDateConceptId,
      int tbProgramId,
      int patientStateId,
      int activeTBConceptId,
      int yesConceptId,
      int tbTreatmentPlanConceptId,
      int startDrugsConceptId,
      int continueRegimenConceptId) {
    Map<String, Integer> map = new HashMap<>();
    map.put("adultoSeguimentoEncounterTypeId", adultoSeguimentoEncounterTypeId);
    map.put("arvPediatriaSeguimentoEncounterTypeId", arvPediatriaSeguimentoEncounterTypeId);
    map.put("tbStartDateConceptId", tbStartDateConceptId);
    map.put("tbEndDateConceptId", tbEndDateConceptId);
    map.put("tbProgramId", tbProgramId);
    map.put("patientStateId", patientStateId);
    map.put("activeTBConceptId", activeTBConceptId);
    map.put("yesConceptId", yesConceptId);
    map.put("tbTreatmentPlanConceptId", tbTreatmentPlanConceptId);
    map.put("startDrugsConceptId", startDrugsConceptId);
    map.put("continueRegimenConceptId", continueRegimenConceptId);

    String query =
        "SELECT p.patient_id "
            + "FROM patient p "
            + "JOIN encounter e ON p.patient_id = e.patient_id "
            + "JOIN obs "
            + "start ON e.encounter_id = start.encounter_id "
            + "WHERE e.encounter_type IN (${adultoSeguimentoEncounterTypeId}, "
            + "                           ${arvPediatriaSeguimentoEncounterTypeId}) "
            + "  AND start.concept_id = ${tbStartDateConceptId} "
            + "  AND start.value_datetime IS NOT NULL "
            + "  AND start.value_datetime BETWEEN   :startDate AND   :endDate "
            + "  AND e.location_id=  :location  "
            + "  AND p.patient_id NOT IN "
            + "    (SELECT p1.patient_id "
            + "     FROM patient p1 "
            + "     JOIN encounter e1 ON p1.patient_id = e1.patient_id "
            + "     JOIN obs o ON e1.encounter_id = o.encounter_id "
            + "     WHERE e1.encounter_type IN (${adultoSeguimentoEncounterTypeId}, "
            + "                                 ${arvPediatriaSeguimentoEncounterTypeId}) "
            + "       AND o.concept_id = ${tbEndDateConceptId} "
            + "       AND (o.value_datetime IS NOT NULL "
            + "            AND o.value_datetime <=   :endDate) "
            + "       AND o.value_datetime > start.value_datetime "
            + "       AND e.location_id=  :location  ) "
            + "UNION   "
            + "SELECT p.patient_id "
            + "FROM patient p "
            + "JOIN patient_program pp ON p.patient_id=pp.patient_id "
            + "JOIN patient_state ps ON pp.patient_program_id=ps.patient_program_id "
            + "WHERE pp.program_id=${tbProgramId} "
            + "  AND ps.state=${patientStateId} "
            + "  AND pp.location_id=  :location  "
            + "  AND pp.date_enrolled BETWEEN   :startDate AND   :endDate "
            + "  AND (pp.date_completed IS NULL "
            + "       OR pp.date_completed >   :endDate) "
            + "  AND p.voided=0 "
            + "  AND ps.voided=0 "
            + "UNION  "
            + "SELECT p.patient_id "
            + "FROM patient p "
            + "    INNER JOIN encounter e  "
            + "        ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o  "
            + "        ON e.encounter_id = o.encounter_id "
            + "    INNER JOIN (SELECT p.patient_id, "
            + "            max(e.encounter_datetime) encounter_datetime "
            + "        FROM patient p "
            + "        INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "        INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "        WHERE o.concept_id = ${activeTBConceptId} "
            + "            AND e.location_id =   :location  "
            + "            AND e.encounter_type IN (${adultoSeguimentoEncounterTypeId},${arvPediatriaSeguimentoEncounterTypeId}) "
            + "            AND e.encounter_datetime BETWEEN date_sub(  :endDate, INTERVAL 6 MONTH) AND   :endDate "
            + "            AND p.voided = 0 "
            + "            and e.voided = 0 "
            + "            and o.voided = 0 "
            + "        GROUP BY p.patient_id) last  "
            + "                ON p.patient_id = last.patient_id "
            + "    AND e.encounter_datetime = last.encounter_datetime "
            + "WHERE o.value_coded = ${yesConceptId} "
            + "    AND o.concept_id = ${activeTBConceptId} "
            + "    AND e.encounter_type IN (${adultoSeguimentoEncounterTypeId},${arvPediatriaSeguimentoEncounterTypeId}) "
            + "    AND e.voided=0 "
            + "    AND o.voided=0 "
            + "    AND p.voided=0 "
            + "UNION "
            + "SELECT p.patient_id "
            + "FROM patient p "
            + "    INNER JOIN encounter e  "
            + "        ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o  "
            + "        ON e.encounter_id = o.encounter_id "
            + "JOIN "
            + "  (SELECT p.patient_id, "
            + "          max(o.obs_datetime) obs_datetime "
            + "   FROM patient p "
            + "   JOIN encounter e ON p.patient_id = e.patient_id "
            + "   JOIN obs o ON e.encounter_id = o.encounter_id "
            + "   WHERE o.concept_id = ${tbTreatmentPlanConceptId} "
            + "     AND e.location_id =   :location  "
            + "     AND e.encounter_type IN (${adultoSeguimentoEncounterTypeId}, "
            + "                              ${arvPediatriaSeguimentoEncounterTypeId}) "
            + "     AND o.obs_datetime <=  :endDate "
            + "     AND p.voided=0 "
            + "     AND e.voided=0 "
            + "     AND o.voided=0 "
            + "   GROUP BY p.patient_id) last ON p.patient_id = last.patient_id "
            + "AND o.obs_datetime = last.obs_datetime "
            + "WHERE o.value_coded IN(${startDrugsConceptId},${continueRegimenConceptId}) "
            + "  AND o.concept_id = ${tbTreatmentPlanConceptId} "
            + "  AND e.encounter_type IN (${adultoSeguimentoEncounterTypeId},${arvPediatriaSeguimentoEncounterTypeId}) "
            + "  AND e.voided=0 "
            + "  AND o.voided=0 "
            + "  AND p.voided=0 "
            + "  AND e.location_id =   :location ; "
            + "";

    StringSubstitutor sb = new StringSubstitutor(map);
    String replaced = sb.replace(query);

    return replaced;
  }

  /**
   * Gets last obs with value coded before enDate
   *
   * @param encounterTypeId The Obs encounter Type
   * @param question The Obs quetion concept
   * @param answers The third value coded
   * @return String
   */
  public static String getLastCodedObsBeforeDate(
      List<Integer> encounterTypes, Integer question, List<Integer> answers) {

    Map<String, String> map = new HashMap<>();

    map.put("encounterTypes", StringUtils.join(encounterTypes, ","));
    map.put("question", String.valueOf(question));
    map.put("answers", StringUtils.join(answers, ","));

    String query =
        "SELECT patient_id FROM ( "
            + "  SELECT p.patient_id, max(e.encounter_datetime) datetime "
            + "  FROM patient p "
            + "  INNER JOIN encounter e "
            + "  ON p.patient_id = e.patient_id "
            + "  INNER JOIN obs o "
            + "  ON e.encounter_id = o.encounter_id "
            + "  WHERE e.location_id = :location AND e.encounter_type IN (${encounterTypes}) "
            + "  AND o.concept_id = ${question} AND o.value_coded IN (${answers}) "
            + "  AND e.encounter_datetime <= :onOrBefore  "
            + "  AND p.voided = 0 AND e.voided = 0 AND o.voided = 0 "
            + "  GROUP BY p.patient_id ) AS last_encounter";

    StringSubstitutor sb = new StringSubstitutor(map);
    String replaced = sb.replace(query);

    return replaced;
  }



  /**
   * Gets last obs with value coded before enDate
   *
   * @param encounterTypeId The Obs encounter Type
   * @param question The Obs quetion concept
   * @param answers The third value coded
   * @return String
   */
  public static String getLastCodedObservationBeforeDate(
          List<Integer> encounterTypes, Integer question, List<Integer> answers) {

    Map<String, String> map = new HashMap<>();

    map.put("encounterTypes", StringUtils.join(encounterTypes, ","));
    map.put("question", String.valueOf(question));
    map.put("answers", StringUtils.join(answers, ","));

    String query = " SELECT patient_id  "
            +"FROM   (SELECT p.patient_id,  "
            +"               (SELECT e.encounter_id  "
            +"                FROM   encounter e  "
            +"                       INNER JOIN obs o  "
            +"                               ON e.encounter_id = o.encounter_id  "
            +"                                  AND e.location_id = :location  "
            +"                                  AND e.encounter_type IN (${encounterTypes})  "
            +"                                  AND o.concept_id = ${question}  "
            +"                                  AND e.encounter_datetime <= :onOrBefore  "
            +"                                  AND e.voided = 0  "
            +"                                  AND o.voided = 0  "
            +"                WHERE  e.patient_id = p.patient_id  "
            +"                ORDER  BY o.obs_id DESC  "
            +"                LIMIT  1) last_encounter_of_question  "
            +"        FROM   patient p  "
            +"        WHERE  p.voided = 0) base_tbl  "
            +"       JOIN (SELECT obs.encounter_id  "
            +"             FROM   obs  "
            +"             WHERE  obs.concept_id = ${question}  "
            +"                    AND obs.value_coded IN (${answers})  "
            +"                    AND obs.location_id =:location  "
            +"                    AND obs.voided = 0) AS answers_tbl  "
            +"         ON base_tbl.last_encounter_of_question = answers_tbl.encounter_id  "
            +"GROUP  BY patient_id  ";
            

    StringSubstitutor sb = new StringSubstitutor(map);
    String replaced = sb.replace(query);

    return replaced;
  }
}




