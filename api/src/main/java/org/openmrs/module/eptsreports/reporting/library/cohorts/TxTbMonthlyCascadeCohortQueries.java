package org.openmrs.module.eptsreports.reporting.library.cohorts;

import static org.openmrs.module.eptsreports.reporting.library.cohorts.TxTbMonthlyCascadeCohortQueries.TxTbComposition.NEWART;
import static org.openmrs.module.eptsreports.reporting.library.cohorts.TxTbMonthlyCascadeCohortQueries.TxTbComposition.PREVIOUSLYART;

import java.util.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TxTbMonthlyCascadeCohortQueries {

  @Autowired private TxCurrCohortQueries txCurrCohortQueries;

  @Autowired private TXTBCohortQueries txtbCohortQueries;
  @Autowired private GenericCohortQueries genericCohortQueries;

  @Autowired private HivMetadata hivMetadata;
  @Autowired private TbMetadata tbMetadata;

  public CohortDefinition getTxCurrOrTxCurrWithClinicalConsultation(
      TxCurrComposition indicator1and2Composition) {
    CompositionCohortDefinition chd = new CompositionCohortDefinition();
    chd.addParameter(new Parameter("startDate", "startDate", Date.class));
    chd.addParameter(new Parameter("endDate", "endDate", Date.class));
    chd.addParameter(new Parameter("location", "location", Location.class));
    chd.setName(indicator1and2Composition.getName());

    CohortDefinition newOnArt = getPatientsNewOnArt();
    CohortDefinition previouslyOnArt = getPatientsPreviouslyOnArt();

    chd.addSearch(
        TxCurrComposition.TXCURR.getKey(),
        EptsReportUtils.map(
            txCurrCohortQueries.getTxCurrCompositionCohort("tx_curr", true),
            "onOrBefore=${endDate},location=${location}"));

    chd.addSearch(
        TxCurrComposition.CLINICAL.getKey(),
        EptsReportUtils.map(
            getPatientsWithClinicalConsultationInLast6Months(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    chd.addSearch(
        TxCurrComposition.NEWART.getKey(),
        EptsReportUtils.map(
            newOnArt, "startDate=${startDate},endDate=${endDate},location=${location}"));

    chd.addSearch(
        TxCurrComposition.PREVIOUSLYART.getKey(),
        EptsReportUtils.map(previouslyOnArt, "endDate=${endDate-6m},location=${location}"));

    chd.setCompositionString(indicator1and2Composition.getCompositionString());
    return chd;
  }

  public CohortDefinition getTxTbDenominatorCohort(TxTbComposition txTbComposition) {
    CompositionCohortDefinition chd = new CompositionCohortDefinition();
    chd.addParameter(new Parameter("startDate", "startDate", Date.class));
    chd.addParameter(new Parameter("endDate", "endDate", Date.class));
    chd.addParameter(new Parameter("location", "location", Location.class));
    chd.setName(txTbComposition.getName());

    CohortDefinition newOnArt = getPatientsNewOnArt();
    CohortDefinition previouslyOnArt = getPatientsPreviouslyOnArt();

    chd.addSearch(
        TxTbComposition.TXTB_DENOMINATOR.getKey(),
        EptsReportUtils.map(
            txtbCohortQueries.getDenominator(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    chd.addSearch(
        TxTbComposition.POSITIVESCREENING.getKey(),
        EptsReportUtils.map(
            txtbCohortQueries.positiveScreening(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    chd.addSearch(
        TxTbComposition.NEGATIVESCREENING.getKey(),
        EptsReportUtils.map(
            txtbCohortQueries.newOnARTNegativeScreening(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    chd.addSearch(
        TxTbComposition.CLINICAL.getKey(),
        EptsReportUtils.map(
            getPatientsWithClinicalConsultationInLast6Months(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    chd.addSearch(
        NEWART.getKey(),
        EptsReportUtils.map(
            newOnArt, "startDate=${startDate},endDate=${endDate},location=${location}"));

    chd.addSearch(
        PREVIOUSLYART.getKey(),
        EptsReportUtils.map(previouslyOnArt, "endDate=${endDate-6m},location=${location}"));

    chd.addSearch(
        TxCurrComposition.TXCURR.getKey(),
        EptsReportUtils.map(
            txCurrCohortQueries.getTxCurrCompositionCohort("tx_curr", true),
            "onOrBefore=${endDate},location=${location}"));

    chd.setCompositionString(txTbComposition.getCompositionString());
    return chd;
  }

  public CohortDefinition getTxTbNumeratorCohort(TxTbComposition txTbComposition) {
    CompositionCohortDefinition chd = new CompositionCohortDefinition();
    chd.addParameter(new Parameter("startDate", "startDate", Date.class));
    chd.addParameter(new Parameter("endDate", "endDate", Date.class));
    chd.addParameter(new Parameter("location", "location", Location.class));
    chd.setName(txTbComposition.getName());

    CohortDefinition newOnArt = getPatientsNewOnArt();
    CohortDefinition previouslyOnArt = getPatientsPreviouslyOnArt();

    chd.addSearch(
        TxTbComposition.NUMERATOR.getKey(),
        EptsReportUtils.map(
            txtbCohortQueries.txTbNumerator(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    chd.addSearch(
        NEWART.getKey(),
        EptsReportUtils.map(
            newOnArt, "startDate=${startDate},endDate=${endDate},location=${location}"));

    chd.addSearch(
        PREVIOUSLYART.getKey(),
        EptsReportUtils.map(previouslyOnArt, "endDate=${endDate-6m},location=${location}"));

    chd.addSearch(
        TxCurrComposition.TXCURR.getKey(),
        EptsReportUtils.map(
            txCurrCohortQueries.getTxCurrCompositionCohort("tx_curr", true),
            "onOrBefore=${endDate},location=${location}"));

    chd.setCompositionString(txTbComposition.getCompositionString());
    return chd;
  }

  /**
   * Apply the disaggregation as following: Patients New on ART as follows (A) Check
   *
   * @return getPatientsNewOnArt() java doc
   */
  public CohortDefinition getPatientsOnTxCurrAndNewOnArt() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Patients New on ART (A)");
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition txcurr = txCurrCohortQueries.getTxCurrCompositionCohort("tx_curr", true);
    CohortDefinition newOnArt = getPatientsNewOnArt();

    cd.addSearch(
        "txcurr", EptsReportUtils.map(txcurr, "onOrBefore=${endDate},location=${location}"));
    cd.addSearch(
        "newOnArt",
        EptsReportUtils.map(
            newOnArt, "startDate=${endDate-6m+1d},endDate=${endDate},location=${location}"));

    cd.setCompositionString("txcurr AND newOnArt");
    return cd;
  }

  /**
   * This Cohort Composition implements all combinations to provide indicators 5, 6a and 7 For more
   * details check the java doc of the method on the composition
   *
   * @return CohortDefinition
   */
  public CohortDefinition get5And6and7(SemearTbLamGXPertComposition semearTbLamGXPertComposition) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Indicator 5");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition sent = txtbCohortQueries.specimenSent();
    CohortDefinition sixa = getSixaComposition();
    CohortDefinition sixb = getSixbComposition();
    CohortDefinition mwrd = txtbCohortQueries.getmWRD();
    CohortDefinition semear = txtbCohortQueries.getSmearMicroscopyOnly();
    CohortDefinition tbLam = getPetientsHaveTBLAM();
    CohortDefinition others = txtbCohortQueries.getAdditionalTest();
    CohortDefinition newOnArt = getPatientsNewOnArt();
    CohortDefinition previouslyOnArt = getPatientsPreviouslyOnArt();

    cd.addSearch(
        SemearTbLamGXPertComposition.SIXA.getKey(),
        EptsReportUtils.map(
            sixa, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        SemearTbLamGXPertComposition.FIVE.getKey(),
        EptsReportUtils.map(
            sent, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        SemearTbLamGXPertComposition.SIXB.getKey(),
        EptsReportUtils.map(
            sixb, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        SemearTbLamGXPertComposition.MWRD.getKey(),
        EptsReportUtils.map(
            mwrd, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        SemearTbLamGXPertComposition.SEMEAR.getKey(),
        EptsReportUtils.map(
            semear, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        SemearTbLamGXPertComposition.TBLAM.getKey(),
        EptsReportUtils.map(
            tbLam, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        SemearTbLamGXPertComposition.OTHER.getKey(),
        EptsReportUtils.map(
            others, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        TxTbComposition.NUMERATOR.getKey(),
        EptsReportUtils.map(
            patientsPreviouslyOnARTNumerator(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        NEWART.getKey(),
        EptsReportUtils.map(
            newOnArt, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        PREVIOUSLYART.getKey(),
        EptsReportUtils.map(previouslyOnArt, "endDate=${endDate-6m},location=${location}"));

    cd.setCompositionString(semearTbLamGXPertComposition.getCompositionString());
    return cd;
  }

  public CohortDefinition getSixaComposition() {

    CompositionCohortDefinition composition = new CompositionCohortDefinition();
    composition.setName("Composition to return positive results for specific tests");
    composition.addParameter(new Parameter("startDate", "startDate", Date.class));
    composition.addParameter(new Parameter("endDate", "endDate", Date.class));
    composition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition mwrd =
        getPositiveResultsComposition(txtbCohortQueries.getmWRD(), tbMetadata.getPositiveConcept());
    CohortDefinition semear =
        getPositiveResultsSmearComposition(txtbCohortQueries.getSmearMicroscopyOnly(), true);
    CohortDefinition tbLam =
        getPositiveResultsTblamComposition(getPetientsHaveTBLAM(), tbMetadata.getPositiveConcept());
    CohortDefinition others = getPatientsInOthersWithoutGenexPert(tbMetadata.getPositiveConcept());
    CohortDefinition sent = txtbCohortQueries.specimenSent();

    composition.addSearch(
        SemearTbLamGXPertComposition.SEMEAR.getKey(),
        EptsReportUtils.map(
            semear, "startDate=${startDate},endDate=${endDate},location=${location}"));

    composition.addSearch(
        SemearTbLamGXPertComposition.TBLAM.getKey(),
        EptsReportUtils.map(
            tbLam, "startDate=${startDate},endDate=${endDate},location=${location}"));

    composition.addSearch(
        SemearTbLamGXPertComposition.OTHER.getKey(),
        EptsReportUtils.map(
            others, "startDate=${startDate},endDate=${endDate},location=${location}"));

    composition.addSearch(
        SemearTbLamGXPertComposition.MWRD.getKey(),
        EptsReportUtils.map(
            mwrd, "startDate=${startDate},endDate=${endDate},location=${location}"));

    composition.addSearch(
        SemearTbLamGXPertComposition.FIVE.getKey(),
        EptsReportUtils.map(
            sent, "startDate=${startDate},endDate=${endDate},location=${location}"));

    composition.setCompositionString("FIVE AND (MWRD OR SEMEAR OR TBLAM OR OTHER)");

    return composition;
  }

  public CohortDefinition getSixbComposition() {

    CompositionCohortDefinition composition = new CompositionCohortDefinition();
    composition.setName("Composition to return negative results for specific tests");
    composition.addParameter(new Parameter("startDate", "startDate", Date.class));
    composition.addParameter(new Parameter("endDate", "endDate", Date.class));
    composition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition mwrd =
        getPositiveResultsComposition(txtbCohortQueries.getmWRD(), tbMetadata.getNegative());
    CohortDefinition semear =
        getPositiveResultsSmearComposition(txtbCohortQueries.getSmearMicroscopyOnly(), false);
    CohortDefinition tbLam =
        getPositiveResultsTblamComposition(getPetientsHaveTBLAM(), tbMetadata.getNegative());
    CohortDefinition others = getPatientsInOthersWithoutGenexPert(tbMetadata.getNegative());
    CohortDefinition sixa = getSixaComposition();

    CohortDefinition sent = txtbCohortQueries.specimenSent();

    composition.addSearch(
        SemearTbLamGXPertComposition.SEMEAR.getKey(),
        EptsReportUtils.map(
            semear, "startDate=${startDate},endDate=${endDate},location=${location}"));

    composition.addSearch(
        SemearTbLamGXPertComposition.TBLAM.getKey(),
        EptsReportUtils.map(
            tbLam, "startDate=${startDate},endDate=${endDate},location=${location}"));

    composition.addSearch(
        SemearTbLamGXPertComposition.OTHER.getKey(),
        EptsReportUtils.map(
            others, "startDate=${startDate},endDate=${endDate},location=${location}"));

    composition.addSearch(
        SemearTbLamGXPertComposition.MWRD.getKey(),
        EptsReportUtils.map(
            mwrd, "startDate=${startDate},endDate=${endDate},location=${location}"));

    composition.addSearch(
        SemearTbLamGXPertComposition.SIXA.getKey(),
        EptsReportUtils.map(
            sixa, "startDate=${startDate},endDate=${endDate},location=${location}"));

    composition.addSearch(
        SemearTbLamGXPertComposition.FIVE.getKey(),
        EptsReportUtils.map(
            sent, "startDate=${startDate},endDate=${endDate},location=${location}"));

    composition.setCompositionString("(FIVE AND (MWRD OR SEMEAR OR TBLAM OR OTHER)) AND NOT SIXA");

    return composition;
  }

  public CohortDefinition getPositiveResultsComposition(
      CohortDefinition definition, Concept resultConcept) {

    CompositionCohortDefinition composition = new CompositionCohortDefinition();
    composition.setName("Composition to return positive results for specific tests");
    composition.addParameter(new Parameter("startDate", "startDate", Date.class));
    composition.addParameter(new Parameter("endDate", "endDate", Date.class));
    composition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition positiveOrNegavtiveResults = getPatientsGeneXpertMtbRif(resultConcept);

    composition.addSearch("EXAMS", Mapped.mapStraightThrough(definition));
    composition.addSearch("POSNEG", Mapped.mapStraightThrough(positiveOrNegavtiveResults));

    composition.setCompositionString("EXAMS AND POSNEG");

    return composition;
  }
  /**
   * <b>Patients with Smear and Positive results</b>
   *
   * @param definition TBLAM Cohort Definition
   * @param positiveOrNegative Concept of the requested result
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPositiveResultsSmearComposition(
      CohortDefinition definition, boolean positiveOrNegative) {

    CompositionCohortDefinition composition = new CompositionCohortDefinition();
    composition.setName("Composition to return positive results for specific tests");
    composition.addParameter(new Parameter("startDate", "startDate", Date.class));
    composition.addParameter(new Parameter("endDate", "endDate", Date.class));
    composition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition positiveOrNegativeresults;
    if (positiveOrNegative) {
      positiveOrNegativeresults = txtbCohortQueries.getSmearMicroscopyOnlyPositiveResult(false);
    } else {
      positiveOrNegativeresults = getSmearMicroscopyOnlyNegative();
    }

    composition.addSearch("EXAMS", Mapped.mapStraightThrough(definition));
    composition.addSearch("POSNEG", Mapped.mapStraightThrough(positiveOrNegativeresults));

    composition.setCompositionString("EXAMS AND POSNEG");

    return composition;
  }

  /**
   * <b>Patients with TBLAM and Positive results</b>
   *
   * @param definition TBLAM Cohort Definition
   * @param positiveOrNegative Concept of the requested result
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPositiveResultsTblamComposition(
      CohortDefinition definition, Concept positiveOrNegative) {

    CompositionCohortDefinition composition = new CompositionCohortDefinition();
    composition.setName("Composition to return positive results for specific tests");
    composition.addParameter(new Parameter("startDate", "startDate", Date.class));
    composition.addParameter(new Parameter("endDate", "endDate", Date.class));
    composition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition positiveOrNegativeResults =
        getPatientsHaveTBLAMpositiveOrNegativeResult(Collections.singletonList(positiveOrNegative));

    composition.addSearch("EXAMS", Mapped.mapStraightThrough(definition));
    composition.addSearch("POSNEG", Mapped.mapStraightThrough(positiveOrNegativeResults));

    composition.setCompositionString("EXAMS AND POSNEG");

    return composition;
  }

  /**
   * Cohort Composition to get patients on Tx Curr and previously on Art
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsOnTxCurrAndPreviouslyOnArt() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Patients on Tx Curr and previously on Art");
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition txcurr = txCurrCohortQueries.getTxCurrCompositionCohort("tx_curr", true);
    CohortDefinition previouslyOnArt = getPatientsPreviouslyOnArt();

    cd.addSearch(
        "txcurr", EptsReportUtils.map(txcurr, "onOrBefore=${endDate},location=${location}"));
    cd.addSearch(
        "previouslyOnArt",
        EptsReportUtils.map(previouslyOnArt, "endDate=${endDate-6m},location=${location}"));

    cd.setCompositionString("txcurr AND previouslyOnArt");
    return cd;
  }

  /**
   * From getPatientsStartedArtLast6MonthsFromEndDate()
   *
   * <p>Exclude all transferred in patients without ART initiation date (Transferred IN and NOT A):
   * Transferred-in from patient program Table: patient_program Criterias: program_id=2,
   * patient_state=29 and start_date <= reporting end date; OR
   *
   * <p>Transferred-in from Ficha Resumo Encounter Type Id= 53 Criterias: “Transfer from other
   * facility” concept Id 1369 = “Yes” concept id 1065 AND obs_datetime <=reporting endDate AND
   * “Type of Patient Transferred from” concept id 6300 = “ART” concept id 6276 AND Date of
   * MasterCard File Opening (PT”: “Data de Abertura da Ficha na US”) (Concept ID 23891
   * value_datetime) <= reporting endDate Note 1: the both concepts 1369 and 6300 should be recorded
   * in the same encounter the both concepts obs_datetime might be different because in this case
   * its not referring to encounter date_time, but to transferred in date, hence should not be
   * considered as encounter_datetime The system will identify the earliest date from the different
   * sources by the reporting end date as patient ART Start Date and include as New on ART those
   * with ART Start Date falling in the last 6 months from reporting end date
   */
  public CohortDefinition getPatientsNewOnArt() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Patients New on ART (A)");
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition startedArtLast6Months = getPatientsStartedArtLast6MonthsFromEndDate();
    CohortDefinition transferredFromProgram = getPatientsTransferredInFromProgram();
    CohortDefinition transferredFromFichaResumo = getPatientsTransferredInFromFichaResumo();

    cd.addSearch(
        "startedArtLast6Months",
        EptsReportUtils.map(
            startedArtLast6Months,
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "transferredFromProgram",
        EptsReportUtils.map(transferredFromProgram, "endDate=${endDate},location=${location}"));
    cd.addSearch(
        "transferredFromFichaResumo",
        EptsReportUtils.map(transferredFromFichaResumo, "endDate=${endDate},location=${location}"));

    cd.setCompositionString("startedArtLast6Months");
    return cd;
  }

  public CohortDefinition patientsPreviouslyOnARTNumerator() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Patients PreviouslyOnARTNumerator");
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    CohortDefinition NUM = txtbCohortQueries.txTbNumerator();

    cd.addSearch(
        "TXTB-NUM",
        EptsReportUtils.map(NUM, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "started-before-start-reporting-period",
        EptsReportUtils.map(
            getPatientsOnArtBeforeEndDate(), "endDate=${endDate},location=${location}"));
    cd.setCompositionString("TXTB-NUM AND started-before-start-reporting-period");

    return cd;
  }

  /**
   * Select all patients have a clinical consultation (encounter type 6) in the last 6 months from
   * the end date For the 6 months’ period, consider: Start Date = reporting end date – 6 months End
   * Date = reporting end date
   */
  public CohortDefinition getPatientsWithClinicalConsultationInLast6Months() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("With clinical consultation in last 6 months ");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND e.encounter_type = ${6} "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate"
            + "GROUP BY p.patient_id";
    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * From getPatientsOnArt() The system will identify the earliest date from the different sources
   * by the reporting end date as patient ART Start Date and include as New on ART those with ART
   * Start Date falling in the last 6 months from reporting end date
   */
  public CohortDefinition getPatientsStartedArtLast6MonthsFromEndDate() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients New on ART  ");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    String patientsOnArtQuery = getPatientsOnArt(true);

    String query =
        "SELECT patient_id "
            + "FROM   ( "
            + patientsOnArtQuery
            + " ) new_art "
            + "WHERE  new_art.art_date BETWEEN :startDate AND :endDate";

    sqlCohortDefinition.setQuery(query);

    return sqlCohortDefinition;
  }

  /**
   * <b>Patients Previously on ART(B)</b> All patients who have their first drugs pick-up date
   * (first encounter_datetime) set in Pharmacy form FILA (Encounter Type ID 18): first occurrence
   * of encounter date_time Encounter Type Ids = 18 And encounter_datetime<=EndDate All patients who
   * have initiated the ARV drugs [ ARV PLAN (Concept ID 1255) = START DRUGS (Concept ID 1256) at
   * the pharmacy or clinical visits (Encounter Type IDs 6,9,18) first occurrence of encounter
   * date_time Encounter Type Ids = 6,9,18 ARV PLAN (Concept Id 1255) = START DRUGS (Concept ID
   * 1256) And encounter_datetime <=EndDate All patients who have the first historical start drugs
   * date (earliest concept ID 1190) set in pharmacy or in clinical forms (Encounter Type IDs 6, 9,
   * 18, 53) earliest “historical start date” Encounter Type Ids = 6,9,18,53 The earliest
   * “Historical Start Date” (Concept Id 1190) And historical start date(Value_datetime) <=EndDate
   * All patients enrolled in ART Program (date_enrolled in program_id 2, from patient program
   * table) program_enrollment date program_id=2 and date_enrolled And date_enrolled <=EndDate All
   * patients with first drugs pick up date (earliest concept ID 23866 value_datetime) set in
   * mastercard pharmacy form “Recepção/Levantou ARV”(Encounter Type ID 52) with Levantou ARV
   * (concept id 23865) = Yes (concept id 1065) earliest “Date of Pick up” Encounter Type Ids = 52
   * The earliest “Data de Levantamento” (Concept Id 23866 value_datetime) <= endDate Levantou ARV
   * (concept id 23865) = SIm (1065) <b>Exclude all patients from A</b>
   */
  public CohortDefinition getPatientsPreviouslyOnArt() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Patients Previously on ART (B)");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Date.class));

    CohortDefinition onArtBeforeEndDate = getPatientsOnArtBeforeEndDate();
    cd.addSearch(
        "onArtBeforeEndDate",
        EptsReportUtils.map(onArtBeforeEndDate, "endDate=${endDate},location=${location}"));
    cd.setCompositionString("onArtBeforeEndDate");
    return cd;
  }

  /**
   * This Cohort Definition implements <b>TB LAM </b> indicators For more details check the java doc
   * of each method on the composition
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPetientsHaveTBLAM() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("TB LAM");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition haveTbLamTestResultOrRequestOrResult =
        getPatientsHaveTBLAMTestRequestOrResult();

    cd.addSearch(
        "haveTbLamTestResultOrRequestOrResult",
        EptsReportUtils.map(
            haveTbLamTestResultOrRequestOrResult,
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "MWRD",
        EptsReportUtils.map(
            txtbCohortQueries.getmWRD(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "Smear",
        EptsReportUtils.map(
            txtbCohortQueries.getSmearMicroscopyOnly(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("haveTbLamTestResultOrRequestOrResult AND NOT (MWRD OR Smear)");
    return cd;
  }
  /**
   * This Cohort Definition implements <b>Other (No GeneXpert) </b> indicators For more details
   * check the java doc of each method on the composition
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsInOthersWithoutGenexPert(Concept positiveOrNegative) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Other (No GeneXpert)");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition others = txtbCohortQueries.getAdditionalTest();
    CohortDefinition othersPositive = getPatientsWithCultureTestResultPositive(positiveOrNegative);

    cd.addSearch(
        "others",
        EptsReportUtils.map(
            others, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "othersPositive",
        EptsReportUtils.map(
            othersPositive, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("(others AND othersPositive)");

    return cd;
  }

  /**
   * Transferred-in from patient program Table: patient_program Criterias: program_id=2,
   * patient_state=29 and start_date <= reporting end date
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsTransferredInFromProgram() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Transferred-in from patient program");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put(
        "29",
        hivMetadata
            .getTransferredFromOtherHealthFacilityWorkflowState()
            .getProgramWorkflowStateId());

    String query =
        ""
            + "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN patient_program pg "
            + "               ON pg.patient_id = p.patient_id "
            + "       INNER JOIN patient_state ps "
            + "               ON ps.patient_program_id = pg.patient_program_id "
            + "WHERE  p.voided = 0 "
            + "       AND pg.voided = 0 "
            + "       AND ps.voided = 0 "
            + "       AND pg.program_id = ${2} "
            + "       AND pg.location_id = :location "
            + "       AND ps.state = ${29} "
            + "       AND ps.start_date <= :endDate ";

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));
    return sqlCohortDefinition;
  }
  /**
   * Transferred-in from Ficha Resumo Encounter Type Id= 53 Criterias: “Transfer from other
   * facility” concept Id 1369 = “Yes” concept id 1065 AND obs_datetime <=reporting endDate AND
   * “Type of Patient Transferred from” concept id 6300 = “ART” concept id 6276 AND Date of
   * MasterCard File Opening (PT”: “Data de Abertura da Ficha na US”) (Concept ID 23891
   * value_datetime) <= reporting endDate Note 1: the both concepts 1369 and 6300 should be recorded
   * in the same encounter the both concepts obs_datetime might be different because in this case
   * its not referring to encounter date_time, but to transferred in date, hence should not be
   * considered as encounter_datetime
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsTransferredInFromFichaResumo() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Transferred-in from patient program");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1369", hivMetadata.getTransferredFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "       INNER JOIN obs o2 "
            + "               ON o2.encounter_id = e.encounter_id "
            + "       INNER JOIN obs o3 "
            + "               ON o3.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND o2.voided = 0 "
            + "       AND o3.voided = 0 "
            + "       AND e.encounter_type = ${53} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${1369} "
            + "       AND o.value_coded = ${1065} "
            + "       AND o.obs_datetime <= :endDate "
            + "       AND o2.concept_id = ${6300} "
            + "       AND o2.value_coded = ${6276} "
            + "       AND o3.concept_id = ${23891} "
            + "       AND o3.value_datetime <= :endDate "
            + "GROUP  BY patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));
    return sqlCohortDefinition;
  }

  public CohortDefinition getPatientsOnArtBeforeEndDate() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients Previously on ART - Before End Date");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    String patientsOnArt = getPatientsOnArt(false);
    sqlCohortDefinition.setQuery(patientsOnArt);
    return sqlCohortDefinition;
  }

  /**
   * have a ‘GeneXpert Positivo ’ registered in the investigações – resultados laboratoriais - ficha
   * clínica – mastercard: * Encounter Type ID = 6 ● TB GENEXPERT TEST (concept id 23723) value
   * coded Positive (concept id 703) ● encounter_datetime >= startDate and <=endDate; or ■ have a
   * ‘GeneXpert Negativo ’ registered in the investigações – resultados laboratoriais - ficha
   * clínica – mastercard or ■ Have a GeneXpert request registered in the investigacoes –ficha
   * clinica – mastercard; ● Encounter Type ID = 6 ● APPLICATION FOR LABORATORY RESEARCH (concept id
   * 23722) value coded TB GENEXPERT TEST (concept id 23723) ● encounter_datetime >= startDate and
   * <=endDate; or ■ have a GENEXPERT Result registered in the laboratory form ● Encounter Type ID =
   * 13 ● Teste TB GENEXPERT (concept id 23723) Value_coded (concept id 664 – Negative, concept id
   * 703 – Positive) ● encounter_datetime >= startDate and <=endDate ■ have a XpertMTB Result
   * registered in the laboratory form ● Encounter Type ID = 13 ● Teste XpertMTB (concept id 165189)
   * Value_coded (concept id 1065 – yes, concept id 1066 – no) ● encounter_datetime >= startDate and
   *
   * @return
   */
  public CohortDefinition getPatientsGeneXpertMtbRif(Concept positiveOrNegativeResult) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("GeneXpert MTB/RIF");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("23723", tbMetadata.getTBGenexpertTestConcept().getConceptId());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("703", tbMetadata.getPositiveConcept().getConceptId());
    map.put("664", tbMetadata.getNegativeConcept().getConceptId());
    map.put("165189", tbMetadata.getTestXpertMtbUuidConcept().getConceptId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("positiveOrNegativeResult", positiveOrNegativeResult.getConceptId());

    String valueCodedForLab = "";

    if (positiveOrNegativeResult != null && positiveOrNegativeResult.getConceptId() == 703) {
      valueCodedForLab = " ${1065} ";
    } else if (positiveOrNegativeResult != null && positiveOrNegativeResult.getConceptId() == 664) {
      valueCodedForLab = " ${1066} ";
    }

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${6} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${23723} "
            + "       AND o.value_coded = ${positiveOrNegativeResult} "
            + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "GROUP  BY p.patient_id "
            + "UNION "
            + "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${13} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${23723} "
            + "       AND o.value_coded =  ${positiveOrNegativeResult} "
            + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "GROUP  BY p.patient_id "
            + "UNION "
            + "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${13} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${165189} "
            + "       AND o.value_coded = "
            + valueCodedForLab
            + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));
    return sqlCohortDefinition;
  }

  /**
   * Have a TB LAM request registered in the investigacoes –ficha clinica – mastercard; ● Encounter
   * Type ID = 6 ● APPLICATION FOR LABORATORY RESEARCH (concept id 23722) value coded TB LAM(concept
   * id 23951) ● encounter_datetime >= startDate <=endDate; or ■ Have a TB LAM Test result
   * registered in the investigacoes – resultados laboratoriais ficha clínica – mastercard ●
   * Encounter Type ID = 6 ● TB LAM TEST (concept id 23951) value coded Positive (concept id 703) or
   * Negative (concept id 664) ● encounter_datetime >= startDate and <=endDate; ■ have a TB LAM
   * Result registered in the laboratory form ● Encounter Type ID = 13 ● TB LAM TEST (concept id
   * 23951) value coded Positive (concept id 703) or Negative (concept id 664) or Indeterminado
   * (concept id 1138) ● encounter_datetime >= startDate and <=endDate
   *
   * @return
   */
  public CohortDefinition getPatientsHaveTBLAMTestRequestOrResult() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("GeneXpert MTB/RIF");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("23723", tbMetadata.getTBGenexpertTestConcept().getConceptId());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("703", tbMetadata.getPositiveConcept().getConceptId());
    map.put("664", tbMetadata.getNegativeConcept().getConceptId());
    map.put("23951", tbMetadata.getTestTBLAM().getConceptId());
    map.put("1138", tbMetadata.getIndeterminate().getConceptId());
    map.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${6} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${23722} "
            + "       AND o.value_coded = ${23951} "
            + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "GROUP  BY p.patient_id "
            + "UNION "
            + "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${6} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${23951} "
            + "       AND o.value_coded IN (${703}, ${664}, ${1138} ) "
            + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "GROUP  BY p.patient_id "
            + "UNION "
            + "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type IN (${13},${51}) "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${23951} "
            + "       AND o.value_coded IN ( ${703}, ${664}, ${1138} ) "
            + "       AND DATE(e.encounter_datetime) BETWEEN :startDate AND :endDate "
            + "GROUP  BY p.patient_id";
    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));
    return sqlCohortDefinition;
  }

  /**
   * <b>TB LAM: patients will be included who during the reporting period:</b>
   * <li>have Investigações - Pedidos Laboratoriais request marked for ‘TB LAM’ in Ficha Clínica –
   *     Mastercard or
   * <li>have Investigações - Resultados Laboratoriais results (ANY RESULT) recorded for ‘TB LAM’ in
   *     Ficha Clínica – Mastercard or
   * <li>have a TB LAM result ANY VALUE registered in the Laboratory Form
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsHaveTBLAMpositiveOrNegativeResult(List<Concept> results) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "TB LAM: patients will be included who during the reporting period");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, String> map = getStringMap(results);

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${6} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${23951} "
            + "       AND o.value_coded IN (${results} ) "
            + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "GROUP  BY p.patient_id "
            + "UNION "
            + "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type IN (${13}, ${51}) "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${23951} "
            + "       AND o.value_coded IN ( ${results} ) "
            + "       AND DATE(e.encounter_datetime) BETWEEN :startDate AND :endDate "
            + "GROUP  BY p.patient_id";
    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));
    return sqlCohortDefinition;
  }

  private Map<String, String> getStringMap(List<Concept> results) {
    List<Integer> answerIds = new ArrayList<>();

    for (Concept concept : results) {
      answerIds.add(concept.getConceptId());
    }

    Map<String, String> map = new HashMap<>();
    map.put(
        "23722", String.valueOf(hivMetadata.getApplicationForLaboratoryResearch().getConceptId()));
    map.put(
        "6", String.valueOf(hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId()));
    map.put(
        "13", String.valueOf(hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId()));
    map.put("51", String.valueOf(hivMetadata.getFsrEncounterType().getEncounterTypeId()));
    map.put("23951", String.valueOf(tbMetadata.getTestTBLAM().getConceptId()));
    map.put("results", StringUtils.join(answerIds, ','));
    return map;
  }

  /**
   * Patients New on ART as follows (A): All patients who have their first drugs pick-up date (first
   * encounter_datetime) set in Pharmacy form FILA (Encounter Type ID 18): first occurrence of
   * encounter date_time Encounter Type Ids = 18 And encounter_datetime<=EndDate All patients who
   * have initiated the ARV drugs [ ARV PLAN (Concept ID 1255) = START DRUGS (Concept ID 1256) at
   * the pharmacy or clinical visits (Encounter Type IDs 6,9,18) first occurrence of encounter
   * date_time Encounter Type Ids = 6,9,18 ARV PLAN (Concept Id 1255) = START DRUGS (Concept ID
   * 1256) And encounter_datetime <=EndDate All patients who have the first historical start drugs
   * date (earliest concept ID 1190) set in pharmacy or in clinical forms (Encounter Type IDs 6, 9,
   * 18, 53) earliest “historical start date” Encounter Type Ids = 6,9,18,53 The earliest
   * “Historical Start Date” (Concept Id 1190) And historical start date(Value_datetime) <=EndDate
   * All patients enrolled in ART Program (date_enrolled in program_id 2, from patient program
   * table) program_enrollment date program_id=2 and date_enrolled And date_enrolled <=EndDate All
   * patients with first drugs pick up date (earliest concept ID 23866 value_datetime) set in
   * mastercard pharmacy form “Recepção/Levantou ARV”(Encounter Type ID 52) with Levantou ARV
   * (concept id 23865) = Yes (concept id 1065) earliest “Date of Pick up” Encounter Type Ids = 52
   * The earliest “Data de Levantamento” (Concept Id 23866 value_datetime) <= endDate Levantou ARV
   * (concept id 23865) = SIm (1065)
   *
   * @return String
   */
  private String getPatientsOnArt(boolean selectArtDate) {
    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());
    valuesMap.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    valuesMap.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    valuesMap.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    valuesMap.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    valuesMap.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    valuesMap.put("1255", hivMetadata.getARVPlanConcept().getConceptId());
    valuesMap.put("1410", hivMetadata.getReturnVisitDateConcept().getConceptId());
    valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    valuesMap.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());
    valuesMap.put("1256", hivMetadata.getStartDrugs().getConceptId());
    valuesMap.put("2", hivMetadata.getARTProgram().getProgramId());

    String fromSql =
        "        FROM   (SELECT p.patient_id, "
            + "                       Min(e.encounter_datetime) art_date "
            + "                FROM   patient p "
            + "                       INNER JOIN encounter e "
            + "                               ON e.patient_id = p.patient_id "
            + "                WHERE  e.encounter_type = ${18} "
            + "                       AND e.encounter_datetime <= :endDate "
            + "                       AND e.voided = 0 "
            + "                       AND p.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                GROUP  BY p.patient_id "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       Min(e.encounter_datetime) art_date "
            + "                FROM   patient p "
            + "                       INNER JOIN encounter e "
            + "                               ON p.patient_id = e.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON e.encounter_id = o.encounter_id "
            + "                WHERE  p.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND e.encounter_type IN ( ${6}, ${9}, ${18} ) "
            + "                       AND o.concept_id = ${1255} "
            + "                       AND o.value_coded = ${1256} "
            + "                       AND e.encounter_datetime <= :endDate "
            + "                       AND e.location_id = :location "
            + "                GROUP  BY p.patient_id "
            + "                UNION "
            + "                SELECT p.patient_id, Min(o.value_datetime) min_date "
            + "                                  FROM   patient p "
            + "                                         INNER JOIN encounter e "
            + "                                                 ON e.patient_id = p.patient_id "
            + "                                         INNER JOIN obs o "
            + "                                                 ON o.encounter_id = "
            + "                                                    e.encounter_id "
            + "                                  WHERE  e.encounter_type IN( ${6}, ${9}, ${18}, ${53} ) "
            + "                                         AND o.concept_id = ${1190} "
            + "                                         AND e.location_id = :location "
            + "                                         AND o.value_datetime <= :endDate "
            + "                                         AND o.voided = 0 "
            + "                                         AND e.voided = 0 "
            + "                                         AND p.voided = 0 "
            + "                                  GROUP  BY p.patient_id "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       MIN(pg.date_enrolled) AS art_date "
            + "                FROM   patient p "
            + "                       INNER JOIN patient_program pg "
            + "                               ON p.patient_id = pg.patient_id "
            + "                       INNER JOIN patient_state ps "
            + "                               ON pg.patient_program_id = ps.patient_program_id "
            + "                WHERE  pg.location_id = :location "
            + "                       AND pg.voided = 0 "
            + "                       AND pg.program_id = ${2} "
            + "                       AND pg.date_enrolled <= :endDate "
            + "                GROUP  BY p.patient_id "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       Min(o.value_datetime) AS art_date "
            + "                FROM   patient p "
            + "                       INNER JOIN encounter e "
            + "                               ON e.patient_id = p.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON o.encounter_id = e.encounter_id "
            + "                WHERE  e.encounter_type = ${52} "
            + "                       AND o.concept_id = ${23866} "
            + "                       AND o.value_datetime <= :endDate "
            + "                       AND o.voided = 0 "
            + "                       AND e.location_id = :location "
            + "                       AND e.voided = 0 "
            + "                       AND p.voided = 0 "
            + "                GROUP  BY p.patient_id) art "
            + "        GROUP  BY art.patient_id";
    String query =
        selectArtDate
            ? "SELECT art.patient_id, Min(art.art_date) art_date ".concat(fromSql)
            : "SELECT art.patient_id ".concat(fromSql);

    StringSubstitutor sb = new StringSubstitutor(valuesMap);

    return sb.replace(query);
  }

  public CohortDefinition getPatientsWithCultureTestResultPositive(Concept positiveOrNegative) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients with Culture test Result positive");
    sqlCohortDefinition.addParameters(getParameters());

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("23774", tbMetadata.getCultureTest().getConceptId());
    map.put("positiveOrNegative", positiveOrNegative.getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${6} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${23774} "
            + "       AND o.value_coded =${positiveOrNegative} "
            + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "GROUP  BY p.patient_id "
            + "UNION "
            + "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${13} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${23774} "
            + "       AND o.value_coded = ${positiveOrNegative} "
            + "       AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "GROUP  BY p.patient_id";
    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));
    return sqlCohortDefinition;
  }

  /**
   * <b>Get patients who have a Basiloscopia Negative registered</b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getSmearMicroscopyOnlyNegative() {
    CompositionCohortDefinition definition = new CompositionCohortDefinition();
    definition.setName("haveBasiloscopia()");
    definition.addParameter(new Parameter("startDate", "startDate", Date.class));
    definition.addParameter(new Parameter("endDate", "endDate", Date.class));
    definition.addParameter(new Parameter("location", "location", Location.class));

    String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";

    CohortDefinition basiloscopiaCohort =
        genericCohortQueries.generalSql(
            "basiloscopiaCohort",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                hivMetadata.getAdultoSeguimentoEncounterType(),
                hivMetadata.getResultForBasiloscopia(),
                Collections.singletonList(tbMetadata.getNegativeConcept())));

    CohortDefinition basiloscopiaLabCohort =
        genericCohortQueries.generalSql(
            "basiloscopiaLabCohort",
            genericCohortQueries.getPatientsWithObsBetweenDates(
                hivMetadata.getMisauLaboratorioEncounterType(),
                hivMetadata.getResultForBasiloscopia(),
                Arrays.asList(
                    tbMetadata.getNegativeConcept(), tbMetadata.getNotFoundTestResultConcept())));

    definition.addSearch("basiloscopiaCohort", EptsReportUtils.map(basiloscopiaCohort, mappings));
    definition.addSearch(
        "basiloscopiaLabCohort", EptsReportUtils.map(basiloscopiaLabCohort, mappings));

    definition.setCompositionString("basiloscopiaCohort OR basiloscopiaLabCohort");

    return definition;
  }

  public enum TxCurrComposition {
    TXCURR {
      @Override
      public String getKey() {
        return "TXCURR";
      }

      @Override
      public String getCompositionString() {
        return getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    CLINICAL {
      @Override
      public String getKey() {
        return "CLINICAL";
      }

      @Override
      public String getCompositionString() {
        return getKey();
      }

      @Override
      public String getName() {
        return "TX_CURR with clinical consultation in last 6 months ";
      }
    },

    NEWART {
      @Override
      public String getKey() {
        return "NEWART";
      }

      @Override
      public String getCompositionString() {
        return getKey();
      }

      @Override
      public String getName() {
        return "TX_CURR with clinical consultation in last 6 months ";
      }
    },

    PREVIOUSLYART {
      @Override
      public String getKey() {
        return "PREVIOUSLYART";
      }

      @Override
      public String getCompositionString() {
        return getKey();
      }

      @Override
      public String getName() {
        return " PREVIOUSLY ON ART ";
      }
    },
    TXCURR_AND_CLINICAL {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return TXCURR.getKey() + " AND " + CLINICAL.getKey();
      }

      @Override
      public String getName() {
        return "TX_CURR with clinical consultation in last 6 months ";
      }
    },
    TXCURR_AND_CLINICAL_AND_NEWART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return TXCURR.getKey() + " AND " + CLINICAL.getKey() + " AND " + NEWART.getKey();
      }

      @Override
      public String getName() {
        return "TX_CURR with clinical consultation in last 6 months ";
      };
    },

    TXCURR_AND_CLINICAL_AND_PREVIUSLYART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return TXCURR.getKey() + " AND " + CLINICAL.getKey() + " AND " + PREVIOUSLYART.getKey();
      }

      @Override
      public String getName() {
        return " TXCURR AND CLINICAL AND PREVIOUSLY ON ART ";
      }
    };

    public abstract String getKey();

    public abstract String getCompositionString();

    public abstract String getName();
  }

  public enum TxTbComposition {
    TXTB_DENOMINATOR {
      @Override
      public String getKey() {
        return "TXTB";
      }

      @Override
      public String getCompositionString() {
        return getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TXTB";
      }
    },
    CLINICAL {
      @Override
      public String getKey() {
        return "CLINICAL";
      }

      @Override
      public String getCompositionString() {
        return getKey();
      }

      @Override
      public String getName() {
        return "TX_CURR with clinical consultation in last 6 months ";
      }
    },

    NEWART {
      @Override
      public String getKey() {
        return "NEWART";
      }

      @Override
      public String getCompositionString() {
        return getKey();
      }

      @Override
      public String getName() {
        return "TX_CURR with clinical consultation in last 6 months ";
      }
    },

    POSITIVESCREENING {
      @Override
      public String getKey() {
        return "POSITIVESCREENING";
      }

      @Override
      public String getCompositionString() {
        return TXTB_DENOMINATOR.getKey() + " AND " + getKey();
      }

      @Override
      public String getName() {
        return "POSITIVESCREENING ";
      }
    },

    NEGATIVESCREENING {
      @Override
      public String getKey() {
        return "NEGATIVESCREENING";
      }

      @Override
      public String getCompositionString() {
        return TXTB_DENOMINATOR.getKey() + " AND NOT " + POSITIVESCREENING.getKey();
      }

      @Override
      public String getName() {
        return "TX TB NEGATIVESCREENING ";
      }
    },
    NUMERATOR {
      @Override
      public String getKey() {
        return "NUMERATOR";
      }

      @Override
      public String getCompositionString() {
        return getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from NUMERATOR ";
      }
    },
    NEGATIVESCREENING_AND_NEWART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return NEGATIVESCREENING.getCompositionString() + " AND " + NEWART.getKey();
      }

      @Override
      public String getName() {
        return "TX TB NEGATIVESCREENING ";
      }
    },
    NUMERATOR_AND_NEWART {
      @Override
      public String getKey() {
        return "NUMERATOR";
      }

      @Override
      public String getCompositionString() {
        return NUMERATOR.getKey() + " AND " + NEWART.getKey();
      }

      @Override
      public String getName() {
        return "TX TB NEGATIVESCREENING ";
      }
    },

    NUMERATOR_AND_TXCURR_AND_NEWART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return NUMERATOR.getKey()
            + " AND "
            + TxCurrComposition.TXCURR.getKey()
            + " AND "
            + NEWART.getKey();
      }

      @Override
      public String getName() {
        return "TX TB NEGATIVESCREENING ";
      }
    },

    NUMERATOR_AND_PREVIOUSLYART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return NUMERATOR.getKey() + " AND " + PREVIOUSLYART.getKey();
      }

      @Override
      public String getName() {
        return "TX TB PREVIOUSLYART ";
      }
    },
    NUMERATOR_AND_TXCURR {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return NUMERATOR.getKey() + " AND " + TxCurrComposition.TXCURR.getKey();
      }

      @Override
      public String getName() {
        return "TX TB PREVIOUSLYART ";
      }
    },
    NUMERATOR_AND_TXCURR_PREVIOUSLYART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return NUMERATOR.getKey()
            + " AND "
            + TxCurrComposition.TXCURR.getKey()
            + " AND "
            + PREVIOUSLYART.getKey();
      }

      @Override
      public String getName() {
        return "TX TB PREVIOUSLYART ";
      }
    },

    POSITIVESCREENING_AND_NEWART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return POSITIVESCREENING.getCompositionString() + " AND " + NEWART.getKey();
      }

      @Override
      public String getName() {
        return "TX TB POSITIVESCREENING ";
      }
    },
    POSITIVESCREENING_AND_PREVIOUSLYRT {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return POSITIVESCREENING.getCompositionString() + " AND " + PREVIOUSLYART.getKey();
      }

      @Override
      public String getName() {
        return "TX TB PREVIOUSLY ART POSITIVESCREENING ";
      }
    },
    NEGATIVESCREENING_AND_PREVIOUSLYRT {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return NEGATIVESCREENING.getCompositionString() + " AND " + PREVIOUSLYART.getKey();
      }

      @Override
      public String getName() {
        return "TX TB PREVIOUSLY ART NEGATIVESCREENING ";
      }
    },

    PREVIOUSLYART {
      @Override
      public String getKey() {
        return "PREVIOUSLYART";
      }

      @Override
      public String getCompositionString() {
        return getKey();
      }

      @Override
      public String getName() {
        return " PREVIOUSLY ON ART ";
      }
    },
    TXTB_AND_CLINICAL {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return TXTB_DENOMINATOR.getKey() + " AND " + CLINICAL.getKey();
      }

      @Override
      public String getName() {
        return "TXTB with clinical consultation in last 6 months ";
      }
    },
    TXTB_AND_PREVIOUSLYART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return TXTB_DENOMINATOR.getKey() + " AND " + PREVIOUSLYART.getKey();
      }

      @Override
      public String getName() {
        return "TXTB with clinical consultation in last 6 months ";
      }
    },

    TXTB_AND_NEWART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return TXTB_DENOMINATOR.getKey() + " AND " + NEWART.getKey();
      }

      @Override
      public String getName() {
        return "TXTB New ART ";
      }
    },
    TXTB_AND_TXCURR {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return TXTB_DENOMINATOR.getKey() + " AND " + TxCurrComposition.TXCURR.getKey();
      }

      @Override
      public String getName() {
        return "TXTB New ART ";
      }
    },
    TXTB_AND_CLINICAL_AND_NEWART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return TXTB_DENOMINATOR.getKey() + " AND " + CLINICAL.getKey() + " AND " + NEWART.getKey();
      }

      @Override
      public String getName() {
        return "TXTB with clinical consultation in last 6 months ";
      };
    },
    TXTB_AND_TXCURR_AND_NEWART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return TXTB_DENOMINATOR.getKey()
            + " AND "
            + TxCurrComposition.TXCURR.getKey()
            + " AND "
            + NEWART.getKey();
      }

      @Override
      public String getName() {
        return "TXTB with clinical consultation in last 6 months ";
      };
    },
    TXTB_AND_TXCURR_AND_PREVIOUSLYART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return TXTB_DENOMINATOR.getKey()
            + " AND "
            + TxCurrComposition.TXCURR.getKey()
            + " AND "
            + PREVIOUSLYART.getKey();
      }

      @Override
      public String getName() {
        return "TXTB with clinical consultation in last 6 months ";
      };
    },

    TXCURR_AND_CLINICAL_AND_PREVIUSLYART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return TXTB_DENOMINATOR.getKey()
            + " AND "
            + CLINICAL.getKey()
            + " AND "
            + PREVIOUSLYART.getKey();
      }

      @Override
      public String getName() {
        return " TXTB AND CLINICAL AND PREVIOUSLY ON ART ";
      }
    };

    public abstract String getKey();

    public abstract String getCompositionString();

    public abstract String getName();
  }

  public enum SemearTbLamGXPertComposition {
    SEMEAR {
      @Override
      public String getKey() {
        return "SEMEAR";
      }

      @Override
      public String getCompositionString() {
        return getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },

    SIXA {
      @Override
      public String getKey() {
        return "SIXA";
      }

      @Override
      public String getCompositionString() {
        return getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    MWRD {
      @Override
      public String getKey() {
        return "MWRD";
      }

      @Override
      public String getCompositionString() {
        return getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },

    TBLAM {
      @Override
      public String getKey() {
        return "TBLAM";
      }

      @Override
      public String getCompositionString() {
        return getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    FIVE {
      @Override
      public String getKey() {
        return "FIVE";
      }

      @Override
      public String getCompositionString() {
        return getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    OTHER {
      @Override
      public String getKey() {
        return "OTHER";
      }

      @Override
      public String getCompositionString() {
        return getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    FIVE_AND_OTHER {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return OTHER.getKey() + " AND " + FIVE.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    FIVE_AND_OTHER_AND_NEWART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return FIVE.getKey() + " AND " + OTHER.getKey() + " AND " + NEWART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    FIVE_AND_OTHER_AND_PREVIOUSLYART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return FIVE.getKey() + " AND " + OTHER.getKey() + " AND " + PREVIOUSLYART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    FIVE_AND_SEMEAR {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SEMEAR.getKey() + " AND " + FIVE.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    FIVE_AND_SEMEAR_AND_NEWART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return FIVE.getKey() + " AND " + SEMEAR.getKey() + " AND " + NEWART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    FIVE_AND_SEMEAR_AND_PREVIOUSLYART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return FIVE.getKey() + " AND " + SEMEAR.getKey() + " AND " + PREVIOUSLYART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    FIVE_AND_MWRD {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return MWRD.getKey() + "  AND " + FIVE.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    FIVE_AND_MWRD_AND_NEWART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return FIVE.getKey() + " AND " + MWRD.getKey() + " AND " + NEWART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    FIVE_AND_MWRD_AND_PREVIOUSLYART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return FIVE.getKey() + " AND " + MWRD.getKey() + " AND " + PREVIOUSLYART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    FIVE_AND_TBLAM {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return FIVE.getKey() + " AND " + TBLAM.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    FIVE_AND_TBLAM_AND_NEWART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return FIVE.getKey() + " AND " + TBLAM.getKey() + " AND " + NEWART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    FIVE_AND_TBLAM_AND_PREVIOUSLYART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return FIVE.getKey() + " AND " + TBLAM.getKey() + " AND " + PREVIOUSLYART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SIXA_AND_OTHER {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXA.getKey() + " AND " + OTHER.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SIXA_AND_OTHER_AND_NEWART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXA.getKey() + " AND " + OTHER.getKey() + " AND " + NEWART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SIXA_AND_OTHER_AND_PREVIOUSLYART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXA.getKey() + " AND " + OTHER.getKey() + " AND " + PREVIOUSLYART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SIXA_AND_SEMEAR {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SEMEAR.getKey() + " AND " + SIXA.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SIXA_AND_SEMEAR_AND_NEWART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXA.getKey() + " AND " + SEMEAR.getKey() + " AND " + NEWART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SIXA_AND_SEMEAR_AND_PREVIOUSLYART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXA.getKey() + " AND " + SEMEAR.getKey() + " AND " + PREVIOUSLYART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SIXA_AND_MWRD {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return MWRD.getKey() + " AND " + SIXA.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SIXA_AND_MWRD_AND_NEWART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXA.getKey() + " AND " + MWRD.getKey() + " AND " + NEWART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SIXA_AND_MWRD_AND_PREVIOUSLYART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXA.getKey() + " AND " + MWRD.getKey() + " AND " + PREVIOUSLYART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SIXA_AND_TBLAM {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXA.getKey() + " AND " + TBLAM.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SIXA_AND_TBLAM_AND_NEWART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXA.getKey() + " AND " + TBLAM.getKey() + " AND " + NEWART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SIXA_AND_TBLAM_AND_PREVIOUSLYART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXA.getKey() + " AND " + TBLAM.getKey() + " AND " + PREVIOUSLYART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SIXB {
      @Override
      public String getKey() {
        return "SIXB";
      }

      @Override
      public String getCompositionString() {
        return getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SIXB_AND_SEMEAR {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXB.getKey() + " AND " + SEMEAR.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SIXB_AND_SEMEAR_AND_NEWART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXB.getKey() + " AND " + SEMEAR.getKey() + " AND " + NEWART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SIXB_AND_SEMEAR_AND_PREVIOUSLYART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXB.getKey() + " AND " + SEMEAR.getKey() + " AND " + PREVIOUSLYART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SIXB_AND_MWRD {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXB.getKey() + " AND " + MWRD.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SIXB_AND_MWRD_AND_NEWART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXB.getKey() + " AND " + MWRD.getKey() + " AND " + NEWART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SIXB_AND_MWRD_AND_PREVIOUSLYART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXB.getKey() + " AND " + MWRD.getKey() + " AND " + PREVIOUSLYART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SIXB_AND_TBLAM {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXB.getKey() + " AND " + TBLAM.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SIXB_AND_TBLAM_AND_NEWART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXB.getKey() + " AND " + TBLAM.getKey() + " AND " + NEWART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SIXB_AND_TBLAM_AND_PREVIOUSLYART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXB.getKey() + " AND " + TBLAM.getKey() + " AND " + PREVIOUSLYART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SIXB_AND_OTHER {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXB.getKey() + " AND " + OTHER.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SIXB_AND_OTHER_AND_NEWART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXB.getKey() + " AND " + OTHER.getKey() + " AND " + NEWART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SIXB_AND_OTHER_AND_PREVIOUSLYART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXB.getKey() + " AND " + OTHER.getKey() + " AND " + PREVIOUSLYART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SEVEN {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXA.getKey() + " AND " + TxTbComposition.NUMERATOR.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SEVEN_AND_SEMEAR {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXA.getKey()
            + " AND "
            + TxTbComposition.NUMERATOR.getKey()
            + " AND "
            + SEMEAR.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SEVEN_AND_SEMEAR_AND_NEWART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXA.getKey()
            + " AND "
            + TxTbComposition.NUMERATOR.getKey()
            + " AND "
            + SEMEAR.getKey()
            + " AND "
            + NEWART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SEVEN_AND_SEMEAR_AND_PREVIOUSLYART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXA.getKey()
            + " AND "
            + TxTbComposition.NUMERATOR.getKey()
            + " AND "
            + SEMEAR.getKey()
            + " AND "
            + PREVIOUSLYART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SEVEN_AND_MWRD {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXA.getKey()
            + " AND "
            + TxTbComposition.NUMERATOR.getKey()
            + " AND "
            + MWRD.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SEVEN_AND_MWRD_AND_NEWART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXA.getKey()
            + " AND "
            + TxTbComposition.NUMERATOR.getKey()
            + " AND "
            + MWRD.getKey()
            + " AND "
            + NEWART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SEVEN_AND_MWRD_AND_PREVIOUSLYART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXA.getKey()
            + " AND "
            + TxTbComposition.NUMERATOR.getKey()
            + " AND "
            + MWRD.getKey()
            + " AND "
            + PREVIOUSLYART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SEVEN_AND_TBLAM {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXA.getKey()
            + " AND "
            + TxTbComposition.NUMERATOR.getKey()
            + " AND "
            + TBLAM.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SEVEN_AND_TBLAM_AND_NEWART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXA.getKey()
            + " AND "
            + TxTbComposition.NUMERATOR.getKey()
            + " AND "
            + TBLAM.getKey()
            + " AND "
            + NEWART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SEVEN_AND_TBLAM_AND_PREVIOUSLYART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXA.getKey()
            + " AND "
            + TxTbComposition.NUMERATOR.getKey()
            + " AND "
            + TBLAM.getKey()
            + " AND "
            + PREVIOUSLYART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SEVEN_AND_OTHER {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXA.getKey()
            + " AND "
            + TxTbComposition.NUMERATOR.getKey()
            + " AND "
            + OTHER.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SEVEN_AND_OTHER_AND_NEWART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXA.getKey()
            + " AND "
            + TxTbComposition.NUMERATOR.getKey()
            + " AND "
            + OTHER.getKey()
            + " AND "
            + NEWART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    SEVEN_AND_OTHER_AND_PREVIOUSLYART {
      @Override
      public String getKey() {
        return "";
      }

      @Override
      public String getCompositionString() {
        return SIXA.getKey()
            + " AND "
            + TxTbComposition.NUMERATOR.getKey()
            + " AND "
            + OTHER.getKey()
            + " AND "
            + PREVIOUSLYART.getKey();
      }

      @Override
      public String getName() {
        return "Select all patients from TX CURR";
      }
    },
    ;

    public abstract String getKey();

    public abstract String getCompositionString();

    public abstract String getName();
  }

  private List<Parameter> getParameters() {
    return Arrays.asList(
        new Parameter("startDate", "startDate", Date.class),
        new Parameter("endDate", "endDate", Date.class),
        new Parameter("location", "location", Date.class));
  }
}
