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
  private String b18Mappings = "endDate=${evaluationYear-1}-06-20,location=${location}";

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
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsWithGoodAdhesion(true, 1, 3),
        // minNumberOfMonths and MaxNumberOfMonths has no effect here because the boolean b5OrC5 is
        // set to true
        endDateMappings);

    // B6- Esteve grávida ou foi lactante entre 3˚ e 9º mês de TARV?: (coluna M)- Resposta = Sim ou
    // Não (RF22)
    pdd.addColumn(
        "pregnant_breastfeeding_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsPregnantBreastfeeding3MonthsTarv(
            3, 9, true),
        endDateMappings);

    // B7 - Condição Clínica Activa de Estadio III ou IV entre a Data Inscrição no MDS e 12º mês de
    // TARV
    pdd.addColumn(
        "clinical_condiction_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getActiveClinicalCondiction(0, 12, true),
        endDateMappings,
        new NotApplicableIfNullConverter());

    // B8- Teve TB nos 1˚s 12 meses de TARV: (coluna Q) - Resposta = Sim ou Não (RF23)
    pdd.addColumn(
        "tb_tarv_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsWithTbThirdToNineMonth(3, 9, true),
        endDateMappings);

    // B9- Data de inscrição no MDS: (coluna R) - Resposta = Data de Inscrição (RF24)
    pdd.addColumn(
        "mds_date",
        listOfPatientsWithMdsEvaluationCohortQueries.getMdsDate(0, 12, true),
        endDateMappings,
        new GeneralDateConverter());

    // B10.1 -Tipo de MDS: (MDS1) Coluna S
    pdd.addColumn(
        "mds_one_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds1(12),
        endDateMappings,
        new DispensationTypeMdcConverter());

    // B10.2 - Data Início de MDS1: Coluna T
    pdd.addColumn(
        "mds_one_start_date_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds1StartDate(12),
        endDateMappings,
        new GeneralDateConverter());

    // B10.3 - Data Fim de MDS1: Coluna U
    pdd.addColumn(
        "mds_one_end_date_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds1EndDate(12),
        endDateMappings,
        new GeneralDateConverter());

    // B10.4 - Tipo de MDS: (MDS2) Coluna V
    pdd.addColumn(
        "mds_two_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds2(12),
        endDateMappings,
        new DispensationTypeMdcConverter());

    // B10.5 - Data Início de MDS2: Coluna W
    pdd.addColumn(
        "mds_two_start_date_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds2StartDate(12),
        endDateMappings,
        new GeneralDateConverter());

    // B10.6 - Data Fim de MDS2: Coluna X
    pdd.addColumn(
        "mds_two_end_date_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds2EndDate(12),
        endDateMappings,
        new GeneralDateConverter());

    // B10.7 - Tipo de MDS: (MDS3) Coluna Y
    pdd.addColumn(
        "mds_three_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds3(12),
        endDateMappings,
        new DispensationTypeMdcConverter());

    // B10.8 - Data Início de MDS3: Coluna Z
    pdd.addColumn(
        "mds_three_start_date_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds3StartDate(12),
        endDateMappings,
        new GeneralDateConverter());

    // B10.9 - Data Fim de MDS3: Coluna AA
    pdd.addColumn(
        "mds_three_end_date_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds3EndDate(12),
        endDateMappings,
        new GeneralDateConverter());

    // B10.10 - Tipo de MDS: (MDS4) Coluna AB
    pdd.addColumn(
        "mds_four_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds4(12),
        endDateMappings,
        new DispensationTypeMdcConverter());

    // B10.11 - Data Início de MDS4: Coluna AC
    pdd.addColumn(
        "mds_four_start_date_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds4StartDate(12),
        endDateMappings,
        new GeneralDateConverter());

    // B10.12 - Data Fim de MDS4: Coluna AD
    pdd.addColumn(
        "mds_four_end_date_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds4EndDate(12),
        endDateMappings,
        new GeneralDateConverter());

    // B10.13 - Tipo de MDS: (MDS5) Coluna AE
    pdd.addColumn(
        "mds_five_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds5(12),
        endDateMappings,
        new DispensationTypeMdcConverter());

    // B10.14 - Data Início de MDS5: Coluna AF
    pdd.addColumn(
        "mds_five_start_date_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds5StartDate(12),
        endDateMappings,
        new GeneralDateConverter());

    // B10.15 - Data Fim de MDS5: Coluna AG
    pdd.addColumn(
        "mds_five_end_date_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds5EndDate(12),
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
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsWhoReceivedPf(0, 12, true),
        endDateMappings,
        new NotApplicableIfNullConverter());

    // B13 - Recebeu TPT entre a data de inscrição no MDS e 12˚ mês de TARV?
    pdd.addColumn(
        "received_tpt_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsWhoReceivedTpt(0, 12, true),
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
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientWithArterialPressure(0, 12, true),
        endDateMappings,
        new NotApplicableIfNullConverter());

    // B16 - Identificação de n˚ de consultas clínicas entre 6˚ e 12˚ mês de TARV Coluna AM
    pdd.addColumn(
        "clinical_consultations_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getNrClinicalConsultations(6, 12),
        endDateMappings);

    // B17 - N˚ de consultas de APSS/PP entre 6˚ e 12˚ mês de TARV - Coluna AN
    pdd.addColumn(
        "apss_pp_consultations_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getNrApssPpConsultations(6, 12),
        endDateMappings);

    // B18 - Rastreado para CACUM entre o 1˚ e 12 meses de TARV?
    pdd.addColumn(
        "cacum_screening_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsWithCacumScreening(0, 12),
        endDateMappings,
        new NotApplicableIfNullConverter());

    // B19 - Resultado positivo para CACUM entre o 1˚ e 12 meses de TARV?
    pdd.addColumn(
        "positive_cacum_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsWithPositiveCacum(0, 12),
        endDateMappings,
        new NotApplicableIfNullConverter());

    // B20 - Estado de permanência no 12˚ mês de TARV: (coluna AO)
    pdd.addColumn(
        "permanence_state_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getLastStateOfStayOnTarv(),
        b18Mappings);

    return pdd;
  }

  @Override
  public List<Parameter> getParameters() {
    return Arrays.asList(
        new Parameter("evaluationYear", "Ano de Avaliação", Integer.class),
        new Parameter("location", "Unidade Sanitária", Location.class));
  }
}
