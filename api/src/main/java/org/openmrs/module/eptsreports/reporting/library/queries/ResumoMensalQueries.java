/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.eptsreports.reporting.library.queries;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.module.eptsreports.metadata.HivMetadata;

public class ResumoMensalQueries {

  /**
   * <b>Description:</b> All patients who have Pre-ART Start Date that is less than startDate
   *
   * @return {@link String}
   */
  public static String getAllPatientsWithPreArtStartDateLessThanReportingStartDate(
      int encounterType, int conceptId) {
    String query =
        "SELECT p.patient_id FROM patient p INNER JOIN encounter e ON p.patient_id=e.patient_id INNER JOIN obs o ON o.encounter_id=e.encounter_id WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND e.encounter_type=%d AND e.location_id=:location AND o.value_datetime IS NOT NULL AND o.concept_id=%d AND o.value_datetime <=:startDate";
    return String.format(query, encounterType, conceptId);
  }

  /**
   * <b>Description:</b> Patients who initiated Pre-ART during current Month with conditions
   *
   * @return {@link String}
   */
  public static String getPatientsWhoInitiatedPreArtDuringCurrentMonthWithConditions(
      int masterCardEncounterType,
      int preArtStartDateConceptId,
      int HIVCareProgramId,
      int ARVAdultInitialEncounterType,
      int ARVPediatriaInitialEncounterType) {
    Map<String, Integer> map = new HashMap<>();
    map.put("masterCardEncounterType", masterCardEncounterType);
    map.put("preArtStartDateConceptId", preArtStartDateConceptId);
    map.put("HIVCareProgramId", HIVCareProgramId);
    map.put("ARVAdultInitialEncounterType", ARVAdultInitialEncounterType);
    map.put("ARVPediatriaInitialEncounterType", ARVPediatriaInitialEncounterType);

    String query =
        "SELECT res.patient_id FROM "
            + "(SELECT results.patient_id, "
            + "       Min(results.enrollment_date) enrollment_date "
            + "FROM   (SELECT p.patient_id, "
            + "               o.value_datetime AS enrollment_date "
            + "        FROM   patient p "
            + "               INNER JOIN encounter e "
            + "                       ON p.patient_id = e.patient_id "
            + "               INNER JOIN obs o "
            + "                       ON o.encounter_id = e.encounter_id "
            + "        WHERE  p.voided = 0 "
            + "               AND e.voided = 0 "
            + "               AND o.voided = 0 "
            + "               AND e.encounter_type = ${masterCardEncounterType} "
            + "               AND e.location_id =:location "
            + "               AND o.value_datetime IS NOT NULL "
            + "               AND o.concept_id =${preArtStartDateConceptId} "
            + "        UNION ALL "
            + "        SELECT p.patient_id, "
            + "               date_enrolled AS enrollment_date "
            + "        FROM   patient_program pp "
            + "               JOIN patient p "
            + "                 ON pp.patient_id = p.patient_id "
            + "        WHERE  p.voided = 0 "
            + "               AND pp.voided = 0 "
            + "               AND pp.program_id =${HIVCareProgramId} "
            + "               AND pp.location_id =:location "
            + "        UNION ALL "
            + "        SELECT p.patient_id, "
            + "               enc.encounter_datetime AS enrollment_date "
            + "        FROM   encounter enc "
            + "               JOIN patient p "
            + "                 ON p.patient_id = enc.patient_id "
            + "        WHERE  p.voided = 0 "
            + "               AND enc.encounter_type IN (${ARVAdultInitialEncounterType},${ARVPediatriaInitialEncounterType}) "
            + "               AND enc.location_id =:location "
            + " 			  AND enc.voided = 0 "
            + "        ORDER  BY enrollment_date ASC) results "
            + "     GROUP  BY results.patient_id) res "
            + "     WHERE res.enrollment_date BETWEEN :startDate AND :endDate ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    return stringSubstitutor.replace(query);
  }

  /**
   * <b>Description:</b> Number of patients transferred from another health facility by end of
   * previous month
   *
   * @return {@link String}
   */
  public static String getPatientsTransferredFromAnotherHealthFacilityByEndOfPreviousMonth(
      int masterCardEncounter,
      int transferFromConcept,
      int yesConcept,
      int typeOfPantientConcept,
      int tarvConcept,
      int artProgram,
      int transferInState) {

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       JOIN encounter e ON p.patient_id = e.patient_id "
            + "       JOIN obs transf ON transf.encounter_id = e.encounter_id "
            + "       JOIN obs type ON type.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND e.encounter_type = ${masterCardEncounter} "
            + "       AND e.location_id = :location "
            + "       AND transf.voided = 0 "
            + "       AND transf.concept_id = ${transferFromConcept} "
            + "       AND transf.value_coded = ${yesConcept} "
            + "       AND type.voided = 0 "
            + "       AND type.concept_id = ${typeOfPantientConcept} "
            + "       AND type.value_coded = ${tarvConcept}"
            + "       AND transf.obs_datetime < :onOrBefore"
            + "UNION"
            + "       SELECT pg.patient_id"
            + "       FROM patient p"
            + "       INNER JOIN patient_program pg ON p.patient_id=pg.patient_id"
            + "       INNER JOIN patient_state ps ON pg.patient_program_id=ps.patient_program_id"
            + "       WHERE pg.voided=0"
            + "       AND ps.voided=0"
            + "       AND p.voided=0"
            + "       AND pg.program_id=${artProgram}"
            + "       AND ps.state=${transferInState}"
            + "       AND ps.end_date is null"
            + "       and ps.start_date < :onOrBefore";

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("masterCardEncounter", masterCardEncounter);
    valuesMap.put("transferFromConcept", transferFromConcept);
    valuesMap.put("yesConcept", yesConcept);
    valuesMap.put("typeOfPantientConcept", typeOfPantientConcept);
    valuesMap.put("tarvConcept", tarvConcept);
    valuesMap.put("artProgram", artProgram);
    valuesMap.put("transferInState", transferInState);
    StringSubstitutor sub = new StringSubstitutor(valuesMap);
    return sub.replace(query);
  }

  /**
   * <b>Relatório - Secção B – Entradas:</b>
   *
   * <p>O sistema irá produzir B.2) Nº de transferidos de outras US em TARV durante o mês incluindo
   * os utentes que estão:
   *
   * <ul>
   *   <li>inscritos como “Transferido de” no serviço TARV-TRATAMENTO (inscrição programa TARV) com
   *       “Data de Transferência” >= “Data Início do Relatório” e “<= “Data Fim do Relatório”; ou
   *   <li>registados no formulário “Ficha de Resumo” como “Transferido de outra US”, com “Data de
   *       Abertura de Ficha na US”>= “Data Início do Relatório” e “<= “Data Fim do Relatório”;
   * </ul>
   *
   * @return {@link String}
   */
  public static String getPatientsWhoAreTransferredIn(
      int mastercard,
      int transferFromOther,
      int yes,
      int dateOfMasterCardFileOpening,
      int programEnrolled,
      int transferredInState,
      boolean isExclusion) {
    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       JOIN encounter e "
            + "         ON p.patient_id = e.patient_id "
            + "       JOIN obs transf "
            + "         ON transf.encounter_id = e.encounter_id "
            + "       JOIN obs type "
            + "         ON type.encounter_id = e.encounter_id "
            + "       JOIN obs opening "
            + "         ON opening.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "        AND e.voided = 0 "
            + "        AND e.encounter_type = ${mastercard} "
            + "        AND e.location_id = :location "
            + "        AND transf.voided = 0 "
            + "        AND transf.concept_id = ${transferFromOther} "
            + "        AND transf.value_coded = ${yes} "
            + "        AND opening.voided = 0 "
            + "        AND opening.concept_id = ${dateOfMasterCardFileOpening} ";
    if (isExclusion) {
      query = query + "AND opening.value_datetime < :onOrAfter ";
    } else {
      query = query + " AND opening.value_datetime BETWEEN :onOrAfter AND :onOrBefore  ";
    }
    query =
        query
            + "        AND type.voided = 0 "
            + "UNION "
            + "SELECT p.patient_id "
            + "FROM patient p   "
            + "    JOIN patient_program pp  "
            + "        ON p.patient_id=pp.patient_id "
            + "    JOIN patient_state ps  "
            + "        ON ps.patient_program_id=pp.patient_program_id "
            + "WHERE  pp.program_id = ${programEnrolled} "
            + "       AND ps.state = ${transferredInState} "
            + "       AND pp.location_id = :location ";
    if (isExclusion) {
      query = query + "    AND ps.start_date < :onOrAfter ";
    } else {
      query = query + "    AND ps.start_date BETWEEN :onOrAfter AND :onOrBefore ";
    }
    query = query + "    AND p.voided = 0 " + "    AND pp.voided = 0 " + "    AND ps.voided = 0";

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("mastercard", mastercard);
    valuesMap.put("transferFromOther", transferFromOther);
    valuesMap.put("yes", yes);
    valuesMap.put("dateOfMasterCardFileOpening", dateOfMasterCardFileOpening);
    valuesMap.put("programEnrolled", programEnrolled);
    valuesMap.put("transferredInState", transferredInState);
    StringSubstitutor sub = new StringSubstitutor(valuesMap);
    return sub.replace(query);
  }

  /**
   * <b>Description:</b>Number of Patients screened for TB
   *
   * @return {@link String}
   */
  public static String getPatientsWithTBScreening(
      int adultoSeguimentoEncounterType,
      int tbScreening,
      int yesConcept,
      int noConcept,
      int tbTreatment) {
    String query =
        "SELECT screening.patient_id FROM (SELECT p.patient_id,e.encounter_datetime "
            + " FROM patient p "
            + "   INNER  JOIN encounter e "
            + "     ON p.patient_id=e.patient_id "
            + "   INNER  JOIN obs o "
            + "     ON e.encounter_id=o.encounter_id "
            + " WHERE p.voided = 0 "
            + "   AND e.voided = 0 "
            + "   AND o.voided = 0 "
            + "   AND e.location_id = :location "
            + "   AND e.encounter_datetime "
            + " BETWEEN :startDate AND :endDate "
            + "   AND e.encounter_type=${adultoSeguimentoEncounterType} "
            + "   AND o.concept_id=${tbScreening} "
            + "   AND o.value_coded in (${yesConcept},${noConcept})) as screening"
            + "   LEFT JOIN "
            + "(SELECT p.patient_id,e.encounter_datetime "
            + " FROM patient p "
            + "   INNER  JOIN encounter e "
            + "     ON p.patient_id=e.patient_id "
            + "   INNER  JOIN obs o "
            + "     ON e.encounter_id=o.encounter_id "
            + " WHERE p.voided = 0 "
            + "   AND e.voided = 0 "
            + "   AND o.voided = 0 "
            + "   AND e.location_id = :location "
            + "   AND e.encounter_datetime "
            + " BETWEEN :startDate AND :endDate "
            + "   AND e.encounter_type=${adultoSeguimentoEncounterType} "
            + "   AND o.concept_id=${tbTreatment}"
            + "   AND o.value_coded is not null) as treatment"
            + "   ON screening.patient_id = treatment.patient_id"
            + "   AND screening.encounter_datetime = treatment.encounter_datetime"
            + " WHERE treatment.patient_id is null"
            + " GROUP BY screening.patient_id";

    Map<String, Integer> map = new HashMap<>();
    map.put("adultoSeguimentoEncounterType", adultoSeguimentoEncounterType);
    map.put("tbScreening", tbScreening);
    map.put("yesConcept", yesConcept);
    map.put("noConcept", noConcept);
    map.put("tbTreatment", tbTreatment);

    StringSubstitutor sub = new StringSubstitutor(map);
    return sub.replace(query);
  }

  /**
   * <b>Description:</b> Number of patients with encounters within start and end date F1
   *
   * @return {@link String}
   */
  public static String getPatientsWithGivenEncounterType(int encounterType) {
    String query =
        " SELECT p.patient_id "
            + " FROM patient p "
            + "	INNER JOIN encounter e "
            + "		ON p.patient_id=e.patient_id "
            + " WHERE e.encounter_type= %d "
            + "		AND e.location_id=:location "
            + " 	AND e.encounter_datetime "
            + "			BETWEEN :startDate AND :endDate "
            + "		AND p.voided=0 "
            + "		AND e.voided=0 ";
    return String.format(query, encounterType);
  }

  /**
   * <b>Description:</b> Number of patients with viral load suppression
   *
   * @return {@link String}
   */
  public static String getPatientsHavingViralLoadSuppression(
      int viralLoadConcept, int encounterType) {
    String query =
        "SELECT p.patient_id FROM patient p JOIN encounter e ON p.patient_id=e.patient_id JOIN obs o ON e.encounter_id=o.encounter_id "
            + " WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND e.location_id=:location "
            + " AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + " AND o.value_numeric IS NOT NULL "
            + " AND o.concept_id=%d "
            + " AND e.encounter_type=%d "
            + " AND o.value_numeric < 1000";
    return String.format(query, viralLoadConcept, encounterType);
  }

  public static String getPatientsHavingVLSuppressionOnFichaResumo() {
    String query =
        "SELECT p.patient_id FROM patient p JOIN encounter e ON p.patient_id=e.patient_id JOIN obs o ON e.encounter_id=o.encounter_id "
            + " WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND e.location_id=:location "
            + " AND o.obs_datetime BETWEEN :startDate AND :endDate "
            + " AND o.value_numeric IS NOT NULL "
            + " AND o.concept_id=856 "
            + " AND e.encounter_type=53 "
            + " AND o.value_numeric < 1000";
    return query;
  }

  /**
   * <b>Description:</b> Number of Patients With Coded Obs And Answers
   *
   * @return {@link String}
   */
  public static String getPatientsWithCodedObsAndAnswers(
      int encounterType, int questionConceptId, int answerConceptId) {
    String query =
        "SELECT p.patient_id FROM patient p JOIN encounter e ON p.patient_id=e.patient_id JOIN obs o ON e.encounter_id=o.encounter_id "
            + " WHERE p.voided = 0 AND e.voided = 0 AND o.voided = 0 "
            + " AND e.location_id = :location AND e.encounter_datetime BETWEEN :startDate AND :endDate AND e.encounter_type=%d "
            + " AND o.concept_id=%d AND o.value_coded=%d";
    return String.format(query, encounterType, questionConceptId, answerConceptId);
  }

  /**
   * <b>Description:</b> Number of patients with viral load suppression
   *
   * @return {@link String}
   */
  public static String getPatientsHavingViralLoadResults(int viralLoadConcept, int encounterType) {
    String query =
        "SELECT p.patient_id FROM patient p JOIN encounter e ON p.patient_id=e.patient_id JOIN obs o ON e.encounter_id=o.encounter_id "
            + " WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND e.location_id=:location "
            + " AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + " AND o.value_numeric IS NOT NULL "
            + " AND o.concept_id=%d "
            + " AND e.encounter_type=%d ";
    return String.format(query, viralLoadConcept, encounterType);
  }

  /**
   * que tiveram um resultado de “Carga Viral qualitativo ou quantitativo” na “Ficha Resumo” com a
   * data da carga viral ocorrida entre a “Data Início Relatório” e “Data Fim de Relatório” (“Data
   * da CV” >= “Data Início Relatório” e <= “Data Fim de Relatório”).
   *
   * @return String
   */
  public static String getPatientsWithQuantitativeViralLoadResultsOnFichaResumo() {

    HivMetadata metadata = new HivMetadata();
    Map<String, Integer> map = new HashMap<>();
    map.put("53", metadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("856", metadata.getHivViralLoadConcept().getConceptId());
    String query =
        "SELECT p.patient_id FROM patient p JOIN encounter e ON p.patient_id=e.patient_id JOIN obs o ON e.encounter_id=o.encounter_id "
            + " WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND e.location_id=:location "
            + " AND o.obs_datetime BETWEEN :startDate AND :endDate "
            + " AND o.value_numeric IS NOT NULL "
            + " AND o.concept_id=${856} "
            + " AND e.encounter_type= ${53} ";
    return new StringSubstitutor(map).replace(query);
  }

  /**
   * <b>Description</b>que tiveram um resultado de “Carga Viral qualitativo ou quantitativo” na
   * “Ficha Resumo” com a data da carga viral ocorrida entre a “Data Início Relatório” e “Data Fim
   * de Relatório” (“Data da CV” >= “Data Início Relatório” e <= “Data Fim de Relatório”)
   *
   * @return {@link String}
   */
  public static String gePatientsWithCodedObs(int encounterType, int conceptId) {
    String query =
        "SELECT p.patient_id FROM patient p JOIN encounter e ON p.patient_id=e.patient_id JOIN obs o ON e.encounter_id=o.encounter_id "
            + " WHERE p.voided = 0 AND e.voided = 0 AND o.voided = 0 "
            + " AND e.location_id = :location AND e.encounter_datetime BETWEEN :startDate AND :endDate AND e.encounter_type=%d "
            + " AND o.concept_id=%d AND o.value_coded IS NOT NULL ";
    return String.format(query, encounterType, conceptId);
  }

  /**
   * <b>Description</b>que tiveram um resultado de “Carga Viral qualitativo ou quantitativo” na
   * “Ficha Resumo” com a data da carga viral ocorrida entre a “Data Início Relatório” e “Data Fim
   * de Relatório” (“Data da CV” >= “Data Início Relatório” e <= “Data Fim de Relatório”).
   *
   * @return {@link String}
   */
  public static String getPatientsWithQualitativeVLOnFichaResumo() {

    HivMetadata metadata = new HivMetadata();
    Map<String, Integer> map = new HashMap<>();
    map.put("53", metadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1305", metadata.getHivViralLoadQualitative().getConceptId());
    String query =
        "SELECT p.patient_id FROM patient p JOIN encounter e ON p.patient_id=e.patient_id JOIN obs o ON e.encounter_id=o.encounter_id "
            + " WHERE p.voided = 0 AND e.voided = 0 AND o.voided = 0 "
            + " AND e.location_id = :location AND o.obs_datetime BETWEEN :startDate AND :endDate AND e.encounter_type=${53} "
            + " AND o.concept_id=${1305} AND o.value_coded IS NOT NULL ";
    return new StringSubstitutor(map).replace(query);
  }

  /**
   * <b>Description: E1</b> exclusion criteria
   *
   * @return {@link String}
   */
  public static String getE1ExclusionCriteria(
      int encounterType, int questionConceptId, int answerConceptId) {
    String query =
        "SELECT p.patient_id FROM patient p JOIN encounter e ON p.patient_id=e.patient_id JOIN obs o ON e.encounter_id=o.encounter_id "
            + " JOIN (SELECT pat.patient_id AS patient_id, enc.encounter_datetime AS endDate FROM patient pat "
            + " JOIN encounter enc ON pat.patient_id=enc.patient_id JOIN obs ob ON enc.encounter_id=ob.encounter_id "
            + " WHERE pat.voided = 0 AND enc.voided = 0 AND ob.voided = 0 AND enc.location_id = :location "
            + " AND enc.encounter_datetime BETWEEN :startDate AND :endDate AND enc.encounter_type=%d AND "
            + " ob.concept_id=%d AND ob.value_coded=%d) ed "
            + " ON p.patient_id=ed.patient_id "
            + " WHERE p.voided = 0 AND e.voided = 0 AND o.voided = 0 "
            + " AND e.location_id = :location AND e.encounter_datetime BETWEEN "
            + " IF(MONTH(:startDate) = 12  && DAY(:startDate) = 21, :startDate, CONCAT(YEAR(:startDate)-1, '-12','-21')) "
            + " AND DATE_ADD(:startDate, INTERVAL -1 DAY) AND e.encounter_type=%d "
            + " AND o.concept_id=%d AND o.value_coded=%d";
    return String.format(
        query,
        encounterType,
        questionConceptId,
        answerConceptId,
        encounterType,
        questionConceptId,
        answerConceptId);
  }

  /**
   * <b>Description: E2</b> exclusion criteria
   *
   * @return {@link String}
   */
  public static String getE2ExclusionCriteria(
      int viralLoadConcept, int encounterType, int qualitativeConcept) {

    String query =
        "SELECT p.patient_id FROM patient p JOIN encounter e ON p.patient_id=e.patient_id JOIN obs o ON e.encounter_id=o.encounter_id "
            + "JOIN (SELECT pat.patient_id AS patient_id, enc.encounter_datetime AS endDate FROM patient pat JOIN encounter enc ON pat.patient_id=enc.patient_id JOIN obs ob "
            + " ON enc.encounter_id=ob.encounter_id "
            + " WHERE pat.voided=0 AND enc.voided=0 AND ob.voided=0 AND enc.location_id=:location AND enc.encounter_datetime "
            + " BETWEEN :startDate AND :endDate AND ob.concept_id IN(%d, %d) AND enc.encounter_type=%d) ed "
            + " ON p.patient_id=ed.patient_id "
            + " WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND e.location_id=:location "
            + " AND e.encounter_datetime BETWEEN "
            + " IF(MONTH(:startDate) = 12  && DAY(:startDate) = 21, :startDate, CONCAT(YEAR(:startDate)-1, '-12','-21')) "
            + " AND DATE_ADD(:startDate, INTERVAL -1 DAY) "
            + " AND o.concept_id IN (%d, %d)"
            + " AND e.encounter_type=%d ";
    return String.format(
        query,
        viralLoadConcept,
        qualitativeConcept,
        encounterType,
        viralLoadConcept,
        qualitativeConcept,
        encounterType);
  }

  /**
   * <b>Description: E2</b> que tiveram um registo de “Carga viral viral qualitativo ou quantitativo
   * ” na “Ficha Resumo” entre 21 de Dezembro do ano anterior ao do relatório e a “Data de CV” [para
   * garantir que apenas a primeira seja contabilizada) dentro do ano do calendário estatístico]
   *
   * @return {@link String}
   */
  public static String getPatientsWithVLOn21DecemberOnFichaResumo() {

    HivMetadata metadata = new HivMetadata();
    Map<String, Integer> map = new HashMap<>();
    map.put("53", metadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("856", metadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", metadata.getHivViralLoadQualitative().getConceptId());
    String query =
        "SELECT p.patient_id FROM patient p JOIN encounter e ON p.patient_id=e.patient_id JOIN obs o ON e.encounter_id=o.encounter_id "
            + "JOIN (SELECT pat.patient_id AS patient_id, enc.encounter_datetime AS endDate FROM patient pat JOIN encounter enc ON pat.patient_id=enc.patient_id JOIN obs ob "
            + " ON enc.encounter_id=ob.encounter_id "
            + " WHERE pat.voided=0 AND enc.voided=0 AND ob.voided=0 AND enc.location_id=:location AND ob.obs_datetime "
            + " BETWEEN :startDate AND :endDate AND ob.concept_id IN(${1305}, ${856}) AND enc.encounter_type= ${53}) ed "
            + " ON p.patient_id=ed.patient_id "
            + " WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND e.location_id=:location "
            + " AND e.encounter_datetime BETWEEN "
            + " IF(MONTH(:startDate) = 12  && DAY(:startDate) = 21, :startDate, CONCAT(YEAR(:startDate)-1, '-12','-21')) "
            + " AND DATE_ADD(:startDate, INTERVAL -1 DAY) "
            + " AND o.concept_id IN (${1305}, ${856})"
            + " AND e.encounter_type= ${53} ";
    return new StringSubstitutor(map).replace(query);
  }

  /**
   * <b>Description: E3</b> exclusion criteria
   *
   * @return {@link String}
   */
  public static String getE3ExclusionCriteria(
      int viralLoadConcept, int adultoSeguimentoEncounterType, int qualitativeConcept) {
    String query =
        " SELECT patient_id FROM ( "
            + " SELECT pat.patient_id AS patient_id, enc.encounter_datetime AS endDate FROM patient pat "
            + " JOIN encounter enc ON pat.patient_id=enc.patient_id JOIN obs ob ON enc.encounter_id=ob.encounter_id "
            + " WHERE pat.voided=0 AND enc.voided=0 AND ob.voided=0 AND enc.location_id=:location "
            + " AND enc.encounter_datetime BETWEEN "
            + " IF(MONTH(:startDate) = 12  && DAY(:startDate) = 21, :startDate, CONCAT(YEAR(:startDate)-1, '-12','-21')) "
            + " AND DATE_ADD(:startDate, INTERVAL -1 DAY) "
            + " AND ob.value_numeric IS NOT NULL "
            + " AND ob.concept_id=${viralLoadConcept} AND enc.encounter_type=${adultoSeguimentoEncounterType} AND ob.value_numeric < 1000"
            + " UNION "
            + " SELECT pat.patient_id AS patient_id, enc.encounter_datetime AS endDate FROM patient pat "
            + " JOIN encounter enc ON pat.patient_id=enc.patient_id JOIN obs ob ON enc.encounter_id=ob.encounter_id "
            + " WHERE pat.voided = 0 AND enc.voided = 0 AND ob.voided = 0 AND enc.location_id = :location AND "
            + " enc.encounter_datetime BETWEEN "
            + " IF(MONTH(:startDate) = 12  && DAY(:startDate) = 21, :startDate, CONCAT(YEAR(:startDate)-1, '-12','-21')) "
            + " AND DATE_ADD(:startDate, INTERVAL -1 DAY) "
            + " AND enc.encounter_type=${adultoSeguimentoEncounterType} AND ob.concept_id=${qualitativeConcept} ) E3exclusion";

    Map<String, Integer> map = new HashMap<>();
    map.put("viralLoadConcept", viralLoadConcept);
    map.put("adultoSeguimentoEncounterType", adultoSeguimentoEncounterType);
    map.put("qualitativeConcept", qualitativeConcept);

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    return stringSubstitutor.replace(query);
  }

  /**
   * <b>Description: E3</b> que tiveram um registo de “Carga viral Quantitativo” <1000 cópias ou
   * qualquer registo de “Carga viral Qualitativo” na “Ficha Resumo” entre 21 de Dezembro do ano
   * anterior ao do relatório e a “Data de CV” [para garantir que apenas a primeira seja
   * contabilizada) dentro do ano do calendário estatístico
   *
   * @return {@link String}
   */
  public static String getPatientsWithVLSuppression21DecemberOnFichaResumo() {
    String query =
        " SELECT patient_id FROM ( "
            + " SELECT pat.patient_id AS patient_id, enc.encounter_datetime AS endDate FROM patient pat "
            + " JOIN encounter enc ON pat.patient_id=enc.patient_id JOIN obs ob ON enc.encounter_id=ob.encounter_id "
            + " WHERE pat.voided=0 AND enc.voided=0 AND ob.voided=0 AND enc.location_id=:location "
            + " AND ob.obs_datetime BETWEEN "
            + " IF(MONTH(:startDate) = 12  && DAY(:startDate) = 21, :startDate, CONCAT(YEAR(:startDate)-1, '-12','-21')) "
            + " AND DATE_ADD(:startDate, INTERVAL -1 DAY) "
            + " AND ob.value_numeric IS NOT NULL "
            + " AND ob.concept_id=856 AND enc.encounter_type=53 AND ob.value_numeric < 1000"
            + " UNION "
            + " SELECT pat.patient_id AS patient_id, enc.encounter_datetime AS endDate FROM patient pat "
            + " JOIN encounter enc ON pat.patient_id=enc.patient_id JOIN obs ob ON enc.encounter_id=ob.encounter_id "
            + " WHERE pat.voided = 0 AND enc.voided = 0 AND ob.voided = 0 AND enc.location_id = :location AND "
            + " ob.obs_datetime BETWEEN "
            + " IF(MONTH(:startDate) = 12  && DAY(:startDate) = 21, :startDate, CONCAT(YEAR(:startDate)-1, '-12','-21')) "
            + " AND DATE_ADD(:startDate, INTERVAL -1 DAY) "
            + " AND enc.encounter_type=53 AND ob.concept_id=1305 ) E3exclusion";

    return query;
  }

  /**
   * <b>Description: F3</b> exclusion criteria
   *
   * @param encounterType
   * @return {@link String}
   */
  public static String getF3Exclusion(int encounterType) {
    String query =
        " SELECT p.patient_id FROM patient p JOIN encounter e ON p.patient_id=e.patient_id "
            + " WHERE e.encounter_type=%d AND e.location_id=:location "
            + " AND e.encounter_datetime >= "
            + " IF(MONTH(:startDate) = 12  && DAY(:startDate) = 21, :startDate, CONCAT(YEAR(:startDate)-1, '-12','-21')) "
            + " AND e.encounter_datetime < :startDate "
            + " AND p.voided=0 AND e.voided=0 ";
    return String.format(query, encounterType);
  }

  /**
   * <b>Description:</b> Patients with first clinical consultation 6 on the same Pre-ART Start date
   *
   * @return {@link String}
   */
  public static String getPatientsWithFirstClinicalConsultationOnTheSameDateAsPreArtStartDate(
      int mastercardEncounterType, int consultationEncounterType, int preArtStarConceptId) {
    String query =
        "SELECT l.patient_id FROM"
            + " (SELECT p.patient_id AS patient_id, MIN(e.encounter_datetime) AS encounter_datetime FROM patient p INNER JOIN encounter e ON p.patient_id=e.patient_id"
            + " INNER JOIN(SELECT p.patient_id AS patient_id,o.value_datetime AS art_start_date FROM patient p INNER JOIN encounter e ON p.patient_id=e.patient_id"
            + " INNER JOIN obs o ON o.encounter_id=e.encounter_id WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND e.encounter_type=%d AND e.location_id=:location "
            + " AND o.value_datetime IS NOT NULL AND o.concept_id=%d GROUP BY p.patient_id) pre_art ON p.patient_id=pre_art.patient_id WHERE p.voided=0 "
            + " AND e.voided=0 AND e.encounter_type=%d AND e.location_id=:location AND encounter_datetime <= :endDate  AND encounter_datetime=pre_art.art_start_date GROUP BY patient_id) l";
    return String.format(
        query, mastercardEncounterType, preArtStarConceptId, consultationEncounterType);
  }

  /**
   * <b>Abandonos até o fim do mês anterior</b>
   *
   * <p>O sistema irá identificar os utentes que abandonaram o TARV até o fim do mês anterior da
   * seguinte forma:
   *
   * <ul>
   *   <li><b>incluindo os utentes:</b>
   *       <ul>
   *         <li>com a data mais recente entre
   *             <ul>
   *               <li>a Data do Último Levantamento registada, até o fim do mês anterior (“Data Fim
   *                   do Relatório” menos 1 mês), na “Ficha Recepção/Levantou ARVs?” com “Levantou
   *                   ARV” = “S”, adicionando 30 dias, e
   *               <li>a Data do Último Agendamento de Levantamento registado no FILA até o fim do
   *                   mês anterior (“Data Fim do Relatório” menos 1 mês);
   *             </ul>
   *         <li>Esta data adicionando 59 dias deve ser menor que a “Data Fim do Relatório” menos 1
   *             mês;
   *       </ul>
   * </ul>
   *
   * @return {@link String}
   */
  public static String getNumberOfPatientsWhoAbandonedArtBySpecifiedDateB7(
      int returnVisitDateForArvDrugConcept,
      int arvPharmaciaEncounterType,
      int artDatePickup,
      int artPickupConcept,
      int yesConceptUuid,
      int masterCardDrugPickupEncounterType) {

    Map<String, Integer> map = new HashMap<>();
    map.put("returnVisitDateForArvDrugConcept", returnVisitDateForArvDrugConcept);
    map.put("arvPharmaciaEncounterType", arvPharmaciaEncounterType);
    map.put("artDatePickup", artDatePickup);
    map.put("artPickupConcept", artPickupConcept);
    map.put("yesConceptUuid", yesConceptUuid);
    map.put("masterCardDrugPickupEncounterType", masterCardDrugPickupEncounterType);

    StringBuilder query = new StringBuilder();
    query.append("SELECT final.patient_id   ");
    query.append("             from(   ");
    query.append("                 SELECT   ");
    query.append(
        "                     most_recent.patient_id, Date_add(Max(most_recent.value_datetime), interval 59 day) final_encounter_date   ");
    query.append("                 FROM   (SELECT fila.patient_id, o.value_datetime from (  ");
    query.append("                             SELECT enc.patient_id,   ");
    query.append(
        "                                 Max(enc.encounter_datetime)  encounter_datetime  ");
    query.append("                             FROM   patient pa   ");
    query.append("                                 inner join encounter enc   ");
    query.append("                                     ON enc.patient_id =  pa.patient_id   ");
    query.append("                                 inner join obs obs   ");
    query.append("                                     ON obs.encounter_id = enc.encounter_id   ");
    query.append("                             WHERE  pa.voided = 0   ");
    query.append("                                 AND enc.voided = 0   ");
    query.append("                                 AND obs.voided = 0   ");
    query.append(
        "                                 AND obs.concept_id =  ${returnVisitDateForArvDrugConcept}   ");
    query.append("                                 AND obs.value_datetime IS NOT NULL   ");
    query.append(
        "                                 AND enc.encounter_type = ${arvPharmaciaEncounterType}   ");
    query.append("                                 AND enc.location_id = :location   ");

    query.append("                                 AND datediff(enc.encounter_datetime,:date) <=0");

    query.append("                             GROUP  BY pa.patient_id) fila   ");
    query.append("                         INNER JOIN encounter e on  ");
    query.append("                             e.patient_id = fila.patient_id and  ");
    query.append(
        "                             e.encounter_datetime = fila.encounter_datetime AND  ");
    query.append(
        "                             e.encounter_type = ${arvPharmaciaEncounterType} AND  ");
    query.append("                             e.location_id = :location AND  ");
    query.append("                             e.voided = 0  ");
    query.append("                         INNER JOIN obs o on  ");
    query.append("                             o.encounter_id = e.encounter_id AND  ");
    query.append(
        "                             o.concept_id = ${returnVisitDateForArvDrugConcept} AND  ");
    query.append("                             o.voided = 0  ");
    query.append("                         UNION   ");
    query.append("                         SELECT enc.patient_id,   ");
    query.append(
        "                             Date_add(Max(obs.value_datetime), interval 30 day) value_datetime  ");
    query.append("                         FROM   patient pa   ");
    query.append("                             inner join encounter enc   ");
    query.append("                                 ON enc.patient_id = pa.patient_id   ");
    query.append("                             inner join obs obs   ");
    query.append("                                 ON obs.encounter_id = enc.encounter_id   ");
    query.append("                             inner join obs obs2   ");
    query.append("                                 ON obs2.encounter_id = enc.encounter_id   ");
    query.append("                         WHERE  pa.voided = 0   ");
    query.append("                             AND enc.voided = 0   ");
    query.append("                             AND obs.voided = 0   ");
    query.append("                             AND obs2.voided = 0   ");
    query.append("                             AND obs.concept_id = ${artDatePickup}   ");
    query.append("                             AND obs.value_datetime IS NOT NULL   ");
    query.append("                             AND obs2.concept_id = ${artPickupConcept}   ");
    query.append("                             AND obs2.value_coded = ${yesConceptUuid}   ");
    query.append(
        "                             AND enc.encounter_type = ${masterCardDrugPickupEncounterType}   ");
    query.append("                             AND enc.location_id = :location   ");

    query.append("                             AND datediff(obs.value_datetime,:date) <=0");
    query.append("                        GROUP  BY pa.patient_id   ");
    query.append("                    UNION ");
    query.append("                    SELECT     pa.patient_id, ");
    query.append("                              obs1.value_datetime AS value_datetime ");
    query.append("                         FROM   patient pa   ");
    query.append("                    INNER JOIN encounter enc ");
    query.append("                    ON         enc.patient_id = pa.patient_id ");
    query.append("                    INNER JOIN obs obs1 ");
    query.append("                    ON         enc.encounter_id = obs1.encounter_id ");
    query.append("                    INNER JOIN ");
    query.append("                               ( ");
    query.append("                                          SELECT     pa.patient_id, ");
    query.append(
        "                                                     Max(enc.encounter_datetime) encounter_datetime ");
    query.append("                                          FROM       patient pa ");
    query.append("                                          INNER JOIN encounter enc ");
    query.append(
        "                                          ON         enc.patient_id = pa.patient_id ");
    query.append("                                          INNER JOIN obs obs ");
    query.append(
        "                                          ON         obs.encounter_id = enc.encounter_id ");
    query.append("                                          WHERE      pa.voided = 0 ");
    query.append("                                          AND        enc.voided = 0 ");
    query.append("                                          AND        obs.voided = 0 ");
    query.append(
        "                                          AND        enc.encounter_type = ${arvPharmaciaEncounterType} ");
    query.append(
        "                                          AND        enc.location_id = :location ");
    query.append(
        "                                          AND        datediff(enc.encounter_datetime, :date) <= 0 ");
    query.append(
        "                                          GROUP BY   pa.patient_id ) AS last_fila ");
    query.append("                    ON         last_fila.patient_id = pa.patient_id ");
    query.append("                    WHERE        pa.voided = 0 ");
    query.append("                    AND          enc.voided = 0 ");
    query.append("                    AND          obs1.voided = 0 ");
    query.append("                    AND      enc.encounter_type = ${arvPharmaciaEncounterType} ");
    query.append(
        "                    AND        enc.encounter_datetime = last_fila.encounter_datetime ");
    query.append("                    AND        NOT EXISTS ");
    query.append("                               ( ");
    query.append("                                          SELECT     enc1.encounter_id ");
    query.append("                                          FROM       encounter enc1 ");
    query.append("                                          INNER JOIN obs obs1 ");
    query.append(
        "                                          ON         obs1.encounter_id = enc1.encounter_id ");
    query.append("                                          WHERE      obs1.voided = 0 ");
    query.append("                                          AND        enc1.voided = 0 ");
    query.append(
        "                                          AND        obs1.concept_id = ${returnVisitDateForArvDrugConcept} ");
    query.append(
        "                                          AND        obs1.value_datetime IS NOT NULL ");
    query.append(
        "                                          AND        enc1.encounter_type = ${arvPharmaciaEncounterType} ");
    query.append(
        "                                          AND        enc1.location_id = :location ");
    query.append(
        "                                          AND        enc1.encounter_datetime = last_fila.encounter_datetime) ");
    query.append("                    ) most_recent   ");
    query.append("                GROUP BY most_recent.patient_id   ");
    query.append("                HAVING datediff(final_encounter_date,:date) < 0  ");

    query.append("             ) final   ");
    query.append("             GROUP BY final.patient_id ");

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    return stringSubstitutor.replace(query.toString());
  }

  /**
   * <b>Description:</b> Get all patients enrolled in PRE-ART program id 1, with date enrolled less
   * than startDate
   *
   * @return {@link String}
   */
  public static String getAllPatientsEnrolledInPreArtProgramWithDateEnrolledLessThanStartDate(
      int programId) {
    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN patient_program pp "
            + "               ON p.patient_id = pp.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND pp.voided = 0 "
            + "       AND pp.location_id = :location "
            + "       AND pp.program_id = ${programId} "
            + "       AND pp.date_enrolled <= :startDate";

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("programId", programId);
    StringSubstitutor sub = new StringSubstitutor(valuesMap);
    return sub.replace(query);
  }

  /**
   * <b>Description:</b> Number of patients registered as transferred in during the statistical year
   *
   * @return {@link String}
   */
  public static String getPatientsRegisteredAsTransferredInDuringTheStatisticalYear(
      int mastercard,
      int transferFromOther,
      int yes,
      int masteCardFileOpenDate,
      int programEnrolled,
      int transferredInState) {

    Map<String, Integer> map = new HashMap<>();
    map.put("mastercard", mastercard);
    map.put("transferFromOther", transferFromOther);
    map.put("yes", yes);
    map.put("masteCardFileOpenDate", masteCardFileOpenDate);
    map.put("programEnrolled", programEnrolled);
    map.put("transferredInState", transferredInState);

    String query =
        " SELECT p.patient_id  "
            + "        FROM   patient p  "
            + "           INNER JOIN encounter e  "
            + "             ON p.patient_id = e.patient_id  "
            + "           INNER JOIN obs transf  "
            + "             ON transf.encounter_id = e.encounter_id  "
            + "           INNER JOIN obs type  "
            + "             ON type.encounter_id = e.encounter_id  "
            + "           INNER JOIN obs mastercardFile  "
            + "             ON mastercardFile.encounter_id = e.encounter_id  "
            + "        WHERE  p.voided = 0  "
            + "           AND e.voided = 0  "
            + "           AND e.encounter_type = ${mastercard}  "
            + "           AND e.location_id = :location  "
            + "  "
            + " "
            + "           AND transf.voided = 0  "
            + "           AND transf.concept_id = ${transferFromOther}  "
            + "           AND transf.value_coded = ${yes}  "
            + "     "
            + "          AND mastercardFile.voided = 0 "
            + "           AND mastercardFile.concept_id = ${masteCardFileOpenDate}"
            + "	         AND mastercardFile.value_datetime BETWEEN  "
            + "	  		        IF(MONTH(:onOrAfter) = 12  && DAY(:onOrAfter) = 21, :onOrAfter, CONCAT(YEAR(:onOrAfter)-1, '-12','-21'))  "
            + "	  		          AND  "
            + "	  		         :onOrBefore   "
            + "    	UNION  "
            + " "
            + "   	SELECT p.patient_id  "
            + "    	FROM patient p    "
            + "        INNER JOIN patient_program pp   "
            + "            ON p.patient_id=pp.patient_id  "
            + "        INNER JOIN patient_state ps   "
            + "            ON ps.patient_program_id=pp.patient_program_id  "
            + "    	WHERE  pp.program_id = ${programEnrolled}  "
            + "        AND ps.state = ${transferredInState}  "
            + "    	  AND pp.location_id = :location"
            + "        AND ps.start_date BETWEEN "
            + "             IF(MONTH(:onOrAfter) = 12  && DAY(:onOrAfter) = 21, :onOrAfter, CONCAT(YEAR(:onOrAfter)-1, '-12','-21'))  "
            + "        AND  "
            + "            :onOrBefore  "
            + "        AND p.voided = 0  "
            + "        AND pp.voided = 0  "
            + "        AND ps.voided = 0 ";

    StringSubstitutor sb = new StringSubstitutor(map);
    return sb.replace(query);
  }

  /**
   * Inscritos no serviço TARV-CUIDADOS (inscrição programa Pré-TARV) com “Data de Inscrição
   * Pré-TARV” < “Data Início do Relatório”
   *
   * @return String
   */
  public String getProgramEnrollmentDate() {
    String sql =
        "SELECT p.patient_id, pp.date_enrolled AS enrollment_date "
            + "FROM   patient p "
            + "       INNER JOIN patient_program pp "
            + "               ON p.patient_id = pp.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND pp.voided = 0 "
            + "       AND pp.location_id = :location "
            + "       AND pp.program_id = ${1} "
            + "       AND pp.date_enrolled <= :endDate ";
    StringSubstitutor sb = new StringSubstitutor(getMetadataMap());
    return sb.replace(sql);
  }

  /**
   * Com abertura do processo clínico (formulário hospital de dia) com “Data de Registo” < “Data
   * Início do Relatório”
   *
   * @return String
   */
  public String getClinicalFileEnrollmentDate() {
    String sql =
        "SELECT p.patient_id, e.encounter_datetime AS enrollment_date "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "WHERE  e.encounter_type IN( ${5}, ${7} ) "
            + "       AND encounter_datetime <= :endDate "
            + "       AND e.location_id = :location "
            + "       AND p.voided = 0 "
            + "       AND e.voided = 0 ";
    StringSubstitutor sb = new StringSubstitutor(getMetadataMap());
    return sb.replace(sql);
  }

  /**
   * registados no formulário “Ficha Resumo” Secção “Cuidados de HIV” com “Data do Início Pré-TARV”
   * < “Data Início do Relatório”;
   *
   * @return String
   */
  public String getMastercardEnrollmentDate() {
    String sql =
        "SELECT p.patient_id, o.value_datetime AS enrollment_date "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON p.patient_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${53} "
            + "       AND e.location_id = :location "
            + "       AND o.value_datetime IS NOT NULL "
            + "       AND o.concept_id = ${23808} "
            + "       AND o.value_datetime <= :endDate ";
    StringSubstitutor sb = new StringSubstitutor(getMetadataMap());
    return sb.replace(sql);
  }

  /**
   * registados no formulário “Ficha Resumo” com “Data Início TARV” < “Data Início do Relatório” e
   * sem nenhum levantamento na farmácia (FILA e Recepção/Levantou ARV) antes (<) da “Data Início do
   * Relatório” ou
   *
   * @return- String
   */
  public String getMastercardArtStartWithoutPickupDate() {
    String sql =
        "SELECT p.patient_id, o.value_datetime AS enrollment_date "
            + "FROM   patient p "
            + "       INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "       LEFT JOIN (SELECT en.patient_id "
            + "                  FROM   encounter en "
            + "                  INNER JOIN obs o2 ON o2.encounter_id = en.encounter_id "
            + "                  INNER JOIN obs o3 ON o3.encounter_id = en.encounter_id "
            + "                  WHERE  en.encounter_type = ${52} "
            + "                         AND o2.concept_id = ${23866} AND o2.value_datetime < :endDate AND o2.voided = 0 "
            + "                         AND o3.concept_id = ${23865} AND o3.value_coded = ${1065} AND o3.voided = 0  "
            + "                         AND en.location_id = :location "
            + "                         AND en.voided = 0 "
            + "                   GROUP BY en.patient_id "
            + "                 ) arv_pickup ON arv_pickup.patient_id = p.patient_id "
            + "      LEFT JOIN (SELECT en.patient_id "
            + "                  FROM   encounter en "
            + "                  WHERE  en.encounter_type = ${18} "
            + "                         AND en.location_id = :location "
            + "                         AND en.voided = 0 "
            + "                   GROUP BY en.patient_id "
            + "                 ) fila_pickup ON fila_pickup.patient_id = p.patient_id "
            + "WHERE  e.encounter_type = ${53} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${1190} "
            + "       AND o.value_datetime <= :endDate "
            + "       AND p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND arv_pickup.patient_id IS NULL "
            + "       AND fila_pickup.patient_id IS NULL ";

    StringSubstitutor sb = new StringSubstitutor(getMetadataMap());
    return sb.replace(sql);
  }

  /**
   * inscritos no serviço TARV-TRATAMENTO (inscrição programa TARV) com “Data de Inscrição TARV” <
   * “Data Início do Relatório” e sem nenhum levantamento na farmácia (FILA e Recepção/Levantou ARV)
   * antes (<) “Data Início do Relatório” ou
   *
   * @return String
   */
  public String getEnrollmentOnTarvProgramDate() {
    String sql =
        "SELECT p.patient_id, pp.date_enrolled AS enrollment_date "
            + "FROM   patient p "
            + "       INNER JOIN patient_program pp ON p.patient_id = pp.patient_id "
            + "       LEFT JOIN (SELECT en.patient_id "
            + "                  FROM   encounter en "
            + "                         INNER JOIN obs o2 ON o2.encounter_id = en.encounter_id "
            + "                         INNER JOIN obs o3 ON o3.encounter_id = en.encounter_id "
            + "                  WHERE        en.encounter_type = ${52} "
            + "                               AND o2.concept_id = ${23866} "
            + "                               AND o2.value_datetime <= :endDate "
            + "                               AND o2.voided = 0 "
            + "                               AND o3.concept_id = ${23865} "
            + "                               AND o3.value_coded = ${1065} "
            + "                               AND o3.voided = 0  "
            + "                               AND en.location_id = :location "
            + "                               AND en.voided = 0 "
            + "                  GROUP BY en.patient_id "
            + "                 ) arv_pickup  ON arv_pickup.patient_id = p.patient_id "
            + "      LEFT JOIN (SELECT en.patient_id "
            + "                  FROM   encounter en "
            + "                  WHERE  en.encounter_type = ${18} "
            + "                         AND en.location_id = :location "
            + "                         AND en.voided = 0 "
            + "                   GROUP BY en.patient_id "
            + "                 ) fila_pickup ON fila_pickup.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND pp.voided = 0 "
            + "       AND p.voided = 0 "
            + "       AND pp.location_id = :location "
            + "       AND pp.program_id = ${2} "
            + "       AND pp.date_enrolled <= :endDate "
            + "       AND arv_pickup.patient_id IS NULL"
            + "       AND fila_pickup.patient_id IS NULL";

    StringSubstitutor sb = new StringSubstitutor(getMetadataMap());
    return sb.replace(sql);
  }

  /**
   * Registados no formulário “Ficha Resumo” com “Data Início TARV” < “Data Início do Relatório” e
   * sem informação registada nas seguintes fontes: ▪ “Data Inicio Pre-TARV” na “Ficha Resumo” e ▪
   * Sem inscrição no programa “TARV-CUIDADOS” e ▪ Sem abertura do processo clínico (formulário
   * hospital de dia)
   *
   * @return String
   */
  public String getEnrollmentOnMastercardWithoutPreTarvAndPickupDate() {
    String sql =
        "SELECT p.patient_id, o.value_datetime AS enrollment_date  "
            + "FROM   patient p "
            + "       INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "       LEFT JOIN (SELECT en.patient_id "
            + "                  FROM   encounter en "
            + "                  WHERE   en.encounter_type IN ( ${5}, ${7}  "
            + "                           AND en.encounter_datetime <= :endDate )"
            + "                           AND en.location_id = :location "
            + "                           AND en.voided = 0"
            + "                  GROUP BY en.patient_id "
            + "                ) no_file ON no_file.patient_id = p.patient_id "
            + "       LEFT JOIN (SELECT en.patient_id "
            + "                  FROM   encounter en "
            + "                         INNER JOIN obs o2 ON o2.encounter_id = en.encounter_id "
            + "                         INNER JOIN obs o3 ON o3.encounter_id = en.encounter_id "
            + "                  WHERE  en.encounter_type = ${53} "
            + "                         AND o2.concept_id = ${23808} "
            + "                         AND o2.value_datetime <= :endDate "
            + "                         AND o2.voided = 0 "
            + "                         AND en.location_id = :location "
            + "                         AND en.voided = 0"
            + "                  GROUP BY en.patient_id "
            + "                ) no_mastercard ON no_mastercard.patient_id = p.patient_id "
            + "       LEFT JOIN(SELECT p.patient_id "
            + "                 FROM   patient p "
            + "                        INNER JOIN patient_program pp ON p.patient_id = pp.patient_id "
            + "                 WHERE  p.voided = 0 "
            + "                        AND pp.voided = 0 "
            + "                        AND pp.location_id = :location "
            + "                        AND pp.program_id = ${1} "
            + "                        AND pp.date_enrolled <= :endDate ) no_program "
            + "              ON no_program.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_type = ${53} "
            + "       AND o.concept_id = ${1190} "
            + "       AND o.value_datetime <= :endDate "
            + "       AND no_mastercard.patient_id IS NULL "
            + "       AND no_file.patient_id IS NULL "
            + "       AND no_program.patient_id IS NULL";

    StringSubstitutor sb = new StringSubstitutor(getMetadataMap());
    return sb.replace(sql);
  }

  /**
   * inscritos no serviço TARV-TRATAMENTO (inscrição programa TARV) com “Data de Inscrição TARV” <
   * “Data Início do Relatório” e sem informação registada nas seguintes fontes: - “Data Inicio
   * Pre-TARV” na “Ficha Resumo” e - Inscrição no programa “TARV-CUIDADOS” e - Abertura do processo
   * clínico (formulário hospital de dia)
   *
   * @return String
   */
  public String getEnrollmentOnTarvWithoutFileAndPreTarvDate() {
    String sql =
        "SELECT p.patient_id, pp.date_enrolled AS enrollment_date  "
            + "FROM   patient p "
            + "       INNER JOIN patient_program pp ON p.patient_id = pp.patient_id "
            + "       LEFT JOIN (SELECT en.patient_id "
            + "                  FROM   encounter en "
            + "                         INNER JOIN obs o2 ON o2.encounter_id = en.encounter_id "
            + "                  WHERE  en.encounter_type IN ( ${5}, ${7} ) "
            + "                         AND en.encounter_datetime <= :endDate  "
            + "                         AND en.location_id = :location "
            + "                         AND en.voided = 0 "
            + "                  GROUP BY en.patient_id                                 "
            + "                     ) no_file ON no_file.patient_id = p.patient_id "
            + "       LEFT JOIN (SELECT en.patient_id "
            + "                  FROM   encounter en "
            + "                         INNER JOIN obs o2 ON o2.encounter_id = en.encounter_id "
            + "                  WHERE   en.encounter_type = ${53} "
            + "                          AND o2.concept_id = ${23808} "
            + "                          AND o2.value_datetime <= :endDate "
            + "                          AND o2.voided = 0  "
            + "                          AND en.location_id = :location "
            + "                          AND en.voided = 0 "
            + "            GROUP BY en.patient_id                                 "
            + "                     ) no_mastercard ON no_mastercard.patient_id = p.patient_id "
            + "       LEFT JOIN(SELECT p.patient_id "
            + "                 FROM   patient p "
            + "                        INNER JOIN patient_program pp ON p.patient_id = pp.patient_id "
            + "                 WHERE  p.voided = 0 "
            + "                        AND pp.voided = 0 "
            + "                        AND pp.location_id = :location "
            + "                        AND pp.program_id = ${1} "
            + "                        AND pp.date_enrolled <= :endDate "
            + "                 GROUP  BY p.patient_id"
            + "       ) no_program ON no_program.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND pp.voided = 0 "
            + "       AND pp.program_id = ${2} "
            + "       AND pp.date_enrolled <= :endDate "
            + "       AND pp.location_id = :location "
            + "       AND no_file.patient_id IS NULL "
            + "       AND no_mastercard.patient_id IS NULL "
            + "       AND no_program.patient_id IS NULL";

    StringSubstitutor sb = new StringSubstitutor(getMetadataMap());
    return sb.replace(sql);
  }

  private Map<String, Integer> getMetadataMap() {
    Map<String, Integer> map = new HashMap<>();

    HivMetadata hivMetadata = new HivMetadata();

    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("23808", hivMetadata.getPreArtStartDate().getConceptId());
    map.put("5", hivMetadata.getARVAdultInitialEncounterType().getEncounterTypeId());
    map.put("7", hivMetadata.getARVPediatriaInitialEncounterType().getEncounterTypeId());
    map.put("1", hivMetadata.getHIVCareProgram().getProgramId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());

    return map;
  }

  /**
   * <b>Utentes Transferidos Para</b><br>
   * Todos os utentes com a data mais recente (“Data de Transferência”) entre as seguintes fontes:
   *
   * <ul>
   *   <li>inscritos como “Transferido para” (último estado de inscrição) no programa SERVIÇO TARV
   *       TRATAMENTO com “Data de Transferência” <= “Data Fim do Relatório” menos 1 mês; ou
   *   <li>registados como último estado [“Mudança Estado Permanência TARV” (Coluna 21) = “T”
   *       (Transferido Para) na Ficha Clínica com “Data da Consulta Actual” (Coluna 1, durante a
   *       qual se fez o registo da mudança do estado de permanência TARV) <= “Data Fim do
   *       Relatório” menos 1 mês; ou
   *   <li>como “Mudança Estado Permanência TARV” = “Transferido Para” na Ficha Resumo com “Data da
   *       Transferência” <= “Data Fim do Relatório” menos 1 mês;
   * </ul>
   *
   * @return String
   */
  public static String getPatientsWithTransferredOutMarkedAsLastState() {

    Map<String, Integer> map = new HashMap<>();

    HivMetadata hivMetadata = new HivMetadata();

    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put(
        "7",
        hivMetadata
            .getTransferredOutToAnotherHealthFacilityWorkflowState()
            .getProgramWorkflowStateId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());

    String sql =
        "SELECT     latest.patient_id, "
            + "           Max(latest.last_date) AS last_date "
            + "FROM       (SELECT p.patient_id, laststate.last_date AS last_date FROM patient p "
            + "INNER JOIN patient_program pg "
            + "ON         p.patient_id = pg.patient_id "
            + "INNER JOIN patient_state ps "
            + "ON         pg.patient_program_id = ps.patient_program_id "
            + "INNER JOIN "
            + "           ( "
            + "                      SELECT     p.patient_id, "
            + "                                 max(ps.start_date) AS last_date "
            + "                      FROM       patient p "
            + "                      INNER JOIN patient_program pg "
            + "                      ON         p.patient_id = pg.patient_id "
            + "                      INNER JOIN patient_state ps "
            + "                      ON         pg.patient_program_id = ps.patient_program_id "
            + "                      WHERE      pg.voided = 0 "
            + "                      AND        ps.voided = 0 "
            + "                      AND        p.voided = 0 "
            + "                      AND        pg.program_id = ${2} "
            + "                      AND        ps.state IS NOT NULL "
            + "                      AND        ps.start_date <= :onOrBefore "
            + "                      AND        pg.location_id = :location "
            + "                      GROUP BY   p.patient_id) laststate "
            + "ON         laststate.patient_id = p.patient_id WHERE pg.voided = 0 "
            + "AND        ps.voided = 0 "
            + "AND        p.voided = 0 "
            + "AND        pg.program_id = ${2} "
            + "AND        ps.state = ${7} "
            + "AND        ps.start_date = laststate.last_date "
            + "AND        pg.location_id = :location GROUP BY p.patient_id "
            + "UNION "
            + "SELECT     p.patient_id, "
            + "           max_resumo_state.last_state_date AS last_date "
            + "FROM       patient p "
            + "INNER JOIN encounter e "
            + "ON         e.patient_id = p.patient_id "
            + "INNER JOIN obs o "
            + "ON         e.encounter_id = o.encounter_id "
            + "INNER JOIN "
            + "           ( "
            + "                      SELECT     p.patient_id, "
            + "                                 max(o.obs_datetime) AS last_state_date "
            + "                      FROM       patient p "
            + "                      INNER JOIN encounter e "
            + "                      ON         e.patient_id = p.patient_id "
            + "                      INNER JOIN obs o "
            + "                      ON         o.encounter_id = e.encounter_id "
            + "                      WHERE      p.voided = 0 "
            + "                      AND        e.voided = 0 "
            + "                      AND        o.voided = 0 "
            + "                      AND        e.encounter_type = ${53} "
            + "                      AND        o.obs_datetime <= :onOrBefore "
            + "                      AND        e.location_id = :location "
            + "                      AND        o.concept_id = ${6272} "
            + "                      GROUP BY   p.patient_id) max_resumo_state "
            + "ON         max_resumo_state.patient_id = p.patient_id "
            + "WHERE      p.voided = 0 "
            + "AND        e.voided = 0 "
            + "AND        o.voided = 0 "
            + "AND        e.encounter_type = ${53} "
            + "AND        o.concept_id = ${6272} "
            + "AND        o.value_coded = ${1706} "
            + "AND        o.obs_datetime = max_resumo_state.last_state_date "
            + "AND        e.location_id = :location "
            + "GROUP BY   p.patient_id "
            + "UNION "
            + "SELECT     p.patient_id, "
            + "           max_state_clinical.last_state_date AS last_date "
            + "FROM       patient p "
            + "INNER JOIN encounter e "
            + "ON         e.patient_id = p.patient_id "
            + "INNER JOIN obs o "
            + "ON         o.encounter_id = e.encounter_id "
            + "INNER JOIN "
            + "           ( "
            + "                      SELECT     p.patient_id, "
            + "                                 max(e.encounter_datetime) AS last_state_date "
            + "                      FROM       patient p "
            + "                      INNER JOIN encounter e "
            + "                      ON         e.patient_id = p.patient_id "
            + "                      INNER JOIN obs o "
            + "                      ON         o.encounter_id = e.encounter_id "
            + "                      WHERE      p.voided = 0 "
            + "                      AND        e.voided = 0 "
            + "                      AND        o.voided = 0 "
            + "                      AND        e.encounter_type = ${6} "
            + "                      AND        e.encounter_datetime <= :onOrBefore "
            + "                      AND        e.location_id = :location "
            + "                      AND        o.concept_id = ${6273} "
            + "                      GROUP BY   p.patient_id) max_state_clinical "
            + "ON         max_state_clinical.patient_id = p.patient_id "
            + "WHERE      p.voided = 0 "
            + "AND        e.voided = 0 "
            + "AND        o.voided = 0 "
            + "AND        e.encounter_type = ${6} "
            + "AND        o.concept_id = ${6273} "
            + "AND        o.value_coded = ${1706} "
            + "AND        e.encounter_datetime = max_state_clinical.last_state_date "
            + "AND        e.location_id = :location "
            + "GROUP BY   p.patient_id) latest "
            + "GROUP BY   latest.patient_id";

    return sql;
  }
}
