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
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PmtctEidDataset extends BaseDataSet {

  @Autowired private EptsGeneralIndicator eptsGeneralIndicator;

  @Autowired private PmtctEidCohortQueries pmtctEidCohortQueries;

  public PmtctEidDataset() {
    //    this.pmtctEidCohortQueries = pmtctEidCohortQueries;
  }

  /**
   * Construction of the PMTCT - EID dataset
   *
   * @return @{@link DataSetDefinition}
   */
  public DataSetDefinition constructPmtctEidDataSet() {
    CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
    dsd.setName("PMTCT-EID Dataset");
    dsd.addParameters(getParameters());

    CohortIndicator PMTCTEID =
        eptsGeneralIndicator.getIndicator(
            "EID",
            EptsReportUtils.map(
                pmtctEidCohortQueries.getPmtctEidNumerator(),
                "startDate=${startDate},endDate=${endDate},location=${location}"));

    dsd.addColumn(
        "PMTCTEID",
        "The PMTCT_EID numerator reports the number of infants who had a virologic HIV test (sample collected) by 12 months of age during the reporting period.",
        EptsReportUtils.map(
            PMTCTEID, "startDate=${startDate},endDate=${endDate},location=${location}"),
        "");
    addRow(
        dsd,
        "PMTCTEID",
        "PMTCT-EID",
        EptsReportUtils.map(
            PMTCTEID, "startDate=${startDate},endDate=${endDate},location=${location}"),
        getColumns());

    return dsd;
  }

  public List<ColumnParameters> getColumns() {
    return Arrays.asList(
        new ColumnParameters(
            "New-Male", "New On ART - Male", "art-status=new-on-art|gender=M", "01"),
        new ColumnParameters(
            "New-Female", "New On ART - Female", "art-status=new-on-art|gender=F", "02"));
  }
}
