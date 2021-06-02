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

  @Autowired private EptsCommonDimension eptsCommonDimension;

  @Autowired private EptsGeneralIndicator eptsGeneralIndicator;

  @Autowired private IntensiveMonitoringCohortQueries intensiveMonitoringCohortQueries;

  @Autowired private QualityImprovement2020CohortQueries qualityImprovement2020CohortQueries;

  @Autowired
  @Qualifier("commonAgeDimensionCohort")
  private AgeDimensionCohortInterface ageDimensionCohort;

  public DataSetDefinition constructIntensiveMonitoringDataSet() {

    CohortIndicatorDataSetDefinition dataSetDefinition = new CohortIndicatorDataSetDefinition();
    dataSetDefinition.setName("Intensive Monitoring DataSet");
    dataSetDefinition.addParameters(getParameters());

    /** ***************************** DIMENSIONS ***************************** */
    dataSetDefinition.addDimension(
        "age",
        EptsReportUtils.map(
            eptsCommonDimension.age(ageDimensionCohort), "effectiveDate=${endDate}"));

    dataSetDefinition.addDimension(
        "ageInMonths",
        EptsReportUtils.map(eptsCommonDimension.ageInMonths(), "effectiveDate=${endDate}"));

    dataSetDefinition.addDimension(
        "ageBasedOnArt",
        EptsReportUtils.map(
            eptsCommonDimension.ageBasedOnArtStartDateMOH(),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));

    dataSetDefinition.addDimension(
        "mqAge",
        EptsReportUtils.map(
            eptsCommonDimension.getPatientAgeBasedOnFirstViralLoadDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    /**
     * **************************************************** CATEGORY 7
     * ***************************************************
     */

    /** *****************************DENOMINATOR***************************** */
    // 7.1
    dataSetDefinition.addColumn(
        "MI7DEN1",
        "% de adultos HIV+ em TARV elegíveis ao TPT e que iniciaram TPT",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7MOHIV202171Definition("DEN"),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt=adultsArt");
    // 7.2
    dataSetDefinition.addColumn(
        "MI7DEN2",
        "% de adultos HIV+ em TARV elegiveis ao TPT que iniciaram e  completaram TPT",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7MOHIV202172Definition("DEN"),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt=adultsArt");
    // 7.3
    dataSetDefinition.addColumn(
        "MI7DEN3",
        "% de crianças HIV+ em TARV elegiveis ao TPT  e que iniciaram TPT",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7MOHIV202173Definition("DEN"),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt=childrenArt");
    // 7.4
    dataSetDefinition.addColumn(
        "MI7DEN4",
        "% de crianças HIV+ em TARV elegíveis que iniciaram e completaram TPT",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7MOHIV202174Definition("DEN"),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt=childrenArt");
    // 7.5
    dataSetDefinition.addColumn(
        "MI7DEN5",
        "% de mulheres grávidas HIV+ elegíveis ao TPI e que iniciaram TPI",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7MOHIV202175Definition("DEN"),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    // 7.6
    dataSetDefinition.addColumn(
        "MI7DEN6",
        "% de MG HIV+ em TARV que iniciou TPI e que terminou TPI",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7MOHIV202176Definition("DEN"),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    /** *****************************DENOMINATOR***************************** */

    // 7.1
    dataSetDefinition.addColumn(
        "MI7NUM1",
        "% de adultos HIV+ em TARV elegíveis ao TPT e que iniciaram TPT",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7MOHIV202171Definition("NUM"),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt=adultsArt");
    // 7.2
    dataSetDefinition.addColumn(
        "MI7NUM2",
        "% de adultos HIV+ em TARV elegiveis ao TPT que iniciaram e  completaram TPT",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7MOHIV202172Definition("NUM"),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt=adultsArt");
    // 7.3
    dataSetDefinition.addColumn(
        "MI7NUM3",
        "% de crianças HIV+ em TARV elegiveis ao TPT  e que iniciaram TPT",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7MOHIV202173Definition("NUM"),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt=childrenArt");
    // 7.4
    dataSetDefinition.addColumn(
        "MI7NUM4",
        "% de crianças HIV+ em TARV elegíveis que iniciaram e completaram TPT",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7MOHIV202174Definition("NUM"),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt=childrenArt");
    // 7.5
    dataSetDefinition.addColumn(
        "MI7NUM5",
        "% de mulheres grávidas HIV+ elegíveis ao TPI e que iniciaram TPI",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7MOHIV202175Definition("NUM"),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    // 7.6
    dataSetDefinition.addColumn(
        "MI7NUM6",
        "% de MG HIV+ em TARV que iniciou TPI e que terminou TPI",
        EptsReportUtils.map(
            customCohortIndicator(
                intensiveMonitoringCohortQueries.getCat7MOHIV202176Definition("NUM"),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    /**
     * **************************************************** CATEGORY 11
     * ***************************************************
     */

    /** *****************************DENOMINATOR***************************** */
    CohortIndicator MI11DEN1 =
        eptsGeneralIndicator.getIndicator(
            "MI11DEN1",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC11DEN(1, "MI"),
                "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"));

    MI11DEN1.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI11DEN1",
        "Adultos em TARV com o mínimo de 3 consultas de seguimento de adesão na FM-ficha de APSS/PP nos primeiros 3 meses após início do TARV",
        EptsReportUtils.map(
            MI11DEN1,
            "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt=adultsArt");

    CohortIndicator MI11DEN2 =
        eptsGeneralIndicator.getIndicator(
            "MI11DEN2",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC11DEN(2, "MI"),
                "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"));

    MI11DEN2.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI11DEN2",
        "Pacientes na 1a linha de TARV com CV acima de 1000 cópias que tiveram 3 consultas de APSS/PP mensais consecutivas para reforço de adesão",
        EptsReportUtils.map(
            MI11DEN2,
            "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt=adultsArt");

    CohortIndicator MI11DEN3 =
        eptsGeneralIndicator.getIndicator(
            "MI11DEN3",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC11DEN(3, "MI"),
                "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"));

    MI11DEN3.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI11DEN3",
        " MG em TARV com o mínimo de 3 consultas de seguimento de adesão na FM-ficha de APSS/PP nos primeiros 3 meses após início do TARV",
        EptsReportUtils.map(
            MI11DEN3,
            "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI11DEN4 =
        eptsGeneralIndicator.getIndicator(
            "MI11DEN4",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC11DEN(4, "MI"),
                "startDate=${revisionEndDate-4m+1d},endDate=${revisionEndDate-3m},revisionEndDate=${revisionEndDate},location=${location}"));

    MI11DEN4.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI11DEN4",
        "MG na 1a linha de TARV com CV acima de 1000 cópias que tiveram 3 consultas de APSS/PP mensais consecutivas para reforço de adesão",
        EptsReportUtils.map(
            MI11DEN4,
            "startDate=${revisionEndDate-4m+1d},endDate=${revisionEndDate-3m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI11DEN5 =
        eptsGeneralIndicator.getIndicator(
            "MI11DEN5",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC11DEN(5, "MI"),
                "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"));

    MI11DEN5.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI11DEN5",
        "Crianças > 2 anos de idade em TARV com registo mensal de seguimento da adesão na ficha de APSS/PP nos primeiros 99 dias de TARV",
        EptsReportUtils.map(
            MI11DEN5,
            "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt=2-14");

    CohortIndicator MI11DEN6 =
        eptsGeneralIndicator.getIndicator(
            "MI11DEN6",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC11DEN(6, "MI"),
                "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"));

    MI11DEN6.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI11DEN6",
        "Crianças < 2 anos de idade em TARV com registo mensal de seguimento da adesão na ficha de APSS/PP no primeiro Ano de TARV",
        EptsReportUtils.map(
            MI11DEN6,
            "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt=<2");

    CohortIndicator MI11DEN7 =
        eptsGeneralIndicator.getIndicator(
            "MI11DEN7",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC11DEN(7, "MI"),
                "startDate=${revisionEndDate-4m+1d},endDate=${revisionEndDate-3m},revisionEndDate=${revisionEndDate},location=${location}"));

    MI11DEN7.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI11DEN7",
        "Crianças (0-14 anos) na 1a linha de TARV com CV acima de 1000 cópias que tiveram 3 consultas mensais consecutivas de APSS/PP para reforço de adesão",
        EptsReportUtils.map(
            MI11DEN7,
            "startDate=${revisionEndDate-4m+1d},endDate=${revisionEndDate-3m},revisionEndDate=${revisionEndDate},location=${location}"),
        "mqAge=MqChildren");

    /** ***************************** NUMERATOR ***************************** */
    CohortIndicator MI11NUM1 =
        eptsGeneralIndicator.getIndicator(
            "MI11NUM1",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC11NumAnotCnotDnotEnotFandGAdultss("MI"),
                "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"));

    MI11NUM1.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI11NUM1",
        "1.1. % de adultos em TARV com o mínimo de 3 consultas de seguimento de adesão na FM-ficha de APSS/PP nos primeiros 3 meses após início do TARV",
        EptsReportUtils.map(
            MI11NUM1,
            "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt=adultsArt");

    CohortIndicator MI11NUM2 =
        eptsGeneralIndicator.getIndicator(
            "MI11NUM2",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries
                    .getMQC11NumB1nB2notCnotDnotEnotEnotFnHandAdultss("MI"),
                "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"));

    MI11NUM2.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI11NUM2",
        "11.2. % de pacientes na 1a linha de TARV com CV acima de 1000 cópias que tiveram 3 consultas de APSS/PP mensais consecutivas para reforço de adesão ",
        EptsReportUtils.map(
            MI11NUM2,
            "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt=adultsArt");

    CohortIndicator MI11NUM3 =
        eptsGeneralIndicator.getIndicator(
            "MI11NUM3",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC11NumAnB3nCnotDnotEnotEnotFnG("MI"),
                "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"));

    MI11NUM3.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI11NUM3",
        "11.3. % de crianças >2 anos de idade em TARV com registo mensal de seguimento da adesão na ficha de APSS/PP nos primeiros 99 dias de TARV ",
        EptsReportUtils.map(
            MI11NUM3,
            "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI11NUM4 =
        eptsGeneralIndicator.getIndicator(
            "MI11NUM4",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC11NumB1nB2nB3nCnotDnotEnotEnotFnH("MI"),
                "startDate=${revisionEndDate-4m+1d},endDate=${revisionEndDate-3m},revisionEndDate=${revisionEndDate},location=${location}"));

    MI11NUM4.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI11NUM4",
        "11.4. % de crianças <2 anos de idade em TARV com registo mensal de seguimento da adesão na ficha de APSS/PP no primeiro ano de TARV ",
        EptsReportUtils.map(
            MI11NUM4,
            "startDate=${revisionEndDate-4m+1d},endDate=${revisionEndDate-3m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI11NUM5 =
        eptsGeneralIndicator.getIndicator(
            "MI11NUM5",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC11NumAnotCnotDnotEnotFnotGnChildren("MI"),
                "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"));

    MI11NUM5.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI11NUM5",
        "11.5. % de crianças (0-14 anos) na 1a linha de TARV com CV acima de 1000 cópias que tiveram 3 consultas mensais consecutivas de APSS/PP para reforço de adesão",
        EptsReportUtils.map(
            MI11NUM5,
            "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt=2-14");

    CohortIndicator MI11NUM6 =
        eptsGeneralIndicator.getIndicator(
            "MI11NUM6",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC11NumAnotCnotDnotEnotFnotIlessThan9Month(
                    "MI"),
                "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"));

    MI11NUM6.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI11NUM6",
        "11.6. % de crianças <2 anos de idade em TARV com registo mensal de seguimento da adesão na ficha de APSS/PP no primeiro ano de TARV (Line 61 in the template) Numerador (Column D in the",
        EptsReportUtils.map(
            MI11NUM6,
            "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt=<2");

    CohortIndicator MI11NUM7 =
        eptsGeneralIndicator.getIndicator(
            "MI11NUM7",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC11NumB1nB2notCnotDnotEnotFnHChildren(
                    "MI"),
                "startDate=${revisionEndDate-4m+1d},endDate=${revisionEndDate-3m},revisionEndDate=${revisionEndDate},location=${location}"));

    MI11NUM7.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI11NUM7",
        "11.7. % de crianças (0-14 anos) na 1a linha de TARV com CV acima de 1000 cópias que tiveram 3  consultas mensais consecutivas de APSS/PP para reforço de adesão",
        EptsReportUtils.map(
            MI11NUM7,
            "startDate=${revisionEndDate-4m+1d},endDate=${revisionEndDate-3m},revisionEndDate=${revisionEndDate},location=${location}"),
        "mqAge=MqChildren");

    /**
     * **************************************************** CATEGORY 12
     * ***************************************************
     */

    /** *****************************DENOMINATOR***************************** */
    CohortIndicator MI12DEN1 =
        eptsGeneralIndicator.getIndicator(
            "MI12DEN1",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ12DEN(1),
                "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},revisionEndDate=${revisionEndDate},location=${location}"));

    MI12DEN1.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI12DEN1",
        "# de adultos (15/+anos) que iniciaram o TARV no período de inclusão e que retornaram para uma consulta clínica ou levantamento de ARVs dentro de 33 dias após o início do TARV",
        EptsReportUtils.map(
            MI12DEN1,
            "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI12DEN2 =
        eptsGeneralIndicator.getIndicator(
            "MI12DEN2",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ12DEN(2),
                "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"));

    MI12DEN2.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI12DEN2",
        "# de adultos (15/+anos) que iniciaram o TARV no período de inclusão e que tiveram consultas clínicas ou levantamentos de ARVs dentro de 99 dias após o início do TARV",
        EptsReportUtils.map(
            MI12DEN2,
            "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI12DEN5 =
        eptsGeneralIndicator.getIndicator(
            "MI12DEN5",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ12DEN(5),
                "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},revisionEndDate=${revisionEndDate},location=${location}"));

    MI12DEN5.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI12DEN5",
        "Crianças (0-14 anos) que iniciaram o TARV no período de inclusão e que retornaram para uma consulta clínica ou levantamento de ARVs dentro de 33 dias após o início do TARV",
        EptsReportUtils.map(
            MI12DEN5,
            "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},revisionEndDate=${revisionEndDate},location=${location}"),
        "ageBasedOnArt=childrenArt");

    CohortIndicator MI12DEN6 =
        eptsGeneralIndicator.getIndicator(
            "MI12DEN6",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ12DEN(6),
                "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"));

    MI12DEN6.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI12DEN6",
        "# de crianças (0-14 anos) que iniciaram o TARV no período de inclusão e que retornaram para uma consulta clínica ou levantamento de ARVs dentro de 33 dias após o início do TARV",
        EptsReportUtils.map(
            MI12DEN6,
            "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI12DEN9 =
        eptsGeneralIndicator.getIndicator(
            "MI12DEN9",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ12DEN(9),
                "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},revisionEndDate=${revisionEndDate},location=${location}"));

    MI12DEN9.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI12DEN9",
        "# de mulheres grávidas HIV+  que iniciaram o TARV no período de inclusão e que retornaram para uma consulta clínica ou levantamento de ARVs dentro de 33 dias após o início do TARV",
        EptsReportUtils.map(
            MI12DEN9,
            "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI12DEN10 =
        eptsGeneralIndicator.getIndicator(
            "MI12DEN10",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ12DEN(10),
                "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"));

    MI12DEN10.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI12DEN10",
        "# de mulheres grávidas HIV+  que iniciaram o TARV no período de inclusão e que retornaram para uma consulta clínica ou levantamento de ARVs dentro de 33 dias após o início do TARV",
        EptsReportUtils.map(
            MI12DEN10,
            "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    /** ***************************** NUMERATOR ***************************** */
    CohortIndicator MI12NUM1 =
        eptsGeneralIndicator.getIndicator(
            "MI12NUM1",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ12NUM(1),
                "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},revisionEndDate=${revisionEndDate},location=${location}"));

    MI12NUM1.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI12NUM1",
        "# de adultos (15/+anos) que iniciaram o TARV no período de inclusão e que retornaram para uma consulta clínica ou levantamento de ARVs entre 25 a 33 dias após o início do TARV",
        EptsReportUtils.map(
            MI12NUM1,
            "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI12NUM2 =
        eptsGeneralIndicator.getIndicator(
            "MI12NUM2",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ12NUM(2),
                "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"));

    MI12NUM2.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI12NUM2",
        "# de adultos (15/+anos) que iniciaram o TARV no período de inclusão e que tiveram 3 consultas clínicas ou levantamentos de ARVs dentro de 99 dias após o início do TARV",
        EptsReportUtils.map(
            MI12NUM2,
            "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI12NUM5 =
        eptsGeneralIndicator.getIndicator(
            "MI12NUM5",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ12NUM(5),
                "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},revisionEndDate=${revisionEndDate},location=${location}"));

    MI12NUM5.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI12NUM5",
        "# # de crianças (0-14 anos) que iniciaram o TARV no período de inclusão e que retornaram para uma consulta clínica ou levantamento de ARVs dentro de 33 dias após o início do TARV",
        EptsReportUtils.map(
            MI12NUM5,
            "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI12NUM6 =
        eptsGeneralIndicator.getIndicator(
            "MI12NUM6",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ12NUM(6),
                "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"));

    MI12NUM6.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI12NUM6",
        "# de crianças (0-14 anos) que iniciaram o TARV no período de inclusão e que retornaram para uma consulta clínica ou levantamento de ARVs dentro de 33 dias após o início do TARV",
        EptsReportUtils.map(
            MI12NUM6,
            "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI12NUM9 =
        eptsGeneralIndicator.getIndicator(
            "MI12NUM9",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ12NUM(9),
                "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},revisionEndDate=${revisionEndDate},location=${location}"));

    MI12NUM9.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI12NUM9",
        "No de crianças (0-14 anos)  que iniciaram 2ª linha TARV há 12 meses atrás",
        EptsReportUtils.map(
            MI12NUM9,
            "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI12NUM10 =
        eptsGeneralIndicator.getIndicator(
            "MQ12NUM10",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ12NUM(10),
                "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"));

    MI12NUM10.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI12NUM10",
        "# de mulheres grávidas HIV+  que iniciaram o TARV no período de inclusão e que retornaram para uma consulta clínica ou levantamento de ARVs dentro de 33 dias após o início do TARV",
        EptsReportUtils.map(
            MI12NUM10,
            "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    /**
     * **************************************************** CATEGORY 13
     * ***************************************************
     */

    /** *****************************DENOMINATOR***************************** */
    CohortIndicator MI13DEN1 =
        eptsGeneralIndicator.getIndicator(
            "MI13DEN1",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ13(true, 1),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13DEN1.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13DEN1",
        "% de adultos (15/+anos) na 1a linha de TARV que tiveram consulta clínica no período de revisão, eram elegíveis ao pedido de CV e com registo de pedido de CV feito pelo clínico",
        EptsReportUtils.map(
            MI13DEN1,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI13DEN2 =
        eptsGeneralIndicator.getIndicator(
            "MI13DEN2",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC13P3DEN(2),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));
    MI13DEN2.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    dataSetDefinition.addColumn(
        "MI13DEN2",
        "Adultos (15/+anos) na 1a linha de TARV que receberam o resultado da CV entre o sexto e o nono mês após início do TARV",
        EptsReportUtils.map(
            MI13DEN2,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI13DEN3 =
        eptsGeneralIndicator.getIndicator(
            "MI13DEN3",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ13P4(true, 3),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));
    MI13DEN3.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    dataSetDefinition.addColumn(
        "MI13DEN3",
        "# de adultos na 1a linha de TARV que receberam um resultado de CV acima de 1000 cópias no período de inclusão",
        EptsReportUtils.map(
            MI13DEN3,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "mqAge=MqAdults");

    CohortIndicator MI13DEN4 =
        eptsGeneralIndicator.getIndicator(
            "MI13DEN4",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ13(true, 4),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13DEN4.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13DEN4",
        "# de adultos (15/+ anos) na 2a linha de TARV elegíveis a CV.",
        EptsReportUtils.map(
            MI13DEN4,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI13DEN5 =
        eptsGeneralIndicator.getIndicator(
            "MI13DEN5",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC13P3DEN(5),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13DEN5.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13DEN5",
        "Adultos (15/+anos) na 2a linha de TARV que receberam o resultado da CV entre o sexto e o nono mês após o início da 2a linha de TARV",
        EptsReportUtils.map(
            MI13DEN5,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI13DEN6 =
        eptsGeneralIndicator.getIndicator(
            "MI13DEN6",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ13(true, 6),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13DEN6.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13DEN6",
        "% de crianças (0-4 anos de idade) na 1a linha de TARV que tiveram consulta clínica no período de revisão, eram elegíveis ao pedido de CV e com registo de pedido de CV feito pelo clínico.",
        EptsReportUtils.map(
            MI13DEN6,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI13DEN7 =
        eptsGeneralIndicator.getIndicator(
            "MI13DEN7",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ13(true, 7),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13DEN7.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13DEN7",
        "% de crianças (5-9 anos de idade) na 1a linha de TARV que tiveram consulta clínica no período de revisão, eram elegíveis ao pedido de CV e com registo de pedido de CV feito pelo clínico.",
        EptsReportUtils.map(
            MI13DEN7,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI13DEN8 =
        eptsGeneralIndicator.getIndicator(
            "MI13DEN8",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ13(true, 8),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13DEN8.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13DEN8",
        "% de crianças (10-14 anos de idade) na 1a linha de TARV que tiveram consulta clínica no período de revisão, eram elegíveis ao pedido de CV e com registo de pedido de CV feito pelo clínico.",
        EptsReportUtils.map(
            MI13DEN8,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    CohortIndicator MI13DEN9 =
        eptsGeneralIndicator.getIndicator(
            "MI13DEN9",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC13P3DEN(9),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13DEN9.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13DEN9",
        "Crianças  (0-4 anos de idade) na 1a linha de TARV que receberam o resultado da Carga Viral entre o sexto e o nono mês após o início do TARV",
        EptsReportUtils.map(
            MI13DEN9,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI13DEN10 =
        eptsGeneralIndicator.getIndicator(
            "MI13DEN10",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC13P3DEN(10),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13DEN10.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13DEN10",
        "Crianças  (5-9 anos de idade) na 1a linha de TARV que receberam o resultado da Carga Viral entre o sexto e o nono mês após o início do TARV",
        EptsReportUtils.map(
            MI13DEN10,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI13DEN11 =
        eptsGeneralIndicator.getIndicator(
            "MI13DEN11",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC13P3DEN(11),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13DEN11.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    dataSetDefinition.addColumn(
        "MI13DEN11",
        "crianças  (10-14 anos de idade) na 1a linha de TARV que receberam o resultado da Carga Viral entre o sexto e o nono mês após o início do TARV ",
        EptsReportUtils.map(
            MI13DEN11,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    CohortIndicator MI13DEN12 =
        eptsGeneralIndicator.getIndicator(
            "MI13DEN12",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ13P4(true, 12),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13DEN12.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13DEN12",
        "# de crianças (>2 anos de idade) na 1a linha de TARV que receberam um resultado de CV acima de 1000 cópias no período de inclusão",
        EptsReportUtils.map(
            MI13DEN12,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI13DEN13 =
        eptsGeneralIndicator.getIndicator(
            "MI13DEN13",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ13(true, 13),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13DEN13.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13DEN13",
        "# de crianças (>2anos) na 2a linha de TARV elegíveis ao pedido de CV.",
        EptsReportUtils.map(
            MI13DEN13,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    CohortIndicator MI13DEN14 =
        eptsGeneralIndicator.getIndicator(
            "MI13DEN14",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC13P3DEN(14),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13DEN14.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13DEN14",
        "Crianças na 2a linha de TARV que receberam o resultado da Carga Viral entre o sexto e o nono mês após o início da 2a linha de TARV",
        EptsReportUtils.map(
            MI13DEN14,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    // Categoria  13 part 2  Denominator

    CohortIndicator MI13DEN15 =
        eptsGeneralIndicator.getIndicator(
            "MI13DEN15",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getgetMQC13P2DenMGInIncluisionPeriod(),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13DEN15.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13DEN15",
        "13.15. % de MG elegíveis a CV com registo de pedido de CV feito pelo clínico (MG que iniciaram TARV na CPN) Denominator: # de MG com registo de início do TARV na CPN dentro do período de inclusão. (Line 90,Column F in the Template) as following",
        EptsReportUtils.map(
            MI13DEN15,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI13DEN16 =
        eptsGeneralIndicator.getIndicator(
            "MI13DEN16",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getgetMQC13P2DenMGInIncluisionPeriod33Month(),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13DEN16.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13DEN16",
        "13.16. % de MG elegíveis a CV com registo de pedido de CV feito pelo clínico na primeira CPN (MG que entraram em TARV na CPN) Denominator:# de MG que tiveram a primeira CPN no período de inclusão, e que já estavam em TARV há mais de 3 meses  (Line 91,Column F in the Template) as following:",
        EptsReportUtils.map(
            MI13DEN16,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI13DEN17 =
        eptsGeneralIndicator.getIndicator(
            "MI13DEN17",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC13P2DenMGInIncluisionPeriod33Days(),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13DEN17.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13DEN17",
        "13.17. % de MG que receberam o resultado da Carga Viral dentro de 33 dias após pedido Denominator: # de MG com registo de pedido de CV no período de revisão (Line 92,Column F in the Template) as following:<",
        EptsReportUtils.map(
            MI13DEN17,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI13DEN18 =
        eptsGeneralIndicator.getIndicator(
            "MI13DEN18",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ13P4(true, 18),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13DEN18.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13DEN18",
        "# de MG na 1a linha de TARV que receberam um resultado de CV acima de 1000 cópias no período de inclusão",
        EptsReportUtils.map(
            MI13DEN18,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    /** ***************************** NUMERATOR ***************************** */
    CohortIndicator MI13NUM1 =
        eptsGeneralIndicator.getIndicator(
            "MI13NUM1",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ13(false, 1),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13NUM1.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13NUM1",
        "% de adultos (15/+anos) na 1a linha de TARV que tiveram consulta clínica no período de revisão, eram elegíveis ao pedido de CV e com registo de pedido de CV feito pelo clínico",
        EptsReportUtils.map(
            MI13NUM1,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI13NUM2 =
        eptsGeneralIndicator.getIndicator(
            "MI13NUM2",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC13P3NUM(2),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13NUM2.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13NUM2",
        "Adultos (15/+anos) na 1a linha de TARV que receberam o resultado da CV entre o sexto e o nono mês após início do TARV",
        EptsReportUtils.map(
            MI13NUM2,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI13NUM3 =
        eptsGeneralIndicator.getIndicator(
            "MI13NUM3",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ13P4(false, 3),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13NUM3.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13NUM3",
        "% de Adultos (15/+anos) na 1ª linha de TARV com registo de pedido de CV entre o 3º e o 4º mês após terem recebido o último resultado de CV acima de 1000 e terem  3 sessões consecutivas de APSS/PP",
        EptsReportUtils.map(
            MI13NUM3,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "mqAge=MqAdults");

    CohortIndicator MI13NUM4 =
        eptsGeneralIndicator.getIndicator(
            "MI13NUM4",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ13(false, 4),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13NUM4.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13NUM4",
        "% de adultos (15/+anos) na 2a linha de TARV elegíveis a CV com registo de pedido de CV feito pelo clínico",
        EptsReportUtils.map(
            MI13NUM4,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI13NUM5 =
        eptsGeneralIndicator.getIndicator(
            "MI13NUM5",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC13P3NUM(5),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13NUM5.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13NUM5",
        "adultos (15/+anos) na 2a linha de TARV que receberam o resultado da CV entre o sexto e o nono mês após o início da 2a linha de TARV",
        EptsReportUtils.map(
            MI13NUM5,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    CohortIndicator MI13NUM6 =
        eptsGeneralIndicator.getIndicator(
            "MI13NUM6",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ13(false, 6),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13NUM6.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13NUM6",
        "% de crianças (0-4 anos de idade) na 1a linha de TARV que tiveram consulta clínica no período de revisão, eram elegíveis ao pedido de CV e com registo de pedido de CV feito pelo clínico.",
        EptsReportUtils.map(
            MI13NUM6,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI13NUM7 =
        eptsGeneralIndicator.getIndicator(
            "MI13NUM7",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ13(false, 7),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13NUM7.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13NUM7",
        "% de crianças (5-9 anos de idade) na 1a linha de TARV que tiveram consulta clínica no período de revisão, eram elegíveis ao pedido de CV e com registo de pedido de CV feito pelo clínico.",
        EptsReportUtils.map(
            MI13NUM7,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI13NUM8 =
        eptsGeneralIndicator.getIndicator(
            "MI13NUM8",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ13(false, 8),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13NUM8.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13NUM8",
        "% de crianças (10-14 anos de idade) na 1a linha de TARV que tiveram consulta clínica no período de revisão, eram elegíveis ao pedido de CV e com registo de pedido de CV feito pelo clínico.",
        EptsReportUtils.map(
            MI13NUM8,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");
    CohortIndicator MI13NUM9 =
        eptsGeneralIndicator.getIndicator(
            "MI13NUM9",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC13P3NUM(9),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13NUM9.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13NUM9",
        "Crianças  (0-4 anos de idade) na 1a linha de TARV que receberam o resultado da Carga Viral entre o sexto e o nono mês após o início do TARV",
        EptsReportUtils.map(
            MI13NUM9,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI13NUM10 =
        eptsGeneralIndicator.getIndicator(
            "MI13NUM10",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC13P3NUM(10),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13NUM10.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13NUM10",
        "Crianças  (5-9 anos de idade) na 1a linha de TARV que receberam o resultado da Carga Viral entre o sexto e o nono mês após o início do TARV",
        EptsReportUtils.map(
            MI13NUM10,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI13NUM11 =
        eptsGeneralIndicator.getIndicator(
            "MI13NUM11",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC13P3NUM(11),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13NUM11.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13NUM11",
        " crianças  (10-14 anos de idade) na 1a linha de TARV que receberam o resultado da Carga Viral entre o sexto e o nono mês após o início do TARV",
        EptsReportUtils.map(
            MI13NUM11,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI13NUM12 =
        eptsGeneralIndicator.getIndicator(
            "MI13NUM12",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ13P4(false, 12),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13NUM12.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13NUM12",
        "% de crianças (>2 anos de idade) na 1ª linha de TARV com registo de pedido de CV entre o 3º e o 4º mês após terem recebido o último resultado de CV acima de 1000 cópia e terem  3 sessões consecutivas de APSS/PP",
        EptsReportUtils.map(
            MI13NUM12,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "age=2-14");

    CohortIndicator MI13NUM13 =
        eptsGeneralIndicator.getIndicator(
            "MI13NUM13",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ13(false, 13),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13NUM13.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13NUM13",
        "% de crianças na  2ª linha de TARV elegíveis ao pedido de CV e com registo de pedido de CV feito pelo clínico",
        EptsReportUtils.map(
            MI13NUM13,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI13NUM14 =
        eptsGeneralIndicator.getIndicator(
            "MI13NUM14",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC13P3NUM(14),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13NUM14.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13NUM14",
        "Crianças na 2a linha de TARV que receberam o resultado da Carga Viral entre o sexto e o nono mês após o início da 2a linha de TARV",
        EptsReportUtils.map(
            MI13NUM14,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    // Categoria  13 part 2  Numerator

    CohortIndicator MI13NUM15 =
        eptsGeneralIndicator.getIndicator(
            "MI13NUM15",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC13P2Num1(),
                "revisionEndDate=${revisionEndDate},location=${location}"));

    MI13NUM15.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13NUM15",
        "13.15. % de MG elegíveis a CV com registo de pedido de CV feito pelo clínico (MG que iniciaram TARV na CPN) Denominator: # de MG com registo de início do TARV na CPN dentro do período de inclusão. (Line 90,Column F in the Template) as following",
        EptsReportUtils.map(
            MI13NUM15,
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI13NUM16 =
        eptsGeneralIndicator.getIndicator(
            "MI13NUM16",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC13P2Num2(),
                "revisionEndDate=${revisionEndDate},location=${location}"));

    MI13NUM16.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13NUM16",
        "13.16. % de MG elegíveis a CV com registo de pedido de CV feito pelo clínico na primeira CPN (MG que entraram em TARV na CPN) Denominator:# de MG que tiveram a primeira CPN no período de inclusão, e que já estavam em TARV há mais de 3 meses  (Line 91,Column F in the Template) as following:",
        EptsReportUtils.map(
            MI13NUM16,
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI13NUM17 =
        eptsGeneralIndicator.getIndicator(
            "MI13NUM17",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQC13P2Num3(),
                "revisionEndDate=${revisionEndDate},location=${location}"));

    MI13NUM17.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13NUM17",
        "13.17. % de MG que receberam o resultado da Carga Viral dentro de 33 dias após pedido Denominator: # de MG com registo de pedido de CV no período de revisão (Line 92,Column F in the Template) as following:<",
        EptsReportUtils.map(
            MI13NUM17,
            "revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    CohortIndicator MI13NUM18 =
        eptsGeneralIndicator.getIndicator(
            "MI13NUM18",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ13P4(false, 18),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI13NUM18.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI13NUM18",
        "% de MG na 1ª linha de TARV com registo de pedido de CV entre o 3º e o 4º mês após terem recebido o último resultado de CV acima de 1000 cópia e terem 3 sessões consecutivas de APSS/PP",
        EptsReportUtils.map(
            MI13NUM18,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "");

    /**
     * **************************************************** CATEGORY 15
     * ***************************************************
     */

    /** ***************************** DENOMINATOR ***************************** */
    CohortIndicator MI15DEN1 =
        eptsGeneralIndicator.getIndicator(
            "MI15DEN1",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ15DEN(1),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI15DEN1.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI15DEN1",
        "% de Adultos (15/+anos) inscritos há 12 meses em algum MDS  (DT ou GAAC) que continuam activos em TARV",
        EptsReportUtils.map(
            MI15DEN1,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "age=15+");

    CohortIndicator MI15DEN2 =
        eptsGeneralIndicator.getIndicator(
            "MI15DEN2",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ15DEN(2),
                "revisionEndDate=${revisionEndDate},location=${location}"));

    MI15DEN2.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI15DEN2",
        "% de Adultos (15/+anos) inscritos há pelo menos 12 meses em algum MDS (DT ou GAAC) com pedido de pelo menos uma CV",
        EptsReportUtils.map(
            MI15DEN2,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "age=15+");

    CohortIndicator MI15DEN3 =
        eptsGeneralIndicator.getIndicator(
            "MI15DEN3",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ15DEN(3),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI15DEN3.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI15DEN3",
        "% de Adultos (15/+anos) inscritos há 12 meses em algum MDS (DT ou GAAC) que receberam pelo menos um resultado de CV",
        EptsReportUtils.map(
            MI15DEN3,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "age=15+");

    /** ***************************** NUMERATOR ***************************** */
    CohortIndicator MI15NUM1 =
        eptsGeneralIndicator.getIndicator(
            "MI15NUM1",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ15NUM(1),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI15NUM1.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI15NUM1",
        "% de Adultos (15/+anos) inscritos há 12 meses em algum MDS  (DT ou GAAC) que continuam activos em TARV",
        EptsReportUtils.map(
            MI15NUM1,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "age=15+");

    CohortIndicator MI15NUM2 =
        eptsGeneralIndicator.getIndicator(
            "MI15NUM2",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ15NUM(2),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI15NUM2.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI15NUM2",
        "% de Adultos (15/+anos) inscritos há pelo menos 12 meses em algum MDS (DT ou GAAC) com pedido de pelo menos uma CV",
        EptsReportUtils.map(
            MI15NUM2,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "age=15+");

    CohortIndicator MI15NUM3 =
        eptsGeneralIndicator.getIndicator(
            "MI15NUM3",
            EptsReportUtils.map(
                qualityImprovement2020CohortQueries.getMQ15NUM(3),
                "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    MI15NUM3.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    dataSetDefinition.addColumn(
        "MI15NUM3",
        "% de Adultos (15/+anos) inscritos há 12 meses em algum MDS (DT ou GAAC) que receberam pelo menos um resultado de CV",
        EptsReportUtils.map(
            MI15NUM3,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"),
        "age=15+");

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
