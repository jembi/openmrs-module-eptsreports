package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsWithHighViralLoadCohortQueries {

  private final HivMetadata hivMetadata;

  private final CommonMetadata commonMetadata;

  @Autowired
  public ListOfPatientsWithHighViralLoadCohortQueries(
      HivMetadata hivMetadata, CommonMetadata commonMetadata) {
    this.hivMetadata = hivMetadata;
    this.commonMetadata = commonMetadata;
  }

  public DataDefinition getPatientCell() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    String query =
        ""
            + "SELECT address.patient_id, address.celula "
            + "             FROM   (SELECT p.patient_id,pa.address3 celula "
            + "                     FROM   patient p "
            + "                            INNER JOIN person pr ON p.patient_id = pr.person_id "
            + "                            INNER JOIN person_address pa ON pa.person_id = pr.person_id "
            + "                     WHERE  p.voided = 0 "
            + "                            AND pr.voided = 0 "
            + "                     ORDER  BY pa.person_address_id DESC) address "
            + "             GROUP  BY address.patient_id";

    spdd.setQuery(query);

    return spdd;
  }

  /**
   * <b>Data de Colheita da amostra com CV>1000 cp/ml (Sheet 1: Column M)</b>
   *
   * <p>Date of Sample collection recorded in Laboratory or FSR Form with the VL result > 1000 cp/ml
   * with the VL result date occurred during the reporting period. <br>
   * Note: if more than one VL result > 1000 cp/ml are registered during the period the first one
   * should be considered
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getVLSampleCollectionDate() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Data de Colheita da amostra com CV>1000 cp/ml");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    valuesMap.put("23821", commonMetadata.getSampleCollectionDateAndTime().getConceptId());

    String query =
        " SELECT p.patient_id, "
            + "       Min(o2.value_datetime) AS collection_date "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON p.patient_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + "       INNER JOIN obs o2 "
            + "                 ON e.encounter_id = o2.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND o2.voided = 0 "
            + "       AND e.encounter_type IN ( ${13}, ${51} ) "
            + "       AND (o.concept_id = ${856} "
            + "       AND o.value_numeric > 1000) "
            + "       AND e.location_id = :location "
            + "       AND (o2.concept_id = ${23821}"
            + "       AND o2.value_datetime >= :startDate "
            + "       AND o2.value_datetime <= :endDate) "
            + "GROUP  BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }

  /**
   * <b>Data de Recepção do Resultado de CV na US (Sheet 1: Column N)</b>
   *
   * <p>Date of the reception of the VL result > 1000 cp/ml registered in Laboratory or FSR form
   * during the reporting period (as First High Viral Load Result Date). <br>
   * Note: if more than one VL result > 1000 cp/ml are registered during the period the first one
   * should be considered
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getVLResultReceptionDate() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    spdd.setName("Data de Recepção do Resultado de CV na US");

    spdd.addParameter(new Parameter("startDate", "Cohort Start Date", Date.class));
    spdd.addParameter(new Parameter("endDate", "Cohort End Date", Date.class));
    spdd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("13", hivMetadata.getMisauLaboratorioEncounterType().getEncounterTypeId());
    valuesMap.put("51", hivMetadata.getFsrEncounterType().getEncounterTypeId());
    valuesMap.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String query =
        " SELECT p.patient_id, "
            + "       Min(e.encounter_datetime) AS result_date "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON p.patient_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type IN ( ${13}, ${51} ) "
            + "       AND o.concept_id = ${856} "
            + "       AND o.value_numeric > 1000 "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_datetime >= :startDate "
            + "       AND e.encounter_datetime <= :endDate "
            + "GROUP  BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(query));

    return spdd;
  }
}
