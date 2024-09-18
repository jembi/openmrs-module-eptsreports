package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.eptsreports.reporting.library.queries.CommonQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsQueriesUtil;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AdvancedDiseaseAndTBCascadeCohortQueries {

  private HivMetadata hivMetadata;
  private TbMetadata tbMetadata;
  private TXTBCohortQueries txtbCohortQueries;
  private ListOfPatientsArtCohortCohortQueries listOfPatientsArtCohortCohortQueries;
  private TransferredInCohortQueries transferredInCohortQueries;
  private ResumoMensalCohortQueries resumoMensalCohortQueries;
  private AgeCohortQueries ageCohortQueries;
  private final CommonQueries commonQueries;

  private final String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";

  @Autowired
  public AdvancedDiseaseAndTBCascadeCohortQueries(
      HivMetadata hivMetadata,
      TbMetadata tbMetadata,
      ListOfPatientsArtCohortCohortQueries listOfPatientsArtCohortCohortQueries,
      TXTBCohortQueries txtbCohortQueries,
      ResumoMensalCohortQueries resumoMensalCohortQueries,
      TransferredInCohortQueries transferredInCohortQueries,
      AgeCohortQueries ageCohortQueries,
      CommonQueries commonQueries) {

    this.hivMetadata = hivMetadata;
    this.tbMetadata = tbMetadata;
    this.listOfPatientsArtCohortCohortQueries = listOfPatientsArtCohortCohortQueries;
    this.txtbCohortQueries = txtbCohortQueries;
    this.resumoMensalCohortQueries = resumoMensalCohortQueries;
    this.transferredInCohortQueries = transferredInCohortQueries;
    this.ageCohortQueries = ageCohortQueries;
    this.commonQueries = commonQueries;
  }

  /**
   * Number of clients who are eligible for CD4 count request during the inclusion period
   * (TB_DA_FR11)
   *
   * @return CohortDefinition
   */
  public CohortDefinition getClientsEligibleForCd4() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("clients who are eligible for CD4 count request");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition initiatedArt = listOfPatientsArtCohortCohortQueries.getPatientsInitiatedART();
    CohortDefinition pregnant = getPregnantsWithoutCD4Composition();
    CohortDefinition consecutiveVL = getPatientsWithTwoConsecutiveVLGreaterThan1000();
    CohortDefinition reinitiatedArt = getPatientsWhoReinitiatedArt();
    CohortDefinition exclusion = getPatientsTransferredOutOrDead();

    cd.addSearch("initiatedArt", EptsReportUtils.map(initiatedArt, mappings));
    cd.addSearch("pregnant", EptsReportUtils.map(pregnant, mappings));
    cd.addSearch("consecutiveVL", EptsReportUtils.map(consecutiveVL, mappings));
    cd.addSearch("reinitiatedArt", EptsReportUtils.map(reinitiatedArt, mappings));
    cd.addSearch(
        "exclusion",
        EptsReportUtils.map(exclusion, "endDate=${generationDate},location=${location}"));

    cd.setCompositionString(
        "(initiatedArt OR pregnant OR consecutiveVL OR reinitiatedArt) AND NOT exclusion");

    return cd;
  }

  /**
   * TB_DA_FR18 Number of clients with a CD4 count during inclusion period
   *
   * @return CohortDefinition
   */
  public CohortDefinition getClientsWithCd4Count() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("clients with a CD4 count");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Facility", Location.class));

    CohortDefinition eligibleCd4 = getClientsEligibleForCd4();
    CohortDefinition anyResult = getPatientsWithCD4Count();

    cd.addSearch("eligibleCd4", EptsReportUtils.map(eligibleCd4, mappings));
    cd.addSearch("anyResult", EptsReportUtils.map(anyResult, mappings));

    cd.setCompositionString("eligibleCd4 AND anyResult");

    return cd;
  }

  public CohortDefinition getPatientsTransferredOutOrDead() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("clients transferred out and Dead");
    cd.addParameter(new Parameter("location", "Facility", Location.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));

    CohortDefinition transferredOut = transferredInCohortQueries.getTrfOut();
    CohortDefinition death = resumoMensalCohortQueries.getPatientsWhoDied(false);

    cd.addSearch(
        "transferredOut",
        EptsReportUtils.map(transferredOut, "startDate=${endDate},location=${location}"));
    cd.addSearch(
        "death", EptsReportUtils.map(death, "onOrBefore=${endDate},locationList=${location}"));
    cd.setCompositionString("transferredOut OR death");

    return cd;
  }

  /**
   * Number of clients with CD4 count during inclusion period showing severe immunodepression
   * (TB_DA_FR19)
   *
   * @return CohortDefinition
   */
  public CohortDefinition getEligibleClientsWithSevereImmunosuppression() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("showing severe immunodepression");
    cd.addParameter(new Parameter("location", "Facility", Location.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));

    CohortDefinition cd4Count = getClientsWithCd4Count();
    CohortDefinition cd200AgeFiveOrOver =
        getPatientsWithCd4AndAge(Cd4CountComparison.LessThanOrEqualTo200mm3AA, 5, null);
    CohortDefinition cd500AgeBetweenOneAndFour =
        getPatientsWithCd4AndAge(Cd4CountComparison.LessThanOrEqualTo500mm3, 1, 4);
    CohortDefinition cd750AgeUnderYear =
        getPatientsWithCd4AndAge(Cd4CountComparison.LessThanOrEqualTo750mm3, null, 1);

    cd.addSearch("cd4Count", EptsReportUtils.map(cd4Count, mappings));
    cd.addSearch("cd4Under200", EptsReportUtils.map(cd200AgeFiveOrOver, mappings));
    cd.addSearch("cd4Under500", EptsReportUtils.map(cd500AgeBetweenOneAndFour, mappings));
    cd.addSearch("cd4Under750", EptsReportUtils.map(cd750AgeUnderYear, mappings));

    cd.setCompositionString("(cd4Under200 OR cd4Under500 OR cd4Under750) AND cd4Count");

    return cd;
  }

  /**
   * Clients With Cd4 count and TB Lam Result by report generation date
   *
   * @return CohortDefinition
   */
  public CohortDefinition getEligibleClientsWithSevereImmunosuppressionAndTbLamResult() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Clients With Cd4 count and TB Lam Result ");
    cd.addParameter(new Parameter("location", "Facility", Location.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));

    CohortDefinition severeImmunosuppression = getEligibleClientsWithSevereImmunosuppression();

    CohortDefinition anyTbLam = getPatientsWithAnyTbLamResult();

    cd.addSearch("severeImmunosuppression", EptsReportUtils.map(severeImmunosuppression, mappings));
    cd.addSearch(
        "anyTbLam",
        EptsReportUtils.map(
            anyTbLam, "startDate=${endDate-2m+1d},endDate=${generationDate},location=${location}"));

    cd.setCompositionString("severeImmunosuppression AND anyTbLam");

    return cd;
  }

  /**
   * Number of clients with CD4 count showing severe immunosuppression during the inclusion period
   *
   * @return CohortDefinition
   */
  public CohortDefinition getClientsWithSevereImmunosuppression() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Number of clients with CD4 count showing immunosuppression");
    cd.addParameter(new Parameter("location", "Facility", Location.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));

    CohortDefinition cd4Count = getPatientsWithCD4Count();
    CohortDefinition cd200AgeFiveOrOver =
        getPatientsWithCd4AndAge(Cd4CountComparison.LessThanOrEqualTo200mm3AA, 5, null);
    CohortDefinition cd500AgeBetweenOneAndFour =
        getPatientsWithCd4AndAge(Cd4CountComparison.LessThanOrEqualTo500mm3, 1, 4);
    CohortDefinition cd750AgeUnderYear =
        getPatientsWithCd4AndAge(Cd4CountComparison.LessThanOrEqualTo750mm3, null, 1);

    cd.addSearch("cd4Count", EptsReportUtils.map(cd4Count, mappings));
    cd.addSearch("cd4Under200", EptsReportUtils.map(cd200AgeFiveOrOver, mappings));
    cd.addSearch("cd4Under500", EptsReportUtils.map(cd500AgeBetweenOneAndFour, mappings));
    cd.addSearch("cd4Under750", EptsReportUtils.map(cd750AgeUnderYear, mappings));

    cd.setCompositionString("cd4Count AND (cd4Under200 OR cd4Under500 OR cd4Under750)");

    return cd;
  }

  /**
   * Number of clients with CD4 count showing immunosuppression and with TB LAM results during
   * inclusion period
   *
   * @return CohortDefinition
   */
  public CohortDefinition getClientsWithSevereImmunosuppressionAndWithTbLamResult() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "Number of clients with CD4 count showing immunosuppression and with TB LAM results");
    cd.addParameter(new Parameter("location", "Facility", Location.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));

    CohortDefinition clientsWithSevereImmunosuppression = getClientsWithSevereImmunosuppression();
    CohortDefinition anyTbLam = getPatientsWithAnyTbLamResult();
    CohortDefinition exclusion = getPatientsTransferredOutOrDead();

    cd.addSearch(
        "clientsWithSevereImmunosuppression",
        EptsReportUtils.map(clientsWithSevereImmunosuppression, mappings));
    cd.addSearch("anyTbLam", EptsReportUtils.map(anyTbLam, mappings));
    cd.addSearch(
        "exclusion",
        EptsReportUtils.map(exclusion, "endDate=${generationDate},location=${location}"));

    cd.setCompositionString("(clientsWithSevereImmunosuppression AND anyTbLam) AND NOT exclusion");

    return cd;
  }

  /**
   * Number of clients with positive TB LAM result during the inclusion period
   *
   * @return CohortDefinition
   */
  public CohortDefinition getClientsWithSevereImmunodepressionAndWithTbLamPositiveResult() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Number of clients with positive TB LAM result during");
    cd.addParameter(new Parameter("location", "Facility", Location.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));

    CohortDefinition severeImmunosuppressionAndWithTbLamResult =
        getClientsWithSevereImmunosuppressionAndWithTbLamResult();

    CohortDefinition positiveTbLam = getPatientsWithTbLamResult(TbLamResult.POSITIVE);

    cd.addSearch(
        "severeImmunosuppressionAndWithTbLamResult",
        EptsReportUtils.map(severeImmunosuppressionAndWithTbLamResult, mappings));
    cd.addSearch("positiveTbLam", EptsReportUtils.map(positiveTbLam, mappings));

    cd.setCompositionString("severeImmunosuppressionAndWithTbLamResult AND positiveTbLam");

    return cd;
  }

  /**
   * Number of clients with negative TB LAM result during the inclusion period
   *
   * @return CohortDefinition
   */
  public CohortDefinition getClientsWithSevereImmunodepressionAndWithTbLamNegativeResult() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Number of clients with negative TB LAM result during");
    cd.addParameter(new Parameter("location", "Facility", Location.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));

    CohortDefinition severeImmunosuppressionAndWithTbLamResult =
        getClientsWithSevereImmunosuppressionAndWithTbLamResult();

    CohortDefinition negativeTbLam = getPatientsWithTbLamResult(TbLamResult.NEGATIVE);

    cd.addSearch(
        "severeImmunosuppressionAndWithTbLamResult",
        EptsReportUtils.map(severeImmunosuppressionAndWithTbLamResult, mappings));
    cd.addSearch("negativeTbLam", EptsReportUtils.map(negativeTbLam, mappings));

    cd.setCompositionString("severeImmunosuppressionAndWithTbLamResult AND negativeTbLam");

    return cd;
  }

  /**
   * Number of clients with positive TB LAM result during the inclusion period
   *
   * @return CohortDefinition
   */
  public CohortDefinition
      getClientsWithSevereImmunodepressionAndWithTbLamPositiveResultAndNotTestedForGeneXpert() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Number of clients with positive TB LAM result");
    cd.addParameter(new Parameter("location", "Facility", Location.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));

    CohortDefinition severeImmunodepressionAndWithTbLamPositiveResult =
        getClientsWithSevereImmunodepressionAndWithTbLamPositiveResult();

    CohortDefinition genXpert = getPatientsWithAnyGeneXpertResult();

    cd.addSearch(
        "severeImmunodepressionAndWithTbLamPositiveResult",
        EptsReportUtils.map(severeImmunodepressionAndWithTbLamPositiveResult, mappings));
    cd.addSearch("genXpert", EptsReportUtils.map(genXpert, mappings));

    cd.setCompositionString("severeImmunodepressionAndWithTbLamPositiveResult AND NOT genXpert");

    return cd;
  }

  /**
   * Clients with positive TB LAM result during the inclusion period and also tested with GeneXpert
   * by report generation date
   *
   * @return CohortDefinition
   */
  public CohortDefinition
      getClientsWithSevereImmunodepressionAndWithTbLamPositiveResultAndTestedForGeneXpert() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "Clients with positive TB LAM result during the inclusion period and also tested with GeneXpert");
    cd.addParameter(new Parameter("location", "Facility", Location.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));

    CohortDefinition severeImmunodepressionAndWithTbLamPositiveResult =
        getClientsWithSevereImmunodepressionAndWithTbLamPositiveResult();

    CohortDefinition genXpert = getPatientsWithAnyGeneXpertResult();

    cd.addSearch(
        "severeImmunodepressionAndWithTbLamPositiveResult",
        EptsReportUtils.map(severeImmunodepressionAndWithTbLamPositiveResult, mappings));
    cd.addSearch("genXpert", EptsReportUtils.map(genXpert, mappings));

    cd.setCompositionString("severeImmunodepressionAndWithTbLamPositiveResult AND genXpert");

    return cd;
  }

  /**
   * Clients with positive TB LAM during inclusion period and GeneXpert positive for TB by report
   * generation date
   *
   * @return CohortDefinition
   */
  public CohortDefinition
      getClientsWithSevereImmunodepressionAndWithTbLamPositiveResultAndPositiveGeneXpert() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "Clients with positive TB LAM during inclusion period and GeneXpert positive for TB");
    cd.addParameter(new Parameter("location", "Facility", Location.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));

    CohortDefinition severeImmunodepressionAndWithTbLamPositiveResultAndTestedForGeneXpert =
        getClientsWithSevereImmunodepressionAndWithTbLamPositiveResultAndTestedForGeneXpert();

    CohortDefinition positiveGen = getPatientsWithPositiveGeneXpertResult();

    cd.addSearch(
        "severeImmunodepressionAndWithTbLamPositiveResultAndTestedForGeneXpert",
        EptsReportUtils.map(
            severeImmunodepressionAndWithTbLamPositiveResultAndTestedForGeneXpert, mappings));
    cd.addSearch(
        "positiveGen",
        EptsReportUtils.map(
            positiveGen,
            "startDate=${endDate-2m+1d},endDate=${generationDate},location=${location}"));

    cd.setCompositionString(
        "severeImmunodepressionAndWithTbLamPositiveResultAndTestedForGeneXpert AND positiveGen");

    return cd;
  }

  /**
   * Clients with positive TB LAM during inclusion period and on TB treatment by report generation
   * date
   *
   * @return CohortDefinition
   */
  public CohortDefinition
      getClientsWithSevereImmunodepressionAndWithTbLamPositiveResultAndOnTreatment() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Clients with positive TB LAM during inclusion period and on TB treatment");
    cd.addParameter(new Parameter("location", "Facility", Location.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));

    CohortDefinition severeImmunodepressionAndWithTbLamPositiveResult =
        getClientsWithSevereImmunodepressionAndWithTbLamPositiveResult();

    CohortDefinition onTb = getPatientsOnTbTreatment();

    cd.addSearch(
        "severeImmunodepressionAndWithTbLamPositiveResult",
        EptsReportUtils.map(severeImmunodepressionAndWithTbLamPositiveResult, mappings));
    cd.addSearch("onTb", EptsReportUtils.map(onTb, mappings));

    cd.setCompositionString("severeImmunodepressionAndWithTbLamPositiveResult AND onTb");

    return cd;
  }
  /**
   * @param cd4 - Absolute CD4 count
   * @param minAge minimum age of patient base on effective date
   * @param maxAge maximum age of patent base on effective date
   * @return CohortDefinition
   */
  private CohortDefinition getPatientsWithCd4AndAge(
      Cd4CountComparison cd4, Integer minAge, Integer maxAge) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Absolute Cd4");
    cd.addParameter(new Parameter("location", "Facility", Location.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));

    CohortDefinition absoluteCd4 = getPatientsWithAbsoluteCd4Count(cd4);
    CohortDefinition age = ageCohortQueries.createXtoYAgeCohort("Age", minAge, maxAge);

    cd.addSearch("absoluteCd4", EptsReportUtils.map(absoluteCd4, mappings));

    cd.addSearch("age", EptsReportUtils.map(age, "effectiveDate=${endDate}"));

    cd.setCompositionString("absoluteCd4 AND age");

    return cd;
  }

  /**
   * <b>The system will include all clients who reinitiated ART during the inclusion period who
   * have:</b>
   *
   * <ul>
   *   <li>The most recent “Mudança de Estado de Permanência” state marked as “Reinício” in Ficha
   *       Clínica, with the state date falling during the inclusion period.
   *   <li>OR The most recent “Mudança de Estado de Permanência” state marked as “Reinício” in Ficha
   *       Resumo, with the state date falling during the inclusion period.
   * </ul>
   *
   * @return CohortDefinition *
   */
  public CohortDefinition getPatientsWhoReinitiatedArt() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Clients who reinitiated ART during the inclusion period");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "End Date", Location.class));

    String query = getRestartedArtQuery(false);

    StringSubstitutor sb = new StringSubstitutor(getMetadata());
    cd.setQuery(sb.replace(query));

    return cd;
  }

  /**
   *
   *
   * <ul>
   *   <li>Absolute CD4 count results (CD4 Absoluto= ANY RESULT) registered in the Ficha Clinica –
   *       Mastercard during the inclusion periodor
   *   <li>CD4 count result (CD4 absoluto - Último CD4= ANY RESULT) registered in the Ficha Resumo –
   *       Mastercard with the result date during the inclusion period or
   *   <li>Absolute CD4 count results (CD4 Absoluto= ANY RESULT) registered in Laboratory form
   *       during the inclusion period or
   *   <li>Absolute CD4 count results (CD4 Absoluto= ANY RESULT) registered in the Electronic Lab
   *       form during the inclusion period or
   *   <li>Absolute CD4 count results (CD4 Absoluto= ANY RESULT) registered in the Ficha DAH with
   *       the result date during the inclusion period
   * </ul>
   *
   * <p>Note: For clients with more than one CD4 count result registered within this period, the
   * most recent result will be considered
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWithCD4Count() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Number of clients with a CD4 count during inclusion period");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "End Date", Location.class));

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "         INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "         INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "         INNER JOIN ( "
            + getEligibilityDateQuery()
            + "         ) eligible ON eligible.patient_id = p.patient_id  "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.encounter_type IN ( ${6}, ${13}, ${51} ) "
            + "                          AND ((o.concept_id = ${1695} "
            + "                          AND o.value_numeric IS NOT NULL)"
            + "                             OR (o.concept_id = ${165515} AND o.value_coded IS NOT NULL ) ) "
            + "                          AND e.location_id = :location "
            + "                          AND DATE(e.encounter_datetime) BETWEEN eligible.eligibility_date AND DATE_ADD(eligible.eligibility_date, INTERVAL 33 DAY) "
            + "                   GROUP  BY p.patient_id "
            + "                   UNION "
            + "SELECT p.patient_id "
            + "FROM   patient p "
            + "         INNER JOIN   encounter e ON e.patient_id = p.patient_id "
            + "         INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "         INNER JOIN ( "
            + getEligibilityDateQuery()
            + "         ) eligible ON eligible.patient_id = p.patient_id  "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.encounter_type IN ( ${53}, ${90} ) "
            + "                          AND ((o.concept_id = ${1695} "
            + "                          AND o.value_numeric IS NOT NULL)"
            + "                             OR (o.concept_id = ${165515} AND o.value_coded IS NOT NULL ) ) "
            + "                          AND e.location_id = :location "
            + "                          AND o.obs_datetime BETWEEN eligible.eligibility_date AND DATE_ADD(eligible.eligibility_date, INTERVAL 33 DAY)"
            + "                   GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(getMetadata());
    cd.setQuery(sb.replace(query));
    return cd;
  }

  /**
   * <b>Pregnant Women</b>
   *
   * <p>Client identified as Female AND:
   * <li>Is marked as “PREGNANT” in the initial consultation (Processo Clinico Parte A or Ficha
   *     Resumo – Master Card) or follow-up consultation (Ficha de Seguimento Adulto o r Ficha
   *     Clinica – Master Card) by during the period range or
   * <li>Has “Number of weeks Pregnant” registered in the initial or follow-up consultation
   *     (Processo Clinico Parte A or Ficha de Seguimento Adulto) during the period range or
   * <li>Has “Delivery Due Date” registered in the initial or follow-up consultation (Processo
   *     Clinico Parte A or Ficha de Seguimento Adulto) during the period range or
   * <li>Is enrolled on Prevention of the Vertical Transmission/Elimination of the Vertical
   *     Transmission (PTV/ETV) program within period range or
   * <li>Has started ART for being B+ as specified in “CRITÉRIO PARA INÍCIO DE TARV” in the
   *     follow-up consultations (Ficha de Seguimento) that occurred during the period range or
   * <li>Has “Actualmente encontra-se gravida” marked as “Sim” on e-Lab Form and Data de Colheita is
   *     during the period range.
   *
   *     <p>Excluding:
   * <li>All clients who have a CD4 result recorded after the first occurrence of pregnancy recorded
   *     during the period range from the sources listed above and before the end of Inclusion
   *     period
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPregnantWithoutCd4() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Pregnant patients without CD4 after pregnancy date");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(
        new Parameter("inclusionStartDate", "inclusionStartDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));
    Map<String, Integer> map = new HashMap<>();

    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("5", hivMetadata.getARVAdultInitialEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    map.put("1600", hivMetadata.getPregnancyDueDate().getConceptId());
    map.put("23821", hivMetadata.getSampleCollectionDateAndTime().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("6334", hivMetadata.getCriteriaForArtStart().getConceptId());
    map.put("8", hivMetadata.getPtvEtvProgram().getProgramId());
    map.put("1279", hivMetadata.getNumberOfWeeksPregnant().getConceptId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("6331", hivMetadata.getBPlusConcept().getConceptId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("5599", hivMetadata.getPriorDeliveryDateConcept().getConceptId());
    map.put("6332", hivMetadata.getBreastfeeding().getConceptId());
    map.put("27", hivMetadata.getPatientGaveBirthWorkflowState().getProgramWorkflowStateId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("165515", hivMetadata.getCD4SemiQuantitativeConcept().getConceptId());
    map.put("90", hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId());

    String query =
        " SELECT pregnant.patient_id "
            + " FROM   (SELECT pg.patient_id, "
            + "               MAX(pg.pregnancy_date) AS pg_date "
            + "        FROM   (SELECT p.patient_id, "
            + "                       e.encounter_datetime AS pregnancy_date "
            + "                FROM   patient p "
            + "                       INNER JOIN person p2 "
            + "                               ON p2.person_id = p.patient_id "
            + "                       INNER JOIN encounter e "
            + "                               ON e.patient_id = p.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON o.encounter_id = e.encounter_id "
            + "                WHERE  e.encounter_type IN ( ${5}, ${6} ) "
            + "                       AND p2.gender = 'F' "
            + "                       AND ( o.concept_id = ${1982} "
            + "                             AND o.value_coded = ${1065} ) "
            + "                       AND e.location_id = :location "
            + "                       AND p.voided = 0 "
            + "                       AND e.encounter_datetime BETWEEN "
            + "                           :startDate AND :endDate "
            + "                       AND p2.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       e.encounter_datetime AS pregnancy_date "
            + "                FROM   patient p "
            + "                       INNER JOIN person p2 "
            + "                               ON p2.person_id = p.patient_id "
            + "                       INNER JOIN encounter e "
            + "                               ON e.patient_id = p.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON o.encounter_id = e.encounter_id "
            + "                WHERE  p2.gender = 'F' "
            + "                       AND e.encounter_type IN ( ${5}, ${6} ) "
            + "                       AND o.concept_id = ${1279} "
            + "                       AND e.location_id = :location "
            + "                       AND p.voided = 0 "
            + "                       AND p2.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND e.encounter_datetime BETWEEN "
            + "                           :startDate AND :endDate "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       e.encounter_datetime AS pregnancy_date "
            + "                FROM   patient p "
            + "                       INNER JOIN person p2 "
            + "                               ON p2.person_id = p.patient_id "
            + "                       INNER JOIN encounter e "
            + "                               ON e.patient_id = p.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON o.encounter_id = e.encounter_id "
            + "                WHERE  p2.gender = 'F' "
            + "                       AND e.encounter_type IN ( ${5}, ${6} ) "
            + "                       AND o.concept_id = ${1600} "
            + "                       AND e.location_id = :location "
            + "                       AND p.voided = 0 "
            + "                       AND p2.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND e.encounter_datetime BETWEEN "
            + "                           :startDate AND :endDate "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       e.encounter_datetime AS pregnancy_date "
            + "                FROM   patient p "
            + "                       INNER JOIN person p2 "
            + "                               ON p2.person_id = p.patient_id "
            + "                       INNER JOIN encounter e "
            + "                               ON e.patient_id = p.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON o.encounter_id = e.encounter_id "
            + "                WHERE  p2.gender = 'F' "
            + "                       AND e.encounter_type = ${6} "
            + "                       AND ( o.concept_id = ${6334} "
            + "                             AND o.value_coded = ${6331} ) "
            + "                       AND e.location_id = :location "
            + "                       AND p.voided = 0 "
            + "                       AND p2.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND e.encounter_datetime BETWEEN "
            + "                           :startDate AND :endDate "
            + "                UNION "
            + "                SELECT pp.patient_id, "
            + "                       pp.date_enrolled AS pregnancy_date "
            + "                FROM   patient_program pp "
            + "                       INNER JOIN person p "
            + "                               ON p.person_id = pp.patient_id "
            + "                       INNER JOIN encounter e "
            + "                               ON e.patient_id = pp.patient_id "
            + "                WHERE  p.gender = 'F' "
            + "                       AND pp.program_id = ${8} "
            + "                       AND e.location_id = :location "
            + "                       AND p.voided = 0 "
            + "                       AND pp.voided = 0 "
            + "                       AND pp.date_enrolled BETWEEN "
            + "                           :startDate AND :endDate "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       o2.value_datetime AS pregnancy_date "
            + "                FROM   patient p "
            + "                       INNER JOIN person p2 "
            + "                               ON p2.person_id = p.patient_id "
            + "                       INNER JOIN encounter e "
            + "                               ON e.patient_id = p.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON o.encounter_id = e.encounter_id "
            + "                       INNER JOIN obs o2 "
            + "                               ON o2.encounter_id = e.encounter_id "
            + "                WHERE  p2.gender = 'F' "
            + "                       AND e.encounter_type = ${53} "
            + "                       AND ( o.concept_id = ${1982} "
            + "                             AND o.value_coded = ${1065} ) "
            + "                       AND ( o2.concept_id = ${1190} "
            + "                             AND o2.value_datetime BETWEEN "
            + "                                 :startDate AND :endDate ) "
            + "                       AND e.location_id = :location "
            + "                       AND p.voided = 0 "
            + "                       AND p2.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o2.voided = 0 "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       e.encounter_datetime AS pregnancy_date "
            + "                FROM   patient p "
            + "                       INNER JOIN person p2 "
            + "                               ON p2.person_id = p.patient_id "
            + "                       INNER JOIN encounter e "
            + "                               ON e.patient_id = p.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON o.encounter_id = e.encounter_id "
            + "                WHERE  p2.gender = 'F' "
            + "                       AND e.encounter_type = ${6} "
            + "                       AND ( o.concept_id = ${1982} "
            + "                             AND o.value_coded = ${1065} ) "
            + "                       AND e.location_id = :location "
            + "                       AND p.voided = 0 "
            + "                       AND p2.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND e.encounter_datetime BETWEEN "
            + "                           :startDate AND :endDate "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       Date(o2.value_datetime) AS pregnancy_date "
            + "                FROM   patient p "
            + "                       INNER JOIN person p2 "
            + "                               ON p2.person_id = p.patient_id "
            + "                       INNER JOIN encounter e "
            + "                               ON e.patient_id = p.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON o.encounter_id = e.encounter_id "
            + "                       INNER JOIN obs o2 "
            + "                               ON o2.encounter_id = e.encounter_id "
            + "                WHERE  p2.gender = 'F' "
            + "                       AND e.encounter_type = ${51} "
            + "                       AND ( o.concept_id = ${1982} "
            + "                             AND o.value_coded = ${1065} ) "
            + "                       AND ( o2.concept_id = ${23821} "
            + "                             AND Date(o2.value_datetime) BETWEEN "
            + "                                 :startDate AND :endDate "
            + "                           ) "
            + "                       AND e.location_id = :location "
            + "                       AND p.voided = 0 "
            + "                       AND p2.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o2.voided = 0) pg "
            + "        GROUP  BY pg.patient_id) pregnant "
            + "            LEFT JOIN (  "
            + "                SELECT bf.patient_id,  "
            + "                       MAX(bf.breastfeeding_date) AS breastfeeding_date  "
            + "                FROM   (  "
            + "                           SELECT e.patient_id, o.value_datetime AS breastfeeding_date  "
            + "                           FROM   encounter e  "
            + "                                      INNER JOIN obs o  "
            + "                                                 ON o.encounter_id = e.encounter_id  "
            + "                           WHERE  e.encounter_type IN ( ${5}, ${6} )  "
            + "                             AND  o.concept_id = ${5599}  "
            + "                             AND e.location_id = :location     "
            + "                             AND o.value_datetime >= :startDate and o.value_datetime <= :endDate  "
            + "                             AND e.voided = 0  "
            + "                             AND o.voided = 0  "
            + "                           UNION  "
            + "                           SELECT e.patient_id, e.encounter_datetime AS breastfeeding_date  "
            + "                           FROM   encounter e  "
            + "                                      INNER JOIN obs o  "
            + "                                                 ON o.encounter_id = e.encounter_id  "
            + "                           WHERE  e.encounter_type = ${6}  "
            + "                             AND e.location_id = :location  "
            + "                             AND ( o.concept_id =${6332}  "
            + "                               AND o.value_coded =${1065} )  "
            + "                             AND e.encounter_datetime >= :startDate AND e.encounter_datetime <= :endDate  "
            + "                             AND o.voided = 0  "
            + "                             AND e.voided = 0     "
            + "                           UNION  "
            + "                           SELECT e.patient_id, e.encounter_datetime AS breastfeeding_date  "
            + "                           FROM   encounter e  "
            + "                                      INNER JOIN obs o  "
            + "                                                 ON o.encounter_id = e.encounter_id  "
            + "                           WHERE  e.encounter_type IN ( ${5}, ${6} )  "
            + "                             AND o.concept_id = ${6334}  "
            + "                             AND o.value_coded =${6332}  "
            + "                             AND e.voided = 0  "
            + "                             AND o.voided = 0     "
            + "                             AND e.location_id = :location  "
            + "                             AND e.encounter_datetime >= :startDate  "
            + "                             AND e.encounter_datetime <= :endDate  "
            + "                           UNION  "
            + "                           SELECT pp.patient_id, ps.start_date AS breastfeeding_date  "
            + "                           FROM   patient_program pp  "
            + "                                      INNER JOIN patient_state ps  "
            + "                                                 ON ps.patient_program_id =  "
            + "                                                    pp.patient_program_id  "
            + "                           WHERE  pp.program_id =${8}  "
            + "                             AND ps.state =${27}  "
            + "                             AND pp.location_id = :location  "
            + "                             AND pp.voided = 0  "
            + "                             AND ps.voided = 0  "
            + "                             AND ps.start_date >= :startDate  "
            + "                             AND ps.start_date <= :endDate  "
            + "                           UNION  "
            + "                           SELECT e.patient_id, o2.value_datetime AS breastfeeding_date  "
            + "                           FROM   encounter e  "
            + "                                      INNER JOIN obs o  "
            + "                                                 ON o.encounter_id = e.encounter_id  "
            + "                                      INNER JOIN obs o2  "
            + "                                                 ON o2.encounter_id = e.encounter_id  "
            + "                           WHERE  e.encounter_type =${53}  "
            + "                             AND (o.concept_id =${6332}  "
            + "                             AND o.value_coded =${1065} )    "
            + "                             AND ( o2.concept_id = ${1190} "
            + "                               AND o2.value_datetime BETWEEN :startDate AND :endDate )     "
            + "                             AND e.location_id = :location  "
            + "                             AND e.voided = 0  "
            + "                             AND o.voided = 0  "
            + "                           UNION  "
            + "                           SELECT e.patient_id, DATE(o2.value_datetime) AS breastfeeding_date  "
            + "                           FROM   encounter e  "
            + "                                      INNER JOIN obs o  "
            + "                                                 ON o.encounter_id = e.encounter_id  "
            + "                                      INNER JOIN obs o2  "
            + "                                                 ON o2.encounter_id = e.encounter_id  "
            + "                           WHERE  e.encounter_type = ${51}  "
            + "                             AND o.concept_id =${6332}     "
            + "                             AND o.value_coded =${1065}  "
            + "                             AND o2.concept_id = ${23821}  "
            + "                             AND DATE(o2.value_datetime) >= :startDate AND DATE(o2.value_datetime) <= :endDate  "
            + "                             AND e.voided = 0  "
            + "                             AND o.voided = 0  "
            + "                             AND o2.voided = 0  "
            + "                             AND e.location_id =:location  "
            + "                       ) bf  "
            + "                GROUP  BY bf.patient_id  "
            + "            ) breastfeeding ON pregnant.patient_id = breastfeeding.patient_id  "
            + "            WHERE ((pregnant.pg_date IS NOT NULL  "
            + "                       AND pregnant.pg_date >= breastfeeding.breastfeeding_date)  "
            + "                 OR (breastfeeding.breastfeeding_date IS NULL) )";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    String mappedQuery = stringSubstitutor.replace(query);
    sqlCohortDefinition.setQuery(mappedQuery);
    return sqlCohortDefinition;
  }

  /**
   * All patients with CD4 results after the first occurrence of pregnancy and before the inclusion
   * period
   *
   * @return
   */
  public CohortDefinition getPregnantWitCd4AfterPregnancy() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Pregnant patients without CD4 after pregnancy date");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(
        new Parameter("inclusionStartDate", "inclusionStartDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));
    Map<String, Integer> map = new HashMap<>();

    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("5", hivMetadata.getARVAdultInitialEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    map.put("1600", hivMetadata.getPregnancyDueDate().getConceptId());
    map.put("23821", hivMetadata.getSampleCollectionDateAndTime().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("6334", hivMetadata.getCriteriaForArtStart().getConceptId());
    map.put("8", hivMetadata.getPtvEtvProgram().getProgramId());
    map.put("1279", hivMetadata.getNumberOfWeeksPregnant().getConceptId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("6331", hivMetadata.getBPlusConcept().getConceptId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("5599", hivMetadata.getPriorDeliveryDateConcept().getConceptId());
    map.put("6332", hivMetadata.getBreastfeeding().getConceptId());
    map.put("27", hivMetadata.getPatientGaveBirthWorkflowState().getProgramWorkflowStateId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("165515", hivMetadata.getCD4SemiQuantitativeConcept().getConceptId());
    map.put("90", hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId());

    String query =
        " SELECT pregnant.patient_id "
            + " FROM   (SELECT pg.patient_id, "
            + "               MIN(pg.pregnancy_date) AS pg_date "
            + "        FROM   (SELECT p.patient_id, "
            + "                       e.encounter_datetime AS pregnancy_date "
            + "                FROM   patient p "
            + "                       INNER JOIN person p2 "
            + "                               ON p2.person_id = p.patient_id "
            + "                       INNER JOIN encounter e "
            + "                               ON e.patient_id = p.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON o.encounter_id = e.encounter_id "
            + "                WHERE  e.encounter_type IN ( ${5}, ${6} ) "
            + "                       AND p2.gender = 'F' "
            + "                       AND ( o.concept_id = ${1982} "
            + "                             AND o.value_coded = ${1065} ) "
            + "                       AND e.location_id = :location "
            + "                       AND p.voided = 0 "
            + "                       AND e.encounter_datetime BETWEEN "
            + "                           :startDate AND :endDate "
            + "                       AND p2.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       e.encounter_datetime AS pregnancy_date "
            + "                FROM   patient p "
            + "                       INNER JOIN person p2 "
            + "                               ON p2.person_id = p.patient_id "
            + "                       INNER JOIN encounter e "
            + "                               ON e.patient_id = p.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON o.encounter_id = e.encounter_id "
            + "                WHERE  p2.gender = 'F' "
            + "                       AND e.encounter_type IN ( ${5}, ${6} ) "
            + "                       AND o.concept_id = ${1279} "
            + "                       AND e.location_id = :location "
            + "                       AND p.voided = 0 "
            + "                       AND p2.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND e.encounter_datetime BETWEEN "
            + "                           :startDate AND :endDate "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       e.encounter_datetime AS pregnancy_date "
            + "                FROM   patient p "
            + "                       INNER JOIN person p2 "
            + "                               ON p2.person_id = p.patient_id "
            + "                       INNER JOIN encounter e "
            + "                               ON e.patient_id = p.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON o.encounter_id = e.encounter_id "
            + "                WHERE  p2.gender = 'F' "
            + "                       AND e.encounter_type IN ( ${5}, ${6} ) "
            + "                       AND o.concept_id = ${1600} "
            + "                       AND e.location_id = :location "
            + "                       AND p.voided = 0 "
            + "                       AND p2.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND e.encounter_datetime BETWEEN "
            + "                           :startDate AND :endDate "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       e.encounter_datetime AS pregnancy_date "
            + "                FROM   patient p "
            + "                       INNER JOIN person p2 "
            + "                               ON p2.person_id = p.patient_id "
            + "                       INNER JOIN encounter e "
            + "                               ON e.patient_id = p.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON o.encounter_id = e.encounter_id "
            + "                WHERE  p2.gender = 'F' "
            + "                       AND e.encounter_type = ${6} "
            + "                       AND ( o.concept_id = ${6334} "
            + "                             AND o.value_coded = ${6331} ) "
            + "                       AND e.location_id = :location "
            + "                       AND p.voided = 0 "
            + "                       AND p2.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND e.encounter_datetime BETWEEN "
            + "                           :startDate AND :endDate "
            + "                UNION "
            + "                SELECT pp.patient_id, "
            + "                       pp.date_enrolled AS pregnancy_date "
            + "                FROM   patient_program pp "
            + "                       INNER JOIN person p "
            + "                               ON p.person_id = pp.patient_id "
            + "                       INNER JOIN encounter e "
            + "                               ON e.patient_id = pp.patient_id "
            + "                WHERE  p.gender = 'F' "
            + "                       AND pp.program_id = ${8} "
            + "                       AND e.location_id = :location "
            + "                       AND p.voided = 0 "
            + "                       AND pp.voided = 0 "
            + "                       AND pp.date_enrolled BETWEEN "
            + "                           :startDate AND :endDate "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       o2.value_datetime AS pregnancy_date "
            + "                FROM   patient p "
            + "                       INNER JOIN person p2 "
            + "                               ON p2.person_id = p.patient_id "
            + "                       INNER JOIN encounter e "
            + "                               ON e.patient_id = p.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON o.encounter_id = e.encounter_id "
            + "                       INNER JOIN obs o2 "
            + "                               ON o2.encounter_id = e.encounter_id "
            + "                WHERE  p2.gender = 'F' "
            + "                       AND e.encounter_type = ${53} "
            + "                       AND ( o.concept_id = ${1982} "
            + "                             AND o.value_coded = ${1065} ) "
            + "                       AND ( o2.concept_id = ${1190} "
            + "                             AND o2.value_datetime BETWEEN "
            + "                                 :startDate AND :endDate ) "
            + "                       AND e.location_id = :location "
            + "                       AND p.voided = 0 "
            + "                       AND p2.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o2.voided = 0 "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       e.encounter_datetime AS pregnancy_date "
            + "                FROM   patient p "
            + "                       INNER JOIN person p2 "
            + "                               ON p2.person_id = p.patient_id "
            + "                       INNER JOIN encounter e "
            + "                               ON e.patient_id = p.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON o.encounter_id = e.encounter_id "
            + "                WHERE  p2.gender = 'F' "
            + "                       AND e.encounter_type = ${6} "
            + "                       AND ( o.concept_id = ${1982} "
            + "                             AND o.value_coded = ${1065} ) "
            + "                       AND e.location_id = :location "
            + "                       AND p.voided = 0 "
            + "                       AND p2.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND e.encounter_datetime BETWEEN "
            + "                           :startDate AND :endDate "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       Date(o2.value_datetime) AS pregnancy_date "
            + "                FROM   patient p "
            + "                       INNER JOIN person p2 "
            + "                               ON p2.person_id = p.patient_id "
            + "                       INNER JOIN encounter e "
            + "                               ON e.patient_id = p.patient_id "
            + "                       INNER JOIN obs o "
            + "                               ON o.encounter_id = e.encounter_id "
            + "                       INNER JOIN obs o2 "
            + "                               ON o2.encounter_id = e.encounter_id "
            + "                WHERE  p2.gender = 'F' "
            + "                       AND e.encounter_type = ${51} "
            + "                       AND ( o.concept_id = ${1982} "
            + "                             AND o.value_coded = ${1065} ) "
            + "                       AND ( o2.concept_id = ${23821} "
            + "                             AND Date(o2.value_datetime) BETWEEN "
            + "                                 :startDate AND :endDate "
            + "                           ) "
            + "                       AND e.location_id = :location "
            + "                       AND p.voided = 0 "
            + "                       AND p2.voided = 0 "
            + "                       AND e.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o2.voided = 0) pg "
            + "        GROUP  BY pg.patient_id) pregnant "
            + " WHERE EXISTS ( "
            + "                   SELECT e.patient_id "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o "
            + "                                  ON o.encounter_id = e.encounter_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.patient_id = pregnant.patient_id "
            + "                          AND e.encounter_type IN ( ${6}, ${13}, ${51} ) "
            + "                          AND ( ( o.concept_id = ${1695} "
            + "                                  AND o.value_numeric IS NOT NULL ) "
            + "                                 OR ( o.concept_id = ${165515} "
            + "                                      AND o.value_coded IS NOT NULL ) ) "
            + "                          AND e.location_id = :location "
            + "                          AND Date(e.encounter_datetime) >= pregnant.pg_date "
            + "                          AND Date(e.encounter_datetime) <= :inclusionStartDate "
            + "                   UNION "
            + "                   SELECT e.patient_id "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o "
            + "                                  ON o.encounter_id = e.encounter_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.patient_id = pregnant.patient_id "
            + "                          AND e.encounter_type IN ( ${53}, ${90} ) "
            + "                          AND ( ( o.concept_id = ${1695} "
            + "                                  AND o.value_numeric IS NOT NULL ) "
            + "                                 OR ( o.concept_id = ${165515} "
            + "                                      AND o.value_coded IS NOT NULL ) ) "
            + "                          AND e.location_id = :location "
            + "                          AND o.obs_datetime >= pregnant.pg_date "
            + "                          AND o.obs_datetime <= :inclusionStartDate) ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    String mappedQuery = stringSubstitutor.replace(query);
    sqlCohortDefinition.setQuery(mappedQuery);
    return sqlCohortDefinition;
  }

  public CohortDefinition getPregnantsWithoutCD4Composition() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Pregnant Patients who dont have CD4 results during the inclusion period");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition pregnant = getPregnantWithoutCd4();
    CohortDefinition pregnantWithCd4AfterPregnancy = getPregnantWitCd4AfterPregnancy();

    cd.addSearch(
        "PREGNANT",
        EptsReportUtils.map(
            pregnant, "startDate=${startDate-8m},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "CD4",
        EptsReportUtils.map(
            pregnantWithCd4AfterPregnancy,
            "startDate=${startDate-8m},endDate=${endDate},inclusionStartDate=${startDate},location=${location}"));

    cd.setCompositionString("PREGNANT AND NOT CD4");
    return cd;
  }

  /**
   *
   *
   * <ul>
   *   <li>TB LAM result with ANY RESULT registered in the Investigações – resultados laboratoriais
   *       in Ficha Clínica – Mastercard
   *   <li>TB LAM result with ANY RESULT registered in the Laboratory form
   *   <li>TB-LAM urina with ``pos`` or ``neg`` result registered in the Ficha da Doença Avançada
   *       por HIV
   * </ul>
   *
   * @return CohortDefinition *
   */
  public CohortDefinition getPatientsWithAnyTbLamResult() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Number of clients with TB LAM results by report generation date ");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "End Date", Location.class));

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN (SELECT e.patient_id "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.encounter_type IN ( ${6}, ${13} ) "
            + "                          AND o.concept_id = ${23951} "
            + "                          AND o.value_coded IS NOT NULL "
            + "                          AND e.location_id = :location "
            + "                          AND e.encounter_datetime BETWEEN :startDate AND :endDate"
            + "                   GROUP  BY e.patient_id "
            + "                   UNION "
            + "                   SELECT e.patient_id "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.encounter_type = ${90} "
            + "                          AND o.concept_id = ${23951} "
            + "                          AND o.value_coded IN ( ${703}, ${664} ) "
            + "                          AND e.location_id = :location "
            + "                          AND o.obs_datetime BETWEEN :startDate AND :endDate"
            + "                   GROUP  BY e.patient_id) tb_lam "
            + "               ON tb_lam.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(getMetadata());
    cd.setQuery(sb.replace(query));
    return cd;
  }

  /**
   *
   *
   * <ul>
   *   <li>TB LAM result registered in the Investigações – Resultados Laboratoriais as @tbLamResult
   *       in Ficha Clínica or
   *   <li>TB LAM result marked as @tbLamResult in the Laboratory Form or
   *   <li>TB LAM result marked as @tbLamResult in Ficha DAH
   * </ul>
   *
   * @return CohortDefinition *
   */
  public CohortDefinition getPatientsWithTbLamResult(TbLamResult tbLamResult) {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Clients with positive TB LAM");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "End Date", Location.class));

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN (SELECT e.patient_id "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.encounter_type IN ( ${6}, ${13} ) "
            + "                          AND o.concept_id = ${23951} "
            + "                          AND o.value_coded = ".concat(tbLamResult.getValueCoded())
            + "                          AND e.location_id = :location "
            + "                          AND e.encounter_datetime BETWEEN :startDate AND :endDate"
            + "                   GROUP  BY e.patient_id "
            + "                   UNION "
            + "                   SELECT e.patient_id "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.encounter_type = ${90} "
            + "                          AND o.concept_id = ${23951} "
            + "                          AND o.value_coded =  ".concat(tbLamResult.getValueCoded())
            + "                          AND e.location_id = :location "
            + "                          AND o.obs_datetime BETWEEN :startDate AND :endDate"
            + "                   GROUP  BY e.patient_id) tb_lam "
            + "               ON tb_lam.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(getMetadata());
    cd.setQuery(sb.replace(query));
    return cd;
  }
  /**
   *
   *
   * <ul>
   *   <li>GeneXpert result marked with ANY RESULT registered in the Investigações – resultados
   *       laboratoriais - Ficha Clínica – Mastercard or
   *   <li>GeneXpert result marked with ANY RESULT registered in the Laboratory Form or
   *   <li>XpertMTB result marked with ANY RESULT registered in the Laboratory Form
   * </ul>
   *
   * @return CohortDefinition *
   */
  public CohortDefinition getPatientsWithAnyGeneXpertResult() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Clients with positive TB LAM");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "End Date", Location.class));

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN (SELECT e.patient_id "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.encounter_type = ${6}  "
            + "                          AND o.concept_id = ${23723}"
            + "                          AND o.value_coded IS NOT NULL "
            + "                          AND e.location_id = :location "
            + "                          AND e.encounter_datetime BETWEEN :startDate AND :endDate"
            + "                   GROUP  BY e.patient_id "
            + "                   UNION "
            + "                   SELECT e.patient_id "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.encounter_type = ${13} "
            + "                          AND o.concept_id IN (${23723}, ${165189}) "
            + "                          AND o.value_coded IS NOT NULL "
            + "                          AND e.location_id = :location "
            + "                          AND o.obs_datetime BETWEEN :startDate AND :endDate"
            + "                   GROUP  BY e.patient_id) tb_lam "
            + "               ON tb_lam.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(getMetadata());
    cd.setQuery(sb.replace(query));
    return cd;
  }

  /**
   *
   *
   * <ul>
   *   <li>GeneXpert result marked as Positive, registered in the Investigações – resultados
   *       laboratoriais - Ficha Clínica – Mastercard; or
   *   <li>GeneXpert result marked as Positive registered in the Laboratory Form or
   *   <li>XpertMTB result marked as SIM registered in the Laboratory Form
   * </ul>
   *
   * @return CohortDefinition *
   */
  public CohortDefinition getPatientsWithPositiveGeneXpertResult() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Clients with positive TB LAM and GeneXpert positive for TB");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "End Date", Location.class));

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN (SELECT e.patient_id "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.encounter_type IN (${6}, ${13}) "
            + "                          AND o.concept_id = ${23723}"
            + "                          AND o.value_coded = ${703} "
            + "                          AND e.location_id = :location "
            + "                          AND e.encounter_datetime BETWEEN :startDate AND :endDate"
            + "                   GROUP  BY e.patient_id "
            + "                   UNION "
            + "                   SELECT e.patient_id "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.encounter_type = ${13} "
            + "                          AND o.concept_id = ${165189} "
            + "                          AND o.value_coded = ${1065} "
            + "                          AND e.location_id = :location "
            + "                          AND o.obs_datetime BETWEEN :startDate AND :endDate"
            + "                   GROUP  BY e.patient_id) tb_lam "
            + "               ON tb_lam.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(getMetadata());
    cd.setQuery(sb.replace(query));
    return cd;
  }

  /**
   *
   *
   * <ul>
   *   <li>are marked with “Tratamento TB– Início (I)” on Ficha Clínica Master Card between (end
   *       date minus (-) 2 months) and the report generation date, or
   *   <li>have at least TB Treatment (Tratamento de TB) Start Date (Data de Início) in “Client
   *       Clinical Record of ART - Ficha de Seguimento between (end date minus 2 months) and the
   *       report generation date, or
   *   <li>have a TB Date (Condições Médicas Importantes – Ficha Resumo – Mastercard); between (end
   *       date minus (-) 2 months) and the report generation date, or
   *   <li>are enrolled in TB Program with enrollment Date (Data de Admissão) between (end date
   *       minus (-) 2 months) and the report generation date
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsOnTbTreatment() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Clients TB treatment by report generation date");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "End Date", Location.class));

    cd.addSearch(
        "startedTbTreatment",
        EptsReportUtils.map(txtbCohortQueries.tbTreatmentStartDateWithinReportingDate(), mappings));
    cd.addSearch("tbProgram", EptsReportUtils.map(txtbCohortQueries.getInTBProgram(), mappings));

    cd.addSearch("pumonaryTb", EptsReportUtils.map(txtbCohortQueries.getPulmonaryTB(), mappings));

    cd.addSearch("tbPlan", EptsReportUtils.map(txtbCohortQueries.getTBTreatmentStart(), mappings));

    cd.setCompositionString("startedTbTreatment OR tbProgram OR pumonaryTb OR tbPlan");

    return cd;
  }

  /**
   *
   *
   * <ul>
   *   <li>TB LAM result registered in the Investigações – Resultados Laboratoriais as Positive and
   *       grade @tbLamGrade marked for the positive result in Ficha Clínica or
   *   <li>TB LAM result marked as Positive and with grade @tbLamGrade marked for the positive
   *       result in the Laboratory Form or TB LAM result marked as Positive and with
   *       grade @tbLamGrade marked for the positive result in Ficha DAH
   *   <li>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWithPositiveTbLamAndGrade(Concept tbLamGrade) {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Clients with positive TB LAM and Grade");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "End Date", Location.class));

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN (SELECT e.patient_id "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                          INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND o2.voided = 0 "
            + "                          AND e.encounter_type  = ${6} "
            + "                          AND o.concept_id = ${23951} "
            + "                          AND o.value_coded = ${703} "
            + "                          AND o2.concept_id = ${165185} "
            + "                          AND o2.value_coded = "
                .concat(tbLamGrade.getConceptId().toString())
            + "                          AND e.location_id = :location "
            + "                          AND e.encounter_datetime BETWEEN :startDate AND :endDate"
            + "                   GROUP  BY e.patient_id "
            + "                   UNION "
            + " SELECT e.patient_id "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                          INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "                          INNER JOIN obs o3 ON o3.encounter_id = e.encounter_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND o2.voided = 0 "
            + "                          AND o3.voided = 0 "
            + "                          AND e.encounter_type = ${13}  "
            + "                          AND o.concept_id = ${23951} "
            + "                          AND o.value_coded = ${703} "
            + "                          AND o2.concept_id = ${165185} "
            + "                          AND o2.value_coded = "
                .concat(tbLamGrade.getConceptId().toString())
            + "                          AND o3.concept_id =  ${165349} "
            + "                          AND o3.obs_id = o2.obs_group_id"
            + "                          AND e.location_id = :location "
            + "                          AND e.encounter_datetime BETWEEN :startDate AND :endDate"
            + "                   GROUP  BY e.patient_id "
            + "                   UNION "
            + "                   SELECT e.patient_id "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                          INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND o2.voided = 0 "
            + "                          AND e.encounter_type = ${90} "
            + "                          AND o.concept_id = ${23951} "
            + "                          AND o2.concept_id = ${165185} "
            + "                          AND o2.value_coded =  "
                .concat(tbLamGrade.getConceptId().toString())
            + "                          AND e.location_id = :location "
            + "                          AND o.obs_datetime BETWEEN :startDate AND :endDate "
            + "                   GROUP  BY e.patient_id) positive_grade "
            + "               ON positive_grade.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(getMetadata());
    cd.setQuery(sb.replace(query));
    return cd;
  }

  public CohortDefinition getPatientsWithPositiveTbLamAndGradeFourPlus() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Clients with positive TB LAM and Grade 4+");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "End Date", Location.class));

    return getPatientsWithPositiveTbLamAndGrade(hivMetadata.getFourPlusConcept());
  }

  public CohortDefinition getPatientsWithPositiveTbLamAndGradeThreePlus() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Clients with positive TB LAM and Grade 3+");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "End Date", Location.class));

    CohortDefinition positiveThreePlus =
        getPatientsWithPositiveTbLamAndGrade(hivMetadata.getThreePlusConcept());

    cd.addSearch(
        "positiveFourPlus",
        EptsReportUtils.map(getPatientsWithPositiveTbLamAndGradeFourPlus(), mappings));

    cd.addSearch("positiveThreePlus", EptsReportUtils.map(positiveThreePlus, mappings));

    cd.setCompositionString("positiveThreePlus AND NOT positiveFourPlus");

    return cd;
  }

  public CohortDefinition getPatientsWithPositiveTbLamAndGradeTwoPlus() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Clients with positive TB LAM and Grade 2+");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "End Date", Location.class));

    CohortDefinition positiveTwoPlus =
        getPatientsWithPositiveTbLamAndGrade(hivMetadata.getTwoPlusConcept());

    cd.addSearch(
        "positiveThreePlus",
        EptsReportUtils.map(getPatientsWithPositiveTbLamAndGradeThreePlus(), mappings));

    cd.addSearch("positiveTwoPlus", EptsReportUtils.map(positiveTwoPlus, mappings));

    cd.setCompositionString("positiveTwoPlus AND NOT positiveThreePlus");

    return cd;
  }

  public CohortDefinition getPatientsWithPositiveTbLamAndGradeOnePlus() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Clients with positive TB LAM and Grade 1+");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "End Date", Location.class));

    CohortDefinition positiveOnePlus =
        getPatientsWithPositiveTbLamAndGrade(hivMetadata.getOnePlusConcept());

    cd.addSearch(
        "positiveTwoPlus",
        EptsReportUtils.map(getPatientsWithPositiveTbLamAndGradeTwoPlus(), mappings));

    cd.addSearch("positiveOnePlus", EptsReportUtils.map(positiveOnePlus, mappings));

    cd.setCompositionString("positiveOnePlus AND NOT positiveTwoPlus");

    return cd;
  }

  public CohortDefinition getPatientsWithPositiveTbLamAndGradeNotReported() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Clients with positive TB LAM and Not Reported Grade");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "End Date", Location.class));

    cd.addSearch(
        "positive",
        EptsReportUtils.map(getPatientsWithTbLamResult(TbLamResult.POSITIVE), mappings));

    cd.addSearch(
        "positiveOnePlus",
        EptsReportUtils.map(getPatientsWithPositiveTbLamAndGradeOnePlus(), mappings));

    cd.setCompositionString("positive AND NOT positiveOnePlus");

    return cd;
  }

  /**
   *
   *
   * <ul>
   *   <li>Who have a VL Result > 1000 copies/ml registered in Ficha de Laboratório Geral or e-lab
   *       with the VL Result Date during the inclusion period
   *   <li>Who have a previous VL result > 1000 copies/ml registered in the most recent Ficha de
   *       Laboratório Geral or e-lab prior to the VL Result > 1000 copies/ml identified above
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWithTwoConsecutiveVLGreaterThan1000() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "Clients with two consecutive Viral Load results > 1000 copies/mm3 for which the second one falls in the inclusion period");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "End Date", Location.class));

    cd.addSearch(
        "vlOnPeriod", EptsReportUtils.map(getPatientsUnsuppressedVLDuringInclusion(), mappings));

    cd.addSearch(
        "vlBeforeLastVl",
        EptsReportUtils.map(getPatientsUnsuppressedVLPreviousInclusion(), mappings));

    cd.setCompositionString("vlOnPeriod AND vlBeforeLastVl");

    return cd;
  }

  /**
   * Who have a VL Result > 1000 copies/ml registered in Ficha de Laboratório Geral or e-lab with
   * the VL Result Date during the inclusion period
   *
   * @return CohortDefinition
   */
  private CohortDefinition getPatientsUnsuppressedVLDuringInclusion() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Clients who had a second consecutive unsuppressed VL ");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "End Date", Location.class));

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "       INNER JOIN (SELECT e.patient_id, Min(Date(e.encounter_datetime)) vl_date "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                                     AND e.voided = 0 "
            + "                                     AND o.voided = 0 "
            + "                                     AND e.encounter_type IN ( ${13}, ${51} ) "
            + "                                     AND e.location_id = :location "
            + "                                                 AND ( ( o.concept_id = ${856} "
            + "                                                     AND o.value_numeric IS NOT NULL ) "
            + "                                                   OR ( o.concept_id = ${1305} "
            + "                                                        AND o.value_coded IS NOT NULL ) ) "
            + "                                     AND Date(e.encounter_datetime) BETWEEN :startDate AND :endDate "
            + "                   GROUP  BY e.patient_id)vl_inclusion "
            + "               ON vl_inclusion.patient_id = e.patient_id "
            + "WHERE  e.encounter_type IN( ${13}, ${51} ) "
            + "       AND e.location_id = :location "
            + "       AND Date(e.encounter_datetime) = vl_inclusion.vl_date "
            + "       AND e.voided = 0 "
            + "       AND p.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND o.concept_id = ${856} "
            + "       AND o.value_numeric > 1000 "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(getMetadata());
    cd.setQuery(sb.replace(query));
    return cd;
  }

  /**
   * Who have a previous VL result > 1000 copies/ml registered in the most recent Ficha de
   * Laboratório Geral or e-lab prior to the VL Result > 1000 copies/ml identified above {@link
   * #getPatientsUnsuppressedVLDuringInclusion()}
   *
   * @return CohortDefinition
   */
  private CohortDefinition getPatientsUnsuppressedVLPreviousInclusion() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Clients Who have a previous VL result > 1000");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "End Date", Location.class));

    String query =
        "SELECT p.patient_id "
            + "             FROM   patient p "
            + "                    INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                    INNER JOIN ( "
            + "                 SELECT p.patient_id , MAX(Date(e.encounter_datetime)) AS second_vl "
            + "                 FROM   patient p "
            + "                            INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                                INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                            INNER JOIN ( "
            + "                     SELECT p.patient_id , MAX(Date(e.encounter_datetime)) as last_vl1000_date "
            + "                     FROM   patient p "
            + "                                INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                                INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                                INNER JOIN (SELECT e.patient_id, MAX(Date(e.encounter_datetime)) vl_date "
            + "                                            FROM   encounter e "
            + "                                                       INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                                                AND e.voided = 0 "
            + "                                                AND o.voided = 0 "
            + "                                                AND e.encounter_type IN ( ${13}, ${51} ) "
            + "                                                AND e.location_id = :location "
            + "                                                 AND ( ( o.concept_id = ${856} "
            + "                                                     AND o.value_numeric IS NOT NULL ) "
            + "                                                   OR ( o.concept_id = ${1305} "
            + "                                                        AND o.value_coded IS NOT NULL ) ) "
            + "                                                AND Date(e.encounter_datetime) BETWEEN :startDate AND :endDate "
            + "                                            GROUP  BY e.patient_id)vl_inclusion "
            + "                                           ON vl_inclusion.patient_id = e.patient_id "
            + "                     WHERE  e.encounter_type IN( ${13}, ${51} ) "
            + "                       AND e.location_id = :location "
            + "                       AND Date(e.encounter_datetime) = vl_inclusion.vl_date "
            + "                       AND e.voided = 0 "
            + "                       AND p.voided = 0 "
            + "                       AND o.voided = 0 "
            + "                       AND o.concept_id = ${856} "
            + "                       AND o.value_numeric > 1000 "
            + "                     GROUP BY p.patient_id "
            + "                 )last_vl100  ON last_vl100.patient_id = e.patient_id "
            + "                 WHERE  e.encounter_type IN( ${13}, ${51} ) "
            + "                   AND e.location_id = :location "
            + "                   AND Date(e.encounter_datetime) < last_vl100.last_vl1000_date "
            + "                   AND e.voided = 0 AND o.voided = 0 "
            + "                   AND p.voided = 0 "
            + "                   AND ( ( o.concept_id = ${856} "
            + "                          AND o.value_numeric IS NOT NULL ) "
            + "                       OR ( o.concept_id = ${1305} "
            + "                   AND o.value_coded IS NOT NULL ) ) "
            + "                 GROUP  BY p.patient_id "
            + "             )second_vl "
            + "                            ON second_vl.patient_id = e.patient_id "
            + "             WHERE  e.encounter_type IN( ${13}, ${51} ) "
            + "                    AND e.location_id = :location "
            + "                    AND Date(e.encounter_datetime) = second_vl.second_vl "
            + "                    AND e.voided = 0 "
            + "                    AND p.voided = 0 "
            + "                    AND o.voided = 0 "
            + "                    AND o.concept_id = ${856} "
            + "                    AND o.value_numeric > 1000 "
            + "             GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(getMetadata());
    cd.setQuery(sb.replace(query));
    return cd;
  }

  /**
   * Absolute CD4 Count
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWithCd4Count(Cd4CountComparison cd4CountComparison) {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Clients with Absolute CD4 Count");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "End Date", Location.class));

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "       INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "       INNER JOIN (SELECT recent_cd4.patient_id, Max(cd4_date) recent_date "
            + "                   FROM  (SELECT e.patient_id, Date(e.encounter_datetime) cd4_date "
            + "                          FROM   encounter e "
            + "                                 INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                          WHERE  e.encounter_type IN ( ${6}, ${13}, ${51} ) "
            + "                                 AND e.location_id = :location "
            + "                                 AND Date(e.encounter_datetime) BETWEEN :startDate AND :endDate "
            + "                                 AND o.concept_id = ${1695} "
            + "                                 AND e.voided = 0 "
            + "                                 AND o.voided = 0 "
            + "                          UNION "
            + "                          SELECT e.patient_id, o.obs_datetime AS cd4_date "
            + "                          FROM   encounter e "
            + "                                 INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                          WHERE  e.encounter_type IN ( ${90}, ${53} ) "
            + "                                 AND e.location_id = :location "
            + "                                 AND e.voided = 0 "
            + "                                 AND o.voided = 0 "
            + "                                 AND o.concept_id = ${1695} "
            + "                                 AND o.obs_datetime BETWEEN :startDate AND :endDate "
            + "                           ) recent_cd4 "
            + "                   GROUP  BY recent_cd4.patient_id) cd4 "
            + "               ON cd4.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${1695} "
            + "       AND  ".concat(cd4CountComparison.getProposition())
            + "       AND ( ( Date(e.encounter_datetime) = cd4.recent_date AND e.encounter_type IN ( ${6}, ${13}, ${51} ) ) "
            + "              OR ( Date(o.obs_datetime) = cd4.recent_date  AND e.encounter_type IN ( ${90}, ${53} ) )"
            + "    ) "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(getMetadata());
    cd.setQuery(sb.replace(query));
    return cd;
  }

  /**
   * Absolute CD4 Count
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWithAbsoluteCd4Count(Cd4CountComparison cd4CountComparison) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Clients with Absolute CD4 Count");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "End Date", Location.class));

    cd.addSearch(
        cd4CountComparison.getSearchKey(),
        EptsReportUtils.map(getPatientsWithCd4Count(cd4CountComparison), mappings));

    cd.setCompositionString(cd4CountComparison.getCompositionString());
    return cd;
  }

  private String getArtStartDate() {
    String arvStart = commonQueries.getARTStartDate(true);

    String query =
        "SELECT patient_id, initiated_art.first_pickup AS eligibility_date FROM ( "
            + arvStart
            + " ) initiated_art"
            + "   WHERE initiated_art.first_pickup BETWEEN :startDate AND :endDate "
            + "   GROUP BY patient_id";
    return query;
  }

  private static String getPregnantWithoutCd4Query() {
    return " SELECT pregnant.patient_id, pregnant.pg_date AS eligibility_date "
        + " FROM   (SELECT pg.patient_id, "
        + "               MAX(pg.pregnancy_date) AS pg_date "
        + "        FROM   (SELECT p.patient_id, "
        + "                       e.encounter_datetime AS pregnancy_date "
        + "                FROM   patient p "
        + "                       INNER JOIN person p2 "
        + "                               ON p2.person_id = p.patient_id "
        + "                       INNER JOIN encounter e "
        + "                               ON e.patient_id = p.patient_id "
        + "                       INNER JOIN obs o "
        + "                               ON o.encounter_id = e.encounter_id "
        + "                WHERE  e.encounter_type IN ( ${5}, ${6} ) "
        + "                       AND p2.gender = 'F' "
        + "                       AND ( o.concept_id = ${1982} "
        + "                             AND o.value_coded = ${1065} ) "
        + "                       AND e.location_id = :location "
        + "                       AND p.voided = 0 "
        + "                       AND e.encounter_datetime BETWEEN "
        + "                           DATE_SUB(:startDate, INTERVAL 8 MONTH) AND :endDate "
        + "                       AND p2.voided = 0 "
        + "                       AND e.voided = 0 "
        + "                       AND o.voided = 0 "
        + "                UNION "
        + "                SELECT p.patient_id, "
        + "                       e.encounter_datetime AS pregnancy_date "
        + "                FROM   patient p "
        + "                       INNER JOIN person p2 "
        + "                               ON p2.person_id = p.patient_id "
        + "                       INNER JOIN encounter e "
        + "                               ON e.patient_id = p.patient_id "
        + "                       INNER JOIN obs o "
        + "                               ON o.encounter_id = e.encounter_id "
        + "                WHERE  p2.gender = 'F' "
        + "                       AND e.encounter_type IN ( ${5}, ${6} ) "
        + "                       AND o.concept_id = ${1279} "
        + "                       AND e.location_id = :location "
        + "                       AND p.voided = 0 "
        + "                       AND p2.voided = 0 "
        + "                       AND e.voided = 0 "
        + "                       AND o.voided = 0 "
        + "                       AND e.encounter_datetime BETWEEN "
        + "                           DATE_SUB(:startDate, INTERVAL 8 MONTH) AND :endDate "
        + "                UNION "
        + "                SELECT p.patient_id, "
        + "                       e.encounter_datetime AS pregnancy_date "
        + "                FROM   patient p "
        + "                       INNER JOIN person p2 "
        + "                               ON p2.person_id = p.patient_id "
        + "                       INNER JOIN encounter e "
        + "                               ON e.patient_id = p.patient_id "
        + "                       INNER JOIN obs o "
        + "                               ON o.encounter_id = e.encounter_id "
        + "                WHERE  p2.gender = 'F' "
        + "                       AND e.encounter_type IN ( ${5}, ${6} ) "
        + "                       AND o.concept_id = ${1600} "
        + "                       AND e.location_id = :location "
        + "                       AND p.voided = 0 "
        + "                       AND p2.voided = 0 "
        + "                       AND e.voided = 0 "
        + "                       AND o.voided = 0 "
        + "                       AND e.encounter_datetime BETWEEN "
        + "                           DATE_SUB(:startDate, INTERVAL 8 MONTH) AND :endDate "
        + "                UNION "
        + "                SELECT p.patient_id, "
        + "                       e.encounter_datetime AS pregnancy_date "
        + "                FROM   patient p "
        + "                       INNER JOIN person p2 "
        + "                               ON p2.person_id = p.patient_id "
        + "                       INNER JOIN encounter e "
        + "                               ON e.patient_id = p.patient_id "
        + "                       INNER JOIN obs o "
        + "                               ON o.encounter_id = e.encounter_id "
        + "                WHERE  p2.gender = 'F' "
        + "                       AND e.encounter_type = ${6} "
        + "                       AND ( o.concept_id = ${6334} "
        + "                             AND o.value_coded = ${6331} ) "
        + "                       AND e.location_id = :location "
        + "                       AND p.voided = 0 "
        + "                       AND p2.voided = 0 "
        + "                       AND e.voided = 0 "
        + "                       AND o.voided = 0 "
        + "                       AND e.encounter_datetime BETWEEN "
        + "                           DATE_SUB(:startDate, INTERVAL 8 MONTH) AND :endDate "
        + "                UNION "
        + "                SELECT pp.patient_id, "
        + "                       pp.date_enrolled AS pregnancy_date "
        + "                FROM   patient_program pp "
        + "                       INNER JOIN person p "
        + "                               ON p.person_id = pp.patient_id "
        + "                       INNER JOIN encounter e "
        + "                               ON e.patient_id = pp.patient_id "
        + "                WHERE  p.gender = 'F' "
        + "                       AND pp.program_id = ${8} "
        + "                       AND e.location_id = :location "
        + "                       AND p.voided = 0 "
        + "                       AND pp.voided = 0 "
        + "                       AND pp.date_enrolled BETWEEN "
        + "                           DATE_SUB(:startDate, INTERVAL 8 MONTH) AND :endDate "
        + "                UNION "
        + "                SELECT p.patient_id, "
        + "                       o2.value_datetime AS pregnancy_date "
        + "                FROM   patient p "
        + "                       INNER JOIN person p2 "
        + "                               ON p2.person_id = p.patient_id "
        + "                       INNER JOIN encounter e "
        + "                               ON e.patient_id = p.patient_id "
        + "                       INNER JOIN obs o "
        + "                               ON o.encounter_id = e.encounter_id "
        + "                       INNER JOIN obs o2 "
        + "                               ON o2.encounter_id = e.encounter_id "
        + "                WHERE  p2.gender = 'F' "
        + "                       AND e.encounter_type = ${53} "
        + "                       AND ( o.concept_id = ${1982} "
        + "                             AND o.value_coded = ${1065} ) "
        + "                       AND ( o2.concept_id = ${1190} "
        + "                             AND o2.value_datetime BETWEEN "
        + "                                 DATE_SUB(:startDate, INTERVAL 8 MONTH) AND :endDate ) "
        + "                       AND e.location_id = :location "
        + "                       AND p.voided = 0 "
        + "                       AND p2.voided = 0 "
        + "                       AND e.voided = 0 "
        + "                       AND o.voided = 0 "
        + "                       AND o2.voided = 0 "
        + "                UNION "
        + "                SELECT p.patient_id, "
        + "                       e.encounter_datetime AS pregnancy_date "
        + "                FROM   patient p "
        + "                       INNER JOIN person p2 "
        + "                               ON p2.person_id = p.patient_id "
        + "                       INNER JOIN encounter e "
        + "                               ON e.patient_id = p.patient_id "
        + "                       INNER JOIN obs o "
        + "                               ON o.encounter_id = e.encounter_id "
        + "                WHERE  p2.gender = 'F' "
        + "                       AND e.encounter_type = ${6} "
        + "                       AND ( o.concept_id = ${1982} "
        + "                             AND o.value_coded = ${1065} ) "
        + "                       AND e.location_id = :location "
        + "                       AND p.voided = 0 "
        + "                       AND p2.voided = 0 "
        + "                       AND e.voided = 0 "
        + "                       AND o.voided = 0 "
        + "                       AND e.encounter_datetime BETWEEN "
        + "                           DATE_SUB(:startDate, INTERVAL 8 MONTH) AND :endDate "
        + "                UNION "
        + "                SELECT p.patient_id, "
        + "                       Date(o2.value_datetime) AS pregnancy_date "
        + "                FROM   patient p "
        + "                       INNER JOIN person p2 "
        + "                               ON p2.person_id = p.patient_id "
        + "                       INNER JOIN encounter e "
        + "                               ON e.patient_id = p.patient_id "
        + "                       INNER JOIN obs o "
        + "                               ON o.encounter_id = e.encounter_id "
        + "                       INNER JOIN obs o2 "
        + "                               ON o2.encounter_id = e.encounter_id "
        + "                WHERE  p2.gender = 'F' "
        + "                       AND e.encounter_type = ${51} "
        + "                       AND ( o.concept_id = ${1982} "
        + "                             AND o.value_coded = ${1065} ) "
        + "                       AND ( o2.concept_id = ${23821} "
        + "                             AND Date(o2.value_datetime) BETWEEN "
        + "                                 DATE_SUB(:startDate, INTERVAL 8 MONTH) AND :endDate "
        + "                           ) "
        + "                       AND e.location_id = :location "
        + "                       AND p.voided = 0 "
        + "                       AND p2.voided = 0 "
        + "                       AND e.voided = 0 "
        + "                       AND o.voided = 0 "
        + "                       AND o2.voided = 0) pg "
        + "        GROUP  BY pg.patient_id) pregnant "
        + "            LEFT JOIN (  "
        + "                SELECT bf.patient_id,  "
        + "                       MAX(bf.breastfeeding_date) AS breastfeeding_date  "
        + "                FROM   (  "
        + "                           SELECT e.patient_id, o.value_datetime AS breastfeeding_date  "
        + "                           FROM   encounter e  "
        + "                                      INNER JOIN obs o  "
        + "                                                 ON o.encounter_id = e.encounter_id  "
        + "                           WHERE  e.encounter_type IN ( ${5}, ${6} )  "
        + "                             AND  o.concept_id = ${5599}  "
        + "                             AND e.location_id = :location     "
        + "                             AND o.value_datetime >= DATE_SUB(:startDate, INTERVAL 8 MONTH) and o.value_datetime <= :endDate  "
        + "                             AND e.voided = 0  "
        + "                             AND o.voided = 0  "
        + "                           UNION  "
        + "                           SELECT e.patient_id, e.encounter_datetime AS breastfeeding_date  "
        + "                           FROM   encounter e  "
        + "                                      INNER JOIN obs o  "
        + "                                                 ON o.encounter_id = e.encounter_id  "
        + "                           WHERE  e.encounter_type = ${6}  "
        + "                             AND e.location_id = :location  "
        + "                             AND ( o.concept_id =${6332}  "
        + "                               AND o.value_coded =${1065} )  "
        + "                             AND e.encounter_datetime >= DATE_SUB(:startDate, INTERVAL 8 MONTH) AND e.encounter_datetime <= :endDate  "
        + "                             AND o.voided = 0  "
        + "                             AND e.voided = 0     "
        + "                           UNION  "
        + "                           SELECT e.patient_id, e.encounter_datetime AS breastfeeding_date  "
        + "                           FROM   encounter e  "
        + "                                      INNER JOIN obs o  "
        + "                                                 ON o.encounter_id = e.encounter_id  "
        + "                           WHERE  e.encounter_type IN ( ${5}, ${6} )  "
        + "                             AND o.concept_id = ${6334}  "
        + "                             AND o.value_coded =${6332}  "
        + "                             AND e.voided = 0  "
        + "                             AND o.voided = 0     "
        + "                             AND e.location_id = :location  "
        + "                             AND e.encounter_datetime >= DATE_SUB(:startDate, INTERVAL 8 MONTH)  "
        + "                             AND e.encounter_datetime <= :endDate  "
        + "                           UNION  "
        + "                           SELECT pp.patient_id, ps.start_date AS breastfeeding_date  "
        + "                           FROM   patient_program pp  "
        + "                                      INNER JOIN patient_state ps  "
        + "                                                 ON ps.patient_program_id =  "
        + "                                                    pp.patient_program_id  "
        + "                           WHERE  pp.program_id =${8}  "
        + "                             AND ps.state =${27}  "
        + "                             AND pp.location_id = :location  "
        + "                             AND pp.voided = 0  "
        + "                             AND ps.voided = 0  "
        + "                             AND ps.start_date >= DATE_SUB(:startDate, INTERVAL 8 MONTH)  "
        + "                             AND ps.start_date <= :endDate  "
        + "                           UNION  "
        + "                           SELECT e.patient_id, o2.value_datetime AS breastfeeding_date  "
        + "                           FROM   encounter e  "
        + "                                      INNER JOIN obs o  "
        + "                                                 ON o.encounter_id = e.encounter_id  "
        + "                                      INNER JOIN obs o2  "
        + "                                                 ON o2.encounter_id = e.encounter_id  "
        + "                           WHERE  e.encounter_type =${53}  "
        + "                             AND (o.concept_id =${6332}  "
        + "                             AND o.value_coded =${1065} )    "
        + "                             AND ( o2.concept_id = ${1190} "
        + "                               AND o2.value_datetime BETWEEN DATE_SUB(:startDate, INTERVAL 8 MONTH) AND :endDate )     "
        + "                             AND e.location_id = :location  "
        + "                             AND e.voided = 0  "
        + "                             AND o.voided = 0  "
        + "                           UNION  "
        + "                           SELECT e.patient_id, DATE(o2.value_datetime) AS breastfeeding_date  "
        + "                           FROM   encounter e  "
        + "                                      INNER JOIN obs o  "
        + "                                                 ON o.encounter_id = e.encounter_id  "
        + "                                      INNER JOIN obs o2  "
        + "                                                 ON o2.encounter_id = e.encounter_id  "
        + "                           WHERE  e.encounter_type = ${51}  "
        + "                             AND o.concept_id =${6332}     "
        + "                             AND o.value_coded =${1065}  "
        + "                             AND o2.concept_id = ${23821}  "
        + "                             AND DATE(o2.value_datetime) >= DATE_SUB(:startDate, INTERVAL 8 MONTH) AND DATE(o2.value_datetime) <= :endDate  "
        + "                             AND e.voided = 0  "
        + "                             AND o.voided = 0  "
        + "                             AND o2.voided = 0  "
        + "                             AND e.location_id =:location  "
        + "                       ) bf  "
        + "                GROUP  BY bf.patient_id  "
        + "            ) breastfeeding ON pregnant.patient_id = breastfeeding.patient_id  "
        + "            WHERE ((pregnant.pg_date IS NOT NULL  "
        + "                       AND pregnant.pg_date >= breastfeeding.breastfeeding_date)  "
        + "                 OR (breastfeeding.breastfeeding_date IS NULL) )"
        + "AND   NOT EXISTS ( "
        + " SELECT pg.patient_id "
        + " FROM   (SELECT pg.patient_id, "
        + "               MIN(pg.pregnancy_date) AS pg_date "
        + "        FROM   (SELECT p.patient_id, "
        + "                       e.encounter_datetime AS pregnancy_date "
        + "                FROM   patient p "
        + "                       INNER JOIN person p2 "
        + "                               ON p2.person_id = p.patient_id "
        + "                       INNER JOIN encounter e "
        + "                               ON e.patient_id = p.patient_id "
        + "                       INNER JOIN obs o "
        + "                               ON o.encounter_id = e.encounter_id "
        + "                WHERE  e.encounter_type IN ( ${5}, ${6} ) "
        + "                       AND p2.gender = 'F' "
        + "                       AND ( o.concept_id = ${1982} "
        + "                             AND o.value_coded = ${1065} ) "
        + "                       AND e.location_id = :location "
        + "                       AND p.voided = 0 "
        + "                       AND e.encounter_datetime BETWEEN "
        + "                           :startDate AND :endDate "
        + "                       AND p2.voided = 0 "
        + "                       AND e.voided = 0 "
        + "                       AND o.voided = 0 "
        + "                UNION "
        + "                SELECT p.patient_id, "
        + "                       e.encounter_datetime AS pregnancy_date "
        + "                FROM   patient p "
        + "                       INNER JOIN person p2 "
        + "                               ON p2.person_id = p.patient_id "
        + "                       INNER JOIN encounter e "
        + "                               ON e.patient_id = p.patient_id "
        + "                       INNER JOIN obs o "
        + "                               ON o.encounter_id = e.encounter_id "
        + "                WHERE  p2.gender = 'F' "
        + "                       AND e.encounter_type IN ( ${5}, ${6} ) "
        + "                       AND o.concept_id = ${1279} "
        + "                       AND e.location_id = :location "
        + "                       AND p.voided = 0 "
        + "                       AND p2.voided = 0 "
        + "                       AND e.voided = 0 "
        + "                       AND o.voided = 0 "
        + "                       AND e.encounter_datetime BETWEEN "
        + "                           :startDate AND :endDate "
        + "                UNION "
        + "                SELECT p.patient_id, "
        + "                       e.encounter_datetime AS pregnancy_date "
        + "                FROM   patient p "
        + "                       INNER JOIN person p2 "
        + "                               ON p2.person_id = p.patient_id "
        + "                       INNER JOIN encounter e "
        + "                               ON e.patient_id = p.patient_id "
        + "                       INNER JOIN obs o "
        + "                               ON o.encounter_id = e.encounter_id "
        + "                WHERE  p2.gender = 'F' "
        + "                       AND e.encounter_type IN ( ${5}, ${6} ) "
        + "                       AND o.concept_id = ${1600} "
        + "                       AND e.location_id = :location "
        + "                       AND p.voided = 0 "
        + "                       AND p2.voided = 0 "
        + "                       AND e.voided = 0 "
        + "                       AND o.voided = 0 "
        + "                       AND e.encounter_datetime BETWEEN "
        + "                           :startDate AND :endDate "
        + "                UNION "
        + "                SELECT p.patient_id, "
        + "                       e.encounter_datetime AS pregnancy_date "
        + "                FROM   patient p "
        + "                       INNER JOIN person p2 "
        + "                               ON p2.person_id = p.patient_id "
        + "                       INNER JOIN encounter e "
        + "                               ON e.patient_id = p.patient_id "
        + "                       INNER JOIN obs o "
        + "                               ON o.encounter_id = e.encounter_id "
        + "                WHERE  p2.gender = 'F' "
        + "                       AND e.encounter_type = ${6} "
        + "                       AND ( o.concept_id = ${6334} "
        + "                             AND o.value_coded = ${6331} ) "
        + "                       AND e.location_id = :location "
        + "                       AND p.voided = 0 "
        + "                       AND p2.voided = 0 "
        + "                       AND e.voided = 0 "
        + "                       AND o.voided = 0 "
        + "                       AND e.encounter_datetime BETWEEN "
        + "                           :startDate AND :endDate "
        + "                UNION "
        + "                SELECT pp.patient_id, "
        + "                       pp.date_enrolled AS pregnancy_date "
        + "                FROM   patient_program pp "
        + "                       INNER JOIN person p "
        + "                               ON p.person_id = pp.patient_id "
        + "                       INNER JOIN encounter e "
        + "                               ON e.patient_id = pp.patient_id "
        + "                WHERE  p.gender = 'F' "
        + "                       AND pp.program_id = ${8} "
        + "                       AND e.location_id = :location "
        + "                       AND p.voided = 0 "
        + "                       AND pp.voided = 0 "
        + "                       AND pp.date_enrolled BETWEEN "
        + "                           :startDate AND :endDate "
        + "                UNION "
        + "                SELECT p.patient_id, "
        + "                       o2.value_datetime AS pregnancy_date "
        + "                FROM   patient p "
        + "                       INNER JOIN person p2 "
        + "                               ON p2.person_id = p.patient_id "
        + "                       INNER JOIN encounter e "
        + "                               ON e.patient_id = p.patient_id "
        + "                       INNER JOIN obs o "
        + "                               ON o.encounter_id = e.encounter_id "
        + "                       INNER JOIN obs o2 "
        + "                               ON o2.encounter_id = e.encounter_id "
        + "                WHERE  p2.gender = 'F' "
        + "                       AND e.encounter_type = ${53} "
        + "                       AND ( o.concept_id = ${1982} "
        + "                             AND o.value_coded = ${1065} ) "
        + "                       AND ( o2.concept_id = ${1190} "
        + "                             AND o2.value_datetime BETWEEN "
        + "                                 :startDate AND :endDate ) "
        + "                       AND e.location_id = :location "
        + "                       AND p.voided = 0 "
        + "                       AND p2.voided = 0 "
        + "                       AND e.voided = 0 "
        + "                       AND o.voided = 0 "
        + "                       AND o2.voided = 0 "
        + "                UNION "
        + "                SELECT p.patient_id, "
        + "                       e.encounter_datetime AS pregnancy_date "
        + "                FROM   patient p "
        + "                       INNER JOIN person p2 "
        + "                               ON p2.person_id = p.patient_id "
        + "                       INNER JOIN encounter e "
        + "                               ON e.patient_id = p.patient_id "
        + "                       INNER JOIN obs o "
        + "                               ON o.encounter_id = e.encounter_id "
        + "                WHERE  p2.gender = 'F' "
        + "                       AND e.encounter_type = ${6} "
        + "                       AND ( o.concept_id = ${1982} "
        + "                             AND o.value_coded = ${1065} ) "
        + "                       AND e.location_id = :location "
        + "                       AND p.voided = 0 "
        + "                       AND p2.voided = 0 "
        + "                       AND e.voided = 0 "
        + "                       AND o.voided = 0 "
        + "                       AND e.encounter_datetime BETWEEN "
        + "                           :startDate AND :endDate "
        + "                UNION "
        + "                SELECT p.patient_id, "
        + "                       Date(o2.value_datetime) AS pregnancy_date "
        + "                FROM   patient p "
        + "                       INNER JOIN person p2 "
        + "                               ON p2.person_id = p.patient_id "
        + "                       INNER JOIN encounter e "
        + "                               ON e.patient_id = p.patient_id "
        + "                       INNER JOIN obs o "
        + "                               ON o.encounter_id = e.encounter_id "
        + "                       INNER JOIN obs o2 "
        + "                               ON o2.encounter_id = e.encounter_id "
        + "                WHERE  p2.gender = 'F' "
        + "                       AND e.encounter_type = ${51} "
        + "                       AND ( o.concept_id = ${1982} "
        + "                             AND o.value_coded = ${1065} ) "
        + "                       AND ( o2.concept_id = ${23821} "
        + "                             AND Date(o2.value_datetime) BETWEEN "
        + "                                 :startDate AND :endDate "
        + "                           ) "
        + "                       AND e.location_id = :location "
        + "                       AND p.voided = 0 "
        + "                       AND p2.voided = 0 "
        + "                       AND e.voided = 0 "
        + "                       AND o.voided = 0 "
        + "                       AND o2.voided = 0) pg "
        + "        GROUP  BY pg.patient_id) pg "
        + " WHERE pg.patient_id = pregnant.patient_id "
        + "           AND EXISTS ( "
        + "                   SELECT e.patient_id "
        + "                   FROM   encounter e "
        + "                          INNER JOIN obs o "
        + "                                  ON o.encounter_id = e.encounter_id "
        + "                   WHERE  e.voided = 0 "
        + "                          AND o.voided = 0 "
        + "                          AND e.patient_id = pg.patient_id "
        + "                          AND e.encounter_type IN ( ${6}, ${13}, ${51} ) "
        + "                          AND ( ( o.concept_id = ${1695} "
        + "                                  AND o.value_numeric IS NOT NULL ) "
        + "                                 OR ( o.concept_id = ${165515} "
        + "                                      AND o.value_coded IS NOT NULL ) ) "
        + "                          AND e.location_id = :location "
        + "                          AND Date(e.encounter_datetime) >= pg.pg_date "
        + "                          AND Date(e.encounter_datetime) <= DATE_SUB(:startDate, INTERVAL 8 MONTH) "
        + "                   UNION "
        + "                   SELECT e.patient_id "
        + "                   FROM   encounter e "
        + "                          INNER JOIN obs o "
        + "                                  ON o.encounter_id = e.encounter_id "
        + "                   WHERE  e.voided = 0 "
        + "                          AND o.voided = 0 "
        + "                          AND e.patient_id = pg.patient_id "
        + "                          AND e.encounter_type IN ( ${53}, ${90} ) "
        + "                          AND ( ( o.concept_id = ${1695} "
        + "                                  AND o.value_numeric IS NOT NULL ) "
        + "                                 OR ( o.concept_id = ${165515} "
        + "                                      AND o.value_coded IS NOT NULL ) ) "
        + "                          AND e.location_id = :location "
        + "                          AND o.obs_datetime >= pg.pg_date "
        + "                          AND o.obs_datetime <= DATE_SUB(:startDate, INTERVAL 8 MONTH)) "
        + "                      ) ";
  }

  private static String getCd4CountOnPeriod() {
    return " SELECT vl.patient_id, vl.vl_date AS eligibility_date FROM ( "
        + " SELECT p.patient_id, DATE(e.encounter_datetime) as vl_date "
        + " FROM   patient p "
        + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
        + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
        + "       INNER JOIN (SELECT e.patient_id, MAX(Date(e.encounter_datetime)) vl_date "
        + "                   FROM   encounter e "
        + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
        + "                                     AND e.voided = 0 "
        + "                                     AND o.voided = 0 "
        + "                                     AND e.encounter_type IN ( ${13}, ${51} ) "
        + "                                     AND e.location_id = :location "
        + "                                     AND ( ( o.concept_id = ${856} "
        + "                                         AND o.value_numeric IS NOT NULL ) "
        + "                                       OR ( o.concept_id = ${1305} "
        + "                                            AND o.value_coded IS NOT NULL ) ) "
        + "                                     AND Date(e.encounter_datetime) BETWEEN :startDate AND :endDate "
        + "                   GROUP  BY e.patient_id)vl_inclusion "
        + "               ON vl_inclusion.patient_id = p.patient_id "
        + "WHERE  e.encounter_type IN( ${13}, ${51} ) "
        + "       AND e.location_id = :location "
        + "       AND Date(e.encounter_datetime) = vl_inclusion.vl_date "
        + "       AND e.voided = 0 "
        + "       AND p.voided = 0 "
        + "       AND o.voided = 0 "
        + "       AND o.concept_id = ${856} "
        + "       AND o.value_numeric > 1000 "
        + "GROUP  BY p.patient_id ) vl "
        + "INNER JOIN ( "
        + "SELECT p.patient_id "
        + "             FROM   patient p "
        + "                    INNER JOIN encounter e ON e.patient_id = p.patient_id "
        + "                    INNER JOIN obs o ON o.encounter_id = e.encounter_id "
        + "                    INNER JOIN ( "
        + "                 SELECT p.patient_id , MAX(Date(e.encounter_datetime)) AS second_vl "
        + "                 FROM   patient p "
        + "                            INNER JOIN encounter e ON e.patient_id = p.patient_id "
        + "                                INNER JOIN obs o ON o.encounter_id = e.encounter_id "
        + "                            INNER JOIN ( "
        + "                     SELECT p.patient_id , MAX(Date(e.encounter_datetime)) as last_vl1000_date "
        + "                     FROM   patient p "
        + "                                INNER JOIN encounter e ON e.patient_id = p.patient_id "
        + "                                INNER JOIN obs o ON o.encounter_id = e.encounter_id "
        + "                                INNER JOIN (SELECT e.patient_id, MAX(Date(e.encounter_datetime)) vl_date "
        + "                                            FROM   encounter e "
        + "                                                       INNER JOIN obs o ON o.encounter_id = e.encounter_id "
        + "                                                AND e.voided = 0 "
        + "                                                AND o.voided = 0 "
        + "                                                AND e.encounter_type IN ( ${13}, ${51} ) "
        + "                                                AND e.location_id = :location "
        + "                                     AND ( ( o.concept_id = ${856} "
        + "                                         AND o.value_numeric IS NOT NULL ) "
        + "                                       OR ( o.concept_id = ${1305} "
        + "                                            AND o.value_coded IS NOT NULL ) ) "
        + "                                                AND Date(e.encounter_datetime) BETWEEN :startDate AND :endDate "
        + "                                            GROUP  BY e.patient_id)vl_inclusion "
        + "                                           ON vl_inclusion.patient_id = e.patient_id "
        + "                     WHERE  e.encounter_type IN( ${13}, ${51} ) "
        + "                       AND e.location_id = :location "
        + "                       AND Date(e.encounter_datetime) = vl_inclusion.vl_date "
        + "                       AND e.voided = 0 "
        + "                       AND p.voided = 0 "
        + "                       AND o.voided = 0 "
        + "                       AND o.concept_id = ${856} "
        + "                       AND o.value_numeric > 1000 "
        + "                     GROUP BY p.patient_id "
        + "                 )last_vl100  ON last_vl100.patient_id = e.patient_id "
        + "                 WHERE  e.encounter_type IN( ${13}, ${51} ) "
        + "                   AND e.location_id = :location "
        + "                   AND Date(e.encounter_datetime) < last_vl100.last_vl1000_date "
        + "                   AND e.voided = 0 AND o.voided = 0"
        + "                   AND p.voided = 0 "
        + "                   AND ( ( o.concept_id = ${856} "
        + "                       AND o.value_numeric IS NOT NULL ) "
        + "                     OR ( o.concept_id = ${1305} "
        + "                          AND o.value_coded IS NOT NULL ) ) "
        + "                 GROUP  BY p.patient_id "
        + "             )second_vl "
        + "                            ON second_vl.patient_id = e.patient_id "
        + "             WHERE  e.encounter_type IN( ${13}, ${51} ) "
        + "                    AND e.location_id = :location "
        + "                    AND Date(e.encounter_datetime) = second_vl.second_vl "
        + "                    AND e.voided = 0 "
        + "                    AND p.voided = 0 "
        + "                    AND o.voided = 0 "
        + "                    AND o.concept_id = ${856} "
        + "                    AND o.value_numeric > 1000 "
        + "             GROUP  BY p.patient_id"
        + " )consecutive ON consecutive.patient_id =  vl.patient_id "
        + "             GROUP  BY vl.patient_id";
  }

  private static String getRestartedArtQuery(boolean restartDate) {
    String fromSQL =
        " FROM   patient p "
            + "       INNER JOIN (SELECT e.patient_id, e.encounter_datetime AS restart_date  "
            + "                   FROM   encounter e INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                          INNER JOIN(SELECT e.patient_id, "
            + "                                            Max(e.encounter_datetime) state_date "
            + "                                     FROM   encounter e INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                                     WHERE  e.voided = 0 "
            + "                                            AND o.voided = 0 "
            + "                                            AND e.encounter_type = ${6} "
            + "                                            AND o.concept_id = ${6273} "
            + "                                            AND e.location_id = :location "
            + "                                            AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "                                     GROUP  BY e.patient_id) recent_state "
            + "                                  ON recent_state.patient_id = e.patient_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND o.concept_id = ${6273} "
            + "                          AND o.value_coded = ${1705} "
            + "                          AND e.encounter_type = ${6} "
            + "                          AND e.location_id = :location "
            + "                          AND e.encounter_datetime = recent_state.state_date "
            + "                   GROUP  BY e.patient_id "
            + "                   UNION "
            + "                   SELECT e.patient_id, o.obs_datetime AS restart_date "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                          INNER JOIN(SELECT e.patient_id, "
            + "                                            Max(o.obs_datetime) state_date "
            + "                                     FROM   encounter e INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                                     WHERE  e.voided = 0 "
            + "                                            AND o.voided = 0 "
            + "                                            AND e.encounter_type = ${53} "
            + "                                            AND o.concept_id = ${6272} "
            + "                                            AND e.location_id = :location "
            + "                                            AND o.obs_datetime BETWEEN :startDate AND :endDate "
            + "                                     GROUP  BY e.patient_id) recent_state ON recent_state.patient_id = e.patient_id "
            + "                   WHERE  e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.encounter_type = ${53} "
            + "                          AND o.concept_id = ${6272} "
            + "                          AND o.value_coded = ${1705} "
            + "                          AND e.location_id = :location "
            + "                          AND o.obs_datetime = recent_state.state_date "
            + "                   GROUP  BY e.patient_id) reinitiated ON reinitiated.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "GROUP  BY p.patient_id";

    return restartDate
        ? " SELECT p.patient_id, reinitiated.restart_date AS eligibility_date ".concat(fromSQL)
        : " SELECT p.patient_id ".concat(fromSQL);
  }

  public enum Cd4CountComparison {
    LessThanOrEqualTo200mm3 {
      @Override
      public String getProposition() {
        return "o.value_numeric < 200";
      }

      @Override
      public String getCompositionString() {
        return getSearchKey();
      }

      @Override
      public String getSearchKey() {
        return "A";
      }
    },
    LessThanOrEqualTo200mm3AA {
      @Override
      public String getProposition() {
        return "o.value_numeric <= 200";
      }

      @Override
      public String getCompositionString() {
        return getSearchKey();
      }

      @Override
      public String getSearchKey() {
        return "AA";
      }
    },
    LessThanOrEqualTo500mm3 {
      @Override
      public String getProposition() {
        return "o.value_numeric < 500";
      }

      @Override
      public String getCompositionString() {
        return getSearchKey();
      }

      @Override
      public String getSearchKey() {
        return "B";
      }
    },
    LessThanOrEqualTo750mm3 {
      @Override
      public String getProposition() {
        return "o.value_numeric < 750";
      }

      @Override
      public String getCompositionString() {
        return getSearchKey();
      }

      @Override
      public String getSearchKey() {
        return "C";
      }
    },

    GreaterThanOrEqualTo200mm3 {
      @Override
      public String getProposition() {
        return "o.value_numeric >= 200";
      }

      @Override
      public String getCompositionString() {
        return getSearchKey();
      }

      @Override
      public String getSearchKey() {
        return "D";
      }
    },
    GreaterThanOrEqualTo500mm3 {
      @Override
      public String getProposition() {
        return "o.value_numeric >= 500";
      }

      @Override
      public String getCompositionString() {
        return getSearchKey();
      }

      @Override
      public String getSearchKey() {
        return "E";
      }
    },
    GreaterThanOrEqualTo750mm3 {
      @Override
      public String getProposition() {
        return "o.value_numeric >= 750";
      }

      @Override
      public String getCompositionString() {
        return getSearchKey();
      }

      @Override
      public String getSearchKey() {
        return "F";
      }
    };

    public abstract String getProposition();

    public abstract String getCompositionString();

    public abstract String getSearchKey();
  }

  private Map<String, Integer> getMetadata() {
    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    map.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    map.put("90", hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayPriorArtPatientConcept().getConceptId());
    map.put("1705", hivMetadata.getRestartConcept().getConceptId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("703", hivMetadata.getPositive().getConceptId());
    map.put("664", hivMetadata.getNegative().getConceptId());
    map.put("23951", tbMetadata.getTestTBLAM().getConceptId());
    map.put("23723", tbMetadata.getTBGenexpertTestConcept().getConceptId());
    map.put("165189", tbMetadata.getTestXpertMtbUuidConcept().getConceptId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    map.put("165185", hivMetadata.getPositivityLevelConcept().getConceptId());
    map.put("165348", hivMetadata.getFourPlusConcept().getConceptId());
    map.put("165188", hivMetadata.getThreePlusConcept().getConceptId());
    map.put("165187", hivMetadata.getTwoPlusConcept().getConceptId());
    map.put("165186", hivMetadata.getOnePlusConcept().getConceptId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("165349", tbMetadata.getTbLamPosivityLvelConcept().getConceptId());
    map.put("165515", hivMetadata.getCD4SemiQuantitativeConcept().getConceptId());
    map.put("5", hivMetadata.getARVAdultInitialEncounterType().getEncounterTypeId());
    map.put("1600", hivMetadata.getPregnancyDueDate().getConceptId());
    map.put("23821", hivMetadata.getSampleCollectionDateAndTime().getConceptId());
    map.put("6334", hivMetadata.getCriteriaForArtStart().getConceptId());
    map.put("8", hivMetadata.getPtvEtvProgram().getProgramId());
    map.put("1279", hivMetadata.getNumberOfWeeksPregnant().getConceptId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());
    map.put("6331", hivMetadata.getBPlusConcept().getConceptId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("5599", hivMetadata.getPriorDeliveryDateConcept().getConceptId());
    map.put("6332", hivMetadata.getBreastfeeding().getConceptId());
    map.put("27", hivMetadata.getPatientGaveBirthWorkflowState().getProgramWorkflowStateId());
    return map;
  }

  private String getEligibilityDateQuery() {
    return new EptsQueriesUtil()
        .unionBuilder(getArtStartDate())
        .union(getPregnantWithoutCd4Query())
        .union(getCd4CountOnPeriod())
        .union(getRestartedArtQuery(true))
        .buildQuery();
  }

  enum TbLamResult {
    POSITIVE {
      @Override
      public String getValueCoded() {
        return "${703}";
      }
    },
    NEGATIVE {
      @Override
      public String getValueCoded() {
        return "${664}";
      }
    };

    public abstract String getValueCoded();
  }
}
