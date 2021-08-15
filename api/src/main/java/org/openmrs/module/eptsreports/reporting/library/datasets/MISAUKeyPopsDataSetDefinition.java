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
import org.openmrs.module.eptsreports.reporting.library.cohorts.MISAUKeyPopsCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.dimensions.AgeDimensionCohortInterface;
import org.openmrs.module.eptsreports.reporting.library.dimensions.EptsCommonDimension;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class MISAUKeyPopsDataSetDefinition extends BaseDataSet {

  private MISAUKeyPopsCohortQueries mISAUKeyPopsCohortQueries;

  private EptsGeneralIndicator eptsGeneralIndicator;

  private EptsCommonDimension eptsCommonDimension;

  @Autowired
  @Qualifier("commonAgeDimensionCohort")
  private AgeDimensionCohortInterface ageDimensionCohort;

  @Autowired
  public MISAUKeyPopsDataSetDefinition(
      MISAUKeyPopsCohortQueries mISAUKeyPopsCohortQueries,
      EptsGeneralIndicator eptsGeneralIndicator,
      EptsCommonDimension eptsCommonDimension) {
    this.mISAUKeyPopsCohortQueries = mISAUKeyPopsCohortQueries;
    this.eptsGeneralIndicator = eptsGeneralIndicator;
    this.eptsCommonDimension = eptsCommonDimension;
  }

  public CohortIndicatorDataSetDefinition constructMISAUKeyPopsDataset() {

    CohortIndicatorDataSetDefinition dataSetDefinition = new CohortIndicatorDataSetDefinition();
    dataSetDefinition.setName("MISAU Key Population Data Set");
    dataSetDefinition.addParameters(getParameters());
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";

    dataSetDefinition.addDimension(
        "age",
        EptsReportUtils.map(
            eptsCommonDimension.age(ageDimensionCohort), "effectiveDate=${endDate}"));

    String keyPopMappings = "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}";
    CohortDefinitionDimension keyPopsDimension = eptsCommonDimension.getKeyPopsDimension();
    dataSetDefinition.addDimension("keypop", EptsReportUtils.map(keyPopsDimension, keyPopMappings));

    CohortIndicator getPatientsInARTIndicator =
        eptsGeneralIndicator.getIndicator(
            "getPatientsInARTIndicator",
            EptsReportUtils.map(mISAUKeyPopsCohortQueries.getPatientsInART(), mappings));

    CohortIndicator getPatientsCurrentlyInARTIndicator =
        eptsGeneralIndicator.getIndicator(
            "getPatientsCurrentlyInARTIndicator",
            EptsReportUtils.map(mISAUKeyPopsCohortQueries.getPatientsCurrentlyInART(), mappings));

    CohortIndicator getPatientsARTWithViralLoadTestIndicator =
        eptsGeneralIndicator.getIndicator(
            "getPatientsARTWithViralLoadTestIndicator",
            EptsReportUtils.map(
                mISAUKeyPopsCohortQueries.getPatientsARTWithViralLoadTest(), mappings));

    CohortIndicator getPatientsARTWithVLSuppressionIndicator =
        eptsGeneralIndicator.getIndicator(
            "getPatientsARTWithVLSuppressionIndicator",
            EptsReportUtils.map(
                mISAUKeyPopsCohortQueries.getPatientsARTWithVLSuppression(), mappings));

    CohortIndicator getPatientsStartedARTInLast12MonthsIndicator =
        eptsGeneralIndicator.getIndicator(
            "getPatientsStartedARTInLast12MonthsIndicator",
            EptsReportUtils.map(
                mISAUKeyPopsCohortQueries.getPatientsStartedARTInLast12Months(), mappings));

    CohortIndicator getPatientsOnARTInLast12MonthsIndicator =
        eptsGeneralIndicator.getIndicator(
            "getPatientsOnARTInLast12MonthsIndicator",
            EptsReportUtils.map(
                mISAUKeyPopsCohortQueries.getPatientsOnARTInLast12Months(), mappings));

    // This returns total adult patients Started ART B1
    addRow(
        dataSetDefinition,
        "START",
        "Total Started ART",
        EptsReportUtils.map(getPatientsInARTIndicator, mappings),
        getColumnsForAdults());
    // Numero adultos que iniciaram TARV durante o trimestre PID
    addRow(
        dataSetDefinition,
        "STARTPID",
        "START: People who inject drugs",
        EptsReportUtils.map(getPatientsInARTIndicator, mappings),
        getColumnForPID());

    // Numero adultos que iniciaram TARV durante o trimestre MSM
    addRow(
        dataSetDefinition,
        "STARTMSM",
        "START: Men who have sex with men",
        EptsReportUtils.map(getPatientsInARTIndicator, mappings),
        getColumnForMSM());

    // Numero adultos que iniciaram TARV durante o trimestre CSW
    addRow(
        dataSetDefinition,
        "STARTCSW",
        "START: Female sex workers",
        EptsReportUtils.map(getPatientsInARTIndicator, mappings),
        getColumnForCSW());

    // Numero adultos que iniciaram TARV durante o trimestre PRI
    addRow(
        dataSetDefinition,
        "STARTPRI",
        "START: People in prison and other closed settings",
        EptsReportUtils.map(getPatientsInARTIndicator, mappings),
        getColumnForPRI());

    // This returns total adult patients currently ART B13
    addRow(
        dataSetDefinition,
        "CURRART",
        "Total Current ART",
        EptsReportUtils.map(getPatientsCurrentlyInARTIndicator, mappings),
        getColumnsForAdults());
    // currently ART - PID age dissaggregations
    addRow(
        dataSetDefinition,
        "CURRARTPID",
        "CURRART: People who inject drugs",
        EptsReportUtils.map(getPatientsCurrentlyInARTIndicator, mappings),
        getColumnForPID());
    // currently ART - MSM age dissaggregations
    addRow(
        dataSetDefinition,
        "CURRARTMSM",
        "CURRART: Men who have sex with men",
        EptsReportUtils.map(getPatientsCurrentlyInARTIndicator, mappings),
        getColumnForMSM());

    // currently ART - CSW age dissaggregations
    addRow(
        dataSetDefinition,
        "CURRARTCSW",
        "CURRART: Female sex workers",
        EptsReportUtils.map(getPatientsCurrentlyInARTIndicator, mappings),
        getColumnForCSW());
    // currently ART - CSW age dissaggregations
    addRow(
        dataSetDefinition,
        "CURRARTPRI",
        "CURRART: People in prison and other closed settings",
        EptsReportUtils.map(getPatientsCurrentlyInARTIndicator, mappings),
        getColumnForPRI());

    // This returns total adult patients with viral load test E2
    addRow(
        dataSetDefinition,
        "VL",
        "Total VL test patients",
        EptsReportUtils.map(getPatientsARTWithViralLoadTestIndicator, mappings),
        getColumnsForAdults());

    // Adults patients with viral load test PID
    addRow(
        dataSetDefinition,
        "VLPID",
        "VL: People who inject drugs",
        EptsReportUtils.map(getPatientsARTWithViralLoadTestIndicator, mappings),
        getColumnForPID());
    // Adults patients with viral load test MSM
    addRow(
        dataSetDefinition,
        "VLMSM",
        "VL: Men who have sex with men",
        EptsReportUtils.map(getPatientsARTWithViralLoadTestIndicator, mappings),
        getColumnForMSM());
    // Adults patients with viral load test CSW
    addRow(
        dataSetDefinition,
        "VLCSW",
        "VL: Female sex workers",
        EptsReportUtils.map(getPatientsARTWithViralLoadTestIndicator, mappings),
        getColumnForCSW());
    // Adults patients with viral load test PRI
    addRow(
        dataSetDefinition,
        "VLPRI",
        "VL: People in prison and other closed settings",
        EptsReportUtils.map(getPatientsARTWithViralLoadTestIndicator, mappings),
        getColumnForPRI());

    // This returns total adult patients with viral load supression E3
    addRow(
        dataSetDefinition,
        "VLSUP",
        "Total VL supression patients",
        EptsReportUtils.map(getPatientsARTWithVLSuppressionIndicator, mappings),
        getColumnsForAdults());

    // Adult patients with viral load supression - PID
    addRow(
        dataSetDefinition,
        "VLSUPPID",
        "VLSUP: People who inject drugs",
        EptsReportUtils.map(getPatientsARTWithVLSuppressionIndicator, mappings),
        getColumnForPID());
    // Adult patients with viral load supression - MSM
    addRow(
        dataSetDefinition,
        "VLSUPMSM",
        "VLSUP: Men who have sex with men",
        EptsReportUtils.map(getPatientsARTWithVLSuppressionIndicator, mappings),
        getColumnForMSM());
    // Adult patients with viral load supression - CSW
    addRow(
        dataSetDefinition,
        "VLSUPCSW",
        "VLSUP: Female sex workers",
        EptsReportUtils.map(getPatientsARTWithVLSuppressionIndicator, mappings),
        getColumnForMSM());

    // Adult patients with viral load supression - PRI
    addRow(
        dataSetDefinition,
        "VLSUPPRI",
        "VLSUP: People in prison and other closed settings",
        EptsReportUtils.map(getPatientsARTWithVLSuppressionIndicator, mappings),
        getColumnForPRI());

    // This returns total adult patients started ART in last 12 Months
    addRow(
        dataSetDefinition,
        "START12",
        "Total patients started ART in last 12 Months",
        EptsReportUtils.map(getPatientsStartedARTInLast12MonthsIndicator, mappings),
        getColumnsForAdults());

    // Adult patients started ART in last 12 Months - PID
    addRow(
        dataSetDefinition,
        "START12PID",
        "START12: People who inject drugs",
        EptsReportUtils.map(getPatientsStartedARTInLast12MonthsIndicator, mappings),
        getColumnForPID());
    // Adult patients started ART in last 12 Months - MSM
    addRow(
        dataSetDefinition,
        "START12MSM",
        "START12: Men who have sex with men",
        EptsReportUtils.map(getPatientsStartedARTInLast12MonthsIndicator, mappings),
        getColumnForMSM());
    // Adult patients started ART in last 12 Months - CSW
    addRow(
        dataSetDefinition,
        "START12CSW",
        "START12: Female sex workers",
        EptsReportUtils.map(getPatientsStartedARTInLast12MonthsIndicator, mappings),
        getColumnForCSW());
    // Adult patients started ART in last 12 Months - PRI
    addRow(
        dataSetDefinition,
        "START12PRI",
        "START12: People in prison and other closed settings",
        EptsReportUtils.map(getPatientsStartedARTInLast12MonthsIndicator, mappings),
        getColumnForPRI());

    // This returns total adult patients on ART in last 12 Months
    addRow(
        dataSetDefinition,
        "ARTLAST12",
        "Total patients on ART in last 12 Months",
        EptsReportUtils.map(getPatientsOnARTInLast12MonthsIndicator, mappings),
        getColumnsForAdults());

    // Adult patients on ART in last 12 Months - PID
    addRow(
        dataSetDefinition,
        "ARTLAST12PID",
        "ARTLAST12: People who inject drugs",
        EptsReportUtils.map(getPatientsOnARTInLast12MonthsIndicator, mappings),
        getColumnForPID());

    // Adult patients on ART in last 12 Months - MSM
    addRow(
        dataSetDefinition,
        "ARTLAST12MSM",
        "ARTLAST12: Men who have sex with men",
        EptsReportUtils.map(getPatientsOnARTInLast12MonthsIndicator, mappings),
        getColumnForMSM());
    // Adult patients on ART in last 12 Months - CSW
    addRow(
        dataSetDefinition,
        "ARTLAST12CSW",
        "ARTLAST12: Female sex workers",
        EptsReportUtils.map(getPatientsOnARTInLast12MonthsIndicator, mappings),
        getColumnForCSW());
    // Adult patients on ART in last 12 Months - PRI
    addRow(
        dataSetDefinition,
        "ARTLAST12PRI",
        "ARTLAST12: People in prison and other closed settings",
        EptsReportUtils.map(getPatientsOnARTInLast12MonthsIndicator, mappings),
        getColumnForPRI());

    // POP CHAVES PID- Coorte 6 meses - Totals
    dataSetDefinition.addColumn(
        "STARTPID6MT",
        "Total Started ART in 6 months cohort",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "getPatientsInARTIndicator",
                EptsReportUtils.map(
                    mISAUKeyPopsCohortQueries.getNumberOfAdultsWhoStartedArtInSixMonthsCohort(),
                    mappings)),
            mappings),
        "keypop=PID");
    // POP CHAVES PID- Coorte 6 meses - Totals split into agges
    addRow(
        dataSetDefinition,
        "STARTPID6MD",
        "Total Started ART in 6 months cohort disagg",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "getPatientsInARTIndicator",
                EptsReportUtils.map(
                    mISAUKeyPopsCohortQueries.getNumberOfAdultsWhoStartedArtInSixMonthsCohort(),
                    mappings)),
            mappings),
        getColumnForPID());

    return dataSetDefinition;
  }

  private List<ColumnParameters> getColumnsForAdults() {
    ColumnParameters fifteenPlus =
        new ColumnParameters("fifteenPlus", "15 +", "age=15+", "adultos");

    return Arrays.asList(fifteenPlus);
  }

  private List<ColumnParameters> getColumnForPID() {
    ColumnParameters pidFifteenTo19 =
        new ColumnParameters("pidFifteenTo19", "PID 15-19 years", "age=15-19|keypop=PID", "01");
    ColumnParameters pidTwentyTo24 =
        new ColumnParameters("pidTwentyTo24", "PID 20-24 years", "age=20-24|keypop=PID", "02");
    ColumnParameters pidTwenty25Plus =
        new ColumnParameters("pidTwenty25Plus", "PID 25 years+", "age=25+|keypop=PID", "03");

    return Arrays.asList(pidFifteenTo19, pidTwentyTo24, pidTwenty25Plus);
  }

  private List<ColumnParameters> getColumnForMSM() {
    ColumnParameters msmFifteenTo19 =
        new ColumnParameters("msmFifteenTo19", "MSM 15-19 years", "age=15-19|keypop=MSM", "01");
    ColumnParameters msmTwentyTo24Art =
        new ColumnParameters("msmTwentyTo24Art", "MSM 20-24 years", "age=20-24|keypop=MSM", "02");
    ColumnParameters msmTwenty25Plus =
        new ColumnParameters("msmTwenty25Plus", "MSM 25 years+", "age=25+|keypop=MSM", "03");

    return Arrays.asList(msmFifteenTo19, msmTwentyTo24Art, msmTwenty25Plus);
  }

  private List<ColumnParameters> getColumnForCSW() {
    ColumnParameters cswFifteenTo19 =
        new ColumnParameters("cswFifteenTo19", "CSW 15-19 years", "age=15-19|keypop=CSW", "01");
    ColumnParameters cswTwentyTo24 =
        new ColumnParameters("cswTwentyTo24", "CSW 20-24 years", "age=20-24|keypop=CSW", "02");
    ColumnParameters cswTwenty25Plus =
        new ColumnParameters("cswTwenty25Plus", "CSW 25 years+", "age=25+|keypop=CSW", "03");

    return Arrays.asList(cswFifteenTo19, cswTwentyTo24, cswTwenty25Plus);
  }

  private List<ColumnParameters> getColumnForPRI() {
    ColumnParameters priFifteenTo19 =
        new ColumnParameters("priFifteenTo19", "CSW 15-19 years", "age=15-19|keypop=PRI", "01");
    ColumnParameters priTwentyTo24 =
        new ColumnParameters("priTwentyTo24", "CSW 20-24 years", "age=20-24|keypop=PRI", "02");
    ColumnParameters priTwenty25Plus =
        new ColumnParameters("priTwenty25Plus", "CSW 25 years+", "age=25+|keypop=PRI", "03");

    return Arrays.asList(priFifteenTo19, priTwentyTo24, priTwenty25Plus);
  }

  private List<ColumnParameters> getAgeColumnsForKp() {
    ColumnParameters fifteenTo19 =
        new ColumnParameters("fifteenTo19", "15-19 years", "age=15-19", "01");
    ColumnParameters twentyTo24 =
        new ColumnParameters("twentyTo24", "20-24 years", "age=20-24", "02");
    ColumnParameters twenty25Plus =
        new ColumnParameters("twenty25Plus", "25+ years", "age=25+", "03");
    ColumnParameters total = new ColumnParameters("total", "All categories", "", "04");

    return Arrays.asList(fifteenTo19, twentyTo24, twenty25Plus, total);
  }
}
