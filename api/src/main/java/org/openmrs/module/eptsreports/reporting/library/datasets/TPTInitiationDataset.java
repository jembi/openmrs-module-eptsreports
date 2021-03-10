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
package org.openmrs.module.eptsreports.reporting.library.datasets;

import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.library.cohorts.TPT_InitiationQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.TbPrevCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.dimensions.EptsCommonDimension;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TPTInitiationDataset extends BaseDataSet {

  @Autowired private EptsGeneralIndicator eptsGeneralIndicator;

  @Autowired private TbPrevCohortQueries tbPrevCohortQueries;

  @Autowired private TPT_InitiationQueries tptInitiationQueries;

  @Autowired private EptsCommonDimension eptsCommonDimension;

  private HivMetadata hivMetadata;

  @Autowired
  public TPTInitiationDataset(HivMetadata hivMetadata) {
    this.hivMetadata = hivMetadata;
  }


  public DataSetDefinition constructDatset(List<Parameter> parameterList) {

    SqlDataSetDefinition sdd = new SqlDataSetDefinition();

    sdd.setName("TPT Initiation Data Set");
    sdd.addParameters(parameterList);
    sdd.setSqlQuery(tptInitiationQueries.getTPTInitiationPatients(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId()
    ));

    return sdd;
  }
}
