package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.openmrs.Location;
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
    cohortIndicatorDefinition.addParameters(getParameters());

    cohortIndicatorDefinition.addDimension(
        "age",
        EptsReportUtils.map(
            eptsCommonDimension.age(ageDimensionCohort), "effectiveDate=${endDate}"));

    CohortIndicator TXCURR =
        eptsGeneralIndicator.getIndicator(
            "TXCURRTOTAL",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getTxCurrOrTxCurrWithClinicalConsultation(
                    TxTbMonthlyCascadeCohortQueries.Indicator1and2Composition.TXCURR),
                "startDate=${endDate-6m},endDate=${endDate},location=${location}"));

    cohortIndicatorDefinition.addColumn(
        "TXCURRTOTAL",
        "TXCURR TOTAL",
        EptsReportUtils.map(TXCURR, "endDate=${endDate},location=${location}"),
        "");

    CohortIndicator Clinical =
        eptsGeneralIndicator.getIndicator(
            "CLINICAL",
            EptsReportUtils.map(
                txTbMonthlyCascadeCohortQueries.getPatientsGeneXpertMtbRif(),
                "startDate=${endDate-6m},endDate=${endDate},location=${location}"));

    cohortIndicatorDefinition.addColumn(
        "CLINICAL",
        "FICHA CLINICA",
        EptsReportUtils.map(Clinical, "endDate=${endDate},location=${location}"),
        "");

    return cohortIndicatorDefinition;
  }

  @Override
  public List<Parameter> getParameters() {
    return Arrays.asList(
        new Parameter("endDate", "endDate", Date.class),
        new Parameter("location", "Location", Location.class));
  }
}
