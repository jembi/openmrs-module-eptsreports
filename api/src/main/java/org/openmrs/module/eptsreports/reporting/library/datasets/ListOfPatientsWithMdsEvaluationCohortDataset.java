package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.Arrays;
import java.util.List;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
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
  private ListOfPatientsArtCohortCohortQueries listOfPatientsArtCohortCohortQueries;

  private TPTListOfPatientsEligibleDataSet tptListOfPatientsEligibleDataSet;

  private TPTInitiationDataDefinitionQueries tptInitiationDataDefinitionQueries;

  private final DQACargaViralCohortQueries dQACargaViralCohortQueries;

  private final TbMetadata tbMetadata;

  private ListChildrenOnARTandFormulationsDataset listChildrenOnARTandFormulationsDataset;

  private ListOfPatientsDefaultersOrIITCohortQueries listOfPatientsDefaultersOrIITCohortQueries;

  @Autowired
  public ListOfPatientsWithMdsEvaluationCohortDataset(
      ListOfPatientsWithMdsEvaluationCohortQueries listOfPatientsWithMdsEvaluationCohortQueries,
      ListOfPatientsArtCohortCohortQueries listOfPatientsArtCohortCohortQueries,
      TPTListOfPatientsEligibleDataSet tptListOfPatientsEligibleDataSet,
      TPTInitiationDataDefinitionQueries tptInitiationDataDefinitionQueries,
      DQACargaViralCohortQueries dQACargaViralCohortQueries,
      TbMetadata tbMetadata,
      ListOfPatientsDefaultersOrIITCohortQueries listOfPatientsDefaultersOrIITCohortQueries,
      ListChildrenOnARTandFormulationsDataset listChildrenOnARTandFormulationsDataset) {
    this.listOfPatientsWithMdsEvaluationCohortQueries =
        listOfPatientsWithMdsEvaluationCohortQueries;
    this.listOfPatientsArtCohortCohortQueries = listOfPatientsArtCohortCohortQueries;
    this.tptListOfPatientsEligibleDataSet = tptListOfPatientsEligibleDataSet;
    this.tptInitiationDataDefinitionQueries = tptInitiationDataDefinitionQueries;
    this.dQACargaViralCohortQueries = dQACargaViralCohortQueries;
    this.tbMetadata = tbMetadata;
    this.listOfPatientsDefaultersOrIITCohortQueries = listOfPatientsDefaultersOrIITCohortQueries;
    this.listChildrenOnARTandFormulationsDataset = listChildrenOnARTandFormulationsDataset;
  }

  public DataSetDefinition contructDataset() {

    PatientDataSetDefinition pdd = new PatientDataSetDefinition();
    pdd.setName("MDS");
    pdd.setParameters(getParameters());

    pdd.addRowFilter(
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsInitiatedART12Or24Months(),
        "evaluationYear=${evaluationYear},location=${location}");

    //  SECÇÃO A
    //  INFORMAÇÃO DO PACIENTE

    // A1- Nr. Sequencial: (Coluna A)
    pdd.addColumn("counter", new PersonIdDataDefinition(), "", new ObjectCounterConverter());

    // A2- Coorte: (coluna B)
    pdd.addColumn(
        "coort",
        listOfPatientsWithMdsEvaluationCohortQueries.getCoort12Or24Months(),
        "evaluationYear=${evaluationYear},location=${location}",
        null);

    // A3- Sexo: (coluna C)
    pdd.addColumn("gender", new GenderDataDefinition(), "", new MaleFemaleConverter());

    // A4- Idade: (coluna D)
    pdd.addColumn(
        "age",
        listOfPatientsWithMdsEvaluationCohortQueries.getAgeOnMOHArtStartDate(),
        "evaluationYear=${evaluationYear},location=${location}",
        null);

    // A5- Data início TARV: (coluna E)
    pdd.addColumn(
        "art_start",
        listOfPatientsWithMdsEvaluationCohortQueries.getArtStartDate(),
        "evaluationYear=${evaluationYear},location=${location}",
        null);

    // A6- Elegível ao TPT no Início do TARV: (coluna F)
    pdd.addColumn(
        "tpt_eligible_tarv",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsTptNotEligible(),
        "evaluationYear=${evaluationYear},location=${location}",
        null);

    // A7- Data de início do TPT: (coluna G)
    pdd.addColumn(
        "tpt_start_date",
        listOfPatientsWithMdsEvaluationCohortQueries.getTptInitiationDate(),
        "evaluationYear=${evaluationYear},location=${location}",
        null);

    // A8- Data de registo do resultado de CD4 inicial: (coluna H)
    pdd.addColumn(
        "cd4_register_date",
        listOfPatientsWithMdsEvaluationCohortQueries.getCd4ResultDate(),
        "evaluationYear=${evaluationYear},location=${location}",
        null);

    // A9- Resultado do CD4 Inicial: (coluna I)
    pdd.addColumn(
        "initial_cd4_result",
        listOfPatientsWithMdsEvaluationCohortQueries.getCd4Result(),
        "evaluationYear=${evaluationYear},location=${location}",
        null);

    //  SECÇÃO B
    //  12 MESES DEPOIS DO INÍCIO DO TARV: ELIGIBILIDADE A MDS

    // B.1 - Data do pedido da 1ª CV - Sheet 1: Column J
    pdd.addColumn(
        "first_cv_date",
        listOfPatientsWithMdsEvaluationCohortQueries.getFirstViralLoad(),
        "evaluationYear=${evaluationYear},location=${location}",
        null);

    // B.2 - Data de registo do resultado da 1ª CV - Sheet 1: Column K
    pdd.addColumn(
        "first_cv_result_date",
        listOfPatientsWithMdsEvaluationCohortQueries.getFirstViralLoadResultDate(),
        "evaluationYear=${evaluationYear},location=${location}",
        null);

    // B.3 - Resultado da 1ª CV - Sheet 1: Column L
    pdd.addColumn(
        "first_cv_result",
        listOfPatientsWithMdsEvaluationCohortQueries.getFirstViralLoadResult(),
        "evaluationYear=${evaluationYear},location=${location}",
        null);

    // B.4 - Resultado do 2˚ CD4 (2˚ CD4 feito nos 1˚s 12 meses de TARV) - Sheet 1: Column M
    pdd.addColumn(
        "second_cd4_result",
        listOfPatientsWithMdsEvaluationCohortQueries.getSecondCd4Result(),
        "evaluationYear=${evaluationYear},location=${location}",
        null);

    // B5- Teve registo de boa adesão em TODAS consultas entre 1˚ e 3˚ mês de TARV?
    pdd.addColumn(
        "good_adherence_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsWithGoodAdhesion(),
        "evaluationYear=${evaluationYear},location=${location}",
        null);

    // B6- Esteve grávida ou foi lactante entre 3˚ e 9º mês de TARV?: (coluna M)- Resposta = Sim ou
    // Não (RF22)
    pdd.addColumn(
        "pregnant_breastfeeding_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsPregnantBreastfeeding3MonthsTarv(
            3, 9),
        "evaluationYear=${evaluationYear},location=${location}",
        null);

    // B8- Teve TB nos 1˚s 12 meses de TARV: (coluna Q) - Resposta = Sim ou Não (RF23)
    pdd.addColumn(
        "tb_tarv_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsWithTbThirdToNineMonth(3, 9),
        "evaluationYear=${evaluationYear},location=${location}",
        null);

    // B9- Data de inscrição no MDS: (coluna R) - Resposta = Data de Inscrição (RF24)
    pdd.addColumn(
        "mds_date",
        listOfPatientsWithMdsEvaluationCohortQueries.getMdsDate(3, 9),
        "evaluationYear=${evaluationYear},location=${location}",
        null);

    // B10 -Tipo de MDS: (MDS1) Coluna S
    pdd.addColumn(
        "mds_one_b",
        listOfPatientsWithMdsEvaluationCohortQueries.getMds1(),
        "evaluationYear=${evaluationYear},location=${location}",
        new DispensationTypeMdcConverter());

    // B10 - Data de MDS1: Coluna T
    pdd.addColumn(
            "mds_one_start_date_b",
            listOfPatientsWithMdsEvaluationCohortQueries.getMds1StartDate(),
            "evaluationYear=${evaluationYear},location=${location}",
            null);

    // C1- Data do pedido da CV de seguimento: (coluna AP)
    pdd.addColumn(
        "cv_date_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getSecondtViralLoad(),
        "evaluationYear=${evaluationYear},location=${location}",
        null);

    // C4 - Resultado do CD4 feito entre 12˚ e 24˚ mês de TARV- C.4 (Coluna AS)
    pdd.addColumn(
        "cd4_result_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getCd4ResultSectionC(),
        "evaluationYear=${evaluationYear},location=${location}",
        null);

    // C5- Teve registo de boa adesão em TODAS consultas entre 12˚ e 24˚ mês de TARV?
    pdd.addColumn(
        "good_adherence_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsWithGoodAdhesionAfterAYearInTarv(),
        "evaluationYear=${evaluationYear},location=${location}",
        null);

    // C6 - Esteve grávida ou foi lactante entre 12˚ e 24º mês de TARV?: (coluna AU) - Resposta =
    // Sim ou Não (RF37)
    pdd.addColumn(
        "pregnant_breastfeeding_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsPregnantBreastfeeding3MonthsTarv(
            12, 24),
        "evaluationYear=${evaluationYear},location=${location}",
        null);

    // C8 - Teve TB entre 12˚ e 24 ˚ meses de TARV: (coluna AW) - Resposta = Sim ou Não (RF38)
    pdd.addColumn(
        "tb_tarv_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsWithTbThirdToNineMonth(12, 24),
        "evaluationYear=${evaluationYear},location=${location}",
        null);

    // C9 - Data de inscrição no MDS entre 12º e 24º mês de TAV: (coluna AX) - Resposta = Data de
    // Inscrição (RF39)
    pdd.addColumn(
        "mds_tarv_c",
        listOfPatientsWithMdsEvaluationCohortQueries.getMdsDate(12, 24),
        "evaluationYear=${evaluationYear},location=${location}",
        null);

    return pdd;
  }

  @Override
  public List<Parameter> getParameters() {
    return Arrays.asList(
        new Parameter("evaluationYear", "Ano de Avaliação", Integer.class),
        new Parameter("location", "Unidade Sanitária", Location.class));
  }
}
