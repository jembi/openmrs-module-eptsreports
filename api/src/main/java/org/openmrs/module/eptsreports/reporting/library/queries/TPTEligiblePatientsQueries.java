package org.openmrs.module.eptsreports.reporting.library.queries;

public class TPTEligiblePatientsQueries {


    /**
     * 1: Select Encounter Datetime From Ficha Clinica - Master Card
     * (encounter type 6) with “Outras prescricoes” (concept id 1719)
     * with value coded equal to “3HP” (concept id 23954) and encounter
     * datetime <= End date;
     *
     * @return String
     */
    public static String getMpart1(){

        return " SELECT p.patient_id, "
                + "               e.encounter_datetime "
                + "        FROM   patient p "
                + "               inner join encounter e "
                + "                       ON e.patient_id = p.patient_id "
                + "               inner join obs o "
                + "                       ON o.encounter_id = e.encounter_id "
                + "        WHERE  p.voided = 0 "
                + "               AND e.voided = 0 "
                + "               AND o.voided = 0 "
                + "               AND e.location_id = :location "
                + "               AND e.encounter_type = ${60} "
                + "               AND o.concept_id = ${23985} "
                + "               AND o.value_coded IN ( ${23954}, ${23984})"
                + "               AND e.encounter_datetime <= :endDate " ;
    }



    /**
     * 2: Select Encounter Datetime from FILT (encounter type 60)
     * with “Regime de TPT” (concept id 23985) value coded “3HP” or
     * ” 3HP+Piridoxina” (concept id in [23954, 23984]) and encounter
     * datetime <= End date
     *
     * @return String
     */
    public static String getMpart2(){

        return " SELECT p.patient_id, e.encounter_datetime "
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
                + "                          AND e.encounter_datetime <= :endDate " ;
    }



    /**
     * 3: Select value datetime(value datetime, concept id 6128)
     * of Última profilaxia(concept id 23985) value coded 3HP
     * (concept id 23954) and Data Início da Profilaxia TPT(value datetime,
     * concept id 6128) registered by reporting end date
     * on Ficha Resumo (Encounter type 53) ; or
     *
     * @return String
     */
    public static String getMpart3(){

        return " SELECT p.patient_id, o2.value_datetime "
                + "FROM   patient p "
                + "       INNER JOIN encounter e ON p.patient_id = e.patient_id "
                + "       INNER JOIN obs o ON e.encounter_id = o.encounter_id "
                + "       INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id "
                + "WHERE  p.voided = 0 AND e.voided = 0 AND o.voided = 0 "
                + "       AND e.location_id = :location "
                + "       AND e.encounter_type = ${53} "
                + "       AND ( o.concept_id = ${23985} AND o.value_coded = ${23954} ) "
                + "       AND ( o2.concept_id = ${6128} AND o2.value_datetime BETWEEN :startDate AND :endDate ) ";
    }

    /**
     * 4: Select encounter datetime of Profilaxia TPT
     * (concept id 23985) value coded 3HP (concept id 23954)
     * and Estado da Profilaxia (concept id 165308) value coded
     * Início (concept id 1256) registered  by reporting end date
     * on Ficha Clinica (Encounter type 6) ; or
     *
     * @return String
     */
    public static String getMpart4(){

        return " SELECT p.patient_id, e.encounter_datetime "
                + "FROM   patient p "
                + "       INNER JOIN encounter e ON p.patient_id = e.patient_id "
                + "       INNER JOIN obs o ON e.encounter_id = o.encounter_id "
                + "       INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id "
                + "WHERE  p.voided = 0 AND e.voided = 0 AND o.voided = 0 AND o2.voided = 0 "
                + "       AND e.location_id = :location "
                + "       AND e.encounter_type = ${6}"
                + "       AND o.concept_id = ${23985} AND o.value_coded = ${23954} "
                + "       AND o2.concept_id = ${165308} AND o2.value_coded = ${1256} "
                + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate ";
    }


    /**
     * 5: Select encounter datetime from Ficha Clinica - Master Card
     * (encounter type 6) with “Outras prescricoes” (concept id 1719)
     * with value coded equal to “3HP” (concept id 23954) and encounter
     * <= end date and
     * <li> no other 3HP prescriptions [“Outras prescricoes”
     * (concept id 1719) with value coded equal to “3HP” (concept id 23954)]
     * marked on Ficha-Clínica in the 4 months prior to the 3HP Start Date
     * and </li>
     * <li> no “Regime de TPT” (concept id 23985) with value coded “3HP” or
     * ” 3HP+Piridoxina” (concept id in [23954, 23984])  marked on FILT
     * (encounter type 60) in the 4 months prior to the 3HP Start Date; </li>
     *
     * @return String
     */
    public static String getMpart5(){

        return " SELECT p.patient_id, pickup.first_pickup_date "
                + "                 FROM  patient p "
                + "                  INNER JOIN encounter e ON e.patient_id = p.patient_id "
                + "                  INNER JOIN obs o ON o.encounter_id = e.encounter_id "
                + "                  INNER JOIN (SELECT  p.patient_id, MIN(e.encounter_datetime) as first_pickup_date "
                + "                      FROM    patient p "
                + "                      INNER JOIN encounter e ON e.patient_id = p.patient_id "
                + "                      INNER JOIN obs o ON o.encounter_id = e.encounter_id "
                + "                      WHERE   p.voided = 0 "
                + "                          AND e.voided = 0 "
                + "                          AND o.voided = 0 "
                + "                          AND e.location_id = :location "
                + "                          AND e.encounter_type = ${6} "
                + "                          AND o.concept_id = ${1719} "
                + "                          AND o.value_coded = ${23954} "
                + "                          AND e.encounter_datetime <= :endDate "
                + "                      GROUP BY p.patient_id) AS pickup "
                + "              ON pickup.patient_id = p.patient_id "
                + "                 WHERE p.patient_id NOT IN ( SELECT pp.patient_id "
                + "                      FROM patient pp "
                + "                            INNER JOIN encounter ee ON ee.patient_id = pp.patient_id "
                + "                            INNER JOIN obs oo ON oo.encounter_id = ee.encounter_id "
                + "                      WHERE pp.voided = 0 "
                + "                            AND p.patient_id = pp.patient_id "
                + "                           AND ee.voided = 0 "
                + "                           AND oo.voided = 0 "
                + "                           AND ee.location_id = :location "
                + "                           AND ee.encounter_type = ${6} "
                + "                           AND oo.concept_id = ${1719} "
                + "                           AND oo.value_coded = ${23954} "
                + "                           AND ee.encounter_datetime >= DATE_SUB(pickup.first_pickup_date, INTERVAL 120 DAY) "
                + "                           AND ee.encounter_datetime < pickup.first_pickup_date "
                + "                 UNION "
                + "                    SELECT pp.patient_id "
                + "                          FROM patient pp "
                + "                                INNER JOIN encounter ee ON ee.patient_id = pp.patient_id "
                + "                                INNER JOIN obs oo ON oo.encounter_id = ee.encounter_id "
                + "                          WHERE pp.voided = 0 "
                + "                                AND p.patient_id = pp.patient_id "
                + "                               AND ee.voided = 0 "
                + "                               AND oo.voided = 0 "
                + "                               AND ee.location_id = :location "
                + "                               AND ee.encounter_type = ${60} "
                + "                               AND oo.concept_id = ${23985} "
                + "                               AND oo.value_coded IN (${23954},${23984}) "
                + "                               AND ee.encounter_datetime >= DATE_SUB(pickup.first_pickup_date, INTERVAL 120 DAY) "
                + "                               AND ee.encounter_datetime < pickup.first_pickup_date )";

    }
}
