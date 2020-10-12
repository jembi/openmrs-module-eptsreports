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
package org.openmrs.module.eptsreports.reporting.library.disaggregations;

import java.util.Arrays;
import java.util.List;
import org.openmrs.module.eptsreports.reporting.library.datasets.BaseDataSet.ColumnParameters;
import org.springframework.stereotype.Component;

@Component
public class APSSResumoTrimestraldisaggregations {

  public static List<ColumnParameters> getAPSSPPDisagg() {
    ColumnParameters zeroTo14yearsMale =
        new ColumnParameters(
            "zeroTo14yearsMale", "0 to  14 years male patients", "gender=M|age=0-14", "01");
    ColumnParameters zeroTo14yearsFemale =
        new ColumnParameters(
            "zeroTo14yearsFemale", "0 to  14 years female patients", "gender=F|age=0-14", "02");
    ColumnParameters zeroTo14yearsTotal =
        new ColumnParameters("zeroTo14yearsFemale", "0 to  14 years patients", "age=0-14", "03");
    ColumnParameters fifteenYearsPlusYearsM =
        new ColumnParameters(
            "fifteenYearsPlusYearsM", "15 years plus male patients", "gender=M|age=15+", "04");
    ColumnParameters fifteenYearsPlusYearsF =
        new ColumnParameters(
            "fifteenYearsPlusYearsF", "15 years plus female patients", "gender=F|age=15-19", "05");
    ColumnParameters adultsTotal =
        new ColumnParameters("adultsTotal", "Adults patients - Totals", "age=15+", "06");

    return Arrays.asList(
        zeroTo14yearsMale,
        zeroTo14yearsFemale,
        zeroTo14yearsTotal,
        fifteenYearsPlusYearsM,
        fifteenYearsPlusYearsF,
        adultsTotal);
  }
}
