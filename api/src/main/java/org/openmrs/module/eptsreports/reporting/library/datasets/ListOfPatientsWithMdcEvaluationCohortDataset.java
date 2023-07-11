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

    //  SECÇÃO B
    //  12 MESES DEPOIS DO INÍCIO DO TARV: ELIGIBILIDADE A MDS

    // B.1 - Data do pedido da 1ª CV - Sheet 1: Column J
    pdd.addColumn(
            "firstCv_date",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.2 - Data de registo do resultado da 1ª CV - Sheet 1: Column K
    pdd.addColumn(
            "firstCv_result_date",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.3 - Resultado da 1ª CV - Sheet 1: Column L
    pdd.addColumn(
            "firstCv_result",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.4 - Resultado do 2˚ CD4 (2˚ CD4 feito nos 1˚s 12 meses de TARV) - Sheet 1: Column M
    pdd.addColumn(
            "second_cd4_result",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.5 - Teve registo de boa adesão em TODAS as consultas entre o 1˚ e 3˚ mês de TARV? - Sheet 1: Column N
    pdd.addColumn(
            "registed_firstToThirdMonthTarv",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.6 - Esteve grávida ou foi lactante entre 3˚ e 9˚ mês de TARV? - Sheet 1: Column O
    pdd.addColumn(
            "pregnantBreastfeeding_thridToNinethmonthTarv",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.7 - Teve condição clínica activa do estadio III ou IV entre 3˚ e 9˚ mês de  TARV - Sheet 1: Column P
    pdd.addColumn(
            "clinicalConditionIiiorIv_thridToNinethmonthTarv",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.8 - Teve TB entre o 3˚ e 9˚ mês de  TARV - Sheet 1: Column Q
    pdd.addColumn(
            "tb_thridToNinethmonthTarv",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.9 - Data de inscrição no MDS - Sheet 1: Column R
    pdd.addColumn(
            "mds_date",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.1 - Tipo de MDS 1 - Sheet 1: Column S
    pdd.addColumn(
            "mds1_type",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.2 - Data de Inicio MDS 1 - Sheet 1: Column T
    pdd.addColumn(
            "mds1_startDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.3 - Data de Fim MDS 1 - Sheet 1: Column U
    pdd.addColumn(
            "mds1_endDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.4 - Tipo de MDS 2 - Sheet 1: Column V
    pdd.addColumn(
            "mds2_type",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.5 - Data de Inicio MDS 2 - Sheet 1: Column W
    pdd.addColumn(
            "mds2_startDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.6 - Data de Fim MDS 2 - Sheet 1: Column X
    pdd.addColumn(
            "mds2_endDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.7 - Tipo de MDS 3 - Sheet 1: Column Y
    pdd.addColumn(
            "mds3_type",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.8 - Data de Inicio MDS 3 - Sheet 1: Column Z
    pdd.addColumn(
            "mds3_startDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.9 - Data de Fim MDS 3 - Sheet 1: Column AA
    pdd.addColumn(
            "mds3_endDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.10 - Tipo de MDS 4 - Sheet 1: Column AB
    pdd.addColumn(
            "mds4_type",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.11 - Data de Inicio MDS 4 - Sheet 1: Column AC
    pdd.addColumn(
            "mds4_startDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.12 - Data de Fim MDS 4 - Sheet 1: Column AD
    pdd.addColumn(
            "mds4_endDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.13 - Tipo de MDS 5 - Sheet 1: Column AE
    pdd.addColumn(
            "mds5_type",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.14 - Data de Inicio MDS 5 - Sheet 1: Column AF
    pdd.addColumn(
            "mds5_startDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.15 - Data de Fim MDS 5 - Sheet 1: Column AG
    pdd.addColumn(
            "mds5_endDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // QUALIDADE DE SERVIÇOS

    // B.11 - Rastreado para TB em TODAS as consultas entre a data de inscrição no MDS e 12˚ mês de TARV? - Sheet 1: Column AH
    pdd.addColumn(
            "tb_screening",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.12 - Elegível para TPT entre a data de inscrição no MDS e 12˚ mês de TARV? - Sheet 1: Column AI
    pdd.addColumn(
            "tpt_eligible",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.13 - Recebeu TPT entre a data de inscrição no MDS e 12˚ mês de TARV? - Sheet 1: Column AJ
    pdd.addColumn(
            "receivedTpt",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.14 - PB/IMC registado em TODAS as consultas desde a inscrição no MDS até ao 12˚ mês de TARV? - Sheet 1: Column AK
    pdd.addColumn(
            "pbImc",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.15 - Malnutrido recebeu tto para desnutrição entre a inscrição no MDS e o 12˚ mês de TARV? - Sheet 1: Column AL
    pdd.addColumn(
            "malnutrition",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.16 - N˚ de consultas clínicas entre 6˚ e 12˚ mês de TARV - Sheet 1: Column AM
    pdd.addColumn(
            "nr_clinicalConsultations_sixthToTwelvethMonth",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.17 - N˚ de consultas de APSS/PP entre 6˚ e 12˚ mês de TARV - Sheet 1: Column AN
    pdd.addColumn(
            "nr_apssPpConsultations_sixthToTwelvethMonth",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.18 - Estado de permanência no 12˚ mês de TARV - Sheet 1: Column AO
    pdd.addColumn(
            "nr_apssPpConsultations_sixthToTwelvethMonth",
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
