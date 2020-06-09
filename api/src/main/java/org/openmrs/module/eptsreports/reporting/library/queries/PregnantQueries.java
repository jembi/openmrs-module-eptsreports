/*
 * The contents of this file are subject to the OpenMRS Public License Version
 * 1.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://license.openmrs.org
 *
 * Software distributed under the License is distributed ON an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights AND limitations under the License.
 *
 * Copyright (C) OpenMRS, LLC. All Rights Reserved.
 */
package org.openmrs.module.eptsreports.reporting.library.queries;

/** Re usable queries that can be used for finding patients who are pregnant */
public class PregnantQueries {

  /** GRAVIDAS INSCRITAS NO SERVIÇO TARV */
  public static String getPregnantWhileOnArt(
      int pregnantConcept,
      int yesConcept,
      int weeksPregnantConcept,
      int eddConcept,
      int adultInitailEncounter,
      int adultSegEncounter,
      int fichaResumo,
      int lastMenstrualPeriod,
      int etvProgram,
      int startARVCriteriaConcept,
      int bPLusConcept) {

    return "SELECT     p.patient_id"
        + " FROM patient p"
        + " INNER JOIN person pe ON p.patient_id=pe.person_id"
        + " INNER JOIN encounter e ON p.patient_id=e.patient_id"
        + " INNER JOIN obs o ON e.encounter_id=o.encounter_id"
        + " WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND concept_id="
        + pregnantConcept
        + " AND value_coded="
        + yesConcept
        + " AND e.encounter_type in ("
        + adultInitailEncounter
        + ","
        + adultSegEncounter
        + ") AND e.encounter_datetime BETWEEN :startDate AND :endDate AND e.location_id=:location AND pe.gender='F' "
        + " UNION"
        + " SELECT     p.patient_id"
        + " FROM patient p"
        + " INNER JOIN person pe ON p.patient_id=pe.person_id"
        + " INNER JOIN encounter e ON p.patient_id=e.patient_id"
        + " INNER JOIN obs o ON e.encounter_id=o.encounter_id"
        + " WHERE p.voided=0 AND pe.voided=0 AND e.voided=0 AND o.voided=0 AND concept_id="
        + startARVCriteriaConcept
        + " AND value_coded="
        + bPLusConcept
        + " AND e.encounter_type in ("
        + adultInitailEncounter
        + ","
        + adultSegEncounter
        + ") AND e.encounter_datetime BETWEEN :startDate AND :endDate AND e.location_id=:location AND pe.gender='F' "
        + " UNION"
        + " SELECT p.patient_id"
        + " FROM patient p"
        + " INNER JOIN person pe ON p.patient_id=pe.person_id"
        + " INNER JOIN encounter e ON p.patient_id=e.patient_id"
        + " INNER JOIN obs o ON e.encounter_id=o.encounter_id"
        + " WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND concept_id="
        + weeksPregnantConcept
        + " AND"
        + " e.encounter_type in ("
        + adultInitailEncounter
        + ","
        + adultSegEncounter
        + ") AND e.encounter_datetime BETWEEN :startDate AND :endDate AND e.location_id=:location AND pe.gender='F' "
        + " UNION"
        + " SELECT p.patient_id"
        + " FROM patient p"
        + " INNER JOIN person pe ON p.patient_id=pe.person_id"
        + " INNER JOIN encounter e ON p.patient_id=e.patient_id"
        + " INNER JOIN obs o ON e.encounter_id=o.encounter_id"
        + " WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND concept_id="
        + eddConcept
        + " AND"
        + " e.encounter_type in ("
        + adultInitailEncounter
        + ","
        + adultSegEncounter
        + ") AND e.encounter_datetime BETWEEN :startDate AND :endDate AND e.location_id=:location AND pe.gender='F' "
        + " UNION"
        + " SELECT pp.patient_id FROM patient_program pp"
        + " INNER JOIN person pe ON pp.patient_id=pe.person_id"
        + " WHERE pp.program_id="
        + etvProgram
        + " AND pp.voided=0 AND pp.date_enrolled BETWEEN :startDate AND :endDate AND pp.location_id=:location AND pe.gender='F' "
        + "UNION "
        + " SELECT p.patient_id FROM patient p "
        + " INNER JOIN person pe ON p.patient_id=pe.person_id "
        + " INNER JOIN encounter e ON p.patient_id=e.patient_id "
        + " INNER JOIN obs o ON e.encounter_id=o.encounter_id "
        + "  WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND concept_id= "
        + lastMenstrualPeriod
        + " AND e.encounter_type IN ( "
        + fichaResumo
        + ") "
        + " AND o.value_datetime BETWEEN :startDate AND :endDate ";
  }

  /** LACTANTES INSCRITAS NO SERVIÇO TARV */
  public static String getBreastfeedingWhileOnArt(
      int breastFeedingConcept,
      int yesConcept,
      int eddConcept,
      int adultInitailEncounter,
      int adultSegEncounter,
      int startARVCriteriaConcept,
      int fichaResumo,
      int etvProgram,
      int etvProgramState,
      int historicalARTStartDate,
      int pregnantConcept,
      int weeksPregnantConcept,
      int bPLusConcept,
      int lastMenstrualPeriod) {

    return "  SELECT list.patient_id FROM (  "
        + " SELECT breastfeeding.patient_id, breastfeeding.last_date, pregnant_table.pregnant_date FROM ("
        + " SELECT p.patient_id, MAX(o.value_datetime) AS last_date"
        + " FROM patient p"
        + " INNER JOIN person pe ON p.patient_id=pe.person_id"
        + " INNER JOIN encounter e ON p.patient_id=e.patient_id"
        + " INNER JOIN obs o ON e.encounter_id=o.encounter_id"
        + " WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND concept_id="
        + eddConcept
        + " AND"
        + " e.encounter_type in ("
        + adultInitailEncounter
        + ","
        + adultSegEncounter
        + ") AND o.value_datetime BETWEEN :startDate AND :endDate AND e.location_id=:location AND pe.gender='F' "
        + " GROUP BY p.patient_id"
        + " UNION"
        + "SELECT     p.patient_id, MAX(e.encounter_datetime) AS last_date"
        + " FROM patient p"
        + " INNER JOIN person pe ON p.patient_id=pe.person_id"
        + " INNER JOIN encounter e ON p.patient_id=e.patient_id"
        + " INNER JOIN obs o ON e.encounter_id=o.encounter_id"
        + " WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND o.concept_id="
        + breastFeedingConcept
        + " AND o.value_coded="
        + yesConcept
        + " AND e.encounter_type in ("
        + adultInitailEncounter
        + ","
        + adultSegEncounter
        + ") AND e.encounter_datetime BETWEEN :startDate AND :endDate AND e.location_id=:location AND pe.gender='F' "
        + " GROUP BY p.patient_id"
        + " UNION"
        + " SELECT     p.patient_id, MAX(e.encounter_datetime) AS last_date"
        + " FROM patient p"
        + " INNER JOIN person pe ON p.patient_id=pe.person_id"
        + " INNER JOIN encounter e ON p.patient_id=e.patient_id"
        + " INNER JOIN obs o ON e.encounter_id=o.encounter_id"
        + " WHERE p.voided=0 AND pe.voided=0 AND e.voided=0 AND o.voided=0 AND o.concept_id="
        + startARVCriteriaConcept
        + " AND o.value_coded="
        + breastFeedingConcept
        + " AND e.encounter_type in ("
        + adultInitailEncounter
        + ","
        + adultSegEncounter
        + ") AND e.encounter_datetime BETWEEN :startDate AND :endDate AND e.location_id=:location AND pe.gender='F' "
        + " GROUP BY p.patient_id"
        + " UNION"
        + " SELECT pp.patient_id, pp.date_enrolled AS last_date "
        + " FROM patient_program pp"
        + " INNER JOIN person pe ON pp.patient_id=pe.person_id"
        + " INNER JOIN patient_state ps ON pp.patient_program_id=ps.patient_program_id"
        + " WHERE pp.program_id="
        + etvProgram
        + " AND ps.state="
        + etvProgramState
        + " AND pp.voided=0 AND pp.date_enrolled AND pp.date_enrolled BETWEEN :startDate AND :endDate AND pp.location_id=:location AND pe.gender='F' "
        + "UNION "
        + " SELECT p.patient_id, hist.value_datetime AS last_date"
        + " FROM patient p "
        + " INNER JOIN person pe ON p.patient_id=pe.person_id "
        + " INNER JOIN encounter e ON p.patient_id=e.patient_id "
        + " INNER JOIN obs o ON e.encounter_id=o.encounter_id "
        + " INNER JOIN obs hist ON e.encounter_id=hist.encounter_id "
        + "  WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND o.concept_id= "
        + breastFeedingConcept
        + " AND o.value_coded="
        + yesConcept
        + " AND e.encounter_type ="
        + fichaResumo
        + " AND hist.concept_id="
        + historicalARTStartDate
        + " AND hist.value_datetime BETWEEN :startDate AND :endDate "
        + " GROUP BY p.patient_id"
        + " ) AS breastfeeding "
        + " LEFT JOIN "
        + " (SELECT patient_id, MAX(pregnancy_date) AS pregnancy_date FROM "
        + " (SELECT p.patient_id , MAX(e.encounter_datetime) AS pregnancy_date "
        + " FROM patient p "
        + " INNER JOIN person pe ON p.patient_id=pe.person_id "
        + " INNER JOIN encounter e ON p.patient_id=e.patient_id "
        + " INNER JOIN obs o ON e.encounter_id=o.encounter_id "
        + " WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND concept_id=  "
        + pregnantConcept
        + " AND value_coded=  "
        + yesConcept
        + " AND e.encounter_type in ( "
        + adultInitailEncounter
        + ","
        + adultSegEncounter
        + " ) AND e.encounter_datetime between :startDate AND :endDate AND e.location_id= :location AND pe.gender='F' GROUP BY p.patient_id "
        + " UNION "
        + " select p.patient_id, MAX(historical_date.value_datetime) as pregnancy_date FROM patient p "
        + " INNER JOIN person pe ON p.patient_id=pe.person_id "
        + " INNER JOIN encounter e ON p.patient_id=e.patient_id "
        + " INNER JOIN obs pregnancy ON e.encounter_id=pregnancy.encounter_id "
        + " INNER JOIN obs historical_date ON e.encounter_id = historical_date.encounter_id "
        + " WHERE p.voided=0 AND e.voided=0 AND pregnancy.voided=0 AND pregnancy.concept_id=  "
        + pregnantConcept
        + " AND pregnancy.value_coded=  "
        + yesConcept
        + " AND historical_date.voided=0 AND historical_date.concept_id= "
        + historicalARTStartDate
        + " AND e.encounter_type = "
        + fichaResumo
        + " AND historical_date.value_datetime between :startDate AND :endDate AND e.location_id= :location AND pe.gender='F' GROUP BY p.patient_id "
        + " UNION "
        + " Select p.patient_id,  MAX(e.encounter_datetime) as pregnancy_date "
        + " FROM patient p "
        + " INNER JOIN person pe ON p.patient_id=pe.person_id "
        + " INNER JOIN encounter e ON p.patient_id=e.patient_id "
        + " INNER JOIN obs o ON e.encounter_id=o.encounter_id "
        + " WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND concept_id=  "
        + weeksPregnantConcept
        + " AND "
        + " e.encounter_type in ( "
        + adultInitailEncounter
        + ","
        + adultSegEncounter
        + " ) AND e.encounter_datetime between :startDate AND :endDate AND e.location_id= :location  AND pe.gender='F' GROUP BY p.patient_id "
        + " UNION "
        + " Select p.patient_id,  e.encounter_datetime as pregnancy_date "
        + " FROM patient p "
        + " INNER JOIN person pe ON p.patient_id=pe.person_id "
        + " INNER JOIN encounter e ON p.patient_id=e.patient_id "
        + " INNER JOIN obs o ON e.encounter_id=o.encounter_id "
        + " WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND concept_id=  "
        + eddConcept
        + " AND "
        + " e.encounter_type in ( "
        + adultInitailEncounter
        + ","
        + adultSegEncounter
        + " ) AND e.encounter_datetime between :startDate AND :endDate AND e.location_id= :location AND pe.gender='F' GROUP BY p.patient_id "
        + " UNION "
        + " Select p.patient_id, MAX(e.encounter_datetime) as pregnancy_date "
        + " FROM patient p "
        + " INNER JOIN person pe ON p.patient_id=pe.person_id "
        + " INNER JOIN encounter e ON p.patient_id=e.patient_id "
        + " INNER JOIN obs o ON e.encounter_id=o.encounter_id "
        + " WHERE p.voided=0 AND pe.voided=0 AND e.voided=0 AND o.voided=0 AND concept_id=	 "
        + startARVCriteriaConcept
        + " AND value_coded=  "
        + bPLusConcept
        + " e.encounter_type in ( "
        + adultInitailEncounter
        + ","
        + adultSegEncounter
        + " ) AND e.encounter_datetime BETWEEN :startDate AND :endDate AND e.location_id= :location AND pe.gender='F' GROUP BY p.patient_id "
        + " UNION "
        + " SELECT pp.patient_id,  MAX(pp.date_enrolled) AS pregnancy_date "
        + " FROM patient_program pp "
        + " INNER JOIN person pe ON pp.patient_id=pe.person_id "
        + " WHERE pp.program_id=  "
        + etvProgram
        + " AND pp.voided=0 AND pp.date_enrolled between  :startDate AND :endDate AND pp.location_id= :location AND pe.gender='F' GROUP BY pp.patient_id "
        + " UNION "
        + " SELECT p.patient_id,  MAX(o.value_datetime) as pregnancy_date  FROM patient p "
        + " INNER JOIN person pe ON p.patient_id=pe.person_id "
        + " INNER JOIN encounter e ON p.patient_id=e.patient_id "
        + " INNER JOIN obs o ON e.encounter_id=o.encounter_id "
        + "  WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND concept_id=  "
        + lastMenstrualPeriod
        + " AND e.encounter_type =  "
        + adultSegEncounter
        + " AND o.value_datetime BETWEEN :startDate AND :endDate GROUP BY p.patient_id) as pregnancy  "
        + " GROUP BY patient_id) AS pregnant_table "
        + " ON pregnant_table.patient_id = breastfeeding.patient_id "
        + " WHERE (breastfeeding.last_date > pregnant_table.pregnant_date "
        + " OR pregnant_table.pregnant_date IS NULL)"
        + " GROUP BY breastfeeding.patient_id) AS list ";
  }
}
