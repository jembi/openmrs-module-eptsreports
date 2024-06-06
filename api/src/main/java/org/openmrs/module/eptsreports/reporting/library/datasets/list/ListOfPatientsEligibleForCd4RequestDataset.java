package org.openmrs.module.eptsreports.reporting.library.datasets.list;

import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.reporting.data.converter.EmptyToNaoAndAnyToSimConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.GenderConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.NotApplicableIfNullConverter;
import org.openmrs.module.eptsreports.reporting.library.cohorts.advancedhivillness.ListOfPatientsInAdvancedHivIllnessCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.cd4request.ListOfPatientsEligibleForCd4RequestCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.cd4request.ListOfPatientsEligibleForCd4RequestDataDefinitionQueries;
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
public class ListOfPatientsEligibleForCd4RequestDataset extends BaseDataSet {

  String MAPPING =
      "startDate=${startDate},endDate=${endDate},generationDate=${generationDate},location=${location}";
  String MAPPING2 = "startDate=${startDate},endDate=${endDate},location=${location}";

  private final ListOfPatientsInAdvancedHivIllnessCohortQueries
      listOfPatientsInAdvancedHivIllnessCohortQueries;

  private final ListOfPatientsEligibleForCd4RequestCohortQueries
      listOfPatientsEligibleForCd4RequestCohortQueries;
  private final ListOfPatientsEligibleForCd4RequestDataDefinitionQueries
      listOfPatientsEligibleForCd4RequestDataDefinitionQueries;

  @Autowired
  public ListOfPatientsEligibleForCd4RequestDataset(
      ListOfPatientsInAdvancedHivIllnessCohortQueries
          listOfPatientsInAdvancedHivIllnessCohortQueries,
      ListOfPatientsEligibleForCd4RequestCohortQueries
          listOfPatientsEligibleForCd4RequestCohortQueries,
      ListOfPatientsEligibleForCd4RequestDataDefinitionQueries
          listOfPatientsEligibleForCd4RequestDataDefinitionQueries) {
    this.listOfPatientsInAdvancedHivIllnessCohortQueries =
        listOfPatientsInAdvancedHivIllnessCohortQueries;
    this.listOfPatientsEligibleForCd4RequestCohortQueries =
        listOfPatientsEligibleForCd4RequestCohortQueries;
    this.listOfPatientsEligibleForCd4RequestDataDefinitionQueries =
        listOfPatientsEligibleForCd4RequestDataDefinitionQueries;
  }

  public DataSetDefinition listOfPatientsEligibleForCd4RequestColumnsDataset() {
    PatientDataSetDefinition patientDataSetDefinition = new PatientDataSetDefinition();

    patientDataSetDefinition.setName("Eligible for CD4 Request Columns DataSet");
    patientDataSetDefinition.setParameters(getParameters());

    DataConverter nameFormatter = new ObjectFormatter("{givenName} {middleName} {familyName} ");

    DataDefinition nameDefinition =
        new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);

    PatientIdentifierType identifierType =
        Context.getPatientService()
            .getPatientIdentifierTypeByUuid("e2b966d0-1d5f-11e0-b929-000c29ad1d07");

    patientDataSetDefinition.addRowFilter(
        listOfPatientsEligibleForCd4RequestCohortQueries
            .getPatientsEligibleForCd4RequestComposition(),
        MAPPING);

    patientDataSetDefinition.addColumn("id", new PersonIdDataDefinition(), "");

    // 1- NID sheet 1 - Column A
    patientDataSetDefinition.addColumn(
        "nid",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getNID(
            identifierType.getPatientIdentifierTypeId()),
        "");

    // 2 - Name - Sheet 1: Column B
    patientDataSetDefinition.addColumn("name", nameDefinition, "");

    // 3 - Data Nascimento - Sheet 1: Column C
    patientDataSetDefinition.addColumn(
        "age",
        listOfPatientsEligibleForCd4RequestDataDefinitionQueries.getPatientAgeInYearsOrMonths(),
        "endDate=${endDate}",
        new NotApplicableIfNullConverter());

    // 4 - Sexo - Sheet 1: Column D
    patientDataSetDefinition.addColumn(
        "gender", new GenderDataDefinition(), "", new GenderConverter());

    // 5 - Data Início TARV - Sheet 1: Column E
    patientDataSetDefinition.addColumn(
        "art_start",
        listOfPatientsEligibleForCd4RequestDataDefinitionQueries.getArtStartDate(),
        "endDate=${endDate},location=${location}");

    //  6  - Transferido de Outra US- Sheet 1: Column F
    patientDataSetDefinition.addColumn(
        "transferred_in",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getPatientsTransferredInByTheEndOfPeriod(),
        "endDate=${endDate},location=${location}",
        new EmptyToNaoAndAnyToSimConverter());

    // 7 - Grávida / Lactante – Sheet 1: Column G
    patientDataSetDefinition.addColumn(
        "pregnancy",
        listOfPatientsEligibleForCd4RequestDataDefinitionQueries
            .getPregnantOrBreastfeedingFemalePatients(),
        MAPPING2,
        new NotApplicableIfNullConverter());

    // 8 - Data da Última Consulta Clínica – Sheet 1: Column H
    patientDataSetDefinition.addColumn(
        "last_consultation",
        listOfPatientsEligibleForCd4RequestDataDefinitionQueries.getLastClinicalConsultationDate(),
        "endDate=${endDate},location=${location}",
        new NotApplicableIfNullConverter());

    // 9 - Data da Próxima Consulta Clínica Agendada – Sheet 1: Column I
    patientDataSetDefinition.addColumn(
        "next_consultation",
        listOfPatientsEligibleForCd4RequestDataDefinitionQueries
            .getNextConsultationDateOnLastClinicalConsultationDate(),
        "endDate=${endDate},location=${location}",
        new NotApplicableIfNullConverter());

    return patientDataSetDefinition;
  }

  public DataSetDefinition listOfPatientsEligibleForCd4RequestTotalsDataset() {

    CohortIndicatorDataSetDefinition dataSetDefinition = new CohortIndicatorDataSetDefinition();
    dataSetDefinition.setName("Eligible for CD4 Request Totals DataSet");

    dataSetDefinition.addParameters(getParameters());

    return dataSetDefinition;
  }
}
