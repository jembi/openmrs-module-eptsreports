package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.reporting.data.converter.*;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ListOfPatientsArtCohortCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ListOfPatientsDefaultersOrIITCohortQueries;
//import org.openmrs.module.eptsreports.reporting.library.cohorts.ListOfPatientsWithHighViralLoadCohortQueries;
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
public class ListOfPatientsWithMdcEvaluationCohortDataset extends BaseDataSet {

//  private ListOfPatientsWithHighViralLoadCohortQueries listOfPatientsWithHighViralLoadCohortQueries;

  private ListOfPatientsArtCohortCohortQueries listOfPatientsArtCohortCohortQueries;

  private TPTListOfPatientsEligibleDataSet tptListOfPatientsEligibleDataSet;

  private TPTInitiationDataDefinitionQueries tptInitiationDataDefinitionQueries;

  private ListChildrenOnARTandFormulationsDataset listChildrenOnARTandFormulationsDataset;

  private ListOfPatientsDefaultersOrIITCohortQueries listOfPatientsDefaultersOrIITCohortQueries;

  @Autowired
  public ListOfPatientsWithMdcEvaluationCohortDataset(
//      ListOfPatientsWithHighViralLoadCohortQueries listOfPatientsWithHighViralLoadCohortQueries,
      ListOfPatientsArtCohortCohortQueries listOfPatientsArtCohortCohortQueries,
      TPTListOfPatientsEligibleDataSet tptListOfPatientsEligibleDataSet,
      TPTInitiationDataDefinitionQueries tptInitiationDataDefinitionQueries,
      ListOfPatientsDefaultersOrIITCohortQueries listOfPatientsDefaultersOrIITCohortQueries,
      ListChildrenOnARTandFormulationsDataset listChildrenOnARTandFormulationsDataset) {
//    this.listOfPatientsWithHighViralLoadCohortQueries =
//        listOfPatientsWithHighViralLoadCohortQueries;
    this.listOfPatientsArtCohortCohortQueries = listOfPatientsArtCohortCohortQueries;
    this.tptListOfPatientsEligibleDataSet = tptListOfPatientsEligibleDataSet;
    this.tptInitiationDataDefinitionQueries = tptInitiationDataDefinitionQueries;
    this.listOfPatientsDefaultersOrIITCohortQueries = listOfPatientsDefaultersOrIITCohortQueries;
    this.listChildrenOnARTandFormulationsDataset = listChildrenOnARTandFormulationsDataset;
  }

  public DataSetDefinition contructDataset() {

    PatientDataSetDefinition pdd = new PatientDataSetDefinition();

    pdd.setName("TEST");

    PatientIdentifierType identifierType =
        Context.getPatientService()
            .getPatientIdentifierTypeByUuid("e2b966d0-1d5f-11e0-b929-000c29ad1d07");

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

//    pdd.addRowFilter(
//        listOfPatientsWithHighViralLoadCohortQueries.getPatientsWithUnsuppressedVlResult(),
//        "startDate=${startDate},endDate=${endDate},location=${location}");
//    pdd.addColumn("id", new PersonIdDataDefinition(), "");

    //  SECÇÃO A
    //  INFORMAÇÃO DO PACIENTE

    // A.1 - Nr Sequencial sheet 1 - Column A
    pdd.addColumn(
        "sequencial_nr",
        tptListOfPatientsEligibleDataSet.getNID(identifierType.getPatientIdentifierTypeId()),
        "");

    // A.2 - Coorte - Sheet 1: Column B
    pdd.addColumn("coort", nameDef, "");

    // A.3 - Sexo - Sheet 1: Column C
    pdd.addColumn("gender", new GenderDataDefinition(), "", new GenderConverter());

    // A.4 - Idade - Sheet 1: Column D
    pdd.addColumn(
        "age",
        listOfPatientsArtCohortCohortQueries.getAge(),
        "evaluationDate=${evaluationDate}",
        new NotApplicableIfNullConverter());

    // A.5 - Data do início TARV - Sheet 1: Column E
    pdd.addColumn(
            "art_start",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // A.6 - Elegível ao TPT no início do TARV - Sheet 1: Column F
    pdd.addColumn(
            "tpt_eligible",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // A.7 - Data de início do TPT - Sheet 1: Column G
    pdd.addColumn(
            "tpt_start_date",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // A.8 - Data de registo do resultado do CD4 inicial - Sheet 1: Column H
    pdd.addColumn(
            "cd4_register_date",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // A.9 - Resultado do CD4 inicial - Sheet 1: Column I
    pdd.addColumn(
            "cd4_result",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);


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
