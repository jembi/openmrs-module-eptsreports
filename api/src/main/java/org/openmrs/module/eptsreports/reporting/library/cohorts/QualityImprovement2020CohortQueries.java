package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class QualityImprovement2020CohortQueries {

  private GenericCohortQueries genericCohortQueries;

  private HivMetadata hivMetadata;

  private CommonMetadata commonMetadata;

  private GenderCohortQueries genderCohortQueries;

  private CommonCohortQueries commonCohortQueries;

  private final String MAPPING = "startDate=${startDate},endDate=${endDate},location=${location}";

  @Autowired
  public QualityImprovement2020CohortQueries(
      GenericCohortQueries genericCohortQueries,
      HivMetadata hivMetadata,
      CommonMetadata commonMetadata,
      GenderCohortQueries genderCohortQueries,
      CommonCohortQueries commonCohortQueries) {
    this.genericCohortQueries = genericCohortQueries;
    this.hivMetadata = hivMetadata;
    this.commonMetadata = commonMetadata;
    this.genderCohortQueries = genderCohortQueries;
    this.commonCohortQueries = commonCohortQueries;
  }

  /**
   * <b>MQC3D1</b>: Melhoria de Qualidade Category 3 Deniminator 1 <br>
   * <i> A and not B</i> <br>
   *
   * <ul>
   *   <li>A - Select all patients who initiated ART during the Inclusion Period (startDateRevision
   *       and endDateInclusion)
   *   <li>B - Exclude all transferred in patients as following:
   *       <ul>
   *         <li>All patients registered in Ficha Resumo (Encounter Type Id= 53) and marked as
   *             Transferred-in (“Transfer from other facility” concept Id 1369 = “Yes” concept id
   *             1065) in TARV (“Type of Patient Transferred from” concept id 6300 = “ART” concept
   *             id 6276)
   *             <p>< Note: the both concepts 1369 and 6300 should be recorded in the same encounter
   *       </ul>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQC3D1() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName("Patients Who Initiated ART During The Inclusion Period");
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Date.class));

    CohortDefinition startedART = this.genericCohortQueries.getStartedArtOnPeriod(false, true);

    CohortDefinition transferredIn = this.getTransferredInPatients();

    compositionCohortDefinition.addSearch(
        "A",
        EptsReportUtils.map(
            startedART, "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch("B", EptsReportUtils.map(transferredIn, MAPPING));

    compositionCohortDefinition.setCompositionString("A AND NOT B");

    return compositionCohortDefinition;
  }

  private CohortDefinition getTransferredInPatients() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("transferred in patients");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Date.class));

    Map<String, Integer> map = new HashMap<>();
    map.put(
        "masterCardEncounterType", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put(
        "transferFromOtherFacilityConcept",
        commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("patientFoundYesConcept", hivMetadata.getPatientFoundYesConcept().getConceptId());
    map.put(
        "typeOfPatientTransferredFrom",
        hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("artStatus", hivMetadata.getArtStatus().getConceptId());

    String query =
        "SELECT  p.patient_id "
            + "FROM patient p "
            + "    INNER JOIN encounter e "
            + "        ON e.patient_id=p.patient_id "
            + "    INNER JOIN obs obs1 "
            + "        ON obs1.encounter_id=e.encounter_id "
            + "    INNER JOIN obs obs2 "
            + "        ON obs2.encounter_id=e.encounter_id "
            + "WHERE p.voided =0  "
            + "    AND e.voided = 0 "
            + "    AND obs1.voided =0 "
            + "    AND obs2.voided =0 "
            + "    AND e.encounter_type = ${masterCardEncounterType}  "
            + "    AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "    AND e.location_id = :location "
            + "    AND obs1.concept_id = ${transferFromOtherFacilityConcept} AND obs1.value_coded = ${patientFoundYesConcept} "
            + "    AND obs2.concept_id = ${typeOfPatientTransferredFrom} AND obs2.value_coded = ${artStatus} ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }
  /**
   * <b>MQC4D1</b>: Melhoria de Qualidade Category 4 Deniminator 1 <br>
   * <i> A and NOT B and NOT C and Age < 15 years</i> <br>
   *
   * <ul>
   *   <li>A - Select all patients who initiated ART during the Inclusion Period (startDateRevision
   *       and endDateInclusion)
   *   <li>B - Select all female patients who are pregnant as following:
   *       <ul>
   *         <li>All patients registered in Ficha Clínica (encounter type=53) with
   *             “Gestante”(concept_id 1982) value coded equal to “Yes” (concept_id 1065) and
   *             encounter datetime >= startDateRevision and <=endDateInclusion and sex=Female
   *       </ul>
   *   <li>C- Select all female patients who are breastfeeding as following:
   *       <ul>
   *         <li>all patients registered in Ficha Clinica (encounter type=53) with
   *             “Lactante”(concept_id 6332) value coded equal to “Yes” (concept_id 1065) and
   *             encounter datetime >= startDateRevision and <=endDateInclusion and sex=Female
   *       </ul>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQC4D1() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MCC4D1 Patients");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Date.class));

    cd.addSearch(
        "A",
        EptsReportUtils.map(
            genericCohortQueries.getStartedArtOnPeriod(false, true),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));
    cd.addSearch(
        "B",
        EptsReportUtils.map(
            getPregnantAndBreastfeedingStates(
                hivMetadata.getPregnantConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "C",
        EptsReportUtils.map(
            getPregnantAndBreastfeedingStates(
                hivMetadata.getBreastfeeding().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch("FEMALE", EptsReportUtils.map(genderCohortQueries.femaleCohort(), ""));
    cd.addSearch(
        "CHILDREN",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnArtStartDate(0, 14, true),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));
    cd.setCompositionString("(A AND FEMALE AND CHILDREN) AND NOT (B OR C)");
    return cd;
  }

  /**
   * <b>MQC4D2</b>: Melhoria de Qualidade Category 4 Deniminator 2 <br>
   * <i> A and B and NOT C</i> <br>
   *
   * <ul>
   *   <li>A - Select all patients who initiated ART during the Inclusion Period (startDateRevision
   *       and endDateInclusion)
   *   <li>B - Select all female patients who are pregnant as following:
   *       <ul>
   *         <li>All patients registered in Ficha Clínica (encounter type=53) with
   *             “Gestante”(concept_id 1982) value coded equal to “Yes” (concept_id 1065) and
   *             encounter datetime >= startDateRevision and <=endDateInclusion and sex=Female
   *       </ul>
   *   <li>C- Select all female patients who are breastfeeding as following:
   *       <ul>
   *         <li>all patients registered in Ficha Clinica (encounter type=53) with
   *             “Lactante”(concept_id 6332) value coded equal to “Yes” (concept_id 1065) and
   *             encounter datetime >= startDateRevision and <=endDateInclusion and sex=Female
   *       </ul>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQC4D2() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MCC4D2 Patients");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Date.class));

    cd.addSearch(
        "A",
        EptsReportUtils.map(
            genericCohortQueries.getStartedArtOnPeriod(false, true),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));
    cd.addSearch(
        "B",
        EptsReportUtils.map(
            getPregnantAndBreastfeedingStates(
                hivMetadata.getPregnantConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "C",
        EptsReportUtils.map(
            getPregnantAndBreastfeedingStates(
                hivMetadata.getBreastfeeding().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch("FEMALE", EptsReportUtils.map(genderCohortQueries.femaleCohort(), ""));

    cd.setCompositionString("(A AND FEMALE) AND NOT (B OR C)");
    return cd;
  }

  private CohortDefinition getPregnantAndBreastfeedingStates(int conceptIdQn, int conceptIdAns) {
    Map<String, Integer> map = new HashMap<>();
    map.put("conceptIdQn", conceptIdQn);
    map.put("conceptIdAns", conceptIdAns);
    map.put(
        "fichaClinicaEncounterType", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    String query =
        "SELECT "
            + "   p.patient_id "
            + "   FROM "
            + "   patient p "
            + "   INNER JOIN "
            + "      encounter e "
            + "      ON p.patient_id = e.patient_id "
            + "   INNER JOIN "
            + "      obs o "
            + "      ON o.encounter_id = e.encounter_id "
            + "  WHERE "
            + "   p.voided = 0 "
            + "   AND e.voided = 0 "
            + "   AND o.voided = 0 "
            + "   AND e.encounter_type = ${fichaClinicaEncounterType} "
            + "   AND o.concept_id = ${conceptIdQn} "
            + "   AND o.value_coded = ${conceptIdAns} "
            + "   AND e.encounter_datetime BETWEEN :startDate AND :endDate";

    return genericCohortQueries.generalSql(
        "Pregnant or breastfeeding females", stringSubstitutor.replace(query));
  }

  /*
   *
   * All  patients the first clinical consultation with nutricional state equal
   * to “DAM” or “DAG” occurred during the revision period.
   *
   */

  private CohortDefinition getPatientsWithNutritionalState() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients with Nutritional Calssification");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Date.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6336", commonMetadata.getClassificationOfMalnutritionConcept().getConceptId());
    map.put("1844", hivMetadata.getChronicMalnutritionConcept().getConceptId());
    map.put("68", hivMetadata.getMalnutritionConcept().getConceptId());

    String query =
        " SELECT p.patient_id "
            + " FROM patient p "
            + " INNER JOIN ( "
            + " SELECT  p.patient_id, min(e.encounter_datetime) "
            + " FROM patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + " INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + " WHERE p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND e.location_id = :location "
            + " AND e.encounter_type = ${6}  "
            + " AND o.concept_id = ${6336}  "
            + " AND o.value_coded IN (${1844},${68}) "
            + " AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + " GROUP BY p.patient_id) nut ON p.patient_id = nut.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /*
   *
   * All  patients the first clinical consultation with nutricional state equal
   * to “DAM” or “DAG” occurred during the revision period and
   * “Apoio/Educação Nutricional” = “ATPU” or “SOJA” in
   * the same clinical consultation
   *
   */

  public CohortDefinition getPatientsWithNutritionalStateAndNutritionalSupport() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients with Nutritional Calssification");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Date.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6336", commonMetadata.getClassificationOfMalnutritionConcept().getConceptId());
    map.put("1844", hivMetadata.getChronicMalnutritionConcept().getConceptId());
    map.put("68", hivMetadata.getMalnutritionConcept().getConceptId());
    map.put("2152", hivMetadata.getNutritionalSupplememtConcept().getConceptId());
    map.put("6143", hivMetadata.getATPUSupplememtConcept().getConceptId());
    map.put("2151", hivMetadata.getSojaSupplememtConcept().getConceptId());

    String query =
        " SELECT "
            + " p.patient_id "
            + " FROM "
            + " patient p "
            + "     INNER JOIN "
            + " (SELECT  "
            + "     p.patient_id, MIN(e.encounter_datetime) "
            + " FROM "
            + "     patient p "
            + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + " INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + " INNER JOIN obs o1 ON o1.encounter_id = o.encounter_id "
            + " WHERE "
            + "     p.voided = 0 AND e.voided = 0 "
            + "         AND o.voided = 0 "
            + "         AND o1.voided = 0 "
            + "         AND e.location_id = :location "
            + "         AND e.encounter_type = ${6} "
            + "         AND o.concept_id = ${6336} "
            + "         AND o.value_coded IN (${1844} , ${68}) "
            + "         AND o1.concept_id = ${2152} "
            + "         AND o1.value_coded IN (${6143} , ${2151}) "
            + "         AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + " GROUP BY p.patient_id) nut ON p.patient_id = nut.patient_id; ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>MQC5D1</b>: Melhoria de Qualidade Category 5 Criancas <br>
   * <i> DENOMINATOR: (A AND B) AND NOT (C OR D OR E)</i> <br>
   * <i> NOMINATOR: (A AND B) AND NOT (C OR D OR E) AND F</i> <br>
   *
   * <ul>
   *   <li>A - Select all patients who initiated ART during the Inclusion period (startDateInclusion
   *       and endDateInclusion)
   *   <li>
   *   <li>B - Filter all patients with nutritional state equal to “DAM” or “DAG” registered on a
   *       clinical consultation during the period
   *   <li>
   *   <li>C - All female patients registered as “Pregnant” on a clinical consultation during the
   *       inclusion period (startDateInclusion and endDateInclusion)
   *   <li>
   *   <li>D - All female patients registered as “Breastfeeding” on a clinical consultation during
   *       the inclusion period (startDateInclusion and endDateInclusion)
   *   <li>
   *   <li>E - All transferred IN patients
   *   <li>
   *   <li>F - F - Filter all patients with “Apoio/Educação Nutricional” equals to “ATPU” or “SOJA”
   *       in the same clinical consultation where“Grau da Avaliação Nutricional” equals to “DAM” or
   *       “DAG” during the revision period, clinical consultation >= startDateRevision and
   *       <=endDateRevision
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQ5A(Boolean den) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    if (den) {
      compositionCohortDefinition.setName("% de crianças em TARV com desnutrição (DAM ou DAG)");
    } else {
      compositionCohortDefinition.setName(
          "% de crianças em TARV com desnutrição (DAM ou DAG) e com registo de prescrição de suplementação ou tratamento nutricional");
    }
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Date.class));

    CohortDefinition startedART = getMQC3D1();

    CohortDefinition nutritionalClass = getPatientsWithNutritionalState();

    CohortDefinition pregnant =
        commonCohortQueries.getMohMQPatientsOnCondition(
            true,
            false,
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getPregnantConcept(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition breastfeeding =
        commonCohortQueries.getMohMQPatientsOnCondition(
            true,
            false,
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getBreastfeeding(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition transferIn =
        commonCohortQueries.getMohMQPatientsOnCondition(
            false,
            true,
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getTransferFromOtherFacilityConcept(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            hivMetadata.getTypeOfPatientTransferredFrom(),
            Collections.singletonList(hivMetadata.getArtStatus()));

    CohortDefinition nutSupport = getPatientsWithNutritionalStateAndNutritionalSupport();

    compositionCohortDefinition.addSearch("A", EptsReportUtils.map(startedART, MAPPING));

    compositionCohortDefinition.addSearch("B", EptsReportUtils.map(nutritionalClass, MAPPING));

    compositionCohortDefinition.addSearch("C", EptsReportUtils.map(pregnant, MAPPING));

    compositionCohortDefinition.addSearch("D", EptsReportUtils.map(breastfeeding, MAPPING));

    compositionCohortDefinition.addSearch("E", EptsReportUtils.map(transferIn, MAPPING));

    compositionCohortDefinition.addSearch("F", EptsReportUtils.map(nutSupport, MAPPING));

    if (den) {
      compositionCohortDefinition.setCompositionString("(A AND B) AND NOT (C OR D OR E)");
    } else {
      compositionCohortDefinition.setCompositionString("(A AND B) AND NOT (C OR D OR E) AND F");
    }
    return compositionCohortDefinition;
  }

  /**
   * <b>MQC5D1</b>: Melhoria de Qualidade Category 5 MG <br>
   * <i> DENOMINATOR: (A AND B AND C) AND NOT (D OR E)</i> <br>
   * <i> NOMINATOR: (A AND B AND C) AND NOT (D OR E) AND F</i> <br>
   *
   * <ul>
   *   <li>A - Select all patients who initiated ART during the Inclusion period (startDateInclusion
   *       and endDateInclusion)
   *   <li>
   *   <li>B - Filter all patients with nutritional state equal to “DAM” or “DAG” registered on a
   *       clinical consultation during the period
   *   <li>
   *   <li>C - All female patients registered as “Pregnant” on a clinical consultation during the
   *       inclusion period (startDateInclusion and endDateInclusion)
   *   <li>
   *   <li>D - All female patients registered as “Breastfeeding” on a clinical consultation during
   *       the inclusion period (startDateInclusion and endDateInclusion)
   *   <li>
   *   <li>E - All transferred IN patients
   *   <li>
   *   <li>F - F - Filter all patients with “Apoio/Educação Nutricional” equals to “ATPU” or “SOJA”
   *       in the same clinical consultation where“Grau da Avaliação Nutricional” equals to “DAM” or
   *       “DAG” during the revision period, clinical consultation >= startDateRevision and
   *       <=endDateRevision
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQ5B(Boolean den) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    if (den) {
      compositionCohortDefinition.setName(
          "% de mulheres gravidas em TARV com desnutrição (DAM ou DAG)");
    } else {
      compositionCohortDefinition.setName(
          "% de MG em TARV com desnutrição (DAM ou DAG) e com registo de prescrição de suplementação ou tratamento nutricional");
    }
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Date.class));

    CohortDefinition startedART = getMQC3D1();

    CohortDefinition nutritionalClass = getPatientsWithNutritionalState();

    CohortDefinition pregnant =
        commonCohortQueries.getMohMQPatientsOnCondition(
            true,
            false,
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getPregnantConcept(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition breastfeeding =
        commonCohortQueries.getMohMQPatientsOnCondition(
            true,
            false,
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getBreastfeeding(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition transferIn =
        commonCohortQueries.getMohMQPatientsOnCondition(
            false,
            true,
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getTransferFromOtherFacilityConcept(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            hivMetadata.getTypeOfPatientTransferredFrom(),
            Collections.singletonList(hivMetadata.getArtStatus()));

    CohortDefinition nutSupport = getPatientsWithNutritionalStateAndNutritionalSupport();

    compositionCohortDefinition.addSearch("A", EptsReportUtils.map(startedART, MAPPING));

    compositionCohortDefinition.addSearch("B", EptsReportUtils.map(nutritionalClass, MAPPING));

    compositionCohortDefinition.addSearch("C", EptsReportUtils.map(pregnant, MAPPING));

    compositionCohortDefinition.addSearch("D", EptsReportUtils.map(breastfeeding, MAPPING));

    compositionCohortDefinition.addSearch("E", EptsReportUtils.map(transferIn, MAPPING));

    compositionCohortDefinition.addSearch("F", EptsReportUtils.map(nutSupport, MAPPING));

    if (den) {
      compositionCohortDefinition.setCompositionString("(A AND B AND C) AND NOT (D OR E)");
    } else {
      compositionCohortDefinition.setCompositionString("(A AND B AND C) AND NOT (D OR E) AND F");
    }
    return compositionCohortDefinition;
  }
}
