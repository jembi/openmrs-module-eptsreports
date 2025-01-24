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

import static org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils.map;

import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.reporting.cohort.definition.*;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResumoMensalCcrCohortQueries {

  private HivMetadata hivMetadata;
  private CommonMetadata commonMetadata;
  private TbMetadata tbMetadata;

  @Autowired
  public ResumoMensalCcrCohortQueries(
      HivMetadata hivMetadata, CommonMetadata commonMetadata, TbMetadata tbMetadata) {
    this.hivMetadata = hivMetadata;
    this.commonMetadata = commonMetadata;
    this.tbMetadata = tbMetadata;
  }

  String mapping = "startDate=${startDate},endDate=${endDate},location=${location}";
  String mapping2 = "startDate=${startDate-8m},endDate=${endDate-8m},location=${location}";
  String mapping3 =
      "startDate=${startDate-8m},endDate=${endDate-8m},actualEndDate=${endDate},location=${location}";
  String mapping4 = "startDate=${startDate-8m},endDate=${endDate},location=${location}";
  String mapping5 = "startDate=${startDate-17m},endDate=${endDate-17m},location=${location}";
  String mapping6 = "startDate=${startDate-17m},endDate=${endDate},location=${location}";
  String mapping7 =
      "startDate=${startDate-17m},endDate=${endDate-17m},actualEndDate=${endDate},location=${location}";

  public String get1stCcrConsulation() {
    return "SELECT "
        + "  p.patient_id, "
        + "  Min(e.encounter_datetime) AS enrollment_date "
        + "FROM "
        + "  patient p "
        + "  INNER JOIN encounter e ON p.patient_id = e.patient_id "
        + "WHERE "
        + "  p.voided = 0 "
        + "  AND e.voided = 0 "
        + "  AND e.encounter_type = ${92} "
        + "  AND e.location_id = :location "
        + "  AND e.encounter_datetime >= :startDate "
        + "  AND e.encounter_datetime <= :endDate "
        + "GROUP BY "
        + "  p.patient_id";
  }

  public String get1stCcrSeguimentoConsulation() {
    return "SELECT "
        + "  p.patient_id, "
        + "  Min(e.encounter_datetime) AS first_consultation_date "
        + "FROM "
        + "  patient p "
        + "  INNER JOIN encounter e ON p.patient_id = e.patient_id "
        + "WHERE "
        + "  p.voided = 0 "
        + "  AND e.voided = 0 "
        + "  AND e.encounter_type = ${93} "
        + "  AND e.location_id = :location "
        + "  AND e.encounter_datetime >= :startDate "
        + "  AND e.encounter_datetime <= :endDate "
        + "GROUP BY "
        + "  p.patient_id";
  }

  public String getLastCcrSeguimentoConsulation() {
    return "SELECT "
        + "  p.patient_id, "
        + "  MAX(e.encounter_datetime) AS last_consultation_date "
        + "FROM "
        + "  patient p "
        + "  INNER JOIN encounter e ON p.patient_id = e.patient_id "
        + "WHERE "
        + "  p.voided = 0 "
        + "  AND e.voided = 0 "
        + "  AND e.encounter_type = ${93} "
        + "  AND e.location_id = :location "
        + "  AND e.encounter_datetime >= :startDate "
        + "  AND e.encounter_datetime <= :actualEndDate "
        + "GROUP BY "
        + "  p.patient_id";
  }

  /**
   * CCR-FR7
   *
   * <p>Indicador 1- Total de 1as Consultas
   *
   * <p>O sistema irá produzir o indicador 1 “Total de 1as Consultas” da seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas crianças que tiveram a 1ª consulta de CCR durante o período de reporte,
   *       ou seja, a “Data de Abertura do Processo” registada na “Ficha Resumo de CCR” ocorrida
   *       durante o periodo de relatório (“Data de abertura do processo” >= “Data Início” e <=
   *       “Data Fim”)
   * </ul>
   *
   * @return @link{@link CohortDefinition}
   */
  public CohortDefinition getPatients1stConsultation() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patients 1st Consultation");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("92", hivMetadata.getCCRResumoEncounterType().getEncounterTypeId());

    String query = "SELECT ccr.patient_id " + " FROM ( " + get1stCcrConsulation() + " ) ccr ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * <b>CCR-FR8</b>
   *
   * <p><b>Indicador 2</b> - Crianças com contacto com tuberculose
   *
   * <p>O sistema irá produzir o indicador 2 “Total de crianças com contacto com tuberculose HIV”,
   * da seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta durante o período de reporte
   *       (CCR-FR7) e o “Motivo da consulta” igual a "Contacto com Tuberculose” registado na “Ficha
   *       Resumo de CCR” com a “Data de Abertura do Processo” ocorrida durante do periodo de
   *       reporte (“Data de abertura do processo”>= “Data Início” e <= “Data Fim”).
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWithVisitReason(List<Integer> reasonsConcept) {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Children With Tuberculosis Contact");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    Map<String, String> map = new HashMap<>();
    map.put("92", String.valueOf(hivMetadata.getCCRResumoEncounterType().getEncounterTypeId()));
    map.put(
        "1874",
        String.valueOf(commonMetadata.getMotivoConsultaCriancaRiscoConcept().getConceptId()));
    map.put("reasonConcept", StringUtils.join(reasonsConcept, ","));

    String query =
        "SELECT "
            + "    p.patient_id "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "    INNER JOIN ( "
            + get1stCcrConsulation()
            + ")ccr ON ccr.patient_id = p.patient_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${92} "
            + "    AND o.concept_id = ${1874} "
            + "    AND o.value_coded IN (${reasonConcept}) "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_datetime >= :startDate "
            + "    AND e.encounter_datetime <= :endDate "
            + "GROUP BY "
            + "    p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   *
   *
   * <ul>
   *   <li>Filtrando as que tiveram o registo de “Peso/Estatura(DP)” igual a "Desnutrição Aguda
   *       Moderada” na primeira “Ficha de Seguimento de CCR” registada durante o período de reporte
   *       (“Data da Consulta” >= “Data Início” e <= “Data Fim”).
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getDamChildren(Concept answerConcept) {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Crianças com desnutrição aguda moderada (DAM)");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("23756", hivMetadata.getWeightStatureConcept().getConceptId());
    map.put("answerConcept", answerConcept.getConceptId());

    String query =
        "SELECT "
            + "    p.patient_id "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "     INNER JOIN ( "
            + get1stCcrSeguimentoConsulation()
            + ")ccr ON ccr.patient_id = p.patient_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${93} "
            + "    AND o.concept_id = ${23756} "
            + "    AND o.value_coded = ${answerConcept} "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_datetime = ccr.first_consultation_date "
            + "GROUP BY "
            + "    p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * <b>CCR-FR9</b>
   *
   * <p><b>Indicador 3</b> - Crianças com desnutrição aguda moderada
   *
   * <p>O sistema irá produzir o indicador 3 “Total de crianças com desnutrição aguda moderada”, da
   * seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta durante o período de reporte
   *       (CCR-FR7) e o “Motivo da consulta” igual a "Desnutrição Aguda” registado na “Ficha Resumo
   *       de CCR” com a “Data de Abertura do Processo” ocorrida durante do periodo de reporte
   *       (“Data de abertura do processo”>= “Data Início” e <= “Data Fim”)
   *   <li>Filtrando as que tiveram o registo de “Peso/Estatura(DP)” igual a "Desnutrição Aguda
   *       Moderada” na primeira “Ficha de Seguimento de CCR” registada durante o período de reporte
   *       (“Data da Consulta” >= “Data Início” e <= “Data Fim”).
   * </ul>
   *
   * <p><b>Nota:</b> em caso de existirem mais que uma “Ficha de Seguimento de CCR” durante o
   * período será considerada a informação registada na primeira ficha.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWithModerateAcuteMalnutrition() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças com desnutrição aguda moderada");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch(
        "damReason",
        map(
            getChildrenWithVisitReason(
                Collections.singletonList(
                    hivMetadata.getChronicMalnutritionConcept().getConceptId())),
            mapping));
    cd.addSearch(
        "damConsultation", map(getDamChildren(hivMetadata.getModerateNutritionConcept()), mapping));

    cd.setCompositionString("damReason AND damConsultation");
    return cd;
  }

  /**
   * <b>CCR-FR10</b>
   *
   * <p><b>Indicador 4</b> - Crianças com desnutrição aguda grave
   *
   * <p>O sistema irá produzir o indicador 4 “Total de crianças com desnutrição aguda grave”, da
   * seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta durante o período de reporte
   *       (CCR-FR FR7) e o “Motivo da consulta” igual a "Desnutrição Aguda” registado na “Ficha
   *       Resumo de CCR” com a “Data de Abertura do Processo” ocorrida durante do periodo de
   *       reporte (“Data de abertura do processo”>= “Data Início” e <= “Data Fim”)
   *   <li>Filtrando as que tiveram o registo de “Peso/Estatura(DP)” igual a "Desnutrição Aguda
   *       Grave” na primeira “Ficha de Seguimento de CCR” registada durante o período de reporte
   *       (“Data da Consulta” >= “Data Início” e <= “Data Fim”).
   * </ul>
   *
   * <p><b>Nota:</b> em caso de existirem mais que uma “Ficha de Seguimento de CCR” durante o
   * período será considerada a informação registada na primeira ficha.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWithSevereAcuteMalnutrition() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças com desnutrição aguda grave");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch(
        "dagReason",
        map(
            getChildrenWithVisitReason(
                Collections.singletonList(
                    hivMetadata.getChronicMalnutritionConcept().getConceptId())),
            mapping));
    cd.addSearch(
        "dagConsultation",
        map(getDamChildren(hivMetadata.getSevereAcuteMalnutritionConcept()), mapping));

    cd.setCompositionString("dagReason AND dagConsultation");
    return cd;
  }

  /**
   * O registo de “Diagnóstico Tratamento” igual a "Profilaxia com Isoniazida” na primeira “Ficha de
   * Seguimento de CCR” ocorrida durante do periodo de avaliação (“Data da consulta >=“StartDate” e
   * <= “EndDate”).
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWhoStartedIsoziazida() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Crianças que iniciaram Isoniazida na CCR");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("23985", tbMetadata.getRegimeTPTConcept().getConceptId());
    map.put("656", tbMetadata.getIsoniazidConcept().getConceptId());

    String query =
        "SELECT "
            + "    p.patient_id "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "     INNER JOIN ( "
            + get1stCcrSeguimentoConsulation()
            + ")ccr ON ccr.patient_id = p.patient_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${93} "
            + "    AND o.concept_id = ${23985} "
            + "    AND o.value_coded = ${656} "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_datetime = ccr.first_consultation_date "
            + "GROUP BY "
            + "    p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * O registo de “Tratamento Nutricional ATPU” igual a "Sim” na primeira “Ficha de Seguimento de
   * CCR” ocorrida durante do periodo de avaliação (“Data da consulta >=“StartDate” e <= “EndDate”).
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWhoReceivedAtpu() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Tratamento Nutricional ATPU");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("6143", commonMetadata.getATPUSupplememtConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());

    String query =
        "SELECT "
            + "    p.patient_id "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "     INNER JOIN ( "
            + get1stCcrSeguimentoConsulation()
            + ")ccr ON ccr.patient_id = p.patient_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${93} "
            + "    AND o.concept_id = ${6143} "
            + "    AND o.value_coded = ${1065} "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_datetime = ccr.first_consultation_date "
            + "GROUP BY "
            + "    p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * O registo de “Tratamento Nutricional CSB” igual a "Sim” na primeira “Ficha de Seguimento de
   * CCR” ocorrida durante do periodo de avaliação (“Data da consulta >= “StartDate” e <=
   * “EndDate”).
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWhoReceivedCsb() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Crianças que receberam CSB/suplemento nutricional");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("2151", commonMetadata.getSojaSupplememtConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());

    String query =
        "SELECT "
            + "    p.patient_id "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "     INNER JOIN ( "
            + get1stCcrSeguimentoConsulation()
            + ")ccr ON ccr.patient_id = p.patient_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${93} "
            + "    AND o.concept_id = ${2151} "
            + "    AND o.value_coded = ${1065} "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_datetime = ccr.first_consultation_date "
            + "GROUP BY "
            + "    p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * Filtrando as que tiveram o registo de “Tratamento Nutricional CSB” igual a "Sim” na primeira
   * “Ficha de Seguimento de CCR” registada durante o período de reporte (“Data da Consulta” >=
   * “Data Início” e <= “Data Fim”).
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWhoStartedCtz() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Crianças que iniciaram CTZ");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("6121", commonMetadata.getCotrimoxazolConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());

    String query =
        "SELECT "
            + "    p.patient_id "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "     INNER JOIN ( "
            + get1stCcrSeguimentoConsulation()
            + ")ccr ON ccr.patient_id = p.patient_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${93} "
            + "    AND o.concept_id = ${6121} "
            + "    AND o.value_coded = ${1065} "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_datetime = ccr.first_consultation_date "
            + "GROUP BY "
            + "    p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * CCR-FR13
   *
   * <p><b>Indicador 7 - </b>Crianças que iniciaram Isoniazida na CCR
   *
   * <p>O sistema irá produzir o Indicador 7 “Total de crianças que iniciaram Isoniazida na CCR” da
   * seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta durante o período de reporte (CCR-
   *       FR7)
   *   <li>Filtrando as que tiveram o registo de “Diagnóstico Tratamento” igual a "Profilaxia com
   *       Isoniazida” na primeira “Ficha de Seguimento de CCR” registada durante o período de
   *       reporte (“Data da Consulta” >= “Data Início” e <= “Data Fim”).
   * </ul>
   *
   * <p><b>Mota:</b> em caso de existirem mais que uma “Ficha de Seguimento de CCR” durante o
   * período será considerada a informação registada na primeira ficha.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWhoStartedINH() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças que iniciaram Isoniazida na CCR");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("firstConsultation", map(getPatients1stConsultation(), mapping));
    cd.addSearch("startedInh", map(getChildrenWhoStartedIsoziazida(), mapping));

    cd.setCompositionString("firstConsultation AND startedInh");
    return cd;
  }

  /**
   * CCR-FR14
   *
   * <p><b>Indicador 8 - </b>Crianças que receberam ATPU
   *
   * <p>O sistema irá produzir o Indicador 8 “Total de crianças que receberam ATPU”, da seguinte
   * forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta durante o período de reporte (CCR-
   *       FR7)
   *   <li>Filtrando as que tiveram o registo de “Tratamento Nutricional ATPU” igual a "Sim” na
   *       primeira “Ficha de Seguimento de CCR” registada durante o período de reporte (“Data da
   *       Consulta” >= “Data Início” e <= “Data Fim”)..
   * </ul>
   *
   * <p><b>Mota:</b> em caso de existirem mais que uma “Ficha de Seguimento de CCR” durante o
   * período será considerada a informação registada na primeira ficha.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWhoReceivedNutritionalTreatment() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças que receberam ATPU");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("firstConsultation", map(getPatients1stConsultation(), mapping));
    cd.addSearch("receivedAtpu", map(getChildrenWhoReceivedAtpu(), mapping));

    cd.setCompositionString("firstConsultation AND receivedAtpu");
    return cd;
  }

  /**
   * CCR-FR15
   *
   * <p><b>Indicador 9 - </b>Crianças que receberam CSB/suplemento nutricional
   *
   * <p>O sistema irá produzir o Indicador 9 “Total de crianças que receberam CSB/suplemento
   * nutricional”, da seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta durante o período de reporte (CCR-
   *       FR7)
   *   <li>Filtrando as que tiveram o registo de “Tratamento Nutricional CSB” igual a "Sim” na
   *       primeira “Ficha de Seguimento de CCR” registada durante o período de reporte (“Data da
   *       Consulta” >= “Data Início” e <= “Data Fim”).
   * </ul>
   *
   * <p><b>Mota:</b> em caso de existirem mais que uma “Ficha de Seguimento de CCR” durante o
   * período será considerada a informação registada na primeira ficha.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWhoReceivedCsbOrNutritionalSuplement() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças que receberam CSB/suplemento nutricional");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("firstConsultation", map(getPatients1stConsultation(), mapping));
    cd.addSearch("receivedCsb", map(getChildrenWhoReceivedCsb(), mapping));

    cd.setCompositionString("firstConsultation AND receivedCsb");
    return cd;
  }

  /**
   * Incluindo todas as crianças que tiveram a 1ª consulta durante o período de reporte (CCR- FR7) e
   * com idade < 2 meses (CCR-FR5)
   *
   * @see #getChildrenWhoStartedCtzBellow2MonthsOfAge
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getInfantAge(boolean greaterThan, Integer age) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Infant Age");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("92", hivMetadata.getCCRResumoEncounterType().getEncounterTypeId());
    map.put("Age", age);

    String query =
        "SELECT "
            + "    pr.person_id "
            + "FROM "
            + "    person pr "
            + "        INNER JOIN ( "
            + "        SELECT "
            + "            p.patient_id, "
            + "            MIN(e.encounter_datetime) AS enrollment_date "
            + "        FROM "
            + "            patient p "
            + "                INNER JOIN encounter e "
            + "                           ON p.patient_id = e.patient_id "
            + "        WHERE "
            + "            p.voided = 0 "
            + "          AND e.voided = 0 "
            + "          AND e.encounter_type = ${92} "
            + "          AND e.location_id = :location "
            + "          AND e.encounter_datetime >= :startDate "
            + "          AND e.encounter_datetime <= :endDate "
            + "        GROUP BY "
            + "            p.patient_id "
            + "    ) ccr "
            + "                   ON pr.person_id = ccr.patient_id "
            + "WHERE "
            + "    pr.birthdate IS NOT NULL "
            + "  AND ccr.enrollment_date IS NOT NULL ";
    if (greaterThan) {
      query += "  AND TIMESTAMPDIFF(MONTH , pr.birthdate, ccr.enrollment_date) >= ${Age}";
    } else {
      query += "  AND TIMESTAMPDIFF(MONTH , pr.birthdate, ccr.enrollment_date) < ${Age}";
    }

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * CCR-FR16
   *
   * <p><b>Indicador 10 - </b>Crianças que iniciaram CTZ < 2 meses de idade
   *
   * <p>O sistema irá produzir o Indicador 10 “Total de crianças que iniciaram CTZ < 2 meses de
   * idade” da seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta durante o período de reporte (CCR-
   *       FR7) e com idade < 2 meses (CCR-FR5)
   *   <li>Filtrando as que tiveram o registo de “Profilaxia com cotrimoxazol” igual a "Sim” na
   *       primeira “Ficha de Seguimento de CCR” registada durante o período de reporte (“Data da
   *       Consulta” >= “Data Início” e <= “Data Fim”).
   * </ul>
   *
   * <p><b>Mota:</b> em caso de existirem mais que uma “Ficha de Seguimento de CCR” durante o
   * período será considerada a informação registada na primeira ficha.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWhoStartedCtzBellow2MonthsOfAge() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças que iniciaram CTZ < 2 meses de idade");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("firstConsultation", map(getPatients1stConsultation(), mapping));
    cd.addSearch("bellow2monthsOfAge", map(getInfantAge(false, 2), mapping));
    cd.addSearch("receivedCtz", map(getChildrenWhoStartedCtz(), mapping));

    cd.setCompositionString("firstConsultation AND bellow2monthsOfAge AND receivedCtz");
    return cd;
  }

  /**
   * CCR-FR16
   *
   * <p><b>Indicador 11 - </b>Crianças que iniciaram CTZ >= 2 meses de idade
   *
   * <p>O sistema irá produzir o Indicador 11 “Total de crianças que iniciaram CTZ ≥ 2 meses de
   * idade” da seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta durante o período de reporte (CCR-
   *       FR7) e com idade >= 2 meses (CCR-FR5)
   *   <li>Filtrando as que tiveram o registo de “Profilaxia com cotrimoxazol” igual a "Sim” na
   *       primeira “Ficha de Seguimento de CCR” registada durante o período de reporte (“Data da
   *       Consulta” >= “Data Início” e <= “Data Fim”).
   * </ul>
   *
   * <p><b>Mota:</b> em caso de existirem mais que uma “Ficha de Seguimento de CCR” durante o
   * período será considerada a informação registada na primeira ficha.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWhoStartedCtzAbove2MonthsOfAge() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças que iniciaram CTZ >= 2 meses de idade");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("firstConsultation", map(getPatients1stConsultation(), mapping));
    cd.addSearch("above2monthsOfAge", map(getInfantAge(true, 2), mapping));
    cd.addSearch("receivedCtz", map(getChildrenWhoStartedCtz(), mapping));

    cd.setCompositionString("firstConsultation AND above2monthsOfAge AND receivedCtz");
    return cd;
  }

  /**
   * filtrando as que tiveram o registo de “PCR (data da colheita)”, na “Ficha de Seguimento de
   * CCR”, sendo essa data “PCR (data da colheita)” durante o período de reporte (“PCR (data da
   * colheita)” >= “Data Início” e <= “Data Fim”)
   *
   * @see #getChildrenFirstPcrCollectedUnder2MonthsofAge
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenFirstPcr() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("registo de PCR (data da colheita), na Ficha de Seguimento de CCR");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("1998", commonMetadata.getPcrConcept().getConceptId());

    String query =
        "SELECT pat.patient_id FROM ( "
            + "SELECT "
            + "    p.patient_id, MIN(o.obs_datetime) AS first_pcr "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_type = ${93} "
            + "    AND o.concept_id = ${1998} "
            + "    AND o.obs_datetime BETWEEN :startDate AND :endDate "
            + "GROUP BY "
            + "    p.patient_id "
            + ") pat";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * com idade <2 meses nesta data “PCR (data da colheita” – “Data de Nascimento” < 2 meses). Nota:
   * no caso de existência de registo de mais que uma data “PCR (data da colheita)” durante o
   * período de reporte, será considerada a primeira ocorrência
   *
   * @see #getChildrenFirstPcrCollectedUnder2MonthsofAge
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getInfantAgeAtPcr(boolean greaterThan, Integer age) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Infant Age at PCR");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("1998", commonMetadata.getPcrConcept().getConceptId());
    map.put("Age", age);

    String query =
        "SELECT "
            + "    pr.person_id "
            + "FROM "
            + "    person pr "
            + "        INNER JOIN ( "
            + "        SELECT "
            + "            p.patient_id, "
            + "            MIN(o.obs_datetime) AS first_pcr "
            + "        FROM "
            + "            patient p "
            + "             INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "             INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "        WHERE "
            + "            p.voided = 0 "
            + "          AND e.voided = 0 "
            + "          AND o.voided = 0 "
            + "          AND e.location_id = :location "
            + "          AND e.encounter_type = ${93} "
            + "          AND o.concept_id = ${1998} "
            + "          AND o.obs_datetime BETWEEN :startDate AND :endDate "
            + "        GROUP BY "
            + "            p.patient_id "
            + "    ) pcr "
            + "                   ON pr.person_id = pcr.patient_id "
            + "WHERE "
            + "    pr.birthdate IS NOT NULL "
            + "  AND pcr.first_pcr IS NOT NULL ";
    if (greaterThan) {
      query += "  AND TIMESTAMPDIFF(MONTH , pr.birthdate, pcr.first_pcr) >= ${Age}";
    } else {
      query += "  AND TIMESTAMPDIFF(MONTH , pr.birthdate, pcr.first_pcr) < ${Age}";
    }

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * CCR-FR18
   *
   * <p><b>Indicador 12 - </b>1º PCR colhido < 2 meses de idade
   *
   * <p>O sistema irá produzir o Indicador 12 “Total de 1º PCR colhido < 2 meses de idade”, da
   * seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta durante o período de reporte (CCR-
   *       FR7)
   *   <li>Filtrando as que tiveram o registo de “PCR (data da colheita)”, na “Ficha de Seguimento
   *       de CCR”, sendo essa data “PCR (data da colheita)” durante o período de reporte (“PCR
   *       (data da colheita)” >= “Data Início” e <= “Data Fim”) e com idade <2 meses nesta data
   *       (“PCR (data da colheita” – “Data de Nascimento” < 2 meses). Nota: no caso de existência
   *       de registo de mais que uma data “PCR (data da colheita)” durante o período de reporte,
   *       será considerada a primeira ocorrência.
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenFirstPcrCollectedUnder2MonthsofAge() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("1º PCR colhido < 2 meses de idade");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("firstConsultation", map(getPatients1stConsultation(), mapping));
    cd.addSearch("received1stPcr", map(getChildrenFirstPcr(), mapping));
    cd.addSearch("pcrbellow2monthsOfAge", map(getInfantAgeAtPcr(false, 2), mapping));

    cd.setCompositionString("firstConsultation AND received1stPcr AND pcrbellow2monthsOfAge");
    return cd;
  }

  /**
   * CCR-FR19
   *
   * <p><b>Indicador 13 - </b>1º PCR colhido ≥ 2 meses de idade
   *
   * <p>O sistema irá produzir o Indicador 13 “Total de 1º PCR colhido ≥ 2 meses de idade”, da
   * seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta durante o período de reporte (CCR-
   *       FR7)
   *   <li>Filtrando as que tiveram o registo de “PCR (data da colheita)”, na “Ficha de Seguimento
   *       de CCR”, sendo essa data “PCR (data da colheita)” durante o período de reporte (“PCR
   *       (data da colheita)” >= “Data Início” e <= “Data Fim”) e com idade >= 2 meses nesta data
   *       (“PCR (data da colheita” – “Data de Nascimento” >= 2 meses). Nota: no caso de existência
   *       de registo de mais que uma data “PCR (data da colheita)” durante o período de reporte,
   *       será considerada a primeira ocorrência.
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenFirstPcrCollectedAbove2MonthsofAge() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("1º PCR colhido >= 2 meses de idade");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("firstConsultation", map(getPatients1stConsultation(), mapping));
    cd.addSearch("received1stPcr", map(getChildrenFirstPcr(), mapping));
    cd.addSearch("pcrabove2monthsOfAge", map(getInfantAgeAtPcr(true, 2), mapping));

    cd.setCompositionString("firstConsultation AND received1stPcr AND pcrabove2monthsOfAge");
    return cd;
  }

  public CohortDefinition getGeneralCcrQuery(Concept questionConcept, List<Concept> answerConcept) {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Ficha Seguimento CCR Query");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    List<Integer> answerIds = new ArrayList<>();

    for (Concept concept : answerConcept) {
      answerIds.add(concept.getConceptId());
    }

    Map<String, String> map = new HashMap<>();
    map.put("93", String.valueOf(hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId()));
    map.put("questionConcept", String.valueOf(questionConcept.getConceptId()));
    map.put("answerConcept", StringUtils.join(answerIds, ","));
    String query =
        "SELECT "
            + "    p.patient_id "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "     INNER JOIN ( "
            + get1stCcrSeguimentoConsulation()
            + ")ccr ON ccr.patient_id = p.patient_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${93} "
            + "    AND o.concept_id = ${questionConcept} "
            + "    AND o.value_coded IN (${answerConcept}) "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_datetime = ccr.first_consultation_date "
            + "GROUP BY "
            + "    p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * CCR-FR20
   *
   * <p><b>Indicador 14 - </b> Crianças expostas ≥9 meses testadas com Teste Rápido de HIV
   *
   * <p>O sistema irá produzir o Indicador 14 “Total de crianças expostas ≥9 meses testadas com
   * Teste Rápido de HIV”, da seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas crianças com exposição ao HIV durante o período de reporte (CCR-FR11) e
   *       com idade >= 9 meses (CCR-FR5)
   *   <li>Filtrando as que tiveram o registo de “HIV (teste rápido)” igual a "Positivo” ou
   *       "Negativo” ou “Indeterminado” na primeira “Ficha de Seguimento de CCR” registada durante
   *       o período de reporte (“Data da Consulta” >= “Data Início” e <= “Data Fim”).
   * </ul>
   *
   * Nota: em caso de existirem mais que uma “Ficha de Seguimento de CCR” durante o período será
   * considerada a informação registada na primeira ficha.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getExposedChildrenAbove9MonthsofAge() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças expostas ≥9 meses testadas com Teste Rápido de HIV");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch(
        "hivExposure",
        map(
            getChildrenWithVisitReason(
                Collections.singletonList(
                    commonMetadata.getRecenNascidoMaeHivPositivoConcept().getConceptId())),
            mapping));

    cd.addSearch("above9monthsOfAge", map(getInfantAge(true, 9), mapping));

    cd.addSearch(
        "rapidTest",
        map(
            getGeneralCcrQuery(
                hivMetadata.getHivRapidTest1QualitativeConcept(),
                Arrays.asList(
                    hivMetadata.getPositive(),
                    hivMetadata.getNegative(),
                    tbMetadata.getIndeterminate())),
            mapping));

    cd.setCompositionString("hivExposure AND above9monthsOfAge AND rapidTest");
    return cd;
  }

  /**
   * CCR-FR21
   *
   * <p><b>Indicador 15 - </b> Crianças não expostas ao HIV testadas com Teste Rápido de HIV
   *
   * <p>O sistema irá produzir o Indicador 15 “Total de crianças não expostas ao HIV testadas com
   * Teste Rápido de HIV” da seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta durante o período de reporte (CCR-
   *       FR7)
   *   <li>Filtrando as que tiveram o registo de “HIV (teste rápido)” igual a "Positivo” ou
   *       "Negativo” ou “Indeterminado” na primeira “Ficha de Seguimento de CCR” registada durante
   *       o período de reporte (“Data da Consulta” >= “Data Início” e <= “Data Fim”)
   *   <li>Excluindo todas crianças com exposição ao HIV durante o período de reporte (CCR-FR11)
   * </ul>
   *
   * Nota: em caso de existirem mais que uma “Ficha de Seguimento de CCR” durante o período será
   * considerada a informação registada na primeira ficha.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenTestedAndNotExposedToHiv() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças não expostas ao HIV testadas com Teste Rápido de HIV");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("firstConsultation", map(getPatients1stConsultation(), mapping));

    cd.addSearch(
        "rapidTest",
        map(
            getGeneralCcrQuery(
                hivMetadata.getHivRapidTest1QualitativeConcept(),
                Arrays.asList(
                    hivMetadata.getPositive(),
                    hivMetadata.getNegative(),
                    tbMetadata.getIndeterminate())),
            mapping));

    cd.addSearch(
        "hivExposure",
        map(
            getChildrenWithVisitReason(
                Collections.singletonList(
                    commonMetadata.getRecenNascidoMaeHivPositivoConcept().getConceptId())),
            mapping));

    cd.setCompositionString("(firstConsultation AND rapidTest) AND NOT hivExposure");
    return cd;
  }

  /**
   * CCR-FR22
   *
   * <p><b>Indicador 16 - </b> Crianças não expostas ao HIV, testadas com Teste Rápido que tiveram
   * resultado positivo
   *
   * <p>O sistema irá produzir o Indicador 16 “Total de crianças não expostas ao HIV, testadas com
   * Teste Rápido que tiveram resultado positivo”, da seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta durante o período de reporte (CCR-
   *       FR7)
   *   <li>Filtrando as que tiveram o registo de “HIV (teste rápido)” igual a "Positivo” na primeira
   *       “Ficha de Seguimento de CCR” registada durante o período de reporte (“Data da Consulta”
   *       >= “Data Início” e <= “Data Fim”).
   *   <li>Excluindo todas crianças com exposição ao HIV durante o período de reporte (CCR-FR11)
   * </ul>
   *
   * Nota: em caso de existirem mais que uma “Ficha de Seguimento de CCR” durante o período será
   * considerada a informação registada na primeira ficha.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenNotExposedToHivWithPositiveTestResult() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "Crianças não expostas ao HIV, testadas com Teste Rápido que tiveram resultado positivo");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("firstConsultation", map(getPatients1stConsultation(), mapping));

    cd.addSearch(
        "rapidTestPositive",
        map(
            getGeneralCcrQuery(
                hivMetadata.getHivRapidTest1QualitativeConcept(),
                Collections.singletonList(hivMetadata.getPositive())),
            mapping));

    cd.addSearch(
        "hivExposure",
        map(
            getChildrenWithVisitReason(
                Collections.singletonList(
                    commonMetadata.getRecenNascidoMaeHivPositivoConcept().getConceptId())),
            mapping));

    cd.setCompositionString("(firstConsultation AND rapidTestPositive) AND NOT hivExposure");
    return cd;
  }

  /**
   * Filtrando as crianças que tiveram registo de “Diagnóstico Tratamento” igual a "Profilaxia com
   * Isoniazida” em seis (6) consultas (“Ficha de Seguimento de CCR”) ocorridas entre “Data Iníco” –
   * 8 meses e “Data Fim”.
   *
   * @see #getChildrenWhoCompletedINH
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWhoCompletedsoziazida() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Crianças que completaram Isonizada – coorte de 9 meses");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("23985", tbMetadata.getRegimeTPTConcept().getConceptId());
    map.put("656", tbMetadata.getIsoniazidConcept().getConceptId());

    String query =
        "SELECT "
            + "    p.patient_id "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${93} "
            + "    AND o.concept_id = ${23985} "
            + "    AND o.value_coded = ${656} "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "GROUP BY "
            + "    p.patient_id "
            + "HAVING "
            + "    COUNT(e.encounter_id) >= 6";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * CCR-FR25 <b>Indicador 21-</b> Crianças que completaram Isonizada – coorte de 9 meses
   *
   * <p>O sistema irá produzir o Indicador 21 “Total de crianças que completaram Isoniazida”, da
   * seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta há 9 meses atrás que tiveram
   *       contacto com TB (CCR-FR23)
   *   <li>Filtrando as crianças que tiveram registo de “Diagnóstico Tratamento” igual a "Profilaxia
   *       com Isoniazida” em seis (6) consultas (“Ficha de Seguimento de CCR”) ocorridas entre
   *       “Data Iníco” – 8 meses e “Data Fim”.
   * </ul>
   *
   * <b>Nota:</b> as seis (6) consultas de CCR registadas na “Ficha de Seguimento de CCR” podem ser
   * consecutivas ou não consecutivas.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWhoCompletedINH() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças que completaram Isonizada – coorte de 9 meses");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("firstConsultation", map(getPatients1stConsultation(), mapping2));
    cd.addSearch("completedInh", map(getChildrenWhoCompletedsoziazida(), mapping2));

    cd.setCompositionString("firstConsultation AND completedInh");
    return cd;
  }

  public CohortDefinition getChildrenReferredToPNTC() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Crianças referidas para PNCT – coorte de 9 meses");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("actualEndDate", "Actual End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("92", hivMetadata.getCCRResumoEncounterType().getEncounterTypeId());
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("1873", hivMetadata.getTipoDeAltaConcept().getConceptId());
    map.put("165483", hivMetadata.getTransferidoParaSectorTbConcept().getConceptId());

    String query =
        "SELECT "
            + "    p.patient_id "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${92} "
            + "    AND o.concept_id = ${1873} "
            + "    AND o.value_coded = ${165483} "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "GROUP BY "
            + "    p.patient_id "
            + "UNION "
            + "SELECT "
            + "    p.patient_id "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "    INNER JOIN ( "
            + getLastCcrSeguimentoConsulation()
            + "    ) ccr ON ccr.patient_id = p.patient_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${93} "
            + "    AND o.concept_id = ${1873} "
            + "    AND o.value_coded = ${165483} "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_datetime = ccr.last_consultation_date "
            + "GROUP BY "
            + "    p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * CCR-FR26 <b>Indicador 22-</b> Crianças referidas para PNCT – coorte de 9 meses
   *
   * <p>O sistema irá produzir o Indicador 22 “Total de crianças referidas para PNCT”, da seguinte
   * forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta há 9 meses atrás que tiveram
   *       contacto com TB (CCR-FR23)
   *   <li>Filtrando as crianças que tiveram registo de “Transferido para sector de TB” na “Ficha
   *       Resumo de CCR” com a “Data de Abertura do Processo” ocorrida há 9 meses (“Data de
   *       abertura do processo”>= “Data Início” – 8 meses e <= “Data Fim” – 8 meses) ou na última
   *       “Ficha de Seguimento de CCR” registada no período compreendido entre “Data Iníco” – 8
   *       meses e “Data Fim”.
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenPnct() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças referidas para PNCT – coorte de 9 meses");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("firstConsultation", map(getPatients1stConsultation(), mapping2));
    cd.addSearch("pnct", map(getChildrenReferredToPNTC(), mapping3));

    cd.setCompositionString("firstConsultation AND pnct");
    return cd;
  }

  public CohortDefinition getChildrenWhoAbandoned() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Crianças que abandonaram – coorte de 9 meses");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("actualEndDate", "Actual End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("92", hivMetadata.getCCRResumoEncounterType().getEncounterTypeId());
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("1873", hivMetadata.getTipoDeAltaConcept().getConceptId());
    map.put("1707", hivMetadata.getAbandonedConcept().getConceptId());

    String query =
        "SELECT "
            + "    p.patient_id "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${92} "
            + "    AND o.concept_id = ${1873} "
            + "    AND o.value_coded = ${1707} "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "GROUP BY "
            + "    p.patient_id "
            + "UNION "
            + "SELECT "
            + "    p.patient_id "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "    INNER JOIN ( "
            + getLastCcrSeguimentoConsulation()
            + "    ) ccr ON ccr.patient_id = p.patient_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${93} "
            + "    AND o.concept_id = ${1873} "
            + "    AND o.value_coded = ${1707} "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_datetime = ccr.last_consultation_date "
            + "GROUP BY "
            + "    p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * CCR-FR27 <b>Indicador 23-</b> Crianças que abandonaram – coorte de 9 meses
   *
   * <p>O sistema irá produzir o Indicador 23 “Total de crianças que abandonaram”, da seguinte
   * forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta há 9 meses atrás que tiveram
   *       contacto com TB (CCR-FR23)
   *   <li>Filtrando as crianças que tiveram registo de “Abandono” na “Ficha Resumo de CCR” com a
   *       “Data de Abertura do Processo” ocorrida há 9 meses (“Data de abertura do processo”>=
   *       “Data Início” – 8 meses e <= “Data Fim” – 8 meses) ou na última “Ficha de Seguimento de
   *       CCR” registada no período compreendido entre “Data Iníco” – 8 meses e “Data Fim”.
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWhoAbandonedBeforePeriod() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças que abandonaram");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("firstConsultation", map(getPatients1stConsultation(), mapping2));
    cd.addSearch("abandoned", map(getChildrenWhoAbandoned(), mapping3));

    cd.setCompositionString("firstConsultation AND abandoned");
    return cd;
  }

  public CohortDefinition getChildrenWhoDesnutricaoAgudaModerada() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Crianças com DAM – coorte de 9");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("actualEndDate", "Actual End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("92", hivMetadata.getCCRResumoEncounterType().getEncounterTypeId());
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("1874", commonMetadata.getMotivoConsultaCriancaRiscoConcept().getConceptId());
    map.put("1844", hivMetadata.getChronicMalnutritionConcept().getConceptId());
    map.put("23756", hivMetadata.getWeightStatureConcept().getConceptId());
    map.put("165497", hivMetadata.getModerateNutritionConcept().getConceptId());

    String query =
        "SELECT "
            + "    p.patient_id "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "    INNER JOIN ( "
            + get1stCcrSeguimentoConsulation()
            + "    ) ccr ON ccr.patient_id = p.patient_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${93} "
            + "    AND o.concept_id = ${23756} "
            + "    AND o.value_coded = ${165497} "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_datetime = ccr.first_consultation_date "
            + "GROUP BY "
            + "    p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  public CohortDefinition getChildrenWithDesnutricaoAgudaOnFichaResumoCcr() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Crianças com Desnutricao Aguda – coorte de 9");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("actualEndDate", "Actual End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("92", hivMetadata.getCCRResumoEncounterType().getEncounterTypeId());
    map.put("1874", commonMetadata.getMotivoConsultaCriancaRiscoConcept().getConceptId());
    map.put("1844", hivMetadata.getChronicMalnutritionConcept().getConceptId());

    String query =
        "SELECT "
            + "    p.patient_id "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${92} "
            + "    AND o.concept_id = ${1874} "
            + "    AND o.value_coded = ${1844} "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "GROUP BY "
            + "    p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  public CohortDefinition getChildrenWhoDesnutricaoAgudaGrave() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Crianças com DAG – coorte de 9");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("actualEndDate", "Actual End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("23756", hivMetadata.getWeightStatureConcept().getConceptId());
    map.put("165496", hivMetadata.getSevereAcuteMalnutritionConcept().getConceptId());

    String query =
        "SELECT "
            + "    p.patient_id "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "    INNER JOIN ( "
            + get1stCcrSeguimentoConsulation()
            + "    ) ccr ON ccr.patient_id = p.patient_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${93} "
            + "    AND o.concept_id = ${23756} "
            + "    AND o.value_coded = ${165496} "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_datetime = ccr.first_consultation_date "
            + "GROUP BY "
            + "    p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * CCR-FR28 <b>Indicador 24-</b> Crianças com DAM – coorte de 9 meses
   *
   * <p>O sistema irá produzir o Indicador 24 “Total de crianças com DAM”, da seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta há 9 meses (CCR-FR23) e o “Motivo
   *       da consulta” igual a "Desnutrição Aguda” registado na “Ficha Resumo de CCR” com a “Data
   *       de Abertura do Processo” ocorrida há 9 meses (“Data de abertura do processo”>= “Data
   *       Início” – 8 meses e <= “Data Fim” – 8 meses).
   *   <li>Filtrando as que tiveram o registo de “Peso/Estatura(DP)” igual a "Desnutrição Aguda
   *       Moderada” na primeira “Ficha de Seguimento de CCR” registada há 9 meses atrás (“Data da
   *       Consulta” >= “Data Início” – 8 meses e <= “Data Fim” – 8 meses).
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWithDam() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças com DAM");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("firstConsultation", map(getPatients1stConsultation(), mapping2));
    cd.addSearch(
        "desnutricaoAguda", map(getChildrenWithDesnutricaoAgudaOnFichaResumoCcr(), mapping2));
    cd.addSearch("dam", map(getChildrenWhoDesnutricaoAgudaModerada(), mapping2));

    cd.setCompositionString("firstConsultation AND desnutricaoAguda AND dam");
    return cd;
  }

  /**
   * Filtrando as crianças que tiveram registo de “Transferido para Consulta de Criança Sadia” na
   * “Ficha Resumo de CCR” com a “Data de Abertura do Processo” ocorrida há 9 meses (“Data de
   * abertura do processo”>= “Data Início” – 8 meses e <= “Data Fim” – 8 meses) ou na última “Ficha
   * de Seguimento de CCR” registada no período compreendido entre “Data Início” – 8 meses e “Data
   * Fim”
   *
   * @see #getChildrenWithRestoredDamBeforePeriod
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWithRestoredDam() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Crianças com DAM recuperadas");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("actualEndDate", "Actual End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("92", hivMetadata.getCCRResumoEncounterType().getEncounterTypeId());
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("1873", hivMetadata.getTipoDeAltaConcept().getConceptId());
    map.put("165485", hivMetadata.getTransferidoParaConsultaDeCriancaSadiaConcept().getConceptId());

    String query =
        "SELECT "
            + "    p.patient_id "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${92} "
            + "    AND o.concept_id = ${1873} "
            + "    AND o.value_coded = ${165485} "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "GROUP BY "
            + "    p.patient_id "
            + "UNION "
            + "SELECT "
            + "    p.patient_id "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "    INNER JOIN ( "
            + getLastCcrSeguimentoConsulation()
            + "    ) ccr ON ccr.patient_id = p.patient_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${93} "
            + "    AND o.concept_id = ${1873} "
            + "    AND o.value_coded = ${165485} "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_datetime = ccr.last_consultation_date "
            + "GROUP BY "
            + "    p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * CCR-FR29 <b>Indicador 25-</b> Crianças com DAM recuperadas – coorte de 9 meses
   *
   * <p>O sistema irá produzir o Indicador 25 “Total de Crianças com DAM recuperadas”, da seguinte
   * forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças com DAM que tiveram a 1ª consulta há 9 meses (CCR-FR28)
   *   <li>Filtrando as crianças que tiveram registo de “Transferido para Consulta de Criança Sadia”
   *       na “Ficha Resumo de CCR” com a “Data de Abertura do Processo” ocorrida há 9 meses (“Data
   *       de abertura do processo”>= “Data Início” – 8 meses e <= “Data Fim” – 8 meses) ou na
   *       última “Ficha de Seguimento de CCR” registada no período compreendido entre “Data Início”
   *       – 8 meses e “Data Fim”.
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWithRestoredDamBeforePeriod() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças com DAM recuperadas");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("damChild", map(getChildrenWithDam(), mapping2));
    cd.addSearch("restoredDam", map(getChildrenWithRestoredDam(), mapping3));

    cd.setCompositionString("damChild AND restoredDam");
    return cd;
  }

  /**
   * CCR-FR30 <b>Indicador 26-</b> Crianças com DAM que abandonaram – coorte de 9 meses
   *
   * <p>O sistema irá produzir o Indicador 26 “Total de Crianças com DAM que abandonaram”, da
   * seguinte forma::
   *
   * <ul>
   *   <li>Incluindo todas as crianças com DAM que tiveram a 1ª consulta há 9 meses (CCR-FR28)
   *   <li>Filtrando as crianças que tiveram registo de “Abandono” na “Ficha Resumo de CCR” com a
   *       “Data de Abertura do Processo” ocorrida há 9 meses (“Data de abertura do processo”>=
   *       “Data Início” – 8 meses e <= “Data Fim” – 8 meses) ou na última “Ficha de Seguimento de
   *       CCR” registada no período compreendido entre “Data Iníco” – 8 meses e “Data Fim”.
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWhoAbandonedDam() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças com DAM que abandonaram – coorte de 9 meses");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("damChild", map(getChildrenWithDam(), mapping2));
    cd.addSearch("abandoned", map(getChildrenWhoAbandoned(), mapping3));

    cd.setCompositionString("damChild AND abandoned");
    return cd;
  }

  /**
   * CCR-FR31 <b>Indicador 27-</b> Crianças com DAG – coorte de 9 meses
   *
   * <p>O sistema irá produzir o Indicador 27 “Total de crianças com DAG” da seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta há 9 meses (CCR-FR23) e o “Motivo
   *       da consulta” igual a "Desnutrição Aguda” registado na “Ficha Resumo de CCR” com a “Data
   *       de Abertura do Processo” ocorrida há 9 meses (“Data de abertura do processo”>= “Data
   *       Início” – 8 meses e <= “Data Fim” – 8 meses).
   *   <li>Filtrando as que tiveram o registo de “Peso/Estatura(DP)” igual a "Desnutrição Aguda
   *       Grave” na primeira “Ficha de Seguimento de CCR” registada há 9 meses atrás (“Data da
   *       Consulta” >= “Data Início” – 8 meses e <= “Data Fim” – 8 meses).
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWithDag() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças com DAG – coorte de 9 meses");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("firstConsultation", map(getPatients1stConsultation(), mapping2));
    cd.addSearch(
        "desnutricaoAguda", map(getChildrenWithDesnutricaoAgudaOnFichaResumoCcr(), mapping2));
    cd.addSearch("dag", map(getChildrenWhoDesnutricaoAgudaGrave(), mapping2));

    cd.setCompositionString("firstConsultation AND desnutricaoAguda AND dag");
    return cd;
  }

  /**
   * Filtrando as que tiveram o registo de “Referido para Internamento” igual a "Sim” na ltima
   * consulta de CCR (“Ficha de Seguimento de CCR”) ocorrida no período compreendido entre “Data da
   * Consulta” >= “Data Início” – 8 meses e <= “Data Fim” )
   *
   * @see #getChildrenWithDagReferredForInternation
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenReferredForInternation() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Crianças com DAG que foram referidas para internamento – coorte de 9 meses");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("actualEndDate", "Actual End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("1595", hivMetadata.getMedicalInpatientConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());

    String query =
        "SELECT "
            + "    p.patient_id "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "    INNER JOIN ( "
            + getLastCcrSeguimentoConsulation()
            + "    ) ccr ON ccr.patient_id = p.patient_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${93} "
            + "    AND o.concept_id = ${1595} "
            + "    AND o.value_coded = ${1065} "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_datetime = ccr.last_consultation_date "
            + "GROUP BY "
            + "    p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * CCR-FR32 <b>Indicador 28-</b> Crianças com DAG que foram referidas para internamento – coorte
   * de 9 meses
   *
   * <p>O sistema irá produzir o Indicador 28 “Total de crianças com DAG que foram referidas para
   * internamento”, da seguinte forma:
   *
   * <ul>
   *   <li>incluindo todas as crianças com DAG que tiveram a 1ª consulta há 9 meses (CCR-FR31)
   *   <li>Filtrando as que tiveram o registo de “Referido para Internamento” igual a "Sim” na
   *       última consulta de CCR (“Ficha de Seguimento de CCR”) ocorrida no período compreendido
   *       entre “Data da Consulta” >= “Data Início” – 8 meses e <= “Data Fim” ).
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWithDagReferredForInternation() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças com DAG que foram referidas para internamento – coorte de 9 meses");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("childrenDag", map(getChildrenWithDag(), mapping2));
    cd.addSearch("internation", map(getChildrenReferredForInternation(), mapping3));

    cd.setCompositionString("childrenDag AND internation");
    return cd;
  }

  /**
   * CCR-FR33 <b>Indicador 29-</b> Crianças com DAG recuperadas – coorte de 9 meses
   *
   * <p>O sistema irá produzir o Indicador 29 “Total de Crianças com DAG recuperadas” da seguinte
   * forma:
   *
   * <ul>
   *   <li>incluindo todas as crianças com DAG que tiveram a 1ª consulta há 9 meses (CCR-FR31)
   *   <li>Filtrando as crianças que tiveram registo de “Transferido para Consulta de Criança Sadia”
   *       na “Ficha Resumo de CCR” com a “Data de Abertura do Processo” ocorrida há 9 meses (“Data
   *       de abertura do processo”>= “Data Início” – 8 meses e <= “Data Fim” – 8 meses) ou na
   *       última “Ficha de Seguimento de CCR” registada no período compreendido entre “Data Início”
   *       – 8 meses e “Data Fim”.
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWithDagRestored() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças com DAG recuperadas – coorte de 9 meses");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("childrenDag", map(getChildrenWithDag(), mapping2));
    cd.addSearch("restoredDam", map(getChildrenWithRestoredDam(), mapping3));

    cd.setCompositionString("childrenDag AND restoredDam");
    return cd;
  }

  /**
   * CCR-FR34 <b>Indicador 30-</b> Crianças que com DAG que abandonaram – coorte de 9 meses
   *
   * <p>O sistema irá produzir o Indicador 29 “Total de Crianças com DAG recuperadas” da seguinte
   * forma:
   *
   * <ul>
   *   <li>incluindo todas as crianças com DAG que tiveram a 1ª consulta há 9 meses (CCR-FR31)
   *   <li>Filtrando as crianças que tiveram registo de “Transferido para Consulta de Criança Sadia”
   *       na “Ficha Resumo de CCR” com a “Data de Abertura do Processo” ocorrida há 9 meses (“Data
   *       de abertura do processo”>= “Data Início” – 8 meses e <= “Data Fim” – 8 meses) ou na
   *       última “Ficha de Seguimento de CCR” registada no período compreendido entre “Data Início”
   *       – 8 meses e “Data Fim”.
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWithDagWhoAbandoned() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças que com DAG que abandonaram – coorte de 9 meses");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("childrenDag", map(getChildrenWithDag(), mapping2));
    cd.addSearch("abandoned", map(getChildrenWhoAbandoned(), mapping3));

    cd.setCompositionString("childrenDag AND abandoned");
    return cd;
  }

  /**
   * Filtrando as crianças que tiveram registo de “Óbito” na “Ficha Resumo de CCR” com a “Data de
   * Abertura do Processo” ocorrida há 9 meses (“Data de abertura do processo”>= “Data Início” – 8
   * meses e <= “Data Fim” – 8 meses) ou na última “Ficha de Seguimento de CCR” registada no período
   * compreendido entre “Data Iníco” – 8 meses e “Data Fim”.
   *
   * @see #getChildrenWithDagWhoDied
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWhoDied() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Crianças com DAG que foram óbito");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("actualEndDate", "Actual End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("92", hivMetadata.getCCRResumoEncounterType().getEncounterTypeId());
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("1873", hivMetadata.getTipoDeAltaConcept().getConceptId());
    map.put("1366", hivMetadata.getPatientHasDiedConcept().getConceptId());
    map.put("165485", hivMetadata.getTransferidoParaConsultaDeCriancaSadiaConcept().getConceptId());

    String query =
        "SELECT "
            + "    p.patient_id "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${92} "
            + "    AND o.concept_id = ${1873} "
            + "    AND o.value_coded = ${1366} "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "GROUP BY "
            + "    p.patient_id "
            + "UNION "
            + "SELECT "
            + "    p.patient_id "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "    INNER JOIN ( "
            + getLastCcrSeguimentoConsulation()
            + "    ) ccr ON ccr.patient_id = p.patient_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${93} "
            + "    AND o.concept_id = ${1873} "
            + "    AND o.value_coded = ${165485} "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_datetime = ccr.last_consultation_date "
            + "GROUP BY "
            + "    p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * CCR-FR35 <b>Indicador 31-</b> Crianças com DAG que foram óbito – coorte de 9 meses
   *
   * <p>O sistema irá produzir o Indicador 31 “Total de crianças com DAG que foram óbito”, da
   * seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças com DAG que tiveram a 1ª consulta há 9 meses (CCR-FR31)
   *   <li>Filtrando as crianças que tiveram registo de “Óbito” na “Ficha Resumo de CCR” com a “Data
   *       de Abertura do Processo” ocorrida há 9 meses (“Data de abertura do processo”>= “Data
   *       Início” – 8 meses e <= “Data Fim” – 8 meses) ou na última “Ficha de Seguimento de CCR”
   *       registada no período compreendido entre “Data Iníco” – 8 meses e “Data Fim”.
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWithDagWhoDied() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças com DAG que foram óbito – coorte de 9 meses");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("childrenDag", map(getChildrenWithDag(), mapping2));
    cd.addSearch("died", map(getChildrenWhoDied(), mapping3));

    cd.setCompositionString("childrenDag AND died");
    return cd;
  }

  /**
   * CCR-FR36 <b>Indicador 32-</b> Crianças expostas – coorte de 9 meses
   *
   * <p>O sistema irá produzir o Indicador 32 “Total de crianças expostas”, da seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta há 9 meses (CCR-FR23) e o “Motivo
   *       da consulta” igual a "Exposição ao HIV” registado na “Ficha Resumo de CCR” com a “Data de
   *       Abertura do Processo” ocorrida há 9 meses (“Data de abertura do processo”>= “Data Início”
   *       – 8 meses e <= “Data Fim” – 8 meses).
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getExposedChildren() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças expostas – coorte de 9 meses");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("firstConsultation", map(getPatients1stConsultation(), mapping2));
    cd.addSearch(
        "hivExposure",
        map(
            getChildrenWithVisitReason(
                Collections.singletonList(
                    commonMetadata.getRecenNascidoMaeHivPositivoConcept().getConceptId())),
            mapping2));

    cd.setCompositionString("firstConsultation AND hivExposure");
    return cd;
  }

  /**
   * @see #getExposedChildren5MonthsOfAge
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getExatInfantAge(Integer age) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Infant Age");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("92", hivMetadata.getCCRResumoEncounterType().getEncounterTypeId());
    map.put("age", age);

    String query =
        "SELECT "
            + "    pr.person_id "
            + "FROM "
            + "    person pr "
            + "        INNER JOIN ( "
            + "        SELECT "
            + "            p.patient_id, "
            + "            MIN(e.encounter_datetime) AS enrollment_date "
            + "        FROM "
            + "            patient p "
            + "                INNER JOIN encounter e "
            + "                           ON p.patient_id = e.patient_id "
            + "        WHERE "
            + "            p.voided = 0 "
            + "          AND e.voided = 0 "
            + "          AND e.encounter_type = ${92} "
            + "          AND e.location_id = :location "
            + "          AND e.encounter_datetime >= :startDate "
            + "          AND e.encounter_datetime <= :endDate "
            + "        GROUP BY "
            + "            p.patient_id "
            + "    ) ccr "
            + "                   ON pr.person_id = ccr.patient_id "
            + "WHERE "
            + "    pr.birthdate IS NOT NULL "
            + "  AND ccr.enrollment_date IS NOT NULL "
            + "  AND TIMESTAMPDIFF(MONTH , pr.birthdate, ccr.enrollment_date) = ${age}";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * Filtrando as crianças que tiveram registo de “PTV Mãe” igual a “TARV” registado na “Ficha
   * Resumo de CCR” com a “Data de Abertura do Processo” ocorrida há 9 meses (“Data de abertura do
   * processo”>= “Data Início” – 8 meses e <= “Data Fim” – 8 meses).
   *
   * @see #getExposedChildren5MonthsOfAge
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getGeneralResumoCcrQuery(
      Concept questionConcept, List<Concept> answerConcept) {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Ficha Seguimento CCR Query");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    List<Integer> answerIds = new ArrayList<>();

    for (Concept concept : answerConcept) {
      answerIds.add(concept.getConceptId());
    }

    Map<String, String> map = new HashMap<>();
    map.put("92", String.valueOf(hivMetadata.getCCRResumoEncounterType().getEncounterTypeId()));
    map.put("questionConcept", String.valueOf(questionConcept.getConceptId()));
    map.put("answerConcept", StringUtils.join(answerIds, ","));

    String query =
        "SELECT "
            + "    p.patient_id "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "     INNER JOIN ( "
            + get1stCcrConsulation()
            + ")ccr ON ccr.patient_id = p.patient_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_type = ${92} "
            + "    AND o.concept_id = ${questionConcept} "
            + "    AND o.value_coded IN (${answerConcept}) "
            + "    AND e.encounter_datetime >= :startDate "
            + "    AND e.encounter_datetime <= :endDate "
            + "GROUP BY "
            + "    p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * CCR-FR37 <b>Indicador 33-</b> Crianças expostas com 5 meses de idade e com mãe em TARV – coorte
   * de 9 meses
   *
   * <p>O sistema irá produzir o Indicador 32 “Total de crianças expostas”, da seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta há 9 meses atrás que foram expostas
   *       ao HIV (CCR-FR36) com idade igual a 5 meses (CCR-FR6).
   *   <li>Filtrando as crianças que tiveram registo de “PTV Mãe” igual a “TARV” registado na “Ficha
   *       Resumo de CCR” com a “Data de Abertura do Processo” ocorrida há 9 meses (“Data de
   *       abertura do processo”>= “Data Início” – 8 meses e <= “Data Fim” – 8 meses).
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getExposedChildren5MonthsOfAge() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças expostas com 5 meses de idade e com mãe em TARV – coorte de 9 meses");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("exposed", map(getExposedChildren(), mapping));
    cd.addSearch("age", map(getExatInfantAge(5), mapping2));
    cd.addSearch(
        "tarv",
        map(
            getGeneralResumoCcrQuery(
                hivMetadata.gePmctMothersRegimeConcept(),
                Collections.singletonList(hivMetadata.getArtStatus())),
            mapping2));

    cd.setCompositionString("exposed AND age AND tarv");
    return cd;
  }

  /**
   * Filtrando as crianças que tiveram registo de “Aleitamento Materno Exclusivo” igual a “Sim” numa
   * “Ficha de Seguimento de CCR” registada no período compreendido entre “Data Iníco” – 8 meses e
   * “Data Fim
   *
   * @see #getExposedChildrenWithBreastfed5MonthsOfAge
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWithAleitamentoMaterno() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Crianças expostas com aleitamento ");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("5526", commonMetadata.getBreastfedExclusivelyConcept().getConceptId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());

    String query =
        "SELECT "
            + "    p.patient_id "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_type = ${93} "
            + "    AND o.concept_id = ${5526} "
            + "    AND o.value_coded = ${1065} "
            + "    AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "GROUP BY "
            + "    p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * Crianças expostas com aleitamento materno exclusivo aos 5 meses
   *
   * @see #getExposedChildrenWithBreastfed5MonthsOfAge
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getInfantAgeOnBreastfed() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Infant Age");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("5526", commonMetadata.getBreastfedExclusivelyConcept().getConceptId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());

    String query =
        "SELECT "
            + "    pr.person_id "
            + "FROM "
            + "    person pr "
            + "        INNER JOIN ( "
            + "        SELECT "
            + "            p.patient_id, "
            + "            e.encounter_datetime AS breastfed_date "
            + "        FROM "
            + "            patient p "
            + "             INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "             INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "        WHERE "
            + "            p.voided = 0 "
            + "          AND e.voided = 0 "
            + "          AND o.voided = 0 "
            + "          AND e.location_id = :location "
            + "          AND e.encounter_type = ${93} "
            + "          AND o.concept_id = ${5526} "
            + "          AND o.value_coded = ${1065} "
            + "          AND e.encounter_datetime >= :startDate "
            + "          AND e.encounter_datetime <= :endDate "
            + "        GROUP BY "
            + "            p.patient_id, e.encounter_datetime "
            + "    ) ccr "
            + "                   ON pr.person_id = ccr.patient_id "
            + "WHERE "
            + "    pr.birthdate IS NOT NULL "
            + "  AND ccr.breastfed_date IS NOT NULL "
            + "  AND TIMESTAMPDIFF( DAY, pr.birthdate, ccr.breastfed_date ) BETWEEN 150 AND 179";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    System.out.println(sqlCohortDefinition.getQuery());

    return sqlCohortDefinition;
  }

  /**
   * CCR-FR38 <b>Indicador 34-</b> Crianças expostas com aleitamento materno exclusivo aos 5 meses –
   * coorte de 9 meses
   *
   * <p>O sistema irá produzir o Indicador 34 “Total de crianças expostas com aleitamento materno
   * exclusivo aos 5 meses” da seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta há 9 meses atrás que foram expostas
   *       ao HIV (CCR-FR36).
   *   <li>Filtrando as crianças que tiveram registo de “Aleitamento Materno Exclusivo” igual a
   *       “Sim” numa “Ficha de Seguimento de CCR” registada no período compreendido entre “Data
   *       Iníco” – 8 meses e “Data Fim”, tendo a criança nesta consulta idade = 5 meses (“Data
   *       Consulta” menos “Data Nascimento” = 5 meses).
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getExposedChildrenWithBreastfed5MonthsOfAge() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "Crianças expostas com aleitamento materno exclusivo aos 5 meses – coorte de 9 meses");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("exposed", map(getExposedChildren(), mapping));
    cd.addSearch("breastfed", map(getChildrenWithAleitamentoMaterno(), mapping4));
    cd.addSearch("age", map(getInfantAgeOnBreastfed(), mapping4));

    cd.setCompositionString("exposed AND breastfed AND age");
    return cd;
  }

  /**
   * Filtrando as crianças que tiveram registo de “Aleitamento Misto” igual a “Sim” numa “Ficha de
   * Seguimento de CCR” registada no período compreendido entre “Data Iníco” – 8 meses e “Data Fim”
   *
   * @see #getExposedChildrenWithMixedFeeding5MonthsOfAge
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWithMixedFeeding() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Crianças expostas com aleitamento ");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("6046", commonMetadata.getMixedFeedingConcept().getConceptId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());

    String query =
        "SELECT "
            + "    p.patient_id "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_type = ${93} "
            + "    AND o.concept_id = ${6046} "
            + "    AND o.value_coded = ${1065} "
            + "    AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "GROUP BY "
            + "    p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * tendo a criança nesta consulta idade = 5 meses (“Data Consulta” menos “Data Nascimento” = 5
   * meses)
   *
   * @see #getExposedChildrenWithMixedFeeding5MonthsOfAge
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getInfantAgeOnMixedFeeding() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Infant Age on Mixed Feeding");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("6046", commonMetadata.getMixedFeedingConcept().getConceptId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());

    String query =
        "SELECT "
            + "    pr.person_id "
            + "FROM "
            + "    person pr "
            + "        INNER JOIN ( "
            + "        SELECT "
            + "            p.patient_id, "
            + "            e.encounter_datetime AS breastfed_date "
            + "        FROM "
            + "            patient p "
            + "             INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "             INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "        WHERE "
            + "            p.voided = 0 "
            + "          AND e.voided = 0 "
            + "          AND o.voided = 0 "
            + "          AND e.location_id = :location "
            + "          AND e.encounter_type = ${93} "
            + "          AND o.concept_id = ${6046} "
            + "          AND o.value_coded = ${1065} "
            + "          AND e.encounter_datetime >= :startDate "
            + "          AND e.encounter_datetime <= :endDate "
            + "        GROUP BY "
            + "            p.patient_id, e.encounter_datetime "
            + "    ) ccr "
            + "                   ON pr.person_id = ccr.patient_id "
            + "WHERE "
            + "    pr.birthdate IS NOT NULL "
            + "  AND ccr.breastfed_date IS NOT NULL "
            + "  AND TIMESTAMPDIFF( DAY, pr.birthdate, ccr.breastfed_date ) BETWEEN 150 AND 179";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * CCR-FR39 <b>Indicador 35-</b> Total de crianças expostas com aleitamento materno exclusivo aos
   * 5 meses – coorte 9 meses
   *
   * <p>O sistema irá produzir o Indicador 35 “Total de crianças expostas em Aleitamento Misto aos 5
   * meses” da seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta há 9 meses atrás que foram expostas
   *       ao HIV (CCR-FR36).
   *   <li>Filtrando as crianças que tiveram registo de “Aleitamento Misto” igual a “Sim” numa
   *       “Ficha de Seguimento de CCR” registada no período compreendido entre “Data Iníco” – 8
   *       meses e “Data Fim”, tendo a criança nesta consulta idade = 5 meses (“Data Consulta” menos
   *       “Data Nascimento” = 5 meses).
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getExposedChildrenWithMixedFeeding5MonthsOfAge() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "Total de crianças expostas com aleitamento materno exclusivo aos 5 meses – coorte 9 meses");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("exposed", map(getExposedChildren(), mapping));
    cd.addSearch("mixedFeed", map(getChildrenWithMixedFeeding(), mapping4));
    cd.addSearch("age", map(getInfantAgeOnMixedFeeding(), mapping4));

    cd.setCompositionString("exposed AND mixedFeed AND age");
    return cd;
  }

  /**
   * Filtrando as crianças que tiveram registo de “Profilaxia com Nevirapina” igual a “Sim” ou
   * “Profilaxia com Zidovudina” igual a “Sim” numa “Ficha de Seguimento de CCR” registada no
   * período compreendido entre “Data Iníco” – 8 meses e “Data Fim”
   *
   * @see #getExposedChildrenWhoReceivedArv5MonthsOfAge
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWhoReceivedArv() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Crianças que receberam ARV ");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("631", commonMetadata.getNevirapineConcept().getConceptId());
    map.put("797", hivMetadata.getZidovudineConcept().getConceptId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());

    String query =
        "SELECT "
            + "    p.patient_id "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_type = ${93} "
            + "    AND o.concept_id IN ( ${631}, ${797} ) "
            + "    AND o.value_coded = ${1065} "
            + "    AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "GROUP BY "
            + "    p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * Filtrando as crianças que tiveram registo de “Profilaxia com Nevirapina” igual a “Sim” ou
   * “Profilaxia com Zidovudina” igual a “Sim” numa “Ficha de Seguimento de CCR” registada no
   * período compreendido entre “Data Iníco” – 8 meses e “Data Fim”
   *
   * @see #getExposedChildrenWhoReceivedArv5MonthsOfAge
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getInfantAgeOnArv(Integer age) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Infant Age on ARV");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("631", commonMetadata.getNevirapineConcept().getConceptId());
    map.put("797", hivMetadata.getZidovudineConcept().getConceptId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    map.put("age", age);

    String query =
        "SELECT "
            + "    pr.person_id "
            + "FROM "
            + "    person pr "
            + "        INNER JOIN ( "
            + "        SELECT "
            + "            p.patient_id, "
            + "            e.encounter_datetime AS breastfed_date "
            + "        FROM "
            + "            patient p "
            + "             INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "             INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "        WHERE "
            + "            p.voided = 0 "
            + "          AND e.voided = 0 "
            + "          AND o.voided = 0 "
            + "          AND e.location_id = :location "
            + "          AND e.encounter_type = ${93} "
            + "          AND o.concept_id IN ( ${631}, ${797} ) "
            + "          AND o.value_coded = ${1065} "
            + "          AND e.encounter_datetime >= :startDate "
            + "          AND e.encounter_datetime <= :endDate "
            + "        GROUP BY "
            + "            p.patient_id "
            + "    ) ccr "
            + "                   ON pr.person_id = ccr.patient_id "
            + "WHERE "
            + "    pr.birthdate IS NOT NULL "
            + "  AND ccr.breastfed_date IS NOT NULL "
            + "  AND TIMESTAMPDIFF(MONTH , pr.birthdate, ccr.breastfed_date) = ${age}";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * CCR-FR40 <b>Indicador 36-</b> Crianças que receberam ARV aos 5 meses – coorte de 9 meses
   *
   * <p>O sistema irá produzir o Indicador 36 “Total de crianças expostas que receberam ARV aos 5
   * meses” da seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta há 9 meses atrás que foram expostas
   *       ao HIV (CCR-FR36).
   *   <li>Filtrando as crianças que tiveram registo de “Profilaxia com Nevirapina” igual a “Sim” ou
   *       “Profilaxia com Zidovudina” igual a “Sim” numa “Ficha de Seguimento de CCR” registada no
   *       período compreendido entre “Data Iníco” – 8 meses e “Data Fim”, tendo a criança nesta
   *       consulta idade = 5 meses (“Data Consulta” menos “Data Nascimento” = 5 meses).
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getExposedChildrenWhoReceivedArv5MonthsOfAge() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças que receberam ARV aos 5 meses – coorte de 9 meses");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("exposed", map(getExposedChildren(), mapping));
    cd.addSearch("arv", map(getChildrenWhoReceivedArv(), mapping4));
    cd.addSearch("age", map(getInfantAgeOnArv(5), mapping4));

    cd.setCompositionString("exposed AND arv AND age");
    return cd;
  }

  /**
   * CCR-FR41 <b>Indicador 37-</b> PCR colhido <2 meses de idade – coorte de 9 meses
   *
   * <p>O sistema irá produzir o Indicador 37 “Total de PCR colhido <2 meses de idade” da seguinte
   * forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta há 9 meses atrás que foram expostas
   *       ao HIV (CCR-FR36).
   *   <li>Filtrando as crianças que tiveram registo de “PCR (Data de Colheita), na “Ficha de
   *       Seguimento de CCR” e ocorrida no período compreendido entre “Data Iníco” – 8 meses e
   *       “Data Fim”, tendo a criança nesta data idade <2 meses (“PCR (Data de Colheita)” menos
   *       “Data Nascimento” < 2 meses). Nota: em caso de existência de registo de mais que uma “PCR
   *       (Data de Colheita) durante o período será considerada a primeira ocorrência.
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getExposedChildrenWhoReceivedPcrWithLessThan2MonthsOfAge() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("PCR colhido <2 meses de idade  – coorte de 9 meses");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("exposed", map(getExposedChildren(), mapping));
    cd.addSearch("pcr", map(getChildrenFirstPcr(), mapping4));
    cd.addSearch("age", map(getInfantAgeAtPcr(false, 2), mapping4));

    cd.setCompositionString("exposed AND pcr AND age");
    return cd;
  }

  /**
   * CCR-FR42 <b>Indicador 38-</b> PCR colhido ≥2 meses de idade – coorte de 9 meses
   *
   * <p>O sistema irá produzir o Indicador 38 “Total de PCR colhido >=2 meses de idade”, da seguinte
   * forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta há 9 meses atrás que foram expostas
   *       ao HIV (CCR-FR36).
   *   <li>Filtrando as crianças que tiveram registo de “PCR (Data de Colheita), na “Ficha de
   *       Seguimento de CCR” e ocorrida no período compreendido entre “Data Iníco” – 8 meses e
   *       “Data Fim”, tendo a criança nesta data idade >=2 meses (“PCR (Data de Colheita)” menos
   *       “Data Nascimento” >= 2 meses). Nota: em caso de existência de registo de mais que uma
   *       “PCR (Data de Colheita) durante o período será considerada a primeira ocorrência.
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getExposedChildrenWhoReceivedPcrWithMoreThan2MonthsOfAge() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("PCR colhido >=2 meses de idade  – coorte de 9 meses");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("exposed", map(getExposedChildren(), mapping));
    cd.addSearch("pcr", map(getChildrenFirstPcr(), mapping4));
    cd.addSearch("age", map(getInfantAgeAtPcr(true, 2), mapping4));

    cd.setCompositionString("exposed AND pcr AND age");
    return cd;
  }

  /**
   * Filtrando as crianças que tiveram registo de “PCR (Resultado) igual a “Positivo”, numa “Ficha
   * de Seguimento de CCR” registada no período compreendido entre “Data Iníco” – 8 meses e “Data
   * Fim”
   *
   * @see #getChildrenWithPositivePcrBellow2MonthsOfAge
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWithPositivePcr() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("registo de PCR (data da colheita), na Ficha de Seguimento de CCR");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("1030", hivMetadata.getHivPCRQualitativeConceptUuid().getConceptId());
    map.put("703", hivMetadata.getPositive().getConceptId());

    String query =
        "SELECT pat.patient_id FROM ( "
            + "SELECT "
            + "    p.patient_id, MAX(e.encounter_datetime) AS last_pcr "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_type = ${93} "
            + "    AND o.concept_id = ${1030} "
            + "    AND o.value_coded = ${703} "
            + "    AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "GROUP BY "
            + "    p.patient_id "
            + ") pat";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * Filtrando as crianças que tiveram o último registo de “HIV (teste rápido)” como “Negativo”, na
   * “Ficha de Seguimento de CCR” registada no período compreendido entre “Data Iníco” – 17 meses e
   * “Data Fim.
   *
   * @see #getChildrenWithNegativePcrIn18Months
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWithNegativePcr() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("HIV (teste rápido) como Negativo");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("1030", hivMetadata.getHivPCRQualitativeConceptUuid().getConceptId());
    map.put("664", hivMetadata.getNegative().getConceptId());

    String query =
        "SELECT pat.patient_id FROM ( "
            + "SELECT "
            + "    p.patient_id, MAX(e.encounter_datetime) AS last_pcr "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_type = ${93} "
            + "    AND o.concept_id = ${1030} "
            + "    AND o.value_coded = ${664} "
            + "    AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "GROUP BY "
            + "    p.patient_id "
            + ") pat";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * Filtrando as crianças que tiveram registo de “PCR (Resultado) igual a “Positivo”, numa “Ficha
   * de Seguimento de CCR” registada no período compreendido entre “Data Iníco” – 8 meses e “Data
   * Fim”, tendo a criança nesta data idade < 2 meses (“Data Consulta” menos “Data Nascimento” < 2
   * meses). Nota: em caso de existência de registo de mais que uma “Ficha de Seguimento de CCR”
   * durante o período será considerado o primeiro registo
   *
   * @see #getChildrenWithPositivePcrBellow2MonthsOfAge
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getInfantAgeAtPcrResult(boolean greaterThan, Integer age) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Infant Age at PCR");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("1030", hivMetadata.getHivPCRQualitativeConceptUuid().getConceptId());
    map.put("703", hivMetadata.getPositive().getConceptId());
    map.put("age", age);

    String query =
        "SELECT "
            + "    pr.person_id "
            + "FROM "
            + "    person pr "
            + "        INNER JOIN ( "
            + "        SELECT "
            + "            p.patient_id, "
            + "            MIN(e.encounter_datetime) AS last_pcr "
            + "        FROM "
            + "            patient p "
            + "             INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "             INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "        WHERE "
            + "            p.voided = 0 "
            + "          AND e.voided = 0 "
            + "          AND o.voided = 0 "
            + "          AND e.location_id = :location "
            + "          AND e.encounter_type = ${93} "
            + "          AND o.concept_id = ${1030} "
            + "          AND o.value_coded = ${703} "
            + "          AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "        GROUP BY "
            + "            p.patient_id "
            + "    ) pcr "
            + "                   ON pr.person_id = pcr.patient_id "
            + "WHERE "
            + "    pr.birthdate IS NOT NULL "
            + "  AND pcr.last_pcr IS NOT NULL ";
    if (greaterThan) {
      query += "  AND TIMESTAMPDIFF(MONTH , pr.birthdate, pcr.last_pcr) >= ${age}";
    } else {
      query += "  AND TIMESTAMPDIFF(MONTH , pr.birthdate, pcr.last_pcr) < ${age}";
    }

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * CCR-FR43 <b>Indicador 39-</b> crianças com resultados PCR positivo <2 meses de idade – coorte
   * de 9 meses
   *
   * <p>O sistema irá produzir o Indicador 39 “Total de crianças com resultados PCR positivo <2
   * meses de idade” da seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta há 9 meses atrás que foram expostas
   *       ao HIV (CCR-FR36).
   *   <li>Filtrando as crianças que tiveram registo de “PCR (Resultado) igual a “Positivo”, numa
   *       “Ficha de Seguimento de CCR” registada no período compreendido entre “Data Iníco” – 8
   *       meses e “Data Fim”, tendo a criança nesta data idade < 2 meses (“Data Consulta” menos
   *       “Data Nascimento” < 2 meses). Nota: em caso de existência de registo de mais que uma
   *       “Ficha de Seguimento de CCR” durante o período será considerado o primeiro registo.
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWithPositivePcrBellow2MonthsOfAge() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("crianças com resultados PCR positivo <2 meses de idade  – coorte de 9 meses");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("exposed", map(getExposedChildren(), mapping));
    cd.addSearch("pcr", map(getChildrenWithPositivePcr(), mapping4));
    cd.addSearch("age", map(getInfantAgeAtPcrResult(false, 2), mapping4));

    cd.setCompositionString("exposed AND pcr AND age");
    return cd;
  }

  /**
   * CCR-FR44 <b>Indicador 40-</b> crianças com resultados PCR positivo >= 2 meses de idade – coorte
   * de 9 meses
   *
   * <p>O sistema irá produzir o Indicador 40 “Total de crianças com resultados PCR positivo >= 2
   * meses de idade” da seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta há 9 meses atrás que foram expostas
   *       ao HIV (CCR-FR36).
   *   <li>Filtrando as crianças que tiveram registo de “PCR (Resultado) igual a “Positivo”, numa
   *       “Ficha de Seguimento de CCR” registada no período compreendido entre “Data Iníco” – 8
   *       meses e “Data Fim”, tendo a criança nesta data idade >= 2 meses (“Data Consulta” menos
   *       “Data Nascimento” >= 2 meses). Nota: em caso de existência de registo de mais que uma
   *       “Ficha de Seguimento de CCR” durante o período será considerado o primeiro registo.
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWithPositivePcrAbove2MonthsOfAge() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("crianças com resultados PCR positivo >= 2 meses de idade  – coorte de 9 meses");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("exposed", map(getExposedChildren(), mapping));
    cd.addSearch("pcr", map(getChildrenWithPositivePcr(), mapping4));
    cd.addSearch("age", map(getInfantAgeAtPcrResult(true, 2), mapping4));

    cd.setCompositionString("exposed AND pcr AND age");
    return cd;
  }

  /**
   * CCR-FR46 <b>Indicador 41-</b> Crianças expostas – coorte de 18 meses
   *
   * <p>O sistema irá produzir o Indicador 41 “Total de crianças expostas”, da seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta há 18 meses (CCR-FR45) e o “Motivo
   *       da consulta” igual a "Exposição ao HIV” registado na “Ficha Resumo de CCR” com a “Data de
   *       Abertura do Processo” ocorrida há 18 meses (“Data de abertura do processo”>= “Data
   *       Início” – 17 meses e <= “Data Fim” – 17 meses).
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getExposedChildrenIn18Months() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças expostas – coorte de 18 meses");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("firstConsultation", map(getPatients1stConsultation(), mapping5));
    cd.addSearch(
        "hivExposure",
        map(
            getChildrenWithVisitReason(
                Collections.singletonList(
                    commonMetadata.getRecenNascidoMaeHivPositivoConcept().getConceptId())),
            mapping5));

    cd.setCompositionString("firstConsultation AND hivExposure");
    return cd;
  }

  /**
   * CCR-FR47 <b>Indicador 42-</b> Crianças expostas com resultado definitivo de HIV positivo –
   * coorte de 18 meses
   *
   * <p>O sistema irá produzir o Indicador 42 “Total de crianças expostas com resultado definitivo
   * de HIV positivo” da seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta há 18 meses atrás que foram
   *       expostas ao HIV (CCR-FR46).
   *   <li>Filtrando as crianças que tiveram o último registo de “PCR (Resultado)” como “Positivo”,
   *       ou o último registo de “HIV (teste rápido)” como “Positivo”, na “Ficha de Seguimento de
   *       CCR” registada no período compreendido entre “Data Iníco” – 17 meses e “Data Fim”.
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWithPositivePcrIn18Months() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças expostas com resultado definitivo de HIV positivo – coorte de 18 meses");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("firstConsultation", map(getPatients1stConsultation(), mapping5));
    cd.addSearch("positivePcr", map(getChildrenWithPositivePcr(), mapping6));

    cd.setCompositionString("firstConsultation AND positivePcr");
    return cd;
  }

  /**
   * CCR-FR48 <b>Indicador 43-</b> Crianças expostas com resultado definitivo de HIV negativo –
   * coorte de 18 meses
   *
   * <p>O sistema irá produzir o Indicador 42 “Total de crianças expostas com resultado definitivo
   * de HIV positivo” da seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta há 18 meses atrás que foram
   *       expostas ao HIV (CCR-FR46).
   *   <li>Filtrando as crianças que tiveram o último registo de “HIV (teste rápido)” como
   *       “Negativo”, na “Ficha de Seguimento de CCR” registada no período compreendido entre “Data
   *       Iníco” – 17 meses e “Data Fim.
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWithNegativePcrIn18Months() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças expostas com resultado definitivo de HIV negativo – coorte de 18 meses");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("firstConsultation", map(getPatients1stConsultation(), mapping5));
    cd.addSearch("negativePcr", map(getChildrenWithNegativePcr(), mapping6));

    cd.setCompositionString("firstConsultation AND negativePcr");
    return cd;
  }

  /**
   * CCR-FR49 <b>Indicador 44-</b> Crianças expostas com transferidas para a Consulta Criança Sadia
   * – coorte de 18 meses
   *
   * <p>O sistema irá produzir Indicador 44 “Total de crianças expostas transferidas para a Consulta
   * Criança Sadia” da seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta há 18 meses atrás que foram
   *       expostas ao HIV (CCR-FR46).
   *   <li>Filtrando as crianças que tiveram registo de “Transferido para Consulta de Criança Sadia”
   *       na “Ficha Resumo de CCR” com a “Data de Abertura do Processo” ocorrida há 18 meses (“Data
   *       de abertura do processo”>= “Data Início” – 17 meses e <= “Data Fim” – 17 meses) ou na
   *       última “Ficha de Seguimento de CCR” registada no período compreendido entre “Data Início”
   *       – 17 meses e “Data Fim”.
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenTransferedForConsultation() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "Crianças expostas com transferidas para a Consulta Criança Sadia  – coorte de 18 meses");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("firstConsultation", map(getPatients1stConsultation(), mapping5));
    cd.addSearch("restoredDam", map(getChildrenWithRestoredDam(), mapping7));

    cd.setCompositionString("firstConsultation AND restoredDam");
    return cd;
  }

  /**
   * Filtrando as crianças que tiveram registo de “Transferido para Consultas Integradas” na “Ficha
   * Resumo de CCR” com a “Data de Abertura do Processo” ocorrida há 18 meses (“Data de abertura do
   * processo”>= “Data Início” – 17 meses e <= “Data Fim” – 17 meses) ou na última “Ficha de
   * Seguimento de CCR” registada no período compreendido entre “Data Início” – 17 meses e “Data
   * Fim”
   *
   * @see #getChildrenTransferedForIntegratedConsultation
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWithIntegratedConsultation() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Crianças com DAM recuperadas");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("actualEndDate", "Actual End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("92", hivMetadata.getCCRResumoEncounterType().getEncounterTypeId());
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("1873", hivMetadata.getTipoDeAltaConcept().getConceptId());
    map.put("165484", hivMetadata.getTransferidoParaConsultasIntegradasConcept().getConceptId());

    String query =
        "SELECT "
            + "    p.patient_id "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${92} "
            + "    AND o.concept_id = ${1873} "
            + "    AND o.value_coded = ${165484} "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "GROUP BY "
            + "    p.patient_id "
            + "UNION "
            + "SELECT "
            + "    p.patient_id "
            + "FROM "
            + "    patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "    INNER JOIN ( "
            + getLastCcrSeguimentoConsulation()
            + "    ) ccr ON ccr.patient_id = p.patient_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${93} "
            + "    AND o.concept_id = ${1873} "
            + "    AND o.value_coded = ${165484} "
            + "    AND e.location_id = :location "
            + "    AND e.encounter_datetime = ccr.last_consultation_date "
            + "GROUP BY "
            + "    p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * CCR-FR50 <b>Indicador 45-</b> Crianças expostas transferidas para as Consultas Integradas –
   * coorte de 18 meses
   *
   * <p>O sistema irá produzir o Indicador 45 “Total de crianças expostas transferidas para as
   * Consultas Integradas” da seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta há 18 meses atrás que foram
   *       expostas ao HIV (CCR-FR46).
   *   <li>Filtrando as crianças que tiveram registo de “Transferido para Consultas Integradas” na
   *       “Ficha Resumo de CCR” com a “Data de Abertura do Processo” ocorrida há 18 meses (“Data de
   *       abertura do processo”>= “Data Início” – 17 meses e <= “Data Fim” – 17 meses) ou na última
   *       “Ficha de Seguimento de CCR” registada no período compreendido entre “Data Início” – 17
   *       meses e “Data Fim”.
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenTransferedForIntegratedConsultation() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças expostas transferidas para as Consultas Integradas – coorte de 18 meses");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("firstConsultation", map(getPatients1stConsultation(), mapping5));
    cd.addSearch("integrated", map(getChildrenWithIntegratedConsultation(), mapping7));

    cd.setCompositionString("firstConsultation AND integrated");
    return cd;
  }

  /**
   * CCR-FR51 <b>Indicador 46-</b> Crianças expostas que abandonaram – coorte de 18 meses
   *
   * <p>O sistema irá produzir o Indicador 46 “Total de crianças expostas que abandonaram” da
   * seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta há 18 meses atrás que foram
   *       expostas ao HIV (CCR-FR46).
   *   <li>Filtrando as crianças que tiveram registo de “Abandono” na “Ficha Resumo de CCR” com a
   *       “Data de Abertura do Processo” ocorrida há 18 meses (“Data de abertura do processo”>=
   *       “Data Início” – 17 meses e <= “Data Fim” – 17 meses) ou na última “Ficha de Seguimento de
   *       CCR” registada no período compreendido entre “Data Início” – 17 meses e “Data Fim”.
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWhoAbandonedIn18Months() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças expostas que abandonaram   – coorte de 18 meses");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("firstConsultation", map(getPatients1stConsultation(), mapping5));
    cd.addSearch("abandoned", map(getChildrenWhoAbandoned(), mapping7));

    cd.setCompositionString("firstConsultation AND abandoned");
    return cd;
  }

  /**
   * CCR-FR52 <b>Indicador 47-</b> Crianças expostas que foram óbito – coorte de 18 meses
   *
   * <p>O sistema irá produzir o Indicador 47 “Total de crianças expostas que foram óbito” da
   * seguinte forma:
   *
   * <ul>
   *   <li>Incluindo todas as crianças que tiveram a 1ª consulta há 18 meses atrás que foram
   *       expostas ao HIV (CCR-FR46).
   *   <li>Filtrando as crianças que tiveram registo de “Óbito” na “Ficha Resumo de CCR” com a “Data
   *       de Abertura do Processo” ocorrida há 18 meses (“Data de abertura do processo”>= “Data
   *       Início” – 17 meses e <= “Data Fim” – 17 meses) ou na última “Ficha de Seguimento de CCR”
   *       registada no período compreendido entre “Data Início” – 17 meses e “Data Fim”.
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getChildrenWithDagWhoDiedIn18Months() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Crianças expostas que foram óbito  – coorte de 18 meses");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    cd.addSearch("firstConsultation", map(getPatients1stConsultation(), mapping5));
    cd.addSearch("died", map(getChildrenWhoDied(), mapping7));

    cd.setCompositionString("firstConsultation AND died");
    return cd;
  }
}
