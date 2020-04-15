package org.openmrs.module.eptsreports.reporting.library.cohorts;

import org.springframework.stereotype.Component;

@Component
public class ResumoTrimestralQueries {

  public static String getPatientsWhoReceivedOneViralLoadResult(
      int encounterType, int viralLoad, int viralLoadQualitative) {
    String query =
        "SELECT p.patient_id FROM "
            + "patient p JOIN encounter e "
            + "ON p.patient_id = e.patient_id JOIN obs o "
            + "ON o.encounter_id = e.encounter_id "
            + "WHERE p.voided = 0 AND e.voided = 0 AND o.voided = 0 AND "
            + "e.encounter_type = %d AND (o.concept_id = %d AND o.value_numeric IS NOT NULL) "
            + "OR (o.concept_id = %d AND o.value_numeric IS NOT NULL) "
            + "AND e.encounter_datetime <= :onOrBefore group by patient_id";
    return String.format(query, encounterType, viralLoad, viralLoadQualitative);
  }
}
