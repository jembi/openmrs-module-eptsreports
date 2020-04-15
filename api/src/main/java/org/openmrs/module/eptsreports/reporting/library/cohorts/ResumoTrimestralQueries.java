package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Component;

@Component
public class ResumoTrimestralQueries {

  public static String getPatientsInTheFirstLineOfTreatment(
      int adultoSeguimentoEncounterType, int therapeuticLineConcept, int firstLineConcept) {
    String query =
        "SELECT tbl.patient_id  FROM "
            + " (SELECT patient_id, e.encounter_id FROM encounter e "
            + " JOIN obs o ON o.encounter_id = e.encounter_id "
            + "     AND e.encounter_type = ${adultoSeguimentoEncounterType} "
            + "     AND o.concept_id = ${therapeuticLineConcept} "
            + "     AND o.value_coded = ${firstLineConcept} "
            + "     AND o.location_id = :location "
            + "     AND o.voided = 0 "
            + " UNION "
            + " SELECT patient_id, e.encounter_id FROM encounter e "
            + " JOIN obs o ON o.encounter_id = e.encounter_id "
            + "     AND e.encounter_type = ${adultoSeguimentoEncounterType} "
            + "     AND o.concept_id = ${therapeuticLineConcept} "
            + "     AND isnull(o.value_coded) "
            + "     AND o.location_id = :location "
            + "     AND o.voided = 0) tbl "
            + " JOIN patient p ON p.patient_id = tbl.patient_id "
            + " JOIN encounter enc ON enc.encounter_id = tbl.encounter_id "
            + "     AND enc.voided = 0 "
            + "     AND enc.location_id = :location "
            + "     AND p.voided = 0 "
            + "     AND enc.encounter_datetime <= :onOrBefore "
            + "     GROUP BY patient_id ";

    Map<String, Integer> map = new HashMap<>();
    map.put("adultoSeguimentoEncounterType", adultoSeguimentoEncounterType);
    map.put("therapeuticLineConcept", therapeuticLineConcept);
    map.put("firstLineConcept", firstLineConcept);

    StringSubstitutor sub = new StringSubstitutor(map);
    System.out.println(sub.replace(query).toString());
    return sub.replace(query);
  }
}
