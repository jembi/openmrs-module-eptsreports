package org.openmrs.module.eptsreports.reporting.library.cohorts;

import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.library.queries.CommonQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class ListChildrenAdolescentARTWithoutFullDisclosureCohortQueries {

  private final GenericCohortQueries genericCohortQueries;
  private final AgeCohortQueries ageCohortQueries;
  private final CommonQueries commonQueries;
  private final HivMetadata hivMetadata;

  @Autowired
  public ListChildrenAdolescentARTWithoutFullDisclosureCohortQueries(
      GenericCohortQueries genericCohortQueries,
      AgeCohortQueries ageCohortQueries,
      CommonQueries commonQueries,
      HivMetadata hivMetadata) {
    this.genericCohortQueries = genericCohortQueries;
    this.ageCohortQueries = ageCohortQueries;
    this.commonQueries = commonQueries;
    this.hivMetadata = hivMetadata;
  }

  public CohortDefinition getBaseCohortForAdolescent() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("List Children Adolescent ART Without Full Disclosure - base cohort");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addSearch(
        "base",
        EptsReportUtils.map(
            genericCohortQueries.getBaseCohort(), "endDate=${endDate},location=${location}"));
    cd.addSearch(
        "age",
        EptsReportUtils.map(
            ageCohortQueries.createXtoYAgeCohort("age", 8, 14), "effectiveDate=${endDate}"));
    cd.addSearch(
        "art", EptsReportUtils.map(getPatientsOnART(), "endDate=${endDate},location=${location}"));
    cd.addSearch(
        "full",
        EptsReportUtils.map(
            getAdolescentsCurrentlyOnArtWithFullDisclosure(),
            "endDate=${endDate},location=${location}"));
    cd.setCompositionString("(base AND age AND art) AND NOT full");
    return cd;
  }

  private CohortDefinition getPatientsOnART() {
    String query = commonQueries.getARTStartDate(true);
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Get patients on ART");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.setQuery(
        "SELECT p.patient_id FROM patient p "
            + " INNER JOIN( "
            + query
            + ") art ON p.patient_id=art.patient_id"
            + " WHERE p.voided=0 AND art.first_pickup IS NOT NULL ");
    return cd;
  }

  private CohortDefinition getAdolescentsCurrentlyOnArtWithFullDisclosure() {
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put(
        "6340",
        hivMetadata.getDisclosureOfHIVDiagnosisToChildrenAdolescentsConcept().getConceptId());
    map.put("6337", hivMetadata.getRevealdConcept().getConceptId());
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Adolescent patients with full disclosure");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    String query =
        "SELECT p.patient_id FROM patient p "
            + " INNER JOIN encounter e ON p.patient_id=e.patient_id "
            + " INNER JOIN obs o ON e.encounter_id=o.encounter_id "
            + " WHERE p.voided=0 AND e.voided=0 AND o.voided=0 AND e.encounter_type = ${53} "
            + " AND o.concept_id=${6340} AND o.value_coded= ${6337} AND e.encounter_datetime <= :endDate "
            + " AND e.location_id=:location ";

    StringSubstitutor sb = new StringSubstitutor(map);
    String replacedQuery = sb.replace(query);
    cd.setQuery(replacedQuery);
    return cd;
  }
}
