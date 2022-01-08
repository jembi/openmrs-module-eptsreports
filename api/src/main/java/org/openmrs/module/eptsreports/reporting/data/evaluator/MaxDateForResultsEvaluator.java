package org.openmrs.module.eptsreports.reporting.data.evaluator;

import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.EncounterType;
import org.openmrs.annotation.Handler;
import org.openmrs.module.eptsreports.reporting.data.definition.InitialArtStartDateDataDefinition;
import org.openmrs.module.eptsreports.reporting.data.definition.MaxDateForResultsDataDefinition;
import org.openmrs.module.eptsreports.reporting.library.queries.CommonQueries;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.data.patient.EvaluatedPatientData;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.evaluator.PatientDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

@Handler(supports = MaxDateForResultsDataDefinition.class, order = 50)
public class MaxDateForResultsEvaluator implements PatientDataEvaluator {

  protected static final Log log = LogFactory.getLog(InitialArtStartDateDataDefinition.class);

  private final EvaluationService evaluationService;
  private final CommonQueries commonQueries;

  @Autowired
  public MaxDateForResultsEvaluator(
      EvaluationService evaluationService, CommonQueries commonQueries) {
    this.evaluationService = evaluationService;
    this.commonQueries = commonQueries;
  }

  @Override
  public EvaluatedPatientData evaluate(PatientDataDefinition definition, EvaluationContext context)
      throws EvaluationException {
    MaxDateForResultsDataDefinition def = (MaxDateForResultsDataDefinition) definition;

    EvaluatedPatientData evaluatedPatientData = new EvaluatedPatientData(def, context);

    if (context.getBaseCohort() != null && context.getBaseCohort().isEmpty()) {
      return evaluatedPatientData;
    }
    String onOrBefore = DateUtil.formatDate(def.getOnOrBefore(), "yyyy-MM-dd");
    String onOrAfter = DateUtil.formatDate(def.getOnOrAfter(), "yyyy-MM-dd");
    Integer location = def.getLocation().getLocationId();
    List<EncounterType> types = def.getEncounterTypeList();
    Integer questionConcept = def.getQuestionConcept();

    SqlQueryBuilder q =
        new SqlQueryBuilder(
            commonQueries.obsDateOverallQuery(
                onOrAfter, onOrBefore, questionConcept, location, types));

    List<Object[]> results = evaluationService.evaluateToList(q, context);

    for (Object[] row : results) {
      Integer patientId = Integer.valueOf(String.valueOf(row[0]));
      Date artStartDate = (Date) (row[1]);

      if (artStartDate != null) {
        evaluatedPatientData.addData(patientId, artStartDate);
      } else {
        evaluatedPatientData.addData(patientId, null);
      }
    }
    return evaluatedPatientData;
  }
}
