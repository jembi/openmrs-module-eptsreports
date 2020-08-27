package org.openmrs.module.eptsreports.reporting.calculation.txcurr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.ListResult;
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
    Concept quaterly = hivMetadata.getQuarterlyConcept();
    Concept dispensaSemestra = hivMetadata.getSemiannualDispensation();
    Concept startDrugs = hivMetadata.getStartDrugs();
    Concept continueRegimen = hivMetadata.getContinueRegimenConcept();
    Concept monthly = hivMetadata.getMonthlyConcept();

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
    // get all the next pick up date
    CalculationResultMap getAllFilaWithReturnVisitForDrugFilledMap =
        ePTSCalculationService.getObs(
            returnVisitDateForArvDrugs,
            Arrays.asList(fila),
            cohort,
            Arrays.asList(location),
            null,
            TimeQualifier.ANY,
            null,
            onOrBefore,
            context);
    CalculationResultMap getLastFichaWithSemestaral =
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
    // get all the list of typeOfDispensation
    CalculationResultMap getAllFichaWithSemestaral =
        ePTSCalculationService.getObs(
            typeOfDispensation,
            Arrays.asList(ficha),
            cohort,
            Arrays.asList(location),
            null,
            TimeQualifier.ANY,
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
            null,
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
    // all list of dispensaSemestra with start or continue regime
    CalculationResultMap allDispensaSemestraWithoutStartOrContinueDrugsMap =
        ePTSCalculationService.getObs(
            dispensaSemestra,
            Arrays.asList(ficha),
            cohort,
            Arrays.asList(location),
            null,
            TimeQualifier.ANY,
            null,
            onOrBefore,
            context);
    // find map that has monthly as an obs for type of desposition
    CalculationResultMap getLastFichaWithMonthlyObsMap =
        ePTSCalculationService.getObs(
            typeOfDispensation,
            Arrays.asList(ficha),
            cohort,
            Arrays.asList(location),
            Arrays.asList(monthly),
            TimeQualifier.LAST,
            null,
            onOrBefore,
            context);
    // find map that has quartely response
    CalculationResultMap getLastFichaWithQuartelyObsMap =
        ePTSCalculationService.getObs(
            typeOfDispensation,
            Arrays.asList(ficha),
            cohort,
            Arrays.asList(location),
            Arrays.asList(quaterly),
            TimeQualifier.LAST,
            null,
            onOrBefore,
            context);
    // find map that has semestarl as last obs
    CalculationResultMap getLastFichaWithSemestaralObsMap =
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
    CalculationResultMap lastFichaEncounterMap =
        ePTSCalculationService.getEncounter(
            Arrays.asList(ficha), TimeQualifier.LAST, cohort, location, onOrBefore, context);
    CalculationResultMap lastFilaEncounterMap =
        ePTSCalculationService.getEncounter(
            Arrays.asList(fila), TimeQualifier.LAST, cohort, location, onOrBefore, context);
    CalculationResultMap allFilaEncountersMap =
        ePTSCalculationService.getEncounter(
            Arrays.asList(fila), TimeQualifier.ANY, cohort, location, onOrBefore, context);
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
      // get latest ficha with monthly obs collected
      Obs getLastFichaWithMonthlyObs =
          EptsCalculationUtils.obsResultForPatient(getLastFichaWithMonthlyObsMap, pId);
      // get lates ficha with quatertly collected
      Obs getLastFichaWithQuartelyObs =
          EptsCalculationUtils.obsResultForPatient(getLastFichaWithQuartelyObsMap, pId);
      // get latest semestarl obs collected
      Obs getLastFichaWithSemestaralObs =
          EptsCalculationUtils.obsResultForPatient(getLastFichaWithSemestaralObsMap, pId);

      // get all fila list of date obs
      ListResult listResultAllNextDateOfAppointment =
          (ListResult) getAllFilaWithReturnVisitForDrugFilledMap.get(pId);
      List<Obs> allFilaObsList =
          EptsCalculationUtils.extractResultValues(listResultAllNextDateOfAppointment);
      // get all ficha with type of disposition
      ListResult listResultDisposition = (ListResult) getAllFichaWithSemestaral.get(pId);
      List<Obs> listResultDispositionObs =
          EptsCalculationUtils.extractResultValues(listResultDisposition);
      // get all ficha obs with allDispensaSemestraWithoutStartOrContinueDrugsMap
      ListResult allListResultDispensaSemestry =
          (ListResult) allDispensaSemestraWithoutStartOrContinueDrugsMap.get(pId);
      List<Obs> allListResultDispensaSemestryObs =
          EptsCalculationUtils.extractResultValues(allListResultDispensaSemestry);

      ListResult listResultAllFilaEncounters = (ListResult) allFilaEncountersMap.get(pId);
      List<Encounter> allFilaEncounters =
          EptsCalculationUtils.extractResultValues(listResultAllFilaEncounters);

      Encounter lastFilaPickedEncounter = null;
      Encounter secondLastEncounter = null;
      List<Obs> filaObsOnTheSameEncounterDate = new ArrayList<Obs>();
      if (allFilaEncounters.size() > 1) {
        EptsCalculationUtils.sortEncountersByEncounterId(allFilaEncounters);
        lastFilaPickedEncounter = allFilaEncounters.get(allFilaEncounters.size() - 1);
        secondLastEncounter = allFilaEncounters.get(allFilaEncounters.size() - 2);
      }
      // case 1 fila filled is after ficha filled with semestral concept id
      if (lastFilaEncounter != null
          && lastFichaEncounter != null
          && lastFilaWithReturnForDrugsObs != null
          && lastFilaWithReturnForDrugsObs.getValueDatetime() != null
          && lastFilaEncounter
                  .getEncounterDatetime()
                  .compareTo(lastFichaEncounter.getEncounterDatetime())
              > 0
          && EptsCalculationUtils.exactDaysSince(
                  lastFilaWithReturnForDrugsObs.getValueDatetime(),
                  lastFilaWithReturnForDrugsObs.getEncounter().getEncounterDatetime())
              > 173) {
        found = true;

      }
      // case 2 ficha filled is after fila filled with semestral concept id reverse of 1
      else if (lastFilaWithReturnForDrugsObs != null
          && lastFilaWithReturnForDrugsObs.getEncounter() != null
          && lastFichaObsWithSemestarlValueCoded != null
          && lastFichaObsWithSemestarlValueCoded.getEncounter() != null
          && lastFichaObsWithSemestarlValueCoded.getValueCoded().equals(dispensaSemestra)
          && lastFichaObsWithSemestarlValueCoded
                  .getEncounter()
                  .getEncounterDatetime()
                  .compareTo(lastFilaWithReturnForDrugsObs.getEncounter().getEncounterDatetime())
              > 0) {
        found = true;
      }
      // case 3 ficha filled is after fila filled with start or continue regimen concept id
      else if (lastFilaWithReturnForDrugsObs != null
          && lastFichaEncounter != null
          && lastFilaEncounter != null
          && lastFilaWithReturnForDrugsObs.getEncounter() != null
          && lastFilaEncounter.equals(lastFilaWithReturnForDrugsObs.getEncounter())
          && lastDispensaSemestraWithStartOrContinueDrugsObs != null
          && lastFichaEncounter.equals(
              lastDispensaSemestraWithStartOrContinueDrugsObs.getEncounter())
          && (lastDispensaSemestraWithStartOrContinueDrugsObs.getValueCoded().equals(startDrugs)
              || lastDispensaSemestraWithStartOrContinueDrugsObs
                  .getValueCoded()
                  .equals(continueRegimen))
          && lastFichaEncounter
              .getEncounterDatetime()
              .after(lastFilaEncounter.getEncounterDatetime())) {
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
          && EptsCalculationUtils.exactDaysSince(
                  lastFilaWithReturnForDrugsObs.getValueDatetime(),
                  lastFilaWithReturnForDrugsObs.getEncounter().getEncounterDatetime())
              > 173) {
        found = true;

      }
      // case 5 if ficha filled
      else if ((lastFichaObsWithSemestarlValueCoded != null
              && lastFichaEncounter != null
              && lastFichaObsWithSemestarlValueCoded.getEncounter() != null
              && lastFichaEncounter.equals(lastFichaObsWithSemestarlValueCoded.getEncounter())
              && lastFichaObsWithSemestarlValueCoded.getValueCoded().equals(dispensaSemestra)
              && lastFilaWithReturnForDrugsObs == null)
          || (lastDispensaSemestraWithStartOrContinueDrugsObs != null
                  && lastFichaEncounter != null
                  && lastDispensaSemestraWithStartOrContinueDrugsObs.getEncounter() != null
                  && lastFichaEncounter.equals(
                      lastDispensaSemestraWithStartOrContinueDrugsObs.getEncounter())
                  && (lastDispensaSemestraWithStartOrContinueDrugsObs
                          .getValueCoded()
                          .equals(startDrugs)
                      || lastDispensaSemestraWithStartOrContinueDrugsObs
                          .getValueCoded()
                          .equals(continueRegimen)))
              && lastFilaWithReturnForDrugsObs == null) {
        found = true;
      }
      // case 6 if there is a fila filled with ficha filled with semestral concept filled on the
      // same date
      // we will end up picking the fila
      else if (lastFichaEncounter != null
          && lastFilaWithReturnForDrugsObs != null
          && lastFilaWithReturnForDrugsObs.getEncounter() != null
          && lastFilaWithReturnForDrugsObs.getEncounter().getEncounterDatetime() != null
          && lastFilaWithReturnForDrugsObs.getValueDatetime() != null
          && lastFichaEncounter
                  .getEncounterDatetime()
                  .compareTo(lastFilaWithReturnForDrugsObs.getEncounter().getEncounterDatetime())
              == 0
          && EptsCalculationUtils.exactDaysSince(
                  lastFilaWithReturnForDrugsObs.getValueDatetime(),
                  lastFilaWithReturnForDrugsObs.getEncounter().getEncounterDatetime())
              > 173) {
        found = true;
      }
      // find all the fila, compare with the last encounter, if it has >173 days, pick it here
      else if (lastFilaWithReturnForDrugsObs != null
          && lastFilaWithReturnForDrugsObs.getEncounter() != null
          && lastFichaEncounter == null
          && lastFilaWithReturnForDrugsObs.getEncounter().getEncounterDatetime() != null
          && lastFilaWithReturnForDrugsObs.getValueDatetime() != null
          && EptsCalculationUtils.exactDaysSince(
                  lastFilaWithReturnForDrugsObs.getValueDatetime(),
                  lastFilaWithReturnForDrugsObs.getEncounter().getEncounterDatetime())
              > 173) {
        found = true;
      }
      // find all obs compared per the encounter based on
      else if (lastFilaEncounter == null
          && lastFichaObsWithSemestarlValueCoded != null
          && lastFichaObsWithSemestarlValueCoded.getValueCoded().equals(dispensaSemestra)) {
        found = true;
      }
      // find all obs compared with last encounter with start and continue regimen
      else if (lastFilaEncounter == null
          && lastDispensaSemestraWithStartOrContinueDrugsObs != null
          && lastDispensaSemestraWithStartOrContinueDrugsObs.getValueCoded() != null
          && (lastDispensaSemestraWithStartOrContinueDrugsObs.getValueCoded().equals(startDrugs)
              || lastDispensaSemestraWithStartOrContinueDrugsObs
                  .getValueCoded()
                  .equals(continueRegimen))) {
        found = true;
      }
      // what if there is 3 fichas on the same date that has a criteria for <3 months, 3-5 months
      // and > 6 months
      // we will have to pick that criteria here as well
      else if (getLastFichaWithMonthlyObs != null
          && getLastFichaWithSemestaralObs != null
          && lastFilaEncounter == null
          && getLastFichaWithMonthlyObs.getObsDatetime() != null
          && getLastFichaWithSemestaralObs.getObsDatetime() != null
          && getLastFichaWithSemestaralObs
                  .getObsDatetime()
                  .compareTo(getLastFichaWithMonthlyObs.getObsDatetime())
              >= 0) {
        found = true;
      } else if (getLastFichaWithQuartelyObs != null
          && lastFilaEncounter == null
          && getLastFichaWithSemestaralObs != null
          && getLastFichaWithQuartelyObs.getObsDatetime() != null
          && getLastFichaWithSemestaralObs.getObsDatetime() != null
          && getLastFichaWithSemestaralObs
                  .getObsDatetime()
                  .compareTo(getLastFichaWithQuartelyObs.getObsDatetime())
              >= 0) {
        found = true;
      }
      // check if there is multiple filas taken on the same date, pick the one with the highest
      // value datetime and >= last ficha
      else if (lastFilaPickedEncounter != null
          && lastFichaEncounter != null
          && secondLastEncounter != null
          && allFilaObsList.size() > 0) {
        if (lastFilaPickedEncounter
            .getEncounterDatetime()
            .equals(secondLastEncounter.getEncounterDatetime())) {
          // loop through the obs and pick those that match those 2 encounter
          for (Obs obs : allFilaObsList) {
            if (obs.getValueDatetime() != null
                && (lastFilaPickedEncounter.equals(obs.getEncounter())
                    || secondLastEncounter.equals(obs.getEncounter()))) {
              filaObsOnTheSameEncounterDate.add(obs);
            }
          }
          Date requiredDate = null;
          if (filaObsOnTheSameEncounterDate.size() == 2) {
            requiredDate = filaObsOnTheSameEncounterDate.get(0).getValueDatetime();
            if (filaObsOnTheSameEncounterDate.get(1).getValueDatetime().compareTo(requiredDate)
                > 0) {
              requiredDate = filaObsOnTheSameEncounterDate.get(1).getValueDatetime();
            }
          }
          // no that you have the right value datetime and the encounter date, do the logic for
          // days and <=173 days
          if (requiredDate != null
              && EptsCalculationUtils.exactDaysSince(
                      requiredDate, lastFilaPickedEncounter.getEncounterDatetime())
                  > 173) {
            found = true;
          }
        }
      }

      // check if there is multiple filas taken on the same date, pick the one with the highest
      // value datetime and ficha is null, just to check agianst fila only
      else if (lastFilaPickedEncounter != null
          && lastFichaEncounter == null
          && secondLastEncounter != null
          && allFilaObsList.size() > 0) {
        if (lastFilaPickedEncounter
            .getEncounterDatetime()
            .equals(secondLastEncounter.getEncounterDatetime())) {
          // loop through the obs and pick those that match those 2 encounter
          for (Obs obs : allFilaObsList) {
            if (obs.getValueDatetime() != null
                && (lastFilaPickedEncounter.equals(obs.getEncounter())
                    || secondLastEncounter.equals(obs.getEncounter()))) {
              filaObsOnTheSameEncounterDate.add(obs);
            }
          }
          Date requiredDate = null;
          if (filaObsOnTheSameEncounterDate.size() == 2) {
            requiredDate = filaObsOnTheSameEncounterDate.get(0).getValueDatetime();
            if (filaObsOnTheSameEncounterDate.get(1).getValueDatetime().compareTo(requiredDate)
                > 0) {
              requiredDate = filaObsOnTheSameEncounterDate.get(1).getValueDatetime();
            }
          }
          // no that you have the right value datetime and the encounter date, do the logic
          // days and <=173 days
          if (requiredDate != null
              && EptsCalculationUtils.exactDaysSince(
                      requiredDate, lastFilaPickedEncounter.getEncounterDatetime())
                  > 173) {
            found = true;
          }
        }
      }

      // case 8:
      if (lastDispensaTrimestralWithCompltedObs != null
          && lastFichaEncounter != null
          && lastFilaEncounter != null
          && lastFichaEncounter
                  .getEncounterDatetime()
                  .compareTo(lastFilaEncounter.getEncounterDatetime())
              > 0) {
        found = false;
      }
      resultMap.put(pId, new BooleanResult(found, this));
    }

    return resultMap;
  }
}
