package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.eptsreports.reporting.calculation.tpt.CompletedIsoniazidTPTCalculation;
import org.openmrs.module.eptsreports.reporting.cohort.definition.CalculationCohortDefinition;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TPTEligiblePatientListCohortQueries {

  private HivMetadata hivMetadata;

  private TbMetadata tbMetadata;

  private TbPrevCohortQueries tbPrevCohortQueries;

  private TxCurrCohortQueries txCurrCohortQueries;

  private TXTBCohortQueries txTbCohortQueries;

  @Autowired
  public TPTEligiblePatientListCohortQueries(
      HivMetadata hivMetadata,
      TbMetadata tbMetadata,
      TbPrevCohortQueries tbPrevCohortQueries,
      TxCurrCohortQueries txCurrCohortQueries,
      TXTBCohortQueries txTbCohortQueries) {
    this.hivMetadata = hivMetadata;
    this.tbMetadata = tbMetadata;
    this.tbPrevCohortQueries = tbPrevCohortQueries;
    this.txCurrCohortQueries = txCurrCohortQueries;
    this.txTbCohortQueries = txTbCohortQueries;
  }

  private final String mapping = "endDate=${endDate},location=${location}";

  public CohortDefinition getPatientsThatCompletedProphylaticTreatment() {
    CalculationCohortDefinition cd =
        new CalculationCohortDefinition(
            Context.getRegisteredComponents(CompletedIsoniazidTPTCalculation.class).get(0));
    cd.setName("Patients that completed prophylatic treatment");
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
    return cd;
  }

  public CohortDefinition getTxCurrWithoutTPT() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName("TX_CURR without TPT");
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    compositionCohortDefinition.addSearch(
        "txcurr",
        EptsReportUtils.map(
            txCurrCohortQueries.getTxCurrCompositionCohort("txCurr", true),
            "onOrBefore=${endDate},location=${location},locations=${location}"));

    compositionCohortDefinition.addSearch(
        "A1",
        EptsReportUtils.map(
            getINHStartA1(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "A2",
        EptsReportUtils.map(
            getINHStartA2(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getStartDrugs().getConceptId(),
                hivMetadata.getIsoniazidUsageConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "A3",
        EptsReportUtils.map(
            getINHStartA3(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "A4",
        EptsReportUtils.map(
            getINHStartA4(
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.getIsoniazidConcept().getConceptId(),
                tbMetadata.getIsoniazidePiridoxinaConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "3HPA1",
        EptsReportUtils.map(
            get3HPStartA1(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                tbMetadata.getTreatmentPrescribedConcept().getConceptId(),
                tbMetadata.get3HPConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "3HPA2",
        EptsReportUtils.map(
            get3HPStartA2(
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.get3HPConcept().getConceptId(),
                tbMetadata.get3HPPiridoxinaConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "IPTB1",
        EptsReportUtils.map(
            getIPTB1(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                hivMetadata.getDataFinalizacaoProfilaxiaIsoniazidaConcept().getConceptId(),
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getIsoniazidUsageConcept().getConceptId(),
                hivMetadata.getStartDrugs().getConceptId(),
                hivMetadata.getCompletedConcept().getConceptId(),
                hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId(),
                hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.getIsoniazidConcept().getConceptId(),
                tbMetadata.getIsoniazidePiridoxinaConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "IPTB2",
        EptsReportUtils.map(
            getIPTB2(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getIsoniazidUsageConcept().getConceptId(),
                hivMetadata.getStartDrugs().getConceptId(),
                hivMetadata.getCompletedConcept().getConceptId(),
                hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId(),
                hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.getIsoniazidConcept().getConceptId(),
                tbMetadata.getIsoniazidePiridoxinaConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "IPTB3",
        EptsReportUtils.map(
            getIPTB3(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                hivMetadata.getDataFinalizacaoProfilaxiaIsoniazidaConcept().getConceptId(),
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getIsoniazidUsageConcept().getConceptId(),
                hivMetadata.getStartDrugs().getConceptId(),
                hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId(),
                hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.getIsoniazidConcept().getConceptId(),
                tbMetadata.getIsoniazidePiridoxinaConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "IPTB4",
        EptsReportUtils.map(
            getIPTB4(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                hivMetadata.getDataFinalizacaoProfilaxiaIsoniazidaConcept().getConceptId(),
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getIsoniazidUsageConcept().getConceptId(),
                hivMetadata.getStartDrugs().getConceptId(),
                hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId(),
                hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.getIsoniazidConcept().getConceptId(),
                tbMetadata.getIsoniazidePiridoxinaConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "IPTB5Part1",
        EptsReportUtils.map(
            getIPTB5Part1(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getIsoniazidUsageConcept().getConceptId(),
                hivMetadata.getStartDrugs().getConceptId(),
                hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId(),
                hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getContinueRegimenConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "IPTB5Part2",
        EptsReportUtils.map(
            getIPTB5Part2(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getIsoniazidUsageConcept().getConceptId(),
                hivMetadata.getStartDrugs().getConceptId(),
                hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId(),
                hivMetadata.getContinueRegimenConcept().getConceptId(),
                tbMetadata.getTreatmentPrescribedConcept().getConceptId(),
                tbMetadata.getDtINHConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "IPTBPart3",
        EptsReportUtils.map(
            getIPTB5Part3(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getIsoniazidUsageConcept().getConceptId(),
                hivMetadata.getStartDrugs().getConceptId(),
                hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId(),
                hivMetadata.getContinueRegimenConcept().getConceptId(),
                tbMetadata.getTreatmentPrescribedConcept().getConceptId(),
                tbMetadata.getDtINHConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "IPTB6Part1",
        EptsReportUtils.map(
            getIPTB6Part1(
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.getIsoniazidConcept().getConceptId(),
                tbMetadata.getIsoniazidePiridoxinaConcept().getConceptId(),
                hivMetadata.getMonthlyConcept().getConceptId(),
                tbMetadata.getTypeDispensationTPTConceptUuid().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "IPTB6Part2",
        EptsReportUtils.map(
            getIPTB6Part2(
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.getIsoniazidConcept().getConceptId(),
                tbMetadata.getIsoniazidePiridoxinaConcept().getConceptId(),
                tbMetadata.getTypeDispensationTPTConceptUuid().getConceptId(),
                hivMetadata.getQuarterlyConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "IPTB6Part3",
        EptsReportUtils.map(
            getIPTB6Part3(
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.getIsoniazidConcept().getConceptId(),
                tbMetadata.getIsoniazidePiridoxinaConcept().getConceptId(),
                hivMetadata.getMonthlyConcept().getConceptId(),
                tbMetadata.getTypeDispensationTPTConceptUuid().getConceptId(),
                hivMetadata.getQuarterlyConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "3HPC1",
        EptsReportUtils.map(
            get3HPC1(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                tbMetadata.get3HPConcept().getConceptId(),
                tbMetadata.getTreatmentPrescribedConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "3HPC2",
        EptsReportUtils.map(
            get3HPC2(
                tbMetadata.get3HPConcept().getConceptId(),
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.get3HPPiridoxinaConcept().getConceptId(),
                tbMetadata.getTypeDispensationTPTConceptUuid().getConceptId(),
                hivMetadata.getQuarterlyConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "3HPC3",
        EptsReportUtils.map(
            get3HPC3(
                tbMetadata.get3HPConcept().getConceptId(),
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.get3HPPiridoxinaConcept().getConceptId(),
                tbMetadata.getTypeDispensationTPTConceptUuid().getConceptId(),
                hivMetadata.getMonthlyConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "TBTreatmentPart1",
        EptsReportUtils.map(
            getTBTreatmentPart1(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getStartDrugs().getConceptId(),
                tbMetadata.getTBTreatmentPlanConcept().getConceptId(),
                tbMetadata.getTBDrugTreatmentStartDate().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "getTBTreatmentPart2",
        EptsReportUtils.map(
            getTBTreatmentPart2(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId(),
                tbMetadata.getTuberculosis().getConceptId()),
            mapping));

    compositionCohortDefinition.setCompositionString(
        "txcurr AND NOT (A1 OR A2 OR A3 OR A4 OR 3HPA1 OR 3HPA2 OR IPTB1 OR IPTB2 OR IPTB3 OR IPTB4 OR IPTB5Part1 OR IPTB5Part2 OR IPTBPart3 OR IPTB6Part1 OR IPTB6Part3 OR 3HPC1 OR 3HPC2 OR 3HPC3 OR TBTreatmentPart1)");

    return compositionCohortDefinition;
  }

  /**
   * <b>IMER1</b>:User Story TPT Eligible Patient List <br>
   *
   * <ul>
   *   <li>A1:Select all patients with Ficha Resumo (encounter type 53) with “Ultima profilaxia
   *       Isoniazida (Data Inicio)” (concept id 6128) and value datetime not null and between
   *       endDate-7months (210 DAYs) and endDate. or
   *   <li>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getINHStartA1(
      int masterCardEncounterType, int dataInicioProfilaxiaIsoniazidaConcept) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with Ultima profilaxia Isoniazida (Data Inicio)");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", masterCardEncounterType);
    map.put("6128", dataInicioProfilaxiaIsoniazidaConcept);

    String query =
        " SELECT p.patient_id"
            + " FROM"
            + " patient p"
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + " INNER JOIN obs o ON e.encounter_id = o.encounter_id"
            + " WHERE"
            + " p.voided = 0"
            + " AND e.voided = 0"
            + " AND o.voided = 0"
            + " e.encounter_type = ${53} "
            + " AND o.value_datetime IS NOT NULL"
            + " AND o.concept_id = ${6128}"
            + " AND e.location_id = :location"
            + " AND o.value_datetime BETWEEN DATE_SUB(:endDate, INTERVAL 210 DAY) AND :endDate";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>IMER1</b>:User Story TPT Eligible Patient List <br>
   *
   * <ul>
   *   <li>A2: Select all patients with Ficha clinica (encounter type 6) with “Profilaxia INH”
   *       (concept id 6122) with value code “Inicio” (concept id 1256) and encounter datetime
   *       between endDate-7months (210 DAYs) and endDate. or
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getINHStartA2(
      int adultoSeguimentoEncounterType, int startDrugsConcept, int isoniazidUsageConcept) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName(" all patients with Profilaxia INH");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", adultoSeguimentoEncounterType);
    map.put("6122", isoniazidUsageConcept);
    map.put("1256", startDrugsConcept);

    String query =
        "SELECT p.patient_id FROM patient p  "
            + "          JOIN encounter e ON e.patient_id = p.patient_id "
            + "          JOIN obs o ON o.encounter_id = e.encounter_id "
            + "          WHERE e.encounter_type = ${6} AND o.concept_id = ${6122} "
            + "          AND o.voided = 0 AND e.voided = 0  "
            + "          AND p.voided = 0 AND e.location_id = :location "
            + "          AND o.value_coded = ${1256} "
            + "          AND e.encounter_datetime BETWEEN DATE_SUB(:endDate, INTERVAL 210 DAY) AND :endDate";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>IMER1</b>:User Story TPT Eligible Patient List <br>
   *
   * <ul>
   *   <li>A3: Select all patients with Ficha clinica or Ficha Pediatrica (encounter type 6,9) with
   *       “Profilaxia com INH” (concept id 6128) and value datetime is not null and
   *       betweenendDate-7months (210 DAYs) and endDate. or
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getINHStartA3(
      int pediatriaSeguimentoEncounterType,
      int dataInicioProfilaxiaIsoniazidaConcept,
      int adultoSeguimentoEncounterType) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with Ficha clinica or Ficha Pediatrica A3");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", adultoSeguimentoEncounterType);
    map.put("9", pediatriaSeguimentoEncounterType);
    map.put("6128", dataInicioProfilaxiaIsoniazidaConcept);

    String query =
        "SELECT p.patient_id FROM patient p  "
            + "          JOIN encounter e ON e.patient_id = p.patient_id "
            + "          JOIN obs o ON o.encounter_id = e.encounter_id "
            + "          WHERE e.encounter_type IN (${6}, ${9}) AND o.concept_id = ${6128} "
            + "          AND o.voided = 0 AND e.voided = 0  "
            + "          AND p.voided = 0 AND e.location_id = :location "
            + "          AND o.value_datetime IS NOT NULL "
            + "          AND o.value_datetime BETWEEN DATE_SUB(:endDate, INTERVAL 210 DAY) AND :endDate";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>IMER1</b>:User Story TPT Eligible Patient List <br>
   *
   * <ul>
   *   <li>A4: Select all patients with FILT (encounter type 60) with “Regime de TPT” (concept id
   *       23985) value coded ‘Isoniazid’ or ‘Isoniazid + piridoxina’ (concept id in [656, 23982])
   *       and encounter datetime between endDate-7months (210 DAYs) and endDate and no other INH
   *       values (“Isoniazida” or “Isoniazida + Piridoxina”) marked on FILT in the 210 DAYs prior
   *       to the INH Start Date; OR
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getINHStartA4(
      int regimeTPTEncounterType,
      int regimeTPTConcept,
      int isoniazidConcept,
      int isoniazidePiridoxinaConcept) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName(" all patients with Regime de TPT A4");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("60", regimeTPTEncounterType);
    map.put("23985", regimeTPTConcept);
    map.put("656", isoniazidConcept);
    map.put("23982", isoniazidePiridoxinaConcept);

    String query =
        "SELECT p.patient_id "
            + "          FROM  patient p   "
            + "          INNER JOIN encounter e ON e.patient_id = p.patient_id   "
            + "          INNER JOIN obs o ON o.encounter_id = e.encounter_id   "
            + "          INNER JOIN (SELECT  p.patient_id, MIN(e.encounter_datetime) first_pickup_date  "
            + "       FROM    patient p   "
            + "       INNER JOIN encounter e ON e.patient_id = p.patient_id  "
            + "       INNER JOIN obs o ON o.encounter_id = e.encounter_id   "
            + "       WHERE   p.voided = 0   "
            + "           AND e.voided = 0   "
            + "           AND o.voided = 0   "
            + "           AND e.location_id = :location  "
            + "           AND e.encounter_type = ${60} "
            + "           AND o.concept_id = ${23985} "
            + "           AND o.value_coded IN (${656},${23982})  "
            + "           AND e.encounter_datetime BETWEEN DATE_SUB(:endDate, INTERVAL 210 DAY) AND :endDate"
            + "       GROUP BY p.patient_id) AS pickup  "
            + "       ON pickup.patient_id = p.patient_id "
            + "          WHERE p.patient_id NOT IN ( SELECT pp.patient_id "
            + "        FROM patient pp  "
            + "             INNER JOIN encounter ee ON ee.patient_id = pp.patient_id  "
            + "             INNER JOIN obs oo ON oo.encounter_id = ee.encounter_id "
            + "        WHERE pp.voided = 0   "
            + "             AND p.patient_id = pp.patient_id "
            + "             AND ee.voided = 0   "
            + "             AND oo.voided = 0   "
            + "             AND ee.location_id = :location  "
            + "             AND ee.encounter_type = ${60} "
            + "             AND oo.concept_id = ${23985} "
            + "             AND oo.value_coded IN (${656}, ${23982})  "
            + "             AND ee.encounter_datetime >= DATE_SUB(pickup.first_pickup_date, INTERVAL 210 DAY)"
            + "             AND ee.encounter_datetime < pickup.first_pickup_date) "
            + " GROUP BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition get3HPStartA1(
      int adultoSeguimentoEncounterType, int treatmentPrescribedConcept, int threeHPConcept) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with Outras Prescricoes A1");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", adultoSeguimentoEncounterType);
    map.put("1719", treatmentPrescribedConcept);
    map.put("23954", threeHPConcept);

    String query =
        "           SELECT p.patient_id "
            + "         FROM  patient p   "
            + "         INNER JOIN encounter e ON e.patient_id = p.patient_id   "
            + "         INNER JOIN obs o ON o.encounter_id = e.encounter_id   "
            + "         INNER JOIN (SELECT  p.patient_id, MIN(e.encounter_datetime) first_pickup_date  "
            + "      FROM    patient p   "
            + "      INNER JOIN encounter e ON e.patient_id = p.patient_id  "
            + "      INNER JOIN obs o ON o.encounter_id = e.encounter_id   "
            + "      WHERE   p.voided = 0   "
            + "          AND e.voided = 0   "
            + "          AND o.voided = 0   "
            + "          AND e.location_id = :location  "
            + "          AND e.encounter_type = ${6} "
            + "          AND o.concept_id = ${1719} "
            + "          AND o.value_coded IN (${23954})  "
            + "          AND e.encounter_datetime BETWEEN DATE_SUB(:endDate, INTERVAL 120 DAY) AND :endDate"
            + "      GROUP BY p.patient_id) AS pickup  "
            + "      ON pickup.patient_id = p.patient_id "
            + "         WHERE p.patient_id NOT IN ( SELECT pp.patient_id   "
            + "       FROM patient pp  "
            + "             INNER JOIN encounter ee ON ee.patient_id = pp.patient_id  "
            + "             INNER JOIN obs oo ON oo.encounter_id = ee.encounter_id "
            + "       WHERE pp.voided = 0   "
            + "             AND p.patient_id = pp.patient_id "
            + "            AND ee.voided = 0   "
            + "            AND oo.voided = 0   "
            + "            AND ee.location_id = :location  "
            + "            AND ee.encounter_type = ${6} "
            + "            AND oo.concept_id = ${1719} "
            + "            AND oo.value_coded IN (${23954})  "
            + "            AND ee.encounter_datetime >= DATE_SUB(pickup.first_pickup_date, INTERVAL 120 DAY)   "
            + "            AND ee.encounter_datetime < pickup.first_pickup_date)  ";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition get3HPStartA2(
      int regimeTPTEncounterType,
      int regimeTPTConcept,
      int threeHPConcept,
      int hPPiridoxinaConcept) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with Outras Prescricoes A2");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("60", regimeTPTEncounterType);
    map.put("23985", regimeTPTConcept);
    map.put("23954", threeHPConcept);
    map.put("23984", hPPiridoxinaConcept);

    String query =
        "           SELECT p.patient_id "
            + "         FROM  patient p   "
            + "         INNER JOIN encounter e ON e.patient_id = p.patient_id   "
            + "         INNER JOIN obs o ON o.encounter_id = e.encounter_id   "
            + "         INNER JOIN (SELECT  p.patient_id, MIN(e.encounter_datetime) first_pickup_date  "
            + "      FROM    patient p   "
            + "      INNER JOIN encounter e ON e.patient_id = p.patient_id  "
            + "      INNER JOIN obs o ON o.encounter_id = e.encounter_id   "
            + "      WHERE   p.voided = 0   "
            + "          AND e.voided = 0   "
            + "          AND o.voided = 0   "
            + "          AND e.location_id = :location  "
            + "          AND e.encounter_type = ${60} "
            + "          AND o.concept_id = ${23985} "
            + "          AND o.value_coded IN (${23954},${23984})  "
            + "          AND e.encounter_datetime BETWEEN DATE_SUB(:endDate, INTERVAL 120 DAY) AND :endDate"
            + "      GROUP BY p.patient_id) AS pickup  "
            + "      ON pickup.patient_id = p.patient_id "
            + "         WHERE p.patient_id NOT IN ( SELECT pp.patient_id   "
            + "       FROM patient pp  "
            + "             INNER JOIN encounter ee ON ee.patient_id = pp.patient_id  "
            + "             INNER JOIN obs oo ON oo.encounter_id = ee.encounter_id "
            + "       WHERE pp.voided = 0   "
            + "             AND p.patient_id = pp.patient_id "
            + "            AND ee.voided = 0   "
            + "            AND oo.voided = 0   "
            + "            AND ee.location_id = :location  "
            + "            AND ee.encounter_type = ${60} "
            + "            AND oo.concept_id = ${23985} "
            + "            AND oo.value_coded IN (${23954}, ${23984})  "
            + "            AND ee.encounter_datetime >= DATE_SUB(pickup.first_pickup_date, INTERVAL 120 DAY)   "
            + "            AND ee.encounter_datetime < pickup.first_pickup_date)  ";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getIPTB1(
      int masterCardEncounterType,
      int dataFinalizacaoProfilaxiaIsoniazidaConcept,
      int adultoSeguimentoEncounterType,
      int isoniazidUsageConcept,
      int startDrugsConcept,
      int completedConcept,
      int dataInicioProfilaxiaIsoniazidaConcept,
      int pediatriaSeguimentoEncounterType,
      int regimeTPTEncounterType,
      int regimeTPTConcept,
      int isoniazidConcept,
      int isoniazidePiridoxinaConcept) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with Ultima profilaxia Isoniazida (Data Fim) B1");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", masterCardEncounterType);
    map.put("6", adultoSeguimentoEncounterType);
    map.put("9", pediatriaSeguimentoEncounterType);
    map.put("6128", dataInicioProfilaxiaIsoniazidaConcept);
    map.put("6129", dataFinalizacaoProfilaxiaIsoniazidaConcept);
    map.put("6122", isoniazidUsageConcept);
    map.put("1256", startDrugsConcept);
    map.put("1267", completedConcept);
    map.put("60", regimeTPTEncounterType);
    map.put("23985", regimeTPTConcept);
    map.put("656", isoniazidConcept);
    map.put("23982", isoniazidePiridoxinaConcept);

    String query =
        "SELECT "
            + "                p.patient_id "
            + "            FROM "
            + "                patient p "
            + "                 INNER JOIN"
            + "             encounter e ON e.patient_id = p.patient_id"
            + "                 INNER JOIN"
            + "             obs o ON o.encounter_id = e.encounter_id"
            + "         WHERE"
            + "             p.voided = 0 AND e.voided = 0"
            + "                 AND o.voided = 0"
            + "                 AND e.location_id = :location"
            + "                 AND e.encounter_type = ${6}"
            + "                 AND o.concept_id = ${6122}"
            + "                 AND o.value_coded = ${1267}"
            + "                 AND e.encounter_datetime BETWEEN DATE_ADD(CAST((SELECT "
            + "                             oo.value_datetime"
            + "                         FROM"
            + "                             encounter ee"
            + "                                 JOIN"
            + "                             obs oo ON oo.encounter_id = ee.encounter_id"
            + "                         WHERE"
            + "                             ee.encounter_type = ${53}"
            + "                                 AND oo.concept_id = ${6128}"
            + "                                 AND p.patient_id = ee.patient_id"
            + "                                 AND oo.voided = 0"
            + "                                 AND ee.voided = 0"
            + "                                 AND ee.location_id = :location"
            + "                                 AND oo.value_datetime IS NOT NULL"
            + "                                 AND oo.value_datetime <= :endDate"
            + "                         LIMIT 1)"
            + "                     AS DATE),"
            + "                 INTERVAL 173 DAY) AND DATE_ADD(CAST((SELECT "
            + "                                 oo.value_datetime"
            + "                             FROM"
            + "                                 encounter ee"
            + "                                     JOIN"
            + "                                 obs oo ON oo.encounter_id = ee.encounter_id"
            + "                             WHERE"
            + "                                 ee.encounter_type = ${53}"
            + "                                     AND oo.concept_id = ${6128}"
            + "                                     AND p.patient_id = ee.patient_id"
            + "                                     AND oo.voided = 0"
            + "                                     AND ee.voided = 0"
            + "                                     AND ee.location_id = :location"
            + "                                     AND oo.value_datetime IS NOT NULL"
            + "                                     AND oo.value_datetime <= :endDate"
            + "                             LIMIT 1) AS DATE),"
            + "                 INTERVAL 365 DAY)"
            + "         GROUP BY p.patient_id"
            + "     UNION "
            + "         SELECT "
            + "             p.patient_id"
            + "       FROM"
            + "              patient p "
            + "                  INNER JOIN"
            + "             encounter e ON e.patient_id = p.patient_id"
            + "                 INNER JOIN"
            + "             obs o ON o.encounter_id = e.encounter_id"
            + "      WHERE"
            + "          p.voided = 0 AND e.voided = 0"
            + "              AND o.voided = 0"
            + "                 AND e.location_id = :location"
            + "                  AND e.encounter_type = ${6}"
            + "                 AND o.concept_id = ${6122}"
            + "                 AND o.value_coded = ${1267}"
            + "                 AND e.encounter_datetime BETWEEN DATE_ADD(CAST((SELECT "
            + "                             ee.encounter_datetime"
            + "                         FROM"
            + "                             encounter ee"
            + "                                 JOIN"
            + "                             obs oo ON oo.encounter_id = ee.encounter_id"
            + "                         WHERE"
            + "                             ee.encounter_type = ${6}"
            + "                                 AND oo.concept_id = ${6122}"
            + "                                 AND oo.voided = 0"
            + "                                 AND ee.voided = 0"
            + "                                 AND p.patient_id = ee.patient_id"
            + "                                 AND ee.location_id = :location"
            + "                                 AND oo.value_coded = ${1256}"
            + "                                 AND ee.encounter_datetime <= :endDate"
            + "                         LIMIT 1)"
            + "                     AS DATE),"
            + "                 INTERVAL 173 DAY) AND DATE_ADD(CAST((SELECT "
            + "                                 ee.encounter_datetime"
            + "                             FROM"
            + "                                 encounter ee"
            + "                                     JOIN"
            + "                                 obs oo ON oo.encounter_id = ee.encounter_id"
            + "                             WHERE"
            + "                                 ee.encounter_type = ${6}"
            + "                                     AND oo.concept_id = ${6122}"
            + "                                     AND oo.voided = 0"
            + "                                     AND ee.voided = 0"
            + "                                     AND p.patient_id = ee.patient_id"
            + "                                     AND ee.location_id = :location"
            + "                                     AND oo.value_coded = ${1256}"
            + "                                     AND ee.encounter_datetime <= :endDate"
            + "                             LIMIT 1) AS DATE),"
            + "                 INTERVAL 365 DAY)"
            + "         GROUP BY p.patient_id"
            + "     UNION "
            + "         SELECT "
            + "             p.patient_id"
            + "         FROM"
            + "             patient p"
            + "                 INNER JOIN"
            + "             encounter e ON e.patient_id = p.patient_id"
            + "                 INNER JOIN"
            + "             obs o ON o.encounter_id = e.encounter_id"
            + "         WHERE"
            + "             p.voided = 0 AND e.voided = 0"
            + "                 AND o.voided = 0"
            + "                 AND e.location_id = :location"
            + "                  AND e.encounter_type = ${6}"
            + "                 AND o.concept_id = ${6122}"
            + "                 AND o.value_coded = ${1267}"
            + "                 AND e.encounter_datetime BETWEEN DATE_ADD(CAST((SELECT "
            + "                             ee.encounter_datetime"
            + "                         FROM"
            + "                             encounter ee"
            + "                                 INNER JOIN"
            + "                             obs oo ON ee.encounter_id = oo.encounter_id"
            + "                         WHERE"
            + "                             ee.voided = 0 AND oo.voided = 0"
            + "                                 AND p.patient_id = ee.patient_id"
            + "                                 AND ee.encounter_type IN (${6} , ${9})"
            + "                                 AND oo.concept_id = ${6128}"
            + "                                 AND ee.location_id = :location"
            + "                                 AND oo.value_datetime IS NOT NULL"
            + "                                 AND oo.value_datetime <= :endDate"
            + "                         LIMIT 1)"
            + "                     AS DATE),"
            + "                 INTERVAL 173 DAY) AND DATE_ADD(CAST((SELECT "
            + "                             ee.encounter_datetime"
            + "                         FROM"
            + "                             encounter ee"
            + "                                 INNER JOIN"
            + "                             obs oo ON ee.encounter_id = oo.encounter_id"
            + "                         WHERE"
            + "                             ee.voided = 0 AND oo.voided = 0"
            + "                                 AND p.patient_id = ee.patient_id"
            + "                                 AND ee.encounter_type IN (${6} , ${9})"
            + "                                 AND oo.concept_id = ${6128}"
            + "                                 AND ee.location_id = :location"
            + "                                 AND oo.value_datetime IS NOT NULL"
            + "                                 AND oo.value_datetime <= :endDate"
            + "                         LIMIT 1)"
            + "                     AS DATE),"
            + "                 INTERVAL 365 DAY)"
            + "         GROUP BY p.patient_id"
            + "     UNION "
            + "         SELECT "
            + "             p.patient_id"
            + "         FROM"
            + "             patient p"
            + "                 INNER JOIN"
            + "             encounter e ON e.patient_id = p.patient_id"
            + "                 INNER JOIN"
            + "             obs o ON o.encounter_id = e.encounter_id"
            + "         WHERE"
            + "             p.voided = 0 AND e.voided = 0"
            + "                 AND o.voided = 0"
            + "                 AND e.location_id = :location"
            + "                 AND e.encounter_type = ${6}"
            + "                 AND o.concept_id = ${6122}"
            + "                 AND o.value_coded = ${1267}"
            + "                 AND e.encounter_datetime BETWEEN DATE_ADD(CAST((SELECT "
            + "                             ee.encounter_datetime"
            + "                         FROM"
            + "                             encounter ee"
            + "                                 INNER JOIN"
            + "                             obs oo ON ee.encounter_id = oo.encounter_id"
            + "                         WHERE"
            + "                             ee.voided = 0 AND oo.voided = 0"
            + "                                 AND p.patient_id = ee.patient_id"
            + "                                 AND ee.encounter_type = ${60}"
            + "                                 AND oo.concept_id = ${23985}"
            + "                                 AND oo.value_coded IN (${656} , ${23982})"
            + "                                 AND ee.location_id = :location"
            + "                                 AND ee.encounter_datetime <= :endDate"
            + "                         LIMIT 1)"
            + "                     AS DATE),"
            + "                 INTERVAL 173 DAY) AND DATE_ADD(CAST((SELECT "
            + "                             ee.encounter_datetime"
            + "                         FROM"
            + "                             encounter ee"
            + "                                 INNER JOIN"
            + "                                obs oo ON ee.encounter_id = oo.encounter_id"
            + "                            WHERE"
            + "                                ee.voided = 0 AND oo.voided = 0"
            + "                                    AND p.patient_id = ee.patient_id"
            + "                                    AND ee.encounter_type = ${60}"
            + "                                    AND oo.concept_id = ${23985}"
            + "                                    AND oo.value_coded IN (${656} , ${23982})"
            + "                                    AND ee.location_id = :location"
            + "                                    AND ee.encounter_datetime <= :endDate"
            + "                            LIMIT 1)"
            + "                        AS DATE),"
            + "                    INTERVAL 365 DAY)"
            + "         GROUP BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getIPTB2(
      int masterCardEncounterType,
      int adultoSeguimentoEncounterType,
      int isoniazidUsageConcept,
      int startDrugsConcept,
      int completedConcept,
      int dataInicioProfilaxiaIsoniazidaConcept,
      int pediatriaSeguimentoEncounterType,
      int regimeTPTEncounterType,
      int regimeTPTConcept,
      int isoniazidConcept,
      int isoniazidePiridoxinaConcept) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with Profilaxia com INH B2");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", masterCardEncounterType);
    map.put("6", adultoSeguimentoEncounterType);
    map.put("9", pediatriaSeguimentoEncounterType);
    map.put("6128", dataInicioProfilaxiaIsoniazidaConcept);
    map.put("6122", isoniazidUsageConcept);
    map.put("1256", startDrugsConcept);
    map.put("1267", completedConcept);
    map.put("60", regimeTPTEncounterType);
    map.put("23985", regimeTPTConcept);
    map.put("656", isoniazidConcept);
    map.put("23982", isoniazidePiridoxinaConcept);

    String query =
        " SELECT p.patient_id"
            + "     FROM   patient p"
            + "       INNER JOIN encounter e"
            + "               ON e.patient_id = p.patient_id"
            + "       INNER JOIN obs o"
            + "               ON o.encounter_id = e.encounter_id"
            + "     WHERE  p.voided = 0"
            + "       AND e.voided = 0"
            + "       AND o.voided = 0"
            + "       AND e.location_id = :location"
            + "       AND e.encounter_type = ${6}"
            + "       AND o.concept_id = ${6122}"
            + "       AND o.value_coded = ${1267}"
            + "       AND e.encounter_datetime BETWEEN Date_add("
            + "           Cast((SELECT oo.value_datetime FROM"
            + "           encounter"
            + "           ee JOIN obs oo ON"
            + "           oo.encounter_id"
            + "                ="
            + "                ee.encounter_id WHERE ee.encounter_type ="
            + "           ${53} AND oo.concept_id = ${6128}"
            + "           AND"
            + "                p.patient_id ="
            + "                ee.patient_id AND oo.voided = 0 AND"
            + "           ee.voided = 0 AND ee.location_id ="
            + "           :location AND"
            + "                oo.value_datetime IS NOT NULL AND"
            + "           oo.value_datetime <= :endDate"
            + "           LIMIT 1) AS"
            + "                date), INTERVAL 173 DAY) AND"
            + "           Date_add("
            + "               Cast((SELECT oo.value_datetime FROM encounter ee JOIN obs oo ON"
            + "               oo.encounter_id"
            + "               ="
            + "               ee.encounter_id WHERE"
            + "               ee.encounter_type = ${53} AND oo.concept_id = ${6128}"
            + "                               AND"
            + "               p.patient_id ="
            + "               ee.patient_id AND oo.voided = 0"
            + "               AND ee.voided = 0 AND"
            + "               ee.location_id ="
            + "                     :location AND"
            + "               oo.value_datetime IS NOT NULL AND"
            + "               oo.value_datetime <= :endDate"
            + "                   LIMIT 1) AS"
            + "               date), INTERVAL 365 DAY)"
            + "     GROUP  BY p.patient_id"
            + "     UNION"
            + "     SELECT p.patient_id"
            + "     FROM   patient p"
            + "            INNER JOIN encounter e"
            + "                    ON e.patient_id = p.patient_id"
            + "            INNER JOIN obs o"
            + "                    ON o.encounter_id = e.encounter_id"
            + "     WHERE  p.voided = 0"
            + "       AND e.voided = 0"
            + "       AND o.voided = 0"
            + "       AND e.location_id = :location"
            + "       AND e.encounter_type = ${6}"
            + "       AND o.concept_id = ${6122}"
            + "       AND o.value_coded = ${1267}"
            + "       AND e.encounter_datetime BETWEEN Date_add("
            + "           Cast((SELECT ee.encounter_datetime FROM"
            + "           encounter ee JOIN obs oo ON"
            + "                oo.encounter_id ="
            + "                ee.encounter_id WHERE ee.encounter_type ="
            + "           ${6} AND oo.concept_id = ${6122}"
            + "           AND"
            + "                oo.voided = 0 AND ee.voided = 0 AND"
            + "           p.patient_id = ee.patient_id AND"
            + "                ee.location_id ="
            + "                :location AND oo.value_coded = ${1256} AND"
            + "           ee.encounter_datetime <= :endDate"
            + "           LIMIT 1)"
            + "                AS date), INTERVAL 173 DAY) AND Date_add("
            + "           Cast((SELECT ee.encounter_datetime FROM"
            + "           encounter ee JOIN obs oo ON"
            + "           oo.encounter_id ="
            + "           ee.encounter_id WHERE"
            + "           ee.encounter_type = 6 AND oo.concept_id = ${6122}"
            + "                              AND"
            + "           oo.voided = 0 AND ee.voided ="
            + "           0 AND p.patient_id ="
            + "           ee.patient_id AND"
            + "           ee.location_id ="
            + "           :location AND oo.value_coded = ${1256}"
            + "           AND ee.encounter_datetime <="
            + "           :endDate"
            + "                      LIMIT 1)"
            + "           AS date), INTERVAL 365 DAY)"
            + "     GROUP  BY p.patient_id"
            + "     UNION"
            + "     SELECT p.patient_id"
            + "     FROM   patient p"
            + "            INNER JOIN encounter e"
            + "                    ON e.patient_id = p.patient_id"
            + "            INNER JOIN obs o"
            + "                    ON o.encounter_id = e.encounter_id"
            + "     WHERE  p.voided = 0"
            + "       AND e.voided = 0"
            + "       AND o.voided = 0"
            + "       AND e.location_id = :location"
            + "       AND e.encounter_type = ${6}"
            + "       AND o.concept_id = ${6122}"
            + "       AND o.value_coded = ${1267}"
            + "       AND e.encounter_datetime BETWEEN Date_add("
            + "           Cast((SELECT ee.encounter_datetime"
            + "           FROM"
            + "           encounter ee INNER JOIN obs oo ON"
            + "                ee.encounter_id = oo.encounter_id"
            + "           WHERE"
            + "           ee.voided = 0 AND oo.voided = 0"
            + "                                                 AND"
            + "                p.patient_id ="
            + "                ee.patient_id AND ee.encounter_type IN"
            + "           (${6},"
            + "           ${9}) AND oo.concept_id = ${6128}"
            + "                                                 AND"
            + "                ee.location_id ="
            + "                :location AND oo.value_datetime IS NOT NULL"
            + "           AND"
            + "           oo.value_datetime <="
            + "           :endDate"
            + "                LIMIT 1) AS date), INTERVAL 173 DAY)"
            + "                                        AND"
            + "                                            Date_add("
            + "           Cast((SELECT ee.encounter_datetime FROM"
            + "           encounter ee INNER JOIN obs oo ON"
            + "                    ee.encounter_id = oo.encounter_id"
            + "           WHERE ee.voided = 0 AND oo.voided = 0"
            + "                                            AND"
            + "                    p.patient_id ="
            + "                    ee.patient_id AND ee.encounter_type IN"
            + "           (${6}, ${9}) AND oo.concept_id = ${6128}"
            + "                                            AND"
            + "                    ee.location_id ="
            + "                    :location AND oo.value_datetime IS NOT NULL"
            + "           AND oo.value_datetime <="
            + "                                            :endDate"
            + "                    LIMIT 1) AS date), INTERVAL 365 DAY)"
            + "   GROUP  BY p.patient_id"
            + "   UNION"
            + "   SELECT p.patient_id"
            + "   FROM   patient p"
            + "          INNER JOIN encounter e"
            + "                  ON e.patient_id = p.patient_id"
            + "          INNER JOIN obs o"
            + "                  ON o.encounter_id = e.encounter_id"
            + "   WHERE  p.voided = 0"
            + "       AND e.voided = 0"
            + "       AND o.voided = 0"
            + "       AND e.location_id = :location"
            + "       AND e.encounter_type = ${6}"
            + "       AND o.concept_id = ${6122}"
            + "       AND o.value_coded = ${1267}"
            + "       AND e.encounter_datetime BETWEEN Date_add("
            + "           Cast((SELECT ee.encounter_datetime"
            + "           FROM"
            + "           encounter ee INNER JOIN obs oo ON"
            + "                ee.encounter_id = oo.encounter_id"
            + "           WHERE"
            + "           ee.voided = 0 AND oo.voided = 0"
            + "                                                 AND"
            + "                p.patient_id ="
            + "                ee.patient_id AND ee.encounter_type ="
            + "           ${60}"
            + "           AND oo.concept_id = ${23985} AND"
            + "                oo.value_coded IN ("
            + "                ${656}, ${23982}) AND ee.location_id = :location"
            + "           AND"
            + "           ee.encounter_datetime <="
            + "           :endDate"
            + "                LIMIT 1) AS date), INTERVAL 173 DAY)"
            + "                                        AND"
            + "                                            Date_add("
            + "           Cast((SELECT ee.encounter_datetime FROM"
            + "           encounter ee INNER JOIN obs oo ON"
            + "                    ee.encounter_id = oo.encounter_id"
            + "           WHERE ee.voided = 0 AND oo.voided = 0"
            + "                                            AND"
            + "                    p.patient_id ="
            + "                    ee.patient_id AND ee.encounter_type ="
            + "           ${60} AND oo.concept_id = ${23985} AND"
            + "                    oo.value_coded IN ("
            + "                    ${656}, ${23982}) AND ee.location_id = :location"
            + "           AND ee.encounter_datetime <="
            + "                                            :endDate"
            + "                    LIMIT 1) AS date), INTERVAL 365 DAY)"
            + "         GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getIPTB3(
      int masterCardEncounterType,
      int dataFinalizacaoProfilaxiaIsoniazidaConcept,
      int adultoSeguimentoEncounterType,
      int isoniazidUsageConcept,
      int startDrugsConcept,
      int dataInicioProfilaxiaIsoniazidaConcept,
      int pediatriaSeguimentoEncounterType,
      int regimeTPTEncounterType,
      int regimeTPTConcept,
      int isoniazidConcept,
      int isoniazidePiridoxinaConcept) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with Profilaxia com INH B3");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", masterCardEncounterType);
    map.put("6", adultoSeguimentoEncounterType);
    map.put("9", pediatriaSeguimentoEncounterType);
    map.put("6128", dataInicioProfilaxiaIsoniazidaConcept);
    map.put("6129", dataFinalizacaoProfilaxiaIsoniazidaConcept);
    map.put("6122", isoniazidUsageConcept);
    map.put("1256", startDrugsConcept);
    map.put("60", regimeTPTEncounterType);
    map.put("23985", regimeTPTConcept);
    map.put("656", isoniazidConcept);
    map.put("23982", isoniazidePiridoxinaConcept);

    String query =
        "SELECT p.patient_id"
            + "FROM   patient p"
            + "       INNER JOIN encounter e"
            + "               ON e.patient_id = p.patient_id"
            + "       INNER JOIN obs o"
            + "               ON o.encounter_id = e.encounter_id"
            + "WHERE  p.voided = 0"
            + "       AND e.voided = 0"
            + "       AND o.voided = 0"
            + "       AND e.location_id = :location"
            + "       AND e.encounter_type = ${6}"
            + "       AND o.concept_id = ${6129}"
            + "       AND o.value_datetime IS NOT NULL"
            + "       AND o.value_datetime BETWEEN Date_add("
            + "               Cast((SELECT oo.value_datetime FROM encounter ee JOIN obs oo ON"
            + "                                        oo.encounter_id"
            + "                                             ="
            + "                                             ee.encounter_id WHERE"
            + "               ee.encounter_type ="
            + "               ${53}"
            + "                                                 AND oo.concept_id = ${6128}"
            + "                                                                          AND"
            + "                                             p.patient_id ="
            + "                                             ee.patient_id AND oo.voided = 0 AND"
            + "               ee.voided"
            + "                                                 = 0 AND ee.location_id ="
            + "                                                                          :location"
            + "               AND"
            + "                                             oo.value_datetime IS NOT NULL AND"
            + "               oo.value_datetime <="
            + "               :endDate"
            + "                                                                          LIMIT"
            + "               1) AS"
            + "                                             date), INTERVAL 173 DAY) AND"
            + "                                        Date_add(Cast((SELECT oo.value_datetime"
            + "                                                 FROM"
            + "                                                 encounter ee JOIN obs oo ON"
            + "                                                 oo.encounter_id"
            + "                                                 ="
            + "                                                 ee.encounter_id WHERE"
            + "                                                 ee.encounter_type = ${53} AND"
            + "                                                 oo.concept_id ="
            + "                                                 ${6128}"
            + "                                                                 AND"
            + "                                                 p.patient_id ="
            + "                                                 ee.patient_id AND oo.voided = 0"
            + "                                                 AND ee.voided = 0 AND"
            + "                                                 ee.location_id ="
            + "                                                       :location AND"
            + "                                                 oo.value_datetime IS NOT NULL"
            + "                                                 AND"
            + "                                                 oo.value_datetime <="
            + "                                                 :endDate"
            + "                                                     LIMIT 1) AS"
            + "                                                 date), INTERVAL 365 DAY)"
            + "   GROUP  BY p.patient_id"
            + "   UNION"
            + "   SELECT p.patient_id"
            + "   FROM   patient p"
            + "       INNER JOIN encounter e"
            + "                  ON e.patient_id = p.patient_id"
            + "          INNER JOIN obs o"
            + "                  ON o.encounter_id = e.encounter_id"
            + "   WHERE  p.voided = 0"
            + "       AND e.voided = 0"
            + "       AND o.voided = 0"
            + "       AND e.location_id = :location"
            + "       AND e.encounter_type = ${6}"
            + "       AND o.concept_id = ${6129}"
            + "       AND o.value_datetime IS NOT NULL"
            + "       AND o.value_datetime BETWEEN Date_add("
            + "               Cast((SELECT ee.encounter_datetime FROM encounter ee JOIN obs oo"
            + "               ON"
            + "                                             oo.encounter_id ="
            + "                                             ee.encounter_id WHERE"
            + "               ee.encounter_type ="
            + "               6"
            + "                                                 AND oo.concept_id = ${6122}"
            + "                                                                          AND"
            + "                                             oo.voided = 0 AND ee.voided = 0 AND"
            + "               p.patient_id ="
            + "               ee.patient_id AND"
            + "                                             ee.location_id ="
            + "                                             :location AND oo.value_coded = ${1256} AND"
            + "               ee.encounter_datetime <= :endDate"
            + "                                                                          LIMIT"
            + "               1)"
            + "                                             AS date), INTERVAL 173 DAY) AND"
            + "                                        Date_add(Cast((SELECT"
            + "                                                 ee.encounter_datetime"
            + "                                                 FROM"
            + "                                                 encounter ee JOIN obs oo ON"
            + "                                                 oo.encounter_id ="
            + "                                                 ee.encounter_id WHERE"
            + "                                                 ee.encounter_type = ${6} AND"
            + "                                                 oo.concept_id ="
            + "                                                 ${6122}"
            + "                                                                    AND"
            + "                                                 oo.voided = 0 AND ee.voided ="
            + "                                                 0 AND p.patient_id ="
            + "                                                 ee.patient_id AND"
            + "                                                 ee.location_id ="
            + "                                                 :location AND oo.value_coded = ${1256}"
            + "                                                 AND ee.encounter_datetime <="
            + "                                                 :endDate"
            + "                                                            LIMIT 1)"
            + "                                                 AS date), INTERVAL 365 DAY)"
            + "    GROUP  BY p.patient_id"
            + "    UNION"
            + "    SELECT p.patient_id"
            + "    FROM   patient p"
            + "       INNER JOIN encounter e"
            + "                  ON e.patient_id = p.patient_id"
            + "          INNER JOIN obs o"
            + "                  ON o.encounter_id = e.encounter_id"
            + "   WHERE  p.voided = 0"
            + "       AND e.voided = 0"
            + "       AND o.voided = 0"
            + "       AND e.location_id = :location"
            + "       AND e.encounter_type = ${6}"
            + "       AND o.concept_id = ${6129}"
            + "       AND o.value_datetime IS NOT NULL"
            + "       AND o.value_datetime BETWEEN Date_add("
            + "           Cast((SELECT ee.encounter_datetime"
            + "           FROM"
            + "           encounter ee INNER JOIN obs oo"
            + "                                        ON"
            + "           ee.encounter_id = oo.encounter_id"
            + "           WHERE"
            + "               ee.voided = 0 AND oo.voided = 0"
            + "                                        AND"
            + "           p.patient_id ="
            + "           ee.patient_id AND ee.encounter_type"
            + "           IN (${6},"
            + "           ${9})"
            + "               AND oo.concept_id = ${6128}"
            + "                                        AND"
            + "           ee.location_id ="
            + "           :location AND oo.value_datetime IS NOT NULL AND"
            + "               oo.value_datetime <="
            + "                                        :endDate"
            + "           LIMIT 1) AS date), INTERVAL 173 DAY) AND"
            + "                                        Date_add("
            + "           Cast((SELECT ee.encounter_datetime FROM"
            + "           encounter"
            + "           ee INNER JOIN obs oo ON"
            + "                    ee.encounter_id = oo.encounter_id"
            + "           WHERE"
            + "           ee.voided = 0 AND oo.voided = 0"
            + "                                        AND"
            + "                    p.patient_id ="
            + "                    ee.patient_id AND ee.encounter_type IN"
            + "           (${6},"
            + "           ${9}) AND oo.concept_id = ${6128}"
            + "                                        AND"
            + "                    ee.location_id ="
            + "                    :location AND oo.value_datetime IS NOT NULL"
            + "           AND"
            + "           oo.value_datetime <="
            + "                                        :endDate"
            + "                    LIMIT 1) AS date), INTERVAL 365 DAY)"
            + "   GROUP  BY p.patient_id"
            + "   UNION"
            + "   SELECT p.patient_id"
            + "   FROM   patient p"
            + "       INNER JOIN encounter e"
            + "                  ON e.patient_id = p.patient_id"
            + "          INNER JOIN obs o"
            + "                  ON o.encounter_id = e.encounter_id"
            + "   WHERE  p.voided = 0"
            + "       AND e.voided = 0"
            + "       AND o.voided = 0"
            + "       AND e.location_id = :location"
            + "       AND e.encounter_type = ${6}"
            + "       AND o.concept_id = ${6129}"
            + "       AND o.value_datetime IS NOT NULL"
            + "       AND o.value_datetime BETWEEN Date_add("
            + "           Cast((SELECT ee.encounter_datetime"
            + "           FROM"
            + "           encounter ee INNER JOIN obs oo"
            + "                                        ON"
            + "           ee.encounter_id = oo.encounter_id"
            + "           WHERE"
            + "               ee.voided = 0 AND oo.voided = 0"
            + "                                        AND"
            + "           p.patient_id ="
            + "           ee.patient_id AND ee.encounter_type"
            + "           = ${60}"
            + "           AND"
            + "               oo.concept_id = ${23985} AND"
            + "           oo.value_coded IN ("
            + "           ${656}, ${23982}) AND ee.location_id = :location AND"
            + "               ee.encounter_datetime <="
            + "                                        :endDate"
            + "           LIMIT 1) AS date), INTERVAL 173 DAY) AND"
            + "                                        Date_add("
            + "           Cast((SELECT ee.encounter_datetime FROM"
            + "           encounter"
            + "           ee INNER JOIN obs oo ON"
            + "                    ee.encounter_id = oo.encounter_id"
            + "           WHERE"
            + "           ee.voided = 0 AND oo.voided = 0"
            + "                                        AND"
            + "                    p.patient_id ="
            + "                    ee.patient_id AND ee.encounter_type ="
            + "           ${60}"
            + "           AND oo.concept_id = ${23985} AND"
            + "                    oo.value_coded IN ("
            + "                    ${656}, ${23982}) AND ee.location_id = :location "
            + "         AND"
            + "        ee.encounter_datetime <="
            + "                                     :endDate"
            + "                 LIMIT 1) AS date), INTERVAL 365 DAY)"
            + "          GROUP  BY p.patient_id ";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getIPTB4(
      int masterCardEncounterType,
      int dataFinalizacaoProfilaxiaIsoniazidaConcept,
      int adultoSeguimentoEncounterType,
      int isoniazidUsageConcept,
      int startDrugsConcept,
      int dataInicioProfilaxiaIsoniazidaConcept,
      int pediatriaSeguimentoEncounterType,
      int regimeTPTEncounterType,
      int regimeTPTConcept,
      int isoniazidConcept,
      int isoniazidePiridoxinaConcept) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with Profilaxia com INH B4");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", masterCardEncounterType);
    map.put("6", adultoSeguimentoEncounterType);
    map.put("9", pediatriaSeguimentoEncounterType);
    map.put("6128", dataInicioProfilaxiaIsoniazidaConcept);
    map.put("6129", dataFinalizacaoProfilaxiaIsoniazidaConcept);
    map.put("6122", isoniazidUsageConcept);
    map.put("1256", startDrugsConcept);
    map.put("60", regimeTPTEncounterType);
    map.put("23985", regimeTPTConcept);
    map.put("656", isoniazidConcept);
    map.put("23982", isoniazidePiridoxinaConcept);

    String query =
        "SELECT"
            + "            p.patient_id"
            + "        FROM"
            + "            patient p"
            + "                INNER JOIN"
            + "            encounter e ON e.patient_id = p.patient_id"
            + "                INNER JOIN"
            + "            obs o ON o.encounter_id = e.encounter_id"
            + "        WHERE"
            + "            p.voided = 0 AND e.voided = 0"
            + "                AND o.voided = 0"
            + "                AND e.location_id = :location"
            + "                AND e.encounter_type = ${9}"
            + "                AND o.concept_id = ${6129}"
            + "                AND o.value_datetime IS NOT NULL"
            + "                AND o.value_datetime BETWEEN DATE_ADD(CAST((SELECT"
            + "                            oo.value_datetime"
            + "                        FROM"
            + "                            encounter ee"
            + "                                JOIN"
            + "                            obs oo ON oo.encounter_id = ee.encounter_id"
            + "                        WHERE"
            + "                            ee.encounter_type = ${53}"
            + "                                AND oo.concept_id = ${6128}"
            + "                                AND p.patient_id = ee.patient_id"
            + "                                AND oo.voided = 0"
            + "                                AND ee.voided = 0"
            + "                                AND ee.location_id = :location"
            + "                                AND oo.value_datetime IS NOT NULL"
            + "                                AND oo.value_datetime <= :endDate"
            + "                        LIMIT 1)"
            + "                    AS DATE),"
            + "                INTERVAL 173 DAY) AND DATE_ADD(CAST((SELECT"
            + "                                oo.value_datetime"
            + "                            FROM"
            + "                                encounter ee"
            + "                                    JOIN"
            + "                                obs oo ON oo.encounter_id = ee.encounter_id"
            + "                            WHERE"
            + "                                ee.encounter_type = ${53}"
            + "                                    AND oo.concept_id = ${6128}"
            + "                                    AND p.patient_id = ee.patient_id"
            + "                                    AND oo.voided = 0"
            + "                                    AND ee.voided = 0"
            + "                                    AND ee.location_id = :location"
            + "                                    AND oo.value_datetime IS NOT NULL"
            + "                                    AND oo.value_datetime <= :endDate"
            + "                            LIMIT 1) AS DATE),"
            + "                INTERVAL 365 DAY)"
            + "        GROUP BY p.patient_id"
            + "     UNION"
            + "        SELECT"
            + "            p.patient_id"
            + "        FROM"
            + "            patient p"
            + "                INNER JOIN"
            + "            encounter e ON e.patient_id = p.patient_id"
            + "                INNER JOIN"
            + "            obs o ON o.encounter_id = e.encounter_id"
            + "        WHERE"
            + "            p.voided = 0 AND e.voided = 0"
            + "                AND o.voided = 0"
            + "                AND e.location_id = :location"
            + "                AND e.encounter_type = ${9}"
            + "                AND o.concept_id = ${6129}"
            + "                AND o.value_datetime IS NOT NULL"
            + "                AND o.value_datetime BETWEEN DATE_ADD(CAST((SELECT"
            + "                            ee.encounter_datetime"
            + "                        FROM"
            + "                            encounter ee"
            + "                                JOIN"
            + "                            obs oo ON oo.encounter_id = ee.encounter_id"
            + "                        WHERE"
            + "                            ee.encounter_type = ${6}"
            + "                                AND oo.concept_id = ${6122}"
            + "                                AND oo.voided = 0"
            + "                                AND ee.voided = 0"
            + "                                AND p.patient_id = ee.patient_id"
            + "                                AND ee.location_id = :location"
            + "                                AND oo.value_coded = ${1256}"
            + "                                AND ee.encounter_datetime <= :endDate"
            + "                        LIMIT 1)"
            + "                    AS DATE),"
            + "                INTERVAL 173 DAY) AND DATE_ADD(CAST((SELECT"
            + "                                ee.encounter_datetime"
            + "                            FROM"
            + "                                encounter ee"
            + "                                    JOIN"
            + "                                obs oo ON oo.encounter_id = ee.encounter_id"
            + "                            WHERE"
            + "                                ee.encounter_type = ${6}"
            + "                                    AND oo.concept_id = ${6122}"
            + "                                    AND oo.voided = 0"
            + "                                    AND ee.voided = 0"
            + "                                    AND p.patient_id = ee.patient_id"
            + "                                    AND ee.location_id = :location"
            + "                                    AND oo.value_coded = ${1256}"
            + "                                    AND ee.encounter_datetime <= :endDate"
            + "                            LIMIT 1) AS DATE),"
            + "                INTERVAL 365 DAY)"
            + "        GROUP BY p.patient_id"
            + "     UNION"
            + "        SELECT"
            + "            p.patient_id"
            + "        FROM"
            + "            patient p"
            + "                INNER JOIN"
            + "            encounter e ON e.patient_id = p.patient_id"
            + "                INNER JOIN"
            + "            obs o ON o.encounter_id = e.encounter_id"
            + "        WHERE"
            + "            p.voided = 0 AND e.voided = 0"
            + "                AND o.voided = 0"
            + "                AND e.location_id = :location"
            + "                AND e.encounter_type = ${9}"
            + "                AND o.concept_id = ${6129}"
            + "                AND o.value_datetime IS NOT NULL"
            + "                AND o.value_datetime BETWEEN DATE_ADD(CAST((SELECT"
            + "                            ee.encounter_datetime"
            + "                        FROM"
            + "                            encounter ee"
            + "                                INNER JOIN"
            + "                            obs oo ON ee.encounter_id = oo.encounter_id"
            + "                        WHERE"
            + "                            ee.voided = 0 AND oo.voided = 0"
            + "                                AND p.patient_id = ee.patient_id"
            + "                                AND ee.encounter_type IN (${6} , ${9})"
            + "                                AND oo.concept_id = ${6128}"
            + "                                AND ee.location_id = :location"
            + "                                AND oo.value_datetime IS NOT NULL"
            + "                                AND oo.value_datetime <= :endDate"
            + "                        LIMIT 1)"
            + "                    AS DATE),"
            + "                INTERVAL 173 DAY) AND DATE_ADD(CAST((SELECT"
            + "                            ee.encounter_datetime"
            + "                        FROM"
            + "                            encounter ee"
            + "                                INNER JOIN"
            + "                            obs oo ON ee.encounter_id = oo.encounter_id"
            + "                        WHERE"
            + "                            ee.voided = 0 AND oo.voided = 0"
            + "                                AND p.patient_id = ee.patient_id"
            + "                                AND ee.encounter_type IN (${6} , ${9})"
            + "                                AND oo.concept_id = ${6128}"
            + "                                AND ee.location_id = :location"
            + "                                AND oo.value_datetime IS NOT NULL"
            + "                                AND oo.value_datetime <= :endDate"
            + "                        LIMIT 1)"
            + "                    AS DATE),"
            + "                INTERVAL 365 DAY)"
            + "        GROUP BY p.patient_id"
            + "     UNION"
            + "        SELECT"
            + "            p.patient_id"
            + "        FROM"
            + "            patient p"
            + "                INNER JOIN"
            + "            encounter e ON e.patient_id = p.patient_id"
            + "                INNER JOIN"
            + "            obs o ON o.encounter_id = e.encounter_id"
            + "        WHERE"
            + "            p.voided = 0 AND e.voided = 0"
            + "                AND o.voided = 0"
            + "                AND e.location_id = :location"
            + "                AND e.encounter_type = ${9}"
            + "                AND o.concept_id = ${6129}"
            + "                AND o.value_datetime IS NOT NULL"
            + "                AND o.value_datetime BETWEEN DATE_ADD(CAST((SELECT"
            + "                            ee.encounter_datetime"
            + "                        FROM"
            + "                            encounter ee"
            + "                                INNER JOIN"
            + "                            obs oo ON ee.encounter_id = oo.encounter_id"
            + "                        WHERE"
            + "                            ee.voided = 0 AND oo.voided = 0"
            + "                                AND p.patient_id = ee.patient_id"
            + "                                AND ee.encounter_type = ${60}"
            + "                                AND oo.concept_id = ${23985}"
            + "                                AND oo.value_coded IN (${656} , ${23982})"
            + "                                AND ee.location_id = :location"
            + "                                AND ee.encounter_datetime <= :endDate"
            + "                        LIMIT 1)"
            + "                    AS DATE),"
            + "                INTERVAL 173 DAY) AND DATE_ADD(CAST((SELECT"
            + "                            ee.encounter_datetime"
            + "                        FROM"
            + "                            encounter ee"
            + "                                INNER JOIN"
            + "                            obs oo ON ee.encounter_id = oo.encounter_id"
            + "                        WHERE"
            + "                            ee.voided = 0 AND oo.voided = 0"
            + "                                AND p.patient_id = ee.patient_id"
            + "                                AND ee.encounter_type = ${60}"
            + "                                AND oo.concept_id = ${23985}"
            + "                                AND oo.value_coded IN (${656} , ${23982})"
            + "                                AND ee.location_id = :location"
            + "                                AND ee.encounter_datetime <= :endDate"
            + "                        LIMIT 1)"
            + "                    AS DATE),"
            + "                INTERVAL 365 DAY)"
            + "        GROUP BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getIPTB5Part1(
      int adultoSeguimentoEncounterType,
      int isoniazidUsageConcept,
      int startDrugsConcept,
      int dataInicioProfilaxiaIsoniazidaConcept,
      int pediatriaSeguimentoEncounterType,
      int continuaConcept,
      int yesConcept) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with Profilaxia com INH B5.1");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", adultoSeguimentoEncounterType);
    map.put("9", pediatriaSeguimentoEncounterType);
    map.put("6128", dataInicioProfilaxiaIsoniazidaConcept);
    map.put("6122", isoniazidUsageConcept);
    map.put("1256", startDrugsConcept);
    map.put("1257", continuaConcept);
    map.put("1065", yesConcept);

    String query =
        "  SELECT p.patient_id"
            + "  FROM   patient p"
            + "        INNER JOIN encounter e"
            + "                ON e.patient_id = p.patient_id"
            + "        INNER JOIN obs o"
            + "                ON o.encounter_id = e.encounter_id"
            + "        INNER JOIN(SELECT p.patient_id,"
            + "                          e.encounter_datetime"
            + "                   FROM   patient p"
            + "                          INNER JOIN encounter e"
            + "                                  ON e.patient_id = p.patient_id"
            + "                          INNER JOIN obs o"
            + "                                  ON o.encounter_id = e.encounter_id"
            + "                   WHERE  p.voided = 0"
            + "                          AND e.voided = 0"
            + "                          AND o.voided = 0"
            + "                          AND e.location_id = :location "
            + "                          AND e.encounter_type = ${6}"
            + "                          AND ( ( o.concept_id = ${6122}"
            + "                                  AND o.value_coded = ${1256} )"
            + "                                 OR ( o.concept_id = ${6128}"
            + "                                      AND o.value_datetime IS NOT NULL ) )"
            + "                          AND e.encounter_datetime <= :endDate) AS tabela"
            + "                ON tabela.patient_id = p.patient_id"
            + " WHERE  p.voided = 0"
            + "        AND e.voided = 0"
            + "        AND o.voided = 0"
            + "        AND e.location_id = :location "
            + "        AND e.encounter_type = ${6}"
            + "        AND (( (SELECT Count(*)"
            + "                FROM   encounter ee"
            + "                       INNER JOIN obs oo"
            + "                               ON oo.encounter_id = ee.encounter_id"
            + "                WHERE  ee.voided = 0"
            + "                       AND p.patient_id = ee.patient_id"
            + "                       AND oo.voided = 0"
            + "                       AND ee.location_id = :location "
            + "                       AND oo.concept_id = ${6122}"
            + "                       AND ee.encounter_datetime BETWEEN"
            + "                           tabela.encounter_datetime AND"
            + "               Date_add(tabela.encounter_datetime,"
            + "               INTERVAL 210 DAY)"
            + "               AND ( ( ee.encounter_type = ${6}"
            + "               AND oo.value_coded IN ( ${1256}, ${1257} ) )"
            + "               OR ( ee.encounter_type = ${9}"
            + "               AND oo.value_coded = ${1065} ) )) >= 5 ))"
            + " GROUP  BY p.patient_id ";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getIPTB5Part2(
      int adultoSeguimentoEncounterType,
      int isoniazidUsageConcept,
      int startDrugsConcept,
      int dataInicioProfilaxiaIsoniazidaConcept,
      int continuaConcept,
      int treatmentPrescribedConcept,
      int dtINHConcept) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with Profilaxia com INH B5.2");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", adultoSeguimentoEncounterType);
    map.put("6128", dataInicioProfilaxiaIsoniazidaConcept);
    map.put("6122", isoniazidUsageConcept);
    map.put("1256", startDrugsConcept);
    map.put("1257", continuaConcept);
    map.put("1719", treatmentPrescribedConcept);
    map.put("23955", dtINHConcept);

    String query =
        " SELECT p.patient_id"
            + " FROM   patient p"
            + "       INNER JOIN encounter e"
            + "               ON e.patient_id = p.patient_id"
            + "       INNER JOIN obs o"
            + "               ON o.encounter_id = e.encounter_id"
            + "       INNER JOIN (SELECT p.patient_id,"
            + "                          e.encounter_datetime"
            + "                   FROM   patient p"
            + "                          INNER JOIN encounter e"
            + "                                  ON e.patient_id = p.patient_id"
            + "                          INNER JOIN obs o"
            + "                                  ON o.encounter_id = e.encounter_id"
            + "                   WHERE  p.voided = 0"
            + "                          AND e.voided = 0"
            + "                          AND o.voided = 0"
            + "                          AND e.location_id = :location"
            + "                          AND e.encounter_type = ${6}"
            + "                          AND ( ( o.concept_id = ${6122}"
            + "                                  AND o.value_coded = ${1256} )"
            + "                                 OR ( o.concept_id = ${6128}"
            + "                                      AND o.value_datetime IS NOT NULL ) )"
            + "                          AND e.encounter_datetime <= :endDate) AS tabela"
            + "               ON tabela.patient_id = p.patient_id"
            + "   WHERE  p.voided = 0"
            + "       AND e.voided = 0"
            + "       AND o.voided = 0"
            + "       AND e.location_id = :location"
            + "       AND e.encounter_type = ${6}"
            + "       AND ( (SELECT Count(*)"
            + "              FROM   patient pp"
            + "                     JOIN encounter ee"
            + "                       ON pp.patient_id = ee.patient_id"
            + "              WHERE  pp.voided = 0"
            + "                     AND ee.voided = 0"
            + "                     AND p.patient_id = pp.patient_id"
            + "                     AND ee.encounter_type = ${6}"
            + "                     AND ee.location_id = :location"
            + "                     AND ee.voided = 0"
            + "                     AND ( EXISTS (SELECT eee.patient_id"
            + "                                   FROM   encounter eee"
            + "                                          JOIN obs oo"
            + "                                            ON eee.encounter_id ="
            + "                                               oo.encounter_id"
            + "                                   WHERE  eee.encounter_id = ee.encounter_id"
            + "                                          AND oo.concept_id = ${6122}"
            + "                                          AND oo.value_coded IN(${1256}, ${1257} ))"
            + "                           AND EXISTS (SELECT eee.patient_id"
            + "                                       FROM   encounter eee"
            + "                                              JOIN obs oo"
            + "                                                ON eee.encounter_id ="
            + "                                                   oo.encounter_id"
            + "                                       WHERE  eee.encounter_id = ee.encounter_id"
            + "                                              AND oo.concept_id = ${1719}"
            + "                                              AND oo.value_coded IN( ${23955} )) )"
            + "                     AND ee.encounter_datetime BETWEEN"
            + "                         tabela.encounter_datetime AND"
            + "             Date_add(tabela.encounter_datetime,"
            + "             INTERVAL 150 DAY)) >= 2 )"
            + "   GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getIPTB5Part3(
      int adultoSeguimentoEncounterType,
      int isoniazidUsageConcept,
      int startDrugsConcept,
      int dataInicioProfilaxiaIsoniazidaConcept,
      int continuaConcept,
      int treatmentPrescribedConcept,
      int dtINHConcept) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with Profilaxia com INH B5.3");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", adultoSeguimentoEncounterType);
    map.put("6128", dataInicioProfilaxiaIsoniazidaConcept);
    map.put("6122", isoniazidUsageConcept);
    map.put("1256", startDrugsConcept);
    map.put("1257", continuaConcept);
    map.put("1719", treatmentPrescribedConcept);
    map.put("23955", dtINHConcept);

    String query =
        "  SELECT p.patient_id"
            + "   FROM   patient p"
            + "       INNER JOIN encounter e"
            + "               ON e.patient_id = p.patient_id"
            + "       INNER JOIN obs o"
            + "               ON o.encounter_id = e.encounter_id"
            + "       INNER JOIN(SELECT p.patient_id,"
            + "                         e.encounter_datetime"
            + "                  FROM   patient p"
            + "                         INNER JOIN encounter e"
            + "                                 ON e.patient_id = p.patient_id"
            + "                         INNER JOIN obs o"
            + "                                 ON o.encounter_id = e.encounter_id"
            + "                  WHERE  p.voided = 0"
            + "                         AND e.voided = 0"
            + "                         AND o.voided = 0"
            + "                         AND e.location_id = :location"
            + "                         AND e.encounter_type = ${6}"
            + "                         AND ( ( o.concept_id = ${6122}"
            + "                                 AND o.value_coded = ${1256} )"
            + "                                OR ( o.concept_id = ${6128}"
            + "                                     AND o.value_datetime IS NOT NULL ) )"
            + "                         AND e.encounter_datetime <= :endDate) AS tabela"
            + "               ON tabela.patient_id = p.patient_id"
            + "   WHERE  p.voided = 0"
            + "       AND e.voided = 0"
            + "       AND o.voided = 0"
            + "       AND e.location_id = :location"
            + "       AND e.encounter_type = ${6}"
            + "       AND ( ( (SELECT Count(*)"
            + "                FROM   encounter ee"
            + "                       INNER JOIN obs oo"
            + "                               ON oo.encounter_id = ee.encounter_id"
            + "                WHERE  ee.voided = 0"
            + "                       AND p.patient_id = ee.patient_id"
            + "                       AND oo.voided = 0"
            + "                       AND ee.location_id = :location"
            + "                       AND oo.concept_id = ${6122}"
            + "                       AND ee.encounter_type = ${6}"
            + "                       AND oo.value_coded IN ( ${1256}, ${1257} )"
            + "                       AND ee.encounter_datetime BETWEEN"
            + "                           tabela.encounter_datetime AND"
            + "               Date_add(tabela.encounter_datetime,"
            + "               INTERVAL 210 DAY)) >= 3 )"
            + "             AND (SELECT Count(*)"
            + "                  FROM   encounter ee"
            + "                         INNER JOIN obs oo"
            + "                                 ON oo.encounter_id = ee.encounter_id"
            + "                  WHERE  ee.voided = 0"
            + "                         AND oo.voided = 0"
            + "                         AND ee.location_id = :location"
            + "                         AND ee.encounter_type = ${6}"
            + "                         AND oo.concept_id = ${1719}"
            + "                         AND oo.value_coded = ${23955}"
            + "                         AND p.patient_id = ee.patient_id"
            + "                         AND ee.encounter_datetime BETWEEN"
            + "                             tabela.encounter_datetime AND"
            + "                 Date_add(tabela.encounter_datetime,"
            + "                 INTERVAL 210 DAY)) >= 1 )"
            + "   GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getIPTB6Part1(
      int regimeTPTEncounterType,
      int regimeTPTConcept,
      int isoniazidConcept,
      int isoniazidePiridoxinaConcept,
      int monthlyConcept,
      int typeDispensationTPTConceptUuid) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with Regime de TPT B6.1");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("60", regimeTPTEncounterType);
    map.put("23985", regimeTPTConcept);
    map.put("656", isoniazidConcept);
    map.put("23982", isoniazidePiridoxinaConcept);
    map.put("23986", typeDispensationTPTConceptUuid);
    map.put("1098", monthlyConcept);

    String query =
        " SELECT p.patient_id"
            + "   FROM   patient p"
            + "       inner join encounter e"
            + "               ON e.patient_id = p.patient_id"
            + "       inner join obs o"
            + "               ON o.encounter_id = e.encounter_id"
            + "       inner join (SELECT p.patient_id,"
            + "                          e.encounter_datetime"
            + "                   FROM   patient p"
            + "                          inner join encounter e"
            + "                                  ON e.patient_id = p.patient_id"
            + "                          inner join obs o"
            + "                                  ON e.encounter_id = o.encounter_id"
            + "                   WHERE  e.voided = 0"
            + "                          AND o.voided = 0"
            + "                          AND p.voided = 0"
            + "                          AND e.encounter_type = ${60}"
            + "                          AND o.concept_id = ${23985}"
            + "                          AND o.value_coded IN ( ${656}, ${23982} )"
            + "                          AND e.location_id = :location"
            + "                          AND e.encounter_datetime <= :endDate) AS tabela"
            + "               ON tabela.patient_id = p.patient_id"
            + "   WHERE  p.voided = 0"
            + "       AND e.voided = 0"
            + "       AND o.voided = 0"
            + "       AND e.location_id = :location"
            + "       AND e.encounter_type = ${60}"
            + "       AND (( (SELECT Count(*)"
            + "               FROM   patient pp"
            + "                      inner join encounter ee"
            + "                              ON pp.patient_id = ee.patient_id"
            + "               WHERE  pp.voided = 0"
            + "                      AND ee.voided = 0"
            + "                      AND p.patient_id = pp.patient_id"
            + "                      AND ee.location_id = :location"
            + "                      AND ( EXISTS(SELECT oo.obs_id"
            + "                                   FROM   obs oo"
            + "                                   WHERE  oo.encounter_id = ee.encounter_id"
            + "                                          AND oo.concept_id = ${23985}"
            + "                                          AND oo.value_coded IN ( ${656}, ${23982} ))"
            + "                            AND EXISTS(SELECT oo.obs_id"
            + "                                       FROM   obs oo"
            + "                                       WHERE  oo.encounter_id = ee.encounter_id"
            + "                                              AND oo.concept_id = ${23986}"
            + "                                              AND oo.value_coded IN ( ${1098} )) )"
            + "                      AND ee.encounter_datetime BETWEEN"
            + "                          tabela.encounter_datetime AND"
            + "              Date_add(tabela.encounter_datetime,"
            + "              interval 210 DAY)) >= 6 ))"
            + "   GROUP  BY p.patient_id  ";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getIPTB6Part2(
      int regimeTPTEncounterType,
      int regimeTPTConcept,
      int isoniazidConcept,
      int isoniazidePiridoxinaConcept,
      int typeDispensationTPTConceptUuid,
      int quarterlyConcept) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with Regime de TPT B6.2");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("60", regimeTPTEncounterType);
    map.put("23985", regimeTPTConcept);
    map.put("656", isoniazidConcept);
    map.put("23982", isoniazidePiridoxinaConcept);
    map.put("23986", typeDispensationTPTConceptUuid);
    map.put("23720", quarterlyConcept);

    String query =
        " SELECT p.patient_id   "
            + "FROM   patient p   "
            + "       inner join encounter e  "
            + "               ON e.patient_id = p.patient_id  "
            + "       inner join obs o    "
            + "               ON o.encounter_id = e.encounter_id  "
            + "       inner join (SELECT p.patient_id,    "
            + "                          e.encounter_datetime "
            + "                   FROM   patient p    "
            + "                          inner join encounter e   "
            + "                                  ON e.patient_id = p.patient_id   "
            + "                          inner join obs o "
            + "                                  ON e.encounter_id = o.encounter_id   "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND p.voided = 0 "
            + "                          AND e.encounter_type = ${60}    "
            + "                          AND o.concept_id = ${23985} "
            + "                          AND o.value_coded IN ( ${656}, ${23982} )  "
            + "                          AND e.location_id = :location  "
            + "                          AND e.encounter_datetime <= :endDate) AS tabela  "
            + "               ON tabela.patient_id = p.patient_id "
            + "WHERE  p.voided = 0    "
            + "       AND e.voided = 0    "
            + "       AND o.voided = 0    "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_type = ${60}   "
            + "       AND (( (SELECT Count(*) "
            + "               FROM   patient pp   "
            + "                      inner join encounter ee  "
            + "                              ON pp.patient_id = ee.patient_id "
            + "               WHERE  pp.voided = 0    "
            + "                      AND ee.voided = 0    "
            + "                      AND p.patient_id = pp.patient_id "
            + "                      AND ee.location_id = :location "
            + "                      AND ( EXISTS(SELECT oo.obs_id    "
            + "                                   FROM   obs oo   "
            + "                                   WHERE  oo.encounter_id = ee.encounter_id    "
            + "                                          AND oo.concept_id = ${23985}    "
            + "                                          AND oo.value_coded IN ( ${656}, ${23982} ))    "
            + "                            AND EXISTS(SELECT oo.obs_id    "
            + "                                       FROM   obs oo   "
            + "                                       WHERE  oo.encounter_id = ee.encounter_id    "
            + "                                              AND oo.concept_id = ${23986}    "
            + "                                              AND oo.value_coded IN ( ${23720} )) )   "
            + "                      AND ee.encounter_datetime BETWEEN    "
            + "                          tabela.encounter_datetime AND    "
            + "              Date_add(tabela.encounter_datetime,  "
            + "              interval 210 DAY)) >= 2 ))   "
            + "GROUP  BY p.patient_id  ";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getIPTB6Part3(
      int regimeTPTEncounterType,
      int regimeTPTConcept,
      int isoniazidConcept,
      int isoniazidePiridoxinaConcept,
      int monthlyConcept,
      int typeDispensationTPTConceptUuid,
      int quarterlyConcept) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with INH Mensal and DT-INH B6.3");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("60", regimeTPTEncounterType);
    map.put("23985", regimeTPTConcept);
    map.put("656", isoniazidConcept);
    map.put("23982", isoniazidePiridoxinaConcept);
    map.put("23986", typeDispensationTPTConceptUuid);
    map.put("1098", monthlyConcept);
    map.put("23720", quarterlyConcept);

    String query =
        " SELECT p.patient_id"
            + "   FROM   patient p"
            + "       inner join encounter e"
            + "               ON e.patient_id = p.patient_id"
            + "       inner join obs o"
            + "               ON o.encounter_id = e.encounter_id"
            + "       inner join (SELECT p.patient_id,"
            + "                          e.encounter_datetime"
            + "                   FROM   patient p"
            + "                          inner join encounter e"
            + "                                  ON e.patient_id = p.patient_id"
            + "                          inner join obs o"
            + "                                  ON e.encounter_id = o.encounter_id"
            + "                   WHERE  e.voided = 0"
            + "                          AND o.voided = 0"
            + "                          AND p.voided = 0"
            + "                          AND e.encounter_type = ${60}"
            + "                          AND o.concept_id = ${23985}"
            + "                          AND o.value_coded IN ( ${656}, ${23982} )"
            + "                          AND e.location_id = :location"
            + "                          AND e.encounter_datetime <= :endDate) AS tabela"
            + "               ON tabela.patient_id = p.patient_id"
            + "   WHERE  p.voided = 0"
            + "       AND e.voided = 0"
            + "       AND o.voided = 0"
            + "       AND e.location_id = :location"
            + "       AND e.encounter_type = ${60}"
            + "       AND ( ( (SELECT Count(*)"
            + "                FROM   patient pp"
            + "                       inner join encounter ee"
            + "                               ON pp.patient_id = ee.patient_id"
            + "                WHERE  pp.voided = 0"
            + "                       AND ee.voided = 0"
            + "                       AND p.patient_id = pp.patient_id"
            + "                       AND ee.location_id = :location"
            + "                       AND ( EXISTS(SELECT oo.obs_id"
            + "                                    FROM   obs oo"
            + "                                    WHERE  oo.encounter_id = ee.encounter_id"
            + "                                           AND oo.concept_id = ${23985}"
            + "                                           AND oo.value_coded IN ( ${656}, ${23982} ))"
            + "                             AND EXISTS(SELECT oo.obs_id"
            + "                                        FROM   obs oo"
            + "                                        WHERE  oo.encounter_id = ee.encounter_id"
            + "                                               AND oo.concept_id = ${23986}"
            + "                                               AND oo.value_coded IN ( ${1098} )) )"
            + "                       AND ee.encounter_datetime BETWEEN"
            + "                           tabela.encounter_datetime AND"
            + "               Date_add(tabela.encounter_datetime,"
            + "               interval 210 DAY)) >= 3 )"
            + "             AND ( (SELECT Count(*)"
            + "                    FROM   patient pp"
            + "                           inner join encounter ee"
            + "                                   ON pp.patient_id = ee.patient_id"
            + "                    WHERE  pp.voided = 0"
            + "                           AND ee.voided = 0"
            + "                           AND p.patient_id = pp.patient_id"
            + "                           AND ee.location_id = :location"
            + "                           AND ( EXISTS(SELECT oo.obs_id"
            + "                                        FROM   obs oo"
            + "                                        WHERE  oo.encounter_id = ee.encounter_id"
            + "                                               AND oo.concept_id = ${23985}"
            + "                                               AND oo.value_coded IN ( ${656},"
            + "                                                   ${23982} ))"
            + "                                 AND EXISTS(SELECT oo.obs_id"
            + "                                            FROM   obs oo"
            + "                                            WHERE  oo.encounter_id ="
            + "                                                   ee.encounter_id"
            + "                                                   AND oo.concept_id = ${23986}"
            + "                                                   AND oo.value_coded IN ( ${23720}"
            + "                                                       )) )"
            + "                           AND ee.encounter_datetime BETWEEN"
            + "                               tabela.encounter_datetime AND"
            + "                   Date_add(tabela.encounter_datetime,"
            + "                   interval 210 DAY)) >= 1 ) )"
            + "   GROUP  BY p.patient_id  ";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition get3HPC1(
      int adultoSeguimentoEncounterType, int threeHPConcept, int treatmentPrescribedConcept) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with Outras prescricoes C1");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", adultoSeguimentoEncounterType);
    map.put("23954", threeHPConcept);
    map.put("1719", treatmentPrescribedConcept);

    String query =
        " SELECT p.patient_id"
            + " FROM   patient p"
            + "        inner join encounter e"
            + "                ON e.patient_id = p.patient_id"
            + "        inner join obs o"
            + "                ON o.encounter_id = e.encounter_id"
            + "        inner join (SELECT p.patient_id,"
            + "                           e.encounter_datetime"
            + "                    FROM   patient p"
            + "                           inner join encounter e"
            + "                                   ON e.patient_id = p.patient_id"
            + "                           inner join obs o"
            + "                                   ON o.encounter_id = e.encounter_id"
            + "                    WHERE  p.voided = 0"
            + "                           AND e.voided = 0"
            + "                           AND o.voided = 0"
            + "                           AND e.location_id = :location"
            + "                           AND e.encounter_type = ${6}"
            + "                           AND o.concept_id = ${1719}"
            + "                           AND o.value_coded IN ( ${23954})"
            + "                           AND e.encounter_datetime <= :endDate) AS tabela"
            + "                ON tabela.patient_id = p.patient_id"
            + " WHERE  p.voided = 0"
            + "        AND e.voided = 0"
            + "        AND o.voided = 0"
            + "        AND e.location_id = :location"
            + "        AND e.encounter_type = ${6}"
            + "        AND ( (SELECT Count(*)"
            + "               FROM   patient pp"
            + "                      join encounter ee"
            + "                        ON pp.patient_id = ee.patient_id"
            + "                      join obs oo"
            + "                        ON oo.encounter_id = ee.encounter_id"
            + "              WHERE  pp.voided = 0"
            + "                     AND ee.voided = 0"
            + "                     AND oo.voided = 0"
            + "                     AND p.patient_id = pp.patient_id"
            + "                     AND ee.encounter_type = ${6}"
            + "                     AND ee.location_id = :location"
            + "                     AND ee.voided = 0"
            + "                     AND oo.concept_id = ${1719}"
            + "                     AND oo.value_coded = ${23954}"
            + "                     AND ee.encounter_datetime BETWEEN"
            + "                         tabela.encounter_datetime AND"
            + "             Date_add(tabela.encounter_datetime,"
            + "             interval 120 DAY)) >= 3 )"
            + " GROUP  BY p.patient_id;  ";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition get3HPC2(
      int threeHPConcept,
      int regimeTPTEncounterType,
      int regimeTPTConcept,
      int hPPiridoxinaConcept,
      int typeDispensationTPTConceptUuid,
      int quarterlyConcept) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with Regime de TPT C2");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("23954", threeHPConcept);
    map.put("60", regimeTPTEncounterType);
    map.put("23985", regimeTPTConcept);
    map.put("23984", hPPiridoxinaConcept);
    map.put("23986", typeDispensationTPTConceptUuid);
    map.put("23720", quarterlyConcept);

    String query =
        " SELECT p.patient_id   "
            + "FROM   patient p   "
            + "       inner join encounter e  "
            + "               ON e.patient_id = p.patient_id  "
            + "       inner join obs o    "
            + "               ON o.encounter_id = e.encounter_id  "
            + "       inner join (SELECT p.patient_id,    "
            + "                          e.encounter_datetime "
            + "                   FROM   patient p    "
            + "                          inner join encounter e   "
            + "                                  ON e.patient_id = p.patient_id   "
            + "                          inner join obs o "
            + "                                  ON o.encounter_id = e.encounter_id   "
            + "                   WHERE  p.voided = 0 "
            + "                          AND e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.location_id = :location  "
            + "                          AND e.encounter_type = ${60}    "
            + "                          AND o.concept_id = ${23985} "
            + "                          AND o.value_coded IN ( ${23954}, ${23984} )    "
            + "                          AND e.encounter_datetime <= :endDate) AS tabela  "
            + "               ON tabela.patient_id = p.patient_id "
            + "WHERE  p.voided = 0    "
            + "       AND e.voided = 0    "
            + "       AND o.voided = 0    "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_type = ${60}   "
            + "       AND ( (SELECT Count(*)  "
            + "              FROM   patient pp    "
            + "                     join encounter ee "
            + "                       ON pp.patient_id = ee.patient_id    "
            + "              WHERE  pp.voided = 0 "
            + "                     AND ee.voided = 0 "
            + "                     AND p.patient_id = pp.patient_id  "
            + "                     AND ee.encounter_type = ${60}    "
            + "                     AND ee.location_id = :location  "
            + "                     AND ee.voided = 0 "
            + "                     AND ( EXISTS (SELECT o.person_id  "
            + "                                   FROM   obs o    "
            + "                                   WHERE  o.encounter_id = ee.encounter_id "
            + "                                          AND o.concept_id = ${23985} "
            + "                                          AND o.value_coded IN ( ${23954}, ${23984} ))   "
            + "                           AND EXISTS (SELECT o.person_id  "
            + "                                       FROM   obs o    "
            + "                                       WHERE  o.encounter_id = ee.encounter_id "
            + "                                              AND o.concept_id = ${23720} "
            + "                                              AND o.value_coded IN ( ${23720} )) )    "
            + "                     AND ee.encounter_datetime BETWEEN "
            + "                         tabela.encounter_datetime AND "
            + "             Date_add(tabela.encounter_datetime,   "
            + "             interval 120 DAY)) >= 1 ) "
            + "GROUP  BY p.patient_id ";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition get3HPC3(
      int threeHPConcept,
      int regimeTPTEncounterType,
      int regimeTPTConcept,
      int hPPiridoxinaConcept,
      int typeDispensationTPTConceptUuid,
      int monthlyConcept) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with Regime de TPT C3");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("23954", threeHPConcept);
    map.put("60", regimeTPTEncounterType);
    map.put("23985", regimeTPTConcept);
    map.put("23984", hPPiridoxinaConcept);
    map.put("23986", typeDispensationTPTConceptUuid);
    map.put("1098", monthlyConcept);

    String query =
        " SELECT p.patient_id   "
            + "FROM   patient p   "
            + "       inner join encounter e  "
            + "               ON e.patient_id = p.patient_id  "
            + "       inner join obs o    "
            + "               ON o.encounter_id = e.encounter_id  "
            + "       inner join (SELECT p.patient_id,    "
            + "                          e.encounter_datetime "
            + "                   FROM   patient p    "
            + "                          inner join encounter e   "
            + "                                  ON e.patient_id = p.patient_id   "
            + "                          inner join obs o "
            + "                                  ON o.encounter_id = e.encounter_id   "
            + "                   WHERE  p.voided = 0 "
            + "                          AND e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.location_id = :location  "
            + "                          AND e.encounter_type = ${60}    "
            + "                          AND o.concept_id = ${23985} "
            + "                          AND o.value_coded IN ( ${23954}, ${23984} )    "
            + "                          AND e.encounter_datetime <= :endDate) AS tabela  "
            + "               ON tabela.patient_id = p.patient_id "
            + "WHERE  p.voided = 0    "
            + "       AND e.voided = 0    "
            + "       AND o.voided = 0    "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_type = ${60}   "
            + "       AND ( (SELECT Count(*)  "
            + "              FROM   patient pp    "
            + "                     join encounter ee "
            + "                       ON pp.patient_id = ee.patient_id    "
            + "              WHERE  pp.voided = 0 "
            + "                     AND ee.voided = 0 "
            + "                     AND p.patient_id = pp.patient_id  "
            + "                     AND ee.encounter_type = ${60}    "
            + "                     AND ee.location_id = :location  "
            + "                     AND ee.voided = 0 "
            + "                     AND ( EXISTS (SELECT o.person_id  "
            + "                                   FROM   obs o    "
            + "                                   WHERE  o.encounter_id = ee.encounter_id "
            + "                                          AND o.concept_id = ${23985} "
            + "                                          AND o.value_coded IN ( ${23954}, ${23984} ))   "
            + "                           AND EXISTS (SELECT o.person_id  "
            + "                                       FROM   obs o    "
            + "                                       WHERE  o.encounter_id = ee.encounter_id "
            + "                                              AND o.concept_id = ${23986} "
            + "                                              AND o.value_coded IN ( ${1098} )) ) "
            + "                     AND ee.encounter_datetime BETWEEN "
            + "                         tabela.encounter_datetime AND "
            + "             Date_add(tabela.encounter_datetime,   "
            + "             interval 120 DAY)) >= 3 ) "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getTBTreatmentPart1(
      int adultoSeguimentoEncounterType,
      int pediatriaSeguimentoEncounterType,
      int startDrugsConcept,
      int tBTreatmentPlanConcept,
      int tBDrugTreatmentStartDate) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with TRATAMENTO DE TUBERCULOSE D1");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", adultoSeguimentoEncounterType);
    map.put("9", pediatriaSeguimentoEncounterType);
    map.put("1256", startDrugsConcept);
    map.put("1268", tBTreatmentPlanConcept);
    map.put("1113", tBDrugTreatmentStartDate);

    String query =
        " SELECT p.patient_id   "
            + "   FROM   patient p   "
            + "          inner join encounter e  "
            + "                  ON e.patient_id = p.patient_id  "
            + "          inner join obs o    "
            + "                  ON o.encounter_id = e.encounter_id  "
            + "   WHERE  p.voided = 0    "
            + "          AND o.voided = 0    "
            + "          AND e.voided = 0    "
            + "          AND e.location_id = 271 "
            + "          AND e.encounter_type IN ( ${6}, ${9} )    "
            + "          AND o.concept_id = ${1268} "
            + "          AND o.value_coded IN ( ${1256}, ${1113} ) "
            + "           OR o.obs_datetime BETWEEN Date_sub(:endDate, INTERVAL 210 DAY) AND "
            + "                                     :endDate "
            + "   GROUP  BY p.patient_id ";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getTBTreatmentPart2(
      int masterCardEncounterType, int pediatriaSeguimentoEncounterType, int tbConcept) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with TRATAMENTO DE TUBERCULOSE D2");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", masterCardEncounterType);
    map.put("1406", pediatriaSeguimentoEncounterType);
    map.put("42", tbConcept);

    String query =
        " SELECT p.patient_id   "
            + "   FROM   patient p   "
            + "          inner join encounter e  "
            + "                  ON e.patient_id = p.patient_id  "
            + "          inner join obs o    "
            + "                  ON o.encounter_id = e.encounter_id  "
            + "   WHERE  p.voided = 0    "
            + "          AND o.voided = 0    "
            + "          AND e.voided = 0    "
            + "          AND e.location_id = :location "
            + "          AND e.encounter_type = ${53}   "
            + "          AND o.concept_id = ${1406} "
            + "          AND o.value_coded = ${42}  "
            + "          AND o.obs_datetime BETWEEN Date_sub(:endDate, INTERVAL 210 DAY) AND "
            + "                                     :endDate "
            + "   GROUP  BY p.patient_id ";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }
}
