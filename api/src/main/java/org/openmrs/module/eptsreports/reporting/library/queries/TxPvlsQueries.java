package org.openmrs.module.eptsreports.reporting.library.queries;

public class TxPvlsQueries {

  /**
   *
   *
   * <blockquote>
   *
   * <li>Patients with VL result within the period and on ART for at least 3 months On Clinical
   *     Consultations
   *
   *     </blockquote>
   */
  public static String getVlResultOnClinicalConsultations() {
    return "              SELECT     p.patient_id, "
        + "                             Max(e.encounter_datetime) AS last_date "
        + "                  FROM       patient p "
        + "                  INNER JOIN encounter e "
        + "                  ON         e.patient_id =p.patient_id "
        + "                  INNER JOIN obs o "
        + "                  ON         o.encounter_id =e.encounter_id "
        + "                  INNER JOIN obs o2 "
        + "                  ON         o2.encounter_id = e.encounter_id "
        + "                  WHERE      e.encounter_type IN (${6},${9},${13},${51}) "
        + "                  AND        (( "
        + "                                                   o.concept_id = ${856} "
        + "                                        AND        o.value_numeric IS NOT NULL) "
        + "                             OR         ( "
        + "                                                   o2.concept_id= ${1305} "
        + "                                        OR         o2.value_coded IS NOT NULL)) "
        + "                  AND        e.voided =0 "
        + "                  AND        p.voided =0 "
        + "                  AND        o.voided =0 "
        + "                  AND        o2.voided =0 "
        + "                  AND        e.encounter_datetime BETWEEN date_sub(:endDate, interval 12 month) AND        :endDate "
        + "                  GROUP BY   p.patient_id";
  }

  /**
   *
   *
   * <blockquote>
   *
   * <li>Patients with VL result within the period and on ART for at least 3 months On Ficha Resumo
   *
   *     </blockquote>
   */
  public static String getVlResultOnFichaResumo() {
    return "SELECT     p.patient_id, "
        + "           Max(o.obs_datetime)AS last_date "
        + "FROM       patient p "
        + "INNER JOIN encounter e "
        + "ON         e.patient_id =p.patient_id "
        + "INNER JOIN obs o "
        + "ON         o.encounter_id =e.encounter_id "
        + "INNER JOIN obs o2 "
        + "ON         o2.encounter_id = e.encounter_id "
        + "WHERE      e.encounter_type =${53} "
        + "AND        (( "
        + "                                 o.concept_id = ${856} "
        + "                      AND        o.value_numeric IS NOT NULL) "
        + "           OR         ( "
        + "                                 o2.concept_id= ${1305} "
        + "                      AND        o2.value_coded IS NOT NULL)) "
        + "AND        e.voided =0 "
        + "AND        p.voided =0 "
        + "AND        o.voided =0 "
        + "AND        o2.voided =0 "
        + "AND        o.obs_datetime BETWEEN date_sub(:endDate, interval 12 month) AND        :endDate "
        + "GROUP BY   p.patient_id";
  }

  /**
   *
   *
   * <blockquote>
   *
   * <li>Patients that are female and have the “Delivery date” (obs concept id 5599) registered in
   *     the initial or follow-up consultation where delivery date within the period range
   *
   *     </blockquote>
   */
  public static String getBreastfeedingOnAdultoSeguimentoANDAdultoInitialConsultation() {
    return "SELECT p.patient_id, "
        + "                                 Max(o.value_datetime) AS last_date "
        + "                          FROM   patient p "
        + "                                 inner join person p2 "
        + "                                         ON p2.person_id = p.patient_id "
        + "                                 inner join encounter e "
        + "                                         ON e.patient_id = p.patient_id "
        + "                                 inner join obs o "
        + "                                         ON o.encounter_id = e.encounter_id "
        + "                          WHERE  p2.gender = 'F' "
        + "                                 AND e.encounter_type IN ( ${5}, ${6} ) "
        + "                                 AND e.location_id = :location "
        + "                                 AND o.concept_id = ${5599} "
        + "                                 AND o.value_datetime <= :endDate "
        + "                                 AND o.voided = 0 "
        + "                                 AND p.voided = 0 "
        + "                                 AND e.voided = 0 "
        + "                                 AND p2.voided = 0 "
        + "                          GROUP  BY p.patient_id";
  }

  /**
   *
   *
   * <blockquote>
   *
   * <li>Patients that are female and have registered as breastfeeding in follow up consultation
   *     within the period range
   *
   *     </blockquote>
   */
  public static String getBreastfeedingOnFichaClinica() {
    return "SELECT p.patient_id, "
        + "                                 Max(e.encounter_datetime) AS last_date "
        + "                          FROM   patient p "
        + "                                 inner join person p2 "
        + "                                         ON p2.person_id = p.patient_id "
        + "                                 inner join encounter e "
        + "                                         ON e.patient_id = p.patient_id "
        + "                                 inner join obs o "
        + "                                         ON o.encounter_id = e.encounter_id "
        + "                          WHERE  p2.gender = 'F' "
        + "                                 AND e.encounter_type = ${6} "
        + "                                 AND e.location_id = :location "
        + "                                 AND o.concept_id = ${6332} "
        + "                                 AND o.value_coded = ${1065} "
        + "                                 AND e.encounter_datetime <= :endDate "
        + "                                 AND o.voided = 0 "
        + "                                 AND e.voided = 0 "
        + "                                 AND p.voided = 0 "
        + "                                 AND p2.voided = 0 "
        + "                          GROUP  BY p.patient_id";
  }

  /**
   *
   *
   * <blockquote>
   *
   * <li>Patients that are female and have started ART for being breastfeeding as specified in
   *     “CRITÉRIO PARA INICIO DE TRATAMENTO ARV” with response equal to “LACTACAO” in the initial
   *     or follow-up consultation within the period range
   *
   *     </blockquote>
   */
  public static String
      getBreastfeedingOnAdultoSeguimentoANDAdultoInitialConsultationCriterioInicioTarV() {
    return "SELECT p.patient_id, "
        + "                                 Max(e.encounter_datetime) AS last_date "
        + "                          FROM   patient p "
        + "                                 inner join person p2 "
        + "                                         ON p2.person_id = p.patient_id "
        + "                                 inner join encounter e "
        + "                                         ON e.patient_id = p.patient_id "
        + "                                 inner join obs o "
        + "                                         ON o.encounter_id = e.encounter_id "
        + "                          WHERE  p2.gender = 'F' "
        + "                                 AND e.encounter_type IN ( ${5}, ${6} ) "
        + "                                 AND o.concept_id = ${6334} "
        + "                                 AND o.value_coded = ${6332} "
        + "                                 AND p.voided = 0 "
        + "                                 AND e.voided = 0 "
        + "                                 AND o.voided = 0 "
        + "                                 AND p2.voided = 0 "
        + "                                 AND e.location_id = :location "
        + "                                 AND e.encounter_datetime <= :endDate "
        + "                          GROUP  BY p.patient_id";
  }

  /**
   *
   *
   * <blockquote>
   *
   * <li>Patients enrolled in PTV(ETV) Program
   *
   *     </blockquote>
   */
  public static String getPatientsEnrolledInPtv() {
    return "SELECT pp.patient_id, "
        + "                                 Max(ps.start_date) AS last_date "
        + "                          FROM   patient_program pp "
        + "                                 inner join person p "
        + "                                         ON p.person_id = pp.patient_id "
        + "                                 inner join patient_state ps "
        + "                                         ON ps.patient_program_id = pp.patient_program_id "
        + "                          WHERE  p.gender = 'F' "
        + "                                 AND pp.program_id = ${8} "
        + "                                 AND ps.patient_state_id = ${27} "
        + "                                 AND pp.location_id = :location "
        + "                                 AND pp.voided = 0 "
        + "                                 AND ps.voided = 0 "
        + "                                 AND p.voided = 0 "
        + "                                 AND ps.start_date <= :endDate "
        + "                          GROUP  BY pp.patient_id";
  }

  /**
   *
   *
   * <blockquote>
   *
   * <li>Patients that are female and have registered as breastfeeding in Ficha Resumo within the
   *     period range
   *
   *     </blockquote>
   */
  public static String getBreastfeedingOnFichaResumo() {
    return "SELECT p.patient_id, "
        + "                                 Max(o.obs_datetime) AS last_date "
        + "                          FROM   patient p "
        + "                                 inner join person p2 "
        + "                                         ON p2.person_id = p.patient_id "
        + "                                 inner join encounter e "
        + "                                         ON e.patient_id = p.patient_id "
        + "                                 inner join obs o "
        + "                                         ON o.encounter_id = e.encounter_id "
        + "                          WHERE  p2.gender = 'F' "
        + "                                 AND e.encounter_type = ${53} "
        + "                                 AND o.concept_id = ${6332} "
        + "                                 AND o.value_coded = ${1065} "
        + "                                 AND e.location_id = :location "
        + "                                 AND o.obs_datetime <= :endDate "
        + "                                 AND p.voided = 0 "
        + "                                 AND e.voided = 0 "
        + "                                 AND o.voided = 0 "
        + "                                 AND p2.voided = 0 "
        + "                          GROUP  BY p.patient_id";
  }

  /**
   *
   *
   * <blockquote>
   *
   * <li>Patient who have “Actualmente está a amamentar?” marked as “Sim” and Data de Colheita is
   *     during the period range
   *
   *     </blockquote>
   */
  public static String getBreastfeedingOnFSR() {
    return "SELECT p.patient_id, "
        + "                                 Max(o2.value_datetime) AS last_date "
        + "                          FROM   patient p "
        + "                                 inner join encounter e "
        + "                                         ON e.patient_id = p.patient_id "
        + "                                 inner join obs o "
        + "                                         ON o.encounter_id = e.encounter_id "
        + "                                 inner join obs o2 "
        + "                                         ON o2.encounter_id = e.encounter_id "
        + "                          WHERE  e.encounter_type = ${51} "
        + "                                 AND ( o.concept_id = ${6332} "
        + "                                       AND o.value_coded = ${1065} ) "
        + "                                 AND ( o2.concept_id = ${23821} "
        + "                                       AND o2.value_datetime <= :endDate ) "
        + "                                 AND p.voided = 0 "
        + "                                 AND e.voided = 0 "
        + "                                 AND o.voided = 0 "
        + "                                 AND o2.voided = 0 "
        + "                                 AND e.location_id = :location "
        + "                          GROUP  BY p.patient_id";
  }
}
