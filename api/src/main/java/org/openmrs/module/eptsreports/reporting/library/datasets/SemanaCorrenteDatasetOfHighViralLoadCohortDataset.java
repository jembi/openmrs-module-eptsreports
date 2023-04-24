package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.reporting.data.converter.GenderConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.NotApplicableIfNullConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.StoYesAndNtoNoConverter;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ListOfPatientsArtCohortCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ListOfPatientsDefaultersOrIITCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ListOfPatientsEligibleForVLDataDefinitionQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ListOfPatientsWithHighViralLoadCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.TPTInitiationDataDefinitionQueries;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.*;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SemanaCorrenteDatasetOfHighViralLoadCohortDataset extends BaseDataSet {

  private ListOfPatientsWithHighViralLoadCohortQueries listOfPatientsWithHighViralLoadCohortQueries;

  private ListOfPatientsArtCohortCohortQueries listOfPatientsArtCohortCohortQueries;

  private TPTListOfPatientsEligibleDataSet tptListOfPatientsEligibleDataSet;

  private TPTInitiationDataDefinitionQueries tptInitiationDataDefinitionQueries;

  private ListChildrenOnARTandFormulationsDataset listChildrenOnARTandFormulationsDataset;

  private ListOfPatientsDefaultersOrIITCohortQueries listOfPatientsDefaultersOrIITCohortQueries;

  @Autowired
  public SemanaCorrenteDatasetOfHighViralLoadCohortDataset(
      ListOfPatientsWithHighViralLoadCohortQueries listOfPatientsWithHighViralLoadCohortQueries,
      ListOfPatientsArtCohortCohortQueries listOfPatientsArtCohortCohortQueries,
      TPTListOfPatientsEligibleDataSet tptListOfPatientsEligibleDataSet,
      TPTInitiationDataDefinitionQueries tptInitiationDataDefinitionQueries,
      ListChildrenOnARTandFormulationsDataset listChildrenOnARTandFormulationsDataset,
      ListOfPatientsEligibleForVLDataDefinitionQueries
          listOfPatientsEligibleForVLDataDefinitionQueries) {
    this.listOfPatientsWithHighViralLoadCohortQueries =
        listOfPatientsWithHighViralLoadCohortQueries;
    this.listOfPatientsArtCohortCohortQueries = listOfPatientsArtCohortCohortQueries;
    this.tptListOfPatientsEligibleDataSet = tptListOfPatientsEligibleDataSet;
    this.tptInitiationDataDefinitionQueries = tptInitiationDataDefinitionQueries;
    this.listChildrenOnARTandFormulationsDataset = listChildrenOnARTandFormulationsDataset;
  }

  public DataSetDefinition contructDataset() {

    PatientDataSetDefinition pdd = new PatientDataSetDefinition();

    pdd.setName("SMNC");

    PatientIdentifierType identifierType =
        Context.getPatientService()
            .getPatientIdentifierTypeByUuid("e2b966d0-1d5f-11e0-b929-000c29ad1d07");

    PersonAttributeType contactAttributeType =
        Context.getPersonService()
            .getPersonAttributeTypeByUuid("e2e3fd64-1d5f-11e0-b929-000c29ad1d07");

    DataDefinition conctactDef =
        new ConvertedPersonDataDefinition(
            "contact",
            new PersonAttributeDataDefinition(contactAttributeType.getName(), contactAttributeType),
            null);

    PersonAttributeType referencePersonContactAttributeType =
        Context.getPersonService()
            .getPersonAttributeTypeByUuid("4d9d4863-6637-4aba-a907-f2e60f5be9c0");

    DataDefinition referenceConctactDef =
        new ConvertedPersonDataDefinition(
            "reference",
            new PersonAttributeDataDefinition(
                referencePersonContactAttributeType.getName(), referencePersonContactAttributeType),
            null);

    DataConverter identifierFormatter = new ObjectFormatter("{identifier}");

    DataDefinition identifierDef =
        new ConvertedPatientDataDefinition(
            "identifier",
            new PatientIdentifierDataDefinition(identifierType.getName(), identifierType),
            identifierFormatter);

    DataConverter formatter = new ObjectFormatter("{familyName}, {givenName}");

    DataDefinition nameDef =
        new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), formatter);

    pdd.setParameters(getParameters());

    pdd.addRowFilter(
        listOfPatientsWithHighViralLoadCohortQueries.getPatientsWithUnsuppressedVlResult(),
        "startDate=${startDate},endDate=${endDate},location=${location}");
    pdd.addColumn("id", new PersonIdDataDefinition(), "");
    // 1- NID Sheet 2 - Column A
    pdd.addColumn(
        "nid",
        tptListOfPatientsEligibleDataSet.getNID(identifierType.getPatientIdentifierTypeId()),
        "");

    // 2 - Name - Sheet 2: Column B
    pdd.addColumn("name", nameDef, "");

    // 3 - Idade - Sheet 2: Column C
    pdd.addColumn(
        "age",
        listOfPatientsArtCohortCohortQueries.getAge(),
        "evaluationDate=${evaluationDate}",
        new NotApplicableIfNullConverter());

    // 4 - Sexo - Sheet 2: Column D
    pdd.addColumn("gender", new GenderDataDefinition(), "", new GenderConverter());

    // 5 Contacto – Sheet 2: Column E */
    pdd.addColumn("contact", conctactDef, "", null);

    // 6 Contacto – Sheet 2: Column F */
    pdd.addColumn("reference", referenceConctactDef, "", null);

    // 7 Address (Localidade) – Sheet 2: Column G */
    pdd.addColumn(
        "location",
        listOfPatientsDefaultersOrIITCohortQueries.getLocation(),
        "location=${location}",
        null);

    // 8 Address (Bairro) – Sheet 2: Column H */
    pdd.addColumn(
        "neighborhood",
        listOfPatientsDefaultersOrIITCohortQueries.getNeighborhood(),
        "location=${location}",
        null);

    // 9 Address (Célula) – Sheet 2: Column I */
    pdd.addColumn(
        "cell",
        listOfPatientsWithHighViralLoadCohortQueries.getPatientCell(),
        "location=${location}",
        null);

    // 10 - Data Inicio Tarv - Sheet 2: Column J
    pdd.addColumn(
        "inicio_tarv",
        tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
        "startDate=${startDate},endDate=${endDate},location=${location}",
        null);

    // 11 - Pregnant/Breastfeeding: - Sheet 2: Column K
    pdd.addColumn(
        "pregnant_breastfeeding",
        tptListOfPatientsEligibleDataSet.pregnantBreasfeediDefinition(),
        "location=${location}",
        null);

    // 12 - Patients active on TB Treatment - Sheet 2: Column L
    pdd.addColumn(
        "ontbtreatment",
        listChildrenOnARTandFormulationsDataset.getPatientsActiveOnTB(),
        "endDate=${generationDate},location=${location}",
        new StoYesAndNtoNoConverter());

    return pdd;
  }

  @Override
  public List<Parameter> getParameters() {
    return Arrays.asList(
        new Parameter("startDate", "Cohort Start Date", Date.class),
        new Parameter("endDate", "Cohort End Date", Date.class),
        new Parameter("location", "Location", Location.class));
  }
}
