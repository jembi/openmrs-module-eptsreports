package org.openmrs.module.eptsreports.reporting.library.queries.cd4request;

public class ListOfPatientsEligibleForCd4RequestQueries {

  public static String getLastVlResultDate() {
    return "SELECT p.patient_id, "
        + "       Max(e.encounter_datetime) AS most_recent "
        + "FROM   patient p "
        + "       INNER JOIN encounter e "
        + "               ON e.patient_id = p.patient_id "
        + "       INNER JOIN obs o "
        + "               ON o.encounter_id = e.encounter_id "
        + "WHERE  e.encounter_type = ${6} "
        + "       AND ( ( o.concept_id = ${856} "
        + "               AND o.value_numeric IS NOT NULL ) "
        + "              OR ( o.concept_id = ${1305} "
        + "                   AND o.value_coded IS NOT NULL ) ) "
        + "       AND e.encounter_datetime <= :endDate "
        + "       AND e.location_id = :location "
        + "       AND e.voided = 0 "
        + "       AND p.voided = 0 "
        + "       AND o.voided = 0 "
        + "GROUP  BY p.patient_id ";
  }

  /**
   * <b> utentes com registo do último “Resultado de CV” numa consulta clínica (Ficha Clínica –
   * Ficha Mestra)</b>
   *
   * @return {@link String}
   */
  public static String getLastVlResult() {
    return "SELECT p.patient_id, o.value_numeric AS viral_load  "
        + "FROM   patient p  "
        + "                                  INNER JOIN encounter e  "
        + "                                          ON e.patient_id = p.patient_id  "
        + "                                  INNER JOIN obs o  "
        + "                                          ON o.encounter_id = e.encounter_id  "
        + "                           INNER JOIN (  "
        + getLastVlResultDate()
        + " )last_vl ON last_vl.patient_id = p.patient_id  "
        + "                           WHERE  e.encounter_type = ${6}  "
        + "                                  AND o.concept_id = ${856}  "
        + "                                          AND o.value_numeric IS NOT NULL "
        + "                                          AND o.value_numeric > 1000 "
        + "                                  AND e.encounter_datetime = last_vl.most_recent  "
        + "                                  AND e.location_id = :location  "
        + "                                  AND e.voided = 0  "
        + "                                  AND p.voided = 0  "
        + "                                  AND o.voided = 0  "
        + "                           GROUP  BY p.patient_id ";
  }

  public static String getSecondVlResultDate() {
    return "SELECT p.patient_id, Max(e.encounter_datetime) AS second_most_recent  "
        + "FROM   patient p  "
        + "                                  INNER JOIN encounter e  "
        + "                                          ON e.patient_id = p.patient_id  "
        + "                                  INNER JOIN obs o  "
        + "                                          ON o.encounter_id = e.encounter_id  "
        + "                           INNER JOIN (  "
        + getLastVlResultDate()
        + " )last_vl ON last_vl.patient_id = p.patient_id  "
        + "WHERE  e.encounter_type = ${6} "
        + "       AND ( ( o.concept_id = ${856} "
        + "               AND o.value_numeric IS NOT NULL ) "
        + "              OR ( o.concept_id = ${1305} "
        + "                   AND o.value_coded IS NOT NULL ) ) "
        + "       AND e.encounter_datetime < last_vl.most_recent "
        + "       AND e.location_id = :location "
        + "       AND e.voided = 0 "
        + "       AND p.voided = 0 "
        + "       AND o.voided = 0 "
        + "GROUP  BY p.patient_id ";
  }

  /**
   * <b> utentes com registo do penúltimo “Resultado de CV” numa consulta clínica (Ficha Clínica –
   * Ficha Mestra)</b>
   *
   * @return {@link String}
   */
  public static String getSecondVlResult() {
    return "SELECT p.patient_id, o.value_numeric AS second_viral_load  "
        + "FROM   patient p  "
        + "                                  INNER JOIN encounter e  "
        + "                                          ON e.patient_id = p.patient_id  "
        + "                                  INNER JOIN obs o  "
        + "                                          ON o.encounter_id = e.encounter_id  "
        + "                           INNER JOIN (  "
        + getSecondVlResultDate()
        + " )second_vl ON second_vl.patient_id = p.patient_id  "
        + "                           WHERE  e.encounter_type = ${6}  "
        + "                                  AND o.concept_id = ${856}  "
        + "                                          AND o.value_numeric IS NOT NULL "
        + "                                          AND o.value_numeric > 1000 "
        + "                                  AND e.encounter_datetime = second_vl.second_most_recent  "
        + "                                  AND e.location_id = :location  "
        + "                                  AND e.voided = 0  "
        + "                                  AND p.voided = 0  "
        + "                                  AND o.voided = 0  "
        + "                           GROUP  BY p.patient_id ";
  }

  /**
   * Registo da lista representativa de Estadio IV (CD4_RF25)
   *
   * <p>Registo da lista representativa de Estadio III (CD4_RF24)
   *
   * @return {@link String}
   */
  public static String getEstadioOmsQuery() {
    return "    SELECT p.patient_id,  "
        + "                        Min(e.encounter_datetime) AS first_date "
        + "                             FROM   patient p "
        + "                                    INNER JOIN encounter e "
        + "                                            ON p.patient_id = e.patient_id "
        + "                                    INNER JOIN obs o "
        + "                                            ON e.encounter_id = o.encounter_id "
        + "                             WHERE  p.voided = 0 "
        + "                                    AND e.voided = 0 "
        + "                                    AND o.voided = 0 "
        + "                                    AND e.encounter_type = ${6}  "
        + "                                    AND o.concept_id = ${1406} "
        + "                                    AND o.value_coded IN (${3},${42},${43},${60},${126},${507},${1294}, "
        + "                                         ${1570},${5018},${5042},${5334},${5344},${5340},${5945},${6783},${6990},${7180},${14656}) "
        + "                                    AND e.encounter_datetime >= :startDate "
        + "                                    AND e.encounter_datetime <= :endDate "
        + "                                    AND e.location_id = :location "
        + "                             GROUP  BY p.patient_id ";
  }

  /**
   * último resultado do CD4 registado numa consulta clínica (Ficha Clínica – Ficha Mestra)
   *
   * @return {@link String}
   */
  public static String getLastCd4ResultDateQuery() {
    return "SELECT pa.patient_id, MAX(enc.encounter_datetime) AS last_cd4 "
        + "FROM "
        + "    patient pa "
        + "        INNER JOIN encounter enc "
        + "                   ON enc.patient_id =  pa.patient_id "
        + "        INNER JOIN obs "
        + "                   ON obs.encounter_id = enc.encounter_id "
        + "WHERE  pa.voided = 0 "
        + "  AND enc.voided = 0 "
        + "  AND obs.voided = 0 "
        + "  AND enc.encounter_type = ${6} "
        + "  AND ( "
        + "        (obs.concept_id = ${1695} AND obs.value_numeric IS NOT NULL) "
        + "        OR "
        + "        (obs.concept_id = ${730} AND obs.value_numeric IS NOT NULL) "
        + "      ) "
        + "  AND enc.encounter_datetime <= :endDate "
        + "  AND enc.location_id = :location "
        + "GROUP BY pa.patient_id";
  }

  /**
   * utentes do sexo feminino com registo de “Grávida” = “Sim” numa consulta clínica (Ficha Clínica
   * – Ficha Mestra) ocorrida nos últimos 9 meses do fim do período de reporte
   *
   * @return {@link String}
   */
  public static String getPregnancyQuery() {
    return " SELECT p.patient_id, MIN(e.encounter_datetime) AS pregnancy_date "
        + "FROM patient p  "
        + "     INNER JOIN person pr "
        + "       ON p.patient_id = pr.person_id "
        + "     INNER JOIN encounter e "
        + "         ON e.patient_id=p.patient_id  "
        + "     INNER JOIN obs o "
        + "         ON o.encounter_id=e.encounter_id "
        + "WHERE p.voided = 0 "
        + "   AND e.voided=0 "
        + "   AND o.voided=0 "
        + "   AND e.location_id=:location "
        + "   AND e.encounter_type= ${6} "
        + "   AND o.concept_id = ${1982} "
        + "   AND o.value_coded= ${1065}"
        + "   AND pr.gender = 'F' "
        + "   AND e.encounter_datetime >= :startDate "
        + "   AND e.encounter_datetime <= :endDate "
        + "GROUP BY p.patient_id ";
  }

  public static String getFemalePregnantPatientsQuery() {

    return "SELECT max_pregnant.patient_id, "
        + "       max_pregnant.pregnancy_date AS pregnancy_date"
        + "FROM   (SELECT p.patient_id, "
        + "               Max(e.encounter_datetime) AS pregnancy_date "
        + "        FROM   patient p "
        + "               INNER JOIN person pe "
        + "                       ON p.patient_id = pe.person_id "
        + "               INNER JOIN encounter e "
        + "                       ON p.patient_id = e.patient_id "
        + "               INNER JOIN obs o "
        + "                       ON e.encounter_id = o.encounter_id "
        + "        WHERE  p.voided = 0 "
        + "               AND e.voided = 0 "
        + "               AND o.voided = 0 "
        + "               AND concept_id = 1982 "
        + "               AND value_coded = 1065 "
        + "               AND e.encounter_type = 6 "
        + "               AND e.encounter_datetime BETWEEN :startDate AND :endDate "
        + "               AND e.location_id = :location "
        + "               AND pe.gender = 'F' "
        + "        GROUP  BY p.patient_id) max_pregnant "
        + "       LEFT JOIN (SELECT p.patient_id, "
        + "                         Max(e.encounter_datetime) AS breastfeeding_date "
        + "                  FROM   patient p "
        + "                         INNER JOIN person pe "
        + "                                 ON p.patient_id = pe.person_id "
        + "                         INNER JOIN encounter e "
        + "                                 ON p.patient_id = e.patient_id "
        + "                         INNER JOIN obs o "
        + "                                 ON e.encounter_id = o.encounter_id "
        + "                  WHERE  p.voided = 0 "
        + "                         AND e.voided = 0 "
        + "                         AND o.voided = 0 "
        + "                         AND concept_id = 1982 "
        + "                         AND value_coded = 1065 "
        + "                         AND e.encounter_type = 6 "
        + "                         AND e.encounter_datetime BETWEEN "
        + "                             :startDate AND :endDate "
        + "                         AND e.location_id = :location "
        + "                         AND pe.gender = 'F' "
        + "                  GROUP  BY p.patient_id) max_breastfeeding "
        + "              ON max_pregnant.patient_id = max_breastfeeding.patient_id "
        + "WHERE  ( max_pregnant.pregnancy_date IS NOT NULL "
        + "         AND max_pregnant.pregnancy_date >= "
        + "       max_breastfeeding.breastfeeding_date ) "
        + "        OR ( max_breastfeeding.breastfeeding_date IS NULL )";
  }

  public static String getFemaleBreastfeedingPatientsQuery() {
    return "SELECT max_breastfeeding.patient_id, "
        + "       max_breastfeeding.breastfeeding_date "
        + "FROM   (SELECT p.patient_id, "
        + "               Max(e.encounter_datetime) AS breastfeeding_date "
        + "        FROM   patient p "
        + "               INNER JOIN person pe "
        + "                       ON p.patient_id = pe.person_id "
        + "               INNER JOIN encounter e "
        + "                       ON p.patient_id = e.patient_id "
        + "               INNER JOIN obs o "
        + "                       ON e.encounter_id = o.encounter_id "
        + "        WHERE  p.voided = 0 "
        + "               AND e.voided = 0 "
        + "               AND o.voided = 0 "
        + "               AND concept_id = 1982 "
        + "               AND value_coded = 1065 "
        + "               AND e.encounter_type = 6 "
        + "               AND e.encounter_datetime BETWEEN :startDate AND :endDate "
        + "               AND e.location_id = :location "
        + "               AND pe.gender = 'F' "
        + "        GROUP  BY p.patient_id) max_breastfeeding "
        + "       LEFT JOIN (SELECT p.patient_id, "
        + "                         Max(e.encounter_datetime) AS pregnancy_date "
        + "                  FROM   patient p "
        + "                         INNER JOIN person pe "
        + "                                 ON p.patient_id = pe.person_id "
        + "                         INNER JOIN encounter e "
        + "                                 ON p.patient_id = e.patient_id "
        + "                         INNER JOIN obs o "
        + "                                 ON e.encounter_id = o.encounter_id "
        + "                  WHERE  p.voided = 0 "
        + "                         AND e.voided = 0 "
        + "                         AND o.voided = 0 "
        + "                         AND concept_id = 1982 "
        + "                         AND value_coded = 1065 "
        + "                         AND e.encounter_type = 6 "
        + "                         AND e.encounter_datetime BETWEEN "
        + "                             :startDate AND :endDate "
        + "                         AND e.location_id = :location "
        + "                         AND pe.gender = 'F' "
        + "                  GROUP  BY p.patient_id) max_pregnant "
        + "              ON max_pregnant.patient_id = max_breastfeeding.patient_id "
        + "WHERE  ( max_breastfeeding.breastfeeding_date IS NOT NULL "
        + "         AND max_breastfeeding.breastfeeding_date > max_pregnant.pregnancy_date "
        + "       ) "
        + "        OR ( max_pregnant.pregnancy_date IS NULL ) ";
  }
}
