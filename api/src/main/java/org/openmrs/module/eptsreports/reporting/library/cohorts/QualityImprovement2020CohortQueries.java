package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.*;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.*;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QualityImprovement2020CohortQueries {

  private GenericCohortQueries genericCohortQueries;

  private HivMetadata hivMetadata;

  private CommonMetadata commonMetadata;

  private AgeCohortQueries ageCohortQueries;

  private final String MAPPING = "startDate=${startDate},endDate=${endDate},location=${location}";

  @Autowired
  public QualityImprovement2020CohortQueries(
      GenericCohortQueries genericCohortQueries,
      HivMetadata hivMetadata,
      CommonMetadata commonMetadata,
      AgeCohortQueries ageCohortQueries) {
    this.genericCohortQueries = genericCohortQueries;
    this.hivMetadata = hivMetadata;
    this.commonMetadata = commonMetadata;
    this.ageCohortQueries = ageCohortQueries;
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
   *         <li>All patients registered in Ficha Resumo (Encounter Type Id=
   *             ${masterCardEncounterType}) and marked as Transferred-in (“Transfer from other
   *             facility” concept Id 1369 = “Yes” concept id 1065) in TARV (“Type of Patient
   *             Transferred from” concept id 6300 = “ART” concept id 6276)
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

    CohortDefinition transferredIn = this.getTransferredInPatients();

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
            + "    AND e.encounter_datetime BETWEEN :startDate AND :endDate "
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
}
