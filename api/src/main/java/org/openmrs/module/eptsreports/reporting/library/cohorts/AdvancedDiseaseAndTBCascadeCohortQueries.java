package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AdvancedDiseaseAndTBCascadeCohortQueries {

  private HivMetadata hivMetadata;
  private TbMetadata tbMetadata;

  private TXTBCohortQueries txtbCohortQueries;

  private ListOfPatientsArtCohortCohortQueries listOfPatientsArtCohortCohortQueries;

  private TxNewCohortQueries txNewCohortQueries;

  private final String reportingPeriod =
      "startDate=${endDate}-2m,endDate=${generationDate},location=${location}";

  @Autowired
  public AdvancedDiseaseAndTBCascadeCohortQueries(
      HivMetadata hivMetadata,
      TbMetadata tbMetadata,
      ListOfPatientsArtCohortCohortQueries listOfPatientsArtCohortCohortQueries,
      TxNewCohortQueries txNewCohortQueries,
      TXTBCohortQueries txtbCohortQueries) {

    this.hivMetadata = hivMetadata;
    this.tbMetadata = tbMetadata;
    this.listOfPatientsArtCohortCohortQueries = listOfPatientsArtCohortCohortQueries;
    this.txNewCohortQueries = txNewCohortQueries;
    this.txtbCohortQueries = txtbCohortQueries;
  }

  /**
   * <b>The system will include all clients who reinitiated ART during the inclusion period who
   * have:</b>
   *
   * <ul>
   *   <li>The most recent “Mudança de Estado de Permanência” state marked as “Reinício” in Ficha
   *       Clínica, with the state date falling during the inclusion period.
   *   <li>OR The most recent “Mudança de Estado de Permanência” state marked as “Reinício” in Ficha
   *       Resumo, with the state date falling during the inclusion period.
   * </ul>
   *
   * @return CohortDefinition *
   */
  public CohortDefinition getPatientsWhoReinitiatedArt() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Clients who reinitiated ART during the inclusion period");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "End Date", Location.class));

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN (SELECT e.patient_id "
            + "                   FROM   encounter e INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                          INNER JOIN(SELECT e.patient_id, "
            + "                                            Max(e.encounter_datetime) state_date "
            + "                                     FROM   encounter e INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                                     WHERE  e.voided = 0 "
            + "                                            AND o.voided = 0 "
            + "                                            AND e.encounter_type = ${6} "
            + "                                            AND o.concept_id = ${6273} "
            + "                                            AND e.location_id = :location "
            + "                                            AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "                                     GROUP  BY e.patient_id) recent_state "
            + "                                  ON recent_state.patient_id = e.patient_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND o.concept_id = ${6273} "
            + "                          AND o.value_coded = ${1705} "
            + "                          AND e.location_id = :location "
            + "                          AND e.encounter_datetime = recent_state.state_date "
            + "                   GROUP  BY e.patient_id "
            + "                   UNION "
            + "                   SELECT e.patient_id "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                          INNER JOIN(SELECT e.patient_id, "
            + "                                            Max(o.obs_datetime) state_date "
            + "                                     FROM   encounter e INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                                     WHERE  e.voided = 0 "
            + "                                            AND o.voided = 0 "
            + "                                            AND e.encounter_type = ${53} "
            + "                                            AND o.concept_id = ${6272} "
            + "                                            AND e.location_id = :location "
            + "                                            AND o.obs_datetime BETWEEN :startDate AND :endDate "
            + "                                     GROUP  BY e.patient_id) recent_state ON recent_state.patient_id = e.patient_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND o.concept_id = ${6272} "
            + "                          AND o.value_coded = ${1705} "
            + "                          AND e.location_id = :location "
            + "                          AND o.obs_datetime = recent_state.state_date "
            + "                   GROUP  BY e.patient_id) reinitiated ON reinitiated.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(getMetadata());
    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   *
   *
   * <ul>
   *   <li>Absolute CD4 count results (CD4 Absoluto= ANY RESULT) registered in the Ficha Clinica –
   *       Mastercard during the inclusion periodor
   *   <li>CD4 count result (CD4 absoluto - Último CD4= ANY RESULT) registered in the Ficha Resumo –
   *       Mastercard with the result date during the inclusion period or
   *   <li>Absolute CD4 count results (CD4 Absoluto= ANY RESULT) registered in Laboratory form
   *       during the inclusion period or
   *   <li>Absolute CD4 count results (CD4 Absoluto= ANY RESULT) registered in the Electronic Lab
   *       form during the inclusion period or
   *   <li>Absolute CD4 count results (CD4 Absoluto= ANY RESULT) registered in the Ficha DAH with
   *       the result date during the inclusion period
   * </ul>
   *
   * <p>Note: For clients with more than one CD4 count result registered within this period, the
   * most recent result will be considered
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWithCD4Count() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Number of clients with a CD4 count during inclusion period");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "End Date", Location.class));

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN (SELECT e.patient_id, Max(DATE(e.encounter_datetime)) AS result_date "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.encounter_type IN ( ${6}, ${13}, ${51} ) "
            + "                          AND o.concept_id = ${1695} "
            + "                          AND o.value_numeric IS NOT NULL "
            + "                          AND e.location_id = :location "
            + "                          AND DATE(e.encounter_datetime) BETWEEN :startDate AND :endDate "
            + "                   GROUP  BY e.patient_id "
            + "                   UNION "
            + "                   SELECT e.patient_id, Max(o.obs_datetime) AS result_date "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.encounter_type IN ( ${53}, ${90} ) "
            + "                          AND o.concept_id = ${1695} "
            + "                          AND o.value_numeric IS NOT NULL "
            + "                          AND e.location_id = :location "
            + "                          AND o.obs_datetime BETWEEN :startDate AND :endDate"
            + "                   GROUP  BY e.patient_id) cd4 "
            + "               ON cd4.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "GROUP  BY p.patient_id";
    StringSubstitutor sb = new StringSubstitutor(getMetadata());
    cd.setQuery(sb.replace(query));
    return cd;
  }

  /**
   *
   *
   * <ul>
   *   <li>TB LAM result with ANY RESULT registered in the Investigações – resultados laboratoriais
   *       in Ficha Clínica – Mastercard
   *   <li>TB LAM result with ANY RESULT registered in the Laboratory form
   *   <li>TB-LAM urina with ``pos`` or ``neg`` result registered in the Ficha da Doença Avançada
   *       por HIV
   * </ul>
   *
   * @return CohortDefinition *
   */
  public CohortDefinition getPatientsWithTbLamResult() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Number of clients with TB LAM results by report generation date ");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "End Date", Location.class));

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN (SELECT e.patient_id "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.encounter_type IN ( ${6}, ${13} ) "
            + "                          AND o.concept_id = ${23951} "
            + "                          AND o.value_coded IS NOT NULL "
            + "                          AND e.location_id = :location "
            + "                          AND e.encounter_datetime BETWEEN :startDate AND :endDate"
            + "                   GROUP  BY e.patient_id "
            + "                   UNION "
            + "                   SELECT e.patient_id "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.encounter_type = ${90} "
            + "                          AND o.concept_id = ${23951} "
            + "                          AND o.value_coded IN ( ${703}, ${664} ) "
            + "                          AND e.location_id = :location "
            + "                          AND o.obs_datetime BETWEEN :startDate AND :endDate"
            + "                   GROUP  BY e.patient_id) tb_lam "
            + "               ON tb_lam.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(getMetadata());
    cd.setQuery(sb.replace(query));
    return cd;
  }

  /**
   *
   *
   * <ul>
   *   <li>TB LAM result registered in the Investigações – Resultados Laboratoriais as Positive in
   *       Ficha Clínica or
   *   <li>TB LAM result marked as Positive in the Laboratory Form or
   *   <li>TB LAM result marked as Positive in Ficha DAH
   * </ul>
   *
   * @return CohortDefinition *
   */
  public CohortDefinition getPatientsWithPositiveTbLamResult() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Clients with positive TB LAM");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "End Date", Location.class));

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN (SELECT e.patient_id "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.encounter_type IN ( ${6}, ${13} ) "
            + "                          AND o.concept_id = ${23951} "
            + "                          AND o.value_coded = ${703} "
            + "                          AND e.location_id = :location "
            + "                          AND e.encounter_datetime BETWEEN :startDate AND :endDate"
            + "                   GROUP  BY e.patient_id "
            + "                   UNION "
            + "                   SELECT e.patient_id "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.encounter_type = ${90} "
            + "                          AND o.concept_id = ${23951} "
            + "                          AND o.value_coded = ${703} "
            + "                          AND e.location_id = :location "
            + "                          AND o.obs_datetime BETWEEN :startDate AND :endDate"
            + "                   GROUP  BY e.patient_id) tb_lam "
            + "               ON tb_lam.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(getMetadata());
    cd.setQuery(sb.replace(query));
    return cd;
  }
  /**
   *
   *
   * <ul>
   *   <li>GeneXpert result marked with ANY RESULT registered in the Investigações – resultados
   *       laboratoriais - Ficha Clínica – Mastercard or
   *   <li>GeneXpert result marked with ANY RESULT registered in the Laboratory Form or
   *   <li>XpertMTB result marked with ANY RESULT registered in the Laboratory Form
   * </ul>
   *
   * @return CohortDefinition *
   */
  public CohortDefinition getPatientsWithAnyGeneXpertResult() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Clients with positive TB LAM");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "End Date", Location.class));

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN (SELECT e.patient_id "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.encounter_type = ${6}  "
            + "                          AND o.concept_id = ${23723}"
            + "                          AND o.value_coded IS NOT NULL "
            + "                          AND e.location_id = :location "
            + "                          AND e.encounter_datetime BETWEEN :startDate AND :endDate"
            + "                   GROUP  BY e.patient_id "
            + "                   UNION "
            + "                   SELECT e.patient_id "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.encounter_type = ${13} "
            + "                          AND o.concept_id IN (${23723}, ${165189}) "
            + "                          AND o.value_coded IS NOT NULL "
            + "                          AND e.location_id = :location "
            + "                          AND o.obs_datetime BETWEEN :startDate AND :endDate"
            + "                   GROUP  BY e.patient_id) tb_lam "
            + "               ON tb_lam.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(getMetadata());
    cd.setQuery(sb.replace(query));
    return cd;
  }

  /**
   *
   *
   * <ul>
   *   <li>GeneXpert result marked as Positive, registered in the Investigações – resultados
   *       laboratoriais - Ficha Clínica – Mastercard; or
   *   <li>GeneXpert result marked as Positive registered in the Laboratory Form or
   *   <li>XpertMTB result marked as SIM registered in the Laboratory Form
   * </ul>
   *
   * @return CohortDefinition *
   */
  public CohortDefinition getPatientsWithPositiveGeneXpertResult() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Clients with positive TB LAM and GeneXpert positive for TB");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "End Date", Location.class));

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN (SELECT e.patient_id "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.encounter_type IN (${6}, ${13}) "
            + "                          AND o.concept_id = ${23723}"
            + "                          AND o.value_coded = ${703} "
            + "                          AND e.location_id = :location "
            + "                          AND e.encounter_datetime BETWEEN :startDate AND :endDate"
            + "                   GROUP  BY e.patient_id "
            + "                   UNION "
            + "                   SELECT e.patient_id "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.encounter_type = ${13} "
            + "                          AND o.concept_id = ${165189} "
            + "                          AND o.value_coded = ${1065} "
            + "                          AND e.location_id = :location "
            + "                          AND o.obs_datetime BETWEEN :startDate AND :endDate"
            + "                   GROUP  BY e.patient_id) tb_lam "
            + "               ON tb_lam.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(getMetadata());
    cd.setQuery(sb.replace(query));
    return cd;
  }

  /**
   *
   *
   * <ul>
   *   <li>are marked with “Tratamento TB– Início (I)” on Ficha Clínica Master Card between (end
   *       date minus (-) 2 months) and the report generation date, or
   *   <li>have at least TB Treatment (Tratamento de TB) Start Date (Data de Início) in “Client
   *       Clinical Record of ART - Ficha de Seguimento between (end date minus 2 months) and the
   *       report generation date, or
   *   <li>have a TB Date (Condições Médicas Importantes – Ficha Resumo – Mastercard); between (end
   *       date minus (-) 2 months) and the report generation date, or
   *   <li>are enrolled in TB Program with enrollment Date (Data de Admissão) between (end date
   *       minus (-) 2 months) and the report generation date
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsOnTbTreatment() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Clients TB treatment by report generation date");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "End Date", Location.class));

    cd.addSearch(
        "startedTbTreatment",
        EptsReportUtils.map(
            txtbCohortQueries.tbTreatmentStartDateWithinReportingDate(), reportingPeriod));
    cd.addSearch(
        "tbProgram", EptsReportUtils.map(txtbCohortQueries.getInTBProgram(), reportingPeriod));

    cd.addSearch(
        "pumonaryTb", EptsReportUtils.map(txtbCohortQueries.getPulmonaryTB(), reportingPeriod));

    cd.addSearch(
        "tbPlan", EptsReportUtils.map(txtbCohortQueries.getTBTreatmentStart(), reportingPeriod));

    cd.setCompositionString("startedTbTreatment OR tbProgram OR pumonaryTb OR tbPlan");

    return cd;
  }

  private Map<String, Integer> getMetadata() {
    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    map.put("90", 90);
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayPriorArtPatientConcept().getConceptId());
    map.put("1705", hivMetadata.getRestartConcept().getConceptId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("703", hivMetadata.getPositive().getConceptId());
    map.put("664", hivMetadata.getNegative().getConceptId());
    map.put("23951", tbMetadata.getTestTBLAM().getConceptId());
    map.put("23723", tbMetadata.getTBGenexpertTestConcept().getConceptId());
    map.put("165189", tbMetadata.getTestXpertMtbUuidConcept().getConceptId());
    map.put("165189", tbMetadata.getTestXpertMtbUuidConcept().getConceptId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    return map;
  }
}
