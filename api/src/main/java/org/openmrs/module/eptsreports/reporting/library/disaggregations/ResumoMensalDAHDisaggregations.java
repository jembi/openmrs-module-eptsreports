package org.openmrs.module.eptsreports.reporting.library.disaggregations;

import java.util.Arrays;
import java.util.List;
import org.openmrs.module.eptsreports.reporting.library.datasets.BaseDataSet;
import org.springframework.stereotype.Component;

@Component
public class ResumoMensalDAHDisaggregations {

  public List<BaseDataSet.ColumnParameters> getColumnDisaggregations() {
    BaseDataSet.ColumnParameters tenTo14Male =
        new BaseDataSet.ColumnParameters(
            "tenTo14Male", "10 to 14 years male", "gender=M|age=10-14", "01");
    BaseDataSet.ColumnParameters tenTo14Female =
        new BaseDataSet.ColumnParameters(
            "tenTo14Female", "10 to 14 years female", "gender=F|age=10-14", "02");
    BaseDataSet.ColumnParameters fifteenTo19Female =
        new BaseDataSet.ColumnParameters(
            "fifteenTo19Female", "15 to 19 years female", "gender=F|age=15-19", "03");
    BaseDataSet.ColumnParameters fifteenTo19Male =
        new BaseDataSet.ColumnParameters(
            "fifteenTo19Male", "15 to 19 years male", "gender=M|age=15-19", "04");
    BaseDataSet.ColumnParameters above20YearsM =
        new BaseDataSet.ColumnParameters(
            "above20YearsF", "Above 20 years male patients", "gender=M|age=20+", "05");
    BaseDataSet.ColumnParameters above20YearsF =
        new BaseDataSet.ColumnParameters(
            "above20YearsF", "Above 20 years female patients", "gender=F|age=20+", "06");
    BaseDataSet.ColumnParameters fiveTo9 =
        new BaseDataSet.ColumnParameters("fiveTo9", "5 to 9 years", "age=5-9", "07");
    BaseDataSet.ColumnParameters under5 =
        new BaseDataSet.ColumnParameters("under5", "under 5 years patients", "age=0-4", "08");

    return Arrays.asList(
        tenTo14Male,
        tenTo14Female,
        fifteenTo19Female,
        fifteenTo19Male,
        above20YearsM,
        above20YearsF,
        fiveTo9,
        under5);
  }
}
