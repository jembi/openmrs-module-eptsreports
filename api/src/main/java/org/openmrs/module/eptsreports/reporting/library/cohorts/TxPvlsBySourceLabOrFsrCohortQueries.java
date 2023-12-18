package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.definition.library.DocumentedDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TxPvlsBySourceLabOrFsrCohortQueries {

  private TxPvlsCohortQueries txPvlsCohortQueries;

  private HivMetadata hivMetadata;

  @Autowired
  public TxPvlsBySourceLabOrFsrCohortQueries(
      TxPvlsCohortQueries txPvlsCohortQueries, HivMetadata hivMetadata) {
    this.txPvlsCohortQueries = txPvlsCohortQueries;
    this.hivMetadata = hivMetadata;
  }

  /**
   * Adult and pediatric patients on ART with suppressed viral load results (<1,000 copies/ml)
   * documented in the medical records and /or supporting laboratory results within the past 12
   * months based on lab and fsr
   *
   * @return CohortDefinition
   */
  @DocumentedDefinition(value = "suppressedViralLoadWithin12MonthsForLabAndFsrNumerator")
  public CohortDefinition getPatientsWithSuppressedViralLoadWithin12MonthsForLabAndFsrNumerator() {
    SqlCohortDefinition sql = new SqlCohortDefinition();
    sql.setName("suppressedViralLoadWithin12MonthsForLabAndFsrNumerator");
    sql.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sql.addParameter(new Parameter("endDate", "End Date", Date.class));
    sql.addParameter(new Parameter("location", "Location", Location.class));
    sql.setQuery(
        TxPvlsBySourceQueries.getPatientsWithViralLoadSuppressionForLabAndFsrNumerator(
            hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId(),
            hivMetadata.getFsrEncounterType().getEncounterTypeId(),
            hivMetadata.getHivViralLoadConcept().getConceptId(),
            hivMetadata.getHivViralLoadQualitative().getConceptId()));
    return sql;
  }

  /**
   * <b>Description:</b> Number of adult and pediatric ART patients with a viral load result
   * documented in the patient medical record and/ or laboratory records in the past 12 months.
   * Based on Lab and fsr
   *
   * @return {@link CohortDefinition}
   */
  @DocumentedDefinition(value = "viralLoadWithin12MonthsForLabAndFsrDenominator")
  public CohortDefinition getPatientsViralLoadWithin12MonthsForLabAndFsrDenominator() {
    SqlCohortDefinition sql = new SqlCohortDefinition();
    sql.setName("viralLoadWithin12MonthsForLabAndFsrDenominator");
    sql.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sql.addParameter(new Parameter("endDate", "End Date", Date.class));
    sql.addParameter(new Parameter("location", "Location", Location.class));
    sql.setQuery(
        TxPvlsBySourceQueries.getPatientsHavingViralLoadInLast12MonthsForLabAndFsrDenominator(
            hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId(),
            hivMetadata.getFsrEncounterType().getEncounterTypeId(),
            hivMetadata.getHivViralLoadConcept().getConceptId(),
            hivMetadata.getHivViralLoadQualitative().getConceptId()));
    return sql;
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
  public CohortDefinition
      getPatientsWithViralLoadSuppressionForLabAndFsrNumeratorWhoAreOnArtMoreThan3Months() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";
    cd.addSearch(
        "supp",
        EptsReportUtils.map(
            getPatientsWithSuppressedViralLoadWithin12MonthsForLabAndFsrNumerator(), mappings));
    cd.addSearch(
        "onArtLongEnough",
        EptsReportUtils.map(
            txPvlsCohortQueries.getPatientsWhoAreMoreThan3MonthsOnArt(
                Arrays.asList(
                    hivMetadata.getMisauLaboratorioEncounterType(),
                    hivMetadata.getFsrEncounterType())),
            "endDate=${endDate},location=${location}"));
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
  public CohortDefinition
      getPatientsViralLoadWithin12MonthsForLabAndFsrDenominatorAndOnArtForMoreThan3Months() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";
    cd.addSearch(
        "results",
        EptsReportUtils.map(getPatientsViralLoadWithin12MonthsForLabAndFsrDenominator(), mappings));
    cd.addSearch(
        "onArtLongEnough",
        EptsReportUtils.map(
            txPvlsCohortQueries.getPatientsWhoAreMoreThan3MonthsOnArt(
                Arrays.asList(
                    hivMetadata.getMisauLaboratorioEncounterType(),
                    hivMetadata.getFsrEncounterType())),
            "endDate=${endDate},location=${location}"));
    cd.setCompositionString("results AND onArtLongEnough");
    return cd;
  }

  /**
   * <b>Description</b>Breast feeding women with viral load suppression and on ART for more than 3
   * months
   *
   * @return CohortDefinition
   */
  @DocumentedDefinition(value = "breastfeedingWomenWithViralSuppressionForLabAndFsrNumerator")
  public CohortDefinition getBreastfeedingWomenWithViralSuppressionForLabAndFsrNumerator() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Breastfeeding with viral suppression for Lab and FSR Numerator");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "breastfeeding",
        EptsReportUtils.map(
            getBreastfeedingPatientsForLabViralLoadResults(),
            "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "suppression",
        EptsReportUtils.map(
            getPatientsWithViralLoadSuppressionForLabAndFsrNumeratorWhoAreOnArtMoreThan3Months(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.setCompositionString("breastfeeding AND suppression");

    return cd;
  }

  /**
   * <b>Description</b> Breast feeding women with viral load suppression
   *
   * @return CohortDefinition
   */
  @DocumentedDefinition(value = "breastfeedingWomenWithViralLoadResultsForLabAndFsrDenominator")
  public CohortDefinition getBreastfeedingWomenWhoHaveViralLoadResultsForLabAndFsrDenominator() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Breastfeeding with viral results");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "breastfeeding",
        EptsReportUtils.map(
            getBreastfeedingPatientsForLabViralLoadResults(),
            "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "results",
        EptsReportUtils.map(
            getPatientsViralLoadWithin12MonthsForLabAndFsrDenominatorAndOnArtForMoreThan3Months(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.setCompositionString("breastfeeding AND results");

    return cd;
  }

  /**
   * <b>Description</b> Get pregnant women Numerator
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPregnantWomenWithViralLoadSuppressionForLabAndFsrNumerator() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Get pregnant women with viral load suppression for lab and fsr");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";
    cd.addSearch(
        "suppression",
        EptsReportUtils.map(
            getPatientsWithViralLoadSuppressionForLabAndFsrNumeratorWhoAreOnArtMoreThan3Months(),
            mappings));
    cd.addSearch(
        "pregnant",
        EptsReportUtils.map(
            getPregnantWomanWithLaboratoryVLResult(), "endDate=${endDate},location=${location}"));
    cd.setCompositionString("suppression AND pregnant");
    return cd;
  }
  /**
   * <b>Description</b>Get pregnant women Denominator
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPregnantWomenWithViralLoadResultsForLabAndFsrDenominator() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Get pregnant women with viral load results denominator for Lab and fsr only");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";
    cd.addSearch(
        "results",
        EptsReportUtils.map(
            getPatientsViralLoadWithin12MonthsForLabAndFsrDenominatorAndOnArtForMoreThan3Months(),
            mappings));
    cd.addSearch(
        "pregnant",
        EptsReportUtils.map(
            getPregnantWomanWithLaboratoryVLResult(), "endDate=${endDate},location=${location}"));
    cd.setCompositionString("results AND pregnant");
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
  public CohortDefinition getPatientsWithViralLoadResultsAndOnRoutineForLabAndFsrDenominator() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";
    cd.addSearch(
        "results",
        EptsReportUtils.map(getPatientsViralLoadWithin12MonthsForLabAndFsrDenominator(), mappings));
    cd.addSearch(
        "onArtLongEnough",
        EptsReportUtils.map(
            txPvlsCohortQueries.getPatientsWhoAreMoreThan3MonthsOnArt(
                Arrays.asList(
                    hivMetadata.getMisauLaboratorioEncounterType(),
                    hivMetadata.getFsrEncounterType())),
            "endDate=${endDate},location=${location}"));
    cd.addSearch("RoutineByLab", EptsReportUtils.map(getRoutineByLab(), mappings));
    cd.addSearch("RoutineByFsr", EptsReportUtils.map(getRoutineByFsr(), mappings));
    cd.setCompositionString("(results AND onArtLongEnough) AND (RoutineByLab OR RoutineByFsr)");
    return cd;
  }

  private CohortDefinition getRoutineByFsr() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.setQuery(
        TxPvlsBySourceQueries.getPatientsHavingRoutineViralLoadTestsUsingFsr(
            hivMetadata.getFsrEncounterType().getEncounterTypeId(),
            hivMetadata.getReasonForRequestingViralLoadConcept().getConceptId(),
            hivMetadata.getRoutineForRequestingViralLoadConcept().getConceptId(),
            hivMetadata.getUnkownConcept().getConceptId(),
            hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId(),
            hivMetadata.getHivViralLoadConcept().getConceptId(),
            hivMetadata.getHivViralLoadQualitative().getConceptId()));
    return cd;
  }

  private CohortDefinition getRoutineByLab() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.setQuery(
        TxPvlsBySourceQueries.getPatientsHavingRoutineViralLoadTestsUsinglab(
            hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId(),
            hivMetadata.getHivViralLoadConcept().getConceptId(),
            hivMetadata.getHivViralLoadQualitative().getConceptId(),
            hivMetadata.getFsrEncounterType().getEncounterTypeId()));
    return cd;
  }

  public CohortDefinition getPatientsOnTargetByFsr() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Get patients on target using FSR encounter type");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.setQuery(
        TxPvlsBySourceQueries.getPatientsOnTargetWithViralLoadTestsUsingFsr(
            hivMetadata.getFsrEncounterType().getEncounterTypeId(),
            hivMetadata.getReasonForRequestingViralLoadConcept().getConceptId(),
            hivMetadata.getRoutineForRequestingViralLoadConcept().getConceptId(),
            hivMetadata.getUnkownConcept().getConceptId(),
            hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId(),
            hivMetadata.getHivViralLoadConcept().getConceptId(),
            hivMetadata.getHivViralLoadQualitative().getConceptId()));
    return cd;
  }

  /**
   * <b>Description</b> Get patients who are on target Composition
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoAreOnTargetForFsrDenominator() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("All patients on Target based on FSR form");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addSearch(
        "results",
        EptsReportUtils.map(
            getPatientsViralLoadWithin12MonthsForLabAndFsrDenominator(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "onArtLongEnough",
        EptsReportUtils.map(
            txPvlsCohortQueries.getPatientsWhoAreMoreThan3MonthsOnArt(
                Arrays.asList(
                    hivMetadata.getMisauLaboratorioEncounterType(),
                    hivMetadata.getFsrEncounterType())),
            "endDate=${endDate},location=${location}"));
    cd.addSearch(
        "target",
        EptsReportUtils.map(
            getPatientsOnTargetByFsr(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.setCompositionString("(results AND onArtLongEnough) AND target");
    return cd;
  }

  /**
   * <b>Description</b>Get patients having viral load suppression and routine for adults and
   * children - Numerator
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientWithViralSuppressionAndOnRoutineForLabAndFsrNumerator() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Suppression and on routine adult and children ForLabAndFsrNumerator");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";
    cd.addSearch(
        "supp",
        EptsReportUtils.map(
            getPatientsWithViralLoadSuppressionForLabAndFsrNumeratorWhoAreOnArtMoreThan3Months(),
            mappings));

    cd.addSearch("RoutineByLab", EptsReportUtils.map(getRoutineByLab(), mappings));
    cd.addSearch("RoutineByFsr", EptsReportUtils.map(getRoutineByFsr(), mappings));
    cd.setCompositionString("supp AND (RoutineByLab OR RoutineByFsr)");
    return cd;
  }

  /**
   * <b>Description</b>Get patients having viral load suppression and target for adults and children
   * - Numerator
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientWithViralSuppressionAndOnTargetForLabAndFsrNumerator() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Suppression and on target adult and children ForLabAndFsrNumerator");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";
    cd.addSearch(
        "supp",
        EptsReportUtils.map(
            getPatientsWithViralLoadSuppressionForLabAndFsrNumeratorWhoAreOnArtMoreThan3Months(),
            mappings));

    cd.addSearch(
        "onArtLongEnough",
        EptsReportUtils.map(
            txPvlsCohortQueries.getPatientsWhoAreMoreThan3MonthsOnArt(
                Arrays.asList(
                    hivMetadata.getMisauLaboratorioEncounterType(),
                    hivMetadata.getFsrEncounterType())),
            "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "target",
        EptsReportUtils.map(
            getPatientsOnTargetByFsr(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("(supp AND onArtLongEnough) AND target");
    return cd;
  }

  public CohortDefinition getRoutineDisaggregationForPvlsBySource() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";

    cd.addSearch("RoutineByLab", EptsReportUtils.map(getRoutineByLab(), mappings));

    cd.addSearch("RoutineByFsr", EptsReportUtils.map(getRoutineByFsr(), mappings));

    cd.setCompositionString("RoutineByLab OR RoutineByFsr");
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
  public CohortDefinition getPregnantWomanWithLaboratoryVLResult() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        " Patients disaggregation - Pregnant with Clinical OR Ficah Resumo VL Result");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));
    Map<String, Integer> map = new HashMap<>();

    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("5", hivMetadata.getARVAdultInitialEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    map.put("1600", hivMetadata.getPregnancyDueDate().getConceptId());
    map.put("23821", hivMetadata.getSampleCollectionDateAndTime().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("6334", hivMetadata.getCriteriaForArtStart().getConceptId());
    map.put("8", hivMetadata.getPtvEtvProgram().getProgramId());
    map.put("1279", hivMetadata.getNumberOfWeeksPregnant().getConceptId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("6331", hivMetadata.getBPlusConcept().getConceptId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("5599", hivMetadata.getPriorDeliveryDateConcept().getConceptId());
    map.put("6332", hivMetadata.getBreastfeeding().getConceptId());
    map.put("27", hivMetadata.getPatientGaveBirthWorkflowState().getProgramWorkflowStateId());

    String query =
        "SELECT pregnant.patient_id "
            + "             FROM  (SELECT vl.patient_id, "
            + "                           vl.last_date    AS last_vl, "
            + "                           Max(pg.pregnancy_date) AS pg_date "
            + "                    FROM   (SELECT vl.patient_id, "
            + "                                   Max(vl.last_date) AS last_date "
            + "                            FROM   (SELECT p.patient_id, "
            + "										   e.encounter_datetime AS last_date "
            + "                                    FROM   patient p "
            + "                                               INNER JOIN encounter e "
            + "                                                          ON e.patient_id = p.patient_id "
            + "                                               INNER JOIN obs o "
            + "                                                          ON o.encounter_id = e.encounter_id "
            + "                                    WHERE  ( ( e.encounter_type IN (${13}, ${51}) "
            + "                                        AND e.encounter_datetime BETWEEN "
            + "                                                   Timestampadd(month, -12, :endDate) "
            + "                                                   AND "
            + "                                                   :endDate ) ) "
            + "                                      AND ( ( o.concept_id = ${856} "
            + "                                        AND o.value_numeric IS NOT NULL ) "
            + "                                        OR ( o.concept_id = ${1305} "
            + "                                            AND o.value_coded IS NOT NULL ) ) "
            + "                                      AND e.voided = 0 "
            + "                                      AND p.voided = 0 "
            + "                                      AND o.voided = 0) vl "
            + "                            GROUP  BY vl.patient_id) vl "
            + "                               INNER JOIN (SELECT p.patient_id, "
            + "                                                  e.encounter_datetime AS pregnancy_date "
            + "                                           FROM   patient p "
            + "                                                      INNER JOIN person p2 "
            + "                                                                 ON p2.person_id = p.patient_id "
            + "                                                      INNER JOIN encounter e "
            + "                                                                 ON e.patient_id = p.patient_id "
            + "                                                      INNER JOIN obs o "
            + "                                                                 ON o.encounter_id = e.encounter_id "
            + "                                           WHERE  e.encounter_type IN ( ${5}, ${6} ) "
            + "                                             AND p2.gender = 'F' "
            + "                                             AND ( o.concept_id =${1982} "
            + "                                               AND o.value_coded =${1065} ) "
            + "                                             AND e.location_id = :location "
            + "                                             AND p.voided = 0 "
            + "                                             AND e.encounter_datetime <= :endDate "
            + "                                             AND p2.voided = 0 "
            + "                                             AND e.voided = 0 "
            + "                                             AND o.voided = 0 "
            + "                                           UNION "
            + "                                           SELECT p.patient_id, "
            + "                                                  e.encounter_datetime AS pregnancy_date "
            + "                                           FROM   patient p "
            + "                                                      INNER JOIN person p2 "
            + "                                                                 ON p2.person_id = p.patient_id "
            + "                                                      INNER JOIN encounter e "
            + "                                                                 ON e.patient_id = p.patient_id "
            + "                                                      INNER JOIN obs o "
            + "                                                                 ON o.encounter_id = e.encounter_id "
            + "                                           WHERE  p2.gender = 'F' "
            + "                                             AND e.encounter_type IN ( ${5}, ${6} ) "
            + "                                             AND o.concept_id = ${1279} "
            + "                                             AND e.location_id = :location "
            + "                                             AND p.voided = 0 "
            + "                                             AND p2.voided = 0 "
            + "                                             AND e.voided = 0 "
            + "                                             AND o.voided = 0 "
            + "                                             AND e.encounter_datetime <= :endDate "
            + "                                           UNION "
            + "                                           SELECT p.patient_id, "
            + "                                                  e.encounter_datetime AS pregnancy_date "
            + "                                           FROM   patient p "
            + "                                                      INNER JOIN person p2 "
            + "                                                                 ON p2.person_id = p.patient_id "
            + "                                                      INNER JOIN encounter e "
            + "                                                                 ON e.patient_id = p.patient_id "
            + "                                                      INNER JOIN obs o "
            + "                                                                 ON o.encounter_id = e.encounter_id "
            + "                                           WHERE  p2.gender = 'F' "
            + "                                             AND e.encounter_type IN ( ${5}, ${6} ) "
            + "                                             AND o.concept_id = ${1600} "
            + "                                             AND e.location_id = :location "
            + "                                             AND p.voided = 0 "
            + "                                             AND p2.voided = 0 "
            + "                                             AND e.voided = 0 "
            + "                                             AND o.voided = 0 "
            + "                                             AND e.encounter_datetime <= :endDate "
            + "                                           UNION "
            + "                                           SELECT p.patient_id, "
            + "                                                  e.encounter_datetime AS pregnancy_date "
            + "                                           FROM   patient p "
            + "                                                      INNER JOIN person p2 "
            + "                                                                 ON p2.person_id = p.patient_id "
            + "                                                      INNER JOIN encounter e "
            + "                                                                 ON e.patient_id = p.patient_id "
            + "                                                      INNER JOIN obs o "
            + "                                                                 ON o.encounter_id = e.encounter_id "
            + "                                           WHERE  p2.gender = 'F' "
            + "                                             AND e.encounter_type = ${6} "
            + "                                             AND ( o.concept_id = ${6334} "
            + "                                               AND o.value_coded = ${6331} ) "
            + "                                             AND e.location_id = :location "
            + "                                             AND p.voided = 0 "
            + "                                             AND p2.voided = 0 "
            + "                                             AND e.voided = 0 "
            + "                                             AND o.voided = 0 "
            + "                                             AND e.encounter_datetime <= :endDate "
            + "                                           UNION "
            + "                                           SELECT pp.patient_id, "
            + "                                                  pp.date_enrolled AS pregnancy_date "
            + "                                           FROM   patient_program pp "
            + "                                                      INNER JOIN person p "
            + "                                                                 ON p.person_id = pp.patient_id "
            + "                                                      INNER JOIN encounter e "
            + "                                                                 ON e.patient_id = pp.patient_id "
            + "                                           WHERE  p.gender = 'F' "
            + "                                             AND pp.program_id =${8} "
            + "                                             AND e.location_id = :location "
            + "                                             AND p.voided = 0 "
            + "                                             AND pp.voided = 0 "
            + "                                             AND pp.date_enrolled <= :endDate "
            + "                                           UNION "
            + "                                           SELECT p.patient_id, "
            + "                                                  o2.value_datetime AS pregnancy_date "
            + "                                           FROM   patient p "
            + "                                                      INNER JOIN person p2 "
            + "                                                                 ON p2.person_id = p.patient_id "
            + "                                                      INNER JOIN encounter e "
            + "                                                                 ON e.patient_id = p.patient_id "
            + "                                                      INNER JOIN obs o "
            + "                                                                 ON o.encounter_id = e.encounter_id "
            + "                                                      INNER JOIN obs o2 "
            + "                                                                 ON o2.encounter_id = e.encounter_id "
            + "                                           WHERE  p2.gender = 'F' "
            + "                                             AND e.encounter_type =${53} "
            + "                                             AND ( o.concept_id = ${1982} "
            + "                                               AND o.value_coded =${1065} ) "
            + "                                             AND ( o2.concept_id = ${1190} "
            + "                                               AND o2.value_datetime <= :endDate ) "
            + "                                             AND e.location_id = :location "
            + "                                             AND p.voided = 0 "
            + "                                             AND p2.voided = 0 "
            + "                                             AND e.voided = 0 "
            + "                                             AND o.voided = 0 "
            + "                                             AND o2.voided = 0 "
            + "                                           UNION "
            + "                                           SELECT p.patient_id, "
            + "                                                  e.encounter_datetime AS pregnancy_date "
            + "                                           FROM   patient p "
            + "                                                      INNER JOIN person p2 "
            + "                                                                 ON p2.person_id = p.patient_id "
            + "                                                      INNER JOIN encounter e "
            + "                                                                 ON e.patient_id = p.patient_id "
            + "                                                      INNER JOIN obs o "
            + "                                                                 ON o.encounter_id = e.encounter_id "
            + "                                           WHERE  p2.gender = 'F' "
            + "                                             AND e.encounter_type = ${6} "
            + "                                             AND ( o.concept_id =${1982} "
            + "                                               AND o.value_coded =${1065} ) "
            + "                                             AND e.location_id = :location "
            + "                                             AND p.voided = 0 "
            + "                                             AND p2.voided = 0 "
            + "                                             AND e.voided = 0 "
            + "                                             AND o.voided = 0 "
            + "                                             AND e.encounter_datetime <= :endDate "
            + "                                           UNION "
            + "                                           SELECT p.patient_id, "
            + "                                                  CAST(o2.value_datetime AS DATE) AS pregnancy_date "
            + "                                           FROM   patient p "
            + "                                                      INNER JOIN person p2 "
            + "                                                                 ON p2.person_id = p.patient_id "
            + "                                                      INNER JOIN encounter e "
            + "                                                                 ON e.patient_id = p.patient_id "
            + "                                                      INNER JOIN obs o "
            + "                                                                 ON o.encounter_id = e.encounter_id "
            + "                                                      INNER JOIN obs o2 "
            + "                                                                 ON o2.encounter_id = e.encounter_id "
            + "                                           WHERE  p2.gender = 'F' "
            + "                                             AND e.encounter_type =${51} "
            + "                                             AND ( o.concept_id =${1982} "
            + "                                               AND o.value_coded =${1065} ) "
            + "                                             AND ( o2.concept_id = ${23821} "
            + "                                               AND CAST(o2.value_datetime as date) <= :endDate ) "
            + "                                             AND e.location_id = :location "
            + "                                             AND p.voided = 0 "
            + "                                             AND p2.voided = 0 "
            + "                                             AND e.voided = 0 "
            + "                                             AND o.voided = 0 "
            + "                                             AND o2.voided = 0) pg "
            + "                                          ON vl.patient_id = pg.patient_id "
            + "                    WHERE  pg.pregnancy_date BETWEEN Timestampadd(month, -9, vl.last_date) "
            + "                               AND "
            + "                               vl.last_date "
            + "                    GROUP  BY vl.patient_id) pregnant "
            + "             WHERE  NOT EXISTS (SELECT e.patient_id "
            + "                                FROM   encounter e "
            + "                                           INNER JOIN obs o "
            + "                                                      ON o.encounter_id = e.encounter_id "
            + "                                WHERE  e.encounter_type IN ( ${5}, ${6} ) "
            + "                                  AND ( o.concept_id = ${5599} "
            + "                                    AND o.value_datetime BETWEEN "
            + "                                            Timestampadd(month, -18, pregnant.last_vl) "
            + "                                            AND "
            + "                                            pregnant.last_vl ) "
            + "                                  AND e.location_id = :location "
            + "                                  AND pregnant.patient_id = e.patient_id "
            + "                                  AND o.value_datetime > pregnant.pg_date "
            + "                                  AND e.voided = 0 "
            + "                                  AND o.voided = 0 "
            + "                                UNION "
            + "                                SELECT e.patient_id "
            + "                                FROM   encounter e "
            + "                                           INNER JOIN obs o "
            + "                                                      ON o.encounter_id = e.encounter_id "
            + "                                WHERE  e.encounter_type = ${6} "
            + "                                  AND e.location_id = :location "
            + "                                  AND ( o.concept_id =${6332} "
            + "                                    AND o.value_coded =${1065} ) "
            + "                                  AND pregnant.patient_id = e.patient_id "
            + "                                  AND e.encounter_datetime > pregnant.pg_date "
            + "                                  AND e.encounter_datetime BETWEEN "
            + "                                    Timestampadd(month, -18, pregnant.last_vl) AND "
            + "                                    pregnant.last_vl "
            + "                                  AND o.voided = 0 "
            + "                                  AND e.voided = 0 "
            + "                                UNION "
            + "                                SELECT e.patient_id "
            + "                                FROM   encounter e "
            + "                                           INNER JOIN obs o "
            + "                                                      ON o.encounter_id = e.encounter_id "
            + "                                WHERE  e.encounter_type IN ( ${5}, ${6} ) "
            + "                                  AND o.concept_id = ${6334} "
            + "                                  AND o.value_coded =${6332} "
            + "                                  AND e.voided = 0 "
            + "                                  AND o.voided = 0 "
            + "                                  AND e.location_id = :location "
            + "                                  AND pregnant.patient_id = e.patient_id "
            + "                                  AND e.encounter_datetime > pregnant.pg_date "
            + "                                  AND e.encounter_datetime BETWEEN "
            + "                                    Timestampadd(month, -18, pregnant.last_vl) AND "
            + "                                    pregnant.last_vl "
            + "                                UNION "
            + "                                SELECT pp.patient_id "
            + "                                FROM   patient_program pp "
            + "                                           INNER JOIN patient_state ps "
            + "                                                      ON ps.patient_program_id = "
            + "                                                         pp.patient_program_id "
            + "                                WHERE  pp.program_id =${8} "
            + "                                  AND ps.state =${27} "
            + "                                  AND pp.location_id = :location "
            + "                                  AND pp.voided = 0 "
            + "                                  AND ps.voided = 0 "
            + "                                  AND pregnant.patient_id = pp.patient_id "
            + "                                  AND ps.start_date > pregnant.pg_date "
            + "                                  AND ps.start_date BETWEEN Timestampadd(month, -18 "
            + "                                    , pregnant.last_vl) AND "
            + "                                    pregnant.last_vl "
            + "                                UNION "
            + "                                SELECT e.patient_id "
            + "                                FROM   encounter e "
            + "                                           INNER JOIN obs o "
            + "                                                      ON o.encounter_id = e.encounter_id "
            + "                                WHERE  e.encounter_type =${53} "
            + "                                  AND o.concept_id =${6332} "
            + "                                  AND o.value_coded =${1065} "
            + "                                  AND e.location_id = :location "
            + "                                  AND pregnant.patient_id = e.patient_id "
            + "                                  AND o.obs_datetime > pregnant.pg_date "
            + "                                  AND o.obs_datetime BETWEEN Timestampadd(month, -18 "
            + "                                    , pregnant.last_vl) "
            + "                                    AND "
            + "                                    pregnant.last_vl "
            + "                                  AND e.voided = 0 "
            + "                                  AND o.voided = 0 "
            + "                                UNION "
            + "                                SELECT e.patient_id "
            + "                                FROM   encounter e "
            + "                                           INNER JOIN obs o "
            + "                                                      ON o.encounter_id = e.encounter_id "
            + "                                           INNER JOIN obs o2 "
            + "                                                      ON o2.encounter_id = e.encounter_id "
            + "                                WHERE  e.encounter_type =${51} "
            + "                                  AND o.concept_id =${6332} "
            + "                                  AND o.value_coded =${1065} "
            + "                                  AND o2.concept_id = ${23821} "
            + "                                  AND CAST(o2.value_datetime AS DATE) BETWEEN "
            + "                                    Timestampadd(month, -18, pregnant.last_vl) "
            + "                                    AND "
            + "                                    pregnant.last_vl "
            + "                                  AND pregnant.patient_id = e.patient_id "
            + "                                  AND CAST(o2.value_datetime as DATE) > pregnant.pg_date "
            + "                                  AND e.voided = 0 "
            + "                                  AND o.voided = 0 "
            + "                                  AND o2.voided = 0 "
            + "                                  AND e.location_id =:location);";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    String mappedQuery = stringSubstitutor.replace(query);
    sqlCohortDefinition.setQuery(mappedQuery);
    return sqlCohortDefinition;
  }

  /**
   * Patients disaggregation - breastfeeding
   *
   * <p>The system will identify female patients who are breastfeeding as following:
   *
   * <ul>
   *   <li>Patients who have the “Delivery date” registered in the initial or follow-up
   *       consultations (Processo Clinico Parte A or Ficha de Seguimento Adulto) and where the
   *       delivery date is during the period range or
   *   <li>Patients who started ART for being breastfeeding as specified in “CRITÉRIO PARA INÍCIO DE
   *       TRATAMENTO ARV” in the initial or follow-up consultations (Processo Clinico Parte A or
   *       Ficha de Seguimento Adulto) that occurred during the period range or
   *   <li>Patients who have been registered as breastfeeding in follow up consultation (Ficha de
   *       Seguimento Adulto) within the period range.
   *   <li>Patients registered as breastfeeding in Ficha Resumo or Ficha Clinica during the period
   *       range or
   *   <li>Patients enrolled on Prevention of the Vertical Transmission/Elimination of the Vertical
   *       Transmission (PTV/ETV) program with state 27 (gave birth) during the period range or
   *   <li>Patients who have “Actualmente está a amamentar” marked as “Sim” on e-Lab Form and Data
   *       de Colheita is during the period range.
   * </ul>
   *
   * <p>If the patient has both states (pregnant and breastfeeding), the most recent state should be
   * considered. For patients who have both states (pregnant and breastfeeding) marked on the same
   * date, the system will consider the patient as pregnant.
   *
   * <ul>
   *   <li>Period range:
   *       <ul>
   *         <li>start_date = the most recent VL result date - 18 months
   *         <li>end_date = the most recent VL result date
   *       </ul>
   * </ul>
   */
  public CohortDefinition getBreastfeedingPatientsForLabViralLoadResults() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" Patients disaggregation - breastfeeding for Lab VL");
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
        "SELECT breastfeeding.patient_id "
            + "FROM   ( "
            + "                  SELECT     vl.patient_id, "
            + "                             Max(vl.last_date)          AS last_vl, "
            + "                             Max(bf.breastfeeding_date) AS bf_date "
            + "                  FROM       ( "
            + "                                      SELECT   vl.patient_id, "
            + "                                               Max(vl.last_date) AS last_date "
            + "                                      FROM     ( "
            + "                                                          SELECT     p.patient_id, "
            + "                                                                     e.encounter_datetime AS last_date "
            + "                                                          FROM       patient p "
            + "                                                          INNER JOIN encounter e "
            + "                                                          ON         e.patient_id = p.patient_id "
            + "                                                          INNER JOIN obs o "
            + "                                                          ON         o.encounter_id = e.encounter_id "
            + "                                                          WHERE      ( ( "
            + "                                                                                           e.encounter_type IN (${13}, "
            + "                                                                                                                ${51}) "
            + "                                                                                AND        e.encounter_datetime BETWEEN timestampadd(month, -12, :endDate) AND        :endDate) ) "
            + "                                                          AND        ( ( "
            + "                                                                                           o.concept_id = ${856} "
            + "                                                                                AND        o.value_numeric IS NOT NULL) "
            + "                                                                     OR         ( "
            + "                                                                                           o.concept_id = ${1305} "
            + "                                                                                AND        o.value_coded IS NOT NULL) ) "
            + "                                                          AND        e.voided = 0 "
            + "                                                          AND        p.voided = 0 "
            + "                                                          AND        o.voided = 0 ) vl "
            + "                                      GROUP BY vl.patient_id) vl "
            + "                  INNER JOIN "
            + "                             ( "
            + "                                    SELECT lactantes.patient_id, "
            + "                                           lactantes.last_date AS breastfeeding_date "
            + "                                    FROM   ( "
            + "                                                      SELECT     p.patient_id, "
            + "                                                                 o.value_datetime AS last_date "
            + "                                                      FROM       patient p "
            + "                                                      INNER JOIN person p2 "
            + "                                                      ON         p2.person_id = p.patient_id "
            + "                                                      INNER JOIN encounter e "
            + "                                                      ON         e.patient_id = p.patient_id "
            + "                                                      INNER JOIN obs o "
            + "                                                      ON         o.encounter_id = e.encounter_id "
            + "                                                      WHERE      p2.gender = 'F' "
            + "                                                      AND        e.encounter_type IN ( ${5}, "
            + "                                                                                      ${6} ) "
            + "                                                      AND        e.location_id = :location "
            + "                                                      AND        o.concept_id = ${5599} "
            + "                                                      AND        o.value_datetime <= :endDate "
            + "                                                      AND        o.voided = 0 "
            + "                                                      AND        p.voided = 0 "
            + "                                                      AND        e.voided = 0 "
            + "                                                      AND        p2.voided = 0 "
            + "                                                      UNION "
            + "                                                      SELECT     p.patient_id, "
            + "                                                                 e.encounter_datetime AS last_date "
            + "                                                      FROM       patient p "
            + "                                                      INNER JOIN person p2 "
            + "                                                      ON         p2.person_id = p.patient_id "
            + "                                                      INNER JOIN encounter e "
            + "                                                      ON         e.patient_id = p.patient_id "
            + "                                                      INNER JOIN obs o "
            + "                                                      ON         o.encounter_id = e.encounter_id "
            + "                                                      WHERE      p2.gender = 'F' "
            + "                                                      AND        e.encounter_type = ${6} "
            + "                                                      AND        e.location_id = :location "
            + "                                                      AND        o.concept_id = ${6332} "
            + "                                                      AND        o.value_coded = ${1065} "
            + "                                                      AND        e.encounter_datetime <= :endDate "
            + "                                                      AND        o.voided = 0 "
            + "                                                      AND        e.voided = 0 "
            + "                                                      AND        p.voided = 0 "
            + "                                                      AND        p2.voided = 0 "
            + "                                                      UNION "
            + "                                                      SELECT     p.patient_id, "
            + "                                                                 e.encounter_datetime AS last_date "
            + "                                                      FROM       patient p "
            + "                                                      INNER JOIN person p2 "
            + "                                                      ON         p2.person_id = p.patient_id "
            + "                                                      INNER JOIN encounter e "
            + "                                                      ON         e.patient_id = p.patient_id "
            + "                                                      INNER JOIN obs o "
            + "                                                      ON         o.encounter_id = e.encounter_id "
            + "                                                      WHERE      p2.gender = 'F' "
            + "                                                      AND        e.encounter_type IN ( ${5}, "
            + "                                                                                      ${6} ) "
            + "                                                      AND        o.concept_id = ${6334} "
            + "                                                      AND        o.value_coded = ${6332} "
            + "                                                      AND        p.voided = 0 "
            + "                                                      AND        e.voided = 0 "
            + "                                                      AND        o.voided = 0 "
            + "                                                      AND        p2.voided = 0 "
            + "                                                      AND        e.location_id = :location "
            + "                                                      AND        e.encounter_datetime <= :endDate "
            + "                                                      UNION "
            + "                                                      SELECT     pp.patient_id, "
            + "                                                                 ps.start_date AS last_date "
            + "                                                      FROM       patient_program pp "
            + "                                                      INNER JOIN person p "
            + "                                                      ON         p.person_id = pp.patient_id "
            + "                                                      INNER JOIN patient_state ps "
            + "                                                      ON         ps.patient_program_id = pp.patient_program_id "
            + "                                                      WHERE      p.gender = 'F' "
            + "                                                      AND        pp.program_id = ${8} "
            + "                                                      AND        ps.state = ${27} "
            + "                                                      AND        pp.location_id = :location "
            + "                                                      AND        pp.voided = 0 "
            + "                                                      AND        ps.voided = 0 "
            + "                                                      AND        p.voided = 0 "
            + "                                                      AND        ps.start_date <= :endDate "
            + "                                                      UNION "
            + "                                                      SELECT     p.patient_id, "
            + "                                                                 hist.value_datetime AS last_date "
            + "                                                      FROM       patient p "
            + "                                                      INNER JOIN person pe "
            + "                                                      ON         p.patient_id=pe.person_id "
            + "                                                      INNER JOIN encounter e "
            + "                                                      ON         p.patient_id=e.patient_id "
            + "                                                      INNER JOIN obs o "
            + "                                                      ON         e.encounter_id=o.encounter_id "
            + "                                                      INNER JOIN obs hist "
            + "                                                      ON         e.encounter_id=hist.encounter_id "
            + "                                                      WHERE      p.voided = 0 "
            + "                                                      AND        e.voided = 0 "
            + "                                                      AND        o.voided = 0 "
            + "                                                      AND        pe.voided = 0 "
            + "                                                      AND        hist.voided=0 "
            + "                                                      AND        o.concept_id = ${6332} "
            + "                                                      AND        o.value_coded = ${1065} "
            + "                                                      AND        e.encounter_type = ${53} "
            + "                                                      AND        hist.concept_id = ${1190} "
            + "                                                      AND        hist.value_datetime <= :endDate "
            + "                                                      UNION "
            + "                                                      SELECT     p.patient_id, "
            + "                                                                 cast(o2.value_datetime AS date) AS last_date "
            + "                                                      FROM       patient p "
            + "                                                      INNER JOIN encounter e "
            + "                                                      ON         e.patient_id = p.patient_id "
            + "                                                      INNER JOIN person p2 "
            + "                                                      ON         p2.person_id=p.patient_id "
            + "                                                      INNER JOIN obs o "
            + "                                                      ON         o.encounter_id = e.encounter_id "
            + "                                                      INNER JOIN obs o2 "
            + "                                                      ON         o2.encounter_id = e.encounter_id "
            + "                                                      WHERE      e.encounter_type = ${51} "
            + "                                                      AND        o.concept_id = ${6332} "
            + "                                                      AND        o.value_coded = ${1065} "
            + "                                                      AND        o2.concept_id = ${23821} "
            + "                                                      AND        cast(o2.value_datetime AS date) <= :endDate "
            + "                                                      AND        p2.gender = 'F' "
            + "                                                      AND        p.voided = 0 "
            + "                                                      AND        e.voided = 0 "
            + "                                                      AND        o.voided = 0 "
            + "                                                      AND        o2.voided = 0 "
            + "                                                      AND        e.location_id = :location )lactantes ) bf "
            + "                  ON         vl.patient_id=bf.patient_id "
            + "                  WHERE      bf.breastfeeding_date BETWEEN timestampadd(month, -18, vl.last_date) AND        vl.last_date "
            + "                  GROUP BY   vl.patient_id ) breastfeeding "
            + "WHERE  NOT EXISTS "
            + "       ( "
            + "                  SELECT     e.patient_id "
            + "                  FROM       encounter e "
            + "                  INNER JOIN obs o "
            + "                  ON         o.encounter_id = e.encounter_id "
            + "                  WHERE      e.encounter_type IN ( ${5}, "
            + "                                                  ${6} ) "
            + "                  AND        ( "
            + "                                        o.concept_id = ${1982} "
            + "                             AND        o.value_coded = ${1065} ) "
            + "                  AND        e.location_id = :location "
            + "                  AND        e.voided = 0 "
            + "                  AND        o.voided = 0 "
            + "                  AND        breastfeeding.patient_id = e.patient_id "
            + "                  AND        e.encounter_datetime >= breastfeeding.bf_date "
            + "                  AND        e.encounter_datetime BETWEEN timestampadd(month, -9, breastfeeding.last_vl) AND        breastfeeding.last_vl "
            + "                  UNION "
            + "                  SELECT     e.patient_id "
            + "                  FROM       encounter e "
            + "                  INNER JOIN obs o "
            + "                  ON         o.encounter_id = e.encounter_id "
            + "                  WHERE      e.encounter_type IN ( ${5}, "
            + "                                                  ${6} ) "
            + "                  AND        o.concept_id = ${1279} "
            + "                  AND        e.location_id = :location "
            + "                  AND        e.voided = 0 "
            + "                  AND        o.voided = 0 "
            + "                  AND        breastfeeding.patient_id = e.patient_id "
            + "                  AND        e.encounter_datetime >= breastfeeding.bf_date "
            + "                  AND        e.encounter_datetime BETWEEN timestampadd(month, -9, breastfeeding.last_vl) AND        breastfeeding.last_vl "
            + "                  UNION "
            + "                  SELECT     e.patient_id "
            + "                  FROM       encounter e "
            + "                  INNER JOIN obs o "
            + "                  ON         o.encounter_id = e.encounter_id "
            + "                  WHERE      e.encounter_type IN ( ${5}, "
            + "                                                  ${6} ) "
            + "                  AND        o.concept_id = ${1600} "
            + "                  AND        e.location_id = :location "
            + "                  AND        e.voided = 0 "
            + "                  AND        o.voided = 0 "
            + "                  AND        breastfeeding.patient_id = e.patient_id "
            + "                  AND        e.encounter_datetime >= breastfeeding.bf_date "
            + "                  AND        e.encounter_datetime BETWEEN timestampadd(month, -9, breastfeeding.last_vl) AND        breastfeeding.last_vl "
            + "                  UNION "
            + "                  SELECT     e.patient_id "
            + "                  FROM       encounter e "
            + "                  INNER JOIN obs o "
            + "                  ON         o.encounter_id = e.encounter_id "
            + "                  WHERE      e.encounter_type = ${6} "
            + "                  AND        o.concept_id = ${6334} "
            + "                  AND        o.value_coded = ${6331} "
            + "                  AND        e.location_id = :location "
            + "                  AND        e.voided = 0 "
            + "                  AND        o.voided = 0 "
            + "                  AND        breastfeeding.patient_id = e.patient_id "
            + "                  AND        e.encounter_datetime >= breastfeeding.bf_date "
            + "                  AND        e.encounter_datetime BETWEEN timestampadd(month, -9, breastfeeding.last_vl) AND        breastfeeding.last_vl "
            + "                  UNION "
            + "                  SELECT pp.patient_id "
            + "                  FROM   patient_program pp "
            + "                  WHERE  pp.program_id = ${8} "
            + "                  AND    pp.voided = 0 "
            + "                  AND    breastfeeding.patient_id = pp.patient_id "
            + "                  AND    pp.location_id = :location "
            + "                  AND    pp.date_enrolled >= breastfeeding.bf_date "
            + "                  AND    pp.date_enrolled BETWEEN timestampadd(month, -9, breastfeeding.last_vl) AND    breastfeeding.last_vl "
            + "                  UNION "
            + "                  SELECT     e.patient_id "
            + "                  FROM       encounter e "
            + "                  INNER JOIN obs o "
            + "                  ON         o.encounter_id = e.encounter_id "
            + "                  INNER JOIN obs o2 "
            + "                  ON         o2.encounter_id = e.encounter_id "
            + "                  WHERE      e.encounter_type = ${53} "
            + "                  AND        o.concept_id = ${1982} "
            + "                  AND        o.value_coded = ${1065} "
            + "                  AND        o2.concept_id = ${1190} "
            + "                  AND        o2.value_datetime BETWEEN timestampadd(month, -9, breastfeeding.last_vl) AND        breastfeeding.last_vl "
            + "                  AND        e.location_id = :location "
            + "                  AND        e.voided = 0 "
            + "                  AND        o.voided = 0 "
            + "                  AND        o2.voided = 0 "
            + "                  AND        breastfeeding.patient_id = e.patient_id "
            + "                  AND        o2.value_datetime >= breastfeeding.bf_date "
            + "                  UNION "
            + "                  SELECT     e.patient_id "
            + "                  FROM       encounter e "
            + "                  INNER JOIN obs o "
            + "                  ON         o.encounter_id = e.encounter_id "
            + "                  WHERE      e.encounter_type = ${6} "
            + "                  AND        o.concept_id = ${1982} "
            + "                  AND        o.value_coded = ${1065} "
            + "                  AND        e.location_id = :location "
            + "                  AND        e.voided = 0 "
            + "                  AND        o.voided = 0 "
            + "                  AND        breastfeeding.patient_id = e.patient_id "
            + "                  AND        e.encounter_datetime >= breastfeeding.bf_date "
            + "                  AND        e.encounter_datetime BETWEEN timestampadd(month, -9, breastfeeding.last_vl) AND        breastfeeding.last_vl "
            + "                  UNION "
            + "                  SELECT     e.patient_id "
            + "                  FROM       encounter e "
            + "                  INNER JOIN obs o "
            + "                  ON         o.encounter_id = e.encounter_id "
            + "                  INNER JOIN obs o2 "
            + "                  ON         o2.encounter_id = e.encounter_id "
            + "                  WHERE      e.encounter_type = ${51} "
            + "                  AND        o.concept_id = ${1982} "
            + "                  AND        o.value_coded = ${1065} "
            + "                  AND        o2.concept_id = ${23821} "
            + "                  AND        cast(o2.value_datetime AS date) BETWEEN timestampadd(month, -9, breastfeeding.last_vl) AND        breastfeeding.last_vl "
            + "                  AND        e.location_id = :location "
            + "                  AND        e.voided = 0 "
            + "                  AND        o.voided = 0 "
            + "                  AND        o2.voided = 0 "
            + "                  AND        breastfeeding.patient_id = e.patient_id "
            + "                  AND        cast(o2.value_datetime AS date) >= breastfeeding.bf_date )";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    String mappedQuery = stringSubstitutor.replace(query);
    sqlCohortDefinition.setQuery(mappedQuery);

    return sqlCohortDefinition;
  }
}
