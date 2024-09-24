package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.Arrays;
import java.util.List;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.data.converter.*;
import org.openmrs.module.eptsreports.reporting.library.cohorts.*;
import org.openmrs.module.reporting.data.person.definition.*;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsWithMdsEvaluationCohortDataset extends BaseDataSet {

  private ListOfPatientsWithMdsEvaluationCohortQueries listOfPatientsWithMdsEvaluationCohortQueries;

  private String endDateMappings = "endDate=${evaluationYear}-06-20,location=${location}";
  private String b20Mappings = "endDate=${evaluationYear-1}-06-20,location=${location}";
  private String c20Mappings = "endDate=${evaluationYear-2}-06-20,location=${location}";
  private String d20Mappings = "endDate=${evaluationYear-3}-06-20,location=${location}";

  @Autowired
  public ListOfPatientsWithMdsEvaluationCohortDataset(
      ListOfPatientsWithMdsEvaluationCohortQueries listOfPatientsWithMdsEvaluationCohortQueries) {
    this.listOfPatientsWithMdsEvaluationCohortQueries =
        listOfPatientsWithMdsEvaluationCohortQueries;
  }

  public DataSetDefinition contructDataset() {

    PatientDataSetDefinition pdd = new PatientDataSetDefinition();
    pdd.setName("MDS");
    pdd.setParameters(getParameters());

    pdd.addRowFilter(
        listOfPatientsWithMdsEvaluationCohortQueries.getCoort12Or24Or36(),
        "evaluationYear=${evaluationYear},location=${location}");

    //  SECÇÃO A
    //  INFORMAÇÃO DO PACIENTE

    // Patient_ID
    pdd.addColumn("pid", new PersonIdDataDefinition(), "");

    // A1- Nr. Sequencial: (Coluna A)
    pdd.addColumn("counter", new PersonIdDataDefinition(), "", new ObjectCounterConverter());

    // A2- Coorte: (coluna B)
    pdd.addColumn(
        "coort",
        listOfPatientsWithMdsEvaluationCohortQueries.getCoort12Or24Or36Months(),
        "evaluationYear=${evaluationYear},location=${location}");

    // A3- Sexo: (coluna C)
    pdd.addColumn("gender", new GenderDataDefinition(), "");

    // A4- Idade: (coluna D)
    pdd.addColumn(
        "age",
        listOfPatientsWithMdsEvaluationCohortQueries.getAgeOnMOHArtStartDate(),
        endDateMappings);

    // A5- Data início TARV: (coluna E)
    pdd.addColumn(
        "art_start",
        listOfPatientsWithMdsEvaluationCohortQueries.getArtStartDate(),
        endDateMappings,
        new GeneralDateConverter());

    // A6- Elegível ao TPT no Início do TARV: (coluna F)
    pdd.addColumn(
        "tpt_eligible_tarv",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsTptNotEligible(),
        endDateMappings,
        new YesOrNoConverter());

    // A7- Data de início do TPT: (coluna G)
    pdd.addColumn(
        "tpt_start_date",
        listOfPatientsWithMdsEvaluationCohortQueries.getTptInitiationDate(),
        endDateMappings,
        new GeneralDateConverter());

    // A8- Data de registo do resultado de CD4 inicial: (coluna H)
    pdd.addColumn(
        "cd4_register_date",
        listOfPatientsWithMdsEvaluationCohortQueries.getCd4ResultDate(),
        endDateMappings,
        new GeneralDateConverter());

    // A9- Resultado do CD4 Inicial: (coluna I)
    pdd.addColumn(
        "initial_cd4_result",
        listOfPatientsWithMdsEvaluationCohortQueries.getCd4Result(),
        endDateMappings);

    //  SECÇÃO B
    //  12 MESES DEPOIS DO INÍCIO DO TARV: ELIGIBILIDADE A MDS

    // B.1 - Data do pedido da última CV nos 1˚s  12 meses de TARV - Sheet 1: Column J
    pdd.addColumn(
        "first_cv_date",
        listOfPatientsWithMdsEvaluationCohortQueries
            .getLastViralLoadRequestOnTheFirst12MonthsOfTarv(),
        endDateMappings,
        new GeneralDateConverter());

    // B.2 - Data de registo do resultado da última CV nos 1os 12 meses do TARV - Sheet 1: Column K
    pdd.addColumn(
        "first_cv_result_date",
        listOfPatientsWithMdsEvaluationCohortQueries.getFirstViralLoadResultDate(),
        endDateMappings,
        new GeneralDateConverter());

    // B.3 - Resultado da última CV - Sheet 1: Column L
    pdd.addColumn(
        "first_cv_result",
        listOfPatientsWithMdsEvaluationCohortQueries.getLastViralLoadResultOnThe1st12MonthsOfTarv(),
        endDateMappings,
        new ViralLoadQualitativeLabelConverter());

    // B.4 - Resultado do 2˚ CD4 (2˚ CD4 feito nos 1˚s 12 meses de TARV) - Sheet 1: Column M
    pdd.addColumn(
        "second_cd4_result",
        listOfPatientsWithMdsEvaluationCohortQueries.getSecondCd4Result(),
        endDateMappings);

    // B5- Teve registo de boa adesão em TODAS consultas entre 1˚ e 3˚ mês de TARV?
    pdd.addColumn(
        "good_adherence_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsWithGoodAdhesion(true, 1, 3, 1, 4),
        // minNumberOfMonths and MaxNumberOfMonths has no effect here because the boolean b5OrC5 is
        // set to true
        endDateMappings);

    // B6- Esteve grávida ou foi lactante entre 3˚ e 9º mês de TARV?: (coluna M)- Resposta = Sim ou
    // Não (RF22)
    pdd.addColumn(
        "pregnant_breastfeeding_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsPregnantBreastfeeding3MonthsTarv(
            3, 9, true, 1, 4),
        endDateMappings);

    // B7 - Condição Clínica Activa de Estadio III ou IV entre a Data Inscrição no MDS e 12º mês de
    // TARV
    pdd.addColumn(
        "clinical_condiction_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getActiveClinicalCondiction(0, 12, true, 1, 4),
        endDateMappings,
        new NotApplicableIfNullConverter());

    // B8- Teve TB nos 1˚s 12 meses de TARV: (coluna Q) - Resposta = Sim ou Não (RF23)
    pdd.addColumn(
        "tb_tarv_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsWithTbThirdToNineMonth(
            3, 9, true, 1, 4),
        endDateMappings);

    // B9- Data de inscrição no MDS: (coluna R) - Resposta = Data de Inscrição (RF24)
    pdd.addColumn(
        "mds_date",
        listOfPatientsWithMdsEvaluationCohortQueries.getMdsDate(0, 12, true, 1, 4),
        endDateMappings,
        new GeneralDateConverter());

    // B10.1 -Tipo de MDS: (MDS1) Coluna S
    pdd.addColumn(
        "mds_one_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds1(12, 1, 4),
        endDateMappings,
        new DispensationTypeMdcConverter());

    // B10.2 - Data Início de MDS1: Coluna T
    pdd.addColumn(
        "mds_one_start_date_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds1StartDate(12, 1, 4),
        endDateMappings,
        new GeneralDateConverter());

    // B10.3 - Data Fim de MDS1: Coluna U
    pdd.addColumn(
        "mds_one_end_date_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds1EndDate(12, 1, 4),
        endDateMappings,
        new GeneralDateConverter());

    // B10.4 - Tipo de MDS: (MDS2) Coluna V
    pdd.addColumn(
        "mds_two_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds2(12, 1, 4),
        endDateMappings,
        new DispensationTypeMdcConverter());

    // B10.5 - Data Início de MDS2: Coluna W
    pdd.addColumn(
        "mds_two_start_date_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds2StartDate(12, 1, 4),
        endDateMappings,
        new GeneralDateConverter());

    // B10.6 - Data Fim de MDS2: Coluna X
    pdd.addColumn(
        "mds_two_end_date_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds2EndDate(12, 1, 4),
        endDateMappings,
        new GeneralDateConverter());

    // B10.7 - Tipo de MDS: (MDS3) Coluna Y
    pdd.addColumn(
        "mds_three_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds3(12, 1, 4),
        endDateMappings,
        new DispensationTypeMdcConverter());

    // B10.8 - Data Início de MDS3: Coluna Z
    pdd.addColumn(
        "mds_three_start_date_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds3StartDate(12, 1, 4),
        endDateMappings,
        new GeneralDateConverter());

    // B10.9 - Data Fim de MDS3: Coluna AA
    pdd.addColumn(
        "mds_three_end_date_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds3EndDate(12, 1, 4),
        endDateMappings,
        new GeneralDateConverter());

    // B10.10 - Tipo de MDS: (MDS4) Coluna AB
    pdd.addColumn(
        "mds_four_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds4(12, 1, 4),
        endDateMappings,
        new DispensationTypeMdcConverter());

    // B10.11 - Data Início de MDS4: Coluna AC
    pdd.addColumn(
        "mds_four_start_date_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds4StartDate(12, 1, 4),
        endDateMappings,
        new GeneralDateConverter());

    // B10.12 - Data Fim de MDS4: Coluna AD
    pdd.addColumn(
        "mds_four_end_date_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds4EndDate(12, 1, 4),
        endDateMappings,
        new GeneralDateConverter());

    // B10.13 - Tipo de MDS: (MDS5) Coluna AE
    pdd.addColumn(
        "mds_five_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds5(12, 1, 4),
        endDateMappings,
        new DispensationTypeMdcConverter());

    // B10.14 - Data Início de MDS5: Coluna AF
    pdd.addColumn(
        "mds_five_start_date_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds5StartDate(12, 1, 4),
        endDateMappings,
        new GeneralDateConverter());

    // B10.15 - Data Fim de MDS5: Coluna AG
    pdd.addColumn(
        "mds_five_end_date_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds5EndDate(12, 1, 4),
        endDateMappings,
        new GeneralDateConverter());

    // B11 - Rastreado para TB em TODAS as consultas entre a data de inscrição no MDS e 12˚ mês de
    // TARV
    pdd.addColumn(
        "tb_screening_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getTbScreeningSectionB(0, 12, true),
        endDateMappings,
        new NotApplicableIfNullConverter());

    // B12 - Recebeu uma forma de PF entre a data de inscrição no MDS e 12˚ mês de TARV?
    pdd.addColumn(
        "family_planning_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsWhoReceivedPf(0, 12, 1, 4),
        endDateMappings,
        new NotApplicableIfNullConverter());

    // B13 - Recebeu TPT entre a data de inscrição no MDS e 12˚ mês de TARV?
    pdd.addColumn(
        "received_tpt_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsWhoReceivedTpt(0, 12, true, 1, 4),
        endDateMappings,
        new NotApplicableIfNullConverter());

    // B14 - Identificação de registo de PB/IMC em TODAS as consultas desde a inscrição no MDS até
    // ao 12˚ mês de TARV Coluna AK
    pdd.addColumn(
        "pb_imc_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getTbScreeningSectionB(0, 12, false),
        endDateMappings,
        new NotApplicableIfNullConverter());

    // B15 - Rastreado para Tensão Arterial em todas as consultas entre a data de inscrição no MDS e
    // 12 ̊ mês de TARV?
    pdd.addColumn(
        "arterial_pressure_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientWithArterialPressure(
            0, 12, true, 4, 1),
        endDateMappings,
        new NotApplicableIfNullConverter());

    // B16 - Identificação de n˚ de consultas clínicas entre 6˚ e 12˚ mês de TARV Coluna AM
    pdd.addColumn(
        "clinical_consultations_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getNrClinicalConsultations(6, 12, 1, 4),
        endDateMappings);

    // B17 - N˚ de consultas de APSS/PP entre 6˚ e 12˚ mês de TARV - Coluna AN
    pdd.addColumn(
        "apss_pp_consultations_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getNrApssPpConsultations(6, 12, 1, 4),
        endDateMappings);

    // B18 - Rastreado para CACUM entre o 1˚ e 12 meses de TARV?
    pdd.addColumn(
        "cacum_screening_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsWithCacumScreening(0, 12, 1, 4),
        endDateMappings,
        new NotApplicableIfNullConverter());

    // B19 - Resultado positivo para CACUM entre o 1˚ e 12 meses de TARV?
    pdd.addColumn(
        "positive_cacum_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsWithPositiveCacum(0, 12, 1, 4),
        endDateMappings,
        new NotApplicableIfNullConverter());

    // B20 - Estado de permanência no 12˚ mês de TARV: (coluna AO)
    pdd.addColumn(
        "permanence_state_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getLastStateOfStayOnTarv(0, 3),
        b20Mappings);

    // C1 - Data do pedido da CV entre 12º e 24º mês de TARV: (coluna AP)
    pdd.addColumn(
        "cv_date_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getLastViralLoadOnThePeriod(12, 24, 2, 4),
        endDateMappings,
        new GeneralDateConverter());

    // C.2 - Data de registo do resultado da CV entre 12º e 24º mês do TARV: (coluna AQ)
    pdd.addColumn(
        "cv_result_date_c",
        listOfPatientsWithMdsEvaluationCohortQueries
            .getLastViralLoadResultDateBetweenPeriodsInMonthsAfterTarv(12, 24, 2, 4),
        endDateMappings,
        new GeneralDateConverter());

    // C.3 - Identificação do Resultado da CV de Seguimento - Sheet 1: (coluna AR)
    pdd.addColumn(
        "cv_result_c",
        listOfPatientsWithMdsEvaluationCohortQueries
            .getSecondViralLoadResultBetweenPeriodsOfMonthsAfterTarv(12, 24, 2, 4),
        endDateMappings,
        new ViralLoadQualitativeLabelConverter());

    // C4 - Resultado do CD4 feito entre 12˚ e 24˚ mês de TARV- C.4 (Coluna AS)
    pdd.addColumn(
        "cd4_result_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getCd4ResultSectionC(12, 24, 2, 4),
        endDateMappings);

    // C5- Teve registo de boa adesão em TODAS consultas entre 12˚ e 24˚ mês de TARV?
    pdd.addColumn(
        "good_adherence_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsWithGoodAdhesion(
            false, 12, 24, 2, 4),
        endDateMappings);

    // C6 - Esteve grávida ou foi lactante entre 12˚ e 24º mês de TARV?: (coluna AU) - Resposta =
    // Sim ou Não (RF37)
    pdd.addColumn(
        "pregnant_breastfeeding_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsPregnantBreastfeeding3MonthsTarv(
            12, 24, false, 2, 4),
        endDateMappings);

    // C7 - Condição Clínica Activa de Estadio III ou IV entre 12˚ e 24˚ mês de TARV
    pdd.addColumn(
        "clinical_condiction_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getActiveClinicalCondiction(
            12, 24, false, 2, 4),
        endDateMappings);

    // C8 - Teve TB entre 12˚ e 24 ˚ meses de TARV: (coluna AW) - Resposta = Sim ou Não (RF38)
    pdd.addColumn(
        "tb_tarv_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsWithTbThirdToNineMonth(
            12, 24, false, 2, 4),
        endDateMappings);

    // C9 - Data de inscrição no MDS entre 12º e 24º mês de TAV: (coluna AX) - Resposta = Data de
    // Inscrição (RF39)
    pdd.addColumn(
        "mds_tarv_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getMdsDate(12, 24, false, 2, 4),
        endDateMappings,
        new GeneralDateConverter());

    // C10.1 -Tipo de MDS: (MDS1) Coluna AY
    pdd.addColumn(
        "mds_one_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds1(24, 2, 4),
        endDateMappings,
        new DispensationTypeMdcConverter());

    // C10.2 - Data Início de MDS1: Coluna AZ
    pdd.addColumn(
        "mds_one_start_date_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds1StartDate(24, 2, 4),
        endDateMappings,
        new GeneralDateConverter());

    // C10.3 - Data Fim de MDS1: Coluna BA
    pdd.addColumn(
        "mds_one_end_date_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds1EndDate(24, 2, 4),
        endDateMappings,
        new GeneralDateConverter());

    // C10.4 - Tipo de MDS: (MDS2) Coluna BB
    pdd.addColumn(
        "mds_two_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds2(24, 2, 4),
        endDateMappings,
        new DispensationTypeMdcConverter());

    // C10.5 - Data Início de MDS2: Coluna BC
    pdd.addColumn(
        "mds_two_start_date_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds2StartDate(24, 2, 4),
        endDateMappings,
        new GeneralDateConverter());

    // C10.6 - Data Fim de MDS2: Coluna BD
    pdd.addColumn(
        "mds_two_end_date_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds2EndDate(24, 2, 4),
        endDateMappings,
        new GeneralDateConverter());

    // C10.7 - Tipo de MDS: (MDS3) Coluna BE
    pdd.addColumn(
        "mds_three_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds3(24, 2, 4),
        endDateMappings,
        new DispensationTypeMdcConverter());

    // C10.8 - Data Início de MDS3: Coluna BF
    pdd.addColumn(
        "mds_three_start_date_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds3StartDate(24, 2, 4),
        endDateMappings,
        new GeneralDateConverter());

    // C10.9 - Data Fim de MDS3: Coluna BG
    pdd.addColumn(
        "mds_three_end_date_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds3EndDate(24, 2, 4),
        endDateMappings,
        new GeneralDateConverter());

    // C10.10 - Tipo de MDS: (MDS4) Coluna BH
    pdd.addColumn(
        "mds_four_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds4(24, 2, 4),
        endDateMappings,
        new DispensationTypeMdcConverter());

    // C10.11 - Data Início de MDS4: Coluna BI
    pdd.addColumn(
        "mds_four_start_date_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds4StartDate(24, 2, 4),
        endDateMappings,
        new GeneralDateConverter());

    // C10.12 - Data Fim de MDS4: Coluna BJ
    pdd.addColumn(
        "mds_four_end_date_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds4EndDate(24, 2, 4),
        endDateMappings,
        new GeneralDateConverter());

    // C10.13 - Tipo de MDS: (MDS5) Coluna BK
    pdd.addColumn(
        "mds_five_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds5(24, 2, 4),
        endDateMappings,
        new DispensationTypeMdcConverter());

    // C10.14 - Data Início de MDS5: Coluna BL
    pdd.addColumn(
        "mds_five_start_date_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds5StartDate(24, 2, 4),
        endDateMappings,
        new GeneralDateConverter());

    // C10.15 - Data Fim de MDS5: Coluna BM
    pdd.addColumn(
        "mds_five_end_date_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds5EndDate(24, 2, 4),
        endDateMappings,
        new GeneralDateConverter());

    // C11 - Rastreado para TB em TODAS as consultas entre 12˚ e 24˚ mês de TARV?- C.11 (Coluna BN)
    pdd.addColumn(
        "tb_screening_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getTbScreeningSectionC(true, 12, 24, 2, 4),
        endDateMappings);

    // C12 - Recebeu uma forma de PF entre 12˚ e 24˚ mês de TARV?
    pdd.addColumn(
        "family_planning_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsWhoReceivedPf12to24MonthTarv(
            12, 24, 2, 4),
        endDateMappings,
        new NotApplicableIfNullConverter());

    // C13 - Recebeu TPT entre 12˚ e 24˚ mês de TARV?
    pdd.addColumn(
        "received_tpt_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsWhoReceivedTpt(12, 24, false, 2, 4),
        endDateMappings,
        new NotApplicableIfNullConverter());

    // C14 - PB/IMC registado em TODAS as consultas entre o 12˚ a 24º mês de TARV? (coluna BQ) -
    // Resposta = Sim ou Não ou N/A (RF27)
    pdd.addColumn(
        "pb_imc_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getTbScreeningSectionC(false, 12, 24, 2, 4),
        endDateMappings,
        new NotApplicableIfNullConverter());

    // C15 - Rastreado para Tensão Arterial em todas as consultas entre 12˚ e 24˚ mês de TARV?
    pdd.addColumn(
        "arterial_pressure_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientWithArterialPressure(
            12, 24, false, 2, 4),
        endDateMappings,
        new NotApplicableIfNullConverter());

    // C16 - Identificação de n˚ de consultas clínicas entre 6˚ e 12˚ mês de TARV Coluna AJ
    pdd.addColumn(
        "clinical_consultations_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getNrClinicalConsultations(12, 24, 2, 4),
        endDateMappings);

    // C17 - N˚ de consultas de APSS/PP tre 12˚ e 24˚ mês de TARV - Coluna BT
    pdd.addColumn(
        "apss_pp_consultations_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getNrApssPpConsultations(12, 24, 2, 4),
        endDateMappings);

    // C18 - Rastreado para CACUM entre 12˚ e 24˚ mês de TARV?
    pdd.addColumn(
        "cacum_screening_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsWithCacumScreening(12, 24, 2, 4),
        endDateMappings,
        new NotApplicableIfNullConverter());

    // C19 - Resultado positivo para CACUM entre 12˚ e 24˚ mês de TARV?
    pdd.addColumn(
        "positive_cacum_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsWithPositiveCacum(12, 24, 2, 4),
        endDateMappings,
        new NotApplicableIfNullConverter());

    // C20 - Estado de permanência no 24˚ mês de TARV: (coluna BU)
    pdd.addColumn(
        "permanence_state_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getLastStateOfStayOnTarv(0, 2),
        c20Mappings);

    // D.1 - Data do pedido da CV de seguimento - D.1 (coluna BV)
    pdd.addColumn(
        "cv_date_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getLastViralLoadOnThePeriod(24, 36, 3, 4),
        endDateMappings,
        new GeneralDateConverter());

    // D.2 - Data de registo do resultado da CV de Seguimento - D.2 (coluna BV)
    pdd.addColumn(
        "cv_result_date_d",
        listOfPatientsWithMdsEvaluationCohortQueries
            .getLastViralLoadResultDateBetweenPeriodsInMonthsAfterTarv(24, 36, 3, 4),
        endDateMappings,
        new GeneralDateConverter());

    // D.3 - Resultado da CV de Seguimento - D.3 (coluna BX)
    pdd.addColumn(
        "cv_result_d",
        listOfPatientsWithMdsEvaluationCohortQueries
            .getSecondViralLoadResultBetweenPeriodsOfMonthsAfterTarv(24, 36, 3, 4),
        endDateMappings,
        new ViralLoadQualitativeLabelConverter());

    // D.4 - Resultado do CD4 feito entre 24˚ e 36˚ mês de TARV- D.4 (Coluna BY)
    pdd.addColumn(
        "cd4_result_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getCd4ResultSectionC(24, 36, 3, 4),
        endDateMappings);

    // D.5- Teve registo de boa adesão em TODAS consultas entre 24˚ e 36˚ mês de TARV?
    pdd.addColumn(
        "good_adherence_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsWithGoodAdhesion(
            false, 24, 36, 3, 4),
        endDateMappings);

    // D.6 - Esteve grávida ou foi lactante entre 24˚ e 36º mês de TARV?: (coluna CA)
    pdd.addColumn(
        "pregnant_breastfeeding_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsPregnantBreastfeeding3MonthsTarv(
            24, 36, false, 3, 4),
        endDateMappings);

    // D7 - Condição Clínica Activa de Estadio III ou IV entre 24º e 36º mês de TARV
    pdd.addColumn(
        "clinical_condiction_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getActiveClinicalCondiction(
            24, 36, false, 3, 4),
        endDateMappings,
        new NotApplicableIfNullConverter());

    // D.8 - Teve TB entre 24˚ e 36 ˚ meses de TARV: (coluna CC)
    pdd.addColumn(
        "tb_tarv_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsWithTbThirdToNineMonth(
            24, 36, false, 3, 4),
        endDateMappings);

    // D.9 - Data de inscrição em algum MDS entre 24º e 36º mês de TARV: (coluna CD)
    pdd.addColumn(
        "mds_tarv_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getMdsDate(24, 36, false, 3, 4),
        endDateMappings,
        new GeneralDateConverter());

    // D10.1 -Tipo de MDS: (MDS1) Coluna CE
    pdd.addColumn(
        "mds_one_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds1(36, 3, 4),
        endDateMappings,
        new DispensationTypeMdcConverter());

    // D10.2 - Data Início de MDS1: Coluna CF
    pdd.addColumn(
        "mds_one_start_date_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds1StartDate(36, 3, 4),
        endDateMappings,
        new GeneralDateConverter());

    // D10.3 - Data Fim de MDS1: Coluna CG
    pdd.addColumn(
        "mds_one_end_date_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds1EndDate(36, 3, 4),
        endDateMappings,
        new GeneralDateConverter());

    // D10.4 - Tipo de MDS: (MDS2) Coluna CH
    pdd.addColumn(
        "mds_two_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds2(36, 3, 4),
        endDateMappings,
        new DispensationTypeMdcConverter());

    // D10.5 - Data Início de MDS2: Coluna CI
    pdd.addColumn(
        "mds_two_start_date_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds2StartDate(36, 3, 4),
        endDateMappings,
        new GeneralDateConverter());

    // D10.6 - Data Fim de MDS2: Coluna CJ
    pdd.addColumn(
        "mds_two_end_date_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds2EndDate(36, 3, 4),
        endDateMappings,
        new GeneralDateConverter());

    // D10.7 - Tipo de MDS: (MDS3) Coluna CK
    pdd.addColumn(
        "mds_three_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds3(36, 3, 4),
        endDateMappings,
        new DispensationTypeMdcConverter());

    // D10.8 - Data Início de MDS3: Coluna CL
    pdd.addColumn(
        "mds_three_start_date_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds3StartDate(36, 3, 4),
        endDateMappings,
        new GeneralDateConverter());

    // D10.9 - Data Fim de MDS3: Coluna CM
    pdd.addColumn(
        "mds_three_end_date_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds3EndDate(36, 3, 4),
        endDateMappings,
        new GeneralDateConverter());

    // D10.10 - Tipo de MDS: (MDS4) Coluna CN
    pdd.addColumn(
        "mds_four_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds4(36, 3, 4),
        endDateMappings,
        new DispensationTypeMdcConverter());

    // D10.11 - Data Início de MDS4: Coluna CO
    pdd.addColumn(
        "mds_four_start_date_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds4StartDate(36, 3, 4),
        endDateMappings,
        new GeneralDateConverter());

    // D10.12 - Data Fim de MDS4: Coluna CP
    pdd.addColumn(
        "mds_four_end_date_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds4EndDate(36, 3, 4),
        endDateMappings,
        new GeneralDateConverter());

    // D10.13 - Tipo de MDS: (MDS5) Coluna CQ
    pdd.addColumn(
        "mds_five_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds5(36, 3, 4),
        endDateMappings,
        new DispensationTypeMdcConverter());

    // D10.14 - Data Início de MDS5: Coluna CR
    pdd.addColumn(
        "mds_five_start_date_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds5StartDate(36, 3, 4),
        endDateMappings,
        new GeneralDateConverter());

    // D10.15 - Data Fim de MDS5: Coluna CS
    pdd.addColumn(
        "mds_five_end_date_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds5EndDate(36, 3, 4),
        endDateMappings,
        new GeneralDateConverter());

    // D11 - Rastreado para TB em TODAS as consultas entre 24˚ e 36˚ mês de TARV?- D.11 (Coluna CT)
    pdd.addColumn(
        "tb_screening_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getTbScreeningSectionC(true, 24, 36, 3, 4),
        endDateMappings);

    // D12 - Recebeu uma forma de PF entre 24˚ e 36˚ mês de TARV?
    pdd.addColumn(
        "family_planning_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsWhoReceivedPf12to24MonthTarv(
            24, 36, 3, 4),
        endDateMappings,
        new NotApplicableIfNullConverter());

    // D13 - Recebeu TPT entre 24˚ e 36˚ mês de TARV?
    pdd.addColumn(
        "received_tpt_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsWhoReceivedTpt(24, 36, false, 3, 4),
        endDateMappings,
        new NotApplicableIfNullConverter());

    // D14 - PB/IMC registado em TODAS as consultas entre o 24˚ a 36º mês de TARV?- D.14 (Coluna CW)
    pdd.addColumn(
        "pb_imc_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getTbScreeningSectionC(false, 24, 36, 4, 3),
        endDateMappings,
        new NotApplicableIfNullConverter());

    // D15 - Rastreado para Tensão Arterial em todas as consultas entre 24˚ e 36˚ mês de TARV?
    pdd.addColumn(
        "arterial_pressure_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientWithArterialPressure(
            24, 36, false, 3, 4),
        endDateMappings,
        new NotApplicableIfNullConverter());

    // D16 - N˚ de consultas clínicas entre 24˚ e 36˚ mês de TARV- D.16 (Coluna CY)
    pdd.addColumn(
        "clinical_consultations_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getNrClinicalConsultations(24, 36, 3, 4),
        endDateMappings);

    // D17 - N˚ de consultas de APSS/PP tre 24˚ e 36˚ mês de TARV- D.18 (Coluna CZ)
    pdd.addColumn(
        "apss_pp_consultations_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getNrApssPpConsultations(24, 36, 3, 4),
        endDateMappings);

    // D18 - Rastreado para CACUM entre 24˚ e 36˚ mês de TARV?
    pdd.addColumn(
        "cacum_screening_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsWithCacumScreening(24, 36, 3, 4),
        endDateMappings,
        new NotApplicableIfNullConverter());

    // D19 - Resultado positivo para CACUM entre 24˚ e 36˚ mês de TARV?
    pdd.addColumn(
        "positive_cacum_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsWithPositiveCacum(24, 36, 3, 4),
        endDateMappings,
        new NotApplicableIfNullConverter());

    // D20 - Estado de permanência no 36˚ mês de TARV: (coluna DA)
    pdd.addColumn(
        "permanence_state_d",
        listOfPatientsWithMdsEvaluationCohortQueries.getLastStateOfStayOnTarv(0, 1),
        d20Mappings);

    return pdd;
  }

  @Override
  public List<Parameter> getParameters() {
    return Arrays.asList(
        new Parameter("evaluationYear", "Ano de Avaliação", Integer.class),
        new Parameter("location", "Unidade Sanitária", Location.class));
  }
}
