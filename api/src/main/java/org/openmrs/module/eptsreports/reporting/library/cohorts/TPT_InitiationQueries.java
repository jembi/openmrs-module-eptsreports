package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TPT_InitiationQueries {
  private HivMetadata hivMetadata;

  @Autowired
  public TPT_InitiationQueries(HivMetadata hivMetadata) {
    this.hivMetadata = hivMetadata;
  }

  /**
   * Unique query to get TPT Initiation patients
   *
   * @return CohortDefinition
   */
  public String getTPTInitiationPatients( int clinicalEncounter) {

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", clinicalEncounter);

    String query = "";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);
    return substitutor.replace(query) ;
  }
}
