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
 */

package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.Arrays;
import java.util.List;
import org.openmrs.module.eptsreports.reporting.library.cohorts.TXTBCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.dimensions.*;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TxTBDataset extends BaseDataSet {
  @Autowired private EptsCommonDimension eptsCommonDimension;

  @Autowired private EptsGeneralIndicator eptsGeneralIndicator;

  @Autowired private TXTBCohortQueries txTbCohortQueries;

  @Autowired
  @Qualifier("commonAgeDimensionCohort")
  private AgeDimensionCohortInterface ageDimensionCohort;

  public DataSetDefinition constructTxTBDataset() {
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";
    CohortIndicatorDataSetDefinition dataSetDefinition = new CohortIndicatorDataSetDefinition();
    dataSetDefinition.setName("TX_TB Data Set");
    dataSetDefinition.addParameters(getParameters());

    dataSetDefinition.addDimension("gender", EptsReportUtils.map(eptsCommonDimension.gender(), ""));
    dataSetDefinition.addDimension(
        "age",
        EptsReportUtils.map(
            eptsCommonDimension.age(ageDimensionCohort), "effectiveDate=${endDate}"));
    addTXTBNumerator(mappings, dataSetDefinition);

    addTXTBDenominator(mappings, dataSetDefinition);

    return dataSetDefinition;
  }

  private void addTXTBNumerator(
      String mappings, CohortIndicatorDataSetDefinition dataSetDefinition) {
    CohortIndicator numerator =
        eptsGeneralIndicator.getIndicator(
            "NUMERATOR", EptsReportUtils.map(txTbCohortQueries.txTbNumerator(), mappings));
    CohortIndicator patientsPreviouslyOnARTNumerator =
        eptsGeneralIndicator.getIndicator(
            "patientsPreviouslyOnARTNumerator",
            EptsReportUtils.map(txTbCohortQueries.patientsPreviouslyOnARTNumerator(), mappings));
    CohortIndicator patientsNewOnARTNumerator =
        eptsGeneralIndicator.getIndicator(
            "patientsNewOnARTNumerator",
            EptsReportUtils.map(txTbCohortQueries.patientsNewOnARTNumerator(), mappings));

    dataSetDefinition.addColumn(
        "TXB_NUM", "TX_TB: Numerator total", EptsReportUtils.map(numerator, mappings), "");
    addRow(
        dataSetDefinition,
        "TXB_NUM_PREV",
        "Numerator (patientsPreviouslyOnARTNumerator)",
        EptsReportUtils.map(patientsPreviouslyOnARTNumerator, mappings),
        dissagregations());
    addRow(
        dataSetDefinition,
        "TXB_NUM_NEW",
        "Numerator (patientsNewOnARTNumerator)",
        EptsReportUtils.map(patientsNewOnARTNumerator, mappings),
        dissagregations());
  }

  private void addTXTBDenominator(
      String mappings, CohortIndicatorDataSetDefinition dataSetDefinition) {
    CohortIndicator previouslyOnARTPostiveScreening =
        eptsGeneralIndicator.getIndicator(
            "previouslyOnARTPositiveScreening",
            EptsReportUtils.map(txTbCohortQueries.previouslyOnARTPositiveScreening(), mappings));
    CohortIndicator previouslyOnARTNegativeScreening =
        eptsGeneralIndicator.getIndicator(
            "previouslyOnARTNegativeScreening",
            EptsReportUtils.map(txTbCohortQueries.previouslyOnARTNegativeScreening(), mappings));
    CohortIndicator newOnARTPositiveScreening =
        eptsGeneralIndicator.getIndicator(
            "newOnARTPositiveScreening",
            EptsReportUtils.map(txTbCohortQueries.newOnARTPositiveScreening(), mappings));
    CohortIndicator newOnARTNegativeScreening =
        eptsGeneralIndicator.getIndicator(
            "newOnARTNegativeScreening",
            EptsReportUtils.map(txTbCohortQueries.newOnARTNegativeScreening(), mappings));
    CohortIndicator specimenSent =
        eptsGeneralIndicator.getIndicator(
            "specimenSent", EptsReportUtils.map(txTbCohortQueries.specimenSent(), mappings));
    CohortIndicator smearMicroscopyOnly =
        eptsGeneralIndicator.getIndicator(
            "smearMicroscopyOnly",
            EptsReportUtils.map(txTbCohortQueries.smearMicroscopyOnly(), mappings));
    CohortIndicator mWRD =
        eptsGeneralIndicator.getIndicator(
            "mWRD", EptsReportUtils.map(txTbCohortQueries.mWRD(), mappings));
    CohortIndicator otherAdditionalTest =
        eptsGeneralIndicator.getIndicator(
            "otherAdditionalTest",
            EptsReportUtils.map(txTbCohortQueries.otherAdditionalTest(), mappings));
    CohortIndicator positiveResultsReturned =
        eptsGeneralIndicator.getIndicator(
            "positiveInvestigationResult",
            EptsReportUtils.map(txTbCohortQueries.positiveResultsReturned(), mappings));

    CohortIndicator symptomScreen =
        eptsGeneralIndicator.getIndicator(
            "Symptom Screen Alone",
            EptsReportUtils.map(txTbCohortQueries.getSymtomScreen(), mappings));

    CohortIndicator screenedUsingCXR =
        eptsGeneralIndicator.getIndicator(
            "Patients screened using CXR",
            EptsReportUtils.map(txTbCohortQueries.getPatientsScreenedUsingCXR(), mappings));

    dataSetDefinition.addColumn(
        "TXB_DEN",
        "TX_TB: Denominator total",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "Denominator Total",
                EptsReportUtils.map(txTbCohortQueries.getDenominator(), mappings)),
            mappings),
        "");

    addRow(
        dataSetDefinition,
        "TXB_DEN_NEW_POS",
        "Denominator (newOnARTPositiveScreening)",
        EptsReportUtils.map(newOnARTPositiveScreening, mappings),
        dissagregations());
    addRow(
        dataSetDefinition,
        "TXB_DEN_NEW_NEG",
        "Denominator (newOnARTNegativeScreening)",
        EptsReportUtils.map(newOnARTNegativeScreening, mappings),
        dissagregations());
    addRow(
        dataSetDefinition,
        "TXB_DEN_PREV_POS",
        "Denominator (previouslyOnARTPositiveScreening)",
        EptsReportUtils.map(previouslyOnARTPostiveScreening, mappings),
        dissagregations());
    addRow(
        dataSetDefinition,
        "TXB_DEN_PREV_NEG",
        "Denominator (previouslyOnARTNegativeScreening)",
        EptsReportUtils.map(previouslyOnARTNegativeScreening, mappings),
        dissagregations());

    dataSetDefinition.addColumn(
        "TXB_DEN_SPEC_SENT",
        "TX_TB: Denominator (Specimen Sent)",
        EptsReportUtils.map(specimenSent, mappings),
        "");

    dataSetDefinition.addColumn(
        "TXB_DEN_SMEAR_ONLY",
        "TX_TB: Denominator (Diagnostic Test - Smear Only)",
        EptsReportUtils.map(smearMicroscopyOnly, mappings),
        "");

    dataSetDefinition.addColumn(
        "TXB_DEN_M_WRD",
        "TX_TB: Denominator (Diagnostic Test - M WRD)",
        EptsReportUtils.map(mWRD, mappings),
        "");

    dataSetDefinition.addColumn(
        "TXB_DEN_OTHER",
        "TX_TB: Denominator (Diagnostic Test - Other No Xpert)",
        EptsReportUtils.map(otherAdditionalTest, mappings),
        "");

    dataSetDefinition.addColumn(
        "TXB_DEN_POS_RES_RET",
        "TX_TB: Denominator (Positive Result Returned)",
        EptsReportUtils.map(positiveResultsReturned, mappings),
        "");

    dataSetDefinition.addColumn(
        "SSA", "Symptom Screen Alone", EptsReportUtils.map(symptomScreen, mappings), "");

    dataSetDefinition.addColumn(
        "CXR", "Patients screened using CXR", EptsReportUtils.map(screenedUsingCXR, mappings), "");
  }

  private List<ColumnParameters> dissagregations() {

    ColumnParameters under1M =
        new ColumnParameters(
            "under1M",
            "under 1 year male",
            EptsCommonDimensionKey.of(DimensionKeyForAge.bellowOneYear)
                .and(DimensionKeyForGender.male)
                .getDimensions(),
            "M1");

    ColumnParameters oneTo4M =
        new ColumnParameters(
            "oneTo4M",
            "1 - 4 years male",
            EptsCommonDimensionKey.of(DimensionKeyForAge.betweenOneAnd4Years)
                .and(DimensionKeyForGender.male)
                .getDimensions(),
            "M2");

    ColumnParameters fiveTo9M =
        new ColumnParameters(
            "fiveTo9M",
            "5 - 9 years male",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between5And9Years)
                .and(DimensionKeyForGender.male)
                .getDimensions(),
            "M3");

    ColumnParameters under1F =
        new ColumnParameters(
            "under1F",
            "under 1 year female",
            EptsCommonDimensionKey.of(DimensionKeyForAge.bellowOneYear)
                .and(DimensionKeyForGender.female)
                .getDimensions(),
            "F1");

    ColumnParameters oneTo4F =
        new ColumnParameters(
            "oneTo4F",
            "1 - 4 years female",
            EptsCommonDimensionKey.of(DimensionKeyForAge.betweenOneAnd4Years)
                .and(DimensionKeyForGender.female)
                .getDimensions(),
            "F2");

    ColumnParameters fiveTo9F =
        new ColumnParameters(
            "fiveTo9F",
            "5 - 9 years female",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between5And9Years)
                .and(DimensionKeyForGender.female)
                .getDimensions(),
            "F3");

    ColumnParameters unknownM =
        new ColumnParameters(
            "unknownM",
            "Unknown age male",
            EptsCommonDimensionKey.of(DimensionKeyForAge.unknown)
                .and(DimensionKeyForGender.male)
                .getDimensions(),
            "UNKM");

    ColumnParameters tenTo14M =
        new ColumnParameters(
            "tenTo14M",
            "10 - 14 male",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between10And14Years)
                .and(DimensionKeyForGender.male)
                .getDimensions(),
            "M4");

    ColumnParameters fifteenTo19M =
        new ColumnParameters(
            "fifteenTo19M",
            "15 - 19 male",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between15And19Years)
                .and(DimensionKeyForGender.male)
                .getDimensions(),
            "M5");

    ColumnParameters twentyTo24M =
        new ColumnParameters(
            "twentyTo24M",
            "20 - 24 male",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between20And24Years)
                .and(DimensionKeyForGender.male)
                .getDimensions(),
            "M6");

    ColumnParameters twenty5To29M =
        new ColumnParameters(
            "twenty4To29M",
            "25 - 29 male",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between25And29Years)
                .and(DimensionKeyForGender.male)
                .getDimensions(),
            "M7");

    ColumnParameters thirtyTo34M =
        new ColumnParameters(
            "thirtyTo34M",
            "30 - 34 male",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between30And34Years)
                .and(DimensionKeyForGender.male)
                .getDimensions(),
            "M8");

    ColumnParameters thirty5To39M =
        new ColumnParameters(
            "thirty5To39M",
            "35 - 39 male",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between35And39Years)
                .and(DimensionKeyForGender.male)
                .getDimensions(),
            "M9");

    ColumnParameters fortyTo44M =
        new ColumnParameters(
            "fortyTo44M",
            "40 - 44 male",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between40And44Years)
                .and(DimensionKeyForGender.male)
                .getDimensions(),
            "M10");

    ColumnParameters forty5To49M =
        new ColumnParameters(
            "forty5To49M",
            "45 - 49 male",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between45and49Years)
                .and(DimensionKeyForGender.male)
                .getDimensions(),
            "M11");

    ColumnParameters fiftyTo54M =
        new ColumnParameters(
            "fiftyTo54M",
            "50 - 54 male",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between50And54Years)
                .and(DimensionKeyForGender.male)
                .getDimensions(),
            "M12");

    ColumnParameters fifty5To59M =
        new ColumnParameters(
            "fifty5To59M",
            "55 - 59 male",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between55And59Years)
                .and(DimensionKeyForGender.male)
                .getDimensions(),
            "M13");

    ColumnParameters sixtyTo64M =
        new ColumnParameters(
            "sixtyTo64M",
            "60 - 64 male",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between60And64Years)
                .and(DimensionKeyForGender.male)
                .getDimensions(),
            "M14");

    ColumnParameters above65M =
        new ColumnParameters(
            "above65M",
            "65+ male",
            EptsCommonDimensionKey.of(DimensionKeyForAge.overOrEqualTo65Years)
                .and(DimensionKeyForGender.male)
                .getDimensions(),
            "M15");

    ColumnParameters unknownF =
        new ColumnParameters(
            "unknownF",
            "Unknown age female",
            EptsCommonDimensionKey.of(DimensionKeyForAge.unknown)
                .and(DimensionKeyForGender.female)
                .getDimensions(),
            "UNKF");

    ColumnParameters tenTo14F =
        new ColumnParameters(
            "tenTo14F",
            "10 - 14 female",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between10And14Years)
                .and(DimensionKeyForGender.female)
                .getDimensions(),
            "F4");

    ColumnParameters fifteenTo19F =
        new ColumnParameters(
            "fifteenTo19F",
            "15 - 19 female",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between15And19Years)
                .and(DimensionKeyForGender.female)
                .getDimensions(),
            "F5");

    ColumnParameters twentyTo24F =
        new ColumnParameters(
            "twentyTo24F",
            "20 - 24 female",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between20And24Years)
                .and(DimensionKeyForGender.female)
                .getDimensions(),
            "F6");

    ColumnParameters twenty5To29F =
        new ColumnParameters(
            "twenty4To29F",
            "25 - 29 female",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between25And29Years)
                .and(DimensionKeyForGender.female)
                .getDimensions(),
            "F7");

    ColumnParameters thirtyTo34F =
        new ColumnParameters(
            "thirtyTo34F",
            "30 - 34 female",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between30And34Years)
                .and(DimensionKeyForGender.female)
                .getDimensions(),
            "F8");

    ColumnParameters thirty5To39F =
        new ColumnParameters(
            "thirty5To39F",
            "35 - 39 female",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between35And39Years)
                .and(DimensionKeyForGender.female)
                .getDimensions(),
            "F9");

    ColumnParameters fortyTo44F =
        new ColumnParameters(
            "fortyTo44F",
            "40 - 44 female",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between40And44Years)
                .and(DimensionKeyForGender.female)
                .getDimensions(),
            "F10");

    ColumnParameters forty5To49F =
        new ColumnParameters(
            "forty5To49F",
            "45 - 49 female",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between45and49Years)
                .and(DimensionKeyForGender.female)
                .getDimensions(),
            "F11");

    ColumnParameters fiftyTo54F =
        new ColumnParameters(
            "fiftyTo54F",
            "50 - 54 female",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between50And54Years)
                .and(DimensionKeyForGender.female)
                .getDimensions(),
            "F12");

    ColumnParameters fifty5To59F =
        new ColumnParameters(
            "fifty5To59F",
            "55 - 59 female",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between55And59Years)
                .and(DimensionKeyForGender.female)
                .getDimensions(),
            "F13");

    ColumnParameters sixtyTo64F =
        new ColumnParameters(
            "sixtyTo64F",
            "60 - 64 female",
            EptsCommonDimensionKey.of(DimensionKeyForAge.between60And64Years)
                .and(DimensionKeyForGender.female)
                .getDimensions(),
            "F14");

    ColumnParameters above65F =
        new ColumnParameters(
            "above65F",
            "65+ female",
            EptsCommonDimensionKey.of(DimensionKeyForAge.overOrEqualTo65Years)
                .and(DimensionKeyForGender.female)
                .getDimensions(),
            "F15");

    return Arrays.asList(
        under1M,
        oneTo4M,
        fiveTo9M,
        under1F,
        oneTo4F,
        fiveTo9F,
        unknownM,
        tenTo14M,
        fifteenTo19M,
        twentyTo24M,
        twenty5To29M,
        thirtyTo34M,
        thirty5To39M,
        fortyTo44M,
        forty5To49M,
        fiftyTo54M,
        fifty5To59M,
        sixtyTo64M,
        above65M,
        unknownF,
        tenTo14F,
        fifteenTo19F,
        twentyTo24F,
        twenty5To29F,
        thirtyTo34F,
        thirty5To39F,
        fortyTo44F,
        forty5To49F,
        fiftyTo54F,
        fifty5To59F,
        sixtyTo64F,
        above65F);
  }
}
