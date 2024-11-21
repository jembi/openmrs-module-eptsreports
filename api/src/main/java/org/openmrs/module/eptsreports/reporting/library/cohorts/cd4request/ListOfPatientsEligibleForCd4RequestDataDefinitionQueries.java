package org.openmrs.module.eptsreports.reporting.library.cohorts.cd4request;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ResumoMensalCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.queries.CommonQueries;
import org.openmrs.module.eptsreports.reporting.library.queries.cd4request.ListOfPatientsEligibleForCd4RequestQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsQueriesUtil;
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
  private final TbMetadata tbMetadata;
  private final ResumoMensalCohortQueries resumoMensalCohortQueries;

  @Autowired
  public ListOfPatientsEligibleForCd4RequestDataDefinitionQueries(
      CommonQueries commonQueries,
      HivMetadata hivMetadata,
      CommonMetadata commonMetadata,
      TbMetadata tbMetadata,
      ResumoMensalCohortQueries resumoMensalCohortQueries) {
    this.commonQueries = commonQueries;
    this.hivMetadata = hivMetadata;
    this.commonMetadata = commonMetadata;
    this.tbMetadata = tbMetadata;
    this.resumoMensalCohortQueries = resumoMensalCohortQueries;
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

  /**
   * <b>Data do último Pedido de CD4</b>
   *
   * <p>A data do último registo de “Pedido de CD4” na consulta clínica (Ficha Clínica – Ficha
   * Mestra) decorrida até ao fim do período de reporte
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getLastCd4ResquestDate() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Data do último Pedido de CD4");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());

    String query =
        "SELECT p.patient_id, MAX(e.encounter_datetime) request_date "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${6} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${23722} "
            + "       AND o.value_coded = ${1695} "
            + "       AND e.encounter_datetime <= :endDate "
            + " GROUP BY p.patient_id ";

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlPatientDataDefinition.setQuery(sb.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   *
   * <li>Resposta = “C1 – CD4 Inicial - Novo Início TARV”, se o utente iniciou TARV durante o
   *     período seguindo os critérios definidos no CD4_RF3
   * <li>Resposta = “C2– CD4 Inicial - Reinício TARV”, se o utente reiniciou o TARV durante o
   *     período seguindo os critérios definidos no CD4_FR4.
   * <li>Resposta = “C3– CV Alta”, se o utente tiver duas CVs altas seguindo os critérios definidos
   *     no CD4_FR5.
   * <li>Resposta = “C4– Estadiamento Clínico III ou IV”, se o utente tiver condição activa de
   *     estadiamento clínico III ou IV seguindo os critérios definidos no CD4_FR6.
   * <li>Resposta = “C5– CD4 de Seguimento”, se o utente é elegível ao pedido de CD4 de seguimento
   *     seguindo os critérios definidos no CD4_FR7).
   * <li>Resposta = “C6– Mulher Grávida”, se o utente for mulher grávida elegível ao pedido de CD4
   *     seguindo os critérios definidos no CD4_FR8.
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getReasonForCd4Eligibility() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Motivo para Elegibilidade de CD4");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(
        new Parameter("generationDate", "generationDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1705", hivMetadata.getRestartConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("730", hivMetadata.getCD4PercentConcept().getConceptId());
    map.put("1406", hivMetadata.getOtherDiagnosis().getConceptId());
    map.put("5018", hivMetadata.getChronicDiarrheaConcept().getConceptId());
    map.put("3", hivMetadata.getAnemiaConcept().getConceptId());
    map.put("5945", hivMetadata.getFeverConcept().getConceptId());
    map.put("43", hivMetadata.getPneumoniaConcept().getConceptId());
    map.put("60", hivMetadata.getMeningitisConcept().getConceptId());
    map.put("126", hivMetadata.getGingivitisConcept().getConceptId());
    map.put("6783", hivMetadata.getEstomatiteUlcerativaNecrotizanteConcept().getConceptId());
    map.put("5334", hivMetadata.getCandidiaseOralConcept().getConceptId());
    map.put("1294", hivMetadata.getCryptococcalMeningitisConcept().getConceptId());
    map.put("1570", hivMetadata.getCervicalCancerConcept().getConceptId());
    map.put("5340", hivMetadata.getCandidiaseEsofagicaConcept().getConceptId());
    map.put("5344", hivMetadata.getHerpesSimplesConcept().getConceptId());
    map.put("14656", hivMetadata.getCachexiaConcept().getConceptId());
    map.put("7180", hivMetadata.getToxoplasmoseConcept().getConceptId());
    map.put("6990", hivMetadata.getHivDiseaseResultingInEncephalopathyConcept().getConceptId());
    map.put("507", commonMetadata.getKaposiSarcomaConcept().getConceptId());
    map.put("5042", tbMetadata.getExtraPulmonaryTbConcept().getConceptId());
    map.put("42", tbMetadata.getPulmonaryTB().getConceptId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("165515", hivMetadata.getCD4SemiQuantitativeConcept().getConceptId());
    map.put("165513", hivMetadata.getCD4CountLessThanOrEqualTo200Concept().getConceptId());
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());

    String query =
        " SELECT C1.patient_id, 'C1 – CD4 Inicial - Novo Início TARV' "
            + " FROM ( "
            + getC1Query()
            + "    ) AS C1  "
            + " UNION   "
            + " SELECT C2.patient_id, 'C2 – CD4 Inicial - Reinício TARV' "
            + " FROM ( "
            + getC2Query()
            + "  ) C2 "
            + " WHERE C2.patient_id NOT IN ( "
            + getC1Query().concat("AND C2.patient_id = start.patient_id")
            + "  ) "
            + " UNION   "
            + " SELECT C3.patient_id, 'C3– CV Alta' "
            + " FROM ( "
            + getC3Query()
            + "  ) C3 "
            + " WHERE C3.patient_id NOT IN ( "
            + new EptsQueriesUtil()
                .unionBuilder(getC1Query().concat(" AND C3.patient_id = start.patient_id "))
                .union(getC2Query().concat(" AND C3.patient_id = restarted.patient_id "))
                .buildQuery()
            + " ) "
            + " UNION   "
            + " SELECT C4.patient_id, 'C4– Estadiamento Clínico III ou IV' "
            + " FROM ( "
            + getC4Query()
            + "  ) C4 "
            + " WHERE C4.patient_id NOT IN ( "
            + new EptsQueriesUtil()
                .unionBuilder(getC1Query().concat(" AND C4.patient_id = start.patient_id "))
                .union(getC2Query().concat(" AND C4.patient_id = restarted.patient_id "))
                .union(getC3Query().concat(" AND C4.patient_id = first_vl_result.patient_id "))
                .buildQuery()
            + " ) "
            + " UNION   "
            + " SELECT C5.patient_id, 'C5– CD4 de Seguimento' "
            + " FROM ( "
            + getC5Query()
            + "  ) C5 "
            + " WHERE C5.patient_id NOT IN ( "
            + new EptsQueriesUtil()
                .unionBuilder(getC1Query().concat(" AND C5.patient_id = start.patient_id "))
                .union(getC2Query().concat(" AND C5.patient_id = restarted.patient_id "))
                .union(getC3Query().concat(" AND C5.patient_id = first_vl_result.patient_id "))
                .union(getC4Query().concat(" AND C5.patient_id = estadio.patient_id "))
                .buildQuery()
            + " ) "
            + " UNION   "
            + " SELECT C6.patient_id, 'C6– Mulher Grávida' "
            + " FROM ( "
            + getC6Query()
            + "  ) C6 "
            + " WHERE C6.patient_id NOT IN ( "
            + new EptsQueriesUtil()
                .unionBuilder(getC1Query().concat(" AND C6.patient_id = start.patient_id "))
                .union(getC2Query().concat(" AND C6.patient_id = restarted.patient_id "))
                .union(getC3Query().concat(" AND C6.patient_id = first_vl_result.patient_id "))
                .union(getC4Query().concat(" AND C6.patient_id = estadio.patient_id "))
                .union(getC5Query().concat(" AND C6.patient_id = cd4.patient_id "))
                .buildQuery()
            + " ) ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  private String getC1Query() {
    return "SELECT start.patient_id "
        + "FROM   ( "
        + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
        + " ) start "
        + "WHERE  start.first_pickup BETWEEN :startDate AND    :endDate "
        + "AND    start.patient_id NOT IN "
        + "       ( "
        + "                  SELECT     p.patient_id "
        + "                  FROM       patient p "
        + "                  INNER JOIN patient_program pg "
        + "                  ON         p.patient_id = pg.patient_id "
        + "                  INNER JOIN patient_state ps "
        + "                  ON         pg.patient_program_id = ps.patient_program_id "
        + "                  INNER JOIN "
        + "                             ( "
        + "                                        SELECT     p.patient_id, "
        + "                                                   min(ps.start_date) AS data_transferido "
        + "                                        FROM       patient p "
        + "                                        INNER JOIN patient_program pg "
        + "                                        ON         p.patient_id = pg.patient_id "
        + "                                        INNER JOIN patient_state ps "
        + "                                        ON         pg.patient_program_id = ps.patient_program_id "
        + "                                        WHERE      pg.voided = 0 "
        + "                                        AND        ps.voided = 0 "
        + "                                        AND        p.voided = 0 "
        + "                                        AND        pg.program_id = ${2} "
        + "                                        AND        pg.location_id = :location "
        + "                                        AND        ps.start_date <= :endDate "
        + "                                        GROUP BY   p.patient_id) pgenrollment "
        + "                  ON         pgenrollment.patient_id = p.patient_id "
        + "                  WHERE      ps.start_date=pgenrollment.data_transferido "
        + "                  AND        pg.program_id = ${2} "
        + "                  AND        ps.state = ${29} "
        + "                  AND        p.patient_id = start.patient_id "
        + "                  AND        pg.location_id = :location "
        + "                  AND        ps.voided=0 "
        + "                  AND        pg.voided = 0 "
        + "                  AND        p.voided = 0 "
        + "                  UNION "
        + "                  SELECT     p.patient_id "
        + "                  FROM       patient p "
        + "                  INNER JOIN encounter e "
        + "                  ON         p.patient_id = e.patient_id "
        + "                  INNER JOIN obs o "
        + "                  ON         e.encounter_id = o.encounter_id "
        + "                  INNER JOIN obs o2 "
        + "                  ON         e.encounter_id = o2.encounter_id "
        + "                  WHERE      p.voided = 0 "
        + "                  AND        e.voided = 0 "
        + "                  AND        o.voided = 0 "
        + "                  AND        o2.voided = 0 "
        + "                  AND        p.patient_id = start.patient_id "
        + "                  AND        e.location_id = :location "
        + "                  AND        e.encounter_type = ${53} "
        + "                  AND        o.concept_id = ${1369} "
        + "                  AND        o.value_coded = ${1065} "
        + "                  AND        o2.concept_id = ${23891} "
        + "                  AND        o2.value_datetime <= :endDate "
        + "                  GROUP BY   p.patient_id "
        + "                  UNION "
        + "                  SELECT     pa.patient_id "
        + "                  FROM       patient pa "
        + "                  INNER JOIN encounter enc "
        + "                  ON         enc.patient_id = pa.patient_id "
        + "                  INNER JOIN obs "
        + "                  ON         obs.encounter_id = enc.encounter_id "
        + "                  WHERE      pa.voided = 0 "
        + "                  AND        enc.voided = 0 "
        + "                  AND        obs.voided = 0 "
        + "                  AND        enc.encounter_type = ${6} "
        + "                  AND ( "
        + "                        (obs.concept_id = ${1695} AND obs.value_numeric IS NOT NULL) "
        + "                        OR "
        + "                        (obs.concept_id = ${730} AND obs.value_numeric IS NOT NULL) "
        + "                        OR ( obs.concept_id = ${165515} AND obs.value_coded IS NOT NULL ) "
        + "                      ) "
        + "                  AND        enc.encounter_datetime <= :generationDate "
        + "                  AND        enc.location_id = :location "
        + "                  AND        pa.patient_id = start.patient_id "
        + "                  GROUP BY   pa.patient_id ) ";
  }

  private String getC2Query() {
    return " SELECT restarted.patient_id FROM ( SELECT p.patient_id "
        + " FROM   patient p "
        + "           INNER JOIN encounter e "
        + "                      ON e.patient_id = p.patient_id "
        + "           INNER JOIN obs o "
        + "                      ON o.encounter_id = e.encounter_id "
        + "INNER JOIN ( "
        + "             SELECT p.patient_id, "
        + "                    MIN(e.encounter_datetime) AS first_date "
        + "             FROM   patient p "
        + "                        INNER JOIN encounter e "
        + "                                   ON e.patient_id = p.patient_id "
        + "                        INNER JOIN obs o "
        + "                                   ON o.encounter_id = e.encounter_id "
        + "             WHERE "
        + "                 p.voided = 0 "
        + "               AND e.voided = 0 "
        + "               AND o.voided = 0 "
        + "               AND e.encounter_type = ${6} "
        + "               AND e.location_id = :location "
        + "               AND o.concept_id = ${6273} "
        + "               AND o.value_coded IS NOT NULL "
        + "               AND e.encounter_datetime >= :startDate "
        + "               AND e.encounter_datetime <= :endDate "
        + "             GROUP  BY p.patient_id "
        + ") first_state ON first_state.patient_id = p.patient_id "
        + "WHERE   p.voided = 0 "
        + "  AND e.voided = 0 "
        + "  AND o.voided = 0 "
        + "  AND e.encounter_type = ${6} "
        + "  AND e.location_id = :location "
        + "  AND o.concept_id = ${6273} "
        + "  AND o.value_coded = ${1705} "
        + "  AND e.encounter_datetime = first_state.first_date "
        + "GROUP  BY p.patient_id "
        + "UNION "
        + "SELECT p.patient_id "
        + "FROM   patient p "
        + "           INNER JOIN encounter e "
        + "                      ON e.patient_id = p.patient_id "
        + "           INNER JOIN obs o "
        + "                      ON o.encounter_id = e.encounter_id "
        + "           INNER JOIN ( "
        + "             SELECT p.patient_id, "
        + "                    MIN(o.obs_datetime) AS first_date "
        + "             FROM   patient p "
        + "                        INNER JOIN encounter e "
        + "                                   ON e.patient_id = p.patient_id "
        + "                        INNER JOIN obs o "
        + "                                   ON o.encounter_id = e.encounter_id "
        + "             WHERE "
        + "                 p.voided = 0 "
        + "               AND e.voided = 0 "
        + "               AND o.voided = 0 "
        + "               AND e.encounter_type = ${53} "
        + "               AND e.location_id = :location "
        + "               AND o.concept_id = ${6272} "
        + "               AND o.value_coded IS NOT NULL "
        + "               AND e.encounter_datetime >= :startDate "
        + "               AND e.encounter_datetime <= :endDate "
        + "             GROUP  BY p.patient_id "
        + ") first_state ON first_state.patient_id = p.patient_id "
        + "WHERE   p.voided = 0 "
        + "  AND e.voided = 0 "
        + "  AND o.voided = 0 "
        + "  AND e.encounter_type = ${53} "
        + "  AND e.location_id = :location "
        + "  AND o.concept_id = ${6272} "
        + "  AND o.value_coded = ${1705} "
        + "  AND o.obs_datetime = first_state.first_date "
        + "GROUP  BY p.patient_id ) restarted "
        + " WHERE restarted.patient_id NOT IN ( "
        + "                  SELECT     pa.patient_id "
        + "                  FROM       patient pa "
        + "                  INNER JOIN encounter enc "
        + "                  ON         enc.patient_id = pa.patient_id "
        + "                  INNER JOIN obs "
        + "                  ON         obs.encounter_id = enc.encounter_id "
        + "                  WHERE      pa.voided = 0 "
        + "                  AND        enc.voided = 0 "
        + "                  AND        obs.voided = 0 "
        + "                  AND        enc.encounter_type = ${6} "
        + "                  AND ( "
        + "                        (obs.concept_id = ${1695} AND obs.value_numeric IS NOT NULL) "
        + "                        OR "
        + "                        (obs.concept_id = ${730} AND obs.value_numeric IS NOT NULL) "
        + "                        OR ( obs.concept_id = ${165515} AND obs.value_coded IS NOT NULL ) "
        + "                      ) "
        + "                  AND enc.encounter_datetime >= :startDate "
        + "                  AND enc.encounter_datetime <= :generationDate "
        + "                  AND        enc.location_id = :location "
        + "                  AND        pa.patient_id = restarted.patient_id "
        + "                  GROUP BY   pa.patient_id ) ";
  }

  private String getC3Query() {
    return "SELECT first_vl_result.patient_id FROM (SELECT p.patient_id, o.value_numeric AS viral_load "
        + "                                        FROM   patient p "
        + "                                                   INNER JOIN encounter e "
        + "                                                              ON e.patient_id = p.patient_id "
        + "                                                   INNER JOIN obs o "
        + "                                                              ON o.encounter_id = e.encounter_id "
        + "                                                   INNER JOIN ( "
        + "SELECT p.patient_id, "
        + "       Max(e.encounter_datetime) AS most_recent "
        + "FROM   patient p "
        + "       INNER JOIN encounter e "
        + "               ON e.patient_id = p.patient_id "
        + "       INNER JOIN obs o "
        + "               ON o.encounter_id = e.encounter_id "
        + "WHERE  e.encounter_type = ${6} "
        + "       AND ( ( o.concept_id = ${856} "
        + "               AND o.value_numeric IS NOT NULL ) "
        + "              OR ( o.concept_id = ${1305} "
        + "                   AND o.value_coded IS NOT NULL ) ) "
        + "       AND e.encounter_datetime <= :endDate "
        + "       AND e.location_id = :location "
        + "       AND e.voided = 0 "
        + "       AND p.voided = 0 "
        + "       AND o.voided = 0 "
        + "GROUP  BY p.patient_id "
        + "                                            )last_vl ON last_vl.patient_id = p.patient_id "
        + "                                        WHERE  e.encounter_type = ${6} "
        + "                                          AND o.concept_id = ${856} "
        + "                                          AND o.value_numeric IS NOT NULL "
        + "                                          AND o.value_numeric > 1000 "
        + "                                          AND e.encounter_datetime = last_vl.most_recent "
        + "                                          AND e.location_id = :location "
        + "                                          AND e.voided = 0 "
        + "                                          AND p.voided = 0 "
        + "                                          AND o.voided = 0 "
        + "                                        GROUP  BY p.patient_id   ) first_vl_result "
        + "                                           INNER JOIN ( "
        + "    SELECT p.patient_id, o.value_numeric AS second_viral_load "
        + "    FROM   patient p "
        + "               INNER JOIN encounter e "
        + "                          ON e.patient_id = p.patient_id "
        + "               INNER JOIN obs o "
        + "                          ON o.encounter_id = e.encounter_id "
        + "               INNER JOIN ( "
        + "SELECT p.patient_id, Max(e.encounter_datetime) AS second_most_recent  "
        + "FROM   patient p  "
        + "                                  INNER JOIN encounter e  "
        + "                                          ON e.patient_id = p.patient_id  "
        + "                                  INNER JOIN obs o  "
        + "                                          ON o.encounter_id = e.encounter_id  "
        + "                           INNER JOIN (  "
        + "SELECT p.patient_id, "
        + "       Max(e.encounter_datetime) AS most_recent "
        + "FROM   patient p "
        + "       INNER JOIN encounter e "
        + "               ON e.patient_id = p.patient_id "
        + "       INNER JOIN obs o "
        + "               ON o.encounter_id = e.encounter_id "
        + "WHERE  e.encounter_type = ${6} "
        + "       AND ( ( o.concept_id = ${856} "
        + "               AND o.value_numeric IS NOT NULL ) "
        + "              OR ( o.concept_id = ${1305} "
        + "                   AND o.value_coded IS NOT NULL ) ) "
        + "       AND e.encounter_datetime <= :endDate "
        + "       AND e.location_id = :location "
        + "       AND e.voided = 0 "
        + "       AND p.voided = 0 "
        + "       AND o.voided = 0 "
        + "GROUP  BY p.patient_id "
        + " )last_vl ON last_vl.patient_id = p.patient_id  "
        + "WHERE  e.encounter_type = ${6} "
        + "       AND ( ( o.concept_id = ${856} "
        + "               AND o.value_numeric IS NOT NULL ) "
        + "              OR ( o.concept_id = ${1305} "
        + "                   AND o.value_coded IS NOT NULL ) ) "
        + "       AND e.encounter_datetime < last_vl.most_recent "
        + "       AND e.location_id = :location "
        + "       AND e.voided = 0 "
        + "       AND p.voided = 0 "
        + "       AND o.voided = 0 "
        + "GROUP  BY p.patient_id "
        + "        )second_vl ON second_vl.patient_id = p.patient_id "
        + "    WHERE  e.encounter_type = ${6} "
        + "      AND o.concept_id = ${856} "
        + "      AND o.value_numeric IS NOT NULL "
        + "      AND o.value_numeric > 1000 "
        + "      AND e.encounter_datetime = second_vl.second_most_recent "
        + "      AND e.location_id = :location "
        + "      AND e.voided = 0 "
        + "      AND p.voided = 0 "
        + "      AND o.voided = 0 "
        + "    GROUP  BY p.patient_id "
        + ") second_vl_result ON second_vl_result.patient_id = first_vl_result.patient_id "
        + "WHERE first_vl_result.patient_id NOT IN ( "
        + "    SELECT pa.patient_id "
        + "    FROM "
        + "        patient pa "
        + "            INNER JOIN encounter enc "
        + "                       ON enc.patient_id =  pa.patient_id "
        + "            INNER JOIN obs "
        + "                       ON obs.encounter_id = enc.encounter_id "
        + "            INNER JOIN ( "
        + "SELECT p.patient_id, "
        + "       Max(e.encounter_datetime) AS most_recent "
        + "FROM   patient p "
        + "       INNER JOIN encounter e "
        + "               ON e.patient_id = p.patient_id "
        + "       INNER JOIN obs o "
        + "               ON o.encounter_id = e.encounter_id "
        + "WHERE  e.encounter_type = ${6} "
        + "       AND ( ( o.concept_id = ${856} "
        + "               AND o.value_numeric IS NOT NULL ) "
        + "              OR ( o.concept_id = ${1305} "
        + "                   AND o.value_coded IS NOT NULL ) ) "
        + "       AND e.encounter_datetime <= :endDate "
        + "       AND e.location_id = :location "
        + "       AND e.voided = 0 "
        + "       AND p.voided = 0 "
        + "       AND o.voided = 0 "
        + "GROUP  BY p.patient_id "
        + "            ) last_vl ON last_vl.patient_id = pa.patient_id "
        + "    WHERE  pa.voided = 0 "
        + "      AND enc.voided = 0 "
        + "      AND obs.voided = 0 "
        + "      AND enc.encounter_type = ${6} "
        + "      AND ( "
        + "        (obs.concept_id = ${1695} AND obs.value_numeric IS NOT NULL) "
        + "            OR "
        + "        (obs.concept_id = ${730} AND obs.value_numeric IS NOT NULL) "
        + "        OR (obs.concept_id = ${165515} AND obs.value_coded IS NOT NULL) "
        + "        ) "
        + "      AND enc.encounter_datetime >= last_vl.most_recent "
        + "      AND enc.encounter_datetime <= :generationDate "
        + "      AND enc.location_id = :location "
        + "      AND pa.patient_id = first_vl_result.patient_id "
        + "    GROUP BY pa.patient_id "
        + ")"
        + " UNION "
        + "  SELECT first_vl_result.patient_id FROM (SELECT p.patient_id, o.value_numeric AS viral_load "
        + "                                        FROM   patient p "
        + "                                                   INNER JOIN encounter e "
        + "                                                              ON e.patient_id = p.patient_id "
        + "                                                   INNER JOIN obs o "
        + "                                                              ON o.encounter_id = e.encounter_id "
        + "                                                   INNER JOIN ( "
        + "SELECT p.patient_id, "
        + "       Max(DATE(e.encounter_datetime)) AS most_recent "
        + "FROM   patient p "
        + "       INNER JOIN encounter e "
        + "               ON e.patient_id = p.patient_id "
        + "       INNER JOIN obs o "
        + "               ON o.encounter_id = e.encounter_id "
        + "WHERE  e.encounter_type IN (${13},${51}) "
        + "       AND ( ( o.concept_id = ${856} "
        + "               AND o.value_numeric IS NOT NULL ) "
        + "              OR ( o.concept_id = ${1305} "
        + "                   AND o.value_coded IS NOT NULL ) ) "
        + "       AND DATE(e.encounter_datetime) <= :endDate "
        + "       AND e.location_id = :location "
        + "       AND e.voided = 0 "
        + "       AND p.voided = 0 "
        + "       AND o.voided = 0 "
        + "GROUP  BY p.patient_id "
        + "                                            )last_vl ON last_vl.patient_id = p.patient_id "
        + "                                        WHERE  e.encounter_type IN (${13},${51}) "
        + "                                          AND o.concept_id = ${856} "
        + "                                          AND o.value_numeric IS NOT NULL "
        + "                                          AND o.value_numeric > 1000 "
        + "                                          AND DATE(e.encounter_datetime) = last_vl.most_recent "
        + "                                          AND e.location_id = :location "
        + "                                          AND e.voided = 0 "
        + "                                          AND p.voided = 0 "
        + "                                          AND o.voided = 0 "
        + "                                        GROUP  BY p.patient_id   ) first_vl_result "
        + "                                           INNER JOIN ( "
        + "    SELECT p.patient_id, o.value_numeric AS second_viral_load "
        + "    FROM   patient p "
        + "               INNER JOIN encounter e "
        + "                          ON e.patient_id = p.patient_id "
        + "               INNER JOIN obs o "
        + "                          ON o.encounter_id = e.encounter_id "
        + "               INNER JOIN ( "
        + "SELECT p.patient_id, Max(DATE(e.encounter_datetime)) AS second_most_recent  "
        + "FROM   patient p  "
        + "                                  INNER JOIN encounter e  "
        + "                                          ON e.patient_id = p.patient_id  "
        + "                                  INNER JOIN obs o  "
        + "                                          ON o.encounter_id = e.encounter_id  "
        + "                           INNER JOIN (  "
        + "SELECT p.patient_id, "
        + "       Max(DATE(e.encounter_datetime)) AS most_recent "
        + "FROM   patient p "
        + "       INNER JOIN encounter e "
        + "               ON e.patient_id = p.patient_id "
        + "       INNER JOIN obs o "
        + "               ON o.encounter_id = e.encounter_id "
        + "WHERE  e.encounter_type IN (${13},${51}) "
        + "       AND ( ( o.concept_id = ${856} "
        + "               AND o.value_numeric IS NOT NULL ) "
        + "              OR ( o.concept_id = ${1305} "
        + "                   AND o.value_coded IS NOT NULL ) ) "
        + "       AND DATE(e.encounter_datetime) <= :endDate "
        + "       AND e.location_id = :location "
        + "       AND e.voided = 0 "
        + "       AND p.voided = 0 "
        + "       AND o.voided = 0 "
        + "GROUP  BY p.patient_id "
        + " )last_vl ON last_vl.patient_id = p.patient_id  "
        + "WHERE  e.encounter_type IN (${13},${51}) "
        + "       AND ( ( o.concept_id = ${856} "
        + "               AND o.value_numeric IS NOT NULL ) "
        + "              OR ( o.concept_id = ${1305} "
        + "                   AND o.value_coded IS NOT NULL ) ) "
        + "       AND DATE(e.encounter_datetime) < last_vl.most_recent "
        + "       AND e.location_id = :location "
        + "       AND e.voided = 0 "
        + "       AND p.voided = 0 "
        + "       AND o.voided = 0 "
        + "GROUP  BY p.patient_id "
        + "        )second_vl ON second_vl.patient_id = p.patient_id "
        + "    WHERE  e.encounter_type IN (${13},${51}) "
        + "      AND o.concept_id = ${856} "
        + "      AND o.value_numeric IS NOT NULL "
        + "      AND o.value_numeric > 1000 "
        + "      AND DATE(e.encounter_datetime) = second_vl.second_most_recent "
        + "      AND e.location_id = :location "
        + "      AND e.voided = 0 "
        + "      AND p.voided = 0 "
        + "      AND o.voided = 0 "
        + "    GROUP  BY p.patient_id "
        + ") second_vl_result ON second_vl_result.patient_id = first_vl_result.patient_id "
        + "WHERE first_vl_result.patient_id NOT IN ( "
        + "    SELECT pa.patient_id "
        + "    FROM "
        + "        patient pa "
        + "            INNER JOIN encounter enc "
        + "                       ON enc.patient_id =  pa.patient_id "
        + "            INNER JOIN obs "
        + "                       ON obs.encounter_id = enc.encounter_id "
        + "            INNER JOIN ( "
        + "SELECT p.patient_id, "
        + "       Max(DATE(e.encounter_datetime)) AS most_recent "
        + "FROM   patient p "
        + "       INNER JOIN encounter e "
        + "               ON e.patient_id = p.patient_id "
        + "       INNER JOIN obs o "
        + "               ON o.encounter_id = e.encounter_id "
        + "WHERE  e.encounter_type IN (${13},${51}) "
        + "       AND ( ( o.concept_id = ${856} "
        + "               AND o.value_numeric IS NOT NULL ) "
        + "              OR ( o.concept_id = ${1305} "
        + "                   AND o.value_coded IS NOT NULL ) ) "
        + "       AND DATE(e.encounter_datetime) <= :endDate "
        + "       AND e.location_id = :location "
        + "       AND e.voided = 0 "
        + "       AND p.voided = 0 "
        + "       AND o.voided = 0 "
        + "GROUP  BY p.patient_id "
        + "            ) last_vl ON last_vl.patient_id = pa.patient_id "
        + "    WHERE  pa.voided = 0 "
        + "      AND enc.voided = 0 "
        + "      AND obs.voided = 0 "
        + "      AND enc.encounter_type = ${6} "
        + "      AND ( "
        + "        (obs.concept_id = ${1695} AND obs.value_numeric IS NOT NULL) "
        + "            OR "
        + "        (obs.concept_id = ${730} AND obs.value_numeric IS NOT NULL) "
        + "        OR (obs.concept_id = ${165515} AND obs.value_coded IS NOT NULL) "
        + "        ) "
        + "      AND DATE(enc.encounter_datetime) >= last_vl.most_recent "
        + "      AND DATE(enc.encounter_datetime) <= :generationDate "
        + "      AND enc.location_id = :location "
        + "      AND pa.patient_id = first_vl_result.patient_id "
        + "    GROUP BY pa.patient_id "
        + ")";
  }

  private String getC4Query() {
    return " SELECT estadio.patient_id FROM (SELECT DISTINCT p.patient_id "
        + "                                FROM "
        + "                                    patient p INNER JOIN encounter e ON p.patient_id= e.patient_id "
        + "                                              INNER JOIN obs o on e.encounter_id = o.encounter_id "
        + "                                WHERE p.voided = 0 AND e.voided = 0 AND o.voided = 0 "
        + "                                  AND e.encounter_type = ${6} "
        + "                                  AND o.concept_id = ${1406} "
        + "                                  AND o.value_coded IN (${3},${42},${43},${60},${126},${507},${1294},${1570},${5018},${5042},${5334},${5344},${5340},${5945},${6783},${6990},${7180},${14656}) "
        + "                                  AND e.encounter_datetime >= :startDate "
        + "                                  AND e.encounter_datetime <= :endDate "
        + "                                  AND e.location_id = :location  ) estadio "
        + "WHERE estadio.patient_id NOT IN ( "
        + "    SELECT pa.patient_id "
        + "    FROM "
        + "        patient pa "
        + "            INNER JOIN encounter enc "
        + "                       ON enc.patient_id =  pa.patient_id "
        + "            INNER JOIN obs "
        + "                       ON obs.encounter_id = enc.encounter_id "
        + "            INNER JOIN ( "
        + ListOfPatientsEligibleForCd4RequestQueries.getEstadioOmsQuery()
        + "            ) estadio ON estadio.patient_id = pa.patient_id "
        + "    WHERE  pa.voided = 0 "
        + "      AND enc.voided = 0 "
        + "      AND obs.voided = 0 "
        + "      AND enc.encounter_type = ${6} "
        + "      AND ( "
        + "        (obs.concept_id = ${1695} AND obs.value_numeric IS NOT NULL) "
        + "            OR "
        + "        (obs.concept_id = ${730} AND obs.value_numeric IS NOT NULL) "
        + "        OR ( obs.concept_id = ${165515} AND obs.value_coded IS NOT NULL ) "
        + "        ) "
        + "      AND enc.encounter_datetime >= estadio.first_date "
        + "      AND enc.encounter_datetime <= :generationDate "
        + "      AND enc.location_id = :location "
        + "      AND pa.patient_id = estadio.patient_id "
        + "    GROUP BY pa.patient_id "
        + " ) ";
  }

  private String getC5Query() {
    return "SELECT cd4.patient_id FROM ( "
        + "                               SELECT pa.patient_id "
        + "                               FROM "
        + "                                   patient pa "
        + "                                       INNER JOIN encounter enc "
        + "                                                  ON enc.patient_id =  pa.patient_id "
        + "                                       INNER JOIN obs "
        + "                                                  ON obs.encounter_id = enc.encounter_id "
        + "                                       INNER JOIN ( "
        + "                                          SELECT pa.patient_id, MAX(enc.encounter_datetime) AS last_cd4 "
        + "                                          FROM "
        + "                                              patient pa "
        + "                                                  INNER JOIN encounter enc "
        + "                                                             ON enc.patient_id =  pa.patient_id "
        + "                                                  INNER JOIN obs "
        + "                                                             ON obs.encounter_id = enc.encounter_id "
        + "                                          WHERE  pa.voided = 0 "
        + "                                            AND enc.voided = 0 "
        + "                                            AND obs.voided = 0 "
        + "                                            AND enc.encounter_type = ${6} "
        + "                                            AND ( "
        + "                                                  (obs.concept_id = ${1695} AND obs.value_numeric IS NOT NULL) "
        + "                                                  OR "
        + "                                                  (obs.concept_id = ${730} AND obs.value_numeric IS NOT NULL) "
        + "                                              OR ( obs.concept_id = ${165515} AND obs.value_coded IS NOT NULL ) "
        + "                                                ) "
        + "                                            AND enc.encounter_datetime <= DATE_SUB(:endDate, INTERVAL 12 MONTH) "
        + "                                            AND enc.location_id = :location "
        + "                                          GROUP BY pa.patient_id "
        + "                                       ) cd4_date ON cd4_date.patient_id = pa.patient_id "
        + "                               WHERE  pa.voided = 0 "
        + "                                 AND enc.voided = 0 "
        + "                                 AND obs.voided = 0 "
        + "                                 AND enc.encounter_type = ${6} "
        + "                                 AND ( "
        + "                                   (obs.concept_id = ${1695} "
        + "                                       AND obs.value_numeric IS NOT NULL "
        + "                                       AND obs.value_numeric < 350) "
        + "                                       OR "
        + "                                   (obs.concept_id = ${730} "
        + "                                       AND obs.value_numeric IS NOT NULL "
        + "                                       AND obs.value_numeric < 30) "
        + "                                     OR "
        + "                                     (obs.concept_id = ${165515} "
        + "                                          AND obs.value_coded = ${165513}) "
        + "                                   ) "
        + "                                 AND enc.encounter_datetime = cd4_date.last_cd4 "
        + "                                 AND enc.location_id = :location "
        + "                               GROUP BY pa.patient_id "
        + "                           ) cd4 "
        + "WHERE cd4.patient_id NOT IN ( "
        + "    SELECT pa.patient_id "
        + "    FROM "
        + "        patient pa "
        + "            INNER JOIN encounter enc "
        + "                       ON enc.patient_id =  pa.patient_id "
        + "            INNER JOIN obs "
        + "                       ON obs.encounter_id = enc.encounter_id "
        + "            INNER JOIN ( "
        + "                                          SELECT pa.patient_id, MAX(enc.encounter_datetime) AS last_cd4 "
        + "                                          FROM "
        + "                                              patient pa "
        + "                                                  INNER JOIN encounter enc "
        + "                                                             ON enc.patient_id =  pa.patient_id "
        + "                                                  INNER JOIN obs "
        + "                                                             ON obs.encounter_id = enc.encounter_id "
        + "                                          WHERE  pa.voided = 0 "
        + "                                            AND enc.voided = 0 "
        + "                                            AND obs.voided = 0 "
        + "                                            AND enc.encounter_type = ${6} "
        + "                                            AND ( "
        + "                                                  (obs.concept_id = ${1695} AND obs.value_numeric IS NOT NULL) "
        + "                                                  OR "
        + "                                                  (obs.concept_id = ${730} AND obs.value_numeric IS NOT NULL) "
        + "                                              OR ( obs.concept_id = ${165515} AND obs.value_coded IS NOT NULL ) "
        + "                                                ) "
        + "                                            AND enc.encounter_datetime <= DATE_SUB(:endDate, INTERVAL 12 MONTH) "
        + "                                            AND enc.location_id = :location "
        + "                                          GROUP BY pa.patient_id "
        + "            ) cd4_date ON cd4_date.patient_id = pa.patient_id "
        + "    WHERE  pa.voided = 0 "
        + "      AND enc.voided = 0 "
        + "      AND obs.voided = 0 "
        + "      AND enc.encounter_type = ${6} "
        + "      AND ( "
        + "        (obs.concept_id = ${1695} AND obs.value_numeric IS NOT NULL) "
        + "            OR "
        + "        (obs.concept_id = ${730} AND obs.value_numeric IS NOT NULL) "
        + "         OR ( obs.concept_id = ${165515} AND obs.value_coded IS NOT NULL ) "
        + "        ) "
        + "      AND enc.encounter_datetime >= DATE_ADD(cd4_date.last_cd4, INTERVAL 1 DAY) "
        + "      AND enc.encounter_datetime <= :generationDate "
        + "      AND enc.location_id = :location "
        + "      AND pa.patient_id = cd4.patient_id "
        + "    GROUP BY pa.patient_id "
        + ")";
  }

  private String getC6Query() {
    return "SELECT pregnancy.patient_id FROM ( "
        + "                                     SELECT p.patient_id, MIN(e.encounter_datetime) AS pregnancy_date "
        + "                                     FROM patient p "
        + "                                              INNER JOIN person pr "
        + "                                                         ON p.patient_id = pr.person_id "
        + "                                              INNER JOIN encounter e "
        + "                                                         ON e.patient_id=p.patient_id "
        + "                                              INNER JOIN obs o "
        + "                                                         ON o.encounter_id=e.encounter_id "
        + "                                     WHERE p.voided = 0 "
        + "                                       AND e.voided=0 "
        + "                                       AND o.voided=0 "
        + "                                       AND e.location_id=:location "
        + "                                       AND e.encounter_type= ${6} "
        + "                                       AND o.concept_id = ${1982} "
        + "                                       AND o.value_coded= ${1065} "
        + "                                       AND pr.gender = 'F' "
        + "                                       AND e.encounter_datetime >= DATE_SUB(:endDate, INTERVAL 9 MONTH) "
        + "                                       AND e.encounter_datetime <= :endDate "
        + "                                     GROUP BY p.patient_id "
        + "                                 ) pregnancy "
        + "WHERE pregnancy.patient_id NOT IN ( "
        + "    SELECT pa.patient_id "
        + "    FROM "
        + "        patient pa "
        + "            INNER JOIN encounter enc "
        + "                       ON enc.patient_id =  pa.patient_id "
        + "            INNER JOIN obs "
        + "                       ON obs.encounter_id = enc.encounter_id "
        + "            INNER JOIN ( "
        + "                                     SELECT p.patient_id, MIN(e.encounter_datetime) AS pregnancy_date "
        + "                                     FROM patient p "
        + "                                              INNER JOIN person pr "
        + "                                                         ON p.patient_id = pr.person_id "
        + "                                              INNER JOIN encounter e "
        + "                                                         ON e.patient_id=p.patient_id "
        + "                                              INNER JOIN obs o "
        + "                                                         ON o.encounter_id=e.encounter_id "
        + "                                     WHERE p.voided = 0 "
        + "                                       AND e.voided=0 "
        + "                                       AND o.voided=0 "
        + "                                       AND e.location_id=:location "
        + "                                       AND e.encounter_type= ${6} "
        + "                                       AND o.concept_id = ${1982} "
        + "                                       AND o.value_coded= ${1065} "
        + "                                       AND pr.gender = 'F' "
        + "                                       AND e.encounter_datetime >= DATE_SUB(:endDate, INTERVAL 9 MONTH) "
        + "                                       AND e.encounter_datetime <= :endDate "
        + "                                     GROUP BY p.patient_id "
        + "            ) pregnant ON pregnant.patient_id = pa.patient_id "
        + "    WHERE  pa.voided = 0 "
        + "      AND enc.voided = 0 "
        + "      AND obs.voided = 0 "
        + "      AND enc.encounter_type = ${6} "
        + "      AND ( "
        + "        (obs.concept_id = ${1695} "
        + "            AND obs.value_numeric IS NOT NULL) "
        + "            OR "
        + "        (obs.concept_id = ${730} "
        + "            AND obs.value_numeric IS NOT NULL ) "
        + "        OR ( obs.concept_id = ${165515} AND obs.value_coded IS NOT NULL ) "
        + "        ) "
        + "      AND enc.encounter_datetime >= pregnant.pregnancy_date "
        + "      AND enc.encounter_datetime <= :generationDate "
        + "      AND enc.location_id = :location "
        + "      AND pa.patient_id = pregnancy.patient_id "
        + "    GROUP BY pa.patient_id "
        + ") ";
  }

  /**
   * <b> Data do Último CD4 </b>
   * <li>A data do registo mais recente de resultado de CD4 (absoluto) ocorrido até o fim do período
   *     de avaliação, registado na “Ficha Clínica – Ficha Mestra” ou “Ficha e-Lab” ou “Ficha de
   *     Laboratório”
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getLastCd4ResultDate(List<Integer> encounterTypes) {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Resultado do Último CD4 Absoluto");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, String> map = new HashMap<>();
    map.put("encounterTypes", StringUtils.join(encounterTypes, ","));
    map.put("1695", String.valueOf(hivMetadata.getCD4AbsoluteOBSConcept().getConceptId()));
    map.put("730", String.valueOf(hivMetadata.getCD4PercentConcept().getConceptId()));
    map.put("165515", String.valueOf(hivMetadata.getCD4SemiQuantitativeConcept().getConceptId()));
    map.put(
        "165513",
        String.valueOf(hivMetadata.getCD4CountLessThanOrEqualTo200Concept().getConceptId()));

    String query =
        " SELECT result.person_id, Max(result.most_recent) AS most_recent FROM ( "
            + getPatientsWithCD4AbsoluteResultOnPeriodQuery()
            + " ) result GROUP BY result.person_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b> Resultado do Último CD4 </b>
   * <li>O registo mais recente de resultado de CD4 (absoluto) ocorrido até o fim do período de
   *     avaliação, registado na “Ficha Clínica – Ficha Mestra” ou “Ficha e-Lab” ou “Ficha de
   *     Laboratório”
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getLastCd4Result(List<Integer> encounterTypes) {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Resultado do Último CD4 Absoluto");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, String> map = new HashMap<>();
    map.put("encounterTypes", StringUtils.join(encounterTypes, ","));
    map.put("1695", String.valueOf(hivMetadata.getCD4AbsoluteOBSConcept().getConceptId()));
    map.put("730", String.valueOf(hivMetadata.getCD4PercentConcept().getConceptId()));
    map.put("165515", String.valueOf(hivMetadata.getCD4SemiQuantitativeConcept().getConceptId()));
    map.put(
        "165513",
        String.valueOf(hivMetadata.getCD4CountLessThanOrEqualTo200Concept().getConceptId()));

    String query =
        " SELECT ps.person_id, IF(o.concept_id = ${165515}, o.value_coded, o.value_numeric) AS cd4_result "
            + " FROM   person ps "
            + "       INNER JOIN encounter e "
            + "               ON ps.person_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + " INNER JOIN ( "
            + " SELECT result.person_id, Max(result.most_recent) AS most_recent FROM ( "
            + getPatientsWithCD4AbsoluteResultOnPeriodQuery()
            + " ) result GROUP BY result.person_id "
            + " ) last_cd4 ON last_cd4.person_id = ps.person_id "
            + "WHERE  ps.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + " AND e.encounter_type IN ( ${encounterTypes} ) "
            + " AND ( ( o.concept_id = ${1695} "
            + "         AND o.value_numeric IS NOT NULL ) "
            + "   OR ( o.concept_id = ${730} "
            + "        AND o.value_numeric IS NOT NULL ) "
            + "   OR ( o.concept_id = ${165515} "
            + "        AND o.value_coded IS NOT NULL ) ) "
            + " AND DATE(e.encounter_datetime) = last_cd4.most_recent "
            + " AND e.location_id = :location";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b> Data do Resultado do Penúltimo CD4 </b>
   * <li>A data do registo que antecede o registo mais recente de resultado de CD4 (absoluto)
   *     ocorrido até o fim do período de avaliação, registado na “Ficha Clínica – Ficha Mestra” ou
   *     “Ficha e-Lab” ou “Ficha de Laboratório”
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getLastCd4ResultDateBeforeMostRecentCd4(List<Integer> encounterTypes) {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Data do Resultado do Penúltimo CD4 Absoluto");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, String> map = new HashMap<>();
    map.put("encounterTypes", StringUtils.join(encounterTypes, ","));
    map.put("1695", String.valueOf(hivMetadata.getCD4AbsoluteOBSConcept().getConceptId()));
    map.put("730", String.valueOf(hivMetadata.getCD4PercentConcept().getConceptId()));
    map.put("165515", String.valueOf(hivMetadata.getCD4SemiQuantitativeConcept().getConceptId()));
    map.put(
        "165513",
        String.valueOf(hivMetadata.getCD4CountLessThanOrEqualTo200Concept().getConceptId()));
    String query =
        " SELECT result.person_id, MAX(result.second_cd4_result) FROM ( "
            + getLastCd4OrResultDateBeforeMostRecentCd4()
            + " ) result GROUP BY result.person_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b> Resultado do Penúltimo CD4 </b>
   * <li>O registo que antecede o registo mais recente de resultado de CD4 (absoluto) ocorrido até o
   *     fim do período de avaliação, registado na “Ficha Clínica – Ficha Mestra” ou “Ficha e-Lab”
   *     ou “Ficha de Laboratório”
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getLastCd4ResultBeforeMostRecentCd4(List<Integer> encounterTypes) {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Resultado do Penúltimo CD4 Absoluto");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, String> map = new HashMap<>();
    map.put("encounterTypes", StringUtils.join(encounterTypes, ","));
    map.put("1695", String.valueOf(hivMetadata.getCD4AbsoluteOBSConcept().getConceptId()));
    map.put("730", String.valueOf(hivMetadata.getCD4PercentConcept().getConceptId()));
    map.put("165515", String.valueOf(hivMetadata.getCD4SemiQuantitativeConcept().getConceptId()));
    map.put(
        "165513",
        String.valueOf(hivMetadata.getCD4CountLessThanOrEqualTo200Concept().getConceptId()));

    String query =
        " SELECT result.person_id, result.cd4_result FROM ( "
            + getLastCd4OrResultDateBeforeMostRecentCd4()
            + " ) result ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>Data da Última Carga Viral</b>
   * <li>A data do registo mais recente de resultado de Carga Viral (quantitativo) na “Ficha Clínica
   *     – Ficha Mestra” ou “Ficha e-Lab” ou “Ficha de Laboratório”, ocorrido até o fim do período
   *     de avaliação
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getMostRecentVLResultDate(List<Integer> encounterTypes) {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Data da Última Carga Viral");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, String> map = new HashMap<>();
    map.put("encounterTypes", StringUtils.join(encounterTypes, ","));
    map.put("856", String.valueOf(hivMetadata.getHivViralLoadConcept().getConceptId()));
    map.put("1305", String.valueOf(hivMetadata.getHivViralLoadQualitative().getConceptId()));

    String query =
        " SELECT result_date.patient_id, MAX(result_date.most_recent) FROM ( "
            + getVLoadResultAndMostRecent()
            + " ) AS result_date GROUP BY result_date.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>Resultado da Última Carga Viral</b>
   * <li>O registo mais recente de resultado do Carga Viral (quantitativo), na “Ficha Clínica –
   *     Ficha Mestra” ou “Ficha e-Lab” ou “Ficha de Laboratório”, ocorrido até o fim do período de
   *     avaliação,
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getMostRecentVLResult(List<Integer> encounterTypes) {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Resultado da Última Carga Viral");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, String> map = new HashMap<>();
    map.put("encounterTypes", StringUtils.join(encounterTypes, ","));
    map.put("856", String.valueOf(hivMetadata.getHivViralLoadConcept().getConceptId()));
    map.put("1305", String.valueOf(hivMetadata.getHivViralLoadQualitative().getConceptId()));

    String query =
        " SELECT vl_result.patient_id, vl_result.viral_load FROM ( "
            + getVLoadResultAndMostRecent()
            + " ) AS vl_result GROUP BY vl_result.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>Data da Penúltima Carga Viral</b>
   * <li>A data do registo que antecede o registo mais recente de Carga Viral (quantitativo) na
   *     “Ficha Clínica – Ficha Mestra” ou “Ficha e-Lab” ou “Ficha de Laboratório”, ocorrido até o
   *     fim do período de avaliação
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getLastVLResultDateBeforeMostRecentVLResultDate(
      List<Integer> encounterTypes) {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Resultado da Penúltima Carga Viral");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, String> map = new HashMap<>();
    map.put("encounterTypes", StringUtils.join(encounterTypes, ","));
    map.put("856", String.valueOf(hivMetadata.getHivViralLoadConcept().getConceptId()));
    map.put("1305", String.valueOf(hivMetadata.getHivViralLoadQualitative().getConceptId()));

    String query =
        " SELECT second_result_date.patient_id, MAX(second_result_date.second_vl) FROM ( "
            + getSecondVLResultOrResultDateBeforeMostRecent()
            + " ) AS second_result_date GROUP BY second_result_date.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>Resultado da Penúltima Carga Viral</b>
   * <li>O registro que antecede o registo mais recente de resultado de Carga Viral (quantitativo)
   *     na “Ficha Clínica – Ficha Mestra” ou “Ficha e-Lab” ou “Ficha de Laboratório”, ocorrido até
   *     o fim do período de avaliação
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getLastVLResultBeforeMostRecentVLResultDate(List<Integer> encounterTypes) {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Resultado da Penúltima Carga Viral");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, String> map = new HashMap<>();
    map.put("encounterTypes", StringUtils.join(encounterTypes, ","));
    map.put("856", String.valueOf(hivMetadata.getHivViralLoadConcept().getConceptId()));
    map.put("1305", String.valueOf(hivMetadata.getHivViralLoadQualitative().getConceptId()));

    String query =
        " SELECT second_result.patient_id, second_result.viral_load FROM ( "
            + getSecondVLResultOrResultDateBeforeMostRecent()
            + " ) AS second_result GROUP BY second_result.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  private String getLastCd4OrResultDateBeforeMostRecentCd4() {
    return " SELECT ps.person_id, IF(o.concept_id = ${165515}, o.value_coded, o.value_numeric) AS cd4_result, DATE(last_cd4.second_date) AS second_cd4_result "
        + " FROM   person ps "
        + "       INNER JOIN encounter e "
        + "               ON ps.person_id = e.patient_id "
        + "       INNER JOIN obs o "
        + "               ON e.encounter_id = o.encounter_id "
        + " INNER JOIN ( "
        + " SELECT second.person_id, MAX(second.cd4_result) as second_date FROM ( "
        + getLastCd4OrResultDateBeforeMostRecentQuery()
        + " ) second GROUP BY second.person_id "
        + " ) last_cd4 ON last_cd4.person_id = ps.person_id "
        + "WHERE  ps.voided = 0 "
        + "       AND e.voided = 0 "
        + "       AND o.voided = 0 "
        + "       AND e.encounter_type IN ( ${encounterTypes} ) "
        + "  AND ( (o.concept_id = ${1695} "
        + "         AND o.value_numeric IS NOT NULL ) "
        + "   OR ( o.concept_id = ${730} "
        + "        AND o.value_numeric IS NOT NULL ) "
        + "   OR ( o.concept_id = ${165515} "
        + "        AND o.value_coded IS NOT NULL ) ) "
        + "             AND DATE(e.encounter_datetime) = last_cd4.second_date "
        + "       AND e.location_id = :location"
        + "       GROUP BY ps.person_id ";
  }

  private String getLastCd4OrResultDateBeforeMostRecentQuery() {
    return " SELECT ps.person_id, MAX(DATE(e.encounter_datetime)) AS cd4_result "
        + " FROM   person ps "
        + "       INNER JOIN encounter e "
        + "               ON ps.person_id = e.patient_id "
        + "       INNER JOIN obs o "
        + "               ON e.encounter_id = o.encounter_id "
        + " INNER JOIN ( "
        + " SELECT result.person_id, Max(result.most_recent) 'AS most_recent FROM ( "
        + getPatientsWithCD4AbsoluteResultOnPeriodQuery()
        + " ) result GROUP BY result.person_id ) "
        + " last_cd4 ON last_cd4.person_id = ps.person_id "
        + "WHERE  ps.voided = 0 "
        + "       AND e.voided = 0 "
        + "       AND o.voided = 0 "
        + "       AND e.encounter_type IN ( ${encounterTypes} ) "
        + "  AND ( (o.concept_id = ${1695} "
        + "         AND o.value_numeric IS NOT NULL ) "
        + "   OR ( o.concept_id = ${730} "
        + "        AND o.value_numeric IS NOT NULL ) "
        + "   OR ( o.concept_id = ${165515} "
        + "        AND o.value_coded IS NOT NULL ) ) "
        + "             AND DATE(e.encounter_datetime) < last_cd4.most_recent  "
        + "       AND e.location_id = :location"
        + "       GROUP BY ps.person_id ";
  }

  /**
   *
   * <li>Utentes com registo do resultado de CD4 (absoluto) na “Ficha Clínica – Ficha Mestra” ou
   *     “Ficha e-Lab” ou “Ficha de Laboratório” até o fim do período de avaliação (“Data Resultado
   *     CD4” <= “Data Fim Avaliação”)
   *
   * @return {@link String}
   */
  private String getPatientsWithCD4AbsoluteResultOnPeriodQuery() {

    return " SELECT ps.person_id, Max(DATE(e.encounter_datetime)) AS most_recent "
        + " FROM   person ps "
        + "       INNER JOIN encounter e "
        + "               ON ps.person_id = e.patient_id "
        + "       INNER JOIN obs o "
        + "               ON e.encounter_id = o.encounter_id "
        + "WHERE  ps.voided = 0 "
        + "       AND e.voided = 0 "
        + "       AND o.voided = 0 "
        + "       AND e.encounter_type IN ( ${encounterTypes} ) "
        + "       AND ( ( o.concept_id = ${1695} "
        + "             AND o.value_numeric IS NOT NULL ) "
        + "        OR ( o.concept_id = ${730} "
        + "             AND o.value_numeric IS NOT NULL ) "
        + "        OR ( o.concept_id = ${165515} "
        + "             AND o.value_coded IS NOT NULL ) ) "
        + "      AND DATE(e.encounter_datetime) <= :endDate "
        + "      AND e.location_id = :location "
        + " GROUP BY ps.person_id ";
  }

  private String getVLoadResultAndMostRecent() {
    return "SELECT p.patient_id, "
        + "       last_vl.most_recent, "
        + "  IF(o.concept_id = 856, o.value_numeric, (IF(o2.value_coded = 165331, CONCAT('MENOR QUE ',o2.comments), (IF(e.encounter_type = 51 and o2.value_coded=1306, 'NIVEL DE DETECÇÃO BAIXO',(IF(e.encounter_type in (6,9) and o2.value_coded=1306, 'NIVEL BAIXO DE DETECÇÃO',o2.value_coded)))) )) ) AS "
        + "                          viral_load "
        + "FROM   patient p "
        + "       INNER JOIN encounter e "
        + "               ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o "
        + "               ON e.encounter_id = o.encounter_id "
        + "       INNER JOIN obs o2 "
        + "               ON e.encounter_id = o2.encounter_id "
        + "       INNER JOIN ("
        + " SELECT result.patient_id, MAX(result.most_recent) as most_recent FROM ( "
        + "                   SELECT p.patient_id, "
        + "                          Max(DATE(e.encounter_datetime)) AS most_recent "
        + "                   FROM   patient p "
        + "                          INNER JOIN encounter e "
        + "                                  ON e.patient_id = p.patient_id "
        + "                          INNER JOIN obs o "
        + "                                  ON o.encounter_id = e.encounter_id "
        + "                   WHERE  e.encounter_type IN( ${encounterTypes} ) "
        + "                          AND ( ( o.concept_id = ${856} "
        + "                                  AND o.value_numeric IS NOT NULL ) "
        + "                                 OR ( o.concept_id = ${1305} "
        + "                                      AND o.value_coded IS NOT NULL ) ) "
        + "                          AND DATE(e.encounter_datetime) <= :endDate "
        + "                          AND e.location_id = :location "
        + "                          AND e.voided = 0 "
        + "                          AND p.voided = 0 "
        + "                          AND o.voided = 0 "
        + "                   GROUP  BY p.patient_id"
        + "    ) result GROUP BY result.patient_id "
        + ") last_vl "
        + "               ON last_vl.patient_id = p.patient_id "
        + "WHERE  e.encounter_type IN( ${encounterTypes} ) "
        + "       AND ( ( o.concept_id = ${856} "
        + "               AND o.value_numeric IS NOT NULL ) "
        + "              OR ( o2.concept_id = ${1305} "
        + "                   AND o2.value_coded IS NOT NULL ) ) "
        + "       AND DATE(e.encounter_datetime) = last_vl.most_recent "
        + "       AND e.location_id = :location "
        + "       AND e.voided = 0 "
        + "       AND p.voided = 0 "
        + "       AND o.voided = 0 "
        + "       AND o2.voided = 0 "
        + "GROUP  BY p.patient_id";
  }

  public String getSecondVLResultOrResultDateBeforeMostRecent() {
    return "SELECT p.patient_id, "
        + "       last_vl.last_vl AS second_vl, "
        + "  IF(o.concept_id = 856, o.value_numeric, (IF(o.value_coded = 165331, CONCAT('MENOR QUE ',o.comments), (IF(e.encounter_type = 51 and o.value_coded=1306, 'NIVEL DE DETECÇÃO BAIXO',(IF(e.encounter_type in (6,9) and o.value_coded=1306, 'NIVEL BAIXO DE DETECÇÃO',o.value_coded)))) )) ) AS "
        + "                          viral_load "
        + "FROM   patient p "
        + "       INNER JOIN encounter e "
        + "               ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o "
        + "               ON e.encounter_id = o.encounter_id "
        + "       INNER JOIN ("
        + " SELECT result_date.patient_id, MAX(result_date.second_vl_date) as last_vl FROM ( "
        + getMostRecentVLBeforeLastVLDate()
        + " ) AS result_date GROUP BY result_date.patient_id "
        + ") last_vl "
        + "               ON last_vl.patient_id = p.patient_id "
        + "WHERE  e.encounter_type IN( ${encounterTypes} ) "
        + "       AND ( ( o.concept_id = ${856} "
        + "               AND o.value_numeric IS NOT NULL ) "
        + "              OR ( o.concept_id = ${1305} "
        + "                   AND o.value_coded IS NOT NULL ) ) "
        + "       AND DATE(e.encounter_datetime) = last_vl.last_vl "
        + "       AND e.location_id = :location "
        + "       AND e.voided = 0 "
        + "       AND p.voided = 0 "
        + "       AND o.voided = 0 "
        + "GROUP  BY p.patient_id";
  }

  private String getMostRecentVLBeforeLastVLDate() {
    return "SELECT p.patient_id, "
        + "       MAX(DATE(e.encounter_datetime)) AS second_vl_date "
        + "FROM   patient p "
        + "       INNER JOIN encounter e "
        + "               ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o "
        + "               ON e.encounter_id = o.encounter_id "
        + "       INNER JOIN ("
        + " SELECT result.patient_id, MAX(result.most_recent) as most_recent FROM ( "
        + "                   SELECT p.patient_id, "
        + "                          Max(DATE(e.encounter_datetime)) AS most_recent "
        + "                   FROM   patient p "
        + "                          INNER JOIN encounter e "
        + "                                  ON e.patient_id = p.patient_id "
        + "                          INNER JOIN obs o "
        + "                                  ON o.encounter_id = e.encounter_id "
        + "                   WHERE  e.encounter_type IN( ${encounterTypes} ) "
        + "                          AND ( ( o.concept_id = ${856} "
        + "                                  AND o.value_numeric IS NOT NULL ) "
        + "                                 OR ( o.concept_id = ${1305} "
        + "                                      AND o.value_coded IS NOT NULL ) ) "
        + "                          AND DATE(e.encounter_datetime) <= :endDate "
        + "                          AND e.location_id = :location "
        + "                          AND e.voided = 0 "
        + "                          AND p.voided = 0 "
        + "                          AND o.voided = 0 "
        + "                   GROUP  BY p.patient_id"
        + "    ) result GROUP BY result.patient_id "
        + ") last_vl "
        + "               ON last_vl.patient_id = p.patient_id "
        + "WHERE  e.encounter_type IN( ${encounterTypes} ) "
        + "       AND ( ( o.concept_id = ${856} "
        + "               AND o.value_numeric IS NOT NULL ) "
        + "              OR ( o.concept_id = ${1305} "
        + "                   AND o.value_coded IS NOT NULL ) ) "
        + "       AND DATE(e.encounter_datetime) < last_vl.most_recent "
        + "       AND e.location_id = :location "
        + "       AND e.voided = 0 "
        + "       AND p.voided = 0 "
        + "       AND o.voided = 0 "
        + "GROUP  BY p.patient_id";
  }
}
