package org.openmrs.module.eptsreports.reporting.library.datasets;

import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.reporting.data.converter.CalculationResultConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.GenderConverter;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ListOfPatientsDefaultersOrIITCohortQueries;
import org.openmrs.module.reporting.data.DataDefinition;
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
public class ListOfPatientsDefaultersOrIITTemplateDataSet extends BaseDataSet {

  @Autowired
  private ListChildrenOnARTandFormulationsDataset listChildrenOnARTandFormulationsDataset;

  @Autowired TPTListOfPatientsEligibleDataSet tptListOfPatientsEligibleDataSet;

  @Autowired ListOfPatientsDefaultersOrIITCohortQueries listOfPatientsDefaultersOrIITCohortQueries;

  public DataSetDefinition constructDataSet() {
    PatientDataSetDefinition pdd = new PatientDataSetDefinition();

    pdd.setName("FATL");

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

    pdd.addColumn("id", new PersonIdDataDefinition(), "");

    /** 1 - NID - Sheet 1: Column A */
    pdd.addColumn("nid", listChildrenOnARTandFormulationsDataset.getNID(), "");

    /** 2 - Name - Sheet 1: Column B */
    pdd.addColumn("name", nameDef, "");

    /** 3 - ART Start Date - Sheet 1: Column C */
    pdd.addColumn(
        "inicio_tarv",
        listChildrenOnARTandFormulationsDataset.getArtStartDate(),
        "onOrBefore=${endDate},location=${location}",
        new CalculationResultConverter());

    /** 4 - Sex - Sheet 1: Column D */
    pdd.addColumn("gender", new GenderDataDefinition(), "", new GenderConverter());

    /** 5 - Age - Sheet 1: Column */
    pdd.addColumn("age", new AgeDataDefinition(), "", null);

    /** 6 - Pregnancy/Breastfeeding status (Grávida/Lactante) – Sheet 1: Column F */
    pdd.addColumn(
        "pregnant_or_breastfeeding",
        tptListOfPatientsEligibleDataSet.pregnantBreasfeediDefinition(),
        "location=${location}",
        null);

    /** 7 - Patients active on TB Treatment - Sheet 1: Column G */
    pdd.addColumn(
        "tb_treatment",
        listOfPatientsDefaultersOrIITCohortQueries.getPatientsActiveOnTB(),
        "location=${location}",
        null);

    /** 8 -· Consentimento Informado – Sheet 1: Column H */
    pdd.addColumn(
        "patient_informed_consent",
        listOfPatientsDefaultersOrIITCohortQueries.getPatientsMarkedYes(),
        "location=${location}",
        null);

    /** PRINT ‘N’ IF THE PATIENT HAS ONE OF THE FOLLOWING OPTIONS: */
    pdd.addColumn(
        "confidant_informed_consent",
        listOfPatientsDefaultersOrIITCohortQueries.getPatientsMarkedNo(),
        "location=${location}",
        null);

    /** 14 - Last Follow up Consultation Date - Sheet 1: Column N */
    pdd.addColumn(
        "last_consultation_date",
        listChildrenOnARTandFormulationsDataset.getLastFollowupConsultationDate(),
        "endDate=${endDate},location=${location}",
        null);

    /** 15 - Next Follow up Consultation Date - Sheet 1: Column O */
    pdd.addColumn(
        "next_consultation_date",
        listChildrenOnARTandFormulationsDataset.getNextFollowUpConsultationDate(),
        "endDate=${endDate},location=${location}",
        null);

    /** 16 - Last Drug Pick-up Date - Sheet 1: Column P */
    pdd.addColumn(
        "date_of_last_survey_fila",
        listChildrenOnARTandFormulationsDataset.getLastDrugPickupDate(),
        "endDate=${endDate},location=${location}",
        null);

    /** 17 - Last Drug Pick-up Date - Sheet 1: Column Q */
    pdd.addColumn(
        "date_of_last_survey_reception_raised_ARV",
        listOfPatientsDefaultersOrIITCohortQueries.getLastDrugPickUpDate(),
        "endDate=${endDate},location=${location}",
        null);

    /** 18 - Next Drug pick-up Date - Sheet 1: Column R */
    pdd.addColumn(
        ".next_date_survey_fila",
        listChildrenOnARTandFormulationsDataset.getNextDrugPickupDate(),
        "endDate=${endDate},location=${location}",
        null);

    /** 19 - Next Drug pick-up Date - Sheet 1: Column S */
    pdd.addColumn(
        "next_date_survey _reception_raised_ARV",
        listChildrenOnARTandFormulationsDataset.getNextDrugPickupDate(),
        "endDate=${endDate},location=${location}",
        null);

    /** 20 - Days of Delay - Sheet 1: Column T */
    pdd.addColumn(
        "days_of_absence_to_survey",
        listOfPatientsDefaultersOrIITCohortQueries.getNumberOfDaysOfDelay(),
        "endDate=${endDate},location=${location}",
        null);

    return pdd;
  }
}
