package org.openmrs.module.eptsreports.reporting.library.cohorts.cd4request;

import static org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils.map;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ResumoMensalCohortQueries;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsEligibleForCd4RequestCohortQueries {

  private final HivMetadata hivMetadata;

  private final ResumoMensalCohortQueries resumoMensalCohortQueries;

  String MAPPING = "startDate=${startDate},endDate=${endDate},location=${location}";
  String MAPPING2 =
      "startDate=${startDate},endDate=${endDate},generationDate=${generationDate},location=${location}";

  @Autowired
  public ListOfPatientsEligibleForCd4RequestCohortQueries(
      HivMetadata hivMetadata, ResumoMensalCohortQueries resumoMensalCohortQueries) {
    this.hivMetadata = hivMetadata;
    this.resumoMensalCohortQueries = resumoMensalCohortQueries;
  }

  /**
   * <b>C1 - Utentes que iniciaram TARV durante o período</b>
   *
   * <p>incluindo todos os utentes do indicador B1 do relatório “Resumo Mensal de HIV/SIDA” (Nº de
   * utentes que iniciaram TARV durante o mês) para o período do relatório correspondente a “Data
   * Início” e “Data Fim”.
   *
   * <p>excluindo todos os utentes que tiveram registo de resultado de CD4 numa consulta clínica
   * (Ficha Clínica –Ficha Mestra) até a data geração do relatório; Nota 1: a definição do indicador
   * B1 (Nº de utentes que iniciaram TARV durante o mês) encontra-se detalhada no documento de
   * requisitos do relatório “Resumo Mensal de HIV/SIDA”.
   *
   * @see ResumoMensalCohortQueries#getPatientsWhoInitiatedTarvAtThisFacilityDuringCurrentMonthB1()
   *     Resumo Mensal B1
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientWhoInitiatedTarvDuringPeriod() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("generationDate", "generationDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition rmB1 =
        resumoMensalCohortQueries.getPatientsWhoInitiatedTarvAtThisFacilityDuringCurrentMonthB1();

    CohortDefinition cd4ResultByReportGenerationDate =
        getPatientsWithCd4ResultsOnRestartedTarvDate(false);

    compositionCohortDefinition.addSearch("B1", map(rmB1, MAPPING));

    compositionCohortDefinition.addSearch(
        "CD4RESULT", map(cd4ResultByReportGenerationDate, MAPPING2));

    compositionCohortDefinition.setCompositionString("B1 AND NOT CD4RESULT");

    return compositionCohortDefinition;
  }

  /**
   * <b>todos os utentes que tiveram registo de resultado de CD4 numa consulta clínica (Ficha
   * Clínica – Ficha Mestra) até a data (geração do relatório / período do relatório ) </b>
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithCd4ResultsOnRestartedTarvDate(Boolean duringPeriod) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "todos os utentes que tiveram registo de resultado de CD4 numa consulta"
            + " clínica (Ficha Clínica –Ficha Mestra) até a data geração do relatório");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("generationDate", "generationDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("730", hivMetadata.getCD4PercentConcept().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("1705", hivMetadata.getRestartConcept().getConceptId());

    String query =
        "SELECT pa.patient_id "
            + "FROM "
            + "    patient pa "
            + "        INNER JOIN encounter enc "
            + "                   ON enc.patient_id =  pa.patient_id "
            + "        INNER JOIN obs "
            + "                   ON obs.encounter_id = enc.encounter_id "
            + "WHERE  pa.voided = 0 "
            + "  AND enc.voided = 0 "
            + "  AND obs.voided = 0 "
            + "  AND enc.encounter_type = ${6} "
            + "  AND ( "
            + "        (obs.concept_id = ${1695} AND obs.value_numeric IS NOT NULL) "
            + "        OR "
            + "        (obs.concept_id = ${730} AND obs.value_numeric IS NOT NULL) "
            + "      ) "
                .concat(
                    duringPeriod
                        ? "       AND e.encounter_datetime >= :startDate "
                            + "       AND e.encounter_datetime <= :endDate "
                        : "  AND enc.encounter_datetime <= :generationDate ")
            + "  AND enc.location_id = :location "
            + "GROUP BY pa.patient_id";

    StringSubstitutor sb = new StringSubstitutor(map);
    sqlCohortDefinition.setQuery(sb.replace(query));

    return sqlCohortDefinition;
  }
}
