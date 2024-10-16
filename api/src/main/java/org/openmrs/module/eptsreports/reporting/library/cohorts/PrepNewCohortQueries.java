package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Date;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.library.queries.PrepNewQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PrepNewCohortQueries {

  private HivMetadata hivMetadata;

  private CommonMetadata commonMetadata;

  private GenericCohortQueries genericCohortQueries;

  @Autowired
  public PrepNewCohortQueries(
      HivMetadata hivMetadata,
      CommonMetadata commonMetadata,
      GenericCohortQueries genericCohortQueries) {
    this.hivMetadata = hivMetadata;
    this.commonMetadata = commonMetadata;
    this.genericCohortQueries = genericCohortQueries;
  }

  /**
   * The system will generate the PrEP_NEW indicator numerator as the number of clients (>=15 years)
   * newly enrolled on PrEP during the reporting period (PREP_NEW_FR10) with the specified
   * disaggregation (PREP_NEW_FR3).
   *
   * <p>The clients included are the adults (>=15 years old) who initiated the treatment
   * (PREP_NEW_FR4) during the reporting period.
   *
   * <p>The system will exclude the following clients:
   *
   * <ul>
   *   <li>Clients who were transferred in from another facility (PREP_NEW_FR5).
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getClientsWhoNewlyInitiatedPrep() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Clients Who Newly Initiated PrEP");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "A",
        EptsReportUtils.map(
            getClientsWhoInitiatedPrep(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "B",
        EptsReportUtils.map(
            getClientsWhoAreTransferredIn(), "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "C",
        EptsReportUtils.map(
            genericCohortQueries.getPatientAgeBasedOnPrepStartDate(15, 200),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("(A AND C) AND NOT B");

    return cd;
  }

  /**
   * <b>A: For the selected Location and reporting period (start Date and endDate) the system will
   * identify clients (>= 15 years old) who newly initiated PrEP as following</b>
   *
   * <p>All clients who have “O utente esta iniciar pela 1a vez a PrEP Data” (Concept id 165296 from
   * encounter type 80) value coded “Start drugs” (concept id 1256) and value datetime <=end date;
   * or
   *
   * @return
   */
  public CohortDefinition getClientsWhoInitiatedPrep() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Clients Who Initiated PrEP");
    sqlCohortDefinition.addParameter(new Parameter("startDate", " Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", " End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    String query =
        PrepNewQueries.getA(
            hivMetadata.getInitialStatusPrepUserConcept().getConceptId(),
            hivMetadata.getPrepInicialEncounterType().getEncounterTypeId(),
            hivMetadata.getStartDrugs().getConceptId(),
            hivMetadata.getPrepStartDateConcept().getConceptId());

    sqlCohortDefinition.setQuery(query);
    return sqlCohortDefinition;
  }

  /**
   * <b>B: Exclude all clients who have been transferred in as follows </b>
   *
   * <p>All clients who are enrolled in PrEP Program (PREP)(program id 25) and have the first
   * historical state as “Transferido de outra US” (patient state id= 76) in the client chart by end
   * of the reporting period; or
   *
   * <p>All clients who have marked “Transferido de outra US”(concept id 1594 value coded 1369) in
   * the first Ficha de Consulta Inicial PrEP(encounter type 80, Min(encounter datetime)) registered
   * in the system–by end of the reporting period.
   *
   * @return
   */
  public CohortDefinition getClientsWhoAreTransferredIn() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Clients WHo are Transferred In On PrEP Program or PrEP Inicial");
    sqlCohortDefinition.addParameter(new Parameter("endDate", " End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    String query =
        PrepNewQueries.getB(
            hivMetadata.getPrepProgram().getProgramId(),
            hivMetadata.getStateOfStayOnPrepProgram().getConceptId(),
            hivMetadata.getPrepInicialEncounterType().getEncounterTypeId(),
            hivMetadata.getReferalTypeConcept().getConceptId(),
            hivMetadata.getTransferredFromOtherFacilityConcept().getConceptId());

    sqlCohortDefinition.setQuery(query);
    return sqlCohortDefinition;
  }

  /**
   * Clients who are Pregnant In Prep New
   *
   * @return
   */
  public CohortDefinition getPregnantPatientsBasedOnPrepNew() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Clients who are Pregnant In Prep New");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    String query =
        PrepNewQueries.pregnantPatientsBasedOnPrepNew(
            hivMetadata.getPrepInicialEncounterType().getEncounterTypeId(),
            hivMetadata.getInitialStatusPrepUserConcept().getConceptId(),
            hivMetadata.getPrepTargetGroupConcept().getConceptId(),
            hivMetadata.getStartDrugs().getConceptId(),
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId(),
            commonMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getPrepStartDateConcept().getConceptId());

    sqlCohortDefinition.setQuery(query);
    return sqlCohortDefinition;
  }

  /**
   * Clients who are Breastfeeding In Prep New
   *
   * @return
   */
  public CohortDefinition getBreastfeedingPatientsBasedOnPrepNew() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Clients who are Breastfeeding In Prep New");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    String query =
        PrepNewQueries.breastfeedingPatientsBasedOnPrepNew(
            hivMetadata.getPrepInicialEncounterType().getEncounterTypeId(),
            hivMetadata.getInitialStatusPrepUserConcept().getConceptId(),
            hivMetadata.getPrepTargetGroupConcept().getConceptId(),
            hivMetadata.getStartDrugs().getConceptId(),
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId(),
            commonMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getPrepStartDateConcept().getConceptId());

    sqlCohortDefinition.setQuery(query);
    return sqlCohortDefinition;
  }
}
