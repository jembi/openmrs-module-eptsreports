package org.openmrs.module.eptsreports.reporting.calculation.txcurr;

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
public class ThreeToFiveMonthsOnArtDispensationCalculation extends AbstractPatientCalculation {

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
    Concept quaterlyDispensation = hivMetadata.getQuarterlyConcept();
    Concept quaterlyDispensationDT = hivMetadata.getQuarterlyDispensation();
    Concept startDrugs = hivMetadata.getStartDrugs();
    Concept continueRegimen = hivMetadata.getContinueRegimenConcept();
    Concept monthly = hivMetadata.getMonthlyConcept();
    // get the last fila with next drug pick up date captured, only the last one
    CalculationResultMap getLastFila =
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
    // get all the fila obs having next pickup concept collected
    CalculationResultMap getAllFila =
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
    // get exactly last typeOfDispensation
    CalculationResultMap getLastTypeOfDispensationWithoutQuartelyAsValueCoded =
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
    // get all obs with typeOfDispensation and and quartely value coded
    CalculationResultMap getAllLastTypeOfDispensationWithQuartelyAsValueCoded =
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
    // get last DT
    CalculationResultMap getLastQuartelyDispensationWithStartOrContinueRegimen =
        ePTSCalculationService.getObs(
            quaterlyDispensationDT,
            Arrays.asList(ficha),
            cohort,
            Arrays.asList(location),
            null,
            TimeQualifier.LAST,
            null,
            onOrBefore,
            context);
    // get DT with ANY list
    CalculationResultMap getAllDtQuartelyDispensationWithStartOrContinueRegimen =
        ePTSCalculationService.getObs(
            quaterlyDispensationDT,
            Arrays.asList(ficha),
            cohort,
            Arrays.asList(location),
            null,
            TimeQualifier.ANY,
            null,
            onOrBefore,
            context);
    CalculationResultMap lastFichaEncounterMap =
        ePTSCalculationService.getEncounter(
            Arrays.asList(ficha), TimeQualifier.LAST, cohort, location, onOrBefore, context);
    CalculationResultMap lastFilaEncounterMap =
        ePTSCalculationService.getEncounter(
            Arrays.asList(fila), TimeQualifier.LAST, cohort, location, onOrBefore, context);
    CalculationResultMap quartelyMap =
        ePTSCalculationService.getObs(
            quaterlyDispensation,
            Arrays.asList(ficha),
            cohort,
            Arrays.asList(location),
            null,
            TimeQualifier.LAST,
            null,
            onOrBefore,
            context);
    CalculationResultMap getLastEncounterWithDepositionAndMonthlyAsCodedValueMap =
        ePTSCalculationService.getObs(
            typeOfDispensation,
            Arrays.asList(ficha),
            cohort,
            Arrays.asList(location),
            null,
            TimeQualifier.LAST,
            null,
            context);
    for (Integer pId : cohort) {
      boolean found = false;

      Obs lastFilaObs = EptsCalculationUtils.obsResultForPatient(getLastFila, pId);
      Obs getLastTypeOfDispensationObsWithoutQuartelyValueCoded =
          EptsCalculationUtils.obsResultForPatient(
              getLastTypeOfDispensationWithoutQuartelyAsValueCoded, pId);
      Obs getLastQuartelyDispensationObsWithStartOrContinueRegimenObs =
          EptsCalculationUtils.obsResultForPatient(
              getLastQuartelyDispensationWithStartOrContinueRegimen, pId);
      Encounter lastFichaEncounter =
          EptsCalculationUtils.resultForPatient(lastFichaEncounterMap, pId);
      Obs lastQuartelyObsWithCompleted = EptsCalculationUtils.obsResultForPatient(quartelyMap, pId);

      Encounter lastFilaEncounter =
          EptsCalculationUtils.resultForPatient(lastFilaEncounterMap, pId);

      Obs getObsWithDepositionAndMonthlyAsCodedValue =
          EptsCalculationUtils.obsResultForPatient(
              getLastEncounterWithDepositionAndMonthlyAsCodedValueMap, pId);

      // get all the list result for the fila
      ListResult listResultForAllFila = (ListResult) getAllFila.get(pId);
      List<Obs> obsListForAllFila = EptsCalculationUtils.extractResultValues(listResultForAllFila);
      // get all the list results for ficha with quaterlyDispensation
      ListResult listResultAddQuartelyDispensation =
          (ListResult) getAllLastTypeOfDispensationWithQuartelyAsValueCoded.get(pId);
      List<Obs> listObsForQuartely =
          EptsCalculationUtils.extractResultValues(listResultAddQuartelyDispensation);
      // get all the list with the patients DT and
      ListResult listResultDtAll =
          (ListResult) getAllDtQuartelyDispensationWithStartOrContinueRegimen.get(pId);
      List<Obs> listresultsDtAll = EptsCalculationUtils.extractResultValues(listResultDtAll);

      // case 1: fila as last encounter and has return visit date for drugs filled
      // this is compared to ficha, if fila > ficha and the ficha filled should be the one with
      // typeOfDispensation(23739)
      if (lastFilaObs != null
          && lastFilaEncounter != null
          && lastFilaObs.getEncounter() != null
          && lastFilaEncounter.equals(lastFilaObs.getEncounter())
          && lastFilaObs.getEncounter().getEncounterDatetime() != null
          && getLastTypeOfDispensationObsWithoutQuartelyValueCoded != null
          && getLastTypeOfDispensationObsWithoutQuartelyValueCoded.getEncounter() != null
          && getLastTypeOfDispensationObsWithoutQuartelyValueCoded
                  .getEncounter()
                  .getEncounterDatetime()
              != null
          && lastFilaObs.getValueDatetime() != null
          && lastFilaObs
              .getEncounter()
              .getEncounterDatetime()
              .after(
                  getLastTypeOfDispensationObsWithoutQuartelyValueCoded
                      .getEncounter()
                      .getEncounterDatetime())
          && EptsCalculationUtils.daysSince(
                  lastFilaObs.getEncounter().getEncounterDatetime(), lastFilaObs.getValueDatetime())
              >= 83
          && EptsCalculationUtils.daysSince(
                  lastFilaObs.getEncounter().getEncounterDatetime(), lastFilaObs.getValueDatetime())
              <= 173) {
        found = true;
      }
      // case 2: fila as last encounter and has return visit date for drugs filled
      // this is compared to ficha, if fila > ficha and the ficha filled should be the one with
      // QUARTERLY DISPENSATION (DT) - 23730
      else if (lastFilaObs != null
          && lastFilaEncounter != null
          && lastFilaObs.getEncounter() != null
          && lastFilaEncounter.equals(lastFilaObs.getEncounter())
          && lastFilaObs.getEncounter().getEncounterDatetime() != null
          && getLastQuartelyDispensationObsWithStartOrContinueRegimenObs != null
          && getLastQuartelyDispensationObsWithStartOrContinueRegimenObs.getEncounter() != null
          && getLastQuartelyDispensationObsWithStartOrContinueRegimenObs
                  .getEncounter()
                  .getEncounterDatetime()
              != null
          && lastFilaObs.getValueDatetime() != null
          && lastFilaObs
              .getEncounter()
              .getEncounterDatetime()
              .after(
                  getLastQuartelyDispensationObsWithStartOrContinueRegimenObs
                      .getEncounter()
                      .getEncounterDatetime())
          && EptsCalculationUtils.daysSince(
                  lastFilaObs.getEncounter().getEncounterDatetime(), lastFilaObs.getValueDatetime())
              >= 83
          && EptsCalculationUtils.daysSince(
                  lastFilaObs.getEncounter().getEncounterDatetime(), lastFilaObs.getValueDatetime())
              <= 173) {
        found = true;
      }

      // case 3: fila as last encounter and has return visit date for drugs filled
      // this is compared to the date of Encounter Type Id = 6 Last QUARTERLY DISPENSATION (DT)
      // (id=23730)Value.coded= START DRUGS (id=1256) OR Value.coded= (CONTINUE REGIMEN id=1257)
      else if (lastFilaObs != null
          && lastFilaEncounter != null
          && lastFilaObs.getEncounter() != null
          && lastFilaEncounter.equals(lastFilaObs.getEncounter())
          && lastFilaObs.getEncounter().getEncounterDatetime() != null
          && getLastQuartelyDispensationObsWithStartOrContinueRegimenObs != null
          && getLastQuartelyDispensationObsWithStartOrContinueRegimenObs.getEncounter() != null
          && getLastQuartelyDispensationObsWithStartOrContinueRegimenObs
                  .getEncounter()
                  .getEncounterDatetime()
              != null
          && lastFilaObs.getValueDatetime() != null
          && (getLastQuartelyDispensationObsWithStartOrContinueRegimenObs
                  .getValueCoded()
                  .equals(startDrugs)
              || getLastQuartelyDispensationObsWithStartOrContinueRegimenObs
                  .getValueCoded()
                  .equals(continueRegimen))
          && lastFilaObs
              .getEncounter()
              .getEncounterDatetime()
              .after(
                  getLastQuartelyDispensationObsWithStartOrContinueRegimenObs
                      .getEncounter()
                      .getEncounterDatetime())
          && EptsCalculationUtils.daysSince(
                  lastFilaObs.getEncounter().getEncounterDatetime(), lastFilaObs.getValueDatetime())
              >= 83
          && EptsCalculationUtils.daysSince(
                  lastFilaObs.getEncounter().getEncounterDatetime(), lastFilaObs.getValueDatetime())
              <= 173) {
        found = true;
      }

      // case 4: fila as the last encounter and has return visit date for drugs filled
      // This is comapred to the 6 with 23739 concept collected and value coded of 23720

      else if (lastFilaObs != null
          && lastFilaEncounter != null
          && lastFilaObs.getEncounter() != null
          && lastFilaEncounter.equals(lastFilaObs.getEncounter())
          && lastFilaObs.getEncounter().getEncounterDatetime() != null
          && getLastTypeOfDispensationObsWithoutQuartelyValueCoded != null
          && getLastTypeOfDispensationObsWithoutQuartelyValueCoded.getEncounter() != null
          && getLastTypeOfDispensationObsWithoutQuartelyValueCoded
                  .getEncounter()
                  .getEncounterDatetime()
              != null
          && lastFilaObs.getValueDatetime() != null
          && getLastTypeOfDispensationObsWithoutQuartelyValueCoded
              .getValueCoded()
              .equals(quaterlyDispensation)
          && lastFilaObs
              .getEncounter()
              .getEncounterDatetime()
              .after(
                  getLastTypeOfDispensationObsWithoutQuartelyValueCoded
                      .getEncounter()
                      .getEncounterDatetime())
          && EptsCalculationUtils.daysSince(
                  lastFilaObs.getEncounter().getEncounterDatetime(), lastFilaObs.getValueDatetime())
              >= 83
          && EptsCalculationUtils.daysSince(
                  lastFilaObs.getEncounter().getEncounterDatetime(), lastFilaObs.getValueDatetime())
              <= 173) {
        found = true;
      }

      // case 5: ficha  as last encounter(ficha > fila) reverse of case1
      // this is compared to the date of Encounter Type Id = 6 Last TYPE OF DISPENSATION
      // (id=23739)Value.code = QUARTERLY (id=23720)
      else if (lastFilaObs != null
          && lastFichaEncounter != null
          && lastFilaObs.getEncounter() != null
          && getLastTypeOfDispensationObsWithoutQuartelyValueCoded != null
          && getLastTypeOfDispensationObsWithoutQuartelyValueCoded.getEncounter() != null
          && lastFichaEncounter.equals(
              getLastTypeOfDispensationObsWithoutQuartelyValueCoded.getEncounter())
          && getLastTypeOfDispensationObsWithoutQuartelyValueCoded
              .getValueCoded()
              .equals(quaterlyDispensation)
          && getLastTypeOfDispensationObsWithoutQuartelyValueCoded
              .getEncounter()
              .getEncounterDatetime()
              .after(lastFilaObs.getEncounter().getEncounterDatetime())) {
        found = true;
      }

      // case 6: ficha as last encounter, ficha >fila opposite of 2
      // this is compared to the date of Encounter Type Id = 6 Last QUARTERLY DISPENSATION (DT)
      // (id=23730)Value.coded= START DRUGS (id=1256) OR Value.coded= (CONTINUE REGIMEN id=1257)
      else if (lastFilaObs != null
          && lastFichaEncounter != null
          && lastFilaObs.getEncounter() != null
          && lastFilaObs.getEncounter().getEncounterDatetime() != null
          && getLastQuartelyDispensationObsWithStartOrContinueRegimenObs != null
          && getLastQuartelyDispensationObsWithStartOrContinueRegimenObs.getEncounter() != null
          && lastFichaEncounter.equals(
              getLastQuartelyDispensationObsWithStartOrContinueRegimenObs.getEncounter())
          && (getLastQuartelyDispensationObsWithStartOrContinueRegimenObs
                  .getValueCoded()
                  .equals(startDrugs)
              || getLastQuartelyDispensationObsWithStartOrContinueRegimenObs
                  .getValueCoded()
                  .equals(continueRegimen))
          && getLastQuartelyDispensationObsWithStartOrContinueRegimenObs
              .getEncounter()
              .getEncounterDatetime()
              .after(lastFilaObs.getEncounter().getEncounterDatetime())) {
        found = true;
      }
      // case 7: If the most recent have more than one source FILA and FICHA registered on the same
      // most recent date, then consider the information from FILA
      else if (lastFichaEncounter != null
          && lastFilaEncounter != null
          && lastFilaObs != null
          && lastFilaObs.getEncounter() != null
          && lastFilaEncounter.equals(lastFilaObs.getEncounter())
          && lastFilaObs.getValueDatetime() != null
          && lastFilaObs
              .getEncounter()
              .getEncounterDatetime()
              .equals(lastFichaEncounter.getEncounterDatetime())
          && EptsCalculationUtils.daysSince(
                  lastFilaObs.getEncounter().getEncounterDatetime(), lastFilaObs.getValueDatetime())
              >= 83
          && EptsCalculationUtils.daysSince(
                  lastFilaObs.getEncounter().getEncounterDatetime(), lastFilaObs.getValueDatetime())
              <= 173) {
        found = true;

      }
      // case 8: here fila is the latest/only  encounter with the values for the observations when
      // DT is null
      // collected getLastTypeOfDispensationObsWithoutQuartelyValueCoded
      else if (lastFilaObs != null
          && lastFilaEncounter != null
          && lastFilaObs.getEncounter() != null
          && lastFilaEncounter.equals(lastFilaObs.getEncounter())
          && lastFilaObs.getEncounter().getEncounterDatetime() != null
          && lastFilaObs.getValueDatetime() != null
          && getLastTypeOfDispensationObsWithoutQuartelyValueCoded == null
          && EptsCalculationUtils.daysSince(
                  lastFilaObs.getEncounter().getEncounterDatetime(), lastFilaObs.getValueDatetime())
              >= 83
          && EptsCalculationUtils.daysSince(
                  lastFilaObs.getEncounter().getEncounterDatetime(), lastFilaObs.getValueDatetime())
              <= 173) {
        found = true;
      }

      // case 9: here fila is the latest/only  encounter with the values for the observations when
      // DT is null
      // collected getLastQuartelyDispensationObsWithStartOrContinueRegimenObs
      else if (lastFilaObs != null
          && lastFilaEncounter != null
          && lastFilaObs.getEncounter() != null
          && lastFilaEncounter.equals(lastFilaObs.getEncounter())
          && lastFilaObs.getEncounter().getEncounterDatetime() != null
          && lastFilaObs.getValueDatetime() != null
          && getLastQuartelyDispensationObsWithStartOrContinueRegimenObs == null
          && EptsCalculationUtils.daysSince(
                  lastFilaObs.getEncounter().getEncounterDatetime(), lastFilaObs.getValueDatetime())
              >= 83
          && EptsCalculationUtils.daysSince(
                  lastFilaObs.getEncounter().getEncounterDatetime(), lastFilaObs.getValueDatetime())
              <= 173) {
        found = true;
      }

      // case 10: ficha has last/only encounter  Last TYPE OF DISPENSATION (id=23739) Value.code =
      // QUARTERLY (id=23720)
      // included also is the start and continue regimen
      else if (getLastTypeOfDispensationObsWithoutQuartelyValueCoded != null
          && lastFichaEncounter != null
          && lastFilaObs == null
          && getLastTypeOfDispensationObsWithoutQuartelyValueCoded.getEncounter() != null
          && lastFichaEncounter.equals(
              getLastTypeOfDispensationObsWithoutQuartelyValueCoded.getEncounter())
          && getLastTypeOfDispensationObsWithoutQuartelyValueCoded.getValueCoded() != null
          && getLastTypeOfDispensationObsWithoutQuartelyValueCoded
              .getValueCoded()
              .equals(quaterlyDispensation)) {
        found = true;
      }

      // case 11: ficha has last/only encounter  Last TYPE OF DISPENSATION (id=23739) Value.code =
      // QUARTERLY (id=23720)
      // included also is the start and continue regimen
      else if (getLastQuartelyDispensationObsWithStartOrContinueRegimenObs != null
          && lastFichaEncounter != null
          && lastFilaObs == null
          && getLastQuartelyDispensationObsWithStartOrContinueRegimenObs.getEncounter() != null
          && lastFichaEncounter.equals(
              getLastQuartelyDispensationObsWithStartOrContinueRegimenObs.getEncounter())
          && getLastQuartelyDispensationObsWithStartOrContinueRegimenObs.getValueCoded() != null
          && (getLastQuartelyDispensationObsWithStartOrContinueRegimenObs
                  .getValueCoded()
                  .equals(startDrugs)
              || getLastQuartelyDispensationObsWithStartOrContinueRegimenObs
                  .getValueCoded()
                  .equals(continueRegimen))) {
        found = true;
      }
      // case 12, if a fila has value and a ficha is filled regardless of the obs collected
      else if (lastFilaEncounter != null && obsListForAllFila.size() > 0) {
        for (Obs obs : obsListForAllFila) {
          if (lastFilaEncounter.equals(obs.getEncounter())
              && obs.getValueDatetime() != null
              && EptsCalculationUtils.daysSince(
                      obs.getEncounter().getEncounterDatetime(), obs.getValueDatetime())
                  >= 83
              && EptsCalculationUtils.daysSince(
                      obs.getEncounter().getEncounterDatetime(), obs.getValueDatetime())
                  <= 173) {
            found = true;
            break;
          }
        }
      }
      // case 13, if a ficha has value and a fila is filled regardless of the obs collected with
      // quartely ass answer
      else if (lastFichaEncounter != null && listObsForQuartely.size() > 0) {
        for (Obs obs : listObsForQuartely) {
          if (lastFichaEncounter.equals(obs.getEncounter())
              && obs.getValueCoded().equals(quaterlyDispensation)) {
            found = true;
            break;
          }
        }
      }

      // case 14, if a ficha has value and a fila is filled regardless of the obs collected with
      // start and continue
      else if (lastFichaEncounter != null && listresultsDtAll.size() > 0) {
        for (Obs obs : listresultsDtAll) {
          if (lastFichaEncounter.equals(obs.getEncounter())
              && (obs.getValueCoded().equals(startDrugs)
                  || obs.getValueCoded().equals(continueRegimen))) {
            found = true;
            break;
          }
        }
      }

      // case 15
      // exclude   patients   who   have   the   last   SEMESTRAL   QUARTERLY (concept   id=23730
      // with value_coded as value_coded=1267)
      if (lastQuartelyObsWithCompleted != null
          && lastQuartelyObsWithCompleted.getValueCoded() != null
          && lastQuartelyObsWithCompleted.getValueCoded().equals(completedConcept)) {
        found = false;
      }

      // case 16: ficha as the last encounter and has Last TYPE OF DISPENSATION and value coded as
      // monthly, make sure the last encounter has required obs collected on them
      // this section exclude patients already in <3 months on ARV dispensation
      else if (lastFichaEncounter != null
          && getObsWithDepositionAndMonthlyAsCodedValue != null
          && getObsWithDepositionAndMonthlyAsCodedValue.getEncounter() != null
          && lastFichaEncounter.equals(getObsWithDepositionAndMonthlyAsCodedValue.getEncounter())
          && getObsWithDepositionAndMonthlyAsCodedValue.getValueCoded() != null
          && getObsWithDepositionAndMonthlyAsCodedValue.getValueCoded().equals(monthly)) {
        found = false;
      }
      resultMap.put(pId, new BooleanResult(found, this));
    }
    return resultMap;
  }
}
