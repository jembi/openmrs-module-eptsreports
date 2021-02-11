package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Date;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.calculation.cxcascrn.CXCASCRNCalculationAA;
import org.openmrs.module.eptsreports.reporting.calculation.cxcascrn.CXCASCRNCalculationBB;
import org.openmrs.module.eptsreports.reporting.cohort.definition.CalculationCohortDefinition;
import org.openmrs.module.eptsreports.reporting.library.queries.CXCASCRNQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CXCASCRNCohortQueries {

  private TxCurrCohortQueries txCurrCohortQueries;

  private GenderCohortQueries genderCohortQueries;

  private AgeCohortQueries ageCohortQueries;

  private HivMetadata hivMetadata;

  @Autowired
  public CXCASCRNCohortQueries(
      TxCurrCohortQueries txCurrCohortQueries,
      GenderCohortQueries genderCohortQueries,
      AgeCohortQueries ageCohortQueries,
      HivMetadata hivMetadata) {
    this.txCurrCohortQueries = txCurrCohortQueries;
    this.genderCohortQueries = genderCohortQueries;
    this.ageCohortQueries = ageCohortQueries;
    this.hivMetadata = hivMetadata;
  }

  public enum CXCASCRNResult {
    POSITIVE,
    NEGATIVE,
    ANY,
    SUSPECTED
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
  private CohortDefinition getAA() {
    CXCASCRNCalculationAA cxcascrnCalculation =
        Context.getRegisteredComponents(CXCASCRNCalculationAA.class).get(0);

    CalculationCohortDefinition cd = new CalculationCohortDefinition();
    cd.setName("AA from CXCA SCRN");
    cd.setCalculation(cxcascrnCalculation);
    cd.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    cd.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    return cd;
  }

  private CohortDefinition getAA1OrAA2(CXCASCRNResult cxcascrnResult, boolean isAA1, boolean max) {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    if (isAA1) {
      cd.setName("AA1 from CXCA SCRN");
    } else {
      cd.setName("AA2 from CXCA SCRN");
    }

    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    cd.setQuery(
        CXCASCRNQueries.getAA1OrAA2Query(
            cxcascrnResult,
            isAA1,
            max,
            hivMetadata.getRastreioDoCancroDoColoUterinoEncounterType().getEncounterTypeId(),
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getResultadoViaConcept().getConceptId(),
            hivMetadata.getNegative().getConceptId(),
            hivMetadata.getPositive().getConceptId(),
            hivMetadata.getSuspectedCancerConcept().getConceptId()));

    return cd;
  }

  public CohortDefinition getAA3OrAA4(CXCASCRNResult cxcascrnResult) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    if (cxcascrnResult == CXCASCRNResult.NEGATIVE) {
      cd.setName("AA3 from CXCA SCRN");
    }
    if (cxcascrnResult == CXCASCRNResult.POSITIVE) {
      cd.setName("AA4 from CXCA SCRN");
    }
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition aa1 = getAA1OrAA2(cxcascrnResult, true, false);
    CohortDefinition aa2 = getAA1OrAA2(cxcascrnResult, false, false);

    cd.addSearch("AA1", EptsReportUtils.map(aa1, "startDate=${startDate},location=${location}"));
    cd.addSearch("AA2", EptsReportUtils.map(aa2, "startDate=${startDate},location=${location}"));

    cd.setCompositionString("AA1 OR AA2");

    return cd;
  }

  private CohortDefinition getBB() {
    CXCASCRNCalculationBB cxcascrnCalculation =
        Context.getRegisteredComponents(CXCASCRNCalculationBB.class).get(0);

    CalculationCohortDefinition cd = new CalculationCohortDefinition();
    cd.setName("BB from CXCA SCRN");
    cd.setCalculation(cxcascrnCalculation);
    cd.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    cd.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    return cd;
  }

  public CohortDefinition get1stTimeScreened() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition a = getA();
    CohortDefinition aa = getAA();
    CohortDefinition aa1 = getAA1OrAA2(CXCASCRNResult.ANY, true, false);
    CohortDefinition aa2 = getAA1OrAA2(CXCASCRNResult.ANY, false, false);

    cd.addSearch(
        "A",
        EptsReportUtils.map(a, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "AA",
        EptsReportUtils.map(
            aa, "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));
    cd.addSearch("AA1", EptsReportUtils.map(aa1, "startDate=${startDate},location=${location}"));
    cd.addSearch("AA2", EptsReportUtils.map(aa2, "startDate=${startDate},location=${location}"));

    cd.setCompositionString("A AND AA AND NOT (AA1 OR AA2)");
    return cd;
  }

  public CohortDefinition getRescreenedAfterPreviousNegative() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition a = getA();
    CohortDefinition aa = getAA();
    CohortDefinition aa3 = getAA3OrAA4(CXCASCRNResult.NEGATIVE);

    cd.addSearch(
        "A",
        EptsReportUtils.map(a, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "AA",
        EptsReportUtils.map(
            aa, "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));
    cd.addSearch("AA3", EptsReportUtils.map(aa3, "startDate=${startDate},location=${location}"));

    cd.setCompositionString("A AND AA AND AA3");
    return cd;
  }

  public CohortDefinition getPostTreatmentFollowUp() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition a = getA();
    CohortDefinition aa = getAA();
    CohortDefinition aa4 = getAA3OrAA4(CXCASCRNResult.POSITIVE);
    CohortDefinition bb = getBB();

    cd.addSearch(
        "A",
        EptsReportUtils.map(a, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "AA",
        EptsReportUtils.map(
            aa, "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));
    cd.addSearch("AA4", EptsReportUtils.map(aa4, "startDate=${startDate},location=${location}"));
    cd.addSearch(
        "BB",
        EptsReportUtils.map(
            bb, "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));

    cd.setCompositionString("A AND AA AND AA4 AND BB");

    return cd;
  }

  public CohortDefinition getRescreenedAfterPreviousPositive() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition a = getA();
    CohortDefinition aa = getAA();
    CohortDefinition fitstTimeScreened = get1stTimeScreened();
    CohortDefinition rescreenedAfterPreviousNegative = getRescreenedAfterPreviousNegative();
    CohortDefinition postTreatmentFollowUp = getPostTreatmentFollowUp();

    cd.addSearch(
        "A",
        EptsReportUtils.map(a, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "AA",
        EptsReportUtils.map(
            aa, "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));
    cd.addSearch(
        "postTreatmentFollowUp",
        EptsReportUtils.map(
            fitstTimeScreened, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "rescreenedAfterPreviousNegative",
        EptsReportUtils.map(
            rescreenedAfterPreviousNegative,
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "postTreatmentFollowUp",
        EptsReportUtils.map(
            postTreatmentFollowUp,
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString(
        "A AND AA AND NOT postTreatmentFollowUp AND NOT rescreenedAfterPreviousNegative AND NOT postTreatmentFollowUp ");
    return cd;
  }

  public CohortDefinition getPositive() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition a = getA();
    CohortDefinition aa = getAA();
    CohortDefinition positive = null;

    cd.addSearch(
        "A",
        EptsReportUtils.map(a, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "AA",
        EptsReportUtils.map(
            aa, "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));
    cd.addSearch(
        "POSITIVE",
        EptsReportUtils.map(
            positive, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("A AND AA AND POSITIVE");

    return cd;
  }

  public CohortDefinition getNegative() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition a = getA();
    CohortDefinition aa = getAA();
    CohortDefinition negative = null;

    cd.addSearch(
        "A",
        EptsReportUtils.map(a, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "AA",
        EptsReportUtils.map(
            aa, "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));
    cd.addSearch(
        "NEGATIVE",
        EptsReportUtils.map(
            negative, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("A AND AA AND NEGATIVE");

    return cd;
  }

  public CohortDefinition getSupected() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition a = getA();
    CohortDefinition aa = getAA();
    CohortDefinition supected = null;

    cd.addSearch(
        "A",
        EptsReportUtils.map(a, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "AA",
        EptsReportUtils.map(
            aa, "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));
    cd.addSearch(
        "SUSPECTED",
        EptsReportUtils.map(
            supected, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("A AND AA AND SUSPECTED");

    return cd;
  }
}
