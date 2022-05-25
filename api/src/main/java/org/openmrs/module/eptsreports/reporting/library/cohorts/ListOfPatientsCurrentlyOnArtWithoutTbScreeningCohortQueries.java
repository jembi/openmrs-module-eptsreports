package org.openmrs.module.eptsreports.reporting.library.cohorts;

import org.apache.commons.text.StringSubstitutor;
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

  @Autowired private HivMetadata hivMetadata;

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

  public DataDefinition getDispensationTypeOnClinicalAndPediatricEncounter() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Dispensation Type on Encounter 6 and 9 ");
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "End Date", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("23739", hivMetadata.getTypeOfDispensationConcept().getConceptId());
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());

    String query =
        "SELECT p.patient_id , o.value_coded "
            + "FROM patient p "
            + "INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "INNER JOIN( "
            + "				SELECT p.patient_id , MAX(e.encounter_datetime) encounter_date "
            + "				FROM patient p "
            + "				INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "				WHERE e.encounter_type IN (${6},${9}) "
            + "				AND e.location_id = :location "
            + "				AND e.encounter_datetime <= :endDate "
            + "				AND p.voided = 0 "
            + "				AND e.voided = 0 "
            + "				GROUP BY p.patient_id "
            + ") recent_fila ON recent_fila.patient_id = p.patient_id "
            + " "
            + "WHERE e.encounter_type IN (${6},${9}) "
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

  public DataDefinition getDispensationTypeOnFila() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Dispensation Type on FILA ");
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "End Date", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    valuesMap.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());

    String query =
        "SELECT p.patient_id, o.value_coded "
            + "FROM   patient p INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "       INNER JOIN(SELECT p.patient_id, MAX(e.encounter_datetime) encounter_date "
            + "                  FROM   patient p "
            + "                         INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                  WHERE  e.encounter_type = ${18} "
            + "                         AND e.location_id = :location "
            + "                         AND e.encounter_datetime <= :endDate "
            + "                         AND p.voided = 0 "
            + "                         AND e.voided = 0 "
            + "                  GROUP  BY p.patient_id) recent_fila "
            + "               ON recent_fila.patient_id = p.patient_id "
            + "WHERE  e.encounter_type = ${18} "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_datetime = recent_fila.encounter_date "
            + "       AND o.concept_id = ${165174} "
            + "       AND p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(valuesMap);
    sqlPatientDataDefinition.setQuery(sb.replace(query));

    return sqlPatientDataDefinition;
  }

  public DataDefinition getMostRecentDrugPickupDateOnRecepcaoLevantouArv() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Most Recent Drug Pick-Up on Recepção Levantou ARVs ");
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "End Date", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    valuesMap.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());

    String query =
        "SELECT p.patient_id, "
            + "       MAX(o.value_datetime) encounter_date "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  e.encounter_type = ${52} "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_datetime <= :endDate "
            + "       AND o.concept_id = ${23866} "
            + "       AND p.voided = 0 "
            + "       AND e.voided = 0 "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(valuesMap);
    sqlPatientDataDefinition.setQuery(sb.replace(query));

    return sqlPatientDataDefinition;
  }

  public DataDefinition getNextScheduledDrugPickupDateOnRecepcaoLevantouArv() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Most Recent Drug Pick-Up on Recepção Levantou ARVs ");
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "End Date", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    valuesMap.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());

    String query =
        "SELECT p.patient_id, "
            + "       MAX(DATE_ADD(o.value_datetime, INTERVAL 30 MONTH)) encounter_date "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  e.encounter_type = ${52} "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_datetime <= :endDate "
            + "       AND o.concept_id = ${23866} "
            + "       AND p.voided = 0 "
            + "       AND e.voided = 0 "
            + "GROUP  BY p.patient_id";

    StringSubstitutor sb = new StringSubstitutor(valuesMap);
    sqlPatientDataDefinition.setQuery(sb.replace(query));

    return sqlPatientDataDefinition;
  } public DataDefinition getMdcDispensationType(DispensationColumn dispensationColumn) {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Dispensation Type ");
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "End Date", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("165174", hivMetadata. getLastRecordOfDispensingModeConcept().getConceptId());
    valuesMap.put("165322", hivMetadata.getMdcState().getConceptId());
    valuesMap.put("1256", hivMetadata.getStartDrugs().getConceptId());
    valuesMap.put("1257", hivMetadata.getContinueRegimenConcept().getConceptId());

    String query =
             "SELECT dispensation.patient_id, " + dispensationColumn.getQuery()
            + "FROM  (SELECT p.patient_id, e.encounter_id, otype.obs_id, otype.value_coded "
            + "       FROM   patient p "
            + "              INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "              INNER JOIN obs otype ON otype.encounter_id = e.encounter_id "
            + "              INNER JOIN obs ostate ON ostate.encounter_id = e.encounter_id "
            + "              INNER JOIN (SELECT p.patient_id, Max(e.encounter_datetime) consultation_date "
            + "                          FROM   patient p "
            + "                                 INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                                 INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                          WHERE  e.encounter_type = ${6} "
            + "                                 AND e.location_id = :location "
            + "                                 AND e.encounter_datetime <= CURRENT_DATE() "
            + "                                 AND o.concept_id = ${165174} "
            + "                                 AND e.voided = 0 "
            + "                                 AND p.voided = 0 "
            + "                          GROUP  BY p.patient_id) most_recent "
            + "                      ON most_recent.patient_id = p.patient_id "
            + "       WHERE  e.encounter_datetime = most_recent.consultation_date "
            + "              AND e.encounter_type = ${6} "
            + "              AND e.location_id = :location "
            + "              AND otype.concept_id = ${165174} "
            + "              AND ostate.concept_id = ${165322} "
            + "              AND ostate.value_coded IN ( ${1256}, ${1257} ) "
            + "              AND otype.obs_group_id = ostate.obs_group_id "
            + "              AND e.voided = 0 "
            + "              AND p.voided = 0 "
            + "              AND otype.voided = 0 "
            + "              AND ostate.voided = 0 "
            + "       GROUP  BY p.patient_id, otype.obs_id) dispensation "
            + "GROUP  BY dispensation.patient_id, dispensation.obs_id";


    StringSubstitutor sb = new StringSubstitutor(valuesMap);
    sqlPatientDataDefinition.setQuery(sb.replace(query));

    return sqlPatientDataDefinition;
  }

  enum DispensationColumn{
    MDC1{
      @Override
      public String getQuery(){
        return   "( SELECT obs.value_coded "
                + "FROM   obs "
                + "WHERE  obs.encounter_id = dispensation.encounter_id "
                + "       AND obs.concept_id = 165174 "
                + "LIMIT  1 ) MDC1";
      }
    },
    MDC2{
      @Override
      public String getQuery(){
        return   "( SELECT obs.value_coded "
                + "FROM   obs "
                + "WHERE  obs.encounter_id = dispensation.encounter_id "
                + "       AND obs.concept_id = 165174 "
                + "LIMIT  1,1 ) MDC2";
      }
    },
    MDC3{
      @Override
      public String getQuery(){
        return   "( SELECT obs.value_coded "
                + "FROM   obs "
                + "WHERE  obs.encounter_id = dispensation.encounter_id "
                + "       AND obs.concept_id = 165174 "
                + "LIMIT  2,1 ) MDC3";
      }
    },
    MDC4{
      @Override
      public String getQuery(){
        return   "( SELECT obs.value_coded "
                + "FROM   obs "
                + "WHERE  obs.encounter_id = dispensation.encounter_id "
                + "       AND obs.concept_id = 165174 "
                + "LIMIT  3,1 ) MDC4";
      }
    },
    MDC5{
      @Override
      public String getQuery(){
        return   "( SELECT obs.value_coded "
                + "FROM   obs "
                + "WHERE  obs.encounter_id = dispensation.encounter_id "
                + "       AND obs.concept_id = 165174 "
                + "LIMIT  4,1 ) MDC5";
      }
    };

    public abstract String getQuery();
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
