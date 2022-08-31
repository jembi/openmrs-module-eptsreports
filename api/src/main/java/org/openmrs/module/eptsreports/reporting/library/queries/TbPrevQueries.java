package org.openmrs.module.eptsreports.reporting.library.queries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.module.eptsreports.reporting.utils.EptsQueriesUtil;
import org.openmrs.module.eptsreports.reporting.utils.queries.UnionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TbPrevQueries {

    @Autowired
    private CommonQueries commonQueries;
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

   public String get3HPStartOnFichaResumo(){
     return
             "SELECT p.patient_id, o2.obs_datetime AS start_date "
                     + "FROM   patient p "
                     + "       INNER JOIN encounter e ON p.patient_id = e.patient_id "
                     + "       INNER JOIN obs o ON e.encounter_id = o.encounter_id "
                     + "       INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id "
                     + "WHERE  p.voided = 0 AND e.voided = 0 AND o.voided = 0 AND o2.voided = 0"
                     + "       AND e.location_id = :location "
                     + "       AND e.encounter_type = ${53} "
                     + "       AND ( (o.concept_id = ${23985} AND o.value_coded = ${23954}) "
                     + "        AND (o2.concept_id = ${165308} AND o2.value_coded = ${1256} "
                     + "        AND o2.obs_datetime BETWEEN DATE_SUB(:startDate, INTERVAL 6 MONTH) AND DATE_SUB(:endDate, INTERVAL 6 MONTH) ) )";
   }

   public String getStartDateOf3HPOnFichaClinica(){
      return
              "SELECT p.patient_id, o2.obs_datetime AS start_date "
                      + "FROM   patient p "
                      + "       INNER JOIN encounter e ON p.patient_id = e.patient_id "
                      + "       INNER JOIN obs o ON e.encounter_id = o.encounter_id "
                      + "       INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id "
                      + "WHERE  p.voided = 0 AND e.voided = 0 AND o.voided = 0 AND o2.voided = 0 "
                      + "       AND e.location_id = :location "
                      + "       AND e.encounter_type = ${6}"
                      + "       AND ( (o.concept_id = ${23985} AND o.value_coded = ${23954})  "
                      + "       AND (o2.concept_id = ${165308} AND o2.value_coded = ${1256} "
                      + "       AND o2.obs_datetime BETWEEN DATE_SUB(:startDate, INTERVAL 6 MONTH) AND DATE_SUB(:endDate, INTERVAL 6 MONTH) )";
   }

  public String getStartDateOfDT3HPOnFichaClinica(){
    return
            "SELECT p.patient_id, e.encounter_datetime AS start_date "
                    + "FROM   patient p "
                    + "       INNER JOIN encounter e ON p.patient_id = e.patient_id "
                    + "       INNER JOIN obs o ON e.encounter_id = o.encounter_id "
                    + "WHERE  p.voided = 0 "
                    + "       AND e.voided = 0 "
                    + "       AND o.voided = 0 "
                    + "       AND e.location_id = :location "
                    + "       AND e.encounter_type = ${6} "
                    + "       AND o.concept_id = ${1719} "
                    + "       AND o.value_coded = ${165307} "
                    + "       AND e.encounter_datetime BETWEEN DATE_SUB(:startDate, INTERVAL 6 MONTH) AND DATE_SUB(:endDate, INTERVAL 6 MONTH) ";
  }

  public String getStartDateOf3hpPiridoxinaOnFilt(){

      return
                "SELECT filt.patient_id, filt_3hp_start_date AS start_date "
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

  public String getMinTPTStartDate(){
      EptsQueriesUtil eptsQueriesUtil = new EptsQueriesUtil();
      String tptQuery = eptsQueriesUtil.unionBuilder(eptsQueriesUtil.min(getStartDateOf3hpPiridoxinaOnFilt()).getQuery())
              .union(eptsQueriesUtil.min(getStartDateOf3HPOnFichaClinica()).getQuery())
              .union(eptsQueriesUtil.min(getStartDateOfDT3HPOnFichaClinica()).getQuery())
              .union(eptsQueriesUtil.min(get3HPStartOnFichaResumo()).getQuery()).buildQuery();

       return    eptsQueriesUtil.min(tptQuery).getQuery();
  }

    public String getPatientsNewOnART(){
     return
                " SELECT most_recent.patient_id "
                        + " FROM (        "
                        + getMinTPTStartDate()
                        + "                ) tpt_start "
                        + " INNER JOIN ( "
                        + commonQueries.getARTStartDate(true)
                        + " ) art on art.patient_id = tpt_start.patient_id "
                        + " WHERE tpt_start.start_date BETWEEN art.first_pickup AND DATE_ADD(art.first_pickup, INTERVAL 6 MONTH) "
                        ;


    }
}
