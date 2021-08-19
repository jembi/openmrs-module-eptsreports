package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.library.queries.TXCurrQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsDefaultersOrIITCohortQueries {

  @Autowired private TxCurrCohortQueries txCurrCohortQueries;

  @Autowired private CommonCohortQueries commonCohortQueries;

  @Autowired private GenericCohortQueries genericCohortQueries;

  @Autowired private HivMetadata hivMetadata;

  private final String MAPPING = "location=${location}";

  private final String MAPPING2 = "onOrBefore=${endDate},location=${location}";

  private final String MAPPING3 = "onOrBefore=${endDate}";

  /**
   * <b>E1</b> - exclude all patients who are transferred out by end of report generation date,
   *
   * <p><b>1.1</b> - All transferred-outs registered in Patient Program State by reporting end date,
   * i.e LAST Transferred out state in program enrollment by end of period.
   * Patient_program.program_id =2 = SERVICO TARV-TRATAMENTO and Patient_State.state = 7
   * (Transferred-out) or max(Patient_State.start_date) <= Report Generation Date => The most recent
   * start_date by Report Generation Date. Patient_state.end_date is null
   *
   * <p><b>1.2</b> - All transferred-outs registered in Ficha Resumo and Ficha Clinica of Master
   * Card by reporting end date Encounter Type ID= 6 Estado de Permanencia (Concept Id 6273) =
   * Transferred-out (Concept ID 1706) Encounter_datetime <= Report Generation Date OR Encounter
   * Type ID= 53 Estado de Permanencia (Concept Id 6272) = Transferred-out (Concept ID 1706)
   * obs_datetime <= Report Generation Date
   *
   * <p><b>1.3</b> - Exclude all patients who after the most recent date from 1.1 to 1.2, have a
   * drugs pick up or Consultation: Encounter Type ID= 6, 9, 18 and encounter_datetime> the most
   * recent date and <= Report Generation Date or Encounter Type ID = 52 and “Data de Levantamento”
   * (Concept Id 23866 value_datetime) > the most recent date and <= Report Generation Date
   * Encounter Type ID= 6, 9, 18 and encounter_datetime> the most recent date and <= Report
   * Generation Date or Encounter Type ID = 52 and “Data de Levantamento” (Concept Id 23866
   * value_datetime) > the most recent date and <= Report Generation Date
   *
   * @return sqlCohortDefinition
   */
  public CohortDefinition getE1() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition e11 = getTransferredOutByEndDate();
    CohortDefinition e12 = getTranferredOutPatients();
    CohortDefinition e13 = getPatientsConsultationAfterMostRecent();

    cd.addSearch("e11", EptsReportUtils.map(e11, MAPPING));
    cd.addSearch("e12", EptsReportUtils.map(e12, MAPPING));
    cd.addSearch("e13", EptsReportUtils.map(e13, MAPPING));

    cd.setCompositionString("(e11 OR e12) AND e13");

    return cd;
  }

  /**
   * <b>E2</b> - exclude all patients who died by Report Generation date,
   *
   * <p><b>2.1</b> - All deaths registered in Patient Program State by reporting end date
   * Patient_program.program_id =2 = SERVICO TARV-TRATAMENTO and Patient_State.state = 10 (Died)
   * Patient_State.start_date <= Report Generation Date Patient_state.end_date is null
   *
   * <p><b>2.2</b> - All deaths registered in Patient Demographics by reporting end date
   * Person.Dead=1 and death_date <= Report Generation Date
   *
   * <p><b>2.3</b> - All deaths registered in Last Home Visit Card by reporting end date Last Home
   * Visit Card (Encounter Type 21, 36, 37) Reason of Not Finding (Concept ID 2031 or 23944 or
   * 23945) = Died (Concept Id 1366) Last Encounter_datetime <= Report Generation Date
   *
   * <p><b>2.4</b> - All deaths registered in Ficha Resumo and Ficha Clinica of Master Card by
   * reporting end date Encounter Type ID= 6 Estado de Permanencia (Concept Id 6273) = Dead (Concept
   * ID 1366) Encounter_datetime <= Report Generation Date OR Encounter Type ID= 53 Estado de
   * Permanencia (Concept Id 6272) = Dead (Concept ID 1366) obs_datetime <= Report Generation Date
   *
   * @return sqlCohortDefinition
   */
  public CohortDefinition getE2() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition e21 = getE21();
    CohortDefinition e22 = getE22();
    CohortDefinition e23 =
            getPatientDeathRegisteredInLastHomeVisitCardByReportingEndDate();
    CohortDefinition e24 =
            getDeadPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate();
    CohortDefinition e25 = getE25();

    cd.addSearch("e21", EptsReportUtils.map(e21, MAPPING));
    cd.addSearch("e22", EptsReportUtils.map(e22, MAPPING));
    cd.addSearch("e23", EptsReportUtils.map(e23, MAPPING));
    cd.addSearch("e24", EptsReportUtils.map(e24, MAPPING));
    cd.addSearch("e25", EptsReportUtils.map(e25, MAPPING));

    cd.setCompositionString("(e21 OR e22 OR e23 OR e24) AND e25");

    return cd;
  }

  /**
   * <b>E3</b> - exclude all patients who stopped/suspended treatment by end of the reporting period, 
   *
   * <p><b>3.1</b> - All suspended registered in Patient Program State by reporting end date
   * i.e LAST Transferred out state in program enrollment by end of period.
   * Patient_program.program_id =2 = SERVICO TARV-TRATAMENTO and Patient_State.state = 8
   * (Suspended treatment) OR Patient_State.start_date <= ReportGenerationDate
   * start_date by Report Generation Date. Patient_state.end_date is null
   *
   * <p><b>2.2</b> - All suspensions registered in Ficha Resumo and Ficha Clinica of Master Card 
   * by reporting end date Encounter Type ID= 6 Estado de Permanencia (Concept Id 6273) =
   *  Suspended (Concept ID 1709) Encounter_datetime <= Report Generation Date OR Encounter
   * Type ID= 53 Estado de Permanencia (Concept Id 6272) =  Suspended (Concept ID 1709)
   * obs_datetime <= Report Generation Date
   *
   * <p><b>3.3</b> - Except all patients who after the most recent date from 3.1 to 3.2, have a
   * drugs pick up or Consultation: Encounter Type ID= 6, 9, 18 and encounter_datetime> the most
   * recent date and <= Report Generation Date or Encounter Type ID = 52 and “Data de Levantamento”
   * (Concept Id 23866 value_datetime) > the most recent date and <= Report Generation Date
   *
   * @return sqlCohortDefinition
   */
  public CohortDefinition getE3() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition e31 = getSuspendedOnPatientProgram();
    CohortDefinition e32 = getSuspensionsOnFichaResumoAndClinica();
    CohortDefinition e33 = getPatientsConsultationAfterMostRecentE3();

    cd.addSearch("e31", EptsReportUtils.map(e31, MAPPING));
    cd.addSearch("e32", EptsReportUtils.map(e32, MAPPING));
    cd.addSearch("e33", EptsReportUtils.map(e33, MAPPING));

    cd.setCompositionString("(e31 OR e32) AND e32");

    return cd;
  }

  /**
   * <b>2.4</b> - All deaths registered in Patient Program State by reporting end date *
   * Patient_program.program_id =2 = SERVICO TARV-TRATAMENTO and Patient_State.state = 10 (Died) *
   * Patient_State.start_date <= Report Generation Date Patient_state.end_date is null
   *
   * @return
   */
  public SqlCohortDefinition getE21() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" All patients that started ART during inclusion period ");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "EndDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("10", hivMetadata.getArtDeadWorkflowState().getProgramWorkflowStateId());

    String query =
        "SELECT p.patient_id FROM patient p    "
            + "    INNER JOIN patient_program pg ON pg.patient_id = p.patient_id "
            + "    INNER JOIN patient_state ps ON ps.patient_program_id = pg.patient_program_id "
            + " WHERE  pg.program_id = ${2}  "
            + " AND ps.state = ${10} "
            + " AND ps.start_date <= CURRENT_DATE() "
            + " AND ps.end_date IS NULL "
            + " AND pg.location_id = :location";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>2.2</b> - All deaths registered in Patient Demographics by reporting end date
   *    * Person.Dead=1 and death_date <= Report Generation Date
   *
   * @return
   */
  public SqlCohortDefinition getE22() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" All patients that started ART during inclusion period ");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "EndDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();

    String query =
            "SELECT p.person_id   "
                    + "                FROM person p  "
                    + "                INNER JOIN patient_program pg ON p.person_id=pg.patient_id "
                    + "                INNER JOIN patient_state ps ON pg.patient_program_id=ps.patient_program_id "
                    + "                WHERE p.dead=1  "
                    + "                AND p.death_date <= CURRENT_DATE()  "
                    + "                AND p.voided=0";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <p><b>2.3</b> - All deaths registered in Last Home Visit Card by reporting end date Last Home
   * Visit Card (Encounter Type 21, 36, 37) Reason of Not Finding (Concept ID 2031 or 23944 or
   * 23945) = Died (Concept Id 1366) Last Encounter_datetime <= Report Generation Date
   *
   * @param buscaActivaEncounterType
   * @param visitaApoioReintegracaoParteAEncounterType
   * @param visitaApoioReintegracaoParteBEncounterType
   * @param reasonPatientNotFound
   * @param reasonPatientNotFoundByActivist2ndVisit
   * @param reasonPatientNotFoundByActivist3rdVisit
   * @param patientIsDead
   * @return
   */
  public static String getPatientDeathRegisteredInLastHomeVisitCardByReportingEndDate(
          int buscaActivaEncounterType,
          int visitaApoioReintegracaoParteAEncounterType,
          int visitaApoioReintegracaoParteBEncounterType,
          int reasonPatientNotFound,
          int reasonPatientNotFoundByActivist2ndVisit,
          int reasonPatientNotFoundByActivist3rdVisit,
          int patientIsDead) {
    String query =
            "  SELECT  max_date.patient_id FROM  "
                    + "    (SELECT  "
                    + "      p.patient_id,  "
                    + "      MAX(e.encounter_datetime) last   "
                    + "    FROM patient p "
                    + "      INNER  JOIN encounter e ON e.patient_id=p.patient_id "
                    + "     WHERE  "
                    + "      e.encounter_datetime <= CURRENT_DATE() "
                    + "      AND e.location_id = :location "
                    + "      AND e.encounter_type  in( ${buscaActiva},${visitaApoioReintegracaoParteA},${visitaApoioReintegracaoParteB})  "
                    + "      AND e.voided=0 "
                    + "      AND p.voided = 0 "
                    + "    GROUP BY  p.patient_id  ) max_date "
                    + "    INNER  JOIN encounter ee "
                    + "            ON ee.patient_id = max_date.patient_id "
                    + "    INNER  JOIN obs o ON ee.encounter_id = o.encounter_id  "
                    + "        WHERE  "
                    + "        ( "
                    + "            (o.concept_id = ${reasonPatientNotFound} AND o.value_coded = ${patientIsDead}) OR "
                    + "            (o.concept_id = ${reasonPatientNotFoundByActivist2ndVisit} AND o.value_coded = ${patientIsDead}) OR "
                    + "            (o.concept_id = ${reasonPatientNotFoundByActivist3rdVisit} AND o.value_coded = ${patientIsDead} ) "
                    + "        )  "
                    + "    AND o.voided=0 "
                    + "    AND ee.voided = 0 "
                    + "    GROUP BY  max_date.patient_id";

    Map<String, Integer> map = new HashMap<>();
    map.put("buscaActiva", buscaActivaEncounterType);
    map.put("visitaApoioReintegracaoParteA", visitaApoioReintegracaoParteAEncounterType);
    map.put("visitaApoioReintegracaoParteB", visitaApoioReintegracaoParteBEncounterType);
    map.put("reasonPatientNotFound", reasonPatientNotFound);
    map.put("reasonPatientNotFoundByActivist2ndVisit", reasonPatientNotFoundByActivist2ndVisit);
    map.put("reasonPatientNotFoundByActivist3rdVisit", reasonPatientNotFoundByActivist3rdVisit);
    map.put("patientIsDead", patientIsDead);

    StringSubstitutor sub = new StringSubstitutor(map);
    return sub.replace(query);
  }

  public CohortDefinition getPatientDeathRegisteredInLastHomeVisitCardByReportingEndDate() {
    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("patientDeathRegisteredInLastHomeVisitCardByReportingEndDate");

    definition.setQuery(
            getPatientDeathRegisteredInLastHomeVisitCardByReportingEndDate(
                    hivMetadata.getBuscaActivaEncounterType().getEncounterTypeId(),
                    hivMetadata.getVisitaApoioReintegracaoParteAEncounterType().getEncounterTypeId(),
                    hivMetadata.getVisitaApoioReintegracaoParteBEncounterType().getEncounterTypeId(),
                    hivMetadata.getReasonPatientNotFound().getConceptId(),
                    hivMetadata.getReasonPatientNotFoundByActivist2ndVisitConcept().getConceptId(),
                    hivMetadata.getReasonPatientNotFoundByActivist3rdVisitConcept().getConceptId(),
                    hivMetadata.getPatientHasDiedConcept().getConceptId()));

    definition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));

    return definition;
  }

  /**
   <p><b>2.4</b> - All deaths registered in Ficha Resumo and Ficha Clinica of Master Card by
   * reporting end date Encounter Type ID= 6 Estado de Permanencia (Concept Id 6273) = Dead (Concept
   * ID 1366) Encounter_datetime <= Report Generation Date OR Encounter Type ID= 53 Estado de
   * Permanencia (Concept Id 6272) = Dead (Concept ID 1366) obs_datetime <= Report Generation Date
   *
   * @param adultoSeguimentoEncounterType
   * @param masterCardEncounterType
   * @param stateOfStayPriorArtPatientConcept
   * @param stateOfStayOfArtPatient
   * @param patientHasDiedConcept
   * @return
   */
  public static String getDeadPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate(
          int adultoSeguimentoEncounterType,
          int masterCardEncounterType,
          int stateOfStayPriorArtPatientConcept,
          int stateOfStayOfArtPatient,
          int patientHasDiedConcept) {

    Map<String, Integer> map = new HashMap<>();
    map.put("adultoSeguimentoEncounterType", adultoSeguimentoEncounterType);
    map.put("masterCardEncounterType", masterCardEncounterType);
    map.put("stateOfStayPriorArtPatientConcept", stateOfStayPriorArtPatientConcept);
    map.put("stateOfStayOfArtPatient", stateOfStayOfArtPatient);
    map.put("patientHasDiedConcept", patientHasDiedConcept);

    String query =
            "SELECT  p.patient_id  "
                    + "FROM patient p  "
                    + "    INNER JOIN encounter e  "
                    + "        ON e.patient_id=p.patient_id  "
                    + "    INNER JOIN obs o  "
                    + "        ON o.encounter_id=e.encounter_id  "
                    + "WHERE e.encounter_type = ${adultoSeguimentoEncounterType} "
                    + "    AND e.encounter_datetime <= CURRENT_DATE() "
                    + "    AND o.concept_id = ${stateOfStayOfArtPatient} "
                    + "    AND o.value_coded=${patientHasDiedConcept}  "
                    + "    AND e.location_id = :location  "
                    + "    AND p.voided=0   "
                    + "    AND e.voided=0  "
                    + "    AND o.voided=0  "
                    + "GROUP BY p.patient_id "
                    + "UNION "
                    + "SELECT  p.patient_id  "
                    + "FROM patient p  "
                    + "    INNER JOIN encounter e  "
                    + "        ON e.patient_id=p.patient_id  "
                    + "    INNER JOIN obs o  "
                    + "        ON o.encounter_id=e.encounter_id  "
                    + "WHERE e.encounter_type = ${masterCardEncounterType}  "
                    + "    AND o.obs_datetime <= CURRENT_DATE() "
                    + "    AND o.concept_id = ${stateOfStayPriorArtPatientConcept} "
                    + "    AND o.value_coded=${patientHasDiedConcept}  "
                    + "    AND e.location_id = :location  "
                    + "    AND p.voided=0   "
                    + "    AND e.voided=0  "
                    + "    AND o.voided=0  "
                    + "GROUP BY p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    return stringSubstitutor.replace(query);
  }

  public CohortDefinition getDeadPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate() {
    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("deadPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate");

    definition.setQuery(
            getDeadPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate(
                    hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                    hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                    hivMetadata.getStateOfStayPriorArtPatientConcept().getConceptId(),
                    hivMetadata.getStateOfStayOfArtPatient().getConceptId(),
                    hivMetadata.getPatientHasDiedConcept().getConceptId()));

    definition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));

    return definition;
  }


  /**
   * <b>2.4</b> - All deaths registered in Ficha Resumo and Ficha Clinica of Master Card by
   * reporting end date Encounter Type ID= 6 Estado de Permanencia (Concept Id 6273) = Dead (Concept
   * ID 1366) Encounter_datetime <= Report Generation Date OR Encounter Type ID= 53 Estado de
   * Permanencia (Concept Id 6272) = Dead (Concept ID 1366) obs_datetime <= Report Generation Date
   *
   * @return
   */
  public SqlCohortDefinition getE25() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" All patients that started ART during inclusion period ");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "EndDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("10", hivMetadata.getArtDeadWorkflowState().getProgramWorkflowStateId());
    map.put("21", hivMetadata.getBuscaActivaEncounterType().getEncounterTypeId());
    map.put("36", hivMetadata.getVisitaApoioReintegracaoParteAEncounterType().getEncounterTypeId());
    map.put("37", hivMetadata.getVisitaApoioReintegracaoParteBEncounterType().getEncounterTypeId());
    map.put("2031", hivMetadata.getReasonPatientNotFound().getConceptId());
    map.put(
        "23944", hivMetadata.getReasonPatientNotFoundByActivist2ndVisitConcept().getConceptId());
    map.put(
        "23945", hivMetadata.getReasonPatientNotFoundByActivist3rdVisitConcept().getConceptId());
    map.put("1366", hivMetadata.getPatientHasDiedConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("6272", hivMetadata.getStateOfStayPriorArtPatientConcept().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());

    String query =
        "SELECT patient_id "
            + "FROM   (SELECT transferout.patient_id,   Max(transferout.transferout_date) transferout_date "
            + "        FROM   (SELECT  max_date.patient_id, (max_date.last) AS transferout_date FROM "
            + "            (SELECT "
            + "                 p.patient_id, "
            + "                 MAX(e.encounter_datetime) last "
            + "             FROM patient p "
            + "                      INNER  JOIN encounter e ON e.patient_id=p.patient_id "
            + "             WHERE "
            + "                     e.encounter_datetime <= curdate() "
            + "               AND e.location_id = :location "
            + "               AND e.encounter_type  in(  ${21} , ${36} , ${37} ) "
            + "               AND e.voided=0 "
            + "               AND p.voided = 0 "
            + "             GROUP BY  p.patient_id  ) max_date "
            + "                INNER  JOIN encounter ee "
            + "                            ON ee.patient_id = max_date.patient_id "
            + "                INNER  JOIN obs o ON ee.encounter_id = o.encounter_id "
            + "                WHERE "
            + "                    ( (o.concept_id =  ${2031}  AND o.value_coded =  ${1366} ) OR "
            + "                            (o.concept_id =  ${23944}  AND o.value_coded =  ${1366} ) OR "
            + "                            (o.concept_id =  ${23945}  AND o.value_coded =  ${1366}  ) ) "
            + "                  AND o.voided=0 "
            + "                  AND ee.voided = 0 "
            + "                GROUP BY  max_date.patient_id "
            + " "
            + "                UNION "
            + "                 "
            + "                SELECT  p.patient_id , (e.encounter_datetime) AS transferout_date "
            + "                FROM patient p "
            + "                         INNER JOIN encounter e "
            + "                                    ON e.patient_id=p.patient_id "
            + "                         INNER JOIN obs o "
            + "                                    ON o.encounter_id=e.encounter_id "
            + "                WHERE e.encounter_type =  ${6} "
            + "                  AND e.encounter_datetime <= curdate() "
            + "                  AND o.concept_id =  ${6273} "
            + "                  AND o.value_coded= ${1366} "
            + "                  AND e.location_id = :location "
            + "                  AND p.voided= 0 "
            + "                  AND e.voided= 0 "
            + "                  AND o.voided= 0 "
            + "                GROUP BY p.patient_id "
            + " "
            + "                UNION "
            + " "
            + "                SELECT pg.patient_id, (ps.start_date) AS transferout_date FROM patient p "
            + "                INNER JOIN patient_program pg ON p.patient_id=pg.patient_id "
            + "                INNER JOIN patient_state ps ON pg.patient_program_id=ps.patient_program_id "
            + "                WHERE pg.voided=0 AND ps.voided=0 AND p.voided=0 "
            + "                AND pg.program_id=${2} AND ps.state=${10} AND ps.end_date is null "
            + "                AND ps.start_date <= curdate() AND location_id=:location "
            + " "
            + "                UNION "
            + " "
            + "                SELECT p.person_id, (ps.start_date) AS transferout_date   "
            + "                FROM person p  "
            + "                INNER JOIN patient_program pg ON p.person_id=pg.patient_id "
            + "                INNER JOIN patient_state ps ON pg.patient_program_id=ps.patient_program_id "
            + "                WHERE p.dead= 1  "
            + "                AND p.death_date <= curdate()  "
            + "                AND p.voided=0 "
            + " "
            + "                UNION "
            + " "
            + "                SELECT  p.patient_id, (e.encounter_datetime) AS transferout_date "
            + "                FROM patient p "
            + "                         INNER JOIN encounter e "
            + "                                    ON e.patient_id=p.patient_id "
            + "                         INNER JOIN obs o "
            + "                                    ON o.encounter_id=e.encounter_id "
            + "                WHERE e.encounter_type =  ${53} "
            + "                  AND o.obs_datetime <= curdate() "
            + "                  AND o.concept_id =  ${6272} "
            + "                  AND o.value_coded= ${1366} "
            + "                  AND e.location_id = :location "
            + "                  AND p.voided=0 "
            + "                  AND e.voided=0 "
            + "                  AND o.voided=0 "
            + "                GROUP BY p.patient_id) transferout "
            + "        GROUP  BY transferout.patient_id) max_transferout "
            + " WHERE  max_transferout.patient_id NOT IN (SELECT p.patient_id "
            + "                                          FROM   patient p "
            + "                                                     JOIN encounter e "
            + "                                                          ON p.patient_id = "
            + "                                                             e.patient_id "
            + "                                          WHERE  p.voided = 0 "
            + "                                            AND e.voided = 0 "
            + "                                            AND e.encounter_type IN (${6},${9},${18})  "
            + "                                            AND e.location_id = :location "
            + "                                            AND "
            + "                                                  e.encounter_datetime > transferout_date "
            + "                                            AND "
            + "                                                  e.encounter_datetime <= curdate() "
            + "                                          UNION "
            + "                                          SELECT p.patient_id "
            + "                                          FROM   patient p "
            + "                                                     JOIN encounter e "
            + "                                                          ON p.patient_id = "
            + "                                                             e.patient_id "
            + "                                                     JOIN obs o "
            + "                                                          ON e.encounter_id = "
            + "                                                             o.encounter_id "
            + "                                          WHERE  p.voided = 0 "
            + "                                            AND e.voided = 0 "
            + "                                            AND e.encounter_type =  ${52} "
            + "                                            AND e.location_id = :location "
            + "                                            AND o.concept_id =  ${23866} "
            + "                                            AND o.value_datetime > transferout_date "
            + "                                            AND o.value_datetime <= curdate()); ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>E1</b> - exclude all patients who are transferred out by end of report generation date,
   *
   * <p><b>1.1</b> - All transferred-outs registered in Patient Program State by reporting end date,
   * i.e LAST Transferred out state in program enrollment by end of period.
   * Patient_program.program_id =2 = SERVICO TARV-TRATAMENTO and Patient_State.state = 7
   * (Transferred-out) or max(Patient_State.start_date) <= Report Generation Date => The most recent
   * start_date by Report Generation Date. Patient_state.end_date is null
   *
   * @return sqlCohortDefinition
   */
  public CohortDefinition getTransferredOutByEndDate() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "All transferred-outs registered in Patient Program State by reporting end date");
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put(
        "7",
        hivMetadata
            .getTransferredOutToAnotherHealthFacilityWorkflowState()
            .getProgramWorkflowStateId());

    String query =
        " SELECT p.patient_id "
            + " FROM   patient p  "
            + "        INNER JOIN patient_program pg "
            + "                ON p.patient_id = pg.patient_id "
            + "        INNER JOIN patient_state ps "
            + "                ON pg.patient_program_id = ps.patient_program_id "
            + " WHERE  pg.location_id = :location "
            + "        AND pg.program_id = ${2} "
            + "        AND ps.state = ${7} "
            + " UNION "
            + " SELECT p.patient_id "
            + " FROM   patient p "
            + "        INNER JOIN (SELECT pp.patient_id, "
            + "                           Max(ps.start_date) AS max_startDate "
            + "                    FROM   patient pp "
            + "                           INNER JOIN patient_program pg "
            + "                                   ON pp.patient_id = pg.patient_id "
            + "                           INNER JOIN patient_state ps "
            + "                                   ON pg.patient_program_id = "
            + "                                      ps.patient_program_id "
            + "                    WHERE  pg.location_id = :location "
            + "                           AND ps.start_date IS NOT NULL "
            + "                    GROUP  BY pp.patient_id) AS tbl "
            + "                ON p.patient_id = tbl.patient_id "
            + " WHERE  p.voided = 0 "
            + "        AND tbl.max_startdate <= curdate()  ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>E1</b> - exclude all patients who are transferred out by end of report generation date,
   *
   * <p><b>1.2</b> - All transferred-outs registered in Ficha Resumo and Ficha Clinica of Master
   * Card by reporting end date Encounter Type ID= 6 Estado de Permanencia (Concept Id 6273) =
   * Transferred-out (Concept ID 1706) Encounter_datetime <= Report Generation Date OR Encounter
   * Type ID= 53 Estado de Permanencia (Concept Id 6272) = Transferred-out (Concept ID 1706)
   * obs_datetime <= Report Generation Date
   *
   * @return sqlCohortDefinition
   */
  public CohortDefinition getTranferredOutPatients() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "All transferred-outs registered in Ficha Resumo and Ficha Clinica of Master Card by reporting end date");
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());

    String query =
        " SELECT p.patient_id "
            + "     FROM   patient p "
            + "            JOIN encounter e "
            + "              ON p.patient_id = e.patient_id "
            + "            JOIN obs o "
            + "              ON e.encounter_id = o.encounter_id "
            + "     WHERE  p.voided = 0 "
            + "            AND e.voided = 0 "
            + "            AND e.location_id = :location "
            + "            AND e.encounter_type = ${6} "
            + "            AND e.encounter_datetime <= curdate() "
            + "            AND o.voided = 0 "
            + "            AND o.concept_id = ${6273} "
            + "            AND o.value_coded = ${1706} "
            + "     GROUP  BY p.patient_id "
            + "     UNION "
            + "     SELECT p.patient_id "
            + "     FROM   patient p "
            + "            JOIN encounter e "
            + "              ON p.patient_id = e.patient_id "
            + "            JOIN obs o "
            + "              ON e.encounter_id = o.encounter_id "
            + "     WHERE  p.voided = 0 "
            + "            AND e.voided = 0 "
            + "            AND e.location_id = :location "
            + "            AND e.encounter_type = ${53} "
            + "            AND o.obs_datetime <= curdate() "
            + "            AND o.voided = 0 "
            + "            AND o.concept_id = ${6272} "
            + "            AND o.value_coded = ${1706} "
            + "     GROUP  BY p.patient_id  ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>E1</b> - exclude all patients who are transferred out by end of report generation date,
   *
   * <p><b>1.3</b> - Exclude all patients who after the most recent date from 1.1 to 1.2, have a
   * drugs pick up or Consultation: Encounter Type ID= 6, 9, 18 and encounter_datetime> the most
   * recent date and <= Report Generation Date or Encounter Type ID = 52 and “Data de Levantamento”
   * (Concept Id 23866 value_datetime) > the most recent date and <= Report Generation Date
   * Encounter Type ID= 6, 9, 18 and encounter_datetime> the most recent date and <= Report
   * Generation Date or Encounter Type ID = 52 and “Data de Levantamento” (Concept Id 23866
   * value_datetime) > the most recent date and <= Report Generation Date
   *
   * @return sqlCohortDefinition
   */
  public CohortDefinition getPatientsConsultationAfterMostRecent() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "all patients who after the most recent date from 1.1 to 1.2, have a drugs pick up or Consultation");
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put(
        "7",
        hivMetadata
            .getTransferredOutToAnotherHealthFacilityWorkflowState()
            .getProgramWorkflowStateId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());

    String query =
        " SELECT patient_id "
            + " FROM   (SELECT transferout.patient_id, "
            + "                Max(transferout.transferout_date) transferout_date "
            + "         FROM   (SELECT p.patient_id, "
            + "                        Max(ps.start_date) AS transferout_date "
            + "                 FROM   patient p "
            + "                        INNER JOIN patient_program pg "
            + "                                ON p.patient_id = pg.patient_id "
            + "                        INNER JOIN patient_state ps "
            + "                                ON pg.patient_program_id = ps.patient_program_id "
            + "                 WHERE  pg.location_id = :location "
            + "                        AND pg.program_id = ${2} "
            + "                        AND ps.state = ${7} "
            + "                 GROUP  BY p.patient_id "
            + "                 UNION "
            + "                 SELECT p.patient_id, "
            + "                        tbl.max_startdate AS transferout_date "
            + "                 FROM   patient p "
            + "                        INNER JOIN (SELECT pp.patient_id, "
            + "                                           Max(ps.start_date) AS max_startDate "
            + "                                    FROM   patient pp "
            + "                                           INNER JOIN patient_program pg "
            + "                                                   ON pp.patient_id = "
            + "                                                      pg.patient_id "
            + "                                           INNER JOIN patient_state ps "
            + "                                                   ON pg.patient_program_id = "
            + "                                                      ps.patient_program_id "
            + "                                    WHERE  pg.location_id = :location "
            + "                                           AND ps.start_date IS NOT NULL "
            + "                                    GROUP  BY pp.patient_id) AS tbl "
            + "                                ON p.patient_id = tbl.patient_id "
            + "                 WHERE  p.voided = 0 "
            + "                        AND tbl.max_startdate <= curdate() "
            + "                 UNION "
            + "                 SELECT p.patient_id, "
            + "                        Max(e.encounter_datetime) AS transferout_date "
            + "                 FROM   patient p "
            + "                        JOIN encounter e "
            + "                          ON p.patient_id = e.patient_id "
            + "                        JOIN obs o "
            + "                          ON e.encounter_id = o.encounter_id "
            + "                 WHERE  p.voided = 0 "
            + "                        AND e.voided = 0 "
            + "                        AND e.location_id = :location "
            + "                        AND e.encounter_type = ${6} "
            + "                        AND e.encounter_datetime <= curdate() "
            + "                        AND o.voided = 0 "
            + "                        AND o.concept_id = ${6273} "
            + "                        AND o.value_coded = ${1706} "
            + "                 GROUP  BY p.patient_id "
            + "                 UNION "
            + "                 SELECT p.patient_id, "
            + "                        Max(o.obs_datetime) AS transferout_date "
            + "                 FROM   patient p "
            + "                        JOIN encounter e "
            + "                          ON p.patient_id = e.patient_id "
            + "                        JOIN obs o "
            + "                          ON e.encounter_id = o.encounter_id "
            + "                 WHERE  p.voided = 0 "
            + "                        AND e.voided = 0 "
            + "                        AND e.location_id = :location "
            + "                        AND e.encounter_type = ${53} "
            + "                        AND o.obs_datetime <= curdate() "
            + "                        AND o.voided = 0 "
            + "                        AND o.concept_id = ${6272} "
            + "                        AND o.value_coded = ${1706} "
            + "                 GROUP  BY p.patient_id) transferout "
            + "         GROUP  BY transferout.patient_id) max_transferout "
            + " WHERE  max_transferout.patient_id NOT IN (SELECT p.patient_id "
            + "                                           FROM   patient p "
            + "                                                  JOIN encounter e "
            + "                                                    ON p.patient_id = "
            + "                                                       e.patient_id "
            + "                                           WHERE  p.voided = 0 "
            + "                                                  AND e.voided = 0 "
            + "                                                  AND e.encounter_type IN ( ${6}, ${9}, "
            + "                                                      ${18} ) "
            + "                                                  AND e.location_id = :location "
            + "                                                  AND "
            + "               e.encounter_datetime > transferout_date "
            + "                                                  AND "
            + "               e.encounter_datetime <= curdate() "
            + "                                           UNION "
            + "                                           SELECT p.patient_id "
            + "                                           FROM   patient p "
            + "                                                  JOIN encounter e "
            + "                                                    ON p.patient_id = "
            + "                                                       e.patient_id "
            + "                                                  JOIN obs o "
            + "                                                    ON e.encounter_id = "
            + "                                                       o.encounter_id "
            + "                                           WHERE  p.voided = 0 "
            + "                                                  AND e.voided = 0 "
            + "                                                  AND e.encounter_type = ${52} "
            + "                                                  AND e.location_id = :location "
            + "                                                  AND o.concept_id = ${23866} "
            + "                                                  AND o.value_datetime > "
            + "                                                      transferout_date "
            + "                                                  AND o.value_datetime <= "
            + "                                                      curdate())";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

/**
   * <b>E3</b> - exclude all patients who stopped/suspended treatment by end of the reporting period, 
   * <p>3.1 - All suspended registered in Patient Program State by reporting end date
   * Patient_program.program_id =2 = SERVICO TARV-TRATAMENTO and
   * Patient_State.state = 8 (Suspended treatment) or
   * Patient_State.start_date <= ReportGenerationDate
   * Patient_state.end_date is null
   * 
   * @return sqlCohortDefinition
   */
  public CohortDefinition getSuspendedOnPatientProgram() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("All suspended registered in Patient Program State by reporting end date");
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("8", hivMetadata.getSuspendedTreatmentWorkflowState().getProgramWorkflowStateId());

    String query = 
    " SELECT  p.patient_id FROM   patient p     "    
         + "       INNER JOIN patient_program pg ON p.patient_id = pg.patient_id "
         + "       INNER JOIN patient_state ps ON  pg.patient_program_id = ps.patient_program_id "
         + "   WHERE pg.location_id = :location AND pg.program_id = ${2} "
         + "       AND ps.state = ${8} OR (ps.start_date <= curdate() AND ps.end_date IS NULL) "
         + " GROUP BY p.patient_id ";


         StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

         sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));
     
         return sqlCohortDefinition;
  }

/**
   * <b>E3</b> - exclude all patients who stopped/suspended treatment by end of the reporting period, 
   *
   * <p><b>3.2</b> - All suspensions registered in Ficha Resumo and Ficha Clinica of Master Card by reporting end date
   * Encounter Type ID= 6
   * Estado de Permanencia (Concept Id 6273) =  Suspended (Concept ID 1709)
   * Encounter_datetime <= ReportGenerationDate
   * OR
   * Encounter Type ID= 53
   * Estado de Permanencia (Concept Id 6272) =  Suspended (Concept ID 1709)
   * obs_datetime <= ReportGenerationDate
   *
   * @return sqlCohortDefinition
   */
  public CohortDefinition getSuspensionsOnFichaResumoAndClinica() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "All suspensions registered in Ficha Resumo and Ficha Clinica of Master Card by reporting end date");
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("1709", hivMetadata.getSuspendedTreatmentConcept().getConceptId());

    String query =
        " SELECT p.patient_id "
            + "     FROM   patient p "
            + "            JOIN encounter e "
            + "              ON p.patient_id = e.patient_id "
            + "            JOIN obs o "
            + "              ON e.encounter_id = o.encounter_id "
            + "     WHERE  p.voided = 0 "
            + "            AND e.voided = 0 "
            + "            AND e.location_id = :location "
            + "            AND e.encounter_type = ${6} "
            + "            AND e.encounter_datetime <= curdate() "
            + "            AND o.voided = 0 "
            + "            AND o.concept_id = ${6273} "
            + "            AND o.value_coded = ${1709} "
            + "     GROUP  BY p.patient_id "
            + "     UNION "
            + "     SELECT p.patient_id "
            + "     FROM   patient p "
            + "            JOIN encounter e "
            + "              ON p.patient_id = e.patient_id "
            + "            JOIN obs o "
            + "              ON e.encounter_id = o.encounter_id "
            + "     WHERE  p.voided = 0 "
            + "            AND e.voided = 0 "
            + "            AND e.location_id = :location "
            + "            AND e.encounter_type = ${53} "
            + "            AND o.obs_datetime <= curdate() "
            + "            AND o.voided = 0 "
            + "            AND o.concept_id = ${6272} "
            + "            AND o.value_coded = ${1709} "
            + "     GROUP  BY p.patient_id  ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>E3</b> - exclude all patients who stopped/suspended treatment by end of the reporting period,
   *
   * <p><b>3.3</b> - Except all patients who after the most recent date from 3.1 to 3.2, have a drugs pick up or consultation:
   * Encounter Type ID= 6, 9, 18 and encounter_datetime> the most
   * recent date and <= Report Generation Date or Encounter Type ID = 52 and “Data de Levantamento”
   * (Concept Id 23866 value_datetime) > the most recent date and <= Report Generation Date
   * Encounter Type ID= 6, 9, 18 and encounter_datetime> the most recent date and <= Report
   * Generation Date or Encounter Type ID = 52 and “Data de Levantamento” (Concept Id 23866
   * value_datetime) > the most recent date and <= Report Generation Date
   *
   * @return sqlCohortDefinition
   */
  public CohortDefinition getPatientsConsultationAfterMostRecentE3() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("all patients who after the most recent date from 3.1 to 3.2, have a drugs pick up or consultation");
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("8", hivMetadata.getSuspendedTreatmentWorkflowState().getProgramWorkflowStateId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1709", hivMetadata.getSuspendedTreatmentConcept().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());

    String query =
    " SELECT patient_id   "
    + " FROM   (SELECT transferout.patient_id, "
    + "               Max(transferout.transferout_date) transferout_date "
    + "        FROM   (SELECT p.patient_id, "
    + "                       Max(ps.start_date) AS transferout_date "
    + "                FROM   patient p "
    + "                       INNER JOIN patient_program pg "
    + "                               ON p.patient_id = pg.patient_id "
    + "                       INNER JOIN patient_state ps "
    + "                               ON pg.patient_program_id = ps.patient_program_id "
    + "                WHERE  pg.location_id = :location "
    + "                       AND pg.program_id = ${2} "
    + "                       AND ps.state = ${8} "
    + "                        OR ( ps.start_date <= curdate() "
    + "                             AND ps.end_date IS NULL ) "
    + "                GROUP  BY p.patient_id "
    + "                UNION "
    + "                SELECT p.patient_id, "
    + "                       Max(e.encounter_datetime) AS transferout_date "
    + "                FROM   patient p "
    + "                       JOIN encounter e "
    + "                         ON p.patient_id = e.patient_id "
    + "                       JOIN obs o "
    + "                         ON e.encounter_id = o.encounter_id "
    + "                WHERE  p.voided = 0 "
    + "                       AND e.voided = 0 "
    + "                       AND e.location_id = :location "
    + "                       AND e.encounter_type = ${6} "
    + "                       AND e.encounter_datetime <= curdate() "
    + "                       AND o.voided = 0 "
    + "                       AND o.concept_id = ${6273} "
    + "                       AND o.value_coded = ${1709} "
    + "                GROUP  BY p.patient_id "
    + "                UNION "
    + "                SELECT p.patient_id, "
    + "                       Max(o.obs_datetime) AS transferout_date "
    + "                FROM   patient p "
    + "                       JOIN encounter e "
    + "                         ON p.patient_id = e.patient_id "
    + "                       JOIN obs o "
    + "                         ON e.encounter_id = o.encounter_id "
    + "                WHERE  p.voided = 0 "
    + "                       AND e.voided = 0 "
    + "                       AND e.location_id = :location "
    + "                       AND e.encounter_type = ${53} "
    + "                       AND o.obs_datetime <= curdate() "
    + "                       AND o.voided = 0 "
    + "                       AND o.concept_id = ${6272} "
    + "                       AND o.value_coded = ${1709} "
    + "                GROUP  BY p.patient_id) transferout "
    + "        GROUP  BY transferout.patient_id) max_transferout "
    + " WHERE  max_transferout.patient_id NOT IN (SELECT p.patient_id "
    + "                                          FROM   patient p "
    + "                                                 JOIN encounter e "
    + "                                                   ON p.patient_id = "
    + "                                                      e.patient_id "
    + "                                          WHERE  p.voided = 0 "
    + "                                                 AND e.voided = 0 "
    + "                                                 AND e.encounter_type IN ( ${6}, ${9}, "
    + "                                                     ${18} ) "
    + "                                                 AND e.location_id = :location  "
    + "                                                 AND "
    + "              e.encounter_datetime > transferout_date "
    + "                                                 AND e.encounter_datetime <= "
    + "                                                     curdate() "
    + "                                          UNION "
    + "                                          SELECT p.patient_id "
    + "                                          FROM   patient p "
    + "                                                 JOIN encounter e "
    + "                                                   ON p.patient_id = "
    + "                                                      e.patient_id "
    + "                                                 JOIN obs o "
    + "                                                   ON e.encounter_id = "
    + "                                                      o.encounter_id "
    + "                                          WHERE  p.voided =  "
    + "                                                 AND e.voided = 0 "
    + "                                                 AND e.encounter_type = ${52} "
    + "                                                 AND e.location_id = :location "
    + "                                                 AND o.concept_id = ${23866} "
    + "                                                 AND o.value_datetime > "
    + "                                                     transferout_date "
    + "                                                 AND o.value_datetime <= curdate "
    + "                                                     ())  ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

}
