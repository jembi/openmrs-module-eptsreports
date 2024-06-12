package org.openmrs.module.eptsreports.reporting.library.datasets.list;

import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.reporting.data.converter.GenderConverter;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ListOfPatientsDefaultersOrIITCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.advancedhivillness.ListOfPatientsInAdvancedHivIllnessCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ccr.ListOfChildrenEnrolledInCCRDataDefinitionQueries;
import org.openmrs.module.eptsreports.reporting.library.datasets.BaseDataSet;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.person.definition.*;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListOfChildrenEnrolledInCCRDataset extends BaseDataSet {

  private final ListOfChildrenEnrolledInCCRDataDefinitionQueries
      listOfChildrenEnrolledInCCRDataDefinitionQueries;

  private final ListOfPatientsInAdvancedHivIllnessCohortQueries
      listOfPatientsInAdvancedHivIllnessCohortQueries;
  private final ListOfPatientsDefaultersOrIITCohortQueries
      listOfPatientsDefaultersOrIITCohortQueries;

  @Autowired
  public ListOfChildrenEnrolledInCCRDataset(
      ListOfChildrenEnrolledInCCRDataDefinitionQueries
          listOfChildrenEnrolledInCCRDataDefinitionQueries,
      ListOfPatientsInAdvancedHivIllnessCohortQueries
          listOfPatientsInAdvancedHivIllnessCohortQueries,
      ListOfPatientsDefaultersOrIITCohortQueries listOfPatientsDefaultersOrIITCohortQueries) {
    this.listOfChildrenEnrolledInCCRDataDefinitionQueries =
        listOfChildrenEnrolledInCCRDataDefinitionQueries;
    this.listOfPatientsInAdvancedHivIllnessCohortQueries =
        listOfPatientsInAdvancedHivIllnessCohortQueries;
    this.listOfPatientsDefaultersOrIITCohortQueries = listOfPatientsDefaultersOrIITCohortQueries;
  }

  public DataSetDefinition listOfChildrenEnrolledInCCRColumnsDataset() {
    PatientDataSetDefinition patientDataSetDefinition = new PatientDataSetDefinition();

    patientDataSetDefinition.setName("List of Children Enrolled in CCR Columns DataSet");
    patientDataSetDefinition.setParameters(getParameters());

    PatientIdentifierType identifierType =
        Context.getPatientService()
            .getPatientIdentifierTypeByUuid("e2b966d0-1d5f-11e0-b929-000c29ad1d07");

    PersonAttributeType contactAttributeType =
        Context.getPersonService()
            .getPersonAttributeTypeByUuid("e2e3fd64-1d5f-11e0-b929-000c29ad1d07");

    DataDefinition conctactDef =
        new ConvertedPersonDataDefinition(
            "contact",
            new PersonAttributeDataDefinition(
                contactAttributeType.getName(), contactAttributeType));

    DataConverter nameFormatter = new ObjectFormatter("{givenName} {middleName} {familyName} ");

    DataDefinition nameDefinition =
        new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);

    patientDataSetDefinition.addRowFilter(
        listOfChildrenEnrolledInCCRDataDefinitionQueries.getListOfChildrenEnrolledInCCR(),
        "startDate=${startDate},endDate=${endDate},location=${location}");

    patientDataSetDefinition.addColumn("id", new PersonIdDataDefinition(), "");

    // NID sheet 1 - Column A
    patientDataSetDefinition.addColumn(
        "nid",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getNID(
            identifierType.getPatientIdentifierTypeId()),
        "");

    // Child Name - Sheet 1: Column B
    patientDataSetDefinition.addColumn("name", nameDefinition, "");

    // Sex -Sheet 1: Column C
    patientDataSetDefinition.addColumn(
        "gender", new GenderDataDefinition(), "", new GenderConverter());

    //    Age (Idade) – Sheet 1: Column D - In years
    patientDataSetDefinition.addColumn(
        "age_years",
        listOfChildrenEnrolledInCCRDataDefinitionQueries.getPatientAgeInYearsOrMonths(true),
        "endDate=${endDate}");

    //    Age (Idade) – Sheet 1: Column E - In months
    patientDataSetDefinition.addColumn(
        "age_months",
        listOfChildrenEnrolledInCCRDataDefinitionQueries.getPatientAgeInYearsOrMonths(false),
        "endDate=${endDate}");

    // Address (Localidade) - Sheet 1: Column F
    patientDataSetDefinition.addColumn(
        "location",
        listOfPatientsDefaultersOrIITCohortQueries.getLocation(),
        "location=${location}");

    // Address (Bairro) – Sheet 1: Column G
    patientDataSetDefinition.addColumn(
        "neighborhood",
        listOfPatientsDefaultersOrIITCohortQueries.getNeighborhood(),
        "location=${location}");

    // Address (Ponto de Referencia) – Sheet 1: Column H
    patientDataSetDefinition.addColumn(
        "reference_point",
        listOfPatientsDefaultersOrIITCohortQueries.getReferencePoint(),
        "location=${location}");

    //    Contacto – Sheet 1: Column I
    patientDataSetDefinition.addColumn("contact", conctactDef, "");

    return patientDataSetDefinition;
  }

  public DataSetDefinition listOfChildrenEnrolledInCCRTotalsDataset() {

    CohortIndicatorDataSetDefinition dataSetDefinition = new CohortIndicatorDataSetDefinition();
    dataSetDefinition.setName("List of Children Enrolled in CCR Totals DataSet");

    dataSetDefinition.addParameters(getParameters());

    return dataSetDefinition;
  }
}
