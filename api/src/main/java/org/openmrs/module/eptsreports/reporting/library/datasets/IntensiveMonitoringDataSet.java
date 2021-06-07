package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.library.cohorts.IntensiveMonitoringCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.dimensions.EptsCommonDimension;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IntensiveMonitoringDataSet extends BaseDataSet {

  private EptsGeneralIndicator eptsGeneralIndicator;

  private IntensiveMonitoringCohortQueries intensiveMonitoringCohortQueries;

  private EptsCommonDimension eptsCommonDimension;

  @Autowired
  public IntensiveMonitoringDataSet(
      EptsGeneralIndicator eptsGeneralIndicator,
      IntensiveMonitoringCohortQueries intensiveMonitoringCohortQueries,
      EptsCommonDimension eptsCommonDimension) {
    this.eptsGeneralIndicator = eptsGeneralIndicator;
    this.intensiveMonitoringCohortQueries = intensiveMonitoringCohortQueries;
    this.eptsCommonDimension = eptsCommonDimension;
  }

  public DataSetDefinition constructIntensiveMonitoringDataSet() {

    CohortIndicatorDataSetDefinition dataSetDefinition = new CohortIndicatorDataSetDefinition();
    dataSetDefinition.setName("Intensive Monitoring DataSet");
    dataSetDefinition.addParameters(getParameters());
    // dimensions to be added here
    dataSetDefinition.addDimension(
        "ageBasedOnArt135",
        EptsReportUtils.map(
            eptsCommonDimension.ageBasedOnArtStartDateMOH(),
            "onOrAfter=${revisionEndDate-2m+1d},onOrBefore=${revisionEndDate-1m},location=${location}"));
    dataSetDefinition.addDimension(
        "ageBasedOnArt246",
        EptsReportUtils.map(
            eptsCommonDimension.ageBasedOnArtStartDateMOH(),
            "onOrAfter=${revisionEndDate-8m+1d},onOrBefore=${revisionEndDate-7m},location=${location}"));
    // CAT 7 Denominator
    // 7.1
    dataSetDefinition.addColumn(
        "MI7DEN1",
        "% de adultos HIV+ em TARV elegíveis ao TPT e que iniciaram TPT",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7DenMOHIV202171Definition(1, "DEN"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt135=adultsArt");
    // 7.2
    dataSetDefinition.addColumn(
        "MI7DEN2",
        "% de adultos HIV+ em TARV elegiveis ao TPT que iniciaram e  completaram TPT",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7DenMOHIV202172Definition(2, "DEN"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt246=adultsArt");
    // 7.3
    dataSetDefinition.addColumn(
        "MI7DEN3",
        "% de crianças HIV+ em TARV elegiveis ao TPT  e que iniciaram TPT",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7DenMOHIV202171Definition(3, "DEN"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt135=childrenArt");
    // 7.4
    dataSetDefinition.addColumn(
        "MI7DEN4",
        "% de crianças HIV+ em TARV elegíveis que iniciaram e completaram TPT",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7DenMOHIV202172Definition(4, "DEN"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt246=childrenArt");
    // 7.5
    dataSetDefinition.addColumn(
        "MI7DEN5",
        "% de mulheres grávidas HIV+ elegíveis ao TPI e que iniciaram TPI",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7DenMOHIV202171Definition(5, "DEN"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    // 7.6
    dataSetDefinition.addColumn(
        "MI7DEN6",
        "% de MG HIV+ em TARV que iniciou TPI e que terminou TPI",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7DenMOHIV202172Definition(6, "DEN"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // CAT 7 Numerator
    // 7.1
    dataSetDefinition.addColumn(
        "MI7NUM1",
        "% de adultos HIV+ em TARV elegíveis ao TPT e que iniciaram TPT",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7DenMOHIV202171Definition(1, "NUM"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt135=adultsArt");
    // 7.2
    dataSetDefinition.addColumn(
        "MI7NUM2",
        "% de adultos HIV+ em TARV elegiveis ao TPT que iniciaram e  completaram TPT",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7DenMOHIV202172Definition(2, "NUM"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt246=adultsArt");
    // 7.3
    dataSetDefinition.addColumn(
        "MI7NUM3",
        "% de crianças HIV+ em TARV elegiveis ao TPT  e que iniciaram TPT",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7DenMOHIV202171Definition(3, "NUM"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt135=childrenArt");
    // 7.4
    dataSetDefinition.addColumn(
        "MI7NUM4",
        "% de crianças HIV+ em TARV elegíveis que iniciaram e completaram TPT",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7DenMOHIV202172Definition(4, "NUM"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt246=childrenArt");
    // 7.5
    dataSetDefinition.addColumn(
        "MI7NUM5",
        "% de mulheres grávidas HIV+ elegíveis ao TPI e que iniciaram TPI",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7DenMOHIV202171Definition(5, "NUM"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    // 7.6
    dataSetDefinition.addColumn(
        "MI7NUM6",
        "% de MG HIV+ em TARV que iniciou TPI e que terminou TPI",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7DenMOHIV202172Definition(6, "NUM"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // CAT 13 P2 DENOMINATOR
    // 13.15
    dataSetDefinition.addColumn(
        "MI13DEN15",
        "% de MG elegíveis a CV com registo de pedido de CV feito pelo clínico (MG que iniciaram TARV na CPN) Denominator: # de MG com registo de início do TARV na CPN dentro do período de inclusão. (Line 90,Column F in the Template) as following",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMICat13Part2(15, "DEN15"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // 13.16
    dataSetDefinition.addColumn(
        "MI13DEN16",
        "% de MG elegíveis a CV com registo de pedido de CV feito pelo clínico na primeira CPN (MG que entraram em TARV na CPN) Denominator:# de MG que tiveram a primeira CPN no período de inclusão, e que já estavam em TARV há mais de 3 meses",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMICat13Part2(16, "DEN16"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // 13.17
    dataSetDefinition.addColumn(
        "MI13DEN17",
        "% de MG que receberam o resultado da Carga Viral dentro de 33 dias após pedido Denominator: # de MG com registo de pedido de CV no período de revisão",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMICat13Part2(17, "DEN17"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // CAT 13 P2 NUMERATOR
    // 13.15
    dataSetDefinition.addColumn(
        "MI13NUM15",
        "% de MG elegíveis a CV com registo de pedido de CV feito pelo clínico (MG que iniciaram TARV na CPN) Denominator: # de MG com registo de início do TARV na CPN dentro do período de inclusão. (Line 90,Column F in the Template) as following",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMICat13Part2(15, "NUM15"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // 13.16
    dataSetDefinition.addColumn(
        "MI13NUM16",
        "% de MG elegíveis a CV com registo de pedido de CV feito pelo clínico na primeira CPN (MG que entraram em TARV na CPN) Denominator:# de MG que tiveram a primeira CPN no período de inclusão, e que já estavam em TARV há mais de 3 meses",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMICat13Part2(16, "NUM16"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // 13.17
    dataSetDefinition.addColumn(
        "MI13NUM17",
        "% de MG que receberam o resultado da Carga Viral dentro de 33 dias após pedido Denominator: # de MG com registo de pedido de CV no período de revisão",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMICat13Part2(17, "NUM17"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    return dataSetDefinition;
  }

  @Override
  public List<Parameter> getParameters() {
    return Arrays.asList(
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
