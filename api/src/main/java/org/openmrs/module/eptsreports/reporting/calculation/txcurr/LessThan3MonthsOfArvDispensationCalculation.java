package org.openmrs.module.eptsreports.reporting.calculation.txcurr;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.eptsreports.reporting.calculation.BooleanResult;
import org.openmrs.module.eptsreports.reporting.calculation.common.EPTSCalculationService;
import org.openmrs.module.eptsreports.reporting.utils.EptsCalculationUtils;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.springframework.stereotype.Component;

@Component
public class LessThan3MonthsOfArvDispensationCalculation extends AbstractPatientCalculation {
  @Override
  public CalculationResultMap evaluate(
      Collection<Integer> cohort, Map<String, Object> map, PatientCalculationContext context) {

    HivMetadata hivMetadata = Context.getRegisteredComponents(HivMetadata.class).get(0);
    EPTSCalculationService ePTSCalculationService =
        Context.getRegisteredComponents(EPTSCalculationService.class).get(0);

    CalculationResultMap resultMap = new CalculationResultMap();

    Location location = (Location) context.getFromCache("location");
    Date onOrBefore = (Date) context.getFromCache("onOrBefore");

    EncounterType fila = hivMetadata.getARVPharmaciaEncounterType();
    EncounterType ficha = hivMetadata.getAdultoSeguimentoEncounterType();

    Concept returnVisitDateForArvDrugs = hivMetadata.getReturnVisitDateForArvDrugConcept();
    Concept typeOfDispensation = hivMetadata.getTypeOfDispensationConcept();
    Concept monthly = hivMetadata.getMonthlyConcept();

    CalculationResultMap getFilaEncounterMap =
        ePTSCalculationService.getEncounter(
            Arrays.asList(fila), TimeQualifier.LAST, cohort, location, onOrBefore, context);
    CalculationResultMap getFichaEncounterMap =
        ePTSCalculationService.getEncounter(
            Arrays.asList(ficha), TimeQualifier.LAST, cohort, location, onOrBefore, context);
    CalculationResultMap getLastEncounterWithReturnDateForArvMap =
        ePTSCalculationService.getObs(
            returnVisitDateForArvDrugs,
            Arrays.asList(fila),
            cohort,
            Arrays.asList(location),
            null,
            TimeQualifier.LAST,
            null,
            context);
    CalculationResultMap getLastEncounterWithDepositionAndMonthlyAsCodedValueMap =
        ePTSCalculationService.getObs(
            typeOfDispensation,
            Arrays.asList(ficha),
            cohort,
            Arrays.asList(location),
            Arrays.asList(monthly),
            TimeQualifier.LAST,
            null,
            context);

    for (Integer pId : cohort) {
      boolean found = false;

      Encounter lastFilaEncounter = EptsCalculationUtils.resultForPatient(getFilaEncounterMap, pId);
      Encounter lastFichaEncounter =
          EptsCalculationUtils.resultForPatient(getFichaEncounterMap, pId);

      Obs getObsWithReturnVisitDateFilled =
          EptsCalculationUtils.obsResultForPatient(getLastEncounterWithReturnDateForArvMap, pId);

      Obs getObsWithDepositionAndMonthlyAsCodedValue =
          EptsCalculationUtils.obsResultForPatient(
              getLastEncounterWithDepositionAndMonthlyAsCodedValueMap, pId);

      // case 1: fila as last encounter and has return visit date for drugs filled
      // Both 2 encounter are filled with relevant obseravtions
      // We consider the fila
      if (lastFilaEncounter != null
          && lastFichaEncounter != null
          && lastFilaEncounter.getEncounterDatetime() != null
          && lastFichaEncounter.getEncounterDatetime() != null
          && getObsWithReturnVisitDateFilled != null
          && getObsWithDepositionAndMonthlyAsCodedValue != null
          && getObsWithReturnVisitDateFilled.getEncounter() != null
          && getObsWithDepositionAndMonthlyAsCodedValue.getEncounter() != null
          && lastFichaEncounter.equals(getObsWithDepositionAndMonthlyAsCodedValue.getEncounter())
          && lastFilaEncounter.equals(getObsWithReturnVisitDateFilled.getEncounter())
          && getObsWithReturnVisitDateFilled.getEncounter().getEncounterDatetime() != null
          && lastFilaEncounter
              .getEncounterDatetime()
              .after(lastFichaEncounter.getEncounterDatetime())
          && EptsCalculationUtils.daysSince(
                  lastFilaEncounter.getEncounterDatetime(),
                  getObsWithReturnVisitDateFilled.getValueDatetime())
              < 83) {
        found = true;
      }
      // case 2: ficha as the last encounter and has Last TYPE OF DISPENSATION and value coded as
      // monthly, make sure the last encounter has required obs collected on them
      else if (lastFilaEncounter != null
          && lastFichaEncounter != null
          && lastFilaEncounter.getEncounterDatetime() != null
          && lastFichaEncounter.getEncounterDatetime() != null
          && getObsWithReturnVisitDateFilled != null
          && getObsWithDepositionAndMonthlyAsCodedValue != null
          && getObsWithReturnVisitDateFilled.getEncounter() != null
          && getObsWithDepositionAndMonthlyAsCodedValue.getEncounter() != null
          && lastFichaEncounter.equals(getObsWithDepositionAndMonthlyAsCodedValue.getEncounter())
          && lastFilaEncounter.equals(getObsWithReturnVisitDateFilled.getEncounter())
          && getObsWithReturnVisitDateFilled.getEncounter().getEncounterDatetime() != null
          && lastFichaEncounter
              .getEncounterDatetime()
              .after(lastFilaEncounter.getEncounterDatetime())) {
        found = true;
      }
      // case 3: Only fila available and has value datetime collected for the next drug pick up

      else if (lastFilaEncounter != null
          && lastFilaEncounter.getEncounterDatetime() != null
          && getObsWithReturnVisitDateFilled != null
          && getObsWithReturnVisitDateFilled.getEncounter() != null
          && getObsWithReturnVisitDateFilled.getEncounter().getEncounterDatetime() != null
          && lastFilaEncounter.equals(getObsWithReturnVisitDateFilled.getEncounter())
          && EptsCalculationUtils.daysSince(
                  lastFilaEncounter.getEncounterDatetime(),
                  getObsWithReturnVisitDateFilled.getValueDatetime())
              < 83) {
        found = true;
      }
      // case 4: if only ficha is available and has Last TYPE OF DISPENSATION and value coded as
      // monthly
      else if (lastFichaEncounter != null
          && getObsWithDepositionAndMonthlyAsCodedValue != null
          && getObsWithDepositionAndMonthlyAsCodedValue.getEncounter() != null
          && lastFichaEncounter.equals(getObsWithDepositionAndMonthlyAsCodedValue.getEncounter())) {
        found = true;
      }
      // case 5: if both fila and ficha are available taken on the same date, we pick fila first if
      // it has the next drug pick up date
      // otherwise we consider ficha if fila is null, but ficha has to contain the required obs to
      // pass
      else if (lastFilaEncounter != null
          && lastFilaEncounter.getEncounterDatetime() != null
          && lastFichaEncounter != null
          && lastFichaEncounter.getEncounterDatetime() != null
          && lastFilaEncounter
              .getEncounterDatetime()
              .equals(lastFichaEncounter.getEncounterDatetime())) {

        // check the fila if it has the value date time recorded for the next drug pick up
        if (getObsWithReturnVisitDateFilled != null
            && getObsWithReturnVisitDateFilled.getEncounter() != null
            && getObsWithReturnVisitDateFilled.getEncounter().getEncounterDatetime() != null
            && lastFilaEncounter.equals(getObsWithReturnVisitDateFilled.getEncounter())
            && EptsCalculationUtils.daysSince(
                    lastFilaEncounter.getEncounterDatetime(),
                    getObsWithReturnVisitDateFilled.getValueDatetime())
                < 83) {
          found = true;
        }
        // check if fila is empty, we check the ficha if there is any obs saved
        else if (getObsWithDepositionAndMonthlyAsCodedValue != null
            && getObsWithDepositionAndMonthlyAsCodedValue.getEncounter() != null
            && lastFichaEncounter.equals(
                getObsWithDepositionAndMonthlyAsCodedValue.getEncounter())) {
          found = true;
        }
      }
      resultMap.put(pId, new BooleanResult(found, this));
    }
    return resultMap;
  }
}
