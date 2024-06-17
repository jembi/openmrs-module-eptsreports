package org.openmrs.module.eptsreports.reporting.library.datasets;

import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.data.converter.ConceptNameConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.GenderConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.NotApplicableIfNullConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.StateOfStayArtPatientConverter;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ListOfPatientsDefaultersOrIITCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ListOfPatientsEligibleForVLCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ListOfPatientsEligibleForVLDataDefinitionQueries;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.BirthdateConverter;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.*;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsEligibleForVLDataSet extends BaseDataSet {

  private ListOfPatientsEligibleForVLDataDefinitionQueries
      listOfpatientsEligibleForVLDataDefinitionQueries;

  private ListOfPatientsEligibleForVLCohortQueries listOfPatientsEligibleForVLCohortQueries;

  private ListChildrenOnARTandFormulationsDataset listChildrenOnARTandFormulationsDataset;

  private ListOfPatientsDefaultersOrIITCohortQueries listOfPatientsDefaultersOrIITCohortQueries;

  private HivMetadata hivMetadata;

  private CommonMetadata commonMetadata;

  @Autowired
  public ListOfPatientsEligibleForVLDataSet(
      ListOfPatientsEligibleForVLDataDefinitionQueries
          listOfpatientsEligibleForVLDataDefinitionQueries,
      ListChildrenOnARTandFormulationsDataset listChildrenOnARTandFormulationsDataset,
      ListOfPatientsEligibleForVLCohortQueries listOfPatientsEligibleForVLCohortQueries,
      ListOfPatientsDefaultersOrIITCohortQueries listOfPatientsDefaultersOrIITCohortQueries,
      HivMetadata hivMetadata,
      CommonMetadata commonMetadata) {

    this.listOfpatientsEligibleForVLDataDefinitionQueries =
        listOfpatientsEligibleForVLDataDefinitionQueries;
    this.listChildrenOnARTandFormulationsDataset = listChildrenOnARTandFormulationsDataset;
    this.listOfPatientsEligibleForVLCohortQueries = listOfPatientsEligibleForVLCohortQueries;
    this.listOfPatientsDefaultersOrIITCohortQueries = listOfPatientsDefaultersOrIITCohortQueries;
    this.hivMetadata = hivMetadata;
    this.commonMetadata = commonMetadata;
  }

  public DataSetDefinition constructDataSet() {
    PatientDataSetDefinition pdd = new PatientDataSetDefinition();

    pdd.setName("LPEVL");

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

    pdd.addRowFilter(
        listOfPatientsEligibleForVLCohortQueries.getBaseCohort(),
        "startDate=${startDate},endDate=${endDate},location=${location}");

    PersonAttributeType contactAttributeType =
        Context.getPersonService()
            .getPersonAttributeTypeByUuid("e2e3fd64-1d5f-11e0-b929-000c29ad1d07");
    DataDefinition conctactDef =
        new ConvertedPersonDataDefinition(
            "contact",
            new PersonAttributeDataDefinition(contactAttributeType.getName(), contactAttributeType),
            null);

    pdd.addColumn("nid", listChildrenOnARTandFormulationsDataset.getNID(), "");

    pdd.addColumn("name", nameDef, "");
    pdd.addColumn(
        "birthdate", new BirthdateDataDefinition(), "", new BirthdateConverter("dd/MM/yyyy"));

    pdd.addColumn(
        "age", listChildrenOnARTandFormulationsDataset.getAge(), "endDate=${endDate}", null);
    pdd.addColumn("gender", new GenderDataDefinition(), "", new GenderConverter());
    pdd.addColumn("contact", conctactDef, "");
    pdd.addColumn(
        "art_start_date",
        listOfpatientsEligibleForVLDataDefinitionQueries.getPatientsAndARTStartDate(),
        "endDate=${endDate},location=${location}",
        null);
    pdd.addColumn(
        "last_arv",
        listChildrenOnARTandFormulationsDataset.getLastARVRegimen(),
        "endDate=${endDate},location=${location}",
        new ConceptNameConverter());
    pdd.addColumn(
        "last_linha",
        listOfpatientsEligibleForVLDataDefinitionQueries.getLastTARVLinha(),
        "endDate=${endDate},location=${location}",
        null);

    // Data Último Pedido de Carga Viral - Sheet 1: Column J */
    pdd.addColumn(
        "data_ultimo_pedido_vl",
        listOfpatientsEligibleForVLDataDefinitionQueries.getPatientsLastVLRequestDate(),
        "endDate=${endDate},location=${location}");

    pdd.addColumn(
        "last_vl_date",
        listOfpatientsEligibleForVLDataDefinitionQueries.getPatientsAndMostRecentVLResultDate(),
        "startDate=${startDate},location=${location}",
        null);
    pdd.addColumn(
        "recent_vl",
        listOfpatientsEligibleForVLDataDefinitionQueries.getPatientsAndMostRecentViralLoad(),
        "startDate=${startDate},location=${location}",
        null);

    pdd.addColumn(
        "last_followup",
        listOfpatientsEligibleForVLDataDefinitionQueries
            .getPatientsAndLastFollowUpConsultationDate(),
        "startDate=${startDate},location=${location}",
        null);
    pdd.addColumn(
        "next_consultation_date",
        listOfpatientsEligibleForVLDataDefinitionQueries
            .getPatientsAndNextFollowUpConsultationDate(),
        "startDate=${startDate},location=${location}",
        null);
    pdd.addColumn(
        "last_pickup",
        listOfpatientsEligibleForVLDataDefinitionQueries.getPatientsAndLastDrugPickUpDateOnFila(),
        "startDate=${startDate},location=${location}",
        null);

    pdd.addColumn(
        "next_drug_pickup",
        listOfpatientsEligibleForVLDataDefinitionQueries.getPatientsAndNextDrugPickUpDateOnFila(),
        "startDate=${startDate},location=${location}",
        null);

    pdd.addColumn(
        "last_drug_pickup",
        listOfpatientsEligibleForVLDataDefinitionQueries
            .getPatientsAndLastDrugPickUpDateOnFichaMestre(),
        "startDate=${startDate},location=${location}",
        null);

    pdd.addColumn(
        "next_pickup_mestre",
        listOfpatientsEligibleForVLDataDefinitionQueries
            .getPatientsAndNextpickUpDateOnFichaMestre(),
        "startDate=${startDate},location=${location}",
        null);
    pdd.addColumn(
        "apss_pp",
        listOfpatientsEligibleForVLDataDefinitionQueries
            .getPatientsAndNumberOfAPSSAndPPAfterHadVLGreaterThan1000(),
        "startDate=${startDate},location=${location}",
        null);

    // Data da Consulta mais recente com PopChave informado - Sheet 1: Column T */
    pdd.addColumn(
        "keypop_last_date",
        listOfPatientsDefaultersOrIITCohortQueries.getLastKeyPopulationRegistrationDate(),
        "endDate=${endDate}");

    // HSH - Sheet 1: Column U */
    pdd.addColumn(
        "keypop_hsh",
        listOfPatientsDefaultersOrIITCohortQueries.getLastRegisteredKeyPopulation(
            hivMetadata.getHomosexualConcept()),
        "endDate=${endDate}");

    // PID - Sheet 1: Column V */
    pdd.addColumn(
        "keypop_pid",
        listOfPatientsDefaultersOrIITCohortQueries.getLastRegisteredKeyPopulation(
            hivMetadata.getDrugUseConcept()),
        "endDate=${endDate}");

    // REC - Sheet 1: Column W */
    pdd.addColumn(
        "keypop_rec",
        listOfPatientsDefaultersOrIITCohortQueries.getLastRegisteredKeyPopulation(
            hivMetadata.getImprisonmentConcept()),
        "endDate=${endDate}");

    // MTS - Sheet 1: Column X */
    pdd.addColumn(
        "keypop_mts",
        listOfPatientsDefaultersOrIITCohortQueries.getLastRegisteredKeyPopulation(
            hivMetadata.getSexWorkerConcept()),
        "endDate=${endDate}");

    // TG - Sheet 1: Column Y */
    pdd.addColumn(
        "keypop_tg",
        listOfPatientsDefaultersOrIITCohortQueries.getLastRegisteredKeyPopulation(
            hivMetadata.getTransGenderConcept()),
        "endDate=${endDate}");

    // Data de Inscrição no OVC - Sheet 1: Column Z */
    pdd.addColumn(
        "ovc_data_inscricao",
        listOfPatientsDefaultersOrIITCohortQueries.getLastOVCDate(
            commonMetadata.getOVCDataInscricaoPersonAttributeType(), false),
        "endDate=${endDate}",
        new NotApplicableIfNullConverter());

    // Data de Saída no OVC - Sheet 1: Column AA */
    pdd.addColumn(
        "ovc_data_saida",
        listOfPatientsDefaultersOrIITCohortQueries.getLastOVCDate(
            commonMetadata.getOVCDataSaidaPersonAttributeType(), false),
        "endDate=${endDate}",
        new NotApplicableIfNullConverter());

    // Estado do Beneficiário - Sheet 1: Column AB */
    pdd.addColumn(
        "ovc_estado_beneficiario",
        listOfPatientsDefaultersOrIITCohortQueries.getLastOVCDate(
            commonMetadata.getOVCEstadoBeneficiarioPersonAttributeType(), true),
        "endDate=${endDate}",
        new NotApplicableIfNullConverter());

    // Saída de TARV - Sheet 1: Column AC */
    pdd.addColumn(
        "saida_tarv",
        listOfpatientsEligibleForVLDataDefinitionQueries.getARTExitStatus(),
        "startDate=${startDate},endDate=${endDate},location=${location",
        new StateOfStayArtPatientConverter());

    // Data de Saída de TARV - Sheet 1: Column AD */
    pdd.addColumn(
        "data_saida_tarv",
        listOfpatientsEligibleForVLDataDefinitionQueries.getARTExitDate(),
        "endDate=${endDate}");

    // Observações - Sheet 1: Column AE */
    pdd.addColumn("pid", new PersonIdDataDefinition(), "");

    return pdd;
  }
}
