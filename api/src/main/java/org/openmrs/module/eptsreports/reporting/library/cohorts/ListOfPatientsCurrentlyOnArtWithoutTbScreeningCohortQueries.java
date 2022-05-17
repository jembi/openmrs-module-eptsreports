package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Date;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.library.queries.CommonQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries {

  @Autowired private TxCurrCohortQueries txCurrCohortQueries;

  @Autowired private TXTBCohortQueries txtbCohortQueries;

  @Autowired private CommonQueries commonQueries;

  /**
   * <b>Technical Specs</b>
   *
   * <blockquote>
   *
   * <p><b>List of Patients currently on ART without TB Screening</b> <br>
   *
   * <p>From all patients currently on ART (TX_CURR) (TB_NSCRN_FR3) by reporting end date, the
   * system will exclude
   *
   * <p>All Patients on ART who were screened for TB symptoms at least once (TX_TB – Indicator
   * Denominator) during the 6 months’ period before the reporting end date
   *
   * </blockquote>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsCurrentlyOnArtWithoutTbScreening() {

    CompositionCohortDefinition composition = new CompositionCohortDefinition();
    addParameters(composition);
    composition.setName("Currently on ART without TB Screening");

    CohortDefinition txCurr = txCurrCohortQueries.getTxCurrBaseCohort();
    CohortDefinition txTbDenominator = txtbCohortQueries.getDenominator();

    composition.addSearch(
        "tx-curr", EptsReportUtils.map(txCurr, "endDate=${endDate},location=${location}"));
    composition.addSearch(
        "txtb-denominator",
        EptsReportUtils.map(
            txTbDenominator, "startDate=${endDate-6m},endDate=${endDate},location=${location}"));

    composition.setCompositionString("tx-curr AND NOT txtb-denominator");

    return composition;
  }

  public DataDefinition getArtStartDate() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Get ART Start Date");
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "End Date", Location.class));

    sqlPatientDataDefinition.setQuery(commonQueries.getARTStartDate(true));

    return sqlPatientDataDefinition;
  }

  private void addParameters(CohortDefinition cd) {
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));
  }

  private void addSqlCohortDefinitionParameters(SqlCohortDefinition sqlCohortDefinition) {
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
  }
}
