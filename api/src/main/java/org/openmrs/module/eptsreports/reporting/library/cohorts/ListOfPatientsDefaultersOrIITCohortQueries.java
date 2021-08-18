package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
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

  private final String MAPPING = "endDate=${endDate},location=${location}";

  private final String MAPPING2 = "onOrBefore=${endDate},location=${location}";

  private final String MAPPING3 = "onOrBefore=${endDate}";

  /**
     * <b>E1</b> - exclude all patients who are transferred out by end of report generation date,  
     * 
     * <p><b>1.1</b> -
     * All transferred-outs registered in Patient Program State by reporting end date, i.e LAST Transferred out state in program enrollment by end of period.
     * Patient_program.program_id =2 = SERVICO TARV-TRATAMENTO and
     * Patient_State.state = 7 (Transferred-out) or 
     * max(Patient_State.start_date) <= Report Generation Date => The most recent start_date by Report Generation Date. 
     * Patient_state.end_date is null
     * <p><b>1.2</b> -
     * All transferred-outs registered in Ficha Resumo and Ficha Clinica of Master Card by reporting end date
     * Encounter Type ID= 6
     * Estado de Permanencia (Concept Id 6273) = Transferred-out (Concept ID 1706)
     * Encounter_datetime <= Report Generation Date OR  
     * Encounter Type ID= 53
     * Estado de Permanencia (Concept Id 6272) = Transferred-out (Concept ID 1706)
     * obs_datetime <= Report Generation Date
     * <p><b>1.3</b> -
     * Exclude all patients who after the most recent date from 1.1 to 1.2, have a drugs pick up or Consultation:
     * Encounter Type ID= 6, 9, 18 and encounter_datetime> the most recent date and <= Report Generation Date or 
     * Encounter Type ID = 52 and “Data de Levantamento” (Concept Id 23866 value_datetime) > the most recent date and <= Report Generation Date
     * Encounter Type ID= 6, 9, 18 and encounter_datetime> the most recent date and <= Report Generation Date 
     * or Encounter Type ID = 52 and “Data de Levantamento” (Concept Id 23866 value_datetime) > the most recent date and <= Report Generation Date
     * @return sqlCohortDefinition
     */
  public CohortDefinition getE1() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition e11 = getTransferredOutByEndDate();
    CohortDefinition e12 = getTranferredOutPatients();
    CohortDefinition e13 = getPatientsConsultationAfterMostRecent();

    cd.addSearch("A", EptsReportUtils.map(e11, MAPPING));
    cd.addSearch("B", EptsReportUtils.map(e12, MAPPING));
    cd.addSearch("C", EptsReportUtils.map(e13, MAPPING));

    cd.setCompositionString(
            "(A OR B) AND C");

    return cd;
  }

  public CohortDefinition getE2() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition e21 = getPatientsInArtCareWhoDied();
    CohortDefinition e22 = txCurrCohortQueries.getDeadPatientsInDemographiscByReportingEndDate();
    CohortDefinition e23 =
        txCurrCohortQueries.getPatientDeathRegisteredInLastHomeVisitCardByReportingEndDate();
    CohortDefinition e24 =
        txCurrCohortQueries.getDeadPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate();
    CohortDefinition e25 = getE15();

    cd.addSearch("e21", EptsReportUtils.map(e21, MAPPING));
    cd.addSearch("e22", EptsReportUtils.map(e22, MAPPING3));
    cd.addSearch("e23", EptsReportUtils.map(e23, MAPPING2));
    cd.addSearch("e24", EptsReportUtils.map(e24, MAPPING2));
    cd.addSearch("e25", EptsReportUtils.map(e25, MAPPING));


    cd.setCompositionString("(e21 OR e22 OR e23 OR e24) AND e25");

    return cd;
  }

  public CohortDefinition getPatientsInArtCareWhoDied() {
    Program hivCareProgram = hivMetadata.getARTProgram();
    ProgramWorkflowState dead = hivMetadata.getArtDeadWorkflowState();
    return genericCohortQueries.getPatientsBasedOnPatientStatesBeforeDate(
        hivCareProgram.getProgramId(), dead.getProgramWorkflowStateId());
  }


  public SqlCohortDefinition getE15() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" All patients that started ART during inclusion period ");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "EndDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("10", hivMetadata.getArtDeadWorkflowState().getProgramWorkflowStateId());
    map.put("21",hivMetadata.getBuscaActivaEncounterType().getEncounterTypeId());
    map.put("36", hivMetadata.getVisitaApoioReintegracaoParteAEncounterType().getEncounterTypeId());
    map.put("37", hivMetadata.getVisitaApoioReintegracaoParteBEncounterType().getEncounterTypeId());
    map.put("2031", hivMetadata.getReasonPatientNotFound().getConceptId());
    map.put("23944", hivMetadata.getReasonPatientNotFoundByActivist2ndVisitConcept().getConceptId());
    map.put("23945",hivMetadata.getReasonPatientNotFoundByActivist3rdVisitConcept().getConceptId());
    map.put("1366", hivMetadata.getPatientHasDiedConcept().getConceptId());
    map.put("6",hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("9",hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("18",hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("6272", hivMetadata.getStateOfStayPriorArtPatientConcept().getConceptId());
    map.put("6273",  hivMetadata.getStateOfStayOfArtPatient().getConceptId());
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
                    + "                     e.encounter_datetime <= :endDate "
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
                    + "                  AND e.encounter_datetime <= :endDate "
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
                    + "                AND ps.start_date <= :endDate AND location_id=:location "
                    + " "
                    + "                UNION "
                    + " "
                    + "                SELECT p.person_id, (ps.start_date) AS transferout_date   "
                    + "                FROM person p  "
                    + "                INNER JOIN patient_program pg ON p.person_id=pg.patient_id "
                    + "                INNER JOIN patient_state ps ON pg.patient_program_id=ps.patient_program_id "
                    + "                WHERE p.dead= 1  "
                    + "                AND p.death_date <= :endDate  "
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
                    + "                  AND o.obs_datetime <= :endDate "
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
                    + "                                                  e.encounter_datetime <= :endDate "
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
                    + "                                            AND o.value_datetime <= :endDate); ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
     * <b>E1</b> - exclude all patients who are transferred out by end of report generation date,  
     * 
     * <p><b>1.1</b> -
     * All transferred-outs registered in Patient Program State by reporting end date, i.e LAST Transferred out state in program enrollment by end of period.
     * Patient_program.program_id =2 = SERVICO TARV-TRATAMENTO and
     * Patient_State.state = 7 (Transferred-out) or 
     * max(Patient_State.start_date) <= Report Generation Date => The most recent start_date by Report Generation Date. 
     * Patient_state.end_date is null

     * @return sqlCohortDefinition
     */
    public CohortDefinition getTransferredOutByEndDate() {

        SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
            sqlCohortDefinition.setName("All transferred-outs registered in Patient Program State by reporting end date");
            sqlCohortDefinition.addParameter(new Parameter("endDate", "EndDate", Date.class));
            sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));
    
            Map<String, Integer> map = new HashMap<>();
            map.put("2", hivMetadata.getARTProgram().getProgramId());
            map.put("7", hivMetadata.getTransferredOutToAnotherHealthFacilityWorkflowState().getProgramWorkflowStateId());
    
            String query = " SELECT p.patient_id "
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
              + "        AND tbl.max_startdate <= :endDate  ";
    
              StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
              sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));
    
        return sqlCohortDefinition;
           
       }
       
       /**
         * <b>E1</b> - exclude all patients who are transferred out by end of report generation date,  
         * 
         * <p><b>1.2</b> -
         * All transferred-outs registered in Ficha Resumo and Ficha Clinica of Master Card by reporting end date
         * Encounter Type ID= 6
         * Estado de Permanencia (Concept Id 6273) = Transferred-out (Concept ID 1706)
         * Encounter_datetime <= Report Generation Date OR  
         * Encounter Type ID= 53
         * Estado de Permanencia (Concept Id 6272) = Transferred-out (Concept ID 1706)
         * obs_datetime <= Report Generation Date
    
         * @return sqlCohortDefinition
         */
       public CohortDefinition getTranferredOutPatients() {
    
        SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
        sqlCohortDefinition.setName("All transferred-outs registered in Ficha Resumo and Ficha Clinica of Master Card by reporting end date");
        sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
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
           + "            AND e.encounter_datetime <= :endDate "
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
           + "            AND o.obs_datetime <= :endDate "
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
         * <p><b>1.3</b> -
         * Exclude all patients who after the most recent date from 1.1 to 1.2, have a drugs pick up or Consultation:
         * Encounter Type ID= 6, 9, 18 and encounter_datetime> the most recent date and <= Report Generation Date or 
         * Encounter Type ID = 52 and “Data de Levantamento” (Concept Id 23866 value_datetime) > the most recent date and <= Report Generation Date
         * Encounter Type ID= 6, 9, 18 and encounter_datetime> the most recent date and <= Report Generation Date 
         * or Encounter Type ID = 52 and “Data de Levantamento” (Concept Id 23866 value_datetime) > the most recent date and <= Report Generation Date
         * @return sqlCohortDefinition
         */
      public CohortDefinition getPatientsConsultationAfterMostRecent() {
    
        SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
        sqlCohortDefinition.setName("all patients who after the most recent date from 1.1 to 1.2, have a drugs pick up or Consultation");
        sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
        sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    
        Map<String, Integer> map = new HashMap<>();
        map.put("2", hivMetadata.getARTProgram().getProgramId());
        map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
        map.put("7", hivMetadata.getTransferredOutToAnotherHealthFacilityWorkflowState().getProgramWorkflowStateId());
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
               + "                        AND tbl.max_startdate <= :endDate "
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
               + "                        AND e.encounter_datetime <= :endDate "
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
               + "                        AND o.obs_datetime <= :endDate "
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
               + "               e.encounter_datetime <= :endDate "
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
               + "                                                      :endDate)";
    
        StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    
        sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));
    
        return sqlCohortDefinition;
      }
}
