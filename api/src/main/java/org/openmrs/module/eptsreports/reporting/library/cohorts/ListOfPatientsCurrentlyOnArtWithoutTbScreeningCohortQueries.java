package org.openmrs.module.eptsreports.reporting.library.cohorts;

import org.apache.commons.text.StringSubstitutor;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class ListOfPatientsCurrentlyOnArtWithoutTbScreeningCohortQueries {

  @Autowired private TxCurrCohortQueries txCurrCohortQueries;

  @Autowired private TXTBCohortQueries txtbCohortQueries;

  @Autowired private CommonQueries commonQueries;

  @Autowired
  private HivMetadata hivMetadata;

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
  }public DataDefinition getDispensationTypeOnEncounter(EncounterType encounterType) {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Dispensation Type on Encounter "+ encounterType.getName());
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "End Date", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("23739", hivMetadata.getTypeOfDispensationConcept().getConceptId());
    valuesMap.put("encounter", encounterType.getEncounterTypeId());

    String query =
             "SELECT p.patient_id , o.value_coded "
            + "FROM patient p "
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "INNER JOIN( "
            + "				SELECT p.patient_id , MAX(e.encounter_datetime) encounter_date "
            + "				FROM patient p "
            + "				INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "				WHERE e.encounter_type =  ${encounter}"
            + "				AND e.location_id = :location "
            + "				AND e.encounter_datetime <= :endDate "
            + "				AND p.voided = 0 "
            + "				AND e.voided = 0 "
            + "				GROUP BY p.patient_id "
            + ") recent_fila ON recent_fila.patient_id = p.patient_id "
            + " "
            + "WHERE e.encounter_type =  ${encounter}"
            + "AND e.location_id = :location "
            + "AND e.encounter_datetime = recent_fila.encounter_date "
            + "AND o.concept_id = ${23739} "
            + "AND p.voided = 0 "
            + "AND e.voided = 0 "
            + "AND o.voided = 0 "
            + "GROUP BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(valuesMap);
    sqlPatientDataDefinition.setQuery(sb.replace(query));

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
