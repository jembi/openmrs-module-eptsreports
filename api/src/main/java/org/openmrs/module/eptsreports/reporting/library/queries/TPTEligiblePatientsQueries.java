package org.openmrs.module.eptsreports.reporting.library.queries;

public class TPTEligiblePatientsQueries {

  /**
   * 1: Select Encounter Datetime From Ficha Clinica - Master Card (encounter type 6) with “Outras
   * prescricoes” (concept id 1719) with value coded equal to “3HP” (concept id 23954) and encounter
   * datetime <= End date;
   *
   * @return {@link String}
   */
  public static String getMpart1() {

    return " SELECT p.patient_id, "
        + "               e.encounter_datetime AS encounter_datetime "
        + "        FROM   patient p "
        + "               inner join encounter e "
        + "                       ON e.patient_id = p.patient_id "
        + "               inner join obs o "
        + "                       ON o.encounter_id = e.encounter_id "
        + "        WHERE  p.voided = 0 "
        + "               AND e.voided = 0 "
        + "               AND o.voided = 0 "
        + "               AND e.location_id = :location "
        + "               AND e.encounter_type = ${6} "
        + "               AND o.concept_id = ${1719} "
        + "               AND o.value_coded = ${23954} "
        + "               AND e.encounter_datetime <= :endDate ";
  }

  /**
   * 2: Select Encounter Datetime from FILT (encounter type 60) with “Regime de TPT” (concept id
   * 23985) value coded “3HP” or ” 3HP+Piridoxina” (concept id in [23954, 23984]) and encounter
   * datetime <= End date
   *
   * @return {@link String}
   */
  public static String getMpart2() {

    return " SELECT p.patient_id, e.encounter_datetime AS encounter_datetime "
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
        + "                          AND e.encounter_datetime <= :endDate ";
  }

  /**
   * 3: Select value datetime(value datetime, concept id 6128) of Última profilaxia(concept id
   * 23985) value coded 3HP (concept id 23954) and Data Início da Profilaxia TPT(value datetime,
   * concept id 6128) registered by reporting end date on Ficha Resumo (Encounter type 53) ; or
   *
   * @return {@link String}
   */
  public static String getMpart3() {

    return " SELECT p.patient_id, o2.value_datetime AS encounter_datetime "
        + "FROM   patient p "
        + "       INNER JOIN encounter e ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o ON e.encounter_id = o.encounter_id "
        + "       INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id "
        + "WHERE  p.voided = 0 AND e.voided = 0 AND o.voided = 0 "
        + "       AND e.location_id = :location "
        + "       AND e.encounter_type = ${53} "
        + "       AND ( o.concept_id = ${23985} AND o.value_coded = ${23954} ) "
        + "       AND ( o2.concept_id = ${6128} AND o2.value_datetime <= :endDate ) ";
  }

  /**
   * 4: Select encounter datetime of Profilaxia TPT (concept id 23985) value coded 3HP (concept id
   * 23954) and Estado da Profilaxia (concept id 165308) value coded Início (concept id 1256)
   * registered by reporting end date on Ficha Clinica (Encounter type 6) ; or
   *
   * @return String
   */
  public static String getMpart4() {

    return " SELECT p.patient_id, e.encounter_datetime AS encounter_datetime"
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
   * 5: Select encounter datetime from Ficha Clinica - Master Card (encounter type 6) with “Outras
   * prescricoes” (concept id 1719) with value coded equal to “3HP” (concept id 23954) and encounter
   * <= end date and
   * <li>no other 3HP prescriptions [“Outras prescricoes” (concept id 1719) with value coded equal
   *     to “3HP” (concept id 23954)] marked on Ficha-Clínica in the 4 months prior to the 3HP Start
   *     Date and
   * <li>no “Regime de TPT” (concept id 23985) with value coded “3HP” or ” 3HP+Piridoxina” (concept
   *     id in [23954, 23984]) marked on FILT (encounter type 60) in the 4 months prior to the 3HP
   *     Start Date;
   *
   * @return String
   */
  public static String getMpart5() {

    return " SELECT p.patient_id, pickup.first_pickup_date AS encounter_datetime "
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

  /**
   * valuesMap.put("1719", tbMetadata.getTreatmentPrescribedConcept().getConceptId());
   * valuesMap.put("165307", tbMetadata.getDT3HPConcept().getConceptId()); valuesMap.put("6",
   * hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
   * <li>
   *
   *     <p>6: Select all patients with Outras prescricoes(concept id 1719) value coded DT-3HP
   *     (concept id 165307) on Ficha clinica (encounter type 6) by report end date;
   *
   * @return String
   */
  public static String getMpart6() {

    return "SELECT p.patient_id, e.encounter_datetime AS encounter_datetime "
        + "FROM   patient p "
        + "       INNER JOIN encounter e ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o ON e.encounter_id = o.encounter_id "
        + "WHERE  p.voided = 0 "
        + "       AND e.voided = 0 "
        + "       AND o.voided = 0 "
        + "       AND e.location_id = :location "
        + "       AND e.encounter_type = ${6} "
        + "       AND o.concept_id = ${1719} "
        + "       AND o.value_coded = ${165307} "
        + "       AND e.encounter_datetime <= :endDate ";
  }

  /**
   *
   * <li>7: Select encounter datetime as FILT 3HP start date from “Regime de TPT” (concept id 23985)
   *     with value coded “3HP” or ” 3HP+Piridoxina” (concept id in [23954, 23984]) and “Seguimento
   *     de tratamento TPT”(concept ID 23987) value coded “inicio” or “re-inicio”(concept ID in
   *     [1256, 1705]) marked on FILT (encounter type 60) and encounter datetime <= end date;
   *
   * @return String
   */
  public static String getMpart7() {

    return "SELECT p.patient_id, e.encounter_datetime AS encounter_datetime"
        + "FROM   patient p "
        + "       inner join encounter e ON p.patient_id = e.patient_id "
        + "       inner join obs o ON e.encounter_id = o.encounter_id "
        + "       inner join obs o2 ON e.encounter_id = o2.encounter_id "
        + "WHERE  p.voided = 0 AND e.voided = 0 AND o.voided = 0 AND o2.voided = 0 "
        + "       AND e.location_id = :location "
        + "       AND e.encounter_type = ${60} "
        + "       AND ( o.concept_id = ${23985} AND o.value_coded IN ( ${23954}, ${23984} ) ) "
        + "       AND (o2.concept_id=  ${23987} AND o2.value_coded IN ( ${1256} , ${1705} ))   "
        + "       AND e.encounter_datetime <= :endDate ";
  }

  // valuesMap.put("23985", tbMetadata.getRegimeTPTConcept().getConceptId());
  //    valuesMap.put("23954", tbMetadata.get3HPConcept().getConceptId());
  //    valuesMap.put("23984", tbMetadata.get3HPPiridoxinaConcept().getConceptId());
  //    valuesMap.put("23987", hivMetadata.getPatientTreatmentFollowUp().getConceptId());
  //    valuesMap.put("60", tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId());
  //    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
  //    valuesMap.put("165308", tbMetadata.getDataEstadoDaProfilaxiaConcept().getConceptId());
  //    valuesMap.put("1256", hivMetadata.getStartDrugs().getConceptId());
  //    valuesMap.put("1719", tbMetadata.getTreatmentPrescribedConcept().getConceptId());
  //    valuesMap.put("165307", tbMetadata.getDT3HPConcept().getConceptId());
  //    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
  //    valuesMap.put("6128",
  // hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId());
  //    valuesMap.put("1257", hivMetadata.getContinueRegimenConcept().getConceptId());
  //    valuesMap.put("1267", hivMetadata.getCompletedConcept().getConceptId());
  //    valuesMap.put("1705", hivMetadata.getRestartConcept().getConceptId());

  /**
   *
   *
   * <blockquote>
   *
   * <p>Select all patients with “Regime de TPT” (concept id 23985) with value coded “3HP” or ”
   * 3HP+Piridoxina” (concept id in [23954, 23984]) and “Seguimento de tratamento TPT”(concept ID
   * 23987) value coded “continua” or “fim” or no value(concept ID in [1257, 1267, null]) marked on
   * the first FILT (encounter type 60) and encounter datetime between start date and end date and:
   *
   * </blockquote>
   *
   * <li>
   *
   *     <p>No other Regime de TPT (concept id 23985) value coded “3HP” or ” 3HP+Piridoxina”
   *     (concept id in [23954, 23984]) marked on FILT (encounter type 60) in the 4 months prior to
   *     the FILT 3HP start date. ; and
   * <li>
   *
   *     <p>No other 3HP start dates marked on Ficha clinica (encounter type 6, encounter datetime)
   *     with Profilaxia TPT (concept id 23985) value coded 3HP (concept id 23954) and Estado da
   *     Profilaxia (concept id 165308) value coded Início (concept id 1256) or Outras prescrições
   *     (concept id 1719) value coded 3HP or DT-3HP (concept id in [23954,165307])in the 4 months
   *     prior to the FILT 3HP start date. ; and
   * <li>
   *
   *     <p>No other 3HP start dates marked on Ficha Resumo (encounter type 53) with Última
   *     profilaxia(concept id 23985) value coded 3HP(concept id 23954) and Data Início da
   *     Profilaxia TPT(value datetime, concept id 6128) in the 4 months prior to the FILT 3HP start
   *     date. ;
   *
   * @return String
   */
  public static String getMpart8() {

    return "SELECT p.patient_id, e.encounter_datetime AS encounter_datetime"
        + "FROM   patient p "
        + "       inner join encounter e ON p.patient_id = e.patient_id "
        + "       inner join obs o ON e.encounter_id = o.encounter_id "
        + "       inner join obs o2 ON e.encounter_id = o2.encounter_id "
        + "WHERE  p.voided = 0 AND e.voided = 0 AND o.voided = 0 AND o2.voided = 0 "
        + "       AND e.location_id = :location "
        + "       AND e.encounter_type = ${60} "
        + "       AND ( o.concept_id = ${23985} AND o.value_coded IN ( ${23954}, ${23984} ) ) "
        + "       AND ( o2.concept_id = ${23987} AND o2.value_coded IN ( ${1257}, ${1267} ) OR o2.value_coded IS NULL ) "
        + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate "
        + "       AND p.patient_id NOT IN ( "
        + "           SELECT p.patient_id "
        + "           FROM   patient p "
        + "                  inner join encounter e ON e.patient_id = p.patient_id "
        + "                  inner join obs o ON e.encounter_id = o.encounter_id "
        + "                  inner join (SELECT p.patient_id, "
        + "                         Min(e.encounter_datetime) AS start_date "
        + "                             FROM   patient p "
        + "                             inner join encounter e ON p.patient_id = e.patient_id "
        + "                             inner join obs o ON e.encounter_id = o.encounter_id "
        + "                             inner join obs o2 ON e.encounter_id = o2.encounter_id "
        + "                             WHERE  p.voided = 0 AND e.voided = 0 AND o.voided = 0 "
        + "                             AND e.encounter_type = ${60} "
        + "                             AND ( o.concept_id = ${23985} AND o.value_coded IN ( ${23954}, ${23984} ) ) "
        + "                             AND ( o2.concept_id = ${23987} AND o2.value_coded IN ( ${1256}, ${1705} ) ) "
        + "                             AND e.encounter_datetime BETWEEN :startDate AND :endDate) filt "
        + "                             ON filt.patient_id = p.patient_id "
        + "                  WHERE  p.voided = 0 "
        + "                  AND e.voided = 0 "
        + "                  AND o.voided = 0 "
        + "                  AND e.location_id = :location "
        + "                  AND e.encounter_type = ${60} "
        + "                  AND o.concept_id = ${23985} AND o.value_coded IN ( ${23954}, ${23984} ) "
        + "                  AND e.encounter_datetime BETWEEN Date_sub(filt.start_date, interval 4 month) AND filt.start_date "
        + "                  GROUP  BY p.patient_id "
        + "           UNION "
        + "           SELECT p.patient_id "
        + "           FROM   patient p "
        + "           inner join encounter e ON e.patient_id = p.patient_id "
        + "           inner join obs o3 ON e.encounter_id = o3.encounter_id "
        + "           inner join obs o4 ON e.encounter_id = o4.encounter_id "
        + "           inner join obs o5 ON e.encounter_id = o5.encounter_id "
        + "           inner join (SELECT p.patient_id, "
        + "                 Min(e.encounter_datetime) AS start_date "
        + "           FROM   patient p "
        + "           inner join encounter e ON p.patient_id = e.patient_id "
        + "           inner join obs o ON e.encounter_id = o.encounter_id "
        + "           inner join obs o2 ON e.encounter_id = o2.encounter_id "
        + "                 WHERE  p.voided = 0 AND e.voided = 0 AND o.voided = 0 "
        + "                 AND e.encounter_type = ${60} "
        + "                 AND ( o.concept_id = ${23985} AND o.value_coded IN ( ${23954}, ${23984} ) ) "
        + "                 AND ( o2.concept_id = ${23987} AND o2.value_coded IN ( ${1256}, ${1705} ) ) "
        + "                 AND e.encounter_datetime BETWEEN :startDate AND :endDate) filt "
        + "                 ON filt.patient_id = p.patient_id "
        + "           WHERE  p.voided = 0 AND e.voided = 0 AND o3.voided = 0 AND o4.voided = 0 AND o5.voided = 0 "
        + "           AND e.location_id = :location "
        + "           AND e.encounter_type = ${6} "
        + "           AND ( o3.concept_id = ${23985} AND o3.value_coded = ${23954}   "
        + "                 AND o4.concept_id = ${165308} AND o4.value_coded = ${1256} ) "
        + "           OR  ( o5.concept_id = ${1719} AND o5.value_coded IN ( ${23954}, ${165307} ) ) "
        + "           AND e.encounter_datetime BETWEEN Date_sub(filt.start_date, interval 4 month) AND filt.start_date "
        + "           GROUP  BY p.patient_id "
        + "           UNION "
        + "           SELECT p.patient_id "
        + "           FROM   patient p "
        + "           inner join encounter e ON e.patient_id = p.patient_id "
        + "           inner join obs o6 ON e.encounter_id = o6.encounter_id "
        + "           inner join obs o7 ON e.encounter_id = o7.encounter_id "
        + "           inner join (SELECT p.patient_id, "
        + "                 Min(e.encounter_datetime) AS start_date "
        + "                 FROM   patient p "
        + "                 inner join encounter e ON p.patient_id = e.patient_id "
        + "                 inner join obs o ON e.encounter_id = o.encounter_id "
        + "                 inner join obs o2 ON e.encounter_id = o2.encounter_id "
        + "                 WHERE p.voided = 0 AND e.voided = 0 AND o.voided = 0 "
        + "                 AND e.encounter_type = ${60} "
        + "                 AND ( o.concept_id = ${23985} AND o.value_coded IN ( ${23954}, ${23984} ) ) "
        + "                 AND ( o2.concept_id = ${23987} AND o2.value_coded IN ( ${1256}, ${1705} ) ) "
        + "                 AND e.encounter_datetime BETWEEN :startDate AND :endDate) filt "
        + "                 ON filt.patient_id = p.patient_id "
        + "           WHERE p.voided = 0 AND e.voided = 0 AND o6.voided = 0 AND o7.voided = 0 "
        + "           AND e.location_id = :location "
        + "           AND e.encounter_type = ${53} "
        + "           AND o6.concept_id = ${23985} AND o6.value_coded = ${23954} "
        + "           AND o7.concept_id = ${6128} AND o7.value_datetime BETWEEN Date_sub(filt.start_date, interval 4 month) AND filt.start_date "
        + "           GROUP BY p.patient_id) "
        + "GROUP BY p.patient_id ";
  }
}
