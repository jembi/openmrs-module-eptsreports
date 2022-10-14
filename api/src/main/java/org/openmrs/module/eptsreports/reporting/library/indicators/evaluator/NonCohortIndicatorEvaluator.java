package org.openmrs.module.eptsreports.reporting.library.indicators.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.library.indicators.NonCohortIndicator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.indicator.Indicator;
import org.openmrs.module.reporting.indicator.SimpleIndicatorResult;
import org.openmrs.module.reporting.indicator.evaluator.IndicatorEvaluator;
import org.openmrs.parameter.EncounterSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Handler(supports = NonCohortIndicator.class)
public class NonCohortIndicatorEvaluator implements IndicatorEvaluator {

  protected static final Log log = LogFactory.getLog(NonCohortIndicatorEvaluator.class);
  @Autowired
  HivMetadata hivMetadata;

  @Override
  public SimpleIndicatorResult evaluate(Indicator indicator, EvaluationContext context) throws EvaluationException {
    NonCohortIndicator nonCohortIndicator = (NonCohortIndicator) indicator;

    Date fromDate = nonCohortIndicator.getStartDate();
    Date toDate = nonCohortIndicator.getEndDate();
    Location location = nonCohortIndicator.getLocation();

    List<Encounter> hivCareEncounters = Context.getEncounterService().getEncounters(encounterSearchCriteria(location, fromDate, toDate, Arrays.asList(hivMetadata.getAdultoSeguimentoEncounterType(),hivMetadata.getMasterCardEncounterType())));

    SimpleIndicatorResult result = new SimpleIndicatorResult();
    result.setIndicator(indicator);
    result.setContext(context);
    result.setNumeratorResult(hivCareEncounters.size());

    return result;

  }
  private EncounterSearchCriteria encounterSearchCriteria(Location facility, Date startDate, Date endDate, List<EncounterType> encounterTypeList) {
    return new EncounterSearchCriteria(null, facility, startDate, endDate, null,
            null, encounterTypeList, null, null, null,
            false);
  }

}
