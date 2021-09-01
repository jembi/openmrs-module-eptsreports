package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsEligibleForVLDataDefinitionQueries {

  private HivMetadata hivMetadata;

  @Autowired
  public ListOfPatientsEligibleForVLDataDefinitionQueries(HivMetadata hivMetadata) {
    this.hivMetadata = hivMetadata;
  }

  // 10
  public DataDefinition getPatientsAndMostRecentVLResultDate() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("All patients with most recent Data de Levantamento");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlPatientDataDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        "SELECT result_date.patient_id, MAX(result_date.most_recent) FROM ("
            + "SELECT p.patient_id, MAX(o.obs_datetime) most_recent FROM patient p"
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "        INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "WHERE e.encounter_type = ${53}"
            + "AND ((o.concept_id = ${856} AND o.value_numeric > 0) OR (o.concept_id = ${1305} AND o.value_coded IS NOT NULL))"
            + "AND o.obs_datetime <= :startDate"
            + "AND e.location_id = :location"
            + "AND e.voided = 0"
            + "AND p.voided = 0"
            + "AND o.voided = 0"
            + "GROUP BY p.patient_id"
            + ""
            + "UNION"
            + ""
            + "SELECT p.patient_id, MAX(e.encounter_datetime) most_recent FROM patient p"
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "        INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "WHERE e.encounter_type IN(${6},${9},${13},${51})"
            + "AND ((o.concept_id = ${856} AND o.value_numeric > 0) OR (o.concept_id = ${1305} AND o.value_coded IS NOT NULL))"
            + "AND e.encounter_datetime <= :startDate"
            + "AND e.location_id = :location"
            + "AND e.voided = 0"
            + "AND p.voided = 0"
            + "AND o.voided = 0"
            + "GROUP BY p.patient_id"
            + ") AS result_date GROUP BY result_date.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  // 11
  public DataDefinition getPatientsAndMostRecentViralLoad() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("All patients with most recent Data de Levantamento");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlPatientDataDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        " SELECT recent_vl.patient_id, recent_vl.viral_load FROM("
            + "SELECT p.patient_id, MAX(o.obs_datetime) most_recent, o.value_numeric viral_load FROM patient p"
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "        INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "WHERE e.encounter_type = ${53}"
            + "AND ((o.concept_id = ${856} AND o.value_numeric > 0) OR (o.concept_id = ${1305} AND o.value_coded IS NOT NULL))"
            + "AND o.obs_datetime <= :startDate"
            + "AND e.location_id = :location"
            + "AND e.voided = 0"
            + "AND p.voided = 0"
            + "AND o.voided = 0"
            + "GROUP BY p.patient_id"
            + "UNION"
            + "SELECT p.patient_id, MAX(e.encounter_datetime), o.value_numeric viral_load FROM patient p"
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "        INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "WHERE e.encounter_type IN(${6},${9},${13},${51})"
            + "AND ((o.concept_id = ${856} AND o.value_numeric > 0) OR (o.concept_id = ${1305} AND o.value_coded IS NOT NULL))"
            + "AND e.encounter_datetime <= :startDate"
            + "AND e.location_id = :location"
            + "AND e.voided = 0"
            + "AND p.voided = 0"
            + "AND o.voided = 0"
            + "GROUP BY p.patient_id"
            + ") AS recent_vl GROUP BY recent_vl.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  public DataDefinition getPatientsAndLastFollowUpConsultationDate() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("All patients with most recent Data de Levantamento");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlPatientDataDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());

    String query =
        " SELECT p.patient_id, MAX(e.encounter_datetime) FROM patient p"
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "        INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "WHERE e.encounter_type IN(${6},${9})"
            + "AND e.encounter_datetime <= :startDate"
            + "AND e.location_id = :location"
            + "AND e.voided = 0"
            + "AND p.voided = 0"
            + "AND o.voided = 0"
            + ""
            + "GROUP BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  // 13
  public DataDefinition getPatientsAndLastFollowUpNextConsultationDate() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("All patients with most recent Data de Levantamento");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlPatientDataDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("1410", hivMetadata.getReturnVisitDateConcept().getConceptId());

    String query =
        " SELECT p.patient_id, MAX(o.value_datetime) FROM patient p"
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "        INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "WHERE e.encounter_type IN(${6},${9})"
            + "AND o.concept_id = ${1410}"
            + "AND e.encounter_datetime <= :startDate"
            + "AND e.voided = 0"
            + "AND e.location_id = :location"
            + "AND p.voided = 0"
            + "AND o.voided = 0"
            + "GROUP BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  // 14
  public DataDefinition getPatientsAndLastDrugPickUpDateOnFila() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("All patients with most recent Data de Levantamento");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlPatientDataDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());

    String query =
        "SELECT p.patient_id, MAX(e.encounter_datetime) FROM patient p"
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id   "
            + "WHERE e.encounter_type = ${18}"
            + "AND e.encounter_datetime <= :startDate"
            + "AND e.voided = 0"
            + "AND e.location_id = :location"
            + "AND p.voided = 0"
            + "GROUP BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }
  // 15
  public DataDefinition getPatientsAndLastDrugPickUpDateOnFichaMestre() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("All patients with most recent Data de Levantamento");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlPatientDataDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    valuesMap.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    valuesMap.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    valuesMap.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());

    String query =
        "SELECT last_ficha.patient_id, last_ficha.max_date FROM("
            + "SELECT p.patient_id, MAX(e.encounter_datetime) encounter_date, MAX(obs_value.value_datetime) max_date  FROM patient p"
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "        INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "        INNER JOIN obs obs_value ON obs_value.encounter_id = e.encounter_id "
            + "        AND o.person_id = obs_value.person_id"
            + "WHERE e.encounter_type = ${52}"
            + "AND o.concept_id = ${23865}"
            + "AND o.value_coded = ${1065}"
            + "AND obs_value.concept_id = ${23866}"
            + "AND obs_value.value_datetime <= :startDate"
            + "AND e.voided = 0"
            + "AND e.location_id = :location"
            + "AND p.voided = 0"
            + "AND o.voided = 0"
            + "GROUP BY p.patient_id) AS last_ficha GROUP BY last_ficha.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  // 16
  public DataDefinition getPatientsAndNextDrugPickUpDateOnFila() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("All patients with most recent Data de Levantamento");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlPatientDataDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    valuesMap.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());

    String query =
        "SELECT next_pickup.patient_id, next_pickup.next_date FROM ("
            + "SELECT p.patient_id, MAX(e.encounter_datetime), o.value_datetime AS next_date FROM patient p"
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "        INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "WHERE e.encounter_type = ${18}"
            + "AND o.concept_id = ${5096}"
            + "AND e.encounter_datetime <= :startDate"
            + "AND e.voided = 0"
            + "AND e.location_id = :location"
            + "AND p.voided = 0"
            + "AND o.voided = 0"
            + "GROUP BY p.patient_id) next_pickup "
            + "GROUP BY next_pickup.patient_id;";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  public SqlPatientDataDefinition getPatientsAndLastDrugPickUpOnFila() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("All patients with most recent Data de Levantamento");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlPatientDataDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    valuesMap.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    valuesMap.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    valuesMap.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());

    String query =
        "SELECT last_ficha.patient_id, last_ficha.max_date FROM("
            + "SELECT p.patient_id, MAX(e.encounter_datetime) encounter_date, MAX(obs_value.value_datetime) max_date  FROM patient p"
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "        INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "        INNER JOIN obs obs_value ON obs_value.encounter_id = e.encounter_id "
            + "        AND o.person_id = obs_value.person_id"
            + "WHERE e.encounter_type = ${52}"
            + "AND o.concept_id = ${23865}"
            + "AND o.value_coded = ${1065}"
            + "AND obs_value.concept_id = ${23866}"
            + "AND obs_value.value_datetime <= :startDate"
            + "AND e.voided = 0"
            + "AND e.location_id = :location"
            + "AND p.voided = 0"
            + "AND o.voided = 0"
            + "GROUP BY p.patient_id) AS last_ficha GROUP BY last_ficha.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  // 17
  public DataDefinition getPatientsAndNextpickUpDateOnFichaMestre() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("All patients with most recent Data de Levantamento");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlPatientDataDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    valuesMap.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    valuesMap.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    valuesMap.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());

    String query =
        "SELECT most_recent.patient_id, most_recent.return_date FROM ("
            + "SELECT p.patient_id, MAX(e.encounter_datetime), DATE_ADD(obs_value.value_datetime, INTERVAL 30 DAY) return_date FROM patient p"
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "        INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "        INNER JOIN obs obs_value ON obs_value.encounter_id = e.encounter_id AND obs_value.person_id = o.person_id"
            + "WHERE e.encounter_type = ${52}"
            + "AND o.concept_id = ${23865}"
            + "AND o.value_coded = ${1065}"
            + "AND obs_value.concept_id = ${23866}"
            + "AND obs_value.value_datetime <= :startDate"
            + "AND e.voided = 0"
            + "AND e.location_id = :location"
            + "AND p.voided = 0"
            + "AND o.voided = 0"
            + "GROUP BY p.patient_id)"
            + "AS most_recent"
            + "GROUP BY most_recent.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  public DataDefinition getPatientsAndNumberOfAPSSAndPPAfterHadVLGreaterThan1000() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("All patients with most recent Data de Levantamento");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlPatientDataDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String query =
        "SELECT "
            + "    p.patient_id, COUNT(e.encounter_id)"
            + "FROM"
            + "    patient p"
            + "        INNER JOIN"
            + "    encounter e ON e.patient_id = p.patient_id"
            + "        INNER JOIN"
            + "    (SELECT "
            + "        p.patient_id, MAX(o.obs_datetime) recent_date"
            + "    FROM"
            + "        patient p"
            + "    INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "    WHERE"
            + "        e.encounter_type = ${53}"
            + "            AND o.concept_id = ${856}"
            + "            AND o.value_numeric >= 1000"
            + "            AND o.obs_datetime <= :startDate"
            + "            AND e.location_id = :location"
            + "            AND e.voided = 0"
            + "            AND p.voided = 0"
            + "            AND o.voided = 0"
            + "    GROUP BY patient_id UNION SELECT "
            + "        p.patient_id, MAX(e.encounter_datetime) recent_date"
            + "    FROM"
            + "        patient p"
            + "    INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id"
            + "    WHERE"
            + "        e.encounter_type IN (${13} , ${6}, ${9}, ${51})"
            + "            AND o.concept_id = ${856}"
            + "            AND o.value_numeric >= 1000"
            + "            AND e.encounter_datetime <= :startDate"
            + "            AND e.location_id = :location"
            + "            AND e.voided = 0"
            + "            AND p.voided = 0"
            + "            AND o.voided = 0"
            + "    GROUP BY p.patient_id) AS most_recent_vl ON most_recent_vl.patient_id = p.patient_id"
            + "WHERE"
            + "    e.encounter_type = ${35}"
            + "        AND e.encounter_datetime >= most_recent_vl.recent_date"
            + "        AND e.encounter_datetime <= :startDate"
            + "        AND e.location_id = :location"
            + "        AND e.voided = 0"
            + "        AND p.voided = 0"
            + "GROUP BY patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }
}
