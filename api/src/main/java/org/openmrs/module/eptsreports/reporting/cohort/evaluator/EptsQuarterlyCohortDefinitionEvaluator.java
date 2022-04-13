package org.openmrs.module.eptsreports.reporting.cohort.evaluator;

import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.reporting.cohort.definition.EptsQuarterlyCohortDefinition;
import org.openmrs.module.eptsreports.reporting.library.cohorts.GenericCohortQueries;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reportingcompatibility.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Handler(supports = EptsQuarterlyCohortDefinition.class)
public class EptsQuarterlyCohortDefinitionEvaluator implements CohortDefinitionEvaluator {

  private CohortDefinitionService cohortDefinitionService;
  private GenericCohortQueries genericCohortQueries;

  @Autowired
  public EptsQuarterlyCohortDefinitionEvaluator(
      CohortDefinitionService cohortDefinitionService, GenericCohortQueries genericCohortQueries) {
    this.cohortDefinitionService = cohortDefinitionService;
    this.genericCohortQueries = genericCohortQueries;
  }

  @Override
  public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context)
      throws EvaluationException {
    EptsQuarterlyCohortDefinition cd = (EptsQuarterlyCohortDefinition) cohortDefinition;
    EvaluatedCohort ret = new EvaluatedCohort(cohortDefinition, context);
    Integer year = cd.getYear();
    EptsQuarterlyCohortDefinition.Quarter quarter = cd.getQuarter();
    EptsQuarterlyCohortDefinition.Month month = cd.getMonth();
    Map<String, Date> range = getRange(year, quarter, month);
    context.getParameterValues().putAll(range);
    context.setBaseCohort(evaluateBaseCohort(context));
    Cohort c = cohortDefinitionService.evaluate(cd.getCohortDefinition(), context);
    //    Cohort c = evaluateBaseCohort(context);
    //    System.out.println(
    //        "PATIENTS FROM BASECOHORT (LINE 46) =>" + context.getBaseCohort().getMemberIds());
    //    System.out.println("PATIENTS FROM C(LINE 47) =>" + c.getMemberIds());
    //    Cohort s = getAllPatientsCohort();
    //    if (c == null) {
    //      c = getAllPatientsCohort();
    //    }
//    ret.getMemberIds().addAll(getAllMembersIds(c).getMemberIds()); // BRINGS EMPTY ARRAY
    //    getAllMembersIds(c);
        ret.setMemberIds(getAllMembersIds(c));
    //    System.out.println("RET RESULT => " + ret.getMemberIds());
    //    System.out.println("BY NICHOLAS => " + s.getMemberIds());
    return ret;
  }

  private EvaluatedCohort evaluateBaseCohort(EvaluationContext context) throws EvaluationException {
    EvaluationContext baseCohortContext = new EvaluationContext();
    baseCohortContext.addParameterValue("endDate", context.getParameterValue("endDate"));
    baseCohortContext.addParameterValue("location", context.getParameterValue("location"));
    return cohortDefinitionService.evaluate(
        genericCohortQueries.getBaseCohort(), baseCohortContext);
  }

  /**
   * If both quarter and month are given it returns a map containing the dates for the beginning and
   * end of the month within the quarter.
   *
   * <p>If no month is given returns the dates for the whole quarter.
   */
  public Map<String, Date> getRange(
      Integer year,
      EptsQuarterlyCohortDefinition.Quarter quarter,
      EptsQuarterlyCohortDefinition.Month month) {
    Integer q = quarter.ordinal() + 1;
    Integer m = null;
    if (month != null) {
      q = null;
      m = 3 * quarter.ordinal() + month.ordinal() + 1;
    }
    Map<String, Date> periodDates = DateUtil.getPeriodDates(year, q, m);
    Date start = DateUtil.getStartOfDay(periodDates.get("startDate"));
    Date end = DateUtil.getStartOfDay(periodDates.get("endDate"));
    periodDates.put("startDate", start);
    periodDates.put("onOrAfter", start);
    periodDates.put("endDate", end);
    periodDates.put("onOrBefore", end);
    return periodDates;
  }

  private Cohort getAllPatientsCohort() {
    ReportService reportService = Context.getService(ReportService.class);
    Set<Integer> ids = new HashSet<Integer>();
    for (Integer values : reportService.getAllPatients().getMemberIds()) {
      if (values != null) {
        ids.add(values);
      }
    }
    return new Cohort("All patients", "All Patients returned from the DB", ids);
  }

  private Set<Integer> getAllMembersIds(Cohort c) {
    //    ReportService reportService = Context.getService(ReportService.class);
    Set<Integer> ids = new HashSet<>();
    for (Integer values : c.getMemberIds()) {
      if (values != null) {
        ids.add(values);
      }
    }
    return ids;
  }
}
