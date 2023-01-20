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
import org.openmrs.module.eptsreports.reporting.library.cohorts.ListOfPatientsWithHighViralLoadCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.TPTInitiationDataDefinitionQueries;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.ConvertedPersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonAttributeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonIdDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsWithHighViralLoadCohortDataset extends BaseDataSet {

  private ListOfPatientsWithHighViralLoadCohortQueries listOfPatientsWithHighViralLoadCohortQueries;

  private ListOfPatientsArtCohortCohortQueries listOfPatientsArtCohortCohortQueries;

  private TPTListOfPatientsEligibleDataSet tptListOfPatientsEligibleDataSet;

  private TPTInitiationDataDefinitionQueries tptInitiationDataDefinitionQueries;

  private ListChildrenOnARTandFormulationsDataset listChildrenOnARTandFormulationsDataset;

  private ListOfPatientsDefaultersOrIITCohortQueries listOfPatientsDefaultersOrIITCohortQueries;

  @Autowired
  public ListOfPatientsWithHighViralLoadCohortDataset(
      ListOfPatientsWithHighViralLoadCohortQueries listOfPatientsWithHighViralLoadCohortQueries,
      ListOfPatientsArtCohortCohortQueries listOfPatientsArtCohortCohortQueries,
      TPTListOfPatientsEligibleDataSet tptListOfPatientsEligibleDataSet,
      TPTInitiationDataDefinitionQueries tptInitiationDataDefinitionQueries,
      ListOfPatientsDefaultersOrIITCohortQueries listOfPatientsDefaultersOrIITCohortQueries,
      ListChildrenOnARTandFormulationsDataset listChildrenOnARTandFormulationsDataset) {
    this.listOfPatientsWithHighViralLoadCohortQueries =
        listOfPatientsWithHighViralLoadCohortQueries;
    this.listOfPatientsArtCohortCohortQueries = listOfPatientsArtCohortCohortQueries;
    this.tptListOfPatientsEligibleDataSet = tptListOfPatientsEligibleDataSet;
    this.tptInitiationDataDefinitionQueries = tptInitiationDataDefinitionQueries;
    this.listOfPatientsDefaultersOrIITCohortQueries = listOfPatientsDefaultersOrIITCohortQueries;
    this.listChildrenOnARTandFormulationsDataset = listChildrenOnARTandFormulationsDataset;
  }

  public DataSetDefinition contructDataset() {

    PatientDataSetDefinition pdd = new PatientDataSetDefinition();

    pdd.setName("HIGHVL");

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
        listOfPatientsArtCohortCohortQueries.getPatientsInitiatedART(),
        "startDate=${startDate},endDate=${endDate},location=${location}");
    pdd.addColumn("id", new PersonIdDataDefinition(), "");
    // 1- NID sheet 1 - Column A
    pdd.addColumn(
        "nid",
        tptListOfPatientsEligibleDataSet.getNID(identifierType.getPatientIdentifierTypeId()),
        "");

    // 2 - Name - Sheet 1: Column B
    pdd.addColumn("name", nameDef, "");

    // 3 - Idade - Sheet 1: Column C
    pdd.addColumn(
        "age",
        listOfPatientsArtCohortCohortQueries.getAge(),
        "evaluationDate=${evaluationDate}",
        new NotApplicableIfNullConverter());

    // 4 - Sexo - Sheet 1: Column D
    pdd.addColumn("gender", new GenderDataDefinition(), "", new GenderConverter());

    // 5 Contacto – Sheet 1: Column E */
    pdd.addColumn("contact", conctactDef, "", null);

    // 6 Contacto – Sheet 1: Column F */
    pdd.addColumn("reference", referenceConctactDef, "", null);

    // 7 Address (Localidade) – Sheet 1: Column G */
    pdd.addColumn(
        "location",
        listOfPatientsDefaultersOrIITCohortQueries.getLocation(),
        "location=${location}",
        null);

    // 8 Address (Bairro) – Sheet 1: Column H */
    pdd.addColumn(
        "neighborhood",
        listOfPatientsDefaultersOrIITCohortQueries.getNeighborhood(),
        "location=${location}",
        null);

    // 9 Address (Célula) – Sheet 1: Column I */
    pdd.addColumn("cell", listOfPatientsWithHighViralLoadCohortQueries.getPatientCell(), "", null);

    // 10 - Data Inicio Tarv - Sheet 1: Column J
    pdd.addColumn(
        "inicio_tarv",
        tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
        "startDate=${startDate},endDate=${endDate},location=${location}",
        null);

    // 11 - Pregnant/Breastfeeding: - Sheet 1: Column K
    pdd.addColumn(
        "pregnant_breastfeeding",
        tptListOfPatientsEligibleDataSet.pregnantBreasfeediDefinition(),
        "location=${location}",
        null);

    // 12 - Patients active on TB Treatment - Sheet 1: Column L
    pdd.addColumn(
        "ontbtreatment",
        listChildrenOnARTandFormulationsDataset.getPatientsActiveOnTB(),
        "endDate=${generationDate},location=${location}",
        new StoYesAndNtoNoConverter());

    // 13 - Date of Sample collection - Sheet 1: Column M
    pdd.addColumn(
        "sample_collection_date",
        listOfPatientsWithHighViralLoadCohortQueries.getVLSampleCollectionDate(),
        "startDate=${startDate},endDate=${endDate},location=${location}",
        null);

    // 14 - Date of the VL Result reception - Sheet 1: Column N
    pdd.addColumn(
        "result_reception_date",
        listOfPatientsWithHighViralLoadCohortQueries.getVLResultReceptionDate(true),
        "startDate=${startDate},endDate=${endDate},location=${location}",
        null);

    // 15 - The first VL Result > 1000 - Sheet 1: Column O
    pdd.addColumn(
        "vl_result",
        listOfPatientsWithHighViralLoadCohortQueries.getVLResultReceptionDate(false),
        "startDate=${startDate},endDate=${endDate},location=${location}",
        null);

    // 16 - ART Line on High VL Result Date - Sheet 1: Column P
    pdd.addColumn(
        "art_line",
        listOfPatientsWithHighViralLoadCohortQueries.getArtLineOnHighVLResultDate(),
        "startDate=${startDate},endDate=${endDate},location=${location}",
        null);

    // 17 - The first Clinical Consultation Date - Sheet 1: Column Q
    pdd.addColumn(
        "first_consultation",
        listOfPatientsWithHighViralLoadCohortQueries
            .getFirstRegisteredClinicalOrApssConsultationAfterHighVlResultDate(true),
        "startDate=${startDate},endDate=${endDate},location=${location}",
        new NotApplicableIfNullConverter());

    // 18 - The expected Clinical Consultation date - Sheet 1: Column R - The Expected APSS/PP
    // Session 0 Consultation Date - Sheet 1: Column T
    pdd.addColumn(
        "expected_clinical_apss_consultation_date",
        listOfPatientsWithHighViralLoadCohortQueries.getExpectedClinicalOrApssConsultationDate(),
        "startDate=${startDate},endDate=${endDate},location=${location}",
        new NotApplicableIfNullConverter());

    // 19 - The date of first APSS/PP Consultation - Sheet 1: Column S
    pdd.addColumn(
        "first_apss_consultation",
        listOfPatientsWithHighViralLoadCohortQueries
            .getFirstRegisteredClinicalOrApssConsultationAfterHighVlResultDate(false),
        "startDate=${startDate},endDate=${endDate},location=${location}",
        new NotApplicableIfNullConverter());

    // 20 - The date of the first APSS/PP Consultation after Session Zero - Sheet 1: Column U
    pdd.addColumn(
        "apss_session_one",
        listOfPatientsWithHighViralLoadCohortQueries
            .getFirstRegisteredApssAfterApssSessionZeroConsultationDate(),
        "startDate=${startDate},endDate=${endDate},location=${location}",
        new NotApplicableIfNullConverter());

    // 21 - The Expected 1st APSS/PP Consultation Date - Sheet 1: Column V
    pdd.addColumn(
        "apss_expected_date_session_one",
        listOfPatientsWithHighViralLoadCohortQueries.getExpectedApssSessionOneConsultationDate(),
        "startDate=${startDate},endDate=${endDate},location=${location}",
        new NotApplicableIfNullConverter());

    // 22 - The date of the first APSS/PP Consultation after Session One - Sheet 1: Column W
    pdd.addColumn(
        "apss_session_two",
        listOfPatientsWithHighViralLoadCohortQueries
            .getFirstRegisteredApssAfterApssSessionOneConsultationDate(),
        "startDate=${startDate},endDate=${endDate},location=${location}",
        new NotApplicableIfNullConverter());

    // 23 - Expected 2nd APSS/PP Consultation Date - Sheet 1: Column X
    pdd.addColumn(
        "apss_expected_date_session_two",
        listOfPatientsWithHighViralLoadCohortQueries.getExpectedApssSessionTwoConsultationDate(),
        "startDate=${startDate},endDate=${endDate},location=${location}",
        new NotApplicableIfNullConverter());

    // 24 - The date of the first APSS/PP Consultation after Session Two - Sheet 1: Column Y
    pdd.addColumn(
        "apss_session_three",
        listOfPatientsWithHighViralLoadCohortQueries
            .getFirstRegisteredApssAfterApssSessionOTwoConsultationDate(),
        "startDate=${startDate},endDate=${endDate},location=${location}",
        new NotApplicableIfNullConverter());

    // 25 - Expected 3rd APSS/PP Consultation Date - Sheet 1: Column Z
    pdd.addColumn(
        "apss_expected_date_session_three",
        listOfPatientsWithHighViralLoadCohortQueries.getExpectedApssSessionThreeConsultationDate(),
        "startDate=${startDate},endDate=${endDate},location=${location}",
        new NotApplicableIfNullConverter());

    // 26 - The Adherence Evaluation Sheet 1: Column AA
    pdd.addColumn(
            "adherence_evaluation",
            listOfPatientsWithHighViralLoadCohortQueries.getAdherenceEvaluation(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null
    ); // add converter

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
