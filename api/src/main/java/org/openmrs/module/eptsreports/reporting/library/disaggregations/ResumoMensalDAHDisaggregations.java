package org.openmrs.module.eptsreports.reporting.library.disaggregations;

import java.util.Arrays;
import java.util.List;
import org.openmrs.module.eptsreports.reporting.library.datasets.BaseDataSet;
import org.springframework.stereotype.Component;

@Component
public class ResumoMensalDAHDisaggregations {
  /**
   * Method to combine dimensions by Age and Gender to fill indicators from 0 to 7
   *
   * @return {@link BaseDataSet}
   */
  public List<BaseDataSet.ColumnParameters> get0to7ColumnDisaggregations() {
    BaseDataSet.ColumnParameters tenTo14Male =
        new BaseDataSet.ColumnParameters(
            "tenTo14Male", "10 to 14 years male", "gender=M|age=10-14", "01");
    BaseDataSet.ColumnParameters tenTo14Female =
        new BaseDataSet.ColumnParameters(
            "tenTo14Female", "10 to 14 years female", "gender=F|age=10-14", "02");
    BaseDataSet.ColumnParameters fifteenTo19Male =
        new BaseDataSet.ColumnParameters(
            "fifteenTo19Male", "15 to 19 years male", "gender=M|age=15-19", "03");
    BaseDataSet.ColumnParameters fifteenTo19Female =
        new BaseDataSet.ColumnParameters(
            "fifteenTo19Female", "15 to 19 years female", "gender=F|age=15-19", "04");
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

  /**
   * Method to combine dimensions by Art Status, Age, Pregnancy and Followup on DAH to fill
   * indicators 8 and 9
   *
   * @return {@link BaseDataSet}
   */
  public List<BaseDataSet.ColumnParameters> get8and9ColumnDisaggregations() {

    // NEW ON ART
    BaseDataSet.ColumnParameters under15NewArt89 =
        new BaseDataSet.ColumnParameters(
            "under15NewArt89",
            "Under 15 New on Art",
            "art=new-art-dah-89|age=<15",
            "under15NewArt89");

    BaseDataSet.ColumnParameters above15NewArt89 =
        new BaseDataSet.ColumnParameters(
            "above15NewArt89",
            "Above 15 New on Art",
            "art=new-art-dah-89|age=15+",
            "above15NewArt89");

    // RESTARTED ART
    BaseDataSet.ColumnParameters under15RestartedArt89 =
        new BaseDataSet.ColumnParameters(
            "under15RestartedArt",
            "Under 15 RestartedArt",
            "art=restart-art-dah-89|age=<15",
            "under15RestartedArt89");

    BaseDataSet.ColumnParameters above15RestartedArt89 =
        new BaseDataSet.ColumnParameters(
            "above15RestartedArt",
            "Above 15 Restarted Art",
            "art=restart-art-dah-89|age=15+",
            "above15RestartedArt89");

    // ACTIVE ON ART
    BaseDataSet.ColumnParameters under15ActiveArt89 =
        new BaseDataSet.ColumnParameters(
            "under15ActiveArt",
            "Under 15 Active on Art",
            "art=on-art-dah-89|age=<15",
            "under15ActiveArt89");

    BaseDataSet.ColumnParameters above15ActiveArt89 =
        new BaseDataSet.ColumnParameters(
            "above15ActiveArt",
            "Above 15 Active on Art",
            "art=on-art-dah-89|age=15+",
            "above15ActiveArt89");

    // PREGNANT
    BaseDataSet.ColumnParameters pregnant89 =
        new BaseDataSet.ColumnParameters(
            "pregnant", "Pregnant", "maternity=pregnant-dah", "pregnant");

    return Arrays.asList(
        under15NewArt89,
        above15NewArt89,
        under15RestartedArt89,
        above15RestartedArt89,
        under15ActiveArt89,
        above15ActiveArt89,
        pregnant89);
  }

  /**
   * Method to combine dimensions by Art Status, Age, Pregnancy and Followup on DAH to fill
   * indicators from 10 to 19
   *
   * @return {@link BaseDataSet}
   */
  public List<BaseDataSet.ColumnParameters> get10to19ColumnDisaggregations() {

    // NEW ON ART
    BaseDataSet.ColumnParameters under15NewArt =
        new BaseDataSet.ColumnParameters(
            "under15NewArt", "Under 15 New on Art", "art=new-art-dah|age=<15", "under15NewArt");

    BaseDataSet.ColumnParameters above15NewArt =
        new BaseDataSet.ColumnParameters(
            "above15NewArt", "Above 15 New on Art", "art=new-art-dah|age=15+", "above15NewArt");

    // RESTARTED ART
    BaseDataSet.ColumnParameters under15RestartedArt =
        new BaseDataSet.ColumnParameters(
            "under15RestartedArt",
            "Under 15 RestartedArt",
            "art=restart-art-dah|age=<15",
            "under15RestartedArt");

    BaseDataSet.ColumnParameters above15RestartedArt =
        new BaseDataSet.ColumnParameters(
            "above15RestartedArt",
            "Above 15 Restarted Art",
            "art=restart-art-dah|age=15+",
            "above15RestartedArt");

    // ACTIVE ON ART
    BaseDataSet.ColumnParameters under15ActiveArt =
        new BaseDataSet.ColumnParameters(
            "under15ActiveArt",
            "Under 15 Active on Art",
            "art=on-art-dah|age=<15",
            "under15ActiveArt");

    BaseDataSet.ColumnParameters above15ActiveArt =
        new BaseDataSet.ColumnParameters(
            "above15ActiveArt",
            "Above 15 Active on Art",
            "art=on-art-dah|age=15+",
            "above15ActiveArt");

    // PREGNANT
    BaseDataSet.ColumnParameters pregnant =
        new BaseDataSet.ColumnParameters(
            "pregnant", "Pregnant", "maternity=pregnant-dah", "pregnant");

    // FOLLOWUP
    BaseDataSet.ColumnParameters under15Followup =
        new BaseDataSet.ColumnParameters(
            "under15Followup", "Under 15 on Followup", "age=<15", "under15Followup");

    BaseDataSet.ColumnParameters above15Followup =
        new BaseDataSet.ColumnParameters(
            "above15Followup", "Above 15 on Followup", "age=15+", "above15Followup");

    return Arrays.asList(
        under15NewArt,
        above15NewArt,
        under15RestartedArt,
        above15RestartedArt,
        under15ActiveArt,
        above15ActiveArt,
        pregnant,
        under15Followup,
        above15Followup);
  }
}
