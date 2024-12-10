/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.eptsreports.reporting.library.datasets.resumo;

import static org.openmrs.module.reporting.evaluation.parameter.Mapped.mapStraightThrough;

import java.util.Arrays;
import java.util.Collections;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ResumoMensalCcrCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.datasets.BaseDataSet;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResumoMensalCcrDataSetDefinition extends BaseDataSet {

  private final EptsGeneralIndicator eptsGeneralIndicator;

  private final ResumoMensalCcrCohortQueries resumoMensalCcrCohortQueries;

  private final CommonMetadata commonMetadata;

  private final HivMetadata hivMetadata;

  @Autowired
  public ResumoMensalCcrDataSetDefinition(
      EptsGeneralIndicator eptsGeneralIndicator,
      ResumoMensalCcrCohortQueries resumoMensalCcrCohortQueries,
      CommonMetadata commonMetadata,
      HivMetadata hivMetadata) {
    this.eptsGeneralIndicator = eptsGeneralIndicator;
    this.resumoMensalCcrCohortQueries = resumoMensalCcrCohortQueries;
    this.commonMetadata = commonMetadata;
    this.hivMetadata = hivMetadata;
  }

  public DataSetDefinition constructResumoMensalDataset() {
    CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
    dsd.setName("Resumo Mensal CCR Dataset");
    dsd.addParameters(getParameters());

    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";

    // Indicador 1- Total de 1as Consultas
    dsd.addColumn("FIRST", "Total de 1as Consultas", getChildrenWithFirstConsultation(), "");

    // Indicador 2- Crianças com contacto com tuberculose
    dsd.addColumn(
        "TUBERCULOSIS", "Crianças com contacto com tuberculose", getChildrenWithTbContact(), "");

    // Indicador 3 - Crianças com desnutrição aguda moderada
    dsd.addColumn("DAM", "Crianças com desnutrição aguda moderada", getChildrenWithDam(), "");

    // Indicador 4 - Crianças com desnutrição aguda grave
    dsd.addColumn("DAG", "Crianças com desnutrição aguda grave", getChildrenWithDag(), "");

    // Indicador 5 - Crianças com exposição ao HIV
    dsd.addColumn("HIV", "Crianças com exposição ao HIV", getChildrenWithHivExposure(), "");

    // Indicador 6 - Crianças com outra condição de Risco
    dsd.addColumn(
        "OTHER", "Crianças com outra condição de Risco", getChildrenWithAnotherRiskCondition(), "");

    // Indicador 7 - Crianças que iniciaram Isoniazida na CCR
    dsd.addColumn(
        "INH", "Crianças que iniciaram Isoniazida na CCR", getChildrenWhoStartedInhOnCcr(), "");

    // Indicador 8 - Crianças que receberam ATPU
    dsd.addColumn("ATPU", "Crianças que receberam ATPU", getChildrenWhoReceivedAtpuonCcr(), "");

    // Indicador 9- Crianças que receberam CSB/suplemento nutricional
    dsd.addColumn(
        "CSB",
        "Crianças que receberam CSB/suplemento nutricional",
        getChildrenWhoReceivedCsbOnCcr(),
        "");

    // Indicador 10- Crianças que iniciaram CTZ < 2 meses de idade
    dsd.addColumn(
        "CTZA",
        "Crianças que iniciaram CTZ < 2 meses de idade",
        getChildrenWhoStartedCtzBellow2MonthsOfAge(),
        "");

    // Indicador 11 - Crianças que iniciaram CTZ ≥ 2 meses de idade
    dsd.addColumn(
        "CTZB",
        "Crianças que iniciaram CTZ >= 2 meses de idade",
        getChildrenWhoStartedCtzAndAbove2MonthsOfAge(),
        "");

    // Indicador 12 - 1º PCR colhido < 2 meses de idade
    dsd.addColumn(
        "PCRA",
        "1º PCR colhido < 2 meses de idade",
        getChildrenFirstPcrCollectedUnder2MonthsofAge(),
        "");

    // Indicador 13 - 1º PCR colhido ≥ 2 meses de idade
    dsd.addColumn(
        "PCRB",
        "1º PCR colhido >= 2 meses de idade",
        getChildrenFirstPcrCollectedAbove2MonthsofAge(),
        "");

    // Indicador 14 - Crianças expostas ≥9 meses testadas com Teste Rápido de HIV
    dsd.addColumn(
        "EXPOSED",
        "Crianças expostas ≥9 meses testadas com Teste Rápido de HIV",
        getChildrenAbove9MonthsofAgeExposedAndTested(),
        "");

    // Indicador 15 - Crianças não expostas ao HIV testadas com Teste Rápido de HIV
    dsd.addColumn(
        "NOTEXPOSED",
        "Crianças não expostas ao HIV testadas com Teste Rápido de HIV",
        getChildrenWithRapidTestAndNotExposedToHiv(),
        "");

    // Indicador 16 - Crianças não expostas ao HIV, testadas com Teste Rápido que tiveram resultado
    // positivo
    dsd.addColumn(
        "NOTEXPOSEDPOSITIVE",
        "Crianças não expostas ao HIV, testadas com Teste Rápido que tiveram resultado positivo",
        getChildrenNotExposedToHivAndWithPositiveTestResult(),
        "");

    // Crianças com 1as Consultas – Coorte de 9 meses
    CohortIndicator first9MONTHS =
        eptsGeneralIndicator.getIndicator(
            "FIRST9MONTHS",
            EptsReportUtils.map(
                resumoMensalCcrCohortQueries.getPatients1stConsultation(), mappings));

    dsd.addColumn(
        "F9M",
        "Crianças com 1as Consultas – Coorte de 9 meses",
        EptsReportUtils.map(first9MONTHS, mappings),
        "");

    // Indicador 20 - Crianças com contacto com TB – coorte de 9 meses
    CohortIndicator tb9MONTHS =
        eptsGeneralIndicator.getIndicator(
            "TB9MONTHS",
            EptsReportUtils.map(
                resumoMensalCcrCohortQueries.getChildrenWithVisitReason(
                    Collections.singletonList(
                        commonMetadata.getContactoTbConcept().getConceptId())),
                mappings));

    dsd.addColumn(
        "TB9M",
        "Crianças com 1as Consultas – Coorte de 9 meses",
        EptsReportUtils.map(tb9MONTHS, mappings),
        "");

    // Indicador 21 - Crianças que completaram Isonizada – coorte de 9 meses
    CohortIndicator completedinh =
        eptsGeneralIndicator.getIndicator(
            "COMPLETEDINH",
            EptsReportUtils.map(
                resumoMensalCcrCohortQueries.getChildrenWhoCompletedINH(), mappings));

    dsd.addColumn(
        "CINH",
        "Crianças que completaram Isonizada – coorte de 9 meses",
        EptsReportUtils.map(completedinh, mappings),
        "");

    // Indicador 22 - Crianças referidas para PNCT – coorte de 9 meses
    CohortIndicator pnct =
        eptsGeneralIndicator.getIndicator(
            "PNCT", EptsReportUtils.map(resumoMensalCcrCohortQueries.getChildrenPnct(), mappings));

    dsd.addColumn(
        "PNCT",
        "Crianças que completaram Isonizada – coorte de 9 meses",
        EptsReportUtils.map(pnct, mappings),
        "");

    // Indicador 23 - Crianças que abandonaram – coorte de 9 meses
    CohortIndicator abandoned9MONTHS =
        eptsGeneralIndicator.getIndicator(
            "ABANDONED9MONTHS",
            EptsReportUtils.map(
                resumoMensalCcrCohortQueries.getChildrenWhoAbandonedBeforePeriod(), mappings));

    dsd.addColumn(
        "A9M",
        "Crianças que abandonaram – coorte de 9 meses",
        EptsReportUtils.map(abandoned9MONTHS, mappings),
        "");

    // Indicador 24 - Crianças com DAM – coorte de 9 meses
    CohortIndicator dam9MONTHS =
        eptsGeneralIndicator.getIndicator(
            "DAM9MONTHS",
            EptsReportUtils.map(resumoMensalCcrCohortQueries.getChildrenWithDam(), mappings));

    dsd.addColumn(
        "DAM9M",
        "Crianças com DAM – coorte de 9 meses",
        EptsReportUtils.map(dam9MONTHS, mappings),
        "");

    // Indicador 25 - Crianças com DAM recuperadas – coorte de 9 meses
    CohortIndicator restoreddam9MONTHS =
        eptsGeneralIndicator.getIndicator(
            "RESTOREDDAM9MONTHS",
            EptsReportUtils.map(
                resumoMensalCcrCohortQueries.getChildrenWithRestoredDamBeforePeriod(), mappings));

    dsd.addColumn(
        "RDAM9M",
        "Crianças com DAM recuperadas – coorte de 9 meses",
        EptsReportUtils.map(restoreddam9MONTHS, mappings),
        "");

    // Indicador 26 - Crianças com DAM que abandonaram – coorte de 9 meses
    CohortIndicator abandoneddam9MONTHS =
        eptsGeneralIndicator.getIndicator(
            "ABANDONEDDAM9MONTHS",
            EptsReportUtils.map(
                resumoMensalCcrCohortQueries.getChildrenWhoAbandonedDam(), mappings));

    dsd.addColumn(
        "AD9M",
        "Crianças com DAM que abandonaram – coorte de 9 meses",
        EptsReportUtils.map(abandoneddam9MONTHS, mappings),
        "");

    // Indicador 27 - Crianças com DAG – coorte de 9 meses
    CohortIndicator dag9MONTHS =
        eptsGeneralIndicator.getIndicator(
            "DAG9MONTHS",
            EptsReportUtils.map(resumoMensalCcrCohortQueries.getChildrenWithDag(), mappings));

    dsd.addColumn(
        "D9M",
        "Crianças com DAG – coorte de 9 meses",
        EptsReportUtils.map(dag9MONTHS, mappings),
        "");

    // Indicador 28 - Crianças com DAG que foram referidas para internamento – coorte de 9 meses
    CohortIndicator daginternation9MONTHS =
        eptsGeneralIndicator.getIndicator(
            "DAGINTERNATION9MONTHS",
            EptsReportUtils.map(
                resumoMensalCcrCohortQueries.getChildrenWithDagReferredForInternation(), mappings));

    dsd.addColumn(
        "DI9M",
        "Crianças com DAG que foram referidas para internamento – coorte de 9 meses",
        EptsReportUtils.map(daginternation9MONTHS, mappings),
        "");

    // Indicador 29 - Crianças com DAG recuperadas – coorte de 9 meses
    CohortIndicator dagrestored9MONTHS =
        eptsGeneralIndicator.getIndicator(
            "DAGRESTORED9MONTHS",
            EptsReportUtils.map(
                resumoMensalCcrCohortQueries.getChildrenWithDagRestored(), mappings));

    dsd.addColumn(
        "DR9M",
        "Crianças com DAG recuperadas – coorte de 9 meses",
        EptsReportUtils.map(dagrestored9MONTHS, mappings),
        "");

    // Indicador 30 - Crianças que com DAG que abandonaram – coorte de 9 meses
    CohortIndicator dagabandoned9MONTHS =
        eptsGeneralIndicator.getIndicator(
            "DAGABANDONED9MONTHS",
            EptsReportUtils.map(
                resumoMensalCcrCohortQueries.getChildrenWithDagWhoAbandoned(), mappings));

    dsd.addColumn(
        "DA9M",
        "Crianças que com DAG que abandonaram – coorte de 9 meses",
        EptsReportUtils.map(dagabandoned9MONTHS, mappings),
        "");

    // Indicador 31 - Crianças com DAG que foram óbito – coorte de 9 meses
    CohortIndicator dagdead9MONTHS =
        eptsGeneralIndicator.getIndicator(
            "DAGDEAD9MONTHS",
            EptsReportUtils.map(
                resumoMensalCcrCohortQueries.getChildrenWithDagWhoDied(), mappings));

    dsd.addColumn(
        "DD9M",
        "Crianças com DAG que foram óbito – coorte de 9 meses",
        EptsReportUtils.map(dagdead9MONTHS, mappings),
        "");

    // Indicador 32 - Crianças expostas – coorte de 9 meses
    CohortIndicator exposed9MONTHS =
        eptsGeneralIndicator.getIndicator(
            "EXPOSED9MONTHS",
            EptsReportUtils.map(resumoMensalCcrCohortQueries.getExposedChildren(), mappings));

    dsd.addColumn(
        "E9M",
        "Crianças expostas – coorte de 9 meses",
        EptsReportUtils.map(exposed9MONTHS, mappings),
        "");

    // Indicador 33 - Crianças expostas com 5 meses de idade e com mãe em TARV – coorte de 9 meses
    CohortIndicator exposed5MONTHS =
        eptsGeneralIndicator.getIndicator(
            "EXPOSED5MONTHS",
            EptsReportUtils.map(
                resumoMensalCcrCohortQueries.getExposedChildren5MonthsOfAge(), mappings));

    dsd.addColumn(
        "E5M",
        "Crianças expostas com 5 meses de idade e com mãe em TARV – coorte de 9 meses",
        EptsReportUtils.map(exposed9MONTHS, mappings),
        "");

    // Indicador 34 - Crianças expostas com aleitamento materno exclusivo aos 5 meses – coorte de 9
    // meses
    CohortIndicator exposedbreatfed5MONTHS =
        eptsGeneralIndicator.getIndicator(
            "EXPOSEDBREATFED5MONTHS",
            EptsReportUtils.map(
                resumoMensalCcrCohortQueries.getExposedChildrenWithBreastfed5MonthsOfAge(),
                mappings));

    dsd.addColumn(
        "EB5M",
        "Crianças expostas com aleitamento materno exclusivo aos 5 meses – coorte de 9 meses",
        EptsReportUtils.map(exposedbreatfed5MONTHS, mappings),
        "");

    // Indicador 35 - Total de crianças expostas com aleitamento materno exclusivo aos 5 meses –
    // coorte 9 meses
    // meses
    CohortIndicator exposedmixedfeed5MONTHS =
        eptsGeneralIndicator.getIndicator(
            "EXPOSEDMIXEDFEED5MONTHS",
            EptsReportUtils.map(
                resumoMensalCcrCohortQueries.getExposedChildrenWithMixedFeeding5MonthsOfAge(),
                mappings));

    dsd.addColumn(
        "EMX5M",
        "Total de crianças expostas com aleitamento materno exclusivo aos 5 meses – coorte 9 meses",
        EptsReportUtils.map(exposedmixedfeed5MONTHS, mappings),
        "");

    // Indicador 36 - Crianças que receberam ARV aos 5 meses – coorte de 9 meses
    CohortIndicator exposedarv5MONTHS =
        eptsGeneralIndicator.getIndicator(
            "EXPOSEDARV5MONTHS",
            EptsReportUtils.map(
                resumoMensalCcrCohortQueries.getExposedChildrenWhoReceivedArv5MonthsOfAge(),
                mappings));

    dsd.addColumn(
        "EARV5M",
        "Crianças que receberam ARV aos 5 meses – coorte de 9 meses",
        EptsReportUtils.map(exposedarv5MONTHS, mappings),
        "");

    // Indicador 37 - PCR colhido <2 meses de idade  – coorte de 9 meses
    CohortIndicator pcr2MONTHSA =
        eptsGeneralIndicator.getIndicator(
            "PCR2MONTHSA",
            EptsReportUtils.map(
                resumoMensalCcrCohortQueries
                    .getExposedChildrenWhoReceivedPcrWithLessThan2MonthsOfAge(),
                mappings));

    dsd.addColumn(
        "PCR2A",
        "PCR colhido <2 meses de idade  – coorte de 9 meses",
        EptsReportUtils.map(pcr2MONTHSA, mappings),
        "");

    // Indicador 38 - PCR colhido >=2 meses de idade  – coorte de 9 meses
    CohortIndicator pcr2MONTHSB =
        eptsGeneralIndicator.getIndicator(
            "PCR2MONTHSB",
            EptsReportUtils.map(
                resumoMensalCcrCohortQueries
                    .getExposedChildrenWhoReceivedPcrWithMoreThan2MonthsOfAge(),
                mappings));

    dsd.addColumn(
        "PCR2B",
        "PCR colhido >=2 meses de idade  – coorte de 9 meses",
        EptsReportUtils.map(pcr2MONTHSB, mappings),
        "");

    // Indicador 39 - crianças com resultados PCR positivo <2 meses de idade  – coorte de 9 meses
    CohortIndicator pcrpositive2MONTHSA =
        eptsGeneralIndicator.getIndicator(
            "PCRPOSITIVE2MONTHSA",
            EptsReportUtils.map(
                resumoMensalCcrCohortQueries.getChildrenWithPositivePcrBellow2MonthsOfAge(),
                mappings));

    dsd.addColumn(
        "PCRP2A",
        "crianças com resultados PCR positivo <2 meses de idade  – coorte de 9 meses",
        EptsReportUtils.map(pcrpositive2MONTHSA, mappings),
        "");

    // Indicador 40 - crianças com resultados PCR positivo >= 2 meses de idade  – coorte de 9 meses
    CohortIndicator pcrpositive2MONTHSB =
        eptsGeneralIndicator.getIndicator(
            "PCRPOSITIVE2MONTHSB",
            EptsReportUtils.map(
                resumoMensalCcrCohortQueries.getChildrenWithPositivePcrAbove2MonthsOfAge(),
                mappings));

    dsd.addColumn(
        "PCRP2B",
        "crianças com resultados PCR positivo >= 2 meses de idade  – coorte de 9 meses",
        EptsReportUtils.map(pcrpositive2MONTHSB, mappings),
        "");

    return dsd;
  }

  private Mapped<CohortIndicator> getChildrenWithFirstConsultation() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Total de 1as Consultas",
            mapStraightThrough(resumoMensalCcrCohortQueries.getPatients1stConsultation())));
  }

  private Mapped<CohortIndicator> getChildrenWithTbContact() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Crianças com contacto com tuberculose",
            mapStraightThrough(
                resumoMensalCcrCohortQueries.getChildrenWithVisitReason(
                    Collections.singletonList(
                        commonMetadata.getContactoTbConcept().getConceptId())))));
  }

  private Mapped<CohortIndicator> getChildrenWithDam() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Crianças com desnutrição aguda moderada",
            mapStraightThrough(
                resumoMensalCcrCohortQueries.getChildrenWithModerateAcuteMalnutrition())));
  }

  private Mapped<CohortIndicator> getChildrenWithDag() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Crianças com desnutrição aguda grave",
            mapStraightThrough(
                resumoMensalCcrCohortQueries.getChildrenWithSevereAcuteMalnutrition())));
  }

  private Mapped<CohortIndicator> getChildrenWithHivExposure() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Crianças com exposição ao HIV",
            mapStraightThrough(
                resumoMensalCcrCohortQueries.getChildrenWithVisitReason(
                    Collections.singletonList(
                        commonMetadata.getRecenNascidoMaeHivPositivoConcept().getConceptId())))));
  }

  private Mapped<CohortIndicator> getChildrenWithAnotherRiskCondition() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Crianças com outra condição de Risco",
            mapStraightThrough(
                resumoMensalCcrCohortQueries.getChildrenWithVisitReason(
                    Arrays.asList(
                        commonMetadata.getPrematuridadeConcept().getConceptId(),
                        commonMetadata.getCriancaMaeAusenteConcept().getConceptId(),
                        commonMetadata.getTwinsConcept().getConceptId(),
                        commonMetadata.getDesmameBruscoAleitamentoArtificalConcept().getConceptId(),
                        commonMetadata.getMigracaoRecenteFamiliaConcept().getConceptId(),
                        hivMetadata.getOtherOrNonCodedConcept().getConceptId())))));
  }

  private Mapped<CohortIndicator> getChildrenWhoStartedInhOnCcr() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Crianças que iniciaram Isoniazida na CCR",
            mapStraightThrough(resumoMensalCcrCohortQueries.getChildrenWhoStartedINH())));
  }

  private Mapped<CohortIndicator> getChildrenWhoReceivedAtpuonCcr() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Crianças que receberam ATPU",
            mapStraightThrough(
                resumoMensalCcrCohortQueries.getChildrenWhoReceivedNutritionalTreatment())));
  }

  private Mapped<CohortIndicator> getChildrenWhoReceivedCsbOnCcr() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Crianças que receberam CSB/suplemento nutricional",
            mapStraightThrough(
                resumoMensalCcrCohortQueries.getChildrenWhoReceivedCsbOrNutritionalSuplement())));
  }

  private Mapped<CohortIndicator> getChildrenWhoStartedCtzBellow2MonthsOfAge() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Crianças que iniciaram CTZ < 2 meses de idade",
            mapStraightThrough(
                resumoMensalCcrCohortQueries.getChildrenWhoStartedCtzBellow2MonthsOfAge())));
  }

  private Mapped<CohortIndicator> getChildrenWhoStartedCtzAndAbove2MonthsOfAge() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Crianças que iniciaram CTZ >= 2 meses de idade",
            mapStraightThrough(
                resumoMensalCcrCohortQueries.getChildrenWhoStartedCtzAbove2MonthsOfAge())));
  }

  private Mapped<CohortIndicator> getChildrenFirstPcrCollectedUnder2MonthsofAge() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "1º PCR colhido < 2 meses de idade",
            mapStraightThrough(
                resumoMensalCcrCohortQueries.getChildrenFirstPcrCollectedUnder2MonthsofAge())));
  }

  private Mapped<CohortIndicator> getChildrenFirstPcrCollectedAbove2MonthsofAge() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "1º PCR colhido >= 2 meses de idade",
            mapStraightThrough(
                resumoMensalCcrCohortQueries.getChildrenFirstPcrCollectedAbove2MonthsofAge())));
  }

  private Mapped<CohortIndicator> getChildrenAbove9MonthsofAgeExposedAndTested() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Crianças expostas ≥9 meses testadas com Teste Rápido de HIV",
            mapStraightThrough(
                resumoMensalCcrCohortQueries.getExposedChildrenAbove9MonthsofAge())));
  }

  private Mapped<CohortIndicator> getChildrenWithRapidTestAndNotExposedToHiv() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Crianças não expostas ao HIV testadas com Teste Rápido de HIV",
            mapStraightThrough(
                resumoMensalCcrCohortQueries.getChildrenTestedAndNotExposedToHiv())));
  }

  private Mapped<CohortIndicator> getChildrenNotExposedToHivAndWithPositiveTestResult() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Crianças não expostas ao HIV, testadas com Teste Rápido que tiveram resultado positivo",
            mapStraightThrough(
                resumoMensalCcrCohortQueries.getChildrenNotExposedToHivWithPositiveTestResult())));
  }
}
