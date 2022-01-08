/*
 * The contents of this file are subject to the OpenMRS Public License Version
 * 1.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 *
 * Copyright (C) OpenMRS, LLC. All Rights Reserved.
 */
package org.openmrs.module.eptsreports.reporting.calculation.pvls;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.eptsreports.reporting.calculation.BooleanResult;
import org.openmrs.module.eptsreports.reporting.calculation.common.EPTSCalculationService;
import org.openmrs.module.eptsreports.reporting.calculation.generic.InitialArtStartDateCalculation;
import org.openmrs.module.eptsreports.reporting.data.definition.MaxDateForResultsDataDefinition;
import org.openmrs.module.eptsreports.reporting.utils.EptsCalculationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <b>Description</b> On ART for that X months
 *
 * <blockquote>
 *
 * Based on Viral load results taken in the last 12 months from end of reporting period and the ART
 * start date
 *
 * </blockquote>
 */
@Component
public class OnArtForMoreThanXmonthsCalcultion extends AbstractPatientCalculation {

  @Autowired private HivMetadata hivMetadata;

  @Autowired private EPTSCalculationService ePTSCalculationService;

  @Override
  public CalculationResultMap evaluate(
      Collection<Integer> cohort, Map<String, Object> params, PatientCalculationContext context) {

    CalculationResultMap map = new CalculationResultMap();
    Location location = (Location) context.getFromCache("location");
    List<EncounterType> encounterTypeList = (List<EncounterType>) params.get("listOfEncounters");
    Concept viralLoadConcept = hivMetadata.getHivViralLoadConcept();
    Concept qualitativeViralLoadResults = hivMetadata.getHivViralLoadQualitative();

    // get data inicio TARV
    // TODO: pass in as a parameter and only recalculate if parameter is
    // null
    CalculationResultMap arvsInitiationDateMap =
        calculate(
            Context.getRegisteredComponents(InitialArtStartDateCalculation.class).get(0),
            cohort,
            context);
    Date onOrBefore = (Date) context.getFromCache("onOrBefore");
    Date oneYearBefore = EptsCalculationUtils.addMonths(onOrBefore, -12);
    CalculationResultMap lastVl =
        getResultMap(
            oneYearBefore,
            onOrBefore,
            viralLoadConcept.getConceptId(),
            location,
            encounterTypeList,
            cohort,
            params,
            context);
    CalculationResultMap qViralLoadResultsMap =
        getResultMap(
            oneYearBefore,
            oneYearBefore,
            qualitativeViralLoadResults.getConceptId(),
            location,
            encounterTypeList,
            cohort,
            params,
            context);

    for (Integer ptId : cohort) {
      boolean isOnArtForMoreThan3Months = false;
      SimpleResult artStartDateResult = (SimpleResult) arvsInitiationDateMap.get(ptId);
      SimpleResult lastVlResults1DateResults = (SimpleResult) lastVl.get(ptId);
      SimpleResult lastVlResults2DateResults = (SimpleResult) qViralLoadResultsMap.get(ptId);
      Date artStartDate;
      Date lastVlResults1Date;
      Date lastVlResults2Date;
      if (artStartDateResult != null) {
        artStartDate = (Date) artStartDateResult.getValue();
        lastVlResults1Date = (Date) lastVlResults1DateResults.getValue();
        lastVlResults2Date = (Date) lastVlResults2DateResults.getValue();
        if (artStartDate != null
            && lastVlResults1Date != null
            && isAtLeastThreeMonthsLater(artStartDate, lastVlResults1Date)) {
          isOnArtForMoreThan3Months = true;
        }
        if (artStartDate != null
            && lastVlResults2Date != null
            && isAtLeastThreeMonthsLater(artStartDate, lastVlResults2Date)) {
          isOnArtForMoreThan3Months = true;
        }
      }
      map.put(ptId, new BooleanResult(isOnArtForMoreThan3Months, this));
    }
    return map;
  }

  private boolean isAtLeastThreeMonthsLater(Date artStartDate, Date lastVlDate) {
    Date threeMonthsLater = EptsCalculationUtils.addDays(artStartDate, 90);
    return lastVlDate.compareTo(threeMonthsLater) >= 0;
  }

  private CalculationResultMap getResultMap(
      Date startDate,
      Date endDate,
      Integer conceptQuestion,
      Location location,
      List<EncounterType> types,
      Collection<Integer> cohort,
      Map<String, Object> params,
      PatientCalculationContext context) {
    MaxDateForResultsDataDefinition maxDateForResultsDataDefinition =
        new MaxDateForResultsDataDefinition();
    maxDateForResultsDataDefinition.setName("Patients with given observations on a given date");
    maxDateForResultsDataDefinition.setOnOrBefore(endDate);
    maxDateForResultsDataDefinition.setOnOrAfter(startDate);
    maxDateForResultsDataDefinition.setLocation(location);
    maxDateForResultsDataDefinition.setQuestionConcept(conceptQuestion);
    maxDateForResultsDataDefinition.setEncounterTypeList(types);
    return EptsCalculationUtils.evaluateWithReporting(
        maxDateForResultsDataDefinition, cohort, params, null, context);
  }
}
