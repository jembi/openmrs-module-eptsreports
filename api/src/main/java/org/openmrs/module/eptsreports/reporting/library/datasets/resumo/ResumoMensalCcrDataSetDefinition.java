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
                    commonMetadata.getContactoTbConcept()))));
  }

  private Mapped<CohortIndicator> getChildrenWithDam() {
    return mapStraightThrough(
        eptsGeneralIndicator.getIndicator(
            "Crianças com desnutrição aguda moderada",
            mapStraightThrough(
                resumoMensalCcrCohortQueries.getChildrenWithModerateAcuteMalnutrition())));
  }
}
