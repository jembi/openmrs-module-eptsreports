package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.library.cohorts.IntensiveMonitoringCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.dimensions.AgeDimensionCohortInterface;
import org.openmrs.module.eptsreports.reporting.library.dimensions.EptsCommonDimension;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class IntensiveMonitoringDataSet extends BaseDataSet {

  @Autowired private EptsCommonDimension eptsCommonDimension;

  @Autowired private EptsGeneralIndicator eptsGeneralIndicator;

  @Autowired private IntensiveMonitoringCohortQueries intensiveMonitoringCohortQueries;

  @Autowired
  @Qualifier("commonAgeDimensionCohort")
  private AgeDimensionCohortInterface ageDimensionCohort;

  public DataSetDefinition constructIntensiveMonitoringDataSet() {

    CohortIndicatorDataSetDefinition dataSetDefinition = new CohortIndicatorDataSetDefinition();
    dataSetDefinition.setName("Intensive Monitoring DataSet");
    dataSetDefinition.addParameters(getParameters());

    // Category 12 denominator indicators Part 1
    CohortIndicator MI12DEN1 =
        eptsGeneralIndicator.getIndicator(
            "MI12DEN1",
            EptsReportUtils.map(
                intensiveMonitoringCohortQueries.getMI12DEN(),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI12DEN1.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI12DEN1",
        "# de adultos (15/+anos) que iniciaram o TARV no período de inclusão e que retornaram para uma consulta clínica ou levantamento de ARVs dentro de 33 dias após o início do TARV",
        EptsReportUtils.map(
            MI12DEN1,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // Category 12 numerator indicators
    CohortIndicator MI12NUM1 =
        eptsGeneralIndicator.getIndicator(
            "MI12NUM1",
            EptsReportUtils.map(
                intensiveMonitoringCohortQueries.getMI12NUM(),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI12NUM1.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI12NUM1",
        "# de adultos (15/+anos) que iniciaram o TARV no período de inclusão e que retornaram para uma consulta clínica ou levantamento de ARVs entre 25 a 33 dias após o início do TARV",
        EptsReportUtils.map(
            MI12NUM1,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    return dataSetDefinition;
  }

  @Override
  public List<Parameter> getParameters() {
    return Arrays.asList(
        new Parameter("startDate", "Data Inicial Inclusão", Date.class),
        new Parameter("endDate", "Data Final Inclusão", Date.class),
        new Parameter("revisionEndDate", "Data Final Revisão", Date.class),
        new Parameter("location", "Unidade Sanitária", Location.class));
  }

  private CohortIndicator customCohortIndicator(CohortDefinition cd, String mapping) {
    CohortIndicator cohortIndicator =
        eptsGeneralIndicator.getIndicator(cd.getName(), EptsReportUtils.map(cd, mapping));
    cohortIndicator.addParameter(new Parameter("revisionEndDate", "Revision Date", Date.class));
    return cohortIndicator;
  }
}
