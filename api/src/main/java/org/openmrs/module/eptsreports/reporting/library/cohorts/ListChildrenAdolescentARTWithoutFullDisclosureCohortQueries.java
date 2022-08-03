package org.openmrs.module.eptsreports.reporting.library.cohorts;

import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.library.queries.CommonQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ListChildrenAdolescentARTWithoutFullDisclosureCohortQueries {

  private final GenericCohortQueries genericCohortQueries;
  private final AgeCohortQueries ageCohortQueries;
  private final CommonQueries commonQueries;

  @Autowired
  public ListChildrenAdolescentARTWithoutFullDisclosureCohortQueries(
      GenericCohortQueries genericCohortQueries,
      AgeCohortQueries ageCohortQueries,
      CommonQueries commonQueries) {
    this.genericCohortQueries = genericCohortQueries;
    this.ageCohortQueries = ageCohortQueries;
    this.commonQueries = commonQueries;
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
    cd.setCompositionString("base AND age AND art");
    return cd;
  }

  public CohortDefinition getPatientsOnART() {
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
}
