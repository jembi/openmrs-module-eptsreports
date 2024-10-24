package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.eptsreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.eptsreports.reporting.calculation.generic.AgeOnObsDatetimeCalculation;
import org.openmrs.module.eptsreports.reporting.calculation.melhoriaQualidade.ConsultationUntilEndDateAfterStartingART;
import org.openmrs.module.eptsreports.reporting.cohort.definition.CalculationCohortDefinition;
import org.openmrs.module.eptsreports.reporting.library.queries.QualityImprovement2020Queries;
import org.openmrs.module.eptsreports.reporting.library.queries.ViralLoadQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsQueriesUtil;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportConstants;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportConstants.MIMQ;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
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

  private TxCurrCohortQueries txCurrCohortQueries;

  private EriDSDCohortQueries eriDSDCohortQueries;

  private IntensiveMonitoringCohortQueries intensiveMonitoringCohortQueries;

  private final String MAPPING = "startDate=${startDate},endDate=${endDate},location=${location}";
  private final String MAPPING1 =
      "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}";
  private final String MAPPING2 =
      "startDate=${revisionEndDate-14m},endDate=${revisionEndDate-11m},location=${location}";
  private final String MAPPING3 =
      "startDate=${startDate},endDate=${revisionEndDate},location=${location}";
  private String MAPPING4 =
      "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},location=${location}";
  private final String MAPPING5 =
      "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate},revisionEndDate=${revisionEndDate},location=${location}";
  private final String MAPPING6 =
      "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate},location=${location}";
  private final String MAPPING7 =
      "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate},location=${location}";
  private final String MAPPING8 =
      "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},location=${location}";
  private final String MAPPING9 =
      "startDate=${revisionEndDate-4m+1d},endDate=${revisionEndDate},revisionEndDate=${revisionEndDate},location=${location}";
  private final String MAPPING10 =
      "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}";
  private final String MAPPING11 =
      "revisionStartDate=${startDate},revisionEndDate=${revisionEndDate},location=${location}";
  private final String MAPPING12 =
      "revisionStartDate=${revisionEndDate-5m+1d},revisionEndDate=${revisionEndDate},location=${location}";
  private final String MAPPING13 =
      "revisionStartDate=${revisionEndDate-4m+1d},revisionEndDate=${revisionEndDate},location=${location}";
  private final String MAPPING14 = "revisionEndDate=${revisionEndDate},location=${location}";

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
      TxMlCohortQueries txMlCohortQueries,
      TxCurrCohortQueries txCurrCohortQueries,
      EriDSDCohortQueries eriDSDCohortQueries) {
    this.genericCohortQueries = genericCohortQueries;
    this.commonMetadata = commonMetadata;
    this.hivMetadata = hivMetadata;
    this.genderCohortQueries = genderCohortQueries;
    this.ageCohortQueries = ageCohortQueries;
    this.resumoMensalCohortQueries = resumoMensalCohortQueries;
    this.commonCohortQueries = commonCohortQueries;
    this.tbMetadata = tbMetadata;
    this.txPvls = txPvls;
    this.txMlCohortQueries = txMlCohortQueries;
    this.txCurrCohortQueries = txCurrCohortQueries;
    this.eriDSDCohortQueries = eriDSDCohortQueries;
  }

  public void setIntensiveMonitoringCohortQueries(
      IntensiveMonitoringCohortQueries intensiveMonitoringCohortQueries) {
    this.intensiveMonitoringCohortQueries = intensiveMonitoringCohortQueries;
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
   *     <li><strong>Should</strong> Returns empty if there is no patient who meets the conditions
   *     <li><strong>Should</strong> fetch all patients who initiated ART during the inclusion
   *         period
   */
  public SqlCohortDefinition getMOHArtStartDate() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" All patients that started ART during inclusion period ");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
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
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    compositionCohortDefinition.addSearch(
        "CAT3DEN", EptsReportUtils.map(this.getMQC3D1(), MAPPING));

    compositionCohortDefinition.addSearch(
        "C", EptsReportUtils.map(this.getCFromMQC3N1(), "location=${location}"));
    compositionCohortDefinition.addSearch(
        "D", EptsReportUtils.map(this.getDFromMQC3N1(), "location=${location},endDate=${endDate}"));

    compositionCohortDefinition.setCompositionString("CAT3DEN AND (C OR D)");

    return compositionCohortDefinition;
  }

  /**
   * /**
   * <li>D: Filter all child patients who have the first clinical consultation between Diagnosis
   *     Date and Diagnosis Date+7days as following:
   *
   *     <ul>
   *       <li>all patients who have: Filter all adults patients who have the first clinical
   *           consultation between Diagnosis Date and Diagnosis Date+7days as following:ve who have
   *           the first consultation registered in “Ficha Clinica” (encounter type 6) after “Data
   *           Diagnostico” with the following conditions:
   *           <ul>
   *             <li>
   *                 <p>[encounter type 6] “Data da consulta” (encounter datetime) >= [encounter
   *                 type 53] “Data Diagnóstico” (concept_id 22772 obs datetime) and <= [encounter
   *                 type 53] “Data Diagnóstico” (concept_id 22772 obs datetime) + 7 days
   *           </ul>
   *       <li>And the first consultation [encounter type 6] “Data da consulta” (encounter datetime)
   *           >= [encounter type 53] the oldest “Data Diagnóstico” minus the oldest “Data
   *           Diagnóstico” is >=0 and <=7 days
   *     </ul>
   *
   * @return CohortDefinition
   */
  private CohortDefinition getCFromMQC3N1() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName(
        "Melhoria de Qualidade Category 3 Numerator C part composition");

    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    compositionCohortDefinition.addSearch(
        "C", EptsReportUtils.map(getCqueryFromCat3(), "location=${location}"));

    compositionCohortDefinition.setCompositionString("C");

    return compositionCohortDefinition;
  }

  /**
   *
   * <li>D: Filter all child patients who have the first clinical consultation between Diagnosis
   *     Date and Diagnosis Date+7days as following:
   *
   *     <ul>
   *       <li>all patients who have the first consultation registered in “Ficha Clinica” (encounter
   *           type 6) after “Data Diagnostico” as following:
   *           <ul>
   *             <li>
   *                 <p>Select the oldest date between “Data Diagnóstico” (concept_id 23807 obs
   *                 datetime, encounter type 53) and “Data Diagnóstico Presuntivo” (concept_id
   *                 22772 obs datetime, encounter type 53)
   *                 <p>(Note: if “Data Diagnóstico” is empty then consider “Data Diagnóstico
   *                 Presuntivo” if it exists. If “Data Diagnostico” is empty then consider “Data
   *                 Diagnostico Presuntivo” if it exists).
   *                 <ul>
   *                   <li>
   *                       <p>And the first consultation [encounter type 6] “Data da consulta”
   *                       (encounter datetime) >= [encounter type 53] the oldest “Data Diagnóstico”
   *                       minus the oldest “Data Diagnóstico” is >=0 and <=7 days
   *                 </ul>
   *           </ul>
   *       <li>
   *     </ul>
   *
   * @return CohortDefinition
   */
  private CohortDefinition getDFromMQC3N1() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName(
        "Melhoria de Qualidade Category 3 Numerator C part composition");
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    compositionCohortDefinition.addSearch(
        "D", EptsReportUtils.map(getDqueryFromCat3(), "endDate=${endDate},location=${location}"));

    compositionCohortDefinition.setCompositionString("D");

    return compositionCohortDefinition;
  }

  /**
   * /**
   * <li>D: Filter all child patients who have the first clinical consultation between Diagnosis
   *     Date and Diagnosis Date+7days as following:
   *
   *     <ul>
   *       <li>all patients who have: Filter all adults patients who have the first clinical
   *           consultation between Diagnosis Date and Diagnosis Date+7days as following:ve who have
   *           the first consultation registered in “Ficha Clinica” (encounter type 6) after “Data
   *           Diagnostico” with the following conditions:
   *           <ul>
   *             <li>
   *                 <p>[encounter type 6] “Data da consulta” (encounter datetime) >= [encounter
   *                 type 53] “Data Diagnóstico” (concept_id 22772 obs datetime) and <= [encounter
   *                 type 53] “Data Diagnóstico” (concept_id 22772 obs datetime) + 7 days
   *           </ul>
   *     </ul>
   *
   * @return CohortDefinition
   */
  private CohortDefinition getCqueryFromCat3() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

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
            + "    AND  first_consultation.encounterdatetime BETWEEN diagnostico.data_diagnostico "
            + "     AND DATE_ADD(diagnostico.data_diagnostico, INTERVAL 7 DAY) ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   *
   * <li>D: Filter all child patients who have the first clinical consultation between Diagnosis
   *     Date and Diagnosis Date+7days as following:
   *
   *     <ul>
   *       <li>all patients who have the first consultation registered in “Ficha Clinica” (encounter
   *           type 6) after “Data Diagnostico” as following:
   *           <ul>
   *             <li>
   *                 <p>Select the oldest date between “Data Diagnóstico” (concept_id 23807 obs
   *                 datetime, encounter type 53) and “Data Diagnóstico Presuntivo” (concept_id
   *                 22772 obs datetime, encounter type 53)
   *                 <p>(Note: if “Data Diagnóstico” is empty then consider “Data Diagnóstico
   *                 Presuntivo” if it exists. If “Data Diagnostico” is empty then consider “Data
   *                 Diagnostico Presuntivo” if it exists).
   *           </ul>
   *       <li>And the first consultation [encounter type 6] “Data da consulta” (encounter datetime)
   *           >= [encounter type 53] the oldest “Data Diagnóstico” minus the oldest “Data
   *           Diagnóstico” is >=0 and <=7 days
   *     </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getDqueryFromCat3() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("22772", hivMetadata.getTypeTestHIVConcept().getConceptId());
    map.put("23807", hivMetadata.getPresumptiveDiagnosisInChildrenConcep().getConceptId());

    String query =
        "SELECT enc_6.patient_id AS patient_id FROM( "
            + "  SELECT pa.patient_id AS patient_id, MIN(ee.encounter_datetime) AS encounter_date FROM patient pa  "
            + "  INNER JOIN encounter ee ON ee.patient_id=pa.patient_id "
            + "  WHERE ee.encounter_type = ${6} "
            + "  AND ee.location_id= :location AND ee.encounter_datetime <=:endDate "
            + " GROUP BY pa.patient_id) enc_6 "
            + " INNER JOIN "
            + "  (SELECT p.patient_id AS patient_id, MIN(o.obs_datetime) AS obs_datetime FROM patient p "
            + "  INNER JOIN encounter e ON e.patient_id=p.patient_id "
            + "  INNER JOIN obs o ON e.encounter_id=o.encounter_id "
            + "  WHERE e.encounter_type = ${53} AND o.concept_id IN ( ${23807}, ${22772}) "
            + "  AND e.location_id= :location GROUP BY p.patient_id) en_53 "
            + "  ON en_53.patient_id=enc_6.patient_id "
            + "  WHERE enc_6.encounter_date BETWEEN en_53.obs_datetime AND DATE_ADD(en_53.obs_datetime, INTERVAL 7 DAY)";

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

  /**
   *
   *
   * <ul>
   *   <li>Select all female patients who are pregnant as following:
   *       <ul>
   *         <li>All patients registered in Ficha Clínica (encounter type=53) with
   *             “Gestante”(concept_id 1982) value coded equal to “Yes” (concept_id 1065) and
   *             encounter datetime >= startDateRevision and <=endDateInclusion and sex=Female
   *       </ul>
   *   <li>Select all female patients who are breastfeeding as following:
   *       <ul>
   *         <li>all patients registered in Ficha Clinica (encounter type=53) with
   *             “Lactante”(concept_id 6332) value coded equal to “Yes” (concept_id 1065) and
   *             encounter datetime >= startDateRevision and <=endDateInclusion and sex=Female
   *       </ul>
   * </ul>
   *
   * @param conceptIdQn The Obs quetion concept
   * @param conceptIdAns The value coded answers concept
   * @return CohortDefinition
   */
  // This will bypass the previous implementation and allow reuse of the method with other
  // parameters
  public CohortDefinition getPregnantAndBreastfeedingStates(int conceptIdQn, int conceptIdAns) {

    return getPregnantAndBreastfeedingStates(
        hivMetadata.getMasterCardEncounterType(), conceptIdQn, conceptIdAns);
  }

  public CohortDefinition getPregnantAndBreastfeedingStates(
      EncounterType encounterType, int conceptIdQn, int conceptIdAns) {
    Map<String, Integer> map = new HashMap<>();
    map.put("conceptIdQn", conceptIdQn);
    map.put("conceptIdAns", conceptIdAns);
    map.put("fichaClinicaEncounterType", encounterType.getEncounterTypeId());
    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    String query =
        "SELECT "
            + "   p.patient_id "
            + "   FROM "
            + "   patient p "
            + " inner join person pe on pe.person_id=p.patient_id "
            + "   INNER JOIN "
            + "      encounter e "
            + "      ON p.patient_id = e.patient_id "
            + "   INNER JOIN "
            + "      obs o "
            + "      ON o.encounter_id = e.encounter_id "
            + "  WHERE "
            + "   p.voided = 0 "
            + " and pe.gender='F'"
            + "   AND e.voided = 0 "
            + "   AND o.voided = 0 "
            + "   AND e.encounter_type = ${fichaClinicaEncounterType} "
            + "   AND o.concept_id = ${conceptIdQn} "
            + "   AND o.value_coded = ${conceptIdAns} "
            + "   AND e.encounter_datetime BETWEEN :startDate AND :endDate";

    return genericCohortQueries.generalSql(
        "Pregnant or breastfeeding females", stringSubstitutor.replace(query));
  }

  /**
   * <b>MQ4NUN1</b>: Melhoria de Qualidade Category 4 Numerador 1 <br>
   * <i> Denominador and E </i> <br>
   * <li>Select all patients from Categoria 4 Denominador
   *
   *     <ul>
   *       <li>E: Filter all patients who on their last clinical consultation registered in “Ficha
   *           Clinica” (encounter type 6) during the revision period (encounter datetime>=
   *           startDateInclusion and <=EndDateRevision), have “CLASSIFICAÇÃO DE DESNUTRIÇÃO”
   *           (concept_id 6336) value coded “Normal/Ligeira/DAM/ DAG”(concept_ids 1115, 6335, 68,
   *           1844).
   *     </ul>
   *
   * @return CohortDefinition
   */
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

  /**
   * <b>MQ4NUN2</b>: Melhoria de Qualidade Category 4 Numerador 2 <br>
   * <i> Denominador and E </i> <br>
   * <li>Select all patients from Categoria 4 Denominador
   *
   *     <ul>
   *       <li>E: Filter all patients who on their last clinical consultation registered in “Ficha
   *           Clinica” (encounter type 6) during the revision period (encounter datetime>=
   *           startDateInclusion and <=EndDateRevision), have “CLASSIFICAÇÃO DE DESNUTRIÇÃO”
   *           (concept_id 6336) value coded “Normal/Ligeira/DAM/ DAG”(concept_ids 1115, 6335, 68,
   *           1844).
   *     </ul>
   *
   * @return CohortDefinition
   */
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

  /**
   *
   *
   * <ul>
   *   <li>E: Filter all patients who on their last clinical consultation registered in “Ficha
   *       Clinica” (encounter type 6) during the revision period (encounter datetime>=
   *       startDateInclusionRevision and <=EndDateRevisionendDateInclusion), have “CLASSIFICAÇÃO DE
   *       DESNUTRIÇÃO” (concept_id 6336) value coded “Normal/Ligeira/DAM/ DAG”(concept_ids 1115,
   *       6335, 68, 1844).
   * </ul>
   *
   * @return CohortDefinition
   */
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

  /**
   *
   *
   * <ul>
   *   <li>F - Filter all patients with “Apoio/Educação Nutricional” equals to “ATPU” or “SOJA” in
   *       the same clinical consultation where“Grau da Avaliação Nutricional” equals to “DAM” or
   *       “DAG” during the revision period, clinical consultation >= startDateRevision and
   *       <=endDateRevision :
   *       <ul>
   *         <li>
   *             <p>All patients registered in Ficha Clinica (encounter type=6) with “Apoio/Educação
   *             Nutricional” (concept_id = 2152) and value_coded equal to “ATPU” (concept_id =
   *             6143) or equal to “SOJA” (concept_id = 2151) during the encounter_datetime >=
   *             startDateRevision and <=endDateInclusion
   *             <p>Note: the clinical consultation with “Apoio/Educação Nutricional” = “ATPU” or
   *             “SOJA” must be the same clinical consultation where “Grau da Avaliação Nutricional”
   *             equals to “DAM” or “DAG”. The first consultation with “Grau da Avaliação
   *             Nutricional” equals to “DAM” or “DAG” should be considered.
   *       </ul>
   * </ul>
   *
   * @return CohortDefinition
   *     <li><strong>Should</strong> Returns empty if there is no patient who meets the conditions
   *     <li><strong>Should</strong> fetch all patients with F criteria
   */
  public CohortDefinition getPatientsWithNutritionalStateAndNutritionalSupport() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients with Nutritional Classification");
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

  /**
   * All patients ll patients with a clinical consultation(encounter type 6) during the Revision
   * period with the following conditions:
   *
   * <p>“PROFILAXIA = INH” (concept_id 23985 value 656) and “Data Fim” (concept_id 165308 value
   * 1267) Encounter_datetime between startDateRevision and endDateRevision and:
   *
   * <p>- Obs_datetime(from the last clinical consultation with “PROFILAXIA = INH” (concept_id 23985
   * value 656) and “Data Fim” (concept_id 165308 value 1267)) MINUS - Obs_datetime (from the
   * clinical consultation with “PROFILAXIA = * INH” (concept_id 23985 value 656) and “Data Fim”
   * (concept_id 165308 value 1267) between 6 months and 9 months
   *
   * @return CohortDefinition
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
    map.put("23985", tbMetadata.getRegimeTPTConcept().getConceptId());
    map.put("656", tbMetadata.getIsoniazidConcept().getConceptId());
    map.put("165308", tbMetadata.getDataEstadoDaProfilaxiaConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugsConcept().getConceptId());
    map.put("1267", hivMetadata.getCompletedConcept().getConceptId());

    String query =
        " SELECT  p.patient_id   "
            + " FROM  patient p   "
            + "    INNER JOIN   "
            + "            ( "
            + "                SELECT p.patient_id, MAX(o2.obs_datetime) AS e_datetime   "
            + "                FROM patient p   "
            + "                    INNER JOIN encounter e ON p.patient_id = e.patient_id   "
            + "                    INNER JOIN obs o ON e.encounter_id = o.encounter_id   "
            + "                    INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id   "
            + "                WHERE   "
            + "                    e.location_id = :location   "
            + "                        AND e.encounter_type = ${6}   "
            + "                        AND ((o.concept_id = ${23985}   "
            + "                        AND o.value_coded = ${656})   "
            + "                        AND (o2.concept_id = ${165308}   "
            + "                        AND o2.value_coded = ${1256}))   "
            + "                        AND o2.obs_datetime >= :startDate   "
            + "                        AND o2.obs_datetime <= :endDate   "
            + "                        AND p.voided = 0   "
            + "                        AND e.voided = 0   "
            + "                        AND o.voided = 0   "
            + "                        AND o2.voided = 0   "
            + "                GROUP BY p.patient_id "
            + "            ) AS b4 ON p.patient_id = b4.patient_id   "
            + "    INNER JOIN   "
            + "            ( "
            + "                SELECT  p.patient_id, MAX(o2.obs_datetime) AS e_datetime   "
            + "                FROM  patient p   "
            + "                    INNER JOIN  encounter e ON p.patient_id = e.patient_id   "
            + "                    INNER JOIN  obs o ON e.encounter_id = o.encounter_id   "
            + "                    INNER JOIN  obs o2 ON e.encounter_id = o2.encounter_id   "
            + "                WHERE  e.location_id = :location   "
            + "                    AND e.encounter_type = ${6}  "
            + "                    AND ((o.concept_id = ${23985}   "
            + "                    AND o.value_coded = ${656})   "
            + "                    AND (o2.concept_id = ${165308}   "
            + "                    AND o2.value_coded = ${1267}))   "
            + "                    AND o2.obs_datetime >= :startDate   "
            + "                    AND o2.obs_datetime <= :revisionEndDate   "
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
   * <p>“PROFILAXIA = 3HP” (concept_id 23985 value 23954) and “Data Fim” (concept_id 165308 value
   * 1267) Encounter_datetime between startDateRevision and endDateRevision and:
   *
   * <p>- Obs_datetime(from the last clinical consultation with “PROFILAXIA = 3HP” (concept_id 23985
   * value 23954) and “Data Fim” (concept_id 165308 value 1267)) MINUS - Obs_datetime (from the
   * clinical consultation with “PROFILAXIA = * 3HP” (concept_id 23985 value 23954) and “Data Fim”
   * (concept_id 165308 value 1267) between 6 months and 9 months
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWithProphylaxyDuringRevisionPeriod3HP() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "Patients with Prophylaxy Treatment within Revision Period for 3HP");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("23985", tbMetadata.getRegimeTPTConcept().getConceptId());
    map.put("23954", tbMetadata.get3HPConcept().getConceptId());
    map.put("165308", tbMetadata.getDataEstadoDaProfilaxiaConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugsConcept().getConceptId());
    map.put("1267", hivMetadata.getCompletedConcept().getConceptId());

    String query =
        " SELECT  p.patient_id   "
            + " FROM  patient p   "
            + "    INNER JOIN   "
            + "            ( "
            + "                SELECT p.patient_id, MAX(o2.obs_datetime) AS e_datetime   "
            + "                FROM patient p   "
            + "                    INNER JOIN encounter e ON p.patient_id = e.patient_id   "
            + "                    INNER JOIN obs o ON e.encounter_id = o.encounter_id   "
            + "                    INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id   "
            + "                WHERE   "
            + "                    e.location_id = :location   "
            + "                        AND e.encounter_type = ${6}   "
            + "                        AND ((o.concept_id = ${23985}   "
            + "                        AND o.value_coded = ${23954})   "
            + "                        AND (o2.concept_id = ${165308}   "
            + "                        AND o2.value_coded = ${1256}))   "
            + "                        AND o2.obs_datetime >= :startDate   "
            + "                        AND o2.obs_datetime <= :endDate   "
            + "                        AND p.voided = 0   "
            + "                        AND e.voided = 0   "
            + "                        AND o.voided = 0   "
            + "                        AND o2.voided = 0   "
            + "                GROUP BY p.patient_id "
            + "            ) AS b4 ON p.patient_id = b4.patient_id   "
            + "    INNER JOIN   "
            + "            ( "
            + "                SELECT  p.patient_id, MAX(o2.obs_datetime) AS e_datetime   "
            + "                FROM  patient p   "
            + "                    INNER JOIN  encounter e ON p.patient_id = e.patient_id   "
            + "                    INNER JOIN  obs o ON e.encounter_id = o.encounter_id   "
            + "                    INNER JOIN  obs o2 ON e.encounter_id = o2.encounter_id   "
            + "                WHERE  e.location_id = :location   "
            + "                    AND e.encounter_type = ${6}  "
            + "                    AND ((o.concept_id = ${23985}   "
            + "                    AND o.value_coded = ${23954})   "
            + "                    AND (o2.concept_id = ${165308}   "
            + "                    AND o2.value_coded = ${1267}))   "
            + "                    AND o2.obs_datetime >= :startDate   "
            + "                    AND o2.obs_datetime <= :revisionEndDate   "
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
   * <p>- ( obs_datetime (from the last MASTERCARD - Ficha Resumo (encounter 53) with Ultima
   * Profilaxia TPT (concept_id 23985) = INH (concept = 656) and (Data Início)” (obs_datetime for
   * concept id 165308 value 1256) AND - obs_datetime (from the last clinical consultation
   * (encounter 6) with Ultima Profilaxia TPT (concept_id 23985) = INH (concept = 656) and (Data *
   * Início)” (obs_datetime for concept id 165308 value 1256) ) PLUS 9 MONTHS
   *
   * @return CohortDefinition
   *     <li><strong>Should</strong> Returns empty if there is no patient who meets the conditions
   *     <li><strong>Should</strong> fetch all patients with TB Diagnosis Active
   */
  public CohortDefinition getPatientsWithTBDiagActive() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients with TB Diagnosis Active INH");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("23985", tbMetadata.getRegimeTPTConcept().getConceptId());
    map.put("656", tbMetadata.getIsoniazidConcept().getConceptId());
    map.put("165308", tbMetadata.getDataEstadoDaProfilaxiaConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("23761", tbMetadata.getActiveTBConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());

    String query =
        ""
            + " SELECT p.patient_id "
            + " FROM patient p "
            + "   INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "   INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "     INNER JOIN (SELECT tpt_start.patient_id, MAX(tpt_start.last_encounter) tpt_start_date "
            + "FROM ( "
            + "         SELECT p.patient_id, MAX(o2.obs_datetime) last_encounter "
            + "         FROM patient p "
            + "                  INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                  INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                  INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "         WHERE p.voided = 0 "
            + "           AND e.voided = 0 "
            + "           AND o.voided = 0 "
            + "           AND o2.voided = 0 "
            + "           AND e.location_id = :location "
            + "           AND e.encounter_type = ${6} "
            + "           AND ( ( o.concept_id = ${23985} "
            + "           AND     o.value_coded = ${656} ) "
            + "           AND   ( o2.concept_id = ${165308} "
            + "           AND     o2.value_coded = ${1256} "
            + "           AND     o2.obs_datetime BETWEEN :startDate AND :endDate ) ) "
            + "         GROUP BY p.patient_id "
            + "         UNION "
            + "         SELECT p.patient_id, MAX(o2.obs_datetime) last_encounter "
            + "         FROM patient p "
            + "                  INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                  INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                  INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "         WHERE p.voided = 0 "
            + "           AND e.voided = 0 "
            + "           AND o.voided = 0 "
            + "           AND o2.voided = 0 "
            + "           AND e.location_id = :location "
            + "           AND e.encounter_type = ${53} "
            + "           AND ( ( o.concept_id = ${23985} AND o.value_coded = ${656} ) "
            + "           AND   ( o2.concept_id = ${165308} AND o2.value_coded = ${1256}  "
            + "           AND     o2.obs_datetime BETWEEN :startDate AND :endDate ) ) "
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
            + "         AND o.voided = 0 ";

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
   * <p>- “Diagnótico TB activo” (concept_id 23761) value coded “SIM”(concept id 1065) and
   * Encounter_datetime between Encounter_datetime(the most recent from B5_1 or B5_2) and
   * Encounter_datetime(the most recent from B5_1 or B5_2) + 6 months
   *
   * @return CohortDefinition
   *     <li><strong>Should</strong> Returns empty if there is no patient who meets the conditions
   *     <li><strong>Should</strong> fetch all patients with TB Diagnosis Active
   */
  public CohortDefinition getPatientsWithTBDiagActive3hp() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients with TB Diagnosis Active 3hp");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("23985", tbMetadata.getRegimeTPTConcept().getConceptId());
    map.put("23954", tbMetadata.get3HPConcept().getConceptId());
    map.put("165308", tbMetadata.getDataEstadoDaProfilaxiaConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("23761", tbMetadata.getActiveTBConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());

    String query =
        ""
            + " SELECT p.patient_id "
            + " FROM patient p "
            + "   INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "   INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "     INNER JOIN (SELECT tpt_start.patient_id, MAX(tpt_start.last_encounter) tpt_start_date "
            + "FROM ( "
            + "         SELECT p.patient_id, MAX(o2.obs_datetime) last_encounter "
            + "         FROM patient p "
            + "                  INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                  INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                  INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "         WHERE p.voided = 0 "
            + "           AND e.voided = 0 "
            + "           AND o.voided = 0 "
            + "           AND o2.voided = 0 "
            + "           AND e.location_id = :location "
            + "           AND e.encounter_type = ${6} "
            + "           AND ( ( o.concept_id = ${23985} "
            + "           AND     o.value_coded = ${23954} ) "
            + "           AND   ( o2.concept_id = ${165308} "
            + "           AND     o2.value_coded = ${1256} "
            + "           AND     o2.obs_datetime BETWEEN :startDate AND :endDate ) ) "
            + "         GROUP BY p.patient_id "
            + "         UNION "
            + "         SELECT p.patient_id, MAX(o2.obs_datetime) last_encounter "
            + "         FROM patient p "
            + "                  INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                  INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                  INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "         WHERE p.voided = 0 "
            + "           AND e.voided = 0 "
            + "           AND o.voided = 0 "
            + "           AND o2.voided = 0 "
            + "           AND e.location_id = :location "
            + "           AND e.encounter_type = ${53} "
            + "           AND ( ( o.concept_id = ${23985} AND o.value_coded = ${23954} ) "
            + "           AND   ( o2.concept_id = ${165308} AND o2.value_coded = ${1256}  "
            + "           AND     o2.obs_datetime BETWEEN :startDate AND :endDate ) ) "
            + "         GROUP BY p.patient_id "
            + "     ) AS tpt_start "
            + "GROUP BY tpt_start.patient_id) AS last ON p.patient_id = last.patient_id "
            + " WHERE "
            + "     e.location_id = :location "
            + "         AND e.encounter_type = ${6} "
            + "         AND o.concept_id = ${23761} "
            + "         AND o.value_coded IN (${1065}) "
            + "         AND e.encounter_datetime BETWEEN last.tpt_start_date AND DATE_ADD(last.tpt_start_date, INTERVAL 6 MONTH) "
            + "         AND p.voided = 0 "
            + "         AND e.voided = 0 "
            + "         AND o.voided = 0 ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * All patients ll patients with a clinical consultation(encounter type 6) during the Revision
   * period with the following conditions:
   *
   * <p>- “TEM SINTOMAS DE TB” (concept_id 23758) value coded “SIM” (concept_id IN [1065]) and
   * Encounter_datetime between:
   *
   * <p>- ( obs_datetime (from the last MASTERCARD - Ficha Resumo (encounter 53) with Ultima
   * Profilaxia TPT (concept_id 23985) = INH (concept = 656) and (Data Início)” (obs_datetime for
   * concept id 165308 value 1256) AND - obs_datetime (from the last clinical consultation
   * (encounter 6) with Ultima Profilaxia TPT (concept_id 23985) = INH (concept = 656) and (Data *
   * Início)” (obs_datetime for concept id 165308 value 1256) ) PLUS 9 MONTHS
   *
   * @return CohortDefinition
   *     <li><strong>Should</strong> Returns empty if there is no patient who meets the conditions
   *     <li><strong>Should</strong> fetch all patients with TB symptoms
   */
  public CohortDefinition getPatientsWithTBSymtoms() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients with TB Symptoms INH");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("23985", tbMetadata.getRegimeTPTConcept().getConceptId());
    map.put("656", tbMetadata.getIsoniazidConcept().getConceptId());
    map.put("165308", tbMetadata.getDataEstadoDaProfilaxiaConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugsConcept().getConceptId());
    map.put("23758", tbMetadata.getHasTbSymptomsConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());

    String query =
        " SELECT p.patient_id "
            + " FROM patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "         INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "     INNER JOIN (SELECT tpt_start.patient_id, MAX(tpt_start.last_encounter) tpt_start_date "
            + "            FROM ( "
            + "                     SELECT p.patient_id, MAX(o2.obs_datetime) last_encounter "
            + "                     FROM patient p "
            + "                              INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                              INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                              INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "                     WHERE p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o2.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                       AND e.encounter_type = ${6} "
            + "                       AND ( ( o.concept_id = ${23985} "
            + "                       AND     o.value_coded = ${656}) "
            + "                       AND   ( o2.concept_id = ${165308} "
            + "                       AND     o2.value_coded = ${1256} "
            + "                       AND     o2.obs_datetime BETWEEN :startDate AND :endDate ) ) "
            + "                     GROUP BY p.patient_id "
            + "                     UNION "
            + "                     SELECT p.patient_id, MAX(o2.obs_datetime) last_encounter "
            + "                     FROM patient p "
            + "                              INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                              INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                              INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "                     WHERE p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o2.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                       AND e.encounter_type = ${53} "
            + "                       AND ( ( o.concept_id = ${23985} AND o.value_coded = ${656} ) "
            + "                       AND   ( o2.concept_id = ${165308} AND o2.value_coded = ${1256}  "
            + "                       AND     o2.obs_datetime BETWEEN :startDate AND :endDate ) ) "
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
            + "         AND o.voided = 0 ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * All patients ll patients with a clinical consultation(encounter type 6) during the Revision
   * period with the following conditions:
   *
   * <p>- “TEM SINTOMAS DE TB” (concept_id 23758) value coded “SIM” (concept_id IN [1065]) and
   * Encounter_datetime between:
   *
   * <p>- “TEM SINTOMAS DE TB” (concept_id 23758) value coded “SIM” (concept_id IN [1065]) and
   * Encounter_datetime between Encounter_datetime(from B5_1 or B5_2) and Encounter_datetime(from
   * B5_1 or B5_2) + 6 months
   *
   * @return CohortDefinition
   *     <li><strong>Should</strong> Returns empty if there is no patient who meets the conditions
   *     <li><strong>Should</strong> fetch all patients with TB symptoms
   */
  public CohortDefinition getPatientsWithTBSymtoms3HP() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients with TB Symptoms 3hp");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("23985", tbMetadata.getRegimeTPTConcept().getConceptId());
    map.put("23954", tbMetadata.get3HPConcept().getConceptId());
    map.put("165308", tbMetadata.getDataEstadoDaProfilaxiaConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugsConcept().getConceptId());
    map.put("23758", tbMetadata.getHasTbSymptomsConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("6128", hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId());

    String query =
        " SELECT p.patient_id "
            + " FROM patient p "
            + "    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "         INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "     INNER JOIN (SELECT tpt_start.patient_id, MAX(tpt_start.last_encounter) tpt_start_date "
            + "            FROM ( "
            + "                     SELECT p.patient_id, MAX(o2.obs_datetime) last_encounter "
            + "                     FROM patient p "
            + "                              INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                              INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                              INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "                     WHERE p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o2.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                       AND e.encounter_type = ${6} "
            + "                       AND ( ( o.concept_id = ${23985} "
            + "                       AND     o.value_coded = ${23954}) "
            + "                       AND   ( o2.concept_id = ${165308} "
            + "                       AND     o2.value_coded = ${1256} "
            + "                       AND o2.obs_datetime BETWEEN :startDate AND :endDate ) ) "
            + "                     GROUP BY p.patient_id "
            + "                     UNION "
            + "                     SELECT p.patient_id, MAX(o2.value_datetime) last_encounter "
            + "                     FROM patient p "
            + "                              INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                              INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                              INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "                     WHERE p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o2.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                       AND e.encounter_type = ${53} "
            + "                       AND ( ( o.concept_id = ${23985} AND o.value_coded = ${23954} ) "
            + "                       AND   ( o2.concept_id = ${6128} "
            + "                       AND     o2.value_datetime BETWEEN :startDate AND :endDate ) ) "
            + "                     GROUP BY p.patient_id "
            + "                 ) AS tpt_start "
            + "            GROUP BY tpt_start.patient_id ) AS last ON p.patient_id = last.patient_id "
            + " WHERE "
            + "     e.location_id = :location "
            + "         AND e.encounter_type = ${6} "
            + "         AND o.concept_id = ${23758} "
            + "         AND o.value_coded IN (${1065}) "
            + "         AND e.encounter_datetime BETWEEN last.tpt_start_date AND DATE_ADD(last.tpt_start_date, INTERVAL 6 MONTH) "
            + "         AND p.voided = 0 "
            + "         AND e.voided = 0 "
            + "         AND o.voided = 0 ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * All patients with a clinical consultation(encounter type 6) during the Revision period with the
   * following conditions:
   *
   * <p><b>excluindo</b> todos os utentes com <b>“Tratamento TB</b> (respostas = {“Início” ,
   * “Continua”, “Fim”) e a respectiva “Data de Tratamento TB” decorrida durante o <b>período de
   * tratamento</b> (“Data de Consulta”>= “Data Início TPT” e <= “Data Início TPT”+ 9meses ou 297
   * dias).
   *
   * <p>Nota: a “Data Início TPT - Isoniazida” está definida no RF12.
   *
   * @return CohortDefinition
   *     <li><strong>Should</strong> Returns empty if there is no patient who meets the conditions
   *     <li><strong>Should</strong> fetch all patients with TB treatment
   */
  public CohortDefinition getPatientsWithTBTreatment() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients with TB Treatment INH");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("23985", tbMetadata.getRegimeTPTConcept().getConceptId());
    map.put("656", tbMetadata.getIsoniazidConcept().getConceptId());
    map.put("165308", tbMetadata.getDataEstadoDaProfilaxiaConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugsConcept().getConceptId());
    map.put("1268", tbMetadata.getTBTreatmentPlanConcept().getConceptId());
    map.put("1257", hivMetadata.getContinueRegimenConcept().getConceptId());
    map.put("1267", hivMetadata.getCompletedConcept().getConceptId());

    String query =
        " SELECT  p.patient_id  "
            + " FROM  patient p  "
            + "     INNER JOIN  encounter e ON p.patient_id = e.patient_id  "
            + "     INNER JOIN  obs o ON e.encounter_id = o.encounter_id  "
            + "     INNER JOIN (SELECT tpt_start.patient_id, MAX(tpt_start.last_encounter) tpt_start_date "
            + "            FROM ( "
            + "                     SELECT p.patient_id, MAX(o2.obs_datetime) last_encounter "
            + "                     FROM patient p "
            + "                              INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                              INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                              INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "                     WHERE p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o2.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                       AND e.encounter_type = ${6} "
            + "                       AND ( ( o.concept_id = ${23985} "
            + "                       AND     o.value_coded = ${656}) "
            + "                       AND   ( o2.concept_id = ${165308} "
            + "                       AND     o2.value_coded = ${1256} "
            + "                       AND     o2.obs_datetime BETWEEN :startDate AND :endDate ) ) "
            + "                     GROUP BY p.patient_id "
            + "                     UNION "
            + "                     SELECT p.patient_id, MAX(o2.obs_datetime) last_encounter "
            + "                     FROM patient p "
            + "                              INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                              INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                              INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "                     WHERE p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o2.voided = 0 "
            + "                       AND e.encounter_type = ${53} "
            + "                       AND e.location_id = :location "
            + "                       AND ( ( o.concept_id = ${23985} AND o.value_coded = ${656} ) "
            + "                       AND   ( o2.concept_id = ${165308} AND o2.value_coded = ${1256}  "
            + "                       AND     o2.obs_datetime BETWEEN :startDate AND :endDate ) ) "
            + "                     GROUP BY p.patient_id "
            + "                 ) AS tpt_start "
            + "            GROUP BY tpt_start.patient_id ) AS last ON last.patient_id = p.patient_id  "
            + " WHERE  "
            + "     e.location_id = :location  "
            + "         AND e.encounter_type = ${6}  "
            + "         AND o.concept_id = ${1268}  "
            + "         AND o.value_coded IN (${1256} , ${1257}, ${1267})  "
            + "         AND ( ( DATE(o.obs_datetime) BETWEEN DATE(last.tpt_start_date) AND DATE(DATE_ADD(last.tpt_start_date, INTERVAL 9 MONTH)) ) "
            + "         OR ( DATE(o.obs_datetime) BETWEEN DATE(last.tpt_start_date) AND DATE(DATE_ADD(last.tpt_start_date, INTERVAL 297 DAY)) ) ) "
            + "         AND p.voided = 0  "
            + "         AND e.voided = 0  "
            + "         AND o.voided = 0";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * All patients with a clinical consultation(encounter type 6) during the Revision period with the
   * following conditions:
   *
   * <p><b>excluindo</b> todos os utentes com <b>“Tratamento TB</b> (respostas = {“Início” ,”,
   * “Continua”, “Fim”) e a respectiva “Data de Tratamento TB” decorrida durante o <b>período de
   * tratamento</b> (“Data de Consulta”>= “Data Início TPT” e <= “Data Início TPT”+ 6meses ou 198
   * dias).
   *
   * <p>Nota: a “Data Início TPT – 3HP” está definida no RF12.1
   *
   * @return CohortDefinition
   *     <li><strong>Should</strong> Returns empty if there is no patient who meets the conditions
   *     <li><strong>Should</strong> fetch all patients with TB treatment
   */
  public CohortDefinition getPatientsWithTBTreatment3HP() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients with TB Treatment 3hp");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("23985", tbMetadata.getRegimeTPTConcept().getConceptId());
    map.put("23954", tbMetadata.get3HPConcept().getConceptId());
    map.put("165308", tbMetadata.getDataEstadoDaProfilaxiaConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugsConcept().getConceptId());
    map.put("1268", tbMetadata.getTBTreatmentPlanConcept().getConceptId());
    map.put("1257", hivMetadata.getContinueRegimenConcept().getConceptId());
    map.put("1267", hivMetadata.getCompletedConcept().getConceptId());

    String query =
        " SELECT  p.patient_id  "
            + " FROM  patient p  "
            + "     INNER JOIN  encounter e ON p.patient_id = e.patient_id  "
            + "     INNER JOIN  obs o ON e.encounter_id = o.encounter_id  "
            + "     INNER JOIN (SELECT tpt_start.patient_id, MAX(tpt_start.last_encounter) tpt_start_date "
            + "            FROM ( "
            + "                     SELECT p.patient_id, MAX(o2.obs_datetime) last_encounter "
            + "                     FROM patient p "
            + "                              INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                              INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                              INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "                     WHERE p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o2.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                       AND e.encounter_type = ${6} "
            + "                       AND ( ( o.concept_id = ${23985} "
            + "                       AND     o.value_coded = ${23954}) "
            + "                       AND   ( o2.concept_id = ${165308} "
            + "                       AND     o2.value_coded = ${1256} "
            + "                       AND     o2.obs_datetime BETWEEN :startDate AND :endDate ) ) "
            + "                     GROUP BY p.patient_id "
            + "                     UNION "
            + "                     SELECT p.patient_id, MAX(o2.value_datetime) last_encounter "
            + "                     FROM patient p "
            + "                              INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                              INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                              INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "                     WHERE p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o2.voided = 0 "
            + "                       AND e.encounter_type = ${53} "
            + "                       AND e.location_id = :location "
            + "                       AND ( ( o.concept_id = ${23985} AND o.value_coded = ${23954} ) "
            + "                       AND   ( o2.concept_id = ${165308} AND o2.value_coded = ${1256} "
            + "                       AND     o2.obs_datetime BETWEEN :startDate AND :endDate ) ) "
            + "                     GROUP BY p.patient_id "
            + "                 ) AS tpt_start "
            + "            GROUP BY tpt_start.patient_id ) AS last ON last.patient_id = p.patient_id  "
            + " WHERE  "
            + "     e.location_id = :location  "
            + "         AND e.encounter_type = ${6}  "
            + "         AND o.concept_id = ${1268}  "
            + "         AND o.value_coded IN (${1256} , ${1257}, ${1267})  "
            + "         AND ( ( DATE(o.obs_datetime) between DATE(last.tpt_start_date) AND DATE(DATE_ADD(last.tpt_start_date, INTERVAL 6 MONTH)) ) "
            + "         OR ( DATE(o.obs_datetime) between DATE(last.tpt_start_date) AND DATE(DATE_ADD(last.tpt_start_date, INTERVAL 198 DAY)) ) ) "
            + "         AND p.voided = 0  "
            + "         AND e.voided = 0  "
            + "         AND o.voided = 0";

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
   * @param den boolean parameter, true indicates denominator ,false indicates numerator
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
   * <p>Note: consider the first clinical consultation with nutritional state equal to “DAM” or
   * “DAG” occurred during the revision period.
   *
   * @return SqlCohortDefinition
   *     <li><strong>Should</strong> Returns empty if there is no patient who meets the conditions
   *     <li><strong>Should</strong> fetch all patients with B criteria
   */
  public SqlCohortDefinition getNutritionalBCat5() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" All patients that started ART during inclusion period ");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

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
   * @param den boolean parameter, true indicates denominator ,false indicates numerator
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
   * @param den indicator number
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
   * @param num indicator number
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
   * @param den indicator number
   */
  public CohortDefinition getMQ7A(Integer den) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    if (den == 1) {
      compositionCohortDefinition.setName(
          "Categoria 7 Adulto Indicador 7.1 – Denominador Início TPT");
    } else if (den == 2) {
      compositionCohortDefinition.setName(
          "Categoria 7 Adulto Indicador 7.2 – Denominador- FIM TPT");
    } else if (den == 3) {
      compositionCohortDefinition.setName(
          "Categoria 7 Pediátrico Indicador 7.3 – Denominador- Início TPT");
    } else if (den == 4) {
      compositionCohortDefinition.setName(
          "Categoria 7 Pediátrico Indicador 7.4 – Denominador- FIM TPT");
    } else if (den == 5) {
      compositionCohortDefinition.setName("Categoria 7 MG Indicador 7.5 – Denominador- Início TPT");
    } else if (den == 6) {
      compositionCohortDefinition.setName("Categoria 7 MG Indicador 7.6 – Denominador- FIM TPT");
    }
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition startedART = getMOHArtStartDate();

    CohortDefinition b41 = getB4And1();
    CohortDefinition b42 = getB4And2();
    CohortDefinition b51 = getB5And1();
    CohortDefinition b52 = getB5And2();

    CohortDefinition tptInh = getPatientsWhoStartedTpt(true);
    CohortDefinition tpt3hp = getPatientsWhoStartedTpt(false);

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

    CohortDefinition transferOut = getTranferredOutPatientsCat7();

    CohortDefinition tbDiagOnPeriod = getPatientsWithTBDiagActive();

    CohortDefinition tbDiagOnPeriod3HP = getPatientsWithTBDiagActive3hp();

    CohortDefinition tbSymptomsOnPeriod = getPatientsWithTBSymtoms();

    CohortDefinition tbSymptomsOnPeriod3hp = getPatientsWithTBSymtoms3HP();

    CohortDefinition tbTreatmentOnPeriod = getPatientsWithTBTreatment();

    CohortDefinition tbTreatmentOnPeriod3hp = getPatientsWithTBTreatment3HP();

    compositionCohortDefinition.addSearch("A", EptsReportUtils.map(startedART, MAPPING));

    compositionCohortDefinition.addSearch("B1", EptsReportUtils.map(tbActive, MAPPING));

    compositionCohortDefinition.addSearch("B2", EptsReportUtils.map(tbSymptoms, MAPPING));

    compositionCohortDefinition.addSearch("B3", EptsReportUtils.map(tbTreatment, MAPPING));

    compositionCohortDefinition.addSearch("B4", EptsReportUtils.map(tbProphilaxy, MAPPING));

    compositionCohortDefinition.addSearch("C", EptsReportUtils.map(pregnant, MAPPING));

    compositionCohortDefinition.addSearch("D", EptsReportUtils.map(breastfeeding, MAPPING));

    compositionCohortDefinition.addSearch("E", EptsReportUtils.map(transferredIn, MAPPING));

    compositionCohortDefinition.addSearch("F", EptsReportUtils.map(transferOut, MAPPING11));

    compositionCohortDefinition.addSearch("H", EptsReportUtils.map(tbDiagOnPeriod, MAPPING));

    compositionCohortDefinition.addSearch("H1", EptsReportUtils.map(tbDiagOnPeriod3HP, MAPPING));

    compositionCohortDefinition.addSearch("I", EptsReportUtils.map(tbSymptomsOnPeriod, MAPPING));

    compositionCohortDefinition.addSearch(
        "I1", EptsReportUtils.map(tbSymptomsOnPeriod3hp, MAPPING));

    compositionCohortDefinition.addSearch("J", EptsReportUtils.map(tbTreatmentOnPeriod, MAPPING));

    compositionCohortDefinition.addSearch(
        "J1", EptsReportUtils.map(tbTreatmentOnPeriod3hp, MAPPING));

    compositionCohortDefinition.addSearch("B41", EptsReportUtils.map(b41, MAPPING));

    compositionCohortDefinition.addSearch("B42", EptsReportUtils.map(b42, MAPPING));

    compositionCohortDefinition.addSearch("B51", EptsReportUtils.map(b51, MAPPING));

    compositionCohortDefinition.addSearch("B52", EptsReportUtils.map(b52, MAPPING));

    compositionCohortDefinition.addSearch("INHSTART", EptsReportUtils.map(tptInh, MAPPING));

    compositionCohortDefinition.addSearch("TPT3HPSTART", EptsReportUtils.map(tpt3hp, MAPPING));

    if (den == 1 || den == 3) {
      compositionCohortDefinition.setCompositionString("A AND NOT (B1 OR B2 OR B3 OR C OR D OR E)");
    } else if (den == 2 || den == 4) {
      compositionCohortDefinition.setCompositionString(
          "(A AND (INHSTART OR TPT3HPSTART)) AND NOT (B1 OR B2 OR B3 OR C OR D OR E OR F OR H OR H1 OR I OR I1 OR J OR J1)");
    } else if (den == 5) {
      compositionCohortDefinition.setCompositionString(
          "(A AND C) AND NOT (B1 OR B2 OR B3 OR D OR E)");
    } else if (den == 6) {
      compositionCohortDefinition.setCompositionString(
          "(A AND (INHSTART OR TPT3HPSTART) AND C) AND NOT (B1 OR B2 OR B3 OR D OR E OR F OR H OR H1 OR I OR I1 OR J OR J1)");
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
   * @param num indicator number
   * @return CohortDefinition
   */
  public CohortDefinition getMQ7B(Integer num) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    if (num == 1) {
      compositionCohortDefinition.setName(
          "Categoria 7 Adulto Indicador 7.1 – Numerador Início TPT");
    } else if (num == 2) {
      compositionCohortDefinition.setName("Categoria 7 Adulto Indicador 7.2 – Numerador FIM TPT");
    } else if (num == 3) {
      compositionCohortDefinition.setName(
          "Categoria 7 Pediátrico Indicador 7.3 – Numerador Início TPT");
    } else if (num == 4) {
      compositionCohortDefinition.setName(
          "Categoria 7 Pediátrico – Indicador 7.4 – Numerador FIM TPT");
    } else if (num == 5) {
      compositionCohortDefinition.setName("Categoria 7 MG Indicador 7.5 – Numerador Início TPT");
    } else if (num == 6) {
      compositionCohortDefinition.setName("Categoria 7 MG Indicador 7.6 – Numerador FIM TPT");
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

    CohortDefinition transferOut = getTranferredOutPatientsCat7();

    CohortDefinition tbProphylaxyOnPeriod = getPatientsWithProphylaxyDuringRevisionPeriod();

    CohortDefinition tbProphylaxyOnPeriod3hp = getPatientsWithProphylaxyDuringRevisionPeriod3HP();

    CohortDefinition tbDiagOnPeriod = getPatientsWithTBDiagActive();

    CohortDefinition tbDiagOnPeriod3HP = getPatientsWithTBDiagActive3hp();

    CohortDefinition tbSymptomsOnPeriod = getPatientsWithTBSymtoms();

    CohortDefinition tbSymptomsOnPeriod3hp = getPatientsWithTBSymtoms3HP();

    CohortDefinition tbTreatmentOnPeriod = getPatientsWithTBTreatment();

    CohortDefinition tbTreatmentOnPeriod3hp = getPatientsWithTBTreatment3HP();

    CohortDefinition tptInh = getPatientsWhoStartedTpt(true);

    CohortDefinition tpt3hp = getPatientsWhoStartedTpt(false);

    CohortDefinition mq7DenOne = getMQ7A(1);

    CohortDefinition mq7DenTwo = getMQ7A(2);

    CohortDefinition mq7DenThree = getMQ7A(3);

    CohortDefinition mq7DenFour = getMQ7A(4);

    CohortDefinition mq7DenFive = getMQ7A(5);

    CohortDefinition mq7DenSix = getMQ7A(6);

    compositionCohortDefinition.addSearch("A", EptsReportUtils.map(startedART, MAPPING));

    compositionCohortDefinition.addSearch("B1", EptsReportUtils.map(tbActive, MAPPING));

    compositionCohortDefinition.addSearch("B2", EptsReportUtils.map(tbSymptoms, MAPPING));

    compositionCohortDefinition.addSearch("B3", EptsReportUtils.map(tbTreatment, MAPPING));

    compositionCohortDefinition.addSearch("B4", EptsReportUtils.map(tbProphilaxy, MAPPING));

    compositionCohortDefinition.addSearch("C", EptsReportUtils.map(pregnant, MAPPING));

    compositionCohortDefinition.addSearch("D", EptsReportUtils.map(breastfeeding, MAPPING));

    compositionCohortDefinition.addSearch("E", EptsReportUtils.map(transferredIn, MAPPING));

    compositionCohortDefinition.addSearch("F", EptsReportUtils.map(transferOut, MAPPING11));

    compositionCohortDefinition.addSearch("G", EptsReportUtils.map(tbProphylaxyOnPeriod, MAPPING1));

    compositionCohortDefinition.addSearch(
        "G1", EptsReportUtils.map(tbProphylaxyOnPeriod3hp, MAPPING1));

    compositionCohortDefinition.addSearch("H", EptsReportUtils.map(tbDiagOnPeriod, MAPPING));

    compositionCohortDefinition.addSearch("H1", EptsReportUtils.map(tbDiagOnPeriod3HP, MAPPING));

    compositionCohortDefinition.addSearch("I", EptsReportUtils.map(tbSymptomsOnPeriod, MAPPING));

    compositionCohortDefinition.addSearch(
        "I1", EptsReportUtils.map(tbSymptomsOnPeriod3hp, MAPPING));

    compositionCohortDefinition.addSearch("J", EptsReportUtils.map(tbTreatmentOnPeriod, MAPPING));

    compositionCohortDefinition.addSearch(
        "J1", EptsReportUtils.map(tbTreatmentOnPeriod3hp, MAPPING));

    compositionCohortDefinition.addSearch(
        "GNEW", EptsReportUtils.map(getPatientsWithTptInhEnd(), MAPPING1));

    compositionCohortDefinition.addSearch(
        "L", EptsReportUtils.map(getPatientsWithTpt3hpEnd(), MAPPING1));

    compositionCohortDefinition.addSearch("INHSTART", EptsReportUtils.map(tptInh, MAPPING));

    compositionCohortDefinition.addSearch("TPT3HPSTART", EptsReportUtils.map(tpt3hp, MAPPING));

    compositionCohortDefinition.addSearch("MQ7DEN1", EptsReportUtils.map(mq7DenOne, MAPPING1));

    compositionCohortDefinition.addSearch("MQ7DEN2", EptsReportUtils.map(mq7DenTwo, MAPPING1));

    compositionCohortDefinition.addSearch("MQ7DEN3", EptsReportUtils.map(mq7DenThree, MAPPING1));

    compositionCohortDefinition.addSearch("MQ7DEN4", EptsReportUtils.map(mq7DenFour, MAPPING1));

    compositionCohortDefinition.addSearch("MQ7DEN5", EptsReportUtils.map(mq7DenFive, MAPPING1));

    compositionCohortDefinition.addSearch("MQ7DEN6", EptsReportUtils.map(mq7DenSix, MAPPING1));

    if (num == 1) {
      compositionCohortDefinition.setCompositionString("MQ7DEN1 AND (INHSTART OR TPT3HPSTART)");
    } else if (num == 2) {
      compositionCohortDefinition.setCompositionString("MQ7DEN2 AND (GNEW OR L)");
    } else if (num == 3) {
      compositionCohortDefinition.setCompositionString("MQ7DEN3 AND (INHSTART OR TPT3HPSTART)");
    } else if (num == 4) {
      compositionCohortDefinition.setCompositionString("MQ7DEN4 AND (GNEW OR L)");
    } else if (num == 5) {
      compositionCohortDefinition.setCompositionString("MQ7DEN5 AND INHSTART");
    } else if (num == 6) {
      compositionCohortDefinition.setCompositionString("MQ7DEN6 AND GNEW");
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
   * @param indicatorFlag indicator number
   * @param reportSource report Source (MQ or MI)
   * @return CohortDefinition
   * @params indicatorFlag A to G For inicator 11.1 to 11.7 respectively
   */
  public CohortDefinition getMQC11DEN(int indicatorFlag, MIMQ reportSource) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    boolean useE53 = false;

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

    CohortDefinition patientsFromFichaClinicaCargaViral = getB2_13(useE53);

    CohortDefinition pregnantWithCargaViralHigherThan1000;
    CohortDefinition breastfeedingWithCargaViralHigherThan1000;

    if (indicatorFlag == 4) {
      pregnantWithCargaViralHigherThan1000 =
          QualityImprovement2020Queries.getMQ13DenB4_P4(
              hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
              hivMetadata.getHivViralLoadConcept().getConceptId(),
              hivMetadata.getYesConcept().getConceptId(),
              commonMetadata.getPregnantConcept().getConceptId(),
              50);

      breastfeedingWithCargaViralHigherThan1000 =
          QualityImprovement2020Queries.getMQ13DenB5_P4(
              hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
              hivMetadata.getHivViralLoadConcept().getConceptId(),
              hivMetadata.getYesConcept().getConceptId(),
              commonMetadata.getBreastfeeding().getConceptId(),
              50);

    } else {
      pregnantWithCargaViralHigherThan1000 =
          QualityImprovement2020Queries.getMQ13DenB4_P4(
              hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
              hivMetadata.getHivViralLoadConcept().getConceptId(),
              hivMetadata.getYesConcept().getConceptId(),
              commonMetadata.getPregnantConcept().getConceptId());

      breastfeedingWithCargaViralHigherThan1000 =
          QualityImprovement2020Queries.getMQ13DenB5_P4(
              hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
              hivMetadata.getHivViralLoadConcept().getConceptId(),
              hivMetadata.getYesConcept().getConceptId(),
              commonMetadata.getBreastfeeding().getConceptId());
    }

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

    CohortDefinition transfOut = intensiveMonitoringCohortQueries.getTranferredOutPatientsForMI7();

    if (reportSource.equals(MIMQ.MQ)) {
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
      compositionCohortDefinition.addSearch("F", EptsReportUtils.map(transfOut, MAPPING14));
      compositionCohortDefinition.addSearch(
          "ADULT",
          EptsReportUtils.map(
              genericCohortQueries.getPatientAgeBasedOnFirstViralLoadDate(15, 200),
              "startDate=${startDate},endDate=${endDate},location=${location}"));
    } else if (reportSource.equals(MIMQ.MI)) {

      if (indicatorFlag == 1) {
        compositionCohortDefinition.addSearch("F", EptsReportUtils.map(transfOut, MAPPING14));
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
            "B1", EptsReportUtils.map(patientsFromFichaClinicaLinhaTerapeutica, MAPPING5));

        compositionCohortDefinition.addSearch(
            "B2", EptsReportUtils.map(patientsFromFichaClinicaCargaViral, MAPPING8));

        compositionCohortDefinition.addSearch(
            "B4", EptsReportUtils.map(pregnantWithCargaViralHigherThan1000, MAPPING8));

        compositionCohortDefinition.addSearch(
            "B5", EptsReportUtils.map(breastfeedingWithCargaViralHigherThan1000, MAPPING8));

        compositionCohortDefinition.addSearch(
            "ADULT",
            EptsReportUtils.map(
                genericCohortQueries.getPatientAgeBasedOnFirstViralLoadDate(15, 200),
                "startDate=${startDate},endDate=${endDate},location=${location}"));

        compositionCohortDefinition.addSearch("C", EptsReportUtils.map(pregnant, MAPPING));
        compositionCohortDefinition.addSearch("D", EptsReportUtils.map(breastfeeding, MAPPING));
        compositionCohortDefinition.addSearch("E", EptsReportUtils.map(transferredIn, MAPPING7));
        compositionCohortDefinition.addSearch("F", EptsReportUtils.map(transfOut, MAPPING14));
      }
      if (indicatorFlag == 3 || indicatorFlag == 5 || indicatorFlag == 6) {
        compositionCohortDefinition.addSearch("F", EptsReportUtils.map(transfOut, MAPPING14));
      }
    }

    if (indicatorFlag == 1) {
      compositionCohortDefinition.setCompositionString("((A OR D) AND NOT (C OR E OR F))");
    }
    if (indicatorFlag == 5 || indicatorFlag == 6) {
      compositionCohortDefinition.setCompositionString("A AND NOT (C OR D OR E OR F)");
    }
    if (indicatorFlag == 2) {
      compositionCohortDefinition.setCompositionString(
          "((B2 AND (ADULT OR B5) AND B1) AND NOT (B4 OR F))");
    }
    if (indicatorFlag == 7) {
      compositionCohortDefinition.setCompositionString("((B1 AND B2) AND NOT (B4 OR B5 OR F))");
    }
    if (indicatorFlag == 3) {
      compositionCohortDefinition.setCompositionString("(A AND C) AND NOT (E OR F)");
    }
    if (indicatorFlag == 4) {
      compositionCohortDefinition.setCompositionString("((B1 AND B4) AND NOT F)");
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
   * @param indicatorFlag indicator number
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

    CohortDefinition transfOut = getTranferredOutPatientsCat7();

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
            "revisionStartDate=${revisionEndDate-14m},revisionEndDate=${revisionEndDate},location=${location}"));

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
          "(A AND (ADULT OR D)) AND B1 NOT (B1E OR C OR F)");
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
      compositionCohortDefinition.setCompositionString("(A AND B1 AND C) AND NOT (B1E OR F)");
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
   * @param indicator indicator number
   * @return CohortDefinition
   * @params indicatorFlag A to F For inicator 13.2 to 13.14 accordingly to the specs
   */
  public CohortDefinition getMQC13P3DEN(int indicator) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    if (indicator == 2)
      compositionCohortDefinition.setName(
          "Adultos (15/+anos) que iniciaram a 1a linha de TARV ou novo regime da 1ª linha há 9 meses atrás");
    if (indicator == 9)
      compositionCohortDefinition.setName(
          "Crianças (0-4 anos de idade) com registo de início da 1a linha de TARV há 9 meses");
    if (indicator == 10)
      compositionCohortDefinition.setName(
          "Crianças  (5-9 anos de idade) com registo de início da 1a linha de TARV ou novo regime de TARV há 9 meses");
    if (indicator == 11)
      compositionCohortDefinition.setName(
          "Crianças  (10-14 anos de idade) com registo de início da 1a linha de TARV ou novo regime da 1ª linha de TARV no mês de avaliação");
    if (indicator == 5)
      compositionCohortDefinition.setName(
          "Adultos (15/+ anos) com registo de início da 2a linha de TARV há 9 meses");
    if (indicator == 14)
      compositionCohortDefinition.setName(
          "Crianças com registo de início da 2a linha de TARV no mês de avaliação");

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

    CohortDefinition transfOut = getTranferredOutPatientsCat7();

    CohortDefinition abandonedTarv = getPatientsWhoAbandonedOrRestartedTarvOnLast6MonthsArt();
    CohortDefinition abandonedFirstLine = getPatientsWhoAbandonedTarvOnOnFirstLineDate();
    CohortDefinition abandonedSecondLine = getPatientsWhoAbandonedTarvOnOnSecondLineDate();
    CohortDefinition tbDiagnosisActive = getPatientsWithTbActiveOrTbTreatment();

    if (indicator == 2) {
      compositionCohortDefinition.addSearch(
          "age",
          EptsReportUtils.map(
              ageCohortQueries.createXtoYAgeCohort(
                  "Adultos (15/+anos) que iniciaram a 1a linha de TARV ou novo regime da 1ª linha há 9 meses atrás",
                  15,
                  null),
              "effectiveDate=${revisionEndDate}"));
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
                  "Crianças (0-4 anos de idade) com registo de início da 1a linha de TARV há 9 meses",
                  0,
                  4),
              "effectiveDate=${revisionEndDate}"));
    } else if (indicator == 10) {
      compositionCohortDefinition.addSearch(
          "age",
          EptsReportUtils.map(
              ageCohortQueries.createXtoYAgeCohort(
                  "Crianças  (5-9 anos de idade) com registo de início da 1a linha de TARV ou novo regime de TARV há 9 meses",
                  5,
                  9),
              "effectiveDate=${revisionEndDate}"));
    } else if (indicator == 11) {
      compositionCohortDefinition.addSearch(
          "age",
          EptsReportUtils.map(
              ageCohortQueries.createXtoYAgeCohort(
                  "Crianças  (10-14 anos de idade) com registo de início da 1a linha de TARV ou novo regime da 1ª linha de TARV no mês de avaliação",
                  10,
                  14),
              "effectiveDate=${revisionEndDate}"));
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
        "DD", EptsReportUtils.map(getDeadPatientsCompositionMQ13(), MAPPING3));

    compositionCohortDefinition.addSearch("E", EptsReportUtils.map(transferredIn, MAPPING));

    compositionCohortDefinition.addSearch("F", EptsReportUtils.map(transfOut, MAPPING11));

    compositionCohortDefinition.addSearch(
        "ABANDONEDTARV", EptsReportUtils.map(abandonedTarv, MAPPING1));

    compositionCohortDefinition.addSearch(
        "ABANDONED1LINE", EptsReportUtils.map(abandonedFirstLine, MAPPING1));

    compositionCohortDefinition.addSearch(
        "ABANDONED2LINE", EptsReportUtils.map(abandonedSecondLine, MAPPING1));

    compositionCohortDefinition.addSearch(
        "tbDiagnosisActive", EptsReportUtils.map(tbDiagnosisActive, MAPPING));

    if (indicator == 2 || indicator == 9 || indicator == 10 || indicator == 11)
      compositionCohortDefinition.setCompositionString(
          "((A AND NOT C) OR B1) AND NOT (F OR E OR DD OR ABANDONEDTARV OR tbDiagnosisActive) AND age");
    if (indicator == 5 || indicator == 14)
      compositionCohortDefinition.setCompositionString(
          "B2New AND NOT (F OR E OR DD OR ABANDONEDTARV OR tbDiagnosisActive) AND age");
    return compositionCohortDefinition;
  }

  /**
   *
   *
   * <ul>
   *   <li>B1E- Select all patients from Ficha Clinica (encounter type 6) who have “LINHA
   *       TERAPEUTICA”(Concept id 21151) with value coded DIFFERENT THAN “PRIMEIRA LINHA”(Concept
   *       id 21150) registered in the LAST consultation (encounter type 6) by endDateRevision
   *   <li>B2E- Select all patients from Ficha Clinica (encounter type 6) who have “LINHA
   *       TERAPEUTICA”(Concept id 21151) with value coded DIFFERENT THAN “SEGUNDA LINHA”(Concept id
   *       21148) registered in the LAST consultation (encounter type 6) by endDateRevision
   * </ul>
   *
   * @param b1e Boolean parameter, true for b1e and false for b2e
   * @return CohortDefinition
   */
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
   *   <li>B2- Select all patients from Ficha Clinica or Ficha Resumo(encounter type 6 or 53) with
   *       “Carga Viral” (Concept id 856) registered with numeric value >= 1000 during the Inclusion
   *       period (startDateInclusion and endDateInclusion). Note: if there is more than one record
   *       with value_numeric > 1000 than consider the first occurrence during the inclusion period.
   * </ul>
   *
   * @return CohortDefinition <strong>Should</strong> <strong>Should</strong> Returns empty if there
   *     is no patient who meets the conditions <strong>Should</strong> fetch all patients with B2
   *     criteria
   */
  public CohortDefinition getB2_13(boolean e53) {

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
            + "                    AND ("
            + "                         ( e.encounter_type = ${6} "
            + "                         AND e.encounter_datetime BETWEEN :startDate AND :endDate) ";

    if (e53) {
      query +=
          "                         OR (e.encounter_type = ${53} "
              + "                         AND o.obs_datetime BETWEEN :startDate AND :endDate)";
    }
    query +=
        "                   ) "
            + "               GROUP BY p.patient_id) filtered ON p.patient_id = filtered.patient_id ";

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
            + "       AND e.encounter_type = ${6}";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * B2- Select all patients from Ficha Clinica (encounter type 6) who have THE LAST “LINHA
   * TERAPEUTICA”(Concept id 21151) during the Inclusion period (startDateInclusion =
   * endDateRevision - 14 months and endDateInclusion = endDateRevision - 11 months) and the value
   * coded is “SEGUNDA LINHA”(Concept id 21148)
   *
   * @return CohortDefinition
   */
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
   * Primeira consulta de APSS/PP após a Data Início TARV, ocorrida entre 20 a 33 dias após o início
   * TARV (1ª “Data Consulta APSS/PP” >= “Data Início TARV” +20 Dias e <= “Data Início TARV” +
   * 33dias)
   *
   * @param minDays minimum number of days to check after Art Start Date
   * @param maxDays maximum number of days to check after Art Start Date
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getApssBetween20and33DaysAfterArtStart(Integer minDays, Integer maxDays) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "Primeira consulta de APSS/PP após a Data Início TARV, ocorrida entre 20 a 33 dias após o início TARV");

    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getHistoricalDrugStartDateConcept().getConceptId());
    map.put("minDays", minDays);
    map.put("maxDays", maxDays);

    String query =
        "SELECT p.patient_id "
            + "FROM patient p "
            + "         INNER JOIN encounter e "
            + "                    ON p.patient_id = e.patient_id "
            + "         INNER JOIN ( "
            + "    SELECT patient_id, art.art_date "
            + "    FROM   (SELECT p.patient_id, Min(value_datetime) art_date "
            + "            FROM patient p "
            + "                     INNER JOIN encounter e "
            + "                                ON p.patient_id = e.patient_id "
            + "                     INNER JOIN obs o "
            + "                                ON e.encounter_id = o.encounter_id "
            + "            WHERE  p.voided = 0 "
            + "              AND e.voided = 0 "
            + "              AND o.voided = 0 "
            + "              AND e.encounter_type = ${53} "
            + "              AND o.concept_id = ${1190} "
            + "              AND o.value_datetime IS NOT NULL "
            + "              AND o.value_datetime <= :endDate "
            + "              AND e.location_id = :location "
            + "            GROUP  BY p.patient_id  ) art "
            + "    WHERE  art.art_date BETWEEN :startDate AND :endDate "
            + ") art_start on art_start.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND e.encounter_type = ${35} "
            + "  AND e.location_id = :location "
            + "  AND e.encounter_datetime > art_start.art_date "
            + "  AND e.encounter_datetime BETWEEN DATE_ADD(art_start.art_date, INTERVAL ${minDays} DAY) "
            + "    AND DATE_ADD(art_start.art_date, INTERVAL ${maxDays} DAY) "
            + "GROUP  BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   *
   * <li>Primeira consulta de APSS/PP após a Data Início TARV, ocorrida entre 20 a 33 dias após o
   *     início TARV (1ª “Data Consulta APSS/PP” >= “Data Início TARV” +20 Dias e <= “Data Início
   *     TARV” + 33dias) e
   *
   * @param minDays minimum number of days to check after Art Start Date
   * @param maxDays maximum number of days to check after Art Start Date
   * @return {@link CohortDefinition}
   */
  public CohortDefinition get1stApssBetween20and33DaysAfterArtStart(
      Integer minDays, Integer maxDays) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "Primeira consulta de APSS/PP após a Data Início TARV, ocorrida entre 20 a 33 dias após o início TARV");

    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getHistoricalDrugStartDateConcept().getConceptId());
    map.put("minDays", minDays);
    map.put("maxDays", maxDays);

    String query =
        "SELECT first_apss.patient_id "
            + "FROM  (SELECT p.patient_id, "
            + "              Min(e.encounter_datetime) AS first_encounter "
            + "       FROM   patient p "
            + "                  INNER JOIN encounter e "
            + "                             ON p.patient_id = e.patient_id "
            + "                  INNER JOIN (SELECT patient_id, "
            + "                                     art.art_date "
            + "                              FROM   ("
            + QualityImprovement2020Queries.getArtStartDate()
            + ") art "
            + "                              WHERE  art.art_date BETWEEN :startDate AND :endDate) "
            + "           art_start "
            + "                             ON art_start.patient_id = p.patient_id "
            + "       WHERE  p.voided = 0 "
            + "         AND e.voided = 0 "
            + "         AND e.encounter_type = ${35} "
            + "         AND e.location_id = :location "
            + "         AND e.encounter_datetime > art_start.art_date "
            + "       GROUP  BY p.patient_id) first_apss "
            + "          INNER JOIN (SELECT patient_id, "
            + "                             art.art_date "
            + "                      FROM   ("
            + QualityImprovement2020Queries.getArtStartDate()
            + ") art "
            + "                      WHERE  art.art_date BETWEEN :startDate AND :endDate) art_start "
            + "                     ON art_start.patient_id = first_apss.patient_id "
            + "WHERE  first_apss.first_encounter BETWEEN Date_add(art_start.art_date, INTERVAL "
            + "                                                   ${minDays} day) "
            + "           AND "
            + "           Date_add(art_start.art_date, INTERVAL "
            + "                    ${maxDays} day)";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   *
   * <li>Pelo menos uma consulta de APSS/PP, ocorrida entre 20 a 33 dias após o a primeira consulta
   *     de APSS/PP (2ª “Data Consulta APSS/PP” >= 1ª “Data Consulta APSS/PP” +20 Dias e <= 1ª “Data
   *     Consulta APSS/PP” + 33dias)<b>Nota: caso existir mais que uma consulta de APSS/PP no
   *     período compreendido entre 20 e 33 dias após a primeira consulta de APSS/PP, considerar a
   *     primeira ocorrência neste período como 2ª “Data Consulta APSS/PP”.</b>
   *
   * @param minDays minimum number of days to check after Art Start Date
   * @param maxDays maximum number of days to check after Art Start Date
   * @return {@link CohortDefinition}
   */
  public CohortDefinition get2ndApssBetween20and33DaysAfterArtStart(
      Integer minDays, Integer maxDays) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "Segunda consulta de APSS/PP após a Data Início TARV, ocorrida entre 20 a 33 dias após o início TARV");

    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getHistoricalDrugStartDateConcept().getConceptId());
    map.put("minDays", minDays);
    map.put("maxDays", maxDays);

    String query =
        "SELECT second_apss.patient_id "
            + "FROM   ( "
            + "                  SELECT     p.patient_id, "
            + "                             Min(e.encounter_datetime) AS second_encounter "
            + "                  FROM       patient p "
            + "                  INNER JOIN encounter e "
            + "                  ON         p.patient_id = e.patient_id "
            + "                  INNER JOIN "
            + "                             ( "
            + "                                        SELECT     first_apss.patient_id, "
            + "                                                   first_apss.first_encounter "
            + "                                        FROM       ( "
            + "                                                              SELECT     p.patient_id, "
            + "                                                                         Min(e.encounter_datetime) AS first_encounter "
            + "                                                              FROM       patient p "
            + "                                                              INNER JOIN encounter e "
            + "                                                              ON         p.patient_id = e.patient_id "
            + "                                                              INNER JOIN ( "
            + "                                                                                SELECT patient_id, "
            + "                                                                    art.art_date "
            + "                                                             FROM ( "
            + QualityImprovement2020Queries.getArtStartDate()
            + "                                                                     ) art "
            + "                                                                                WHERE  art.art_date BETWEEN :startDate AND    :endDate) art_start "
            + "                                                              ON         art_start.patient_id = p.patient_id "
            + "                                                              WHERE      p.voided = 0 "
            + "                                                              AND        e.voided = 0 "
            + "                                                              AND        e.encounter_type = ${35} "
            + "                                                              AND        e.location_id = :location "
            + "                                                              AND        e.encounter_datetime > art_start.art_date "
            + "                                                              GROUP BY   p.patient_id) first_apss "
            + "                                        INNER JOIN ( "
            + "                                                                                SELECT patient_id, "
            + "                                                                    art.art_date "
            + "                                                             FROM ( "
            + QualityImprovement2020Queries.getArtStartDate()
            + "                                                                     ) art "
            + "                                                          WHERE  art.art_date BETWEEN :startDate AND    :endDate) art_start "
            + "                                        ON         art_start.patient_id = first_apss.patient_id "
            + "                                        WHERE      first_apss.first_encounter BETWEEN date_add( art_start.art_date, INTERVAL ${minDays} day) AND        date_add(art_start.art_date, INTERVAL ${maxDays} day)) first_apss "
            + "                  ON         first_apss.patient_id = p.patient_id "
            + "                  WHERE      p.voided = 0 "
            + "                  AND        e.voided = 0 "
            + "                  AND        e.encounter_type = ${35} "
            + "                  AND        e.location_id = :location "
            + "                  AND        e.encounter_datetime > first_apss.first_encounter "
            + "                  AND        e.encounter_datetime BETWEEN date_add(first_apss.first_encounter, INTERVAL ${minDays} day) AND        date_add(first_apss.first_encounter, INTERVAL ${maxDays} day) "
            + "                  GROUP BY   p.patient_id) second_apss";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   *
   * <li>Pelo menos uma consulta de APSS/PP, ocorrida entre 20 a 33 dias após o a segunda consulta
   *     de APSS/PP registada no ponto anterior (3ª “Data Consulta APSS/PP” >= 2ª “Data Consulta
   *     APSS/PP” +20 Dias e <= 2ª “Data Consulta APSS/PP” + 33dias).</b>
   * <li><b>Nota:</b>“Data Início TARV” é a data definida segundo os critérios do RF5.
   *
   * @param minDays minimum number of days to check after Art Start Date
   * @param maxDays maximum number of days to check after Art Start Date
   * @return {@link CohortDefinition}
   */
  public CohortDefinition get3rdApssBetween20and33DaysAfterArtStart(
      Integer minDays, Integer maxDays) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "Terceira consulta de APSS/PP após a Data Início TARV, ocorrida entre 20 a 33 dias após o início TARV");

    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getHistoricalDrugStartDateConcept().getConceptId());
    map.put("minDays", minDays);
    map.put("maxDays", maxDays);

    String query =
        "SELECT third_apss.patient_id "
            + "FROM   ( "
            + "                  SELECT     p.patient_id, "
            + "                             Min(e.encounter_datetime) AS third_encounter "
            + "                  FROM       patient p "
            + "                  INNER JOIN encounter e "
            + "                  ON         p.patient_id = e.patient_id "
            + "                  INNER JOIN "
            + "                             ( "
            + "                                        SELECT     p.patient_id, "
            + "                                                   Min(e.encounter_datetime) AS second_encounter "
            + "                                        FROM       patient p "
            + "                                        INNER JOIN encounter e "
            + "                                        ON         p.patient_id = e.patient_id "
            + "                                        INNER JOIN "
            + "                                                   ( "
            + "                                                              SELECT     first_apss.patient_id, "
            + "                                                                         first_apss.first_encounter "
            + "                                                              FROM       ( "
            + "                                                                                    SELECT     p.patient_id, "
            + "                                                                                               Min(e.encounter_datetime) AS first_encounter "
            + "                                                                                    FROM       patient p "
            + "                                                                                    INNER JOIN encounter e "
            + "                                                                                    ON         p.patient_id = e.patient_id "
            + "                                                                                    INNER JOIN ( "
            + "                                                                                SELECT patient_id, "
            + "                                                                    art.art_date "
            + "                                                             FROM ( "
            + QualityImprovement2020Queries.getArtStartDate()
            + "                                                                     ) art "
            + "                                                                                                      WHERE  art.art_date BETWEEN :startDate AND    :endDate) art_start "
            + "                                                                                    ON         art_start.patient_id = p.patient_id "
            + "                                                                                    WHERE      p.voided = 0 "
            + "                                                                                    AND        e.voided = 0 "
            + "                                                                                    AND        e.encounter_type = ${35} "
            + "                                                                                    AND        e.location_id = :location "
            + "                                                                                    AND        e.encounter_datetime > art_start.art_date "
            + "                                                                                    GROUP BY   p.patient_id) first_apss "
            + "                                                              INNER JOIN ( "
            + "                                                                                SELECT patient_id, "
            + "                                                                    art.art_date "
            + "                                                             FROM ( "
            + QualityImprovement2020Queries.getArtStartDate()
            + "                                                                     ) art "
            + "                                                                                WHERE  art.art_date BETWEEN :startDate AND    :endDate ) art_start "
            + "                                                              ON         art_start.patient_id = first_apss.patient_id "
            + "                                                              WHERE      first_apss.first_encounter BETWEEN date_add( art_start.art_date, INTERVAL ${minDays} day) AND        date_add(art_start.art_date, INTERVAL ${maxDays} day)) first_apss "
            + "                                        ON         first_apss.patient_id = p.patient_id "
            + "                                        WHERE      p.voided = 0 "
            + "                                        AND        e.voided = 0 "
            + "                                        AND        e.encounter_type = ${35} "
            + "                                        AND        e.location_id = :location "
            + "                                        AND        e.encounter_datetime > first_apss.first_encounter "
            + "                                        AND        e.encounter_datetime BETWEEN date_add(first_apss.first_encounter, INTERVAL ${minDays} day ) AND        date_add(first_apss.first_encounter, INTERVAL ${maxDays} day ) "
            + "                                        GROUP BY   p.patient_id) second_apss "
            + "                  ON         second_apss.patient_id = p.patient_id "
            + "                  WHERE      p.voided = 0 "
            + "                  AND        e.voided = 0 "
            + "                  AND        e.encounter_type = ${35} "
            + "                  AND        e.location_id = :location "
            + "                  AND        e.encounter_datetime > second_apss.second_encounter "
            + "                  AND        e.encounter_datetime BETWEEN date_add(second_apss.second_encounter, INTERVAL ${minDays} day ) AND        date_add(second_apss.second_encounter, INTERVAL ${maxDays} day ) "
            + "                  GROUP BY   p.patient_id) third_apss";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * Pelo menos uma consulta de APSS/PP, ocorrida entre 20 a 33 dias após a primeira consulta de
   * APSS/PP (2ª “Data Consulta APSS/PP” >= 1ª “Data Consulta APSS/PP” +20 Dias e <= 1ª “Data
   * Consulta APSS/PP” + 33dias).
   *
   * @param minDays minimum number of days to check after first apss consultation Date
   * @param maxDays maximum number of days to check after first apss consultation Date
   * @see #getApssBetween20and33DaysAfterArtStart(Integer, Integer)
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getApssBetween20and33DaysAfterFirstApss(
      Integer minDays, Integer maxDays) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "Pelo menos uma consulta de APSS/PP, ocorrida entre 20 a 33 dias após a primeira consulta de APSS/PP");

    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getHistoricalDrugStartDateConcept().getConceptId());
    map.put("minDays", minDays);
    map.put("maxDays", maxDays);

    String query =
        "SELECT p.patient_id "
            + "FROM patient p "
            + "         INNER JOIN encounter e "
            + "                    ON p.patient_id = e.patient_id "
            + "         INNER JOIN ( "
            + "    SELECT p.patient_id, MIN(e.encounter_datetime) as first_consultation "
            + "     FROM patient p "
            + "         INNER JOIN encounter e "
            + "                    ON p.patient_id = e.patient_id "
            + "         INNER JOIN ( "
            + "           SELECT patient_id, art.art_date "
            + "           FROM   (SELECT p.patient_id, Min(value_datetime) art_date "
            + "            FROM patient p "
            + "                     INNER JOIN encounter e "
            + "                                ON p.patient_id = e.patient_id "
            + "                     INNER JOIN obs o "
            + "                                ON e.encounter_id = o.encounter_id "
            + "            WHERE  p.voided = 0 "
            + "              AND e.voided = 0 "
            + "              AND o.voided = 0 "
            + "              AND e.encounter_type = ${53} "
            + "              AND o.concept_id = ${1190} "
            + "              AND o.value_datetime IS NOT NULL "
            + "              AND o.value_datetime <= :endDate "
            + "              AND e.location_id = :location "
            + "            GROUP  BY p.patient_id  ) art "
            + "         WHERE  art.art_date BETWEEN :startDate AND :endDate "
            + ") art_start on art_start.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND e.encounter_type = ${35} "
            + "  AND e.location_id = :location "
            + "  AND e.encounter_datetime > art_start.art_date "
            + "  AND e.encounter_datetime BETWEEN DATE_ADD(art_start.art_date, INTERVAL ${minDays} DAY) "
            + "    AND DATE_ADD(art_start.art_date, INTERVAL ${maxDays} DAY) "
            + "GROUP  BY p.patient_id "
            + ") first_apss on first_apss.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND e.encounter_type = ${35} "
            + "  AND e.location_id = :location "
            + "  AND e.encounter_datetime > first_apss.first_consultation "
            + "  AND e.encounter_datetime BETWEEN DATE_ADD(first_apss.first_consultation, INTERVAL ${minDays} DAY) "
            + "    AND DATE_ADD(first_apss.first_consultation, INTERVAL ${maxDays} DAY) "
            + "GROUP  BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * Pelo menos uma consulta de APSS/PP, ocorrida entre 20 a 33 dias após a segunda consulta de
   * APSS/PP registada no ponto anterior (3ª “Data Consulta APSS/PP” >= 2ª “Data Consulta APSS/PP”
   * +20 Dias e <= 2ª “Data Consulta APSS/PP” + 33dias)
   *
   * @param minDays minimum number of days to check after second apss consultation Date
   * @param maxDays maximum number of days to check after second apss consultation Date
   * @see #getApssBetween20and33DaysAfterFirstApss(Integer, Integer)
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getApssBetween20and33DaysAfterSecondApss(
      Integer minDays, Integer maxDays) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "Pelo menos uma consulta de APSS/PP, ocorrida entre 20 a 33 dias após a segunda consulta de APSS/PP");

    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getHistoricalDrugStartDateConcept().getConceptId());
    map.put("minDays", minDays);
    map.put("maxDays", maxDays);

    String query =
        "SELECT p.patient_id "
            + "FROM patient p "
            + "         INNER JOIN encounter e "
            + "                    ON p.patient_id = e.patient_id "
            + "         INNER JOIN ( "
            + "SELECT p.patient_id, MIN(e.encounter_datetime) AS second_consultation "
            + "FROM patient p "
            + "         INNER JOIN encounter e "
            + "                    ON p.patient_id = e.patient_id "
            + "         INNER JOIN ( "
            + "    SELECT p.patient_id, MIN(e.encounter_datetime) as first_consultation "
            + "     FROM patient p "
            + "         INNER JOIN encounter e "
            + "                    ON p.patient_id = e.patient_id "
            + "         INNER JOIN ( "
            + "           SELECT patient_id, art.art_date "
            + "           FROM   (SELECT p.patient_id, Min(value_datetime) art_date "
            + "            FROM patient p "
            + "                     INNER JOIN encounter e "
            + "                                ON p.patient_id = e.patient_id "
            + "                     INNER JOIN obs o "
            + "                                ON e.encounter_id = o.encounter_id "
            + "            WHERE  p.voided = 0 "
            + "              AND e.voided = 0 "
            + "              AND o.voided = 0 "
            + "              AND e.encounter_type = ${53} "
            + "              AND o.concept_id = ${1190} "
            + "              AND o.value_datetime IS NOT NULL "
            + "              AND o.value_datetime <= :endDate "
            + "              AND e.location_id = :location "
            + "            GROUP  BY p.patient_id  ) art "
            + "         WHERE  art.art_date BETWEEN :startDate AND :endDate "
            + ") art_start on art_start.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND e.encounter_type = ${35} "
            + "  AND e.location_id = :location "
            + "  AND e.encounter_datetime > art_start.art_date "
            + "  AND e.encounter_datetime BETWEEN DATE_ADD(art_start.art_date, INTERVAL ${minDays} DAY) "
            + "    AND DATE_ADD(art_start.art_date, INTERVAL ${maxDays} DAY) "
            + "GROUP  BY p.patient_id "
            + ") first_apss on first_apss.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND e.encounter_type = ${35} "
            + "  AND e.location_id = :location "
            + "  AND e.encounter_datetime > first_apss.first_consultation "
            + "  AND e.encounter_datetime BETWEEN DATE_ADD(first_apss.first_consultation, INTERVAL ${minDays} DAY) "
            + "    AND DATE_ADD(first_apss.first_consultation, INTERVAL ${maxDays} DAY) "
            + "GROUP  BY p.patient_id "
            + ") second_apss on second_apss.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND e.encounter_type = ${35} "
            + "  AND e.location_id = :location "
            + "  AND e.encounter_datetime > second_apss.second_consultation "
            + "  AND e.encounter_datetime BETWEEN DATE_ADD(second_apss.second_consultation, INTERVAL ${minDays} DAY) "
            + "    AND DATE_ADD(second_apss.second_consultation, INTERVAL ${maxDays} DAY) "
            + "GROUP  BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>Utentes com 3 Consultas de APSS/PP mensais dentro de 99 dias</b>
   * <li>Primeira consulta de APSS/PP após a Data Início TARV, ocorrida entre 20 a 33 dias após o
   *     início TARV (1ª “Data Consulta APSS/PP” >= “Data Início TARV” +20 Dias e <= “Data Início
   *     TARV” + 33dias) e
   * <li>Pelo menos uma consulta de APSS/PP, ocorrida entre 20 a 33 dias após a primeira consulta de
   *     APSS/PP (2ª “Data Consulta APSS/PP” >= 1ª “Data Consulta APSS/PP” +20 Dias e <= 1ª “Data
   *     Consulta APSS/PP” + 33dias) e
   * <li>Pelo menos uma consulta de APSS/PP, ocorrida entre 20 a 33 dias após a segunda consulta de
   *     APSS/PP registada no ponto anterior (3ª “Data Consulta APSS/PP” >= 2ª “Data Consulta
   *     APSS/PP” +20 Dias e <= 2ª “Data Consulta APSS/PP” + 33dias)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWith3MonthlyApssConsultationsWithin99Days() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName(
        "Utentes com 3 Consultas de APSS/PP mensais dentro de 99 dias");
    compositionCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition firstApss = get1stApssBetween20and33DaysAfterArtStart(20, 33);

    CohortDefinition secondApss = get2ndApssBetween20and33DaysAfterArtStart(20, 33);

    CohortDefinition thirdApss = get3rdApssBetween20and33DaysAfterArtStart(20, 33);

    compositionCohortDefinition.addSearch(
        "firstApss",
        EptsReportUtils.map(
            firstApss, "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "secondApss",
        EptsReportUtils.map(
            secondApss, "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "thirdApss",
        EptsReportUtils.map(
            thirdApss, "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.setCompositionString("firstApss AND secondApss AND thirdApss");

    return compositionCohortDefinition;
  }

  /**
   * H1 - One Consultation (Encounter_datetime (from encounter type 35)) on the same date when the
   * Viral Load with >=1000 result was recorded (oldest date from B2)
   *
   * @param vlQuantity Quantity of viral load to evaluate
   * @return CohortDefinition
   */
  public CohortDefinition getMQC11NH1(int vlQuantity) {
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
            + "                        AND o.concept_id = ${856} AND o.value_numeric >= "
            + vlQuantity
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

  /**
   * H2- Another consultation (Encounter_datetime (from encounter type 35)) >= “1st consultation”
   * (oldest date from H1)+20 days and <=“1st consultation” (oldest date from H1)+33days
   *
   * @param vlQuantity Quantity of viral load to evaluate
   * @return CohortDefinition
   */
  public CohortDefinition getMQC11NH2(int vlQuantity) {
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
            + "                                        AND o.concept_id = ${856} AND o.value_numeric > "
            + vlQuantity
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
            + "    AND e.encounter_datetime >= DATE_ADD(h1.encounter_datetime, INTERVAL 20 DAY)  "
            + "         AND e.encounter_datetime <= DATE_ADD(h1.encounter_datetime, INTERVAL 33 DAY) "
            + "    AND e.location_id = :location ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * H3- Another consultation (Encounter_datetime (from encounter type 35)) >= “2nd consultation”
   * (oldest date from H2)+20 days and <=“2nd consultation” (oldest date from H2)+33days
   *
   * @param vlQuantity Quantity of viral load to evaluate
   * @return CohortDefinition
   */
  public CohortDefinition getMQC11NH3(int vlQuantity) {
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
            + "                                                            AND o.concept_id = ${856} AND o.value_numeric > "
            + vlQuantity
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
            + "                        AND e.encounter_datetime >= DATE_ADD(h1.encounter_datetime, INTERVAL 20 DAY)  "
            + "                             AND e.encounter_datetime <= DATE_ADD(h1.encounter_datetime, INTERVAL 33 DAY) "
            + "                        AND e.location_id = :location "
            + "                ) h2 ON h2.patient_id = p.patient_id "
            + " WHERE p.voided = 0  "
            + "    AND e.voided = 0 "
            + "    AND e.encounter_type = ${35} "
            + "    AND e.encounter_datetime >= DATE_ADD(h2.encounter_datetime, INTERVAL 20 DAY)  "
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
   *       the Viral Load with >= 1000 result was recorded (oldest date from B2) AND
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
  public CohortDefinition getMQC11NH(boolean numerator4) {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.addParameter(new Parameter("startDate", "Start date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End date", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    compositionCohortDefinition.setName("Category 11 Numerator session G");

    CohortDefinition h1;
    CohortDefinition h2;
    CohortDefinition h3;

    if (numerator4) {
      h1 = getMQC11NH1(50);
      h2 = getMQC11NH2(50);
      h3 = getMQC11NH3(50);
    } else {
      h1 = getMQC11NH1(1000);
      h2 = getMQC11NH2(1000);
      h3 = getMQC11NH3(1000);
    }

    String mapping = "startDate=${startDate},endDate=${endDate},location=${location}";

    compositionCohortDefinition.addSearch("h1", EptsReportUtils.map(h1, mapping));
    compositionCohortDefinition.addSearch("h2", EptsReportUtils.map(h2, mapping));
    compositionCohortDefinition.addSearch("h3", EptsReportUtils.map(h3, mapping));

    compositionCohortDefinition.setCompositionString("h1 AND h2 AND h3");

    return compositionCohortDefinition;
  }

  public CohortDefinition getMQC11NH() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.addParameter(new Parameter("startDate", "Start date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End date", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    compositionCohortDefinition.addSearch(
        "indicator", Mapped.mapStraightThrough(getMQC11NH(false)));

    compositionCohortDefinition.setCompositionString("indicator");

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
        "Category 11 - numerator - Session I - Interval of 99 Days for APSS consultations after ART start date");
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
   *
   * @param reportResource report Resource (MQ or MI)
   * @return CohortDefinition
   */
  public CohortDefinition getMQC11NumAnotCnotDnotEnotFandGAdultss(MIMQ reportResource) {
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

    CohortDefinition f = intensiveMonitoringCohortQueries.getTranferredOutPatientsForMI7();
    CohortDefinition g = getPatientsWith3MonthlyApssConsultationsWithin99Days();

    if (reportResource.equals(EptsReportConstants.MIMQ.MQ)) {
      compositionCohortDefinition.addSearch("A", EptsReportUtils.map(a, MAPPING));
      compositionCohortDefinition.addSearch("C", EptsReportUtils.map(c, MAPPING));
      compositionCohortDefinition.addSearch("D", EptsReportUtils.map(d, MAPPING));
      compositionCohortDefinition.addSearch("E", EptsReportUtils.map(e, MAPPING));
      compositionCohortDefinition.addSearch("F", EptsReportUtils.map(f, MAPPING14));
      compositionCohortDefinition.addSearch("G", EptsReportUtils.map(g, MAPPING1));
    } else if (reportResource.equals(EptsReportConstants.MIMQ.MI)) {
      compositionCohortDefinition.addSearch("A", EptsReportUtils.map(a, MAPPING4));
      compositionCohortDefinition.addSearch("C", EptsReportUtils.map(c, MAPPING6));
      compositionCohortDefinition.addSearch("D", EptsReportUtils.map(d, MAPPING6));
      compositionCohortDefinition.addSearch("E", EptsReportUtils.map(e, MAPPING6));
      compositionCohortDefinition.addSearch("F", EptsReportUtils.map(f, MAPPING14));
      compositionCohortDefinition.addSearch("G", EptsReportUtils.map(g, MAPPING10));
    }

    compositionCohortDefinition.setCompositionString("(((A OR D) AND G) AND NOT (C OR E OR F))");

    return compositionCohortDefinition;
  }

  /**
   * 11.2. % de pacientes na 1a linha de TARV com CV acima de 1000 cópias que tiveram 3 consultas de
   * APSS/PP mensais consecutivas para reforço de adesão (Line 57 in the template) Numerador (Column
   * D in the Template) as following: <code>
   * B1 and B2 and NOT C and NOT B5 and NOT E and NOT F AND H and  Age > 14*</code>
   *
   * @param reportSource report Resource (MQ or MI)
   * @return CohortDefinition
   */
  public CohortDefinition getMQC11NumB1nB2notCnotDnotEnotEnotFnHandAdultss(MIMQ reportSource) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    boolean useE53 = false;

    compositionCohortDefinition.setName("Category 11 : Numeraror 11.2 ");

    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    if (reportSource.equals(EptsReportConstants.MIMQ.MQ)) {

      compositionCohortDefinition.addSearch(
          "DENOMINATOR",
          EptsReportUtils.map(getMQC11DEN(2, EptsReportConstants.MIMQ.MQ), MAPPING1));

      compositionCohortDefinition.addSearch("H", EptsReportUtils.map(getMQC11NH(), MAPPING));

    } else if (reportSource.equals(EptsReportConstants.MIMQ.MI)) {

      compositionCohortDefinition.addSearch(
          "DENOMINATOR",
          EptsReportUtils.map(getMQC11DEN(2, EptsReportConstants.MIMQ.MI), MAPPING1));

      compositionCohortDefinition.addSearch("H", EptsReportUtils.map(getMQC11NH(), MAPPING8));
    }

    compositionCohortDefinition.setCompositionString("DENOMINATOR AND H");

    return compositionCohortDefinition;
  }

  /**
   * 11.3.% de MG em TARV com o mínimo de 3 consultas de seguimento de adesão na FM-ficha de APSS/PP
   * nos primeiros 3 meses após início do TARV (Line 58 in the template) Numerador (Column D in the
   * Template) as following: <code> A  and  C and NOT D and NOT E and NOT F  AND G </code>
   *
   * @param reportSource report Resource (MQ or MI)
   * @return CohortDefinition
   */
  public CohortDefinition getMQC11NumAnB3nCnotDnotEnotEnotFnG(MIMQ reportSource) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName("Category 11 : Numeraror 11.3");

    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition g = getPatientsWith3MonthlyApssConsultationsWithin99Days();

    if (reportSource.equals(EptsReportConstants.MIMQ.MQ)) {
      compositionCohortDefinition.addSearch(
          "DENOMINATOR",
          EptsReportUtils.map(getMQC11DEN(3, EptsReportConstants.MIMQ.MQ), MAPPING1));
      compositionCohortDefinition.addSearch("G", EptsReportUtils.map(g, MAPPING1));
    } else if (reportSource.equals(EptsReportConstants.MIMQ.MI)) {
      compositionCohortDefinition.addSearch(
          "DENOMINATOR",
          EptsReportUtils.map(
              getMQC11DEN(3, EptsReportConstants.MIMQ.MI),
              "revisionEndDate=${revisionEndDate},location=${location}"));
      compositionCohortDefinition.addSearch("G", EptsReportUtils.map(g, MAPPING10));
    }

    compositionCohortDefinition.setCompositionString("DENOMINATOR AND G");

    return compositionCohortDefinition;
  }

  /**
   * 11.4. % de MG na 1a linha de TARV com CV acima de 1000 cópias que tiveram 3 consultas de
   * APSS/PP mensais consecutivas para reforço de adesão (Line 59 in the template) Numerador (Column
   * D in the Template) as following: <code>
   *  B1 and B2 and B5 and NOT E and NOT F AND H </code>
   *
   * @param reportSource report Resource (MQ or MI)
   * @return CohortDefinition
   */
  public CohortDefinition getMQC11NumB1nB2nB3nCnotDnotEnotEnotFnH(MIMQ reportSource) {
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
            hivMetadata.getHivViralLoadConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId(),
            commonMetadata.getPregnantConcept().getConceptId(),
            50);

    CohortDefinition b5 =
        QualityImprovement2020Queries.getMQ13DenB5_P4(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getHivViralLoadConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId(),
            commonMetadata.getBreastfeeding().getConceptId(),
            50);

    CohortDefinition c =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition f = intensiveMonitoringCohortQueries.getTranferredOutPatientsForMI7();
    CohortDefinition h = getMQC11NH(true);

    if (reportSource.equals(EptsReportConstants.MIMQ.MQ)) {
      compositionCohortDefinition.addSearch("B1", EptsReportUtils.map(b1, MAPPING1));
      compositionCohortDefinition.addSearch("B4", EptsReportUtils.map(b4, MAPPING1));
      compositionCohortDefinition.addSearch("B5", EptsReportUtils.map(b5, MAPPING));
      compositionCohortDefinition.addSearch("C", EptsReportUtils.map(c, MAPPING));
      compositionCohortDefinition.addSearch("F", EptsReportUtils.map(f, MAPPING14));
      compositionCohortDefinition.addSearch("H", EptsReportUtils.map(h, MAPPING));
    } else if (reportSource.equals(EptsReportConstants.MIMQ.MI)) {
      compositionCohortDefinition.addSearch("B1", EptsReportUtils.map(b1, MAPPING8));
      compositionCohortDefinition.addSearch("B4", EptsReportUtils.map(b4, MAPPING8));
      compositionCohortDefinition.addSearch("B5", EptsReportUtils.map(b5, MAPPING8));
      compositionCohortDefinition.addSearch("C", EptsReportUtils.map(c, MAPPING));
      compositionCohortDefinition.addSearch("F", EptsReportUtils.map(f, MAPPING14));
      compositionCohortDefinition.addSearch("H", EptsReportUtils.map(h, MAPPING8));
    }

    compositionCohortDefinition.setCompositionString("((B1 AND B4 AND H) AND NOT F)");

    return compositionCohortDefinition;
  }

  /**
   * 11.5. % de crianças >2 anos de idade em TARV com registo mensal de seguimento da adesão na
   * ficha de APSS/PP nos primeiros 99 dias de TARV (Line 60 in the template) Numerador (Column D in
   * the Template) as following: <code>
   * A and NOT C and NOT D and NOT E and NOT F  AND G and Age BETWEEN 2 AND 14*</code>
   *
   * @param reportSource report Resource (MQ or MI)
   * @return CohortDefinition
   */
  public CohortDefinition getMQC11NumAnotCnotDnotEnotFnotGnChildren(MIMQ reportSource) {
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
    CohortDefinition f = intensiveMonitoringCohortQueries.getTranferredOutPatientsForMI7();
    CohortDefinition g = getPatientsWith3MonthlyApssConsultationsWithin99Days();

    if (reportSource.equals(EptsReportConstants.MIMQ.MQ)) {
      compositionCohortDefinition.addSearch("A", EptsReportUtils.map(a, MAPPING));
      compositionCohortDefinition.addSearch("C", EptsReportUtils.map(c, MAPPING));
      compositionCohortDefinition.addSearch("D", EptsReportUtils.map(d, MAPPING));
      compositionCohortDefinition.addSearch("E", EptsReportUtils.map(e, MAPPING));
      compositionCohortDefinition.addSearch("F", EptsReportUtils.map(f, MAPPING14));
      compositionCohortDefinition.addSearch("G", EptsReportUtils.map(g, MAPPING1));
    } else if (reportSource.equals(EptsReportConstants.MIMQ.MI)) {
      compositionCohortDefinition.addSearch("A", EptsReportUtils.map(a, MAPPING4));
      compositionCohortDefinition.addSearch("C", EptsReportUtils.map(c, MAPPING6));
      compositionCohortDefinition.addSearch("D", EptsReportUtils.map(d, MAPPING6));
      compositionCohortDefinition.addSearch("E", EptsReportUtils.map(e, MAPPING6));
      compositionCohortDefinition.addSearch("F", EptsReportUtils.map(f, MAPPING14));
      compositionCohortDefinition.addSearch("G", EptsReportUtils.map(g, MAPPING10));
    }

    compositionCohortDefinition.setCompositionString("((A AND G) AND NOT (C OR D OR E OR F))");

    return compositionCohortDefinition;
  }
  /**
   * 11.6. % de crianças <2 anos de idade em TARV com registo mensal de seguimento da adesão na
   * ficha de APSS/PP no primeiro ano de TARV (Line 61 in the template) Numerador (Column D in the
   * Template) as following: <code>
   *  A and NOT C and NOT D and NOT E and NOT F AND I  AND Age  <= 9 MONTHS</code>
   *
   * @param reportSource report Resource (MQ or MI)
   * @return CohortDefinition
   */
  public CohortDefinition getMQC11NumAnotCnotDnotEnotFnotIlessThan9Month(MIMQ reportSource) {
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
    CohortDefinition f = intensiveMonitoringCohortQueries.getTranferredOutPatientsForMI7();
    CohortDefinition i = getPatientWhoHadThreeApssAfterArtStart();
    CohortDefinition babies = genericCohortQueries.getAgeInMonths(0, 9);

    if (reportSource.equals(EptsReportConstants.MIMQ.MQ)) {
      compositionCohortDefinition.addSearch("A", EptsReportUtils.map(a, MAPPING));
      compositionCohortDefinition.addSearch("C", EptsReportUtils.map(c, MAPPING));
      compositionCohortDefinition.addSearch("D", EptsReportUtils.map(d, MAPPING));
      compositionCohortDefinition.addSearch("E", EptsReportUtils.map(e, MAPPING));
      compositionCohortDefinition.addSearch("F", EptsReportUtils.map(f, MAPPING14));
      compositionCohortDefinition.addSearch(
          "I",
          EptsReportUtils.map(i, "startDate=${startDate},endDate=${endDate},location=${location}"));

    } else if (reportSource.equals(EptsReportConstants.MIMQ.MI)) {
      compositionCohortDefinition.addSearch("A", EptsReportUtils.map(a, MAPPING4));
      compositionCohortDefinition.addSearch("C", EptsReportUtils.map(c, MAPPING6));
      compositionCohortDefinition.addSearch("D", EptsReportUtils.map(d, MAPPING6));
      compositionCohortDefinition.addSearch("E", EptsReportUtils.map(e, MAPPING6));
      compositionCohortDefinition.addSearch("F", EptsReportUtils.map(f, MAPPING14));
      compositionCohortDefinition.addSearch(
          "I",
          EptsReportUtils.map(
              i,
              "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},location=${location}"));
    }

    compositionCohortDefinition.setCompositionString(
        "A and NOT C and NOT D and NOT E and NOT F AND I");

    return compositionCohortDefinition;
  }

  /**
   * 11.7. % de crianças (0-14 anos) na 1a linha de TARV com CV acima de 1000 cópias que tiveram 3
   * consultas mensais consecutivas de APSS/PP para reforço de adesão(Line 62 in the template)
   * Numerador (Column D in the Template) as following: <code>
   *  B1 and B2 and  NOT C and NOT B5 and NOT E and NOT F  And H and  Age < 15**</code>
   *
   * @param reportSource report Resource (MQ or MI)
   * @return CohortDefinition
   */
  public CohortDefinition getMQC11NumB1nB2notCnotDnotEnotFnHChildren(MIMQ reportSource) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    boolean useE53 = false;

    compositionCohortDefinition.setName("Category 11 : Numeraror 11.7");

    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition b1 = getPatientsFromFichaClinicaWithLastTherapeuticLineSetAsFirstLine_B1();

    CohortDefinition b2 = getB2_13(useE53);

    CohortDefinition b4 =
        QualityImprovement2020Queries.getMQ13DenB4_P4(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getHivViralLoadConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId(),
            commonMetadata.getPregnantConcept().getConceptId());

    CohortDefinition b5 =
        QualityImprovement2020Queries.getMQ13DenB5_P4(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getHivViralLoadConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId(),
            commonMetadata.getBreastfeeding().getConceptId());

    CohortDefinition f = intensiveMonitoringCohortQueries.getTranferredOutPatientsForMI7();
    CohortDefinition h = getMQC11NH();

    if (reportSource.equals(EptsReportConstants.MIMQ.MQ)) {
      compositionCohortDefinition.addSearch("B1", EptsReportUtils.map(b1, MAPPING1));
      compositionCohortDefinition.addSearch("B2", EptsReportUtils.map(b2, MAPPING1));
      compositionCohortDefinition.addSearch("B4", EptsReportUtils.map(b4, MAPPING));
      compositionCohortDefinition.addSearch("B5", EptsReportUtils.map(b5, MAPPING));
      compositionCohortDefinition.addSearch("F", EptsReportUtils.map(f, MAPPING14));
      compositionCohortDefinition.addSearch("H", EptsReportUtils.map(h, MAPPING));
    } else if (reportSource.equals(EptsReportConstants.MIMQ.MI)) {
      compositionCohortDefinition.addSearch("B1", EptsReportUtils.map(b1, MAPPING5));
      compositionCohortDefinition.addSearch("B2", EptsReportUtils.map(b2, MAPPING8));
      compositionCohortDefinition.addSearch("B4", EptsReportUtils.map(b4, MAPPING8));
      compositionCohortDefinition.addSearch("B5", EptsReportUtils.map(b5, MAPPING8));
      compositionCohortDefinition.addSearch("F", EptsReportUtils.map(f, MAPPING14));
      compositionCohortDefinition.addSearch("H", EptsReportUtils.map(h, MAPPING8));
    }

    compositionCohortDefinition.setCompositionString(
        "B1 AND B2 AND NOT B5 AND NOT F AND NOT B4 AND H");

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
   * @param den indicator number
   * @return CohortDefinition <strong>Should</strong> Returns empty if there is no patient who meets
   *     the conditions <strong>Should</strong> fetch patients in category 12 MG of the MQ report
   *     denominator
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

    CohortDefinition transferredOut =
        intensiveMonitoringCohortQueries.getTranferredOutPatientsForMI7();

    comp.addSearch("startedART", EptsReportUtils.map(startedART, MAPPING));

    comp.addSearch("pregnant", EptsReportUtils.map(pregnant, MAPPING));

    comp.addSearch("breastfeeding", EptsReportUtils.map(breastfeeding, MAPPING));

    comp.addSearch(
        "transferredIn",
        EptsReportUtils.map(
            transferredIn,
            "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    comp.addSearch("transferredOut", EptsReportUtils.map(transferredOut, MAPPING14));

    comp.addSearch(
        "ADULT",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnMOHArtStartDate(15, null, false),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));

    if (den == 1 || den == 2) {
      comp.setCompositionString(
          "startedART AND (breastfeeding OR ADULT) NOT (pregnant OR transferredIn OR transferredOut)");
    }
    if (den == 5 || den == 6) {
      comp.setCompositionString(
          "startedART AND NOT (pregnant OR breastfeeding OR transferredIn OR transferredOut)");
    } else if (den == 9 || den == 10) {
      comp.setCompositionString(
          "(startedART AND pregnant) AND NOT (transferredIn OR transferredOut)");
    }
    return comp;
  }

  /**
   * <b>MQC13Part3B1</b>: Melhoria de Qualidade Category 13 Deniminator B1 <br>
   * <i> BI1 and not B1E</i> <br>
   *
   * <ul>
   *   <li>BI1 - Select all patients who have the most recent “ALTERNATIVA A LINHA - 1a LINHA”
   *       (Concept Id 21190, obs_datetime) recorded in Ficha Resumo (encounter type 53) with any
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
   *       >= inclusionStartDate and <= revisionEndDate AND at least for 6 months ( “Last Clinical
   *       Consultation” (last encounter_datetime from B1) minus obs_datetime(from B2) >= 6 months)
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsOnRegimeArvSecondLineB2NEWP1_2() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients With Clinical Consultation");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();

    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("21187", hivMetadata.getRegArvSecondLine().getConceptId());
    map.put("21151", hivMetadata.getTherapeuticLineConcept().getConceptId());
    map.put("21148", hivMetadata.getSecondLineConcept().getConceptId());

    String query =
        " SELECT p.patient_id "
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
            + "       AND o.obs_datetime <= :revisionEndDate "
            + "       AND TIMESTAMPDIFF(MONTH, o.obs_datetime,  last_clinical.last_visit) >= 6";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>RF58 - Categoria 13 TB/HIV Indicador 13.14 Pediátrico Denominador – Resultado de CV</b>:
   *
   * <ul>
   *   <li>que têm o último registo de “Regime ARV Segunda Linha” na Ficha Resumo durante o período
   *       de inclusão (“Data Última 2ª Linha” >= “Data Início Inclusão” e <= “Data Fim Inclusão”)
   *       excepto os utentes que têm como “Justificação de Mudança do Tratamento” (associada a
   *       “Data Última 2ª Linha”) igual a “Gravidez”
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
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "       INNER JOIN (SELECT p.patient_id, "
            + "                          Max(o.obs_datetime) AS max_2nd_line_date "
            + "                   FROM   patient p "
            + "                          INNER JOIN encounter e "
            + "                                  ON e.patient_id = p.patient_id "
            + "                          INNER JOIN obs o "
            + "                                  ON o.encounter_id = e.encounter_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND p.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.encounter_type = ${53} "
            + "                          AND e.location_id = :location "
            + "                          AND ( o.concept_id = ${21187} "
            + "                                AND o.value_coded IS NOT NULL ) "
            + "                          AND o.obs_datetime >= :startDate "
            + "                          AND o.obs_datetime <= :endDate "
            + "                   GROUP  BY p.patient_id) last_2nd_line "
            + "               ON last_2nd_line.patient_id = p.patient_id "
            + "WHERE  p.patient_id NOT IN (SELECT p.patient_id "
            + "                            FROM   patient p "
            + "                                   INNER JOIN encounter e "
            + "                                           ON e.patient_id = p.patient_id "
            + "                                   INNER JOIN obs o1 "
            + "                                           ON o1.encounter_id = e.encounter_id "
            + "                            WHERE  p.voided = 0 "
            + "                                   AND e.voided = 0 "
            + "                                   AND o1.voided = 0 "
            + "                                   AND e.encounter_type = ${53} "
            + "                                   AND e.location_id = :location "
            + "                                   AND ( o1.concept_id = ${1792} "
            + "                                         AND o1.value_coded = ${1982} ) "
            + "                                   AND o1.obs_datetime = "
            + "                                       last_2nd_line.max_2nd_line_date "
            + "                            GROUP  BY p.patient_id)";

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
   * @param flag indicator number
   * @return CohortDefinition
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
    CohortDefinition mq12Den3 = getMQC12P2DEN(3);

    CohortDefinition mq12Den4 = getMQC12P2DEN(4);

    CohortDefinition mq12Den7 = getMQC12P2DEN(7);

    CohortDefinition mq12Den8 = getMQC12P2DEN(8);

    CohortDefinition mq12Den11 = getMQC12P2DEN(11);

    CohortDefinition b13 = resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13();

    cd.addSearch(
        "mq12Den3",
        EptsReportUtils.map(
            mq12Den3,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "mq12Den4",
        EptsReportUtils.map(
            mq12Den4,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "mq12Den7",
        EptsReportUtils.map(
            mq12Den7,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "mq12Den8",
        EptsReportUtils.map(
            mq12Den8,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "mq12Den11",
        EptsReportUtils.map(
            mq12Den11,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "B13", EptsReportUtils.map(b13, "endDate=${revisionEndDate},location=${location}"));

    if (flag == 3) {
      cd.setCompositionString("mq12Den3 AND B13");
    } else if (flag == 4) {
      cd.setCompositionString("mq12Den4 AND B13");
    } else if (flag == 7) {
      cd.setCompositionString("mq12Den7 AND B13");
    } else if (flag == 8) {
      cd.setCompositionString("mq12Den8 AND B13");
    } else if (flag == 11) {
      cd.setCompositionString("mq12Den11 AND B13");
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
   * @param den indicator number
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

    CohortDefinition mq12Den1 = getMQ12DEN(1);
    CohortDefinition mq12Den2 = getMQ12DEN(2);
    CohortDefinition mq12Den5 = getMQ12DEN(5);
    CohortDefinition mq12Den6 = getMQ12DEN(6);
    CohortDefinition mq12Den9 = getMQ12DEN(9);
    CohortDefinition mq12Den10 = getMQ12DEN(10);

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
        "mq12Den1",
        EptsReportUtils.map(
            mq12Den1,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    comp.addSearch(
        "mq12Den2",
        EptsReportUtils.map(
            mq12Den2,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    comp.addSearch(
        "mq12Den5",
        EptsReportUtils.map(
            mq12Den5,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    comp.addSearch(
        "mq12Den6",
        EptsReportUtils.map(
            mq12Den6,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    comp.addSearch(
        "mq12Den9",
        EptsReportUtils.map(
            mq12Den9,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    comp.addSearch(
        "mq12Den10",
        EptsReportUtils.map(
            mq12Den10,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    if (den == 1) {
      comp.setCompositionString("mq12Den1 AND H");
    } else if (den == 2) {
      comp.setCompositionString("mq12Den2 AND I AND II AND III");
    } else if (den == 5) {
      comp.setCompositionString("mq12Den5 AND H");
    } else if (den == 6) {
      comp.setCompositionString("mq12Den6 AND I AND II AND III");
    } else if (den == 9) {
      comp.setCompositionString("mq12Den9 AND H ");
    } else if (den == 10) {
      comp.setCompositionString("mq12Den10 AND I AND II AND III");
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
   * <b>RF14</b>: Utentes em 1ª Linha elegíveis ao pedido de CV <br>
   * <i></i><br>
   * <i> <b>O sistema irá identificar os utentes em 1ª Linha de TARV elegíveis ao pedido de Carga
   * Viral:</i> <br>
   * <i></i><br>
   * <i> <b>incluindo os utentes há pelo menos 6 meses na 1ª Linha de TARV, ou seja, incluindo todos
   * os utentes que têm o último registo da “Linha Terapêutica” na Ficha Clínica durante o período
   * de revisão igual a “1ª Linha” (última consulta, “Data 1ª Linha”>= “Data Início Revisão” e <=
   * “Data Fim Revisão”), sendo a “Data 1ª Linha” menos (-) “Data do Início TARV” registada na Ficha
   * Resumo maior ou igual (>=) a 6 meses.</i> <br>
   * <i> <b>Nota: “Data do Início TARV” é a data início TARV registada na “Ficha Resumo”,
   * independentemente do período. Caso exista o registo de mais que uma “Ficha Resumo” deve-se
   * considerar a data de início TARV mais antiga.</i> <br>
   * <br>
   * <i> <b>incluindo os utentes que reiniciaram TARV há pelo menos 6 meses, ou seja, incluindo
   * todos os utentes que têm o registo de “Mudança de Estado de Permanência TARV” = “Reinício” na
   * Ficha Clínica durante o período de inclusão (“Data Consulta Reinício TARV” >= “Data Início
   * Inclusão” e <= “Data Fim Inclusão”), sendo a “Data Última Consulta” durante o período de
   * revisão, menos (-) “Data Consulta Reinício TARV” maior ou igual (>=) a 6 meses.</i> <br>
   * <br>
   * <i> <b>Nota: “Data Última Consulta” é a data da última consulta clínica ocorrida durante o
   * período de revisão.</i> <br>
   * <i> <b>incluindo os utentes que Mudaram de Regime na 1ª Linha de TARV há pelo menos 6 meses, ou
   * seja, incluindo todos os utentes que têm o último registo da “Alternativa a Linha – 1ª Linha”
   * na Ficha Resumo, sendo a “Data Última Alternativa 1ª Linha” menos (-) “Data Última Consulta”
   * maior ou igual (>=) a 6 meses. Excepto (excluindo) os utentes que, entretanto, mudaram de
   * linha, ou seja, excepto os utentes que têm um registo de “Linha Terapêutica”, na Ficha Clínica,
   * diferente de “1ª Linha”, durante o período compreendido entre “Data Última Alternativa 1ª
   * Linha” e “Data Última Consulta.</i> <br>
   * <br>
   * <i> <b>Nota: “Data Última Consulta” é a data da última consulta clínica ocorrida durante o
   * período de revisão.</i> <br>
   * <i> <b>excluindo os utentes abandono ou reinício TARV nos últimos 6 meses anteriores a última
   * consulta do período de revisão (seguindo os critérios definidos no RF7.2);</i> <br>
   * <br>
   * <i> <b>excluindo os utentes que têm o registo do “Pedido de Investigações Laboratoriais” igual
   * a “Carga Viral”, na Ficha Clínica nos últimos 12 meses da última consulta clínica (“Data Pedido
   * CV” >= “Data Última Consulta” menos (-) 12meses e < “Data Última Consulta”).</i> <br>
   * <br>
   * <i> <b>Nota: “Data Última Consulta” é a data da última consulta clínica ocorrida durante o
   * período de revisão.</i> <br>
   * <br>
   *
   * @param preposition composition string and description
   */
  public CohortDefinition getUtentesPrimeiraLinha(UtentesPrimeiraLinhaPreposition preposition) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName(preposition.getDescription());

    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition b2New =
        commonCohortQueries.getPatientsWithFirstTherapeuticLineOnLastClinicalEncounterB2NEW();

    CohortDefinition restartded = getPatientsWhoRestartedTarvAtLeastSixMonths();

    CohortDefinition changeRegimen6Months = getMOHPatientsOnTreatmentFor6Months();

    CohortDefinition B3E = getMOHPatientsToExclusion();

    CohortDefinition abandonedExclusionInTheLastSixMonths =
        getPatientsWhoAbandonedOrRestartedTarvOnLast6MonthsArt();

    CohortDefinition abandonedExclusionFirstLine = getPatientsWhoAbandonedTarvOnOnFirstLineDate();

    CohortDefinition B5EMQ =
        commonCohortQueries.getMOHPatientsWithVLRequestorResultBetweenClinicalConsultations(
            false, true, 12);

    CohortDefinition B5EMI =
        commonCohortQueries.getMOHPatientsWithVLRequestorResultBetweenClinicalConsultations(
            false, true, 12);

    CohortDefinition transferredIn =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    compositionCohortDefinition.addSearch(
        "B2NEW",
        EptsReportUtils.map(
            b2New,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch("RESTARTED", EptsReportUtils.map(restartded, MAPPING1));

    compositionCohortDefinition.addSearch(
        "ABANDONEDTARV", EptsReportUtils.map(abandonedExclusionInTheLastSixMonths, MAPPING1));

    compositionCohortDefinition.addSearch(
        "B3MQ",
        EptsReportUtils.map(
            changeRegimen6Months,
            "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "B3MI",
        EptsReportUtils.map(
            changeRegimen6Months,
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "B3EMQ",
        EptsReportUtils.map(
            B3E, "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "B3EMI",
        EptsReportUtils.map(B3E, "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "ABANDONED1LINE", EptsReportUtils.map(abandonedExclusionFirstLine, MAPPING1));

    compositionCohortDefinition.addSearch(
        "B5EMQ",
        EptsReportUtils.map(
            B5EMQ, "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "B5EMI",
        EptsReportUtils.map(
            B5EMI, "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "TRANSFERREDIN", EptsReportUtils.map(transferredIn, "location=${location}"));

    compositionCohortDefinition.setCompositionString(preposition.getCompositionString());

    return compositionCohortDefinition;
  }

  public enum UtentesPrimeiraLinhaPreposition {
    MQ {
      @Override
      public String getCompositionString() {
        return "(B2NEW OR RESTARTED OR (B3MQ AND NOT B3EMQ) ) AND NOT (ABANDONEDTARV OR B5EMQ)";
      }

      @Override
      public String getDescription() {
        return "Utentes em Primeira Linha For MQ";
      }
    },
    MI {
      @Override
      public String getCompositionString() {
        return "(B2NEW OR RESTARTED OR (B3MI AND NOT B3EMI) ) AND NOT (ABANDONEDTARV OR B5EMI OR TRANSFERREDIN)";
      }

      @Override
      public String getDescription() {
        return "Utentes em Primeira Linha For MI";
      }
    };

    public abstract String getCompositionString();

    public abstract String getDescription();
  }

  public enum UtentesSegundaLinhaPreposition {
    MQ {
      @Override
      public String getCompositionString() {
        return "(secondLineB2 AND NOT B2E) AND NOT (ABANDONEDTARV OR B5E)";
      }

      @Override
      public String getDescription() {
        return "Utentes em Segunda Linha For MQ";
      }
    },
    MI {
      @Override
      public String getCompositionString() {
        return "(secondLineB2 AND NOT B2E) AND NOT (ABANDONEDTARV OR B5E OR TRANSFERREDIN)";
      }

      @Override
      public String getDescription() {
        return "Utentes em Segunda Linha For MI";
      }
    };

    public abstract String getCompositionString();

    public abstract String getDescription();
  }

  /**
   * <b>RF15</b>: Utentes em 2ª Linha elegíveis ao pedido de CV <br>
   * <i></i><br>
   * <i> <b>O sistema irá identificar os utentes em 2ª Linha de TARV elegíveis ao pedido de Carga
   * Viral:</i> <br>
   * <i></i><br>
   * <i> <b>incluindo os utentes há pelo menos 6 meses na 2ª Linha de TARV, ou seja, incluindo todos
   * os utentes que têm o último registo de “Regime ARV Segunda Linha” na Ficha Resumo durante o
   * período de revisão (“Data Última 2ª Linha” >= “Data Início Revisão” e <= “Data Fim Revisão”),
   * sendo a “Data Última 2ª Linha” menos (-) “Data Última Consulta” maior ou igual (>=) a 6 meses.
   * Excepto os utentes que, entretanto, mudaram de linha, ou seja, excepto os utentes que têm um
   * registo de “Linha Terapêutica” diferente de “2ª Linha”, na Ficha Clínica durante o período
   * compreendido entre “Data Última 2ª Linha” e “Data Última Consulta”. </i> <br>
   * <i> <b>Nota: “Data Última Consulta” é a data da última consulta clínica ocorrida durante o
   * período de revisão..</i> <br>
   * <br>
   * <i> <b>excluindo os utentes abandono ou reinício TARV nos últimos 6 meses anteriores a última
   * consulta do período de revisão (seguindo os critérios definidos no RF7.2);</i> <br>
   * <br>
   * <i> <b>excluindo os utentes que têm o registo do “Pedido de Investigações Laboratoriais” igual
   * a “Carga Viral”, na Ficha Clínica nos últimos 12 meses da última consulta clínica (“Data Pedido
   * CV”>= “Data Última Consulta” menos (-) 12meses e < “Data Última Consulta”). Nota: “Data Última
   * Consulta” é a data da última consulta clínica ocorrida durante o período de revisão.</i> <br>
   * <br>
   */
  public CohortDefinition getUtentesSegundaLinha(UtentesSegundaLinhaPreposition preposition) {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition secondLine6Months = getPatientsOnRegimeArvSecondLineB2NEWP1_2();

    CohortDefinition b2e = getMQC13DEN_B2E();

    CohortDefinition abandonedExclusionInTheLastSixMonths =
        getPatientsWhoAbandonedOrRestartedTarvOnLast6MonthsArt();

    CohortDefinition abandonedExclusionSecondLine = getPatientsWhoAbandonedTarvOnOnSecondLineDate();

    CohortDefinition B5E =
        commonCohortQueries.getMOHPatientsWithVLRequestorResultBetweenClinicalConsultations(
            false, true, 12);

    CohortDefinition transferredIn =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    compositionCohortDefinition.addSearch(
        "secondLineB2",
        EptsReportUtils.map(
            secondLine6Months,
            "startDate=${startDate},revisionEndDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "B2E",
        EptsReportUtils.map(
            b2e,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "ABANDONEDTARV", EptsReportUtils.map(abandonedExclusionInTheLastSixMonths, MAPPING1));

    compositionCohortDefinition.addSearch(
        "ABANDONED2LINE", EptsReportUtils.map(abandonedExclusionSecondLine, MAPPING1));

    compositionCohortDefinition.addSearch(
        "B5E",
        EptsReportUtils.map(
            B5E, "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "TRANSFERREDIN", EptsReportUtils.map(transferredIn, "location=${location}"));

    compositionCohortDefinition.setCompositionString(preposition.getCompositionString());

    return compositionCohortDefinition;
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
   * @param den boolean parameter, true indicates denominator ,false indicates numerator
   * @param line indicator number
   * @return CohortDefinition <strong>Should</strong> Returns empty if there is no patient who meets
   *     the conditions <strong>Should</strong> fetch patients in category 13 MG of the MQ report
   */
  public CohortDefinition getMQ13(Boolean den, Integer line) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    if (den) {
      compositionCohortDefinition.setName("DENOMINADOR");
    } else {
      compositionCohortDefinition.setName("NUMERADOR");
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

    CohortDefinition b2e = getMQC13DEN_B2E();

    CohortDefinition secondLine6Months = getPatientsOnRegimeArvSecondLineB2NEWP1_2();

    CohortDefinition changeRegimen6Months = getMOHPatientsOnTreatmentFor6Months();

    CohortDefinition B3E =
        commonCohortQueries.getMOHPatientsToExcludeFromTreatmentIn6Months(
            true,
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getRegimenAlternativeToFirstLineConcept(),
            commonMetadata.getAlternativeLineConcept(),
            Arrays.asList(
                commonMetadata.getAlternativeFirstLineConcept(),
                commonMetadata.getRegimeChangeConcept(),
                hivMetadata.getNoConcept()),
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getTherapeuticLineConcept(),
            Collections.singletonList(hivMetadata.getFirstLineConcept()));

    CohortDefinition abandonedExclusionInTheLastSixMonthsFromFirstLineDate =
        getPatientsWhoAbandonedInTheLastSixMonthsFromFirstLineDate();

    CohortDefinition abandonedExclusionByTarvRestartDate =
        getPatientsWhoAbandonedTarvOnArtRestartDate();

    CohortDefinition abandonedExclusionFirstLine = getPatientsWhoAbandonedTarvOnOnFirstLineDate();

    CohortDefinition abandonedExclusionSecondLine = getPatientsWhoAbandonedTarvOnOnSecondLineDate();

    CohortDefinition restartdedExclusion = getPatientsWhoRestartedTarvAtLeastSixMonths();

    CohortDefinition B5E =
        commonCohortQueries.getMOHPatientsWithVLRequestorResultBetweenClinicalConsultations(
            false, true, 12);

    CohortDefinition G = getMQ13G();

    CohortDefinition PrimeiraLinha = getUtentesPrimeiraLinha(UtentesPrimeiraLinhaPreposition.MQ);

    CohortDefinition SegundaLinha = getUtentesSegundaLinha(UtentesSegundaLinhaPreposition.MQ);

    CohortDefinition tbDiagnosisActive = getPatientsWithTbActiveOrTbTreatment();

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
            "startDate=${startDate},revisionEndDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "B3",
        EptsReportUtils.map(
            changeRegimen6Months,
            "startDate=${startDate},revisionEndDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "B3E",
        EptsReportUtils.map(B3E, "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "B5E",
        EptsReportUtils.map(
            B5E, "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "G",
        EptsReportUtils.map(
            G, "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "B2E",
        EptsReportUtils.map(
            b2e,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "RESTARTED", EptsReportUtils.map(restartdedExclusion, MAPPING));

    compositionCohortDefinition.addSearch(
        "RESTARTEDTARV", EptsReportUtils.map(abandonedExclusionByTarvRestartDate, MAPPING));

    compositionCohortDefinition.addSearch(
        "ABANDONEDTARV",
        EptsReportUtils.map(abandonedExclusionInTheLastSixMonthsFromFirstLineDate, MAPPING1));

    compositionCohortDefinition.addSearch(
        "ABANDONED1LINE", EptsReportUtils.map(abandonedExclusionFirstLine, MAPPING1));

    compositionCohortDefinition.addSearch(
        "ABANDONED2LINE", EptsReportUtils.map(abandonedExclusionSecondLine, MAPPING1));

    compositionCohortDefinition.addSearch(
        "PrimeiraLinha", EptsReportUtils.map(PrimeiraLinha, MAPPING1));

    compositionCohortDefinition.addSearch(
        "SegundaLinha", EptsReportUtils.map(SegundaLinha, MAPPING1));

    compositionCohortDefinition.addSearch(
        "tbDiagnosisActive", EptsReportUtils.map(tbDiagnosisActive, MAPPING3));

    if (den) {
      if (line == 1) {
        compositionCohortDefinition.setCompositionString(
            "(((B1 AND age) OR D) AND PrimeiraLinha) AND NOT (C OR tbDiagnosisActive)");
      } else if (line == 6 || line == 7 || line == 8) {
        compositionCohortDefinition.setCompositionString(
            "(B1 AND PrimeiraLinha AND age) AND NOT (C OR D OR tbDiagnosisActive) ");
      } else if (line == 4) {
        compositionCohortDefinition.setCompositionString(
            "(B1 AND SegundaLinha AND D AND age) AND NOT (C OR tbDiagnosisActive) ");
      } else if (line == 13) {
        compositionCohortDefinition.setCompositionString(
            "((B1 AND SegundaLinha) AND NOT (C OR D) AND age");
      }
    } else {
      if (line == 1) {
        compositionCohortDefinition.setCompositionString(
            "((((B1 AND age) OR D) AND PrimeiraLinha) AND NOT (C OR tbDiagnosisActive)) AND G");
      } else if (line == 6 || line == 7 || line == 8) {
        compositionCohortDefinition.setCompositionString(
            "(B1 AND PrimeiraLinha AND G AND age) AND NOT (C OR D OR tbDiagnosisActive) ");
      } else if (line == 4) {
        compositionCohortDefinition.setCompositionString(
            "(B1 AND SegundaLinha AND D AND G AND age) AND NOT (C OR tbDiagnosisActive)");
      } else if (line == 13) {
        compositionCohortDefinition.setCompositionString(
            "((B1 AND SegundaLinha) AND NOT (C OR D) AND (G AND age)");
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
   * @param indicator indicator number
   * @return CohortDefinition
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
              "effectiveDate=${revisionEndDate}"));
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
              "effectiveDate=${revisionEndDate}"));
    } else if (indicator == 10) {
      cd.addSearch(
          "age",
          EptsReportUtils.map(
              ageCohortQueries.createXtoYAgeCohort(
                  "Crianças  (5-9 anos de idade) na 1a linha de TARV que receberam o resultado da Carga Viral entre o sexto e o nono mês após o início do TARV",
                  5,
                  9),
              "effectiveDate=${revisionEndDate}"));
    } else if (indicator == 11) {
      cd.addSearch(
          "age",
          EptsReportUtils.map(
              ageCohortQueries.createXtoYAgeCohort(
                  "Crianças  (10-14 anos de idade) na 1a linha de TARV que receberam o resultado da Carga Viral entre o sexto e o nono mês após o início do TARV",
                  10,
                  14),
              "effectiveDate=${revisionEndDate}"));
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
        "ABANDONEDTARV",
        EptsReportUtils.map(getPatientsWhoAbandonedOrRestartedTarvOnLast6MonthsArt(), MAPPING1));

    cd.addSearch(
        "ABANDONED1LINE",
        EptsReportUtils.map(getPatientsWhoAbandonedTarvOnOnFirstLineDate(), MAPPING1));

    cd.addSearch(
        "ABANDONED2LINE",
        EptsReportUtils.map(getPatientsWhoAbandonedTarvOnOnSecondLineDate(), MAPPING1));

    cd.addSearch("F", EptsReportUtils.map(getTranferredOutPatientsCat7(), MAPPING11));

    cd.addSearch("G", EptsReportUtils.map(getMQC13P3NUM_G(), MAPPING));
    cd.addSearch("H", EptsReportUtils.map(getMQC13P3NUM_H(), MAPPING));
    cd.addSearch("I", EptsReportUtils.map(getMQC13P3NUM_I(), mapping));
    cd.addSearch("J", EptsReportUtils.map(getMQC13P3NUM_J(), MAPPING));
    cd.addSearch("K", EptsReportUtils.map(getMQC13P3NUM_K(), MAPPING1));
    cd.addSearch("L", EptsReportUtils.map(getMQC13P3NUM_L(), MAPPING));
    cd.addSearch("DD", EptsReportUtils.map(getDeadPatientsCompositionMQ13(), MAPPING3));
    cd.addSearch(
        "tbDiagnosisActive", EptsReportUtils.map(getPatientsWithTbActiveOrTbTreatment(), MAPPING));

    if (indicator == 2 || indicator == 9 || indicator == 10 || indicator == 11)
      cd.setCompositionString(
          "((A AND NOT C AND (G OR J)) OR (B1 AND (H OR K))) AND NOT (F OR E OR DD OR ABANDONEDTARV OR tbDiagnosisActive) AND age");
    if (indicator == 5 || indicator == 14)
      cd.setCompositionString(
          "(B2New AND (I OR L)) AND NOT (F OR E OR DD OR ABANDONEDTARV OR tbDiagnosisActive) AND age");

    return cd;
  }

  /**
   * os utentes que tiveram um registo do Resultado de Carga Viral (quantitativo ou qualitativo) na
   * Ficha Clínica ou na Ficha Resumo (“Última Carga Viral”), entre “Data de Início TARV” mais (+)
   * 198 dias e “Data de Início TARV” mais (+) 297 dias. Nota: “Data do Início TARV” é a data
   * definida no RF5.1
   *
   * @return CohortDefinition
   */
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
        "SELECT cv.patient_id "
            + "FROM   (SELECT p.patient_id, "
            + "               e.encounter_datetime AS cv_encounter "
            + "        FROM   patient p "
            + "               JOIN encounter e "
            + "                 ON e.patient_id = p.patient_id "
            + "               JOIN obs o "
            + "                 ON o.encounter_id = e.encounter_id "
            + "        WHERE  e.encounter_type = ${6} "
            + "               AND p.voided = 0 "
            + "               AND e.voided = 0 "
            + "               AND o.voided = 0 "
            + "               AND e.location_id = :location "
            + "               AND ( ( o.concept_id = ${856} "
            + "                       AND o.value_numeric IS NOT NULL ) "
            + "                      OR ( o.concept_id = ${1305} "
            + "                           AND o.value_coded IS NOT NULL ) ) ) cv "
            + "       INNER JOIN (SELECT inicio.patient_id, "
            + "                          inicio.data_inicio AS data_inicio "
            + "                   FROM   (SELECT p.patient_id, "
            + "                                  Min(value_datetime) data_inicio "
            + "                           FROM   patient p "
            + "                                  INNER JOIN encounter e "
            + "                                          ON p.patient_id = e.patient_id "
            + "                                  INNER JOIN obs o "
            + "                                          ON e.encounter_id = o.encounter_id "
            + "                           WHERE  p.voided = 0 "
            + "                                  AND e.voided = 0 "
            + "                                  AND o.voided = 0 "
            + "                                  AND e.encounter_type = ${53} "
            + "                                  AND o.concept_id = ${1190} "
            + "                                  AND o.value_datetime IS NOT NULL "
            + "                                  AND o.value_datetime <= :endDate "
            + "                                  AND e.location_id = :location "
            + "                           GROUP  BY p.patient_id) inicio "
            + "                   WHERE  data_inicio BETWEEN :startDate AND :endDate "
            + "                   GROUP  BY patient_id) art_tbl "
            + "               ON cv.patient_id = art_tbl.patient_id "
            + "WHERE  cv.cv_encounter BETWEEN Date_add(art_tbl.data_inicio, INTERVAL 198 day) "
            + "                               AND "
            + "                                      Date_add(art_tbl.data_inicio, "
            + "                                      INTERVAL 297 day)";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * os utentes que tiveram um registo do Resultado de Carga Viral (quantitativo ou qualitativo) na
   * Ficha Clínica ou na Ficha Resumo (“Última Carga Viral”), entre “Data Última Alternativa 1ª
   * Linha” mais (+) 198 dias e “Data Última Alternativa 1ª Linha” mais (+) 297 dias.
   *
   * @return CohortDefinition
   */
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
            + "                                        interval 198 day) AND "
            + "                                               Date_add(B1.regime_date, "
            + "                                               interval 297 day);  ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * I - os utentes que tiveram um registo do Resultado de Carga Viral (quantitativo ou qualitativo)
   * na Ficha Clínica ou na Ficha Resumo (“Última Carga Viral”), entre “Data Última 2ª Linha” mais
   * (+) 198 dias e “Data Última 2ª Linha” mais (+) 297 dias.
   *
   * @return CohortDefinition
   */
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
            + "                               AND oo.concept_id = ${1792} "
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
            + "         WHERE  I_tbl.encounter_datetime BETWEEN Date_add(B2.regime_date, interval 198 day) AND Date_add(B2.regime_date, interval 297 day)";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>MQ13</b>: Melhoria de Qualidade Category 13 Part 4, H <br>
   *
   * <ul>
   *   <li>os utentes que tiveram um registo do “Pedido de Investigações Laboratoriais” igual a
   *       Carga Viral na Ficha Clínica entre “Data da CV>=1000” mais (+) 80 dias e “Data da
   *       CV>=1000” mais (+) 132 dias
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQ13P4H(int vlQuantity) {
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
            + "                AND o.value_numeric >=  "
            + vlQuantity
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
            + "            INTERVAL 132 DAY);";

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
   * @param den boolean parameter, true indicates denominator ,false indicates numerator
   * @param line indicator number
   * @return CohortDefinition
   */
  public CohortDefinition getMQ13P4(Boolean den, Integer line) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    boolean useE53 = true;

    if (den) {
      if (line == 3) {
        compositionCohortDefinition.setName(
            "# de Adultos (15/+anos) na 1ª linha de TARV com registo resultado de CV acima de 1000");
      } else if (line == 12) {
        compositionCohortDefinition.setName(
            "# de crianças (>2 anos de idade) na 1ª linha de TARV com registo de resultado de CV ≥1000");
      } else if (line == 18) {
        compositionCohortDefinition.setName(
            "# de MG na 1ª linha de TARV com registo de resultado de CV >50");
      }
    } else {
      if (line == 3) {
        compositionCohortDefinition.setName(
            "# de Adultos (15/+anos) na 1ª linha de TARV com registo de pedido de CV entre o 3º e o 4º mês após terem recebido o último resultado de CV ≥1000 cps/ml");
      } else if (line == 12) {
        compositionCohortDefinition.setName(
            "# de crianças (>2 anos de idade) na 1ª linha de TARV com registo de pedido de CV entre o 3º e o 4º mês após terem recebido o último resultado de CV ≥1000");
      } else if (line == 18) {
        compositionCohortDefinition.setName(
            "# de MG na 1ª linha de TARV com registo de pedido de CV entre o 3º e o 4º mês após terem recebido o último resultado de CV >50");
      }
    }
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition children = this.ageCohortQueries.createXtoYAgeCohort("children", 2, 14);

    CohortDefinition adult = this.ageCohortQueries.createXtoYAgeCohort("adult", 15, 200);

    CohortDefinition patientsFromFichaClinicaLinhaTerapeutica =
        getPatientsOnArtFirstLineForMoreThanSixMonthsFromArtStartDate();

    CohortDefinition patientsFromFichaClinicaCargaViral = getB2_13(useE53);

    CohortDefinition pregnantWithCargaViralHigherThan1000 =
        QualityImprovement2020Queries.getMQ13DenB4_P4(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getHivViralLoadConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId(),
            commonMetadata.getPregnantConcept().getConceptId());

    CohortDefinition breastfeedingWithCargaViralHigherThan1000 =
        QualityImprovement2020Queries.getMQ13DenB5_P4(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getHivViralLoadConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId(),
            commonMetadata.getBreastfeeding().getConceptId());

    CohortDefinition pregnantWithCargaViralHigherThan50 =
        QualityImprovement2020Queries.getCV50ForPregnant(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getHivViralLoadConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId(),
            commonMetadata.getPregnantConcept().getConceptId(),
            50);

    CohortDefinition breastfeedingWithCargaViralHigherThan50 =
        QualityImprovement2020Queries.getMQ13DenB5_P4(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getHivViralLoadConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId(),
            commonMetadata.getBreastfeeding().getConceptId(),
            50);

    CohortDefinition transferredIn =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    CohortDefinition transferOut =
        intensiveMonitoringCohortQueries.getTranferredOutPatientsForMI7();

    CohortDefinition H = getMQ13P4H(1000);

    CohortDefinition H50 = getMQ13P4H(50);

    CohortDefinition patientsWithCargaViralonFichaClinicaAndFichaResumo =
        getPatientsWithCargaViralonFichaClinicaAndFichaResumo(1000);

    CohortDefinition patientsWithCargaViralonFichaClinicaAndFichaResumoDen18 =
        getPatientsWithCargaViralonFichaClinicaAndFichaResumo(50);

    CohortDefinition b2New =
        commonCohortQueries.getPatientsWithFirstTherapeuticLineOnLastClinicalEncounterB2NEW();

    CohortDefinition PatPrimeraLinha = getPatientsFirstLine();

    CohortDefinition abandonedExclusionInTheLastSixMonthsFromFirstLineDate =
        getPatientsWhoAbandonedInTheLastSixMonthsFromFirstLineDate();

    CohortDefinition restartdedExclusion = getPatientsWhoRestartedTarvAtLeastSixMonths();

    CohortDefinition abandonedExclusionByTarvRestartDate =
        getPatientsWhoAbandonedTarvOnArtRestartDate();

    CohortDefinition changeRegimen6Months = getMOHPatientsOnTreatmentFor6Months();

    CohortDefinition B3E =
        commonCohortQueries.getMOHPatientsToExcludeFromTreatmentIn6Months(
            true,
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getRegimenAlternativeToFirstLineConcept(),
            commonMetadata.getAlternativeLineConcept(),
            Arrays.asList(
                commonMetadata.getAlternativeFirstLineConcept(),
                commonMetadata.getRegimeChangeConcept(),
                hivMetadata.getNoConcept()),
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getTherapeuticLineConcept(),
            Collections.singletonList(hivMetadata.getFirstLineConcept()));

    CohortDefinition abandonedExclusionFirstLine = getPatientsWhoAbandonedTarvOnOnFirstLineDate();

    CohortDefinition B5E =
        commonCohortQueries.getMOHPatientsWithVLRequestorResultBetweenClinicalConsultations(
            false, true, 12);

    CohortDefinition PrimeiraLinha = getUtentesPrimeiraLinha(UtentesPrimeiraLinhaPreposition.MQ);

    compositionCohortDefinition.addSearch(
        "children", EptsReportUtils.map(children, "effectiveDate=${revisionEndDate}"));
    compositionCohortDefinition.addSearch(
        "adult", EptsReportUtils.map(adult, "effectiveDate=${revisionEndDate}"));

    compositionCohortDefinition.addSearch(
        "B1", EptsReportUtils.map(patientsWithCargaViralonFichaClinicaAndFichaResumo, MAPPING));

    compositionCohortDefinition.addSearch(
        "B1Den18",
        EptsReportUtils.map(patientsWithCargaViralonFichaClinicaAndFichaResumoDen18, MAPPING));

    compositionCohortDefinition.addSearch(
        "B2", EptsReportUtils.map(patientsFromFichaClinicaCargaViral, MAPPING1));

    compositionCohortDefinition.addSearch(
        "B4", EptsReportUtils.map(pregnantWithCargaViralHigherThan1000, MAPPING1));

    compositionCohortDefinition.addSearch(
        "B5", EptsReportUtils.map(breastfeedingWithCargaViralHigherThan1000, MAPPING1));

    compositionCohortDefinition.addSearch("E", EptsReportUtils.map(transferredIn, MAPPING));

    compositionCohortDefinition.addSearch(
        "F",
        EptsReportUtils.map(
            transferOut, "revisionEndDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "DD", EptsReportUtils.map(getDeadPatientsCompositionMQ13(), MAPPING3));

    compositionCohortDefinition.addSearch("H", EptsReportUtils.map(H, MAPPING));

    compositionCohortDefinition.addSearch("H50", EptsReportUtils.map(H50, MAPPING));

    compositionCohortDefinition.addSearch(
        "B2NEW",
        EptsReportUtils.map(
            b2New,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "ABANDONEDTARV",
        EptsReportUtils.map(abandonedExclusionInTheLastSixMonthsFromFirstLineDate, MAPPING1));

    compositionCohortDefinition.addSearch(
        "RESTARTED", EptsReportUtils.map(restartdedExclusion, MAPPING));

    compositionCohortDefinition.addSearch(
        "RESTARTEDTARV", EptsReportUtils.map(abandonedExclusionByTarvRestartDate, MAPPING));

    compositionCohortDefinition.addSearch(
        "B3",
        EptsReportUtils.map(
            changeRegimen6Months,
            "startDate=${startDate},revisionEndDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "B3E",
        EptsReportUtils.map(B3E, "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "ABANDONED1LINE", EptsReportUtils.map(abandonedExclusionFirstLine, MAPPING1));

    compositionCohortDefinition.addSearch(
        "B5E",
        EptsReportUtils.map(
            B5E, "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "B4CV50", EptsReportUtils.map(pregnantWithCargaViralHigherThan50, MAPPING1));

    compositionCohortDefinition.addSearch(
        "B5CV50", EptsReportUtils.map(breastfeedingWithCargaViralHigherThan50, MAPPING1));

    compositionCohortDefinition.addSearch(
        "PrimeiraLinha", EptsReportUtils.map(PrimeiraLinha, MAPPING1));

    compositionCohortDefinition.addSearch(
        "PatPrimeraLinha", EptsReportUtils.map(PatPrimeraLinha, MAPPING));

    if (den) {
      if (line == 3) {
        compositionCohortDefinition.setCompositionString(
            "(((B1 AND PatPrimeraLinha) AND NOT (B4 or E or F or DD)) AND adult");
      } else if (line == 12) {
        compositionCohortDefinition.setCompositionString(
            "(((B1 AND PatPrimeraLinha) AND NOT (B4 or B5 or E or F)) AND children");
      } else if (line == 18) {
        compositionCohortDefinition.setCompositionString(
            "((B4CV50 AND PatPrimeraLinha) AND NOT (E or F))");
      }
    } else {
      if (line == 3) {
        compositionCohortDefinition.setCompositionString(
            "(((B1 AND PatPrimeraLinha AND H) AND NOT (B4 or E or F or DD)) AND adult");
      } else if (line == 12) {
        compositionCohortDefinition.setCompositionString(
            "(((B1 AND PatPrimeraLinha AND H) AND NOT (B4 or B5 or E or F)) AND children");
      } else if (line == 18) {
        compositionCohortDefinition.setCompositionString(
            "((B4CV50 AND PatPrimeraLinha AND H50) AND NOT (E or F)");
      }
    }
    return compositionCohortDefinition;
  }

  /**
   * B2 - Select all female patients with first clinical consultation (encounter type 6) that have
   * the concept “GESTANTE” (Concept Id 1982) and value coded “SIM” (Concept Id 1065) registered
   * (1)during the inclusion period (first occurrence, encounter_datetime >= startDateInclusion and
   * <=endDateInclusion) and (2) 3 months after the start of ART (encounter_datetime > “Patient ART
   * Start Date” + 3 months)
   *
   * @return CohortDefinition
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
        "       SELECT patient_id "
            + " FROM ("
            + "         SELECT p.patient_id, MIN(e.encounter_datetime) AS first_gestante "
            + "         FROM  patient p "
            + "               INNER JOIN person per on p.patient_id=per.person_id "
            + "               INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "               INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "         WHERE p.voided = 0 "
            + "           AND per.voided=0 AND per.gender = 'F' "
            + "           AND e.voided = 0 AND o.voided  = 0 "
            + "           AND e.encounter_type = ${6} "
            + "           AND o.concept_id = ${1982} "
            + "           AND o.value_coded = ${1065} "
            + "           AND e.location_id = :location "
            + "         GROUP BY p.patient_id) gest  "
            + " WHERE gest.first_gestante >= :startDate "
            + "   AND gest.first_gestante <= :endDate "
            + "   AND gest.first_gestante > DATE_ADD((SELECT MIN(o.value_datetime) as art_date "
            + "                                       FROM encounter e "
            + "                                           INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                                       WHERE gest.patient_id = e.patient_id "
            + "                                         AND e.voided = 0 AND o.voided = 0 "
            + "                                         AND e.encounter_type = ${53} AND o.concept_id = ${1190} "
            + "                                         AND o.value_datetime IS NOT NULL AND o.value_datetime <= :endDate AND e.location_id = :location "
            + "                                       LIMIT 1), interval 3 MONTH) ";

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
   * @return CohortDefinition
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
            + "       AND e.encounter_datetime <= DATE_ADD(inicio.art_date,INTERVAL 132 DAY)  "
            + "       AND e.location_id = :location";

    cd.setQuery(sb.replace(query));
    return cd;
  }

  /**
   * filtrando os utentes que têm o registo de “Pedido de Investigações Laboratoriais” igual a
   * “Carga Viral” na primeira consulta clínica com registo de grávida durante o período de inclusão
   * (“Data 1ª Consulta Grávida”).
   *
   * @return CohortDefinition
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
        "SELECT     p.patient_id "
            + "FROM       patient p "
            + "INNER JOIN encounter e "
            + "ON         p.patient_id = e.patient_id "
            + "INNER JOIN obs o "
            + "ON         e.encounter_id = o.encounter_id "
            + "INNER JOIN "
            + "           ( "
            + "                  SELECT pregnant.patient_id, "
            + "                         pregnant.first_gestante "
            + "                  FROM   ( "
            + "                                    SELECT     p.patient_id, "
            + "                                               Min(e.encounter_datetime) AS first_gestante "
            + "                                    FROM       patient p "
            + "                                    INNER JOIN encounter e "
            + "                                    ON         e.patient_id = p.patient_id "
            + "                                    INNER JOIN obs o "
            + "                                    ON         e.encounter_id = o.encounter_id "
            + "                                    WHERE      p.voided = 0 "
            + "                                    AND        e.voided = 0 "
            + "                                    AND        o.voided = 0 "
            + "                                    AND        e.encounter_type = ${6} "
            + "                                    AND        o.concept_id = ${1982} "
            + "                                    AND        o.value_coded = ${1065} "
            + "                                    AND        e.encounter_datetime <= :endDate "
            + "                                    AND        e.location_id = :location "
            + "                                    GROUP BY   p.patient_id ) pregnant "
            + "                  WHERE  pregnant.first_gestante BETWEEN :startDate AND    :endDate ) pregnancy "
            + "ON         pregnancy.patient_id = p.patient_id "
            + "WHERE      p.voided = 0 "
            + "AND        e.voided = 0 "
            + "AND        o.voided = 0 "
            + "AND        e.encounter_type = ${6} "
            + "AND        o.concept_id = ${23722} "
            + "AND        o.value_coded = ${856} "
            + "AND        e.encounter_datetime = pregnancy.first_gestante "
            + "AND        e.location_id = :location "
            + "GROUP BY   p.patient_id";

    cd.setQuery(sb.replace(query));
    return cd;
  }

  /**
   * 13.15. % de MG elegíveis a CV com registo de pedido de CV feito pelo clínico (MG que iniciaram
   * TARV na CPN) Denominator: # de MG com registo de início do TARV na CPN dentro do período de
   * inclusão. as following: <code>
   * (startedART AND pregnant) AND NOT (abandoned OR transferredOut </code>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getgetMQC13P2DenMGInIncluisionPeriod() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(" CAT 13 DEN - part 2 - 13.15. % de MG elegíveis ");
    cd.addParameter(new Parameter("startDate", "StartDate", Date.class));
    cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition startedART = getMOHArtStartDate();

    CohortDefinition transferredOut = getTranferredOutPatientsCat7();
    CohortDefinition pregnant =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition pregnantAbandonedDuringPeriod =
        getPatientsWhoAbandonedOrRestartedTarvOnLast3MonthsArt();

    cd.addSearch("startedART", EptsReportUtils.map(startedART, MAPPING));
    cd.addSearch("pregnant", EptsReportUtils.map(pregnant, MAPPING));
    cd.addSearch("transferredOut", EptsReportUtils.map(transferredOut, MAPPING11));
    cd.addSearch("abandoned", EptsReportUtils.map(pregnantAbandonedDuringPeriod, MAPPING1));

    cd.setCompositionString("((startedART AND pregnant) AND NOT (abandoned OR transferredOut))");

    return cd;
  }

  /**
   * 13.16. % de MG elegíveis a CV com registo de pedido de CV feito pelo clínico na primeira CPN
   * (MG que entraram em TARV na CPN) Denominator:# de MG que tiveram a primeira CPN no período de
   * inclusão, e que já estavam em TARV há mais de 3 meses (Line 91,Column F in the Template) as
   * following: B2
   *
   * @return CohortDefinition
   */
  public CohortDefinition getgetMQC13P2DenMGInIncluisionPeriod33Month() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "StartDate", Date.class));
    cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch("B2", EptsReportUtils.map(getMQC13P2DenB2(), MAPPING));

    CohortDefinition pregnantAbandonedDuringPeriod =
        getPatientsWhoAbandonedOrRestartedTarvOnLast3MonthsArt();
    cd.addSearch("ABANDONED", EptsReportUtils.map(pregnantAbandonedDuringPeriod, MAPPING1));

    cd.setCompositionString("B2 AND NOT ABANDONED");

    return cd;
  }

  /**
   * O sistema irá produzir o seguinte denominador do Indicador 13.17 da Categoria 13 MG de
   * Resultado de CV:
   *
   * <ul>
   *   <li>incluindo todos os utentes selecionados no Indicador 13.15 Numerador definido no RF29
   *       (Categoria 13 MG Indicador 13.15 – Numerador Pedido CV) e
   *   <li>incluindo todos os utentes selecionados no Indicador 13.16 Numerador definido no RF31
   *       (Categoria 13 MG Indicador 13.16 – Numerador Pedido CV)
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQC13P2DenMGInIncluisionPeriod33Days() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "StartDate", Date.class));
    cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition num13a15 = getMQC13P2Num1();

    CohortDefinition num13a16 = getMQC13P2Num2();

    cd.addSearch("num13a15", EptsReportUtils.map(num13a15, MAPPING1));

    cd.addSearch("num13a16", EptsReportUtils.map(num13a16, MAPPING1));

    cd.setCompositionString("num13a15 OR num13a16");

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
   * @param flag report source
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
   * @return CohortDefinition
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
   * @return CohortDefinition
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
   * <code>(startedART AND pregnant AND investLab) AND NOT (abandoned OR transferredOut)</code>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getMQC13P2Num1() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("13.15 - MG elegíveis a CV com registo de pedido de CV");
    cd.addParameter(new Parameter("startDate", "StartDate", Date.class));
    cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition pregnant =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition transferredOut = getTranferredOutPatients();

    CohortDefinition pregnantAbandonedDuringPeriod =
        getPatientsWhoAbandonedOrRestartedTarvOnLast3MonthsArt();

    cd.addSearch("startedART", EptsReportUtils.map(getMOHArtStartDate(), MAPPING));
    cd.addSearch("pregnant", EptsReportUtils.map(pregnant, MAPPING));
    cd.addSearch("transferredOut", EptsReportUtils.map(transferredOut, MAPPING1));
    cd.addSearch("investLab", EptsReportUtils.map(getMQC13P2DenB3(), MAPPING));
    cd.addSearch("abandoned", EptsReportUtils.map(pregnantAbandonedDuringPeriod, MAPPING1));

    cd.setCompositionString(
        "((startedART AND pregnant AND investLab) AND NOT (abandoned OR transferredOut))");
    return cd;
  }

  /**
   * 13.16. % de MG elegíveis a CV com registo de pedido de CV feito pelo clínico na primeira CPN
   * (MG que entraram em TARV na CPN) (Line 91 in the template) Numerator (Column E in the Template)
   * as following: (B2 and J)
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQC13P2Num2() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "StartDate", Date.class));
    cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch("B2", EptsReportUtils.map(getMQC13P2DenB2(), MAPPING));
    cd.addSearch("J", EptsReportUtils.map(getgetMQC13P2DenB4(), MAPPING));
    CohortDefinition pregnantAbandonedDuringPeriod =
        getPatientsWhoAbandonedOrRestartedTarvOnLast3MonthsArt();
    cd.addSearch("ABANDONED", EptsReportUtils.map(pregnantAbandonedDuringPeriod, MAPPING1));

    cd.setCompositionString("(B2 AND NOT ABANDONED) AND J");

    return cd;
  }

  /**
   * 13.17. % de MG que receberam o resultado da Carga Viral dentro de 33 dias após pedido (Line 92
   * in the template) Numerator (Column E in the Template) as following: <code>
   * ((A and B1 and B3 and K) or (B2 and B4 and L)) and NOT (D or E or F) and Age >= 15*</code>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQC13P2Num3() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "StartDate", Date.class));
    cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition num13a15 = getMQC13P2Num1();

    CohortDefinition num13a16 = getMQC13P2Num2();

    cd.addSearch("num13a15", EptsReportUtils.map(num13a15, MAPPING1));

    cd.addSearch("num13a16", EptsReportUtils.map(num13a16, MAPPING1));

    cd.addSearch("K", EptsReportUtils.map(getMQC13P2NumK(), MAPPING));
    cd.addSearch("L", EptsReportUtils.map(getMQC13P2NumL(), MAPPING));
    cd.addSearch("M", EptsReportUtils.map(getMQC13P2NumM(), MAPPING));
    cd.addSearch("N", EptsReportUtils.map(getMQC13P2NumN(), MAPPING));

    cd.setCompositionString("(num13a15 AND (K OR M)) OR (num13a16 AND (L OR N))");

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
   * @param den indicator number
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
    comp.addParameter(new Parameter("location", "location", Location.class));

    List<Integer> dispensationTypes =
        Arrays.asList(
            hivMetadata.getGaac().getConceptId(),
            hivMetadata.getQuarterlyDispensation().getConceptId(),
            hivMetadata.getDispensaComunitariaViaApeConcept().getConceptId(),
            hivMetadata.getDescentralizedArvDispensationConcept().getConceptId(),
            hivMetadata.getRapidFlow().getConceptId(),
            hivMetadata.getSemiannualDispensation().getConceptId(),
            hivMetadata.getAnnualArvDispensationConcept().getConceptId());

    List<Integer> states = Arrays.asList(hivMetadata.getStartDrugs().getConceptId());

    CohortDefinition inscritosNoMDSHa24Meses =
        QualityImprovement2020Queries.getPatientsWithFollowingMdcDispensationsWithStates(
            dispensationTypes, states);

    List<Integer> quarterlyDispensation =
        Arrays.asList(hivMetadata.getQuarterlyDispensation().getConceptId());
    CohortDefinition withDT =
        QualityImprovement2020Queries.getPatientsWithFollowingMdcDispensationsWithStates(
            quarterlyDispensation, states);

    CohortDefinition tipoDispensa =
        genericCohortQueries.hasCodedObs(
            hivMetadata.getTypeOfDispensationConcept(),
            BaseObsCohortDefinition.TimeModifier.LAST,
            SetComparator.IN,
            Arrays.asList(hivMetadata.getAdultoSeguimentoEncounterType()),
            Arrays.asList(
                hivMetadata.getQuarterlyConcept(),
                hivMetadata.getSemiannualDispensation(),
                hivMetadata.getAnnualArvDispensationConcept()));

    CohortDefinition transferOut = getTranferredOutPatients();

    CohortDefinition dead = getDeadPatientsComposition();

    CohortDefinition nextPickupBetween83And97 =
        QualityImprovement2020Queries.getPatientsWithPickupOnFilaBetween(83, 97);
    CohortDefinition nextPickupBetween173And187 =
        QualityImprovement2020Queries.getPatientsWithPickupOnFilaBetween(173, 187);
    CohortDefinition nextPickupBetween335And395 =
        QualityImprovement2020Queries.getPatientsWithPickupOnFilaBetween(335, 395);

    CohortDefinition viralLoad = QualityImprovement2020Queries.getPatientsWithVlGreaterThen1000();
    // Pacientes com pedidos de investigações depois de DT
    List<Integer> concepts = Arrays.asList(hivMetadata.getQuarterlyDispensation().getConceptId());
    CohortDefinition IADT =
        getPatientsWithVLResultLessThan1000Between2VlRequestAfterTheseMDS(concepts);

    // Utentes que tiveram dois pedidos de investigação depois da inscrição ao GACC/DT/APE/DD/FR/DS
    List<Integer> mdsConcepts =
        Arrays.asList(
            hivMetadata.getGaac().getConceptId(),
            hivMetadata.getQuarterlyDispensation().getConceptId(),
            hivMetadata.getDispensaComunitariaViaApeConcept().getConceptId(),
            hivMetadata.getDescentralizedArvDispensationConcept().getConceptId(),
            hivMetadata.getRapidFlow().getConceptId(),
            hivMetadata.getSemiannualDispensation().getConceptId(),
            hivMetadata.getAnnualArvDispensationConcept().getConceptId());
    CohortDefinition VL2Pedidos =
        getPatientsWithVLResultLessThan1000Between2VlRequestAfterTheseMDS(mdsConcepts);

    // Utentes que têm o registo de Resultado de Carga Viral na Ficha Laboratório registada entre a
    // data do 2o pedido de CV e Data de Revisao
    CohortDefinition VLFL =
        getPatientsWhoHadVLResultOnLaboratoryFormAfterSecondVLRequest(mdsConcepts);

    comp.addSearch(
        "MDSHa24Meses",
        EptsReportUtils.map(
            inscritosNoMDSHa24Meses,
            "startDate=${revisionEndDate-26m+1d},endDate=${revisionEndDate-24m},location=${location}"));

    comp.addSearch(
        "proxLevtoFILA83a97Dias",
        EptsReportUtils.map(
            nextPickupBetween83And97,
            "startDate=${revisionEndDate-26m+1d},endDate=${revisionEndDate-24m},location=${location}"));

    comp.addSearch(
        "proxLevtoFILA173a187Dias",
        EptsReportUtils.map(
            nextPickupBetween173And187,
            "startDate=${revisionEndDate-26m+1d},endDate=${revisionEndDate-24m},location=${location}"));

    comp.addSearch(
        "proxLevtoFILA335a395Dias",
        EptsReportUtils.map(
            nextPickupBetween335And395,
            "startDate=${revisionEndDate-26m+1d},endDate=${revisionEndDate-24m},location=${location}"));

    comp.addSearch(
        "DT",
        EptsReportUtils.map(
            withDT,
            "startDate=${revisionEndDate-26m+1d},endDate=${revisionEndDate-24m},location=${location}"));

    comp.addSearch(
        "tipoDispensa",
        EptsReportUtils.map(
            tipoDispensa,
            "onOrAfter=${revisionEndDate-26m+1d},onOrBefore=${revisionEndDate-24m},locationList=${location}"));

    comp.addSearch(
        "CD",
        EptsReportUtils.map(
            getPregnantOrBreastfeedingWomen(),
            "revisionEndDate=${revisionEndDate},location=${location}"));
    comp.addSearch(
        "G2",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13(),
            "endDate=${revisionEndDate},location=${location}"));

    comp.addSearch("F", EptsReportUtils.map(transferOut, MAPPING1));

    comp.addSearch(
        "dead", EptsReportUtils.map(dead, "endDate=${revisionEndDate},location=${location}"));

    comp.addSearch(
        "VL",
        EptsReportUtils.map(
            viralLoad,
            "startDate=${revisionEndDate-26m+1d},endDate=${revisionEndDate},location=${location}"));

    comp.addSearch(
        "IADT",
        EptsReportUtils.map(
            IADT,
            "startDate=${revisionEndDate-26m+1d},endDate=${revisionEndDate-24m},revisionEndDate=${revisionEndDate},location=${location}"));

    comp.addSearch(
        "VL2Pedidos",
        EptsReportUtils.map(
            VL2Pedidos,
            "startDate=${revisionEndDate-26m+1d},endDate=${revisionEndDate-24m},revisionEndDate=${revisionEndDate},location=${location}"));

    comp.addSearch(
        "VLFL",
        EptsReportUtils.map(
            VLFL,
            "startDate=${revisionEndDate-26m+1d},endDate=${revisionEndDate-24m},revisionEndDate=${revisionEndDate},location=${location}"));

    if (den == 1) {
      comp.setCompositionString(
          "(MDSHa24Meses OR tipoDispensa OR proxLevtoFILA83a97Dias OR proxLevtoFILA173a187Dias OR proxLevtoFILA335a395Dias) "
              + " AND NOT (CD OR F OR dead)");
    } else if (den == 2) {
      comp.setCompositionString(
          "((MDSHa24Meses OR tipoDispensa OR proxLevtoFILA83a97Dias OR proxLevtoFILA173a187Dias OR proxLevtoFILA335a395Dias) "
              + " AND NOT (CD OR F OR VL)) AND G2");
    } else if (den == 3) {
      comp.setCompositionString(
          "(MDSHa24Meses OR tipoDispensa OR proxLevtoFILA83a97Dias OR proxLevtoFILA173a187Dias OR proxLevtoFILA335a395Dias) "
              + " AND G2 AND VL2Pedidos AND NOT (CD OR F OR VL)");
    } else if (den == 4) {
      comp.setCompositionString(
          "((MDSHa24Meses OR tipoDispensa OR proxLevtoFILA83a97Dias OR proxLevtoFILA173a187Dias OR proxLevtoFILA335a395Dias) "
              + " AND G2 AND VL2Pedidos AND VLFL AND NOT (CD OR F OR VL)) ");
    } else if (den == 5 || den == 6) {
      comp.setCompositionString(
          "((DT OR tipoDispensa OR proxLevtoFILA83a97Dias)  AND  NOT (CD OR F OR dead))");
    } else if (den == 7 || den == 8) {
      comp.setCompositionString(
          "((DT OR tipoDispensa OR proxLevtoFILA83a97Dias) AND  NOT (CD OR F OR VL)) AND G2 ");
    } else if (den == 11 || den == 12) {
      comp.setCompositionString(
          "((DT OR tipoDispensa OR proxLevtoFILA83a97Dias OR proxLevtoFILA173a187Dias OR proxLevtoFILA335a395Dias) "
              + " AND  NOT (CD OR F OR VL)) AND G2 AND IADT AND VLFL");
    } else if (den == 9 || den == 10) {
      comp.setCompositionString(
          "((DT OR tipoDispensa OR proxLevtoFILA83a97Dias OR proxLevtoFILA173a187Dias OR proxLevtoFILA335a395Dias) "
              + " AND  NOT (CD OR F OR VL)) AND G2 IADT");
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
   * @param num indicator number
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

    List<Integer> dispensationTypes =
        Arrays.asList(
            hivMetadata.getGaac().getConceptId(),
            hivMetadata.getQuarterlyDispensation().getConceptId(),
            hivMetadata.getDispensaComunitariaViaApeConcept().getConceptId(),
            hivMetadata.getDescentralizedArvDispensationConcept().getConceptId(),
            hivMetadata.getRapidFlow().getConceptId(),
            hivMetadata.getSemiannualDispensation().getConceptId());

    List<Integer> states = Arrays.asList(hivMetadata.getStartDrugs().getConceptId());

    CohortDefinition queryA1 =
        QualityImprovement2020Queries.getPatientsWithFollowingMdcDispensationsWithStates(
            dispensationTypes, states);

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
            hivMetadata.getTypeOfDispensationConcept().getConceptId(),
            hivMetadata.getSemiannualDispensation().getConceptId());

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
    // Utentes que tiveram dois pedidos de investigação depois da inscrição ao GACC/DT/APE/DD/FR/DS
    List<Integer> concepts =
        Arrays.asList(
            hivMetadata.getGaac().getConceptId(),
            hivMetadata.getQuarterlyDispensation().getConceptId(),
            hivMetadata.getDispensaComunitariaViaApeConcept().getConceptId(),
            hivMetadata.getDescentralizedArvDispensationConcept().getConceptId(),
            hivMetadata.getRapidFlow().getConceptId(),
            hivMetadata.getSemiannualDispensation().getConceptId(),
            hivMetadata.getAnnualArvDispensationConcept().getConceptId());
    CohortDefinition VL2Pedidos =
        getPatientsWithVLResultLessThan1000Between2VlRequestAfterTheseMDS(concepts);

    // Utentes que têm o registo de Resultado de Carga Viral na Ficha Laboratório registada entre a
    // data do 2o pedido de CV e Data de Revisao
    CohortDefinition VLFL = getPatientsWhoHadVLResultOnLaboratoryFormAfterSecondVLRequest(concepts);

    CohortDefinition LOWVLFL =
        getPatientsWhoHadVLResultLessThen1000nLaboratoryFormAfterSecondVLRequest(concepts);

    // Pacientes com pedidos de investigações depois de DT
    List<Integer> dtConcept = Arrays.asList(hivMetadata.getQuarterlyDispensation().getConceptId());
    CohortDefinition IADT =
        getPatientsWithVLResultLessThan1000Between2VlRequestAfterTheseMDS(dtConcept);

    comp.addSearch(
        "A1",
        EptsReportUtils.map(
            queryA1,
            "startDate=${revisionEndDate-14m},endDate=${revisionEndDate-11m},location=${location}"));

    comp.addSearch(
        "A2",
        EptsReportUtils.map(
            queryA2,
            "startDate=${revisionEndDate-26m+1d},endDate=${revisionEndDate-24m},location=${location}"));

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
        "Den2",
        EptsReportUtils.map(
            getMQ15DEN(2),
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    comp.addSearch(
        "Den3",
        EptsReportUtils.map(
            getMQ15DEN(3),
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));
    comp.addSearch(
        "Den4",
        EptsReportUtils.map(
            getMQ15DEN(4),
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));
    comp.addSearch(
        "Den5",
        EptsReportUtils.map(
            getMQ15DEN(5),
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    comp.addSearch(
        "Den7",
        EptsReportUtils.map(
            getMQ15DEN(7),
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    comp.addSearch(
        "Den10",
        EptsReportUtils.map(
            getMQ15DEN(10),
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));
    comp.addSearch(
        "Den11",
        EptsReportUtils.map(
            getMQ15DEN(11),
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));
    comp.addSearch(
        "G2",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13(),
            "endDate=${revisionEndDate},location=${location}"));

    comp.addSearch(
        "VL2Pedidos",
        EptsReportUtils.map(
            VL2Pedidos,
            "startDate=${revisionEndDate-26m+1d},endDate=${revisionEndDate-24m},revisionEndDate=${revisionEndDate},location=${location}"));
    comp.addSearch(
        "VLFL",
        EptsReportUtils.map(
            VLFL,
            "startDate=${revisionEndDate-26m+1d},endDate=${revisionEndDate-24m},revisionEndDate=${revisionEndDate},location=${location}"));

    comp.addSearch(
        "LOWVLFL",
        EptsReportUtils.map(
            LOWVLFL,
            "startDate=${revisionEndDate-26m+1d},endDate=${revisionEndDate-24m},revisionEndDate=${revisionEndDate},location=${location}"));

    comp.addSearch(
        "IADT",
        EptsReportUtils.map(
            IADT,
            "startDate=${revisionEndDate-26m+1d},endDate=${revisionEndDate-24m},revisionEndDate=${revisionEndDate},location=${location}"));

    if (num == 1) {
      comp.setCompositionString("Den1 AND G2");
    } else if (num == 2) {
      comp.setCompositionString("Den2 AND VL2Pedidos");
    } else if (num == 3) {
      comp.setCompositionString("Den3 AND VLFL");
    } else if (num == 4) {
      comp.setCompositionString("Den4 AND LOWVLFL");
    } else if (num == 5 || num == 6) {
      comp.setCompositionString("Den5 AND G2");
    } else if (num == 7 || num == 8) {
      comp.setCompositionString("Den7  AND G2 AND IADT");
    } else if (num == 9 || num == 10) {
      comp.setCompositionString("Den10 AND G2 AND VLFL");
    } else if (num == 11) {
      comp.setCompositionString("Den11 AND G2 AND LOWVLFL");
    } else if (num == 12) {
      comp.setCompositionString("Den11 AND G2 AND LOWVLFL");
    }
    return comp;
  }

  /**
   * Filtrando os pacientesutentes que têm o registo de início do MDS para pacienteutente estável na
   * última consulta decorrida há 12 meses (última “Data Consulta Clínica” >= “Data Fim Revisão” –
   * 12 meses+1dia e <= “Data Fim Revisão”), ou seja, registo de um MDC (MDC1 ou MDC2 ou MDC3 ou
   * MDC4 ou MDC5) como:
   *
   * <p>“GA” e o respectivo “Estado” = “Início” ou “DT” e o respectivo “Estado” = “Início” ou “DS” e
   * o respectivo “Estado” = “Início” ou “APE” e o respectivo “Estado” = “Início” ou “FR” e o
   * respectivo “Estado” = “Início” ou “DD” e o respectivo “Estado” = “Início” na última consulta
   * clínica (“Ficha Clínica”, coluna 24) decorrida entre: “Data Início de Avaliação” = “Data Fim de
   * Revisão” menos 12 meses + 1 dia “Data Fim de Avaliação” = “Data Fim de Revisão”
   *
   * <p>os utentes que têm o registo de “Tipo de Dispensa” = “DT” na última consulta (“Ficha
   * Clínica”) decorrida há 12 meses (última “Data Consulta Clínica” >= “Data Fim Revisão” – 12
   * meses+1dia e <= “Data Fim Revisão”)
   *
   * <p>os utentes com registo de “Tipo de Dispensa” = “DS” na última consulta (“Ficha Clínica”)
   * decorrida há 12 meses (última “Data Consulta Clínica” >= “Data Fim Revisão” – 12 meses+1dia e
   * <= “Data Fim Revisão”)
   *
   * <p>os utentes com registo de último levantamento na farmácia (FILA) há 12 meses (última “Data
   * Levantamento”>= “Data Fim Revisão” – 12 meses+1dia e <= “Data Fim Revisão”) com próximo
   * levantamento agendado para 83 a 97 dias ( “Data Próximo Levantamento” menos “Data
   * Levantamento”>= 83 dias e <= 97 dias)
   *
   * <p>os utentes com registo de último levantamento na farmácia (FILA) há 12 meses (última “Data
   * Levantamento”>= “Data Fim Revisão” – 12 meses+1dia e <= “Data Fim Revisão”) com próximo
   * levantamento agendado para 173 a 187 dias ( “Data Próximo Levantamento” menos “Data
   * Levantamento”>= 173 dias e <= 187 dias)
   */
  public CohortDefinition getPatientsWhoHadMdsOnMostRecentClinicalAndPickupOnFilaFR36(
      List<Integer> dispensationTypes, List<Integer> states) {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName(
        "MDS para utentes estáveis que tiveram consulta no período de avaliação");

    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));

    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition mdsLastClinical =
        getPatientsWithMdcOnMostRecentClinicalFormWithFollowingDispensationTypesAndState(
            dispensationTypes, states);

    CohortDefinition queryA3 =
        genericCohortQueries.hasCodedObs(
            hivMetadata.getTypeOfDispensationConcept(),
            BaseObsCohortDefinition.TimeModifier.LAST,
            SetComparator.IN,
            Arrays.asList(hivMetadata.getAdultoSeguimentoEncounterType()),
            Arrays.asList(
                hivMetadata.getQuarterlyConcept(), hivMetadata.getSemiannualDispensation()));

    CohortDefinition nextPickupBetween83And97 =
        QualityImprovement2020Queries.getPatientsWithPickupOnFilaBetween(83, 97);

    CohortDefinition nextPickupBetween173And187 =
        QualityImprovement2020Queries.getPatientsWithPickupOnFilaBetween(173, 187);

    compositionCohortDefinition.addSearch(
        "MDS",
        EptsReportUtils.map(
            mdsLastClinical, "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "DSDT",
        EptsReportUtils.map(
            queryA3, "onOrAfter=${startDate},onOrBefore=${endDate},locationList=${location}"));

    compositionCohortDefinition.addSearch(
        "FILA83",
        EptsReportUtils.map(
            nextPickupBetween83And97,
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "FILA173",
        EptsReportUtils.map(
            nextPickupBetween173And187,
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.setCompositionString("MDS OR DSDT OR FILA83 OR FILA173");

    return compositionCohortDefinition;
  }

  /**
   * O sistema irá identificar utentes já inscritos em algum MDS selecionado os seguinte utentes:
   * selecionando todos os utentes que têm o último registo de pelo menos um dos seguintes modelos
   * na “Ficha Clínica” (coluna 24) registada antes da “Data Última Consulta”:
   *
   * <p>Último registo de MDC (MDC1 ou MDC2 ou MDC3 ou MDC4 ou MDC5) como “GAAC” e o respectivo
   * “Estado” = “Iníicioar” ou “Continua”, ou
   *
   * <p>Último registo de MDC (MDC1 ou MDC2 ou MDC3 ou MDC4 ou MDC5) como “DT” e o respectivo
   * “Estado” = “Início” ou “Continua”, ou
   *
   * <p>Último registo de MDC (MDC1 ou MDC2 ou MDC3 ou MDC4 ou MDC5) como “DS” e o respectivo
   * “Estado” = “Início” ou “Continua”, ou
   *
   * <p>Último registo de MDC (MDC1 ou MDC2 ou MDC3 ou MDC4 ou MDC5) como “APE” e o respectivo
   * “Estado” = “Início” ou “Continua”, ou
   *
   * <p>Último registo de MDC (MDC1 ou MDC2 ou MDC3 ou MDC4 ou MDC5) como “FR” e o respectivo
   * “Estado” = “Início” ou “Continua”, ou
   *
   * <p>Último registo de MDC (MDC1 ou MDC2 ou MDC3 ou MDC4 ou MDC5) como “DD” e o respectivo
   * “Estado” = “Início” ou “Continua”
   *
   * <p>Nota1: A “Data Última Consulta” é a última “Data de Consulta” do utente ocorrida no período
   * compreendido entre: “Data Início Avaliação” = “Data Fim de Revisão” menos 12 meses + 1 dia
   * “Data Fim Avaliação” = “Data Fim de Revisão”
   *
   * <p>Filtrando os utentes que têm o último registo de “Tipo de Dispensa” = “DT” antes da última
   * consulta do período de revisão ( “Data Última Consulta”)
   *
   * <p>Filtrando os utentes que têm o último registo de “Tipo de Dispensa” = “DS” antes da última
   * consulta do período de revisão ( “Data Última Consulta”)
   *
   * <p>Filtrando os utentes com registo de “Tipo de Dispensa” = “DA” na última consulta (“Ficha
   * Clínica”) decorrida há 12 meses (última “Data Consulta Clínica” >= “Data Fim Revisão” – 12
   * meses+1dia e <= “Data Fim Revisão”) ou
   *
   * <p>Filtrando os utentes com registo de último levantamento na farmácia (FILA) antes da última
   * consulta do período de revisão (“Data última Consulta) com próximo levantamento agendado para
   * 83 a 97 dias ( “Data Próximo Levantamento” menos “Data Levantamento”>= 83 dias e <= 97 dias,
   * sendo “Data Levantamento” último levantamento registado no FILA < “Data Última Consulta”)
   *
   * <p>Filtrando os utentes com registo de último levantamento na farmácia (FILA) antes da última
   * consulta do período de revisão (“Data última Consulta) com próximo levantamento agendado para
   * 173 a 187 dias ( “Data Próximo Levantamento” menos “Data Levantamento”>= 173 dias e <= 187
   * dias, sendo “Data Levantamento” último levantamento registado no FILA < “Data Última
   * Consulta”).
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsAlreadyEnrolledInTheMdc() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName(
        "MDS para utentes estáveis que tiveram consulta no período de avaliação");
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    List<Integer> mdsConcepts =
        Arrays.asList(
            hivMetadata.getGaac().getConceptId(),
            hivMetadata.getQuarterlyDispensation().getConceptId(),
            hivMetadata.getDispensaComunitariaViaApeConcept().getConceptId(),
            hivMetadata.getDescentralizedArvDispensationConcept().getConceptId(),
            hivMetadata.getRapidFlow().getConceptId(),
            hivMetadata.getSemiannualDispensation().getConceptId(),
            hivMetadata.getAnnualArvDispensationConcept().getConceptId());

    List<Integer> states =
        Arrays.asList(
            hivMetadata.getStartDrugs().getConceptId(),
            hivMetadata.getContinueRegimenConcept().getConceptId());

    CohortDefinition mdsLastClinical =
        getPatientsWithMdcBeforeMostRecentClinicalFormWithFollowingDispensationTypesAndState(
            mdsConcepts, states);

    CohortDefinition dtBeforeClinical =
        getPatientsWithDispensationBeforeLastConsultationDate(hivMetadata.getQuarterlyConcept());
    CohortDefinition dsBeforeClinical =
        getPatientsWithDispensationBeforeLastConsultationDate(
            hivMetadata.getSemiannualDispensation());

    CohortDefinition proxLevtoFILA83a97Dias =
        getPatientsWhoHadFilaBeforeLastClinicalConsutationBetween(83, 97);

    CohortDefinition proxLevtoFILA173a187Dias =
        getPatientsWhoHadFilaBeforeLastClinicalConsutationBetween(173, 187);

    CohortDefinition proxLevtoFILA335a395Dias =
        getPatientsWhoHadFilaBeforeLastClinicalConsutationBetween(335, 395);

    compositionCohortDefinition.addSearch(
        "MDS",
        EptsReportUtils.map(
            mdsLastClinical, "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "DT",
        EptsReportUtils.map(
            dtBeforeClinical, "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "DS",
        EptsReportUtils.map(
            dsBeforeClinical, "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "proxLevtoFILA83a97Dias",
        EptsReportUtils.map(
            proxLevtoFILA83a97Dias,
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "proxLevtoFILA173a187Dias",
        EptsReportUtils.map(
            proxLevtoFILA173a187Dias,
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "proxLevtoFILA335a395Dias",
        EptsReportUtils.map(
            proxLevtoFILA335a395Dias,
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.setCompositionString(
        "MDS OR DS OR DT OR "
            + " proxLevtoFILA83a97Dias OR proxLevtoFILA173a187Dias OR proxLevtoFILA335a395Dias");

    return compositionCohortDefinition;
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
   * @param preposition composition string and description
   * @return CohortDefinition
   */
  public CohortDefinition getMQ14(MQCat14Preposition preposition) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName(preposition.getDescription());

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
        EptsReportUtils.map(txPvls.getPregnantWoman(), "endDate=${endDate},location=${location}"));
    cd.addSearch(
        "A2",
        EptsReportUtils.map(
            txPvls.getBreastfeedingPatients(), "endDate=${endDate},location=${location}"));

    cd.setCompositionString(preposition.getCompositionString());

    return cd;
  }

  public enum MQCat14Preposition {
    A {
      @Override
      public String getCompositionString() {
        return "A";
      }

      @Override
      public String getDescription() {
        return "MQ Cat 14 - A ";
      }
    },
    A_AND_A1 {
      @Override
      public String getCompositionString() {
        return "A AND A1";
      }

      @Override
      public String getDescription() {
        return "MQ Cat 14 - A and A1 ";
      }
    },
    A_AND_A2 {
      @Override
      public String getCompositionString() {
        return "A AND A2";
      }

      @Override
      public String getDescription() {
        return "MQ Cat 14 - A and A2";
      }
    },
    A_NOT_A1A2 {
      @Override
      public String getCompositionString() {
        return "A AND NOT (A1 OR A2)";
      }

      @Override
      public String getDescription() {
        return "MQ Cat 14 Denominator - VL Patients that are not Pregnant or Breastfeeding";
      }
    },
    B {
      @Override
      public String getCompositionString() {
        return "B";
      }

      @Override
      public String getDescription() {
        return "MQ Cat 14 - B ";
      }
    },
    B_AND_B1 {
      @Override
      public String getCompositionString() {
        return "B AND B1";
      }

      @Override
      public String getDescription() {
        return "MQ Cat 14 - B AND B1";
      }
    },
    B_AND_B2 {
      @Override
      public String getCompositionString() {
        return "B AND B2";
      }

      @Override
      public String getDescription() {
        return "MQ Cat 14 - B AND B2";
      }
    },
    B_NOT_B1B2 {
      @Override
      public String getCompositionString() {
        return "B AND NOT (B1 OR B2)";
      }

      @Override
      public String getDescription() {
        return "MQ Cat 14 Numerator - VL Patients that are not Pregnant or Breastfeeding";
      }
    };

    public abstract String getCompositionString();

    public abstract String getDescription();
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
   * @param preposition
   * @return CohortDefinition
   */
  public CohortDefinition getMQ14NUM(MQCat14Preposition preposition) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.setName(preposition.getDescription());
    cd.addSearch(
        "B",
        EptsReportUtils.map(
            txPvls.getPatientsWithViralLoadSuppressionWhoAreOnArtMoreThan3Months(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "B1",
        EptsReportUtils.map(txPvls.getPregnantWoman(), "endDate=${endDate},location=${location}"));
    cd.addSearch(
        "B2",
        EptsReportUtils.map(
            txPvls.getBreastfeedingPatients(), "endDate=${endDate},location=${location}"));
    cd.setCompositionString(preposition.getCompositionString());

    return cd;
  }

  /**
   * <b>MQ9Den: M&Q Report - Categoria 9 Denominador</b><br>
   *
   * <ul>
   *   <li>9.1. % de adultos HIV+ em TARV que tiveram conhecimento do resultado do primeiro CD4
   *       dentro de 33 dias após a inscrição
   *   <li>9.2. % de crianças HIV+ em TARV que tiveram conhecimento do resultado do primeiro CD4
   *       dentro de 33 dias após a inscrição
   * </ul>
   *
   * @param flag indicator number
   * @return CohortDefinition
   */
  public CohortDefinition getMQ9Den(int flag) {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    switch (flag) {
      case 1:
        cd.setName(
            "9.1 % de adultos  (15/+anos) com pedido de CD4 na primeira consulta clínica depois do diagnóstico de HIV+");
        break;
      case 2:
        cd.setName(
            "9.2 % de adultos (15/+anos) HIV+ que receberam o resultado do primeiro CD4 dentro de 33 dias após a primeira consulta clínica");
        break;
    }

    cd.addSearch(
        "AGE",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnFirstClinicalConsultation(15, null),
            "onOrAfter=${revisionEndDate-12m+1d},onOrBefore=${revisionEndDate-9m},revisionEndDate=${revisionEndDate},location=${location}"));

    String inclusionPeriodMappings =
        "revisionEndDate=${revisionEndDate},startDate=${revisionEndDate-12m+1d},endDate=${revisionEndDate-9m},location=${location}";

    cd.addSearch(
        "A",
        EptsReportUtils.map(
            getFirstClinicalConsultationDuringInclusionPeriod(),
            "startDate=${revisionEndDate-12m+1d},endDate=${revisionEndDate-9m},revisionEndDate=${revisionEndDate},location=${location}"));

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
        "pregnantOnPeriod",
        EptsReportUtils.map(
            getMOHPregnantORBreastfeedingOnClinicalConsultation(
                commonMetadata.getPregnantConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            inclusionPeriodMappings));

    cd.addSearch(
        "breastfeedingOnPeriod",
        EptsReportUtils.map(
            getMOHPregnantORBreastfeedingOnClinicalConsultation(
                commonMetadata.getBreastfeeding().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            inclusionPeriodMappings));

    cd.setCompositionString(
        "(A AND (AGE OR D OR breastfeedingOnPeriod)) AND NOT (C OR E OR pregnantOnPeriod)");
    return cd;
  }

  /**
   * <b>Categoria 9 Denominador - Pedido e Resultado de CD4 - MG</b>
   * <li>Pedido de CD4 = “% de MG HIV+ que teve registo de pedido do primeiro CD4 na data da
   *     primeira consulta clínica/abertura da Ficha Mestra”
   * <li>Resultado de CD4 = “% de MG HIV+ que teve conhecimento do resultado do primeiro CD4 dentro
   *     de 33 dias após a data da primeira CPN (primeira consulta com registo de Gravidez”
   *
   * @param denominator parameter to receive the indicator numbe
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getCd4RequestAndResultForPregnantsCat9Den(Integer denominator) {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    switch (denominator) {
      case 9:
        cd.setName("9.9 % de MG  HIV+ com registo de pedido de CD4 na primeira CPN");
        break;
      case 10:
        cd.setName(
            "9.10 % de MG  HIV+ que receberam o resultado do primeiro CD4 dentro de 33 dias  após a primeira CPN");
        break;
    }

    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    String inclusionPeriodMappings =
        "startDate=${revisionEndDate-12m+1d},endDate=${revisionEndDate-9m},location=${location}";

    cd.addSearch(
        "pregnantOnPeriod",
        EptsReportUtils.map(
            intensiveMonitoringCohortQueries.getFirstPregnancyORBreastfeedingOnClinicalConsultation(
                commonMetadata.getPregnantConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            inclusionPeriodMappings));

    cd.addSearch(
        "transferredIn",
        EptsReportUtils.map(
            QualityImprovement2020Queries.getTransferredInPatients(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
                hivMetadata.getPatientFoundYesConcept().getConceptId(),
                hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
                hivMetadata.getArtStatus().getConceptId()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("pregnantOnPeriod AND NOT transferredIn");

    return cd;
  }

  /**
   * Incluindo todas as utentes do Denominador - Pedido de CD4 – MG (definidos no RF16)
   *
   * <p>Filtrando as que tiveram registo do “Pedido de CD4” na mesma consulta clínica na qual
   * tiveram o primeiro registo de Gravidez durante o período de inclusão (>= “Data Fim de Revisão”
   * menos (-) 12 meses mais (+) 1 dia e “Data fim de Revisão” menos (-) 9 meses)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getCd4RequestAndResultForPregnantsCat9Num(Integer numerator) {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    switch (numerator) {
      case 9:
        cd.setName("9.9 % de MG  HIV+ com registo de pedido de CD4 na primeira CPN.");
        break;
      case 10:
        cd.setName(
            "9.10 % de MG  HIV+ que receberam o resultado do primeiro CD4 dentro de 33 dias  após a primeira CPN");
        break;
    }
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    String inclusionPeriodMappings =
        "startDate=${revisionEndDate-12m+1d},endDate=${revisionEndDate-9m},location=${location}";
    cd.addSearch(
        "requestCd4ForPregnant",
        EptsReportUtils.map(
            getRequestForCd4OnFirstClinicalConsultationOfPregnancy(
                commonMetadata.getPregnantConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId(),
                hivMetadata.getApplicationForLaboratoryResearch().getConceptId(),
                hivMetadata.getCD4AbsoluteOBSConcept().getConceptId()),
            inclusionPeriodMappings));

    cd.addSearch(
        "resultCd4ForPregnant",
        EptsReportUtils.map(
            getCd4ResultAfterFirstConsultationOfPregnancy(
                commonMetadata.getPregnantConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            inclusionPeriodMappings));

    if (numerator == 9) {
      cd.addSearch(
          "DENOMINATOR",
          EptsReportUtils.map(getCd4RequestAndResultForPregnantsCat9Den(9), MAPPING1));
      cd.setCompositionString("DENOMINATOR AND requestCd4ForPregnant");
    } else if (numerator == 10) {
      cd.addSearch(
          "DENOMINATOR",
          EptsReportUtils.map(getCd4RequestAndResultForPregnantsCat9Den(10), MAPPING1));
      cd.setCompositionString("DENOMINATOR AND resultCd4ForPregnant");
    }

    return cd;
  }

  /**
   * <b>MQ10Den: M&Q Report - Categoria 10 Denominador</b><br>
   *
   * <ul>
   *   <li>10.1. % de adultos (15/+anos) que iniciaram o TARV dentro de 15 dias após diagnóstico
   *   <li>10.3. % de crianças (0-14 anos) HIV+ que iniciaram TARV dentro de 15 dias após
   *       diagnóstico
   * </ul>
   *
   * @param adults indicators flag
   * @return CohortDefinition
   */
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
   * <li>Filtrando os que tiveram registo do “Resultado de CD4” na consulta clínica decorrida em 33
   *     dias após a primeira consulta clínica do período de inclusão (= “Data Fim de Revisão” menos
   *     (-) 12 meses mais (+) 1 dia e “Data fim de Revisão” menos (-) 9 meses), ou seja, “Data
   *     Resultado de CD4” menos a “Data Primeira Consulta” <=33 dias
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getCd4ResultAfterFirstConsultationOnInclusionPeriod() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "B: Filter all patients with CD4 within 33 days from the first clinical consultation");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    String query =
        "SELECT pa.patient_id "
            + "FROM "
            + "    patient pa "
            + "        INNER JOIN encounter enc "
            + "                   ON enc.patient_id =  pa.patient_id "
            + "        INNER JOIN obs "
            + "                   ON obs.encounter_id = enc.encounter_id "
            + "        INNER JOIN obs o2 "
            + "                   ON o2.encounter_id = enc.encounter_id "
            + "        INNER JOIN "
            + "    ( "
            + "        SELECT final.patient_id, final.first_consultation "
            + "                    FROM   ( "
            + "                               SELECT pa.patient_id, "
            + "                                      MIN(enc.encounter_datetime) AS first_consultation "
            + "                               FROM   patient pa "
            + "                                          INNER JOIN encounter enc "
            + "                                                     ON enc.patient_id =  pa.patient_id "
            + "                                          INNER JOIN obs "
            + "                                                     ON obs.encounter_id = enc.encounter_id "
            + "                               WHERE pa.voided = 0 "
            + "                                 AND enc.voided = 0 "
            + "                                 AND obs.voided = 0 "
            + "                                 AND enc.encounter_type = ${6} "
            + "                                 AND enc.encounter_datetime <= :revisionEndDate "
            + "                                 AND enc.location_id = :location "
            + "                               GROUP  BY pa.patient_id "
            + "                           ) final "
            + "                    WHERE  final.first_consultation >= :startDate "
            + "                      AND final.first_consultation <= :endDate "
            + "        GROUP  BY final.patient_id "
            + "    ) consultation_date ON consultation_date.patient_id = pa.patient_id "
            + "WHERE  pa.voided = 0 "
            + "  AND enc.voided = 0 "
            + "  AND obs.voided = 0 "
            + "  AND o2.voided = 0 "
            + "  AND enc.encounter_type = ${6} "
            + "  AND ( "
            + "        (obs.concept_id = ${1695} AND obs.value_numeric IS NOT NULL) "
            + "        OR "
            + "        (o2.concept_id = ${730} AND o2.value_numeric IS NOT NULL) "
            + "        OR "
            + "        (o2.concept_id = ${165515} AND o2.value_coded IS NOT NULL) "
            + "      ) "
            + "  AND enc.encounter_datetime >= consultation_date.first_consultation "
            + "  AND enc.encounter_datetime <= DATE_ADD(consultation_date.first_consultation, INTERVAL 33 DAY) "
            + "  AND enc.location_id = :location "
            + "GROUP BY pa.patient_id";

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("730", hivMetadata.getCD4PercentConcept().getConceptId());
    map.put("165515", hivMetadata.getCD4SemiQuantitativeConcept().getConceptId());

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>MQ9Num: M&Q Report - Categoria 9 Numerador - Pedido de CD4 Adulto</b><br>
   *
   * <ul>
   *   <li>9.1. % de adultos HIV+ ≥ 15 anos que teve registo de pedido do primeiro CD4 na data da
   *       primeira consulta clínica/abertura da Ficha Mestra”
   *   <li>9.2. % de crianças HIV+ em TARV que tiveram conhecimento do resultado do primeiro CD4
   *       dentro de 33 dias após a inscrição
   * </ul>
   *
   * @param numerator indicator number
   * @return CohortDefinition
   */
  public CohortDefinition getMQ9Num(Integer numerator) {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    switch (numerator) {
      case 1:
        cd.setName(
            "% de adultos HIV+ ≥ 15 anos que teve registo de pedido do primeiro CD4 na data da primeira consulta clínica/abertura da Ficha Mestra");
        break;
      case 2:
        cd.setName(
            "9.2 % de adultos  (15/+anos) HIV+ que receberam o resultado do primeiro CD4 dentro de 33 dias  após a primeira consulta clínica");
        break;
      case 3:
        cd.setName(
            "9.3 % de adultos (15/+anos) com pedido de CD4 na consulta clínica de reinício do TARV");
        break;
      case 4:
        cd.setName(
            "9.4 % de adultos (15/+anos) que receberam o resultado do CD4 dentro de 33 dias após consulta clínica de reinício do TARV");
        break;
      case 5:
        cd.setName(
            "9.5 % de crianças  (0-14 anos) com pedido de CD4 na primeira consulta clínica depois do diagnóstico de HIV+");
        break;
      case 6:
        cd.setName(
            "9.6 % de crianças  (0-14 anos) HIV+ que receberam o resultado do primeiro CD4 dentro de 33 dias  após a primeira consulta clínica");
        break;
      case 8:
        cd.setName("");
        break;
    }

    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    if (numerator == 1) {
      cd.addSearch("DENOMINATOR", EptsReportUtils.map(getMQ9Den(1), MAPPING1));
    } else if (numerator == 2) {
      cd.addSearch("DENOMINATOR", EptsReportUtils.map(getMQ9Den(2), MAPPING1));
    } else if (numerator == 3) {
      cd.addSearch(
          "DENOMINATOR",
          EptsReportUtils.map(getAdultPatientsRestartedWithCd4RequestAndResult(3), MAPPING1));
    } else if (numerator == 4) {
      cd.addSearch(
          "DENOMINATOR",
          EptsReportUtils.map(getPatientsRestartedAndEligibleForCd4Request(4), MAPPING1));
    } else if (numerator == 5 || numerator == 6) {
      cd.addSearch("DENOMINATOR", EptsReportUtils.map(getMQ9Den5and6(), MAPPING1));
    } else if (numerator == 8) {
      cd.addSearch(
          "DENOMINATOR",
          EptsReportUtils.map(getPatientsRestartedAndEligibleForCd4Request(8), MAPPING1));
    }

    cd.addSearch(
        "REQUEST1CONSULTA",
        EptsReportUtils.map(
            getRequestForCd4OnFirstClinicalConsultationDuringInclusionPeriod(),
            "startDate=${revisionEndDate-12m+1d},endDate=${revisionEndDate-9m},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "REQUESTONRESTART",
        EptsReportUtils.map(getPatientsWithCd4RequestsOnRestartedTarvDate(), MAPPING3));

    cd.addSearch(
        "RESULTS1CONSULTA",
        EptsReportUtils.map(
            getCd4ResultAfterFirstConsultationOnInclusionPeriod(),
            "startDate=${revisionEndDate-12m+1d},endDate=${revisionEndDate-9m},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "RESULTSONRESTART",
        EptsReportUtils.map(getPatientsWithCd4ResultsOnRestartedTarvDate(), MAPPING3));

    if (numerator == 1 || numerator == 5) {
      cd.setCompositionString("DENOMINATOR AND REQUEST1CONSULTA");
    } else if (numerator == 3) {
      cd.setCompositionString("DENOMINATOR AND REQUESTONRESTART");
    } else if (numerator == 4) {
      cd.setCompositionString("DENOMINATOR AND RESULTSONRESTART");
    } else if (numerator == 2 || numerator == 6) {
      cd.setCompositionString("DENOMINATOR AND RESULTS1CONSULTA");
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
   *
   * @param flag indicator number
   * @return CohortDefinition
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
   *
   * <p>* @return CohortDefinition
   */
  public CohortDefinition getCombinedB13ForCat15Indicators() {
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

  /**
   * <b>Select all patients with at least one of the following models registered in Ficha Clinica
   * (encounter type 6, encounter_datetime) during the revision period (start date and end date)</b>
   *
   * <ul>
   *   *
   *   <li>Last record of GAAC (concept id 23724) and the response is “Iniciar” * (value_coded,
   *       concept id 1256) or “Continua” (value_coded, concept id * 1257)
   *   <li>Last record of DT (concept id 23730) and the response is “Iniciar” * (value_coded,
   *       concept id 1256) or “Continua” (value_coded, concept id * 1257) or Type of dispensation
   *       (concept id 23793) value coded DT * (concept id 23888)
   *   <li>Last record of DS (concept id 23888) and the response is “Iniciar” * (value_coded,
   *       concept id 1256) or “Continua” (value_coded, concept id * 1257) or Type of dispensation
   *       (concept id 23793) value coded DT * (concept id 23720)
   *   <li>Last record of FR (concept id 23729) and the response is “ Iniciar” * (value_coded,
   *       concept id 1256) or “Continua” (value_coded, concept id * 1257)
   *   <li>Last record of DC (concept id 23731) and the response is “Iniciar” * (value_coded,
   *       concept id 1256) or “Continua” (value_coded, concept id * 1257)
   *   <li>Last record of Dispensing mode (concept id 165174) and the response is * “Dispensa
   *       Comunitária via APE (DCAPE)” (value_coded, concept id * 165179)
   *   <li>Last record of FARMAC (concept id 165177) and the response is * “Iniciar” (value_coded,
   *       concept id 1256) or “Continua” (value_coded, * concept id 1257)
   * </ul>
   *
   * @return
   */
  public CohortDefinition getMQMdsC() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("C: MDS para pacientes estáveis");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1257", hivMetadata.getContinueRegimenConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("23724", hivMetadata.getGaac().getConceptId());
    map.put("23730", hivMetadata.getQuarterlyDispensation().getConceptId());
    map.put("23888", hivMetadata.getSemiannualDispensation().getConceptId());
    map.put("23729", hivMetadata.getRapidFlow().getConceptId());
    map.put("23731", hivMetadata.getCommunityDispensation().getConceptId());
    map.put("165177", hivMetadata.getLastRecordOfFarmacConcept().getConceptId());
    map.put("165179", hivMetadata.getDispensaComunitariaViaApeConcept().getConceptId());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
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
            + "                   WHERE  p.voided = 0 "
            + "                          AND e.voided = 0 "
            + "                          AND e.location_id = :location "
            + "                          AND e.encounter_type = ${6} "
            + "                          AND e.encounter_datetime BETWEEN "
            + "                              :startDate AND :endDate "
            + "                   GROUP  BY p.patient_id) last_consultation "
            + "               ON last_consultation.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_type = ${6} "
            + "       AND ( ( o.concept_id = ${23724} "
            + "               AND (o.value_coded = ${1257} OR o.value_coded = ${1256})) "
            + "              OR ( o.concept_id = ${23730} "
            + "                   AND (o.value_coded = ${1257} OR o.value_coded = ${1256})) "
            + "              OR ( o.concept_id = ${23888} "
            + "                   AND (o.value_coded = ${1257} OR o.value_coded = ${1256})) "
            + "              OR ( o.concept_id = ${23729} "
            + "                   AND (o.value_coded = ${1257} OR o.value_coded = ${1256})) "
            + "              OR ( o.concept_id = ${165174} "
            + "                   AND (o.value_coded = ${165179})) "
            + "              OR ( o.concept_id = ${165177} "
            + "                   AND (o.value_coded = ${1257} OR o.value_coded = ${1256})) "
            + "              OR ( o.concept_id = ${23731} "
            + "                   AND (o.value_coded = ${1257} OR o.value_coded = ${1256}))) "
            + "       AND e.encounter_datetime < last_consultation.encounter_datetime";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  public CohortDefinition getMQ15DenMDS() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Denominator 15 - Pacientes elegíveis a MDS");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition Mq15A = intensiveMonitoringCohortQueries.getMI15A();
    CohortDefinition Mq15B1 = intensiveMonitoringCohortQueries.getMI15B1();
    CohortDefinition Mq15C = getMQ15CPatientsMarkedAsPregnant();
    CohortDefinition Mq15D = getMQ15DPatientsMarkedAsBreastfeeding();
    CohortDefinition Mq15F = intensiveMonitoringCohortQueries.getMI15F();
    CohortDefinition Mq15G = intensiveMonitoringCohortQueries.getMI15G();
    CohortDefinition alreadyMds = getPatientsAlreadyEnrolledInTheMdc();
    CohortDefinition onTB = commonCohortQueries.getPatientsOnTbTreatment();
    CohortDefinition onSK = getPatientsWithSarcomaKarposi();
    CohortDefinition returned = getPatientsWhoReturned();
    CohortDefinition endTb = getPatientsWhoEndedTbTreatmentWithin30DaysOfLastClinicalConslutation();

    cd.addSearch(
        "A",
        EptsReportUtils.map(
            Mq15A, "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));
    cd.addSearch(
        "B1",
        EptsReportUtils.map(
            Mq15B1, "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));
    cd.addSearch(
        "C",
        EptsReportUtils.map(
            Mq15C,
            "startDate=${revisionEndDate-14m+1d},endDate=${revisionEndDate},location=${location}"));
    cd.addSearch(
        "D",
        EptsReportUtils.map(
            Mq15D,
            "startDate=${revisionEndDate-14m+1d},endDate=${revisionEndDate},location=${location}"));
    cd.addSearch(
        "F",
        EptsReportUtils.map(
            Mq15F, "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));
    cd.addSearch(
        "G", EptsReportUtils.map(Mq15G, "endDate=${revisionEndDate},location=${location}"));
    cd.addSearch(
        "MDS",
        EptsReportUtils.map(
            alreadyMds,
            "startDate=${revisionEndDate-12m+1d},endDate=${revisionEndDate},location=${location}"));
    cd.addSearch(
        "onTB", EptsReportUtils.map(onTB, "endDate=${revisionEndDate},location=${location}"));
    cd.addSearch(
        "onSK", EptsReportUtils.map(onSK, "endDate=${revisionEndDate},location=${location}"));
    cd.addSearch(
        "returned",
        EptsReportUtils.map(returned, "endDate=${revisionEndDate},location=${location}"));
    cd.addSearch(
        "adverseReaction",
        EptsReportUtils.map(
            genericCohortQueries.hasCodedObs(
                hivMetadata.getAdverseReaction(),
                BaseObsCohortDefinition.TimeModifier.ANY,
                SetComparator.IN,
                Arrays.asList(
                    hivMetadata.getAdultoSeguimentoEncounterType(),
                    hivMetadata.getPediatriaSeguimentoEncounterType()),
                Arrays.asList(
                    hivMetadata.getCytopeniaConcept(),
                    hivMetadata.getPancreatitis(),
                    hivMetadata.getNephrotoxicityConcept(),
                    hivMetadata.getHepatitisConcept(),
                    hivMetadata.getStevensJonhsonSyndromeConcept(),
                    hivMetadata.getHypersensitivityToAbcOrRailConcept(),
                    hivMetadata.getLacticAcidosis(),
                    hivMetadata.getHepaticSteatosisWithHyperlactataemiaConcept())),
            "onOrAfter=${revisionEndDate-6m},onOrBefore=${revisionEndDate},locationList=${location}"));

    cd.addSearch(
        "AGE",
        EptsReportUtils.map(
            ageCohortQueries.createXtoYAgeCohort("Ages", 2, 200), "effectiveDate=${endDate}"));
    cd.addSearch(
        "endTb",
        EptsReportUtils.map(endTb, "revisionEndDate=${revisionEndDate},location=${location}"));

    cd.setCompositionString(
        "A AND B1 AND NOT (C OR D OR F OR G OR MDS OR onTB OR endTb OR adverseReaction OR onSK OR returned) AND AGE");

    return cd;
  }

  public CohortDefinition getMQ15NumeratorMDS() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Numerator MQ 15 - Pacientes elegíveis a MDS");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition Mq15DenMDS = getMQ15DenMDS();
    CohortDefinition MqK = intensiveMonitoringCohortQueries.getMI15K();

    List<Integer> mdsConcepts =
        Arrays.asList(
            hivMetadata.getGaac().getConceptId(),
            hivMetadata.getQuarterlyDispensation().getConceptId(),
            hivMetadata.getDispensaComunitariaViaApeConcept().getConceptId(),
            hivMetadata.getDescentralizedArvDispensationConcept().getConceptId(),
            hivMetadata.getRapidFlow().getConceptId(),
            hivMetadata.getSemiannualDispensation().getConceptId(),
            hivMetadata.getAnnualArvDispensationConcept().getConceptId());

    List<Integer> states = Arrays.asList(hivMetadata.getStartDrugs().getConceptId());

    CohortDefinition mds =
        getPatientsWhoHadMdsOnMostRecentClinicalAndPickupOnFilaFR36(mdsConcepts, states);

    cd.addSearch(
        "MQ15DenMDS",
        EptsReportUtils.map(
            Mq15DenMDS,
            "startDate=${startDate},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "MDS",
        EptsReportUtils.map(
            mds,
            "startDate=${revisionEndDate-12m+1d},endDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "AGE",
        EptsReportUtils.map(
            ageCohortQueries.createXtoYAgeCohort("Ages", 2, 200), "effectiveDate=${endDate}"));

    cd.setCompositionString("MQ15DenMDS AND MDS AND AGE");
    return cd;
  }

  public CohortDefinition getMQ15MdsDen14() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("15.14 - % de inscritos em MDS que receberam CV");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition Mq15A = intensiveMonitoringCohortQueries.getMI15A();
    CohortDefinition alreadyMdc = getPatientsAlreadyEnrolledInTheMdc();

    CohortDefinition Mq15H = intensiveMonitoringCohortQueries.getMI15H();

    cd.addSearch(
        "A",
        EptsReportUtils.map(
            Mq15A, "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));
    cd.addSearch(
        "MDC",
        EptsReportUtils.map(
            alreadyMdc, "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "H",
        EptsReportUtils.map(
            Mq15H, "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    cd.setCompositionString("A AND MDC AND H");
    return cd;
  }

  public CohortDefinition getMQ15MdsNum14() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Numerator 15.14: # de pacientes inscritos em MDS ");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition Mq15DenMds14 = getMQ15MdsDen14();
    CohortDefinition hadFilaAfterClinical =
        getPatientsWhoHadPickupOnFilaAfterMostRecentVlOnFichaClinica();

    cd.addSearch(
        "Mq15DenMds14",
        EptsReportUtils.map(
            Mq15DenMds14,
            "startDate=${startDate},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "FAC",
        EptsReportUtils.map(
            hadFilaAfterClinical,
            "startDate=${startDate},endDate=${revisionEndDate},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.setCompositionString("Mq15DenMds14 AND FAC");

    return cd;
  }

  public CohortDefinition getMI15MdsNum14() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Numerator 15.14: # de pacientes inscritos em MDS ");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    List<Integer> dispensationTypes =
        Arrays.asList(
            hivMetadata.getGaac().getConceptId(),
            hivMetadata.getQuarterlyDispensation().getConceptId(),
            hivMetadata.getDispensaComunitariaViaApeConcept().getConceptId(),
            hivMetadata.getDescentralizedArvDispensationConcept().getConceptId(),
            hivMetadata.getRapidFlow().getConceptId(),
            hivMetadata.getSemiannualDispensation().getConceptId());

    List<Integer> states =
        Collections.singletonList(hivMetadata.getCompletedConcept().getConceptId());

    CohortDefinition Mq15DenMds14 = getMQ15MdsDen14();

    CohortDefinition MdsFimNum14 =
        getPatientsWithMdcOnMostRecentClinicalFormWithFollowingDispensationTypesAndState(
            dispensationTypes, states);

    CohortDefinition hadFilaAfterClinical =
        getPatientsWhoHadPickupOnFilaAfterMostRecentVlOnFichaClinica();

    cd.addSearch(
        "Mq15DenMds14",
        EptsReportUtils.map(
            Mq15DenMds14,
            "startDate=${startDate},revisionEndDate=${revisionEndDate-1m},location=${location}"));

    cd.addSearch(
        "MdsFimNum14",
        EptsReportUtils.map(
            MdsFimNum14, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "FAC",
        EptsReportUtils.map(
            hadFilaAfterClinical,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.setCompositionString("Mq15DenMds14 AND FAC");

    return cd;
  }

  public CohortDefinition getMQMI15DEN15WithoutExclusions() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("15.15 % de pacientes inscritos em MDS em TARV há mais de 21 meses ");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition Mq15A = intensiveMonitoringCohortQueries.getMI15A();
    CohortDefinition alreadyMdc = getPatientsAlreadyEnrolledInTheMdc();
    CohortDefinition Mq15B2 = intensiveMonitoringCohortQueries.getMI15B2(24);

    cd.addSearch(
        "A",
        EptsReportUtils.map(
            Mq15A, "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));
    cd.addSearch(
        "MDC",
        EptsReportUtils.map(
            alreadyMdc, "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));
    cd.addSearch(
        "B2",
        EptsReportUtils.map(
            Mq15B2, "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    cd.setCompositionString("A AND MDC AND B2");
    return cd;
  }

  public CohortDefinition getMQ15Den15() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("15.15 % de pacientes inscritos em MDS em TARV há mais de 21 meses MQ ");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition Mq15withoutExclusions = getMQMI15DEN15WithoutExclusions();

    cd.addSearch(
        "A",
        EptsReportUtils.map(
            Mq15withoutExclusions,
            "startDate=${startDate},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.setCompositionString("A");
    return cd;
  }

  public CohortDefinition getMI15Den15() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("15.15 % de pacientes inscritos em MDS em TARV há mais de 21 meses MI ");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition Mq15withoutExclusions = getMQMI15DEN15WithoutExclusions();
    CohortDefinition Mq15P =
        intensiveMonitoringCohortQueries.getPatientsWhoHadLabInvestigationsRequest();

    cd.addSearch(
        "A",
        EptsReportUtils.map(
            Mq15withoutExclusions,
            "startDate=${startDate},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.setCompositionString("A");
    return cd;
  }

  public CohortDefinition getAgeOnObsDatetime(Integer minAge, Integer maxAge) {
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

  public CohortDefinition getMQ15MdsNum15() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("15.15 % de pacientes inscritos em MDS em TARV há mais de 21 meses ");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition Mq15MdsDen15 = getMQ15Den15();
    CohortDefinition Mq15I = intensiveMonitoringCohortQueries.getMI15I(24, 10, 20);

    cd.addSearch(
        "Mq15MdsDen15",
        EptsReportUtils.map(
            Mq15MdsDen15,
            "startDate=${startDate},revisionEndDate=${revisionEndDate},location=${location}"));
    cd.addSearch(
        "Mq15I",
        EptsReportUtils.map(
            Mq15I, "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    cd.setCompositionString("Mq15MdsDen15 AND Mq15I");

    return cd;
  }

  public CohortDefinition getMI15Num15() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("15.15 % de pacientes inscritos em MDS em TARV há mais de 21 meses ");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition Mi15Den = getMI15Den15();
    CohortDefinition Mq15I = intensiveMonitoringCohortQueries.getMI15I(20, 10, 20);

    cd.addSearch(
        "MI15DEN15",
        EptsReportUtils.map(
            Mi15Den,
            "startDate=${startDate},revisionEndDate=${revisionEndDate},location=${location}"));
    cd.addSearch(
        "Mq15I",
        EptsReportUtils.map(
            Mq15I, "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    cd.setCompositionString("MI15DEN15 AND Mq15I");

    return cd;
  }

  public CohortDefinition getMQDen15Dot16() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName(
        "15.16. % de utentes inscritos em MDS (para pacientes estáveis) com supressão viral");

    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addSearch(
        "A",
        EptsReportUtils.map(
            txPvls.getPatientsWithViralLoadResultsAndOnArtForMoreThan3Months(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "MDC",
        EptsReportUtils.map(
            getPatientsWhoHadMdsOnMostRecentClinicalAndPickupOnFilaFR36BasedOnLastVl(
                Arrays.asList(
                    hivMetadata.getGaac().getConceptId(),
                    hivMetadata.getQuarterlyDispensation().getConceptId(),
                    hivMetadata.getDispensaComunitariaViaApeConcept().getConceptId(),
                    hivMetadata.getDescentralizedArvDispensationConcept().getConceptId(),
                    hivMetadata.getRapidFlow().getConceptId(),
                    hivMetadata.getSemiannualDispensation().getConceptId()),
                Arrays.asList(
                    hivMetadata.getStartDrugs().getConceptId(),
                    hivMetadata.getContinueRegimenConcept().getConceptId())),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.setCompositionString("A AND MDC");
    return cd;
  }

  public CohortDefinition getMQNum15Dot16() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName(
        "15.16. % de utentes inscritos em MDS (para pacientes estáveis) com supressão viral");

    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    cd.addSearch(
        "B",
        EptsReportUtils.map(
            txPvls.getPatientsWithViralLoadSuppressionWhoAreOnArtMoreThan3Months(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "MDC",
        EptsReportUtils.map(
            getPatientsWhoHadMdsOnMostRecentClinicalAndPickupOnFilaFR36BasedOnLastVl(
                Arrays.asList(
                    hivMetadata.getGaac().getConceptId(),
                    hivMetadata.getQuarterlyDispensation().getConceptId(),
                    hivMetadata.getDispensaComunitariaViaApeConcept().getConceptId(),
                    hivMetadata.getDescentralizedArvDispensationConcept().getConceptId(),
                    hivMetadata.getRapidFlow().getConceptId(),
                    hivMetadata.getSemiannualDispensation().getConceptId()),
                Arrays.asList(
                    hivMetadata.getStartDrugs().getConceptId(),
                    hivMetadata.getContinueRegimenConcept().getConceptId())),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.setCompositionString("B AND MDC");
    return cd;
  }

  /**
   * <b>C - All female patients registered as “Pregnant” on Ficha Clinica during the revision period
   * (startDateInclusion = endDateRevision - 14 months and endDateRevision):</b>
   *
   * <ul>
   *   <li>all patients registered in Ficha Clínica (encounter type=6) with “Gestante”(concept_id
   *       1982) value_coded equal to “Yes” (concept_id 1065) and sex=Female and encounter_datetime
   *       >= startDateInclusion (endDateRevision - 14 months) and encounter_datetime <=
   *       endDateRevision. <i>NOTE: IF the patient has both states pregnant and breastfeeding, the
   *       system will consider the most recent registry. If the patient has both states on the same
   *       day, the system will consider the patient as pregnant.</i>
   * </ul>
   */
  public CohortDefinition getMQ15CPatientsMarkedAsPregnant() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("All female patients registered as pregnant but not breastfeeding");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6332", hivMetadata.getBreastfeeding().getConceptId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());

    String query =
        "SELECT pregnant.patient_id "
            + "FROM   (SELECT p.patient_id, MAX(e.encounter_datetime) AS pregnancy_date "
            + "        FROM   patient p "
            + "               INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "               INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "               INNER JOIN person ps ON ps.person_id = p.patient_id "
            + "        WHERE  e.encounter_type = ${6} "
            + "               AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "               AND e.location_id = :location "
            + "               AND e.voided = 0 "
            + "               AND o.concept_id = ${1982} "
            + "               AND o.value_coded = ${1065} "
            + "               AND o.voided = 0 "
            + "               AND ps.gender = 'F' "
            + "               AND ps.voided = 0 "
            + "        GROUP  BY p.patient_id) pregnant "
            + "       LEFT JOIN (SELECT p.patient_id, MAX(e.encounter_datetime) breastfeed_date "
            + "                  FROM   patient p "
            + "                         INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                         INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                         INNER JOIN person ps ON ps.person_id = p.patient_id "
            + "                  WHERE  e.encounter_type = ${6} "
            + "                         AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "                         AND e.location_id = :location "
            + "                         AND e.voided = 0 "
            + "                         AND o.concept_id = ${6332} "
            + "                         AND o.value_coded = ${1065} "
            + "                         AND o.voided = 0 "
            + "                         AND ps.gender = 'F' "
            + "                         AND ps.voided = 0 "
            + "                  GROUP  BY p.patient_id) AS breastfeeding "
            + "              ON breastfeeding.patient_id = pregnant.patient_id "
            + "WHERE  pregnant.pregnancy_date >= breastfeeding.breastfeed_date OR breastfeeding.breastfeed_date IS NULL "
            + "GROUP  BY pregnant.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));
    return sqlCohortDefinition;
  }

  /**
   * <b>D - All female patients registered as “Breastfeeding” on Ficha Clinica during the revision
   * period (startDateInclusion = endDateRevision - 14 months and endDateRevision):</b>
   *
   * <ul>
   *   <li>all patients registered in Ficha Clínica (encounter type=6) with “Lactante”(concept_id
   *       6332) value_coded equal to “Yes” (concept_id 1065) and sex=Female and encounter_datetime
   *       >= startDateInclusion (endDateRevision - 14 months) and encounter_datetime <=
   *       endDateRevision <i>NOTE: IF the patient has both states pregnant and breastfeeding, the
   *       system will consider the most recent registry. If the patient has both states on the same
   *       day, the system will consider the patient as pregnant.</i>
   * </ul>
   */
  public CohortDefinition getMQ15DPatientsMarkedAsBreastfeeding() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("All female patients registered as Breastfeeding but not pregnant");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6332", hivMetadata.getBreastfeeding().getConceptId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());

    String query =
        "SELECT breastfeeding.patient_id "
            + "FROM  (SELECT p.patient_id, MAX(e.encounter_datetime) breastfeed_date "
            + "       FROM   patient p INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "              INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "              INNER JOIN person ps ON ps.person_id = p.patient_id "
            + "       WHERE  e.encounter_type = ${6} "
            + "              AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "              AND e.location_id = :location "
            + "              AND e.voided = 0 "
            + "              AND o.concept_id = ${6332} "
            + "              AND o.value_coded = ${1065} "
            + "              AND o.voided = 0 "
            + "              AND ps.gender = 'F' "
            + "              AND ps.voided = 0 "
            + "       GROUP  BY p.patient_id) breastfeeding "
            + "      LEFT JOIN (SELECT p.patient_id, MAX(e.encounter_datetime) pregnancy_date "
            + "                 FROM   patient p "
            + "                        INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                        INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                        INNER JOIN person ps ON ps.person_id = p.patient_id "
            + "                 WHERE  e.encounter_type = ${6} "
            + "                        AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "                        AND e.location_id = :location "
            + "                        AND e.voided = 0 "
            + "                        AND o.concept_id = ${1982} "
            + "                        AND o.value_coded = ${1065} "
            + "                        AND o.voided = 0 "
            + "                        AND ps.gender = 'F' "
            + "                        AND ps.voided = 0 "
            + "                 GROUP  BY p.patient_id) pregnant "
            + "             ON pregnant.patient_id = breastfeeding.patient_id "
            + "WHERE  breastfeeding.breastfeed_date > pregnant.pregnancy_date "
            + "        OR pregnant.pregnancy_date IS NULL "
            + "GROUP  BY breastfeeding.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));
    return sqlCohortDefinition;
  }

  /**
   *
   *
   * <ul>
   *   <b>Pacientes que Iniciaram TPT - Isoniazida durante período de inclusão</b>
   *   <li>O sistema irá identificar pacientes que iniciaram TPT – Isoniazida durante o período de
   *       inclusão seleccionando os pacientes:
   *       <p>com registo de “Última Profilaxia TPT” = “INH” e “Última Profilaxia TPT (Data
   *       Início)”, no formulário “Ficha de Resumo”, durante o período de inclusão (“Última
   *       Profilazia TPT (Data Início)” >= “Data Início Inclusão” e <= “Data Fim Inclusão”). Em
   *       caso de existência de mais que uma Ficha Resumo com registo do “Última Profilaxia (Data
   *       Início)”, deve-se considerar o último registo durante o período de inclusão ou.
   *   <li>Nota: sendo a “Data Início TPT - Isonazida” do paciente a data mais recente entre os
   *       critérios acima listados.
   * </ul>
   *
   * @return CohortDefinition
   *     <li><strong>Should</strong> Returns empty if there is no patient who meets the conditions
   *     <li><strong>Should</strong> fetch all patients with B4_1 criteria
   */
  public CohortDefinition getB4And2() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("B4_2");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("23985", tbMetadata.getRegimeTPTConcept().getConceptId());
    map.put("656", tbMetadata.getIsoniazidConcept().getConceptId());
    map.put("165308", tbMetadata.getDataEstadoDaProfilaxiaConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());

    String query =
        "SELECT  final.patient_id "
            + "FROM "
            + "( "
            + "   SELECT p.patient_id, MAX(o2.obs_datetime) last_encounter "
            + "   FROM patient p "
            + "         INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "         INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "         INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "   WHERE p.voided = 0 "
            + "     AND e.voided = 0 "
            + "     AND o.voided = 0 "
            + "     AND o2.voided = 0 "
            + "     AND e.location_id = :location "
            + "     AND e.encounter_type = ${53} "
            + "     AND ( ( o.concept_id = ${23985} AND o.value_coded = ${656}) "
            + "     AND (o2.concept_id = ${165308} AND o2.value_coded = ${1256} "
            + "     AND     o2.obs_datetime BETWEEN :startDate AND :endDate ) ) "
            + "   GROUP BY p.patient_id "
            + ") AS  final";

    StringSubstitutor sb = new StringSubstitutor(map);
    cd.setQuery(sb.replace(query));
    return cd;
  }

  /**
   * <b>RF12</b> Utentes que <b>Iniciaram TPT - Isoniazida</b> durante período de inclusão
   *
   * <p>O sistema irá identificar utentes que iniciaram TPT – Isoniazida durante o período de
   * inclusão seleccionando os utentes:
   *
   * <ul>
   *   <li>com registo de “Última Profilaxia TPT” = “INH” e “Última Profilaxia TPT (Data Início)”,
   *       no formulário “Ficha Resumo”, durante o período de inclusão (“Última Profilaxia TPT (Data
   *       Início)” >= “Data Início Inclusão” e <= “Data Fim Inclusão”). Em caso de existência de
   *       mais que uma Ficha Resumo com registo dao “Última Profilaxia (Data Início)”, deve-se
   *       considerar o a data mais recente durante o período de inclusão
   *   <li>com o registo de “Profilaxia TPT” = ”INH” e “Estado da Profilaxia” =“Inicio” numa
   *       consulta clínica (Ficha Clínica) ocorrida durante o período de inclusão (“Data de
   *       Consulta”>= “Data Início Inclusão” e <= “Data Fim Inclusão”). Em caso de existência de
   *       mais que uma Ficha Clínica com registo do “Início”, deve-se considerar o último registo
   *       durante o período de inclusão.
   *       <p>sendo a <b>“Data Início TPT - Isoniazida”</b> do utente a data mais recente entre os
   *       critérios acima listados.
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoStartedTpt(boolean inhOr3hp) {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("iniciaram TPT – Isoniazida");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("23985", tbMetadata.getRegimeTPTConcept().getConceptId());
    map.put("656", tbMetadata.getIsoniazidConcept().getConceptId());
    map.put("23954", tbMetadata.get3HPConcept().getConceptId());
    map.put("165308", tbMetadata.getDataEstadoDaProfilaxiaConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());

    String query =
        "SELECT tpt_inh_start.patient_id "
            + "FROM  ( "
            + "          SELECT final.patient_id, "
            + "                 MAX(final.last_encounter) AS tpt_start_date "
            + "          FROM   ( "
            + "                     SELECT     p.patient_id, "
            + "                                MAX(o2.obs_datetime) last_encounter "
            + "                     FROM       patient p "
            + "                                    INNER JOIN encounter e "
            + "                                               ON         p.patient_id = e.patient_id "
            + "                                    INNER JOIN obs o "
            + "                                               ON         o.encounter_id = e.encounter_id "
            + "                                    INNER JOIN obs o2 "
            + "                                               ON         o2.encounter_id = e.encounter_id "
            + "                     WHERE      p.voided = 0 "
            + "                       AND        e.voided = 0 "
            + "                       AND        o.voided = 0 "
            + "                       AND        o2.voided = 0 "
            + "                       AND        e.location_id = :location "
            + "                       AND        e.encounter_type = ${53} "
            + "                       AND        ( ( "
            + "                                        o.concept_id = ${23985} ";
    query +=
        inhOr3hp
            ? "                                            AND        o.value_coded = ${656}) "
            : "                                            AND        o.value_coded = ${23954}) ";
    query +=
        "                         AND        ( "
            + "                                        o2.concept_id = ${165308} "
            + "                                            AND        o2.value_coded = ${1256} "
            + "                                            AND        o2.obs_datetime BETWEEN :startDate AND :endDate ) ) "
            + "                     GROUP BY   p.patient_id "
            + "                     UNION "
            + "                     SELECT     p.patient_id, "
            + "                                MAX(o2.obs_datetime) last_encounter "
            + "                     FROM       patient p "
            + "                                    INNER JOIN encounter e "
            + "                                               ON         p.patient_id = e.patient_id "
            + "                                    INNER JOIN obs o "
            + "                                               ON         o.encounter_id = e.encounter_id "
            + "                                    INNER JOIN obs o2 "
            + "                                               ON         o2.encounter_id = e.encounter_id "
            + "                     WHERE      p.voided = 0 "
            + "                       AND        e.voided = 0 "
            + "                       AND        o.voided = 0 "
            + "                       AND        o2.voided = 0 "
            + "                       AND        e.location_id = :location "
            + "                       AND        e.encounter_type = ${6} "
            + "                       AND        ( ( "
            + "                                        o.concept_id = ${23985} ";
    query +=
        inhOr3hp
            ? "                                            AND        o.value_coded = ${656}) "
            : "                                            AND        o.value_coded = ${23954}) ";
    query +=
        "                         AND        ( "
            + "                                        o2.concept_id = ${165308} "
            + "                                            AND        o2.value_coded = ${1256} "
            + "                                            AND        o2.obs_datetime BETWEEN :startDate AND :endDate) ) "
            + "                     GROUP BY   p.patient_id ) final "
            + "        GROUP BY final.patient_id) tpt_inh_start";

    StringSubstitutor sb = new StringSubstitutor(map);
    cd.setQuery(sb.replace(query));
    return cd;
  }

  /**
   *
   *
   * <ul>
   *   <b>Pacientes que Iniciaram TPT - Isoniazida durante período de inclusão</b>
   *   <li>O sistema irá identificar pacientes que iniciaram TPT – Isoniazida durante o período de
   *       inclusão seleccionando os pacientes:
   *       <p>com o registo de “Profilaxia TPT”=”INH” e “Estado da Profilaxia” =“Inicio” numa
   *       consulta clínica (Ficha Clínica) ocorrida durante o período de inclusão (“Data de
   *       Consulta”>= “Data Início Inclusão” e <= “Data Fim Inclusão”). Em caso de existência de
   *       mais que uma Ficha Clínica com registo do “Início”, deve-se considerar o último registo
   *       durante o período de inclusão.
   *   <li>Nota: sendo a “Data Início TPT - Isonazida” do paciente a data mais recente entre os
   *       critérios acima listados.
   * </ul>
   *
   * @return CohortDefinition
   *     <li><strong>Should</strong> Returns empty if there is no patient who meets the conditions
   *     <li><strong>Should</strong> fetch all patients with B4_2 criteria
   */
  public CohortDefinition getB4And1() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("B4_1");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("23985", tbMetadata.getRegimeTPTConcept().getConceptId());
    map.put("656", tbMetadata.getIsoniazidConcept().getConceptId());
    map.put("165308", tbMetadata.getDataEstadoDaProfilaxiaConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());

    String query =
        ""
            + "SELECT  final.patient_id "
            + "FROM "
            + "( "
            + "   SELECT p.patient_id, MAX(o2.obs_datetime) last_encounter "
            + "   FROM patient p "
            + "         INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "         INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "         INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "   WHERE p.voided = 0 "
            + "     AND e.voided = 0 "
            + "     AND o.voided = 0 "
            + "     AND o2.voided = 0 "
            + "     AND e.location_id = :location "
            + "     AND e.encounter_type = ${6} "
            + "     AND ( (o.concept_id = ${23985} "
            + "     AND o.value_coded = ${656}) "
            + "     AND   (o2.concept_id = ${165308} "
            + "     AND o2.value_coded = ${1256} "
            + "     AND o2.obs_datetime between :startDate AND :endDate) ) "
            + "   GROUP BY p.patient_id "
            + ") AS  final";

    StringSubstitutor sb = new StringSubstitutor(map);
    cd.setQuery(sb.replace(query));
    return cd;
  }

  /**
   *
   *
   * <ul>
   *   <b>Pacientes que Iniciaram TPT – 3HP durante período de inclusão</b>
   *   <li>O sistema irá identificar pacientes que iniciaram TPT – 3HP durante o período de inclusão
   *       seleccionando os pacientes:
   *       <p>com registo de “Última Profilaxia TPT” = “3HP” e “Última Profilaxia TPT (Data
   *       Início)”, no formulário “Ficha de Resumo”, durante o período de inclusão (“Última
   *       Profilazia TPT (Data Início)” >= “Data Início Inclusão” e <= “Data Fim Inclusão”). Em
   *       caso de existência de mais que uma Ficha Resumo com registo do “Última Profilaxia (Data
   *       Início)”, deve-se considerar o último registo durante o período de inclusão ou
   *   <li>Nota: sendo a “Data Início TPT – 3HP” do paciente a data mais recente entre os critérios
   *       acima listados.
   * </ul>
   *
   * @return CohortDefinition
   *     <li><strong>Should</strong> Returns empty if there is no patient who meets the conditions
   *     <li><strong>Should</strong> fetch all patients with B4_3 criteria
   */
  public CohortDefinition getB5And2() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("B5_2");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("23985", tbMetadata.getRegimeTPTConcept().getConceptId());
    map.put("23954", tbMetadata.get3HPConcept().getConceptId());
    map.put("165308", tbMetadata.getDataEstadoDaProfilaxiaConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    String query =
        ""
            + "SELECT  final.patient_id "
            + "FROM "
            + "( "
            + "   SELECT p.patient_id, MAX(o2.obs_datetime) last_encounter "
            + "   FROM patient p "
            + "         INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "         INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "         INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "   WHERE p.voided = 0 "
            + "     AND e.voided = 0 "
            + "     AND o.voided = 0 "
            + "     AND o2.voided = 0 "
            + "     AND e.location_id = :location "
            + "     AND e.encounter_type = ${53} "
            + "     AND ( ( o.concept_id = ${23985} AND o.value_coded = ${23954} ) "
            + "     AND (o2.concept_id = ${165308} AND o2.value_coded = ${1256} "
            + "     AND     o2.obs_datetime BETWEEN :startDate AND :endDate ) ) "
            + "   GROUP BY p.patient_id "
            + ") AS  final";

    StringSubstitutor sb = new StringSubstitutor(map);
    cd.setQuery(sb.replace(query));
    return cd;
  }

  /**
   *
   *
   * <ul>
   *   <b>Pacientes que Iniciaram TPT – 3HP durante período de inclusão</b>
   *   <li>O sistema irá identificar pacientes que iniciaram TPT – 3HP durante o período de inclusão
   *       seleccionando os pacientes:
   *       <p>com o registo de “Profilaxia TPT”= ”3HP” e “Estado da Profilaxia” =“Inicio” numa
   *       consulta clínica (Ficha Clínica) ocorrida durante o período de inclusão (“Data de
   *       Consulta”>= “Data Início Inclusão” e <= “Data Fim Inclusão”). Em caso de existência de
   *       mais que uma Ficha Clínica com registo do “Início”, deve-se considerar o último registo
   *       durante o período de inclusão.
   *   <li>Nota: sendo a “Data Início TPT – 3HP” do paciente a data mais recente entre os critérios
   *       acima listados.
   * </ul>
   *
   * @return CohortDefinition
   *     <li><strong>Should</strong> Returns empty if there is no patient who meets the conditions
   *     <li><strong>Should</strong> fetch all patients with B4_4 criteria
   */
  public CohortDefinition getB5And1() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("B5_1");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("23985", tbMetadata.getRegimeTPTConcept().getConceptId());
    map.put("23954", tbMetadata.get3HPConcept().getConceptId());
    map.put("165308", tbMetadata.getDataEstadoDaProfilaxiaConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());

    String query =
        ""
            + "SELECT  final.patient_id "
            + "FROM "
            + "( "
            + "   SELECT p.patient_id, MAX(o2.obs_datetime) last_encounter "
            + "   FROM patient p "
            + "         INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "         INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "         INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "   WHERE p.voided = 0 "
            + "     AND e.voided = 0 "
            + "     AND o.voided = 0 "
            + "     AND o2.voided = 0 "
            + "     AND e.location_id = :location "
            + "     AND e.encounter_type = ${6} "
            + "     AND ( (o.concept_id = ${23985} "
            + "     AND o.value_coded = ${23954}) "
            + "     AND   (o2.concept_id = ${165308} "
            + "     AND o2.value_coded = ${1256} "
            + "     AND o2.obs_datetime between :startDate AND :endDate) ) "
            + "   GROUP BY p.patient_id "
            + ") AS  final";

    StringSubstitutor sb = new StringSubstitutor(map);
    cd.setQuery(sb.replace(query));
    return cd;
  }

  /**
   *
   *
   * <ul>
   *   <li>G_New: Filter all patients with the most recent date as “TPT end Date” between the
   *       following::
   *       <ul>
   *         <li>
   *             <p>"Profilaxia TPT"(concept id 23985) value coded INH(concept id 656) and Estado da
   *             Profilaxia (concept id 165308) value coded FIM(concept id 1267) durante o período
   *             de revisao (obs_datetime >= startDateRevision and <= endDateRevision) Nota: Em caso
   *             de existência de mais que uma Ficha Clínica com registo do “FIM”, deve-se
   *             considerar o último registo durante o período de revisao.
   *         <li>
   *             <p>" Ultima profilaxia TPT"(concept id 23985) value coded INH(concept id 656) and
   *             value_datetime(concept id 6129) during the revision period (value_datetime >=
   *             startDateRevision and <= endDateRevision) Nota: Em caso de existência de mais que
   *             uma Ficha Resumo com registo do “Última ProfilaxiaIsoniazida (Data FIM)”, deve-se
   *             considerar o último registo durante o período de revisao.
   *             <p>and “TPT Start Date” (the most recent date from B4_1 and B4_2) minus “TPT End
   *             Date” is between 170 days and 297 days
   *       </ul>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWithTptInhEnd() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("TPT INH End");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("23985", tbMetadata.getRegimeTPTConcept().getConceptId());
    map.put("656", tbMetadata.getIsoniazidConcept().getConceptId());
    map.put("165308", tbMetadata.getDataEstadoDaProfilaxiaConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("1267", hivMetadata.getCompletedConcept().getConceptId());

    String query =
        "SELECT p.patient_id  "
            + "FROM patient p  "
            + "    INNER JOIN (  "
            + "                SELECT tpt_end.patient_id, MAX(tpt_end.last_encounter) AS tpt_end_date  "
            + "                FROM (  "
            + "                         SELECT p.patient_id, MAX(o2.obs_datetime) last_encounter  "
            + "                         FROM patient p  "
            + "                                  INNER JOIN encounter e ON p.patient_id = e.patient_id  "
            + "                                  INNER JOIN obs o ON o.encounter_id = e.encounter_id  "
            + "                                  INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id  "
            + "                         WHERE p.voided = 0  "
            + "                           AND e.voided = 0  "
            + "                           AND o.voided = 0  "
            + "                           AND o2.voided = 0  "
            + "                           AND e.encounter_type = ${6}  "
            + "                           AND e.location_id = :location  "
            + "                           AND ( ( o.concept_id = ${23985}  "
            + "                           AND     o.value_coded = ${656} )  "
            + "                           AND   ( o2.concept_id = ${165308} "
            + "                           AND     o2.value_coded = ${1267} "
            + "                           AND     o2.obs_datetime BETWEEN :startDate AND :revisionEndDate ) ) "
            + "                         GROUP BY p.patient_id  "
            + "                         UNION  "
            + "                         SELECT p.patient_id, MAX(o2.obs_datetime) last_encounter  "
            + "                         FROM patient p  "
            + "                                  INNER JOIN encounter e ON p.patient_id = e.patient_id  "
            + "                                  INNER JOIN obs o ON o.encounter_id = e.encounter_id  "
            + "                                  INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id  "
            + "                         WHERE p.voided = 0  "
            + "                           AND e.voided = 0  "
            + "                           AND o.voided = 0  "
            + "                           AND o2.voided = 0  "
            + "                           AND e.location_id = :location  "
            + "                           AND e.encounter_type = ${53}  "
            + "                           AND ( ( o.concept_id = ${23985} AND o.value_coded = ${656} )  "
            + "                           AND   ( o2.concept_id = ${165308}  AND o2.value_coded = ${1267} "
            + "                           AND     o2.obs_datetime BETWEEN :startDate AND :revisionEndDate ) )  "
            + "                         GROUP BY p.patient_id  "
            + "                     ) AS tpt_end  "
            + "                GROUP BY  tpt_end.patient_id  "
            + "            ) AS tpt_fim ON tpt_fim.patient_id = p.patient_id  "
            + "    INNER JOIN (  "
            + "                SELECT tpt_start.patient_id, MAX(tpt_start.last_encounter) tpt_start_date  "
            + "                FROM (  "
            + "                         SELECT p.patient_id, MAX(o2.obs_datetime) last_encounter  "
            + "                         FROM patient p  "
            + "                                  INNER JOIN encounter e ON p.patient_id = e.patient_id  "
            + "                                  INNER JOIN obs o ON o.encounter_id = e.encounter_id  "
            + "                                  INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id  "
            + "                         WHERE p.voided = 0  "
            + "                           AND e.voided = 0  "
            + "                           AND o.voided = 0  "
            + "                           AND o2.voided = 0  "
            + "                           AND e.location_id = :location  "
            + "                           AND e.encounter_type = ${6}  "
            + "                           AND ( ( o.concept_id = ${23985}  "
            + "                           AND     o.value_coded = ${656} )  "
            + "                           AND   ( o2.concept_id = ${165308}  "
            + "                           AND     o2.value_coded = ${1256}   "
            + "                           AND     o2.obs_datetime BETWEEN :startDate AND :endDate ) )  "
            + "                         GROUP BY p.patient_id  "
            + "                         UNION  "
            + "                         SELECT p.patient_id, MAX(o2.obs_datetime) last_encounter  "
            + "                         FROM patient p  "
            + "                                  INNER JOIN encounter e ON p.patient_id = e.patient_id  "
            + "                                  INNER JOIN obs o ON o.encounter_id = e.encounter_id  "
            + "                                  INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id  "
            + "                         WHERE p.voided = 0  "
            + "                           AND e.voided = 0  "
            + "                           AND o.voided = 0  "
            + "                           AND o2.voided = 0  "
            + "                           AND e.location_id = :location  "
            + "                           AND e.encounter_type = ${53}  "
            + "                           AND ( ( o.concept_id = ${23985} AND o.value_coded = ${656} )  "
            + "                           AND   ( o2.concept_id = ${165308} AND o2.value_coded = ${1256} "
            + "                           AND     o2.obs_datetime BETWEEN :startDate AND :endDate ) )  "
            + "                         GROUP BY p.patient_id  "
            + "                     ) AS tpt_start  "
            + "                GROUP BY tpt_start.patient_id  "
            + "            ) AS tpt_inicio ON tpt_inicio.patient_id = p.patient_id  "
            + "WHERE p.voided = 0  "
            + "    AND TIMESTAMPDIFF(DAY, tpt_inicio.tpt_start_date,tpt_fim.tpt_end_date) BETWEEN  170 AND 231 ";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   *
   *
   * <ul>
   *   <li>L: Filter all patients with the most recent date as “TPT end Date” between the
   *       following::
   *       <ul>
   *         <li>
   *             <p>the most recent clinical consultation(encounter type 6)during the revision
   *             period(obs_datetime >= startDateRevision and <= endDateRevision) with “Ultima
   *             Profilaxia TPT (concept_id 23985) = 3HP (concept = 23954) and “Data Fim”
   *             (concept_id 165308 value 1267)
   *         <li>
   *             <p>the most recent “Última Profilaxia = 3HP (concept 23985 value 23954) and value
   *             datetime FIM (concept id 6129) registered in Ficha Resumo (encounter type 53)
   *             occurred during the revision period (value_datetime >= startDateRevision and <=
   *             endDateRevision)
   *             <p>and “TPT Start Date” (the most recent date from B5_1 and B5_2) minus “TPT End
   *             Date” is between 80 days and 198 days
   *       </ul>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWithTpt3hpEnd() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("G new 3HP");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("23985", tbMetadata.getRegimeTPTConcept().getConceptId());
    map.put("23954", tbMetadata.get3HPConcept().getConceptId());
    map.put("165308", tbMetadata.getDataEstadoDaProfilaxiaConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("1267", hivMetadata.getCompletedConcept().getConceptId());

    String query =
        "SELECT p.patient_id  "
            + "FROM patient p  "
            + "    INNER JOIN (  "
            + "                SELECT tpt_end.patient_id, MAX(tpt_end.last_encounter) AS tpt_end_date  "
            + "                FROM (  "
            + "                         SELECT p.patient_id, MAX(o2.obs_datetime) last_encounter  "
            + "                         FROM patient p  "
            + "                                  INNER JOIN encounter e ON p.patient_id = e.patient_id  "
            + "                                  INNER JOIN obs o ON o.encounter_id = e.encounter_id  "
            + "                                  INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id  "
            + "                         WHERE p.voided = 0  "
            + "                           AND e.voided = 0  "
            + "                           AND o.voided = 0  "
            + "                           AND o2.voided = 0  "
            + "                           AND e.location_id = :location  "
            + "                           AND e.encounter_type = ${6}  "
            + "                           AND ( ( o.concept_id = ${23985}  "
            + "                           AND     o.value_coded = ${23954})  "
            + "                           AND   ( o2.concept_id = ${165308}  "
            + "                           AND     o2.value_coded = ${1267}  "
            + "                           AND o2.obs_datetime BETWEEN :startDate AND :revisionEndDate ) )  "
            + "                         GROUP BY p.patient_id  "
            + "                         UNION  "
            + "                         SELECT p.patient_id, MAX(o2.obs_datetime) last_encounter  "
            + "                         FROM patient p  "
            + "                                  INNER JOIN encounter e ON p.patient_id = e.patient_id  "
            + "                                  INNER JOIN obs o ON o.encounter_id = e.encounter_id  "
            + "                                  INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id  "
            + "                         WHERE p.voided = 0  "
            + "                           AND e.voided = 0  "
            + "                           AND o.voided = 0  "
            + "                           AND o2.voided = 0  "
            + "                           AND e.location_id = :location  "
            + "                           AND e.encounter_type = ${53}  "
            + "                           AND ( ( o.concept_id = ${23985} AND o.value_coded = ${23954} ) "
            + "                           AND   ( o2.concept_id = ${165308}  AND o2.value_coded = ${1267} "
            + "                           AND     o2.obs_datetime BETWEEN :startDate AND :revisionEndDate ) )  "
            + "                         GROUP BY p.patient_id  "
            + "                     ) AS tpt_end  "
            + "                GROUP BY  tpt_end.patient_id  "
            + "            ) AS tpt_fim ON tpt_fim.patient_id = p.patient_id  "
            + "    INNER JOIN (  "
            + "                SELECT tpt_start.patient_id, MAX(tpt_start.last_encounter) tpt_start_date  "
            + "                FROM (  "
            + "                         SELECT p.patient_id, MAX(o2.obs_datetime) last_encounter  "
            + "                         FROM patient p  "
            + "                                  INNER JOIN encounter e ON p.patient_id = e.patient_id  "
            + "                                  INNER JOIN obs o ON o.encounter_id = e.encounter_id  "
            + "                                  INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id  "
            + "                         WHERE p.voided = 0  "
            + "                           AND e.voided = 0  "
            + "                           AND o.voided = 0  "
            + "                           AND o2.voided = 0  "
            + "                           AND e.location_id = :location  "
            + "                           AND e.encounter_type = ${6}  "
            + "                           AND ( ( o.concept_id = ${23985}  "
            + "                           AND     o.value_coded = ${23954})  "
            + "                           AND   ( o2.concept_id = ${165308}  "
            + "                           AND     o2.value_coded = ${1256}  "
            + "                           AND     o2.obs_datetime BETWEEN :startDate AND :endDate ) )  "
            + "                         GROUP BY p.patient_id  "
            + "                         UNION  "
            + "                         SELECT p.patient_id, MAX(o2.obs_datetime) last_encounter  "
            + "                         FROM patient p  "
            + "                                  INNER JOIN encounter e ON p.patient_id = e.patient_id  "
            + "                                  INNER JOIN obs o ON o.encounter_id = e.encounter_id  "
            + "                                  INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id  "
            + "                         WHERE p.voided = 0  "
            + "                           AND e.voided = 0  "
            + "                           AND o.voided = 0  "
            + "                           AND o2.voided = 0  "
            + "                           AND e.location_id = :location  "
            + "                           AND e.encounter_type = ${53}  "
            + "                           AND ( ( o.concept_id = ${23985} AND o.value_coded = ${23954} )  "
            + "                           AND   ( o2.concept_id = ${165308}  AND o2.value_coded = ${1256}  "
            + "                           AND     o2.obs_datetime BETWEEN :startDate AND :endDate ) )  "
            + "                         GROUP BY p.patient_id  "
            + "                     ) AS tpt_start  "
            + "                GROUP BY tpt_start.patient_id  "
            + "            ) AS tpt_inicio ON tpt_inicio.patient_id = p.patient_id  "
            + "WHERE p.voided = 0  "
            + "    AND TIMESTAMPDIFF(DAY, tpt_inicio.tpt_start_date,tpt_fim.tpt_end_date) BETWEEN  80 AND 132 ";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   * J - Select all patients from Ficha Resumo (encounter type 53) with “HIV Carga viral”(Concept id
   * 856, value_numeric not null) and obs_datetime between “Patient ART Start Date” (the oldest date
   * from A) + 198 days and “Patient ART Start Date” (the oldest date from query A) + 297 days.
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQC13P3NUM_J() {

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
            + "       AND o.obs_datetime BETWEEN Date_add(tabela.art_date, INTERVAL 198 day)  "
            + "                                  AND  "
            + "                                      Date_add(tabela.art_date, INTERVAL 297 day "
            + "                                      )  "
            + " GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   * K - Select all patients from Ficha Resumo (encounter type 53) with “HIV Carga viral”(Concept id
   * 856, value_numeric not null) and obs_datetimebetween “ALTERNATIVA A LINHA - 1a LINHA Date” (the
   * most recent date from B1) + 198 days and “ALTERNATIVA A LINHA - 1a LINHA Date” (the most recent
   * date from B1) +297 days.
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQC13P3NUM_K() {

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
            + "                        AND o.obs_datetime BETWEEN DATE_ADD(B1.regime_date, INTERVAL 198 DAY)  "
            + "                        AND DATE_ADD(B1.regime_date, INTERVAL 297 DAY) "
            + "                        GROUP BY p.patient_id  ";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   * L - Select all patients from Ficha Resumo (encounter type 53) with “HIV Carga viral”(Concept id
   * 856, value_numeric not null) and obs_datetime between “Segunda Linha Date” (the most recent
   * date from B2New) + 198 days and “Segunda Linha Date” (the most recent date from B2New) + 297
   * days.
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQC13P3NUM_L() {

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
            + "                        AND o.obs_datetime BETWEEN DATE_ADD(B2NEW.last_consultation, INTERVAL 198 DAY)   "
            + "                        AND DATE_ADD(B2NEW.last_consultation, INTERVAL 297 DAY)  "
            + "                        GROUP BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   * B2E - Exclude all patients from Ficha Clinica (encounter type 6, encounter_datetime) who have
   * “LINHA TERAPEUTICA”(Concept id 21151) with value coded DIFFERENT THAN “SEGUNDA LINHA”(Concept
   * id 21150) and obs_datetime > “REGIME ARV SEGUNDA LINHA” (from B2New) and <= “Last Clinical
   * Consultation” (last encounter_datetime from B1)
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQC13DEN_B2E() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("All patients with LINHA TERAPEUTICA DIFFERENT THAN SEGUNDA LINHA ");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("21187", hivMetadata.getRegArvSecondLine().getConceptId());
    map.put("21151", hivMetadata.getTherapeuticLineConcept().getConceptId());
    map.put("21148", hivMetadata.getSecondLineConcept().getConceptId());

    String query =
        "SELECT     p.patient_id "
            + "FROM       patient p "
            + "INNER JOIN encounter e "
            + "ON         e.patient_id = p.patient_id "
            + "INNER JOIN obs o "
            + "ON         o.encounter_id = e.encounter_id "
            + "INNER JOIN "
            + "           ( "
            + "                      SELECT     p.patient_id , "
            + "                                 o.obs_datetime           AS linha_terapeutica, "
            + "                                 last_clinical.last_visit AS last_consultation "
            + "                      FROM       patient p "
            + "                      INNER JOIN encounter e "
            + "                      ON         e.patient_id = p.patient_id "
            + "                      INNER JOIN obs o "
            + "                      ON         o.encounter_id = e.encounter_id "
            + "                      INNER JOIN "
            + "                                 ( "
            + "                                            SELECT     p.patient_id, "
            + "                                                       Max(e.encounter_datetime) last_visit "
            + "                                            FROM       patient p "
            + "                                            INNER JOIN encounter e "
            + "                                            ON         e.patient_id = p.patient_id "
            + "                                            WHERE      p.voided = 0 "
            + "                                            AND        e.voided = 0 "
            + "                                            AND        e.encounter_type = ${6} "
            + "                                            AND        e.location_id = :location "
            + "                                            AND        e.encounter_datetime BETWEEN :startDate AND :revisionEndDate "
            + "                                            GROUP BY   p.patient_id) AS last_clinical "
            + "                      ON         last_clinical.patient_id = p.patient_id "
            + "                      WHERE      e.voided = 0 "
            + "                      AND        p.voided = 0 "
            + "                      AND        o.voided = 0 "
            + "                      AND        e.encounter_type = ${53} "
            + "                      AND        e.location_id = :location "
            + "                      AND        o.concept_id = ${21187} "
            + "                      AND        o.value_coded IS NOT NULL "
            + "                      AND        o.obs_datetime >= :startDate "
            + "                      AND        o.obs_datetime <= :revisionEndDate "
            + "                      AND        timestampdiff(month, o.obs_datetime, last_clinical.last_visit) >= 6) second_line "
            + "ON         second_line.patient_id = p.patient_id "
            + "WHERE      e.voided = 0 "
            + "AND        p.voided = 0 "
            + "AND        o.voided = 0 "
            + "AND        e.encounter_type = ${6} "
            + "AND        e.location_id = :location "
            + "AND        o.concept_id = ${21151} "
            + "AND        o.value_coded <> ${21148} "
            + "AND        o.obs_datetime > second_line.linha_terapeutica "
            + "AND        o.obs_datetime <= second_line.last_consultation";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  // ************** ABANDONED ART SECTION *************

  /**
   * <b> RF7.2 Utentes Abandono ou reinício TARV durante os últimos 6 meses anteriores a última
   * consulta (para exclusão)</b><br>
   * <br>
   *
   * <p>O sistema irá identificar utentes que abandonaram o tratamento TARV durante o período da
   * seguinte forma:
   *
   * <p>incluindo os utentes com registo de “Mudança de Estado de Permanência” = “Abandono” ou
   * “Reinicio” na Ficha Clínica nos 6 meses anteriores a data da última consulta (“Data Consulta
   * Abandono/Reinicio” >= “Data Última Consulta” menos 6 meses e <= “Data última Consulta”).<br>
   * <br>
   *
   * <p>incluindo os utentes com registo de “Mudança de Estado de Permanência” = “Abandono” ou
   * “Reinicio” na Ficha Resumo nos 6 meses anteriores a data da última consulta (“Data de Mudança
   * de Estado Permanência Abandono/Reinicio” >= “Data Última Consulta” menos 6 meses e <= “Data
   * última Consulta”).<br>
   * <br>
   *
   * <p><b>Nota:</b> “Data Última Consulta” é a data da última consulta clínica ocorrida durante o
   * período de revisão.
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoAbandonedOrRestartedTarvOnLast6MonthsArt() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("All patients who abandoned TARV On Art Start Date");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    cd.setQuery(
        QualityImprovement2020Queries.getMQ13AbandonedOrRestartedTarvOnLast6MonthsArt(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getStateOfStayOfArtPatient().getConceptId(),
            hivMetadata.getAbandonedConcept().getConceptId(),
            hivMetadata.getRestartConcept().getConceptId(),
            hivMetadata.getStateOfStayOfPreArtPatient().getConceptId(),
            6));

    return cd;
  }

  /**
   * <b> RF7.3 Utentes Abandono ou Reinício TARV durante o período (para exclusão)</b><br>
   * <br>
   *
   * <p>O sistema irá identificar utentes que abandonaram ou reiniciaram o tratamento TARV durante
   * um determinado período (entre “Data Início Periodo” e “Data Fim Período” da seguinte forma:
   *
   * <p>incluindo os utentes com registo de “Mudança de Estado de Permanência” = “Abandono” ou
   * “Reincio” na Ficha Clínica durante o período (“Data Consulta Abandono/Reinicio” >= “Data Início
   * Período” e <= “Data Fim Período”).<br>
   * <br>
   *
   * <p>incluindo os utentes com registo de “Mudança de Estado de Permanência” = “Abandono” ou
   * “Reinicio” na Ficha Resumo durante o período (“Data de Mudança de Estado Permanência
   * Abandono/Reinicio” >= “Data Início Período” e <= “Data Fim Período”). <br>
   * <br>
   *
   * <p><b>Nota:</b> “Data Início Período” e “Data Fim Período” estão definidas no respectivo
   * indicador/RF.
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoAbandonedOrRestartedTarvOnLast3MonthsArt() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("All patients who abandoned TARV On Art Start Date");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    cd.setQuery(
        QualityImprovement2020Queries.getMQ13AbandonedOrRestartedTarvOnLast6MonthsArt(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getStateOfStayOfArtPatient().getConceptId(),
            hivMetadata.getAbandonedConcept().getConceptId(),
            hivMetadata.getRestartConcept().getConceptId(),
            hivMetadata.getStateOfStayOfPreArtPatient().getConceptId(),
            3));

    return cd;
  }

  /**
   * <b> RF7.3 Utentes Abandono ou Reinício TARV durante o período (para exclusão)</b><br>
   * <br>
   *
   * <p>O sistema irá identificar utentes que abandonaram ou reiniciaram o tratamento TARV durante
   * um determinado período ( entre “Data Início Periodo” e “Data Fim Período” da seguinte forma:
   *
   * <p>incluindo os utentes com registo de “Mudança de Estado de Permanência” = “Abandono” ou
   * “Reincio” na Ficha Clínica durante o período (“Data Consulta Abandono/Reinicio” >= “Data Início
   * Período e <= “Data Fim Período.<br>
   * <br>
   *
   * <p>incluindo os utentes com registo de “Mudança de Estado de Permanência” = “Abandono” ou
   * “Reinicio” na Ficha Resumo durante o período (“Data de Mudança de Estado Permanência
   * Abandono/Reinicio” >= “Data Início Período” e <= “Data Fim Período”). .<br>
   * <br>
   *
   * <p><b>Nota:</b> Data Início Período” e “Data Fim Período” estão definidas no respectivo
   * indicador/RF.
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoAbandonedOrRestartedTarvOnArtStartDate() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("All patients who abandoned TARV On Art Start Date");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    cd.setQuery(
        QualityImprovement2020Queries.getMQ13AbandonedOrRestartedTarvOnArtStartDate(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getStateOfStayOfArtPatient().getConceptId(),
            hivMetadata.getAbandonedConcept().getConceptId(),
            hivMetadata.getRestartConcept().getConceptId(),
            hivMetadata.getStateOfStayOfPreArtPatient().getConceptId()));

    return cd;
  }

  /**
   * <b> RF7.2 EXCLUSION FOR PREGNANT PATIENTS WHO ABANDONED DURING ART START DATE PERIOD</b>
   *
   * <p>O sistema irá identificar utentes que abandonaram o tratamento TARV durante o período da
   * seguinte forma:
   *
   * <p>incluindo os utentes com Último registo de “Mudança de Estado de Permanência” = “Abandono”
   * na Ficha Clínica durante o período (“Data Consulta”>=”Data Início Período” e “Data
   * Consulta”<=”Data Fim Período”
   *
   * <p>incluindo os utentes com Último registo de “Mudança de Estado de Permanência” = “Abandono”
   * na Ficha Resumo durante o período (“Data de Mudança de Estado Permanência”>=”Data Início
   * Período” e “Data Consulta”<=”Data Fim Período”
   * <li>5. para exclusão nas mulheres grávidas que iniciaram TARV a “Data Início Período” será
   *     igual a “Data Início TARV” e “Data Fim do Período” será igual a “Data Início TARV”+3meses.
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoAbandonedTarvOnArtStartDateForPregnants() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("All patients who abandoned TARV on Art Start Date For Pregnants");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    cd.setQuery(
        QualityImprovement2020Queries.getMQ13AbandonedTarvOnArtStartDateForPregnants(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getStateOfStayOfArtPatient().getConceptId(),
            hivMetadata.getAbandonedConcept().getConceptId(),
            hivMetadata.getRestartConcept().getConceptId(),
            hivMetadata.getStateOfStayOfPreArtPatient().getConceptId()));

    return cd;
  }

  /**
   * <b> RF7.3 EXCLUSION FOR PREGNANT PATIENTS WHO ABANDONED DURING ART START DATE PERIOD</b>
   *
   * <p>excluindo os utentes abandono ou reinício TARV durante o período (seguindo os critérios
   * definidos no RF7.3) nos últimos 3 meses anteriores a última consulta do período de revisão
   * (entre “Data última Consulta menos 3 meses” [=Data Início Periodo] e “Data última Consulta”
   * [=Data Fim Período])..<br>
   * <br>
   *
   * <p>Nota 1: “Data 1ª Consulta Grávida” deve ser a primeira consulta de sempre com registo de
   * grávida e essa consulta deve ter ocorrido no período de inclusão. <br>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoAbandonedTarvBetween3MonthsBeforePregnancyDate() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("All patients who abandoned TARV on Art Start Date For Pregnants");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("1707", hivMetadata.getAbandonedConcept().getConceptId());
    map.put("1705", hivMetadata.getRestartConcept().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String a =
        "SELECT abandoned.patient_id from (  "
            + "                                                               SELECT p.patient_id, max(e.encounter_datetime) as last_encounter FROM patient p  "
            + "                                                                                                                                         INNER JOIN encounter e ON e.patient_id = p.patient_id  "
            + "                                                                                                                                         INNER JOIN obs o on e.encounter_id = o.encounter_id  "
            + "                                            INNER JOIN ( "
            + "                                                   SELECT p.patient_id, MAX(e.encounter_datetime) as last_consultation "
            + "                                                   FROM   patient p  "
            + "                                                           INNER JOIN encounter e ON p.patient_id = e.patient_id  "
            + "                                                           INNER JOIN obs o ON e.encounter_id = o.encounter_id  "
            + "                                                   WHERE  p.voided = 0  "
            + "                                                    AND e.voided = 0  "
            + "                                                    AND o.voided = 0  "
            + "                                                    AND e.location_id = :location "
            + "                                                    AND e.encounter_type = ${6} "
            + "                                                    AND e.encounter_datetime <= :endDate "
            + "                                                  GROUP BY p.patient_id "
            + "                                          ) most_recent  ON p.patient_id = most_recent.patient_id   "
            + "                                                               WHERE p.voided = 0 AND e.voided = 0 AND o.voided = 0  "
            + "                                                                 AND e.encounter_type = ${6}  "
            + "                                                                 AND o.concept_id = ${6273}  "
            + "                                                                 AND o.value_coded IN ( ${1707}, ${1705} ) "
            + "                                                                 AND e.location_id = :location  "
            + "                                                                 AND e.encounter_datetime >= DATE_SUB(most_recent.last_consultation, INTERVAL 3 MONTH)  "
            + "                                                                 AND e.encounter_datetime <= most_recent.last_consultation  "
            + "                                                               GROUP BY p.patient_id  "
            + "                                                               UNION  "
            + "                                                               SELECT p.patient_id, max(o.obs_datetime) as last_encounter FROM patient p  "
            + "                                                                                                                                   INNER JOIN encounter e ON e.patient_id = p.patient_id  "
            + "                                                                                                                                   INNER JOIN obs o on e.encounter_id = o.encounter_id  "
            + "                                            INNER JOIN ( "
            + "                                                   SELECT p.patient_id, MAX(e.encounter_datetime) as last_consultation "
            + "                                                   FROM   patient p  "
            + "                                                           INNER JOIN encounter e ON p.patient_id = e.patient_id  "
            + "                                                           INNER JOIN obs o ON e.encounter_id = o.encounter_id  "
            + "                                                   WHERE  p.voided = 0  "
            + "                                                    AND e.voided = 0  "
            + "                                                    AND o.voided = 0  "
            + "                                                    AND e.location_id = :location "
            + "                                                    AND e.encounter_type = ${6} "
            + "                                                    AND e.encounter_datetime <= :endDate "
            + "                                                  GROUP BY p.patient_id "
            + "                                          ) most_recent  ON p.patient_id = most_recent.patient_id   "
            + "                                                               WHERE p.voided = 0 AND e.voided = 0 AND o.voided = 0  "
            + "                                                                 AND e.encounter_type = ${53}  "
            + "                                                                 AND o.concept_id = ${6272}  "
            + "                                                                 AND o.value_coded IN ( ${1707}, ${1705} )  "
            + "                                                                 AND e.location_id = :location  "
            + "                                                                 AND o.obs_datetime >= DATE_SUB(most_recent.last_consultation, INTERVAL 3 MONTH)  "
            + "                                                                 AND o.obs_datetime <= most_recent.last_consultation  "
            + "                                                               GROUP BY p.patient_id  "
            + "                                                           ) abandoned GROUP BY abandoned.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    cd.setQuery(sb.replace(a));

    return cd;
  }

  /**
   * <b> RF7.2 EXCLUSION FOR PATIENTS WHO ABANDONED DURING ART RESTART DATE PERIOD</b>
   *
   * <p>O sistema irá identificar utentes que abandonaram o tratamento TARV durante o período da
   * seguinte forma:
   *
   * <p>incluindo os utentes com Último registo de “Mudança de Estado de Permanência” = “Abandono”
   * na Ficha Clínica durante o período (“Data Consulta”>=”Data Início Período” e “Data
   * Consulta”<=”Data Fim Período”
   *
   * <p>incluindo os utentes com Último registo de “Mudança de Estado de Permanência” = “Abandono”
   * na Ficha Resumo durante o período (“Data de Mudança de Estado Permanência”>=”Data Início
   * Período” e “Data Consulta”<=”Data Fim Período”
   * <li>2.para exclusão nos utentes que reiniciaram TARV, a “Data Início Período” será igual a
   *     “Data Consulta Reinício TARV” e “Data Fim do Período” será igual a “Data Consulta Reínicio
   *     TARV”+6meses.
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoAbandonedTarvOnArtRestartDate() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("All patients who abandoned TARV on Art Restart Date");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    cd.setQuery(
        QualityImprovement2020Queries.getMQ13AbandonedTarvOnArtRestartDate(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getStateOfStayOfArtPatient().getConceptId(),
            hivMetadata.getAbandonedConcept().getConceptId(),
            hivMetadata.getStateOfStayOfPreArtPatient().getConceptId(),
            hivMetadata.getRestartConcept().getConceptId()));

    return cd;
  }

  /**
   * <b> RF7.2 EXCLUSION FOR PATIENTS WHO ABANDONED DURING FIRST LINE REGIMEN DATE PERIOD</b>
   *
   * <p>O sistema irá identificar utentes que abandonaram o tratamento TARV durante o período da
   * seguinte forma:
   *
   * <p>incluindo os utentes com Último registo de “Mudança de Estado de Permanência” = “Abandono”
   * ou “Reincio” na Ficha Clínica durante o período (“Data Consulta”>=”Data Início Período” e “Data
   * Consulta”<=”Data Fim Período”
   *
   * <p>incluindo os utentes com Último registo de “Mudança de Estado de Permanência” = “Abandono”
   * ou “Reincio” na Ficha Resumo durante o período (“Data de Mudança de Estado Permanência”>=”Data
   * Início Período” e “Data Consulta”<=”Data Fim Período”
   * <li>3. para exclusão nos utentes que iniciaram novo regime de 1ª Linha, a “Data Início Período”
   *     será igual a “Data última Alternativa 1ª Linha” e a “Data Fim do Período” será “Data última
   *     Alternativa 1ª Linha” + 6meses.
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoAbandonedTarvOnOnFirstLineDate() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("All patients who abandoned TARV on On First Line Date");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    cd.setQuery(
        QualityImprovement2020Queries.getMQ13AbandonedTarvOnFirstLineDate(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getStateOfStayOfArtPatient().getConceptId(),
            hivMetadata.getAbandonedConcept().getConceptId(),
            hivMetadata.getRestartConcept().getConceptId(),
            hivMetadata.getStateOfStayOfPreArtPatient().getConceptId(),
            hivMetadata.getTherapeuticLineConcept().getConceptId(),
            hivMetadata.getFirstLineConcept().getConceptId(),
            hivMetadata.getARVStartDateConcept().getConceptId(),
            hivMetadata.getJustificativeToChangeArvTreatment().getConceptId(),
            commonMetadata.getRegimenAlternativeToFirstLineConcept().getConceptId(),
            commonMetadata.getPregnantConcept().getConceptId()));

    return cd;
  }

  /**
   * <b> RF7.2 EXCLUSION FOR PATIENTS WHO ABANDONED IN THE LAST SIX MONTHS FROM FIRST LINE DATE</b>
   *
   * <p>O sistema irá identificar utentes que abandonaram o tratamento TARV durante o período da
   * seguinte forma:
   *
   * <p>incluindo os utentes com Último registo de “Mudança de Estado de Permanência” = “Abandono”
   * ou “Reinicio” na Ficha Clínica durante o período (“Data Consulta”>=”Data Início Período” e
   * “Data Consulta”<=”Data Fim Período”
   *
   * <p>incluindo os utentes com Último registo de “Mudança de Estado de Permanência” = “Abandono”
   * ou “Reinicio” na Ficha Resumo durante o período (“Data de Mudança de Estado Permanência”>=”Data
   * Início Período” e “Data Consulta”<=”Data Fim Período”
   * <li>6. para exclusão nos utentes que estão na 1ª linha de TARV, a “Data Início Período” será
   *     igual a “Data 1a Linha” – 6 meses e “Data Fim do Período” será igual a “Data 1a Linha”.
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoAbandonedInTheLastSixMonthsFromFirstLineDate() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("All patients who abandoned TARV In The Last Six Months From First Line Date");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    cd.setQuery(
        QualityImprovement2020Queries.getMQ13AbandonedTarvInTheLastSixMonthsFromFirstLineDate(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getStateOfStayOfArtPatient().getConceptId(),
            hivMetadata.getAbandonedConcept().getConceptId(),
            hivMetadata.getRestartConcept().getConceptId(),
            hivMetadata.getStateOfStayOfPreArtPatient().getConceptId(),
            hivMetadata.getTherapeuticLineConcept().getConceptId(),
            hivMetadata.getFirstLineConcept().getConceptId(),
            hivMetadata.getARVStartDateConcept().getConceptId(),
            hivMetadata.getJustificativeToChangeArvTreatment().getConceptId(),
            commonMetadata.getRegimenAlternativeToFirstLineConcept().getConceptId(),
            commonMetadata.getPregnantConcept().getConceptId()));

    return cd;
  }

  /**
   * <b> RF7.2 EXCLUSION FOR PATIENTS WHO ABANDONED DURING SECOND LINE REGIMEN DATE PERIOD</b>
   *
   * <p>O sistema irá identificar utentes que abandonaram o tratamento TARV durante o período da
   * seguinte forma:
   *
   * <p>incluindo os utentes com Último registo de “Mudança de Estado de Permanência” = “Abandono”
   * na Ficha Clínica durante o período (“Data Consulta”>=”Data Início Período” e “Data
   * Consulta”<=”Data Fim Período”
   *
   * <p>incluindo os utentes com Último registo de “Mudança de Estado de Permanência” = “Abandono”
   * na Ficha Resumo durante o período (“Data de Mudança de Estado Permanência”>=”Data Início
   * Período” e “Data Consulta”<=”Data Fim Período”
   * <li>4. para exclusão nos utentes que iniciaram 2ª linha de TARV, a “Data Início Período” será
   *     igual a “Data 2ª Linha” a “Data Fim do Período” será “Data 2ª Linha”+ 6 meses.
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoAbandonedTarvOnOnSecondLineDate() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("All patients who abandoned TARV on On Second Line Date");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    cd.setQuery(
        QualityImprovement2020Queries.getMQ13AbandonedTarvOnSecondLineDate(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getStateOfStayOfArtPatient().getConceptId(),
            hivMetadata.getAbandonedConcept().getConceptId(),
            hivMetadata.getRestartConcept().getConceptId(),
            hivMetadata.getStateOfStayOfPreArtPatient().getConceptId(),
            hivMetadata.getRegArvSecondLine().getConceptId()));

    return cd;
  }

  // **************/ABANDONED ART SECTION *************

  /**
   * <b>RF14</b>: Select all patients who restarted ART for at least 6 months following: all
   * patients who have “Mudança de Estado de Permanência TARV”=”Reinício” na Ficha Clínica durante o
   * período de inclusão (“Data Consulta Reinício TARV” >= “Data Início Inclusão” e <= “Data Fim
   * Inclusão”), where “Data Última Consulta” durante o período de revisão, menos (-) “Data Consulta
   * Reinício TARV” maior ou igual (>=) a 6 meses
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoRestartedTarvAtLeastSixMonths() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("All patients who restarted TARV for at least 6 months");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("1705", hivMetadata.getRestartConcept().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());

    EptsQueriesUtil patientBuilder = new EptsQueriesUtil();

    String query =
        patientBuilder
            .patientIdQueryBuilder(QualityImprovement2020Queries.getRestartedArtQuery())
            .getQuery();

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   * Filtrando os utentes que têm o registo de dois pedidos de CV na Ficha Clínica (“Pedido de
   * Investigações Laboratoriais” igual a “Carga Viral”) entre “Data Inscrição MDS (DT)” e “Data Fim
   * Revisão” e o registo de um resultado de CV <1000 cps/ml entre os dois pedidos (“Data Consulta
   * Resultado CV”> “Data Consulta 1º Pedido de CV” e < “Data Consulta 2º Pedido de CV”)
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWithVLResultLessThan1000Between2VlRequestAfterTheseMDS(
      List<Integer> dispensationTypes) {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Utentes que têm o registo de dois pedidos de CV na Ficha Clinica ");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, String> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId().toString());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId().toString());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId().toString());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId().toString());
    map.put("165322", hivMetadata.getMdcState().getConceptId().toString());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId().toString());
    map.put("23739", hivMetadata.getTypeOfDispensationConcept().getConceptId().toString());
    map.put("23888", hivMetadata.getSemiannualDispensation().getConceptId().toString());
    map.put("23720", hivMetadata.getQuarterlyConcept().getConceptId().toString());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId().toString());
    map.put("dispensationTypes", getMetadataFrom(dispensationTypes));
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId().toString());
    map.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId().toString());
    map.put("lower", "83");
    map.put("upper", "97");

    String query =
        "SELECT two_dispensations.patient_id "
            + "FROM   (SELECT patient_id, MIN(encounter_datetime) first_date, MAX(encounter_datetime) second_date "
            + "        FROM   (SELECT p.patient_id, e.encounter_datetime "
            + "                FROM   patient p "
            + "                       INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                       INNER JOIN obs o  ON o.encounter_id = e.encounter_id "
            + "                WHERE  e.encounter_type = ${6} "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                       AND o.concept_id = ${23722} "
            + "                       AND o.value_coded = ${856} "
            + "                       AND e.encounter_datetime <= :revisionEndDate "
            + "                       AND e.encounter_datetime >= (SELECT dt_date "
            + "                                                    FROM   (SELECT most_recent.patient_id, MAX(most_recent.encounter_datetime) dt_date "
            + "                                                            FROM   (SELECT p2.patient_id, e2.encounter_datetime "
            + "                                                                    FROM   patient p2 "
            + "                                                                    INNER JOIN encounter e2 ON e2.patient_id = p2.patient_id "
            + "                                                                    INNER JOIN obs otype ON otype.encounter_id = e2.encounter_id "
            + "                                                                    INNER JOIN obs ostate ON ostate.encounter_id = e2.encounter_id "
            + "                                                            WHERE  e2.encounter_type = ${6} "
            + "                                                             AND e2.location_id = :location "
            + "                                                             AND otype.concept_id = ${165174} "
            + "                                                             AND otype.value_coded IN( ${dispensationTypes} ) "
            + "                                                             AND ostate.concept_id = ${165322} "
            + "                                                             AND ostate.value_coded = ${1256} "
            + "                                                             AND e2.encounter_datetime >= :startDate "
            + "                                                             AND e2.encounter_datetime <= :endDate "
            + "                                                             AND otype.obs_group_id = ostate.obs_group_id "
            + "                                                             AND e2.voided = 0 "
            + "                                                             AND p2.voided = 0 "
            + "                                                             AND otype.voided = 0 "
            + "                                                             AND ostate.voided = 0 "
            + "                                                             UNION "
            + "                                                             SELECT p.patient_id, e.encounter_datetime "
            + "                                                             FROM   patient p INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                                                             INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                                                             WHERE  p.voided = 0 "
            + "                                                                    AND o.voided = 0 "
            + "                                                                    AND e.voided = 0 "
            + "                                                                    AND e.location_id = :location "
            + "                                                                    AND e.encounter_type = ${6} "
            + "                                                                    AND o.concept_id = ${23739} "
            + "                                                                    AND o.value_coded IN ( ${23720}, ${23888}) "
            + "                                                                    AND e.encounter_datetime "
            + "                                                                    AND e.encounter_datetime >= :startDate "
            + "                                                                    AND e.encounter_datetime <= :endDate "
            + "                                                             UNION "
            + "                                                             SELECT p.patient_id, e.encounter_datetime "
            + "                                                             FROM   patient p "
            + "                                                             INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                                                             INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                                                             INNER JOIN (SELECT p.patient_id, MAX(e.encounter_datetime) consultation_date "
            + "                                                                         FROM   patient p "
            + "                                                                         INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                                                                         WHERE  e.encounter_type = ${18} "
            + "                                                                         AND e.location_id = :location "
            + "                                                                         AND e.encounter_datetime BETWEEN  :startDate AND :endDate "
            + "                                                                         AND e.voided = 0 "
            + "                                                                         AND p.voided = 0 "
            + "                                                                         GROUP  BY p.patient_id) recent_clinical "
            + "                                                             ON recent_clinical.patient_id = p.patient_id "
            + "                                                             WHERE  e.encounter_datetime = recent_clinical.consultation_date "
            + "                                                             AND e.encounter_type = ${18} "
            + "                                                             AND e.location_id = :location "
            + "                                                             AND o.concept_id = ${5096} "
            + "                                                             AND ( (DATEDIFF(o.value_datetime, e.encounter_datetime) >= ${lower} "
            + "                                                             AND DATEDIFF(o.value_datetime, e.encounter_datetime) <= ${upper}) OR (DATEDIFF(o.value_datetime, e.encounter_datetime) >= 173 AND DATEDIFF(o.value_datetime, e.encounter_datetime) <= 187) )"
            + "                                                             AND p.voided = 0 "
            + "                                                             AND e.voided = 0 "
            + "                                                             AND o.voided = 0 "
            + "                                                             GROUP  BY p.patient_id) most_recent "
            + "                                                 GROUP  BY most_recent.patient_id) dispensation "
            + "                                                 WHERE  dispensation.patient_id = p.patient_id)) investigations "
            + "               GROUP  BY investigations.patient_id "
            + "               HAVING COUNT(investigations.encounter_datetime) >= 2) two_dispensations "
            + " INNER JOIN (SELECT p.patient_id, e.encounter_datetime AS vl_date "
            + "             FROM   patient p "
            + "             INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "             INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "             WHERE  e.encounter_type = ${6} "
            + "             AND e.location_id = :location "
            + "             AND ( ( o.concept_id = ${856} AND o.value_numeric < 1000 ) OR ( o.concept_id = ${1305} AND o.value_coded IS NOT NULL ) ) "
            + "             AND p.voided = 0 "
            + "             AND e.voided = 0 "
            + "             AND o.voided = 0) vl_result ON two_dispensations.patient_id = vl_result.patient_id "
            + " WHERE  vl_result.vl_date > two_dispensations.first_date AND vl_result.vl_date < two_dispensations.second_date"
            + " GROUP BY two_dispensations.patient_id      ";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   * Utentes que têm o registo de início do MDS para utente estável na última consulta decorrida há
   * 12 meses (última “Data Consulta Clínica” >= “Data Fim Revisão” – 12 meses+1dia e <= “Data Fim
   * Revisão”), ou seja, registo de um MDC (MDC1 ou MDC2 ou MDC3 ou MDC4 ou MDC5) como:
   *
   * <p>“GA” e o respectivo “Estado” = “Início” “DT” e o respectivo “Estado” = “Início” “DS” e o
   * respectivo “Estado” = “Início” “APE” e o respectivo “Estado” = “Início” “FR” e o respectivo
   * “Estado” = “Início” “DD” e o respectivo “Estado” = “Início”
   *
   * @return CohortDefinition
   */
  public CohortDefinition
      getPatientsWithMdcOnMostRecentClinicalFormWithFollowingDispensationTypesAndState(
          List<Integer> dispensationTypes, List<Integer> states) {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Utentes que têm o registo de dois pedidos de CV na Ficha Clinica ");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, String> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId().toString());
    map.put("165322", hivMetadata.getMdcState().getConceptId().toString());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId().toString());
    map.put("dispensationTypes", getMetadataFrom(dispensationTypes));
    map.put("states", getMetadataFrom(states));

    String query =
        "SELECT     p.patient_id "
            + "FROM       patient p "
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "INNER JOIN obs otype ON otype.encounter_id = e.encounter_id "
            + "INNER JOIN obs ostate ON ostate.encounter_id = e.encounter_id "
            + "INNER JOIN ( "
            + "                      SELECT     p.patient_id, MAX(e.encounter_datetime) consultation_date "
            + "                      FROM       patient p "
            + "                      INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                      WHERE      e.encounter_type = ${6} "
            + "                      AND        e.location_id = :location "
            + "                      AND        e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "                      AND        p.voided = 0 "
            + "                      AND        e.voided = 0 "
            + "                      GROUP BY   p.patient_id ) consultation ON consultation.patient_id = p.patient_id "
            + "WHERE      e.encounter_type = ${6} "
            + "AND        e.location_id = :location "
            + "AND        otype.concept_id = ${165174} "
            + "AND        otype.value_coded IN (${dispensationTypes}) "
            + "AND        ostate.concept_id = ${165322} "
            + "AND        ostate.value_coded IN (${states}) "
            + "AND        e.encounter_datetime = consultation.consultation_date "
            + "AND        otype.obs_group_id = ostate.obs_group_id "
            + "AND        e.voided = 0 "
            + "AND        p.voided = 0 "
            + "AND        otype.voided = 0 "
            + "AND        ostate.voided = 0 "
            + "GROUP BY   p.patient_id ";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   * Utentes com marcação de próxima consulta a seguir a última consulta clínica no período de
   * avaliação/revisão, na qual foi registado o resultado de CV >= 1000, sendo esta marcação de
   * consulta entre 23 a 37 dias da consultao, ou seja, “Próxima Consulta” (marcada na Ficha Clínica
   * com “Próxima Consulta” >= “Data última Consulta” e <= “Data Fim Revisão”) >= data última
   * consulta + 23 dias e <= data última consulta + 37 dias)
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoHadPickupOnFilaAfterMostRecentVlOnFichaClinica() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Utentes que têm o registo de dois pedidos de CV na Ficha Clinica ");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision endDate", Date.class));

    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1410", hivMetadata.getReturnVisitDateConcept().getConceptId());

    String query =
        "SELECT     p.patient_id "
            + "FROM       patient p "
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "INNER JOIN ( "
            + "                      SELECT     p.patient_id, MAX(e.encounter_datetime) vl_date "
            + "                      FROM       patient p "
            + "                      INNER JOIN encounter e ON         e.patient_id = p.patient_id "
            + "                      INNER JOIN obs o  ON  o.encounter_id = e.encounter_id "
            + "                      WHERE      e.encounter_type = ${6} "
            + "                      AND        e.location_id = :location "
            + "                      AND        o.concept_id = ${856} "
            + "                      AND        o.value_numeric >= 1000 "
            + "                      AND        e.encounter_datetime BETWEEN :startDate AND :revisionEndDate "
            + "                      AND        p.voided = 0 "
            + "                      AND        e.voided = 0 "
            + "                      AND        o.voided = 0 "
            + "                      GROUP BY   p.patient_id ) vl ON vl.patient_id = p.patient_id "
            + "WHERE      e.encounter_type = ${6} "
            + "AND        e.location_id = :location "
            + "AND        o.concept_id = ${1410} "
            + "AND        e.encounter_datetime BETWEEN vl.vl_date AND :revisionEndDate "
            + "AND        o.value_datetime BETWEEN DATE_ADD(e.encounter_datetime, INTERVAL 23 DAY) AND  DATE_ADD(e.encounter_datetime, INTERVAL 37 DAY) "
            + "AND        e.voided = 0 "
            + "AND        p.voided = 0 "
            + "AND        o.voided = 0 "
            + "GROUP BY   p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   * Filtrando os utentes que têm o último registo de “Tipo de Dispensa” = @param dispensationType
   * antes da última consulta do período de revisão ( “Data Última Consulta”)
   *
   * @param dispensationType
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWithDispensationBeforeLastConsultationDate(
      Concept dispensationType) {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Utentes com registo de tipo de dispensa antes da última ficha clinica");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("23739", hivMetadata.getTypeOfDispensationConcept().getConceptId());
    map.put("dispensation", dispensationType.getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "       INNER JOIN (SELECT p.patient_id, MAX(e.encounter_datetime) encounter_datetime "
            + "                   FROM   patient p "
            + "                          INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                          INNER JOIN (SELECT p.patient_id, MAX(e.encounter_datetime) AS encounter_datetime "
            + "                                      FROM   patient p "
            + "                                             INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                                      WHERE  p.voided = 0 "
            + "                                             AND e.voided = 0 "
            + "                                             AND e.location_id = :location "
            + "                                             AND e.encounter_type = ${6} "
            + "                                             AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "                                      GROUP  BY p.patient_id) last_consultation "
            + "                                  ON last_consultation.patient_id = p.patient_id "
            + "                   WHERE  p.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.voided = 0 "
            + "                          AND e.location_id = :location "
            + "                          AND e.encounter_type = ${6} "
            + "                          AND o.concept_id = ${23739} "
            + "                          AND e.encounter_datetime < last_consultation.encounter_datetime "
            + "                   GROUP  BY p.patient_id) recent_dispensation_type ON recent_dispensation_type.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_type = ${6} "
            + "       AND o.concept_id = ${23739} "
            + "       AND o.value_coded = ${dispensation} "
            + "       AND e.encounter_datetime = recent_dispensation_type.encounter_datetime "
            + "GROUP  BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * Todos os utentes que têm o último registo de pelo menos um dos seguintes modelos na “Ficha
   * Clínica” (coluna 24) registada antes da “Data Última Consulta”: Último registo de MDC (MDC1 ou
   * MDC2 ou MDC3 ou MDC4 ou MDC5) como “GAAC” e o respectivo “Estado” = “Iníicio” ou “Continua”, ou
   * Último registo de MDC (MDC1 ou MDC2 ou MDC3 ou MDC4 ou MDC5) como “DT” e o respectivo “Estado”
   * = “Início” ou “Continua”, ou Último registo de MDC (MDC1 ou MDC2 ou MDC3 ou MDC4 ou MDC5) como
   * “DS” e o respectivo “Estado” = “Início” ou “Continua”, ou Último registo de MDC (MDC1 ou MDC2
   * ou MDC3 ou MDC4 ou MDC5) como “APE” e o respectivo “Estado” = “Início” ou “Continua”, ou Último
   * registo de MDC (MDC1 ou MDC2 ou MDC3 ou MDC4 ou MDC5) como “FR” e o respectivo “Estado” =
   * “Início” ou “Continua”, ou Último registo de MDC (MDC1 ou MDC2 ou MDC3 ou MDC4 ou MDC5) como
   * “DD” e o respectivo “Estado” = “Início” ou “Continua”
   *
   * @return CohortDefinition
   */
  public CohortDefinition
      getPatientsWithMdcBeforeMostRecentClinicalFormWithFollowingDispensationTypesAndState(
          List<Integer> dispensationTypes, List<Integer> states) {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Utentes que têm o registo de dois pedidos de CV na Ficha Clinica ");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, String> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId().toString());
    map.put("165322", hivMetadata.getMdcState().getConceptId().toString());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId().toString());
    map.put("dispensationTypes", getMetadataFrom(dispensationTypes));
    map.put("states", getMetadataFrom(states));

    String query =
        "SELECT     p.patient_id "
            + "FROM       patient p "
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "INNER JOIN obs otype ON otype.encounter_id = e.encounter_id "
            + "INNER JOIN obs ostate ON ostate.encounter_id = e.encounter_id "
            + "INNER JOIN ( "
            + "                   SELECT     p.patient_id, "
            + "                              Max(e.encounter_datetime) second_consultation_date "
            + "                   FROM       patient p "
            + "                   INNER JOIN encounter e "
            + "                   ON         e.patient_id = p.patient_id "
            + "                   INNER JOIN "
            + "                              ( "
            + "                                         SELECT     p.patient_id, "
            + "                                                    Max(e.encounter_datetime) consultation_date "
            + "                                         FROM       patient p "
            + "                                         INNER JOIN encounter e "
            + "                                         ON         e.patient_id = p.patient_id "
            + "                                         WHERE      e.encounter_type = ${6} "
            + "                                         AND        e.location_id = :location "
            + "                                         AND        e.encounter_datetime BETWEEN :startDate AND        :endDate "
            + "                                         AND        p.voided = 0 "
            + "                                         AND        e.voided = 0 "
            + "                                         GROUP BY   p.patient_id ) last_clinical "
            + "                   ON         last_clinical.patient_id = p.patient_id "
            + "                   WHERE      e.encounter_type = ${6} "
            + "                   AND        e.location_id = :location "
            + "                   AND        e.encounter_datetime < last_clinical.consultation_date "
            + "                   AND        p.voided = 0 "
            + "                   AND        e.voided = 0 "
            + "                   GROUP BY   p.patient_id"
            + "                      ) second_consultation ON second_consultation.patient_id = p.patient_id "
            + "WHERE      e.encounter_type = ${6} "
            + "AND        e.location_id = :location "
            + "AND        otype.concept_id = ${165174} "
            + "AND        otype.value_coded IN (${dispensationTypes}) "
            + "AND        ostate.concept_id = ${165322} "
            + "AND        ostate.value_coded IN (${states}) "
            + "AND        e.encounter_datetime = second_consultation.second_consultation_date "
            + "AND        otype.obs_group_id = ostate.obs_group_id "
            + "AND        e.voided = 0 "
            + "AND        p.voided = 0 "
            + "AND        otype.voided = 0 "
            + "AND        ostate.voided = 0 "
            + "GROUP BY   p.patient_id ";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   * Os utentes com registo de último levantamento na farmácia (FILA) antes da última consulta do
   * período de revisão (“Data última Consulta) com próximo levantamento agendado para 83 a 97 dias
   * ( “Data Próximo Levantamento” menos “Data Levantamento”>= 83 dias e <= 97 dias, sendo “Data
   * Levantamento” último levantamento registado no FILA < “Data Última Consulta”)
   */
  public CohortDefinition getPatientsWhoHadFilaBeforeLastClinicalConsutationBetween(
      int lowerBounded, int upperBounded) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "Patients who have pickup registered on FILA Before Last Clinical Consultation)");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    HivMetadata hivMetadata = new HivMetadata();
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());
    map.put("lower", lowerBounded);
    map.put("upper", upperBounded);

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "       INNER JOIN (SELECT p.patient_id, Max(e.encounter_datetime) consultation_date "
            + "                   FROM   patient p "
            + "                          INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                          INNER JOIN (SELECT p.patient_id, Max(e.encounter_datetime) consultation_date "
            + "                                      FROM   patient p "
            + "                                             INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                                      WHERE  e.encounter_type = ${6} "
            + "                                             AND e.location_id = :location "
            + "                                             AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "                                             AND e.voided = 0 "
            + "                                             AND p.voided = 0 "
            + "                                      GROUP  BY p.patient_id) recent_clinical ON recent_clinical.patient_id = p.patient_id "
            + "                   WHERE  e.encounter_datetime < recent_clinical.consultation_date "
            + "                          AND e.encounter_type = ${18} "
            + "                          AND e.location_id = :location "
            + "                          AND p.voided = 0 "
            + "                          AND e.voided = 0 "
            + "                   GROUP  BY p.patient_id) recent_fila "
            + "               ON recent_fila.patient_id = p.patient_id "
            + "WHERE  e.encounter_type = ${18} "
            + "       AND e.encounter_datetime = recent_fila.consultation_date "
            + "       AND o.concept_id = ${5096} "
            + "       AND e.location_id = :location "
            + "       AND p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND DATEDIFF(o.value_datetime, e.encounter_datetime) >= ${lower} "
            + "       AND DATEDIFF(o.value_datetime, e.encounter_datetime) <= ${upper} "
            + " GROUP  BY p.patient_id  ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * utentes que têm o registo de “Resultado de Carga Viral” < 1000 cópias, na “Ficha de
   * Laboratório” registada entre “Data Consulta 2º Pedido de CV” e “Data Fim Revisão” .e o
   * resultado é < 1000 cps/ml.
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoHadVLResultLessThen1000nLaboratoryFormAfterSecondVLRequest(
      List<Integer> dispensationTypes) {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Utentes que têm o registo de dois pedidos de CV na Ficha Clinica ");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, String> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId().toString());
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId().toString());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId().toString());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId().toString());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId().toString());
    map.put("165322", hivMetadata.getMdcState().getConceptId().toString());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId().toString());
    map.put("23739", hivMetadata.getTypeOfDispensationConcept().getConceptId().toString());
    map.put("23888", hivMetadata.getSemiannualDispensation().getConceptId().toString());
    map.put("23720", hivMetadata.getQuarterlyConcept().getConceptId().toString());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId().toString());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId().toString());
    map.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId().toString());
    map.put("lower", "83");
    map.put("upper", "97");
    map.put("dispensationTypes", getMetadataFrom(dispensationTypes));

    String query =
        "SELECT two_dispensations.patient_id "
            + "FROM  ( "
            + "SELECT p.patient_id, MIN(e.encounter_datetime) second_date "
            + "FROM patient p "
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "INNER JOIN encounter e2 ON e2.patient_id = p.patient_id "
            + "INNER JOIN obs o2 ON o2.encounter_id = e2.encounter_id "
            + "INNER JOIN "
            + " (SELECT patient_id, MIN(encounter_datetime) first_date "
            + "        FROM   (SELECT p.patient_id, e.encounter_datetime "
            + "                FROM   patient p "
            + "                       INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                       INNER JOIN obs o  ON o.encounter_id = e.encounter_id "
            + "                WHERE  e.encounter_type = ${6} "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                       AND o.concept_id = ${23722} "
            + "                       AND o.value_coded = ${856} "
            + "                       AND e.encounter_datetime <= :revisionEndDate "
            + "                       AND e.encounter_datetime >= (SELECT dt_date "
            + "                                                    FROM   (SELECT most_recent.patient_id, MAX(most_recent.encounter_datetime) dt_date "
            + "                                                            FROM   (SELECT p2.patient_id, e2.encounter_datetime "
            + "                                                                    FROM   patient p2 "
            + "                                                                    INNER JOIN encounter e2 ON e2.patient_id = p2.patient_id "
            + "                                                                    INNER JOIN obs otype ON otype.encounter_id = e2.encounter_id "
            + "                                                                    INNER JOIN obs ostate ON ostate.encounter_id = e2.encounter_id "
            + "                                                                    WHERE  e2.encounter_type = ${6} "
            + "                                                                    AND e2.location_id = :location "
            + "                                                                    AND otype.concept_id =  ${165174} "
            + "                                                                    AND otype.value_coded IN( ${dispensationTypes} ) "
            + "                                                                    AND ostate.concept_id = ${165322} "
            + "                                                                    AND ostate.value_coded =  ${1256} "
            + "                                                                    AND e2.encounter_datetime >= :startDate "
            + "                                                                    AND e2.encounter_datetime <= :endDate "
            + "                                                                    AND otype.obs_group_id = ostate.obs_group_id "
            + "                                                                    AND e2.voided = 0 "
            + "                                                                    AND p2.voided = 0 "
            + "                                                                    AND otype.voided = 0 "
            + "                                                                    AND ostate.voided = 0 "
            + "                                                                    UNION "
            + "                                                                    SELECT p.patient_id, e.encounter_datetime "
            + "                                                                    FROM   patient p "
            + "                                                                    INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                                                                    INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                                                                    WHERE  p.voided = 0 "
            + "                                                                           AND o.voided = 0 "
            + "                                                                           AND e.voided = 0 "
            + "                                                                           AND e.location_id = :location "
            + "                                                                           AND e.encounter_type = ${6} "
            + "                                                                           AND o.concept_id = ${23739} "
            + "                                                                           AND o.value_coded IN ( ${23720}, ${23888}) "
            + "                                                                           AND e.encounter_datetime "
            + "                                                                           AND e.encounter_datetime >= :startDate "
            + "                                                                           AND e.encounter_datetime <= :endDate "
            + "                                                                           UNION "
            + "                                                                           SELECT p.patient_id, e.encounter_datetime "
            + "                                                                           FROM   patient p "
            + "                                                                           INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                                                                           INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                                                                           INNER JOIN (SELECT p.patient_id, MAX(e.encounter_datetime) consultation_date "
            + "                                                                                       FROM   patient p "
            + "                                                                                       INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                                                                                       WHERE  e.encounter_type = ${18} "
            + "                                                                                       AND e.location_id = :location "
            + "                                                                                       AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "                                                                                       AND e.voided = 0 "
            + "                                                                                       AND p.voided = 0 "
            + "                                                                                       GROUP  BY p.patient_id) recent_clinical "
            + "                                                                           ON recent_clinical.patient_id = p.patient_id "
            + "                                                                           WHERE  e.encounter_datetime = recent_clinical.consultation_date "
            + "                                                                                 AND e.encounter_type = ${18} "
            + "                                                                                 AND e.location_id = :location "
            + "                                                                                 AND o.concept_id = ${5096} "
            + "                                                                                 AND ( (DATEDIFF(o.value_datetime, e.encounter_datetime) >= ${lower} "
            + "                                                                                 AND DATEDIFF(o.value_datetime, e.encounter_datetime) <= ${upper}) OR (DATEDIFF(o.value_datetime, e.encounter_datetime) >= 173 AND DATEDIFF(o.value_datetime, e.encounter_datetime) <= 187) )"
            + "                                                                                 AND p.voided = 0 "
            + "                                                                                 AND e.voided = 0 "
            + "                                                                                 AND o.voided = 0 "
            + "                                                                                 GROUP  BY p.patient_id) most_recent "
            + "                                                                   GROUP  BY most_recent.patient_id) dispensation "
            + "                                     WHERE  dispensation.patient_id = p.patient_id) )  first_dispensation GROUP BY first_dispensation.patient_id)"
            + "                                     first_investigation ON first_investigation.patient_id = p.patient_id "
            + "                                 WHERE e.encounter_type = ${6} "
            + "                                 AND e.voided = 0 "
            + "                                 AND o.voided = 0 "
            + "                                 AND e.location_id = :location "
            + "                                 AND o.concept_id = ${23722} "
            + "                                 AND o.value_coded = ${856} "
            + "                                 AND e.encounter_datetime > first_investigation.first_date "
            + "                                 AND e.encounter_datetime <= :revisionEndDate "
            + "                                 AND e2.encounter_type = ${6} "
            + "                                 AND e2.location_id = :location "
            + "                                 AND e2.encounter_datetime > first_investigation.first_date "
            + "                                 AND e2.encounter_datetime <= :revisionEndDate "
            + "                                 AND e.encounter_datetime > e2.encounter_datetime "
            + "                                 AND e2.voided = 0 "
            + "                                 AND ( "
            + "                                      (o2.concept_id = ${856} AND o2.value_numeric < 1000) "
            + "                                      OR "
            + "                                      (o2.concept_id = ${1305} AND o2.value_coded IS NOT NULL) "
            + "                                    ) "
            + "                                AND o2.voided = 0"
            + "                                 GROUP BY p.patient_id ) two_dispensations"
            + "       INNER JOIN (SELECT p.patient_id, e.encounter_datetime AS vl_date "
            + "                   FROM   patient p "
            + "                          INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                   WHERE  e.encounter_type = ${13} "
            + "                          AND e.location_id = :location AND ( ( o.concept_id = ${856} AND o.value_numeric < 1000 ) "
            + "                                                                OR ( o.concept_id = ${1305} AND o.value_coded IS NOT NULL ) ) "
            + "                          AND p.voided = 0 "
            + "                          AND e.voided = 0 "
            + "                          AND o.voided = 0) vl_result "
            + "               ON two_dispensations.patient_id = vl_result.patient_id "
            + "WHERE  vl_result.vl_date > two_dispensations.second_date AND vl_result.vl_date <= :revisionEndDate";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  public CohortDefinition getPatientsWhoHadVLResultOnLaboratoryFormAfterSecondVLRequest(
      List<Integer> dispensationTypes) {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Utentes que têm o registo de dois pedidos de CV na Ficha Laboratório ");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, String> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId().toString());
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId().toString());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId().toString());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId().toString());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId().toString());
    map.put("165322", hivMetadata.getMdcState().getConceptId().toString());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId().toString());
    map.put("23739", hivMetadata.getTypeOfDispensationConcept().getConceptId().toString());
    map.put("23888", hivMetadata.getSemiannualDispensation().getConceptId().toString());
    map.put("23720", hivMetadata.getQuarterlyConcept().getConceptId().toString());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId().toString());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId().toString());
    map.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId().toString());
    map.put("lower", "83");
    map.put("upper", "97");
    map.put("dispensationTypes", getMetadataFrom(dispensationTypes));

    String query =
        "SELECT two_dispensations.patient_id "
            + "FROM  ( "
            + "SELECT p.patient_id, MIN(e.encounter_datetime) second_date "
            + "FROM patient p "
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "INNER JOIN encounter e2 ON e2.patient_id = p.patient_id "
            + "INNER JOIN obs o2 ON o2.encounter_id = e2.encounter_id "
            + "INNER JOIN "
            + " (SELECT patient_id, MIN(encounter_datetime) first_date "
            + "        FROM   (SELECT p.patient_id, e.encounter_datetime "
            + "                FROM   patient p "
            + "                       INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                       INNER JOIN obs o  ON o.encounter_id = e.encounter_id "
            + "                WHERE  e.encounter_type = ${6} "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                       AND o.concept_id = ${23722} "
            + "                       AND o.value_coded = ${856} "
            + "                       AND e.encounter_datetime <= :revisionEndDate "
            + "                       AND e.encounter_datetime >= (SELECT dt_date "
            + "                                                    FROM   (SELECT most_recent.patient_id, MAX(most_recent.encounter_datetime) dt_date "
            + "                                                            FROM   (SELECT p2.patient_id, e2.encounter_datetime "
            + "                                                                    FROM   patient p2 "
            + "                                                                    INNER JOIN encounter e2 ON e2.patient_id = p2.patient_id "
            + "                                                                    INNER JOIN obs otype ON otype.encounter_id = e2.encounter_id "
            + "                                                                    INNER JOIN obs ostate ON ostate.encounter_id = e2.encounter_id "
            + "                                                                    WHERE  e2.encounter_type = ${6} "
            + "                                                                         AND e2.location_id = :location "
            + "                                                                         AND otype.concept_id = ${165174} "
            + "                                                                         AND otype.value_coded IN( ${dispensationTypes} ) "
            + "                                                                         AND ostate.concept_id = ${165322} "
            + "                                                                         AND ostate.value_coded = ${1256} "
            + "                                                                         AND e2.encounter_datetime >= :startDate "
            + "                                                                         AND e2.encounter_datetime <= :endDate "
            + "                                                                         AND otype.obs_group_id = ostate.obs_group_id "
            + "                                                                         AND e2.voided = 0 "
            + "                                                                         AND p2.voided = 0 "
            + "                                                                         AND otype.voided = 0 "
            + "                                                                         AND ostate.voided = 0 "
            + "                                                             UNION "
            + "                                                             SELECT p.patient_id, e.encounter_datetime "
            + "                                                             FROM   patient p "
            + "                                                             INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                                                             INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                                                             WHERE  p.voided = 0 "
            + "                                                                 AND o.voided = 0 "
            + "                                                                 AND e.voided = 0 "
            + "                                                                 AND e.location_id = :location "
            + "                                                                 AND e.encounter_type = ${6} "
            + "                                                                 AND o.concept_id = ${23739} "
            + "                                                                 AND o.value_coded IN ( ${23720}, ${23888}) "
            + "                                                                 AND e.encounter_datetime "
            + "                                                                 AND e.encounter_datetime >= :startDate "
            + "                                                                 AND e.encounter_datetime <= :endDate "
            + "                                                         UNION "
            + "                                                         SELECT p.patient_id, e.encounter_datetime "
            + "                                                         FROM   patient p "
            + "                                                         INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                                                         INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                                                         INNER JOIN (SELECT p.patient_id, MAX(e.encounter_datetime) consultation_date "
            + "                                                         FROM   patient p "
            + "                                                         INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                                                         WHERE  e.encounter_type = ${18} "
            + "                                                             AND e.location_id = :location "
            + "                                                             AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "                                                             AND e.voided = 0 "
            + "                                                             AND p.voided = 0 "
            + "                                                         GROUP  BY p.patient_id) recent_clinical "
            + "                                                   ON recent_clinical.patient_id = p.patient_id "
            + "                                                   WHERE  e.encounter_datetime = recent_clinical.consultation_date "
            + "                                                   AND e.encounter_type = ${18} "
            + "                                                   AND e.location_id = :location "
            + "                                                   AND o.concept_id = ${5096} "
            + "                                                   AND ( (DATEDIFF(o.value_datetime, e.encounter_datetime) >= ${lower} "
            + "                                                   AND DATEDIFF(o.value_datetime, e.encounter_datetime) <= ${upper})"
            + "                                                      OR (DATEDIFF(o.value_datetime, e.encounter_datetime) >= 173 AND DATEDIFF(o.value_datetime, e.encounter_datetime) <= 187) )"
            + "                                                   AND p.voided = 0 "
            + "                                                   AND e.voided = 0 "
            + "                                                   AND o.voided = 0 "
            + "                                               GROUP  BY p.patient_id) most_recent "
            + "                                     GROUP  BY most_recent.patient_id) dispensation "
            + "                                     WHERE  dispensation.patient_id = p.patient_id) )  first_dispensation GROUP BY first_dispensation.patient_id)"
            + "                                     first_investigation ON first_investigation.patient_id = p.patient_id "
            + "                                 WHERE e.encounter_type = ${6} "
            + "                                 AND e.voided = 0 "
            + "                                 AND o.voided = 0 "
            + "                                 AND e.location_id = :location "
            + "                                 AND o.concept_id = ${23722} "
            + "                                 AND o.value_coded = ${856} "
            + "                                 AND e.encounter_datetime > first_investigation.first_date "
            + "                                 AND e.encounter_datetime <= :revisionEndDate "
            + "                                 AND e2.encounter_type = ${6} "
            + "                                 AND e2.location_id = :location "
            + "                                 AND e2.encounter_datetime > first_investigation.first_date "
            + "                                 AND e2.encounter_datetime <= :revisionEndDate "
            + "                                 AND e.encounter_datetime > e2.encounter_datetime "
            + "                                 AND e2.voided = 0 "
            + "                                 AND ( "
            + "                                      (o2.concept_id = ${856} AND o2.value_numeric < 1000) "
            + "                                      OR "
            + "                                      (o2.concept_id = ${1305} AND o2.value_coded IS NOT NULL) "
            + "                                    ) "
            + "                                AND o2.voided = 0"
            + "                                 GROUP BY p.patient_id ) two_dispensations"
            + "       INNER JOIN (SELECT p.patient_id, e.encounter_datetime AS vl_date "
            + "                   FROM   patient p "
            + "                          INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                   WHERE  e.encounter_type = ${13} "
            + "                          AND e.location_id = :location "
            + "                          AND  ((o.concept_id = ${856} AND o.value_numeric IS NOT NULL)   OR (o.concept_id = ${1305} AND o.value_coded IS NOT NULL)) "
            + "                          AND p.voided = 0 "
            + "                          AND e.voided = 0 "
            + "                          AND o.voided = 0) vl_result  ON two_dispensations.patient_id = vl_result.patient_id "
            + "WHERE  vl_result.vl_date > two_dispensations.second_date "
            + "       AND vl_result.vl_date <= :revisionEndDate";

    StringSubstitutor sb = new StringSubstitutor(map);
    cd.setQuery(sb.replace(query));

    return cd;
  }

  private String getMetadataFrom(List<Integer> dispensationTypes) {
    if (dispensationTypes == null || dispensationTypes.isEmpty()) {

      throw new RuntimeException("The list of encounters or concepts might not be empty ");
    }
    return StringUtils.join(dispensationTypes, ",");
  }

  public CohortDefinition getPatientsHavingTypeOfDispensationBasedOnTheirLastVlResults() {
    SqlCohortDefinition sql = new SqlCohortDefinition();
    sql.setName("Patients Having Type of Dispensation Based on Last VL");
    sql.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sql.addParameter(new Parameter("endDate", "End Date", Date.class));
    sql.addParameter(new Parameter("location", "Location", Location.class));
    sql.setQuery(ViralLoadQueries.getPatientsHavingTypeOfDispensationBasedOnTheirLastVlResults());
    return sql;
  }

  /**
   * Filtrando os pacientesutentes que têm o registo de início do MDS para pacienteutente estável na
   * última consulta decorrida há 12 meses (última “Data Consulta Clínica” >= “Data Fim Revisão” –
   * 12 meses+1dia e <= “Data Fim Revisão”), ou seja, registo de um MDC (MDC1 ou MDC2 ou MDC3 ou
   * MDC4 ou MDC5) como:
   *
   * <p>“GA” e o respectivo “Estado” = “Início” ou “DT” e o respectivo “Estado” = “Início” ou “DS” e
   * o respectivo “Estado” = “Início” ou “APE” e o respectivo “Estado” = “Início” ou “FR” e o
   * respectivo “Estado” = “Início” ou “DD” e o respectivo “Estado” = “Início” na última consulta
   * clínica (“Ficha Clínica”, coluna 24) decorrida entre: “Data Início de Avaliação” = “Data Fim de
   * Revisão” menos 12 meses + 1 dia “Data Fim de Avaliação” = “Data Fim de Revisão”
   *
   * <p>os utentes que têm o registo de “Tipo de Dispensa” = “DT” na última consulta (“Ficha
   * Clínica”) decorrida há 12 meses (última “Data Consulta Clínica” >= “Data Fim Revisão” – 12
   * meses+1dia e <= “Data Fim Revisão”)
   *
   * <p>os utentes com registo de “Tipo de Dispensa” = “DS” na última consulta (“Ficha Clínica”)
   * decorrida há 12 meses (última “Data Consulta Clínica” >= “Data Fim Revisão” – 12 meses+1dia e
   * <= “Data Fim Revisão”)
   *
   * <p>os utentes com registo de último levantamento na farmácia (FILA) há 12 meses (última “Data
   * Levantamento”>= “Data Fim Revisão” – 12 meses+1dia e <= “Data Fim Revisão”) com próximo
   * levantamento agendado para 83 a 97 dias ( “Data Próximo Levantamento” menos “Data
   * Levantamento”>= 83 dias e <= 97 dias)
   *
   * <p>os utentes com registo de último levantamento na farmácia (FILA) há 12 meses (última “Data
   * Levantamento”>= “Data Fim Revisão” – 12 meses+1dia e <= “Data Fim Revisão”) com próximo
   * levantamento agendado para 173 a 187 dias ( “Data Próximo Levantamento” menos “Data
   * Levantamento”>= 173 dias e <= 187 dias)
   */
  public CohortDefinition getPatientsWhoHadMdsOnMostRecentClinicalAndPickupOnFilaFR36BasedOnLastVl(
      List<Integer> dispensationTypes, List<Integer> states) {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName(
        "MDS para utentes estáveis que tiveram consulta no período de avaliação based on the last VL results");

    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));

    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition mdsLastClinical =
        getPatientsWithMdcOnMostRecentClinicalFormWithFollowingDispensationTypesAndStateBasedOnLastVl12Months(
            dispensationTypes, states);

    CohortDefinition dsd = getPatientsHavingTypeOfDispensationBasedOnTheirLastVlResults();

    CohortDefinition nextPickupBetween83And97 =
        QualityImprovement2020Queries.getPatientsWithPickupOnFilaBasedOnLastVl12Months(83, 97);

    CohortDefinition nextPickupBetween173And187 =
        QualityImprovement2020Queries.getPatientsWithPickupOnFilaBasedOnLastVl12Months(173, 187);

    compositionCohortDefinition.addSearch(
        "MDS",
        EptsReportUtils.map(
            mdsLastClinical, "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "DSDT",
        EptsReportUtils.map(dsd, "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "FILA83",
        EptsReportUtils.map(
            nextPickupBetween83And97,
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "FILA173",
        EptsReportUtils.map(
            nextPickupBetween173And187,
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.setCompositionString("MDS OR DSDT OR FILA83 OR FILA173");

    return compositionCohortDefinition;
  }

  /**
   * O sistema identificará os utentes registados num MDS para utentes estáveis para desagregação de
   * Denominador (FR4) e Numerador (FR5) da seguinte forma:
   *
   * <ul>
   *   <li>todos os utentes que têm o último registo de pelo menos um dos seguintes modelos na
   *       última consulta clínica (Ficha Clínica) antes da data do resultado da CV mais recente
   *       (“Data Última CV”), como um dos seguintes:
   *       <ul>
   *         <li>Último registo de MDC (MDC1 ou MDC2 ou MDC3 ou MDC4 ou MDC5) como “GA” e o
   *             respectivo “Estado” = “Início” ou “Continua”, ou
   *         <li>Último registo de MDC (MDC1 ou MDC2 ou MDC3 ou MDC4 ou MDC5) como “DT” e o
   *             respectivo “Estado” = “Início” ou “Continua”, ou último registo do “Tipo de
   *             Dispensa” = “DT” na Ficha Clínica ou
   *         <li>Último registo de MDC (MDC1 ou MDC2 ou MDC3 ou MDC4 ou MDC5) como “DS” e o
   *             respectivo “Estado” = “Início” ou “Continua”, ou último registo do “Tipo de
   *             Dispensa” = “DS” na Ficha Clínica ou
   *         <li>Último registo de MDC (MDC1 ou MDC2 ou MDC3 ou MDC4 ou MDC5) como “FR” e o
   *             respectivo “Estado” = “Início” ou “Continua”, ou
   *         <li>Último registo de MDC (MDC1 ou MDC2 ou MDC3 ou MDC4 ou MDC5) como “DD” e o
   *             respectivo “Estado” = “Início” ou “Continua”, ou
   *         <li>Último registo de MDC (MDC1 ou MDC2 ou MDC3 ou MDC4 ou MDC5) como “DCA” e o
   *             respectivo “Estado” = “Início” ou “Continua”,
   *       </ul>
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition
      getPatientsWithMdcOnMostRecentClinicalFormWithFollowingDispensationTypesAndStateBasedOnLastVl12Months(
          List<Integer> dispensationTypes, List<Integer> states) {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Utentes que têm o registo de dois pedidos de CV na Ficha Clinica ");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, String> map = new HashMap<>();
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId().toString());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId().toString());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId().toString());
    map.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId().toString());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId().toString());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId().toString());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId().toString());
    map.put("165322", hivMetadata.getMdcState().getConceptId().toString());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId().toString());
    map.put("dispensationTypes", getMetadataFrom(dispensationTypes));
    map.put("states", getMetadataFrom(states));

    String query =
        "SELECT out_p.patient_id "
            + "FROM   patient pp "
            + "       INNER JOIN encounter ep ON pp.patient_id = ep.patient_id "
            + "       INNER JOIN obs otype ON otype.encounter_id = ep.encounter_id "
            + "       INNER JOIN obs ostate ON ostate.encounter_id = ep.encounter_id "
            + "       INNER JOIN (SELECT patient_id, MAX(encounter_datetime) AS max_vl_date_and_max_ficha "
            + "                   FROM   (SELECT pp.patient_id, ee.encounter_datetime "
            + "                           FROM   patient pp "
            + "                                  INNER JOIN encounter ee ON pp.patient_id = ee.patient_id "
            + "                                  INNER JOIN obs oo ON ee.encounter_id = oo.encounter_id "
            + "                                  INNER JOIN (SELECT patient_id, DATE( Max(encounter_date)) AS vl_max_date "
            + "                                              FROM   (SELECT p.patient_id, DATE(e.encounter_datetime) AS encounter_date "
            + "                                                      FROM   patient p "
            + "                                                      INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                                                      INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                                                      WHERE  p.voided = 0 "
            + "                                                       AND e.voided = 0 "
            + "                                                       AND o.voided = 0 "
            + "                                                       AND e.encounter_type IN ( ${13}, ${6}, ${9}, ${51} ) "
            + "                                                       AND ( ( o.concept_id = ${856} AND o.value_numeric IS NOT  NULL ) "
            + "                                                             OR ( o.concept_id = ${1305}  AND o.value_coded IS NOT NULL ) ) "
            + "                                                       AND DATE(e.encounter_datetime) BETWEEN :startDate AND :endDate "
            + "                                                       AND e.location_id = :location "
            + "                                               UNION "
            + "                                               SELECT p.patient_id, DATE(o.obs_datetime) AS encounter_date "
            + "                                               FROM   patient p "
            + "                                               INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                                               INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                                               WHERE  p.voided = 0 "
            + "                                                 AND e.voided = 0 "
            + "                                                 AND o.voided = 0 "
            + "                                                 AND e.encounter_type IN ( ${53} ) "
            + "                                             AND ( ( o.concept_id = ${856} AND o.value_numeric IS NOT  NULL ) "
            + "                                                     OR ( o.concept_id = ${1305}  AND o.value_coded IS NOT NULL ) ) "
            + "                                                 AND DATE(o.obs_datetime) BETWEEN :startDate AND :endDate "
            + "                                                 AND e.location_id = :location) max_vl_date "
            + "                                                 GROUP  BY patient_id "
            + "                   ) vl_date_tbl ON pp.patient_id = vl_date_tbl.patient_id "
            + "                 WHERE ee.encounter_datetime < vl_date_tbl.vl_max_date "
            + "                 AND oo.voided = 0 "
            + "                 AND ee.voided = 0 "
            + "                 AND ee.location_id = :location "
            + "                 AND ee.encounter_type = ${6}) fin_tbl "
            + "                 GROUP  BY patient_id) out_p ON pp.patient_id = out_p.patient_id "
            + "WHERE  ep.encounter_type = ${6} "
            + "	AND        ep.location_id = :location "
            + "	AND        otype.concept_id = ${165174} "
            + "	AND        otype.value_coded IN (${dispensationTypes}) "
            + "	AND        ostate.concept_id = ${165322} "
            + "	AND        ostate.value_coded IN (${states}) "
            + "	AND        ep.encounter_datetime = out_p.max_vl_date_and_max_ficha  "
            + "	AND        otype.obs_group_id = ostate.obs_group_id "
            + "	AND        ep.voided = 0 "
            + "	AND        pp.voided = 0 "
            + "	AND        otype.voided = 0 "
            + "	AND        ostate.voided = 0 "
            + "	GROUP BY   pp.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    cd.setQuery(sb.replace(query));

    return cd;
  }

  public CohortDefinition getPatientsOnMQCat18Denominator() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Cat 18 Denominator");
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition startedArt = getMOHArtStartDate();
    CohortDefinition inTarv = resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13();
    CohortDefinition transferredIn =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    cd.addSearch(
        "startedArt",
        EptsReportUtils.map(
            startedArt, "startDate=${endDate-14m},endDate=${endDate-11m},location=${location}"));

    cd.addSearch("inTarv", EptsReportUtils.map(inTarv, "endDate=${endDate},location=${location}"));
    cd.addSearch(
        "transferredIn",
        EptsReportUtils.map(
            transferredIn, "startDate=${endDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("(startedArt AND inTarv) AND NOT transferredIn");

    return cd;
  }

  public CohortDefinition getPatientsOnMQCat18Numerator() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Cat 18 Numerator");
    cd.addParameter(new Parameter("revisionEndDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition denominator = getPatientsOnMQCat18Denominator();
    CohortDefinition diagnose =
        QualityImprovement2020Queries.getDisclosureOfHIVDiagnosisToChildrenAdolescents();

    cd.addSearch(
        "denominator",
        EptsReportUtils.map(denominator, "endDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "diagnose",
        EptsReportUtils.map(
            diagnose,
            "startDate=${revisionEndDate-14m},endDate=${revisionEndDate-11m},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.setCompositionString("denominator AND diagnose");

    return cd;
  }

  /**
   * <b>Description:</b> MQ-MOH Query For pregnant or Breastfeeding patients
   *
   * <p><b>Technical Specs</b>
   * <li>A - O sistema irá identificar mulheres grávidas registadas na consulta inicial selecionando
   *     todos os utentes do sexo feminino, independentemente da idade, e registados como
   *     “Grávida=Sim” na primeira consulta clínica decorrida durante o período de inclusão (“Data
   *     Consulta Inicial” >= “Data Fim Revisão” menos (-) 12 meses mais (+) 1 dia e <= “Data Fim
   *     Revisão” menos (-) 9 meses. Nota 1: é a primeira consulta clínica de sempre do utente que
   *     decorreu no período de inclusão.
   * <li>B - O sistema irá identificar mulheres lactantes registadas na consulta inicial
   *     selecionando todos os utentes do sexo feminino, independentemente da idade, e registados
   *     como “Lactante=Sim” na primeira consulta clínica decorrida durante o período de inclusão
   *     (“Data Consulta Inicial” >= “Data Fim Revisão” menos (-) 12 meses mais (+) 1 dia e <= “Data
   *     Fim Revisão” menos 9 (-) meses. Nota 1: é a primeira consulta clínica de sempre do utente
   *     que decorreu no período de inclusão.
   * <li>Nota 2: A mulher grávida e lactante ao mesmo tempo, ou seja com registo de “Grávida=Sim” e
   *     “Lactante=Sim” na mesma consulta inicial, será considerada como grávida.
   * <li>Nota: Para o registo de mulheres grávidas deve se considerar a consulta inicial e não
   *     qualquer consulta durante o periodo de inclusão.
   *
   * @param question The question Concept Id
   * @param answer The value coded Concept Id
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getMOHPregnantORBreastfeedingOnClinicalConsultation(
      int question, int answer) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Pregnant Or Breastfeeding");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("question", question);
    map.put("answer", answer);

    String query =
        "SELECT primeira.patient_id "
            + "                      FROM  (SELECT p.patient_id, "
            + "                                   Min(e.encounter_datetime) AS first_consultation "
            + "                            FROM   patient p "
            + "                                   inner join encounter e "
            + "                                           ON e.patient_id = p.patient_id "
            + "                            WHERE  e.encounter_type = ${6} "
            + "                                   AND e.encounter_datetime <= :revisionEndDate "
            + "                                   AND e.voided = 0 "
            + "                                   AND p.voided = 0 "
            + "                                   AND e.location_id = :location "
            + "                            GROUP  BY p.patient_id) AS primeira "
            + "                           inner join encounter enc "
            + "                                   ON enc.patient_id = primeira.patient_id "
            + "                           inner join obs o "
            + "                                   ON o.encounter_id = enc.encounter_id "
            + "                           inner join person pe "
            + "                                   ON pe.person_id = primeira.patient_id "
            + "                        WHERE enc.encounter_datetime = primeira.first_consultation "
            + "                        AND enc.encounter_datetime >= :startDate "
            + "                        AND enc.encounter_datetime <= :endDate "
            + "                        AND enc.voided = 0 "
            + "                        AND o.voided = 0 "
            + "                        AND enc.encounter_type = ${6} "
            + "                        AND o.concept_id = ${question} "
            + "                        AND o.value_coded = ${answer} "
            + "                        AND pe.gender = 'F' "
            + "                        AND enc.location_id = :location "
            + "                        GROUP BY primeira.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * Registo da primeira consulta clínica durante o período de inclusão (>= “Data Fim de Revisão”
   * menos (-) 12 meses mais (+) 1 dia e <= “Data fim de Revisão” menos (-) 9 meses).
   *
   * <p>Nota: é a primeira consulta clínica de sempre do utente que decorreu no período de inclusão.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getFirstClinicalConsultationDuringInclusionPeriod() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" First Clinical Consultation During Inclusion Period ");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());

    String query =
        "SELECT final.patient_id  "
            + "FROM   (  "
            + "           SELECT pa.patient_id,  "
            + "                  MIN(enc.encounter_datetime) AS first_consultation  "
            + "           FROM   patient pa  "
            + "                      INNER JOIN encounter enc  "
            + "                                 ON enc.patient_id =  pa.patient_id  "
            + "                      INNER JOIN obs  "
            + "                                 ON obs.encounter_id = enc.encounter_id  "
            + "           WHERE pa.voided = 0  "
            + "             AND enc.voided = 0  "
            + "             AND obs.voided = 0  "
            + "             AND enc.encounter_type = ${6}  "
            + "             AND enc.encounter_datetime <= :revisionEndDate "
            + "             AND enc.location_id = :location  "
            + "           GROUP  BY pa.patient_id  "
            + "       ) final  "
            + "WHERE  final.first_consultation >= :startDate  "
            + "  AND final.first_consultation <= :endDate";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * Filtrando os que tiveram registo do “Pedido de CD4” na primeira consulta clínica do período de
   * inclusão (>= “Data Fim de Revisão” menos (-) 12 meses mais (+) 1 dia e <= “Data fim de Revisão”
   * menos (-) 9 meses).
   *
   * <p>Nota: é a primeira consulta clínica de sempre do utente que decorreu no período de inclusão.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getRequestForCd4OnFirstClinicalConsultationDuringInclusionPeriod() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" First Clinical Consultation During Inclusion Period ");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());

    String query =
        "SELECT pa.patient_id "
            + "FROM "
            + "    patient pa "
            + "        INNER JOIN encounter enc "
            + "                   ON enc.patient_id =  pa.patient_id "
            + "        INNER JOIN obs o2 "
            + "                   ON o2.encounter_id = enc.encounter_id "
            + "        INNER JOIN "
            + "    ( "
            + "        SELECT pa.patient_id, "
            + "               MIN(enc.encounter_datetime)  first_consultation "
            + "        FROM   patient pa "
            + "                   INNER JOIN encounter enc "
            + "                              ON enc.patient_id =  pa.patient_id "
            + "                   INNER JOIN obs "
            + "                              ON obs.encounter_id = enc.encounter_id "
            + "        WHERE  pa.voided = 0 "
            + "          AND enc.voided = 0 "
            + "          AND obs.voided = 0 "
            + "          AND enc.encounter_type = ${6} "
            + "          AND enc.encounter_datetime <= :revisionEndDate "
            + "          AND enc.location_id = :location "
            + "        GROUP  BY pa.patient_id "
            + "    ) final ON final.patient_id = pa.patient_id "
            + "        AND final.first_consultation >= :startDate "
            + "        AND final.first_consultation <= :endDate "
            + "WHERE pa.voided = 0 "
            + "  AND enc.voided = 0 "
            + "  AND o2.voided = 0 "
            + "  AND enc.encounter_type = ${6} "
            + "  AND enc.encounter_datetime = final.first_consultation "
            + "  AND o2.concept_id = ${23722} "
            + "  AND o2.value_coded = ${1695} "
            + "  AND enc.location_id = :location "
            + "GROUP BY pa.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>Description:</b> MQ-MOH Query For pregnant or Breastfeeding patients
   *
   * <p><b>Technical Specs</b>
   * <li>O sistema irá identificar mulheres que tiveram primeiro registo de gravidez durante o
   *     período de inclusão, ou seja, as que iniciaram a gravidez durante o período de inclusão,
   *     seleccionado:
   * <li>todos os utentes do sexo feminino, independentemente da idade, e registados como
   *     “Grávida=Sim” numa consulta clínica decorrida durante o período de inclusão (“Data Consulta
   *     Clínica Gravida” >= “Data Fim Revisão” menos (-) 12 meses mais (+) 1 dia e <= “Data Fim
   *     Revisão” menos (-) 9 meses.
   * <li>excluindo todos os utentes registados como “Grávida=Sim” numa consulta clínica decorrida
   *     nos últimos 9 meses antes do período de inclusão (“Data Consulta Clínica Gravida” < “Data
   *     Fim Revisão” menos (-) 12 meses mais (+) 1 dia e >= “Data Fim Revisão” menos (-) 12 meses
   *     mais (+) 1 dia menos (-) 9 meses).
   *
   * @param question The question Concept Id
   * @param answer The value coded Concept Id
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getFirstPregnancyORBreastfeedingOnClinicalConsultation(
      int question, int answer) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Mulheres com registo de primeira gravidez no período de inclusão");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("question", question);
    map.put("answer", answer);

    String query =
        "SELECT   pregnant.person_id "
            + "FROM     ( "
            + "                  SELECT   p.person_id, "
            + "                           Min(e.encounter_datetime) AS first_pregnancy "
            + "                  FROM     person p "
            + "                  JOIN     encounter e "
            + "                  ON       e.patient_id = p.person_id "
            + "                  JOIN     obs o "
            + "                  ON       o.encounter_id = e.encounter_id "
            + "                  AND      encounter_type = ${6} "
            + "                  AND      o.concept_id = ${question} "
            + "                  AND      o.value_coded = ${answer} "
            + "                  AND      e.location_id = :location "
            + "                  AND      e.encounter_datetime <= :revisionEndDate "
            + "                  AND      p.gender = 'F' "
            + "                  AND      e.voided = 0 "
            + "                  AND      o.voided = 0 "
            + "                  AND      p.voided = 0 "
            + "                  GROUP BY p.person_id) pregnant "
            + "WHERE    pregnant.first_pregnancy >= :startDate "
            + "AND      pregnant.first_pregnancy <= :endDate "
            + "AND      pregnant.person_id NOT IN "
            + "         ( "
            + "                SELECT p.person_id "
            + "                FROM   person p "
            + "                JOIN   encounter e "
            + "                ON     e.patient_id = p.person_id "
            + "                JOIN   obs o "
            + "                ON     o.encounter_id = e.encounter_id "
            + "                WHERE    encounter_type = ${6} "
            + "                AND    o.concept_id = ${question} "
            + "                AND    o.value_coded = ${answer} "
            + "                AND    e.location_id = :location "
            + "                AND    e.encounter_datetime >= date_sub(:startDate, interval 9 month ) "
            + "                AND    e.encounter_datetime < :startDate "
            + "                AND    p.gender = 'F' "
            + "                AND    e.voided = 0 "
            + "                AND    o.voided = 0 "
            + "                AND    p.voided = 0 ) "
            + "GROUP BY pregnant.person_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * Filtrando as que tiveram registo do “Pedido de CD4” na mesma consulta clínica na qual tiveram o
   * primeiro registo de Gravidez durante o período de inclusão (>= “Data Fim de Revisão” menos (-)
   * 12 meses mais (+) 1 dia e “Data fim de Revisão” menos (-) 9 meses).
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getRequestForCd4OnFirstClinicalConsultationOfPregnancy(
      int pregnantConcept, int yesConcept, int labResearchConcept, int cd4) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        " “Pedido de CD4” na mesma consulta clínica na qual tiveram o primeiro registo de Gravidez");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("question", pregnantConcept);
    map.put("answer", yesConcept);
    map.put("labResearchConcept", labResearchConcept);
    map.put("cd4", cd4);

    String query =
        "SELECT pa.patient_id "
            + "             FROM "
            + "                 patient pa "
            + "                     INNER JOIN encounter enc "
            + "                                ON enc.patient_id =  pa.patient_id "
            + "                     INNER JOIN obs o2 "
            + "                                ON o2.encounter_id = enc.encounter_id "
            + "                     INNER JOIN "
            + "                 ( "
            + IntensiveMonitoringCohortQueries.getPregnantOrBreastfeedingQuery()
            + "                 ) final ON final.person_id = pa.patient_id "
            + "             WHERE pa.voided = 0 "
            + "               AND enc.voided = 0 "
            + "               AND o2.voided = 0 "
            + "               AND enc.encounter_type = ${6} "
            + "               AND enc.encounter_datetime = final.first_consultation "
            + "               AND o2.concept_id = ${labResearchConcept} "
            + "               AND o2.value_coded = ${cd4} "
            + "               AND enc.location_id = :location "
            + "             GROUP BY pa.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   *
   * <li>Filtrando os que tiveram registo do “Resultado de CD4” numa consulta clínica decorrida em
   *     33 dias da consulta clínica com o primeiro registo de Gravidez no período de inclusão
   *     (“Data Consulta Grávida” >= “Data Fim de Revisão” menos (-) 12 meses mais (+) 1 dia e “Data
   *     fim de Revisão” menos (-) 9 meses), ou seja, “Data Resultado de CD4” menos a “Data Primeira
   *     Gravidez” <=33 dias <b>Nota: caso o resultado do CD4 esteja registado na consulta clínica
   *     com o primeiro registo de Gravidez deve ser considerado</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getCd4ResultAfterFirstConsultationOfPregnancy(
      int pregnantConcept, int yesConcept) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "Filter all patients with CD4 within 33 days from the first pregnancy consultation");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("730", hivMetadata.getCD4PercentConcept().getConceptId());
    map.put("165515", hivMetadata.getCD4SemiQuantitativeConcept().getConceptId());
    map.put("question", pregnantConcept);
    map.put("answer", yesConcept);

    String query =
        "SELECT pa.patient_id "
            + "FROM "
            + "    patient pa "
            + "        INNER JOIN encounter enc "
            + "                   ON enc.patient_id =  pa.patient_id "
            + "        INNER JOIN obs "
            + "                   ON obs.encounter_id = enc.encounter_id "
            + "        INNER JOIN obs o2 "
            + "                   ON o2.encounter_id = enc.encounter_id "
            + "        INNER JOIN "
            + "    ( "
            + IntensiveMonitoringCohortQueries.getPregnantOrBreastfeedingQuery()
            + "    ) consultation_date ON consultation_date.person_id = pa.patient_id "
            + "WHERE  pa.voided = 0 "
            + "  AND enc.voided = 0 "
            + "  AND obs.voided = 0 "
            + "  AND o2.voided = 0 "
            + "  AND enc.encounter_type = ${6} "
            + "  AND ( "
            + "        (obs.concept_id = ${1695} AND obs.value_numeric IS NOT NULL) "
            + "        OR "
            + "        (o2.concept_id = ${730} AND o2.value_numeric IS NOT NULL) "
            + "        OR "
            + "        (o2.concept_id = ${165515} AND o2.value_coded IS NOT NULL) "
            + "      ) "
            + "  AND enc.encounter_datetime >= consultation_date.first_consultation "
            + "  AND enc.encounter_datetime <= DATE_ADD(consultation_date.first_consultation, INTERVAL 33 DAY) "
            + "  AND enc.location_id = :location "
            + "GROUP BY pa.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>Description:</b> MOH Transferred Out Query
   *
   * <p><b>Technical Specs</b>
   *
   * <blockquote>
   *
   * O sistema irá identificar utentes “Transferido Para” outras US em TARV durante o período de
   * revisão seleccionando os utentes registados como:
   *
   * <ul>
   *   <li>[“Mudança Estado Permanência TARV” (Coluna 21) = “T” (Transferido Para) na “Ficha
   *       Clínica” com “Data da Consulta Actual” (Coluna 1, durante a qual se fez o registo da
   *       mudança do estado de permanência TARV) dentro do período de revisão ou
   *   <li>>registados como “Mudança Estado Permanência TARV” = “Transferido Para”, último estado
   *       registado na “Ficha Resumo” com “Data da Transferência” dentro do período de revisão;
   * </ul>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   *     <li><strong>Should</strong> Returns empty if there is no patient who meets the conditions
   *     <li><strong>Should</strong> fetch all patients transfer out other facility
   */
  public CohortDefinition getTranferredOutPatients() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("All patients registered as Transferred Out");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());

    String query =
        "SELECT transferred_out.patient_id "
            + "        FROM   (SELECT p.patient_id, "
            + "               last_clinical_state.last_date AS last_date "
            + "                FROM   patient p "
            + "                           JOIN encounter e "
            + "                                ON p.patient_id = e.patient_id "
            + "                           JOIN obs o "
            + "                                ON e.encounter_id = o.encounter_id "
            + "                           JOIN (SELECT p.patient_id, "
            + "                                        Max(e.encounter_datetime) AS last_date "
            + "                                 FROM   patient p "
            + "                                            JOIN encounter e "
            + "                                                 ON p.patient_id = e.patient_id "
            + "                                            JOIN obs o "
            + "                                                 ON e.encounter_id = o.encounter_id "
            + "                                 WHERE  p.voided = 0 "
            + "                                   AND e.voided = 0 "
            + "                                   AND o.voided = 0 "
            + "                                   AND e.location_id = :location "
            + "                                   AND e.encounter_type = ${6} "
            + "                                   AND e.encounter_datetime >= :startDate "
            + "                                   AND e.encounter_datetime <= :revisionEndDate "
            + "                                   AND o.concept_id = ${6273} "
            + "                                   AND o.value_coded IS NOT NULL "
            + "                                 GROUP  BY p.patient_id) last_clinical_state "
            + "                                ON last_clinical_state.patient_id = p.patient_id "
            + "                WHERE  p.voided = 0 "
            + "                  AND e.voided = 0 "
            + "                  AND e.location_id = :location "
            + "                  AND e.encounter_type = ${6} "
            + "                  AND e.encounter_datetime = last_clinical_state.last_date "
            + "                  AND o.voided = 0 "
            + "                  AND o.concept_id = ${6273} "
            + "                  AND o.value_coded = ${1706} "
            + "                GROUP  BY p.patient_id "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       Max(o.obs_datetime) AS last_date "
            + "                FROM   patient p "
            + "                       JOIN encounter e "
            + "                         ON p.patient_id = e.patient_id "
            + "                       JOIN obs o "
            + "                         ON e.encounter_id = o.encounter_id "
            + "                WHERE  p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                       AND e.encounter_type = ${53} "
            + "                       AND o.obs_datetime >= :startDate "
            + "                       AND o.obs_datetime <= :revisionEndDate "
            + "                       AND o.concept_id = ${6272} "
            + "                       AND o.value_coded = ${1706} "
            + "                GROUP  BY p.patient_id) transferred_out ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   *
   *
   * <blockquote>
   *
   * O sistema irá identificar utentes “Transferido Para” outras US em TARV durante o período de
   * revisão seleccionando os utentes registados como:
   *
   * <ul>
   *   <li>Último registo de [“Mudança Estado Permanência TARV” (Coluna 21) = “T” (Transferido Para)
   *       na “Ficha Clínica” com “Data da Consulta Actual” (Coluna 1, durante a qual se fez o
   *       registo da mudança do estado de permanência TARV) durante do período de revisão (>= “Data
   *       Início Revisão” e <= “Data Fim Revisão”) ou
   *   <li>>registados como “Mudança Estado Permanência TARV” = “Transferido Para”, último estado
   *       registado na “Ficha Resumo” com “Data da Transferência” durante do período de revisão (>=
   *       “Data Início Revisão” e <= “Data Fim Revisão”);
   * </ul>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   *     <li><strong>Should</strong> Returns empty if there is no patient who meets the conditions
   *     <li><strong>Should</strong> fetch all patients transfer out other facility
   */
  public CohortDefinition getTranferredOutPatientsCat7() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("All patients registered as Transferred Out");
    sqlCohortDefinition.addParameter(
        new Parameter("revisionStartDate", "revisionStartDate", Date.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());

    String query =
        "SELECT transferout.patient_id "
            + "        FROM   (SELECT p.patient_id, "
            + "                       last_registed_clinical.last_date_registed AS max_date "
            + "                FROM   patient p "
            + "                       JOIN encounter e "
            + "                         ON p.patient_id = e.patient_id "
            + "                       JOIN obs o "
            + "                         ON e.encounter_id = o.encounter_id "
            + "                       JOIN (SELECT p.patient_id, "
            + "                                    Max(e.encounter_datetime) AS "
            + "                                    last_date_registed "
            + "                             FROM   patient p "
            + "                                    JOIN encounter e "
            + "                                      ON p.patient_id = e.patient_id "
            + "                                    JOIN obs o "
            + "                                      ON e.encounter_id = o.encounter_id "
            + "                             WHERE  p.voided = 0 "
            + "                                    AND e.voided = 0 "
            + "                                    AND o.voided = 0 "
            + "                                    AND e.location_id = :location "
            + "                                    AND e.encounter_type = ${6} "
            + "                                    AND e.encounter_datetime >= "
            + "                                        :revisionStartDate "
            + "                                    AND e.encounter_datetime <= :revisionEndDate "
            + "                                    AND o.concept_id = ${6273} "
            + "                                    AND o.value_coded IS NOT NULL "
            + "                             GROUP  BY p.patient_id) last_registed_clinical "
            + "                         ON last_registed_clinical.patient_id = p.patient_id "
            + "                WHERE  p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                       AND e.encounter_type = ${6} "
            + "                       AND e.encounter_datetime = "
            + "                           last_registed_clinical.last_date_registed "
            + "                       AND o.voided = 0 "
            + "                       AND o.concept_id = ${6273} "
            + "                       AND o.value_coded = ${1706} "
            + "                GROUP  BY p.patient_id "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       last_registed_resumo.last_date_registed AS last_date "
            + "                FROM   patient p "
            + "                       JOIN encounter e "
            + "                         ON p.patient_id = e.patient_id "
            + "                       JOIN obs o "
            + "                         ON e.encounter_id = o.encounter_id "
            + "                       JOIN (SELECT p.patient_id, "
            + "                                    Max(o.obs_datetime) AS last_date_registed "
            + "                             FROM   patient p "
            + "                                    JOIN encounter e "
            + "                                      ON p.patient_id = e.patient_id "
            + "                                    JOIN obs o "
            + "                                      ON e.encounter_id = o.encounter_id "
            + "                             WHERE  p.voided = 0 "
            + "                                    AND e.voided = 0 "
            + "                                    AND o.voided = 0 "
            + "                                    AND e.location_id = :location "
            + "                                    AND e.encounter_type = ${53} "
            + "                                    AND o.obs_datetime >= "
            + "                                        :revisionStartDate "
            + "                                    AND o.obs_datetime <= :revisionEndDate "
            + "                                    AND o.concept_id = ${6272} "
            + "                                    AND o.value_coded IS NOT NULL "
            + "                             GROUP  BY p.patient_id) last_registed_resumo "
            + "                         ON last_registed_resumo.patient_id = p.patient_id "
            + "                WHERE  p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                       AND e.encounter_type = ${53} "
            + "                       AND o.obs_datetime = "
            + "                           last_registed_resumo.last_date_registed "
            + "                       AND o.concept_id = ${6272} "
            + "                       AND o.value_coded = ${1706} "
            + "                GROUP  BY p.patient_id) transferout ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>MQC11B2</b>: Utentes em 1ª Linha elegíveis ao pedido de CV <br>
   *
   * <ul>
   *   <li>incluindo os utentes há pelo menos 6 meses na 1ª Linha de TARV, ou seja, incluindo todos
   *       os utentes que têm o último registo da “Linha Terapêutica” na Ficha Clínica durante o
   *       período de revisão igual a “1ª Linha” (última consulta, “Data 1ª Linha”>= “Data Início
   *       Revisão” e <= “Data Fim Revisão”), sendo a “Data 1ª Linha” menos (-) “Data do Início
   *       TARV” registada na Ficha Resumo maior ou igual (>=) a 6 meses
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsOnArtFirstLineForMoreThanSixMonthsFromArtStartDate() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("1ª Linha de TARV há mais de 6 meses do Início TARV");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("21151", hivMetadata.getTherapeuticLineConcept().getConceptId());
    map.put("21150", hivMetadata.getFirstLineConcept().getConceptId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());

    String query =
        " SELECT p.patient_id  "
            + "FROM   patient p  "
            + "           INNER JOIN encounter e  "
            + "                      ON e.patient_id = p.patient_id  "
            + "           INNER JOIN obs o  "
            + "                      ON o.encounter_id = e.encounter_id  "
            + "           INNER JOIN (SELECT p.patient_id,  "
            + "                              Max(e.encounter_datetime) AS encounter_datetime  "
            + "                       FROM   patient p  "
            + "                                  INNER JOIN encounter e  "
            + "                                             ON e.patient_id = p.patient_id  "
            + "                                  JOIN obs o  "
            + "                                       ON o.encounter_id = e.encounter_id  "
            + "                       WHERE  e.encounter_type = ${6}  "
            + "                         AND p.voided = 0  "
            + "                         AND e.voided = 0  "
            + "                         AND e.location_id = :location  "
            + "                         AND o.voided = 0  "
            + "                         AND e.encounter_datetime BETWEEN  "
            + "                           :startDate AND :revisionEndDate  "
            + "                       GROUP  BY p.patient_id) last_consultation  "
            + "                      ON p.patient_id = last_consultation.patient_id  "
            + "           INNER JOIN (  "
            + "    SELECT p.patient_id, Min(o.value_datetime) art_date  "
            + "            FROM patient p  "
            + "                     INNER JOIN encounter e  "
            + "                                ON p.patient_id = e.patient_id  "
            + "                     INNER JOIN obs o  "
            + "                                ON e.encounter_id = o.encounter_id  "
            + "            WHERE  p.voided = 0  "
            + "              AND e.voided = 0  "
            + "              AND o.voided = 0  "
            + "              AND e.encounter_type = ${53}  "
            + "              AND o.concept_id = ${1190}  "
            + "              AND o.value_datetime IS NOT NULL  "
            + "              AND o.value_datetime <= :revisionEndDate  "
            + "              AND e.location_id = :location  "
            + "            GROUP  BY p.patient_id ) art_start on art_start.patient_id = p.patient_id  "
            + "WHERE e.encounter_type = ${6}  "
            + "  AND o.concept_id = ${21151}  "
            + "  AND o.value_coded = ${21150}  "
            + "  AND e.location_id = :location  "
            + "  AND e.voided = 0  "
            + "  AND o.voided = 0  "
            + "  AND e.encounter_datetime = last_consultation.encounter_datetime  "
            + "  AND TIMESTAMPDIFF(MONTH, art_start.art_date, e.encounter_datetime) >= 6 ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>MQC11B2</b>: Utentes em 1ª Linha elegíveis ao pedido de CV <br>
   *
   * <ul>
   *   Todos utentes que Mudaram de Regime na 1ª Linha de TARV há pelo menos 6 meses, ou seja,
   *   incluindo todos os utentes que têm o último registo da “Alternativa a Linha – 1ª Linha” na
   *   Ficha Resumo, sendo a “Data Última Alternativa 1ª Linha” menos (-) “Data Última Consulta”
   *   maior ou igual (>=) a 6 meses
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getMOHPatientsOnTreatmentFor6Months() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "utentes que Mudaram de Regime na 1ª Linha de TARV há pelo menos 6 meses");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("21190", commonMetadata.getRegimenAlternativeToFirstLineConcept().getConceptId());
    map.put("23898", commonMetadata.getAlternativeLineConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "       INNER JOIN obs o2 "
            + "               ON e.encounter_id = o2.encounter_id "
            + "       INNER JOIN (SELECT p.patient_id, "
            + "                          Max(e.encounter_datetime) AS encounter_datetime "
            + "                   FROM   patient p "
            + "                          INNER JOIN encounter e "
            + "                                  ON e.patient_id = p.patient_id "
            + "                   WHERE  e.encounter_type = ${6} "
            + "                          AND p.voided = 0 "
            + "                          AND e.voided = 0 "
            + "                          AND e.location_id = :location "
            + "                          AND e.encounter_datetime BETWEEN "
            + "                              :startDate AND :endDate "
            + "                   GROUP  BY p.patient_id) last_consultation "
            + "               ON p.patient_id = last_consultation.patient_id "
            + "       INNER JOIN (SELECT p.patient_id, "
            + "                          Max(o2.obs_datetime) first_line_date "
            + "                   FROM   patient p "
            + "                          INNER JOIN encounter e "
            + "                                  ON p.patient_id = e.patient_id "
            + "                          INNER JOIN obs o "
            + "                                  ON e.encounter_id = o.encounter_id "
            + "                          INNER JOIN obs o2 "
            + "                                  ON e.encounter_id = o2.encounter_id "
            + "                   WHERE  p.voided = 0 "
            + "                          AND e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND o2.voided = 0 "
            + "                          AND e.encounter_type = ${53} "
            + "                          AND o.concept_id = ${23898} "
            + "                          AND o2.concept_id = ${21190} "
            + "                          AND o2.value_coded IS NOT NULL "
            + "                          AND o2.obs_datetime <= :endDate "
            + "                          AND o2.obs_group_id = o.obs_id "
            + "                          AND e.location_id = :location "
            + "                   GROUP  BY p.patient_id) regimen_change "
            + "               ON regimen_change.patient_id = p.patient_id "
            + "WHERE  Timestampdiff(month, regimen_change.first_line_date, "
            + "              last_consultation.encounter_datetime) >= 6 "
            + "GROUP  BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>Description:</b> Utentes em 1ª Linha elegíveis ao pedido de CV
   *
   * <p><b>Technical Specs</b>
   *
   * <blockquote>
   *
   * Excepto (excluindo) os utentes que, entretanto, mudaram de linha, ou seja, excepto os utentes
   * que têm um registo de “Linha Terapêutica”, na Ficha Clínica, diferente de “1ª Linha”, durante o
   * período compreendido entre “Data Última Alternativa 1ª Linha” e “Data Última Consulta. Nota:
   * “Data Última Consulta” é a data da última consulta clínica ocorrida durante o período de
   * revisão.
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getMOHPatientsToExclusion() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients to exclude in treatment in the last 6 months");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("21190", commonMetadata.getRegimenAlternativeToFirstLineConcept().getConceptId());
    map.put("23898", commonMetadata.getAlternativeLineConcept().getConceptId());
    map.put("21151", hivMetadata.getTherapeuticLineConcept().getConceptId());
    map.put("21150", hivMetadata.getFirstLineConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "             FROM   patient p "
            + "                    INNER JOIN encounter e "
            + "                            ON e.patient_id = p.patient_id "
            + "                    INNER JOIN obs o "
            + "                            ON o.encounter_id = e.encounter_id "
            + "                    INNER JOIN obs o2 "
            + "                            ON o2.encounter_id = e.encounter_id "
            + "                    INNER JOIN (SELECT p.patient_id, "
            + "                                       Max(e.encounter_datetime) AS encounter_datetime "
            + "                                FROM   patient p "
            + "                                       INNER JOIN encounter e "
            + "                                               ON e.patient_id = p.patient_id "
            + "                                       JOIN obs o "
            + "                                         ON o.encounter_id = e.encounter_id "
            + "                                WHERE  e.encounter_type = ${6} "
            + "                                       AND p.voided = 0 "
            + "                                       AND e.voided = 0 "
            + "                                       AND e.location_id = :location "
            + "                                       AND o.voided = 0 "
            + "                                       AND e.encounter_datetime BETWEEN "
            + "                                           :startDate AND :endDate "
            + "                                GROUP  BY p.patient_id) last_consultation "
            + "                            ON p.patient_id = last_consultation.patient_id "
            + "                    INNER JOIN (SELECT p.patient_id, "
            + "                                       Max(o2.obs_datetime) last_line_date "
            + "                                FROM   patient p "
            + "                                       INNER JOIN encounter e "
            + "                                               ON p.patient_id = e.patient_id "
            + "                                       INNER JOIN obs o "
            + "                                               ON e.encounter_id = o.encounter_id "
            + "                                       INNER JOIN obs o2 "
            + "                                               ON e.encounter_id = o2.encounter_id "
            + "                                WHERE  p.voided = 0 "
            + "                                       AND e.voided = 0 "
            + "                                       AND o.voided = 0 "
            + "                                       AND o2.voided = 0 "
            + "                                       AND e.encounter_type = ${53} "
            + "                                       AND ( ( o.concept_id = ${23898} ) "
            + "                                             AND ( o2.concept_id = ${21190} "
            + "                                                   AND o2.value_coded IS NOT NULL ) "
            + "                                             AND o.obs_datetime <= :endDate ) "
            + "                                       AND o2.obs_group_id = o.obs_id "
            + "                                       AND e.location_id = :location "
            + "                                GROUP  BY p.patient_id) regimen_change "
            + "                            ON regimen_change.patient_id = p.patient_id "
            + "             WHERE  e.encounter_type = 6 "
            + "                    AND o.concept_id = ${21151} "
            + "                    AND o.value_coded <> ${21150} "
            + "                    AND e.location_id = :location "
            + "                    AND e.voided = 0 "
            + "                    AND o.voided = 0 "
            + "                    AND o2.voided = 0 "
            + "                    AND DATE(e.encounter_datetime) >= DATE(regimen_change.last_line_date) "
            + "                    AND DATE(e.encounter_datetime) <= DATE(last_consultation.encounter_datetime) "
            + "                    GROUP BY patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>MQC11B2</b>: Utentes em 1ª Linha elegíveis ao pedido de CV <br>
   *
   * <ul>
   *   <li>incluindo todos os utentes com idade >= 15 anos (seguindo o critério definido no RF13) e
   *       com registo de uma Carga Viral na Ficha Clínica ou Ficha Resumo com resultado >= 1000
   *       cópias durante o período de inclusão (“Data da CV>=1000” >= “Data Início Inclusão” e <=
   *       “Data Fim Inclusão”)
   * </ul>
   *
   * @param vlQuantity Quantity of viral load to evaluate
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWithCargaViralonFichaClinicaAndFichaResumo(int vlQuantity) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "Carga Viral na Ficha Clínica ou Ficha Resumo com resultado >= 1000");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String query =
        "  SELECT vl.patient_id "
            + " FROM   (SELECT p.patient_id, "
            + "         CASE "
            + "           WHEN e.encounter_type = ${6} THEN MIN(e.encounter_datetime) "
            + "           WHEN e.encounter_type = ${53} THEN MIN(o.obs_datetime) "
            + "         END AS first_encounter "
            + "        FROM   patient p "
            + "               INNER JOIN encounter e "
            + "                       ON e.patient_id = p.patient_id "
            + "               INNER JOIN obs o "
            + "                       ON o.encounter_id = e.encounter_id "
            + "         WHERE "
            + "             ( "
            + "               (  (e.encounter_type = ${6} "
            + "                     AND e.encounter_datetime BETWEEN :startDate AND :endDate)"
            + "               OR (e.encounter_type = ${53} "
            + "                     AND o.obs_datetime BETWEEN :startDate AND :endDate) ) "
            + "             ) "
            + "         AND e.location_id = :location "
            + "         AND o.concept_id = ${856} "
            + "         AND o.value_numeric >= "
            + vlQuantity
            + "        AND p.voided = 0 "
            + "        AND e.voided = 0 "
            + "        AND o.voided = 0 "
            + " GROUP BY p.patient_id "
            + "        ) vl "
            + " GROUP BY vl.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * Filtrando os utentes com o registo de pelo menos 3 consultas de APSS/PP em 99 dias após a “Data
   * Início TARV”, isto é, as 3 consultas de APSS/PP devem ocorrer no período compreendido entre a
   * “Data Início TARV” + 1dia e “Data Início TARV” + 99 dias
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientWhoHadThreeApssAfterArtStart() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Three APss consultation");

    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getHistoricalDrugStartDateConcept().getConceptId());
    map.put("35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    String query =
        "SELECT patient_id "
            + " FROM ( "
            + " SELECT apss.patient_id, count(apss.encounter_id) consultations "
            + " FROM ( "
            + " SELECT p.patient_id ,e.encounter_datetime, e.encounter_id, art_date "
            + " FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + " INNER JOIN( "
            + "			 SELECT patient_id, art_date "
            + "			 FROM   (SELECT p.patient_id, Min(value_datetime) art_date "
            + "						 FROM patient p "
            + "				   INNER JOIN encounter e "
            + "					   ON p.patient_id = e.patient_id "
            + "				   INNER JOIN obs o "
            + "					   ON e.encounter_id = o.encounter_id "
            + "			   WHERE  p.voided = 0 "
            + "				   AND e.voided = 0 "
            + "				   AND o.voided = 0 "
            + "				   AND e.encounter_type = ${53} "
            + "				   AND o.concept_id = ${1190} "
            + "				   AND o.value_datetime IS NOT NULL "
            + "				   AND o.value_datetime <= :endDate "
            + "				   AND e.location_id = :location "
            + "			   GROUP  BY p.patient_id  ) "
            + "					union_tbl "
            + "			 WHERE  union_tbl.art_date BETWEEN :startDate AND :endDate "
            + " )art ON art.patient_id = p.patient_id "
            + " WHERE e.encounter_type = ${35} "
            + " AND e.location_id = :location "
            + " AND e.encounter_datetime >= :startDate "
            + " AND e.voided = 0 "
            + " ) apss "
            + " WHERE apss.encounter_datetime > apss.art_date AND apss.encounter_datetime <= DATE_ADD(art_date, INTERVAL 99 DAY) "
            + " GROUP BY apss.patient_id "
            + " HAVING consultations >= 3 "
            + " ) three_consultations";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <b>a.</b> com o último estado [“Mudança Estado Permanência TARV” (Coluna 21) = “O” (Óbito) na
   * Ficha Clínica com “Data da Consulta Actual” (Coluna 1, durante a qual se fez o registo da
   * mudança do estado de permanência TARV) <= “Data Fim de Revisão”; ou
   *
   * <p><b>
   *
   * <p>b.</b> com último estado “Mudança Estado Permanência TARV” = “Óbito” na Ficha Resumo antes
   * do fim do período de revisão (“Data de Óbito” <= “Data Fim de Revisão”); ou
   *
   * <p><b>
   *
   * <p>c.</b> como “‘Óbito” nos “Dados Demográficos do Utente” ou
   *
   * <p><b>
   *
   * <p>d.</b> como ‘Óbito’ (último estado de inscrição) no programa SERVIÇO TARV TRATAMENTO antes
   * do fim do período de revisão (“Data de Óbito” <= Data Fim de revisão”;
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getDeadPatientsComposition() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Get patients who are dead");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "deadByPatientProgramState",
        EptsReportUtils.map(
            txMlCohortQueries.getPatientsDeadInProgramStateByReportingEndDate(),
            "onOrBefore=${endDate},location=${location}"));
    cd.addSearch(
        "deadByPatientDemographics",
        EptsReportUtils.map(
            txCurrCohortQueries.getDeadPatientsInDemographiscByReportingEndDate(),
            "onOrBefore=${endDate}"));
    cd.addSearch(
        "deadRegisteredInFichaResumoAndFichaClinicaMasterCard",
        EptsReportUtils.map(
            txCurrCohortQueries.getDeadPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate(),
            "onOrBefore=${endDate},location=${location}"));
    cd.addSearch(
        "exclusion",
        EptsReportUtils.map(
            txMlCohortQueries.getExclusionForDeadOrSuspendedPatients(true),
            "endDate=${endDate},reportEndDate=${endDate},location=${location}"));

    cd.setCompositionString(
        "(deadByPatientDemographics OR deadByPatientProgramState "
            + " OR deadRegisteredInFichaResumoAndFichaClinicaMasterCard) AND NOT exclusion");

    return cd;
  }

  /**
   * O sistema irá identificar todos os utentes com Sarcoma de Karposi os que têm o registo de:
   *
   * <p>Outros diagnósticos – Sarcoma de Kaposi na “Ficha de Seguimento” registada até o fim do
   * período de revisão ou
   *
   * <p>Infecções oportunistas incluindo Sarcoma de Kaposi e outras doenças – Sarcoma de Kaposi
   * registado na “Ficha Clinica – Master Card” até o fim do período de revisão.
   *
   * @return {@link CohortDefinition}
   */
  public SqlCohortDefinition getPatientsWithSarcomaKarposi() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients with Sarcoma Karposi");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("507", hivMetadata.getKaposiSarcomaConcept().getConceptId());
    map.put("1406", hivMetadata.getOtherDiagnosis().getConceptId());

    String query =
        " SELECT p.patient_id "
            + "        FROM patient p "
            + "             INNER JOIN encounter e "
            + "                ON p.patient_id = e.patient_id "
            + "             INNER JOIN obs o "
            + "                ON e.encounter_id = o.encounter_id "
            + "        WHERE p.voided = 0 "
            + "             AND e.voided = 0 "
            + "             AND o.voided = 0 "
            + "             AND e.encounter_type IN (${6},${9}) "
            + "             AND o.concept_id = ${1406} "
            + "             AND o.value_coded = ${507} "
            + "             AND e.location_id = :location "
            + "             AND e.encounter_datetime <= :endDate "
            + "        GROUP BY p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>RF 7.1 Utentes com Registo de Óbito (para exclusão)</b>
   *
   * <blockquote>
   *
   * <p>O sistema irá identificar utentes com registo de “Óbito” durante o período de revisão
   * seleccionando os utentes registados:
   *
   * <p><b>a.</b> com o último estado [“Mudança Estado Permanência TARV” (Coluna 21) = “O” (Óbito)
   * na Ficha Clínica com “Data da Consulta Actual” (Coluna 1, durante a qual se fez o registo da
   * mudança do estado de permanência TARV) <= “Data Fim de Revisão”; ou
   *
   * <p><b>b.</b> com último estado “Mudança Estado Permanência TARV” = “Óbito” na Ficha Resumo
   * antes do fim do período de revisão (“Data de Óbito” <= “Data Fim de Revisão”); ou
   *
   * <p><b>c.</b> como “‘Óbito” nos “Dados Demográficos do Paciente” ou
   *
   * <p><b>d.</b> como ‘Óbito’ (último estado de inscrição) no programa SERVIÇO TARV TRATAMENTO
   * antes do fim do período de revisão (“Data de Óbito” <= Data Fim de revisão” ;
   *
   * <p><b>Nota:.</b> Excluindo os utentes que tenham tido uma consulta clínica (Ficha Clínica) ou
   * levantamento de ARV (FILA) após a “Data de Óbito” (a data mais recente entre os critérios acima
   * identificados) e até “Data Fim Revisão”.
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getDeadPatientsCompositionMQ13() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Get patients who are dead according to criteria a,b,c,d and e");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("reportEndDate", "Report End Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "deadByPatientProgramState",
        EptsReportUtils.map(
            txMlCohortQueries.getPatientsDeadInProgramStateByReportingEndDate(),
            "onOrBefore=${endDate},location=${location}"));
    cd.addSearch(
        "deadByPatientDemographics",
        EptsReportUtils.map(
            txCurrCohortQueries.getDeadPatientsInDemographiscByReportingEndDate(),
            "onOrBefore=${endDate}"));
    cd.addSearch(
        "deadRegisteredInFichaResumoAndFichaClinicaMasterCard",
        EptsReportUtils.map(
            txCurrCohortQueries.getDeadPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate(),
            "onOrBefore=${endDate},location=${location}"));
    cd.addSearch(
        "exclusion",
        EptsReportUtils.map(
            txMlCohortQueries.getExclusionForDeadOrSuspendedPatients(true),
            "endDate=${endDate},reportEndDate=${endDate},location=${location}"));

    cd.setCompositionString(
        "(deadByPatientDemographics OR deadByPatientProgramState"
            + " OR deadRegisteredInFichaResumoAndFichaClinicaMasterCard) AND NOT exclusion");

    return cd;
  }

  /**
   * filtrando os utentes que estão na 1ª Linha de TARV no período de inclusão , ou seja, os utentes
   * com registo da última linha terapêutica, na Ficha Clínica durante o período de avaliação
   *
   * @return {@link CohortDefinition}
   */
  public SqlCohortDefinition getPatientsFirstLine() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients with LINHA TERAPEUTICA equal to PRIMEIRA LINHA");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("21151", hivMetadata.getTherapeuticLineConcept().getConceptId());
    map.put("21150", hivMetadata.getFirstLineConcept().getConceptId());

    String query =
        "SELECT     p.patient_id "
            + "FROM       patient p "
            + "INNER JOIN encounter e "
            + "ON         e.patient_id = p.patient_id "
            + "INNER JOIN obs o "
            + "ON         o.encounter_id = e.encounter_id "
            + "INNER JOIN "
            + "           ( "
            + "                      SELECT     p.patient_id, "
            + "                                 Max(e.encounter_datetime) AS last_clinical "
            + "                      FROM       patient p "
            + "                      INNER JOIN encounter e "
            + "                      ON         e.patient_id = p.patient_id "
            + "                      INNER JOIN obs o "
            + "                      ON         o.encounter_id = e.encounter_id "
            + "                      WHERE      e.encounter_type = ${6} "
            + "                      AND        p.voided = 0 "
            + "                      AND        e.voided = 0 "
            + "                      AND        o.voided = 0 "
            + "                      AND        e.location_id = :location "
            + "                      AND        o.concept_id = ${21151} "
            + "                      AND        e.encounter_datetime BETWEEN :startDate AND  :endDate "
            + "                      GROUP BY   p.patient_id) filtered "
            + "ON         filtered.patient_id = p.patient_id "
            + "WHERE      e.encounter_datetime = filtered.last_clinical "
            + "AND        p.voided = 0 "
            + "AND        e.voided = 0 "
            + "AND        o.voided = 0 "
            + "AND        o.concept_id = ${21151} "
            + "AND        o.value_coded = ${21150} "
            + "AND        e.encounter_type = ${6} "
            + "AND        e.location_id = :location "
            + "GROUP BY   p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>Utentes que terminaram tratamento de TB há menos de 30 dias (Exclusão)</b>
   *
   * <p>O sistema irá identificar todos os utentes que terminaram o tratamento de TB há menos de 30
   * dias, seleccionando:
   *
   * <ul>
   *   <li>Todos os utentes com último registo de Tratamento TB= Fim (F), com respectiva data de fim
   *       de tratamento (“Data Fim TB”) numa consulta clínica (Ficha Clínica- MasterCard) ocorrida
   *       até a data fim de revisão e.</i>
   *   <li>Sendo esta “Data Fim TB” ocorrida há menos de 30 dias da última consulta do período de
   *       revisão (“Data Última Consulta”), ou seja, “Data Última Consulta” menos (-) “Última
   *       Consulta Fim TB” <= 30 dias.</i>
   *       <p><b>Nota:</b> A “Data Última Consulta” é a última “Data de Consulta” no período de
   *       revisão.
   * </ul>
   */
  public CohortDefinition getPatientsWhoEndedTbTreatmentWithin30DaysOfLastClinicalConslutation() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Utentes que terminaram tratamento de TB há menos de 30 dias");
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "Revision End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1268", hivMetadata.getTBTreatmentPlanConcept().getConceptId());
    map.put("1267", hivMetadata.getCompletedConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "           INNER JOIN (SELECT p.patient_id, "
            + "                              Max(o.obs_datetime) AS last_tb_end "
            + "                       FROM   patient p "
            + "                                  INNER JOIN encounter e "
            + "                                             ON e.patient_id = p.patient_id "
            + "                                  INNER JOIN obs o "
            + "                                             ON o.encounter_id = e.encounter_id "
            + "                       WHERE  p.voided = 0 "
            + "                         AND e.voided = 0 "
            + "                         AND o.voided = 0 "
            + "                         AND e.encounter_type = ${6} "
            + "                         AND e.location_id = :location "
            + "                         AND o.concept_id = ${1268} "
            + "                         AND o.value_coded = ${1267} "
            + "                       GROUP  BY p.patient_id) tb_end "
            + "                      ON tb_end.patient_id = p.patient_id "
            + "           INNER JOIN (SELECT p.patient_id, "
            + "                              Max(e.encounter_datetime) AS max_consult "
            + "                       FROM   patient p "
            + "                                  INNER JOIN encounter e "
            + "                                             ON e.patient_id = p.patient_id "
            + "                       WHERE  p.voided = 0 "
            + "                         AND e.voided = 0 "
            + "                         AND e.encounter_type = ${6} "
            + "                         AND e.location_id = :location "
            + "                         AND e.encounter_datetime <= :revisionEndDate "
            + "                       GROUP  BY p.patient_id) last_consult "
            + "                      ON last_consult.patient_id = p.patient_id "
            + "WHERE  Timestampdiff(day, tb_end.last_tb_end, last_consult.max_consult) <= 30";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>RF27: Utentes reinicios TARV</b>
   *
   * <p>Incluindo todos os utentes que tiveram registo de “Mudança de E stado de Permanência” =
   * “Reinício” numa consulta clínica (Ficha Clínica) ocorrida durante o período de revisão (“Data
   * de Consulta Reinício” >= “Data Início Revisão” e <= “Data Fim Revisão”)
   *
   * <p><b>Nota</b>: em caso de existência de mais que uma consulta com registo de Reinício durante
   * o período de revisão, o sistema irá considerar o registo mais recente.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithRestartedStateOfStay() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Utentes reinicios TARV");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("1705", hivMetadata.getRestartConcept().getConceptId());

    String query =
        new EptsQueriesUtil()
            .patientIdQueryBuilder(
                QualityImprovement2020Queries.getPatientsWithRestartedStateOfStayQuery())
            .getQuery();

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>RF27: utentes que abandonaram o tratamento há mais de 3 meses</b>
   *
   * <p>Filtrando os utentes que abandonaram o tratamento há mais de 3 meses, ou seja, “Data de
   * Consulta Reinício” menos (-) “Data de Abandono” >= 99 dias. A “Data de Abandono” é a data mais
   * recente entre os seguintes critérios:
   * <li>“Data de Último Levantamento”, registado na Ficha Recepção/Lavantou ARV, antes da “Data de
   *     Consulta Reinício”), adicionando (+) 90 dias (Nota: 30 dias para identificar a data
   *     esperada do próximo levantamento, e 60 dias para identificar o abandono)
   * <li>“Data de Consulta” (Ficha Clínica) ocorrida antes da “Data de Consulta Reinício” e onde foi
   *     efectuado o registo de “Estado de Permanência” com resposta “Abandono”. Nota: será
   *     considerada a última consulta com registo de “Abandono” antes da consulta de reinício.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoAbandonedMoreThan3months() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Utentes utentes que abandonaram o tratamento há mais de 3 meses");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("1705", hivMetadata.getRestartConcept().getConceptId());
    map.put("1707", hivMetadata.getAbandonedConcept().getConceptId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());

    String query =
        "SELECT restarted.patient_id FROM ( "
            + QualityImprovement2020Queries.getPatientsWithRestartedStateOfStayQuery()
            + "                         ) restarted "
            + "INNER JOIN  (SELECT abandoned_dates.patient_id, MAX(abandoned_dates.last_date) AS abandoned_date "
            + "            FROM (SELECT p.patient_id, "
            + "                         date_add(max(o.value_datetime), INTERVAL 90 DAY) as last_date "
            + "                  FROM patient p "
            + "                           INNER JOIN encounter e "
            + "                                      ON e.patient_id = p.patient_id "
            + "                           INNER JOIN obs o "
            + "                                      ON o.encounter_id = e.encounter_id "
            + "                           INNER JOIN ( "
            + QualityImprovement2020Queries.getPatientsWithRestartedStateOfStayQuery()
            + "                                     ) restarted ON restarted.patient_id = p.patient_id "
            + "                  WHERE "
            + "                     e.voided = 0 "
            + "                    AND p.voided = 0 "
            + "                    AND o.voided = 0 "
            + "                    AND e.location_id = :location "
            + "                    AND e.encounter_type = ${52} "
            + "                    AND o.concept_id = ${23866} "
            + "                    AND o.value_datetime < restarted.restart_date "
            + "                  GROUP BY p.patient_id "
            + "                  UNION "
            + "                  SELECT p.patient_id, "
            + "                         MAX(e.encounter_datetime) AS last_date "
            + "                  FROM patient p "
            + "                           INNER JOIN encounter e "
            + "                                ON p.patient_id = e.patient_id "
            + "                           INNER JOIN obs o "
            + "                                ON e.encounter_id = o.encounter_id "
            + "                           INNER JOIN ( "
            + QualityImprovement2020Queries.getPatientsWithRestartedStateOfStayQuery()
            + "                                     ) restarted ON restarted.patient_id = p.patient_id "
            + "                  WHERE p.voided = 0 "
            + "                    AND e.voided = 0 "
            + "                    AND o.voided = 0 "
            + "                    AND e.location_id = :location "
            + "                    AND e.encounter_type = ${6} "
            + "                    AND e.encounter_datetime < restarted.restart_date "
            + "                    AND o.concept_id = ${6273} "
            + "                    AND o.value_coded = ${1707} "
            + "                  GROUP BY p.patient_id) abandoned_dates "
            + "            GROUP BY abandoned_dates.patient_id) abandoned "
            + "           ON abandoned.patient_id = restarted.patient_id "
            + "               AND DATEDIFF(restarted.restart_date, abandoned.abandoned_date) >= 99 ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>>RF27: Registo de resultado CD4 na consulta clínica</b>
   * <li>os utentes que tiveram registo de resultado CD4 na consulta clínica (Ficha Clínica),
   *     ocorrida “Data Consulta Reinício e “Data Fim Revisão” (“Data Consulta Resultado CD4” >=
   *     “Data de Consulta Reinício” e <= “Data de Consulta Reinício”)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getCd4ResultAfterRestartDate() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "os utentes que tiveram registo de resultado CD4 na consulta clínica (Ficha Clínica) "
            + "ocorrida entre “Data Consulta Reinício e “Data Fim Revisão” ");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    String query =
        "SELECT pa.patient_id "
            + "FROM "
            + "    patient pa "
            + "        INNER JOIN encounter e "
            + "                   ON e.patient_id =  pa.patient_id "
            + "        INNER JOIN obs "
            + "                   ON obs.encounter_id = e.encounter_id "
            + "        INNER JOIN "
            + "    ( "
            + QualityImprovement2020Queries.getPatientsWithRestartedStateOfStayQuery()
            + "    ) restarted ON restarted.patient_id = pa.patient_id "
            + "WHERE  pa.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND obs.voided = 0 "
            + "  AND e.encounter_type = ${6} "
            + "  AND ( "
            + "        (obs.concept_id = ${1695} AND obs.value_numeric IS NOT NULL) "
            + "        OR "
            + "        (obs.concept_id = ${730} AND obs.value_numeric IS NOT NULL) "
            + "        OR "
            + "        (obs.concept_id = ${165515} AND obs.value_coded IS NOT NULL) "
            + "      ) "
            + "  AND e.encounter_datetime >= restarted.restart_date "
            + "  AND e.encounter_datetime <= :endDate "
            + "  AND e.location_id = :location "
            + "GROUP BY pa.patient_id";

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("730", hivMetadata.getCD4PercentConcept().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("1705", hivMetadata.getRestartConcept().getConceptId());
    map.put("165515", hivMetadata.getCD4SemiQuantitativeConcept().getConceptId());

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>9.4 % de adultos (15/+anos) que receberam o resultado do CD4 dentro de 33 dias após consulta
   * clínica de reinício do TARV" / 9.8 % de crianças (0-14 anos) que receberam o resultado do CD4
   * dentro de 33 dias após consulta clínica de reinício do TARV</b>
   *
   * <p>Incluindo todos os utentes que tiveram registo de “Mudança de Estado de Permanência” =
   * “Reinício” numa consulta clínica (Ficha Clínica) ocorrida durante o período de revisão
   *
   * <p>Filtrando os utentes que receberam um resultado de CD4 numa consulta clínica ocorrida entre
   * “Data Consulta Reinício e “Data Fim Revisão” (“Data Resultado CD4”>= “Data Consulta Reinício e
   * <=”Data Fim Revisão”)
   *
   * <p>Incluindo todos os utentes com idade ≥15 anos (RF11.2)
   *
   * <p>excepto os que reiniciaram com menos de 30 dias do fim do período de revisão ( “Data
   * Consulta Reinício” menos (-) “Data Fim Revisão” < 33 dias)
   *
   * @see #getPatientsWithRestartedStateOfStay()
   * @see GenericCohortQueries#getAgeOnRestartedStateOfStayAndCd4Request(Integer, Integer)
   * @see #getCd4ResultAfterRestartDate()
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsRestartedAndEligibleForCd4Request(Integer denominator) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    switch (denominator) {
      case 4:
        cd.setName(
            "9.4 % de adultos (15/+anos) que receberam o resultado do CD4 dentro de 33 dias após consulta clínica de reinício do TARV");
        break;
      case 8:
        cd.setName(
            "9.8 % de crianças (0-14 anos) que receberam o resultado do CD4 dentro de 33 dias após consulta clínica de reinício do TARV");
        break;
    }
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch("RESTARTED", EptsReportUtils.map(getPatientsWithRestartedStateOfStay(), MAPPING3));
    cd.addSearch(
        "RESTARTED33DAYSBEFORE",
        EptsReportUtils.map(getPatientsRestartedWithLessThan33Days(), MAPPING3));

    cd.addSearch("RESULTS", EptsReportUtils.map(getCd4ResultAfterRestartDate(), MAPPING3));

    if (denominator == 4) {
      cd.addSearch(
          "AGE",
          EptsReportUtils.map(
              genericCohortQueries.getAgeOnRestartedStateOfStayAndCd4Request(15, null), MAPPING3));
    } else if (denominator == 8) {
      cd.addSearch(
          "AGE",
          EptsReportUtils.map(
              genericCohortQueries.getAgeOnRestartedStateOfStayAndCd4Request(0, 14), MAPPING3));
    }

    cd.setCompositionString("(RESTARTED AND RESULTS AND AGE) AND NOT RESTARTED33DAYSBEFORE");

    return cd;
  }

  /**
   * <b>Categoria 9 Denominador - Pedido e Resultado de CD4 nos Reinícios TARV– Adulto </b>
   *
   * <p>Incluindo todos os utentes que reiniciaram TARV durante o período de revisão e são elegíveis
   * ao pedido de CD4 (RF27)
   *
   * <p>Filtrando os utentes com idade ≥15 anos (seguindo o critério definido no RF11).
   *
   * <p>Excluindo todos os utentes “Transferido de” outra US (seguindo os critérios definidos no
   * RF5)
   *
   * <p>Nota: esta definição do denominador é a mesma para o denominador dos indicadores 9.7
   * (pedido) e 9.8 (resultado) do grupo de adultos reinícios TARV.
   *
   * @see #getPatientsWithRestartedStateOfStay()
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getAdultPatientsRestartedWithCd4RequestAndResult(int denominator) {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    switch (denominator) {
      case 3:
        cd.setName(
            "9.3 % de adultos (15/+anos) com pedido de CD4 na consulta clínica de reinício do TARV");
        break;
      case 7:
        cd.setName(
            "9.7 % de crianças (0-14 anos) com pedido de CD4 na consulta clínica de reinício do TARV");
        break;
      case 10:
        cd.setName(
            " 9.10 % de crianças HIV+ < 15 anos reinícios TARV que teve conhecimento do resultado do CD4 dentro de 33 dias após a data da consulta clínica de reinício TARV");
        break;
    }

    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    if (denominator == 3) {
      cd.addSearch(
          "AGE",
          EptsReportUtils.map(
              genericCohortQueries.getAgeOnRestartedStateOfStayConsultation(15, null), MAPPING3));
    } else if (denominator == 7) {
      cd.addSearch(
          "AGE",
          EptsReportUtils.map(
              genericCohortQueries.getAgeOnRestartedStateOfStayConsultation(0, 14), MAPPING3));
    } else if (denominator == 9 || denominator == 10) {
      cd.addSearch(
          "AGE",
          EptsReportUtils.map(
              genericCohortQueries.getAgeOnFirstClinicalConsultation(0, 14),
              "onOrAfter=${revisionEndDate-12m+1d},onOrBefore=${revisionEndDate-9m},revisionEndDate=${revisionEndDate},location=${location}"));
    }

    cd.addSearch("RESTARTED", EptsReportUtils.map(getPatientsWithRestartedStateOfStay(), MAPPING3));

    cd.addSearch(
        "transferredIn",
        EptsReportUtils.map(
            QualityImprovement2020Queries.getTransferredInPatients(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
                hivMetadata.getPatientFoundYesConcept().getConceptId(),
                hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
                hivMetadata.getArtStatus().getConceptId()),
            MAPPING));

    cd.setCompositionString("(RESTARTED AND AGE) AND NOT transferredIn");

    return cd;
  }

  /**
   * <b>Categoria 9 Numerador - Pedido CD4 nos Reinícios TARV - Adultos </b>
   *
   * <p>Incluindo todos os utentes do Denominador - Pedido de CD4 nos Reinícios TARV- Adulto
   * (definidos no RF21)
   *
   * <p>Filtrando os que tiveram registo do “Pedido de CD4” na consulta clínica de reinício durante
   * o período de revisão. Nota: é a consulta clínica de reinício na qual o utente é elegível ao
   * pedido de CD4 (seguindo os critérios definidos no RF27)
   *
   * @see #getAdultPatientsRestartedWithCd4RequestAndResult(int)
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithCd4RequestOnRestartedTarvDate(int numerator) {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    switch (numerator) {
      case 7:
        cd.setName(
            "9.7 % de crianças (0-14 anos) com pedido de CD4 na consulta clínica de reinício do TARV");
        break;
      case 8:
        cd.setName(
            "9.8 % de adultos HIV+ ≥ 15 anos reinícios TARV que teve conhecimento do resultado do CD4 dentro de 33 dias após a data da consulta clínica de reinício TARV");
        break;
      case 9:
        cd.setName(
            "9.9 % de crianças HIV+ < 15 anos que reiniciaram TARV durante o período de revisão e tiveram registo de pedido do CD4 na consulta de reinício");
        break;
      case 10:
        cd.setName(
            "9.10 % de crianças HIV+ < 15 anos reinícios TARV que teve conhecimento do resultado do CD4 dentro de 33 dias após a data da consulta clínica de reinício TARV");
    }

    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "REQUEST", EptsReportUtils.map(getPatientsWithCd4RequestsOnRestartedTarvDate(), MAPPING3));

    cd.addSearch(
        "RESULTS", EptsReportUtils.map(getPatientsWithCd4ResultsOnRestartedTarvDate(), MAPPING3));

    if (numerator == 7) {

      cd.addSearch(
          "DENOMINATOR",
          EptsReportUtils.map(getAdultPatientsRestartedWithCd4RequestAndResult(7), MAPPING1));

      cd.setCompositionString("DENOMINATOR AND REQUEST");
    } else if (numerator == 8) {

      cd.addSearch(
          "DENOMINATOR",
          EptsReportUtils.map(getPatientsRestartedAndEligibleForCd4Request(8), MAPPING1));

      cd.setCompositionString("DENOMINATOR AND RESULTS");
    } else if (numerator == 9) {

      cd.addSearch(
          "DENOMINATOR",
          EptsReportUtils.map(getCd4RequestAndResultForPregnantsCat9Den(9), MAPPING1));

      cd.setCompositionString("DENOMINATOR AND REQUEST");
    } else if (numerator == 10) {

      cd.addSearch(
          "DENOMINATOR",
          EptsReportUtils.map(getCd4RequestAndResultForPregnantsCat9Den(10), MAPPING1));

      cd.setCompositionString("DENOMINATOR AND RESULTS");
    }
    return cd;
  }

  /**
   * Filtrando os que tiveram registo do “Pedido de CD4” na consulta clínica de reinício durante o
   * período de revisão. Nota: é a consulta clínica de reinício na qual o utente é elegível ao
   * pedido de CD4 (seguindo os critérios definidos no RF27)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithCd4RequestsOnRestartedTarvDate() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "Os utentes que tiveram registo de “Pedido de CD4” na consulta clínica de reinício durante o período de revisão ");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    String query =
        "SELECT pa.patient_id "
            + "FROM "
            + "    patient pa "
            + "        INNER JOIN encounter e "
            + "                   ON e.patient_id =  pa.patient_id "
            + "        INNER JOIN obs "
            + "                   ON obs.encounter_id = e.encounter_id "
            + "        INNER JOIN "
            + "    ( "
            + QualityImprovement2020Queries.getPatientsWithRestartedStateOfStayQuery()
            + "    ) restarted ON restarted.patient_id = pa.patient_id "
            + "WHERE  pa.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND obs.voided = 0 "
            + "  AND e.encounter_type = ${6} "
            + "  AND obs.concept_id = ${23722} AND obs.value_coded = ${1695} "
            + "  AND e.encounter_datetime = restarted.restart_date "
            + "  AND e.location_id = :location "
            + "GROUP BY pa.patient_id";

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("1705", hivMetadata.getRestartConcept().getConceptId());

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * Filtrando os que tiveram registo do “Resultado de CD4” numa consulta clínica decorrida em 33
   * dias da consulta clínica de reinício durante o período de revisão. Nota: é a consulta clínica
   * de reinício na qual o utente é elegível ao pedido de CD4 (seguindo os critérios definidos no
   * RF27)
   *
   * @see #getAdultPatientsRestartedWithCd4RequestAndResult(int)
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithCd4ResultsOnRestartedTarvDate() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "Utentes que tiveram registo do “Resultado de CD4” numa consulta clínica decorrida em 33 dias da consulta clínica de reinício");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("730", hivMetadata.getCD4PercentConcept().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("1705", hivMetadata.getRestartConcept().getConceptId());
    map.put("165515", hivMetadata.getCD4SemiQuantitativeConcept().getConceptId());

    String query =
        "SELECT pa.patient_id "
            + "FROM "
            + "    patient pa "
            + "        INNER JOIN encounter enc "
            + "                   ON enc.patient_id =  pa.patient_id "
            + "        INNER JOIN obs "
            + "                   ON obs.encounter_id = enc.encounter_id "
            + "        INNER JOIN "
            + "    ( "
            + QualityImprovement2020Queries.getPatientsWithRestartedStateOfStayQuery()
            + "    ) restarted ON restarted.patient_id = pa.patient_id "
            + "WHERE  pa.voided = 0 "
            + "  AND enc.voided = 0 "
            + "  AND obs.voided = 0 "
            + "  AND enc.encounter_type = ${6} "
            + "  AND ( "
            + "        (obs.concept_id = ${1695} AND obs.value_numeric IS NOT NULL) "
            + "        OR "
            + "        (obs.concept_id = ${730} AND obs.value_numeric IS NOT NULL) "
            + "        OR "
            + "        (obs.concept_id = ${165515} AND obs.value_coded IS NOT NULL) "
            + "      ) "
            + "  AND enc.encounter_datetime >= restarted.restart_date "
            + "  AND enc.encounter_datetime <= DATE_ADD(restarted.restart_date, INTERVAL 33 DAY) "
            + "  AND enc.location_id = :location "
            + "GROUP BY pa.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>Utentes que reiniciaram o TARV nos últimos 3 meses</b>
   *
   * <p>O sistema irá identificar os utentes que reiniciaram o tratamento TARV nos últimos 3 meses,
   * da seguinte forma:
   *
   * <ul>
   *   <li>De todos os utentes activos em TARV “Data Fim Revisão” seguindo os critérios definidos no
   *       “Resumo Mensal de HIV/SIDA” Indicador B13, o sistema irá filtrar utentes que
   *   <li>tiveram interrupção no tratamento 3 meses antes do fim do período de revisão (“Data fim
   *       de revisão” menos (-) 3 meses) (FR49) e
   *   <li>tiveram pelo menos 1 registo de levantamento no “FILA” ou na “Ficha Recepção - Levantou
   *       ARV” nos últimos 3 meses do fim do períodio (“Data de levantamento ” >= “Data Fim
   *       Revisão” menos (–) 3 meses e <= “Data Fim Revisão”)
   * </ul>
   *
   * O sistema irá excluir:
   *
   * <ul>
   *   <li>Utentes Transferidos de outras Unidades Sanitárias
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoReturned() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Patients who returned to treatment");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition transferredIn =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    cd.addSearch(
        "treatmentInterruption",
        EptsReportUtils.map(
            eriDSDCohortQueries.getPatientsWhoExperiencedInterruptionInTreatment(),
            "endDate=${endDate-3m},location=${location}"));

    cd.addSearch(
        "filaOrDrugPickup",
        EptsReportUtils.map(
            eriDSDCohortQueries.getFilaOrDrugPickup(), "endDate=${endDate},location=${location}"));
    cd.addSearch(
        "transferredIn",
        EptsReportUtils.map(
            transferredIn, "startDate=${startDate},endDate=${endDate},location=${location}"));

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
   * <b>MQ9Den: M&Q Report - Categoria 9 Denominador</b><br>
   *
   * <ul>
   *   <li>9.1. % de adultos HIV+ em TARV que tiveram conhecimento do resultado do primeiro CD4
   *       dentro de 33 dias após a inscrição
   *   <li>9.2. % de crianças HIV+ em TARV que tiveram conhecimento do resultado do primeiro CD4
   *       dentro de 33 dias após a inscrição
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQ9Den5and6() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "AGE",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnFirstClinicalConsultation(0, 14),
            "onOrAfter=${revisionEndDate-12m+1d},onOrBefore=${revisionEndDate-9m},revisionEndDate=${revisionEndDate},location=${location}"));

    String inclusionPeriodMappings =
        "revisionEndDate=${revisionEndDate},startDate=${revisionEndDate-12m+1d},endDate=${revisionEndDate-9m},location=${location}";

    cd.addSearch(
        "A",
        EptsReportUtils.map(
            getFirstClinicalConsultationDuringInclusionPeriod(),
            "startDate=${revisionEndDate-12m+1d},endDate=${revisionEndDate-9m},revisionEndDate=${revisionEndDate},location=${location}"));

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
        "pregnantOnPeriod",
        EptsReportUtils.map(
            getMOHPregnantORBreastfeedingOnClinicalConsultation(
                commonMetadata.getPregnantConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            inclusionPeriodMappings));

    cd.addSearch(
        "breastfeedingOnPeriod",
        EptsReportUtils.map(
            getMOHPregnantORBreastfeedingOnClinicalConsultation(
                commonMetadata.getBreastfeeding().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            inclusionPeriodMappings));

    cd.setCompositionString(
        "((A AND AGE) AND NOT (C OR D OR E OR pregnantOnPeriod OR breastfeedingOnPeriod))");
    return cd;
  }

  /**
   * <b>adultos (15/+anos) na 1ª aou 2ª linha de TARV que tiveram consulta clínica no período de
   * revisão e que eram elegíveis ao pedido de CV </b>
   *
   * <p>Incluindo o somatório do resultado dos seguintes indicadores - para denominador:
   * <li>Denominador do Indicador 13.1-1ª Linha da Categoria 13 Adulto de Pedido de CV (RF16.1)
   * <li>Denominador do Indicador 13.4-2ª Linha da Categoria 13 Adulto de Pedido de CV (RF18)
   *
   *     <p>Incluindo o somatório do resultado dos seguintes indicadores - para numerador:
   * <li>Numerador do Indicador 13.1-1ª Linha da Categoria 13 Adulto de Pedido de CV (RF17.1).
   * <li>Numerador do Indicador 13.4-2ª Linha da Categoria 13 Adulto de Pedido de CV (RF19).
   *
   * @param denominator boolean parameter to choose between Denominator and Numerator
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getSumOfPatientsIn1stOr2ndLineOfArt(Boolean denominator) {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName(
        "# de adultos (15/+anos) na 1ª ou 2ª linha de TARV - Somatorio (numerador e denominador)");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    if (denominator) {
      cd.addSearch("PRIMEIRALINHA", Mapped.mapStraightThrough(getMQ13(true, 1)));

      cd.addSearch("SEGUNDALINHA", Mapped.mapStraightThrough(getMQ13(true, 4)));
    } else {
      cd.addSearch("PRIMEIRALINHA", Mapped.mapStraightThrough(getMQ13(false, 1)));

      cd.addSearch("SEGUNDALINHA", Mapped.mapStraightThrough(getMQ13(false, 4)));
    }

    cd.setCompositionString("PRIMEIRALINHA OR SEGUNDALINHA");

    return cd;
  }

  /**
   * <b>adultos (15/+anos) na 1ª aou 2ª linha de TARV que tiveram consulta clínica no período de
   * revisão e que eram elegíveis ao pedido de CV </b>
   *
   * <p>todos os utentes que tiveram registo de “Diagnóstico de TB Activa” = Sim numa Ficha Clínica
   * durante o período ou
   *
   * <p>registo de “Tratamento de TB” com “Estado” = “Início” ou “Continua” com a respectiva “Data
   * de Tratamento TB” ocorrida durante o período numa consulta clínica ocorrida durante o período
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithTbActiveOrTbTreatment() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("os utentes com diagnóstico TB Activa durante o período de revisão");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

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

    CohortDefinition tbTreatment =
        commonCohortQueries.getMohMQPatientsOnCondition(
            false,
            false,
            "once",
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getTBTreatmentPlanConcept(),
            Arrays.asList(
                hivMetadata.getStartDrugsConcept(), hivMetadata.getContinueRegimenConcept()),
            null,
            null);

    cd.addSearch("tbActive", Mapped.mapStraightThrough(tbActive));

    cd.addSearch("tbTreatment", Mapped.mapStraightThrough(tbTreatment));

    cd.setCompositionString("tbActive OR tbTreatment");

    return cd;
  }

  /**
   * <b># de crianças na 1a linha (10-14 anos de idade) ou 2ª linha (0-14 anos) de TARV que tiveram
   * consulta clínica no período de revisão e que eram elegíveis ao pedido de CV</b>
   *
   * <p>Incluindo o somatório do resultado dos seguintes indicadores - para denominador:
   * <li>Denominador do Indicador 13.8-1ª Linha da Categoria 13 Pediátrico de Pedido de CV (RF24.1).
   * <li>Denominador do Indicador 13.13-2ª Linha da Categoria 13 Pediátrico de Pedido de CV (RF26).
   *
   *     <p>Incluindo o somatório do resultado dos seguintes indicadores - para numerador:
   * <li>Numerador do Indicador 13.8-1ª Linha da Categoria 13 Pediátrico de Pedido de CV (RF25.1).
   * <li>Numerador do Indicador 13.13-2ª Linha da Categoria 13 Pediátrico de Pedido de CV (RF27).
   *
   * @param denominator boolean parameter to choose between Denominator and Numerator
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getSumOfPatientsIn1stOr2ndLineOfArtForDenNum8(Boolean denominator) {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName(
        "# criancas (10-14 anos de idade) na 1ª ou 2ª linha de TARV - Somatorio (numerador e denominador)");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    if (denominator) {
      cd.addSearch("PRIMEIRALINHA", Mapped.mapStraightThrough(getMQ13(true, 8)));

      cd.addSearch("SEGUNDALINHA", Mapped.mapStraightThrough(getMQ13(true, 13)));
    } else {
      cd.addSearch("PRIMEIRALINHA", Mapped.mapStraightThrough(getMQ13(false, 8)));

      cd.addSearch("SEGUNDALINHA", Mapped.mapStraightThrough(getMQ13(false, 13)));
    }

    cd.setCompositionString("PRIMEIRALINHA OR SEGUNDALINHA");

    return cd;
  }

  /**
   * <b># de crianças (15/+anos) na 1a ou 2ª linha de TARV ou mudança de regime de 1ª linhaV</b>
   *
   * <p>Incluindo o somatório do resultado dos seguintes indicadores - para denominador:
   * <li>Denominador do Indicador 13.2-1ª Linha da Categoria 13 Adulto de Resultado de CV (RF34.1).
   *     <liDenominador do Indicador 13.5-2ª Linha da Categoria 13 Adulto de Resultado de CV (RF42).
   *
   *     <p>Incluindo o somatório do resultado dos seguintes indicadores - para numerador:
   * <li>Numerador do Indicador 13.2-1ª Linha da Categoria 13 Adulto de Resultado de CV (RF35.1).
   * <li>Numerador do Indicador 13.5-2ª Linha da Categoria 13 Adulto de Resultado de CV (RF43).
   *
   * @param denominator boolean parameter to choose between Denominator and Numerator
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getSumOfPatientsIn1stOr2ndLineOfArtForDenNum2(Boolean denominator) {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName(
        "# criancas de (15/+anos) na 1a ou 2ª linha de TARV ou mudança de regime de 1ª linha");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    if (denominator) {
      cd.addSearch("PRIMEIRALINHA", Mapped.mapStraightThrough(getMQC13P3DEN(2)));

      cd.addSearch("SEGUNDALINHA", Mapped.mapStraightThrough(getMQC13P3DEN(5)));
    } else {
      cd.addSearch("PRIMEIRALINHA", Mapped.mapStraightThrough(getMQC13P3NUM(2)));

      cd.addSearch("SEGUNDALINHA", Mapped.mapStraightThrough(getMQC13P3NUM(5)));
    }

    cd.setCompositionString("PRIMEIRALINHA OR SEGUNDALINHA");

    return cd;
  }

  /**
   * <b># de crianças na 1a linha de TARV ou mudança de regime de 1ª linha (10-14 anos de idade) ou
   * 2ª Linha TARV (0-14 anos de idade) que receberam o resultado da CV entre o sexto e o nono mês
   * após início do TARV</b>
   *
   * <p>Incluindo o somatório do resultado dos seguintes indicadores - para denominador:
   * <li>Denominador do Indicador 13.11-1ª Linha da Categoria 13 Pediátrico de Resultado de CV
   *     (RF40.1).
   * <li>Denominador do Indicador 13.14-2ª Linha da Categoria 13 Pediátrico de Resultado de CV
   *     (RF44).
   *
   *     <p>Incluindo o somatório do resultado dos seguintes indicadores - para numerador:
   * <li>Numerador do Indicador 13.11-1ª Linha da Categoria 13 Pediátrico de Resultado de CV
   *     (RF41.1).
   * <li>Numerador do Indicador 13.14-2ª Linha da Categoria 13 Pediátrico de Resultado de CV (RF45).
   *
   * @param denominator boolean parameter to choose between Denominator and Numerator
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getSumOfPatientsIn1stOr2ndLineOfArtForDenNum11(Boolean denominator) {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName(
        "# de crianças na 1a linha de TARV ou mudança de regime de 1ª linha (10-14 anos de idade) "
            + "ou 2ª Linha TARV (0-14 anos de idade) que receberam o resultado da CV entre o sexto"
            + " e o nono mês após início do TARV");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    if (denominator) {
      cd.addSearch("PRIMEIRALINHA", Mapped.mapStraightThrough(getMQC13P3DEN(11)));

      cd.addSearch("SEGUNDALINHA", Mapped.mapStraightThrough(getMQC13P3DEN(14)));
    } else {
      cd.addSearch("PRIMEIRALINHA", Mapped.mapStraightThrough(getMQC13P3NUM(11)));

      cd.addSearch("SEGUNDALINHA", Mapped.mapStraightThrough(getMQC13P3NUM(14)));
    }

    cd.setCompositionString("PRIMEIRALINHA OR SEGUNDALINHA");

    return cd;
  }

  /**
   * <b>% de adultos (15/+anos) coinfectados TB/HIV com consulta clínica no período de revisão,
   * elegíveis ao pedido de CV e com registo de pedido de CV</b>
   * <li>incluindo todos os utentes com idade >= 15 anos (seguindo o critério definido no RF12) e
   *     que tiveram o registo de pelo menos uma consulta clínica durante o período de revisão
   *     (“Data Última Consulta”>= “Data Início Revisão” e <= “Data Fim Revisão”). Nota: considerar
   *     a última consulta clínica durante o período de revisão.
   * <li>incluindo as mulheres lactantes (independentemente da idade) registadas na última consulta
   *     clínica (seguindo o critério definido no RF11). Nota: serão considerados os dois grupos,
   *     adultos >=15 anos, e também as mulheres lactantes independentemente da idade.
   * <li>filtrando os utentes em 1ª Linha de TARV elegíveis ao pedido de Carga Viral (CV), seguindo
   *     os critérios definidos no RF14, ou os utentes em 2ª Linha de TARV elegíveis ao pedido de
   *     Carga Viral (CV), seguindo os critérios definidos no RF15.
   * <li>filtrando os utentes com diagnóstico TB Activa durante o período de revisão (RF60).
   * <li>excluindo mulheres grávidas registadas na última consulta clínica (seguindo os critérios
   *     definidos no RF10).
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getMQ13NewDen4() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName(
        " adultos (15/+anos) coinfectados TB/HIV com consulta clínica no período de revisão");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition lastClinical = commonCohortQueries.getMOHPatientsLastClinicalConsultation();

    CohortDefinition pregnant =
        commonCohortQueries.getNewMQPregnantORBreastfeeding(
            hivMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition breastfeeding =
        commonCohortQueries.getNewMQPregnantORBreastfeeding(
            hivMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition firstLine = getUtentesPrimeiraLinha(UtentesPrimeiraLinhaPreposition.MQ);

    CohortDefinition secondLine = getUtentesSegundaLinha(UtentesSegundaLinhaPreposition.MQ);

    CohortDefinition tbDiagnosisActive = getPatientsWithTbActiveOrTbTreatment();

    cd.addSearch(
        "AGE",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPatientsAgeOnLastClinicalConsultationDate(15, null),
            MAPPING3));

    cd.addSearch(
        "CONSULTATION",
        EptsReportUtils.map(
            lastClinical,
            "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "PREGNANT",
        EptsReportUtils.map(
            pregnant, "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "BREASTFEEDING",
        EptsReportUtils.map(
            breastfeeding,
            "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    cd.addSearch("FIRSTLINE", EptsReportUtils.map(firstLine, MAPPING1));

    cd.addSearch("SECONDLINE", EptsReportUtils.map(secondLine, MAPPING1));

    cd.addSearch("TBACTIVE", EptsReportUtils.map(tbDiagnosisActive, MAPPING3));

    cd.setCompositionString(
        "((CONSULTATION OR BREASTFEEDING) AND (FIRSTLINE OR SECONDLINE) AND TBACTIVE AND AGE) AND NOT PREGNANT ");

    return cd;
  }

  /**
   * <b>% de adultos (15/+anos) coinfectados TB/HIV com consulta clínica no período de revisão,
   * elegíveis ao pedido de CV e com registo de pedido de CV</b>
   * <li>incluindo todos os utentes selecionados no Indicador 13.4 Denominador definido no RF52
   *     (Categoria 13 TB/HIV Adulto Indicador 13.4 – Denominador Pedido CV) / incluindo todos os
   *     utentes selecionados no Indicador 13.13 Denominador definido no RF54 (Categoria 13 TB/HIV
   *     Pediátrico Indicador 13.13 - Denominador Pedido CV) e
   * <li>filtrando os utentes que têm o registo de “Pedido de Investigações Laboratoriais” igual a
   *     “Carga Viral” na última consulta clínica decorrida no período de revisão (“Data Última
   *     Consulta”).
   *
   * @see #getMQ13NewDen4()
   * @see #getMQ13NewDen13()
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getMQ13NewNum4(Boolean numerator4) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName(
        "% de adultos (15/+anos) coinfectados TB/HIV com consulta clínica no período de "
            + "revisão, elegíveis ao pedido de CV e com registo de pedido de CV");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition cvExamRequest = getMQ13G();

    cd.addSearch("EXAMREQUEST", EptsReportUtils.map(cvExamRequest, MAPPING3));

    if (numerator4) {
      cd.addSearch("DENOMINATOR", EptsReportUtils.map(getMQ13NewDen4(), MAPPING1));
    } else {
      cd.addSearch("DENOMINATOR", EptsReportUtils.map(getMQ13NewDen13(), MAPPING1));
    }

    cd.setCompositionString("DENOMINATOR AND EXAMREQUEST");

    return cd;
  }

  /**
   * <b>% de crianças (0-14 anos) coinfectados TB/HIV com consulta clínica no período de revisão,
   * elegíveis ao pedido de CV e com registo de pedido de CV</b>
   * <li>incluindo todos os utentes com idade >= 0 e <= 14 anos (seguindo o critério definido no
   *     RF12) e que tiveram o registo de pelo menos uma consulta clínica durante o período de
   *     revisão (“Data Última Consulta”>= “Data Início Revisão” e <= “Data Fim Revisão”). Nota:
   *     considerar a última consulta clínica durante o período de revisão.
   * <li>filtrando os utentes em 1ª Linha de TARV elegíveis ao pedido de Carga Viral (CV), seguindo
   *     os critérios definidos no RF14, ou os utentes em 2ª Linha de TARV elegíveis ao pedido de
   *     Carga Viral (CV), seguindo os critérios definidos no RF15.
   * <li>filtrando os utentes com diagnóstico TB Activa durante o período de revisão (RF60).
   * <li>excluindo mulheres grávidas registadas na última consulta clínica (seguindo os critérios
   *     definidos no RF10)
   * <li>excluindo mulheres lactantes registadas na última consulta clínica (seguindo os critérios
   *     definidos no RF11)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getMQ13NewDen13() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName(
        " adultos (15/+anos) coinfectados TB/HIV com consulta clínica no período de revisão");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition lastClinical = commonCohortQueries.getMOHPatientsLastClinicalConsultation();

    CohortDefinition pregnant =
        commonCohortQueries.getNewMQPregnantORBreastfeeding(
            hivMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition breastfeeding =
        commonCohortQueries.getNewMQPregnantORBreastfeeding(
            hivMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition firstLine = getUtentesPrimeiraLinha(UtentesPrimeiraLinhaPreposition.MQ);

    CohortDefinition secondLine = getUtentesSegundaLinha(UtentesSegundaLinhaPreposition.MQ);

    CohortDefinition tbDiagnosisActive = getPatientsWithTbActiveOrTbTreatment();

    cd.addSearch(
        "AGE",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPatientsAgeOnLastClinicalConsultationDate(0, 14), MAPPING3));

    cd.addSearch(
        "CONSULTATION",
        EptsReportUtils.map(
            lastClinical,
            "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "PREGNANT",
        EptsReportUtils.map(
            pregnant, "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "BREASTFEEDING",
        EptsReportUtils.map(
            breastfeeding,
            "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    cd.addSearch("FIRSTLINE", EptsReportUtils.map(firstLine, MAPPING1));

    cd.addSearch("SECONDLINE", EptsReportUtils.map(secondLine, MAPPING1));

    cd.addSearch("TBACTIVE", EptsReportUtils.map(tbDiagnosisActive, MAPPING3));

    cd.setCompositionString(
        "(CONSULTATION AND (FIRSTLINE OR SECONDLINE) AND TBACTIVE AND AGE) AND NOT (PREGNANT OR BREASTFEEDING)");

    return cd;
  }

  /**
   * <b>% de adultos (15/+anos) coinfectados TB/HIV com resultado de CV registado na FM</b>
   * <li>incluindo os utentes com idade >= 15 anos (seguindo o critério definido no RF13) e que
   *     iniciaram TARV no período de inclusão (seguindo os requisitos definidos no RF5) excluindo
   *     mulheres grávidas no início TARV (seguindo os critérios definidos no RF8)
   * <li>incluindo os utentes com idade >= 15 anos (seguindo o critério definido no RF13) e que têm
   *     o último registo de “Regime ARV Segunda Linha” na Ficha Resumo durante o período de
   *     inclusão (“Data Última 2ª Linha” >= “Data Início Inclusão” e <= “Data Fim Inclusão”)
   *     excepto os utentes que têm como “Justificação de Mudança do Tratamento” (associada a “Data
   *     Última 2ª Linha”) igual a “Gravidez”. Nota: serão considerados os dois grupos, os adultos
   *     >=15 anos que iniciaram tarv durante o período de inclusão, e também os adultos >=15 anos
   *     que iniciaram 2ª linha de tarv durante o período de inclusão.
   * <li>filtrando os utentes com diagnóstico TB Activa durante o período de inclusão (RF60).
   * <li>excluindo os utentes “Transferido de” outra US (seguindo os critérios definidos no RF6)
   * <li>excluindo os utentes “Transferido para” outra US (seguindo os critérios definidos no RF7)
   * <li>excluindo os utentes com registo de “Óbito” (seguindo os critérios definidos no RF7.1)
   * <li>excluindo os utentes abandono ou reinício TARV nos últimos 6 meses anteriores a última
   *     consulta do período de revisão (seguindo os critérios definidos no RF7.2);
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getMQ13NewDen5() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("% de adultos (15/+anos) coinfectados TB/HIV com resultado de CV registado na FM");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition startedART = getMOHArtStartDate();

    CohortDefinition pregnant =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition tbDiagnosisActive = getPatientsWithTbActiveOrTbTreatment();

    CohortDefinition arvRegimen = getPatientsOnRegimeArvSecondLine();

    CohortDefinition transferredIn =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    CohortDefinition transferredOut = getTranferredOutPatients();

    CohortDefinition dead = getDeadPatientsCompositionMQ13();

    CohortDefinition abandonedOrRestartedTarv =
        getPatientsWhoAbandonedOrRestartedTarvOnLast6MonthsArt();

    cd.addSearch(
        "ARTSTART",
        EptsReportUtils.map(
            startedART, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "PREGNANT",
        EptsReportUtils.map(
            pregnant, "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    cd.addSearch("ARVREGIMEN", EptsReportUtils.map(arvRegimen, MAPPING));

    cd.addSearch("TRANSFERREDIN", EptsReportUtils.map(transferredIn, MAPPING));

    cd.addSearch(
        "TRANSFERREDOUT",
        EptsReportUtils.map(
            transferredOut,
            "startDate=${startDate},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch("TBACTIVE", EptsReportUtils.map(tbDiagnosisActive, MAPPING));

    cd.addSearch("DEAD", EptsReportUtils.map(dead, MAPPING3));

    cd.addSearch("ABANDONED", EptsReportUtils.map(abandonedOrRestartedTarv, MAPPING1));

    cd.setCompositionString(
        "((ARTSTART AND NOT PREGNANT) OR ARVREGIMEN) AND TBACTIVE AND NOT (TRANSFERREDIN OR TRANSFERREDOUT OR DEAD OR ABANDONED)");

    return cd;
  }

  /**
   * <b>% de adultos (15/+anos) coinfectados TB/HIV com resultado de CV registado na FM</b>
   * <li>incluindo todos os utentes selecionados no Indicador 13.5 Denominador definido no RF56
   *     (Categoria 13 TB/HIV Adulto Indicador 13.5 - Denominador Resultado CV) e
   * <li>filtrando os utentes que tiveram um registo do Resultado de Carga Viral (quantitativo ou
   *     qualitativo) na Ficha Clínica ou Ficha Resumo (última carga viral) entre “Data de Início
   *     TARV” mais (+) 6meses e “Data de Início TARV” mais (+) 9meses ou entre “Data última 2ª
   *     Linha” mais (+) 6meses e “Data de Início TARV” mais (+) 9meses.
   *
   * @see #getMQ13NewDen5()
   * @see #getMQC13P3NUM_I()
   * @see #getMQC13P3NUM_L()
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getMQ13NewNum5(Boolean numerator5) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("adultos (15/+anos) coinfectados TB/HIV com resultado de CV registado na FM");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition vlOnSecondLine = getMQC13P3NUM_I();
    CohortDefinition vlOnTarv = getMQC13P3NUM_L();

    cd.addSearch(
        "VL2LINE",
        EptsReportUtils.map(
            vlOnSecondLine,
            "startDate=${startDate},endDate=${endDate},less3mDate=${startDate-3m},location=${location}"));
    cd.addSearch("VLTARV", EptsReportUtils.map(vlOnTarv, MAPPING));
    if (numerator5) {
      cd.addSearch("DENOMINATOR", EptsReportUtils.map(getMQ13NewDen5(), MAPPING1));
    } else {
      cd.addSearch("DENOMINATOR", EptsReportUtils.map(getMQ13NewDen14(), MAPPING1));
    }

    cd.setCompositionString("DENOMINATOR AND (VL2LINE OR VLTARV)");

    return cd;
  }

  /**
   * <b>% de crianças (0-14 anos) coinfectados TB/HIV com resultado de CV registado na FM</b>
   * <li>incluindo todos os utentes com idade >=0 e <=14 anos (seguindo o critério definido no RF13)
   *     e que iniciaram TARV no período de inclusão (seguindo os requisitos definidos no RF5)
   *     excluindo mulheres grávidas no início TARV (seguindo os critérios definidos no RF8) e
   * <li>incluindo todos os utentes com idade >=0 e <= 14 anos (seguindo o critério definido no
   *     RF13) e que têm o último registo de “Regime ARV Segunda Linha” na Ficha Resumo durante o
   *     período de inclusão (“Data Última 2ª Linha” >= “Data Início Inclusão” e <= “Data Fim
   *     Inclusão”) excepto os utentes que têm como “Justificação de Mudança do Tratamento”
   *     (associada a “Data Última 2ª Linha”) igual a “Gravidez”
   * <li>filtrando os utentes com diagnóstico TB Activa durante o período de inclusão (RF60).
   * <li>excluindo os utentes “Transferido de” outra US (seguindo os critérios definidos no RF6)
   * <li>excluindo os utentes “Transferido para” outra US (seguindo os critérios definidos no RF7)
   * <li>excluindo os utentes com registo de “Óbito” (seguindo os critérios definidos no RF7.1)
   * <li>excluindo os utentes abandono ou reinício TARV nos últimos 6 meses anteriores a última
   *     consulta do período de revisão (seguindo os critérios definidos no RF7.2);
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getMQ13NewDen14() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("crianças (0-14 anos) coinfectados TB/HIV com resultado de CV registado na FM");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition startedART = getMOHArtStartDate();

    CohortDefinition firstLine = getUtentesPrimeiraLinha(UtentesPrimeiraLinhaPreposition.MQ);

    CohortDefinition secondLine = getUtentesSegundaLinha(UtentesSegundaLinhaPreposition.MQ);

    CohortDefinition arvRegimen = getPatientsOnRegimeArvSecondLine();

    CohortDefinition tbDiagnosisActive = getPatientsWithTbActiveOrTbTreatment();

    CohortDefinition transferredIn =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    CohortDefinition transferredOut = getTranferredOutPatients();

    CohortDefinition dead = getDeadPatientsCompositionMQ13();

    CohortDefinition abandonedOrRestartedTarv =
        getPatientsWhoAbandonedOrRestartedTarvOnLast6MonthsArt();

    cd.addSearch(
        "AGE",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPatientsAgeOnLastClinicalConsultationDate(0, 14), MAPPING3));

    cd.addSearch("A", EptsReportUtils.map(startedART, MAPPING));

    cd.addSearch("FIRSTLINE", EptsReportUtils.map(firstLine, MAPPING1));

    cd.addSearch("SECONDLINE", EptsReportUtils.map(secondLine, MAPPING1));

    cd.addSearch("ARVREGIMEN", EptsReportUtils.map(arvRegimen, MAPPING));

    cd.addSearch("TBACTIVE", EptsReportUtils.map(tbDiagnosisActive, MAPPING));

    cd.addSearch("TRANSFERREDIN", EptsReportUtils.map(transferredIn, MAPPING));

    cd.addSearch(
        "TRANSFERREDOUT",
        EptsReportUtils.map(
            transferredOut,
            "startDate=${startDate},revisionEndDate=${revisionEndDate},location=${location}"));
    cd.addSearch("DEAD", EptsReportUtils.map(dead, MAPPING3));

    cd.addSearch("ABANDONED", EptsReportUtils.map(abandonedOrRestartedTarv, MAPPING1));

    cd.setCompositionString(
        "(AGE AND (A OR ARVREGIMEN)) AND TBACTIVE AND NOT (TRANSFERREDIN OR TRANSFERREDOUT OR DEAD OR ABANDONED)");

    return cd;
  }

  /**
   * <b> excepto os que reiniciaram com menos de 30 dias do fim do período de revisão (“Data
   * Consulta Reinício” menos (-) “Data Fim Revisão” < 33 dias) </b>
   *
   * @see QualityImprovement2020Queries#getPatientsWithRestartedStateOfStayQuery()
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsRestartedWithLessThan33Days() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "os que reiniciaram com menos de 30 dias do fim do período de revisão"
            + " (“Data Consulta Reinício” menos (-) “Data Fim Revisão” < 33 dias");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    String query =
        "SELECT restart.patient_id "
            + " FROM   ( "
            + QualityImprovement2020Queries.getPatientsWithRestartedStateOfStayQuery()
            + " )restart "
            + " WHERE "
            + " TIMESTAMPDIFF(DAY, restart.restart_date, :endDate) < 33 ";

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("1705", hivMetadata.getRestartConcept().getConceptId());

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>RF8 - Utentes Presuntivos de TB</b>
   *
   * @return {@link String}
   */
  static String getUnionQueryUtentesPresuntivos() {

    HivMetadata hivMetadata = new HivMetadata();
    TbMetadata tbMetadata = new TbMetadata();

    int encounterType1 = hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId();
    int concept1 = hivMetadata.getTBSymptomsConcept().getConceptId();
    List<Integer> values1 = Arrays.asList(hivMetadata.getPatientFoundYesConcept().getConceptId());

    int encounterType2 = hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId();
    int concept2 = tbMetadata.getObservationTB().getConceptId();
    List<Integer> values2 =
        Arrays.asList(
            tbMetadata.getFeverLastingMoraThan3Weeks().getConceptId(),
            tbMetadata.getWeightLossOfMoreThan3KgInLastMonth().getConceptId(),
            tbMetadata.getNightsWeatsLastingMoraThan3Weeks().getConceptId(),
            tbMetadata.getCoughLastingMoraThan3Weeks().getConceptId(),
            tbMetadata.getAsthenia().getConceptId(),
            tbMetadata.getCohabitantBeingTreatedForTB().getConceptId(),
            tbMetadata.getLymphadenopathy().getConceptId());

    EptsQueriesUtil queriesUtil = new EptsQueriesUtil();

    return queriesUtil
        .unionBuilder(
            QualityImprovement2020Queries.getPatientsWithConsulationObservationsAndEarliestDate(
                encounterType1, concept1, values1, true))
        .union(
            QualityImprovement2020Queries.getPatientsWithConsulationObservationsAndEarliestDate(
                encounterType2, concept2, values2, true))
        .buildQuery();
  }
  /**
   * <b>Utentes Presuntivos de TB</b>
   *
   * <p>O sistema irá identificar utentes <b>presuntivos de TB</b> seleccionando:
   *
   * <ul>
   *   <li>todos os utentes com registo de “Tem sintomas?” (TB) = “Sim” em uma consulta clínica
   *       (Ficha Clínica) ocorrida durante o período de revisão; ou
   *   <li>>todos os utentes com registo de algum sintoma FESTAC em uma consulta clínica (Ficha
   *       Clínica) ocorrida durante o período de revisão;
   *       <p>Nota 1: A “Data Presuntivo de TB” do utente é a data da consulta clínica (Ficha
   *       clínica) com registo da primeira ocorrência (algum dos sintomas FESTAC) durante o período
   *       de revisão dos critérios acima definidos.
   *       <p>Nota 2: Os sintomas FESTAC incluem (Febre- F, Emagrecimento – E, Sudorese Noturna –S,
   *       Tosse a mais de 2 semanas –T, Astenia –A, Contacto com TB- C e Adenopatia Cervical
   *       Indolor)
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getUtentesPresuntivosDeTb() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Utentes Presuntivos de TB");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    String query =
        "SELECT patient_id " + "FROM   (" + getUnionQueryUtentesPresuntivos() + ") presuntivo_tb";

    sqlCohortDefinition.setQuery((query));

    System.out.println(query);

    return sqlCohortDefinition;
  }

  /**
   * <b>Utentes com Pedido de Xpert</b>
   *
   * <p>O sistema irá identificar utentes com registo de <b>Pedido de teste Xpert</b> durante o
   * período de revisão, selecionando:
   *
   * <ul>
   *   <li>todos os utentes com registo do <b>“Pedido de Xpert”</b> em uma <b>consulta clínica
   *       (Ficha Clínica) –</b> secção investigações pedidos laboratoriais, ocorrida durante o
   *       período de revisão (>= “Data Início Revisão” e <= “Data Fim Revisão”).
   *       <p>Nota 1: A “Data Presuntivo de TB” do utente é a data da consulta clínica (Ficha
   *       clínica) com registo da primeira ocorrência (algum dos sintomas FESTAC) durante o período
   *       de revisão dos critérios acima definidos.
   *       <p><b>Nota 1:</b> em caso de existência de mais de uma consulta clínica com registo de
   *       pedido de teste Xpert, o sistema irá considerar a <b>primeira</b> ocorrência durante o
   *       período de revisão como <b>“Data Pedido de Xpert”.</b>
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public static CohortDefinition getUtentesComPedidoDeXpert() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Utentes com Pedido de Xpert");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    HivMetadata hivMetadata = new HivMetadata();
    TbMetadata tbMetadata = new TbMetadata();

    int encounterType = hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId();
    int concept = hivMetadata.getApplicationForLaboratoryResearch().getConceptId();
    List<Integer> values = Arrays.asList(tbMetadata.getTBGenexpertTestConcept().getConceptId());

    String query =
        "SELECT patient_id "
            + "FROM   ( "
            + QualityImprovement2020Queries.getPatientsWithConsulationObservationsAndEarliestDate(
                encounterType, concept, values, true)
            + " ) pedidoXpert";

    sqlCohortDefinition.setQuery((query));
    return sqlCohortDefinition;
  }

  /**
   * O sistema irá identificar os utentes que tiveram registo do resultado Xpert dentro de 7 dias do
   * pedido de Xpert, selecionado:
   *
   * <ul>
   *   <li>os utentes que tiveram registo do “Resultado de Xpert” na consulta clínica (Ficha
   *       Clínica) – secção investigações resultados laboratoriais, ocorrida durante o período de
   *       revisão, sete (7) dias após o registo do “Pedido de Xpert” na consulta clínica (Ficha
   *       Clínica) – secção investigações pedidos laboratoriais, ocorrida durante o período de
   *       revisão. Ou seja, “Data Resultado Xpert” menos (-) a “Data Pedido Xpert” >=0 e <=7 dias.
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getUtentesComResultadoDeXpertEm7Dias(boolean sevenDays) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Utentes com Resultado de Xpert dentro de 7 dias após Pedido");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    HivMetadata hivMetadata = new HivMetadata();
    TbMetadata tbMetadata = new TbMetadata();

    int encounterType = hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId();
    int concept = hivMetadata.getApplicationForLaboratoryResearch().getConceptId();
    List<Integer> values = Arrays.asList(tbMetadata.getTBGenexpertTestConcept().getConceptId());

    int encounterType2 = hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId();
    int concept2 = tbMetadata.getTBGenexpertTestConcept().getConceptId();
    List<Integer> values2 =
        Arrays.asList(
            tbMetadata.getPositiveConcept().getConceptId(),
            tbMetadata.getNegativeConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN ( "
            + QualityImprovement2020Queries.getPatientsWithConsulationObservationsAndEarliestDate(
                encounterType, concept, values, true)
            + "                   ) pedidoXpert "
            + "               ON pedidoXpert.patient_id = p.patient_id "
            + "       INNER JOIN ( "
            + QualityImprovement2020Queries.getPatientsWithConsulationObservationsAndEarliestDate(
                encounterType2, concept2, values2, true)
            + "                   ) resultadoXpert "
            + "               ON resultadoXpert.patient_id = p.patient_id ";
    query +=
        sevenDays
            ? "WHERE  resultadoXpert.the_date >= "
                + "       pedidoXpert.the_date "
                + "       AND Timestampdiff(day, pedidoXpert.the_date, "
                + "               resultadoXpert.the_date) <= 7"
            : "WHERE  resultadoXpert.the_date >= " + "       pedidoXpert.the_date ";

    sqlCohortDefinition.setQuery((query));

    return sqlCohortDefinition;
  }

  /**
   * O sistema irá identificar os utentes que tiveram registo do resultado Xpert dentro de 7 dias do
   * pedido de Xpert, selecionado:
   *
   * <ul>
   *   <li>os utentes que tiveram registo do “Resultado de Xpert” na consulta clínica (Ficha
   *       Clínica) – secção investigações resultados laboratoriais, ocorrida durante o período de
   *       revisão, sete (7) dias após o registo do “Pedido de Xpert” na consulta clínica (Ficha
   *       Clínica) – secção investigações pedidos laboratoriais, ocorrida durante o período de
   *       revisão. Ou seja, “Data Resultado Xpert” menos (-) a “Data Pedido Xpert” >=0 e <=7 dias.
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getUtentesComResultadoDeXpertEm7DiasAfterDataPresuntivo(
      boolean sevenDays) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Utentes com Resultado de Xpert dentro de 7 dias após Pedido");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    HivMetadata hivMetadata = new HivMetadata();
    TbMetadata tbMetadata = new TbMetadata();

    int encounterType = hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId();
    int concept = hivMetadata.getApplicationForLaboratoryResearch().getConceptId();
    List<Integer> values = Arrays.asList(tbMetadata.getTBGenexpertTestConcept().getConceptId());

    int encounterType2 = hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId();
    int concept2 = tbMetadata.getTBGenexpertTestConcept().getConceptId();
    List<Integer> values2 =
        Arrays.asList(
            tbMetadata.getPositiveConcept().getConceptId(),
            tbMetadata.getNegativeConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN ( "
            + getUnionQueryUtentesPresuntivos()
            + "                   ) pedidoXpert "
            + "               ON pedidoXpert.patient_id = p.patient_id "
            + "       INNER JOIN ( "
            + QualityImprovement2020Queries.getPatientsWithConsulationObservationsAndEarliestDate(
                encounterType2, concept2, values2, true)
            + "                   ) resultadoXpert "
            + "               ON resultadoXpert.patient_id = p.patient_id ";
    query +=
        sevenDays
            ? "WHERE  resultadoXpert.the_date >= "
                + "       pedidoXpert.the_date "
                + "       AND Timestampdiff(day, pedidoXpert.the_date, "
                + "               resultadoXpert.the_date) <= 7"
            : "WHERE  resultadoXpert.the_date >= " + "       pedidoXpert.the_date ";

    sqlCohortDefinition.setQuery((query));
    return sqlCohortDefinition;
  }

  /**
   *
   *
   * <ul>
   *   <li><b>Filtrando</b> os que tiveram registo do <b>Pedido de Xpert</b> na <b>consulta clínica
   *       (Ficha Clínica) - </b> secção investigações pedidos laboratoriais, ocorrida na <b>Data
   *       Presuntivo de TB</b>.
   *       <p><b>Nota:</b> a data da consulta clínica com o pedido de teste Xpert, <b>“Data Pedido
   *       Xpert”</b> menos a <b>"Data Presuntivo de TB"</b> deve ser igual a <b>zero.</b> ocorrida
   *       durante o período de revisão (>= “Data Início Revisão” e <= “Data Fim Revisão”).
   * </ul>
   *
   * <p><b>Nota 1:</b> A <b>“Data Presuntivo de TB”</b> do utente é definida no <b>RF8</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getUtentesComPedidoXpertNaDataPresuntivoDeTB() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Utentes com Pedido de Xpert na Data Presuntivo de TB");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    HivMetadata hivMetadata = new HivMetadata();
    TbMetadata tbMetadata = new TbMetadata();

    int encounterType = hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId();
    int concept = hivMetadata.getApplicationForLaboratoryResearch().getConceptId();
    List<Integer> values = Arrays.asList(tbMetadata.getTBGenexpertTestConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN ( "
            + QualityImprovement2020Queries.getPatientsWithConsulationObservationsAndEarliestDate(
                encounterType, concept, values, true)
            + "                   ) pedidoXpert "
            + "               ON pedidoXpert.patient_id = p.patient_id "
            + "       INNER JOIN ( "
            + getUnionQueryUtentesPresuntivos()
            + "                   ) presuntivosTb "
            + "               ON presuntivosTb.patient_id = p.patient_id "
            + "WHERE  presuntivosTb.the_date = "
            + "       pedidoXpert.the_date "
            + "       AND Timestampdiff(day, presuntivosTb.the_date, "
            + "               pedidoXpert.the_date) = 0";

    sqlCohortDefinition.setQuery((query));

    return sqlCohortDefinition;
  }

  /**
   * <b>Utentes com Diagnóstico TB Activo</b>
   *
   * <p>O sistema irá identificar utentes com registo de <b>Diagnóstico de TB Activo</b> durante o
   * período de revisão, seleccionando:
   *
   * <ul>
   *   <li>todos os utentes com registo de <b>“Diagnóstico de TB Activo” = "Sim"</b> em uma consulta
   *       clínica (Ficha Clínica) ocorrida durante o período de revisão (>= “Data Início Revisão” e
   *       <= “Data Fim Revisão”);
   * </ul>
   *
   * <p><b>Nota 1:</b> em caso de existência de mais de uma consulta clínica com registo de
   * diagnóstico de tb activo, o sistema irá considerar a <b>primeira</b> ocorrência durante o
   * período de revisão como <b>“Data Diagnóstico de TB”</b>.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getUtentesComDiagnosticoTbActivo() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Utentes com Diagnóstico TB Activo");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    HivMetadata hivMetadata = new HivMetadata();
    TbMetadata tbMetadata = new TbMetadata();

    int encounterType = hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId();
    int concept = tbMetadata.getActiveTBConcept().getConceptId();
    List<Integer> values = Arrays.asList(hivMetadata.getYesConcept().getConceptId());

    String query =
        "SELECT patient_id "
            + "FROM   ( "
            + QualityImprovement2020Queries.getPatientsWithConsulationObservationsAndEarliestDate(
                encounterType, concept, values, true)
            + " ) tbActivo";

    sqlCohortDefinition.setQuery((query));

    return sqlCohortDefinition;
  }

  /**
   *
   *
   * <ul>
   *   <li><b>Filtrando</b> os que tiveram registo do <b>“Tratamento de TB” = “Início”</b> em uma
   *       <b>consulta clínica (Ficha Clínica)</b> ocorrida durante o período de revisão, sendo a
   *       respectiva <b>“Data Início Tratamento TB”</b>. igual a <b>“Data Diagnóstico de TB”</b> ou
   *       seja <b>“Data Início Tratamento TB”</b> menos (-) a <b>“Data Diagnóstico de TB”</b> igual
   *       a <b>zero</b>.
   * </ul>
   *
   * <p><b>Nota 1:</b> A <b>“Data Diagnóstico de TB”</b> do utente é definida no <b>RF8.2</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getUtentesComInicioDeTratamentoDeTbNaDataDeDiagnosticoTb() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Utentes com Pedido de Xpert na Data Presuntivo de TB");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    HivMetadata hivMetadata = new HivMetadata();
    TbMetadata tbMetadata = new TbMetadata();

    int encounterType = hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId();
    int concept = tbMetadata.getActiveTBConcept().getConceptId();
    List<Integer> values = Arrays.asList(hivMetadata.getYesConcept().getConceptId());

    int encounterType2 = hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId();
    int concept2 = hivMetadata.getTBTreatmentPlanConcept().getConceptId();
    List<Integer> values2 = Arrays.asList(hivMetadata.getStartDrugs().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN ( "
            + QualityImprovement2020Queries.getPatientsWithConsulationObservationsAndEarliestDate(
                encounterType, concept, values, true)
            + "                   ) diagnosticoTb "
            + "               ON diagnosticoTb.patient_id = p.patient_id "
            + "       INNER JOIN ( "
            + QualityImprovement2020Queries.getPatientsWithConsulationObservationsAndEarliestDate(
                encounterType2, concept2, values2, false)
            + "                   ) tratamentoTb "
            + "               ON tratamentoTb.patient_id = p.patient_id "
            + "WHERE  diagnosticoTb.the_date = "
            + "       tratamentoTb.the_date "
            + "       AND Timestampdiff(day, diagnosticoTb.the_date, "
            + "               tratamentoTb.the_date ) = 0";

    sqlCohortDefinition.setQuery((query));

    return sqlCohortDefinition;
  }

  /**
   * <b>MQ19</b>: Melhoria de Qualidade Category 19 <br>
   * <i> DENOMINATOR 1: (presuntivosTb AND age) NOT transferredOut</i> <br>
   * <i> DENOMINATOR 2: (resultadoXpert AND age) NOT transferredOut</i> <br>
   * <i> DENOMINATOR 3: (diagnosticoTbActivo AND age) NOT transferredOut</i> <br>
   * <i> DENOMINATOR 4: (presuntivosTb AND age) NOT transferredOut</i> <br>
   * <i> DENOMINATOR 5: (resultadoXpert AND age) NOT transferredOut</i> <br>
   * <i> DENOMINATOR 6: (diagnosticoTbActivo AND age) NOT transferredOut</i> <br>
   *
   * @param den indicator number
   */
  public CohortDefinition getMQ19A(Integer den, MIMQ reportSource) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    if (den == 1) {
      compositionCohortDefinition.setName("Categoria 19 Denominador – Pedido XPert  Adulto");
    } else if (den == 2) {
      compositionCohortDefinition.setName("Categoria 19 Denominador – Resultado XPert Adulto");
    } else if (den == 3) {
      compositionCohortDefinition.setName("Categoria 19 Denominador – Tratamento TB - Adulto");
    } else if (den == 4) {
      compositionCohortDefinition.setName("Categoria 19 Denominador – Pedido XPert Pediátrico");
    } else if (den == 5) {
      compositionCohortDefinition.setName("Categoria 19 Denominador – Resultado XPert  Pediátrico");
    } else if (den == 6) {
      compositionCohortDefinition.setName("Categoria 19 Denominador – Tratamento TB - Pediátrico");
    }

    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    if (reportSource.equals(MIMQ.MQ)) {

      if (den == 1) {
        compositionCohortDefinition.addSearch(
            "age",
            EptsReportUtils.map(genericCohortQueries.getAgeOnPresuntivoTbDate(15, 200), MAPPING3));
      } else if (den == 2) {
        compositionCohortDefinition.addSearch(
            "age",
            EptsReportUtils.map(genericCohortQueries.getAgeOnXpertRequestDate(15, 200), MAPPING3));
      } else if (den == 3) {
        compositionCohortDefinition.addSearch(
            "age",
            EptsReportUtils.map(genericCohortQueries.getAgeOnTbDiagnosisDate(15, 200), MAPPING3));
      } else if (den == 4) {
        compositionCohortDefinition.addSearch(
            "age",
            EptsReportUtils.map(genericCohortQueries.getAgeOnPresuntivoTbDate(0, 14), MAPPING3));
      } else if (den == 5) {
        compositionCohortDefinition.addSearch(
            "age",
            EptsReportUtils.map(genericCohortQueries.getAgeOnXpertRequestDate(0, 14), MAPPING3));
      } else if (den == 6) {
        compositionCohortDefinition.addSearch(
            "age",
            EptsReportUtils.map(genericCohortQueries.getAgeOnTbDiagnosisDate(0, 14), MAPPING3));
      }

      CohortDefinition transferOut =
          intensiveMonitoringCohortQueries.getTranferredOutPatientsForMI7();

      CohortDefinition presuntivosTb = getUtentesPresuntivosDeTb();

      CohortDefinition resultadoXpert = getUtentesComResultadoDeXpertEm7Dias(false);

      CohortDefinition diagnosticoTbActivo = getUtentesComDiagnosticoTbActivo();

      compositionCohortDefinition.addSearch(
          "transferredOut", EptsReportUtils.map(transferOut, MAPPING14));

      compositionCohortDefinition.addSearch(
          "presuntivosTb", EptsReportUtils.map(presuntivosTb, MAPPING3));

      compositionCohortDefinition.addSearch(
          "resultadoXpert", EptsReportUtils.map(resultadoXpert, MAPPING3));

      compositionCohortDefinition.addSearch(
          "diagnosticoTbActivo", EptsReportUtils.map(diagnosticoTbActivo, MAPPING3));
    } else if (reportSource.equals(MIMQ.MI)) {

      if (den == 1) {
        compositionCohortDefinition.addSearch(
            "age",
            EptsReportUtils.map(genericCohortQueries.getAgeOnPresuntivoTbDate(15, 200), MAPPING));
      } else if (den == 2) {
        compositionCohortDefinition.addSearch(
            "age",
            EptsReportUtils.map(genericCohortQueries.getAgeOnXpertRequestDate(15, 200), MAPPING));
      } else if (den == 3) {
        compositionCohortDefinition.addSearch(
            "age",
            EptsReportUtils.map(genericCohortQueries.getAgeOnTbDiagnosisDate(15, 200), MAPPING));
      } else if (den == 4) {
        compositionCohortDefinition.addSearch(
            "age",
            EptsReportUtils.map(genericCohortQueries.getAgeOnPresuntivoTbDate(0, 14), MAPPING));
      } else if (den == 5) {
        compositionCohortDefinition.addSearch(
            "age",
            EptsReportUtils.map(genericCohortQueries.getAgeOnXpertRequestDate(0, 14), MAPPING));
      } else if (den == 6) {
        compositionCohortDefinition.addSearch(
            "age",
            EptsReportUtils.map(genericCohortQueries.getAgeOnTbDiagnosisDate(0, 14), MAPPING));
      }

      CohortDefinition transferOut =
          intensiveMonitoringCohortQueries.getTranferredOutPatientsForMI7();

      CohortDefinition presuntivosTb = getUtentesPresuntivosDeTb();

      CohortDefinition resultadoXpert = getUtentesComResultadoDeXpertEm7Dias(false);

      CohortDefinition diagnosticoTbActivo = getUtentesComDiagnosticoTbActivo();

      compositionCohortDefinition.addSearch(
          "transferredOut", EptsReportUtils.map(transferOut, MAPPING14));

      compositionCohortDefinition.addSearch(
          "presuntivosTb", EptsReportUtils.map(presuntivosTb, MAPPING));

      compositionCohortDefinition.addSearch(
          "resultadoXpert", EptsReportUtils.map(resultadoXpert, MAPPING));

      compositionCohortDefinition.addSearch(
          "diagnosticoTbActivo", EptsReportUtils.map(diagnosticoTbActivo, MAPPING));
    }

    if (den == 1 || den == 4) {
      compositionCohortDefinition.setCompositionString(
          "(presuntivosTb AND age) NOT transferredOut");
    } else if (den == 2 || den == 5) {
      compositionCohortDefinition.setCompositionString(
          "(resultadoXpert AND age) NOT transferredOut");
    } else if (den == 3 || den == 6) {
      compositionCohortDefinition.setCompositionString(
          "(diagnosticoTbActivo AND age) NOT transferredOut");
    }

    return compositionCohortDefinition;
  }

  /**
   * <b>MQ19</b>: Melhoria de Qualidade Category 19<br>
   * <i> NUMERATOR 1: MQ19DEN1 AND pedidoXpertOnPresuntivoTb</i> <br>
   * <i> NUMERATOR 2: MQ19DEN2 AND resultadoXpertEm7Dias</i> <br>
   * <i> NUMERATOR 3: MQ19DEN3 AND tratamentoDeTbNaDataDeDiagnosticoTb</i> <br>
   * <i> NUMERATOR 4: MQ19DEN4 AND pedidoXpertOnPresuntivoTb</i> <br>
   * <i> NUMERATOR 5: MQ19DEN5 AND resultadoXpertEm7Dias</i> <br>
   * <i> NUMERATOR 6: MQ19DEN6 AND tratamentoDeTbNaDataDeDiagnosticoTb</i> <br>
   *
   * @param num indicator number
   * @return CohortDefinition
   */
  public CohortDefinition getMQ19B(Integer num, MIMQ reportSource) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    if (reportSource.equals(MIMQ.MQ)) {
      if (num == 1) {
        compositionCohortDefinition.setName("Categoria 19 Numerador – Pedido XPert Adulto");
      } else if (num == 2) {
        compositionCohortDefinition.setName("Categoria 19 Numerador – Resultado XPert Adulto");
      } else if (num == 3) {
        compositionCohortDefinition.setName("Categoria 19 Numerador – Tratamento TB Adulto");
      } else if (num == 4) {
        compositionCohortDefinition.setName("Categoria 19 Numerador – Pedido XPert Pediátrico");
      } else if (num == 5) {
        compositionCohortDefinition.setName("Categoria 19 Numerador – Resultado XPert Pediátrico");
      } else if (num == 6) {
        compositionCohortDefinition.setName("Categoria 19 Numerador – Tratamento TB - Pediátrico");
      }

      compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
      compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
      compositionCohortDefinition.addParameter(
          new Parameter("revisionEndDate", "revisionEndDate", Date.class));
      compositionCohortDefinition.addParameter(
          new Parameter("location", "location", Location.class));

      CohortDefinition mq19DenOne = getMQ19A(1, EptsReportConstants.MIMQ.MQ);
      CohortDefinition mq19DenTwo = getMQ19A(2, EptsReportConstants.MIMQ.MQ);
      CohortDefinition mq19DenThree = getMQ19A(3, EptsReportConstants.MIMQ.MQ);
      CohortDefinition mq19DenFour = getMQ19A(4, EptsReportConstants.MIMQ.MQ);
      CohortDefinition mq19DenFive = getMQ19A(5, EptsReportConstants.MIMQ.MQ);
      CohortDefinition mq19DenSix = getMQ19A(6, EptsReportConstants.MIMQ.MQ);

      CohortDefinition pedidoXpertOnPresuntivoTb = getUtentesComPedidoXpertNaDataPresuntivoDeTB();

      CohortDefinition resultadoXpertEm7Dias = getUtentesComResultadoDeXpertEm7Dias(true);

      CohortDefinition tratamentoDeTbNaDataDeDiagnosticoTb =
          getUtentesComInicioDeTratamentoDeTbNaDataDeDiagnosticoTb();

      compositionCohortDefinition.addSearch("MQ19DEN1", EptsReportUtils.map(mq19DenOne, MAPPING1));

      compositionCohortDefinition.addSearch("MQ19DEN2", EptsReportUtils.map(mq19DenTwo, MAPPING1));

      compositionCohortDefinition.addSearch(
          "MQ19DEN3", EptsReportUtils.map(mq19DenThree, MAPPING1));

      compositionCohortDefinition.addSearch("MQ19DEN4", EptsReportUtils.map(mq19DenFour, MAPPING1));

      compositionCohortDefinition.addSearch("MQ19DEN5", EptsReportUtils.map(mq19DenFive, MAPPING1));

      compositionCohortDefinition.addSearch("MQ19DEN6", EptsReportUtils.map(mq19DenSix, MAPPING1));

      compositionCohortDefinition.addSearch(
          "pedidoXpertOnPresuntivoTb", EptsReportUtils.map(pedidoXpertOnPresuntivoTb, MAPPING3));

      compositionCohortDefinition.addSearch(
          "resultadoXpertEm7Dias", EptsReportUtils.map(resultadoXpertEm7Dias, MAPPING3));

      compositionCohortDefinition.addSearch(
          "tratamentoDeTbNaDataDeDiagnosticoTb",
          EptsReportUtils.map(tratamentoDeTbNaDataDeDiagnosticoTb, MAPPING3));

    } else if (reportSource.equals(MIMQ.MI)) {

      if (num == 1) {
        compositionCohortDefinition.setName("Categoria 19 Numerador – Pedido XPert Adulto");
      } else if (num == 2) {
        compositionCohortDefinition.setName("Categoria 19 Numerador – Resultado XPert Adulto");
      } else if (num == 3) {
        compositionCohortDefinition.setName("Categoria 19 Numerador – Tratamento TB Adulto");
      } else if (num == 4) {
        compositionCohortDefinition.setName("Categoria 19 Numerador – Pedido XPert Pediátrico");
      } else if (num == 5) {
        compositionCohortDefinition.setName("Categoria 19 Numerador – Resultado XPert Pediátrico");
      } else if (num == 6) {
        compositionCohortDefinition.setName("Categoria 19 Numerador – Tratamento TB - Pediátrico");
      }

      compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
      compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
      compositionCohortDefinition.addParameter(
          new Parameter("revisionEndDate", "revisionEndDate", Date.class));
      compositionCohortDefinition.addParameter(
          new Parameter("location", "location", Location.class));

      CohortDefinition mq19DenOne = getMQ19A(1, EptsReportConstants.MIMQ.MQ);
      CohortDefinition mq19DenTwo = getMQ19A(2, EptsReportConstants.MIMQ.MQ);
      CohortDefinition mq19DenThree = getMQ19A(3, EptsReportConstants.MIMQ.MQ);
      CohortDefinition mq19DenFour = getMQ19A(4, EptsReportConstants.MIMQ.MQ);
      CohortDefinition mq19DenFive = getMQ19A(5, EptsReportConstants.MIMQ.MQ);
      CohortDefinition mq19DenSix = getMQ19A(6, EptsReportConstants.MIMQ.MQ);

      CohortDefinition pedidoXpertOnPresuntivoTb = getUtentesComPedidoXpertNaDataPresuntivoDeTB();

      CohortDefinition resultadoXpertEm7Dias =
          getUtentesComResultadoDeXpertEm7DiasAfterDataPresuntivo(true);

      CohortDefinition tratamentoDeTbNaDataDeDiagnosticoTb =
          getUtentesComInicioDeTratamentoDeTbNaDataDeDiagnosticoTb();

      compositionCohortDefinition.addSearch("MQ19DEN1", EptsReportUtils.map(mq19DenOne, MAPPING1));

      compositionCohortDefinition.addSearch("MQ19DEN2", EptsReportUtils.map(mq19DenTwo, MAPPING1));

      compositionCohortDefinition.addSearch(
          "MQ19DEN3", EptsReportUtils.map(mq19DenThree, MAPPING1));

      compositionCohortDefinition.addSearch("MQ19DEN4", EptsReportUtils.map(mq19DenFour, MAPPING1));

      compositionCohortDefinition.addSearch("MQ19DEN5", EptsReportUtils.map(mq19DenFive, MAPPING1));

      compositionCohortDefinition.addSearch("MQ19DEN6", EptsReportUtils.map(mq19DenSix, MAPPING1));

      compositionCohortDefinition.addSearch(
          "pedidoXpertOnPresuntivoTb", EptsReportUtils.map(pedidoXpertOnPresuntivoTb, MAPPING));

      compositionCohortDefinition.addSearch(
          "resultadoXpertEm7Dias", EptsReportUtils.map(resultadoXpertEm7Dias, MAPPING));

      compositionCohortDefinition.addSearch(
          "tratamentoDeTbNaDataDeDiagnosticoTb",
          EptsReportUtils.map(tratamentoDeTbNaDataDeDiagnosticoTb, MAPPING));
    }

    if (num == 1) {
      compositionCohortDefinition.setCompositionString("MQ19DEN1 AND pedidoXpertOnPresuntivoTb");
    } else if (num == 2) {
      compositionCohortDefinition.setCompositionString("MQ19DEN1 AND resultadoXpertEm7Dias");
    } else if (num == 3) {
      compositionCohortDefinition.setCompositionString(
          "MQ19DEN3 AND tratamentoDeTbNaDataDeDiagnosticoTb");
    } else if (num == 4) {
      compositionCohortDefinition.setCompositionString("MQ19DEN4 AND pedidoXpertOnPresuntivoTb");
    } else if (num == 5) {
      compositionCohortDefinition.setCompositionString("MQ19DEN4 AND resultadoXpertEm7Dias");
    } else if (num == 6) {
      compositionCohortDefinition.setCompositionString(
          "MQ19DEN6 AND tratamentoDeTbNaDataDeDiagnosticoTb");
    }

    return compositionCohortDefinition;
  }
}
