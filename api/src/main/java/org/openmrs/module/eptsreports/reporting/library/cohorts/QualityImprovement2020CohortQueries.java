package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.eptsreports.reporting.calculation.melhoriaQualidade.ConsultationUntilEndDateAfterStartingART;
import org.openmrs.module.eptsreports.reporting.calculation.melhoriaQualidade.EncounterAfterOldestARTStartDateCalculation;
import org.openmrs.module.eptsreports.reporting.calculation.melhoriaQualidade.SecondFollowingEncounterAfterOldestARTStartDateCalculation;
import org.openmrs.module.eptsreports.reporting.calculation.melhoriaQualidade.ThirdFollowingEncounterAfterOldestARTStartDateCalculation;
import org.openmrs.module.eptsreports.reporting.cohort.definition.CalculationCohortDefinition;
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

  private ResumoMensalCohortQueries resumoMensalCohortQueries;

  private CommonCohortQueries commonCohortQueries;

  private final String MAPPING = "startDate=${startDate},endDate=${endDate},location=${location}";

  @Autowired
  public QualityImprovement2020CohortQueries(
      GenericCohortQueries genericCohortQueries,
      HivMetadata hivMetadata,
      CommonMetadata commonMetadata,
      GenderCohortQueries genderCohortQueries,
      ResumoMensalCohortQueries resumoMensalCohortQueries,
      CommonCohortQueries commonCohortQueries) {
    this.genericCohortQueries = genericCohortQueries;
    this.hivMetadata = hivMetadata;
    this.commonMetadata = commonMetadata;
    this.genderCohortQueries = genderCohortQueries;
    this.resumoMensalCohortQueries = resumoMensalCohortQueries;
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

  public CohortDefinition getMQC4N1() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Date.class));

    compositionCohortDefinition.setName("Numerator for Category 4");

    compositionCohortDefinition.addSearch(
        "E", EptsReportUtils.map(getLastClinicalConsultationClassficacaoDesnutricao(), MAPPING));

    compositionCohortDefinition.addSearch("MQC4D1", EptsReportUtils.map(getMQC4D1(), MAPPING));

    compositionCohortDefinition.setCompositionString("MQC4D1 AND E");

    return compositionCohortDefinition;
  }

  public CohortDefinition getMQC4N2() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Date.class));

    compositionCohortDefinition.setName("Numerator for Category 4");

    compositionCohortDefinition.addSearch(
        "E", EptsReportUtils.map(getLastClinicalConsultationClassficacaoDesnutricao(), MAPPING));

    compositionCohortDefinition.addSearch("MQC4D2", EptsReportUtils.map(getMQC4D2(), MAPPING));

    compositionCohortDefinition.setCompositionString("MQC4D2 AND E");

    return compositionCohortDefinition;
  }

  private CohortDefinition getLastClinicalConsultationClassficacaoDesnutricao() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "last clinical consultation registered CLASSIFICAÇÃO DE DESNUTRIÇÃO");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put(
        "adultoSeguimentoEncounterType",
        hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put(
        "classificationOfMalnutritionConcept",
        commonMetadata.getClassificationOfMalnutritionConcept().getConceptId());
    map.put("normalConcept", hivMetadata.getNormalConcept().getConceptId());
    map.put(
        "malnutritionLightConcept", commonMetadata.getMalnutritionLightConcept().getConceptId());
    map.put("malnutritionConcept", hivMetadata.getMalnutritionConcept().getConceptId());
    map.put(
        "chronicMalnutritionConcept", hivMetadata.getChronicMalnutritionConcept().getConceptId());

    String query =
        "SELECT max_date.patient_id "
            + "FROM "
            + "   ( "
            + "    SELECT p.patient_id, MAX(encounter_datetime) AS max_encounter_date "
            + "    FROM patient p "
            + "        INNER JOIN encounter e "
            + "            ON e.patient_id = p.patient_id "
            + "        INNER JOIN obs o "
            + "            ON o.encounter_id = e.encounter_id "
            + "    WHERE "
            + "        p.voided = 0 "
            + "        AND e.voided = 0 "
            + "        AND o.voided = 0 "
            + "        AND e.encounter_type = ${adultoSeguimentoEncounterType} "
            + "        AND e.location_id = :location "
            + "        AND e.encounter_datetime  "
            + "            BETWEEN :startDate AND :endDate "
            + "        AND o.concept_id = ${classificationOfMalnutritionConcept} "
            + "        AND o.value_coded IN (${normalConcept}, ${malnutritionLightConcept}, ${malnutritionConcept}, ${chronicMalnutritionConcept}) "
            + "    GROUP BY patient_id "
            + "    ) max_date";

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
    map.put("2152", commonMetadata.getNutritionalSupplememtConcept().getConceptId());
    map.put("6143", commonMetadata.getATPUSupplememtConcept().getConceptId());
    map.put("2151", commonMetadata.getSojaSupplememtConcept().getConceptId());

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
   * <b>MQ5A</b>: Melhoria de Qualidade Category 5 Criancas <br>
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

    CohortDefinition nutritionalClass =
        commonCohortQueries.getMohMQPatientsOnCondition(
            false,
            false,
            "first",
            hivMetadata.getAdultoSeguimentoEncounterType(),
            commonMetadata.getClassificationOfMalnutritionConcept(),
            Arrays.asList(
                hivMetadata.getChronicMalnutritionConcept(), hivMetadata.getMalnutritionConcept()),
            null,
            null);

    CohortDefinition pregnant =
        commonCohortQueries.getMohMQPatientsOnCondition(
            true,
            false,
            "once",
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getPregnantConcept(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition breastfeeding =
        commonCohortQueries.getMohMQPatientsOnCondition(
            true,
            false,
            "once",
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getBreastfeeding(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition transferIn =
        commonCohortQueries.getMohMQPatientsOnCondition(
            false,
            true,
            "once",
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
   * <b>MQ5B</b>: Melhoria de Qualidade Category 5 MG <br>
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

    CohortDefinition nutritionalClass =
        commonCohortQueries.getMohMQPatientsOnCondition(
            false,
            false,
            "first",
            hivMetadata.getAdultoSeguimentoEncounterType(),
            commonMetadata.getClassificationOfMalnutritionConcept(),
            Arrays.asList(
                hivMetadata.getChronicMalnutritionConcept(), hivMetadata.getMalnutritionConcept()),
            null,
            null);

    CohortDefinition pregnant =
        commonCohortQueries.getMohMQPatientsOnCondition(
            true,
            false,
            "once",
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getPregnantConcept(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition breastfeeding =
        commonCohortQueries.getMohMQPatientsOnCondition(
            true,
            false,
            "once",
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getBreastfeeding(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition transferIn =
        commonCohortQueries.getMohMQPatientsOnCondition(
            false,
            true,
            "once",
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

    compositionCohortDefinition.setCompositionString("(A AND B AND C) AND NOT (D OR E)");

    return compositionCohortDefinition;
  }

  /**
   * <b>MQ6</b>: Melhoria de Qualidade Category 6 <br>
   * <i> DENOMINATOR 1: A AND NOT (B OR C OR D OR E)</i> <br>
   * <i> DENOMINATOR 2: A AND NOT (B OR C OR D OR E)</i> <br>
   * <i> DENOMINATOR 3: (A AND C) AND NOT (B OR D OR E)</i> <br>
   * <i> DENOMINATOR 4: A AND D AND NOT (B OR C OR E)</i> <br>
   *
   * <ul>
   *   <li>A - Select all patients who initiated ART during the Inclusion period (startDateInclusion
   *       and endDateInclusion)
   *   <li>
   *   <li>B - B - Filter all patients with the last clinical consultation(encounter type 6) with
   *       “Diagnótico TB activo” (concept id 23761) and value coded “SIM”(concept id 1065) and
   *       Encounter_datetime between startDateInclusion and endDateRevision
   *   <li>
   *   <li>C - All female patients registered as “Pregnant” on a clinical consultation during the
   *       inclusion period (startDateInclusion and endDateInclusion)
   *   <li>
   *   <li>D - All female patients registered as “Breastfeeding” on a clinical consultation during
   *       the inclusion period (startDateInclusion and endDateInclusion)
   *   <li>
   *   <li>E - All transferred IN patients during the inclusion period
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQ6A(Integer den) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    if (den == 1) {
      compositionCohortDefinition.setName(
          "% de adultos HIV+ em TARV rastreados para TB na última consulta clínica");
    } else if (den == 2) {
      compositionCohortDefinition.setName(
          "% de crianças HIV+ em TARV rastreadas para TB na última consulta clínica");
    } else if (den == 3) {
      compositionCohortDefinition.setName(
          "% de mulheres grávidas HIV+ rastreadas para TB na última consulta clínica");
    } else if (den == 4) {
      compositionCohortDefinition.setName(
          "% de mulheres lactantes HIV+ rastreadas para TB  na última consulta");
    }
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Date.class));

    CohortDefinition startedART = getMQC3D1();

    CohortDefinition tbActive =
        commonCohortQueries.getMohMQPatientsOnCondition(
            false,
            false,
            "last",
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getActiveTBConcept(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition pregnant =
        commonCohortQueries.getMohMQPatientsOnCondition(
            true,
            false,
            "once",
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getPregnantConcept(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition breastfeeding =
        commonCohortQueries.getMohMQPatientsOnCondition(
            true,
            false,
            "once",
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getBreastfeeding(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition transferIn =
        commonCohortQueries.getMohMQPatientsOnCondition(
            false,
            true,
            "once",
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getTransferFromOtherFacilityConcept(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            hivMetadata.getTypeOfPatientTransferredFrom(),
            Collections.singletonList(hivMetadata.getArtStatus()));

    compositionCohortDefinition.addSearch("A", EptsReportUtils.map(startedART, MAPPING));

    compositionCohortDefinition.addSearch("B", EptsReportUtils.map(tbActive, MAPPING));

    compositionCohortDefinition.addSearch("C", EptsReportUtils.map(pregnant, MAPPING));

    compositionCohortDefinition.addSearch("D", EptsReportUtils.map(breastfeeding, MAPPING));

    compositionCohortDefinition.addSearch("E", EptsReportUtils.map(transferIn, MAPPING));

    if (den == 1 || den == 2) {
      compositionCohortDefinition.setCompositionString("A AND NOT (B OR C OR D OR E)");
    } else if (den == 3) {
      compositionCohortDefinition.setCompositionString("(A AND C) AND NOT (B OR D OR E)");
    } else if (den == 4) {
      compositionCohortDefinition.setCompositionString("A AND D AND NOT (B OR C OR E)");
    }
    return compositionCohortDefinition;
  }

  private <T extends AbstractPatientCalculation>
      CohortDefinition getApssConsultationAfterARTstartDateOrAfterApssConsultation(
          int lowerBoundary, int upperBoundary, Class<T> clazz) {

    CalculationCohortDefinition cd =
        new CalculationCohortDefinition(Context.getRegisteredComponents(clazz).get(0));
    cd.setName("APSS consultation after ART start date");
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
    cd.addCalculationParameter("considerTransferredIn", false);
    cd.addCalculationParameter("considerPharmacyEncounter", true);
    cd.addCalculationParameter("lowerBoundary", lowerBoundary);
    cd.addCalculationParameter("upperBoundary", upperBoundary);

    return cd;
  }

  /**
   * G: Select all patients who have 3 APSS&PP(encounter type 35) consultation in 99 days after
   * Starting ART(The oldest date from A) as following the conditions:
   *
   * <ul>
   *   <li>G1 - FIRST consultation (Encounter_datetime (from encounter type 35)) > “ART Start Date”
   *       (oldest date from A)+20days and <= “ART Start Date” (oldest date from A)+33days AND
   *   <li>G2 - At least one consultation (Encounter_datetime (from encounter type 35)) registered
   *       during the period between “1st Consultation Date(from G1)+20days” and “1st Consultation
   *       Date(from G1)+33days” AND
   *   <li>G3 - At least one consultation (Encounter_datetime (from encounter type 35)) registered
   *       during the period between “2nd Consultation Date(from G2, the oldest)+20days” and “2nd
   *       Consultation Date(from G2, the oldest)+33days” AND
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQC11NG() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName("Category 11 Numerator session G");
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition firstApss =
        getApssConsultationAfterARTstartDateOrAfterApssConsultation(
            20, 33, EncounterAfterOldestARTStartDateCalculation.class);

    CohortDefinition secondApss =
        getApssConsultationAfterARTstartDateOrAfterApssConsultation(
            20, 33, SecondFollowingEncounterAfterOldestARTStartDateCalculation.class);

    CohortDefinition thirdApss =
        getApssConsultationAfterARTstartDateOrAfterApssConsultation(
            20, 33, ThirdFollowingEncounterAfterOldestARTStartDateCalculation.class);

    compositionCohortDefinition.addSearch(
        "firstApss", EptsReportUtils.map(firstApss, "onOrBefore=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "secondApss",
        EptsReportUtils.map(secondApss, "onOrBefore=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "thirdApss", EptsReportUtils.map(thirdApss, "onOrBefore=${endDate},location=${location}"));

    compositionCohortDefinition.setCompositionString("firstApss AND secondApss AND thirdApss");

    return compositionCohortDefinition;
  }

  public CohortDefinition getMQC11NH1() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Date.class));

    sqlCohortDefinition.setName("Category 11 - Numerator - H1");

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String query =
        " SELECT p.patient_id "
            + " FROM patient p "
            + "    INNER JOIN encounter e "
            + "        ON p.patient_id = e.patient_id "
            + "    INNER JOIN ( "
            + "                    SELECT p.patient_id, MIN(e.encounter_datetime) as  encounter_date "
            + "                    FROM patient p   "
            + "                        INNER JOIN encounter e "
            + "                            ON p.patient_id = e.patient_id "
            + "                        INNER JOIN obs o "
            + "                            ON o.encounter_id = e.encounter_id "
            + "                    WHERE p.voided = 0  "
            + "                        AND e.voided = 0 "
            + "                        AND o.voided = 0 "
            + "                        AND e.encounter_type = ${6} "
            + "                        AND e.encounter_datetime "
            + "                            BETWEEN :startDate AND :endDate "
            + "                        AND e.location_id = :location "
            + "                        AND o.concept_id = ${856} AND o.value_numeric >  1000 "
            + "                    GROUP BY p.patient_id "
            + "                ) viral_load ON viral_load.patient_id = p.patient_id "
            + " WHERE p.voided = 0  "
            + "    AND e.voided = 0 "
            + "    AND e.encounter_type = ${35} "
            + "    AND e.encounter_datetime = viral_load.encounter_date "
            + "    AND e.location_id = :location ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getMQC11NH2() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Date.class));

    sqlCohortDefinition.setName("Category 11 - Numerator - H2");

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String query =
        " SELECT p.patient_id "
            + "FROM patient p "
            + "    INNER JOIN encounter e "
            + "        ON p.patient_id = e.patient_id "
            + "    INNER JOIN ( "
            + "                 SELECT p.patient_id, e.encounter_datetime"
            + "                 FROM patient p "
            + "                    INNER JOIN encounter e "
            + "                        ON p.patient_id = e.patient_id "
            + "                    INNER JOIN ( "
            + "                                    SELECT p.patient_id, MIN(e.encounter_datetime) as  encounter_date "
            + "                                    FROM patient p   "
            + "                                        INNER JOIN encounter e "
            + "                                            ON p.patient_id = e.patient_id "
            + "                                        INNER JOIN obs o "
            + "                                            ON o.encounter_id = e.encounter_id "
            + "                                    WHERE p.voided = 0  "
            + "                                        AND e.voided = 0 "
            + "                                        AND o.voided = 0 "
            + "                                        AND e.encounter_type = ${6} "
            + "                                        AND e.encounter_datetime "
            + "                                            BETWEEN :startDate AND :endDate "
            + "                                        AND e.location_id = :location "
            + "                                        AND o.concept_id = ${856} AND o.value_numeric >  1000 "
            + "                                    GROUP BY p.patient_id "
            + "                                ) viral_load ON viral_load.patient_id = p.patient_id "
            + "                 WHERE p.voided = 0  "
            + "                     AND e.voided = 0 "
            + "                    AND e.encounter_type = ${35} "
            + "                    AND e.encounter_datetime = viral_load.encounter_date "
            + "                    AND e.location_id = :location "
            + "                ) h1 ON h1.patient_id = p.patient_id "
            + " WHERE p.voided = 0  "
            + "    AND e.voided = 0 "
            + "    AND e.encounter_type = ${35} "
            + "    AND e.encounter_datetime > DATE_ADD(h1.encounter_datetime, INTERVAL 20 DAY)  "
            + "         AND e.encounter_datetime <= DATE_ADD(h1.encounter_datetime, INTERVAL 33 DAY) "
            + "    AND e.location_id = :location ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition getMQC11NH3() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Date.class));

    sqlCohortDefinition.setName("Category 11 - Numerator - H3");

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM patient p "
            + "    INNER JOIN encounter e "
            + "        ON p.patient_id = e.patient_id "
            + "    INNER JOIN ( "
            + "                     SELECT p.patient_id, e.encounter_datetime "
            + "                    FROM patient p "
            + "                        INNER JOIN encounter e "
            + "                            ON p.patient_id = e.patient_id "
            + "                        INNER JOIN ( "
            + "                                     SELECT p.patient_id, e.encounter_datetime"
            + "                                     FROM patient p "
            + "                                        INNER JOIN encounter e "
            + "                                            ON p.patient_id = e.patient_id "
            + "                                        INNER JOIN ( "
            + "                                                        SELECT p.patient_id, MIN(e.encounter_datetime) as  encounter_date "
            + "                                                        FROM patient p   "
            + "                                                            INNER JOIN encounter e "
            + "                                                                ON p.patient_id = e.patient_id "
            + "                                                            INNER JOIN obs o "
            + "                                                                ON o.encounter_id = e.encounter_id "
            + "                                                        WHERE p.voided = 0  "
            + "                                                            AND e.voided = 0 "
            + "                                                            AND o.voided = 0 "
            + "                                                            AND e.encounter_type = ${6} "
            + "                                                            AND e.encounter_datetime "
            + "                                                                BETWEEN :startDate AND :endDate "
            + "                                                            AND e.location_id = :location "
            + "                                                            AND o.concept_id = ${856} AND o.value_numeric >  1000 "
            + "                                                        GROUP BY p.patient_id "
            + "                                                    ) viral_load ON viral_load.patient_id = p.patient_id "
            + "                                     WHERE p.voided = 0  "
            + "                                         AND e.voided = 0 "
            + "                                        AND e.encounter_type = ${35} "
            + "                                        AND e.encounter_datetime = viral_load.encounter_date "
            + "                                        AND e.location_id = :location "
            + "                                    ) h1 ON h1.patient_id = p.patient_id "
            + "                     WHERE p.voided = 0  "
            + "                        AND e.voided = 0 "
            + "                        AND e.encounter_type = ${35} "
            + "                        AND e.encounter_datetime > DATE_ADD(h1.encounter_datetime, INTERVAL 20 DAY)  "
            + "                             AND e.encounter_datetime <= DATE_ADD(h1.encounter_datetime, INTERVAL 33 DAY) "
            + "                        AND e.location_id = :location "
            + "                ) h2 ON h2.patient_id = p.patient_id "
            + " WHERE p.voided = 0  "
            + "    AND e.voided = 0 "
            + "    AND e.encounter_type = ${35} "
            + "    AND e.encounter_datetime > DATE_ADD(h2.encounter_datetime, INTERVAL 20 DAY)  "
            + "         AND e.encounter_datetime <= DATE_ADD(h2.encounter_datetime, INTERVAL 33 DAY) "
            + "    AND e.location_id = :location ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * H: Select all patients who have 3 APSS&PP (encounter type 35) consultation in 66 days after
   * Viral Load result (the oldest date from B2) following the conditions:
   *
   * <ul>
   *   <li>H1 - One Consultation (Encounter_datetime (from encounter type 35)) on the same date when
   *       the Viral Load with >1000 result was recorded (oldest date from B2) AND
   *   <li>H2- Another consultation (Encounter_datetime (from encounter type 35)) > “1st
   *       consultation” (oldest date from H1)+20 days and <=“1st consultation” (oldest date from
   *       H1)+33days AND
   *   <li>H3- Another consultation (Encounter_datetime (from encounter type 35)) > “2nd
   *       consultation” (oldest date from H2)+20 days and <=“2nd consultation” (oldest date from
   *       H2)+33days AND
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQC11NH() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.addParameter(new Parameter("startDate", "Start date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Date.class));

    compositionCohortDefinition.setName("Category 11 Numerator session G");

    CohortDefinition h1 = getMQC11NH1();
    CohortDefinition h2 = getMQC11NH2();
    CohortDefinition h3 = getMQC11NH3();

    String mapping = "startDate=${startDate},endDate=${endDate},location=${location}";

    compositionCohortDefinition.addSearch("h1", EptsReportUtils.map(h1, mapping));
    compositionCohortDefinition.addSearch("h2", EptsReportUtils.map(h2, mapping));
    compositionCohortDefinition.addSearch("h3", EptsReportUtils.map(h3, mapping));

    compositionCohortDefinition.setCompositionString("h1 AND h2 AND h3");

    return compositionCohortDefinition;
  }

  /**
   * I: Select all patients who have monthly APSS&PP(encounter type 35) consultation (until the end
   * of the revision period) after Starting ART(The oldest date from A) as following pseudo-code:
   *
   * <p>Start pseudo-code:
   *
   * <ul>
   *   <li>For ( i=0; i<(days between “ART Start Date” and endDateRevision; i++)
   *       <ul>
   *         <li>Existence of consultation (Encounter_datetime (from encounter type 35)) > [“ART
   *             Start Date” (oldest date from A)+i] and <= “ART Start Date” (oldest date from
   *             A)+i+33days
   *         <li>i= i+33days
   *       </ul>
   *   <li>End pseudo-code.
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMQC11NI() {

    CalculationCohortDefinition cd =
        new CalculationCohortDefinition(
            Context.getRegisteredComponents(ConsultationUntilEndDateAfterStartingART.class).get(0));
    cd.setName("Categoru 11 - numerator - Session I - Interval of 33 Daus for APSS consultations after ART start date");
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
    cd.addCalculationParameter("considerTransferredIn", false);
    cd.addCalculationParameter("considerPharmacyEncounter", true);

    return cd;
  }

  /**
   *<p>
   *     11.1. % de adultos em TARV com o mínimo de 3 consultas de seguimento de adesão na FM-ficha de APSS/PP nos primeiros 3 meses após início do TARV (Line 56 in the template) Numerador (Column D in the Template) as following:
   *</p>
   * <code>A and NOT C and NOT D and NOT E and NOT F  AND G and Age > 14*</code>
   */
  public  CohortDefinition getMQC11NumAnotCnotDnotEnotFandGAdultss(){
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName("Category 11 : Numeraror 11.1 ");

    compositionCohortDefinition.addParameter(new Parameter("startDate","startDate",Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate","endDate",Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location","location",Location.class));

    CohortDefinition a = null;
    CohortDefinition c = null;
    CohortDefinition d = null;
    CohortDefinition e = null;
    CohortDefinition f = null;
    CohortDefinition g = getMQC11NG();
    CohortDefinition adults =  genericCohortQueries.getAgeOnArtStartDate(15, 200, true);

    compositionCohortDefinition.addSearch("A",EptsReportUtils.map(a,""));
    compositionCohortDefinition.addSearch("C",EptsReportUtils.map(c,""));
    compositionCohortDefinition.addSearch("D",EptsReportUtils.map(d,""));

    compositionCohortDefinition.addSearch("E",EptsReportUtils.map(e,""));
    compositionCohortDefinition.addSearch("F",EptsReportUtils.map(f,""));
    compositionCohortDefinition.addSearch("G",EptsReportUtils.map(g,""));
    compositionCohortDefinition.addSearch("ADULTS",EptsReportUtils.map(adults,""));

    compositionCohortDefinition.setCompositionString("A AND NOT C AND NOT D AND NOT E AND NOT F  AND G AND ADULTS");

    return  compositionCohortDefinition;

  }

  /**
   *<p>
   *     11.2. % de pacientes na 1a linha de TARV com CV acima de 1000 cópias que tiveram 3 consultas de APSS/PP mensais consecutivas para reforço de adesão (Line 57 in the template) Numerador (Column D in the Template) as following:
   *</p>
   * <code>B1 and B2 and NOT C and NOT D and NOT E and NOT F AND H and  Age > 14*</code>
   */
  public  CohortDefinition getMQC11NumB1nB2notCnotDnotEnotEnotFnHandAdultss(){
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName("Category 11 : Numeraror 11.2 ");

    compositionCohortDefinition.addParameter(new Parameter("startDate","startDate",Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate","endDate",Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location","location",Location.class));

    CohortDefinition b1 = null;
    CohortDefinition b2 = null;
    CohortDefinition c = null;
    CohortDefinition d = null;
    CohortDefinition e = null;
    CohortDefinition f = null;
    CohortDefinition h = getMQC11NH();
    CohortDefinition adults =  genericCohortQueries.getAgeOnArtStartDate(15, 200, true);

    compositionCohortDefinition.addSearch("B1",EptsReportUtils.map(b1,""));
    compositionCohortDefinition.addSearch("B2",EptsReportUtils.map(b2,""));
    compositionCohortDefinition.addSearch("C",EptsReportUtils.map(c,""));

    compositionCohortDefinition.addSearch("D",EptsReportUtils.map(d,""));
    compositionCohortDefinition.addSearch("E",EptsReportUtils.map(e,""));
    compositionCohortDefinition.addSearch("F",EptsReportUtils.map(f,""));
    compositionCohortDefinition.addSearch("H",EptsReportUtils.map(h,""));

    compositionCohortDefinition.addSearch("ADULTS",EptsReportUtils.map(adults,""));

    compositionCohortDefinition.setCompositionString("B1 AND B2 AND NOT C AND NOT D AND NOT E AND NOT F AND H AND  ADULTS");

    return  compositionCohortDefinition;

  }

  /**
   * <p>11.3.% de MG em TARV com o mínimo de 3 consultas de seguimento de adesão na FM-ficha de APSS/PP nos primeiros 3 meses após início do TARV (Line 58 in the template) Numerador (Column D in the Template) as following:</p>
   * <code> A and B3 and  C and NOT D and NOT E and NOT F  AND G </code>
   */

  public  CohortDefinition getMQC11NumAnB3nCnotDnotEnotEnotFnG(){
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName("Category 11 : Numeraror 11.3");

    compositionCohortDefinition.addParameter(new Parameter("startDate","startDate",Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate","endDate",Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location","location",Location.class));

    CohortDefinition a = null;
    CohortDefinition b3 = null;
    CohortDefinition c = null;
    CohortDefinition d = null;
    CohortDefinition e = null;
    CohortDefinition f = null;
    CohortDefinition g = getMQC11NG();

    compositionCohortDefinition.addSearch("A",EptsReportUtils.map(a,""));
    compositionCohortDefinition.addSearch("B3",EptsReportUtils.map(b3,""));
    compositionCohortDefinition.addSearch("C",EptsReportUtils.map(c,""));

    compositionCohortDefinition.addSearch("D",EptsReportUtils.map(d,""));
    compositionCohortDefinition.addSearch("E",EptsReportUtils.map(e,""));
    compositionCohortDefinition.addSearch("F",EptsReportUtils.map(f,""));
    compositionCohortDefinition.addSearch("G",EptsReportUtils.map(g,""));

    compositionCohortDefinition.setCompositionString("A AND B3 AND  C AND NOT D AND NOT E AND NOT F  AND G");

    return  compositionCohortDefinition;

  }

  /**
   * <p>
   *     11.4. % de MG na 1a linha de TARV com CV acima de 1000 cópias que tiveram 3 consultas de APSS/PP mensais consecutivas para reforço de adesão (Line 59 in the template) Numerador (Column D in the Template) as following:
   * </p>
   * <code> B1 and B2 and B3 and C and NOT D and NOT E and NOT F AND H </code>
   */

  public  CohortDefinition getMQC11NumB1nB2nB3nCnotDnotEnotEnotFnH(){
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName("Category 11 : Numeraror  11.4");

    compositionCohortDefinition.addParameter(new Parameter("startDate","startDate",Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate","endDate",Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location","location",Location.class));

    CohortDefinition b1 = null;
    CohortDefinition b2 = null;
    CohortDefinition b3 = null;
    CohortDefinition c = null;
    CohortDefinition d = null;
    CohortDefinition e = null;
    CohortDefinition f = null;
    CohortDefinition h = getMQC11NH();


    compositionCohortDefinition.addSearch("B1",EptsReportUtils.map(b1,""));
    compositionCohortDefinition.addSearch("B2",EptsReportUtils.map(b2,""));
    compositionCohortDefinition.addSearch("B3",EptsReportUtils.map(b3,""));
    compositionCohortDefinition.addSearch("C",EptsReportUtils.map(c,""));
    compositionCohortDefinition.addSearch("D",EptsReportUtils.map(d,""));
    compositionCohortDefinition.addSearch("E",EptsReportUtils.map(e,""));
    compositionCohortDefinition.addSearch("F",EptsReportUtils.map(f,""));
    compositionCohortDefinition.addSearch("H",EptsReportUtils.map(h,""));


    compositionCohortDefinition.setCompositionString("B1 AND B2 AND B3 AND C AND NOT D AND NOT E AND NOT F AND H");

    return  compositionCohortDefinition;

  }

  /**
   *
   * <p>
   *     11.5. % de crianças >2 anos de idade em TARV com registo mensal de seguimento da adesão na ficha de APSS/PP nos primeiros 99 dias de TARV (Line 60 in the template) Numerador (Column D in the Template) as following:
   * </p>
   * <code>A and NOT C and NOT D and NOT E and NOT F  AND G and Age BETWEEN 2 AND 14*</code>
   */

  public  CohortDefinition getMQC11NumAnotCnotDnotEnotFnotGnChildren(){
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName("Category 11 : Numeraror 11.5");

    compositionCohortDefinition.addParameter(new Parameter("startDate","startDate",Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate","endDate",Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location","location",Location.class));

    CohortDefinition a = null;
    CohortDefinition c = null;
    CohortDefinition d = null;
    CohortDefinition e = null;
    CohortDefinition f = null;
    CohortDefinition g = null;
    CohortDefinition children = genericCohortQueries.getAgeOnArtStartDate(2, 14, true);


    compositionCohortDefinition.addSearch("A",EptsReportUtils.map(a,""));
    compositionCohortDefinition.addSearch("C",EptsReportUtils.map(c,""));
    compositionCohortDefinition.addSearch("D",EptsReportUtils.map(d,""));
    compositionCohortDefinition.addSearch("E",EptsReportUtils.map(e,""));
    compositionCohortDefinition.addSearch("F",EptsReportUtils.map(f,""));
    compositionCohortDefinition.addSearch("G",EptsReportUtils.map(g,""));
    compositionCohortDefinition.addSearch("CHILDREN",EptsReportUtils.map(children,""));


    compositionCohortDefinition.setCompositionString("A AND NOT C AND NOT D AND NOT E AND NOT F  AND G AND CHILDREN");

    return  compositionCohortDefinition;

  }
  /**
   * <p>
   *     11.6. % de crianças <2 anos de idade em TARV com registo mensal de seguimento da adesão na ficha de APSS/PP no primeiro ano de TARV (Line 61 in the template) Numerador (Column D in the Template) as following:
   * </p>
   * <code> A and NOT C and NOT D and NOT E and NOT F AND I  AND Age  <= 9 MONTHS</code>
   *
   */

  public  CohortDefinition getMQC11NumAnotCnotDnotEnotFnotIlessThan9Month(){
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName("Category 11 : Numeraror 11.6");

    compositionCohortDefinition.addParameter(new Parameter("startDate","startDate",Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate","endDate",Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location","location",Location.class));

    CohortDefinition a = null;
    CohortDefinition c = null;
    CohortDefinition d = null;
    CohortDefinition e = null;
    CohortDefinition f = null;
    CohortDefinition i = null;
    CohortDefinition babies = null;


    compositionCohortDefinition.addSearch("A",EptsReportUtils.map(a,""));
    compositionCohortDefinition.addSearch("C",EptsReportUtils.map(c,""));
    compositionCohortDefinition.addSearch("D",EptsReportUtils.map(d,""));
    compositionCohortDefinition.addSearch("E",EptsReportUtils.map(e,""));
    compositionCohortDefinition.addSearch("F",EptsReportUtils.map(f,""));
    compositionCohortDefinition.addSearch("I",EptsReportUtils.map(i,""));
    compositionCohortDefinition.addSearch("BABIES",EptsReportUtils.map(babies,""));


    compositionCohortDefinition.setCompositionString("A and NOT C and NOT D and NOT E and NOT F AND I  AND BABIES");

    return  compositionCohortDefinition;

  }


  /**
   * <p>
   *     11.7. % de crianças (0-14 anos) na 1a linha de TARV com CV acima de 1000 cópias que tiveram 3 consultas mensais consecutivas de APSS/PP para reforço de adesão(Line 62 in the template) Numerador (Column D in the Template) as following:
   * </p>
   * <code> B1 and B2 and  NOT C and NOT D and NOT E and NOT F  And H and  Age < 15**</code>
   *
   */

  public  CohortDefinition getMQC11NumB1nB2notCnotDnotEnotFnHChildren() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.setName("Category 11 : Numeraror 11.6");

    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition b1 = null;
    CohortDefinition b2 = null;
    CohortDefinition c = null;
    CohortDefinition d = null;
    CohortDefinition e = null;
    CohortDefinition f = null;
    CohortDefinition h = null;
    CohortDefinition children = genericCohortQueries.getAgeOnArtStartDate(0, 14, true);
    ;


    compositionCohortDefinition.addSearch("B1", EptsReportUtils.map(b1, ""));
    compositionCohortDefinition.addSearch("B2", EptsReportUtils.map(b2, ""));
    compositionCohortDefinition.addSearch("C", EptsReportUtils.map(c, ""));
    compositionCohortDefinition.addSearch("D", EptsReportUtils.map(d, ""));
    compositionCohortDefinition.addSearch("E", EptsReportUtils.map(e, ""));
    compositionCohortDefinition.addSearch("F", EptsReportUtils.map(f, ""));
    compositionCohortDefinition.addSearch("H", EptsReportUtils.map(h, ""));
    compositionCohortDefinition.addSearch("CHILDREN", EptsReportUtils.map(children, ""));


    compositionCohortDefinition.setCompositionString("B1 AND B2 AND  NOT C AND NOT D AND NOT E AND NOT F  AND H AND CHILDREN ");

    return compositionCohortDefinition;
  }

}
