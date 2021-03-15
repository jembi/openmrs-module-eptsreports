package org.openmrs.module.eptsreports.reporting.library.queries;

import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;

public class TPTCompletionQueries {

  @Autowired private HivMetadata hivMetadata;
  @Autowired private TbMetadata tbMetadata;

  public static String getRegimeTPTOrOutrasPrescricoes(
      EncounterType encounterType, Concept question, List<Concept> answers, Integer boundary) {

    List<Integer> answerIds = new ArrayList<>();

    for (Concept concept : answers) {
      answerIds.add(concept.getConceptId());
    }

    String query =
        " SELECT p.patient_id "
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
            + "             GROUP BY p.patient_id) AS inh "
            + " WHERE p.patient_id NOT IN ( SELECT patient_id  "
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

  public static String getINHStartA3(int encounterType, int profilaxiaIsoniazidaConcept) {
    String query =
        "SELECT p.patient_id FROM patient p "
            + "INNER JOIN encounter e ON p.patient_id  = e.encounter_id "
            + "INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "WHERE e.encounter_type = ${encounterType} "
            + "AND o.concept_id = ${profilaxiaIsoniazidaConcept} "
            + "AND e.location = :location "
            + "AND o.value_datetime < :endDate "
            + "AND p.voided = 0 AND e.voided = 0 AND o.voided = 0";

    Map<String, Integer> map = new HashMap<>();
    map.put("encounterType", encounterType);
    map.put("profilaxiaIsoniazidaConcept", profilaxiaIsoniazidaConcept);

    StringSubstitutor sb = new StringSubstitutor(map);

    return sb.replace(query);
  }

  public SqlCohortDefinition getINHStartA4(
      int pediatriaSeguimentoEncounterType, int dataInicioProfilaxiaIsoniazidaConcept) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with Ficha Seguimento Pediatrico ");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "After Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("pediatriaSeguimentoEncounterType", pediatriaSeguimentoEncounterType);
    map.put("dataInicioProfilaxiaIsoniazidaConcept", dataInicioProfilaxiaIsoniazidaConcept);

    String query =
        " SELECT "
            + "	p.patient_id "
            + " FROM "
            + "  	patient p "
            + "     	INNER JOIN "
            + " 	encounter e ON p.patient_id = e.patient_id "
            + "     	INNER JOIN "
            + " 	obs o ON e.encounter_id = o.encounter_id "
            + " WHERE "
            + " 	p.voided = 0 AND e.voided = 0 "
            + "     	AND o.voided = 0 "
            + "     	AND e.encounter_type = ${pediatriaSeguimentoEncounterType} "
            + "     	AND o.concept_id = ${dataInicioProfilaxiaIsoniazidaConcept} "
            + " 	AND e.location_id = :location "
            + "     	AND e.encounter_datetime < :endDate ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  public static String get3HPStartC1(
      int encounterType, int treatmentPrescribedConcept, int threeHPConcept) {
    String query =
        "SELECT p.patient_id FROM patient p "
            + "INNER JOIN encounter e ON p.patient_id  = e.encounter_id "
            + "INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "WHERE e.encounter_type = ${encounterType} "
            + "AND o.concept_id = ${treatmentPrescribedConcept} AND o.value_coded = ${threeHPConcept} "
            + "AND e.location = :location "
            + "AND e.encounter_datetime < :endDate "
            + "AND p.voided = 0 AND e.voided = 0 AND o.voided = 0";

    Map<String, Integer> map = new HashMap<>();
    map.put("encounterType", encounterType);
    map.put("treatmentPrescribedConcept", treatmentPrescribedConcept);
    map.put("threeHPConcept", threeHPConcept);

    StringSubstitutor sb = new StringSubstitutor(map);

    return sb.replace(query);
  }

  public SqlCohortDefinition get3HPStartC2(
      int adultoSeguimentoEncounterType,
      int regimeTPTConcept,
      int hPConcept,
      int hPPiridoxinaConcept) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with FILT ");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "After Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("adultoSeguimentoEncounterType", adultoSeguimentoEncounterType);
    map.put("regimeTPTConcept", regimeTPTConcept);
    map.put("hPConcept", hPConcept);
    map.put("hPPiridoxinaConcept", hPPiridoxinaConcept);

    String query =
        " SELECT  "
            + "  p.patient_id "
            + " 	FROM "
            + "     	patient p "
            + " 	INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + " 	INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + " 	WHERE "
            + "    	e.encounter_type = ${adultoSeguimentoEncounterType} AND p.voided = 0 "
            + "         	AND e.voided = 0 "
            + "         	AND o.voided = 0 "
            + "         	AND o.concept_id = ${RegimeTPTConcept} "
            + "         	AND o.value_coded in (${hPConcept},${hPPiridoxinaConcept}) "
            + "         	AND e.location_id = :location "
            + "         	AND e.encounter_datetime <:endDate ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }
}
