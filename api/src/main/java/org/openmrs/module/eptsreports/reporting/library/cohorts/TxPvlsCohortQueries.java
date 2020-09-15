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

import java.util.Date;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.calculation.pvls.BreastfeedingPregnantCalculation;
import org.openmrs.module.eptsreports.reporting.calculation.pvls.OnArtForMoreThanXmonthsCalcultion;
import org.openmrs.module.eptsreports.reporting.cohort.definition.CalculationCohortDefinition;
import org.openmrs.module.eptsreports.reporting.library.queries.ViralLoadQueries;
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

  private final HivCohortQueries hivCohortQueries;

  private final HivMetadata hivMetadata;

  @Autowired
  public TxPvlsCohortQueries(HivCohortQueries hivCohortQueries, HivMetadata hivMetadata) {
    this.hivCohortQueries = hivCohortQueries;
    this.hivMetadata = hivMetadata;
  }

  /**
   * Patients who have NOT been on ART for 3 months based on the ART initiation date and date of
   * last viral load registered
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoAreMoreThan3MonthsOnArt() {
    CalculationCohortDefinition cd =
        new CalculationCohortDefinition(
            "On ART for at least 3 months",
            Context.getRegisteredComponents(OnArtForMoreThanXmonthsCalcultion.class).get(0));
    cd.addParameter(new Parameter("onOrBefore", "On or before Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    return cd;
  }

  /**
   * Breast feeding women with viral load suppression and on ART for more than 3 months
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
        EptsReportUtils.map(
            getPatientsWhoArePregnantOrBreastfeedingBasedOnParameter(
                PregnantOrBreastfeedingWomen.BREASTFEEDINGWOMEN),
            "onOrBefore=${endDate},location=${location}"));

    cd.addSearch(
        "suppression",
        EptsReportUtils.map(
            getPatientsWithViralLoadSuppressionWhoAreOnArtMoreThan3Months(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.setCompositionString("breastfeeding AND suppression");

    return cd;
  }

  /**
   * Breast feeding women with viral load suppression
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
        EptsReportUtils.map(
            getPatientsWhoArePregnantOrBreastfeedingBasedOnParameter(
                PregnantOrBreastfeedingWomen.BREASTFEEDINGWOMEN),
            "onOrBefore=${endDate},location=${location}"));

    cd.addSearch(
        "results",
        EptsReportUtils.map(
            getPatientsWithViralLoadResultsAndOnArtForMoreThan3Months(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.setCompositionString("breastfeeding AND results");

    return cd;
  }

  /**
   * Patients with viral suppression of <1000 in the last 12 months excluding dead, LTFU,
   * transferred out, stopped ART
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
            getPatientsWhoAreMoreThan3MonthsOnArt(), "onOrBefore=${endDate},location=${location}"));
    cd.setCompositionString("supp AND onArtLongEnough");
    return cd;
  }

  /**
   * Patients with viral results recorded in the last 12 months excluding dead, LTFU, transferred
   * out, stopped ARTtxNewCohortQueries Only filter out patients who are on routine
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
            getPatientsWhoAreMoreThan3MonthsOnArt(), "onOrBefore=${endDate},location=${location}"));
    cd.setCompositionString("results AND onArtLongEnough");
    return cd;
  }
  /**
   * Patients with viral results recorded in the last 12 months excluding dead, LTFU, transferred
   * out, stopped ARTtxNewCohortQueries Only filter out patients who are on routine
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
            getPatientsWhoAreMoreThan3MonthsOnArt(), "onOrBefore=${endDate},location=${location}"));
    cd.addSearch("Routine", EptsReportUtils.map(getPatientsWhoAreOnRoutine(), mappings));
    cd.setCompositionString("(results AND onArtLongEnough) AND Routine");
    return cd;
  }

  /**
   * Patients with viral results recorded in the last 12 months excluding dead, LTFU, transferred
   * out, stopped ARTtxNewCohortQueries Only filter out patients who are on target
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
            getPatientsWhoAreMoreThan3MonthsOnArt(), "onOrBefore=${endDate},location=${location}"));
    cd.addSearch("Target", EptsReportUtils.map(getPatientsWhoAreOnTarget(), mappings));
    cd.setCompositionString("(results AND onArtLongEnough) AND Target");
    return cd;
  }

  /**
   * Get patients who are on routine
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoAreOnRoutine() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Patients who have viral load results OR FSR coded values");
    cd.setName("Routine for all patients using FSR form");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    SqlCohortDefinition sql = new SqlCohortDefinition();
    sql.setName("Routine for all patients using FSR form");
    sql.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sql.addParameter(new Parameter("endDate", "End Date", Date.class));
    sql.addParameter(new Parameter("location", "Location", Location.class));
    sql.setQuery(
        ViralLoadQueries.getPatientsHavingRoutineViralLoadTestsUsingFsr(
            hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId(),
            hivMetadata.getReasonForRequestingViralLoadConcept().getConceptId(),
            hivMetadata.getRoutineForRequestingViralLoadConcept().getConceptId(),
            hivMetadata.getUnkownConcept().getConceptId()));
    cd.addSearch(
        "results",
        EptsReportUtils.map(
            hivCohortQueries.getPatientsViralLoadWithin12Months(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "fsr",
        EptsReportUtils.map(sql, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.setCompositionString("results OR fsr");
    return cd;
  }

  /**
   * Get patients who are on target
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
   * Get patients having viral load suppression and routine for adults and children - Numerator
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
   * Get patients having viral load suppression and target for adults and children - Numerator
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
   * Get pregnant women Numerator
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
        EptsReportUtils.map(
            this.getPatientsWhoArePregnantOrBreastfeedingBasedOnParameter(
                PregnantOrBreastfeedingWomen.PREGNANTWOMEN),
            "onOrBefore=${endDate},location=${location}"));
    cd.setCompositionString("suppression AND pregnant");
    return cd;
  }

  /**
   * Get pregnant women Denominator
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
        EptsReportUtils.map(
            this.getPatientsWhoArePregnantOrBreastfeedingBasedOnParameter(
                PregnantOrBreastfeedingWomen.PREGNANTWOMEN),
            "onOrBefore=${endDate},location=${location}"));
    cd.setCompositionString("results AND pregnant");
    return cd;
  }

  /**
   * Get patients who are breastfeeding or pregnant controlled by parameter
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoArePregnantOrBreastfeedingBasedOnParameter(
      PregnantOrBreastfeedingWomen state) {
    CalculationCohortDefinition cd =
        new CalculationCohortDefinition(
            "pregnantBreastfeeding",
            Context.getRegisteredComponents(BreastfeedingPregnantCalculation.class).get(0));
    cd.addParameter(new Parameter("onOrBefore", "On or before Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addCalculationParameter("state", state);

    return cd;
  }
}
