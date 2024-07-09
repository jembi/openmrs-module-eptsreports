package org.openmrs.module.eptsreports.reporting.library.datasets.list;

import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.data.converter.DashDateFormatConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.GenderConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.SifNotNullAndNifNullConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.ObservationToConceptNameConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.TestResultConverter;
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

  private final CommonMetadata commonMetadata;

  private final HivMetadata hivMetadata;

  @Autowired
  public ListOfChildrenEnrolledInCCRDataset(
      ListOfChildrenEnrolledInCCRDataDefinitionQueries
          listOfChildrenEnrolledInCCRDataDefinitionQueries,
      ListOfPatientsInAdvancedHivIllnessCohortQueries
          listOfPatientsInAdvancedHivIllnessCohortQueries,
      ListOfPatientsDefaultersOrIITCohortQueries listOfPatientsDefaultersOrIITCohortQueries,
      CommonMetadata commonMetadata,
      HivMetadata hivMetadata) {
    this.listOfChildrenEnrolledInCCRDataDefinitionQueries =
        listOfChildrenEnrolledInCCRDataDefinitionQueries;
    this.listOfPatientsInAdvancedHivIllnessCohortQueries =
        listOfPatientsInAdvancedHivIllnessCohortQueries;
    this.listOfPatientsDefaultersOrIITCohortQueries = listOfPatientsDefaultersOrIITCohortQueries;
    this.commonMetadata = commonMetadata;
    this.hivMetadata = hivMetadata;
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

    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";
    patientDataSetDefinition.addRowFilter(
        listOfChildrenEnrolledInCCRDataDefinitionQueries.getListOfChildrenEnrolledInCCR(),
        mappings);

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

    // CCR Enrollment Date (Data Inscrição na CCR) – Sheet 1: Column J
    patientDataSetDefinition.addColumn(
        "ccr_enrollment_date",
        listOfChildrenEnrolledInCCRDataDefinitionQueries.getCCREnrollmentDate(),
        mappings,
        new DashDateFormatConverter());

    // Prematurity (Prematuridade) – Sheet 1: Column K
    patientDataSetDefinition.addColumn(
        "reason_k",
        listOfChildrenEnrolledInCCRDataDefinitionQueries.getReasonForVisitOnCCREnrollmentDate(
            commonMetadata.getPrematuridadeConcept()),
        mappings,
        new SifNotNullAndNifNullConverter());

    // Birth weight under 2.5 kg (Peso ao nascer inferior a 2,5kg) – Sheet 1: Column L
    patientDataSetDefinition.addColumn(
        "reason_l",
        listOfChildrenEnrolledInCCRDataDefinitionQueries.getReasonForVisitOnCCREnrollmentDate(
            commonMetadata.getPesoInferior2dot5KgConcept()),
        mappings,
        new SifNotNullAndNifNullConverter());

    // Failure to thrive (Crescimento insuficiente) – Sheet 1: Column M
    patientDataSetDefinition.addColumn(
        "reason_m",
        listOfChildrenEnrolledInCCRDataDefinitionQueries.getReasonForVisitOnCCREnrollmentDate(
            commonMetadata.getFalenciaDeCrescimentoConcept()),
        mappings,
        new SifNotNullAndNifNullConverter());

    // Acute Malnutrition (Desnutrição aguda) – Sheet 1: Column N
    patientDataSetDefinition.addColumn(
        "reason_n",
        listOfChildrenEnrolledInCCRDataDefinitionQueries.getReasonForVisitOnCCREnrollmentDate(
            hivMetadata.getChronicMalnutritionConcept()),
        mappings,
        new SifNotNullAndNifNullConverter());

    // HIV exposure (Exposição ao HIV) – Sheet 1: Column O
    patientDataSetDefinition.addColumn(
        "reason_o",
        listOfChildrenEnrolledInCCRDataDefinitionQueries.getReasonForVisitOnCCREnrollmentDate(
            commonMetadata.getRecenNascidoMaeHivPositivoConcept()),
        mappings,
        new SifNotNullAndNifNullConverter());

    // Mother deceased or absent (Mãe falecida/ausente) – Sheet 1: Column P
    patientDataSetDefinition.addColumn(
        "reason_p",
        listOfChildrenEnrolledInCCRDataDefinitionQueries.getReasonForVisitOnCCREnrollmentDate(
            commonMetadata.getCriancaMaeAusenteConcept()),
        mappings,
        new SifNotNullAndNifNullConverter());

    // Contact with Tuberculosis (Contacto com Tuberculose) – Sheet 1: Column Q
    patientDataSetDefinition.addColumn(
        "reason_q",
        listOfChildrenEnrolledInCCRDataDefinitionQueries.getReasonForVisitOnCCREnrollmentDate(
            commonMetadata.getContactoTbConcept()),
        mappings,
        new SifNotNullAndNifNullConverter());

    // Twins (Gémeos) – Sheet 1: Column R
    patientDataSetDefinition.addColumn(
        "reason_r",
        listOfChildrenEnrolledInCCRDataDefinitionQueries.getReasonForVisitOnCCREnrollmentDate(
            commonMetadata.getTwinsConcept()),
        mappings,
        new SifNotNullAndNifNullConverter());

    // Formula feeding or abrupt weaning (Leite artificial ou desmame brusco) – Sheet 1: Column S
    patientDataSetDefinition.addColumn(
        "reason_s",
        listOfChildrenEnrolledInCCRDataDefinitionQueries.getReasonForVisitOnCCREnrollmentDate(
            commonMetadata.getDesmameBruscoAleitamentoArtificalConcept()),
        mappings,
        new SifNotNullAndNifNullConverter());

    // Recent migration of the family (Migração recente da família) – Sheet 1: Column T
    patientDataSetDefinition.addColumn(
        "reason_t",
        listOfChildrenEnrolledInCCRDataDefinitionQueries.getReasonForVisitOnCCREnrollmentDate(
            commonMetadata.getMigracaoRecenteFamiliaConcept()),
        mappings,
        new SifNotNullAndNifNullConverter());

    // Other (Outro) – Sheet 1: Column U
    patientDataSetDefinition.addColumn(
        "reason_u",
        listOfChildrenEnrolledInCCRDataDefinitionQueries.getReasonForVisitOnCCREnrollmentDate(
            hivMetadata.getOtherOrNonCodedConcept()),
        mappings,
        new SifNotNullAndNifNullConverter());

    // PTV Code (Código PTV) Sheet 1: Column V
    patientDataSetDefinition.addColumn(
        "ptv_code", listOfChildrenEnrolledInCCRDataDefinitionQueries.getPtvCode(), mappings);

    // Mother’s Name (Nome da Mãe) Sheet 1: Column W
    patientDataSetDefinition.addColumn(
        "mother_name", listOfChildrenEnrolledInCCRDataDefinitionQueries.getMothersName(), mappings);

    // Mother’s TARV NID (NID TARV da Mãe) – Sheet 1: Column X
    patientDataSetDefinition.addColumn(
        "mother_nid", listOfChildrenEnrolledInCCRDataDefinitionQueries.getMothersNID(), mappings);

    // Aceita Visita Domiciliar – Sheet 1: Column Z
    patientDataSetDefinition.addColumn(
        "visit_consent",
        listOfChildrenEnrolledInCCRDataDefinitionQueries.getHomeVisitConsent(),
        mappings);

    // Data da Primeira Consulta CCR – Sheet 1: Column AA
    patientDataSetDefinition.addColumn(
        "first_ccr_consultation",
        listOfChildrenEnrolledInCCRDataDefinitionQueries.getFirstCCRSeguimentoDate(true),
        mappings);

    // Data da Última Consulta de Seguimento CCR – Sheet 1: Column AB
    patientDataSetDefinition.addColumn(
        "last_ccr_consultation",
        listOfChildrenEnrolledInCCRDataDefinitionQueries.getFirstCCRSeguimentoDate(false),
        mappings);

    // Next Scheduled CCR Consultation Date – Sheet 1: Column AC
    patientDataSetDefinition.addColumn(
        "next_ccr_consultation",
        listOfChildrenEnrolledInCCRDataDefinitionQueries.getNextScheduledCCRConsultation(),
        mappings);

    // Date of Most Recent PCR Test Result – Sheet 1: Column AD
    patientDataSetDefinition.addColumn(
        "last_pcr",
        listOfChildrenEnrolledInCCRDataDefinitionQueries.getMostRecentPCRTestDate(),
        "endDate=${endDate},location=${location}");

    // Sample Collection Type for Most Recent PCR Test Result – Sheet 1: Column AE
    patientDataSetDefinition.addColumn(
        "sample_type",
        listOfChildrenEnrolledInCCRDataDefinitionQueries.getSampleCollectionType(),
        "endDate=${endDate},location=${location}",
        new ObservationToConceptNameConverter());

    // getMostRecentPCRTestResult – Sheet 1: Column AF
    patientDataSetDefinition.addColumn(
        "pcr_result",
        listOfChildrenEnrolledInCCRDataDefinitionQueries.getSampleCollectionType(),
        "endDate=${endDate},location=${location}",
        new TestResultConverter());

    return patientDataSetDefinition;
  }

  public DataSetDefinition listOfChildrenEnrolledInCCRTotalsDataset() {

    CohortIndicatorDataSetDefinition dataSetDefinition = new CohortIndicatorDataSetDefinition();
    dataSetDefinition.setName("List of Children Enrolled in CCR Totals DataSet");

    dataSetDefinition.addParameters(getParameters());

    return dataSetDefinition;
  }
}
