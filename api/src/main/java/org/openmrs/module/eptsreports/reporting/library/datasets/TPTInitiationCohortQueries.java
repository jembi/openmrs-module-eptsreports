package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TPTInitiationCohortQueries {

  private HivMetadata hivMetadata;
  private TbMetadata tbMetadata;

  @Autowired
  public TPTInitiationCohortQueries(HivMetadata hivMetadata, TbMetadata tbMetadata) {
    this.hivMetadata = hivMetadata;
    this.tbMetadata = tbMetadata;
  }

  public CohortDefinition getBaseCohort() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("TPT Initiation Patient List Cohort");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    //  CohortDefinition txcurr = this.txCurrCohortQueries.getTxCurrCompositionCohort("txcurr",
    // true);
    CohortDefinition A3HP1 = getPatientsWith3HP1Prescriptions();
    CohortDefinition A3HP2 = getPatientsWith3HP2FirstFILTAndRegimeTPT();
    CohortDefinition A3HP3 = getPatientsWith3HP3RegimeTPTAndSeguimentoDeTratamento();

    cd.addSearch(
        "A3HP1",
        EptsReportUtils.map(
            A3HP1, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "A3HP2",
        EptsReportUtils.map(
            A3HP2, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "A3HP3",
        EptsReportUtils.map(
            A3HP3, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("(A3HP1 OR A3HP2 OR A3HP3)");

    return cd;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <p>Select all patients with Ficha Clinica - MasterCard (encounter type 6) with “Outras
   * prescricoes” (concept id 1719) with value coded equal to “3HP” (concept id 23954) and
   * encounter_datetime between start date and end date as <b>3HP Start Date</b> and (exception-> no
   * other 3HP prescriptions [“Outras prescricoes” (concept id 1719) with value coded equal to “3HP”
   * (concept id 23954)] marked on Ficha-Clínica in the 4 months prior to the <b>3HP Start Date</b>
   * and no “Regime de TPT” (concept id 23985) with value coded “3HP” or ” 3HP+Piridoxina” (concept
   * id 23985) with value coded “3HP” or ” 3HP+Piridoxina” (concept id in [23954, 23984]) marked on
   * FILT (encounter type 60) in the 4 months prior to the <b>3HP Start Date</b>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWith3HP1Prescriptions() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("3HP1 - Patients with other 3HP prescriptions");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("60", tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId());
    valuesMap.put("1719", tbMetadata.getTreatmentPrescribedConcept().getConceptId());
    valuesMap.put("23954", tbMetadata.get3HPConcept().getConceptId());
    valuesMap.put("23985", tbMetadata.getRegimeTPTConcept().getConceptId());
    valuesMap.put("23984", tbMetadata.get3HPPiridoxinaConcept().getConceptId());

    String query =
        "SELECT p.patient_id  "
            + "         FROM  patient p  "
            + "         INNER JOIN encounter e ON e.patient_id = p.patient_id       "
            + "         INNER JOIN obs o ON o.encounter_id = e.encounter_id       "
            + "         INNER JOIN (SELECT  p.patient_id, MIN(e.encounter_datetime) first_pickup_date      "
            + "      FROM    patient p       "
            + "      INNER JOIN encounter e ON e.patient_id = p.patient_id      "
            + "      INNER JOIN obs o ON o.encounter_id = e.encounter_id       "
            + "      WHERE   p.voided = 0 AND e.voided = 0 AND o.voided = 0 AND e.location_id = :location      "
            + "          AND e.encounter_type =   ${6} AND o.concept_id =   ${1719}       "
            + "          AND o.value_coded IN ( ${23954} ) AND e.encounter_datetime >= :startDate     "
            + "          AND e.encounter_datetime <= :endDate GROUP BY p.patient_id) AS pickup      "
            + "      ON pickup.patient_id = p.patient_id     "
            + "         WHERE p.patient_id NOT IN (    "
            + "           SELECT patient_id       "
            + "           FROM patient p      "
            + "           WHERE p.voided = 0   AND e.voided = 0       "
            + "               AND o.voided = 0   AND e.location_id = :location      "
            + "               AND e.encounter_type =   ${6}  AND o.concept_id =   ${1719}       "
            + "               AND o.value_coded IN ( ${23954} )      "
            + "               AND e.encounter_datetime >= DATE_SUB(pickup.first_pickup_date, INTERVAL 4 MONTH)       "
            + "               AND e.encounter_datetime < pickup.first_pickup_date   "
            + "           UNION   "
            + "           SELECT p.patient_id   "
            + "           FROM patient p   "
            + "               INNER JOIN encounter e ON p.patient_id = e.patient_id   "
            + "               INNER JOIN obs o ON e.encounter_id = o.encounter_id   "
            + "           WHERE p.voided = 0 AND e.voided = 0  AND o.voided = 0 AND e.encounter_type=  ${60}    "
            + "               AND (o.concept_id=  ${23985}   AND o.value_coded IN ( ${23954} , ${23984} ))   "
            + "               AND e.encounter_datetime >= DATE_SUB(pickup.first_pickup_date, INTERVAL 4 MONTH)       "
            + "               AND e.encounter_datetime < pickup.first_pickup_date)    ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <p>2: Select all patients with the First FILT (encounter type 60) with “Regime de TPT” (concept
   * id 23985) value coded “3HP” or ” 3HP+Piridoxina” (concept id in [23954, 23984]) and encounter
   * datetime (as <b>3HP Start Date</b>) between start date and end date and no other 3HP
   * prescriptions (concept id 1719) marked on Ficha-Clinica in the 4 months prior to the <b>3HP
   * Start Date</b> and no “Regime de TPT” (concept id 23985) with value coded “3HP” or ”
   * 3HP+Piridoxina” (concept id in [23954, 23984]) marked on FILT (encounter type 60) in the 4
   * months prior to the <b>3HP Start Date</b>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWith3HP2FirstFILTAndRegimeTPT() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("3HP2 - Patients with First FILT");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("60", tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId());
    valuesMap.put("1719", tbMetadata.getTreatmentPrescribedConcept().getConceptId());
    valuesMap.put("23954", tbMetadata.get3HPConcept().getConceptId());
    valuesMap.put("23985", tbMetadata.getRegimeTPTConcept().getConceptId());
    valuesMap.put("23984", tbMetadata.get3HPPiridoxinaConcept().getConceptId());

    String query =
        " SELECT p.patient_id     "
            + "         FROM  patient p       "
            + "         INNER JOIN encounter e ON e.patient_id = p.patient_id       "
            + "         INNER JOIN obs o ON o.encounter_id = e.encounter_id       "
            + "         INNER JOIN (SELECT  p.patient_id, MIN(e.encounter_datetime) first_pickup_date      "
            + "      FROM    patient p       "
            + "      INNER JOIN encounter e ON e.patient_id = p.patient_id      "
            + "      INNER JOIN obs o ON o.encounter_id = e.encounter_id       "
            + "      WHERE   p.voided = 0 AND e.voided = 0       "
            + "          AND o.voided = 0   AND e.location_id = :location      "
            + "          AND e.encounter_type =   ${60}       "
            + "          AND o.concept_id =   ${23985}       "
            + "          AND o.value_coded IN (${23954},${23984}  )      "
            + "          AND e.encounter_datetime >= :startDate     "
            + "          AND e.encounter_datetime <= :endDate     "
            + "      GROUP BY p.patient_id) AS pickup      "
            + "      ON pickup.patient_id = p.patient_id     "
            + "         WHERE p.patient_id NOT IN ( SELECT patient_id       "
            + "       FROM patient p      "
            + " WHERE p.voided = 0       "
            + "            AND e.voided = 0 AND o.voided = 0       "
            + "            AND e.location_id = :location      "
            + "            AND e.encounter_type =   ${6}  AND o.concept_id =   ${23985}       "
            + "            AND o.value_coded IN ( ${23954}  ,  ${23984} )      "
            + "            AND e.encounter_datetime >= DATE_SUB(pickup.first_pickup_date, INTERVAL 4 MONTH)       "
            + "            AND e.encounter_datetime < pickup.first_pickup_date   "
            + "   UNION   "
            + "  SELECT p.patient_id   "
            + "  FROM patient p   "
            + "      INNER JOIN encounter e ON p.patient_id = e.patient_id   "
            + "      INNER JOIN obs o ON e.encounter_id = o.encounter_id   "
            + "  WHERE p.voided = 0 AND e.voided = 0   "
            + "      AND o.voided = 0   "
            + "      AND e.encounter_type= ${60}    "
            + "      AND (o.concept_id=  ${23985}   AND o.value_coded IN ( ${23954} , ${23984} ))   "
            + "      AND e.encounter_datetime >= DATE_SUB(pickup.first_pickup_date, INTERVAL 4 MONTH)       "
            + "      AND e.encounter_datetime < pickup.first_pickup_date)   ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <p>Select all patients with “Regime de TPT” (concept id 23985) with value coded “3HP” or ”
   * 3HP+Piridoxina” (concept id in [23954, 23984]) and “Seguimento de tratamento TPT”(concept ID
   * 23987) value coded “inicio” or “re-inicio” (concept ID in [1256, 1705]) marked on FILT
   * (encounter type 60) and encounter datetime between start date and end date
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWith3HP3RegimeTPTAndSeguimentoDeTratamento() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("3HP3 - Patients with Regime TPT marked on FILT");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("60", tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId());
    valuesMap.put("23985", tbMetadata.getRegimeTPTConcept().getConceptId());
    valuesMap.put("23954", tbMetadata.get3HPConcept().getConceptId());
    valuesMap.put("23984", tbMetadata.get3HPPiridoxinaConcept().getConceptId());
    valuesMap.put("23987", hivMetadata.getPatientTreatmentFollowUp().getConceptId());
    valuesMap.put("1256", hivMetadata.getStartDrugs().getConceptId());
    valuesMap.put("1705", hivMetadata.getRestartConcept().getConceptId());

    String query =
        " SELECT p.patient_id   "
            + "  FROM patient p   "
            + "      INNER JOIN encounter e ON p.patient_id = e.patient_id   "
            + "      INNER JOIN obs o ON e.encounter_id = o.encounter_id   "
            + "      INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id   "
            + "  WHERE p.voided = 0 AND e.voided = 0   "
            + "      AND o.voided = 0   "
            + "      AND e.encounter_type=  ${60}    "
            + "      AND (o.concept_id=  ${23985}   AND o.value_coded IN ( ${23954}  , ${23984} ))   "
            + "      AND (o2.concept_id=  ${23987}   AND o2.value_coded IN ( ${1256} , ${1705} ))   "
            + "      AND e.encounter_datetime BETWEEN :startDate AND :endDate   "
            + "      GROUP BY p.patient_id   ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }
}
