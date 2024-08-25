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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
   * <b> Infants enrolled in CCR by report end date </b>
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

    cd.setCompositionString("A AND (B OR C)");

    return cd;
  }
}
