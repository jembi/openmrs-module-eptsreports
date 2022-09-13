package org.openmrs.module.eptsreports.reporting.library.cohorts;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ListOfPatientsWithPositiveTbScreeningDataDefinitionQueries {

  private final HivMetadata hivMetadata;

  private final TbMetadata tbMetadata;

  private final ListOfPatientsWithPositiveTbScreeningCohortQueries
      listOfPatientsWithPositiveTbScreeningCohortQueries;

  @Autowired
  public ListOfPatientsWithPositiveTbScreeningDataDefinitionQueries(
          HivMetadata hivMetadata,
          TbMetadata tbMetadata, ListOfPatientsWithPositiveTbScreeningCohortQueries
          listOfPatientsWithPositiveTbScreeningCohortQueries) {
    this.hivMetadata = hivMetadata;
    this.tbMetadata = tbMetadata;
    this.listOfPatientsWithPositiveTbScreeningCohortQueries =
        listOfPatientsWithPositiveTbScreeningCohortQueries;
  }

  /**
   * <b>Date of the Last Positive TB screening (Data do Último Rastreio Positivo de TB)</b>
   *
   * <p>The system will identify and show the most recent date, amongst all sources, when the
   * patient had a positive screening for TB during the reporting period
   *
   * @see
   *     ListOfPatientsWithPositiveTbScreeningCohortQueries#getPatientsWithMostRecentTbScreeningDate()
   * @return {@link DataDefinition}
   */
  public DataDefinition getLastTbPositiveScreeningDate() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Date of the Last Positive TB screening");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    sqlPatientDataDefinition.setQuery(
        listOfPatientsWithPositiveTbScreeningCohortQueries
            .getTbPositiveScreeningFromSourcesQuery());

    return sqlPatientDataDefinition;
  }


  /**
   * <b>GeneXpert Request Date</b>
   * <p>
   *     The system will identify and show the most recent clinical consultation
   *     with GeneXpert request marked in Investigações – Pedidos Laboratoriais
   *     section of Ficha Clínica registered between the Last Positive TB Screening
   *     Date (Value of Column H) and report generation date.
   * </p>
   *
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getTbGenexpertRequestDate(List<Concept> testConcept) {
// STILL NEED TO BE CHECKED BEFORE PR
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("GeneXpert Request Dat");
    sqlPatientDataDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("generationDate", "generationDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, String> map = new HashMap<>();
    map.put("6", String.valueOf(hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId()));
    map.put("23723", String.valueOf(tbMetadata.getTBGenexpertTestConcept().getConceptId()));
    map.put("testConcept", StringUtils.join(testConcept,","));

    String query =
            "SELECT p.patient_id,e.encounter_datetime AS recent_date "
                    + "FROM   patient p "
                    + "       INNER JOIN encounter e "
                    + "               ON e.patient_id = p.patient_id "
                    + "       INNER JOIN obs o "
                    + "               ON o.encounter_id = e.encounter_id "
                    + "      INNER JOIN ( "
                    + " SELECT positive.patient_id, MAX(positive.recent_date) as recent_date FROM ( "
                    + listOfPatientsWithPositiveTbScreeningCohortQueries.getTbPositiveScreeningFromSourcesQuery()
                    + " ) positive GROUP BY positive.patient_id ) as positiveScreening ON positiveScreening.patient_id = p.patient_id "
                    + "WHERE  e.encounter_type = ${6} "
                    + "       AND e.location_id = :location "
                    + "       AND o.concept_id = ${23722} "
                    + "       AND o.value_coded IN ( ${testConcept} ) "
                    + "       AND e.encounter_datetime >= positiveScreening.recent_date "
                    + "AND e.encounter_datetime <= :generationDate "
                    + "       AND p.voided = 0 "
                    + "       AND e.voided = 0 "
                    + "       AND o.voided = 0 ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }
}
