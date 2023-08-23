package org.openmrs.module.eptsreports.reporting.library.queries;

public class ListOfPatientsWithMdsEvaluationQueries {

  public static String getPatientsInitiatedART12Or24Months(
      String inclusionStartMonthAndDay,
      String inclusionEndMonthAndDay,
      int numberOfYearsStartDate,
      int numberOfYearsEndDate) {
    return "SELECT p.patient_id, "
        + "               Min(o.value_datetime) art_start "
        + "        FROM   patient p "
        + "               INNER JOIN encounter e "
        + "                       ON e.patient_id = p.patient_id "
        + "               INNER JOIN obs o "
        + "                       ON o.encounter_id = e.encounter_id "
        + "        WHERE  e.encounter_type = ${53} "
        + "               AND o.concept_id = ${1190} "
        + "               AND e.location_id = :location "
        + "               AND o.value_datetime BETWEEN DATE_SUB( "
        + "  CONCAT(:evaluationYear,"
        + inclusionStartMonthAndDay
        + "        ) "
        + "                                            ,INTERVAL "
        + numberOfYearsStartDate
        + " YEAR) AND DATE_SUB( "
        + "  CONCAT(:evaluationYear,"
        + inclusionEndMonthAndDay
        + "        ) "
        + "                   ,INTERVAL "
        + numberOfYearsEndDate
        + " YEAR) "
        + "               AND p.voided = 0 "
        + "               AND e.voided = 0 "
        + "               AND o.voided = 0 "
        + "        GROUP  BY p.patient_id";
  }

  public static String getPatientArtStart(String inclusionEndMonthAndDay) {
    return "SELECT p.patient_id, "
        + "       Min(e.encounter_datetime) art_pickup_date "
        + "FROM   patient p "
        + "       INNER JOIN encounter e "
        + "               ON e.patient_id = p.patient_id "
        + "WHERE p.voided = 0 "
        + "       AND e.voided = 0 "
        + "       AND e.encounter_type = ${18} "
        + "       AND e.location_id = :location "
        + "            AND e.encounter_datetime <= CONCAT(:evaluationYear, "
        + inclusionEndMonthAndDay
        + "        ) "
        + "GROUP  BY p.patient_id "
        + "UNION "
        + "SELECT p.patient_id, "
        + "       Min(o2.value_datetime) art_pickup_date "
        + "FROM   patient p "
        + "       INNER JOIN encounter e "
        + "               ON e.patient_id = p.patient_id "
        + "       INNER JOIN obs o "
        + "               ON o.encounter_id = e.encounter_id "
        + "       INNER JOIN obs o2 "
        + "               ON o2.encounter_id = e.encounter_id "
        + "WHERE  p.voided = 0 "
        + "       AND e.voided = 0 "
        + "       AND o.voided = 0 "
        + "       AND o2.voided = 0 "
        + "            AND e.encounter_type = ${52} "
        + "            AND e.location_id = :location "
        + "            AND o.concept_id = ${23865} "
        + "            AND o.value_coded = ${1065} "
        + "            AND o2.concept_id = ${23866} "
        + "            AND o2.value_datetime IS NOT NULL "
        + "            AND o2.value_datetime <= CONCAT(:evaluationYear, "
        + inclusionEndMonthAndDay
        + "        ) "
        + "GROUP  BY p.patient_id";
  }

  public static String getTranferredPatients(
      String inclusionEndMonthAndDay, int numberOfYearsEndDate) {
    return "SELECT p.patient_id "
        + "		        FROM   patient p "
        + "		                INNER JOIN encounter e ON p.patient_id = e.patient_id "
        + "		                INNER JOIN obs o ON e.encounter_id = o.encounter_id "
        + "		                INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id "
        + "		         WHERE  p.voided = 0 "
        + "		         AND e.voided = 0 "
        + "		         AND o.voided = 0 "
        + "		         AND o2.voided = 0 "
        + "		         AND e.location_id = :location "
        + "		         AND e.encounter_type = ${53} "
        + "		         AND ((o.concept_id = ${1369} AND o.value_coded = ${1065}) "
        + "		               AND (o2.concept_id = ${6300} AND o2.value_coded = ${6276})) "
        + "		 	    UNION "
        + "				SELECT p.patient_id "
        + "				FROM   patient p "
        + "					   INNER JOIN encounter e ON p.patient_id = e.patient_id "
        + "					   INNER JOIN obs o ON e.encounter_id = o.encounter_id "
        + "				WHERE  p.voided = 0 "
        + "				AND e.voided = 0 "
        + "				AND o.voided = 0 "
        + "				AND e.location_id = :location "
        + "				AND e.encounter_type = ${6} "
        + "				AND o.concept_id = ${6273} "
        + "				AND o.value_coded = ${1706} "
        + "				AND o.obs_datetime <= DATE_SUB( "
        + "  CONCAT(:evaluationYear,"
        + inclusionEndMonthAndDay
        + "        ) "
        + "                                            ,INTERVAL "
        + numberOfYearsEndDate
        + " YEAR) "
        + "                 "
        + "                UNION "
        + "				SELECT trf_out_resumo.patient_id "
        + "				FROM ( SELECT p.patient_id, "
        + "							  MAX(o.obs_datetime) AS obs_date "
        + "					   FROM   patient p "
        + "							  INNER JOIN encounter e ON p.patient_id = e.patient_id "
        + "							  INNER JOIN obs o ON e.encounter_id = o.encounter_id "
        + "					   WHERE  p.voided = 0 "
        + "					   AND e.voided = 0 "
        + "					   AND o.voided = 0 "
        + "					   AND e.location_id = :location "
        + "					   AND e.encounter_type = ${53} "
        + "                  AND        o.concept_id = ${6272} "
        + "                  AND        o.value_coded = ${1706} "
        + "                  AND        o.obs_datetime <= DATE_SUB( "
        + "  CONCAT(:evaluationYear,"
        + inclusionEndMonthAndDay
        + "        ) "
        + "                                            ,INTERVAL "
        + numberOfYearsEndDate
        + " YEAR) "
        + "                  GROUP BY   p.patient_id "
        + "         )trf_out_resumo ";
  }
}
