package org.openmrs.module.eptsreports.reporting.library.datasets;

import org.openmrs.module.eptsreports.reporting.library.cohorts.TxTbMonthlyCascadeCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.dimensions.AgeDimensionCohortInterface;
import org.openmrs.module.eptsreports.reporting.library.dimensions.EptsCommonDimension;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Component
public class TxTbMonthlyCascadeDataset extends BaseDataSet {

  @Autowired private TxTbMonthlyCascadeCohortQueries txTbMonthlyCascadeCohortQueries;

  @Autowired private EptsGeneralIndicator eptsGeneralIndicator;
  @Autowired private EptsCommonDimension eptsCommonDimension;

  @Autowired
  @Qualifier("commonAgeDimensionCohort")
  private AgeDimensionCohortInterface ageDimensionCohort;

  public DataSetDefinition constructTXTBMonthlyDataset() {

    CohortIndicatorDataSetDefinition cohortIndicatorDefinition =
        new CohortIndicatorDataSetDefinition();
    cohortIndicatorDefinition.setName("TX_TB Monthly Cascade Dataset");
    cohortIndicatorDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    cohortIndicatorDefinition.addParameter(new Parameter("location", "Facilities", Locale.class));

    cohortIndicatorDefinition.addDimension(
        "age",
        EptsReportUtils.map(
            eptsCommonDimension.age(ageDimensionCohort), "effectiveDate=${endDate}"));

    CohortIndicator MQC3D1 =
        eptsGeneralIndicator.getIndicator(
            "TXCURR",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getTxCurr(),
                "endDate=${endDate},location=${location}"));

    addRow(
        cohortIndicatorDefinition,
        "TXCURR",
        " Indicador 1 - Tx Curr ",
        EptsReportUtils.map(MQC3D1, "endDate=${endDate},location=${location}"),
        getDisagsForAdultsAndChildrenBasedOnArtStartDateColumn());

    return cohortIndicatorDefinition;
  }

  private List<ColumnParameters> getDisagsForAdultsAndChildrenBasedOnArtStartDateColumn() {
    ColumnParameters adults = new ColumnParameters("ADULTOS", "Adultos", "age=<1", "ADULTOS");
    ColumnParameters children = new ColumnParameters("CRIANCAS", "Criancas", "age=1-4", "CRIANCAS");
    return Arrays.asList(adults, children);
  }
}
