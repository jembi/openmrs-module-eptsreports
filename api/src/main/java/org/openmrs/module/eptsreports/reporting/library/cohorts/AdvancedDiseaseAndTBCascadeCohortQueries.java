package org.openmrs.module.eptsreports.reporting.library.cohorts;

import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AdvancedDiseaseAndTBCascadeCohortQueries {

  private HivMetadata hivMetadata;
  private TbMetadata tbMetadata;

  @Autowired
  public AdvancedDiseaseAndTBCascadeCohortQueries(HivMetadata hivMetadata, TbMetadata tbMetadata) {

    this.hivMetadata = hivMetadata;
    this.tbMetadata = tbMetadata;
  }
}
