package org.openmrs.module.eptsreports.reporting.library.cohorts;

import static org.openmrs.module.reporting.evaluation.parameter.Mapped.mapStraightThrough;

import java.util.*;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.eptsreports.reporting.library.queries.CommonQueries;
import org.openmrs.module.eptsreports.reporting.library.queries.TXTBQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition.TimeModifier;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TXTBCohortQueries {

  @Autowired private TbMetadata tbMetadata;

  @Autowired private HivMetadata hivMetadata;

  @Autowired private CommonMetadata commonMetadata;

  @Autowired private GenericCohortQueries genericCohortQueries;

  @Autowired private CommonQueries commonQueries;

  @Autowired private TxTbMonthlyCascadeCohortQueries txTbMonthlyCascadeCohortQueries;

  private final String generalParameterMapping =
      "startDate=${startDate},endDate=${endDate},location=${location}";

  private final String codedObsParameterMapping =
      "onOrAfter=${startDate},onOrBefore=${endDate},locationList=${location}";

  private Mapped<CohortDefinition> map(CohortDefinition cd, String parameterMappings) {
    return EptsReportUtils.map(
        cd,
        EptsReportUtils.removeMissingParameterMappingsFromCohortDefintion(cd, parameterMappings));
  }

  private void addGeneralParameters(CohortDefinition cd) {
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * Data Inicio De Tratamento De Tuberculose <b>(concept_id = 1113)</b>
   *
   * <p>Data notificada nas fichas de: Seguimento <b>(encounterType_id = 6 or 9)</b>, Rastreio E
   * Livro Tb.
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition tbTreatmentStartDateWithinReportingDate() {
    CohortDefinition definition =
        genericCohortQueries.generalSql(
            "startedTbTreatment",
            TXTBQueries.dateObs(
                tbMetadata.getTBDrugTreatmentStartDate().getConceptId(),
                Arrays.asList(
                    hivMetadata.getAdultoSeguimentoEncounterType().getId(),
                    hivMetadata.getPediatriaSeguimentoEncounterType().getId()),
                true));
    addGeneralParameters(definition);
    return definition;
  }

  public CohortDefinition getPulmonaryTBDate() {
    CohortDefinition definition =
        genericCohortQueries.generalSql(
            "pulmonaryTBDate",
            TXTBQueries.tbPulmonaryTBDate(
                hivMetadata.getMasterCardEncounterType().getId(),
                hivMetadata.getOtherDiagnosis().getConceptId(),
                tbMetadata.getPulmonaryTB().getConceptId()));
    addGeneralParameters(definition);
    return definition;
  }

  public CohortDefinition getMarkedAsTratamentoTBInicio() {
    CohortDefinition definition =
        genericCohortQueries.generalSql(
            "markedAsTratamentoTBInicio",
            TXTBQueries.markedAsTratamentoTBInicio(
                hivMetadata.getAdultoSeguimentoEncounterType().getId(),
                hivMetadata.getPediatriaSeguimentoEncounterType().getId(),
                tbMetadata.getTBTreatmentPlanConcept().getConceptId(),
                hivMetadata.getStartDrugs().getConceptId()));
    addGeneralParameters(definition);
    return definition;
  }

  /**
   * <b>Description:</b> Number of patients enrolled in TB program <b>(program_id = 5)</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getInTBProgram() {
    CohortDefinition definition =
        genericCohortQueries.generalSql(
            "TBPROGRAMA",
            TXTBQueries.inTBProgramWithinReportingPeriodAtLocation(
                tbMetadata.getTBProgram().getProgramId()));
    addGeneralParameters(definition);
    return definition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * Patients with Pulmonary TB Date in Patient Clinical Record of ART date TB (Condicoes medicas
   * importantes – Ficha Resumo Mastercard during reporting period
   *
   * <ul>
   *   <li>Encounter Type ID = 53
   *   <li>Concept ID for Other Diagnosis = 1406
   *   <li>Answer = Pulmonary TB (value_coded 42)
   *   <li>Obs_datetime >= startDate and <=endDate
   * </ul>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPulmonaryTB() {
    CohortDefinition cd =
        genericCohortQueries.generalSql(
            "PULMONARYTB",
            TXTBQueries.pulmonaryTB(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                hivMetadata.getOtherDiagnosis().getConceptId(),
                tbMetadata.getPulmonaryTB().getConceptId()));
    addGeneralParameters(cd);
    return cd;
  }

  /**
   * <b>Description:</b> Patients marked as “Tratamento TB = Inicio (I) - <b>Start Drugs</b>" in
   * (Ficha Clinica - Mastercard) Card
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getTBTreatmentStart() {
    CohortDefinition cd =
        genericCohortQueries.generalSql(
            "TBTREATMENTSTART",
            TXTBQueries.tbTreatmentStart(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                tbMetadata.getTBTreatmentPlanConcept().getConceptId(),
                hivMetadata.getStartDrugs().getConceptId()));
    addGeneralParameters(cd);
    return cd;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * Patients with Tuberculosis Symptoms <b>(concept_id = 23758)</b> registered in the Adult
   * follow-up <b>(encounter_id = 6)</b> with Answers:
   *
   * <ul>
   *   <li>YES <b>(concept_id = 1065)</b>
   *   <li>NO <b>(concept_id = 1066)</b>
   * </ul>
   *
   * <p>Registered in the Adult follow-up <b>(encounterType_id = 6)</b> during the reporting period
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getTuberculosisSymptoms() {
    CohortDefinition cd =
        genericCohortQueries.generalSql(
            "tuberculosisSymptoms",
            TXTBQueries.tuberculosisSymptoms(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                tbMetadata.getHasTbSymptomsConcept().getConceptId(),
                commonMetadata.getYesConcept().getConceptId(),
                commonMetadata.getNoConcept().getConceptId()));
    addGeneralParameters(cd);
    return cd;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * Patients with Tuberculosis Symptoms <b>(concept_id = 23758)</b> and Positive Screening "YES"
   * <b>(concept_id = 1065)</b>
   *
   * <p>Registered in the Adult follow-up <b>(encounterType_id = 6)</b> during the reporting period
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getTuberculosisSymptomsPositiveScreening() {
    CohortDefinition cd =
        genericCohortQueries.generalSql(
            "tuberculosisSymptoms",
            TXTBQueries.tuberculosisSymptoms(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                tbMetadata.getHasTbSymptomsConcept().getConceptId(),
                commonMetadata.getYesConcept().getConceptId(),
                null));
    addGeneralParameters(cd);
    return cd;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * Patients with Active Tuberculosis <b>(concept_id = 23761)</b> with Answer "YES" <b>(concept_id
   * = 1065)</b>
   *
   * <p>Registered in the Adult follow-up <b>(encounterType_id = 6)</b> during the reporting period
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getActiveTuberculosis() {
    CohortDefinition cd =
        genericCohortQueries.generalSql(
            "activeTuberculosis",
            TXTBQueries.activeTuberculosis(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getActiveTBConcept().getConceptId(),
                commonMetadata.getYesConcept().getConceptId()));
    addGeneralParameters(cd);
    return cd;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * Patients with TB Observations <b>(concept_id = 1766)</b> in Answers =
   *
   * <ul>
   *   <li>Fever Lasting More Than 3 Weeks <b>(id = 1763)</b> OR
   *   <li>Weight Loss Of More Than 3 Kg In Last Month <b>(id = 1764)</b> OR
   *   <li>Nightsweats Lasting More Than 3 Weeks <b>(id = 1762)</b> OR
   *   <li>Cough Lasting More Than 3 Weeks <b>( id = 1760)</b> OR
   *   <li>Asthenia <b>(id = 23760)</b> OR
   *   <li>Cohabitant Being Treated For Tb <b>(id = 1765)</b> OR
   *   <li>Lymphadenopathy <b>(id = 161)</b>
   * </ul>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getTBObservation() {
    CohortDefinition cd =
        genericCohortQueries.generalSql(
            "tbObservation",
            TXTBQueries.tbObservation(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                tbMetadata.getObservationTB().getConceptId(),
                tbMetadata.getFeverLastingMoraThan3Weeks().getConceptId(),
                tbMetadata.getWeightLossOfMoreThan3KgInLastMonth().getConceptId(),
                tbMetadata.getNightsWeatsLastingMoraThan3Weeks().getConceptId(),
                tbMetadata.getCoughLastingMoraThan3Weeks().getConceptId(),
                tbMetadata.getAsthenia().getConceptId(),
                tbMetadata.getCohabitantBeingTreatedForTB().getConceptId(),
                tbMetadata.getLymphadenopathy().getConceptId()));
    addGeneralParameters(cd);
    return cd;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * Application for Laboratory Research <b>(concept_id = 23722)</b> with Answers:
   *
   * <ul>
   *   <li>TB Genexpert Test <b>(concept_id = 23723)</b>
   *   <li>Culture Test <b>(concept_id = 23774)</b>
   *   <li>Test TB LAM <b>(concept_id = 23951)</b>
   * </ul>
   *
   * <p>Registered in the Adult follow-up <b>(encounterType_id = 6)</b> during the reporting period
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getApplicationForLaboratoryResearch() {
    CohortDefinition cd =
        genericCohortQueries.generalSql(
            "applicationForLaboratoryResearch",
            TXTBQueries.applicationForLaboratoryResearch(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getApplicationForLaboratoryResearch().getConceptId(),
                tbMetadata.getTBGenexpertTestConcept().getConceptId(),
                tbMetadata.getCultureTest().getConceptId(),
                tbMetadata.getTestTBLAM().getConceptId(),
                hivMetadata.getResultForBasiloscopia().getConceptId(),
                tbMetadata.getXRayChest().getConceptId()));
    addGeneralParameters(cd);
    return cd;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * TB GeneExpertTest <b>(concept_id = 23723)</b> with Answer Positive <b>(concept_id = 703)</b> or
   * Negative <b>(id = 664)</b>
   *
   * <p>Registered in the Adult follow-up <b>(encounterType_id = 6)</b> during the reporting period
   *
   * <p>Method refined to accept different encounterTypes
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getTBGenexpertTestCohort(Integer encounterType) {
    if (encounterType == 6) {
      encounterType = hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId();
    }
    if (encounterType == 13) {
      encounterType = hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId();
    }

    CohortDefinition cd =
        genericCohortQueries.generalSql(
            "TBGenexpertTest",
            TXTBQueries.tbGenexpertTest(
                encounterType,
                tbMetadata.getTBGenexpertTestConcept().getConceptId(),
                commonMetadata.getPositive().getConceptId(),
                commonMetadata.getNegative().getConceptId()));
    addGeneralParameters(cd);
    return cd;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * Culture Test <b>(concept_id = 23774)</b> with Answer Positive <b>(concept_id = 703)</b> or
   * Negative <b>(id = 664)</b>
   *
   * <p>Registered in the Adult follow-up <b>(encounterType_id = 6)</b> during the reporting period
   *
   * <p>Method refined to accept different encounterTypes
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getCultureTest(Integer encounterType) {
    if (encounterType == 6) {
      encounterType = hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId();
    }
    if (encounterType == 13) {
      encounterType = hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId();
    }

    CohortDefinition cd =
        genericCohortQueries.generalSql(
            "CultureTest",
            TXTBQueries.cultureTest(
                encounterType,
                tbMetadata.getCultureTest().getConceptId(),
                commonMetadata.getPositive().getConceptId(),
                commonMetadata.getNegative().getConceptId()));
    addGeneralParameters(cd);
    return cd;
  }

  /**
   * @param encounterType
   * @return
   */
  public CohortDefinition getBkTest(Integer encounterType) {
    if (encounterType == 6) {
      encounterType = hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId();
    }
    if (encounterType == 13) {
      encounterType = hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId();
    }

    CohortDefinition cd =
        genericCohortQueries.generalSql(
            "bkTest",
            TXTBQueries.bkTest(
                encounterType,
                hivMetadata.getResultForBasiloscopia().getConceptId(),
                commonMetadata.getPositive().getConceptId(),
                commonMetadata.getNegative().getConceptId()));
    addGeneralParameters(cd);
    return cd;
  }

  /**
   * @param encounterType
   * @return
   */
  public CohortDefinition rxTest(Integer encounterType) {
    if (encounterType == 6) {
      encounterType = hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId();
    }
    if (encounterType == 13) {
      encounterType = hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId();
    }

    CohortDefinition cd =
        genericCohortQueries.generalSql(
            "rxTest",
            TXTBQueries.rxTorax(
                encounterType,
                tbMetadata.getXRayChest().getConceptId(),
                commonMetadata.getSugestive().getConceptId(),
                commonMetadata.getNegative().getConceptId(),
                commonMetadata.getIndeterminate().getConceptId()));
    addGeneralParameters(cd);
    return cd;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * Test TB LAM <b>(concept_id = 237951)</b> with Answer Positive <b>(concept_id = 703)</b> or
   * Negative <b>(id = 664)</b>
   *
   * <p>Registered in the Adult follow-up <b>(encounterType_id = 6)</b> during the reporting period
   *
   * <p>Method refined to accept different encounterTypes
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getTestTBLAM(Integer encounterType) {
    if (encounterType == 6) {
      encounterType = hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId();
    }
    if (encounterType == 13) {
      encounterType = hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId();
    }
    CohortDefinition cd =
        genericCohortQueries.generalSql(
            "TestTBLAM",
            TXTBQueries.testTBLAM(
                encounterType,
                tbMetadata.getTestTBLAM().getConceptId(),
                commonMetadata.getPositive().getConceptId(),
                commonMetadata.getNegative().getConceptId()));
    addGeneralParameters(cd);
    return cd;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * Raio X Torax <b>(id = 12)</b> Answer Positive <b>(id = 703)</> or Negative <b>(id = 664)</b> or
   * Indeterminado (id = 1138)
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getTestXRayChest(Integer encounterType) {
    if (encounterType == 6) {
      encounterType = hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId();
    }
    if (encounterType == 13) {
      encounterType = hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId();
    }

    CohortDefinition cd =
        genericCohortQueries.generalSql(
            "TestXRayChest",
            TXTBQueries.getTest(
                encounterType,
                tbMetadata.getXRayChest().getConceptId(),
                commonMetadata.getPositive().getConceptId(),
                commonMetadata.getNegative().getConceptId(),
                commonMetadata.getIndeterminate().getConceptId()));

    addGeneralParameters(cd);
    return cd;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * BK Test <b>(id = 307)</b> Answer Positive <b>(id = 703)</> or Negative <b>(id = 664)</b>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getTestBK(Integer encounterType) {
    if (encounterType == 6) {
      encounterType = hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId();
    }
    if (encounterType == 13) {
      encounterType = hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId();
    }

    CohortDefinition cd =
        genericCohortQueries.generalSql(
            "BK Test",
            TXTBQueries.getTest(
                encounterType,
                hivMetadata.getResultForBasiloscopia().getConceptId(),
                commonMetadata.getPositive().getConceptId(),
                commonMetadata.getNegative().getConceptId(),
                null));

    addGeneralParameters(cd);
    return cd;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * Result For Basiloscopia <b>(concept_id = 307)</b> with Answer Positive <b>(concept_id =
   * 703)</b> or Negative <b>(id = 664)</b>
   *
   * <p>Registered in Misau Laboratorio <b>(encounterType_id = 13)</b> during the reporting period
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getResultForBasiloscopia() {
    CohortDefinition cd =
        genericCohortQueries.generalSql(
            "ResultForBasiloscopia",
            TXTBQueries.resultForBasiloscopia(
                hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId(),
                hivMetadata.getResultForBasiloscopia().getConceptId()));
    addGeneralParameters(cd);
    return cd;
  }

  /**
   * <b>Description:</b> Pacientes Com Rastreio De Tuberculose <b>(concept_id = 6257)</b> Negativo
   * <b>(concept_id = 1066)</b> in the follow-up (Adult and Children) codes: RASTREIOTBNEG
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition codedNoTbScreening() {
    CohortDefinition cd =
        genericCohortQueries.hasCodedObs(
            tbMetadata.getTbScreeningConcept(),
            TimeModifier.ANY,
            SetComparator.IN,
            Arrays.asList(
                hivMetadata.getAdultoSeguimentoEncounterType(),
                hivMetadata.getPediatriaSeguimentoEncounterType()),
            Arrays.asList(commonMetadata.getNoConcept()));
    addGeneralParameters(cd);
    return cd;
  }

  /**
   * <b>Description:</b> Pacientes Com Rastreio De Tuberculose <b>(concept_id = 6257)</b> Positivo
   * <b>(concept_id = 1065)</b> in the follow-up (Adult and Children) codes: RASTREIOTBNEG
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition codedYesTbScreening() {
    CohortDefinition cd =
        genericCohortQueries.hasCodedObs(
            tbMetadata.getTbScreeningConcept(),
            TimeModifier.ANY,
            SetComparator.IN,
            Arrays.asList(
                hivMetadata.getAdultoSeguimentoEncounterType(),
                hivMetadata.getPediatriaSeguimentoEncounterType()),
            Arrays.asList(commonMetadata.getYesConcept()));
    addGeneralParameters(cd);
    return cd;
  }

  /**
   * <b>Description:</b> Patients who started art <b>on</b> period considering the transferred in
   * for that same period
   *
   * <p>And patients who started art <b>before</b> period also considering the transferred in for
   * that same period
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition artList() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.addSearch(
        "started-art-on-period-including-transferred-in",
        EptsReportUtils.map(
            getPatientsArtStartDateBeforeOrDuringPeriod(true),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "started-art-before-startDate-including-transferred-in",
        EptsReportUtils.map(
            getPatientsArtStartDateBeforeOrDuringPeriod(false),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString(
        "started-art-on-period-including-transferred-in OR started-art-before-startDate-including-transferred-in");
    addGeneralParameters(cd);
    return cd;
  }

  /**
   * <b>Description:</b> Positive Investigation Research result <b>(concept_id = 6277)</b> Positivo
   * <b>(concept_id = 1065)</b> in the follow-up (Adult and Children)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition positiveInvestigationResult() {
    CohortDefinition cd =
        genericCohortQueries.hasCodedObs(
            tbMetadata.getResearchResultConcept(),
            TimeModifier.ANY,
            SetComparator.IN,
            Arrays.asList(
                hivMetadata.getAdultoSeguimentoEncounterType(),
                hivMetadata.getPediatriaSeguimentoEncounterType()),
            Arrays.asList(tbMetadata.getPositiveConcept()));
    addGeneralParameters(cd);
    return cd;
  }

  /**
   * <b>Description:</b> At least one “POS” selected for “Resultado da Investigação para TB de BK
   * e/ou RX?” during the reporting period consultations;
   *
   * <p><b>Technical Specs</b>
   *
   * <blockquote>
   *
   * response 703: "POS" for question: 6277
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition positiveInvestigationResultComposition() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    CohortDefinition P = positiveInvestigationResult();
    cd.addSearch("P", map(P, codedObsParameterMapping));
    cd.setCompositionString("P");
    addGeneralParameters(cd);
    return cd;
  }

  /**
   * <b>Description:</b> Negative Investigation Research result <b>(concept_id = 6277)</b> Negativo
   * <b>(concept_id = 1065)</b> in the follow-up (Adult and Children)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition negativeInvestigationResult() {
    CohortDefinition cd =
        genericCohortQueries.hasCodedObs(
            tbMetadata.getResearchResultConcept(),
            TimeModifier.ANY,
            SetComparator.IN,
            Arrays.asList(
                hivMetadata.getAdultoSeguimentoEncounterType(),
                hivMetadata.getPediatriaSeguimentoEncounterType()),
            Arrays.asList(tbMetadata.getNegativeConcept()));
    addGeneralParameters(cd);
    return cd;
  }

  /**
   * <b>Description:</b> At least one “NEG” selected for “Resultado da Investigação para TB de BK
   * e/ou RX?” during the reporting period consultations;
   *
   * <p><b>Technical Specs</b>
   *
   * <blockquote>
   *
   * response 664: "NEG" for question: 6277
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition negativeInvestigationResultComposition() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    CohortDefinition N = negativeInvestigationResult();
    cd.addSearch("N", map(N, codedObsParameterMapping));
    cd.setCompositionString("N");
    addGeneralParameters(cd);
    return cd;
  }

  /**
   * <b>Description:</b> At least one “S” or “N” selected for TB Screening (Rastreio de TB) during
   * the reporting period consultations
   *
   * <p><b>Technical Specs</b>
   *
   * <blockquote>
   *
   * (response 1065: "YES" or 1066: "NO" for question 6257: "SCREENING FOR TB")
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition yesOrNoInvestigationResult() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    CohortDefinition S = codedYesTbScreening();
    cd.addSearch("S", map(S, codedObsParameterMapping));
    CohortDefinition N = codedNoTbScreening();
    cd.addSearch("N", map(N, codedObsParameterMapping));
    cd.setCompositionString("S OR N");
    addGeneralParameters(cd);
    return cd;
  }

  /**
   * <b>Description:</b> TX_TB Numerator A
   *
   * <p><b>Technical Specs</b>
   *
   * <p>Number of patients on TB Treatment, with Pulmonary TB Date, who initiated TB Treatment in
   * ART
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition txTbNumeratorA() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    CohortDefinition i =
        genericCohortQueries.generalSql(
            "onTbTreatment",
            TXTBQueries.dateObs(
                tbMetadata.getTBDrugTreatmentStartDate().getConceptId(),
                Arrays.asList(
                    hivMetadata.getAdultoSeguimentoEncounterType().getId(),
                    hivMetadata.getPediatriaSeguimentoEncounterType().getId()),
                true));
    addGeneralParameters(i);
    cd.addSearch("i", map(i, generalParameterMapping));

    CohortDefinition ii = getInTBProgram();
    cd.addSearch("ii", map(ii, generalParameterMapping));

    CohortDefinition patientswithPulmonaryTbDate =
        genericCohortQueries.generalSql(
            "patientswithPulmonaryTbDate",
            TXTBQueries.tbPulmonaryTBDate(
                hivMetadata.getMasterCardEncounterType().getId(),
                hivMetadata.getOtherDiagnosis().getConceptId(),
                tbMetadata.getPulmonaryTB().getConceptId()));
    cd.addSearch(
        "patientswithPulmonaryTbDate", map(patientswithPulmonaryTbDate, generalParameterMapping));

    CohortDefinition patientsWhoInitiatedTbTreatment =
        genericCohortQueries.generalSql(
            "patientsWhoInitiatedTbTreatment",
            TXTBQueries.getPatientsWithObsBetweenDates(
                hivMetadata.getTBTreatmentPlanConcept().getConceptId(),
                hivMetadata.getStartDrugs().getConceptId(),
                Arrays.asList(
                    hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                    hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId())));
    addGeneralParameters(patientsWhoInitiatedTbTreatment);

    cd.addSearch(
        "patientsWhoInitiatedTbTreatment",
        map(patientsWhoInitiatedTbTreatment, generalParameterMapping));

    CohortDefinition artList = artList();
    cd.addSearch("artList", map(artList, generalParameterMapping));
    cd.setCompositionString(
        "(i OR ii OR patientswithPulmonaryTbDate OR patientsWhoInitiatedTbTreatment) AND artList");
    addGeneralParameters(cd);
    return cd;
  }

  /**
   * <b>Description:</b> TX_TB Numerator
   *
   * <p><b>Technical Specs</b> Exclusion for patients who started TB Treatment on previous period
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition txTbNumerator() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    CohortDefinition A = txTbNumeratorA();
    cd.addSearch("A", map(A, generalParameterMapping));

    cd.addSearch(
        "started-tb-treatment-previous-period",
        EptsReportUtils.map(
            tbTreatmentStartDateWithinReportingDate(),
            "startDate=${startDate-6m},endDate=${startDate-1d},location=${location}"));
    cd.addSearch(
        "pulmonary-tb-date",
        EptsReportUtils.map(
            getPulmonaryTBDate(),
            "startDate=${startDate-6m},endDate=${startDate-1d},location=${location}"));
    cd.addSearch(
        "marked-as-tratamento-tb-inicio",
        EptsReportUtils.map(
            getMarkedAsTratamentoTBInicio(),
            "startDate=${startDate-6m},endDate=${startDate-1d},location=${location}"));
    cd.addSearch(
        "in-tb-program",
        EptsReportUtils.map(
            getInTBProgram(),
            "startDate=${startDate-6m},endDate=${startDate-1d},location=${location}"));

    cd.setCompositionString(
        "A AND NOT(started-tb-treatment-previous-period OR pulmonary-tb-date OR marked-as-tratamento-tb-inicio OR in-tb-program)");
    addGeneralParameters(cd);
    return cd;
  }

  /**
   * <b>Description: BR-6</b> Positive Screening
   *
   * <p><b>Technical Specs</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition positiveScreening() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addSearch("A", mapStraightThrough(getPatientsWithAtLeastOneYesForTBScreening()));
    cd.addSearch("B", mapStraightThrough(getPatientsWithAtLeastPosInvestigationResultTB()));
    cd.addSearch("C", mapStraightThrough(getPatientsWithAtLeastNegInvestigationResultTB()));
    cd.addSearch("D", mapStraightThrough(getPatientsWithTBTreatmentStartDate()));
    cd.addSearch("E", mapStraightThrough(getPatientsInTBProgramInThePreviousPeriod()));
    cd.addSearch("F", mapStraightThrough(getResultForBasiloscopia()));
    cd.addSearch("G", mapStraightThrough(getTBTreatmentStart()));
    cd.addSearch("H", mapStraightThrough(getPulmonaryTB()));
    cd.addSearch("I", mapStraightThrough(getPatientsWithAtLeastOneResponseForPositiveScreeningI()));
    cd.addSearch("J", mapStraightThrough(getXpertMtbTestCohort()));
    cd.addSearch("TBLAM", mapStraightThrough(getTestTBLAM(51)));
    cd.setCompositionString("A OR B OR C OR D OR E OR F OR G OR H OR I OR J OR TBLAM");
    addGeneralParameters(cd);
    return cd;
  }

  /**
   * <b>Description:</b> All patients with at least one “POS” selected for “Resultado da
   * Investigação para TB de BK e/ou RX?” (Ficha de Seguimento) during reporting period
   *
   * <p><b>Technical Specs</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithAtLeastPosInvestigationResultTB() {
    SqlCohortDefinition cd = new SqlCohortDefinition();

    cd.setName("Patients With At Least One Yes For TB Screening During the reporting  period");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<String, Integer>();
    map.put(
        "adultoSeguimentoEncounterType",
        hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put(
        "pediatriaSeguimentoEncounterType",
        hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("researchResultConcept", tbMetadata.getResearchResultConcept().getConceptId());
    map.put("positiveConcept", tbMetadata.getPositiveConcept().getConceptId());
    map.put("negativeConcept", tbMetadata.getNegativeConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM patient p "
            + "    INNER JOIN encounter e "
            + "        ON e.patient_id = p.patient_id "
            + "    INNER JOIN obs o "
            + "        ON o.encounter_id = e.encounter_id "
            + "WHERE "
            + "    p.voided = 0 AND "
            + "    e.voided = 0 AND "
            + "    o.voided = 0 AND "
            + "    e.encounter_type IN (${adultoSeguimentoEncounterType},${pediatriaSeguimentoEncounterType})  AND "
            + "    o.concept_id = ${researchResultConcept} AND "
            + "    o.value_coded = ${positiveConcept} AND "
            + "    e.encounter_datetime BETWEEN :startDate AND :endDate AND "
            + "    e.location_id  = :location "
            + "GROUP BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    String replaceQuery = sb.replace(query);

    cd.setQuery(replaceQuery);

    return cd;
  }

  /**
   * <b>Description:</b> All patients with at least one “NEG” selected for “Resultado da
   * Investigação para TB de BK e/ou RX?” (Ficha de Seguimento) AND “N” selected for TB Screening
   * “Rastreio TB” in same encounter occurred during reporting period during reporting period
   *
   * <p><b>Technical Specs</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithAtLeastNegInvestigationResultTB() {
    SqlCohortDefinition cd = new SqlCohortDefinition();

    cd.setName("Patients With At Least One Yes For TB Screening During the reporting  period");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<String, Integer>();
    map.put(
        "adultoSeguimentoEncounterType",
        hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put(
        "pediatriaSeguimentoEncounterType",
        hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("researchResultConcept", tbMetadata.getResearchResultConcept().getConceptId());
    map.put("tbScreening", tbMetadata.getTbScreeningConcept().getConceptId());
    map.put("negativeConcept", tbMetadata.getNegativeConcept().getConceptId());
    map.put("noConcept", commonMetadata.getNoConcept().getConceptId());

    String query =
        "SELECT patient_id FROM ( "
            + "SELECT p.patient_id, e.encounter_id "
            + "FROM patient p "
            + "    INNER JOIN encounter e "
            + "        ON e.patient_id = p.patient_id "
            + "    INNER JOIN obs o "
            + "        ON o.encounter_id = e.encounter_id "
            + "    INNER JOIN"
            + "    (SELECT p.patient_id, e.encounter_id "
            + "FROM patient p "
            + "    INNER JOIN encounter e "
            + "        ON e.patient_id = p.patient_id "
            + "    INNER JOIN obs o "
            + "        ON o.encounter_id = e.encounter_id "
            + "WHERE "
            + "    p.voided = 0 AND "
            + "    e.voided = 0 AND "
            + "    o.voided = 0 AND "
            + "    o.concept_id = ${tbScreening} AND "
            + "    o.value_coded = ${noConcept} "
            + ") as screening "
            + "ON e.encounter_id = screening.encounter_id "
            + "WHERE "
            + "    p.voided = 0 AND "
            + "    e.voided = 0 AND "
            + "    o.voided = 0 AND "
            + "    e.encounter_type IN (${adultoSeguimentoEncounterType},${pediatriaSeguimentoEncounterType})  AND "
            + "    o.concept_id = ${researchResultConcept} AND "
            + "    o.value_coded = ${negativeConcept} AND "
            + "    e.encounter_datetime BETWEEN :startDate AND :endDate AND "
            + "    e.location_id  = :location "
            + "    GROUP BY p.patient_id) as list";

    StringSubstitutor sb = new StringSubstitutor(map);
    String replaceQuery = sb.replace(query);

    cd.setQuery(replaceQuery);

    return cd;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * All patients with at least one “S” (Yes) <b>(concept_id =1065)</b> selected for TB Screening
   * “Rastreio TB <b>(concept_id = 6257)</b>” (Ficha de Seguimento Adult or Pediatric) during the
   * reporting period
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithAtLeastOneYesForTBScreening() {
    SqlCohortDefinition cd = new SqlCohortDefinition();

    cd.setName("Patients With At Least One Yes For TB Screening During the reporting  period");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put(
        "adultoSeguimentoEncounterType",
        hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put(
        "pediatriaSeguimentoEncounterType",
        hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("tbScreeningConcept", tbMetadata.getTbScreeningConcept().getConceptId());
    map.put("getYesConcept", commonMetadata.getYesConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM patient p "
            + "    INNER JOIN encounter e "
            + "        ON e.patient_id = p.patient_id "
            + "    INNER JOIN obs o "
            + "        ON o.encounter_id = e.encounter_id "
            + "WHERE "
            + "    p.voided = 0 AND "
            + "    e.voided = 0 AND  "
            + "    o.voided = 0 AND "
            + "    e.encounter_type IN (${adultoSeguimentoEncounterType},${pediatriaSeguimentoEncounterType})  AND "
            + "    o.concept_id = ${tbScreeningConcept} AND "
            + "    o.value_coded = ${getYesConcept} AND "
            + "    e.encounter_datetime BETWEEN :startDate AND :endDate AND "
            + "    e.location_id  = :location "
            + "GROUP BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    String replaceQuery = sb.replace(query);

    cd.setQuery(replaceQuery);

    return cd;
  }

  /**
   * <b>Patients New on ART (TX_TB_FR7): Positive Screening (TX_TB_FR9)
   *
   * @see TxNewCohortQueries#getTxNewCompositionCohort(String)
   * @see #getDenominator()
   * @see #positiveScreening()
   * @return {@link CohortDefinition}
   */
  public CohortDefinition newOnARTPositiveScreening() {
    CompositionCohortDefinition definition = new CompositionCohortDefinition();
    definition.setName("Patients New on ART: Positive Screening");
    definition.addSearch(
        "denominator", EptsReportUtils.map(getDenominator(), generalParameterMapping));
    definition.addSearch("new-on-art", EptsReportUtils.map(getNewOnArt(), generalParameterMapping));
    definition.addSearch(
        "positive-screening", EptsReportUtils.map(positiveScreening(), generalParameterMapping));
    addGeneralParameters(definition);
    definition.setCompositionString("denominator AND new-on-art AND positive-screening");
    return definition;
  }

  /**
   * <b>Description:</b> New On ART Negative Screening
   *
   * <p><b>Technical Specs</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition newOnARTNegativeScreening() {
    CompositionCohortDefinition definition = new CompositionCohortDefinition();
    definition.setName("newOnARTPositiveScreening()");
    definition.addSearch(
        "denominator", EptsReportUtils.map(getDenominator(), generalParameterMapping));
    definition.addSearch("new-on-art", EptsReportUtils.map(getNewOnArt(), generalParameterMapping));
    definition.addSearch(
        "positive-screening", EptsReportUtils.map(positiveScreening(), generalParameterMapping));
    addGeneralParameters(definition);
    definition.setCompositionString("(denominator AND new-on-art) AND NOT positive-screening");
    return definition;
  }

  /**
   * <b>Description:</b> Previously On ART Positive Screening
   *
   * <p><b>Technical Specs</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition previouslyOnARTPositiveScreening() {
    CompositionCohortDefinition definition = new CompositionCohortDefinition();
    definition.setName("newOnARTPositiveScreening()");
    definition.addSearch(
        "denominator", EptsReportUtils.map(getDenominator(), generalParameterMapping));
    definition.addSearch("new-on-art", EptsReportUtils.map(getNewOnArt(), generalParameterMapping));
    definition.addSearch(
        "positive-screening", EptsReportUtils.map(positiveScreening(), generalParameterMapping));
    addGeneralParameters(definition);
    definition.setCompositionString("(denominator AND positive-screening) NOT new-on-art");
    return definition;
  }

  /**
   * <b>Description:</b> Previously On ART Negative Screening
   *
   * <p><b>Technical Specs</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition previouslyOnARTNegativeScreening() {
    CompositionCohortDefinition definition = new CompositionCohortDefinition();
    definition.setName("previouslyOnARTNegativeScreening()");
    definition.addSearch(
        "denominator", EptsReportUtils.map(getDenominator(), generalParameterMapping));
    definition.addSearch("new-on-art", EptsReportUtils.map(getNewOnArt(), generalParameterMapping));
    definition.addSearch(
        "positive-screening", EptsReportUtils.map(positiveScreening(), generalParameterMapping));
    addGeneralParameters(definition);
    definition.setCompositionString("denominator NOT (new-on-art OR positive-screening)");
    return definition;
  }

  /**
   * <b>Description:</b> Patients New On ART Numerator
   *
   * <p><b>Technical Specs</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition patientsNewOnARTNumerator() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    CohortDefinition NUM = txTbNumerator();
    cd.addSearch("NUM", map(NUM, generalParameterMapping));
    cd.addSearch(
        "started-during-reporting-period",
        EptsReportUtils.map(
            getPatientsArtStartDateBeforeOrDuringPeriod(true),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.setCompositionString("NUM AND started-during-reporting-period");
    addGeneralParameters(cd);
    return cd;
  }

  /**
   * <b>Description:</b> Patients Previously On ART Numerator
   *
   * <p><b>Technical Specs</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition patientsPreviouslyOnARTNumerator() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    CohortDefinition NUM = txTbNumerator();
    cd.addSearch("NUM", map(NUM, generalParameterMapping));
    cd.addSearch(
        "started-before-start-reporting-period",
        EptsReportUtils.map(
            getPatientsArtStartDateBeforeOrDuringPeriod(false),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.setCompositionString("NUM AND started-before-start-reporting-period");
    addGeneralParameters(cd);
    return cd;
  }

  /**
   * <b>Description:</b> Denominator
   *
   * <p><b>Technical Specs</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getDenominator() {
    CompositionCohortDefinition definition = new CompositionCohortDefinition();
    addGeneralParameters(definition);
    definition.setName("TxTB - Denominator");
    definition.addSearch("art-list", EptsReportUtils.map(artList(), generalParameterMapping));
    definition.addSearch(
        "tb-screening", EptsReportUtils.map(yesOrNoInvestigationResult(), generalParameterMapping));
    definition.addSearch(
        "tb-investigation",
        EptsReportUtils.map(positiveInvestigationResultComposition(), generalParameterMapping));
    definition.addSearch(
        "tb-investigation-negative",
        EptsReportUtils.map(negativeInvestigationResultComposition(), generalParameterMapping));
    definition.addSearch(
        "started-tb-treatment",
        EptsReportUtils.map(tbTreatmentStartDateWithinReportingDate(), generalParameterMapping));
    definition.addSearch(
        "in-tb-program", EptsReportUtils.map(getInTBProgram(), generalParameterMapping));
    definition.addSearch(
        "pulmonary-tb", EptsReportUtils.map(getPulmonaryTB(), generalParameterMapping));
    definition.addSearch(
        "marked-as-tb-treatment-start",
        EptsReportUtils.map(getTBTreatmentStart(), generalParameterMapping));

    definition.addSearch(
        "tuberculosis-symptomys",
        EptsReportUtils.map(getTuberculosisSymptoms(), generalParameterMapping));

    definition.addSearch(
        "active-tuberculosis",
        EptsReportUtils.map(getActiveTuberculosis(), generalParameterMapping));

    definition.addSearch(
        "tb-observations", EptsReportUtils.map(getTBObservation(), generalParameterMapping));

    definition.addSearch(
        "application-for-laboratory-research",
        EptsReportUtils.map(getApplicationForLaboratoryResearch(), generalParameterMapping));

    definition.addSearch(
        "tb-genexpert-test",
        EptsReportUtils.map(getTBGenexpertTestCohort(6), generalParameterMapping));

    definition.addSearch(
        "tb-genexpert-lab-test",
        EptsReportUtils.map(getTBGenexpertTestCohort(13), generalParameterMapping));
    definition.addSearch(
        "tb-xpert-mtb", EptsReportUtils.map(getXpertMtbTestCohort(), generalParameterMapping));

    definition.addSearch(
        "culture-test", EptsReportUtils.map(getCultureTest(6), generalParameterMapping));

    definition.addSearch(
        "culture-test-lab", EptsReportUtils.map(getCultureTest(13), generalParameterMapping));

    definition.addSearch(
        "test-tb-lam", EptsReportUtils.map(getTestTBLAM(6), generalParameterMapping));

    definition.addSearch(
        "test-tb-lam-lab", EptsReportUtils.map(getTestTBLAM(13), generalParameterMapping));

    definition.addSearch("test-bk", EptsReportUtils.map(getTestBK(6), generalParameterMapping));

    definition.addSearch(
        "x-ray-chest", EptsReportUtils.map(getTestXRayChest(6), generalParameterMapping));

    definition.addSearch(
        "result-for-basiloscopia",
        EptsReportUtils.map(getResultForBasiloscopia(), generalParameterMapping));

    definition.addSearch(
        "started-tb-treatment-previous-period",
        EptsReportUtils.map(
            tbTreatmentStartDateWithinReportingDate(),
            "startDate=${startDate-6m},endDate=${startDate-1d},location=${location}"));
    definition.addSearch(
        "in-tb-program-previous-period",
        EptsReportUtils.map(
            getPatientsInTBProgramInThePreviousPeriod(),
            "startDate=${startDate-6m},endDate=${startDate-1d},location=${location}"));
    definition.addSearch(
        "transferred-out",
        EptsReportUtils.map(
            getTransferredOut(), "startDate=${startDate},endDate=${endDate},location=${location}"));
    definition.addSearch(
        "pulmonary-tb-date",
        EptsReportUtils.map(
            getPulmonaryTBDate(),
            "startDate=${startDate-6m},endDate=${startDate-1d},location=${location}"));
    definition.addSearch(
        "marked-as-tratamento-tb-inicio",
        EptsReportUtils.map(
            getMarkedAsTratamentoTBInicio(),
            "startDate=${startDate-6m},endDate=${startDate-1d},location=${location}"));

    definition.addSearch(
        "test-tb-lam-elab", EptsReportUtils.map(getTestTBLAM(51), generalParameterMapping));

    definition.setCompositionString(
        "(art-list AND (tb-screening OR tb-investigation OR tb-investigation-negative OR started-tb-treatment OR in-tb-program OR pulmonary-tb OR marked-as-tb-treatment-start "
            + "OR (tuberculosis-symptomys OR active-tuberculosis OR tb-observations OR application-for-laboratory-research OR tb-genexpert-test OR tb-genexpert-lab-test OR tb-xpert-mtb OR culture-test OR culture-test-lab "
            + "OR test-tb-lam OR test-tb-lam-lab OR test-tb-lam-elab OR test-bk OR x-ray-chest) OR result-for-basiloscopia)) "
            + "NOT ((transferred-out NOT (marked-as-tb-treatment-start OR started-tb-treatment OR pulmonary-tb OR in-tb-program)) OR started-tb-treatment-previous-period OR in-tb-program-previous-period OR pulmonary-tb-date OR marked-as-tratamento-tb-inicio)");

    return definition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <p>The system will identify patients who are Transferred Out as follows:
   *
   * <ul>
   *   <li>Patients enrolled on ART Program (Service TARV- Tratamento) with the following last
   *       status: Transferred Out or
   *   <li>Patients who have “Mudança no Estado de Permanência TARV” filled out in Ficha Resumo or
   *       Ficha Clinica – Master Card for the following reasons that are specified in the patient
   *       chart: patient Transferred Out or
   *   <li>Patients who have REASON PATIENT MISSED VISIT (MOTIVOS DA FALTA) as “Transferido para
   *       outra US” or “Auto-transferência” marked in the last Home Visit Card by reporting end
   *       date. Use the “data da visita” when the patient reason was marked on the home visit card
   *       as the reference date.
   * </ul>
   *
   * <br>
   *
   * <p>The system will identify the most recent date from the different sources as the date of
   * Transferred Out. Patients who are “marked” as transferred out who have an ARV pick-up
   * registered in FILA or have a clinical consultation after the date the patient was “marked” as
   * transferred out will not be considered as Transferred Out.<br>
   *
   * <p>The system will consider patient as transferred out as above defined only if the most recent
   * date between (next scheduled ART pick-up on FILA + 1 day) and (the most recent ART pickup date
   * on Ficha Recepção – Levantou ARVs + 31 days) falls during the reporting period.
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getTransferredOut() {
    CompositionCohortDefinition definition = new CompositionCohortDefinition();
    addGeneralParameters(definition);
    definition.setName("TxTB - Transferred Out");
    definition.addSearch(
        "transferred-out",
        EptsReportUtils.map(
            getPatientsTransferredOut(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    definition.addSearch(
        "transferred-out-fila-arv",
        EptsReportUtils.map(
            getPatientsTransferredOutFilaArvPickUp(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    definition.setCompositionString("transferred-out AND transferred-out-fila-arv");

    return definition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <p>The system will identify patients who are Transferred Out as follows:
   *
   * <ul>
   *   <li>Patients enrolled on ART Program (Service TARV- Tratamento) with the following last
   *       status: Transferred Out or
   *   <li>Patients who have “Mudança no Estado de Permanência TARV” filled out in Ficha Resumo or
   *       Ficha Clinica – Master Card for the following reasons that are specified in the patient
   *       chart: patient Transferred Out or
   *   <li>Patients who have REASON PATIENT MISSED VISIT (MOTIVOS DA FALTA) as “Transferido para
   *       outra US” or “Auto-transferência” marked in the last Home Visit Card by reporting end
   *       date. Use the “data da visita” when the patient reason was marked on the home visit card
   *       as the reference date.
   * </ul>
   *
   * <br>
   *
   * <p>The system will identify the most recent date from the different sources as the date of
   * Transferred Out. Patients who are “marked” as transferred out who have an ARV pick-up
   * registered in FILA or have a clinical consultation after the date the patient was “marked” as
   * transferred out will not be considered as Transferred Out.<br>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsTransferredOut() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName(
        "Patient Transferred Out With No Drug Pick After The Transferred out Date ");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put(
        "adultoSeguimentoEncounterType",
        hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put(
        "pediatriaSeguimentoEncounterType",
        hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put(
        "pharmaciaEncounterType", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put(
        "masterCardDrugPickupEncounterType",
        hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("artDatePickup", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put(
        "masterCardEncounterType", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put(
        "stateOfStayOfPreArtPatient", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("transferredOutConcept", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("autoTransferConcept", hivMetadata.getAutoTransferConcept().getConceptId());
    map.put("stateOfStayOfArtPatient", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("defaultingMotiveConcept", hivMetadata.getDefaultingMotiveConcept().getConceptId());
    map.put(
        "returnVisitDateForArvDrugConcept",
        hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());
    map.put(
        "buscaActivaEncounterType", hivMetadata.getBuscaActivaEncounterType().getEncounterTypeId());
    map.put("artProgram", hivMetadata.getARTProgram().getProgramId());
    map.put(
        "transferredOutToAnotherHealthFacilityWorkflowState",
        hivMetadata
            .getTransferredOutToAnotherHealthFacilityWorkflowState()
            .getProgramWorkflowStateId());

    String query =
        "SELECT   transferred_out.patient_id "
            + "             FROM     ( "
            + "                                 SELECT     latest.patient_id , "
            + "                                            Max(latest.last_date) AS last_date "
            + "                                 FROM       ( "
            + "                                                       SELECT     p.patient_id , "
            + "                                                                  Max(ps.start_date) AS last_date "
            + "                                                       FROM       patient p "
            + "                                                       INNER JOIN patient_program pg "
            + "                                                       ON         p.patient_id=pg.patient_id "
            + "                                                       INNER JOIN patient_state ps "
            + "                                                       ON         pg.patient_program_id=ps.patient_program_id "
            + "                                                      INNER JOIN ( "
            + "                                                       SELECT     p.patient_id , "
            + "                                                                  Max(ps.start_date) AS last_date "
            + "                                                       FROM       patient p "
            + "                                                       INNER JOIN patient_program pg "
            + "                                                       ON         p.patient_id=pg.patient_id "
            + "                                                       INNER JOIN patient_state ps "
            + "                                                       ON         pg.patient_program_id=ps.patient_program_id "
            + "                                                       WHERE      pg.voided=0 "
            + "                                                       AND        ps.voided=0 "
            + "                                                       AND        p.voided=0 "
            + "                                                       AND        pg.program_id= ${artProgram} "
            + "                                                       AND        ps.state IS NOT NULL "
            + "                                                       AND        ps.start_date <= :endDate "
            + "                                                       AND        pg.location_id= :location "
            + "                                                       GROUP BY   p.patient_id ) last_state on last_state.patient_id = p.patient_id"
            + "                                                       WHERE      pg.voided=0 "
            + "                                                       AND        ps.voided=0 "
            + "                                                       AND        p.voided=0 "
            + "                                                       AND        pg.program_id= ${artProgram} "
            + "                                                       AND        ps.state = ${transferredOutToAnotherHealthFacilityWorkflowState} "
            + "                                                       AND        ps.start_date = last_state.last_date "
            + "                                                       AND        pg.location_id= :location "
            + "                                                       GROUP BY   p.patient_id "
            + "                                                       UNION "
            + "                                                       SELECT     p.patient_id, "
            + "                                                                  max(o.obs_datetime) AS last_date "
            + "                                                       FROM       patient p "
            + "                                                       INNER JOIN encounter e "
            + "                                                       ON         e.patient_id=p.patient_id "
            + "                                                       INNER JOIN obs o "
            + "                                                       ON         o.encounter_id=e.encounter_id "
            + "                                                     INNER JOIN ( "
            + "                                                      SELECT     p.patient_id, "
            + "                                                                  max(o.obs_datetime) AS last_date "
            + "                                                       FROM       patient p "
            + "                                                       INNER JOIN encounter e "
            + "                                                       ON         e.patient_id=p.patient_id "
            + "                                                       INNER JOIN obs o "
            + "                                                       ON         o.encounter_id=e.encounter_id "
            + "                                                       WHERE      p.voided = 0 "
            + "                                                       AND        e.voided = 0 "
            + "                                                       AND        o.voided = 0 "
            + "                                                       AND        e.encounter_type = ${masterCardEncounterType} "
            + "                                                       AND        o.concept_id = ${stateOfStayOfPreArtPatient} "
            + "                                                       AND        o.value_coded IS NOT NULL "
            + "                                                       AND        o.obs_datetime <= :endDate "
            + "                                                       AND        e.location_id = :location "
            + "                                                       GROUP BY   p.patient_id ) last_state on last_state.last_date "
            + "                                                       WHERE      p.voided = 0 "
            + "                                                       AND        e.voided = 0 "
            + "                                                       AND        o.voided = 0 "
            + "                                                       AND        e.encounter_type = ${masterCardEncounterType} "
            + "                                                       AND        o.concept_id = ${stateOfStayOfPreArtPatient} "
            + "                                                       AND        o.value_coded = ${transferredOutConcept} "
            + "                                                       AND        o.obs_datetime = last_state.last_date "
            + "                                                       AND        e.location_id = :location "
            + "                                                       GROUP BY   p.patient_id "
            + "                                                       UNION "
            + "                                                       SELECT     p.patient_id , "
            + "                                                                  max(e.encounter_datetime) AS last_date "
            + "                                                       FROM       patient p "
            + "                                                       INNER JOIN encounter e "
            + "                                                       ON         e.patient_id=p.patient_id "
            + "                                                       INNER JOIN obs o "
            + "                                                       ON         o.encounter_id=e.encounter_id "
            + "                                                     INNER JOIN ( "
            + "                                                         SELECT     p.patient_id , "
            + "                                                                  max(e.encounter_datetime) AS last_date "
            + "                                                       FROM       patient p "
            + "                                                       INNER JOIN encounter e "
            + "                                                       ON         e.patient_id=p.patient_id "
            + "                                                       INNER JOIN obs o "
            + "                                                       ON         o.encounter_id=e.encounter_id "
            + "                                                       WHERE      p.voided = 0 "
            + "                                                       AND        e.voided = 0 "
            + "                                                       AND        o.voided = 0 "
            + "                                                       AND        e.encounter_type = ${adultoSeguimentoEncounterType} "
            + "                                                       AND        o.concept_id = ${stateOfStayOfArtPatient} "
            + "                                                       AND        o.value_coded IS NOT NULL "
            + "                                                       AND        e.encounter_datetime <= :endDate "
            + "                                                       AND        e.location_id = :location "
            + "                                                       GROUP BY   p.patient_id ) last_state on last_state.last_date "
            + "                                                       WHERE      p.voided = 0 "
            + "                                                       AND        e.voided = 0 "
            + "                                                       AND        o.voided = 0 "
            + "                                                       AND        e.encounter_type = ${adultoSeguimentoEncounterType} "
            + "                                                       AND        o.concept_id = ${stateOfStayOfArtPatient} "
            + "                                                       AND        o.value_coded = ${transferredOutConcept} "
            + "                                                       AND        e.encounter_datetime = last_state.last_date "
            + "                                                       AND        e.location_id = :location "
            + "                                                       GROUP BY   p.patient_id "
            + "                                                       UNION "
            + "                                                       SELECT     p.patient_id, "
            + "                                                                  max(e.encounter_datetime) last_date "
            + "                                                       FROM       patient p "
            + "                                                       INNER JOIN encounter e "
            + "                                                       ON         p.patient_id = e.patient_id "
            + "                                                       INNER JOIN obs o "
            + "                                                       ON         e.encounter_id = o.encounter_id "
            + "                                                       WHERE      o.concept_id = ${defaultingMotiveConcept} "
            + "                                                       AND        e.location_id = :location "
            + "                                                       AND        e.encounter_type= ${buscaActivaEncounterType} "
            + "                                                       AND        e.encounter_datetime <= :endDate "
            + "                                                       AND        o.value_coded IN (${transferredOutConcept}, "
            + "                                                                                    ${autoTransferConcept}) "
            + "                                                       AND        e.voided=0 "
            + "                                                       AND        o.voided=0 "
            + "                                                       AND        p.voided=0 "
            + "                                                       GROUP BY   p.patient_id ) latest "
            + "                                                       WHERE      latest.patient_id NOT IN "
            + "                                                                         ( "
            + "                                                                         SELECT     p.patient_id "
            + "                                                                         FROM       patient p "
            + "                                                                         INNER JOIN encounter e "
            + "                                                                         ON         e.patient_id=p.patient_id "
            + "                                                                         WHERE      p.voided = 0 "
            + "                                                                         AND        e.voided = 0 "
            + "                                                                         AND        e.encounter_type = ${pharmaciaEncounterType} "
            + "                                                       AND        e.encounter_datetime > last_date "
            + "                                                       AND        e.encounter_datetime <= :endDate "
            + "                                                       AND        e.location_id = :location "
            + "                                                       GROUP BY   p.patient_id ) "
            + "                                                       GROUP BY   latest.patient_id "
            + "                                            ) transferred_out "
            + "             GROUP BY transferred_out.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    String mappedQuery = stringSubstitutor.replace(query);

    sqlCohortDefinition.setQuery(mappedQuery);

    return sqlCohortDefinition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <p>The system will identify the most recent date from the different sources as the date of
   * Transferred Out.
   *
   * <p>Patients who are “marked” as transferred out who have an ARV pick-up registered in FILA or
   * have a clinical consultation after the date the patient was “marked” as transferred out will
   * not be considered as Transferred Out.
   *
   * <p>The system will consider patient as transferred out as above defined only if the most recent
   * date between (next scheduled ART pick-up on FILA + 1 day) and (the most recent ART pickup date
   * on Ficha Recepção – Levantou ARVs + 31 days) falls during the reporting period.
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsTransferredOutFilaArvPickUp() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName(
        "Patient Transferred Out With most recent date between Fila AND ARV PickUp ");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put(
        "adultoSeguimentoEncounterType",
        hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put(
        "pediatriaSeguimentoEncounterType",
        hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put(
        "pharmaciaEncounterType", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("artDatePickup", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put(
        "masterCardDrugPickupEncounterType",
        hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put(
        "returnVisitDateForArvDrugConcept",
        hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());

    String query =
        "SELECT final.patient_id "
            + "FROM ( "
            + "         SELECT most_recent.patient_id, "
            + "                Max(most_recent.value_datetime) AS value_datetime "
            + "         FROM (SELECT p.patient_id, "
            + "                      date_add(max(o.value_datetime), interval 1 day)  AS value_datetime "
            + "               FROM patient p "
            + "                        INNER JOIN encounter e "
            + "                                   ON e.patient_id = p.patient_id "
            + "                        INNER JOIN obs o "
            + "                                   ON o.encounter_id = e.encounter_id "
            + "               WHERE p.voided = 0 "
            + "                 AND e.voided = 0 "
            + "                 AND o.voided = 0 "
            + "                 AND e.encounter_type = ${pharmaciaEncounterType} "
            + "                 AND o.concept_id = ${returnVisitDateForArvDrugConcept} "
            + "                 AND e.encounter_datetime <= :endDate "
            + "                 AND e.location_id = :location "
            + "               GROUP BY p.patient_id "
            + "               UNION "
            + "               SELECT p.patient_id, "
            + "                      date_add(max(o.value_datetime), interval 31 day) AS value_datetime "
            + "               FROM patient p "
            + "                        INNER JOIN encounter e "
            + "                                   ON e.patient_id = p.patient_id "
            + "                        INNER JOIN obs o "
            + "                                   ON o.encounter_id = e.encounter_id "
            + "               WHERE p.voided = 0 "
            + "                 AND e.voided = 0 "
            + "                 AND o.voided = 0 "
            + "                 AND e.encounter_type = ${masterCardDrugPickupEncounterType} "
            + "                 AND o.concept_id = ${artDatePickup} "
            + "                 AND o.value_datetime <= :endDate "
            + "                 AND e.location_id = :location "
            + "               GROUP BY p.patient_id) AS most_recent "
            + "               GROUP BY most_recent.patient_id "
            + "     ) final "
            + " WHERE final.value_datetime BETWEEN :startDate AND :endDate ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    String mappedQuery = stringSubstitutor.replace(query);

    sqlCohortDefinition.setQuery(mappedQuery);

    return sqlCohortDefinition;
  }

  /**
   * <b>Description:</b> Patients in TB Program<b>(p.program_id = 5)</b> in Within Period
   *
   * <p><b>Technical Specs</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsInTBProgramInThePreviousPeriod() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("in tb in the previeus period");

    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("tbProgram", tbMetadata.getTBProgram().getProgramId());
    String sql =
        " SELECT pg.patient_id "
            + " FROM patient p "
            + "    INNER JOIN patient_program pg "
            + "        ON p.patient_id=pg.patient_id "
            + " WHERE pg.voided=0 "
            + "    AND p.voided=0 "
            + "    AND program_id= ${tbProgram}"
            + "    AND date_enrolled "
            + "        BETWEEN :startDate AND :endDate "
            + "    AND location_id= :location "
            + " GROUP BY pg.patient_id ";

    StringSubstitutor sb = new StringSubstitutor(map);
    String replacedStrinbg = sb.replace(sql);

    cd.setQuery(replacedStrinbg);

    return cd;
  }

  /**
   * TB Treatment Start Date ( Ficha de Seguimento) within reporting period
   *
   * @return
   */
  public CohortDefinition getPatientsWithTBTreatmentStartDate() {
    SqlCohortDefinition cd = new SqlCohortDefinition();

    cd.setName("Patients who started TB treatment within period");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<String, Integer>();
    map.put(
        "adultoSeguimentoEncounterType",
        hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put(
        "pediatriaSeguimentoEncounterType",
        hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("tbStartTreatment", tbMetadata.getTBDrugTreatmentStartDate().getConceptId());

    String query =
        " SELECT p.patient_id, e.encounter_id "
            + "FROM patient p "
            + "    INNER JOIN encounter e "
            + "        ON e.patient_id = p.patient_id "
            + "    INNER JOIN obs o "
            + "        ON o.encounter_id = e.encounter_id "
            + "WHERE "
            + "    p.voided = 0 AND "
            + "    e.voided = 0 AND "
            + "    o.voided = 0 AND "
            + "    e.encounter_type IN (${adultoSeguimentoEncounterType},${pediatriaSeguimentoEncounterType})  AND "
            + "    o.concept_id = ${tbStartTreatment} AND "
            + "    o.value_datetime IS NOT NULL AND "
            + "    e.encounter_datetime BETWEEN :startDate AND :endDate AND "
            + "    e.location_id  = :location";

    StringSubstitutor sb = new StringSubstitutor(map);
    String replaceQuery = sb.replace(query);

    cd.setQuery(replaceQuery);

    return cd;
  }
  /**
   * <b>Description:</b> New On ART
   *
   * <p><b>Technical Specs</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getNewOnArt() {
    CompositionCohortDefinition definition = new CompositionCohortDefinition();
    definition.setName("TxTB New on ART");
    addGeneralParameters(definition);
    definition.addSearch(
        "started-on-period",
        EptsReportUtils.map(
            getPatientsArtStartDateBeforeOrDuringPeriod(true),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    definition.addSearch(
        "started-before-start-reporting-period",
        EptsReportUtils.map(
            getPatientsArtStartDateBeforeOrDuringPeriod(false),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    definition.setCompositionString(
        "started-on-period AND NOT started-before-start-reporting-period");

    return definition;
  }

  /**
   * <b>Description:</b> Patients With At Least One Response For Positive ScreeningH
   *
   * <p><b>Technical Specs</b>
   *
   * @return {@link CohortDefinition}
   */
  public CompositionCohortDefinition getPatientsWithAtLeastOneResponseForPositiveScreeningI() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addSearch(
        "tuberculosis-symptomys", mapStraightThrough(getTuberculosisSymptomsPositiveScreening()));
    cd.addSearch("active-tuberculosis", mapStraightThrough(getActiveTuberculosis()));
    cd.addSearch("tb-observations", mapStraightThrough(getTBObservation()));
    cd.addSearch(
        "application-for-laboratory-research",
        mapStraightThrough(getApplicationForLaboratoryResearch()));
    cd.addSearch("tb-genexpert-test6", mapStraightThrough(getTBGenexpertTestCohort(6)));
    cd.addSearch("culture-test6", mapStraightThrough(getCultureTest(6)));
    cd.addSearch("test-tb-lam6", mapStraightThrough(getTestTBLAM(6)));
    cd.addSearch("test-bk6", mapStraightThrough(getBkTest(6)));
    cd.addSearch("test-rx", mapStraightThrough(rxTest(6)));
    cd.addSearch("tb-genexpert-test13", mapStraightThrough(getTBGenexpertTestCohort(13)));
    cd.addSearch("culture-test13", mapStraightThrough(getCultureTest(13)));
    cd.addSearch("test-tb-lam13", mapStraightThrough(getTestTBLAM(13)));
    cd.addSearch("test-bk13", mapStraightThrough(getBkTest(13)));
    cd.setCompositionString(
        "tuberculosis-symptomys OR active-tuberculosis OR tb-observations "
            + "OR application-for-laboratory-research OR tb-genexpert-test6 OR"
            + " culture-test6 OR test-tb-lam6 OR tb-genexpert-test13 OR culture-test13 OR"
            + " test-tb-lam13 OR test-bk13 OR test-bk6 OR test-rx");
    addGeneralParameters(cd);
    return cd;
  }

  /**
   * <b> Get patients who sent specimen within date boundaries </b>
   * <li>Have Diagnóstico Laboratorial para TB (ANY RESULT) registered for ‘Baciloscopia’,
   *     ‘GeneXpert’, ‘Xpert MTB’, ‘Cultura’, ’TB LAM’ on the Laboratory Form or e-Lab Form OR
   * <li>If the Investigações - Pedidos Laboratoriais request is marked for ‘TB LAM’, ‘GeneXpert’ ,
   *     ‘Cultura’ or ‘BK’ on Ficha Clinica OR
   * <li>If Investigações - Resultados Laboratoriais results (ANY RESULT) is recorded for ‘TB LAM’ ,
   *     ‘GeneXpert’, ‘Cultura’, or ‘BK’ on Ficha Clinica.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getSpecimenSent() {
    CohortDefinition cd =
        getPatientsWhoHaveSentSpecimen(
            hivMetadata.getMisauLaboratorioEncounterType(),
            hivMetadata.getApplicationForLaboratoryResearch(),
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getResultForBasiloscopia(),
            tbMetadata.getTBGenexpertTestConcept(),
            tbMetadata.getTestTBLAM(),
            tbMetadata.getCultureTest(),
            commonMetadata.getPositive(),
            commonMetadata.getNegative(),
            commonMetadata.getNotFoundConcept(),
            commonMetadata.getIndeterminate(),
            hivMetadata.getFsrEncounterType());
    return cd;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <p>Patients Disaggregation – Diagnostic Test
   *
   * <p>TX_TB_FR13
   *
   * <blockquote>
   *
   * The system will identify patients with any diagnostic Test sent disaggregation as follows:
   * (mWRD (with or without other testing):patients will be included who during the reporting
   * period:
   *
   * <ul>
   *   <li>have a ‘GeneXpert Positivo’ registered in the Investigações – resultados laboratoriais -
   *       Ficha Clínica – Mastercard; or
   *   <li>have a ‘GeneXpert Negativo’ registered in the Investigações – resultados laboratoriais
   *       Ficha Clínica – Mastercard; or
   *   <li>have a GeneXpert request registered in the Investigações – Pedidos Laboratoriais - Ficha
   *       Clínica – Mastercard; or
   *   <li>have a GeneXpert result ANY VALUE registered in the Laboratory Form or
   *   <li>have a XpertMTB result ANY VALUE registered in the Laboratory Form
   * </ul>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getmWRD() {
    CohortDefinition cd =
        getPatientsWhoHaveGeneXpert(
            hivMetadata.getApplicationForLaboratoryResearch(),
            hivMetadata.getAdultoSeguimentoEncounterType(),
            tbMetadata.getTBGenexpertTestConcept(),
            commonMetadata.getPositive(),
            commonMetadata.getNegative(),
            hivMetadata.getMisauLaboratorioEncounterType());
    return cd;
  }

  /**
   * <b>Description:</b> Get patients who have a Basiloscopia And Not GeneXpert registered
   *
   * <p><b>Technical Specs</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getSmearMicroscopyOnly() {
    CohortDefinition cd =
        getSmearMicroscopyOnly(
            hivMetadata.getMisauLaboratorioEncounterType(),
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getApplicationForLaboratoryResearch(),
            hivMetadata.getResultForBasiloscopia(),
            commonMetadata.getPositive(),
            commonMetadata.getNegative(),
            tbMetadata.getNotFoundTestResultConcept());
    return cd;
  }

  /**
   * <b>Description:</b> Get patients who have a Basiloscopia And Not GeneXpert Positve registered
   *
   * <p><b>Technical Specs</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getSmearMicroscopyOnlyPositiveResult(boolean txTbOrTb4) {
    CohortDefinition cd =
        getSmearMicroscopyOnlyPositve(
            hivMetadata.getMisauLaboratorioEncounterType(),
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getApplicationForLaboratoryResearch(),
            hivMetadata.getResultForBasiloscopia(),
            commonMetadata.getPositive(),
            txTbOrTb4);
    return cd;
  }

  /**
   * <b>Description:</b> Get patients who have a Basiloscopia And Not GeneXpert Positve registered
   *
   * <p><b>Technical Specs</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getSmearMicroscopyOnlyNegativeResult(boolean txTbOrTb) {
    CohortDefinition cd =
        getSmearMicroscopyOnlyPositve(
            hivMetadata.getMisauLaboratorioEncounterType(),
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getApplicationForLaboratoryResearch(),
            hivMetadata.getResultForBasiloscopia(),
            commonMetadata.getNegative(),
            txTbOrTb);
    return cd;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <p>Patients Disaggregation – Diagnostic Test
   *
   * <p>TX_TB_FR13
   *
   * <blockquote>
   *
   * <p>Additional test other than mWRD : patients will be included who during the reporting period:
   *
   * <ul>
   *   <li>have Investigações - Pedidos Laboratoriais request marked for ‘TB LAM’ or ‘Cultura’ or
   *   <li>have Investigações - Resultados Laboratoriais results (ANY RESULT) recorded for ‘TB LAM’
   *       or ‘Cultura’ or
   *   <li>have a Cultura result ANY VALUE registered in the Laboratory Form or
   *   <li>have a TB LAM result ANY VALUE registered in the Laboratory Form AND
   *   <li>do not have a GeneXpert, Xpert MTB and Baciloscopia result registered in the Laboratory
   *       Form and
   *   <li>do not have Investigações – Pedidos Laboratorais request marked for ‘GeneXpert’ and ‘BK’
   *       and
   *   <li>do not have Investigações – Resultados Laboratoriais result recorded for ‘GeneXpert’ and
   *       ‘BK’
   * </ul>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getAdditionalTest() {
    CohortDefinition cd =
        getAdditionalTest(
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getMisauLaboratorioEncounterType(),
            tbMetadata.getCultureTest(),
            hivMetadata.getApplicationForLaboratoryResearch(),
            commonMetadata.getPositive(),
            commonMetadata.getNegative());
    return cd;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * Get patients from denominator who have positive results returned registered during the period
   * Have a ‘GeneXpert Positivo’ registered in the
   *
   * <ul>
   *   <li>investigacoes – resultados laboratoriais - ficha clinica – mastercard OR
   *   <li>Have a ‘resultado baciloscopia positive’ registered in the laboratory form OR
   *   <li>Have a TB LAM positivo registered in the investigacoes – resultados laboratoriais ficha
   *       clinica – mastercard OR
   *   <li>Have a cultura positiva registered in the investigacoes – resultados laboratoriais ficha
   *       clinica – mastercard
   * </ul>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPositiveResultsReturned() {
    CohortDefinition cd =
        getPositiveResultsReturned(
            hivMetadata.getMisauLaboratorioEncounterType(),
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getResultForBasiloscopia(),
            tbMetadata.getTBGenexpertTestConcept(),
            tbMetadata.getTestTBLAM(),
            tbMetadata.getCultureTest(),
            tbMetadata.getTestXpertMtbUuidConcept(),
            commonMetadata.getPositive(),
            commonMetadata.getYesConcept());
    return cd;
  }

  /**
   * <b>Description:</b> BR-8 Specimen Sent - Get patients from denominator AND tb_screened AND
   * specimen_sent
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition specimenSent() {
    CompositionCohortDefinition definition = new CompositionCohortDefinition();
    definition.setName("specimenSent()");
    definition.addSearch(
        "denominator", EptsReportUtils.map(getDenominator(), generalParameterMapping));
    definition.addSearch(
        "specimen-sent", EptsReportUtils.map(getSpecimenSent(), generalParameterMapping));
    addGeneralParameters(definition);
    definition.setCompositionString("denominator AND specimen-sent");
    return definition;
  }

  /**
   * <b>Description:</b> BR-9 mWRD - Get patients from denominator AND positive_screened AND mWRD
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition mWRD() {
    CompositionCohortDefinition definition = new CompositionCohortDefinition();
    definition.setName("mWRD()");
    definition.addSearch(
        "denominator", EptsReportUtils.map(getDenominator(), generalParameterMapping));
    definition.addSearch("mWRD", EptsReportUtils.map(getmWRD(), generalParameterMapping));
    addGeneralParameters(definition);
    definition.setCompositionString("denominator AND mWRD");
    return definition;
  }

  /**
   * <b>Description: BR-10 </b> Get patients who have a Basiloscopia Positivo or Negativo registered
   * in the laboratory form encounter type 13 Except patients identified in GeneXpert
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition smearMicroscopyOnly() {
    CompositionCohortDefinition definition = new CompositionCohortDefinition();
    definition.setName("smearMicroscopyOnly()");
    definition.addSearch(
        "denominator", EptsReportUtils.map(getDenominator(), generalParameterMapping));
    definition.addSearch(
        "smearMicroscopyOnly",
        EptsReportUtils.map(getSmearMicroscopyOnly(), generalParameterMapping));
    addGeneralParameters(definition);
    definition.setCompositionString("denominator AND smearMicroscopyOnly  ");
    return definition;
  }

  /**
   * <b>Description: BR-11</b> Additional Test - Denominator AND Screened AND Additional AND NOT
   * Genexpert AND NOT Microscopy
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition otherAdditionalTest() {
    CompositionCohortDefinition definition = new CompositionCohortDefinition();
    definition.setName("otherAdditionalTest()");
    definition.addSearch(
        "denominator", EptsReportUtils.map(getDenominator(), generalParameterMapping));
    definition.addSearch(
        "otherAdditionalTest", EptsReportUtils.map(getAdditionalTest(), generalParameterMapping));
    addGeneralParameters(definition);
    definition.setCompositionString("denominator AND otherAdditionalTest");
    return definition;
  }

  /**
   * <b>Description: BR-12</b> Positive Results Returned All patients from denominator who have the
   * following requests/results registered during the period:
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition positiveResultsReturned() {
    CompositionCohortDefinition definition = new CompositionCohortDefinition();
    definition.setName("positiveResultsReturned()");
    definition.addSearch(
        "denominator", EptsReportUtils.map(getDenominator(), generalParameterMapping));
    definition.addSearch(
        "positiveResultsReturned",
        EptsReportUtils.map(getPositiveResultsReturned(), generalParameterMapping));
    addGeneralParameters(definition);
    definition.setCompositionString("denominator AND positiveResultsReturned");
    return definition;
  }

  /**
   * <b> Get patients who sent specimen within date boundaries </b>
   * <li>Have Diagnóstico Laboratorial para TB (ANY RESULT) registered for ‘Baciloscopia’,
   *     ‘GeneXpert’, ‘Xpert MTB’, ‘Cultura’, ’TB LAM’ on the Laboratory Form or e-Lab Form OR
   * <li>If the Investigações - Pedidos Laboratoriais request is marked for ‘TB LAM’, ‘GeneXpert’ ,
   *     ‘Cultura’ or ‘BK’ on Ficha Clinica OR
   * <li>If Investigações - Resultados Laboratoriais results (ANY RESULT) is recorded for ‘TB LAM’ ,
   *     ‘GeneXpert’, ‘Cultura’, or ‘BK’ on Ficha Clinica.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoHaveSentSpecimen(
      EncounterType laboratory,
      Concept applicationForLaboratoryResearch,
      EncounterType fichaClinica,
      Concept basiloscopiaExam,
      Concept genexpertTest,
      Concept tbLamTest,
      Concept cultureTest,
      Concept positive,
      Concept negative,
      Concept notFound,
      Concept indeterminate,
      EncounterType eLabFormFSR) {

    CohortDefinition basiloscopiaExamCohort =
        genericCohortQueries.generalSql(
            "basiloscopiaExamCohort",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                fichaClinica, basiloscopiaExam, Arrays.asList(negative, positive, notFound)));
    addGeneralParameters(basiloscopiaExamCohort);

    CohortDefinition basiloscopiaLabExamCohort =
        genericCohortQueries.generalSql(
            "basiloscopiaLabExamCohort",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                laboratory, basiloscopiaExam, Arrays.asList(negative, positive, notFound)));
    addGeneralParameters(basiloscopiaLabExamCohort);

    CohortDefinition genexpertTestCohort =
        genericCohortQueries.generalSql(
            "genexpertTestCohort",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                fichaClinica, genexpertTest, Arrays.asList(negative, positive)));
    addGeneralParameters(genexpertTestCohort);

    CohortDefinition genexpertLabTestCohort =
        genericCohortQueries.generalSql(
            "genexpertLabTestCohort",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                laboratory, genexpertTest, Arrays.asList(negative, positive)));
    addGeneralParameters(genexpertLabTestCohort);

    CohortDefinition tbLamTestCohort =
        genericCohortQueries.generalSql(
            "tbLamTestCohort",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                fichaClinica, tbLamTest, Arrays.asList(negative, positive)));
    addGeneralParameters(tbLamTestCohort);

    CohortDefinition tbLamLabTestCohort =
        genericCohortQueries.generalSql(
            "tbLamLabTestCohort",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                laboratory, tbLamTest, Arrays.asList(negative, positive, indeterminate)));
    addGeneralParameters(tbLamLabTestCohort);

    CohortDefinition cultureTestCohort =
        genericCohortQueries.generalSql(
            "cultureTestCohort",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                fichaClinica, cultureTest, Arrays.asList(negative, positive)));
    addGeneralParameters(cultureTestCohort);

    CohortDefinition cultureLabTestCohort =
        genericCohortQueries.generalSql(
            "cultureLabTestCohort",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                laboratory, cultureTest, Arrays.asList(negative, positive)));
    addGeneralParameters(cultureLabTestCohort);

    CohortDefinition applicationForLaboratoryResearchCohort =
        genericCohortQueries.generalSql(
            "applicationForLaboratoryResearchCohort",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                fichaClinica,
                applicationForLaboratoryResearch,
                Arrays.asList(genexpertTest, cultureTest, tbLamTest, basiloscopiaExam)));
    addGeneralParameters(applicationForLaboratoryResearchCohort);

    CohortDefinition tbLamElabTest =
        genericCohortQueries.generalSql(
            "tbLam-elab",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                eLabFormFSR, tbLamTest, Arrays.asList(negative, positive)));
    addGeneralParameters(tbLamElabTest);

    CompositionCohortDefinition definition = new CompositionCohortDefinition();
    definition.setName("specimenSent()");
    addGeneralParameters(definition);

    definition.addSearch(
        "basiloscopiaExamCohort",
        EptsReportUtils.map(basiloscopiaExamCohort, generalParameterMapping));
    definition.addSearch(
        "basiloscopiaLabExamCohort",
        EptsReportUtils.map(basiloscopiaLabExamCohort, generalParameterMapping));
    definition.addSearch(
        "genexpertTestCohort", EptsReportUtils.map(genexpertTestCohort, generalParameterMapping));
    definition.addSearch(
        "genexpertLabTestCohort",
        EptsReportUtils.map(genexpertLabTestCohort, generalParameterMapping));
    definition.addSearch(
        "tbLamTestCohort", EptsReportUtils.map(tbLamTestCohort, generalParameterMapping));
    definition.addSearch(
        "tbLamLabTestCohort", EptsReportUtils.map(tbLamLabTestCohort, generalParameterMapping));
    definition.addSearch(
        "cultureTestCohort", EptsReportUtils.map(cultureTestCohort, generalParameterMapping));
    definition.addSearch(
        "cultureLabTestCohort", EptsReportUtils.map(cultureLabTestCohort, generalParameterMapping));
    definition.addSearch(
        "applicationForLaboratoryResearchCohort",
        EptsReportUtils.map(applicationForLaboratoryResearchCohort, generalParameterMapping));
    definition.addSearch(
        "tb-xpert-mtb", EptsReportUtils.map(getXpertMtbTestCohort(), generalParameterMapping));

    definition.addSearch("tbLam-elab", EptsReportUtils.map(tbLamElabTest, generalParameterMapping));

    definition.setCompositionString(
        "basiloscopiaExamCohort OR basiloscopiaLabExamCohort OR genexpertTestCohort OR genexpertLabTestCohort"
            + " OR tbLamTestCohort OR tbLamLabTestCohort OR cultureTestCohort OR cultureLabTestCohort "
            + "OR applicationForLaboratoryResearchCohort OR tb-xpert-mtb OR tbLam-elab");
    return definition;
  }

  /**
   * <b>Description:</b> Get patients who have a GeneXpert Positivo or Negativo registered in the
   * investigations - lab results - ficha clinica - mastercard OR Get patients who have a GeneXpert
   * request registered in the investigations - lab results - ficha clinica - mastercard
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoHaveGeneXpert(
      Concept applicationForLaboratoryResearch,
      EncounterType fichaClinica,
      Concept genexpertTest,
      Concept positive,
      Concept negative,
      EncounterType laboratory) {

    CohortDefinition genexpertTestCohort =
        genericCohortQueries.generalSql(
            "genexpertTestCohort",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                fichaClinica, genexpertTest, Arrays.asList(negative, positive)));
    addGeneralParameters(genexpertTestCohort);

    CohortDefinition genexpertLabTestCohort =
        genericCohortQueries.generalSql(
            "genexpertLabTestCohort",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                laboratory, genexpertTest, Arrays.asList(negative, positive)));
    addGeneralParameters(genexpertLabTestCohort);

    CohortDefinition applicationForLaboratoryResearchCohort =
        genericCohortQueries.generalSql(
            "applicationForLaboratoryResearchCohort",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                fichaClinica, applicationForLaboratoryResearch, Arrays.asList(genexpertTest)));
    addGeneralParameters(applicationForLaboratoryResearchCohort);

    CompositionCohortDefinition definition = new CompositionCohortDefinition();
    definition.setName("haveGeneXpert()");
    addGeneralParameters(definition);

    definition.addSearch(
        "genexpertTestCohort", EptsReportUtils.map(genexpertTestCohort, generalParameterMapping));
    definition.addSearch(
        "genexpertLabTestCohort",
        EptsReportUtils.map(genexpertLabTestCohort, generalParameterMapping));
    definition.addSearch(
        "applicationForLaboratoryResearchCohort",
        EptsReportUtils.map(applicationForLaboratoryResearchCohort, generalParameterMapping));
    definition.addSearch(
        "tb-xpert-mtb", EptsReportUtils.map(getXpertMtbTestCohort(), generalParameterMapping));

    definition.setCompositionString(
        "genexpertTestCohort OR genexpertLabTestCohort OR applicationForLaboratoryResearchCohort OR tb-xpert-mtb");
    return definition;
  }

  /**
   * Smear Microscopy - Get patients who have a Basiloscopia Positivo or Negativo registered in the
   * laboratory form encounter type 13 Except patients identified in GeneXpert
   *
   * @return CohortDefinition
   */
  public CohortDefinition getSmearMicroscopyOnly(
      EncounterType laboratory,
      EncounterType fichaClinica,
      Concept applicationForLaboratoryResearch,
      Concept basiloscopiaExam,
      Concept positive,
      Concept negative,
      Concept notFoundResult) {

    CohortDefinition basiloscopiaCohort =
        genericCohortQueries.generalSql(
            "basiloscopiaCohort",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                fichaClinica, basiloscopiaExam, Arrays.asList(negative, positive, notFoundResult)));
    addGeneralParameters(basiloscopiaCohort);

    CohortDefinition basiloscopiaLabCohort =
        genericCohortQueries.generalSql(
            "basiloscopiaLabCohort",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                laboratory, basiloscopiaExam, Arrays.asList(negative, positive, notFoundResult)));
    addGeneralParameters(basiloscopiaLabCohort);

    CompositionCohortDefinition definition = new CompositionCohortDefinition();
    definition.setName("haveBasiloscopia()");
    addGeneralParameters(definition);

    CohortDefinition applicationForLaboratoryResearchCohort =
        genericCohortQueries.generalSql(
            "applicationForLaboratoryResearchCohort",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                fichaClinica, applicationForLaboratoryResearch, Arrays.asList(basiloscopiaExam)));
    addGeneralParameters(applicationForLaboratoryResearchCohort);

    definition.addSearch(
        "basiloscopiaCohort", EptsReportUtils.map(basiloscopiaCohort, generalParameterMapping));
    definition.addSearch(
        "basiloscopiaLabCohort",
        EptsReportUtils.map(basiloscopiaLabCohort, generalParameterMapping));
    definition.addSearch(
        "applicationForLaboratoryResearchCohort",
        EptsReportUtils.map(applicationForLaboratoryResearchCohort, generalParameterMapping));

    definition.addSearch("mWRDCohort", EptsReportUtils.map(getmWRD(), generalParameterMapping));

    definition.setCompositionString(
        "(basiloscopiaCohort OR basiloscopiaLabCohort OR applicationForLaboratoryResearchCohort) AND NOT mWRDCohort");
    return definition;
  }

  /**
   * Smear Microscopy - Get patients who have a Basiloscopia Positive registered in the laboratory
   * form encounter type 13 Except patients identified in GeneXpert
   *
   * @return CohortDefinition
   */
  public CohortDefinition getSmearMicroscopyOnlyPositve(
      EncounterType laboratory,
      EncounterType fichaClinica,
      Concept applicationForLaboratoryResearch,
      Concept basiloscopiaExam,
      Concept positive,
      Boolean txtbOrTb4) {

    CohortDefinition basiloscopiaCohort =
        genericCohortQueries.generalSql(
            "basiloscopiaCohort",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                fichaClinica, basiloscopiaExam, Arrays.asList(positive)));
    addGeneralParameters(basiloscopiaCohort);

    CohortDefinition basiloscopiaLabCohort =
        genericCohortQueries.generalSql(
            "basiloscopiaLabCohort",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                laboratory, basiloscopiaExam, Arrays.asList(positive)));
    addGeneralParameters(basiloscopiaLabCohort);

    CompositionCohortDefinition definition = new CompositionCohortDefinition();
    definition.setName("haveBasiloscopia()");
    addGeneralParameters(definition);

    definition.addSearch(
        "basiloscopiaCohort", EptsReportUtils.map(basiloscopiaCohort, generalParameterMapping));
    definition.addSearch(
        "basiloscopiaLabCohort",
        EptsReportUtils.map(basiloscopiaLabCohort, generalParameterMapping));

    definition.setCompositionString("basiloscopiaCohort OR basiloscopiaLabCohort");

    return definition;
  }

  /**
   * <b>Description:</b> Get patients who have a Additional Test AND Not GeneXpert AND Not Smear
   * Microscopy Only
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getAdditionalTest(
      EncounterType fichaClinica,
      EncounterType laboratory,
      Concept cultureTest,
      Concept applicationForLaboratoryResearch,
      Concept positive,
      Concept negative) {

    CohortDefinition cultureTestCohort =
        genericCohortQueries.generalSql(
            "cultureTestCohort",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                fichaClinica, cultureTest, Arrays.asList(negative, positive)));
    addGeneralParameters(cultureTestCohort);

    CohortDefinition cultureLabTestCohort =
        genericCohortQueries.generalSql(
            "cultureLabTestCohort",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                laboratory, cultureTest, Arrays.asList(negative, positive)));
    addGeneralParameters(cultureLabTestCohort);

    CohortDefinition applicationForLaboratoryResearchCohort =
        genericCohortQueries.generalSql(
            "applicationForLaboratoryResearchCohort",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                fichaClinica,
                applicationForLaboratoryResearch,
                Collections.singletonList(cultureTest)));
    addGeneralParameters(applicationForLaboratoryResearchCohort);

    CompositionCohortDefinition definition = new CompositionCohortDefinition();
    definition.setName("additionalTest");
    addGeneralParameters(definition);

    definition.addSearch(
        "cultureTestCohort", EptsReportUtils.map(cultureTestCohort, generalParameterMapping));
    definition.addSearch(
        "cultureLabTestCohort", EptsReportUtils.map(cultureLabTestCohort, generalParameterMapping));
    definition.addSearch(
        "applicationForLaboratoryResearchCohort",
        EptsReportUtils.map(applicationForLaboratoryResearchCohort, generalParameterMapping));
    definition.addSearch("mWRDCohort", EptsReportUtils.map(getmWRD(), generalParameterMapping));
    definition.addSearch(
        "smearMicroscopyOnlyCohort",
        EptsReportUtils.map(getSmearMicroscopyOnly(), generalParameterMapping));

    definition.addSearch(
        "tbLam",
        EptsReportUtils.map(
            txTbMonthlyCascadeCohortQueries.getPetientsHaveTBLAM(), generalParameterMapping));

    definition.setCompositionString(
        "(cultureTestCohort OR cultureLabTestCohort OR applicationForLaboratoryResearchCohort) AND NOT (mWRDCohort OR smearMicroscopyOnlyCohort OR tbLam)");
    return definition;
  }

  /**
   * <b>Description:</b> Get patients who have positive results returned
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPositiveResultsReturned(
      EncounterType laboratory,
      EncounterType fichaClinica,
      Concept basiloscopiaExam,
      Concept genexpertTest,
      Concept tbLamTest,
      Concept cultureTest,
      Concept mtbTest,
      Concept positive,
      Concept yes) {
    // genxpert both sources
    CohortDefinition genexpertTestCohort =
        genericCohortQueries.generalSql(
            "genexpertTestCohort",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                fichaClinica, genexpertTest, Arrays.asList(positive)));
    addGeneralParameters(genexpertTestCohort);

    CohortDefinition genexpertLabTestCohort =
        genericCohortQueries.generalSql(
            "genexpertLabTestCohort",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                laboratory, genexpertTest, Arrays.asList(positive)));
    addGeneralParameters(genexpertLabTestCohort);

    CohortDefinition basiloscopiaExamCohort = getSmearMicroscopyOnlyPositiveResult(true);
    CohortDefinition tbLamTestCohort =
        genericCohortQueries.generalSql(
            "tbLamTestCohort",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                fichaClinica, tbLamTest, Arrays.asList(positive)));
    addGeneralParameters(tbLamTestCohort);

    CohortDefinition tbLamLabTestCohort =
        genericCohortQueries.generalSql(
            "tbLamLabTestCohort",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                laboratory, tbLamTest, Arrays.asList(positive)));
    addGeneralParameters(tbLamLabTestCohort);

    CohortDefinition cultureTestCohort =
        genericCohortQueries.generalSql(
            "cultureTestCohort",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                fichaClinica, cultureTest, Arrays.asList(positive)));
    addGeneralParameters(cultureTestCohort);

    CohortDefinition cultureLabTestCohort =
        genericCohortQueries.generalSql(
            "cultureLabTestCohort",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                fichaClinica, cultureTest, Arrays.asList(positive)));
    addGeneralParameters(cultureLabTestCohort);

    CohortDefinition mtbTestCohort =
        genericCohortQueries.generalSql(
            "mtbTestCohort",
            TXTBQueries.tbGenexpertTest(
                laboratory.getEncounterTypeId(), mtbTest.getConceptId(), yes.getConceptId(), null));
    addGeneralParameters(mtbTestCohort);

    CompositionCohortDefinition definition = new CompositionCohortDefinition();
    definition.setName("positiveResultsReturned()");
    addGeneralParameters(definition);

    definition.addSearch(
        "genexpertTestCohort", EptsReportUtils.map(genexpertTestCohort, generalParameterMapping));
    definition.addSearch(
        "genexpertLabTestCohort",
        EptsReportUtils.map(genexpertLabTestCohort, generalParameterMapping));
    definition.addSearch(
        "basiloscopiaExamCohort",
        EptsReportUtils.map(basiloscopiaExamCohort, generalParameterMapping));
    definition.addSearch(
        "tbLamTestCohort", EptsReportUtils.map(tbLamTestCohort, generalParameterMapping));
    definition.addSearch(
        "tbLamLabTestCohort", EptsReportUtils.map(tbLamLabTestCohort, generalParameterMapping));
    definition.addSearch(
        "cultureTestCohort", EptsReportUtils.map(cultureTestCohort, generalParameterMapping));
    definition.addSearch(
        "cultureLabTestCohort", EptsReportUtils.map(cultureLabTestCohort, generalParameterMapping));
    definition.addSearch(
        "mtbTestCohort", EptsReportUtils.map(mtbTestCohort, generalParameterMapping));

    definition.setCompositionString(
        "genexpertTestCohort OR genexpertLabTestCohort OR basiloscopiaExamCohort OR tbLamTestCohort OR tbLamLabTestCohort OR cultureTestCohort OR cultureLabTestCohort OR mtbTestCohort");
    return definition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * Xpert MTB <b>(concept_id = 165189)</b> with Answer Yes <b>(concept_id = 1065)</b> or No <b>(id
   * = 1066)</b>
   *
   * <p>Registered in the laboratory form <b>(encounterType_id = 13)</b> during the reporting period
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getXpertMtbTestCohort() {
    CohortDefinition cd =
        genericCohortQueries.generalSql(
            "xpertMtbTest",
            TXTBQueries.tbGenexpertTest(
                hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId(),
                tbMetadata.getTestXpertMtbUuidConcept().getConceptId(),
                commonMetadata.getYesConcept().getConceptId(),
                commonMetadata.getNoConcept().getConceptId()));
    addGeneralParameters(cd);
    return cd;
  }

  /**
   * <b>Patients screened using Symptom Screen (alone)</b>
   *
   * <p>All patients on ART screened for TB (denominator) (TX_TB_FR2) that are not included in the
   * CXR screening type disaggregate
   *
   * @see #getDenominator()
   * @see #getPatientsScreenedUsingCXR()
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getSymtomScreen() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Patients screened using Symptom Screen (alone)");
    addGeneralParameters(cd);

    cd.addSearch(
        "DENOMINATOR",
        EptsReportUtils.map(
            getDenominator(), "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "CXR",
        EptsReportUtils.map(
            getPatientsScreenedUsingCXR(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("DENOMINATOR AND NOT CXR");
    return cd;
  }

  /**
   * <b>Patients screened using CXR</b>
   *
   * <p>All patients >=10 years of age (TX_TB_FR12) who have Investigações - Resultados
   * Laboratoriais with ANY RESULT recorded for Raio-X marked oin a Ficha Clíinica during the
   * reporting period
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsScreenedUsingCXR() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Patients screened using CXR");
    addGeneralParameters(cd);

    cd.addSearch(
        "XRAY",
        EptsReportUtils.map(
            getPatientsWithAnyXrayResult(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "AGE",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnReportEndDate(10, 200),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));

    cd.addSearch(
        "DENOMINATOR",
        EptsReportUtils.map(
            getDenominator(), "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("DENOMINATOR AND XRAY AND AGE");
    return cd;
  }

  /**
   * All patients who have Investigações - Resultados Laboratoriais with ANY RESULT recorded for
   * Raio-X marked oin a Ficha Clínica during the reporting period
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithAnyXrayResult() {
    SqlCohortDefinition cd = new SqlCohortDefinition();

    cd.setName("Patients with any result for Raio X");
    addGeneralParameters(cd);

    Map<String, Integer> map = new HashMap<String, Integer>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("12", tbMetadata.getXRayChest().getConceptId());

    String query =
        " SELECT p.patient_id "
            + "FROM patient p "
            + "    INNER JOIN encounter e "
            + "        ON e.patient_id = p.patient_id "
            + "    INNER JOIN obs o "
            + "        ON o.encounter_id = e.encounter_id "
            + "WHERE "
            + "  p.voided = 0  "
            + "  AND  e.voided = 0  "
            + "  AND  o.voided = 0  "
            + "  AND  e.encounter_type = ${6} "
            + "  AND o.concept_id = ${12} "
            + "  AND o.value_coded IS NOT NULL "
            + "  AND  e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "  AND  e.location_id  = :location";

    StringSubstitutor sb = new StringSubstitutor(map);
    String replaceQuery = sb.replace(query);

    cd.setQuery(replaceQuery);

    return cd;
  }

  /**
   * <b>Patients on ART TXTB </b>
   * <li>The system will identify patients on ART, as those who initiated the treatment,
   *     independently of the HF where the treatment initiated, during/before the reporting period
   *     as follows:
   * <li>All patients who have their first drug pick-up date set in Pharmacy form (FILA) during the
   *     reporting period;
   * <li>All patients who have initiated the drugs (ARV PLAN = START DRUGS) during the pharmacy or
   *     clinical visits during the reporting period;
   * <li>All patients who have the first historical start drugs date set during the reporting period
   *     in Pharmacy Tool (FILA) or Clinical tools (Ficha Clínica and Ficha Resumo - Mastercard,
   *     Ficha de Seguimento Adulto and Ficha de Seguimento Pediatria);
   * <li>All patients who have picked up drugs (Recepção Levantou ARV) – Master Card during the
   *     reporting period
   * <li>All patients enrolled in ART Program during the reporting period;
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsArtStartDateBeforeOrDuringPeriod(boolean duringPeriod) {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patient’s earliest ART start date from pick-up and clinical sources TXTB");
    addGeneralParameters(cd);

    String query =
        " SELECT patient_id "
            + " FROM ( "
            + commonQueries.getARTStartDate(true)
            + "       ) start "
            + " WHERE "
                .concat(
                    duringPeriod
                        ? " start.first_pickup BETWEEN :startDate AND :endDate "
                        : " start.first_pickup < :startDate ");

    cd.setQuery(query);
    return cd;
  }

  /**
   * <b>Description:</b> DenominatorScreenedOnly
   *
   * <p><b>Technical Specs</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getDenominatorScreenedOnly() {
    CompositionCohortDefinition definition = new CompositionCohortDefinition();
    addGeneralParameters(definition);
    definition.setName("TxTB - Denominator Screened Only");

    definition.addSearch("art-list", EptsReportUtils.map(artList(), generalParameterMapping));
    definition.addSearch(
        "tb-screening", EptsReportUtils.map(yesOrNoInvestigationResult(), generalParameterMapping));
    definition.addSearch(
        "tb-investigation",
        EptsReportUtils.map(positiveInvestigationResultComposition(), generalParameterMapping));
    definition.addSearch(
        "tb-investigation-negative",
        EptsReportUtils.map(negativeInvestigationResultComposition(), generalParameterMapping));
    definition.addSearch(
        "started-tb-treatment",
        EptsReportUtils.map(tbTreatmentStartDateWithinReportingDate(), generalParameterMapping));
    definition.addSearch(
        "in-tb-program", EptsReportUtils.map(getInTBProgram(), generalParameterMapping));
    definition.addSearch(
        "pulmonary-tb", EptsReportUtils.map(getPulmonaryTB(), generalParameterMapping));
    definition.addSearch(
        "marked-as-tb-treatment-start",
        EptsReportUtils.map(getTBTreatmentStart(), generalParameterMapping));

    definition.addSearch(
        "tuberculosis-symptomys",
        EptsReportUtils.map(getTuberculosisSymptoms(), generalParameterMapping));

    definition.addSearch(
        "active-tuberculosis",
        EptsReportUtils.map(getActiveTuberculosis(), generalParameterMapping));

    definition.addSearch(
        "tb-observations", EptsReportUtils.map(getTBObservation(), generalParameterMapping));

    definition.addSearch(
        "application-for-laboratory-research",
        EptsReportUtils.map(getApplicationForLaboratoryResearch(), generalParameterMapping));

    definition.addSearch(
        "tb-genexpert-test",
        EptsReportUtils.map(getTBGenexpertTestCohort(6), generalParameterMapping));

    definition.addSearch(
        "tb-genexpert-lab-test",
        EptsReportUtils.map(getTBGenexpertTestCohort(13), generalParameterMapping));
    definition.addSearch(
        "tb-xpert-mtb", EptsReportUtils.map(getXpertMtbTestCohort(), generalParameterMapping));

    definition.addSearch(
        "culture-test", EptsReportUtils.map(getCultureTest(6), generalParameterMapping));

    definition.addSearch(
        "culture-test-lab", EptsReportUtils.map(getCultureTest(13), generalParameterMapping));

    definition.addSearch(
        "test-tb-lam", EptsReportUtils.map(getTestTBLAM(6), generalParameterMapping));

    definition.addSearch(
        "test-tb-lam-lab", EptsReportUtils.map(getTestTBLAM(13), generalParameterMapping));

    definition.addSearch("test-bk", EptsReportUtils.map(getTestBK(6), generalParameterMapping));

    definition.addSearch(
        "x-ray-chest", EptsReportUtils.map(getTestXRayChest(6), generalParameterMapping));

    definition.addSearch(
        "result-for-basiloscopia",
        EptsReportUtils.map(getResultForBasiloscopia(), generalParameterMapping));

    definition.addSearch(
        "started-tb-treatment-previous-period",
        EptsReportUtils.map(
            tbTreatmentStartDateWithinReportingDate(),
            "startDate=${startDate-6m},endDate=${startDate-1d},location=${location}"));
    definition.addSearch(
        "in-tb-program-previous-period",
        EptsReportUtils.map(
            getPatientsInTBProgramInThePreviousPeriod(),
            "startDate=${startDate-6m},endDate=${startDate-1d},location=${location}"));
    definition.addSearch(
        "transferred-out",
        EptsReportUtils.map(
            getTransferredOut(), "startDate=${startDate},endDate=${endDate},location=${location}"));
    definition.addSearch(
        "pulmonary-tb-date",
        EptsReportUtils.map(
            getPulmonaryTBDate(),
            "startDate=${startDate-6m},endDate=${startDate-1d},location=${location}"));
    definition.addSearch(
        "marked-as-tratamento-tb-inicio",
        EptsReportUtils.map(
            getMarkedAsTratamentoTBInicio(),
            "startDate=${startDate-6m},endDate=${startDate-1d},location=${location}"));

    definition.setCompositionString(
        "(art-list AND (tb-screening OR tb-investigation OR tb-investigation-negative OR started-tb-treatment OR in-tb-program OR pulmonary-tb OR marked-as-tb-treatment-start "
            + "OR (tuberculosis-symptomys OR active-tuberculosis OR tb-observations OR application-for-laboratory-research OR tb-genexpert-test OR tb-genexpert-lab-test OR tb-xpert-mtb OR culture-test OR culture-test-lab "
            + "OR test-tb-lam OR test-tb-lam-lab OR test-bk OR x-ray-chest) OR result-for-basiloscopia)) ");

    return definition;
  }
}
