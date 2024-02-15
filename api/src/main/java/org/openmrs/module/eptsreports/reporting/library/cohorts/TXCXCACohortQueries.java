package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.calculation.cxcascrn.CXCASCRNBBCalculation;
import org.openmrs.module.eptsreports.reporting.calculation.cxcascrn.CXCATreatmentHierarchyCalculation;
import org.openmrs.module.eptsreports.reporting.calculation.cxcascrn.TXCXCACalculation;
import org.openmrs.module.eptsreports.reporting.calculation.cxcascrn.TreatmentType;
import org.openmrs.module.eptsreports.reporting.cohort.definition.CalculationCohortDefinition;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TXCXCACohortQueries {

  private CXCASCRNCohortQueries cxcascrnCohortQueries;

  private HivMetadata hivMetadata;

  private final String MAPPINGS = "startDate=${startDate},endDate=${endDate},location=${location}";

  @Autowired
  public TXCXCACohortQueries(CXCASCRNCohortQueries cxcascrnCohortQueries, HivMetadata hivMetadata) {
    this.cxcascrnCohortQueries = cxcascrnCohortQueries;
    this.hivMetadata = hivMetadata;
  }

  public CohortDefinition getFirstTimeScreened() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.setName("TX f1srt Time Screened");

    CohortDefinition totalPatientsCxcaScrnPositiveWithTreatment = getTotal();
    CohortDefinition patientsWithScreeningTestForCervicalCancerBeforeStartDate =
        this.cxcascrnCohortQueries.getPatientsWithScreeningTestForCervicalCancer(true);

    cd.addSearch(
        "totalPatientsCxcaScrnPositiveWithTreatment",
        EptsReportUtils.map(totalPatientsCxcaScrnPositiveWithTreatment, MAPPINGS));
    cd.addSearch(
        "patientsWithScreeningTestForCervicalCancerBeforeStartDate",
        EptsReportUtils.map(
            patientsWithScreeningTestForCervicalCancerBeforeStartDate,
            "startDate=${startDate},location=${location}"));

    cd.setCompositionString(
        "totalPatientsCxcaScrnPositiveWithTreatment AND NOT patientsWithScreeningTestForCervicalCancerBeforeStartDate");
    return cd;
  }

  public CohortDefinition getRescreenedAfterPreviousNegative() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.setName("TX Rescreened After Previous Negative");

    CohortDefinition totalPatientsCxcaScrnPositiveWithTreatment = getTotal();
    CohortDefinition patientsWithNegativeResultForScreeningTestBeforeReportingPeriod =
        this.cxcascrnCohortQueries.getPatientsWithNegativeResultForScreeningTest(true);

    cd.addSearch(
        "totalPatientsCxcaScrnPositiveWithTreatment",
        EptsReportUtils.map(totalPatientsCxcaScrnPositiveWithTreatment, MAPPINGS));
    cd.addSearch(
        "patientsWithNegativeResultForScreeningTestBeforeReportingPeriod",
        EptsReportUtils.map(
            patientsWithNegativeResultForScreeningTestBeforeReportingPeriod,
            "startDate=${startDate},location=${location}"));

    cd.setCompositionString(
        "totalPatientsCxcaScrnPositiveWithTreatment AND patientsWithNegativeResultForScreeningTestBeforeReportingPeriod");

    return cd;
  }

  public CohortDefinition getPatientsWithPostTreatmentFollowUp() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.setName("TX Post Treatment Follow Up");

    CohortDefinition totalPatientsCxcaScrnPositiveWithTreatment = getTotal();
    CohortDefinition postTreatmentFollowUp = this.cxcascrnCohortQueries.getPostTreatmentFollowUp();

    cd.addSearch(
        "totalPatientsCxcaScrnPositiveWithTreatment",
        EptsReportUtils.map(totalPatientsCxcaScrnPositiveWithTreatment, MAPPINGS));
    cd.addSearch(
        "postTreatmentFollowUp",
        EptsReportUtils.map(
            postTreatmentFollowUp,
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("totalPatientsCxcaScrnPositiveWithTreatment AND postTreatmentFollowUp");

    return cd;
  }

  public CohortDefinition getRescreenedAfterPreviousPositive() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.setName("TX Rescreened after previous positive");

    CohortDefinition totalPatientsCxcaScrnPositiveWithTreatment = getTotal();
    CohortDefinition f1srtTimeScreened = getFirstTimeScreened();
    CohortDefinition rescreenedAfterPreviousNegative = getRescreenedAfterPreviousNegative();
    CohortDefinition postTreatmentFollowUp = getPatientsWithPostTreatmentFollowUp();

    cd.addSearch(
        "totalPatientsCxcaScrnPositiveWithTreatment",
        EptsReportUtils.map(totalPatientsCxcaScrnPositiveWithTreatment, MAPPINGS));
    cd.addSearch("f1srtTimeScreened", EptsReportUtils.map(f1srtTimeScreened, MAPPINGS));
    cd.addSearch(
        "rescreenedAfterPreviousNegative",
        EptsReportUtils.map(rescreenedAfterPreviousNegative, MAPPINGS));
    cd.addSearch("postTreatmentFollowUp", EptsReportUtils.map(postTreatmentFollowUp, MAPPINGS));

    cd.setCompositionString(
        "totalPatientsCxcaScrnPositiveWithTreatment AND NOT (postTreatmentFollowUp OR rescreenedAfterPreviousNegative OR f1srtTimeScreened)");

    return cd;
  }

  /**
   * <b> The system will generate CXCA_TX indicator numerator as number of patients on ART screened
   * positive (TX_FR2) and who received a treatment type during the reporting period (TX_FR7) with
   * the specified disaggregation (TX_FR5). </b>
   *
   * <p><b> fr7</b> The system will identify women patients who received a treatment type during the
   * period as follows:
   * <li>Patients who have Crioterapia Realizada no Mesmo dia que a via=Sim registered in Ficha de
   *     Registo para Rastreio do Cancro do Colo Uterino between first First VIA Result Positive
   *     (TX_FR6) and reporting period end date or
   * <li>Patients who have Data da Realização da Crioterapia occurred in Ficha de Registo para
   *     Rastreio do Cancro do Colo Uterino between first First VIA Result Positive (TX_FR6) and
   *     reporting period end date or
   * <li>Patients who have Resposta = (“Crioterapia Feita” or “Termocoagulação Feita” or “Leep
   *     Feito” or “Conização Feita”) registered in Ficha de Registo para Rastreio do Cancro do Colo
   *     Uterino between first First VIA Result Positive (TX_FR6) and reporting period end date.
   *
   * @return {@link CohortDefinition}
   */
  private CohortDefinition getBB() {
    TXCXCACalculation cxcascrnCalculation =
        Context.getRegisteredComponents(TXCXCACalculation.class).get(0);

    CalculationCohortDefinition cd = new CalculationCohortDefinition();
    cd.setName("BB from CXCA SCRN");
    cd.setCalculation(cxcascrnCalculation);
    cd.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    cd.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addCalculationParameter(
        "answers", this.cxcascrnCohortQueries.getAnswers(CXCASCRNCohortQueries.CXCASCRNResult.ANY));

    return cd;
  }

  private CohortDefinition getBB1() {
    CXCASCRNBBCalculation cxcascrnCalculation =
        Context.getRegisteredComponents(CXCASCRNBBCalculation.class).get(0);

    CalculationCohortDefinition cd = new CalculationCohortDefinition();
    cd.setName("BB1 from CXCA SCRN");
    cd.setCalculation(cxcascrnCalculation);
    cd.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    cd.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addCalculationParameter(
        "answers",
        this.cxcascrnCohortQueries.getAnswers(CXCASCRNCohortQueries.CXCASCRNResult.POSITIVE));

    return cd;
  }

  public CohortDefinition getB5OrB6OrB7(TreatmentType treatmentType) {
    CXCATreatmentHierarchyCalculation cxcascrnCalculation =
        Context.getRegisteredComponents(CXCATreatmentHierarchyCalculation.class).get(0);

    CalculationCohortDefinition cd = new CalculationCohortDefinition();
    switch (treatmentType) {
      case B5:
        cd.setName("TX B5 Cryotherapy");
        break;
      case B6:
        cd.setName("TX B6 Thermocoagulation");
        break;
      case B7:
        cd.setName("TX B7 LEEP");
        break;
      default:
        throw new IllegalArgumentException("Unsupported value");
    }
    cd.setCalculation(cxcascrnCalculation);
    cd.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    cd.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addCalculationParameter("type", treatmentType);

    return cd;
  }

  public CohortDefinition getTotal() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Total of  TX CXCA SCRN Positive With a Treatment Type");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition patientsOnArtWithPositiveScreening =
        this.cxcascrnCohortQueries.getTotalPatientsWithPositiveResult();
    CohortDefinition patientsWhoReceivedATreatmentType = getPatientsWhoReceivedATreatmentType();

    cd.addSearch(
        "patientsOnArtWithPositiveScreening",
        EptsReportUtils.map(patientsOnArtWithPositiveScreening, MAPPINGS));
    cd.addSearch(
        "patientsWhoReceivedATreatmentType",
        EptsReportUtils.map(
            patientsWhoReceivedATreatmentType,
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString(
        "patientsOnArtWithPositiveScreening AND patientsWhoReceivedATreatmentType");

    return cd;
  }

  public CohortDefinition getB5() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.setName("TX B5 Cryotherapy");

    Map<String, Integer> map = new HashMap<>();
    map.put("28", hivMetadata.getRastreioDoCancroDoColoUterinoEncounterType().getEncounterTypeId());
    map.put("2117", hivMetadata.getCryotherapyPerformedOnTheSameDayASViaConcept().getConceptId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    map.put("23967", hivMetadata.getCryotherapyDateConcept().getConceptId());
    map.put("2149", hivMetadata.getViaResultOnTheReferenceConcept().getConceptId());
    map.put("23874", hivMetadata.getPediatricNursingConcept().getConceptId());

    String sql =
        ""
            + "SELECT p.patient_id "
            + "FROM patient p "
            + "    INNER JOIN encounter e "
            + "        ON e.patient_id = p.patient_id "
            + "    INNER JOIN obs o "
            + "        ON e.encounter_id = o.encounter_id "
            + "WHERE  "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${28} "
            + "    AND ( "
            + "            (o.concept_id = ${2117} AND o.value_coded = ${1065}) "
            + "            OR "
            + "            (o.concept_id = ${23967} ) "
            + "            OR "
            + "            (o.concept_id = ${2149} AND o.value_coded = ${23874}) "
            + "        )    "
            + "    AND e.location_id = :location";

    StringSubstitutor sb = new StringSubstitutor(map);
    cd.setQuery(sb.replace(sql));

    return cd;
  }

  public SqlCohortDefinition getB6() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.setName("TX B6 Thermocoagulation ");

    Map<String, Integer> map = new HashMap<>();
    map.put("28", hivMetadata.getRastreioDoCancroDoColoUterinoEncounterType().getEncounterTypeId());
    map.put("2149", hivMetadata.getViaResultOnTheReferenceConcept().getConceptId());
    map.put("23972", hivMetadata.getThermocoagulationConcept().getConceptId());

    String sql =
        ""
            + " SELECT p.patient_id "
            + " FROM patient p "
            + "    INNER JOIN encounter e "
            + "        ON e.patient_id = p.patient_id "
            + "    INNER JOIN obs o "
            + "        ON e.encounter_id = o.encounter_id "
            + " WHERE  "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${28} "
            + "    AND o.concept_id = ${2149} AND o.value_coded = ${23972} "
            + "    AND e.location_id = :location ";

    StringSubstitutor sb = new StringSubstitutor(map);
    cd.setQuery(sb.replace(sql));

    return cd;
  }

  public SqlCohortDefinition getB7() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.setName("TX B7 LEEP");

    Map<String, Integer> map = new HashMap<>();
    map.put("28", hivMetadata.getRastreioDoCancroDoColoUterinoEncounterType().getEncounterTypeId());
    map.put("2149", hivMetadata.getViaResultOnTheReferenceConcept().getConceptId());
    map.put("23970", hivMetadata.getLeepConcept().getConceptId());
    map.put("23973", hivMetadata.getconizationConcept().getConceptId());

    String sql =
        ""
            + " SELECT p.patient_id "
            + " FROM patient p "
            + "    INNER JOIN encounter e "
            + "        ON e.patient_id = p.patient_id "
            + "    INNER JOIN obs o "
            + "        ON e.encounter_id = o.encounter_id "
            + " WHERE  "
            + "    p.voided = 0 "
            + "    AND e.voided = 0 "
            + "    AND o.voided = 0 "
            + "    AND e.encounter_type = ${28} "
            + "    AND o.concept_id = ${2149} AND o.value_coded IN (${23970}, ${23973}) "
            + "    AND e.location_id = :location";

    StringSubstitutor sb = new StringSubstitutor(map);
    cd.setQuery(sb.replace(sql));

    return cd;
  }

  public CohortDefinition getFinalComposition(
      CohortDefinition ccd, CohortDefinition scd, String name) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.setName(name);

    cd.addSearch("CCD", EptsReportUtils.map(ccd, MAPPINGS));
    cd.addSearch(
        "SCD",
        EptsReportUtils.map(
            scd, "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));

    cd.setCompositionString("CCD AND SCD");

    return cd;
  }

  /**
   * <b>SCRN_FR7</b> Patient who received a treatment type during the reporting period
   *
   * <p>The system will identify female patients who received a treatment type during the period by
   * selecting all patients who:
   *
   * <ul>
   *   <li>have Tratamento Feito = Crioterapia or Termoablação registered on the Ficha de Registo
   *       Individual: Rastreio dos Cancros do Colo do Utero e da Mama with a Data do Tratamento
   *       between the most recent positive VIA result (TX_FR6) and the reporting period end date OR
   *   <li>have Qual foi o tratamento/avaliação no HdR = Crioterapia Feita” or “Termocoagulação
   *       Feita” or “Leep Feito” or “Conização Feita”) registered in Ficha de Registo Individual:
   *       Rastreio dos Cancros do Colo do Útero e da Mama between the most recent positive VIA
   *       result (TX_FR6) and the reporting period end date.
   * </ul>
   *
   * <p>For patients who have more than one treatment registered during the reporting period, the
   * system will consider the most recent one among them.
   *
   * <p>For two different treatment types registered on the same date, the algorithm will apply the
   * following hierarchy:
   *
   * <ul>
   *   <li>1 - LEEP / Conização
   *   <li>2 - Termocoagulação
   *   <li>3 - Crioterapia
   * </ul>
   */
  public CohortDefinition getPatientsWhoReceivedATreatmentType() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patient who received a treatment type during the reporting period");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("28", hivMetadata.getRastreioDoCancroDoColoUterinoEncounterType().getEncounterTypeId());
    map.put("2094", hivMetadata.getResultadoViaConcept().getConceptId());
    map.put("703", hivMetadata.getPositive().getConceptId());
    map.put("2093", hivMetadata.getSuspectedCancerConcept().getConceptId());
    map.put("664", hivMetadata.getNegative().getConceptId());
    map.put("165436", hivMetadata.getHumanPapillomavirusDnaConcept().getConceptId());
    map.put("1185", hivMetadata.getTreatmentConcept().getConceptId());
    map.put("2149", hivMetadata.getViaResultOnTheReferenceConcept().getConceptId());
    map.put("23974", hivMetadata.getCryotherapyConcept().getConceptId());
    map.put("165439", hivMetadata.getTermoablationConcept().getConceptId());
    map.put("23972", hivMetadata.getThermocoagulationConcept().getConceptId());
    map.put("23970", hivMetadata.getLeepConcept().getConceptId());
    map.put("23973", hivMetadata.getconizationConcept().getConceptId());

    String query =
        "SELECT     p.patient_id "
            + "FROM       patient p "
            + "               INNER JOIN encounter e "
            + "                          ON         e.patient_id = p.patient_id "
            + "               INNER JOIN obs o "
            + "                          ON         o.encounter_id = e.encounter_id "
            + "               INNER JOIN "
            + "           ( "
            + "               SELECT     p.patient_id, "
            + "                          last_result.encounter_date AS last_positive_encounter "
            + "               FROM       patient p "
            + "                              INNER JOIN encounter e "
            + "                                         ON         e.patient_id = p.patient_id "
            + "                              INNER JOIN obs o "
            + "                                         ON         o.encounter_id = e.encounter_id "
            + "                              INNER JOIN "
            + "                          ( "
            + "                              SELECT     p.patient_id, "
            + "                                         Max(e.encounter_datetime) AS encounter_date "
            + "                              FROM       patient p "
            + "                                             INNER JOIN encounter e "
            + "                                                        ON         e.patient_id = p.patient_id "
            + "                                             INNER JOIN obs o "
            + "                                                        ON         o.encounter_id =e.encounter_id "
            + "                              WHERE      p.voided = 0 "
            + "                                AND        e.voided = 0 "
            + "                                AND        o.voided = 0 "
            + "                                AND        e.encounter_datetime BETWEEN :startDate AND        :endDate "
            + "                                AND        e.location_id = :location "
            + "                                AND        e.encounter_type = ${28} "
            + "                                AND        o.concept_id = ${2094} "
            + "                                AND        o.value_coded IN (${703}, "
            + "                                                             ${2093}, "
            + "                                                             ${664}) "
            + "                              GROUP BY   p.patient_id) AS last_result "
            + "                          ON         last_result.patient_id = p.patient_id "
            + "               WHERE      p.voided = 0 "
            + "                 AND        e.voided = 0 "
            + "                 AND        o.voided = 0 "
            + "                 AND        e.encounter_type = ${28} "
            + "                 AND        e.encounter_datetime = last_result.encounter_date "
            + "                 AND        e.location_id = :location "
            + "                 AND        o.concept_id = ${2094} "
            + "                 AND        o.value_coded = ${703} "
            + "               GROUP BY   p.patient_id ) positive_via "
            + "WHERE      p.voided = 0 "
            + "  AND        o.voided = 0 "
            + "  AND        e.encounter_type = ${28} "
            + "  AND        e.location_id = :location "
            + "  AND        ( ( "
            + "                   o.concept_id = ${1185} "
            + "                       AND        o.value_coded IN ( ${23974}, "
            + "                                                      ${165439} ) "
            + "                       AND        o.obs_datetime BETWEEN positive_via.last_positive_encounter AND        :endDate) "
            + "    OR         ( "
            + "                   o.concept_id = ${2149} "
            + "                       AND        o.value_coded IN ( ${23974}, "
            + "                                                      ${23972}, "
            + "                                                      ${23970}, "
            + "                                                      ${23973} ) "
            + "                       AND        o.obs_datetime BETWEEN positive_via.last_positive_encounter AND        :endDate) )";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   * <b>LEEP</b>
   *
   * <p>All patients who screened positive and received treatment (TX_FR3) who have or Qual foi o
   * tratamento/avaliação no HdR = "LEEP Feita” or “Conização Feita."
   *
   * <p>For patients who have more than one treatment registered during the reporting period, the
   * system will consider the most recent one among them.
   *
   * <p>For two different treatment types registered on the same date, the algorithm will apply the
   * following hierarchy:
   *
   * <ul>
   *   <li>1 - LEEP / Conização
   *   <li>2 - Termocoagulação
   *   <li>3 - Crioterapia
   * </ul>
   */
  public CohortDefinition getPatientsWhoHaveLeepOrConizationTreatmentType() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patients who have Leep or Conization treatment type during the reporting period");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("28", hivMetadata.getRastreioDoCancroDoColoUterinoEncounterType().getEncounterTypeId());
    map.put("2094", hivMetadata.getResultadoViaConcept().getConceptId());
    map.put("703", hivMetadata.getPositive().getConceptId());
    map.put("2093", hivMetadata.getSuspectedCancerConcept().getConceptId());
    map.put("664", hivMetadata.getNegative().getConceptId());
    map.put("165436", hivMetadata.getHumanPapillomavirusDnaConcept().getConceptId());
    map.put("2149", hivMetadata.getViaResultOnTheReferenceConcept().getConceptId());
    map.put("23970", hivMetadata.getLeepConcept().getConceptId());
    map.put("23973", hivMetadata.getconizationConcept().getConceptId());

    String query =
        "SELECT leep_conization.patient_id "
            + "FROM   ( "
            + "           SELECT     p.patient_id, "
            + "                      Max(o.obs_datetime) "
            + "           FROM       patient p "
            + "                          INNER JOIN encounter e "
            + "                                     ON         e.patient_id = p.patient_id "
            + "                          INNER JOIN obs o "
            + "                                     ON         o.encounter_id = e.encounter_id "
            + "                          INNER JOIN "
            + "                      ( "
            + "                          SELECT     p.patient_id, "
            + "                                     last_result.encounter_date AS last_positive_encounter "
            + "                          FROM       patient p "
            + "                                         INNER JOIN encounter e "
            + "                                                    ON         e.patient_id = p.patient_id "
            + "                                         INNER JOIN obs o "
            + "                                                    ON         o.encounter_id = e.encounter_id "
            + "                                         INNER JOIN "
            + "                                     ( "
            + "                                         SELECT     p.patient_id, "
            + "                                                    Max(e.encounter_datetime) AS encounter_date "
            + "                                         FROM       patient p "
            + "                                                        INNER JOIN encounter e "
            + "                                                                   ON         e.patient_id = p.patient_id "
            + "                                                        INNER JOIN obs o "
            + "                                                                   ON         o.encounter_id =e.encounter_id "
            + "                                         WHERE      p.voided = 0 "
            + "                                           AND        e.voided = 0 "
            + "                                           AND        o.voided = 0 "
            + "                                           AND        e.encounter_datetime BETWEEN :startDate AND        :endDate "
            + "                                           AND        e.location_id = :location "
            + "                                           AND        e.encounter_type = ${28} "
            + "                                           AND        o.concept_id = ${2094} "
            + "                                           AND        o.value_coded IN (${703}, "
            + "                                                                        ${2093}, "
            + "                                                                        ${664}) "
            + "                                         GROUP BY   p.patient_id) AS last_result "
            + "                                     ON         last_result.patient_id = p.patient_id "
            + "                          WHERE      p.voided = 0 "
            + "                            AND        e.voided = 0 "
            + "                            AND        o.voided = 0 "
            + "                            AND        e.encounter_type = ${28} "
            + "                            AND        e.encounter_datetime = last_result.encounter_date "
            + "                            AND        e.location_id = :location "
            + "                            AND        o.concept_id = ${2094} "
            + "                            AND        o.value_coded = ${703} "
            + "                          GROUP BY   p.patient_id ) positive_via "
            + "WHERE      p.voided = 0 "
            + "  AND        o.voided = 0 "
            + "  AND        e.encounter_type = ${28} "
            + "  AND        e.location_id = :location "
            + "  AND        o.concept_id = ${2149} "
            + "  AND        o.value_coded IN ( ${23970}, ${23973} ) "
            + "  AND        o.obs_datetime BETWEEN positive_via.last_positive_encounter AND        :endDate "
            + "  GROUP BY p.patient_id ) leep_conization";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   * <b>Thermocoagulation</b>
   *
   * <p>All patients who screened positive and received treatment (TX_FR3) who have Tratamento Feito
   * = Termoablação or Qual foi o tratamento/avaliação no HdR = "Termocolagulação Feita."
   *
   * <p>For patients who have more than one treatment registered during the reporting period, the
   * system will consider the most recent one among them.
   *
   * <p>For two different treatment types registered on the same date, the algorithm will apply the
   * following hierarchy:
   *
   * <ul>
   *   <li>1 - LEEP / Conização
   *   <li>2 - Termocoagulação
   *   <li>3 - Crioterapia
   * </ul>
   */
  public CohortDefinition getPatientsWhoHaveThermocoagulationAsLastTreatmentType() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName(
        "Patients who have Thermocoagulation as last treatment type during the reporting period");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("28", hivMetadata.getRastreioDoCancroDoColoUterinoEncounterType().getEncounterTypeId());
    map.put("2094", hivMetadata.getResultadoViaConcept().getConceptId());
    map.put("703", hivMetadata.getPositive().getConceptId());
    map.put("2093", hivMetadata.getSuspectedCancerConcept().getConceptId());
    map.put("664", hivMetadata.getNegative().getConceptId());
    map.put("165436", hivMetadata.getHumanPapillomavirusDnaConcept().getConceptId());
    map.put("2149", hivMetadata.getViaResultOnTheReferenceConcept().getConceptId());
    map.put("23970", hivMetadata.getLeepConcept().getConceptId());
    map.put("23973", hivMetadata.getconizationConcept().getConceptId());
    map.put("23972", hivMetadata.getThermocoagulationConcept().getConceptId());

    String query =
        "SELECT thermocoagulation.patient_id "
            + "FROM   ( "
            + "           SELECT     p.patient_id, "
            + "                      Max(o.obs_datetime) AS last_thermocoagulation "
            + "           FROM       patient p "
            + "                          INNER JOIN encounter e "
            + "                                     ON         e.patient_id = p.patient_id "
            + "                          INNER JOIN obs o "
            + "                                     ON         o.encounter_id = e.encounter_id "
            + "                          INNER JOIN "
            + "                      ( "
            + "                          SELECT     p.patient_id, "
            + "                                     last_result.encounter_date AS last_positive_encounter "
            + "                          FROM       patient p "
            + "                                         INNER JOIN encounter e "
            + "                                                    ON         e.patient_id = p.patient_id "
            + "                                         INNER JOIN obs o "
            + "                                                    ON         o.encounter_id = e.encounter_id "
            + "                                         INNER JOIN "
            + "                                     ( "
            + "                                         SELECT     p.patient_id, "
            + "                                                    Max(e.encounter_datetime) AS encounter_date "
            + "                                         FROM       patient p "
            + "                                                        INNER JOIN encounter e "
            + "                                                                   ON         e.patient_id = p.patient_id "
            + "                                                        INNER JOIN obs o "
            + "                                                                   ON         o.encounter_id =e.encounter_id "
            + "                                         WHERE      p.voided = 0 "
            + "                                           AND        e.voided = 0 "
            + "                                           AND        o.voided = 0 "
            + "                                           AND        e.encounter_datetime BETWEEN :startDate AND        :endDate "
            + "                                           AND        e.location_id = :location "
            + "                                           AND        e.encounter_type = ${28} "
            + "                                           AND        o.concept_id = ${2094} "
            + "                                           AND        o.value_coded IN (${703}, "
            + "                                                                        ${2093}, "
            + "                                                                        ${664}) "
            + "                                         GROUP BY   p.patient_id) AS last_result "
            + "                                     ON         last_result.patient_id = p.patient_id "
            + "                          WHERE      p.voided = 0 "
            + "                            AND        e.voided = 0 "
            + "                            AND        o.voided = 0 "
            + "                            AND        e.encounter_type = ${28} "
            + "                            AND        e.encounter_datetime = last_result.encounter_date "
            + "                            AND        e.location_id = :location "
            + "                            AND        o.concept_id = ${2094} "
            + "                            AND        o.value_coded = ${703} "
            + "                          GROUP BY   p.patient_id ) positive_via "
            + "           WHERE      p.voided = 0 "
            + "             AND        o.voided = 0 "
            + "             AND        e.encounter_type = ${28} "
            + "             AND        e.location_id = :location "
            + "             AND        o.concept_id = ${2149} "
            + "             AND        o.value_coded = ${23972} "
            + "             AND        o.obs_datetime BETWEEN positive_via.last_positive_encounter AND        :endDate "
            + "           GROUP BY   p.patient_id) thermocoagulation "
            + "WHERE  NOT EXISTS "
            + "           ( "
            + "               SELECT     e.patient_id "
            + "               FROM       encounter e "
            + "                              INNER JOIN obs o "
            + "                                         ON         o.encounter_id = e.encounter_id "
            + "                              INNER JOIN "
            + "                          ( "
            + "                              SELECT     p.patient_id, "
            + "                                         last_result.encounter_date AS last_positive_encounter "
            + "                              FROM       patient p "
            + "                                             INNER JOIN encounter e "
            + "                                                        ON         e.patient_id = p.patient_id "
            + "                                             INNER JOIN obs o "
            + "                                                        ON         o.encounter_id = e.encounter_id "
            + "                                             INNER JOIN "
            + "                                         ( "
            + "                                             SELECT     p.patient_id, "
            + "                                                        max(e.encounter_datetime) AS encounter_date "
            + "                                             FROM       patient p "
            + "                                                            INNER JOIN encounter e "
            + "                                                                       ON         e.patient_id = p.patient_id "
            + "                                                            INNER JOIN obs o "
            + "                                                                       ON         o.encounter_id =e.encounter_id "
            + "                                             WHERE      p.voided = 0 "
            + "                                               AND        e.voided = 0 "
            + "                                               AND        o.voided = 0 "
            + "                                               AND        e.encounter_datetime BETWEEN :startDate AND        :endDate "
            + "                                               AND        e.location_id = :location "
            + "                                               AND        e.encounter_type = ${28} "
            + "                                               AND        o.concept_id = ${2094} "
            + "                                               AND        o.value_coded IN (${703}, "
            + "                                                                            ${2093}, "
            + "                                                                            ${664}) "
            + "                                             GROUP BY   p.patient_id) AS last_result "
            + "                                         ON         last_result.patient_id = p.patient_id "
            + "                              WHERE      p.voided = 0 "
            + "                                AND        e.voided = 0 "
            + "                                AND        o.voided = 0 "
            + "                                AND        e.encounter_type = ${28} "
            + "                                AND        e.encounter_datetime = last_result.encounter_date "
            + "                                AND        e.location_id = :location "
            + "                                AND        o.concept_id = ${2094} "
            + "                                AND        o.value_coded = ${703} "
            + "                              GROUP BY   p.patient_id ) positive_via "
            + "               WHERE      e.voided = 0 "
            + "                 AND        o.voided = 0 "
            + "                 AND        e.encounter_type = ${28} "
            + "                 AND        e.location_id = :location "
            + "                 AND        o.concept_id = ${2149} "
            + "                 AND        o.value_coded IN (${23973}, "
            + "                                              ${23970}) "
            + "                 AND        o.obs_datetime BETWEEN positive_via.last_positive_encounter AND        :endDate "
            + "                 AND        thermocoagulation.patient_id = e.patient_id "
            + "                 AND        e.encounter_datetime >= thermocoagulation.last_thermocoagulation "
            + "                 AND        e.encounter_datetime <= :endDate )";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   * <b>Cryotherapy</b>
   *
   * <p>All patients who screened positive and received treatment (TX_FR3) who have Tratamento Feito
   * = Crioterapia or Qual foi o tratamento/avaliação no HdR= "criotherapia Feita."
   *
   * <p>For patients who have more than one treatment registered during the reporting period, the
   * system will consider the most recent one among them.
   *
   * <p>For two different treatment types registered on the same date, the algorithm will apply the
   * following hierarchy:
   *
   * <ul>
   *   <li>1 - LEEP / Conização
   *   <li>2 - Termocoagulação
   *   <li>3 - Crioterapia
   * </ul>
   */
  public CohortDefinition getPatientsWhoHaveCryotherapyAsLastTreatmentType() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patients who have Cryotherapy as last treatment type during the reporting period");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("28", hivMetadata.getRastreioDoCancroDoColoUterinoEncounterType().getEncounterTypeId());
    map.put("2094", hivMetadata.getResultadoViaConcept().getConceptId());
    map.put("703", hivMetadata.getPositive().getConceptId());
    map.put("2093", hivMetadata.getSuspectedCancerConcept().getConceptId());
    map.put("664", hivMetadata.getNegative().getConceptId());
    map.put("165436", hivMetadata.getHumanPapillomavirusDnaConcept().getConceptId());
    map.put("1185", hivMetadata.getTreatmentConcept().getConceptId());
    map.put("2149", hivMetadata.getViaResultOnTheReferenceConcept().getConceptId());
    map.put("23970", hivMetadata.getLeepConcept().getConceptId());
    map.put("23973", hivMetadata.getconizationConcept().getConceptId());
    map.put("23972", hivMetadata.getThermocoagulationConcept().getConceptId());
    map.put("23974", hivMetadata.getCryotherapyConcept().getConceptId());

    String query =
        "SELECT cryotherapy.patient_id "
            + "FROM   ( "
            + "                  SELECT     p.patient_id, "
            + "                             Max(o.obs_datetime) AS last_cryotherapy "
            + "                  FROM       patient p "
            + "                  INNER JOIN encounter e "
            + "                  ON         e.patient_id = p.patient_id "
            + "                  INNER JOIN obs o "
            + "                  ON         o.encounter_id = e.encounter_id "
            + "                  INNER JOIN "
            + "                             ( "
            + "                                        SELECT     p.patient_id, "
            + "                                                   last_result.encounter_date AS last_positive_encounter "
            + "                                        FROM       patient p "
            + "                                        INNER JOIN encounter e "
            + "                                        ON         e.patient_id = p.patient_id "
            + "                                        INNER JOIN obs o "
            + "                                        ON         o.encounter_id = e.encounter_id "
            + "                                        INNER JOIN "
            + "                                                   ( "
            + "                                                              SELECT     p.patient_id, "
            + "                                                                         Max(e.encounter_datetime) AS encounter_date "
            + "                                                              FROM       patient p "
            + "                                                              INNER JOIN encounter e "
            + "                                                              ON         e.patient_id = p.patient_id "
            + "                                                              INNER JOIN obs o "
            + "                                                              ON         o.encounter_id =e.encounter_id "
            + "                                                              WHERE      p.voided = 0 "
            + "                                                              AND        e.voided = 0 "
            + "                                                              AND        o.voided = 0 "
            + "                                                              AND        e.encounter_datetime BETWEEN :startDate AND        :endDate "
            + "                                                              AND        e.location_id = :location "
            + "                                                              AND        e.encounter_type = ${28} "
            + "                                                              AND        o.concept_id = ${2094} "
            + "                                                              AND        o.value_coded IN (${703}, "
            + "                                                                                           ${2093}, "
            + "                                                                                           ${664}) "
            + "                                                              GROUP BY   p.patient_id) AS last_result "
            + "                                        ON         last_result.patient_id = p.patient_id "
            + "                                        WHERE      p.voided = 0 "
            + "                                        AND        e.voided = 0 "
            + "                                        AND        o.voided = 0 "
            + "                                        AND        e.encounter_type = ${28} "
            + "                                        AND        e.encounter_datetime = last_result.encounter_date "
            + "                                        AND        e.location_id = :location "
            + "                                        AND        o.concept_id = ${2094} "
            + "                                        AND        o.value_coded = ${703} "
            + "                                        GROUP BY   p.patient_id ) positive_via "
            + "                  WHERE      p.voided = 0 "
            + "                  AND        o.voided = 0 "
            + "                  AND        e.encounter_type = ${28} "
            + "                  AND        e.location_id = :location "
            + "                  AND        o.concept_id IN ( ${1185}, "
            + "                                              ${2149} ) "
            + "                  AND        o.value_coded = ${23974} "
            + "                  AND        o.obs_datetime BETWEEN positive_via.last_positive_encounter AND        :endDate "
            + "                  GROUP BY   p.patient_id) cryotherapy "
            + "WHERE  NOT EXISTS "
            + "       ( "
            + "                  SELECT     e.patient_id "
            + "                  FROM       encounter e "
            + "                  INNER JOIN obs o "
            + "                  ON         o.encounter_id = e.encounter_id "
            + "                  INNER JOIN "
            + "                             ( "
            + "                                        SELECT     p.patient_id, "
            + "                                                   last_result.encounter_date AS last_positive_encounter "
            + "                                        FROM       patient p "
            + "                                        INNER JOIN encounter e "
            + "                                        ON         e.patient_id = p.patient_id "
            + "                                        INNER JOIN obs o "
            + "                                        ON         o.encounter_id = e.encounter_id "
            + "                                        INNER JOIN "
            + "                                                   ( "
            + "                                                              SELECT     p.patient_id, "
            + "                                                                         max(e.encounter_datetime) AS encounter_date "
            + "                                                              FROM       patient p "
            + "                                                              INNER JOIN encounter e "
            + "                                                              ON         e.patient_id = p.patient_id "
            + "                                                              INNER JOIN obs o "
            + "                                                              ON         o.encounter_id =e.encounter_id "
            + "                                                              WHERE      p.voided = 0 "
            + "                                                              AND        e.voided = 0 "
            + "                                                              AND        o.voided = 0 "
            + "                                                              AND        e.encounter_datetime BETWEEN :startDate AND        :endDate "
            + "                                                              AND        e.location_id = :location "
            + "                                                              AND        e.encounter_type = ${28} "
            + "                                                              AND        o.concept_id = ${2094} "
            + "                                                              AND        o.value_coded IN (${703}, "
            + "                                                                                           ${2093}, "
            + "                                                                                           ${664}) "
            + "                                                              GROUP BY   p.patient_id) AS last_result "
            + "                                        ON         last_result.patient_id = p.patient_id "
            + "                                        WHERE      p.voided = 0 "
            + "                                        AND        e.voided = 0 "
            + "                                        AND        o.voided = 0 "
            + "                                        AND        e.encounter_type = ${28} "
            + "                                        AND        e.encounter_datetime = last_result.encounter_date "
            + "                                        AND        e.location_id = :location "
            + "                                        AND        o.concept_id = ${2094} "
            + "                                        AND        o.value_coded = ${703} "
            + "                                        GROUP BY   p.patient_id ) positive_via "
            + "                  WHERE      e.voided = 0 "
            + "                  AND        o.voided = 0 "
            + "                  AND        e.encounter_type = ${28} "
            + "                  AND        e.location_id = :location "
            + "                  AND        o.concept_id = ${2149} "
            + "                  AND        o.value_coded IN (${23970}, "
            + "                                               ${23972}, "
            + "                                               ${23973}) "
            + "                  AND        o.obs_datetime BETWEEN positive_via.last_positive_encounter AND        :endDate "
            + "                  AND        cryotherapy.patient_id = e.patient_id "
            + "                  AND        e.encounter_datetime >= cryotherapy.last_cryotherapy "
            + "                  AND        e.encounter_datetime <= :endDate )";

    StringSubstitutor sb = new StringSubstitutor(map);

    cd.setQuery(sb.replace(query));

    return cd;
  }

  public CohortDefinition get1stTimeScreenedPatientsWithCryotherapy() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("1st Time Screened With Cryotherapy ");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition firstTimeScreened = getFirstTimeScreened();
    CohortDefinition cryotherapy = getPatientsWhoHaveCryotherapyAsLastTreatmentType();

    cd.addSearch("firstTimeScreened", EptsReportUtils.map(firstTimeScreened, MAPPINGS));
    cd.addSearch("cryotherapy", EptsReportUtils.map(cryotherapy, MAPPINGS));

    cd.setCompositionString("firstTimeScreened AND cryotherapy");
    return cd;
  }

  public CohortDefinition get1stTimeScreenedPatientsWithThermocoagulation() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("1st Time Screened With Thermocoagulation ");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition firstTimeScreened = getFirstTimeScreened();
    CohortDefinition thermocoagulation = getPatientsWhoHaveThermocoagulationAsLastTreatmentType();

    cd.addSearch("firstTimeScreened", EptsReportUtils.map(firstTimeScreened, MAPPINGS));
    cd.addSearch("thermocoagulation", EptsReportUtils.map(thermocoagulation, MAPPINGS));

    cd.setCompositionString("firstTimeScreened AND thermocoagulation");
    return cd;
  }

  public CohortDefinition get1stTimeScreenedPatientsWithLeepOrConization() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("1st Time Screened With Leep Or Conization ");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition firstTimeScreened = getFirstTimeScreened();
    CohortDefinition leepOrConization = getPatientsWhoHaveLeepOrConizationTreatmentType();

    cd.addSearch("firstTimeScreened", EptsReportUtils.map(firstTimeScreened, MAPPINGS));
    cd.addSearch("leepOrConization", EptsReportUtils.map(leepOrConization, MAPPINGS));

    cd.setCompositionString("firstTimeScreened AND leepOrConization");
    return cd;
  }

  public CohortDefinition getRescreenedAfterPreviousNegativePatientsWithCryotherapy() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Rescreened After Previous Negative Patients With Cryotherapy ");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition rescreenedAfterPreviousNegative = getRescreenedAfterPreviousNegative();
    CohortDefinition cryotherapy = getPatientsWhoHaveCryotherapyAsLastTreatmentType();

    cd.addSearch(
        "rescreenedAfterPreviousNegative",
        EptsReportUtils.map(rescreenedAfterPreviousNegative, MAPPINGS));
    cd.addSearch("cryotherapy", EptsReportUtils.map(cryotherapy, MAPPINGS));

    cd.setCompositionString("rescreenedAfterPreviousNegative AND cryotherapy");
    return cd;
  }

  public CohortDefinition getRescreenedAfterPreviousNegativePatientsWithThermocoagulation() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Rescreened After Previous Negative Patients With Thermocoagulation ");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition rescreenedAfterPreviousNegative = getRescreenedAfterPreviousNegative();
    CohortDefinition thermocoagulation = getPatientsWhoHaveThermocoagulationAsLastTreatmentType();

    cd.addSearch(
        "rescreenedAfterPreviousNegative",
        EptsReportUtils.map(rescreenedAfterPreviousNegative, MAPPINGS));
    cd.addSearch("thermocoagulation", EptsReportUtils.map(thermocoagulation, MAPPINGS));

    cd.setCompositionString("rescreenedAfterPreviousNegative AND thermocoagulation");
    return cd;
  }

  public CohortDefinition getRescreenedAfterPreviousNegativePatientsWithLeepOrConization() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Rescreened After Previous Negative Patients With Leep Or Conization ");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition rescreenedAfterPreviousNegative = getRescreenedAfterPreviousNegative();
    CohortDefinition leepOrConization = getPatientsWhoHaveLeepOrConizationTreatmentType();

    cd.addSearch(
        "rescreenedAfterPreviousNegative",
        EptsReportUtils.map(rescreenedAfterPreviousNegative, MAPPINGS));
    cd.addSearch("leepOrConization", EptsReportUtils.map(leepOrConization, MAPPINGS));

    cd.setCompositionString("rescreenedAfterPreviousNegative AND leepOrConization");
    return cd;
  }

  public CohortDefinition getPostTreatmentFollowUpPatientsWithCryotherapy() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Post Treatment Follow-Up Patients With Cryotherapy ");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition postTreatmentFollowUp = getPatientsWithPostTreatmentFollowUp();
    CohortDefinition cryotherapy = getPatientsWhoHaveCryotherapyAsLastTreatmentType();

    cd.addSearch("postTreatmentFollowUp", EptsReportUtils.map(postTreatmentFollowUp, MAPPINGS));
    cd.addSearch("cryotherapy", EptsReportUtils.map(cryotherapy, MAPPINGS));

    cd.setCompositionString("postTreatmentFollowUp AND cryotherapy");
    return cd;
  }

  public CohortDefinition getPostTreatmentFollowUpPatientsWithThermocoagulation() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("PostTreatmentFollowUp Patients With Thermocoagulation ");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition postTreatmentFollowUp = getPatientsWithPostTreatmentFollowUp();
    CohortDefinition thermocoagulation = getPatientsWhoHaveThermocoagulationAsLastTreatmentType();

    cd.addSearch("postTreatmentFollowUp", EptsReportUtils.map(postTreatmentFollowUp, MAPPINGS));
    cd.addSearch("thermocoagulation", EptsReportUtils.map(thermocoagulation, MAPPINGS));

    cd.setCompositionString("postTreatmentFollowUp AND thermocoagulation");
    return cd;
  }

  public CohortDefinition getPostTreatmentFollowUpPatientsWithLeepOrConization() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Post Treatment Follow-Up Patients With Leep Or Conization ");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition postTreatmentFollowUp = getPatientsWithPostTreatmentFollowUp();
    CohortDefinition leepOrConization = getPatientsWhoHaveLeepOrConizationTreatmentType();

    cd.addSearch("postTreatmentFollowUp", EptsReportUtils.map(postTreatmentFollowUp, MAPPINGS));
    cd.addSearch("leepOrConization", EptsReportUtils.map(leepOrConization, MAPPINGS));

    cd.setCompositionString("postTreatmentFollowUp AND leepOrConization");
    return cd;
  }
}
