package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.calculation.txml.StartedArtOnLastClinicalContactCalculation;
import org.openmrs.module.eptsreports.reporting.cohort.definition.CalculationCohortDefinition;
import org.openmrs.module.eptsreports.reporting.library.queries.CommonQueries;
import org.openmrs.module.eptsreports.reporting.library.queries.TXCurrQueries;
import org.openmrs.module.eptsreports.reporting.library.queries.TxMlQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.definition.library.DocumentedDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** All queries needed for TxMl report needed for EPTS project */
@Component
public class TxMlCohortQueries {

  private HivMetadata hivMetadata;

  private GenericCohortQueries genericCohortQueries;

  private TxCurrCohortQueries txCurrCohortQueries;

  private HivCohortQueries hivCohortQueries;

  private TxRttCohortQueries txRttCohortQueries;

  private CommonCohortQueries commonCohortQueries;

  private CommonMetadata commonMetadata;

  @Autowired
  public TxMlCohortQueries(
      HivMetadata hivMetadata,
      GenericCohortQueries genericCohortQueries,
      TxCurrCohortQueries txCurrCohortQueries,
      HivCohortQueries hivCohortQueries,
      TxRttCohortQueries txRttCohortQueries,
      CommonCohortQueries commonCohortQueries,
      CommonMetadata commonMetadata) {
    this.hivMetadata = hivMetadata;
    this.genericCohortQueries = genericCohortQueries;
    this.txCurrCohortQueries = txCurrCohortQueries;
    this.hivCohortQueries = hivCohortQueries;
    this.txRttCohortQueries = txRttCohortQueries;
    this.commonCohortQueries = commonCohortQueries;
    this.commonMetadata = commonMetadata;
  }

  /**
   * <b>Indicator numerator </b>
   *
   * <p>From all patients who ever initiated ART (TX_ML_FR4) by end of reporting period:
   *
   * <blockquote>
   *
   * <p>All patients whose earliest ART start date from pick-up and clinical sources (CURR_FR4.1)
   * falls before (<) 21 December 2023 and this date falls by the end of the reporting period. OR
   *
   * <p>All patients whose earliest ART start date (CURR_FR4.1) falls on or after (>=) 21 December
   * 2023 AND whose first ever drug pick-up date between the following sources falls by the end of
   * the reporting period:
   *
   * </blockquote>
   *
   * <li>Include patients marked as “died” (using all the criteria defined in TX_ML_FR5) by end of
   *     reporting period;
   * <li>Include patients marked as “suspended” (using all the criteria defined in TX_ML_FR46) by
   *     end of reporting period
   * <li>Include patients marked as “transferred-out” (using all the criteria defined in TX_ML_FR7)
   *     by end of reporting period who have the last scheduled pick-up marked on FILA + 1 day
   *     falling by end of reporting period.
   * <li>Include patients with Interruption In Treatment (IIT) during the reporting period (using
   *     all the criteria defined in TX_ML_FR8)<br>
   *     <b>The system will exclude the following patients:</b>
   * <li>All patients who were transferred-out (using all defined criteria on TX_ML_FR7) by end of
   *     previous reporting period
   * <li>All patient who are dead (TX_ML_FR5) by end of previous reporting period
   * <li>All patients who stopped/suspended treatment (TX_ML_FR6) by end of previous reporting
   *     period
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition
      getPatientsWhoMissedNextAppointmentAndNoScheduledDrugPickupOrNextConsultation() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Get patients who missed appointment and are NOT transferred out");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition startedArtBeforeDecember2023 =
        txCurrCohortQueries.getPatientsWhoEverInitiatedTreatmentBeforeDecember2023();
    CohortDefinition startedArtAfterDecember2023 =
        txCurrCohortQueries
            .getPatientsWhoStartedArtAfterDecember2023AndHasDrugPickupByReportEndDate();
    CohortDefinition suspendedTreatment =
        txCurrCohortQueries.getPatientsWhoStoppedOrSuspendedTreatment();

    CohortDefinition interruptedTreatment =
        getPatientsWhoExperiencedInterruptionInTreatmentComposition();

    CohortDefinition transferredOut = getPatientsWhoHasTransferredOutComposition();
    CohortDefinition transferredOutPrevious = getPatientsWhoHasTransferredOutByEndOfPeriod();

    String mappings = "onOrBefore=${endDate},location=${location}";
    String mappings2 = "startDate=${startDate},endDate=${endDate},location=${location}";
    String previousPeriodMappings = "endDate=${startDate-1d},location=${location}";

    CohortDefinition dead = txCurrCohortQueries.getPatientsWhoAreDead();

    cd.addSearch(
        "transferredOutPreviousPeriod",
        EptsReportUtils.map(transferredOutPrevious, previousPeriodMappings));

    cd.addSearch("transferredOutReportingPeriod", EptsReportUtils.map(transferredOut, mappings2));

    cd.addSearch(
        "deadPreviousPeriod",
        EptsReportUtils.map(dead, "onOrBefore=${startDate-1d},location=${location}"));

    cd.addSearch("deadReportingPeriod", EptsReportUtils.map(dead, mappings));

    cd.addSearch("suspendedReportingPeriod", EptsReportUtils.map(suspendedTreatment, mappings));
    cd.addSearch(
        "suspendedPreviousPeriod",
        EptsReportUtils.map(suspendedTreatment, "onOrBefore=${startDate-1d},location=${location}"));

    cd.addSearch(
        "startedArtAfterDecember2023",
        EptsReportUtils.map(
            startedArtAfterDecember2023, "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "startedArtBeforeDecember2023",
        EptsReportUtils.map(
            startedArtBeforeDecember2023, "endDate=${endDate},location=${location}"));

    cd.addSearch("iit", EptsReportUtils.map(interruptedTreatment, mappings2));

    cd.setCompositionString(
        "( (startedArtBeforeDecember2023 OR startedArtAfterDecember2023) AND"
            + " (deadReportingPeriod OR suspendedReportingPeriod OR transferredOutReportingPeriod OR iit) ) "
            + "AND NOT (transferredOutPreviousPeriod OR deadPreviousPeriod OR suspendedPreviousPeriod)");

    return cd;
  }

  /**
   * <b>Patients experienced Interruption in Treatment (IIT)</b>
   * <li>All patients with the most recent date between next scheduled drug pickup date (FILA) and
   *     30 days after last ART pickup date (Ficha Recepção – Levantou ARVs) and adding 28 days and
   *     this date >=report start date and < reporting end date
   * <li>All patients who do not have the next scheduled drug pick up date on their last drug
   *     pick-up (FILA) that occurred during the reporting period nor any ART pickup date registered
   *     on Ficha Recepção – Levantou ARVs or FILA during the reporting period
   * <li>The system will exclude: All patients who are dead (TX_ML_FR5) or transferred out
   *     (TX_ML_FR7).
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoExperiencedInterruptionInTreatmentComposition() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "iitWithoutNextScheduledDrugPickup",
        EptsReportUtils.map(
            getPatientWithoutScheduledDrugPickup(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "iitMostRecentScheduleAfter28Days",
        EptsReportUtils.map(
            getPatientHavingLastScheduledDrugPickupDate(28),
            "startDate=${startDate-1d},endDate=${endDate},location=${location}"));

    // EXCLUSIONS
    cd.addSearch(
        "deadReportingPeriod",
        EptsReportUtils.map(
            txCurrCohortQueries.getPatientsWhoAreDead(),
            "onOrBefore=${endDate},location=${location}"));

    cd.addSearch(
        "transferredOut",
        EptsReportUtils.map(
            getPatientsWhoHasTransferredOutComposition(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "transferredOutPrevious",
        EptsReportUtils.map(
            getPatientsWhoHasTransferredOutByEndOfPeriod(),
            "endDate=${startDate-1d},location=${location}"));

    cd.setCompositionString(
        "(iitMostRecentScheduleAfter28Days OR iitWithoutNextScheduledDrugPickup) AND NOT (deadReportingPeriod OR (transferredOut NOT transferredOutPrevious))");
    return cd;
  }

  /**
   * <b>Patients who are Transferred Out to another HF</b>
   * <li>Patients enrolled on ART Program (Service TARV- Tratamento) with the following last state:
   *     “Transferred Out” or
   * <li>Patients whose most recently informed “Mudança no Estado de Permanência TARV” is
   *     Transferred Out on Ficha Clinica ou Ficha Resumo – Master Card.
   * <li>Patients who have REASON PATIENT MISSED VISIT (MOTIVOS DA FALTA) as “Transferido para outra
   *     US” or “Auto-transferência” marked in the last Home Visit Card by reporting end date. Use
   *     the “data da visita” when the patient reason was marked on the Home Visit Card as the
   *     reference date
   * <li>The system will consider patient as transferred out as above defined only if the most
   *     recent date between (next scheduled ART pick-up on FILA + 1 day) and (the most recent ART
   *     pickup date on Ficha Recepção – Levantou ARVs + 31 days) falls by the end of the reporting
   *     period <b>Note:</b>
   *
   *     <p>Patients who are “marked” as transferred out who have an ARV pick-up registered in FILA
   *     after the date the patient was “marked” as transferred out will be evaluated for IIT
   *     definition (CURR_FR5).
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoHasTransferredOutComposition() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Patients who are Transferred Out to another HF");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "transferredOutReportingPeriod",
        EptsReportUtils.map(
            txCurrCohortQueries.getPatientsWhoAreTransferredOutToAnotherHf(),
            "onOrBefore=${endDate},location=${location}"));

    cd.addSearch(
        "mostRecentScheduleDuringPeriod",
        EptsReportUtils.map(
            getTransferredOutBetweenNextPickupDateFilaAndRecepcaoLevantou(true),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("transferredOutReportingPeriod AND mostRecentScheduleDuringPeriod");
    return cd;
  }

  public CohortDefinition getPatientsWhoHasTransferredOutByEndOfPeriod() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Patients who are Transferred Out to another HF");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "transferredOutReportingPeriod",
        EptsReportUtils.map(
            txCurrCohortQueries.getPatientsWhoAreTransferredOutToAnotherHf(),
            "onOrBefore=${endDate},location=${location}"));

    cd.addSearch(
        "mostRecentScheduleDuringPeriod",
        EptsReportUtils.map(
            txCurrCohortQueries.getTransferredOutBetweenNextPickupDateFilaAndRecepcaoLevantou(),
            "onOrBefore=${endDate},location=${location}"));

    cd.setCompositionString("transferredOutReportingPeriod AND mostRecentScheduleDuringPeriod");
    return cd;
  }

  public CohortDefinition getPatientWithoutScheduledDrugPickup() {
    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("patientWithoutScheduledDrugPickupDateMasterCardAmdArtPickup");

    definition.addParameter(new Parameter("startDate", "startDate", Date.class));
    definition.addParameter(new Parameter("endDate", "endDate", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));

    definition.setQuery(
        TxMlQueries.getPatientWithoutScheduledDrugPickupDateMasterCardAndArtPickupQuery(
            hivMetadata.getARVPharmaciaEncounterType(),
            hivMetadata.getMasterCardDrugPickupEncounterType(),
            hivMetadata.getReturnVisitDateForArvDrugConcept(),
            hivMetadata.getArtDatePickupMasterCard()));

    return definition;
  }

  /**
   * <b>Patients experienced Interruption in Treatment (IIT)</b>
   * <li>All patients with the most recent date between next scheduled drug pickup date (FILA) and
   *     30 days after last ART pickup date (Ficha Recepção – Levantou ARVs) and adding 28 days and
   *     this date >=report start date and < reporting end date
   *
   * @param numDays number of days to add
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientHavingLastScheduledDrugPickupDate(int numDays) {
    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("patientWithoutScheduledDrugPickupDateMasterCardAmdArtPickup");

    definition.addParameter(new Parameter("startDate", "startDate", Date.class));
    definition.addParameter(new Parameter("endDate", "endDate", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));

    definition.setQuery(
        TxMlQueries.getPatientHavingLastScheduledDrugPickupDateQuery(
            hivMetadata.getReturnVisitDateForArvDrugConcept(),
            hivMetadata.getARVPharmaciaEncounterType(),
            commonMetadata.getReturnVisitDateConcept(),
            hivMetadata.getArtDatePickupMasterCard(),
            hivMetadata.getMasterCardDrugPickupEncounterType(),
            numDays));

    return definition;
  }

  /**
   * <b>Description:</b> Patients Started Art and missed Next Appointment or Next Drug Pickup (a and
   * b) and Died during reporting period
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoMissedNextAppointmentAndDiedDuringReportingPeriod() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "Get patients who missed appointment and are NOT transferred out, but died during reporting period");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addSearch(
        "missedAppointment",
        EptsReportUtils.map(
            getPatientsWhoMissedNextAppointmentAndNoScheduledDrugPickupOrNextConsultation(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "dead",
        EptsReportUtils.map(
            txCurrCohortQueries.getPatientsWhoAreDead(),
            "onOrBefore=${endDate},location=${location}"));

    cd.setCompositionString("missedAppointment AND dead");

    return cd;
  }

  /**
   * <b>Description:</b> (from A and B) Refused/Stopped treatment
   *
   * <p>Except patients identified in Dead or Transferred–out Disaggregation
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoMissedNextAppointmentAndRefusedOrStoppedTreatment() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "Get patients who missed appointment and are NOT dead and NOT transferred out during the reporting period");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    String mappings = "onOrBefore=${endDate},location=${location}";

    CohortDefinition suspendedTreatment =
        txCurrCohortQueries.getPatientsWhoStoppedOrSuspendedTreatment();

    cd.addSearch(
        "numerator",
        EptsReportUtils.map(
            getPatientsWhoMissedNextAppointmentAndNoScheduledDrugPickupOrNextConsultation(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch("suspendedReportingPeriod", EptsReportUtils.map(suspendedTreatment, mappings));

    cd.setCompositionString("numerator AND suspendedReportingPeriod");

    return cd;
  }

  /**
   * <b>Description:</b> A, B AND Transferred Out Except all patients who after the most recent date
   * from above criterias, have a drugs pick up or consultation: Except patients identified in Dead
   * Disaggregation
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoMissedNextAppointmentAndTransferredOut() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Get patients who are transferred out, but died during reporting period");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "transferredOut",
        EptsReportUtils.map(
            getPatientsWhoHasTransferredOutComposition(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "dead",
        EptsReportUtils.map(
            getPatientsWhoMissedNextAppointmentAndDiedDuringReportingPeriod(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "numerator",
        EptsReportUtils.map(
            getPatientsWhoMissedNextAppointmentAndNoScheduledDrugPickupOrNextConsultation(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("numerator AND (transferredOut AND NOT dead)");
    return cd;
  }

  /**
   * <b>Description:</b> A and B and Traced (Unable to locate)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoMissedNextAppointmentAndNotTransferredOutAndTraced() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName(
        "Get patients who missed next appointment, not transferred out and traced (Unable to locate)");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "patientsNotFound",
        EptsReportUtils.map(
            getPatientsTracedAndNotFound(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "patientsFound",
        EptsReportUtils.map(
            getPatientTracedAndFound(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("patientsNotFound AND NOT patientsFound");

    return cd;
  }

  /**
   * <b>Description: </b> A and B and Untraced Patients
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoMissedNextAppointmentAndNotTransferredOutAndUntraced() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Get patients who missed next appointment, not transferred out and untraced");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "withoutVisitCard",
        EptsReportUtils.map(
            getPatientsWithoutVisitCardRegisteredBtwnLastAppointmentOrDrugPickupAndEnddate(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "withVisitCardandWithObs",
        EptsReportUtils.map(
            getPatientsWithVisitCardAndWithObs(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.setCompositionString("withoutVisitCard OR NOT withVisitCardandWithObs");

    return cd;
  }

  /**
   * <b>Description:</b> “Lost to Follow-Up After being on Treatment for <3 months” will have the
   * following combination: ((A OR B) AND C1) AND NOT DEAD AND NOT TRANSFERRED OUT AND NOT REFUSED
   * Lost to Follow-Up After being on Treatment for <3 months
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsLTFULessThan90DaysComposition() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Get patients who are Lost To Follow Up Composition");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "missedAppointment",
        EptsReportUtils.map(
            getPatientsWhoMissedNextAppointmentAndNoScheduledDrugPickupOrNextConsultation(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "untracedPatients",
        EptsReportUtils.map(
            getPatientsWhoMissedNextAppointmentAndNotTransferredOutAndUntraced(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "tracedPatients",
        EptsReportUtils.map(
            getPatientsWhoMissedNextAppointmentAndNotTransferredOutAndTraced(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "C1",
        EptsReportUtils.map(
            getPatientsOnARTForLessOrMoreThan180Days(1),
            "onOrBefore=${endDate},location=${location}"));
    cd.addSearch(
        "dead",
        EptsReportUtils.map(
            getDeadPatientsComposition(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "transferredOut",
        EptsReportUtils.map(
            getPatientsWhoMissedNextAppointmentAndTransferredOut(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "refusedOrStopped",
        EptsReportUtils.map(
            getPatientsWhoMissedNextAppointmentAndRefusedOrStoppedTreatment(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString(
        "(missedAppointment AND ((untracedPatients OR tracedPatients) AND C1)) AND NOT dead AND NOT transferredOut AND NOT refusedOrStopped");

    return cd;
  }

  /**
   * <b>Description:</b> “Lost to Follow-Up After being on Treatment for >6 months” will have the
   * following combination:
   *
   * <ul>
   *   <li>((A OR B) AND C2) AND NOT Dead AND NOT Transferred-Out AND NOT Refused
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsLTFUMoreThan180DaysComposition() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Get patients who are Lost To Follow Up Composition");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "missedAppointment",
        EptsReportUtils.map(
            getPatientsWhoMissedNextAppointmentAndNoScheduledDrugPickupOrNextConsultation(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "untracedPatients",
        EptsReportUtils.map(
            getPatientsWhoMissedNextAppointmentAndNotTransferredOutAndUntraced(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "tracedPatients",
        EptsReportUtils.map(
            getPatientsWhoMissedNextAppointmentAndNotTransferredOutAndTraced(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "C2",
        EptsReportUtils.map(
            getPatientsOnARTForLessOrMoreThan180Days(3),
            "onOrBefore=${endDate},location=${location}"));
    cd.addSearch(
        "dead",
        EptsReportUtils.map(
            getPatientsWhoMissedNextAppointmentAndDiedDuringReportingPeriod(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "transferredOut",
        EptsReportUtils.map(
            getPatientsWhoMissedNextAppointmentAndTransferredOut(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "refusedOrStopped",
        EptsReportUtils.map(
            getPatientsWhoMissedNextAppointmentAndRefusedOrStoppedTreatment(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString(
        "missedAppointment AND ((untracedPatients OR tracedPatients) AND C2) AND NOT dead AND NOT transferredOut AND NOT refusedOrStopped");
    return cd;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <b>10 –</b> All transferred-outs <b>(Patient_State.state = 7)</b> in ART Program <b>(program_id
   * =2)</b>
   *
   * <p>with Estado de Permanencia <b>(concept_id = 6272)</b> = Transferred-out <b>(concept_id =
   * 1706)</b>
   *
   * <p>with Busca activa <b>(encounterType_id = 21)</b> = Transferred-out <b>(concept_id =
   * 1706)</b> and Auto Transfer <b>(concept_id = 23863)</b>
   *
   * <p>And had no registered in Drug pickup <b>(encounterType_id = 53)</b> after The
   * Transferred-out Date within reporting period
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getTransferredOutPatientsComposition() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName(
        "Patient Transferred Out With No Drug Pick After The Transferred out Date ");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("reportEndDate", "Report End Date", Date.class));
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
        "buscaActivaEncounterType", hivMetadata.getBuscaActivaEncounterType().getEncounterTypeId());
    map.put("artProgram", hivMetadata.getARTProgram().getProgramId());
    map.put(
        "transferredOutToAnotherHealthFacilityWorkflowState",
        hivMetadata
            .getTransferredOutToAnotherHealthFacilityWorkflowState()
            .getProgramWorkflowStateId());

    String query =
        "  SELECT mostrecent.patient_id "
            + "FROM ("
            + " SELECT lastest.patient_id ,Max(lastest.last_date) as  last_date "
            + " FROM (  "
            + "    SELECT p.patient_id , Max(ps.start_date) AS last_date  "
            + "    FROM patient p   "
            + "        INNER JOIN patient_program pg   "
            + "            ON p.patient_id=pg.patient_id   "
            + "        INNER JOIN patient_state ps   "
            + "            ON pg.patient_program_id=ps.patient_program_id   "
            + "    WHERE pg.voided=0   "
            + "        AND ps.voided=0   "
            + "        AND p.voided=0   "
            + "        AND pg.program_id= ${artProgram}  "
            + "        AND ps.state = ${transferredOutToAnotherHealthFacilityWorkflowState} "
            + "        AND ps.start_date <= :endDate    "
            + "        AND pg.location_id= :location   "
            + "    group by p.patient_id  "
            + "  "
            + "    UNION  "
            + "  "
            + "    SELECT  p.patient_id,  Max(o.obs_datetime) AS last_date  "
            + "    FROM patient p    "
            + "        INNER JOIN encounter e   "
            + "            ON e.patient_id=p.patient_id   "
            + "        INNER JOIN obs o   "
            + "            ON o.encounter_id=e.encounter_id   "
            + "    WHERE  p.voided = 0   "
            + "        AND e.voided = 0   "
            + "        AND o.voided = 0   "
            + "        AND e.encounter_type = ${masterCardEncounterType}   "
            + "        AND o.concept_id = ${stateOfStayOfPreArtPatient}  "
            + "        AND o.value_coded =  ${transferredOutConcept}   "
            + "        AND o.obs_datetime <= :endDate   "
            + "        AND e.location_id =  :location   "
            + "    GROUP BY p.patient_id  "
            + "    UNION   "
            + "    SELECT  p.patient_id , Max(e.encounter_datetime) AS last_date  "
            + "    FROM patient p    "
            + "        INNER JOIN encounter e   "
            + "            ON e.patient_id=p.patient_id   "
            + "        INNER JOIN obs o   "
            + "            ON o.encounter_id=e.encounter_id   "
            + "    WHERE  p.voided = 0   "
            + "        AND e.voided = 0   "
            + "        AND o.voided = 0   "
            + "        AND e.encounter_type = ${adultoSeguimentoEncounterType}  "
            + "        AND o.concept_id = ${stateOfStayOfArtPatient}  "
            + "        AND o.value_coded = ${transferredOutConcept}   "
            + "        AND e.encounter_datetime <= :endDate   "
            + "        AND e.location_id =  :location  "
            + "    GROUP BY p.patient_id   "
            + "  "
            + "    UNION  "
            + "  "
            + "    SELECT p.patient_id, Max(e.encounter_datetime) last_date   "
            + "    FROM patient p   "
            + "        INNER JOIN encounter e   "
            + "              ON p.patient_id = e.patient_id   "
            + "        INNER JOIN obs o   "
            + "              ON e.encounter_id = o.encounter_id   "
            + "    WHERE o.concept_id = ${defaultingMotiveConcept}  "
            + "    	   AND e.location_id = :location   "
            + "        AND e.encounter_type= ${buscaActivaEncounterType}   "
            + "        AND e.encounter_datetime <= :endDate "
            + "		   AND o.value_coded IN (${transferredOutConcept} ,${autoTransferConcept})  "
            + "        AND e.voided=0   "
            + "        AND o.voided=0   "
            + "        AND p.voided=0   "
            + "    GROUP BY p.patient_id "
            + ") lastest   "
            + "WHERE lastest.patient_id NOT  IN("
            + " "
            + "  			     SELECT  p.patient_id    "
            + "	                 FROM patient p      "
            + "	                     INNER JOIN encounter e     "
            + "	                         ON e.patient_id=p.patient_id     "
            + "	                 WHERE  p.voided = 0     "
            + "	                     AND e.voided = 0     "
            + "	                     AND e.encounter_type IN (${adultoSeguimentoEncounterType}, "
            + " ${pediatriaSeguimentoEncounterType}, "
            + " ${pharmaciaEncounterType})    "
            + "	                     AND e.encounter_datetime > lastest.last_date "
            + " AND e.encounter_datetime <=  :reportEndDate    "
            + "	                     AND e.location_id =  :location    "
            + "	                 GROUP BY p.patient_id "
            + ")  "
            + " GROUP BY lastest.patient_id"
            + " )mostrecent "
            + " GROUP BY mostrecent.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    String mappedQuery = stringSubstitutor.replace(query);

    sqlCohortDefinition.setQuery(mappedQuery);

    return sqlCohortDefinition;
  }

  /**
   * <b>Description:</b> Get all patients who after most recent Date have drug pickup or
   * consultation
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientWhoAfterMostRecentDateHaveDrugPickupOrConsultation() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Get all patients who after most recent Date have drug pickup or consultation");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Date.class));

    cd.addSearch(
        "patientsWithDrugsPickupOrConsultation",
        EptsReportUtils.map(
            txCurrCohortQueries
                .getPatientWhoAfterMostRecentDateHaveDrugPickupOrConsultationComposition(),
            "onOrBefore=${endDate},location=${location}"));

    cd.setCompositionString("patientsWithDrugsPickupOrConsultation");

    return cd;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <b>a.</b> All deaths registered in Patient Program State by reporting end date
   *
   * <p><b>
   *
   * <p>b.</b> All deaths registered in Patient Demographics by reporting end date
   *
   * <p><b>
   *
   * <p>c.</b> All deaths registered in Last Home Visit Card by reporting end date
   *
   * <p><b>
   *
   * <p>d.</b> All deaths registered in Ficha Resumo and Ficha Clinica of Master Card by reporting
   * end date
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getDeadPatientsComposition() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Get patients who are dead according to criteria a,b,c,d and e");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("reportEndDate", "Report End Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "deadByPatientProgramState",
        EptsReportUtils.map(
            getPatientsDeadInProgramStateByReportingEndDate(),
            "onOrBefore=${endDate},location=${location}"));
    cd.addSearch(
        "deadByPatientDemographics",
        EptsReportUtils.map(
            txCurrCohortQueries.getDeadPatientsInDemographiscByReportingEndDate(),
            "onOrBefore=${endDate}"));
    cd.addSearch(
        "deadRegisteredInLastHomeVisitCard",
        EptsReportUtils.map(
            txCurrCohortQueries.getPatientDeathRegisteredInLastHomeVisitCardByReportingEndDate(),
            "onOrBefore=${endDate},location=${location}"));
    cd.addSearch(
        "deadRegisteredInFichaResumoAndFichaClinicaMasterCard",
        EptsReportUtils.map(
            txCurrCohortQueries.getDeadPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate(),
            "onOrBefore=${endDate},location=${location}"));
    cd.addSearch(
        "exclusion",
        EptsReportUtils.map(
            getExclusionForDeadOrSuspendedPatients(true),
            "endDate=${endDate},reportEndDate=${endDate},location=${location}"));

    cd.setCompositionString(
        "(deadByPatientDemographics OR deadByPatientProgramState OR deadRegisteredInLastHomeVisitCard"
            + " OR deadRegisteredInFichaResumoAndFichaClinicaMasterCard) AND NOT exclusion");

    return cd;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <b>a.</b> All deaths registered in Patient Program State by reporting end date
   *
   * <p><b>
   *
   * <p>b.</b> All deaths registered in Patient Demographics by reporting end date
   *
   * <p><b>
   *
   * <p>c.</b> All deaths registered in Last Home Visit Card by reporting end date
   *
   * <p><b>
   *
   * <p>d.</b> All deaths registered in Ficha Resumo and Ficha Clinica of Master Card by reporting
   * end date
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getSuspendedPatientsComposition() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Get patients who are dead according to criteria a,b,c,d and e");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("reportEndDate", "Report End Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "suspendedByPatientProgramState",
        EptsReportUtils.map(
            getPatientsSuspendedInProgramStateByReportingEndDate(),
            "onOrBefore=${endDate},location=${location}"));

    cd.addSearch(
        "suspendedRegisteredInFichaResumoAndFichaClinicaMasterCard",
        EptsReportUtils.map(
            getDeadPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate(),
            "onOrBefore=${endDate},location=${location}"));

    cd.addSearch(
        "exclusion",
        EptsReportUtils.map(
            getExclusionForDeadOrSuspendedPatients(false),
            "endDate=${endDate},reportEndDate=${endDate},location=${location}"));

    cd.setCompositionString(
        "(suspendedByPatientProgramState OR suspendedRegisteredInFichaResumoAndFichaClinicaMasterCard) AND NOT exclusion");

    return cd;
  }

  public CohortDefinition getExclusionForDeadOrSuspendedPatients(boolean exclusionForDead) {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("get Exclusion For Dead Or Suspended patients");
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("reportEndDate", " Report end Date", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("21", hivMetadata.getBuscaActivaEncounterType().getEncounterTypeId());
    map.put("36", hivMetadata.getVisitaApoioReintegracaoParteAEncounterType().getEncounterTypeId());
    map.put("37", hivMetadata.getVisitaApoioReintegracaoParteBEncounterType().getEncounterTypeId());
    map.put("2031", hivMetadata.getReasonPatientNotFound().getConceptId());
    map.put(
        "23944", hivMetadata.getReasonPatientNotFoundByActivist2ndVisitConcept().getConceptId());
    map.put(
        "23945", hivMetadata.getReasonPatientNotFoundByActivist3rdVisitConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6272", hivMetadata.getStateOfStayPriorArtPatientConcept().getConceptId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());

    if (exclusionForDead) {
      map.put("programState", hivMetadata.getArtDeadWorkflowState().getProgramWorkflowStateId());
      map.put("state", hivMetadata.getPatientHasDiedConcept().getConceptId());

    } else {
      map.put(
          "programState",
          hivMetadata.getSuspendedTreatmentWorkflowState().getProgramWorkflowStateId());
      map.put("state", hivMetadata.getSuspendedTreatmentConcept().getConceptId());
    }

    String query =
        "SELECT outter.patient_id "
            + "FROM "
            + "    ( "
            + "    SELECT mostrecent.patient_id, MAX(mostrecent.last_date) as l_date "
            + "    FROM "
            + "        ( "
            + "        SELECT p.patient_id, MAX(ps.start_date) AS last_date "
            + "        FROM patient p  "
            + "            INNER JOIN patient_program pg ON p.patient_id=pg.patient_id  "
            + "            INNER JOIN patient_state ps ON pg.patient_program_id=ps.patient_program_id  "
            + "        WHERE pg.voided=0  "
            + "            AND ps.voided=0  "
            + "            AND p.voided=0  "
            + "            AND pg.program_id= ${2}  "
            + "            AND ps.state = ${programState} "
            + "            AND ps.start_date<=:endDate  "
            + "            AND pg.location_id= :location  "
            + "        GROUP BY p.patient_id "
            + "        UNION "
            + "        SELECT  max_date.patient_id, max_date.last_date "
            + "        FROM   "
            + "            ( "
            + "                SELECT  p.patient_id,  MAX(e.encounter_datetime) last_date   "
            + "                FROM patient p  "
            + "                    INNER  JOIN encounter e ON e.patient_id=p.patient_id  "
            + "                WHERE   "
            + "                    e.encounter_datetime <= :endDate  "
            + "                    AND e.location_id =  :location  "
            + "                    AND e.encounter_type  in(${21},${36},${37})   "
            + "                    AND e.voided=0  "
            + "                    AND p.voided = 0  "
            + "                GROUP BY  p.patient_id   "
            + "            ) AS max_date  "
            + "            INNER  JOIN encounter ee ON ee.patient_id = max_date.patient_id  "
            + "            INNER  JOIN obs o ON ee.encounter_id = o.encounter_id   "
            + "        WHERE   "
            + "            (  "
            + "                (o.concept_id = ${2031} AND o.value_coded = ${state}) OR  "
            + "                (o.concept_id = ${23944} AND o.value_coded = ${state}) OR  "
            + "                (o.concept_id = ${23945} AND o.value_coded = ${state})  "
            + "            )   "
            + "            AND o.voided=0  "
            + "            AND ee.voided = 0  "
            + "            GROUP BY  max_date.patient_id "
            + " "
            + "        UNION "
            + " "
            + "        SELECT  p.patient_id, MAX(e.encounter_datetime) last_date "
            + "        FROM patient p   "
            + "            INNER JOIN encounter e   "
            + "                ON e.patient_id=p.patient_id   "
            + "            INNER JOIN obs o   "
            + "                ON o.encounter_id=e.encounter_id   "
            + "        WHERE e.encounter_type = ${6}  "
            + "            AND e.encounter_datetime <= :endDate  "
            + "            AND o.concept_id = ${6273} "
            + "            AND o.value_coded = ${state}  "
            + "            AND e.location_id =  :location   "
            + "            AND p.voided=0    "
            + "            AND e.voided=0   "
            + "            AND o.voided=0   "
            + "        GROUP BY p.patient_id  "
            + "        UNION  "
            + "        SELECT  p.patient_id, MAX(o.obs_datetime) AS last_date "
            + "        FROM patient p   "
            + "            INNER JOIN encounter e   "
            + "                ON e.patient_id=p.patient_id   "
            + "            INNER JOIN obs o   "
            + "                ON o.encounter_id=e.encounter_id   "
            + "        WHERE e.encounter_type = ${53}   "
            + "            AND o.obs_datetime <= :endDate  "
            + "            AND o.concept_id = ${6272}  "
            + "            AND o.value_coded =${state}   "
            + "            AND e.location_id =  :location   "
            + "            AND p.voided=0    "
            + "            AND e.voided=0   "
            + "            AND o.voided=0   "
            + "        GROUP BY p.patient_id  "
            + " "
            + "        UNION "
            + " "
            + "        SELECT p.person_id AS patient_id, MAX(p.death_date) AS last_date   "
            + "        FROM person p  "
            + "        WHERE p.dead=1  "
            + "            AND p.death_date <= :endDate  "
            + "            AND p.voided=0 "
            + "        GROUP BY patient_id "
            + "        ) AS mostrecent "
            + "    GROUP BY mostrecent.patient_id "
            + "    ) AS outter "
            + "      INNER JOIN encounter e ON e.patient_id = outter.patient_id  "
            + "      INNER JOIN obs obss ON obss.encounter_id=e.encounter_id  "
            + "WHERE e.voided=0  "
            + "    AND obss.voided=0  "
            + "    AND   ( "
            + "                e.encounter_type IN (${6},${9},${18})  "
            + "                AND  e.encounter_datetime >  outter.l_date   "
            + "                AND e.encounter_datetime <= :reportEndDate   "
            + "             )    "
            + "    AND e.location_id =   :location  "
            + "GROUP BY outter.patient_id ";

    StringSubstitutor sb = new StringSubstitutor(map);
    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   * * <b>Description: A -</b> “Untraced” patients as following (Part II): * *
   *
   * <p><b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <p>All Patients without “Patient Visit Card” <b>(encounterType_id 21 or 36 or 37)</b>
   * registered between most recent scheduled date (as below) and the reporting end date with the
   * following observations:
   *
   * <ul>
   *   <li>Type of Visit: <b>concept id= 1981 value = concept_id = 2160</b> (Busca) AND
   *   <li>Second Attempt: <b>concept_id = 6254</b> any value OR
   *   <li>Third Attempt: <b>concept_id = 6255</b> any value OR
   *   <li>Patient Found: <b>concept_id = 2003</b> any value OR
   *   <li>Defaulting Motive: <b>concept_id = 2016</b> any value OR
   *   <li>Report Visit: <b>concept_ids =  2158, 2157</b> any value OR
   *   <li>Patient Found Forwarded: <b>concept_id = 1272</b> any value OR
   *   <li>Reason of not finding: <b>concept_id =  2031</b> any value OR
   *   <li>Who gave the information: <b>concept_id = 2037</b> any value OR
   *   <li>Card Delivery Date: <b>concept_id = 2180</b> any value)
   * </ul>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithVisitCardAndWithObs() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName("Get patients without Visit Card but with a set of observations");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    sqlCohortDefinition.setQuery(
        TxMlQueries.getPatientsWithVisitCardAndWithObs(
            hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId(),
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId(),
            hivMetadata.getReturnVisitDateConcept().getConceptId(),
            hivMetadata.getBuscaActivaEncounterType().getEncounterTypeId(),
            hivMetadata.getVisitaApoioReintegracaoParteAEncounterType().getEncounterTypeId(),
            hivMetadata.getVisitaApoioReintegracaoParteBEncounterType().getEncounterTypeId(),
            hivMetadata.getTypeOfVisitConcept().getConceptId(),
            hivMetadata.getBuscaConcept().getConceptId(),
            hivMetadata.getSecondAttemptConcept().getConceptId(),
            hivMetadata.getThirdAttemptConcept().getConceptId(),
            hivMetadata.getPatientFoundConcept().getConceptId(),
            hivMetadata.getDefaultingMotiveConcept().getConceptId(),
            hivMetadata.getReportOfVisitSupportConcept().getConceptId(),
            hivMetadata.getPatientHadDifficultyConcept().getConceptId(),
            hivMetadata.getPatientFoundForwardedConcept().getConceptId(),
            hivMetadata.getReasonPatientNotFound().getConceptId(),
            hivMetadata.getWhoGaveInformationConcept().getConceptId(),
            hivMetadata.getCardDeliveryDateConcept().getConceptId(),
            hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId(),
            hivMetadata.getArtDatePickupMasterCard().getConceptId()));

    return sqlCohortDefinition;
  }

  /**
   * * <b>Description: A-</b> “Untraced” patients as following (Part I): * *
   *
   * <p><b>Technical Specs:</b>
   *
   * <p><b>1 -</b> All Patients without “Patient Visit Card” (Encounter type 21 or 36 or 37)
   * registered between the most recent scheduled date (as below) and the reporting end date
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition
      getPatientsWithoutVisitCardRegisteredBtwnLastAppointmentOrDrugPickupAndEnddate() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName(
        "Get patients without Visit Card registered between the last scheduled appointment or drugs pick up by reporting end date and the reporting end date");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    sqlCohortDefinition.setQuery(
        TxMlQueries.getPatientsWithoutVisitCardRegisteredBtwnLastAppointmentOrDrugPickupAndEnddate(
            hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId(),
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId(),
            hivMetadata.getReturnVisitDateConcept().getConceptId(),
            hivMetadata.getBuscaActivaEncounterType().getEncounterTypeId(),
            hivMetadata.getVisitaApoioReintegracaoParteAEncounterType().getEncounterTypeId(),
            hivMetadata.getVisitaApoioReintegracaoParteBEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId(),
            hivMetadata.getArtDatePickupMasterCard().getConceptId()));

    return sqlCohortDefinition;
  }

  /**
   * <b><b>Description:</b> All patients who missed (by 28 days) the last scheduled clinical
   * appointment or last drugs pick up-FILA or 30 days
   *
   * <p><b>Technical Specs</b>
   *
   * <blockquote>
   *
   * All patients with the most recent date between the
   *
   * <p><b>(1)</b> last scheduled Drug pick up <b>(concept_id = 5096)</b> date from last Fila form
   * <b>(EncounterType_id = 18)</b> and the
   *
   * <p><b>(2)</b> last scheduled consultation date (concept_id = 1410) from last Ficha Seguimento
   * or Ficha Clinica Form <b>(encounterType_id = 6 or 9)</b> and
   *
   * <p><b>(3)</b> 30 days after the last ART pickup date <b>(concept_id = 23866)</b> from last
   * Recepcao – Levantou ARV Form <b>(encounterType_id = 52)</b>
   *
   * <p>Adding 28 days and this date is less than the reporting end Date and greater and equal than
   * start date minus 1 day
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getAllPatientsWhoMissedNextAppointment() {
    return genericCohortQueries.generalSql(
        "Missed Next appointment",
        TxMlQueries.getPatientsWhoMissedAppointment(
            hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId(),
            hivMetadata.getReturnVisitDateConcept().getConceptId(),
            hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId(),
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId(),
            hivMetadata.getArtDatePickupMasterCard().getConceptId()));
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * Patients who have Reason Patient Missed Visit <b>(obs concept_id = 2016)</b>
   *
   * <ul>
   *   <li>As “Transferido para outra US <b>(concept_id = 1706)</b>” or “Auto-transferencia
   *       <b>(concept_id = 2363)</b>” marked last Home Visit Card <b>(EncounterType_id =21)</b>
   *       occurred during the reporting period.
   *       <p>Use the “data da visita” when the patient reason was marked on the home visit card as
   *       the relference date
   * </ul>
   *
   * </blockquote>
   *
   * @return {@link SqlCohortDefinition}
   */
  public CohortDefinition getPatientsWithMissedVisit() {
    SqlCohortDefinition sql = new SqlCohortDefinition();
    sql.setName("Patients With Missed Visit On Master Card Query");
    sql.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sql.addParameter(new Parameter("endDate", "End Date", Date.class));
    sql.addParameter(new Parameter("location", "location", Location.class));

    sql.setQuery(
        TxMlQueries.getPatientsWithMissedVisit(
            hivMetadata.getBuscaActivaEncounterType().getEncounterTypeId(),
            hivMetadata.getDefaultingMotiveConcept().getConceptId(),
            hivMetadata.getTransferredOutConcept().getConceptId(),
            hivMetadata.getAutoTransferConcept().getConceptId()));

    return sql;
  }

  /**
   * <b>Description:</b> Patients Who have refused or Stopped Treatment Query
   *
   * <p><b>Technical Specs</b>
   *
   * <blockquote>
   *
   * Patients where Home Visit Card <b>(EncounterType_id =21)</b> where Reason Patient Missed Visit
   * <b>(obs concept_id = 2016)</b> in answers =
   *
   * <ul>
   *   <li>Patient Forgot Visit Date <b>(2005)</b>
   *   <li>Patient Is Bedridden At Home <b>(2006)</b>
   *   <li>Distance Or Money For Transport Is To Much For Patient <b>(2007)</b>
   *   <li>Patient Is Dissatisfied With Day Hospital Services <b>(2010)</b>
   *   <li>Fear Of The Provider (23915)absence Of Health Provider In Health Unit <b>(23946)</b>
   *   <li>Patient Does Not Like Arv Treatment Side Effects <b>(2015)</b>
   *   <li>Patient Is Treating Hiv With Traditional Medicine <b>(2013)</b>
   *   <li>Other Reason Why Patient Missed Visit <b>(2017)</b>
   * </ul>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getRefusedOrStoppedTreatmentQuery() {
    SqlCohortDefinition sql = new SqlCohortDefinition();
    sql.setName("Patients Who have refused or Stopped Treatment Query");
    sql.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sql.addParameter(new Parameter("endDate", "End date", Date.class));
    sql.addParameter(new Parameter("location", "location", Location.class));

    sql.setQuery(
        TxMlQueries.getRefusedOrStoppedTreatment(
            hivMetadata.getBuscaActivaEncounterType().getEncounterTypeId(),
            hivMetadata.getDefaultingMotiveConcept().getConceptId(),
            hivMetadata.getPatientForgotVisitDateConcept().getConceptId(),
            hivMetadata.getPatientIsBedriddenAtHomeConcept().getConceptId(),
            hivMetadata.getDistanceOrMoneyForTransportIsTooMuchForPatientConcept().getConceptId(),
            hivMetadata.getPatientIsDissatisfiedWithDayHospitalServicesConcept().getConceptId(),
            hivMetadata.getFearOfTheProviderConcept().getConceptId(),
            hivMetadata.getAbsenceOfHealthProviderInHealthUnitConcept().getConceptId(),
            hivMetadata.getAdverseReaction().getConceptId(),
            hivMetadata.getPatientIsTreatingHivWithTraditionalMedicineConcept().getConceptId(),
            hivMetadata.getOtherReasonWhyPatientMissedVisitConcept().getConceptId(),
            hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId(),
            hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId(),
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getReturnVisitDateConcept().getConceptId(),
            hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId(),
            hivMetadata.getArtDatePickupMasterCard().getConceptId()));

    return sql;
  }

  /**
   * <b>Description:</b> TRACED PATIENTS AND NOT FOUND
   *
   * <p><b>Technical Specs</b>
   *
   * <blockquote>
   *
   * All Patients with “Patient Visit Card” <b>(encounterType 21 or 36 or 37)</b> with "NO" answer
   * <b>(concept_id 1066)</b> for Patient Found <b>(concept_id = 2003)</b>
   *
   * <p>Registered between the most recent scheduled date (as below) and the reporting end date with
   * the following information
   *
   * <p>With For Reason Not Found <b>(obs concept (id = 2031 or id = 23944 or id = 23945))</b> and
   * Answer <b>(id = 2024 or id = 2026 or id = 2011 or id = 2032)</b>
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsTracedAndNotFound() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName("Get patients traced (Unable to locate) and Not Found");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    sqlCohortDefinition.setQuery(
        TxMlQueries.getPatientsTracedWithReasonNotFound(
            hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId(),
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId(),
            hivMetadata.getReturnVisitDateConcept().getConceptId(),
            hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId(),
            hivMetadata.getBuscaActivaEncounterType().getEncounterTypeId(),
            hivMetadata.getVisitaApoioReintegracaoParteAEncounterType().getEncounterTypeId(),
            hivMetadata.getVisitaApoioReintegracaoParteBEncounterType().getEncounterTypeId(),
            hivMetadata.getTypeOfVisitConcept().getConceptId(),
            hivMetadata.getBuscaConcept().getConceptId(),
            hivMetadata.getReasonPatientNotFound().getConceptId(),
            hivMetadata.getDefaultingMotiveConcept().getConceptId(),
            hivMetadata.getReasonForStoppedTakingArvDrugsDuringLast7DaysConcept().getConceptId(),
            hivMetadata.getReasonForStoppedTakingArvDrugsDuringLastMonthConcept().getConceptId(),
            hivMetadata.getMainReasonForDelayInTakingArvConcept().getConceptId(),
            hivMetadata.getPatientRecordHasWrongAddressConcept().getConceptId(),
            hivMetadata.getPatientMovedHousesConcept().getConceptId(),
            hivMetadata.getPatientTookATripConcept().getConceptId(),
            hivMetadata.getOtherReasonsWhyPatientWasNotLocatedByActivistConcept().getConceptId(),
            hivMetadata.getArtDatePickupMasterCard().getConceptId()));

    return sqlCohortDefinition;
  }

  // Patients Traced and Found.
  private CohortDefinition getPatientTracedAndFound() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName("Get patients traced (Unable to locate) and Found");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    sqlCohortDefinition.setQuery(
        TxMlQueries.getPatientsTracedWithVisitCard(
            hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId(),
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId(),
            hivMetadata.getReturnVisitDateConcept().getConceptId(),
            hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId(),
            hivMetadata.getBuscaActivaEncounterType().getEncounterTypeId(),
            hivMetadata.getVisitaApoioReintegracaoParteAEncounterType().getEncounterTypeId(),
            hivMetadata.getVisitaApoioReintegracaoParteBEncounterType().getEncounterTypeId(),
            hivMetadata.getTypeOfVisitConcept().getConceptId(),
            hivMetadata.getBuscaConcept().getConceptId(),
            hivMetadata.getPatientFoundConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getArtDatePickupMasterCard().getConceptId()));

    return sqlCohortDefinition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * All patients who have been on treatment for less than 90 days since the date initiated ARV
   * treatment to the date of their last scheduled clinical contact (the last scheduled clinical
   * appointment or last drugs pick up-FILA or 30 days after last drugs pick-up-MasterCard (the most
   * recent one from 3 sources) by reporting end date)
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsOnARTForLessOrMoreThan180Days(Integer periodFlag) {
    CalculationCohortDefinition cd =
        new CalculationCohortDefinition(
            "lessThanOrMoreThan90DaysPatients",
            Context.getRegisteredComponents(StartedArtOnLastClinicalContactCalculation.class)
                .get(0));

    cd.addCalculationParameter("periodFlag", periodFlag);
    cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    return cd;
  }

  /**
   * <b>Disaggregation: Patient IIT Breakdown</b>
   * <li>On Treatment for <3 months when experienced IIT All patients who have been on treatment for
   *     less than 90 days since the date initiated ARV treatment (TX_ML_FR4) to the date of their
   *     last scheduled ARV pick-up
   * <li>On Treatment for 3-5 months when experienced IIT All patients who have been on treatment
   *     for greater or equal than 90 days and less than 180 days since the date initiated ARV
   *     treatment (TX_ML_FR4) to the date of their last scheduled ARV pick-up
   * <li>On Treatment for >=6 months when experienced IIT All patients who have been on treatment
   *     for greater or equal than 180 days since the date initiated ARV treatment (TX_ML_FR4) to
   *     the date of their last scheduled ARV pick-up
   *
   * @see CommonQueries#getARTStartDate(boolean)
   * @param minDays minimum of days of interruption
   * @param maxDays maximum of days of interruption
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getTreatmentInterruptionOfXDaysBeforeReturningToTreatment(
      Integer minDays, Integer maxDays) {
    SqlCohortDefinition definition = new SqlCohortDefinition();

    definition.setName("Disaggregation: Patient IIT Breakdown");

    definition.setQuery(
        TxMlQueries.getTreatmentInterruptionOfXDaysBeforeReturningToTreatmentQuery(
            hivMetadata.getARVPharmaciaEncounterType(),
            hivMetadata.getReturnVisitDateForArvDrugConcept(),
            hivMetadata.getMasterCardDrugPickupEncounterType(),
            hivMetadata.getArtDatePickupMasterCard(),
            minDays,
            maxDays));

    definition.addParameter(new Parameter("endDate", "endDate", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));

    return definition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <b>All patients who after the most recent date from below criterias:</b>
   *
   * <p><b>(Patient_program.program_id = 2)</b> = (SERVICO TARV-TRATAMENTO) and Patient_State.state
   * = 7 (Transferred-out) or Patient_State.start_date <= endDate Patient_state.end_date is null And
   *
   * <p><b>encounterType_id = 53</b>, Estado de Permanencia <b>(concept Id 6272)</b> =
   * Transferred-out <b>(concept_id = 1706)</b> obs_datetime <= endDate OR Encounter Type ID= 6
   * Estado de Permanencia <b>(concept_id 6273)</b> = Transferred-out <b>(concept_id 1706)</b>
   * Encounter_datetime <= endDate And
   *
   * <p><b>EncounterType_id = 21</b>, Last Encounter_datetime <=endDate Reason Patient Missed Visit
   * <b>(obs concept id = 2016)</b> Answers = "Transferred Out To Another Facility" <b>(id =
   * 1706)</b> OR "Auto Transfer" <b>(id = 23863)</b> have a drugs pick up or consultation
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientWithFilaOrConsultationAfterTrasnferDiedMissed() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName("get Patients With Most Recent Date Have Fila or Consultation ");
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
        "buscaActivaEncounterType", hivMetadata.getBuscaActivaEncounterType().getEncounterTypeId());
    map.put("artProgram", hivMetadata.getARTProgram().getProgramId());
    map.put(
        "transferredOutToAnotherHealthFacilityWorkflowState",
        hivMetadata
            .getTransferredOutToAnotherHealthFacilityWorkflowState()
            .getProgramWorkflowStateId());

    String query =
        "  SELECT mostrecent.patient_id "
            + "FROM ("
            + " SELECT lastest.patient_id ,lastest.last_date  "
            + " FROM (  "
            + "    SELECT p.patient_id , MAX(ps.start_date) AS last_date  "
            + "    FROM patient p   "
            + "        INNER JOIN patient_program pg   "
            + "            ON p.patient_id=pg.patient_id   "
            + "        INNER JOIN patient_state ps   "
            + "            ON pg.patient_program_id=ps.patient_program_id   "
            + "    WHERE pg.voided=0   "
            + "        AND ps.voided=0   "
            + "        AND p.voided=0   "
            + "        AND pg.program_id= ${artProgram}  "
            + "        AND ps.state = ${transferredOutToAnotherHealthFacilityWorkflowState}   "
            + "        AND ps.start_date<= :endDate    "
            + "        AND pg.location_id= :location   "
            + "    group by p.patient_id  "
            + "  "
            + "    UNION  "
            + "  "
            + "    SELECT  p.patient_id,  MAX(o.obs_datetime) AS last_date  "
            + "    FROM patient p    "
            + "        INNER JOIN encounter e   "
            + "            ON e.patient_id=p.patient_id   "
            + "        INNER JOIN obs o   "
            + "            ON o.encounter_id=e.encounter_id   "
            + "    WHERE  p.voided = 0   "
            + "        AND e.voided = 0   "
            + "        AND o.voided = 0   "
            + "        AND e.encounter_type = ${masterCardEncounterType}   "
            + "        AND o.concept_id = ${stateOfStayOfPreArtPatient}  "
            + "        AND o.value_coded =  ${transferredOutConcept}   "
            + "        AND o.obs_datetime <=  :endDate   "
            + "        AND e.location_id =  :location   "
            + "    GROUP BY p.patient_id  "
            + "    UNION   "
            + "    SELECT  p.patient_id ,MAX(e.encounter_datetime) AS last_date  "
            + "    FROM patient p    "
            + "        INNER JOIN encounter e   "
            + "            ON e.patient_id=p.patient_id   "
            + "        INNER JOIN obs o   "
            + "            ON o.encounter_id=e.encounter_id   "
            + "    WHERE  p.voided = 0   "
            + "        AND e.voided = 0   "
            + "        AND o.voided = 0   "
            + "        AND e.encounter_type = ${adultoSeguimentoEncounterType}  "
            + "        AND o.concept_id = ${stateOfStayOfArtPatient}  "
            + "        AND o.value_coded = ${transferredOutConcept}   "
            + "        AND e.encounter_datetime <=  :endDate   "
            + "        AND e.location_id =  :location  "
            + "    GROUP BY p.patient_id   "
            + "  "
            + "    UNION  "
            + "  "
            + "    SELECT p.patient_id, MAX(e.encounter_datetime) last_date   "
            + "    FROM patient p   "
            + "        INNER JOIN encounter e   "
            + "              ON p.patient_id = e.patient_id   "
            + "        INNER JOIN obs o   "
            + "              ON e.encounter_id = o.encounter_id   "
            + "    WHERE o.concept_id = ${defaultingMotiveConcept}  "
            + "    	   AND e.location_id = :location   "
            + "        AND e.encounter_type= ${buscaActivaEncounterType}   "
            + "        AND e.encounter_datetime BETWEEN :startDate AND :endDate AND p.voided=0  "
            + "		   AND o.value_coded IN (${transferredOutConcept} ,${autoTransferConcept})  "
            + "        AND e.voided=0   "
            + "        AND o.voided=0   "
            + "        AND p.voided=0   "
            + "    GROUP BY p.patient_id "
            + ") lastest   "
            + " INNER JOIN encounter e ON e.patient_id = lastest.patient_id   "
            + " INNER JOIN obs o ON o.encounter_id = e.encounter_id   "
            + " WHERE  e.voided = 0  "
            + "        AND o.voided = 0  "
            + "        AND (( e.encounter_type = ${masterCardDrugPickupEncounterType} "
            + "					AND o.concept_id = ${artDatePickup} "
            + "					AND o.value_datetime > lastest.last_date "
            + "					AND  o.value_datetime <= :endDate)  "
            + "        OR  ( e.encounter_type IN (${adultoSeguimentoEncounterType},${pediatriaSeguimentoEncounterType},${pharmaciaEncounterType})  "
            + "					AND e.encounter_datetime > lastest.last_date "
            + "					AND  e.encounter_datetime <= :endDate))  "
            + "        AND e.location_id = :location  "
            + " GROUP BY lastest.patient_id) mostrecent"
            + " GROUP BY mostrecent.patient_id";

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
   * All deaths <b>(Patient_State.state = 10)</b> in ART Service Program
   * <b>(Patient_program.program_id = 2)</b> registered in Patient Program State by reporting end
   * date
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  @DocumentedDefinition(value = "patientsDeadInProgramStateByReportingEndDate")
  public CohortDefinition getPatientsDeadInProgramStateByReportingEndDate() {

    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("patientsDeadInProgramStateByReportingEndDate");

    definition.setQuery(
        TxMlQueries.getPatientsListBasedOnProgramAndStateByReportingEndDate(
            hivMetadata.getARTProgram().getProgramId(),
            hivMetadata.getArtDeadWorkflowState().getProgramWorkflowStateId()));

    definition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));

    return definition;
  }

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * All deaths <b>(Patient_State.state = 10)</b> in ART Service Program
   * <b>(Patient_program.program_id = 2)</b> registered in Patient Program State by reporting end
   * date
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  @DocumentedDefinition(value = "patientsSuspendedInProgramStateByReportingEndDate")
  public CohortDefinition getPatientsSuspendedInProgramStateByReportingEndDate() {

    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("patientsDeadInProgramStateByReportingEndDate");

    definition.setQuery(
        TxMlQueries.getPatientsListBasedOnProgramAndStateByReportingEndDate(
            hivMetadata.getARTProgram().getProgramId(),
            hivMetadata.getSuspendedTreatmentWorkflowState().getProgramWorkflowStateId()));

    definition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));

    return definition;
  }

  /**
   * <b>On Treatment for >=6 months when experienced IIT</b>
   * <li>All patients who have been on treatment for greater or equal than 180 days since the date
   *     initiated ARV treatment (TX_ML_FR4) to the date of their last scheduled ARV pick-up
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsIITMoreThan180DaysComposition() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Get patients who are Lost To Follow Up Composition");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "missedAppointment",
        EptsReportUtils.map(
            getPatientsWithIITComposition(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "moreThan180Days",
        EptsReportUtils.map(
            getTreatmentInterruptionOfXDaysBeforeReturningToTreatment(180, null),
            "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "iit",
        EptsReportUtils.map(
            getPatientsWhoExperiencedInterruptionInTreatmentComposition(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "suspendedTreatment",
        EptsReportUtils.map(
            txCurrCohortQueries.getPatientsWhoStoppedOrSuspendedTreatment(),
            "onOrBefore=${endDate},location=${location}"));

    cd.setCompositionString(
        "(missedAppointment AND (iit OR suspendedTreatment) AND moreThan180Days)");
    return cd;
  }

  /**
   * <b>On Treatment for <3 months when experienced IIT</b>
   * <li>All patients who have been on treatment for less than 90 days since the date initiated ARV
   *     treatment (TX_ML_FR4) to the date of their last scheduled ARV pick-up
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsIITLessThan90DaysComposition() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("On Treatment for >=6 months when experienced IIT");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "missedAppointment",
        EptsReportUtils.map(
            getPatientsWithIITComposition(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "lessThan90days",
        EptsReportUtils.map(
            getTreatmentInterruptionOfXDaysBeforeReturningToTreatment(null, 90),
            "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "iit",
        EptsReportUtils.map(
            getPatientsWhoExperiencedInterruptionInTreatmentComposition(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "suspendedTreatment",
        EptsReportUtils.map(
            txCurrCohortQueries.getPatientsWhoStoppedOrSuspendedTreatment(),
            "onOrBefore=${endDate},location=${location}"));

    cd.setCompositionString(
        "(missedAppointment AND (iit OR suspendedTreatment) AND lessThan90days)");

    return cd;
  }

  /**
   * <b>On Treatment for 3-5 months when experienced IIT</b>
   * <li>All patients who have been on treatment for greater or equal than 90 days and less than 180
   *     days since the date initiated ARV treatment (TX_ML_FR4) to the date of their last scheduled
   *     ARV pick-up
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsIITBetween90DaysAnd180DaysComposition() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Get patients who are Lost To Follow Up Composition between 3-5 months");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "missedAppointment",
        EptsReportUtils.map(
            getPatientsWithIITComposition(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "greaterThan90AndLessThan180days",
        EptsReportUtils.map(
            getTreatmentInterruptionOfXDaysBeforeReturningToTreatment(90, 180),
            "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "iit",
        EptsReportUtils.map(
            getPatientsWhoExperiencedInterruptionInTreatmentComposition(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "suspendedTreatment",
        EptsReportUtils.map(
            txCurrCohortQueries.getPatientsWhoStoppedOrSuspendedTreatment(),
            "onOrBefore=${endDate},location=${location}"));

    cd.setCompositionString(
        "(missedAppointment AND (iit OR suspendedTreatment) AND greaterThan90AndLessThan180days)");
    return cd;
  }

  /**
   * <b>Description:</b> “Interruption In Treatment Total” will have the following combination:
   *
   * <ul>
   *   <li>(IIT<3month OR IITBetween3-5month OR IIT>6months)
   * </ul>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithIITComposition() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Patients experienced Interruption in Treatment (IIT)");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "missedAppointment",
        EptsReportUtils.map(
            getPatientsWhoMissedNextAppointmentAndNoScheduledDrugPickupOrNextConsultation(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "iit",
        EptsReportUtils.map(
            getPatientsWhoExperiencedInterruptionInTreatmentComposition(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "suspendedTreatment",
        EptsReportUtils.map(
            txCurrCohortQueries.getPatientsWhoStoppedOrSuspendedTreatment(),
            "onOrBefore=${endDate},location=${location}"));

    cd.setCompositionString("missedAppointment AND (iit OR suspendedTreatment)");
    return cd;
  }

  public CohortDefinition getTransferredOutPatientsCompositionWithoutVisitCard() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();

    sqlCohortDefinition.setName(
        "Patient Transferred Out With No Drug Pick After The Transferred out Date ");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());

    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put(
        "7",
        hivMetadata
            .getTransferredOutToAnotherHealthFacilityWorkflowState()
            .getProgramWorkflowStateId());

    String query =
        "  SELECT mostrecent.patient_id "
            + "FROM ("
            + " SELECT lastest.patient_id ,Max(lastest.last_date) as  last_date "
            + " FROM (  "
            + "    SELECT p.patient_id , Max(ps.start_date) AS last_date  "
            + "    FROM patient p   "
            + "        INNER JOIN patient_program pg   "
            + "            ON p.patient_id=pg.patient_id   "
            + "        INNER JOIN patient_state ps   "
            + "            ON pg.patient_program_id=ps.patient_program_id   "
            + "    WHERE pg.voided=0   "
            + "        AND ps.voided=0   "
            + "        AND p.voided=0   "
            + "        AND pg.program_id= ${2}  "
            + "        AND ps.state = ${7}   "
            + "        AND ps.start_date <= :endDate    "
            + "        AND pg.location_id= :location   "
            + "    group by p.patient_id  "
            + "  "
            + "    UNION  "
            + "  "
            + "    SELECT  p.patient_id,  Max(o.obs_datetime) AS last_date  "
            + "    FROM patient p    "
            + "        INNER JOIN encounter e   "
            + "            ON e.patient_id=p.patient_id   "
            + "        INNER JOIN obs o   "
            + "            ON o.encounter_id=e.encounter_id   "
            + "    WHERE  p.voided = 0   "
            + "        AND e.voided = 0   "
            + "        AND o.voided = 0   "
            + "        AND e.encounter_type = ${53}   "
            + "        AND o.concept_id = ${6272}  "
            + "        AND o.value_coded =  ${1706}   "
            + "        AND o.obs_datetime <= :endDate   "
            + "        AND e.location_id =  :location   "
            + "    GROUP BY p.patient_id  "
            + "    UNION   "
            + "    SELECT  p.patient_id , Max(e.encounter_datetime) AS last_date  "
            + "    FROM patient p    "
            + "        INNER JOIN encounter e   "
            + "            ON e.patient_id=p.patient_id   "
            + "        INNER JOIN obs o   "
            + "            ON o.encounter_id=e.encounter_id   "
            + "    WHERE  p.voided = 0   "
            + "        AND e.voided = 0   "
            + "        AND o.voided = 0   "
            + "        AND e.encounter_type = ${6}  "
            + "        AND o.concept_id = ${6273}  "
            + "        AND o.value_coded = ${1706}   "
            + "        AND e.encounter_datetime <= :endDate   "
            + "        AND e.location_id =  :location  "
            + "    GROUP BY p.patient_id   "
            + ") lastest   "
            + "WHERE lastest.patient_id NOT  IN("
            + " "
            + "  			     SELECT  p.patient_id    "
            + "	                 FROM patient p      "
            + "	                     INNER JOIN encounter e     "
            + "	                         ON e.patient_id=p.patient_id     "
            + "	                 WHERE  p.voided = 0     "
            + "	                     AND e.voided = 0     "
            + "	                     AND e.encounter_type IN (${6},"
            + "${9},"
            + "${18})    "
            + "	                     AND e.encounter_datetime > lastest.last_date "
            + " AND e.encounter_datetime <=  :endDate    "
            + "	                     AND e.location_id =  :location    "
            + "	                 GROUP BY p.patient_id "
            + " UNION "
            + "        			 SELECT  p.patient_id    "
            + "	                 FROM patient p       "
            + "	                      INNER JOIN encounter e      "
            + "	                          ON e.patient_id=p.patient_id      "
            + "	                      INNER JOIN obs o      "
            + "	                          ON o.encounter_id=e.encounter_id      "
            + "	                  WHERE  p.voided = 0      "
            + "	                      AND e.voided = 0      "
            + "	                      AND o.voided = 0      "
            + "	                      AND e.encounter_type = ${52}     "
            + "	                      AND o.concept_id = ${23866}     "
            + "	                      AND o.value_datetime > lastest.last_date  "
            + " AND o.value_datetime <= :endDate      "
            + "	                      AND e.location_id =  :location     "
            + "	                  GROUP BY p.patient_id   "
            + ")  "
            + " GROUP BY lastest.patient_id"
            + " )mostrecent "
            + " GROUP BY mostrecent.patient_id";

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
   * <b>9 –</b> All deaths registered in Ficha Resumo and Ficha Clinica of Master Card by reporting
   * end date Encounter Type ID= 6 or 53 Estado de Permanencia (Concept Id 6272) = Dead (Concept ID
   * 1709) Encounter_datetime <= endDate
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  @DocumentedDefinition(
      value = "suspendedPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate")
  public CohortDefinition getDeadPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate() {
    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName("suspendedPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate");

    definition.setQuery(
        TXCurrQueries.getDeadPatientsInFichaResumeAndClinicaOfMasterCardByReportEndDate(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getStateOfStayPriorArtPatientConcept().getConceptId(),
            hivMetadata.getStateOfStayOfArtPatient().getConceptId(),
            hivMetadata.getSuspendedTreatmentConcept().getConceptId()));

    definition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));

    return definition;
  }

  /**
   * The system will consider patient as transferred out as above defined only if the most recent
   * date between (next scheduled ART pick-up on FILA + 1 day) and (the most recent ART pickup date
   * on Ficha Recepção – Levantou ARVs + 31 days) falls during the reporting period.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getTransferredOutBetweenNextPickupDateFilaAndRecepcaoLevantou(
      boolean duringPeriod) {

    SqlCohortDefinition definition = new SqlCohortDefinition();
    definition.setName(
        "Patients Transfered Out between (next scheduled ART pick-up on FILA + 1 day) "
            + "and (the most recent ART pickup date on Ficha Recepção – Levantou ARVs + 31 days");

    definition.addParameter(new Parameter("startDate", "startDate", Date.class));
    definition.addParameter(new Parameter("endDate", "endDate", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    valuesMap.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    valuesMap.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());
    valuesMap.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());

    String query =
        "SELECT final.patient_id FROM  ( "
            + "SELECT considered_transferred.patient_id, max(considered_transferred.value_datetime) as max_date "
            + "FROM ( "
            + "               SELECT     p.patient_id, "
            + "                          date_add(max(o.value_datetime), interval 1 day) AS value_datetime "
            + "               FROM       patient p "
            + "                              INNER JOIN encounter e "
            + "                                         ON         e.patient_id=p.patient_id "
            + "                              INNER JOIN obs o "
            + "                                         ON         o.encounter_id=e.encounter_id "
            + "               WHERE      p.voided = 0 "
            + "                 AND        e.voided = 0 "
            + "                 AND        o.voided = 0 "
            + "                 AND        e.encounter_type = ${18} "
            + "                 AND        o.concept_id = ${5096} "
            + "                 AND        e.encounter_datetime <= :endDate "
            + "                 AND        e.location_id = :location "
            + "               GROUP BY   p.patient_id "
            + " UNION "
            + "               SELECT     p.patient_id, "
            + "                          date_add(max(o.value_datetime), interval 31 day)  AS value_datetime "
            + "               FROM       patient p "
            + "                              INNER JOIN encounter e "
            + "                                         ON         e.patient_id=p.patient_id "
            + "                              INNER JOIN obs o "
            + "                                         ON         o.encounter_id=e.encounter_id "
            + "               WHERE      p.voided = 0 "
            + "                 AND        e.voided = 0 "
            + "                 AND        o.voided = 0 "
            + "                 AND        e.encounter_type = ${52} "
            + "                 AND        o.concept_id = ${23866} "
            + "                 AND        o.value_datetime  <= :endDate  "
            + "                 AND        e.location_id = :location "
            + "               GROUP BY   p.patient_id "
            + " )  considered_transferred "
            + " GROUP BY considered_transferred.patient_id "
            + " ) final "
            + " WHERE  ".concat(duringPeriod ? " final.max_date >= :startDate  AND " : " ")
            + " final.max_date  <= :endDate  ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);
    definition.setQuery(stringSubstitutor.replace(query));

    return definition;
  }
}
