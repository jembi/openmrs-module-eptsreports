package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.library.cohorts.IntensiveMonitoringCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.QualityImprovement2020CohortQueries;
import org.openmrs.module.eptsreports.reporting.library.dimensions.AgeDimensionCohortInterface;
import org.openmrs.module.eptsreports.reporting.library.dimensions.EptsCommonDimension;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportConstants;
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

  private EptsGeneralIndicator eptsGeneralIndicator;

  private IntensiveMonitoringCohortQueries intensiveMonitoringCohortQueries;

  private final QualityImprovement2020CohortQueries qualityImprovement2020CohortQueries;

  @Autowired
  @Qualifier("commonAgeDimensionCohort")
  private AgeDimensionCohortInterface ageDimensionCohort;

  private EptsCommonDimension eptsCommonDimension;

  @Autowired
  public IntensiveMonitoringDataSet(
      EptsGeneralIndicator eptsGeneralIndicator,
      IntensiveMonitoringCohortQueries intensiveMonitoringCohortQueries,
      QualityImprovement2020CohortQueries qualityImprovement2020CohortQueries,
      EptsCommonDimension eptsCommonDimension) {
    this.eptsGeneralIndicator = eptsGeneralIndicator;
    this.intensiveMonitoringCohortQueries = intensiveMonitoringCohortQueries;
    this.qualityImprovement2020CohortQueries = qualityImprovement2020CohortQueries;
    this.eptsCommonDimension = eptsCommonDimension;
  }

  public DataSetDefinition constructIntensiveMonitoringDataSet() {

    CohortIndicatorDataSetDefinition dataSetDefinition = new CohortIndicatorDataSetDefinition();
    dataSetDefinition.setName("Intensive Monitoring DataSet");
    dataSetDefinition.addParameters(getParameters());

    /**
     * ******** DIMENSIONS will be added here based on individual indicators required
     * *****************************
     */
    dataSetDefinition.addDimension(
        "miAge",
        EptsReportUtils.map(
            eptsCommonDimension.getPatientAgeBasedOnFirstViralLoadDate(),
            "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},location=${location}"));

    dataSetDefinition.addDimension(
        "miAge11",
        EptsReportUtils.map(
            eptsCommonDimension.getPatientAgeBasedOnFirstViralLoadDate(),
            "startDate=${revisionEndDate-4m+1d},endDate=${revisionEndDate-3m},location=${location}"));

    dataSetDefinition.addDimension(
        "age",
        EptsReportUtils.map(
            eptsCommonDimension.age(ageDimensionCohort), "effectiveDate=${revisionEndDate-4m}"));

    dataSetDefinition.addDimension(
        "ageByEndDateRevision",
        EptsReportUtils.map(
            eptsCommonDimension.age(ageDimensionCohort), "effectiveDate=${revisionEndDate}"));
    dataSetDefinition.addDimension(
        "ageByEvaluationEndDate",
        EptsReportUtils.map(
            eptsCommonDimension.age(ageDimensionCohort), "effectiveDate=${revisionEndDate-1m}"));

    // dimensions to be added here
    dataSetDefinition.addDimension(
        "ageBasedOnArt135",
        EptsReportUtils.map(
            eptsCommonDimension.ageBasedOnArtStartDateMOH(),
            "onOrAfter=${revisionEndDate-2m+1d},onOrBefore=${revisionEndDate},location=${location}"));
    dataSetDefinition.addDimension(
        "ageBasedOnArt246",
        EptsReportUtils.map(
            eptsCommonDimension.ageBasedOnArtStartDateMOH(),
            "onOrAfter=${revisionEndDate-8m+1d},onOrBefore=${revisionEndDate},location=${location}"));
    dataSetDefinition.addDimension(
        "ageBasedOnArt54",
        EptsReportUtils.map(
            eptsCommonDimension.ageBasedOnArtStartDateMOH(),
            "onOrAfter=${revisionEndDate-5m+1d},onOrBefore=${revisionEndDate-4m},location=${location}"));
    dataSetDefinition.addDimension(
        "ageBasedOnArt43",
        EptsReportUtils.map(
            eptsCommonDimension.ageBasedOnArtStartDateMOH(),
            "onOrAfter=${revisionEndDate-4m+1d},onOrBefore=${revisionEndDate-3m},location=${location}"));
    dataSetDefinition.addDimension(
        "ageBasedOnArtCat18",
        EptsReportUtils.map(
            eptsCommonDimension.ageBasedOnArtStartDateMOH(),
            "onOrAfter=${revisionEndDate-14m+1d},onOrBefore=${revisionEndDate-13m},location=${location}"));

    dataSetDefinition.addDimension(
        "ageInMonths54",
        EptsReportUtils.map(
            eptsCommonDimension.ageInMonths(), "effectiveDate=${revisionEndDate-4m}"));
    dataSetDefinition.addDimension(
        "ageBasedOnArt32",
        EptsReportUtils.map(
            eptsCommonDimension.ageBasedOnArtStartDateMOH(),
            "onOrAfter=${revisionEndDate-3m+1d},onOrBefore=${revisionEndDate-2m},location=${location}"));
    dataSetDefinition.addDimension(
        "ageBasedOnArt73",
        EptsReportUtils.map(
            eptsCommonDimension.ageBasedOnArtStartDateMOH(),
            "onOrAfter=${revisionEndDate-2m+1d},onOrBefore=${revisionEndDate-1m},location=${location}"));
    /**
     * *********************************** CATEGORY 7 ********************* //*********************
     * Denominator CAT7 **************
     */
    dataSetDefinition.addColumn(
        "MI7DEN1",
        "% de adultos HIV+ em TARV elegíveis ao TPT e que iniciaram TPT",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7DenMI2021Part135Definition(1),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt135=adultsArt");
    // 7.2
    dataSetDefinition.addColumn(
        "MI7DEN2",
        "% de adultos HIV+ em TARV elegiveis ao TPT que iniciaram e  completaram TPT",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7DenMI2021Part246Definition(2),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt246=adultsArt");
    // 7.3
    dataSetDefinition.addColumn(
        "MI7DEN3",
        "% de crianças HIV+ em TARV elegiveis ao TPT  e que iniciaram TPT",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7DenMI2021Part135Definition(3),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt73=1-14");
    // 7.4
    dataSetDefinition.addColumn(
        "MI7DEN4",
        "% de crianças HIV+ em TARV elegíveis que iniciaram e completaram TPT",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7DenMI2021Part246Definition(4),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt246=1-14");
    // 7.5
    dataSetDefinition.addColumn(
        "MI7DEN5",
        "% de mulheres grávidas HIV+ elegíveis ao TPI e que iniciaram TPI",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7DenMI2021Part135Definition(5),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // 7.6
    dataSetDefinition.addColumn(
        "MI7DEN6",
        "% de MG HIV+ em TARV que iniciou TPI e que terminou TPI",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7DenMI2021Part246Definition(6),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // ********************* NUMERATOR CAT7 **************
    // 7.1
    dataSetDefinition.addColumn(
        "MI7NUM1",
        "% de adultos HIV+ em TARV elegíveis ao TPT e que iniciaram TPT",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7NumMI2021Part135Definition(1),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt135=adultsArt");
    // 7.2
    dataSetDefinition.addColumn(
        "MI7NUM2",
        "% de adultos HIV+ em TARV elegiveis ao TPT que iniciaram e  completaram TPT",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7NumMI2021Part246Definition(2),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt246=adultsArt");
    // 7.3
    dataSetDefinition.addColumn(
        "MI7NUM3",
        "% de crianças HIV+ em TARV elegiveis ao TPT  e que iniciaram TPT",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7NumMI2021Part135Definition(3),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt73=1-14");
    // 7.4
    dataSetDefinition.addColumn(
        "MI7NUM4",
        "% de crianças HIV+ em TARV elegíveis que iniciaram e completaram TPT",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7NumMI2021Part246Definition(4),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt246=1-14");
    // 7.5
    dataSetDefinition.addColumn(
        "MI7NUM5",
        "% de mulheres grávidas HIV+ elegíveis ao TPI e que iniciaram TPI",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7NumMI2021Part135Definition(5),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    // 7.6
    dataSetDefinition.addColumn(
        "MI7NUM6",
        "% de MG HIV+ em TARV que iniciou TPI e que terminou TPI",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7NumMI2021Part246Definition(6),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // MI indicators category 9 denominators

    dataSetDefinition.addColumn(
        "MI9DEN1",
        "9.1 % de adultos (15/+anos) com pedido de CD4 na primeira consulta clínica depois do diagnóstico de HIV+",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMI9Den(1),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI9DEN2",
        "9.2 % de adultos  (15/+anos) que receberam o resultado do primeiro CD4 dentro de 33 dias  após a primeira consulta clínica.",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMI9Den(2),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI9DEN3",
        "9.3 % de adultos (15/+anos) com pedido de CD4 na consulta clínica de reinício do TARV",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMI9Den(3),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI9DEN4",
        "9.4 % de adultos (15/+anos) que receberam o resultado do CD4 dentro de 33 dias após consulta clínica de reinício do TARV",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMI9Den(4),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI9DEN5",
        "9.5 % de crianças  (0-14 anos) com pedido de CD4 na primeira consulta clínica depois do diagnóstico de HIV+",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMI9Den(5),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI9DEN6",
        "9.6 % de crianças  (0-14 anos) HIV+ que receberam o resultado do primeiro CD4 dentro de 33 dias  após a primeira consulta clínica",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMI9Den(6),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI9DEN7",
        "9.7 % de crianças (0-14 anos) com pedido de CD4 na consulta clínica de reinício do TARV",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMI9Den(7),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI9DEN8",
        "9.8 % de crianças (0-14 anos) que receberam o resultado do CD4 dentro de 33 dias após consulta clínica de reinício do TARV",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMI9Den(8),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI9DEN9",
        "9.9 % de MG  HIV+ com registo de pedido de CD4 na primeira CPN.",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCd4RequestAndResultForPregnantsCat9Den(9),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI9DEN10",
        "9.10 % de MG  HIV+ que receberam o resultado do primeiro CD4 dentro de 33 dias  após a primeira CPN.",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCd4RequestAndResultForPregnantsCat9Den(10),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // MI indicators category 9 numerator
    dataSetDefinition.addColumn(
        "MI9NUM1",
        "9.1 % de adultos  (15/+anos) com pedido de CD4 na primeira consulta clínica depois do diagnóstico de HIV+",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMI9Num(1),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI9NUM2",
        "9.2 % de adultos  (15/+anos) HIV+ que receberam o resultado do primeiro CD4 dentro de 33 dias  após a primeira consulta clínica",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMI9Num(2),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI9NUM3",
        "9.3 % de adultos  (15/+anos) com pedido de CD4 na consulta clínica de reinício do TARV",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMI9Num(3),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI9NUM4",
        "9.4 % de adultos (15/+anos) que receberam o resultado do CD4 dentro de 33 dias após consulta clínica de reinício do TARV",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMI9Num(4),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI9NUM5",
        "9.5 % de crianças  (0-14 anos) com pedido de CD4 na primeira consulta clínica depois do diagnóstico de HIV+",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMI9Num(5),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI9NUM6",
        "9.6 % de crianças  (0-14 anos) HIV+ que receberam o resultado do primeiro CD4 dentro de 33 dias  após a primeira consulta clínica",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMI9Num(6),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI9NUM7",
        "9.7 % de adultos HIV+ ≥ 15 anos que reiniciaram TARV durante o período de revisão e tiveram registo de pedido do CD4 na consulta de reinício",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMI9Num(7),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI9NUM8",
        "9.8 % de adultos HIV+ ≥ 15 anos reinícios TARV que teve conhecimento do resultado do CD4 dentro de 33 dias após a data da consulta clínica de reinício TARV",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMI9Num(8),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI9NUM9",
        "9.9 % de MG  HIV+ com registo de pedido de CD4 na primeira CPN.",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCd4RequestAndResultForPregnantsCat9Num(9),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI9NUM10",
        "9.10 % de MG  HIV+ que receberam o resultado do primeiro CD4 dentro de 33 dias  após a primeira CPN",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCd4RequestAndResultForPregnantsCat9Num(10),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    /**
     * *********************************** CATEGORY 11 ********************* //*********************
     * Denominator **************
     */
    // 11.1
    dataSetDefinition.addColumn(
        "MI11DEN1",
        "MI DEN 11.1",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMIC11DEN(1),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt54=adultsArt");
    // 11.2
    dataSetDefinition.addColumn(
        "MI11DEN2",
        "MI DEN 11.2",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMIC11DEN(2),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "miAge11=MqAdults");
    // 11.3
    dataSetDefinition.addColumn(
        "MI11DEN3",
        "MI DEN 11.3",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMIC11DEN(3),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    // 11.4
    dataSetDefinition.addColumn(
        "MI11DEN4",
        "MI DEN 11.4",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMIC11DEN(4),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    // 11.5
    dataSetDefinition.addColumn(
        "MI11DEN5",
        "MI DEN 11.5",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMIC11DEN(5),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt54=2-14"); // porque isto inclui intervalos
    // 11.6
    dataSetDefinition.addColumn(
        "MI11DEN6",
        "MI DEN 11.6",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMIC11DEN(6),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt54=<2");
    // 11.7
    dataSetDefinition.addColumn(
        "MI11DEN7",
        "MI DEN 11.7",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMIC11DEN(7),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "miAge11=MqChildren");

    // ********************* Numerator **************
    // 11.1
    dataSetDefinition.addColumn(
        "MI11NUM1",
        "MI NUM 11.1",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMIC11NUM(1),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt54=adultsArt");
    // 11.2
    dataSetDefinition.addColumn(
        "MI11NUM2",
        "MI NUM 11.2",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMIC11NUM(2),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "miAge11=MqAdults");
    // 11.3
    dataSetDefinition.addColumn(
        "MI11NUM3",
        "MI NUM 11.3",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMIC11NUM(3),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    // 11.4
    dataSetDefinition.addColumn(
        "MI11NUM4",
        "MI NUM 11.4",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMIC11NUM(4),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    // 11.5
    dataSetDefinition.addColumn(
        "MI11NUM5",
        "MI NUM 11.5",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMIC11NUM(5),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt54=2-14");
    // 11.6
    dataSetDefinition.addColumn(
        "MI11NUM6",
        "MI NUM 11.6",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMIC11NUM(6),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt54=<2");
    // 11.7
    dataSetDefinition.addColumn(
        "MI11NUM7",
        "MI NUM 11.7",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMIC11NUM(7),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "miAge11=MqChildren");

    /**
     * *********************************** CATEGORY 12 ******************************************
     * //* Part 1 Denominator **************
     */
    // 12.1
    dataSetDefinition.addColumn(
        "MI12P1DEN1",
        "MI DEN 12.1",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat12P1DenNum(1, false),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt32=adultsArt");
    // 12.2
    dataSetDefinition.addColumn(
        "MI12P1DEN2",
        "MI DEN 12.2",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat12P1DenNum(2, false),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt54=adultsArt");
    // 12.5
    dataSetDefinition.addColumn(
        "MI12P1DEN5",
        "MI DEN 12.5",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat12P1DenNum(5, false),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt32=childrenArt");
    // 12.6
    dataSetDefinition.addColumn(
        "MI12P1DEN6",
        "MI DEN 12.6",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat12P1DenNum(6, false),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt54=childrenArt");
    // 12.9
    dataSetDefinition.addColumn(
        "MI12P1DEN9",
        "MI DEN 12.9",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat12P1DenNum(9, false),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    // 12.10
    dataSetDefinition.addColumn(
        "MI12P1DEN10",
        "MI DEN 12.10",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat12P1DenNum(10, false),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // ******* */ Part 1 Numerator **************
    // 12.1
    dataSetDefinition.addColumn(
        "MI12P1NUM1",
        "MI NUM 12.1",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat12P1DenNum(1, true),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt32=adultsArt");
    // 12.2
    dataSetDefinition.addColumn(
        "MI12P1NUM2",
        "MI NUM 12.2",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat12P1DenNum(2, true),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt54=adultsArt");
    // 12.5
    dataSetDefinition.addColumn(
        "MI12P1NUM5",
        "MI NUM 12.5",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat12P1DenNum(5, true),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt32=childrenArt");
    // 12.6
    dataSetDefinition.addColumn(
        "MI12P1NUM6",
        "MI NUM 12.6",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat12P1DenNum(6, true),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt54=childrenArt");
    // 12.9
    dataSetDefinition.addColumn(
        "MI12P1NUM9",
        "MI NUM 12.9",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat12P1DenNum(9, true),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    // 12.10
    dataSetDefinition.addColumn(
        "MI12P1NUM10",
        "MI NUM 12.10",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat12P1DenNum(10, true),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    /**
     * *********************************** CATEGORY 13 ********************* //*********************
     * PART 1 **************
     */

    // CAT 13 P2 DENOMINATOR
    // 13.15
    CohortIndicator MI13DEN15 =
        eptsGeneralIndicator.getIndicator(
            "MI13DEN15",
            EptsReportUtils.map(
                intensiveMonitoringCohortQueries.getMI13Den15MGInIncluisionPeriod(),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13DEN15.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13DEN15",
        "% de MG elegíveis a CV com registo de pedido de CV feito pelo clínico (MG que iniciaram TARV na CPN) Denominator: # de MG com registo de início do TARV na CPN dentro do período de inclusão. (Line 90,Column F in the Template) as following",
        EptsReportUtils.map(
            MI13DEN15,
            "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"),
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
    CohortIndicator MI13NUM15 =
        eptsGeneralIndicator.getIndicator(
            "MI13NUM15",
            EptsReportUtils.map(
                intensiveMonitoringCohortQueries.getMI13Num15(),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13NUM15.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13NUM15",
        "% de MG elegíveis a CV com registo de pedido de CV feito pelo clínico (MG que iniciaram TARV na CPN) Denominator: # de MG com registo de início do TARV na CPN dentro do período de inclusão. (Line 90,Column F in the Template) as following",
        EptsReportUtils.map(
            MI13NUM15,
            "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"),
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

    // Category 13 Part-1

    // 13.1

    CohortIndicator MI13DEN1 =
        eptsGeneralIndicator.getIndicator(
            "MI13DEN1",
            EptsReportUtils.map(
                intensiveMonitoringCohortQueries.getSumOfPatientsIn1stOr2ndLineOfArt(true),
                "revisionEndDate=${revisionEndDate},location=${location}"));

    MI13DEN1.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13DEN1",
        "# de adultos (15/+anos) na 1ª ou 2ª linha de TARV que tiveram consulta clínica no período de avaliação e que eram elegíveis ao pedido de CV",
        EptsReportUtils.map(MI13DEN1, "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI13DOT1DEN",
        "% de adultos (15/+anos) na 1a linha de TARV que tiveram consulta clínica no período de revisão, eram elegíveis ao pedido de CV e com registo de pedido de CV feito pelo clínico",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat13Den(1, false),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    // 13.6
    dataSetDefinition.addColumn(
        "MI13DEN6",
        "% de crianças (0-4 anos de idade) na 1a linha de TARV que tiveram consulta clínica no período de revisão, eram elegíveis ao pedido de CV e com registo de pedido de CV feito pelo clínico.",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat13Den(6, false),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    // 13.7
    dataSetDefinition.addColumn(
        "MI13DEN7",
        "% de crianças (5-9 anos de idade) na 1a linha de TARV que tiveram consulta clínica no período de revisão, eram elegíveis ao pedido de CV e com registo de pedido de CV feito pelo clínico.",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat13Den(7, false),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    // 13.8
    CohortIndicator MI13DEN8 =
        eptsGeneralIndicator.getIndicator(
            "MI13DEN8",
            EptsReportUtils.map(
                intensiveMonitoringCohortQueries.getSumOfPatientsIn1stOr2ndLineOfArtForDenNum8(
                    true),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13DEN8.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13DEN8",
        "# de crianças na 1a linha (10-14 anos de idade) ou 2ª linha (0-14 anos) de TARV que tiveram consulta clínica no período de revisão e que eram elegíveis ao pedido de CV",
        EptsReportUtils.map(
            MI13DEN8,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI13DEN8PrimeiraLinha",
        "% de crianças (10-14 anos de idade) na 1a linha de TARV que tiveram consulta clínica no período de revisão, eram elegíveis ao pedido de CV e com registo de pedido de CV feito pelo clínico.",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat13Den(8, false),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // 13.1
    CohortIndicator MI13NUM1 =
        eptsGeneralIndicator.getIndicator(
            "MI13NUM1",
            EptsReportUtils.map(
                intensiveMonitoringCohortQueries.getSumOfPatientsIn1stOr2ndLineOfArt(false),
                "revisionEndDate=${revisionEndDate},location=${location}"));

    MI13NUM1.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13NUM1",
        "# de adultos (15/+anos) na 1a ou 2ª linha de TARV que tiveram consulta clínica no período de revisão, eram elegíveis ao pedido de CV e com registo de pedido de CV feito pelo clínico",
        EptsReportUtils.map(MI13NUM1, "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI13DOT1NUM",
        "% de adultos (15/+anos) na 1a linha de TARV que tiveram consulta clínica no período de revisão, eram elegíveis ao pedido de CV e com registo de pedido de CV feito pelo clínico",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat13Den(1, true),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    // 13.6
    dataSetDefinition.addColumn(
        "MI13NUM6",
        "% de crianças (0-4 anos de idade) na 1a linha de TARV que tiveram consulta clínica no período de revisão, eram elegíveis ao pedido de CV e com registo de pedido de CV feito pelo clínico.",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat13Den(6, true),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    // 13.7
    dataSetDefinition.addColumn(
        "MI13NUM7",
        "% de crianças (5-9 anos de idade) na 1a linha de TARV que tiveram consulta clínica no período de revisão, eram elegíveis ao pedido de CV e com registo de pedido de CV feito pelo clínico.",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat13Den(7, true),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    // 13.8
    CohortIndicator MI13NUM8 =
        eptsGeneralIndicator.getIndicator(
            "MI13NUM8",
            EptsReportUtils.map(
                intensiveMonitoringCohortQueries.getSumOfPatientsIn1stOr2ndLineOfArtForDenNum8(
                    false),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13NUM8.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13NUM8",
        "# de crianças na 1a linha (10-14 anos de idade) ou 2ª linha (0-14 anos de idade) de TARV que tiveram consulta clínica no período de revisão, "
            + "eram elegíveis ao pedido de CV e com registo de pedido de CV feito pelo clínico",
        EptsReportUtils.map(
            MI13NUM8,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI13NUM8PrimeiraLinha",
        "% de crianças (10-14 anos de idade) na 1a linha de TARV que tiveram consulta clínica no período de revisão, eram elegíveis ao pedido de CV e com registo de pedido de CV feito pelo clínico.",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat13Den(8, true),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // 13.2 DEN
    CohortIndicator MI13DEN2 =
        eptsGeneralIndicator.getIndicator(
            "MI13DEN2",
            EptsReportUtils.map(
                intensiveMonitoringCohortQueries.getMiSumOfPatientsIn1stOr2ndLineOfArtForDenNum2(
                    true),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13DEN2.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13DEN2",
        "# de adultos (15/+anos) na 1a ou 2ª linha de TARV ou mudança de regime de 1ª linha",
        EptsReportUtils.map(
            MI13DEN2,
            "startDate=${revisionEndDate-11m+1d},endDate=${revisionEndDate-10m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI13DEN2PrimeiraLinha",
        "Adultos (15/+anos) que iniciaram a 1a linha de TARV ou novo regime da 1ª linha há 9 meses atrás",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMI13DEN2(2),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // 13.2 NUM
    CohortIndicator MI13NUM2 =
        eptsGeneralIndicator.getIndicator(
            "MI13NUM2",
            EptsReportUtils.map(
                intensiveMonitoringCohortQueries.getMiSumOfPatientsIn1stOr2ndLineOfArtForDenNum2(
                    false),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13NUM2.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13NUM2",
        "# de adultos (15/+anos) na 1a ou 2ª linha de TARV ou mudança de regime de 1ª linha, que receberam o resultado da CV entre o sexto e o nono mês após início do TARV",
        EptsReportUtils.map(
            MI13NUM2,
            "startDate=${revisionEndDate-11m+1d},endDate=${revisionEndDate-10m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI13NUM2PrimeiraLinha",
        "Adultos (15/+anos) na 1a linha de TARV que receberam o resultado da CV entre o sexto e o nono mês após início do TARV",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMI13NUM2(2),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // 13.5 DEN
    CohortIndicator MI13DEN5 =
        eptsGeneralIndicator.getIndicator(
            "MI13DEN5",
            EptsReportUtils.map(
                intensiveMonitoringCohortQueries.getMI13NewDen5(),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13DEN5.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13DEN5",
        "Adultos (15/+anos) na 2a linha de TARV que receberam o resultado da CV entre o sexto e o nono mês após o início da 2a linha de TARV",
        EptsReportUtils.map(
            MI13DEN5,
            "startDate=${revisionEndDate-11m+1d},endDate=${revisionEndDate-10m},revisionEndDate=${revisionEndDate},location=${location}"),
        "ageByEndDateRevision=15+");

    dataSetDefinition.addColumn(
        "MI13DEN5SegundaLinha",
        "de adultos (15/+anos) na 2a linha de TARV que receberam o resultado da CV entre o sexto e o nono mês após o início da 2a linha de TARV",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMI13DEN5(5),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // 13.5 NUM
    CohortIndicator MI13NUM5 =
        eptsGeneralIndicator.getIndicator(
            "MI13NUM5",
            EptsReportUtils.map(
                intensiveMonitoringCohortQueries.getMI13NewNum5(true),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13NUM5.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13NUM5",
        "% de adultos (15/+anos) coinfectados TB/HIV com resultado de CV registado na FM",
        EptsReportUtils.map(
            MI13NUM5,
            "startDate=${revisionEndDate-11m+1d},endDate=${revisionEndDate-10m},revisionEndDate=${revisionEndDate},location=${location}"),
        "ageByEndDateRevision=15+");

    dataSetDefinition.addColumn(
        "MI13NUM5SegundaLinha",
        "Adultos (15/+anos) na 2a linha de TARV que receberam o resultado da CV entre o sexto e o nono mês após o início da 2a linha de TARV",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMI13NUM5(5),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // 13.9 DEN
    dataSetDefinition.addColumn(
        "MI13DEN9",
        "Crianças (0-4 anos de idade) com registo de início da 1a linha de TARV há 9 meses",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMI13DEN9(9),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    // 13.9 NUM
    dataSetDefinition.addColumn(
        "MI13NUM9",
        "Crianças  (0-4 anos de idade) na 1a linha de TARV que receberam o resultado da Carga Viral entre o sexto e o nono mês após o início do TARV",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMI13NUM9(9),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    // 13.10 DEN
    dataSetDefinition.addColumn(
        "MI13DEN10",
        "Crianças  (5-9 anos de idade) com registo de início da 1a linha de TARV ou novo regime de TARV há 9 meses",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMI13DEN10(10),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    // 13.10 NUM
    dataSetDefinition.addColumn(
        "MI13NUM10",
        "Crianças  (5-9 anos de idade) na 1a linha de TARV que receberam o resultado da Carga Viral entre o sexto e o nono mês após o início do TARV",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMI13NUM10(10),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    // 13.11 DEN
    CohortIndicator MI13DEN11 =
        eptsGeneralIndicator.getIndicator(
            "MI13DEN11",
            EptsReportUtils.map(
                intensiveMonitoringCohortQueries.getMiSumOfPatientsIn1stOr2ndLineOfArtForDenNum11(
                    true),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13DEN11.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13DEN11",
        "# de crianças na 1a linha de TARV ou mudança de regime de 1ª linha (10-14 anos de idade) ou 2ª Linha TARV (0-14 anos de idade) que receberam o resultado da CV entre o sexto e o nono mês após início do TARV",
        EptsReportUtils.map(
            MI13DEN11,
            "startDate=${revisionEndDate-11m+1d},endDate=${revisionEndDate-10m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI13DEN11PrimeiraLinha",
        "Crianças  (10-14 anos de idade) com registo de início da 1a linha de TARV ou novo regime da 1ª linha de TARV no mês de avaliação",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMI13DEN11(11),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // 13.11 NUM
    CohortIndicator MI13NUM11 =
        eptsGeneralIndicator.getIndicator(
            "MI13NUM11",
            EptsReportUtils.map(
                intensiveMonitoringCohortQueries.getMiSumOfPatientsIn1stOr2ndLineOfArtForDenNum11(
                    false),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13NUM11.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13NUM11",
        "# de crianças (10-14 anos de idade) na 1a linha de TARV ou mudança de regime de 1ª linha”, que receberam o resultado da CV entre o sexto e o nono mês após início do TARV",
        EptsReportUtils.map(
            MI13NUM11,
            "startDate=${revisionEndDate-11m+1d},endDate=${revisionEndDate-10m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI13NUM11PrimeiraLinha",
        "Crianças (10-14 anos de idade) na 1a linha de TARV que receberam o resultado da Carga Viral entre o sexto e o nono mês após o início do TARV",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMI13NUM11(11),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // 13.14 DEN
    CohortIndicator MI13DEN14 =
        eptsGeneralIndicator.getIndicator(
            "MI13DEN14",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ13NewDen14(),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13DEN14.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13DEN14",
        "% de crianças (0-14 anos) coinfectados TB/HIV com resultado de CV registado na FM",
        EptsReportUtils.map(
            MI13DEN14,
            "startDate=${revisionEndDate-11m+1d},endDate=${revisionEndDate-10m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI13DEN14SegundaLinha",
        "Crianças com registo de início da 2a linha de TARV no mês de avaliação",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMI13DEN14(14),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // 13.14 NUM
    CohortIndicator MI13NUM14 =
        eptsGeneralIndicator.getIndicator(
            "MI13NUM14",
            EptsReportUtils.map(
                intensiveMonitoringCohortQueries.getMI13NewNum5(false),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13NUM14.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13NUM14",
        "Crianças na 2a linha de TARV que receberam o resultado da Carga Viral entre o sexto e o nono mês após o início da 2a linha de TARV",
        EptsReportUtils.map(
            MI13NUM14,
            "startDate=${revisionEndDate-11m+1d},endDate=${revisionEndDate-10m},revisionEndDate=${revisionEndDate},location=${location}"),
        "ageByEndDateRevision=0-14");

    dataSetDefinition.addColumn(
        "MI13NUM14SegundaLinha",
        "Crianças na 2a linha de TARV que receberam o resultado da CV entre o sexto e o nono mês após início da 2a linha de TARV no mês de avaliação",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMI13NUM14(14),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // ********************* PART 2 **************

    // 13.4
    CohortIndicator MI13DEN4 =
        eptsGeneralIndicator.getIndicator(
            "MI13DEN4",
            EptsReportUtils.map(
                intensiveMonitoringCohortQueries.getMI13NewDen4(),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13DEN4.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13DEN4",
        "% de adultos (15/+anos) coinfectados TB/HIV com consulta clínica no período de revisão, elegíveis ao pedido de CV e com registo de pedido de CV",
        EptsReportUtils.map(
            MI13DEN4,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI13DEN4SegundaLinha",
        "# de adultos (15/+ anos) na 2a linha de TARV elegíveis a CV.",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat13Den(4, false),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // 13.13
    CohortIndicator MI13DEN13 =
        eptsGeneralIndicator.getIndicator(
            "MI13DEN13",
            EptsReportUtils.map(
                intensiveMonitoringCohortQueries.getMI13NewDen13(),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13DEN13.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13DEN13",
        "% de crianças (0-14 anos) coinfectados TB/HIV com consulta clínica no período de revisão, elegíveis ao pedido de CV e com registo de pedido de CV",
        EptsReportUtils.map(
            MI13DEN13,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI13DEN13SegundaLinha",
        "# de crianças (>2anos) na 2a linha de TARV elegíveis ao pedido de CV.",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat13Den(13, false),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // 13.4
    CohortIndicator MI13NUM4 =
        eptsGeneralIndicator.getIndicator(
            "MI13NUM4",
            EptsReportUtils.map(
                intensiveMonitoringCohortQueries.getMI13NewNum4(),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13NUM4.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13NUM4",
        "% de adultos (15/+anos) coinfectados TB/HIV com consulta clínica no período de revisão, elegíveis ao pedido de CV e com registo de pedido de CV",
        EptsReportUtils.map(
            MI13NUM4,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI13NUM4SegundaLinha",
        "# de adultos (15/+ anos) na 2a linha de TARV elegíveis a CV.",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat13Den(4, true),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // 13.13
    CohortIndicator MI13NUM13 =
        eptsGeneralIndicator.getIndicator(
            "MI13NUM13",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ13NewNum4(false),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13NUM13.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13NUM13",
        "% de crianças (0-14 anos) coinfectados TB/HIV com consulta clínica no período de revisão, elegíveis ao pedido de CV e com registo de pedido de CV",
        EptsReportUtils.map(
            MI13NUM13,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    dataSetDefinition.addColumn(
        "MI13NUM13SegundaLinha",
        "# de crianças (>2anos) na 2a linha de TARV elegíveis ao pedido de CV.",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat13Den(13, true),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // 13.3 P4 Den
    dataSetDefinition.addColumn(
        "MI13DEN3",
        "# de Adultos (15/+anos) na 1ª linha de TARV com registo resultado de CV acima de 1000",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMICat13Part4(3, false),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageByEndDateRevision=15+");

    // 13.12 P4 Den
    dataSetDefinition.addColumn(
        "MI13DEN12",
        "# de crianças (>=2 anos de idade) na 1ª linha de TARV com registo de resultado de CV >= 1000",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMICat13Part4(12, false),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // 13.18 P4 Den
    dataSetDefinition.addColumn(
        "MI13DEN18",
        "# de MG na 1a linha de TARV que receberam um resultado de CV acima de 1000 cópias no período de inclusão",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMICat13Part4(18, false),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // 13.3 P4 Num
    dataSetDefinition.addColumn(
        "MI13NUM3",
        "# de Adultos (15/+anos) na 1ª linha de TARV com registo de pedido de CV entre o 3º e o 4º mês após terem recebido o último resultado de CV >= 1000 cps/ml ",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMICat13Part4(3, true),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // 13.12 P4 Num
    dataSetDefinition.addColumn(
        "MI13NUM12",
        "# de crianças (>=2 anos de idade) na 1ª linha de TARV com registo de pedido de CV entre o 3º e o 4º mês após terem recebido o último resultado de CV >= 1000",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMICat13Part4(12, true),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // 13.18 P4 Num
    dataSetDefinition.addColumn(
        "MI13NUM18",
        "% de MG na 1ª linha de TARV com registo de pedido de CV entre o 3º e o 4º mês após terem recebido o último resultado de CV acima de 1000 cópia e terem 3 sessões consecutivas de APSS/PP",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMICat13Part4(18, true),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    /**
     * *********************************** MI CATEGORY 14 *********************
     * //********************* MI CAT14 Inherited from the MQCAT14 with adjusted date parameters
     * **************
     */
    // MI CAT 14 Denominator
    // CAT 14 DEN 1
    dataSetDefinition.addColumn(
        "MI14DEN1",
        "14.1. % de utentes (<1 ano) em TARV com supressão viral (CV<1000 Cps/ml)",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMICAT14(
                    QualityImprovement2020CohortQueries.MQCat14Preposition.A_NOT_A1A2, "DEN"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageByEndDateRevision=<1");
    // CAT 14 DEN 2
    dataSetDefinition.addColumn(
        "MI14DEN2",
        "14.2.% de utentes (1- 4 anos) em TARV com supressão viral (CV<1000 Cps/ml)",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMICAT14(
                    QualityImprovement2020CohortQueries.MQCat14Preposition.A_NOT_A1A2, "DEN"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageByEndDateRevision=1-4");
    // CAT 14 DEN 3
    dataSetDefinition.addColumn(
        "MI14DEN3",
        "14.3.% de utentes (5 - 9 anos) em TARV com supressão viral (CV<1000 Cps/ml)",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMICAT14(
                    QualityImprovement2020CohortQueries.MQCat14Preposition.A_NOT_A1A2, "DEN"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageByEndDateRevision=5-9");
    // CAT 14 DEN 4
    dataSetDefinition.addColumn(
        "MI14DEN4",
        "14.4. % de utentes (10 - 14 anos) em TARV com supressão viral (CV<1000 Cps/ml)",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMICAT14(
                    QualityImprovement2020CohortQueries.MQCat14Preposition.A_NOT_A1A2, "DEN"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageByEndDateRevision=10-14");
    // CAT 14 DEN 5
    dataSetDefinition.addColumn(
        "MI14DEN5",
        "14.5. % de utentes (15 -19 anos) em TARV com supressão viral (CV<1000 Cps/ml)",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMICAT14(
                    QualityImprovement2020CohortQueries.MQCat14Preposition.A_NOT_A1A2, "DEN"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageByEndDateRevision=15-19");
    // CAT 14 DEN 6
    dataSetDefinition.addColumn(
        "MI14DEN6",
        "14.6. % de utentes (20+ anos) em TARV com supressão viral (CV<1000 Cps/ml",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMICAT14(
                    QualityImprovement2020CohortQueries.MQCat14Preposition.A_NOT_A1A2, "DEN"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageByEndDateRevision=20+");
    // CAT 14 DEN 7
    dataSetDefinition.addColumn(
        "MI14DEN7",
        "14.7. % de MG em TARV com supressão viral (CV<1000 Cps/ml)",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMICAT14(
                    QualityImprovement2020CohortQueries.MQCat14Preposition.A_AND_A1, "DEN"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    // CAT 14 DEN 8
    dataSetDefinition.addColumn(
        "MI14DEN8",
        "14.8. % de ML em TARV com supressão viral (CV<1000 Cps/ml)",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMICAT14(
                    QualityImprovement2020CohortQueries.MQCat14Preposition.A_AND_A2, "DEN"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // MI CAT 14 Numerator
    // CAT 14 NUM 1
    dataSetDefinition.addColumn(
        "MI14NUM1",
        "14.1. % de utentes (<1 ano) em TARV com supressão viral (CV<1000 Cps/ml)",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMICAT14(
                    QualityImprovement2020CohortQueries.MQCat14Preposition.B_NOT_B1B2, "NUM"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageByEndDateRevision=<1");
    // CAT 14 NUM 2
    dataSetDefinition.addColumn(
        "MI14NUM2",
        "14.2.% de utentes (1- 4 anos) em TARV com supressão viral (CV<1000 Cps/ml)",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMICAT14(
                    QualityImprovement2020CohortQueries.MQCat14Preposition.B_NOT_B1B2, "NUM"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageByEndDateRevision=1-4");
    // CAT 14 NUM 3
    dataSetDefinition.addColumn(
        "MI14NUM3",
        "14.3.% de utentes (5 - 9 anos) em TARV com supressão viral (CV<1000 Cps/ml)",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMICAT14(
                    QualityImprovement2020CohortQueries.MQCat14Preposition.B_NOT_B1B2, "NUM"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageByEndDateRevision=5-9");

    // CAT 14 NUM 4
    dataSetDefinition.addColumn(
        "MI14NUM4",
        "14.4. % de utentes (10 - 14 anos) em TARV com supressão viral (CV<1000 Cps/ml)",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMICAT14(
                    QualityImprovement2020CohortQueries.MQCat14Preposition.B_NOT_B1B2, "NUM"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageByEndDateRevision=10-14");

    // CAT 14 NUM 5
    dataSetDefinition.addColumn(
        "MI14NUM5",
        "14.5. % de utentes (15 -19 anos) em TARV com supressão viral (CV<1000 Cps/ml)",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMICAT14(
                    QualityImprovement2020CohortQueries.MQCat14Preposition.B_NOT_B1B2, "NUM"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageByEndDateRevision=15-19");
    // CAT 14 NUM 6
    dataSetDefinition.addColumn(
        "MI14NUM6",
        "14.6. % de utentes (20+ anos) em TARV com supressão viral (CV<1000 Cps/ml",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMICAT14(
                    QualityImprovement2020CohortQueries.MQCat14Preposition.B_NOT_B1B2, "NUM"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageByEndDateRevision=20+");
    // CAT 14 NUM 7
    dataSetDefinition.addColumn(
        "MI14NUM7",
        "14.7. % de MG em TARV com supressão viral (CV<1000 Cps/ml)",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMICAT14(
                    QualityImprovement2020CohortQueries.MQCat14Preposition.B_AND_B1, "NUM"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    // CAT 14 NUM 8
    dataSetDefinition.addColumn(
        "MI14NUM8",
        "14.8. % de ML em TARV com supressão viral (CV<1000 Cps/ml)",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getMICAT14(
                    QualityImprovement2020CohortQueries.MQCat14Preposition.B_AND_B2, "NUM"),
                "revisionEndDate=${revisionEndDate},location=${location}"),
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // ************************* MI CATEGORY 15 MDS INDICATORS **********************

    CohortIndicator MI15DEN13 =
        eptsGeneralIndicator.getIndicator(
            "MI15DEN13",
            EptsReportUtils.map(
                intensiveMonitoringCohortQueries.getMI15Den13(),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI15DEN13.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI15DEN13",
        "15.13 - % de pacientes elegíveis a MDS, que foram inscritos em MDS",
        EptsReportUtils.map(
            MI15DEN13,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI15NUM13 =
        eptsGeneralIndicator.getIndicator(
            "MI15NUM13",
            EptsReportUtils.map(
                intensiveMonitoringCohortQueries.getMI15Nume13(),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI15NUM13.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI15NUM13",
        "Numerator:  “# de pacientes elegíveis a MDS ",
        EptsReportUtils.map(
            MI15NUM13,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},revisionEndDate=${revisionEndDate},location=${location}"),
        "ageByEvaluationEndDate=2+");

    CohortIndicator MI15DEN14 =
        eptsGeneralIndicator.getIndicator(
            "MI15DEN14",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ15MdsDen14(),
                "startDate=${startDate},revisionEndDate=${endDate},location=${location}"));

    MI15DEN14.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI15DEN14",
        "15.14 - % de inscritos em MDS que receberam CV acima de 1000 cópias  ",
        EptsReportUtils.map(
            MI15DEN14,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"),
        "ageByEvaluationEndDate=2+");

    CohortIndicator MI15NUM14 =
        eptsGeneralIndicator.getIndicator(
            "MI15NUM14",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMI15MdsNum14(),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI15NUM14.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI15NUM14",
        "Numerator: # de pacientes inscritos em MDS para pacientes estáveis ",
        EptsReportUtils.map(
            MI15NUM14,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},revisionEndDate=${revisionEndDate},location=${location}"),
        "ageByEvaluationEndDate=2+");

    CohortIndicator MI15DEN15 =
        eptsGeneralIndicator.getIndicator(
            "MI15DEN15",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMI15Den15(),
                "startDate=${startDate},revisionEndDate=${endDate},location=${location}"));

    MI15DEN15.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI15DEN15",
        "Numerator 15.15 : # de pacientes inscritos em MDS para pacientes estáveis - 21 meses",
        EptsReportUtils.map(
            MI15DEN15,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"),
        "ageByEvaluationEndDate=2+");

    CohortIndicator MI15NUM15 =
        eptsGeneralIndicator.getIndicator(
            "MI15NUM15",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMI15Num15(),
                "startDate=${startDate},revisionEndDate=${endDate},location=${location}"));

    MI15NUM15.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI15NUM15",
        "Numerator: # de pacientes inscritos em MDS para pacientes estáveis ",
        EptsReportUtils.map(
            MI15NUM15,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"),
        "ageByEvaluationEndDate=2+");

    CohortIndicator MI15DEN16 =
        eptsGeneralIndicator.getIndicator(
            "MI15DEN16",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQDen15Dot16(),
                "startDate=${startDate},endDate=${endDate},location=${location}"));
    MI15DEN16.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI15DEN16",
        "15.16. % de utentes inscritos em MDS (para pacientes estáveis) com supressão viral",
        EptsReportUtils.map(
            MI15DEN16,
            "startDate=${revisionEndDate-12m+1d},endDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI15NUM16 =
        eptsGeneralIndicator.getIndicator(
            "MI15NUM16",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQNum15Dot16(),
                "startDate=${startDate},endDate=${endDate},location=${location}"));
    MI15NUM16.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI15NUM16",
        "15.16. % de utentes inscritos em MDS (para pacientes estáveis) com supressão viral",
        EptsReportUtils.map(
            MI15NUM16,
            "startDate=${revisionEndDate-12m+1d},endDate=${revisionEndDate},location=${location}"),
        "");

    // Category 18 Denominator

    CohortIndicator MQ18DEN =
        eptsGeneralIndicator.getIndicator(
            "MI18DEN",
            EptsReportUtils.map(
                intensiveMonitoringCohortQueries.getPatientsOnMICat18Denominator(),
                "endDate=${revisionEndDate},location=${location}"));

    MQ18DEN.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI18DEN89",
        "Crianças dos 8 - 9 anos activos em TARV com RD Total  (T)",
        EptsReportUtils.map(MQ18DEN, "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArtCat18=8-9");

    dataSetDefinition.addColumn(
        "MI18DEN1014",
        "Crianças dos 10 - 14 anos activos em TARV com RD Total  (T)",
        EptsReportUtils.map(MQ18DEN, "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArtCat18=10-14");
    // MI Cat 18 Numerator
    CohortIndicator MI18NUM =
        eptsGeneralIndicator.getIndicator(
            "MI18NUM",
            EptsReportUtils.map(
                intensiveMonitoringCohortQueries.getPatientsOnMICat18Numerator(),
                "revisionEndDate=${revisionEndDate},location=${location}"));

    MI18NUM.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI18NUM89",
        "Crianças dos 8 - 9 anos activos em TARV com RD Total  (T) NUM",
        EptsReportUtils.map(MI18NUM, "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArtCat18=8-9");

    dataSetDefinition.addColumn(
        "MI18NUM1014",
        "Adolescentes de 10 - 14 anos activos em TARV com RD Total  (T) NUM",
        EptsReportUtils.map(MI18NUM, "revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArtCat18=10-14");

    // MI Category 19 DENOMINATOR 1
    CohortIndicator MI19DEN1 =
        eptsGeneralIndicator.getIndicator(
            "MI19DEN1",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ19A(1, EptsReportConstants.MIMQ.MI),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI19DEN1.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI19DEN1",
        "19.1 % de adultos (>=15 anos) presuntivos de TB com pedido de teste molecular (Xpert/Truenat) na data da 1ª consulta",
        EptsReportUtils.map(
            MI19DEN1,
            "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // MI Category 19 DENOMINATOR 2
    CohortIndicator MI19DEN2 =
        eptsGeneralIndicator.getIndicator(
            "MI19DEN2",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ19A(1, EptsReportConstants.MIMQ.MI),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI19DEN2.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI19DEN2",
        "19.2 % de adultos (>=15 anos) HIV+ presuntivos de TB que receberam resultado do teste molecular (Xpert/Truenat) dentro de 7 dias após o pedido",
        EptsReportUtils.map(
            MI19DEN2,
            "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // MI Category 19 DENOMINATOR 3
    CohortIndicator MI19DEN3 =
        eptsGeneralIndicator.getIndicator(
            "MI19DEN3",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ19A(3, EptsReportConstants.MIMQ.MI),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI19DEN3.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI19DEN3",
        "19.3 % de adultos (>=15 anos) HIV+ diagnosticados com TB e que iniciaram tratamento de TB na data do diagnóstico de TB",
        EptsReportUtils.map(
            MI19DEN3,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // MI Category 19 DENOMINATOR 4
    CohortIndicator MI19DEN4 =
        eptsGeneralIndicator.getIndicator(
            "MI19DEN4",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ19A(4, EptsReportConstants.MIMQ.MI),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI19DEN4.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI19DEN4",
        "19.4 % de crianças (0-14 anos) presuntivos de TB com pedido de teste molecular (Xpert/Truenat) na data da 1ª consulta.",
        EptsReportUtils.map(
            MI19DEN4,
            "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // MI Category 19 DENOMINATOR 5
    CohortIndicator MI19DEN5 =
        eptsGeneralIndicator.getIndicator(
            "MI19DEN5",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ19A(4, EptsReportConstants.MIMQ.MI),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI19DEN5.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI19DEN5",
        "19.5 % de crianças (0-14 anos) HIV+ presuntivos de TB que receberam resultado do teste molecular (Xpert/Truenat) dentro de 7 dias após o pedido",
        EptsReportUtils.map(
            MI19DEN5,
            "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // MI Category 19 DENOMINATOR 6
    CohortIndicator MI19DEN6 =
        eptsGeneralIndicator.getIndicator(
            "MI19DEN6",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ19A(6, EptsReportConstants.MIMQ.MI),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI19DEN6.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI19DEN6",
        "19.6 % de crianças (0 - 14 anos) HIV+ diagnosticados com TB e que iniciaram tratamento de TB na data do diagnóstico de TB",
        EptsReportUtils.map(
            MI19DEN6,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // MI Category 19 NUMERATOR 1
    CohortIndicator MI19NUM1 =
        eptsGeneralIndicator.getIndicator(
            "MI19NUM1",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ19B(1, EptsReportConstants.MIMQ.MI),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI19NUM1.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI19NUM1",
        "19.1 % de adultos (>=15 anos) presuntivos de TB com pedido de teste molecular (Xpert/Truenat) na data da 1ª consulta",
        EptsReportUtils.map(
            MI19NUM1,
            "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // MI Category 19 NUMERATOR 2
    CohortIndicator MI19NUM2 =
        eptsGeneralIndicator.getIndicator(
            "MI19NUM2",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ19B(2, EptsReportConstants.MIMQ.MI),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI19NUM2.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI19NUM2",
        "19.2 % de adultos (>=15 anos) HIV+ presuntivos de TB que receberam resultado do teste molecular (Xpert/Truenat) dentro de 7 dias após o pedido",
        EptsReportUtils.map(
            MI19NUM2,
            "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // MI Category 19 NUMERATOR 3
    CohortIndicator MI19NUM3 =
        eptsGeneralIndicator.getIndicator(
            "MI19NUM3",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ19B(3, EptsReportConstants.MIMQ.MI),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI19NUM3.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI19NUM3",
        "19.3 % de adultos (>=15 anos) HIV+ diagnosticados com TB e que iniciaram tratamento de TB na data do diagnóstico de TB",
        EptsReportUtils.map(
            MI19NUM3,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // MI Category 19 NUMERATOR 4
    CohortIndicator MI19NUM4 =
        eptsGeneralIndicator.getIndicator(
            "MI19NUM4",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ19B(4, EptsReportConstants.MIMQ.MI),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI19NUM4.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI19NUM4",
        "19.4 % de crianças (0-14 anos) presuntivos de TB com pedido de teste molecular (Xpert/Truenat) na data da 1ª consulta",
        EptsReportUtils.map(
            MI19NUM4,
            "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // MI Category 19 NUMERATOR 5
    CohortIndicator MI19NUM5 =
        eptsGeneralIndicator.getIndicator(
            "MI19NUM5",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ19B(5, EptsReportConstants.MIMQ.MI),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI19NUM5.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI19NUM5",
        "19.5 % de crianças (0-14 anos) HIV+ presuntivos de TB que receberam resultado do teste molecular (Xpert/Truenat) dentro de 7 dias após o pedido",
        EptsReportUtils.map(
            MI19NUM5,
            "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // MI Category 19 NUMERATOR 6
    CohortIndicator MI19NUM6 =
        eptsGeneralIndicator.getIndicator(
            "MI19NUM6",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ19B(6, EptsReportConstants.MIMQ.MI),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI19NUM6.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI19NUM6",
        "19.6 % de crianças (0 - 14 anos) HIV+ diagnosticados com TB e que iniciaram tratamento de TB na data do diagnóstico de TB",
        EptsReportUtils.map(
            MI19NUM6,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},revisionEndDate=${revisionEndDate},location=${location}"),
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
