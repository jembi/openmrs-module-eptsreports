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
    String cohort9months = "startDate=${startDate-8m},endDate=${endDate-8},location=${location}";
    String mapping3 =
        "startDate=${startDate-8m},endDate=${endDate-8m},actualEndDate=${endDate}location=${location}";

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
