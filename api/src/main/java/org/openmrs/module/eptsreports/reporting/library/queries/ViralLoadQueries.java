/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.eptsreports.reporting.library.queries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.EncounterType;
import org.openmrs.module.eptsreports.metadata.HivMetadata;

public class ViralLoadQueries {

  /**
   * <b>Description</b>Patients with viral load suppression within 12 months
   *
   * @param labEncounter_13
   * @param adultSeguimentoEncounter_6
   * @param pediatriaSeguimentoEncounter_9
   * @param mastercardEncounter_53
   * @param fsrEncounter_51
   * @param vlConceptQuestion_856
   * @param vlQualitativeConceptQuestion_1305
   * @return String
   */
  public static String getPatientsWithViralLoadSuppression() {

    HivMetadata hivMetadata = new HivMetadata();
    Map<String, Integer> map = new HashMap<>();

    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

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
            + "                                  AND e.encounter_type IN ( ${6}, ${9}, ${13}, ${51} ) "
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
            + "              ( e.encounter_type IN ( ${6}, ${9}, ${13}, ${51} ) AND DATE(e.encounter_datetime) = vl.recent_date ) "
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
            + "                             AND ( ( en.encounter_type IN ( ${6}, ${9}, ${13}, ${51} ) AND DATE(en.encounter_datetime) = vl.recent_date ) "
            + "                                    OR ( en.encounter_type = ${53} AND DATE(ob.obs_datetime) = vl.recent_date ) ) "
            + "                      GROUP  BY en.patient_id) "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    return sb.replace(query);
  }

  /**
   * <b>Description:</b> Patients having viral load within the 12 months period
   *
   * @param labEncounter
   * @param adultSeguimentoEncounter
   * @param pediatriaSeguimentoEncounter
   * @param mastercardEncounter
   * @param fsrEncounter
   * @param vlConceptQuestion
   * @param vlQualitativeConceptQuestion
   * @return {@link String}
   */
  public static String getPatientsHavingViralLoadInLast12Months(
      List<EncounterType> encounterTypeList) {

    HivMetadata hivMetadata = new HivMetadata();
    Map<String, Integer> map = new HashMap<>();

    List<Integer> encountersId =
        encounterTypeList.stream()
            .map(encounterType -> encounterType.getEncounterTypeId())
            .collect(Collectors.toList());
    List<Integer> notEncounter53 =
        encountersId.stream()
            .filter(e -> e != hivMetadata.getMasterCardEncounterType().getEncounterTypeId())
            .collect(Collectors.toList());
    List<Integer> encounter53 =
        encountersId.stream()
            .filter(e -> e == hivMetadata.getMasterCardEncounterType().getEncounterTypeId())
            .collect(Collectors.toList());

    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query = "";

    if (!encounter53.isEmpty()) {
      query =
          " SELECT p.patient_id, DATE(o.obs_datetime) vl_date FROM  patient p "
              + " INNER JOIN encounter e ON p.patient_id=e.patient_id "
              + " INNER JOIN obs o ON e.encounter_id=o.encounter_id "
              + " WHERE p.voided=0 "
              + " AND e.voided=0 "
              + " AND o.voided=0 "
              + " AND e.encounter_type IN ("
              + StringUtils.join(encounter53, ",")
              + ")"
              + " AND ((o.concept_id=${856} AND o.value_numeric IS NOT NULL) OR (o.concept_id=${1305} AND o.value_coded IS NOT NULL)) "
              + "AND DATE(o.obs_datetime) BETWEEN date_add(date_add(:endDate, interval -12 MONTH), interval 1 day) AND :endDate "
              + " AND e.location_id=:location ";
    }
    if (!notEncounter53.isEmpty() && !encounter53.isEmpty()) {
      query += " UNION ";
    }
    if (!notEncounter53.isEmpty()) {
      query +=
          " SELECT p.patient_id, DATE(e.encounter_datetime) vl_date FROM  patient p"
              + " INNER JOIN encounter e ON p.patient_id=e.patient_id "
              + " INNER JOIN obs o ON e.encounter_id=o.encounter_id "
              + " WHERE p.voided=0 "
              + " AND e.voided=0 "
              + " AND o.voided=0 "
              + " AND e.encounter_type IN ("
              + StringUtils.join(notEncounter53, ",")
              + ")"
              + " AND ((o.concept_id=${856} AND o.value_numeric IS NOT NULL) OR (o.concept_id=${1305} AND o.value_coded IS NOT NULL)) "
              + " AND DATE(e.encounter_datetime) BETWEEN date_add(date_add(:endDate, interval -12 MONTH), interval 1 day) AND :endDate AND "
              + " e.location_id=:location ";
    }

    StringSubstitutor sb = new StringSubstitutor(map);
    return sb.replace(query);
  }

  /**
   * <b>Description:</b> Patients having viral load within the 12 months period
   *
   * @param labEncounter
   * @param adultSeguimentoEncounter
   * @param pediatriaSeguimentoEncounter
   * @param mastercardEncounter
   * @param fsrEncounter
   * @param vlConceptQuestion
   * @param vlQualitativeConceptQuestion
   * @return {@link String}
   */
  public static String getPatientsHavingViralLoadInLast12MonthsBySource() {

    HivMetadata hivMetadata = new HivMetadata();
    Map<String, Integer> map = new HashMap<>();

    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        "SELECT p.patient_id FROM  patient p "
            + " INNER JOIN encounter e ON p.patient_id=e.patient_id "
            + " INNER JOIN obs o ON e.encounter_id=o.encounter_id "
            + " WHERE p.voided=0 "
            + " AND e.voided=0 "
            + " AND o.voided=0 "
            + " AND e.encounter_type IN (${6}, ${9}) "
            + " AND ((o.concept_id=${856} AND o.value_numeric IS NOT NULL) OR (o.concept_id=${1305} AND o.value_coded IS NOT NULL)) "
            + " AND DATE(e.encounter_datetime) BETWEEN date_add(date_add(:endDate, interval -12 MONTH), interval 1 day) AND :endDate AND "
            + " e.location_id=:location "
            + " UNION "
            + " SELECT p.patient_id FROM  patient p "
            + " INNER JOIN encounter e ON p.patient_id=e.patient_id "
            + " INNER JOIN obs o ON e.encounter_id=o.encounter_id "
            + " WHERE p.voided=0 "
            + " AND e.voided=0 "
            + " AND o.voided=0 "
            + " AND e.encounter_type IN (${53}) "
            + " AND ((o.concept_id=${856} AND o.value_numeric IS NOT NULL) OR (o.concept_id=${1305} AND o.value_coded IS NOT NULL)) "
            + "AND DATE(o.obs_datetime) BETWEEN date_add(date_add(:endDate, interval -12 MONTH), interval 1 day) AND :endDate "
            + " AND e.location_id=:location ";

    StringSubstitutor sb = new StringSubstitutor(map);
    return sb.replace(query);
  }

  /**
   * <b>Description</b> Patients or routine using FSR with VL results
   *
   * @return String
   */
  public static String getPatientsHavingRoutineViralLoadTestsUsingFsr() {
    return "SELECT p.patient_id "
        + "FROM   patient p "
        + "       INNER JOIN encounter e "
        + "               ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o "
        + "               ON e.encounter_id = o.encounter_id "
        + "       INNER JOIN obs o2 "
        + "               ON e.encounter_id = o2.encounter_id "
        + "       INNER JOIN (SELECT max_vl_result.patient_id, "
        + "                          Max(max_vl_result.max_vl) last_vl "
        + "                   FROM   (SELECT p.patient_id, "
        + "                                  Date(e.encounter_datetime) AS max_vl "
        + "                           FROM   patient p "
        + "                                  INNER JOIN encounter e "
        + "                                          ON p.patient_id = e.patient_id "
        + "                                  INNER JOIN obs o "
        + "                                          ON e.encounter_id = o.encounter_id "
        + "                           WHERE  p.voided = 0 "
        + "                                  AND e.voided = 0 "
        + "                                  AND o.voided = 0 "
        + "                                  AND e.encounter_type IN ( ${6}, ${9}, ${13}, ${51} ) "
        + "                                  AND ( ( o.concept_id = ${856} "
        + "                                          AND o.value_numeric IS NOT NULL ) "
        + "                                         OR ( o.concept_id = ${1305} "
        + "                                              AND o.value_coded IS NOT NULL ) ) "
        + "                                  AND Date(e.encounter_datetime) <= :endDate "
        + "                                  AND e.location_id = :location "
        + "                           UNION "
        + "                           SELECT p.patient_id, "
        + "                                  Date(o.obs_datetime) AS max_vl "
        + "                           FROM   patient p "
        + "                                  INNER JOIN encounter e "
        + "                                          ON p.patient_id = e.patient_id "
        + "                                  INNER JOIN obs o "
        + "                                          ON e.encounter_id = o.encounter_id "
        + "                           WHERE  p.voided = 0 "
        + "                                  AND e.voided = 0 "
        + "                                  AND o.voided = 0 "
        + "                                  AND e.encounter_type IN ( ${53} ) "
        + "                                  AND ( ( o.concept_id = ${856} "
        + "                                          AND o.value_numeric IS NOT NULL ) "
        + "                                         OR ( o.concept_id = ${1305} "
        + "                                              AND o.value_coded IS NOT NULL ) ) "
        + "                                  AND Date(o.obs_datetime) <= :endDate "
        + "                                  AND e.location_id = :location) max_vl_result "
        + "                   GROUP  BY max_vl_result.patient_id) last_date "
        + "               ON p.patient_id = last_date.patient_id "
        + "WHERE  p.voided = 0 "
        + "       AND e.voided = 0 "
        + "       AND o.voided = 0 "
        + "       AND ( ( e.encounter_type IN ( ${6}, ${9}, ${13} ) "
        + "               AND ( ( o.concept_id = ${856} "
        + "                       AND o.value_numeric IS NOT NULL ) "
        + "                      OR ( o.concept_id = ${1305} "
        + "                           AND o.value_coded IS NOT NULL ) ) "
        + "               AND Date(e.encounter_datetime) = last_date.last_vl ) "
        + "              OR ( e.encounter_type = ${53} "
        + "                   AND ( ( o.concept_id = ${856} "
        + "                           AND o.value_numeric IS NOT NULL ) "
        + "                          OR ( o.concept_id = ${1305} "
        + "                               AND o.value_coded IS NOT NULL ) ) "
        + "                   AND Date(o.obs_datetime) = last_date.last_vl ) "
        + "              OR ( e.encounter_type = ${51} "
        + "                   AND ( ( ( o.concept_id = ${856} "
        + "                             AND o.value_numeric IS NOT NULL ) "
        + "                            OR ( o.concept_id = ${1305} "
        + "                                 AND o.value_coded IS NOT NULL ) ) "
        + "                         AND ( o2.concept_id = ${23818} "
        + "                               AND o2.value_coded IN ( ${23817}, ${1067} ) "
        + "                               AND o2.voided = 0 ) ) "
        + "                   AND Date(e.encounter_datetime) = last_date.last_vl ) ) "
        + "GROUP  BY p.patient_id";
  }

  /**
   * <b>Descritpion</b>
   *
   * <p>Patients whose age is depended on the Viral load - VL date and DOB difference will result
   * into age required
   */
  public static String getPatientAgeBasedOnFirstViralLoadDate(
      int conceptId, int encounterType, int masterCardEncounterType, int minAge, int maxAge) {
    Map<String, Integer> map = new HashMap<>();
    map.put("856", conceptId);
    map.put("6", encounterType);
    map.put("53", masterCardEncounterType);
    map.put("minAge", minAge);
    map.put("maxAge", maxAge);
    String query =
        "SELECT patient_id "
            + "FROM   ( "
            + "         SELECT     pat.patient_id, Timestampdiff(year, pn.birthdate, encounter_datetime) AS age "
            + "         FROM       patient pat "
            + "            INNER JOIN person pn ON pat.patient_id=pn.person_id "
            + "            INNER JOIN "
            + "            ( "
            + "                SELECT   p.patient_id, Min(e.encounter_datetime) AS encounter_datetime "
            + "                FROM  patient p "
            + "                    INNER JOIN encounter e ON p.patient_id=e.patient_id "
            + "                    INNER JOIN obs o ON e.encounter_id=o.encounter_id "
            + "                WHERE   o.value_numeric IS NOT NULL "
            + "                    AND p.voided = 0 "
            + "                    AND e.voided = 0 "
            + "                    AND o.voided = 0 "
            + "                    AND e.location_id = :location "
            + "                    AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "                    AND e.encounter_type = ${6} "
            + "                    AND o.concept_id = ${856} "
            + "                    AND o.value_numeric >= 1000 "
            + "                GROUP BY patient_id "
            + "                UNION "
            + "                SELECT   p.patient_id, Min(o.obs_datetime) AS encounter_datetime "
            + "                FROM  patient p "
            + "                    INNER JOIN encounter e ON p.patient_id=e.patient_id "
            + "                    INNER JOIN obs o ON e.encounter_id=o.encounter_id "
            + "                WHERE   o.value_numeric IS NOT NULL "
            + "                    AND   p.voided = 0 "
            + "                    AND   e.voided = 0 "
            + "                    AND   o.voided = 0 "
            + "                    AND   e.location_id = :location "
            + "                    AND o.obs_datetime BETWEEN :startDate AND :endDate "
            + "                    AND e.encounter_type = ${53} "
            + "                    AND o.concept_id = ${856} "
            + "                    AND o.value_numeric >= 1000 "
            + "             GROUP BY patient_id "
            + "         ) ex ON pat.patient_id=ex.patient_id ) fin "
            + "WHERE  age BETWEEN ${minAge} AND ${maxAge} ";
    StringSubstitutor sb = new StringSubstitutor(map);
    return sb.replace(query);
  }

  /**
   * <b>Description:</b> todos os utentes que têm o último registo de pelo menos um dos seguintes
   * modelos na última consulta clínica (Ficha Clínica) antes da data do resultado da CV mais
   * recente (“Data Última CV”), como um dos seguintes:
   *
   * <ul>
   *   <li>Último registo de MDC (MDC1 ou MDC2 ou MDC3 ou MDC4 ou MDC5) como “DT” e o respectivo
   *       “Estado” = “Início” ou “Continua”, ou último registo do “Tipo de Dispensa” = “DT” na
   *       Ficha Clínica ou
   *   <li>Último registo de MDC (MDC1 ou MDC2 ou MDC3 ou MDC4 ou MDC5) como “DS” e o respectivo
   *       “Estado” = “Início” ou “Continua”, ou último registo do “Tipo de Dispensa” = “DS” na
   *       Ficha Clínica ou
   * </ul>
   *
   * @return {@link String}
   */
  public static String getPatientsHavingTypeOfDispensationBasedOnTheirLastVlResults() {

    HivMetadata hivMetadata = new HivMetadata();
    Map<String, Integer> map = new HashMap<>();

    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());
    map.put("23739", hivMetadata.getTypeOfDispensationConcept().getConceptId());
    map.put("23720", hivMetadata.getQuarterlyConcept().getConceptId());
    map.put("23888", hivMetadata.getSemiannualDispensation().getConceptId());

    String query =
        "SELECT out_p.patient_id "
            + "FROM   patient pp "
            + "       INNER JOIN encounter ep ON pp.patient_id = ep.patient_id "
            + "       INNER JOIN obs op ON ep.encounter_id = op.encounter_id "
            + "       INNER JOIN (SELECT patient_id, MAX(encounter_datetime) AS max_vl_date_and_max_ficha "
            + "                   FROM   (SELECT pp.patient_id, ee.encounter_datetime "
            + "                           FROM   patient pp "
            + "                                  INNER JOIN encounter ee ON pp.patient_id = ee.patient_id "
            + "                                  INNER JOIN obs oo ON ee.encounter_id = oo.encounter_id "
            + "                                  INNER JOIN (SELECT patient_id, DATE( Max(encounter_date)) AS vl_max_date "
            + "                                              FROM   (SELECT p.patient_id, DATE(e.encounter_datetime) AS encounter_date "
            + "                                                      FROM   patient p "
            + "                                                      INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                                                      INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                                                      WHERE  p.voided = 0 "
            + "                                                       AND e.voided = 0 "
            + "                                                       AND o.voided = 0 "
            + "                                                       AND e.encounter_type IN ( ${13}, ${6}, ${9}, ${51} ) "
            + "                                                       AND ( ( o.concept_id = ${856} AND o.value_numeric IS NOT  NULL ) "
            + "                                                             OR ( o.concept_id = ${1305}  AND o.value_coded IS NOT NULL ) ) "
            + "                                                       AND DATE(e.encounter_datetime) BETWEEN :startDate AND :endDate "
            + "                                                       AND e.location_id = :location "
            + "                                               UNION "
            + "                                               SELECT p.patient_id, DATE(o.obs_datetime) AS encounter_date "
            + "                                               FROM   patient p "
            + "                                               INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                                               INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                                               WHERE  p.voided = 0 "
            + "                                                 AND e.voided = 0 "
            + "                                                 AND o.voided = 0 "
            + "                                                 AND e.encounter_type IN ( ${53} ) "
            + "                                                 AND ((o.concept_id=${856} AND o.value_numeric IS NOT NULL)"
            + "                                                                OR (o.concept_id=${1305} AND o.value_coded IS NOT NULL)) "
            + "                                                 AND e.location_id = :location) max_vl_date "
            + "                                                 GROUP  BY patient_id"
            + "                   ) vl_date_tbl ON pp.patient_id = vl_date_tbl.patient_id "
            + "                 WHERE  ee.encounter_datetime < vl_date_tbl.vl_max_date "
            + "                 AND oo.voided = 0 "
            + "                 AND ee.voided = 0   "
            + "                 AND ee.location_id = :location   "
            + "                 AND ee.encounter_type = ${6}) fin_tbl "
            + "                 GROUP  BY patient_id) out_p ON pp.patient_id = out_p.patient_id "
            + "WHERE  ep.encounter_type = ${6} "
            + "       AND op.voided = 0 "
            + "       AND ep.voided = 0 "
            + "       AND pp.voided = 0 "
            + "       AND ep.location_id = :location   "
            + "       AND ep.encounter_datetime = max_vl_date_and_max_ficha "
            + "       AND op.concept_id = ${23739} "
            + "       AND op.value_coded IN( ${23720}, ${23888} )";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    return stringSubstitutor.replace(query);
  }
}
