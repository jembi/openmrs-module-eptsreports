package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.eptsreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.eptsreports.reporting.calculation.generic.AgeOnObsDatetimeCalculation;
import org.openmrs.module.eptsreports.reporting.calculation.melhoriaQualidade.ConsultationUntilEndDateAfterStartingART;
import org.openmrs.module.eptsreports.reporting.calculation.melhoriaQualidade.EncounterAfterOldestARTStartDateCalculation;
import org.openmrs.module.eptsreports.reporting.calculation.melhoriaQualidade.SecondFollowingEncounterAfterOldestARTStartDateCalculation;
import org.openmrs.module.eptsreports.reporting.calculation.melhoriaQualidade.ThirdFollowingEncounterAfterOldestARTStartDateCalculation;
import org.openmrs.module.eptsreports.reporting.cohort.definition.CalculationCohortDefinition;
import org.openmrs.module.eptsreports.reporting.library.queries.QualityImprovement2020Queries;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportConstants;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.SetComparator;
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

  private TbMetadata tbMetadata;

  private TxPvlsCohortQueries txPvls;

  private TxMlCohortQueries txMlCohortQueries;

  private final String MAPPING = "startDate=${startDate},endDate=${endDate},location=${location}";
  private final String MAPPING1 =
      "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}";
  private final String MAPPING2 =
      "startDate=${revisionEndDate-14m},endDate=${revisionEndDate-11m},location=${location}";
  private final String MAPPING3 =
      "startDate=${startDate},endDate=${revisionEndDate},location=${location}";
  private final String MAPPING4 =
      "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},location=${location}";
  private final String MAPPING5 =
      "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate},revisionEndDate=${revisionEndDate},location=${location}";
  private final String MAPPING6 =
      "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate},location=${location}";
  private final String MAPPING7 =
      "startDate=${revisionEndDate-4m+1d},endDate=${revisionEndDate},location=${location}";
  private final String MAPPING8 =
      "startDate=${revisionEndDate-4m+1d},endDate=${revisionEndDate-3m},location=${location}";
  private final String MAPPING9 =
      "startDate=${revisionEndDate-4m+1d},endDate=${revisionEndDate},revisionEndDate=${revisionEndDate},location=${location}";
  private final String MAPPING10 =
      "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}";

  @Autowired
  public QualityImprovement2020CohortQueries(
      GenericCohortQueries genericCohortQueries,
      HivMetadata hivMetadata,
      CommonMetadata commonMetadata,
      GenderCohortQueries genderCohortQueries,
      ResumoMensalCohortQueries resumoMensalCohortQueries,
      CommonCohortQueries commonCohortQueries,
      TbMetadata tbMetadata,
      TxPvlsCohortQueries txPvls,
      AgeCohortQueries ageCohortQueries,
      TxMlCohortQueries txMlCohortQueries) {
    this.genericCohortQueries = genericCohortQueries;
    this.hivMetadata = hivMetadata;
    this.commonMetadata = commonMetadata;
    this.genderCohortQueries = genderCohortQueries;
    this.resumoMensalCohortQueries = resumoMensalCohortQueries;
    this.commonCohortQueries = commonCohortQueries;
    this.tbMetadata = tbMetadata;
    this.txPvls = txPvls;
    this.ageCohortQueries = ageCohortQueries;
    this.txMlCohortQueries = txMlCohortQueries;
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
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition startedART = getMOHArtStartDate();

    CohortDefinition transferredIn =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    compositionCohortDefinition.addSearch("A", EptsReportUtils.map(startedART, MAPPING));

    compositionCohortDefinition.addSearch("B", EptsReportUtils.map(transferredIn, MAPPING));

    compositionCohortDefinition.setCompositionString("A AND NOT B");

    return compositionCohortDefinition;
  }
  /**
   * 17 - MOH MQ: Patients who initiated ART during the inclusion period
   *
   * <p>A2-All patients who have the first historical start drugs date (earliest concept ID 1190)
   * set in FICHA RESUMO (Encounter Type 53) earliest “historical start date” Encounter Type Ids =
   * 53 The earliest “Historical Start Date” (Concept Id 1190)And historical start
   * date(Value_datetime) <=EndDate And the earliest date from A1 and A2 (identified as Patient ART
   * Start Date) is >= startDateRevision and <=endDateInclusion
   *
   * @return SqlCohortDefinition
   */
  public SqlCohortDefinition getMOHArtStartDate() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" All patients that started ART during inclusion period ");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Date.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1190", hivMetadata.getHistoricalDrugStartDateConcept().getConceptId());

    String query =
        " SELECT patient_id "
            + "        FROM   (SELECT p.patient_id, Min(value_datetime) art_date "
            + "                    FROM patient p "
            + "              INNER JOIN encounter e "
            + "                  ON p.patient_id = e.patient_id "
            + "              INNER JOIN obs o "
            + "                  ON e.encounter_id = o.encounter_id "
            + "          WHERE  p.voided = 0 "
            + "              AND e.voided = 0 "
            + "              AND o.voided = 0 "
            + "              AND e.encounter_type = ${53} "
            + "              AND o.concept_id = ${1190} "
            + "              AND o.value_datetime IS NOT NULL "
            + "              AND o.value_datetime <= :endDate "
            + "              AND e.location_id = :location "
            + "          GROUP  BY p.patient_id  )  "
            + "               union_tbl  "
            + "        WHERE  union_tbl.art_date BETWEEN :startDate AND :endDate";

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

    compositionCohortDefinition.addSearch(
        "C", EptsReportUtils.map(getCqueryFromCat3(), "location=${location}"));
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
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Date.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("22772", hivMetadata.getTypeTestHIVConcept().getConceptId());

    String query =
        " SELECT p.patient_id   "
            + " FROM patient p "
            + "    INNER JOIN ( "
            + "                    SELECT p.patient_id, MIN(e.encounter_datetime) AS encounterdatetime  "
            + "                    FROM patient p  "
            + "                        INNER JOIN encounter e  "
            + "                            ON e.patient_id = p.patient_id  "
            + "                    WHERE p.voided = 0  "
            + "                        AND e.voided = 0  "
            + "                        AND e.location_id = :location  "
            + "                        AND e.encounter_type = ${6}  "
            + "                    GROUP BY  p.patient_id  "
            + "              )  first_consultation ON  first_consultation.patient_id = p.patient_id "
            + "    INNER JOIN ( "
            + "                    SELECT p_diagnostico.patient_id,  o_diagnostico.obs_datetime AS data_diagnostico  "
            + "                    FROM patient p_diagnostico  "
            + "                        INNER JOIN encounter e_diagnostico  "
            + "                            ON e_diagnostico.patient_id = p_diagnostico.patient_id  "
            + "                        INNER JOIN obs o_diagnostico  "
            + "                            ON o_diagnostico.encounter_id = e_diagnostico.encounter_id  "
            + "                    WHERE p_diagnostico.voided = 0  "
            + "                        AND e_diagnostico.voided = 0  "
            + "                        AND o_diagnostico.voided = 0  "
            + "                        AND e_diagnostico.location_id = :location  "
            + "                        AND e_diagnostico.encounter_type = ${53}  "
            + "                        AND o_diagnostico.concept_id = ${22772} "
            + "                ) diagnostico ON diagnostico.patient_id = p.patient_id  "
            + "                 "
            + " WHERE p.voided = 0 "
            + "    AND  first_consultation.encounterdatetime >= diagnostico.data_diagnostico  "
            + "    AND first_consultation.encounterdatetime  "
            + "        BETWEEN  diagnostico.data_diagnostico  AND   "
            + "                        DATE_ADD(diagnostico.data_diagnostico, INTERVAL 7 DAY) ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getDqueryFromCat3() {
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
        "      SELECT    "
            + "        final_diag_pres.patient_id   "
            + "    FROM   "
            + "        (SELECT    "
            + "            p.patient_id,   "
            + "                MIN(e.encounter_datetime) AS first_consultation   "
            + "        FROM   "
            + "            patient p   "
            + "        INNER JOIN encounter e ON e.patient_id = p.patient_id   "
            + "        INNER JOIN (SELECT p.patient_id, MIN(diag_pres.data_diagnostico) as data_diagnostico   "
            + "        FROM patient p    "
            + "        INNER JOIN   "
            + "                (SELECT    "
            + "            p_presuntivo.patient_id,   "
            + "                MIN(o_presuntivo.obs_datetime) AS data_diagnostico   "
            + "        FROM   "
            + "            patient p_presuntivo   "
            + "        INNER JOIN encounter e_presuntivo ON e_presuntivo.patient_id = p_presuntivo.patient_id   "
            + "        INNER JOIN obs o_presuntivo ON o_presuntivo.encounter_id = e_presuntivo.encounter_id   "
            + "        WHERE   "
            + "            p_presuntivo.voided = 0   "
            + "                AND e_presuntivo.voided = 0   "
            + "                AND o_presuntivo.voided = 0   "
            + "                AND e_presuntivo.location_id = :location   "
            + "                AND e_presuntivo.encounter_type = ${masterCardEncounterType}   "
            + "                AND o_presuntivo.concept_id = ${presumptiveDiagnosisInChildrenConcep}   "
            + "        GROUP BY p_presuntivo.patient_id   "
            + "        UNION   "
            + "        SELECT    "
            + "            p_diagnostico.patient_id,   "
            + "                MIN(o_diagnostico.obs_datetime) AS data_diagnostico   "
            + "        FROM   "
            + "            patient p_diagnostico   "
            + "        INNER JOIN encounter e_diagnostico ON e_diagnostico.patient_id = p_diagnostico.patient_id   "
            + "        INNER JOIN obs o_diagnostico ON o_diagnostico.encounter_id = e_diagnostico.encounter_id   "
            + "        WHERE   "
            + "            p_diagnostico.voided = 0   "
            + "                AND e_diagnostico.voided = 0   "
            + "                AND o_diagnostico.voided = 0   "
            + "                AND e_diagnostico.location_id = :location   "
            + "                AND e_diagnostico.encounter_type = ${masterCardEncounterType}   "
            + "                AND o_diagnostico.concept_id = ${typeTestHIVConcept}    "
            + "                GROUP BY p_diagnostico.patient_id   "
            + "        ) AS diag_pres ON diag_pres.patient_id = p.patient_id   "
            + "        GROUP BY p.patient_id) AS oldest ON oldest.patient_id = p.patient_id   "
            + "        WHERE   "
            + "            p.voided = 0 AND e.voided = 0   "
            + "                AND e.location_id = :location   "
            + "                AND e.encounter_type = ${adultoSeguimentoEncounterType}   "
            + "                AND e.encounter_datetime >= oldest.data_diagnostico   "
            + "                AND DATEDIFF(e.encounter_datetime,oldest.data_diagnostico) >= 0   "
            + "                AND DATEDIFF(e.encounter_datetime,oldest.data_diagnostico) <= 7   "
            + "      GROUP BY p.patient_id) AS final_diag_pres;";

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
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    cd.addSearch("A", EptsReportUtils.map(getMOHArtStartDate(), MAPPING));
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
    cd.addSearch(
        "D",
        EptsReportUtils.map(
            QualityImprovement2020Queries.getTransferredInPatients(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
                hivMetadata.getPatientFoundYesConcept().getConceptId(),
                hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
                hivMetadata.getArtStatus().getConceptId()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "CHILDREN",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnMOHArtStartDate(0, 14, true),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));
    cd.setCompositionString("(A AND NOT B AND NOT C AND NOT D) AND CHILDREN");
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
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    cd.addSearch("A", EptsReportUtils.map(getMOHArtStartDate(), MAPPING));
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
        "D",
        EptsReportUtils.map(
            QualityImprovement2020Queries.getTransferredInPatients(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
                hivMetadata.getPatientFoundYesConcept().getConceptId(),
                hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
                hivMetadata.getArtStatus().getConceptId()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("((A AND B) AND NOT (C OR D)) AND FEMALE");
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
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    compositionCohortDefinition.setName("Numerator for Category 4");

    compositionCohortDefinition.addSearch(
        "E",
        EptsReportUtils.map(
            getLastClinicalConsultationClassficacaoDesnutricao(),
            "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch("MQC4D1", EptsReportUtils.map(getMQC4D1(), MAPPING));

    compositionCohortDefinition.setCompositionString("MQC4D1 AND E");

    return compositionCohortDefinition;
  }

  public CohortDefinition getMQC4N2() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    compositionCohortDefinition.setName("Numerator for Category 4");

    compositionCohortDefinition.addSearch(
        "E",
        EptsReportUtils.map(
            getLastClinicalConsultationClassficacaoDesnutricao(),
            "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

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
        "SELECT "
            + " p.patient_id "
            + " FROM "
            + " patient p "
            + " INNER JOIN "
            + " encounter e "
            + " ON e.patient_id = p.patient_id "
            + " INNER JOIN "
            + " obs o "
            + " ON o.encounter_id = e.encounter_id "
            + " INNER JOIN "
            + " ( "
            + " SELECT "
            + " p.patient_id, "
            + " Max(e.encounter_datetime) AS encounter_datetime "
            + " FROM "
            + " patient p "
            + " INNER JOIN "
            + " encounter e "
            + " ON e.patient_id = p.patient_id "
            + " WHERE "
            + " e.encounter_type = ${adultoSeguimentoEncounterType} "
            + " AND p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND e.location_id = :location "
            + " AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + " GROUP BY "
            + " p.patient_id "
            + " ) filtered "
            + " ON p.patient_id = filtered.patient_id "
            + " WHERE "
            + " e.encounter_datetime = filtered.encounter_datetime "
            + " AND e.location_id = :location "
            + " AND o.concept_id = ${classificationOfMalnutritionConcept} "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND o.value_coded IN (${normalConcept}, ${malnutritionLightConcept}, ${malnutritionConcept}, ${chronicMalnutritionConcept}) "
            + " AND e.encounter_type = ${adultoSeguimentoEncounterType} ";

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
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

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

  /*
   *
   * All  patients ll patients with a clinical consultation(encounter type 6) during
   * the Revision period with the following conditions:
   *
   * “PROFILAXIA COM ISONIAZIDA”(concept_id 6122) value coded “Fim” (concept_id 1267)
   * Encounter_datetime between startDateRevision and endDateRevision and:
   *
   *  - Encounter_datetime(from the last colinical consultation with
   * “PROFILAXIA COM ISONIAZIDA”(concept_id 6122) value coded “Fim” (concept_id 1267))
   * MINUS
   * - Encounter_datetime (from the colinical consultation with
   * “PROFILAXIA COM ISONIAZIDA”(concept_id 6122) value coded “Fim” (concept_id 1267))
   * between 6 months and 9 months
   *
   */

  public CohortDefinition getPatientsWithProphylaxyDuringRevisionPeriod() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients with Prophylaxy Treatment within Revision Period");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6122", hivMetadata.getIsoniazidUsageConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugsConcept().getConceptId());
    map.put("1267", hivMetadata.getCompletedConcept().getConceptId());

    String query =
        " SELECT  p.patient_id   "
            + " FROM  patient p   "
            + "    INNER JOIN   "
            + "            ( "
            + "                SELECT p.patient_id, MAX(e.encounter_datetime) AS e_datetime   "
            + "                FROM patient p   "
            + "                    INNER JOIN encounter e ON p.patient_id = e.patient_id   "
            + "                    INNER JOIN obs o ON e.encounter_id = o.encounter_id   "
            + "                WHERE   "
            + "                    e.location_id = :location   "
            + "                        AND e.encounter_type = ${6}   "
            + "                        AND o.concept_id = ${6122}   "
            + "                        AND o.value_coded = ${1256}   "
            + "                        AND e.encounter_datetime >= :startDate   "
            + "                        AND e.encounter_datetime <= :endDate   "
            + "                        AND p.voided = 0   "
            + "                        AND e.voided = 0   "
            + "                        AND o.voided = 0   "
            + "                GROUP BY p.patient_id "
            + "            ) AS b4 ON p.patient_id = b4.patient_id   "
            + "    INNER JOIN   "
            + "            ( "
            + "                SELECT  p.patient_id, MAX(e.encounter_datetime) AS e_datetime   "
            + "                FROM  patient p   "
            + "                    INNER JOIN  encounter e ON p.patient_id = e.patient_id   "
            + "                    INNER JOIN  obs o ON e.encounter_id = o.encounter_id   "
            + "                WHERE  e.location_id = :location   "
            + "                    AND e.encounter_type = ${6}  "
            + "                    AND o.concept_id = ${6122}   "
            + "                    AND o.value_coded = ${1267}   "
            + "                    AND e.encounter_datetime >= :startDate   "
            + "                    AND e.encounter_datetime <= :revisionEndDate   "
            + "                    AND p.voided = 0   "
            + "                    AND e.voided = 0   "
            + "                    AND o.voided = 0 "
            + "                GROUP BY p.patient_id "
            + "            ) AS g ON p.patient_id = g.patient_id "
            + " WHERE p.voided =0 "
            + "    AND TIMESTAMPDIFF(DAY,b4.e_datetime,g.e_datetime) >= 170 "
            + "    AND TIMESTAMPDIFF(DAY,b4.e_datetime,g.e_datetime) <= 297 ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * All patients ll patients with a clinical consultation(encounter type 6) during the Revision
   * period with the following conditions:
   *
   * <p>- with “Diagnótico TB activo” (concept_id 23761) value coded “SIM”(concept id 1065)
   * Encounter_datetime between:
   *
   * <p>- Encounter_datetime (from the last colinical consultation with “PROFILAXIA COM
   * ISONIAZIDA”(concept_id 6122) value coded “Inicio” (concept_id 1256)) AND - Encounter_datetime
   * (from the last colinical consultation with “PROFILAXIA COM ISONIAZIDA”(concept_id 6122) value
   * coded “Inicio” (concept_id 1256)) PLUS 9 MONTHS
   */
  public CohortDefinition getPatientsWithTBDiagActive() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients with TB Diagnosis Active");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6122", hivMetadata.getIsoniazidUsageConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugsConcept().getConceptId());
    map.put("23761", tbMetadata.getActiveTBConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("6128", hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId());

    String query =
        ""
            + " SELECT p.patient_id "
            + " FROM patient p "
            + "   INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "   INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "     INNER JOIN (SELECT tpt_start.patient_id, MAX(tpt_start.last_encounter) tpt_start_date "
            + "FROM ( "
            + "         SELECT p.patient_id, MAX(o.value_datetime) last_encounter "
            + "         FROM patient p "
            + "                  INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                  INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "         WHERE p.voided = 0 "
            + "           AND e.voided = 0 "
            + "           AND o.voided = 0 "
            + "           AND o.concept_id = ${6128} "
            + "           AND e.encounter_type = ${53} "
            + "           AND e.location_id = :location "
            + "           AND o.value_datetime BETWEEN :startDate AND :endDate "
            + "         GROUP BY p.patient_id "
            + "         UNION "
            + "         SELECT p.patient_id, MAX(e.encounter_datetime) last_encounter "
            + "         FROM patient p "
            + "                  INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                  INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "         WHERE p.voided = 0 "
            + "           AND e.voided = 0 "
            + "           AND o.voided = 0 "
            + "           AND o.concept_id = ${6122} "
            + "           AND o.value_coded = ${1256} "
            + "           AND e.encounter_type = ${6} "
            + "           AND e.location_id = :location "
            + "           AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "         GROUP BY p.patient_id "
            + "     ) AS tpt_start "
            + "GROUP BY tpt_start.patient_id) AS last ON p.patient_id = last.patient_id "
            + " WHERE "
            + "     e.location_id = :location "
            + "         AND e.encounter_type = ${6} "
            + "         AND o.concept_id = ${23761} "
            + "         AND o.value_coded IN (${1065}) "
            + "         AND e.encounter_datetime BETWEEN last.tpt_start_date AND DATE_ADD(last.tpt_start_date, INTERVAL 9 MONTH) "
            + "         AND p.voided = 0 "
            + "         AND e.voided = 0 "
            + "         AND o.voided = 0; ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * All patients ll patients with a clinical consultation(encounter type 6) during the Revision
   * period with the following conditions:
   *
   * <p>- “TEM SINTOMAS DE TB” (concept_id 23758) value coded “SIM” or “NÃO”(concept_id IN [1065,
   * 1066]) and Encounter_datetime between:
   *
   * <p>- Encounter_datetime (from the last colinical consultation with “PROFILAXIA COM
   * ISONIAZIDA”(concept_id 6122) value coded “Inicio” (concept_id 1256)) AND - Encounter_datetime
   * (from the last colinical consultation with “PROFILAXIA COM ISONIAZIDA”(concept_id 6122) value
   * coded “Inicio” (concept_id 1256)) PLUS 9 MONTHS
   */
  public CohortDefinition getPatientsWithTBSymtoms() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients with TB Diagnosis Active");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6122", hivMetadata.getIsoniazidUsageConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugsConcept().getConceptId());
    map.put("23758", tbMetadata.getHasTbSymptomsConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("6128", hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId());

    String query =
        " SELECT p.patient_id "
            + " FROM patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "         INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "     INNER JOIN (SELECT tpt_start.patient_id, MAX(tpt_start.last_encounter) tpt_start_date "
            + "            FROM ( "
            + "                     SELECT p.patient_id, MAX(o.value_datetime) last_encounter "
            + "                     FROM patient p "
            + "                              INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                              INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                     WHERE p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o.concept_id = ${6128} "
            + "                       AND e.encounter_type = ${53} "
            + "                       AND e.location_id = :location "
            + "                       AND o.value_datetime BETWEEN :startDate AND :endDate "
            + "                     GROUP BY p.patient_id "
            + "                     UNION "
            + "                     SELECT p.patient_id, MAX(e.encounter_datetime) last_encounter "
            + "                     FROM patient p "
            + "                              INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                              INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                     WHERE p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o.concept_id = ${6122} "
            + "                       AND o.value_coded = ${1256} "
            + "                       AND e.encounter_type = ${6} "
            + "                       AND e.location_id = :location "
            + "                       AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "                     GROUP BY p.patient_id "
            + "                 ) AS tpt_start "
            + "            GROUP BY tpt_start.patient_id ) AS last ON p.patient_id = last.patient_id "
            + " WHERE "
            + "     e.location_id = :location "
            + "         AND e.encounter_type = ${6} "
            + "         AND o.concept_id = ${23758} "
            + "         AND o.value_coded IN (${1065}) "
            + "         AND e.encounter_datetime BETWEEN last.tpt_start_date AND DATE_ADD(last.tpt_start_date, INTERVAL 9 MONTH) "
            + "         AND p.voided = 0 "
            + "         AND e.voided = 0 "
            + "         AND o.voided = 0;";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * All patients ll patients with a clinical consultation(encounter type 6) during the Revision
   * period with the following conditions:
   *
   * <p>- “TRATAMENTO DE TUBERCULOSE”(concept_id 1268) value coded “Inicio” or “Continua” or
   * “Fim”(concept_id IN [1256, 1257, 1267]) “Data Tratamento TB” (obs datetime 1268) between:
   *
   * <p>- Encounter_datetime (from the last colinical consultation with “PROFILAXIA COM
   * ISONIAZIDA”(concept_id 6122) value coded “Inicio” (concept_id 1256)) AND - Encounter_datetime
   * (from the last colinical consultation with “PROFILAXIA COM ISONIAZIDA”(concept_id 6122) value
   * coded “Inicio” (concept_id 1256)) PLUS 9 MONTHS
   */
  public CohortDefinition getPatientsWithTBTreatment() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients with TB Diagnosis Active");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6122", hivMetadata.getIsoniazidUsageConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugsConcept().getConceptId());
    map.put("1268", tbMetadata.getTBTreatmentPlanConcept().getConceptId());
    map.put("1257", hivMetadata.getContinueRegimenConcept().getConceptId());
    map.put("1267", hivMetadata.getCompletedConcept().getConceptId());
    map.put("6128", hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId());

    String query =
        " SELECT  p.patient_id  "
            + " FROM  patient p  "
            + "     INNER JOIN  encounter e ON p.patient_id = e.patient_id  "
            + "     INNER JOIN  obs o ON e.encounter_id = o.encounter_id  "
            + "     INNER JOIN (SELECT tpt_start.patient_id, MAX(tpt_start.last_encounter) tpt_start_date "
            + "            FROM ( "
            + "                     SELECT p.patient_id, MAX(o.value_datetime) last_encounter "
            + "                     FROM patient p "
            + "                              INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                              INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                     WHERE p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o.concept_id = ${6128} "
            + "                       AND e.encounter_type = ${53} "
            + "                       AND e.location_id = :location "
            + "                       AND o.value_datetime BETWEEN :startDate AND :endDate "
            + "                     GROUP BY p.patient_id "
            + "                     UNION "
            + "                     SELECT p.patient_id, MAX(e.encounter_datetime) last_encounter "
            + "                     FROM patient p "
            + "                              INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                              INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                     WHERE p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o.concept_id = ${6122} "
            + "                       AND o.value_coded = ${1256} "
            + "                       AND e.encounter_type = ${6} "
            + "                       AND e.location_id = :location "
            + "                       AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "                     GROUP BY p.patient_id "
            + "                 ) AS tpt_start "
            + "            GROUP BY tpt_start.patient_id ) AS last ON last.patient_id = p.patient_id  "
            + " WHERE  "
            + "     e.location_id = :location  "
            + "         AND e.encounter_type = ${6}  "
            + "         AND o.concept_id = ${1268}  "
            + "         AND o.value_coded IN (${1256} , ${1257}, ${1267})  "
            + "         AND DATE(o.obs_datetime) between DATE(last.tpt_start_date) AND DATE(DATE_ADD(last.tpt_start_date, INTERVAL 9 MONTH))  "
            + "         AND p.voided = 0  "
            + "         AND e.voided = 0  "
            + "         AND o.voided = 0;";

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
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition startedART = getMOHArtStartDate();

    CohortDefinition nutritionalClass = getNutritionalBCat5();

    CohortDefinition pregnant =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition breastfeeding =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());
    CohortDefinition transferredIn =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    CohortDefinition nutSupport = getPatientsWithNutritionalStateAndNutritionalSupport();

    compositionCohortDefinition.addSearch("A", EptsReportUtils.map(startedART, MAPPING));

    compositionCohortDefinition.addSearch("B", EptsReportUtils.map(nutritionalClass, MAPPING));

    compositionCohortDefinition.addSearch("C", EptsReportUtils.map(pregnant, MAPPING));

    compositionCohortDefinition.addSearch("D", EptsReportUtils.map(breastfeeding, MAPPING));

    compositionCohortDefinition.addSearch("E", EptsReportUtils.map(transferredIn, MAPPING));

    compositionCohortDefinition.addSearch("F", EptsReportUtils.map(nutSupport, MAPPING));

    if (den) {
      compositionCohortDefinition.setCompositionString("(A AND B) AND NOT (C OR D OR E)");
    } else {
      compositionCohortDefinition.setCompositionString("(A AND B) AND NOT (C OR D OR E) AND F");
    }
    return compositionCohortDefinition;
  }

  /**
   * B - Filter all patients with nutritional state equal to “DAM” or “DAG” registered on a clinical
   * consultation during the period:
   *
   * <p>All patients registered in Ficha Clinica (encounter type=6) with “CLASSIFICAÇÃO DE
   * DESNUTRIÇÃO” (concept_id = 6336) and value_coded equal to “DAG” (concept_id = 1844) or equal to
   * “DAM” (concept_id = 68) during the encounter_datetime >= startDateRevision and
   * <=endDateRevision
   *
   * @return SqlCohortDefinition
   */
  public SqlCohortDefinition getNutritionalBCat5() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" All patients that started ART during inclusion period ");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Date.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1844", hivMetadata.getChronicMalnutritionConcept().getConceptId());
    map.put("68", hivMetadata.getMalnutritionConcept().getConceptId());
    map.put("6336", commonMetadata.getClassificationOfMalnutritionConcept().getConceptId());

    String query =
        " SELECT p.patient_id "
            + "FROM   patient p "
            + "           INNER JOIN(SELECT p.patient_id, "
            + "                             Min(e.encounter_datetime) AS min_nutritional "
            + "                      FROM   patient p "
            + "                                 INNER JOIN encounter e "
            + "                                            ON e.patient_id = p.patient_id "
            + "                                 INNER JOIN obs o "
            + "                                            ON o.encounter_id = e.encounter_id "
            + "                      WHERE  p.voided = 0 "
            + "                        AND e.voided = 0 "
            + "                        AND o.voided = 0 "
            + "                        AND e.encounter_type = ${6} "
            + "                        AND o.concept_id = ${6336} "
            + "                        AND o.value_coded IN (${1844},${68}) "
            + "                        AND e.location_id = :location "
            + "                        AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "                      GROUP  BY p.patient_id) AS list "
            + "                     ON list.patient_id = p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
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
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition startedART = getMOHArtStartDate();

    CohortDefinition nutritionalClass = getNutritionalBCat5();

    CohortDefinition pregnant =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition breastfeeding =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition transferredIn =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    CohortDefinition nutSupport = getPatientsWithNutritionalStateAndNutritionalSupport();

    compositionCohortDefinition.addSearch("A", EptsReportUtils.map(startedART, MAPPING));

    compositionCohortDefinition.addSearch("B", EptsReportUtils.map(nutritionalClass, MAPPING));

    compositionCohortDefinition.addSearch("C", EptsReportUtils.map(pregnant, MAPPING));

    compositionCohortDefinition.addSearch("D", EptsReportUtils.map(breastfeeding, MAPPING));

    compositionCohortDefinition.addSearch("E", EptsReportUtils.map(transferredIn, MAPPING));

    compositionCohortDefinition.addSearch("F", EptsReportUtils.map(nutSupport, MAPPING));

    if (den) {
      compositionCohortDefinition.setCompositionString("(A AND B AND C) AND NOT (D OR E)");
    } else {
      compositionCohortDefinition.setCompositionString("(A AND B AND C) AND NOT (D OR E) AND F");
    }

    return compositionCohortDefinition;
  }

  /**
   * <b>MQ6</b>: Melhoria de Qualidade Category 6 <br>
   * <i> DENOMINATOR 1: A AND NOT (B OR C OR D OR E)</i> <br>
   * <i> DENOMINATOR 2: A AND NOT (B OR C OR D OR E)</i> <br>
   * <i> DENOMINATOR 3: (A AND C) AND NOT (B OR D OR E)</i> <br>
   * <i> DENOMINATOR 4: A AND D AND NOT (B OR C OR E)</i> <br>
   *
   * <ul>
   *   <li>A - Select all patients who initiated ART during the Inclusion period (startDateInclusion
   *       and endDateInclusion)
   *   <li>
   *   <li>B - B - Filter all patients with the last clinical consultation(encounter type 6) with
   *       “Diagnótico TB activo” (concept id 23761) and value coded “SIM”(concept id 1065) and
   *       Encounter_datetime between startDateInclusion and endDateRevision
   *   <li>
   *   <li>C - All female patients registered as “Pregnant” on a clinical consultation during the
   *       inclusion period (startDateInclusion and endDateInclusion)
   *   <li>
   *   <li>D - All female patients registered as “Breastfeeding” on a clinical consultation during
   *       the inclusion period (startDateInclusion and endDateInclusion)
   *   <li>
   *   <li>E - All transferred IN patients during the inclusion period
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQ6A(Integer den) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    if (den == 1) {
      compositionCohortDefinition.setName(
          "% de adultos HIV+ em TARV rastreados para TB na última consulta clínica");
    } else if (den == 2) {
      compositionCohortDefinition.setName(
          "% de crianças HIV+ em TARV rastreadas para TB na última consulta clínica");
    } else if (den == 3) {
      compositionCohortDefinition.setName(
          "% de mulheres grávidas HIV+ rastreadas para TB na última consulta clínica");
    } else if (den == 4) {
      compositionCohortDefinition.setName(
          "% de mulheres lactantes HIV+ rastreadas para TB  na última consulta");
    }
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    String mapping = "startDate=${startDate},endDate=${revisionEndDate},location=${location}";

    CohortDefinition startedART = getMOHArtStartDate();

    CohortDefinition tbActive =
        commonCohortQueries.getMohMQPatientsOnCondition(
            false,
            false,
            "last",
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getActiveTBConcept(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition pregnant =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition breastfeeding =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());
    CohortDefinition transferredIn =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    compositionCohortDefinition.addSearch("A", EptsReportUtils.map(startedART, MAPPING));

    compositionCohortDefinition.addSearch("B", EptsReportUtils.map(tbActive, mapping));

    compositionCohortDefinition.addSearch("C", EptsReportUtils.map(pregnant, MAPPING));

    compositionCohortDefinition.addSearch("D", EptsReportUtils.map(breastfeeding, MAPPING));

    compositionCohortDefinition.addSearch("E", EptsReportUtils.map(transferredIn, MAPPING));

    if (den == 1 || den == 2) {
      compositionCohortDefinition.setCompositionString("A AND NOT (B OR C OR D OR E)");
    } else if (den == 3) {
      compositionCohortDefinition.setCompositionString("(A AND C) AND NOT (B OR D OR E)");
    } else if (den == 4) {
      compositionCohortDefinition.setCompositionString("A AND D AND NOT (B OR C OR E)");
    }
    return compositionCohortDefinition;
  }

  /**
   * <b>MQ6NUM</b>: Melhoria de Qualidade Category 6 <br>
   * <i> NUMERATOR 1: (A AND F) AND NOT (B OR C OR D OR E)</i> <br>
   * <i> NUMERATOR 2: A AND F NOT (B OR C OR D OR E)</i> <br>
   * <i> NUMERATOR 3: (A AND C AND F) AND NOT (B OR D OR E)</i> <br>
   * <i> NUMERATOR 4: (A AND D AND F) AND NOT (B OR C OR E)</i> <br>
   *
   * <ul>
   *   <li>A - Select all patients who initiated ART during the Inclusion period (startDateInclusion
   *       and endDateInclusion)
   *   <li>
   *   <li>B - B - Filter all patients with the last clinical consultation(encounter type 6) with
   *       “Diagnótico TB activo” (concept id 23761) and value coded “SIM”(concept id 1065) and
   *       Encounter_datetime between startDateInclusion and endDateRevision
   *   <li>
   *   <li>C - All female patients registered as “Pregnant” on a clinical consultation during the
   *       inclusion period (startDateInclusion and endDateInclusion)
   *   <li>
   *   <li>D - All female patients registered as “Breastfeeding” on a clinical consultation during
   *       the inclusion period (startDateInclusion and endDateInclusion)
   *   <li>
   *   <li>E - All transferred IN patients during the inclusion period
   *   <li>
   *   <li>D - Filter all patients with the last Ficha Clinica(encounter type 6) during the revision
   *       period with the following conditions: “TEM SINTOMAS DE TB” (concept id 23758) value coded
   *       “SIM” or “NÃO”(concept id IN [1065, 1066]) and Encounter_datetime between
   *       startDateRevision and endDateRevision (should be the last encounter during the revision
   *       period)
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQ6NUM(Integer num) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    if (num == 1) {
      compositionCohortDefinition.setName(
          "% de adultos HIV+ em TARV rastreados para TB na última consulta clínica");
    } else if (num == 2) {
      compositionCohortDefinition.setName(
          "% de crianças HIV+ em TARV rastreadas para TB na última consulta clínica");
    } else if (num == 3) {
      compositionCohortDefinition.setName(
          "% de mulheres grávidas HIV+ rastreadas para TB na última consulta clínica");
    } else if (num == 4) {
      compositionCohortDefinition.setName(
          "% de mulheres lactantes HIV+ rastreadas para TB  na última consulta");
    }
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    String mapping = "startDate=${startDate},endDate=${revisionEndDate},location=${location}";

    CohortDefinition startedART = getMOHArtStartDate();

    CohortDefinition tbActive =
        commonCohortQueries.getMohMQPatientsOnCondition(
            false,
            false,
            "last",
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getActiveTBConcept(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition pregnant =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition breastfeeding =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());
    CohortDefinition transferredIn =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    CohortDefinition tbSymptoms =
        QualityImprovement2020Queries.getPatientsWithTBSymptoms(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getTBSymptomsConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getNoConcept().getConceptId());

    compositionCohortDefinition.addSearch("A", EptsReportUtils.map(startedART, MAPPING));

    compositionCohortDefinition.addSearch("B", EptsReportUtils.map(tbActive, mapping));

    compositionCohortDefinition.addSearch("C", EptsReportUtils.map(pregnant, MAPPING));

    compositionCohortDefinition.addSearch("D", EptsReportUtils.map(breastfeeding, MAPPING));

    compositionCohortDefinition.addSearch("E", EptsReportUtils.map(transferredIn, MAPPING));

    compositionCohortDefinition.addSearch("F", EptsReportUtils.map(tbSymptoms, MAPPING1));

    if (num == 1 || num == 2) {
      compositionCohortDefinition.setCompositionString("(A AND F) AND NOT (B OR C OR D OR E)");
    } else if (num == 3) {
      compositionCohortDefinition.setCompositionString("(A AND C AND F) AND NOT (B OR D OR E)");
    } else if (num == 4) {
      compositionCohortDefinition.setCompositionString("(A AND D AND F) AND NOT (B OR C OR E)");
    }

    return compositionCohortDefinition;
  }

  /**
   * <b>MQ7</b>: Melhoria de Qualidade Category 7 <br>
   * <i> DENOMINATOR 1: A AND NOT (B1 OR B2 OR B3 OR C OR D OR E OR F)</i> <br>
   * <i> DENOMINATOR 2: (A AND B4) AND NOT (B1 OR B2 OR B3 OR C OR D OR E OR F)</i> <br>
   * <i> DENOMINATOR 3: A AND NOT (B1 OR B2 OR B3 OR C OR D OR E OR F)</i> <br>
   * <i> DENOMINATOR 4: (A AND B4) AND NOT (B1 OR B2 OR B3 OR C OR D OR E OR F)</i> <br>
   * <i> DENOMINATOR 5: (A AND C) AND NOT (B1 OR B2 OR B3 OR D OR E OR F)</i> <br>
   * <i> DENOMINATOR 6: (A AND B4 AND C) AND NOT (B1 OR B2 OR B3 OR D OR E OR F)</i> <br>
   *
   * <ul>
   *   <li>A - Select all patients who initiated ART during the Inclusion period (startDateInclusion
   *       and endDateInclusion)
   *   <li>
   *   <li>B1 - Filter all patients with a clinical consultation(encounter type 6) with “Diagnótico
   *       TB activo” (concept id 23761) and value coded “SIM”(concept id 1065) and
   *       Encounter_datetime between startDateInclusion and endDateRevision
   *   <li>
   *   <li>
   *   <li>B2 - Filter all patients with a clinical consultation(encounter type 6) with “TEM
   *       SINTOMAS DE TB” (concept_id 23758) value coded “SIM” (concept_id 1065) and
   *       Encounter_datetime between startDateInclusion and endDateInclusion
   *   <li>
   *   <li>
   *   <li>B3 - Filter all patients with a clinical consultation(encounter type 6) with “TRATAMENTO
   *       DE TUBERCULOSE”(concept_id 1268) value coded “Inicio” or “Continua” or “Fim” (concept_id
   *       IN [1256, 1257, 1267]) Encounter_datetime between startDateInclusion and endDateInclusion
   *   <li>
   *   <li>
   *   <li>B4 - Filter all patients with a clinical consultation(encounter type 6) with “PROFILAXIA
   *       COM ISONIAZIDA”(concept_id 6122) value coded “Inicio” (concept_id 1256)
   *       Encounter_datetime between startDateInclusion and endDateInclusion
   *   <li>
   *   <li>C - All female patients registered as “Pregnant” on a clinical consultation during the
   *       inclusion period (startDateInclusion and endDateInclusion)
   *   <li>
   *   <li>D - All female patients registered as “Breastfeeding” on a clinical consultation during
   *       the inclusion period (startDateInclusion and endDateInclusion)
   *   <li>
   *   <li>E - All transferred IN patients during the inclusion period
   *   <li>
   *   <li>F - Filter all patients with the last clinical consultation(encounter type 6) with
   *       “Diagnótico TB activo” (concept id 23761) and value coded “SIM”(concept id 1065) and
   *       Encounter_datetime between startDateInclusion and endDateRevision
   *   <li>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQ7A(Integer den) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    if (den == 1 || den == 3) {
      compositionCohortDefinition.setName("A AND NOT (B1 OR B2 OR B3 OR C OR D OR E OR F)");
    } else if (den == 2 || den == 4) {
      compositionCohortDefinition.setName(
          "(A AND B4) AND NOT (B1 OR B2 OR B3 OR C OR D OR E OR F OR H OR I OR J)");
    } else if (den == 5) {
      compositionCohortDefinition.setName("(A AND C) AND NOT (B1 OR B2 OR B3 OR D OR E OR F)");
    } else if (den == 6) {
      compositionCohortDefinition.setName(
          "(A AND B4 AND C) AND NOT (B1 OR B2 OR B3 OR D OR E OR F OR H OR I OR J)");
    }
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition startedART = getMOHArtStartDate();

    CohortDefinition b41 = getB4And1();
    CohortDefinition b42 = getB4And2();

    CohortDefinition tbActive =
        commonCohortQueries.getMohMQPatientsOnCondition(
            false,
            false,
            "once",
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getActiveTBConcept(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition tbSymptoms =
        commonCohortQueries.getMohMQPatientsOnCondition(
            false,
            false,
            "once",
            hivMetadata.getAdultoSeguimentoEncounterType(),
            tbMetadata.getHasTbSymptomsConcept(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition tbTreatment =
        commonCohortQueries.getMohMQPatientsOnCondition(
            false,
            false,
            "once",
            hivMetadata.getAdultoSeguimentoEncounterType(),
            tbMetadata.getTBTreatmentPlanConcept(),
            Arrays.asList(
                tbMetadata.getStartDrugsConcept(),
                hivMetadata.getContinueRegimenConcept(),
                hivMetadata.getCompletedConcept()),
            null,
            null);

    CohortDefinition tbProphilaxy =
        commonCohortQueries.getMohMQPatientsOnCondition(
            false,
            false,
            "once",
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getIsoniazidUsageConcept(),
            Collections.singletonList(hivMetadata.getStartDrugsConcept()),
            null,
            null);

    CohortDefinition pregnant =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition breastfeeding =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition transferredIn =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    CohortDefinition transferOut = commonCohortQueries.getTranferredOutPatients();

    CohortDefinition tbDiagOnPeriod = getPatientsWithTBDiagActive();

    CohortDefinition tbSymptomsOnPeriod = getPatientsWithTBSymtoms();

    CohortDefinition tbTreatmentOnPeriod = getPatientsWithTBTreatment();

    compositionCohortDefinition.addSearch("A", EptsReportUtils.map(startedART, MAPPING));

    compositionCohortDefinition.addSearch("B1", EptsReportUtils.map(tbActive, MAPPING));

    compositionCohortDefinition.addSearch("B2", EptsReportUtils.map(tbSymptoms, MAPPING));

    compositionCohortDefinition.addSearch("B3", EptsReportUtils.map(tbTreatment, MAPPING));

    compositionCohortDefinition.addSearch("B4", EptsReportUtils.map(tbProphilaxy, MAPPING));

    compositionCohortDefinition.addSearch("C", EptsReportUtils.map(pregnant, MAPPING));

    compositionCohortDefinition.addSearch("D", EptsReportUtils.map(breastfeeding, MAPPING));

    compositionCohortDefinition.addSearch("E", EptsReportUtils.map(transferredIn, MAPPING));

    compositionCohortDefinition.addSearch("F", EptsReportUtils.map(transferOut, MAPPING1));

    compositionCohortDefinition.addSearch("H", EptsReportUtils.map(tbDiagOnPeriod, MAPPING));

    compositionCohortDefinition.addSearch("I", EptsReportUtils.map(tbSymptomsOnPeriod, MAPPING));

    compositionCohortDefinition.addSearch("J", EptsReportUtils.map(tbTreatmentOnPeriod, MAPPING));

    compositionCohortDefinition.addSearch("B41", EptsReportUtils.map(b41, MAPPING));

    compositionCohortDefinition.addSearch("B42", EptsReportUtils.map(b42, MAPPING));

    if (den == 1 || den == 3) {
      compositionCohortDefinition.setCompositionString(
          "A AND NOT (B1 OR B2 OR B3 OR C OR D OR E OR F)");
    } else if (den == 2 || den == 4) {
      compositionCohortDefinition.setCompositionString(
          "(A AND (B41 OR B42)) AND NOT (B1 OR B2 OR B3 OR C OR D OR E OR F OR H OR I OR J)");
    } else if (den == 5) {
      compositionCohortDefinition.setCompositionString(
          "(A AND C) AND NOT (B1 OR B2 OR B3 OR D OR E OR F)");
    } else if (den == 6) {
      compositionCohortDefinition.setCompositionString(
          "(A AND (B41 OR B42) AND C) AND NOT (B1 OR B2 OR B3 OR D OR E OR F OR H OR I OR J)");
    }

    return compositionCohortDefinition;
  }

  /**
   * <b>MQ7</b>: Melhoria de Qualidade Category 7 <br>
   * <i> DENOMINATOR 1: A AND NOT (B1 OR B2 OR B3 OR C OR D OR E OR F)</i> <br>
   * <i> DENOMINATOR 2: (A AND B4) AND NOT (B1 OR B2 OR B3 OR C OR D OR E OR F)</i> <br>
   * <i> DENOMINATOR 3: A AND NOT (B1 OR B2 OR B3 OR C OR D OR E OR F)</i> <br>
   * <i> DENOMINATOR 4: (A AND B4) AND NOT (B1 OR B2 OR B3 OR C OR D OR E OR F)</i> <br>
   * <i> DENOMINATOR 5: (A AND C) AND NOT (B1 OR B2 OR B3 OR D OR E OR F)</i> <br>
   * <i> DENOMINATOR 6: (A AND B4 AND C) AND NOT (B1 OR B2 OR B3 OR D OR E OR F)</i> <br>
   *
   * <ul>
   *   <li>G - Filter all patients with the last clinical consultation(encounter type 6) with
   *       “PROFILAXIA COM ISONIAZIDA”(concept_id 6122) value coded “Fim” (concept_id 1267)
   *       Encounter_datetime between startDateRevision and endDateRevision and
   *       Encounter_datetime(the most recent from B4) minus Encounter_datetime(the most recent from
   *       G) between 6 months and 9 months
   *   <li>
   *   <li>
   *   <li>H - Filter all patients with a clinical consultation(encounter type 6) with “Diagnótico
   *       TB activo” (concept_id 23761) value coded “SIM”(concept id 1065) during the treatment
   *       period: Encounter_datetime between Encounter_datetime(the most recent from B4) and
   *       Encounter_datetime(the most recent from B4) + 9 months
   *   <li>
   *   <li>
   *   <li>I - Filter all patients with a clinical consultation(encounter type 6) during the
   *       Inclusion period with the following conditions: “TEM SINTOMAS DE TB” (concept_id 23758)
   *       value coded “SIM” or “NÃO”(concept_id IN [1065, 1066]) and Encounter_datetime between
   *       Encounter_datetime(the most recent from B4) and Encounter_datetime(the most recent from
   *       B4) + 9 months
   *   <li>
   *   <li>
   *   <li>J - Filter all patients with a clinical consultation(encounter type 6) during the
   *       Inclusion period with the following conditions: “TRATAMENTO DE TUBERCULOSE”(concept_id
   *       1268) value coded “Inicio” or “Continua” or “Fim”(concept_id IN [1256, 1257, 1267]) “Data
   *       Tratamento TB” (obs datetime 1268) between Encounter_datetime(the most recent from B4)
   *       and Encounter_datetime(the most recent from B4) + 9 months
   *   <li>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQ7B(Integer num) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    if (num == 1 || num == 3) {
      compositionCohortDefinition.setName(
          "(A AND B4) AND NOT (B1 OR B2 OR B3 OR C OR D OR E OR F)");
    } else if (num == 2 || num == 4) {
      compositionCohortDefinition.setName(
          "(A AND B4 AND G) AND NOT (B1 OR B2 OR B3 OR C OR D OR E OR F )");
    } else if (num == 5) {
      compositionCohortDefinition.setName(
          "(A AND C AND B4) AND NOT (B1 OR B2 OR B3 OR D OR E OR F)");
    } else if (num == 6) {
      compositionCohortDefinition.setName(
          "(A AND B4 AND C AND G) AND NOT (B1 OR B2 OR B3 OR D OR E OR F OR H OR I OR J)");
    }
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition startedART = getMOHArtStartDate();

    CohortDefinition tbActive =
        commonCohortQueries.getMohMQPatientsOnCondition(
            false,
            false,
            "once",
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getActiveTBConcept(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition tbSymptoms =
        commonCohortQueries.getMohMQPatientsOnCondition(
            false,
            false,
            "once",
            hivMetadata.getAdultoSeguimentoEncounterType(),
            tbMetadata.getHasTbSymptomsConcept(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition tbTreatment =
        commonCohortQueries.getMohMQPatientsOnCondition(
            false,
            false,
            "once",
            hivMetadata.getAdultoSeguimentoEncounterType(),
            tbMetadata.getTBTreatmentPlanConcept(),
            Arrays.asList(
                tbMetadata.getStartDrugsConcept(),
                hivMetadata.getContinueRegimenConcept(),
                hivMetadata.getCompletedConcept()),
            null,
            null);

    CohortDefinition tbProphilaxy =
        commonCohortQueries.getMohMQPatientsOnCondition(
            false,
            false,
            "once",
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getIsoniazidUsageConcept(),
            Collections.singletonList(hivMetadata.getStartDrugs()),
            null,
            null);

    CohortDefinition pregnant =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition breastfeeding =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition transferredIn =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    CohortDefinition transferOut = commonCohortQueries.getTranferredOutPatients();

    CohortDefinition tbProphylaxyOnPeriod = getPatientsWithProphylaxyDuringRevisionPeriod();

    CohortDefinition tbDiagOnPeriod = getPatientsWithTBDiagActive();

    CohortDefinition tbSymptomsOnPeriod = getPatientsWithTBSymtoms();

    CohortDefinition tbTreatmentOnPeriod = getPatientsWithTBTreatment();

    CohortDefinition b41 = getB4And1();

    CohortDefinition b42 = getB4And2();

    compositionCohortDefinition.addSearch("A", EptsReportUtils.map(startedART, MAPPING));

    compositionCohortDefinition.addSearch("B1", EptsReportUtils.map(tbActive, MAPPING));

    compositionCohortDefinition.addSearch("B2", EptsReportUtils.map(tbSymptoms, MAPPING));

    compositionCohortDefinition.addSearch("B3", EptsReportUtils.map(tbTreatment, MAPPING));

    compositionCohortDefinition.addSearch("B4", EptsReportUtils.map(tbProphilaxy, MAPPING));

    compositionCohortDefinition.addSearch("C", EptsReportUtils.map(pregnant, MAPPING));

    compositionCohortDefinition.addSearch("D", EptsReportUtils.map(breastfeeding, MAPPING));

    compositionCohortDefinition.addSearch("E", EptsReportUtils.map(transferredIn, MAPPING));

    compositionCohortDefinition.addSearch("F", EptsReportUtils.map(transferOut, MAPPING1));

    compositionCohortDefinition.addSearch("G", EptsReportUtils.map(tbProphylaxyOnPeriod, MAPPING1));

    compositionCohortDefinition.addSearch("H", EptsReportUtils.map(tbDiagOnPeriod, MAPPING));

    compositionCohortDefinition.addSearch("I", EptsReportUtils.map(tbSymptomsOnPeriod, MAPPING));

    compositionCohortDefinition.addSearch("J", EptsReportUtils.map(tbTreatmentOnPeriod, MAPPING));

    compositionCohortDefinition.addSearch("B41", EptsReportUtils.map(b41, MAPPING));

    compositionCohortDefinition.addSearch("B42", EptsReportUtils.map(b42, MAPPING));

    compositionCohortDefinition.addSearch("GNEW", EptsReportUtils.map(getGNew(), MAPPING1));

    if (num == 1 || num == 3) {
      compositionCohortDefinition.setCompositionString(
          "(A AND  (B41 OR B42)) AND NOT (B1 OR B2 OR B3 OR C OR D OR E OR F)");
    } else if (num == 2 || num == 4) {
      compositionCohortDefinition.setCompositionString(
          "(A AND (B41 OR B42) AND GNEW) AND NOT (B1 OR B2 OR B3 OR C OR D OR E OR F OR H OR I OR J)");
    } else if (num == 5) {
      compositionCohortDefinition.setCompositionString(
          "(A AND C AND (B41 OR B42) ) AND NOT (B1 OR B2 OR B3 OR D OR E OR F)");
    } else if (num == 6) {
      compositionCohortDefinition.setCompositionString(
          "(A AND (B41 OR B42) AND C AND GNEW) AND NOT (B1 OR B2 OR B3 OR D OR E OR F OR H OR I OR J)");
    }
    return compositionCohortDefinition;
  }

  /**
   * <b>MQC11</b>: Melhoria de Qualidade Category 11 Denominator <br>
   * <i> DENOMINATORS: A,B1,B2,B3,C,D and E</i> <br>
   *
   * <ul>
   *   <li>A - Select all patients who initiated ART during the Inclusion period (startDateInclusion
   *       and endDateInclusion)
   *   <li>
   *   <li>B1 - Select all patients from Ficha Clinica (encounter type 6) who have THE LAST “LINHA
   *       TERAPEUTICA
   *   <li>
   *   <li>B2-Select all patients from Ficha Clinica (encounter type 6) with “Carga Viral”
   *       registered with numeric value > 1000
   *   <li>
   *   <li>B3-Filter all patients with clinical consultation (encounter type 6) with concept
   *       “GESTANTE” and value coded “SIM”
   *   <li>
   *   <li>C - All female patients registered as “Pregnant” on a clinical consultation during the
   *       inclusion period (startDateInclusion and endDateInclusion)
   *   <li>
   *   <li>D - All female patients registered as “Breastfeeding” on a clinical consultation during
   *       the inclusion period (startDateInclusion and endDateInclusion)
   *   <li>
   *   <li>E - All transferred IN patients
   *   <li>
   *   <li>F - All Transferred Out patients
   * </ul>
   *
   * @return CohortDefinition
   * @params indicatorFlag A to G For inicator 11.1 to 11.7 respectively
   */
  public CohortDefinition getMQC11DEN(int indicatorFlag, String reportSource) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName(
        "% adultos em TARV com o mínimo de 3 consultas de seguimento de adesão na FM-ficha de APSS/PP");

    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition startedART = getMOHArtStartDate();

    CohortDefinition patientsFromFichaClinicaLinhaTerapeutica =
        getPatientsFromFichaClinicaWithLastTherapeuticLineSetAsFirstLine_B1();

    CohortDefinition patientsFromFichaClinicaCargaViral = getB2_13();

    CohortDefinition pregnantWithCargaViralHigherThan1000 =
        QualityImprovement2020Queries.getMQ13DenB4_P4(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getHivViralLoadConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId(),
            commonMetadata.getPregnantConcept().getConceptId());

    CohortDefinition breastfeedingWithCargaViralHigherThan1000 =
        QualityImprovement2020Queries.getMQ13DenB5_P4(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getHivViralLoadConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId(),
            commonMetadata.getBreastfeeding().getConceptId());

    CohortDefinition pregnant =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition breastfeeding =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition transferredIn =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    CohortDefinition transfOut = commonCohortQueries.getTranferredOutPatients();

    if (reportSource.equals("MQ")) {
      compositionCohortDefinition.addSearch("A", EptsReportUtils.map(startedART, MAPPING));
      compositionCohortDefinition.addSearch(
          "B1", EptsReportUtils.map(patientsFromFichaClinicaLinhaTerapeutica, MAPPING1));

      compositionCohortDefinition.addSearch(
          "B2", EptsReportUtils.map(patientsFromFichaClinicaCargaViral, MAPPING1));

      compositionCohortDefinition.addSearch(
          "B4", EptsReportUtils.map(pregnantWithCargaViralHigherThan1000, MAPPING1));

      compositionCohortDefinition.addSearch(
          "B5", EptsReportUtils.map(breastfeedingWithCargaViralHigherThan1000, MAPPING1));

      compositionCohortDefinition.addSearch("C", EptsReportUtils.map(pregnant, MAPPING));
      compositionCohortDefinition.addSearch("D", EptsReportUtils.map(breastfeeding, MAPPING));
      compositionCohortDefinition.addSearch("E", EptsReportUtils.map(transferredIn, MAPPING));
      compositionCohortDefinition.addSearch("F", EptsReportUtils.map(transfOut, MAPPING1));
    } else if (reportSource.equals("MI")) {

      if (indicatorFlag == 1) {
        compositionCohortDefinition.addSearch("F", EptsReportUtils.map(transfOut, MAPPING5));
      }
      if (indicatorFlag == 1 || indicatorFlag == 3 || indicatorFlag == 5 || indicatorFlag == 6) {
        compositionCohortDefinition.addSearch("A", EptsReportUtils.map(startedART, MAPPING4));
        compositionCohortDefinition.addSearch("C", EptsReportUtils.map(pregnant, MAPPING6));
        compositionCohortDefinition.addSearch("D", EptsReportUtils.map(breastfeeding, MAPPING6));
        compositionCohortDefinition.addSearch("E", EptsReportUtils.map(transferredIn, MAPPING6));
      } else {
        compositionCohortDefinition.addSearch("A", EptsReportUtils.map(startedART, MAPPING));
      }
      if (indicatorFlag == 2 || indicatorFlag == 4 || indicatorFlag == 7) {
        compositionCohortDefinition.addSearch(
            "B1", EptsReportUtils.map(patientsFromFichaClinicaLinhaTerapeutica, MAPPING8));

        compositionCohortDefinition.addSearch(
            "B2", EptsReportUtils.map(patientsFromFichaClinicaCargaViral, MAPPING8));

        compositionCohortDefinition.addSearch(
            "B4", EptsReportUtils.map(pregnantWithCargaViralHigherThan1000, MAPPING8));

        compositionCohortDefinition.addSearch(
            "B5", EptsReportUtils.map(breastfeedingWithCargaViralHigherThan1000, MAPPING8));

        compositionCohortDefinition.addSearch("C", EptsReportUtils.map(pregnant, MAPPING));
        compositionCohortDefinition.addSearch("D", EptsReportUtils.map(breastfeeding, MAPPING));
        compositionCohortDefinition.addSearch("E", EptsReportUtils.map(transferredIn, MAPPING7));
        compositionCohortDefinition.addSearch("F", EptsReportUtils.map(transfOut, MAPPING9));
      }
      if (indicatorFlag == 3 || indicatorFlag == 5 || indicatorFlag == 6) {
        compositionCohortDefinition.addSearch("F", EptsReportUtils.map(transfOut, MAPPING5));
      }
    }

    if (indicatorFlag == 1 || indicatorFlag == 5 || indicatorFlag == 6) {
      compositionCohortDefinition.setCompositionString("A AND NOT (C OR D OR E OR F)");
    }
    if (indicatorFlag == 2 || indicatorFlag == 7) {
      compositionCohortDefinition.setCompositionString("(B1 AND B2) AND NOT (B5 OR E OR F OR B4)");
    }
    if (indicatorFlag == 3) {
      compositionCohortDefinition.setCompositionString("(A AND C) AND NOT (D OR E OR F)");
    }
    if (indicatorFlag == 4) {
      compositionCohortDefinition.setCompositionString("(B1 AND B4) AND NOT (B5 OR E OR F)");
    }

    return compositionCohortDefinition;
  }
  /**
   * <b>MQ12</b>: Melhoria de Qualidade Category 12 Denominator Part 2 <br>
   * <i> DENOMINATORS: A,B1,B1E,B2,B2E,C,D and E</i> <br>
   *
   * <ul>
   *   <li>A - Select all patients who initiated ART during the period (startDateInclusion =
   *       endDateRevision - 14 months and endDateInclusion = endDateRevision - 11 months)
   *   <li>
   *   <li>B1- Select all patients from Ficha Clinica (encounter type 6) who have THE LAST “LINHA
   *       TERAPEUTICA”(Concept id 21151) during the Inclusion period (startDateInclusion =
   *       endDateRevision - 14 months and endDateInclusion = endDateRevision - 11 months) and the
   *       value coded is “PRIMEIRA LINHA”(Concept id 21150)
   *   <li>
   *   <li>B1E- Select all patients from Ficha Clinica (encounter type 6) who have “LINHA
   *       TERAPEUTICA”(Concept id 21151) with value coded DIFFERENT THAN “PRIMEIRA LINHA”(Concept
   *       id 21150) during the period (startDate = endDateRevision - 14 months and endDateRevision)
   *   <li>
   *   <li>B2- Select all patients from Ficha Clinica (encounter type 6) who have THE LAST “LINHA
   *       TERAPEUTICA”(Concept id 21151) during the Inclusion period (startDateInclusion =
   *       endDateRevision - 14 months and endDateInclusion = endDateRevision - 11 months) and the
   *       value coded is “SEGUNDA LINHA”(Concept id 21148)
   *   <li>
   *   <li>C - All female patients registered as “Pregnant” on a clinical consultation during the
   *       inclusion period (startDateInclusion and endDateInclusion)
   *   <li>
   *   <li>D - All female patients registered as “Breastfeeding” on a clinical consultation during
   *       the inclusion period (startDateInclusion and endDateInclusion)
   *   <li>
   *   <li>E - All transferred IN patients
   *   <li>
   *   <li>F - All Transferred Out patients
   * </ul>
   *
   * @return CohortDefinition
   * @params indicatorFlag
   */
  public CohortDefinition getMQC12P2DEN(Integer indicatorFlag) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    if (indicatorFlag == 3)
      compositionCohortDefinition.setName(
          "Adultos (15/+anos) na 1ª linha que iniciaram o TARV há 12 meses atrás");
    if (indicatorFlag == 4)
      compositionCohortDefinition.setName(
          "Adultos (15/+anos) que iniciaram 2ª linha TARV há 12 meses atrás");
    if (indicatorFlag == 7)
      compositionCohortDefinition.setName(
          "Crianças (0-14 anos) na 1ª linha que iniciaram o TARV há 12 meses atrás");
    if (indicatorFlag == 8)
      compositionCohortDefinition.setName(
          "Crianças (0-14 anos) que iniciaram 2ª linha TARV há 12 meses atrás");
    if (indicatorFlag == 11)
      compositionCohortDefinition.setName(
          "Mulheres grávidas HIV+ 1ª linha que iniciaram o TARV há 12 meses atrás");

    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition startedART = getMOHArtStartDate();

    CohortDefinition b1 = getPatientsFromFichaClinicaWithLastTherapeuticLineSetAsFirstLine_B1();

    CohortDefinition b1E = getPatientsFromFichaClinicaDenominatorB1EOrB2E(true);

    CohortDefinition b2 = getPatientsFromFichaClinicaWithLastTherapeuticLineSetAsSecondLine_B2();

    CohortDefinition b2E = getPatientsFromFichaClinicaDenominatorB1EOrB2E(false);

    CohortDefinition pregnant =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition breastfeeding =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition transfOut = commonCohortQueries.getTranferredOutPatients();

    compositionCohortDefinition.addSearch("A", EptsReportUtils.map(startedART, MAPPING2));

    compositionCohortDefinition.addSearch(
        "B1",
        EptsReportUtils.map(
            b1,
            "startDate=${revisionEndDate-14m},endDate=${revisionEndDate-11m},location=${location},revisionEndDate=${revisionEndDate}"));

    compositionCohortDefinition.addSearch(
        "B1E", EptsReportUtils.map(b1E, "location=${location},revisionEndDate=${revisionEndDate}"));

    compositionCohortDefinition.addSearch(
        "B2",
        EptsReportUtils.map(
            b2,
            "startDate=${revisionEndDate-14m},endDate=${revisionEndDate-11m},location=${location},revisionEndDate=${revisionEndDate}"));

    compositionCohortDefinition.addSearch(
        "B2E", EptsReportUtils.map(b2E, "location=${location},revisionEndDate=${revisionEndDate}"));

    compositionCohortDefinition.addSearch("C", EptsReportUtils.map(pregnant, MAPPING));

    compositionCohortDefinition.addSearch("D", EptsReportUtils.map(breastfeeding, MAPPING));

    compositionCohortDefinition.addSearch(
        "F",
        EptsReportUtils.map(
            transfOut,
            "startDate=${revisionEndDate-14m},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "ADULT",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnMOHArtStartDate(15, null, false),
            "onOrAfter=${revisionEndDate-14m},onOrBefore=${revisionEndDate-11m},location=${location}"));

    compositionCohortDefinition.addSearch(
        "CHILDREN",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnMOHArtStartDate(0, 14, true),
            "onOrAfter=${revisionEndDate-14m},onOrBefore=${revisionEndDate-11m},location=${location}"));

    if (indicatorFlag == 3) {
      compositionCohortDefinition.setCompositionString(
          "(A AND B1) AND NOT B1E AND NOT (C OR D OR F) AND ADULT");
    }
    if (indicatorFlag == 4) {
      compositionCohortDefinition.setCompositionString(
          "(A AND B2) AND NOT B2E AND NOT (C OR D OR F) AND ADULT");
    }
    if (indicatorFlag == 7) {
      compositionCohortDefinition.setCompositionString(
          "(A AND B1) AND NOT B1E AND NOT (C OR D OR F) AND CHILDREN");
    }
    if (indicatorFlag == 8) {
      compositionCohortDefinition.setCompositionString(
          "(A AND B2) AND NOT B2E AND NOT (C OR D OR F) AND CHILDREN");
    }
    if (indicatorFlag == 11) {
      compositionCohortDefinition.setCompositionString(
          "(A AND B1 AND C) AND NOT B1E AND NOT (D OR F)");
    }

    return compositionCohortDefinition;
  }

  /**
   * <b>MQC11B1B2</b>: Melhoria de Qualidade Category 11 Deniminator B1 and B2 <br>
   * /** <b>MQC13</b>: Melhoria de Qualidade Category 13 Part 3 Denominator <br>
   * <i> DENOMINATORS: A,B1,B2,B3,C,D and E</i> <br>
   *
   * <ul>
   *   <li>A - Select all patients who initiated ART during the Inclusion period (startDateInclusion
   *       and endDateInclusion)
   *   <li>
   *   <li>B1 - B1= (BI1 and not B1E) : MUDANCA DE REGIME
   *   <li>
   *   <li>B2 = (BI2 and not B2E) - PACIENTES 2a LINHA
   *   <li>
   *   <li>C - All female patients registered as “Pregnant” on a clinical consultation during the
   *       inclusion period (startDateInclusion and endDateInclusion)
   *   <li>
   *   <li>D - All female patients registered as “Breastfeeding” on a clinical consultation during
   *       the inclusion period (startDateInclusion and endDateInclusion)
   *   <li>
   *   <li>E - All transferred IN patients
   *   <li>
   *   <li>F - All Transferred Out patients
   * </ul>
   *
   * @return CohortDefinition
   * @params indicatorFlag A to F For inicator 13.2 to 13.14 accordingly to the specs
   */
  public CohortDefinition getMQC13P3DEN(int indicator) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    if (indicator == 2)
      compositionCohortDefinition.setName(
          "Crianças  (0-4 anos de idade) que iniciaram TARV no período de inclusão");
    if (indicator == 9)
      compositionCohortDefinition.setName(
          "Crianças  (5-9 anos de idade) que iniciaram TARV no período de inclusão");
    if (indicator == 10)
      compositionCohortDefinition.setName(
          "Crianças  (5-9 anos de idade) que iniciaram TARV no período de inclusão");
    if (indicator == 11)
      compositionCohortDefinition.setName(
          "crianças  (10-14 anos de idade) que iniciaram TARV no período de inclusão");
    if (indicator == 5)
      compositionCohortDefinition.setName(
          "Adultos (15/+ anos) que iniciaram a 2a linha do TARV no período de inclusão ");
    if (indicator == 14)
      compositionCohortDefinition.setName(
          "Crianças  > 2 anos que iniciaram a 2a linha do TARV no período de inclusão");

    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    String mapping2 =
        "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}";

    CohortDefinition startedART = getMOHArtStartDate();

    CohortDefinition b1Patients = getPatientsOnRegimeChangeBI1AndNotB1E_B1();

    CohortDefinition b2NewPatients = getPatientsOnRegimeArvSecondLine();

    CohortDefinition pregnant =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition breastfeeding =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition transferredIn =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    CohortDefinition transfOut = commonCohortQueries.getTranferredOutPatients();

    if (indicator == 2) {
      compositionCohortDefinition.addSearch(
          "age",
          EptsReportUtils.map(
              ageCohortQueries.createXtoYAgeCohort(
                  "Adultos (15/+anos) na 2a linha de TARV que receberam o resultado da CV entre o sexto e o nono mês após o início da 2a linha de TARV",
                  15,
                  null),
              "effectiveDate=${endDate}"));
    } else if (indicator == 5) {

      compositionCohortDefinition.addSearch(
          "age",
          EptsReportUtils.map(
              getAgeOnObsDatetime(15, null),
              "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));
    } else if (indicator == 9) {
      compositionCohortDefinition.addSearch(
          "age",
          EptsReportUtils.map(
              ageCohortQueries.createXtoYAgeCohort(
                  "Crianças (0-4 anos de idade) na 1a linha de TARV que receberam o resultado da Carga Viral entre o sexto e o nono mês após o início do TARV",
                  0,
                  4),
              "effectiveDate=${endDate}"));
    } else if (indicator == 10) {
      compositionCohortDefinition.addSearch(
          "age",
          EptsReportUtils.map(
              ageCohortQueries.createXtoYAgeCohort(
                  "Crianças  (5-9 anos de idade) na 1a linha de TARV que receberam o resultado da Carga Viral entre o sexto e o nono mês após o início do TARV",
                  5,
                  9),
              "effectiveDate=${endDate}"));
    } else if (indicator == 11) {
      compositionCohortDefinition.addSearch(
          "age",
          EptsReportUtils.map(
              ageCohortQueries.createXtoYAgeCohort(
                  "Crianças  (10-14 anos de idade) na 1a linha de TARV que receberam o resultado da Carga Viral entre o sexto e o nono mês após o início do TARV",
                  10,
                  14),
              "effectiveDate=${endDate}"));
    } else if (indicator == 14) {
      compositionCohortDefinition.addSearch(
          "age",
          EptsReportUtils.map(
              getAgeOnObsDatetime(2, 14),
              "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));
    }

    compositionCohortDefinition.addSearch("A", EptsReportUtils.map(startedART, MAPPING));

    compositionCohortDefinition.addSearch("B1", EptsReportUtils.map(b1Patients, mapping2));

    compositionCohortDefinition.addSearch("B2New", EptsReportUtils.map(b2NewPatients, MAPPING));

    compositionCohortDefinition.addSearch("C", EptsReportUtils.map(pregnant, MAPPING));

    compositionCohortDefinition.addSearch("D", EptsReportUtils.map(breastfeeding, MAPPING));

    compositionCohortDefinition.addSearch(
        "DD", EptsReportUtils.map(txMlCohortQueries.getDeadPatientsComposition(), MAPPING3));

    compositionCohortDefinition.addSearch("E", EptsReportUtils.map(transferredIn, MAPPING));

    compositionCohortDefinition.addSearch("F", EptsReportUtils.map(transfOut, MAPPING1));

    if (indicator == 2 || indicator == 9 || indicator == 10 || indicator == 11)
      compositionCohortDefinition.setCompositionString(
          "((A AND NOT C AND NOT D) OR B1) AND NOT (F OR E OR DD) AND age");
    if (indicator == 5 || indicator == 14)
      compositionCohortDefinition.setCompositionString("B2New AND NOT (F OR E OR DD) AND age");
    return compositionCohortDefinition;
  }

  /**
   * <b>MQC11B2</b>: Melhoria de Qualidade Category 11 Deniminator B2 <br>
   * <i> A and not B</i> <br>
   *
   * <ul>
   *   <li>B1 – Select all patients from Ficha Clinica (encounter type 6) who have THE LAST “LINHA
   *       TERAPEUTICA”(Concept id 21151) during the Inclusion period (startDateInclusion and
   *       endDateInclusion) and the value coded is “PRIMEIRA LINHA”(Concept id 21150)
   *   <li>B2 – Select all patients from Ficha Clinica (encounter type 6) with “Carga Viral”
   *       (Concept id 856) registered with numeric value > 1000 during the Inclusion period
   *       (startDateInclusion and endDateInclusion)
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsFromFichaClinicaDenominatorB(String key) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "All patients from Ficha Clinica who have THE LAST “LINHA TERAPEUTICA” or with “Carga Viral");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("21151", hivMetadata.getTherapeuticLineConcept().getConceptId());
    map.put("21150", hivMetadata.getFirstLineConcept().getConceptId());
    map.put("21148", hivMetadata.getSecondLineConcept().getConceptId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String query =
        "SELECT p.patient_id FROM patient p INNER JOIN (SELECT p.patient_id, MAX(e.encounter_datetime), e.encounter_id ";
    String queryTermination =
        " GROUP BY p.patient_id) filtered ON p.patient_id = filtered.patient_id ";
    String valueQuery = "";

    if (key.equals("B1")) {
      valueQuery = " AND o.concept_id = ${21151} AND o.value_coded = ${21150} ";
    }
    if (key.equals("B1E")) {
      valueQuery = " AND o.concept_id = ${21151} AND o.value_coded <> ${21150} ";
    }
    if (key.equals("B2_11")) {
      query = "SELECT p.patient_id ";
      valueQuery = " AND o.concept_id = ${856} AND o.value_numeric > 1000 ";
      queryTermination = "";
    }
    if (key.equals("B2_12")) {

      valueQuery = " AND o.concept_id = ${21151} AND o.value_coded = ${21148} ";
    }

    if (key.equals("B2E")) {
      valueQuery = " AND o.concept_id = ${21151} AND o.value_coded <> ${21148} ";
    }

    query +=
        "FROM   patient p  "
            + "              INNER JOIN encounter e  "
            + "                        ON e.patient_id = p.patient_id  "
            + "                    JOIN obs o  "
            + "                        ON o.encounter_id = e.encounter_id  "
            + "                   WHERE  e.encounter_type = ${6}  "
            + "                          AND p.voided = 0 AND e.voided = 0 "
            + "                          AND e.location_id = :location AND o.location_id = :location "
            + valueQuery;
    if (key.equals("B1E") || key.equals("B2E")) {
      query +=
          "                          AND e.encounter_datetime BETWEEN  DATE_SUB(:revisionEndDate, INTERVAL 14 MONTH) "
              + "                         AND :revisionEndDate  ";
    } else {
      query +=
          "                          AND e.encounter_datetime BETWEEN  :startDate AND :endDate  ";
    }

    query += queryTermination;

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getPatientsFromFichaClinicaDenominatorB1EOrB2E(boolean b1e) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients From Ficha Clinica Denominator B1E Or B2E");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("21151", hivMetadata.getTherapeuticLineConcept().getConceptId());
    map.put("21150", hivMetadata.getFirstLineConcept().getConceptId());
    map.put("21148", hivMetadata.getSecondLineConcept().getConceptId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    StringBuilder query = new StringBuilder();
    query.append(" SELECT p.patient_id ");
    query.append(" FROM patient p ");
    query.append("    INNER JOIN encounter e ");
    query.append("        ON p.patient_id = e.patient_id ");
    query.append("    INNER JOIN obs o ");
    query.append("               ON o.encounter_id = e.encounter_id ");
    query.append("    INNER JOIN  (");
    query.append("                    SELECT p.patient_id, MAX(e.encounter_datetime) max_date ");
    query.append("                    FROM patient p ");
    query.append("                        INNER JOIN encounter e ");
    query.append("                            ON p.patient_id = e.patient_id ");
    query.append("                    WHERE ");
    query.append("                        p.voided = 0 ");
    query.append("                        AND e.voided = 0 ");
    query.append("                        AND e.encounter_type = ${6} ");
    query.append("                        AND e.location_id = :location ");
    query.append("                        AND e.encounter_datetime <= :revisionEndDate ");
    query.append("                     GROUP BY p.patient_id");
    query.append(")  AS last_ficha ON last_ficha.patient_id = p.patient_id ");
    query.append(" WHERE");
    query.append("    p.voided = 0 ");
    query.append("    AND e.voided = 0 ");
    query.append("    AND o.voided = 0 ");
    query.append("    AND e.encounter_type = ${6} ");
    query.append("    AND e.encounter_datetime = last_ficha.max_date ");
    query.append("    AND o.concept_id = ${21151} ");
    if (b1e) {
      query.append("  AND o.value_coded <>  ${21150} ");

    } else {
      query.append("  AND o.value_coded <>  ${21148} ");
    }
    query.append("    AND e.location_id = :location ");
    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>MQC11B2</b>: Melhoria de Qualidade Category 13 Deniminator B2 <br>
   *
   * <ul>
   *   <li>B2- Select all patients from Ficha Clinica (encounter type 6) with “Carga Viral” (Concept
   *       id 856) registered with numeric value > 1000 during the Inclusion period
   *       (startDateInclusion and endDateInclusion). Note: if there is more than one record with
   *       value_numeric > 1000 than consider the first occurrence during the inclusion period.
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getB2_13() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "All patients from Ficha Clinica with “Carga Viral” registered with numeric value > 1000 during the Inclusion period");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String query =
        " SELECT"
            + "             p.patient_id"
            + "             FROM"
            + "             patient p"
            + "                INNER JOIN"
            + "             (SELECT"
            + "                p.patient_id, MIN(e.encounter_datetime) AS first_encounter"
            + "             FROM"
            + "                patient p"
            + "             INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "             JOIN obs o ON o.encounter_id = e.encounter_id"
            + "             WHERE p.voided = 0"
            + "                    AND e.voided = 0"
            + "                    AND o.voided = 0"
            + "                    AND e.location_id = :location"
            + "                    AND o.location_id = :location"
            + "                    AND o.concept_id = ${856}"
            + "                    AND o.value_numeric >= 1000"
            + "                    AND (( e.encounter_type = ${6}"
            + "                    AND e.encounter_datetime BETWEEN :startDate AND :endDate) "
            + "                    OR (e.encounter_type = ${53}"
            + "                    AND o.obs_datetime BETWEEN :startDate AND :endDate))"
            + "             GROUP BY p.patient_id) filtered ON p.patient_id = filtered.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>MQC11B2</b>: Melhoria de Qualidade Category 11 Deniminator B1 <br>
   * <i> A and not B</i> <br>
   *
   * <ul>
   *   <li>B1 – Select all patients from Ficha Clinica (encounter type 6) who have THE LAST “LINHA
   *       TERAPEUTICA”(Concept id 21151) during the Inclusion period (startDateInclusion and
   *       endDateInclusion) and the value coded is “PRIMEIRA LINHA”(Concept id 21150)
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsFromFichaClinicaWithLastTherapeuticLineSetAsFirstLine_B1() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "Patients From Ficha Clinica With Last Therapeutic Line Set As First Line");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("21151", hivMetadata.getTherapeuticLineConcept().getConceptId());
    map.put("21150", hivMetadata.getFirstLineConcept().getConceptId());

    String query =
        " SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "       INNER JOIN (SELECT p.patient_id, "
            + "                          Max(e.encounter_datetime) AS encounter_datetime "
            + "                   FROM   patient p "
            + "                          INNER JOIN encounter e "
            + "                                  ON e.patient_id = p.patient_id "
            + "                          JOIN obs o "
            + "                            ON o.encounter_id = e.encounter_id "
            + "                   WHERE  e.encounter_type = ${6} "
            + "                          AND p.voided = 0 "
            + "                          AND e.voided = 0 "
            + "                          AND e.location_id = :location "
            + "                          AND o.voided = 0 "
            + "                          AND o.concept_id = ${21151} "
            + "                          AND e.encounter_datetime BETWEEN "
            + "                              :startDate AND :endDate "
            + "                   GROUP  BY p.patient_id) filtered "
            + "               ON p.patient_id = filtered.patient_id "
            + "WHERE  e.encounter_datetime = filtered.encounter_datetime "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${21151} "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND o.value_coded = ${21150} "
            + "       AND e.encounter_type = ${6};  ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getPatientsFromFichaClinicaWithLastTherapeuticLineSetAsSecondLine_B2() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "Patients From Ficha Clinica With Last Therapeutic Line Set As Second Line B2");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("21151", hivMetadata.getTherapeuticLineConcept().getConceptId());
    map.put("21148", hivMetadata.getSecondLineConcept().getConceptId());

    String query =
        " SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "       INNER JOIN (SELECT p.patient_id, "
            + "                          Max(e.encounter_datetime) AS encounter_datetime "
            + "                   FROM   patient p "
            + "                          INNER JOIN encounter e "
            + "                                  ON e.patient_id = p.patient_id "
            + "                          JOIN obs o "
            + "                            ON o.encounter_id = e.encounter_id "
            + "                   WHERE  e.encounter_type = ${6} "
            + "                          AND p.voided = 0 "
            + "                          AND e.voided = 0 "
            + "                          AND e.location_id = :location "
            + "                          AND o.voided = 0 "
            + "                          AND o.concept_id = ${21151} "
            + "                          AND e.encounter_datetime BETWEEN "
            + "                              :startDate AND :endDate "
            + "                   GROUP  BY p.patient_id) filtered "
            + "               ON p.patient_id = filtered.patient_id "
            + "WHERE  e.encounter_datetime = filtered.encounter_datetime "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${21151} "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND o.value_coded = ${21148} "
            + "       AND e.encounter_type = ${6};  ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>MQC11B3</b>: Melhoria de Qualidade Category 11 Deniminator B3 <br>
   * <i> A and not B</i> <br>
   *
   * <ul>
   *   <li>B3 – Filter all patients with clinical consultation (encounter type 6) with concept
   *       “GESTANTE” (Concept Id 1982) and value coded “SIM” (Concept Id 1065) with the
   *       Encounter_datetime = ART Start Date (from query A) during the Inclusion period
   *       (startDateInclusion and endDateInclusion)
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWithClinicalConsultationB3() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients With Clinical Consultation");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1982", commonMetadata.getPregnantConcept().getConceptId());

    String query =
        " SELECT art_tbl.patient_id  "
            + "FROM   (SELECT patient_id,  "
            + "               art_date  "
            + "        FROM   (SELECT p.patient_id, Min(value_datetime) art_date "
            + "                    FROM patient p "
            + "              INNER JOIN encounter e "
            + "                  ON p.patient_id = e.patient_id "
            + "              INNER JOIN obs o "
            + "                  ON e.encounter_id = o.encounter_id "
            + "          WHERE  p.voided = 0 "
            + "              AND e.voided = 0 "
            + "              AND o.voided = 0 "
            + "              AND e.encounter_type = ${53} "
            + "              AND o.concept_id = ${1190} "
            + "              AND o.value_datetime IS NOT NULL "
            + "              AND o.value_datetime <= :endDate "
            + "              AND e.location_id = :location "
            + "          GROUP  BY p.patient_id  )  "
            + "               union_tbl  "
            + "        WHERE  union_tbl.art_date BETWEEN :startDate AND :endDate) art_tbl  "
            + "       INNER JOIN (SELECT p.patient_id,  "
            + "                          e.encounter_datetime  "
            + "                   FROM   patient p  "
            + "                          INNER JOIN encounter e  "
            + "                                  ON e.patient_id = p.patient_id  "
            + "                          JOIN obs o  "
            + "                            ON o.encounter_id = e.encounter_id  "
            + "                   WHERE  e.encounter_type = ${6}  "
            + "                          AND o.concept_id = ${1982}  "
            + "                          AND p.voided = 0  "
            + "                          AND e.voided = 0  "
            + "                          AND e.location_id = :location AND o.location_id = :location   "
            + "                          AND o.value_coded = ${1065}  "
            + "                          AND e.encounter_datetime BETWEEN :startDate AND :endDate) gest_tbl "
            + "               ON art_tbl.patient_id = gest_tbl.patient_id  "
            + "WHERE  gest_tbl.encounter_datetime = art_tbl.art_date;  ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  private <T extends AbstractPatientCalculation>
      CohortDefinition getApssConsultationAfterARTstartDateOrAfterApssConsultation(
          int lowerBoundary, int upperBoundary, Class<T> clazz) {

    CalculationCohortDefinition cd =
        new CalculationCohortDefinition(Context.getRegisteredComponents(clazz).get(0));
    cd.setName("APSS consultation after ART start date");
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "After Date", Date.class));

    cd.addCalculationParameter("considerTransferredIn", false);
    cd.addCalculationParameter("considerPharmacyEncounter", true);
    cd.addCalculationParameter("lowerBoundary", lowerBoundary);
    cd.addCalculationParameter("upperBoundary", upperBoundary);

    return cd;
  }

  /**
   * G: Select all patients who have 3 APSS&PP(encounter type 35) consultation in 99 days after
   * Starting ART(The oldest date from A) as following the conditions:
   *
   * <ul>
   *   <li>G1 - FIRST consultation (Encounter_datetime (from encounter type 35)) > “ART Start Date”
   *       (oldest date from A)+20days and <= “ART Start Date” (oldest date from A)+33days AND
   *   <li>G2 - At least one consultation (Encounter_datetime (from encounter type 35)) registered
   *       during the period between “1st Consultation Date(from G1)+20days” and “1st Consultation
   *       Date(from G1)+33days” AND
   *   <li>G3 - At least one consultation (Encounter_datetime (from encounter type 35)) registered
   *       during the period between “2nd Consultation Date(from G2, the oldest)+20days” and “2nd
   *       Consultation Date(from G2, the oldest)+33days” AND
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQC11NG() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName("Category 11 Numerator session G");
    compositionCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition firstApss =
        getApssConsultationAfterARTstartDateOrAfterApssConsultation(
            20, 33, EncounterAfterOldestARTStartDateCalculation.class);

    CohortDefinition secondApss =
        getApssConsultationAfterARTstartDateOrAfterApssConsultation(
            20, 33, SecondFollowingEncounterAfterOldestARTStartDateCalculation.class);

    CohortDefinition thirdApss =
        getApssConsultationAfterARTstartDateOrAfterApssConsultation(
            20, 33, ThirdFollowingEncounterAfterOldestARTStartDateCalculation.class);

    compositionCohortDefinition.addSearch(
        "firstApss",
        EptsReportUtils.map(
            firstApss,
            "onOrAfter=${startDate},onOrBefore=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "secondApss",
        EptsReportUtils.map(
            secondApss,
            "onOrAfter=${startDate},onOrBefore=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "thirdApss",
        EptsReportUtils.map(
            thirdApss,
            "onOrAfter=${startDate},onOrBefore=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.setCompositionString("firstApss AND secondApss AND thirdApss");

    return compositionCohortDefinition;
  }

  public CohortDefinition getMQC11NH1() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    sqlCohortDefinition.setName("Category 11 - Numerator - H1");

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String query =
        " SELECT p.patient_id "
            + " FROM patient p "
            + "    INNER JOIN encounter e "
            + "        ON p.patient_id = e.patient_id "
            + "    INNER JOIN ( "
            + "                    SELECT p.patient_id, MIN(e.encounter_datetime) as  encounter_date "
            + "                    FROM patient p   "
            + "                        INNER JOIN encounter e "
            + "                            ON p.patient_id = e.patient_id "
            + "                        INNER JOIN obs o "
            + "                            ON o.encounter_id = e.encounter_id "
            + "                    WHERE p.voided = 0  "
            + "                        AND e.voided = 0 "
            + "                        AND o.voided = 0 "
            + "                        AND e.encounter_type = ${6} "
            + "                        AND e.encounter_datetime "
            + "                            BETWEEN :startDate AND :endDate "
            + "                        AND e.location_id = :location "
            + "                        AND o.concept_id = ${856} AND o.value_numeric >=  1000 "
            + "                    GROUP BY p.patient_id "
            + "                ) viral_load ON viral_load.patient_id = p.patient_id "
            + " WHERE p.voided = 0  "
            + "    AND e.voided = 0 "
            + "    AND e.encounter_type = ${35} "
            + "    AND e.encounter_datetime = viral_load.encounter_date "
            + "    AND e.location_id = :location ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getMQC11NH2() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    sqlCohortDefinition.setName("Category 11 - Numerator - H2");

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String query =
        " SELECT p.patient_id "
            + "FROM patient p "
            + "    INNER JOIN encounter e "
            + "        ON p.patient_id = e.patient_id "
            + "    INNER JOIN ( "
            + "                 SELECT p.patient_id, e.encounter_datetime"
            + "                 FROM patient p "
            + "                    INNER JOIN encounter e "
            + "                        ON p.patient_id = e.patient_id "
            + "                    INNER JOIN ( "
            + "                                    SELECT p.patient_id, MIN(e.encounter_datetime) as  encounter_date "
            + "                                    FROM patient p   "
            + "                                        INNER JOIN encounter e "
            + "                                            ON p.patient_id = e.patient_id "
            + "                                        INNER JOIN obs o "
            + "                                            ON o.encounter_id = e.encounter_id "
            + "                                    WHERE p.voided = 0  "
            + "                                        AND e.voided = 0 "
            + "                                        AND o.voided = 0 "
            + "                                        AND e.encounter_type = ${6} "
            + "                                        AND e.encounter_datetime "
            + "                                            BETWEEN :startDate AND :endDate "
            + "                                        AND e.location_id = :location "
            + "                                        AND o.concept_id = ${856} AND o.value_numeric >  1000 "
            + "                                    GROUP BY p.patient_id "
            + "                                ) viral_load ON viral_load.patient_id = p.patient_id "
            + "                 WHERE p.voided = 0  "
            + "                     AND e.voided = 0 "
            + "                    AND e.encounter_type = ${35} "
            + "                    AND e.encounter_datetime = viral_load.encounter_date "
            + "                    AND e.location_id = :location "
            + "                ) h1 ON h1.patient_id = p.patient_id "
            + " WHERE p.voided = 0  "
            + "    AND e.voided = 0 "
            + "    AND e.encounter_type = ${35} "
            + "    AND e.encounter_datetime > DATE_ADD(h1.encounter_datetime, INTERVAL 20 DAY)  "
            + "         AND e.encounter_datetime <= DATE_ADD(h1.encounter_datetime, INTERVAL 33 DAY) "
            + "    AND e.location_id = :location ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getMQC11NH3() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    sqlCohortDefinition.setName("Category 11 - Numerator - H3");

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM patient p "
            + "    INNER JOIN encounter e "
            + "        ON p.patient_id = e.patient_id "
            + "    INNER JOIN ( "
            + "                     SELECT p.patient_id, e.encounter_datetime "
            + "                    FROM patient p "
            + "                        INNER JOIN encounter e "
            + "                            ON p.patient_id = e.patient_id "
            + "                        INNER JOIN ( "
            + "                                     SELECT p.patient_id, e.encounter_datetime"
            + "                                     FROM patient p "
            + "                                        INNER JOIN encounter e "
            + "                                            ON p.patient_id = e.patient_id "
            + "                                        INNER JOIN ( "
            + "                                                        SELECT p.patient_id, MIN(e.encounter_datetime) as  encounter_date "
            + "                                                        FROM patient p   "
            + "                                                            INNER JOIN encounter e "
            + "                                                                ON p.patient_id = e.patient_id "
            + "                                                            INNER JOIN obs o "
            + "                                                                ON o.encounter_id = e.encounter_id "
            + "                                                        WHERE p.voided = 0  "
            + "                                                            AND e.voided = 0 "
            + "                                                            AND o.voided = 0 "
            + "                                                            AND e.encounter_type = ${6} "
            + "                                                            AND e.encounter_datetime "
            + "                                                                BETWEEN :startDate AND :endDate "
            + "                                                            AND e.location_id = :location "
            + "                                                            AND o.concept_id = ${856} AND o.value_numeric >  1000 "
            + "                                                        GROUP BY p.patient_id "
            + "                                                    ) viral_load ON viral_load.patient_id = p.patient_id "
            + "                                     WHERE p.voided = 0  "
            + "                                         AND e.voided = 0 "
            + "                                        AND e.encounter_type = ${35} "
            + "                                        AND e.encounter_datetime = viral_load.encounter_date "
            + "                                        AND e.location_id = :location "
            + "                                    ) h1 ON h1.patient_id = p.patient_id "
            + "                     WHERE p.voided = 0  "
            + "                        AND e.voided = 0 "
            + "                        AND e.encounter_type = ${35} "
            + "                        AND e.encounter_datetime > DATE_ADD(h1.encounter_datetime, INTERVAL 20 DAY)  "
            + "                             AND e.encounter_datetime <= DATE_ADD(h1.encounter_datetime, INTERVAL 33 DAY) "
            + "                        AND e.location_id = :location "
            + "                ) h2 ON h2.patient_id = p.patient_id "
            + " WHERE p.voided = 0  "
            + "    AND e.voided = 0 "
            + "    AND e.encounter_type = ${35} "
            + "    AND e.encounter_datetime > DATE_ADD(h2.encounter_datetime, INTERVAL 20 DAY)  "
            + "         AND e.encounter_datetime <= DATE_ADD(h2.encounter_datetime, INTERVAL 33 DAY) "
            + "    AND e.location_id = :location ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * H: Select all patients who have 3 APSS&PP (encounter type 35) consultation in 66 days after
   * Viral Load result (the oldest date from B2) following the conditions:
   *
   * <ul>
   *   <li>H1 - One Consultation (Encounter_datetime (from encounter type 35)) on the same date when
   *       the Viral Load with >1000 result was recorded (oldest date from B2) AND
   *   <li>H2- Another consultation (Encounter_datetime (from encounter type 35)) > “1st
   *       consultation” (oldest date from H1)+20 days and <=“1st consultation” (oldest date from
   *       H1)+33days AND
   *   <li>H3- Another consultation (Encounter_datetime (from encounter type 35)) > “2nd
   *       consultation” (oldest date from H2)+20 days and <=“2nd consultation” (oldest date from
   *       H2)+33days AND
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQC11NH() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.addParameter(new Parameter("startDate", "Start date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End date", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    compositionCohortDefinition.setName("Category 11 Numerator session G");

    CohortDefinition h1 = getMQC11NH1();
    CohortDefinition h2 = getMQC11NH2();
    CohortDefinition h3 = getMQC11NH3();

    String mapping = "startDate=${startDate},endDate=${endDate},location=${location}";

    compositionCohortDefinition.addSearch("h1", EptsReportUtils.map(h1, mapping));
    compositionCohortDefinition.addSearch("h2", EptsReportUtils.map(h2, mapping));
    compositionCohortDefinition.addSearch("h3", EptsReportUtils.map(h3, mapping));

    compositionCohortDefinition.setCompositionString("h1 AND h2 AND h3");

    return compositionCohortDefinition;
  }

  /**
   * I: Select all patients who have monthly APSS&PP(encounter type 35) consultation (until the end
   * of the revision period) after Starting ART(The oldest date from A) as following pseudo-code:
   *
   * <p>Start pseudo-code:
   *
   * <ul>
   *   <li>For ( i=0; i<(days between “ART Start Date” and endDateRevision; i++)
   *       <ul>
   *         <li>Existence of consultation (Encounter_datetime (from encounter type 35)) > [“ART
   *             Start Date” (oldest date from A)+i] and <= “ART Start Date” (oldest date from
   *             A)+i+33days
   *         <li>i= i+33days
   *       </ul>
   *   <li>End pseudo-code.
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQC11NI() {

    CalculationCohortDefinition cd =
        new CalculationCohortDefinition(
            Context.getRegisteredComponents(ConsultationUntilEndDateAfterStartingART.class).get(0));
    cd.setName(
        "Categoru 11 - numerator - Session I - Interval of 33 Daus for APSS consultations after ART start date");
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "After Date", Date.class));

    cd.addCalculationParameter("considerTransferredIn", false);
    cd.addCalculationParameter("considerPharmacyEncounter", true);

    return cd;
  }

  /**
   * 11.1. % de adultos em TARV com o mínimo de 3 consultas de seguimento de adesão na FM-ficha de
   * APSS/PP nos primeiros 3 meses após início do TARV (Line 56 in the template) Numerador (Column D
   * in the Template) as following: <code>
   * A and NOT C and NOT D and NOT E and NOT F  AND G and Age > 14*</code>
   */
  public CohortDefinition getMQC11NumAnotCnotDnotEnotFandGAdultss(String reportResource) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName("Category 11 : Numeraror 11.1 ");

    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition a = getMOHArtStartDate();

    CohortDefinition c =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition d =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition e =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    CohortDefinition f = commonCohortQueries.getTranferredOutPatients();
    CohortDefinition g = getMQC11NG();

    if (reportResource.equals("MQ")) {
      compositionCohortDefinition.addSearch("A", EptsReportUtils.map(a, MAPPING));
      compositionCohortDefinition.addSearch("C", EptsReportUtils.map(c, MAPPING));
      compositionCohortDefinition.addSearch("D", EptsReportUtils.map(d, MAPPING));
      compositionCohortDefinition.addSearch("E", EptsReportUtils.map(e, MAPPING));
      compositionCohortDefinition.addSearch("F", EptsReportUtils.map(f, MAPPING1));
      compositionCohortDefinition.addSearch("G", EptsReportUtils.map(g, MAPPING1));
    } else if (reportResource.equals("MI")) {
      compositionCohortDefinition.addSearch("A", EptsReportUtils.map(a, MAPPING4));
      compositionCohortDefinition.addSearch("C", EptsReportUtils.map(c, MAPPING6));
      compositionCohortDefinition.addSearch("D", EptsReportUtils.map(d, MAPPING6));
      compositionCohortDefinition.addSearch("E", EptsReportUtils.map(e, MAPPING6));
      compositionCohortDefinition.addSearch("F", EptsReportUtils.map(f, MAPPING5));
      compositionCohortDefinition.addSearch("G", EptsReportUtils.map(g, MAPPING10));
    }

    compositionCohortDefinition.setCompositionString(
        "A AND NOT C AND NOT D AND NOT E AND NOT F  AND G");

    return compositionCohortDefinition;
  }

  /**
   * 11.2. % de pacientes na 1a linha de TARV com CV acima de 1000 cópias que tiveram 3 consultas de
   * APSS/PP mensais consecutivas para reforço de adesão (Line 57 in the template) Numerador (Column
   * D in the Template) as following: <code>
   * B1 and B2 and NOT C and NOT B5 and NOT E and NOT F AND H and  Age > 14*</code>
   */
  public CohortDefinition getMQC11NumB1nB2notCnotDnotEnotEnotFnHandAdultss(String reportSource) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName("Category 11 : Numeraror 11.2 ");

    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition b1 = getPatientsFromFichaClinicaWithLastTherapeuticLineSetAsFirstLine_B1();

    CohortDefinition b2 = getB2_13();

    CohortDefinition b4 =
        QualityImprovement2020Queries.getMQ13DenB4_P4(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getHivViralLoadConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId(),
            commonMetadata.getPregnantConcept().getConceptId());

    CohortDefinition b5 =
        QualityImprovement2020Queries.getMQ13DenB5_P4(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getHivViralLoadConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId(),
            commonMetadata.getBreastfeeding().getConceptId());

    CohortDefinition e =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());
    CohortDefinition f = commonCohortQueries.getTranferredOutPatients();
    CohortDefinition h = getMQC11NH();

    if (reportSource.equals("MQ")) {
      compositionCohortDefinition.addSearch("B1", EptsReportUtils.map(b1, MAPPING1));
      compositionCohortDefinition.addSearch("B2", EptsReportUtils.map(b2, MAPPING1));
      compositionCohortDefinition.addSearch("B4", EptsReportUtils.map(b4, MAPPING));
      compositionCohortDefinition.addSearch("B5", EptsReportUtils.map(b5, MAPPING));
      compositionCohortDefinition.addSearch("E", EptsReportUtils.map(e, MAPPING));
      compositionCohortDefinition.addSearch("F", EptsReportUtils.map(f, MAPPING1));
      compositionCohortDefinition.addSearch("H", EptsReportUtils.map(h, MAPPING));
    } else if (reportSource.equals("MI")) {
      compositionCohortDefinition.addSearch("B1", EptsReportUtils.map(b1, MAPPING8));
      compositionCohortDefinition.addSearch("B2", EptsReportUtils.map(b2, MAPPING8));
      compositionCohortDefinition.addSearch("B4", EptsReportUtils.map(b4, MAPPING8));
      compositionCohortDefinition.addSearch("B5", EptsReportUtils.map(b5, MAPPING8));
      compositionCohortDefinition.addSearch("E", EptsReportUtils.map(e, MAPPING7));
      compositionCohortDefinition.addSearch("F", EptsReportUtils.map(f, MAPPING9));
      compositionCohortDefinition.addSearch("H", EptsReportUtils.map(h, MAPPING8));
    }

    compositionCohortDefinition.setCompositionString(
        "B1 AND B2 AND NOT B5 AND NOT E AND NOT F AND NOT B4 AND H");

    return compositionCohortDefinition;
  }

  /**
   * 11.3.% de MG em TARV com o mínimo de 3 consultas de seguimento de adesão na FM-ficha de APSS/PP
   * nos primeiros 3 meses após início do TARV (Line 58 in the template) Numerador (Column D in the
   * Template) as following: <code> A  and  C and NOT D and NOT E and NOT F  AND G </code>
   */
  public CohortDefinition getMQC11NumAnB3nCnotDnotEnotEnotFnG(String reportSource) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName("Category 11 : Numeraror 11.3");

    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition a = getMOHArtStartDate();

    CohortDefinition c =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition d =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition e =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());
    CohortDefinition f = commonCohortQueries.getTranferredOutPatients();

    CohortDefinition g = getMQC11NG();

    if (reportSource.equals("MQ")) {
      compositionCohortDefinition.addSearch("A", EptsReportUtils.map(a, MAPPING));
      compositionCohortDefinition.addSearch("C", EptsReportUtils.map(c, MAPPING));
      compositionCohortDefinition.addSearch("D", EptsReportUtils.map(d, MAPPING));
      compositionCohortDefinition.addSearch("E", EptsReportUtils.map(e, MAPPING));
      compositionCohortDefinition.addSearch("F", EptsReportUtils.map(f, MAPPING1));
      compositionCohortDefinition.addSearch("G", EptsReportUtils.map(g, MAPPING1));
    } else if (reportSource.equals("MI")) {
      compositionCohortDefinition.addSearch("A", EptsReportUtils.map(a, MAPPING4));
      compositionCohortDefinition.addSearch("C", EptsReportUtils.map(c, MAPPING6));
      compositionCohortDefinition.addSearch("D", EptsReportUtils.map(d, MAPPING6));
      compositionCohortDefinition.addSearch("E", EptsReportUtils.map(e, MAPPING6));
      compositionCohortDefinition.addSearch("F", EptsReportUtils.map(f, MAPPING5));
      compositionCohortDefinition.addSearch("G", EptsReportUtils.map(g, MAPPING10));
    }

    compositionCohortDefinition.setCompositionString("A AND C AND NOT D AND NOT E AND NOT F AND G");

    return compositionCohortDefinition;
  }

  /**
   * 11.4. % de MG na 1a linha de TARV com CV acima de 1000 cópias que tiveram 3 consultas de
   * APSS/PP mensais consecutivas para reforço de adesão (Line 59 in the template) Numerador (Column
   * D in the Template) as following: <code>
   *  B1 and B2 and B5 and NOT E and NOT F AND H </code>
   */
  public CohortDefinition getMQC11NumB1nB2nB3nCnotDnotEnotEnotFnH(String reportSource) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName("Category 11 : Numeraror  11.4");

    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition b1 = getPatientsFromFichaClinicaWithLastTherapeuticLineSetAsFirstLine_B1();

    CohortDefinition b4 =
        QualityImprovement2020Queries.getMQ13DenB4_P4(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getHivViralLoadConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId(),
            commonMetadata.getPregnantConcept().getConceptId());

    CohortDefinition b5 =
        QualityImprovement2020Queries.getMQ13DenB5_P4(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getHivViralLoadConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId(),
            commonMetadata.getBreastfeeding().getConceptId());

    CohortDefinition c =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition e =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    CohortDefinition f = commonCohortQueries.getTranferredOutPatients();
    CohortDefinition h = getMQC11NH();

    if (reportSource.equals("MQ")) {
      compositionCohortDefinition.addSearch("B1", EptsReportUtils.map(b1, MAPPING1));
      compositionCohortDefinition.addSearch("B4", EptsReportUtils.map(b4, MAPPING1));
      compositionCohortDefinition.addSearch("B5", EptsReportUtils.map(b5, MAPPING));
      compositionCohortDefinition.addSearch("C", EptsReportUtils.map(c, MAPPING));
      compositionCohortDefinition.addSearch("E", EptsReportUtils.map(e, MAPPING));
      compositionCohortDefinition.addSearch("F", EptsReportUtils.map(f, MAPPING1));
      compositionCohortDefinition.addSearch("H", EptsReportUtils.map(h, MAPPING));
    } else if (reportSource.equals("MI")) {
      compositionCohortDefinition.addSearch("B1", EptsReportUtils.map(b1, MAPPING8));
      compositionCohortDefinition.addSearch("B4", EptsReportUtils.map(b4, MAPPING8));
      compositionCohortDefinition.addSearch("B5", EptsReportUtils.map(b5, MAPPING8));
      compositionCohortDefinition.addSearch("C", EptsReportUtils.map(c, MAPPING));
      compositionCohortDefinition.addSearch("E", EptsReportUtils.map(e, MAPPING7));
      compositionCohortDefinition.addSearch("F", EptsReportUtils.map(f, MAPPING9));
      compositionCohortDefinition.addSearch("H", EptsReportUtils.map(h, MAPPING8));

      /*
      compositionCohortDefinition.addSearch(
            "B1", EptsReportUtils.map(patientsFromFichaClinicaLinhaTerapeutica, MAPPING8));

        compositionCohortDefinition.addSearch(
            "B2", EptsReportUtils.map(patientsFromFichaClinicaCargaViral, MAPPING8));

        compositionCohortDefinition.addSearch(
            "B4", EptsReportUtils.map(pregnantWithCargaViralHigherThan1000, MAPPING8));

        compositionCohortDefinition.addSearch(
            "B5", EptsReportUtils.map(breastfeedingWithCargaViralHigherThan1000, MAPPING8));

        compositionCohortDefinition.addSearch("C", EptsReportUtils.map(pregnant, MAPPING));
        compositionCohortDefinition.addSearch("D", EptsReportUtils.map(breastfeeding, MAPPING));
        compositionCohortDefinition.addSearch("E", EptsReportUtils.map(transferredIn, MAPPING7));
        compositionCohortDefinition.addSearch("F", EptsReportUtils.map(transfOut, MAPPING9));
        */
    }

    compositionCohortDefinition.setCompositionString(
        "B1 AND NOT B5 AND B4 AND NOT E AND NOT F AND H");

    return compositionCohortDefinition;
  }

  /**
   * 11.5. % de crianças >2 anos de idade em TARV com registo mensal de seguimento da adesão na
   * ficha de APSS/PP nos primeiros 99 dias de TARV (Line 60 in the template) Numerador (Column D in
   * the Template) as following: <code>
   * A and NOT C and NOT D and NOT E and NOT F  AND G and Age BETWEEN 2 AND 14*</code>
   */
  public CohortDefinition getMQC11NumAnotCnotDnotEnotFnotGnChildren(String reportSource) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName("Category 11 : Numeraror 11.5");

    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition a = getMOHArtStartDate();
    CohortDefinition c =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());
    CohortDefinition d =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());
    CohortDefinition e =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());
    CohortDefinition f = commonCohortQueries.getTranferredOutPatients();
    CohortDefinition g = getMQC11NG();
    CohortDefinition children = genericCohortQueries.getAgeOnMOHArtStartDate(2, 14, true);

    if (reportSource.equals("MQ")) {
      compositionCohortDefinition.addSearch("A", EptsReportUtils.map(a, MAPPING));
      compositionCohortDefinition.addSearch("C", EptsReportUtils.map(c, MAPPING));
      compositionCohortDefinition.addSearch("D", EptsReportUtils.map(d, MAPPING));
      compositionCohortDefinition.addSearch("E", EptsReportUtils.map(e, MAPPING));
      compositionCohortDefinition.addSearch("F", EptsReportUtils.map(f, MAPPING1));
      compositionCohortDefinition.addSearch("G", EptsReportUtils.map(g, MAPPING1));
    } else if (reportSource.equals("MI")) {
      compositionCohortDefinition.addSearch("A", EptsReportUtils.map(a, MAPPING4));
      compositionCohortDefinition.addSearch("C", EptsReportUtils.map(c, MAPPING6));
      compositionCohortDefinition.addSearch("D", EptsReportUtils.map(d, MAPPING6));
      compositionCohortDefinition.addSearch("E", EptsReportUtils.map(e, MAPPING6));
      compositionCohortDefinition.addSearch("F", EptsReportUtils.map(f, MAPPING5));
      compositionCohortDefinition.addSearch("G", EptsReportUtils.map(g, MAPPING10));
    }

    compositionCohortDefinition.setCompositionString(
        "A AND NOT C AND NOT D AND NOT E AND NOT F AND G");

    return compositionCohortDefinition;
  }
  /**
   * 11.6. % de crianças <2 anos de idade em TARV com registo mensal de seguimento da adesão na
   * ficha de APSS/PP no primeiro ano de TARV (Line 61 in the template) Numerador (Column D in the
   * Template) as following: <code>
   *  A and NOT C and NOT D and NOT E and NOT F AND I  AND Age  <= 9 MONTHS</code>
   */
  public CohortDefinition getMQC11NumAnotCnotDnotEnotFnotIlessThan9Month(String reportSource) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName("Category 11 : Numeraror 11.6");

    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition a = getMOHArtStartDate();
    CohortDefinition c =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition d =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());
    CohortDefinition e =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());
    CohortDefinition f = commonCohortQueries.getTranferredOutPatients();
    CohortDefinition i = getMQC11NI();
    CohortDefinition babies = genericCohortQueries.getAgeInMonths(0, 9);

    if (reportSource.equals("MQ")) {
      compositionCohortDefinition.addSearch("A", EptsReportUtils.map(a, MAPPING));
      compositionCohortDefinition.addSearch("C", EptsReportUtils.map(c, MAPPING));
      compositionCohortDefinition.addSearch("D", EptsReportUtils.map(d, MAPPING));
      compositionCohortDefinition.addSearch("E", EptsReportUtils.map(e, MAPPING));
      compositionCohortDefinition.addSearch("F", EptsReportUtils.map(f, MAPPING1));
      compositionCohortDefinition.addSearch(
          "I",
          EptsReportUtils.map(
              i, "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));

    } else if (reportSource.equals("MI")) {
      compositionCohortDefinition.addSearch("A", EptsReportUtils.map(a, MAPPING4));
      compositionCohortDefinition.addSearch("C", EptsReportUtils.map(c, MAPPING6));
      compositionCohortDefinition.addSearch("D", EptsReportUtils.map(d, MAPPING6));
      compositionCohortDefinition.addSearch("E", EptsReportUtils.map(e, MAPPING6));
      compositionCohortDefinition.addSearch("F", EptsReportUtils.map(f, MAPPING5));
      compositionCohortDefinition.addSearch(
          "I",
          EptsReportUtils.map(
              i,
              "onOrAfter=${revisionEndDate-5m+1d},onOrBefore=${revisionEndDate-4m},location=${location}"));
    }

    compositionCohortDefinition.addSearch(
        "BABIES", EptsReportUtils.map(babies, "effectiveDate=${effectiveDate}"));

    compositionCohortDefinition.setCompositionString(
        "A and NOT C and NOT D and NOT E and NOT F AND I AND BABIES");

    return compositionCohortDefinition;
  }

  /**
   * 11.7. % de crianças (0-14 anos) na 1a linha de TARV com CV acima de 1000 cópias que tiveram 3
   * consultas mensais consecutivas de APSS/PP para reforço de adesão(Line 62 in the template)
   * Numerador (Column D in the Template) as following: <code>
   *  B1 and B2 and  NOT C and NOT B5 and NOT E and NOT F  And H and  Age < 15**</code>
   */
  public CohortDefinition getMQC11NumB1nB2notCnotDnotEnotFnHChildren(String reportSource) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName("Category 11 : Numeraror 11.7");

    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition b1 = getPatientsFromFichaClinicaWithLastTherapeuticLineSetAsFirstLine_B1();

    CohortDefinition b2 = getB2_13();

    CohortDefinition b4 =
        QualityImprovement2020Queries.getMQ13DenB4_P4(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getHivViralLoadConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId(),
            commonMetadata.getPregnantConcept().getConceptId());

    CohortDefinition b5 =
        QualityImprovement2020Queries.getMQ13DenB5_P4(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getHivViralLoadConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId(),
            commonMetadata.getBreastfeeding().getConceptId());

    CohortDefinition e =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    CohortDefinition f = commonCohortQueries.getTranferredOutPatients();
    CohortDefinition h = getMQC11NH();

    if (reportSource.equals("MQ")) {
      compositionCohortDefinition.addSearch("B1", EptsReportUtils.map(b1, MAPPING1));
      compositionCohortDefinition.addSearch("B2", EptsReportUtils.map(b2, MAPPING1));
      compositionCohortDefinition.addSearch("B4", EptsReportUtils.map(b4, MAPPING));
      compositionCohortDefinition.addSearch("B5", EptsReportUtils.map(b5, MAPPING));
      compositionCohortDefinition.addSearch("E", EptsReportUtils.map(e, MAPPING));
      compositionCohortDefinition.addSearch("F", EptsReportUtils.map(f, MAPPING1));
      compositionCohortDefinition.addSearch("H", EptsReportUtils.map(h, MAPPING));
    } else if (reportSource.equals("MI")) {
      compositionCohortDefinition.addSearch("B1", EptsReportUtils.map(b1, MAPPING8));
      compositionCohortDefinition.addSearch("B2", EptsReportUtils.map(b2, MAPPING8));
      compositionCohortDefinition.addSearch("B4", EptsReportUtils.map(b4, MAPPING8));
      compositionCohortDefinition.addSearch("B5", EptsReportUtils.map(b5, MAPPING8));
      compositionCohortDefinition.addSearch("E", EptsReportUtils.map(e, MAPPING7));
      compositionCohortDefinition.addSearch("F", EptsReportUtils.map(f, MAPPING9));
      compositionCohortDefinition.addSearch("H", EptsReportUtils.map(h, MAPPING8));
    }

    compositionCohortDefinition.setCompositionString(
        "B1 AND B2 AND NOT B5 AND NOT E AND NOT F AND NOT B4 AND H");

    return compositionCohortDefinition;
  }

  /**
   * <b>MQ12DenP1: Melhoria de Qualidade Category 12 Denominator - Part 1</b><br>
   * <br>
   * <i> DENOMINATOR 1: A AND NOT (C OR D OR E OR F) AND AGE > 15 </i> <br>
   * <br>
   * <i> DENOMINATOR 2: A AND NOT (C OR D OR E OR F) AND AGE > 15 </i> <br>
   * <br>
   * <i> DENOMINATOR 6: A AND NOT (C OR D OR E OR F) AND AGE <= 15 </i> <br>
   * <br>
   * <i> DENOMINATOR 7: A AND NOT (C OR D OR E OR F) AND AGE <= 15 </i> <br>
   * <br>
   * <i> DENOMINATOR 10: (A AND C) AND NOT (D OR E OR F) </i> <br>
   * <br>
   * <i> DENOMINATOR 11: (A AND C) AND NOT (D OR E OR F) </i> <br>
   * <br>
   *
   * <ul>
   *   <li>A - Select all patients who initiated ART during the Inclusion period (startDateInclusion
   *       and endDateInclusion)
   *   <li>C - All female patients registered as “Pregnant” on MasterCard during the inclusion
   *       period (startDateInclusion and endDateInclusion)
   *   <li>D - All female patients registered as “Breastfeeding” on MasterCard during the inclusion
   *       period (startDateInclusion and endDateInclusion)
   *   <li>E - All transferred IN patients within the revision period
   *   <li>F - All transferred OUT patients within the revision period
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQ12DEN(Integer den) {
    CompositionCohortDefinition comp = new CompositionCohortDefinition();

    switch (den) {
      case 1:
        comp.setName(
            "Adultos (15/+anos) que iniciaram o TARV no período de inclusão e que retornaram para uma consulta clínica ou levantamento de ARVs dentro de 33 dias após o início do TARV");
        break;
      case 2:
        comp.setName(
            "Adultos (15/+anos) que iniciaram o TARV no período de inclusão e que tiveram consultas clínicas ou levantamentos de ARVs dentro de 99 dias após o início do TARV");
        break;
      case 5:
        comp.setName(
            "Crianças (0-14 anos) que iniciaram o TARV no período de inclusão e que retornaram para uma consulta clínica ou levantamento de ARVs dentro de 33 dias após o início do TARV");
        break;
      case 6:
        comp.setName(
            "Crianças (0-14 anos) que iniciaram o TARV no período de inclusão e que tiveram consultas clínicas ou levantamentos de ARVs dentro de 99 dias após o início do TARV");
        break;
      case 9:
        comp.setName(
            "Mulheres grávidas HIV+ que iniciaram o TARV no período de inclusão e que retornaram para uma consulta clínica ou levantamento de ARVs dentro de 33 dias após o início do TARV");
        break;
      case 10:
        comp.setName(
            "Mulheres grávidas HIV+ que iniciaram o TARV no período de inclusão e que tiveram consultas clínicas ou levantamentos de ARVs dentro de 99 dias após o início do TARV");
        break;
    }

    comp.addParameter(new Parameter("startDate", "startDate", Date.class));
    comp.addParameter(new Parameter("endDate", "endDate", Date.class));
    comp.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    comp.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition startedART = getMOHArtStartDate();

    CohortDefinition pregnant =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition breastfeeding =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition transferredIn =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    CohortDefinition transferOut = commonCohortQueries.getTranferredOutPatients();

    comp.addSearch("A", EptsReportUtils.map(startedART, MAPPING));

    comp.addSearch("C", EptsReportUtils.map(pregnant, MAPPING));

    comp.addSearch("D", EptsReportUtils.map(breastfeeding, MAPPING));

    comp.addSearch(
        "E",
        EptsReportUtils.map(
            transferredIn,
            "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    comp.addSearch("F", EptsReportUtils.map(transferOut, MAPPING1));

    comp.addSearch(
        "ADULT",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnMOHArtStartDate(15, null, false),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));

    if (den == 1 || den == 2) {
      comp.setCompositionString("A AND NOT (C OR D OR E OR F) AND ADULT");
    }
    if (den == 5 || den == 6) {
      comp.setCompositionString("A AND NOT (C OR D OR E OR F)");
    } else if (den == 9 || den == 10) {
      comp.setCompositionString("(A AND C) AND NOT (D OR E OR F)");
    }
    return comp;
  }

  /**
   * <b>MQC13Part3B1</b>: Melhoria de Qualidade Category 13 Deniminator B1 <br>
   * <i> BI1 and not B1E</i> <br>
   *
   * <ul>
   *   <li>BI1 - Select all patients who have the most recent “ALTERNATIVA A LINHA - 1a LINHA”
   *       (Concept Id 23898, obs_datetime) recorded in Ficha Resumo (encounter type 53) with any
   *       value coded (not null) during the inclusion period (startDateInclusion and
   *       endDateInclusion) AND
   *   <li>B1E - Exclude all patients from Ficha Clinica (encounter type 6, encounter_datetime) who
   *       have “LINHA TERAPEUTICA”(Concept id 21151) with value coded DIFFERENT THAN “PRIMEIRA
   *       LINHA”(Concept id 21150) and encounter_datetime > the most recent “ALTERNATIVA A LINHA -
   *       1a LINHA” (from B1) and <= endDateInclusion
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsOnRegimeChangeBI1AndNotB1E_B1() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients With Clinical Consultation");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "Data Final de Avaliacao", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();

    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("21190", commonMetadata.getRegimenAlternativeToFirstLineConcept().getConceptId());
    map.put("21151", hivMetadata.getTherapeuticLineConcept().getConceptId());
    map.put("21150", hivMetadata.getFirstLineConcept().getConceptId());
    map.put("1792", hivMetadata.getJustificativeToChangeArvTreatment().getConceptId());
    map.put("1982", commonMetadata.getPregnantConcept().getConceptId());

    String query =
        "SELECT patient_id "
            + "FROM   (SELECT p.patient_id, "
            + "               Max(o.obs_datetime) AS regime_date "
            + "        FROM   patient p "
            + "               JOIN encounter e "
            + "                 ON e.patient_id = p.patient_id "
            + "               JOIN obs o "
            + "                 ON o.encounter_id = e.encounter_id "
            + "               JOIN obs o2 "
            + "                 ON o2.encounter_id = e.encounter_id "
            + "        WHERE  e.encounter_type = ${53} "
            + "               AND o.concept_id = ${21190} "
            + "               AND o.value_coded IS NOT NULL "
            + "               AND e.location_id = :location "
            + "               AND ( "
            + "                      (o2.concept_id = ${1792} AND o2.value_coded <> ${1982})"
            + "                       OR "
            + "                      (o2.concept_id = ${1792} AND o2.value_coded IS NULL) "
            + "                       OR "
            + "                      ( "
            + "                       NOT EXISTS ( "
            + "                               SELECT * FROM obs oo "
            + "                               WHERE oo.voided = 0 "
            + "                               AND oo.encounter_id = e.encounter_id "
            + "                               AND oo.concept_id = 1792 "
            + "                           ) "
            + "                     ) "
            + "                    ) "
            + "               AND e.voided = 0 "
            + "               AND p.voided = 0 "
            + "               AND o.voided = 0 "
            + "               AND o2.voided = 0"
            + "               AND o.obs_datetime BETWEEN :startDate AND :endDate "
            + "        GROUP  BY p.patient_id) bI1 "
            + "WHERE  bI1.patient_id NOT IN (SELECT p.patient_id "
            + "                              FROM   patient p "
            + "                                     JOIN encounter e "
            + "                                       ON e.patient_id = p.patient_id "
            + "                                     JOIN obs o "
            + "                                       ON o.encounter_id = e.encounter_id "
            + "                              WHERE  e.encounter_type = ${6} "
            + "                                     AND o.concept_id = ${21151} AND o.value_coded <> ${21150} "
            + "                                     AND e.location_id = :location "
            + "                                     AND e.voided = 0 "
            + "                                     AND p.voided = 0 "
            + "                                     AND e.encounter_datetime > bI1.regime_date "
            + "                                     AND e.encounter_datetime <= :revisionEndDate)";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>MQC13Part3B2</b>: B2NEW P1_2 <br>
   *
   * <ul>
   *   <li>B2NEW P1_2- Select all patients who have the REGIME ARV SEGUNDA LINHA (Concept Id 21187,
   *       value coded different NULL) recorded in Ficha Resumo (encounter type 53) and obs_datetime
   *       >= inclusionStartDate and <= inclusionEndDate AND at least for 6 months ( “Last Clinical
   *       Consultation” (last encounter_datetime from B1) minus obs_datetime(from B2) >= 6 months)
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsOnRegimeArvSecondLineB2NEWP1_2() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients With Clinical Consultation");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();

    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("21187", hivMetadata.getRegArvSecondLine().getConceptId());

    String query =
        "SELECT p.patient_id "
            + " FROM   patient p"
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "       INNER JOIN(SELECT p.patient_id, "
            + "                         Max(e.encounter_datetime) last_visit "
            + "                  FROM   patient p "
            + "                         INNER JOIN encounter e "
            + "                                 ON e.patient_id = p.patient_id "
            + "                  WHERE  p.voided = 0 "
            + "                         AND e.voided = 0 "
            + "                         AND e.encounter_type = ${6} "
            + "                         AND e.location_id = :location "
            + "                         AND e.encounter_datetime BETWEEN "
            + "                             :startDate AND :revisionEndDate "
            + "                  GROUP  BY p.patient_id) AS last_clinical "
            + "               ON last_clinical.patient_id = p.patient_id "
            + " WHERE  e.voided = 0 "
            + "       AND p.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${53} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${21187} "
            + "       AND o.value_coded IS NOT NULL "
            + "       AND o.obs_datetime >= :startDate "
            + "       AND o.obs_datetime <= :endDate "
            + "       AND TIMESTAMPDIFF(MONTH, o.obs_datetime,  last_clinical.last_visit) >= 6 ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>MQC13Part3B2</b>: Melhoria de Qualidade Category 13 Deniminator B2 <br>
   * <i> BI2 and not B2E</i> <br>
   *
   * <ul>
   *   <li>BI2- Select all patients who have the LAST “LINHA TERAPEUTICA” (Concept Id 21151)
   *       recorded in Ficha Clinica (encounter type 6, encounter_datetime) with value coded
   *       “SEGUNDA LINHA” (concept id 21148) during the inclusion period (startDateInclusion and
   *       endDateInclusion) AND
   *   <li>B1E - Exclude all patients with clinical consultation (encounter type 6) with concept
   *       “PEDIDO DE INVESTIGACOES LABORATORIAIS” (Concept Id 23722) and value coded “HIV CARGA
   *       VIRAL” (Concept Id 856) on Encounter_datetime startDateInclusion-3months and
   *       startDateInclusion.
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsOnRegimeArvSecondLine() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients With Clinical Consultation");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();

    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1982", commonMetadata.getPregnantConcept().getConceptId());
    map.put("21187", hivMetadata.getRegArvSecondLine().getConceptId());
    map.put("1792", hivMetadata.getJustificativeToChangeArvTreatment().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "  FROM patient p "
            + "       INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "       INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "  WHERE e.voided = 0 AND p.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND o2.voided = 0 "
            + "    AND e.encounter_type = ${53} "
            + "    AND e.location_id = :location "
            + "    AND (o.concept_id = ${21187} AND o.value_coded IS NOT NULL) "
            + "               AND ( "
            + "                      (o2.concept_id = ${1792} AND o2.value_coded <> ${1982})"
            + "                       OR "
            + "                      (o2.concept_id = ${1792} AND o2.value_coded IS NULL) "
            + "                       OR "
            + "                      ( "
            + "                       NOT EXISTS ( "
            + "                               SELECT * FROM obs oo "
            + "                               WHERE oo.voided = 0 "
            + "                               AND oo.encounter_id = e.encounter_id "
            + "                               AND oo.concept_id = 1792 "
            + "                           ) "
            + "                     ) "
            + "                    ) "
            + "    AND o.obs_datetime >= :startDate "
            + "    AND o.obs_datetime <= :endDate "
            + "  GROUP BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>MQ12</b>: Melhoria de Qualidade Categoria 12 Numerador - P2 <br>
   *
   * <p>Select patients from the corresponding denominator and all active patients as done in resumo
   * mensal report - B13 and apply the following categories <i>12.3 - (A and B1 and NOT (B1E or C or
   * D or E)) AND NOT G and Age >= 15*</i><br>
   * <i>12.4 - (A and B2 and NOT (B2E or C or D or E)) AND NOT G and Age > =15*</i><br>
   * <i>12.8 - (A and B1 and NOT (B1E or C or D or E)) AND NOT G and Age < 15*</i><br>
   * <i>12.9 - (A and B2) and NOT (B2E or C or D or E) AND NOT G and Age < 15*</i><br>
   * <i>12.12 - (A and B1 and C) and NOT (B1E or D or E) AND NOT G </i><br>
   *
   * <p>All age disaggreagtions should be based on the ART start date
   *
   * @param flag
   * @return
   */
  public CohortDefinition getMQ12NumeratorP2(int flag) {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    switch (flag) {
      case 3:
        cd.setName(
            "Adultos (15/+anos) na 1ª linha que iniciaram o TARV há 12 meses atrás sem registo de saídas");
        break;
      case 4:
        cd.setName("Adultos (15/+anos) que iniciaram 2ª linha TARV há 12 meses atrás");
        break;
      case 7:
        cd.setName("Crianças (0-14 anos) na 1ª linha que iniciaram o TARV há 12 meses atrás");
        break;
      case 8:
        cd.setName("Crianças (0-14 anos) que iniciaram 2ª linha TARV há 12 meses atrás");
        break;
      case 11:
        cd.setName("Mulheres grávidas HIV+ 1ª linha que iniciaram o TARV há 12 meses atrás");
        break;
    }
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    // Start adding the definitions based on the requirements
    CohortDefinition startedART = getMOHArtStartDate();

    CohortDefinition b1 = getPatientsFromFichaClinicaWithLastTherapeuticLineSetAsFirstLine_B1();

    CohortDefinition b1E = getPatientsFromFichaClinicaDenominatorB1EOrB2E(true);

    CohortDefinition b2 = getPatientsFromFichaClinicaWithLastTherapeuticLineSetAsSecondLine_B2();

    CohortDefinition b2E = getPatientsFromFichaClinicaDenominatorB1EOrB2E(false);

    CohortDefinition pregnant =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition breastfeeding =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition transferOut = commonCohortQueries.getTranferredOutPatients();

    cd.addSearch("A", EptsReportUtils.map(startedART, MAPPING2));

    cd.addSearch(
        "B1",
        EptsReportUtils.map(
            b1,
            "startDate=${revisionEndDate-14m},endDate=${revisionEndDate-11m},location=${location},revisionEndDate=${revisionEndDate}"));

    cd.addSearch(
        "B1E", EptsReportUtils.map(b1E, "location=${location},revisionEndDate=${revisionEndDate}"));

    cd.addSearch(
        "B2",
        EptsReportUtils.map(
            b2,
            "startDate=${revisionEndDate-14m},endDate=${revisionEndDate-11m},location=${location},revisionEndDate=${revisionEndDate}"));

    cd.addSearch(
        "B2E", EptsReportUtils.map(b2E, "location=${location},revisionEndDate=${revisionEndDate}"));

    cd.addSearch("C", EptsReportUtils.map(pregnant, MAPPING));

    cd.addSearch("D", EptsReportUtils.map(breastfeeding, MAPPING));

    cd.addSearch("F", EptsReportUtils.map(transferOut, MAPPING1));

    cd.addSearch(
        "G",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getActivePatientsInARTByEndOfCurrentMonth(true),
            "startDate=${revisionEndDate-14m},endDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "ADULT",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnMOHArtStartDate(15, null, false),
            "onOrAfter=${revisionEndDate-14m},onOrBefore=${revisionEndDate-11m},location=${location}"));
    cd.addSearch(
        "CHILDREN",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnMOHArtStartDate(0, 14, true),
            "onOrAfter=${revisionEndDate-14m},onOrBefore=${revisionEndDate-11m},location=${location}"));
    if (flag == 3) {
      cd.setCompositionString("(A AND B1 AND NOT (B1E OR C OR D OR F)) AND G AND ADULT");
    } else if (flag == 4) {
      cd.setCompositionString("(A AND B2 AND NOT (B2E OR C OR D OR F)) AND G AND ADULT");
    } else if (flag == 7) {
      cd.setCompositionString("(A AND B1 AND NOT (B1E OR C OR D OR F)) AND G AND CHILDREN");
    } else if (flag == 8) {
      cd.setCompositionString("(A AND B2) AND NOT (B2E OR C OR D OR F) AND G AND CHILDREN");
    } else if (flag == 11) {
      cd.setCompositionString("(A AND B1 AND C) AND NOT (B1E OR D OR F) AND G");
    }

    return cd;
  }

  /**
   * <b>MQ12NumP1: Melhoria de Qualidade Category 12 Numerator - Part 1</b><br>
   * <br>
   * <i> NUMERATOR 1: (A and NOT (C or D or E or F)) and H and Age >= 15 </i> <br>
   * <br>
   * <i> NUMERATOR 2: (A and NOT (C or D or E or F)) and I and Age >= 15 </i> <br>
   * <br>
   * <i> NUMERATOR 6: (A and NOT (C or D or E or F)) and H Age < 15 </i> <br>
   * <br>
   * <i> NUMERATOR 7: (A and NOT (C or D or E or F)) and I and Age < 15 </i> <br>
   * <br>
   * <i> NUMERATOR 10: ((A and C) and NOT (D or E or F)) and H Age < 15 </i> <br>
   * <br>
   * <i> NUMERATOR 11: ((A and C) and NOT (D or E or F)) and I </i> <br>
   * <br>
   *
   * <ul>
   *   <li>H - Filter all patients that returned for another clinical consultation (encounter type
   *       6, encounter_datetime) or ARV pickup (encounter type 52, concept ID 23866 value_datetime,
   *       Levantou ARV (concept id 23865) = Sim (1065)) between 25 and 33 days after ART start
   *       date(Oldest date From A)
   *   <li>I1 - FIRST consultation (Encounter_datetime (from encounter type 6)) >= “ART Start Date”
   *       (oldest date from A)+20days and <= “ART Start Date” (oldest date from A)+33days
   *   <li>AND
   *   <li>I2 - At least one consultation (Encounter_datetime (from encounter type 6)) >= “First
   *       Consultation” (oldest date from I1)+20days and <=“First Consultation” (oldest date from
   *       I1)+33days
   *   <li>I3 - At least one consultation (Encounter_datetime (from encounter type 6)) > “Second
   *       Consultation” (oldest date from I2)+20days and <= “Second Consultation” (oldest date from
   *       I2)+33days
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQ12NUM(Integer den) {
    CompositionCohortDefinition comp = new CompositionCohortDefinition();

    switch (den) {
      case 1:
        comp.setName(
            "Adultos (15/+anos) que iniciaram o TARV no período de inclusão e que retornaram para uma consulta clínica ou levantamento de ARVs entre 25 a 33 dias após o início do TARV");
        break;
      case 2:
        comp.setName(
            "Adultos (15/+anos) que iniciaram o TARV no período de inclusão e que tiveram 3 consultas clínicas ou levantamentos de ARVs dentro de 99 dias após o início do TARV");
        break;
      case 5:
        comp.setName(
            "Crianças (0-14 anos) que iniciaram o TARV no período de inclusão e que retornaram para uma consulta clínica ou levantamento de ARVs dentro de 33 dias após o início do TARV");
        break;
      case 6:
        comp.setName(
            "Crianças (0-14 anos) que iniciaram o TARV no período de inclusão e que tiveram consultas clínicas ou levantamentos de ARVs dentro de 99 dias após o início do TARV");
        break;
      case 9:
        comp.setName(
            "Mulheres grávidas HIV+  que iniciaram o TARV no período de inclusão e que retornaram para uma consulta clínica ou levantamento de ARVs dentro de 33 dias após o início do TARV");
        break;
      case 10:
        comp.setName(
            "Mulheres grávidas HIV+  que iniciaram o TARV no período de inclusão e que tiveram consultas clínicas ou levantamentos de ARVs dentro de 99 dias após o início do TARV");
        break;
    }

    comp.addParameter(new Parameter("startDate", "startDate", Date.class));
    comp.addParameter(new Parameter("endDate", "endDate", Date.class));
    comp.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    comp.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition startedART = getMOHArtStartDate();

    CohortDefinition pregnant =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition breastfeeding =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition transferredIn =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    CohortDefinition transferOut = commonCohortQueries.getTranferredOutPatients();

    CohortDefinition returnedForAnyConsultationOrPickup =
        QualityImprovement2020Queries.getMQ12NumH(
            20,
            33,
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getYesConcept().getConceptId(),
            hivMetadata.getHistoricalDrugStartDateConcept().getConceptId(),
            hivMetadata.getArtPickupConcept().getConceptId(),
            hivMetadata.getArtDatePickupMasterCard().getConceptId());

    CohortDefinition returnedForAnotherConsultationOrPickup =
        QualityImprovement2020Queries.getMQ12NumI(
            20,
            33,
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getYesConcept().getConceptId(),
            hivMetadata.getHistoricalDrugStartDateConcept().getConceptId(),
            hivMetadata.getArtPickupConcept().getConceptId(),
            hivMetadata.getArtDatePickupMasterCard().getConceptId());

    CohortDefinition returnedForAnotherConsultationOrPickup3466 =
        QualityImprovement2020Queries.getMQ12NumI(
            34,
            66,
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getYesConcept().getConceptId(),
            hivMetadata.getHistoricalDrugStartDateConcept().getConceptId(),
            hivMetadata.getArtPickupConcept().getConceptId(),
            hivMetadata.getArtDatePickupMasterCard().getConceptId());

    CohortDefinition returnedForAnotherConsultationOrPickup6799 =
        QualityImprovement2020Queries.getMQ12NumI(
            67,
            99,
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getYesConcept().getConceptId(),
            hivMetadata.getHistoricalDrugStartDateConcept().getConceptId(),
            hivMetadata.getArtPickupConcept().getConceptId(),
            hivMetadata.getArtDatePickupMasterCard().getConceptId());

    comp.addSearch("A", EptsReportUtils.map(startedART, MAPPING));

    comp.addSearch("C", EptsReportUtils.map(pregnant, MAPPING));

    comp.addSearch("D", EptsReportUtils.map(breastfeeding, MAPPING));

    comp.addSearch(
        "E",
        EptsReportUtils.map(
            transferredIn,
            "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    comp.addSearch("F", EptsReportUtils.map(transferOut, MAPPING1));

    comp.addSearch(
        "H",
        EptsReportUtils.map(
            returnedForAnyConsultationOrPickup,
            "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    comp.addSearch(
        "I",
        EptsReportUtils.map(
            returnedForAnotherConsultationOrPickup,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));
    comp.addSearch(
        "II",
        EptsReportUtils.map(
            returnedForAnotherConsultationOrPickup3466,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    comp.addSearch(
        "III",
        EptsReportUtils.map(
            returnedForAnotherConsultationOrPickup6799,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    comp.addSearch(
        "CHILDREN",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnMOHArtStartDate(0, 14, true),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));

    comp.addSearch(
        "ADULT",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnMOHArtStartDate(15, null, false),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));

    if (den == 1) {
      comp.setCompositionString("(A AND NOT (C OR D OR E OR F)) AND H AND ADULT");
    } else if (den == 2) {
      comp.setCompositionString("(A AND NOT (C OR D OR E OR F)) AND I AND II AND III AND ADULT");
    } else if (den == 5) {
      comp.setCompositionString("(A AND NOT (C OR D OR E OR F)) AND H");
    } else if (den == 6) {
      comp.setCompositionString("(A AND NOT (C OR D OR E OR F)) AND I AND II AND III");
    } else if (den == 9) {
      comp.setCompositionString("((A AND C) AND NOT (D OR E OR F)) AND H ");
    } else if (den == 10) {
      comp.setCompositionString("((A AND C) AND NOT (D OR E OR F)) AND I AND II AND III ");
    }
    return comp;
  }

  /**
   * <b>Description:</b> MQ Categoria 13 C query
   *
   * <p><b>Technical Specs</b>
   *
   * <blockquote>
   *
   * C - Select all patients with Last Clinical Consultation (encounter type 6, encounter_datetime)
   * occurred during the period (encounter_datetime >= endDateInclusion and <= endDateRevision) and
   * the concept: “PEDIDO DE INVESTIGACOES LABORATORIAIS” (Concept Id 23722) and value coded “HIV
   * CARGA VIRAL” (Concept Id 856) In this last consultation.
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getMQ13G() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Last Ficha Clinica");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());

    String query =
        " SELECT p.patient_id  "
            + " FROM  "
            + "     patient p  "
            + "         INNER JOIN  "
            + "     encounter e ON e.patient_id = p.patient_id  "
            + "         INNER JOIN  "
            + "     obs o ON o.encounter_id = e.encounter_id  "
            + "         INNER JOIN  "
            + "     (SELECT   "
            + "         p.patient_id, MAX(e.encounter_datetime) last_visit  "
            + "     FROM  "
            + "         patient p  "
            + "     INNER JOIN encounter e ON e.patient_id = p.patient_id  "
            + "     WHERE  "
            + "         p.voided = 0 AND e.voided = 0  "
            + "             AND e.encounter_type = ${6}  "
            + "             AND e.location_id = :location  "
            + "             AND e.encounter_datetime BETWEEN :startDate AND :endDate  "
            + "     GROUP BY p.patient_id) b1 ON b1.patient_id = e.patient_id"
            + "     WHERE p.voided = 0 AND e.voided = 0  AND o.voided = 0"
            + "    AND e.encounter_type = ${6}  "
            + "    AND e.location_id = :location  "
            + "    AND e.encounter_datetime BETWEEN :startDate AND :endDate  "
            + "    AND e.encounter_datetime = b1.last_visit"
            + "        AND o.concept_id = ${23722}"
            + "        AND o.value_coded = ${856}";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>MQ13</b>: Melhoria de Qualidade Category 13 <br>
   * <i></i><br>
   * <i> <b>DENOMINATOR (1,6,7,8):</b> B1 AND ((B2 AND NOT B2E) OR (B3 AND NOT B3E)) AND NOT (B4E OR
   * B5E)</i> <br>
   * <i></i><br>
   * <i> <b>NUMERATOR (1,6,7,8):</b> B1 AND ((B2 AND NOT B2E) OR (B3 AND NOT B3E)) AND NOT (B4E OR
   * B5E) AND C </i> <br>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQ13(Boolean den, Integer line) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    if (den) {
      compositionCohortDefinition.setName("B AND NOT C AND NOT D");
    } else {
      compositionCohortDefinition.setName("(B AND G) AND NOT (C OR D)");
    }
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition lastClinical = commonCohortQueries.getMOHPatientsLastClinicalConsultation();

    CohortDefinition pregnant =
        commonCohortQueries.getNewMQPregnantORBreastfeeding(
            hivMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition brestfeeding =
        commonCohortQueries.getNewMQPregnantORBreastfeeding(
            hivMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition b2New =
        commonCohortQueries.getPatientsWithFirstTherapeuticLineOnLastClinicalEncounterB2NEW();

    CohortDefinition secondLine6Months = getPatientsOnRegimeArvSecondLineB2NEWP1_2();

    CohortDefinition changeRegimen6Months =
        commonCohortQueries.getMOHPatientsOnTreatmentFor6Months(
            true,
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getRegimenAlternativeToFirstLineConcept(),
            Arrays.asList(
                commonMetadata.getAlternativeFirstLineConcept(),
                commonMetadata.getRegimeChangeConcept(),
                hivMetadata.getNoConcept()));

    CohortDefinition B3E =
        commonCohortQueries.getMOHPatientsToExcludeFromTreatmentIn6Months(
            true,
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getRegimenAlternativeToFirstLineConcept(),
            Arrays.asList(
                commonMetadata.getAlternativeFirstLineConcept(),
                commonMetadata.getRegimeChangeConcept(),
                hivMetadata.getNoConcept()),
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getTherapeuticLineConcept(),
            Collections.singletonList(hivMetadata.getFirstLineConcept()));

    CohortDefinition B4E =
        commonCohortQueries.getMOHPatientsWithVLRequestorResultBetweenClinicalConsultations(
            true, false, 12);

    CohortDefinition B5E =
        commonCohortQueries.getMOHPatientsWithVLRequestorResultBetweenClinicalConsultations(
            false, true, -3);

    CohortDefinition G = getMQ13G();

    if (line == 1) {
      compositionCohortDefinition.addSearch(
          "age",
          EptsReportUtils.map(
              commonCohortQueries.getMOHPatientsAgeOnLastClinicalConsultationDate(15, null),
              "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));
    } else if (line == 4) {
      compositionCohortDefinition.addSearch(
          "age",
          EptsReportUtils.map(
              commonCohortQueries.getMOHPatientsAgeOnLastClinicalConsultationDate(15, null),
              "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));
    } else if (line == 6) {
      compositionCohortDefinition.addSearch(
          "age",
          EptsReportUtils.map(
              commonCohortQueries.getMOHPatientsAgeOnLastClinicalConsultationDate(0, 4),
              "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));
    } else if (line == 7) {
      compositionCohortDefinition.addSearch(
          "age",
          EptsReportUtils.map(
              commonCohortQueries.getMOHPatientsAgeOnLastClinicalConsultationDate(5, 9),
              "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));
    } else if (line == 8) {
      compositionCohortDefinition.addSearch(
          "age",
          EptsReportUtils.map(
              commonCohortQueries.getMOHPatientsAgeOnLastClinicalConsultationDate(10, 14),
              "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));
    } else if (line == 13) {
      compositionCohortDefinition.addSearch(
          "age",
          EptsReportUtils.map(
              commonCohortQueries.getMOHPatientsAgeOnLastClinicalConsultationDate(2, 14),
              "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));
    }

    compositionCohortDefinition.addSearch(
        "B1",
        EptsReportUtils.map(
            lastClinical,
            "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "B2NEW",
        EptsReportUtils.map(
            b2New,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "C",
        EptsReportUtils.map(
            pregnant, "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "D",
        EptsReportUtils.map(
            brestfeeding,
            "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "secondLineB2",
        EptsReportUtils.map(
            secondLine6Months,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "B3",
        EptsReportUtils.map(
            changeRegimen6Months,
            "startDate=${endDate},endDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "B3E",
        EptsReportUtils.map(
            B3E, "startDate=${endDate},endDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "B4E",
        EptsReportUtils.map(
            B4E, "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "B5E",
        EptsReportUtils.map(
            B5E, "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "G",
        EptsReportUtils.map(
            G, "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    if (den) {
      if (line == 1) {
        compositionCohortDefinition.setCompositionString(
            "(B1 AND (B2NEW OR (B3 AND NOT B3E)) AND NOT B4E AND NOT B5E) AND NOT (C OR D) AND age");
      } else if (line == 6 || line == 7 || line == 8) {
        compositionCohortDefinition.setCompositionString(
            "(B1 AND (B2NEW OR (B3 AND NOT B3E)) AND NOT B4E AND NOT B5E) AND NOT (C OR D) AND age");
      } else if (line == 4 || line == 13) {
        compositionCohortDefinition.setCompositionString(
            "(B1 AND secondLineB2 AND NOT B4E AND NOT B5E) AND NOT (C OR D) AND age");
      }
    } else {
      if (line == 1 || line == 6 || line == 7 || line == 8) {
        compositionCohortDefinition.setCompositionString(
            "(B1 AND (B2NEW OR (B3 AND NOT B3E)) AND NOT B4E AND NOT B5E) AND NOT (C OR D) AND G AND age");
      } else if (line == 4 || line == 13) {
        compositionCohortDefinition.setCompositionString(
            "(B1 AND secondLineB2 AND NOT B4E AND NOT B5E) AND NOT (C OR D) AND G AND age");
      }
    }

    return compositionCohortDefinition;
  }

  /**
   * <b>MQC13P3NUM</b>: Melhoria de Qualidade Categoria 13 Numerador - Part 3 <br>
   *
   * <p>Select patients from the corresponding denominator and apply the following categories
   * <i>13.2 - ( (A and NOT E and G) OR (B1 and H)) and NOT (C or D or F) and Age >= 15*</i><br>
   * <i>13.9 - ( (A and NOT E and G) OR (B1 and H))and NOT (C or D or F) and Age >= 0 and <=4
   * years*</i><br>
   * <i>13.10 - ( (A and NOT E and G) OR (B1 and H)) and NOT (C or D or F) and Age >= 5 and <=9
   * years*</i><br>
   * <i>13.11 - ( (A and NOT E and G) OR (B1 and H)) and NOT (C or D or F) and Age >= 10 and Age <=
   * 14 years*</i><br>
   * <i>13.5 - (B2 and I) and NOT (C or D or F) and Age>=15years* </i><br>
   * <i>13.14 - (B2 and I) and NOT (C or D or F) and Age > 2 and Age <15years * </i><br>
   *
   * <p>All age disaggreagtions should be based on the ART start date
   *
   * @param indicator
   * @return
   */
  public CohortDefinition getMQC13P3NUM(int indicator) {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    if (indicator == 2)
      cd.setName(
          "Adultos (15/+anos) na 1a linha de TARV que receberam o resultado da CV entre o sexto e o nono mês após início do TARV");
    if (indicator == 9)
      cd.setName(
          "Crianças  (0-4 anos de idade) na 1a linha de TARV que receberam o resultado da Carga Viral entre o sexto e o nono mês após o início do TARV");
    if (indicator == 10)
      cd.setName(
          "Crianças  (5-9 anos de idade) na 1a linha de TARV que receberam o resultado da Carga Viral entre o sexto e o nono mês após o início do TARV");
    if (indicator == 11)
      cd.setName(
          "Crianças  (10-14 anos de idade) na 1a linha de TARV que receberam o resultado da Carga Viral entre o sexto e o nono mês após o início do TARV");
    if (indicator == 5)
      cd.setName(
          "Adultos (15/+anos) na 2a linha de TARV que receberam o resultado da CV entre o sexto e o nono mês após o início da 2a linha de TARV");
    if (indicator == 14)
      cd.setName(
          "Crianças na 2a linha de TARV que receberam o resultado da Carga Viral entre o sexto e o nono mês após o início da 2a linha de TARV");

    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Data final de Revisao", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    String mapping =
        "startDate=${startDate},endDate=${endDate},less3mDate=${startDate-3m},location=${location}";

    if (indicator == 2) {
      cd.addSearch(
          "age",
          EptsReportUtils.map(
              ageCohortQueries.createXtoYAgeCohort(
                  "Adultos (15/+anos) na 2a linha de TARV que receberam o resultado da CV entre o sexto e o nono mês após o início da 2a linha de TARV",
                  15,
                  null),
              "effectiveDate=${endDate}"));
    } else if (indicator == 5) {
      cd.addSearch(
          "age",
          EptsReportUtils.map(
              getAgeOnObsDatetime(15, null),
              "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));
    } else if (indicator == 9) {
      cd.addSearch(
          "age",
          EptsReportUtils.map(
              ageCohortQueries.createXtoYAgeCohort(
                  "Crianças (0-4 anos de idade) na 1a linha de TARV que receberam o resultado da Carga Viral entre o sexto e o nono mês após o início do TARV",
                  0,
                  4),
              "effectiveDate=${endDate}"));
    } else if (indicator == 10) {
      cd.addSearch(
          "age",
          EptsReportUtils.map(
              ageCohortQueries.createXtoYAgeCohort(
                  "Crianças  (5-9 anos de idade) na 1a linha de TARV que receberam o resultado da Carga Viral entre o sexto e o nono mês após o início do TARV",
                  5,
                  9),
              "effectiveDate=${endDate}"));
    } else if (indicator == 11) {
      cd.addSearch(
          "age",
          EptsReportUtils.map(
              ageCohortQueries.createXtoYAgeCohort(
                  "Crianças  (10-14 anos de idade) na 1a linha de TARV que receberam o resultado da Carga Viral entre o sexto e o nono mês após o início do TARV",
                  10,
                  14),
              "effectiveDate=${endDate}"));
    } else if (indicator == 14) {
      cd.addSearch(
          "age",
          EptsReportUtils.map(
              getAgeOnObsDatetime(2, 14),
              "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));
    }

    // Start adding the definitions based on the requirements
    cd.addSearch("A", EptsReportUtils.map(getMOHArtStartDate(), MAPPING));

    cd.addSearch("B1", EptsReportUtils.map(getPatientsOnRegimeChangeBI1AndNotB1E_B1(), MAPPING1));

    cd.addSearch("B2New", EptsReportUtils.map(getPatientsOnRegimeArvSecondLine(), MAPPING));

    cd.addSearch(
        "C",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPregnantORBreastfeeding(
                commonMetadata.getPregnantConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            MAPPING));
    cd.addSearch(
        "D",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPregnantORBreastfeeding(
                commonMetadata.getBreastfeeding().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            MAPPING));
    cd.addSearch(
        "E",
        EptsReportUtils.map(
            QualityImprovement2020Queries.getTransferredInPatients(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
                hivMetadata.getPatientFoundYesConcept().getConceptId(),
                hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
                hivMetadata.getArtStatus().getConceptId()),
            MAPPING));

    cd.addSearch(
        "F", EptsReportUtils.map(commonCohortQueries.getTranferredOutPatients(), MAPPING1));

    cd.addSearch("G", EptsReportUtils.map(getMQC13P3NUM_G(), MAPPING));
    cd.addSearch("H", EptsReportUtils.map(getMQC13P3NUM_H(), MAPPING));
    cd.addSearch("I", EptsReportUtils.map(getMQC13P3NUM_I(), mapping));
    cd.addSearch("J", EptsReportUtils.map(getMQC13P3NUM_J(), MAPPING));
    cd.addSearch("K", EptsReportUtils.map(getMQC13P3NUM_K(), MAPPING1));
    cd.addSearch("L", EptsReportUtils.map(getMQC13P3NUM_L(), MAPPING));
    cd.addSearch(
        "DD", EptsReportUtils.map(txMlCohortQueries.getDeadPatientsComposition(), MAPPING3));

    if (indicator == 2 || indicator == 9 || indicator == 10 || indicator == 11)
      cd.setCompositionString(
          "((A AND NOT C AND NOT D AND (G OR J) AND NOT DD) OR (B1 AND (H OR K))) AND NOT (F OR E OR DD) AND age");

    if (indicator == 5)
      cd.setCompositionString("(B2New AND (I OR L)) AND NOT (F OR E OR DD) AND age");

    if (indicator == 14)
      cd.setCompositionString("(B2New AND (I OR L)) AND NOT (F OR E OR DD) AND age");

    return cd;
  }

  public CohortDefinition getMQC13P3NUM_G() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    sqlCohortDefinition.setName("Category 13 Part 3- Numerator - G");

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        "SELECT art_tbl.patient_id "
            + "FROM   (SELECT patient_id, "
            + "               Min(data_inicio) data_inicio "
            + "        FROM   (SELECT p.patient_id, "
            + "                       Min(value_datetime) data_inicio "
            + "                FROM   patient p "
            + "                       inner join encounter e "
            + "                               ON p.patient_id = e.patient_id "
            + "                       inner join obs o "
            + "                               ON e.encounter_id = o.encounter_id "
            + "                WHERE  p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND e.encounter_type = ${53} "
            + "                       AND o.concept_id = ${1190} "
            + "                       AND o.value_datetime IS NOT NULL "
            + "                       AND o.value_datetime <= :endDate "
            + "                       AND e.location_id = :location "
            + "                GROUP  BY p.patient_id ) inicio "
            + "        WHERE  data_inicio BETWEEN :startDate AND :endDate "
            + "        GROUP  BY patient_id) art_tbl "
            + "       join (SELECT p.patient_id, "
            + "                    e.encounter_datetime "
            + "             FROM   patient p "
            + "                    join encounter e "
            + "                      ON e.patient_id = p.patient_id "
            + "                    join obs o "
            + "                      ON o.encounter_id = e.encounter_id "
            + "             WHERE  e.encounter_type = ${6} "
            + "                    AND e.voided = 0 "
            + "                    AND e.location_id = :location "
            + "                    AND ( ( o.concept_id = ${856} "
            + "                            AND o.value_numeric IS NOT NULL ) "
            + "                           OR ( o.concept_id = ${1305} "
            + "                                AND o.value_coded IS NOT NULL ) ))G_tbl "
            + "         ON G_tbl.patient_id = art_tbl.patient_id "
            + "       WHERE  G_tbl.encounter_datetime BETWEEN Date_add(art_tbl.data_inicio, "
            + "                                        interval 6 month) "
            + "                                        AND "
            + "       Date_add(art_tbl.data_inicio, interval 9 month)  ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getMQC13P3NUM_H() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    sqlCohortDefinition.setName("Category 13 Part 3- Numerator - H");

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("21190", commonMetadata.getRegimenAlternativeToFirstLineConcept().getConceptId());
    map.put("21151", hivMetadata.getTherapeuticLineConcept().getConceptId());
    map.put("21150", hivMetadata.getFirstLineConcept().getConceptId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        " SELECT B1.patient_id "
            + "FROM   (SELECT patient_id, "
            + "               regime_date "
            + "        FROM   (SELECT p.patient_id, "
            + "                       Max(o.obs_datetime) AS regime_date "
            + "                FROM   patient p "
            + "                       join encounter e "
            + "                         ON e.patient_id = p.patient_id "
            + "                       join obs o "
            + "                         ON o.encounter_id = e.encounter_id "
            + "                WHERE  e.encounter_type = ${53} "
            + "                       AND o.concept_id = ${21190} "
            + "                       AND o.value_coded IS NOT NULL "
            + "                       AND e.location_id = :location "
            + "                       AND e.voided = 0 "
            + "                       AND p.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o.obs_datetime BETWEEN :startDate AND :endDate "
            + "                GROUP  BY p.patient_id) bI1 "
            + "        WHERE  bI1.patient_id NOT IN (SELECT p.patient_id "
            + "                                      FROM   patient p "
            + "                                             join encounter e "
            + "                                               ON e.patient_id = p.patient_id "
            + "                                             join obs o "
            + "                                               ON o.encounter_id = "
            + "                                                  e.encounter_id "
            + "                                      WHERE  e.encounter_type = ${6} "
            + "                                             AND o.concept_id = ${21151} "
            + "                                             AND o.value_coded <> ${21150} "
            + "                                             AND e.location_id = :location "
            + "                                             AND e.voided = 0 "
            + "                                             AND p.voided = 0 "
            + "                                             AND e.encounter_datetime > "
            + "                                                 bI1.regime_date "
            + "                                             AND e.encounter_datetime <= "
            + "                                                 :endDate))B1 "
            + "       join (SELECT p.patient_id, "
            + "                    e.encounter_datetime "
            + "             FROM   patient p "
            + "                    join encounter e "
            + "                      ON e.patient_id = p.patient_id "
            + "                    join obs o "
            + "                      ON o.encounter_id = e.encounter_id "
            + "             WHERE  e.encounter_type = ${6} "
            + "                    AND e.voided = 0 "
            + "                    AND e.location_id = :location "
            + "                    AND ( ( o.concept_id = ${856} "
            + "                            AND o.value_numeric IS NOT NULL ) "
            + "                           OR ( o.concept_id = ${1305} "
            + "                                AND o.value_coded IS NOT NULL ) )) H_tbl "
            + "         ON H_tbl.patient_id = B1.patient_id "
            + "WHERE  H_tbl.encounter_datetime BETWEEN Date_add(B1.regime_date, "
            + "                                        interval 6 month) AND "
            + "                                               Date_add(B1.regime_date, "
            + "                                               interval 9 month);  ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getMQC13P3NUM_I() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("less3mDate", "Less3months date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    sqlCohortDefinition.setName("Category 13 Part 3- Numerator - I");

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1982", commonMetadata.getPregnantConcept().getConceptId());
    map.put("21187", hivMetadata.getRegArvSecondLine().getConceptId());
    map.put("1792", hivMetadata.getJustificativeToChangeArvTreatment().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        " SELECT B2.patient_id "
            + "         FROM   ( SELECT p.patient_id, MAX(o.obs_datetime) as regime_date "
            + "   FROM "
            + "   patient p "
            + "       INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "       INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "   WHERE e.voided = 0 AND p.voided = 0 "
            + "   AND o.voided = 0 "
            + "   AND o2.voided = 0 "
            + "   AND e.encounter_type = ${53} "
            + "   AND e.location_id = :location  "
            + "   AND (o.concept_id = ${21187} AND o.value_coded IS NOT NULL) "
            + "               AND ( "
            + "                      (o2.concept_id = ${1792} AND o2.value_coded <> ${1982})"
            + "                       OR "
            + "                      (o2.concept_id = ${1792} AND o2.value_coded IS NULL) "
            + "                       OR "
            + "                      ( "
            + "                       NOT EXISTS ( "
            + "                               SELECT * FROM obs oo "
            + "                               WHERE oo.voided = 0 "
            + "                               AND oo.encounter_id = e.encounter_id "
            + "                               AND oo.concept_id = 1792 "
            + "                           ) "
            + "                     ) "
            + "                    ) "
            + "   AND o.obs_datetime >= :startDate "
            + "   AND o.obs_datetime <= :endDate "
            + "  GROUP BY p.patient_id "
            + "         ) B2  "
            + "                join ( SELECT p.patient_id, e.encounter_datetime  "
            + "                      FROM   patient p  "
            + "                             join encounter e ON e.patient_id = p.patient_id  "
            + "                             join obs o ON o.encounter_id = e.encounter_id  "
            + "                      WHERE  e.encounter_type = ${6}  "
            + "                             AND e.voided = 0  "
            + "                             AND e.location_id = :location  "
            + "                             AND ( ( o.concept_id = ${856}  "
            + "             AND o.value_numeric IS NOT NULL )  "
            + "             OR ( o.concept_id = ${1305}  "
            + "             AND o.value_coded IS NOT NULL ) )) I_tbl  "
            + "                  ON I_tbl.patient_id = B2.patient_id  "
            + "         WHERE  I_tbl.encounter_datetime BETWEEN Date_add(B2.regime_date, interval 6 month) AND Date_add(B2.regime_date, interval 9 month)";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>MQ13</b>: Melhoria de Qualidade Category 13 Part 4, B2 <br>
   *
   * <ul>
   *   <li>B2 - Select all patients from Ficha Clinica (encounter type 6) with FIRST concept “Carga
   *       Viral” (Concept id 856) with value_numeric > 1000 and Encounter_datetime during the
   *       Inclusion period (startDateInclusion and endDateInclusion). Note: if there is more than
   *       one record with value_numeric > 1000 than consider the first occurrence during the
   *       inclusion period.
   *   <li>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQ13P4B() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    sqlCohortDefinition.setName("Category 13 - Part 4 Denominator - B2");

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String query =
        " SELECT  "
            + "     p.patient_id  "
            + " FROM  "
            + "     patient p  "
            + "         INNER JOIN  "
            + "     encounter e ON e.patient_id = p.patient_id  "
            + "         INNER JOIN  "
            + "     obs o ON o.encounter_id = e.encounter_id  "
            + "         INNER JOIN  "
            + "     (SELECT   "
            + "         p.patient_id, MIN(e.encounter_datetime) encounter_datetime  "
            + "     FROM  "
            + "         patient p  "
            + "     INNER JOIN encounter e ON e.patient_id = p.patient_id  "
            + "     INNER JOIN obs o ON o.encounter_id = e.encounter_id  "
            + "     WHERE  "
            + "         p.voided = 0 AND e.voided = 0  "
            + "             AND o.voided = 0  "
            + "             AND e.location_id = :location  "
            + "             AND e.encounter_type = ${6}  "
            + "             AND e.encounter_datetime BETWEEN :startDate AND :endDate  "
            + "             AND o.concept_id = ${856}  "
            + "     GROUP BY p.patient_id) AS list ON list.patient_id = p.patient_id  "
            + " WHERE  "
            + "     p.voided = 0 AND e.voided = 0  "
            + "         AND o.voided = 0  "
            + "         AND e.location_id = :location  "
            + "         AND DATE(e.encounter_datetime) = DATE(list.encounter_datetime)  "
            + "         AND o.concept_id = ${856}  "
            + "         AND o.value_numeric > 1000;";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>MQ13</b>: Melhoria de Qualidade Category 13 Part 4, G <br>
   *
   * <ul>
   *   <li>G - Select all patients who have 3 APSS&PP (encounter type 35) consultations in 99 days
   *       after Viral Load Result (the oldest date from B2) following the conditions:
   *       <p>G1 - One Consultation (Encounter_datetime (from encounter type 35)) on the same date
   *       when the Viral Load with >1000 result was recorded (the oldest date from B2) AND G2 -
   *       Another consultation (Encounter_datetime (from encounter type 35) > “Viral Load Date”
   *       (the oldest date from B2)+20dias and <=“Viral Load Date” (the oldest date from
   *       B2)+33days. AND G3 - Another consultation (Encounter_datetime (from encounter type 35)) >
   *       “Second Date” (date from G2, the oldest from G2)+20days and <=“Second Date” (date from
   *       G2, the oldest one)+33days.
   *   <li>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQ13P4G() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    sqlCohortDefinition.setName("Category 13 - Part 4 Denominator - G");

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String query =
        "SELECT DISTINCT   "
            + "      p.patient_id   "
            + "  FROM   "
            + "      patient p   "
            + "         INNER JOIN   "
            + "      encounter e ON p.patient_id = e.patient_id   "
            + "          INNER JOIN   "
            + "      (SELECT  "
            + "     p.patient_id,DATE(list.encounter_datetime) encounter_datetime   "
            + " FROM  "
            + "     patient p  "
            + "         INNER JOIN  "
            + "     encounter e ON e.patient_id = p.patient_id  "
            + "         INNER JOIN  "
            + "     obs o ON o.encounter_id = e.encounter_id  "
            + "         INNER JOIN  "
            + "     (SELECT   "
            + "         p.patient_id, MIN(e.encounter_datetime) encounter_datetime  "
            + "     FROM  "
            + "         patient p  "
            + "     INNER JOIN encounter e ON e.patient_id = p.patient_id  "
            + "     INNER JOIN obs o ON o.encounter_id = e.encounter_id  "
            + "     WHERE  "
            + "         p.voided = 0 AND e.voided = 0  "
            + "             AND o.voided = 0  "
            + "             AND e.location_id = :location  "
            + "             AND e.encounter_type = ${6}  "
            + "             AND e.encounter_datetime BETWEEN :startDate AND :endDate  "
            + "             AND o.concept_id = ${856}  "
            + "     GROUP BY p.patient_id) AS list ON list.patient_id = p.patient_id  "
            + " WHERE  "
            + "     p.voided = 0 AND e.voided = 0  "
            + "         AND o.voided = 0  "
            + "         AND e.location_id = :location  "
            + "         AND DATE(e.encounter_datetime) = DATE(list.encounter_datetime)  "
            + "         AND o.concept_id = ${856}  "
            + "         AND o.value_numeric >= 1000) vl ON p.patient_id = vl.patient_id   "
            + "          INNER JOIN   "
            + "      (SELECT    "
            + "          p.patient_id, e.encounter_datetime AS primeira   "
            + "      FROM   "
            + "          patient p   "
            + "      INNER JOIN encounter e ON e.patient_id = p.patient_id   "
            + "      WHERE   "
            + "          p.voided = 0 AND e.voided = 0   "
            + "              AND e.encounter_type = ${35}) visit1 ON visit1.patient_id = p.patient_id   "
            + "          INNER JOIN   "
            + "      (SELECT    "
            + "          p.patient_id, e.encounter_datetime AS segunda   "
            + "      FROM   "
            + "          patient p   "
            + "      INNER JOIN encounter e ON e.patient_id = p.patient_id   "
            + "      WHERE   "
            + "          p.voided = 0 AND e.voided = 0   "
            + "              AND e.encounter_type = ${35}) visit2 ON visit2.patient_id = p.patient_id   "
            + "          INNER JOIN   "
            + "      (SELECT    "
            + "          p.patient_id, e.encounter_datetime AS terceira   "
            + "      FROM   "
            + "          patient p   "
            + "      INNER JOIN encounter e ON e.patient_id = p.patient_id   "
            + "      WHERE   "
            + "          p.voided = 0 AND e.voided = 0   "
            + "              AND e.encounter_type = ${35}) visit3 ON visit3.patient_id = p.patient_id   "
            + "  WHERE p.voided = 0   "
            + "   AND   e.voided = 0     "
            + "          AND e.encounter_type = ${35}   "
            + "          AND e.location_id = :location   "
            + "          AND DATE(visit1.primeira) = DATE(vl.encounter_datetime)   "
            + "          AND DATE(visit2.segunda) > DATE(visit1.primeira)   "
            + "          AND DATE(visit2.segunda) >= DATE_ADD(visit1.primeira, INTERVAL 20 DAY)   "
            + "          AND DATE(visit2.segunda) <= DATE_ADD(visit1.primeira, INTERVAL 33 DAY)   "
            + "          AND DATE(visit3.terceira) > DATE(visit2.segunda)   "
            + "          AND DATE(visit3.terceira) >= DATE_ADD(visit2.segunda, INTERVAL 20 DAY)    "
            + "          AND DATE(visit3.terceira) <= DATE_ADD(visit2.segunda, INTERVAL 33 DAY);";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>MQ13</b>: Melhoria de Qualidade Category 13 Part 4, H <br>
   *
   * <ul>
   *   <li>Select all patients with clinical consultation (encounter type 6) with concept “PEDIDO DE
   *       INVESTIGACOES LABORATORIAIS” (Concept Id 23722) and value coded “HIV CARGA VIRAL”
   *       (Concept Id 856) on Encounter_datetime between “Viral Load Date” (the oldest date from
   *       B2)+80 days and “Viral Load Date” (the oldest date from B2)+130 days.
   *   <li>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQ13P4H() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    sqlCohortDefinition.setName("Category 13 - Part 4 Denominator - H");

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());

    String query =
        " SELECT DISTINCT  "
            + "        p.patient_id  "
            + "    FROM  "
            + "        patient p  "
            + "            INNER JOIN  "
            + "        encounter e ON e.patient_id = p.patient_id  "
            + "            INNER JOIN  "
            + "        obs o ON o.encounter_id = e.encounter_id  "
            + "            INNER JOIN  "
            + "        (SELECT   "
            + "            p.patient_id, MIN(e.encounter_datetime) value_datetime  "
            + "        FROM  "
            + "            patient p  "
            + "        INNER JOIN encounter e ON e.patient_id = p.patient_id  "
            + "        INNER JOIN obs o ON o.encounter_id = e.encounter_id  "
            + "        WHERE  "
            + "            p.voided = 0 AND e.voided = 0  "
            + "                AND o.voided = 0  "
            + "                AND e.location_id = :location  "
            + "                AND e.encounter_type = ${6}  "
            + "                AND e.encounter_datetime BETWEEN :startDate AND :endDate  "
            + "                AND o.concept_id = ${856}  "
            + "                AND o.value_numeric >= 1000  "
            + "        GROUP BY p.patient_id) vl ON vl.patient_id = p.patient_id  "
            + "    WHERE  "
            + "        p.voided = 0 AND e.voided = 0  "
            + "            AND o.voided = 0  "
            + "            AND e.encounter_type = ${6}  "
            + "            AND e.location_id = :location  "
            + "            AND o.concept_id = ${23722}  "
            + "            AND o.value_coded = ${856}  "
            + "            AND DATE(e.encounter_datetime) BETWEEN DATE_ADD(vl.value_datetime,  "
            + "            INTERVAL 80 DAY) AND DATE_ADD(vl.value_datetime,  "
            + "            INTERVAL 130 DAY);";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>MQ13</b>: Melhoria de Qualidade Category 13 <br>
   * <i> DENOMINATOR 3: (B1 and B2) and NOT (C or D or F) and Age >= 15* </i> <br>
   * <i> DENOMINATOR 12: (B1 and B2) and NOT (C or D or F) and Age > 2 and Age < 15* </i> <br>
   * <i> DENOMINATOR 18: (B1 and B2 and C) and NOT (D or F) and Age > 2 and Age < 15* </i> <br>
   * <i> NUMERATOR 3: (B1 and B2 AND G AND H) and NOT (C or D or F) and Age >= 15* </i> <br>
   * <i> NUMERATOR 12: (B1 and B2 AND G AND H) and NOT (C or D or F) and Age > 2 and Age < 15* </i>
   * <br>
   *
   * <ul>
   *   <li>B1- Select all patients who have the LAST “LINHA TERAPEUTICA” (Concept Id 21151) recorded
   *       in Ficha Clinica (encounter type 6, encounter_datetime) with value coded “PRIMEIRA LINHA”
   *       (concept id 21150) during the inclusion period (startDateInclusion and endDateInclusion).
   *   <li>
   *   <li>B2 - Select all patients from Ficha Clinica (encounter type 6) with FIRST concept “Carga
   *       Viral” (Concept id 856) with value_numeric > 1000 and Encounter_datetime during the
   *       Inclusion period (startDateInclusion and endDateInclusion). Note: if there is more than
   *       one record with value_numeric > 1000 than consider the first occurrence during the
   *       inclusion period.
   *   <li>
   *   <li>
   *   <li>C - All female patients registered as “Pregnant” on a clinical consultation during the
   *       inclusion period (startDateInclusion and endDateInclusion)
   *   <li>
   *   <li>D - All female patients registered as “Breastfeeding” on a clinical consultation during
   *       the inclusion period (startDateInclusion and endDateInclusion)
   *   <li>
   *   <li>F - All Transferred Out patients
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQ13P4(Boolean den, Integer line) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    if (den) {
      if (line == 3) {
        compositionCohortDefinition.setName(
            "(B1 and B2)  and NOT (B4 or B5 or E or F) and Age > 14**");
      } else if (line == 12) {
        compositionCohortDefinition.setName(
            "(B1 and B2)  and NOT (B4 or B5 or E or F) and Age >= 2 and Age <= 15*");
      } else if (line == 18) {
        compositionCohortDefinition.setName("(B1 and B4)  and NOT (B5 or E or F)");
      }
    } else {
      if (line == 3) {
        compositionCohortDefinition.setName(
            "(B1 and B2 AND G AND H)  and NOT (B4 or B5 or E or F) and Age >= 15*");
      } else if (line == 12) {
        compositionCohortDefinition.setName(
            "(B1 and B2 AND G AND H)  and NOT (B4 or B5 or E or F) and Age > 2 and Age < 14*");
      } else if (line == 18) {
        compositionCohortDefinition.setName("(B1 and B4 AND G AND H)  and NOT (B5 or E or F)");
      }
    }
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition adults = this.ageCohortQueries.createXtoYAgeCohort("adullts", 2, 14);

    CohortDefinition patientsFromFichaClinicaLinhaTerapeutica =
        getPatientsFromFichaClinicaWithLastTherapeuticLineSetAsFirstLine_B1();

    CohortDefinition patientsFromFichaClinicaCargaViral = getB2_13();

    CohortDefinition pregnantWithCargaViralHigherThan1000 =
        QualityImprovement2020Queries.getMQ13DenB4_P4(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getHivViralLoadConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId(),
            commonMetadata.getPregnantConcept().getConceptId());

    CohortDefinition breastfeedingWithCargaViralHigherThan1000 =
        QualityImprovement2020Queries.getMQ13DenB5_P4(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getHivViralLoadConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId(),
            commonMetadata.getBreastfeeding().getConceptId());

    CohortDefinition transferredIn =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    CohortDefinition transferOut = commonCohortQueries.getTranferredOutPatients();

    CohortDefinition G = getMQ13P4G();

    CohortDefinition H = getMQ13P4H();
    compositionCohortDefinition.addSearch(
        "adults", EptsReportUtils.map(adults, "effectiveDate=${endDate}"));
    compositionCohortDefinition.addSearch(
        "B1", EptsReportUtils.map(patientsFromFichaClinicaLinhaTerapeutica, MAPPING1));

    compositionCohortDefinition.addSearch(
        "B2", EptsReportUtils.map(patientsFromFichaClinicaCargaViral, MAPPING1));

    compositionCohortDefinition.addSearch(
        "B4", EptsReportUtils.map(pregnantWithCargaViralHigherThan1000, MAPPING1));

    compositionCohortDefinition.addSearch(
        "B5", EptsReportUtils.map(breastfeedingWithCargaViralHigherThan1000, MAPPING1));

    compositionCohortDefinition.addSearch("E", EptsReportUtils.map(transferredIn, MAPPING));

    compositionCohortDefinition.addSearch("F", EptsReportUtils.map(transferOut, MAPPING1));

    compositionCohortDefinition.addSearch("G", EptsReportUtils.map(G, MAPPING));

    compositionCohortDefinition.addSearch("H", EptsReportUtils.map(H, MAPPING));

    if (den) {
      if (line == 3) {
        compositionCohortDefinition.setCompositionString(
            "(B1 AND B2) AND NOT (B4 or B5 or E or F)");
      } else if (line == 12) {
        compositionCohortDefinition.setCompositionString(
            "((B1 AND B2) AND NOT (B4 or B5 or E or F)) AND adults");
      } else if (line == 18) {
        compositionCohortDefinition.setCompositionString("(B1 AND B4) AND NOT (B5 or E or F)");
      }
    } else {
      if (line == 3) {
        compositionCohortDefinition.setCompositionString(
            "(B1 AND B2 AND G AND H) AND NOT (B4 or B5 or E or F)");
      } else if (line == 12) {
        compositionCohortDefinition.setCompositionString(
            "(B1 AND B2 AND G AND H) AND NOT (B4 or B5 or E or F)");
      } else if (line == 18) {
        compositionCohortDefinition.setCompositionString(
            "(B1 AND B4 AND G AND H) AND NOT (B5 or E or F)");
      }
    }
    return compositionCohortDefinition;
  }

  /**
   * B2 - Select all female patients with first clinical consultation (encounter type 6) that have
   * the concept “GESTANTE” (Concept Id 1982) and value coded “SIM” (Concept Id 1065) registered
   * (1)during the inclusion period (first occurrence, encounter_datetime >= startDateInclusion and
   * <=endDateInclusion) and (2) after the start of ART (encounter_datetime > “Patient ART Start
   * Date”)
   *
   * @return
   */
  public CohortDefinition getMQC13P2DenB2() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.addParameter(new Parameter("startDate", "StartDate", Date.class));
    cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.setName(" B2 - categoria 13 - Denominador - part 2");

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());

    String query =
        "    SELECT patient_id "
            + "    FROM    ("
            + "        SELECT p.patient_id, MIN(e.encounter_datetime) AS first_gestante "
            + "        FROM patient p "
            + "            INNER JOIN person per on p.patient_id=per.person_id "
            + "            INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "            INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "        WHERE   p.voided = 0 "
            + "        AND per.voided=0 AND per.gender = 'F' "
            + "          AND e.voided = 0 AND o.voided  = 0 "
            + "          AND e.encounter_type = ${6} AND o.concept_id = ${1982} "
            + "          AND o.value_coded = ${1065} AND e.location_id = :location "
            + "        GROUP BY p.patient_id) gest  "
            + "  WHERE  gest.first_gestante >= :startDate AND gest.first_gestante <= :endDate "
            + "  AND gest.first_gestante > (SELECT MIN(o.value_datetime) as art_date "
            + "        FROM encounter e "
            + "  INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "        WHERE   "
            + "  gest.patient_id = e.patient_id "
            + "          AND e.voided = 0 AND o.voided = 0 "
            + "          AND e.encounter_type = ${53} AND o.concept_id = ${1190} "
            + "          AND o.value_datetime IS NOT NULL AND o.value_datetime <= :endDate AND e.location_id = :location "
            + "    LIMIT 1) ";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));
    return cd;
  }

  /**
   * (B3=H from Numerator) - Select all patients with clinical consultation (encounter type 6) with
   * concept “PEDIDO DE INVESTIGACOES LABORATORIAIS” (Concept Id 23722) and value coded “HIV CARGA
   * VIRAL” (Concept Id 856) on Encounter_datetime between “Patient ART Start Date” (the oldest from
   * query A)+80days and “Patient ART Start Date” (the oldest from query A)+130days. Note: if more
   * than one encounter exists that satisfies these conditions, select the oldest one.
   *
   * @return
   */
  public CohortDefinition getMQC13P2DenB3() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.addParameter(new Parameter("startDate", "StartDate", Date.class));
    cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.setName(" B3 - categoria 13 - Denominador - part 2");

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    StringSubstitutor sb = new StringSubstitutor(map);

    String query =
        " SELECT p.patient_id "
            + " FROM patient p "
            + " INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + " INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + " INNER JOIN ( "
            + "             SELECT patient_id, art_date, encounter_id "
            + "             FROM	( "
            + "                     SELECT p.patient_id, Min(value_datetime) as art_date, e.encounter_id "
            + "                     FROM patient p "
            + "                     INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                     INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                     WHERE  p.voided = 0  "
            + "                           AND e.voided = 0  "
            + "                           AND o.voided = 0 "
            + "                           AND e.encounter_type = ${53}   "
            + "                           AND o.concept_id = ${1190}   "
            + "                           AND o.value_datetime IS NOT NULL "
            + "                           AND o.value_datetime <= :endDate   "
            + "                           AND e.location_id = :location  "
            + "                     GROUP  BY p.patient_id   "
            + "                   ) union_tbl  "
            + "             WHERE union_tbl.art_date  "
            + "                 BETWEEN :startDate AND :endDate   "
            + "            ) AS inicio ON inicio.patient_id = p.patient_id   "
            + " INNER JOIN ( "
            + "             SELECT p.patient_id, MIN(e.encounter_datetime) AS first_carga_viral, e.encounter_id "
            + "             FROM patient p  "
            + "             INNER JOIN encounter e ON e.patient_id = p.patient_id  "
            + "             INNER JOIN obs o ON e.encounter_id = o.encounter_id  "
            + "             WHERE p.voided = 0  "
            + "                   AND e.voided = 0  "
            + "                   AND o.voided  = 0  "
            + "                   AND e.encounter_type = ${6}   "
            + "                   AND o.concept_id = ${23722}   "
            + "                   AND o.value_coded = ${856} "
            + "                   AND e.location_id = :location   "
            + "             GROUP BY p.patient_id "
            + "            ) AS carga_viral  ON carga_viral.patient_id = p.patient_id   "
            + " WHERE p.voided = 0  "
            + "       AND e.voided = 0  "
            + "       AND o.voided  = 0  "
            + "       AND e.encounter_type = ${6}  "
            + "       AND o.concept_id = ${23722}  "
            + "       AND o.value_coded = ${856}  "
            + "       AND e.encounter_datetime >= DATE_ADD(inicio.art_date,INTERVAL 80 DAY)  "
            + "       AND e.encounter_datetime <= DATE_ADD(inicio.art_date,INTERVAL 130 DAY)  "
            + "       AND e.location_id = :location";

    cd.setQuery(sb.replace(query));
    return cd;
  }

  /**
   * (B4=J from Numerator) - Select all patients with clinical consultation (encounter type 6) with
   * concept “PEDIDO DE INVESTIGACOES LABORATORIAIS” (Concept Id 23722) and value coded “HIV CARGA
   * VIRAL” (Concept Id 856) on the first occurrence of concept “GESTANTE” (Concept Id 1982) and
   * value coded “SIM” ( Concept Id 1065) encounter_datetime during the inclusion period
   * (encounter_datetime from B2)
   *
   * @return
   */
  public CohortDefinition getgetMQC13P2DenB4() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.addParameter(new Parameter("startDate", "StartDate", Date.class));
    cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.setName(" B4 - categoria 13 - Denominador - part 2");

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    StringSubstitutor sb = new StringSubstitutor(map);

    String query =
        " SELECT p.patient_id "
            + " FROM patient p "
            + " INNER JOIN ( "
            + "             SELECT p.patient_id, MIN(e.encounter_datetime) AS first_gestante "
            + "             FROM patient p  "
            + "             INNER JOIN encounter e ON e.patient_id = p.patient_id  "
            + "             INNER JOIN obs o ON e.encounter_id = o.encounter_id  "
            + "             WHERE   p.voided = 0  "
            + "                     AND e.voided = 0  "
            + "                     AND o.voided = 0  "
            + "                     AND e.encounter_type = ${6}   "
            + "                     AND o.concept_id = ${1982}   "
            + "                     AND o.value_coded = ${1065}   "
            + "                     AND e.encounter_datetime BETWEEN :startDate AND :endDate  "
            + "                     AND e.location_id = :location   "
            + "             GROUP BY p.patient_id "
            + "           ) AS gestante ON gestante.patient_id = p.patient_id "
            + " INNER JOIN ( "
            + "             SELECT p.patient_id, e.encounter_datetime as carga_viral "
            + "             FROM patient p "
            + "             INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "             INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "             WHERE  p.voided = 0  "
            + "                    AND e.voided = 0  "
            + "                    AND o.voided = 0 "
            + "                    AND e.encounter_type = ${6}   "
            + "                    AND o.concept_id = ${23722}  "
            + "                    AND o.value_coded = ${856} "
            + "                    AND e.encounter_datetime BETWEEN :startDate AND :endDate  "
            + "                    AND e.location_id = :location  "
            + "             GROUP  BY p.patient_id   "
            + "           ) AS lab ON lab.patient_id = p.patient_id "
            + " WHERE p.voided = 0 "
            + "       AND gestante.first_gestante = lab.carga_viral";

    cd.setQuery(sb.replace(query));
    return cd;
  }

  /**
   * 13.15. % de MG elegíveis a CV com registo de pedido de CV feito pelo clínico (MG que iniciaram
   * TARV na CPN) Denominator: # de MG com registo de início do TARV na CPN dentro do período de
   * inclusão. (Line 90,Column F in the Template) as following: <code>
   * (A and B1) and NOT (D or F) and Age >= 15*</code>
   */
  public CohortDefinition getgetMQC13P2DenMGInIncluisionPeriod() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(" CAT 13 DEN - part 2 - 13.15. % de MG elegíveis ");
    cd.addParameter(new Parameter("startDate", "StartDate", Date.class));
    cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition startedART = getMOHArtStartDate();

    CohortDefinition transferOut = commonCohortQueries.getTranferredOutPatients();

    CohortDefinition breastfeeding =
        commonCohortQueries.getMohMQPatientsOnCondition(
            true,
            false,
            "once",
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getBreastfeeding(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition pregnant =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition transferredIn =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    cd.addSearch("A", EptsReportUtils.map(startedART, MAPPING));
    cd.addSearch("C", EptsReportUtils.map(pregnant, MAPPING));
    cd.addSearch("D", EptsReportUtils.map(breastfeeding, MAPPING));
    cd.addSearch(
        "E",
        EptsReportUtils.map(
            transferredIn,
            "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));
    cd.addSearch("F", EptsReportUtils.map(transferOut, MAPPING1));

    cd.setCompositionString("(A AND C) AND NOT (D OR E OR F)");

    return cd;
  }

  /**
   * 13.16. % de MG elegíveis a CV com registo de pedido de CV feito pelo clínico na primeira CPN
   * (MG que entraram em TARV na CPN) Denominator:# de MG que tiveram a primeira CPN no período de
   * inclusão, e que já estavam em TARV há mais de 3 meses (Line 91,Column F in the Template) as
   * following: <code>B2 and NOT (D or E or F) and Age >= 15*</code>
   */
  public CohortDefinition getgetMQC13P2DenMGInIncluisionPeriod33Month() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "StartDate", Date.class));
    cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch("B2", EptsReportUtils.map(getMQC13P2DenB2(), MAPPING));

    cd.setCompositionString("B2");

    return cd;
  }

  /**
   * 13.17. % de MG que receberam o resultado da Carga Viral dentro de 33 dias após pedido
   * Denominator: # de MG com registo de pedido de CV no período de revisão (Line 92,Column F in the
   * Template) as following: <code>
   * ((A and B1 and B3) or (B2 and B4)) and NOT (D or E or F) and Age >= 15*</code>
   */
  public CohortDefinition getMQC13P2DenMGInIncluisionPeriod33Days() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "StartDate", Date.class));
    cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition transfOut = commonCohortQueries.getTranferredOutPatients();

    CohortDefinition breastfeeding =
        commonCohortQueries.getMohMQPatientsOnCondition(
            true,
            false,
            "once",
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getBreastfeeding(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition transferredIn =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    CohortDefinition pregnant =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    cd.addSearch("A", EptsReportUtils.map(getMOHArtStartDate(), MAPPING));

    cd.addSearch("B2", EptsReportUtils.map(getMQC13P2DenB2(), MAPPING));
    cd.addSearch("B3", EptsReportUtils.map(getMQC13P2DenB3(), MAPPING));
    cd.addSearch("B4", EptsReportUtils.map(getgetMQC13P2DenB4(), MAPPING));

    cd.addSearch("C", EptsReportUtils.map(pregnant, MAPPING));
    cd.addSearch("D", EptsReportUtils.map(breastfeeding, MAPPING));
    cd.addSearch("E", EptsReportUtils.map(transferredIn, MAPPING));
    cd.addSearch("F", EptsReportUtils.map(transfOut, MAPPING1));

    cd.setCompositionString("((A AND C AND B3) AND NOT (D OR E OR F)) OR (B2 AND B4)");

    return cd;
  }
  /**
   * <b>M&Q Report - Cat 10 Indicator 10.3 - Numerator and Denominator<br>
   *
   * <p>Conta para o numerador: Crianças diagnosticadas através do teste de PCR (registados na ficha
   * resumo, no campo Cuidados de HIV-Data-PCR) e que iniciaram o TARV no período de inclusão,
   * dentro de 15 dias após a data do diagnóstico (isto é a diferença entre a data do diagnóstico
   * através do PCR registada na ficha resumo e a data do início do TARV registada na ficha resumo
   * deve ser igual ou inferior a 15 dias). Selecionar todos os que iniciaram TARV (1a Linha) no
   * período de inclusão e Filtrar as Crianças ( 0 a 18m) excluindo Mulheres Grávidas, Lactantes,
   * Criancas>18m, Adultos e Transferidos De, com a diferença entre a Data do Início TARV (Ficha
   * Clínica) e a Data de Diagnóstico (Ficha Resumo - Data do PCR) é entre 0 a 15 dias.
   *
   * <ul>
   *   <li>A - Select all patients who initiated ART during the Inclusion Period (startDateRevision
   *       and endDateInclusion)
   *   <li>B – Filter all patients diagnosed with the PCR test (registado na ficha resumo, no campo
   *       Cuidados de HIV-Data-PCR)
   *   <li>C - Exclude all transferred in patients
   *   <li>D – Filter all patients with “ART Start Date”( the oldest date from A) minus “Diagnosis
   *       Date” >=0 and <=15 days
   * </ul>
   *
   * <p>10.3. % de crianças com PCR positivo para HIV que iniciaram TARV dentro de 2 semanas após o
   * diagnóstico/entrega do resultado ao cuidador
   *
   * <ul>
   *   <li>Denominator: # de crianças com idade compreendida entre 0 - 18 meses, diagnosticadas
   *       através do teste de PCR (registados na ficha resumo, no campo Cuidados de HIV-Data-PCR) e
   *       que iniciaram o TARV no período de inclusão. <b>A and B and NOT C and Age>=0months and
   *       &lt;18months</b>
   *   <li>Numerator: # crianças com idade compreendida entre 0 - 18 meses, diagnosticadas através
   *       do PCR (registado na ficha resumo, no campo Cuidados de HIV-Data-PCR) que tenham iniciado
   *       o TARV dentro de 15 dias após o diagnóstico através do PCR. <b>A and B and D and NOT C
   *       and Age>=0months and <18months</b>
   * </ul>
   *
   * <p>Age should be calculated on Patient ART Start Date
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQ10NUMDEN103(String flag) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MQ10NUMDEN103 Cohort definition");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addSearch(
        "infant",
        EptsReportUtils.map(
            genericCohortQueries.getAgeInMonthsOnArtStartDate(0, 18),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));
    cd.addSearch("A", EptsReportUtils.map(getMOHArtStartDate(), MAPPING));
    cd.addSearch(
        "B",
        EptsReportUtils.map(
            commonCohortQueries.getMohMQPatientsOnCondition(
                false,
                true,
                "once",
                hivMetadata.getMasterCardEncounterType(),
                hivMetadata.getTypeTestHIVConcept(),
                Collections.singletonList(hivMetadata.getHivPCRQualitativeConceptUuid()),
                hivMetadata.getTypeTestHIVConcept(),
                Collections.singletonList(hivMetadata.getHivPCRQualitativeConceptUuid())),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "C",
        EptsReportUtils.map(
            commonCohortQueries.getMohMQPatientsOnCondition(
                false,
                true,
                "once",
                hivMetadata.getMasterCardEncounterType(),
                commonMetadata.getTransferFromOtherFacilityConcept(),
                Collections.singletonList(hivMetadata.getYesConcept()),
                hivMetadata.getTypeOfPatientTransferredFrom(),
                Collections.singletonList(hivMetadata.getArtStatus())),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "D",
        EptsReportUtils.map(
            genericCohortQueries.getArtDateMinusDiagnosisDate(),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));
    if (flag.equals("num")) {
      cd.setCompositionString("(A AND B AND D AND infant) AND NOT C");
    } else if (flag.equals("den")) {
      cd.setCompositionString("(A AND B AND infant) AND NOT C");
    }
    return cd;
  }

  /**
   * K - Select all patients from Ficha Clinica (encounter type 6) with concept “Carga Viral”
   * (Concept id 856, value_numeric not null) OR concept “Carga Viral Qualitative”(Concept id 1305,
   * value_coded not null) and Encounter_datetime > “Data de Pedido de Carga Viral”(the date from
   * B3) and Encounter_datetime <= “Data de Pedido de Carga Viral”(the date from B3)+33days
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQC13P2NumK() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.addParameter(new Parameter("startDate", "StartDate", Date.class));
    cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.setName(" K - categoria 13 - Numerador - part 2");

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        ""
            + "SELECT p.patient_id   "
            + "FROM patient p  "
            + "    INNER JOIN encounter e  "
            + "        ON e.patient_id = p.patient_id  "
            + "    INNER JOIN obs o  "
            + "        ON o.encounter_id = e.encounter_id  "
            + "    INNER JOIN  "
            + "                (  "
            + "                SELECT p.patient_id, e.encounter_datetime AS b3_datetime  "
            + "                FROM patient p  "
            + "                    INNER JOIN encounter e  "
            + "                        ON e.patient_id = p.patient_id  "
            + "                    INNER JOIN obs o  "
            + "                        ON o.encounter_id = e.encounter_id  "
            + "                    INNER JOIN  "
            + "                                (  "
            + "                                    SELECT inicio1.patient_id, inicio1.data_inicio  "
            + "                                    FROM (  "
            + "                                            SELECT   patient_id,Min(data_inicio) data_inicio  "
            + "                                            FROM (  "
            + "                                                    SELECT  p.patient_id, Min(value_datetime) data_inicio  "
            + "                                                    FROM       patient p  "
            + "                                                        INNER JOIN encounter e  "
            + "                                                            ON  p.patient_id = e.patient_id  "
            + "                                                        INNER JOIN obs o  "
            + "                                                            ON  e.encounter_id = o.encounter_id  "
            + "                                                    WHERE      p.voided = 0  "
            + "                                                        AND e.voided = 0  "
            + "                                                        AND o.voided = 0  "
            + "                                                        AND e.encounter_type = ${53}  "
            + "                                                        AND o.concept_id = ${1190}  "
            + "                                                        AND o.value_datetime IS NOT NULL  "
            + "                                                        AND o.value_datetime <= :endDate  "
            + "                                                        AND e.location_id = :location  "
            + "                                                    GROUP BY   p.patient_id  "
            + "                                                ) AS inicio  "
            + "                                                GROUP BY patient_id  "
            + "                                        ) inicio1  "
            + "                                    WHERE  data_inicio BETWEEN :startDate AND    :endDate  "
            + "                                ) art_start_date ON art_start_date.patient_id = p.patient_id  "
            + "                WHERE   "
            + "                    p.voided = 0  "
            + "                    AND e.voided = 0  "
            + "                    AND o.voided  = 0  "
            + "                    AND e.encounter_type = ${6}  "
            + "                    AND o.concept_id = ${23722}  "
            + "                    AND o.value_coded = ${856}  "
            + "                    AND e.encounter_datetime >= DATE_ADD(art_start_date.data_inicio,INTERVAL 80 DAY)  "
            + "                    AND e.encounter_datetime <= DATE_ADD(art_start_date.data_inicio,INTERVAL 130 DAY)  "
            + "                    AND e.location_id = :location  "
            + "                  "
            + "                            "
            + "                ) b3 ON b3.patient_id = p.patient_id       "
            + "WHERE   "
            + "    p.voided = 0  "
            + "    AND e.voided = 0  "
            + "    AND o.voided  = 0  "
            + "    AND e.encounter_type = ${6}  "
            + "    AND (  "
            + "            (o.concept_id = ${856}   AND o.value_numeric IS NOT NULL)  "
            + "            OR  "
            + "            (o.concept_id = ${1305}   AND o.value_coded IS NOT NULL)  "
            + "        )  "
            + "      "
            + "    AND e.encounter_datetime > b3.b3_datetime  "
            + "    AND e.encounter_datetime <= DATE_ADD(b3.b3_datetime, INTERVAL 33 DAY)  "
            + "    AND e.location_id = :location  ";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   * L - Select all patients from Ficha Clinica (encounter type 6) with concept “Carga Viral”
   * (Concept id 856, value_numeric not null) or concept “Carga Viral Qualitative”(Concept id 1305,
   * value_coded not null) and Encounter_datetime > “Data de Pedido de Carga Viral”(the date from
   * B4) and Encounter_datetime <= “Data de Pedido de Carga Viral”(the date from B4)+33days
   *
   * @return
   */
  public CohortDefinition getMQC13P2NumL() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.addParameter(new Parameter("startDate", "StartDate", Date.class));
    cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.setName(" L - categoria 13 - Numerador - part 2");

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM patient p "
            + "    INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "    INNER JOIN ( "
            + "        SELECT p.patient_id, e.encounter_datetime AS b4_datetime "
            + "        FROM patient p "
            + "            INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "            INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "            INNER JOIN ( "
            + "                SELECT p.patient_id, MIN(first_gestante) AS min_datetime "
            + "                FROM patient p "
            + "                    INNER JOIN ( "
            + "                        SELECT patient_id, art_date, encounter_id "
            + "                        FROM	( "
            + "                            SELECT p.patient_id, Min(value_datetime) as art_date, e.encounter_id  "
            + "                            FROM patient p  "
            + "                                INNER JOIN encounter e ON p.patient_id = e.patient_id  "
            + "                                INNER JOIN obs o ON e.encounter_id = o.encounter_id  "
            + "                            WHERE  p.voided = 0  "
            + "                              AND e.voided = 0  "
            + "                              AND o.voided = 0  "
            + "                              AND e.encounter_type = ${53} "
            + "                              AND o.concept_id = ${1190} "
            + "                              AND o.value_datetime IS NOT NULL "
            + "                              AND o.value_datetime <= :endDate "
            + "                              AND e.location_id = :location "
            + "                            GROUP  BY p.patient_id "
            + "                        ) union_tbl "
            + "                    ) AS inicio ON inicio.patient_id = p.patient_id "
            + "                    INNER JOIN ( "
            + "                        SELECT patient_id, first_gestante "
            + "                        FROM   ( "
            + "                            SELECT p.patient_id, MIN(e.encounter_datetime) AS first_gestante "
            + "                            FROM patient p "
            + "                                INNER JOIN person per on p.patient_id=per.person_id "
            + "                                INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                                INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                            WHERE   p.voided = 0 "
            + "                              AND per.gender = 'F' "
            + "                              AND e.voided = 0 "
            + "                              AND o.voided  = 0 "
            + "                              AND e.encounter_type = ${6} "
            + "                              AND o.concept_id = ${1982} "
            + "                              AND o.value_coded = ${1065} "
            + "                              AND e.location_id = :location "
            + "                            GROUP BY p.patient_id "
            + "                        ) gest "
            + "                        WHERE  gest.first_gestante BETWEEN :startDate AND :endDate "
            + "                    ) AS gestante  ON gestante.patient_id = p.patient_id AND gestante.first_gestante > inicio.art_date "
            + "                GROUP BY p.patient_id "
            + "                ) b2  ON b2.patient_id = p.patient_id "
            + "        WHERE p.voided = 0 "
            + "          AND e.voided = 0 "
            + "          AND o.voided  = 0 "
            + "          AND e.encounter_type = ${6} "
            + "          AND o.concept_id = ${23722} "
            + "          AND o.value_coded = ${856} "
            + "          AND e.encounter_datetime = b2.min_datetime "
            + "          AND e.location_id = :location "
            + "    ) b4 ON b4.patient_id = p.patient_id "
            + "WHERE p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided  = 0 "
            + "  AND e.encounter_type = ${6} "
            + "  AND ( "
            + "      (o.concept_id = ${856} AND o.value_numeric IS NOT NULL) "
            + "          OR "
            + "      (o.concept_id = ${1305} AND o.value_coded IS NOT NULL) "
            + "  ) "
            + "  AND e.encounter_datetime > b4.b4_datetime "
            + "  AND e.encounter_datetime <= DATE_ADD(b4.b4_datetime, INTERVAL 33 DAY) "
            + "  AND e.location_id = :location "
            + "GROUP BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   * M - Select all patients from Ficha Resumo (encounter type 53) with “HIV Carga” viral”(Concept
   * id 856, value_numeric not null) and obs_datetime >= “Data de Pedido, de Carga Viral”(the date
   * from B3) and obs_datetime <= “Data de Pedido de Carga Viral”(the date from B3)+33days
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQC13P2NumM() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.addParameter(new Parameter("startDate", "StartDate", Date.class));
    cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.setName(" M - categoria 13 - Numerador - part 2");

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        "SELECT p.patient_id   "
            + "            FROM patient p  "
            + "                INNER JOIN encounter e  "
            + "                    ON e.patient_id = p.patient_id  "
            + "                INNER JOIN obs o  "
            + "                    ON o.encounter_id = e.encounter_id  "
            + "                INNER JOIN  "
            + "                            (  "
            + "                            SELECT p.patient_id, e.encounter_datetime AS b3_datetime  "
            + "                            FROM patient p  "
            + "                                INNER JOIN encounter e  "
            + "                                    ON e.patient_id = p.patient_id  "
            + "                                INNER JOIN obs o  "
            + "                                    ON o.encounter_id = e.encounter_id  "
            + "                                INNER JOIN  "
            + "                                            (  "
            + "                                                SELECT inicio1.patient_id, inicio1.data_inicio  "
            + "                                                FROM (  "
            + "                                                        SELECT   patient_id,Min(data_inicio) data_inicio  "
            + "                                                        FROM (  "
            + "                                                                SELECT  p.patient_id, Min(value_datetime) data_inicio  "
            + "                                                                FROM       patient p  "
            + "                                                                    INNER JOIN encounter e  "
            + "                                                                        ON  p.patient_id = e.patient_id  "
            + "                                                                    INNER JOIN obs o  "
            + "                                                                        ON  e.encounter_id = o.encounter_id  "
            + "                                                                WHERE      p.voided = 0  "
            + "                                                                    AND e.voided = 0  "
            + "                                                                    AND o.voided = 0  "
            + "                                                                    AND e.encounter_type = 53  "
            + "                                                                    AND o.concept_id = 1190  "
            + "                                                                    AND o.value_datetime IS NOT NULL  "
            + "                                                                    AND o.value_datetime <= :endDate  "
            + "                                                                    AND e.location_id = :location  "
            + "                                                                GROUP BY   p.patient_id  "
            + "                                                            ) AS inicio  "
            + "                                                            GROUP BY patient_id  "
            + "                                                    ) inicio1  "
            + "                                                WHERE  data_inicio BETWEEN :startDate AND    :endDate  "
            + "                                            ) art_start_date ON art_start_date.patient_id = p.patient_id  "
            + "                            WHERE   "
            + "                                p.voided = 0  "
            + "                                AND e.voided = 0  "
            + "                                AND o.voided  = 0  "
            + "                                AND e.encounter_type = ${6}  "
            + "                                AND o.concept_id = ${23722}  "
            + "                                AND o.value_coded = ${856}  "
            + "                                AND e.encounter_datetime >= DATE_ADD(art_start_date.data_inicio,INTERVAL 80 DAY)  "
            + "                                AND e.encounter_datetime <= DATE_ADD(art_start_date.data_inicio,INTERVAL 130 DAY)  "
            + "                                AND e.location_id = :location  "
            + "                              "
            + "                                        "
            + "                            ) b3 ON b3.patient_id = p.patient_id       "
            + "            WHERE   "
            + "                p.voided = 0  "
            + "                AND e.voided = 0  "
            + "                AND o.voided  = 0  "
            + "                AND e.encounter_type = ${53}  "
            + "                AND o.concept_id = ${856}   "
            + "                AND o.value_numeric IS NOT NULL "
            + "                AND o.obs_datetime >= b3.b3_datetime  "
            + "                AND o.obs_datetime <= DATE_ADD(b3.b3_datetime, INTERVAL 33 DAY)  "
            + "                AND e.location_id = :location  ;";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   * N - Select all patients from Ficha Resumo (encounter type 53) with “HIV Carga viral”(Concept id
   * 856, value_numeric not null) and obs_datetime >= “Data de Pedido de Carga Viral”(the date from
   * B4) and obs_datetime <= “Data de Pedido de Carga Viral”(the date from B4)+33days
   *
   * @return
   */
  public CohortDefinition getMQC13P2NumN() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.addParameter(new Parameter("startDate", "StartDate", Date.class));
    cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.setName(" N - categoria 13 - Numerador - part 2");

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        "        SELECT p.patient_id "
            + "            FROM patient p "
            + "                INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                INNER JOIN ( "
            + "                    SELECT p.patient_id, e.encounter_datetime AS b4_datetime "
            + "                    FROM patient p "
            + "                        INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                        INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                        INNER JOIN ( "
            + "                            SELECT p.patient_id, MIN(first_gestante) AS min_datetime "
            + "                            FROM patient p "
            + "                                INNER JOIN ( "
            + "                                    SELECT patient_id, art_date, encounter_id "
            + "                                    FROM ( "
            + "                                        SELECT p.patient_id, Min(value_datetime) as art_date, e.encounter_id  "
            + "                                        FROM patient p  "
            + "                                            INNER JOIN encounter e ON p.patient_id = e.patient_id  "
            + "                                            INNER JOIN obs o ON e.encounter_id = o.encounter_id  "
            + "                                        WHERE  p.voided = 0  "
            + "                                          AND e.voided = 0  "
            + "                                          AND o.voided = 0  "
            + "                                          AND e.encounter_type = ${53} "
            + "                                          AND o.concept_id = 1190 "
            + "                                          AND o.value_datetime IS NOT NULL "
            + "                                          AND o.value_datetime <= :endDate "
            + "                                          AND e.location_id = :location "
            + "                                        GROUP  BY p.patient_id "
            + "                                    ) union_tbl "
            + "                                ) AS inicio ON inicio.patient_id = p.patient_id "
            + "                                INNER JOIN ( "
            + "                                    SELECT patient_id, first_gestante "
            + "                                    FROM   ( "
            + "                                        SELECT p.patient_id, MIN(e.encounter_datetime) AS first_gestante "
            + "                                        FROM patient p "
            + "                                            INNER JOIN person per on p.patient_id=per.person_id "
            + "                                            INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                                            INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                                        WHERE   p.voided = 0 "
            + "                                          AND per.gender = 'F' "
            + "                                          AND e.voided = 0 "
            + "                                          AND o.voided  = 0 "
            + "                                          AND e.encounter_type = 6 "
            + "                                          AND o.concept_id = 1982 "
            + "                                          AND o.value_coded = 1065 "
            + "                                          AND e.location_id = :location "
            + "                                        GROUP BY p.patient_id "
            + "                                    ) gest "
            + "                                    WHERE  gest.first_gestante BETWEEN :startDate AND :endDate "
            + "                                ) AS gestante  ON gestante.patient_id = p.patient_id AND gestante.first_gestante > inicio.art_date "
            + "                            GROUP BY p.patient_id "
            + "                            ) b2  ON b2.patient_id = p.patient_id "
            + "                    WHERE p.voided = 0 "
            + "                      AND e.voided = 0 "
            + "                      AND o.voided  = 0 "
            + "                      AND e.encounter_type = ${6} "
            + "                      AND o.concept_id = ${23722} "
            + "                      AND o.value_coded = ${856} "
            + "                      AND e.encounter_datetime = b2.min_datetime "
            + "                      AND e.location_id = :location "
            + "                ) b4 ON b4.patient_id = p.patient_id "
            + "            WHERE p.voided = 0  "
            + "                AND e.voided = 0  "
            + "                AND o.voided  = 0  "
            + "                AND e.encounter_type = ${53}  "
            + "                AND o.concept_id = ${856}   "
            + "                AND o.value_numeric IS NOT NULL"
            + "              AND o.obs_datetime >= b4.b4_datetime "
            + "              AND o.obs_datetime <= DATE_ADD(b4.b4_datetime, INTERVAL 33 DAY) "
            + "              AND e.location_id = :location "
            + "            GROUP BY p.patient_id;";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   * 13.15. % de MG elegíveis a CV com registo de pedido de CV feito pelo clínico (MG que iniciaram
   * TARV na CPN) (Line 90 in the template) Numerator (Column E in the Template) as following:
   * <code>(A and B1 and H) and NOT (D or F) and Age >= 15*</code>
   */
  public CohortDefinition getMQC13P2Num1() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "StartDate", Date.class));
    cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition pregnant =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition transfOut = commonCohortQueries.getTranferredOutPatients();

    CohortDefinition breastfeeding =
        commonCohortQueries.getMohMQPatientsOnCondition(
            true,
            false,
            "once",
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getBreastfeeding(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition transferredIn =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    cd.addSearch("A", EptsReportUtils.map(getMOHArtStartDate(), MAPPING));
    cd.addSearch("C", EptsReportUtils.map(pregnant, MAPPING));
    cd.addSearch("D", EptsReportUtils.map(breastfeeding, MAPPING));
    cd.addSearch(
        "E",
        EptsReportUtils.map(
            transferredIn,
            "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));
    cd.addSearch("F", EptsReportUtils.map(transfOut, MAPPING1));
    cd.addSearch("H", EptsReportUtils.map(getMQC13P2DenB3(), MAPPING));

    cd.setCompositionString("(A AND C AND H) AND NOT (D OR E OR F)");
    return cd;
  }

  /**
   * 13.16. % de MG elegíveis a CV com registo de pedido de CV feito pelo clínico na primeira CPN
   * (MG que entraram em TARV na CPN) (Line 91 in the template) Numerator (Column E in the Template)
   * as following: <code>(B2 and J) and NOT (D or E or F) and Age >= 15*</code>
   */
  public CohortDefinition getMQC13P2Num2() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "StartDate", Date.class));
    cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch("B2", EptsReportUtils.map(getMQC13P2DenB2(), MAPPING));
    cd.addSearch("J", EptsReportUtils.map(getgetMQC13P2DenB4(), MAPPING));

    cd.setCompositionString("(B2 AND J)");

    return cd;
  }

  /**
   * 13.17. % de MG que receberam o resultado da Carga Viral dentro de 33 dias após pedido (Line 92
   * in the template) Numerator (Column E in the Template) as following: <code>
   * ((A and B1 and B3 and K) or (B2 and B4 and L)) and NOT (D or E or F) and Age >= 15*</code>
   */
  public CohortDefinition getMQC13P2Num3() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "StartDate", Date.class));
    cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition transfOut = commonCohortQueries.getTranferredOutPatients();

    CohortDefinition breastfeeding =
        commonCohortQueries.getMohMQPatientsOnCondition(
            true,
            false,
            "once",
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getBreastfeeding(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition transferredIn =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    CohortDefinition pregnant =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    cd.addSearch("A", EptsReportUtils.map(getMOHArtStartDate(), MAPPING));

    cd.addSearch("B2", EptsReportUtils.map(getMQC13P2DenB2(), MAPPING));
    cd.addSearch("B3", EptsReportUtils.map(getMQC13P2DenB3(), MAPPING));
    cd.addSearch("B4", EptsReportUtils.map(getgetMQC13P2DenB4(), MAPPING));

    cd.addSearch("C", EptsReportUtils.map(pregnant, MAPPING));
    cd.addSearch("D", EptsReportUtils.map(breastfeeding, MAPPING));
    cd.addSearch("E", EptsReportUtils.map(transferredIn, MAPPING));
    cd.addSearch("F", EptsReportUtils.map(transfOut, MAPPING1));
    cd.addSearch("K", EptsReportUtils.map(getMQC13P2NumK(), MAPPING));
    cd.addSearch("L", EptsReportUtils.map(getMQC13P2NumL(), MAPPING));
    cd.addSearch("M", EptsReportUtils.map(getMQC13P2NumM(), MAPPING));
    cd.addSearch("N", EptsReportUtils.map(getMQC13P2NumN(), MAPPING));

    cd.setCompositionString(
        "((A AND C AND B3 AND (K OR M)) AND NOT (D OR E OR F)) OR (B2 AND B4 AND (L OR N))");

    return cd;
  }

  /**
   * <b>MQ15Den: Melhoria de Qualidade Category 15 Denominator</b><br>
   * <br>
   * <i> DENOMINATOR 1: A and NOT B1 and NOT C and NOT D and NOT F and Age > 14 </i> <br>
   * <br>
   * <i> DENOMINATOR 2: A and NOT B1 and NOT C and NOT D and NOT F and Age > 14 </i> <br>
   * <br>
   * <i> DENOMINATOR 3: A and NOT B1 and NOT C and NOT D and NOT F and Age > 14 </i> <br>
   * <br>
   * <i> DENOMINATOR 4: A and NOT B1 and NOT C and NOT D and NOT F and Age > 14 </i> <br>
   * <br>
   * <i> DENOMINATOR 5: (A2 or A3) and NOT B1 and NOT C and NOT D and NOT F and Age between 2 and 9
   * </i> <br>
   * <br>
   * <i> DENOMINATOR 6: (A2 or A3) and NOT B1 and NOT C and NOT D and NOT F and Age between 10 and
   * 14 </i> <br>
   * <br>
   * <i> DENOMINATOR 7: (A2 or A3) and NOT B1 and NOT C and NOT D and NOT F and Age between 2 and 9
   * </i> <br>
   * <br>
   * <i> DENOMINATOR 8: (A2 or A3) and NOT B1 and NOT C and NOT D and NOT F and Age between 10 and
   * 14 </i> <br>
   * <br>
   * <i> DENOMINATOR 9: (A2 or A3) and NOT B1 and NOT C and NOT D and NOT F and Age between 2 and 9
   * </i> <br>
   * <br>
   * <i> DENOMINATOR 10: (A2 or A3) and NOT B1 and NOT C and NOT D and NOT F and Age between 10 and
   * 14 </i> <br>
   * <br>
   * <i> DENOMINATOR 11: (A2 or A3) and NOT B1 and NOT C and NOT D and NOT F and Age between 2 and 9
   * </i> <br>
   * <br>
   * <i> DENOMINATOR 12: (A2 or A3) and NOT B1 and NOT C and NOT D and NOT F and Age between 10 and
   * 14 </i> <br>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQ15DEN(Integer den) {
    CompositionCohortDefinition comp = new CompositionCohortDefinition();

    switch (den) {
      case 1:
        comp.setName(
            "% de Adultos (15/+anos) inscritos há 12 meses em algum MDS  (DT ou GAAC) que continuam activos em TARV");
        break;
      case 2:
        comp.setName(
            "% de Adultos (15/+anos) inscritos há pelo menos 12 meses em algum MDS (DT ou GAAC) com pedido de pelo menos uma CV");
        break;
      case 3:
        comp.setName(
            "% de Adultos (15/+anos) inscritos há 12 meses em algum MDS (DT ou GAAC) que receberam pelo menos um resultado de CV");
        break;
      case 4:
        comp.setName(
            "% de Adultos (15/+anos) inscritos há 12 meses em algum MDS (DT ou GAAC) com CV <1000 Cópias");
        break;
      case 5:
        comp.setName(
            "% de Crianças (2-9 anos) inscritas há 12 meses em algum MDS (DT) que continuam activos em TARV");
        break;
      case 6:
        comp.setName(
            "% de Crianças (10-14 anos) inscritas há 12 em algum MDS (DT) que continuam activos em TARV");
        break;
      case 7:
        comp.setName(
            "% de Crianças (2-9 anos de idade) inscritas há 12 meses em algum MDS (DT) com pedido de pelo menos uma CV");
        break;
      case 8:
        comp.setName(
            "% de Crianças (10-14 anos de idade) inscritas há 12 meses em algum MDS (DT) com pedido de pelo menos uma CV");
        break;
      case 9:
        comp.setName(
            "% de Crianças (2-9 anos) inscritas há 12 meses em algum MDS (DT) que receberam pelo menos um resultado de CV");
        break;
      case 10:
        comp.setName(
            "% de Crianças (10-14 anos) inscritas há 12 meses em algum MDS (DT) que receberam pelo menos um resultado de CV");
        break;
      case 11:
        comp.setName(
            "% de Crianças (2-9 anos) inscritas há 12 meses em algum MDS (DT) com CV <1000 Cópias");
        break;
      case 12:
        comp.setName(
            "% de Crianças (10-14 anos) inscritas há 12 meses em algum MDS (DT) com CV <1000 Cópias");
        break;
    }

    comp.addParameter(new Parameter("startDate", "startDate", Date.class));
    comp.addParameter(new Parameter("endDate", "endDate", Date.class));
    comp.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    comp.addParameter(new Parameter("location", "location", Date.class));

    CohortDefinition queryA1 =
        QualityImprovement2020Queries.getMQ15DenA1(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getStartDrugs().getConceptId(),
            hivMetadata.getQuarterlyConcept().getConceptId(),
            hivMetadata.getGaac().getConceptId(),
            hivMetadata.getQuarterlyDispensation().getConceptId(),
            hivMetadata.getTypeOfDispensationConcept().getConceptId());

    CohortDefinition queryA2 =
        QualityImprovement2020Queries.getMQ15DenA1orA2(
            "A2",
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getStartDrugs().getConceptId(),
            hivMetadata.getGaac().getConceptId(),
            hivMetadata.getQuarterlyDispensation().getConceptId());

    CohortDefinition queryA3 =
        genericCohortQueries.hasCodedObs(
            hivMetadata.getTypeOfDispensationConcept(),
            BaseObsCohortDefinition.TimeModifier.LAST,
            SetComparator.IN,
            Arrays.asList(hivMetadata.getAdultoSeguimentoEncounterType()),
            Arrays.asList(hivMetadata.getQuarterlyConcept()));

    CohortDefinition transferOut = commonCohortQueries.getTranferredOutPatients();

    comp.addSearch(
        "A1",
        EptsReportUtils.map(
            queryA1,
            "startDate=${revisionEndDate-14m},endDate=${revisionEndDate-11m},location=${location}"));

    comp.addSearch("A2", EptsReportUtils.map(queryA2, MAPPING1));

    comp.addSearch(
        "A3",
        EptsReportUtils.map(
            queryA3,
            "onOrAfter=${revisionEndDate-14m},onOrBefore=${revisionEndDate-11m},locationList=${location}"));

    comp.addSearch(
        "CD",
        EptsReportUtils.map(
            getPregnantOrBreastfeedingWomen(),
            "revisionEndDate=${revisionEndDate},location=${location}"));
    comp.addSearch(
        "G2",
        EptsReportUtils.map(
            getCombinedB13ForCat15Indicators(),
            "revisionEndDate=${revisionEndDate},location=${location}"));

    comp.addSearch("F", EptsReportUtils.map(transferOut, MAPPING1));

    if (den == 1) {
      comp.setCompositionString("(A1 OR A3) AND NOT (CD OR F)");
    } else if (den == 2 || den == 3 || den == 4) {
      comp.setCompositionString("((A1 OR A3) AND NOT (CD OR F)) AND G2");
    } else if (den == 5 || den == 6) {
      comp.setCompositionString("(A2 OR A3) AND  NOT (CD OR F)");
    } else if (den == 7 || den == 9 || den == 11 || den == 8 || den == 10 || den == 12) {
      comp.setCompositionString("((A2 OR A3) AND  NOT (CD OR F)) AND G2");
    }
    return comp;
  }

  /**
   * <b>MQ15Num: Melhoria de Qualidade Category 15 Numerator</b><br>
   * <br>
   * <i> NUMERATOR 1: A and NOT B1 and NOT C and NOT D and NOT F and Age > 14 </i> <br>
   * <br>
   * <i> NUMERATOR 2: A and NOT B1 and NOT C and NOT D and NOT F and Age > 14 </i> <br>
   * <br>
   * <i> NUMERATOR 3: A and NOT B1 and NOT C and NOT D and NOT F and Age > 14 </i> <br>
   * <br>
   * <i> NUMERATOR 4: A and NOT B1 and NOT C and NOT D and NOT F and Age > 14 </i> <br>
   * <br>
   * <i> NUMERATOR 5: (A2 or A3) and NOT B1 and NOT C and NOT D and NOT F and Age between 2 and 9
   * </i> <br>
   * <br>
   * <i> NUMERATOR 6: (A2 or A3) and NOT B1 and NOT C and NOT D and NOT F and Age between 10 and 14
   * </i> <br>
   * <br>
   * <i> NUMERATOR 7: (A2 or A3) and NOT B1 and NOT C and NOT D and NOT F and Age between 2 and 9
   * </i> <br>
   * <br>
   * <i> NUMERATOR 8: (A2 or A3) and NOT B1 and NOT C and NOT D and NOT F and Age between 10 and 14
   * </i> <br>
   * <br>
   * <i> NUMERATOR 9: (A2 or A3) and NOT B1 and NOT C and NOT D and NOT F and Age between 2 and 9
   * </i> <br>
   * <br>
   * <i> NUMERATOR 10: (A2 or A3) and NOT B1 and NOT C and NOT D and NOT F and Age between 10 and 14
   * </i> <br>
   * <br>
   * <i> NUMERATOR 11: (A2 or A3) and NOT B1 and NOT C and NOT D and NOT F and Age between 2 and 9
   * </i> <br>
   * <br>
   * <i> NUMERATOR 12: (A2 or A3) and NOT B1 and NOT C and NOT D and NOT F and Age between 10 and 14
   * </i> <br>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQ15NUM(Integer num) {
    CompositionCohortDefinition comp = new CompositionCohortDefinition();

    switch (num) {
      case 1:
        comp.setName(
            "% de Adultos (15/+anos) inscritos há 12 meses em algum MDS  (DT ou GAAC) que continuam activos em TARV");
        break;
      case 2:
        comp.setName(
            "% de Adultos (15/+anos) inscritos há pelo menos 12 meses em algum MDS (DT ou GAAC) com pedido de pelo menos uma CV");
        break;
      case 3:
        comp.setName(
            "% de Adultos (15/+anos) inscritos há 12 meses em algum MDS (DT ou GAAC) que receberam pelo menos um resultado de CV");
        break;
      case 4:
        comp.setName(
            "% de Adultos (15/+anos) inscritos há 12 meses em algum MDS (DT ou GAAC) com CV <1000 Cópias");
        break;
      case 5:
        comp.setName(
            "% de Crianças (2-9 anos) inscritas há 12 meses em algum MDS (DT) que continuam activos em TARV");
        break;
      case 6:
        comp.setName(
            "% de Crianças (10-14 anos) inscritas há 12 em algum MDS (DT) que continuam activos em TARV");
        break;
      case 7:
        comp.setName(
            "% de Crianças (2-9 anos de idade) inscritas há 12 meses em algum MDS (DT) com pedido de pelo menos uma CV");
        break;
      case 8:
        comp.setName(
            "% de Crianças (10-14 anos de idade) inscritas há 12 meses em algum MDS (DT) com pedido de pelo menos uma CV");
        break;
      case 9:
        comp.setName(
            "% de Crianças (2-9 anos) inscritas há 12 meses em algum MDS (DT) que receberam pelo menos um resultado de CV");
        break;
      case 10:
        comp.setName(
            "% de Crianças (10-14 anos) inscritas há 12 meses em algum MDS (DT) que receberam pelo menos um resultado de CV");
        break;
      case 11:
        comp.setName(
            "% de Crianças (2-9 anos) inscritas há 12 meses em algum MDS (DT) com CV <1000 Cópias");
        break;
      case 12:
        comp.setName(
            "% de Crianças (10-14 anos) inscritas há 12 meses em algum MDS (DT) com CV <1000 Cópias");
        break;
    }

    comp.addParameter(new Parameter("startDate", "startDate", Date.class));
    comp.addParameter(new Parameter("endDate", "endDate", Date.class));
    comp.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    comp.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition queryA1 =
        QualityImprovement2020Queries.getMQ15DenA1(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getStartDrugs().getConceptId(),
            hivMetadata.getQuarterlyConcept().getConceptId(),
            hivMetadata.getGaac().getConceptId(),
            hivMetadata.getQuarterlyDispensation().getConceptId(),
            hivMetadata.getTypeOfDispensationConcept().getConceptId());

    CohortDefinition queryA2 =
        QualityImprovement2020Queries.getMQ15DenA1orA2(
            "A2",
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getStartDrugs().getConceptId(),
            hivMetadata.getGaac().getConceptId(),
            hivMetadata.getQuarterlyDispensation().getConceptId());

    CohortDefinition queryA3 =
        QualityImprovement2020Queries.getMQ15DenA3(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getQuarterlyConcept().getConceptId(),
            hivMetadata.getTypeOfDispensationConcept().getConceptId());

    CohortDefinition pregnant =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition breastfeeding =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition h1 =
        QualityImprovement2020Queries.getMQ15NumH(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getStartDrugs().getConceptId(),
            hivMetadata.getQuarterlyConcept().getConceptId(),
            hivMetadata.getGaac().getConceptId(),
            hivMetadata.getQuarterlyDispensation().getConceptId(),
            hivMetadata.getTypeOfDispensationConcept().getConceptId(),
            hivMetadata.getApplicationForLaboratoryResearch().getConceptId(),
            hivMetadata.getHivViralLoadConcept().getConceptId());

    CohortDefinition h2 =
        QualityImprovement2020Queries.getMQ15NumH2(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getStartDrugs().getConceptId(),
            hivMetadata.getQuarterlyConcept().getConceptId(),
            hivMetadata.getGaac().getConceptId(),
            hivMetadata.getQuarterlyDispensation().getConceptId(),
            hivMetadata.getTypeOfDispensationConcept().getConceptId(),
            hivMetadata.getApplicationForLaboratoryResearch().getConceptId(),
            hivMetadata.getHivViralLoadConcept().getConceptId(),
            hivMetadata.getHivViralLoadQualitative().getConceptId(),
            hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());

    CohortDefinition i =
        QualityImprovement2020Queries.getMQ15NumI(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getStartDrugs().getConceptId(),
            hivMetadata.getQuarterlyConcept().getConceptId(),
            hivMetadata.getGaac().getConceptId(),
            hivMetadata.getQuarterlyDispensation().getConceptId(),
            hivMetadata.getTypeOfDispensationConcept().getConceptId(),
            hivMetadata.getApplicationForLaboratoryResearch().getConceptId(),
            hivMetadata.getHivViralLoadConcept().getConceptId(),
            hivMetadata.getHivViralLoadQualitative().getConceptId(),
            hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());

    CohortDefinition transferOut = commonCohortQueries.getTranferredOutPatients();

    comp.addSearch(
        "A1",
        EptsReportUtils.map(
            queryA1,
            "startDate=${revisionEndDate-14m},endDate=${revisionEndDate-11m},location=${location}"));

    comp.addSearch("A2", EptsReportUtils.map(queryA2, MAPPING1));

    comp.addSearch(
        "A3",
        EptsReportUtils.map(
            queryA3,
            "startDate=${revisionEndDate-14m},endDate=${revisionEndDate-11m},location=${location}"));

    comp.addSearch(
        "C",
        EptsReportUtils.map(
            pregnant,
            "startDate=${revisionEndDate-14m},endDate=${revisionEndDate-11m},location=${location}"));

    comp.addSearch(
        "D",
        EptsReportUtils.map(
            breastfeeding,
            "startDate=${revisionEndDate-14m},endDate=${revisionEndDate-11m},location=${location}"));

    comp.addSearch("F", EptsReportUtils.map(transferOut, MAPPING1));

    comp.addSearch(
        "H1",
        EptsReportUtils.map(
            h1, "startDate=${startDate},revisionEndDate=${revisionEndDate},location=${location}"));

    comp.addSearch(
        "H2",
        EptsReportUtils.map(
            h2, "startDate=${startDate},revisionEndDate=${revisionEndDate},location=${location}"));

    comp.addSearch(
        "I",
        EptsReportUtils.map(
            i, "startDate=${startDate},revisionEndDate=${revisionEndDate},location=${location}"));

    comp.addSearch(
        "Den1",
        EptsReportUtils.map(
            getMQ15DEN(1),
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));
    comp.addSearch(
        "Den5",
        EptsReportUtils.map(
            getMQ15DEN(5),
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));
    comp.addSearch(
        "G2",
        EptsReportUtils.map(
            getCombinedB13ForCat15Indicators(),
            "revisionEndDate=${revisionEndDate},location=${location}"));

    if (num == 1) {
      comp.setCompositionString("Den1 AND G2");
    } else if (num == 2) {
      comp.setCompositionString("Den1 AND H1 AND G2");
    } else if (num == 3) {
      comp.setCompositionString("Den1 AND H2 AND G2");
    } else if (num == 4) {
      comp.setCompositionString("Den1 AND I AND G2");
    } else if (num == 5 || num == 6) {
      comp.setCompositionString("Den5 AND G2");
    } else if (num == 7 || num == 8) {
      comp.setCompositionString("Den5 AND H1 AND G2");
    } else if (num == 9 || num == 10) {
      comp.setCompositionString("Den5 AND H2 AND G2");
    } else if (num == 11 || num == 12) {
      comp.setCompositionString("Den5 AND I AND G2");
    }
    return comp;
  }

  /**
   * <b>MQ14Den: M&Q Report - Categoria 14 Denominador</b><br>
   * <i>A - Select all patientsTX PVLS DENOMINATOR: TX PVLS Denominator</i> <i>A1 - Filter all
   * Pregnant Women from A</i> <i>A2 - Filter all Breastfeeding Women from A</i> <b>The following
   * disaggregations will be outputed</b>
   *
   * <ul>
   *   <li>14.1. % de adultos (15/+anos) em TARV com supressão viral - A and NOT A1 and NOT A2 and
   *       Age > 14
   *   <li>14.2.% de crianças (0-14 anos) em TARV com supressão viral - A and NOT A1 and NOT A2 and
   *       Age <= 14
   *   <li>14.3.% de MG em TARV com supressão viral - A and A1 and NOT A2
   *   <li>14.4. % de ML em TARV com supressão viral - A and NOT A1 and A2
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQ14DEN(Integer flag) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    switch (flag) {
      case 1:
        cd.setName("% de adultos (15/+anos) em TARV com supressão viral");
        break;
      case 2:
        cd.setName("% de crianças (0-14 anos) em TARV com supressão viral");
        break;
      case 3:
        cd.setName("% de MG em TARV com supressão viral");
        break;
      case 4:
        cd.setName("% de ML em TARV com supressão viral");
    }

    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addSearch(
        "A",
        EptsReportUtils.map(
            txPvls.getPatientsWithViralLoadResultsAndOnArtForMoreThan3Months(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "A1",
        EptsReportUtils.map(
            txPvls.getPatientsWhoArePregnantOrBreastfeedingBasedOnParameter(
                EptsReportConstants.PregnantOrBreastfeedingWomen.PREGNANTWOMEN),
            "onOrBefore=${endDate},location=${location}"));
    cd.addSearch(
        "A2",
        EptsReportUtils.map(
            txPvls.getPatientsWhoArePregnantOrBreastfeedingBasedOnParameter(
                EptsReportConstants.PregnantOrBreastfeedingWomen.BREASTFEEDINGWOMEN),
            "onOrBefore=${endDate},location=${location}"));
    if (flag == 1 || flag == 2) {
      cd.setCompositionString("A AND NOT (A1 OR A2)");
    } else if (flag == 3) {
      cd.setCompositionString("(A AND A1) AND NOT A2");
    } else if (flag == 4) {
      cd.setCompositionString("(A AND NOT A1) AND A2");
    }

    return cd;
  }

  /**
   * <b>MQ14Num: M&Q Report - Categoria 14 Numerator</b><br>
   * <i>B - Select all patientsTX PVLS NUMERATOR: TX PVLS NUMERATOR</i> <i>B1 - Filter all Pregnant
   * Women from B</i> <i>B2 - Filter all Breastfeeding Women from B</i> <b>The following
   * disaggregations will be outputed</b>
   *
   * <ul>
   *   <li>14.1. % de adultos (15/+anos) em TARV com supressão viral - B and NOT B1 and NOT B2 and
   *       Age > 14
   *   <li>14.2.% de crianças (0-14 anos) em TARV com supressão viral - B and NOT B1 and NOT B2 and
   *       Age <= 14
   *   <li>14.3.% de MG em TARV com supressão viral - B and B1 and NOT B2
   *   <li>14.4. % de ML em TARV com supressão viral - B and NOT B1 and B2
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQ14NUM(Integer flag) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    switch (flag) {
      case 1:
        cd.setName("% de adultos (15/+anos) em TARV com supressão viral");
        break;
      case 2:
        cd.setName("% de crianças (0-14 anos) em TARV com supressão viral");
        break;
      case 3:
        cd.setName("% de MG em TARV com supressão viral");
        break;
      case 4:
        cd.setName("% de ML em TARV com supressão viral");
    }

    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addSearch(
        "B",
        EptsReportUtils.map(
            txPvls.getPatientsWithViralLoadSuppressionWhoAreOnArtMoreThan3Months(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "B1",
        EptsReportUtils.map(
            txPvls.getPatientsWhoArePregnantOrBreastfeedingBasedOnParameter(
                EptsReportConstants.PregnantOrBreastfeedingWomen.PREGNANTWOMEN),
            "onOrBefore=${endDate},location=${location}"));
    cd.addSearch(
        "B2",
        EptsReportUtils.map(
            txPvls.getPatientsWhoArePregnantOrBreastfeedingBasedOnParameter(
                EptsReportConstants.PregnantOrBreastfeedingWomen.BREASTFEEDINGWOMEN),
            "onOrBefore=${endDate},location=${location}"));
    if (flag == 1 || flag == 2) {
      cd.setCompositionString("B AND NOT (B1 OR B2)");
    } else if (flag == 3) {
      cd.setCompositionString("(B AND B1) AND NOT B2");
    } else if (flag == 4) {
      cd.setCompositionString("(B AND NOT B1) AND B2");
    }

    return cd;
  }

  public CohortDefinition getMQ9Den(int flag) {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    switch (flag) {
      case 1:
        cd.setName(
            "% de adultos  HIV+ em TARV que tiveram conhecimento do resultado do primeiro CD4 dentro de 33 dias após a inscrição");
        break;
      case 2:
        cd.setName(
            "% de crianças HIV+ em TARV que tiveram conhecimento do resultado do primeiro CD4 dentro de 33 dias após a inscrição");
        break;
    }
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "A",
        EptsReportUtils.map(
            getMOHArtStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "C",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPregnantORBreastfeeding(
                commonMetadata.getPregnantConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "D",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPregnantORBreastfeeding(
                commonMetadata.getBreastfeeding().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "E",
        EptsReportUtils.map(
            QualityImprovement2020Queries.getTransferredInPatients(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
                hivMetadata.getPatientFoundYesConcept().getConceptId(),
                hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
                hivMetadata.getArtStatus().getConceptId()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "F",
        EptsReportUtils.map(
            commonCohortQueries.getTranferredOutPatients(),
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));
    cd.addSearch(
        "CHILDREN",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnMOHArtStartDate(0, 14, true),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));

    cd.addSearch(
        "ADULT",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnMOHArtStartDate(15, null, false),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));
    if (flag == 1) {
      cd.setCompositionString("A AND NOT (C OR D OR E OR F) AND ADULT");
    } else if (flag == 2) {
      cd.setCompositionString("A AND NOT (C OR D OR E OR F) AND CHILDREN");
    }

    return cd;
  }

  public CohortDefinition getMQ10Den(boolean adults) {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    if (adults) {
      cd.setName("Category 10 Denominator adults");
    } else {
      cd.setName("Category 10 Denominator children");
    }

    cd.addSearch(
        "A",
        EptsReportUtils.map(
            getMOHArtStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "C",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPregnantORBreastfeeding(
                commonMetadata.getPregnantConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "D",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPregnantORBreastfeeding(
                commonMetadata.getBreastfeeding().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "E",
        EptsReportUtils.map(
            QualityImprovement2020Queries.getTransferredInPatients(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
                hivMetadata.getPatientFoundYesConcept().getConceptId(),
                hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
                hivMetadata.getArtStatus().getConceptId()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "CHILDREN",
        EptsReportUtils.map(
            getChildrenCompositionMore19Months(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "ADULT",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnMOHArtStartDate(15, null, false),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));

    if (adults) {
      cd.setCompositionString("A AND NOT (C OR D OR E) AND ADULT");
    } else {
      cd.setCompositionString("A AND NOT (C OR D OR E) AND CHILDREN");
    }

    return cd;
  }

  private CohortDefinition getChildrenCompositionMore19Months() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "CHILDREN",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnMOHArtStartDate(0, 14, true),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));

    cd.addSearch(
        "BABIES",
        EptsReportUtils.map(
            genericCohortQueries.getAgeInMonthsOnArtStartDate(0, 20),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));

    cd.setCompositionString("CHILDREN AND NOT BABIES");

    return cd;
  }

  /**
   *
   *
   * <ul>
   *   <li>B: Filter all patients with CD4 (concept id 1695) result registered in Ficha Clinica
   *       (encounter type 6) withinunder 33 days from the first clinical consultation (encounter
   *       type 6) that occurred by endDateRevision, as following:
   *       <ul>
   *         <li>who have a clinical consultation (encounter type 6) with CD4 (concept id 1695)
   *             result (value numeric not null) and encounter_datetime > first clinical
   *             consultation (encounter type 6) encounter_datetime and <= first clinical
   *             consultation (encounter type 6) encounter_datetime+33 days.
   *       </ul>
   * </ul>
   *
   * @return
   */
  public CohortDefinition getBFromCategory9Numerator() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setQuery(
        "B: Filter all patients with CD4 within 33 days from the first clinical consultation");
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    String query =
        " SELECT p.patient_id  "
            + " FROM patient p  "
            + "    INNER JOIN       "
            + "            (  "
            + "                SELECT p.patient_id, MIN(e.encounter_datetime) AS e_encounter_datetime  "
            + "                FROM patient p  "
            + "                    INNER JOIN encounter e  "
            + "                        ON e.patient_id = p.patient_id  "
            + "                WHERE  "
            + "                    p.voided = 0  "
            + "                    AND e.voided = 0  "
            + "                    AND e.encounter_type  = ${6} "
            + "                    AND e.location_id = :location  "
            + "                GROUP BY p.patient_id  "
            + "            ) AS first_clinical_consultation  "
            + "        ON first_clinical_consultation.patient_id = p.patient_id  "
            + "         "
            + "    INNER JOIN  "
            + "            ( "
            + "                SELECT p.patient_id, e.encounter_datetime AS e_encounter_datetime  "
            + "                FROM patient p  "
            + "                    INNER JOIN encounter e  "
            + "                        ON e.patient_id = p.patient_id  "
            + "                    INNER JOIN obs o  "
            + "                        ON o.encounter_id = e.encounter_id "
            + "                WHERE p.voided = 0  "
            + "                    AND e.voided = 0  "
            + "                    AND o.voided = 0  "
            + "                    AND e.encounter_type  = ${6} "
            + "                    AND o.concept_id = ${1695} "
            + "                    AND o.value_numeric IS NOT NULL "
            + "                    AND e.encounter_datetime <= :revisionEndDate  "
            + "                    AND e.location_id = :location  "
            + "            ) AS cd4 "
            + "        ON cd4.patient_id =p.patient_id "
            + " WHERE  "
            + "    p.voided = 0  "
            + "    AND cd4.e_encounter_datetime > first_clinical_consultation.e_encounter_datetime "
            + "    AND cd4.e_encounter_datetime <= DATE_ADD(first_clinical_consultation.e_encounter_datetime, INTERVAL 33 DAY) ";

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getMQ9Num(int flag) {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    switch (flag) {
      case 1:
        cd.setName(
            "% de adultos  HIV+ em TARV que tiveram conhecimento do resultado do primeiro CD4 dentro de 33 dias após a inscrição");
        break;
      case 2:
        cd.setName(
            "% de crianças HIV+ em TARV que tiveram conhecimento do resultado do primeiro CD4 dentro de 33 dias após a inscrição");
        break;
    }
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "A",
        EptsReportUtils.map(
            getMOHArtStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "C",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPregnantORBreastfeeding(
                commonMetadata.getPregnantConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "D",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPregnantORBreastfeeding(
                commonMetadata.getBreastfeeding().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "E",
        EptsReportUtils.map(
            QualityImprovement2020Queries.getTransferredInPatients(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
                hivMetadata.getPatientFoundYesConcept().getConceptId(),
                hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
                hivMetadata.getArtStatus().getConceptId()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "F",
        EptsReportUtils.map(
            commonCohortQueries.getTranferredOutPatients(),
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));
    cd.addSearch("FEMALE", EptsReportUtils.map(genderCohortQueries.femaleCohort(), ""));
    cd.addSearch(
        "CHILDREN",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnMOHArtStartDate(0, 14, true),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));

    cd.addSearch(
        "ADULT",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnMOHArtStartDate(15, null, false),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));

    cd.addSearch(
        "B",
        EptsReportUtils.map(
            getBFromCategory9Numerator(),
            "revisionEndDate=${revisionEndDate},location=${location}"));

    if (flag == 1) {
      cd.setCompositionString("A AND B AND NOT (C OR D OR E OR F) AND ADULT");
    } else if (flag == 2) {
      cd.setCompositionString("A AND B AND NOT (C OR D OR E OR F) AND CHILDREN");
    }

    return cd;
  }

  /**
   * <b>M&Q Categoria 10 - Numerador</b>
   *
   * <ul>
   *   <li>10.1. % de adultos (15/+anos) que iniciaram o TARV dentro de 15 dias após diagnóstico - A
   *       AND F AND NOT (C OR D OR E) AND ADULTS
   *   <li>10.3. % de crianças (0-14 anos) HIV+ que iniciaram TARV dentro de 15 dias após
   *       diagnóstico - A AND F AND NOT (C OR D OR E) AND CHILDREN
   * </ul>
   */
  public CohortDefinition getMQ10NUM(int flag) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    switch (flag) {
      case 1:
        cd.setName(
            "% de adultos (15/+anos) que iniciaram o TARV dentro de 15 dias após diagnóstico");
        break;
      case 2:
        cd.setName(
            "% de crianças (0-14 anos) HIV+ que iniciaram TARV dentro de 15 dias após diagnóstico");
        break;
    }
    cd.addSearch(
        "A",
        EptsReportUtils.map(
            getMOHArtStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "C",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPregnantORBreastfeeding(
                commonMetadata.getPregnantConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "D",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPregnantORBreastfeeding(
                commonMetadata.getBreastfeeding().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "E",
        EptsReportUtils.map(
            QualityImprovement2020Queries.getTransferredInPatients(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
                hivMetadata.getPatientFoundYesConcept().getConceptId(),
                hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
                hivMetadata.getArtStatus().getConceptId()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "F",
        EptsReportUtils.map(
            genericCohortQueries.getArtDateMinusDiagnosisDate(),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));

    cd.addSearch(
        "CHILDREN",
        EptsReportUtils.map(
            getChildrenCompositionMore19Months(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "ADULT",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnMOHArtStartDate(15, null, false),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));
    if (flag == 1) {
      cd.setCompositionString("A AND F AND NOT (C OR D OR E) AND ADULT");
    } else if (flag == 3) {
      cd.setCompositionString("A AND F AND NOT (C OR D OR E) AND CHILDREN");
    }
    return cd;
  }

  public CohortDefinition getPregnantOrBreastfeedingWomen() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Pregnant or breastfeeding women");
    cd.addParameter(new Parameter("revisionEndDate", "End revision Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition pregnant =
        genericCohortQueries.hasCodedObs(
            commonMetadata.getPregnantConcept(),
            BaseObsCohortDefinition.TimeModifier.ANY,
            SetComparator.IN,
            Arrays.asList(hivMetadata.getAdultoSeguimentoEncounterType()),
            Arrays.asList(hivMetadata.getYesConcept()));

    CohortDefinition breastfeeding =
        genericCohortQueries.hasCodedObs(
            commonMetadata.getBreastfeeding(),
            BaseObsCohortDefinition.TimeModifier.ANY,
            SetComparator.IN,
            Arrays.asList(hivMetadata.getAdultoSeguimentoEncounterType()),
            Arrays.asList(hivMetadata.getYesConcept()));

    CohortDefinition women = genderCohortQueries.femaleCohort();

    cd.addSearch(
        "P",
        EptsReportUtils.map(
            pregnant,
            "onOrAfter=${revisionEndDate-14m},onOrBefore=${revisionEndDate},locationList=${location}"));
    cd.addSearch(
        "B",
        EptsReportUtils.map(
            breastfeeding,
            "onOrAfter=${revisionEndDate-14m},onOrBefore=${revisionEndDate},locationList=${location}"));
    cd.addSearch("F", EptsReportUtils.map(women, ""));
    cd.setCompositionString("(P OR B) AND F");
    return cd;
  }

  /**
   * Combined B13 for the CAT15 indicators Active patients excluding suspended, abandoned, dead and
   * transferout by end revision date
   */
  private CohortDefinition getCombinedB13ForCat15Indicators() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("B13 for the MQ CAT 15 indicators ");
    cd.addParameter(new Parameter("revisionEndDate", "End revision Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addSearch(
        "Active",
        EptsReportUtils.map(
            QualityImprovement2020Queries.getPatientsWithAtLeastAdrugPickup(
                hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId(),
                hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId(),
                hivMetadata.getArtDatePickupMasterCard().getConceptId(),
                hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId()),
            "endDate=${revisionEndDate},location{location}"));
    cd.addSearch(
        "suspended",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoSuspendedTreatmentB6(false),
            "onOrBefore=${revisionEndDate},location=${location}"));
    cd.addSearch(
        "abandoned",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getNumberOfPatientsWhoAbandonedArtDuringPreviousMonthForB7(),
            "date=${revisionEndDate},location=${location}"));
    cd.addSearch(
        "dead",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoDied(false),
            "onOrBefore=${revisionEndDate},locationList=${location}"));
    cd.addSearch(
        "TO",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsTransferredOutB5(true),
            "onOrBefore=${revisionEndDate},location=${location}"));
    cd.setCompositionString("Active AND NOT (suspended OR abandoned OR dead OR TO)");

    return cd;
  }

  private CohortDefinition getAgeOnObsDatetime(Integer minAge, Integer maxAge) {
    CalculationCohortDefinition cd =
        new CalculationCohortDefinition(
            Context.getRegisteredComponents(AgeOnObsDatetimeCalculation.class).get(0));
    cd.setName("Calculate Age based on ObsDatetime");
    cd.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addCalculationParameter("minAgeOnObsDatetime", minAge);
    cd.addCalculationParameter("maxAgeOnObsDatetime", maxAge);
    return cd;
  }

  public CohortDefinition getB4And2() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("B4_2");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6128", hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId());

    String query =
        ""
            + "SELECT  final.patient_id "
            + "FROM "
            + "( "
            + "   SELECT p.patient_id, MAX(o.value_datetime) last_encounter "
            + "   FROM patient p "
            + "         INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "         INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "   WHERE p.voided = 0 "
            + "     AND e.voided = 0 "
            + "     AND o.voided = 0 "
            + "     AND o.concept_id = ${6128} "
            + "     AND e.encounter_type = ${53} "
            + "     AND e.location_id = :location "
            + "     AND o.value_datetime between :startDate AND :endDate "
            + "   GROUP BY p.patient_id "
            + ") AS  final";

    StringSubstitutor sb = new StringSubstitutor(map);
    cd.setQuery(sb.replace(query));
    return cd;
  }

  public CohortDefinition getB4And1() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("B4_1");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6122", hivMetadata.getIsoniazidUsageConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());

    String query =
        ""
            + "SELECT  final.patient_id "
            + "FROM"
            + "( "
            + "   SELECT p.patient_id, MAX(e.encounter_datetime) last_encounter "
            + "   FROM patient p "
            + "         INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "         INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "   WHERE p.voided = 0 "
            + "     AND e.voided = 0 "
            + "     AND o.voided = 0 "
            + "     AND o.concept_id = ${6122} "
            + "     AND o.value_coded = ${1256} "
            + "     AND e.encounter_type = ${6} "
            + "     AND e.location_id = :location "
            + "     AND e.encounter_datetime between :startDate AND :endDate "
            + "   GROUP BY p.patient_id "
            + ") AS  final";

    StringSubstitutor sb = new StringSubstitutor(map);
    cd.setQuery(sb.replace(query));
    return cd;
  }

  public CohortDefinition getGNew() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("G new ");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6122", hivMetadata.getIsoniazidUsageConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("6128", hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId());
    map.put("6129", hivMetadata.getDataFinalizacaoProfilaxiaIsoniazidaConcept().getConceptId());
    map.put("1267", hivMetadata.getCompletedConcept().getConceptId());

    String query =
        ""
            + "SELECT p.patient_id  "
            + "FROM patient p  "
            + "    INNER JOIN (  "
            + "                SELECT tpt_end.patient_id, MAX(tpt_end.last_encounter) AS tpt_end_date  "
            + "                FROM (  "
            + "                         SELECT p.patient_id, MAX(o.value_datetime) last_encounter  "
            + "                         FROM patient p  "
            + "                                  INNER JOIN encounter e ON p.patient_id = e.patient_id  "
            + "                                  INNER JOIN obs o ON o.encounter_id = e.encounter_id  "
            + "                         WHERE p.voided = 0  "
            + "                           AND e.voided = 0  "
            + "                           AND o.voided = 0  "
            + "                           AND o.concept_id = ${6129}  "
            + "                           AND e.encounter_type = ${53}  "
            + "                           AND e.location_id = :location  "
            + "                           AND o.value_datetime BETWEEN :startDate AND :revisionEndDate  "
            + "                         GROUP BY p.patient_id  "
            + "                         UNION  "
            + "                         SELECT p.patient_id, MAX(e.encounter_datetime) last_encounter  "
            + "                         FROM patient p  "
            + "                                  INNER JOIN encounter e ON p.patient_id = e.patient_id  "
            + "                                  INNER JOIN obs o ON o.encounter_id = e.encounter_id  "
            + "                         WHERE p.voided = 0  "
            + "                           AND e.voided = 0  "
            + "                           AND o.voided = 0  "
            + "                           AND o.concept_id = ${6122}  "
            + "                           AND o.value_coded = ${1267}  "
            + "                           AND e.encounter_type = ${6}  "
            + "                           AND e.location_id = :location  "
            + "                           AND e.encounter_datetime BETWEEN :startDate AND :revisionEndDate  "
            + "                         GROUP BY p.patient_id  "
            + "                     ) AS tpt_end  "
            + "                GROUP BY  tpt_end.patient_id  "
            + "            ) AS tpt_fim ON tpt_fim.patient_id = p.patient_id  "
            + "    INNER JOIN (  "
            + "                SELECT tpt_start.patient_id, MAX(tpt_start.last_encounter) tpt_start_date  "
            + "                FROM (  "
            + "                         SELECT p.patient_id, MAX(o.value_datetime) last_encounter  "
            + "                         FROM patient p  "
            + "                                  INNER JOIN encounter e ON p.patient_id = e.patient_id  "
            + "                                  INNER JOIN obs o ON o.encounter_id = e.encounter_id  "
            + "                         WHERE p.voided = 0  "
            + "                           AND e.voided = 0  "
            + "                           AND o.voided = 0  "
            + "                           AND o.concept_id = ${6128}  "
            + "                           AND e.encounter_type = ${53}  "
            + "                           AND e.location_id = :location  "
            + "                           AND o.value_datetime BETWEEN :startDate AND :endDate  "
            + "                         GROUP BY p.patient_id  "
            + "                         UNION  "
            + "                         SELECT p.patient_id, MAX(e.encounter_datetime) last_encounter  "
            + "                         FROM patient p  "
            + "                                  INNER JOIN encounter e ON p.patient_id = e.patient_id  "
            + "                                  INNER JOIN obs o ON o.encounter_id = e.encounter_id  "
            + "                         WHERE p.voided = 0  "
            + "                           AND e.voided = 0  "
            + "                           AND o.voided = 0  "
            + "                           AND o.concept_id = ${6122}  "
            + "                           AND o.value_coded = ${1256}  "
            + "                           AND e.encounter_type = ${6}  "
            + "                           AND e.location_id = :location  "
            + "                           AND e.encounter_datetime BETWEEN :startDate AND :endDate  "
            + "                         GROUP BY p.patient_id  "
            + "                     ) AS tpt_start  "
            + "                GROUP BY tpt_start.patient_id  "
            + "            ) AS tpt_inicio ON tpt_inicio.patient_id = p.patient_id  "
            + "WHERE p.voided = 0  "
            + "    AND TIMESTAMPDIFF(DAY, tpt_inicio.tpt_start_date,tpt_fim.tpt_end_date) BETWEEN  170 AND 297 ";

    StringSubstitutor sb = new StringSubstitutor(map);
    cd.setQuery(sb.replace(query));
    return cd;
  }

  private CohortDefinition getMQC13P3NUM_J() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("All patients with HIV Carga Viral - J");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getHistoricalDrugStartDateConcept().getConceptId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String query =
        " SELECT p.patient_id "
            + " FROM   patient p "
            + "       INNER JOIN encounter e  "
            + "               ON e.patient_id = p.patient_id  "
            + "       INNER JOIN obs o  "
            + "               ON o.encounter_id = e.encounter_id  "
            + "       INNER JOIN(SELECT patient_id, "
            + "                         art_date  "
            + "                  FROM   (SELECT p.patient_id, "
            + "                                 Min(value_datetime) art_date  "
            + "                          FROM   patient p "
            + "                                 INNER JOIN encounter e  "
            + "                                         ON p.patient_id = e.patient_id  "
            + "                                 INNER JOIN obs o  "
            + "                                         ON e.encounter_id = o.encounter_id  "
            + "                          WHERE  p.voided = 0  "
            + "                                 AND e.voided = 0  "
            + "                                 AND o.voided = 0  "
            + "                                 AND e.encounter_type = ${53} "
            + "                                 AND o.concept_id = ${1190} "
            + "                                 AND o.value_datetime IS NOT NULL  "
            + "                                 AND o.value_datetime <= :endDate  "
            + "                                 AND e.location_id = :location "
            + "                          GROUP  BY p.patient_id) union_tbl  "
            + "                  WHERE  union_tbl.art_date BETWEEN  "
            + "                         :startDate AND :endDate) AS "
            + "                                 tabela  "
            + "               ON tabela.patient_id = p.patient_id "
            + " WHERE  p.voided = 0  "
            + "       AND e.voided = 0  "
            + "       AND o.voided = 0  "
            + "       AND e.encounter_type = ${53} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${856}  "
            + "       AND o.value_numeric IS NOT NULL "
            + "       AND o.obs_datetime BETWEEN Date_add(tabela.art_date, INTERVAL 6 MONTH)  "
            + "                                  AND  "
            + "                                      Date_add(tabela.art_date, INTERVAL 9 MONTH "
            + "                                      )  "
            + " GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  private CohortDefinition getMQC13P3NUM_K() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("All patients with HIV Carga Viral - K");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("21190", commonMetadata.getRegimenAlternativeToFirstLineConcept().getConceptId());
    map.put("21151", hivMetadata.getTherapeuticLineConcept().getConceptId());
    map.put("21150", hivMetadata.getFirstLineConcept().getConceptId());
    map.put("1792", hivMetadata.getJustificativeToChangeArvTreatment().getConceptId());
    map.put("1982", commonMetadata.getPregnantConcept().getConceptId());

    String query =
        " SELECT   "
            + "    p.patient_id "
            + " FROM "
            + "    patient p  "
            + "        INNER JOIN "
            + "    encounter e ON e.patient_id = p.patient_id "
            + "        INNER JOIN "
            + "    obs o ON o.encounter_id = e.encounter_id   "
            + "    INNER JOIN(SELECT patient_id, regime_date  "
            + "            FROM   (SELECT p.patient_id,   "
            + "                           Max(o.obs_datetime) AS regime_date  "
            + "                    FROM   patient p   "
            + "                           JOIN encounter e  "
            + "                             ON e.patient_id = p.patient_id  "
            + "                           JOIN obs o  "
            + "                             ON o.encounter_id = e.encounter_id  "
            + "                           JOIN obs o2   "
            + "                             ON o2.encounter_id = e.encounter_id   "
            + "                    WHERE  e.encounter_type = ${53}   "
            + "                           AND o.concept_id = ${21190}  "
            + "                           AND o.value_coded IS NOT NULL   "
            + "                           AND e.location_id = :location   "
            + "                           AND (   "
            + "                                  (o2.concept_id = ${1792} AND o2.value_coded <> ${1982})  "
            + "                                   OR  "
            + "                                  (o2.concept_id = ${1792} AND o2.value_coded IS NULL)  "
            + "                                   OR  "
            + "                                  (  "
            + "                                   NOT EXISTS (  "
            + "                                           SELECT * FROM obs oo  "
            + "                                           WHERE oo.voided = 0   "
            + "                                           AND oo.encounter_id = e.encounter_id  "
            + "                                           AND oo.concept_id = ${1792}  "
            + "                                       )   "
            + "                                 )   "
            + "                                )  "
            + "                           AND e.voided = 0  "
            + "                           AND p.voided = 0  "
            + "                           AND o.voided = 0  "
            + "                           AND o2.voided = 0 "
            + "                           AND o.obs_datetime BETWEEN :startDate AND :endDate  "
            + "                    GROUP  BY p.patient_id) bI1  "
            + "            WHERE  bI1.patient_id NOT IN (SELECT p.patient_id  "
            + "                                          FROM   patient p   "
            + "                                                 JOIN encounter e  "
            + "                                                   ON e.patient_id = p.patient_id  "
            + "                                                 JOIN obs o  "
            + "                                                   ON o.encounter_id = e.encounter_id  "
            + "                                          WHERE  e.encounter_type = ${6}  "
            + "                                                 AND o.concept_id = ${21151} AND o.value_coded <> ${21150}   "
            + "                                                 AND e.location_id = :location   "
            + "                                                 AND e.voided = 0  "
            + "                                                 AND p.voided = 0  "
            + "                                                 AND e.encounter_datetime > bI1.regime_date  "
            + "                                                 AND e.encounter_datetime <= :revisionEndDate))  "
            + "                    AS B1 ON B1.patient_id = p.patient_id  "
            + "                    WHERE  "
            + "                    p.voided = 0   "
            + "                        AND e.voided = 0   "
            + "                        AND o.voided = 0   "
            + "                        AND e.encounter_type = ${53}  "
            + "                        AND e.location_id = :location  "
            + "                        AND o.concept_id = ${856} "
            + "                        AND o.value_numeric IS NOT NULL  "
            + "                        AND o.obs_datetime BETWEEN DATE_ADD(B1.regime_date, INTERVAL 6 MONTH)  "
            + "                        AND DATE_ADD(B1.regime_date, INTERVAL 9 MONTH) "
            + "                        GROUP BY p.patient_id  ";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  private CohortDefinition getMQC13P3NUM_L() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("All patients with HIV Carga Viral - L");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1792", hivMetadata.getJustificativeToChangeArvTreatment().getConceptId());
    map.put("1982", commonMetadata.getPregnantConcept().getConceptId());
    map.put("21187", hivMetadata.getRegArvSecondLine().getConceptId());

    String query =
        " SELECT   "
            + "    p.patient_id "
            + " FROM "
            + "    patient p  "
            + "        INNER JOIN "
            + "    encounter e ON e.patient_id = p.patient_id "
            + "        INNER JOIN "
            + "    obs o ON o.encounter_id = e.encounter_id   "
            + "    INNER JOIN(SELECT p.patient_id, Max(e.encounter_datetime) AS last_consultation "
            + "              FROM patient p   "
            + "                   INNER JOIN encounter e ON e.patient_id = p.patient_id   "
            + "                   INNER JOIN obs o ON o.encounter_id = e.encounter_id   "
            + "                   INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id   "
            + "              WHERE e.voided = 0 AND p.voided = 0  "
            + "                AND o.voided = 0   "
            + "                AND o2.voided = 0  "
            + "                AND e.encounter_type = ${53}  "
            + "                AND e.location_id = :location  "
            + "                AND (o.concept_id = ${21187} AND o.value_coded IS NOT NULL)   "
            + "                           AND (   "
            + "                                  (o2.concept_id = ${1792} AND o2.value_coded <> ${1982})  "
            + "                                   OR  "
            + "                                  (o2.concept_id = ${1792} AND o2.value_coded IS NULL)  "
            + "                                   OR  "
            + "                                  (  "
            + "                                   NOT EXISTS (  "
            + "                                           SELECT * FROM obs oo  "
            + "                                           WHERE oo.voided = 0   "
            + "                                           AND oo.encounter_id = e.encounter_id  "
            + "                                           AND oo.concept_id = ${1792}  "
            + "                                       )   "
            + "                                 )   "
            + "                                )  "
            + "                AND o.obs_datetime >= :startDate   "
            + "                AND o.obs_datetime <= :endDate   "
            + "              GROUP BY p.patient_id)   "
            + "                    AS B2NEW ON B2NEW.patient_id = p.patient_id  "
            + "                    WHERE  "
            + "                    p.voided = 0   "
            + "                        AND e.voided = 0   "
            + "                        AND o.voided = 0   "
            + "                        AND e.encounter_type = ${53}  "
            + "                        AND e.location_id = :location  "
            + "                        AND o.concept_id = ${856} "
            + "                        AND o.value_numeric IS NOT NULL  "
            + "                        AND o.obs_datetime BETWEEN DATE_ADD(B2NEW.last_consultation, INTERVAL 6 MONTH)   "
            + "                        AND DATE_ADD(B2NEW.last_consultation, INTERVAL 9 MONTH)  "
            + "                        GROUP BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }
}
