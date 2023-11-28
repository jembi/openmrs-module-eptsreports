package org.openmrs.module.eptsreports.reporting.library.queries.resumo;

public class ResumoMensalDAHQueries {

  /**
   * @param valueNumeric Amount of Cd4 for each range of age
   * @return {@link String}
   */
  public static String getCd4ResultOverOrEqualTo5years(int valueNumeric) {

    return "  SELECT ps.person_id FROM   person ps "
        + "       INNER JOIN encounter e "
        + "               ON ps.person_id = e.patient_id "
        + "       INNER JOIN obs o "
        + "               ON e.encounter_id = o.encounter_id "
        + "WHERE  ps.voided = 0 "
        + "       AND e.voided = 0 "
        + "       AND o.voided = 0 "
        + "       AND e.encounter_type IN (${90}, ${6})  "
        + "       AND o.concept_id = ${1695}  "
        + "       AND o.value_numeric < "
        + valueNumeric
        + " AND ( "
        + "  ( o.obs_datetime >= :startDate "
        + "  AND o.obs_datetime <= :endDate)"
        + "OR "
        + " ( e.encounter_datetime >= :startDate "
        + "  AND e.encounter_datetime <= :endDate) "
        + " ) "
        + "  AND e.location_id = :location"
        + "  AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) >=5 "
        + "  GROUP BY ps.person_id ";
  }

  /**
   * @param valueNumeric Amount of Cd4 for each range of age
   * @return {@link String}
   */
  public static String getCd4ResultBetweenOneAnd5years(int valueNumeric) {

    return "  SELECT ps.person_id FROM   person ps "
        + "       INNER JOIN encounter e "
        + "               ON ps.person_id = e.patient_id "
        + "       INNER JOIN obs o "
        + "               ON e.encounter_id = o.encounter_id "
        + "WHERE  ps.voided = 0 "
        + "       AND e.voided = 0 "
        + "       AND o.voided = 0 "
        + "       AND e.encounter_type IN (${90}, ${6})  "
        + "       AND o.concept_id = ${1695}  "
        + "       AND o.value_numeric < "
        + valueNumeric
        + " AND ( "
        + "  ( o.obs_datetime >= :startDate "
        + "  AND o.obs_datetime <= :endDate)"
        + "OR "
        + " ( e.encounter_datetime >= :startDate "
        + "  AND e.encounter_datetime <= :endDate) "
        + " ) "
        + "  AND e.location_id = :location"
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) >=1 "
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) <5 "
        + "  GROUP BY ps.person_id ";
  }

  /**
   * @param valueNumeric Amount of Cd4 for each range of age
   * @return {@link String}
   */
  public static String getCd4ResultBellowOneYear(int valueNumeric) {

    return "  SELECT ps.person_id FROM   person ps "
        + "       INNER JOIN encounter e "
        + "               ON ps.person_id = e.patient_id "
        + "       INNER JOIN obs o "
        + "               ON e.encounter_id = o.encounter_id "
        + "WHERE  ps.voided = 0 "
        + "       AND e.voided = 0 "
        + "       AND o.voided = 0 "
        + "       AND e.encounter_type IN (${90}, ${6})  "
        + "       AND o.concept_id = ${1695}  "
        + "       AND o.value_numeric < "
        + valueNumeric
        + " AND ( "
        + "  ( o.obs_datetime >= :startDate "
        + "  AND o.obs_datetime <= :endDate)"
        + "OR "
        + " ( e.encounter_datetime >= :startDate "
        + "  AND e.encounter_datetime <= :endDate) "
        + " ) "
        + "  AND e.location_id = :location"
        + "  AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) <1 "
        + "  GROUP BY ps.person_id ";
  }
}
