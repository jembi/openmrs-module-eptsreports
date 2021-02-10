package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.calculation.cxcascrn.CXCASCRNCalculation;
import org.openmrs.module.eptsreports.reporting.cohort.definition.CalculationCohortDefinition;
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
    NEGATIVE
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
    CXCASCRNCalculation cxcascrnCalculation =
        Context.getRegisteredComponents(CXCASCRNCalculation.class).get(0);

    CalculationCohortDefinition cd = new CalculationCohortDefinition();
    cd.setName("AA from CXCA SCRN");
    cd.setCalculation(cxcascrnCalculation);
    cd.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    cd.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    return cd;
  }

  private CohortDefinition getAA1OrAA2(CXCASCRNResult cxcascrnResult, boolean isAA1) {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("AA1 from CXCA SCRN");

    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("28", hivMetadata.getRastreioDoCancroDoColoUterinoEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());

    map.put("2094", hivMetadata.getResultadoViaConcept().getConceptId());
    map.put("664", hivMetadata.getNegative().getConceptId());
    map.put("703", hivMetadata.getPositive().getConceptId());

    StringBuilder query = new StringBuilder();
    query.append(" SELECT p.patient_id ");
    query.append(" FROM patient p ");
    query.append("    INNER JOIN encounter e ");
    query.append("        ON e.patient_id = p.patient_id ");
    query.append("    INNER JOIN obs o ");
    query.append("        ON o.encounter_id = e.patient_id ");
    query.append(" WHERE p.voided = 0 ");
    query.append("    AND e.voided = 0 ");
    query.append("    AND o.voided = 0 ");
    if (isAA1) {
      query.append("    AND e.encounter_type IN (${6},${28}) ");
    } else {
      query.append("    AND e.encounter_type = ${53} ");
    }
    query.append("    AND o.concept_id = ${2094} ");
    if (cxcascrnResult == CXCASCRNResult.NEGATIVE) {
      query.append("    AND o.value_coded = ${664} ");
    }
    if (cxcascrnResult == CXCASCRNResult.POSITIVE) {
      query.append("    AND o.value_coded = ${703} ");
    }
    query.append("    AND e.encounter_datetime < :startDate ");
    query.append("    AND e.location_id = :location ");

    StringSubstitutor sb = new StringSubstitutor(map);
    cd.setQuery(sb.replace(query));

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

    CohortDefinition aa1 = getAA1OrAA2(cxcascrnResult, true);
    CohortDefinition aa2 = getAA1OrAA2(cxcascrnResult, false);

    cd.addSearch("AA1", EptsReportUtils.map(aa1, "startDate=${startDate},location=${location}"));
    cd.addSearch("AA2", EptsReportUtils.map(aa2, "startDate=${startDate},location=${location}"));

    cd.setCompositionString("AA1 OR AA2");

    return cd;
  }
}
