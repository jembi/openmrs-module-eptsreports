/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.eptsreports.reporting.library.cohorts;

import static org.openmrs.module.reporting.evaluation.parameter.Mapped.mapStraightThrough;

import java.util.*;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.library.queries.CommonQueries;
import org.openmrs.module.eptsreports.reporting.library.queries.TxRttQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TxRttCohortQueries {

  private HivMetadata hivMetadata;

  private GenericCohortQueries genericCohortQueries;

  private TxCurrCohortQueries txCurrCohortQueries;

  private CommonCohortQueries commonCohortQueries;

  private HivCohortQueries hivCohortQueries;

  private ResumoMensalCohortQueries resumoMensalCohortQueries;

  private CommonMetadata commonMetadata;

  private TxNewCohortQueries txNewCohortQueries;

  private AgeCohortQueries ageCohortQueries;

  private CommonQueries commonQueries;

  private final String artStartPeriod = " '2023-12-21' ";

  private final String DEFAULT_MAPPING =
      "startDate=${startDate},endDate=${endDate},location=${location}";

  private final String previousReportingPeriod = "endDate=${endDate},location=${location}";

  @Autowired
  public TxRttCohortQueries(
      HivMetadata hivMetadata,
      GenericCohortQueries genericCohortQueries,
      TxCurrCohortQueries txCurrCohortQueries,
      CommonCohortQueries commonCohortQueries,
      ResumoMensalCohortQueries resumoMensalCohortQueries,
      HivCohortQueries hivCohortQueries,
      CommonMetadata commonMetadata,
      TxNewCohortQueries txNewCohortQueries,
      AgeCohortQueries ageCohortQueries,
      CommonQueries commonQueries) {
    this.hivMetadata = hivMetadata;
    this.genericCohortQueries = genericCohortQueries;
    this.txCurrCohortQueries = txCurrCohortQueries;
    this.commonCohortQueries = commonCohortQueries;
    this.resumoMensalCohortQueries = resumoMensalCohortQueries;
    this.hivCohortQueries = hivCohortQueries;
    this.commonMetadata = commonMetadata;
    this.txNewCohortQueries = txNewCohortQueries;
    this.ageCohortQueries = ageCohortQueries;
    this.commonQueries = commonQueries;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * All patients (adults and children) who at ANY clinical contact (clinical consultation or drugs
   * pick up <b>[Encounter Type Ids = 6,9,${}1}8,52]</b>) registered during the reporting period had
   * a delay greater than 28/30 days from the last scheduled/expected, which may have happened
   * during or prior to the reporting period period
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getAllPatientsWhoMissedAppointmentBy28Or30DaysButLaterHadVisit() {
    return genericCohortQueries.generalSql(
        "Having visit 30 days later",
        TxRttQueries.getAllPatientsWhoMissedPreviousAppointmentBy28Days(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId(),
            hivMetadata.getReturnVisitDateConcept().getConceptId(),
            hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId(),
            hivMetadata.getArtDatePickupMasterCard().getConceptId()));
  }

  /**
   * The system will generate the TX_RTT indicator numerator as the number of patients (adults and
   * children) who ever initiated ART by the end of the previous reporting period (TX_RTT_FR4) and
   * who:
   *
   * <ul>
   *   <li><b>1)</b> experienced IIT by end of previous reporting period (TX_RTT_FR5) and
   *   <li><b>2)</b> returned to ARV treatment during the reporting period (TX_RTT_FR6) and
   *   <li><b>3)</b> remained on treatment (TX CURR) by end of reporting period,
   *   <li><b>4)</b> with the specified disaggregation (TX_RTT_FR3).
   * </ul>
   *
   * <p>The system will exclude:
   *
   * <ul>
   *   <li>Patients who were Transferred-In (TRF IN) during the reporting period. (For more details
   *       refer to the TRF_IN Requirements document.)
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getRTTComposition() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    cd.addSearch(
        "initiatedPreviousPeriod",
        EptsReportUtils.map(
            getPatientsWhoEverInitiatedTreatment(),
            "endDate=${startDate-1d},location=${location}"));

    cd.addSearch(
        "LTFU",
        EptsReportUtils.map(
            getITTOrLTFUPatients(28), "onOrBefore=${startDate-1d},location=${location}"));

    cd.addSearch(
        "returned",
        EptsReportUtils.map(getPatientsReturnedTreatmentDuringReportingPeriod(), DEFAULT_MAPPING));

    cd.addSearch(
        "txcurr",
        EptsReportUtils.map(
            txCurrCohortQueries.getTxCurrCompositionCohort("txcurr", true),
            "onOrBefore=${endDate},location=${location}"));

    cd.addSearch(
        "transferredOut",
        EptsReportUtils.map(
            commonCohortQueries.getMohTransferredOutPatientsByEndOfPeriod(),
            "onOrBefore=${startDate-1d},location=${location}"));

    cd.addSearch(
        "transferredIn",
        EptsReportUtils.map(
            this.getTransferredInPatients(),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));

    cd.setCompositionString(
        "(initiatedPreviousPeriod AND (LTFU AND NOT transferredOut) AND returned AND txcurr) AND NOT transferredIn");

    return cd;
  }

  /**
   * Filter all patients who returned to the treatment during the reporting period following the
   * criterias below:
   *
   * <ul>
   *   <li>At least one Drugs Pick up registered in FILA during the reporting period (Encounter Type
   *       18, and encounter_datetime>= startDate and <=endDate) OR
   *   <li>At least one Drugs Pick up registered in MasterCard-Recepção/Levantoy ARV, during the
   *       reporting period (Encounter Type 52, and “Levantou ARV”- concept ID 23865”= “Yes”
   *       (concept id 1065) and “Data de Levantamento” (concept Id 23866 value_datetime>= startDate
   *       and <=endDate)
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsReturnedTreatmentDuringReportingPeriod() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition fila =
        getPatientsWithFilaOrFichaOrMasterCardPickup(
            Arrays.asList(hivMetadata.getARVPharmaciaEncounterType()));

    CohortDefinition drugPickUp =
        getPatientsWithFilaOrFichaOrMasterCardPickup(
            Arrays.asList(hivMetadata.getMasterCardDrugPickupEncounterType()),
            hivMetadata.getArtPickupConcept(),
            hivMetadata.getYesConcept(),
            hivMetadata.getArtDatePickupMasterCard());

    cd.addSearch("fila", EptsReportUtils.map(fila, DEFAULT_MAPPING));

    cd.addSearch("drugPickUp", EptsReportUtils.map(drugPickUp, DEFAULT_MAPPING));

    cd.setCompositionString("fila OR drugPickUp");

    return cd;
  }

  public CohortDefinition getPatientsWithFilaOrFichaOrMasterCardPickup(
      List<EncounterType> encounterTypes, Concept... conceptIds) {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    StringBuilder builder = new StringBuilder();

    builder.append(" SELECT p.patient_id ");
    builder.append(" FROM patient p ");
    builder.append("    INNER JOIN encounter e  ");
    builder.append("        on e.patient_id = p.patient_id ");
    if (conceptIds.length == 3) {
      builder.append("  INNER JOIN obs o1");
      builder.append("      on e.encounter_id = o1.encounter_id ");
      builder.append("  INNER JOIN obs o2 ");
      builder.append("      on e.encounter_id = o2.encounter_id ");
    }
    builder.append(" WHERE  ");
    builder.append("    p.voided = 0  ");
    builder.append(" AND   e.voided = 0  ");
    if (encounterTypes.size() > 1) {
      builder.append("   AND e.encounter_type IN (%s,%s) ");
    } else {
      builder.append("   AND e.encounter_type = %s ");
    }
    if (conceptIds.length == 3) {
      builder.append(" AND o1.voided= 0 ");
      builder.append(" AND o2.voided= 0 ");
      builder.append(" AND (o1.concept_id = %s AND o1.value_coded = %s) ");
      builder.append(
          " AND (o2.concept_id = %s AND o2.value_datetime BETWEEN :startDate AND :endDate) ");
    } else {
      builder.append(" AND e.encounter_datetime  ");
      builder.append("        BETWEEN :startDate AND :endDate ");
    }
    builder.append("   AND e.location_id = :location ");
    String query = builder.toString();

    String formattedQuery = null;

    if (conceptIds.length == 3) {
      formattedQuery =
          String.format(
              query,
              encounterTypes.get(0).getEncounterTypeId(),
              conceptIds[0].getConceptId(),
              conceptIds[1].getConceptId(),
              conceptIds[2].getConceptId());
    } else {
      formattedQuery =
          encounterTypes.size() > 1
              ? String.format(
                  query,
                  encounterTypes.get(0).getEncounterTypeId(),
                  encounterTypes.get(1).getEncounterTypeId())
              : String.format(query, encounterTypes.get(0).getEncounterTypeId());
    }

    cd.setQuery(formattedQuery);

    return cd;
  }

  public CohortDefinition getITTOrLTFUPatients(int numDays) {
    CompositionCohortDefinition definition = new CompositionCohortDefinition();

    definition.addSearch(
        "31",
        mapStraightThrough(
            txCurrCohortQueries.getPatientHavingLastScheduledDrugPickupDateDaysBeforeEndDate(
                numDays)));

    definition.addSearch("32", mapStraightThrough(getSecondPartFromITT()));

    definition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));
    definition.setCompositionString("31 OR 32");

    return definition;
  }

  public CohortDefinition getSecondPartFromITT() {

    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("patientWithoutScheduledDrugPickupDateMasterCardAmdArtPickup");
    definition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());

    String query =
        "SELECT final.patient_id"
            + " FROM( "
            + " select totaltotal.patient_id "
            + " from ( "
            + "        SELECT total.patient_id FROM"
            + "            ("
            + "                SELECT     pat1.patient_id, Max(enc1.encounter_datetime) AS encounter_datetime"
            + "                FROM  patient pat1 "
            + "                    INNER JOIN encounter enc1 "
            + "                        ON         pat1.patient_id=enc1.patient_id "
            + "                    "
            + "                WHERE enc1.encounter_datetime<=:onOrBefore"
            + "                    AND pat1.voided=0 "
            + "                    AND enc1.voided=0 "
            + "                    AND enc1.location_id=:location "
            + "                    AND enc1.encounter_type IN (${18})"
            + "                GROUP BY   pat1.patient_id"
            + "            ) AS total"
            + "            LEFT JOIN"
            + "                    ("
            + "                        SELECT p.patient_id "
            + "                        FROM patient p"
            + "                            INNER JOIN encounter e"
            + "                                ON e.patient_id =p.patient_id"
            + "                            INNER JOIN obs o"
            + "                                ON o.encounter_id =e.encounter_id"
            + "                        WHERE"
            + "                            p.voided = 0"
            + "                            AND e.voided = 0"
            + "                            AND o.voided = 0"
            + "                            AND e.encounter_type IN (${18})"
            + "                            AND encounter_datetime<=:onOrBefore"
            + "                            AND e.location_id=:location "
            + "                            AND o.concept_id = ${5096}"
            + "                        UNION"
            + "                        SELECT p.patient_id "
            + "                        FROM patient p"
            + "                            INNER JOIN encounter e"
            + "                                ON e.patient_id =p.patient_id"
            + "                            INNER JOIN obs o"
            + "                                ON o.encounter_id =e.encounter_id"
            + "                        WHERE"
            + "                            p.voided = 0"
            + "                            AND e.voided = 0"
            + "                            AND o.voided = 0"
            + "                            AND e.encounter_type IN (${18})"
            + "                            AND encounter_datetime<=:onOrBefore"
            + "                            AND e.location_id=:location "
            + "                            AND o.concept_id = ${5096}"
            + "                            AND o.value_datetime IS NOT NULL"
            + "                    ) right1"
            + "            ON total.patient_id = right1.patient_id  "
            + "        WHERE "
            + "            right1.patient_id IS NULL"
            + ") totaltotal group by totaltotal.patient_id HAVING count(totaltotal.patient_id) >=2"
            + ") AS final "
            + " WHERE final.patient_id NOT  IN ("
            + "    SELECT p.patient_id"
            + "    FROM  patient p"
            + "        INNER JOIN encounter e "
            + "            ON e.patient_id = p.patient_id"
            + "        INNER JOIN obs o "
            + "            ON e.encounter_id = o.encounter_id"
            + "    "
            + "    WHERE p.voided = 0"
            + "        AND e.voided = 0"
            + "        AND o.voided = 0"
            + "        AND e.encounter_type = ${52}"
            + "        AND e.location_id = :location"
            + "        AND o.value_datetime <= :onOrBefore"
            + "        AND o.concept_id = ${23866}"
            + "       "
            + "        )";
    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    definition.setQuery(stringSubstitutor.replace(query));

    return definition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * Number Patients who were transferred in from other facility defined as following: - All
   * patients enrolled in ART Program and who have been registered with the following state
   * TRANSFERRED IN FROM OTHER FACILITY - All patients who have filled “Transferido de outra US” and
   * checked “Em TARV” in Ficha Resumo with MasterCard file opening Date during reporting period -
   * But excluding patients who were included in Tx CURR of previous reporting period
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getTransferredInPatients() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Number of Transferred In patients by end of current period");
    cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
    cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition transferredIn = resumoMensalCohortQueries.getTransferredInPatients(false);

    CohortDefinition txCurr = txCurrCohortQueries.getTxCurrCompositionCohort("txCurr", true);

    CohortDefinition transferredOut = getTrfOut();

    CohortDefinition homeVisitTrfOut =
        txCurrCohortQueries.getPatientsTransferedOutInLastHomeVisitCard();

    CohortDefinition drugPickUp = getPatientsReturnedTreatmentDuringReportingPeriod();

    String mappingsTrfIn = "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}";
    String mappingsCurr = "onOrBefore=${onOrBefore-3m},location=${location}";
    String mappingsTrfOut = "startDate=${onOrAfter},location=${location}";
    String mappingsHomeVisitTrfOut = "onOrBefore=${onOrAfter-1d},location=${location}";
    String mappingsPickUplVisit =
        "startDate=${onOrAfter},endDate=${onOrBefore},location=${location}";

    cd.addSearch("transferredIn", EptsReportUtils.map(transferredIn, mappingsTrfIn));
    cd.addSearch("txCurr", EptsReportUtils.map(txCurr, mappingsCurr));
    cd.addSearch("transferredOut", EptsReportUtils.map(transferredOut, mappingsTrfOut));
    cd.addSearch("homeVisitTrfOut", EptsReportUtils.map(homeVisitTrfOut, mappingsHomeVisitTrfOut));
    cd.addSearch("drugPickUp", EptsReportUtils.map(drugPickUp, mappingsPickUplVisit));

    cd.setCompositionString("(transferredIn OR transferredOut) AND drugPickUp AND NOT txCurr");

    return cd;
  }

  public CohortDefinition getTreatmentInterruptionOfXDaysBeforeReturningToTreatment(
      Integer minDays, Integer maxDays) {
    SqlCohortDefinition definition = new SqlCohortDefinition();

    definition.setName("patientHavingLastScheduledDrugPickupDate");

    definition.setQuery(
        TxRttQueries.getTreatmentInterruptionOfXDaysBeforeReturningToTreatmentQuery(
            hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId(),
            hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId(),
            hivMetadata.getArtDatePickupMasterCard().getConceptId(),
            hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId(),
            minDays,
            maxDays));

    definition.addParameter(new Parameter("startDate", "startDate", Date.class));
    definition.addParameter(new Parameter("endDate", "endDate", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));

    return definition;
  }

  /**
   * <b>1. <\b> Experienced treatment interruption of <3 months (less than 90 days) before returning
   * to treatment:
   *
   * <p>Main composition: ( AA or AB) and AC and B and C and Date from AA (the most recent one)
   * minus Date from B (the earliest one) should be < 90 days
   *
   * <p><b>2. <\b> Experienced treatment interruption of 3-5 months (>= 90 days and <180 days)
   * before returning to treatment:
   *
   * <p>Main composition: ( AA or AB) and AC and B and C and Date from AA (the most recent one)
   * minus Date from B (the earliest one) should be >= 90 days and <180
   *
   * <p><b>3. <\b> Experienced treatment interruption of 6+ months (>=180 days) before returning to
   * treatment
   *
   * <p>Main composition: ( AA or AB) and AC and B and C and Date from AA (the most recent one)
   * minus Date from B (the earliest one) should be >= 180
   *
   * @return CohortDefinition
   */
  public CohortDefinition treatmentInterruptionOfXDays(Integer minDays, Integer maxDays) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Duration of treatment interruption before returning to treatment");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    cd.addSearch("txRtt", EptsReportUtils.map(getRTTComposition(), DEFAULT_MAPPING));

    cd.addSearch(
        "AAB",
        EptsReportUtils.map(
            getTreatmentInterruptionOfXDaysBeforeReturningToTreatment(minDays, maxDays),
            DEFAULT_MAPPING));

    cd.setCompositionString("txRtt AND AAB");

    return cd;
  }

  /**
   * All patients whose earliest ART start date from pick-up and clinical sources (NEW_FR4.1) falls
   * before (<) 21 December 2023 and this date falls by the end of the previous reporting period.
   * (This criterion will identify patients who started ART prior to 21-Dec-23 according the
   * previous TX_NEW requirements definition, and therefore should be included as TX_NEW in
   * reporting periods prior to 21-Dec-23.)
   */
  public CohortDefinition getPatientsArtStartDateBeforePeriod() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("All patients whose earliest ART start date from pick-up and clinical sources");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    String query =
        "       SELECT patient_id "
            + " FROM ( "
            + commonQueries.getARTStartDate(true)
            + "       ) start "
            + " WHERE start.first_pickup < "
            + artStartPeriod
            + " AND start.first_pickup <= :endDate ";

    cd.setQuery(query);
    return cd;
  }

  /**
   * All patients whose earliest ART start date (NEW_FR4.1) falls on or after (>=) 21 December 2023.
   */
  public CohortDefinition getPatientsArtStartDateAfterPeriod() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("All patients whose earliest ART start date from pick-up and clinical sources");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    String query =
        "       SELECT patient_id "
            + " FROM ( "
            + commonQueries.getARTStartDate(true)
            + "       ) start "
            + " WHERE start.first_pickup >= "
            + artStartPeriod
            + " AND start.first_pickup <= :endDate";

    cd.setQuery(query);
    return cd;
  }

  /**
   * Patients whose first ever drug pick-up date between the following sources falls during the
   * reporting period:
   *
   * <ul>
   *   <li>Drug pick-up date registered on (FILA)
   *   <li>Drug pick-up date registered on (Recepção Levantou ARV) – Master Card
   * </ul>
   *
   * @return String
   */
  public CohortDefinition getPatientsFirstDrugPickup() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patient’s earliest drug pick-up");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());

    String query =
        "       SELECT patient_id "
            + " FROM ( "
            + "        SELECT first.patient_id, "
            + "               Min(first.pickup_date) AS first_pickup_ever "
            + "        FROM   ("
            + CommonQueries.getFirstDrugPickOnFilaOrRecepcaoLevantouQuery()
            + "        ) first "
            + "          GROUP  BY first.patient_id "
            + "      ) start "
            + " WHERE start.first_pickup_ever >= "
            + artStartPeriod;

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   * <b>NEW_FR4: Patients who initiated ART during the reporting period</b>
   *
   * <ul>
   *   <li>All patients whose earliest ART start date from pick-up and clinical sources
   *       (TX_RTT_FR4.1) falls before (<) 21 December 2023 and this date falls by the end of the
   *       previous reporting period. (This criterion will identify patients who started ART prior
   *       to 21-Dec-23 according the previous TX_NEW requirements definition, and therefore should
   *       be included as TX_NEW in reporting periods prior to 21-Dec-23.)
   *   <li>All patients whose earliest ART start date (TX_RTT_FR4.1) falls on or after (>=) 21
   *       December 2023.
   *       <p><b>AND</b> whose first ever drug pick-up date between the following sources falls by
   *       the end of the previous reporting period:
   * </ul>
   *
   * <ul>
   *   <li>Drug pick-up date registered on (FILA)
   *   <li>Drug pick-up date registered on (Recepção Levantou ARV) – Master Card
   * </ul>
   *
   * <p>AND excluding patients with an earliest ART start date from pick-up and clinical sources
   * (TX_RTT_FR4.1) that falls before (<) 21 December 2023.
   *
   * <p><b>Note:</b> Ensure that the drug pick-up is the patient’s first ever pick-up at the health
   * facility.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoEverInitiatedTreatment() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Patients who initiated ART during the reporting period");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition earliestArtStartDateBeforePeriod = getPatientsArtStartDateBeforePeriod();
    CohortDefinition earliestArtStartDateAfterPeriod = getPatientsArtStartDateAfterPeriod();
    CohortDefinition firstDrugPickUpAfterPeriod = getPatientsFirstDrugPickup();

    cd.addSearch(
        "earliestArtStartDateBeforePeriod",
        EptsReportUtils.map(earliestArtStartDateBeforePeriod, previousReportingPeriod));
    cd.addSearch(
        "earliestArtStartDateAfterPeriod",
        EptsReportUtils.map(earliestArtStartDateAfterPeriod, previousReportingPeriod));
    cd.addSearch(
        "firstDrugPickUpAfterPeriod",
        EptsReportUtils.map(firstDrugPickUpAfterPeriod, previousReportingPeriod));

    cd.setCompositionString(
        "earliestArtStartDateBeforePeriod OR (earliestArtStartDateAfterPeriod AND firstDrugPickUpAfterPeriod)");
    return cd;
  }

  /**
   * Absolute CD4 Count
   *
   * @return CohortDefinition
   */
  public CohortDefinition getCd4Result(
      AdvancedDiseaseAndTBCascadeCohortQueries.Cd4CountComparison cd4CountComparison) {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patients with CD4 Result");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "       INNER JOIN (SELECT e.patient_id,MIN(DATE(cd4.cd4_date)) cd4_date "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                          INNER JOIN ( "
            + "        SELECT first.patient_id, "
            + "               Min(first.pickup_date) AS first_pickup_ever "
            + "        FROM   ("
            + CommonQueries.getFirstDrugPickOnFilaOrRecepcaoLevantouQuery()
            + "        ) first "
            + "          GROUP  BY first.patient_id "
            + "                 ) returned "
            + "  ON returned.patient_id = e.patient_id "
            + "  INNER JOIN (SELECT e.patient_id, DATE(e.encounter_datetime) cd4_date "
            + "            FROM   encounter e "
            + "            INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "            WHERE  e.encounter_type IN ( ${6}, ${13}, ${51} ) "
            + "            AND e.location_id = :location "
            + "            AND DATE(e.encounter_datetime) <= :endDate "
            + "            AND o.concept_id = ${1695} "
            + "            AND e.voided = 0 "
            + "            AND o.voided = 0 "
            + "            UNION "
            + "            SELECT e.patient_id, DATE(o.obs_datetime) AS cd4_date "
            + "            FROM   encounter e "
            + "            INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "            WHERE  e.encounter_type = ${53} "
            + "            AND e.location_id = :location "
            + "            AND e.voided = 0 "
            + "            AND o.voided = 0 "
            + "            AND o.concept_id IN ( ${1695} ) "
            + "            AND o.obs_datetime <= :endDate"
            + "     ) cd4 ON cd4.patient_id = e.patient_id "
            + "   WHERE  e.voided = 0 "
            + "   AND o.voided = 0 "
            + "   AND e.location_id = :location "
            + "   AND o.value_numeric IS NOT NULL "
            + "   AND ( ( DATE(e.encounter_datetime) BETWEEN DATE_SUB(returned.first_pickup_ever, INTERVAL 30 day) AND DATE_ADD(returned.first_pickup_ever, INTERVAL 28 day) "
            + "             AND e.encounter_type IN ( ${6}, ${13}, ${51} ) AND o.concept_id = ${1695} ) "
            + "          OR ( DATE(o.obs_datetime) BETWEEN DATE_SUB(returned.first_pickup_ever, INTERVAL 30 day) AND DATE_ADD(returned.first_pickup_ever, INTERVAL 28 day) "
            + "   AND e.encounter_type = ${53} AND o.concept_id = ${1695} ) ) "
            + "   GROUP  BY e.patient_id) min_cd4 ON min_cd4.patient_id = p.patient_id "
            + " WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.location_id = :location "
            + "       AND  ".concat(cd4CountComparison.getProposition())
            + "       AND ( ( DATE(e.encounter_datetime) = min_cd4.cd4_date AND e.encounter_type IN ( ${6}, ${13}, ${51} ) AND o.concept_id = ${1695}  ) "
            + "              OR ( DATE(o.obs_datetime) = min_cd4.cd4_date AND e.encounter_type = ${53} AND o.concept_id IN (${1695})  ) "
            + "             ) "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   * @param cd4 - Absolute CD4 result
   * @param minAge minimum age of patient base on effective date
   * @param maxAge maximum age of patent base on effective date
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWithCd4AndAge(
      AdvancedDiseaseAndTBCascadeCohortQueries.Cd4CountComparison cd4,
      Integer minAge,
      Integer maxAge) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Cd4 And Age");
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));

    CohortDefinition getCd4Result = getCd4Result(cd4);
    CohortDefinition age = ageCohortQueries.createXtoYAgeCohort("Age", minAge, maxAge);

    cd.addSearch(
        "getCd4Result",
        EptsReportUtils.map(getCd4Result, "endDate=${endDate},location=${location}"));

    cd.addSearch("age", EptsReportUtils.map(age, "effectiveDate=${endDate}"));

    cd.setCompositionString("getCd4Result AND age");

    return cd;
  }

  /**
   * <b>TX_RTT_FR11 - Patient Disaggregation- Not Eligible for CD4</b>
   *
   * <p>The system will identify patients who are included in the TX_RTT numerator (TX_RTT_FR2) for
   * the Not Eligible for CD4 disaggregation as follows:
   *
   * <ul>
   *   <li>Patients whose difference between the patient ART Restart date (TX_RTT_FR6) and the most
   *       recent date (by end of previous reporting period) between last scheduled drug pickup date
   *       (FILA) and last ART pickup date (Recepção – Levantou ARV) + 30 days is less than 60 days.
   * </ul>
   */
  public CohortDefinition getPatientsNotEligibleForCd4() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patients Not Eligible for CD4");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN (SELECT earliest.patient_id, "
            + "                          Min(earliest.pick_up) AS pick_up_date "
            + "                   FROM   (SELECT p.patient_id, "
            + "                                  Min(e.encounter_datetime) AS pick_up "
            + "                           FROM   patient p "
            + "                                  INNER JOIN encounter e "
            + "                                          ON e.patient_id = p.patient_id "
            + "                           WHERE  p.voided = 0 "
            + "                                  AND e.voided = 0 "
            + "                                  AND e.encounter_type = ${18} "
            + "                                  AND e.encounter_datetime BETWEEN "
            + "                                      :startDate AND :endDate "
            + "                                  AND e.location_id = :location "
            + "                           GROUP  BY p.patient_id "
            + "                           UNION "
            + "                           SELECT p.patient_id, "
            + "                                  Min(o.value_datetime) AS pick_up "
            + "                           FROM   patient p "
            + "                                  INNER JOIN encounter e "
            + "                                          ON e.patient_id = p.patient_id "
            + "                                  INNER JOIN obs o "
            + "                                          ON e.encounter_id = o.encounter_id "
            + "                           WHERE  p.voided = 0 "
            + "                                  AND e.voided = 0 "
            + "                                  AND e.encounter_type = ${52} "
            + "                                  AND o.voided = 0 "
            + "                                  AND ( o.concept_id = ${23866} "
            + "                                        AND o.value_datetime BETWEEN "
            + "                                            :startDate AND :endDate "
            + "                                      ) "
            + "                                  AND e.location_id = :location "
            + "                           GROUP  BY p.patient_id)earliest "
            + "                   GROUP  BY earliest.patient_id) restarted "
            + "               ON restarted.patient_id = p.patient_id "
            + "       INNER JOIN (SELECT most_recent.patient_id, "
            + "                          Max(most_recent.value_datetime) AS "
            + "                          final_encounter_date "
            + "                   FROM   (SELECT p.patient_id, "
            + "                                  Max(o.value_datetime) value_datetime "
            + "                           FROM   patient p "
            + "                                  INNER JOIN encounter e "
            + "                                          ON e.patient_id = p.patient_id "
            + "                                  INNER JOIN obs o "
            + "                                          ON o.encounter_id = e.encounter_id "
            + "                           WHERE  p.voided = 0 "
            + "                                  AND e.voided = 0 "
            + "                                  AND o.voided = 0 "
            + "                                  AND e.encounter_type = ${18} "
            + "                                  AND o.concept_id = ${5096} "
            + "                                  AND o.value_datetime < :startDate "
            + "                                  AND e.location_id = :location "
            + "                           GROUP  BY p.patient_id "
            + "                           UNION "
            + "                           SELECT p.patient_id, "
            + "                                  Date_add(Max(o.value_datetime), "
            + "                                  INTERVAL 30 day) "
            + "                                  value_datetime "
            + "                           FROM   patient p "
            + "                                  INNER JOIN encounter e "
            + "                                          ON e.patient_id = p.patient_id "
            + "                                  INNER JOIN obs o "
            + "                                          ON o.encounter_id = e.encounter_id "
            + "                           WHERE  p.voided = 0 "
            + "                                  AND e.voided = 0 "
            + "                                  AND o.voided = 0 "
            + "                                  AND e.encounter_type = ${52} "
            + "                                  AND o.concept_id = ${23866} "
            + "                                  AND o.value_datetime < :startDate "
            + "                                  AND e.location_id = :location "
            + "                           GROUP  BY p.patient_id) most_recent "
            + "                   GROUP  BY most_recent.patient_id) "
            + "                  most_recent_of_previous_period "
            + "               ON most_recent_of_previous_period.patient_id = p.patient_id "
            + "WHERE p.voided = 0 "
            + "AND  TIMESTAMPDIFF(DAY, most_recent_of_previous_period.final_encounter_date, "
            + "               restarted.pick_up_date) < 60";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   * @param minAge minimum age of patient base on effective date
   * @param maxAge maximum age of patent base on effective date
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsNotEligibleForCd4AndAge(Integer minAge, Integer maxAge) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Not Eligible For CD4 And Age");
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));

    CohortDefinition getPatientsNotEligibleForCd4 = getPatientsNotEligibleForCd4();
    CohortDefinition age = ageCohortQueries.createXtoYAgeCohort("Age", minAge, maxAge);

    cd.addSearch(
        "getPatientsNotEligibleForCd4",
        EptsReportUtils.map(getPatientsNotEligibleForCd4, DEFAULT_MAPPING));

    cd.addSearch("age", EptsReportUtils.map(age, "effectiveDate=${endDate}"));

    cd.setCompositionString("getPatientsNotEligibleForCd4 AND age");

    return cd;
  }

  public CohortDefinition getPatientsWhoAreNotEligibleForCd4() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Not Eligible for CD4");
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));

    CohortDefinition txRtt = getRTTComposition();
    CohortDefinition notEligibleCd4 = getPatientsNotEligibleForCd4AndAge(5, null);

    cd.addSearch("txRtt", EptsReportUtils.map(txRtt, DEFAULT_MAPPING));
    cd.addSearch("notEligibleCd4", EptsReportUtils.map(notEligibleCd4, DEFAULT_MAPPING));

    cd.setCompositionString("txRtt AND notEligibleCd4");

    return cd;
  }

  /**
   * <b>TX_RTT_FR8 - Patient Disaggregation- CD4 result <200/mm3 </b>
   *
   * <p>The system will identify patients who are included in the TX_RTT numerator (TX_RTT_FR2) for
   * the absolute CD4 <200/mm3 disaggregation as follows:
   *
   * <ul>
   *   <li>Patients with an absolute CD4 result <200/mm3 registered in the following sources:
   * </ul>
   *
   * <ul>
   *   <li>Last CD4 absolute value marked on Ficha Resumo OR
   *   <li>CD4 absolute result marked in the Investigações - Resultados Laboratoriais section on
   *       Ficha Clínica OR
   *   <li>CD4 absolute result registered on the Lab Form OR
   *   <li>CD4 absolute result registered on the e-Lab Form
   * </ul>
   *
   * <p>AND
   *
   * <ul>
   *   <li>Excluding patients <5 years of age (TX_RTT_FR12).
   *   <li>Excluding Patients Not Eligible for CD4 (TX_RTT_FR11).
   * </ul>
   *
   * <p>The system will consider the oldest CD4 result date falling between patient ART Restart Date
   * (TX_RTT_FR6) - 30 days and ART Restart Date (TX_RTT_FR6) + 28 days from the different sources
   * listed above for the evaluation of the result (< 200). <br>
   *
   * <p><b>Notes:</b>
   *
   * <ul>
   *   <li>For clients who have CD4 results ≥200/mm3 and <200/mm3 on the same, oldest date, the CD4
   *       result <200/mm3 will be prioritized.
   * </ul>
   */
  public CohortDefinition getPatientWithCd4ResultLessThan200() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Patients with CD4 Result Less than 200");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition txRtt = getRTTComposition();

    CohortDefinition cd4Under200AndAge =
        getPatientsWithCd4AndAge(
            AdvancedDiseaseAndTBCascadeCohortQueries.Cd4CountComparison.LessThanOrEqualTo200mm3,
            5,
            null);

    CohortDefinition cd4Above200AndAge =
        getPatientsWithCd4AndAge(
            AdvancedDiseaseAndTBCascadeCohortQueries.Cd4CountComparison.GreaterThanOrEqualTo200mm3,
            5,
            null);

    CohortDefinition notEligibleForCd4AndAge = getPatientsNotEligibleForCd4AndAge(5, null);

    cd.addSearch("txRtt", EptsReportUtils.map(txRtt, DEFAULT_MAPPING));
    cd.addSearch(
        "cd4Under200AndAge",
        EptsReportUtils.map(cd4Under200AndAge, "endDate=${endDate},location=${location}"));
    cd.addSearch(
        "cd4Above200AndAge",
        EptsReportUtils.map(cd4Above200AndAge, "endDate=${endDate},location=${location}"));
    cd.addSearch(
        "notEligibleForCd4AndAge", EptsReportUtils.map(notEligibleForCd4AndAge, DEFAULT_MAPPING));

    cd.setCompositionString(
        "(txRtt AND cd4Under200AndAge) AND NOT (cd4Above200AndAge OR notEligibleForCd4AndAge)");

    return cd;
  }

  /**
   * <b>TX_RTT_FR9 - Patient Disaggregation- CD4 result ≥200/mm3</b>
   *
   * <p>The system will identify patients who are included in the TX_RTT numerator (TX_RTT_FR2) for
   * the absolute CD4 ≥200/mm3 disaggregation as follows:
   *
   * <ul>
   *   <li>Patients with an absolute CD4 result ≥200/mm3 registered in the following sources:
   * </ul>
   *
   * <ul>
   *   <li>Last CD4 absolute value marked on Ficha Resumo OR
   *   <li>CD4 absolute result marked in the Investigações - Resultados Laboratoriais section on
   *       Ficha Clínica OR
   *   <li>CD4 absolute result registered on the Lab Form OR
   *   <li>CD4 absolute result registered on the e-Lab Form
   * </ul>
   *
   * <p>AND
   *
   * <ul>
   *   <li>Excluding patients <5 years of age (TX_RTT_FR12).
   *   <li>Excluding patients Not Eligible for CD4 (TX_RTT_FR11).
   * </ul>
   *
   * <p>The system will consider the oldest CD4 result date falling between patient ART Restart Date
   * (TX_RTT_FR6) - 30 days and ART Restart Date (TX_RTT_FR6) + 28 days from the different sources
   * listed above for the evaluation of the result (≥200).
   *
   * <p><b>Notes:</b>
   *
   * <ul>
   *   <li>For clients who have CD4 results ≥200/mm3 and <200/mm3 on the same, oldest date, the CD4
   *       result <200/mm3 will be prioritized.
   * </ul>
   */
  public CohortDefinition getPatientWithcd4ResultGreaterThan200() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Patients with CD4 Result Above 200");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition txRtt = getRTTComposition();

    CohortDefinition cd4Above200AndAge =
        getPatientsWithCd4AndAge(
            AdvancedDiseaseAndTBCascadeCohortQueries.Cd4CountComparison.GreaterThanOrEqualTo200mm3,
            5,
            null);

    CohortDefinition cd4Under200AndAge =
        getPatientsWithCd4AndAge(
            AdvancedDiseaseAndTBCascadeCohortQueries.Cd4CountComparison.LessThanOrEqualTo200mm3,
            5,
            null);

    CohortDefinition notEligibleForCd4AndAge = getPatientsNotEligibleForCd4AndAge(5, null);

    cd.addSearch("txRtt", EptsReportUtils.map(txRtt, DEFAULT_MAPPING));

    cd.addSearch(
        "cd4Above200AndAge",
        EptsReportUtils.map(cd4Above200AndAge, "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "cd4Under200AndAge",
        EptsReportUtils.map(cd4Under200AndAge, "endDate=${endDate},location=${location}"));
    cd.addSearch(
        "notEligibleForCd4AndAge", EptsReportUtils.map(notEligibleForCd4AndAge, DEFAULT_MAPPING));

    cd.setCompositionString(
        "(txRtt AND cd4Above200AndAge) AND NOT (cd4Under200AndAge OR notEligibleForCd4AndAge)");

    return cd;
  }

  /**
   * <b>TX_RTT_FR10 - Patient Disaggregation- Unknown CD4</b>
   *
   * <p>The system will identify patients with unknown CD4 as follows:
   *
   * <ul>
   *   <li>All patients in TX_RTT (TX_RTT_FR2) that are not included in the CD4 result <200/mm3
   *       (TX_RTT_FR8), the CD4 result ≥200/mm3 (TX_RTT_FR9) nor the Not Eligible for CD4
   *       (TX_RTT_FR11) disaggregates.
   * </ul>
   *
   * <p><b>Note:</b> All children <5 years of age that are included in the TX_RTT numerator will be
   * included in the “Unknown CD4” disaggregate.
   */
  public CohortDefinition getPatientWithUnknownCd4Result() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Patients with Unknown CD4 Result");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition txRtt = getRTTComposition();

    CohortDefinition cd4Under200AndAge =
        getPatientsWithCd4AndAge(
            AdvancedDiseaseAndTBCascadeCohortQueries.Cd4CountComparison.LessThanOrEqualTo200mm3,
            5,
            null);

    CohortDefinition cd4Above200AndAge =
        getPatientsWithCd4AndAge(
            AdvancedDiseaseAndTBCascadeCohortQueries.Cd4CountComparison.GreaterThanOrEqualTo200mm3,
            5,
            null);

    CohortDefinition notEligibleForCd4AndAge = getPatientsNotEligibleForCd4AndAge(5, null);

    cd.addSearch("txRtt", EptsReportUtils.map(txRtt, DEFAULT_MAPPING));
    cd.addSearch(
        "cd4Under200AndAge",
        EptsReportUtils.map(cd4Under200AndAge, "endDate=${endDate},location=${location}"));
    cd.addSearch(
        "cd4Above200AndAge",
        EptsReportUtils.map(cd4Above200AndAge, "endDate=${endDate},location=${location}"));
    cd.addSearch(
        "notEligibleForCd4AndAge", EptsReportUtils.map(notEligibleForCd4AndAge, DEFAULT_MAPPING));

    cd.setCompositionString(
        "txRtt AND NOT (cd4Under200AndAge OR cd4Above200AndAge OR notEligibleForCd4AndAge)");

    return cd;
  }

  private void addGeneralParameters(CohortDefinition cd) {
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <p>The system will identify patients who were transferred out by end of previous reporting
   * period as follows:
   *
   * <ul>
   *   <li>Patients enrolled on ART Program (Service TARV- Tratamento) with the following last
   *       status: Transferred Out or
   *   <li>Patients who have “Mudança no Estado de Permanência TARV” filled out in Ficha Resumo or
   *       Ficha Clinica – Master Card for the following reasons that are specified in the patient
   *       chart: patient Transferred Out or
   *   <li>Patients who have REASON PATIENT MISSED VISIT (MOTIVOS DA FALTA) as “Transferido para
   *       outra US” or “Auto-transferência” marked in the last Home Visit Card by reporting end
   *       date. Use the “data da visita” when the patient reason was marked on the home visit card
   *       as the reference date.
   * </ul>
   *
   * <br>
   *
   * <p>The system will identify the most recent date from the different sources as the date of
   * Transferred Out. Patients who are “marked” as transferred out who have an ARV pick-up
   * registered in FILA or have a clinical consultation after the date the patient was “marked” as
   * transferred out will not be considered as Transferred Out.<br>
   *
   * <p>The system will consider patient as transferred out as above defined only if the most recent
   * date between (next scheduled ART pick-up on FILA + 1 day) and (the most recent ART pickup date
   * on Ficha Recepção – Levantou ARVs + 31 days) falls during the reporting period.
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getTrfOut() {
    CompositionCohortDefinition definition = new CompositionCohortDefinition();
    addGeneralParameters(definition);
    definition.setName("TxTB - Transferred Out");
    definition.addSearch(
        "transferred-out",
        EptsReportUtils.map(
            txCurrCohortQueries.getPatientsWhoAreTransferredOutToAnotherHf(),
            "onOrBefore=${startDate-1d},location=${location}"));
    definition.addSearch(
        "transferred-out-fila-arv",
        EptsReportUtils.map(
            getTrfOutBetweenNextPickupDateFilaAndRecepcaoLevantou(),
            "startDate=${startDate},location=${location}"));
    definition.setCompositionString("transferred-out AND transferred-out-fila-arv");

    return definition;
  }

  /**
   * The system will consider patient as transferred out as above defined only if the most recent
   * date between (next scheduled ART pick-up on FILA + 1 day) and (the most recent ART pickup date
   * on Ficha Recepção – Levantou ARVs + 31 days) falls by end of previous reporting period.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getTrfOutBetweenNextPickupDateFilaAndRecepcaoLevantou() {

    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName(
        "Patients Transfered Out between (next scheduled ART pick-up on FILA + 1 day) "
            + "and (the most recent ART pickup date on Ficha Recepção – Levantou ARVs + 31 days");

    definition.addParameter(new Parameter("startDate", "startDate", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    valuesMap.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    valuesMap.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());
    valuesMap.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());

    String query =
        "SELECT final.patient_id FROM  ( "
            + "SELECT considered_transferred.patient_id, max(considered_transferred.value_datetime) as max_date "
            + "FROM ( "
            + "               SELECT     p.patient_id, "
            + "                          date_add(max(o.value_datetime), interval 1 day) AS value_datetime "
            + "               FROM       patient p "
            + "                              INNER JOIN encounter e "
            + "                                         ON         e.patient_id=p.patient_id "
            + "                              INNER JOIN obs o "
            + "                                         ON         o.encounter_id=e.encounter_id "
            + "               WHERE      p.voided = 0 "
            + "                 AND        e.voided = 0 "
            + "                 AND        o.voided = 0 "
            + "                 AND        e.encounter_type = ${18} "
            + "                 AND        o.concept_id = ${5096} "
            + "                 AND        e.encounter_datetime < :startDate "
            + "                 AND        e.location_id = :location "
            + "               GROUP BY   p.patient_id "
            + " UNION "
            + "               SELECT     p.patient_id, "
            + "                          date_add(max(o.value_datetime), interval 31 day)  AS value_datetime "
            + "               FROM       patient p "
            + "                              INNER JOIN encounter e "
            + "                                         ON         e.patient_id=p.patient_id "
            + "                              INNER JOIN obs o "
            + "                                         ON         o.encounter_id=e.encounter_id "
            + "               WHERE      p.voided = 0 "
            + "                 AND        e.voided = 0 "
            + "                 AND        o.voided = 0 "
            + "                 AND        e.encounter_type = ${52} "
            + "                 AND        o.concept_id = ${23866} "
            + "                 AND        o.value_datetime < :startDate "
            + "                 AND        e.location_id = :location "
            + "               GROUP BY   p.patient_id "
            + " )  considered_transferred "
            + " GROUP BY considered_transferred.patient_id "
            + " ) final "
            + " WHERE final.max_date < :startDate   ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    definition.setQuery(stringSubstitutor.replace(query));

    return definition;
  }
}
