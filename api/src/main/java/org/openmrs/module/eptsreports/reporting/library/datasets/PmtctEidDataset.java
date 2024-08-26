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
import org.openmrs.module.eptsreports.reporting.library.cohorts.PmtctEidCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.dimensions.EptsCommonDimension;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PmtctEidDataset extends BaseDataSet {

  @Autowired private EptsCommonDimension eptsCommonDimension;

  @Autowired private EptsGeneralIndicator eptsGeneralIndicator;

  @Autowired private PmtctEidCohortQueries pmtctEidCohortQueries;

  /**
   * Construction of the PMTCT - EID dataset
   *
   * @return @{@link DataSetDefinition}
   */
  public DataSetDefinition constructPmtctEidDataSet() {
    CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
    dsd.setName("PMTCT-EID Dataset");
    dsd.addParameters(getParameters());

    dsd.addDimension(
        "infantAgeInMonths",
        EptsReportUtils.map(
            eptsCommonDimension.getInfantAgeInMonths(),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));

    dsd.addDimension(
        "sampleCollection",
        EptsReportUtils.map(
            eptsCommonDimension.getInfantsWhoUnderwentSampleCollectionForFirstVirologicHivTest(),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));

    CohortIndicator TOTAL =
        eptsGeneralIndicator.getIndicator(
            "TOTAL",
            EptsReportUtils.map(
                pmtctEidCohortQueries.getPmtctEidNumerator(),
                "startDate=${startDate},endDate=${endDate},location=${location}"));

    dsd.addColumn(
        "TOTAL",
        "The PMTCT_EID numerator reports the number of infants who had a virologic HIV test (sample collected) by 12 months of age during the reporting period.",
        EptsReportUtils.map(
            TOTAL, "startDate=${startDate},endDate=${endDate},location=${location}"),
        "");
    addRow(
        dsd,
        "TOTAL",
        "PMTCT-EID-TOTAL",
        EptsReportUtils.map(
            TOTAL, "startDate=${startDate},endDate=${endDate},location=${location}"),
        getColumns());

    return dsd;
  }

  public List<ColumnParameters> getColumns() {
    return Arrays.asList(
        new ColumnParameters(
            "First-Test-2-Months",
            "First Test < 2 months",
            "sampleCollection=firstSample|infantAgeInMonths=lessThan2Months",
            "01"),
        new ColumnParameters(
            "First-Test-2-12-Months",
            "First Test 2-12 months",
            "sampleCollection=firstSample|infantAgeInMonths=from2To12Months",
            "02"),
        new ColumnParameters(
            "First-Test-Infants", "First Test Infants", "sampleCollection=firstSample", "03"),
        new ColumnParameters(
            "Second-Test-2-Months",
            "Second Test < 2 months",
            "sampleCollection=secondSample|infantAgeInMonths=lessThan2Months",
            "04"),
        new ColumnParameters(
            "Second-Test-2-12-Months",
            "Second Test 2-12 months",
            "sampleCollection=secondSample|infantAgeInMonths=lessThan2Months",
            "05"),
        new ColumnParameters(
            "Second-Test-Infants", "Second Test Infants", "sampleCollection=secondSample", "06"));
  }
}
