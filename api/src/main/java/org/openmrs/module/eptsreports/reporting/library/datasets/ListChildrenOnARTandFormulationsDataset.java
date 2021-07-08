package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.*;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.eptsreports.reporting.calculation.formulations.ListOfPatientsFormulationCalculation;
import org.openmrs.module.eptsreports.reporting.calculation.generic.InitialArtStartDateCalculation;
import org.openmrs.module.eptsreports.reporting.data.converter.CalculationResultConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.EncounterDatetimeConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.GenderConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.SimpleResultsConverter;
import org.openmrs.module.eptsreports.reporting.data.definition.CalculationDataDefinition;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.data.person.definition.*;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListChildrenOnARTandFormulationsDataset extends BaseDataSet {

  @Autowired private HivMetadata hivMetadata;

  @Autowired private TbMetadata tbMetadata;

  public DataSetDefinition constructDataset() {

    PatientDataSetDefinition patientDataSetDefinition = new PatientDataSetDefinition();

    patientDataSetDefinition.setName("Formulations");
    patientDataSetDefinition.addParameters(getParameters());

    PatientIdentifierType identifierType =
        Context.getPatientService()
            .getPatientIdentifierTypeByUuid("e2b966d0-1d5f-11e0-b929-000c29ad1d07");
    DataConverter identifierFormatter = new ObjectFormatter("{identifier}");

    DataDefinition identifierDef =
        new ConvertedPatientDataDefinition(
            "identifier",
            new PatientIdentifierDataDefinition(identifierType.getName(), identifierType),
            identifierFormatter);

    DataConverter formatter = new ObjectFormatter("{familyName}, {givenName}");

    DataDefinition nameDef =
        new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), formatter);

    patientDataSetDefinition.setParameters(getParameters());

    patientDataSetDefinition.addColumn("id", new PersonIdDataDefinition(), "");
    patientDataSetDefinition.addColumn("nid", identifierDef, "");
    patientDataSetDefinition.addColumn("name", nameDef, "");
    patientDataSetDefinition.addColumn(
        "artstartdate",
        getArtStartDate(),
        "onOrBefore=${endDate},location=${location}",
        new CalculationResultConverter());

    patientDataSetDefinition.addColumn(
        "gender", new GenderDataDefinition(), "", new GenderConverter());
    patientDataSetDefinition.addColumn("age", new AgeDataDefinition(), "", null);

    patientDataSetDefinition.addColumn(
        "ontbtreatment",
        this.getPatientsActiveOnTB(),
        "endDate=${endDate},location=${location}",
        new EncounterDatetimeConverter());

    patientDataSetDefinition.addColumn(
        "lastpickupdate",
        this.getLastDrugPickupDate(),
        "endDate=${endDate},location=${location}",
        new EncounterDatetimeConverter());

    patientDataSetDefinition.addColumn(
        "lastregimewithdrawal",
        getLastARVRegimen(),
        "onOrBefore=${endDate},location=${location}",
        new EncounterDatetimeConverter());
    // Formulations
    patientDataSetDefinition.addColumn(
        "formulation1",
        getFormulations(1),
        "onOrBefore=${endDate},location=${location}",
        new SimpleResultsConverter());

    patientDataSetDefinition.addColumn(
        "formulation2",
        getFormulations(2),
        "onOrBefore=${endDate},location=${location}",
        new SimpleResultsConverter());

    patientDataSetDefinition.addColumn(
        "formulation3",
        getFormulations(3),
        "onOrBefore=${endDate},location=${location}",
        new SimpleResultsConverter());

    patientDataSetDefinition.addColumn(
        "formulation4",
        getFormulations(4),
        "onOrBefore=${endDate},location=${location}",
        new SimpleResultsConverter());

    patientDataSetDefinition.addColumn(
        "nextpickupdate",
        getNextDrugPickupDate(),
        "onOrBefore=${endDate},location=${location}",
        new EncounterDatetimeConverter());

    patientDataSetDefinition.addColumn(
        "lastconsultationdate",
        this.getLastFollowupConsultationDate(),
        "endDate=${endDate},location=${location}",
        new EncounterDatetimeConverter());

    patientDataSetDefinition.addColumn(
        "lastregimeconsultation",
        this.getARVRegimenLastConsultationDate(),
        "endDate=${endDate},location=${location}",
        new EncounterDatetimeConverter());

    patientDataSetDefinition.addColumn(
        "weight",
        this.getWeightLossLastConsultationDate(),
        "endDate=${endDate},location=${location}",
        new EncounterDatetimeConverter());

    patientDataSetDefinition.addColumn(
        "familyapproachlastconsultation",
        this.getAbordagemFamiliarOnLastConsultationDate(),
        "endDate=${endDate},location=${location}",
        new EncounterDatetimeConverter());

    patientDataSetDefinition.addColumn(
        "quartelydismissallastconsultation",
        this.get3MonthsDispensationOnLastConsultationDate(),
        "endDate=${endDate},location=${location}",
        new EncounterDatetimeConverter());

    patientDataSetDefinition.addColumn(
        "nextconsultationdate",
        this.getNextFollowUpConsultationDate(),
        "endDate=${endDate},location=${location}",
        new EncounterDatetimeConverter());

    return patientDataSetDefinition;
  }

  @Override
  public List<Parameter> getParameters() {
    List<Parameter> parameters = new ArrayList<>();
    parameters.add(ReportingConstants.END_DATE_PARAMETER);
    parameters.add(ReportingConstants.LOCATION_PARAMETER);
    return parameters;
  }

  private DataDefinition getArtStartDate() {
    CalculationDataDefinition cd =
        new CalculationDataDefinition(
            "Art start date",
            Context.getRegisteredComponents(InitialArtStartDateCalculation.class).get(0));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
    return cd;
  }

  /** 6 */
  private DataDefinition getPatientsActiveOnTB() {
    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();
    spdd.setName("Last ARV Regimen (FILA)");
    spdd.addParameter(new Parameter("location", "location", Location.class));
    spdd.addParameter(new Parameter("endDate", "endDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("1268", hivMetadata.getTBTreatmentPlanConcept().getConceptId());
    valuesMap.put("1113", hivMetadata.getTBDrugStartDateConcept().getConceptId());
    valuesMap.put("1406", hivMetadata.getOtherDiagnosis().getConceptId());
    valuesMap.put("42", tbMetadata.getPulmonaryTB().getConceptId());
    valuesMap.put("6269", hivMetadata.getActiveOnProgramConcept().getConceptId());
    valuesMap.put("5", hivMetadata.getTBProgram().getProgramId());
    valuesMap.put("23761", hivMetadata.getActiveTBConcept().getConceptId());
    valuesMap.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    valuesMap.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());

    String sql =
        " SELECT final_query.patient_id, CASE WHEN final_query.result_Value IS NOT NULL THEN 'ACTIVE' WHEN final_query.result_Value IS NULL THEN 'INACTIVE' "
            + " FROM "
            + "( "
            + "                SELECT p.patient_id, o.value_coded  AS result_Value"
            + "                FROM patient p "
            + "                         INNER JOIN encounter e on p.patient_id = e.patient_id "
            + "                         INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "                WHERE p.voided = 0 "
            + "                  AND e.voided = 0 "
            + "                  AND o.voided = 0 "
            + "                  AND e.location_id = :location "
            + "                  AND e.encounter_type = ${6} "
            + "                  AND ( "
            + "                        (o.concept_id = ${1268} AND o.value_coded = 1256) "
            + "                        OR "
            + "                        (o.concept_id = ${1113} AND o.obs_datetime "
            + "                            BETWEEN DATE_SUB(:endDate, INTERVAL 210 DAY) AND :endDate) "
            + "                    ) "
            + "                  AND e.encounter_datetime <= :endDate "
            + "                UNION  "
            + "                SELECT p.patient_id, o.value_datetime AS result_Value"
            + "                FROM patient p "
            + "                         INNER JOIN encounter e on p.patient_id = e.patient_id "
            + "                         INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "                WHERE p.voided = 0 "
            + "                  AND e.voided = 0 "
            + "                  AND o.voided = 0 "
            + "                  AND e.location_id = :location "
            + "                  AND e.encounter_type IN (${6},${9}) "
            + "                  AND o.concept_id = ${1113} "
            + "                  AND o.value_datetime "
            + "                            BETWEEN DATE_SUB( :endDate, INTERVAL 210 DAY ) AND :endDate "
            + "                  AND e.encounter_datetime <= :endDate "
            + "                UNION  "
            + "                SELECT p.patient_id, o.value_coded AS result_Value "
            + "                FROM patient p "
            + "                         INNER JOIN encounter e on p.patient_id = e.patient_id "
            + "                         INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "                WHERE p.voided = 0 "
            + "                  AND e.voided = 0 "
            + "                  AND o.voided = 0 "
            + "                  AND e.location_id = :location "
            + "                  AND e.encounter_type = ${53} "
            + "                  AND o.concept_id = ${1406} "
            + "                  AND o.value_coded = ${42} "
            + "                  AND o.obs_datetime "
            + "                    BETWEEN DATE_SUB ( :endDate, INTERVAL 210 DAY ) AND :endDate "
            + "                UNION  "
            + "                SELECT p.patient_id , cn.name AS result_Value "
            + "                FROM patient p "
            + "                         INNER JOIN patient_program pp ON p.patient_id = pp.patient_id "
            + "                         INNER JOIN program pgr ON pp.program_id = pgr.program_id "
            + "                         INNER JOIN patient_state ps on pp.patient_program_id = ps.patient_program_id "
            + "                         INNER JOIN program_workflow_state pws  on ps.state = pws.program_workflow_state_id          "
            + "                         INNER JOIN concept_name cn  on pws.concept_id = cn.concept_id          "
            + "                WHERE p.voided = 0 "
            + "                  AND pp.voided = 0 "
            + "                  AND ps.voided = 0 "
            + "                  AND ps.state = ${6269} "
            + "                  AND pgr.program_id = ${5} "
            + "                  AND cn.locale = 'pt' "
            + "                  AND ps.start_date >= DATE_SUB(:endDate, INTERVAL 210 DAY) "
            + "                  AND ps.end_date <= :endDate "
            + "               UNION  "
            + "                SELECT p.patient_id, o.value_coded  AS result_Value"
            + "                FROM patient p "
            + "                         INNER JOIN encounter e on p.patient_id = e.patient_id "
            + "                         INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "                WHERE p.voided = 0 "
            + "                  AND e.voided = 0 "
            + "                  AND o.voided = 0 "
            + "                  AND e.encounter_type = ${6} "
            + "                  AND e.location_id = :location "
            + "                  AND o.concept_id = ${23761} "
            + "                  AND o.value_coded = ${1065} "
            + "                  AND e.encounter_datetime "
            + "                    BETWEEN DATE_SUB ( :endDate, INTERVAL 210 DAY ) AND :endDate"
            + ") AS final_query "
            + " WHERE p.voided = 0";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(sql));
    return spdd;
  }

  /**
   * 8
   *
   * @return
   */
  private DataDefinition getLastARVRegimen() {
    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();
    spdd.setName("Last ARV Regimen (FILA)");
    spdd.addParameter(new Parameter("location", "location", Location.class));
    spdd.addParameter(new Parameter("endDate", "endDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    valuesMap.put("1088", hivMetadata.getRegimeConcept().getConceptId());

    String sql =
        " SELECT p.patient_id, o.value_coded "
            + " FROM patient p"
            + "   INNER JOIN "
            + "   ( SELECT p.patient_id, MAX(e.encounter_datetime) as encounter_datetime "
            + "     FROM  patient p  "
            + "         INNER JOIN encounter e  ON p.patient_id = e.patient_id "
            + "     WHERE p.voided = 0 "
            + "         AND p.voided = 0  "
            + "         AND e.location_id = :location "
            + "         AND e.encounter_datetime <= :endDate "
            + "         AND e.encounter_type = ${18}"
            + "     GROUP BY p.patient_id "
            + "   ) max_encounter ON p.patient_id=max_encounter.patient_id"
            + "     INNER JOIN encounter e ON p.patient_id= e.patient_id "
            + "     INNER JOIN obs ob ON e.encounter_id = o.encounter_id "
            + "     INNER JOIN concept_name cn on ob.concept_id=cn.concept_id "
            + " WHERE  p.voided = 0"
            + "     AND e.voided = 0 "
            + "     AND o.voided = 0"
            + "     AND cn.locale='pt'"
            + "     AND max_encounter.encounter_datetime = e.encounter_datetime "
            + "     AND e.encounter_type = ${18} "
            + "     AND e.location_id = :location "
            + "     AND o.concept_id = ${1088} "
            + "     AND o.value_coded IS NOT NULL "
            + "     AND e.encounter_datetime <= :endDate ";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(sql));
    return spdd;
  }

  /**
   * 7
   *
   * @return
   */
  private DataDefinition getLastDrugPickupDate() {
    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();
    spdd.setName("Last Drug Pick Up Date");
    spdd.addParameter(new Parameter("location", "location", Location.class));
    spdd.addParameter(new Parameter("endDate", "endDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());

    String sql =
        " SELECT p.patient_id, MAX(e.encounter_datetime) "
            + " FROM   patient p  "
            + "          INNER JOIN encounter e  "
            + "                          ON p.patient_id = e.patient_id  "
            + " WHERE  p.voided = 0  "
            + "          AND e.voided = 0  "
            + "          AND e.location_id = :location "
            + "          AND e.encounter_type = ${18} "
            + "         AND e.encounter_datetime <= :endDate "
            + " GROUP BY p.patient_id ";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(sql));
    return spdd;
  }

  /**
   * 13
   *
   * @return
   */
  private DataDefinition getNextDrugPickupDate() {
    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();
    spdd.setName("Next Drug Pick Up Date");
    spdd.addParameter(new Parameter("location", "location", Location.class));
    spdd.addParameter(new Parameter("endDate", "endDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    valuesMap.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());

    String sql =
        ""
            + "  SELECT p.patient_id, o.value_datetime "
            + " FROM   patient p  "
            + "     INNER JOIN encounter e  "
            + "         ON p.patient_id = e.patient_id  "
            + "     INNER JOIN obs o  "
            + "         ON e.encounter_id = o.encounter_id  "
            + "     INNER JOIN ("
            + "             SELECT p.patient_id, MAX(e.encounter_datetime) as e_datetime "
            + "             FROM   patient p  "
            + "                 INNER JOIN encounter e  "
            + "                     ON p.patient_id = e.patient_id  "
            + "                 INNER JOIN obs o  "
            + "                     ON e.encounter_id = o.encounter_id  "
            + "             WHERE  p.voided = 0  "
            + "                 AND e.voided = 0  "
            + "                 AND o.voided = 0  "
            + "                 AND e.location_id = :location "
            + "                 AND e.encounter_type IN (${18}) "
            + "                 AND e.encounter_datetime <= :endDate "
            + "             GROUP BY p.patient_id "
            + "                   ) most_recent  ON p.patient_id = most_recent.patient_id   "
            + " WHERE  p.voided = 0  "
            + "     AND e.voided = 0  "
            + "     AND o.voided = 0  "
            + "     AND e.location_id = :location "
            + "     AND e.encounter_type IN (${18}) "
            + "     AND e.encounter_datetime <= :endDate "
            + "     AND e.encounter_datetime = most_recent.e_datetime "
            + "     AND o.concept_id = ${5096} ";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(sql));
    return spdd;
  }

  /**
   * 14
   *
   * @return
   */
  private DataDefinition getLastFollowupConsultationDate() {
    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();
    spdd.setName("Last Follow up Consultation Date");
    spdd.addParameter(new Parameter("location", "location", Location.class));
    spdd.addParameter(new Parameter("endDate", "endDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());

    String sql =
        " SELECT p.patient_id, MAX(e.encounter_datetime) "
            + " FROM   patient p  "
            + "         INNER JOIN encounter e  "
            + "                         ON p.patient_id = e.patient_id  "
            + "         INNER JOIN obs o  "
            + "                         ON e.encounter_id = o.encounter_id  "
            + "WHERE  p.voided = 0  "
            + "         AND e.voided = 0  "
            + "         AND o.voided = 0  "
            + "         AND e.location_id = :location "
            + "         AND e.encounter_type IN (${6}, ${9}) "
            + "         AND e.encounter_datetime <= :endDate "
            + "GROUP BY p.patient_id ";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(sql));
    return spdd;
  }

  /**
   * 15
   *
   * @return
   */
  private DataDefinition getARVRegimenLastConsultationDate() {
    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();
    spdd.setName("ARV Regimen on Last Consultation");
    spdd.addParameter(new Parameter("location", "location", Location.class));
    spdd.addParameter(new Parameter("endDate", "endDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("1087", hivMetadata.getPreviousARVUsedForTreatmentConcept().getConceptId());
    String sql =
        ""
            + "  SELECT p.patient_id, o.value_coded "
            + " FROM   patient p  "
            + "     INNER JOIN encounter e  "
            + "         ON p.patient_id = e.patient_id  "
            + "     INNER JOIN obs o  "
            + "         ON e.encounter_id = o.encounter_id  "
            + "     INNER JOIN ("
            + "             SELECT p.patient_id, MAX(e.encounter_datetime) as e_datetime "
            + "             FROM   patient p  "
            + "                 INNER JOIN encounter e  "
            + "                     ON p.patient_id = e.patient_id  "
            + "                 INNER JOIN obs o  "
            + "                     ON e.encounter_id = o.encounter_id  "
            + "             WHERE  p.voided = 0  "
            + "                 AND e.voided = 0  "
            + "                 AND o.voided = 0  "
            + "                 AND e.location_id = :location "
            + "                 AND e.encounter_type IN (${6}, ${9}) "
            + "                 AND e.encounter_datetime <= :endDate "
            + "             GROUP BY p.patient_id "
            + "                   ) most_recent  ON p.patient_id = most_recent.patient_id   "
            + " WHERE  p.voided = 0  "
            + "     AND e.voided = 0  "
            + "     AND o.voided = 0  "
            + "     AND e.location_id = :location "
            + "     AND e.encounter_type IN (${6}, ${9}) "
            + "     AND e.encounter_datetime <= :endDate "
            + "     AND e.encounter_datetime = most_recent.e_datetime "
            + "     AND o.concept_id = ${1087} ";
    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(sql));
    return spdd;
  }

  /**
   * 16
   *
   * @return
   */
  private DataDefinition getWeightLossLastConsultationDate() {
    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();
    spdd.setName("Weight on Last Consultation");
    spdd.addParameter(new Parameter("location", "location", Location.class));
    spdd.addParameter(new Parameter("endDate", "endDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("5089", hivMetadata.getWeightConcept().getConceptId());

    String sql =
        ""
            + "  SELECT p.patient_id, o.value_numeric "
            + " FROM   patient p  "
            + "     INNER JOIN encounter e  "
            + "         ON p.patient_id = e.patient_id  "
            + "     INNER JOIN obs o  "
            + "         ON e.encounter_id = o.encounter_id  "
            + "     INNER JOIN ("
            + "             SELECT p.patient_id, MAX(e.encounter_datetime) as e_datetime "
            + "             FROM   patient p  "
            + "                 INNER JOIN encounter e  "
            + "                     ON p.patient_id = e.patient_id  "
            + "                 INNER JOIN obs o  "
            + "                     ON e.encounter_id = o.encounter_id  "
            + "             WHERE  p.voided = 0  "
            + "                 AND e.voided = 0  "
            + "                 AND o.voided = 0  "
            + "                 AND e.location_id = :location "
            + "                 AND e.encounter_type IN (${6}, ${9}) "
            + "                 AND e.encounter_datetime <= :endDate "
            + "             GROUP BY p.patient_id "
            + "                   ) most_recent  ON p.patient_id = most_recent.patient_id   "
            + " WHERE  p.voided = 0  "
            + "     AND e.voided = 0  "
            + "     AND o.voided = 0  "
            + "     AND e.location_id = :location "
            + "     AND e.encounter_type IN (${6}, ${9}) "
            + "     AND e.encounter_datetime <= :endDate "
            + "     AND e.encounter_datetime = most_recent.e_datetime "
            + "     AND o.concept_id = ${5089} ";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(sql));
    return spdd;
  }

  /**
   * 17
   *
   * @return
   */
  private DataDefinition getAbordagemFamiliarOnLastConsultationDate() {
    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();
    spdd.setName("Abordagem Familiar on Last Consultation");
    spdd.addParameter(new Parameter("location", "location", Location.class));
    spdd.addParameter(new Parameter("endDate", "endDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("23725", hivMetadata.getFamilyApproach().getConceptId());

    String sql =
        ""
            + "  SELECT p.patient_id, o.value_coded "
            + " FROM   patient p  "
            + "     INNER JOIN encounter e  "
            + "         ON p.patient_id = e.patient_id  "
            + "     INNER JOIN obs o  "
            + "         ON e.encounter_id = o.encounter_id  "
            + "     INNER JOIN ("
            + "             SELECT p.patient_id, MAX(e.encounter_datetime) as e_datetime "
            + "             FROM   patient p  "
            + "                 INNER JOIN encounter e  "
            + "                     ON p.patient_id = e.patient_id  "
            + "                 INNER JOIN obs o  "
            + "                     ON e.encounter_id = o.encounter_id  "
            + "             WHERE  p.voided = 0  "
            + "                 AND e.voided = 0  "
            + "                 AND o.voided = 0  "
            + "                 AND e.location_id = :location "
            + "                 AND e.encounter_type IN (${6}, ${9}) "
            + "                 AND e.encounter_datetime <= :endDate "
            + "             GROUP BY p.patient_id "
            + "                   ) most_recent  ON p.patient_id = most_recent.patient_id   "
            + " WHERE  p.voided = 0  "
            + "     AND e.voided = 0  "
            + "     AND o.voided = 0  "
            + "     AND e.location_id = :location "
            + "     AND e.encounter_type IN (${6}, ${9}) "
            + "     AND e.encounter_datetime <= :endDate "
            + "     AND e.encounter_datetime = most_recent.e_datetime "
            + "     AND o.concept_id = ${23725} ";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(sql));
    return spdd;
  }

  /**
   * 17
   *
   * @return
   */
  private DataDefinition get3MonthsDispensationOnLastConsultationDate() {
    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();
    spdd.setName("3 month Dispensation on Last consultation");
    spdd.addParameter(new Parameter("location", "location", Location.class));
    spdd.addParameter(new Parameter("endDate", "endDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("23739", hivMetadata.getTypeOfDispensationConcept().getConceptId());
    valuesMap.put("23720", hivMetadata.getQuarterlyConcept().getConceptId());

    String sql =
        ""
            + "  SELECT p.patient_id, CASE WHEN e.encounter_datetime IS NOT NULL THEN Sim WHEN e.encounter_datetime IS NULL THEN NAO "
            + " FROM   patient p  "
            + "     INNER JOIN encounter e  "
            + "         ON p.patient_id = e.patient_id  "
            + "     INNER JOIN obs o  "
            + "         ON e.encounter_id = o.encounter_id  "
            + "     INNER JOIN ("
            + "             SELECT p.patient_id, MAX(e.encounter_datetime) as e_datetime "
            + "             FROM   patient p  "
            + "                 INNER JOIN encounter e  "
            + "                     ON p.patient_id = e.patient_id  "
            + "                 INNER JOIN obs o  "
            + "                     ON e.encounter_id = o.encounter_id  "
            + "             WHERE  p.voided = 0  "
            + "                 AND e.voided = 0  "
            + "                 AND o.voided = 0  "
            + "                 AND e.location_id = :location "
            + "                 AND e.encounter_type IN (${6}, ${9}) "
            + "                 AND e.encounter_datetime <= :endDate "
            + "             GROUP BY p.patient_id "
            + "                   ) most_recent  ON p.patient_id = most_recent.patient_id   "
            + " WHERE  p.voided = 0  "
            + "     AND e.voided = 0  "
            + "     AND o.voided = 0  "
            + "     AND e.location_id = :location "
            + "     AND e.encounter_type IN (${6}, ${9}) "
            + "     AND e.encounter_datetime <= :endDate "
            + "     AND e.encounter_datetime = most_recent.e_datetime "
            + "     AND o.concept_id = ${23739} "
            + "     AND o.value_coded = ${23720} ";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(sql));
    return spdd;
  }

  /**
   * 18
   *
   * @return
   */
  private DataDefinition getNextFollowUpConsultationDate() {
    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();
    spdd.setName("Next Follow up Consultation Date");
    spdd.addParameter(new Parameter("location", "location", Location.class));
    spdd.addParameter(new Parameter("endDate", "endDate", Date.class));

    Map<String, Integer> valuesMap = new HashMap<>();
    valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    valuesMap.put("1410", hivMetadata.getReturnVisitDateConcept().getConceptId());

    String sql =
        " SELECT p.patient_id, MAX(o.value_datetime) "
            + " FROM   patient p  "
            + "         INNER JOIN encounter e  "
            + "                         ON p.patient_id = e.patient_id  "
            + "         INNER JOIN obs o  "
            + "                         ON e.encounter_id = o.encounter_id  "
            + " WHERE  p.voided = 0  "
            + "         AND e.voided = 0  "
            + "         AND o.voided = 0  "
            + "         AND e.location_id = :location "
            + "         AND e.encounter_type IN (${6}, ${9}) "
            + "         AND o.concept_id = ${1410}  "
            + "         AND e.encounter_datetime <= :endDate "
            + " GROUP BY p.patient_id ";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(sql));
    return spdd;
  }

  private DataDefinition getFormulations(Integer column) {

    CalculationDataDefinition calculationDataDefinition =
        Context.getRegisteredComponents(ListOfPatientsFormulationCalculation.class).get(0);
    calculationDataDefinition.setName("Formulation Queries");
    calculationDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    calculationDataDefinition.addParameter(new Parameter("endDate", "endDate", Location.class));

    calculationDataDefinition.addCalculationParameter("column", column);

    return calculationDataDefinition;
  }
}
