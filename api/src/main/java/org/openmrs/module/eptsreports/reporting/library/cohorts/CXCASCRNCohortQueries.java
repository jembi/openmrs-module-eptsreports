package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.*;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.calculation.cxcascrn.CXCASCRNAACalculation;
import org.openmrs.module.eptsreports.reporting.calculation.cxcascrn.CXCASCRNBBCalculation;
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
    SUSPECTED,
    ALL
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
   * <b>SCRN_FR4</b>Patient with a screening test for Cervical cancer
   *
   * <ul>
   *   <li>Have Resultado VIA with Positivo, Negativo or Suspeita de Cancro result marked on Ficha
   *       de Registo Individual: Rastreio dos Cancros do Colo do Útero e da Mama during the
   *       reporting period or
   *   <li>Have Resultado do Rastreio HPV-DNA Negativo marked on Ficha de Registo Individual:
   *       Rastreio dos Cancros do Colo do Útero e da Mama during the reporting period.
   * </ul>
   *
   * <p><b>Note:</b>Patients with registration of tTreatment during the reporting period, but
   * without VIA Result during the reporting period will not be included in CXCA_SCRN numerator.
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWithScreeningTestForCervicalCancer(
      CXCASCRNCohortQueries.CXCASCRNResult cxcascrnResult, boolean beforeStartDate) {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patients with Screening Result for Cervical Cancer");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("28", hivMetadata.getRastreioDoCancroDoColoUterinoEncounterType().getEncounterTypeId());
    map.put("2094", hivMetadata.getResultadoViaConcept().getConceptId());
    map.put("664", hivMetadata.getNegative().getConceptId());
    map.put("703", hivMetadata.getPositive().getConceptId());
    map.put("2093", hivMetadata.getSuspectedCancerConcept().getConceptId());
    map.put("165436", hivMetadata.getHumanPapillomavirusDnaConcept().getConceptId());

    StringBuilder query =
        new StringBuilder(
            "SELECT final.patient_id "
                + "FROM   ( "
                + "           SELECT   via_or_hpv.patient_id, "
                + "                    via_or_hpv.screening_date AS the_screening_date "
                + "           FROM     ( "
                + "                        SELECT     p.patient_id, "
                + "                                   e.encounter_datetime AS screening_date "
                + "                        FROM       patient p "
                + "                                       INNER JOIN encounter e "
                + "                                                  ON         e.patient_id = p.patient_id "
                + "                                       INNER JOIN obs o "
                + "                                                  ON         o.encounter_id = e.encounter_id "
                + "                        WHERE      p.voided = 0 "
                + "                          AND        e.voided = 0 "
                + "                          AND        o.voided = 0 "
                + "                          AND        e.encounter_type = ${28} "
                + "                          AND        o.concept_id = ${2094} ");
    if (cxcascrnResult == CXCASCRNCohortQueries.CXCASCRNResult.NEGATIVE) {
      query.append("    AND o.value_coded = ${664} ");
    } else if (cxcascrnResult == CXCASCRNCohortQueries.CXCASCRNResult.POSITIVE) {
      query.append("    AND o.value_coded = ${703} ");
    } else if (cxcascrnResult == CXCASCRNCohortQueries.CXCASCRNResult.SUSPECTED) {
      query.append("    AND o.value_coded = ${2093} ");
    } else if (cxcascrnResult == CXCASCRNCohortQueries.CXCASCRNResult.ANY) {
      query.append("    AND o.value_coded IN (${2093}, ${664}, ${703}) ");
    }
    query.append("                          AND        e.encounter_datetime  ");
    query.append(beforeStartDate ? " < :startDate " : "  BETWEEN :startDate AND  :endDate ");
    query.append(
        "                          AND        e.location_id = :location "
            + "                        GROUP BY   p.patient_id "
            + "                        UNION "
            + "                        SELECT     p.patient_id, "
            + "                                   e.encounter_datetime AS screening_date "
            + "                        FROM       patient p "
            + "                                       INNER JOIN encounter e "
            + "                                                  ON         p.patient_id = e.patient_id "
            + "                                       INNER JOIN obs o "
            + "                                                  ON         e.encounter_id = o.encounter_id "
            + "                        WHERE      p.voided = 0 "
            + "                          AND        e.voided = 0 "
            + "                          AND        o.voided = 0 "
            + "                          AND        e.encounter_type = ${28} "
            + "                          AND        o.concept_id = ${165436} "
            + "                          AND        o.value_coded = ${664} "
            + "                          AND        e.encounter_datetime ");
    query.append(beforeStartDate ? " < :startDate " : "  BETWEEN :startDate AND  :endDate ");
    query.append(
        "                          AND        e.location_id = :location "
            + "                        GROUP BY   p.patient_id "
            + "   ) via_or_hpv "
            + "   GROUP BY via_or_hpv.patient_id ) final");

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    cd.setQuery(stringSubstitutor.replace(query.toString()));

    return cd;
  }

  /**
   * <b>SCRN_FR4</b> Patient with a screening test for Cervical cancer
   *
   * <p>The system will identify female patients who received at least one screening test for
   * cervical cancer during the reporting period as follows:
   *
   * <ul>
   *   <li>Have Resultado VIA with Positivo, Negativo or Suspeita de Cancro result marked on Ficha
   *       de Registo Individual: Rastreio dos Cancros do Colo do UteroÚtero e da Mama during the
   *       reporting period or
   *   <li>Have Resultado do Rastreio HPV-DNA Negativo marked on Ficha de Registo Individual:
   *       Rastreio dos Cancros do Colo do Útero e da Mama during the reporting period.
   * </ul>
   *
   * <p><b>Note:</b> Patients with registration of tTreatment during the reporting period, but
   * without VIA Result during the reporting period will not be included in CXCA_SCRN numerator.
   */
  public CohortDefinition getPatientsWithScreeningTestForCervicalCancer(boolean beforeStartDate) {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patients with Screening Result for Cervical Cancer");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("28", hivMetadata.getRastreioDoCancroDoColoUterinoEncounterType().getEncounterTypeId());
    map.put("2094", hivMetadata.getResultadoViaConcept().getConceptId());
    map.put("664", hivMetadata.getNegative().getConceptId());
    map.put("703", hivMetadata.getPositive().getConceptId());
    map.put("2093", hivMetadata.getSuspectedCancerConcept().getConceptId());
    map.put("165436", hivMetadata.getHumanPapillomavirusDnaConcept().getConceptId());

    String query =
        " SELECT     p.patient_id "
            + "      FROM       patient p "
            + "                     INNER JOIN encounter e "
            + "                                ON         e.patient_id = p.patient_id "
            + "                     INNER JOIN obs o "
            + "                                ON         o.encounter_id = e.encounter_id "
            + "                     INNER JOIN obs o2 "
            + "                                ON         o2.encounter_id = e.encounter_id "
            + "      WHERE      p.voided = 0 "
            + "        AND        e.voided = 0 "
            + "        AND        o.voided = 0 "
            + "        AND        o2.voided = 0 "
            + "        AND        e.encounter_type = ${28} "
            + "        AND        ( ( o.concept_id = ${2094} "
            + "        AND            o.value_coded IN (${2093}, ${664}, ${703}) ) "
            + "         OR          (  o2.concept_id = ${165436} "
            + "        AND             o2.value_coded = ${664} ) )  "
            + "        AND        e.location_id = :location "
            + "        AND        e.encounter_datetime  ";
    query += beforeStartDate ? " < :startDate " : "  BETWEEN :startDate AND  :endDate ";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   * <b>SCRN_FR9</b> Numerator Disaggregation: Result
   *
   * <ul>
   *   <li>Negative
   *       <ul>
   *         <li>All screened patients (SCRN_FR4) who have been marked as VIA Negativo on Resultado
   *             VIA OR have Resultado do Rastreio HPV-DNA marked as Negativo
   *       </ul>
   * </ul>
   *
   * <p><b>Note:</b> If there is more than one HPV result or VIA result registered during the
   * reporting period, the system will consider the most recent result.<br>
   * <br>
   *
   * <p><b>Note:</b> If there are discrepant results registered on the same (most recent) day, the
   * system will consider the following hierarchy:
   *
   * <ul>
   *   <li>Positive
   *   <li>Suspected Cancer
   *   <li>Negative
   * </ul>
   */
  public CohortDefinition getPatientsWithNegativeResultForScreeningTest(boolean beforeStartDate) {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Disagregation - Patients with Negative Screening Result");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("28", hivMetadata.getRastreioDoCancroDoColoUterinoEncounterType().getEncounterTypeId());
    map.put("2094", hivMetadata.getResultadoViaConcept().getConceptId());
    map.put("664", hivMetadata.getNegative().getConceptId());
    map.put("703", hivMetadata.getPositive().getConceptId());
    map.put("2093", hivMetadata.getSuspectedCancerConcept().getConceptId());
    map.put("165436", hivMetadata.getHumanPapillomavirusDnaConcept().getConceptId());

    String query =
        "SELECT patient_id "
            + "FROM   (SELECT negative.patient_id, "
            + "               Max(negative.negative_result_date) AS last_negative_result_Date "
            + "        FROM   (SELECT p.patient_id, "
            + "                       e.encounter_datetime AS negative_result_date "
            + "                FROM   patient p "
            + "                           INNER JOIN encounter e "
            + "                                      ON e.patient_id = p.patient_id "
            + "                           INNER JOIN obs o "
            + "                                      ON o.encounter_id = e.encounter_id "
            + "                WHERE  p.voided = 0 "
            + "                  AND e.voided = 0 "
            + "                  AND o.voided = 0 "
            + "                  AND e.encounter_type = ${28} "
            + "                  AND o.concept_id IN ( ${2094}, ${165436} ) "
            + "                  AND o.value_coded = ${664} "
            + "                  AND e.location_id = :location "
            + "                  AND e.encounter_datetime ";
    query += beforeStartDate ? " < :startDate " : "  BETWEEN :startDate AND  :endDate ";
    query +=
        "                GROUP  BY p.patient_id) AS negative "
            + "        GROUP  BY patient_id) max_negative "
            + "WHERE  NOT EXISTS (SELECT e.patient_id "
            + "                   FROM   encounter e "
            + "                              INNER JOIN obs o "
            + "                                         ON o.encounter_id = e.encounter_id "
            + "                   WHERE  e.voided = 0 "
            + "                     AND o.voided = 0 "
            + "                     AND e.encounter_type = ${28} "
            + "                     AND o.concept_id = ${2094} "
            + "                     AND o.value_coded IN ( ${703}, ${2093} ) "
            + "                     AND e.location_id = :location "
            + "                     AND max_negative.patient_id = e.patient_id "
            + "                     AND e.encounter_datetime >= "
            + "                         max_negative.last_negative_result_Date "
            + "                     AND e.encounter_datetime ";
    query += beforeStartDate ? " < :startDate ) " : " <= :endDate ) ";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   * <b>SCRN_FR9</b> Numerator Disaggregation: Result
   *
   * <ul>
   *   <li>Suspected Cancer
   *       <ul>
   *         <li>All screened patients (SCRN_FR4) who have been marked as Suspeita de Cancro on
   *             Resultado VIA
   *       </ul>
   * </ul>
   *
   * <p><b>Note:</b> If there are discrepant results registered on the same (most recent) day, the
   * system will consider the following hierarchy:
   *
   * <ul>
   *   <li>Positive
   *   <li>Suspected Cancer
   *   <li>Negative
   * </ul>
   */
  public CohortDefinition getPatientsWithSuspectedCancerResultForScreeningTest() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Disagregation - Patients with Suspected Cancer Result");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("28", hivMetadata.getRastreioDoCancroDoColoUterinoEncounterType().getEncounterTypeId());
    map.put("2094", hivMetadata.getResultadoViaConcept().getConceptId());
    map.put("664", hivMetadata.getNegative().getConceptId());
    map.put("703", hivMetadata.getPositive().getConceptId());
    map.put("2093", hivMetadata.getSuspectedCancerConcept().getConceptId());
    map.put("165436", hivMetadata.getHumanPapillomavirusDnaConcept().getConceptId());

    String query =
        "SELECT patient_id "
            + "FROM   (SELECT suspected.patient_id, "
            + "               Max(suspected.suspected_result_date) AS last_suspected_result_Date "
            + "        FROM   (SELECT p.patient_id, "
            + "                       e.encounter_datetime AS suspected_result_date "
            + "                FROM   patient p "
            + "                           INNER JOIN encounter e "
            + "                                      ON e.patient_id = p.patient_id "
            + "                           INNER JOIN obs o "
            + "                                      ON o.encounter_id = e.encounter_id "
            + "                WHERE  p.voided = 0 "
            + "                  AND e.voided = 0 "
            + "                  AND o.voided = 0 "
            + "                  AND e.encounter_type = ${28} "
            + "                  AND o.concept_id = ${2094}  "
            + "                  AND o.value_coded = ${2093} "
            + "                  AND e.location_id = :location "
            + "                  AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "                GROUP  BY p.patient_id) AS suspected "
            + "        GROUP  BY patient_id) max_suspected "
            + "WHERE  NOT EXISTS (SELECT e.patient_id "
            + "                   FROM   encounter e "
            + "                              INNER JOIN obs o "
            + "                                         ON o.encounter_id = e.encounter_id "
            + "                   WHERE  e.voided = 0 "
            + "                     AND o.voided = 0 "
            + "                     AND e.encounter_type = ${28} "
            + "                     AND o.concept_id = ${2094} "
            + "                     AND o.value_coded = ${703} "
            + "                     AND e.location_id = :location "
            + "                     AND max_suspected.patient_id = e.patient_id "
            + "                     AND e.encounter_datetime >= "
            + "                         max_suspected.last_suspected_result_Date "
            + "                     AND e.encounter_datetime <= :endDate)";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   * <b>SCRN_FR9</b> Numerator Disaggregation: Result
   *
   * <ul>
   *   <li>Positive (CXCA_SCRN_POS – SCRN_FR11)
   *       <ul>
   *         <li>All screened patients (SCRN_FR4) who have been marked as VIA Positivo on Resultado
   *             VIA
   *       </ul>
   * </ul>
   */
  public CohortDefinition getPatientsWithPositiveResultForScreeningTest() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Disagregation - Patients with Positive Result For Screening Test");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("28", hivMetadata.getRastreioDoCancroDoColoUterinoEncounterType().getEncounterTypeId());
    map.put("2094", hivMetadata.getResultadoViaConcept().getConceptId());
    map.put("703", hivMetadata.getPositive().getConceptId());

    String query =
        "SELECT positivo.patient_id "
            + "        FROM   (SELECT p.patient_id, "
            + "                       Max(e.encounter_datetime) AS last_positivo_result_date "
            + "                FROM   patient p "
            + "                           INNER JOIN encounter e "
            + "                                      ON e.patient_id = p.patient_id "
            + "                           INNER JOIN obs o "
            + "                                      ON o.encounter_id = e.encounter_id "
            + "                WHERE  p.voided = 0 "
            + "                  AND e.voided = 0 "
            + "                  AND o.voided = 0 "
            + "                  AND e.encounter_type = ${28} "
            + "                  AND o.concept_id = ${2094}  "
            + "                  AND o.value_coded = ${703} "
            + "                  AND e.location_id = :location "
            + "                  AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "                GROUP  BY p.patient_id) positivo ";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

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
  private CohortDefinition getAA(CXCASCRNResult result) {
    CXCASCRNAACalculation cxcascrnCalculation =
        Context.getRegisteredComponents(CXCASCRNAACalculation.class).get(0);

    CalculationCohortDefinition cd = new CalculationCohortDefinition();
    cd.setName("AA from CXCA SCRN");
    cd.setCalculation(cxcascrnCalculation);
    cd.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    cd.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addCalculationParameter("result", result);

    return cd;
  }

  public CohortDefinition getAA1OrAA2(CXCASCRNResult cxcascrnResult, boolean isAA1, boolean max) {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    if (isAA1) {
      cd.setName("AA1 from CXCA SCRN");
    } else {
      cd.setName("AA2 from CXCA SCRN");
    }

    cd.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    cd.setQuery(
        CXCASCRNQueries.getAA1OrAA2Query(
            cxcascrnResult,
            isAA1,
            max,
            hivMetadata.getRastreioDoCancroDoColoUterinoEncounterType().getEncounterTypeId(),
            hivMetadata.getResultadoViaConcept().getConceptId(),
            hivMetadata.getNegative().getConceptId(),
            hivMetadata.getPositive().getConceptId(),
            hivMetadata.getSuspectedCancerConcept().getConceptId()));

    return cd;
  }

  private CohortDefinition getBB(CXCASCRNResult result) {
    CXCASCRNBBCalculation cxcascrnCalculation =
        Context.getRegisteredComponents(CXCASCRNBBCalculation.class).get(0);

    CalculationCohortDefinition cd = new CalculationCohortDefinition();
    cd.setName("BB from CXCA SCRN");
    cd.setCalculation(cxcascrnCalculation);
    cd.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    cd.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addCalculationParameter("result", result);

    return cd;
  }

  public CohortDefinition get1stTimeScreened(CXCASCRNResult cxcascrnResult) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("1st Time Screened");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition a = getA();
    CohortDefinition aa = getPatientsWithScreeningTestForCervicalCancer(cxcascrnResult, false);
    CohortDefinition aa1 = getPatientsWithScreeningTestForCervicalCancer(cxcascrnResult, true);

    cd.addSearch(
        "A",
        EptsReportUtils.map(a, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "AA",
        EptsReportUtils.map(aa, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch("AA1", EptsReportUtils.map(aa1, "startDate=${startDate},location=${location}"));

    cd.setCompositionString("(A AND AA) AND NOT AA1");
    return cd;
  }

  public CohortDefinition get1stTimeScreenedPatients() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("1st Time Screened");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition a = getTotal();
    CohortDefinition aa1 = getPatientsWithScreeningTestForCervicalCancer(true);

    cd.addSearch(
        "A",
        EptsReportUtils.map(a, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch("AA1", EptsReportUtils.map(aa1, "startDate=${startDate},location=${location}"));

    cd.setCompositionString("A AND NOT AA1");
    return cd;
  }

  public CohortDefinition get1stTimeScreenedPatientsWithPositiveResult() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("1st Time Screened With Positive Result");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition firstTimeScreened = get1stTimeScreenedPatients();
    CohortDefinition positiveResult = getPatientsWithPositiveResultForScreeningTest();

    cd.addSearch(
        "firstTimeScreened",
        EptsReportUtils.map(
            firstTimeScreened, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "positiveResult",
        EptsReportUtils.map(
            positiveResult, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("firstTimeScreened AND positiveResult");
    return cd;
  }

  public CohortDefinition get1stTimeScreenedPatientsWithNegativeResult() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("1st Time Screened With Negative Result");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition firstTimeScreened = get1stTimeScreenedPatients();
    CohortDefinition negativeResult = getPatientsWithNegativeResultForScreeningTest(false);

    cd.addSearch(
        "firstTimeScreened",
        EptsReportUtils.map(
            firstTimeScreened, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "negativeResult",
        EptsReportUtils.map(
            negativeResult, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("firstTimeScreened AND negativeResult");
    return cd;
  }

  public CohortDefinition get1stTimeScreenedPatientsWithSuspectedCancerResult() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("1st Time Screened With Suspected Cancer Result");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition firstTimeScreened = get1stTimeScreenedPatients();
    CohortDefinition suspectedResult = getPatientsWithSuspectedCancerResultForScreeningTest();

    cd.addSearch(
        "firstTimeScreened",
        EptsReportUtils.map(
            firstTimeScreened, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "suspectedResult",
        EptsReportUtils.map(
            suspectedResult, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("firstTimeScreened AND suspectedResult");
    return cd;
  }

  public CohortDefinition getRescreenedAfterPreviousNegative(CXCASCRNResult cxcascrnResult) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Rescreened After Previous Negative");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition a = getA();
    CohortDefinition aa = getAA(cxcascrnResult);
    CohortDefinition aa3 = getAA3OrAA4(CXCASCRNResult.NEGATIVE);

    cd.addSearch(
        "A",
        EptsReportUtils.map(a, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "AA",
        EptsReportUtils.map(
            aa, "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));
    cd.addSearch("AA3", EptsReportUtils.map(aa3, "onOrAfter=${startDate},location=${location}"));

    cd.setCompositionString("A AND AA AND AA3");
    return cd;
  }

  public CohortDefinition
      getPatientsRescreenedAfterPreviousNegativeWithPositiveResutOnReportingPeriod() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Rescreened After Previous Negative And With Positive Result on Reporting Period");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition rescreenedAfterPreviousNegative = getPatentsRescreenedAfterPreviousNegative();
    CohortDefinition positiveResult = getPatientsWithPositiveResultForScreeningTest();

    cd.addSearch(
        "rescreenedAfterPreviousNegative",
        EptsReportUtils.map(
            rescreenedAfterPreviousNegative,
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "positiveResult",
        EptsReportUtils.map(
            positiveResult, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("rescreenedAfterPreviousNegative AND positiveResult");
    return cd;
  }

  public CohortDefinition
      getPatientsRescreenedAfterPreviousNegativeWithSuspectedCancerResutOnReportingPeriod() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "Rescreened After Previous Negative And With Suspected Cancer Result on Reporting Period");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition rescreenedAfterPreviousNegative = getPatentsRescreenedAfterPreviousNegative();
    CohortDefinition suspectedCancerResult = getPatientsWithSuspectedCancerResultForScreeningTest();

    cd.addSearch(
        "rescreenedAfterPreviousNegative",
        EptsReportUtils.map(
            rescreenedAfterPreviousNegative,
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "suspectedCancerResult",
        EptsReportUtils.map(
            suspectedCancerResult,
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("rescreenedAfterPreviousNegative AND suspectedCancerResult");
    return cd;
  }

  public CohortDefinition
      getPatientsRescreenedAfterPreviousNegativeWithNegativeResutOnReportingPeriod() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Rescreened After Previous Negative And With Negative Result on Reporting Period");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition rescreenedAfterPreviousNegative = getPatentsRescreenedAfterPreviousNegative();
    CohortDefinition negativeResult = getPatientsWithNegativeResultForScreeningTest(false);

    cd.addSearch(
        "rescreenedAfterPreviousNegative",
        EptsReportUtils.map(
            rescreenedAfterPreviousNegative,
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "negativeResult",
        EptsReportUtils.map(
            negativeResult, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("rescreenedAfterPreviousNegative AND negativeResult");
    return cd;
  }

  public CohortDefinition getPatentsRescreenedAfterPreviousNegative() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Rescreened After Previous Negative");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition a = getTotal();
    CohortDefinition aa = getPatientsWithNegativeResultForScreeningTest(true);

    cd.addSearch(
        "A",
        EptsReportUtils.map(a, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch("AA", EptsReportUtils.map(aa, "startDate=${startDate},location=${location}"));

    cd.setCompositionString("A AND AA");
    return cd;
  }

  public CohortDefinition getPostTreatmentFollowUp(CXCASCRNResult cxcascrnResult) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Post Treatment FollowUp");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition a = getA();
    CohortDefinition aa = getAA(cxcascrnResult);
    CohortDefinition aa4 = getAA3OrAA4(CXCASCRNResult.POSITIVE);
    CohortDefinition bb = getBB(cxcascrnResult);

    cd.addSearch(
        "A",
        EptsReportUtils.map(a, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "AA",
        EptsReportUtils.map(
            aa, "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));
    cd.addSearch("AA4", EptsReportUtils.map(aa4, "onOrAfter=${startDate},location=${location}"));
    cd.addSearch(
        "BB",
        EptsReportUtils.map(
            bb, "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));

    cd.setCompositionString("A AND AA AND AA4 AND BB");

    return cd;
  }

  /**
   * <b>SCRN_FR7</b> Post-Treatment follow-up
   *
   * <p>The system will identify patients who are Post-Treatment follow-up as follows:
   *
   * <ul>
   *   <li>All screened patients (SCRN_FR4) who have the last Resultado VIA registered and marked as
   *       Positive (use the criteria defined on SCRN_FR9) before the reporting period start date
   *       AND
   *   <li>With one of the following variables registered on the Ficha de Registo Individual:
   *       Rastreio dos Cancros do Colo do Utero e da Mama between the last Resultado VIA (marked as
   *       Positive) before reporting period start date and before the tmost recent VIA screening
   *       (SCRN_FR4) during the reporting period :
   *       <ul>
   *         <li>Tratamento Feito = Crioterapia or Termoablação with Data do Tratamento
   *         <li>Qual foi o tratamento/avaliação no HdR = Crioterapia Feita or Termocoagulação Feita
   *             or Leep or Conização Feita marked in a Ficha CCU registered
   *       </ul>
   * </ul>
   */
  public CohortDefinition getPatientsWitPostTratmentFollowUp() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Disagregation - Post Treatment Follow Up");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("28", hivMetadata.getRastreioDoCancroDoColoUterinoEncounterType().getEncounterTypeId());
    map.put("2094", hivMetadata.getResultadoViaConcept().getConceptId());
    map.put("703", hivMetadata.getPositive().getConceptId());
    map.put("1185", hivMetadata.getTreatmentConcept().getConceptId());
    map.put("2149", hivMetadata.getViaResultOnTheReferenceConcept().getConceptId());
    map.put("23974", hivMetadata.getCryotherapyConcept().getConceptId());
    map.put("165439", hivMetadata.getTermoablationConcept().getConceptId());
    map.put("23972", hivMetadata.getThermocoagulationConcept().getConceptId());
    map.put("23970", hivMetadata.getLeepConcept().getConceptId());
    map.put("23973", hivMetadata.getconizationConcept().getConceptId());

    String query =
        "SELECT final.patient_id "
            + "FROM   (SELECT p.patient_id, "
            + "               most_recent.recent_datetime AS recent_datetime "
            + "        FROM   patient p "
            + "                   INNER JOIN encounter e "
            + "                              ON e.patient_id = p.patient_id "
            + "                   INNER JOIN obs o "
            + "                              ON o.encounter_id = e.encounter_id "
            + "                   INNER JOIN (SELECT recent.patient_id, "
            + "                                      Max(recent.x_datetime) AS recent_datetime "
            + "                               FROM   (SELECT p.patient_id, "
            + "                                              Max(e.encounter_datetime) AS "
            + "                                                  x_datetime "
            + "                                       FROM   patient p "
            + "                                                  INNER JOIN encounter e "
            + "                                                             ON e.patient_id = p.patient_id "
            + "                                                  INNER JOIN obs o "
            + "                                                             ON o.encounter_id = "
            + "                                                                e.encounter_id "
            + "                                       WHERE  p.voided = 0 "
            + "                                         AND e.voided = 0 "
            + "                                         AND o.voided = 0 "
            + "                                         AND e.encounter_datetime < :startDate "
            + "                                         AND e.location_id = :location "
            + "                                         AND e.encounter_type = ${28} "
            + "                                         AND o.concept_id = ${2094} "
            + "                                         AND o.value_coded IS NOT NULL "
            + "                                       GROUP  BY p.patient_id) AS recent "
            + "                               GROUP  BY recent.patient_id) AS most_recent "
            + "                              ON most_recent.patient_id = p.patient_id "
            + "        WHERE  p.voided = 0 "
            + "          AND e.voided = 0 "
            + "          AND o.voided = 0 "
            + "          AND (( e.encounter_type = ${28}  "
            + "            AND e.encounter_datetime = most_recent.recent_datetime )) "
            + "          AND e.location_id = :location "
            + "          AND o.concept_id = ${2094} "
            + "          AND o.value_coded = ${703} "
            + "        GROUP  BY p.patient_id) final "
            + "           INNER JOIN encounter e "
            + "                      ON e.patient_id = final.patient_id "
            + "           INNER JOIN obs o1 "
            + "                      ON o1.encounter_id = e.encounter_id "
            + "           INNER JOIN obs o2 "
            + "                      ON o2.encounter_id = e.encounter_id "
            + "WHERE  e.voided = 0 "
            + "  AND o1.voided = 0 "
            + "  AND o2.voided = 0 "
            + "  AND e.encounter_type = ${28} "
            + "  AND e.location_id = :location "
            + "  AND ( ( o1.concept_id = ${1185} "
            + "    AND o1.value_coded IN ( ${23974}, ${165439} ) "
            + "    AND o1.obs_datetime >= final.recent_datetime ) "
            + "    OR ( o2.concept_id = ${2149} "
            + "        AND o2.value_coded IN ( ${23974}, ${23972}, ${23970}, ${23973} ) "
            + "        AND o2.obs_datetime >= final.recent_datetime ) ) ";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  public CohortDefinition getPatientsWithPostTreatmentFollowUp() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Post Treatment FollowUp");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition a = getTotal();
    CohortDefinition postTratmentFollowUp = getPatientsWitPostTratmentFollowUp();

    cd.addSearch(
        "A",
        EptsReportUtils.map(a, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "postTratmentFollowUp",
        EptsReportUtils.map(postTratmentFollowUp, "startDate=${startDate},location=${location}"));

    cd.setCompositionString("A AND postTratmentFollowUp");

    return cd;
  }

  public CohortDefinition getPatientsWithPostTreatmentFollowUpAndHavePositiveResult() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Post Treatment FollowUp With Positive Result");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition postTreatmentFollowUp = getPatientsWithPostTreatmentFollowUp();
    CohortDefinition positiveResult = getPatientsWithPositiveResultForScreeningTest();

    cd.addSearch(
        "postTreatmentFollowUp",
        EptsReportUtils.map(
            postTreatmentFollowUp,
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "positiveResult",
        EptsReportUtils.map(
            positiveResult, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("postTreatmentFollowUp AND positiveResult");

    return cd;
  }

  public CohortDefinition getPatientsWithPostTreatmentFollowUpWithNegativeResult() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Post Treatment FollowUp With Negative Result");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition postTreatmentFollowUp = getPatientsWithPostTreatmentFollowUp();
    CohortDefinition negativeResult = getPatientsWithNegativeResultForScreeningTest(false);

    cd.addSearch(
        "postTreatmentFollowUp",
        EptsReportUtils.map(
            postTreatmentFollowUp,
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "negativeResult",
        EptsReportUtils.map(
            negativeResult, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("postTreatmentFollowUp AND negativeResult");

    return cd;
  }

  public CohortDefinition getPatientsWithPostTreatmentFollowUpWithSuspectedCancerResult() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Post Treatment FollowUp With Suspected Cancer Result");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition postTreatmentFollowUp = getPatientsWithPostTreatmentFollowUp();
    CohortDefinition suspectedCancerResult = getPatientsWithSuspectedCancerResultForScreeningTest();

    cd.addSearch(
        "postTreatmentFollowUp",
        EptsReportUtils.map(
            postTreatmentFollowUp,
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "suspectedCancerResult",
        EptsReportUtils.map(
            suspectedCancerResult,
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("postTreatmentFollowUp AND suspectedCancerResult");

    return cd;
  }

  public CohortDefinition getRescreenedAfterPreviousPositive(CXCASCRNResult cxcascrnResult) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Rescreened After Previous Positive");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition a = getA();
    CohortDefinition aa = getAA(cxcascrnResult);
    CohortDefinition firstTimeScreened = get1stTimeScreened(cxcascrnResult);
    CohortDefinition rescreenedAfterPreviousNegative =
        getRescreenedAfterPreviousNegative(cxcascrnResult);
    CohortDefinition postTreatmentFollowUp = getPostTreatmentFollowUp(cxcascrnResult);

    cd.addSearch(
        "A",
        EptsReportUtils.map(a, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "AA",
        EptsReportUtils.map(
            aa, "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));
    cd.addSearch(
        "firstTimeScreened",
        EptsReportUtils.map(
            firstTimeScreened, "startDate=${startDate},endDate=${endDate},location=${location}"));
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
        "A AND AA AND NOT firstTimeScreened AND NOT rescreenedAfterPreviousNegative AND NOT postTreatmentFollowUp ");
    return cd;
  }

  public CohortDefinition getPatientsWhoRescreenedAfterPreviousPositive() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Rescreened After Previous Positive");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition a = getTotal();
    CohortDefinition firstTimeScreened = get1stTimeScreenedPatients();
    CohortDefinition rescreenedAfterPreviousNegative = getPatentsRescreenedAfterPreviousNegative();
    CohortDefinition postTreatmentFollowUp = getPatientsWithPostTreatmentFollowUp();

    cd.addSearch(
        "A",
        EptsReportUtils.map(a, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "firstTimeScreened",
        EptsReportUtils.map(
            firstTimeScreened, "startDate=${startDate},endDate=${endDate},location=${location}"));
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
        "A AND NOT (firstTimeScreened OR rescreenedAfterPreviousNegative OR postTreatmentFollowUp)");
    return cd;
  }

  public CohortDefinition getPatientsWhoRescreenedAfterPreviousPositiveAndWithPositiveResult() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Rescreened After Previous Positive And with Positive Result");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition rescreenedAfterPreviousPositive =
        getPatientsWhoRescreenedAfterPreviousPositive();
    CohortDefinition positiveResult = getPatientsWithPositiveResultForScreeningTest();

    cd.addSearch(
        "rescreenedAfterPreviousPositive",
        EptsReportUtils.map(
            rescreenedAfterPreviousPositive,
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "positiveResult",
        EptsReportUtils.map(
            positiveResult, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("rescreenedAfterPreviousPositive AND positiveResult");
    return cd;
  }

  public CohortDefinition getPatientsWhoRescreenedAfterPreviousPositiveAndWithNegativeResult() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Rescreened After Previous Positive And with Negative Result");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition rescreenedAfterPreviousPositive =
        getPatientsWhoRescreenedAfterPreviousPositive();
    CohortDefinition negativeResult = getPatientsWithNegativeResultForScreeningTest(false);

    cd.addSearch(
        "rescreenedAfterPreviousPositive",
        EptsReportUtils.map(
            rescreenedAfterPreviousPositive,
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "negativeResult",
        EptsReportUtils.map(
            negativeResult, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("rescreenedAfterPreviousPositive AND negativeResult");
    return cd;
  }

  public CohortDefinition
      getPatientsWhoRescreenedAfterPreviousPositiveAndWithSuspectedCancerResult() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Rescreened After Previous Positive And with Suspected Cancer Result");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition rescreenedAfterPreviousPositive =
        getPatientsWhoRescreenedAfterPreviousPositive();
    CohortDefinition suspectedCancerResult = getPatientsWithSuspectedCancerResultForScreeningTest();

    cd.addSearch(
        "rescreenedAfterPreviousPositive",
        EptsReportUtils.map(
            rescreenedAfterPreviousPositive,
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "suspectedCancerResult",
        EptsReportUtils.map(
            suspectedCancerResult,
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("rescreenedAfterPreviousPositive AND suspectedCancerResult");
    return cd;
  }

  public CohortDefinition getPositiveOrNegativeOrSuspetedOrAll(CXCASCRNResult cxcascrnResult) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition a = getA();
    CohortDefinition aa = getAA(cxcascrnResult);

    cd.addSearch(
        "A",
        EptsReportUtils.map(a, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "AA",
        EptsReportUtils.map(
            aa, "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));

    cd.setCompositionString("A AND AA");

    return cd;
  }

  public CohortDefinition getTotal(CXCASCRNResult cxcascrnResult) {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Total of CXCA SCRN");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition aa = getPatientsWithScreeningTestForCervicalCancer(cxcascrnResult, false);

    CohortDefinition a = this.getA();

    cd.addSearch("A", EptsReportUtils.map(a, "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "AA",
        EptsReportUtils.map(aa, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("A AND AA");

    return cd;
  }

  public CohortDefinition getTotal() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Total of CXCA SCRN");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition aa = getPatientsWithScreeningTestForCervicalCancer(false);

    CohortDefinition a = this.getA();

    cd.addSearch("A", EptsReportUtils.map(a, "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "AA",
        EptsReportUtils.map(aa, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("A AND AA");

    return cd;
  }

  public List<Concept> getAnswers(CXCASCRNResult cxcascrnResult) {
    Concept suspectedCancerConcept = hivMetadata.getSuspectedCancerConcept();
    Concept negative = hivMetadata.getNegative();
    Concept positive = hivMetadata.getPositive();

    List<Concept> answers = new ArrayList<>();

    if (cxcascrnResult == CXCASCRNResult.ALL) {
      answers = Arrays.asList(suspectedCancerConcept, negative, positive);

    } else if (cxcascrnResult == CXCASCRNResult.SUSPECTED) {
      answers = Arrays.asList(suspectedCancerConcept);

    } else if (cxcascrnResult == CXCASCRNResult.POSITIVE) {
      answers = Arrays.asList(positive);

    } else if (cxcascrnResult == CXCASCRNResult.NEGATIVE) {
      answers = Arrays.asList(negative);
    }
    return answers;
  }

  public CohortDefinition getAA3OrAA4(CXCASCRNResult cxcascrnResult) {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    if (cxcascrnResult == CXCASCRNResult.NEGATIVE) {
      cd.setName("AA3 from CXCA SCRN");
    }
    if (cxcascrnResult == CXCASCRNResult.POSITIVE) {
      cd.setName("AA4 from CXCA SCRN");
    }

    cd.setQuery(CXCASCRNQueries.getAA3OrAA4Query(cxcascrnResult, hivMetadata, false));

    return cd;
  }

  public CohortDefinition getTotalPatientsWithPositiveResult() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Total of Patients With Positive Result");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition a = getTotal();
    CohortDefinition positiveResult = getPatientsWithPositiveResultForScreeningTest();

    cd.addSearch(
        "A",
        EptsReportUtils.map(a, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "positiveResult",
        EptsReportUtils.map(
            positiveResult, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("A AND positiveResult");
    return cd;
  }

  public CohortDefinition getPositive1stTimeScreenedPatients() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Positive Patients - 1st Time Screened");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition a = getTotalPatientsWithPositiveResult();
    CohortDefinition aa1 = getPatientsWithScreeningTestForCervicalCancer(true);

    cd.addSearch(
        "A",
        EptsReportUtils.map(a, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch("AA1", EptsReportUtils.map(aa1, "startDate=${startDate},location=${location}"));

    cd.setCompositionString("A AND NOT AA1");

    return cd;
  }

  public CohortDefinition getPositivePatentsRescreenedAfterPreviousNegative() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Positive Patients With Rescreened After Previous Negative");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition a = getTotalPatientsWithPositiveResult();
    CohortDefinition aa = getPatientsWithNegativeResultForScreeningTest(true);

    cd.addSearch(
        "A",
        EptsReportUtils.map(a, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch("AA", EptsReportUtils.map(aa, "startDate=${startDate},location=${location}"));

    cd.setCompositionString("A AND AA");
    return cd;
  }

  public CohortDefinition getPositivePatientsWithPostTreatmentFollowUp() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Positive Patients With Post Treatment FollowUp");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition a = getTotalPatientsWithPositiveResult();
    CohortDefinition postTratmentFollowUp = getPatientsWitPostTratmentFollowUp();

    cd.addSearch(
        "A",
        EptsReportUtils.map(a, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "postTratmentFollowUp",
        EptsReportUtils.map(postTratmentFollowUp, "startDate=${startDate},location=${location}"));

    cd.setCompositionString("A AND postTratmentFollowUp");

    return cd;
  }

  public CohortDefinition getPositivePatientsWhoRescreenedAfterPreviousPositive() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Positive Patients With Rescreened After Previous Positive");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition a = getTotalPatientsWithPositiveResult();
    CohortDefinition firstTimeScreened = get1stTimeScreenedPatients();
    CohortDefinition rescreenedAfterPreviousNegative = getPatentsRescreenedAfterPreviousNegative();
    CohortDefinition postTreatmentFollowUp = getPatientsWithPostTreatmentFollowUp();

    cd.addSearch(
        "A",
        EptsReportUtils.map(a, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "firstTimeScreened",
        EptsReportUtils.map(
            firstTimeScreened, "startDate=${startDate},endDate=${endDate},location=${location}"));
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
        "A AND NOT (firstTimeScreened OR rescreenedAfterPreviousNegative OR postTreatmentFollowUp)");
    return cd;
  }
}
