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

import static org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils.map;
import static org.openmrs.module.reporting.evaluation.parameter.Mapped.mapStraightThrough;

import java.util.Arrays;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ResumoMensalCcrCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.datasets.BaseDataSet;
import org.openmrs.module.eptsreports.reporting.library.dimensions.AgeDimensionCohortInterface;
import org.openmrs.module.eptsreports.reporting.library.dimensions.EptsCommonDimension;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ResumoMensalCcrDataSetDefinition extends BaseDataSet {

  private EptsCommonDimension eptsCommonDimension;

  private EptsGeneralIndicator eptsGeneralIndicator;

  private ResumoMensalCcrCohortQueries resumoMensalCcrCohortQueries;

  private CommonMetadata commonMetadata;

  private HivMetadata hivMetadata;

  @Autowired
  @Qualifier("commonAgeDimensionCohort")
  private AgeDimensionCohortInterface ageDimensionCohort;

  @Autowired
  public ResumoMensalCcrDataSetDefinition(
      EptsCommonDimension eptsCommonDimension,
      EptsGeneralIndicator eptsGeneralIndicator,
      ResumoMensalCcrCohortQueries resumoMensalCcrCohortQueries,
      CommonMetadata commonMetadata,
      HivMetadata hivMetadata) {
    this.eptsCommonDimension = eptsCommonDimension;
    this.eptsGeneralIndicator = eptsGeneralIndicator;
    this.resumoMensalCcrCohortQueries = resumoMensalCcrCohortQueries;
    this.commonMetadata = commonMetadata;
    this.hivMetadata = hivMetadata;
  }

  public DataSetDefinition constructResumoMensalDataset() {
    CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
    String mapping = "startDate=${startDate},endDate=${endDate},location=${location}";
    dsd.setName("Resumo Mensal CCR Dataset");
    dsd.addParameters(getParameters());

    dsd.addDimension("gender", map(eptsCommonDimension.gender(), ""));
    dsd.addDimension(
        "age", map(eptsCommonDimension.age(ageDimensionCohort), "effectiveDate=${endDate}"));

    dsd.addColumn("FIRST", "Total de 1as Consultas", getChildrenWithFirstConsultation(), "");

    dsd.addColumn(
        "TUBERCULOSIS", "Crianças com contacto com tuberculose", getChildrenWithTbContact(), "");

    dsd.addColumn("DAM", "Crianças com desnutrição aguda moderada", getChildrenWithDam(), "");

    dsd.addColumn("DAG", "Crianças com desnutrição aguda grave", getChildrenWithDag(), "");

    dsd.addColumn("HIV", "Crianças com exposição ao HIV", getChildrenWithHivExposure(), "");

    dsd.addColumn(
        "OTHER", "Crianças com outra condição de Risco", getChildrenWithAnotherRiskCondition(), "");

    dsd.addColumn(
        "INH", "Crianças que iniciaram Isoniazida na CCR", getChildrenWhoStartedInhOnCcr(), "");

    dsd.addColumn("ATPU", "Crianças que receberam ATPU", getChildrenWhoReceivedAtpuonCcr(), "");

    dsd.addColumn("CSB", "Crianças que receberam CSB/suplemento nutricional", getChildrenWhoReceivedCsbOnCcr(), "");

    dsd.addColumn("CTZ", "Crianças que iniciaram CTZ < 2 meses de idade", getChildrenWhoStartedCtzBellow2MonthsOfAge(), "");

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
                    Arrays.asList(commonMetadata.getContactoTbConcept().getConceptId())))));
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
                    Arrays.asList(
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
}
