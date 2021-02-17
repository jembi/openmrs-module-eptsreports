package org.openmrs.module.eptsreports.reporting.calculation.cxcascrn;

import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.module.eptsreports.reporting.calculation.AbstractPatientCalculation;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

/**
 *
 */

@Component
public class CXCATreatmentCalculation  extends AbstractPatientCalculation {

    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> parameterValues, PatientCalculationContext context) {
        return null;
    }
}
