package org.openmrs.module.eptsreports.reporting.library.queries;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;

public class TxMlQueries {

  /**
   * <b>Description:</b> Number of patients who missed Appointment
   *
   * @return {@link String}
   */
  public static String getPatientsWhoMissedAppointment(
      int returnVisitDateForDrugsConcept,
      int returnVisitDateConcept,
      int pharmacyEncounterType,
      int adultoSequimentoEncounterType,
      int pediatriaSeguimentoEncounterType,
      int masterCardDrugPickupEncounterType,
      int artPickupDateMasterCardConcept) {

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("returnVisitDateForArvDrug", returnVisitDateForDrugsConcept);
    valuesMap.put("aRVPharmaciaEncounterType", pharmacyEncounterType);
    valuesMap.put("returnVisitDate", returnVisitDateConcept);
    valuesMap.put("adultoSeguimentoEncounterType", adultoSequimentoEncounterType);
    valuesMap.put("pediatriaSeguimentoEncounterType", pediatriaSeguimentoEncounterType);
    valuesMap.put("artDatePickupMasterCard", artPickupDateMasterCardConcept);
    valuesMap.put("masterCardDrugPickupEncounterType", masterCardDrugPickupEncounterType);
    String query =
        " SELECT final.patient_id    "
            + "             from(    "
            + "                SELECT   patient_id,"
            + "                     Greatest(COALESCE(return_date_fila,0),COALESCE(return_date_consulta,0),COALESCE(return_date_master,0)) AS return_date"
            + "                FROM     ("
            + "                         SELECT p.patient_id,"
            + "                         ("
            + "                                SELECT   o.value_datetime"
            + "                                FROM     encounter e"
            + "                                JOIN     obs o"
            + "                                ON       e.encounter_id=o.encounter_id"
            + "                                WHERE    p.patient_id=e.patient_id"
            + "                                AND      e.location_id=:location"
            + "                                AND      e.encounter_type=${aRVPharmaciaEncounterType}"
            + "                                AND      o.concept_id = ${returnVisitDateForArvDrug}"
            + "                                AND      e.voided = 0"
            + "                                AND      o.voided = 0"
            + "                                AND      e.encounter_datetime ="
            + "                                         ("
            + "                                                    SELECT     e.encounter_datetime AS return_date"
            + "                                                    FROM       encounter e"
            + "                                                    INNER JOIN obs o"
            + "                                                    ON         e.encounter_id = o.encounter_id"
            + "                                                    WHERE      p.patient_id = e.patient_id"
            + "                                                    AND        e.voided = 0"
            + "                                                    AND        o.voided = 0"
            + "                                                    AND        e.encounter_type = ${aRVPharmaciaEncounterType}"
            + "                                                    AND        e.location_id = :location"
            + "                                                    AND        e.encounter_datetime <= :endDate"
            + "                                                    ORDER BY   e.encounter_datetime DESC limit 1)"
            + "                                ORDER BY o.value_datetime DESC limit 1) AS return_date_fila,"
            + "                                 ("
            + "                                SELECT   o.value_datetime"
            + "                                FROM     encounter e"
            + "                                JOIN     obs o"
            + "                                ON       e.encounter_id=o.encounter_id"
            + "                                WHERE    p.patient_id=e.patient_id"
            + "                                AND      e.location_id=:location"
            + "                                AND      e.encounter_type IN(${adultoSeguimentoEncounterType},"
            + "                                                             ${pediatriaSeguimentoEncounterType})"
            + "                                AND      o.concept_id = ${returnVisitDate}"
            + "                                AND      e.voided = 0"
            + "                                AND      o.voided = 0"
            + "                                AND      e.encounter_datetime ="
            + "                                         ("
            + "                                                    SELECT     e.encounter_datetime AS return_date"
            + "                                                    FROM       encounter e"
            + "                                                    INNER JOIN obs o"
            + "                                                    ON         e.encounter_id = o.encounter_id"
            + "                                                    WHERE      p.patient_id = e.patient_id"
            + "                                                    AND        e.voided = 0"
            + "                                                    AND        o.voided = 0"
            + "                                                    AND        e.encounter_type IN (${adultoSeguimentoEncounterType},"
            + "                                                                                    ${pediatriaSeguimentoEncounterType})"
            + "                                                    AND        e.location_id = :location"
            + "                                                    AND        e.encounter_datetime <= :endDate"
            + "                                                    ORDER BY   e.encounter_datetime DESC limit 1)"
            + "                                ORDER BY o.value_datetime DESC limit 1) AS return_date_consulta,"
            + "                                 ("
            + "                                  SELECT     date_add(o.value_datetime, interval 30 day) AS return_date"
            + "                                  FROM       encounter e"
            + "                                  INNER JOIN obs o"
            + "                                  ON         e.encounter_id = o.encounter_id"
            + "                                  WHERE      p.patient_id = e.patient_id"
            + "                                  AND        e.voided = 0"
            + "                                  AND        o.voided = 0"
            + "                                  AND        e.encounter_type = ${masterCardDrugPickupEncounterType} "
            + "                                  AND        e.location_id = :location"
            + "                                  AND        o.concept_id = ${artDatePickupMasterCard}"
            + "                                  AND        o.value_datetime <= :endDate"
            + "                                  ORDER BY   o.value_datetime DESC limit 1) AS return_date_master"
            + "                FROM   patient p"
            + "                WHERE  p.voided=0) e"
            + " GROUP BY e.patient_id "
            + "                HAVING  DATE_ADD(return_date, INTERVAL 28 DAY) < :endDate AND   "
            + "                        DATE_ADD(return_date, INTERVAL 28 DAY) >= DATE_ADD( :startDate, INTERVAL -1 DAY)   "
            + "             ) final    "
            + "             GROUP BY final.patient_id;";

    StringSubstitutor sub = new StringSubstitutor(valuesMap);
    return sub.replace(query);
  }

  /**
   * <b>Description:</b> Patients who have REASON PATIENT MISSED VISIT (MOTIVOS DA FALTA) as
   * “Transferido para outra US” or “Auto-transferencia” marked last Home Visit Card occurred during
   * the reporting period. Use the “data da visita” when the patient reason was marked on the home
   * visit card as the reference date
   *
   * @param homeVisitCardEncounterTypeId
   * @param reasonPatientMissedVisitConceptId
   * @param transferredOutToAnotherFacilityConceptId
   * @param autoTransferConceptId
   * @return
   */
  public static String getPatientsWithMissedVisit(
      int homeVisitCardEncounterTypeId,
      int reasonPatientMissedVisitConceptId,
      int transferredOutToAnotherFacilityConceptId,
      int autoTransferConceptId) {
    String query =
        "SELECT e.patient_id "
            + "FROM encounter e "
            + "         JOIN obs o ON e.encounter_id = o.encounter_id "
            + "         JOIN (SELECT p.patient_id, MAX(e.encounter_datetime) encounter_datetime "
            + "               FROM patient p "
            + "                        JOIN encounter e ON p.patient_id = e.patient_id "
            + "                        JOIN obs o ON e.encounter_id = o.encounter_id "
            + "               WHERE o.concept_id = %d "
            + "                 AND e.location_id = :location "
            + "                 AND e.encounter_type= %d "
            + "                 AND e.encounter_datetime BETWEEN :startDate AND :endDate AND p.voided=0 "
            + "               GROUP BY p.patient_id) last "
            + "              ON e.patient_id = last.patient_id AND last.encounter_datetime = e.encounter_datetime "
            + "WHERE o.value_coded IN (%d,%d) AND e.location_id = :location AND e.voided=0 AND o.voided=0 ";

    return String.format(
        query,
        reasonPatientMissedVisitConceptId,
        homeVisitCardEncounterTypeId,
        transferredOutToAnotherFacilityConceptId,
        autoTransferConceptId);
  }

  /**
   * <b>Description:</b> Patients who Refused or stopped treatment
   *
   * @return {@link String}
   */
  public static String getRefusedOrStoppedTreatment(
      int homeVisitCardEncounterTypeId,
      int reasonPatientMissedVisitConceptId,
      int patientForgotVisitDateConceptId,
      int patientIsBedriddenAtHomeConceptId,
      int distanceOrMoneyForTransportIsToMuchForPatientConceptId,
      int patientIsDissatifiedWithDayHospitalServicesConceptId,
      int fearOfTheProviderConceptId,
      int absenceOfHealthProviderInHealthUnitConceptId,
      int patientDoesNotLikeArvTreatmentSideEffectsConceptId,
      int patientIsTreatingHivWithTraditionalMedicineConceptId,
      int otherReasonWhyPatientMissedVisitConceptId,
      int pharmacyEncounterTypeId,
      int returnVisitDateForDrugsConcept,
      int adultoSequimentoEncounterTypeId,
      int arvPediatriaSeguimentoEncounterTypeId,
      int returnVisitDateConcept,
      int masterCardDrugPickupEncounterTypeId,
      int artDatePickupConceptId) {

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("homeVisitCardEncounterTypeId", homeVisitCardEncounterTypeId);

    valuesMap.put("reasonPatientMissedVisitConceptId", reasonPatientMissedVisitConceptId);
    valuesMap.put("patientForgotVisitDateConceptId", patientForgotVisitDateConceptId);
    valuesMap.put("patientIsBedriddenAtHomeConceptId", patientIsBedriddenAtHomeConceptId);
    valuesMap.put(
        "distanceOrMoneyForTransportIsToMuchForPatientConceptId",
        distanceOrMoneyForTransportIsToMuchForPatientConceptId);
    valuesMap.put(
        "patientIsDissatifiedWithDayHospitalServicesConceptId",
        patientIsDissatifiedWithDayHospitalServicesConceptId);
    valuesMap.put("fearOfTheProviderConceptId", fearOfTheProviderConceptId);
    valuesMap.put(
        "absenceOfHealthProviderInHealthUnitConceptId",
        absenceOfHealthProviderInHealthUnitConceptId);
    valuesMap.put(
        "patientDoesNotLikeArvTreatmentSideEffectsConceptId",
        patientDoesNotLikeArvTreatmentSideEffectsConceptId);
    valuesMap.put(
        "patientIsTreatingHivWithTraditionalMedicineConceptId",
        patientIsTreatingHivWithTraditionalMedicineConceptId);
    valuesMap.put(
        "otherReasonWhyPatientMissedVisitConceptId", otherReasonWhyPatientMissedVisitConceptId);
    valuesMap.put("pharmacyEncounterTypeId", pharmacyEncounterTypeId);
    valuesMap.put("returnVisitDateForDrugsConcept", returnVisitDateForDrugsConcept);
    valuesMap.put("adultoSequimentoEncounterTypeId", adultoSequimentoEncounterTypeId);
    valuesMap.put("arvPediatriaSeguimentoEncounterTypeId", arvPediatriaSeguimentoEncounterTypeId);
    valuesMap.put("returnVisitDateConcept", returnVisitDateConcept);
    valuesMap.put("masterCardDrugPickupEncounterTypeId", masterCardDrugPickupEncounterTypeId);
    valuesMap.put("artDatePickupConceptId", artDatePickupConceptId);

    String query =
        "SELECT e.patient_id "
            + "FROM encounter e "
            + "         JOIN obs o ON e.encounter_id = o.encounter_id "
            + "         JOIN (SELECT patient_id, "
            + " GREATEST(COALESCE(return_date_fila,0),COALESCE(return_date_consulta,0),COALESCE(return_date_master,0)) AS return_date "
            + "                           FROM (SELECT p.patient_id, "
            + "                                        (SELECT o.value_datetime"
            + "                                        FROM encounter e JOIN obs o ON e.encounter_id=o.encounter_id"
            + "                                        WHERE p.patient_id=e.patient_id "
            + "                                         AND e.location_id=:location AND e.encounter_type=${pharmacyEncounterTypeId}"
            + "                                         AND o.concept_id = ${returnVisitDateForDrugsConcept} "
            + "                                         AND e.voided = 0 "
            + "                                         AND o.voided = 0"
            + "                                         AND e.encounter_datetime = (SELECT e.encounter_datetime AS return_date "
            + "                                         FROM encounter e "
            + "                                                  INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                                         WHERE p.patient_id = e.patient_id "
            + "                                           AND e.voided = 0 "
            + "                                           AND o.voided = 0 "
            + "                                           AND e.encounter_type = ${pharmacyEncounterTypeId} "
            + "                                           AND e.location_id = :location "
            + "                                           AND e.encounter_datetime <= :endDate "
            + "                                         ORDER BY e.encounter_datetime DESC "
            + "                                         LIMIT 1) "
            + "                                         ORDER BY o.value_datetime DESC LIMIT 1) AS return_date_fila,  "
            + "                                        (SELECT o.value_datetime"
            + "                                        FROM encounter e JOIN obs o ON e.encounter_id=o.encounter_id"
            + "                                        WHERE p.patient_id=e.patient_id "
            + "                                         AND e.location_id=:location AND e.encounter_type IN(${adultoSequimentoEncounterTypeId},${arvPediatriaSeguimentoEncounterTypeId})"
            + "                                         AND o.concept_id = ${returnVisitDateConcept} "
            + "                                         AND e.voided = 0 "
            + "                                         AND o.voided = 0"
            + "                                         AND e.encounter_datetime = (SELECT e.encounter_datetime AS return_date "
            + "                                         FROM encounter e "
            + "                                                  INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                                         WHERE p.patient_id = e.patient_id "
            + "                                           AND e.voided = 0 "
            + "                                           AND o.voided = 0 "
            + "                                           AND e.encounter_type IN (${adultoSequimentoEncounterTypeId},${arvPediatriaSeguimentoEncounterTypeId}) "
            + "                                           AND e.location_id = :location "
            + "                                           AND e.encounter_datetime <= :endDate "
            + "                                         ORDER BY e.encounter_datetime DESC "
            + "                                         LIMIT 1) ORDER BY o.value_datetime DESC LIMIT 1) AS return_date_consulta,"
            + "                                         (SELECT DATE_ADD(o.value_datetime, INTERVAL 30 DAY) AS return_date "
            + "                                         FROM encounter e "
            + "                                                  INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                                         WHERE p.patient_id = e.patient_id "
            + "                                           AND e.voided = 0 "
            + "                                           AND o.voided = 0 "
            + "                                           AND e.encounter_type = ${artDatePickupConceptId} "
            + "                                           AND e.location_id = :location "
            + "                                           AND o.concept_id = ${masterCardDrugPickupEncounterTypeId} "
            + "                                           AND o.value_datetime <= :endDate "
            + "                                         ORDER BY o.value_datetime DESC "
            + "                                         LIMIT 1) AS return_date_master "
            + "                                 FROM patient p "
            + "                                 where p.voided=0) e "
            + "                           GROUP BY e.patient_id) lp ON e.patient_id = lp.patient_id "
            + "WHERE o.concept_id = ${reasonPatientMissedVisitConceptId} AND o.value_coded IN (${patientForgotVisitDateConceptId}, "
            + "${patientIsBedriddenAtHomeConceptId},${distanceOrMoneyForTransportIsToMuchForPatientConceptId}, "
            + "${patientIsDissatifiedWithDayHospitalServicesConceptId},${fearOfTheProviderConceptId}, "
            + "${absenceOfHealthProviderInHealthUnitConceptId},${patientDoesNotLikeArvTreatmentSideEffectsConceptId}, "
            + "${patientIsTreatingHivWithTraditionalMedicineConceptId},${otherReasonWhyPatientMissedVisitConceptId}) "
            + "AND e.encounter_type = ${homeVisitCardEncounterTypeId} "
            + "AND e.encounter_datetime BETWEEN lp.return_date and :endDate "
            + "AND e.location_id = :location "
            + "AND e.voided=0 "
            + "AND o.voided=0 ";
    StringSubstitutor sub = new StringSubstitutor(valuesMap);
    return sub.replace(query);
  }

  /**
   * <b>Description:</b> Untraced Patients Criteria 2 Patients without Patient Visit Card of type
   * busca and with a set of observations
   *
   * @param pharmacyEncounterTypeId
   * @param adultoSequimentoEncounterTypeId
   * @param arvPediatriaSeguimentoEncounterTypeId
   * @param returnVisitDateForDrugsConcept
   * @param returnVisitDateConcept
   * @param homeVisitCardEncounterTypeId
   * @param apoioReintegracaoParteAEncounterTypeId
   * @param apoioReintegracaoParteBEncounterTypeId
   * @param typeOfVisitConcept
   * @param buscaConcept
   * @param secondAttemptConcept
   * @param thirdAttemptConcept
   * @param patientFoundConcept
   * @param defaultingMotiveConcept
   * @param reportVisitConcept1
   * @param reportVisitConcept2
   * @param patientFoundForwardedConcept
   * @param reasonForNotFindingConcept
   * @param whoGaveInformationConcept
   * @param cardDeliveryDate
   * @param masterCardDrugPickupEncounterTypeId
   * @param artDatePickupConceptId
   * @return
   */
  public static String getPatientsWithVisitCardAndWithObs(
      int pharmacyEncounterTypeId,
      int adultoSequimentoEncounterTypeId,
      int arvPediatriaSeguimentoEncounterTypeId,
      int returnVisitDateForDrugsConcept,
      int returnVisitDateConcept,
      int homeVisitCardEncounterTypeId,
      int apoioReintegracaoParteAEncounterTypeId,
      int apoioReintegracaoParteBEncounterTypeId,
      int typeOfVisitConcept,
      int buscaConcept,
      int secondAttemptConcept,
      int thirdAttemptConcept,
      int patientFoundConcept,
      int defaultingMotiveConcept,
      int reportVisitConcept1,
      int reportVisitConcept2,
      int patientFoundForwardedConcept,
      int reasonForNotFindingConcept,
      int whoGaveInformationConcept,
      int cardDeliveryDate,
      int masterCardDrugPickupEncounterTypeId,
      int artDatePickupConceptId) {
    String query =
        "SELECT pa.patient_id "
            + "FROM   patient pa "
            + "INNER JOIN ("
            + "SELECT   patient_id,"
            + "                     Greatest(COALESCE(return_date_fila,0),COALESCE(return_date_consulta,0),COALESCE(return_date_master,0)) AS return_date"
            + "                FROM     ("
            + "                         SELECT p.patient_id,"
            + "                         ("
            + "                                SELECT   o.value_datetime"
            + "                                FROM     encounter e"
            + "                                JOIN     obs o"
            + "                                ON       e.encounter_id=o.encounter_id"
            + "                                WHERE    p.patient_id=e.patient_id"
            + "                                AND      e.location_id=:location"
            + "                                AND      e.encounter_type=${aRVPharmaciaEncounterType}"
            + "                                AND      o.concept_id = ${returnVisitDateForArvDrug}"
            + "                                AND      e.voided = 0"
            + "                                AND      o.voided = 0"
            + "                                AND      e.encounter_datetime ="
            + "                                         ("
            + "                                                    SELECT     e.encounter_datetime AS return_date"
            + "                                                    FROM       encounter e"
            + "                                                    INNER JOIN obs o"
            + "                                                    ON         e.encounter_id = o.encounter_id"
            + "                                                    WHERE      p.patient_id = e.patient_id"
            + "                                                    AND        e.voided = 0"
            + "                                                    AND        o.voided = 0"
            + "                                                    AND        e.encounter_type = ${aRVPharmaciaEncounterType}"
            + "                                                    AND        e.location_id = :location"
            + "                                                    AND        e.encounter_datetime <= :endDate"
            + "                                                    ORDER BY   e.encounter_datetime DESC limit 1)"
            + "                                ORDER BY o.value_datetime DESC limit 1) AS return_date_fila,"
            + "                                 ("
            + "                                SELECT   o.value_datetime"
            + "                                FROM     encounter e"
            + "                                JOIN     obs o"
            + "                                ON       e.encounter_id=o.encounter_id"
            + "                                WHERE    p.patient_id=e.patient_id"
            + "                                AND      e.location_id=:location"
            + "                                AND      e.encounter_type IN(${adultoSeguimentoEncounterType},"
            + "                                                             ${pediatriaSeguimentoEncounterType})"
            + "                                AND      o.concept_id = ${returnVisitDate}"
            + "                                AND      e.voided = 0"
            + "                                AND      o.voided = 0"
            + "                                AND      e.encounter_datetime ="
            + "                                         ("
            + "                                                    SELECT     e.encounter_datetime AS return_date"
            + "                                                    FROM       encounter e"
            + "                                                    INNER JOIN obs o"
            + "                                                    ON         e.encounter_id = o.encounter_id"
            + "                                                    WHERE      p.patient_id = e.patient_id"
            + "                                                    AND        e.voided = 0"
            + "                                                    AND        o.voided = 0"
            + "                                                    AND        e.encounter_type IN (${adultoSeguimentoEncounterType},"
            + "                                                                                    ${pediatriaSeguimentoEncounterType})"
            + "                                                    AND        e.location_id = :location"
            + "                                                    AND        e.encounter_datetime <= :endDate"
            + "                                                    ORDER BY   e.encounter_datetime DESC limit 1)"
            + "                                ORDER BY o.value_datetime DESC limit 1) AS return_date_consulta,"
            + "                                 ("
            + "                                  SELECT     date_add(o.value_datetime, interval 30 day) AS return_date"
            + "                                  FROM       encounter e"
            + "                                  INNER JOIN obs o"
            + "                                  ON         e.encounter_id = o.encounter_id"
            + "                                  WHERE      p.patient_id = e.patient_id"
            + "                                  AND        e.voided = 0"
            + "                                  AND        o.voided = 0"
            + "                                  AND        e.encounter_type = ${masterCardDrugPickupEncounterType} "
            + "                                  AND        e.location_id = :location"
            + "                                  AND        o.concept_id = ${artDatePickupMasterCard}"
            + "                                  AND        o.value_datetime <= :endDate"
            + "                                  ORDER BY   o.value_datetime DESC limit 1) AS return_date_master"
            + "                FROM   patient p"
            + "                WHERE  p.voided=0) e"
            + " GROUP BY e.patient_id) lp ON pa.patient_id=lp.patient_id "
            + "INNER JOIN encounter e ON "
            + "  pa.patient_id=e.patient_id AND "
            + "  e.encounter_datetime >= lp.return_date AND "
            + "  e.encounter_datetime <= :endDate AND "
            + "  e.encounter_type IN (${homeVisit}, ${apoioA}, ${apoioB}) AND "
            + "  e.location_id=:location "
            + "INNER JOIN obs visitType ON "
            + "  pa.patient_id=visitType.person_id AND "
            + "  visitType.encounter_id = e.encounter_id AND "
            + "  visitType.concept_id = ${typeVisit} AND "
            + "  visitType.value_coded = ${busca} AND "
            + "  visitType.obs_datetime <= :endDate "
            + "INNER JOIN obs o ON "
            + "  pa.patient_id=o.person_id AND "
            + "  o.encounter_id = e.encounter_id AND "
            + "  o.concept_id IN (${secondAttempt},"
            + "                   ${thirdAttempt},"
            + "                   ${patientFound},"
            + "                   ${defaultingMotive},"
            + "                   ${reportVisit1},"
            + "                   ${reportVisit2},"
            + "                   ${patientFoundForwarded},"
            + "                   ${reasonForNotFinding},"
            + "                   ${whoGaveInformation},"
            + "                   ${cardDeliveryDate} ) AND "
            + "  o.obs_datetime <= :endDate "
            + "GROUP BY pa.patient_id ";

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("returnVisitDateForArvDrug", returnVisitDateForDrugsConcept);
    valuesMap.put("aRVPharmaciaEncounterType", pharmacyEncounterTypeId);
    valuesMap.put("returnVisitDate", returnVisitDateConcept);
    valuesMap.put("adultoSeguimentoEncounterType", adultoSequimentoEncounterTypeId);
    valuesMap.put("pediatriaSeguimentoEncounterType", arvPediatriaSeguimentoEncounterTypeId);
    valuesMap.put("artDatePickupMasterCard", artDatePickupConceptId);
    valuesMap.put("masterCardDrugPickupEncounterType", masterCardDrugPickupEncounterTypeId);
    valuesMap.put("homeVisit", homeVisitCardEncounterTypeId);
    valuesMap.put("apoioA", apoioReintegracaoParteAEncounterTypeId);
    valuesMap.put("apoioB", apoioReintegracaoParteBEncounterTypeId);
    valuesMap.put("typeVisit", typeOfVisitConcept);
    valuesMap.put("busca", buscaConcept);
    valuesMap.put("secondAttempt", secondAttemptConcept);
    valuesMap.put("thirdAttempt", thirdAttemptConcept);
    valuesMap.put("patientFound", patientFoundConcept);
    valuesMap.put("defaultingMotive", defaultingMotiveConcept);
    valuesMap.put("reportVisit1", reportVisitConcept1);
    valuesMap.put("reportVisit2", reportVisitConcept2);
    valuesMap.put("patientFoundForwarded", patientFoundForwardedConcept);
    valuesMap.put("reasonForNotFinding", reasonForNotFindingConcept);
    valuesMap.put("whoGaveInformation", whoGaveInformationConcept);
    valuesMap.put("cardDeliveryDate", cardDeliveryDate);
    StringSubstitutor sub = new StringSubstitutor(valuesMap);
    return sub.replace(query);
  }

  /**
   * All Patients without “Patient Visit Card” (Encounter type 21 or 36 or 37) registered between ◦
   * the last scheduled appointment or drugs pick up (the most recent one) by reporting end date and
   * ◦ the reporting end date
   *
   * @return {@link String}
   */
  public static String
      getPatientsWithoutVisitCardRegisteredBtwnLastAppointmentOrDrugPickupAndEnddate(
          int pharmacyEncounterTypeId,
          int adultoSequimentoEncounterTypeId,
          int arvPediatriaSeguimentoEncounterTypeId,
          int returnVisitDateForDrugsConcept,
          int returnVisitDateConcept,
          int homeVisitCardEncounterTypeId,
          int apoioReintegracaoParteAEncounterTypeId,
          int apoioReintegracaoParteBEncounterTypeId,
          int masterCardDrugPickupEncounterTypeId,
          int artDatePickupConceptId) {
    Map<String, Integer> valuesMap = new HashMap<>();
    StringSubstitutor sub = new StringSubstitutor(valuesMap);
    valuesMap.put("returnVisitDateForArvDrug", returnVisitDateForDrugsConcept);
    valuesMap.put("aRVPharmaciaEncounterType", pharmacyEncounterTypeId);
    valuesMap.put("returnVisitDate", returnVisitDateConcept);
    valuesMap.put("adultoSeguimentoEncounterType", adultoSequimentoEncounterTypeId);
    valuesMap.put("pediatriaSeguimentoEncounterType", arvPediatriaSeguimentoEncounterTypeId);
    valuesMap.put("artDatePickupMasterCard", artDatePickupConceptId);
    valuesMap.put("masterCardDrugPickupEncounterType", masterCardDrugPickupEncounterTypeId);
    valuesMap.put("homeVisitCardEncounterTypeId", homeVisitCardEncounterTypeId);
    valuesMap.put("apoioReintegracaoParteAEncounterTypeId", apoioReintegracaoParteAEncounterTypeId);
    valuesMap.put("apoioReintegracaoParteBEncounterTypeId", apoioReintegracaoParteBEncounterTypeId);

    String query =
        " SELECT pa.patient_id FROM patient pa "
            + " WHERE pa.patient_id NOT IN ("
            + "  SELECT pa.patient_id "
            + "  FROM patient pa"
            + "  INNER JOIN encounter e ON pa.patient_id=e.patient_id "
            + "  INNER JOIN obs o ON pa.patient_id=o.person_id "
            + "  INNER JOIN ("
            + "                SELECT   patient_id,"
            + "                     Greatest(COALESCE(return_date_fila,0),COALESCE(return_date_consulta,0),COALESCE(return_date_master,0)) AS return_date"
            + "                FROM     ("
            + "                         SELECT p.patient_id,"
            + "                         ("
            + "                                SELECT   o.value_datetime"
            + "                                FROM     encounter e"
            + "                                JOIN     obs o"
            + "                                ON       e.encounter_id=o.encounter_id"
            + "                                WHERE    p.patient_id=e.patient_id"
            + "                                AND      e.location_id=:location"
            + "                                AND      e.encounter_type=${aRVPharmaciaEncounterType}"
            + "                                AND      o.concept_id = ${returnVisitDateForArvDrug}"
            + "                                AND      e.voided = 0"
            + "                                AND      o.voided = 0"
            + "                                AND      e.encounter_datetime ="
            + "                                         ("
            + "                                                    SELECT     e.encounter_datetime AS return_date"
            + "                                                    FROM       encounter e"
            + "                                                    INNER JOIN obs o"
            + "                                                    ON         e.encounter_id = o.encounter_id"
            + "                                                    WHERE      p.patient_id = e.patient_id"
            + "                                                    AND        e.voided = 0"
            + "                                                    AND        o.voided = 0"
            + "                                                    AND        e.encounter_type = ${aRVPharmaciaEncounterType}"
            + "                                                    AND        e.location_id = :location"
            + "                                                    AND        e.encounter_datetime <= :endDate"
            + "                                                    ORDER BY   e.encounter_datetime DESC limit 1)"
            + "                                ORDER BY o.value_datetime DESC limit 1) AS return_date_fila,"
            + "                                 ("
            + "                                SELECT   o.value_datetime"
            + "                                FROM     encounter e"
            + "                                JOIN     obs o"
            + "                                ON       e.encounter_id=o.encounter_id"
            + "                                WHERE    p.patient_id=e.patient_id"
            + "                                AND      e.location_id=:location"
            + "                                AND      e.encounter_type IN(${adultoSeguimentoEncounterType},"
            + "                                                             ${pediatriaSeguimentoEncounterType})"
            + "                                AND      o.concept_id = ${returnVisitDate}"
            + "                                AND      e.voided = 0"
            + "                                AND      o.voided = 0"
            + "                                AND      e.encounter_datetime ="
            + "                                         ("
            + "                                                    SELECT     e.encounter_datetime AS return_date"
            + "                                                    FROM       encounter e"
            + "                                                    INNER JOIN obs o"
            + "                                                    ON         e.encounter_id = o.encounter_id"
            + "                                                    WHERE      p.patient_id = e.patient_id"
            + "                                                    AND        e.voided = 0"
            + "                                                    AND        o.voided = 0"
            + "                                                    AND        e.encounter_type IN (${adultoSeguimentoEncounterType},"
            + "                                                                                    ${pediatriaSeguimentoEncounterType})"
            + "                                                    AND        e.location_id = :location"
            + "                                                    AND        e.encounter_datetime <= :endDate"
            + "                                                    ORDER BY   e.encounter_datetime DESC limit 1)"
            + "                                ORDER BY o.value_datetime DESC limit 1) AS return_date_consulta,"
            + "                                 ("
            + "                                  SELECT     date_add(o.value_datetime, interval 30 day) AS return_date"
            + "                                  FROM       encounter e"
            + "                                  INNER JOIN obs o"
            + "                                  ON         e.encounter_id = o.encounter_id"
            + "                                  WHERE      p.patient_id = e.patient_id"
            + "                                  AND        e.voided = 0"
            + "                                  AND        o.voided = 0"
            + "                                  AND        e.encounter_type = ${masterCardDrugPickupEncounterType} "
            + "                                  AND        e.location_id = :location"
            + "                                  AND        o.concept_id = ${artDatePickupMasterCard}"
            + "                                  AND        o.value_datetime <= :endDate"
            + "                                  ORDER BY   o.value_datetime DESC limit 1) AS return_date_master"
            + "                FROM   patient p"
            + "                WHERE  p.voided=0) e"
            + " GROUP BY e.patient_id)lp ON pa.patient_id=lp.patient_id "
            + "  WHERE e.encounter_datetime >= lp.return_date AND e.encounter_datetime<=:endDate"
            + "  AND e.encounter_type IN (${homeVisitCardEncounterTypeId},${apoioReintegracaoParteAEncounterTypeId}, ${apoioReintegracaoParteBEncounterTypeId}) "
            + "  AND e.location_id=:location  "
            + "  GROUP BY pa.patient_id"
            + ") "
            + " GROUP BY pa.patient_id";
    System.out.println(sub.replace(query));
    return sub.replace(query);
  }

  /**
   * <b>Description:</b> Get patients traced (Unable to locate) and Not Found
   *
   * @return {@link String}
   */
  public static String getPatientsTracedWithReasonNotFound(
      int pharmacyEncounterTypeId,
      int adultoSequimentoEncounterTypeId,
      int arvPediatriaSeguimentoEncounterTypeId,
      int returnVisitDateForDrugsConcept,
      int returnVisitDateConcept,
      int masterCardDrugPickupEncounterTypeId,
      int homeVisitCardEncounterTypeId,
      int apoioReintegracaoParteAEncounterTypeId,
      int apoioReintegracaoParteBEncounterTypeId,
      int typeOfVisitConcept,
      int buscaConcept,
      int reasonPatientNotFound,
      int defaultingMotiveConcept,
      int reasonForStoppedTakingArvDrugsDuringLast7DaysConcept,
      int reasonForStoppedTakingArvDrugsDuringLastMonthConcept,
      int mainReasonForDelayInTakingArvConcept,
      int patientRecordHasWrongAddressConcept,
      int patientMovedHousesConcept,
      int patientTookATripConcept,
      int otherReasonsWhyPatientWasNotLocatedByActivistConcept,
      int artDatePickupConceptId) {
    String query =
        "SELECT DISTINCT pa.patient_id FROM patient pa "
            + "    INNER JOIN ( "
            + "    SELECT patient_id, "
            + "                 GREATEST(COALESCE(return_date_fila,0),COALESCE(return_date_consulta,0),COALESCE(return_date_master,0)) AS return_date "
            + "                           FROM (SELECT p.patient_id, "
            + "                                        (SELECT o.value_datetime"
            + "                                        FROM encounter e JOIN obs o ON e.encounter_id=o.encounter_id"
            + "                                        WHERE p.patient_id=e.patient_id "
            + "                                         AND e.location_id=:location AND e.encounter_type=${pharmacyEncounterTypeId}"
            + "                                         AND o.concept_id = ${returnVisitDateForDrugsConcept} "
            + "                                         AND e.voided = 0 "
            + "                                         AND o.voided = 0"
            + "                                         AND e.encounter_datetime = (SELECT e.encounter_datetime AS return_date "
            + "                                         FROM encounter e "
            + "                                                  INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                                         WHERE p.patient_id = e.patient_id "
            + "                                           AND e.voided = 0 "
            + "                                           AND o.voided = 0 "
            + "                                           AND e.encounter_type = ${pharmacyEncounterTypeId} "
            + "                                           AND e.location_id = :location "
            + "                                           AND e.encounter_datetime <= :endDate "
            + "                                         ORDER BY e.encounter_datetime DESC "
            + "                                         LIMIT 1) "
            + "                                         ORDER BY o.value_datetime DESC LIMIT 1) AS return_date_fila,  "
            + "                                        (SELECT o.value_datetime"
            + "                                        FROM encounter e JOIN obs o ON e.encounter_id=o.encounter_id"
            + "                                        WHERE p.patient_id=e.patient_id "
            + "                                         AND e.location_id=:location AND e.encounter_type IN(${adultoSequimentoEncounterTypeId},${arvPediatriaSeguimentoEncounterTypeId})"
            + "                                         AND o.concept_id = ${returnVisitDateConcept} "
            + "                                         AND e.voided = 0 "
            + "                                         AND o.voided = 0"
            + "                                         AND e.encounter_datetime = (SELECT e.encounter_datetime AS return_date "
            + "                                         FROM encounter e "
            + "                                                  INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                                         WHERE p.patient_id = e.patient_id "
            + "                                           AND e.voided = 0 "
            + "                                           AND o.voided = 0 "
            + "                                           AND e.encounter_type IN (${adultoSequimentoEncounterTypeId},${arvPediatriaSeguimentoEncounterTypeId}) "
            + "                                           AND e.location_id = :location "
            + "                                           AND e.encounter_datetime <= :endDate "
            + "                                         ORDER BY e.encounter_datetime DESC "
            + "                                         LIMIT 1) ORDER BY o.value_datetime DESC LIMIT 1) AS return_date_consulta,"
            + "                                         (SELECT DATE_ADD(o.value_datetime, INTERVAL 30 DAY) AS return_date "
            + "                                         FROM encounter e "
            + "                                                  INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                                         WHERE p.patient_id = e.patient_id "
            + "                                           AND e.voided = 0 "
            + "                                           AND o.voided = 0 "
            + "                                           AND e.encounter_type = ${masterCardDrugPickupEncounterTypeId} "
            + "                                           AND e.location_id = :location "
            + "                                           AND o.concept_id =  ${artDatePickupConceptId} "
            + "                                           AND o.value_datetime <= :endDate "
            + "                                         ORDER BY o.value_datetime DESC "
            + "                                         LIMIT 1) AS return_date_master "
            + "                                 FROM patient p "
            + "                                 where p.voided=0) e "
            + "                           GROUP BY e.patient_id)lp ON pa.patient_id=lp.patient_id "
            + "    INNER JOIN encounter e ON "
            + "        pa.patient_id=e.patient_id AND "
            + "        e.encounter_type IN (${homeVisitCardEncounterTypeId}, ${apoioReintegracaoParteAEncounterTypeId}, ${apoioReintegracaoParteBEncounterTypeId}) AND "
            + "        e.location_id = :location AND "
            + "        e.encounter_datetime >= lp.return_date AND "
            + "        e.encounter_datetime <= :endDate "
            + "    INNER JOIN obs visitType ON "
            + "        pa.patient_id=visitType.person_id AND "
            + "        visitType.encounter_id=e.encounter_id AND "
            + "        (visitType.concept_id= ${typeOfVisitConcept} AND visitType.value_coded= ${buscaConcept}) "
            + "    LEFT JOIN obs patientNotFound ON "
            + "        pa.patient_id=patientNotFound.person_id AND "
            + "        patientNotFound.encounter_id=e.encounter_id AND "
            + "        (patientNotFound.concept_id IN (${reasonPatientNotFound}, ${defaultingMotiveConcept}, ${reasonForStoppedTakingArvDrugsDuringLast7DaysConcept},${reasonForStoppedTakingArvDrugsDuringLastMonthConcept}, ${mainReasonForDelayInTakingArvConcept})"
            + " AND patientNotFound.value_coded IN (${patientRecordHasWrongAddressConcept}, ${patientMovedHousesConcept}, ${patientTookATripConcept}, ${otherReasonsWhyPatientWasNotLocatedByActivistConcept})) "
            + " WHERE patientNotFound.obs_id IS NOT NULL "
            + " AND lp.patient_id NOT IN "
            + "             ("
            + "       SELECT  p.patient_id"
            + "               FROM patient p"
            + "       INNER JOIN encounter e ON e.patient_id = p.patient_id"
            + "       INNER JOIN obs visitType ON p.patient_id = visitType.person_id"
            + "               AND visitType.encounter_id = e.encounter_id AND"
            + "                  (visitType.concept_id= ${typeOfVisitConcept} AND visitType.value_coded= ${buscaConcept}) "
            + "        LEFT JOIN obs patientNotFound ON "
            + "                  p.patient_id=patientNotFound.person_id AND "
            + "                  patientNotFound.encounter_id=e.encounter_id AND "
            + "                  (patientNotFound.concept_id IN (${reasonPatientNotFound}, ${defaultingMotiveConcept}, ${reasonForStoppedTakingArvDrugsDuringLast7DaysConcept},${reasonForStoppedTakingArvDrugsDuringLastMonthConcept}, ${mainReasonForDelayInTakingArvConcept})"
            + "               AND patientNotFound.value_coded IN (${patientRecordHasWrongAddressConcept}, ${patientMovedHousesConcept}, ${patientTookATripConcept}, ${otherReasonsWhyPatientWasNotLocatedByActivistConcept})) "
            + "        WHERE "
            + "                   e.encounter_datetime BETWEEN lp.return_date AND :endDate"
            + "               AND patientNotFound.obs_id IS NULL)"
            + "     ORDER BY pa.patient_id ";

    Map<String, Integer> valuesMap = new HashMap<>();

    valuesMap.put("pharmacyEncounterTypeId", pharmacyEncounterTypeId);
    valuesMap.put("returnVisitDateForDrugsConcept", returnVisitDateForDrugsConcept);
    valuesMap.put("adultoSequimentoEncounterTypeId", adultoSequimentoEncounterTypeId);
    valuesMap.put("arvPediatriaSeguimentoEncounterTypeId", arvPediatriaSeguimentoEncounterTypeId);
    valuesMap.put("returnVisitDateConcept", returnVisitDateConcept);
    valuesMap.put("masterCardDrugPickupEncounterTypeId", masterCardDrugPickupEncounterTypeId);
    valuesMap.put("artDatePickupConceptId", artDatePickupConceptId);
    valuesMap.put("homeVisitCardEncounterTypeId", homeVisitCardEncounterTypeId);
    valuesMap.put("apoioReintegracaoParteAEncounterTypeId", apoioReintegracaoParteAEncounterTypeId);
    valuesMap.put("apoioReintegracaoParteBEncounterTypeId", apoioReintegracaoParteBEncounterTypeId);
    valuesMap.put("typeOfVisitConcept", typeOfVisitConcept);
    valuesMap.put("buscaConcept", buscaConcept);
    valuesMap.put("reasonPatientNotFound", reasonPatientNotFound);
    valuesMap.put("defaultingMotiveConcept", defaultingMotiveConcept);
    valuesMap.put(
        "reasonForStoppedTakingArvDrugsDuringLast7DaysConcept",
        reasonForStoppedTakingArvDrugsDuringLast7DaysConcept);
    valuesMap.put(
        "reasonForStoppedTakingArvDrugsDuringLastMonthConcept",
        reasonForStoppedTakingArvDrugsDuringLastMonthConcept);
    valuesMap.put("mainReasonForDelayInTakingArvConcept", mainReasonForDelayInTakingArvConcept);
    valuesMap.put("patientRecordHasWrongAddressConcept", patientRecordHasWrongAddressConcept);
    valuesMap.put("patientMovedHousesConcept", patientMovedHousesConcept);
    valuesMap.put("patientTookATripConcept", patientTookATripConcept);
    valuesMap.put(
        "otherReasonsWhyPatientWasNotLocatedByActivistConcept",
        otherReasonsWhyPatientWasNotLocatedByActivistConcept);

    StringSubstitutor sub = new StringSubstitutor(valuesMap);
    return sub.replace(query);
  }

  /**
   * <b>Description:</b> Get patients traced (Unable to locate) and Found
   *
   * @return {@link String}
   */
  public static String getPatientsTracedWithVisitCard(
      int pharmacyEncounterTypeId,
      int adultoSequimentoEncounterTypeId,
      int arvPediatriaSeguimentoEncounterTypeId,
      int returnVisitDateForDrugsConcept,
      int returnVisitDateConcept,
      int masterCardDrugPickupEncounterTypeId,
      int homeVisitCardEncounterTypeId,
      int apoioReintegracaoParteAEncounterTypeId,
      int apoioReintegracaoParteBEncounterTypeId,
      int typeOfVisitConcept,
      int buscaConcept,
      int patientFoundConcept,
      int patientFoundAnswerConcept,
      int artDatePickupConceptId) {
    String query =
        "SELECT DISTINCT pa.patient_id FROM patient pa "
            + "    INNER JOIN ( "
            + "    SELECT patient_id, "
            + "                 GREATEST(COALESCE(return_date_fila,0),COALESCE(return_date_consulta,0),COALESCE(return_date_master,0)) AS return_date "
            + "                           FROM (SELECT p.patient_id, "
            + "                                        (SELECT o.value_datetime"
            + "                                        FROM encounter e JOIN obs o ON e.encounter_id=o.encounter_id"
            + "                                        WHERE p.patient_id=e.patient_id "
            + "                                         AND e.location_id=:location AND e.encounter_type=${pharmacyEncounterTypeId}"
            + "                                         AND o.concept_id = ${returnVisitDateForDrugsConcept} "
            + "                                         AND e.voided = 0 "
            + "                                         AND o.voided = 0"
            + "                                         AND e.encounter_datetime = (SELECT e.encounter_datetime AS return_date "
            + "                                         FROM encounter e "
            + "                                                  INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                                         WHERE p.patient_id = e.patient_id "
            + "                                           AND e.voided = 0 "
            + "                                           AND o.voided = 0 "
            + "                                           AND e.encounter_type = ${pharmacyEncounterTypeId} "
            + "                                           AND e.location_id = :location "
            + "                                           AND e.encounter_datetime <= :endDate "
            + "                                         ORDER BY e.encounter_datetime DESC "
            + "                                         LIMIT 1) "
            + "                                         ORDER BY o.value_datetime DESC LIMIT 1) AS return_date_fila,  "
            + "                                        (SELECT o.value_datetime"
            + "                                        FROM encounter e JOIN obs o ON e.encounter_id=o.encounter_id"
            + "                                        WHERE p.patient_id=e.patient_id "
            + "                                         AND e.location_id=:location AND e.encounter_type IN(${adultoSequimentoEncounterTypeId},${arvPediatriaSeguimentoEncounterTypeId})"
            + "                                         AND o.concept_id = ${returnVisitDateConcept} "
            + "                                         AND e.voided = 0 "
            + "                                         AND o.voided = 0"
            + "                                         AND e.encounter_datetime = (SELECT e.encounter_datetime AS return_date "
            + "                                         FROM encounter e "
            + "                                                  INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                                         WHERE p.patient_id = e.patient_id "
            + "                                           AND e.voided = 0 "
            + "                                           AND o.voided = 0 "
            + "                                           AND e.encounter_type IN (${adultoSequimentoEncounterTypeId},${arvPediatriaSeguimentoEncounterTypeId}) "
            + "                                           AND e.location_id = :location "
            + "                                           AND e.encounter_datetime <= :endDate "
            + "                                         ORDER BY e.encounter_datetime DESC "
            + "                                         LIMIT 1) ORDER BY o.value_datetime DESC LIMIT 1) AS return_date_consulta,"
            + "                                         (SELECT DATE_ADD(o.value_datetime, INTERVAL 30 DAY) AS return_date "
            + "                                         FROM encounter e "
            + "                                                  INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                                         WHERE p.patient_id = e.patient_id "
            + "                                           AND e.voided = 0 "
            + "                                           AND o.voided = 0 "
            + "                                           AND e.encounter_type = ${masterCardDrugPickupEncounterTypeId} "
            + "                                           AND e.location_id = :location "
            + "                                           AND o.concept_id = ${artDatePickupConceptId} "
            + "                                           AND o.value_datetime <= :endDate "
            + "                                         ORDER BY o.value_datetime DESC "
            + "                                         LIMIT 1) AS return_date_master "
            + "                                 FROM patient p "
            + "                                 where p.voided=0) e "
            + "                           GROUP BY e.patient_id)lp ON pa.patient_id=lp.patient_id "
            + "    INNER JOIN encounter e ON "
            + "        pa.patient_id=e.patient_id AND "
            + "        e.encounter_type IN (${homeVisitCardEncounterTypeId}, ${apoioReintegracaoParteAEncounterTypeId}, ${apoioReintegracaoParteBEncounterTypeId}) AND "
            + "        e.location_id = :location AND "
            + "        e.encounter_datetime >= lp.return_date AND "
            + "        e.encounter_datetime <= :endDate "
            + "    INNER JOIN obs visitType ON "
            + "        pa.patient_id=visitType.person_id AND "
            + "        visitType.encounter_id=e.encounter_id AND "
            + "        (visitType.concept_id= ${typeOfVisitConcept} AND visitType.value_coded= ${buscaConcept}) "
            + "    LEFT JOIN obs patientNotFound ON "
            + "        pa.patient_id=patientNotFound.person_id AND "
            + "        patientNotFound.encounter_id=e.encounter_id AND "
            + "        (patientNotFound.concept_id= ${patientFoundConcept} AND patientNotFound.value_coded= ${patientFoundAnswerConcept}) "
            + " WHERE patientNotFound.obs_id IS NOT NULL "
            + " ORDER BY pa.patient_id ";

    Map<String, Integer> valuesMap = new HashMap<>();

    valuesMap.put("pharmacyEncounterTypeId", pharmacyEncounterTypeId);
    valuesMap.put("returnVisitDateForDrugsConcept", returnVisitDateForDrugsConcept);
    valuesMap.put("adultoSequimentoEncounterTypeId", adultoSequimentoEncounterTypeId);
    valuesMap.put("arvPediatriaSeguimentoEncounterTypeId", arvPediatriaSeguimentoEncounterTypeId);
    valuesMap.put("returnVisitDateConcept", returnVisitDateConcept);
    valuesMap.put("masterCardDrugPickupEncounterTypeId", masterCardDrugPickupEncounterTypeId);
    valuesMap.put("artDatePickupConceptId", artDatePickupConceptId);
    valuesMap.put("homeVisitCardEncounterTypeId", homeVisitCardEncounterTypeId);
    valuesMap.put("apoioReintegracaoParteAEncounterTypeId", apoioReintegracaoParteAEncounterTypeId);
    valuesMap.put("apoioReintegracaoParteBEncounterTypeId", apoioReintegracaoParteBEncounterTypeId);
    valuesMap.put("typeOfVisitConcept", typeOfVisitConcept);
    valuesMap.put("buscaConcept", buscaConcept);
    valuesMap.put("patientFoundConcept", patientFoundConcept);
    valuesMap.put("patientFoundAnswerConcept", patientFoundAnswerConcept);

    StringSubstitutor sub = new StringSubstitutor(valuesMap);
    return sub.replace(query);
  }

  /**
   *
   * <li>All patients who do not have the next scheduled drug pick up date on their last drug
   *     pick-up (FILA) that occurred during the reporting period nor any ART pickup date registered
   *     on Ficha Recepção – Levantou ARVs or FILA during the reporting period.
   *
   * @return {@link String}
   */
  public static String getPatientWithoutScheduledDrugPickupDateMasterCardAndArtPickupQuery(
      EncounterType aRVPharmaciaEncounterType,
      EncounterType masterCardDrugPickupEncounterType,
      Concept returnVisitDateForArvDrugConcept,
      Concept artDatePickup) {
    Map<String, Integer> map = new HashMap<>();
    map.put("18", aRVPharmaciaEncounterType.getEncounterTypeId());
    map.put("5096", returnVisitDateForArvDrugConcept.getConceptId());
    map.put("52", masterCardDrugPickupEncounterType.getEncounterTypeId());
    map.put("23866", artDatePickup.getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       LEFT JOIN(SELECT e.patient_id "
            + "                 FROM   encounter e "
            + "                 WHERE  e.encounter_type = ${18} "
            + "                        AND e.encounter_datetime >= :startDate "
            + "                        AND e.encounter_datetime <= :endDate "
            + "                        AND e.voided = 0 "
            + "                        AND e.location_id = :location "
            + "                 GROUP  BY e.patient_id) fila ON fila.patient_id = p.patient_id "
            + "       LEFT JOIN("
            + "                   SELECT e.patient_id "
            + "                   FROM encounter e "
            + "                   INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                   INNER JOIN ("
            + "                   SELECT e.patient_id, MAX(e.encounter_datetime) encounter_date "
            + "                 FROM   encounter e "
            + "                 WHERE  e.encounter_type = ${18} "
            + "                        AND e.encounter_datetime >= :startDate "
            + "                        AND e.encounter_datetime <= :endDate "
            + "                        AND e.voided = 0 "
            + "                        AND e.location_id = :location "
            + "                 GROUP  BY e.patient_id ) recent_fila ON recent_fila.patient_id = e.patient_id "
            + "                   WHERE e.encounter_type = ${18} "
            + "                   AND e.encounter_datetime = recent_fila.encounter_date"
            + "                   AND o.concept_id = ${5096} "
            + "                   AND o.value_datetime IS NOT NULL"
            + ") next ON next.patient_id = p.patient_id "
            + "       LEFT JOIN(SELECT e.patient_id "
            + "                 FROM   encounter e "
            + "                        INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                 WHERE  e.encounter_type = ${52} "
            + "                        AND o.concept_id = ${23866} "
            + "                        AND o.value_datetime >= :startDate "
            + "                        AND o.value_datetime <= :endDate "
            + "                AND o.voided = 0                  "
            + "                        AND e.voided = 0 "
            + "                        AND e.location_id = :location "
            + "                 GROUP  BY e.patient_id) arv_pickup ON arv_pickup.patient_id = p.patient_id "
            + "WHERE fila.patient_id IS NOT NULL AND arv_pickup.patient_id IS NULL  AND  next.patient_id IS NULL "
            + "        AND p.voided = 0                  "
            + "GROUP  BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    return stringSubstitutor.replace(query);
  }

  /**
   * <b>Description:</b> Number of patients based on the program and state in the program. by
   * reporting end date <b>ps.start_date <=: onOrBefore</b>
   *
   * @param program - program id
   * @param deadState - Dead state
   * @return String
   */
  public static String getPatientsListBasedOnProgramAndStateByReportingEndDate(
      int program, int deadState) {
    Map<String, Integer> map = new HashMap<>();
    map.put("program", program);
    map.put("deadState", deadState);
    String query =
        "select patient_id "
            + "from ("
            + " select p.patient_id, Max(ps.start_date) "
            + " from patient p "
            + " inner join patient_program pg on p.patient_id=pg.patient_id "
            + " inner join patient_state ps on pg.patient_program_id=ps.patient_program_id "
            + " where pg.voided=0 "
            + " and ps.voided=0 "
            + " and p.voided=0 "
            + " and pg.program_id=${program} "
            + " and ps.state = ${deadState} "
            + " and ps.start_date<=:onOrBefore "
            + " and pg.location_id=:location "
            + " group by p.patient_id  "
            + "       ) last_state";
    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    return stringSubstitutor.replace(query);
  }

  /**
   *
   * <li>All patients with the most recent date between next scheduled drug pickup date (FILA) and
   *     30 days after last ART pickup date (Ficha Recepção – Levantou ARVs) and adding 28 days and
   *     this date >=report start date and < reporting end date
   *
   * @return {@link String}
   */
  public static String getPatientHavingLastScheduledDrugPickupDateQuery(
      Concept returnVisitDateForArvDrugConcept,
      EncounterType ARVPharmaciaEncounterType,
      Concept returnVisitDateConcept,
      Concept artDatePickup,
      EncounterType masterCardDrugPickupEncounterType,
      int numDays) {

    Map<String, Integer> map = new HashMap<>();
    map.put("returnVisitDateForArvDrugConcept", returnVisitDateForArvDrugConcept.getConceptId());
    map.put("ARVPharmaciaEncounterType", ARVPharmaciaEncounterType.getEncounterTypeId());
    map.put("returnVisitDateConcept", returnVisitDateConcept.getConceptId());
    map.put("artDatePickup", artDatePickup.getConceptId());
    map.put(
        "masterCardDrugPickupEncounterType",
        masterCardDrugPickupEncounterType.getEncounterTypeId());
    map.put("numDays", numDays);

    String query =
        "SELECT final.patient_id "
            + "            from( "
            + "                SELECT "
            + "                    most_recent.patient_id, Date_add(Max(most_recent.value_datetime), interval ${numDays} day) final_encounter_date "
            + "                FROM   (SELECT fila.patient_id, o.value_datetime from ( "
            + "                            SELECT pa.patient_id, "
            + "                                Max(enc.encounter_datetime)  encounter_datetime "
            + "                            FROM   patient pa "
            + "                                inner join encounter enc "
            + "                                    ON enc.patient_id =  pa.patient_id "
            + "                            WHERE  pa.voided = 0 "
            + "                                AND enc.voided = 0 "
            + "								AND enc.encounter_type = ${ARVPharmaciaEncounterType} "
            + "                                AND enc.location_id = :location "
            + "                                AND enc.encounter_datetime <= :endDate "
            + "                            GROUP  BY pa.patient_id) fila "
            + "                        INNER JOIN encounter e on "
            + "                            e.patient_id = fila.patient_id and "
            + "                            e.encounter_datetime = fila.encounter_datetime and "
            + "                            e.encounter_type =  ${ARVPharmaciaEncounterType} and "
            + "                            e.location_id = :location and "
            + "                            e.voided = 0 and "
            + "                            e.encounter_datetime <= :endDate "
            + "					INNER JOIN obs o on "
            + "                            o.encounter_id = e.encounter_id and "
            + "                            o.concept_id = ${returnVisitDateForArvDrugConcept} and "
            + "                            o.voided = 0 "
            + "                        UNION "
            + "                        SELECT pa.patient_id, "
            + "                            Date_add(Max(obs.value_datetime), interval 30 day) value_datetime "
            + "                        FROM   patient pa "
            + "                            inner join encounter enc "
            + "                                ON enc.patient_id = pa.patient_id "
            + "                            inner join obs obs "
            + "                                ON obs.encounter_id = enc.encounter_id "
            + "                        WHERE  pa.voided = 0 "
            + "                            AND enc.voided = 0 "
            + "                            AND obs.voided = 0 "
            + "                            AND obs.concept_id = ${artDatePickup} "
            + "                            AND obs.value_datetime IS NOT NULL "
            + "                            AND enc.encounter_type = ${masterCardDrugPickupEncounterType}  "
            + "                            AND enc.location_id = :location "
            + "                            AND obs.value_datetime <= :endDate "
            + "                       GROUP  BY pa.patient_id "
            + "                   ) most_recent "
            + "               GROUP BY most_recent.patient_id "
            + "               HAVING final_encounter_date >= :startDate AND final_encounter_date < :endDate "
            + "            ) final ";
    return new StringSubstitutor(map).replace(query);
  }

  /**
   *
   * <li>On Treatment for <3 months when experienced IIT All patients who have been on treatment for
   *     less than 90 days since the date initiated ARV treatment (TX_ML_FR4) to the date of their
   *     last scheduled ARV pick-up
   * <li>On Treatment for 3-5 months when experienced IIT All patients who have been on treatment
   *     for greater or equal than 90 days and less than 180 days since the date initiated ARV
   *     treatment (TX_ML_FR4) to the date of their last scheduled ARV pick-up
   * <li>On Treatment for >=6 months when experienced IIT All patients who have been on treatment
   *     for greater or equal than 180 days since the date initiated ARV treatment (TX_ML_FR4) to
   *     the date of their last scheduled ARV pick-up
   *
   * @see CommonQueries#getARTStartDate(boolean)
   * @param aRVPharmaciaEncounterType Arv Pharmacia EncounterType
   * @param returnVisitDateForArvDrugConcept Return Visit Date Concept
   * @param masterCardDrugPickupEncounterType Recepcao Levantou ARV EncounterType
   * @param artDatePickupMasterCard Arv Pickup Date Concept
   * @param minDays minimum of days of interruption
   * @param maxDays maximum of days of interruption
   * @return {@link String}
   */
  public static String getTreatmentInterruptionOfXDaysBeforeReturningToTreatmentQuery(
      EncounterType aRVPharmaciaEncounterType,
      Concept returnVisitDateForArvDrugConcept,
      EncounterType masterCardDrugPickupEncounterType,
      Concept artDatePickupMasterCard,
      Integer minDays,
      Integer maxDays) {

    CommonQueries commonQueries = new CommonQueries(new CommonMetadata(), new HivMetadata());

    String query =
        "SELECT patient_id FROM ( "
            + "      SELECT start_art.patient_id, start_art.first_pickup "
            + " FROM ( "
            + commonQueries.getARTStartDate(true)
            + "       ) start_art "
            + " INNER JOIN ( "
            + " SELECT "
            + "                                 most_recent.patient_id, Max(most_recent.value_datetime) final_encounter_date "
            + "                             FROM   (SELECT fila.patient_id, o.value_datetime from ( "
            + "                                         SELECT pa.patient_id, "
            + "                                             Max(enc.encounter_datetime)  encounter_datetime "
            + "                                         FROM   patient pa "
            + "                                             inner join encounter enc "
            + "                                                 ON enc.patient_id =  pa.patient_id "
            + "                                         WHERE  pa.voided = 0 "
            + "                                             AND enc.voided = 0 "
            + "             								AND enc.encounter_type = ${18} "
            + "                                             AND enc.location_id = :location "
            + "                                             AND enc.encounter_datetime <= :endDate "
            + "                                         GROUP  BY pa.patient_id) fila "
            + "                                     INNER JOIN encounter e on "
            + "                                         e.patient_id = fila.patient_id and "
            + "                                         e.encounter_datetime = fila.encounter_datetime and "
            + "                                         e.encounter_type =  ${18} and "
            + "                                         e.location_id = :location and "
            + "                                         e.voided = 0 and "
            + "                                         e.encounter_datetime <= :endDate "
            + "             					INNER JOIN obs o on "
            + "                                         o.encounter_id = e.encounter_id and "
            + "                                         o.concept_id = ${5096} and "
            + "                                         o.voided = 0 "
            + "                                     UNION "
            + "                                     SELECT pa.patient_id, "
            + "                                         Date_add(Max(obs.value_datetime), interval 30 day) value_datetime "
            + "                                     FROM   patient pa "
            + "                                         inner join encounter enc "
            + "                                             ON enc.patient_id = pa.patient_id "
            + "                                         inner join obs obs "
            + "                                             ON obs.encounter_id = enc.encounter_id "
            + "                                     WHERE  pa.voided = 0 "
            + "                                         AND enc.voided = 0 "
            + "                                         AND obs.voided = 0 "
            + "                                         AND obs.concept_id = ${23866} "
            + "                                         AND obs.value_datetime IS NOT NULL "
            + "                                         AND enc.encounter_type = ${52} "
            + "                                         AND enc.location_id = :location "
            + "                                         AND obs.value_datetime <= :endDate "
            + "                                    GROUP  BY pa.patient_id "
            + "                                ) most_recent "
            + "                            GROUP BY most_recent.patient_id "
            + "                       ) last_pickup "
            + "     on last_pickup.patient_id = start_art.patient_id ";

    if (minDays == null && maxDays != null) {
      query +=
          "    WHERE  TIMESTAMPDIFF(day, start_art.first_pickup, last_pickup.final_encounter_date) < ${maxDays} ";
    } else if (minDays != null && maxDays == null) {
      query +=
          "    WHERE  TIMESTAMPDIFF(day, start_art.first_pickup, last_pickup.final_encounter_date) >= ${minDays} ";
    } else {
      query +=
          "    WHERE  TIMESTAMPDIFF(day, start_art.first_pickup, last_pickup.final_encounter_date) >= ${minDays} "
              + " AND TIMESTAMPDIFF(day, start_art.first_pickup, last_pickup.final_encounter_date) < ${maxDays} ";
    }
    query += " ) as final";

    Map<String, Integer> map = new HashMap<>();
    map.put("5096", returnVisitDateForArvDrugConcept.getConceptId());
    map.put("18", aRVPharmaciaEncounterType.getEncounterTypeId());
    map.put("23866", artDatePickupMasterCard.getConceptId());
    map.put("52", masterCardDrugPickupEncounterType.getEncounterTypeId());
    map.put("minDays", minDays);
    map.put("maxDays", maxDays);

    StringSubstitutor sub = new StringSubstitutor(map);
    return sub.replace(query);
  }
}
