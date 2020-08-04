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
public class SixMonthsAndAboveOnArvDispensationCalculation extends AbstractPatientCalculation {

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
    Concept completedConcept = hivMetadata.getCompletedConcept();
    Concept quaterly = hivMetadata.getQuarterlyDispensation();
    Concept dispensaSemestra = hivMetadata.getSemiannualDispensation();
    Concept startDrugs = hivMetadata.getStartDrugs();
    Concept continueRegimen = hivMetadata.getContinueRegimenConcept();

    CalculationResultMap getLastFilaWithReturnVisitForDrugFilledMap =
        ePTSCalculationService.getObs(
            returnVisitDateForArvDrugs,
            Arrays.asList(fila),
            cohort,
            Arrays.asList(location),
            null,
            TimeQualifier.LAST,
            null,
            onOrBefore,
            context);
    CalculationResultMap getLastFichaWithSemestaral =
        ePTSCalculationService.getObs(
            typeOfDispensation,
            Arrays.asList(ficha),
            cohort,
            Arrays.asList(location),
            Arrays.asList(dispensaSemestra),
            TimeQualifier.LAST,
            null,
            onOrBefore,
            context);
    CalculationResultMap getLastFichaWithoutSemestralMap =
        ePTSCalculationService.getObs(
            typeOfDispensation,
            Arrays.asList(ficha),
            cohort,
            Arrays.asList(location),
            null,
            TimeQualifier.LAST,
            null,
            onOrBefore,
            context);
    CalculationResultMap lastDispensaTrimestralWithCompletedMap =
        ePTSCalculationService.getObs(
            dispensaSemestra,
            Arrays.asList(ficha),
            cohort,
            Arrays.asList(location),
            Arrays.asList(completedConcept),
            TimeQualifier.LAST,
            null,
            onOrBefore,
            context);
    CalculationResultMap lastDispensaSemestraWithStartOrContinueDrugsMap =
        ePTSCalculationService.getObs(
            dispensaSemestra,
            Arrays.asList(ficha),
            cohort,
            Arrays.asList(location),
            Arrays.asList(startDrugs, continueRegimen),
            TimeQualifier.LAST,
            null,
            onOrBefore,
            context);

    CalculationResultMap lastDispensaSemestraWithoutStartOrContinueDrugsMap =
        ePTSCalculationService.getObs(
            dispensaSemestra,
            Arrays.asList(ficha),
            cohort,
            Arrays.asList(location),
            null,
            TimeQualifier.LAST,
            null,
            onOrBefore,
            context);
    CalculationResultMap lastFichaEncounterMap =
        ePTSCalculationService.getEncounter(
            Arrays.asList(ficha), TimeQualifier.LAST, cohort, location, onOrBefore, context);
    CalculationResultMap lastFilaEncounterMap =
        ePTSCalculationService.getEncounter(
            Arrays.asList(fila), TimeQualifier.LAST, cohort, location, onOrBefore, context);
    for (Integer pId : cohort) {
      boolean found = false;
      // get last encounters
      Encounter lastFichaEncounter =
          EptsCalculationUtils.resultForPatient(lastFichaEncounterMap, pId);
      Encounter lastFilaEncounter =
          EptsCalculationUtils.resultForPatient(lastFilaEncounterMap, pId);

      Obs lastFilaWithReturnForDrugsObs =
          EptsCalculationUtils.obsResultForPatient(getLastFilaWithReturnVisitForDrugFilledMap, pId);
      Obs lastFichaObsWithSemestarlValueCoded =
          EptsCalculationUtils.obsResultForPatient(getLastFichaWithSemestaral, pId);
      Obs lastDispensaTrimestralWithCompltedObs =
          EptsCalculationUtils.obsResultForPatient(lastDispensaTrimestralWithCompletedMap, pId);
      Obs lastDispensaSemestraWithStartOrContinueDrugsObs =
          EptsCalculationUtils.obsResultForPatient(
              lastDispensaSemestraWithStartOrContinueDrugsMap, pId);

      Obs lastDispensaTrimestralWithoutSemestralObs =
          EptsCalculationUtils.obsResultForPatient(getLastFichaWithoutSemestralMap, pId);
      Obs lastDispensaSemestraWithoutStartOrContinueDrugsObs =
          EptsCalculationUtils.obsResultForPatient(
              lastDispensaSemestraWithoutStartOrContinueDrugsMap, pId);

      // case 1 fila filled is after ficha filled with semestral concept id
      if (lastFichaEncounter != null
          && lastFichaEncounter.getEncounterDatetime() != null
          && lastFilaWithReturnForDrugsObs != null
          && lastFilaWithReturnForDrugsObs.getEncounter() != null
          && lastFilaWithReturnForDrugsObs.getValueDatetime() != null
          && lastFilaWithReturnForDrugsObs
              .getEncounter()
              .getEncounterDatetime()
              .after(lastFichaEncounter.getEncounterDatetime())
          && EptsCalculationUtils.daysSince(
                  lastFilaWithReturnForDrugsObs.getEncounter().getEncounterDatetime(),
                  lastFilaWithReturnForDrugsObs.getValueDatetime())
              > 173) {
        found = true;

      }
      // case 2 ficha filled is after fila filled with semestral concept id reverse of 1
      else if (lastFilaWithReturnForDrugsObs != null
          && lastFilaWithReturnForDrugsObs.getEncounter() != null
          && lastFilaWithReturnForDrugsObs.getEncounter().getEncounterDatetime() != null
          && lastFichaObsWithSemestarlValueCoded != null
          && lastFichaObsWithSemestarlValueCoded.getEncounter() != null
          && lastFichaObsWithSemestarlValueCoded.getEncounter().getEncounterDatetime() != null
          && lastFichaObsWithSemestarlValueCoded
              .getEncounter()
              .getEncounterDatetime()
              .after(lastFilaWithReturnForDrugsObs.getEncounter().getEncounterDatetime())) {
        found = true;
      }
      // case 3 ficha filled is after fila filled with start or continue regimen concept id
      else if (lastFilaWithReturnForDrugsObs != null
          && lastFilaWithReturnForDrugsObs.getEncounter() != null
          && lastFilaWithReturnForDrugsObs.getEncounter().getEncounterDatetime() != null
          && lastDispensaSemestraWithStartOrContinueDrugsObs != null
          && lastDispensaSemestraWithStartOrContinueDrugsObs.getEncounter() != null
          && lastDispensaSemestraWithStartOrContinueDrugsObs.getEncounter().getEncounterDatetime()
              != null
          && lastDispensaSemestraWithStartOrContinueDrugsObs
              .getEncounter()
              .getEncounterDatetime()
              .after(lastFilaWithReturnForDrugsObs.getEncounter().getEncounterDatetime())) {
        found = true;

      }
      // case 4 if there are multiple fila filled/only fila available for the same date, pick the
      // latest that has
      // information filled
      else if (lastFilaWithReturnForDrugsObs != null
          && lastFilaWithReturnForDrugsObs.getEncounter() != null
          && lastFilaWithReturnForDrugsObs.getEncounter().getEncounterDatetime() != null
          && lastDispensaTrimestralWithoutSemestralObs == null
          && lastDispensaSemestraWithoutStartOrContinueDrugsObs == null
          && lastFilaWithReturnForDrugsObs.getValueDatetime() != null
          && EptsCalculationUtils.daysSince(
                  lastFilaWithReturnForDrugsObs.getEncounter().getEncounterDatetime(),
                  lastFilaWithReturnForDrugsObs.getValueDatetime())
              > 173) {

        found = true;
      }
      // case 6 if ficha filled is after fila filled with semestral concept id
      else if ((lastFichaObsWithSemestarlValueCoded != null
              || lastDispensaSemestraWithStartOrContinueDrugsObs != null)
          && lastFilaWithReturnForDrugsObs == null) {
        found = true;
      }
      // case 7 if there is a fila filled with ficha filled with semestral concept filled on the
      // same date
      // we will end up picking the fila
      else if (lastFichaEncounter != null
          && lastFichaEncounter.getEncounterDatetime() != null
          && lastFilaWithReturnForDrugsObs != null
          && lastFilaWithReturnForDrugsObs.getEncounter() != null
          && lastFilaWithReturnForDrugsObs.getEncounter().getEncounterDatetime() != null
          && lastFilaWithReturnForDrugsObs.getValueDatetime() != null
          && lastFichaEncounter
              .getEncounterDatetime()
              .equals(lastFilaWithReturnForDrugsObs.getEncounter().getEncounterDatetime())
          && EptsCalculationUtils.daysSince(
                  lastFilaWithReturnForDrugsObs.getEncounter().getEncounterDatetime(),
                  lastFilaWithReturnForDrugsObs.getValueDatetime())
              > 173) {
        found = true;
      }

      // case 8:
      if (lastFichaEncounter != null
          && lastDispensaTrimestralWithCompltedObs != null
          && lastFichaEncounter.equals(lastDispensaTrimestralWithCompltedObs.getEncounter())) {
        found = false;
      }

      resultMap.put(pId, new BooleanResult(found, this));
    }

    return resultMap;
  }
}
