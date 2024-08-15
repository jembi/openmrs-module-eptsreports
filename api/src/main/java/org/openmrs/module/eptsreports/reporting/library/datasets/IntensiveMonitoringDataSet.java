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
        "MI9DEN9",
        "9.9 % de MG  HIV+ com registo de pedido de CD4 na primeira CPN.",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCd4RequestAndResultForPregnantsCat9Den(5),
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
