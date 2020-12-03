package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QualityImprovement2020CohortQueries {

  private GenericCohortQueries genericCohortQueries;

  private HivMetadata hivMetadata;

  private CommonMetadata commonMetadata;

  private GenderCohortQueries genderCohortQueries;

  private AgeCohortQueries ageCohortQueries;

  private ResumoMensalCohortQueries resumoMensalCohortQueries;

  private CommonCohortQueries commonCohortQueries;

  private final String MAPPING = "startDate=${startDate},endDate=${endDate},location=${location}";

  @Autowired
  public QualityImprovement2020CohortQueries(
      GenericCohortQueries genericCohortQueries,
      HivMetadata hivMetadata,
      CommonMetadata commonMetadata,
      ResumoMensalCohortQueries resumoMensalCohortQueries,
      CommonCohortQueries commonCohortQueries,
      AgeCohortQueries ageCohortQueries,
      GenderCohortQueries genderCohortQueries) {
    this.genericCohortQueries = genericCohortQueries;
    this.hivMetadata = hivMetadata;
    this.commonMetadata = commonMetadata;
    this.ageCohortQueries = ageCohortQueries;
    this.genderCohortQueries = genderCohortQueries;
    this.resumoMensalCohortQueries = resumoMensalCohortQueries;
    this.commonCohortQueries = commonCohortQueries;
  }

  /**
   * <b>MQC3D1</b>: Melhoria de Qualidade Category 3 Deniminator 1 <br>
   * <i> A and not B</i> <br>
   *
   * <ul>
   *   <li>A - Select all patients who initiated ART during the Inclusion Period (startDateRevision
   *       and endDateInclusion)
   *   <li>B - Exclude all transferred in patients as following:
   *       <ul>
   *         <li>All patients registered in Ficha Resumo (Encounter Type Id= 53) and marked as
   *             Transferred-in (“Transfer from other facility” concept Id 1369 = “Yes” concept id
   *             1065) in TARV (“Type of Patient Transferred from” concept id 6300 = “ART” concept
   *             id 6276)
   *             <p>< Note: the both concepts 1369 and 6300 should be recorded in the same encounter
   *       </ul>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQC3D1() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName("Melhoria de Qualidade Category 3 Denomirator");
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Date.class));

    CohortDefinition startedART = this.genericCohortQueries.getStartedArtOnPeriod(false, true);

    CohortDefinition transferredIn = getTransferredInPatients();

    compositionCohortDefinition.addSearch(
        "A",
        EptsReportUtils.map(
            startedART, "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch("B", EptsReportUtils.map(transferredIn, MAPPING));

    compositionCohortDefinition.setCompositionString("A AND NOT B");

    return compositionCohortDefinition;
  }

  private CohortDefinition getTransferredInPatients() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("transferred in patients");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Date.class));

    Map<String, Integer> map = new HashMap<>();
    map.put(
        "masterCardEncounterType", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put(
        "transferFromOtherFacilityConcept",
        commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("patientFoundYesConcept", hivMetadata.getPatientFoundYesConcept().getConceptId());
    map.put(
        "typeOfPatientTransferredFrom",
        hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("artStatus", hivMetadata.getArtStatus().getConceptId());

    String query =
        "SELECT  p.patient_id "
            + "FROM patient p "
            + "    INNER JOIN encounter e "
            + "        ON e.patient_id=p.patient_id "
            + "    INNER JOIN obs obs1 "
            + "        ON obs1.encounter_id=e.encounter_id "
            + "    INNER JOIN obs obs2 "
            + "        ON obs2.encounter_id=e.encounter_id "
            + "WHERE p.voided =0  "
            + "    AND e.voided = 0 "
            + "    AND obs1.voided =0 "
            + "    AND obs2.voided =0 "
            + "    AND e.encounter_type = ${masterCardEncounterType}  "
            + "    AND obs1.obs_datetime BETWEEN :startDate AND :endDate "
            + "    AND obs2.obs_datetime BETWEEN :startDate AND :endDate "
            + "    AND e.location_id = :location "
            + "    AND obs1.concept_id = ${transferFromOtherFacilityConcept} AND obs1.value_coded = ${patientFoundYesConcept} "
            + "    AND obs2.concept_id = ${typeOfPatientTransferredFrom} AND obs2.value_coded = ${artStatus} ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   *
   *
   * <h4>Categoria 3 Denominador </h4>
   *
   * <ul>
   *   <li>Select all patients From Categoria 3 Denominador:
   *   <li>C: Filter all adults patients who have the first clinical consultation between Diagnosis
   *       Date and Diagnosis Date+7days as following:
   *       <ul>
   *         <li>all patients who haC: Filter all adults patients who have the first clinical
   *             consultation between Diagnosis Date and Diagnosis Date+7days as following:ve who
   *             have the first consultation registered in “Ficha Clinica” (encounter type 6) after
   *             “Data Diagnostico” with the following conditions:
   *             <ul>
   *               <li>[encounter type 6] “Data da consulta” (encounter datetime) >= [encounter type
   *                   53] “Data Diagnóstico” (concept_id 22772 obs datetime) and <= [encounter type
   *                   53] “Data Diagnóstico” (concept_id 22772 obs datetime) + 7 days
   *             </ul>
   *       </ul>
   *   <li>D: Filter all child patients who have the first clinical consultation between Diagnosis
   *       Date and Diagnosis Date+7days as following:
   *       <ul>
   *         <li>all patients who have the first consultation registered in “Ficha Clinica”
   *             (encounter type 6) after “Data Diagnostico” as following:
   *             <ul>
   *               <li>
   *                   <p>Select the oldest date between “Data Diagnóstico” (concept_id 23807 obs
   *                   datetime, encounter type 53) and “Data Diagnóstico Presuntivo” (concept_id
   *                   22772 obs datetime, encounter type 53)
   *                   <p>(Note: if “Data Diagnóstico” is empty then consider “Data Diagnóstico
   *                   Presuntivo” if it exists. If “Data Diagnostico” is empty then consider “Data
   *                   Diagnostico Presuntivo” if it exists).
   *             </ul>
   *         <li>And the first consultation [encounter type 6] “Data da consulta” (encounter
   *             datetime) >= [encounter type 53] the oldest “Data Diagnóstico” minus the oldest
   *             “Data Diagnóstico” is >=0 and <=7 days
   *       </ul>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQC3N1() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName("Melhoria de Qualidade Category 3 Numerator");

    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Date.class));

    compositionCohortDefinition.addSearch(
        "CAT3DEN", EptsReportUtils.map(this.getMQC3D1(), MAPPING));

    compositionCohortDefinition.addSearch("C", EptsReportUtils.map(this.getCFromMQC3N1(), MAPPING));
    compositionCohortDefinition.addSearch("D", EptsReportUtils.map(this.getDFromMQC3N1(), MAPPING));

    compositionCohortDefinition.setCompositionString("CAT3DEN AND (C OR D)");

    return compositionCohortDefinition;
  }

  private CohortDefinition getCFromMQC3N1() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName(
        "Melhoria de Qualidade Category 3 Numerator C part compposition");

    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Date.class));

    compositionCohortDefinition.addSearch("C", EptsReportUtils.map(getCqueryFromCat3(), MAPPING));
    compositionCohortDefinition.addSearch(
        "ADULTS",
        EptsReportUtils.map(
            this.ageCohortQueries.createXtoYAgeCohort("adults", 15, 200),
            "effectiveDate=${endDate}"));

    compositionCohortDefinition.setCompositionString("C AND ADULTS");

    return compositionCohortDefinition;
  }

  private CohortDefinition getDFromMQC3N1() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName(
        "Melhoria de Qualidade Category 3 Numerator C part compposition");

    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Date.class));

    compositionCohortDefinition.addSearch("D", EptsReportUtils.map(getDqueryFromCat3(), MAPPING));
    compositionCohortDefinition.addSearch(
        "CHILDREN",
        EptsReportUtils.map(
            this.ageCohortQueries.createXtoYAgeCohort("children", 0, 14),
            "effectiveDate=${endDate}"));

    compositionCohortDefinition.setCompositionString("D AND CHILDREN");

    return compositionCohortDefinition;
  }

  private CohortDefinition getCqueryFromCat3() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Date.class));

    Map<String, Integer> map = new HashMap<>();
    map.put(
        "adultoSeguimentoEncounterType",
        hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put(
        "masterCardEncounterType", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("typeTestHIVConcept", hivMetadata.getTypeTestHIVConcept().getConceptId());

    String query =
        "SELECT final.patient_id  "
            + "FROM  "
            + "    (SELECT p.patient_id, MIN(e.encounter_datetime) AS first_consultation "
            + "    FROM patient p "
            + "        INNER JOIN encounter e "
            + "            ON e.patient_id = p.patient_id "
            + "        INNER JOIN (SELECT p_diagnostico.patient_id,  o_diagnostico.obs_datetime AS data_diagnostico "
            + "                    FROM patient p_diagnostico "
            + "                        INNER JOIN encounter e_diagnostico "
            + "                            ON e_diagnostico.patient_id = p_diagnostico.patient_id "
            + "                        INNER JOIN obs o_diagnostico "
            + "                            ON o_diagnostico.encounter_id = e_diagnostico.encounter_id "
            + "                    WHERE p_diagnostico.voided = 0 "
            + "                        AND e_diagnostico.voided = 0 "
            + "                        AND o_diagnostico.voided = 0 "
            + "                        AND e_diagnostico.location_id = :location "
            + "                        AND e_diagnostico.encounter_type = ${masterCardEncounterType} "
            + "                        AND o_diagnostico.obs_datetime BETWEEN :startDate AND  :endDate "
            + "                        AND o_diagnostico.concept_id = ${typeTestHIVConcept}) diagnostico "
            + " "
            + "                ON diagnostico.patient_id = p.patient_id "
            + "        INNER JOIN (SELECT p_diag_plus_seven.patient_id, DATE_ADD(o_diag_plus_seven.obs_datetime, INTERVAL 7 DAY) AS data_diag_plus_seven "
            + "                FROM patient p_diag_plus_seven "
            + "                    INNER JOIN encounter e_diag_plus_seven "
            + "                        ON e_diag_plus_seven.patient_id = p_diag_plus_seven.patient_id "
            + "                    INNER JOIN obs o_diag_plus_seven "
            + "                        ON o_diag_plus_seven.encounter_id = e_diag_plus_seven.encounter_id "
            + "                WHERE p_diag_plus_seven.voided = 0 "
            + "                    AND e_diag_plus_seven.voided = 0 "
            + "                    AND o_diag_plus_seven.voided = 0 "
            + "                    AND e_diag_plus_seven.location_id = :location "
            + "                    AND e_diag_plus_seven.encounter_type = ${masterCardEncounterType} "
            + "                    AND o_diag_plus_seven.obs_datetime BETWEEN :startDate AND  :endDate "
            + "                    AND o_diag_plus_seven.concept_id = ${typeTestHIVConcept}) diag_plus_seven "
            + "                ON    diag_plus_seven.patient_id = p.patient_id "
            + "    WHERE p.voided = 0 "
            + "        AND e.voided = 0 "
            + "        AND e.location_id = :location "
            + "        AND e.encounter_type = ${adultoSeguimentoEncounterType} "
            + "        AND e.encounter_datetime >= diagnostico.data_diagnostico "
            + "        AND e.encounter_datetime  "
            + "            BETWEEN "
            + "                diagnostico.data_diagnostico "
            + "            AND  "
            + "                diag_plus_seven.data_diag_plus_seven "
            + "    GROUP BY  p.patient_id "
            + "    ) AS final";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  private CohortDefinition getDqueryFromCat3() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Date.class));

    Map<String, Integer> map = new HashMap<>();
    map.put(
        "adultoSeguimentoEncounterType",
        hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put(
        "masterCardEncounterType", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("typeTestHIVConcept", hivMetadata.getTypeTestHIVConcept().getConceptId());
    map.put(
        "presumptiveDiagnosisInChildrenConcep",
        hivMetadata.getPresumptiveDiagnosisInChildrenConcep().getConceptId());

    String query =
        "SELECT final.patient_id   "
            + "FROM   "
            + "    (  "
            + "        SELECT p.patient_id, MIN(e.encounter_datetime) AS first_consultation  "
            + "        FROM patient p  "
            + "            INNER JOIN encounter e  "
            + "                ON e.patient_id = p.patient_id  "
            + "            INNER JOIN(  "
            + "                        SELECT oldest.patient_id, MIN(oldest.data_diagnostico) AS oldest_date  "
            + "                        FROM (  "
            + "                            SELECT p_diagnostico.patient_id, o_diagnostico.obs_datetime AS data_diagnostico  "
            + "                            FROM patient p_diagnostico  "
            + "                                INNER JOIN encounter e_diagnostico  "
            + "                                    ON e_diagnostico.patient_id = p_diagnostico.patient_id  "
            + "                                INNER JOIN obs o_diagnostico  "
            + "                                    ON o_diagnostico.encounter_id = e_diagnostico.encounter_id  "
            + "                            WHERE p_diagnostico.voided = 0  "
            + "                                AND e_diagnostico.voided = 0  "
            + "                                AND o_diagnostico.voided = 0  "
            + "                                AND e_diagnostico.location_id = :location  "
            + "                                AND e_diagnostico.encounter_type = ${masterCardEncounterType}  "
            + "                                AND o_diagnostico.obs_datetime BETWEEN :startDate AND  :endDate  "
            + "                                AND o_diagnostico.concept_id = ${typeTestHIVConcept}  "
            + "                            UNION  "
            + "                            SELECT p_presuntivo.patient_id, o_presuntivo.obs_datetime AS data_diagnostico  "
            + "                            FROM patient p_presuntivo  "
            + "                                INNER JOIN encounter e_presuntivo  "
            + "                                    ON e_presuntivo.patient_id = p_presuntivo.patient_id  "
            + "                                INNER JOIN obs o_presuntivo  "
            + "                                    ON o_presuntivo.encounter_id = e_presuntivo.encounter_id  "
            + "                            WHERE p_presuntivo.voided = 0  "
            + "                                AND e_presuntivo.voided = 0  "
            + "                                AND o_presuntivo.voided = 0  "
            + "                                AND e_presuntivo.location_id = :location  "
            + "                                AND e_presuntivo.encounter_type = ${masterCardEncounterType}  "
            + "                                AND o_presuntivo.obs_datetime BETWEEN :startDate AND  :endDate  "
            + "                                AND o_presuntivo.concept_id = ${presumptiveDiagnosisInChildrenConcep}  "
            + "                            ) oldest  "
            + "                        GROUP BY oldest.patient_id) as a_oldest  "
            + "                ON a_oldest.patient_id = p.patient_id  "
            + "            INNER JOIN(  "
            + "                        SELECT oldest.patient_id, DATE_ADD(MIN(oldest.data_diagnostico) , INTERVAL 7 DAY) AS oldest_date  "
            + "                        FROM (  "
            + "                            SELECT p_diagnostico.patient_id, o_diagnostico.obs_datetime AS data_diagnostico  "
            + "                            FROM patient p_diagnostico  "
            + "                                INNER JOIN encounter e_diagnostico  "
            + "                                    ON e_diagnostico.patient_id = p_diagnostico.patient_id  "
            + "                                INNER JOIN obs o_diagnostico  "
            + "                                    ON o_diagnostico.encounter_id = e_diagnostico.encounter_id  "
            + "                            WHERE p_diagnostico.voided = 0  "
            + "                                AND e_diagnostico.voided = 0  "
            + "                                AND o_diagnostico.voided = 0  "
            + "                                AND e_diagnostico.location_id = :location  "
            + "                                AND e_diagnostico.encounter_type = ${masterCardEncounterType}  "
            + "                                AND o_diagnostico.obs_datetime BETWEEN :startDate AND  :endDate  "
            + "                                AND o_diagnostico.concept_id = ${typeTestHIVConcept}  "
            + "                            UNION  "
            + "                            SELECT p_presuntivo.patient_id, o_presuntivo.obs_datetime AS data_diagnostico  "
            + "                            FROM patient p_presuntivo  "
            + "                                INNER JOIN encounter e_presuntivo  "
            + "                                    ON e_presuntivo.patient_id = p_presuntivo.patient_id  "
            + "                                INNER JOIN obs o_presuntivo  "
            + "                                    ON o_presuntivo.encounter_id = e_presuntivo.encounter_id  "
            + "                            WHERE p_presuntivo.voided = 0  "
            + "                                AND e_presuntivo.voided = 0  "
            + "                                AND o_presuntivo.voided = 0  "
            + "                                AND e_presuntivo.location_id = :location  "
            + "                                AND e_presuntivo.encounter_type = ${masterCardEncounterType}  "
            + "                                AND o_presuntivo.obs_datetime BETWEEN :startDate AND  :endDate  "
            + "                                AND o_presuntivo.concept_id = ${presumptiveDiagnosisInChildrenConcep}  "
            + "                            ) oldest  "
            + "                        GROUP BY oldest.patient_id) as b_oldest  "
            + "                ON b_oldest.patient_id = p.patient_id  "
            + "        WHERE p.voided = 0  "
            + "            AND e.voided = 0  "
            + "            AND e.location_id = :location  "
            + "            AND e.encounter_type = ${adultoSeguimentoEncounterType}  "
            + "            AND e.encounter_datetime >= a_oldest.oldest_date  "
            + "            AND e.encounter_datetime   "
            + "                BETWEEN  "
            + "                    a_oldest.oldest_date  "
            + "                AND   "
            + "                    b_oldest.oldest_date  "
            + "        GROUP BY  p.patient_id  "
            + ") AS final";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }
  /**
   * <b>MQC4D1</b>: Melhoria de Qualidade Category 4 Deniminator 1 <br>
   * <i> A and NOT B and NOT C and Age < 15 years</i> <br>
   *
   * <ul>
   *   <li>A - Select all patients who initiated ART during the Inclusion Period (startDateRevision
   *       and endDateInclusion)
   *   <li>B - Select all female patients who are pregnant as following:
   *       <ul>
   *         <li>All patients registered in Ficha Clínica (encounter type=53) with
   *             “Gestante”(concept_id 1982) value coded equal to “Yes” (concept_id 1065) and
   *             encounter datetime >= startDateRevision and <=endDateInclusion and sex=Female
   *       </ul>
   *   <li>C- Select all female patients who are breastfeeding as following:
   *       <ul>
   *         <li>all patients registered in Ficha Clinica (encounter type=53) with
   *             “Lactante”(concept_id 6332) value coded equal to “Yes” (concept_id 1065) and
   *             encounter datetime >= startDateRevision and <=endDateInclusion and sex=Female
   *       </ul>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQC4D1() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MCC4D1 Patients");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Date.class));

    cd.addSearch(
        "A",
        EptsReportUtils.map(
            genericCohortQueries.getStartedArtOnPeriod(false, true),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));
    cd.addSearch(
        "B",
        EptsReportUtils.map(
            getPregnantAndBreastfeedingStates(
                hivMetadata.getPregnantConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "C",
        EptsReportUtils.map(
            getPregnantAndBreastfeedingStates(
                hivMetadata.getBreastfeeding().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch("FEMALE", EptsReportUtils.map(genderCohortQueries.femaleCohort(), ""));
    cd.addSearch(
        "CHILDREN",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnArtStartDate(0, 14, true),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));
    cd.setCompositionString("(A AND FEMALE AND CHILDREN) AND NOT (B OR C)");
    return cd;
  }

  /**
   * <b>MQC4D2</b>: Melhoria de Qualidade Category 4 Deniminator 2 <br>
   * <i> A and B and NOT C</i> <br>
   *
   * <ul>
   *   <li>A - Select all patients who initiated ART during the Inclusion Period (startDateRevision
   *       and endDateInclusion)
   *   <li>B - Select all female patients who are pregnant as following:
   *       <ul>
   *         <li>All patients registered in Ficha Clínica (encounter type=53) with
   *             “Gestante”(concept_id 1982) value coded equal to “Yes” (concept_id 1065) and
   *             encounter datetime >= startDateRevision and <=endDateInclusion and sex=Female
   *       </ul>
   *   <li>C- Select all female patients who are breastfeeding as following:
   *       <ul>
   *         <li>all patients registered in Ficha Clinica (encounter type=53) with
   *             “Lactante”(concept_id 6332) value coded equal to “Yes” (concept_id 1065) and
   *             encounter datetime >= startDateRevision and <=endDateInclusion and sex=Female
   *       </ul>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQC4D2() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MCC4D2 Patients");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Date.class));

    cd.addSearch(
        "A",
        EptsReportUtils.map(
            genericCohortQueries.getStartedArtOnPeriod(false, true),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));
    cd.addSearch(
        "B",
        EptsReportUtils.map(
            getPregnantAndBreastfeedingStates(
                hivMetadata.getPregnantConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "C",
        EptsReportUtils.map(
            getPregnantAndBreastfeedingStates(
                hivMetadata.getBreastfeeding().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch("FEMALE", EptsReportUtils.map(genderCohortQueries.femaleCohort(), ""));

    cd.setCompositionString("(A AND FEMALE) AND NOT (B OR C)");
    return cd;
  }

  private CohortDefinition getPregnantAndBreastfeedingStates(int conceptIdQn, int conceptIdAns) {
    Map<String, Integer> map = new HashMap<>();
    map.put("conceptIdQn", conceptIdQn);
    map.put("conceptIdAns", conceptIdAns);
    map.put(
        "fichaClinicaEncounterType", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    String query =
        "SELECT "
            + "   p.patient_id "
            + "   FROM "
            + "   patient p "
            + "   INNER JOIN "
            + "      encounter e "
            + "      ON p.patient_id = e.patient_id "
            + "   INNER JOIN "
            + "      obs o "
            + "      ON o.encounter_id = e.encounter_id "
            + "  WHERE "
            + "   p.voided = 0 "
            + "   AND e.voided = 0 "
            + "   AND o.voided = 0 "
            + "   AND e.encounter_type = ${fichaClinicaEncounterType} "
            + "   AND o.concept_id = ${conceptIdQn} "
            + "   AND o.value_coded = ${conceptIdAns} "
            + "   AND e.encounter_datetime BETWEEN :startDate AND :endDate";

    return genericCohortQueries.generalSql(
        "Pregnant or breastfeeding females", stringSubstitutor.replace(query));
  }

  public CohortDefinition getMQC4N1() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Date.class));

    compositionCohortDefinition.setName("Numerator for Category 4");

    compositionCohortDefinition.addSearch(
        "E", EptsReportUtils.map(getLastClinicalConsultationClassficacaoDesnutricao(), MAPPING));

    compositionCohortDefinition.addSearch("MQC4D1", EptsReportUtils.map(getMQC4D1(), MAPPING));

    compositionCohortDefinition.setCompositionString("MQC4D1 AND E");

    return compositionCohortDefinition;
  }

  public CohortDefinition getMQC4N2() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Date.class));

    compositionCohortDefinition.setName("Numerator for Category 4");

    compositionCohortDefinition.addSearch(
        "E", EptsReportUtils.map(getLastClinicalConsultationClassficacaoDesnutricao(), MAPPING));

    compositionCohortDefinition.addSearch("MQC4D2", EptsReportUtils.map(getMQC4D2(), MAPPING));

    compositionCohortDefinition.setCompositionString("MQC4D2 AND E");

    return compositionCohortDefinition;
  }

  private CohortDefinition getLastClinicalConsultationClassficacaoDesnutricao() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "last clinical consultation registered CLASSIFICAÇÃO DE DESNUTRIÇÃO");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put(
        "adultoSeguimentoEncounterType",
        hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put(
        "classificationOfMalnutritionConcept",
        commonMetadata.getClassificationOfMalnutritionConcept().getConceptId());
    map.put("normalConcept", hivMetadata.getNormalConcept().getConceptId());
    map.put(
        "malnutritionLightConcept", commonMetadata.getMalnutritionLightConcept().getConceptId());
    map.put("malnutritionConcept", hivMetadata.getMalnutritionConcept().getConceptId());
    map.put(
        "chronicMalnutritionConcept", hivMetadata.getChronicMalnutritionConcept().getConceptId());

    String query =
        "SELECT max_date.patient_id "
            + "FROM "
            + "   ( "
            + "    SELECT p.patient_id, MAX(encounter_datetime) AS max_encounter_date "
            + "    FROM patient p "
            + "        INNER JOIN encounter e "
            + "            ON e.patient_id = p.patient_id "
            + "        INNER JOIN obs o "
            + "            ON o.encounter_id = e.encounter_id "
            + "    WHERE "
            + "        p.voided = 0 "
            + "        AND e.voided = 0 "
            + "        AND o.voided = 0 "
            + "        AND e.encounter_type = ${adultoSeguimentoEncounterType} "
            + "        AND e.location_id = :location "
            + "        AND e.encounter_datetime  "
            + "            BETWEEN :startDate AND :endDate "
            + "        AND o.concept_id = ${classificationOfMalnutritionConcept} "
            + "        AND o.value_coded IN (${normalConcept}, ${malnutritionLightConcept}, ${malnutritionConcept}, ${chronicMalnutritionConcept}) "
            + "    GROUP BY patient_id "
            + "    ) max_date";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /*
   *
   * All  patients the first clinical consultation with nutricional state equal
   * to “DAM” or “DAG” occurred during the revision period.
   *
   */

  private CohortDefinition getPatientsWithNutritionalState() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients with Nutritional Calssification");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Date.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6336", commonMetadata.getClassificationOfMalnutritionConcept().getConceptId());
    map.put("1844", hivMetadata.getChronicMalnutritionConcept().getConceptId());
    map.put("68", hivMetadata.getMalnutritionConcept().getConceptId());

    String query =
        " SELECT p.patient_id "
            + " FROM patient p "
            + " INNER JOIN ( "
            + " SELECT  p.patient_id, min(e.encounter_datetime) "
            + " FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + " INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + " WHERE p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND e.location_id = :location "
            + " AND e.encounter_type = ${6}  "
            + " AND o.concept_id = ${6336}  "
            + " AND o.value_coded IN (${1844},${68}) "
            + " AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + " GROUP BY p.patient_id) nut ON p.patient_id = nut.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /*
   *
   * All  patients the first clinical consultation with nutricional state equal
   * to “DAM” or “DAG” occurred during the revision period and
   * “Apoio/Educação Nutricional” = “ATPU” or “SOJA” in
   * the same clinical consultation
   *
   */

  public CohortDefinition getPatientsWithNutritionalStateAndNutritionalSupport() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients with Nutritional Calssification");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Date.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6336", commonMetadata.getClassificationOfMalnutritionConcept().getConceptId());
    map.put("1844", hivMetadata.getChronicMalnutritionConcept().getConceptId());
    map.put("68", hivMetadata.getMalnutritionConcept().getConceptId());
    map.put("2152", commonMetadata.getNutritionalSupplememtConcept().getConceptId());
    map.put("6143", commonMetadata.getATPUSupplememtConcept().getConceptId());
    map.put("2151", commonMetadata.getSojaSupplememtConcept().getConceptId());

    String query =
        " SELECT "
            + " p.patient_id "
            + " FROM "
            + " patient p "
            + "     INNER JOIN "
            + " (SELECT  "
            + "     p.patient_id, MIN(e.encounter_datetime) "
            + " FROM "
            + "     patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + " INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + " INNER JOIN obs o1 ON o1.encounter_id = o.encounter_id "
            + " WHERE "
            + "     p.voided = 0 AND e.voided = 0 "
            + "         AND o.voided = 0 "
            + "         AND o1.voided = 0 "
            + "         AND e.location_id = :location "
            + "         AND e.encounter_type = ${6} "
            + "         AND o.concept_id = ${6336} "
            + "         AND o.value_coded IN (${1844} , ${68}) "
            + "         AND o1.concept_id = ${2152} "
            + "         AND o1.value_coded IN (${6143} , ${2151}) "
            + "         AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + " GROUP BY p.patient_id) nut ON p.patient_id = nut.patient_id; ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>MQ5A</b>: Melhoria de Qualidade Category 5 Criancas <br>
   * <i> DENOMINATOR: (A AND B) AND NOT (C OR D OR E)</i> <br>
   * <i> NOMINATOR: (A AND B) AND NOT (C OR D OR E) AND F</i> <br>
   *
   * <ul>
   *   <li>A - Select all patients who initiated ART during the Inclusion period (startDateInclusion
   *       and endDateInclusion)
   *   <li>
   *   <li>B - Filter all patients with nutritional state equal to “DAM” or “DAG” registered on a
   *       clinical consultation during the period
   *   <li>
   *   <li>C - All female patients registered as “Pregnant” on a clinical consultation during the
   *       inclusion period (startDateInclusion and endDateInclusion)
   *   <li>
   *   <li>D - All female patients registered as “Breastfeeding” on a clinical consultation during
   *       the inclusion period (startDateInclusion and endDateInclusion)
   *   <li>
   *   <li>E - All transferred IN patients
   *   <li>
   *   <li>F - F - Filter all patients with “Apoio/Educação Nutricional” equals to “ATPU” or “SOJA”
   *       in the same clinical consultation where“Grau da Avaliação Nutricional” equals to “DAM” or
   *       “DAG” during the revision period, clinical consultation >= startDateRevision and
   *       <=endDateRevision
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQ5A(Boolean den) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    if (den) {
      compositionCohortDefinition.setName("% de crianças em TARV com desnutrição (DAM ou DAG)");
    } else {
      compositionCohortDefinition.setName(
          "% de crianças em TARV com desnutrição (DAM ou DAG) e com registo de prescrição de suplementação ou tratamento nutricional");
    }
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Date.class));

    CohortDefinition startedART = getMQC3D1();

    CohortDefinition nutritionalClass = getPatientsWithNutritionalState();

    CohortDefinition pregnant =
        commonCohortQueries.getMohMQPatientsOnCondition(
            true,
            false,
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getPregnantConcept(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition breastfeeding =
        commonCohortQueries.getMohMQPatientsOnCondition(
            true,
            false,
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getBreastfeeding(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition transferIn =
        commonCohortQueries.getMohMQPatientsOnCondition(
            false,
            true,
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getTransferFromOtherFacilityConcept(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            hivMetadata.getTypeOfPatientTransferredFrom(),
            Collections.singletonList(hivMetadata.getArtStatus()));

    CohortDefinition nutSupport = getPatientsWithNutritionalStateAndNutritionalSupport();

    compositionCohortDefinition.addSearch("A", EptsReportUtils.map(startedART, MAPPING));

    compositionCohortDefinition.addSearch("B", EptsReportUtils.map(nutritionalClass, MAPPING));

    compositionCohortDefinition.addSearch("C", EptsReportUtils.map(pregnant, MAPPING));

    compositionCohortDefinition.addSearch("D", EptsReportUtils.map(breastfeeding, MAPPING));

    compositionCohortDefinition.addSearch("E", EptsReportUtils.map(transferIn, MAPPING));

    compositionCohortDefinition.addSearch("F", EptsReportUtils.map(nutSupport, MAPPING));

    if (den) {
      compositionCohortDefinition.setCompositionString("(A AND B) AND NOT (C OR D OR E)");
    } else {
      compositionCohortDefinition.setCompositionString("(A AND B) AND NOT (C OR D OR E) AND F");
    }
    return compositionCohortDefinition;
  }

  /**
   * <b>MQ5B</b>: Melhoria de Qualidade Category 5 MG <br>
   * <i> DENOMINATOR: (A AND B AND C) AND NOT (D OR E)</i> <br>
   * <i> NOMINATOR: (A AND B AND C) AND NOT (D OR E) AND F</i> <br>
   *
   * <ul>
   *   <li>A - Select all patients who initiated ART during the Inclusion period (startDateInclusion
   *       and endDateInclusion)
   *   <li>
   *   <li>B - Filter all patients with nutritional state equal to “DAM” or “DAG” registered on a
   *       clinical consultation during the period
   *   <li>
   *   <li>C - All female patients registered as “Pregnant” on a clinical consultation during the
   *       inclusion period (startDateInclusion and endDateInclusion)
   *   <li>
   *   <li>D - All female patients registered as “Breastfeeding” on a clinical consultation during
   *       the inclusion period (startDateInclusion and endDateInclusion)
   *   <li>
   *   <li>E - All transferred IN patients
   *   <li>
   *   <li>F - F - Filter all patients with “Apoio/Educação Nutricional” equals to “ATPU” or “SOJA”
   *       in the same clinical consultation where“Grau da Avaliação Nutricional” equals to “DAM” or
   *       “DAG” during the revision period, clinical consultation >= startDateRevision and
   *       <=endDateRevision
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQ5B(Boolean den) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    if (den) {
      compositionCohortDefinition.setName(
          "% de mulheres gravidas em TARV com desnutrição (DAM ou DAG)");
    } else {
      compositionCohortDefinition.setName(
          "% de MG em TARV com desnutrição (DAM ou DAG) e com registo de prescrição de suplementação ou tratamento nutricional");
    }
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Date.class));

    CohortDefinition startedART = getMQC3D1();

    CohortDefinition nutritionalClass = getPatientsWithNutritionalState();

    CohortDefinition pregnant =
        commonCohortQueries.getMohMQPatientsOnCondition(
            true,
            false,
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getPregnantConcept(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition breastfeeding =
        commonCohortQueries.getMohMQPatientsOnCondition(
            true,
            false,
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getBreastfeeding(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition transferIn =
        commonCohortQueries.getMohMQPatientsOnCondition(
            false,
            true,
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getTransferFromOtherFacilityConcept(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            hivMetadata.getTypeOfPatientTransferredFrom(),
            Collections.singletonList(hivMetadata.getArtStatus()));

    CohortDefinition nutSupport = getPatientsWithNutritionalStateAndNutritionalSupport();

    compositionCohortDefinition.addSearch("A", EptsReportUtils.map(startedART, MAPPING));

    compositionCohortDefinition.addSearch("B", EptsReportUtils.map(nutritionalClass, MAPPING));

    compositionCohortDefinition.addSearch("C", EptsReportUtils.map(pregnant, MAPPING));

    compositionCohortDefinition.addSearch("D", EptsReportUtils.map(breastfeeding, MAPPING));

    compositionCohortDefinition.addSearch("E", EptsReportUtils.map(transferIn, MAPPING));

    compositionCohortDefinition.addSearch("F", EptsReportUtils.map(nutSupport, MAPPING));

    if (den) {
      compositionCohortDefinition.setCompositionString("(A AND B AND C) AND NOT (D OR E)");
    } else {
      compositionCohortDefinition.setCompositionString("(A AND B AND C) AND NOT (D OR E) AND F");
    }
    return compositionCohortDefinition;
  }
}
