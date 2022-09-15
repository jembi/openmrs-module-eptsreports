package org.openmrs.module.eptsreports.reporting.library.queries;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;

public class DsdQueries {

  public static String getPatientsEnrolledOnGAAC() {
    String query =
        "SELECT gm.member_id FROM gaac g "
            + "INNER JOIN gaac_member gm "
            + "ON g.gaac_id=gm.gaac_id "
            + "WHERE gm.start_date < :endDate "
            + "AND gm.voided = 0 "
            + "AND g.voided = 0 "
            + "AND ((leaving is null) OR (leaving = 0) OR (leaving = 1 AND gm.end_date > :endDate)) "
            + "AND location_id = :location";
    return query;
  }

  /*
   * Get Patients who participate in at least one of the following measured DSD model (AF, CA, PU, DC)
   *
   * @return String
   * */
  public static String getPatientsParticipatingInAfCaPuFrDcDsdModels() {

    String query =
        ""
            + "SELECT "
            + "	p.patient_id "
            + "FROM patient p INNER JOIN encounter e ON p.patient_id=e.patient_id "
            + "INNER JOIN obs o ON p.patient_id=o.person_id "
            + "WHERE e.encounter_id = %d "
            + "	AND o.concept_id IN (%d, %d, %d) "
            + "	AND o.value_coded IN (%d, %d) "
            + " AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "	AND e.location_id = :location";

    return String.format(
        query,
        new HivMetadata().getAdultoSeguimentoEncounterType().getEncounterTypeId(),
        new HivMetadata().getFamilyApproach().getConceptId(), // fa
        new HivMetadata().getAccessionClubs().getConceptId(), // ca
        new HivMetadata().getCommunityDispensation().getConceptId(), // dc
        new HivMetadata().getStartDrugs().getConceptId(),
        new HivMetadata().getContinueRegimenConcept().getConceptId());
  }

  /**
   * N5: Number of active patients on ART (Non-pregnant and Non-Breastfeeding not on TB treatment)
   * who are in AF
   *
   * @param encounterTypeId - encounterType
   * @param lastCommunityConceptId - last Community Concept
   * @param startDrugsConceptId - start Drugs Concept
   * @param continueRegimenConceptId - continue Regimen Concept
   * @return String
   */
  public static String getPatientsWithDispense(
      int encounterTypeId,
      int lastCommunityConceptId,
      int startDrugsConceptId,
      int continueRegimenConceptId) {
    String query =
        "select "
            + " p.patient_id FROM patient p "
            + " JOIN  "
            + " encounter e ON "
            + "    p.patient_id = e.patient_id "
            + " JOIN "
            + " obs o  ON "
            + "    p.patient_id = o.person_id "
            + " WHERE "
            + " e.encounter_type=%d "
            + "    AND o.concept_id=%d "
            + "    AND o.value_coded in (%d,%d) AND e.location_id= :location  "
            + "    AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "    AND e.voided=0 AND o.voided=0 AND p.voided=0";

    return String.format(
        query,
        encounterTypeId,
        lastCommunityConceptId,
        startDrugsConceptId,
        continueRegimenConceptId);
  }
  /**
   * N5: Number of active patients on ART (Non-pregnant and Non-Breastfeeding not on TB treatment)
   * who are in AF
   *
   * @param adultSeguimentoEncounterTypeId {@link HivMetadata#getAdultoSeguimentoEncounterType()}
   * @param lastFamilyApproachConceptId - last Family Approach ConceptId
   * @param startDrugsConceptId - start Drugs ConceptId
   * @param continueRegimenConceptId - continue Regimen ConceptId
   * @return String
   */
  public static String getPatientsOnMasterCardAF(
      int adultSeguimentoEncounterTypeId,
      int lastFamilyApproachConceptId,
      int startDrugsConceptId,
      int continueRegimenConceptId) {
    Map<String, Integer> map = new HashMap<>();
    map.put("adultSeguimentoEncounterTypeId", adultSeguimentoEncounterTypeId);
    map.put("lastFamilyApproachConceptId", lastFamilyApproachConceptId);
    map.put("startDrugsConceptId", startDrugsConceptId);
    map.put("continueRegimenConceptId", continueRegimenConceptId);
    String query =
        "SELECT last_abordagem_familiar.patient_id "
            + "FROM ( "
            + "      SELECT p.patient_id, max(e.encounter_datetime)  "
            + "      FROM patient p  "
            + "        JOIN encounter e ON p.patient_id=e.patient_id  "
            + "        JOIN obs o ON e.encounter_id = o.encounter_id  "
            + "      WHERE e.encounter_type= ${adultSeguimentoEncounterTypeId}  "
            + "        AND o.concept_id= ${lastFamilyApproachConceptId}  "
            + "        AND o.value_coded IN (${startDrugsConceptId},${continueRegimenConceptId})  "
            + "        AND e.location_id= :location  "
            + "        AND e.encounter_datetime <= :endDate   "
            + "        AND e.voided=0  "
            + "        AND o.voided=0  "
            + "        AND p.voided=0  "
            + "      GROUP BY p.patient_id "
            + "      ) last_abordagem_familiar";

    StringSubstitutor sb = new StringSubstitutor(map);

    return sb.replace(query);
  }
  /**
   * Get All Patients On Sarcoma Karposi
   *
   * @param adultSeguimentoEncounter - {@link HivMetadata#getAdultoSeguimentoEncounterType()}
   * @param pediatriaSeguimentoEncounter - {@link {@link
   *     HivMetadata#getPediatriaSeguimentoEncounterType()}}
   * @param otherDiagnosisConceptId - other Diagnosis ConceptId
   * @param sarcomakarposiConceptId - sarcomakarposi ConceptId
   * @return String
   */
  public static String getPatientsOnSarcomaKarposi(
      int adultSeguimentoEncounter,
      int pediatriaSeguimentoEncounter,
      int otherDiagnosisConceptId,
      int sarcomakarposiConceptId) {
    String query =
        ""
            + "SELECT "
            + " p.patient_id "
            + "FROM patient p "
            + "INNER JOIN encounter e ON p.patient_id=e.patient_id "
            + "INNER JOIN obs o ON e.encounter_id=o.encounter_id "
            + "WHERE "
            + "e.encounter_type IN (%d,%d) AND o.concept_id=%d AND o.value_coded=%d AND e.location_id= :location AND e.encounter_datetime<= :endDate AND p.voided=0 AND e.voided=0 AND o.voided=0";

    return String.format(
        query,
        adultSeguimentoEncounter,
        pediatriaSeguimentoEncounter,
        otherDiagnosisConceptId,
        sarcomakarposiConceptId);
  }

  /**
   * <b>Patients With Type Of Dispensation On Last MDC Record</b>
   * <li>All active patients whose one of the MDCs is marked as “@param dispensationTypes” with
   *     Estado do MDC as Iniciar (I) or Continuar (C) in the last Ficha Clinica with MDCs
   *     registered
   *
   * @param dispensationTypes The List of dispensation types concepts
   * @param states The list of MDC states
   * @return {@link CohortDefinition}
   */
  public static SqlCohortDefinition getPatientsWithTypeOfDispensationOnLastMdcRecord(
      List<Integer> dispensationTypes, List<Integer> states) {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName(
        "All active patients whose one of the MDCs is marked as “DT OR DS OR DA” with Iniciar or Continuar in the last Ficha Clinica with MDC");
    cd.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, String> map = new HashMap<>();
    HivMetadata hivMetadata = new HivMetadata();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId().toString());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId().toString());
    map.put("165322", hivMetadata.getMdcState().getConceptId().toString());
    map.put("dispensationTypes", StringUtils.join(dispensationTypes, ","));
    map.put("states", StringUtils.join(states, ","));

    String query =
        " SELECT p.patient_id   "
            + "FROM   patient p   "
            + "           INNER JOIN encounter e ON e.patient_id = p.patient_id   "
            + "           INNER JOIN obs otype ON otype.encounter_id = e.encounter_id   "
            + "           INNER JOIN obs ostate ON ostate.encounter_id = e.encounter_id   "
            + "           INNER JOIN (    "
            + "    SELECT p.patient_id, MAX(e.encounter_datetime) AS last_encounter   "
            + "    FROM   patient p   "
            + "               INNER JOIN encounter e ON p.patient_id = e.patient_id   "
            + "               INNER JOIN obs otype ON otype.encounter_id = e.encounter_id   "
            + "               INNER JOIN obs ostate ON ostate.encounter_id = e.encounter_id   "
            + "    WHERE  p.voided = 0    "
            + "      AND e.voided = 0   "
            + "      AND otype.voided = 0   "
            + "      AND ostate.voided = 0    "
            + "      AND e.encounter_type = ${6}   "
            + "      AND otype.concept_id = ${165174}    "
            + "      AND otype.value_coded IS NOT NULL "
            + "      AND ostate.concept_id = ${165322}   "
            + "      AND ostate.value_coded IS NOT NULL   "
            + "      AND otype.obs_group_id = ostate.obs_group_id   "
            + "      AND e.encounter_datetime <= :onOrBefore "
            + "      AND e.location_id = :location    "
            + "    group by p.patient_id    "
            + ") first_mdc ON first_mdc.patient_id = p.patient_id   "
            + "WHERE  e.encounter_type = ${6}   "
            + "  AND otype.concept_id = ${165174}   "
            + "  AND otype.value_coded IN (${dispensationTypes})   "
            + "  AND ostate.concept_id = ${165322}    "
            + "  AND ostate.value_coded IN (${states})   "
            + "  AND e.encounter_datetime = first_mdc.last_encounter    "
            + "  AND otype.obs_group_id = ostate.obs_group_id   "
            + "  AND e.location_id = :location    "
            + "  AND e.voided = 0   "
            + "  AND p.voided = 0   "
            + "  AND otype.voided = 0   "
            + "  AND ostate.voided = 0    ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }
}
