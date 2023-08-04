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
  private ListOfPatientsArtCohortCohortQueries listOfPatientsArtCohortCohortQueries;

  private TPTListOfPatientsEligibleDataSet tptListOfPatientsEligibleDataSet;

  private TPTInitiationDataDefinitionQueries tptInitiationDataDefinitionQueries;

  private final DQACargaViralCohortQueries dQACargaViralCohortQueries;

  private ListChildrenOnARTandFormulationsDataset listChildrenOnARTandFormulationsDataset;

  private ListOfPatientsDefaultersOrIITCohortQueries listOfPatientsDefaultersOrIITCohortQueries;

  @Autowired
  public ListOfPatientsWithMdsEvaluationCohortDataset(
      ListOfPatientsWithMdsEvaluationCohortQueries listOfPatientsWithMdsEvaluationCohortQueries,
      ListOfPatientsArtCohortCohortQueries listOfPatientsArtCohortCohortQueries,
      TPTListOfPatientsEligibleDataSet tptListOfPatientsEligibleDataSet,
      TPTInitiationDataDefinitionQueries tptInitiationDataDefinitionQueries,
      DQACargaViralCohortQueries dQACargaViralCohortQueries,
      ListOfPatientsDefaultersOrIITCohortQueries listOfPatientsDefaultersOrIITCohortQueries,
      ListChildrenOnARTandFormulationsDataset listChildrenOnARTandFormulationsDataset) {
    this.listOfPatientsWithMdsEvaluationCohortQueries =
        listOfPatientsWithMdsEvaluationCohortQueries;
    this.listOfPatientsArtCohortCohortQueries = listOfPatientsArtCohortCohortQueries;
    this.tptListOfPatientsEligibleDataSet = tptListOfPatientsEligibleDataSet;
    this.tptInitiationDataDefinitionQueries = tptInitiationDataDefinitionQueries;
    this.dQACargaViralCohortQueries = dQACargaViralCohortQueries;
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

    // A.1 - Nr Sequencial sheet 1 - Column A
    pdd.addColumn("counter", new PersonIdDataDefinition(), "", new ObjectCounterConverter());

    // A.2 - Coorte - Sheet 1: Column B
    pdd.addColumn(
        "coort",
        listOfPatientsWithMdsEvaluationCohortQueries.getCoort12Or24Months(),
        "evaluationYear=${evaluationYear},location=${location}",
        null);

    // A.3 - Sexo - Sheet 1: Column C
    pdd.addColumn("gender", new GenderDataDefinition(), "", new MaleFemaleConverter());

    // A.4 - Idade - Sheet 1: Column D
    pdd.addColumn(
        "age",
        listOfPatientsWithMdsEvaluationCohortQueries.getAgeOnMOHArtStartDate(),
        "evaluationYear=${evaluationYear},location=${location}",
        null);

    // A.5 - Data do início TARV - Sheet 1: Column E
    pdd.addColumn(
        "art_start",
        listOfPatientsWithMdsEvaluationCohortQueries.getArtStartDate(),
        "evaluationYear=${evaluationYear},location=${location}",
        null);

    // A.6 - Elegível ao TPT no início do TARV - Sheet 1: Column F
    pdd.addColumn(
        "tpt_eligible_tarv",
        listOfPatientsWithMdsEvaluationCohortQueries.getPatientsTptNotEligible(),
        "evaluationYear=${evaluationYear},location=${location}",
        null);

    // A.7 - Data de início do TPT - Sheet 1: Column G
    pdd.addColumn(
        "tpt_start_date",
        listOfPatientsWithMdsEvaluationCohortQueries.getTptInitiationDate(),
        "evaluationYear=${evaluationYear},location=${location}",
        null);

    // A.8 - Data de registo do resultado do CD4 inicial - Sheet 1: Column H
    pdd.addColumn(
        "cd4_register_date",
        listOfPatientsWithMdsEvaluationCohortQueries.getCd4ResultDate(),
        "evaluationYear=${evaluationYear},location=${location}",
        null);

    // A.9 - Resultado do CD4 inicial - Sheet 1: Column I
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
        "evaluationYear=${evaluationYear-1},location=${location}",
        null);

    // B.2 - Data de registo do resultado da 1ª CV - Sheet 1: Column K
    pdd.addColumn(
        "first_cv_result_date",
        listOfPatientsWithMdsEvaluationCohortQueries.getFirstViralLoadResultDate(),
        "evaluationYear=${evaluationYear-1},location=${location}",
        null);

    // B.3 - Resultado da 1ª CV - Sheet 1: Column L
    pdd.addColumn(
        "first_cv_result",
        listOfPatientsWithMdsEvaluationCohortQueries.getFirstViralLoadResult(),
        "evaluationYear=${evaluationYear-1},location=${location}",
        null);

    // B.4 - Resultado do 2˚ CD4 (2˚ CD4 feito nos 1˚s 12 meses de TARV) - Sheet 1: Column M
    pdd.addColumn(
        "second_cd4_result",
        listOfPatientsWithMdsEvaluationCohortQueries.getSecondCd4Result(),
        "evaluationYear=${evaluationYear-1},location=${location}",
        null);

    // C.18 - Estado de permanência no 24˚ mês de TARV - Sheet 1: Column BU
    pdd.addColumn(
        "permanence_state_c",
        tptInitiationDataDefinitionQueries.getPatientsAndARTStartDate(),
        "startDate=${startDate},endDate=${endDate},location=${location}",
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
