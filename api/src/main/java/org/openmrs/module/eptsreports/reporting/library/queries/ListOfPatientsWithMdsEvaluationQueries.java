package org.openmrs.module.eptsreports.reporting.library.queries;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.eptsreports.reporting.library.cohorts.GenericCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ResumoMensalCohortQueries;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsWithMdsEvaluationQueries {
  private static HivMetadata hivMetadata = new HivMetadata();
  private static TbMetadata tbMetadata = new TbMetadata();
  private static CommonMetadata commonMetadata = new CommonMetadata();

  private static final ResumoMensalCohortQueries resumoMensalCohortQueries =
      new ResumoMensalCohortQueries(
          new HivMetadata(), new TbMetadata(), new GenericCohortQueries());

  /**
   * O sistema irá determinar a Data Início TARV do utente da seguinte forma:
   *
   * <ul>
   *   <li>seleccionando, a data mais antiga entre: o 1º levantamento de ARVs registado no FILA
   *       (“Data de Levantamento”) e <br>
   *   <li>o 1º levantamento registado na “Ficha Recepção/ Levantou ARVs?” com “Levantou ARV” = Sim
   *       (“Data de Levantamento”) <br>
   *       <p>sendo a data mais antiga dos critérios acima<= “Data Fim Inclusão”. <br>
   *       <p>Data Fim = “20 de Junho” de “Ano de Avaliação” Nota 1: Deve-se confirmar que a data
   *       início TARV é realmente a primeira ocorrência (data mais antiga) até a “Data Fim”
   *       Inclusão. Isto irá prevenir situações em que utentes que, por algum motivo, possam ter
   *       mais do que uma data de início TARV registado no sistema. <br>
   * </ul>
   *
   * @return {String}
   */
  public static String getPatientArtStart(String inclusionEndMonthAndDay) {

    String query =
        " SELECT first.patient_id, MIN(first.pickup_date) first_pickup "
            + "       FROM ( "
            + " SELECT p.patient_id, "
            + "       Min(e.encounter_datetime) pickup_date "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "WHERE p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND e.encounter_type = ${18} "
            + "       AND e.location_id = :location "
            + "            AND e.encounter_datetime <= CONCAT(:evaluationYear, "
            + inclusionEndMonthAndDay
            + "        ) "
            + "GROUP  BY p.patient_id "
            + "UNION "
            + "SELECT p.patient_id, "
            + "       Min(o2.value_datetime) pickup_date "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "       INNER JOIN obs o2 "
            + "               ON o2.encounter_id = e.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND o2.voided = 0 "
            + "            AND e.encounter_type = ${52} "
            + "            AND e.location_id = :location "
            + "            AND o.concept_id = ${23865} "
            + "            AND o.value_coded = ${1065} "
            + "            AND o2.concept_id = ${23866} "
            + "            AND o2.value_datetime <= CONCAT(:evaluationYear, "
            + inclusionEndMonthAndDay
            + "        ) "
            + "GROUP  BY p.patient_id "
            + "        ) first "
            + "     GROUP BY first.patient_id ";

    Map<String, Integer> map = new HashMap<>();
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    return stringSubstitutor.replace(query);
  }

  /**
   * Excluindo :
   *
   * <ul>
   *   <li>Todos os utentes Transferidos De Outra US (RF7)
   *   <li>Todos os utentes Transferidos Para outra US (RF8)
   * </ul>
   *
   * <br>
   * <b>Utentes Transferidos de Outra US</b><br>
   * O sistema irá identificar todos os utentes “Transferidos de” outras US em TARV selecionando os
   * utentes registados como:
   *
   * <ul>
   *   <li>“Transferido de outra US”, na “Ficha de Resumo”, com opção “TARV” selecionada;
   * </ul>
   *
   * <b>Nota:</b> não há verificação de data de transferência para identificação dos utentes
   * “Transferido de outra US”<br>
   * <br>
   * <b>Utentes Transferidos Para</b><br>
   * O sistema irá identificar os utentes Transferido para até o fim do período de avaliação, da
   * seguinte forma:
   *
   * <ul>
   *   <li>utentes inscritos como “Transferido para” (último estado de inscrição) no programa
   *       SERVIÇO TARV TRATAMENTO com “Data de Transferência” <= “Data Fim Avaliação; ou
   *   <li>utentes com registo do último estado [“Mudança Estado Permanência TARV” (Coluna 21) = “T”
   *       (Transferido Para) na Ficha Clínica com “Data da Consulta Actual” (Coluna 1, durante a
   *       qual se fez o registo da mudança do estado de permanência TARV) <= “Data Fim Avaliação;
   *       ou
   *   <li>utentes com último registo de “Mudança Estado Permanência TARV” = “Transferido Para” na
   *       Ficha Resumo com “Data da Transferência” <= “Data Fim Avaliação;
   * </ul>
   *
   * <br>
   * <b>excepto os utentes que tenham tido um levantamento de ARV (FILA) após a “Data de
   * Transferência” (a data mais recente entre os critérios acima identificados) e até “Data Fim
   * Avaliação
   *
   * <p>excepto os utentes que tenham a data mais recente entre:
   *
   * <ul>
   *   <li>a “Data Próximo Levantamento” +1 dia, registado no último FILA até ao fim do período de
   *       avaliação e
   *   <li>a última “Data de Levantamento” registada até o fim do período de avaliação na Ficha
   *       Recepção/Levantou ARV, adicionando 31 dias.
   *       <p>e sendo essa data posterior ao fim do período de avaliação. Ou seja, o utente só será
   *       considerado como Transferido Para outra US depois de terminar a sua última medicação.
   * </ul>
   *
   * </b><br>
   * <b>Nota: </b> Sendo a “Data Fim de Inclusão” definida da seguinte forma:
   *
   * <ul>
   *   <li>Coluna “Estado de Permanência no 12º Mês” (RF30)
   *       <ul>
   *         <li>Data Fim Inclusão = “20 de Junho” de “Ano de Avaliação” menos (-) 1 ano
   *       </ul>
   *   <li>Coluna “Estado de Permanência no 24º Mês” (RF45)
   *       <ul>
   *         <li>Data Fim Inclusão = “20 de Junho” de “Ano de Avaliação” menos (-) 2 anos
   *       </ul>
   *   <li>Coluna “Estado de Permanência no 36º Mês” (RF60) *
   *       <ul>
   *         *
   *         <li>Data Fim Inclusão = “20 de Junho” de “Ano de Avaliação” menos (-) 3 anos *
   *       </ul>
   *   <li>Exclusão do Relatório (RF4) *
   *       <ul>
   *         *
   *         <li>Data Fim Inclusão = “20 de Junho” de “Ano de Avaliação” *
   *       </ul>
   * </ul>
   *
   * @param inclusionEndMonthAndDay = '-06-20'
   * @return String
   */
  public static String getTranferredPatients(String inclusionEndMonthAndDay, int coortendYear) {

    String query =
        "SELECT p.patient_id "
            + "		        FROM   patient p "
            + "		                INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "		                INNER JOIN obs o ON e.encounter_id = o.encounter_id "
            + "		         WHERE  p.voided = 0 "
            + "		         AND e.voided = 0 "
            + "		         AND o.voided = 0 "
            + "		         AND e.location_id = :location "
            + "		         AND e.encounter_type = ${53} "
            + "		         AND o.concept_id = ${1369} AND o.value_coded = ${1065} "
            + " UNION "
            + "SELECT transferred_out.patient_id "
            + "FROM   (SELECT latest.patient_id, "
            + "               Max(latest.last_date) AS last_date "
            + "        FROM   (SELECT p.patient_id, "
            + "                       laststate.last_date AS last_date "
            + "                FROM   patient p "
            + "                           INNER JOIN patient_program pg "
            + "                                      ON p.patient_id = pg.patient_id "
            + "                           INNER JOIN patient_state ps "
            + "                                      ON pg.patient_program_id = ps.patient_program_id "
            + "                           INNER JOIN (SELECT p.patient_id, "
            + "                                              Max(ps.start_date) AS last_date "
            + "                                       FROM   patient p "
            + "                                                  INNER JOIN patient_program pg "
            + "                                                             ON "
            + "                                                                 p.patient_id = pg.patient_id "
            + "                                                  INNER JOIN patient_state ps "
            + "                                                             ON "
            + "                                                                 pg.patient_program_id = ps.patient_program_id "
            + "                                       WHERE  pg.voided = 0 "
            + "                                         AND ps.voided = 0 "
            + "                                         AND p.voided = 0 "
            + "                                         AND pg.program_id = ${2} "
            + "                                         AND ps.state IS NOT NULL "
            + "                                         AND ps.start_date <= DATE_SUB(CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ), INTERVAL "
            + coortendYear
            + " YEAR) "
            + "                                         AND pg.location_id = :location "
            + "                                       GROUP  BY p.patient_id) laststate "
            + "                                      ON laststate.patient_id = p.patient_id "
            + "                WHERE  pg.voided = 0 "
            + "                  AND ps.voided = 0 "
            + "                  AND p.voided = 0 "
            + "                  AND pg.program_id = ${2} "
            + "                  AND ps.state = ${7} "
            + "                  AND ps.start_date = laststate.last_date "
            + "                  AND pg.location_id = :location "
            + "                GROUP  BY p.patient_id "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       Max(o.obs_datetime) AS last_date "
            + "                FROM   patient p "
            + "                           INNER JOIN encounter e "
            + "                                      ON e.patient_id = p.patient_id "
            + "                           INNER JOIN obs o "
            + "                                      ON e.encounter_id = o.encounter_id "
            + "                WHERE  p.voided = 0 "
            + "                  AND e.voided = 0 "
            + "                  AND o.voided = 0 "
            + "                  AND e.encounter_type = ${53} "
            + "                  AND o.concept_id = ${6272} "
            + "                  AND o.value_coded = ${1706} "
            + "                  AND o.obs_datetime <= DATE_SUB(CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ), INTERVAL "
            + coortendYear
            + " YEAR) "
            + "                  AND e.location_id = :location "
            + "                GROUP  BY p.patient_id "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       max_seg.last_date AS last_date "
            + "                FROM   patient p "
            + "                           INNER JOIN encounter e "
            + "                                      ON e.patient_id = p.patient_id "
            + "                           INNER JOIN obs o "
            + "                                      ON o.encounter_id = e.encounter_id "
            + "                           INNER JOIN (SELECT p.patient_id, "
            + "                                              Max(e.encounter_datetime) AS last_date "
            + "                                       FROM   patient p "
            + "                                                  INNER JOIN encounter e "
            + "                                                             ON e.patient_id = p.patient_id "
            + "                                                  INNER JOIN obs o "
            + "                                                             ON o.encounter_id = e.encounter_id "
            + "                                       WHERE  p.voided = 0 "
            + "                                         AND e.voided = 0 "
            + "                                         AND o.voided = 0 "
            + "                                         AND e.encounter_type = ${6} "
            + "                                         AND e.encounter_datetime <= DATE_SUB(CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ), INTERVAL "
            + coortendYear
            + " YEAR) "
            + "                                         AND e.location_id = :location "
            + "                                       GROUP  BY p.patient_id) max_seg "
            + "                                      ON max_seg.patient_id = p.patient_id "
            + "                WHERE  p.voided = 0 "
            + "                  AND e.voided = 0 "
            + "                  AND o.voided = 0 "
            + "                  AND e.encounter_type = ${6} "
            + "                  AND o.concept_id = ${6273} "
            + "                  AND o.value_coded = ${1706} "
            + "                  AND e.encounter_datetime = max_seg.last_date "
            + "                  AND e.location_id = :location "
            + "                GROUP  BY p.patient_id) latest "
            + "        GROUP  BY latest.patient_id) transferred_out "
            + "WHERE  transferred_out.patient_id NOT IN (SELECT p.patient_id "
            + "                                          FROM   patient p "
            + "                                                     INNER JOIN encounter e "
            + "                                                                ON "
            + "                                                                    e.patient_id = p.patient_id "
            + "                                          WHERE  p.voided = 0 "
            + "                                            AND e.voided = 0 "
            + "                                            AND p.patient_id = "
            + "                                                transferred_out.patient_id "
            + "                                            AND e.encounter_type = ${18} "
            + "                                            AND "
            + "                                              e.encounter_datetime > transferred_out.last_date "
            + "                                            AND "
            + "                                              e.encounter_datetime <= DATE_SUB(CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ), INTERVAL "
            + coortendYear
            + " YEAR) "
            + "                                            AND e.location_id = :location "
            + "                                          UNION "
            + "                                          SELECT p.patient_id "
            + "                                          FROM   patient p "
            + "                                                     INNER JOIN (SELECT "
            + "                                                                     last_next_pick_up.patient_id, "
            + "                                                                     Max(last_next_pick_up.result_value) AS "
            + "                                                                         max_datetame "
            + "                                                                 FROM "
            + "                                                                     (SELECT p.patient_id, "
            + "                                                                             Timestampadd(day, 1, o.value_datetime)  AS "
            + "                                                                                 result_value "
            + "                                                                      FROM   patient p "
            + "                                                                                 INNER JOIN encounter e "
            + "                                                                                            ON p.patient_id = "
            + "                                                                                               e.patient_id "
            + "                                                                                 INNER JOIN obs o "
            + "                                                                                            ON e.encounter_id = "
            + "                                                                                               o.encounter_id "
            + "                                                                                 INNER JOIN "
            + "                                                                             (SELECT p.patient_id, "
            + "                                                                                     Max(e.encounter_datetime) "
            + "                                                                                         AS "
            + "                                                                                         e_datetime "
            + "                                                                              FROM   patient p "
            + "                                                                                         INNER JOIN encounter e "
            + "                                                                                                    ON p.patient_id = "
            + "                                                                                                       e.patient_id "
            + "                                                                              WHERE  p.voided = 0 "
            + "                                                                                AND e.voided = 0 "
            + "                                                                                AND e.location_id = :location "
            + "                                                                                AND e.encounter_type = ${18} "
            + "                                                                                AND e.encounter_datetime <= DATE_SUB(CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ), INTERVAL "
            + coortendYear
            + " YEAR) "
            + "                                                                              GROUP  BY p.patient_id) "
            + "                                                                                 most_recent "
            + "                                                                             ON p.patient_id = "
            + "                                                                                most_recent.patient_id "
            + "                                                                      WHERE  p.voided = 0 "
            + "                                                                        AND e.voided = 0 "
            + "                                                                        AND o.voided = 0 "
            + "                                                                        AND e.location_id = :location "
            + "                                                                        AND e.encounter_type = ${18} "
            + "                                                                        AND o.concept_id = ${5096} "
            + "                                                                        AND e.encounter_datetime = "
            + "                                                                            most_recent.e_datetime "
            + "                                                                      GROUP  BY p.patient_id "
            + "                                                                      UNION "
            + "                                                                      SELECT p.patient_id, "
            + "                                                                             Timestampadd(day, 31, Max(o.value_datetime)) "
            + "                                                                                 AS "
            + "                                                                                 result_value "
            + "                                                                      FROM   patient p "
            + "                                                                                 INNER JOIN encounter e "
            + "                                                                                            ON p.patient_id = e.patient_id "
            + "                                                                                 INNER JOIN obs o "
            + "                                                                                            ON e.encounter_id = o.encounter_id "
            + "                                                                                 INNER JOIN obs o2 "
            + "                                                                                            ON e.encounter_id = o2.encounter_id "
            + "                                                                      WHERE  p.voided = 0 "
            + "                                                                        AND e.voided = 0 "
            + "                                                                        AND o.voided = 0 "
            + "                                                                        AND o2.voided = 0 "
            + "                                                                        AND e.location_id = :location "
            + "                                                                        AND e.encounter_type = ${52} "
            + "                                                                        AND ( o.concept_id = ${23866} "
            + "                                                                          AND o.value_datetime <= DATE_SUB(CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ), INTERVAL "
            + coortendYear
            + " YEAR) "
            + " ) "
            + "                                                                        AND ( o2.concept_id = ${23865} "
            + "                                                                          AND o2.value_coded = ${1065} ) "
            + "                                                                      GROUP  BY p.patient_id) AS last_next_pick_up "
            + "                                                                 GROUP  BY last_next_pick_up.patient_id) AS "
            + "                                              last_next_scheduled_pick_up "
            + "                                                                ON last_next_scheduled_pick_up.patient_id = p.patient_id "
            + "                                          WHERE  last_next_scheduled_pick_up.max_datetame > DATE_SUB(CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ), INTERVAL "
            + coortendYear
            + " YEAR) "
            + " ) "
            + "GROUP  BY transferred_out.patient_id";

    Map<String, Integer> map = new HashMap<>();
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put(
        "7",
        hivMetadata
            .getTransferredOutToAnotherHealthFacilityWorkflowState()
            .getProgramWorkflowStateId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    return stringSubstitutor.replace(query);
  }

  /**
   * <b>Utentes Transferidos Para</b><br>
   * O sistema irá identificar os utentes Transferido para até o fim do período de avaliação, da
   * seguinte forma:
   *
   * <ul>
   *   <li>utentes inscritos como “Transferido para” (último estado de inscrição) no programa
   *       SERVIÇO TARV TRATAMENTO com “Data de Transferência” <= “Data Fim Avaliação; ou
   *   <li>utentes com registo do último estado [“Mudança Estado Permanência TARV” (Coluna 21) = “T”
   *       (Transferido Para) na Ficha Clínica com “Data da Consulta Actual” (Coluna 1, durante a
   *       qual se fez o registo da mudança do estado de permanência TARV) <= “Data Fim Avaliação;
   *       ou
   *   <li>utentes com último registo de “Mudança Estado Permanência TARV” = “Transferido Para” na
   *       Ficha Resumo com “Data da Transferência” <= “Data Fim Avaliação;
   * </ul>
   *
   * <br>
   * <b>excepto os utentes que tenham tido um levantamento de ARV (FILA) após a “Data de
   * Transferência” (a data mais recente entre os critérios acima identificados) e até “Data Fim
   * Avaliação
   *
   * <p>excepto os utentes que tenham a data mais recente entre:
   *
   * <ul>
   *   <li>a “Data Próximo Levantamento” +1 dia, registado no último FILA até ao fim do período de
   *       avaliação e
   *   <li>a última “Data de Levantamento” registada até o fim do período de avaliação na Ficha
   *       Recepção/Levantou ARV, adicionando 31 dias.
   *       <p>e sendo essa data posterior ao fim do período de avaliação. Ou seja, o utente só será
   *       considerado como Transferido Para outra US depois de terminar a sua última medicação.
   * </ul>
   *
   * </b><br>
   * <b>Nota: </b> Sendo a “Data Fim de Inclusão” definida da seguinte forma:
   *
   * <ul>
   *   <li>Coluna “Estado de Permanência no 12º Mês” (RF30)
   *       <ul>
   *         <li>Data Fim Inclusão = “20 de Junho” de “Ano de Avaliação” menos (-) 1 ano
   *       </ul>
   *   <li>Coluna “Estado de Permanência no 24º Mês” (RF45)
   *       <ul>
   *         <li>Data Fim Inclusão = “20 de Junho” de “Ano de Avaliação” menos (-) 2 anos
   *       </ul>
   *   <li>Coluna “Estado de Permanência no 36º Mês” (RF60) *
   *       <ul>
   *         *
   *         <li>Data Fim Inclusão = “20 de Junho” de “Ano de Avaliação” menos (-) 3 anos *
   *       </ul>
   *   <li>Exclusão do Relatório (RF4) *
   *       <ul>
   *         *
   *         <li>Data Fim Inclusão = “20 de Junho” de “Ano de Avaliação” *
   *       </ul>
   * </ul>
   *
   * @return String
   */
  public String getPatientsWhoHaveTransferredOutAsPermananceState() {

    Map<String, Integer> map = new HashMap<>();
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put(
        "7",
        hivMetadata
            .getTransferredOutToAnotherHealthFacilityWorkflowState()
            .getProgramWorkflowStateId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());

    String query =
        " SELECT patient_id, "
            + " 'Transferido Para' "
            + "FROM   (SELECT latest.patient_id, "
            + "               Max(latest.last_date) AS last_date "
            + "        FROM   (SELECT p.patient_id, "
            + "                       laststate.last_date AS last_date "
            + "                FROM   patient p "
            + "                           INNER JOIN patient_program pg "
            + "                                      ON p.patient_id = pg.patient_id "
            + "                           INNER JOIN patient_state ps "
            + "                                      ON pg.patient_program_id = ps.patient_program_id "
            + "                           INNER JOIN (SELECT p.patient_id, "
            + "                                              Max(ps.start_date) AS last_date "
            + "                                       FROM   patient p "
            + "                                                  INNER JOIN patient_program pg "
            + "                                                             ON "
            + "                                                                 p.patient_id = pg.patient_id "
            + "                                                  INNER JOIN patient_state ps "
            + "                                                             ON "
            + "                                                                 pg.patient_program_id = ps.patient_program_id "
            + "                                       WHERE  pg.voided = 0 "
            + "                                         AND ps.voided = 0 "
            + "                                         AND p.voided = 0 "
            + "                                         AND pg.program_id = ${2} "
            + "                                         AND ps.state IS NOT NULL "
            + "                                         AND ps.start_date <= :endDate "
            + "                                         AND pg.location_id = :location "
            + "                                       GROUP  BY p.patient_id) laststate "
            + "                                      ON laststate.patient_id = p.patient_id "
            + "                WHERE  pg.voided = 0 "
            + "                  AND ps.voided = 0 "
            + "                  AND p.voided = 0 "
            + "                  AND pg.program_id = ${2} "
            + "                  AND ps.state = ${7} "
            + "                  AND ps.start_date = laststate.last_date "
            + "                  AND pg.location_id = :location "
            + "                GROUP  BY p.patient_id "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       Max(o.obs_datetime) AS last_date "
            + "                FROM   patient p "
            + "                           INNER JOIN encounter e "
            + "                                      ON e.patient_id = p.patient_id "
            + "                           INNER JOIN obs o "
            + "                                      ON e.encounter_id = o.encounter_id "
            + "                WHERE  p.voided = 0 "
            + "                  AND e.voided = 0 "
            + "                  AND o.voided = 0 "
            + "                  AND e.encounter_type = ${53} "
            + "                  AND o.concept_id = ${6272} "
            + "                  AND o.value_coded = ${1706} "
            + "                  AND o.obs_datetime <= :endDate "
            + "                  AND e.location_id = :location "
            + "                GROUP  BY p.patient_id "
            + "                UNION "
            + "                SELECT p.patient_id, "
            + "                       max_seg.last_date AS last_date "
            + "                FROM   patient p "
            + "                           INNER JOIN encounter e "
            + "                                      ON e.patient_id = p.patient_id "
            + "                           INNER JOIN obs o "
            + "                                      ON o.encounter_id = e.encounter_id "
            + "                           INNER JOIN (SELECT p.patient_id, "
            + "                                              Max(e.encounter_datetime) AS last_date "
            + "                                       FROM   patient p "
            + "                                                  INNER JOIN encounter e "
            + "                                                             ON e.patient_id = p.patient_id "
            + "                                                  INNER JOIN obs o "
            + "                                                             ON o.encounter_id = e.encounter_id "
            + "                                       WHERE  p.voided = 0 "
            + "                                         AND e.voided = 0 "
            + "                                         AND o.voided = 0 "
            + "                                         AND e.encounter_type = ${6} "
            + "                                         AND e.encounter_datetime <= :endDate "
            + "                                         AND e.location_id = :location "
            + "                                       GROUP  BY p.patient_id) max_seg "
            + "                                      ON max_seg.patient_id = p.patient_id "
            + "                WHERE  p.voided = 0 "
            + "                  AND e.voided = 0 "
            + "                  AND o.voided = 0 "
            + "                  AND e.encounter_type = ${6} "
            + "                  AND o.concept_id = ${6273} "
            + "                  AND o.value_coded = ${1706} "
            + "                  AND e.encounter_datetime = max_seg.last_date "
            + "                  AND e.location_id = :location "
            + "                GROUP  BY p.patient_id) latest "
            + "        GROUP  BY latest.patient_id) transferred_out "
            + "WHERE  transferred_out.patient_id NOT IN (SELECT p.patient_id "
            + "                                          FROM   patient p "
            + "                                                     INNER JOIN encounter e "
            + "                                                                ON "
            + "                                                                    e.patient_id = p.patient_id "
            + "                                          WHERE  p.voided = 0 "
            + "                                            AND e.voided = 0 "
            + "                                            AND p.patient_id = "
            + "                                                transferred_out.patient_id "
            + "                                            AND e.encounter_type = ${18} "
            + "                                            AND "
            + "                                              e.encounter_datetime > transferred_out.last_date "
            + "                                            AND "
            + "                                              e.encounter_datetime <= :endDate "
            + "                                            AND e.location_id = :location "
            + "                                          UNION "
            + "                                          SELECT p.patient_id "
            + "                                          FROM   patient p "
            + "                                                     INNER JOIN (SELECT "
            + "                                                                     last_next_pick_up.patient_id, "
            + "                                                                     Max(last_next_pick_up.result_value) AS "
            + "                                                                         max_datetame "
            + "                                                                 FROM "
            + "                                                                     (SELECT p.patient_id, "
            + "                                                                             Timestampadd(day, 1, o.value_datetime)  AS "
            + "                                                                                 result_value "
            + "                                                                      FROM   patient p "
            + "                                                                                 INNER JOIN encounter e "
            + "                                                                                            ON p.patient_id = "
            + "                                                                                               e.patient_id "
            + "                                                                                 INNER JOIN obs o "
            + "                                                                                            ON e.encounter_id = "
            + "                                                                                               o.encounter_id "
            + "                                                                                 INNER JOIN "
            + "                                                                             (SELECT p.patient_id, "
            + "                                                                                     Max(e.encounter_datetime) "
            + "                                                                                         AS "
            + "                                                                                         e_datetime "
            + "                                                                              FROM   patient p "
            + "                                                                                         INNER JOIN encounter e "
            + "                                                                                                    ON p.patient_id = "
            + "                                                                                                       e.patient_id "
            + "                                                                              WHERE  p.voided = 0 "
            + "                                                                                AND e.voided = 0 "
            + "                                                                                AND e.location_id = :location "
            + "                                                                                AND e.encounter_type = ${18} "
            + "                                                                                AND e.encounter_datetime "
            + "                                                                                  <= "
            + "                                                                                    :endDate "
            + "                                                                              GROUP  BY p.patient_id) "
            + "                                                                                 most_recent "
            + "                                                                             ON p.patient_id = "
            + "                                                                                most_recent.patient_id "
            + "                                                                      WHERE  p.voided = 0 "
            + "                                                                        AND e.voided = 0 "
            + "                                                                        AND o.voided = 0 "
            + "                                                                        AND e.location_id = :location "
            + "                                                                        AND e.encounter_type = ${18} "
            + "                                                                        AND o.concept_id = ${5096} "
            + "                                                                        AND e.encounter_datetime = "
            + "                                                                            most_recent.e_datetime "
            + "                                                                      GROUP  BY p.patient_id "
            + "                                                                      UNION "
            + "                                                                      SELECT p.patient_id, "
            + "                                                                             Timestampadd(day, 31, Max(o.value_datetime)) "
            + "                                                                                 AS "
            + "                                                                                 result_value "
            + "                                                                      FROM   patient p "
            + "                                                                                 INNER JOIN encounter e "
            + "                                                                                            ON p.patient_id = e.patient_id "
            + "                                                                                 INNER JOIN obs o "
            + "                                                                                            ON e.encounter_id = o.encounter_id "
            + "                                                                                 INNER JOIN obs o2 "
            + "                                                                                            ON e.encounter_id = o2.encounter_id "
            + "                                                                      WHERE  p.voided = 0 "
            + "                                                                        AND e.voided = 0 "
            + "                                                                        AND o.voided = 0 "
            + "                                                                        AND o2.voided = 0 "
            + "                                                                        AND e.location_id = :location "
            + "                                                                        AND e.encounter_type = ${52} "
            + "                                                                        AND ( o.concept_id = ${23866} "
            + "                                                                          AND o.value_datetime <= :endDate ) "
            + "                                                                        AND ( o2.concept_id = ${23865} "
            + "                                                                          AND o2.value_coded = ${1065} ) "
            + "                                                                      GROUP  BY p.patient_id) AS last_next_pick_up "
            + "                                                                 GROUP  BY last_next_pick_up.patient_id) AS "
            + "                                              last_next_scheduled_pick_up "
            + "                                                                ON last_next_scheduled_pick_up.patient_id = p.patient_id "
            + "                                          WHERE  last_next_scheduled_pick_up.max_datetame > :endDate ) "
            + "GROUP  BY transferred_out.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    return stringSubstitutor.replace(query);
  }

  /**
   * O registo de “Diagnóstico TB Activa” (resposta = “Sim”) numa Ficha Clínica (“Data Consulta”)
   * registada em 33 dias do Início TARV, ou seja, entre “Data Início TARV” e “Data Início TARV” +
   * 33 dias.
   *
   * @return String
   */
  public static String getTbActive() {

    String query =
        " SELECT     p.patient_id, "
            + "          e.encounter_datetime AS encounter_date "
            + " FROM       patient p "
            + " INNER JOIN encounter e "
            + " ON         e.patient_id = p.patient_id "
            + " INNER JOIN obs o "
            + " ON         o.encounter_id = e.encounter_id "
            + " INNER JOIN ( "
            + "               SELECT art_patient.patient_id, "
            + "                      art_patient.first_pickup AS art_encounter "
            + "               FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                       ) art_patient "
            + " ) art ON art.patient_id = p.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND e.encounter_type = ${6} "
            + " AND e.location_id = :location "
            + " AND o.concept_id = ${23761} "
            + " AND o.value_coded = ${1065} "
            + " AND e.encounter_datetime >= art.art_encounter "
            + " AND e.encounter_datetime <= DATE_ADD( art.art_encounter, INTERVAL 33 DAY ) "
            + " GROUP  BY p.patient_id ";

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("23761", hivMetadata.getActiveTBConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    return stringSubstitutor.replace(query);
  }

  /**
   * O registo de “Diagnóstico TB Activa” (resposta = “Sim”) numa Ficha Clínica (“Data Consulta”)
   * registada em 33 dias do Início TARV, ou seja, entre “Data Início TARV” e “Data Início TARV” +
   * 33 dias.
   *
   * @return String
   */
  public static String getTBSymptoms() {

    String query =
        " SELECT     p.patient_id, "
            + "          e.encounter_datetime AS encounter_date "
            + " FROM       patient p "
            + " INNER JOIN encounter e "
            + " ON         e.patient_id = p.patient_id "
            + " INNER JOIN obs o "
            + " ON         o.encounter_id = e.encounter_id "
            + " INNER JOIN ( "
            + "             SELECT art_patient.patient_id, "
            + "                    art_patient.first_pickup AS art_encounter "
            + "             FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                    ) art_patient "
            + " ) art ON art.patient_id = p.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND e.encounter_type = ${6} "
            + " AND e.location_id = :location "
            + " AND o.concept_id = ${23758} "
            + " AND o.value_coded = ${1065} "
            + " AND e.encounter_datetime >= art.art_encounter "
            + " AND e.encounter_datetime <= DATE_ADD( art.art_encounter, INTERVAL 33 DAY ) "
            + " GROUP  BY p.patient_id ";

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("23758", hivMetadata.getTBSymptomsConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    return stringSubstitutor.replace(query);
  }

  /**
   * O registo de “Quais Sintomas de TB?” (resposta = “Febre” ou “Emagrecimento” ou "Sudorese
   * noturna” ou “Tosse com ou sem sangue” ou “Astenia” ou “Contacto recente com TB” numa Ficha
   * Clínica (“Data Consulta”) registada em 33 dias do Início TARV, ou seja, entre “Data Início
   * TARV” e “Data Início TARV” + 33 dias.
   *
   * @return String
   */
  public static String getTBSymptomsTypes() {
    String query =
        " SELECT     p.patient_id, "
            + "          e.encounter_datetime AS encounter_date "
            + " FROM       patient p "
            + " INNER JOIN encounter e "
            + " ON         e.patient_id = p.patient_id "
            + " INNER JOIN obs o "
            + " ON         o.encounter_id = e.encounter_id "
            + " INNER JOIN ( "
            + "              SELECT art_patient.patient_id, "
            + "                     art_patient.first_pickup AS art_encounter "
            + "              FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                     ) art_patient "
            + " ) art ON art.patient_id = p.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND e.encounter_type = ${6} "
            + " AND e.location_id = :location "
            + " AND o.concept_id = ${1766} "
            + " AND o.value_coded IN( ${1763}, ${1764}, ${1762}, ${1760}, "
            + "                       ${23760}, ${1765}, ${161} ) "
            + " AND e.encounter_datetime >= art.art_encounter "
            + " AND e.encounter_datetime <= DATE_ADD( art.art_encounter, INTERVAL 33 DAY ) "
            + " GROUP  BY p.patient_id ";

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1766", tbMetadata.getObservationTB().getConceptId());
    map.put("1763", tbMetadata.getFeverLastingMoraThan3Weeks().getConceptId());
    map.put("1764", tbMetadata.getWeightLossOfMoreThan3KgInLastMonth().getConceptId());
    map.put("1762", tbMetadata.getNightsWeatsLastingMoraThan3Weeks().getConceptId());
    map.put("1760", tbMetadata.getCoughLastingMoraThan3Weeks().getConceptId());
    map.put("23760", tbMetadata.getAsthenia().getConceptId());
    map.put("1765", tbMetadata.getCohabitantBeingTreatedForTB().getConceptId());
    map.put("161", tbMetadata.getLymphadenopathy().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    return stringSubstitutor.replace(query);
  }

  /**
   * O registo de “Tratamento TB” (resposta = “Início”, “Contínua”, “Fim”) na Ficha Clínica com
   * “Data de Tratamento” registada em 33 dias do Início TARV, ou seja, entre “Data Início TARV” e
   * “Data Início TARV” + 33 dias.
   *
   * @return String
   */
  public static String getTbTreatment() {
    String query =
        " SELECT     p.patient_id, "
            + "          o.obs_datetime AS encounter_date "
            + " FROM       patient p "
            + " INNER JOIN encounter e "
            + " ON         e.patient_id = p.patient_id "
            + " INNER JOIN obs o "
            + " ON         o.encounter_id = e.encounter_id "
            + " INNER JOIN ( "
            + "               SELECT art_patient.patient_id, "
            + "                      art_patient.first_pickup AS art_encounter "
            + "               FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                      ) art_patient "
            + " ) art ON art.patient_id = p.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND e.encounter_type = ${6} "
            + " AND e.location_id = :location "
            + " AND o.concept_id = ${1268} "
            + " AND o.value_coded IN ( ${1256}, ${1257}, ${1267} ) "
            + " AND o.obs_datetime >= art.art_encounter "
            + " AND o.obs_datetime <= DATE_ADD( art.art_encounter, INTERVAL 33 DAY ) "
            + " GROUP  BY p.patient_id ";

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1268", hivMetadata.getTBTreatmentPlanConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("1257", hivMetadata.getContinueRegimenConcept().getConceptId());
    map.put("1267", hivMetadata.getCompletedConcept().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    return stringSubstitutor.replace(query);
  }

  /**
   * O registo de “TB” nas “Condições médicas Importantes” na Ficha Resumo com “Data” registada em
   * 33 dias do Início TARV, ou seja, entre “Data Início TARV” e “Data Início TARV” + 33 dias.
   *
   * @return String
   */
  public static String getImportantMedicalConditions() {
    String query =
        " SELECT     p.patient_id, "
            + "          o.obs_datetime AS encounter_date "
            + " FROM       patient p "
            + " INNER JOIN encounter e "
            + " ON         e.patient_id = p.patient_id "
            + " INNER JOIN obs o "
            + " ON         o.encounter_id = e.encounter_id "
            + " INNER JOIN ( "
            + "               SELECT art_patient.patient_id, "
            + "                      art_patient.first_pickup AS art_encounter "
            + "               FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                      ) art_patient "
            + " ) art ON art.patient_id = p.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND e.encounter_type = ${53} "
            + " AND e.location_id = :location "
            + " AND o.concept_id = ${1406} "
            + " AND o.value_coded = ${42} "
            + " AND o.obs_datetime >= art.art_encounter "
            + " AND o.obs_datetime <= DATE_ADD( art.art_encounter, INTERVAL 33 DAY ) "
            + " GROUP  BY p.patient_id ";

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1406", hivMetadata.getOtherDiagnosis().getConceptId());
    map.put("42", tbMetadata.getPulmonaryTB().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    return stringSubstitutor.replace(query);
  }

  public static String getFirstMdsAndDateQuery(int numberOfMonths) {
    return "   SELECT     mds1.patient_id, "
        + "                             otype1.value_coded AS mds_one,"
        + "                                      enc.encounter_datetime AS first_mds "
        + "                  FROM       patient mds1 "
        + "                  INNER JOIN encounter enc "
        + "                  ON         enc.patient_id = mds1.patient_id "
        + "                  INNER JOIN obs otype1 "
        + "                  ON         otype1.encounter_id = enc.encounter_id "
        + "                  INNER JOIN obs ostate1 "
        + "                  ON         ostate1.encounter_id = enc.encounter_id "
        + "                  INNER JOIN ( "
        + "                  SELECT     p.patient_id, "
        + "                             MIN(e.encounter_datetime) AS encounter_date "
        + "                  FROM       patient p "
        + "                  INNER JOIN encounter e "
        + "                  ON         e.patient_id = p.patient_id "
        + "                  INNER JOIN obs otype "
        + "                  ON         otype.encounter_id = e.encounter_id "
        + "                  INNER JOIN obs ostate "
        + "                  ON         ostate.encounter_id = e.encounter_id "
        + "                  INNER JOIN ( "
        + "                           SELECT art_patient.patient_id, "
        + "                                  art_patient.first_pickup AS art_encounter "
        + "                           FROM   ( "
        + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
        + "                           ) art_patient "
        + "                             ) art "
        + "                  ON         art.patient_id = p.patient_id "
        + "                  WHERE      p.voided = 0 "
        + "                  AND        e.voided = 0 "
        + "                  AND        otype.voided = 0 "
        + "                  AND        ostate.voided = 0 "
        + "                  AND        e.encounter_type = ${6} "
        + "                  AND        e.location_id = :location "
        + "                  AND        e.encounter_datetime >= date_add( art.art_encounter, INTERVAL 33 DAY ) "
        + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
        + numberOfMonths
        + " MONTH ) "
        + "                  AND    (   ( otype.concept_id = ${165174} "
        + "                               AND otype.value_coded IS NOT NULL ) "
        + "                  AND         ( ostate.concept_id = ${165322} "
        + "                                 AND  ostate.value_coded IN (${1256}) ) ) "
        + "                  AND  otype.obs_group_id = ostate.obs_group_id "
        + "                  GROUP BY   p.patient_id ) mds_one "
        + "                  ON mds_one.patient_id = mds1.patient_id "
        + "                  WHERE mds1.voided = 0 "
        + "                  AND enc.voided = 0 "
        + "                  AND otype1.voided = 0 "
        + "                  AND ostate1.voided = 0 "
        + "                  AND        enc.encounter_type = ${6} "
        + "                  AND        enc.location_id = :location "
        + "                  AND        enc.encounter_datetime = mds_one.encounter_date "
        + "                  AND    (   ( otype1.concept_id = ${165174} "
        + "                               AND otype1.value_coded IS NOT NULL ) "
        + "                  AND         ( ostate1.concept_id = ${165322} "
        + "                                 AND  ostate1.value_coded IN (${1256}) ) ) "
        + "                  AND  otype1.obs_group_id = ostate1.obs_group_id "
        + "       GROUP BY mds1.patient_id ";
  }
}
