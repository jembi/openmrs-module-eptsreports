package org.openmrs.module.eptsreports.reporting.library.cohorts.cd4request;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.library.queries.CommonQueries;
import org.openmrs.module.eptsreports.reporting.library.queries.cd4request.ListOfPatientsEligibleForCd4RequestQueries;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsEligibleForCd4RequestDataDefinitionQueries {

  private final CommonQueries commonQueries;
  private final HivMetadata hivMetadata;
  private final CommonMetadata commonMetadata;

  @Autowired
  public ListOfPatientsEligibleForCd4RequestDataDefinitionQueries(
      CommonQueries commonQueries, HivMetadata hivMetadata, CommonMetadata commonMetadata) {
    this.commonQueries = commonQueries;
    this.hivMetadata = hivMetadata;
    this.commonMetadata = commonMetadata;
  }

  /**
   * <b>Idade do Utente no fim do Período de Reporte</b>
   *
   * <p>Idade = Data Fim - Data de Nascimento <br>
   * Nota 1: A idade será calculada em anos para os utentes com idade >=2 anos, e em meses para os
   * utentes com idade < 2 anos.
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getPatientAgeInYearsOrMonths() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Idade do Utente no fim do Período de Reporte");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));

    String query =
        "SELECT pr.person_id, "
            + "       IF(ages.age >= 2, ages.age, CONCAT(ABS(Timestampdiff(MONTH, :endDate, "
            + "                                              pr.birthdate)), ' MESES')) AS age "
            + "FROM   person pr "
            + "       INNER JOIN (SELECT p.patient_id, "
            + "                          Floor(Datediff(:endDate, ps.birthdate) / 365) AS "
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
   *
   * <li>o 1º levantamento de ARVs registado no FILA (“Data de Levantamento”) até o fim do período,
   * <li>o 1º levantamento registado na “Ficha Recepção/ Levantou ARVs?” com “Levantou ARV” = Sim
   *     (“Data de Levantamento”),
   * <li>Data de Inscrição no serviço TARV-TRATAMENTO (Módulo de Inscrição do Programa) registada
   *     até o fim do período,
   * <li>Data do Início TARV registada no formulário “Ficha de Resumo – Ficha Mestra” registada até
   *     o fim do período,
   * <li>Data do Início TARV registada na “Ficha de Seguimento Adulto” ou “Ficha de Seguimento
   *     Pediátrica” registada até o fim do período,
   * <li>Data de consulta registada na “Ficha de Seguimento Adulto” ou “Ficha de Seguimento
   *     Pediátrica” com o “Plano ARV “=Início, ocorrida até o fim do período,
   * <li>Data de Levantamento registado no “FILA” com o “Plano ARV “=Início, registado até o fim do
   *     período
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getArtStartDate() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Data Início TARV");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    String query = commonQueries.getARTStartDate(true);
    sqlPatientDataDefinition.setQuery(query);
    return sqlPatientDataDefinition;
  }

  /**
   * <b>Grávida / Lactante </b>
   * <li>Resposta = Grávida, se o utente é do sexo feminino e tem registo de “Grávida” = “Sim” numa
   *     consulta clínica (Ficha Clinica – Ficha Mestra) durante o período de reporte
   * <li>Resposta = Lactante, se o utente é do sexo feminino e tem registo de “Lactante” = “Sim”
   *     numa consulta clínica (Ficha Clínica – Ficha Mestra) durante o período de reporte.
   * <li>Nota 1: em caso de existência de registo “Grávida” = “Sim” e “Lactante” = “Sim” durante o
   *     período de reporte, será considerado o registo mais recente.
   * <li>Nota 2: em caso de existência de registo “Grávida” = “Sim” e “Lactante” = “Sim” na mesma
   *     consulta (mais recente), será considerada como “Grávida”.
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getPregnantOrBreastfeedingFemalePatients() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Data Início TARV");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("5", hivMetadata.getARVAdultInitialEncounterType().getEncounterTypeId());
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("1982", commonMetadata.getPregnantConcept().getConceptId());
    valuesMap.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    valuesMap.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("6334", hivMetadata.getCriteriaForArtStart().getConceptId());
    valuesMap.put("6331", hivMetadata.getBpostiveConcept().getConceptId());
    valuesMap.put("1279", commonMetadata.getNumberOfWeeksPregnant().getConceptId());
    valuesMap.put("8", hivMetadata.getPtvEtvProgram().getProgramId());
    valuesMap.put("1465", hivMetadata.getDateOfLastMenstruationConcept().getConceptId());
    valuesMap.put("5599", commonMetadata.getPriorDeliveryDateConcept().getConceptId());
    valuesMap.put("6332", commonMetadata.getBreastfeeding().getConceptId());
    valuesMap.put("27", hivMetadata.getPatientGaveBirthWorkflowState().getProgramWorkflowStateId());
    valuesMap.put("1600", commonMetadata.getPregnancyDueDate().getConceptId());

    String query =
        " select patient_id, CASE WHEN (pregnancy_date IS NOT NULL) THEN 'Grávida' END AS pregnant_breastfeeding_value "
            + " FROM ( "
            + ListOfPatientsEligibleForCd4RequestQueries.getFemalePregnantPatientsQuery()
            + "    ) AS pregnant  "
            + " UNION   "
            + " select patient_id, CASE WHEN (breastfeeding_date IS NOT NULL) THEN 'Lactante' END AS pregnant_breastfeeding_value "
            + " FROM ( "
            + ListOfPatientsEligibleForCd4RequestQueries.getFemaleBreastfeedingPatientsQuery()
            + "  ) breastfeeding ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>Data da Última Consulta Clínica</b>
   *
   * <p>A data mais recente de consulta clínica (Ficha Clínica) decorrida até o fim do período de
   * reporte (campo: “Data Consulta”).
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getLastClinicalConsultationDate() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Data da Última Consulta Clínica");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    String query = ListOfPatientsEligibleForCd4RequestQueries.getLastConsultationDateQuery();
    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);
    sqlPatientDataDefinition.setQuery(substitutor.replace(query));
    return sqlPatientDataDefinition;
  }

  /**
   * <b>Data da Proxima Consulta Clínica Agendada</b>
   *
   * <p>A data de próxima consulta registada na última consulta clínica (Ficha Clínica) decorrida
   * até o fim do período de reporte (campo: “Data Próxima Consulta”)
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getNextConsultationDateOnLastClinicalConsultationDate() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Last Drug Pick Up Date");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("1410", hivMetadata.getReturnVisitDateConcept().getConceptId());

    String lastConsultationDateQuery =
        ListOfPatientsEligibleForCd4RequestQueries.getLastConsultationDateQuery();

    String query =
        " SELECT p.patient_id, o.value_datetime AS next_consultation_date "
            + " FROM   patient p  "
            + "          INNER JOIN encounter e  "
            + "                          ON p.patient_id = e.patient_id  "
            + "          INNER JOIN obs o "
            + "                             ON o.encounter_id = e.encounter_id  "
            + " INNER JOIN ( "
            + lastConsultationDateQuery
            + " )last_consultation ON last_consultation.patient_id = p.patient_id  "
            + " WHERE  p.voided = 0  "
            + "          AND e.voided = 0  "
            + "          AND o.voided = 0  "
            + "          AND e.location_id = :location "
            + "          AND e.encounter_type = ${6} "
            + "         AND e.encounter_datetime = last_consultation.last_date "
            + "         AND o.concept_id = ${1410} "
            + "         AND o.value_datetime IS NOT NULL "
            + " GROUP BY p.patient_id ";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);
    sqlPatientDataDefinition.setQuery(substitutor.replace(query));
    return sqlPatientDataDefinition;
  }
}
