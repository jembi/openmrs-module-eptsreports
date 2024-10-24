package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;

public class TxPvlsBySourceQueries {

  /**
   * <b>Description:</b> Patients having viral load within the 12 months period
   *
   * @param labEncounter
   * @param fsrEncounter
   * @param vlConceptQuestion
   * @param vlQualitativeConceptQuestion
   * @return {@link String}
   */
  public static String getPatientsHavingViralLoadInLast12MonthsForLabAndFsrDenominator(
      int labEncounter, int fsrEncounter, int vlConceptQuestion, int vlQualitativeConceptQuestion) {

    Map<String, String> map = new HashMap<>();
    map.put("13", String.valueOf(labEncounter));
    map.put("51", String.valueOf(fsrEncounter));
    map.put("856", String.valueOf(vlConceptQuestion));
    map.put("1305", String.valueOf(vlQualitativeConceptQuestion));

    String query =
        "SELECT p.patient_id FROM  patient p INNER JOIN encounter e ON p.patient_id=e.patient_id INNER JOIN"
            + " obs o ON e.encounter_id=o.encounter_id "
            + " WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND "
            + " e.encounter_type IN ( ${13}, ${51}) AND "
            + " ((o.concept_id= ${856} AND o.value_numeric IS NOT NULL) OR (o.concept_id=${1305} AND o.value_coded IS NOT NULL)) AND "
            + " e.encounter_datetime BETWEEN date_add(date_add(:endDate, interval -12 MONTH), interval 1 day) AND :endDate AND "
            + " e.location_id=:location ";
    StringSubstitutor sb = new StringSubstitutor(map);
    return sb.replace(query);
  }

  /**
   * <b>Description</b>Patients with viral load suppression within 12 months
   *
   * @param labEncounter
   * @param fsrEncounter
   * @param vlConceptQuestion
   * @param vlQualitativeConceptQuestion
   * @return String
   */
  public static String getPatientsWithViralLoadSuppressionForLabAndFsrNumerator(
      int labEncounter, int fsrEncounter, int vlConceptQuestion, int vlQualitativeConceptQuestion) {
    Map<String, String> map = new HashMap<>();
    map.put("13", String.valueOf(labEncounter));
    map.put("51", String.valueOf(fsrEncounter));
    map.put("856", String.valueOf(vlConceptQuestion));
    map.put("1305", String.valueOf(vlQualitativeConceptQuestion));
    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "       INNER JOIN (SELECT e.patient_id,  MAX(DATE(e.encounter_datetime)) recent_date "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.encounter_type IN ( ${13}, ${51} ) "
            + "                          AND o.concept_id IN( ${856}, ${1305} ) "
            + "                          AND ( o.value_numeric IS NOT NULL OR o.value_coded IS NOT NULL ) "
            + "                          AND DATE(e.encounter_datetime) BETWEEN "
            + "                              DATE_ADD(DATE_ADD(:endDate, INTERVAL -12 month), INTERVAL 1 day) AND :endDate "
            + "                          AND e.location_id = :location "
            + "                   GROUP  BY e.patient_id)vl "
            + "               ON vl.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type IN ( ${13}, ${51} ) "
            + "       AND DATE(e.encounter_datetime) = vl.recent_date "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id IN ( ${856}, ${1305} ) "
            + "       AND ( o.value_numeric < 1000  OR o.value_coded IS NOT NULL  ) "
            + "       AND NOT EXISTS(SELECT en.patient_id "
            + "                      FROM   encounter en "
            + "                             INNER JOIN obs ob ON ob.encounter_id = en.encounter_id "
            + "                      WHERE  en.voided = 0 "
            + "                             AND ob.voided = 0 "
            + "                             AND en.location_id = :location "
            + "                             AND encounter_type IN ( ${13}, ${51} ) "
            + "                             AND DATE(en.encounter_datetime) = vl.recent_date "
            + "                             AND en.patient_id = p.patient_id "
            + "                             AND ob.concept_id = ${856} "
            + "                             AND ob.value_numeric >= 1000 "
            + "                      GROUP  BY en.patient_id) "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    return sb.replace(query);
  }

  /**
   * <b>Description:</b> Patients having viral load within the 12 months period
   *
   * @param adultSeguimentoEncounter
   * @param pediatriaSeguimentoEncounter
   * @param mastercardEncounter
   * @param vlConceptQuestion
   * @param vlQualitativeConceptQuestion
   * @return {@link String}
   */
  public static String getPatientsHavingViralLoadInLast12MonthsPvlsFichaMastreDenominator(
      int adultSeguimentoEncounter,
      int pediatriaSeguimentoEncounter,
      int mastercardEncounter,
      int vlConceptQuestion,
      int vlQualitativeConceptQuestion) {
    Map<String, String> map = new HashMap<>();
    map.put("6", String.valueOf(adultSeguimentoEncounter));
    map.put("9", String.valueOf(pediatriaSeguimentoEncounter));
    map.put("53", String.valueOf(mastercardEncounter));
    map.put("856", String.valueOf(vlConceptQuestion));
    map.put("1305", String.valueOf(vlQualitativeConceptQuestion));

    String query =
        "SELECT p.patient_id FROM  patient p INNER JOIN encounter e ON p.patient_id=e.patient_id INNER JOIN"
            + " obs o ON e.encounter_id=o.encounter_id "
            + " WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND "
            + " e.encounter_type IN (${6},${9}) AND "
            + " ((o.concept_id=${856} AND o.value_numeric IS NOT NULL) OR (o.concept_id=${1305} AND o.value_coded IS NOT NULL)) AND "
            + " e.encounter_datetime BETWEEN date_add(date_add(:endDate, interval -12 MONTH), interval 1 day) AND :endDate AND "
            + " e.location_id=:location "
            + " UNION "
            + " SELECT p.patient_id FROM  patient p INNER JOIN encounter e ON p.patient_id=e.patient_id INNER JOIN "
            + " obs o ON e.encounter_id=o.encounter_id "
            + " WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND "
            + " o.concept_id IN(${856}, ${1305}) "
            + " AND (o.value_numeric IS NOT NULL OR o.value_coded IS NOT NULL) AND "
            + " o.obs_datetime BETWEEN date_add(date_add(:endDate, interval -12 MONTH), interval 1 day) AND :endDate AND "
            + " e.location_id=:location ";
    StringSubstitutor sb = new StringSubstitutor(map);
    return sb.replace(query);
  }

  /**
   * <b>Description</b>Patients with viral load suppression within 12 months
   *
   * @param adultSeguimentoEncounter
   * @param pediatriaSeguimentoEncounter
   * @param mastercardEncounter
   * @param vlConceptQuestion
   * @param vlQualitativeConceptQuestion
   * @return String
   */
  public static String getPatientsWithViralLoadSuppressionPvlsFichaMastreNumerator(
      int adultSeguimentoEncounter,
      int pediatriaSeguimentoEncounter,
      int mastercardEncounter,
      int vlConceptQuestion,
      int vlQualitativeConceptQuestion) {

    Map<String, String> map = new HashMap<>();
    map.put("6", String.valueOf(adultSeguimentoEncounter));
    map.put("9", String.valueOf(pediatriaSeguimentoEncounter));
    map.put("53", String.valueOf(mastercardEncounter));
    map.put("856", String.valueOf(vlConceptQuestion));
    map.put("1305", String.valueOf(vlQualitativeConceptQuestion));
    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "       INNER JOIN (SELECT recent_vl.patient_id, MAX(recent_vl.vl_date) recent_date "
            + "                   FROM   (SELECT e.patient_id, DATE(MAX(e.encounter_datetime)) vl_date "
            + "                           FROM   encounter e "
            + "                                  INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                           WHERE  e.voided = 0 "
            + "                                  AND o.voided = 0 "
            + "                                  AND e.encounter_type IN ( ${6}, ${9} ) "
            + "                                  AND o.concept_id IN( ${856}, ${1305} ) "
            + "                                  AND ( o.value_numeric IS NOT NULL OR o.value_coded IS NOT NULL ) "
            + "                                  AND DATE(e.encounter_datetime) BETWEEN DATE_ADD(DATE_ADD(:endDate, INTERVAL -12 month), INTERVAL 1 day) AND :endDate "
            + "                                  AND e.location_id = :location "
            + "                           GROUP  BY e.patient_id "
            + "                           UNION "
            + "                           SELECT e.patient_id, DATE(MAX(o.obs_datetime)) vl_date "
            + "                           FROM   encounter e "
            + "                                  INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                           WHERE  e.voided = 0 "
            + "                                  AND o.voided = 0 "
            + "                                  AND e.encounter_type = ${53} "
            + "                                  AND o.concept_id IN( ${856}, ${1305} ) "
            + "                                  AND ( o.value_numeric IS NOT NULL OR o.value_coded IS NOT NULL ) "
            + "                                  AND DATE(o.obs_datetime) BETWEEN DATE_ADD(DATE_ADD(:endDate, INTERVAL -12 MONTH) , INTERVAL 1 day) AND :endDate "
            + "                                  AND e.location_id = :location "
            + "                           GROUP  BY e.patient_id) recent_vl "
            + "                   GROUP  BY recent_vl.patient_id) vl "
            + "               ON vl.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id IN( ${856}, ${1305} ) "
            + "       AND ( o.value_numeric < 1000  OR o.value_coded IS NOT NULL ) "
            + "       AND ( "
            + "              ( e.encounter_type IN ( ${6}, ${9}) AND DATE(e.encounter_datetime) = vl.recent_date ) "
            + "                 OR "
            + "             ( e.encounter_type = ${53} AND DATE(o.obs_datetime) = vl.recent_date ) "
            + "      ) "
            + "       AND NOT EXISTS(SELECT en.patient_id "
            + "                      FROM   encounter en "
            + "                             INNER JOIN obs ob ON ob.encounter_id = en.encounter_id "
            + "                      WHERE  en.voided = 0 "
            + "                             AND ob.voided = 0 "
            + "                             AND ob.concept_id = ${856} "
            + "                             AND ob.value_numeric >= 1000 "
            + "                             AND en.patient_id = p.patient_id "
            + "                             AND ( ( en.encounter_type IN ( ${6}, ${9} ) AND DATE(en.encounter_datetime) = vl.recent_date ) "
            + "                                    OR ( en.encounter_type = ${53} AND DATE(ob.obs_datetime) = vl.recent_date ) ) "
            + "                      GROUP  BY en.patient_id) "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    return sb.replace(query);
  }

  /**
   * <b>Description</b> Patients or routine using FSR with VL results
   *
   * @param fsrEncounter
   * @param viralLoadRequestReasonConceptId
   * @param routineViralLoadConceptId
   * @param unknownConceptId
   * @return String
   */
  public static String getPatientsHavingRoutineViralLoadTestsUsingFsr(
      int fsrEncounter,
      int viralLoadRequestReasonConceptId,
      int routineViralLoadConceptId,
      int unknownConceptId,
      int misauLaboratorioEncounterType,
      int hivViralLoadConcept,
      int hivViralLoadQualitative) {
    Map<String, String> map = new HashMap<>();
    map.put("51", String.valueOf(fsrEncounter));
    map.put("23818", String.valueOf(viralLoadRequestReasonConceptId));
    map.put("23817", String.valueOf(routineViralLoadConceptId));
    map.put("1067", String.valueOf(unknownConceptId));
    map.put("13", String.valueOf(misauLaboratorioEncounterType));
    map.put("856", String.valueOf(hivViralLoadConcept));
    map.put("1305", String.valueOf(hivViralLoadQualitative));
    String query =
        "SELECT p.patient_id  "
            + "FROM   patient p  "
            + "           INNER JOIN encounter e  "
            + "                      ON p.patient_id = e.patient_id  "
            + "           INNER JOIN obs oo  "
            + "                      ON e.encounter_id = oo.encounter_id  "
            + "           INNER JOIN (  "
            + "    SELECT p.patient_id, max(e.encounter_datetime) as most_recent  "
            + "    FROM  patient p INNER JOIN encounter e ON p.patient_id=e.patient_id INNER JOIN  "
            + "          obs o ON e.encounter_id=o.encounter_id  "
            + "    WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND  "
            + "            e.encounter_type IN ( ${13}, ${51}) AND  "
            + "        ((o.concept_id= ${856} AND o.value_numeric IS NOT NULL) OR (o.concept_id=${1305} AND o.value_coded IS NOT NULL)) AND  "
            + "            e.encounter_datetime <= :endDate AND  "
            + "            e.location_id=:location  "
            + "    group by p.patient_id  "
            + ") lastVl on lastVl.patient_id = p.patient_id  "
            + "where p.voided=0 AND e.voided=0 AND oo.voided=0  "
            + "  AND e.encounter_type = ${51}  "
            + "  AND e.encounter_datetime = lastVl.most_recent  "
            + "  AND  oo.concept_id = ${23818}  "
            + "  AND oo.value_coded IN(${23817}, ${1067})  "
            + "  AND e.location_id = :location  "
            + "  AND e.encounter_datetime between date_add(date_add(:endDate, interval -12 MONTH), interval 1 day) and :endDate  "
            + "GROUP BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    return sb.replace(query);
  }

  public static String getPatientsHavingRoutineViralLoadTestsUsinglab(
      int misauLaboratorioEncounterType,
      int hivViralLoadConcept,
      int hivViralLoadQualitative,
      int fsrEncounter) {

    Map<String, String> map = new HashMap<>();
    map.put("13", String.valueOf(misauLaboratorioEncounterType));
    map.put("856", String.valueOf(hivViralLoadConcept));
    map.put("1305", String.valueOf(hivViralLoadQualitative));
    map.put("51", String.valueOf(fsrEncounter));

    String query =
        "SELECT p.patient_id    "
            + "FROM   patient p    "
            + "           INNER JOIN encounter e    "
            + "                      ON p.patient_id = e.patient_id    "
            + "           INNER JOIN obs oo    "
            + "                      ON e.encounter_id = oo.encounter_id    "
            + "           INNER JOIN (    "
            + "    SELECT p.patient_id, max(e.encounter_datetime) as most_recent    "
            + "    FROM  patient p INNER JOIN encounter e ON p.patient_id=e.patient_id INNER JOIN    "
            + "          obs o ON e.encounter_id=o.encounter_id    "
            + "    WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND    "
            + "            e.encounter_type IN ( ${13}, ${51}) AND    "
            + "        ((o.concept_id = ${856} AND o.value_numeric IS NOT NULL) OR (o.concept_id= ${1305} AND o.value_coded IS NOT NULL)) AND    "
            + "            e.encounter_datetime <= :endDate    "
            + "      AND    "
            + "            e.location_id=:location   "
            + "    group by p.patient_id    "
            + ") lastVl on lastVl.patient_id = p.patient_id    "
            + "    "
            + "where p.voided=0 AND e.voided=0 AND oo.voided=0    "
            + "  AND e.encounter_type = ${13}    "
            + "  AND ( ( oo.concept_id = ${856}    "
            + "    AND oo.value_numeric IS NOT NULL )    "
            + "    OR ( oo.concept_id = ${1305}    "
            + "        AND oo.value_coded IS NOT NULL ) )    "
            + "  AND e.encounter_datetime = lastVl.most_recent  "
            + "  AND e.encounter_datetime between date_add(date_add(:endDate, interval -12 MONTH), interval 1 day) and :endDate   "
            + "  AND e.location_id = :location    "
            + "GROUP BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    return sb.replace(query);
  }

  /**
   * <b>Description</b> Patients on target using FSR with VL results
   *
   * @param fsrEncounter
   * @param viralLoadRequestReasonConceptId
   * @param routineViralLoadConceptId
   * @param unknownConceptId
   * @return String
   */
  public static String getPatientsOnTargetWithViralLoadTestsUsingFsr(
      int fsrEncounter,
      int viralLoadRequestReasonConceptId,
      int routineViralLoadConceptId,
      int unknownConceptId,
      int misauLaboratorioEncounterType,
      int hivViralLoadConcept,
      int hivViralLoadQualitative) {
    Map<String, String> map = new HashMap<>();
    map.put("51", String.valueOf(fsrEncounter));
    map.put("23818", String.valueOf(viralLoadRequestReasonConceptId));
    map.put("23817", String.valueOf(routineViralLoadConceptId));
    map.put("1067", String.valueOf(unknownConceptId));
    map.put("13", String.valueOf(misauLaboratorioEncounterType));
    map.put("856", String.valueOf(hivViralLoadConcept));
    map.put("1305", String.valueOf(hivViralLoadQualitative));

    String query =
        "SELECT p.patient_id  "
            + "FROM   patient p  "
            + "           INNER JOIN encounter e  "
            + "                      ON p.patient_id = e.patient_id  "
            + "           INNER JOIN obs oo  "
            + "                      ON e.encounter_id = oo.encounter_id  "
            + "           INNER JOIN (  "
            + "    SELECT p.patient_id, max(e.encounter_datetime) as most_recent  "
            + "    FROM  patient p INNER JOIN encounter e ON p.patient_id=e.patient_id INNER JOIN  "
            + "          obs o ON e.encounter_id=o.encounter_id  "
            + "    WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND  "
            + "            e.encounter_type IN ( ${13}, ${51}) AND  "
            + "        ((o.concept_id= ${856} AND o.value_numeric IS NOT NULL) OR (o.concept_id=${1305} AND o.value_coded IS NOT NULL)) AND  "
            + "            e.encounter_datetime <= :endDate AND  "
            + "            e.location_id=:location  "
            + "    group by p.patient_id  "
            + ") lastVl on lastVl.patient_id = p.patient_id  "
            + "where p.voided=0 AND e.voided=0 AND oo.voided=0  "
            + "  AND e.encounter_type = ${51}  "
            + "  AND e.encounter_datetime = lastVl.most_recent  "
            + "  AND  oo.concept_id = ${23818}  "
            + "  AND oo.value_coded NOT IN(${23817}, ${1067})  "
            + "  AND e.location_id = :location  "
            + "  AND e.encounter_datetime between date_add(date_add(:endDate, interval -12 MONTH), interval 1 day) and :endDate  "
            + "GROUP BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    return sb.replace(query);
  }

  /**
   * <b>Description</b> Patients or routine using FSR with VL results
   *
   * @param adultoEncounter
   * @param paedEncounter
   * @param resumoEncounter
   * @param vlConceptQuestion
   * @param vlQualitativeConceptQuestion
   * @return String
   */
  public static String getPatientsHavingRoutineViralLoadTestsUsingClinicalForms(
      int adultoEncounter,
      int paedEncounter,
      int resumoEncounter,
      int vlConceptQuestion,
      int vlQualitativeConceptQuestion) {
    Map<String, String> map = new HashMap<>();
    map.put("6", String.valueOf(adultoEncounter));
    map.put("9", String.valueOf(paedEncounter));
    map.put("53", String.valueOf(resumoEncounter));
    map.put("856", String.valueOf(vlConceptQuestion));
    map.put("1305", String.valueOf(vlQualitativeConceptQuestion));
    String query =
        "SELECT p.patient_id FROM  patient p INNER JOIN encounter e ON p.patient_id=e.patient_id INNER JOIN"
            + " obs o ON e.encounter_id=o.encounter_id "
            + " WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND "
            + " e.encounter_type IN (${6},${9}) AND "
            + " ((o.concept_id=${856} AND o.value_numeric IS NOT NULL) OR (o.concept_id=${1305} AND o.value_coded IS NOT NULL)) AND "
            + " e.encounter_datetime <=:endDate AND "
            + " e.location_id=:location "
            + " UNION "
            + " SELECT p.patient_id FROM  patient p INNER JOIN encounter e ON p.patient_id=e.patient_id INNER JOIN "
            + " obs o ON e.encounter_id=o.encounter_id "
            + " WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND "
            + " e.encounter_type IN (${53}) AND ((o.concept_id=${856} AND o.value_numeric IS NOT NULL) OR (o.concept_id=${1305} AND o.value_coded IS NOT NULL)) AND "
            + " o.obs_datetime <=:endDate AND "
            + " e.location_id=:location ";

    StringSubstitutor sb = new StringSubstitutor(map);
    return sb.replace(query);
  }
}
