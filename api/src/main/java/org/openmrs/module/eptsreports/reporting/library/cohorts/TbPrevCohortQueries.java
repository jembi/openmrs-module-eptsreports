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

import java.util.*;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.eptsreports.reporting.library.queries.CommonQueries;
import org.openmrs.module.eptsreports.reporting.library.queries.TbPrevQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsQueriesUtil;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition.TimeModifier;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Defines all of the TbPrev we want to expose for EPTS */
@Component
public class TbPrevCohortQueries {

  private final HivMetadata hivMetadata;

  private final TbMetadata tbMetadata;

  private final TbPrevQueries tbPrevQueries;

  private final CommonQueries commonQueries;

  @Autowired
  public TbPrevCohortQueries(
      HivMetadata hivMetadata,
      TbMetadata tbMetadata,
      GenericCohortQueries genericCohortQueries,
      TbPrevQueries tbPrevQueries,
      CommonQueries commonQueries) {
    this.hivMetadata = hivMetadata;
    this.tbMetadata = tbMetadata;
    this.tbPrevQueries = tbPrevQueries;
    this.commonQueries = commonQueries;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * all patients who have in “Patient Clinical Record of ART - Ficha de Seguimento (adults and
   * children)” or “Ficha Resumo” under “Profilaxia com INH – TPI” a Start Date (Data de Início)
   * within previous reporting period
   *
   * <p>Encounter Type Ids = <b>6, 9, 53</b> Isoniazid Prophylaxis start Date <b>(Concept 6128) >=
   * (startDate-6months) and < startDate AND NOT IN Encounter type = 53 RegimeTPT (Concept 23985) =
   * ANY VALUE</b>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsThatStartedProfilaxiaIsoniazidaOnPeriod() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Have Última profilaxia TPT with value INH");
    sqlCohortDefinition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("6128", hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId());
    valuesMap.put("23985", tbMetadata.getRegimeTPTConcept().getConceptId());

    String query =
        ""
            + "SELECT distinct p.patient_id "
            + "FROM patient p "
            + "         INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "         INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "         INNER JOIN ( "
            + "             SELECT distinct p.patient_id, e.encounter_datetime AS encounter "
            + "             FROM patient p "
            + "                 INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                 INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "             WHERE e.voided = 0 "
            + "               AND o.voided = 0 "
            + "               AND e.encounter_type IN (${6}, ${9}, ${53}) "
            + "               AND o.concept_id = ${6128} "
            + "               AND o.value_datetime >= :onOrAfter "
            + "               AND o.value_datetime < :onOrBefore "
            + "               AND o.location_id = :location "
            + "             ) data_inicio ON data_inicio.patient_id = p.patient_id "
            + "WHERE e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND o.location_id = :location "
            + "  AND p.patient_id NOT IN ( "
            + "    SELECT p.patient_id "
            + "    FROM patient p "
            + "             INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "             INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "    WHERE e.voided = 0 "
            + "      AND o.voided = 0 "
            + "      AND e.encounter_type = ${53} "
            + "      AND (o.concept_id = ${23985} AND o.value_coded IS NOT NULL) "
            + "      AND e.encounter_datetime = data_inicio.encounter "
            + ")";

    StringSubstitutor sb = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getNumerator() {
    CompositionCohortDefinition definition = new CompositionCohortDefinition();
    definition.setName("TB-PREV Numerator Query");
    definition.addParameter(new Parameter("onOrAfter", "After Date", Date.class));
    definition.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
    definition.addParameter(new Parameter("location", "Location", Location.class));

    definition.addSearch(
        "COMPLETED",
        EptsReportUtils.map(
            getPatientsThatCompletedIsoniazidProphylacticTreatment(),
            "startDate=${onOrAfter},endDate=${onOrBefore},location=${location}"));

    definition.addSearch(
        "DENOMINATOR",
        EptsReportUtils.map(
            getDenominator(),
            "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    definition.setCompositionString("COMPLETED AND DENOMINATOR");

    return definition;
  }

  public CohortDefinition getPatientsStartedTpt() {
    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("TB-PREV Previously on ART");
    definition.addParameter(new Parameter("startDate", "startDate Date", Date.class));
    definition.addParameter(new Parameter("endDate", "endDate Date", Date.class));
    definition.addParameter(new Parameter("location", "Location", Location.class));
    String query =
        new EptsQueriesUtil()
            .patientIdQueryBuilder(tbPrevQueries.getTPTStartDateQuery())
            .getQuery();
    definition.setQuery(query);
    return definition;
  }

  public CohortDefinition getArtStartDate() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Art Start Date");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));

    String query = commonQueries.getARTStartDate(false);
    cd.setQuery(query);

    return cd;
  }

  public CohortDefinition getDenominator() {
    CompositionCohortDefinition definition = new CompositionCohortDefinition();
    definition.setName("TB-PREV Denominator Query");
    definition.addParameter(new Parameter("onOrAfter", "After Date", Date.class));
    definition.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
    definition.addParameter(new Parameter("location", "Location", Location.class));
    definition.addSearch(
        "ARTSTART",
        EptsReportUtils.map(getArtStartDate(), "endDate=${onOrBefore},location=${location}"));

    definition.addSearch(
        "TRFOUT",
        EptsReportUtils.map(
            getTransferredOutPatientsComposition(),
            "startDate=${onOrAfter-6m},endDate=${onOrBefore},location=${location}"));
    definition.addSearch(
        "COMPLETEDTPT",
        EptsReportUtils.map(
            getPatientsThatCompletedIsoniazidProphylacticTreatment(),
            "startDate=${onOrAfter},endDate=${onOrBefore},location=${location}"));

    definition.addSearch(
        "STARTEDTPT",
        EptsReportUtils.map(
            getPatientsStartedTpt(),
            "startDate=${onOrAfter},endDate=${onOrBefore},location=${location}"));
    definition.setCompositionString(
        "ARTSTART AND STARTEDTPT AND NOT (TRFOUT AND NOT COMPLETEDTPT)");

    return definition;
  }

  public CohortDefinition getPatientsThatCompletedIsoniazidProphylacticTreatment() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Patients that completed Isoniazid prophylatic treatment");
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "Before Date", Date.class));

    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";

    cd.addSearch(
        "A",
        EptsReportUtils.map(tbPrevQueries.getPatientsWhoCompleted3HPAtLeast86Days(), mappings));
    cd.addSearch(
        "B", EptsReportUtils.map(tbPrevQueries.getAtLeast3ConsultationOnFichaClinica(), mappings));
    cd.addSearch("C", EptsReportUtils.map(tbPrevQueries.getAtLeastOne3HPWithDTOnFilt(), mappings));
    cd.addSearch(
        "D",
        EptsReportUtils.map(
            tbPrevQueries.getAtLeast3ConsultarionWithDispensaMensalOnFilt(), mappings));
    cd.addSearch(
        "E",
        EptsReportUtils.map(
            tbPrevQueries.getAtLeast1ConsultarionWithDT3HPOnFichaClinica(), mappings));
    cd.addSearch(
        "F",
        EptsReportUtils.map(tbPrevQueries.getPatientsWhoCompletedINHAtLeast173Days(), mappings));
    cd.addSearch(
        "G",
        EptsReportUtils.map(tbPrevQueries.getAtLeast5ConsultarionINHOnFichaClinica(), mappings));
    cd.addSearch(
        "H",
        EptsReportUtils.map(
            tbPrevQueries.getAtLeast6ConsultarionWithINHDispensaMensalOnFilt(), mappings));
    cd.addSearch(
        "I",
        EptsReportUtils.map(
            tbPrevQueries.getAtLeast2ConsultarionOfDTINHOnFichaClinica(), mappings));
    cd.addSearch(
        "J",
        EptsReportUtils.map(
            tbPrevQueries.getAtLeast2ConsultarionWithINHDispensaTrimestralOnFilt(), mappings));
    cd.addSearch(
        "K",
        EptsReportUtils.map(tbPrevQueries.getAtLeast3ConsultarionOfINHOnFichaClinica(), mappings));
    cd.addSearch(
        "L",
        EptsReportUtils.map(
            tbPrevQueries.getAtLeast1ConsultarionWithDTINHOnFichaClinica(), mappings));
    cd.addSearch(
        "M",
        EptsReportUtils.map(
            tbPrevQueries.getAtLeast3ConsultarionWithINHDispensaMensalOnFilt(), mappings));
    cd.addSearch(
        "N",
        EptsReportUtils.map(
            tbPrevQueries.getAtLeast1ConsultarionWithDTINHDispensaTrimestralOnFilt(), mappings));

    cd.setCompositionString(
        "A OR B OR C OR D OR E OR F OR G OR H OR I OR J OR (K AND L) OR (M AND N)");

    return cd;
  }

  /**
   * Patients who on “Ficha Clinica-MasterCard”, mastercard under “ Profilaxia INH” were marked with
   * an “I” (inicio) in a clinical consultation date occurred between ${onOrAfter} and ${onOrBefore}
   *
   * @return the cohort definition
   */
  public CohortDefinition getPatientsThatInitiatedProfilaxia() {
    Concept profilaxiaINH = hivMetadata.getIsoniazidUsageConcept();
    Concept inicio = hivMetadata.getStartDrugs();
    EncounterType adultoSeguimentoEncounterType = hivMetadata.getAdultoSeguimentoEncounterType();

    CodedObsCohortDefinition cd = new CodedObsCohortDefinition();
    cd.setName("Initiated Profilaxia");
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addParameter(new Parameter("onOrAfter", "After Date", Date.class));
    cd.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
    cd.setEncounterTypeList(Collections.singletonList(adultoSeguimentoEncounterType));
    cd.setTimeModifier(TimeModifier.ANY);
    cd.setQuestion(profilaxiaINH);
    cd.setValueList(Collections.singletonList(inicio));
    cd.setOperator(SetComparator.IN);
    return cd;
  }

  /**
   * 1.All transferred-outs registered in Patient Program State Patient_program.program_id =2 =
   * SERVICO TARV-TRATAMENTO and Patient_State.state = 7 (Transferred-out) or
   * Patient_State.start_date >= (starDate-6months) and <= endDate
   *
   * <p>2.All transferred-outs registered in Ficha Clinica of Master Card Encounter Type ID= 6
   * Estado de Permanencia (Concept Id 6273) = Transferred-out (Concept ID 1706) Encounter_datetime
   * >= (starDate-6months) and <= endDate
   *
   * <p>3.All transferred-outs registered in Ficha Resumo of Master Card Encounter Type ID= 53
   * Estado de Permanencia (Concept Id 6272) = Transferred-out (Concept ID 1706) obs_datetime >=
   * (starDate-6months) and <= endDate
   *
   * <p>4.All transferred-outs registered in Last Home Visit Card Encounter Type ID= 21 Reason
   * Patient Missed Visit (Concept Id 2016) = Transferred-out to another Facility (Concept ID 1706)
   * Or Auto Transfer (Concept id 23863) Encounter_datetime >= (starDate-6months) and <= endDate
   *
   * <p>5.Except all patients who after the most recent date from 1.1 to 1.2 have a drugs pick up or
   * consultation Encounter Type ID= 6, 9, 18 and  encounter_datetime> the most recent date and
   * <=endDate or Encounter Type ID = 52 and “Data de Levantamento” (Concept Id 23866
   * value_datetime) > the most recent date and <=endDate
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsTransferredOut() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName("get Patients With Most Recent Date Have Fila or Consultation ");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("23863", hivMetadata.getAutoTransferConcept().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("2016", hivMetadata.getDefaultingMotiveConcept().getConceptId());
    map.put("21", hivMetadata.getBuscaActivaEncounterType().getEncounterTypeId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put(
        "7",
        hivMetadata
            .getTransferredOutToAnotherHealthFacilityWorkflowState()
            .getProgramWorkflowStateId());

    String query =
        "  SELECT mostrecent.patient_id "
            + "FROM ("
            + " SELECT lastest.patient_id ,Max(lastest.last_date) as  last_date "
            + " FROM (  "
            + "    SELECT p.patient_id , Max(ps.start_date) AS last_date  "
            + "    FROM patient p   "
            + "        INNER JOIN patient_program pg   "
            + "            ON p.patient_id=pg.patient_id   "
            + "        INNER JOIN patient_state ps   "
            + "            ON pg.patient_program_id=ps.patient_program_id   "
            + "    WHERE pg.voided=0   "
            + "        AND ps.voided=0   "
            + "        AND p.voided=0   "
            + "        AND pg.program_id= ${2}  "
            + "        AND ps.state = ${7}   "
            + "        AND ps.start_date BETWEEN :startDate AND :endDate    "
            + "        AND pg.location_id= :location   "
            + "    group by p.patient_id  "
            + "  "
            + "    UNION  "
            + "  "
            + "    SELECT  p.patient_id,  Max(o.obs_datetime) AS last_date  "
            + "    FROM patient p    "
            + "        INNER JOIN encounter e   "
            + "            ON e.patient_id=p.patient_id   "
            + "        INNER JOIN obs o   "
            + "            ON o.encounter_id=e.encounter_id   "
            + "    WHERE  p.voided = 0   "
            + "        AND e.voided = 0   "
            + "        AND o.voided = 0   "
            + "        AND e.encounter_type = ${53}   "
            + "        AND o.concept_id = ${6272}  "
            + "        AND o.value_coded =  ${1706}   "
            + "        AND o.obs_datetime BETWEEN :startDate AND :endDate   "
            + "        AND e.location_id =  :location   "
            + "    GROUP BY p.patient_id  "
            + "    UNION   "
            + "    SELECT  p.patient_id , Max(e.encounter_datetime) AS last_date  "
            + "    FROM patient p    "
            + "        INNER JOIN encounter e   "
            + "            ON e.patient_id=p.patient_id   "
            + "        INNER JOIN obs o   "
            + "            ON o.encounter_id=e.encounter_id   "
            + "    WHERE  p.voided = 0   "
            + "        AND e.voided = 0   "
            + "        AND o.voided = 0   "
            + "        AND e.encounter_type = ${6}  "
            + "        AND o.concept_id = ${6273}  "
            + "        AND o.value_coded = ${1706}   "
            + "        AND e.encounter_datetime BETWEEN :startDate AND :endDate   "
            + "        AND e.location_id =  :location  "
            + "    GROUP BY p.patient_id   "
            + "  "
            + "    UNION  "
            + "  "
            + "    SELECT p.patient_id, Max(e.encounter_datetime) last_date   "
            + "    FROM patient p   "
            + "        INNER JOIN encounter e   "
            + "              ON p.patient_id = e.patient_id   "
            + "        INNER JOIN obs o   "
            + "              ON e.encounter_id = o.encounter_id   "
            + "    WHERE o.concept_id = ${2016}  "
            + "    	   AND e.location_id = :location   "
            + "        AND e.encounter_type= ${21}   "
            + "        AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "		   AND o.value_coded IN (${1706} ,${23863})  "
            + "        AND e.voided=0   "
            + "        AND o.voided=0   "
            + "        AND p.voided=0   "
            + "    GROUP BY p.patient_id "
            + ") lastest   "
            + "WHERE lastest.patient_id NOT  IN("
            + " "
            + "  			     SELECT  p.patient_id    "
            + "	                 FROM patient p      "
            + "	                     INNER JOIN encounter e     "
            + "	                         ON e.patient_id=p.patient_id     "
            + "	                 WHERE  p.voided = 0     "
            + "	                     AND e.voided = 0     "
            + "	                     AND e.encounter_type IN (${6},"
            + "${9},"
            + "${18})    "
            + "	                     AND e.encounter_datetime > lastest.last_date "
            + " AND e.encounter_datetime <=  :endDate    "
            + "	                     AND e.location_id =  :location    "
            + "	                 GROUP BY p.patient_id "
            + " UNION "
            + "        			 SELECT  p.patient_id    "
            + "	                 FROM patient p       "
            + "	                      INNER JOIN encounter e      "
            + "	                          ON e.patient_id=p.patient_id      "
            + "	                      INNER JOIN obs o      "
            + "	                          ON o.encounter_id=e.encounter_id      "
            + "	                  WHERE  p.voided = 0      "
            + "	                      AND e.voided = 0      "
            + "	                      AND o.voided = 0      "
            + "	                      AND e.encounter_type = ${52}     "
            + "	                      AND o.concept_id = ${23866}     "
            + "	                      AND o.value_datetime > lastest.last_date  "
            + " AND o.value_datetime <= :endDate      "
            + "	                      AND e.location_id =  :location     "
            + "	                  GROUP BY p.patient_id   "
            + ")  "
            + " GROUP BY lastest.patient_id"
            + " )mostrecent "
            + " GROUP BY mostrecent.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    String mappedQuery = stringSubstitutor.replace(query);

    sqlCohortDefinition.setQuery(mappedQuery);

    return sqlCohortDefinition;
  }

  /**
   * <b>Description:</b> Patients who have Regime de TPT with the values (“Isoniazida” or
   * “Isoniazida + Piridoxina”) marked on the first pick-up date on Ficha de Levantamento de TPT
   * (FILT) during the previous reporting period (INH Start Date) and no other INH values
   * (“Isoniazida” or “Isoniazida + Piridoxina”) marked on FILT in the 7 months prior to the INH
   * Start Date or
   *
   * <p><b>Technical Specs</b>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoHaveRegimeTPTWithINHMarkedOnFirstPickUpDateOnFILT() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "Patients Who Have Regime TPT With INH Marked On First PickUp Date On FILT");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Date.class));
    sqlCohortDefinition.setQuery(
        TbPrevQueries.getRegimeTPTOrOutrasPrescricoes(
            tbMetadata.getRegimeTPTEncounterType(),
            tbMetadata.getRegimeTPTConcept(),
            Arrays.asList(
                tbMetadata.getIsoniazidConcept(), tbMetadata.getIsoniazidePiridoxinaConcept()),
            7));

    return sqlCohortDefinition;
  }

  /**
   * <b>Description:</b> Patients who have Outras Prescrições with the value “3HP” marked on Ficha
   * Clínica - Mastercard during the previous reporting period (3HP Start Date) and no other 3HP
   * prescriptions marked on Ficha-Clinica in the 4 months prior to the 3HP Start Date
   *
   * <p><b>Technical Specs</b>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoHaveOutrasPrescricoesWith3HPMarkedOnFichaClinica() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "Patients Who Have Outras Prescricoes With 3HP Marked On Ficha Clinica");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Date.class));
    sqlCohortDefinition.setQuery(
        TbPrevQueries.getRegimeTPTOrOutrasPrescricoes(
            hivMetadata.getAdultoSeguimentoEncounterType(),
            tbMetadata.getTreatmentPrescribedConcept(),
            Arrays.asList(tbMetadata.get3HPConcept()),
            4));

    return sqlCohortDefinition;
  }

  /**
   * <b>Description:</b> Patients who have Regime de TPT with the values “3HP or 3HP + Piridoxina”
   * marked on the first pick-up date on Ficha de Levantamento de TPT (FILT) during the previous
   * reporting period (3HP Start Date) and no other 3HP pick-ups marked on FILT in the 4 months
   * prior to the 3HP Start Date
   *
   * <p><b>Technical Specs</b>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoHaveRegimeTPTWith3HPMarkedOnFirstPickUpDateOnFILT() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "Patients Who Have Regime TPT With 3HP Marked On First PickUp Date On FILT");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Date.class));
    sqlCohortDefinition.setQuery(
        TbPrevQueries.getRegimeTPTOrOutrasPrescricoes(
            tbMetadata.getRegimeTPTEncounterType(),
            tbMetadata.getRegimeTPTConcept(),
            Arrays.asList(tbMetadata.get3HPConcept(), tbMetadata.get3HPPiridoxinaConcept()),
            4));

    return sqlCohortDefinition;
  }

  /**
   * <b>Patients Transferred-Out to another health facility</b>
   *
   * <p>The system will identify patients who are Transferred Out
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getTransferredOutPatientsComposition() {
    CompositionCohortDefinition definition = new CompositionCohortDefinition();
    definition.setName("Patients Transferred-Out to another health facility");
    definition.addParameter(new Parameter("startDate", "startDate", Date.class));
    definition.addParameter(new Parameter("endDate", "endDate", Date.class));
    definition.addParameter(new Parameter("location", "Location", Location.class));

    definition.addSearch(
        "PICKUPS",
        EptsReportUtils.map(
            getTransferredOutBetweenNextPickupDateFilaAndRecepcaoLevantou(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    definition.addSearch(
        "TRFOUT",
        EptsReportUtils.map(
            getPatientsTransferredOutTbPrev(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    definition.setCompositionString("TRFOUT AND PICKUPS");

    return definition;
  }

  /**
   * <b>Patients who are Transferred Out to another HF</b>
   * <li>Patients enrolled on ART Program (Service TARV- Tratamento) with the following last state:
   *     “Transferred Out” or
   * <li>Patients whose most recently informed “Mudança no Estado de Permanência TARV” is
   *     Transferred Out on Ficha Clinica ou Ficha Resumo – Master Card.
   * <li>Patients who have REASON PATIENT MISSED VISIT (MOTIVOS DA FALTA) as “Transferido para outra
   *     US” or “Auto-transferência” marked in the last Home Visit Card by reporting end date. Use
   *     the “data da visita” when the patient reason was marked on the Home Visit Card as the
   *     reference date
   * <li>The system will consider patient as transferred out as above defined only if the most
   *     recent date between (next scheduled ART pick-up on FILA + 1 day) and (the most recent ART
   *     pickup date on Ficha Recepção – Levantou ARVs + 31 days) falls by the end of the reporting
   *     period
   *
   *     <p><b>Note:</b>
   *
   *     <p>Patients who are “marked” as transferred out who have an ARV pick-up registered in FILA
   *     after the date the patient was “marked” as transferred out will be evaluated for IIT
   *     definition
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsTransferredOutTbPrev() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName("Patients Transferred-Out to another health facility");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put(
        "7",
        hivMetadata
            .getTransferredOutToAnotherHealthFacilityWorkflowState()
            .getProgramWorkflowStateId());
    map.put("21", hivMetadata.getBuscaActivaEncounterType().getEncounterTypeId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("2016", hivMetadata.getDefaultingMotiveConcept().getConceptId());
    map.put("23863", hivMetadata.getAutoTransferConcept().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());

    String query =
        "SELECT transferred_out.patient_id FROM ( "
            + "  SELECT most_recent.patient_id, max(most_recent.common_date) as transferred_date FROM (  "
            + "  SELECT     p.patient_id, "
            + "  Max(ps.start_date) AS common_date "
            + "  FROM       patient p "
            + "  INNER JOIN patient_program pg "
            + "  ON         p.patient_id=pg.patient_id "
            + "  INNER JOIN patient_state ps "
            + "  ON         pg.patient_program_id=ps.patient_program_id "
            + "  INNER JOIN "
            + "             ( "
            + "                SELECT     p.patient_id , "
            + "                           Max(ps.start_date) AS common_date "
            + "                FROM       patient p "
            + "                INNER JOIN patient_program pg "
            + "                ON         p.patient_id=pg.patient_id "
            + "                INNER JOIN patient_state ps "
            + "                ON         pg.patient_program_id=ps.patient_program_id "
            + "                WHERE      pg.voided=0 "
            + "                AND        ps.voided=0 "
            + "                AND        p.voided=0 "
            + "                AND        pg.program_id= ${2} "
            + "                AND        ps.state IS NOT NULL "
            + "                AND        ps.start_date BETWEEN :startDate AND :endDate "
            + "                AND        pg.location_id= :location "
            + "                GROUP BY   p.patient_id ) laststate "
            + "  ON         laststate.patient_id=p.patient_id "
            + "  WHERE      pg.voided=0 "
            + "  AND        ps.voided=0 "
            + "  AND        p.voided=0 "
            + "  AND        pg.program_id= ${2} "
            + "  AND        ps.state = ${7} "
            + "  AND        ps.start_date = laststate.common_date "
            + "  AND        pg.location_id= :location "
            + "  GROUP BY   p.patient_id "
            + "    UNION  "
            + " SELECT  transferred.patient_id, max(transferred.encounterdatetime) common_date "
            + " FROM ( "
            + " 	SELECT  p.patient_id, max(e.encounter_datetime) AS encounterdatetime "
            + "     FROM patient p  "
            + "     	INNER JOIN encounter e  "
            + "         	ON e.patient_id=p.patient_id  "
            + "         INNER JOIN obs o  "
            + "             ON o.encounter_id=e.encounter_id  "
            + "     WHERE e.encounter_type = ${6}  "
            + "         AND p.voided=0   "
            + "         AND e.voided=0  "
            + "         AND o.voided=0  "
            + "         AND o.concept_id = ${6273}  "
            + "         AND o.value_coded= ${1706}  "
            + "         AND e.location_id = :location  "
            + "         AND e.encounter_datetime BETWEEN :startDate AND :endDate  "
            + "      GROUP BY p.patient_id  "
            + "      UNION  "
            + "      SELECT  p.patient_id, max(o.obs_datetime) AS encounterdatetime "
            + "      FROM patient p    "
            + "      	INNER JOIN encounter e  "
            + "               ON e.patient_id=p.patient_id    "
            + "         INNER JOIN obs o  "
            + "               ON o.encounter_id=e.encounter_id   "
            + "      WHERE e.encounter_type = ${53} "
            + "         AND p.voided=0  "
            + "         AND e.voided=0  "
            + "         AND o.voided=0    "
            + "         AND o.concept_id = ${6272}  "
            + "         AND o.value_coded= ${1706}  "
            + "         AND e.location_id = :location  "
            + "         AND o.obs_datetime BETWEEN :startDate AND :endDate    "
            + "      GROUP BY p.patient_id ) transferred "
            + " GROUP BY transferred.patient_id  "
            + "   UNION  "
            + "  SELECT     p.patient_id, "
            + "             Max(e.encounter_datetime) AS common_date "
            + "  FROM       patient p "
            + "  INNER JOIN encounter e "
            + "  ON         p.patient_id = e.patient_id "
            + "  INNER JOIN obs o "
            + "  ON         e.encounter_id = o.encounter_id "
            + "  INNER JOIN "
            + "             ( "
            + "                        SELECT     p.patient_id, "
            + "                                   Max(e.encounter_datetime) AS common_date "
            + "                        FROM       patient p "
            + "                        INNER JOIN encounter e "
            + "                        ON         e.patient_id = p.patient_id "
            + "                        WHERE      p.voided = 0 "
            + "                        AND        e.voided = 0 "
            + "                        AND        e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "                        AND        e.encounter_type = ${21} "
            + "                        AND        e.location_id = :location "
            + "                        GROUP BY   p.patient_id) latest "
            + "  ON         latest.patient_id = p.patient_id "
            + "  WHERE      p.voided = 0 "
            + "  AND        o.voided = 0 "
            + "  AND        e.voided = 0 "
            + "  AND        e.location_id = :location "
            + "  AND        e.encounter_type = ${21} "
            + "  AND        o.concept_id = ${2016} "
            + "  AND        o.value_coded IN ( ${1706}, ${23863} ) "
            + "  AND        e.encounter_datetime = latest.common_date "
            + "	GROUP BY p.patient_id "
            + " ) most_recent "
            + "	GROUP BY most_recent.patient_id) transferred_out "
            + "    WHERE  NOT EXISTS ( "
            + "           SELECT  e.patient_id "
            + "           FROM    encounter e "
            + "           WHERE  e.encounter_type = ${18} "
            + "               AND e.voided = 0 "
            + "               AND location_id = :location "
            + "               AND e.patient_id = transferred_out.patient_id "
            + "               AND e.encounter_datetime > transferred_out.transferred_date "
            + "               AND e.encounter_datetime <= :endDate "
            + " )  "
            + "    GROUP BY transferred_out.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    String mappedQuery = stringSubstitutor.replace(query);

    sqlCohortDefinition.setQuery(mappedQuery);

    return sqlCohortDefinition;
  }

  /**
   * The system will consider patient as transferred out as above defined only if the most recent
   * date between (next scheduled ART pick-up on FILA + 1 day) and (the most recent ART pickup date
   * on Ficha Recepção – Levantou ARVs + 31 days) falls during the combined previous and current
   * reporting periods.
   *
   * <p>For the combined previous and current reporting period:
   * <li>Start Date = Report Start Date – 6 months
   * <li>End Date = Report End Date
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getTransferredOutBetweenNextPickupDateFilaAndRecepcaoLevantou() {

    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName(
        "Patients Transfered Out between (next scheduled ART pick-up on FILA + 1 day) "
            + "and (the most recent ART pickup date on Ficha Recepção – Levantou ARVs + 31 days");

    definition.addParameter(new Parameter("startDate", "startDate", Date.class));
    definition.addParameter(new Parameter("endDate", "endDate", Date.class));
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
            + "                 AND        e.encounter_datetime BETWEEN :startDate AND :endDate "
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
            + "                 AND        o.value_datetime BETWEEN :startDate AND :endDate "
            + "                 AND        e.location_id = :location "
            + "               GROUP BY   p.patient_id "
            + " )  considered_transferred "
            + " GROUP BY considered_transferred.patient_id "
            + " ) final "
            + " WHERE final.max_date BETWEEN :startDate AND :endDate  ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    definition.setQuery(stringSubstitutor.replace(query));

    return definition;
  }
}
