package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
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
            + "e.encounter_type = ${encounterType} AND (o.concept_id = ${viralLoad} AND o.value_numeric IS NOT NULL) "
            + "OR (o.concept_id = ${viralLoadQualitative} AND o.value_numeric IS NOT NULL) "
            + "AND e.encounter_datetime <= :onOrBefore group by patient_id";

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("encounterType", encounterType);
    valuesMap.put("viralLoad", viralLoad);
    valuesMap.put("viralLoadQualitative", viralLoadQualitative);
    StringSubstitutor sub = new StringSubstitutor(valuesMap);
    return sub.replace(query);
  }
}
