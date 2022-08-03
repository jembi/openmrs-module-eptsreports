package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.data.converter.GenderConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.NotApplicableIfNullConverter;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ListChildrenAdolescentARTWithoutFullDisclosureCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ListOfPatientsArtCohortCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.queries.CommonQueries;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.data.person.definition.ConvertedPersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonIdDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListChildrenAdolescentARTWithoutFullDisclosureDataset extends BaseDataSet {

  private final ListOfPatientsArtCohortCohortQueries listOfPatientsArtCohortCohortQueries;
  private final CommonQueries commonQueries;
  private final ListChildrenAdolescentARTWithoutFullDisclosureCohortQueries
      listChildrenAdolescentARTWithoutFullDisclosureCohortQueries;
  private final HivMetadata hivMetadata;

  @Autowired
  public ListChildrenAdolescentARTWithoutFullDisclosureDataset(
      ListOfPatientsArtCohortCohortQueries listOfPatientsArtCohortCohortQueries,
      CommonQueries commonQueries,
      ListChildrenAdolescentARTWithoutFullDisclosureCohortQueries
          listChildrenAdolescentARTWithoutFullDisclosureCohortQueries,
      HivMetadata hivMetadata) {
    this.listOfPatientsArtCohortCohortQueries = listOfPatientsArtCohortCohortQueries;
    this.commonQueries = commonQueries;
    this.listChildrenAdolescentARTWithoutFullDisclosureCohortQueries =
        listChildrenAdolescentARTWithoutFullDisclosureCohortQueries;
    this.hivMetadata = hivMetadata;
  }

  public DataSetDefinition constructListChildrenAdolescentARTWithoutFullDisclosureDataset() {

    PatientDataSetDefinition pdsd = new PatientDataSetDefinition();
    pdsd.setName("LCA");
    pdsd.setParameters(getParameters());
    PatientIdentifierType identifierType =
        Context.getPatientService()
            .getPatientIdentifierTypeByUuid("e2b966d0-1d5f-11e0-b929-000c29ad1d07");

    DataConverter formatter = new ObjectFormatter("{familyName}, {givenName}");
    DataDefinition nameDef =
        new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), formatter);
    pdsd.addRowFilter(
        listChildrenAdolescentARTWithoutFullDisclosureCohortQueries
            .getAdolescentsCurrentlyOnArtWithDisclosures(
                hivMetadata.getRevealdConcept().getConceptId()),
        "endDate=${endDate},location=${location}");

    pdsd.addColumn("patient_id", new PersonIdDataDefinition(), "");

    pdsd.addColumn("nid", getNID(identifierType.getPatientIdentifierTypeId()), "");

    pdsd.addColumn("name", nameDef, "");

    pdsd.addColumn("sex", new GenderDataDefinition(), "", new GenderConverter());
    pdsd.addColumn(
        "age",
        listOfPatientsArtCohortCohortQueries.getAge(),
        "evaluationDate=${endDate}",
        new NotApplicableIfNullConverter());
    pdsd.addColumn("art", getArtStartDate(), "endDate=${endDate},location=${location}");

    return pdsd;
  }

  public DataDefinition getNID(int identifierType) {
    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();
    spdd.setName("NID");

    Map<String, Integer> valuesMap = new HashMap<>();

    String sql =
        " SELECT p.patient_id,pi.identifier  FROM patient p INNER JOIN patient_identifier pi ON p.patient_id=pi.patient_id "
            + " INNER JOIN patient_identifier_type pit ON pit.patient_identifier_type_id=pi.identifier_type "
            + " WHERE p.voided=0 AND pi.voided=0 AND pit.retired=0 AND pit.patient_identifier_type_id ="
            + identifierType;

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(sql));
    return spdd;
  }

  public DataDefinition getArtStartDate() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Get ART Start Date");
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));

    sqlPatientDataDefinition.setQuery(commonQueries.getARTStartDate(true));

    return sqlPatientDataDefinition;
  }
}
