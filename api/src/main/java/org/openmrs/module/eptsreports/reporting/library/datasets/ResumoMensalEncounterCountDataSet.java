package org.openmrs.module.eptsreports.reporting.library.datasets;

import org.openmrs.module.eptsreports.reporting.library.cohorts.ResumoMensalCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.dataset.definition.SimpleIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResumoMensalEncounterCountDataSet extends BaseDataSet {

  private ResumoMensalCohortQueries resumoMensalCohortQueries;

  private EptsGeneralIndicator eptsGeneralIndicator;

  @Autowired
  public ResumoMensalEncounterCountDataSet(ResumoMensalCohortQueries resumoMensalCohortQueries) {
    this.resumoMensalCohortQueries = resumoMensalCohortQueries;
  }

  public DataSetDefinition constructEncounterCountDataset() {
    SimpleIndicatorDataSetDefinition sidsd = new SimpleIndicatorDataSetDefinition();
    sidsd.setName("Count Encounter DSD");
    sidsd.addParameters(getParameters());
    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";
    sidsd.addColumn(
        "F1",
        "Total encounter",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "Count Clinical Visits",
                resumoMensalCohortQueries.getNumberOfVisitsDuringTheReportingMonthF1()),
            mappings));

    return sidsd;
  }
}
