package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Date;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.reporting.calculation.cxcascrn.CXCASCRNCalculation;
import org.openmrs.module.eptsreports.reporting.cohort.definition.CalculationCohortDefinition;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CXCASCRNCohortQueries {

  private TxCurrCohortQueries txCurrCohortQueries;

  private GenderCohortQueries genderCohortQueries;

  private AgeCohortQueries ageCohortQueries;

  @Autowired
  public CXCASCRNCohortQueries(
      TxCurrCohortQueries txCurrCohortQueries,
      GenderCohortQueries genderCohortQueries,
      AgeCohortQueries ageCohortQueries) {
    this.txCurrCohortQueries = txCurrCohortQueries;
    this.genderCohortQueries = genderCohortQueries;
    this.ageCohortQueries = ageCohortQueries;
  }

  /**
   * A: Select all patients from Tx_curr by end of reporting period and who are female and Age >= 15
   * years
   */
  private CohortDefinition getA() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("A from  CXCA SCRN");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition txcurr = this.txCurrCohortQueries.getTxCurrCompositionCohort("txcurr", true);

    CohortDefinition female = this.genderCohortQueries.femaleCohort();

    CohortDefinition adults = this.ageCohortQueries.createXtoYAgeCohort("adullts", 15, null);

    cd.addSearch(
        "txcurr", EptsReportUtils.map(txcurr, "onOrBefore=${endDate},location=${location}"));

    cd.addSearch("female", EptsReportUtils.map(female, ""));

    cd.addSearch("adults", EptsReportUtils.map(adults, ""));

    cd.setCompositionString("txcurr AND female AND adults");

    return cd;
  }

  /**
   *
   *
   * <ul>
   *   <li>AA:
   *       <ul>
   *         <li>( A_FichaCCU: ( Select all patients with the first Ficha de Registo Para Rastreio
   *             do Cancro do Colo Uterino (encounter type 28) with the following conditions:
   *             <ul>
   *               <li>VIA RESULTS (concept id 2094) and value coded SUSPECTED CANCER, NEGATIVE or
   *                   POSITIVE (concept id IN [2093, 664, 703])
   *               <li>And encounter datetime >= startdate and <= enddate
   *               <li>Note1: if there is more than one record registered, consider the first one,
   *                   i.e., the earliest date during the reporting period
   *             </ul>
   *         <li>If Ficha de Registo para rastreio do CCU(encounter type 28) is not available during
   *             the period the system will consider the following sources:
   *             <ul>
   *               <li>A_FichaClinca: Select all patients with the ficha clinica (encounter type 6)
   *                   with the following conditions:
   *                   <ul>
   *                     <li>VIA RESULTS (concept id 2094) and value coded SUSPECTED CANCER,
   *                         NEGATIVE or POSITIVE (concept id IN [2093, 664, 703])
   *                     <li>And encounter datetime >= startdate and <= enddate
   *                   </ul>
   *               <li>A_FichaResumo: Select all patients on Ficha Resumo (encounter type 53) who
   *                   have the following conditions:
   *                   <ul>
   *                     <li>VIA RESULTS (concept id 2094) and value coded POSITIVE (concept id 703)
   *                     <li>And value datetime >= startdate and <= enddate )
   *                   </ul>
   *               <li>Note2: The system will consider the earliest VIA date during the reporting
   *                   period from the different sources listed above (from encounter type 6 and
   *                   53).
   *             </ul>
   *       </ul>
   * </ul>
   */
  public CohortDefinition getAA() {
    CXCASCRNCalculation cxcascrnCalculation =
        Context.getRegisteredComponents(CXCASCRNCalculation.class).get(0);

    CalculationCohortDefinition cd = new CalculationCohortDefinition();
    cd.setName("AA from  CXCA SCRN");
    cd.setCalculation(cxcascrnCalculation);
    cd.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    cd.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    return cd;
  }
}
