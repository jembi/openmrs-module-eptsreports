package org.openmrs.module.eptsreports.reporting.library.cohorts;

import static org.openmrs.module.reporting.evaluation.parameter.Mapped.mapStraightThrough;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.calculation.dsd.OnArtForAtleastXmonthsCalculation;
import org.openmrs.module.eptsreports.reporting.cohort.definition.CalculationCohortDefinition;
import org.openmrs.module.eptsreports.reporting.library.queries.DsdQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EriDSDCohortQueries {

  @Autowired private TxNewCohortQueries txNewCohortQueries;
  @Autowired private TxRttCohortQueries txRttCohortQueries;
  @Autowired private GenericCohortQueries genericCohortQueries;
  @Autowired private CommonCohortQueries commonCohortQueries;

  @Autowired private AgeCohortQueries ageCohortQueries;
  @Autowired private HivCohortQueries hivCohortQueries;
  @Autowired private HivMetadata hivMetadata;
  @Autowired private ResumoMensalCohortQueries resumoMensalCohortQueries;

  /**
   * <b>Name: D1</b>
   *
   * <p><b>Description:</b> Number of active patients on ART Eligible for DSD for Stable Patients”
   *
   * <p><b>NOTE:</b> Excluding patients registered as pregnant, breastfeeding, in TB Treatment and
   * were ever on Sarcoma Karposi
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getD1() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("D1 - Number of active patients on ART Eligible for DSD for Stable Patients");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addSearch(
        "moreThan2years",
        EptsReportUtils.map(
            ageCohortQueries.createXtoYAgeCohort("moreThanOrEqual2Years", 2, 200),
            "effectiveDate=${endDate}"));

    cd.addSearch(
        "pregnantBreastfeedingTB",
        EptsReportUtils.map(
            getPregnantAndBreastfeedingAndOnTBTreatment(),
            "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "sarcomaKarposi",
        EptsReportUtils.map(
            getAllPatientsOnSarcomaKarposi(), "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "D3",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13(),
            "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "stable",
        EptsReportUtils.map(getPatientsWhoAreStable(3), "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "returned",
        EptsReportUtils.map(getPatientsWhoReturned(), "endDate=${endDate},location=${location}"));

    cd.setCompositionString(
        "(D3 AND moreThan2years AND stable AND NOT (pregnantBreastfeedingTB OR sarcomaKarposi OR returned))");

    return cd;
  }

  /**
   * <b>Description:</b> Active and stable Patients who are Non-Pregnant and Non-Breastfeeding for
   * <b>D1</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoAreNotPregnantAndNotBreastfeedingD1() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("D1 - Patients who are Non-pregnant and Non-Breastfeeding");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "breastfeeding",
        EptsReportUtils.map(
            txNewCohortQueries.getTxNewBreastfeedingComposition(false),
            "onOrAfter=${endDate-18m},onOrBefore=${endDate},location=${location}"));
    cd.addSearch(
        "pregnant",
        EptsReportUtils.map(
            txNewCohortQueries.getPatientsPregnantEnrolledOnART(false),
            "startDate=${endDate-9m},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "activeAndStable", EptsReportUtils.map(getD1(), "endDate=${endDate},location=${location}"));

    cd.setCompositionString("activeAndStable AND NOT pregnant AND NOT breastfeeding");

    return cd;
  }

  /**
   * <b>Name: D2</b>
   *
   * <p><b>Description:</b> Number of active patients on ART Not Eligible for <b>D1</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getD2() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("D2 - Number of active patients on ART Not Eligible for DSD D1");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addSearch(
        "activeAndStablePatients",
        EptsReportUtils.map(getD1(), "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "B13",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13(),
            "endDate=${endDate},location=${location}"));

    cd.setCompositionString("B13 AND NOT activeAndStablePatients");
    //    cd.setCompositionString("B13 AND NOT activeAndStablePatients");

    return cd;
  }

  /**
   * <b>Description:</b> Patients who are Non-Pregnant and Non-Breastfeeding for <b>D2</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoAreNotPregnantAndNotBreastfeedingD2() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("D2 - who are Non-pregnant and Non-Breastfeeding");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "breastfeeding",
        EptsReportUtils.map(
            txNewCohortQueries.getTxNewBreastfeedingComposition(true),
            "onOrAfter=${endDate-18m},onOrBefore=${endDate},location=${location}"));
    cd.addSearch(
        "pregnant",
        EptsReportUtils.map(
            txNewCohortQueries.getPatientsPregnantEnrolledOnART(true),
            "startDate=${endDate-9m},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "activeAndUnstable",
        EptsReportUtils.map(getD2(), "endDate=${endDate},location=${location}"));

    cd.setCompositionString("activeAndUnstable AND NOT (pregnant OR breastfeeding)");

    return cd;
  }

  /**
   * <b>Description:</b> Pregnant Women for <b>D2</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoArePregnantD2() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("D2 - who are pregnant");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "pregnant",
        EptsReportUtils.map(
            txNewCohortQueries.getPatientsPregnantEnrolledOnART(true),
            "startDate=${endDate-9m},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "activeAndUnstable",
        EptsReportUtils.map(getD2(), "endDate=${endDate},location=${location}"));

    cd.setCompositionString("activeAndUnstable AND pregnant");

    return cd;
  }

  /**
   * <b>Description:</b> Patients who are Breastfeeding for <b>D2</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoAreBreastfeedingD2() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("D2 - who are Breastfeeding");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "breastfeeding",
        EptsReportUtils.map(
            txNewCohortQueries.getTxNewBreastfeedingComposition(true),
            "onOrAfter=${endDate-18m},onOrBefore=${endDate},location=${location}"));

    cd.addSearch(
        "activeAndUnstable",
        EptsReportUtils.map(getD2(), "endDate=${endDate},location=${location}"));

    cd.setCompositionString("activeAndUnstable AND breastfeeding");

    return cd;
  }

  /**
   * <b>Name: D3</b>
   *
   * <p><b>Description:</b> D3: Number of all patients currently on ART <b>D3</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getD3() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("D3 - D3: Number of all patients currently on ART");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addSearch(
        "activeAndStablePatients",
        EptsReportUtils.map(getD1(), "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "pregnantOrBreastfeedingOrTBTreatment",
        EptsReportUtils.map(
            getPregnantAndBreastfeedingAndOnTBTreatment(),
            "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "B13",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13(),
            "endDate=${endDate},location=${location}"));

    cd.setCompositionString("B13");

    return cd;
  }

  /**
   * <b>Description:</b> Patients who are Non-Pregnant and Non-Breastfeeding for <b>D3</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoAreNotPregnantAndNotBreastfeedingD3() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("D3 - who are Non-pregnant and Non-Breastfeeding");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addSearch(
        "breastfeeding",
        EptsReportUtils.map(
            txNewCohortQueries.getTxNewBreastfeedingComposition(true),
            "onOrAfter=${endDate-18m},onOrBefore=${endDate},location=${location}"));

    cd.addSearch(
        "pregnant",
        EptsReportUtils.map(
            txNewCohortQueries.getPatientsPregnantEnrolledOnART(true),
            "startDate=${endDate-9m},endDate=${endDate},location=${location}"));

    cd.addSearch("onART", EptsReportUtils.map(getD3(), "endDate=${endDate},location=${location}"));

    cd.setCompositionString("onART AND NOT pregnant AND NOT breastfeeding");

    return cd;
  }

  /**
   * <b>Description:</b> Pregnant Women for <b>D3</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoArePregnantD3() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("D3 - who are pregnant");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "pregnant",
        EptsReportUtils.map(
            txNewCohortQueries.getPatientsPregnantEnrolledOnART(true),
            "startDate=${endDate-9m},endDate=${endDate},location=${location}"));
    cd.addSearch("onART", EptsReportUtils.map(getD3(), "endDate=${endDate},location=${location}"));

    cd.setCompositionString("onART AND pregnant");

    return cd;
  }

  /**
   * <b>Name: D4</b>
   *
   * <p><b>Description:</b> Number of active patients on ART eligible for Dispensa Bimestral”
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getD4() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("D4 - Number of active patients on ART eligible for Dispensa Bimestral");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addSearch(
        "moreThan2years",
        EptsReportUtils.map(
            ageCohortQueries.createXtoYAgeCohort("moreThanOrEqual2Years", 2, 200),
            "effectiveDate=${endDate}"));

    cd.addSearch(
        "breastfeeding",
        EptsReportUtils.map(getBreastfeeding(), "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "pregnant",
        EptsReportUtils.map(
            txNewCohortQueries.getPatientsPregnantEnrolledOnART(true),
            "startDate=${endDate-9m},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "tb",
        EptsReportUtils.map(
            commonCohortQueries.getPatientsOnTbTreatment(),
            "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "sarcomaKarposi",
        EptsReportUtils.map(
            getAllPatientsOnSarcomaKarposi(), "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "B13",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13(),
            "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "stable",
        EptsReportUtils.map(getPatientsWhoAreStable(6), "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "returned",
        EptsReportUtils.map(getPatientsWhoReturned(), "endDate=${endDate},location=${location}"));

    cd.setCompositionString(
        "(B13 AND moreThan2years AND breastfeeding AND stable AND NOT (pregnant OR sarcomaKarposi OR returned OR tb))");
    return cd;
  }

  /**
   * <b>Name: D3 NOT D4</b>
   *
   * <p><b>Description:</b> Number of active patients on ART not eligible for Dispensa Bimestral”
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getD3NotD4() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("D3 Not D4 - Number of active patients on ART not eligible for Dispensa Bimestral");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addSearch(
        "moreThan2years",
        EptsReportUtils.map(
            ageCohortQueries.createXtoYAgeCohort("moreThanOrEqual2Years", 2, 200),
            "effectiveDate=${endDate}"));

    cd.addSearch(
        "breastfeeding",
        EptsReportUtils.map(getBreastfeeding(), "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "pregnant",
        EptsReportUtils.map(
            txNewCohortQueries.getPatientsPregnantEnrolledOnART(true),
            "startDate=${endDate-9m},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "tb",
        EptsReportUtils.map(
            commonCohortQueries.getPatientsOnTbTreatment(),
            "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "sarcomaKarposi",
        EptsReportUtils.map(
            getAllPatientsOnSarcomaKarposi(), "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "B13",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13(),
            "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "stable",
        EptsReportUtils.map(getPatientsWhoAreStable(6), "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "returned",
        EptsReportUtils.map(getPatientsWhoReturned(), "endDate=${endDate},location=${location}"));

    cd.setCompositionString(
        "(B13 AND NOT (moreThan2years AND breastfeeding AND stable AND NOT (pregnant OR sarcomaKarposi OR returned OR tb)))");
    return cd;
  }

  private CohortDefinition getBreastfeeding() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Women who are Breastfeeding at least 11 months");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6332", hivMetadata.getBreastfeeding().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "    FROM patient p "
            + "        INNER JOIN encounter e ON e.patient_id=p.patient_id "
            + "        INNER JOIN obs o ON o.encounter_id=e.encounter_id "
            + "        INNER JOIN (SELECT p.patient_id,MIN(e.encounter_datetime) min_breast "
            + "                    FROM patient p "
            + "                      INNER JOIN person pe ON pe.person_id=p.patient_id "
            + "                      INNER JOIN encounter e ON e.patient_id=p.patient_id "
            + "                      INNER JOIN obs o ON o.encounter_id=e.encounter_id "
            + "                    WHERE p.voided=0 "
            + "                          AND e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND pe.voided = 0 "
            + "                          AND e.encounter_type = ${6} "
            + "                          AND o.concept_id =  ${6332} "
            + "                          AND o.value_coded = ${1065} "
            + "                          AND e.location_id = :location "
            + "                          AND pe.gender='F' "
            + "                          AND e.encounter_datetime BETWEEN DATE_SUB(:endDate, INTERVAL 18 MONTH) AND :endDate "
            + "                      GROUP BY p.patient_id) AS breastfeeding ON breastfeeding.patient_id = p.patient_id "
            + "        INNER JOIN (SELECT p.patient_id,MAX(e.encounter_datetime) AS last_clinic_date "
            + "                    FROM patient p "
            + "                      INNER JOIN encounter e ON e.patient_id=p.patient_id "
            + "                      INNER JOIN obs o ON o.encounter_id=e.encounter_id "
            + "                    WHERE p.voided=0 "
            + "                          AND e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.encounter_type =${6}"
            + "                          AND e.location_id = :location "
            + "                          AND e.encounter_datetime <= :endDate "
            + "                    GROUP BY p.patient_id) AS last_clinic ON last_clinic.patient_id=p.patient_id "
            + "    WHERE "
            + "        p.voided = 0 AND e.voided = 0  AND o.voided = 0 "
            + "        AND TIMESTAMPDIFF(MONTH, breastfeeding.min_breast, last_clinic.last_clinic_date) >= 11 "
            + "        AND e.location_id = :location AND e.encounter_type=6 "
            + "        AND NOT EXISTS( "
            + "                              SELECT p.patient_id "
            + "                              FROM patient p "
            + "                                INNER JOIN encounter e ON e.patient_id=p.patient_id "
            + "                                INNER JOIN( "
            + "                                      SELECT p.patient_id,MAX(e.encounter_datetime) AS pregnant_date "
            + "                                      FROM patient p "
            + "                                        INNER JOIN encounter e ON e.patient_id=p.patient_id "
            + "                                        INNER JOIN obs o ON o.encounter_id=e.encounter_id "
            + "                                      WHERE p.voided=0 "
            + "                                            AND e.voided = 0 "
            + "                                            AND o.voided = 0 "
            + "                                            AND e.encounter_type =${6}"
            + "                                            AND o.concept_id = ${1982} "
            + "                                            AND o.value_coded = ${1065} "
            + "                                            AND e.location_id = :location "
            + "                                            BETWEEN DATE_SUB(:endDate, INTERVAL 18 MONTH) AND :endDate "
            + "                                      GROUP BY p.patient_id) AS last_pregnant ON last_pregnant.patient_id=p.patient_id "
            + "                                INNER JOIN( "
            + "                                  SELECT p.patient_id,MAX(e.encounter_datetime) AS breastfeeding_date "
            + "                                      FROM patient p "
            + "                                        INNER JOIN encounter e ON e.patient_id=p.patient_id "
            + "                                        INNER JOIN obs o ON o.encounter_id=e.encounter_id "
            + "                                      WHERE p.voided=0 "
            + "                                            AND e.voided = 0 "
            + "                                            AND o.voided = 0 "
            + "                                            AND e.encounter_type =${6}"
            + "                                            AND o.concept_id =  ${6332} "
            + "                                            AND o.value_coded = ${1065} "
            + "                                            AND e.location_id = :location "
            + "                                            BETWEEN DATE_SUB(:endDate, INTERVAL 18 MONTH) AND :endDate "
            + "                                      GROUP BY p.patient_id "
            + "                                ) AS last_breast ON last_breast.patient_id=p.patient_id "
            + "                                      WHERE p.voided=0 AND e.voided=0 AND e.encounter_type=${6} AND e.location_id= :location"
            + "                                            AND Date(last_pregnant.pregnant_date)>=Date(last_breast.breastfeeding_date) "
            + "        ) "
            + "    GROUP BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    String replaceQuery = sb.replace(query);

    cd.setQuery(replaceQuery);

    return cd;
  }

  /**
   * <b>Description:</b> Patients who are Breastfeeding for <b>D3</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoAreBreastfeedingD3() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("D3 - who are Breastfeeding");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "breastfeeding",
        EptsReportUtils.map(
            txNewCohortQueries.getTxNewBreastfeedingComposition(true),
            "onOrAfter=${endDate-18m},onOrBefore=${endDate},location=${location}"));

    cd.addSearch("onART", EptsReportUtils.map(getD3(), "endDate=${endDate},location=${location}"));

    cd.setCompositionString("onART AND breastfeeding");

    return cd;
  }

  /**
   * <b>Description:</b> Patients who are Breastfeeding for <b>D4</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoAreBreastfeedingD4() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("D4 - who are Breastfeeding");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "breastfeeding",
        EptsReportUtils.map(getBreastfeeding(), "endDate=${endDate},location=${location}"));

    cd.addSearch("onART", EptsReportUtils.map(getD4(), "endDate=${endDate},location=${location}"));

    cd.setCompositionString("onART");

    return cd;
  }

  /**
   * <b>Description:</b> Pregnant Women for the <b>Numerators</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoArePregnant(int indicatorFlag) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("D3 - who are pregnant");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    String mappings = "endDate=${endDate},location=${location}";

    if (indicatorFlag == 1) {
      cd.addSearch("onART", EptsReportUtils.map(getN1(), mappings));
    } else if (indicatorFlag == 2) {
      cd.addSearch("onART", EptsReportUtils.map(getN2(), mappings));
    } else if (indicatorFlag == 3) {
      cd.addSearch("onART", EptsReportUtils.map(getN3(), mappings));
    } else if (indicatorFlag == 4) {
      cd.addSearch("onART", EptsReportUtils.map(getN4(), mappings));
    } else if (indicatorFlag == 5) {
      cd.addSearch("onART", EptsReportUtils.map(getN5(), mappings));
    } else if (indicatorFlag == 6) {
      cd.addSearch("onART", EptsReportUtils.map(getN6(), mappings));
    } else if (indicatorFlag == 7) {
      cd.addSearch("onART", EptsReportUtils.map(getN7(), mappings));
    } else if (indicatorFlag == 8) {
      cd.addSearch("onART", EptsReportUtils.map(getN8(), mappings));
    } else if (indicatorFlag == 9) {
      cd.addSearch("onART", EptsReportUtils.map(getN9(), mappings));
    } else if (indicatorFlag == 10) {
      cd.addSearch("onART", EptsReportUtils.map(getN10(), mappings));
    } else if (indicatorFlag == 11) {
      cd.addSearch("onART", EptsReportUtils.map(getN11(), mappings));
    } else if (indicatorFlag == 12) {
      cd.addSearch("onART", EptsReportUtils.map(getN12(), mappings));
    } else if (indicatorFlag == 13) {
      cd.addSearch("onART", EptsReportUtils.map(getN13(), mappings));
    } else if (indicatorFlag == 14) {
      cd.addSearch("onART", EptsReportUtils.map(getN14(), mappings));
    } else if (indicatorFlag == 15) {
      cd.addSearch("onART", EptsReportUtils.map(getN15(), mappings));
    } else if (indicatorFlag == 16) {
      cd.addSearch("onART", EptsReportUtils.map(getN16(), mappings));
    } else if (indicatorFlag == 17) {
      cd.addSearch("onART", EptsReportUtils.map(getN17(), mappings));
    } else if (indicatorFlag == 18) {
      cd.addSearch("onART", EptsReportUtils.map(getN18(), mappings));
    } else if (indicatorFlag == 19) {
      cd.addSearch("onART", EptsReportUtils.map(getN19(), mappings));
    } else if (indicatorFlag == 20) {
      cd.addSearch("onART", EptsReportUtils.map(getN20(), mappings));
    } else if (indicatorFlag == 21) {
      cd.addSearch("onART", EptsReportUtils.map(getN21(), mappings));
    }

    cd.addSearch(
        "pregnant",
        EptsReportUtils.map(
            txNewCohortQueries.getPatientsPregnantEnrolledOnART(true),
            "startDate=${endDate-9m},endDate=${endDate},location=${location}"));

    cd.setCompositionString("onART AND pregnant");

    return cd;
  }

  /**
   * <b>Description:</b> Patients who are Breastfeeding for <b>Numerators</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoAreBreastfeeding(int indicatorFlag) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("D3 - who are Breastfeeding");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    String mappings = "endDate=${endDate},location=${location}";
    if (indicatorFlag == 1) {
      cd.addSearch("onART", EptsReportUtils.map(getN1(), mappings));
    }
    if (indicatorFlag == 2) {
      cd.addSearch("onART", EptsReportUtils.map(getN2(), mappings));
    } else if (indicatorFlag == 3) {
      cd.addSearch("onART", EptsReportUtils.map(getN3(), mappings));
    } else if (indicatorFlag == 4) {
      cd.addSearch("onART", EptsReportUtils.map(getN4(), mappings));
    } else if (indicatorFlag == 5) {
      cd.addSearch("onART", EptsReportUtils.map(getN5(), mappings));
    } else if (indicatorFlag == 6) {
      cd.addSearch("onART", EptsReportUtils.map(getN6(), mappings));
    } else if (indicatorFlag == 7) {
      cd.addSearch("onART", EptsReportUtils.map(getN7(), mappings));
    } else if (indicatorFlag == 8) {
      cd.addSearch("onART", EptsReportUtils.map(getN8(), mappings));
    } else if (indicatorFlag == 9) {
      cd.addSearch("onART", EptsReportUtils.map(getN9(), mappings));
    } else if (indicatorFlag == 10) {
      cd.addSearch("onART", EptsReportUtils.map(getN10(), mappings));
    } else if (indicatorFlag == 11) {
      cd.addSearch("onART", EptsReportUtils.map(getN11(), mappings));
    } else if (indicatorFlag == 12) {
      cd.addSearch("onART", EptsReportUtils.map(getN12(), mappings));
    } else if (indicatorFlag == 13) {
      cd.addSearch("onART", EptsReportUtils.map(getN13(), mappings));
    } else if (indicatorFlag == 14) {
      cd.addSearch("onART", EptsReportUtils.map(getN14(), mappings));
    } else if (indicatorFlag == 15) {
      cd.addSearch("onART", EptsReportUtils.map(getN15(), mappings));
    } else if (indicatorFlag == 16) {
      cd.addSearch("onART", EptsReportUtils.map(getN16(), mappings));
    } else if (indicatorFlag == 17) {
      cd.addSearch("onART", EptsReportUtils.map(getN17(), mappings));
    } else if (indicatorFlag == 18) {
      cd.addSearch("onART", EptsReportUtils.map(getN18(), mappings));
    } else if (indicatorFlag == 19) {
      cd.addSearch("onART", EptsReportUtils.map(getN19(), mappings));
    } else if (indicatorFlag == 20) {
      cd.addSearch("onART", EptsReportUtils.map(getN20(), mappings));
    } else if (indicatorFlag == 21) {
      cd.addSearch("onART", EptsReportUtils.map(getN21(), mappings));
    }

    cd.addSearch(
        "breastfeeding",
        EptsReportUtils.map(
            txNewCohortQueries.getTxNewBreastfeedingComposition(true),
            "onOrAfter=${endDate-18m},onOrBefore=${endDate},location=${location}"));

    cd.setCompositionString("onART AND breastfeeding");

    return cd;
  }

  /**
   * <b>Name: N1</b>
   *
   * <p><b>Description:</b> Number of active patients on ART who are included in at least one DSD
   * model for stable patients (GA, DT, DS, DA, FR, DCA, DD)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getN1() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("participatingInDsdModel");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addSearch("N2", mapStraightThrough(getN2()));
    cd.addSearch("N3", mapStraightThrough(getN3()));
    cd.addSearch("N5", mapStraightThrough(getN5()));
    cd.addSearch("N6", mapStraightThrough(getN6()));
    cd.addSearch("N7", mapStraightThrough(getN7()));
    cd.addSearch("N8", mapStraightThrough(getN8()));
    cd.addSearch("N4", mapStraightThrough(getN4()));
    cd.setCompositionString("(N2 OR N3 OR N4 OR N5 OR N6 OR N7 OR N8)");
    return cd;
  }

  /**
   * <b>Name: N2</b>
   *
   * <p><b>Description:</b> Number of all patients currently on ART who are included in DSD model:
   * <b>Dispensa Trimestral (DT)</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getN2() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "Number of all patients currently on ART who are included in DSD model: Dispensa Trimestral (DT)");

    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition nextArtPickUpScheduledORdispensaTrimestral =
        DsdQueries
            .getPatientsWithTypeOfDispensationOnMdcInTheMostRecentFichaClinicaOrWithPickupOnFilaBetween(
                83, 97, Arrays.asList(hivMetadata.getQuarterlyDispensation().getConceptId()));

    cd.addSearch(
        "nextArtPickUpScheduledORdispensaTrimestral",
        EptsReportUtils.map(
            nextArtPickUpScheduledORdispensaTrimestral, "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "D3",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13(),
            "endDate=${endDate},location=${location}"));

    cd.setCompositionString("D3 AND nextArtPickUpScheduledORdispensaTrimestral");

    return cd;
  }

  /**
   * <b>Name: N3</b>
   *
   * <p><b>Description:</b> Number of all patients currently on ART who are included in DSD model:
   * <b>Dispensa Semestral (DS)</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getN3() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "N3: Number of all patients currently on ART who are included in DSD model: Dispensa Semestral (DS)");

    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition nextArtPickUpScheduledORdispensaSemestral =
        DsdQueries
            .getPatientsWithTypeOfDispensationOnMdcInTheMostRecentFichaClinicaOrWithPickupOnFilaBetween(
                173, 187, Arrays.asList(hivMetadata.getSemiannualDispensation().getConceptId()));

    cd.addSearch(
        "nextArtPickUpScheduledORdispensaSemestral",
        EptsReportUtils.map(
            nextArtPickUpScheduledORdispensaSemestral, "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "D3",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13(),
            "endDate=${endDate},location=${location}"));

    cd.setCompositionString("D3 AND nextArtPickUpScheduledORdispensaSemestral");

    return cd;
  }

  /**
   * <b>Name: N4</b>
   *
   * <p><b>Description:</b> N4: Number of all patients currently on ART who are included in DSD
   * model: <b>Dispensa Anual (DA)</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getN4() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "N4: Number of all patients currently on ART who are included in DSD model: Dispensa Anual (DA)");

    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition nextArtPickUpScheduledORdispensaAnual =
        DsdQueries
            .getPatientsWithTypeOfDispensationOnMdcInTheMostRecentFichaClinicaOrWithPickupOnFilaBetween(
                335,
                395,
                Arrays.asList(hivMetadata.getAnnualArvDispensationConcept().getConceptId()));

    cd.addSearch(
        "nextArtPickUpScheduledORdispensaAnual",
        EptsReportUtils.map(
            nextArtPickUpScheduledORdispensaAnual, "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "D3",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13(),
            "endDate=${endDate},location=${location}"));

    cd.setCompositionString("D3 AND nextArtPickUpScheduledORdispensaAnual");

    return cd;
  }

  /**
   * <b>Name: N5</b>
   *
   * <p><b>Description:</b> Number of all patients currently on ART who are included in DSD model:
   * <b>Dispensa Descentralizada (DD)</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getN5() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "N5: Number of all patients currently on ART who are included in DSD model: Dispensa Descentralizada (DD)");

    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition dispensaDescentralizada =
        DsdQueries.getPatientsWithTypeOfDispensationOnMdcInTheMostRecentFichaClinicaAndFila(
            Arrays.asList(hivMetadata.getLastRecordOfFarmacConcept().getConceptId()),
            Arrays.asList(hivMetadata.getDescentralizedArvDispensationConcept().getConceptId()));

    cd.addSearch(
        "dispensaDescentralizada",
        EptsReportUtils.map(dispensaDescentralizada, "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "D3",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13(),
            "endDate=${endDate},location=${location}"));

    cd.setCompositionString("D3 AND dispensaDescentralizada");

    return cd;
  }

  /**
   * <b>Name: N6</b>
   *
   * <p><b>Description:</b> Number of all patients currently on ART who are included in DSD model:
   * <b>Dispensa Comunitária através do APE (DCA)</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getN6() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "N6: Number of all patients currently on ART who are included in DSD model: Dispensa Comunitária através do APE (DCA)");

    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition dispensaComunitariaApe =
        DsdQueries.getPatientsWithTypeOfDispensationOnMdcInTheMostRecentFichaClinicaAndFila(
            Arrays.asList(hivMetadata.getDispensaComunitariaViaApeConcept().getConceptId()),
            Arrays.asList(hivMetadata.getDispensaComunitariaViaApeConcept().getConceptId()));

    cd.addSearch(
        "dispensaComunitariaApe",
        EptsReportUtils.map(dispensaComunitariaApe, "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "D3",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13(),
            "endDate=${endDate},location=${location}"));

    cd.setCompositionString("D3 AND dispensaComunitariaApe");

    return cd;
  }

  /**
   * <b>Name: N7</b>
   *
   * <p><b>Description:</b> N7: Number of all patients currently on ART who are included in DSD
   * model: <b>Fluxo Rápido (FR)</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getN7() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "N7: Number of all patients currently on ART who are included in DSD model: Fluxo Rápido (FR)");

    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition nextArtPickUpScheduledORfluxoRapido =
        DsdQueries.getPatientsWithTypeOfDispensationOnMdcInTheMostRecentFichaClinica(
            Arrays.asList(hivMetadata.getRapidFlow().getConceptId()), 175, 190);

    cd.addSearch(
        "nextArtPickUpScheduledORfluxoRapido",
        EptsReportUtils.map(
            nextArtPickUpScheduledORfluxoRapido, "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "D3",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13(),
            "endDate=${endDate},location=${location}"));

    cd.setCompositionString("D3 AND nextArtPickUpScheduledORfluxoRapido");

    return cd;
  }

  /**
   * <b>Name: N8</b>
   *
   * <p><b>Description:</b> N8: Number of all patients currently on ART who are included in DSD
   * model: <b>GAAC (GA)</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getN8() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "N8 - Number of all patients currently on ART who are included in DSD model: GAAC (GA)");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition GAAC =
        DsdQueries.getPatientsWithTypeOfDispensationOnMdcInTheMostRecentFichaClinica(
            Arrays.asList(hivMetadata.getGaac().getConceptId()));

    cd.addSearch("GAAC", EptsReportUtils.map(GAAC, "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "D3",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13(),
            "endDate=${endDate},location=${location}"));

    cd.setCompositionString("D3 AND GAAC");

    return cd;
  }

  /**
   * <b>Name: N9</b>
   *
   * <p><b>Description:</b> N9: Number of all patients currently on ART who are included in DSD
   * model: <b> Dispensa Comunitária pelo Provedor (DCP)</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getN9() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "N9: Number of all patients currently on ART who are included in DSD model: Dispensa Comunitária pelo Provedor (DCP)");

    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition dispensaComunitariaProvedor =
        DsdQueries.getPatientsWithTypeOfDispensationOnMdcInTheMostRecentFichaClinicaAndFila(
            Arrays.asList(hivMetadata.getDispensaComunitariaViaProvedorConcept().getConceptId()),
            Arrays.asList(hivMetadata.getDispensaComunitariaViaProvedorConcept().getConceptId()));

    cd.addSearch(
        "dispensaComunitariaProvedor",
        EptsReportUtils.map(
            dispensaComunitariaProvedor, "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "D3",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13(),
            "endDate=${endDate},location=${location}"));

    cd.setCompositionString("D3 AND dispensaComunitariaProvedor");

    return cd;
  }

  /**
   * <b>Name: N10</b>
   *
   * <p><b>Description:</b> N10: Number of all patients currently on ART who are included in DSD
   * model: <b>Brigada Móvel (BM)</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getN10() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "N10: Number of all patients currently on ART who are included in DSD model: Brigada Móvel (BM)");

    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition brigadaMovel =
        DsdQueries.getPatientsWithTypeOfDispensationOnMdcInTheMostRecentFichaClinicaAndFila(
            Arrays.asList(
                hivMetadata.getBrigadasMoveisDiurnasConcept().getConceptId(),
                hivMetadata.getBrigadasMoveisNocturnasConcept().getConceptId()),
            Arrays.asList(hivMetadata.getBrigadasMoveisConcept().getConceptId()));

    cd.addSearch(
        "brigadaMovel",
        EptsReportUtils.map(brigadaMovel, "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "D3",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13(),
            "endDate=${endDate},location=${location}"));

    cd.setCompositionString("D3 AND brigadaMovel");
    return cd;
  }

  /**
   * <b>Name: N11</b>
   *
   * <p><b>Description:</b> N11: Number of all patients currently on ART who are included in DSD
   * model: <b>Clínica Móvel (CM)</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getN11() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "N11: Number of all patients currently on ART who are included in DSD model: Clínica Móvel (CM)");

    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition clinicaMovel =
        DsdQueries.getPatientsWithTypeOfDispensationOnMdcInTheMostRecentFichaClinicaAndFila(
            Arrays.asList(
                hivMetadata.getClinicasMoveisDiurnasConcept().getConceptId(),
                hivMetadata.getClinicasMoveisNocturnasConcept().getConceptId()),
            Arrays.asList(hivMetadata.getClinicasMoveisConcept().getConceptId()));

    cd.addSearch(
        "clinicaMovel",
        EptsReportUtils.map(clinicaMovel, "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "D3",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13(),
            "endDate=${endDate},location=${location}"));

    cd.setCompositionString("D3 AND clinicaMovel");

    return cd;
  }

  /**
   * <b>Name: N12</b>
   *
   * <p><b>Description:</b> N12: Number of all patients currently on ART who are included in DSD
   * model: <b>Abordagem Familiar (AF)</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getN12() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "N12: Number of all patients currently on ART who are included in DSD model: Abordagem Familiar (AF)");

    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition abordagemFamiliar =
        DsdQueries.getPatientsWithTypeOfDispensationOnMdcInTheMostRecentFichaClinica(
            Arrays.asList(hivMetadata.getFamilyApproach().getConceptId()));

    cd.addSearch(
        "abordagemFamiliar",
        EptsReportUtils.map(abordagemFamiliar, "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "D3",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13(),
            "endDate=${endDate},location=${location}"));

    cd.setCompositionString("D3 AND abordagemFamiliar");

    return cd;
  }

  /**
   * <b>Name: N13</b>
   *
   * <p><b>Description:</b> N13: Number of all patients currently on ART who are included in DSD
   * model: <b>Clube de Adesão (CA)</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getN13() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "N13: Number of all patients currently on ART who are included in DSD model: Clube de Adesão (CA)");

    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition clubeDeAdesao =
        DsdQueries.getPatientsWithTypeOfDispensationOnMdcInTheMostRecentFichaClinica(
            Arrays.asList(hivMetadata.getAccessionClubs().getConceptId()));

    cd.addSearch(
        "clubeDeAdesao",
        EptsReportUtils.map(clubeDeAdesao, "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "D3",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13(),
            "endDate=${endDate},location=${location}"));

    cd.setCompositionString("D3 AND clubeDeAdesao");

    return cd;
  }

  /**
   * <b>Name: N14</b>
   *
   * <p><b>Description:</b> N14: Number of all patients currently on ART who are included in DSD
   * model: <b>Extensão Horário (EH)</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getN14() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "N14: Number of all patients currently on ART who are included in DSD model: Extensão Horário (EH)");

    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition extensaoHorario =
        DsdQueries.getPatientsWithTypeOfDispensationOnMdcInTheMostRecentFichaClinicaAndFila(
            Arrays.asList(hivMetadata.getForaDoHorarioConcept().getConceptId()),
            Arrays.asList(hivMetadata.getExtensaoHorarioConcept().getConceptId()));

    cd.addSearch(
        "extensaoHorario",
        EptsReportUtils.map(extensaoHorario, "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "D3",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13(),
            "endDate=${endDate},location=${location}"));

    cd.setCompositionString("D3 AND extensaoHorario");

    return cd;
  }

  /**
   * <b>Name: N15</b>
   *
   * <p><b>Description:</b> N15: Number of all patients currently on ART who are included in DSD
   * model: <b>Paragem Única no Sector de Tuberculose (TB)</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getN15() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "N15: Number of all patients currently on ART who are included in DSD model: Paragem Única no Sector de Tuberculose (TB)");

    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition paragemUnicaTb =
        DsdQueries.getPatientsWithTypeOfDispensationOnMdcInTheMostRecentFichaClinica(
            Arrays.asList(hivMetadata.getParagemUnicaNoSectorDaTBConcept().getConceptId()));

    cd.addSearch(
        "paragemUnicaTb",
        EptsReportUtils.map(paragemUnicaTb, "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "D3",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13(),
            "endDate=${endDate},location=${location}"));

    cd.setCompositionString("D3 AND paragemUnicaTb");

    return cd;
  }

  /**
   * <b>Name: N16</b>
   *
   * <p><b>Description:</b> N16: Number of all patients currently on ART who are included in DSD
   * model: <br>
   * Paragem Única de nos Serviços TARV (CT)</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getN16() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "N16: Number of all patients currently on ART who are included in DSD model: Paragem Unica de nos Serviços TARV (CT)");

    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition paragemUnicaTarv =
        DsdQueries.getPatientsWithTypeOfDispensationOnMdcInTheMostRecentFichaClinica(
            Arrays.asList(hivMetadata.getParagemUnicaNosServicosTARVConcept().getConceptId()));

    cd.addSearch(
        "paragemUnicaTarv",
        EptsReportUtils.map(paragemUnicaTarv, "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "D3",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13(),
            "endDate=${endDate},location=${location}"));

    cd.setCompositionString("D3 AND paragemUnicaTarv");

    return cd;
  }

  /**
   * <b>Name: N17</b>
   *
   * <p><b>Description:</b> N17: Number of all patients currently on ART who are included in DSD
   * model: <b>Paragem Unica Servicos Amigos de Adolescentes e Jovens (SAAJ)</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getN17() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "N17: Number of all patients currently on ART who are included in DSD model: Paragem Unica Servicos Amigos de Adolescentes e Jovens (SAAJ)");

    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition paragemUnicaSaaJ =
        DsdQueries.getPatientsWithTypeOfDispensationOnMdcInTheMostRecentFichaClinica(
            Arrays.asList(hivMetadata.getParagemUnicaNoSAAJConcept().getConceptId()));

    cd.addSearch(
        "paragemUnicaSaaJ",
        EptsReportUtils.map(paragemUnicaSaaJ, "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "D3",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13(),
            "endDate=${endDate},location=${location}"));

    cd.setCompositionString("D3 AND paragemUnicaSaaJ");

    return cd;
  }

  /**
   * <b>Name: N18</b>
   *
   * <p><b>Description:</b> N18: Number of all patients currently on ART who are included in DSD
   * model: <b>Paragem Única Saúde Materna-Infantil (SMI)</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getN18() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "N18: Number of all patients currently on ART who are included in DSD model: Paragem Única Saúde Materna-Infantil (SMI)");

    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition paragemUnicaSmi =
        DsdQueries.getPatientsWithTypeOfDispensationOnMdcInTheMostRecentFichaClinica(
            Arrays.asList(hivMetadata.getParagemUnicaNaSMIConcept().getConceptId()));

    cd.addSearch(
        "paragemUnicaSmi",
        EptsReportUtils.map(paragemUnicaSmi, "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "D3",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13(),
            "endDate=${endDate},location=${location}"));

    cd.setCompositionString("D3 AND paragemUnicaSmi");

    return cd;
  }

  /**
   * <b>Name: N19</b>
   *
   * <p><b>Description:</b> N19: Number of all patients currently on ART who are included in DSD
   * model: <b>Doença Avançada por HIV (DAH)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getN19() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "N19: Number of all patients currently on ART who are included in DSD model: Doença Avançada por HIV (DAH)");

    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition doencaAvancadaHiv =
        DsdQueries.getPatientsWithTypeOfDispensationOnMdcInTheMostRecentFichaClinica(
            Arrays.asList(hivMetadata.getDoencaAvancadaPorHIVConcept().getConceptId()));

    cd.addSearch(
        "doencaAvancadaHiv",
        EptsReportUtils.map(doencaAvancadaHiv, "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "D3",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13(),
            "endDate=${endDate},location=${location}"));

    cd.setCompositionString("D3 AND doencaAvancadaHiv");

    return cd;
  }

  /**
   * <b>Name: N20</b>
   *
   * <p><b>Description:</b> N20: Number of active patients on ART who are included in DSD model:
   * Dispensa Bimestral (DB)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getN20() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "N20: Number of active patients on ART who are included in DSD model: Dispensa Bimestral (DB)");

    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition nextArtPickUpScheduledORdispensaBimensal =
        DsdQueries
            .getPatientsWithTypeOfDispensationOnMdcInTheMostRecentFichaClinicaOrWithPickupOnFilaBetween(
                53,
                67,
                Arrays.asList(hivMetadata.getBimonthlyDispensationConcept().getConceptId()));

    cd.addSearch(
        "nextArtPickUpScheduledORdispensaBimensal",
        EptsReportUtils.map(
            nextArtPickUpScheduledORdispensaBimensal, "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "B13",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13(),
            "endDate=${endDate},location=${location}"));

    cd.setCompositionString("B13 AND nextArtPickUpScheduledORdispensaBimensal");

    return cd;
  }

  /**
   * <b>Name: N21</b>
   *
   * <p><b>Description:</b> Number of patients active on ART who are included in at least one DSD
   * model (DB, DT, DS, DA, DD, DCP, DCA, BM, CM, AF, FR, GA, CA, EH, TB, CT, SAAJ, SMI).
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getN21() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "N21: Number of patients active on ART who are included in at least one DSD model (DB, DT, DS, DA, DD, DCP, DCA, BM, CM, AF, FR, GA, CA, EH, TB, C&T, SAAJ, SMI).");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addSearch("N2", mapStraightThrough(getN2()));
    cd.addSearch("N3", mapStraightThrough(getN3()));
    cd.addSearch("N4", mapStraightThrough(getN4()));
    cd.addSearch("N5", mapStraightThrough(getN5()));
    cd.addSearch("N6", mapStraightThrough(getN6()));
    cd.addSearch("N7", mapStraightThrough(getN7()));
    cd.addSearch("N8", mapStraightThrough(getN8()));
    cd.addSearch("N9", mapStraightThrough(getN9()));
    cd.addSearch("N10", mapStraightThrough(getN10()));
    cd.addSearch("N11", mapStraightThrough(getN11()));
    cd.addSearch("N12", mapStraightThrough(getN12()));
    cd.addSearch("N13", mapStraightThrough(getN13()));
    cd.addSearch("N14", mapStraightThrough(getN14()));
    cd.addSearch("N15", mapStraightThrough(getN15()));
    cd.addSearch("N16", mapStraightThrough(getN16()));
    cd.addSearch("N17", mapStraightThrough(getN17()));
    cd.addSearch("N18", mapStraightThrough(getN18()));
    cd.addSearch("N20", mapStraightThrough(getN20()));
    cd.setCompositionString(
        "(N2 OR N3 OR N4 OR N5 OR N6 OR N7 OR N8 OR N9 OR N10 OR N11 OR N12 OR N13 OR N14 OR N15 OR N16 OR N17 OR N18 OR N20)");
    return cd;
  }

  /**
   * <b>Description:</b> Patients who are registered as pregnant, as breastfeeding or who are on TB
   * treatment
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPregnantAndBreastfeedingAndOnTBTreatment() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("endDate", "After Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.setName("Pregnant, Breastfeeding or on TB Treatment");
    CohortDefinition breastfeeding = txNewCohortQueries.getTxNewBreastfeedingComposition(true);
    CohortDefinition pregnant = txNewCohortQueries.getPatientsPregnantEnrolledOnART(true);
    CohortDefinition tb = commonCohortQueries.getPatientsOnTbTreatment();

    String pregnantMappings = "startDate=${endDate-9m},endDate=${endDate},location=${location}";
    cd.addSearch("pregnant", EptsReportUtils.map(pregnant, pregnantMappings));

    String breastfeedingMappings =
        "onOrAfter=${endDate-18m},onOrBefore=${endDate},location=${location}";
    cd.addSearch("breastfeeding", EptsReportUtils.map(breastfeeding, breastfeedingMappings));

    String tbMappings = "endDate=${endDate},location=${location}";
    cd.addSearch("tb", EptsReportUtils.map(tb, tbMappings));

    cd.setCompositionString("pregnant OR breastfeeding OR tb");

    return cd;
  }

  /**
   * <b>Description:</b> Patients who are registered as pregnant, as breastfeeding
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPregnantAndBreastfeeding() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("endDate", "After Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.setName("Pregnant, Breastfeeding or on TB Treatment");
    CohortDefinition breastfeeding = txNewCohortQueries.getTxNewBreastfeedingComposition(true);
    CohortDefinition pregnant = txNewCohortQueries.getPatientsPregnantEnrolledOnART(true);

    String pregnantMappings = "startDate=${endDate-9m},endDate=${endDate},location=${location}";
    cd.addSearch("pregnant", EptsReportUtils.map(pregnant, pregnantMappings));

    String breastfeedingMappings =
        "onOrAfter=${endDate-18m},onOrBefore=${endDate},location=${location}";
    cd.addSearch("breastfeeding", EptsReportUtils.map(breastfeeding, breastfeedingMappings));

    cd.setCompositionString("pregnant OR breastfeeding");

    return cd;
  }

  /**
   * <b>Description:</b> Patients who are registered as pregnant
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getDSDPregnant() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("endDate", "After Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.setName("Pregnant");
    CohortDefinition pregnant = txNewCohortQueries.getPatientsPregnantEnrolledOnART(true);

    String pregnantMappings = "startDate=${endDate-9m},endDate=${endDate},location=${location}";
    cd.addSearch("pregnant", EptsReportUtils.map(pregnant, pregnantMappings));
    cd.setCompositionString("pregnant");

    return cd;
  }

  /**
   * <b>Description:</b> Patients who are registered as breastfeeding
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getDSDBreastfeeding() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("endDate", "After Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.setName("Breastfeeding");
    CohortDefinition breastfeeding = txNewCohortQueries.getTxNewBreastfeedingComposition(true);

    String breastfeedingMappings =
        "onOrAfter=${endDate-18m},onOrBefore=${endDate},location=${location}";
    cd.addSearch("breastfeeding", EptsReportUtils.map(breastfeeding, breastfeedingMappings));
    cd.setCompositionString("breastfeeding");

    return cd;
  }

  /**
   * <b>Description:</b> Number of patients who are on Sarcoma Karposi
   *
   * <p><b>Techinal Specs</b>
   *
   * <blockquote>
   *
   * <pre>
   * <p>Include patients who have Sarcoma Kaposi <b>(concept_id = 507)</b> registered
   * in the follow-up (Adults <b>(encounterType = 6)</b> and Children <b>(encounterType = 9)</b>))
   * consultation and <b>encounter_datetime <= reporting_end_date</b>
   * </pre>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  private CohortDefinition getAllPatientsOnSarcomaKarposi() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("sarcomaKarposiPatients");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.setQuery(
        DsdQueries.getPatientsOnSarcomaKarposi(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getOtherDiagnosis().getConceptId(),
            hivMetadata.getKaposiSarcomaConcept().getConceptId()));

    return cd;
  }

  /**
   * <b>Description:</b> Filter patients (from 4) who are considered stable according to criteria 5:
   * <b>a, b, c, d, e, f</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoAreStable(Integer atLeastXMonthsOnART) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Patients who are stable");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "A",
        EptsReportUtils.map(
            getPatientsWhoAreStableA(atLeastXMonthsOnART),
            "onOrBefore=${endDate},location=${location}"));
    cd.addSearch(
        "vlLess1000",
        EptsReportUtils.map(
            getPatientsWithViralLoadLessThan1000Within12Months(),
            "endDate=${endDate},location=${location}"));
    cd.addSearch(
        "C",
        EptsReportUtils.map(
            getPatientsWithLastCD4Results(), "endDate=${endDate},location=${location}"));
    cd.addSearch(
        "F",
        EptsReportUtils.map(
            genericCohortQueries.hasCodedObs(
                hivMetadata.getAdverseReaction(),
                BaseObsCohortDefinition.TimeModifier.ANY,
                SetComparator.IN,
                Arrays.asList(
                    hivMetadata.getAdultoSeguimentoEncounterType(),
                    hivMetadata.getPediatriaSeguimentoEncounterType()),
                Arrays.asList(
                    hivMetadata.getPancreatitis(),
                    hivMetadata.getLacticAcidosis(),
                    hivMetadata.getCytopeniaConcept(),
                    hivMetadata.getNephrotoxicityConcept(),
                    hivMetadata.getHepatitisConcept(),
                    hivMetadata.getStevensJonhsonSyndromeConcept(),
                    hivMetadata.getHypersensitivityToAbcOrRailConcept(),
                    hivMetadata.getHepaticSteatosisWithHyperlactataemiaConcept())),
            "onOrAfter=${endDate-6m},onOrBefore=${endDate},locationList=${location}"));
    cd.addSearch(
        "patientsWithViralLoad",
        EptsReportUtils.map(
            hivCohortQueries.getPatientsViralLoadWithin12Months(),
            "endDate=${endDate},location=${location}"));
    cd.setCompositionString("A AND (vlLess1000 OR C) AND NOT F");

    return cd;
  }

  /**
   * <b>Name: 5A</b>
   *
   * <p><b>Description:</b> Patients on ART for at least 12 months <b>(if patients age >=2 and
   * <=9)</b> or On ART for at least 6 months <b>(if patients age >=10)</b>
   *
   * <p><b>Technical Specs</b>
   * <blockquote>
   * <pre>
   * <p>On ART for at least x months means: <b>(patient_art_initiation date–
   * reporting end date) >= x months</b>
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  private CohortDefinition getPatientsWhoAreStableA(Integer atLeastXMonthsOnART) {
    CalculationCohortDefinition cd =
        new CalculationCohortDefinition(
            "onArtAtleastXmonths",
            Context.getRegisteredComponents(OnArtForAtleastXmonthsCalculation.class).get(0));
    cd.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addCalculationParameter("atLeastXMonthsOnART", atLeastXMonthsOnART);
    return cd;
  }

  /**
   * <b>Name: 5C</b>
   *
   * <p><b>Description:</b> One CD4 Lab result > 750 cels/mm3 or > 15% in last ART year <b>(if
   * patients age >=2 and <=4)</b> or One CD4 result > 200 cels/mm3 in last ART year <b>(if patients
   * age >=5 and <=9)</b> 5C (i) 5C (ii)
   *
   * @return {@link CohortDefinition}
   */
  private CohortDefinition getCD4CountAndCD4PercentCombined() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Only if VL does not exist");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "CI",
        EptsReportUtils.map(
            getCD4CountAndCD4Percent1(),
            "startDate=${endDate-12m},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "CII",
        EptsReportUtils.map(
            getCD4CountAndCD4Percent2(),
            "startDate=${endDate-12m},endDate=${endDate},location=${location}"));

    cd.setCompositionString("(CI OR CII)");

    return cd;
  }

  /**
   * if VL does not exist: verify CD4 Results. 1. CD4 result > 750 cells/mm3 or > 15% in last ART
   * year (if patients age >=2 and <=4) 2. CD4 result > 200 cells/mm3 in last ART year (if patients
   * age >=5 and <=9)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getCD4CountAndCD4PercentAndCd4Quantitative(
      int cd4_absolute, boolean age2to4) {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patient with last CD4 result without VL Result ");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("730", hivMetadata.getCD4PercentConcept().getConceptId());
    map.put("165515", hivMetadata.getCD4SemiQuantitativeConcept().getConceptId());
    map.put("1254", hivMetadata.getCD4CountGreaterThan200Concept().getConceptId());
    map.put("cd4_absolute", cd4_absolute);

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "WHERE  p.patient_id NOT IN (SELECT vl_max.patient_id "
            + "                            FROM   (SELECT vl.patient_id, "
            + "                                           Max(vl.latest_date) max_date "
            + "                                    FROM   (SELECT p.patient_id, "
            + "                                                   Max(DATE(e.encounter_datetime)) "
            + "                                                       latest_date "
            + "                                            FROM   patient p "
            + "                                                       INNER JOIN encounter e "
            + "                                                                  ON p.patient_id = "
            + "                                                                     e.patient_id "
            + "                                                       INNER JOIN obs o "
            + "                                                                  ON e.encounter_id = "
            + "                                                                     o.encounter_id "
            + "                                            WHERE  e.encounter_type IN ( ${6}, ${9}, "
            + "                                                                         ${13}, ${51} ) "
            + "                                              AND o.concept_id IN ( ${856}, "
            + "                                                                    ${1305} ) "
            + "                                              AND DATE(e.encounter_datetime) BETWEEN "
            + "                                                Date_add(:endDate, "
            + "                                                         INTERVAL -12 month) AND "
            + "                                                :endDate "
            + "                                              AND e.location_id = :location "
            + "                                              AND p.voided = 0 "
            + "                                              AND e.voided = 0 "
            + "                                              AND o.voided = 0 "
            + "                                            GROUP  BY p.patient_id "
            + "                                            UNION "
            + "                                            SELECT p.patient_id, "
            + "                                                   Max(o.obs_datetime) "
            + "                                                       latest_date "
            + "                                            FROM   patient p "
            + "                                                       INNER JOIN encounter e "
            + "                                                                  ON p.patient_id = "
            + "                                                                     e.patient_id "
            + "                                                       INNER JOIN obs o "
            + "                                                                  ON o.encounter_id = "
            + "                                                                     e.encounter_id "
            + "                                            WHERE  e.encounter_type = ${53} "
            + "                                              AND o.concept_id IN ( ${856}, "
            + "                                                                    ${1305} ) "
            + "                                              AND o.obs_datetime BETWEEN "
            + "                                                Date_add( "
            + "                                                        :endDate, "
            + "                                                        INTERVAL "
            + "                                                        -12 month "
            + "                                                ) "
            + "                                                AND "
            + "                                                :endDate "
            + "                                              AND e.location_id = :location "
            + "                                              AND p.voided = 0 "
            + "                                              AND e.voided = 0 "
            + "                                              AND o.voided = 0 "
            + "                                            GROUP  BY p.patient_id) vl "
            + "                                    GROUP  BY vl.patient_id) vl_max "
            + "                                       INNER JOIN encounter e "
            + "                                                  ON e.patient_id = vl_max.patient_id "
            + "                                       INNER JOIN obs o "
            + "                                                  ON o.encounter_id = e.encounter_id "
            + "                            WHERE  e.voided = 0 "
            + "                              AND o.voided = 0 "
            + "                              AND ( ( o.concept_id = ${856} "
            + "                                AND o.value_numeric IS NOT NULL ) "
            + "                                OR ( o.concept_id = ${1305} "
            + "                                    AND o.value_coded IS NOT NULL ) ) "
            + "                              AND e.location_id = :location "
            + "                              AND ( ( e.encounter_type IN ( ${6}, ${9}, ${13}, ${51} ) "
            + "                                AND DATE(e.encounter_datetime) "
            + "                                          BETWEEN "
            + "                                          Date_add(:endDate, "
            + "                                                   INTERVAL -12 month) "
            + "                                          AND :endDate "
            + "                                AND DATE(e.encounter_datetime) = "
            + "                                    vl_max.max_date ) "
            + "                                OR ( e.encounter_type = ${53} "
            + "                                    AND o.obs_datetime BETWEEN "
            + "                                         Date_add( "
            + "                                                 :endDate, "
            + "                                                 INTERVAL -12 month) "
            + "                                         AND "
            + "                                         :endDate "
            + "                                    AND o.obs_datetime = "
            + "                                        vl_max.max_date ) ) "
            + "                            ORDER  BY vl_max.patient_id) "
            + "  AND p.patient_id IN (SELECT cd4_max.patient_id "
            + "                       FROM   (SELECT cd4.patient_id, "
            + "                                      Max(cd4.latest_date) max_date "
            + "                               FROM   (SELECT p.patient_id, "
            + "                                              Max(DATE(e.encounter_datetime)) "
            + "                                                  latest_date "
            + "                                       FROM   patient p "
            + "                                                  INNER JOIN encounter e "
            + "                                                             ON p.patient_id = "
            + "                                                                e.patient_id "
            + "                                                  INNER JOIN obs o "
            + "                                                             ON e.encounter_id = "
            + "                                                                o.encounter_id "
            + "                                       WHERE  e.encounter_type IN ( ${6}, ${9}, "
            + "                                                                    ${13}, ${51} ) "
            + "                                         AND o.concept_id IN ( "
            + "                                                              ${1695}, ${730}, ${165515} "
            + "                                           ) "
            + "                                         AND DATE(e.encounter_datetime) "
            + "                                           BETWEEN "
            + "                                           Date_add(:endDate, "
            + "                                                    INTERVAL -12 month) AND "
            + "                                           :endDate "
            + "                                         AND e.location_id = :location "
            + "                                         AND p.voided = 0 "
            + "                                         AND e.voided = 0 "
            + "                                         AND o.voided = 0 "
            + "                                       GROUP  BY p.patient_id "
            + "                                       UNION "
            + "                                       SELECT p.patient_id, "
            + "                                              Max(o.obs_datetime) "
            + "                                                  latest_date "
            + "                                       FROM   patient p "
            + "                                                  INNER JOIN encounter e "
            + "                                                             ON p.patient_id = "
            + "                                                                e.patient_id "
            + "                                                  INNER JOIN obs o "
            + "                                                             ON o.encounter_id = "
            + "                                                                e.encounter_id "
            + "                                       WHERE  e.encounter_type = ${53} "
            + "                                         AND o.concept_id IN ( "
            + "                                                              ${1695}, ${730}, ${165515} "
            + "                                           ) "
            + "                                         AND o.obs_datetime BETWEEN "
            + "                                           Date_add( "
            + "                                                   :endDate, "
            + "                                                   INTERVAL -12 month "
            + "                                           ) AND "
            + "                                           :endDate "
            + "                                         AND e.location_id = :location "
            + "                                         AND p.voided = 0 "
            + "                                         AND e.voided = 0 "
            + "                                         AND o.voided = 0 "
            + "                                       GROUP  BY p.patient_id) cd4 "
            + "                               GROUP  BY cd4.patient_id) cd4_max "
            + "                                  INNER JOIN encounter ee "
            + "                                             ON ee.patient_id = cd4_max.patient_id "
            + "                                  INNER JOIN obs oo "
            + "                                             ON oo.encounter_id = ee.encounter_id "
            + "                       WHERE  ee.voided = 0 "
            + "                         AND oo.voided = 0 ";
    query +=
        age2to4
            ? "                         AND ( ( oo.concept_id = ${1695} "
                + "                           AND oo.value_numeric > ${cd4_absolute} ) "
                + "                           OR ( oo.concept_id = ${730} "
                + "                               AND oo.value_numeric > 15 ) ) "
            : "                         AND ( ( oo.concept_id = ${1695} "
                + "                           AND oo.value_numeric > ${cd4_absolute} ) "
                + "                           OR ( oo.concept_id = ${165515} "
                + "                               AND oo.value_coded = ${1254} ) ) ";
    query +=
        "                         AND ee.location_id = :location "
            + "                         AND ( ( ee.encounter_type IN ( ${6}, ${9}, ${13}, ${51} ) "
            + "                           AND DATE(ee.encounter_datetime) "
            + "                                     BETWEEN "
            + "                                     Date_add(:endDate, "
            + "                                              INTERVAL -12 month) AND :endDate "
            + "                           AND DATE(ee.encounter_datetime) = "
            + "                               cd4_max.max_date "
            + "                                   ) "
            + "                           OR ( ee.encounter_type = ${53} "
            + "                               AND oo.obs_datetime BETWEEN "
            + "                                    Date_add( "
            + "                                            :endDate, "
            + "                                            INTERVAL -12 month) "
            + "                                    AND "
            + "                                    :endDate "
            + "                               AND oo.obs_datetime = "
            + "                                   cd4_max.max_date ) "
            + "                           ) "
            + "                       ORDER  BY cd4_max.patient_id)";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  public CohortDefinition getCD4CountAnd2To4Age() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Patients 2 to 4 years of age with CD4 Count and no VL");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "Cd4Result",
        EptsReportUtils.map(
            getCD4CountAndCD4PercentAndCd4Quantitative(750, true),
            "endDate=${endDate},location=${location}"));
    cd.addSearch(
        "Age",
        EptsReportUtils.map(
            ageCohortQueries.createXtoYAgeCohort("2-4", 2, 4), "effectiveDate=${endDate}"));

    cd.setCompositionString("Cd4Result AND Age");

    return cd;
  }

  public CohortDefinition getCD4CountAnd5To9Age() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Patients 2 to 4 years of age with CD4 Count and no VL");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "Cd4Result",
        EptsReportUtils.map(
            getCD4CountAndCD4PercentAndCd4Quantitative(200, false),
            "endDate=${endDate},location=${location}"));
    cd.addSearch(
        "Age",
        EptsReportUtils.map(
            ageCohortQueries.createXtoYAgeCohort("5-9", 5, 9), "effectiveDate=${endDate}"));

    cd.setCompositionString("Cd4Result AND Age");

    return cd;
  }

  /**
   * Only if VL does not exist: Last CD4 absolute result > 750 cells/mm3 or CD4 percentuagel > 15%
   * in last ART year (if patients age >=2 and <=4) by reporting end date Last CD4 absolute or
   * semi-quantitative result absolute or semi-quantitative > 200 cells/mm3 in last ART year (if
   * patients age >=5 and <=9) by reporting end date
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithLastCD4Results() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Only if VL does not exist");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "CI",
        EptsReportUtils.map(getCD4CountAnd2To4Age(), "endDate=${endDate},location=${location}"));
    cd.addSearch(
        "CII",
        EptsReportUtils.map(getCD4CountAnd5To9Age(), "endDate=${endDate},location=${location}"));

    cd.setCompositionString("(CI OR CII)");

    return cd;
  }

  /**
   * <b>Name: 5C (i)</b>
   *
   * <p><b>Description:</b> One CD4 Lab result > 750 cels/mm3 or > 15% in last ART year (if patients
   * age >=2 and <=4)
   *
   * <p><b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <ol>
   *   <li>Get the most recent encounter between A and B: <div>A. The last Encounter of type 6,9,13
   *       or 51 occurred (Encounter_date) between reporting end date and (reporting end date – 12
   *       months) which contains one of the following concept:
   *       <p>
   *       <ul>
   *         <li>CD4 Abs Result <b>(Concept id 1695)</b> or
   *         <li>CD4 % Result <b>(Concept id 730)</b>
   *       </ul>
   *       <div>B. The last obs.datetime of obs concept id 1695 occurred between reporting end date
   *       and (reporting end date – 12 months) recorded in Encounter of type 53.
   *   <li>Check If the most recent encounter between A and B contains:
   *       <ul>
   *         <li>CD4 Abs Result <b>(Concept id 1695) >750</b> or
   *         <li>CD4 % Result <b>(Concept id 730) >15%</b>
   *       </ul>
   * </ol>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  private CohortDefinition getCD4CountAndCD4Percent1() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("CD4CountAndCD4percent");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "Cd4Abs",
        EptsReportUtils.map(
            genericCohortQueries.hasNumericObs(
                hivMetadata.getCD4AbsoluteOBSConcept(),
                BaseObsCohortDefinition.TimeModifier.LAST,
                RangeComparator.GREATER_THAN,
                750.0,
                null,
                null,
                Arrays.asList(
                    hivMetadata.getAdultoSeguimentoEncounterType(),
                    hivMetadata.getPediatriaSeguimentoEncounterType(),
                    hivMetadata.getMisauLaboratorioEncounterType(),
                    hivMetadata.getFsrEncounterType(),
                    hivMetadata.getMasterCardEncounterType())),
            "onOrAfter=${endDate-12m},onOrBefore=${endDate},locationList=${location}"));
    cd.addSearch(
        "Cd4Percent",
        EptsReportUtils.map(
            genericCohortQueries.hasNumericObs(
                hivMetadata.getCD4PercentConcept(),
                BaseObsCohortDefinition.TimeModifier.LAST,
                RangeComparator.GREATER_THAN,
                15.0,
                null,
                null,
                Arrays.asList(
                    hivMetadata.getAdultoSeguimentoEncounterType(),
                    hivMetadata.getPediatriaSeguimentoEncounterType(),
                    hivMetadata.getMisauLaboratorioEncounterType(),
                    hivMetadata.getFsrEncounterType(),
                    hivMetadata.getMasterCardEncounterType())),
            "onOrAfter=${endDate-12m},onOrBefore=${endDate},locationList=${location}"));
    cd.addSearch(
        "Age",
        EptsReportUtils.map(
            ageCohortQueries.createXtoYAgeCohort("2-4", 2, 4), "effectiveDate=${endDate}"));

    cd.setCompositionString("(Cd4Abs OR Cd4Percent) AND Age");

    return cd;
  }

  /**
   * <b>Name: 5C (ii)</b>
   *
   * <p><b>Description:</b> One CD4 result > 200 cels/mm3 in last ART year (if patients age >=5
   * years)
   *
   * @return {@link CohortDefinition}
   */
  private CohortDefinition getCD4CountAndCD4Percent2() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "5C (ii) One CD4 result > 200 cels/mm3 in last ART year (if patients age >=5 years)");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "Age",
        EptsReportUtils.map(
            ageCohortQueries.createXtoYAgeCohort("greaterThan5", 5, 9),
            "effectiveDate=${endDate}"));

    cd.addSearch(
        "CD4CountAndCD4Percent2Part1",
        EptsReportUtils.map(
            getCD4CountAndCD4Percent2Part1(), "endDate=${endDate},location=${location}"));

    cd.setCompositionString("CD4CountAndCD4Percent2Part1 AND Age");

    return cd;
  }

  private CohortDefinition getCD4CountAndCD4Percent2Part1() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("LAST CD4 result > 200 cels/mm3 in last ART year (if patients age >=5");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());

    String query =
        "SELECT  cd4_max.patient_id "
            + "FROM "
            + "( "
            + "    SELECT  most_recent.patient_id, MAX(most_recent.last_date) cd4_max_date "
            + "    FROM "
            + "    ( "
            + "        SELECT p.patient_id, MAX(e.encounter_datetime) last_date "
            + "        FROM  patient p "
            + "            INNER JOIN encounter e  "
            + "                ON e.patient_id=p.patient_id "
            + "            INNER JOIN obs o  "
            + "                ON o.encounter_id=e.encounter_id "
            + "        WHERE  "
            + "            e.encounter_type IN (${6},${9},${13},${51})   "
            + "            AND  o.concept_id IN (${1695})   "
            + "            AND e.encounter_datetime   "
            + "                    BETWEEN date_add(date_add( :endDate, interval -12 MONTH), interval 1 day)  "
            + "                        AND  :endDate  "
            + "            AND e.location_id=   :location   "
            + "            AND o.voided = 0  "
            + "            AND e.voided = 0 "
            + "            AND p.voided = 0 "
            + "        GROUP BY p.patient_id "
            + "        UNION "
            + "        SELECT p.patient_id, MAX(o.obs_datetime) latest_date    "
            + "        FROM patient p   "
            + "            INNER JOIN encounter e   "
            + "                    ON p.patient_id=e.patient_id   "
            + "            INNER JOIN obs o   "
            + "                    ON o.encounter_id=e.encounter_id   "
            + "        WHERE e.encounter_type=${53}  "
            + "            AND o.concept_id = ${1695}   "
            + "            AND  o.obs_datetime   "
            + "                        BETWEEN date_add(date_add( :endDate, interval -12 MONTH), interval 1 day)  "
            + "                            AND  :endDate   "
            + "            AND e.location_id=   :location   "
            + "            AND p.voided=0   "
            + "            AND e.voided=0   "
            + "            AND o.voided=0   "
            + "        GROUP BY p.patient_id "
            + "    )most_recent "
            + "    GROUP BY most_recent.patient_id "
            + ")cd4_max "
            + "    INNER JOIN encounter e  "
            + "        ON e.patient_id = cd4_max.patient_id  "
            + "    INNER JOIN obs o  "
            + "        ON o.encounter_id = e.encounter_id  "
            + "WHERE  e.voided=0   "
            + "    AND o.voided=0   "
            + "    AND "
            + "    (  "
            + "        (o.concept_id IN (${1695}) AND o.value_numeric > 200)  "
            + "          "
            + "    )   "
            + "    AND e.location_id=  :location   "
            + "    AND  "
            + "    ( "
            + "        (e.encounter_type IN (${6},${9},${13},${51})  "
            + "            AND e.encounter_datetime   "
            + "                BETWEEN date_add(date_add( :endDate, interval -12 MONTH), interval 1 day) AND  :endDate "
            + "            AND e.encounter_datetime = cd4_max.cd4_max_date  "
            + "        ) "
            + "        OR  "
            + "        (e.encounter_type = ${53} "
            + "            AND o.obs_datetime     "
            + "                BETWEEN date_add(date_add( :endDate, interval -12 MONTH), interval 1 day) AND  :endDate "
            + "            AND o.obs_datetime = cd4_max.cd4_max_date  "
            + "        ) "
            + "    )  "
            + " "
            + "ORDER BY cd4_max.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    String replaceQuery = sb.replace(query);

    cd.setQuery(replaceQuery);

    return cd;
  }

  private CohortDefinition getPatientsWithViralLoadLessThan1000Within12Months() {
    SqlCohortDefinition cd = new SqlCohortDefinition();

    cd.setName(
        "Patients with LAST Viral Load Result < 1000 copies/ml in last ART year (only if VL exists)");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put(
        "adultoSeguimento", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put(
        "pediatriaSeguimento",
        hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put(
        "misauLaboratorio", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("fsr", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    map.put("masterCard", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("hivViralLoad", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("hivViralLoadQualitative", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        " SELECT vl_max.patient_id "
            + "FROM "
            + "( "
            + "SELECT vl.patient_id, MAX(vl.latest_date) max_date "
            + "FROM "
            + "(  "
            + "        SELECT p.patient_id, MAX(e.encounter_datetime) latest_date  "
            + "        FROM patient p  "
            + "            INNER JOIN encounter e  "
            + "                    ON p.patient_id=e.patient_id  "
            + "            INNER JOIN obs o  "
            + "                    ON e.encounter_id=o.encounter_id  "
            + "        WHERE e.encounter_type IN (${adultoSeguimento},${pediatriaSeguimento},${misauLaboratorio},${fsr} )  "
            + "            AND o.concept_id IN (${hivViralLoad},${hivViralLoadQualitative} )  "
            + "            AND CAST(e.encounter_datetime AS DATE)  "
            + "                        BETWEEN date_add(:endDate, interval -12 MONTH) AND :endDate "
            + "            AND e.location_id=  :location "
            + "            AND p.voided=0  "
            + "            AND e.voided=0  "
            + "            AND o.voided=0  "
            + "        GROUP BY p.patient_id  "
            + "        UNION  "
            + "        SELECT p.patient_id, MAX(o.obs_datetime) latest_date   "
            + "        FROM patient p  "
            + "            INNER JOIN encounter e  "
            + "                    ON p.patient_id=e.patient_id  "
            + "            INNER JOIN obs o  "
            + "                    ON o.encounter_id=e.encounter_id  "
            + "        WHERE e.encounter_type=${masterCard} "
            + "            AND o.concept_id IN (${hivViralLoad},${hivViralLoadQualitative} )  "
            + "            AND  o.obs_datetime  "
            + "                        BETWEEN date_add(:endDate, interval -12 MONTH) AND :endDate  "
            + "            AND e.location_id=  :location  "
            + "            AND p.voided=0  "
            + "            AND e.voided=0  "
            + "            AND o.voided=0  "
            + "        GROUP BY p.patient_id"
            + "		) vl "
            + " GROUP BY   vl.patient_id "
            + ") vl_max"
            + "        INNER JOIN encounter e "
            + "            ON e.patient_id = vl_max.patient_id "
            + "        INNER JOIN obs o "
            + "            ON o.encounter_id = e.encounter_id "
            + "    WHERE  e.voided=0  "
            + "        AND o.voided=0  "
            + "        AND( "
            + "                (o.concept_id=${hivViralLoad} AND o.value_numeric < 1000) "
            + "                OR "
            + "                (o.concept_id=${hivViralLoadQualitative} AND o.value_coded IS NOT NULL) "
            + "            )  "
            + "        AND e.location_id= :location "
            + "AND (  "
            + "                                       (e.encounter_type IN (${adultoSeguimento},${pediatriaSeguimento},${misauLaboratorio},${fsr})   "
            + "                                             AND CAST(e.encounter_datetime AS DATE)   "
            + "                        BETWEEN date_add(:endDate, interval -12 MONTH) AND :endDate  "
            + "                                           AND e.encounter_datetime = vl_max.max_date )  "
            + "                           OR   "
            + "                                       (e.encounter_type =${masterCard}  "
            + "                                             AND o.obs_datetime      "
            + "                        BETWEEN date_add(:endDate, interval -12 MONTH) AND :endDate  "
            + "                                             AND o.obs_datetime = vl_max.max_date       )  "
            + "                                 )  "
            + "        "
            + "   ORDER BY vl_max.patient_id ";

    StringSubstitutor sb = new StringSubstitutor(map);
    String replaceQuery = sb.replace(query);
    cd.setQuery(replaceQuery);

    return cd;
  }
  /**
   * <b>Description:</b> Patients who have X given Modos de Dispensa on Y given Encounter Types
   *
   * @param encounterTypes
   * @param typesOfDispensation
   * @return
   */
  private CohortDefinition getPatientsWhoHaveModoDeDispensa(
      List<Integer> encounterTypes, List<Integer> typesOfDispensation) {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("patientsWithQuarterlyTypeOfDispensation");
    cd.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.setQuery(
        GenericCohortQueries.getLastCodedObservationBeforeDate(
            encounterTypes,
            hivMetadata.getTypeOfDispensationConcept().getConceptId(),
            typesOfDispensation));
    return cd;
  }

  /**
   * <b>Description:</b> Patients who have Modo de Dispensa with value <b>FARMAC/Farmacia
   * Privada</b> in the most recent pick-up date in FILA by reporting end date
   *
   * @return {@link CohortDefinition}
   */
  private CohortDefinition getPatientsWithFarmacTypeOfDispensation() {
    CodedObsCohortDefinition cd = new CodedObsCohortDefinition();
    cd.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addEncounterType(hivMetadata.getARVPharmaciaEncounterType());
    cd.setTimeModifier(BaseObsCohortDefinition.TimeModifier.LAST);
    cd.setQuestion(hivMetadata.getTypeOfDispensationConcept());
    cd.setOperator(SetComparator.IN);
    cd.addValue(hivMetadata.getLastRecordOfFarmacConcept());
    return cd;
  }

  /**
   * <b>Description:</b> Patients who have Modo de Dispensa with value <b>FARMAC/Farmacia
   * Privada</b> in the most recent pick-up date in FILA by reporting end date
   *
   * @return {@link CohortDefinition}
   */
  private CohortDefinition getPatientsWithGivenTypeOfDispensation(Concept answerConcept) {
    CodedObsCohortDefinition cd = new CodedObsCohortDefinition();
    cd.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addEncounterType(hivMetadata.getARVPharmaciaEncounterType());
    cd.setTimeModifier(BaseObsCohortDefinition.TimeModifier.LAST);
    cd.setQuestion(hivMetadata.getTypeOfDispensationConcept());
    cd.setOperator(SetComparator.IN);
    cd.addValue(answerConcept);
    return cd;
  }

  /**
   * DSD_FR10
   *
   * <p><b>Description:</b> “Interruption In Treatment for <3 months” will have the following
   * combination: ((A OR B) AND C1) AND NOT DEAD AND NOT TRANSFERRED OUT AND NOT REFUSED Patients
   * who returned to treatment as follows: From all patients currently on ART by reporting end date
   * (DSD_FR9), the system will include:
   *
   * <blockquote>
   *
   * <ol>
   *   <li>Patients who experienced interruption in treatment 3 months before reporting end date
   *       (report end date – 3 months) (DSD_FR11) and
   *   <li>Patients who have at least one drug-pick up registered in FILA or Ficha Recepção -
   *       Levantou ARV in the last 3 months (report end date – 3 months and report end date)
   * </ol>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoReturned() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("DSD_FR10 Patients who returned to treatment");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "treatmentInterruption",
        EptsReportUtils.map(
            getPatientsWhoExperiencedInterruptionInTreatment(),
            "endDate=${endDate-3m},location=${location}"));

    cd.addSearch(
        "filaOrDrugPickup",
        EptsReportUtils.map(getFilaOrDrugPickup(), "endDate=${endDate},location=${location}"));
    cd.addSearch(
        "transferredIn",
        EptsReportUtils.map(
            DsdQueries.getTranferredInPatients(),
            "onOrAfter=${endDate-3m},onOrBefore=${endDate},location=${location}"));

    cd.addSearch(
        "B13",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13(),
            "endDate=${endDate},location=${location}"));

    cd.setCompositionString(
        "(B13 and treatmentInterruption AND filaOrDrugPickup) AND NOT transferredIn");

    return cd;
  }

  /**
   * DSD_FR11
   *
   * <p><b>Description:</b> Patients who experienced interruption in treatment
   *
   * <blockquote>
   *
   * <ol>
   *   <li>All patients having the most recent date (by reporting period end date-3 months) between
   *       last scheduled drug pickup date registered on their last drug pick-up registered in FILA
   *       and 30 days after the last ART pickup date registered in Ficha Recepção – Levantou ARV
   *       and adding 59 days and this date being less than reporting period end date-3 months
   *   <li>All patients who do not have the next scheduled drug pick up date registered in FILA and
   *       any ART Pickup date registered in Ficha Recepção – Levantou ARV, by reporting period end
   *       date-3 months.
   * </ol>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoExperiencedInterruptionInTreatment() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("DSD_FR11 Patients who experienced interruption in treatment");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "interruption3Months",
        EptsReportUtils.map(
            DsdQueries.getPatientsWhoExperiencedInterruptionIn3MonthsBeforeReportingEndDate(
                hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId(),
                hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId(),
                hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId(),
                hivMetadata.getArtDatePickupMasterCard().getConceptId()),
            "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "nextScheduledPickUpDate",
        EptsReportUtils.map(
            DsdQueries.getNextScheduledPickUpDate(), "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "anyArtPickup",
        EptsReportUtils.map(
            DsdQueries.getAnyArtPickup(), "endDate=${endDate},location=${location}"));

    cd.setCompositionString("interruption3Months OR (anyArtPickup AND nextScheduledPickUpDate)");

    return cd;
  }

  /**
   * DSD_FR10 bullet 2 Patients who have at least one drug-pick up registered in FILA OR Ficha
   * Recepção - Levantou ARV in the last 3 months (report end date – 3 months and report end date)
   *
   * @return CohortDefinition
   */
  public CohortDefinition getFilaOrDrugPickup() {
    String dsdMappings = "startDate=${endDate-3m},endDate=${endDate},location=${location}";

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition fila =
        txRttCohortQueries.getPatientsWithFilaOrFichaOrMasterCardPickup(
            Arrays.asList(hivMetadata.getARVPharmaciaEncounterType()));

    CohortDefinition drugPickUp =
        txRttCohortQueries.getPatientsWithFilaOrFichaOrMasterCardPickup(
            Arrays.asList(hivMetadata.getMasterCardDrugPickupEncounterType()),
            hivMetadata.getArtPickupConcept(),
            hivMetadata.getYesConcept(),
            hivMetadata.getArtDatePickupMasterCard());

    cd.addSearch("fila", EptsReportUtils.map(fila, dsdMappings));

    cd.addSearch("drugPickUp", EptsReportUtils.map(drugPickUp, dsdMappings));

    cd.setCompositionString("fila OR drugPickUp");

    return cd;
  }
}
