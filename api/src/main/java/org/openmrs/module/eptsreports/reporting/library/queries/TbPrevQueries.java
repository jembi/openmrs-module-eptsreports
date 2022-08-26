package org.openmrs.module.eptsreports.reporting.library.queries;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Concept;
import org.openmrs.EncounterType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TbPrevQueries {

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

  /**
   * Patients who have Regime de TPT with the values “3HP or 3HP + Piridoxina” and “Seguimento de
   * Tratamento TPT” with values “Continua” or “Fim” or no value marked on the first pick-up date on
   * Ficha de Levantamento de TPT (FILT) during the previous reporting period (3HP Start Date)
   */
  public static String regimeTPT3hpOr3hpPiridoxinaQuery() {
    return " SELECT  p.patient_id, MIN(o2.obs_datetime) first_pickup_date "
        + "                      FROM    patient p "
        + "                                  INNER JOIN encounter e ON e.patient_id = p.patient_id "
        + "                                  INNER JOIN obs o ON o.encounter_id = e.encounter_id "
        + "                                  INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
        + "                      WHERE   p.voided = 0 "
        + "                        AND e.voided = 0 "
        + "                        AND o.voided = 0 "
        + "                        AND e.location_id = :location "
        + "                        AND e.encounter_type = ${60} "
        + "                        AND o.concept_id = ${23985} "
        + "                        AND o.value_coded IN (${23954},${23984}) "
        + "                        AND ((o2.concept_id =${23987} AND o2.value_coded IN (${1257},${1267})) OR o2.concept_id IS NULL) "
        + "                        AND o2.obs_datetime >= :onOrAfter "
        + "                        AND o2.obs_datetime <= :onOrBefore "
        + "                      GROUP BY p.patient_id "
        + "                      UNION "
        + "                      SELECT  p.patient_id, MIN(o2.obs_datetime) first_pickup_date "
        + "                      FROM    patient p "
        + "                                  INNER JOIN encounter e ON e.patient_id = p.patient_id "
        + "                                  INNER JOIN obs o ON o.encounter_id = e.encounter_id "
        + "                                  INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
        + "                      WHERE   p.voided = 0 "
        + "                        AND e.voided = 0 "
        + "                        AND o.voided = 0 "
        + "                        AND o2.voided = 0 "
        + "                        AND e.location_id = :location "
        + "                        AND e.encounter_type = ${60} "
        + "                        AND (o.concept_id = ${23985} AND o.value_coded IN (${1257},${1267})) "
        + "                        AND (o2.concept_id NOT IN (SELECT o.concept_id FROM encounter e "
        + "                                                      INNER JOIN obs o ON o.encounter_id = e.encounter_id "
        + "                                                   WHERE e.patient_id = p.patient_id AND e.encounter_type = ${60} AND  o.concept_id = ${23987})) "
        + "                        AND o2.obs_datetime >= :onOrAfter "
        + "                        AND o2.obs_datetime <= :onOrBefore "
        + "                      GROUP BY p.patient_id ";
  }
}
