package org.openmrs.module.eptsreports.reporting.calculation.cxcascrn;

import java.util.*;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
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
import org.openmrs.module.eptsreports.reporting.library.cohorts.CXCASCRNCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.queries.CXCASCRNQueries;
import org.openmrs.module.eptsreports.reporting.utils.EPTSMetadataDatetimeQualifier;
import org.openmrs.module.eptsreports.reporting.utils.EptsCalculationUtils;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.stereotype.Component;

@Component
public class CXCASCRNBBCalculation extends AbstractPatientCalculation {

  private final String ON_OR_AFTER = "onOrAfter";
  private final String ON_OR_BEFORE = "onOrBefore";
  private final String LOCATION = "location";

  @Override
  public CalculationResultMap evaluate(
      Collection<Integer> cohort,
      Map<String, Object> parameterValues,
      PatientCalculationContext context) {
    CalculationResultMap map = new CalculationResultMap();

    HivMetadata hivMetadata = Context.getRegisteredComponents(HivMetadata.class).get(0);

    Date startDate = (Date) context.getFromCache(ON_OR_AFTER);
    Date endDate = (Date) context.getFromCache(ON_OR_BEFORE);
    Location location = (Location) context.getFromCache(LOCATION);

    EPTSCalculationService eptsCalculationService =
        Context.getRegisteredComponents(EPTSCalculationService.class).get(0);

    EncounterType rastreioDoCancroDoColoUterinoEncounterType =
        hivMetadata.getRastreioDoCancroDoColoUterinoEncounterType();
    Concept cryotherapyPerformedOnTheSameDayASViaConcept =
        hivMetadata.getCryotherapyPerformedOnTheSameDayASViaConcept();
    Concept yesConcept = hivMetadata.getPatientFoundYesConcept();
    Concept cryotherapyDateConcept = hivMetadata.getCryotherapyDateConcept();
    Concept viaResultOnTheReferenceConcept = hivMetadata.getViaResultOnTheReferenceConcept();
    Concept cryotherapyConcept = hivMetadata.getCryotherapyConcept();
    Concept thermocoagulationConcept = hivMetadata.getThermocoagulationConcept();
    Concept leepConcept = hivMetadata.getLeepConcept();
    Concept conizationConcept = hivMetadata.getconizationConcept();

    Map<String, Object> stringObjectMap = new HashMap<>();
    stringObjectMap.put("result", CXCASCRNCohortQueries.CXCASCRNResult.ALL);

    CalculationResultMap aaResultMap =
        calculate(
            Context.getRegisteredComponents(CXCASCRNAACalculation.class).get(0),
            cohort,
            stringObjectMap,
            context);

    CalculationResultMap aa4ResultMap = getAA4Map(hivMetadata, cohort, context);

    CalculationResultMap criotherapyResulMap =
        eptsCalculationService.getObs(
            cryotherapyPerformedOnTheSameDayASViaConcept,
            rastreioDoCancroDoColoUterinoEncounterType,
            cohort,
            location,
            Arrays.asList(yesConcept),
            TimeQualifier.ANY,
            null,
            endDate,
            EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
            context);

    CalculationResultMap criotherapyDateResulMap =
        eptsCalculationService.getObs(
            cryotherapyDateConcept,
            rastreioDoCancroDoColoUterinoEncounterType,
            cohort,
            location,
            null,
            TimeQualifier.ANY,
            null,
            endDate,
            EPTSMetadataDatetimeQualifier.VALUE_DATETIME,
            context);

    CalculationResultMap viaResultRefenceResulMap =
        eptsCalculationService.getObs(
            viaResultOnTheReferenceConcept,
            rastreioDoCancroDoColoUterinoEncounterType,
            cohort,
            location,
            Arrays.asList(
                cryotherapyConcept, thermocoagulationConcept, leepConcept, conizationConcept),
            TimeQualifier.ANY,
            null,
            endDate,
            EPTSMetadataDatetimeQualifier.OBS_DATETIME,
            context);

    for (Integer pId : cohort) {

      Obs obs = EptsCalculationUtils.resultForPatient(aaResultMap, pId);
      Date aa4LastDate = EptsCalculationUtils.resultForPatient(aa4ResultMap, pId);

      if (obs != null && aa4LastDate != null) {
        List<Obs> criotherapies =
            EptsCalculationUtils.extractResultValues((ListResult) criotherapyResulMap.get(pId));
        List<Obs> criotherapyDates =
            EptsCalculationUtils.extractResultValues((ListResult) criotherapyDateResulMap.get(pId));
        List<Obs> viaResultRefences =
            EptsCalculationUtils.extractResultValues(
                (ListResult) viaResultRefenceResulMap.get(pId));

        for (Obs criotherapy : criotherapies) {

          if (criotherapy.getEncounter().getEncounterDatetime().compareTo(aa4LastDate) >= 0
              && criotherapy
                      .getEncounter()
                      .getEncounterDatetime()
                      .compareTo(obs.getEncounter().getEncounterDatetime())
                  <= 0) {
            map.put(pId, new SimpleResult(criotherapy, this));
            break;
          }
        }

        for (Obs criotherapyDate : criotherapyDates) {
          if (criotherapyDate.getValueDatetime().compareTo(aa4LastDate) >= 0
              && criotherapyDate
                      .getValueDatetime()
                      .compareTo(obs.getEncounter().getEncounterDatetime())
                  <= 0) {
            map.put(pId, new SimpleResult(criotherapyDate, this));
            break;
          }
        }

        for (Obs viaResultRefence : viaResultRefences) {

          if (viaResultRefence.getObsDatetime().compareTo(aa4LastDate) >= 0
              && viaResultRefence
                      .getObsDatetime()
                      .compareTo(obs.getEncounter().getEncounterDatetime())
                  <= 0) {
            map.put(pId, new SimpleResult(viaResultRefence, this));
            break;
          }
        }
      }
    }

    return map;
  }

  private CalculationResultMap getAA4Map(
      HivMetadata hivMetadata, Collection<Integer> cohort, PatientCalculationContext context) {
    SqlPatientDataDefinition def = new SqlPatientDataDefinition();
    def.addParameter(new Parameter(ON_OR_AFTER, ON_OR_AFTER, Date.class));
    def.addParameter(new Parameter(LOCATION, LOCATION, Location.class));

    String sql =
        CXCASCRNQueries.getAA3OrAA4Query(
            CXCASCRNCohortQueries.CXCASCRNResult.POSITIVE, hivMetadata, true);

    def.setQuery(sql);
    Map<String, Object> params = new HashMap<>();
    params.put(LOCATION, context.getFromCache(LOCATION));
    params.put(ON_OR_AFTER, context.getFromCache(ON_OR_AFTER));
    return EptsCalculationUtils.evaluateWithReporting(def, cohort, params, null, context);
  }
}
