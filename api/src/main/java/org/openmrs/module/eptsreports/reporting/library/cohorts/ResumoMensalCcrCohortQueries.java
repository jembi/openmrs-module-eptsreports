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
    map.put(
        "reasonConcept", StringUtils.join(reasonsConcept, ",")); // Une os conceitos com vírgulas

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

  public CohortDefinition getInfantAge(boolean greaterThan, Integer Age) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Infant Age");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("92", hivMetadata.getCCRResumoEncounterType().getEncounterTypeId());
    map.put("Age", Age);

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
      query = query + "  AND TIMESTAMPDIFF(MONTH , pr.birthdate, ccr.enrollment_date) >= ${Age}";
    } else {
      query = query + "  AND TIMESTAMPDIFF(MONTH , pr.birthdate, ccr.enrollment_date) < ${Age}";
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
}
