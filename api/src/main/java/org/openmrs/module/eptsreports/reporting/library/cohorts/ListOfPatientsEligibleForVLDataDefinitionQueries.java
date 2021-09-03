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

  // 7
  public DataDefinition getPatientsAndARTStartDate() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(
        "Patient ART Start Date is the oldest date from the set of criterias");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());
    valuesMap.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    valuesMap.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    valuesMap.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    valuesMap.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    valuesMap.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    valuesMap.put("1255", hivMetadata.getARVPlanConcept().getConceptId());
    valuesMap.put("1410", hivMetadata.getReturnVisitDateConcept().getConceptId());
    valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    valuesMap.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());
    valuesMap.put("1256", hivMetadata.getStartDrugs().getConceptId());

    String query =
        " SELECT art.patient_id, MIN(art.art_date) min_art_date FROM ( "
            + " SELECT p.patient_id, MIN(e.encounter_datetime) art_date FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + " WHERE e.encounter_type = ${18} "
            + " AND e.encounter_datetime <= :endDate "
            + " AND e.voided = 0 "
            + " AND p.voided = 0 "
            + " AND e.location_id = :location "
            + " GROUP BY p.patient_id "
            + "  "
            + " UNION "
            + "  "
            + " SELECT p.patient_id, Min(e.encounter_datetime) art_date  "
            + "                                 FROM patient p  "
            + "                           INNER JOIN encounter e  "
            + "                               ON p.patient_id = e.patient_id  "
            + "                           INNER JOIN obs o  "
            + "                               ON e.encounter_id = o.encounter_id  "
            + "                       WHERE  p.voided = 0  "
            + "                           AND e.voided = 0  "
            + "                           AND o.voided = 0  "
            + "                           AND e.encounter_type IN (${6}, ${9}, ${18})  "
            + "                           AND o.concept_id = ${1255}  "
            + "                           AND o.value_coded= ${1256}  "
            + "                           AND e.encounter_datetime <= :endDate  "
            + "                           AND e.location_id = :location  "
            + "                       GROUP  BY p.patient_id  "
            + "  "
            + " UNION "
            + "  "
            + " SELECT p.patient_id, historical.min_date AS art_date FROM patient p  "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "     INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "     INNER JOIN( "
            + " SELECT p.patient_id,e.encounter_id,  MIN(o.value_datetime) min_date FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + " INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + " WHERE e.encounter_type IN(${6},${9},${18},${53}) "
            + " AND o.concept_id = ${1190} "
            + " AND e.location_id = :location "
            + "                 AND o.value_datetime <= :endDate "
            + " AND e.voided = 0 "
            + " AND p.voided = 0 "
            + "  "
            + " GROUP BY p.patient_id "
            + "                 ) historical "
            + " ON historical.patient_id = p.patient_id "
            + " WHERE e.encounter_type IN(${6},${9},${18},${53}) "
            + " AND o.concept_id = ${1190} "
            + " AND e.location_id = :location "
            + "                 AND o.value_datetime <= :endDate "
            + " AND e.voided = 0 "
            + " AND p.voided = 0 "
            + "                 AND historical.encounter_id = e.encounter_id "
            + "                 AND o.value_datetime = historical.min_date "
            + " GROUP BY p.patient_id "
            + "                  "
            + " UNION "
            + "  "
            + " SELECT p.patient_id, ps.start_date AS art_date "
            + "     FROM   patient p   "
            + "           INNER JOIN patient_program pg  "
            + "                ON p.patient_id = pg.patient_id  "
            + "        INNER JOIN patient_state ps  "
            + "                   ON pg.patient_program_id = ps.patient_program_id  "
            + "     WHERE  pg.location_id = :location "
            + "    AND pg.program_id = 2 and ps.start_date <= :endDate "
            + "     "
            + "    UNION "
            + "     "
            + "    SELECT p.patient_id,  MIN(o.value_datetime) AS art_date FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + " INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                         INNER JOIN obs oyes ON oyes.encounter_id = e.encounter_id  "
            + "                         AND o.person_id = oyes.person_id "
            + " WHERE e.encounter_type = 52 "
            + " AND o.concept_id = ${23866} "
            + "                 AND o.value_datetime <= :endDate "
            + "                 AND o.voided = 0 "
            + "                 AND oyes.concept_id = ${23865} "
            + "                 AND oyes.value_coded = ${1065} "
            + "                 AND oyes.voided = 0 "
            + " AND e.location_id = :location                 "
            + " AND e.voided = 0 "
            + " AND p.voided = 0 "
            + "  "
            + " GROUP BY p.patient_id "
            + " ) art  "
            + "  "
            + " GROUP BY art.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }
  // 9
  public DataDefinition getLastTARVLinha() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("The last Linha TARV registered on FICHA CLINICA");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("21151", hivMetadata.getTherapeuticLineConcept().getConceptId());

    String query =
        "   SELECT p.patient_id, cn.name  "
            + "               FROM patient p "
            + "                 INNER JOIN  "
            + "                 ( SELECT p.patient_id, MAX(e.encounter_datetime) as encounter_datetime  "
            + "                   FROM  patient p   "
            + "                       INNER JOIN encounter e  ON p.patient_id = e.patient_id  "
            + "                   WHERE p.voided = 0  "
            + "                       AND e.voided = 0   "
            + "                       AND e.location_id = :location  "
            + "                       AND e.encounter_datetime <= :endDate  "
            + "                       AND e.encounter_type = ${6} "
            + "                   GROUP BY p.patient_id  "
            + "                 ) max_encounter ON p.patient_id=max_encounter.patient_id "
            + "                   INNER JOIN encounter e ON p.patient_id= e.patient_id  "
            + "                   INNER JOIN obs ob ON e.encounter_id = ob.encounter_id  "
            + "                   INNER JOIN concept_name cn on ob.value_coded=cn.concept_id  "
            + "               WHERE  p.voided = 0 "
            + "                   AND e.voided = 0  "
            + "                   AND ob.voided = 0 "
            + "                   AND cn.locale='pt' "
            + "                   AND cn.concept_name_type = 'FULLY_SPECIFIED'  "
            + "                   AND max_encounter.encounter_datetime = e.encounter_datetime  "
            + "                   AND e.encounter_type = ${6} "
            + "                   AND e.location_id = :location  "
            + "                   AND ob.concept_id = ${21151}  "
            + "                   AND ob.value_coded IS NOT NULL  "
            + "                   AND e.encounter_datetime <= :endDate  ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  // 10
  public DataDefinition getPatientsAndMostRecentVLResultDate() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("The Most Recent Viral Load Result Date ");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        " SELECT result_date.patient_id, MAX(result_date.most_recent) FROM ( "
            + " SELECT p.patient_id, MAX(o.obs_datetime) most_recent FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "         INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + " WHERE e.encounter_type = ${53} "
            + " AND ((o.concept_id = ${856} AND o.value_numeric > 0) OR (o.concept_id = ${1305} AND o.value_coded IS NOT NULL)) "
            + " AND o.obs_datetime <= :startDate "
            + " AND e.location_id = :location "
            + " AND e.voided = 0 "
            + " AND p.voided = 0 "
            + " AND o.voided = 0 "
            + " GROUP BY p.patient_id "
            + "  "
            + " UNION "
            + "  "
            + " SELECT p.patient_id, MAX(e.encounter_datetime) most_recent FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "         INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + " WHERE e.encounter_type IN(${6},${9},${13},${51}) "
            + " AND ((o.concept_id = ${856} AND o.value_numeric > 0) OR (o.concept_id = ${1305} AND o.value_coded IS NOT NULL)) "
            + " AND e.encounter_datetime <= :startDate "
            + " AND e.location_id = :location "
            + " AND e.voided = 0 "
            + " AND p.voided = 0 "
            + " AND o.voided = 0 "
            + " GROUP BY p.patient_id "
            + " ) AS result_date GROUP BY result_date.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  // 11
  public DataDefinition getPatientsAndMostRecentViralLoad() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("The Most Recent Viral Load Result");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        "  SELECT recent_vl.patient_id, recent_vl.viral_load FROM( "
            + " SELECT p.patient_id, MAX(o.obs_datetime) most_recent, o.value_numeric viral_load FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "         INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + " WHERE e.encounter_type = ${53} "
            + " AND ((o.concept_id = ${856} AND o.value_numeric > 0) OR (o.concept_id = ${1305} AND o.value_coded IS NOT NULL)) "
            + " AND o.obs_datetime <= :startDate "
            + " AND e.location_id = :location "
            + " AND e.voided = 0 "
            + " AND p.voided = 0 "
            + " AND o.voided = 0 "
            + " GROUP BY p.patient_id "
            + " UNION "
            + " SELECT p.patient_id, MAX(e.encounter_datetime), o.value_numeric viral_load FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "         INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + " WHERE e.encounter_type IN(${6},${9},${13},${51}) "
            + " AND ((o.concept_id = ${856} AND o.value_numeric > 0) OR (o.concept_id = ${1305} AND o.value_coded IS NOT NULL)) "
            + " AND e.encounter_datetime <= :startDate "
            + " AND e.location_id = :location "
            + " AND e.voided = 0 "
            + " AND p.voided = 0 "
            + " AND o.voided = 0 "
            + " GROUP BY p.patient_id "
            + " ) AS recent_vl GROUP BY recent_vl.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }
  // 12
  public DataDefinition getPatientsAndLastFollowUpConsultationDate() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Last Follow up Consultation Date ");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());

    String query =
        "  SELECT p.patient_id, MAX(e.encounter_datetime) FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "         INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + " WHERE e.encounter_type IN(${6},${9}) "
            + " AND e.encounter_datetime <= :startDate "
            + " AND e.location_id = :location "
            + " AND e.voided = 0 "
            + " AND p.voided = 0 "
            + " AND o.voided = 0 "
            + "  "
            + " GROUP BY p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  // 13
  public DataDefinition getPatientsAndNextFollowUpConsultationDate() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Next Follow up Consultation Date");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("1410", hivMetadata.getReturnVisitDateConcept().getConceptId());

    String query =
        "  SELECT p.patient_id, MAX(o.value_datetime) FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "         INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + " WHERE e.encounter_type IN(${6},${9}) "
            + " AND o.concept_id = ${1410} "
            + " AND e.encounter_datetime <= :startDate "
            + " AND e.voided = 0 "
            + " AND e.location_id = :location "
            + " AND p.voided = 0 "
            + " AND o.voided = 0 "
            + " GROUP BY p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  // 14
  public DataDefinition getPatientsAndLastDrugPickUpDateOnFila() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Last Drug Pick-up Date on FILA");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());

    String query =
        " SELECT p.patient_id, MAX(e.encounter_datetime) FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id    "
            + " WHERE e.encounter_type = ${18} "
            + " AND e.encounter_datetime <= :startDate "
            + " AND e.voided = 0 "
            + " AND e.location_id = :location "
            + " AND p.voided = 0 "
            + " GROUP BY p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }
  // 15
  public DataDefinition getPatientsAndLastDrugPickUpDateOnFichaMestre() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Last Drug Pick-up Date on Ficha Mestre");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    valuesMap.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    valuesMap.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    valuesMap.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());

    String query =
        "  SELECT p.patient_id, obs_value.value_datetime    FROM patient p "
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "        INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "            INNER JOIN obs obs_value ON e.encounter_id = obs_value.encounter_id "
            + "       "
            + "        INNER JOIN (  "
            + "   "
            + "              SELECT p.patient_id, MAX(e.encounter_datetime) recent_encounter FROM patient p "
            + "              INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                      INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                      INNER JOIN obs obs_value ON o.encounter_id = obs_value.encounter_id "
            + "              WHERE e.encounter_type = ${52} "
            + "              AND o.concept_id = ${23865} "
            + "              AND o.value_coded = ${1065}   "
            + "              AND obs_value.concept_id = ${23866} "
            + "              AND obs_value.value_datetime <= :startDate"
            + "              AND e.voided = 0 "
            + "              AND e.location_id = :location "
            + "              AND p.voided = 0 "
            + "              AND o.voided = 0 "
            + "              GROUP BY p.patient_id) recent_arv ON recent_arv.patient_id = p.patient_id          "
            + "               WHERE e.encounter_type = ${52} "
            + "               AND e.encounter_datetime = recent_arv.recent_encounter "
            + "              AND o.concept_id = ${23865}    "
            + "              AND o.value_coded = ${1065}   "
            + "              AND obs_value.concept_id = ${23866} "
            + "              AND obs_value.value_datetime <= :startDate "
            + ""
            + "              AND e.voided = 0 "
            + "              AND e.location_id = :location "
            + "              AND p.voided = 0 "
            + "              AND o.voided = 0 "
            + "              GROUP BY p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  // 16
  public DataDefinition getPatientsAndNextDrugPickUpDateOnFila() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Next Drug pick-up Date on FILA");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    valuesMap.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());

    String query =
        "  SELECT p.patient_id, o.value_datetime FROM patient p "
            + "INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "        INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "        INNER JOIN (    "
            + "   "
            + "  SELECT p.patient_id, MAX(e.encounter_datetime) recent_encounter FROM patient p "
            + "  INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "  INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "  WHERE e.encounter_type = ${18}   "
            + "  AND e.encounter_datetime <= :startDate "
            + "  AND e.voided = 0 "
            + "  AND e.location_id = :location "
            + "  AND p.voided = 0 "
            + "                  AND p.voided = 0 "
            + ""
            + "                  GROUP BY patient_id "
            + " ) most_recent ON most_recent.patient_id = p.patient_id "
            + " WHERE e.encounter_type = ${18} "
            + "  AND o.concept_id = ${5096} "
            + "                  AND e.encounter_datetime = most_recent.recent_encounter "
            + "  AND e.encounter_datetime <= :startDate "
            + "  AND e.voided = 0 "
            + "  AND e.location_id = :location "
            + "  AND p.voided = 0 "
            + "                  AND p.voided = 0 "
            + "  AND o.voided = 0"
            + "                  GROUP BY p.patient_id "
            + "                  ";

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

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    valuesMap.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    valuesMap.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    valuesMap.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());

    String query =
        " SELECT last_ficha.patient_id, last_ficha.max_date FROM( "
            + " SELECT p.patient_id, MAX(e.encounter_datetime) encounter_date, MAX(obs_value.value_datetime) max_date  FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "         INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "         INNER JOIN obs obs_value ON obs_value.encounter_id = e.encounter_id  "
            + "         AND o.person_id = obs_value.person_id "
            + " WHERE e.encounter_type = ${52} "
            + " AND o.concept_id = ${23865} "
            + " AND o.value_coded = ${1065} "
            + " AND obs_value.concept_id = ${23866} "
            + " AND obs_value.value_datetime <= :startDate "
            + " AND e.voided = 0 "
            + " AND e.location_id = :location "
            + " AND p.voided = 0 "
            + " AND o.voided = 0 "
            + " GROUP BY p.patient_id) AS last_ficha GROUP BY last_ficha.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  // 17
  public DataDefinition getPatientsAndNextpickUpDateOnFichaMestre() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Next Drug pick-up Date on Ficha Mestre");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    valuesMap.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    valuesMap.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    valuesMap.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());

    String query =
        "  SELECT p.patient_id, DATE_ADD(obs_value.value_datetime, INTERVAL 30 DAY) return_date    FROM patient p "
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "        INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "            INNER JOIN obs obs_value ON e.encounter_id = obs_value.encounter_id "
            + "       "
            + "        INNER JOIN (  "
            + "   "
            + "              SELECT p.patient_id, MAX(e.encounter_datetime) recent_encounter FROM patient p "
            + "              INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                      INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                      INNER JOIN obs obs_value ON o.encounter_id = obs_value.encounter_id "
            + "              WHERE e.encounter_type = ${52} "
            + "              AND o.concept_id = ${23865} "
            + "              AND o.value_coded = ${1065}   "
            + "              AND obs_value.concept_id = ${23866} "
            + "              AND obs_value.value_datetime <= :startDate "
            + "              AND e.voided = 0 "
            + "              AND e.location_id = :location "
            + "              AND p.voided = 0 "
            + "              AND o.voided = 0 "
            + "              GROUP BY p.patient_id) recent_arv ON recent_arv.patient_id = p.patient_id          "
            + "               WHERE e.encounter_type = ${52} "
            + "               AND e.encounter_datetime = recent_arv.recent_encounter "
            + "              AND o.concept_id = ${23865}    "
            + "              AND o.value_coded = ${1065}   "
            + "              AND obs_value.concept_id = ${23866} "
            + "              AND obs_value.value_datetime <= :startDate "
            + ""
            + "              AND e.voided = 0 "
            + "              AND e.location_id = :location "
            + "              AND p.voided = 0 "
            + "              AND o.voided = 0 "
            + "              GROUP BY p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }
  // 18
  public DataDefinition getPatientsAndNumberOfAPSSAndPPAfterHadVLGreaterThan1000() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Nr de sessões de APSS e PP após CV");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

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
        " SELECT  "
            + "     p.patient_id, COUNT(e.encounter_id) "
            + " FROM "
            + "     patient p "
            + "         INNER JOIN "
            + "     encounter e ON e.patient_id = p.patient_id "
            + "         INNER JOIN "
            + "     (SELECT  "
            + "         p.patient_id, MAX(o.obs_datetime) recent_date "
            + "     FROM "
            + "         patient p "
            + "     INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "     INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "     WHERE "
            + "         e.encounter_type = ${53} "
            + "             AND o.concept_id = ${856} "
            + "             AND o.value_numeric >= 1000 "
            + "             AND o.obs_datetime <= :startDate "
            + "             AND e.location_id = :location "
            + "             AND e.voided = 0 "
            + "             AND p.voided = 0 "
            + "             AND o.voided = 0 "
            + "     GROUP BY patient_id UNION SELECT  "
            + "         p.patient_id, MAX(e.encounter_datetime) recent_date "
            + "     FROM "
            + "         patient p "
            + "     INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "     INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "     WHERE "
            + "         e.encounter_type IN (${13} , ${6}, ${9}, ${51}) "
            + "             AND o.concept_id = ${856} "
            + "             AND o.value_numeric >= 1000 "
            + "             AND e.encounter_datetime <= :startDate "
            + "             AND e.location_id = :location "
            + "             AND e.voided = 0 "
            + "             AND p.voided = 0 "
            + "             AND o.voided = 0 "
            + "     GROUP BY p.patient_id) AS most_recent_vl ON most_recent_vl.patient_id = p.patient_id "
            + " WHERE "
            + "     e.encounter_type = ${35} "
            + "         AND e.encounter_datetime >= most_recent_vl.recent_date "
            + "         AND e.encounter_datetime <= :startDate "
            + "         AND e.location_id = :location "
            + "         AND e.voided = 0 "
            + "         AND p.voided = 0 "
            + " GROUP BY patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  public DataDefinition getContact() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Get Patient Contact");

    Map<String, Integer> map = new HashMap<>();

    String query =
        "  SELECT p.patient_id, pc.telemovel FROM patient p  "
            + "     INNER JOIN paciente_com_celular pc on p.patient_id = pc.patient_id  "
            + "  WHERE p.voided =0 GROUP BY p.patient_id; ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  public DataDefinition getPatientBirthdate() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Patient Birthdate from Demographics");
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();

    String query =
        " SELECT p.patient_id, "
            + "        person.birthdate "
            + " FROM   patient p "
            + "        INNER JOIN person person "
            + "                ON p.patient_id = person_id AND p.voided = 0  "
            + " GROUP  BY p.patient_id  ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }
}
