package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.eptsreports.reporting.calculation.tpt.CompletedIsoniazidTPTCalculation;
import org.openmrs.module.eptsreports.reporting.cohort.definition.CalculationCohortDefinition;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TPTCompletionCohortQueries {

  private HivMetadata hivMetadata;

  private TbMetadata tbMetadata;

  private TbPrevCohortQueries tbPrevCohortQueries;

  private TxCurrCohortQueries txCurrCohortQueries;

  private TXTBCohortQueries txTbCohortQueries;

  private GenericCohortQueries genericCohortQueries;

  private TPTEligiblePatientListCohortQueries tptEligiblePatientListCohortQueries;

  @Autowired
  public TPTCompletionCohortQueries(
      HivMetadata hivMetadata,
      TbMetadata tbMetadata,
      TbPrevCohortQueries tbPrevCohortQueries,
      TxCurrCohortQueries txCurrCohortQueries,
      TXTBCohortQueries txTbCohortQueries,
      GenericCohortQueries genericCohortQueries,
      TPTEligiblePatientListCohortQueries tptEligiblePatientListCohortQueries) {
    this.hivMetadata = hivMetadata;
    this.tbMetadata = tbMetadata;
    this.tbPrevCohortQueries = tbPrevCohortQueries;
    this.txCurrCohortQueries = txCurrCohortQueries;
    this.txTbCohortQueries = txTbCohortQueries;
    this.genericCohortQueries = genericCohortQueries;
    this.tptEligiblePatientListCohortQueries = tptEligiblePatientListCohortQueries;
  }

  private final String mapping = "endDate=${endDate},location=${location}";
  private final String mapping2 = "onOrBefore=${endDate},location=${location}";

  public CohortDefinition getPatientsThatCompletedProphylaticTreatment() {
    CalculationCohortDefinition cd =
        new CalculationCohortDefinition(
            Context.getRegisteredComponents(CompletedIsoniazidTPTCalculation.class).get(0));
    cd.setName("Patients that completed prophylatic treatment");
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
    return cd;
  }

  /**
   *
   *
   * <h4>TX_CURR with TPT Completion</h4>
   *
   * <ul>
   *   <li>
   *   <li>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getTxCurrWithTPTCompletion() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName("TX_CURR with TPT Completion");
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    compositionCohortDefinition.addSearch(
        "txcurr",
        EptsReportUtils.map(
            txCurrCohortQueries.getTxCurrCompositionCohort("txCurr", true),
            "onOrBefore=${endDate},location=${location},locations=${location}"));

    compositionCohortDefinition.addSearch("A1", EptsReportUtils.map(getINHStartA1(), mapping));

    compositionCohortDefinition.addSearch("A2", EptsReportUtils.map(getINHStartA2(), mapping));

    compositionCohortDefinition.addSearch(
        "A3",
        EptsReportUtils.map(
            getINHStartA3(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "A4",
        EptsReportUtils.map(
            getINHStartA4(
                hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "A5",
        EptsReportUtils.map(
            getINHStartA5(
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.getIsoniazidConcept().getConceptId(),
                tbMetadata.getIsoniazidePiridoxinaConcept().getConceptId(),
                tbMetadata.getTreatmentFollowUpTPTConcept().getConceptId(),
                hivMetadata.getContinueRegimenConcept().getConceptId(),
                hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId(),
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                hivMetadata.getIsoniazidUsageConcept().getConceptId(),
                hivMetadata.getStartDrugs().getConceptId(),
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "A6",
        EptsReportUtils.map(
            getINHStartA6(
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.getIsoniazidConcept().getConceptId(),
                tbMetadata.getIsoniazidePiridoxinaConcept().getConceptId(),
                hivMetadata.getPatientTreatmentFollowUp().getConceptId(),
                hivMetadata.getStartDrugs().getConceptId(),
                hivMetadata.getRestartConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "B1",
        EptsReportUtils.map(
            tptEligiblePatientListCohortQueries.getIPTB1(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                hivMetadata.getDataFinalizacaoProfilaxiaIsoniazidaConcept().getConceptId(),
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getIsoniazidUsageConcept().getConceptId(),
                hivMetadata.getStartDrugs().getConceptId(),
                hivMetadata.getCompletedConcept().getConceptId(),
                hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId(),
                hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.getIsoniazidConcept().getConceptId(),
                tbMetadata.getIsoniazidePiridoxinaConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "B2",
        EptsReportUtils.map(
            tptEligiblePatientListCohortQueries.getIPTB2(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getIsoniazidUsageConcept().getConceptId(),
                hivMetadata.getStartDrugs().getConceptId(),
                hivMetadata.getCompletedConcept().getConceptId(),
                hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId(),
                hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.getIsoniazidConcept().getConceptId(),
                tbMetadata.getIsoniazidePiridoxinaConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "B3",
        EptsReportUtils.map(
            tptEligiblePatientListCohortQueries.getIPTB3(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                hivMetadata.getDataFinalizacaoProfilaxiaIsoniazidaConcept().getConceptId(),
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getIsoniazidUsageConcept().getConceptId(),
                hivMetadata.getStartDrugs().getConceptId(),
                hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId(),
                hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.getIsoniazidConcept().getConceptId(),
                tbMetadata.getIsoniazidePiridoxinaConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "B4",
        EptsReportUtils.map(
            tptEligiblePatientListCohortQueries.getIPTB4(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                hivMetadata.getDataFinalizacaoProfilaxiaIsoniazidaConcept().getConceptId(),
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getIsoniazidUsageConcept().getConceptId(),
                hivMetadata.getStartDrugs().getConceptId(),
                hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId(),
                hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.getIsoniazidConcept().getConceptId(),
                tbMetadata.getIsoniazidePiridoxinaConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "B5Part1",
        EptsReportUtils.map(
            tptEligiblePatientListCohortQueries.getIPTB5Part1(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getIsoniazidUsageConcept().getConceptId(),
                hivMetadata.getStartDrugs().getConceptId(),
                hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId(),
                hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getContinueRegimenConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "B5Part2",
        EptsReportUtils.map(
            tptEligiblePatientListCohortQueries.getIPTB5Part2(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getIsoniazidUsageConcept().getConceptId(),
                hivMetadata.getStartDrugs().getConceptId(),
                hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId(),
                hivMetadata.getContinueRegimenConcept().getConceptId(),
                tbMetadata.getTreatmentPrescribedConcept().getConceptId(),
                tbMetadata.getDtINHConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "B5Part3",
        EptsReportUtils.map(
            tptEligiblePatientListCohortQueries.getIPTB5Part3(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getIsoniazidUsageConcept().getConceptId(),
                hivMetadata.getStartDrugs().getConceptId(),
                hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId(),
                hivMetadata.getContinueRegimenConcept().getConceptId(),
                tbMetadata.getTreatmentPrescribedConcept().getConceptId(),
                tbMetadata.getDtINHConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "B6Part1",
        EptsReportUtils.map(
            tptEligiblePatientListCohortQueries.getIPTB6Part1(
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.getIsoniazidConcept().getConceptId(),
                tbMetadata.getIsoniazidePiridoxinaConcept().getConceptId(),
                hivMetadata.getMonthlyConcept().getConceptId(),
                tbMetadata.getTypeDispensationTPTConceptUuid().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "B6Part2",
        EptsReportUtils.map(
            tptEligiblePatientListCohortQueries.getIPTB6Part2(
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.getIsoniazidConcept().getConceptId(),
                tbMetadata.getIsoniazidePiridoxinaConcept().getConceptId(),
                tbMetadata.getTypeDispensationTPTConceptUuid().getConceptId(),
                hivMetadata.getQuarterlyConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "B6Part3",
        EptsReportUtils.map(
            tptEligiblePatientListCohortQueries.getIPTB6Part3(
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.getIsoniazidConcept().getConceptId(),
                tbMetadata.getIsoniazidePiridoxinaConcept().getConceptId(),
                hivMetadata.getMonthlyConcept().getConceptId(),
                tbMetadata.getTypeDispensationTPTConceptUuid().getConceptId(),
                hivMetadata.getQuarterlyConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "C1",
        EptsReportUtils.map(
            get3HPStartC1(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                tbMetadata.getTreatmentPrescribedConcept().getConceptId(),
                tbMetadata.get3HPConcept().getConceptId(),
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.get3HPPiridoxinaConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "C2",
        EptsReportUtils.map(
            get3HPStartC2(
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.get3HPConcept().getConceptId(),
                tbMetadata.get3HPPiridoxinaConcept().getConceptId(),
                hivMetadata.getPatientTreatmentFollowUp().getConceptId(),
                hivMetadata.getStartDrugs().getConceptId(),
                hivMetadata.getRestartConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "C3",
        EptsReportUtils.map(
            get3HPStartC3(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                tbMetadata.getTreatmentPrescribedConcept().getConceptId(),
                tbMetadata.get3HPConcept().getConceptId(),
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.get3HPPiridoxinaConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch("C4", EptsReportUtils.map(get3HPStartC4(), mapping));

    compositionCohortDefinition.addSearch(
        "C5",
        EptsReportUtils.map(
            getINHStartC5Query(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.get3HPConcept().getConceptId(),
                tbMetadata.getDataEstadoDaProfilaxiaConcept().getConceptId(),
                hivMetadata.getStartDrugs().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "C6",
        EptsReportUtils.map(
            getINHStartC6Query(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                tbMetadata.getTreatmentPrescribedConcept().getConceptId(),
                tbMetadata.getDT3HPConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch("C7", EptsReportUtils.map(get3HPStartC7(), mapping));

    compositionCohortDefinition.addSearch(
        "D1",
        EptsReportUtils.map(
            get3HPD1(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                tbMetadata.getTreatmentPrescribedConcept().getConceptId(),
                tbMetadata.get3HPConcept().getConceptId(),
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.get3HPPiridoxinaConcept().getConceptId(),
                    hivMetadata.getMasterCardEncounterType().getEncounterTypeId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "D2",
        EptsReportUtils.map(
            get3HPD2(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                tbMetadata.get3HPConcept().getConceptId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.getDataEstadoDaProfilaxiaConcept().getConceptId(),
                hivMetadata.getStartDrugs().getConceptId(),
                hivMetadata.getCompletedConcept().getConceptId(),
                    hivMetadata.getMasterCardEncounterType().getEncounterTypeId()),
            mapping));

    compositionCohortDefinition.addSearch("D3", EptsReportUtils.map(get3HPD3(), mapping));

    compositionCohortDefinition.addSearch("D4", EptsReportUtils.map(get3HPD4(), mapping));

    compositionCohortDefinition.addSearch(
        "completedAll",
        EptsReportUtils.map(getPatientsThatCompletedIsoniazidProphylacticTreatment(), mapping2));

    compositionCohortDefinition.setCompositionString(
        "txcurr AND (((A1 OR A2 OR A3 OR A4 OR A5 OR A6) AND (B1 OR B2 OR B3 OR B4 OR (B5Part1 OR B5Part3 OR B5Part3) OR (B6Part1 OR B6Part3 OR B6Part3))) OR ((C1 OR C2 OR C3 OR C4 OR C5 OR C6 OR C7) AND (D1 OR D2 OR D3 OR D4)))");

    return compositionCohortDefinition;
  }

  /**
   *
   *
   * <h4>TX_CURR without TPT Completion</h4>
   *
   * <ul>
   *   <li>
   *   <li>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getTxCurrWithoutTPTCompletion() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName("TX_CURR without TPT Completion");
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    compositionCohortDefinition.addSearch(
        "txcurr",
        EptsReportUtils.map(
            txCurrCohortQueries.getTxCurrCompositionCohort("txCurr", true),
            "onOrBefore=${endDate},location=${location},locations=${location}"));

    compositionCohortDefinition.addSearch(
        "tpt0", EptsReportUtils.map(getTxCurrWithTPTCompletion(), mapping));

    compositionCohortDefinition.setCompositionString("txcurr AND NOT tpt0");

    return compositionCohortDefinition;
  }

  /**
   *
   *
   * <h4>TX_CURR without TPT Completion with TB Treatment</h4>
   *
   * <ul>
   *   <li>
   *   <li>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getTxCurrWithoutTPTCompletionWithTB() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName("TX_CURR without TPT Completion with TB Treatment");
    compositionCohortDefinition.addParameter(new Parameter("endDate", "end Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    String generalParameterMapping =
        "startDate=${endDate-1095d},endDate=${endDate},location=${location}";

    compositionCohortDefinition.addSearch(
        "tpt1", EptsReportUtils.map(getTxCurrWithoutTPTCompletion(), mapping));

    compositionCohortDefinition.addSearch(
        "E", EptsReportUtils.map(txTbCohortQueries.txTbNumerator(), generalParameterMapping));

    compositionCohortDefinition.setCompositionString("tpt1 AND E");

    return compositionCohortDefinition;
  }

  /**
   *
   *
   * <h4>TX_CURR without TPT Completion with Positive TB Screening</h4>
   *
   * <ul>
   *   <li>
   *   <li>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getTxCurrWithoutTPTCompletionWithPositiveTBScreening() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName(
        "TX_CURR without TPT Completion with Positive TB Screening");
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    String generalParameterMapping =
        "startDate=${endDate-14d},endDate=${endDate},location=${location}";

    compositionCohortDefinition.addSearch(
        "tpt1", EptsReportUtils.map(getTxCurrWithoutTPTCompletion(), mapping));

    compositionCohortDefinition.addSearch(
        "Denominator",
        EptsReportUtils.map(txTbCohortQueries.getDenominator(), generalParameterMapping));

    compositionCohortDefinition.addSearch(
        "PositiveScreening",
        EptsReportUtils.map(txTbCohortQueries.positiveScreening(), generalParameterMapping));

    // F = Dnominator AND Positive Screening

    compositionCohortDefinition.setCompositionString(
        "tpt1 AND (Denominator AND PositiveScreening)");

    return compositionCohortDefinition;
  }

  /**
   *
   *
   * <h4>TX_CURR eligible for TPT Completion</h4>
   *
   * <ul>
   *   <li>
   *   <li>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getTxCurrEligibleForTPTCompletion() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName("TX_CURR eligible for TPT Completion");
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    compositionCohortDefinition.addSearch(
        "tpt1", EptsReportUtils.map(getTxCurrWithoutTPTCompletion(), mapping));

    compositionCohortDefinition.addSearch(
        "tpt2", EptsReportUtils.map(getTxCurrWithoutTPTCompletionWithTB(), mapping));

    compositionCohortDefinition.addSearch(
        "tpt3",
        EptsReportUtils.map(getTxCurrWithoutTPTCompletionWithPositiveTBScreening(), mapping));

    compositionCohortDefinition.setCompositionString("tpt1 AND NOT (tpt2 OR tpt3)");

    return compositionCohortDefinition;
  }

  /**
   *
   *
   * <h4>TX_CURR with TPT in last 7 months</h4>
   *
   * <ul>
   *   <li>
   *   <li>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getTxCurrWithTPTInLast7Months() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName("TX_CURR with TPT in last 7 months");
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    String generalParameterMapping =
        "onOrAfter=${endDate-210d},onOrBefore=${endDate},location=${location}";

    compositionCohortDefinition.addSearch(
        "tpt1", EptsReportUtils.map(getTxCurrWithoutTPTCompletion(), mapping));

    compositionCohortDefinition.addSearch(
        "G", EptsReportUtils.map(getTBPrevDenominator(), generalParameterMapping));

    compositionCohortDefinition.setCompositionString("tpt1 AND G");

    return compositionCohortDefinition;
  }

  /**
   *
   *
   * <h4>TX_CURR eligible for TPT Initiation</h4>
   *
   * <ul>
   *   <li>
   *   <li>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getTxCurrEligibleForTPTInitiation() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName("TX_CURR eligible for TPT Initiation");
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    compositionCohortDefinition.addSearch(
        "tpt4", EptsReportUtils.map(getTxCurrEligibleForTPTCompletion(), mapping));

    compositionCohortDefinition.addSearch(
        "tpt5", EptsReportUtils.map(getTxCurrWithTPTInLast7Months(), mapping));

    compositionCohortDefinition.setCompositionString("tpt4 AND NOT tpt5");

    return compositionCohortDefinition;
  }

  /**
   * <b>IMER1</b>: User_Story_ TPT <br>
   *
   * <ul>
   *   <li>B - INH final and D - 3HP Final
   *   <li>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsThatCompletedIsoniazidProphylacticTreatment() {
    CalculationCohortDefinition cd =
        new CalculationCohortDefinition(
            Context.getRegisteredComponents(CompletedIsoniazidTPTCalculation.class).get(0));
    cd.setName("Patients that completed Isoniazid prophylatic treatment");
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
    return cd;
  }

  /**
   *
   *
   * <h4>User_Story_ TPT</h4>
   *
   * <ul>
   *   <li>A1.1 : Select all patients with Ficha Resumo (encounter type 53) with “Ultima profilaxia
   *       Isoniazida (Data Inicio)” (concept id 6128) and value datetime not null and exclude all
   *       patients with Última profilaxia(concept id 23985) value coded 3HP(concept id 23954) and
   *       before end date.
   *   <li>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getA1Part1() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName("TPT Completion A1.1");
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    compositionCohortDefinition.addSearch(
        "A1Part1Include",
        EptsReportUtils.map(
            getINHStartA1Part1Include(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "A1Part1Exclude",
        EptsReportUtils.map(
            getINHStartA1Part1Exclude(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.get3HPConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.setCompositionString("A1Part1Include AND NOT A1Part1Exclude");

    return compositionCohortDefinition;
  }

  /**
   *
   *
   * <h4>User_Story_ TPT</h4>
   *
   * <ul>
   *   <li>A1.2: Select all patients with Ficha Resumo (encounter type 53) with Última
   *       profilaxia(concept id 23985) value coded INH(concept id 656) and Data Início da
   *       Profilaxia TPT(value datetime, concept id 6128) not null and before end date.
   *   <li>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getA1Part2() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName("TPT Completion A1.2");
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    compositionCohortDefinition.addSearch(
        "A1Part2A",
        EptsReportUtils.map(
            getINHStartA1Part2A(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.getIsoniazidConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "A1Part2B",
        EptsReportUtils.map(
            getINHStartA1Part1Include(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.setCompositionString("A1Part2A AND A1Part2B");

    return compositionCohortDefinition;
  }

  /**
   *
   *
   * <h4>User_Story_ TPT: A1 : (A1.1 or A1.2)</h4>
   *
   * <ul>
   *   <li>A1.1 : Select all patients with Ficha Resumo (encounter type 53) with “Ultima profilaxia
   *       Isoniazida (Data Inicio)” (concept id 6128) and value datetime not null and exclude all
   *       patients with Última profilaxia(concept id 23985) value coded 3HP(concept id 23954) and
   *       before end date.
   *       <p>A1.2: Select all patients with Ficha Resumo (encounter type 53) with Última
   *       profilaxia(concept id 23985) value coded INH(concept id 656) and Data Início da
   *       Profilaxia TPT(value datetime, concept id 6128) not null and before end date.
   *   <li>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getINHStartA1() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName("TPT Completion A1");
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    compositionCohortDefinition.addSearch("A1Part1", EptsReportUtils.map(getA1Part1(), mapping));

    compositionCohortDefinition.addSearch("A1Part2", EptsReportUtils.map(getA1Part2(), mapping));

    compositionCohortDefinition.setCompositionString("A1Part1 OR A1Part2");

    return compositionCohortDefinition;
  }

  /**
   * <b>IMER1</b>: User_Story_ TPT <br>
   *
   * <ul>
   *   <li>A2: Select all patients with Ficha clinica (encounter type 6) with “Profilaxia INH”
   *       (concept id 6122) with value code “Inicio” (concept id 1256) or Profilaxia TPT (concept
   *       id 23985) value coded INH (concept id 656) and Estado da Profilaxia (concept id 165308)
   *       value coded Início (concept id 1256) and encounter datetime before end date
   *   <li>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getINHStartA2() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName("TPT Completion A2");
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    compositionCohortDefinition.addSearch(
        "getINHStartA2Part1",
        EptsReportUtils.map(
            getINHStartA2Part1(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                hivMetadata.getStartDrugs().getConceptId(),
                tbMetadata.getIsoniazidConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.addSearch(
        "getINHStartA2Part2",
        EptsReportUtils.map(
            getINHStartA2Part2(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.getIsoniazidConcept().getConceptId(),
                tbMetadata.getDataEstadoDaProfilaxiaConcept().getConceptId(),
                hivMetadata.getStartDrugs().getConceptId()),
            mapping));

    compositionCohortDefinition.setCompositionString("getINHStartA2Part1 OR getINHStartA2Part2");

    return compositionCohortDefinition;
  }

  /**
   * <b>IMER1</b>: User_Story_ TPT <br>
   *
   * <ul>
   *   <li>A -A1: Select all patients with Ficha Resumo (encounter type 53) with “Ultima profilaxia
   *       Isoniazida (Data Inicio)” (concept id 6128) value datetime not null and before end date
   *   <li>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getINHStartA1Part1Include(
      int masterCardEncounterType, int dataInicioProfilaxiaIsoniazidaConcept) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName(" all patients with Ultima profilaxia Isoniazida (Data Inicio)");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", masterCardEncounterType);
    map.put("6128", dataInicioProfilaxiaIsoniazidaConcept);

    String query =
        "  SELECT"
            + "  p.patient_id"
            + "  FROM"
            + "  patient p"
            + "     INNER JOIN"
            + "  encounter e ON p.patient_id = e.patient_id"
            + "     INNER JOIN"
            + " obs o ON e.encounter_id = o.encounter_id"
            + " WHERE"
            + " p.voided = 0 AND e.voided = 0 AND o.voided = 0"
            + " AND e.encounter_type = ${53}"
            + " AND o.concept_id = ${6128}"
            + " AND o.value_datetime IS NOT NULL"
            + " AND o.value_datetime < :endDate"
            + " AND e.location_id = :location ";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>IMER1</b>: User_Story_ TPT <br>
   *
   * <ul>
   *   <li>A -A1.1 Part1 : Select all patients with Última profilaxia(concept id 23985) value coded
   *       3HP(concept id 23954) and before end date.
   *   <li>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getINHStartA1Part1Exclude(
      int masterCardEncounterType, int regimeTPTConcept, int threeHPConceptConcept) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName(" all patients with Ultima profilaxia Isoniazida (Data Inicio)");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", masterCardEncounterType);
    map.put("23985", regimeTPTConcept);
    map.put("23954", threeHPConceptConcept);

    String query =
        "  SELECT"
            + "  p.patient_id"
            + "  FROM"
            + "  patient p"
            + "     INNER JOIN"
            + "  encounter e ON p.patient_id = e.patient_id"
            + "     INNER JOIN"
            + " obs o ON e.encounter_id = o.encounter_id"
            + " WHERE"
            + " p.voided = 0 AND e.voided = 0 AND o.voided = 0"
            + " AND e.encounter_type = ${53}"
            + " AND o.concept_id = ${23985}"
            + " AND o.value_coded = ${23954}"
            + " AND e.encounter_datetime < :endDate"
            + " AND e.location_id = :location ";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>IMER1</b>: User_Story_ TPT <br>
   *
   * <ul>
   *   <li>A -A1.2: Select all patients with Ficha Resumo (encounter type 53) with Última
   *       profilaxia(concept id 23985) value coded INH(concept id 656)
   *   <li>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getINHStartA1Part2A(
      int masterCardEncounterType, int regimeTPTConcept, int isoniazidConcept) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName(" all patients with Ultima profilaxia Isoniazida (Ficha Resumo)");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", masterCardEncounterType);
    map.put("23985", regimeTPTConcept);
    map.put("656", isoniazidConcept);

    String query =
        "  SELECT"
            + "  p.patient_id"
            + "  FROM"
            + "  patient p"
            + "     INNER JOIN"
            + "  encounter e ON p.patient_id = e.patient_id"
            + "     INNER JOIN"
            + " obs o ON e.encounter_id = o.encounter_id"
            + " WHERE"
            + " p.voided = 0 AND e.voided = 0 AND o.voided = 0"
            + " AND e.encounter_type = ${53}"
            + " AND o.concept_id = ${23985}"
            + " AND o.value_coded = ${656}"
            + " AND o.value_datetime < :endDate"
            + " AND e.location_id = :location ";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>IMER1</b>: User_Story_ TPT <br>
   *
   * <ul>
   *   <li>A2: Select all patients with Ficha clinica (encounter type 6) with “Profilaxia INH”
   *       (concept id 6122) with value code “Inicio” (concept id 1256) and encounter datetime
   *       before end date
   *   <li>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getINHStartA2Part1(
      int adultoSeguimentoEncounterType, int startDrugsConcept, int isoniazidUsageConcept) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName(" all patients with Profilaxia INH");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", adultoSeguimentoEncounterType);
    map.put("6122", isoniazidUsageConcept);
    map.put("1256", startDrugsConcept);

    String query =
        " SELECT"
            + " p.patient_id"
            + " FROM"
            + " patient p"
            + " INNER JOIN"
            + " encounter e ON p.patient_id = e.patient_id"
            + " INNER JOIN"
            + " obs o ON e.encounter_id = o.encounter_id"
            + " WHERE"
            + " p.voided = 0 AND e.voided = 0 AND o.voided = 0"
            + "    AND e.encounter_type = ${6}"
            + "    AND o.concept_id = ${6122}"
            + "    AND o.value_coded = ${1256}"
            + "    AND e.encounter_datetime < :endDate"
            + "    AND e.location_id = :location";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>IMER1</b>: User_Story_ TPT <br>
   *
   * <ul>
   *   <li>A2: Select all patients Profilaxia TPT (concept id 23985) value coded INH (concept id
   *       656) and Estado da Profilaxia (concept id 165308) value coded Início (concept id 1256)
   *       and encounter datetime before end date
   *   <li>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getINHStartA2Part2(
      int adultoSeguimentoEncounterType,
      int regimeTPTConcept,
      int isoniazidConcept,
      int dataEstadoDaProfilaxiaConcept,
      int startDrugsConcept) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName(" all patients with Profilaxia TPT");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", adultoSeguimentoEncounterType);
    map.put("23985", regimeTPTConcept);
    map.put("656", isoniazidConcept);
    map.put("165308", dataEstadoDaProfilaxiaConcept);
    map.put("1256", startDrugsConcept);

    String query =
        " SELECT"
            + " p.patient_id"
            + " FROM"
            + " patient p"
            + " INNER JOIN"
            + " encounter e ON p.patient_id = e.patient_id"
            + " INNER JOIN"
            + " obs o ON e.encounter_id = o.encounter_id"
            + " INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id    "
            + " WHERE"
            + " p.voided = 0 AND e.voided = 0 AND o.voided = 0"
            + "    AND e.encounter_type = ${6}"
            + " AND (o.concept_id = ${23985} AND o.value_coded = ${656})   "
            + " AND (o2.concept_id = ${165308} AND o2.value_coded = ${1256})   "
            + "    AND e.encounter_datetime < :endDate"
            + "    AND e.location_id = :location";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>IMER1</b>: User_Story_ TPT <br>
   *
   * <ul>
   *   <li>A3: Select all patients with Ficha clinica (encounter type 6) with “Profilaxia com INH”
   *       (concept id 6128) value datetime before end date
   *   <li>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getINHStartA3(int encounterType, int profilaxiaIsoniazidaConcept) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName(" all patients with Ficha Clinica ");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", encounterType);
    map.put("6128", profilaxiaIsoniazidaConcept);

    String query =
        "SELECT p.patient_id FROM patient p "
            + "INNER JOIN encounter e ON p.patient_id  = e.patient_id "
            + "INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "WHERE e.encounter_type = ${6} "
            + "AND o.concept_id = ${6128} "
            + "AND e.location_id = :location "
            + "AND e.encounter_datetime < :endDate "
            + "AND p.voided = 0 AND e.voided = 0 AND o.voided = 0";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>IMER1</b>: User_Story_ TPT <br>
   *
   * <ul>
   *   <li>A4: Select all patients with Ficha Seguimento PEdiatrico (encounter type 9) with
   *       “Profilaxia com INH” (concept id 6128) value datetime before end date
   *   <li>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getINHStartA4(
      int pediatriaSeguimentoEncounterType, int dataInicioProfilaxiaIsoniazidaConcept) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with Ficha Seguimento Pediatrico ");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("9", pediatriaSeguimentoEncounterType);
    map.put("6128", dataInicioProfilaxiaIsoniazidaConcept);

    String query =
        " SELECT "
            + "	p.patient_id "
            + " FROM "
            + "  	patient p "
            + "     	INNER JOIN "
            + " 	encounter e ON p.patient_id = e.patient_id "
            + "     	INNER JOIN "
            + " 	obs o ON e.encounter_id = o.encounter_id "
            + " WHERE "
            + " 	p.voided = 0 AND e.voided = 0 "
            + "     	AND o.voided = 0 "
            + "     	AND e.encounter_type = ${9} "
            + "     	AND o.concept_id = ${6128} "
            + " 	AND e.location_id = :location "
            + "     	AND e.encounter_datetime < :endDate ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>IMER1</b>: User_Story_ TPT <br>
   *
   * <ul>
   *   <li>A5: Select all patients with FILT (encounter type 60) with “Regime de TPT” (concept id
   *       23985) value coded ‘Isoniazid’ or ‘Isoniazid + piridoxina’ (concept id in [656, 23982])
   *       and encounter datetime before the reporting period
   *   <li>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getINHStartA5(
      int regimeTPTEncounterType,
      int regimeTPTConcept,
      int isoniazidConcept,
      int isoniazidePiridoxinaConcept,
      int seguimentoTPTConcept,
      int continuaConcept,
      int dataInicioConcept,
      int fichaResumoEncounterType,
      int profilaxiaConcept,
      int inicioConcept,
      int fichaClinicaEncounterType,
      int adultoseguimentoEncounterType) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName(" all patients with Regime de TPT");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("60", regimeTPTEncounterType);
    map.put("23985", regimeTPTConcept);
    map.put("656", isoniazidConcept);
    map.put("23982", isoniazidePiridoxinaConcept);
    map.put("23987", seguimentoTPTConcept);
    map.put("1257", continuaConcept);
    map.put("6128", dataInicioConcept);
    map.put("53", fichaResumoEncounterType);
    map.put("6122", profilaxiaConcept);
    map.put("1256", inicioConcept);
    map.put("6", fichaClinicaEncounterType);
    map.put("9", adultoseguimentoEncounterType);

    String query =
        " SELECT p.patient_id FROM patient p    "
            + " INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + " INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + " INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id    "
            + " WHERE e.encounter_type = ${60}    "
            + " AND (o.concept_id = ${23985} AND o.value_coded IN(${656}, ${23982}))   "
            + " AND (o2.concept_id = ${23987} AND o2.value_coded IN (${1257} , null))   "
            + " AND e.location_id = :location   "
            + " AND e.encounter_datetime < :endDate   "
            + " AND p.voided = 0 AND e.voided = 0 AND o.voided = 0   "
            + " AND p.patient_id NOT IN (   "
            + "    SELECT p.patient_id FROM patient p    "
            + "    INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "    INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "    INNER JOIN (   "
            + "        SELECT p.patient_id, e.encounter_datetime FROM patient p    "
            + "        INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "        INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "        INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id    "
            + "        WHERE e.encounter_type = ${60}    "
            + "        AND (o.concept_id = ${23985} AND o.value_coded IN(${656}, ${23982}))   "
            + "        AND (o2.concept_id = ${23987} AND o2.value_coded IN (${1257} , null))   "
            + "        AND e.location_id = :location   "
            + "        AND e.encounter_datetime < :endDate   "
            + "        AND p.voided = 0 AND e.voided = 0 AND o.voided = 0   "
            + "    ) AS filt    "
            + "    ON filt.patient_id = p.patient_id   "
            + "    WHERE e.encounter_type = ${60}    "
            + "    AND o.concept_id = ${23985} AND o.value_coded IN(${656}, ${23982})   "
            + "    AND e.location_id = :location   "
            + "    AND e.encounter_datetime BETWEEN DATE_SUB(filt.encounter_datetime, INTERVAL 7 MONTH) AND filt.encounter_datetime   "
            + "    AND p.voided = 0 AND e.voided = 0 AND o.voided = 0   "
            + " )   "
            + " AND p.patient_id NOT IN (   "
            + "    SELECT p.patient_id FROM patient p    "
            + "    INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "    INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "    INNER JOIN (   "
            + "        SELECT p.patient_id, e.encounter_datetime FROM patient p    "
            + "        INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "        INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "        INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id    "
            + "        WHERE e.encounter_type = ${60}    "
            + "        AND (o.concept_id = ${23985} AND o.value_coded IN(${656}, ${23982}))   "
            + "        AND (o2.concept_id = ${23987} AND o2.value_coded IN (${1257} , null))           "
            + "        AND e.location_id = :location   "
            + "        AND e.encounter_datetime < :endDate   "
            + "        AND p.voided = 0 AND e.voided = 0 AND o.voided = 0   "
            + "    ) AS fichaResumo    "
            + "    ON fichaResumo.patient_id = p.patient_id   "
            + "    WHERE e.encounter_type = ${53}   "
            + "    AND o.concept_id = ${6128}    "
            + "    AND e.location_id = :location   "
            + "    AND e.encounter_datetime BETWEEN DATE_SUB(fichaResumo.encounter_datetime, INTERVAL 7 MONTH) AND fichaResumo.encounter_datetime   "
            + "    AND p.voided = 0 AND e.voided = 0 AND o.voided = 0   "
            + " )   "
            + " AND p.patient_id NOT IN (   "
            + "    SELECT p.patient_id FROM patient p    "
            + "    INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "    INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "    INNER JOIN (   "
            + "        SELECT p.patient_id, e.encounter_datetime FROM patient p    "
            + "        INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "        INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "        INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id    "
            + "        WHERE e.encounter_type = ${60}    "
            + "        AND (o.concept_id = ${23985} AND o.value_coded IN(${656}, ${23982}))   "
            + "        AND (o2.concept_id = ${23987} AND o2.value_coded IN (${1257} , null))           "
            + "        AND e.location_id = :location   "
            + "        AND e.encounter_datetime < :endDate   "
            + "        AND p.voided = 0 AND e.voided = 0 AND o.voided = 0   "
            + "    ) AS fichaClinica    "
            + "    ON fichaClinica.patient_id = p.patient_id   "
            + "    WHERE e.encounter_type = ${6}   "
            + "    AND o.concept_id = ${6122}    "
            + "    AND o.value_coded = ${1256}   "
            + "    AND e.location_id = :location   "
            + "    AND e.encounter_datetime BETWEEN DATE_SUB(fichaClinica.encounter_datetime, INTERVAL 7 MONTH) AND fichaClinica.encounter_datetime   "
            + "    AND p.voided = 0 AND e.voided = 0 AND o.voided = 0   "
            + " )   "
            + " AND p.patient_id NOT IN (   "
            + "    SELECT p.patient_id FROM patient p    "
            + "    INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "    INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "    INNER JOIN (   "
            + "        SELECT p.patient_id, e.encounter_datetime FROM patient p    "
            + "        INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "        INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "        INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id    "
            + "        WHERE e.encounter_type = ${60}    "
            + "        AND (o.concept_id = ${23985} AND o.value_coded IN(${656}, ${23982}))   "
            + "        AND (o2.concept_id = ${23987} AND o2.value_coded IN (${1257} , null))           "
            + "        AND e.location_id = :location   "
            + "        AND e.encounter_datetime < :endDate   "
            + "        AND p.voided = 0 AND e.voided = 0 AND o.voided = 0   "
            + "    ) AS seguimento    "
            + "    ON seguimento.patient_id = p.patient_id   "
            + "    WHERE e.encounter_type IN(${6},${9})   "
            + "    AND o.concept_id = ${6128}    "
            + "    AND e.location_id = :location   "
            + "    AND e.encounter_datetime BETWEEN DATE_SUB(seguimento.encounter_datetime, INTERVAL 7 MONTH) AND seguimento.encounter_datetime   "
            + "    AND p.voided = 0 AND e.voided = 0 AND o.voided = 0   "
            + " ) ";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>IMER1</b>: User_Story_ TPT <br>
   *
   * <ul>
   *   <li>A6: Select all patients with “Regime de TPT” (concept id 23985) value coded ‘Isoniazid’
   *       or ‘Isoniazid + piridoxina’ (concept id in [656, 23982]) and “Seguimento de tratamento
   *       TPT”(concept ID 23987) value coded “inicio” or “re-inicio” (concept ID in [1256, 1705])
   *       marked on FILT (encounter type 60) and encounter datetime before end date Note: RegimeTPT
   *       and Seguimento de Tratamento TPT should be on the same encounter.
   *   <li>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getINHStartA6(
      int regimeTPTEncounterType,
      int regimeTPTConcept,
      int isoniazidConcept,
      int isoniazidePiridoxinaConcept,
      int seguimentoTPTConcept,
      int inicioConcept,
      int reinicioConcept) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName("all patients with Regime de TPT and Seguimento de tratamento TPT");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("60", regimeTPTEncounterType);
    map.put("23985", regimeTPTConcept);
    map.put("656", isoniazidConcept);
    map.put("23982", isoniazidePiridoxinaConcept);
    map.put("23987", seguimentoTPTConcept);
    map.put("1256", inicioConcept);
    map.put("1705", reinicioConcept);

    String query =
        " SELECT "
            + "  p.patient_id "
            + "  FROM "
            + "  patient p "
            + "  INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "  INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "  INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id  "
            + "  WHERE "
            + "  p.voided = 0  "
            + "  AND e.voided = 0  "
            + "  AND o.voided = 0 "
            + "  AND e.encounter_type = ${60} "
            + "  AND (o.concept_id = ${23985} AND o.value_coded IN (${656} , ${23982})) "
            + "  AND (o2.concept_id = ${23987} AND o2.value_coded IN (${1256} , ${1705})) "
            + "  AND e.encounter_datetime < :endDate  "
            + "  AND e.location_id = :location";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>IMER1</b>: User_Story_ TPT <br>
   *
   * <ul>
   *   <li>C1: Select all patients with Ficha Clinica - Master Card (encounter type 6) with “Outras
   *       prescricoes” (concept id 1719) with value coded equal to “3HP” (concept id 23954) and
   *       encounter datetime before end date;
   *   <li>
   *
   * @return CohortDefinition
   */
  public CohortDefinition get3HPStartC1(
      int encounterType,
      int treatmentPrescribedConcept,
      int threeHPConcept,
      int filtEncounterType,
      int regimedeTPConcept,
      int threeHPPiridoxinaConcept) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName(" all patients with Ficha Clinica Master Card ");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "After Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", encounterType);
    map.put("1719", treatmentPrescribedConcept);
    map.put("23954", threeHPConcept);
    map.put("60", filtEncounterType);
    map.put("23985", regimedeTPConcept);
    map.put("23984", threeHPPiridoxinaConcept);

    String query =
        " SELECT p.patient_id FROM patient p    "
            + "INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "WHERE e.encounter_type = ${6}    "
            + "AND o.concept_id = ${1719} AND o.value_coded = ${23954}    "
            + "AND e.location_id = :location    "
            + "AND e.encounter_datetime < :endDate   "
            + "AND p.voided = 0 AND e.voided = 0 AND o.voided = 0   "
            + "AND p.patient_id NOT IN (   "
            + "   SELECT p.patient_id FROM patient p    "
            + "   INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "   INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "   INNER JOIN (   "
            + "      SELECT p.patient_id, e.encounter_datetime FROM patient p    "
            + "      INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "      INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "      WHERE e.encounter_type = ${6}    "
            + "      AND o.concept_id = ${1719} AND o.value_coded = ${23954}    "
            + "      AND e.location_id = :location    "
            + "      AND e.encounter_datetime < :endDate   "
            + "      AND p.voided = 0 AND e.voided = 0 AND o.voided = 0   "
            + "   ) AS fichaClinica    "
            + "   ON fichaClinica.patient_id = p.patient_id   "
            + "   WHERE e.encounter_type = ${6}    "
            + "   AND o.concept_id = ${1719} AND o.value_coded = ${23954}    "
            + "   AND e.location_id = :location    "
            + "   AND e.encounter_datetime BETWEEN DATE_SUB(fichaClinica.encounter_datetime, INTERVAL 4 MONTH) AND fichaClinica.encounter_datetime   "
            + "   AND p.voided = 0 AND e.voided = 0 AND o.voided = 0   "
            + ")   "
            + "AND p.patient_id NOT IN (   "
            + "   SELECT p.patient_id FROM patient p    "
            + "   INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "   INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "   INNER JOIN (   "
            + "      SELECT p.patient_id, e.encounter_datetime FROM patient p    "
            + "      INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "      INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "      WHERE e.encounter_type = ${6}    "
            + "      AND o.concept_id = ${1719} AND o.value_coded = ${23954}    "
            + "      AND e.location_id = :location    "
            + "      AND e.encounter_datetime < :endDate   "
            + "      AND p.voided = 0 AND e.voided = 0 AND o.voided = 0   "
            + "   ) AS regimeTPT    "
            + "   ON regimeTPT.patient_id = p.patient_id   "
            + "   WHERE e.encounter_type = ${60}    "
            + "   AND o.concept_id = ${23985} AND o.value_coded IN(${23954},${23984})   "
            + "   AND e.location_id = :location   "
            + "   AND e.encounter_datetime BETWEEN DATE_SUB(regimeTPT.encounter_datetime, INTERVAL 4 MONTH) AND regimeTPT.encounter_datetime   "
            + "   AND p.voided = 0 AND e.voided = 0 AND o.voided = 0   "
            + ") ";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>IMER1</b>: User_Story_ TPT <br>
   *
   * <ul>
   *   <li>C2: Select all patients with FILT (encounter type 60) with “Regime de TPT” (concept id
   *       23985) value coded “3HP” or “3HP+Piridoxina” (concept id in [23954, 23984]) and
   *       “Seguimento de tratamento TPT”(concept ID 23987) value coded “inicio” or
   *       “re-inicio”(concept ID in [1256, 1705]) and encounter datetime before end date; Note:
   *       RegimeTPT and Seguimento de Tratamento TPT should be on the same encounter.
   *   <li>
   *
   * @return CohortDefinition
   */
  public CohortDefinition get3HPStartC2(
      int fILT,
      int regimeTPTConcept,
      int hPConcept,
      int hPPiridoxinaConcept,
      int seguimentoTPTConcept,
      int inicioConcept,
      int reinicioConcept) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with FILT ");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("60", fILT);
    map.put("23985", regimeTPTConcept);
    map.put("23954", hPConcept);
    map.put("23984", hPPiridoxinaConcept);
    map.put("23987", seguimentoTPTConcept);
    map.put("1256", inicioConcept);
    map.put("1705", reinicioConcept);

    String query =
        " SELECT  p.patient_id FROM patient p   "
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id   "
            + "INNER JOIN obs o ON e.encounter_id = o.encounter_id   "
            + "INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id   "
            + "WHERE   "
            + "  e.encounter_type = ${60} AND p.voided = 0   "
            + "  AND e.voided = 0   "
            + "  AND o.voided = 0   "
            + "  AND ( o.concept_id = ${23985} AND o.value_coded in (${23954},${23984}) )  "
            + "  AND (o2.concept_id = ${23987} AND o2.value_coded IN (${1256} , ${1705}))  "
            + "  AND e.location_id = :location  "
            + "  AND e.encounter_datetime < :endDate ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>IMER1</b>: User_Story_ TPT <br>
   *
   * <ul>
   *   <li>C3: Select all patients with FILT (encounter type 60) with “Regime de TPT” (concept id
   *       23985) value coded “3HP” or ” 3HP+Piridoxina” (concept id in [23954, 23984]) and
   *       encounter_datetime before end date as “3HP Start Date” no other 3HP prescriptions
   *       [“Outras prescricoes” (concept id 1719) with value coded equal to “3HP” (concept id
   *       23954)] marked on Ficha-Clínica in the 4 months prior to the 3HP Start Date and no
   *       “Regime de TPT” (concept id 23985) with value coded “3HP” or ” 3HP+Piridoxina” (concept
   *       id in [23954, 23984]) marked on FILT (encounter type 60) in the 4 months prior to the 3HP
   *       Start Date;
   *   <li>
   *
   * @return CohortDefinition
   */
  public CohortDefinition get3HPStartC3(
      int encounterType,
      int treatmentPrescribedConcept,
      int threeHPConcept,
      int filtEncounterType,
      int regimedeTPConcept,
      int threeHPPiridoxinaConcept) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName(" all patients with Ficha Clinica Master Card ");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "After Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", encounterType);
    map.put("1719", treatmentPrescribedConcept);
    map.put("23954", threeHPConcept);
    map.put("60", filtEncounterType);
    map.put("23985", regimedeTPConcept);
    map.put("23984", threeHPPiridoxinaConcept);

    String query =
        " SELECT p.patient_id FROM patient p    "
            + "INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "WHERE e.encounter_type = ${60}    "
            + "AND o.concept_id = ${23985} AND o.value_coded IN(${23954}, ${23984})   "
            + "AND e.location_id = :location    "
            + "AND e.encounter_datetime < :endDate   "
            + "AND p.voided = 0 AND e.voided = 0 AND o.voided = 0   "
            + "AND p.patient_id NOT IN (   "
            + "   SELECT p.patient_id FROM patient p    "
            + "   INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "   INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "   INNER JOIN (   "
            + "      SELECT p.patient_id, e.encounter_datetime FROM patient p    "
            + "      INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "      INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + " WHERE e.encounter_type = ${60}    "
            + " AND o.concept_id = ${23985} AND o.value_coded IN(${23954}, ${23984})   "
            + "      AND e.location_id = :location    "
            + "      AND e.encounter_datetime < :endDate   "
            + "      AND p.voided = 0 AND e.voided = 0 AND o.voided = 0   "
            + "   ) AS fichaClinica    "
            + "   ON fichaClinica.patient_id = p.patient_id   "
            + "   WHERE e.encounter_type = ${6}    "
            + "   AND o.concept_id = ${1719} AND o.value_coded = ${23954}    "
            + "   AND e.location_id = :location    "
            + "   AND e.encounter_datetime BETWEEN DATE_SUB(fichaClinica.encounter_datetime, INTERVAL 4 MONTH) AND fichaClinica.encounter_datetime   "
            + "   AND p.voided = 0 AND e.voided = 0 AND o.voided = 0   "
            + ")   "
            + "AND p.patient_id NOT IN (   "
            + "   SELECT p.patient_id FROM patient p    "
            + "   INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "   INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "   INNER JOIN (   "
            + "      SELECT p.patient_id, e.encounter_datetime FROM patient p    "
            + "      INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "      INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + " WHERE e.encounter_type = ${60}    "
            + " AND o.concept_id = ${23985} AND o.value_coded IN(${23954}, ${23984})   "
            + "      AND e.location_id = :location    "
            + "      AND e.encounter_datetime < :endDate   "
            + "      AND p.voided = 0 AND e.voided = 0 AND o.voided = 0   "
            + "   ) AS regimeTPT    "
            + "   ON regimeTPT.patient_id = p.patient_id   "
            + "   WHERE e.encounter_type = ${60}    "
            + "   AND o.concept_id = ${23985} AND o.value_coded IN(${23954},${23984})   "
            + "   AND e.location_id = :location   "
            + "   AND e.encounter_datetime BETWEEN DATE_SUB(regimeTPT.encounter_datetime, INTERVAL 4 MONTH) AND regimeTPT.encounter_datetime   "
            + "   AND p.voided = 0 AND e.voided = 0 AND o.voided = 0   "
            + ") ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   *
   *
   * <h4>User_Story_ TPT</h4>
   *
   * <ul>
   *   <li>C4: Select all patients with Última profilaxia(concept id 23985) value coded 3HP(concept
   *       id 23954) and Data Início da Profilaxia TPT(value datetime, concept id 6128) registered
   *       before end date on Ficha Resumo (Encounter type 53) ; or
   *   <li>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition get3HPStartC4() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName("3HP Start C4");
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    compositionCohortDefinition.addSearch(
        "C4Part1",
        EptsReportUtils.map(
            getINHStartA1Part1Exclude(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.get3HPConcept().getConceptId()),
            mapping));
    compositionCohortDefinition.addSearch(
        "C4Part2",
        EptsReportUtils.map(
            getINHStartA1Part1Include(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.setCompositionString("C4Part1 AND C4Part2");

    return compositionCohortDefinition;
  }

  /**
   * <b>IMER1</b>: User_Story_ TPT <br>
   *
   * <ul>
   *   <li>C5: Select all patients with Profilaxia TPT (concept id 23985) value coded 3HP (concept
   *       id 23954) * and Estado da Profilaxia (concept id 165308) value coded Início (concept id
   *       1256) * registered reporting end date on Ficha Clinica (Encounter type 6) ; or
   *   <li>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getINHStartC5Query(
      int adultoSeguimentoEncounterType,
      int regimeTPTConcept,
      int threeHPConcept,
      int dataEstadoDaProfilaxiaConcept,
      int startDrugsConcept) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName("C5 Query: all patients with Profilaxia TPT");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", adultoSeguimentoEncounterType);
    map.put("23985", regimeTPTConcept);
    map.put("23954", threeHPConcept);
    map.put("165308", dataEstadoDaProfilaxiaConcept);
    map.put("1256", startDrugsConcept);

    String query =
        " SELECT"
            + " p.patient_id"
            + " FROM"
            + " patient p"
            + " INNER JOIN"
            + " encounter e ON p.patient_id = e.patient_id"
            + " INNER JOIN"
            + " obs o ON e.encounter_id = o.encounter_id"
            + " INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id    "
            + " WHERE"
            + " p.voided = 0 AND e.voided = 0 AND o.voided = 0"
            + "    AND e.encounter_type = ${6}"
            + " AND (o.concept_id = ${23985} AND o.value_coded = ${23954})   "
            + " AND (o2.concept_id = ${165308} AND o2.value_coded = ${1256})   "
            + "    AND e.encounter_datetime < :endDate"
            + "    AND e.location_id = :location";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>IMER1</b>: User_Story_ TPT <br>
   *
   * <ul>
   *   <li>C6: Select all patients with Outras prescricoes(concept id 1719) value coded DT-3HP
   *       (concept id 165307) on Ficha clinica (encounter type 6) before end date; or
   *   <li>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getINHStartC6Query(
      int adultoSeguimentoEncounterType, int treatmentPrescribedConcept, int DT3HPConcept) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName("C6 Query: all patients with Outras prescricoes");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", adultoSeguimentoEncounterType);
    map.put("1719", treatmentPrescribedConcept);
    map.put("165307", DT3HPConcept);

    String query =
        " SELECT"
            + " p.patient_id"
            + " FROM"
            + " patient p"
            + " INNER JOIN"
            + " encounter e ON p.patient_id = e.patient_id"
            + " INNER JOIN"
            + " obs o ON e.encounter_id = o.encounter_id"
            + " WHERE"
            + " p.voided = 0 AND e.voided = 0 AND o.voided = 0"
            + "    AND e.encounter_type = ${6}"
            + " AND o.concept_id = ${1719} AND o.value_coded = ${165307}   "
            + "    AND e.encounter_datetime < :endDate"
            + "    AND e.location_id = :location";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   *
   *
   * <h4>User_Story_ TPT</h4>
   *
   * <ul>
   *   <li>C4: Select all patients with “Regime de TPT” (concept id 23985) with value coded “3HP” or
   *       ” 3HP+Piridoxina” (concept id in [23954, 23984]) and “Seguimento de tratamento
   *       TPT”(concept ID 23987) value coded “continua” or “fim” or no value(concept ID in [1257,
   *       1267, null]) marked on the first FILT (encounter type 60) and encounter datetime before
   *       end date and: No other Regime de TPT (concept id 23985) value coded “3HP” or ”
   *       3HP+Piridoxina” (concept id in [23954, 23984]) marked on FILT (encounter type 60) in the
   *       4 months prior to the FILT 3HP start date. ; and No other 3HP start dates marked on Ficha
   *       clinica (encounter type 6, encounter datetime) with Profilaxia TPT (concept id 23985)
   *       value coded 3HP (concept id 23954) and Estado da Profilaxia (concept id 165308) value
   *       coded Início (concept id 1256) or Outras prescrições (concept id 1719) value coded 3HP or
   *       DT-3HP (concept id in [23954,165307])in the 4 months prior to the FILT 3HP start date. ;
   *       and No other 3HP start dates marked on Ficha Resumo (encounter type 53) with Última
   *       profilaxia(concept id 23985) value coded 3HP(concept id 23954) and Data Início da
   *       Profilaxia TPT(value datetime, concept id 6128) in the 4 months prior to the FILT 3HP
   *       start date. ;
   *   <li>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition get3HPStartC7() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName("3HP Start C4");
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    compositionCohortDefinition.addSearch(
        "C4Part1",
        EptsReportUtils.map(
            getINHStartC7Part1Query(
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.get3HPConcept().getConceptId(),
                tbMetadata.get3HPPiridoxinaConcept().getConceptId(),
                tbMetadata.getTreatmentFollowUpTPTConcept().getConceptId(),
                hivMetadata.getContinueRegimenConcept().getConceptId(),
                hivMetadata.getCompletedConcept().getConceptId()),
            mapping));
    compositionCohortDefinition.addSearch(
        "C4Part2",
        EptsReportUtils.map(
            getINHStartC7Part2Query(
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.get3HPConcept().getConceptId(),
                tbMetadata.get3HPPiridoxinaConcept().getConceptId()),
            mapping));
    compositionCohortDefinition.addSearch(
        "C4Part3",
        EptsReportUtils.map(
            getINHStartC7Part3Query(
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.get3HPConcept().getConceptId(),
                tbMetadata.get3HPPiridoxinaConcept().getConceptId(),
                tbMetadata.getDataEstadoDaProfilaxiaConcept().getConceptId(),
                hivMetadata.getStartDrugs().getConceptId(),
                tbMetadata.getTreatmentPrescribedConcept().getConceptId(),
                tbMetadata.getDT3HPConcept().getConceptId()),
            mapping));
    compositionCohortDefinition.addSearch(
        "C4Part4",
        EptsReportUtils.map(
            getINHStartC7Part4Query(
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.get3HPConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.setCompositionString("C4Part1 AND C4Part2 AND C4Part3 AND C4Part4");

    return compositionCohortDefinition;
  }

  /**
   * <b>IMER1</b>: User_Story_ TPT <br>
   *
   * <ul>
   *   <li>C7 Part 1: Select all patients with “Regime de TPT” (concept id 23985) with value coded
   *       “3HP” or ” 3HP+Piridoxina” (concept id in [23954, 23984]) and “Seguimento de tratamento
   *       TPT”(concept ID 23987) value coded “continua” or “fim” or no value(concept ID in [1257,
   *       1267, null]) marked on the first FILT (encounter type 60) and encounter datetime before
   *       end date and:
   *   <li>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getINHStartC7Part1Query(
      int regimeTPTEncounterType,
      int regimeTPTConcept,
      int threeHPConcept,
      int threeHPPiridoxinaConcept,
      int treatmentFollowUpTPTConcept,
      int continueRegimenConcept,
      int completedConcept) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName(
        "C7 Part 1 Query: all patients with Regime de TPT and Seguimento de tratamento TPT");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("60", regimeTPTEncounterType);
    map.put("23985", regimeTPTConcept);
    map.put("23954", threeHPConcept);
    map.put("23984", threeHPPiridoxinaConcept);
    map.put("23987", treatmentFollowUpTPTConcept);
    map.put("1257", continueRegimenConcept);
    map.put("1267", completedConcept);

    String query =
        " SELECT"
            + " p.patient_id"
            + " FROM"
            + " patient p"
            + " INNER JOIN"
            + " encounter e ON p.patient_id = e.patient_id"
            + " INNER JOIN"
            + " obs o ON e.encounter_id = o.encounter_id"
            + " INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id    "
            + " WHERE"
            + " p.voided = 0 AND e.voided = 0 AND o.voided = 0"
            + "    AND e.encounter_type = ${60}"
            + " AND (o.concept_id = ${23985} AND o.value_coded IN(${23954}, ${23984}))   "
            + " AND (o2.concept_id = ${23987} AND o2.value_coded IN (${1257}, ${1267}, null))   "
            + "    AND e.encounter_datetime < :endDate"
            + "    AND e.location_id = :location";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>IMER1</b>: User_Story_ TPT <br>
   *
   * <ul>
   *   <li>C7 Part 2: No other Regime de TPT (concept id 23985) value coded “3HP” or ”
   *       3HP+Piridoxina” (concept id in [23954, 23984]) marked on FILT (encounter type 60) in the
   *       4 months prior to the FILT 3HP start date.
   *   <li>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getINHStartC7Part2Query(
      int regimeTPTEncounterType,
      int regimeTPTConcept,
      int threeHPConcept,
      int threeHPPiridoxinaConcept) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName("C7 Part 2 Query: all patients with Regime de TPT marked on FILT");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("60", regimeTPTEncounterType);
    map.put("23985", regimeTPTConcept);
    map.put("23954", threeHPConcept);
    map.put("23984", threeHPPiridoxinaConcept);

    String query =
        " SELECT p.patient_id FROM patient p    "
            + "INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "INNER JOIN (   "
            + "SELECT p.patient_id, e.encounter_datetime FROM patient p    "
            + "INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "WHERE e.encounter_type = ${60}    "
            + "AND o.concept_id = ${23985} AND o.value_coded IN(${23954}, ${23984})   "
            + "AND e.location_id = :location       "
            + "AND e.encounter_datetime < :endDate "
            + "AND p.voided = 0 AND e.voided = 0 AND o.voided = 0   "
            + ") AS regimeTPT    "
            + "ON regimeTPT.patient_id = p.patient_id   "
            + "WHERE e.encounter_type = ${60}   "
            + "AND o.concept_id = ${23985} AND o.value_coded IN(${23954},${23984})   "
            + "AND e.location_id = :location   "
            + "AND e.encounter_datetime BETWEEN DATE_SUB(regimeTPT.encounter_datetime, INTERVAL 4 MONTH) AND regimeTPT.encounter_datetime   "
            + "AND p.voided = 0 AND e.voided = 0 AND o.voided = 0 ";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>IMER1</b>: User_Story_ TPT <br>
   *
   * <ul>
   *   <li>C7 Part 3: No other 3HP start dates marked on Ficha clinica (encounter type 6, encounter
   *       datetime) with Profilaxia TPT (concept id 23985) value coded 3HP (concept id 23954) and
   *       Estado da Profilaxia (concept id 165308) value coded Início (concept id 1256) or Outras
   *       prescrições (concept id 1719) value coded 3HP or DT-3HP (concept id in [23954,165307])in
   *       the 4 months prior to the FILT 3HP start date. ; and
   *   <li>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getINHStartC7Part3Query(
      int regimeTPTEncounterType,
      int adultoSeguimentoEncounterType,
      int regimeTPTConcept,
      int threeHPConcept,
      int threeHPPiridoxinaConcept,
      int dataEstadoDaProfilaxiaConcept,
      int startDrugsConcept,
      int treatmentPrescribedConcept,
      int DT3HPConcept) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName("C7 Part 3 Query: all patients with Regime de TPT marked on FILT");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("60", regimeTPTEncounterType);
    map.put("6", adultoSeguimentoEncounterType);
    map.put("23985", regimeTPTConcept);
    map.put("23954", threeHPConcept);
    map.put("23984", threeHPPiridoxinaConcept);
    map.put("165308", dataEstadoDaProfilaxiaConcept);
    map.put("1256", startDrugsConcept);
    map.put("1719", treatmentPrescribedConcept);
    map.put("165307", DT3HPConcept);

    String query =
        " SELECT p.patient_id FROM patient p    "
            + "INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id "
            + "INNER JOIN (   "
            + "SELECT p.patient_id, e.encounter_datetime FROM patient p    "
            + "INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "WHERE e.encounter_type = ${60}    "
            + "AND o.concept_id = ${23985} AND o.value_coded IN(${23954}, ${23984})   "
            + "AND e.location_id = :location       "
            + "AND e.encounter_datetime < :endDate "
            + "AND p.voided = 0 AND e.voided = 0 AND o.voided = 0   "
            + ") AS regimeTPT    "
            + "ON regimeTPT.patient_id = p.patient_id   "
            + "WHERE e.encounter_type = ${6}   "
            + "AND (o.concept_id = ${23985} AND o.value_coded = ${23954}) "
            + "AND (o2.concept_id = ${165308} AND o2.value_coded = ${1256})   "
            + "OR (o.concept_id = ${1719} AND o.value_coded IN (${23954},${165307})) "
            + "AND e.location_id = :location   "
            + "AND e.encounter_datetime BETWEEN DATE_SUB(regimeTPT.encounter_datetime, INTERVAL 4 MONTH) AND regimeTPT.encounter_datetime   "
            + "AND p.voided = 0 AND e.voided = 0 AND o.voided = 0 ";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>IMER1</b>: User_Story_ TPT <br>
   *
   * <ul>
   *   <li>C7 Part 4: No other 3HP start dates marked on Ficha Resumo (encounter type 53) with
   *       Última profilaxia(concept id 23985) value coded 3HP(concept id 23954) and Data Início da
   *       Profilaxia TPT(value datetime, concept id 6128) in the 4 months prior to the FILT 3HP
   *       start date. ;
   *   <li>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getINHStartC7Part4Query(
      int regimeTPTEncounterType,
      int masterCardEncounterType,
      int regimeTPTConcept,
      int threeHPConcept) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName("C7 Part 4 Query: all patients with Regime de TPT marked on FILT");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("60", regimeTPTEncounterType);
    map.put("53", masterCardEncounterType);
    map.put("23985", regimeTPTConcept);
    map.put("23954", threeHPConcept);

    String query =
        " SELECT p.patient_id FROM patient p    "
            + "INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "INNER JOIN (   "
            + "SELECT p.patient_id, e.encounter_datetime FROM patient p    "
            + "INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "WHERE e.encounter_type = ${60}    "
            + "AND o.concept_id = ${23985} AND o.value_coded = ${23954} "
            + "AND e.location_id = :location       "
            + "AND e.encounter_datetime < :endDate "
            + "AND p.voided = 0 AND e.voided = 0 AND o.voided = 0   "
            + ") AS regimeTPT    "
            + "ON regimeTPT.patient_id = p.patient_id   "
            + "WHERE e.encounter_type = ${53}   "
            + "AND o.concept_id = ${23985} AND o.value_coded = ${23954} "
            + "AND e.location_id = :location   "
            + "AND o.value_datetime BETWEEN DATE_SUB(regimeTPT.encounter_datetime, INTERVAL 4 MONTH) AND regimeTPT.encounter_datetime   "
            + "AND p.voided = 0 AND e.voided = 0 AND o.voided = 0 ";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>IMER1</b>:User Story TPT Completion Patient List <br>
   *
   * <ul>
   *   <li>67) D1: Select all patients with Última profilaxia(concept id 23985) value coded
   *       3HP(concept id 23954) and Data Fim da Profilaxia TPT(value datetime, concept id 6129)
   *       registered on Ficha Resumo by end date (Encounter type 53) and with value datetime
   *       between 86 days and 365 days from the date of C3 or
   *   <li>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition get3HPD1(
      int encounterType,
      int treatmentPrescribedConcept,
      int threeHPConcept,
      int filtEncounterType,
      int regimedeTPConcept,
      int threeHPPiridoxinaConcept,
      int masterCardEncounterType) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with Outras prescricoes D1");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", encounterType);
    map.put("1719", treatmentPrescribedConcept);
    map.put("23954", threeHPConcept);
    map.put("60", filtEncounterType);
    map.put("23985", regimedeTPConcept);
    map.put("23984", threeHPPiridoxinaConcept);
    map.put("53", masterCardEncounterType);


    String query =
        " SELECT p.patient_id FROM patient p    "
            + "INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "INNER JOIN (   "
            + " SELECT p.patient_id, e.encounter_datetime FROM patient p    "
            + "INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "WHERE e.encounter_type = ${60}    "
            + "AND o.concept_id = ${23985} AND o.value_coded IN(${23954}, ${23984})   "
            + "AND e.location_id = :location    "
            + "AND e.encounter_datetime < :endDate   "
            + "AND p.voided = 0 AND e.voided = 0 AND o.voided = 0   "
            + "AND p.patient_id NOT IN (   "
            + "   SELECT p.patient_id FROM patient p    "
            + "   INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "   INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "   INNER JOIN (   "
            + "      SELECT p.patient_id, e.encounter_datetime FROM patient p    "
            + "      INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "      INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + " WHERE e.encounter_type = ${60}    "
            + " AND o.concept_id = ${23985} AND o.value_coded IN(${23954}, ${23984})   "
            + "      AND e.location_id = :location    "
            + "      AND e.encounter_datetime < :endDate   "
            + "      AND p.voided = 0 AND e.voided = 0 AND o.voided = 0   "
            + "   ) AS fichaClinica    "
            + "   ON fichaClinica.patient_id = p.patient_id   "
            + "   WHERE e.encounter_type = ${6}    "
            + "   AND o.concept_id = ${1719} AND o.value_coded = ${23954}    "
            + "   AND e.location_id = :location    "
            + "   AND e.encounter_datetime BETWEEN DATE_SUB(fichaClinica.encounter_datetime, INTERVAL 4 MONTH) AND fichaClinica.encounter_datetime   "
            + "   AND p.voided = 0 AND e.voided = 0 AND o.voided = 0   "
            + ")   "
            + "AND p.patient_id NOT IN (   "
            + "   SELECT p.patient_id FROM patient p    "
            + "   INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "   INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "   INNER JOIN (   "
            + "      SELECT p.patient_id, e.encounter_datetime FROM patient p    "
            + "      INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "      INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + " WHERE e.encounter_type = ${60}    "
            + " AND o.concept_id = ${23985} AND o.value_coded IN(${23954}, ${23984})   "
            + "      AND e.location_id = :location    "
            + "      AND e.encounter_datetime < :endDate   "
            + "      AND p.voided = 0 AND e.voided = 0 AND o.voided = 0   "
            + "   ) AS regimeTPT   "
            + "   ON regimeTPT.patient_id = p.patient_id   "
            + "   WHERE e.encounter_type = ${60}    "
            + "   AND o.concept_id = ${23985} AND o.value_coded IN(${23954},${23984})   "
            + "   AND e.location_id = :location   "
            + "   AND e.encounter_datetime BETWEEN DATE_SUB(regimeTPT.encounter_datetime, INTERVAL 4 MONTH) AND regimeTPT.encounter_datetime   "
            + "   AND p.voided = 0 AND e.voided = 0 AND o.voided = 0   "
            + ") ) AS regimeTPT    "
            + "ON regimeTPT.patient_id = p.patient_id   "
            + "WHERE e.encounter_type = ${53}   "
            + "AND o.concept_id = ${23985} AND o.value_coded = ${23954} "
            + "AND e.location_id = :location   "
            + "AND DATEDIFF(o.value_datetime, regimeTPT.encounter_datetime) >= 86 AND DATEDIFF(o.value_datetime, regimeTPT.encounter_datetime) <= 365   "
            + "AND p.voided = 0 AND e.voided = 0 AND o.voided = 0 ";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>IMER1</b>:User Story TPT Completion Patient List <br>
   *
   * <ul>
   *   <li>D2: Select all patients withProfilaxia TPT (concept id 23985) value coded 3HP (concept id
   *       23954) and Estado da Profilaxia (concept id 165308) value coded Fim (concept id 1267)
   *       registered on Ficha clinica(encounter type 6) by end date and encounter datetime between
   *       86 days and 365 days from the date of C5; OR
   *   <li>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition get3HPD2(
      int adultoSeguimentoEncounterType,
      int threeHPConcept,
      int regimedeTPConcept,
      int dataEstadoDaProfilaxiaConcept,
      int startDrugs,
      int completedConcept,
      int masterCardEncounterType) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with Outras prescricoes D1");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", adultoSeguimentoEncounterType);
    map.put("23954", threeHPConcept);
    map.put("23985", regimedeTPConcept);
    map.put("165308", dataEstadoDaProfilaxiaConcept);
    map.put("1256", startDrugs);
    map.put("1267", completedConcept);
    map.put("53", masterCardEncounterType);


    String query =
        " SELECT p.patient_id FROM patient p    "
            + "INNER JOIN encounter e ON p.patient_id  = e.patient_id    "
            + "INNER JOIN obs o ON e.encounter_id = o.encounter_id    "
            + "INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id     "
            + "INNER JOIN (SELECT "
            + "             p.patient_id, e.encounter_datetime "
            + "             FROM "
            + "             patient p "
            + "             INNER JOIN "
            + "             encounter e ON p.patient_id = e.patient_id "
            + "             INNER JOIN "
            + "             obs o ON e.encounter_id = o.encounter_id "
            + "             INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id     "
            + "             WHERE "
            + "             p.voided = 0 AND e.voided = 0 AND o.voided = 0 "
            + "                AND e.encounter_type = ${6} "
            + "             AND (o.concept_id = ${23985} AND o.value_coded = ${23954})    "
            + "             AND (o2.concept_id = ${165308} AND o2.value_coded = ${1256})    "
            + "                AND e.encounter_datetime < :endDate "
            + "                AND e.location_id = :location ) AS regimeTPT    "
            + "ON regimeTPT.patient_id = p.patient_id   "
            + "WHERE e.encounter_type = ${53}   "
            + "AND (o.concept_id = ${23985} AND o.value_coded = ${23954})    "
            + "AND (o2.concept_id = ${165308} AND o2.value_coded = ${1267})    "
            + "AND e.location_id = :location   "
            + "AND DATEDIFF(o.value_datetime, regimeTPT.encounter_datetime) >= 86 AND DATEDIFF(o.value_datetime, regimeTPT.encounter_datetime) <= 365   "
            + "AND p.voided = 0 AND e.voided = 0 AND o.voided = 0 ";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   *
   *
   * <h4>User_Story_ TPT</h4>
   *
   * <ul>
   *   <li>D3: Select all pacientes from C and check if The date from M is registered on Ficha
   *       Clinica - Master Card (encounter type 6) or Ficha Resumo (encounter type 53) and: (a) The
   *       patient has at least 3 consultations (encounter type 6) with “Outras prescricoes”
   *       (concept id 1719) with value coded equal to “3HP” (concept id 23954) or with Profilaxia
   *       TPT (concept id 23985) value coded 3HP (concept id 23954) and Estado da Profilaxia
   *       (concept id 165308) value coded Início/continua (concept id in [1256,1257]) during 120
   *       days from the date from C1; or (b) At least 1 consultation registered on Ficha Clínica
   *       (encounter type 6) with DT-3HP (concept ID 1719, value_coded =165307) during 120 days
   *       from the date from C1; or
   *   <li>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition get3HPD3() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName("3HP Final D3");
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    compositionCohortDefinition.addSearch(
        "get3HPD3A",
        EptsReportUtils.map(
            get3HPD3A(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                tbMetadata.get3HPConcept().getConceptId(),
                tbMetadata.getTreatmentPrescribedConcept().getConceptId(),
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.get3HPPiridoxinaConcept().getConceptId(),
                tbMetadata.getDataEstadoDaProfilaxiaConcept().getConceptId(),
                hivMetadata.getStartDrugs().getConceptId(),
                hivMetadata.getContinueRegimenConcept().getConceptId()),
            mapping));
    compositionCohortDefinition.addSearch(
        "get3HPD3B",
        EptsReportUtils.map(
            get3HPD3B(
                hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
                tbMetadata.get3HPConcept().getConceptId(),
                tbMetadata.getTreatmentPrescribedConcept().getConceptId(),
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.get3HPPiridoxinaConcept().getConceptId(),
                tbMetadata.getDT3HPConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.setCompositionString("get3HPD3A OR get3HPD3B");

    return compositionCohortDefinition;
  }

  /**
   * <b>IMER1</b>:User Story TPT Completion Patient List <br>
   *
   * <ul>
   *   <li>D3 A: The date from C is registered on Ficha Clinica - Master Card (encounter type 6) or
   *       Ficha Resumo (encounter type 53) and:
   *   <li>The patient has at least 3 consultations (encounter type 6) with “Outras prescricoes”
   *       (concept id 1719) with value coded equal to “3HP” (concept id 23954) during 120 days from
   *       the date from C1; or
   *   <li>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition get3HPD3A(
      int adultoSeguimentoEncounterType,
      int threeHPConcept,
      int treatmentPrescribedConcept,
      int filtEncounterType,
      int regimedeTPConcept,
      int threeHPPiridoxinaConcept,
      int dataEstadoDaProfilaxiaConcept,
      int startDrugs,
      int continueRegimenConcept) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with Outras prescricoes D1");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", adultoSeguimentoEncounterType);
    map.put("23954", threeHPConcept);
    map.put("1719", treatmentPrescribedConcept);
    map.put("60", filtEncounterType);
    map.put("23985", regimedeTPConcept);
    map.put("23984", threeHPPiridoxinaConcept);
    map.put("165308", dataEstadoDaProfilaxiaConcept);
    map.put("1256", startDrugs);
    map.put("1257", continueRegimenConcept);

    String query =
        " SELECT p.patient_id "
            + " FROM   patient p "
            + "  INNER JOIN encounter e "
            + "    ON e.patient_id = p.patient_id "
            + "  INNER JOIN obs o "
            + "    ON o.encounter_id = e.encounter_id "
            + "  INNER JOIN ( SELECT p.patient_id, e.encounter_datetime FROM patient p     "
            + "             INNER JOIN encounter e ON p.patient_id  = e.patient_id     "
            + "             INNER JOIN obs o ON e.encounter_id = o.encounter_id     "
            + "             WHERE e.encounter_type =  ${6}   "
            + "             AND o.concept_id = ${1719} AND o.value_coded = ${23954}     "
            + "             AND e.location_id = :location     "
            + "             AND e.encounter_datetime < :endDate    "
            + "             AND p.voided = 0 AND e.voided = 0 AND o.voided = 0    "
            + "             AND p.patient_id NOT IN (    "
            + "                SELECT p.patient_id FROM patient p     "
            + "                INNER JOIN encounter e ON p.patient_id  = e.patient_id     "
            + "                INNER JOIN obs o ON e.encounter_id = o.encounter_id     "
            + "                INNER JOIN (    "
            + "                   SELECT p.patient_id, e.encounter_datetime FROM patient p     "
            + "                   INNER JOIN encounter e ON p.patient_id  = e.patient_id     "
            + "                   INNER JOIN obs o ON e.encounter_id = o.encounter_id     "
            + "                   WHERE e.encounter_type =  ${6}   "
            + "                   AND o.concept_id = ${1719} AND o.value_coded = ${23954}     "
            + "                   AND e.location_id = :location     "
            + "                   AND e.encounter_datetime < :endDate    "
            + "                   AND p.voided = 0 AND e.voided = 0 AND o.voided = 0    "
            + "                ) AS fichaClinica     "
            + "                ON fichaClinica.patient_id = p.patient_id    "
            + "                WHERE e.encounter_type =  ${6}   "
            + "                AND o.concept_id = ${1719} AND o.value_coded = ${23954}     "
            + "                AND e.location_id = :location     "
            + "                AND e.encounter_datetime BETWEEN DATE_SUB(fichaClinica.encounter_datetime, INTERVAL 4 MONTH) AND fichaClinica.encounter_datetime    "
            + "                AND p.voided = 0 AND e.voided = 0 AND o.voided = 0    "
            + "             )    "
            + "             AND p.patient_id NOT IN (    "
            + "                SELECT p.patient_id FROM patient p     "
            + "                INNER JOIN encounter e ON p.patient_id  = e.patient_id     "
            + "                INNER JOIN obs o ON e.encounter_id = o.encounter_id     "
            + "                INNER JOIN (    "
            + "                   SELECT p.patient_id, e.encounter_datetime FROM patient p     "
            + "                   INNER JOIN encounter e ON p.patient_id  = e.patient_id     "
            + "                   INNER JOIN obs o ON e.encounter_id = o.encounter_id     "
            + "                   WHERE e.encounter_type =  ${6}   "
            + "                   AND o.concept_id = ${1719} AND o.value_coded = ${23954}     "
            + "                   AND e.location_id = :location     "
            + "                   AND e.encounter_datetime < :endDate    "
            + "                   AND p.voided = 0 AND e.voided = 0 AND o.voided = 0    "
            + "                ) AS regimeTPT     "
            + "                ON regimeTPT.patient_id = p.patient_id    "
            + "                WHERE e.encounter_type = ${60}     "
            + "                AND o.concept_id = ${23985} AND o.value_coded IN(${23954},${23984})    "
            + "                AND e.location_id = :location    "
            + "                AND e.encounter_datetime BETWEEN DATE_SUB(regimeTPT.encounter_datetime, INTERVAL 4 MONTH) AND regimeTPT.encounter_datetime    "
            + "                AND p.voided = 0 AND e.voided = 0 AND o.voided = 0    "
            + "             )) AS tabela "
            + "    ON tabela.patient_id = p.patient_id "
            + " WHERE  p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND e.location_id = :location "
            + "  AND e.encounter_type = ${6} "
            + "  AND ( (SELECT count(*) "
            + "  FROM   patient pp "
            + "       JOIN encounter ee "
            + "      ON pp.patient_id = ee.patient_id "
            + "       JOIN obs oo "
            + "      ON oo.encounter_id = ee.encounter_id "
            + "     WHERE  pp.voided = 0 "
            + "      AND ee.voided = 0 "
            + "      AND oo.voided = 0 "
            + "      AND p.patient_id = pp.patient_id "
            + "      AND ee.encounter_type = ${6} "
            + "      AND ee.location_id = :location "
            + "      AND ee.voided = 0 "
            + "      AND ( "
            + " (oo.concept_id = ${1719} AND oo.value_coded = ${23954} ) "
            + " OR (oo.concept_id IN (${23985}, ${165308}) AND oo.value_coded IN (${23954},${1256},${1257} )) )"
            + "      AND ee.encounter_datetime BETWEEN "
            + "       tabela.encounter_datetime AND "
            + "    DATE_ADD(tabela.encounter_datetime, "
            + "    INTERVAL 120 DAY)) >= 3 ) "
            + " GROUP  BY p.patient_id ";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>IMER1</b>:User Story TPT Completion Patient List <br>
   *
   * <ul>
   *   <li>D1: The date from C is registered on Ficha Clinica - Master Card (encounter type 6) or
   *       Ficha Resumo (encounter type 53) and:
   *   <li>At least 1 consultation registered on Ficha Clínica (encounter type 6) with DT-3HP
   *       (concept ID 1719, value_coded =165307) during 120 days from the date from C1;
   *   <li>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition get3HPD3B(
      int adultoSeguimentoEncounterType,
      int threeHPConcept,
      int treatmentPrescribedConcept,
      int filtEncounterType,
      int regimedeTPConcept,
      int threeHPPiridoxinaConcept,
      int DT3HPConcept) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with Outras prescricoes D1");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", adultoSeguimentoEncounterType);
    map.put("23954", threeHPConcept);
    map.put("1719", treatmentPrescribedConcept);
    map.put("60", filtEncounterType);
    map.put("23985", regimedeTPConcept);
    map.put("23984", threeHPPiridoxinaConcept);
    map.put("165307", DT3HPConcept);

    String query =
        " SELECT p.patient_id "
            + " FROM   patient p "
            + "  INNER JOIN encounter e "
            + "    ON e.patient_id = p.patient_id "
            + "  INNER JOIN obs o "
            + "    ON o.encounter_id = e.encounter_id "
            + "  INNER JOIN ( SELECT p.patient_id, e.encounter_datetime FROM patient p     "
            + "             INNER JOIN encounter e ON p.patient_id  = e.patient_id     "
            + "             INNER JOIN obs o ON e.encounter_id = o.encounter_id     "
            + "             WHERE e.encounter_type =  ${6}   "
            + "             AND o.concept_id = ${1719} AND o.value_coded = ${23954}     "
            + "             AND e.location_id = :location     "
            + "             AND e.encounter_datetime < :endDate    "
            + "             AND p.voided = 0 AND e.voided = 0 AND o.voided = 0    "
            + "             AND p.patient_id NOT IN (    "
            + "                SELECT p.patient_id FROM patient p     "
            + "                INNER JOIN encounter e ON p.patient_id  = e.patient_id     "
            + "                INNER JOIN obs o ON e.encounter_id = o.encounter_id     "
            + "                INNER JOIN (    "
            + "                   SELECT p.patient_id, e.encounter_datetime FROM patient p     "
            + "                   INNER JOIN encounter e ON p.patient_id  = e.patient_id     "
            + "                   INNER JOIN obs o ON e.encounter_id = o.encounter_id     "
            + "                   WHERE e.encounter_type =  ${6}   "
            + "                   AND o.concept_id = ${1719} AND o.value_coded = ${23954}     "
            + "                   AND e.location_id = :location     "
            + "                   AND e.encounter_datetime < :endDate    "
            + "                   AND p.voided = 0 AND e.voided = 0 AND o.voided = 0    "
            + "                ) AS fichaClinica     "
            + "                ON fichaClinica.patient_id = p.patient_id    "
            + "                WHERE e.encounter_type =  ${6}   "
            + "                AND o.concept_id = ${1719} AND o.value_coded = ${23954}     "
            + "                AND e.location_id = :location     "
            + "                AND e.encounter_datetime BETWEEN DATE_SUB(fichaClinica.encounter_datetime, INTERVAL 4 MONTH) AND fichaClinica.encounter_datetime    "
            + "                AND p.voided = 0 AND e.voided = 0 AND o.voided = 0    "
            + "             )    "
            + "             AND p.patient_id NOT IN (    "
            + "                SELECT p.patient_id FROM patient p     "
            + "                INNER JOIN encounter e ON p.patient_id  = e.patient_id     "
            + "                INNER JOIN obs o ON e.encounter_id = o.encounter_id     "
            + "                INNER JOIN (    "
            + "                   SELECT p.patient_id, e.encounter_datetime FROM patient p     "
            + "                   INNER JOIN encounter e ON p.patient_id  = e.patient_id     "
            + "                   INNER JOIN obs o ON e.encounter_id = o.encounter_id     "
            + "                   WHERE e.encounter_type =  ${6}   "
            + "                   AND o.concept_id = ${1719} AND o.value_coded = ${23954}     "
            + "                   AND e.location_id = :location     "
            + "                   AND e.encounter_datetime < :endDate    "
            + "                   AND p.voided = 0 AND e.voided = 0 AND o.voided = 0    "
            + "                ) AS regimeTPT     "
            + "                ON regimeTPT.patient_id = p.patient_id    "
            + "                WHERE e.encounter_type = ${60}     "
            + "                AND o.concept_id = ${23985} AND o.value_coded IN(${23954},${23984})    "
            + "                AND e.location_id = :location    "
            + "                AND e.encounter_datetime BETWEEN DATE_SUB(regimeTPT.encounter_datetime, INTERVAL 4 MONTH) AND regimeTPT.encounter_datetime    "
            + "                AND p.voided = 0 AND e.voided = 0 AND o.voided = 0    "
            + "             )) AS tabela "
            + "    ON tabela.patient_id = p.patient_id "
            + " WHERE  p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND e.location_id = :location "
            + "  AND e.encounter_type = ${6} "
            + "  AND ( (SELECT count(*) "
            + "  FROM   patient pp "
            + "       JOIN encounter ee "
            + "      ON pp.patient_id = ee.patient_id "
            + "       JOIN obs oo "
            + "      ON oo.encounter_id = ee.encounter_id "
            + "     WHERE  pp.voided = 0 "
            + "      AND ee.voided = 0 "
            + "      AND oo.voided = 0 "
            + "      AND p.patient_id = pp.patient_id "
            + "      AND ee.encounter_type = ${6} "
            + "      AND ee.location_id = :location "
            + "      AND ee.voided = 0 "
            + "      AND oo.concept_id = ${1719} "
            + "      AND oo.value_coded = ${165307} "
            + "      AND ee.encounter_datetime BETWEEN "
            + "       tabela.encounter_datetime AND "
            + "    DATE_ADD(tabela.encounter_datetime, "
            + "    INTERVAL 120 DAY)) >= 1 ) "
            + " GROUP  BY p.patient_id ";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   *
   *
   * <h4>User_Story_ TPT</h4>
   *
   * <ul>
   *   <li>D4: The patient date from M is registered on FILT (encounter type 60, encounter
   *       datetime<= enddate) and: The patient has at least 1 drug pick-up on FILT (encounter type
   *       60) with “Regime de TPT” (concept id 23985) value coded “3HP” doxina” (concept id in
   *       [23954, 23984]) and “Tipo de dispensa” (concept id 23986) with value coded “Trimestral”
   *       (concept id 23720) during 120 days from the date from C2; or The patient has at least 3
   *       drug pick-up on FILT (encounter type 60) with “Regime de TPT” (concept id 23985) value
   *       coded “3HP” doxina” (concept id -in [23954, 23984]) and “Tipo de dispensa” (concept id
   *       23986) with value coded “Mensal” (concept id 1098) during 120 days from the date from C2.
   *   <li>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition get3HPD4() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.setName("3HP Final D4");
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    compositionCohortDefinition.addSearch(
        "get3HPD4A",
        EptsReportUtils.map(
            get3HPD4A(
                tbMetadata.get3HPConcept().getConceptId(),
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.get3HPPiridoxinaConcept().getConceptId(),
                tbMetadata.getTypeDispensationTPTConceptUuid().getConceptId(),
                hivMetadata.getQuarterlyConcept().getConceptId(),
                hivMetadata.getPatientTreatmentFollowUp().getConceptId(),
                hivMetadata.getStartDrugs().getConceptId(),
                hivMetadata.getRestartConcept().getConceptId()),
            mapping));
    compositionCohortDefinition.addSearch(
        "get3HPD4B",
        EptsReportUtils.map(
            get3HPD4B(
                tbMetadata.get3HPConcept().getConceptId(),
                tbMetadata.getRegimeTPTEncounterType().getEncounterTypeId(),
                tbMetadata.getRegimeTPTConcept().getConceptId(),
                tbMetadata.get3HPPiridoxinaConcept().getConceptId(),
                tbMetadata.getTypeDispensationTPTConceptUuid().getConceptId(),
                hivMetadata.getMonthlyConcept().getConceptId()),
            mapping));

    compositionCohortDefinition.setCompositionString("get3HPD4A OR get3HPD4B");

    return compositionCohortDefinition;
  }

  /**
   * <b>IMER1</b>:User Story TPT Completion Patient List <br>
   *
   * <ul>
   *   <li>D2: The patient has at least 1 drug pick-up on FILT (encounter type 60) with “Regime de
   *       TPT” (concept id 23985) value coded “3HP” doxina” (concept id in [23954, 23984]) and
   *       “Tipo de dispensa” (concept id 23986) with value coded “Trimestral” (concept id 23720)
   *       during 120 days from the date from C2; or
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition get3HPD4A(
      int threeHPConcept,
      int regimeTPTEncounterType,
      int regimeTPTConcept,
      int hPPiridoxinaConcept,
      int typeDispensationTPTConceptUuid,
      int quarterlyConcept,
      int seguimentoTPTConcept,
      int inicioConcept,
      int reinicioConcept) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with Regime de TPT D2");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("23954", threeHPConcept);
    map.put("60", regimeTPTEncounterType);
    map.put("23985", regimeTPTConcept);
    map.put("23984", hPPiridoxinaConcept);
    map.put("23986", typeDispensationTPTConceptUuid);
    map.put("23720", quarterlyConcept);
    map.put("23987", seguimentoTPTConcept);
    map.put("1256", inicioConcept);
    map.put("1705", reinicioConcept);

    String query =
        " SELECT p.patient_id   "
            + " FROM   patient p   "
            + "       inner join encounter e  "
            + "               ON e.patient_id = p.patient_id  "
            + "       inner join obs o    "
            + "               ON o.encounter_id = e.encounter_id  "
            + "       inner join (SELECT p.patient_id,    "
            + "                          e.encounter_datetime "
            + "                   FROM   patient p    "
            + "                          inner join encounter e   "
            + "                                  ON e.patient_id = p.patient_id   "
            + "                          inner join obs o "
            + "                                  ON o.encounter_id = e.encounter_id   "
            + "                          INNER JOIN obs o2 ON e.encounter_id = o2.encounter_id   "
            + "                   WHERE  p.voided = 0 "
            + "                          AND e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.location_id = :location  "
            + "                          AND e.encounter_type = ${60} "
            + "                          AND ( o.concept_id = ${23985} AND o.value_coded IN (${23954},${23984}) ) "
            + "                          AND (o2.concept_id = ${23987} AND o2.value_coded IN (${1256},${1705})) "
            + "                          AND e.encounter_datetime <= :endDate) AS tabela "
            + "               ON tabela.patient_id = p.patient_id "
            + " WHERE  p.voided = 0    "
            + "       AND e.voided = 0    "
            + "       AND o.voided = 0    "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_type = ${60}   "
            + "       AND ( (SELECT Count(*)  "
            + "              FROM   patient pp    "
            + "                     join encounter ee "
            + "                       ON pp.patient_id = ee.patient_id    "
            + "              WHERE  pp.voided = 0 "
            + "                     AND ee.voided = 0 "
            + "                     AND p.patient_id = pp.patient_id  "
            + "                     AND ee.encounter_type = ${60}    "
            + "                     AND ee.location_id = :location  "
            + "                     AND ee.voided = 0 "
            + "                     AND ( EXISTS (SELECT o.person_id  "
            + "                                   FROM   obs o    "
            + "                                   WHERE  o.encounter_id = ee.encounter_id "
            + "                                          AND o.concept_id = ${23985} "
            + "                                          AND o.value_coded IN ( ${23954}, ${23984} ))   "
            + "                           AND EXISTS (SELECT o.person_id  "
            + "                                       FROM   obs o    "
            + "                                       WHERE  o.encounter_id = ee.encounter_id "
            + "                                              AND o.concept_id = ${23720} "
            + "                                              AND o.value_coded IN ( ${23720} )) )    "
            + "                     AND ee.encounter_datetime BETWEEN "
            + "                         tabela.encounter_datetime AND "
            + "             Date_add(tabela.encounter_datetime,   "
            + "             INTERVAL 120 DAY)) >= 1 ) "
            + " GROUP  BY p.patient_id ";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>IMER1</b>:User Story TPT Eligible Patient List <br>
   *
   * <ul>
   *   <li>D3: The patient has at least 3 drug pick-up on FILT (encounter type 6) with “Regime de
   *       TPT” (concept id 23985) value coded “3HP” doxina” (concept id in [23954, 23984]) and
   *       “Tipo de dispensa” (concept id 23986) with value coded “Trimestral” (concept id 1098)
   *       during 120 days from the date from C2.
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition get3HPD4B(
      int threeHPConcept,
      int regimeTPTEncounterType,
      int regimeTPTConcept,
      int hPPiridoxinaConcept,
      int typeDispensationTPTConceptUuid,
      int monthlyConcept) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(" all patients with Regime de TPT D3");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "Before Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("23954", threeHPConcept);
    map.put("60", regimeTPTEncounterType);
    map.put("23985", regimeTPTConcept);
    map.put("23984", hPPiridoxinaConcept);
    map.put("23986", typeDispensationTPTConceptUuid);
    map.put("1098", monthlyConcept);

    String query =
        " SELECT p.patient_id   "
            + " FROM   patient p   "
            + "       inner join encounter e  "
            + "               ON e.patient_id = p.patient_id  "
            + "       inner join obs o    "
            + "               ON o.encounter_id = e.encounter_id  "
            + "       inner join (SELECT p.patient_id,    "
            + "                          e.encounter_datetime "
            + "                   FROM   patient p    "
            + "                          inner join encounter e   "
            + "                                  ON e.patient_id = p.patient_id   "
            + "                          inner join obs o "
            + "                                  ON o.encounter_id = e.encounter_id   "
            + "                   WHERE  p.voided = 0 "
            + "                          AND e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.location_id = :location  "
            + "                          AND e.encounter_type = ${60}    "
            + "                          AND o.concept_id = ${23985} "
            + "                          AND o.value_coded IN ( ${23954}, ${23984} )    "
            + "                          AND e.encounter_datetime <= :endDate) AS tabela  "
            + "               ON tabela.patient_id = p.patient_id "
            + " WHERE  p.voided = 0    "
            + "       AND e.voided = 0    "
            + "       AND o.voided = 0    "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_type = ${60}   "
            + "       AND ( (SELECT Count(*)  "
            + "              FROM   patient pp    "
            + "                     join encounter ee "
            + "                       ON pp.patient_id = ee.patient_id    "
            + "              WHERE  pp.voided = 0 "
            + "                     AND ee.voided = 0 "
            + "                     AND p.patient_id = pp.patient_id  "
            + "                     AND ee.encounter_type = ${60}    "
            + "                     AND ee.location_id = :location  "
            + "                     AND ee.voided = 0 "
            + "                     AND ( EXISTS (SELECT o.person_id  "
            + "                                   FROM   obs o    "
            + "                                   WHERE  o.encounter_id = ee.encounter_id "
            + "                                          AND o.concept_id = ${23985} "
            + "                                          AND o.value_coded IN ( ${23954}, ${23984} ))   "
            + "                           AND EXISTS (SELECT o.person_id  "
            + "                                       FROM   obs o    "
            + "                                       WHERE  o.encounter_id = ee.encounter_id "
            + "                                              AND o.concept_id = ${23986} "
            + "                                              AND o.value_coded IN ( ${1098} )) ) "
            + "                     AND ee.encounter_datetime BETWEEN "
            + "                         tabela.encounter_datetime AND "
            + "             Date_add(tabela.encounter_datetime,   "
            + "             INTERVAL 120 DAY)) >= 3 ) "
            + " GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>TBPREV</b>: Denominator <br>
   *
   * <ul>
   *   <li>G - TB_PREV - Denominator 7 months before the reporting period
   *   <li>(Start Date = Selected End Date – 210 days and End Date = Selected End Date)
   *   <li>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getTBPrevDenominator() {
    CompositionCohortDefinition definition = new CompositionCohortDefinition();
    definition.setName("TB-PREV Denominator Query");
    definition.addParameter(new Parameter("onOrAfter", "After Date", Date.class));
    definition.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
    definition.addParameter(new Parameter("location", "Location", Location.class));
    definition.addSearch(
        "started-by-end-previous-reporting-period",
        EptsReportUtils.map(
            genericCohortQueries.getStartedArtBeforeDate(false),
            "onOrBefore=${onOrBefore},location=${location}"));
    definition.addSearch(
        "started-isoniazid",
        EptsReportUtils.map(
            tbPrevCohortQueries.getPatientsThatStartedProfilaxiaIsoniazidaOnPeriod(),
            "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));
    definition.addSearch(
        "initiated-profilaxia",
        EptsReportUtils.map(
            tbPrevCohortQueries.getPatientsThatInitiatedProfilaxia(),
            "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));
    definition.addSearch(
        "transferred-out",
        EptsReportUtils.map(
            tbPrevCohortQueries.getPatientsTransferredOut(),
            "startDate=${onOrAfter},endDate=${onOrBefore},location=${location}"));
    definition.addSearch(
        "completed-isoniazid",
        EptsReportUtils.map(
            tbPrevCohortQueries.getPatientsThatCompletedIsoniazidProphylacticTreatment(),
            "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));
    definition.addSearch(
        "regime-tpt-isoniazid",
        EptsReportUtils.map(
            tbPrevCohortQueries.getPatientsWhoHaveRegimeTPTWithINHMarkedOnFirstPickUpDateOnFILT(),
            "startDate=${onOrAfter},endDate=${onOrBefore},location=${location}"));
    definition.addSearch(
        "outras-prescricoes-3hp",
        EptsReportUtils.map(
            tbPrevCohortQueries.getPatientsWhoHaveOutrasPrescricoesWith3HPMarkedOnFichaClinica(),
            "startDate=${onOrAfter},endDate=${onOrBefore},location=${location}"));
    definition.addSearch(
        "regime-tpt-3hp",
        EptsReportUtils.map(
            tbPrevCohortQueries.getPatientsWhoHaveRegimeTPTWith3HPMarkedOnFirstPickUpDateOnFILT(),
            "startDate=${onOrAfter},endDate=${onOrBefore},location=${location}"));
    definition.setCompositionString(
        "started-by-end-previous-reporting-period "
            + " AND ("
            + "            (started-isoniazid OR initiated-profilaxia OR regime-tpt-isoniazid) "
            + "         OR (outras-prescricoes-3hp OR regime-tpt-3hp) "
            + "    AND NOT (transferred-out AND NOT completed-isoniazid)"
            + "     ) ");

    return definition;
  }
}
