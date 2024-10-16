package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.library.queries.CommonQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PatientesWhoReceivedVlResultsCohortQueries {

  private HivMetadata hivMetadata;
  private ResumoMensalCohortQueries resumoMensalCohortQueries;
  private final CommonQueries commonQueries;
  private final TxCurrCohortQueries txCurrCohortQueries;
  private final GenericCohortQueries genericCohortQueries;

  @Autowired
  public PatientesWhoReceivedVlResultsCohortQueries(
      ResumoMensalCohortQueries resumoMensalCohortQueries,
      HivMetadata hivMetadata,
      CommonQueries commonQueries,
      TxCurrCohortQueries txCurrCohortQueries,
      GenericCohortQueries genericCohortQueries) {
    this.hivMetadata = hivMetadata;
    this.resumoMensalCohortQueries = resumoMensalCohortQueries;
    this.commonQueries = commonQueries;
    this.txCurrCohortQueries = txCurrCohortQueries;
    this.genericCohortQueries = genericCohortQueries;
  }

  public CohortDefinition getBaseCohort() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    String mapping1 = "startDate=${startDate},endDate=${startDate+1m-1d},location=${location}";
    String mapping2 = "startDate=${startDate+1m},endDate=${startDate+2m-1d},location=${location}";
    String mapping3 = "startDate=${startDate+2m},endDate=${endDate},location=${location}";

    CohortDefinition E2 =
        resumoMensalCohortQueries
            .getNumberOfActivePatientsInArtAtTheEndOfTheCurrentMonthHavingVlTestResults();

    compositionCohortDefinition.addSearch("L1", EptsReportUtils.map(E2, mapping1));
    compositionCohortDefinition.addSearch("L2", EptsReportUtils.map(E2, mapping2));
    compositionCohortDefinition.addSearch("L3", EptsReportUtils.map(E2, mapping3));

    compositionCohortDefinition.setCompositionString("L1 OR L2 OR L3");

    return compositionCohortDefinition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * CohortDefinition to apply the general base cohort to all indicators from DQA SESP Sheet
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getBaseCohortSesp(SespCompositionString compositionString) {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";
    String mappingM1 = "startDate=${startDate},endDate=${startDate+1m-1d},location=${location}";
    String mappingM2 = "startDate=${startDate+1m},endDate=${startDate+2m-1d},location=${location}";
    String mappingM3 = "startDate=${startDate+2m},endDate=${endDate},location=${location}";

    CohortDefinition ART_SISMA =
        resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13();

    CohortDefinition ART_DATIM =
        txCurrCohortQueries.getTxCurrCompositionCohort("compositionCohort", true);

    CohortDefinition ART_B1_M3 =
        resumoMensalCohortQueries.getPatientsWhoInitiatedTarvAtThisFacilityDuringCurrentMonthB1();

    CohortDefinition ART_B1_M2 =
        resumoMensalCohortQueries.getPatientsWhoInitiatedTarvAtThisFacilityDuringCurrentMonthB1();

    CohortDefinition ART_B1_M1 =
        resumoMensalCohortQueries.getPatientsWhoInitiatedTarvAtThisFacilityDuringCurrentMonthB1();

    CohortDefinition ART_E2_M3 =
        resumoMensalCohortQueries
            .getNumberOfActivePatientsInArtAtTheEndOfTheCurrentMonthHavingVlTestResults();

    CohortDefinition ART_E2_M2 =
        resumoMensalCohortQueries
            .getNumberOfActivePatientsInArtAtTheEndOfTheCurrentMonthHavingVlTestResults();

    CohortDefinition ART_E2_M1 =
        resumoMensalCohortQueries
            .getNumberOfActivePatientsInArtAtTheEndOfTheCurrentMonthHavingVlTestResults();

    CohortDefinition baseCohort = genericCohortQueries.getBaseCohort();

    compositionCohortDefinition.addSearch(
        "SISMA", EptsReportUtils.map(ART_SISMA, "endDate=${endDate},location=${location}"));
    compositionCohortDefinition.addSearch(
        "ARTDATIM", EptsReportUtils.map(ART_DATIM, "onOrBefore=${endDate},location=${location}"));
    compositionCohortDefinition.addSearch("B1M3", EptsReportUtils.map(ART_B1_M3, mappingM3));
    compositionCohortDefinition.addSearch("B1M2", EptsReportUtils.map(ART_B1_M2, mappingM2));
    compositionCohortDefinition.addSearch("B1M1", EptsReportUtils.map(ART_B1_M1, mappingM1));
    compositionCohortDefinition.addSearch("E2M3", EptsReportUtils.map(ART_E2_M3, mappingM3));
    compositionCohortDefinition.addSearch("E2M2", EptsReportUtils.map(ART_E2_M2, mappingM2));
    compositionCohortDefinition.addSearch("E2M1", EptsReportUtils.map(ART_E2_M1, mappingM1));
    compositionCohortDefinition.addSearch(
        "baseCohort", EptsReportUtils.map(baseCohort, "endDate=${endDate},location=${location}"));

    compositionCohortDefinition.setCompositionString(
        "baseCohort AND " + compositionString.getCompositionString());

    return compositionCohortDefinition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * This Enum will return the right composition depending on the content of the compositionString
   * variable on SespBaseCohort
   *
   * </blockquote>
   */
  public enum SespCompositionString {
    SISMA {
      @Override
      public String getCompositionString() {
        return "SISMA";
      }
    },
    ARTDATIM {
      @Override
      public String getCompositionString() {
        return "ARTDATIM";
      }
    },
    B1M3 {
      @Override
      public String getCompositionString() {
        return "B1M3";
      }
    },
    B1M2 {
      @Override
      public String getCompositionString() {
        return "B1M2";
      }
    },
    B1M1 {
      @Override
      public String getCompositionString() {
        return "B1M1";
      }
    },
    E2M3 {
      @Override
      public String getCompositionString() {
        return "E2M3";
      }
    },

    E2M2 {
      @Override
      public String getCompositionString() {
        return "E2M2";
      }
    },

    E2M1 {
      @Override
      public String getCompositionString() {
        return "E2M1";
      }
    };

    public abstract String getCompositionString();
  }

  public DataDefinition getNID(int identifierType) {
    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();
    spdd.setName("NID");

    String sql =
        " SELECT p.patient_id,pi.identifier  FROM patient p INNER JOIN patient_identifier pi ON p.patient_id=pi.patient_id "
            + " INNER JOIN patient_identifier_type pit ON pit.patient_identifier_type_id=pi.identifier_type "
            + " WHERE p.voided=0 AND pi.voided=0 AND pit.retired=0 AND pit.patient_identifier_type_id ="
            + identifierType;

    spdd.setQuery(sql);
    return spdd;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <p>Patient ART Start Date is the oldest date from the set of criterias defined in the common
   * query: 1/1 Patients who initiated ART and ART Start Date as earliest from the following
   * criterias is by End of the period (reporting endDate)
   *
   * </blockquote>
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getArtStartDate() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("3 - ART Start Date  ");
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Location.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Location.class));

    String query = commonQueries.getARTStartDate(true);
    sqlPatientDataDefinition.setQuery(query);

    return sqlPatientDataDefinition;
  }

  public DataDefinition getAge(String calculateAgeOn) {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    if (calculateAgeOn.equals("startDate")) {
      spdd.setName("Patient Age at reporting startDate");
      spdd.addParameter(new Parameter("startDate", "startDate", Date.class));
    } else if (calculateAgeOn.equals("endDate")) {
      spdd.setName("Patient Age at reporting endDate");
      spdd.addParameter(new Parameter("endDate", "endDate", Date.class));
    } else {
      spdd.setName("Patient Age at reporting evaluation Date");
      spdd.addParameter(new Parameter("evaluationDate", "evaluationDate", Date.class));
    }

    Map<String, Integer> valuesMap = new HashMap<>();

    String sql = "";
    if (calculateAgeOn.equals("startDate")) {
      sql += " SELECT p.patient_id, ROUND(DATEDIFF(:startDate,ps.birthdate)/365) AS age ";
    } else if (calculateAgeOn.equals("endDate")) {
      sql += " SELECT p.patient_id, ROUND(DATEDIFF(:endDate,ps.birthdate)/365) AS age ";
    } else {
      sql += " SELECT p.patient_id, ROUND(DATEDIFF(:evaluationDate,ps.birthdate)/365) AS age ";
    }
    sql +=
        " FROM patient p "
            + " INNER JOIN person ps ON p.patient_id = ps.person_id "
            + " WHERE p.voided=0"
            + " AND ps.voided=0";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(sql));
    return spdd;
  }

  public DataDefinition getDataNotificouCV() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Data de Consulta onde Notificou o Resultado de CV");
    spdd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "End Date", Date.class));
    spdd.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();

    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        "SELECT patient_id, MIN(first_result_date) AS lab_result_date "
            + "            FROM( "
            + "               SELECT p.patient_id, Min(e.encounter_datetime) AS first_result_date "
            + "               FROM   patient p "
            + "                   INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                   INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "               WHERE p.voided = 0 "
            + "                 AND e.voided = 0 "
            + "                 AND o.voided = 0 "
            + "                 AND e.location_id = :location "
            + "                 AND e.encounter_type = ${6} "
            + "                 AND o.concept_id = ${856} "
            + "                 AND o.value_numeric IS NOT NULL "
            + "                 AND e.encounter_datetime >= :startDate "
            + "                 AND e.encounter_datetime <= DATE_SUB(DATE_ADD(:startDate, INTERVAL 1 MONTH), INTERVAL 1 DAY) "
            + "               GROUP  BY p.patient_id "
            + "               UNION "
            + "               SELECT p.patient_id, Min(e.encounter_datetime) AS first_result_date "
            + "               FROM patient p "
            + "                        INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                        INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                        INNER JOIN concept_name cn ON cn.concept_id = o.value_coded "
            + "                        INNER JOIN ( "
            + "                   SELECT p.patient_id, Min(e.encounter_datetime) AS first_encounter_date "
            + "                   FROM patient p "
            + "                            INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                            INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                   WHERE p.voided = 0 "
            + "                     AND e.voided = 0 "
            + "                     AND o.voided = 0 "
            + "                     AND e.location_id = :location "
            + "                     AND e.encounter_type = ${6} "
            + "                     AND o.concept_id = ${1305} "
            + "                     AND o.value_coded IS NOT NULL "
            + "                     AND e.encounter_datetime >= :startDate "
            + "                     AND e.encounter_datetime <= DATE_SUB(DATE_ADD(:startDate, INTERVAL 1 MONTH), INTERVAL 1 DAY) "
            + "                   GROUP BY p.patient_id "
            + "               ) first_encounter ON first_encounter.patient_id = p.patient_id "
            + "               WHERE p.voided = 0 "
            + "                 AND e.voided = 0 "
            + "                 AND o.voided = 0 "
            + "                 AND cn.voided = 0 "
            + "                 AND e.location_id = :location "
            + "                 AND e.encounter_type = ${6} "
            + "                 AND o.concept_id = ${1305} "
            + "                 AND o.value_coded IS NOT NULL "
            + "                 AND e.encounter_datetime >= :startDate "
            + "                 AND e.encounter_datetime <= DATE_SUB(DATE_ADD(:startDate, INTERVAL 1 MONTH), INTERVAL 1 DAY) "
            + "                 AND first_encounter.first_encounter_date = e.encounter_datetime "
            + "                 AND cn.locale = 'pt' "
            + "               GROUP BY p.patient_id "
            + "               UNION "
            + "            SELECT p.patient_id, Min(e.encounter_datetime) AS first_result_date "
            + "            FROM   patient p "
            + "                   INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                   INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "            WHERE  p.voided = 0 "
            + "                   AND e.voided = 0 "
            + "                   AND o.voided = 0 "
            + "                   AND e.location_id = :location "
            + "                   AND e.encounter_type = ${6} "
            + "                   AND o.concept_id = ${856} "
            + "                   AND o.value_numeric IS NOT NULL "
            + "                   AND e.encounter_datetime >= DATE_ADD(:startDate, INTERVAL 1 MONTH) "
            + "                   AND e.encounter_datetime <= DATE_SUB(DATE_ADD(:startDate, INTERVAL 2 MONTH), INTERVAL 1 DAY) "
            + "            GROUP  BY p.patient_id "
            + "            UNION "
            + "               SELECT p.patient_id, Min(e.encounter_datetime) AS first_result_date "
            + "               FROM patient p "
            + "                        INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                        INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                        INNER JOIN concept_name cn ON cn.concept_id = o.value_coded "
            + "                        INNER JOIN ( "
            + "                   SELECT p.patient_id, Min(e.encounter_datetime) AS first_encounter_date "
            + "                   FROM patient p "
            + "                            INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                            INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                   WHERE p.voided = 0 "
            + "                     AND e.voided = 0 "
            + "                     AND o.voided = 0 "
            + "                     AND e.location_id = :location "
            + "                     AND e.encounter_type = ${6} "
            + "                     AND o.concept_id = ${1305} "
            + "                     AND o.value_coded IS NOT NULL "
            + "                     AND e.encounter_datetime >= DATE_ADD(:startDate, INTERVAL 1 MONTH) "
            + "                     AND e.encounter_datetime <= DATE_SUB(DATE_ADD(:startDate, INTERVAL 2 MONTH), INTERVAL 1 DAY) "
            + "                   GROUP BY p.patient_id "
            + "               ) first_encounter ON first_encounter.patient_id = p.patient_id "
            + "               WHERE p.voided = 0 "
            + "                 AND e.voided = 0 "
            + "                 AND o.voided = 0 "
            + "                 AND cn.voided = 0 "
            + "                 AND e.location_id = :location "
            + "                 AND e.encounter_type = ${6} "
            + "                 AND o.concept_id = ${1305} "
            + "                 AND o.value_coded IS NOT NULL "
            + "                 AND e.encounter_datetime >= DATE_ADD(:startDate, INTERVAL 1 MONTH) "
            + "                 AND e.encounter_datetime <= DATE_SUB(DATE_ADD(:startDate, INTERVAL 2 MONTH), INTERVAL 1 DAY) "
            + "                 AND first_encounter.first_encounter_date = e.encounter_datetime "
            + "                 AND cn.locale = 'pt' "
            + "               GROUP BY p.patient_id "
            + "            UNION "
            + "            SELECT p.patient_id, Min(e.encounter_datetime) AS first_result_date "
            + "            FROM   patient p "
            + "                   INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                   INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "            WHERE  p.voided = 0 "
            + "                   AND e.voided = 0 "
            + "                   AND o.voided = 0 "
            + "                   AND e.location_id = :location "
            + "                   AND e.encounter_type = ${6} "
            + "                   AND o.concept_id = ${856} "
            + "                   AND o.value_numeric IS NOT NULL "
            + "                   AND e.encounter_datetime >= DATE_ADD(:startDate, INTERVAL 2 MONTH) "
            + "                   AND e.encounter_datetime <= :endDate "
            + "            GROUP  BY p.patient_id "
            + "            UNION "
            + "                   SELECT p.patient_id, Min(e.encounter_datetime) AS first_result_date "
            + "               FROM patient p "
            + "                   INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                   INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                   INNER JOIN concept_name cn ON cn.concept_id = o.value_coded "
            + "                   INNER JOIN ( "
            + "                   SELECT p.patient_id, Min(e.encounter_datetime) AS first_encounter_date "
            + "                   FROM patient p "
            + "                   INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                   INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "                   WHERE p.voided = 0 "
            + "                   AND e.voided = 0 "
            + "                   AND o.voided = 0 "
            + "                   AND e.location_id = :location "
            + "                   AND e.encounter_type = ${6} "
            + "                   AND o.concept_id = ${1305} "
            + "                   AND o.value_coded IS NOT NULL "
            + "                   AND e.encounter_datetime >= DATE_ADD(:startDate, INTERVAL 2 MONTH) "
            + "                   AND e.encounter_datetime <= :endDate "
            + "                   GROUP BY p.patient_id "
            + "                   ) first_encounter ON first_encounter.patient_id = p.patient_id "
            + "               WHERE p.voided = 0 "
            + "                 AND e.voided = 0 "
            + "                 AND o.voided = 0 "
            + "                 AND cn.voided = 0 "
            + "                 AND e.location_id = :location "
            + "                 AND e.encounter_type = 6 "
            + "                 AND o.concept_id = ${1305} "
            + "                 AND o.value_coded IS NOT NULL "
            + "                 AND e.encounter_datetime >= DATE_ADD(:startDate, INTERVAL 2 MONTH) "
            + "                 AND e.encounter_datetime <= :endDate "
            + "                 AND first_encounter.first_encounter_date = e.encounter_datetime "
            + "                 AND cn.locale = 'pt' "
            + "               GROUP BY p.patient_id "
            + "            ) vl GROUP BY vl.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(map);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  public DataDefinition getViralLoadQuantitativeResults() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Resultado da Carga Viral (Resultado Quantitativo)");
    spdd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "End Date", Date.class));
    spdd.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();

    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        "SELECT "
            + "    p.patient_id, "
            + "    o.value_numeric AS first_vl_result "
            + "FROM "
            + "    patient p "
            + "        INNER JOIN "
            + "    encounter e ON p.patient_id = e.patient_id "
            + "        INNER JOIN "
            + "    obs o ON e.encounter_id = o.encounter_id "
            + "        INNER JOIN ( "
            + "        SELECT "
            + "            patient_id, "
            + "            MIN(first_result_date) AS lab_result_date "
            + "        FROM ( "
            + "                 SELECT "
            + "                     p.patient_id, "
            + "                     MIN(e.encounter_datetime) AS first_result_date "
            + "                 FROM "
            + "                     patient p "
            + "                         INNER JOIN "
            + "                     encounter e ON p.patient_id = e.patient_id "
            + "                         INNER JOIN "
            + "                     obs o ON e.encounter_id = o.encounter_id "
            + "                 WHERE "
            + "                     p.voided = 0 "
            + "                   AND e.voided = 0 "
            + "                   AND o.voided = 0 "
            + "                   AND e.location_id = :location "
            + "                   AND e.encounter_type = ${6} "
            + "                   AND o.concept_id = ${856} "
            + "                   AND o.value_numeric IS NOT NULL "
            + "                   AND e.encounter_datetime BETWEEN :startDate AND DATE_SUB(DATE_ADD(:startDate, INTERVAL 1 MONTH), INTERVAL 1 DAY) "
            + "                 GROUP BY "
            + "                     p.patient_id "
            + " "
            + "                 UNION "
            + " "
            + "                 SELECT "
            + "                     p.patient_id, "
            + "                     MIN(e.encounter_datetime) AS first_result_date "
            + "                 FROM "
            + "                     patient p "
            + "                         INNER JOIN "
            + "                     encounter e ON p.patient_id = e.patient_id "
            + "                         INNER JOIN "
            + "                     obs o ON e.encounter_id = o.encounter_id "
            + "                         INNER JOIN "
            + "                     concept_name cn ON cn.concept_id = o.value_coded "
            + "                         INNER JOIN ( "
            + "                         SELECT "
            + "                             p.patient_id, "
            + "                             MIN(e.encounter_datetime) AS first_encounter_date "
            + "                         FROM "
            + "                             patient p "
            + "                                 INNER JOIN "
            + "                             encounter e ON p.patient_id = e.patient_id "
            + "                                 INNER JOIN "
            + "                             obs o ON e.encounter_id = o.encounter_id "
            + "                         WHERE "
            + "                             p.voided = 0 "
            + "                           AND e.voided = 0 "
            + "                           AND o.voided = 0 "
            + "                           AND e.location_id = :location "
            + "                           AND e.encounter_type = ${6} "
            + "                           AND o.concept_id = ${1305} "
            + "                           AND o.value_coded IS NOT NULL "
            + "                           AND e.encounter_datetime BETWEEN :startDate AND DATE_SUB(DATE_ADD(:startDate, INTERVAL 1 MONTH), INTERVAL 1 DAY) "
            + "                         GROUP BY "
            + "                             p.patient_id "
            + "                     ) first_encounter ON first_encounter.patient_id = p.patient_id "
            + "                 WHERE "
            + "                     p.voided = 0 "
            + "                   AND e.voided = 0 "
            + "                   AND o.voided = 0 "
            + "                   AND cn.voided = 0 "
            + "                   AND e.location_id = :location "
            + "                   AND e.encounter_type = ${6} "
            + "                   AND o.concept_id = ${1305} "
            + "                   AND o.value_coded IS NOT NULL "
            + "                   AND e.encounter_datetime BETWEEN :startDate AND DATE_SUB(DATE_ADD(:startDate, INTERVAL 1 MONTH), INTERVAL 1 DAY) "
            + "                   AND first_encounter.first_encounter_date = e.encounter_datetime "
            + "                   AND cn.locale = 'pt' "
            + "                 GROUP BY "
            + "                     p.patient_id "
            + " "
            + "                 UNION "
            + " "
            + "                 SELECT "
            + "                     p.patient_id, "
            + "                     MIN(e.encounter_datetime) AS first_result_date "
            + "                 FROM "
            + "                     patient p "
            + "                         INNER JOIN "
            + "                     encounter e ON p.patient_id = e.patient_id "
            + "                         INNER JOIN "
            + "                     obs o ON e.encounter_id = o.encounter_id "
            + "                 WHERE "
            + "                     p.voided = 0 "
            + "                   AND e.voided = 0 "
            + "                   AND o.voided = 0 "
            + "                   AND e.location_id = :location "
            + "                   AND e.encounter_type = ${6} "
            + "                   AND o.concept_id = ${856} "
            + "                   AND o.value_numeric IS NOT NULL "
            + "                   AND e.encounter_datetime BETWEEN DATE_ADD(:startDate, INTERVAL 1 MONTH) AND DATE_SUB(DATE_ADD(:startDate, INTERVAL 2 MONTH), INTERVAL 1 DAY) "
            + "                 GROUP BY "
            + "                     p.patient_id "
            + " "
            + "                 UNION "
            + " "
            + "                 SELECT "
            + "                     p.patient_id, "
            + "                     MIN(e.encounter_datetime) AS first_result_date "
            + "                 FROM "
            + "                     patient p "
            + "                         INNER JOIN "
            + "                     encounter e ON p.patient_id = e.patient_id "
            + "                         INNER JOIN "
            + "                     obs o ON e.encounter_id = o.encounter_id "
            + "                         INNER JOIN "
            + "                     concept_name cn ON cn.concept_id = o.value_coded "
            + "                         INNER JOIN ( "
            + "                         SELECT "
            + "                             p.patient_id, "
            + "                             MIN(e.encounter_datetime) AS first_encounter_date "
            + "                         FROM "
            + "                             patient p "
            + "                                 INNER JOIN "
            + "                             encounter e ON p.patient_id = e.patient_id "
            + "                                 INNER JOIN "
            + "                             obs o ON e.encounter_id = o.encounter_id "
            + "                         WHERE "
            + "                             p.voided = 0 "
            + "                           AND e.voided = 0 "
            + "                           AND o.voided = 0 "
            + "                           AND e.location_id = :location "
            + "                           AND e.encounter_type = ${6} "
            + "                           AND o.concept_id = ${1305} "
            + "                           AND o.value_coded IS NOT NULL "
            + "                           AND e.encounter_datetime BETWEEN DATE_ADD(:startDate, INTERVAL 1 MONTH) AND DATE_SUB(DATE_ADD(:startDate, INTERVAL 2 MONTH), INTERVAL 1 DAY) "
            + "                         GROUP BY "
            + "                             p.patient_id "
            + "                     ) first_encounter ON first_encounter.patient_id = p.patient_id "
            + "                 WHERE "
            + "                     p.voided = 0 "
            + "                   AND e.voided = 0 "
            + "                   AND o.voided = 0 "
            + "                   AND cn.voided = 0 "
            + "                   AND e.location_id = :location "
            + "                   AND e.encounter_type = ${6} "
            + "                   AND o.concept_id = ${1305} "
            + "                   AND o.value_coded IS NOT NULL "
            + "                   AND e.encounter_datetime BETWEEN DATE_ADD(:startDate, INTERVAL 1 MONTH) AND DATE_SUB(DATE_ADD(:startDate, INTERVAL 2 MONTH), INTERVAL 1 DAY) "
            + "                   AND first_encounter.first_encounter_date = e.encounter_datetime "
            + "                   AND cn.locale = 'pt' "
            + "                 GROUP BY "
            + "                     p.patient_id "
            + " "
            + "                 UNION "
            + " "
            + "                 SELECT "
            + "                     p.patient_id, "
            + "                     MIN(e.encounter_datetime) AS first_result_date "
            + "                 FROM "
            + "                     patient p "
            + "                         INNER JOIN "
            + "                     encounter e ON p.patient_id = e.patient_id "
            + "                         INNER JOIN "
            + "                     obs o ON e.encounter_id = o.encounter_id "
            + "                 WHERE "
            + "                     p.voided = 0 "
            + "                   AND e.voided = 0 "
            + "                   AND o.voided = 0 "
            + "                   AND e.location_id = :location "
            + "                   AND e.encounter_type = ${6} "
            + "                   AND o.concept_id = ${856} "
            + "                   AND o.value_numeric IS NOT NULL "
            + "                   AND e.encounter_datetime BETWEEN DATE_ADD(:startDate, INTERVAL 2 MONTH) AND :endDate "
            + "                 GROUP BY "
            + "                     p.patient_id "
            + " "
            + "                 UNION "
            + " "
            + "                 SELECT "
            + "                     p.patient_id, "
            + "                     MIN(e.encounter_datetime) AS first_result_date "
            + "                 FROM "
            + "                     patient p "
            + "                         INNER JOIN "
            + "                     encounter e ON p.patient_id = e.patient_id "
            + "                         INNER JOIN "
            + "                     obs o ON e.encounter_id = o.encounter_id "
            + "                         INNER JOIN "
            + "                     concept_name cn ON cn.concept_id = o.value_coded "
            + "                         INNER JOIN ( "
            + "                         SELECT "
            + "                             p.patient_id, "
            + "                             MIN(e.encounter_datetime) AS first_encounter_date "
            + "                         FROM "
            + "                             patient p "
            + "                                 INNER JOIN "
            + "                             encounter e ON p.patient_id = e.patient_id "
            + "                                 INNER JOIN "
            + "                             obs o ON e.encounter_id = o.encounter_id "
            + "                         WHERE "
            + "                             p.voided = 0 "
            + "                           AND e.voided = 0 "
            + "                           AND o.voided = 0 "
            + "                           AND e.location_id = :location "
            + "                           AND e.encounter_type = ${6} "
            + "                           AND o.concept_id = ${1305} "
            + "                           AND o.value_coded IS NOT NULL "
            + "                           AND e.encounter_datetime BETWEEN DATE_ADD(:startDate, INTERVAL 2 MONTH) AND :endDate "
            + "                         GROUP BY "
            + "                             p.patient_id "
            + "                     ) first_encounter ON first_encounter.patient_id = p.patient_id "
            + "                 WHERE "
            + "                     p.voided = 0 "
            + "                   AND e.voided = 0 "
            + "                   AND o.voided = 0 "
            + "                   AND cn.voided = 0 "
            + "                   AND e.location_id = :location "
            + "                   AND e.encounter_type = ${6} "
            + "                   AND o.concept_id = ${1305} "
            + "                   AND o.value_coded IS NOT NULL "
            + "                   AND e.encounter_datetime BETWEEN DATE_ADD(:startDate, INTERVAL 2 MONTH) AND :endDate "
            + "                   AND first_encounter.first_encounter_date = e.encounter_datetime "
            + "                   AND cn.locale = 'pt' "
            + "                 GROUP BY "
            + "                     p.patient_id "
            + "             ) vl "
            + "        GROUP BY "
            + "            vl.patient_id "
            + "    ) vl ON vl.patient_id = p.patient_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND o.concept_id = ${856} "
            + "  AND e.location_id = :location "
            + "  AND e.encounter_type = ${6} "
            + "  AND e.encounter_datetime = vl.lab_result_date "
            + "GROUP BY "
            + "    p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(map);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  public DataDefinition getViralLoadQualitativeResults() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Resultado da Carga Viral (Resultado Qualitativo)");
    spdd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "End Date", Date.class));
    spdd.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();

    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        "SELECT "
            + "    p.patient_id, "
            + "    IF(o.value_coded = 165331, CONCAT('MENOR QUE ', o.comments), o.value_coded) "
            + "FROM "
            + "    patient p "
            + "        INNER JOIN "
            + "    encounter e ON p.patient_id = e.patient_id "
            + "        INNER JOIN "
            + "    obs o ON e.encounter_id = o.encounter_id "
            + "        INNER JOIN ( "
            + "        SELECT "
            + "            patient_id, "
            + "            MIN(first_result_date) AS lab_result_date "
            + "        FROM ( "
            + "                 SELECT "
            + "                     p.patient_id, "
            + "                     MIN(e.encounter_datetime) AS first_result_date "
            + "                 FROM "
            + "                     patient p "
            + "                         INNER JOIN "
            + "                     encounter e ON p.patient_id = e.patient_id "
            + "                         INNER JOIN "
            + "                     obs o ON e.encounter_id = o.encounter_id "
            + "                 WHERE "
            + "                     p.voided = 0 "
            + "                   AND e.voided = 0 "
            + "                   AND o.voided = 0 "
            + "                   AND e.location_id = :location "
            + "                   AND e.encounter_type = ${6} "
            + "                   AND o.concept_id = ${856} "
            + "                   AND o.value_numeric IS NOT NULL "
            + "                   AND e.encounter_datetime BETWEEN :startDate AND DATE_SUB(DATE_ADD(:startDate, INTERVAL 1 MONTH), INTERVAL 1 DAY) "
            + "                 GROUP BY "
            + "                     p.patient_id "
            + " "
            + "                 UNION "
            + " "
            + "                 SELECT "
            + "                     p.patient_id, "
            + "                     MIN(e.encounter_datetime) AS first_result_date "
            + "                 FROM "
            + "                     patient p "
            + "                         INNER JOIN "
            + "                     encounter e ON p.patient_id = e.patient_id "
            + "                         INNER JOIN "
            + "                     obs o ON e.encounter_id = o.encounter_id "
            + "                         INNER JOIN "
            + "                     concept_name cn ON cn.concept_id = o.value_coded "
            + "                         INNER JOIN ( "
            + "                         SELECT "
            + "                             p.patient_id, "
            + "                             MIN(e.encounter_datetime) AS first_encounter_date "
            + "                         FROM "
            + "                             patient p "
            + "                                 INNER JOIN "
            + "                             encounter e ON p.patient_id = e.patient_id "
            + "                                 INNER JOIN "
            + "                             obs o ON e.encounter_id = o.encounter_id "
            + "                         WHERE "
            + "                             p.voided = 0 "
            + "                           AND e.voided = 0 "
            + "                           AND o.voided = 0 "
            + "                           AND e.location_id = :location "
            + "                           AND e.encounter_type = ${6} "
            + "                           AND o.concept_id = ${1305} "
            + "                           AND o.value_coded IS NOT NULL "
            + "                           AND e.encounter_datetime BETWEEN :startDate AND DATE_SUB(DATE_ADD(:startDate, INTERVAL 1 MONTH), INTERVAL 1 DAY) "
            + "                         GROUP BY "
            + "                             p.patient_id "
            + "                     ) first_encounter ON first_encounter.patient_id = p.patient_id "
            + "                 WHERE "
            + "                     p.voided = 0 "
            + "                   AND e.voided = 0 "
            + "                   AND o.voided = 0 "
            + "                   AND cn.voided = 0 "
            + "                   AND e.location_id = :location "
            + "                   AND e.encounter_type = ${6} "
            + "                   AND o.concept_id = ${1305} "
            + "                   AND o.value_coded IS NOT NULL "
            + "                   AND e.encounter_datetime BETWEEN :startDate AND DATE_SUB(DATE_ADD(:startDate, INTERVAL 1 MONTH), INTERVAL 1 DAY) "
            + "                   AND first_encounter.first_encounter_date = e.encounter_datetime "
            + "                   AND cn.locale = 'pt' "
            + "                 GROUP BY "
            + "                     p.patient_id "
            + " "
            + "                 UNION "
            + " "
            + "                 SELECT "
            + "                     p.patient_id, "
            + "                     MIN(e.encounter_datetime) AS first_result_date "
            + "                 FROM "
            + "                     patient p "
            + "                         INNER JOIN "
            + "                     encounter e ON p.patient_id = e.patient_id "
            + "                         INNER JOIN "
            + "                     obs o ON e.encounter_id = o.encounter_id "
            + "                 WHERE "
            + "                     p.voided = 0 "
            + "                   AND e.voided = 0 "
            + "                   AND o.voided = 0 "
            + "                   AND e.location_id = :location "
            + "                   AND e.encounter_type = ${6} "
            + "                   AND o.concept_id = ${856} "
            + "                   AND o.value_numeric IS NOT NULL "
            + "                   AND e.encounter_datetime BETWEEN DATE_ADD(:startDate, INTERVAL 1 MONTH) AND DATE_SUB(DATE_ADD(:startDate, INTERVAL 2 MONTH), INTERVAL 1 DAY) "
            + "                 GROUP BY "
            + "                     p.patient_id "
            + " "
            + "                 UNION "
            + " "
            + "                 SELECT "
            + "                     p.patient_id, "
            + "                     MIN(e.encounter_datetime) AS first_result_date "
            + "                 FROM "
            + "                     patient p "
            + "                         INNER JOIN "
            + "                     encounter e ON p.patient_id = e.patient_id "
            + "                         INNER JOIN "
            + "                     obs o ON e.encounter_id = o.encounter_id "
            + "                         INNER JOIN "
            + "                     concept_name cn ON cn.concept_id = o.value_coded "
            + "                         INNER JOIN ( "
            + "                         SELECT "
            + "                             p.patient_id, "
            + "                             MIN(e.encounter_datetime) AS first_encounter_date "
            + "                         FROM "
            + "                             patient p "
            + "                                 INNER JOIN "
            + "                             encounter e ON p.patient_id = e.patient_id "
            + "                                 INNER JOIN "
            + "                             obs o ON e.encounter_id = o.encounter_id "
            + "                         WHERE "
            + "                             p.voided = 0 "
            + "                           AND e.voided = 0 "
            + "                           AND o.voided = 0 "
            + "                           AND e.location_id = :location "
            + "                           AND e.encounter_type = ${6} "
            + "                           AND o.concept_id = ${1305} "
            + "                           AND o.value_coded IS NOT NULL "
            + "                           AND e.encounter_datetime BETWEEN DATE_ADD(:startDate, INTERVAL 1 MONTH) AND DATE_SUB(DATE_ADD(:startDate, INTERVAL 2 MONTH), INTERVAL 1 DAY) "
            + "                         GROUP BY "
            + "                             p.patient_id "
            + "                     ) first_encounter ON first_encounter.patient_id = p.patient_id "
            + "                 WHERE "
            + "                     p.voided = 0 "
            + "                   AND e.voided = 0 "
            + "                   AND o.voided = 0 "
            + "                   AND cn.voided = 0 "
            + "                   AND e.location_id = :location "
            + "                   AND e.encounter_type = ${6} "
            + "                   AND o.concept_id = ${1305} "
            + "                   AND o.value_coded IS NOT NULL "
            + "                   AND e.encounter_datetime BETWEEN DATE_ADD(:startDate, INTERVAL 1 MONTH) AND DATE_SUB(DATE_ADD(:startDate, INTERVAL 2 MONTH), INTERVAL 1 DAY) "
            + "                   AND first_encounter.first_encounter_date = e.encounter_datetime "
            + "                   AND cn.locale = 'pt' "
            + "                 GROUP BY "
            + "                     p.patient_id "
            + " "
            + "                 UNION "
            + " "
            + "                 SELECT "
            + "                     p.patient_id, "
            + "                     MIN(e.encounter_datetime) AS first_result_date "
            + "                 FROM "
            + "                     patient p "
            + "                         INNER JOIN "
            + "                     encounter e ON p.patient_id = e.patient_id "
            + "                         INNER JOIN "
            + "                     obs o ON e.encounter_id = o.encounter_id "
            + "                 WHERE "
            + "                     p.voided = 0 "
            + "                   AND e.voided = 0 "
            + "                   AND o.voided = 0 "
            + "                   AND e.location_id = :location "
            + "                   AND e.encounter_type = ${6} "
            + "                   AND o.concept_id = ${856} "
            + "                   AND o.value_numeric IS NOT NULL "
            + "                   AND e.encounter_datetime BETWEEN DATE_ADD(:startDate, INTERVAL 2 MONTH) AND :endDate "
            + "                 GROUP BY "
            + "                     p.patient_id "
            + " "
            + "                 UNION "
            + " "
            + "                 SELECT "
            + "                     p.patient_id, "
            + "                     MIN(e.encounter_datetime) AS first_result_date "
            + "                 FROM "
            + "                     patient p "
            + "                         INNER JOIN "
            + "                     encounter e ON p.patient_id = e.patient_id "
            + "                         INNER JOIN "
            + "                     obs o ON e.encounter_id = o.encounter_id "
            + "                         INNER JOIN "
            + "                     concept_name cn ON cn.concept_id = o.value_coded "
            + "                         INNER JOIN ( "
            + "                         SELECT "
            + "                             p.patient_id, "
            + "                             MIN(e.encounter_datetime) AS first_encounter_date "
            + "                         FROM "
            + "                             patient p "
            + "                                 INNER JOIN "
            + "                             encounter e ON p.patient_id = e.patient_id "
            + "                                 INNER JOIN "
            + "                             obs o ON e.encounter_id = o.encounter_id "
            + "                         WHERE "
            + "                             p.voided = 0 "
            + "                           AND e.voided = 0 "
            + "                           AND o.voided = 0 "
            + "                           AND e.location_id = :location "
            + "                           AND e.encounter_type = ${6} "
            + "                           AND o.concept_id = ${1305} "
            + "                           AND o.value_coded IS NOT NULL "
            + "                           AND e.encounter_datetime BETWEEN DATE_ADD(:startDate, INTERVAL 2 MONTH) AND :endDate "
            + "                         GROUP BY "
            + "                             p.patient_id "
            + "                     ) first_encounter ON first_encounter.patient_id = p.patient_id "
            + "                 WHERE "
            + "                     p.voided = 0 "
            + "                   AND e.voided = 0 "
            + "                   AND o.voided = 0 "
            + "                   AND cn.voided = 0 "
            + "                   AND e.location_id = :location "
            + "                   AND e.encounter_type = ${6} "
            + "                   AND o.concept_id = ${1305} "
            + "                   AND o.value_coded IS NOT NULL "
            + "                   AND e.encounter_datetime BETWEEN DATE_ADD(:startDate, INTERVAL 2 MONTH) AND :endDate "
            + "                   AND first_encounter.first_encounter_date = e.encounter_datetime "
            + "                   AND cn.locale = 'pt' "
            + "                 GROUP BY "
            + "                     p.patient_id "
            + "             ) vl "
            + "        GROUP BY "
            + "            vl.patient_id "
            + "    ) vl ON vl.patient_id = p.patient_id "
            + "WHERE "
            + "    p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND o.concept_id = ${1305} "
            + "  AND e.location_id = :location "
            + "  AND e.encounter_type = ${6} "
            + "  AND e.encounter_datetime = vl.lab_result_date "
            + "GROUP BY "
            + "    p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(map);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }
}
