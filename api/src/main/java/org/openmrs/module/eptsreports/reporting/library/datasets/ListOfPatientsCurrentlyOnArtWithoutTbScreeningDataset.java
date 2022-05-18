package org.openmrs.module.eptsreports.reporting.library.datasets;

import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.data.converter.DispensationTypeConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.GenderConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.NotApplicableIfNullConverter;
import org.openmrs.module.eptsreports.reporting.library.cohorts.*;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.ConvertedPersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonAttributeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class ListOfPatientsCurrentlyOnArtWithoutTbScreeningDataset extends BaseDataSet {

  @Autowired
  private ListOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries
      listOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries;

  @Autowired private ListOfPatientsArtCohortCohortQueries listOfPatientsArtCohortCohortQueries;

  @Autowired private TPTListOfPatientsEligibleDataSet tptListOfPatientsEligibleDataSet;

  @Autowired
  private ListOfPatientsDefaultersOrIITCohortQueries listOfPatientsDefaultersOrIITCohortQueries;

  @Autowired private TPTInitiationDataDefinitionQueries tptInitiationDataDefinitionQueries;

  @Autowired
  private ListOfPatientsEligibleForVLDataDefinitionQueries
      listOfPatientsEligibleForVLDataDefinitionQueries;

  @Autowired private HivMetadata hivMetadata;

  public DataSetDefinition constructListOfPatientsDataset() {
    PatientDataSetDefinition patientDefinition = new PatientDataSetDefinition();
    patientDefinition.setName("List of patients currently no ART without TB Screening");
    patientDefinition.setParameters(getParameters());

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

    PersonAttributeType contactAttributeType =
        Context.getPersonService()
            .getPersonAttributeTypeByUuid("e2e3fd64-1d5f-11e0-b929-000c29ad1d07");

    patientDefinition.addColumn(
        "nid",
        tptListOfPatientsEligibleDataSet.getNID(identifierType.getPatientIdentifierTypeId()),
        "");

    patientDefinition.addColumn("name", nameDef, "");

    patientDefinition.addColumn("sex", new GenderDataDefinition(), "", new GenderConverter());

    patientDefinition.addColumn(
        "age",
        listOfPatientsArtCohortCohortQueries.getAge(),
        "evaluationDate=${endDate}",
        new NotApplicableIfNullConverter());

    patientDefinition.addColumn(
        "location",
        listOfPatientsDefaultersOrIITCohortQueries.getLocation(),
        "location=${location}",
        null);

    patientDefinition.addColumn(
        "neighborhood",
        listOfPatientsDefaultersOrIITCohortQueries.getNeighborhood(),
        "location=${location}",
        null);

    patientDefinition.addColumn(
        "reference_point",
        listOfPatientsDefaultersOrIITCohortQueries.getReferencePoint(),
        "location=${location}",
        null);

    DataDefinition conctactDef =
        new ConvertedPersonDataDefinition(
            "contact",
            new PersonAttributeDataDefinition(contactAttributeType.getName(), contactAttributeType),
            null);
    patientDefinition.addColumn("contact", conctactDef, "");

    patientDefinition.addColumn(
        "art_start",
        listOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries.getArtStartDate(),
        "endDate=${endDate},location=${location}");

    patientDefinition.addColumn(
        "pregnant_breastfeeding",
        tptListOfPatientsEligibleDataSet.pregnantBreasfeediDefinition(),
        "location=${location}");

    patientDefinition.addColumn(
        "last_pickup_fila",
        listOfPatientsEligibleForVLDataDefinitionQueries.getPatientsAndLastDrugPickUpDateOnFila(),
        "startDate=${endDate},location=${location}");

    patientDefinition.addColumn(
        "next_scheduled_pickup_fila",
        listOfPatientsEligibleForVLDataDefinitionQueries.getPatientsAndNextDrugPickUpDateOnFila(),
        "startDate=${endDate},location=${location}");

    patientDefinition.addColumn(
        "fila_dispensation_mode",
        listOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries.getDispensationTypeOnEncounter(
            hivMetadata.getARVPharmaciaEncounterType()),
        "endDate=${endDate},location=${location}",
        new DispensationTypeConverter());

    patientDefinition.addColumn(
        "last_followup_consultation",
        listOfPatientsEligibleForVLDataDefinitionQueries
            .getPatientsAndLastFollowUpConsultationDate(),
        "startDate=${endDate},location=${location}");

    patientDefinition.addColumn(
        "next_followup_consultation",
        listOfPatientsEligibleForVLDataDefinitionQueries
            .getPatientsAndNextFollowUpConsultationDate(),
        "startDate=${endDate},location=${location}");

    patientDefinition.addColumn(
        "clinical_dispensation_mode",
        listOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries.getDispensationTypeOnEncounter(
            hivMetadata.getAdultoSeguimentoEncounterType()),
        "endDate=${endDate},location=${location}",
        new DispensationTypeConverter());

    patientDefinition.addColumn(
        "recent_arv_pickup",
        listOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries
            .getMostRecentDrugPickupDateOnRecepcaoLevantouArv(),
        "endDate=${endDate},location=${location}");

    patientDefinition.addColumn(
        "scheduled_arv_pickup",
        listOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries
            .getNextScheduledDrugPickupDateOnRecepcaoLevantouArv(),
        "endDate=${endDate},location=${location}");

    return patientDefinition;
  }

  @Override
  public List<Parameter> getParameters() {
    return Arrays.asList(
        new Parameter("endDate", "Report End Date", Date.class),
        new Parameter("location", "Health Facility", Location.class));
  }
}
