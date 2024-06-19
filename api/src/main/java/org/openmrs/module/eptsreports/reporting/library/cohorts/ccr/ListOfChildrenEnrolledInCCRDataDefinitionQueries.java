package org.openmrs.module.eptsreports.reporting.library.cohorts.ccr;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
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

  @Autowired
  public ListOfChildrenEnrolledInCCRDataDefinitionQueries(
      HivMetadata hivMetadata, CommonMetadata commonMetadata) {
    this.hivMetadata = hivMetadata;
    this.commonMetadata = commonMetadata;
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
}
