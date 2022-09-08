package org.openmrs.module.eptsreports.reporting.library.cohorts;

import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.library.queries.ListOfPatientsWhoPickeupArvDuringPeriodQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsQueriesUtil;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class ListOfPatientsWhoPickeupArvDuringPeriodDataDefinitionQueries {

  private final HivMetadata hivMetadata;

  @Autowired
  public ListOfPatientsWhoPickeupArvDuringPeriodDataDefinitionQueries(HivMetadata hivMetadata) {
    this.hivMetadata = hivMetadata;
  }

  /**
   * <b>The system will show information from patient’s Last State (column G) and Last Patient State
   * Date (Sheet 1: Column G and Column H) as follows:</b>
   *
   * <ul>
   *   <li>
   *       <p>Last patient’s state informed on most recent active ART Program (Service TARV -
   *       Tratamento) where patient is enrolled by report generation date or
   *   <li>
   *       <p>Last patient’s state marked in “Mudança no Estado de Permanência TARV” in Ficha Resumo
   *       with the most recente date o state by report generation date or
   *   <li>
   *       <p>Patient’s state marked in “Mudança no Estado de Permanência TARV” in Ficha Clínica by
   *       report generation date Note1: The system will consider the most recent date from all
   *       sources listed above.
   * </ul>
   *
   * @param lastState Last State Flag is used to return last state or last state date column
   * @return {@link DataDefinition}
   */
  public DataDefinition getLastStateOrDateFromARTProgramOrFichaResumoOrClinica(boolean lastState) {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(
        "Last state or state date on ART Program or Ficha Resumo or Ficha Clinica");
    addSqlCohortDefinitionParameters(sqlPatientDataDefinition);

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    valuesMap.put("1707", hivMetadata.getAbandonedConcept().getConceptId());
    valuesMap.put("1366", hivMetadata.getPatientHasDiedConcept().getConceptId());
    valuesMap.put("1709", hivMetadata.getSuspendedTreatmentConcept().getConceptId());
    valuesMap.put("23903", hivMetadata.getNegativeDiagnosisConcept().getConceptId());
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    valuesMap.put("2", hivMetadata.getARTProgram().getProgramId());
    valuesMap.put(
        "7",
        hivMetadata
            .getTransferredOutToAnotherHealthFacilityWorkflowState()
            .getProgramWorkflowStateId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("10", hivMetadata.getArtDeadWorkflowState().getProgramWorkflowStateId());
    valuesMap.put(
        "8", hivMetadata.getSuspendedTreatmentWorkflowState().getProgramWorkflowStateId());

    String stateColumn =
        " CASE "
            + "   WHEN (o.value_coded IS NULL AND ps.state IS NULL) THEN ''   "
            + "   WHEN o.value_coded IS NOT NULL THEN o.value_coded   "
            + "   WHEN ps.state IS NOT NULL THEN ps.state   "
            + "   END AS recent_state ";

    String stateDateColumn = " states.most_recent as last_state_date ";

    String query = " SELECT p.patient_id,  ";

    // this will change the column output if the query is used to return the last state date
    if (lastState) {
      query += stateColumn;
    } else {
      query += stateDateColumn;
    }

    query +=
        " FROM patient p  "
            + "           INNER JOIN encounter e ON p.patient_id = e.patient_id  "
            + "           INNER JOIN obs o ON e.encounter_id = o.encounter_id  "
            + "           INNER JOIN patient_program pg  "
            + "                      ON p.patient_id = pg.patient_id  "
            + "           INNER JOIN patient_state ps  "
            + "                      ON pg.patient_program_id = ps.patient_program_id  "
            + "           INNER JOIN( "
            + " SELECT recent.patient_id, MAX(recent.last_state_date) as most_recent FROM ( "
            + getUnionQuery()
            + " ) recent "
            + " GROUP BY recent.patient_id "
            + " )states on p.patient_id = states.patient_id "
            + "where   "
            + "        e.voided = 0   "
            + "  AND p.voided = 0   "
            + "  AND o.voided = 0   "
            + "  AND pg.voided = 0   "
            + "  AND ps.voided = 0   "
            + "  AND pg.location_id = :location   "
            + "AND (   "
            + "    (   "
            + "                e.encounter_type = ${53}   "
            + "            AND        o.concept_id = ${6272}   "
            + "            AND        o.value_coded IN (${1706}, ${1709}, ${1707}, ${1366}, ${23903})   "
            + "            AND        o.obs_datetime = states.most_recent   "
            + "        )   "
            + "    OR   "
            + "    (   "
            + "                e.encounter_type = ${6}   "
            + "            AND        o.concept_id = ${6273}   "
            + "            AND        o.value_coded IN (${1706}, ${1709} ,${1707}, ${1366}, ${23903})   "
            + "            AND        e.encounter_datetime = states.most_recent   "
            + "            AND NOT EXISTS(   "
            + "                SELECT e.encounter_id from encounter e   "
            + "                                      INNER JOIN obs o2 on e.encounter_id = o2.encounter_id   "
            + "                where   "
            + "                        e.encounter_type = ${53}   "
            + "                  AND        o2.concept_id = ${6272}   "
            + "                  AND        o2.value_coded IN (${1706}, ${1709}, ${1707}, ${1366}, ${23903})   "
            + "                  AND        o2.obs_datetime = states.most_recent   "
            + "                group by e.encounter_id   "
            + "            )   "
            + "        )   "
            + "    OR (   "
            + "            pg.program_id = ${2}   "
            + "            AND ps.state IN ( ${7}, ${8}, ${9}, ${10} )   "
            + "            AND   ps.start_date = states.most_recent   "
            + "AND pg.patient_id NOT IN (   "
            + "                SELECT p.patient_id   "
            + "                from patient p INNER JOIN encounter e ON p.patient_id = e.patient_id   "
            + "                               INNER JOIN obs o2 on e.encounter_id = o2.encounter_id   "
            + "                where e.encounter_type = ${6}   "
            + "                  AND o.concept_id = ${6273}   "
            + "                  AND o.value_coded IN (${1706}, ${1709}, ${1707}, ${1366}, ${23903})   "
            + "                  AND o2.obs_datetime = states.most_recent   "
            + "  AND NOT EXISTS( SELECT e.encounter_id from encounter e   "
            + "                                      INNER JOIN obs o3 on e.encounter_id = o3.encounter_id   "
            + "                where   "
            + "                        e.encounter_type = ${53}   "
            + "                  AND        o3.concept_id = ${6272}   "
            + "                  AND        o3.value_coded IN (${1706}, ${1709}, ${1707}, ${1366}, ${23903})   "
            + "                  AND        o3.obs_datetime = states.most_recent   "
            + "                GROUP BY e.encounter_id   ) "
            + "                GROUP BY p.patient_id   "
            + "            )   "
            + "        )   "
            + "    )   "
            + "GROUP BY p.patient_id ";

    sqlPatientDataDefinition.setQuery(new StringSubstitutor(valuesMap).replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>The system will show information from Last State Source as follows:</b>
   *
   * <p>If the value from Last State and Last State Date is Ficha Resumo (ARV_PICK_FR12) then the
   * value of Last State Source (Column I) should be Ficha Resumo, or
   *
   * <p>If the value from Last State and Last State Date is Clinical Consultation (ARV_PICK_FR12)
   * then the value of Last State Source (Column I) should be Ficha Clínica, or
   *
   * <p>If the value from Last State and Last State Date is Program Enrolment (ARV_PICK_FR12) then
   * the value of Last State Source (Column I) should be Programa SESP
   *
   * @see #getLastStateOrDateFromARTProgramOrFichaResumoOrClinica(boolean) to check how these
   *     (ARV_PICK_FR12) bullets are computed
   * @return {@link DataDefinition}
   */
  public DataDefinition getLastStateSourceFromARTProgramOrFichaResumoOrClinica() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(
        "Last state source on ART Program or Ficha Resumo or Ficha Clinica");
    addSqlCohortDefinitionParameters(sqlPatientDataDefinition);

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    valuesMap.put("1707", hivMetadata.getAbandonedConcept().getConceptId());
    valuesMap.put("1366", hivMetadata.getPatientHasDiedConcept().getConceptId());
    valuesMap.put("1709", hivMetadata.getSuspendedTreatmentConcept().getConceptId());
    valuesMap.put("23903", hivMetadata.getNegativeDiagnosisConcept().getConceptId());
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    valuesMap.put("2", hivMetadata.getARTProgram().getProgramId());
    valuesMap.put(
        "7",
        hivMetadata
            .getTransferredOutToAnotherHealthFacilityWorkflowState()
            .getProgramWorkflowStateId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("10", hivMetadata.getArtDeadWorkflowState().getProgramWorkflowStateId());
    valuesMap.put(
        "8", hivMetadata.getSuspendedTreatmentWorkflowState().getProgramWorkflowStateId());

    String query =
        " SELECT p.patient_id,  CASE "
            + "   WHEN (o.value_coded IS NULL AND ps.state IS NULL) THEN ''   "
            + "   WHEN o.value_coded IS NOT NULL THEN e.encounter_type   "
            + "   WHEN ps.state IS NOT NULL THEN pg.program_id   "
            + "   END AS state_source "
            + " FROM patient p  "
            + "           INNER JOIN encounter e ON p.patient_id = e.patient_id  "
            + "           INNER JOIN obs o ON e.encounter_id = o.encounter_id  "
            + "           INNER JOIN patient_program pg  "
            + "                      ON p.patient_id = pg.patient_id  "
            + "           INNER JOIN patient_state ps  "
            + "                      ON pg.patient_program_id = ps.patient_program_id  "
            + "           INNER JOIN( "
            + " SELECT recent.patient_id, MAX(recent.last_state_date) as most_recent FROM ( "
            + getUnionQuery()
            + " ) recent "
            + " GROUP BY recent.patient_id "
            + " )states on p.patient_id = states.patient_id "
            + "where   "
            + "        e.voided = 0   "
            + "  AND p.voided = 0   "
            + "  AND o.voided = 0   "
            + "  AND pg.voided = 0   "
            + "  AND ps.voided = 0   "
            + "  AND pg.location_id = :location   "
            + "AND (   "
            + "    (   "
            + "                e.encounter_type = ${53}   "
            + "            AND        o.concept_id = ${6272}   "
            + "            AND        o.value_coded IN (${1706}, ${1709}, ${1707}, ${1366}, ${23903})   "
            + "            AND        o.obs_datetime = states.most_recent   "
            + "        )   "
            + "    OR   "
            + "    (   "
            + "                e.encounter_type = ${6}   "
            + "            AND        o.concept_id = ${6273}   "
            + "            AND        o.value_coded IN (${1706}, ${1709} ,${1707}, ${1366}, ${23903})   "
            + "            AND        e.encounter_datetime = states.most_recent   "
            + "            AND NOT EXISTS(   "
            + "                SELECT e.encounter_id from encounter e   "
            + "                                      INNER JOIN obs o2 on e.encounter_id = o2.encounter_id   "
            + "                where   "
            + "                        e.encounter_type = ${53}   "
            + "                  AND        o2.concept_id = ${6272}   "
            + "                  AND        o2.value_coded IN (${1706}, ${1709}, ${1707}, ${1366}, ${23903})   "
            + "                  AND        o2.obs_datetime = states.most_recent   "
            + "                group by e.encounter_id   "
            + "            )   "
            + "        )   "
            + "    OR (   "
            + "            pg.program_id = ${2}   "
            + "            AND ps.state IN ( ${7}, ${8}, ${9}, ${10} )   "
            + "            AND   ps.start_date = states.most_recent   "
            + "AND pg.patient_id NOT IN (   "
            + "                SELECT p.patient_id   "
            + "                from patient p INNER JOIN encounter e ON p.patient_id = e.patient_id   "
            + "                               INNER JOIN obs o2 on e.encounter_id = o2.encounter_id   "
            + "                where e.encounter_type = ${6}   "
            + "                  AND o.concept_id = ${6273}   "
            + "                  AND o.value_coded IN (${1706}, ${1709}, ${1707}, ${1366}, ${23903})   "
            + "                  AND o2.obs_datetime = states.most_recent   "
            + "  AND NOT EXISTS( SELECT e.encounter_id from encounter e   "
            + "                                      INNER JOIN obs o3 on e.encounter_id = o3.encounter_id   "
            + "                where   "
            + "                        e.encounter_type = ${53}   "
            + "                  AND        o3.concept_id = ${6272}   "
            + "                  AND        o3.value_coded IN (${1706}, ${1709}, ${1707}, ${1366}, ${23903})   "
            + "                  AND        o3.obs_datetime = states.most_recent   "
            + "                GROUP BY e.encounter_id   ) "
            + "                GROUP BY p.patient_id   "
            + "            )   "
            + "        )   "
            + "    )   "
            + "GROUP BY p.patient_id ";

    sqlPatientDataDefinition.setQuery(new StringSubstitutor(valuesMap).replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>Patient’s Most Recent Drug Pick-Up Date on FILA</b>
   *
   * <p>Art Pickup Date: MAX(encounter.encounter_datetime) between the selected report start date
   * and end date of S.TARV: FARMACIA (ID=18) as Last ARV Pick-Up Date on FILA
   *
   * @return {@link CohortDefinition}
   */
  public DataDefinition getLastDrugPickupDate() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Most Recent Drug Pick-Up Date on FILA");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());

    String query =
        " SELECT p.patient_id, MAX(e.encounter_datetime) as last_pickup_date"
            + " FROM   patient p  "
            + "          INNER JOIN encounter e  "
            + "                          ON p.patient_id = e.patient_id  "
            + " WHERE  p.voided = 0  "
            + "          AND e.voided = 0  "
            + "          AND e.location_id = :location "
            + "          AND e.encounter_type = ${18} "
            + "         AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + " GROUP BY p.patient_id  ";

    sqlPatientDataDefinition.setQuery(new StringSubstitutor(valuesMap).replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b> this method will generate one union separeted query based on the given queries</b>
   *
   * @return {@link String}
   */
  private String getUnionQuery() {

    EptsQueriesUtil queriesUtil = new EptsQueriesUtil();

    return queriesUtil
        .unionBuilder(
            ListOfPatientsWhoPickeupArvDuringPeriodQueries.getLastStateOfStayOnFichaResumo())
        .union(ListOfPatientsWhoPickeupArvDuringPeriodQueries.getLastStateOfStayOnFichaClinica())
        .union(ListOfPatientsWhoPickeupArvDuringPeriodQueries.getLastStateOfStayOnArtProgram())
        .buildQuery();
  }

  private void addSqlCohortDefinitionParameters(SqlPatientDataDefinition sqlPatientDataDefinition) {
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
  }
}
