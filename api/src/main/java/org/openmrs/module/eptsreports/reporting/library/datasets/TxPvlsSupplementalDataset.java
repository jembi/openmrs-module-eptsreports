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
package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.Arrays;
import java.util.List;
import org.openmrs.module.eptsreports.reporting.library.cohorts.TxPvlsCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.dimensions.AgeDimensionCohortInterface;
import org.openmrs.module.eptsreports.reporting.library.dimensions.EptsCommonDimension;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TxPvlsSupplementalDataset extends BaseDataSet {

  @Autowired private EptsCommonDimension eptsCommonDimension;

  @Autowired private EptsGeneralIndicator eptsGeneralIndicator;

  @Autowired private TxPvlsCohortQueries txPvls;

  @Autowired
  @Qualifier("commonAgeDimensionCohort")
  private AgeDimensionCohortInterface ageDimensionCohort;

  /** @return @{@link DataSetDefinition} */
  public DataSetDefinition constructTxPvlsSupplementalDatset() {

    CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";
    dsd.setName("TxPvlsSupplemental");
    dsd.addParameters(getParameters());

    addDenominatorColumns(dsd, mappings);

    return dsd;
  }

  private void addDenominatorColumns(CohortIndicatorDataSetDefinition dsd, String mappings) {

    dsd.addColumn(
        "TOTAL",
        "Pregnant And Breastfeeding have vl results and on ART more than 3 months Denominator",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "Total Pregnant And Breastfeeding have vl results and on ART more than 3 months Denominator",
                EptsReportUtils.map(
                    txPvls.getPregnantAndBreastfeedingWomenWithViralLoadResults(), mappings)),
            mappings),
        "");

    // Pregnant women on ART for more than 3 months and have VL results
    dsd.addColumn(
        "Pregnant",
        "Pregnant, have vl results and on ART more than 3 months Denominator",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "Pregnant, have vl results and on ART more than 3 months Denominator",
                EptsReportUtils.map(txPvls.getPregnantWomanTxPvlsSupplemental(), mappings)),
            mappings),
        "");

    // Breastfeeding & Pregnant
    // Breastfeeding and on ART for more than 3 months and have VL results
    dsd.addColumn(
        "Breastfeeding",
        "Breast feeding, have vl results and on ART more than 3 months Denominator",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "Breast feeding, have vl results and on ART more than 3 months Denominator",
                EptsReportUtils.map(
                    txPvls.getBreastfeedingWomenWhoHaveViralLoadResults(), mappings)),
            mappings),
        "");
  }
}
