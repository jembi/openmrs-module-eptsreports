package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DQACargaViralCohortQueries {

  private ResumoMensalCohortQueries resumoMensalCohortQueries;

  @Autowired
  public DQACargaViralCohortQueries(ResumoMensalCohortQueries resumoMensalCohortQueries) {
    this.resumoMensalCohortQueries = resumoMensalCohortQueries;
  }

  public CohortDefinition getBaseCohort() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    String mapping1 = "startDate=${startDate},endDate=${startDate+1m-1d},location=${location}";
    String mapping2 = "startDate=${startDate+1m},endDate=${startDate+2m-1d},location=${location}";
    String mapping3 = "startDate=${startDate+2m},endDate=${endDate},location=${location}";

    CohortDefinition E2 =
        resumoMensalCohortQueries
            .getNumberOfActivePatientsInArtAtTheEndOfTheCurrentMonthHavingVlTestResults();

    compositionCohortDefinition.addSearch("L1", EptsReportUtils.map(E2, mapping1));
    compositionCohortDefinition.addSearch("L2", EptsReportUtils.map(E2, mapping2));
    compositionCohortDefinition.addSearch("L3", EptsReportUtils.map(E2, mapping3));

    compositionCohortDefinition.setCompositionString("L1 OR L2 OR L3");

    return compositionCohortDefinition;
  }

  public DataDefinition getAge(String calculateAgeOn) {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    if (calculateAgeOn.equals("startDate")) {
      spdd.setName("Patient Age at reporting startDate");
      spdd.addParameter(new Parameter("startDate", "startDate", Date.class));
    } else if (calculateAgeOn.equals("endDate")) {
      spdd.setName("Patient Age at reporting endDate");
      spdd.addParameter(new Parameter("endDate", "endDate", Date.class));
    } else {
      spdd.setName("Patient Age at reporting evaluation Date");
      spdd.addParameter(new Parameter("evaluationDate", "evaluationDate", Date.class));
    }

    Map<String, Integer> valuesMap = new HashMap<>();

    String sql = "";
    if (calculateAgeOn.equals("startDate")) {
      sql += " SELECT p.patient_id, FLOOR(DATEDIFF(:startDate,ps.birthdate)/365) AS age ";
    } else if (calculateAgeOn.equals("endDate")) {
      sql += " SELECT p.patient_id, FLOOR(DATEDIFF(:endDate,ps.birthdate)/365) AS age ";
    } else {
      sql += " SELECT p.patient_id, FLOOR(DATEDIFF(:evaluationDate,ps.birthdate)/365) AS age ";
    }
    sql +=
        " FROM patient p "
            + " INNER JOIN person ps ON p.patient_id = ps.person_id "
            + " WHERE p.voided=0"
            + " AND ps.voided=0";

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(sql));
    return spdd;
  }
}
