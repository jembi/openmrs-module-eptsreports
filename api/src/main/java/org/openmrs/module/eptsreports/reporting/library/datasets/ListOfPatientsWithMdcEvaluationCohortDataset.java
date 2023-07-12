package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.reporting.data.converter.*;
import org.openmrs.module.eptsreports.reporting.library.cohorts.DQACargaViralCohortQueries;
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

  private final DQACargaViralCohortQueries dQACargaViralCohortQueries;

  private ListChildrenOnARTandFormulationsDataset listChildrenOnARTandFormulationsDataset;

  private ListOfPatientsDefaultersOrIITCohortQueries listOfPatientsDefaultersOrIITCohortQueries;

  @Autowired
  public ListOfPatientsWithMdcEvaluationCohortDataset(
//      ListOfPatientsWithHighViralLoadCohortQueries listOfPatientsWithHighViralLoadCohortQueries,
      ListOfPatientsArtCohortCohortQueries listOfPatientsArtCohortCohortQueries,
      TPTListOfPatientsEligibleDataSet tptListOfPatientsEligibleDataSet,
      TPTInitiationDataDefinitionQueries tptInitiationDataDefinitionQueries,
      DQACargaViralCohortQueries dQACargaViralCohortQueries,
      ListOfPatientsDefaultersOrIITCohortQueries listOfPatientsDefaultersOrIITCohortQueries,
      ListChildrenOnARTandFormulationsDataset listChildrenOnARTandFormulationsDataset) {
//    this.listOfPatientsWithHighViralLoadCohortQueries =
//        listOfPatientsWithHighViralLoadCohortQueries;
    this.listOfPatientsArtCohortCohortQueries = listOfPatientsArtCohortCohortQueries;
    this.tptListOfPatientsEligibleDataSet = tptListOfPatientsEligibleDataSet;
    this.tptInitiationDataDefinitionQueries = tptInitiationDataDefinitionQueries;
    this.dQACargaViralCohortQueries = dQACargaViralCohortQueries;
    this.listOfPatientsDefaultersOrIITCohortQueries = listOfPatientsDefaultersOrIITCohortQueries;
    this.listChildrenOnARTandFormulationsDataset = listChildrenOnARTandFormulationsDataset;
  }

  public DataSetDefinition contructDataset() {

    PatientDataSetDefinition pdd = new PatientDataSetDefinition();

    pdd.setName("MDC");
    pdd.addParameter(new Parameter("startDate", "startDate", Date.class));
    pdd.addParameter(new Parameter("endDate", "endDate", Date.class));
    pdd.addParameter(new Parameter("location", "Location", Location.class));

    PatientIdentifierType identifierType =
        Context.getPatientService()
            .getPatientIdentifierTypeByUuid("e2b966d0-1d5f-11e0-b929-000c29ad1d07");

//    DataConverter identifierFormatter = new ObjectFormatter("{identifier}");

//    DataDefinition identifierDef =
//        new ConvertedPatientDataDefinition(
//            "identifier",
//            new PatientIdentifierDataDefinition(identifierType.getName(), identifierType),
//            identifierFormatter);

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
    pdd.addColumn("counter", new PersonIdDataDefinition(), "", new ObjectCounterConverter());

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
            dQACargaViralCohortQueries.getDataNotificouCV(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            new ForwardSlashDateConverter());

    // B.2 - Data de registo do resultado da 1ª CV - Sheet 1: Column K
    pdd.addColumn(
            "firstCv_result_date",
            dQACargaViralCohortQueries.getDataNotificouCV(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            new ForwardSlashDateConverter());

    // B.3 - Resultado da 1ª CV - Sheet 1: Column L
    pdd.addColumn(
            "firstCv_result",
            dQACargaViralCohortQueries.getDataNotificouCV(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            new ForwardSlashDateConverter());

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
            "bMds1",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.2 - Data de Inicio MDS 1 - Sheet 1: Column T
    pdd.addColumn(
            "bMds1_startDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.3 - Data de Fim MDS 1 - Sheet 1: Column U
    pdd.addColumn(
            "bMds1_endDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.4 - Tipo de MDS 2 - Sheet 1: Column V
    pdd.addColumn(
            "bMds2",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.5 - Data de Inicio MDS 2 - Sheet 1: Column W
    pdd.addColumn(
            "bMds2_startDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.6 - Data de Fim MDS 2 - Sheet 1: Column X
    pdd.addColumn(
            "bMds2_endDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.7 - Tipo de MDS 3 - Sheet 1: Column Y
    pdd.addColumn(
            "bMds3",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.8 - Data de Inicio MDS 3 - Sheet 1: Column Z
    pdd.addColumn(
            "bMds3_startDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.9 - Data de Fim MDS 3 - Sheet 1: Column AA
    pdd.addColumn(
            "bMds3_endDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.10 - Tipo de MDS 4 - Sheet 1: Column AB
    pdd.addColumn(
            "bMds4",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.11 - Data de Inicio MDS 4 - Sheet 1: Column AC
    pdd.addColumn(
            "bMds4_startDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.12 - Data de Fim MDS 4 - Sheet 1: Column AD
    pdd.addColumn(
            "bMds4_endDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.13 - Tipo de MDS 5 - Sheet 1: Column AE
    pdd.addColumn(
            "bMds5",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.14 - Data de Inicio MDS 5 - Sheet 1: Column AF
    pdd.addColumn(
            "bMds5_startDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.10.15 - Data de Fim MDS 5 - Sheet 1: Column AG
    pdd.addColumn(
            "bMds5_endDate",
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
            "clinicalConsultations_6thTo12thMonth",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.17 - N˚ de consultas de APSS/PP entre 6˚ e 12˚ mês de TARV - Sheet 1: Column AN
    pdd.addColumn(
            "apssPpConsultations_6thTo12thMonth",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // B.18 - Estado de permanência no 12˚ mês de TARV - Sheet 1: Column AO
    pdd.addColumn(
            "statePermanence_6thTo12thMonth",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // SECÇÃO C
    // 24 MESES DEPOIS DA INSCRIÇÃO NOS MDS: ELIGIBILIDADE A MDS

    // C.1 - Data do pedido da CV de seguimento - Sheet 1: Column AP
    pdd.addColumn(
            "cv_date",
            dQACargaViralCohortQueries.getDataNotificouCV(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            new ForwardSlashDateConverter());

    // C.2 - Data de registo do resultado da CV de seguimento - Sheet 1: Column AQ
    pdd.addColumn(
            "cvResult_date",
            dQACargaViralCohortQueries.getDataNotificouCV(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            new ForwardSlashDateConverter());

    // C.3 - Resultado da CV de seguimento - Sheet 1: Column AR
    pdd.addColumn(
            "cvResult",
            dQACargaViralCohortQueries.getDataNotificouCV(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            new ForwardSlashDateConverter());

    // C.4 - Resultado do CD4 feito entre 12˚ e 24˚ mês de TARV - Sheet 1: Column AS
    pdd.addColumn(
            "cd4Result_12thTo24thMonthTarv",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // C.5 - Teve registo de boa adesão em TODAS consultas entre 12˚ e o 24˚ mês de TARV? - Sheet 1: Column AT
    pdd.addColumn(
            "goodAdherence_12thTo24thMonthTarv",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // C.6 - Esteve grávida ou foi lactante entre 12˚ e 24˚ mês de TARV - Sheet 1: Column AU
    pdd.addColumn(
            "pregnantBreastfeeding_12thTo24thMonthTarv",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // C.7 - Teve condição clínica activa do estadio III ou IV entre 12˚ e 24˚ mês de TARV - Sheet 1: Column AV
    pdd.addColumn(
            "clinicalConditionIiiOrIv_12thTo24thMonthTarv",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // C.8 - Teve TB entre 12˚ e 24˚ mês de TARV - Sheet 1: Column AW
    pdd.addColumn(
            "tb_12thTo24thMonthTarv",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // C.9 - Inscrito em algum MDS entre 12˚ a 24˚ mês de TARV? - Sheet 1: Column AX
    pdd.addColumn(
            "mds_12thTo24thMonthTarv",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // C.10.1 - Tipo de MDS 1 - Sheet 1: Column AY
    pdd.addColumn(
            "cMds1",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // C.10.2 - Data de Inicio MDS1 - Sheet 1: Column AZ
    pdd.addColumn(
            "cMds1_startDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // C.10.3 - Data de Fim MDS1 - Sheet 1: Column BA
    pdd.addColumn(
            "cMds1_endDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // C.10.4 - Tipo de MDS 2 - Sheet 1: Column BB
    pdd.addColumn(
            "cMds2",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // C.10.5 - Data de Inicio MDS 2 - Sheet 1: Column BC
    pdd.addColumn(
            "cMds2_startDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // C.10.6 - Data de Fim MDS 2 - Sheet 1: Column BD
    pdd.addColumn(
            "cMds2_endDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // C.10.7 - Tipo de MDS 3 - Sheet 1: Column BE
    pdd.addColumn(
            "cMds3",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // C.10.8 - Data de Inicio MDS 3 - Sheet 1: Column BF
    pdd.addColumn(
            "cMds3_startDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // C.10.9 - Data de Fim MDS 3 - Sheet 1: Column BG
    pdd.addColumn(
            "cMds3_endDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // C.10.10 - Tipo de MDS 4 - Sheet 1: Column BH
    pdd.addColumn(
            "cMds4",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // C.10.11 - Data de Inicio MDS 4 - Sheet 1: Column BI
    pdd.addColumn(
            "cMds4_startDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // C.10.12 - Data de Fim MDS 4 - Sheet 1: Column BJ
    pdd.addColumn(
            "cMds4_endDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // C.10.13 - Tipo de MDS 5 - Sheet 1: Column BK
    pdd.addColumn(
            "cMds5",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // C.10.14 - Data de Inicio MDS 5 - Sheet 1: Column BL
    pdd.addColumn(
            "cMds5_startDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // C.10.15 - Data de Fim MDS 5 - Sheet 1: Column BM
    pdd.addColumn(
            "cMds5_endDate",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // QUALIDADE DE SERVIÇOS

    // C.11 - Rastreado para TB em TODAS as consultas entre 12˚ e 24˚ mês de TARV? - Sheet 1: Column BN
    pdd.addColumn(
            "tbScreening_12thTo24thMonthTarv",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // C.12 - Elegível para TPT entre 12˚ e 24˚ mês de TARV - Sheet 1: Column BO
    pdd.addColumn(
            "tptEligible_12thTo24thMonthTarv",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // C.13 - Recebeu TPT entre 12˚ e 24˚ mês de TARV? - Sheet 1: Column BP
    pdd.addColumn(
            "receivedTpt_12thTo24thMonthTarv",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // C.14 - PB/IMC registado em todas as consultas entre 12˚ a 24˚ mês de TARV? - Sheet 1: Column BQ
    pdd.addColumn(
            "pbImc_12thTo24thMonthTarv",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // C.15 - Malnutrido que recebeu tto para desnutrição entre 12˚ e 24˚ mês de TARV? - Sheet 1: Column BR
    pdd.addColumn(
            "malnutrition_12thTo24thMonthTarv",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // C.16 - N˚ de consultas clínicas entre 12˚ e 24˚ mês de TARV - Sheet 1: Column BS
    pdd.addColumn(
            "clinicalConsultations_12thTo24thMonthTarv",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // C.17 - N˚ de consultas de APSS/PP entre 12˚ e 24˚ mês de TARV - Sheet 1: Column BT
    pdd.addColumn(
            "apssPpConsultations_12thTo24thMonthTarv",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    // C.18 - Estado de permanência no 24˚ mês de TARV - Sheet 1: Column BU
    pdd.addColumn(
            "statePermanence",
            tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
            "startDate=${startDate},endDate=${endDate},location=${location}",
            null);

    return pdd;
  }

}
