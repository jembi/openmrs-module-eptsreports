package org.openmrs.module.eptsreports.reporting.library.queries;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public class TPTCompletionQueries {

  private HivMetadata hivMetadata;
  private TbMetadata tbMetadata;

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

  private CohortDefinition getINHStartA1(int masterCardEncounterType, int dataInicioProfilaxiaIsoniazidaConcept ) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    String query =
        "  SELECT"
            + "  p.patient_id, MAX(e.encounter_id) as ultima_profilaxia"
            + "  FROM"
            + "  patient p"
            + "     INNER JOIN"
            + "  encounter e ON p.patient_id = e.patient_id"
            + "     INNER JOIN"
            + " obs o ON e.encounter_id = o.encounter_id"
            + " WHERE"
            + " p.voided = 0 AND e.voided AND o.voided"
            + " and e.encounter_type = ${masterCardEncounterType}"
            + " and o.concept_id = ${dataInicioProfilaxiaIsoniazidaConcept}"
            + " and o.value_datetime IS NOT NULL"
            + " and e.encounter_datetime <= :endDAte"
            + " and e.location_id = :location";
            Map<String, Integer> map = new HashMap<>();

            map.put("masterCardEncounterType", masterCardEncounterType);
            map.put("dataInicioProfilaxiaIsoniazidaConcept", dataInicioProfilaxiaIsoniazidaConcept);
    StringSubstitutor sb = new StringSubstitutor(map);
   
    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  private CohortDefinition getINHStartA2(int adultoSeguimentoEncounterType, int startDrugsConcept, int isoniazidUsageConcept) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    Map<String, Integer> map = new HashMap<>();
    

    String query =
        " SELECT"
            + " p.patient_id"
            + " FROM"
            + " patient p"
            + " INNER JOIN"
            + " encounter e ON p.patient_id = e.patient_id"
            + " INNER JOIN"
            + " obs o ON e.encounter_id = o.encounter_id"
            + " WHERE"
            + " p.voided = 0 AND e.voided AND o.voided"
            + "    AND e.encounter_type = ${adultoSeguimentoEncounterType}"
            + "    AND o.concept_id = ${startDrugsConcept}"
            + "     AND o.value_coded = ${isoniazidUsageConcept}"
            + "     AND e.encounter_datetime <= :endDate"
            + "    AND e.location_id = :location";

            map.put("adultoSeguimentoEncounterType", adultoSeguimentoEncounterType);
            map.put("startDrugsConcept", startDrugsConcept);
           map.put("isoniazidUsageConcept", isoniazidUsageConcept);
    StringSubstitutor sb = new StringSubstitutor(map);

    
    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  private CohortDefinition getINHStartA5(int regimeTPTEncounterType, int regimeTPTConcept, int isoniazidConcept, int isoniazidePiridoxinaConcept) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    Map<String, Integer> map = new HashMap<>();

    String query =
        " SELECT"
            + " p.patient_id"
            + " FROM"
            + " patient p"
            + "    INNER JOIN"
            + "  encounter e ON p.patient_id = e.patient_id"
            + "    INNER JOIN"
            + " obs o ON e.encounter_id = o.encounter_id"
            + " WHERE"
            + " p.voided = 0 AND e.voided AND o.voided"
            + "     AND e.encounter_type = ${regimeTPTEncounterType}"
            + "     AND o.concept_id = ${regimeTPTConcept}"
            + "    AND o.value_coded IN (${isoniazidConcept} , ${isoniazidePiridoxinaConcept})"
            + "     AND e.encounter_datetime < :endDate"
            + "        AND e.location_id = :location";

            map.put("regimeTPTEncounterType", regimeTPTEncounterType);
            map.put("regimeTPTConcept", regimeTPTConcept);
            map.put("isoniazidConcept", isoniazidConcept);
           map.put("isoniazidePiridoxinaConcept", isoniazidePiridoxinaConcept);
    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }
}
