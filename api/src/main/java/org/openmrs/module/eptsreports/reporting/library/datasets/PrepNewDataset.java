package org.openmrs.module.eptsreports.reporting.library.datasets;

import org.openmrs.module.eptsreports.reporting.library.cohorts.PrepNewCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class PrepNewDataset extends BaseDataSet {

  private PrepNewCohortQueries prepNewCohortQueries;

  private EptsGeneralIndicator eptsGeneralIndicator;

  @Autowired
  public PrepNewDataset(
      PrepNewCohortQueries prepNewCohortQueries, EptsGeneralIndicator eptsGeneralIndicator) {
    this.prepNewCohortQueries = prepNewCohortQueries;
    this.eptsGeneralIndicator = eptsGeneralIndicator;
  }

  /**
   * <b>Description:</b> Constructs PrEP New Dataset
   *
   * @return
   */
  public DataSetDefinition constructPrepNewDataset() {

    CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
    dsd.setName("PrEP New Dataset");
    dsd.addParameters(getParameters());
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";

    // start building the datasets
    // get the column for the totals
    dsd.addColumn(
        "P1",
        "Total Clients PrEP",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "Total Clients Who Newly Initiated PrEP",
                EptsReportUtils.map(
                    prepNewCohortQueries.getClientsWhoNewlyInitiatedPrep(), mappings)),
            mappings),
        "");

    addRow(
            dsd,
            "P2",
            "Age and Gender",
            EptsReportUtils.map(
                    eptsGeneralIndicator.getIndicator(
                            "Age and Gender",
                            EptsReportUtils.map(
                                    prepNewCohortQueries.getClientsWhoNewlyInitiatedPrep(),
                                    mappings)),
                    mappings),
            getColumnsForAgeAndGenderAndKeyPop());


    return dsd;
  }

  /**
   * <b>Description:</b> Creates disaggregation based on Age and Gender
   *
   * @return
   */
  private List<ColumnParameters> getColumnsForAgeAndGenderAndKeyPop() {
    ColumnParameters fifteenTo19M =
            new ColumnParameters("fifteenTo19M", "15 - 19 male", "gender=M|age=15-19", "05");
    ColumnParameters twentyTo24M =
            new ColumnParameters("twentyTo24M", "20 - 24 male", "gender=M|age=20-24", "06");
    ColumnParameters twenty5To29M =
            new ColumnParameters("twenty4To29M", "25 - 29 male", "gender=M|age=25-29", "07");
    ColumnParameters thirtyTo34M =
            new ColumnParameters("thirtyTo34M", "30 - 34 male", "gender=M|age=30-34", "08");
    ColumnParameters thirty5To39M =
            new ColumnParameters("thirty5To39M", "35 - 39 male", "gender=M|age=35-39", "09");
    ColumnParameters foutyTo44M =
            new ColumnParameters("foutyTo44M", "40 - 44 male", "gender=M|age=40-44", "10");
    ColumnParameters fouty5To49M =
            new ColumnParameters("fouty5To49M", "45 - 49 male", "gender=M|age=45-49", "11");
    ColumnParameters above50M =
            new ColumnParameters("above50M", "50+ male", "gender=M|age=50+", "12");
    ColumnParameters unknownM =
            new ColumnParameters("unknownM", "Unknown age male", "gender=M|age=UK", "13");
    ColumnParameters totalM = new ColumnParameters("totalM", "Total of Males", "gender=M", "28");

    ColumnParameters under1F =
            new ColumnParameters("under1F", "under 1 year female", "gender=F|age=<1", "14");
    ColumnParameters oneTo4F =
            new ColumnParameters("oneTo4F", "1 - 4 years female", "gender=F|age=1-4", "15");
    ColumnParameters fiveTo9F =
            new ColumnParameters("fiveTo9F", "5 - 9 years female", "gender=F|age=5-9", "16");
    ColumnParameters tenTo14F =
            new ColumnParameters("tenTo14F", "10 - 14 female", "gender=F|age=10-14", "17");
    ColumnParameters fifteenTo19F =
            new ColumnParameters("fifteenTo19F", "15 - 19 female", "gender=F|age=15-19", "18");
    ColumnParameters twentyTo24F =
            new ColumnParameters("twentyTo24F", "20 - 24 female", "gender=F|age=20-24", "19");
    ColumnParameters twenty5To29F =
            new ColumnParameters("twenty4To29F", "25 - 29 female", "gender=F|age=25-29", "20");
    ColumnParameters thirtyTo34F =
            new ColumnParameters("thirtyTo34F", "30 - 34 female", "gender=F|age=30-34", "21");
    ColumnParameters thirty5To39F =
            new ColumnParameters("thirty5To39F", "35 - 39 female", "gender=F|age=35-39", "22");
    ColumnParameters foutyTo44F =
            new ColumnParameters("foutyTo44F", "40 - 44 female", "gender=F|age=40-44", "23");
    ColumnParameters fouty5To49F =
            new ColumnParameters("fouty5To49F", "45 - 49 female", "gender=F|age=45-49", "24");
    ColumnParameters above50F =
            new ColumnParameters("above50F", "50+ female", "gender=F|age=50+", "25");
    ColumnParameters unknownF =
            new ColumnParameters("unknownF", "Unknown age female", "gender=F|age=UK", "26");
    ColumnParameters total = new ColumnParameters("totals", "Totals", "", "27");
    ColumnParameters totalF = new ColumnParameters("totalF", "Total of Females", "gender=F", "29");

    // Key population
    ColumnParameters pid = new ColumnParameters("pid", "PID", "KP=PID", "30");
    ColumnParameters msm = new ColumnParameters("msm", "MSM", "KP=MSM", "31");
    ColumnParameters csw = new ColumnParameters("msm", "CSW", "KP=CSW", "32");
    ColumnParameters pri = new ColumnParameters("pri", "PRI", "KP=PRI", "33");

    return Arrays.asList(
            fifteenTo19M,
            twentyTo24M,
            twenty5To29M,
            thirtyTo34M,
            thirty5To39M,
            foutyTo44M,
            fouty5To49M,
            above50M,
            unknownM,
            totalM,
            under1F,
            oneTo4F,
            fiveTo9F,
            tenTo14F,
            fifteenTo19F,
            twentyTo24F,
            twenty5To29F,
            thirtyTo34F,
            thirty5To39F,
            foutyTo44F,
            fouty5To49F,
            above50F,
            unknownF,
            total,
            totalF,
            pid,
            msm,
            csw,
            pri);
  }
}
