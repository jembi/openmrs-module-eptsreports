/*
 * The contents of this file are subject to the OpenMRS Public License Version
 * 1.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 *
 * Copyright (C) OpenMRS, LLC. All Rights Reserved.
 */
package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.calculation.mq.BreastfeedingPregnantCalculation4MQ;
import org.openmrs.module.eptsreports.reporting.calculation.pvls.BreastfeedingPregnantCalculation;
import org.openmrs.module.eptsreports.reporting.calculation.pvls.OnArtForMoreThanXmonthsCalculation;
import org.openmrs.module.eptsreports.reporting.cohort.definition.CalculationCohortDefinition;
import org.openmrs.module.eptsreports.reporting.library.queries.TxPvlsQueries;
import org.openmrs.module.eptsreports.reporting.library.queries.ViralLoadQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsQueriesUtil;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportConstants.PregnantOrBreastfeedingWomen;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.definition.library.DocumentedDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Defines all of the TxPvls Cohort Definition instances we want to expose for EPTS */
@Component
public class TxPvlsCohortQueries {

  private HivCohortQueries hivCohortQueries;

  private HivMetadata hivMetadata;

  @Autowired
  public TxPvlsCohortQueries(HivCohortQueries hivCohortQueries, HivMetadata hivMetadata) {
    this.hivCohortQueries = hivCohortQueries;
    this.hivMetadata = hivMetadata;
  }

  /**
   * <b>Description</b> On ART more than 3 months <blockqoute> Patients who have NOT been on ART for
   * 3 months based on the ART initiation date and date of last viral load registered </blockqoute>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoAreMoreThan3MonthsOnArt(
      List<EncounterType> encounterTypeList) {
    CalculationCohortDefinition cd =
        new CalculationCohortDefinition(
            "On ART for at least 3 months for pvls",
            Context.getRegisteredComponents(OnArtForMoreThanXmonthsCalculation.class).get(0));
    cd.addParameter(new Parameter("onOrBefore", "On or before Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addCalculationParameter("listOfEncounters", encounterTypeList);
    return cd;
  }

  /**
   * <b>Description</b>Breast feeding women with viral load suppression and on ART for more than 3
   * months
   *
   * @return CohortDefinition
   */
  @DocumentedDefinition(value = "breastfeedingWomenWithViralSuppression")
  public CohortDefinition getBreastfeedingWomenWhoHaveViralSuppression() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Breastfeeding with viral suppression");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "breastfeeding",
        EptsReportUtils.map(getBreastfeedingPatients(), "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "suppression",
        EptsReportUtils.map(
            getPatientsWithViralLoadSuppressionWhoAreOnArtMoreThan3Months(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.setCompositionString("breastfeeding AND suppression");

    return cd;
  }

  /**
   * <b>Description</b> Breast feeding women with viral load suppression
   *
   * @return CohortDefinition
   */
  @DocumentedDefinition(value = "breastfeedingWomenWithViralLoadResults")
  public CohortDefinition getBreastfeedingWomenWhoHaveViralLoadResults() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Breastfeeding with viral results");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "breastfeeding",
        EptsReportUtils.map(getBreastfeedingPatients(), "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "results",
        EptsReportUtils.map(
            getPatientsWithViralLoadResultsAndOnArtForMoreThan3Months(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.setCompositionString("breastfeeding AND results");

    return cd;
  }

  /**
   * <b>Description</b> Viral load suppression
   *
   * <blockquote>
   *
   * Patients with viral suppression of <1000 in the last 12 months excluding dead, LTFU,
   * transferred out, stopped ART
   *
   * </blockquote>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWithViralLoadSuppressionWhoAreOnArtMoreThan3Months() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";
    cd.addSearch(
        "supp",
        EptsReportUtils.map(
            hivCohortQueries.getPatientsWithSuppressedViralLoadWithin12Months(), mappings));
    cd.addSearch(
        "onArtLongEnough",
        EptsReportUtils.map(
            getPatientsWhoAreMoreThan3MonthsOnArt(
                Arrays.asList(
                    hivMetadata.getMisauLaboratorioEncounterType(),
                    hivMetadata.getAdultoSeguimentoEncounterType(),
                    hivMetadata.getPediatriaSeguimentoEncounterType(),
                    hivMetadata.getMasterCardEncounterType(),
                    hivMetadata.getFsrEncounterType())),
            "onOrBefore=${endDate},location=${location}"));
    cd.setCompositionString("supp AND onArtLongEnough");
    return cd;
  }

  /**
   * <b>Description</b> Viral load results composition
   *
   * <blockquote>
   *
   * Patients with viral results recorded in the last 12 months excluding dead, LTFU, transferred
   * out, stopped ARTtxNewCohortQueries Only filter out patients who are on routine
   *
   * </blockquote>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWithViralLoadResultsAndOnArtForMoreThan3Months() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";
    cd.addSearch(
        "results",
        EptsReportUtils.map(hivCohortQueries.getPatientsViralLoadWithin12Months(), mappings));
    cd.addSearch(
        "onArtLongEnough",
        EptsReportUtils.map(
            getPatientsWhoAreMoreThan3MonthsOnArt(
                Arrays.asList(
                    hivMetadata.getMisauLaboratorioEncounterType(),
                    hivMetadata.getAdultoSeguimentoEncounterType(),
                    hivMetadata.getPediatriaSeguimentoEncounterType(),
                    hivMetadata.getMasterCardEncounterType(),
                    hivMetadata.getFsrEncounterType())),
            "onOrBefore=${endDate},location=${location}"));
    cd.setCompositionString("results AND onArtLongEnough");
    return cd;
  }
  /**
   * <b>Description</b> Viral load results and on routine composition
   *
   * <blockquote>
   *
   * Patients with viral results recorded in the last 12 months excluding dead, LTFU, transferred
   * out, stopped ARTtxNewCohortQueries Only filter out patients who are on routine
   *
   * </blockquote>
   *
   * @return Cohort
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWithViralLoadResultsAndOnRoutine() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";
    cd.addSearch(
        "results",
        EptsReportUtils.map(hivCohortQueries.getPatientsViralLoadWithin12Months(), mappings));
    cd.addSearch(
        "onArtLongEnough",
        EptsReportUtils.map(
            getPatientsWhoAreMoreThan3MonthsOnArt(
                Arrays.asList(
                    hivMetadata.getMisauLaboratorioEncounterType(),
                    hivMetadata.getAdultoSeguimentoEncounterType(),
                    hivMetadata.getPediatriaSeguimentoEncounterType(),
                    hivMetadata.getMasterCardEncounterType(),
                    hivMetadata.getFsrEncounterType())),
            "onOrBefore=${endDate},location=${location}"));
    cd.addSearch("Routine", EptsReportUtils.map(getPatientsWhoAreOnRoutine(), mappings));
    cd.setCompositionString("(results AND onArtLongEnough) AND Routine");
    return cd;
  }

  /**
   * <b>Description</b Viral load results and on target
   *
   * <blockquote>
   *
   * Patients with viral results recorded in the last 12 months excluding dead, LTFU, transferred
   * out, stopped ARTtxNewCohortQueries Only filter out patients who are on target
   *
   * </blockquote>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWithViralLoadResultsAndOnTarget() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";
    cd.addSearch(
        "results",
        EptsReportUtils.map(hivCohortQueries.getPatientsViralLoadWithin12Months(), mappings));
    cd.addSearch(
        "onArtLongEnough",
        EptsReportUtils.map(
            getPatientsWhoAreMoreThan3MonthsOnArt(
                Arrays.asList(
                    hivMetadata.getMisauLaboratorioEncounterType(),
                    hivMetadata.getAdultoSeguimentoEncounterType(),
                    hivMetadata.getPediatriaSeguimentoEncounterType(),
                    hivMetadata.getMasterCardEncounterType(),
                    hivMetadata.getFsrEncounterType())),
            "onOrBefore=${endDate},location=${location}"));
    cd.addSearch("Target", EptsReportUtils.map(getPatientsWhoAreOnTarget(), mappings));
    cd.setCompositionString("(results AND onArtLongEnough) AND Target");
    return cd;
  }

  /**
   * <b>Description</b>Get patients who are on routine Composition
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoAreOnRoutine() {
    SqlCohortDefinition sql = new SqlCohortDefinition();
    sql.setName("Patients who have viral load results OR FSR coded values");
    sql.setName("Routine for all patients using FSR form");
    sql.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sql.addParameter(new Parameter("endDate", "End Date", Date.class));
    sql.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());
    map.put("23818", hivMetadata.getReasonForRequestingViralLoadConcept().getConceptId());
    map.put("23817", hivMetadata.getRoutineForRequestingViralLoadConcept().getConceptId());
    map.put("1067", hivMetadata.getUnkownConcept().getConceptId());

    StringSubstitutor sb = new StringSubstitutor(map);

    sql.setQuery(sb.replace(ViralLoadQueries.getPatientsHavingRoutineViralLoadTestsUsingFsr()));

    return sql;
  }

  public CohortDefinition getPatientsWhoAreOnRoutineOnMasterCardAndClinicalEncounter() {
    SqlCohortDefinition sqlc = new SqlCohortDefinition();
    sqlc.setName("Patients who have viral load results OR FSR coded values");
    sqlc.setName("Routine for all patients using FSR form");
    sqlc.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlc.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlc.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();

    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    StringSubstitutor sb = new StringSubstitutor(map);
    String sql =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON p.patient_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + "       INNER JOIN obs o2 "
            + "               ON e.encounter_id = o2.encounter_id "
            + "       INNER JOIN (SELECT max_vl_result.patient_id, "
            + "                          Max(max_vl_result.max_vl) last_vl "
            + "                   FROM   (SELECT p.patient_id, "
            + "                                  Date(e.encounter_datetime) AS max_vl "
            + "                           FROM   patient p "
            + "                                  INNER JOIN encounter e "
            + "                                          ON p.patient_id = e.patient_id "
            + "                                  INNER JOIN obs o "
            + "                                          ON e.encounter_id = o.encounter_id "
            + "                           WHERE  p.voided = 0 "
            + "                                  AND e.voided = 0 "
            + "                                  AND o.voided = 0 "
            + "                                  AND e.encounter_type IN ( ${6}, ${9} ) "
            + "                                  AND ( ( o.concept_id = ${856} "
            + "                                          AND o.value_numeric IS NOT NULL ) "
            + "                                         OR ( o.concept_id = ${1305} "
            + "                                              AND o.value_coded IS NOT NULL ) ) "
            + "                                  AND Date(e.encounter_datetime) <= :endDate "
            + "                                  AND e.location_id = :location "
            + "                           UNION "
            + "                           SELECT p.patient_id, "
            + "                                  Date(o.obs_datetime) AS max_vl "
            + "                           FROM   patient p "
            + "                                  INNER JOIN encounter e "
            + "                                          ON p.patient_id = e.patient_id "
            + "                                  INNER JOIN obs o "
            + "                                          ON e.encounter_id = o.encounter_id "
            + "                           WHERE  p.voided = 0 "
            + "                                  AND e.voided = 0 "
            + "                                  AND o.voided = 0 "
            + "                                  AND e.encounter_type IN ( ${53} ) "
            + "                                  AND ( ( o.concept_id = ${856} "
            + "                                          AND o.value_numeric IS NOT NULL ) "
            + "                                         OR ( o.concept_id = ${1305} "
            + "                                              AND o.value_coded IS NOT NULL ) ) "
            + "                                  AND Date(o.obs_datetime) <= :endDate "
            + "                                  AND e.location_id = :location) max_vl_result "
            + "                   GROUP  BY max_vl_result.patient_id) last_date "
            + "               ON p.patient_id = last_date.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND    ( e.encounter_type IN ( ${6}, ${9} "
            + "               AND ( ( o.concept_id = ${856} "
            + "                       AND o.value_numeric IS NOT NULL ) "
            + "                      OR ( o.concept_id = ${1305} "
            + "                           AND o.value_coded IS NOT NULL ) ) "
            + "               AND Date(e.encounter_datetime) = last_date.last_vl ) "
            + "              OR ( e.encounter_type = ${53} "
            + "                   AND ( ( o.concept_id = ${856} "
            + "                           AND o.value_numeric IS NOT NULL ) "
            + "                          OR ( o.concept_id = ${1305} "
            + "                               AND o.value_coded IS NOT NULL ) ) "
            + "                   AND Date(o.obs_datetime) = last_date.last_vl )) "
            + "GROUP  BY p.patient_id";

    sqlc.setQuery(sb.replace(sql));

    return sqlc;
  }

  /**
   * <b>Description</b> Get patients who are on target Composition
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoAreOnTarget() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("All patients on Target");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addSearch(
        "results",
        EptsReportUtils.map(
            hivCohortQueries.getPatientsViralLoadWithin12Months(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "routine",
        EptsReportUtils.map(
            getPatientsWhoAreOnRoutine(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.setCompositionString("results AND NOT routine");
    return cd;
  }

  /**
   * <b>Description</b> Get patients who are on target Composition
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoAreOnTargetBySource() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("All patients on Target");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addSearch(
        "results",
        EptsReportUtils.map(
            hivCohortQueries.getPatientsViralLoadWithin12MonthsBySource(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "routine",
        EptsReportUtils.map(
            getPatientsWhoAreOnRoutineOnMasterCardAndClinicalEncounter(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.setCompositionString("results AND NOT routine");
    return cd;
  }

  /**
   * <b>Description</b>Get patients having viral load suppression and routine for adults and
   * children - Numerator
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientWithViralSuppressionAndOnRoutine() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Suppression and on routine adult and children");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";
    cd.addSearch(
        "supp",
        EptsReportUtils.map(
            getPatientsWithViralLoadSuppressionWhoAreOnArtMoreThan3Months(), mappings));
    cd.addSearch("routine", EptsReportUtils.map(getPatientsWhoAreOnRoutine(), mappings));
    cd.setCompositionString("supp AND routine");
    return cd;
  }

  /**
   * <b>Description</b>Get patients having viral load suppression and target for adults and children
   * - Numerator
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientWithViralSuppressionAndOnTarget() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Suppression and on target adult and children");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";
    cd.addSearch(
        "supp",
        EptsReportUtils.map(
            getPatientsWithViralLoadSuppressionWhoAreOnArtMoreThan3Months(), mappings));
    cd.addSearch("routine", EptsReportUtils.map(getPatientsWhoAreOnRoutine(), mappings));
    cd.setCompositionString("supp AND NOT routine");
    return cd;
  }

  /**
   * <b>Description</b> Get pregnant women Numerator
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPregnantWomenWithViralLoadSuppression() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Get pregnant women with viral load suppression");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";
    cd.addSearch(
        "suppression",
        EptsReportUtils.map(
            getPatientsWithViralLoadSuppressionWhoAreOnArtMoreThan3Months(), mappings));
    cd.addSearch(
        "pregnant",
        EptsReportUtils.map(this.getPregnantWoman(), "endDate=${endDate},location=${location}"));
    cd.setCompositionString("suppression AND pregnant");
    return cd;
  }

  /**
   * <b>Description</b>Get pregnant women Denominator
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPregnantWomenWithViralLoadResults() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Get pregnant women with viral load results denominator");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";
    cd.addSearch(
        "results",
        EptsReportUtils.map(getPatientsWithViralLoadResultsAndOnArtForMoreThan3Months(), mappings));
    cd.addSearch(
        "pregnant",
        EptsReportUtils.map(this.getPregnantWoman(), "endDate=${endDate},location=${location}"));
    cd.setCompositionString("results AND pregnant");
    return cd;
  }

  /**
   * <b>Description</b>Get patients who are breastfeeding or pregnant controlled by parameter
   *
   * @param state state
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoArePregnantOrBreastfeedingBasedOnParameter(
      PregnantOrBreastfeedingWomen state, List<EncounterType> encounterTypeList) {
    CalculationCohortDefinition cd =
        new CalculationCohortDefinition(
            "pregnantBreastfeeding",
            Context.getRegisteredComponents(BreastfeedingPregnantCalculation.class).get(0));
    cd.addParameter(new Parameter("onOrBefore", "On or before Date", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "On or before Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addCalculationParameter("state", state);
    cd.addCalculationParameter("encounterTypeList", encounterTypeList);
    return cd;
  }
  /**
   * <b>Description</b>Get patients who are breastfeeding or pregnant controlled by parameter This
   * method implements MQ Cat 14 Criteria
   *
   * @param state state
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoArePregnantOrBreastfeedingBasedOnParameter4MQ(
      PregnantOrBreastfeedingWomen state, List<EncounterType> encounterTypeList) {
    CalculationCohortDefinition cd =
        new CalculationCohortDefinition(
            "pregnantBreastfeeding",
            Context.getRegisteredComponents(BreastfeedingPregnantCalculation4MQ.class).get(0));
    cd.addParameter(new Parameter("onOrBefore", "On or before Date", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "On or before Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addCalculationParameter("state", state);
    cd.addCalculationParameter("encounterTypeList", encounterTypeList);
    return cd;
  }

  /**
   * <b>PVLS_FR9</b>
   *
   * <blockquote>
   *
   * <p>The system will identify female patients who are breastfeeding as following:
   *
   * <ul>
   *   <li>Patients who have the “Delivery date” registered in the initial or follow-up
   *       consultations (Processo Clinico Parte A or Ficha de Seguimento Adulto) and where the
   *       delivery date is within the period range or
   *   <li>Patients who started ART for being breastfeeding as specified in “CRITÉRIO PARA INÍCIO DE
   *       TRATAMENTO ARV” in the initial or follow-up consultations (Processo Clinico Parte A or
   *       Ficha de Seguimento Adulto) that occurred within period range or chart: patient
   *       Transferred Out or
   *   <li>Patients who have been registered as breastfeeding in follow up consultation (Ficha de
   *       Seguimento Adulto) within the period range.
   *   <li>Have registered as breastfeeding in Ficha Resumo or Ficha Clinica within the period range
   *       OR
   *   <li>Patients enrolled on Prevention of the Vertical Transmission/Elimination of the Vertical
   *       Transmission (PTV/ETV) program with state 27 (gave birth) within the period range.
   *   <li>Patient who have “Actualmente está a amamentar” marked as “Sim” on FSR Form and Data de
   *       Colheita is during the period range.
   * </ul>
   *
   * <br>
   *
   * <p>If the patient has both states (pregnant and breastfeeding) the most recent one should be
   * considered. For patients who have both state (pregnant and breastfeeding) marked on the same
   * day, the system will consider the patient as pregnant.<br>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getBreastfeedingPatients() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" Patients disaggregation - breastfeeding");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("1600", hivMetadata.getPregnancyDueDate().getConceptId());
    map.put("1279", hivMetadata.getNumberOfWeeksPregnant().getConceptId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("6331", hivMetadata.getBpostiveConcept().getConceptId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("5", hivMetadata.getARVAdultInitialEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    map.put("5599", hivMetadata.getPriorDeliveryDateConcept().getConceptId());
    map.put("23821", hivMetadata.getSampleCollectionDateAndTime().getConceptId());
    map.put("6332", hivMetadata.getBreastfeeding().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("6334", hivMetadata.getCriteriaForArtStart().getConceptId());
    map.put("8", hivMetadata.getPtvEtvProgram().getProgramId());
    map.put("27", hivMetadata.getPatientGaveBirthWorkflowState().getProgramWorkflowStateId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        "SELECT last_vl.patient_id "
            + " FROM  (SELECT ultima.patient_id, "
            + " Max(ultima.last_date) AS last_date "
            + " FROM  ( "
            + getLastVlResult()
            + " )ultima "
            + " GROUP  BY ultima.patient_id)last_vl "
            + " INNER JOIN ("
            + " SELECT lactantes.patient_id, "
            + " Max(lactantes.last_date) AS breastfeeding_date "
            + " FROM ( "
            + getMaxBreastfeeding()
            + " )lactantes "
            + " GROUP  BY lactantes.patient_id "
            + " )AS breastfeeding  "
            + "  ON last_vl.patient_id = breastfeeding.patient_id "
            + " LEFT JOIN ( "
            + "select pregnant.patient_id, pregnant.pregnancy_date AS pregnancy_date "
            + " FROM  ( "
            + " SELECT ultima.patient_id, MAX(ultima.last_Date) as ultima "
            + " FROM ("
            + getLastVlResult()
            + " ) AS ultima "
            + " GROUP  BY ultima.patient_id) AS last_vl "
            + " INNER JOIN ("
            + "SELECT gravidas.patient_id, MAX(gravidas.last_Date) as pregnancy_date from ("
            + getMaxPregnant()
            + " ) gravidas "
            + " group by gravidas.patient_id)as pregnant on last_vl.patient_id = pregnant.patient_id "
            + " WHERE pregnant.pregnancy_date between date_sub(last_vl.ultima, interval 9 month) and last_vl.ultima"
            + " ) AS max_pregnant On max_pregnant.patient_id= breastfeeding.patient_id "
            + " WHERE breastfeeding.breastfeeding_date BETWEEN Date_sub(last_vl.last_date, interval 18 month) AND last_vl.last_date "
            + " AND (breastfeeding.breastfeeding_date is not NULL "
            + " AND breastfeeding.breastfeeding_date > max_pregnant.pregnancy_date) "
            + " OR max_pregnant.pregnancy_date is null ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    String mappedQuery = stringSubstitutor.replace(query);

    sqlCohortDefinition.setQuery(mappedQuery);

    return sqlCohortDefinition;
  }

  public static String getLastVlResult() {
    return new EptsQueriesUtil()
        .unionBuilder(TxPvlsQueries.getVlResultOnClinicalConsultations())
        .union(TxPvlsQueries.getVlResultOnFichaResumo())
        .buildQuery();
  }

  public static String getMaxBreastfeeding() {
    return new EptsQueriesUtil()
        .unionBuilder(
            TxPvlsQueries.getBreastfeedingOnAdultoSeguimentoANDAdultoInitialConsultation())
        .union(TxPvlsQueries.getBreastfeedingOnFichaClinica())
        .union(
            TxPvlsQueries
                .getBreastfeedingOnAdultoSeguimentoANDAdultoInitialConsultationCriterioInicioTarV())
        .union(TxPvlsQueries.getPatientsEnrolledInPtv())
        .union(TxPvlsQueries.getBreastfeedingOnFichaResumo())
        .union(TxPvlsQueries.getBreastfeedingOnFSR())
        .buildQuery();
  }

  public CohortDefinition getPregnantWoman() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" Patients disaggregation - Pregnant");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));
    Map<String, Integer> map = new HashMap<>();

    map.put("1600", hivMetadata.getPregnancyDueDate().getConceptId());
    map.put("1279", hivMetadata.getNumberOfWeeksPregnant().getConceptId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("6331", hivMetadata.getBpostiveConcept().getConceptId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("5", hivMetadata.getARVAdultInitialEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    map.put("5599", hivMetadata.getPriorDeliveryDateConcept().getConceptId());
    map.put("23821", hivMetadata.getSampleCollectionDateAndTime().getConceptId());
    map.put("6332", hivMetadata.getBreastfeeding().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("6334", hivMetadata.getCriteriaForArtStart().getConceptId());
    map.put("8", hivMetadata.getPtvEtvProgram().getProgramId());
    map.put("27", hivMetadata.getPatientGaveBirthWorkflowState().getProgramWorkflowStateId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        "SELECT last_vl.patient_id  "
            + "FROM  (SELECT ultima.patient_id,  "
            + "              Max(ultima.last_date) AS last_date  "
            + "       FROM  ( "
            + getLastVlResult()
            + " )ultima   GROUP BY ultima.patient_id ) as last_vl  "
            + "INNER JOIN(  "
            + " SELECT gravidas.patient_id, MAX(gravidas.last_Date) as pregnancy_date from ( "
            + getMaxPregnant()
            + " ) gravidas "
            + " group by gravidas.patient_id ) as pregnant on last_vl.patient_id = pregnant.patient_id "
            + " left join ( "
            + " SELECT last_vl.patient_id, breastfeeding.breastfeeding_date "
            + " FROM(  SELECT ultima.patient_id, MAX(ultima.last_date) AS last_date "
            + " FROM ("
            + getLastVlResult()
            + " ) as ultima   GROUP BY ultima.patient_id ) as last_vl  "
            + "  inner join (  "
            + "        SELECT lactantes.patient_id, MAX(lactantes.last_date) AS breastfeeding_date"
            + "        FROM ( "
            + getMaxBreastfeeding()
            + ") AS lactantes "
            + "        GROUP BY lactantes.patient_id "
            + " ) AS breastfeeding On last_vl.patient_id = breastfeeding.patient_id "
            + "  WHERE breastfeeding.breastfeeding_date BETWEEN DATE_SUB( last_vl.last_date, interval 18 MONTH ) AND last_vl.last_date "
            + " ) as brestfeading on brestfeading.patient_id = pregnant.patient_id "
            + " WHERE pregnant.pregnancy_date between date_sub(last_vl.last_date, interval 9 month) AND last_vl.last_date "
            + "    and (pregnant.pregnancy_date is not null and pregnant.pregnancy_date >= brestfeading.breastfeeding_date) "
            + "   or brestfeading.breastfeeding_date is null"
            + " group by pregnant.patient_id ";
    ;
    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    String mappedQuery = stringSubstitutor.replace(query);
    sqlCohortDefinition.setQuery(mappedQuery);
    return sqlCohortDefinition;
  }

  public static String getMaxPregnant() {
    return new EptsQueriesUtil()
        .unionBuilder(TxPvlsQueries.getPatientsMarkedAsPregnentInInitialConsultation())
        .union(TxPvlsQueries.getPatientsThatHaveNumberOfWeeksPregnantRegisteredInIinitialOrFollow())
        .union(TxPvlsQueries.getPatientsWithDeliverDueDateInInitialFlowUp())
        .union(TxPvlsQueries.getPatientsThatStartedARTForBeingInCriterioParaInicioTarv())
        .union(TxPvlsQueries.getPatientsEnrolledonPreventionoftheVerticalTransmission())
        .union(TxPvlsQueries.getPatientsRegisteredAsPregnantFichaResumoBetweenStartAndEndDate())
        .union(
            TxPvlsQueries
                .getPatientsthatFemaleAndHaveRegisteredAsPregnantFichaClinicaMasterCardBetweenStartandDate())
        .union(TxPvlsQueries.getPatientWhoActualmenteEncontraGravidaMarkedSim())
        .buildQuery();
  }
}
