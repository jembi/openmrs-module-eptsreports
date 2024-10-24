package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.library.queries.advancedhivillness.ListOfPatientsOnAdvancedHivIllnessQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsQueriesUtil;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsEligibleForVLDataDefinitionQueries {

  private HivMetadata hivMetadata;
  private CommonMetadata commonMetadata;

  private final ListOfPatientsOnAdvancedHivIllnessQueries listOfPatientsOnAdvancedHivIllnessQueries;

  @Autowired
  public ListOfPatientsEligibleForVLDataDefinitionQueries(
      HivMetadata hivMetadata,
      CommonMetadata commonMetadata,
      ListOfPatientsOnAdvancedHivIllnessQueries listOfPatientsOnAdvancedHivIllnessQueries) {
    this.hivMetadata = hivMetadata;
    this.commonMetadata = commonMetadata;
    this.listOfPatientsOnAdvancedHivIllnessQueries = listOfPatientsOnAdvancedHivIllnessQueries;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <p>Patient ART Start Date is the oldest date from the set of criterias defined in the common
   * query:
   *
   * </blockquote>
   *
   * @return {@link DataDefinition}
   */
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
    valuesMap.put("2", hivMetadata.getARTProgram().getProgramId());

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
            + " SELECT p.patient_id, MIN(o.value_datetime) art_date FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + " INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + " WHERE e.encounter_type IN(${6},${9},${18},${53}) "
            + " AND o.concept_id = ${1190} "
            + " AND e.location_id = :location "
            + "                 AND o.value_datetime <= :endDate "
            + " AND e.voided = 0 "
            + " AND p.voided = 0 "
            + " AND o.voided = 0 "
            + " GROUP BY p.patient_id "
            + "                  "
            + " UNION "
            + "  "
            + " SELECT p.patient_id, pg.date_enrolled AS art_date "
            + "     FROM   patient p   "
            + "           INNER JOIN patient_program pg  "
            + "                ON p.patient_id = pg.patient_id  "
            + "        INNER JOIN patient_state ps  "
            + "                   ON pg.patient_program_id = ps.patient_program_id  "
            + "     WHERE  pg.location_id = :location AND pg.voided = 0 AND p.voided = 0"
            + "    AND pg.program_id = ${2} and pg.date_enrolled <= :endDate "
            + "     "
            + "    UNION "
            + "     "
            + "    SELECT p.patient_id,  MIN(o.value_datetime) AS art_date FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + " INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                         INNER JOIN obs oyes ON oyes.encounter_id = e.encounter_id  "
            + "                         AND o.person_id = oyes.person_id "
            + " WHERE e.encounter_type = ${52} "
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

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <p>The last Linha TARV(concept id 21151) registered on FICHA CLINICA (encounter_type 6) by
   * reporting startDate
   *
   * </blockquote>
   *
   * @return {@link DataDefinition}
   */
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

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <p>The Most Recent Viral Load Result Date (concept Id 856 - value_numeric > 0 OR concept Id
   * 1305 - value_coded not null) registered in the Laboratory or Ficha de Seguimento (Adulto or
   * Pediatria) or Ficha Clinica or Ficha Resumo or FSR ( encounter_type 6, 9, 13, 51 -
   * encounter_datetime, encounter_type 53 - obs_datetime) form by start end of reporting period (
   * <= startDate). Note: the most recent record date should be listed ( encounter_datetime for
   * encounter_type 6,9,13,51, obs_datetime for encounter_type 53)
   *
   * </blockquote>
   *
   * @return {@link DataDefinition}
   */
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

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <p>The <b> Most Recent Viral Load Result</b> (concept Id 856 - value_numeric > 0 OR concept Id
   * 1305 - value_coded not null) registered in the Laboratory or Ficha de Seguimento (Adulto or
   * Pediatria) or Ficha Clinica or Ficha Resumo or FSR ( encounter_type 6, 9, 13, 51 -
   * encounter_datetime, encounter_type 53 - obs_datetime) form by start end of reporting period (
   * <= startDate). Note: the most recent record result should be listed ( value_numeric for
   * concept_id 856 or value_coded for concept id 1305)
   *
   * </blockquote>
   *
   * @return {@link DataDefinition}
   */
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

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <p>Print the Date (encounter_datetime) of the most recent clinical consultation registered on
   * Ficha Clínica – MasterCard or Ficha de Seguimento (encounter type 6 or 9) by report start date
   * (encounter_datetime <= startDate)
   *
   * </blockquote>
   *
   * @return {@link DataDefinition}
   */
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
            + " WHERE e.encounter_type IN(${6},${9}) "
            + " AND e.encounter_datetime <= :startDate "
            + " AND e.location_id = :location "
            + " AND e.voided = 0 "
            + " AND p.voided = 0 "
            + " GROUP BY p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <p>Print the “Data da próxima consulta” (concept id 1410, value_datetime) of the most recent
   * clinical consultation registered on Ficha Clínica – MasterCard or Ficha de Seguimento
   * (encounter type 6 or 9) until report start date (encounter_datetime <= startDate)
   *
   * </blockquote>
   *
   * @return {@link DataDefinition}
   */
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
        "SELECT p.patient_id,o.value_datetime "
            + "FROM   patient p "
            + "       INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "       INNER JOIN (SELECT p.patient_id,MAX(e.encounter_datetime) encounter_datetime "
            + "                   FROM   patient p "
            + "                          INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                   WHERE  e.encounter_type IN( ${6}, ${9} ) "
            + "                          AND e.encounter_datetime <= :startDate "
            + "                          AND e.voided = 0 "
            + "                          AND e.location_id = :location "
            + "                          AND p.voided = 0 "
            + "                   GROUP  BY p.patient_id) most_recent "
            + "               ON most_recent.patient_id = p.patient_id "
            + "WHERE  e.encounter_type IN( ${6}, ${9} ) "
            + "       AND o.concept_id = ${1410} "
            + "       AND e.encounter_datetime = most_recent.encounter_datetime "
            + "       AND e.voided = 0 "
            + "       AND e.location_id = :location "
            + "       AND p.voided = 0 "
            + "       AND o.voided = 0 "
            + "GROUP  BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <p>Print the Date (encounter_datetime) of the most recent drugs pick up registered on FILA
   * (encounter type 18) until report start date (encounter_datetime <= startDate)
   *
   * </blockquote>
   *
   * @return {@link DataDefinition}
   */
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

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <p>Print the Date (concept id 23866, value_datetime) of the most recent “Recepcao Levantou ARV”
   * (encounter type 52) with concept “Levantou ARV” (concept_id 23865) set to “SIM” (Concept id
   * 1065) until report start date (concept id 23866, value_datetime<= startDate)
   *
   * </blockquote>
   *
   * @return {@link DataDefinition}
   */
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

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <p>Print the “Data do próximo levantamento” (concept id 5096, value_datetime) of the most
   * recent FILA (encounter type 18) until report start date(encounter_datetime <= startDate)
   *
   * </blockquote>
   *
   * @return {@link DataDefinition}
   */
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

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <p>Return the Date (concept id 23866, value_datetime) +30 days of the most recent “Recepcao
   * Levantou ARV” (encounter type 52) with concept “Levantou ARV” (concept_id 23865) set to “SIM”
   * (Concept id 1065) by report start date ( concept id 23866, value_datetime<= startDate)
   *
   * </blockquote>
   *
   * @return {@link DataDefinition}
   */
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

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <p>Count and print the Number of APSS and PP consultations, registered in Ficha APSS/PP
   * (encounter_type 35), the patient had between the <b>Date of most recent VL with Result >=1000
   * copies/ml</b> by report start date and the report start date Note: “Date of most recent VL with
   * Result >=1000 copies/ml” = “Most Recent VL Date 3” defined in VL5 (see above)
   *
   * </blockquote>
   *
   * @return {@link DataDefinition}
   */
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

  /**
   * <b>The system will show Data do Último Pedido de Carga Viral as follows:</b>
   *
   * <ul>
   *   <li>Data do Último Pedido de Carga Viral – Sheet 1: Column J The most recent clinical
   *       consultation (FIcha Clínica) date by report start date, with a viral load request
   *       registered.
   * </ul>
   *
   * @return DataDefinition *
   */
  public DataDefinition getPatientsLastVLRequestDate() {
    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();
    spdd.setName("Patient's Last VL Request Date");
    spdd.addParameter(new Parameter("location", "location", Location.class));
    spdd.addParameter(new Parameter("endDate", "endDate", Date.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());

    String sql =
        " SELECT p.patient_id, MAX(e.encounter_datetime) "
            + " FROM patient p "
            + "	  INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "   INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + " WHERE p.voided = 0 "
            + "	  AND e.voided = 0 "
            + "   AND o.voided = 0 "
            + "   AND e.location_id = :location "
            + "   AND e.encounter_type = ${6} "
            + "	  AND o.concept_id = ${23722} "
            + "   AND o.value_coded = ${856} "
            + " GROUP BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(map);

    spdd.setQuery(substitutor.replace(sql));
    return spdd;
  }

  public DataDefinition getARTExitDate() {
    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();
    spdd.setName("Patient's Last VL Request Date");
    spdd.addParameter(new Parameter("location", "location", Location.class));
    spdd.addParameter(new Parameter("endDate", "endDate", Date.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());

    String sql =
        "SELECT patient_id, most_recent_next_pickup "
            + "  FROM ( "
            + "    SELECT patient_id, MAX(next_pickup) most_recent_next_pickup "
            + "    FROM ( "
            + "    	SELECT p.patient_id, next_pickup "
            + "      FROM patient p "
            + "        INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "        INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "        INNER JOIN ( "
            + "          SELECT p.patient_id, MAX(e.encounter_datetime) next_pickup "
            + "          FROM   patient p "
            + "            INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "            INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "          WHERE p.voided = 0 "
            + "            AND e.voided = 0 "
            + "            AND o.voided = 0 "
            + "            AND e.encounter_type = ${18} "
            + "            AND e.encounter_datetime <= :endDate "
            + "            AND e.location_id = :location "
            + "          GROUP BY p.patient_id "
            + "        ) last_fila ON last_fila.patient_id = p.patient_id "
            + "      WHERE p.voided = 0 "
            + "        AND e.voided = 0 "
            + "        AND o.voided = 0 "
            + "        AND e.encounter_type = ${18} "
            + "        AND o.concept_id = ${5096} "
            + "        AND o.value_datetime IS NOT NULL "
            + "        AND e.encounter_datetime = last_fila.next_pickup "
            + "        AND e.location_id = :location "
            + "      GROUP BY p.patient_id "
            + " "
            + "      UNION "
            + " "
            + "      SELECT p.patient_id, DATE_ADD( MAX(o.value_datetime), interval 30 DAY) next_pickup "
            + "      FROM patient p "
            + "        INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "        INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "      WHERE p.voided = 0 "
            + "        AND e.voided = 0 "
            + "        AND o.voided = 0 "
            + "        AND e.encounter_type = ${52} "
            + "        AND o.concept_id = ${23866} "
            + "        AND o.value_datetime IS NOT NULL "
            + "        AND o.value_datetime <= :endDate "
            + "        AND e.location_id = :location "
            + "      GROUP BY p.patient_id "
            + "    ) AS max "
            + "    GROUP BY patient_id "
            + "  ) AS next_pickup "
            + "  WHERE DATE_ADD(most_recent_next_pickup, INTERVAL 28 DAY) < :endDate";

    StringSubstitutor substitutor = new StringSubstitutor(map);

    spdd.setQuery(substitutor.replace(sql));
    return spdd;
  }

  public String getPatientsWhoAbandonedTarvQuery() {
    return " SELECT patient_id, 'Abandono/IIT' "
        + "  FROM ( "
        + "    SELECT patient_id, MAX(next_pickup) most_recent_next_pickup "
        + "    FROM ( "
        + "    SELECT p.patient_id, next_pickup "
        + "      FROM patient p "
        + "        INNER JOIN encounter e ON e.patient_id = p.patient_id "
        + "        INNER JOIN obs o ON o.encounter_id = e.encounter_id "
        + "        INNER JOIN ( "
        + "          SELECT p.patient_id, MAX(e.encounter_datetime) next_pickup "
        + "          FROM   patient p "
        + "            INNER JOIN encounter e ON e.patient_id = p.patient_id "
        + "            INNER JOIN obs o ON o.encounter_id = e.encounter_id "
        + "          WHERE p.voided = 0 "
        + "            AND e.voided = 0 "
        + "            AND o.voided = 0 "
        + "            AND e.encounter_type = ${18} "
        + "            AND e.encounter_datetime <= :endDate "
        + "            AND e.location_id = :location "
        + "          GROUP BY p.patient_id "
        + "        ) last_fila ON last_fila.patient_id = p.patient_id "
        + "      WHERE p.voided = 0 "
        + "        AND e.voided = 0 "
        + "        AND o.voided = 0 "
        + "        AND e.encounter_type = ${18} "
        + "        AND o.concept_id = ${5096} "
        + "        AND o.value_datetime IS NOT NULL "
        + "        AND e.encounter_datetime = last_fila.next_pickup "
        + "        AND e.location_id = :location "
        + "      GROUP BY p.patient_id "
        + " "
        + "      UNION "
        + " "
        + "      SELECT p.patient_id, DATE_ADD( MAX(o.value_datetime), interval 30 DAY) next_pickup "
        + "      FROM patient p "
        + "        INNER JOIN encounter e ON e.patient_id = p.patient_id "
        + "        INNER JOIN obs o ON o.encounter_id = e.encounter_id "
        + "      WHERE p.voided = 0 "
        + "        AND e.voided = 0 "
        + "        AND o.voided = 0 "
        + "        AND e.encounter_type = ${52} "
        + "        AND o.concept_id = ${23866} "
        + "        AND o.value_datetime IS NOT NULL "
        + "        AND o.value_datetime <= :endDate "
        + "        AND e.location_id =  :location "
        + "      GROUP BY p.patient_id "
        + "    ) AS max "
        + "    GROUP BY patient_id "
        + "  ) AS most_recent_next_pickup "
        + "  WHERE DATE_ADD(most_recent_next_pickup, INTERVAL 28 DAY) < :endDate "
        + "   "
        + "  UNION "
        + "   "
        + "  SELECT p.patient_id, 'Abandono/IIT' "
        + "  FROM patient p "
        + "    INNER JOIN encounter e ON e.patient_id = p.patient_id "
        + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
        + "    INNER JOIN ( "
        + "      SELECT p.patient_id "
        + "      FROM patient p "
        + "      	INNER JOIN encounter e ON e.patient_id = p.patient_id "
        + "        INNER JOIN obs o ON o.encounter_id = e.encounter_id "
        + "        INNER JOIN ( "
        + "          SELECT p.patient_id, MAX(e.encounter_datetime) last_encounter "
        + "          FROM   patient p "
        + "            INNER JOIN encounter e ON e.patient_id = p.patient_id "
        + "            INNER JOIN obs o ON o.encounter_id = e.encounter_id "
        + "          WHERE p.voided = 0 "
        + "            AND e.voided = 0 "
        + "            AND o.voided = 0 "
        + "            AND e.encounter_type = ${18} "
        + "            AND e.encounter_datetime BETWEEN :startDate AND :endDate "
        + "            AND e.location_id = :location "
        + "          GROUP BY p.patient_id "
        + "        ) last_encounter ON last_encounter.patient_id = p.patient_id "
        + "        WHERE p.voided = 0 "
        + "            AND e.voided = 0 "
        + "            AND o.voided = 0 "
        + "            AND e.encounter_type = ${18} "
        + "            AND e.encounter_datetime = last_encounter.last_encounter "
        + "            AND ( "
        + "            			(o.concept_id = ${5096} AND o.value_datetime IS NULL) "
        + "                	OR "
        + "                  (${5096} NOT IN ( "
        + "                    SELECT concept_id "
        + "                    FROM obs o "
        + "                    WHERE o.encounter_id = e.encounter_id "
        + "                    	AND o.voided = 0 "
        + "                  )) "
        + "            ) "
        + "            AND e.location_id = :location "
        + "    ) no_next_pickup ON no_next_pickup.patient_id = p.patient_id "
        + "    WHERE o.voided = 0 AND p.voided = 0 AND e.voided = 0 AND e.location_id = :location "
        + "    		AND e.encounter_type = ${52} "
        + "  	    AND ( "
        + "        	 ( o.concept_id = ${23866} AND o.value_datetime IS NULL ) "
        + "        		OR "
        + "        	 (${23866} NOT IN ( "
        + "              SELECT concept_id "
        + "              FROM obs o "
        + "              WHERE o.encounter_id = e.encounter_id "
        + "                AND o.voided = 0 "
        + "            )) "
        + "    	)";
  }

  /**
   * <b>The system will classify patient exit status by report end date into the following
   * categories:</b>
   *
   * <ul>
   *   <li>Abandono/IIT - Patients who experienced interruption in treatment (VL_ELG_FR22.1)
   *   <li>Óbito/Died - Patients who died (VL_ELG_FR22.2)
   *   <li>Transferido Para/Transferred Out - Patients who transferred out to another health
   *       facility (VL_ELG_FR22.3)
   *   <li>Suspenso/Stopped/Suspended Treatment - Patients who suspended ART (VL_ELG_FR22.4)
   * </ul>
   *
   * @return DataDefinition *
   */
  public DataDefinition getARTExitStatus() {
    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Exit from ART/Saída de TARV");
    spdd.addParameter(new Parameter("startDate", "startDate", Date.class));
    spdd.addParameter(new Parameter("endDate", "endDate", Date.class));
    spdd.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("1", hivMetadata.getHIVCareProgram().getProgramId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("8", hivMetadata.getSuspendedTreatmentWorkflowState().getProgramWorkflowStateId());
    map.put("10", hivMetadata.getArtDeadWorkflowState().getProgramWorkflowStateId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1366", hivMetadata.getPatientHasDiedConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("1709", hivMetadata.getSuspendedTreatmentConcept().getConceptId());
    map.put("1705", hivMetadata.getRestartConcept().getConceptId());
    map.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6275", hivMetadata.getPreTarvConcept().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());
    map.put(
        "28",
        hivMetadata
            .getArtCareTransferredFromOtherHealthFacilityWorkflowState()
            .getProgramWorkflowStateId());
    map.put(
        "29",
        hivMetadata
            .getTransferredFromOtherHealthFacilityWorkflowState()
            .getProgramWorkflowStateId());

    String query =
        new EptsQueriesUtil()
            .unionBuilder(getPatientsWhoAbandonedTarvQuery())
            .union(listOfPatientsOnAdvancedHivIllnessQueries.getPatientsWhoDied(true))
            .union(
                listOfPatientsOnAdvancedHivIllnessQueries
                    .getPatientsWhoSuspendedTarvOrAreTransferredOut(
                        hivMetadata
                            .getSuspendedTreatmentWorkflowState()
                            .getProgramWorkflowStateId(),
                        hivMetadata.getSuspendedTreatmentConcept().getConceptId(),
                        false,
                        false))
            .buildQuery();

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    spdd.setQuery(stringSubstitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Consultation Date with last KPOP (PopChave) Informed- Sheet 1: Column T</b>
   * <li>Date of the most recent Clínical consultation with KPOP (PopChave) registered by report end
   *     date
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getLastKeyPopulationRegistrationDate() {
    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();
    spdd.setName("Patient's Most Recent Ficha Clinica with KPOP Registration Date");
    spdd.addParameter(new Parameter("location", "location", Location.class));
    spdd.addParameter(new Parameter("endDate", "endDate", Date.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("23703", hivMetadata.getKeyPopulationConcept().getConceptId());

    String sql =
        " SELECT p.patient_id AS patient_id, Max(e.encounter_datetime) AS last_date "
            + " FROM patient p "
            + " 	INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + " 	INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + " WHERE e.voided = 0 "
            + " 	AND p.voided = 0 "
            + " 	AND o.voided = 0 "
            + " 	AND e.location_id = :location "
            + " 	AND e.encounter_type = ${6} "
            + " 	AND o.concept_id = ${23703} "
            + " 	AND e.encounter_datetime <= CURRENT_DATE() "
            + " GROUP BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(map);

    spdd.setQuery(substitutor.replace(sql));
    return spdd;
  }
}
