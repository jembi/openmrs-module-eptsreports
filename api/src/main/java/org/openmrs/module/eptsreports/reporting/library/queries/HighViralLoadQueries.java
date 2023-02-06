package org.openmrs.module.eptsreports.reporting.library.queries;

public class HighViralLoadQueries {

  /**
   * The date of first APSS/PP Consultation Date registered in Ficha APSS/PP between the First High
   * Viral Load Result Date (HVL_FR13) and report end date
   */
  public static String getSessionZeroQuery() {
    return "           SELECT p.patient_id, MIN(e.encounter_datetime) as session_zero_date "
        + "                    FROM patient p INNER JOIN encounter e ON p.patient_id = e.patient_id "
        + "                                   INNER JOIN ( "
        + "                                         SELECT p.patient_id, MIN(e.encounter_datetime) AS result_date "
        + "                                         FROM "
        + "                                             patient p INNER JOIN encounter e ON p.patient_id = e.patient_id "
        + "                                                       INNER JOIN obs o ON e.encounter_id = o.encounter_id "
        + "                                         WHERE p.voided = 0 AND e.voided = 0 AND o.voided = 0 "
        + "                                           AND e.encounter_type IN (${13}, ${51}) "
        + "                                           AND o.concept_id = ${856} "
        + "                                           AND o.value_numeric >= 1000 "
        + "                                           AND e.encounter_datetime >= :startDate "
        + "                                           AND e.encounter_datetime <= :endDate "
        + "                                           AND e.location_id = :location "
        + "                                         GROUP BY p.patient_id "
        + "                                 ) vl_result on p.patient_id = vl_result.patient_id "
        + "                   WHERE p.voided = 0 AND e.voided = 0 "
        + "                    AND e.encounter_type = ${35} "
        + "                    AND e.location_id = :location "
        + "                    AND e.encounter_datetime BETWEEN vl_result.result_date AND :endDate "
        + "                   GROUP BY p.patient_id ";
  }

  /**
   * The date of first APSS/PP Consultation registered in Ficha APSS/PP between (after) the Session
   * 0 APSS/PP Consultation Date (value of column S) and report end date
   */
  public static String getSessionOneQuery() {
    return "SELECT p.patient_id, "
        + "       Min(e.encounter_datetime) AS first_session_date "
        + "FROM   patient p "
        + "       INNER JOIN encounter e "
        + "               ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o "
        + "               ON e.encounter_id = o.encounter_id "
        + "       INNER JOIN ( "
        + HighViralLoadQueries.getSessionZeroQuery()
        + " ) apss_session_zero "
        + "       ON p.patient_id = apss_session_zero.patient_id "
        + "WHERE  p.voided = 0 "
        + "       AND e.voided = 0 "
        + "       AND o.voided = 0 "
        + "       AND e.encounter_type = ${35}"
        + "       AND e.location_id = :location  "
        + "       AND e.encounter_datetime > apss_session_zero.session_zero_date "
        + "       AND e.encounter_datetime <= :endDate "
        + "GROUP BY p.patient_id";
  }

  /**
   * The date of first APSS/PP Consultation registered in Ficha APSS/PP between the 1st APSS/PP
   * Consultation Date (value of column U) and report end date
   */
  public static String getSessionTwoQuery() {
    return "SELECT p.patient_id, "
        + "       Min(e.encounter_datetime) AS second_session_date "
        + "FROM   patient p "
        + "       INNER JOIN encounter e "
        + "               ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o "
        + "               ON e.encounter_id = o.encounter_id "
        + "       INNER JOIN ( "
        + HighViralLoadQueries.getSessionOneQuery()
        + "              ) apss_session_one "
        + "       ON p.patient_id = apss_session_one.patient_id "
        + "WHERE  p.voided = 0 "
        + "       AND e.voided = 0 "
        + "       AND o.voided = 0 "
        + "       AND e.encounter_type = ${35} "
        + "       AND e.location_id = :location  "
        + "       AND e.encounter_datetime > apss_session_one.first_session_date "
        + "       AND e.encounter_datetime <= :endDate "
        + "GROUP BY p.patient_id";
  }

  /**
   * The date of first APSS/PP Consultation registered in Ficha APSS/PP between the 2nd APSS/PP
   * Consultation Date (HVL_FR17- value of column W) and report end date
   */
  public static String getSessionThreeQuery() {
    return "SELECT p.patient_id, "
        + "       Min(e.encounter_datetime) AS third_session_date "
        + "FROM   patient p "
        + "       INNER JOIN encounter e "
        + "               ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o "
        + "               ON e.encounter_id = o.encounter_id "
        + "       INNER JOIN ( "
        + HighViralLoadQueries.getSessionTwoQuery()
        + "        ) apss_session_two "
        + "           ON p.patient_id = apss_session_two.patient_id "
        + " WHERE  p.voided = 0 "
        + "        AND e.voided = 0 "
        + "        AND o.voided = 0 "
        + "        AND e.encounter_type = ${35} "
        + "        AND e.location_id = :location  "
        + "        AND e.encounter_datetime > apss_session_two.second_session_date "
        + "        AND e.encounter_datetime <= :endDate "
        + " GROUP BY p.patient_id";
  }

  /**
   * Date of the earliest Laboratory or FSR form with VL Result registered between the 3rd APSS/PP
   * Consultation Date (value of column Y) and report end date
   */
  public static String getColumnFQuery(boolean greaterThan1000) {
    String query =
        " SELECT p.patient_id, "
            + "       Min(e.encounter_datetime) AS result_date "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON p.patient_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + "INNER JOIN ( "
            + HighViralLoadQueries.getSessionThreeQuery()
            + "          ) session_three ON p.patient_id = session_three.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND o2.voided = 0 "
            + "       AND e.encounter_type IN ( ${13}, ${51} ) "
            + "       AND (o.concept_id = ${856} ";
    if (greaterThan1000) {
      query += "       AND o.value_numeric >= 1000) ";
    } else {
      query += "       AND o.value_numeric IS NOT NULL) ";
    }
    query +=
        "       AND e.location_id = :location "
            + "       AND e.encounter_datetime > session_three.third_session_date "
            + "       AND e.encounter_datetime <= :endDate "
            + "GROUP  BY p.patient_id";

    return query;
  }

  /**
   * The date of first APSS/PP Consultation Date registered in Ficha APSS/SS between the Second High
   * Viral Load Result Date (HVL_FR22 - value of column AF) and report end date
   */
  public static String getApssSessionZero() {

    return "SELECT p.patient_id, "
        + "       Min(e.encounter_datetime) AS apss_date "
        + "FROM   patient p "
        + "       INNER JOIN encounter e "
        + "               ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o "
        + "               ON e.encounter_id = o.encounter_id "
        + "        INNER JOIN ( "
        + HighViralLoadQueries.getColumnFQuery(true)
        + " ) af_date on p.patient_id = af_date.patient_id "
        + " WHERE  p.voided = 0 "
        + "        AND e.voided = 0 "
        + "        AND o.voided = 0 "
        + "        AND e.encounter_type = ${35} "
        + "        AND e.location_id = :location  "
        + "        AND e.encounter_datetime > af_date.result_date "
        + "        AND e.encounter_datetime <= :endDate "
        + " GROUP BY p.patient_id";
  }

  /**
   * The date of first APSS/PP Consultation registered in Ficha APSS/PP between the APSS/PP Session
   * 0 After Second High VL Consultation Date (HVL_FR27 - value of column AM) and report end date
   */
  public static String getApssSessionOne() {
    return "SELECT p.patient_id, "
        + "       Min(e.encounter_datetime) AS apss_date "
        + "FROM   patient p "
        + "       INNER JOIN encounter e "
        + "               ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o "
        + "               ON e.encounter_id = o.encounter_id "
        + "        INNER JOIN ( "
        + HighViralLoadQueries.getApssSessionZero()
        + " ) session_date on p.patient_id = session_date.patient_id "
        + " WHERE  p.voided = 0 "
        + "        AND e.voided = 0 "
        + "        AND o.voided = 0 "
        + "        AND e.encounter_type = ${35} "
        + "        AND e.location_id = :location  "
        + "        AND e.encounter_datetime > session_date.apss_date "
        + "        AND e.encounter_datetime <= :endDate "
        + " GROUP BY p.patient_id";
  }

  /**
   * The date of first APSS/PP Consultation registered in Ficha APSS/PP between the 1st APSS/PP
   * Consultation Date after second high VL (HVL_FR28 - value of column AO) and report end date
   */
  public static String getApssSessionTwo() {
    return "SELECT p.patient_id, "
        + "       Min(e.encounter_datetime) AS apss_date "
        + "FROM   patient p "
        + "       INNER JOIN encounter e "
        + "               ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o "
        + "               ON e.encounter_id = o.encounter_id "
        + "        INNER JOIN ( "
        + HighViralLoadQueries.getApssSessionOne()
        + " ) session_date on p.patient_id = session_date.patient_id "
        + " WHERE  p.voided = 0 "
        + "        AND e.voided = 0 "
        + "        AND o.voided = 0 "
        + "        AND e.encounter_type = ${35} "
        + "        AND e.location_id = :location  "
        + "        AND e.encounter_datetime > session_date.apss_date "
        + "        AND e.encounter_datetime <= :endDate "
        + " GROUP BY p.patient_id";
  }

  /**
   * The date of first APSS/PP Consultation registered in Ficha APSS/PP between the 2nd APSS/PP
   * Consultation Date after second high VL (HVL_FR29- value of column AQ) and report end date
   */
  public static String getApssSessionThree() {
    return "SELECT p.patient_id, "
        + "       Min(e.encounter_datetime) AS apss_date "
        + "FROM   patient p "
        + "       INNER JOIN encounter e "
        + "               ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o "
        + "               ON e.encounter_id = o.encounter_id "
        + "        INNER JOIN ( "
        + HighViralLoadQueries.getApssSessionTwo()
        + " ) session_date on p.patient_id = session_date.patient_id "
        + " WHERE  p.voided = 0 "
        + "        AND e.voided = 0 "
        + "        AND o.voided = 0 "
        + "        AND e.encounter_type = ${35} "
        + "        AND e.location_id = :location  "
        + "        AND e.encounter_datetime > session_date.apss_date "
        + "        AND e.encounter_datetime <= :endDate "
        + " GROUP BY p.patient_id";
  }

  /**
   * Date of the earliest Laboratory or FSR form with VL Result registered between the 3rd APSS/PP
   * Consultation Date after second high VL (value of column AS) and report end date
   *
   * @param resultDate Flag to change the returned column
   * @return {@link String}
   */
  public static String getThirdVLResultOrResultDateQuery(boolean resultDate) {

    String query = " SELECT p.patient_id, ";

    if (resultDate) {
      query += "       Min(e.encounter_datetime) AS result_date ";
    } else {
      query += "      o.value_numeric AS vl_result ";
    }

    query +=
        "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON p.patient_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + "INNER JOIN ( "
            + HighViralLoadQueries.getApssSessionThree()
            + "          ) session_three ON p.patient_id = session_three.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type IN ( ${13}, ${51} ) "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${856} "
            + "       AND o.value_numeric IS NOT NULL "
            + "       AND e.encounter_datetime > session_three.apss_date "
            + "       AND e.encounter_datetime <= :endDate "
            + "GROUP  BY p.patient_id";

    return query;
  }
}
