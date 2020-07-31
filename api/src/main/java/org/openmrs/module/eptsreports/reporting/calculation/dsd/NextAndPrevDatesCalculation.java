package org.openmrs.module.eptsreports.reporting.calculation.dsd;

import java.util.*;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.ListResult;
import org.openmrs.module.eptsreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.eptsreports.reporting.calculation.BooleanResult;
import org.openmrs.module.eptsreports.reporting.calculation.common.EPTSCalculationService;
import org.openmrs.module.eptsreports.reporting.utils.EptsCalculationUtils;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.patient.definition.EncountersForPatientDataDefinition;
import org.springframework.stereotype.Component;

@Component
public class NextAndPrevDatesCalculation extends AbstractPatientCalculation {
  @Override
  public CalculationResultMap evaluate(
      Collection<Integer> cohort,
      Map<String, Object> parameterValues,
      PatientCalculationContext context) {

    EPTSCalculationService ePTSCalculationService =
        Context.getRegisteredComponents(EPTSCalculationService.class).get(0);

    Date onOrBefore = (Date) context.getFromCache("onOrBefore");
    Location location = (Location) context.getFromCache("location");
    CalculationResultMap map = new CalculationResultMap();
    Concept concept = (Concept) parameterValues.get("conceptId");
    Integer lowerBound = (Integer) parameterValues.get("lowerBound");
    Integer upperBound = (Integer) parameterValues.get("upperBound");
    List<EncounterType> encounterTypes =
        (List<EncounterType>) parameterValues.get("encounterTypes");

    // Step 1: Search for last encounter visit for this patient
    CalculationResultMap lastEncounterMap =
        ePTSCalculationService.getEncounter(
            encounterTypes, TimeQualifier.LAST, cohort, location, onOrBefore, context);

    // Step 2.1: Search for next return drug pick/appointment for patient
    CalculationResultMap lastReturnVisitObsMap =
        ePTSCalculationService.getObs(
            concept,
            encounterTypes,
            cohort,
            Arrays.asList(location),
            null,
            TimeQualifier.ANY,
            null,
            context);

    for (Integer pId : cohort) {
      boolean scheduled = false;
      ListResult returnVisitObsResult = (ListResult) lastReturnVisitObsMap.get(pId);
      List<Obs> returnVisitList = EptsCalculationUtils.extractResultValues(returnVisitObsResult);
      Encounter lastEncounter = EptsCalculationUtils.resultForPatient(lastEncounterMap, pId);
      Obs lastReturnVisitObs = null;

      /*for another last encounter that may occur in the same date*/
      if (lastEncounter != null) {

        Encounter anotherLastEncounter =
            getAnotherLastEncounter(
                lastEncounter.getEncounterDatetime(),
                lastEncounter.getEncounterDatetime(),
                lastEncounter.getLocation(),
                lastEncounter,
                pId,
                context);
        if (anotherLastEncounter != null) {
          List<Obs> copyOfReturnVisitList = new ArrayList<>(returnVisitList);
          Obs obs = getLastObs(copyOfReturnVisitList, anotherLastEncounter);
          if (obs != null) {
            scheduled = compareAgainstBoundaries(anotherLastEncounter, obs, lowerBound, upperBound);
          }
        }
      }

      // Step 2.2: identify last return visit obs record
      lastReturnVisitObs = getLastObs(returnVisitList, lastEncounter);

      // Step 3: compare against boundaries
      scheduled =
          compareAgainstBoundaries(lastEncounter, lastReturnVisitObs, lowerBound, upperBound);

      map.put(pId, new BooleanResult(scheduled, this));
    }
    return map;
  }

  private Encounter getAnotherLastEncounter(
      Date onOrAfter,
      Date onOrBefore,
      Location location,
      Encounter encounter,
      Integer patientId,
      PatientCalculationContext context) {
    EncountersForPatientDataDefinition def = new EncountersForPatientDataDefinition();
    def.setWhich(TimeQualifier.ANY);
    def.setOnOrAfter(onOrAfter);
    def.setOnOrBefore(onOrBefore);
    def.setLocationList(Arrays.asList(location));
    def.setTypes(Arrays.asList(encounter.getEncounterType()));
    def.setName("another encounter of type=" + encounter.getEncounterType());

    CalculationResultMap encounterMap =
        EptsCalculationUtils.evaluateWithReporting(
            def, Arrays.asList(patientId), null, null, context);

    List<Encounter> lastEncounters = EptsCalculationUtils.resultForPatient(encounterMap, patientId);
    if (lastEncounters.size() > 0) {
      Encounter otherLastEncounter = null;
      try {
        otherLastEncounter = (Encounter) lastEncounters.get(lastEncounters.size() - 1);
      } catch (ClassCastException e) {
        // otherLastEncounter remains NULL
      }
      if (otherLastEncounter != null
          && otherLastEncounter.getEncounterId() != encounter.getEncounterId()) {
        return otherLastEncounter;
      }
    }
    return null;
  }

  private Obs getLastObs(List<Obs> returnVisitList, Encounter lastEncounter) {
    Obs lastReturnVisitObs = null;

    Iterator<Obs> iterator = returnVisitList.iterator();
    while (iterator.hasNext()) {
      Obs obs = iterator.next();
      if (obs.getVoided()
          || obs.getValueDate() == null
          || obs.getEncounter() == null
          || (obs.getEncounter() != null
              && (obs.getEncounter().getEncounterId() != lastEncounter.getEncounterId()))) {
        iterator.remove();
      }
    }

    Collections.sort(
        returnVisitList,
        new Comparator<Obs>() {
          @Override
          public int compare(Obs obs, Obs t1) {
            return t1.getValueDate().compareTo(obs.getValueDatetime());
          }
        });
    if (returnVisitList.size() > 0) {
      lastReturnVisitObs = returnVisitList.get(returnVisitList.size() - 1);
    }
    return lastReturnVisitObs;
  }

  private boolean compareAgainstBoundaries(
      Encounter lastEncounter, Obs lastReturnVisitObs, Integer lowerBound, Integer upperBound) {

    if (lastEncounter != null
        && lastReturnVisitObs != null
        && lastReturnVisitObs.getValueDate() != null) {

      Date lowerBoundary =
          EptsCalculationUtils.addDays(lastEncounter.getEncounterDatetime(), lowerBound);
      Date upperBoundary =
          EptsCalculationUtils.addDays(lastEncounter.getEncounterDatetime(), upperBound);

      if (lastReturnVisitObs.getValueDate().compareTo(lowerBoundary) >= 0
          && lastReturnVisitObs.getValueDate().compareTo(upperBoundary) <= 0) {
        return true;
      }
    }
    return false;
  }
}
