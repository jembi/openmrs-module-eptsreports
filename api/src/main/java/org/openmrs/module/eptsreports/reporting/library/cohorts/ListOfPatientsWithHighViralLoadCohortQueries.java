package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.library.queries.CommonQueries;
import org.openmrs.module.eptsreports.reporting.library.queries.HighViralLoadQueries;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsWithHighViralLoadCohortQueries {

  private final HivMetadata hivMetadata;

  private final CommonMetadata commonMetadata;

  private final CommonQueries commonQueries;

  @Autowired
  public ListOfPatientsWithHighViralLoadCohortQueries(
      HivMetadata hivMetadata, CommonMetadata commonMetadata, CommonQueries commonQueries) {
    this.hivMetadata = hivMetadata;
    this.commonMetadata = commonMetadata;
    this.commonQueries = commonQueries;
  }

  public DataDefinition getPatientCell() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    String query =
        ""
            + "SELECT address.patient_id, address.celula "
            + "             FROM   (SELECT p.patient_id,pa.address3 celula "
            + "                     FROM   patient p "
            + "                            INNER JOIN person pr ON p.patient_id = pr.person_id "
            + "                            INNER JOIN person_address pa ON pa.person_id = pr.person_id "
            + "                     WHERE  p.voided = 0 "
            + "                            AND pr.voided = 0 "
            + "                     ORDER  BY pa.person_address_id DESC) address "
            + "             GROUP  BY address.patient_id";

    spdd.setQuery(query);

    return spdd;
  }

  /**
   * <b>Data de Colheita da amostra com CV>1000 cp/ml (Sheet 1: Column M)</b>
   *
   * <p>Date of Sample collection recorded in Laboratory or FSR Form with the VL result > 1000 cp/ml
   * with the VL result date occurred during the reporting period. <br>
   * Note: if more than one VL result > 1000 cp/ml are registered during the period the first one
   * should be considered
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getVLSampleCollectionDate() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Data de Colheita da amostra com CV>1000 cp/ml");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("23821", commonMetadata.getSampleCollectionDateAndTime().getConceptId());

    String query =
        " SELECT p.patient_id, "
            + "       Min(o2.value_datetime) AS collection_date "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON p.patient_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + "       INNER JOIN obs o2 "
            + "                 ON e.encounter_id = o2.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND o2.voided = 0 "
            + "       AND e.encounter_type IN ( ${13}, ${51} ) "
            + "       AND (o.concept_id = ${856} "
            + "       AND o.value_numeric > 1000) "
            + "       AND e.location_id = :location "
            + "       AND (o2.concept_id = ${23821}"
            + "       AND o2.value_datetime >= :startDate "
            + "       AND o2.value_datetime <= :endDate) "
            + "GROUP  BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Data de Recepção do Resultado de CV na US (Sheet 1: Column N)</b>
   *
   * <p>Date of the reception of the VL result > 1000 cp/ml registered in Laboratory or FSR form
   * during the reporting period (as First High Viral Load Result Date). <br>
   * Note: if more than one VL result > 1000 cp/ml are registered during the period the first one
   * should be considered
   *
   * @param resultDate Result Date Flag to return Result Date or VL result
   * @return {@link DataDefinition}
   */
  public DataDefinition getVLResultReceptionDate(boolean resultDate) {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName(
        "Data de Recepção do Resultado de CV na US / Resultado da Primeira CV Alta (cp/ml)");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String query = " SELECT p.patient_id, ";
    if (resultDate) {
      query += "       Min(e.encounter_datetime) AS result_date ";
    } else {
      query += "       o.value_numeric AS vl_result ";
    }
    query +=
        "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON p.patient_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type IN ( ${13}, ${51} ) "
            + "       AND o.concept_id = ${856} "
            + "       AND o.value_numeric > 1000 "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_datetime >= :startDate "
            + "       AND e.encounter_datetime <= :endDate "
            + "GROUP  BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Linha ART na Data da CV Alta (Sheet 1: Column P)</b>
   *
   * <ul>
   *   <li>“Primeira Linha”, for all patients who are on 1st Line of ART Regimen (HVL_FR4) and have
   *       the First High Viral Load Result Date registered in FSR or Laboratory form during the
   *       reporting period or
   *   <li>“Segunda Linha”, for all patients who are on 2nd Line of ART Regimen (HVL_FR5) and have
   *       the First High Viral Load Result Date registered in FSR or Laboratory form during the
   *       reporting period
   * </ul>
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getArtLineOnHighVLResultDate() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Linha ART na Data da CV Alta");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("21187", hivMetadata.getRegArvSecondLine().getConceptId());
    valuesMap.put("21188", hivMetadata.getRegArvThirdLine().getConceptId());

    String arvStart = commonQueries.getARTStartDate(true);

    String query =
        " SELECT first_line.patient_id, 'Primeira Linha' FROM ( "
            + "                SELECT patient_id FROM ( "
            + arvStart
            + "                 ) initiated_art"
            + "                    WHERE initiated_art.first_pickup BETWEEN :startDate AND :endDate "
            + "               AND initiated_art.patient_id NOT IN ( "
            + "                             SELECT regimen_lines.patient_id "
            + "                             FROM   (SELECT p.patient_id, "
            + "                                            Max(o.obs_datetime) AS recent_date "
            + "                                     FROM   patient p "
            + "                                            INNER JOIN encounter e "
            + "                                                    ON e.patient_id = p.patient_id "
            + "                                            INNER JOIN obs o "
            + "                                                    ON o.encounter_id = e.encounter_id "
            + "                                     WHERE  e.voided = 0 "
            + "                                            AND p.voided = 0 "
            + "                                            AND o.voided = 0 "
            + "                                            AND e.encounter_type = ${53} "
            + "                                            AND e.location_id = :location "
            + "                                            AND o.concept_id IN ( ${21187}, ${21188} ) "
            + "                                            AND o.value_coded IS NOT NULL "
            + "                                            AND o.obs_datetime <= :endDate "
            + "                                     GROUP  BY p.patient_id) regimen_lines "
            + "                             WHERE  regimen_lines.patient_id = initiated_art.patient_id"
            + "                )) first_line "
            + "INNER JOIN ( "
            + "         SELECT first_vl.patient_id FROM ( "
            + "                   SELECT p.patient_id, MIN(e.encounter_datetime) AS result_date "
            + "                   FROM "
            + "                       patient p INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                                 INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                   WHERE p.voided = 0 AND e.voided = 0 AND o.voided = 0 "
            + "                     AND e.encounter_type IN (${13}, ${51}) "
            + "                     AND o.concept_id = ${856} "
            + "                     AND o.value_numeric > 1000 "
            + "                     AND e.encounter_datetime >= :startDate "
            + "                     AND e.encounter_datetime <= :endDate "
            + "                     AND e.location_id = :location "
            + "                   GROUP BY p.patient_id "
            + "                  ) first_vl "
            + "          )vl "
            + "     ON vl.patient_id = first_line.patient_id "
            + "GROUP BY first_line.patient_id "
            + " UNION "
            + "SELECT second_line.patient_id, 'Segunda Linha' FROM ( "
            + "       SELECT patient_id FROM ( "
            + arvStart
            + "                      ) initiated_art"
            + "                        WHERE initiated_art.first_pickup BETWEEN :startDate AND :endDate "
            + "                   AND initiated_art.patient_id IN ( "
            + "                               SELECT regimen_lines.patient_id "
            + "                               FROM   (SELECT p.patient_id, "
            + "                                              Max(o.obs_datetime) AS recent_date "
            + "                                       FROM   patient p "
            + "                                              INNER JOIN encounter e "
            + "                                                      ON e.patient_id = p.patient_id "
            + "                                              INNER JOIN obs o "
            + "                                                      ON o.encounter_id = e.encounter_id "
            + "                                       WHERE  e.voided = 0 "
            + "                                              AND p.voided = 0 "
            + "                                              AND o.voided = 0 "
            + "                                              AND e.encounter_type = ${53} "
            + "                                              AND e.location_id = :location "
            + "                                              AND o.concept_id = ${21187} "
            + "                                              AND o.value_coded IS NOT NULL "
            + "                                              AND o.obs_datetime <= :endDate "
            + "                                       GROUP  BY p.patient_id) regimen_lines "
            + "                               WHERE  regimen_lines.patient_id = initiated_art.patient_id "
            + "                    ))second_line "
            + " GROUP BY second_line.patient_id ";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Data da Consulta Clínica Ocorrida/Registada no Sistema (Sheet 1: Column Q) / Data da
   * Consulta de APSS/PP ocorrida/registada no Sistema (Sheet 1: Column S)</b>
   *
   * <p>The first Clinical Consultation Date registered in Ficha Clinica between the First High
   * Viral Load Result Date (value of column O) and report end date <br>
   *
   * <p>The date of first APSS/PP Consultation Date registered in Ficha APSS/PP between the First
   * High Viral Load Result Date (value of column O) and report end date
   *
   * <p>Note: If a patient has more than one clinical consultation registered on the same date the
   * system will show from the most recently entered consultation in the system on that specific
   * day. For Patients who do not have any consultation registered during the period evaluated, the
   * corresponding column will be filled with N/A.
   *
   * @param clinicalConsultation Type of consultation flag to change between Clinical and APSS
   *     encounter types
   * @return {@link DataDefinition}
   */
  public DataDefinition getFirstRegisteredClinicalOrApssConsultationAfterHighVlResultDate(
      boolean clinicalConsultation) {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Data da Consulta Clínica/APSS/PP Ocorrida/Registada no Sistema");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    String query =
        " SELECT p.patient_id, MIN(e.encounter_datetime) as first_consultation "
            + "FROM patient p INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "               INNER JOIN ( "
            + "    SELECT p.patient_id, MIN(e.encounter_datetime) AS result_date "
            + "    FROM "
            + "        patient p INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                  INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "    WHERE p.voided = 0 AND e.voided = 0 AND o.voided = 0 "
            + "      AND e.encounter_type IN (${13}, ${51}) "
            + "      AND o.concept_id = ${856} "
            + "      AND o.value_numeric > 1000 "
            + "      AND e.encounter_datetime >= :startDate "
            + "      AND e.encounter_datetime <= :endDate "
            + "      AND e.location_id = :location "
            + "    GROUP BY p.patient_id "
            + ") vl_result on p.patient_id = vl_result.patient_id "
            + "WHERE p.voided = 0 AND e.voided = 0 ";
    if (clinicalConsultation) {
      query += "  AND e.encounter_type = ${6} ";
    } else {
      query += "  AND e.encounter_type = ${35} ";
    }
    query +=
        "  AND e.location_id = :location "
            + "  AND e.encounter_datetime BETWEEN vl_result.result_date AND :endDate "
            + "GROUP BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Data da Prevista da Consulta (Sheet 1: Column R) / Data da Prevista da Consulta APSS/PP
   * (Sheet 1: Column T)</b>
   *
   * <p>The system will calculate the expected Consultation Date as follows: Expected Clinical
   * Consultation Date = First High Viral Load Result Date (value of Column N) + 7 days
   *
   * <p>The system will calculate the expected APSS/PP Consultation Date as follows: Expected
   * APSS/PP Session 0 Consultation Date = First High Viral Load Result Date (value of Column N) + 7
   * days
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getExpectedClinicalOrApssConsultationDate() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Data da Prevista da Consulta Clinical Consultation / APSS/PP Session 0");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String query =
        " SELECT p.patient_id, DATE_ADD(MIN(e.encounter_datetime), interval 7 day) AS expected_date "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON p.patient_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type IN ( ${13}, ${51} ) "
            + "       AND o.concept_id = ${856} "
            + "       AND o.value_numeric > 1000 "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_datetime >= :startDate "
            + "       AND e.encounter_datetime <= :endDate "
            + "GROUP  BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Data da Consulta de APSS/PP ocorrida/registada no Sistema (Sheet 1: Column U)</b>
   *
   * <p>The date of first APSS/PP Consultation registered in Ficha APSS/PP between (after) the
   * Session 0 APSS/PP Consultation Date (value of column S) and report end date
   *
   * <p>Note: Note: For Patients who do not have any APSS/PP consultation registered during the
   * period evaluated, the corresponding column will be filled with N/A.
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getFirstRegisteredApssAfterApssSessionZeroConsultationDate() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Data da Consulta APSS/PP Ocorrida/Registada no Sistema Após a Sessão 0");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    String query =
        "SELECT p.patient_id, "
            + "       Min(e.encounter_datetime) AS first_session_date "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON p.patient_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + "       INNER JOIN ( "
            + HighViralLoadQueries.getSessionZeroQuery()
            + " ) apss_session_zero "
            + "       ON p.patient_id = apss_session_zero.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${35}"
            + "       AND e.location_id = :location  "
            + "       AND e.encounter_datetime > apss_session_zero.session_zero_date "
            + "       AND e.encounter_datetime <= :endDate "
            + "GROUP BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Data da Prevista da Consulta APSS/PP (Sheet 1: Column V)</b>
   *
   * <p>The system will calculate the expected APSS/PP Consultation Date as follows: Expected 1st
   * APSS/PP Consultation Date = APSS/PP Session 0 Consultation Date (HVL_FR15 - value of column S)
   * + 30 days Note: Note: For Patients who do not have any APSS/PP consultation registered during
   * the period evaluated, the corresponding column will be filled with N/A.
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getExpectedApssSessionOneConsultationDate() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Data da Prevista da Consulta APSS/PP Sessão 0");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    String query =
        " SELECT apss_session_zero.patient_id, "
            + "      DATE_ADD(apss_session_zero.session_zero_date, INTERVAL 30 DAY) AS expected_first_session_date "
            + "FROM   ( "
            + HighViralLoadQueries.getSessionZeroQuery()
            + " ) apss_session_zero "
            + "GROUP  BY apss_session_zero.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Data da Consulta de APSS/PP ocorrida/registada no Sistema (Sheet 1: Column W)</b>
   *
   * <p>The date of first APSS/PP Consultation registered in Ficha APSS/PP between the 1st APSS/PP
   * Consultation Date (HVL_FR16 - value of column U) and report end date
   *
   * <p>Note: Note: For Patients who do not have any APSS/PP consultation registered during the
   * period evaluated, the corresponding column will be filled with N/A.
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getFirstRegisteredApssAfterApssSessionOneConsultationDate() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Data da Consulta APSS/PP Ocorrida/Registada no Sistema Após a Sessão 1");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    String query =
        "SELECT p.patient_id, "
            + "       Min(e.encounter_datetime) AS second_session_date "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON p.patient_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + "       INNER JOIN ( "
            + HighViralLoadQueries.getSessionOneQuery()
            + "              ) apss_session_one "
            + "       ON p.patient_id = apss_session_one.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${35} "
            + "       AND e.location_id = :location  "
            + "       AND e.encounter_datetime > apss_session_one.first_session_date "
            + "       AND e.encounter_datetime <= :endDate "
            + "GROUP BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Data da Prevista da Consulta APSS/PP (Sheet 1: Column X)</b>
   *
   * <p>The system will calculate the expected APSS/PP Consultation Date as follows: Expected 2nd
   * APSS/PP Consultation Date = 1st APSS/PP Consultation Date (HVL_FR16 - value of column U) + 30
   * days
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getExpectedApssSessionTwoConsultationDate() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Data da Prevista da Consulta APSS/PP Após a Sessão 1");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    String query =
        " SELECT apss_session_zero.patient_id, "
            + "      DATE_ADD(apss_session_zero.first_session_date, INTERVAL 30 DAY) AS expected_second_session_date "
            + "FROM   ( "
            + HighViralLoadQueries.getSessionOneQuery()
            + " ) apss_session_zero "
            + "GROUP  BY apss_session_zero.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Data da Consulta de APSS/PP ocorrida/registada no Sistema (Sheet 1: Column Y)</b>
   *
   * <p>The date of first APSS/PP Consultation registered in Ficha APSS/PP between the 2nd APSS/PP
   * Consultation Date (HVL_FR17- value of column W) and report end date
   *
   * <p>Note: Note: For Patients who do not have any APSS/PP consultation registered during the
   * period evaluated, the corresponding column will be filled with N/A.
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getFirstRegisteredApssAfterApssSessionOTwoConsultationDate() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Data da Consulta APSS/PP Ocorrida/Registada no Sistema Após a Sessão 2");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    String query =
        "SELECT p.patient_id, "
            + "       Min(e.encounter_datetime) AS third_session_date "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON p.patient_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + "       INNER JOIN ( "
            + HighViralLoadQueries.getSessionTwoQuery()
            + "        ) apss_session_two "
            + "           ON p.patient_id = apss_session_two.patient_id "
            + " WHERE  p.voided = 0 "
            + "        AND e.voided = 0 "
            + "        AND o.voided = 0 "
            + "        AND e.encounter_type = ${35} "
            + "        AND e.location_id = :location  "
            + "        AND e.encounter_datetime > apss_session_two.second_session_date "
            + "        AND e.encounter_datetime <= :endDate "
            + " GROUP BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Data da Prevista da Consulta APSS/PP (Sheet 1: Column Z)</b>
   *
   * <p>The system will calculate the expected APSS/PP Consultation Date as follows: Expected 3rd
   * APSS/PP Consultation Date = 2nd APSS/PP Consultation Date (HVL_FR17- value of column W) + 30
   * days
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getExpectedApssSessionThreeConsultationDate() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Data da Prevista da Consulta APSS/PP Após a Sessão 2");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    String query =
        " SELECT session_two.patient_id, "
            + "      DATE_ADD(session_two.second_session_date, INTERVAL 30 DAY) AS expected_third_session_date "
            + "FROM   ( "
            + HighViralLoadQueries.getSessionOneQuery()
            + " ) session_two "
            + "GROUP  BY session_two.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b> Classificação da aderência depois das Consultas APSS (Sheet 1: Column AA)</b>
   *
   * <p>The system will identify and show the patient’s “ADESÃO ao TARV – Boa, Risco, Má, Dias de
   * atraso na toma ARVs?” registered on their most recent Ficha APSS/PP between (Session 0 (value
   * of Column S), 1st APSS/PP Consultation (value of Column U), 2nd APSS/PP Consultation (value of
   * column W) and 3rd APSS/PP consultation (value of Column Y)) with the following possible values:
   *
   * <ul>
   *   <li>Boa for Patients marked as “B” on “ADESÃO ao TARV – Boa, Risco, Má, Dias de atraso na
   *       toma ARVs?” on the most recent “Ficha APPS/PP” identified or
   *   <li>Risco for Patients marked as “R” on “ADESÃO ao TARV – Boa, Risco, Má, Dias de atraso na
   *       toma ARVs?” on the most recent “Ficha APPS/PP” identified or
   *   <li>Ma for Patients marked as “M” on “ADESÃO ao TARV – Boa, Risco, Má, Dias de atraso na toma
   *       ARVs?” on the most recent “Ficha APPS/PP” identified
   * </ul>
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getAdherenceEvaluation() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Classificação da aderência depois das Consultas APSS (Boa, Risco ou Má)");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("6223", hivMetadata.getAdherenceEvaluationConcept().getConceptId());
    valuesMap.put("1383", hivMetadata.getPatientIsDead().getConceptId());
    valuesMap.put("1385", hivMetadata.getBadConcept().getConceptId());
    valuesMap.put("1749", hivMetadata.getArvAdherenceRiskConcept().getConceptId());

    String query =
        "SELECT p.patient_id, "
            + "       o.value_coded "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON p.patient_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + "       INNER JOIN ( "
            + HighViralLoadQueries.getSessionZeroQuery()
            + "    ) session_zero "
            + "               ON p.patient_id = session_zero.patient_id "
            + "       INNER JOIN ( "
            + HighViralLoadQueries.getSessionThreeQuery()
            + " ) session_three "
            + "               ON session_three.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${35} "
            + "       AND o.concept_id = ${6223} "
            + "       AND o.value_coded IN ( ${1383}, ${1749}, ${1385} ) "
            + "       AND e.encounter_datetime >= session_zero.first_consultation "
            + "       AND e.encounter_datetime < session_three.third_session_date "
            + "       AND e.location_id = :location "
            + "GROUP  BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Data da Consulta da Clínica/pedido (Sheet 1: Column AB)</b>
   *
   * <p>The date of the first Clinical Consultation registered in Ficha Clinica with Carga Viral
   * marked in Investigações - Pedidos Laboratoriais between the 2nd APSS/PP Consultation Date and
   * report end date For Patients who do not have any clinical consultation registered during the
   * evaluated period, the corresponding column will be filled with N/A.
   *
   * @see #getFirstRegisteredApssAfterApssSessionOneConsultationDate
   * @return {@link DataDefinition}
   */
  public DataDefinition getRequestForLaboratoryInvestigationsAfterApssSessionTwo() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Data da Consulta da Clínica/pedido com Investigações - Pedidos Laboratoriais");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("6223", hivMetadata.getAdherenceEvaluationConcept().getConceptId());
    valuesMap.put("1383", hivMetadata.getPatientIsDead().getConceptId());
    valuesMap.put("1385", hivMetadata.getBadConcept().getConceptId());
    valuesMap.put("1749", hivMetadata.getArvAdherenceRiskConcept().getConceptId());
    valuesMap.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());

    String query =
        "SELECT p.patient_id, "
            + "       Min(e.encounter_datetime) AS expected_date "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON p.patient_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + "       INNER JOIN ( "
            + HighViralLoadQueries.getSessionTwoQuery()
            + "       ) session_two "
            + "               ON p.patient_id = session_two.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${6} "
            + "       AND o.concept_id = ${23722} "
            + "       AND o.value_coded = ${856} "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_datetime >= session_two.second_session_date "
            + "       AND e.encounter_datetime <= :endDate "
            + "GROUP  BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Data da Prevista da Consulta Clínica/Pedido (Sheet 1: Column AC)</b>
   *
   * <p>The system will calculate the expected Consultation Date as follows: VL Request Date = 2nd
   * APSS/PP Consultation Date (value of column W) + 30 days
   *
   * <p>Note: Note: For Patients who do not have any APSS/PP consultation registered during the
   * period evaluated, the corresponding column will be filled with N/A.
   *
   * @see #getFirstRegisteredApssAfterApssSessionOneConsultationDate
   * @return {@link DataDefinition}
   */
  public DataDefinition getExpectedClinicalConsultationDateAfterApssSessionTwoConsultationDate() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Data da Prevista da Consulta Clínica/Pedido");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    String query =
        " SELECT session_two.patient_id, "
            + "      DATE_ADD(session_two.second_session_date, INTERVAL 30 DAY) AS expected_date "
            + "FROM   ( "
            + HighViralLoadQueries.getSessionOneQuery()
            + " ) session_two "
            + "GROUP  BY session_two.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Data da Colheita Registada no Sistema (Sheet 1: Column AD)</b>
   *
   * <p>Date of Sample collection on the earliest VL result date registered in Laboratory Form or
   * FSR between the 2nd APSS/PP Consultation Date (value of column W) and report end date
   *
   * @see #getFirstRegisteredApssAfterApssSessionOTwoConsultationDate
   * @return {@link DataDefinition}
   */
  public DataDefinition getDateOfVLSampleCollectionAfterApssSessionTwoConsultationDate() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Data da Colheita Registada no Sistema");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("23821", commonMetadata.getSampleCollectionDateAndTime().getConceptId());
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    String query =
        " SELECT p.patient_id, "
            + "       Min(o.value_datetime) AS collection_date "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON p.patient_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + "INNER JOIN ( "
            + HighViralLoadQueries.getSessionTwoQuery()
            + "          ) session_two ON p.patient_id = session_two.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type IN ( ${13}, ${51} ) "
            + "       AND e.location_id = :location "
            + "       AND (o.concept_id = ${23821}"
            + "       AND o.value_datetime > session_two.second_session_date "
            + "       AND o.value_datetime <= :endDate) "
            + "GROUP  BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Data do Resultado da 2ª CV registada no Sistema (Sheet 1: Column AF)</b>
   *
   * <p>Date of the earliest Laboratory or FSR form with VL Result registered between the 3rd
   * APSS/PP Consultation Date (value of column Y) and report end date
   *
   * @see #getFirstRegisteredApssAfterApssSessionOneConsultationDate
   * @return {@link DataDefinition}
   */
  public DataDefinition getFirstLabOrFsrAfterApssSessionThreeConsultationDate() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Data do Resultado da 2ª CV registada no Sistema");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    String query = HighViralLoadQueries.getColumnFQuery(false);

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Data Prevista do Resultado da 2ª CV (Sheet 1: Column AG)</b>
   *
   * <p>The system will calculate the expected second VL result Date as follows: 2nd VL Result Date
   * = 3rd APSS/PP Consultation Date (value of column Y) + 30 days
   *
   * @see #getFirstRegisteredApssAfterApssSessionOneConsultationDate
   * @return {@link DataDefinition}
   */
  public DataDefinition getExpectedResultDateOfFirstLabOrFsrApssSessionThree() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Data Prevista do Resultado da 2ª CV");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    String query =
        " SELECT session_three.patient_id, "
            + "      DATE_ADD(session_three.third_session_date, INTERVAL 30 DAY) AS expected_date "
            + "FROM   ( "
            + HighViralLoadQueries.getSessionThreeQuery()
            + " ) session_three "
            + "GROUP  BY session_three.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Resultado da 2ª CV em cp/ml (Sheet 1: Column AH)</b>
   *
   * <p>VL Result registered in Laboratory or FSR on the 2nd Viral Load Result Date (HVL_FR22 -
   * value of column AF)
   *
   * <p>Note: For Patients who do not have any VL Result registered in Laboratory or FSR form
   * registered during the evaluated period, the corresponding column will be filled with N/A.
   *
   * @see #getExpectedResultDateOfFirstLabOrFsrApssSessionThree
   * @return {@link DataDefinition}
   */
  public DataDefinition getVLResultOfFirstLabOrFsrApssSessionThree() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Resultado da 2ª CV em cp/ml");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("23821", commonMetadata.getSampleCollectionDateAndTime().getConceptId());
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    String query =
        "SELECT p.patient_id, o.value_numeric "
            + "         FROM "
            + "             patient p INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                       INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                       INNER JOIN ( "
            + HighViralLoadQueries.getColumnFQuery(false)
            + "                                 ) af_date on af_date.patient_id = p.patient_id "
            + "        WHERE p.voided = 0 AND e.voided = 0 AND o.voided = 0 "
            + "          AND e.encounter_type IN (${13}, ${51}) "
            + "          AND o.concept_id = ${856} "
            + "          AND o.value_numeric IS NOT NULL "
            + "          AND e.encounter_datetime = af_date.result_date "
            + "          AND e.location_id = :location "
            + "        GROUP BY p.patient_id ";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Data da Consulta Clínica Ocorrida/Registada no Sistema (Sheet 1: Column AJ)</b>
   *
   * <p>The first Clinical Consultation Date registered in Ficha Clinica between the Second High
   * Viral Load Result (>1000 copies/ml) Date (value of column AF) and report end date
   *
   * <p>Note: If a patient has more than one clinical consultation registered on the same date the
   * system will show from the most recently entered consultation in the system on that specific
   * day. For Patients who do not have any consultation registered during the period evaluated, the
   * corresponding column will be filled with N/A.
   *
   * @see #getFirstLabOrFsrAfterApssSessionThreeConsultationDate
   * @return {@link DataDefinition}
   */
  public DataDefinition getFirstClinicalConsultationAfterSecondHighVLResult() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName(
        "Data da Consulta Clínica Ocorrida/Registada no Sistema Após a 2a Carga Viral alta");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("23821", commonMetadata.getSampleCollectionDateAndTime().getConceptId());
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    String query =
        "SELECT     p.patient_id, "
            + "           Min(e.encounter_datetime) AS first_consultation "
            + "FROM       patient p "
            + "INNER JOIN encounter e "
            + "ON         p.patient_id = e.patient_id "
            + "INNER JOIN obs o "
            + "ON         e.encounter_id = o.encounter_id "
            + "INNER JOIN "
            + "           ( "
            + "                      SELECT     p.patient_id, "
            + "                                 e.encounter_datetime AS result_date "
            + "                      FROM       patient p "
            + "                      INNER JOIN encounter e "
            + "                      ON         p.patient_id = e.patient_id "
            + "                      INNER JOIN obs o "
            + "                      ON         e.encounter_id = o.encounter_id "
            + "                      INNER JOIN "
            + "                                 ( "
            + HighViralLoadQueries.getColumnFQuery(false)
            + " ) af_date "
            + "                      ON         af_date.patient_id = p.patient_id "
            + "                      WHERE      p.voided = 0 "
            + "                      AND        e.voided = 0 "
            + "                      AND        o.voided = 0 "
            + "                      AND        e.encounter_type IN (${13}, "
            + "                                                      ${51}) "
            + "                      AND        o.concept_id = ${856} "
            + "                      AND        o.value_numeric >= 1000 "
            + "                      AND        e.encounter_datetime = af_date.result_date "
            + "                      AND        e.location_id = :location "
            + "                      GROUP BY   p.patient_id ) af_date "
            + "ON         af_date.patient_id = p.patient_id "
            + "WHERE      p.voided = 0 "
            + "AND        e.voided = 0 "
            + "AND        o.voided = 0 "
            + "AND        e.encounter_type = ${6} "
            + "AND        e.location_id = :location "
            + "AND        e.encounter_datetime > af_date.result_date "
            + "AND        e.encounter_datetime <= :endDate "
            + "GROUP BY   p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Data da Prevista da Consulta (Sheet 1: Column AK)</b>
   *
   * <p>The system will calculate the expected Consultation Date as follows: Expected Clinical
   * Consultation Date = Second High Viral Load Result Date (>1000 copies/ml) (HVL_FR22 - value of
   * column AF) + 30 days
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getExpectedClinicalConsultationDate() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Data da Prevista da Consulta");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("23821", commonMetadata.getSampleCollectionDateAndTime().getConceptId());
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    String query =
        " SELECT af_date.patient_id, "
            + "      DATE_ADD(af_date.result_date, INTERVAL 30 DAY) AS expected_date "
            + "FROM   ( "
            + HighViralLoadQueries.getColumnFQuery(true)
            + " ) af_date "
            + "GROUP  BY af_date.patient_id";

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Data de Início da Nova Linha (se aplicável) (Sheet 1: Column AL)</b>
   *
   * <p>The first Clinical Consultation Date registered in Ficha Clinica between the Second High
   * Viral Load Result (>1000 copies/ml) Date (HVL_FR22 - value of column AF) and report end date
   * with ``2a Linha`` OR ``3a Linha`` marked
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getNewLineInitiationDate() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Data de Início da Nova Linha (se aplicável)");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("23821", commonMetadata.getSampleCollectionDateAndTime().getConceptId());
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("21187", hivMetadata.getRegArvSecondLine().getConceptId());
    valuesMap.put("21188", hivMetadata.getRegArvThirdLine().getConceptId());

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    String query =
        "SELECT p.patient_id, MIN(e.encounter_datetime) as first_consultation "
            + "FROM patient p "
            + "         INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "         INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "        INNER JOIN ( "
            + HighViralLoadQueries.getColumnFQuery(true)
            + " ) af_date on p.patient_id = af_date.patient_id "
            + "WHERE p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND e.encounter_type = ${6} "
            + "  AND o.concept_id IN (${21187}, ${21188}) "
            + "  AND o.value_coded IS NOT NULL "
            + "  AND e.location_id = :location "
            + "  AND e.encounter_datetime > af_date.result_date "
            + "  AND e.encounter_datetime <= :endDate "
            + "GROUP BY p.patient_id";

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Data da Consulta de APSS/PP ocorrida/registada no Sistema (Sheet 1: Column AM)</b>
   *
   * <p>The date of first APSS/PP Consultation Date registered in Ficha APSS/SS between the Second
   * High Viral Load Result Date (HVL_FR22 - value of column AF) and report end date
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getFirstApssConsultationAfterSecondHighVLResult() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Data da Consulta APSS/PP Ocorrida/Registada no Sistema Após a Segunda CV Alta");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    String query = HighViralLoadQueries.getApssSessionZero();

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Data Prevista da Consulta APSS/PP (Sheet 1: Column AN)</b>
   *
   * <p>Expected APSS/PP Session 0 Consultation Date = Second High Viral Load Result Date (HVL_FR22
   * - value of column AF) + 30 days
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getExpectedApssConsultationAfterSecondHighVLResult() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Data da Prevista da Consulta");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("23821", commonMetadata.getSampleCollectionDateAndTime().getConceptId());
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    String query =
        " SELECT af_date.patient_id, "
            + "      DATE_ADD(af_date.result_date, INTERVAL 30 DAY) AS expected_date "
            + "FROM   ( "
            + HighViralLoadQueries.getColumnFQuery(false)
            + " ) af_date "
            + "GROUP  BY af_date.patient_id";

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Data da Consulta de APSS/PP ocorrida/registada no Sistema (Sheet 1: Column AO)</b>
   *
   * <p>The date of first APSS/PP Consultation registered in Ficha APSS/PP between the APSS/PP
   * Session 0 After Second High VL Consultation Date (HVL_FR27 - value of column AM) and report end
   * date
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getFirstApssConsultationAfterFirstApssSessionZero() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Data da Consulta APSS/PP Ocorrida/Registada no Sistema Após Sessao 0");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    String query = HighViralLoadQueries.getApssSessionOne();

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Data da Prevista da Consulta APSS/PP (Sheet 1: Column AP)</b>
   *
   * <p>Expected 1st APSS/PP Consultation Date = APSS/PP Session 0 After Second High VL Consultation
   * Date (HVL_FR27 - value of column AM) + 30 days
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getExpectedFirstApssConsultationAfterFirstApssSessionZero() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Data da Prevista da Consulta APSS/PP apos Sessao 0");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("23821", commonMetadata.getSampleCollectionDateAndTime().getConceptId());
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    String query =
        " SELECT am_date.patient_id, "
            + "      DATE_ADD(am_date.apss_date, INTERVAL 30 DAY) AS expected_date "
            + "FROM   ( "
            + HighViralLoadQueries.getApssSessionZero()
            + " ) am_date "
            + "GROUP  BY am_date.patient_id";

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Data da Consulta de APSS/PP ocorrida/registada no Sistema (Sheet 1: Column AQ)</b>
   *
   * <p>The date of first APSS/PP Consultation registered in Ficha APSS/PP between the 1st APSS/PP
   * Consultation Date after second high VL (HVL_FR28 - value of column AO) and report end date
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getFirstApssConsultationAfterFirstApssSessionOne() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Data da Consulta APSS/PP Ocorrida/Registada no Sistema Após Sessao 1");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    String query = HighViralLoadQueries.getApssSessionTwo();

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Data da Prevista da Consulta APSS/PP (Sheet 1: Column AR)</b>
   *
   * <p>Expected 2nd APSS/PP Consultation Date = 1st APSS/PP Consultation Date after second high VL
   * (HVL_FR28 - value of column AO) + 30 days
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getExpectedFirstApssConsultationAfterFirstApssSessionOne() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Expected 2nd APSS/PP Consultation Date ");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("23821", commonMetadata.getSampleCollectionDateAndTime().getConceptId());
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    String query =
        " SELECT session_date.patient_id, "
            + "      DATE_ADD(session_date.apss_date, INTERVAL 30 DAY) AS expected_date "
            + "FROM   ( "
            + HighViralLoadQueries.getApssSessionOne()
            + " ) session_date "
            + "GROUP  BY session_date.patient_id";

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Data da Consulta de APSS/PP ocorrida/registada no Sistema (Sheet 1: Column AS)</b>
   *
   * <p>The date of first APSS/PP Consultation registered in Ficha APSS/PP between the 2nd APSS/PP
   * Consultation Date after second high VL (HVL_FR29- value of column AQ) and report end date
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getFirstApssConsultationAfterFirstApssSessionTwo() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Data da Consulta APSS/PP Ocorrida/Registada no Sistema Após Sessao 2");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    String query =
        "SELECT p.patient_id, "
            + "       Min(e.encounter_datetime) AS apss_date "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON p.patient_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + "        INNER JOIN ( "
            + HighViralLoadQueries.getApssSessionTwo()
            + " ) session_date on p.patient_id = session_date.patient_id "
            + " WHERE  p.voided = 0 "
            + "        AND e.voided = 0 "
            + "        AND o.voided = 0 "
            + "        AND e.encounter_type = ${35} "
            + "        AND e.location_id = :location  "
            + "        AND e.encounter_datetime > session_date.apss_date "
            + "        AND e.encounter_datetime <= :endDate "
            + " GROUP BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Data da Prevista da Consulta APSS/PP (Sheet 1: Column AT)</b>
   *
   * <p>Expected 3rd APSS/PP Consultation Date = 2nd APSS/PP Consultation Date after second high VL
   * (HVL_FR29- value of column AQ) + 30 days
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getExpectedFirstApssConsultationAfterFirstApssSessionTwo() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Expected 3rd APSS/PP Consultation Date ");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("23821", commonMetadata.getSampleCollectionDateAndTime().getConceptId());
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    String query =
        " SELECT session_date.patient_id, "
            + "      DATE_ADD(session_date.apss_date, INTERVAL 30 DAY) AS expected_date "
            + "FROM   ( "
            + HighViralLoadQueries.getApssSessionTwo()
            + " ) session_date "
            + "GROUP  BY session_date.patient_id";

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Data da Consulta da Clínica/pedido (Sheet 1: Column AU)</b>
   *
   * <p>The date of the first Clinical Consultation registered in Ficha Clinica with Carga Viral
   * marked in Investigações - Pedidos Laboratoriais between the 2nd APSS/PP Consultation Date after
   * second high VL (value of column AQ) and report end date
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getRequestForLaboratoryInvestigationsAfterSessionTwo() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName(
        "Data da Consulta da Clínica/pedido com Investigações - Pedidos Laboratoriais Após Segunda APSS");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put(
        "35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());

    String query =
        "SELECT p.patient_id, "
            + "       Min(e.encounter_datetime) AS expected_date "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON p.patient_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + "       INNER JOIN ( "
            + HighViralLoadQueries.getApssSessionTwo()
            + "       ) second_session "
            + "               ON p.patient_id = second_session.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${6} "
            + "       AND o.concept_id = ${23722} "
            + "       AND o.value_coded = ${856} "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_datetime >= second_session.apss_date "
            + "       AND e.encounter_datetime <= :endDate "
            + "GROUP  BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Data de Colheita da amostra com CV>1000 cp/ml (Sheet 1: Column M)</b>
   *
   * <p>Date of Sample collection recorded in Laboratory or FSR Form with the VL result > 1000 cp/ml
   * with the VL result date occurred during the reporting period. <br>
   * Note: if more than one VL result > 1000 cp/ml are registered during the period the first one
   * should be considered
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getVLSampleCollectionDateAfterApssSessionTwo() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Data de Colheita da amostra com CV>1000 cp/ml");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("23821", commonMetadata.getSampleCollectionDateAndTime().getConceptId());

    String query =
        " SELECT p.patient_id, "
            + "       Min(o2.value_datetime) AS collection_date "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON p.patient_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + "       INNER JOIN obs o2 "
            + "                 ON e.encounter_id = o2.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND o2.voided = 0 "
            + "       AND e.encounter_type IN ( ${13}, ${51} ) "
            + "       AND (o.concept_id = ${856} "
            + "       AND o.value_numeric > 1000) "
            + "       AND e.location_id = :location "
            + "       AND (o2.concept_id = ${23821}"
            + "       AND o2.value_datetime >= :startDate "
            + "       AND o2.value_datetime <= :endDate) "
            + "GROUP  BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }
}
