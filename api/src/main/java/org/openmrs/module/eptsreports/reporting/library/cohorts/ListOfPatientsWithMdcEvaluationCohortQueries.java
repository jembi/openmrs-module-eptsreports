package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.library.queries.CommonQueries;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsWithMdcEvaluationCohortQueries {

  private CommonQueries commonQueries;

  private HivMetadata hivMetadata;

  String inclusionStartMonthAndDay = "'-01-21'";
  String inclusionEndMonthAndDay = "'-06-20'";

  @Autowired
  public ListOfPatientsWithMdcEvaluationCohortQueries(
      CommonQueries commonQueries, HivMetadata hivMetadata) {
    this.commonQueries = commonQueries;
    this.hivMetadata = hivMetadata;
  }

  /**
   * <b>RF9 - Relatório – Informação do utente - Secção A1 a A9</b>
   *
   * <p>A2- Coorte: (coluna B) – Resposta = 12 meses, caso o utente tenha iniciado TARV na coorte de
   * 12 meses, ou Resposta = 24 meses, caso o utente tenha iniciado TARV na coorte de 24 meses
   * (RF4).
   *
   * @return {CohortDefinition}
   */
  public CohortDefinition getCoort() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients who initiated the ART between the cohort period");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    String arvStart = commonQueries.getArtStartDateOnFichaResumo();

    String query =
        "SELECT patient_id FROM ( "
            + arvStart
            + " ) initiated_art"
            + "   WHERE initiated_art.art_start BETWEEN :startDate AND :endDate "
            + "   GROUP BY patient_id";

    sqlCohortDefinition.setQuery(query);
    return sqlCohortDefinition;
  }

  /**
   * <b>RF9 - Relatório – Informação do utente - Secção A1 a A9</b>
   *
   * <p>A5- Data início TARV: (coluna E) – Resposta = Data do Início TARV. A “Data do Início TARV” é
   * a data registada na Ficha Resumo (Data do Início TARV). Nota: caso exista mais que uma “Ficha
   * de Resumo” com “Data do Início TARV” diferente, deve ser considerada a data mais antiga.
   *
   * @return {DataDefinition}
   */
  public DataDefinition getArtStartDate() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("A.5 - ART Start Date");
    sqlPatientDataDefinition.addParameter(
        new Parameter("evaluationYear", "evaluationYear", Integer.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());

    String query =
        "SELECT p.patient_id, "
            + "       Min(o.value_datetime) art_start "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  e.encounter_type = ${53} "
            + "       AND o.concept_id = ${1190} "
            + "       AND e.location_id = :location "
            + "       AND o.value_datetime <= "
            + "  CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + " ) "
            + "       AND p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "GROUP  BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * O sistema irá determinar a idade do utente na Data Início TARV, ou seja, irá calcular a idade
   * com base na seguinte fórmula: Idade = Data Início TARV - Data de Nascimento
   *
   * <p>Nota 1: A idade será calculada em anos.
   *
   * <p>Nota 2: A “Data Início TARV” é a data registada na Ficha Resumo (“Data do Início TARV”).
   *
   * <p>Nota 3: caso exista mais que uma “Ficha de Resumo” com “Data do Início TARV” diferente, deve
   * ser considerada a data mais antiga.
   *
   * @return DataDefinition
   */
  public DataDefinition getAgeOnMOHArtStartDate() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Age on MOH ART start date");
    sqlPatientDataDefinition.addParameter(
        new Parameter("evaluationYear", "evaluationYear", Integer.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());

    String query =
        "SELECT p.patient_id, FLOOR(DATEDIFF(A1.first_start_drugs,ps.birthdate)/365) AS age "
            + "FROM patient p "
            + "     INNER JOIN ( "
            + "           SELECT p.patient_id, MIN(o.value_datetime) as first_start_drugs "
            + "           FROM patient p "
            + "                INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "           WHERE p.voided = 0 AND e.voided = 0 AND o.voided = 0 "
            + "             AND e.encounter_type = ${53} and o.concept_id = ${1190} "
            + "             AND e.location_id = :location "
            + "             AND o.value_datetime <= "
            + "  CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + " ) "
            + "           GROUP BY p.patient_id ) AS A1 ON p.patient_id = A1.patient_id "
            + "  INNER JOIN person ps ON p.patient_id=ps.person_id WHERE p.voided=0 AND ps.voided=0 ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  private void addSqlPatientDataDefinitionParameters(
      SqlPatientDataDefinition sqlPatientDataDefinition) {
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
  }
}
