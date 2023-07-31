package org.openmrs.module.eptsreports.reporting.library.datasets;

import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.reporting.data.converter.ForwardSlashDateConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.GenderConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.NotApplicableIfNullConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.ObservationToConceptNameConverter;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ListOfPatientsArtCohortCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.advancedhivillness.ListOfPatientsInAdvancedHivIllnessCohortQueries;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.BirthdateConverter;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.person.definition.*;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsInAdvancedHivIllnessDataset extends BaseDataSet {

  private final ListOfPatientsArtCohortCohortQueries listOfPatientsArtCohortCohortQueries;

  private final TPTListOfPatientsEligibleDataSet tptListOfPatientsEligibleDataSet;
  private final ListOfPatientsInAdvancedHivIllnessCohortQueries
      listOfPatientsInAdvancedHivIllnessCohortQueries;

  String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";

  @Autowired
  public ListOfPatientsInAdvancedHivIllnessDataset(
      ListOfPatientsArtCohortCohortQueries listOfPatientsArtCohortCohortQueries,
      TPTListOfPatientsEligibleDataSet tptListOfPatientsEligibleDataSet,
      ListOfPatientsInAdvancedHivIllnessCohortQueries
          listOfPatientsInAdvancedHivIllnessCohortQueries) {
    this.listOfPatientsArtCohortCohortQueries = listOfPatientsArtCohortCohortQueries;
    this.tptListOfPatientsEligibleDataSet = tptListOfPatientsEligibleDataSet;
    this.listOfPatientsInAdvancedHivIllnessCohortQueries =
        listOfPatientsInAdvancedHivIllnessCohortQueries;
  }

  public DataSetDefinition constructDataset() {

    PatientDataSetDefinition pdd = new PatientDataSetDefinition();

    pdd.setName("DAH");

    PatientIdentifierType identifierType =
        Context.getPatientService()
            .getPatientIdentifierTypeByUuid("e2b966d0-1d5f-11e0-b929-000c29ad1d07");

    PersonAttributeType contactAttributeType =
        Context.getPersonService()
            .getPersonAttributeTypeByUuid("e2e3fd64-1d5f-11e0-b929-000c29ad1d07");

    DataDefinition contactDef =
        new ConvertedPersonDataDefinition(
            "contact",
            new PersonAttributeDataDefinition(contactAttributeType.getName(), contactAttributeType),
            null);

    DataConverter formatter = new ObjectFormatter("{familyName}, {givenName}");

    DataDefinition nameDef =
        new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), formatter);

    pdd.setParameters(getParameters());

    pdd.addRowFilter(
        listOfPatientsInAdvancedHivIllnessCohortQueries
            .getPatientsOnFollowupOrWithCriteriaToStartFollowupOfDAH(),
        mappings);

    // 1- NID sheet 1 - Column A
    pdd.addColumn(
        "nid",
        tptListOfPatientsEligibleDataSet.getNID(identifierType.getPatientIdentifierTypeId()),
        "");

    // 2 - Name - Sheet 1: Column B
    pdd.addColumn("name", nameDef, "");

    // 3 - Data Nascimento - Sheet 1: Column C
    pdd.addColumn(
        "birthdate", new BirthdateDataDefinition(), "", new BirthdateConverter("dd/MM/yyyy"));

    // 4 - Idade - Sheet 1: Column D
    pdd.addColumn(
        "age",
        listOfPatientsArtCohortCohortQueries.getAge(),
        "evaluationDate=${endDate}",
        new NotApplicableIfNullConverter());

    // 5 - Sexo - Sheet 1: Column E
    pdd.addColumn("gender", new GenderDataDefinition(), "", new GenderConverter());

    // 6 - Contacto - Sheet 1: Column F
    pdd.addColumn("contact", contactDef, "");

    // 7 - Data Inicio Tarv - Sheet 1: Column G
    pdd.addColumn(
        "art_start",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getARTStartDate(),
        "endDate=${endDate},location=${location}",
        new ForwardSlashDateConverter());

    // 8 - Data de Último Levantamento TARV - Sheet 1: Column H
    pdd.addColumn(
        "last_pickup",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getLastARVPickupDate(),
        mappings,
        new ForwardSlashDateConverter());

    // 9 - Situação TARV no Início do Seguimento de DAH Sheet 1: Column I
    pdd.addColumn(
        "last_situation",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getLastARVSituation(),
        "endDate=${endDate},location=${location}",
        new ObservationToConceptNameConverter());

    // 10 - Data de Início de Seguimento de DAH Sheet 1: Column J
    pdd.addColumn(
        "followup_startdate",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getFollowupStartDateDAH(),
        "endDate=${endDate},location=${location}",
        new ForwardSlashDateConverter());

    // 11 - Data de Registo de CD4 Absoluto – Sheet 1: Column K
    pdd.addColumn(
        "cd4_resultdate",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getCd4ResultDate(),
        mappings,
        new ForwardSlashDateConverter());

    // 12 - Resultado de CD4 – Sheet 1: Column L
    pdd.addColumn(
        "cd4_result", listOfPatientsInAdvancedHivIllnessCohortQueries.getCd4Result(), mappings);

    // 13 - Data de Registo de Estadio – Sheet 1: Column M
    pdd.addColumn(
        "estadio_date",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getDateOfEstadioOnPeriod(),
        mappings,
        new ForwardSlashDateConverter());

    // 14 - Infecções Estadio OMS – Sheet 1: Column N
    pdd.addColumn(
        "estadio_result",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getResultOfEstadioDuringPeriod(),
        mappings,
        new ObservationToConceptNameConverter());

    //     15 - Data de Registo mais recente de Estadio OMS -  Column O
    pdd.addColumn(
        "last_estadio_date",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getDateOfMostRecentEstadioByEndOfPeriod(),
        "endDate=${endDate},location=${location}",
        new ForwardSlashDateConverter());

    // 16 - Estadio OMS no registo mais recente ate o fim do periodo -  Column P
    pdd.addColumn(
        "last_estadio_result",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getResultOfMostRecentEstadioByEndOfPeriod(),
        "endDate=${endDate},location=${location}",
        new ObservationToConceptNameConverter());

    // 17 - Motivo de Mudança de Estadiamento Clínico - 1 – Sheet 1: Column Q
    pdd.addColumn(
        "reason_change_estadio",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getReasonToChangeEstadio1(),
        "endDate=${endDate},location=${location}",
        new ObservationToConceptNameConverter());

    // 18 - Motivo de Mudança de Estadiamento Clínico - 2 – Sheet 1: Column R
    pdd.addColumn(
        "reason_change_estadio2",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getReasonToChangeEstadio2(),
        "endDate=${endDate},location=${location}",
        new ObservationToConceptNameConverter());

    // 19 - Resultado do Último CD4 – Sheet 1: Column S
    pdd.addColumn(
        "last_cd4_result",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getLastCd4Result(),
        mappings);

    // 20 - Data do Último CD4 – Sheet 1: Column T
    pdd.addColumn(
        "last_cd4_resultdate",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getLastCd4ResultDate(),
        mappings,
        new ForwardSlashDateConverter());

    // 21 - Resultado do Penúltimo CD4 – Sheet 1: Column U
    pdd.addColumn(
        "second_cd4_result", listOfPatientsInAdvancedHivIllnessCohortQueries.getLastCd4ResultBeforeMostRecentCd4(), mappings);

    // 22 - Data do Penúltimo CD4 – Sheet 1: Column V
    pdd.addColumn(
        "second_cd4_resultdate",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getLastCd4ResultDateBeforeMostRecentCd4(),
        mappings,
        new ForwardSlashDateConverter());

    return pdd;
  }
}
