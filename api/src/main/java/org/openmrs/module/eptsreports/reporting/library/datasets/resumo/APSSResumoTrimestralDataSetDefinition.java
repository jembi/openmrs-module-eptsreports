/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 *
 */

package org.openmrs.module.eptsreports.reporting.library.datasets.resumo;

import static org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils.map;
import static org.openmrs.module.reporting.evaluation.parameter.Mapped.mapStraightThrough;

import org.openmrs.module.eptsreports.reporting.library.cohorts.APSSResumoTrimestralCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.datasets.BaseDataSet;
import org.openmrs.module.eptsreports.reporting.library.dimensions.AgeDimensionCohortInterface;
import org.openmrs.module.eptsreports.reporting.library.dimensions.EptsCommonDimension;
import org.openmrs.module.eptsreports.reporting.library.disaggregations.APSSResumoTrimestraldisaggregations;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class APSSResumoTrimestralDataSetDefinition extends BaseDataSet {

  private EptsCommonDimension eptsCommonDimension;

  private EptsGeneralIndicator eptsGeneralIndicator;

  private APSSResumoTrimestralCohortQueries APSSResumoTrimestralCohortQueries;

  @Autowired
  @Qualifier("commonAgeDimensionCohort")
  private AgeDimensionCohortInterface ageDimensionCohort;

  @Autowired
  public APSSResumoTrimestralDataSetDefinition(
      EptsCommonDimension eptsCommonDimension,
      EptsGeneralIndicator eptsGeneralIndicator,
      APSSResumoTrimestralCohortQueries APSSResumoTrimestralCohortQueries) {
    this.eptsCommonDimension = eptsCommonDimension;
    this.eptsGeneralIndicator = eptsGeneralIndicator;
    this.APSSResumoTrimestralCohortQueries = APSSResumoTrimestralCohortQueries;
  }

  public DataSetDefinition constructAPSSResumoTrimestralDataset() {
    CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";
    dsd.setName("APSS Resumo Trimestral Data set");
    dsd.addParameters(getParameters());

    dsd.addDimension("gender", map(eptsCommonDimension.gender(), ""));
    dsd.addDimension(
        "age", map(eptsCommonDimension.age(ageDimensionCohort), "effectiveDate=${endDate}"));

    // indicators

    // A1
    addRow(
        dsd,
        "A1",
        "Annual Notification",
        getA1(),
        APSSResumoTrimestraldisaggregations.getAPSSPPDisagg());

    dsd.addColumn("A1TG", "Total patients - Total Geral", getA1(), "");

    // B1
    addRow(
        dsd,
        "B1",
        "Annual Notification",
        getB1(),
        APSSResumoTrimestraldisaggregations.getAPSSPPDisagg());

    dsd.addColumn("B1TG", "Total patients - Total Geral", getB1(), "");

    // C1
    addRow(
        dsd,
        "C1",
        "Annual Notification",
        getC1(),
        APSSResumoTrimestraldisaggregations.getAPSSPPDisagg());

    dsd.addColumn("C1TG", "Total patients - Total Geral", getC1(), "");

    // D1
    addRow(
        dsd,
        "D1",
        "Annual Notification",
        getD1(),
        APSSResumoTrimestraldisaggregations.getAPSSPPDisagg());

    dsd.addColumn("D1TG", "Total patients - Total Geral", getD1(), "");

    // E1
    addRow(
        dsd,
        "E1",
        "Annual Notification",
        getE1(),
        APSSResumoTrimestraldisaggregations.getAPSSPPDisagg());

    dsd.addColumn("E1TG", "Total patients - Total Geral", getE1(), "");

    // E2
    addRow(
        dsd,
        "E2",
        "Annual Notification",
        getE2(),
        APSSResumoTrimestraldisaggregations.getAPSSPPDisagg());

    dsd.addColumn("E2TG", "Total patients - Total Geral", getE2(), "");

    // E3
    addRow(
        dsd,
        "E3",
        "Annual Notification",
        getE3(),
        APSSResumoTrimestraldisaggregations.getAPSSPPDisagg());

    dsd.addColumn("E3TG", "Total patients - Total Geral", getE3(), "");

    return dsd;
  }

  private Mapped<CohortIndicator> getA1() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Total patients of A1", mapStraightThrough(APSSResumoTrimestralCohortQueries.getA1())));
  }

  private Mapped<CohortIndicator> getB1() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Total patients of B1", mapStraightThrough(APSSResumoTrimestralCohortQueries.getB1())));
  }

  private Mapped<CohortIndicator> getC1() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Total patients of C1", mapStraightThrough(APSSResumoTrimestralCohortQueries.getC1())));
  }

  private Mapped<CohortIndicator> getD1() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Total patients of D1", mapStraightThrough(APSSResumoTrimestralCohortQueries.getD1())));
  }

  private Mapped<CohortIndicator> getE1() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Total patients of E1", mapStraightThrough(APSSResumoTrimestralCohortQueries.getE1())));
  }

  private Mapped<CohortIndicator> getE2() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Total patients of E2", mapStraightThrough(APSSResumoTrimestralCohortQueries.getE2())));
  }

  private Mapped<CohortIndicator> getE3() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Total patients of E3", mapStraightThrough(APSSResumoTrimestralCohortQueries.getE3())));
  }
}
