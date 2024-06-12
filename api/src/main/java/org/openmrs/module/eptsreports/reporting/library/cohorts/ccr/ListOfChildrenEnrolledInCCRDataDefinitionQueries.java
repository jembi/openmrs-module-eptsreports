package org.openmrs.module.eptsreports.reporting.library.cohorts.ccr;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.utils.EptsQueriesUtil;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListOfChildrenEnrolledInCCRDataDefinitionQueries {

  private final HivMetadata hivMetadata;

  @Autowired
  public ListOfChildrenEnrolledInCCRDataDefinitionQueries(HivMetadata hivMetadata) {
    this.hivMetadata = hivMetadata;
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
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
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
}
