package org.openmrs.module.eptsreports.reporting.calculation.formulations;

import java.util.*;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.ListResult;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.eptsreports.reporting.data.definition.CalculationDataDefinition;
import org.openmrs.module.eptsreports.reporting.utils.EptsCalculationUtils;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsFormulationCalculation extends AbstractPatientCalculation {
  private String FORMULATION_CALCULATION = "previousResults";

  @Override
  public CalculationResultMap evaluate(
      Collection<Integer> cohort,
      Map<String, Object> parameterValues,
      PatientCalculationContext context) {
    CalculationResultMap map = new CalculationResultMap();
    Integer column = (Integer) parameterValues.get("column");
    List<CalculationDataDefinition> calculationDataDefinitions =
        (List<CalculationDataDefinition>) parameterValues.get(FORMULATION_CALCULATION);
    HivMetadata hivMetadata = Context.getRegisteredComponents(HivMetadata.class).get(0);
    CalculationResultMap formulation = getFormulationMap(cohort, context, hivMetadata);
    Concept concept23784 = hivMetadata.getTdfAndFtcConcept();
    Concept concept21167 = hivMetadata.getAztAnd3tcAndAbcConcept();
    Concept concept21159 = hivMetadata.getAztAnd3tcAndNvpConcept();
    Concept concept165244 = hivMetadata.getTdfAnd3tcAndEfvConceptUuid();
    List<Integer> formulationConceptIdList =
        Arrays.asList(
            concept23784.getConceptId(),
            concept21159.getConceptId(),
            concept21167.getConceptId(),
            concept165244.getConceptId());
    for (Integer patientId : cohort) {
      ListResult listResult = (ListResult) formulation.get(patientId);
      List<Integer> conceptIdListResult = EptsCalculationUtils.extractResultValues(listResult);
      //   List<Integer> previousResults = ListOfPatientsFormulationCalculation.get(patientId);

      List<Integer> formulation2ConceptIdList =
          EptsCalculationUtils.extractResultValues(listResult);
      List<Integer> formulation3ConceptIdList =
          EptsCalculationUtils.extractResultValues(listResult);
      List<Integer> formulation4ConceptIdList =
          EptsCalculationUtils.extractResultValues(listResult);

      Integer conceptIdResult = null;
      for (Integer cid : conceptIdListResult) {
        if (formulationConceptIdList.contains(cid) && column == 1) {
          conceptIdResult = cid;
        } else if (formulation2ConceptIdList.contains(cid) && column == 2) {
          conceptIdResult = cid;
        } else if (formulation3ConceptIdList.contains(cid) && column == 3) {
          conceptIdResult = cid;
        } else if (formulation4ConceptIdList.contains(cid) && column == 4) {
          conceptIdResult = cid;
        }
      }
      map.put(patientId, new SimpleResult(conceptIdResult, this));
    }
    return map;
  }

  private CalculationResultMap getFormulationMap(
      Collection<Integer> cohort, PatientCalculationContext context, HivMetadata hivMetadata) {
    SqlPatientDataDefinition def = new SqlPatientDataDefinition();
    def.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    def.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("165256", hivMetadata.getArtDrugFormulationConcept().getConceptId());
    map.put("165252", hivMetadata.getDrugAndQuantityConcept().getConceptId());
    String sql =
        "SELECT p.patient_id, c.concept_id "
            + "FROM patient p "
            + "         INNER JOIN encounter e on p.patient_id = e.patient_id "
            + "         INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "         INNER JOIN drug d on d.drug_id = o.value_drug "
            + "         INNER JOIN concept c on d.concept_id = c.concept_id "
            + "         INNER JOIN concept_name cn  on cn.concept_id = c.concept_id "
            + "         INNER JOIN( "
            + "                    SELECT p.patient_id, MAX(e.encounter_datetime) AS e_encounter_date "
            + "                    FROM patient p "
            + "                             INNER JOIN encounter e on p.patient_id = e.patient_id "
            + "                             INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "                    WHERE p.voided = 0 "
            + "                      AND e.voided = 0 "
            + "                      AND o.voided = 0 "
            + "                      AND e.encounter_type = ${18} "
            + "                      AND e.encounter_datetime <= :onOrBefore  "
            + "                      AND e.location_id = :location  "
            + "                    GROUP BY p.patient_id "
            + "             ) AS max_farmacia ON max_farmacia.patient_id= p.patient_id "
            + "WHERE p.voided = 0 "
            + "    AND  e.voided = 0 "
            + "    AND  o.voided  = 0 "
            + "    AND  cn.voided  = 0 "
            + "    AND o.concept_id =  ${165256} "
            + "    AND e.encounter_type = ${18} "
            + "    AND e.encounter_datetime <= :onOrBefore "
            + "    AND e.location_id = :location  "
            + "  AND e.encounter_datetime = max_farmacia.e_encounter_date "
            + "  AND o.obs_group_id = ${165252}";
    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    def.setQuery(stringSubstitutor.replace(sql));
    Map<String, Object> params = new HashMap<>();
    params.put("location", context.getFromCache("location"));
    params.put("onOrBefore", context.getFromCache("onOrBefore"));
    return EptsCalculationUtils.evaluateWithReporting(def, cohort, params, null, context);
  }
}
