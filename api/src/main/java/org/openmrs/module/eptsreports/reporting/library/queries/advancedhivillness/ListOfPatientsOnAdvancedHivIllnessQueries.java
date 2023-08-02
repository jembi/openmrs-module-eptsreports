package org.openmrs.module.eptsreports.reporting.library.queries.advancedhivillness;

public class ListOfPatientsOnAdvancedHivIllnessQueries {

  /**
   *
   * <li>Utentes com registo do resultado de CD4 (absoluto) na “Ficha Clínica – Ficha Mestra” ou
   *     “Ficha e-Lab” ou “Ficha de Laboratório” ou “Ficha de Doença Avançada por HIV”, durante o
   *     período de avaliação (“Data Resultado CD4” >= “Data Início Avaliação” e “Data Resultado
   *     CD4” <= “Data Fim Avaliação”) e com resultado CD4 (absoluto) < <b>valueNumeric</b>
   *
   * @param valueNumeric Max value of CD4 result
   * @param mostRecentDate Flag to return Most Recent date or Cd4 Result
   * @return {@link String}
   */
  public static String getPatientsWithCD4AbsoluteResultOnPeriodQuery(
      int valueNumeric, boolean mostRecentDate) {

    String fromSQL =
        " FROM   person ps "
            + "       INNER JOIN encounter e "
            + "               ON ps.person_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + "WHERE  ps.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND ( ( ( e.encounter_type IN ( ${6}, ${13}, ${51} ) "
            + "                 AND o.concept_id = ${1695} ) "
            + "                OR ( e.encounter_type = ${90} "
            + "                     AND o.concept_id = ${165389} "
            + "                     AND o.value_coded = ${1695} ) ) "
            + "             AND o.value_numeric < "
            + valueNumeric
            + "             AND e.encounter_datetime >= :startDate "
            + "             AND e.encounter_datetime <= :endDate ) "
            + "       AND e.location_id = :location";

    return mostRecentDate
        ? " SELECT ps.person_id, Max(e.encounter_datetime) AS most_recent ".concat(fromSQL)
        : " SELECT ps.person_id, o.value_numeric AS cd4_result ".concat(fromSQL);
  }

  /**
   *
   * <li>Utentes com registo do resultado de CD4 (absoluto) na “Ficha Resumo – Ficha Mestra” durante
   *     o período de avaliação (“Data Resultado CD4” >= “Data Início Avaliação” e “Data Resultado
   *     CD4” <= “Data Fim Avaliação”) e com resultado CD4 (absoluto) < <b>valueNumeric</b>
   *
   * @param valueNumeric Max value of CD4 result
   * @param mostRecentDate Flag to return Most Recent date or Cd4 Result
   * @return {@link String}
   */
  public static String getPatientsWithCD4AbsoluteResultFichaResumoOnPeriodQuery(
      int valueNumeric, boolean mostRecentDate) {

    String fromSQL =
        " FROM "
            + "    person ps INNER JOIN encounter e ON ps.person_id= e.patient_id "
            + "              INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "WHERE ps.voided = 0 AND e.voided = 0 AND o.voided = 0 "
            + "  AND e.encounter_type = ${53} "
            + "  AND o.concept_id = ${1695} "
            + "  AND o.value_numeric < "
            + valueNumeric
            + "  and o.obs_datetime >= :startDate "
            + "  AND o.obs_datetime <= :endDate "
            + "  AND e.location_id = :location";

    return mostRecentDate
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
  public static String getCd4ResultOverOrEqualTo5years() {
    return ListOfPatientsOnAdvancedHivIllnessQueries.getPatientsWithCD4AbsoluteResultOnPeriodQuery(
            200, true)
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) >=5 "
        + "  GROUP BY ps.person_id "
        + " UNION "
        + ListOfPatientsOnAdvancedHivIllnessQueries
            .getPatientsWithCD4AbsoluteResultFichaResumoOnPeriodQuery(200, true)
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
  public static String getCd4ResultBetweenOneAnd5years() {
    return ListOfPatientsOnAdvancedHivIllnessQueries.getPatientsWithCD4AbsoluteResultOnPeriodQuery(
            500, true)
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) >=1 "
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) <5 "
        + "  GROUP BY ps.person_id "
        + " UNION "
        + ListOfPatientsOnAdvancedHivIllnessQueries
            .getPatientsWithCD4AbsoluteResultFichaResumoOnPeriodQuery(500, true)
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
  public static String getCd4ResultBellowOneYear() {
    return ListOfPatientsOnAdvancedHivIllnessQueries.getPatientsWithCD4AbsoluteResultOnPeriodQuery(
            750, true)
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) <1 "
        + "  GROUP BY ps.person_id "
        + " UNION "
        + ListOfPatientsOnAdvancedHivIllnessQueries
            .getPatientsWithCD4AbsoluteResultFichaResumoOnPeriodQuery(750, true)
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) <1 "
        + "  GROUP BY ps.person_id ";
  }

  /**
   * @see #getCd4ResultOverOrEqualTo5years()
   * @return {@link String}
   */
  public static String getCd4ResultOverOrEqualTo5y() {
    return ListOfPatientsOnAdvancedHivIllnessQueries.getPatientsWithCD4AbsoluteResultOnPeriodQuery(
            200, false)
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) >=5 "
        + "  GROUP BY ps.person_id "
        + " UNION "
        + ListOfPatientsOnAdvancedHivIllnessQueries
            .getPatientsWithCD4AbsoluteResultFichaResumoOnPeriodQuery(200, false)
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) >=5 "
        + "  GROUP BY ps.person_id ";
  }

  /**
   * @see #getCd4ResultBetweenOneAnd5years()
   * @return {@link String}
   */
  public static String getCd4ResultBetweenOneAnd5y() {
    return ListOfPatientsOnAdvancedHivIllnessQueries.getPatientsWithCD4AbsoluteResultOnPeriodQuery(
            500, false)
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) >=1 "
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) <5 "
        + "  GROUP BY ps.person_id "
        + " UNION "
        + ListOfPatientsOnAdvancedHivIllnessQueries
            .getPatientsWithCD4AbsoluteResultFichaResumoOnPeriodQuery(500, false)
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) >=1 "
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) <5 "
        + "  GROUP BY ps.person_id ";
  }

  /**
   * @see #getCd4ResultBellowOneYear()
   * @return {@link String}
   */
  public static String getCd4ResultBellow1y() {
    return ListOfPatientsOnAdvancedHivIllnessQueries.getPatientsWithCD4AbsoluteResultOnPeriodQuery(
            750, false)
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) <1 "
        + "  GROUP BY ps.person_id "
        + " UNION "
        + ListOfPatientsOnAdvancedHivIllnessQueries
            .getPatientsWithCD4AbsoluteResultFichaResumoOnPeriodQuery(750, false)
        + " AND TIMESTAMPDIFF(YEAR, ps.birthdate, :endDate) <1 "
        + "  GROUP BY ps.person_id ";
  }

  /**
   *
   * <li>Utentes com registo do resultado de CD4 (absoluto) na “Ficha Clínica – Ficha Mestra” ou
   *     “Ficha e-Lab” ou “Ficha de Laboratório” ou “Ficha de Doença Avançada por HIV”, durante o
   *     período de avaliação (“Data Resultado CD4” >= “Data Início Avaliação” e “Data Resultado
   *     CD4” <= “Data Fim Avaliação”)
   *
   * @param mostRecentDate Flag to return Most Recent date or Cd4 Result
   * @return {@link String}
   */
  public static String getPatientsWithCD4AbsoluteResultOnPeriodQuery(boolean mostRecentDate) {

    String fromSQL =
        " FROM   person ps "
            + "       INNER JOIN encounter e "
            + "               ON ps.person_id = e.patient_id "
            + "       INNER JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + "WHERE  ps.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND ( ( ( e.encounter_type IN ( ${6}, ${13}, ${51} ) "
            + "                 AND o.concept_id = ${1695} ) "
            + "                OR ( e.encounter_type = ${90} "
            + "                     AND o.concept_id = ${165389} "
            + "                     AND o.value_coded = ${1695} ) ) "
            + "             AND o.value_numeric IS NOT NULL "
            + "             AND e.encounter_datetime >= :startDate "
            + "             AND e.encounter_datetime <= :endDate ) "
            + "       AND e.location_id = :location "
            + " GROUP BY ps.person_id ";

    return mostRecentDate
        ? " SELECT ps.person_id, Max(e.encounter_datetime) AS most_recent ".concat(fromSQL)
        : " SELECT ps.person_id, o.value_numeric AS cd4_result ".concat(fromSQL);
  }

  /**
   *
   * <li>Utentes com registo do resultado de CD4 (absoluto) na “Ficha Resumo – Ficha Mestra” durante
   *     o período de avaliação (“Data Resultado CD4” >= “Data Início Avaliação” e “Data Resultado
   *     CD4” <= “Data Fim Avaliação”)
   *
   * @param mostRecentDate Flag to return Most Recent date or Cd4 Result
   * @return {@link String}
   */
  public static String getPatientsWithCD4AbsoluteResultFichaResumoOnPeriodQuery(
      boolean mostRecentDate) {

    String fromSQL =
        " FROM "
            + "    person ps INNER JOIN encounter e ON ps.person_id= e.patient_id "
            + "              INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "WHERE ps.voided = 0 AND e.voided = 0 AND o.voided = 0 "
            + "  AND e.encounter_type = ${53} "
            + "  AND o.concept_id = ${1695} "
            + "  AND o.value_numeric IS NOT NULL "
            + "  and o.obs_datetime >= :startDate "
            + "  AND o.obs_datetime <= :endDate "
            + "  AND e.location_id = :location "
            + " GROUP BY ps.person_id";

    return mostRecentDate
        ? " SELECT ps.person_id, max(o.obs_datetime) as most_recent ".concat(fromSQL)
        : " SELECT ps.person_id, o.value_numeric AS cd4_result ".concat(fromSQL);
  }

  public static String getLastCd4OrResultDateBeforeMostRecentCd4() {
    return " SELECT ps.person_id, o.value_numeric, MAX(e.encounter_datetime) AS second_cd4_result "
        + " FROM   person ps "
        + "       INNER JOIN encounter e "
        + "               ON ps.person_id = e.patient_id "
        + "       INNER JOIN obs o "
        + "               ON e.encounter_id = o.encounter_id "
        + " INNER JOIN ( "
        + ListOfPatientsOnAdvancedHivIllnessQueries.getPatientsWithCD4AbsoluteResultOnPeriodQuery(
            true)
        + " ) last_cd4 ON last_cd4.person_id = ps.person_id "
        + "WHERE  ps.voided = 0 "
        + "       AND e.voided = 0 "
        + "       AND o.voided = 0 "
        + "       AND ( ( ( e.encounter_type IN ( ${6}, ${13}, ${51} ) "
        + "                 AND o.concept_id = ${1695} ) "
        + "                OR ( e.encounter_type = ${90} "
        + "                     AND o.concept_id = ${165389} "
        + "                     AND o.value_coded = ${1695} ) ) "
        + "             AND o.value_numeric IS NOT NULL "
        + "             AND e.encounter_datetime < last_cd4.most_recent ) "
        + "       AND e.location_id = :location"
        + "       GROUP BY ps.person_id "
        + " UNION "
        + " SELECT ps.person_id, o.value_numeric, MAX(o.obs_datetime) AS cd4_result "
        + " FROM "
        + "    person ps INNER JOIN encounter e ON ps.person_id= e.patient_id "
        + "              INNER JOIN obs o on e.encounter_id = o.encounter_id "
        + " INNER JOIN ( "
        + ListOfPatientsOnAdvancedHivIllnessQueries
            .getPatientsWithCD4AbsoluteResultFichaResumoOnPeriodQuery(true)
        + " ) last_cd4 ON last_cd4.person_id = ps.person_id "
        + "WHERE ps.voided = 0 AND e.voided = 0 AND o.voided = 0 "
        + "  AND e.encounter_type = ${53} "
        + "  AND o.concept_id = ${1695} "
        + "  AND o.value_numeric IS NOT NULL "
        + "  AND o.obs_datetime < last_cd4.most_recent "
        + "  AND e.location_id = :location "
        + "       GROUP BY ps.person_id ";
  }

  public static String getVLoadResultAndMostRecent() {
    return " SELECT p.patient_id, o.value_numeric AS viral_load, MAX(o.obs_datetime) AS most_recent FROM patient p "
        + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
        + "         INNER JOIN obs o ON o.encounter_id = e.encounter_id "
        + " WHERE e.encounter_type = ${53} "
        + " AND (o.concept_id = ${856} AND o.value_numeric IS NOT NULL) "
        + " AND o.obs_datetime <= :endDate "
        + " AND e.location_id = :location "
        + " AND e.voided = 0 "
        + " AND p.voided = 0 "
        + " AND o.voided = 0 "
        + " GROUP BY p.patient_id "
        + " UNION "
        + " SELECT p.patient_id, o.value_numeric AS viral_load, MAX(e.encounter_datetime) AS most_recent FROM patient p "
        + " INNER JOIN encounter e ON e.patient_id = p.patient_id "
        + "         INNER JOIN obs o ON o.encounter_id = e.encounter_id "
        + " WHERE e.encounter_type IN(${6},${9},${13},${51}) "
        + " AND (o.concept_id = ${856} AND o.value_numeric IS NOT NULL) "
        + " AND e.encounter_datetime <= :endDate "
        + " AND e.location_id = :location "
        + " AND e.voided = 0 "
        + " AND p.voided = 0 "
        + " AND o.voided = 0 "
        + " GROUP BY p.patient_id ";
  }
}
