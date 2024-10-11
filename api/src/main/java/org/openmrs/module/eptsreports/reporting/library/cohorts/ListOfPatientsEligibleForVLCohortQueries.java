package org.openmrs.module.eptsreports.reporting.library.cohorts;

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
public class ListOfPatientsEligibleForVLCohortQueries {

  private TxCurrCohortQueries txCurrCohortQueries;
  private HivMetadata hivMetadata;
  private CommonMetadata commonMetadata;

  @Autowired
  public ListOfPatientsEligibleForVLCohortQueries(
      TxCurrCohortQueries txCurrCohortQueries,
      HivMetadata hivMetadata,
      CommonMetadata commonMetadata) {

    this.txCurrCohortQueries = txCurrCohortQueries;
    this.hivMetadata = hivMetadata;
    this.commonMetadata = commonMetadata;
  }

  public CohortDefinition getBaseCohort() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("List of patients eligible for VL cohort");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition txcurr = this.txCurrCohortQueries.getTxCurrCompositionCohort("txcurr", true);
    CohortDefinition chdX1 = getLastNextScheduledConsultationDate();
    CohortDefinition chdX2 = getLastNextScheduledPickUpDate();
    CohortDefinition chdX3 = getLastNextScheduledPickUpDateWithMostRecentDataLevantamento();
    CohortDefinition vlMoreThan6Months = getPatientsOnARTForMoreThan6Months();
    CohortDefinition vlLessThan1000MoreThan12Months =
        getPatientsWhoHaveRegisteredVLLessThan1000ForMoreThan12Months();
    CohortDefinition vlGreaterThan1000MoreThan3Months =
        getPatientsWhoHaveRegisteredVLGreaterThan1000ForMoreThan3Months();
    CohortDefinition patientsWithoutAnyVL = getPatientsWhoDontHaveAnyViralLoad();
    CohortDefinition breastfeeding = getPatientsWhoAreBreastfeeding();
    CohortDefinition pregnant = getPatientsWhoArePregnant();

    cd.addSearch(
        "txcurr", EptsReportUtils.map(txcurr, "onOrBefore=${startDate},location=${location}"));

    cd.addSearch(
        "X1",
        EptsReportUtils.map(
            chdX1, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "X2",
        EptsReportUtils.map(
            chdX2, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "X3",
        EptsReportUtils.map(
            chdX3, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "VLMORE6MONTHS",
        EptsReportUtils.map(
            vlMoreThan6Months, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "VL12MONTHS",
        EptsReportUtils.map(
            vlLessThan1000MoreThan12Months,
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "VL3MONTHS",
        EptsReportUtils.map(
            vlGreaterThan1000MoreThan3Months,
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "NOVLRESULTS",
        EptsReportUtils.map(patientsWithoutAnyVL, "endDate=${endDate},location=${location}"));
    cd.addSearch(
        "BREASTFEEDING",
        EptsReportUtils.map(
            breastfeeding, "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));
    cd.addSearch(
        "PREGNANT",
        EptsReportUtils.map(
            pregnant, "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));
    cd.setCompositionString(
        "txcurr AND (X1 OR X2 OR X3) AND VLMORE6MONTHS AND (VL12MONTHS OR VL3MONTHS OR NOVLRESULTS) AND NOT (BREASTFEEDING OR PREGNANT)");

    return cd;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <p>X1- select all patients with “Data da próxima consulta” (concept id 1410, value_datetime) as
   * “Last Next scheduled Consultation Date” from the most recent FICHA CLÍNICA (encounter type 6,9)
   * by report end date (encounter_datetime <= endDate) and “Last Next scheduled Consultation Date”
   * >= startDate and <= endDate
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getLastNextScheduledConsultationDate() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("All patients with data da proxima consulta");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    String query = getLastScheduledConsultationDate(false);

    sqlCohortDefinition.setQuery(query);

    return sqlCohortDefinition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <p>X2- select all patients with “Data do próximo levantamento” (concept id 5096,
   * value_datetime) as “Last Next scheduled Pick-up Date” from the most recent FILA (encounter type
   * 18) by report end date (encounter_datetime <= endDate) and “Last Next scheduled Pick-up Date”
   * >= startDate and <= endDate
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getLastNextScheduledPickUpDate() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "All patients with Data do próximo levantamento from most recent FILA");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    String query = getLastNextScheduledPickUpDate(false);

    sqlCohortDefinition.setQuery(query);

    return sqlCohortDefinition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <p>X3- select all patients with most recent “Data de Levantamento” (concept_id 23866,
   * value_datetime + 30 days) “Last Next scheduled Pick up Date” from “Recepcao Levantou ARV”
   * (encounter type 52) with concept “Levantou ARV” (concept_id 23865) set to “SIM” (Concept id
   * 1065) by report end date (value_datetime <= endDate) and “Last Next scheduled Pick-up Date” >=
   * startDate and <= endDate
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getLastNextScheduledPickUpDateWithMostRecentDataLevantamento() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("All patients with most recent Data de Levantamento");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    String query = getLastNextScheduledPickUpDateWithMostRecentDataLevantamento(false);

    sqlCohortDefinition.setQuery(query);
    return sqlCohortDefinition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <p>VL1- select all patients on ART for more than 6 months on their Last Scheduled Consultation
   * or Drug Pick-up that occurred during the reporting period ( ScheduledDate minus ArtStartDate >=
   * 6 months)
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsOnARTForMoreThan6Months() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "All patients on ART for more than 6 months on their Last Scheduled Consultation");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    valuesMap.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    valuesMap.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    valuesMap.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    valuesMap.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    valuesMap.put("1255", hivMetadata.getARVPlanConcept().getConceptId());
    valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    valuesMap.put("1256", hivMetadata.getStartDrugs().getConceptId());
    valuesMap.put("2", hivMetadata.getARTProgram().getProgramId());

    String X1 = getLastScheduledConsultationDate(true);
    String X2 = getLastNextScheduledPickUpDate(true);
    String X3 = getLastNextScheduledPickUpDateWithMostRecentDataLevantamento(true);

    String query =
        " SELECT recentx.patient_id FROM ( "
            + " SELECT most_recentx.patient_id, MAX(most_recentx.recent_datetime) xdate FROM  ( "
            + X1
            + " UNION  "
            + X2
            + " UNION  "
            + X3
            + ") AS most_recentx "
            + " GROUP BY most_recentx.patient_id "
            + " ) recentx "
            + " INNER JOIN "
            + " (  "
            + " SELECT art.patient_id, MIN(art.art_date) min_art_date FROM ( "
            + " SELECT p.patient_id, MIN(e.encounter_datetime) art_date FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + " WHERE e.encounter_type = ${18} "
            + " AND e.encounter_datetime <= :startDate "
            + " AND e.voided = 0 "
            + " AND p.voided = 0 "
            + " AND e.location_id = :location "
            + " GROUP BY p.patient_id "
            + " UNION "
            + " SELECT p.patient_id, Min(e.encounter_datetime) art_date  "
            + "                                 FROM patient p  "
            + "                           INNER JOIN encounter e  "
            + "                               ON p.patient_id = e.patient_id  "
            + "                           INNER JOIN obs o  "
            + "                               ON e.encounter_id = o.encounter_id  "
            + "                       WHERE  p.voided = 0  "
            + "                           AND e.voided = 0  "
            + "                           AND o.voided = 0  "
            + "                           AND e.encounter_type IN (${6}, ${9}, ${18})  "
            + "                           AND o.concept_id = ${1255}  "
            + "                           AND o.value_coded= ${1256}  "
            + "                           AND e.encounter_datetime <= :startDate  "
            + "                           AND e.location_id = :location  "
            + "                       GROUP  BY p.patient_id  "
            + " UNION "
            + " SELECT p.patient_id, MIN(o.value_datetime) art_date FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + " INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + " WHERE e.encounter_type IN(${6},${9},${18},${53}) "
            + " AND o.concept_id = ${1190} "
            + " AND e.location_id = :location "
            + "                 AND o.value_datetime <= :startDate "
            + " AND e.voided = 0 "
            + " AND p.voided = 0 "
            + " AND o.voided = 0 "
            + " GROUP BY p.patient_id "
            + " UNION "
            + " SELECT p.patient_id, pg.date_enrolled AS art_date "
            + "     FROM   patient p   "
            + "           INNER JOIN patient_program pg  "
            + "                ON p.patient_id = pg.patient_id  "
            + "        INNER JOIN patient_state ps  "
            + "                   ON pg.patient_program_id = ps.patient_program_id  "
            + "     WHERE  pg.location_id = :location AND pg.voided = 0 AND p.voided = 0"
            + "    AND pg.program_id = ${2} and pg.date_enrolled <= :startDate "
            + "     "
            + "    UNION "
            + "     "
            + "    SELECT p.patient_id,  MIN(o.value_datetime) AS art_date FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + " INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                         INNER JOIN obs oyes ON oyes.encounter_id = e.encounter_id  "
            + "                         AND o.person_id = oyes.person_id "
            + " WHERE e.encounter_type = ${52} "
            + " AND o.concept_id = ${23866} "
            + "                 AND o.value_datetime <= :startDate "
            + "                 AND o.voided = 0 "
            + "                 AND oyes.concept_id = ${23865} "
            + "                 AND oyes.value_coded = ${1065} "
            + "                 AND oyes.voided = 0 "
            + " AND e.location_id = :location                 "
            + " AND e.voided = 0 "
            + " AND p.voided = 0 "
            + " GROUP BY p.patient_id "
            + " ) art  "
            + " GROUP BY art.patient_id "
            + " ) min_art "
            + " ON recentx.patient_id = min_art.patient_id "
            + " WHERE TIMESTAMPDIFF(MONTH, min_art.min_art_date, recentx.xdate) >= 6 "
            + " GROUP BY recentx.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <p>VL4-Select all patients who have registered the most recent VL <1000 copies/ml for more than
   * 12 months before “ScheduledDate”, i.e. “ScheduledDate” minus “Most Recent VL Date” >= 12months
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoHaveRegisteredVLLessThan1000ForMoreThan12Months() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "Select all patients who have registered the most recent VL <1000 copies/ml for more than 12 months");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    String X1 = getLastScheduledConsultationDate(true);
    String X2 = getLastNextScheduledPickUpDate(true);
    String X3 = getLastNextScheduledPickUpDateWithMostRecentDataLevantamento(true);

    String VL = mostRecentVL(true);

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        " SELECT most_recentvl.patient_id "
            + " FROM( "
            + " SELECT most_recent.patient_id AS patient_id, MAX(recent_datetime) AS recent_datetime   FROM( "
            + VL
            + "         ) AS most_recent  GROUP BY most_recent.patient_id ) most_recentvl       "
            + " INNER JOIN ( "
            + " SELECT most_recentx.patient_id, MAX(most_recentx.recent_datetime) recent_datetime FROM ("
            + X1
            + " UNION "
            + X2
            + " UNION "
            + X3
            + ") AS most_recentx "
            + " GROUP BY most_recentx.patient_id "
            + " ) AS xpatient ON xpatient.patient_id = most_recentvl.patient_id"
            + "   INNER JOIN encounter e ON most_recentvl.patient_id = e.patient_id  "
            + "   INNER JOIN obs o ON o.encounter_id = e.encounter_id  "
            + " WHERE TIMESTAMPDIFF(MONTH,  most_recentvl.recent_datetime,xpatient.recent_datetime) >= 12 "
            + "   AND        e.location_id = :location  "
            + "                              AND e.voided = 0  "
            + "                              AND o.voided = 0  "
            + "       AND(( "
            + "        e.encounter_datetime = most_recentvl.recent_datetime "
            + "        AND e.encounter_type IN (${13},${6},${9},${51})                             "
            + "        AND o.concept_id = ${856}    "
            + "        AND o.value_numeric < 1000)  "
            + "                              OR "
            + "        (e.encounter_type = ${53}                             "
            + "          AND o.concept_id = ${856}    "
            + "          AND o.value_numeric < 1000)) "
            + " GROUP BY most_recentvl.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <p>VL4-Select all patients who have registered the most recent VL <1000 copies/ml for more than
   * 12 months before “ScheduledDate”, i.e. “ScheduledDate” minus “Most Recent VL Date” >= 12months
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoHaveRegisteredVLGreaterThan1000ForMoreThan3Months() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "Select all patients who have registered the most recent VL >=1000 copies/ml for more than 3 months");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    String X1 = getLastScheduledConsultationDate(true);
    String X2 = getLastNextScheduledPickUpDate(true);
    String X3 = getLastNextScheduledPickUpDateWithMostRecentDataLevantamento(true);

    String VL2 = mostRecentVL(true);

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        " SELECT most_recentvl.patient_id "
            + " FROM( "
            + " SELECT most_recent.patient_id AS patient_id, MAX(recent_datetime) AS recent_datetime   FROM( "
            + VL2
            + "         ) AS most_recent  GROUP BY most_recent.patient_id ) most_recentvl       "
            + " INNER JOIN ( "
            + " SELECT most_recentx.patient_id, MAX(most_recentx.recent_datetime) recent_datetime FROM ("
            + X1
            + " UNION "
            + X2
            + " UNION "
            + X3
            + ") AS most_recentx "
            + " GROUP BY most_recentx.patient_id "
            + " ) AS xpatient ON xpatient.patient_id = most_recentvl.patient_id"
            + "   INNER JOIN encounter e ON most_recentvl.patient_id = e.patient_id  "
            + "   INNER JOIN obs o ON o.encounter_id = e.encounter_id  "
            + " WHERE TIMESTAMPDIFF(MONTH,  most_recentvl.recent_datetime,xpatient.recent_datetime) >= 3 "
            + "   AND        e.location_id = :location  "
            + "                              AND e.voided = 0  "
            + "                              AND o.voided = 0  "
            + "       AND(( "
            + "        e.encounter_datetime = most_recentvl.recent_datetime "
            + "        AND e.encounter_type IN (${13},${6},${9},${51})                             "
            + "        AND o.concept_id = ${856}    "
            + "        AND o.value_numeric > 1000)  "
            + "                              OR "
            + "        (e.encounter_type = ${53}                             "
            + "          AND o.concept_id = ${856}    "
            + "          AND o.value_numeric > 1000)) "
            + " GROUP BY most_recentvl.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <p>VL7- select all patients who DO NOT have any Viral Load Result (concept Id 856 -
   * value_numeric, or concept id 1305 value_coded not null ) documented in the Laboratory Form
   * (encounter type 13, encounter_datetime) or Ficha de Seguimento Adulto or Pediatria (encounter
   * type 6,9, encounter_datetime) or Ficha Clinica (encounter type 6, encounter_datetime) or Ficha
   * Resumo (encounter type 53, obs_datetime ) or FSR form (encounter type 51, encounter_datetime)
   * by start of reporting period (<=startDate)
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoDontHaveAnyViralLoad() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("All patients who DO NOT have any Viral Load Result");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        " SELECT  "
            + "    p.patient_id "
            + "FROM "
            + "    patient p "
            + "        INNER JOIN "
            + "    encounter e ON e.patient_id = p.patient_id "
            + "WHERE "
            + "    e.encounter_type IN (${13} , ${6}, ${9}, ${51}, ${53}) "
            + "        AND e.location_id = :location "
            + "        AND e.voided = 0 "
            + "        AND p.voided = 0 "
            + "        AND p.patient_id NOT IN (SELECT  "
            + "            pp.patient_id "
            + "        FROM "
            + "            patient pp "
            + "                INNER JOIN "
            + "            encounter ee ON ee.patient_id = pp.patient_id "
            + "                INNER JOIN "
            + "            obs oo ON oo.encounter_id = ee.encounter_id "
            + "        WHERE "
            + "            ee.encounter_type = ${53} "
            + "                AND oo.concept_id IN (${856} , ${1305}) "
            + "                AND oo.obs_datetime <= :endDate "
            + "                AND ee.location_id = :location "
            + "                AND ee.voided = 0 "
            + "                AND pp.voided = 0 "
            + "                AND oo.voided = 0  "
            + "                GROUP BY pp.patient_id "
            + "                UNION SELECT  "
            + "            pp.patient_id "
            + "        FROM "
            + "            patient pp "
            + "                INNER JOIN "
            + "            encounter ee ON ee.patient_id = pp.patient_id "
            + "                INNER JOIN "
            + "            obs oo ON oo.encounter_id = ee.encounter_id "
            + "        WHERE "
            + "            ee.encounter_type IN (${13} , ${6}, ${9}, ${51}) "
            + "                AND ee.encounter_datetime <= :endDate "
            + "                AND oo.concept_id IN (${856} , ${1305}) "
            + "                AND ee.location_id = :location "
            + "                AND ee.voided = 0 "
            + "                AND pp.voided = 0 "
            + "                AND oo.voided = 0 "
            + "                GROUP BY pp.patient_id)  "
            + "                GROUP BY p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  public String getLastNextScheduledPickUpDate(boolean selectDatetime) {

    Map<String, Integer> valuesMap = new HashMap<>();

    valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    valuesMap.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());
    String query =
        " FROM patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "       INNER JOIN (SELECT p.patient_id,"
            + "                          Max(e.encounter_datetime) most_recent "
            + "                   FROM   patient p "
            + "                          INNER JOIN encounter e "
            + "                                  ON p.patient_id = e.patient_id "
            + "                   WHERE  e.encounter_type = ${18} "
            + "                          AND e.encounter_datetime <= :startDate "
            + "                          AND e.location_id = :location "
            + "                          AND e.voided = 0 "
            + "                          AND p.voided = 0 "
            + "                   GROUP  BY p.patient_id) last_scheduled "
            + "               ON last_scheduled.patient_id = p.patient_id "
            + "WHERE  last_scheduled.most_recent = e.encounter_datetime "
            + "       AND e.voided = 0 "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_type = ${18} "
            + "       AND p.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND o.concept_id = ${5096} "
            + "       AND o.value_datetime BETWEEN :startDate AND :endDate "
            + "GROUP  BY p.patient_id ";

    String sql =
        selectDatetime
            ? "SELECT p.patient_id, o.value_datetime AS recent_datetime ".concat(query)
            : "SELECT p.patient_id ".concat(query);

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    return stringSubstitutor.replace(sql);
  }

  private String getLastNextScheduledPickUpDateWithMostRecentDataLevantamento(
      boolean selectDatetime) {

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    valuesMap.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    valuesMap.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    valuesMap.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());

    String query =
        "      FROM patient p "
            + "         INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "         INNER JOIN obs oyes ON oyes.encounter_id = e.encounter_id "
            + "         INNER JOIN obs ovalue ON ovalue.encounter_id = e.encounter_id "
            + "         INNER JOIN "
            + "     (SELECT  p.patient_id, MAX(DATE_ADD(o.value_datetime, INTERVAL 30 DAY)) AS last_obs "
            + "     FROM patient p "
            + "     INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "     INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "     WHERE   e.encounter_type = ${52} "
            + "             AND o.concept_id = ${23866} "
            + "             AND o.value_datetime <= :startDate          "
            + "             AND e.voided = 0 "
            + "             AND e.location_id = :location "
            + "             AND e.voided = 0 "
            + "             AND p.voided = 0 "
            + "             AND o.voided = 0 "
            + "     GROUP BY p.patient_id) most_recent ON most_recent.patient_id = e.patient_id "
            + " WHERE "
            + "     e.encounter_type = ${52} "
            + "         AND e.location_id = :location "
            + "         AND oyes.concept_id = ${23865} "
            + "         AND oyes.value_coded = ${1065} "
            + "         AND ovalue.concept_id = ${23866} "
            + "         AND DATE_ADD(ovalue.value_datetime, INTERVAL 30 DAY) = most_recent.last_obs "
            + "         AND most_recent.last_obs BETWEEN :startDate  AND :endDate  "
            + "         AND p.voided = 0 "
            + "         AND e.voided = 0 "
            + "         AND oyes.voided = 0 "
            + "         AND ovalue.voided = 0 "
            + "          "
            + " GROUP BY p.patient_id  ";

    String sql =
        selectDatetime
            ? "SELECT p.patient_id, most_recent.last_obs AS recent_datetime ".concat(query)
            : "SELECT p.patient_id ".concat(query);

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    return stringSubstitutor.replace(sql);
  }

  private String getLastScheduledConsultationDate(boolean selectDatetime) {

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("1410", hivMetadata.getReturnVisitDateConcept().getConceptId());

    String query =
        "             FROM   patient p   "
            + "                     INNER JOIN encounter e   "
            + "                                     ON p.patient_id = e.patient_id   "
            + "                     INNER JOIN obs o   "
            + "                                     ON e.encounter_id = o.encounter_id   "
            + "     INNER JOIN  "
            + "       ( SELECT p.patient_id, MAX(e.encounter_datetime) as encounter_date  "
            + "      FROM  patient p   "
            + "       INNER JOIN encounter e  ON p.patient_id = e.patient_id  "
            + "      WHERE p.voided = 0  "
            + "       AND e.voided = 0   "
            + "       AND e.location_id = :location  "
            + "       AND e.encounter_datetime <= :startDate  "
            + "       AND e.encounter_type IN (${6}, ${9})  "
            + "      GROUP BY p.patient_id  "
            + "       )max_encounter ON p.patient_id=max_encounter.patient_id "
            + "             WHERE  p.voided = 0   "
            + "                     AND e.voided = 0   "
            + "                     AND o.voided = 0   "
            + "                     AND e.location_id = :location "
            + "                     AND e.encounter_type IN (${6}, ${9})  "
            + "                     AND o.concept_id = ${1410}   "
            + "                     AND e.encounter_datetime <= :endDate  "
            + "                     AND o.value_datetime BETWEEN :startDate AND :endDate "
            + "                     AND max_encounter.encounter_date = e.encounter_datetime  "
            + "             GROUP BY p.patient_id ";

    String sql =
        selectDatetime
            ? "SELECT p.patient_id, o.value_datetime AS recent_datetime ".concat(query)
            : "SELECT p.patient_id ".concat(query);

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    return stringSubstitutor.replace(sql);
  }

  enum Sentence {
    GreaterThan(">"),
    EqualOrGreaterThan(">="),
    LessThan("<"),
    EqualOrLessThan("<="),
    Different("<>");

    private final String sentence;

    Sentence(String sentence) {
      this.sentence = sentence;
    }

    public String getSentence() {
      return this.sentence;
    }
  }

  /**
   * <b>Patient is Breastfeeding</b>
   * <li>Patient is identified as Female
   * <li>Patient has “Delivery date” registered in the Initial or Follow-up Consultation (Processo
   *     Clínico Parte A, Ficha de Seguimento, Ficha Resumo or Ficha Clínica) where the delivery
   *     date occurred within the period range or
   * <li>Patient has started ART for being breastfeeding as specified in “CRITÉRIO PARA INÍCIO DE
   *     TARV” in the Initial or Follow-up Consultation (Processo Clínico Parte A or Ficha de
   *     Seguimento) that occurred within the period range or
   * <li>Patient has registered as breastfeeding in Initial or Follow-up Consultation (Ficha de
   *     Seguimento, Ficha Resumo or Ficha Clínica) occurred within the period range or
   * <li>Patient has been registered in the PTV/ETV program with state 27 (gave birth) within the
   *     period range
   * <li>Patient who have “Actualmente está a amamentar” marked as “Sim” on e-Lab Form and “Data de
   *     Colheita” is during the period range.
   *
   *     <p>The system will consider the following as period range: <br>
   *     1. start_date = report end date - 18 months <br>
   *     2. end_date = report end date
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoAreBreastfeeding() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Breastfeeding Patients");
    sqlCohortDefinition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6332", commonMetadata.getBreastfeeding().getConceptId());
    valuesMap.put("1065", hivMetadata.getYesConcept().getConceptId());
    valuesMap.put("5599", hivMetadata.getPriorDeliveryDateConcept().getConceptId());
    valuesMap.put("1600", hivMetadata.getPregnancyDueDate().getConceptId());
    valuesMap.put("5", hivMetadata.getARVAdultInitialEncounterType().getEncounterTypeId());
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("6334", hivMetadata.getCriteriaForArtStart().getConceptId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("8", hivMetadata.getPtvEtvProgram().getProgramId());
    valuesMap.put("27", hivMetadata.getPatientGaveBirthWorkflowState().getProgramWorkflowStateId());
    valuesMap.put("1190", hivMetadata.getHistoricalDrugStartDateConcept().getConceptId());
    valuesMap.put("1982", commonMetadata.getPregnantConcept().getConceptId());
    valuesMap.put("1279", hivMetadata.getNumberOfWeeksPregnant().getConceptId());
    valuesMap.put("6331", hivMetadata.getBPlusConcept().getConceptId());
    valuesMap.put("23821", hivMetadata.getSampleCollectionDateAndTime().getConceptId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());

    String query =
        " SELECT list.patient_id "
            + "FROM   (SELECT breastfeeding.patient_id, "
            + "               breastfeeding.last_date "
            + "        FROM   (SELECT p.patient_id, "
            + "                       Max(o.value_datetime) AS last_date "
            + "                FROM   patient p "
            + "                       INNER JOIN person pe "
            + "                               ON p.patient_id = pe.person_id "
            + "                       INNER JOIN encounter e "
            + "                               ON p.patient_id = e.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON e.encounter_id = o.encounter_id "
            + "                WHERE  p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND concept_id = ${5599} "
            + "                       AND e.encounter_type IN ( ${5}, ${6} ) "
            + "                       AND o.value_datetime BETWEEN DATE_SUB(:onOrBefore, INTERVAL 18 MONTH) AND :onOrBefore "
            + "                       AND e.location_id = :location "
            + "                       AND pe.gender = 'F' "
            + "                GROUP  BY p.patient_id "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       Max(e.encounter_datetime) AS last_date "
            + "                FROM   patient p "
            + "                       INNER JOIN person pe "
            + "                               ON p.patient_id = pe.person_id "
            + "                       INNER JOIN encounter e "
            + "                               ON p.patient_id = e.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON e.encounter_id = o.encounter_id "
            + "                WHERE  p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o.concept_id = ${6332} "
            + "                       AND o.value_coded = ${1065} "
            + "                       AND e.encounter_type IN ( ${5}, ${6} ) "
            + "                       AND e.encounter_datetime BETWEEN DATE_SUB(:onOrBefore, INTERVAL 18 MONTH) AND :onOrBefore "
            + "                       AND e.location_id = :location "
            + "                       AND pe.gender = 'F' "
            + "                GROUP  BY p.patient_id "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       Max(e.encounter_datetime) AS last_date "
            + "                FROM   patient p "
            + "                       INNER JOIN person pe "
            + "                               ON p.patient_id = pe.person_id "
            + "                       INNER JOIN encounter e "
            + "                               ON p.patient_id = e.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON e.encounter_id = o.encounter_id "
            + "                WHERE  p.voided = 0 "
            + "                       AND pe.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o.concept_id = ${6334} "
            + "                       AND o.value_coded = ${6332} "
            + "                       AND e.encounter_type IN ( ${5}, ${6} ) "
            + "                       AND e.encounter_datetime BETWEEN DATE_SUB(:onOrBefore, INTERVAL 18 MONTH) AND :onOrBefore "
            + "                       AND e.location_id = :location "
            + "                       AND pe.gender = 'F' "
            + "                GROUP  BY p.patient_id "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       Max(Date(o.value_datetime)) AS last_date "
            + "                FROM   patient p "
            + "                       INNER JOIN person pe "
            + "                               ON p.patient_id = pe.person_id "
            + "                       INNER JOIN encounter e "
            + "                               ON p.patient_id = e.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON e.encounter_id = o.encounter_id "
            + "                       INNER JOIN obs ob "
            + "                               ON e.encounter_id = ob.encounter_id "
            + "                WHERE  p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o.concept_id = ${23821} "
            + "                       AND ob.concept_id = ${6332} "
            + "                       AND ob.value_coded = ${1065} "
            + "                       AND e.encounter_type = ${51} "
            + "                       AND pe.gender = 'F' "
            + "                       AND Date(o.value_datetime) BETWEEN DATE_SUB(:onOrBefore, INTERVAL 18 MONTH) AND :onOrBefore "
            + "                GROUP  BY p.patient_id "
            + "                UNION "
            + "                SELECT pp.patient_id, "
            + "                       Max(ps.start_date) AS last_date "
            + "                FROM   patient_program pp "
            + "                       INNER JOIN person pe "
            + "                               ON pp.patient_id = pe.person_id "
            + "                       INNER JOIN patient_state ps "
            + "                               ON pp.patient_program_id = ps.patient_program_id "
            + "                WHERE  pp.program_id = ${8} "
            + "                       AND ps.state = ${27} "
            + "                       AND pp.voided = 0 "
            + "                       AND ps.voided = 0 "
            + "                       AND ps.start_date BETWEEN DATE_SUB(:onOrBefore, INTERVAL 18 MONTH) AND :onOrBefore "
            + "                       AND pp.location_id = :location "
            + "                       AND pe.gender = 'F' "
            + "                GROUP  BY pp.patient_id "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       Max(hist.value_datetime) AS last_date "
            + "                FROM   patient p "
            + "                       INNER JOIN person pe "
            + "                               ON p.patient_id = pe.person_id "
            + "                       INNER JOIN encounter e "
            + "                               ON p.patient_id = e.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON e.encounter_id = o.encounter_id "
            + "                       INNER JOIN obs hist "
            + "                               ON e.encounter_id = hist.encounter_id "
            + "                WHERE  p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o.concept_id = ${6332} "
            + "                       AND o.value_coded = ${1065} "
            + "                       AND e.encounter_type = ${53} "
            + "                       AND hist.concept_id = ${1190} "
            + "                       AND pe.gender = 'F' "
            + "                       AND hist.value_datetime BETWEEN DATE_SUB(:onOrBefore, INTERVAL 18 MONTH) AND :onOrBefore "
            + "                GROUP  BY p.patient_id) AS breastfeeding "
            + "               LEFT JOIN (SELECT patient_id, "
            + "                                 Max(pregnancy_date) AS pregnancy_date "
            + "                          FROM   (SELECT p.patient_id, "
            + "                                         Max(e.encounter_datetime) AS "
            + "                                         pregnancy_date "
            + "                                  FROM   patient p "
            + "                                         INNER JOIN person pe "
            + "                                                 ON p.patient_id = pe.person_id "
            + "                                         INNER JOIN encounter e "
            + "                                                 ON p.patient_id = e.patient_id "
            + "                                         INNER JOIN obs o "
            + "                                                 ON "
            + "                                         e.encounter_id = o.encounter_id "
            + "                                  WHERE  p.voided = 0 "
            + "                                         AND e.voided = 0 "
            + "                                         AND o.voided = 0 "
            + "                                         AND concept_id = ${1982} "
            + "                                         AND value_coded = ${1065} "
            + "                                         AND e.encounter_type IN ( ${5}, ${6} ) "
            + "                                         AND e.encounter_datetime BETWEEN "
            + "                                             Date_sub(:onOrBefore, INTERVAL "
            + "                                             9 month) "
            + "                                             AND "
            + "                                             :onOrBefore "
            + "                                         AND e.location_id = :location "
            + "                                         AND pe.gender = 'F' "
            + "                                  GROUP  BY p.patient_id "
            + "                                  UNION "
            + "                                  SELECT p.patient_id, "
            + "                                         Max(historical_date.value_datetime) AS "
            + "                                         pregnancy_date "
            + "                                  FROM   patient p "
            + "                                         INNER JOIN person pe "
            + "                                                 ON p.patient_id = pe.person_id "
            + "                                         INNER JOIN encounter e "
            + "                                                 ON p.patient_id = e.patient_id "
            + "                                         INNER JOIN obs pregnancy "
            + "                                                 ON e.encounter_id = "
            + "                                                    pregnancy.encounter_id "
            + "                                         INNER JOIN obs historical_date "
            + "                                                 ON e.encounter_id = "
            + "                                                    historical_date.encounter_id "
            + "                                  WHERE  p.voided = 0 "
            + "                                         AND e.voided = 0 "
            + "                                         AND pregnancy.voided = 0 "
            + "                                         AND pregnancy.concept_id = ${1982} "
            + "                                         AND pregnancy.value_coded = ${1065} "
            + "                                         AND historical_date.voided = 0 "
            + "                                         AND historical_date.concept_id = ${1190} "
            + "                                         AND e.encounter_type = ${53} "
            + "                                         AND historical_date.value_datetime "
            + "                                             BETWEEN "
            + "                                             Date_sub( "
            + "                                             :onOrBefore, "
            + "                                             INTERVAL 9 month) "
            + "                                             AND :onOrBefore "
            + "                                         AND e.location_id = :location "
            + "                                         AND pe.gender = 'F' "
            + "                                  GROUP  BY p.patient_id "
            + "                                  UNION "
            + "                                  SELECT p.patient_id, "
            + "                                         Max(e.encounter_datetime) AS "
            + "                                         pregnancy_date "
            + "                                  FROM   patient p "
            + "                                         INNER JOIN person pe "
            + "                                                 ON p.patient_id = pe.person_id "
            + "                                         INNER JOIN encounter e "
            + "                                                 ON p.patient_id = e.patient_id "
            + "                                         INNER JOIN obs o "
            + "                                                 ON "
            + "                                         e.encounter_id = o.encounter_id "
            + "                                  WHERE  p.voided = 0 "
            + "                                         AND e.voided = 0 "
            + "                                         AND o.voided = 0 "
            + "                                         AND concept_id = ${1279} "
            + "                                         AND e.encounter_type IN ( ${5}, ${6} ) "
            + "                                         AND e.encounter_datetime BETWEEN "
            + "                                             Date_sub(:onOrBefore, INTERVAL "
            + "                                             9 month) "
            + "                                             AND "
            + "                                             :onOrBefore "
            + "                                         AND e.location_id = :location "
            + "                                         AND pe.gender = 'F' "
            + "                                  GROUP  BY p.patient_id "
            + "                                  UNION "
            + "                                  SELECT p.patient_id, "
            + "                                         Max(e.encounter_datetime) AS "
            + "                                         pregnancy_date "
            + "                                  FROM   patient p "
            + "                                         INNER JOIN person pe "
            + "                                                 ON p.patient_id = pe.person_id "
            + "                                         INNER JOIN encounter e "
            + "                                                 ON p.patient_id = e.patient_id "
            + "                                         INNER JOIN obs o "
            + "                                                 ON "
            + "                                         e.encounter_id = o.encounter_id "
            + "                                  WHERE  p.voided = 0 "
            + "                                         AND e.voided = 0 "
            + "                                         AND o.voided = 0 "
            + "                                         AND concept_id = ${1600} "
            + "                                         AND e.encounter_type IN ( ${5}, ${6} ) "
            + "                                         AND e.encounter_datetime BETWEEN "
            + "                                             Date_sub(:onOrBefore, INTERVAL "
            + "                                             9 month) "
            + "                                             AND "
            + "                                             :onOrBefore "
            + "                                         AND e.location_id = :location "
            + "                                         AND pe.gender = 'F' "
            + "                                  GROUP  BY p.patient_id "
            + "                                  UNION "
            + "                                  SELECT p.patient_id, "
            + "                                         Max(e.encounter_datetime) AS "
            + "                                         pregnancy_date "
            + "                                  FROM   patient p "
            + "                                         INNER JOIN person pe "
            + "                                                 ON p.patient_id = pe.person_id "
            + "                                         INNER JOIN encounter e "
            + "                                                 ON p.patient_id = e.patient_id "
            + "                                         INNER JOIN obs o "
            + "                                                 ON "
            + "                                         e.encounter_id = o.encounter_id "
            + "                                  WHERE  p.voided = 0 "
            + "                                         AND pe.voided = 0 "
            + "                                         AND e.voided = 0 "
            + "                                         AND o.voided = 0 "
            + "                                         AND concept_id = ${6334} "
            + "                                         AND value_coded = ${6331} "
            + "                                         AND e.encounter_type IN ( ${5}, ${6} ) "
            + "                                         AND e.encounter_datetime BETWEEN "
            + "                                             Date_sub(:onOrBefore, INTERVAL "
            + "                                             9 month) "
            + "                                             AND "
            + "                                             :onOrBefore "
            + "                                         AND e.location_id = :location "
            + "                                         AND pe.gender = 'F' "
            + "                                  GROUP  BY p.patient_id "
            + "                                  UNION "
            + "                                  SELECT pp.patient_id, "
            + "                                         Max(pp.date_enrolled) AS pregnancy_date "
            + "                                  FROM   patient_program pp "
            + "                                         INNER JOIN person pe "
            + "                                                 ON pp.patient_id = pe.person_id "
            + "                                  WHERE  pp.program_id = ${8} "
            + "                                         AND pp.voided = 0 "
            + "                                         AND pp.date_enrolled BETWEEN "
            + "                                             Date_sub( "
            + "                                             :onOrBefore, "
            + "                                             INTERVAL 9 month) "
            + "                                             AND "
            + "                                             :onOrBefore "
            + "                                         AND pp.location_id = :location "
            + "                                         AND pe.gender = 'F' "
            + "                                  GROUP  BY pp.patient_id) AS pregnancy "
            + "                          GROUP  BY pregnancy.patient_id) AS pregnant_table "
            + "                      ON pregnant_table.patient_id = breastfeeding.patient_id "
            + "        WHERE  ( breastfeeding.last_date > pregnant_table.pregnancy_date "
            + "                  OR pregnant_table.pregnancy_date IS NULL ) "
            + "        GROUP  BY breastfeeding.patient_id) AS list";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>Patient is Pregnant</b>
   * <li>Patient is identified as Female
   * <li>Patient is marked as “PREGNANT” in the Initial Consultation (Processo Clínico Parte A or
   *     Ficha Resumo – Master Card) or Follow-up Consultation (Ficha de Seguimento or Ficha Clínica
   *     – Master Card) that occurred within the period range or
   * <li>Patient has “Number of weeks Pregnant” registered in the Initial or Follow-up Consultation
   *     (Ficha de Seguimento or Ficha Clínica – Master Card) that occurred within the period range
   *     or
   * <li>Patient has “Pregnancy Due Date” registered in the Initial or Follow-up Consultation
   *     (Processo Clínico Parte A or Ficha de Seguimento Adulto) occurred within the period range
   *     or
   * <li>Patient is enrolled on Prevention of the Vertical Transmission/Elimination of the Vertical
   *     Transmission (PTV/ETV) program within the period range or
   * <li>Patient has started ART for being B+ as specified in “CRITÉRIO PARA INÍCIO DE TARV” in the
   *     Follow-up Consultations (Ficha de Seguimento) occurred within the period range.
   * <li>Patient who have “Actualmente encontra-se gravida” marked as “Sim” on e-Lab Form and “Data
   *     de Colheita” is during the period range.
   *
   *     <p>The system will consider the following as period range: <br>
   *     1. start_date = report end date - 9 months <br>
   *     2. end_date = report end date
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoArePregnant() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Pregnant Patients");
    sqlCohortDefinition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6332", commonMetadata.getBreastfeeding().getConceptId());
    valuesMap.put("1065", hivMetadata.getYesConcept().getConceptId());
    valuesMap.put("5599", hivMetadata.getPriorDeliveryDateConcept().getConceptId());
    valuesMap.put("1600", hivMetadata.getPregnancyDueDate().getConceptId());
    valuesMap.put("5", hivMetadata.getARVAdultInitialEncounterType().getEncounterTypeId());
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("6334", hivMetadata.getCriteriaForArtStart().getConceptId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("8", hivMetadata.getPtvEtvProgram().getProgramId());
    valuesMap.put("27", hivMetadata.getPatientGaveBirthWorkflowState().getProgramWorkflowStateId());
    valuesMap.put("1190", hivMetadata.getHistoricalDrugStartDateConcept().getConceptId());
    valuesMap.put("1982", commonMetadata.getPregnantConcept().getConceptId());
    valuesMap.put("1279", hivMetadata.getNumberOfWeeksPregnant().getConceptId());
    valuesMap.put("6331", hivMetadata.getBPlusConcept().getConceptId());
    valuesMap.put("23821", hivMetadata.getSampleCollectionDateAndTime().getConceptId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());

    String query =
        "SELECT max_pregnant.patient_id "
            + "FROM   (SELECT pregnant.patient_id, "
            + "               Max(pregnant.pregnancy_date) AS pregnancy_date "
            + "        FROM   ("
            + "                     SELECT p.patient_id,  Max(e.encounter_datetime) AS "
            + "                                         pregnancy_date "
            + "                                  FROM   patient p "
            + "                                         INNER JOIN person pe "
            + "                                                 ON p.patient_id = pe.person_id "
            + "                                         INNER JOIN encounter e "
            + "                                                 ON p.patient_id = e.patient_id "
            + "                                         INNER JOIN obs o "
            + "                                                 ON "
            + "                                         e.encounter_id = o.encounter_id "
            + "                                  WHERE  p.voided = 0 "
            + "                                         AND e.voided = 0 "
            + "                                         AND o.voided = 0 "
            + "                                         AND concept_id = ${1982} "
            + "                                         AND value_coded = ${1065} "
            + "                                         AND e.encounter_type IN ( ${5}, ${6} ) "
            + "                                         AND e.encounter_datetime BETWEEN "
            + "                                             Date_sub(:onOrBefore, INTERVAL "
            + "                                             9 month) "
            + "                                             AND "
            + "                                             :onOrBefore "
            + "                                         AND e.location_id = :location "
            + "                                         AND pe.gender = 'F' "
            + "                                  GROUP  BY p.patient_id "
            + "                                  UNION "
            + "                                  SELECT p.patient_id, "
            + "                                         Max(historical_date.value_datetime) AS "
            + "                                         pregnancy_date "
            + "                                  FROM   patient p "
            + "                                         INNER JOIN person pe "
            + "                                                 ON p.patient_id = pe.person_id "
            + "                                         INNER JOIN encounter e "
            + "                                                 ON p.patient_id = e.patient_id "
            + "                                         INNER JOIN obs pregnancy "
            + "                                                 ON e.encounter_id = "
            + "                                                    pregnancy.encounter_id "
            + "                                         INNER JOIN obs historical_date "
            + "                                                 ON e.encounter_id = "
            + "                                                    historical_date.encounter_id "
            + "                                  WHERE  p.voided = 0 "
            + "                                         AND e.voided = 0 "
            + "                                         AND pregnancy.voided = 0 "
            + "                                         AND pregnancy.concept_id = ${1982} "
            + "                                         AND pregnancy.value_coded = ${1065} "
            + "                                         AND historical_date.voided = 0 "
            + "                                         AND historical_date.concept_id = ${1190} "
            + "                                         AND e.encounter_type = ${53} "
            + "                                         AND historical_date.value_datetime "
            + "                                             BETWEEN "
            + "                                             Date_sub( "
            + "                                             :onOrBefore, "
            + "                                             INTERVAL 9 month) "
            + "                                             AND :onOrBefore "
            + "                                         AND e.location_id = :location "
            + "                                         AND pe.gender = 'F' "
            + "                                  GROUP  BY p.patient_id "
            + "                                  UNION "
            + "                                  SELECT p.patient_id, "
            + "                                         Max(e.encounter_datetime) AS "
            + "                                         pregnancy_date "
            + "                                  FROM   patient p "
            + "                                         INNER JOIN person pe "
            + "                                                 ON p.patient_id = pe.person_id "
            + "                                         INNER JOIN encounter e "
            + "                                                 ON p.patient_id = e.patient_id "
            + "                                         INNER JOIN obs o "
            + "                                                 ON "
            + "                                         e.encounter_id = o.encounter_id "
            + "                                  WHERE  p.voided = 0 "
            + "                                         AND e.voided = 0 "
            + "                                         AND o.voided = 0 "
            + "                                         AND concept_id = ${1279} "
            + "                                         AND e.encounter_type IN ( ${5}, ${6} ) "
            + "                                         AND e.encounter_datetime BETWEEN "
            + "                                             Date_sub(:onOrBefore, INTERVAL "
            + "                                             9 month) "
            + "                                             AND "
            + "                                             :onOrBefore "
            + "                                         AND e.location_id = :location "
            + "                                         AND pe.gender = 'F' "
            + "                                  GROUP  BY p.patient_id "
            + "                                  UNION "
            + "                                  SELECT p.patient_id, "
            + "                                         Max(e.encounter_datetime) AS "
            + "                                         pregnancy_date "
            + "                                  FROM   patient p "
            + "                                         INNER JOIN person pe "
            + "                                                 ON p.patient_id = pe.person_id "
            + "                                         INNER JOIN encounter e "
            + "                                                 ON p.patient_id = e.patient_id "
            + "                                         INNER JOIN obs o "
            + "                                                 ON "
            + "                                         e.encounter_id = o.encounter_id "
            + "                                  WHERE  p.voided = 0 "
            + "                                         AND e.voided = 0 "
            + "                                         AND o.voided = 0 "
            + "                                         AND concept_id = ${1600} "
            + "                                         AND e.encounter_type IN ( ${5}, ${6} ) "
            + "                                         AND e.encounter_datetime BETWEEN "
            + "                                             Date_sub(:onOrBefore, INTERVAL "
            + "                                             9 month) "
            + "                                             AND "
            + "                                             :onOrBefore "
            + "                                         AND e.location_id = :location "
            + "                                         AND pe.gender = 'F' "
            + "                                  GROUP  BY p.patient_id "
            + "                                  UNION "
            + "                                  SELECT p.patient_id, "
            + "                                         Max(e.encounter_datetime) AS "
            + "                                         pregnancy_date "
            + "                                  FROM   patient p "
            + "                                         INNER JOIN person pe "
            + "                                                 ON p.patient_id = pe.person_id "
            + "                                         INNER JOIN encounter e "
            + "                                                 ON p.patient_id = e.patient_id "
            + "                                         INNER JOIN obs o "
            + "                                                 ON "
            + "                                         e.encounter_id = o.encounter_id "
            + "                                  WHERE  p.voided = 0 "
            + "                                         AND pe.voided = 0 "
            + "                                         AND e.voided = 0 "
            + "                                         AND o.voided = 0 "
            + "                                         AND concept_id = ${6334} "
            + "                                         AND value_coded = ${6331} "
            + "                                         AND e.encounter_type IN ( ${5}, ${6} ) "
            + "                                         AND e.encounter_datetime BETWEEN "
            + "                                             Date_sub(:onOrBefore, INTERVAL "
            + "                                             9 month) "
            + "                                             AND "
            + "                                             :onOrBefore "
            + "                                         AND e.location_id = :location "
            + "                                         AND pe.gender = 'F' "
            + "                                  GROUP  BY p.patient_id "
            + "                                  UNION "
            + "                                  SELECT pp.patient_id, "
            + "                                         Max(pp.date_enrolled) AS pregnancy_date "
            + "                                  FROM   patient_program pp "
            + "                                         INNER JOIN person pe "
            + "                                                 ON pp.patient_id = pe.person_id "
            + "                                  WHERE  pp.program_id = ${8} "
            + "                                         AND pp.voided = 0 "
            + "                                         AND pp.date_enrolled BETWEEN "
            + "                                             Date_sub( "
            + "                                             :onOrBefore, "
            + "                                             INTERVAL 9 month) "
            + "                                             AND "
            + "                                             :onOrBefore "
            + "                                         AND pp.location_id = :location "
            + "                                         AND pe.gender = 'F' "
            + "                                  GROUP  BY pp.patient_id "
            + ") AS pregnant "
            + "        GROUP  BY patient_id) max_pregnant "
            + "       LEFT JOIN (SELECT breastfeeding.patient_id, "
            + "                         Max(breastfeeding.last_date) AS breastfeeding_date "
            + "                  FROM   (SELECT p.patient_id, "
            + "                                 Max(o.value_datetime) AS last_date "
            + "                          FROM   patient p "
            + "                                 INNER JOIN person pe "
            + "                                         ON p.patient_id = pe.person_id "
            + "                                 INNER JOIN encounter e "
            + "                                         ON p.patient_id = e.patient_id "
            + "                                 INNER JOIN obs o "
            + "                                         ON e.encounter_id = o.encounter_id "
            + "                          WHERE  p.voided = 0 "
            + "                                 AND e.voided = 0 "
            + "                                 AND o.voided = 0 "
            + "                                 AND concept_id = ${5599} "
            + "                                 AND e.encounter_type IN ( ${5}, ${6} ) "
            + "                                 AND o.value_datetime BETWEEN "
            + "                                     Date_sub(:onOrBefore, "
            + "                                     INTERVAL 18 month) AND "
            + "                                     :onOrBefore "
            + "                                 AND e.location_id = :location "
            + "                                 AND pe.gender = 'F' "
            + "                          GROUP  BY p.patient_id "
            + "                          UNION "
            + "                          SELECT p.patient_id, "
            + "                                 Max(e.encounter_datetime) AS last_date "
            + "                          FROM   patient p "
            + "                                 INNER JOIN person pe "
            + "                                         ON p.patient_id = pe.person_id "
            + "                                 INNER JOIN encounter e "
            + "                                         ON p.patient_id = e.patient_id "
            + "                                 INNER JOIN obs o "
            + "                                         ON e.encounter_id = o.encounter_id "
            + "                          WHERE  p.voided = 0 "
            + "                                 AND e.voided = 0 "
            + "                                 AND o.voided = 0 "
            + "                                 AND o.concept_id = ${6332} "
            + "                                 AND o.value_coded = ${1065} "
            + "                                 AND e.encounter_type IN ( ${5}, ${6} ) "
            + "                                 AND e.encounter_datetime BETWEEN Date_sub( "
            + "                                     :onOrBefore, "
            + "                                     INTERVAL 18 month) "
            + "                                     AND "
            + "                                     :onOrBefore "
            + "                                 AND e.location_id = :location "
            + "                                 AND pe.gender = 'F' "
            + "                          GROUP  BY p.patient_id "
            + "                          UNION "
            + "                          SELECT p.patient_id, "
            + "                                 Max(e.encounter_datetime) AS last_date "
            + "                          FROM   patient p "
            + "                                 INNER JOIN person pe "
            + "                                         ON p.patient_id = pe.person_id "
            + "                                 INNER JOIN encounter e "
            + "                                         ON p.patient_id = e.patient_id "
            + "                                 INNER JOIN obs o "
            + "                                         ON e.encounter_id = o.encounter_id "
            + "                          WHERE  p.voided = 0 "
            + "                                 AND pe.voided = 0 "
            + "                                 AND e.voided = 0 "
            + "                                 AND o.voided = 0 "
            + "                                 AND o.concept_id = ${6334} "
            + "                                 AND o.value_coded = ${6332} "
            + "                                 AND e.encounter_type IN ( ${5}, ${6} ) "
            + "                                 AND e.encounter_datetime BETWEEN Date_sub( "
            + "                                     :onOrBefore, "
            + "                                     INTERVAL 18 month) "
            + "                                     AND "
            + "                                     :onOrBefore "
            + "                                 AND e.location_id = :location "
            + "                                 AND pe.gender = 'F' "
            + "                          GROUP  BY p.patient_id "
            + "                          UNION "
            + "                          SELECT pp.patient_id, "
            + "                                 Max(ps.start_date) AS last_date "
            + "                          FROM   patient_program pp "
            + "                                 INNER JOIN person pe "
            + "                                         ON pp.patient_id = pe.person_id "
            + "                                 INNER JOIN patient_state ps "
            + "                                         ON "
            + "                                 pp.patient_program_id = ps.patient_program_id "
            + "                          WHERE  pp.program_id = ${8} "
            + "                                 AND ps.state = ${27} "
            + "                                 AND pp.voided = 0 "
            + "                                 AND ps.voided = 0 "
            + "                                 AND ps.start_date BETWEEN "
            + "                                     Date_sub(:onOrBefore, INTERVAL "
            + "                                     18 month) "
            + "                                     AND "
            + "                                     :onOrBefore "
            + "                                 AND pp.location_id = :location "
            + "                                 AND pe.gender = 'F' "
            + "                          GROUP  BY pp.patient_id "
            + "                          UNION "
            + "                          SELECT p.patient_id, "
            + "                                 Max(hist.value_datetime) AS last_date "
            + "                          FROM   patient p "
            + "                                 INNER JOIN person pe "
            + "                                         ON p.patient_id = pe.person_id "
            + "                                 INNER JOIN encounter e "
            + "                                         ON p.patient_id = e.patient_id "
            + "                                 INNER JOIN obs o "
            + "                                         ON e.encounter_id = o.encounter_id "
            + "                                 INNER JOIN obs hist "
            + "                                         ON e.encounter_id = hist.encounter_id "
            + "                          WHERE  p.voided = 0 "
            + "                                 AND e.voided = 0 "
            + "                                 AND o.voided = 0 "
            + "                                 AND o.concept_id = ${6332} "
            + "                                 AND o.value_coded = ${1065} "
            + "                                 AND hist.concept_id = ${1190} "
            + "                                 AND pe.gender = 'F' "
            + "                                 AND hist.value_datetime IS NOT NULL "
            + "                                 AND hist.value_datetime BETWEEN Date_sub( "
            + "                                     :onOrBefore, "
            + "                                     INTERVAL 18 month) "
            + "                                     AND "
            + "                                     :onOrBefore "
            + "                          GROUP  BY p.patient_id) AS breastfeeding "
            + "                  GROUP  BY patient_id) max_breastfeeding "
            + "              ON max_pregnant.patient_id = max_breastfeeding.patient_id "
            + "WHERE  ( max_pregnant.pregnancy_date IS NOT NULL "
            + "         AND max_pregnant.pregnancy_date >= "
            + "       max_breastfeeding.breastfeeding_date ) "
            + "        OR ( max_breastfeeding.breastfeeding_date IS NULL )";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * Include all patients with the most recent VL Result documented in the Laboratory Form or Ficha
   * de Seguimento (Adulto or Pediatria) or Ficha Clínica or Ficha Resumo or e-Lab form by end of
   * reporting period
   *
   * @param selectDatetime Date or Patient id flag
   * @return {@link String}
   */
  private String mostRecentVL(boolean selectDatetime) {

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        "  FROM patient p        "
            + "   INNER JOIN encounter e ON p.patient_id = e.patient_id  "
            + "   INNER JOIN obs o ON o.encounter_id = e.encounter_id  "
            + "   INNER JOIN ( "
            + "            SELECT max_vl.patient_id, MAX(max_vl.recent_date) recent_datetime  FROM( "
            + "   SELECT p.patient_id,MAX(e.encounter_datetime) recent_date  "
            + "         FROM patient p  "
            + "                              INNER JOIN encounter e ON p.patient_id = e.patient_id  "
            + "                              INNER JOIN obs o ON o.encounter_id = e.encounter_id  "
            + "                              WHERE e.encounter_type IN (${13},${6},${9},${51})  "
            + "                              AND e.encounter_datetime <= :endDate  "
            + "                              AND o.concept_id IN (${856},${1305})                                "
            + "                              AND e.location_id = :location  "
            + "                              AND e.voided = 0  "
            + "                              AND o.voided = 0  "
            + "                              AND p.voided = 0  "
            + "                              GROUP BY p.patient_id  "
            + "                               "
            + "      UNION  "
            + "                                "
            + "                             SELECT p.patient_id, MAX(o.obs_datetime) recent_date  "
            + "                              FROM patient p   "
            + "                              INNER JOIN encounter e ON e.patient_id = p.patient_id  "
            + "                              INNER JOIN obs o ON o.encounter_id = e.encounter_id  "
            + "                              WHERE e.encounter_type = ${53}  "
            + "                              AND o.concept_id IN (${856},${1305})                                 "
            + "                              AND o.obs_datetime <= :endDate  "
            + "                              AND e.location_id = :location  "
            + "                              AND e.voided = 0  "
            + "                              AND p.voided = 0  "
            + "                              AND o.voided = 0     "
            + "                              GROUP BY p.patient_id  ) max_vl  GROUP BY max_vl.patient_id                "
            + "                              ) AS recent_vl ON recent_vl.patient_id = p.patient_id "
            + "   WHERE        e.location_id = :location  "
            + "                              AND e.voided = 0  "
            + "                              AND o.voided = 0  "
            + "                              AND p.voided = 0  "
            + "       AND(( "
            + "        e.encounter_datetime = recent_vl.recent_datetime "
            + "        AND e.encounter_type IN (${13},${6},${9},${51})                             "
            + "        AND o.concept_id = ${856}    "
            + "        AND o.value_numeric  IS NOT NULL)  "
            + "                              OR "
            + "        (e.encounter_type = ${53}                             "
            + "          AND o.concept_id = ${856}    "
            + "          AND o.value_numeric IS NOT NULL "
            + "        AND  o.obs_datetime = recent_vl.recent_datetime ) "
            + "                              ) ";

    String fromSQL = String.format(query);
    String selectSQL =
        selectDatetime
            ? "SELECT recent_vl.patient_id, recent_vl.recent_datetime ".concat(fromSQL)
            : "SELECT recent_vl.patient_id ".concat(fromSQL);

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    return stringSubstitutor.replace(selectSQL);
  }
}
