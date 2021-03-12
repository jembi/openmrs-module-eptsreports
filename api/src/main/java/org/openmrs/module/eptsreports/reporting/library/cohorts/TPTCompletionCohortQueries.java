package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Date;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TPTCompletionCohortQueries {

  @Autowired private TbPrevCohortQueries tbPrevCohortQueries;

  @Autowired private TxCurrCohortQueries txCurrCohortQueries;

  @Autowired private TXTBCohortQueries txtbCohortQueries;

  private final String mapping = "startDate=${startDate},endDate=${endDate},location=${location}";

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
    compositionCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    compositionCohortDefinition.addSearch(
        "txcurr",
        EptsReportUtils.map(
            txCurrCohortQueries.getTxCurrCompositionCohort("txCurr", true), mapping));

    // validate this queries use startDate
    compositionCohortDefinition.addSearch(
        "startedINH",
        EptsReportUtils.map(
            tbPrevCohortQueries.getPatientsThatStartedProfilaxiaIsoniazidaOnPeriod(), mapping));

    compositionCohortDefinition.addSearch(
        "startedINH2",
        EptsReportUtils.map(tbPrevCohortQueries.getPatientsThatInitiatedProfilaxia(), mapping));

    compositionCohortDefinition.addSearch(
        "startedINH3",
        EptsReportUtils.map(
            tbPrevCohortQueries.getPatientsWhoHaveRegimeTPTWithINHMarkedOnFirstPickUpDateOnFILT(),
            mapping));

    compositionCohortDefinition.addSearch(
        "started3HP",
        EptsReportUtils.map(
            tbPrevCohortQueries.getPatientsWhoHaveOutrasPrescricoesWith3HPMarkedOnFichaClinica(),
            mapping));

    compositionCohortDefinition.addSearch(
        "completedAll",
        EptsReportUtils.map(
            tbPrevCohortQueries.getPatientsThatCompletedIsoniazidProphylacticTreatment(),
            "onOrAfter=${startDate},orOrBefore=${endDate},location=${location}"));

    compositionCohortDefinition.setCompositionString(
        "txcurr AND ((startedINH OR startedINH2 OR startedINH3 OR started3HP) OR completedAll)");

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
    compositionCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    compositionCohortDefinition.addSearch(
        "txcurr",
        EptsReportUtils.map(
            txCurrCohortQueries.getTxCurrCompositionCohort("txCurr", true), mapping));

    // validate this queries use startDate
    compositionCohortDefinition.addSearch(
        "startedINH",
        EptsReportUtils.map(
            tbPrevCohortQueries.getPatientsThatStartedProfilaxiaIsoniazidaOnPeriod(), mapping));

    compositionCohortDefinition.addSearch(
        "startedINH2",
        EptsReportUtils.map(tbPrevCohortQueries.getPatientsThatInitiatedProfilaxia(), mapping));

    compositionCohortDefinition.addSearch(
        "startedINH3",
        EptsReportUtils.map(
            tbPrevCohortQueries.getPatientsWhoHaveRegimeTPTWithINHMarkedOnFirstPickUpDateOnFILT(),
            mapping));

    compositionCohortDefinition.addSearch(
        "started3HP",
        EptsReportUtils.map(
            tbPrevCohortQueries.getPatientsWhoHaveOutrasPrescricoesWith3HPMarkedOnFichaClinica(),
            mapping));

    compositionCohortDefinition.addSearch(
        "completedAll",
        EptsReportUtils.map(
            tbPrevCohortQueries.getPatientsThatCompletedIsoniazidProphylacticTreatment(),
            "onOrAfter=${startDate},orOrBefore=${endDate},location=${location}"));

    compositionCohortDefinition.setCompositionString(
        "txcurr AND NOT ((startedINH OR startedINH2 OR startedINH3 OR started3HP) OR completedAll)");

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
    compositionCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "end Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    String generalParameterMapping =
        "startDate=${endDate-1095d},endDate=${endDate},location=${location}";

    compositionCohortDefinition.addSearch(
        "tpt1", EptsReportUtils.map(getTxCurrWithoutTPTCompletion(), mapping));

    compositionCohortDefinition.addSearch(
        "E", EptsReportUtils.map(txtbCohortQueries.txTbNumerator(), generalParameterMapping));

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
    compositionCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    String generalParameterMapping =
        "startDate=${endDate-14d},endDate=${endDate},location=${location}";

    compositionCohortDefinition.addSearch(
        "tpt1", EptsReportUtils.map(getTxCurrWithoutTPTCompletion(), mapping));

    compositionCohortDefinition.addSearch(
        "F", EptsReportUtils.map(txtbCohortQueries.getDenominator(), generalParameterMapping));

    compositionCohortDefinition.setCompositionString("tpt1 AND F");

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
    compositionCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    compositionCohortDefinition.addSearch(
        "tpt1", EptsReportUtils.map(getTxCurrWithoutTPTCompletion(), mapping));

    compositionCohortDefinition.addSearch(
          "tpt2", EptsReportUtils.map(getTxCurrWithoutTPTCompletionWithTB(), mapping));
    
    compositionCohortDefinition.addSearch(
            "tpt3", EptsReportUtils.map(getTxCurrWithoutTPTCompletionWithPositiveTBScreening(), mapping));

    compositionCohortDefinition.setCompositionString("tpt1 AND (tpt2 OR tpt3)");

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
    compositionCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

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
    compositionCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    return compositionCohortDefinition;
  }
}
