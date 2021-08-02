package org.openmrs.module.eptsreports.reporting.calculation.formulations;

import java.util.*;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.ListResult;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.eptsreports.reporting.calculation.common.EPTSCalculationService;
import org.openmrs.module.eptsreports.reporting.utils.EptsCalculationUtils;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.stereotype.Component;

@Component
public class ListOfChildrenOnARTFormulation1Calculation extends AbstractPatientCalculation {

  @Override
  public CalculationResultMap evaluate(
      Collection<Integer> cohort,
      Map<String, Object> parameterValues,
      PatientCalculationContext context) {
    CalculationResultMap map = new CalculationResultMap();

    HivMetadata hivMetadata = Context.getRegisteredComponents(HivMetadata.class).get(0);
    CalculationResultMap formulation1 = getFormulationMap(cohort, context, hivMetadata);
    EPTSCalculationService ePTSCalculationService =
        Context.getRegisteredComponents(EPTSCalculationService.class).get(0);
    Location location = (Location) context.getFromCache("location");

    CalculationResultMap calculationResultMap =
        ePTSCalculationService.getObs(
            hivMetadata.getArtDrugFormulationConcept(),
            Arrays.asList(hivMetadata.getARVPharmaciaEncounterType()),
            cohort,
            Arrays.asList(location),
            null,
            TimeQualifier.ANY,
            null,
            context);

    for (Integer patientId : cohort) {
      ListResult listResult = (ListResult) calculationResultMap.get(patientId);
      List<Obs> obsList = EptsCalculationUtils.extractResultValues(listResult);

      SimpleResult result = (SimpleResult) formulation1.get(patientId);
      System.out.println("Formulation for patient " + patientId + " is :" + obsList);

      if (result != null) {
        System.out.println("Result Is: " + result + " for patient " + patientId);
        map.put(patientId, new SimpleResult(result, this));
      }
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
        "SELECT p.patient_id, cn.name "
            + "FROM patient p "
            + "         INNER JOIN encounter e on p.patient_id = e.patient_id "
            + "         INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "         INNER JOIN drug d on d.drug_id = o.value_drug "
            + "         INNER JOIN concept c on d.concept_id = c.concept_id "
            + "         INNER JOIN concept_name cn  on cn.concept_id = c.concept_id "
            + "         INNER JOIN ( "
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
            + "    AND  cn.locale  = 'pt' "
            + "    AND o.concept_id =  ${165256} "
            + "    AND e.encounter_type = ${18} "
            + "    AND e.encounter_datetime <= :onOrBefore "
            + "    AND e.location_id = :location  "
            + "  AND e.encounter_datetime = max_farmacia.e_encounter_date ";
    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    def.setQuery(stringSubstitutor.replace(sql));
    Map<String, Object> params = new HashMap<>();
    params.put("location", context.getFromCache("location"));
    params.put("onOrBefore", context.getFromCache("onOrBefore"));
    return EptsCalculationUtils.evaluateWithReporting(def, cohort, params, null, context);
  }
}
