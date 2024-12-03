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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.reporting.cohort.definition.*;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResumoMensalCcrCohortQueries {

  private HivMetadata hivMetadata;
  private CommonMetadata commonMetadata;

  @Autowired
  public ResumoMensalCcrCohortQueries(HivMetadata hivMetadata, CommonMetadata commonMetadata) {
    this.hivMetadata = hivMetadata;
    this.commonMetadata = commonMetadata;
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

    String query = "SELECT p.patient_id " + " FROM ( " + get1stCcrConsulation() + " ) ";

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
  public CohortDefinition getChildrenWithVisitReason(Concept reasonConcept) {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Children With Turbeculosis Contact");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Health Facility", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("92", hivMetadata.getCCRResumoEncounterType().getEncounterTypeId());
    map.put("1874", commonMetadata.getMotivoConsultaCriancaRiscoConcept().getConceptId());
    map.put("reasonConcept", reasonConcept.getConceptId());

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
            + "    AND e.encounter_type = ${92} "
            + "    AND o.concept_id = ${1874} "
            + "    AND o.value_coded = ${reasonConcept} "
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
   * <b>CCR-FR8</b>
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
        map(getChildrenWithVisitReason(hivMetadata.getChronicMalnutritionConcept()), mapping));
    cd.addSearch(
        "damConsultation", map(getDamChildren(hivMetadata.getModerateNutritionConcept()), mapping));

    cd.setCompositionString("damReason AND damConsultation");
    return cd;
  }
}
