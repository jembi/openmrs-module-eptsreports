package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.openmrs.Location;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.data.converter.*;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ListOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ListOfPatientsDefaultersOrIITCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.PatientesWhoReceivedVlResultsCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.person.definition.*;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsDefaultersOrIITTemplateDataSet extends BaseDataSet {

  private ListChildrenOnARTandFormulationsDataset listChildrenOnARTandFormulationsDataset;

  private TPTListOfPatientsEligibleDataSet tptListOfPatientsEligibleDataSet;

  private ListOfPatientsDefaultersOrIITCohortQueries listOfPatientsDefaultersOrIITCohortQueries;

  private ListOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries
      listOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries;

  private final PatientesWhoReceivedVlResultsCohortQueries
      patientesWhoReceivedVlResultsCohortQueries;

  private HivMetadata hivMetadata;

  private CommonMetadata commonMetadata;
  private EptsGeneralIndicator eptsGeneralIndicator;

  @Autowired
  public ListOfPatientsDefaultersOrIITTemplateDataSet(
      ListChildrenOnARTandFormulationsDataset listChildrenOnARTandFormulationsDataset,
      TPTListOfPatientsEligibleDataSet tptListOfPatientsEligibleDataSet,
      ListOfPatientsDefaultersOrIITCohortQueries listOfPatientsDefaultersOrIITCohortQueries,
      ListOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries
          listOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries,
      PatientesWhoReceivedVlResultsCohortQueries patientesWhoReceivedVlResultsCohortQueries,
      HivMetadata hivMetadata,
      CommonMetadata commonMetadata,
      EptsGeneralIndicator eptsGeneralIndicator) {
    this.listChildrenOnARTandFormulationsDataset = listChildrenOnARTandFormulationsDataset;
    this.tptListOfPatientsEligibleDataSet = tptListOfPatientsEligibleDataSet;
    this.listOfPatientsDefaultersOrIITCohortQueries = listOfPatientsDefaultersOrIITCohortQueries;
    this.listOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries =
        listOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries;
    this.patientesWhoReceivedVlResultsCohortQueries = patientesWhoReceivedVlResultsCohortQueries;
    this.hivMetadata = hivMetadata;
    this.commonMetadata = commonMetadata;
    this.eptsGeneralIndicator = eptsGeneralIndicator;
  }

  public DataSetDefinition listOfPatientsDefaultersOrIITColumnsDataset() {
    PatientDataSetDefinition pdd = new PatientDataSetDefinition();

    pdd.addParameters(getParameters());
    pdd.setName("FATL");

    PersonAttributeType contactAttributeType =
        Context.getPersonService()
            .getPersonAttributeTypeByUuid("e2e3fd64-1d5f-11e0-b929-000c29ad1d07");
    DataDefinition conctactDef =
        new ConvertedPersonDataDefinition(
            "contact",
            new PersonAttributeDataDefinition(contactAttributeType.getName(), contactAttributeType),
            null);
    DataConverter formatter = new ObjectFormatter("{familyName}, {givenName}");

    pdd.addRowFilter(
        listOfPatientsDefaultersOrIITCohortQueries.getBaseCohort(),
        "endDate=${endDate},minDay=${minDay},maxDay=${maxDay},location=${location}");

    pdd.addColumn("counter", new PersonIdDataDefinition(), "", new ObjectCounterConverter());

    pdd.addColumn("id", new PersonIdDataDefinition(), "");

    // 1 - NID - Sheet 1: Column A
    pdd.addColumn("nid", listChildrenOnARTandFormulationsDataset.getNID(), "");

    DataDefinition nameDef =
        new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), formatter);
    pdd.setParameters(getParameters());

    // 2 - Name - Sheet 1: Column B
    pdd.addColumn("name", nameDef, "");

    // 3 - ART Start Date - Sheet 1: Column C
    pdd.addColumn(
        "inicio_tarv",
        patientesWhoReceivedVlResultsCohortQueries.getArtStartDate(),
        "endDate=${endDate},location=${location}",
        new DashDateFormatConverter());

    // 5 - Sex - Sheet 1: Column E
    pdd.addColumn("gender", new GenderDataDefinition(), "", new GenderConverter());

    // 4 - Age - Sheet 1: Column D
    pdd.addColumn(
        "age", listChildrenOnARTandFormulationsDataset.getAge(), "endDate=${endDate}", null);

    // 6 - Pregnancy/Breastfeeding status (Grávida/Lactante) – Sheet 1: Column F
    pdd.addColumn(
        "pregnant_or_breastfeeding",
        tptListOfPatientsEligibleDataSet.pregnantBreasfeediDefinition(),
        "location=${location}",
        null);

    // 7 - Patients active on TB Treatment - Sheet 1: Column G
    pdd.addColumn(
        "tb_treatment",
        listOfPatientsDefaultersOrIITCohortQueries.getPatientsActiveOnTB(),
        "location=${location}",
        null);

    // 8 -· Consentimento Informado do Paciente– Sheet 1: Column H
    pdd.addColumn(
        "patient_informed_consent",
        listOfPatientsDefaultersOrIITCohortQueries.getPatientConsent(),
        "location=${location}",
        new EmptyIfNullConverter());

    // 9 - Consentimento Informado do Confidente - Sheet 1: Column I
    pdd.addColumn(
        "confidant_informed_consent",
        listOfPatientsDefaultersOrIITCohortQueries.getConfidentConsent(),
        "location=${location}");

    // 10 -· Tipo de Dispensa – Sheet 1: Column J
    pdd.addColumn(
        "type_of_dispensation",
        listOfPatientsDefaultersOrIITCohortQueries.getTypeOfDispensation(),
        "endDate=${endDate},location=${location}",
        null);

    // 11 Contacto – Sheet 1: Column K
    pdd.addColumn("contact", conctactDef, "", null);

    // 12 Address (Localidade) – Sheet 1: Column L
    pdd.addColumn(
        "location",
        listOfPatientsDefaultersOrIITCohortQueries.getLocation(),
        "location=${location}",
        null);

    // 13 Address (Bairro) – Sheet 1: Column M
    pdd.addColumn(
        "neighborhood",
        listOfPatientsDefaultersOrIITCohortQueries.getNeighborhood(),
        "location=${location}",
        null);

    // 14 Address (Ponto de Referencia) – Sheet 1: Column N
    pdd.addColumn(
        "reference_point",
        listOfPatientsDefaultersOrIITCohortQueries.getReferencePoint(),
        "location=${location}",
        null);

    // 15 - Last Follow up Consultation Date - Sheet 1: Column O
    pdd.addColumn(
        "last_consultation_date",
        listChildrenOnARTandFormulationsDataset.getLastFollowupConsultationDate(),
        "endDate=${endDate},location=${location}",
        null);

    // 16 - Next Follow up Consultation Date - Sheet 1: Column P
    pdd.addColumn(
        "next_consultation_date",
        listChildrenOnARTandFormulationsDataset.getNextFollowUpConsultationDate(),
        "endDate=${endDate},location=${location}",
        null);

    // 17 - Mães Mentoras (MM) for Ficha clinica – Sheet 1: Columns Q
    pdd.addColumn(
        "menthor_mother_ficha_clinica",
        listOfPatientsDefaultersOrIITCohortQueries.getSupportGroupsOnFichaClinicaOrSeguimento(
            Collections.singletonList(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId()),
            Collections.singletonList(hivMetadata.getMentoringMotherConcept().getConceptId()),
            false),
        "endDate=${endDate},location=${location}",
        new SupportGroupsConverter());

    // 18 - Adolescentes e Jovens Mentores (AJM) for Ficha clinica – Sheet 1: Columns R
    pdd.addColumn(
        "youth_teenage_ficha_clinica",
        listOfPatientsDefaultersOrIITCohortQueries.getSupportGroupsOnFichaClinicaOrSeguimento(
            Collections.singletonList(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId()),
            Collections.singletonList(
                hivMetadata.getYouthAndTeenageMenthorConcept().getConceptId()),
            false),
        "endDate=${endDate},location=${location}",
        new SupportGroupsConverter());

    // 19 - Homem Campeão (HC) for Ficha clinica – Sheet 1: Columns S
    pdd.addColumn(
        "champion_man_ficha_clinica",
        listOfPatientsDefaultersOrIITCohortQueries.getSupportGroupsOnFichaClinicaOrSeguimento(
            Collections.singletonList(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId()),
            Collections.singletonList(hivMetadata.getChampioManConcept().getConceptId()),
            false),
        "endDate=${endDate},location=${location}",
        new SupportGroupsConverter());

    // 20 - Last APSS/PP Consultation Date – Sheet 1: Column T
    pdd.addColumn(
        "last_apss_consultation_date",
        listOfPatientsDefaultersOrIITCohortQueries.getLastApssConsultationDate(),
        "endDate=${endDate},location=${location}",
        null);

    // 21 - Next Scheduled APSS/PP Consultation Date – Sheet 1: Column U
    pdd.addColumn(
        "next_apss_consultation_date",
        listOfPatientsDefaultersOrIITCohortQueries.getNextApssConsultationDate(),
        "endDate=${endDate},location=${location}",
        null);

    // 22 - Mães Mentoras (MM) for APSS – Sheet 1: Columns V
    pdd.addColumn(
        "menthor_mother_apss",
        listOfPatientsDefaultersOrIITCohortQueries.getSupportGroupsOnFichaClinicaOrSeguimento(
            Collections.singletonList(
                hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId()),
            Collections.singletonList(hivMetadata.getMentoringMotherConcept().getConceptId()),
            true),
        "endDate=${endDate},location=${location}",
        new SupportGroupsConverter());

    // 23 - Adolescentes e Jovens Mentores (AJM) for APSS – Sheet 1: Columns W
    pdd.addColumn(
        "youth_teenage_apss",
        listOfPatientsDefaultersOrIITCohortQueries.getSupportGroupsOnFichaClinicaOrSeguimento(
            Collections.singletonList(
                hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId()),
            Collections.singletonList(
                hivMetadata.getYouthAndTeenageMenthorConcept().getConceptId()),
            true),
        "endDate=${endDate},location=${location}",
        new SupportGroupsConverter());

    // 24 - Homem Campeão (HC) for APSS – Sheet 1: Columns X
    pdd.addColumn(
        "champion_man_apss",
        listOfPatientsDefaultersOrIITCohortQueries.getSupportGroupsOnFichaClinicaOrSeguimento(
            Collections.singletonList(
                hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId()),
            Collections.singletonList(hivMetadata.getChampioManConcept().getConceptId()),
            true),
        "endDate=${endDate},location=${location}",
        new SupportGroupsConverter());

    // 25 - Most Recent Date MDS "I" or "C" - Sheet 1: Column Y
    pdd.addColumn(
        "mds_consultation_date",
        listOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries
            .getMostRecentMdcConsultationDate(),
        "location=${location}");

    // 26 - MDS1 - Sheet 1: Column Z
    pdd.addColumn(
        "mds1",
        listOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries.getMdcDispensationType(
            ListOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries.DispensationColumn.MDC1),
        "location=${location}",
        new DispensationTypeMdcConverter());

    // 27 - MDS2 - Sheet 1: Column AA
    pdd.addColumn(
        "mds2",
        listOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries.getMdcDispensationType(
            ListOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries.DispensationColumn.MDC2),
        "location=${location}",
        new DispensationTypeMdcConverter());

    // 28 - MDS3 - Sheet 1: Column AB
    pdd.addColumn(
        "mds3",
        listOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries.getMdcDispensationType(
            ListOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries.DispensationColumn.MDC3),
        "location=${location}",
        new DispensationTypeMdcConverter());

    // 29 - MDS4 - Sheet 1: Column AC
    pdd.addColumn(
        "mds4",
        listOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries.getMdcDispensationType(
            ListOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries.DispensationColumn.MDC4),
        "location=${location}",
        new DispensationTypeMdcConverter());

    // 30 - MDS5 - Sheet 1: Column AD
    pdd.addColumn(
        "mds5",
        listOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries.getMdcDispensationType(
            ListOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries.DispensationColumn.MDC5),
        "location=${location}",
        new DispensationTypeMdcConverter());

    // 31 - Data da consulta mais recente - Sheet 1: Column AE
    pdd.addColumn(
        "mdc_consultation_date",
        listOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries
            .getMostRecentMdcConsultationDate(),
        "location=${location}");

    // 32 - HSH - Sheet 1: Column AF
    pdd.addColumn(
        "keypop_hsh",
        listOfPatientsDefaultersOrIITCohortQueries.getLastRegisteredKeyPopulation(
            hivMetadata.getHomosexualConcept()),
        "endDate=${endDate}");

    // 33 - PID - Sheet 1: Column AG
    pdd.addColumn(
        "keypop_pid",
        listOfPatientsDefaultersOrIITCohortQueries.getLastRegisteredKeyPopulation(
            hivMetadata.getDrugUseConcept()),
        "endDate=${endDate}");

    // 34 - REC - Sheet 1: Column AH
    pdd.addColumn(
        "keypop_rec",
        listOfPatientsDefaultersOrIITCohortQueries.getLastRegisteredKeyPopulation(
            hivMetadata.getImprisonmentConcept()),
        "endDate=${endDate}");

    // 35 - MTS - Sheet 1: Column AI
    pdd.addColumn(
        "keypop_mts",
        listOfPatientsDefaultersOrIITCohortQueries.getLastRegisteredKeyPopulation(
            hivMetadata.getSexWorkerConcept()),
        "endDate=${endDate}");

    // 36 - TG - Sheet 1: Column AJ
    pdd.addColumn(
        "keypop_tg",
        listOfPatientsDefaultersOrIITCohortQueries.getLastRegisteredKeyPopulation(
            hivMetadata.getTransGenderConcept()),
        "endDate=${endDate}");

    // 37 - Data de Inscrição no OVC - Sheet 1: Column AK
    pdd.addColumn(
        "ovc_data_inscricao",
        listOfPatientsDefaultersOrIITCohortQueries.getLastOVCDate(
            commonMetadata.getOVCDataInscricaoPersonAttributeType(), false),
        "endDate=${endDate}",
        new NotApplicableIfNullConverter());

    // 38 - Data de Saída no OVC - Sheet 1: Column AL
    pdd.addColumn(
        "ovc_data_saida",
        listOfPatientsDefaultersOrIITCohortQueries.getLastOVCDate(
            commonMetadata.getOVCDataSaidaPersonAttributeType(), false),
        "endDate=${endDate}",
        new NotApplicableIfNullConverter());

    // 39 - Estado do Beneficiário - Sheet 1: Column AM
    pdd.addColumn(
        "ovc_estado_beneficiario",
        listOfPatientsDefaultersOrIITCohortQueries.getLastOVCDate(
            commonMetadata.getOVCEstadoBeneficiarioPersonAttributeType(), true),
        "endDate=${endDate}",
        new NotApplicableIfNullConverter());

    // 40 - Last Drug Pick-up Date - Sheet 1: Column AN
    pdd.addColumn(
        "date_of_last_survey_fila",
        listChildrenOnARTandFormulationsDataset.getLastDrugPickupDate(),
        "endDate=${endDate},location=${location}",
        null);

    // 41 - Last Drug Pick-up Date - Sheet 1: Column AO
    pdd.addColumn(
        "date_of_last_survey_reception_raised_ARV",
        listOfPatientsDefaultersOrIITCohortQueries.getLastDrugPickUpDate(),
        "endDate=${endDate},location=${location}",
        null);

    // 42 - Next Drug pick-up Date - Sheet 1: Column AP
    pdd.addColumn(
        "next_date_survey_fila",
        listChildrenOnARTandFormulationsDataset.getNextDrugPickupDate(),
        "endDate=${endDate},location=${location}",
        null);

    // 43 - Next Drug pick-up Date - Sheet 1: Column AQ
    pdd.addColumn(
        "next_date_survey _reception_raised_ARV",
        listOfPatientsDefaultersOrIITCohortQueries.getNextDrugPickUpDateARV(),
        "endDate=${endDate},location=${location}",
        null);

    // 44 - Days of Delay - Sheet 1: Column AR
    pdd.addColumn(
        "days_of_absence_to_survey",
        listOfPatientsDefaultersOrIITCohortQueries.getNumberOfDaysOfDelay(),
        "endDate=${endDate},location=${location}",
        null);

    // 45 -Abandono Notificado - Sheet 1: Column AS
    pdd.addColumn(
        "abandono_notificado_date",
        listOfPatientsDefaultersOrIITCohortQueries.getLastAbandonoNotificado(),
        "endDate=${endDate},location=${location}");

    return pdd;
  }

  public DataSetDefinition listOfPatientsDefaultersOrIITTotalsDataset() {

    CohortIndicatorDataSetDefinition dataSetDefinition = new CohortIndicatorDataSetDefinition();
    dataSetDefinition.setName("Total de Pacientes Faltosos ou Abandonos ao TARV");
    dataSetDefinition.addParameters(getParameters());

    CohortIndicator total =
        eptsGeneralIndicator.getIndicator(
            "total",
            Mapped.mapStraightThrough(listOfPatientsDefaultersOrIITCohortQueries.getBaseCohort()));

    total.addParameter(new Parameter("minDay", "Minimum number of days", Integer.class));
    total.addParameter(new Parameter("maxDay", "Maximum number of days", Integer.class));

    dataSetDefinition.addColumn(
        "total",
        "Total de Pacientes Faltosos ou Abandonos ao TARV",
        Mapped.mapStraightThrough(total),
        "");

    return dataSetDefinition;
  }

  @Override
  public List<Parameter> getParameters() {
    return Arrays.asList(
        new Parameter("endDate", "End date", Date.class),
        new Parameter("minDay", "Minimum number of days", Integer.class),
        new Parameter("maxDay", "Maximum number of days", Integer.class),
        new Parameter("location", "Location", Location.class));
  }
}
