package org.openmrs.module.eptsreports.reporting.library.cohorts;

import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class TPTInitiationDataDefinitionQueries {

    private HivMetadata hivMetadata;

    @Autowired
    public TPTInitiationDataDefinitionQueries(HivMetadata hivMetadata) {
        this.hivMetadata = hivMetadata;
    }

    /**
     * <b>Technical Specs</b>
     *
     * <blockquote>
     *
     * <p>Patient’s birth date information registered in the system should be used to calculate the age of the patient at the end date of reporting period (reporting end date minus birthdate / 365)</p>
     * <p>Patients without birth date information will be considered as unknown age and the corresponding cell in the excel file will be filled with N/A</p>
     *
     * </blockquote>
     *
     * @return {@link DataDefinition}
     */
    public DataDefinition getPatientsAndTheirAges() {
        SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
        sqlPatientDataDefinition.setName("4 - Age");
        sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));

        Map<String, Integer> valuesMap = new HashMap<>();



        String query =
             " SELECT p.patient_id, CASE  WHEN ps.birthdate IS NULL THEN 'N/A'   "
                                 + " ELSE TIMESTAMPDIFF(YEAR,ps.birthdate,:endDate)   END AS age "  +
             " FROM patient p " +
             " INNER JOIN person ps ON p.patient_id = ps.person_id WHERE p.voided=0 AND ps.voided=0 ";


        StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

        sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

        return sqlPatientDataDefinition;
    }

    /**
     * <b>Technical Specs</b>
     *
     * <blockquote>
     *
     * <p></p>
     *
     * </blockquote>
     *
     * @return {@link DataDefinition}
     */
    public DataDefinition getPatientsReceivedTPTInTheLastFollowUpConsultation() {

        SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
        sqlPatientDataDefinition.setName("Received TPT in the last follow-up consultation");
        sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
        sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

        Map<String, Integer> valuesMap = new HashMap<>();
        valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
        valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());

        String query =
               " SELECT    p.patient_id, last_fu_consultation.followup_date as followup_date   "
                + "                FROM  patient p "
                + "                    JOIN encounter e ON e.patient_id = p.patient_id   "
                + "                    JOIN obs o ON o.encounter_id = e.encounter_id   "
                + "         INNER JOIN (SELECT p.patient_id, MAX(e.encounter_datetime) AS followup_date FROM patient p   "
                + "                 JOIN encounter e ON e.patient_id = p.patient_id   "
                + "                 WHERE e.encounter_type = ${6}  AND e.location_id = :location   "
                + "                 AND e.voided = 0 AND p.voided = 0   "
                + "                 AND e.encounter_datetime < curdate() "
                + "                 GROUP BY p.patient_id   "
                + "                ) last_fu_consultation ON last_fu_consultation.patient_id = p.patient_id "
                + "                WHERE "
                + "                e.encounter_datetime = last_fu_consultation.followup_date "
                + "                AND e.encounter_type = ${6}  AND e.voided = 0 "
                + "                AND o.concept_id =  ${1719}  AND o.voided = 0 "
                + "                AND o.value_coded IN ( ${23954} , ${23955} ) "
                + "                AND e.encounter_datetime < curdate()   "
                + "                UNION  "
                + "                SELECT  p.patient_id, last_fu_consultation.followup_date as followup_date   "
                + "                FROM patient p   "
                + "                    JOIN encounter e ON e.patient_id = p.patient_id   "
                + "                    JOIN obs o ON o.encounter_id = e.encounter_id   "
                + "         INNER JOIN (SELECT p.patient_id, MAX(e.encounter_datetime) AS followup_date FROM patient p   "
                + "                 JOIN encounter e ON e.patient_id = p.patient_id "
                + "                 WHERE e.encounter_type = ${6}  AND e.location_id = :location  "
                + "                 AND e.voided = 0 AND p.voided = 0 "
                + "                 AND e.encounter_datetime < curdate() "
                + "                 GROUP BY p.patient_id   "
                + "                ) last_fu_consultation ON last_fu_consultation.patient_id = p.patient_id   "
                + "                WHERE   "
                + "                e.encounter_datetime = last_fu_consultation.followup_date   "
                + "                AND e.encounter_type = ${6}  AND e.voided = 0  "
                + "                AND o.concept_id = ${6122}  AND o.voided = 0  "
                + "                AND o.value_coded IN ( ${1256} , ${1257} ) "
                + "                AND e.encounter_datetime < curdate()   "
                + "                UNION "
                + "                SELECT   p.patient_id, last_fu_consultation.followup_date as followup_date   "
                + "                FROM  patient p   "
                + "                    JOIN encounter e ON e.patient_id = p.patient_id   "
                + "                    JOIN obs o ON o.encounter_id = e.encounter_id   "
                + "         INNER JOIN  (SELECT p.patient_id, MAX(e.encounter_datetime) AS followup_date FROM patient p   "
                + "                 JOIN encounter e ON e.patient_id = p.patient_id   "
                + "                 WHERE e.encounter_type = ${6}  AND e.location_id = :location   "
                + "                 AND e.voided = 0 AND p.voided = 0   "
                + "                 AND e.encounter_datetime < curdate()   "
                + "                 GROUP BY p.patient_id ) last_fu_consultation ON last_fu_consultation.patient_id = p.patient_id   "
                + "                WHERE  e.encounter_datetime = last_fu_consultation.followup_date"
                + "                AND e.encounter_type = ${6}  AND e.voided = 0"
                + "                AND o.concept_id = ${6128}  AND o.voided = 0   "
                + "                AND e.encounter_datetime < curdate()   "
                + "                ) AS received_TPT ON received_TPT.patient_id = p.person_id   ";

        StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

        sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

        return sqlPatientDataDefinition;
    }

    /**
     * <b>Technical Specs</b>
     *
     * <blockquote>
     *
     * <p>The earliest date (encounter_datetime) on FILT (encounter type 60) with “Regime de TPT”
     * (concept id 23985) value coded “3HP” or ” 3HP+Piridoxina” (concept id in [23954, 23984])
     * and encounter datetime between start date and end date</p>
     *
     * </blockquote>
     *
     * @return {@link DataDefinition}
     */
    public DataDefinition getPatientAnd3HPInitiationDateOnFILT() {

        SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
        sqlPatientDataDefinition.setName("Last Follow up Consultation Date ");
        sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
        sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

        Map<String, Integer> valuesMap = new HashMap<>();
        valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());


        String query =
                " SELECT  p.patient_id, MAX(e.encounter_datetime) AS encounter_datetime   "
                + "                FROM   patient p   "
                + "         INNER JOIN  encounter e ON p.patient_id = e.patient_id "
                + "         INNER JOIN  obs o ON e.encounter_id = o.encounter_id "
                + "                WHERE   p.voided = 0 AND e.voided = 0 "
                + "         AND o.voided = 0   "
                + "         AND e.encounter_type = ${60}  "
                + "         AND e.location_id = :location "
                + "         AND o.concept_id = ${23985} "
                + "         AND o.value_coded IN ( ${23954}  ,  ${23984} ) "
                + "         AND e.encounter_datetime BETWEEN :startDate AND :endDate   "
                + "                GROUP BY p.patient_id   "
                + "                ) AS initiation_on_FILT ON initiation_on_FILT.patient_id = p.person_id   ";

        StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

        sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

        return sqlPatientDataDefinition;
    }

    /**
     * <b>Technical Specs</b>
     *
     * <blockquote>
     *
     * <p>The earliest date (encounter_datetime) on Ficha Clinica - Master Card (encounter type 6) with “Outras prescricoes”
     * (concept id 1719) with value coded equal to “3HP” (concept id 23954) and encounter datetime between start date and end date</p>
     *
     * </blockquote>
     *
     * @return {@link DataDefinition}
     */
    public DataDefinition getPatientAnd3HPInitiationDateOnFichaClinica() {

        SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
        sqlPatientDataDefinition.setName("Last Follow up Consultation Date ");
        sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
        sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

        Map<String, Integer> valuesMap = new HashMap<>();
        valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());


        String query =
               " SELECT  p.patient_id,  MAX(e.encounter_datetime) AS encounter_datetime   "
                + "                FROM   patient p   "
                + "         INNER JOIN  encounter e ON p.patient_id = e.patient_id   "
                + "         INNER JOIN   obs o ON e.encounter_id = o.encounter_id   "
                + "                WHERE   p.voided = 0 AND e.voided = 0  AND o.voided = 0   "
                + "         AND e.encounter_type = ${6} "
                + "         AND e.location_id = :location "
                + "         AND o.concept_id =  ${1719} "
                + "         AND o.value_coded =  ${23954} "
                + "         AND e.encounter_datetime BETWEEN :startDate AND :endDate   "
                + "                GROUP BY p.patient_id   "
                + "                )AS initiation_on_clinical_sheet    "
                + "                ON initiation_on_clinical_sheet.patient_id = p.person_id   "
                + "                LEFT JOIN   "
                + "                (  SELECT  p.patient_id,  MAX(e.encounter_datetime) AS encounter_datetime   "
                + "                FROM  patient p   "
                + "         INNER JOIN encounter e ON p.patient_id = e.patient_id   "
                + "         INNER JOIN obs o ON e.encounter_id = o.encounter_id "
                + "                WHERE   p.voided = 0 AND e.voided = 0   "
                + "         AND o.voided = 0 "
                + "         and e.encounter_type=  ${60} "
                + "         and o.concept_id=  ${23985}  "
                + "         and o.value_coded in ( ${23954} ,  ${23984} ) "
                + "         and e.encounter_datetime <= curdate() "
                + "         GROUP BY p.patient_id "
                + "                ) AS FILT_with_3HP "
                + "                ON FILT_with_3HP.patient_id = p.person_id   ";

        StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

        sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

        return sqlPatientDataDefinition;
    }

    /**
     * <b>Technical Specs</b>
     *
     * <blockquote>
     *
     * <p>Most recent FILT (encounter type 60) that has Regime de TPT (concept id 23985) with the values (3HP or 3HP + Piridoxina)
     * (concept id in [23954, 23984]) marked until the report generation dat</p>
     *
     * </blockquote>
     *
     * @return {@link DataDefinition}
     */
    public DataDefinition getPatientsAnd3HPDispensationDate() {

        SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
        sqlPatientDataDefinition.setName("FILT with 3HP Dispensation - Date ");
        sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
        sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

        Map<String, Integer> valuesMap = new HashMap<>();
        valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
        valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());

        String query =
                  "    SELECT  p.patient_id,  MAX(e.encounter_datetime) AS encounter_datetime   "
                + "                FROM  patient p   "
                + "         INNER JOIN encounter e ON p.patient_id = e.patient_id   "
                + "         INNER JOIN obs o ON e.encounter_id = o.encounter_id "
                + "                WHERE   p.voided = 0 AND e.voided = 0   "
                + "         AND o.voided = 0 "
                + "         and e.encounter_type=  ${60} "
                + "         and o.concept_id=  ${23985}  "
                + "         and o.value_coded in ( ${23954} ,  ${23984} ) "
                + "         and e.encounter_datetime <= curdate() "
                + "         GROUP BY p.patient_id "
                + "                ) AS FILT_with_3HP "
                + "                ON FILT_with_3HP.patient_id = p.person_id   ";

        StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

        sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

        return sqlPatientDataDefinition;
    }


    /**
     * <b>Technical Specs</b>
     *
     * <blockquote>
     *
     * <p>Most recent FILT (encounter type 60) that has Regime de TPT (concept id 23985) with the values (3HP or 3HP + Piridoxina)
     * (concept id in [23954, 23984]) marked until the report generation dat</p>
     *
     * </blockquote>
     *
     * @return {@link DataDefinition}
     */
    public DataDefinition getPatientsAndLast3HPTypeOfDispensation() {

        SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
        sqlPatientDataDefinition.setName("FILT with 3HP Dispensation - Date ");
        sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
        sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

        Map<String, Integer> valuesMap = new HashMap<>();
        valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
        valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());

        String query =
                  "        SELECT p.patient_id, o.value_coded AS dispensation_type FROM patient p   "
                + "                JOIN  encounter e ON e.patient_id = p.patient_id   "
                + "                JOIN obs o ON o.encounter_id = e.encounter_id   "
                + "                JOIN (SELECT p.patient_id, MAX(e.encounter_datetime) AS recent_filt "
                + "                FROM  patient p   "
                + "         INNER JOIN encounter e ON p.patient_id = e.patient_id "
                + "         INNER JOIN  obs o ON o.encounter_id = e.encounter_id WHERE  p.voided = 0 AND e.voided = 0  AND o.voided = 0 "
                + "         AND e.encounter_type = ${60} "
                + "         AND o.concept_id = ${23985} "
                + "         AND o.value_coded IN ( ${23954},${23984} ) "
                + "         AND e.location_id = :location "
                + "         AND e.encounter_datetime <= CURDATE() "
                + "         GROUP BY p.patient_id "
                + "         ) AS latest_filt ON latest_filt.patient_id = p.patient_id   "
                + "         WHERE latest_filt.recent_filt = e.encounter_datetime "
                + "         AND o.concept_id =  ${23986} "
                + "         AND o.value_coded IN ( ${1098} , ${23720} ) "
                + "                ) AS FILT_3HP_dispensation ON FILT_3HP_dispensation.patient_id = p.person_id  ";


        StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

        sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

        return sqlPatientDataDefinition;
    }

    /**
     * <b>Technical Specs</b>
     *
     * <blockquote>
     *
     * <p>Patient ART Start Date is the oldest date from the set of criterias defined in the common query:
     * 1/1 Patients who initiated ART and ART Start Date as earliest from the following criterias is by End of the period (reporting endDate)</p>
     *
     * </blockquote>
     *
     * @return {@link DataDefinition}
     */
    public String getARTStartDate(){

        Map<String, Integer> valuesMap = new HashMap<>();
        valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
        valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
        valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
        valuesMap.put("1256", hivMetadata.getStartDrugs().getConceptId());
        valuesMap.put("1255",   hivMetadata.getARVPlanConcept().getConceptId());
        valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
        valuesMap.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
        valuesMap.put("52",  hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
        valuesMap.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
        valuesMap.put("2",  hivMetadata.getARTProgram().getProgramId());


        String sql =
        " SELECT union_tbl.patient_id , MIN(union_tbl.first_pickup) first_pickup FROM   "
                + "       (SELECT p.patient_id, first_pickup FROM patient p   "
                + "       JOIN   "
                + "       patient_program pp ON pp.patient_id = p.patient_id   "
                + "       JOIN   "
                + "       (SELECT p.patient_id, MIN(e.encounter_datetime) first_pickup FROM patient  p   "
                + "       JOIN encounter e ON e.patient_id = p.patient_id   "
                + "       JOIN obs o ON o.encounter_id = e.encounter_id   "
                + "       WHERE e.encounter_type =  ${18}    "
                + "       AND e.voided = 0 AND p.voided = 0   "
                + "       AND o.voided = 0 AND e.location_id = :location   "
                + "       GROUP BY p.patient_id) first_pickup   "
                + "       ON first_pickup.patient_id = p.patient_id   "
                + "       JOIN   "
                + "       (SELECT p.patient_id FROM patient p   "
                + "       JOIN encounter e ON e.patient_id = p.patient_id   "
                + "       JOIN obs o ON o.encounter_id = e.encounter_id   "
                + "       WHERE e.encounter_type IN ( ${6} , ${9} , ${18} )   "
                + "       AND o.concept_id =  ${1255}  AND o.value_coded = ${1256}    "
                + "       AND e.voided = 0 AND p.voided = 0   "
                + "       AND o.voided = 0 AND e.location_id = :location    "
                + "       AND e.encounter_datetime <= :startDate) arv_plan    "
                + "       ON arv_plan.patient_id = p.patient_id   "
                + "       GROUP BY p.patient_id   "
                + "       UNION   "
                + "       SELECT p.patient_id, MIN(o.obs_datetime) AS first_pickup FROM patient p   "
                + "       JOIN encounter e ON e.patient_id = p.patient_id   "
                + "       JOIN obs o ON o.encounter_id = e.encounter_id   "
                + "       WHERE e.encounter_type IN ( ${6} , ${9} , ${18} , ${53} )   "
                + "       AND o.concept_id =  ${1190}     "
                + "       AND e.voided = 0 AND p.voided = 0   "
                + "       AND o.voided = 0 AND e.location_id = :location    "
                + "       AND o.obs_datetime <= :startDate   "
                + "       GROUP BY p.patient_id   "
                + "       UNION   "
                + "       SELECT p.patient_id, MIN(o.value_datetime) first_pickup FROM patient p    "
                + "           JOIN encounter e ON e.patient_id = p.patient_id   "
                + "           JOIN obs o ON o.encounter_id = e.encounter_id   "
                + "           JOIN obs oo ON oo.encounter_id = e.encounter_id   "
                + "           WHERE e.encounter_type =  ${52}  AND e.location_id = :location   "
                + "           AND e.voided = 0 AND p.voided = 0   "
                + "           AND o.concept_id =   ${23866}  AND o.voided =0   "
                + "           AND o.value_datetime <= :endDate   "
                + "           GROUP BY p.patient_id   "
                + "       UNION   "
                + "       SELECT p.patient_id, pp.date_enrolled AS first_pickup FROM patient p   "
                + "       JOIN   "
                + "       patient_program pp ON pp.patient_id = p.patient_id   "
                + "       WHERE pp.program_id =  ${2}    "
                + "       AND pp.date_enrolled <= :endDate   "
                + "       AND p.voided = 0   "
                + "       GROUP BY p.patient_id) union_tbl "
                + "       GROUP BY union_tbl.patient_id) initiated_art "
                + "       ON initiated_art.patient_id = p.person_id ";

        return sql;
    }

    public String getPrenantAndBreastfeeding(){

        String sql =
                " Select max_pregnant.patient_id, pregnancy_date FROM  "
                + "                (SELECT pregnant.patient_id, MAX(pregnant.pregnancy_date) AS pregnancy_date FROM  "
                + "                 ( SELECT p.patient_id , MAX(e.encounter_datetime) AS pregnancy_date  "
                + "                     FROM patient p "
                + "                     INNER JOIN person pe ON p.patient_id=pe.person_id     "
                + "                     INNER JOIN encounter e ON p.patient_id=e.patient_id  "
                + "                     INNER JOIN obs o ON e.encounter_id=o.encounter_id  "
                + "                     WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND concept_id=  ${1982} "
                + "                     AND value_coded=  ${1065}    "
                + "                     AND e.encounter_type in (${5},${6}) AND e.encounter_datetime between :startDate AND curdate() AND e.location_id=:location AND pe.gender='F' GROUP BY p.patient_id     "
                + "                     UNION  "
                + "                     SELECT p.patient_id, MAX(historical_date.value_datetime) as pregnancy_date FROM patient p "
                + "                     INNER JOIN person pe ON p.patient_id=pe.person_id "
                + "                     INNER JOIN encounter e ON p.patient_id=e.patient_id "
                + "                     INNER JOIN obs pregnancy ON e.encounter_id=pregnancy.encounter_id "
                + "                     INNER JOIN obs historical_date ON e.encounter_id = historical_date.encounter_id     "
                + "                     WHERE p.voided=0 AND e.voided=0 AND pregnancy.voided=0 AND pregnancy.concept_id=  ${1982}    "
                + "                     AND pregnancy.value_coded= ${1065} "
                + "                     AND historical_date.voided=0 AND historical_date.concept_id=  ${1190} "
                + "                     AND historical_date.value_datetime IS NOT NULL "
                + "                     AND e.encounter_type = ${53} "
                + "                     AND historical_date.value_datetime between :startDate AND curdate() AND e.location_id=:location AND pe.gender='F' GROUP BY p.patient_id     "
                + "                     UNION "
                + "                     SELECT p.patient_id,  MAX(e.encounter_datetime) as pregnancy_date "
                + "                     FROM patient p "
                + "                     INNER JOIN person pe ON p.patient_id=pe.person_id  "
                + "                     INNER JOIN encounter e ON p.patient_id=e.patient_id  "
                + "                     INNER JOIN obs o ON e.encounter_id=o.encounter_id "
                + "                     WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND concept_id= ${1279}    "
                + "                     and  e.encounter_type in (${5},${6}) AND e.encounter_datetime between :startDate AND curdate() AND e.location_id=:location  AND pe.gender='F' GROUP BY p.patient_id     "
                + "                     UNION "
                + "                     SELECT p.patient_id,  MAX(e.encounter_datetime) as pregnancy_date "
                + "                     FROM patient p "
                + "                     INNER JOIN person pe ON p.patient_id=pe.person_id  "
                + "                     INNER JOIN encounter e ON p.patient_id=e.patient_id  "
                + "                     INNER JOIN obs o ON e.encounter_id=o.encounter_id  "
                + "                     WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND concept_id=  ${1600}    "
                + "                     and  e.encounter_type in (${5},${6}) AND e.encounter_datetime between :startDate AND curdate() AND e.location_id=:location AND pe.gender='F' GROUP BY p.patient_id     "
                + "                     UNION  "
                + "                     SELECT p.patient_id, MAX(e.encounter_datetime) as pregnancy_date     "
                + "                     FROM patient p  "
                + "                     INNER JOIN person pe ON p.patient_id=pe.person_id     "
                + "                     INNER JOIN encounter e ON p.patient_id=e.patient_id     "
                + "                     INNER JOIN obs o ON e.encounter_id=o.encounter_id     "
                + "                     WHERE p.voided=0 AND pe.voided=0 AND e.voided=0 AND o.voided=0 AND concept_id= ${6334} "
                + "                     AND value_coded=  ${6331}    "
                + "                     AND e.encounter_type in (${5},${6}) AND e.encounter_datetime between :startDate AND curdate() AND e.location_id=:location AND pe.gender='F' GROUP BY p.patient_id     "
                + "                     UNION    "
                + "                     select pp.patient_id,  MAX(pp.date_enrolled) as pregnancy_date "
                + "                     FROM patient_program pp  "
                + "                     INNER JOIN person pe ON pp.patient_id=pe.person_id  "
                + "                     WHERE pp.program_id=  ${8} "
                + "                     AND pp.voided=0 AND pp.date_enrolled between  :startDate AND curdate() AND pp.location_id=:location AND pe.gender='F' GROUP BY pp.patient_id     "
                + "                     UNION "
                + "                     SELECT p.patient_id,  MAX(o.value_datetime) as pregnancy_date  FROM patient p     "
                + "                     INNER JOIN person pe ON p.patient_id=pe.person_id     "
                + "                     INNER JOIN encounter e ON p.patient_id=e.patient_id     "
                + "                     INNER JOIN obs o ON e.encounter_id=o.encounter_id     "
                + "       WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND concept_id= ${1465}    "
                + "                     AND e.encounter_type = ${6} "
                + "                     AND pe.gender='F' AND o.value_datetime BETWEEN :startDate AND curdate() GROUP BY p.patient_id) as pregnant "
                + "                     GROUP BY patient_id) max_pregnant "
                + "                    LEFT JOIN  (SELECT breastfeeding.patient_id, max(breastfeeding.last_date) as breastfeeding_date FROM ( "
                + "       SELECT p.patient_id, MAX(o.value_datetime) AS last_date     "
                + "       FROM patient p "
                + "       INNER JOIN person pe ON p.patient_id=pe.person_id  "
                + "       INNER JOIN encounter e ON p.patient_id=e.patient_id  "
                + "       INNER JOIN obs o ON e.encounter_id=o.encounter_id   "
                + "       WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND concept_id= ${5599}    "
                + "       AND  e.encounter_type in (${5},${6})  AND o.value_datetime BETWEEN :startDate AND curdate()      "
                + "       AND e.location_id=:location AND pe.gender='F'     "
                + "       GROUP BY p.patient_id     "
                + "       UNION "
                + "       SELECT p.patient_id, MAX(e.encounter_datetime) AS last_date     "
                + "       FROM patient p     "
                + "       INNER JOIN person pe ON p.patient_id=pe.person_id "
                + "       INNER JOIN encounter e ON p.patient_id=e.patient_id "
                + "       INNER JOIN obs o ON e.encounter_id=o.encounter_id "
                + "       WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND o.concept_id= ${6332}    "
                + "       AND o.value_coded= ${1065}    "
                + "       AND e.encounter_type in (${5},${6}) "
                + "        AND e.encounter_datetime BETWEEN :startDate AND curdate() "
                + "           AND e.location_id=:location AND pe.gender='F'     "
                + "       GROUP BY p.patient_id    "
                + "       UNION "
                + "       SELECT p.patient_id, MAX(e.encounter_datetime) AS last_date     "
                + "       FROM patient p     "
                + "       INNER JOIN person pe ON p.patient_id=pe.person_id     "
                + "       INNER JOIN encounter e ON p.patient_id=e.patient_id     "
                + "       INNER JOIN obs o ON e.encounter_id=o.encounter_id     "
                + "       WHERE p.voided=0 AND pe.voided=0 AND e.voided=0 AND o.voided=0 AND o.concept_id= ${6334}    "
                + "       AND o.value_coded=  ${6332}    "
                + "       AND e.encounter_type in (${5},${6})  AND e.encounter_datetime BETWEEN :startDate AND curdate()      "
                + "       AND e.location_id=:location AND pe.gender='F' GROUP BY p.patient_id     "
                + "       UNION "
                + "       SELECT pp.patient_id, MAX(ps.start_date) AS last_date     "
                + "       FROM patient_program pp     "
                + "       INNER JOIN person pe ON pp.patient_id=pe.person_id     "
                + "       INNER JOIN patient_state ps ON pp.patient_program_id=ps.patient_program_id     "
                + "       WHERE pp.program_id= ${8}    "
                + "       AND ps.state=  ${27}    "
                + "       AND pp.voided=0 AND  ps.start_date BETWEEN :startDate AND curdate()      "
                + "       AND pp.location_id=:location AND pe.gender='F'     "
                + "       GROUP BY pp.patient_id     "
                + "       UNION "
                + "       SELECT p.patient_id, MAX(hist.value_datetime) AS last_date     "
                + "       FROM patient p     "
                + "       INNER JOIN person pe ON p.patient_id=pe.person_id     "
                + "       INNER JOIN encounter e ON p.patient_id=e.patient_id     "
                + "       INNER JOIN obs o ON e.encounter_id=o.encounter_id     "
                + "       INNER JOIN obs hist ON e.encounter_id=hist.encounter_id     "
                + "        WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND o.concept_id=  ${6332}    "
                + "       AND o.value_coded=  ${1065}    "
                + "       AND e.encounter_type = ${53}    "
                + "       AND hist.concept_id=   ${1190}    "
                + "       AND pe.gender='F' AND hist.value_datetime IS NOT NULL      "
                + "        AND hist.value_datetime BETWEEN :startDate AND curdate()  GROUP BY p.patient_id     "
                + "       ) AS breastfeeding     "
                + "       GROUP BY patient_id) max_breastfeeding     "
                + "                     ON max_pregnant.patient_id = max_breastfeeding.patient_id     "
                + "                     WHERE (max_pregnant.pregnancy_date Is NOT NULL AND max_pregnant.pregnancy_date >= max_breastfeeding.breastfeeding_date)     "
                + "                     OR (max_breastfeeding.breastfeeding_date Is NULL)    "
                + "                 )pregnancy ON pregnancy.patient_id = p.person_id   "
                + "                 LEFT JOIN   "
                + "                 ( Select max_breastfeeding.patient_id, breastfeeding_date FROM     "
                + "                  (SELECT breastfeeding.patient_id, max(breastfeeding.last_date) as breastfeeding_date FROM (     "
                + "       SELECT p.patient_id, MAX(o.value_datetime) AS last_date     "
                + "       FROM patient p     "
                + "       INNER JOIN person pe ON p.patient_id=pe.person_id     "
                + "       INNER JOIN encounter e ON p.patient_id=e.patient_id     "
                + "       INNER JOIN obs o ON e.encounter_id=o.encounter_id     "
                + "       WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND concept_id=  ${5599}    "
                + "       AND  e.encounter_type in (${5},${6})  AND o.value_datetime BETWEEN :startDate AND curdate()      "
                + "       AND e.location_id=:location AND pe.gender='F'     "
                + "       GROUP BY p.patient_id "
                + "       UNION  SELECT p.patient_id, MAX(e.encounter_datetime) AS last_date     "
                + "       FROM patient p "
                + "       INNER JOIN person pe ON p.patient_id=pe.person_id     "
                + "       INNER JOIN encounter e ON p.patient_id=e.patient_id     "
                + "       INNER JOIN obs o ON e.encounter_id=o.encounter_id     "
                + "       WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND o.concept_id= ${6332}  "
                + "       AND o.value_coded= ${1065} "
                + "       AND e.encounter_type in (${5},${6}) "
                + "        AND e.encounter_datetime BETWEEN :startDate AND curdate() "
                + "           AND e.location_id=:location AND pe.gender='F' "
                + "       GROUP BY p.patient_id "
                + "       UNION "
                + "       SELECT p.patient_id, MAX(e.encounter_datetime) AS last_date     "
                + "       FROM patient p "
                + "       INNER JOIN person pe ON p.patient_id=pe.person_id "
                + "       INNER JOIN encounter e ON p.patient_id=e.patient_id "
                + "       INNER JOIN obs o ON e.encounter_id=o.encounter_id "
                + "       WHERE p.voided=0 AND pe.voided=0 AND e.voided=0 AND o.voided=0 AND o.concept_id= ${6334} "
                + "       AND o.value_coded=  ${6332} "
                + "       AND e.encounter_type in (${5},${6}) "
                + "               AND e.encounter_datetime BETWEEN :startDate AND curdate() "
                + "            AND e.location_id=:location AND pe.gender='F' GROUP BY p.patient_id "
                + "       UNION "
                + "       SELECT pp.patient_id, MAX(ps.start_date) AS last_date "
                + "       FROM patient_program pp "
                + "       INNER JOIN person pe ON pp.patient_id=pe.person_id "
                + "       INNER JOIN patient_state ps ON pp.patient_program_id=ps.patient_program_id     "
                + "       WHERE pp.program_id=  ${8} "
                + "       AND ps.state=  ${27} "
                + "       AND pp.voided=0 AND "
                + "         ps.start_date BETWEEN :startDate AND curdate() "
                + "           AND pp.location_id=:location AND pe.gender='F' "
                + "       GROUP BY pp.patient_id"
                + "                     UNION "
                + "       SELECT p.patient_id, MAX(hist.value_datetime) AS last_date "
                + "       FROM patient p "
                + "       INNER JOIN person pe ON p.patient_id=pe.person_id "
                + "       INNER JOIN encounter e ON p.patient_id=e.patient_id "
                + "       INNER JOIN obs o ON e.encounter_id=o.encounter_id "
                + "       INNER JOIN obs hist ON e.encounter_id=hist.encounter_id "
                + "        WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND o.concept_id=  ${6332} "
                + "       AND o.value_coded=  ${1065} "
                + "       AND e.encounter_type = ${53} "
                + "       AND hist.concept_id=   ${1190} "
                + "       AND pe.gender='F' AND hist.value_datetime IS NOT NULL "
                + "        AND hist.value_datetime BETWEEN :startDate AND curdate() GROUP BY p.patient_id "
                + "       ) AS breastfeeding "
                + "       GROUP BY patient_id) max_breastfeeding "
                + "                    LEFT JOIN "
                + "                     (SELECT pregnant.patient_id, MAX(pregnant.pregnancy_date) AS pregnancy_date FROM "
                + "                 (SELECT p.patient_id , MAX(e.encounter_datetime) AS pregnancy_date "
                + "                     FROM patient p "
                + "                     INNER JOIN person pe ON p.patient_id=pe.person_id  "
                + "                     INNER JOIN encounter e ON p.patient_id=e.patient_id "
                + "                     INNER JOIN obs o ON e.encounter_id=o.encounter_id "
                + "                     WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND concept_id=  ${1982}    "
                + "                     AND value_coded=  ${1065}    "
                + "                     AND e.encounter_type in (${5},${6}) AND e.encounter_datetime between :startDate AND curdate() AND e.location_id=:location AND pe.gender='F' GROUP BY p.patient_id     "
                + "                     UNION     "
                + "                     SELECT p.patient_id, MAX(historical_date.value_datetime) as pregnancy_date FROM patient p     "
                + "                     INNER JOIN person pe ON p.patient_id=pe.person_id     "
                + "                     INNER JOIN encounter e ON p.patient_id=e.patient_id     "
                + "                     INNER JOIN obs pregnancy ON e.encounter_id=pregnancy.encounter_id     "
                + "                     INNER JOIN obs historical_date ON e.encounter_id = historical_date.encounter_id     "
                + "                     WHERE p.voided=0 AND e.voided=0 AND pregnancy.voided=0 AND pregnancy.concept_id=  ${1982}    "
                + "                     AND pregnancy.value_coded=   ${1065}    "
                + "                     AND historical_date.voided=0 AND historical_date.concept_id=  ${1190}    "
                + "                     AND historical_date.value_datetime IS NOT NULL     "
                + "                     AND e.encounter_type = ${53}    "
                + "                 AND historical_date.value_datetime between :startDate AND curdate() AND e.location_id=:location AND pe.gender='F' GROUP BY p.patient_id     "
                + "                     UNION     "
                + "                     SELECT p.patient_id,  MAX(e.encounter_datetime) as pregnancy_date     "
                + "                     FROM patient p     "
                + "                     INNER JOIN person pe ON p.patient_id=pe.person_id     "
                + "                     INNER JOIN encounter e ON p.patient_id=e.patient_id     "
                + "                     INNER JOIN obs o ON e.encounter_id=o.encounter_id     "
                + "                     WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND concept_id= ${1279}    "
                + "                     and     "
                + "                     e.encounter_type in (${5},${6}) AND e.encounter_datetime between :startDate AND curdate() AND e.location_id=:location  AND pe.gender='F' GROUP BY p.patient_id     "
                + "                     UNION "
                + "                     SELECT p.patient_id,  MAX(e.encounter_datetime) as pregnancy_date     "
                + "                     FROM patient p     "
                + "                     INNER JOIN person pe ON p.patient_id=pe.person_id     "
                + "                     INNER JOIN encounter e ON p.patient_id=e.patient_id     "
                + "                     INNER JOIN obs o ON e.encounter_id=o.encounter_id     "
                + "                     WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND concept_id=  ${1600}    "
                + "                     and  e.encounter_type in (${5},${6}) AND e.encounter_datetime between :startDate AND curdate() AND e.location_id=:location AND pe.gender='F' GROUP BY p.patient_id    "
                + "                     UNION "
                + "                     SELECT p.patient_id, MAX(e.encounter_datetime) as pregnancy_date     "
                + "                     FROM patient p     "
                + "                     INNER JOIN person pe ON p.patient_id=pe.person_id     "
                + "                     INNER JOIN encounter e ON p.patient_id=e.patient_id     "
                + "                     INNER JOIN obs o ON e.encounter_id=o.encounter_id     "
                + "                     WHERE p.voided=0 AND pe.voided=0 AND e.voided=0 AND o.voided=0 AND concept_id= ${6334}    "
                + "                     AND value_coded=  ${6331}    "
                + "                     AND e.encounter_type in (${5},${6}) AND e.encounter_datetime between :startDate AND curdate() AND e.location_id=:location AND pe.gender='F' GROUP BY p.patient_id    "
                + " UNION "
                + "                     select pp.patient_id,  MAX(pp.date_enrolled) as pregnancy_date     "
                + "                     FROM patient_program pp     "
                + "                     INNER JOIN person pe ON pp.patient_id=pe.person_id     "
                + "                     WHERE pp.program_id=  ${8}    "
                + "                     AND pp.voided=0 AND pp.date_enrolled between  :startDate AND curdate() AND pp.location_id=:location AND pe.gender='F' GROUP BY pp.patient_id     "
                + "                     UNION   "
                + "                     SELECT p.patient_id,  MAX(o.value_datetime) as pregnancy_date  FROM patient p     "
                + "                     INNER JOIN person pe ON p.patient_id=pe.person_id     "
                + "                     INNER JOIN encounter e ON p.patient_id=e.patient_id     "
                + "                     INNER JOIN obs o ON e.encounter_id=o.encounter_id     "
                + "       WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND concept_id=  ${1465}    "
                + "                     AND e.encounter_type = ${6}    "
                + "                     AND pe.gender='F' AND o.value_datetime BETWEEN :startDate AND curdate() GROUP BY p.patient_id) as pregnant     "
                + "                     GROUP BY patient_id) max_pregnant   "
                + "                     ON max_pregnant.patient_id = max_breastfeeding.patient_id     "
                + "                     WHERE (max_breastfeeding.breastfeeding_date Is NOT NULL AND max_breastfeeding.breastfeeding_date > max_pregnant.pregnancy_date)     "
                + "                     OR (max_pregnant.pregnancy_date Is NULL) ) breastfeeding    "
                + "                     ON breastfeeding.patient_id = p.person_id    ";

        return sql;
    }

    /**
     * <b>Technical Specs</b>
     *
     * <blockquote>
     *
     * <p>Date (encounter_datetime) of the most recent clinical consultation registered on Ficha Clínica – MasterCard or Ficha de Seguimento
     * (encounter type 6) until report generation date</p>
     *
     * </blockquote>
     *
     * @return {@link DataDefinition}
     */
    public DataDefinition getPatientsAndLastFollowUpConsultationDate() {

        SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
        sqlPatientDataDefinition.setName("Last Follow up Consultation Date ");
        sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

        Map<String, Integer> valuesMap = new HashMap<>();
        valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());

        String query =
                "       SELECT p.patient_id, MAX(e.encounter_datetime) AS followup_date FROM patient p    "
                        + "    JOIN encounter e ON e.patient_id = p.patient_id   "
                        + "    WHERE e.encounter_type = ${6}  AND e.location_id = :location   "
                        + "    AND e.voided = 0 AND p.voided = 0   "
                        + "    AND e.encounter_datetime < curdate()   "
                        + "    GROUP BY p.patient_id) last_clinical   "
                        + "    ON last_clinical.patient_id = p.person_id   ";

        StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

        sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

        return sqlPatientDataDefinition;
    }
    /**
     * <b>Technical Specs</b>
     *
     * <blockquote>
     *
     * <p>Set value equal to “Sim” if the patient received TPT in the last follow-up consultation Date (field 7 date) as following otherwise set “Não”:</p>
     * <p>if patient have Ficha Clinica (encounter type 6) with “Outras Prescricoes” (concept id 1719) value coded “3HP”
     * (concept id 23954) or “DT-INH” (concept id 23955) in the last follow up consultation date before the report generation date (same as field 7)</p>
     * <p>If patient have Ficha Clinica (encounter_type 6) with “Profilaxia (INH)” (concept id 6122) value coded “Inicio” (concept id 1256)
     * or “Continua” (concept id 1257) in the last follow up consultation date before the report generation date (same as field 7)</p>
     * <p>Select all patients with Ficha Clinica (encounter type 6) with “Profilaxia com INH - TPI (Data Inicio)” (Concept 6128) in
     * the last follow up consultation date before the reporting end dat</p>
     *
     * </blockquote>
     *
     * @return {@link DataDefinition}
     */
    public DataDefinition getPatientsWhoReceivedTPTInLastFollowUpConsultation() {

        SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
        sqlPatientDataDefinition.setName("Received TPT in the last follow-up consultation ");
        sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

        Map<String, Integer> valuesMap = new HashMap<>();
        valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());

        String query =
                "           SELECT    p.patient_id, last_fu_consultation.followup_date as followup_date   "
                + "                FROM  patient p "
                + "                    JOIN encounter e ON e.patient_id = p.patient_id   "
                + "                    JOIN obs o ON o.encounter_id = e.encounter_id   "
                + "         INNER JOIN (SELECT p.patient_id, MAX(e.encounter_datetime) AS followup_date FROM patient p   "
                + "                 JOIN encounter e ON e.patient_id = p.patient_id   "
                + "                 WHERE e.encounter_type = ${6}  AND e.location_id = :location   "
                + "                 AND e.voided = 0 AND p.voided = 0   "
                + "                 AND e.encounter_datetime < curdate() "
                + "                 GROUP BY p.patient_id   "
                + "                ) last_fu_consultation ON last_fu_consultation.patient_id = p.patient_id "
                + "                WHERE "
                + "                e.encounter_datetime = last_fu_consultation.followup_date "
                + "                AND e.encounter_type = ${6}  AND e.voided = 0 "
                + "                AND o.concept_id =  ${1719}  AND o.voided = 0 "
                + "                AND o.value_coded IN ( ${23954} , ${23955} ) "
                + "                AND e.encounter_datetime < curdate()   "
                + "                UNION  "
                + "                SELECT  p.patient_id, last_fu_consultation.followup_date as followup_date   "
                + "                FROM patient p   "
                + "                    JOIN encounter e ON e.patient_id = p.patient_id   "
                + "                    JOIN obs o ON o.encounter_id = e.encounter_id   "
                + "         INNER JOIN (SELECT p.patient_id, MAX(e.encounter_datetime) AS followup_date FROM patient p   "
                + "                 JOIN encounter e ON e.patient_id = p.patient_id "
                + "                 WHERE e.encounter_type = ${6}  AND e.location_id = :location  "
                + "                 AND e.voided = 0 AND p.voided = 0 "
                + "                 AND e.encounter_datetime < curdate() "
                + "                 GROUP BY p.patient_id   "
                + "                ) last_fu_consultation ON last_fu_consultation.patient_id = p.patient_id   "
                + "                WHERE   "
                + "                e.encounter_datetime = last_fu_consultation.followup_date   "
                + "                AND e.encounter_type = ${6}  AND e.voided = 0  "
                + "                AND o.concept_id = ${6122}  AND o.voided = 0  "
                + "                AND o.value_coded IN ( ${1256} , ${1257} ) "
                + "                AND e.encounter_datetime < curdate()   "
                + "                UNION "
                + "                SELECT   p.patient_id, last_fu_consultation.followup_date as followup_date   "
                + "                FROM  patient p   "
                + "                    JOIN encounter e ON e.patient_id = p.patient_id   "
                + "                    JOIN obs o ON o.encounter_id = e.encounter_id   "
                + "         INNER JOIN  (SELECT p.patient_id, MAX(e.encounter_datetime) AS followup_date FROM patient p   "
                + "                 JOIN encounter e ON e.patient_id = p.patient_id   "
                + "                 WHERE e.encounter_type = ${6}  AND e.location_id = :location   "
                + "                 AND e.voided = 0 AND p.voided = 0   "
                + "                 AND e.encounter_datetime < curdate()   "
                + "                 GROUP BY p.patient_id ) last_fu_consultation ON last_fu_consultation.patient_id = p.patient_id   "
                + "                WHERE  e.encounter_datetime = last_fu_consultation.followup_date"
                + "                AND e.encounter_type = ${6}  AND e.voided = 0"
                + "                AND o.concept_id = ${6128}  AND o.voided = 0   "
                + "                AND e.encounter_datetime < curdate()   "
                + "                ) AS received_TPT ON received_TPT.patient_id = p.person_id   ";

        StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

        sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

        return sqlPatientDataDefinition;
    }

    /**
     * <b>Technical Specs</b>
     *
     * <blockquote>
     *
     * <p>The earliest date (encounter_datetime) on FILT (encounter type 60) with “Regime de TPT” (concept id 23985) value coded “3HP”
     * or ” 3HP+Piridoxina” (concept id in [23954, 23984]) and encounter datetime between start date and end date</>
     *
     * </blockquote>
     *
     * @return {@link DataDefinition}
     */
    public DataDefinition getPatientsAndEarliestDateOf3HPInitiationOnFILT() {

        SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
        sqlPatientDataDefinition.setName("10 3HP Initiation Dates - On FILT ");
        sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

        Map<String, Integer> valuesMap = new HashMap<>();
        valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());

        String query =
                " SELECT  p.patient_id, MAX(e.encounter_datetime) AS encounter_datetime   "
                + "                FROM   patient p   "
                + "         INNER JOIN  encounter e ON p.patient_id = e.patient_id "
                + "         INNER JOIN  obs o ON e.encounter_id = o.encounter_id "
                + "                WHERE   p.voided = 0 AND e.voided = 0 "
                + "         AND o.voided = 0   "
                + "         AND e.encounter_type = ${60}  "
                + "         AND e.location_id = :location "
                + "         AND o.concept_id = ${23985} "
                + "         AND o.value_coded IN ( ${23954}  ,  ${23984} ) "
                + "         AND e.encounter_datetime BETWEEN :startDate AND :endDate   "
                + "                GROUP BY p.patient_id   ";

        StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

        sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

        return sqlPatientDataDefinition;
    }

    /**
     * <b>Technical Specs</b>
     *
     * <blockquote>
     *
     * <p>The earliest date (encounter_datetime) on Ficha Clinica - Master Card (encounter type 6) with “Outras prescricoes”
     * (concept id 1719) with value coded equal to “3HP” (concept id 23954) and encounter datetime between start date and end date</p>
     *
     * </blockquote>
     *
     * @return {@link DataDefinition}
     */
    public DataDefinition getPatientsAnd3HPInitiationDateOnFichaClinica() {

        SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
        sqlPatientDataDefinition.setName("11 - 3HP Initiation Dates  - On Ficha Clínica  ");
        sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

        Map<String, Integer> valuesMap = new HashMap<>();
        valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());

        String query =
                "           SELECT  p.patient_id,  MAX(e.encounter_datetime) AS encounter_datetime   "
                + "                FROM   patient p   "
                + "         INNER JOIN  encounter e ON p.patient_id = e.patient_id   "
                + "         INNER JOIN   obs o ON e.encounter_id = o.encounter_id   "
                + "                WHERE   p.voided = 0 AND e.voided = 0  AND o.voided = 0   "
                + "         AND e.encounter_type = ${6} "
                + "         AND e.location_id = :location "
                + "         AND o.concept_id =  ${1719} "
                + "         AND o.value_coded =  ${23954} "
                + "         AND e.encounter_datetime BETWEEN :startDate AND :endDate   "
                + "                GROUP BY p.patient_id   "
                + "                )AS initiation_on_clinical_sheet    ";

        StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

        sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

        return sqlPatientDataDefinition;
    }

    /**
     * <b>Technical Specs</b>
     *
     * <blockquote>
     *
     * <p>Most recent FILT (encounter type 60) that has Regime de TPT (concept id 23985) with the values (3HP or 3HP + Piridoxina)
     * (concept id in [23954, 23984]) marked until the report generation date</p>
     *
     * </blockquote>
     *
     * @return {@link DataDefinition}
     */
    public DataDefinition getPatientsWithMostRecentFILT3HPDispensationDate() {

        SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
        sqlPatientDataDefinition.setName("12 - FILT with 3HP Dispensation - Date  ");
        sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

        Map<String, Integer> valuesMap = new HashMap<>();
        valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());

        String query =
                "           SELECT  p.patient_id,  MAX(e.encounter_datetime) AS encounter_datetime   "
                + "                FROM  patient p   "
                + "         INNER JOIN encounter e ON p.patient_id = e.patient_id   "
                + "         INNER JOIN obs o ON e.encounter_id = o.encounter_id "
                + "                WHERE   p.voided = 0 AND e.voided = 0   "
                + "         AND o.voided = 0 "
                + "         and e.encounter_type=  ${60} "
                + "         and o.concept_id=  ${23985}  "
                + "         and o.value_coded in ( ${23954} ,  ${23984} ) "
                + "         and e.encounter_datetime <= curdate() "
                + "         GROUP BY p.patient_id "
                + "                ) AS FILT_with_3HP ";

        StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

        sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

        return sqlPatientDataDefinition;
    }

    /**
     * <b>Technical Specs</b>
     *
     * <blockquote>
     *
     * <p>Tipo de Dispensa (concept id 23986)  with value coded  Mensal or Trimestral (concept id IN [1098, 23720])
     * on the most recent FILT (encounter type 60) that has Regime de TPT  (concept id 23985)  with the values (3HP or 3HP + Piridoxina)
     * (concept id in [23954, 23984]) marked until the report generation date</p>
     *
     * </blockquote>
     *
     * @return {@link DataDefinition}
     */
    public DataDefinition getPatientsAndLastFILTWith3HPDispensationTypeOfDispensation() {

        SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
        sqlPatientDataDefinition.setName("13 - Last FILT with 3HP Dispensation - Type of Dispensation ");
        sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

        Map<String, Integer> valuesMap = new HashMap<>();
        valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());

        String query =
                "   SELECT p.patient_id, o.value_coded AS dispensation_type FROM patient p   "
                        + "                JOIN  encounter e ON e.patient_id = p.patient_id   "
                        + "                JOIN obs o ON o.encounter_id = e.encounter_id   "
                        + "                JOIN (SELECT p.patient_id, MAX(e.encounter_datetime) AS recent_filt "
                        + "                FROM  patient p   "
                        + "         INNER JOIN encounter e ON p.patient_id = e.patient_id "
                        + "         INNER JOIN  obs o ON o.encounter_id = e.encounter_id WHERE  p.voided = 0 AND e.voided = 0  AND o.voided = 0 "
                        + "         AND e.encounter_type = ${60} "
                        + "         AND o.concept_id = ${23985} "
                        + "         AND o.value_coded IN ( ${23954},${23984} ) "
                        + "         AND e.location_id = :location "
                        + "         AND e.encounter_datetime <= CURDATE() "
                        + "         GROUP BY p.patient_id "
                        + "         ) AS latest_filt ON latest_filt.patient_id = p.patient_id   "
                        + "         WHERE latest_filt.recent_filt = e.encounter_datetime "
                        + "         AND o.concept_id =  ${23986} "
                        + "         AND o.value_coded IN ( ${1098} , ${23720} ) "
                        + "                ) AS FILT_3HP_dispensation ON FILT_3HP_dispensation.patient_id = p.person_id  ";

        StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

        sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

        return sqlPatientDataDefinition;
    }

    /**
     * <b>Technical Specs</b>
     *
     * <blockquote>
     *
     * <p>The earliest IPT drug pick-up date registered on FILT (encounter type 60) during the reporting period
     * with “Regime de TPT” (concept id 23985) value coded ‘Isoniazid’ or ‘Isoniazid + piridoxina’
     * (concept id in [656, 23982]) and encounter datetime between start date and end date</p>
     *
     * </blockquote>
     *
     * @return {@link DataDefinition}
     */
    public DataDefinition getPatientsAndIPTInitiationDateOnFilt() {

        SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
        sqlPatientDataDefinition.setName("14 - IPT Initiation Date - On FILT ");
        sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

        Map<String, Integer> valuesMap = new HashMap<>();
        valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());

        String query =
                "  SELECT  p.patient_id, MIN(e.encounter_datetime) AS pickup_date "
                        + "                FROM  patient p "
                        + "         INNER JOIN  encounter e ON p.patient_id = e.patient_id "
                        + "         INNER JOIN   obs o ON e.encounter_id = o.encounter_id "
                        + "                WHERE   p.voided = 0 AND e.voided = 0 AND o.voided = 0   "
                        + "         AND e.encounter_type = ${60} "
                        + "         AND o.concept_id = ${23985}   AND e.location_id = :location   "
                        + "         AND o.value_coded IN (${656},${23982}) "
                        + "         AND e.encounter_datetime BETWEEN :startDate AND  :endDate "
                        + "                GROUP BY p.patient_id "
                        + "                )AS IPT_initiation_on_FILT ON IPT_initiation_on_FILT.patient_id = p.person_id ";

        StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

        sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

        return sqlPatientDataDefinition;
    }

    /**
     * <b>Technical Specs</b>
     *
     * <blockquote>
     *
     * <p>The earliest IPT initiation date registered on Ficha Clínica or Ficha de Seguimento
     * (encounter type 6, 9) during the reporting period with following concepts:</p>
     * <p>with “Profilaxia INH” (concept id 6122) with value code “Inicio” (concept id 1256) and encounter datetime between start date and end date</p>
     * <p>with “Profilaxia com  INH” (concept id 6128) and value datetime is not null and between start date and end date</p>
     *
     * </blockquote>
     *
     * @return {@link DataDefinition}
     */
    public DataDefinition getPatientsAndIPTInitiationDateOnFichaClinicaOrFichaSeguimento() {

        SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
        sqlPatientDataDefinition.setName("15 - IPT Initiation Date - on Ficha Clínica or Ficha de Seguimento ");
        sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

        Map<String, Integer> valuesMap = new HashMap<>();
        valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());

        String query =
                          "     SELECT p.patient_id, "
                        + "         CASE  WHEN o.concept_id = ${6122}  THEN MIN(e.encounter_datetime) "
                        + "             WHEN o.concept_id = ${6128}  THEN MIN(o.value_datetime) "
                        + "         END AS initiation_date "
                        + "       FROM patient p  "
                        + "             INNER JOIN  encounter e ON p.patient_id = e.patient_id "
                        + "             INNER JOIN  obs o ON e.encounter_id = o.encounter_id "
                        + "      WHERE p.voided = 0 AND e.voided = 0  "
                        + "             AND o.voided = 0 AND e.location_id = :location "
                        + "             AND e.encounter_type IN (${6} , ${9}) "
                        + "             AND ((o.concept_id = ${6122} "
                        + "             AND o.value_coded = ${1256} "
                        + "             AND e.encounter_datetime BETWEEN :startDate AND  :endDate)  "
                        + "             OR (o.concept_id = ${6128} "
                        + "             AND o.value_datetime IS NOT NULL "
                        + "             AND o.value_datetime BETWEEN :startDate AND  :endDate))  "
                        + "      GROUP BY p.patient_id ) AS IPT_clinical_seg_initiation    "
                        + "                 ON IPT_clinical_seg_initiation.patient_id = p.person_id   ";

        StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

        sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

        return sqlPatientDataDefinition;
    }

    /**
     * <b>Technical Specs</b>
     *
     * <blockquote>
     *
     * <p>The earliest “Ultima profilaxia Isoniazida (Data Inicio)” (concept id 6128) registered on Ficha Resumo
     * (encounter type 53) and value datetime not null and between start date and end date</p>
     *
     * </blockquote>
     *
     * @return {@link DataDefinition}
     */
    public DataDefinition getPatientsAndIPTInitiationDateOnFichaResumo() {

        SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
        sqlPatientDataDefinition.setName("16 - IPT Initiation Date -on Ficha Resumo ");
        sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

        Map<String, Integer> valuesMap = new HashMap<>();
        valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());

        String query =
                "        SELECT p.patient_id, MIN(o.value_datetime) AS encounter_datetime   "
                + "      FROM patient p  "
                + "      INNER JOIN encounter e ON e.patient_id = p.patient_id   "
                + "      INNER JOIN obs o ON e.encounter_id = o.encounter_id   "
                + "      WHERE e.encounter_type = ${53}  AND p.voided = 0  "
                + "      AND e.voided = 0  "
                + "      AND o.voided = 0  "
                + "      AND o.value_datetime IS NOT NULL "
                + "      AND o.concept_id = ${6128} "
                + "      AND e.location_id = :location "
                + "      AND o.value_datetime BETWEEN :startDate AND  :endDate  "
                + "                GROUP BY p.patient_id )IPT_mastercard_initiation    "
                + "                 ON IPT_mastercard_initiation.patient_id = p.person_id ";

        StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

        sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

        return sqlPatientDataDefinition;
    }

    /**
     * <b>Technical Specs</b>
     *
     * <blockquote>
     *
     * <p>The most recent FILT (encounter type 60) that has Regime de TPT (concept id 23985) with the values
     * (Isoniazida or Isoniazida + Piridoxina)  (concept id in [656, 23982]) marked until the report generation date</p>
     *
     * </blockquote>
     *
     * @return {@link DataDefinition}
     */
    public DataDefinition getPatientsAndDateOfLastFILTDispensationWithIPT() {

        SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
        sqlPatientDataDefinition.setName("17 - Last FILT Dispensation with IPT - Date ");
        sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

        Map<String, Integer> valuesMap = new HashMap<>();
        valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());

        String query =
               "                SELECT p.patient_id, MAX(e.encounter_datetime) AS encounter_datetime   "
                + "                    FROM patient p   "
                + "                    INNER JOIN encounter e ON e.patient_id = p.patient_id "
                + "                    INNER JOIN obs o ON e.encounter_id = o.encounter_id  "
                + "                    WHERE e.encounter_type = ${60}  AND p.voided = 0  "
                + "             AND e.voided = 0 "
                + "             AND o.voided = 0 "
                + "             AND o.concept_id = ${23985} "
                + "             AND o.value_coded IN (${656},${23982}) "
                + "             AND e.location_id = :location "
                + "             AND e.encounter_datetime <= CURDATE() GROUP BY p.patient_id   "
                + "                 ) FILT_with_IPT ON FILT_with_IPT.patient_id = p.person_id   ";

        StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

        sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

        return sqlPatientDataDefinition;
    }

    /**
     * <b>Technical Specs</b>
     *
     * <blockquote>
     *
     * <p>Value of Tipo de Dispensa (Mensal or Trimestral) on the most recent FILT (encounter type 60) that has Regime de TPT
     * (concept id 23985)  with the values(Isoniazida or Isoniazida + Piridoxina)  (concept id in [656, 23982]) marked until the report generation date</p>
     *
     * </blockquote>
     *
     * @return {@link DataDefinition}
     */
    public DataDefinition getPatientsAndTypeOfDispensationInLastFILTDispensationWithIPT() {

        SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
        sqlPatientDataDefinition.setName("18 - Last FILT Dispensation with IPT Type of Dispensation ");
        sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

        Map<String, Integer> valuesMap = new HashMap<>();
        valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());

        String query =
                " SELECT   p.patient_id, o.value_coded AS dispensation_type FROM patient p "
                + "                JOIN encounter e ON e.patient_id = p.patient_id "
                + "                JOIN obs o ON o.encounter_id = e.encounter_id "
                + "                JOIN (SELECT p.patient_id, MAX(e.encounter_datetime) AS encounter_datetime "
                + "                    FROM patient p   "
                + "                    INNER JOIN encounter e ON e.patient_id = p.patient_id "
                + "                    INNER JOIN obs o ON e.encounter_id = o.encounter_id "
                + "                    WHERE e.encounter_type = ${60} AND p.voided = 0  "
                + "             AND e.voided = 0 "
                + "             AND o.voided = 0  "
                + "             AND o.concept_id = ${23985} "
                + "             AND o.value_coded IN (${656},${23982}) "
                + "             AND e.location_id = :location  "
                + "             AND e.encounter_datetime <= CURDATE() "
                + "                    GROUP BY p.patient_id ) AS latest_filt   "
                + "         ON latest_filt.patient_id = p.patient_id "
                + "         WHERE  latest_filt.encounter_datetime = e.encounter_datetime "
                + "         AND o.concept_id =  ${23986}  AND o.value_coded IN ( ${1098} , ${23720} )   "
                + "                 ) FILT_with_IPT_dispensation "
                + "                 ON FILT_with_IPT_dispensation.patient_id = p.person_id ";

        StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

        sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

        return sqlPatientDataDefinition;
    }

    /**
     * <b>Technical Specs</b>
     *
     * <blockquote>
     *
     * <p>The most recent date from the following criterias:</p>
     * <p>Profilaxia (INH) (Concept ID 6122) marked with the value Fim (Concept ID 1267) on Ficha Clínica – Mastercard (Encounter Type 6) registered until the report generation date </>
     * <p>Profilaxia com INH – TPI (Data Fim)  (Concept ID 6129) marked in Ficha de Seguimento until the report generation date </p>
     *
     * </blockquote>
     *
     * @return {@link DataDefinition}
     */
    public DataDefinition getPatientsAndIPTCompletioDateOnFichaClinicaOrFichaSeguimento() {

        SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
        sqlPatientDataDefinition.setName("19- IPT completion Date - on Ficha Clinica or Ficha Seguimento  ");
        sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

        Map<String, Integer> valuesMap = new HashMap<>();
        valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());

        String query =
              "              SELECT   fila.patient_id, MAX(fila.encounter_datetime) AS encounter_datetime   "
                + "                    FROM  (SELECT  p.patient_id, e.encounter_datetime "
                + "                    FROM   patient p   "
                + "                    INNER JOIN encounter e ON e.patient_id = p.patient_id   "
                + "                    INNER JOIN obs o ON e.encounter_id = o.encounter_id   "
                + "                    WHERE   e.encounter_type = ${6} "
                + "             AND p.voided = 0 AND e.voided = 0 "
                + "             AND o.voided = 0 AND o.concept_id = ${6122} "
                + "             AND o.value_coded = ${1267}  AND e.location_id = :location "
                + "             AND e.encounter_datetime <= CURDATE() "
                + "             UNION    "
                + "             SELECT p.patient_id, e.encounter_datetime "
                + "                    FROM  patient p   "
                + "                    INNER JOIN encounter e ON e.patient_id = p.patient_id "
                + "                    INNER JOIN obs o ON e.encounter_id = o.encounter_id "
                + "                    WHERE e.encounter_type IN (${6},${9}) AND p.voided = 0 "
                + "             AND e.voided = 0 AND o.voided = 0   "
                + "             AND o.concept_id = ${6129}  AND e.location_id = :location  "
                + "             AND e.encounter_datetime <= CURDATE()) AS fila "
                + "              GROUP BY fila.patient_id  )IPT_completion "
                + "                 ON IPT_completion.patient_id = p.person_id ";

        StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

        sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

        return sqlPatientDataDefinition;
    }
    /**
     * <b>Technical Specs</b>
     *
     * <blockquote>
     *
     * <p>The most recent “Última Profilaxia Isoniazida (Data Fim)” (Concept ID 6129) registered in Ficha Resumo –
     * Mastercard (Encounter Type 53) until the report generation date></p>
     *
     * </blockquote>
     *
     * @return {@link DataDefinition}
     */
    public DataDefinition getPatientsAndIPTCompetionDateOnFichaResumo() {

        SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
        sqlPatientDataDefinition.setName("20 - IPT Completion Date - on Ficha Resumo  ");
        sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

        Map<String, Integer> valuesMap = new HashMap<>();
        valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());

        String query =
                "        SELECT p.patient_id, MAX(o.value_datetime) AS encounter_datetime "
                + "      FROM  patient p  "
                + "      INNER JOIN encounter e ON e.patient_id = p.patient_id "
                + "      INNER JOIN obs o ON e.encounter_id = o.encounter_id "
                + "      WHERE  e.encounter_type = ${53}    "
                + "      AND p.voided = 0  "
                + "      AND e.voided = 0  "
                + "      AND o.voided = 0  "
                + "      AND o.concept_id = ${6129}   "
                + "      AND e.location_id = :location  "
                + "      AND o.value_datetime <= CURDATE()  "
                + "                GROUP BY p.patient_id )IPT_completion_mastercard    "
                + "                 ON IPT_completion_mastercard.patient_id = p.person_id   ";

        StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

        sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

        return sqlPatientDataDefinition;
    }

    /**
     * <b>Technical Specs</b>
     *
     * <blockquote>
     *
     * <p>Expected Completion Date = IPT Start Date (earliest date between 14,15 and 16) + 173 days</p>
     *
     * </blockquote>
     *
     * @return {@link DataDefinition}
     */
    public DataDefinition getPatientsAndIPTCompletionDateAndIPTStartDatePlus173Days() {

        SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
        sqlPatientDataDefinition.setName("IPT expected completion Date - IPT Start Date + 173 Days  ");
        sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

        Map<String, Integer> valuesMap = new HashMap<>();
        valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());

        String query =
               "            SELECT    "
                + "                patient_id, DATE_ADD(MAX(consultation_date), INTERVAL 173 DAY) AS expected_date   "
                + "                FROM  (SELECT  p.patient_id, MIN(e.encounter_datetime) AS consultation_date   "
                + "                FROM  patient p   "
                + "         INNER JOIN  encounter e ON p.patient_id = e.patient_id   "
                + "         INNER JOIN  obs o ON e.encounter_id = o.encounter_id   "
                + "                WHERE  p.voided = 0 AND e.voided = 0   "
                + "         AND o.voided = 0   "
                + "         AND e.encounter_type = ${60}    "
                + "         AND o.concept_id = ${23985} AND e.location_id = :location   "
                + "         AND o.value_coded IN (${656},${23982})   "
                + "         AND e.encounter_datetime BETWEEN :startDate AND  :endDate   "
                + "                GROUP BY p.patient_id   "
                + "                UNION "
                + "           SELECT p.patient_id, CASE  "
                + "             WHEN o.concept_id = ${6122}  THEN MIN(e.encounter_datetime)  "
                + "             WHEN o.concept_id = ${6128}  THEN MIN(o.value_datetime)  "
                + "         END AS consultation_date  "
                + "      FROM  patient p  "
                + "             INNER JOIN  encounter e ON p.patient_id = e.patient_id  "
                + "             INNER JOIN obs o ON e.encounter_id = o.encounter_id  "
                + "      WHERE p.voided = 0 AND e.voided = 0  "
                + "             AND o.voided = 0 AND e.location_id = :location  "
                + "             AND e.encounter_type IN (${6} , ${9})  "
                + "             AND ((o.concept_id = ${6122}   "
                + "             AND o.value_coded = ${1256}   "
                + "             AND e.encounter_datetime BETWEEN :startDate AND  :endDate)  "
                + "             OR (o.concept_id = ${6128}   "
                + "             AND o.value_datetime IS NOT NULL  "
                + "             AND o.value_datetime BETWEEN :startDate AND  :endDate))  "
                + "      GROUP BY p.patient_id   "
                + "                UNION    "
                + "                 SELECT p.patient_id, o.value_datetime AS  consultation_date  "
                + "      FROM  patient p  "
                + "      INNER JOIN encounter e ON e.patient_id = p.patient_id  "
                + "      INNER JOIN obs o ON e.encounter_id = o.encounter_id  "
                + "      WHERE  e.encounter_type = ${53}  AND p.voided = 0  "
                + "      AND e.voided = 0  "
                + "      AND o.voided = 0  "
                + "      AND o.value_datetime IS NOT NULL  "
                + "      AND o.concept_id = ${6128}   "
                + "      AND e.location_id = :location  "
                + "      AND o.value_datetime BETWEEN :startDate AND :endDate  "
                + "                 ) union_tbl GROUP BY patient_id   "
                + "                 ) IPT_expected_completion    "
                + "                 ON IPT_expected_completion.patient_id = p.person_id   ";

        StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

        sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

        return sqlPatientDataDefinition;
    }

    /**
     * <b>Technical Specs</b>
     *
     * <blockquote>
     *
     * <p>Difference between Registered vs Expected Completion Date (In Number of Days) = IPT End Date
     * (the most recent between 19 and 20) minus Expected Completion Date (21)</p>
     *
     * </blockquote>
     *
     * @return {@link DataDefinition}
     */
    public DataDefinition getPatientAndDifferenceBetweenRegisteredCompletionDateAndExpectedCompletionDate() {

        SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
        sqlPatientDataDefinition.setName("22 -  Difference between Registered Completion Date and Expected Completion Date  ");
        sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

        Map<String, Integer> valuesMap = new HashMap<>();
        valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());

        String query =
               "           SELECT p.patient_id, DATEDIFF(union_tbl.encounter_datetime, tbl_21.expected_date) AS result, union_tbl.encounter_datetime AS real_date,tbl_21.expected_date AS expected FROM patient p   "
                + "                JOIN (SELECT "
                + "         fila.patient_id, MAX(fila.encounter_datetime) AS encounter_datetime FROM   "
                + "         (SELECT  p.patient_id, e.encounter_datetime   "
                + "                    FROM patient p   "
                + "                    INNER JOIN encounter e ON e.patient_id = p.patient_id   "
                + "                    INNER JOIN obs o ON e.encounter_id = o.encounter_id   "
                + "                    WHERE e.encounter_type = ${6}     "
                + "             AND p.voided = 0 AND e.voided = 0   "
                + "             AND o.voided = 0 AND o.concept_id = ${6122}    "
                + "             AND o.value_coded = ${1267}  AND e.location_id = :location  "
                + "             AND e.encounter_datetime <= CURDATE()   "
                + "             UNION  "
                + "             SELECT p.patient_id, e.encounter_datetime   "
                + "                    FROM   patient p   "
                + "                    INNER JOIN encounter e ON e.patient_id = p.patient_id   "
                + "                    INNER JOIN obs o ON e.encounter_id = o.encounter_id   "
                + "                    WHERE e.encounter_type IN (${6},${9}) AND p.voided = 0   "
                + "             AND e.voided = 0 AND o.voided = 0   "
                + "             AND o.concept_id = ${6129}  AND e.location_id = :location   "
                + "             AND e.encounter_datetime <= CURDATE()   "
                + "             UNION "
                + "            SELECT p.patient_id, o.value_datetime AS encounter_datetime  "
                + "      FROM   patient p  "
                + "      INNER JOIN encounter e ON e.patient_id = p.patient_id  "
                + "      INNER JOIN obs o ON e.encounter_id = o.encounter_id  "
                + "      WHERE  e.encounter_type = ${53}    "
                + "      AND p.voided = 0  "
                + "      AND e.voided = 0  "
                + "      AND o.voided = 0  "
                + "      AND o.concept_id = ${6129} "
                + "      AND e.location_id = :location  "
                + "      AND o.value_datetime <= CURDATE() ) AS fila "
                + "              GROUP BY fila.patient_id) union_tbl   "
                + "              ON union_tbl.patient_id = p.patient_id   "
                + "              LEFT JOIN   "
                + "              (SELECT patient_id, DATE_ADD(MAX(consultation_date), INTERVAL 173 DAY) AS expected_date   "
                + "                FROM  (SELECT p.patient_id, MIN(e.encounter_datetime) AS consultation_date   "
                + "                FROM patient p   "
                + "         INNER JOIN encounter e ON p.patient_id = e.patient_id   "
                + "         INNER JOIN obs o ON e.encounter_id = o.encounter_id   "
                + "                WHERE p.voided = 0 AND e.voided = 0   "
                + "         AND o.voided = 0   "
                + "         AND e.encounter_type = ${60} "
                + "         AND o.concept_id = ${23985}  "
                + "         AND e.location_id = :location   "
                + "         AND o.value_coded IN (${656},${23982})   "
                + "         AND e.encounter_datetime BETWEEN :startDate AND  :endDate GROUP BY p.patient_id   "
                + "                UNION   "
                + "                SELECT p.patient_id, CASE  "
                + "             WHEN o.concept_id = ${6122}  THEN MIN(e.encounter_datetime)  "
                + "             WHEN o.concept_id = ${6128}  THEN MIN(o.value_datetime)  "
                + "         END AS consultation_date  "
                + "      FROM  patient p  "
                + "             INNER JOIN  encounter e ON p.patient_id = e.patient_id  "
                + "             INNER JOIN  obs o ON e.encounter_id = o.encounter_id  "
                + "      WHERE  p.voided = 0 AND e.voided = 0  "
                + "             AND o.voided = 0 AND e.location_id = :location  "
                + "             AND e.encounter_type IN (${6} , ${9})  "
                + "             AND ((o.concept_id = ${6122}   "
                + "             AND o.value_coded = ${1256}   "
                + "             AND e.encounter_datetime BETWEEN :startDate AND  :endDate)  "
                + "             OR (o.concept_id = ${6128}   "
                + "             AND o.value_datetime IS NOT NULL  "
                + "             AND o.value_datetime BETWEEN :startDate AND  :endDate)) GROUP BY p.patient_id   "
                + "                UNION   "
                + "      SELECT p.patient_id, o.value_datetime AS consultation_date  "
                + "      FROM patient p  "
                + "      INNER JOIN encounter e ON e.patient_id = p.patient_id  "
                + "      INNER JOIN obs o ON e.encounter_id = o.encounter_id  "
                + "      WHERE e.encounter_type = ${53}  AND p.voided = 0  "
                + "      AND e.voided = 0  "
                + "      AND o.voided = 0  "
                + "      AND o.value_datetime IS NOT NULL  "
                + "      AND o.concept_id = ${6128}   "
                + "      AND e.location_id = :location  "
                + "      AND o.value_datetime BETWEEN :startDate AND :endDate  ) union_tbl   "
                + "                    GROUP BY patient_id) tbl_21   "
                + "                    ON tbl_21.patient_id = p.patient_id) AS expected_registered_date_difference   "
                + "                 ON expected_registered_date_difference.patient_id = p.person_id   ";

        StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

        sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

        return sqlPatientDataDefinition;
    }
}
