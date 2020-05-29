package org.openmrs.module.eptsreports.reporting.calculation.ltfu;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.ListResult;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.eptsreports.reporting.calculation.BooleanResult;
import org.openmrs.module.eptsreports.reporting.calculation.common.EPTSCalculationService;
import org.openmrs.module.eptsreports.reporting.utils.EptsCalculationUtils;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.stereotype.Component;

@Component
public class LTFUCalculation extends AbstractPatientCalculation {

  private static final String LOCATION = "location";

  private static final String ON_OR_BEFORE = "onOrBefore";

  private static final String NUM_DAYS = "numDays";

  @Override
  public CalculationResultMap evaluate(
      Collection<Integer> cohort,
      Map<String, Object> parameterValues,
      PatientCalculationContext context) {

    Location location = (Location) context.getFromCache(LOCATION);

    Date onOrBefore = (Date) context.getFromCache(ON_OR_BEFORE);

    Integer numDays = (Integer) parameterValues.get(NUM_DAYS);

    HivMetadata hivMetadata = Context.getRegisteredComponents(HivMetadata.class).get(0);
    CommonMetadata commonMetadata = Context.getRegisteredComponents(CommonMetadata.class).get(0);

    EncounterType pharmacyEncounterType = hivMetadata.getARVPharmaciaEncounterType();
    Concept returnVisitDateForArvDrugConcept = hivMetadata.getReturnVisitDateForArvDrugConcept();

    EncounterType adultEncounterType = hivMetadata.getAdultoSeguimentoEncounterType();
    EncounterType pediatricEncounterType = hivMetadata.getPediatriaSeguimentoEncounterType();
    Concept returnVisitDateConcept = commonMetadata.getReturnVisitDateConcept();

    EncounterType masterCardDrugPickupEncounterType =
        hivMetadata.getMasterCardDrugPickupEncounterType();
    Concept artDatePickupMasterCard = hivMetadata.getArtDatePickupMasterCard();

    EPTSCalculationService eptsCalculationService =
        Context.getRegisteredComponents(EPTSCalculationService.class).get(0);

    CalculationResultMap obs5096Map =
        eptsCalculationService.getObs(
            returnVisitDateForArvDrugConcept,
            Arrays.asList(pharmacyEncounterType),
            cohort,
            Arrays.asList(location),
            null,
            TimeQualifier.ANY,
            null,
            onOrBefore,
            context);

    CalculationResultMap obs1410Map =
        eptsCalculationService.getObs(
            returnVisitDateConcept,
            Arrays.asList(adultEncounterType, pediatricEncounterType),
            cohort,
            Arrays.asList(location),
            null,
            TimeQualifier.ANY,
            null,
            onOrBefore,
            context);

    CalculationResultMap lastPharmacyEncounterMap =
        eptsCalculationService.getEncounter(
            Arrays.asList(pharmacyEncounterType),
            TimeQualifier.LAST,
            cohort,
            location,
            onOrBefore,
            context);

    CalculationResultMap lastPediatricAdultEncounterMao =
        eptsCalculationService.getEncounter(
            Arrays.asList(adultEncounterType, pediatricEncounterType),
            TimeQualifier.LAST,
            cohort,
            location,
            onOrBefore,
            context);

    CalculationResultMap getLastMasterCardPickUpMap =
        this.getLastMasterCardPickUp(
            masterCardDrugPickupEncounterType, artDatePickupMasterCard, cohort, context);

    CalculationResultMap map = new CalculationResultMap();

    for (Integer patientId : cohort) {

      ListResult obs5096ResultList = (ListResult) obs5096Map.get(patientId);

      ListResult obs1410ResultList = (ListResult) obs1410Map.get(patientId);

      SimpleResult simpleResultPharmacy = (SimpleResult) lastPharmacyEncounterMap.get(patientId);
      SimpleResult simpleResultPediatricAdult =
          (SimpleResult) lastPediatricAdultEncounterMao.get(patientId);

      Date maxPharmacyDate = null, maxAdultPediatricDate = null, maxMasterCardPickupDate = null;

      if (simpleResultPharmacy != null && obs5096ResultList != null) {

        Encounter encounter = (Encounter) simpleResultPharmacy.getValue();

        List<Obs> obss = EptsCalculationUtils.extractResultValues(obs5096ResultList);

        if (hasTheEncounterHaveObs(encounter, returnVisitDateForArvDrugConcept, obss)) {
          maxPharmacyDate = encounter.getEncounterDatetime();
        }
      }

      if (simpleResultPediatricAdult != null && obs1410ResultList != null) {

        Encounter encounter = (Encounter) simpleResultPediatricAdult.getValue();

        List<Obs> obss = EptsCalculationUtils.extractResultValues(obs1410ResultList);

        if (hasTheEncounterHaveObs(encounter, returnVisitDateConcept, obss)) {
          maxAdultPediatricDate = encounter.getEncounterDatetime();
        }
      }

      maxMasterCardPickupDate =
          EptsCalculationUtils.resultForPatient(getLastMasterCardPickUpMap, patientId);

      List<Date> dates =
          Arrays.asList(maxPharmacyDate, maxAdultPediatricDate, maxMasterCardPickupDate);

      Date date = this.getTheMaxDate(dates);

      if (date == null) {
        continue;
      }

      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      calendar.add(Calendar.DATE, numDays);
      Date maxDate = calendar.getTime();

      if (maxDate.compareTo(onOrBefore) <= 0) {
        map.put(patientId, new BooleanResult(true, this));
      }
    }

    return map;
  }

  private CalculationResultMap getLastMasterCardPickUp(
      EncounterType encounterType,
      Concept concept,
      Collection<Integer> cohort,
      PatientCalculationContext context) {

    SqlPatientDataDefinition patientDataDefinition = new SqlPatientDataDefinition();
    patientDataDefinition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    patientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    String sql =
        "SELECT enc.patient_id,   "
            + "                             Date_add(Max(obs.value_datetime), interval 30 day) value_datetime  "
            + "                         FROM   patient pa   "
            + "                             inner join encounter enc   "
            + "                                 ON enc.patient_id = pa.patient_id   "
            + "                             inner join obs obs   "
            + "                                 ON obs.encounter_id = enc.encounter_id   "
            + "                         WHERE  pa.voided = 0   "
            + "                             AND enc.voided = 0   "
            + "                             AND obs.voided = 0   "
            + "                             AND obs.concept_id = ${concept}   "
            + "                             AND obs.value_datetime IS NOT NULL   "
            + "                             AND enc.encounter_type = ${encounterType}   "
            + "                             AND enc.location_id = :location   "
            + "                             AND obs.value_datetime <= :onOrBefore  "
            + "                        GROUP  BY pa.patient_id  ";

    Map<String, Integer> map = new HashMap<>();
    map.put("encounterType", encounterType.getEncounterTypeId());
    map.put("concept", concept.getConceptId());

    StringSubstitutor sb = new StringSubstitutor(map);
    String mappedQuery = sb.replace(sql);

    patientDataDefinition.setQuery(mappedQuery);
    Map<String, Object> params = new HashMap<>();
    params.put("location", context.getFromCache("location"));
    params.put("onOrBefore", context.getFromCache("onOrBefore"));
    return EptsCalculationUtils.evaluateWithReporting(
        patientDataDefinition, cohort, params, null, context);
  }

  private boolean hasTheEncounterHaveObs(Encounter encounter, Concept concept, List<Obs> obss) {

    for (Obs o : obss) {
      if (o.getConcept().equals(concept) && o.getEncounter().equals(encounter)) {
        return true;
      }
    }
    return false;
  }

  private Date getTheMaxDate(List<Date> dates) {

    Date max = new GregorianCalendar(1980, 5, 1).getTime();
    for (Date current : dates) {
      if (current != null) {
        if (current.compareTo(max) > 0) {
          max = current;
        }
      }
    }

    if (max.equals(new GregorianCalendar(1980, 5, 1).getTime())) {
      return null;
    }

    return max;
  }
}
