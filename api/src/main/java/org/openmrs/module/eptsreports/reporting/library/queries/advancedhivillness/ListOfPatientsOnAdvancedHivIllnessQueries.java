package org.openmrs.module.eptsreports.reporting.library.queries.advancedhivillness;

import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ResumoMensalCohortQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsQueriesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsOnAdvancedHivIllnessQueries {

  @Autowired private HivMetadata hivMetadata;
  @Autowired private ResumoMensalCohortQueries resumoMensalCohortQueries;
  /**
   *
   * <li>Utentes com registo do resultado de CD4 (absoluto) na “Ficha Clínica – Ficha Mestra” ou
   *     “Ficha e-Lab” ou “Ficha de Laboratório” ou “Ficha de Doença Avançada por HIV”, durante o
   *     período de avaliação (“Data Resultado CD4” >= “Data Início Avaliação” e “Data Resultado
   *     CD4” <= “Data Fim Avaliação”) e com resultado CD4 (absoluto) < <b>valueNumeric</b>
   *
   * @param valueNumeric Max value of CD4 result
   * @param mostRecentDateOrCd4Result Flag to return Most Recent date or Cd4 Result
   * @return {@link String}
   */
  public String getPatientsWithCD4AbsoluteResultOnPeriodQuery(
      int valueNumeric, boolean mostRecentDateOrCd4Result) {

    String fromSQL =
        " FROM   person ps "
            + "       INNER JOIN encounter e "
            + "               ON ps.person_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + "WHERE  ps.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type IN ( ${6}, ${13}, ${51} ) "
            + "       AND o.concept_id = ${1695}  "
            + "       AND o.value_numeric < "
            + valueNumeric
            + "       AND DATE(e.encounter_datetime) >= :startDate "
            + "       AND DATE(e.encounter_datetime) <= :endDate "
            + "       AND e.location_id = :location";

    return mostRecentDateOrCd4Result
        ? " SELECT ps.person_id, Max(DATE(e.encounter_datetime)) AS most_recent ".concat(fromSQL)
        : " SELECT ps.person_id, o.value_numeric AS cd4_result ".concat(fromSQL);
  }

  /**
   *
   * <li>Utentes com registo do resultado de CD4 (absoluto) na “Ficha Resumo – Ficha Mestra” durante
   *     o período de avaliação (“Data Resultado CD4” >= “Data Início Avaliação” e “Data Resultado
   *     CD4” <= “Data Fim Avaliação”) e com resultado CD4 (absoluto) < <b>valueNumeric</b>
   *
   * @param valueNumeric Max value of CD4 result
   * @param mostRecentDateOrCd4Result Flag to return Most Recent date or Cd4 Result
   * @return {@link String}
   */
  public String getPatientsWithCD4AbsoluteResultFichaResumoOnPeriodQuery(
      int valueNumeric, boolean mostRecentDateOrCd4Result) {

    String fromSQL =
        " FROM "
            + "    person ps INNER JOIN encounter e ON ps.person_id= e.patient_id "
            + "              INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "WHERE ps.voided = 0 AND e.voided = 0 AND o.voided = 0 "
            + "  AND e.encounter_type IN ( ${53}, ${90} ) "
            + "  AND o.concept_id = ${1695} "
            + "  AND o.value_numeric < "
            + valueNumeric
            + "  and o.obs_datetime >= :startDate "
            + "  AND o.obs_datetime <= :endDate "
            + "  AND e.location_id = :location";

    return mostRecentDateOrCd4Result
        ? " SELECT ps.person_id, max(o.obs_datetime) as most_recent ".concat(fromSQL)
        : " SELECT ps.person_id, o.value_numeric AS cd4_result ".concat(fromSQL);
  }

  /**
   *
   * <li>Utentes com registo do resultado de CD4 (absoluto) na “Ficha Clínica – Ficha Mestra” ou
   *     “Ficha Resumo – Ficha Mestra” ou “Ficha e-Lab” ou “Ficha de Laboratório” ou “Ficha de
   *     Doença Avançada por HIV”, durante o período de avaliação (“Data Resultado CD4” >= “Data
   *     Início Avaliação” e “Data Resultado CD4” <= “Data Fim Avaliação”) e com resultado CD4
   *     (absoluto) < 200 e idade do utente >= 5 anos ou
   *
   *     <p>Idade = Data Fim de Avaliação - Data de Nascimento
   *
   * @return {@link String}
   */
  public String getCd4ResultOverOrEqualTo5years() {
    return getPatientsWithCD4AbsoluteResultOnPeriodQuery(200, true)
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) >=5 "
        + "  GROUP BY ps.person_id "
        + " UNION "
        + getPatientsWithCD4AbsoluteResultFichaResumoOnPeriodQuery(200, true)
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) >=5 "
        + "  GROUP BY ps.person_id ";
  }

  /**
   *
   * <li>Utentes com registo do resultado de CD4 (absoluto) na “Ficha Clínica – Ficha Mestra” ou
   *     “Ficha Resumo – Ficha Mestra” ou “Ficha e-Lab” ou “Ficha de Laboratório” ou “Ficha de
   *     Doença Avançada por HIV”, durante o período de avaliação (“Data Resultado CD4” >= “Data
   *     Início Avaliação” e “Data Resultado CD4” <= “Data Fim Avaliação”) e com resultado CD4
   *     (absoluto) < 500 e idade do utente >= 1 ano e < 5 ou
   *
   *     <p>Idade = Data Fim de Avaliação - Data de Nascimento
   *
   * @return {@link String}
   */
  public String getCd4ResultBetweenOneAnd5years() {
    return getPatientsWithCD4AbsoluteResultOnPeriodQuery(500, true)
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) >=1 "
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) <5 "
        + "  GROUP BY ps.person_id "
        + " UNION "
        + getPatientsWithCD4AbsoluteResultFichaResumoOnPeriodQuery(500, true)
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) >=1 "
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) <5 "
        + "  GROUP BY ps.person_id ";
  }

  /**
   *
   * <li>Utentes com registo do resultado de CD4 (absoluto) na “Ficha Clínica – Ficha Mestra” ou
   *     “Ficha Resumo – Ficha Mestra” ou “Ficha e-Lab” ou “Ficha de Laboratório” ou “Ficha de
   *     Doença Avançada por HIV”, durante o período de avaliação (“Data Resultado CD4” >= “Data
   *     Início Avaliação” e “Data Resultado CD4” <= “Data Fim Avaliação”) e com resultado CD4
   *     (absoluto) < 750 e idade do utente < 1 ou
   *
   *     <p>Idade = Data Fim de Avaliação - Data de Nascimento
   *
   * @return {@link String}
   */
  public String getCd4ResultBellowOneYear() {
    return getPatientsWithCD4AbsoluteResultOnPeriodQuery(750, true)
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) <1 "
        + "  GROUP BY ps.person_id "
        + " UNION "
        + getPatientsWithCD4AbsoluteResultFichaResumoOnPeriodQuery(750, true)
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) <1 "
        + "  GROUP BY ps.person_id ";
  }

  /**
   * @see #getCd4ResultOverOrEqualTo5years()
   * @return {@link String}
   */
  public String getCd4ResultOverOrEqualTo5y() {
    return " SELECT ps.person_id, o.value_numeric as cd4  "
        + "FROM   person ps  "
        + "           INNER JOIN encounter e  "
        + "                      ON ps.person_id = e.patient_id  "
        + "           INNER JOIN obs o  "
        + "                      ON e.encounter_id = o.encounter_id  "
        + "INNER JOIN ( "
        + getLastCd4ResultDateQueries()
        + " ) last_cd4 ON last_cd4.person_id = ps.person_id  "
        + " WHERE  ps.voided = 0  "
        + "  AND e.voided = 0   "
        + "  AND o.voided = 0  "
        + "  AND e.encounter_type IN ( ${6}, ${13}, ${51} )  "
        + "  AND o.concept_id = ${1695}  "
        + "  AND o.value_numeric < 200  "
        + "  AND Date(e.encounter_datetime) = last_cd4.most_recent  "
        + "  AND e.location_id = :location "
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) >=5 "
        + "  GROUP BY ps.person_id "
        + " UNION "
        + "  SELECT ps.person_id, o.value_numeric as cd4  "
        + "FROM   person ps  "
        + "           INNER JOIN encounter e  "
        + "                      ON ps.person_id = e.patient_id  "
        + "           INNER JOIN obs o  "
        + "                      ON e.encounter_id = o.encounter_id  "
        + "INNER JOIN ( "
        + getLastCd4ResultDateQueries()
        + " ) last_cd4 ON last_cd4.person_id = ps.person_id  "
        + " WHERE  ps.voided = 0  "
        + " AND e.voided = 0  "
        + "  AND o.voided = 0  "
        + "  AND e.encounter_type IN ( ${53}, ${90} )  "
        + "  AND o.concept_id = ${1695}  "
        + "  AND o.value_numeric < 200  "
        + "  AND o.obs_datetime = last_cd4.most_recent  "
        + "  AND e.location_id = :location "
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) >=5 "
        + "  GROUP BY ps.person_id ";
  }

  /**
   * @see #getCd4ResultBetweenOneAnd5years()
   * @return {@link String}
   */
  public String getCd4ResultBetweenOneAnd5y() {

    return " SELECT ps.person_id, o.value_numeric as cd4  "
        + "FROM   person ps  "
        + "           INNER JOIN encounter e  "
        + "                      ON ps.person_id = e.patient_id  "
        + "           INNER JOIN obs o  "
        + "                      ON e.encounter_id = o.encounter_id  "
        + "INNER JOIN ( "
        + getLastCd4ResultDateQueries()
        + " ) last_cd4 ON last_cd4.person_id = ps.person_id  "
        + " WHERE  ps.voided = 0  "
        + "  AND e.voided = 0   "
        + "  AND o.voided = 0  "
        + "  AND e.encounter_type IN ( ${6}, ${13}, ${51} )  "
        + "  AND o.concept_id = ${1695}  "
        + "  AND o.value_numeric < 500  "
        + "  AND Date(e.encounter_datetime) = last_cd4.most_recent  "
        + "  AND e.location_id = :location "
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) >=5 "
        + "  GROUP BY ps.person_id "
        + " UNION "
        + "  SELECT ps.person_id, o.value_numeric as cd4  "
        + "FROM   person ps  "
        + "           INNER JOIN encounter e  "
        + "                      ON ps.person_id = e.patient_id  "
        + "           INNER JOIN obs o  "
        + "                      ON e.encounter_id = o.encounter_id  "
        + "INNER JOIN ( "
        + getLastCd4ResultDateQueries()
        + " ) last_cd4 ON last_cd4.person_id = ps.person_id  "
        + " WHERE  ps.voided = 0  "
        + " AND e.voided = 0  "
        + "  AND o.voided = 0  "
        + "  AND e.encounter_type IN ( ${53}, ${90} )  "
        + "  AND o.concept_id = ${1695}  "
        + "  AND o.value_numeric < 500  "
        + "  AND o.obs_datetime = last_cd4.most_recent  "
        + "  AND e.location_id = :location "
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) >=1 "
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) <5 "
        + "  GROUP BY ps.person_id ";
  }

  /**
   * @see #getCd4ResultBellowOneYear()
   * @return {@link String}
   */
  public String getCd4ResultBellow1y() {

    return " SELECT ps.person_id, o.value_numeric as cd4  "
        + "FROM   person ps  "
        + "           INNER JOIN encounter e  "
        + "                      ON ps.person_id = e.patient_id  "
        + "           INNER JOIN obs o  "
        + "                      ON e.encounter_id = o.encounter_id  "
        + "INNER JOIN ( "
        + getLastCd4ResultDateQueries()
        + " ) last_cd4 ON last_cd4.person_id = ps.person_id  "
        + " WHERE  ps.voided = 0  "
        + "  AND e.voided = 0   "
        + "  AND o.voided = 0  "
        + "  AND e.encounter_type IN ( ${6}, ${13}, ${51} )  "
        + "  AND o.concept_id = ${1695}  "
        + "  AND o.value_numeric < 750  "
        + "  AND Date(e.encounter_datetime) = last_cd4.most_recent  "
        + "  AND e.location_id = :location "
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) <1 "
        + "  GROUP BY ps.person_id "
        + " UNION "
        + "  SELECT ps.person_id, o.value_numeric as cd4  "
        + "FROM   person ps  "
        + "           INNER JOIN encounter e  "
        + "                      ON ps.person_id = e.patient_id  "
        + "           INNER JOIN obs o  "
        + "                      ON e.encounter_id = o.encounter_id  "
        + "INNER JOIN ( "
        + getLastCd4ResultDateQueries()
        + " ) last_cd4 ON last_cd4.person_id = ps.person_id  "
        + " WHERE  ps.voided = 0  "
        + " AND e.voided = 0  "
        + "  AND o.voided = 0  "
        + "  AND e.encounter_type IN ( ${53}, ${90} )  "
        + "  AND o.concept_id = ${1695}  "
        + "  AND o.value_numeric < 750  "
        + "  AND o.obs_datetime = last_cd4.most_recent  "
        + "  AND e.location_id = :location "
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) <1 "
        + "  GROUP BY ps.person_id ";
  }

  /**
   *
   * <li>Utentes com registo do resultado de CD4 (absoluto) na “Ficha Clínica – Ficha Mestra” ou
   *     “Ficha e-Lab” ou “Ficha de Laboratório” ou “Ficha de Doença Avançada por HIV”, até o fim do
   *     período de avaliação (“Data Resultado CD4” <= “Data Fim Avaliação”)
   *
   * @param mostRecentDateOrCd4Result Flag to return Most Recent date or Cd4 Result
   * @return {@link String}
   */
  public String getPatientsWithCD4AbsoluteResultOnPeriodQuery(boolean mostRecentDateOrCd4Result) {

    String fromSQL =
        " FROM   person ps "
            + "       INNER JOIN encounter e "
            + "               ON ps.person_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + "WHERE  ps.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type IN ( ${6}, ${13}, ${51} ) "
            + "                 AND o.concept_id = ${1695} "
            + "             AND o.value_numeric IS NOT NULL "
            + "             AND DATE(e.encounter_datetime) <= :endDate "
            + "       AND e.location_id = :location "
            + " GROUP BY ps.person_id ";

    return mostRecentDateOrCd4Result
        ? " SELECT ps.person_id, Max(DATE(e.encounter_datetime)) AS most_recent ".concat(fromSQL)
        : " SELECT ps.person_id, o.value_numeric AS cd4_result ".concat(fromSQL);
  }

  /**
   *
   * <li>Utentes com registo do resultado de CD4 (absoluto) na “Ficha Resumo – Ficha Mestra” até o
   *     fim do período de avaliação ( “Data Resultado CD4” <= “Data Fim Avaliação”)
   *
   * @param mostRecentDateOrCd4Result Flag to return Most Recent date or Cd4 Result
   * @return {@link String}
   */
  public String getPatientsWithCD4AbsoluteResultFichaResumoOnPeriodQuery(
      boolean mostRecentDateOrCd4Result) {

    String fromSQL =
        " FROM "
            + "    person ps INNER JOIN encounter e ON ps.person_id= e.patient_id "
            + "              INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "WHERE ps.voided = 0 AND e.voided = 0 AND o.voided = 0 "
            + "  AND e.encounter_type IN (${53}, ${90}) "
            + "  AND o.concept_id = ${1695} "
            + "  AND o.value_numeric IS NOT NULL "
            + "  AND o.obs_datetime <= :endDate "
            + "  AND e.location_id = :location "
            + " GROUP BY ps.person_id";

    return mostRecentDateOrCd4Result
        ? " SELECT ps.person_id, max(o.obs_datetime) as most_recent ".concat(fromSQL)
        : " SELECT ps.person_id, o.value_numeric AS cd4_result ".concat(fromSQL);
  }

  public String getLastCd4OrResultDateBeforeMostRecentCd4() {
    return " SELECT ps.person_id, o.value_numeric, DATE(last_cd4.second_date) AS second_cd4_result "
        + " FROM   person ps "
        + "       INNER JOIN encounter e "
        + "               ON ps.person_id = e.patient_id "
        + "       INNER JOIN obs o "
        + "               ON e.encounter_id = o.encounter_id "
        + " INNER JOIN ( "
        + " SELECT second.person_id, MAX(second.cd4_result) as second_date FROM ( "
        + getLastCd4OrResultDateBeforeMostRecentQuery()
        + " ) second GROUP BY second.person_id "
        + " ) last_cd4 ON last_cd4.person_id = ps.person_id "
        + "WHERE  ps.voided = 0 "
        + "       AND e.voided = 0 "
        + "       AND o.voided = 0 "
        + "       AND e.encounter_type IN ( ${6}, ${13}, ${51} ) "
        + "                 AND o.concept_id = ${1695} "
        + "             AND o.value_numeric IS NOT NULL "
        + "             AND DATE(e.encounter_datetime) = last_cd4.second_date "
        + "       AND e.location_id = :location"
        + "       GROUP BY ps.person_id "
        + " UNION "
        + " SELECT ps.person_id, o.value_numeric, last_cd4.second_date AS second_cd4_result "
        + " FROM "
        + "    person ps INNER JOIN encounter e ON ps.person_id= e.patient_id "
        + "              INNER JOIN obs o on e.encounter_id = o.encounter_id "
        + " INNER JOIN ( "
        + " SELECT second.person_id, MAX(second.cd4_result) as second_date FROM ( "
        + getLastCd4OrResultDateBeforeMostRecentQuery()
        + " ) second GROUP BY second.person_id "
        + " ) last_cd4 ON last_cd4.person_id = ps.person_id "
        + "WHERE ps.voided = 0 AND e.voided = 0 AND o.voided = 0 "
        + "  AND e.encounter_type IN (${53}, ${90}) "
        + "  AND o.concept_id = ${1695} "
        + "  AND o.value_numeric IS NOT NULL "
        + "  AND o.obs_datetime = last_cd4.second_date "
        + "  AND e.location_id = :location "
        + "       GROUP BY ps.person_id";
  }

  public String getLastCd4OrResultDateBeforeMostRecentQuery() {
    return " SELECT ps.person_id, MAX(DATE(e.encounter_datetime)) AS cd4_result "
        + " FROM   person ps "
        + "       INNER JOIN encounter e "
        + "               ON ps.person_id = e.patient_id "
        + "       INNER JOIN obs o "
        + "               ON e.encounter_id = o.encounter_id "
        + " INNER JOIN ( "
        + "  SELECT max.person_id, "
        + "           Max(max.most_recent) AS most_recent FROM ( "
        + new EptsQueriesUtil()
            .unionBuilder(getPatientsWithCD4AbsoluteResultOnPeriodQuery(true))
            .union(getPatientsWithCD4AbsoluteResultFichaResumoOnPeriodQuery(true))
            .buildQuery()
        + " ) max group by max.person_id ) "
        + " last_cd4 ON last_cd4.person_id = ps.person_id "
        + "WHERE  ps.voided = 0 "
        + "       AND e.voided = 0 "
        + "       AND o.voided = 0 "
        + "       AND e.encounter_type IN ( ${6}, ${13}, ${51} ) "
        + "                 AND o.concept_id = ${1695} "
        + "             AND o.value_numeric IS NOT NULL "
        + "             AND DATE(e.encounter_datetime) < last_cd4.most_recent  "
        + "       AND e.location_id = :location"
        + "       GROUP BY ps.person_id "
        + " UNION "
        + " SELECT ps.person_id, MAX(o.obs_datetime) AS cd4_result "
        + " FROM "
        + "    person ps INNER JOIN encounter e ON ps.person_id= e.patient_id "
        + "              INNER JOIN obs o on e.encounter_id = o.encounter_id "
        + " INNER JOIN ( "
        + "  SELECT max.person_id, "
        + "           Max(max.most_recent) AS most_recent FROM ( "
        + new EptsQueriesUtil()
            .unionBuilder(getPatientsWithCD4AbsoluteResultOnPeriodQuery(true))
            .union(getPatientsWithCD4AbsoluteResultFichaResumoOnPeriodQuery(true))
            .buildQuery()
        + " ) max group by max.person_id ) "
        + " last_cd4 ON last_cd4.person_id = ps.person_id "
        + "WHERE ps.voided = 0 AND e.voided = 0 AND o.voided = 0 "
        + "  AND e.encounter_type IN (${53}, ${90}) "
        + "  AND o.concept_id = ${1695} "
        + "  AND o.value_numeric IS NOT NULL "
        + "  AND o.obs_datetime < last_cd4.most_recent "
        + "  AND e.location_id = :location "
        + "       GROUP BY ps.person_id ";
  }

  public String getVLoadResultAndMostRecent() {
    return "SELECT p.patient_id, "
        + "       last_vl.most_recent, "
        + " IF(o.value_numeric IS NOT NULL, o.value_numeric, IF(o2.value_coded = 165331, CONCAT('MENOR QUE ',o2.comments), o2.value_coded)) AS viral_load "
        + "FROM   patient p "
        + "       INNER JOIN encounter e "
        + "               ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o "
        + "               ON e.encounter_id = o.encounter_id "
        + "       INNER JOIN obs o2 "
        + "               ON e.encounter_id = o2.encounter_id "
        + "       INNER JOIN ("
        + " SELECT result.patient_id, MAX(result.most_recent) as most_recent FROM ( "
        + getLastVlAndResultDateQuery()
        + "    ) result GROUP BY result.patient_id "
        + ") last_vl "
        + "               ON last_vl.patient_id = p.patient_id "
        + "WHERE  e.encounter_type = ${53} "
        + "       AND ( ( o.concept_id = ${856} "
        + "               AND o.value_numeric IS NOT NULL ) "
        + "              OR ( o2.concept_id = ${1305} "
        + "                   AND o2.value_coded IS NOT NULL ) ) "
        + "       AND o.obs_datetime = last_vl.most_recent "
        + "       AND e.location_id = :location "
        + "       AND e.voided = 0 "
        + "       AND p.voided = 0 "
        + "       AND o.voided = 0 "
        + "       AND o2.voided = 0 "
        + "GROUP  BY p.patient_id "
        + "UNION "
        + "SELECT p.patient_id, "
        + "       last_vl.most_recent, "
        + "  IF(o.value_numeric IS NOT NULL, o.value_numeric, (IF(o2.value_coded = 165331, CONCAT('MENOR QUE ',o2.comments), (IF(e.encounter_type = 51 and o2.value_coded=1306, 'NIVEL DE DETECÇÃO BAIXO',(IF(e.encounter_type in (6,9) and o2.value_coded=1306, 'NIVEL BAIXO DE DETECÇÃO',o2.value_coded)))) )) ) AS "
        + "                          viral_load "
        + "FROM   patient p "
        + "       INNER JOIN encounter e "
        + "               ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o "
        + "               ON e.encounter_id = o.encounter_id "
        + "       INNER JOIN obs o2 "
        + "               ON e.encounter_id = o2.encounter_id "
        + "       INNER JOIN ("
        + " SELECT result.patient_id, MAX(result.most_recent) as most_recent FROM ( "
        + getLastVlAndResultDateQuery()
        + "    ) result GROUP BY result.patient_id "
        + ") last_vl "
        + "               ON last_vl.patient_id = p.patient_id "
        + "WHERE  e.encounter_type IN( ${6}, ${9}, ${13}, ${51} ) "
        + "       AND ( ( o.concept_id = ${856} "
        + "               AND o.value_numeric IS NOT NULL ) "
        + "              OR ( o2.concept_id = ${1305} "
        + "                   AND o2.value_coded IS NOT NULL ) ) "
        + "       AND DATE(e.encounter_datetime) = last_vl.most_recent "
        + "       AND e.location_id = :location "
        + "       AND e.voided = 0 "
        + "       AND p.voided = 0 "
        + "       AND o.voided = 0 "
        + "       AND o2.voided = 0 "
        + "GROUP  BY p.patient_id";
  }

  private static String getLastVlAndResultDateQuery() {
    return "SELECT p.patient_id, "
        + "                          Max(o.obs_datetime) AS most_recent "
        + "                   FROM   patient p "
        + "                          INNER JOIN encounter e "
        + "                                  ON e.patient_id = p.patient_id "
        + "                          INNER JOIN obs o "
        + "                                  ON o.encounter_id = e.encounter_id "
        + "                   WHERE  e.encounter_type = ${53} "
        + "                          AND ( ( o.concept_id = ${856} "
        + "                                  AND o.value_numeric IS NOT NULL ) "
        + "                                 OR ( o.concept_id = ${1305} "
        + "                                      AND o.value_coded IS NOT NULL ) ) "
        + "                          AND o.obs_datetime <= :endDate "
        + "                          AND e.location_id = :location "
        + "                          AND e.voided = 0 "
        + "                          AND p.voided = 0 "
        + "                          AND o.voided = 0 "
        + "                   GROUP  BY p.patient_id "
        + "                   UNION "
        + "                   SELECT p.patient_id, "
        + "                          Max(DATE(e.encounter_datetime)) AS most_recent "
        + "                   FROM   patient p "
        + "                          INNER JOIN encounter e "
        + "                                  ON e.patient_id = p.patient_id "
        + "                          INNER JOIN obs o "
        + "                                  ON o.encounter_id = e.encounter_id "
        + "                   WHERE  e.encounter_type IN( ${6}, ${9}, ${13}, ${51} ) "
        + "                          AND ( ( o.concept_id = ${856} "
        + "                                  AND o.value_numeric IS NOT NULL ) "
        + "                                 OR ( o.concept_id = ${1305} "
        + "                                      AND o.value_coded IS NOT NULL ) ) "
        + "                          AND DATE(e.encounter_datetime) <= :endDate "
        + "                          AND e.location_id = :location "
        + "                          AND e.voided = 0 "
        + "                          AND p.voided = 0 "
        + "                          AND o.voided = 0 "
        + "                   GROUP  BY p.patient_id";
  }

  public String getSecondVLResultOrResultDateBeforeMostRecent() {
    return "SELECT p.patient_id, "
        + "       last_vl.last_vl AS second_vl, "
        + " IF(o.value_numeric IS NOT NULL, o.value_numeric, IF(o.value_coded = 165331, CONCAT('MENOR QUE ',o.comments), o.value_coded)) AS viral_load  "
        + "FROM   patient p "
        + "       INNER JOIN encounter e "
        + "               ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o "
        + "               ON e.encounter_id = o.encounter_id "
        + "       INNER JOIN ("
        + " SELECT result_date.patient_id, MAX(result_date.second_vl_date) as last_vl FROM ( "
        + getMostRecentVLBeforeLastVLDate()
        + " ) AS result_date GROUP BY result_date.patient_id "
        + " ) last_vl "
        + "               ON last_vl.patient_id = p.patient_id "
        + "WHERE  e.encounter_type = ${53} "
        + "       AND ( ( o.concept_id = ${856} "
        + "               AND o.value_numeric IS NOT NULL ) "
        + "              OR ( o.concept_id = ${1305} "
        + "                   AND o.value_coded IS NOT NULL ) ) "
        + "       AND o.obs_datetime = last_vl.last_vl "
        + "       AND e.location_id = :location "
        + "       AND e.voided = 0 "
        + "       AND p.voided = 0 "
        + "       AND o.voided = 0 "
        + "GROUP  BY p.patient_id "
        + "UNION "
        + "SELECT p.patient_id, "
        + "       last_vl.last_vl AS second_vl, "
        + "  IF(o.value_numeric IS NOT NULL, o.value_numeric, (IF(o.value_coded = 165331, CONCAT('MENOR QUE ',o.comments), (IF(e.encounter_type = 51 and o.value_coded=1306, 'NIVEL DE DETECÇÃO BAIXO',(IF(e.encounter_type in (6,9) and o.value_coded=1306, 'NIVEL BAIXO DE DETECÇÃO',o.value_coded)))) )) ) AS "
        + "                          viral_load "
        + "FROM   patient p "
        + "       INNER JOIN encounter e "
        + "               ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o "
        + "               ON e.encounter_id = o.encounter_id "
        + "       INNER JOIN ("
        + " SELECT result_date.patient_id, MAX(result_date.second_vl_date) as last_vl FROM ( "
        + getMostRecentVLBeforeLastVLDate()
        + " ) AS result_date GROUP BY result_date.patient_id "
        + ") last_vl "
        + "               ON last_vl.patient_id = p.patient_id "
        + "WHERE  e.encounter_type IN( ${6}, ${9}, ${13}, ${51} ) "
        + "       AND ( ( o.concept_id = ${856} "
        + "               AND o.value_numeric IS NOT NULL ) "
        + "              OR ( o.concept_id = ${1305} "
        + "                   AND o.value_coded IS NOT NULL ) ) "
        + "       AND DATE(e.encounter_datetime) = last_vl.last_vl "
        + "       AND e.location_id = :location "
        + "       AND e.voided = 0 "
        + "       AND p.voided = 0 "
        + "       AND o.voided = 0 "
        + "GROUP  BY p.patient_id";
  }

  public String getMostRecentVLBeforeLastVLDate() {
    return "SELECT p.patient_id, "
        + "       MAX(o.obs_datetime) AS second_vl_date "
        + "FROM   patient p "
        + "       INNER JOIN encounter e "
        + "               ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o "
        + "               ON e.encounter_id = o.encounter_id "
        + "       INNER JOIN ("
        + " SELECT result.patient_id, MAX(result.most_recent) as most_recent FROM ( "
        + getLastVlAndResultDateQuery()
        + "    ) result GROUP BY result.patient_id "
        + " ) last_vl "
        + "               ON last_vl.patient_id = p.patient_id "
        + "WHERE  e.encounter_type = ${53} "
        + "       AND ( ( o.concept_id = ${856} "
        + "               AND o.value_numeric IS NOT NULL ) "
        + "              OR ( o.concept_id = ${1305} "
        + "                   AND o.value_coded IS NOT NULL ) ) "
        + "       AND o.obs_datetime < last_vl.most_recent "
        + "       AND e.location_id = :location "
        + "       AND e.voided = 0 "
        + "       AND p.voided = 0 "
        + "       AND o.voided = 0 "
        + "GROUP  BY p.patient_id "
        + "UNION "
        + "SELECT p.patient_id, "
        + "       MAX(DATE(e.encounter_datetime)) AS second_vl_date "
        + "FROM   patient p "
        + "       INNER JOIN encounter e "
        + "               ON p.patient_id = e.patient_id "
        + "       INNER JOIN obs o "
        + "               ON e.encounter_id = o.encounter_id "
        + "       INNER JOIN ("
        + " SELECT result.patient_id, MAX(result.most_recent) as most_recent FROM ( "
        + getLastVlAndResultDateQuery()
        + "    ) result GROUP BY result.patient_id "
        + ") last_vl "
        + "               ON last_vl.patient_id = p.patient_id "
        + "WHERE  e.encounter_type IN( ${6}, ${9}, ${13}, ${51} ) "
        + "       AND ( ( o.concept_id = ${856} "
        + "               AND o.value_numeric IS NOT NULL ) "
        + "              OR ( o.concept_id = ${1305} "
        + "                   AND o.value_coded IS NOT NULL ) ) "
        + "       AND DATE(e.encounter_datetime) < last_vl.most_recent "
        + "       AND e.location_id = :location "
        + "       AND e.voided = 0 "
        + "       AND p.voided = 0 "
        + "       AND o.voided = 0 "
        + "GROUP  BY p.patient_id";
  }

  /**
   * <b>Abandonos em Tarv</b>
   *
   * @param stateOnProgram State on Program concept
   * @param stateOnEncounters State on encounter types concept
   * @param transferredOut transferred out flag to change the exclusion query
   * @param isForCohortDefinition flag to return result based on the definition (cohort or data
   *     definition)
   * @return {@link String}
   */
  public String getPatientsWhoSuspendedTarvOrAreTransferredOut(
      int stateOnProgram,
      int stateOnEncounters,
      boolean transferredOut,
      boolean isForCohortDefinition) {
    String query =
        isForCohortDefinition
            ? "  SELECT mostrecent.patient_id "
            : " SELECT mostrecent.patient_id, 'Suspenso' ";
    query +=
        "FROM ("
            + " SELECT lastest.patient_id ,Max(lastest.last_date) as  last_date "
            + " FROM (  "
            + "    SELECT p.patient_id , MAX(ps.start_date) AS last_date  "
            + "    FROM patient p   "
            + "        INNER JOIN patient_program pg   "
            + "            ON p.patient_id=pg.patient_id   "
            + "        INNER JOIN patient_state ps   "
            + "            ON pg.patient_program_id=ps.patient_program_id   "
            + "    WHERE pg.voided=0   "
            + "        AND ps.voided=0   "
            + "        AND p.voided=0   "
            + "        AND pg.program_id= ${2}  "
            + "        AND ps.state = "
            + stateOnProgram
            + "        AND ps.start_date <= :endDate   "
            + "        AND pg.location_id= :location   "
            + "         GROUP BY p.patient_id  "
            + "  "
            + "    UNION  "
            + "  "
            + "    SELECT  p.patient_id,  Max(e.encounter_datetime) AS last_date  "
            + "    FROM patient p    "
            + "        INNER JOIN encounter e   "
            + "            ON e.patient_id=p.patient_id   "
            + "        INNER JOIN obs o   "
            + "            ON o.encounter_id=e.encounter_id   "
            + "    WHERE  p.voided = 0   "
            + "        AND e.voided = 0   "
            + "        AND o.voided = 0   "
            + "        AND e.encounter_type = ${6}   "
            + "        AND o.concept_id = ${6273}  "
            + "        AND o.value_coded = "
            + stateOnEncounters
            + "        AND e.encounter_datetime <= :endDate   "
            + "        AND e.location_id =  :location   "
            + "         GROUP BY p.patient_id  "
            + "  "
            + "    UNION   "
            + "  "
            + "    SELECT  p.patient_id , Max(o.obs_datetime) AS last_date  "
            + "    FROM patient p    "
            + "        INNER JOIN encounter e   "
            + "            ON e.patient_id=p.patient_id   "
            + "        INNER JOIN obs o   "
            + "            ON o.encounter_id=e.encounter_id   "
            + "    WHERE  p.voided = 0   "
            + "        AND e.voided = 0   "
            + "        AND o.voided = 0   "
            + "        AND e.encounter_type = ${53}  "
            + "        AND o.concept_id = ${6272}  "
            + "        AND o.value_coded =  "
            + stateOnEncounters
            + "        AND o.obs_datetime <= :endDate   "
            + "        AND e.location_id =  :location  "
            + "         GROUP BY p.patient_id  "
            + ") lastest   "
            + " WHERE lastest.patient_id NOT IN( ";

    if (transferredOut) {
      query +=
          " SELECT p.patient_id  "
              + "      FROM   patient p  "
              + "             JOIN encounter e  "
              + "               ON p.patient_id = e.patient_id  "
              + "      WHERE  p.voided = 0  "
              + "             AND e.voided = 0  "
              + "             AND e.encounter_type = ${6}   "
              + "             AND e.location_id = :location  "
              + "             AND e.encounter_datetime > lastest.last_date  "
              + "             AND e.encounter_datetime <= :endDate  "
              + "                 UNION"
              + "  SELECT p.patient_id"
              + "      FROM   patient p"
              + "            JOIN encounter e ON p.patient_id = e.patient_id "
              + "            JOIN obs o ON e.encounter_id = o.encounter_id "
              + "     WHERE  p.voided = 0"
              + "            AND e.voided = 0 "
              + "            AND o.voided = 0 "
              + "        AND e.location_id =  :location  "
              + "            AND e.encounter_type = ${18}   "
              + "              AND e.encounter_datetime > lastest.last_date   "
              + "              AND e.encounter_datetime <= :endDate ";
    } else {
      query +=
          "  SELECT p.patient_id"
              + "      FROM   patient p"
              + "            JOIN encounter e ON p.patient_id = e.patient_id "
              + "            JOIN obs o ON e.encounter_id = o.encounter_id "
              + "     WHERE  p.voided = 0"
              + "            AND e.voided = 0 "
              + "            AND o.voided = 0 "
              + "        AND e.location_id =  :location  "
              + "            AND e.encounter_type = ${18}   "
              + "              AND  e.encounter_datetime > lastest.last_date   "
              + "              AND e.encounter_datetime <= :endDate ";
    }
    query +=
        " )  " + " GROUP BY lastest.patient_id )mostrecent " + " GROUP BY mostrecent.patient_id";
    return query;
  }

  /**
   * <b>Utentes em TARV que Abandonaram o TARV</b> Todos os utentes com a data mais recente entre
   * <li>A Data do Último Levantamento registada, até o fim do período de avaliação, na “Ficha
   *     Recepção/Levantou ARVs?” com “Levantou ARV” = “S”, adicionando 30 dias
   * <li>a Data do Último Agendamento de Levantamento registado no FILA até o fim do período de
   *     avaliação Esta data adicionando 60 dias é menor que a “Data Fim”;
   *
   *     <p>Excepto os utentes:
   * <li>Transferidos Para Outra US
   * <li>Suspensos em TARV
   * <li>Óbitos
   *
   * @see #getPatientsWhoSuspendedTarvOrAreTransferredOut(int, int, boolean, boolean)
   *     getPatientsWhoSuspendedTarvOrAreTransferredOut
   * @see #getPatientsWhoDied(boolean) getPatientsWhoDied
   * @return {@link String}
   */
  public String getPatientsWhoAbandonedTarvQuery(boolean isForDataDefinition) {
    String fromSQL =
        "FROM     ( "
            + "                  SELECT   most_recent.patient_id, "
            + "                           date_add(Max(most_recent.value_datetime), interval 60 day) final_encounter_date "
            + "                  FROM     ( "
            + "                                      SELECT     p.patient_id, "
            + "                                                 o.value_datetime "
            + "                                      FROM       patient p "
            + "                                      INNER JOIN encounter e "
            + "                                      ON         e.patient_id = p.patient_id "
            + "                                      INNER JOIN obs o "
            + "                                      ON         o.encounter_id = e.encounter_id "
            + "                                  INNER JOIN ( "
            + "                                      SELECT     p.patient_id, "
            + "                                                 max(e.encounter_datetime) last_encounter "
            + "                                      FROM       patient p "
            + "                                      INNER JOIN encounter e "
            + "                                      ON         e.patient_id = p.patient_id "
            + "                                      INNER JOIN obs o "
            + "                                      ON         o.encounter_id = e.encounter_id "
            + "                                      WHERE      p.voided = 0 "
            + "                                      AND        e.voided = 0 "
            + "                                      AND        o.voided = 0 "
            + "                                      AND        e.encounter_type = ${18} "
            + "                                      AND        e.encounter_datetime <= :endDate  "
            + "                                      AND        e.location_id = :location  "
            + "                                      GROUP BY   p.patient_id "
            + "                                    ) last_fila ON last_fila.patient_id = p.patient_id "
            + "                                      WHERE      p.voided = 0 "
            + "                                      AND        e.voided = 0 "
            + "                                      AND        o.voided = 0 "
            + "                                      AND        e.encounter_type = ${18} "
            + "                                      AND        o.concept_id = ${5096} "
            + "                                      AND        o.value_datetime IS NOT NULL "
            + "                                      AND        e.encounter_datetime = last_fila.last_encounter  "
            + "                                      AND        e.location_id = :location  "
            + "                                      GROUP BY   p.patient_id "
            + "                                      UNION "
            + "                                      SELECT     p.patient_id, "
            + "                                                 date_add(max(o2.value_datetime), interval 30 day) value_datetime "
            + "                                      FROM       patient p "
            + "                                      INNER JOIN encounter e "
            + "                                      ON         e.patient_id = p.patient_id "
            + "                                      INNER JOIN obs o "
            + "                                      ON         o.encounter_id = e.encounter_id "
            + "                                      INNER JOIN obs o2 "
            + "                                      ON         o2.encounter_id = e.encounter_id "
            + "                                      WHERE      p.voided = 0 "
            + "                                      AND        e.voided = 0 "
            + "                                      AND        o.voided = 0 "
            + "                                      AND        o2.voided = 0 "
            + "                                      AND        e.encounter_type = ${52} "
            + "                                      AND        ( "
            + "                                                            o.concept_id = ${23865} "
            + "                                                 AND        o.value_coded = ${1065}) "
            + "                                      AND        ( "
            + "                                                            o2.concept_id = ${23866} "
            + "                                                 AND        o2.value_datetime IS NOT NULL "
            + "                                                 AND        o2.value_datetime <= :endDate ) "
            + "                                      AND        e.location_id =  :location "
            + "                                      GROUP BY   p.patient_id) most_recent "
            + "                  GROUP BY most_recent.patient_id "
            + "                  HAVING   final_encounter_date <= :endDate ) final "
            + "WHERE    final.patient_id NOT IN ( "
            + new EptsQueriesUtil()
                .unionBuilder(
                    getPatientsWhoSuspendedTarvOrAreTransferredOut(
                        hivMetadata
                            .getTransferredOutToAnotherHealthFacilityWorkflowState()
                            .getProgramWorkflowStateId(),
                        hivMetadata.getTransferredOutConcept().getConceptId(),
                        true,
                        true))
                .union(
                    getPatientsWhoSuspendedTarvOrAreTransferredOut(
                        hivMetadata
                            .getSuspendedTreatmentWorkflowState()
                            .getProgramWorkflowStateId(),
                        hivMetadata.getSuspendedTreatmentConcept().getConceptId(),
                        false,
                        true))
                .union(getPatientsWhoDied(false))
                .buildQuery()
            + ") "
            + "GROUP BY final.patient_id";

    return isForDataDefinition
        ? "SELECT final.patient_id, 'Abandono' ".concat(fromSQL)
        : " SELECT final.patient_id ".concat(fromSQL);
  }

  /**
   * <b>Utentes em TARV com registo de Óbito</b>
   * <li>Utentes com registo de “Óbito” (último estado de inscrição) no programa SERVIÇO TARV
   *     TRATAMENTO até o fim do período de avaliação (“Data de Óbito” <= Data Fim”; ou
   * <li>Utentes com registo do último estado [“Mudança Estado Permanência TARV” (Coluna 21) = “O”
   *     (Óbito) na Ficha Clínica com “Data da Consulta Actual” (Coluna 1, durante a qual se fez o
   *     registo da mudança do estado de permanência TARV) <= “Data Fim”; ou
   * <li>Utentes com último registo de “Mudança Estado Permanência TARV” = “Óbito” na Ficha Resumo
   *     com “Data de Óbito” <= “Data Fim”; ou
   *
   * @return {@link String}
   */
  public String getPatientsWhoDied(boolean isForDataDefinition) {
    String fromSQL =
        "FROM ("
            + " SELECT lastest.patient_id ,Max(lastest.deceased_date) as  deceased_date "
            + " FROM (  "
            + "    "
            + "SELECT p.patient_id ,ps.start_date AS deceased_date  "
            + "    FROM patient p   "
            + "        INNER JOIN patient_program pg   "
            + "            ON p.patient_id=pg.patient_id   "
            + "        INNER JOIN patient_state ps   "
            + "            ON pg.patient_program_id=ps.patient_program_id   "
            + "    WHERE pg.voided=0   "
            + "        AND ps.voided=0   "
            + "        AND p.voided=0   "
            + "        AND pg.program_id= ${2}  "
            + "        AND ps.state = ${10} "
            + "        AND ps.start_date <= :endDate   "
            + "        AND pg.location_id= :location   "
            + "         GROUP BY p.patient_id  "
            + "  "
            + "    UNION  "
            + "  "
            + "    SELECT  p.patient_id,  Max(e.encounter_datetime) AS deceased_date  "
            + "    FROM patient p    "
            + "        INNER JOIN encounter e   "
            + "            ON e.patient_id=p.patient_id   "
            + "        INNER JOIN obs o   "
            + "            ON o.encounter_id=e.encounter_id   "
            + "    WHERE  p.voided = 0   "
            + "        AND e.voided = 0   "
            + "        AND o.voided = 0   "
            + "        AND e.encounter_type = ${6}   "
            + "        AND o.concept_id = ${6273}  "
            + "        AND o.value_coded = ${1366} "
            + "        AND e.encounter_datetime <= :endDate   "
            + "        AND e.location_id =  :location   "
            + "         GROUP BY p.patient_id  "
            + "  "
            + "    UNION   "
            + "  "
            + "    SELECT  p.patient_id , Max(o.obs_datetime) AS deceased_date  "
            + "    FROM patient p    "
            + "        INNER JOIN encounter e   "
            + "            ON e.patient_id=p.patient_id   "
            + "        INNER JOIN obs o   "
            + "            ON o.encounter_id=e.encounter_id   "
            + "    WHERE  p.voided = 0   "
            + "        AND e.voided = 0   "
            + "        AND o.voided = 0   "
            + "        AND e.encounter_type = ${53}  "
            + "        AND o.concept_id = ${6272}  "
            + "        AND o.value_coded = ${1366} "
            + "        AND o.obs_datetime <= :endDate   "
            + "        AND e.location_id =  :location  "
            + "         GROUP BY p.patient_id  "
            + " UNION "
            + " SELECT p.person_id, p.death_date AS deceased_date "
            + "                FROM   person p "
            + "                WHERE  p.voided = 0"
            + "                   AND p.dead = 1 "
            + "                   AND p.death_date <= :endDate "
            + ") lastest   "
            + " WHERE lastest.patient_id NOT IN( "
            + " SELECT p.patient_id  "
            + "      FROM   patient p  "
            + "             JOIN encounter e  "
            + "               ON p.patient_id = e.patient_id  "
            + "      WHERE  p.voided = 0  "
            + "             AND e.voided = 0  "
            + "             AND e.encounter_type = ${6}   "
            + "             AND e.location_id = :location  "
            + "             AND e.encounter_datetime > lastest.deceased_date  "
            + "             AND e.encounter_datetime <= :endDate  "
            + "                 UNION"
            + "  SELECT p.patient_id"
            + "      FROM   patient p"
            + "            JOIN encounter e ON p.patient_id = e.patient_id "
            + "            JOIN obs o ON e.encounter_id = o.encounter_id "
            + "     WHERE  p.voided = 0"
            + "            AND e.voided = 0 "
            + "            AND o.voided = 0 "
            + "            AND e.encounter_type = ${18}   "
            + "              AND e.encounter_datetime > lastest.deceased_date"
            + "              AND e.encounter_datetime <= :endDate"
            + " )  "
            + " GROUP BY lastest.patient_id )mostrecent "
            + " GROUP BY mostrecent.patient_id";

    return isForDataDefinition
        ? "  SELECT mostrecent.patient_id, 'Óbito'  ".concat(fromSQL)
        : " SELECT mostrecent.patient_id ".concat(fromSQL);
  }

  /**
   * <b>Utentes Activos em TARV</b>
   * <li>Iniciaram TARV até o fim do período de avaliação, ou seja, com registo do Início TARV
   *     Excluindo todos os utentes:
   * <li>Abandonos em TARV
   * <li>Transferidos Para Outra US
   * <li>Suspensos em TARV
   * <li>Óbitos
   *
   * @see #getPatientsWhoAbandonedTarvQuery(boolean) getPatientsWhoAbandonedTarvQuery
   * @see #getPatientsWhoSuspendedTarvOrAreTransferredOut(int, int, boolean, boolean)
   *     getPatientsWhoSuspendedTarvOrAreTransferredOut
   * @see #getPatientsWhoDied(boolean) getPatientsWhoDied
   * @return {@link String}
   */
  public String getPatientsActiveOnTarv() {
    return "SELECT  final.patient_id, 'Activo' "
        + "FROM "
        + "    ( "
        + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
        + " ) final "
        + "WHERE final.patient_id NOT IN ("
        + new EptsQueriesUtil()
            .unionBuilder(getPatientsWhoAbandonedTarvQuery(false))
            .union(
                getPatientsWhoSuspendedTarvOrAreTransferredOut(
                    hivMetadata.getSuspendedTreatmentWorkflowState().getProgramWorkflowStateId(),
                    hivMetadata.getSuspendedTreatmentConcept().getConceptId(),
                    true,
                    true))
            .union(
                getPatientsWhoSuspendedTarvOrAreTransferredOut(
                    hivMetadata.getSuspendedTreatmentWorkflowState().getProgramWorkflowStateId(),
                    hivMetadata.getSuspendedTreatmentConcept().getConceptId(),
                    false,
                    true))
            .union(getPatientsWhoDied(false))
            .buildQuery()
        + "     ) "
        + "GROUP BY final.patient_id ";
  }

  /**
   *
   * <li>Caso exista registo de infecção representativa de Estadio IV (DAH_RF28) registada na Ficha
   *     Clinica – Ficha Mestra ocorrido até o fim do período de avaliação, o sistema irá considerar
   *     como “Data de Registo” a data do primeiro registo de infecção representativa de Estadio IV
   *     até o fim do período de avaliação. Caso contrário, ou seja, caso não exista registo de
   *     infecção representativa de Estadio IV, mas exista registo de infecção representativa de
   *     Estadio III (DAH_RF27) até o fim do período de avaliação, o sistema irá considerar como
   *     “Data de Registo” a data do primeiro registo de infecção representativa de Estadio III até
   *     o fim do período de avaliação
   *
   * @return {@link String}
   */
  public String getFirstEstadioQuery() {
    return "    SELECT p.patient_id,  "
        + "                        Min(e.encounter_datetime) AS consultation_date "
        + "                             FROM   patient p "
        + "                                    INNER JOIN encounter e "
        + "                                            ON p.patient_id = e.patient_id "
        + "                                    INNER JOIN obs o "
        + "                                            ON e.encounter_id = o.encounter_id "
        + "                             WHERE  p.voided = 0 "
        + "                                    AND e.voided = 0 "
        + "                                    AND o.voided = 0 "
        + "                                    AND e.encounter_type = ${6}  "
        + "                                    AND o.concept_id = ${1406} "
        + "                                    AND o.value_coded IN (${3},${42},${43},${60},${126},${507},${1294}, "
        + "                                         ${1570},${5018},${5042},${5334},${5344},${5340},${5945},${6783},${6990},${7180},${14656}) "
        + "                                    AND e.encounter_datetime <= :endDate "
        + "                                    AND e.location_id = :location "
        + "                             GROUP  BY p.patient_id ";
  }

  /**
   * @see #getFirstEstadioQuery()
   * @return {@link String}
   */
  public String getFirstEstadioQueryWithEncounterId() {
    return "    SELECT p.patient_id, e.encounter_id, e.encounter_datetime AS consultation_date "
        + "                             FROM   patient p "
        + "                                    INNER JOIN encounter e "
        + "                                            ON p.patient_id = e.patient_id "
        + "                                    INNER JOIN obs o "
        + "                                            ON e.encounter_id = o.encounter_id "
        + "                                    INNER JOIN ( "
        + "                         SELECT p.patient_id,  "
        + "                        Min(e.encounter_datetime) AS consultation_date "
        + "                             FROM   patient p "
        + "                                    INNER JOIN encounter e "
        + "                                            ON p.patient_id = e.patient_id "
        + "                                    INNER JOIN obs o "
        + "                                            ON e.encounter_id = o.encounter_id "
        + "                             WHERE  p.voided = 0 "
        + "                                    AND e.voided = 0 "
        + "                                    AND o.voided = 0 "
        + "                                    AND e.encounter_type = ${6}  "
        + "                                    AND o.concept_id = ${1406} "
        + "                                    AND o.value_coded IN (${3},${42},${43},${60},${126},${507},${1294}, "
        + "                                       ${1570},${5018},${5042},${5334},${5344},${5340},${5945},${6783},${6990},${7180},${14656}) "
        + "                                    AND e.encounter_datetime <= :endDate "
        + "                                    AND e.location_id = :location "
        + "                             GROUP  BY p.patient_id "
        + "                                    ) first_consultation on first_consultation.patient_id = p.patient_id "
        + "                             WHERE  p.voided = 0 "
        + "                                    AND e.voided = 0 "
        + "                                    AND o.voided = 0 "
        + "                                    AND e.encounter_type = ${6}  "
        + "                                    AND o.concept_id = ${1406} "
        + "                                    AND o.value_coded IN (${3},${42},${43},${60},${126},${507},${1294}, "
        + "                                                     ${1570},${5018},${5042},${5334},${5344},${5340},${5945},${6783},${6990},${7180},${14656}) "
        + "                                    AND e.encounter_datetime = first_consultation.consultation_date "
        + "                                    AND e.location_id = :location "
        + "                             GROUP  BY p.patient_id ";
  }

  public String getLastCd4ResultDateQueries() {
    String unionQuery =
        new EptsQueriesUtil()
            .unionBuilder(getCd4ResultOverOrEqualTo5years())
            .union(getCd4ResultBetweenOneAnd5years())
            .union(getCd4ResultBellowOneYear())
            .buildQuery();

    return " SELECT absolute_cd4.person_id, max(absolute_cd4.most_recent) AS most_recent FROM ( "
        + unionQuery
        + " ) absolute_cd4 GROUP BY absolute_cd4.person_id ";
  }
}
