/*
 * The contents of this file are subject to the OpenMRS Public License Version
 * 1.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 *
 * Copyright (C) OpenMRS, LLC. All Rights Reserved.
 */
package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.*;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.library.queries.PmtctEidQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsQueriesUtil;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Defines all the PMTCT-EID Cohort Definition instances we want to expose for EPTS */
@Component
public class PmtctEidCohortQueries {

  private HivMetadata hivMetadata;
  private PmtctEidQueries pmtctEidQueries;

  @Autowired
  public PmtctEidCohortQueries(HivMetadata hivMetadata, PmtctEidQueries pmtctEidQueries) {
    this.hivMetadata = hivMetadata;
    this.pmtctEidQueries = pmtctEidQueries;
  }

  /**
   * <b> All infants who have a CCR NID registered </b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getInfantsWithCcrRegisted() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("List of Children enrolled in CCR");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("9", hivMetadata.getCcrNidIdentifierType().getPatientIdentifierTypeId());

    String query =
        new EptsQueriesUtil()
            .patientIdQueryBuilder(pmtctEidQueries.getInfantsWhoHaveCcrNidRegistered())
            .getQuery();

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b> enrolled in CCR Program Enrollment with admission date (Data de admissão) <= report end
   * date or </b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsEnrolledInCcr() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Enrolled in CCR Program");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getCCRProgram().getProgramId());

    String query =
        new EptsQueriesUtil()
            .patientIdQueryBuilder(pmtctEidQueries.getPatientEnrolledInCcrProgram())
            .getQuery();

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b> All infants who have a CCR: Ficha Resumo with Data de abertura do processo <= report end
   * date </b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithCcrFichaResumo() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "All infants who have a CCR: Ficha Resumo with Data de abertura do processo");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("92", hivMetadata.getCCRResumoEncounterType().getEncounterTypeId());

    String query =
        new EptsQueriesUtil()
            .patientIdQueryBuilder(
                pmtctEidQueries.getInfantsWhoHaveCcrFichaResumoWithDataDeAberturadoProcesso())
            .getQuery();

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * EID_FR4 <b> Infants enrolled in CCR by report end date </b>
   *
   * <p>The system will identify infants who were enrolled in CCR by report end date as follows:
   * <li>All infants who have a CCR NID registered and were ever enrolled in CCR Program Enrollment
   *     with admission date (Data de admissão) <= report end date or
   * <li>All infants who have a CCR: Ficha Resumo with Data de abertura do processo <= report end
   *     date
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getInfantsEnrolledInCcrByReportEndDate() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Infants enrolled in CCR by report end date");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    String mapping = "endDate=${endDate},location=${location}";

    Map<String, Integer> map = new HashMap<>();
    map.put("92", hivMetadata.getCCRResumoEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getCCRProgram().getProgramId());
    map.put("9", hivMetadata.getCcrNidIdentifierType().getPatientIdentifierTypeId());

    cd.addSearch("A", EptsReportUtils.map(getInfantsWithCcrRegisted(), mapping));

    cd.addSearch("B", EptsReportUtils.map(getPatientsEnrolledInCcr(), mapping));

    cd.addSearch("C", EptsReportUtils.map(getPatientsWithCcrFichaResumo(), mapping));

    cd.setCompositionString("(A AND B) OR C");

    return cd;
  }

  /**
   * EID_FR5 <b> Infants who underwent a sample collection for a virologic HIV test during the
   * reporting period </b>
   *
   * <p>The system will identify infants who underwent a sample collection for a virological HIV
   * test by selecting all infants who have a Laboratório Geral Form with
   * <li>A specimen collection date (data de colheita de amostra) that falls between report start
   *     date and report end date.
   *
   *     <p>AND
   * <li>One of the following EID collection types (tipo de colheita – DPI) selected:
   *
   *     <ul>
   *       <li>First collection < 9 months (Primeira colheita <9m)
   *           <p>OR
   *       <li>Following collection < 9 months (Colheita seguinte <9m)
   *           <p>OR
   *       <li>First collection 9-17 months (Primeira colheita 9-17 meses)
   *           <p>OR
   *       <li>Following collection 9-17 months (Colheita seguinte 9-17 meses)
   *     </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoUnderwentASampleCollectionForVirologicHivTest() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Underwent a sample collection for a virologic HIV test");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("23821", hivMetadata.getSampleCollectionDateAndTime().getConceptId());
    map.put("165502", hivMetadata.getSampleEarlyInfantDiagnosisConcept().getConceptId());
    map.put("165503", hivMetadata.getFirstSampleInLessThan9MonthsConcept().getConceptId());
    map.put("165506", hivMetadata.getNextSampleInLessThan9MonthsConcept().getConceptId());
    map.put("165507", hivMetadata.getFirstSampleBetween9To17MonthsConcept().getConceptId());
    map.put("165510", hivMetadata.getFollowingtSampleBetween9To17MonthsConcept().getConceptId());

    String query =
        "SELECT "
            + "  patient_id "
            + "FROM "
            + "  ( "
            + "    SELECT "
            + "      p.patient_id, "
            + "      Min(o.value_datetime) AS collection_date "
            + "    FROM "
            + "      patient p "
            + "      INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "      INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "      INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "    WHERE "
            + "      p.voided = 0 "
            + "      AND e.voided = 0 "
            + "      AND o.voided = 0 "
            + "      AND o2.voided = 0 "
            + "      AND e.location_id = :location "
            + "      AND e.encounter_type = ${13} "
            + "      AND ( "
            + "        ( "
            + "          o.concept_id = ${23821} "
            + "          AND o.value_datetime >= :startDate "
            + "          AND o.value_datetime <= :endDate "
            + "        ) "
            + "        AND ( "
            + "          o2.concept_id = ${165502} "
            + "          AND o2.value_coded IN (${165503}, ${165506}, ${165507}, ${165510}) "
            + "        ) "
            + "      ) "
            + "    GROUP BY "
            + "      p.patient_id "
            + "  ) sample_collected";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * identify Infants who underwent sample collection for first or second virologic HIV test
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoUnderwentFirstOrSecondSampleCollectionForVirologicHivTest(
      boolean firstSample) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Underwent a sample collection for a virologic HIV test");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("23821", hivMetadata.getSampleCollectionDateAndTime().getConceptId());
    map.put("165502", hivMetadata.getSampleEarlyInfantDiagnosisConcept().getConceptId());
    map.put("165503", hivMetadata.getFirstSampleInLessThan9MonthsConcept().getConceptId());
    map.put("165506", hivMetadata.getNextSampleInLessThan9MonthsConcept().getConceptId());
    map.put("165507", hivMetadata.getFirstSampleBetween9To17MonthsConcept().getConceptId());
    map.put("165510", hivMetadata.getFollowingtSampleBetween9To17MonthsConcept().getConceptId());

    String query =
        "SELECT patient_id FROM ( "
            + " SELECT p.patient_id, "
            + "       Min(o.value_datetime) AS collection_date "
            + "FROM patient p "
            + "         INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "         INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "         INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "WHERE p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND o2.voided = 0 "
            + "  AND e.location_id = :location "
            + "  AND e.encounter_type = ${13} "
            + "  AND ( (o.concept_id = ${23821} "
            + "    AND o.value_datetime >= :startDate "
            + "    AND o.value_datetime <= :endDate ) "
            + "    AND (o2.concept_id = ${165502} ";
    if (firstSample) {
      query = query + "        AND o2.value_coded IN (${165503}, ${165507}) ) ) ";
    } else {
      query = query + "        AND o2.value_coded IN (${165506}, ${165510}) ) ) ";
    }
    query = query + "GROUP BY p.patient_id ) hiv_sample";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * EID_FR6 <b>Infant`s Age (months)</b>
   *
   * <p>The system will identify infant age for disaggregation intervals as follows:
   *
   * <ul>
   *   <li>Infants with birth date information registered in the system, the age of the infant
   *       should be calculated as the child’s age at the specimen collection date registered on the
   *       Laboratório Geral Form in months. (Specimen Collection Date – Birth Date)
   * </ul>
   *
   * <p><b>Nota 1:</b> Infants without birth date information should be considered as unknown age
   * and can therefore not be included in the numerator.
   *
   * <p><b>Nota 2:</b> Infant age will be calculated in days to determine the corresponding age
   * disaggregation intervals as follows:
   *
   * <ul>
   *   <li>(<2 months) – age between 0-59 days.
   *   <li>( 2-12 months) – age between 60-365 days
   * </ul>
   *
   * @param minAge Minimum age in Days of a patient based on Specimen Collection Date
   * @param maxAge Maximum age in Days of a patient based on Specimen Collection Date
   * @return CohortDefinition
   */
  public CohortDefinition getInfantAge(Integer minAge, Integer maxAge) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Infant Age");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("23821", hivMetadata.getSampleCollectionDateAndTime().getConceptId());
    map.put("165502", hivMetadata.getSampleEarlyInfantDiagnosisConcept().getConceptId());
    map.put("165503", hivMetadata.getFirstSampleInLessThan9MonthsConcept().getConceptId());
    map.put("165506", hivMetadata.getNextSampleInLessThan9MonthsConcept().getConceptId());
    map.put("165507", hivMetadata.getFirstSampleBetween9To17MonthsConcept().getConceptId());
    map.put("165510", hivMetadata.getFollowingtSampleBetween9To17MonthsConcept().getConceptId());
    map.put("minAge", minAge);
    map.put("maxAge", maxAge);

    String query =
        "SELECT "
            + "  pr.person_id "
            + "FROM "
            + "  person pr "
            + "  INNER JOIN ( "
            + "    SELECT "
            + "      p.patient_id, "
            + "      Min(o.value_datetime) AS collection_date "
            + "    FROM "
            + "      patient p "
            + "      INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "      INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "      INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "    WHERE "
            + "      p.voided = 0 "
            + "      AND e.voided = 0 "
            + "      AND o.voided = 0 "
            + "      AND o2.voided = 0 "
            + "      AND e.location_id = :location "
            + "      AND e.encounter_type = ${13} "
            + "      AND ( "
            + "        ( "
            + "          o.concept_id = ${23821} "
            + "          AND o.value_datetime >= :startDate "
            + "          AND o.value_datetime <= :endDate "
            + "        ) "
            + "        AND ( "
            + "          o2.concept_id = ${165502} "
            + "          AND o2.value_coded IN (${165503}, ${165506}, ${165507}, ${165510}) "
            + "        ) "
            + "      ) "
            + "    GROUP BY "
            + "      p.patient_id "
            + "  ) specimen_collection ON pr.person_id = specimen_collection.patient_id "
            + "WHERE "
            + "  pr.birthdate IS NOT NULL "
            + "  AND specimen_collection.collection_date IS NOT NULL "
            + "  AND TIMESTAMPDIFF( "
            + "    DAY, pr.birthdate, specimen_collection.collection_date "
            + "  ) >= ${minAge} "
            + "  AND TIMESTAMPDIFF( "
            + "    DAY, pr.birthdate, specimen_collection.collection_date "
            + "  ) <= ${maxAge}";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * EID_FR2 <b> Indicator numerator </b>
   *
   * <p>The system will generate the PMTCT_EID indicator numerator as the number of infants who had
   * a virologic HIV test (sample collected) by 12 months of age during the reporting period by
   * considering all infants who:
   * <li>Are enrolled in CCR by report end date (EID_FR4)
   *
   *     <p>AND
   * <li>Underwent a sample collection for a virologic HIV test during the reporting period
   *     (EID_FR5)
   *
   *     <p>+
   *
   *     <p>The system will exclude the following patients:
   * <li>All patients older than 12 months of age at sample collection date (EID_FR6).
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPmtctEidNumerator() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("PMTCT-EID");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";
    String mappings1 = "endDate=${endDate},location=${location}";

    cd.addSearch(
        "infantsInCcr", EptsReportUtils.map(getInfantsEnrolledInCcrByReportEndDate(), mappings1));

    cd.addSearch(
        "sampleCollectionHiv",
        EptsReportUtils.map(
            getPatientsWhoUnderwentASampleCollectionForVirologicHivTest(), mappings));

    cd.addSearch("infantAge", EptsReportUtils.map(getInfantAge(0, 365), mappings));

    cd.setCompositionString("infantsInCcr AND sampleCollectionHiv AND infantAge");
    return cd;
  }
}
