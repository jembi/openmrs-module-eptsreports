package org.openmrs.module.eptsreports.reporting.library.queries;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;

public class ListOfPatientsWithMdsEvaluationQueries {
  private static HivMetadata hivMetadata = new HivMetadata();
  private static TbMetadata tbMetadata = new TbMetadata();
  private static CommonMetadata commonMetadata = new CommonMetadata();

  public static String getPatientArtStart(String inclusionEndMonthAndDay) {

    String query =
        " SELECT first.patient_id, MIN(first.pickup_date) first_pickup "
            + "       FROM ( "
            + " SELECT p.patient_id, "
            + "       Min(e.encounter_datetime) pickup_date "
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
            + "       Min(o2.value_datetime) pickup_date "
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
            + "            AND o2.value_datetime <= CONCAT(:evaluationYear, "
            + inclusionEndMonthAndDay
            + "        ) "
            + "GROUP  BY p.patient_id "
            + "        ) first "
            + "     GROUP BY first.patient_id ";

    Map<String, Integer> map = new HashMap<>();
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    return stringSubstitutor.replace(query);
  }

  public static String getTranferredPatients(
      String inclusionEndMonthAndDay, int numberOfYearsEndDate) {

    String query =
        " SELECT patient_id "
            + " FROM ( "
            + "SELECT p.patient_id "
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
            + " UNION "
            + "				SELECT patient_id "
            + "				FROM ( "
            + "				SELECT p.patient_id, "
            + "                     MAX(e.encounter_datetime) AS last_transfer "
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
            + "				AND e.encounter_datetime <= DATE_SUB( "
            + "  CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) ,INTERVAL "
            + numberOfYearsEndDate
            + " YEAR) "
            + "              GROUP BY p.patient_id "
            + "    ) transferred_out_clinical "
            + " UNION "
            + "				SELECT patient_id "
            + "				FROM ( "
            + "				SELECT p.patient_id, "
            + "				  	   MAX(o.obs_datetime) AS last_transfer "
            + "				FROM   patient p "
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
            + "        ) ,INTERVAL "
            + numberOfYearsEndDate
            + " YEAR) "
            + "                  GROUP BY   p.patient_id "
            + "    ) transferred_out_resumo "
            + " ) transferred ";

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    return stringSubstitutor.replace(query);
  }

  /**
   * O registo de “Diagnóstico TB Activa” (resposta = “Sim”) numa Ficha Clínica (“Data Consulta”)
   * registada no período de inclusão
   *
   * @param inclusionEndMonthAndDay
   * @return String
   */
  public static String getTbActive(String inclusionEndMonthAndDay) {

    String query =
        " SELECT     p.patient_id, "
            + "          e.encounter_datetime AS encounter_date "
            + " FROM       patient p "
            + " INNER JOIN encounter e "
            + " ON         e.patient_id = p.patient_id "
            + " INNER JOIN obs o "
            + " ON         o.encounter_id = e.encounter_id "
            + " INNER JOIN ( "
            + "               SELECT art_patient.patient_id, "
            + "                      art_patient.first_pickup AS art_encounter "
            + "               FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientArtStart(inclusionEndMonthAndDay)
            + "                       ) art_patient "
            + " ) art ON art.patient_id = p.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND e.encounter_type = ${6} "
            + " AND e.location_id = :location "
            + " AND o.concept_id = ${23761} "
            + " AND o.value_coded = ${1065} "
            + " AND e.encounter_datetime >= art.art_encounter "
            + " AND e.encounter_datetime <= DATE_ADD( art.art_encounter, INTERVAL 33 DAY ) "
            + " GROUP  BY p.patient_id ";

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("23761", hivMetadata.getActiveTBConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    return stringSubstitutor.replace(query);
  }

  public static String getTBSymptoms(String inclusionEndMonthAndDay) {

    String query =
        " SELECT     p.patient_id, "
            + "          e.encounter_datetime AS encounter_date "
            + " FROM       patient p "
            + " INNER JOIN encounter e "
            + " ON         e.patient_id = p.patient_id "
            + " INNER JOIN obs o "
            + " ON         o.encounter_id = e.encounter_id "
            + " INNER JOIN ( "
            + "             SELECT art_patient.patient_id, "
            + "                    art_patient.first_pickup AS art_encounter "
            + "             FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientArtStart(inclusionEndMonthAndDay)
            + "                    ) art_patient "
            + " ) art ON art.patient_id = p.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND e.encounter_type = ${6} "
            + " AND e.location_id = :location "
            + " AND o.concept_id = ${23758} "
            + " AND o.value_coded = ${1065} "
            + " AND e.encounter_datetime >= art.art_encounter "
            + " AND e.encounter_datetime <= DATE_ADD( art.art_encounter, INTERVAL 33 DAY ) "
            + " GROUP  BY p.patient_id ";

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("23758", hivMetadata.getTBSymptomsConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    return stringSubstitutor.replace(query);
  }

  public static String getTBSymptomsTypes(String inclusionEndMonthAndDay) {
    String query =
        " SELECT     p.patient_id, "
            + "          e.encounter_datetime AS encounter_date "
            + " FROM       patient p "
            + " INNER JOIN encounter e "
            + " ON         e.patient_id = p.patient_id "
            + " INNER JOIN obs o "
            + " ON         o.encounter_id = e.encounter_id "
            + " INNER JOIN ( "
            + "              SELECT art_patient.patient_id, "
            + "                     art_patient.first_pickup AS art_encounter "
            + "              FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientArtStart(inclusionEndMonthAndDay)
            + "                     ) art_patient "
            + " ) art ON art.patient_id = p.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND e.encounter_type = ${6} "
            + " AND e.location_id = :location "
            + " AND o.concept_id = ${1766} "
            + " AND o.value_coded IN( ${1763}, ${1764}, ${1762}, ${1760}, "
            + "                       ${23760}, ${1765}, ${161} ) "
            + " AND e.encounter_datetime >= art.art_encounter "
            + " AND e.encounter_datetime <= DATE_ADD( art.art_encounter, INTERVAL 33 DAY ) "
            + " GROUP  BY p.patient_id ";

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1766", tbMetadata.getObservationTB().getConceptId());
    map.put("1763", tbMetadata.getFeverLastingMoraThan3Weeks().getConceptId());
    map.put("1764", tbMetadata.getWeightLossOfMoreThan3KgInLastMonth().getConceptId());
    map.put("1762", tbMetadata.getNightsWeatsLastingMoraThan3Weeks().getConceptId());
    map.put("1760", tbMetadata.getCoughLastingMoraThan3Weeks().getConceptId());
    map.put("23760", tbMetadata.getAsthenia().getConceptId());
    map.put("1765", tbMetadata.getCohabitantBeingTreatedForTB().getConceptId());
    map.put("161", tbMetadata.getLymphadenopathy().getConceptId());

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    return stringSubstitutor.replace(query);
  }

  public static String getTbTreatment(String inclusionEndMonthAndDay) {
    String query =
        " SELECT     p.patient_id, "
            + "          o.obs_datetime AS encounter_date "
            + " FROM       patient p "
            + " INNER JOIN encounter e "
            + " ON         e.patient_id = p.patient_id "
            + " INNER JOIN obs o "
            + " ON         o.encounter_id = e.encounter_id "
            + " INNER JOIN ( "
            + "               SELECT art_patient.patient_id, "
            + "                      art_patient.first_pickup AS art_encounter "
            + "               FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientArtStart(inclusionEndMonthAndDay)
            + "                      ) art_patient "
            + " ) art ON art.patient_id = p.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND e.encounter_type = ${6} "
            + " AND e.location_id = :location "
            + " AND o.concept_id = ${1268} "
            + " AND o.value_coded IN ( ${1256}, ${1257}, ${1267} ) "
            + " AND o.obs_datetime >= art.art_encounter "
            + " AND o.obs_datetime <= DATE_ADD( art.art_encounter, INTERVAL 33 DAY ) "
            + " GROUP  BY p.patient_id ";

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1268", hivMetadata.getTBTreatmentPlanConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("1257", hivMetadata.getContinueRegimenConcept().getConceptId());
    map.put("1267", hivMetadata.getCompletedConcept().getConceptId());

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    return stringSubstitutor.replace(query);
  }

  public static String getImportantMedicalConditions(String inclusionEndMonthAndDay) {
    String query =
        " SELECT     p.patient_id, "
            + "          o.obs_datetime AS encounter_date "
            + " FROM       patient p "
            + " INNER JOIN encounter e "
            + " ON         e.patient_id = p.patient_id "
            + " INNER JOIN obs o "
            + " ON         o.encounter_id = e.encounter_id "
            + " INNER JOIN ( "
            + "               SELECT art_patient.patient_id, "
            + "                      art_patient.first_pickup AS art_encounter "
            + "               FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientArtStart(inclusionEndMonthAndDay)
            + "                      ) art_patient "
            + " ) art ON art.patient_id = p.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND e.encounter_type = ${53} "
            + " AND e.location_id = :location "
            + " AND o.concept_id = ${1406} "
            + " AND o.value_coded = ${42} "
            + " AND o.obs_datetime >= art.art_encounter "
            + " AND o.obs_datetime <= DATE_ADD( art.art_encounter, INTERVAL 33 DAY ) "
            + " GROUP  BY p.patient_id ";

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1406", hivMetadata.getOtherDiagnosis().getConceptId());
    map.put("42", tbMetadata.getPulmonaryTB().getConceptId());

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    return stringSubstitutor.replace(query);
  }
}
