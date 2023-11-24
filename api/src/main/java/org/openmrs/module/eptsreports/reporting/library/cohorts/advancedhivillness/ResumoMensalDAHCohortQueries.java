package org.openmrs.module.eptsreports.reporting.library.cohorts.advancedhivillness;

import static org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils.map;

import java.util.*;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResumoMensalDAHCohortQueries {

  private final ListOfPatientsInAdvancedHivIllnessCohortQueries
      listOfPatientsInAdvancedHivIllnessCohortQueries;
  private final HivMetadata hivMetadata;

  @Autowired
  public ResumoMensalDAHCohortQueries(
      ListOfPatientsInAdvancedHivIllnessCohortQueries
          listOfPatientsInAdvancedHivIllnessCohortQueries,
      HivMetadata hivMetadata) {
    this.listOfPatientsInAdvancedHivIllnessCohortQueries =
        listOfPatientsInAdvancedHivIllnessCohortQueries;
    this.hivMetadata = hivMetadata;
  }

  /**
   * <b>Relatório- Indicador 0 Utentes em DAH até o fim do mês anterior</b>
   * <li>Com registo de “Data de Início no Modelo de DAH”, na Ficha de DAH, ocorrida até fim do mês
   *     anterior [“Data de Início no Modelo de DAH” <= “Data Início” menos (-) 1 dia].
   *
   *     <p>Excluindo todos os utentes
   * <li>Com registo de pelo menos um motivo (Óbito/ Abandono/ Transferido Para) e “Data de Saída de
   *     TARV na US” (secção J), na Ficha de DAH, ocorrida após a data mais recente da “Data de
   *     Início no Modelo de DAH” e até o fim do mês anterior [“Data de Saída de TARV na US” >=
   *     “Última Data de Início no Modelo de DAH” e <= “Data Início” menos (-) 1 dia] ou
   * <li>Com registo de “Data de Saída” (secção I), registada na Ficha de DAH e ocorrida após a data
   *     mais recente da “Data de Início no Modelo de DAH” e até o fim do mês anterior [“Data de
   *     Saída de TARV na US” >= “Última Data de Início no Modelo de DAH” e <= “Data Início” menos
   *     (-) 1 dia]
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoStartedFollowupOnDAHComposition() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Número total de activos em DAH em TARV,  até ao fim do mês anterior");
    cd.addParameters(getCohortParameters());

    cd.addSearch(
        "onDAH",
        map(
            listOfPatientsInAdvancedHivIllnessCohortQueries.getPatientsWhoStartedFollowupOnDAH(
                false),
            "startDate=${startDate-1d},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "leftTreatment",
        map(
            getPatientsWhoLeftFollowupOnDAHByTheEndOfPreviousMonth(),
            "startDate=${startDate-1d},location=${location}"));

    cd.setCompositionString("onDAH AND NOT leftTreatment");
    return cd;
  }

  /**
   *
   * <li>Com registo de pelo menos um motivo (Óbito/ Abandono/ Transferido Para) e “Data de Saída de
   *     TARV na US” (secção J), na Ficha de DAH, ocorrida após a data mais recente da “Data de
   *     Início no Modelo de DAH” e até o fim do mês anterior [“Data de Saída de TARV na US” >=
   *     “Última Data de Início no Modelo de DAH” e <= “Data Início” menos (-) 1 dia] ou
   * <li>Com registo de “Data de Saída” (secção I), registada na Ficha de DAH e ocorrida após a data
   *     mais recente da “Data de Início no Modelo de DAH” e até o fim do mês anterior [“Data de
   *     Saída de TARV na US” >= “Última Data de Início no Modelo de DAH” e <= “Data Início” menos
   *     (-) 1 dia]
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoLeftFollowupOnDAHByTheEndOfPreviousMonth() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        " utentes que saíram do seguimento para DAH até o fim do mês anterior");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("90", hivMetadata.getAdvancedHivIllnessEncounterType().getEncounterTypeId());
    map.put("1366", hivMetadata.getPatientHasDiedConcept().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1707", hivMetadata.getAbandonedConcept().getConceptId());
    map.put("1708", hivMetadata.getExitFromArvTreatmentConcept().getConceptId());
    map.put("165386", hivMetadata.getExitDateFromArvTreatmentConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM "
            + "    patient p INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "              INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "              INNER JOIN obs o2 on e.encounter_id = o2.encounter_id "
            + "              INNER JOIN ( "
            + "        SELECT p.patient_id, MAX(e.encounter_datetime) last_date "
            + "        FROM "
            + "            patient p INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "                      INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "        WHERE p.voided = 0 AND e.voided = 0 AND o.voided = 0 "
            + "          AND e.encounter_type = ${90} "
            + "          AND e.encounter_datetime <= :startDate "
            + "          AND e.location_id = :location "
            + "        GROUP BY p.patient_id "
            + "    ) last_dah ON last_dah.patient_id = p.patient_id "
            + "WHERE p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND o.voided = 0 "
            + "  AND o2.voided = 0 "
            + "  AND e.encounter_type = ${90} "
            + "  AND ( "
            + "        (o.concept_id = ${1708} "
            + "            AND o.value_coded IN (${1366},${1706},${1707}) "
            + "            AND o.obs_datetime >= last_dah.last_date) "
            + "        OR  (o2.concept_id = ${165386} "
            + "        AND o2.value_datetime >= last_dah.last_date "
            + "        AND o2.value_datetime <= :startDate) "
            + "    ) "
            + "  AND e.location_id = :location "
            + "GROUP BY p.patient_id";

    StringSubstitutor substitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(substitutor.replace(query));

    return sqlCohortDefinition;
  }

  private List<Parameter> getCohortParameters() {
    return Arrays.asList(
        ReportingConstants.START_DATE_PARAMETER,
        ReportingConstants.END_DATE_PARAMETER,
        ReportingConstants.LOCATION_PARAMETER);
  }
}
