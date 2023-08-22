package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.Arrays;
import java.util.Collections;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.eptsreports.reporting.data.converter.*;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ListOfPatientsArtCohortCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.advancedhivillness.ListOfPatientsInAdvancedHivIllnessCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.BirthdateConverter;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.person.definition.*;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsInAdvancedHivIllnessDataset extends BaseDataSet {

  private final ListOfPatientsArtCohortCohortQueries listOfPatientsArtCohortCohortQueries;

  private final TPTListOfPatientsEligibleDataSet tptListOfPatientsEligibleDataSet;
  private final ListOfPatientsInAdvancedHivIllnessCohortQueries
      listOfPatientsInAdvancedHivIllnessCohortQueries;

  private final TbMetadata tbMetadata;

  private final HivMetadata hivMetadata;

  private final CommonMetadata commonMetadata;

  private EptsGeneralIndicator eptsGeneralIndicator;

  String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";

  @Autowired
  public ListOfPatientsInAdvancedHivIllnessDataset(
      ListOfPatientsArtCohortCohortQueries listOfPatientsArtCohortCohortQueries,
      TPTListOfPatientsEligibleDataSet tptListOfPatientsEligibleDataSet,
      ListOfPatientsInAdvancedHivIllnessCohortQueries
          listOfPatientsInAdvancedHivIllnessCohortQueries,
      TbMetadata tbMetadata,
      HivMetadata hivMetadata,
      CommonMetadata commonMetadata,
      EptsGeneralIndicator eptsGeneralIndicator) {
    this.listOfPatientsArtCohortCohortQueries = listOfPatientsArtCohortCohortQueries;
    this.tptListOfPatientsEligibleDataSet = tptListOfPatientsEligibleDataSet;
    this.listOfPatientsInAdvancedHivIllnessCohortQueries =
        listOfPatientsInAdvancedHivIllnessCohortQueries;
    this.tbMetadata = tbMetadata;
    this.hivMetadata = hivMetadata;
    this.commonMetadata = commonMetadata;
    this.eptsGeneralIndicator = eptsGeneralIndicator;
  }

  public DataSetDefinition listOfPatientsInAdvancedHivIllnessColumnsDataset() {

    PatientDataSetDefinition pdd = new PatientDataSetDefinition();

    pdd.setName("DAH");

    PatientIdentifierType identifierType =
        Context.getPatientService()
            .getPatientIdentifierTypeByUuid("e2b966d0-1d5f-11e0-b929-000c29ad1d07");

    PersonAttributeType contactAttributeType =
        Context.getPersonService()
            .getPersonAttributeTypeByUuid("e2e3fd64-1d5f-11e0-b929-000c29ad1d07");

    DataDefinition contactDef =
        new ConvertedPersonDataDefinition(
            "contact",
            new PersonAttributeDataDefinition(contactAttributeType.getName(), contactAttributeType),
            null);

    DataConverter formatter = new ObjectFormatter("{familyName}, {givenName}");

    DataDefinition nameDef =
        new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), formatter);

    pdd.setParameters(getParameters());

    pdd.addRowFilter(
        listOfPatientsInAdvancedHivIllnessCohortQueries
            .getPatientsOnFollowupOrWithCriteriaToStartFollowupOfDAH(),
        mappings);

    // 1- NID sheet 1 - Column A
    pdd.addColumn(
        "nid",
        tptListOfPatientsEligibleDataSet.getNID(identifierType.getPatientIdentifierTypeId()),
        "");

    // 2 - Name - Sheet 1: Column B
    pdd.addColumn("name", nameDef, "");

    // 3 - Data Nascimento - Sheet 1: Column C
    pdd.addColumn(
        "birthdate", new BirthdateDataDefinition(), "", new BirthdateConverter("dd/MM/yyyy"));

    // 4 - Idade - Sheet 1: Column D
    pdd.addColumn(
        "age",
        listOfPatientsArtCohortCohortQueries.getAge(),
        "evaluationDate=${endDate}",
        new NotApplicableIfNullConverter());

    // 5 - Sexo - Sheet 1: Column E
    pdd.addColumn("gender", new GenderDataDefinition(), "", new GenderConverter());

    // 6 - Contacto - Sheet 1: Column F
    pdd.addColumn("contact", contactDef, "");

    // 7 - Data Inicio Tarv - Sheet 1: Column G
    pdd.addColumn(
        "art_start",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getARTStartDate(),
        "endDate=${endDate},location=${location}",
        new ForwardSlashDateConverter());

    // 8 - Data de Último Levantamento TARV - Sheet 1: Column H
    pdd.addColumn(
        "last_pickup",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getLastARVPickupDate(),
        mappings,
        new ForwardSlashDateConverter());

    // 9 - Transferido de Outra US- Sheet 1: Column I
    pdd.addColumn(
        "transferred_in",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getPatientsTransferredInByTheEndOfPeriod(),
        "endDate=${endDate},location=${location}",
        new EmptyToNaoAndAnyToSimConverter());
    // 10 - Último Estado de Permanência TARV- Sheet 1: Column J

    // 11 - Situação TARV no Início do Seguimento de DAH Sheet 1: K
    pdd.addColumn(
        "last_situation",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getLastARVSituation(),
        "endDate=${endDate},location=${location}",
        new ObservationToConceptNameConverter());

    // 12 - Data de Início de Seguimento de DAH Sheet 1: Column L
    pdd.addColumn(
        "followup_startdate",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getFollowupStartDateDAH(),
        "endDate=${endDate},location=${location}",
        new ForwardSlashDateConverter());

    // 13 - Data de Registo de CD4 Absoluto – Sheet 1: Column M
    pdd.addColumn(
        "cd4_resultdate",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getCd4ResultDate(),
        mappings,
        new ForwardSlashDateConverter());

    // 14 - Resultado de CD4 – Sheet 1: Column N
    pdd.addColumn(
        "cd4_result", listOfPatientsInAdvancedHivIllnessCohortQueries.getCd4Result(), mappings);

    // 15 - Data de Registo de Estadio – Sheet 1: Column O
    pdd.addColumn(
        "estadio_date",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getDateOfEstadioOnPeriod(),
        mappings,
        new ForwardSlashDateConverter());

    // 16 - Infecções Estadio OMS – Sheet 1: Column P
    pdd.addColumn(
        "estadio_result",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getResultOfEstadioDuringPeriod(),
        mappings,
        new ObservationToConceptNameConverter());

    //     17 - Data de Registo mais recente de Estadio OMS -  Column Q
    pdd.addColumn(
        "last_estadio_date",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getDateOfMostRecentEstadioByEndOfPeriod(),
        "endDate=${endDate},location=${location}",
        new ForwardSlashDateConverter());

    // 18 - Estadio OMS no registo mais recente ate o fim do periodo -  Column R
    pdd.addColumn(
        "last_estadio_result",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getResultOfMostRecentEstadioByEndOfPeriod(),
        "endDate=${endDate},location=${location}",
        new ObservationToConceptNameConverter());

    // 19 - Motivo de Mudança de Estadiamento Clínico - 1 – Sheet 1: Column S
    pdd.addColumn(
        "reason_change_estadio",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getReasonToChangeEstadio1(),
        "endDate=${endDate},location=${location}",
        new ObservationToConceptNameConverter());

    // 20 - Motivo de Mudança de Estadiamento Clínico - 2 – Sheet 1: Column T
    pdd.addColumn(
        "reason_change_estadio2",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getReasonToChangeEstadio2(),
        "endDate=${endDate},location=${location}",
        new ObservationToConceptNameConverter());

    // 21 - Resultado do Último CD4 – Sheet 1: Column U
    pdd.addColumn(
        "last_cd4_result",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getLastCd4Result(),
        mappings);

    // 22 - Data do Último CD4 – Sheet 1: Column V
    pdd.addColumn(
        "last_cd4_resultdate",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getLastCd4ResultDate(),
        mappings,
        new ForwardSlashDateConverter());

    // 23 - Resultado do Penúltimo CD4 – Sheet 1: Column W
    pdd.addColumn(
        "second_cd4_result",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getLastCd4ResultBeforeMostRecentCd4(),
        mappings);

    // 24 - Data do Penúltimo CD4 – Sheet 1: Column X
    pdd.addColumn(
        "second_cd4_resultdate",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getLastCd4ResultDateBeforeMostRecentCd4(),
        mappings,
        new ForwardSlashDateConverter());

    // 25 - Resultado da Última Carga Viral – Sheet 1: Column Y
    pdd.addColumn(
        "vl_result",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getMostRecentVLResult(),
        "endDate=${endDate},location=${location}");

    // 26 - Data da Último Carga Viral – Sheet 1: Column Z
    pdd.addColumn(
        "vl_result_date",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getMostRecentVLResultDate(),
        "endDate=${endDate},location=${location}",
        new ForwardSlashDateConverter());

    // 27 - Resultado da Penúltima Carga Viral – Sheet 1: Column AA
    pdd.addColumn(
        "second_vl_result",
        listOfPatientsInAdvancedHivIllnessCohortQueries
            .getLastVLResultBeforeMostRecentVLResultDate(),
        "endDate=${endDate},location=${location}");

    // 28 - Data da Penúltima Carga Viral – Sheet 1: Column AB
    pdd.addColumn(
        "second_vl_resultdate",
        listOfPatientsInAdvancedHivIllnessCohortQueries
            .getLastVLResultDateBeforeMostRecentVLResultDate(),
        "endDate=${endDate},location=${location}",
        new ForwardSlashDateConverter());

    // 29 - Resultado do Última TB-LAM – Sheet 1: Column AC
    pdd.addColumn(
        "tblam_result",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getTbLaboratoryResearchResults(
            Arrays.asList(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId(),
                hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId()),
            Collections.singletonList(tbMetadata.getTestTBLAM().getConceptId()),
            Arrays.asList(
                commonMetadata.getPositive().getConceptId(),
                commonMetadata.getNegative().getConceptId(),
                hivMetadata.getNAConcept().getConceptId(),
                tbMetadata.getIndeterminate().getConceptId()),
            true),
        "endDate=${endDate},location=${location}",
        new TestResultConverter());

    // 30 - Data do Último TB-LAM – Sheet 1: Column AD
    pdd.addColumn(
        "tblam_result_date",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getTbLaboratoryResearchResults(
            Arrays.asList(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId(),
                hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId()),
            Collections.singletonList(tbMetadata.getTestTBLAM().getConceptId()),
            Arrays.asList(
                commonMetadata.getPositive().getConceptId(),
                commonMetadata.getNegative().getConceptId(),
                hivMetadata.getNAConcept().getConceptId(),
                tbMetadata.getIndeterminate().getConceptId()),
            false),
        "endDate=${endDate},location=${location}",
        new ForwardSlashDateConverter());

    // 31 - Resultado do Última CrAG Soro – Sheet 1: Column AE
    pdd.addColumn(
        "crag_soro_result",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getTbLaboratoryResearchResults(
            Arrays.asList(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId(),
                hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId()),
            Collections.singletonList(hivMetadata.getCragSoroConcept().getConceptId()),
            Arrays.asList(
                commonMetadata.getPositive().getConceptId(),
                commonMetadata.getNegative().getConceptId(),
                hivMetadata.getNAConcept().getConceptId(),
                tbMetadata.getIndeterminate().getConceptId()),
            true),
        "endDate=${endDate},location=${location}",
        new TestResultConverter());

    // 32 - Data do Último CrAG Soro – Sheet 1: Column AF
    pdd.addColumn(
        "crag_soro_result_date",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getTbLaboratoryResearchResults(
            Arrays.asList(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId(),
                hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId()),
            Collections.singletonList(hivMetadata.getCragSoroConcept().getConceptId()),
            Arrays.asList(
                commonMetadata.getPositive().getConceptId(),
                commonMetadata.getNegative().getConceptId(),
                hivMetadata.getNAConcept().getConceptId(),
                tbMetadata.getIndeterminate().getConceptId()),
            false),
        "endDate=${endDate},location=${location}",
        new ForwardSlashDateConverter());

    // 33 - Resultado do último CrAG LCR – Sheet 1: Column AG
    pdd.addColumn(
        "crag_lrc_result",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getTbLaboratoryResearchResults(
            Arrays.asList(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId(),
                hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId()),
            Collections.singletonList(hivMetadata.getCragLCRConcept().getConceptId()),
            Arrays.asList(
                commonMetadata.getPositive().getConceptId(),
                commonMetadata.getNegative().getConceptId(),
                hivMetadata.getNAConcept().getConceptId(),
                tbMetadata.getIndeterminate().getConceptId()),
            true),
        "endDate=${endDate},location=${location}",
        new TestResultConverter());

    // 34 - Data do último CrAG LCR – Sheet 1: Column AH
    pdd.addColumn(
        "crag_lrc_result_date",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getTbLaboratoryResearchResults(
            Arrays.asList(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId(),
                hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId()),
            Collections.singletonList(hivMetadata.getCragLCRConcept().getConceptId()),
            Arrays.asList(
                commonMetadata.getPositive().getConceptId(),
                commonMetadata.getNegative().getConceptId(),
                hivMetadata.getNAConcept().getConceptId(),
                tbMetadata.getIndeterminate().getConceptId()),
            false),
        "endDate=${endDate},location=${location}",
        new ForwardSlashDateConverter());

    // 35 - Resultado do Último Rastreio de CACU – Sheet 1: Column AI
    pdd.addColumn(
        "cacu_result",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getTbLaboratoryResearchResults(
            Collections.singletonList(
                hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId()),
            Collections.singletonList(hivMetadata.getCacuConcept().getConceptId()),
            Arrays.asList(
                commonMetadata.getPositive().getConceptId(),
                commonMetadata.getNegative().getConceptId()),
            true),
        "endDate=${endDate},location=${location}",
        new ForwardSlashDateConverter());

    // 36 - Data do Último Rastreio de CACU – Sheet 1: Column AJ
    pdd.addColumn(
        "cacu_result_date",
        listOfPatientsInAdvancedHivIllnessCohortQueries.getTbLaboratoryResearchResults(
            Collections.singletonList(
                hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId()),
            Collections.singletonList(hivMetadata.getCacuConcept().getConceptId()),
            Arrays.asList(
                commonMetadata.getPositive().getConceptId(),
                commonMetadata.getNegative().getConceptId()),
            false),
        "endDate=${endDate},location=${location}",
        new ForwardSlashDateConverter());

    return pdd;
  }

  public DataSetDefinition listOfPatientsInAdvancedHivIllnessTotalsDataset() {

    CohortIndicatorDataSetDefinition dataSetDefinition = new CohortIndicatorDataSetDefinition();
    dataSetDefinition.setName("DAH Cohort Totals");

    dataSetDefinition.addParameters(getParameters());

    // Total de Utentes Eligiveis a MDS de DAH
    CohortIndicator DAHTotalOfEligible =
        eptsGeneralIndicator.getIndicator(
            "ELIGIBLE_MDS",
            EptsReportUtils.map(
                listOfPatientsInAdvancedHivIllnessCohortQueries
                    .getTotalOfPatientsWithCriteriaToStartFollowupOfDAH(),
                mappings));

    dataSetDefinition.addColumn(
        "ELIGIBLE_MDS",
        "Total de Utentes Eligiveis a MDS de DAH",
        EptsReportUtils.map(DAHTotalOfEligible, mappings),
        "");

    // Total de Utentes em MDS de DAH
    CohortIndicator DAHTotalOfPatientsOnFollowupDAH =
        eptsGeneralIndicator.getIndicator(
            "ON_MDS",
            EptsReportUtils.map(
                listOfPatientsInAdvancedHivIllnessCohortQueries.getPatientsWhoStartedFollowupOnDAH(
                    true),
                mappings));

    dataSetDefinition.addColumn(
        "ON_MDS",
        "Total de Utentes em MDS de DAH",
        EptsReportUtils.map(DAHTotalOfPatientsOnFollowupDAH, mappings),
        "");

    return dataSetDefinition;
  }
}
