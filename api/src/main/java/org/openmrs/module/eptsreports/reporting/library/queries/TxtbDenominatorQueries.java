package org.openmrs.module.eptsreports.reporting.library.queries;

import org.apache.commons.text.StringSubstitutor;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TxtbDenominatorQueries {

  @Autowired private HivMetadata hivMetadata;

  @Autowired private TbMetadata tbMetadata;

  /**
   * i. at least one “S” or “N” selected for TB Screening (Rastreio de TB) during the reporting
   * period consultations; (response 1065: YES or 1066: NO for question 6257: SCREENING FOR TB)
   *
   * <p>at least one “POS” selected for “Resultado da Investigação para TB de BK e/ou RX?” during
   * the reporting period consultations; ( response 703: POS or 664: NEG for question: 6277)
   *
   * @return {@link String}
   */
  public String getPatientAndScreeningDate() {
    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("6257", tbMetadata.getTbScreeningConcept().getConceptId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    String query =
        "SELECT p.patient_id, "
            + "       e.encounter_datetime "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  e.encounter_type IN( ${6}, ${9} ) "
            + "       AND e.location_id = :location_id "
            + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "       AND o.concept_id = ${6257} "
            + "       AND o.value_coded IN ( ${1065}, ${1066} ) "
            + "       AND p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    return sb.replace(query);
  }
  /**
   * Filter ART_List to Tx_Tb_list all patients who during the reporting period had in “Patient
   * Clinical Record of ART - Ficha de Seguimento (adults and children) and Master Card Ficha
   * Clinica: encounter types 6 and 9
   *
   * <p>at least one “POS” selected for “Resultado da Investigação para TB de BK e/ou RX?” during
   * the reporting period consultations; ( response 703: POS or 664: NEG for question: 6277)
   *
   * @return {@link String}
   */
  public String getPatientWithAtLeastOnePosDate() {
    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("6277", tbMetadata.getResearchResultConcept().getConceptId());
    map.put("703", tbMetadata.getPositiveConcept().getConceptId());
    map.put("664", tbMetadata.getNegativeConcept().getConceptId());

    String query =
        "SELECT p.patient_id, e.encounter_datetime "
            + "FROM   patient p "
            + "       inner join encounter e ON e.patient_id = p.patient_id "
            + "       inner join obs o ON o.encounter_id = e.encounter_id "
            + "WHERE  e.encounter_type IN( ${6}, ${9} ) "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_datetime BETWEEN :startDate AND : endDate "
            + "       AND o.concept_id = ${6277} "
            + "       AND o.value_coded IN ( ${703}, ${664} ) "
            + "       AND p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    return sb.replace(query);
  }

  /**
   * Filter ART_List to Tx_Tb_list all patients who during the reporting period had in “Patient
   * Clinical Record of ART - Ficha de Seguimento (adults and children) and Master Card Ficha
   * Clinica: encounter types 6 and 9
   *
   * <p>at least TB Treatment (Tratamento de TB) start date within the reporting period; Concept:
   * 1113
   *
   * @return {@link String}
   */
  public String getPatientWithAtLeastTbTreatmentDate() {
    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("1113", tbMetadata.getTBDrugTreatmentStartDate().getConceptId());

    String query =
        "SELECT p.patient_id, o.value_datetime "
            + "FROM   patient p "
            + "       INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "WHERE  e.encounter_type IN( ${6}, ${9} ) "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "       AND o.concept_id = ${1113} "
            + "       AND p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    return sb.replace(query);
  }

  /**
   * Filter ART_List to Tx_Tb_list all patients who during the reporting period had in “Patient
   * Clinical Record of ART - Ficha de Seguimento (adults and children) and Master Card Ficha
   * Clinica: encounter types 6 and 9
   *
   * <p>TB Program Enrollment (id 5) date within the reporting period.
   *
   * @return {@link String}
   */
  public String getPatientWithTbProgramEnrollmentAndDate() {
    Map<String, Integer> map = new HashMap<>();
    map.put("5", tbMetadata.getTBProgram().getProgramId());
    String query =
        "SELECT pg.patient_id,pg.date_enrolled "
            + "FROM   patient p "
            + "       INNER JOIN patient_program pg "
            + "               ON pg.patient_id = p.patient_id "
            + "WHERE  pg.voided = 0 "
            + "       AND p.voided = 0 "
            + "       AND program_id = ${5} "
            + "       AND date_enrolled <= BETWEEN :startDate AND :endDate "
            + "       AND pg.location_id = :location "
            + "GROUP  BY pg.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    return sb.replace(query);
  }

  /**
   * Patients with Pulmonary TB Date in Patient Clinical Record of ART date TB (Condicoes medicas
   * importantes – Ficha Resumo Mastercard during reporting period
   *
   * <p>• Encounter Type ID = 53<br>
   * • Concept ID for Pulmonary TB = 421406 <br>
   * • Answer = Yes (value_coded 106542) <br>
   * • Obs_datetime >= startDate and <=endDate
   *
   * @return {@link String}
   */
  public String getPatientWithPulmonaryTbdDate() {
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1406", hivMetadata.getOtherDiagnosis().getConceptId());
    map.put("42", tbMetadata.getPulmonaryTB().getConceptId());
    String query =
        "SELECT p.patient_id,o.obs_datetime "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  e.encounter_type = ${53} "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "       AND o.concept_id = ${1406} "
            + "       AND o.value_coded = ${42} "
            + "       AND p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    return sb.replace(query);
  }

  /**
   * Patients marked as “Tratamento TB= Inicio (I) ” in Ficha Clinica Master Card
   *
   * <p>• Encounter Type ID = 6 <br>
   * • TB Treament Plan ID = 1268 <br>
   * • Answer = START (value_coded 1256) <br>
   * • value_datetime >= startDate and <=endDate <br>
   *
   * @return {@link String}
   */
  public String getPatientMarkedAsTbTreatmentStartAndDate() {
    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1268", hivMetadata.getTBTreatmentPlanConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    String query =
        "SELECT p.patient_id,o.value_datetime "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  e.encounter_type = ${6} "
            + "       AND e.location_id = :location "
            + "       AND o.value_datetime BETWEEN :startDate AND :endDate "
            + "       AND o.concept_id = ${1268} "
            + "       AND o.value_coded = ${1256} "
            + "       AND p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    return sb.replace(query);
  }

  /**
   * Patients with at least one response to the following questions in in Ficha Clinica Master Card
   * during the reporting period
   *
   * <p>• Encounter Type ID = 6 <br>
   * • TUBERCULOSIS SYMPTOMS (concept_id = 23758) Answers YES (id = 1065) or NO (id = 1066)
   *
   * @return {@link String}
   */
  public String getPatientWithTuberculosisSymptomsAndDate() {
    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("23758", hivMetadata.getTBSymptomsConcept().getConceptId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    String query =
        "SELECT p.patient_id,e.encounter_datetime "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  e.encounter_type = ${6} "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "       AND  o.concept_id = ${23758} "
            + "       AND o.value_coded IN( ${1065}, ${1066} )  "
            + "       AND p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    return sb.replace(query);
  }

  /**
   * Patients with at least one response to the following questions in in Ficha Clinica Master Card
   * during the reporting period
   *
   * <p>• Encounter Type ID = 6 <br>
   * ACTIVE TUBERCULOSIS (obs concept_id = 23761) Answer YES (id = 1065)
   *
   * @return {@link String}
   */
  public String getPatientsActiveTuberculosisDate() {
    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("23761", hivMetadata.getActiveTBConcept().getConceptId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());

    String query =
        "SELECT p.patient_id,e.encounter_datetime "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  e.encounter_type = ${6} "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "       AND  o.concept_id = ${23761} "
            + "       AND o.value_coded IN( ${1065} )  "
            + "       AND p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    return sb.replace(query);
  }

  /**
   * Patients with at least one response to the following questions in in Ficha Clinica Master Card
   * during the reporting period
   *
   * <p>• TB OBSERVATIONS (obs concept id = 1766) Answers: <br>
   * a. FEVER LASTING MORE THAN 3 WEEKS (id = 1763) or <br>
   * b. WEIGHT LOSS OF MORE THAN 3 KG IN LAST MONTH (id = 1764) or <br>
   * c. NIGHTSWEATS LASTING MORE THAN 3 WEEKS ( id = 1762) or <br>
   * d. COUGH LASTING MORE THAN 3 WEEKS ( id = 1760) or <br>
   * e. ASTHENIA ( id =23760) or <br>
   * f. COHABITANT BEING TREATED FOR TB (id = 1765) or <br>
   * g. LYMPHADENOPATHY (id = 161)
   *
   * @return {@link String}
   */
  public String getPatientsWithTbObservationsAndDate() {
    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1766", tbMetadata.getObservationTB().getConceptId());
    map.put("1763", tbMetadata.getFeverLastingMoraThan3Weeks().getConceptId());
    map.put("1764", tbMetadata.getWeightLossOfMoreThan3KgInLastMonth().getConceptId());
    map.put("1762", tbMetadata.getNightsWeatsLastingMoraThan3Weeks().getConceptId());
    map.put("1760", tbMetadata.getCoughLastingMoraThan3Weeks().getConceptId());
    map.put("23760", tbMetadata.getAsthenia().getConceptId());
    map.put("1765", tbMetadata.getCohabitantBeingTreatedForTB().getConceptId());
    map.put("161", tbMetadata.getLymphadenopathy().getConceptId());

    String query =
        "SELECT p.patient_id,e.encounter_datetime "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  e.encounter_type = ${6} "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "       AND ( o.concept_id = ${1766} "
            + "             AND o.value_coded IN( ${1763}, ${1764}, ${1762}, ${1760},${23760}, ${1765}, ${161} ) ) "
            + "       AND p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    return sb.replace(query);
  }
}
