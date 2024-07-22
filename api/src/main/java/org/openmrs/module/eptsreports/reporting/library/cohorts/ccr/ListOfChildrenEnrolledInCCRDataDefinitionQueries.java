package org.openmrs.module.eptsreports.reporting.library.cohorts.ccr;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.library.queries.CommonQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsQueriesUtil;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListOfChildrenEnrolledInCCRDataDefinitionQueries {

  private final HivMetadata hivMetadata;
  private final CommonMetadata commonMetadata;
  private final CommonQueries commonQueries;

  @Autowired
  public ListOfChildrenEnrolledInCCRDataDefinitionQueries(
      HivMetadata hivMetadata, CommonMetadata commonMetadata, CommonQueries commonQueries) {
    this.hivMetadata = hivMetadata;
    this.commonMetadata = commonMetadata;
    this.commonQueries = commonQueries;
  }

  /**
   * <b> List of Children enrolled in CCR </b>
   * <li>All children who have a CCR NID registered and are enrolled in CCR in Program Enrollment
   *     with admission date (Data de admissão) during the reporting period
   * <li>All children who have a CCR: Ficha Resumo with Data de abertura do processo during the
   *     reporting period.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getListOfChildrenEnrolledInCCR() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("List of Children enrolled in CCR");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("92", hivMetadata.getCCRResumoEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getCCRProgram().getProgramId());

    String query =
        new EptsQueriesUtil().patientIdQueryBuilder(getChildrenEnrolledInCCRQuery()).getQuery();
    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));
    return sqlCohortDefinition;
  }

  /**
   * <b>Idade do Utente no fim do Período de Reporte</b>
   * <li>For children with birth date information registered in the system, the age of the patient
   *     should be calculated as the child’s age at the reporting end date in years and months
   *
   *     <p>Age (years)
   *
   *     <p>Age (months) Note: Age should be expressed as total years in columns D and the remaining
   *     months in column E
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getPatientAgeInYearsOrMonths(Boolean ageOrRemainingMonths) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Idade do Utente no fim do Período de Reporte");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));

    String query =
        " SELECT pr.person_id, "
                .concat(
                    ageOrRemainingMonths
                        ? " ages.age AS age "
                        : " ABS(TIMESTAMPDIFF(MONTH, DATE_ADD(pr.birthdate, INTERVAL ages.age YEAR), :endDate)) AS remaining_months ")
            + "FROM   person pr "
            + "       INNER JOIN (SELECT p.patient_id, "
            + "                          FLOOR(DATEDIFF(:endDate, ps.birthdate) / 365) AS "
            + "                          age "
            + "                   FROM   patient p "
            + "                          INNER JOIN person ps "
            + "                                  ON p.patient_id = ps.person_id "
            + "                   WHERE  p.voided = 0 "
            + "                          AND ps.voided = 0) ages "
            + "               ON ages.patient_id = pr.person_id "
            + "                  AND pr.voided = 0";

    sqlPatientDataDefinition.setQuery(query);
    return sqlPatientDataDefinition;
  }

  /**
   * <b>CCR Enrollment Date (Data Inscrição na CCR)</b>
   * <li>All children who have a CCR NID registered and are enrolled in CCR in Program Enrollment
   *     with admission date (Data de admissão) during the reporting period
   * <li>All children who have a CCR: Ficha Resumo with Data de abertura do processo during the
   *     reporting period.
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getCCREnrollmentDate() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("CCR Enrollment Date (Data Inscrição na CCR)");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("92", hivMetadata.getCCRResumoEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getCCRProgram().getProgramId());

    String query = new EptsQueriesUtil().min(getChildrenEnrolledInCCRQuery()).getQuery();
    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));
    return sqlPatientDataDefinition;
  }

  /**
   * <b>Reason for visit (Motivo da consulta) Columns K – U </b>
   * <li>All children who have a CCR NID registered and are enrolled in CCR in Program Enrollment
   *     with admission date (Data de admissão) during the reporting period
   * <li>All children who have a CCR: Ficha Resumo with Data de abertura do processo during the
   *     reporting period.
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getReasonForVisitOnCCREnrollmentDate(Concept reasonConcept) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Reason for visit (Motivo da consulta) Columns K – U ");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("92", hivMetadata.getCCRResumoEncounterType().getEncounterTypeId());
    map.put("1874", commonMetadata.getMotivoConsultaCriancaRiscoConcept().getConceptId());
    map.put("reasonConcept", reasonConcept.getConceptId());

    String query =
        " SELECT p.patient_id, o.value_coded "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON p.patient_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + "       INNER JOIN ( "
            + getCCRResumoEnrollmentDateQuery()
            + ")ccr_enrollment "
            + "               ON ccr_enrollment.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${92} "
            + "       AND o.concept_id = ${1874} "
            + "       AND o.value_coded = ${reasonConcept} "
            + "       AND e.encounter_datetime = ccr_enrollment.enrollment_date "
            + "GROUP  BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));
    return sqlPatientDataDefinition;
  }

  /**
   * <b>Mother’s PTV Code (Código PTV) – CCR: Ficha Resumo
   *
   * <p>The number registered in the Exposição ao HIV, código PTV field on CCR: Ficha Resumo
   * (obtained in CCR1_FR6)
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getPtvCode() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Mother’s PTV Code (Código PTV) – CCR: Ficha Resumo");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("92", hivMetadata.getCCRResumoEncounterType().getEncounterTypeId());
    map.put("1874", commonMetadata.getMotivoConsultaCriancaRiscoConcept().getConceptId());
    map.put("1586", commonMetadata.getRecenNascidoMaeHivPositivoConcept().getConceptId());

    String query =
        " SELECT p.patient_id, o2.comments AS ptv_code "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON p.patient_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + "       INNER JOIN obs o2 "
            + "               ON e.encounter_id = o2.encounter_id "
            + "       INNER JOIN ( "
            + getCCRResumoEnrollmentDateQuery()
            + " )ccr_enrollment "
            + "               ON ccr_enrollment.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND o2.voided = 0 "
            + "       AND e.encounter_type = ${92} "
            + "       AND (o.concept_id = ${1874} "
            + "         AND o.value_coded = ${1586}) "
            + "         AND (o2.concept_id = ${1586} "
            + "         AND o2.comments IS NOT NULL) "
            + "       AND e.encounter_datetime = ccr_enrollment.enrollment_date "
            + "GROUP  BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));
    return sqlPatientDataDefinition;
  }

  /**
   * <b>Mother’s Name (Nome da mãe) – CCR: Ficha Resumo
   *
   * <p>The name registered in the Nome da mãe field on CCR: Ficha Resumo (obtained in CCR1_FR6)
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getMothersName() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Mother’s Name (Nome da mãe) – CCR: Ficha Resumo");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("92", hivMetadata.getCCRResumoEncounterType().getEncounterTypeId());
    map.put("1477", commonMetadata.getMothersNameConcept().getConceptId());

    String query =
        " SELECT p.patient_id, o.value_text AS mother_name "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON p.patient_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + "       INNER JOIN ( "
            + getCCRResumoEnrollmentDateQuery()
            + " )ccr_enrollment "
            + "               ON ccr_enrollment.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${92} "
            + "       AND o.concept_id = ${1477} "
            + "         AND o.value_text IS NOT NULL "
            + "       AND e.encounter_datetime = ccr_enrollment.enrollment_date "
            + "GROUP  BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));
    return sqlPatientDataDefinition;
  }

  /**
   * <b>Mother’s TARV NID (NID TARV da mãe) – relacionamento em SESP
   *
   * <p>The NID registered in the Relacionamentos Existentes do Paciente: Mãe field on CCR: Ficha
   * Resumo (obtained in CCR1_FR6)
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getMothersNID() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Mother’s TARV NID (NID TARV da mãe)");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("92", hivMetadata.getCCRResumoEncounterType().getEncounterTypeId());
    map.put("1477", hivMetadata.getNidServiceTarvIdentifierType().getPatientIdentifierTypeId());
    map.put("14", hivMetadata.getMotherToSonRelationshipType().getRelationshipTypeId());
    map.put("2", hivMetadata.getNidServiceTarvIdentifierType().getPatientIdentifierTypeId());

    String query =
        "SELECT mother.patient_id, pi.identifier  "
            + "  FROM patient p INNER JOIN patient_identifier pi "
            + "                       ON p.patient_id=pi.patient_id "
            + "               INNER JOIN patient_identifier_type pit "
            + "                       ON pit.patient_identifier_type_id=pi.identifier_type "
            + "               INNER JOIN ( "
            + getPatientAndMotherIdsQuery()
            + " ) mother ON mother.mother_id = p.patient_id "
            + "WHERE p.voided=0 "
            + "  AND pi.voided=0 "
            + "  AND pit.retired=0 "
            + "  AND pi.preferred = 1 "
            + "  AND pit.patient_identifier_type_id = ${2} "
            + "GROUP BY p.patient_id "
            + "UNION "
            + "SELECT mother.patient_id, pi.identifier  "
            + "  FROM patient p INNER JOIN patient_identifier pi "
            + "                       ON p.patient_id=pi.patient_id "
            + "               INNER JOIN patient_identifier_type pit "
            + "                       ON pit.patient_identifier_type_id=pi.identifier_type "
            + "               INNER JOIN ( "
            + getPatientAndMotherIdsQuery()
            + " ) mother ON mother.mother_id = p.patient_id "
            + "WHERE p.voided=0 "
            + "  AND pi.voided=0 "
            + "  AND pit.retired=0 "
            + "  AND pit.patient_identifier_type_id = ${2} "
            + "GROUP BY p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));
    return sqlPatientDataDefinition;
  }

  /**
   * <b>Patient’s consent for home visits on CCR: Ficha Resumo
   *
   * <p>The response registered in the Visita Domiciliar field (Sim ou Não) on CCR: Ficha Resumo
   * (obtained in CCR1_FR6).
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getHomeVisitConsent() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Aceita Visita Domiciliar");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("92", hivMetadata.getCCRResumoEncounterType().getEncounterTypeId());
    map.put("2071", commonMetadata.getAcceptsHomeVisitConcept().getConceptId());
    map.put("1065", commonMetadata.getYesConcept().getConceptId());
    map.put("1066", commonMetadata.getNoConcept().getConceptId());

    String query =
        " SELECT p.patient_id, o.value_coded AS response "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON p.patient_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + "       INNER JOIN ( "
            + getCCRResumoEnrollmentDateQuery()
            + " )ccr_enrollment "
            + "               ON ccr_enrollment.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${92} "
            + "       AND o.concept_id = ${2071} "
            + "         AND o.value_coded IN (${1065}, ${1066}) "
            + "       AND e.encounter_datetime = ccr_enrollment.enrollment_date "
            + "GROUP  BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));
    return sqlPatientDataDefinition;
  }

  /**
   * <b>First CCR Consultation Date /
   * <li>Earliest CCR: Ficha Seguimento consultation date (Data da consulta) registered on or after
   *     the CCR enrollment date (CCR1_FR6) and during the reporting period
   * <li>Most recent CCR: Ficha Seguimento consultation date (Data da consulta) registered on or
   *     after the CCR enrollment date (CCR1_FR6) and during the reporting period
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getFirstCCRSeguimentoDate(Boolean firstOrLastConsultation) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Patient’s first CCR Seguimento Consultation date");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getCCRProgram().getProgramId());
    map.put("92", hivMetadata.getCCRResumoEncounterType().getEncounterTypeId());
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());

    String query =
        " SELECT p.patient_id, "
                .concat(
                    firstOrLastConsultation
                        ? " MIN(e.encounter_datetime) AS consultation_date "
                        : "MAX(e.encounter_datetime) AS consultation_date ")
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON p.patient_id = e.patient_id "
            + "       INNER JOIN ( "
            + getChildrenEnrolledInCCRQuery()
            + " )ccr_enrollment "
            + "               ON ccr_enrollment.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND e.encounter_type = ${93} "
            + "       AND e.encounter_datetime >= ccr_enrollment.enrollment_date "
            + "       AND e.encounter_datetime <= :endDate "
            + "GROUP  BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));
    return sqlPatientDataDefinition;
  }

  /**
   * <b>Next Scheduled CCR Consultation Date </b>
   *
   * <p>Next consultation date (Data da consulta seguinte) registered on the most recent CCR: Ficha
   * Seguimento consultation date (Column AB) registered on or after the CCR enrollment date
   * (CCR1_FR6) and during the reporting period
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getNextScheduledCCRConsultation() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();

    sqlPatientDataDefinition.setName("Next Scheduled CCR Consultation Date");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getCCRProgram().getProgramId());
    map.put("92", hivMetadata.getCCRResumoEncounterType().getEncounterTypeId());
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("1410", hivMetadata.getReturnVisitDateConcept().getConceptId());

    String query =
        " SELECT p.patient_id, o.value_datetime AS next_consultation_date "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON p.patient_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + "       INNER JOIN ( "
            + " SELECT p.patient_id, MAX(e.encounter_datetime) AS consultation_date "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON p.patient_id = e.patient_id "
            + "       INNER JOIN ( "
            + getChildrenEnrolledInCCRQuery()
            + " )ccr_enrollment "
            + "               ON ccr_enrollment.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND e.encounter_type = ${93} "
            + "       AND e.encounter_datetime >= ccr_enrollment.enrollment_date "
            + "       AND e.encounter_datetime <= :endDate "
            + "GROUP  BY p.patient_id"
            + " )last_ccr "
            + "               ON last_ccr.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${93} "
            + "       AND e.encounter_datetime = last_ccr.consultation_date "
            + "       AND o.concept_id = ${1410} "
            + "       AND o.value_datetime IS NOT NULL "
            + "GROUP  BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));
    return sqlPatientDataDefinition;
  }

  /**
   * <b>Date of Most Recent PCR Test Result – Sheet 1: Column AD</b>
   * <li>Most recent PCR test date registered on Ficha Lab Geral (data de resultado) or CCR: Ficha
   *     Seguimento (consultation date for the consultation that has PCR- resultado registered).
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getMostRecentPCRTestDate() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();

    sqlPatientDataDefinition.setName("Date of Most Recent PCR Test Result");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("1030", hivMetadata.getHivPCRQualitativeConceptUuid().getConceptId());
    String query = getPatientPCRResultDateQuery();
    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));
    return sqlPatientDataDefinition;
  }

  /**
   * <b>Sample Collection Type for Most Recent PCR Test Result – Sheet 1: Column AE</b>
   * <li>Most recent PCR sample collection type registered on the Ficha Lab Geral (Tipo de colheita)
   *     for the Most Recent Result Date Registered (Column AD).
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getSampleCollectionType() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();

    sqlPatientDataDefinition.setName("Sample Collection Type for Most Recent PCR Test Result");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("1030", hivMetadata.getHivPCRQualitativeConceptUuid().getConceptId());
    map.put("23832", hivMetadata.getSampleTypeConcept().getConceptId());

    // TODO: Investigate the types of samples available
    //  for concept_id 23832 and update the converter answers

    String query =
        "SELECT p.patient_id, o.value_coded AS sample_type "
            + "FROM patient p "
            + "         JOIN encounter e "
            + "              ON p.patient_id = e.patient_id "
            + "         JOIN obs o "
            + "              ON e.encounter_id = o.encounter_id "
            + "         JOIN ( "
            + getPatientPCRResultDateQuery()
            + " ) last_pcr ON last_pcr.patient_id = p.patient_id "
            + "WHERE p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND e.location_id = :location "
            + "  AND e.encounter_type = ${13} "
            + "  AND e.encounter_datetime = last_pcr.most_recent "
            + "  AND o.concept_id = ${23832} "
            + "  AND o.value_coded IS NOT NULL "
            + "GROUP BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));
    return sqlPatientDataDefinition;
  }

  /**
   * <b>Most Recent PCR Test Result – Sheet 1: Column AF</b>
   * <li>Most recent test result (Postivo, Negativo ou No Result) for the Most Recent Result Date
   *     Registered (Column AD).
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getMostRecentPCRTestResult() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();

    sqlPatientDataDefinition.setName("Most Recent PCR Test Result");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("1030", hivMetadata.getHivPCRQualitativeConceptUuid().getConceptId());

    String query =
        " SELECT p.patient_id, "
            + "       o.value_coded AS pcr_result "
            + "FROM patient p "
            + "         JOIN encounter e "
            + "              ON p.patient_id = e.patient_id "
            + "         JOIN obs o "
            + "              ON e.encounter_id = o.encounter_id "
            + "         JOIN ( "
            + getPatientPCRResultDateQuery()
            + " ) last_pcr ON last_pcr.patient_id = p.patient_id "
            + "WHERE p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND e.location_id = :location "
            + "  AND e.encounter_type = ${13} "
            + "  AND e.encounter_datetime = last_pcr.most_recent "
            + "  AND o.concept_id = ${1030} "
            + "  AND o.value_coded IS NOT NULL "
            + "GROUP BY p.patient_id "
            + "UNION "
            + " SELECT p.patient_id, "
            + "       o.value_coded AS pcr_result "
            + "FROM patient p "
            + "         JOIN encounter e "
            + "              ON p.patient_id = e.patient_id "
            + "         JOIN obs o "
            + "              ON e.encounter_id = o.encounter_id "
            + "         JOIN ( "
            + getPatientPCRResultDateQuery()
            + " ) last_pcr ON last_pcr.patient_id = p.patient_id "
            + "WHERE p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND e.location_id = :location "
            + "  AND e.encounter_type = ${93} "
            + "  AND e.encounter_datetime = last_pcr.most_recent "
            + "  AND o.concept_id = ${1030} "
            + "  AND o.value_coded IS NOT NULL "
            + "GROUP BY p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));
    return sqlPatientDataDefinition;
  }

  /**
   * <b>Date of Penultimate PCR Test Result– Sheet 1: Column AG</b>
   * <li>Penultimate PCR test date registered on Ficha Lab Geral (Data de resultado) or encounter
   *     date of CCR: Ficha Seguimento with (PCR- resultado)
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getPenultimatePCRTestDate() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();

    sqlPatientDataDefinition.setName("Date of Penultimate PCR Test Result");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("1030", hivMetadata.getHivPCRQualitativeConceptUuid().getConceptId());
    String query = getPenultimatePCRResultDateQuery();
    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));
    return sqlPatientDataDefinition;
  }

  /**
   * <b>Sample Collection Type for Penultimate PCR Test Result – Sheet 1: Column AH</b>
   * <li>Most recent PCR sample collection type registered on the Ficha Lab Geral (Tipo de colheita)
   *     for the Most Recent Result Date Registered (Column AD).
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getPenultimateSampleCollectionType() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();

    sqlPatientDataDefinition.setName("Sample Collection Type for Penultimate PCR Test Result ");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("1030", hivMetadata.getHivPCRQualitativeConceptUuid().getConceptId());
    map.put("23832", hivMetadata.getSampleTypeConcept().getConceptId());

    // TODO: Investigate the types of samples available
    //  for concept_id 23832 and update the converter answers

    String query =
        "SELECT p.patient_id, o.value_coded AS sample_type "
            + "FROM patient p "
            + "         JOIN encounter e "
            + "              ON p.patient_id = e.patient_id "
            + "         JOIN obs o "
            + "              ON e.encounter_id = o.encounter_id "
            + "         JOIN ( "
            + getPenultimatePCRResultDateQuery()
            + " ) pen_pcr ON pen_pcr.patient_id = p.patient_id "
            + "WHERE p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND e.location_id = :location "
            + "  AND e.encounter_type = ${13} "
            + "  AND e.encounter_datetime = pen_pcr.most_recent "
            + "  AND o.concept_id = ${23832} "
            + "  AND o.value_coded IS NOT NULL "
            + "GROUP BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));
    return sqlPatientDataDefinition;
  }

  /**
   * <b>Most Recent PCR Test Result – Sheet 1: Column AF</b>
   * <li>Most recent test result (Postivo, Negativo ou No Result) for the Most Recent Result Date
   *     Registered (Column AD).
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getPenultimatePCRTestResult() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();

    sqlPatientDataDefinition.setName("Most Recent PCR Test Result");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("1030", hivMetadata.getHivPCRQualitativeConceptUuid().getConceptId());

    String query =
        " SELECT p.patient_id, "
            + "       o.value_coded AS pcr_result "
            + "FROM patient p "
            + "         JOIN encounter e "
            + "              ON p.patient_id = e.patient_id "
            + "         JOIN obs o "
            + "              ON e.encounter_id = o.encounter_id "
            + "         JOIN ( "
            + getPenultimatePCRResultDateQuery()
            + " ) pen_pcr ON pen_pcr.patient_id = p.patient_id "
            + "WHERE p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND e.location_id = :location "
            + "  AND e.encounter_type = ${13} "
            + "  AND e.encounter_datetime = pen_pcr.most_recent "
            + "  AND o.concept_id = ${1030} "
            + "  AND o.value_coded IS NOT NULL "
            + "GROUP BY p.patient_id "
            + "UNION "
            + " SELECT p.patient_id, "
            + "       o.value_coded AS pcr_result "
            + "FROM patient p "
            + "         JOIN encounter e "
            + "              ON p.patient_id = e.patient_id "
            + "         JOIN obs o "
            + "              ON e.encounter_id = o.encounter_id "
            + "         JOIN ( "
            + getPenultimatePCRResultDateQuery()
            + " ) pen_pcr ON pen_pcr.patient_id = p.patient_id "
            + "WHERE p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND e.location_id = :location "
            + "  AND e.encounter_type = ${93} "
            + "  AND e.encounter_datetime = pen_pcr.most_recent "
            + "  AND o.concept_id = ${1030} "
            + "  AND o.value_coded IS NOT NULL "
            + "GROUP BY p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));
    return sqlPatientDataDefinition;
  }

  /**
   * <b>Date of Most Recent HIV Rapid Test Result – Sheet 1: Column AJ</b>
   * <li>Consultation Date (Data de consulta) registered on the most recent CCR: Ficha Seguimento
   *     consultation date with an HIV rapid test result recorded
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getPatientHivRapidTestDate() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();

    sqlPatientDataDefinition.setName("Date of Most Recent HIV Rapid Test Result");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("1040", hivMetadata.getHivRapidTest1QualitativeConcept().getConceptId());

    String query =
        " SELECT p.patient_id, "
            + "                          MAX(e.encounter_datetime) AS hiv_test_date "
            + "                   FROM   patient p "
            + "                          INNER JOIN encounter e "
            + "                                  ON p.patient_id = e.patient_id "
            + "                          INNER JOIN obs o "
            + "                                  ON o.encounter_id = e.encounter_id "
            + "                   WHERE  p.voided = 0 "
            + "                          AND e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.encounter_type = ${93} "
            + "                          AND e.encounter_datetime <= :endDate "
            + "                          AND o.concept_id = ${1040} "
            + "                          AND o.value_coded IS NOT NULL "
            + "                   GROUP  BY p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));
    return sqlPatientDataDefinition;
  }

  /**
   * <b>Most Recent HIV Rapid Test Result – Sheet 1: Column AK</b>
   * <li>Most recent test result (Postivo, Negativo ou Indeterminado) for the Most Recent Result
   *     Date Registered (Column AJ).
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getPatientHivRapidTestResult() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();

    sqlPatientDataDefinition.setName("Most Recent HIV Rapid Test Result");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("1040", hivMetadata.getHivRapidTest1QualitativeConcept().getConceptId());

    String query =
        " SELECT p.patient_id, o.value_coded AS hiv_test_result "
            + "                   FROM   patient p "
            + "                          INNER JOIN encounter e "
            + "                                  ON p.patient_id = e.patient_id "
            + "                          INNER JOIN obs o "
            + "                                  ON o.encounter_id = e.encounter_id "
            + "                          INNER JOIN ( "
            + "                                SELECT p.patient_id, "
            + "                                       MAX(e.encounter_datetime) AS hiv_test_date "
            + "                                FROM   patient p "
            + "                                       INNER JOIN encounter e "
            + "                                               ON p.patient_id = e.patient_id "
            + "                                       INNER JOIN obs o "
            + "                                               ON o.encounter_id = e.encounter_id "
            + "                                WHERE  p.voided = 0 "
            + "                                       AND e.voided = 0 "
            + "                                       AND o.voided = 0 "
            + "                                       AND e.encounter_type = ${93} "
            + "                                       AND e.encounter_datetime <= :endDate "
            + "                                       AND o.concept_id = ${1040} "
            + "                                       AND o.value_coded IS NOT NULL "
            + "                                GROUP  BY p.patient_id "
            + "                         ) hiv_test ON hiv_test.patient_id = p.patient_id "
            + "                   WHERE  p.voided = 0 "
            + "                          AND e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.encounter_type = ${93} "
            + "                          AND e.encounter_datetime = hiv_test.hiv_test_date "
            + "                          AND o.concept_id = ${1040} "
            + "                          AND o.value_coded IS NOT NULL "
            + "                   GROUP  BY p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));
    return sqlPatientDataDefinition;
  }

  /**
   * <b>CCR: Programa – Sheet 1: Column AL</b>
   * <li>Most recent state registered for CCR Program in Program Enrollment Module
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getPatientLastStateOfStayOnProgramEnrollment() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();

    sqlPatientDataDefinition.setName("Most Recent CCR: Program in Program Enrollment");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getCCRProgram().getProgramId());

    String query =
        "SELECT p.patient_id , ps.state AS state  "
            + "    FROM patient p   "
            + "        INNER JOIN patient_program pg   "
            + "            ON p.patient_id=pg.patient_id   "
            + "        INNER JOIN patient_state ps   "
            + "            ON pg.patient_program_id=ps.patient_program_id   "
            + "        INNER JOIN ( "
            + "               SELECT p.patient_id, MAX(ps.start_date) AS state_date  "
            + "               FROM patient p   "
            + "                   INNER JOIN patient_program pg   "
            + "                       ON p.patient_id=pg.patient_id   "
            + "                   INNER JOIN patient_state ps   "
            + "                       ON pg.patient_program_id=ps.patient_program_id   "
            + "               WHERE pg.voided = 0   "
            + "                   AND ps.voided = 0   "
            + "                   AND p.voided = 0   "
            + "                   AND pg.program_id = ${6}  "
            + "                   AND ps.state IS NOT NULL "
            + "                   AND ps.start_date <= :endDate   "
            + "                   AND pg.location_id = :location   "
            + "               GROUP BY p.patient_id  "
            + "        ) last_state ON last_state.patient_id = p.patient_id "
            + "    WHERE pg.voided=0   "
            + "        AND ps.voided=0   "
            + "        AND p.voided=0   "
            + "        AND pg.program_id = ${6}  "
            + "        AND ps.state IS NOT NULL "
            + "        AND ps.start_date = last_state.state_date   "
            + "        AND pg.location_id = :location   "
            + "         GROUP BY p.patient_id  ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));
    return sqlPatientDataDefinition;
  }

  /**
   * <b>CCR: Ficha Resumo - Sheet 1: Column AM / CCR: Ficha de Seguimento – Sheet 1: Column AN </b>
   * <li>Most recent state registered in Alta field on CCR: Ficha Resumo / Most recent state
   *     registered in Alta field on CCR: Ficha Seguimento
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getPatientLastStateOfStayOnCCRResumoOrSeguimento(
      Boolean isResumoOrSeguimento) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();

    sqlPatientDataDefinition.setName("Most Recent Patient state on CCR: Ficha Resumo / Seguimento");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("92", hivMetadata.getCCRResumoEncounterType().getEncounterTypeId());
    map.put("93", hivMetadata.getCCRSeguimentoEncounterType().getEncounterTypeId());
    map.put("1366", hivMetadata.getPatientHasDiedConcept().getConceptId());
    map.put("1873", hivMetadata.getTipoDeAltaConcept().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1707", hivMetadata.getAbandonedConcept().getConceptId());
    map.put("165483", hivMetadata.getTransferidoParaSectorTbConcept().getConceptId());
    map.put("165484", hivMetadata.getTransferidoParaConsultasIntegradasConcept().getConceptId());
    map.put("165485", hivMetadata.getTransferidoParaConsultaDeCriancaSadiaConcept().getConceptId());

    String query =
        "SELECT p.patient_id, "
            + "       o.value_coded AS state "
            + "FROM patient p "
            + "         JOIN encounter e "
            + "              ON p.patient_id = e.patient_id "
            + "         JOIN obs o "
            + "              ON e.encounter_id = o.encounter_id "
            + "         JOIN ( "
            + "    SELECT p.patient_id, "
            + "           Max(o.obs_datetime) AS state_date "
            + "    FROM patient p "
            + "             JOIN encounter e "
            + "                  ON p.patient_id = e.patient_id "
            + "             JOIN obs o "
            + "                  ON e.encounter_id = o.encounter_id "
            + "    WHERE p.voided = 0 "
            + "      AND e.voided = 0 "
            + "      AND o.voided = 0 "
            + "      AND e.location_id = :location "
            + "      AND e.encounter_type = ".concat(isResumoOrSeguimento ? " ${92} " : " ${93} ")
            + "      AND e.encounter_datetime <= :endDate "
            + "      AND o.concept_id = ${1873} "
            + "      AND o.value_coded IN (${165484}, ${1706}, ${165485}, ${165483}, ${1707}, ${1366}) "
            + "    GROUP BY p.patient_id "
            + ") last_state ON last_state.patient_id = p.patient_id "
            + "WHERE p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND e.location_id = :location "
            + "  AND e.encounter_type = ".concat(isResumoOrSeguimento ? " ${92} " : " ${93} ")
            + "  AND o.obs_datetime = last_state.state_date "
            + "  AND o.concept_id = ${1873} "
            + "  AND o.value_coded IN (${165484}, ${1706}, ${165485}, ${165483}, ${1707}, ${1366}) "
            + "GROUP BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));
    return sqlPatientDataDefinition;
  }

  /**
   * <b>ART Start Date-child (Data Início TARV-Criança) (Sheet 1: Column AO)</b>
   * <li>Patient’s first drugs pick-up date set in Pharmacy form (FILA) or
   * <li>Date that patient started drugs (ARV PLAN = START DRUGS) during the pharmacy or clinical
   *     visits or
   * <li>Patient’s first historical start drugs date set in Pharmacy Tool (FILA) or Clinical tools
   *     (Ficha de Seguimento Adulto and Ficha de Seguimento Pediatria) or Ficha Resumo - Master
   *     Card or
   * <li>Date that Patient was enrolled in ART Program, or
   * <li>Patient’s first drug pick-up date set on Recepção Levantou ARV – Master Card
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getChildArtStartDate() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("ART Start Date-child (Data Início TARV-Criança)");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));

    sqlPatientDataDefinition.setQuery(commonQueries.getARTStartDate(true));

    return sqlPatientDataDefinition;
  }

  private String getCCRResumoEnrollmentDateQuery() {
    return "SELECT p.patient_id, "
        + "                          Min(e.encounter_datetime) AS enrollment_date "
        + "                   FROM   patient p "
        + "                          INNER JOIN encounter e "
        + "                                  ON p.patient_id = e.patient_id "
        + "                   WHERE  p.voided = 0 "
        + "                          AND e.voided = 0 "
        + "                          AND e.encounter_type = ${92} "
        + "                          AND e.encounter_datetime >= :startDate "
        + "                          AND e.encounter_datetime <= :endDate "
        + "                   GROUP  BY p.patient_id ";
  }

  private String getChildrenEnrolledInCCRQuery() {
    return "SELECT p.patient_id, "
        + "       Min(pp.date_enrolled) AS enrollment_date "
        + "FROM   patient p "
        + "           INNER JOIN patient_program pp "
        + "                      ON p.patient_id = pp.patient_id "
        + "WHERE  p.voided = 0 "
        + "  AND pp.voided = 0 "
        + "  AND pp.program_id = ${6} "
        + "  AND pp.date_enrolled >= :startDate "
        + "  AND pp.date_enrolled <= :endDate "
        + "GROUP  BY p.patient_id "
        + "UNION "
        + "SELECT p.patient_id, "
        + "       Min(e.encounter_datetime) AS enrollment_date "
        + "FROM   patient p "
        + "           INNER JOIN encounter e "
        + "                      ON p.patient_id = e.patient_id "
        + "WHERE  p.voided = 0 "
        + "  AND e.voided = 0 "
        + "  AND e.encounter_type = ${92} "
        + "  AND e.encounter_datetime >= :startDate "
        + "  AND e.encounter_datetime <= :endDate "
        + "GROUP  BY p.patient_id";
  }

  private String getPatientAndMotherIdsQuery() {
    return " SELECT p.patient_id, r.person_a as mother_id "
        + "FROM   patient p "
        + "       INNER JOIN encounter e "
        + "               ON p.patient_id = e.patient_id "
        + "       INNER JOIN patient_identifier pi "
        + "               ON p.patient_id = pi.patient_id "
        + "       INNER JOIN relationship r "
        + "               ON p.patient_id = r.person_b "
        + "       INNER JOIN ( "
        + getCCRResumoEnrollmentDateQuery()
        + " )ccr_enrollment "
        + "               ON ccr_enrollment.patient_id = p.patient_id "
        + "WHERE  p.voided = 0 "
        + "       AND e.voided = 0 "
        + "       AND pi.voided = 0 "
        + "       AND r.voided = 0 "
        + "       AND e.encounter_type = ${92} "
        + "       AND e.encounter_datetime = ccr_enrollment.enrollment_date "
        + "       AND r.relationship = ${14} "
        + "       AND pi.identifier_type = ${2} "
        + "GROUP  BY p.patient_id ";
  }

  /**
   *
   * <li>Most recent PCR test date registered on Ficha Lab Geral (data de resultado) or CCR: Ficha
   *     Seguimento (consultation date for the consultation that has PCR- resultado registered).
   *
   * @return {@link String}
   */
  private String getPatientPCRResultDateQuery() {
    return " SELECT pcr.patient_id, MAX(pcr.pcr_date) AS most_recent "
        + " FROM  ( SELECT p.patient_id, "
        + "       MAX(e.encounter_datetime) AS pcr_date "
        + "FROM patient p "
        + "         JOIN encounter e "
        + "              ON p.patient_id = e.patient_id "
        + "         JOIN obs o "
        + "              ON e.encounter_id = o.encounter_id "
        + "WHERE p.voided = 0 "
        + "  AND e.voided = 0 "
        + "  AND o.voided = 0 "
        + "  AND e.location_id = :location "
        + "  AND e.encounter_type = ${13} "
        + "  AND e.encounter_datetime <= :endDate "
        + "  AND o.concept_id = ${1030} "
        + "  AND o.value_coded IS NOT NULL "
        + "GROUP BY p.patient_id "
        + "UNION "
        + "SELECT p.patient_id, "
        + "       MAX(e.encounter_datetime) AS pcr_date "
        + "FROM patient p "
        + "         JOIN encounter e "
        + "              ON p.patient_id = e.patient_id "
        + "         JOIN obs o "
        + "              ON e.encounter_id = o.encounter_id "
        + "WHERE p.voided = 0 "
        + "  AND e.voided = 0 "
        + "  AND o.voided = 0 "
        + "  AND e.location_id = :location "
        + "  AND e.encounter_type = ${93} "
        + "  AND e.encounter_datetime <= :endDate "
        + "  AND o.concept_id = ${1030} "
        + "  AND o.value_coded IS NOT NULL "
        + "GROUP BY p.patient_id "
        + " ) pcr "
        + " GROUP BY pcr.patient_id ";
  }

  /**
   *
   * <li>Penultimate PCR test date registered on Ficha Lab Geral (Data de resultado) or encounter
   *     date of CCR: Ficha Seguimento with (PCR- resultado)
   *
   * @return {@link String}
   */
  private String getPenultimatePCRResultDateQuery() {
    return " SELECT pen_pcr.patient_id, MAX(pen_pcr.pen_pcr_date) AS most_recent "
        + " FROM  ( SELECT p.patient_id, "
        + "       MAX(e.encounter_datetime) AS pen_pcr_date "
        + "FROM patient p "
        + "         JOIN encounter e "
        + "              ON p.patient_id = e.patient_id "
        + "         JOIN obs o "
        + "              ON e.encounter_id = o.encounter_id "
        + "         JOIN ( "
        + getPatientPCRResultDateQuery()
        + " ) last_pcr ON last_pcr.patient_id = p.patient_id "
        + "WHERE p.voided = 0 "
        + "  AND e.voided = 0 "
        + "  AND o.voided = 0 "
        + "  AND e.location_id = :location "
        + "  AND e.encounter_type = ${13} "
        + "  AND e.encounter_datetime < last_pcr.most_recent "
        + "  AND o.concept_id = ${1030} "
        + "  AND o.value_coded IS NOT NULL "
        + "GROUP BY p.patient_id "
        + "UNION "
        + "SELECT p.patient_id, "
        + "       MAX(e.encounter_datetime) AS pen_pcr_date "
        + "FROM patient p "
        + "         JOIN encounter e "
        + "              ON p.patient_id = e.patient_id "
        + "         JOIN obs o "
        + "              ON e.encounter_id = o.encounter_id "
        + "         JOIN ( "
        + getPatientPCRResultDateQuery()
        + " ) last_pcr ON last_pcr.patient_id = p.patient_id "
        + "WHERE p.voided = 0 "
        + "  AND e.voided = 0 "
        + "  AND o.voided = 0 "
        + "  AND e.location_id = :location "
        + "  AND e.encounter_type = ${93} "
        + "  AND e.encounter_datetime < last_pcr.most_recent "
        + "  AND o.concept_id = ${1030} "
        + "  AND o.value_coded IS NOT NULL "
        + "GROUP BY p.patient_id "
        + " ) pen_pcr "
        + " GROUP BY pen_pcr.patient_id ";
  }
}
