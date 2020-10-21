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

package org.openmrs.module.eptsreports.reporting.library.cohorts;

import static org.openmrs.module.eptsreports.reporting.library.queries.ResumoMensalQueries.*;
import static org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils.map;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.eptsreports.reporting.calculation.generic.StartedArtOnPeriodCalculation;
import org.openmrs.module.eptsreports.reporting.cohort.definition.CalculationCohortDefinition;
import org.openmrs.module.eptsreports.reporting.cohort.definition.EptsTransferredInCohortDefinition;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class APSSResumoTrimestralCohortQueries {

  private HivMetadata hivMetadata;
  private TbMetadata tbMetadata;
  private GenericCohortQueries genericCohortQueries;
  private ResumoMensalCohortQueries resumoMensalCohortQueries;

  @Autowired
  public APSSResumoTrimestralCohortQueries(
      HivMetadata hivMetadata,
      TbMetadata tbMetadata,
      GenericCohortQueries genericCohortQueries,
      ResumoMensalCohortQueries resumoMensalCohortQueries) {
    this.hivMetadata = hivMetadata;
    this.tbMetadata = tbMetadata;
    this.genericCohortQueries = genericCohortQueries;
    this.resumoMensalCohortQueries = resumoMensalCohortQueries;
  }

  /**
   * <b>Name: A1</b>
   *
   * <p><b>Description:</b> Nº de crianças e adolescente de 8 -14 anos que receberam revelação total
   * do diagnóstico durante o trimestre
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getA1() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName("A1");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    // This query is just a placeholder until user story for A1 is finalized
    sqlCohortDefinition.setQuery(
        getAllPatientsWithPreArtStartDateLessThanReportingStartDate(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getPreArtStartDate().getConceptId()));

    return sqlCohortDefinition;
  }

  /**
   * <b>Name: B1</b>
   *
   * <p><b>Description:</b> Nº de pacientes que iniciou cuidados HIV nesta unidade sanitária durante
   * o trimestre e que receberam aconselhamento Pré-TARV no mesmo período
   *
   * <ul>
   *   <li>Nº de pacientes que iniciou Pré-TARV (Cuidados de HIV) [ {@link
   *       ResumoMensalCohortQueries#getNumberOfPatientsWhoInitiatedPreTarvByEndOfPreviousMonthA1}
   *       from Resumo Mensal only changes the period to quarterly)]
   *   <li>And filter all patients registered in encounter “Ficha APSS&PP” (encounter_type =
   *       ${prevencaoPositivaSeguimentoEncounterType}) who have the following conditions:
   *       <ul>
   *         <li>ACONSELHAMENTO PRÉ-TARV” (concept_id = 23886) with value_coded “SIM” (concept_id =
   *             ${patientFoundYesConcept})
   *         <li>And “encounter_datetime” Between StartDate and EndDate
   *       </ul>
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getB1() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.setName("B1");
    CohortDefinition a1 =
        resumoMensalCohortQueries.getNumberOfPatientsWhoInitiatedPreTarvByEndOfPreviousMonthA1();

    cd.addSearch("A1", map(a1, "startDate=${startDate},location=${location}"));

    cd.addSearch(
        "APSSANDPP",
        map(
            getAllPatientsRegisteredInEncounterFichaAPSSANDPP(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("A1 AND APSSANDPP");

    return cd;
  }

  /**
   * <b>Name: C1</b>
   *
   * <p><b>Description:</b> Nº total de pacientes activos em TARV que receberam seguimento de adesão
   * durante o trimestre
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getC1() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName("C1");

    return sqlCohortDefinition;
  }

  /**
   * <b>Name: D1</b>
   *
   * <p><b>Description:</b> Nº de pacientes que iniciou TARV (15/+ anos) nesta unidade sanitária no
   * trimestre anterior e que receberam o pacote completo de prevenção positiva até ao período de
   * reporte
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getD1() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Nº de pacientes que iniciou TARV (15/+ anos) no trimestre anteriaor");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition initiatedArt = this.getPatientsWhoStartedArtByEndOfPreviousMonthB10();
    CohortDefinition patientAtAge15OrOlder =
        genericCohortQueries.getAgeOnArtStartDate(15, null, false);
    CohortDefinition registeredInFichaAPSSPP = this.getPatientsRegisteredInFichaAPSSPP();

    cd.addSearch(
        " initiatedART",
        EptsReportUtils.map(
            initiatedArt, "startDate=${startDate-3m},endDate=${endDate-3m},location=${location}"));

    cd.addSearch(
        "patientAtAge15OrOlder",
        EptsReportUtils.map(
            patientAtAge15OrOlder,
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "registeredInFichaAPSSPP",
        EptsReportUtils.map(
            registeredInFichaAPSSPP,
            "startDate=${startDate-3m},endDate=${endDate},location=${location}"));

    cd.setCompositionString("initiatedART AND patientAtAge15OrOlder AND registeredInFichaAPSSPP");

    return cd;
  }

  /**
   * <b>Name: E1</b>
   *
   * <p><b>Description:</b> Nº pacientes faltosos e abandonos referidos para chamadas e/ou visitas
   * de reintegração durante o trimestre
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getE1() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName("E1");

    return sqlCohortDefinition;
  }

  /**
   * <b>Name: E2</b>
   *
   * <p><b>Description:</b> Nº de pacientes faltosos e abandonos contactados e/ou encontrados
   * durante o trimestre, (dos referidos no mesmo período)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getE2() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName("E2");

    return sqlCohortDefinition;
  }

  /**
   * <b>Name: E3</b>
   *
   * <p><b>Description:</b> Nº de pacientes faltosos e abandonos que retornaram a unidade sanitária
   * durante o trimestre, (dos contactados e/ou encontrados no mesmo período)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getE3() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName("E3");

    return sqlCohortDefinition;
  }

  public SqlCohortDefinition getAllPatientsRegisteredInEncounterFichaAPSSANDPP() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("All Patients Registered In Encounter Ficha APSS AND PP");

    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put(
        "prevencaoPositivaSeguimentoEncounterType",
        hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());
    map.put("preARTCounselingConcept", hivMetadata.getPreARTCounselingConcept().getConceptId());

    String query =
        " SELECT p.patient_id "
            + "FROM patient p "
            + "     INNER JOIN encounter e "
            + "        ON e.patient_id = p.patient_id "
            + "     INNER JOIN obs o "
            + "        ON o.encounter_id=e.encounter_id "
            + "WHERE p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${prevencaoPositivaSeguimentoEncounterType} "
            + "    AND o.concept_id = ${preARTCounselingConcept} "
            + "    AND o.value_coded = ${patientFoundYesConcept} "
            + "    AND encounter_datetime "
            + "        BETWEEN :startDate AND  :endDate"
            + "    AND e.location_id = :location "
            + "    ";

    StringSubstitutor sb = new StringSubstitutor(map);
    String replacedQuery = sb.replace(query);
    cd.setQuery(replacedQuery);

    return cd;
  }

  public CohortDefinition getPatientsWhoStartedArtByEndOfPreviousMonthB10() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Number of cumulative patients who started ART by end of previous month");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "artStartDate",
        map(
            this.getStartedArtBeforeDate(false),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));

    cd.addSearch(
        "transferredIn",
        map(
            this.getTransferredInForB10(),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));

    cd.setCompositionString("artStartDate AND NOT transferredIn");

    return cd;
  }

  private CohortDefinition getStartedArtBeforeDate(boolean considerTransferredIn) {
    CalculationCohortDefinition cd =
        new CalculationCohortDefinition(
            Context.getRegisteredComponents(StartedArtOnPeriodCalculation.class).get(0));
    cd.setName("Art start date");
    cd.addCalculationParameter("considerTransferredIn", considerTransferredIn);
    cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
    cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    return cd;
  }

  private CohortDefinition getTransferredInForB10() {
    EptsTransferredInCohortDefinition cd = new EptsTransferredInCohortDefinition();
    cd.setName(
        "Number of patients transferred-in from another HF during a period less than startDate B10 ");
    cd.setProgramEnrolled(hivMetadata.getARTProgram());
    cd.setPatientState(hivMetadata.getArtTransferredFromOtherHealthFacilityWorkflowState());
    cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
    cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.setB10Flag(true);

    return cd;
  }

  private CohortDefinition getPatientsRegisteredInFichaAPSSPP() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("All Patients Registered In Encounter Ficha APSS AND PP");

    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put(
        "prevencaoPositivaSeguimentoEncounterType",
        hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());
    map.put("preARTCounselingConcept", hivMetadata.getPreARTCounselingConcept().getConceptId());
    map.put("patientFoundYesConcept", hivMetadata.getPatientFoundYesConcept().getConceptId());
    map.put("pp1Concept", hivMetadata.getPP1Concept().getConceptId());
    map.put("pp2Concept", hivMetadata.getPP2Concept().getConceptId());
    map.put("pp3Concept", hivMetadata.getPP3Concept().getConceptId());
    map.put("pp4Concept", hivMetadata.getPP4Concept().getConceptId());
    map.put("familyPlanningConcept", hivMetadata.getfamilyPlanningConcept().getConceptId());
    map.put("pp6Concept", hivMetadata.getPP6Concept().getConceptId());
    map.put("pp7Concept", hivMetadata.getPP7Concept().getConceptId());

    String query =
        ""
            + "SELECT p.patient_id "
            + "FROM patient p "
            + "     INNER JOIN encounter e "
            + "        ON e.patient_id = p.patient_id "
            + "     INNER JOIN obs o "
            + "        ON o.encounter_id=e.encounter_id "
            + "WHERE p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${prevencaoPositivaSeguimentoEncounterType} "
            + "    AND (o.concept_id = ${pp1Concept} AND o.value_coded = ${patientFoundYesConcept}) "
            + "    AND encounter_datetime "
            + "        BETWEEN :startDate AND :endDate "
            + "    AND e.location_id = :location "
            + "UNION "
            + "SELECT p.patient_id "
            + "FROM patient p "
            + "     INNER JOIN encounter e "
            + "        ON e.patient_id = p.patient_id "
            + "     INNER JOIN obs o "
            + "        ON o.encounter_id=e.encounter_id "
            + "WHERE p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${prevencaoPositivaSeguimentoEncounterType} "
            + "    AND (o.concept_id = ${pp2Concept} AND o.value_coded = ${patientFoundYesConcept}) "
            + "    AND encounter_datetime "
            + "        BETWEEN :startDate AND :endDate "
            + "    AND e.location_id = :location "
            + "UNION     "
            + "SELECT p.patient_id "
            + "FROM patient p "
            + "     INNER JOIN encounter e "
            + "        ON e.patient_id = p.patient_id "
            + "     INNER JOIN obs o "
            + "        ON o.encounter_id=e.encounter_id "
            + "WHERE p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${prevencaoPositivaSeguimentoEncounterType} "
            + "    AND (o.concept_id = ${pp3Concept} AND o.value_coded = ${patientFoundYesConcept}) "
            + "    AND encounter_datetime "
            + "        BETWEEN :startDate AND :endDate "
            + "    AND e.location_id = :location "
            + "UNION     "
            + "SELECT p.patient_id "
            + "FROM patient p "
            + "     INNER JOIN encounter e "
            + "        ON e.patient_id = p.patient_id "
            + "     INNER JOIN obs o "
            + "        ON o.encounter_id=e.encounter_id "
            + "WHERE p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${prevencaoPositivaSeguimentoEncounterType} "
            + "    AND (o.concept_id = ${pp4Concept} AND o.value_coded = ${patientFoundYesConcept}) "
            + "    AND encounter_datetime "
            + "        BETWEEN :startDate AND :endDate "
            + "    AND e.location_id = :location "
            + "UNION       "
            + "SELECT p.patient_id "
            + "FROM patient p "
            + "     INNER JOIN encounter e "
            + "        ON e.patient_id = p.patient_id "
            + "     INNER JOIN obs o "
            + "        ON o.encounter_id=e.encounter_id "
            + "WHERE p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${prevencaoPositivaSeguimentoEncounterType} "
            + "    AND (o.concept_id = ${pp6Concept} AND o.value_coded = ${patientFoundYesConcept}) "
            + "    AND encounter_datetime "
            + "        BETWEEN :startDate AND :endDate "
            + "    AND e.location_id = :location "
            + "    UNION     "
            + "SELECT p.patient_id "
            + "FROM patient p "
            + "     INNER JOIN encounter e "
            + "        ON e.patient_id = p.patient_id "
            + "     INNER JOIN obs o "
            + "        ON o.encounter_id=e.encounter_id "
            + "WHERE p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${prevencaoPositivaSeguimentoEncounterType} "
            + "    AND (o.concept_id = ${pp7Concept} AND o.value_coded = ${patientFoundYesConcept}) "
            + "    AND encounter_datetime "
            + "        BETWEEN :startDate AND :endDate "
            + "    AND e.location_id = :location "
            + "    UNION     "
            + "SELECT p.patient_id "
            + "FROM patient p "
            + "     INNER JOIN encounter e "
            + "        ON e.patient_id = p.patient_id "
            + "     INNER JOIN obs o "
            + "        ON o.encounter_id=e.encounter_id "
            + "WHERE p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${prevencaoPositivaSeguimentoEncounterType} "
            + "    AND (o.concept_id = ${familyPlanningConcept} AND o.value_coded = ${patientFoundYesConcept}) "
            + "    AND encounter_datetime "
            + "        BETWEEN :startDate AND :endDate "
            + "    AND e.location_id = :location ";

    StringSubstitutor sb = new StringSubstitutor(map);
    String replacedQuery = sb.replace(query);
    cd.setQuery(replacedQuery);

    return cd;
  }
}
