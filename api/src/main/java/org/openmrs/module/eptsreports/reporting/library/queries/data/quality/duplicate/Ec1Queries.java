package org.openmrs.module.eptsreports.reporting.library.queries.data.quality.duplicate;

public class Ec1Queries {

  /**
   * The patient has more than one Ficha Resumo registered in OpenMRS EPTS at the same Health
   * Facility
   *
   * @return String
   */
  public static String getEc1FichaResumoDuplicates(int encounter_type) {
    String query =
        "SELECT tbl.patient_id FROM "
            + "(SELECT p.patient_id, COUNT(e.encounter_id) AS number_of_ficha FROM patient p INNER JOIN encounter e ON p.patient_id=e.patient_id "
            + " WHERE e.encounter_type=%d AND e.location_id = :location AND e.encounter_datetime <= :endDate AND p.voided = 0 AND e.voided = 0 GROUP BY p.patient_id HAVING COUNT(e.encounter_id) > 1) tbl ";
    return String.format(query, encounter_type);
  }
}
