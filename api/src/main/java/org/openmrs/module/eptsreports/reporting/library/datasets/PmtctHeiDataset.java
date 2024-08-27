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
import org.openmrs.module.eptsreports.reporting.library.cohorts.PmtctHeiCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.dimensions.EptsCommonDimension;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PmtctHeiDataset extends BaseDataSet {

  @Autowired private EptsCommonDimension eptsCommonDimension;

  @Autowired private EptsGeneralIndicator eptsGeneralIndicator;

  @Autowired private PmtctHeiCohortQueries pmtctHeiCohortQueries;

  /**
   * Construction of the PMTCT - EID dataset
   *
   * @return @{@link DataSetDefinition}
   */
  public DataSetDefinition constructPmtctHeiDataSet() {
    CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
    dsd.setName("PMTCT-EID Dataset");
    dsd.addParameters(getParameters());

    dsd.addDimension(
        "infantAgeInMonths",
        EptsReportUtils.map(
            eptsCommonDimension.getInfantAgeInMonths(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    dsd.addDimension(
        "virologicHivResult",
        EptsReportUtils.map(
            eptsCommonDimension.getPosiveOrNegativeVirologicHiv(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    CohortIndicator TOTAL =
        eptsGeneralIndicator.getIndicator(
            "TOTAL",
            EptsReportUtils.map(
                pmtctHeiCohortQueries.getPmtctHeiNumerator(),
                "startDate=${startDate},endDate=${endDate},location=${location}"));

    dsd.addColumn(
        "TOTAL",
        "The PMTCT_HEI numerator reports the number of HIV-exposed infants, with a virologic HIV test result returned in the reporting period, whose diagnostic sample was collected by 12 months of age.",
        EptsReportUtils.map(
            TOTAL, "startDate=${startDate},endDate=${endDate},location=${location}"),
        "");
    addRow(
        dsd,
        "PMTCTHEI",
        "PMTCT-HEI TOTAL",
        EptsReportUtils.map(
            TOTAL, "startDate=${startDate},endDate=${endDate},location=${location}"),
        getColumns());

    return dsd;
  }

  public List<ColumnParameters> getColumns() {
    return Arrays.asList(
        new ColumnParameters(
            "Negative_Hiv_Result_2_Months",
            "Negative HIV Result for less than 2 months",
            "virologicHivResult=negativeResult|infantAgeInMonths=lessThan2Months",
            "01"),
        new ColumnParameters(
            "Negative_Hiv_Result_2_12_Months",
            "Negative HIV Result for 2 to 12 months",
            "virologicHivResult=negativeResult|infantAgeInMonths=from2To12Months",
            "02"),
        new ColumnParameters(
            "Negative_Hiv_Result",
            "Negative HIV Result",
            "virologicHivResult=negativeResult",
            "03"),
        new ColumnParameters(
            "Positive_Hiv_Result_2_Months",
            "Positive HIV Result for less than 2 months",
            "virologicHivResult=positiveResult|infantAgeInMonths=lessThan2Months",
            "04"),
        new ColumnParameters(
            "Positive_Hiv_Result_2_12_Months",
            "Positive HIV Result for 2 to 12 months",
            "virologicHivResult=positiveResult|infantAgeInMonths=from2To12Months",
            "05"),
        new ColumnParameters(
            "Positive_Hiv_Result",
            "Positive HIV Result",
            "virologicHivResult=positiveResult",
            "06"));
  }
}
