package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.eptsreports.reporting.library.queries.ListOfPatientsWithMdsEvaluationQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsQueriesUtil;
import org.openmrs.module.eptsreports.reporting.utils.queries.UnionBuilder;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsWithMdsEvaluationCohortQueries {

  private HivMetadata hivMetadata;
  private TbMetadata tbMetadata;
  private CommonMetadata commonMetadata;
  private String inclusionStartMonthAndDay = "'-12-21'";
  private String inclusionEndMonthAndDay = "'-06-20'";

  private final ResumoMensalCohortQueries resumoMensalCohortQueries;

  private final ListOfPatientsWithMdsEvaluationQueries listOfPatientsWithMdsEvaluationQueries;

  @Autowired
  public ListOfPatientsWithMdsEvaluationCohortQueries(
      HivMetadata hivMetadata,
      TbMetadata tbMetadata,
      CommonMetadata commonMetadata,
      ResumoMensalCohortQueries resumoMensalCohortQueries,
      ListOfPatientsWithMdsEvaluationQueries listOfPatientsWithMdsEvaluationQueries) {
    this.hivMetadata = hivMetadata;
    this.tbMetadata = tbMetadata;
    this.commonMetadata = commonMetadata;
    this.resumoMensalCohortQueries = resumoMensalCohortQueries;
    this.listOfPatientsWithMdsEvaluationQueries = listOfPatientsWithMdsEvaluationQueries;
  }

  /**
   * <b>RF9 - Relatório – Informação do utente - Secção A1 a A9</b>
   *
   * <p>A2- Coorte: (coluna B) – Resposta = 12 meses, caso o utente tenha iniciado TARV na coorte de
   * 12 meses, ou Resposta = 24 meses, caso o utente tenha iniciado TARV na coorte de 24 meses
   * (RF4).
   *
   * @return {CohortDefinition}
   */
  public CohortDefinition getCoort(
      int numberOfYearsStartDate, int numberOfYearsEndDate, boolean coortName) {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Patients who initiated the ART between the cohort period");
    sqlCohortDefinition.addParameter(
        new Parameter("evaluationYear", "evaluationYear", Integer.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "Location", Location.class));

    String query = getCoort12Or24Or36Query(numberOfYearsStartDate, numberOfYearsEndDate, coortName);

    sqlCohortDefinition.setQuery(query);

    return sqlCohortDefinition;
  }

  public CohortDefinition getCoort12Or24Or36() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Patients who initiated the ART between the cohort period");
    cd.addParameter(new Parameter("evaluationYear", "evaluationYear", Integer.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition twelveMonths = getCoort(2, 1, false);
    CohortDefinition twentyFourMonths = getCoort(3, 2, false);
    CohortDefinition thirtySixMonths = getCoort(4, 3, false);

    cd.addSearch("twelveMonths", Mapped.mapStraightThrough(twelveMonths));
    cd.addSearch("twentyFourMonths", Mapped.mapStraightThrough(twentyFourMonths));
    cd.addSearch("thirtySixMonths", Mapped.mapStraightThrough(thirtySixMonths));

    cd.setCompositionString("twelveMonths OR twentyFourMonths OR thirtySixMonths");

    return cd;
  }

  public CohortDefinition getCoort24Or36month() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Patients who initiated the ART between the cohort period");
    cd.addParameter(new Parameter("evaluationYear", "evaluationYear", Integer.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition twentyFourMonths = getCoort(3, 2, false);
    CohortDefinition thirtySixMonths = getCoort(4, 3, false);

    cd.addSearch("twentyFourMonths", Mapped.mapStraightThrough(twentyFourMonths));
    cd.addSearch("thirtySixMonths", Mapped.mapStraightThrough(thirtySixMonths));

    cd.setCompositionString("twentyFourMonths OR thirtySixMonths");

    return cd;
  }

  public CohortDefinition getCoort36month() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Patients who initiated the ART between the cohort period");
    cd.addParameter(new Parameter("evaluationYear", "evaluationYear", Integer.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition thirtySixMonths = getCoort(4, 3, false);

    cd.addSearch("thirtySixMonths", Mapped.mapStraightThrough(thirtySixMonths));

    cd.setCompositionString("thirtySixMonths");

    return cd;
  }

  private String getCoort12Or24Or36Query(
      int numberOfYearsStartDate, int numberOfYearsEndDate, boolean coortName) {

    String query = "";

    String fromQuery =
        "     FROM   ( "
            + ListOfPatientsWithMdsEvaluationQueries.getPatientArtStart(inclusionEndMonthAndDay)
            + "     ) art_patient "
            + " WHERE  art_patient.first_pickup >= DATE_SUB( "
            + "  CONCAT(:evaluationYear,"
            + inclusionStartMonthAndDay
            + "        ), INTERVAL "
            + numberOfYearsStartDate
            + " YEAR) "
            + " AND  art_patient.first_pickup <= DATE_SUB( "
            + "  CONCAT(:evaluationYear,"
            + inclusionEndMonthAndDay
            + "        ) ,INTERVAL  "
            + numberOfYearsEndDate
            + " YEAR) "
            + " AND art_patient.patient_id "
            + " NOT IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getB1PatientsWhoAreTransferredIn(
                inclusionEndMonthAndDay, numberOfYearsEndDate)
            + " )";

    if (coortName && numberOfYearsEndDate == 1) {
      query = "SELECT art_patient.patient_id, '12 Meses' ".concat(fromQuery);
    }
    if (coortName && numberOfYearsEndDate == 2) {
      query = "SELECT art_patient.patient_id, '24 Meses' ".concat(fromQuery);
    }
    if (coortName && numberOfYearsEndDate == 3) {
      query = "SELECT art_patient.patient_id, '36 Meses' ".concat(fromQuery);
    }
    if (!coortName) {
      query = "SELECT art_patient.patient_id ".concat(fromQuery);
    }

    return query;
  }

  /**
   * <b>RF9 - Relatório – Informação do utente - Secção A1 a A9</b>
   *
   * <p>A2- Coorte: (coluna B) – Resposta = 12 meses, caso o utente tenha iniciado TARV na coorte de
   * 12 meses, ou Resposta = 24 meses, caso o utente tenha iniciado TARV na coorte de 24 meses ou
   * Resposta = 36 meses, caso o utente tenha iniciado TARV na coorte de 36 meses (RF4).
   *
   * @return {DataDefinition}
   */
  public DataDefinition getCoort12Or24Or36Months() {

    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(
        "A.2 - Coorte: – Resposta = 12 meses ou Resposta = 24 meses ou Resposta = 36 meses.");
    sqlPatientDataDefinition.addParameter(
        new Parameter("evaluationYear", "evaluationYear", Integer.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    String query12month = getCoort12Or24Or36Query(2, 1, true);
    String query24month = getCoort12Or24Or36Query(3, 2, true);
    String query36month = getCoort12Or24Or36Query(4, 3, true);

    String query =
        new UnionBuilder(query12month).union(query24month).union(query36month).buildQuery();

    sqlPatientDataDefinition.setQuery(query);

    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF9 - Relatório – Informação do utente - Secção A1 a A9</b>
   *
   * <p>O sistema irá determinar a Data Início TARV do utente da seguinte forma:
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
   * @return {DataDefinition}
   */
  public DataDefinition getArtStartDate() {

    SqlPatientDataDefinition dd = new SqlPatientDataDefinition();
    dd.setName("A.5 - ART Start Date");
    dd.addParameter(new Parameter("endDate", "endDate", Date.class));
    dd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());

    String query =
        "       SELECT start.patient_id, "
            + "        start.first_pickup AS first_pickup "
            + " FROM ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "       ) start ";

    StringSubstitutor sb = new StringSubstitutor(map);
    dd.setQuery(sb.replace(query));

    return dd;
  }

  /**
   * <b> this method will generate one union separeted query based on the given queries</b>
   *
   * @return {@link String}
   */
  private String getUnionQuery() {

    EptsQueriesUtil queriesUtil = new EptsQueriesUtil();

    return queriesUtil
        .unionBuilder(ListOfPatientsWithMdsEvaluationQueries.getTbActive())
        .union(ListOfPatientsWithMdsEvaluationQueries.getTBSymptoms())
        .union(ListOfPatientsWithMdsEvaluationQueries.getTBSymptomsTypes())
        .union(ListOfPatientsWithMdsEvaluationQueries.getTbTreatment())
        .union(ListOfPatientsWithMdsEvaluationQueries.getImportantMedicalConditions())
        .buildQuery();
  }

  /**
   * <b>RF12 - Critérios de Não Elegibilidade ao TPT</b>
   *
   * <p>O sistema irá determinar se o utente não é elegível ao TPT se o utente tiver:
   *
   * <ul>
   *   <li>o registo de “Diagnóstico TB Activa” (resposta = “Sim”) numa Ficha Clínica (“Data
   *       Consulta”) registada em 33 dias do Início TARV, ou seja, entre “Data Início TARV” e “Data
   *       Início TARV” + 33 dias. <br>
   *   <li>o registo de “Tem Sintomas TB?” (resposta = “Sim”) numa Ficha Clínica (“Data Consulta”)
   *       registada em 33 dias do Início TARV, ou seja, entre “Data Início TARV” e “Data Início
   *       TARV” + 33 dias. <br>
   *   <li>o registo de “Quais Sintomas de TB?” (resposta = “Febre” ou “Emagrecimento” ou "Sudorese
   *       noturna” ou “Tosse com ou sem sangue” ou “Astenia” ou “Contacto recente com TB” numa
   *       Ficha Clínica (“Data Consulta”) registada em 33 dias do Início TARV, ou seja, entre “Data
   *       Início TARV” e “Data Início TARV” + 33 dias. <br>
   *   <li>o registo de “Tratamento TB” (resposta = “Início”, “Contínua”, “Fim”) na Ficha Clínica
   *       com “Data de Tratamento” registada em 33 dias do Início TARV, ou seja, entre “Data Início
   *       TARV” e “Data Início TARV” + 33 dias. <br>
   *   <li>o registo de “TB” nas “Condições médicas Importantes” na Ficha Resumo com “Data”
   *       registada em 33 dias do Início TARV, ou seja, entre “Data Início TARV” e “Data Início
   *       TARV” + 33 dias. <br>
   * </ul>
   *
   * <p>Nota 1: A “Data Início TARV” é definida no RF61.
   *
   * @return {DataDefinition}
   */
  public DataDefinition getPatientsTptNotEligible() {
    SqlPatientDataDefinition dd = new SqlPatientDataDefinition();
    dd.setName("Elegível ao TPT no Início do TARV: (coluna F)");
    dd.addParameter(new Parameter("endDate", "endDate", Date.class));
    dd.addParameter(new Parameter("location", "location", Location.class));

    String sql = getUnionQuery();

    dd.setQuery(sql);

    return dd;
  }

  /**
   * O sistema irá determinar a idade do utente na Data Início TARV, ou seja, irá calcular a idade
   * com base na seguinte fórmula: Idade = Data Início TARV - Data de Nascimento
   *
   * <p>Nota 1: A idade será calculada em anos.
   *
   * <p>Nota 2: A “Data Início TARV” é definida no RF61
   *
   * @return DataDefinition
   */
  public DataDefinition getAgeOnMOHArtStartDate() {
    SqlPatientDataDefinition dd = new SqlPatientDataDefinition();
    dd.setName("Age on MOH ART start date");
    dd.addParameter(new Parameter("endDate", "endDate", Date.class));
    dd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());

    String query =
        "SELECT p.patient_id, FLOOR(DATEDIFF(art.first_pickup,ps.birthdate)/365) AS age "
            + "FROM patient p "
            + "     INNER JOIN ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "   ) AS art ON art.patient_id = p.patient_id "
            + "  INNER JOIN person ps ON p.patient_id=ps.person_id WHERE p.voided=0 AND ps.voided=0 ";

    StringSubstitutor sb = new StringSubstitutor(map);
    dd.setQuery(sb.replace(query));

    return dd;
  }

  /**
   * <b>RF13 - Data início do TPT - A.7 (coluna G)</b>
   *
   * <p>A7- Data de início do TPT: (coluna G) – Resposta = Data de Início TPT (RF13)
   *
   * <p>O sistema irá determinar a “Data Início do TPT” do utente selecionando a data mais antiga
   * dos seguintes critérios:
   *
   * <p>registo de “Profilaxia TPT” (resposta = “INH” ou “3HP” ou “1HP” ou “LFX”) e o respectivo
   * “Estado de Profilaxia TPT” (resposta = "Início") na Ficha Clínica (“Data Consulta”) durante o
   * período de inclusão;
   *
   * <p>registo de “Última profilaxia TPT” (resposta = “INH” ou ”3HP” ou “1HP” ou “LFX”) com “Data
   * Início” registada na Ficha Resumo durante o período de inclusão;
   *
   * <p>registo de “Outras Prescrições” (resposta= “DT-3HP”) na Ficha Clínica (“Data Consulta”)
   * registada em 33 dias do Início TARV, ou seja, entre “Data Início TARV” e “Data Início TARV” +
   * 33 dias.
   *
   * <p>Nota 1: A “Data Início TARV” é definida no RF61.
   *
   * @return {DataDefinition}
   */
  public DataDefinition getTptInitiationDate() {
    SqlPatientDataDefinition dd = new SqlPatientDataDefinition();
    dd.setName("Data de início do TPT.");
    dd.addParameter(new Parameter("endDate", "endDate", Date.class));
    dd.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    map.put("23985", tbMetadata.getRegimeTPTConcept().getConceptId());
    map.put("23954", tbMetadata.get3HPConcept().getConceptId());
    map.put("656", tbMetadata.getIsoniazidConcept().getConceptId());
    map.put("165305", tbMetadata.get1HPConcept().getConceptId());
    map.put("165306", tbMetadata.getLFXConcept().getConceptId());
    map.put("165308", tbMetadata.getDataEstadoDaProfilaxiaConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("1719", tbMetadata.getTreatmentPrescribedConcept().getConceptId());
    map.put("165307", tbMetadata.getDT3HPConcept().getConceptId());

    String query =
        " SELECT min.patient_id, min.encounter_date "
            + " FROM ( "
            + "         SELECT     p.patient_id, "
            + "                    MIN(e.encounter_datetime) AS encounter_date "
            + "         FROM       patient p "
            + "         INNER JOIN encounter e "
            + "         ON         e.patient_id = p.patient_id "
            + "         INNER JOIN obs o "
            + "         ON         o.encounter_id = e.encounter_id "
            + "         INNER JOIN obs o2 "
            + "         ON         o2.encounter_id = e.encounter_id "
            + "         INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + " ) art ON art.patient_id = p.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND o2.voided = 0 "
            + " AND e.encounter_type = ${6} "
            + " AND e.location_id = :location "
            + " AND ( ( o.concept_id = ${23985} "
            + " AND o.value_coded IN ( ${23954}, ${656}, ${165305}, ${165306} ) ) "
            + " AND ( o2.concept_id = ${165308} "
            + " AND o2.value_coded IN ( ${1256} ) ) ) "
            + " AND e.encounter_datetime >= art.art_encounter "
            + " AND e.encounter_datetime <= DATE_ADD( art.art_encounter, INTERVAL 33 DAY ) "
            + " GROUP  BY p.patient_id "
            + " UNION "
            + "        SELECT     p.patient_id, "
            + "                    MIN(o2.obs_datetime) AS encounter_date "
            + "         FROM       patient p "
            + "         INNER JOIN encounter e "
            + "         ON         e.patient_id = p.patient_id "
            + "         INNER JOIN obs o "
            + "         ON         o.encounter_id = e.encounter_id "
            + "         INNER JOIN obs o2 "
            + "         ON         o2.encounter_id = e.encounter_id "
            + "         INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + " ) art ON art.patient_id = p.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND o2.voided = 0 "
            + " AND e.encounter_type = ${53} "
            + " AND e.location_id = :location "
            + " AND ( ( o.concept_id = ${23985} "
            + " AND o.value_coded IN ( ${23954}, ${656}, ${165305}, ${165306} ) ) "
            + " AND ( o2.concept_id = ${165308} "
            + " AND o2.value_coded IN ( ${1256} ) ) ) "
            + " AND o2.obs_datetime >= art.art_encounter "
            + " AND o2.obs_datetime <= DATE_ADD( art.art_encounter, INTERVAL 33 DAY ) "
            + " GROUP  BY p.patient_id "
            + " UNION "
            + "         SELECT     p.patient_id, "
            + "                    MIN(e.encounter_datetime) AS encounter_date "
            + "         FROM       patient p "
            + "         INNER JOIN encounter e "
            + "         ON         e.patient_id = p.patient_id "
            + "         INNER JOIN obs o "
            + "         ON         o.encounter_id = e.encounter_id "
            + "         INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + " ) art ON art.patient_id = p.patient_id "
            + " WHERE  p.voided = 0 "
            + " AND e.voided = 0 "
            + " AND o.voided = 0 "
            + " AND e.encounter_type = ${6} "
            + " AND e.location_id = :location "
            + " AND o.concept_id = ${1719} "
            + " AND o.value_coded = ${165307} "
            + " AND e.encounter_datetime >= art.art_encounter "
            + " AND e.encounter_datetime <= DATE_ADD( art.art_encounter, INTERVAL 33 DAY ) "
            + " GROUP  BY p.patient_id"
            + " ) min "
            + " GROUP BY min.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    dd.setQuery(stringSubstitutor.replace(query));

    return dd;
  }

  /**
   * <b>RF14 - Data de registo de resultado do CD4 inicial - A.8 (Coluna H)</b>
   *
   * <p>A8- Data de registo do resultado de CD4 inicial: (coluna H) Resposta = Data do resultado de
   * CD4 inicial (RF14)
   *
   * <p>O sistema irá determinar a “Data de Registo de Resultado do CD4 Inicial” identificando a
   * primeira ou a segunda consulta (Ficha Clínica) na qual foi efectuado o registo do resultado do
   * CD4 inicial (“Investigações - Resultados Laboratoriais") ocorrido em 33 dias do Início TARV, ou
   * seja, entre “Data Início TARV” e “Data Início TARV” + 33 dias.
   *
   * @return {DataDefinition}
   */
  public DataDefinition getCd4ResultDate() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(
        "Identificação de Data de registo do resultado de CD4 inicial");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("1065", hivMetadata.getPatientFoundYesConcept().getConceptId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());

    String query = getFirstCd4ResultDateQuery();

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  private String getFirstCd4ResultDateQuery() {
    return "SELECT pt.patient_id, "
        + "        Min(enc.encounter_datetime) AS encounter_date "
        + "FROM   patient pt "
        + "       INNER JOIN (SELECT pa.patient_id, "
        + "                          Min(enc.encounter_datetime) AS encounter_date "
        + "                   FROM   patient pa "
        + "                          INNER JOIN encounter enc "
        + "                                  ON enc.patient_id = pa.patient_id "
        + "                          INNER JOIN obs "
        + "                                  ON obs.encounter_id = enc.encounter_id "
        + "                          INNER JOIN ( "
        + "                           SELECT art_patient.patient_id, "
        + "                                  art_patient.first_pickup AS art_encounter "
        + "                           FROM   ( "
        + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
        + "                           ) art_patient "
        + " ) art ON art.patient_id = enc.patient_id "
        + "                   WHERE  pa.voided = 0 "
        + "                          AND enc.voided = 0 "
        + "                          AND obs.voided = 0 "
        + "                          AND enc.encounter_type = ${6} "
        + "                          AND enc.location_id = :location "
        + "                          AND obs.concept_id = ${1695} "
        + "                          AND obs.value_numeric IS NOT NULL "
        + "                          AND enc.encounter_datetime >= art.art_encounter "
        + "                          AND enc.encounter_datetime <= DATE_ADD( "
        + "                              art.art_encounter, "
        + "                                                        INTERVAL 33 day) "
        + "                   GROUP  BY pa.patient_id "
        + "                   UNION "
        + "                   SELECT ee.patient_id, "
        + "                          Min(ee.encounter_datetime) AS encounter_date "
        + "                   FROM   encounter ee "
        + "                          INNER JOIN (SELECT pa.patient_id, "
        + "                                             Min(enc.encounter_datetime) AS "
        + "                                             encounter_date "
        + "                                      FROM   patient pa "
        + "                                             INNER JOIN encounter enc "
        + "                                                     ON enc.patient_id = "
        + "                                                        pa.patient_id "
        + "                                             INNER JOIN obs "
        + "                                                     ON obs.encounter_id = "
        + "                                                        enc.encounter_id "
        + "                                             INNER JOIN ( "
        + "                           SELECT art_patient.patient_id, "
        + "                                  art_patient.first_pickup AS art_encounter "
        + "                           FROM   ( "
        + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
        + "                           ) art_patient "
        + " ) art ON art.patient_id = enc.patient_id "
        + "       WHERE  pa.voided = 0 "
        + "       AND enc.voided = 0 "
        + "       AND obs.voided = 0 "
        + "       AND enc.encounter_type = ${6} "
        + "       AND enc.location_id = :location "
        + "       AND obs.concept_id = ${1695} "
        + "       AND obs.value_numeric IS NOT NULL "
        + "       AND enc.encounter_datetime >= art.art_encounter "
        + "       AND enc.encounter_datetime <= DATE_ADD( "
        + "       art.art_encounter, "
        + "          INTERVAL 33 day) "
        + "       GROUP  BY pa.patient_id) first_encounter "
        + "       ON first_encounter.patient_id = ee.patient_id "
        + "       WHERE  ee.voided = 0 "
        + "       AND ee.encounter_type = ${6} "
        + "       AND ee.encounter_datetime > first_encounter.encounter_date) first_and_second_encounter "
        + "               ON pt.patient_id = first_and_second_encounter.patient_id "
        + "       INNER JOIN encounter enc "
        + "               ON enc.patient_id = pt.patient_id "
        + "       INNER JOIN obs o "
        + "               ON o.encounter_id = enc.encounter_id "
        + "WHERE  pt.voided = 0 "
        + "       AND enc.voided = 0 "
        + "       AND o.voided = 0 "
        + "       AND enc.encounter_type = ${6} "
        + "       AND o.concept_id = ${1695} "
        + "       AND o.value_numeric IS NOT NULL "
        + "       AND enc.location_id = :location "
        + "GROUP  BY pt.patient_id";
  }

  /**
   * <b>RF14 - Data de registo de resultado do CD4 inicial - A.9 (Coluna I)</b>
   *
   * <p>A9- Resultado do CD4 Inicial: (coluna I) - Resposta = Resultado de CD4 inicial (RF15)
   *
   * <p>O sistema irá identificar o resultado do CD4 inicial registrado em “Investigações-
   * Resultados Laboratoriais" da primeira ou segunda Ficha Clínica ocorrido entre 0 e 33 dias
   * depois da Data de Início TARV, durante o período de inclusão.
   *
   * <p>Nota 1: no caso de existir mais de uma consulta clínica com resultado do CD4 o sistema vai
   * considerar o primeiro registo.
   *
   * <p>Nota 2: A “Data Início TARV” é definida no RF61.
   *
   * @return {DataDefinition}
   */
  public DataDefinition getCd4Result() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Resultado do CD4 inicial - A.9 (Coluna I)");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());

    String query =
        "SELECT pt.patient_id, "
            + "       o.value_numeric AS cd4_result "
            + "FROM   patient pt "
            + "       INNER JOIN (SELECT pa.patient_id, "
            + "                          Min(enc.encounter_datetime) AS encounter_date "
            + "                   FROM   patient pa "
            + "                          INNER JOIN encounter enc "
            + "                                  ON enc.patient_id = pa.patient_id "
            + "                          INNER JOIN obs "
            + "                                  ON obs.encounter_id = enc.encounter_id "
            + "                          INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + " ) art ON art.patient_id = enc.patient_id "
            + "                   WHERE  pa.voided = 0 "
            + "                          AND enc.voided = 0 "
            + "                          AND obs.voided = 0 "
            + "                          AND enc.encounter_type = ${6} "
            + "                          AND enc.location_id = :location "
            + "                          AND obs.concept_id = ${1695} "
            + "                          AND obs.value_numeric IS NOT NULL "
            + "                          AND enc.encounter_datetime >= art.art_encounter "
            + "                          AND enc.encounter_datetime <= DATE_ADD( "
            + "                              art.art_encounter, "
            + "                                                        INTERVAL 33 day) "
            + "                   GROUP  BY pa.patient_id "
            + "                   UNION "
            + "                   SELECT ee.patient_id, "
            + "                          Min(ee.encounter_datetime) AS encounter_date "
            + "                   FROM   encounter ee "
            + "                          INNER JOIN (SELECT pa.patient_id, "
            + "                                             Min(enc.encounter_datetime) AS "
            + "                                             encounter_date "
            + "                                      FROM   patient pa "
            + "                                             INNER JOIN encounter enc "
            + "                                                     ON enc.patient_id = "
            + "                                                        pa.patient_id "
            + "                                             INNER JOIN obs "
            + "                                                     ON obs.encounter_id = "
            + "                                                        enc.encounter_id "
            + "                                             INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + " ) art ON art.patient_id = enc.patient_id "
            + "       WHERE  pa.voided = 0 "
            + "       AND enc.voided = 0 "
            + "       AND obs.voided = 0 "
            + "       AND enc.encounter_type = ${6} "
            + "       AND enc.location_id = :location "
            + "       AND obs.concept_id = ${1695} "
            + "       AND obs.value_numeric IS NOT NULL "
            + "       AND enc.encounter_datetime >= art.art_encounter "
            + "       AND enc.encounter_datetime <= DATE_ADD( "
            + "       art.art_encounter, "
            + "          INTERVAL 33 day) "
            + "       GROUP  BY pa.patient_id) first_encounter "
            + "       ON first_encounter.patient_id = ee.patient_id "
            + "       WHERE  ee.voided = 0 "
            + "       AND ee.encounter_type = ${6} "
            + "       AND ee.encounter_datetime > first_encounter.encounter_date) first_and_second_encounter "
            + "               ON pt.patient_id = first_and_second_encounter.patient_id "
            + "       INNER JOIN encounter enc "
            + "               ON enc.patient_id = pt.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = enc.encounter_id "
            + "WHERE  pt.voided = 0 "
            + "       AND enc.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND enc.encounter_type = ${6} "
            + "       AND o.concept_id = ${1695} "
            + "       AND o.value_numeric IS NOT NULL "
            + "       AND enc.location_id = :location "
            + "GROUP  BY pt.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF17 - Data do pedido da 1a CV - B.1 (Coluna J)</b>
   *
   * <p>O sistema irá determinar a Data do Pedido da CV nos 1˚s 12 meses de TARV, identificando a
   * data da consulta clínica (Ficha Clínica), na qual foi efectuado o registo do Pedido de Carga
   * Viral, durante os primeiros 12 meses de TARV, ou seja, “Data Consulta Pedido CV”>=”Data Início
   * TARV” e <= “Data Início TARV”+12 meses.<br>
   * <br>
   *
   * <p>Nota 1: A “Data Início TARV” é definida no RF61. <br>
   * <br>
   *
   * <p>Nota 2: O utente a ser considerado nesta definição iniciou TARV ou na coorte de 12 meses ou
   * na coorte de 24 meses ou na coorte de 36 meses conforme definido no RF4.
   *
   * <p>Nota 3: Em caso de existência de mais de uma consulta clínica com registo de pedido de CV,
   * durante os primeiros 12 meses do TARV, deve ser listado o registo mais recente/último.
   *
   * @return {DataDefinition}
   */
  public DataDefinition getLastViralLoadRequestOnTheFirst12MonthsOfTarv() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Data do pedido da CV nos 1˚s  12 meses de TARV");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());

    String query =
        "SELECT p.patient_id, "
            + "                    MAX(e.encounter_datetime) AS encounter_date "
            + "             FROM   patient p "
            + "                    INNER JOIN encounter e "
            + "                            ON e.patient_id = p.patient_id "
            + "                    INNER JOIN obs o "
            + "                            ON o.encounter_id = e.encounter_id "
            + "                    INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + " ) art ON art.patient_id = p.patient_id "
            + "       WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.encounter_type = ${6} "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${23722} "
            + "       AND o.value_coded = ${856} "
            + "       AND e.encounter_datetime >= art.art_encounter "
            + "       AND e.encounter_datetime <= DATE_ADD(art.art_encounter, INTERVAL 12 MONTH) "
            + "             GROUP  BY p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>Data do pedido da CV de seguimento- C.1 (coluna AP)</b>
   *
   * <p>O sistema irá determinar a Data do Pedido da CV da CV entre 12º e 24º mês de TARV
   * identificando a consulta clínica (Ficha Clínica), com registo do Pedido de Carga Viral, e
   * ocorrida entre 12º e 24º mês de TARV ou seja, “Data Consulta Pedido CV ” > “Data Início TARV” +
   * 12 meses e <= “Data Início TARV” + 24 meses <br>
   * <br>
   *
   * <p>Nota A “Data Início TARV” é definida no RF61 <br>
   * <br>
   *
   * <p>Nota 2: O utente a ser considerado nesta definição iniciou TARV ou na coorte de 12 meses ou
   * na coorte de 24 meses ou na coorte de 36 meses, conforme definido no RF4.
   *
   * <p>Nota 3: Em caso de existência de mais de uma consulta clínica com registo de pedido de CV,
   * entre 12º e 24º mês de TARV, deve ser listado o registo mais recente/último.
   *
   * @return {DataDefinition}
   */
  public DataDefinition getLastViralLoadOnThePeriod(
      int minNumberOfMonths,
      int maxNumberOfMonths,
      int minCohortNumberOfYears,
      int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Data do pedido da CV de entre 12º e 24º mês de TARV");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "                   SELECT p.patient_id, "
            + "                          MAX(e.encounter_datetime) AS encounter_date "
            + "                   FROM   patient p "
            + "                            INNER JOIN encounter e "
            + "                              ON e.patient_id = p.patient_id "
            + "                            INNER JOIN obs o "
            + "                              ON o.encounter_id = e.encounter_id "
            + "                          INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + " ) art ON art.patient_id = p.patient_id "
            + "       WHERE  p.voided = 0 "
            + "       AND  e.voided = 0 "
            + "       AND  o.voided = 0 "
            + "       AND  e.encounter_type = ${6} "
            + "       AND  e.encounter_datetime > DATE_ADD(art.art_encounter, INTERVAL "
            + minNumberOfMonths
            + " MONTH ) "
            + "       AND  e.encounter_datetime <= DATE_ADD(art.art_encounter, INTERVAL "
            + maxNumberOfMonths
            + " MONTH ) "
            + "       AND o.concept_id = ${23722} "
            + "       AND o.value_coded = ${856} "
            + " AND p.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                maxCohortNumberOfYears, minCohortNumberOfYears)
            + " ) "
            + "       GROUP BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF18 - Data do resultado da 1a CV- B.2 (Coluna K)</b>
   *
   * <p>O sistema irá determinar a Data do Resultado da CV nos 1os 12 meses do TARV, identificando a
   * data da primeira consulta clínica (Ficha Clínica), na qual foi efectuado o registo do resultado
   * da Carga Viral, durante os primeiros 12 meses do início TARV, ou seja, “Data Consulta Resultado
   * CV”>=”Data Início TARV” e <= “Data Início TARV”+12 meses.<br>
   * <br>
   *
   * <p>Nota 1: A “Data Início TARV” é definida no RF61. <br>
   * <br>
   *
   * <p>Nota 2: O utente a ser considerado nesta definição iniciou TARV ou na coorte de 12 meses ou
   * na coorte de 24 meses ou na coorte de 36 meses conforme definido no RF4.
   *
   * <p>Nota 3: Em caso de existência de mais de uma consulta clínica com registo de resultado de
   * CV, durante os primeiros 12 meses, deve ser listada a data de consulta mais recente/última.
   *
   * @return {DataDefinition}
   */
  public DataDefinition getFirstViralLoadResultDate() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("B2- Data de registo do resultado da 1ª CV");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());

    String query = getLastVlDateOn1st12MonthsOfTarv();

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  private String getLastVlDateOn1st12MonthsOfTarv() {
    return "SELECT     p.patient_id, "
        + "           MAX(e.encounter_datetime) AS last_vl_date  "
        + "FROM       patient p "
        + "INNER JOIN encounter e "
        + "ON         e.patient_id = p.patient_id "
        + "INNER JOIN obs o "
        + "ON         o.encounter_id = e.encounter_id "
        + "INNER JOIN "
        + "           ( "
        + "                           SELECT art_patient.patient_id, "
        + "                                  art_patient.first_pickup AS art_encounter "
        + "                           FROM   ( "
        + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
        + "                           ) art_patient "
        + "                     ) art ON art.patient_id = p.patient_id "
        + "       WHERE  p.voided = 0 "
        + "       AND e.voided = 0 "
        + "       AND o.voided = 0 "
        + "AND      e.encounter_type = ${6} "
        + "AND        ( ( "
        + "                                 o.concept_id= ${856} "
        + "                         AND     o.value_numeric IS NOT NULL ) "
        + "           OR         ( "
        + "                                 o.concept_id = ${1305} "
        + "                      AND        o.value_coded IS NOT NULL)) "
        + "AND        e.location_id = :location "
        + "AND        e.encounter_datetime >= art.art_encounter "
        + "AND        e.encounter_datetime <= DATE_ADD(art.art_encounter, INTERVAL 12 MONTH) "
        + "AND        p.voided = 0 "
        + "AND        e.voided = 0 "
        + "AND        o.voided = 0 "
        + "GROUP BY   p.patient_id";
  }

  /**
   * <b>RF33 - Data de registo Registo do Resultado da CV entre 12º e 24º mês do TARV</b>
   *
   * <p>O sistema irá determinar a Data do Resultado da CV de seguimento do utente identificando a
   * data da consulta clínica (Ficha Clínica), na qual foi efectuado o registo do resultado da Carga
   * Viral, e ocorrida entre 12º e 24º mês de TARV, ou seja, “Data Consulta Resultado CV” > “Data
   * Início TARV” + 12 meses e <= “Data Início TARV” + 24 meses.<br>
   * <br>
   *
   * <p>Nota 1: A “Data Início TARV” é definida no RF61. <br>
   * <br>
   *
   * <p>Nota 2: O utente a ser considerado nesta definição iniciou TARV ou na coorte de 12 meses ou
   * na coorte de 24 meses, conforme definido no RF4.
   *
   * <p>Nota 3: Em caso de existência de mais de uma consulta clínica com registo de resultado de
   * CV, entre 12º e 24º meses de TARV, deve ser listada a data de consulta mais recente/última.
   *
   * @return {DataDefinition}
   */
  public DataDefinition getLastViralLoadResultDateBetweenPeriodsInMonthsAfterTarv(
      int minNumberOfMonths,
      int maxNumberOfMonths,
      int minCohortNumberOfYears,
      int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(
        "C2 - Data de registo do resultado da entre 12º e 24º mês do TARV");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        getLastVlDateBetweenPeriods(
            minNumberOfMonths, maxNumberOfMonths, minCohortNumberOfYears, maxCohortNumberOfYears);

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  private String getLastVlDateBetweenPeriods(
      int minNumberOfMonths,
      int maxNumberOfMonths,
      int minCohortNumberOfYears,
      int maxCohortNumberOfYears) {
    return " SELECT     p.patient_id, "
        + "           MAX(e.encounter_datetime) AS last_vl_date  "
        + "FROM       patient p "
        + "INNER JOIN encounter e "
        + "ON         e.patient_id = p.patient_id "
        + "INNER JOIN obs o "
        + "ON         o.encounter_id = e.encounter_id "
        + "                          INNER JOIN ( "
        + "                           SELECT art_patient.patient_id, "
        + "                                  art_patient.first_pickup AS art_encounter "
        + "                           FROM   ( "
        + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
        + "                           ) art_patient "
        + " ) art ON art.patient_id = p.patient_id "
        + "       WHERE  p.voided = 0 "
        + "       AND  e.voided = 0 "
        + "       AND  o.voided = 0 "
        + "       AND  e.encounter_type = ${6} "
        + "       AND  e.encounter_datetime > DATE_ADD(art.art_encounter, INTERVAL "
        + minNumberOfMonths
        + " MONTH ) "
        + "       AND  e.encounter_datetime <= DATE_ADD(art.art_encounter, INTERVAL "
        + maxNumberOfMonths
        + " MONTH ) "
        + "AND        e.location_id = :location "
        + "AND        ( ( "
        + "                                 o.concept_id= ${856} "
        + "                         AND     o.value_numeric IS NOT NULL ) "
        + "           OR         ( "
        + "                                 o.concept_id = ${1305} "
        + "                      AND        o.value_coded IS NOT NULL)) "
        + " AND p.patient_id IN ( "
        + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
            minCohortNumberOfYears, maxCohortNumberOfYears)
        + " ) "
        + "GROUP BY   p.patient_id";
  }

  /**
   * <b>RF18 - Data do resultado da ultima CV- B.2 (Coluna K)</b>
   *
   * <p>O sistema irá determinar o Resultado da 1ª Carga Viral do utente seleccionando o primeiro
   * resultado de Carga Viral registado na primeira consulta clínica (Ficha Clínica) após o início
   * TARV (Data Início TARV). <br>
   * <br>
   *
   * <p>Nota 1: A “Data Início TARV” é definida no RF61. <br>
   * <br>
   *
   * <p>Nota 2: O utente a ser considerado nesta definição iniciou TARV ou na coorte de 12 meses ou
   * na coorte de 24 meses ou na coorte de 36 meses conforme definido no RF4.
   *
   * @return {DataDefinition}
   */
  public DataDefinition getLastViralLoadResultOnThe1st12MonthsOfTarv() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("B3- Resultado da ultima CV");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());

    String query =
        "SELECT     p.patient_id, "
            + "     IF(concept_id= ${856}, o.value_numeric, IF(o.value_coded = 165331, CONCAT('MENOR QUE ',o.comments), o.value_coded)) AS first_vl_result  "
            + "FROM       patient p "
            + "INNER JOIN encounter e "
            + "ON         e.patient_id = p.patient_id "
            + "INNER JOIN obs o "
            + "ON         o.encounter_id = e.encounter_id "
            + "INNER JOIN "
            + "           ( "
            + getLastVlDateOn1st12MonthsOfTarv()
            + "                     ) last_vl ON last_vl.patient_id = p.patient_id "
            + "       WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "AND      e.encounter_type = ${6} "
            + "AND        ( ( "
            + "                                 o.concept_id= ${856} "
            + "                         AND     o.value_numeric IS NOT NULL ) "
            + "           OR         ( "
            + "                                 o.concept_id = ${1305} "
            + "                      AND        o.value_coded IS NOT NULL)) "
            + "AND        e.location_id = :location "
            + "AND        e.encounter_datetime = last_vl.last_vl_date "
            + "AND        p.voided = 0 "
            + "AND        e.voided = 0 "
            + "AND        o.voided = 0 "
            + "GROUP BY   p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF34 - Resultado da CV de seguimento : (coluna AR)</b>
   *
   * <p>O sistema irá determinar o Resultado da CV de seguimento do utente seleccionando o segundo
   * resultado de Carga Viral registado na consulta clínica (Ficha Clínica) após o início TARV (Data
   * Início TARV). <br>
   * <br>
   *
   * <p>Nota 1: A “Data Início TARV” é definida no RF61. <br>
   * <br>
   *
   * <p>Nota 2: O utente a ser considerado nesta definição iniciou TARV ou na coorte de 12 meses ou
   * na coorte de 24 meses, conforme definido no RF4.
   *
   * @return {DataDefinition}
   */
  public DataDefinition getSecondViralLoadResultBetweenPeriodsOfMonthsAfterTarv(
      int firstMonth, int lastMonth, int minCohortNumberOfYears, int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("C3- Resultado da CV entre 12º e 24º mês do TARV ");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "SELECT     p.patient_id, "
            + "     IF(o.concept_id= ${856}, o.value_numeric, IF(o.value_coded = 165331, CONCAT('MENOR QUE ',o.comments), o.value_coded)) AS first_vl_result  "
            + "FROM       patient p "
            + "INNER JOIN encounter e "
            + "ON         e.patient_id = p.patient_id "
            + "INNER JOIN obs o "
            + "ON         o.encounter_id = e.encounter_id "
            + "INNER JOIN "
            + "           ( "
            + getLastVlDateBetweenPeriods(firstMonth, lastMonth, 4, 2)
            + "                     ) last_vl ON last_vl.patient_id = p.patient_id "
            + "       WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "AND      e.encounter_type = ${6} "
            + "AND        ( ( "
            + "                                 o.concept_id= ${856} "
            + "                         AND     o.value_numeric IS NOT NULL ) "
            + "           OR         ( "
            + "                                 o.concept_id = ${1305} "
            + "                      AND        o.value_coded IS NOT NULL)) "
            + "AND        e.location_id = :location "
            + "AND        e.encounter_datetime = last_vl.last_vl_date "
            + "AND        p.voided = 0 "
            + "AND        e.voided = 0 "
            + "AND        o.voided = 0 "
            + " AND p.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) "
            + "GROUP BY   p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF20 - Resultado do 2˚ CD4 (2˚ CD4 feito nos 1˚s 12 meses de TARV) - B.4 (Coluna M)</b>
   *
   * <p>O sistema irá determinar o Resultado do 2º CD4 do utente seleccionando o segundo resultado
   * do CD4 registado na consulta Clínica (Ficha Clínica) e este resultado deve estar entre 33 dias
   * e 12 meses do Início TARV (“Data da Consulta com resultado CD4” >= “Data Início TARV” + 33 dias
   * e <= “Data Início TARV” + 12 meses).
   *
   * <p>Nota 1: A “Data Início TARV” é definida no RF61. <br>
   * <br>
   *
   * <p>Nota 2: O utente a ser considerado nesta definição iniciou TARV ou na coorte de 12 meses ou
   * na coorte de 24 meses ou na coorte de 36 meses conforme definido no RF4.
   *
   * @return {DataDefinition}
   */
  public DataDefinition getSecondCd4Result() {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(
        "Resultado do 2˚ CD4 (2˚ CD4 feito nos 1˚s 12 meses de TARV) - B.4 (Coluna M)");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());

    String query =
        "                   SELECT cd4_2.patient_id, "
            + "                          obs2.value_numeric AS second_cd4_result "
            + "                   FROM   patient cd4_2 "
            + "                          INNER JOIN encounter enc2 "
            + "                                 ON enc2.patient_id = cd4_2.patient_id "
            + "                          INNER JOIN obs obs2 "
            + "                                 ON enc2.encounter_id = obs2.encounter_id "
            + "                          INNER JOIN ( "
            + "                   SELECT cd4.patient_id, "
            + "                         MIN(enc2.encounter_datetime)  AS second_cd4_date "
            + "                   FROM   patient cd4 "
            + "                          INNER JOIN encounter enc2 "
            + "                                 ON enc2.patient_id = cd4.patient_id "
            + "                          INNER JOIN obs obs2 "
            + "                                 ON enc2.encounter_id = obs2.encounter_id "
            + "                          INNER JOIN ( "
            + getFirstCd4ResultDateQuery()
            + " ) first_cd4_date "
            + "       ON first_cd4_date.patient_id = cd4.patient_id "
            + "       WHERE  cd4.voided = 0 "
            + "       AND enc2.voided = 0"
            + "       AND obs2.voided = 0"
            + "       AND enc2.location_id = :location "
            + "       AND enc2.encounter_datetime > first_cd4_date.encounter_date  "
            + "       AND enc2.encounter_datetime <= :endDate  "
            + "       AND enc2.encounter_type = ${6} "
            + "       AND obs2.concept_id = ${1695} "
            + "       AND obs2.value_numeric IS NOT NULL "
            + "       GROUP BY cd4.patient_id "
            + " ) second_cd4_result "
            + "       ON second_cd4_result.patient_id = cd4_2.patient_id "
            + "       WHERE  cd4_2.voided = 0 "
            + "       AND enc2.voided = 0"
            + "       AND obs2.voided = 0"
            + "       AND enc2.location_id = :location "
            + "       AND enc2.encounter_datetime = second_cd4_result.second_cd4_date  "
            + "       AND enc2.encounter_type = ${6} "
            + "       AND obs2.concept_id = ${1695} "
            + "       AND obs2.value_numeric IS NOT NULL "
            + "       GROUP BY cd4_2.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF35 - Resultado do CD4 feito entre 12˚ e 24˚ mês de TARV- C.4 (Coluna AS)</b>
   *
   * <p>O sistema irá determinar o Resultado do CD4 do utente seleccionando o resultado do CD4 mais
   * recente registado na consulta Clínica (Ficha Clínica) entre 12 e 24 meses do Início TARV (“Data
   * da Consulta” <= “Data Início TARV” + 12 meses e >= “Data Início TARV” + 24 meses). .
   *
   * <p>Nota 1: Nota 1: A “Data Início TARV” é definida no RF61. <br>
   * <br>
   *
   * <p>Nota 2: O utente a ser considerado nesta definição iniciou TARV ou na coorte de 12 meses ou
   * na coorte de 24 meses, conforme definido no RF4.
   *
   * @return {DataDefinition}
   */
  public DataDefinition getCd4ResultSectionC(
      int minNumberOfMonths,
      int maxNumberOfMonths,
      int minCohortNumberOfYears,
      int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Resultado do CD4 feito entre 12˚ e 24˚ mês de TARV");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));

    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "       SELECT cd4.patient_id, "
            + "         obs.value_numeric AS cd4_result "
            + "  FROM   patient cd4 "
            + "         INNER JOIN encounter enc "
            + "                ON enc.patient_id = cd4.patient_id "
            + "         INNER JOIN obs  "
            + "                ON enc.encounter_id = obs.encounter_id "
            + "      INNER JOIN (SELECT pa.patient_id, "
            + "    MAX(enc.encounter_datetime) AS "
            + "                encounter_date "
            + "    FROM   patient pa "
            + "           INNER JOIN encounter enc "
            + "                   ON enc.patient_id = "
            + "                      pa.patient_id "
            + "           INNER JOIN obs "
            + "                   ON obs.encounter_id = "
            + "                      enc.encounter_id "
            + "           INNER JOIN ( "
            + "     SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + " ) art ON art.patient_id = enc.patient_id "
            + "       WHERE  pa.voided = 0 "
            + "       AND enc.voided = 0 "
            + "       AND obs.voided = 0 "
            + "       AND enc.encounter_type = ${6} "
            + "       AND enc.location_id = :location "
            + "       AND obs.concept_id = ${1695} "
            + "       AND obs.value_numeric IS NOT NULL "
            + "       AND enc.encounter_datetime > DATE_ADD(art.art_encounter, INTERVAL "
            + minNumberOfMonths
            + " MONTH ) "
            + "       AND enc.encounter_datetime <= DATE_ADD(art.art_encounter, INTERVAL "
            + maxNumberOfMonths
            + " MONTH ) "
            + "       GROUP  BY pa.patient_id) most_recent_cd4 "
            + "       ON most_recent_cd4.patient_id = cd4.patient_id "
            + "       WHERE  cd4.voided = 0 "
            + "       AND enc.voided = 0 "
            + "       AND obs.voided = 0 "
            + "       AND enc.encounter_type = ${6} "
            + "       AND enc.encounter_datetime = most_recent_cd4.encounter_date "
            + "       AND obs.concept_id = ${1695} "
            + "       AND obs.value_numeric IS NOT NULL "
            + " AND cd4.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) "
            + "       GROUP BY cd4.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF21 - Teve registo de boa adesão em TODAS consultas entre 1˚ e 3˚ mês de TARV? - B.5
   * (Coluna N)</b><br>
   * <br>
   *
   * <p>O sistema irá determinar se o utente “Teve registo de boa adesão em TODAS consultas entre 1˚
   * e 3˚ mês de TARV?” com as seguintes respostas:
   *
   * <p>Resposta= Sim, se o utente teve o registo do campo de "Seguimento da Adesão - Adesão ao
   * TARV” com resposta = “BOA” em todas as consultas de APSS/PP decorridas entre 33 dias e 3 meses
   * do Início TARV (“Data da Consulta APSS/PP” >= “Data Início TARV” + 33 dias e <= “Data Início
   * TARV” + 3 meses) * <br>
   * <br>
   *
   * <p>Resposta= Não, se o utente teve pelo menos um registo no campo de "Seguimento da Adesão -
   * Adesão ao TARV” com resposta = “RISCO” ou “MÁ” decorrida entre 33 dias e 3 meses do Início TARV
   * (“Data da Consulta APSS/PP” >= “Data Início TARV” + 33 dias e <= “Data Início TARV” + 3 meses)
   * <br>
   * <br>
   *
   * <p>Nota 1: Caso o utente não satisfaça o critério definido para resposta = Sim e resposta =
   * Não, o sistema não irá preencher nenhuma informação. * <br>
   * <br>
   *
   * <p>Nota 2: A “Data Início TARV” é definida no RF61. <br>
   * <br>
   *
   * <p>Nota 3: O utente a ser considerado nesta definição iniciou TARV ou na coorte de 12 meses ou
   * na coorte de 24 meses ou na coorte de 36 meses conforme definido no RF4.
   *
   * @return {DataDefinition}
   */
  public DataDefinition getPatientsWithGoodAdhesion(
      boolean b5Orc5,
      int minNumberOfMonths,
      int maxNumberOfMonths,
      int minCohortNumberOfYears,
      int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(
        "Teve registo de boa adesão em TODAS consultas entre 1˚ e 3˚ mês de TARV?; (coluna N) – Resposta = Sim ou Não");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());
    map.put("6223", hivMetadata.getAdherenceEvaluationConcept().getConceptId());
    map.put("1383", hivMetadata.getPatientIsDead().getConceptId());
    map.put("1749", hivMetadata.getArvAdherenceRiskConcept().getConceptId());
    map.put("1385", hivMetadata.getBadConcept().getConceptId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "SELECT p.patient_id, "
            + "       CASE "
            + "           WHEN ( consultation_tb.tb_consultations = "
            + "                  good_consultations.good_adhesion ) "
            + "               THEN 'Sim' "
            + "           WHEN ( bad_consultations.bad_adhesion ) "
            + "               THEN 'Não' "
            + "          ELSE '' "
            + "           END AS adhesion "
            + "FROM   patient p "
            + "           INNER JOIN (SELECT e.patient_id, "
            + "                              Count(e.encounter_id) AS tb_consultations "
            + "                       FROM   encounter e "
            + "                                  INNER JOIN (SELECT starv.patient_id, "
            + "                                                     starv.first_pickup AS art_encounter "
            + "                                              FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                                                      ) starv) tarv "
            + "                                             ON tarv.patient_id = e.patient_id "
            + "                       WHERE  e.voided = 0 "
            + "                         AND e.encounter_type = ${35} "
            + "                         AND e.location_id =:location ";

    String b5OrC5 =
        b5Orc5
            ? "                         AND e.encounter_datetime >= DATE_ADD( tarv.art_encounter, INTERVAL 33 DAY) "
                + "                         AND e.encounter_datetime <= DATE_ADD( tarv.art_encounter, INTERVAL 3 MONTH) "
            : " AND        e.encounter_datetime > DATE_ADD( tarv.art_encounter, INTERVAL "
                + minNumberOfMonths
                + " MONTH ) "
                + " AND        e.encounter_datetime <= DATE_ADD( tarv.art_encounter, INTERVAL "
                + maxNumberOfMonths
                + " MONTH ) ";
    query += b5OrC5;

    query +=
        "                       GROUP  BY e.patient_id) consultation_tb "
            + "                      ON consultation_tb.patient_id = p.patient_id "
            + "           LEFT JOIN (SELECT e.patient_id, "
            + "                              Count(e.encounter_id) AS good_adhesion "
            + "                       FROM   encounter e "
            + "                                  INNER JOIN obs o "
            + "                                             ON o.encounter_id = e.encounter_id "
            + "                                  INNER JOIN (SELECT starv.patient_id, "
            + "                                                     starv.first_pickup AS art_encounter "
            + "                                              FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                                                      ) starv) tarv "
            + "                                             ON tarv.patient_id = e.patient_id "
            + "                       WHERE  e.voided = 0 "
            + "                         AND o.voided = 0 "
            + "                         AND e.encounter_type = ${35} "
            + "                         AND e.location_id = :location ";
    query += b5OrC5;

    query +=
        "                         AND        o.concept_id = ${6223} "
            + "                         AND        o.value_coded IN ( ${1383} ) "
            + "                       GROUP  BY e.patient_id) good_consultations "
            + "                      ON good_consultations.patient_id = p.patient_id "
            + "           LEFT JOIN (SELECT e.patient_id, "
            + "                              Count(e.encounter_id) AS bad_adhesion "
            + "                       FROM   encounter e "
            + "                                  INNER JOIN obs o "
            + "                                             ON o.encounter_id = e.encounter_id "
            + "                                  INNER JOIN (SELECT starv.patient_id, "
            + "                                                     starv.first_pickup AS art_encounter "
            + "                                              FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                                                      ) starv) tarv "
            + "                                             ON tarv.patient_id = e.patient_id "
            + "                       WHERE  e.voided = 0 "
            + "                         AND o.voided = 0 "
            + "                         AND e.encounter_type = ${35} "
            + "                         AND e.location_id = :location ";
    query += b5OrC5;

    query +=
        "                         AND        o.concept_id = ${6223} "
            + "                         AND        o.value_coded IN ( ${1749}, "
            + "                                                       ${1385} ) "
            + "                       GROUP  BY e.patient_id) bad_consultations "
            + "                      ON bad_consultations.patient_id = p.patient_id "
            + "WHERE  p.voided = 0"
            + " AND p.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF22 - Esteve grávida ou foi lactante entre 3˚ e 9º mês de TARV?- B.6 (Coluna O)</b><br>
   * <br>
   *
   * <p>O sistema irá identificar se o utente “Esteve grávida ou foi lactante entre 3˚ e 9º mês de
   * TARV?” com as seguintes respostas:
   *
   * <p>Resposta= Sim, se o utente é do sexo feminino, com idade > 9 anos (RF10) e que teve um
   * registo como “Grávida=G” ou “Lactante=L” numa consulta clínica (“Ficha Clinica”) decorrida
   * entre 3 meses a 9 meses do início TARV (Data da Consulta >= “Data Início TARV” + 3 meses e <=
   * “Data Início TARV” + 9 meses) <br>
   * <br>
   *
   * <p>Resposta= Não, se o utente é do sexo feminino, com idade > 9 anos (RF10) e que não teve um
   * registo como “Grávida=G” ou “Lactante=L” numa consulta clínica (“Ficha Clinica”) decorrida
   * entre 3 meses a 9 meses do início TARV (Data da Consulta >= “Data Início TARV” + 3 meses e <=
   * “Data Início TARV” + 9 meses) <br>
   * <br>
   *
   * <p>Nota 1: Caso o utente não satisfaça o critério definido para resposta = Sim e resposta =
   * Não, o sistema não irá preencher nenhuma informação. <br>
   * <br>
   *
   * <p>Nota 2: A “Data Início TARV” é definida no RF61 <br>
   * <br>
   *
   * @return {DataDefinition}
   */
  public DataDefinition getPatientsPregnantBreastfeeding3MonthsTarv(
      int minNumberOfMonths,
      int maxNumberOfMonths,
      boolean b6Period,
      int minCohortNumberOfYears,
      int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(
        "B6- Esteve grávida ou foi lactante entre 3˚ e 9º mês de TARV?: (coluna M)- Resposta = Sim ou Não");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("6332", hivMetadata.getBreastfeeding().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "SELECT final_query.person_id, "
            + "       'Sim' "
            + "FROM   (SELECT p.person_id "
            + "        FROM   person p "
            + "                   INNER JOIN encounter e "
            + "                              ON e.patient_id = p.person_id "
            + "                   INNER JOIN obs o "
            + "                              ON o.encounter_id = e.encounter_id "
            + "                   INNER JOIN obs o2 "
            + "                              ON o2.encounter_id = e.encounter_id "
            + "                   INNER JOIN (SELECT art_patient.patient_id, "
            + "                                      art_patient.first_pickup AS art_encounter "
            + "                               FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                                       ) art_patient ) art "
            + "                              ON art.patient_id = p.person_id "
            + "        WHERE  p.voided = 0 "
            + "          AND e.voided = 0 "
            + "          AND o.voided = 0 "
            + "          AND o2.voided = 0 "
            + "          AND e.encounter_type = ${6} "
            + "          AND e.location_id = :location "
            + "          AND p.gender = 'F' "
            + "          AND Timestampdiff(year, p.birthdate, art.art_encounter) > 9 "
            + "          AND o.concept_id IN ( ${1982}, ${6332} ) "
            + "          AND o.value_coded = ( ${1065} ) ";
    query +=
        b6Period
            ? "          AND e.encounter_datetime >= "
                + "            Date_add(art.art_encounter, INTERVAL "
                + minNumberOfMonths
                + " MONTH ) "
                + "          AND e.encounter_datetime <= "
                + "            Date_add(art.art_encounter, INTERVAL "
                + maxNumberOfMonths
                + " MONTH ) "
            : "          AND e.encounter_datetime > "
                + "            Date_add(art.art_encounter, INTERVAL "
                + minNumberOfMonths
                + " MONTH ) "
                + "          AND e.encounter_datetime <= "
                + "            Date_add(art.art_encounter, INTERVAL "
                + maxNumberOfMonths
                + " MONTH ) ";
    query +=
        "        GROUP  BY p.person_id) final_query "
            + " UNION "
            + "SELECT final_query.person_id, "
            + "       'Não' "
            + "FROM   (SELECT p.person_id "
            + "        FROM   person p "
            + "                   INNER JOIN encounter e "
            + "                              ON e.patient_id = p.person_id "
            + "                   INNER JOIN obs o "
            + "                              ON o.encounter_id = e.encounter_id "
            + "                   INNER JOIN obs o2 "
            + "                              ON o2.encounter_id = e.encounter_id "
            + "                   INNER JOIN (SELECT art_patient.patient_id, "
            + "                                      art_patient.first_pickup AS art_encounter "
            + "                               FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                                       ) art_patient ) art "
            + "                              ON art.patient_id = p.person_id "
            + "        WHERE  p.voided = 0 "
            + "          AND e.voided = 0 "
            + "          AND o.voided = 0 "
            + "          AND o2.voided = 0 "
            + "          AND e.encounter_type = ${6} "
            + "          AND e.location_id = :location "
            + "          AND p.gender = 'F' "
            + "          AND Timestampdiff(year, p.birthdate, art.art_encounter) > 9 "
            + "          AND o.concept_id IN ( ${1982}, ${6332} ) "
            + "          AND o.value_coded = ( ${1065} ) ";
    query +=
        b6Period
            ? "          AND e.encounter_datetime NOT BETWEEN "
                + "            Date_add(art.art_encounter, INTERVAL "
                + minNumberOfMonths
                + " MONTH ) "
                + " AND "
                + "            Date_add(art.art_encounter, INTERVAL "
                + maxNumberOfMonths
                + " MONTH ) "
            : "          AND e.encounter_datetime NOT BETWEEN "
                + "            Date_add(Date_add(art.art_encounter, INTERVAL "
                + minNumberOfMonths
                + " MONTH ), INTERVAL 1 DAY) "
                + "          AND "
                + "            Date_add(art.art_encounter, INTERVAL "
                + maxNumberOfMonths
                + " MONTH ) ";
    query +=
        "        GROUP  BY p.person_id) final_query "
            + " WHERE final_query.person_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF23 - Teve TB entre o 3˚ e 9 mês de TARV- B.8 (Coluna Q)</b><br>
   * <br>
   *
   * <p>O sistema irá determinar se o utente “Teve TB entre o 3˚ e 9 mês de TARV? com as seguintes
   * respostas:
   *
   * <p>Resposta= Sim, se o utente teve um dos seguintes registos em pelo menos uma consulta clínica
   * (Ficha Clínica) decorrida entre 3 a 9 meses após o início TARV (Data da Consulta >= “Data
   * Início TARV” + 3 meses e <= “Data Início TARV” + 9 meses): “Tem Sintomas=” com resposta = “Sim”
   * ou “Quais Sintomas de TB?” com resposta = “Febre” ou “Emagrecimento” ou "Sudorese noturna” ou
   * “Tosse com ou sem sangue” ou “Astenia” ou “Contacto recente com TB” ou “Diagnóstico de TB
   * Activa?” com resposta= SIM <br>
   * <br>
   *
   * <p>Resposta= Não, se o utente não teve nenhum dos seguintes registos numa consulta clínica
   * (Ficha Clínica) decorrida entre 3 a 9 meses após o início TARV (Data da Consulta >= “Data
   * Início TARV” + 3 meses e <= “Data Início TARV” + 9 meses): “Tem Sintomas=” com resposta = “Sim”
   * ou “Quais Sintomas de TB?” com resposta = “Febre” ou “Emagrecimento” ou "Sudorese noturna” ou
   * “Tosse com ou sem sangue” ou “Astenia” ou “Contacto recente com TB” “Diagnóstico de TB Activa?”
   * com resposta= SIM <br>
   * <br>
   *
   * <p>Nota 1: caso o utente não satisfaça o critério definido para resposta = Sim e resposta =
   * Não, o sistema não irá preencher nenhuma informação.<br>
   * <br>
   *
   * <p>Nota 2: A “Data Início TARV” é a data registada na Ficha Resumo (“Data do Início TARV”).
   * <br>
   * <br>
   *
   * <p>Nota 3: O utente a ser considerado nesta definição iniciou TARV ou na coorte de 12 meses ou
   * na coorte de 24 meses, conforme definido no RF4.
   *
   * @return {DataDefinition}
   */
  public DataDefinition getPatientsWithTbThirdToNineMonth(
      int minNumberOfMonths,
      int maxNumberOfMonths,
      boolean b8Period,
      int minCohortNumberOfYears,
      int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(
        "B8-Teve TB nos 1˚s 12 meses de TARV: (coluna Q) - Resposta = Sim ou Não (RF23)");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("23761", hivMetadata.getActiveTBConcept().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("1268", hivMetadata.getTBTreatmentPlanConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("1257", hivMetadata.getContinueRegimenConcept().getConceptId());
    map.put("1267", hivMetadata.getCompletedConcept().getConceptId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "SELECT final_query.patient_id, "
            + "       'Sim' "
            + "FROM   ( "
            + " SELECT     p.patient_id "
            + " FROM       patient p "
            + "     INNER JOIN encounter e "
            + "     ON         e.patient_id = p.patient_id "
            + "     INNER JOIN obs o "
            + "     ON         o.encounter_id = e.encounter_id "
            + "     INNER JOIN obs o2 "
            + "     ON         o2.encounter_id = e.encounter_id "
            + "     INNER JOIN ( "
            + "              SELECT art_patient.patient_id, "
            + "                     art_patient.first_pickup AS art_encounter "
            + "              FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                      ) art_patient "
            + "                 ) art "
            + "                  ON         art.patient_id = p.patient_id "
            + " WHERE      p.voided = 0 "
            + " AND        e.voided = 0 "
            + " AND        o.voided = 0 "
            + " AND        o2.voided = 0 "
            + " AND        e.encounter_type = ${6} "
            + " AND        e.location_id = :location ";
    query +=
        b8Period
            ? " AND        e.encounter_datetime >= "
                + " date_add( art.art_encounter, INTERVAL "
                + minNumberOfMonths
                + " MONTH ) "
                + " AND        e.encounter_datetime <= "
                + " date_add( art.art_encounter, INTERVAL "
                + maxNumberOfMonths
                + " MONTH ) "
            : " AND        e.encounter_datetime > "
                + " date_add( art.art_encounter, INTERVAL "
                + minNumberOfMonths
                + " MONTH ) "
                + " AND        e.encounter_datetime <= "
                + " date_add( art.art_encounter, INTERVAL "
                + maxNumberOfMonths
                + " MONTH ) ";
    query +=
        " AND    (   ( o.concept_id = ${23761} "
            + "              AND o.value_coded IN ( ${1065} ) ) "
            + " OR         ( o2.concept_id = ${1268} "
            + "                AND  o2.value_coded IN ( ${1256}, ${1257} ) ) ) "
            + " GROUP BY   p.patient_id ) AS final_query "
            + " UNION "
            + "SELECT final_query.patient_id, "
            + "       'Não' "
            + "FROM   ( "
            + " SELECT     p.patient_id "
            + " FROM       patient p "
            + "     INNER JOIN encounter e "
            + "     ON         e.patient_id = p.patient_id "
            + "     INNER JOIN obs o "
            + "     ON         o.encounter_id = e.encounter_id "
            + "     INNER JOIN obs o2 "
            + "     ON         o2.encounter_id = e.encounter_id "
            + "     INNER JOIN ( "
            + "              SELECT art_patient.patient_id, "
            + "                     art_patient.first_pickup AS art_encounter "
            + "              FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                      ) art_patient "
            + "                 ) art "
            + "                  ON         art.patient_id = p.patient_id "
            + " WHERE      p.voided = 0 "
            + " AND        e.voided = 0 "
            + " AND        o.voided = 0 "
            + " AND        o2.voided = 0 "
            + " AND        e.encounter_type = ${6} "
            + " AND        e.location_id = :location ";
    query +=
        b8Period
            ? "          AND e.encounter_datetime NOT BETWEEN "
                + "            Date_add(art.art_encounter, INTERVAL "
                + minNumberOfMonths
                + " MONTH ) "
                + "          AND  "
                + "            Date_add(art.art_encounter, INTERVAL "
                + maxNumberOfMonths
                + " MONTH ) "
            : "          AND e.encounter_datetime NOT BETWEEN "
                + "            Date_add(Date_add(art.art_encounter, INTERVAL "
                + minNumberOfMonths
                + " MONTH ), INTERVAL 1 DAY) "
                + "          AND "
                + "            Date_add(art.art_encounter, INTERVAL "
                + maxNumberOfMonths
                + " MONTH ) ";
    query +=
        " AND    (   ( o.concept_id = ${23761} "
            + "              AND o.value_coded NOT IN ( ${1065} ) ) "
            + " OR         ( o2.concept_id = ${1268} "
            + "                AND  o2.value_coded NOT IN ( ${1256}, ${1257} ) ) ) "
            + " GROUP BY   p.patient_id ) AS final_query "
            + " WHERE final_query.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>Inscrito em algum MDS entre 24˚ a 36˚ mês de TARV? - D.9 (RF54)</b><br>
   *
   * <p>O sistema irá determinar a “Data de inscrição no MDS entre 24º e 36º mês do TARV”,
   * seleccionando a data de consulta (Ficha Clínica) com registo de pelo menos um campo de “Modelos
   * diferenciados de Cuidados - MDS” (MDS 1, MDS 2, MDS 3, MDS 4 ou MDS 5) com resposta = “INICIO”
   * numa consulta clínica decorrida entre 24 a 36 meses do Início TARV (Data da Consulta >= “Data
   * Início TARV” + 24 meses e <= “Data Início TARV” + 36 meses) <br>
   *
   * <p>Nota 1: Nota 1: caso exista mais que uma consulta clínica com registo do início no MDS, o
   * sistema irá considerar o registo mais antigo, ou seja, o primeiro registo durante o período de
   * avaliação<br>
   * <br>
   *
   * <p>Nota 2: caso o utente não satisfaça o critério definido, o sistema não irá preencher nenhuma
   * informação.<br>
   * <br>
   *
   * <p>Nota 3: A “Data Início TARV” é definida no RF61 <br>
   * <br>
   *
   * <p>Nota 4: O utente a ser considerado nesta definição iniciou TARV ou na coorte de 12 meses ou
   * na coorte de 24 meses ou na coorte de 36 meses conforme definido no RF4. <br>
   * <br>
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getMdsDate(
      int minNumberOfMonths,
      int maxNumberOfMonths,
      boolean b9period,
      int minCohortNumberOfYears,
      int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(
        "D9- Data de inscrição no MDS: (coluna CD) - Resposta = Data de Inscrição (RF54)");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());
    map.put("165322", hivMetadata.getMdcState().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("1257", hivMetadata.getContinueRegimenConcept().getConceptId());
    map.put("1267", hivMetadata.getCompletedConcept().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "  SELECT     p.patient_id, MIN(e.encounter_datetime) AS encounter_date "
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
            + "                  AND        e.location_id = :location ";
    query +=
        b9period
            ? "                  AND        e.encounter_datetime >= date_add( art.art_encounter, INTERVAL "
                + minNumberOfMonths
                + " MONTH ) "
                + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
                + maxNumberOfMonths
                + " MONTH ) "
            : "                  AND        e.encounter_datetime > date_add( art.art_encounter, INTERVAL "
                + minNumberOfMonths
                + " MONTH ) "
                + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
                + maxNumberOfMonths
                + " MONTH ) "
                + "                  AND    (   ( otype.concept_id = ${165174} "
                + "                               AND otype.value_coded IS NOT NULL ) "
                + "                  AND         ( ostate.concept_id = ${165322} "
                + "                                 AND  ostate.value_coded IN (${1256}) ) ) "
                + "                  AND  otype.obs_group_id = ostate.obs_group_id "
                + " AND p.patient_id IN ( "
                + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                    minCohortNumberOfYears, maxCohortNumberOfYears)
                + " ) "
                + "                  GROUP BY   p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * MDS1 - Coluna S - Primeiro MDS marcado como "Início" numa consulta clínica (Ficha Clínica)
   * ocorrida entre data do início TARV e 12 meses do início TARV (Data da Consulta >= “Data Início
   * TARV” e <= “Data Início TARV” + 12 meses)
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getMds1(
      int numberOfMonths, int minCohortNumberOfYears, int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("B10- Tipo de MDS - (MDS1) Coluna S");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());
    map.put("165322", hivMetadata.getMdcState().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "                  SELECT     mds1.patient_id, "
            + "                             otype1.value_coded AS first_mds "
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
            + "                  AND        e.encounter_datetime >= art.art_encounter "
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
            + " AND mds1.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) "
            + "       GROUP BY mds1.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * Coluna T - Data da consulta (Ficha Clínica) em que o primeiro MDS foi marcado como "Início",
   * ocorrido entre data do início TARV e 12 meses do início TARV (Data da Consulta >= “Data Início
   * TARV” e <= “Data Início TARV” + 12 meses)
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getMds1StartDate(
      int numberOfYears, int minCohortNumberOfYears, int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Data Início de MDS1: Coluna T");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());
    map.put("165322", hivMetadata.getMdcState().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "                  SELECT     p.patient_id, "
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
            + "                  AND        e.encounter_datetime >= art.art_encounter "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfYears
            + " MONTH ) "
            + "                  AND    (   ( otype.concept_id = ${165174} "
            + "                               AND otype.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate.concept_id = ${165322} "
            + "                                 AND  ostate.value_coded IN (${1256}) ) ) "
            + "                  AND  otype.obs_group_id = ostate.obs_group_id "
            + " AND p.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) "
            + "                  GROUP BY   p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * MDS2 - Coluna W - Data da consulta (Ficha Clínica) em que o segundo MDS foi marcado como
   * "Início", ocorrido entre data do início TARV e 12 meses do início TARV (Data da Consulta >=
   * “Data Início TARV” e <= “Data Início TARV” + 12 meses)
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getMds2StartDate(
      int numberOfMonths, int minCohortNumberOfYears, int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Data Início de MDS2: Coluna W");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());
    map.put("165322", hivMetadata.getMdcState().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "                  SELECT     mds2.patient_id, "
            + "                             MIN(enc2.encounter_datetime) AS second_mds_date "
            + "                  FROM       patient mds2 "
            + "                  INNER JOIN encounter enc2 "
            + "                  ON         enc2.patient_id = mds2.patient_id "
            + "                  INNER JOIN obs otype2 "
            + "                  ON         otype2.encounter_id = enc2.encounter_id "
            + "                  INNER JOIN obs ostate2 "
            + "                  ON         ostate2.encounter_id = enc2.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds2.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     p.patient_id, "
            + "                             MIN(e.encounter_datetime) AS first_mds_date, "
            + "                             MIN(otype.value_coded) AS first_mds "
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
            + "                  AND        e.encounter_datetime >= art.art_encounter "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND    (   ( otype.concept_id = ${165174} "
            + "                               AND otype.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate.concept_id = ${165322} "
            + "                                 AND  ostate.value_coded IN (${1256}) ) ) "
            + "                  AND  otype.obs_group_id = ostate.obs_group_id "
            + "       GROUP BY p.patient_id ) mds_1st "
            + "                  ON mds_1st.patient_id = mds2.patient_id "
            + "                  WHERE mds2.voided = 0 "
            + "                  AND enc2.voided = 0 "
            + "                  AND otype2.voided = 0 "
            + "                  AND ostate2.voided = 0 "
            + "                  AND        enc2.encounter_datetime >= art.art_encounter "
            + "                  AND        enc2.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND enc2.encounter_datetime > mds_1st.first_mds_date "
            + "                  AND        enc2.encounter_type = ${6} "
            + "                  AND        enc2.location_id = :location "
            + "                  AND    (   ( otype2.concept_id = ${165174} "
            + "                               AND otype2.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate2.concept_id = ${165322} "
            + "                                 AND  ostate2.value_coded = ${1256} ) ) "
            + "                  AND  otype2.obs_group_id = ostate2.obs_group_id "
            + " AND mds2.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) "
            + "       GROUP BY mds2.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * MDS2 - Coluna X - Data da consulta (Ficha Clínica) em que o segundo MDS foi marcado como “Fim”,
   * ocorrido entre data do início TARV e 12 meses do início TARV (Data da Consulta >= “Data Início
   * TARV” e <= “Data Início TARV” + 12 meses)
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getMds2EndDate(
      int numberOfMonths, int minCohortNumberOfYears, int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Data Fim de MDS2: Coluna X");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());
    map.put("165322", hivMetadata.getMdcState().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("1267", hivMetadata.getCompletedConcept().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "                  SELECT     mds2_end.patient_id, "
            + "                             MIN(ee22.encounter_datetime) AS second_mds_end_date "
            + "                  FROM       patient mds2_end "
            + "                  INNER JOIN encounter ee22 "
            + "                  ON         ee22.patient_id = mds2_end.patient_id "
            + "                  INNER JOIN obs ot22 "
            + "                  ON         ot22.encounter_id = ee22.encounter_id "
            + "                  INNER JOIN obs os22 "
            + "                  ON         os22.encounter_id = ee22.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds2_end.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     mds2.patient_id, "
            + "                             MIN(enc2.encounter_datetime) AS second_mds_date, "
            + "                             MIN(otype2.value_coded) AS second_mds "
            + "                  FROM       patient mds2 "
            + "                  INNER JOIN encounter enc2 "
            + "                  ON         enc2.patient_id = mds2.patient_id "
            + "                  INNER JOIN obs otype2 "
            + "                  ON         otype2.encounter_id = enc2.encounter_id "
            + "                  INNER JOIN obs ostate2 "
            + "                  ON         ostate2.encounter_id = enc2.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds2.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     p.patient_id, "
            + "                             MIN(e.encounter_datetime) AS first_mds_date, "
            + "                             MIN(otype.value_coded) AS first_mds "
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
            + "                  AND        e.encounter_datetime >= art.art_encounter "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND    (   ( otype.concept_id = ${165174} "
            + "                               AND otype.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate.concept_id = ${165322} "
            + "                                 AND  ostate.value_coded IN (${1256}) ) ) "
            + "                  AND  otype.obs_group_id = ostate.obs_group_id "
            + "       GROUP BY p.patient_id ) mds_1st "
            + "                  ON mds_1st.patient_id = mds2.patient_id "
            + "                  WHERE mds2.voided = 0 "
            + "                  AND enc2.voided = 0 "
            + "                  AND otype2.voided = 0 "
            + "                  AND ostate2.voided = 0 "
            + "                  AND enc2.encounter_datetime > mds_1st.first_mds_date "
            + "                  AND        enc2.encounter_datetime >= art.art_encounter "
            + "                  AND        enc2.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        enc2.encounter_type = ${6} "
            + "                  AND        enc2.location_id = :location "
            + "                  AND    (   ( otype2.concept_id = ${165174} "
            + "                               AND otype2.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate2.concept_id = ${165322} "
            + "                                 AND  ostate2.value_coded = ${1256} ) ) "
            + "                  AND  otype2.obs_group_id = ostate2.obs_group_id "
            + "       GROUP BY mds2.patient_id ) mds_2nd "
            + "                  ON mds_2nd.patient_id = mds2_end.patient_id "
            + "                  WHERE mds2_end.voided = 0 "
            + "                  AND ee22.voided = 0 "
            + "                  AND ot22.voided = 0 "
            + "                  AND os22.voided = 0 "
            + "                  AND ee22.encounter_datetime > mds_2nd.second_mds_date "
            + "                  AND        ee22.encounter_datetime >= art.art_encounter "
            + "                  AND        ee22.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        ee22.encounter_type = ${6} "
            + "                  AND        ee22.location_id = :location "
            + "                  AND    (   ( ot22.concept_id = ${165174} "
            + "                               AND ot22.value_coded = mds_2nd.second_mds  ) "
            + "                  AND         ( os22.concept_id = ${165322} "
            + "                                 AND  os22.value_coded = ${1267} ) ) "
            + "                  AND  ot22.obs_group_id = os22.obs_group_id "
            + " AND mds2_end.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) "
            + "       GROUP BY mds2_end.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * Coluna U - Data da consulta (Ficha Clínica) em que o primeiro MDS foi marcado como “Fim”,
   * ocorrido entre data do início TARV e 12 meses do início TARV (Data da Consulta >= “Data Início
   * TARV” e <= “Data Início TARV” + 12 meses)
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getMds1EndDate(
      int numberOfMonths, int minCohortNumberOfYears, int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("Data Fim de MDS1: Coluna U");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());
    map.put("165322", hivMetadata.getMdcState().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("1267", hivMetadata.getCompletedConcept().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "                  SELECT     mds1_end.patient_id, "
            + "                             MIN(ee.encounter_datetime) AS encounter_end_date "
            + "                  FROM       patient mds1_end "
            + "                  INNER JOIN encounter ee "
            + "                  ON         ee.patient_id = mds1_end.patient_id "
            + "                  INNER JOIN obs ot "
            + "                  ON         ot.encounter_id = ee.encounter_id "
            + "                  INNER JOIN obs os "
            + "                  ON         os.encounter_id = ee.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds1_end.patient_id "
            + "                  INNER JOIN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getFirstMdsAndDateQuery(numberOfMonths)
            + "                  ) mds1 "
            + "                  ON         mds1.patient_id = mds1_end.patient_id "
            + "                  WHERE      mds1_end.voided = 0 "
            + "                  AND        ot.voided = 0 "
            + "                  AND        os.voided = 0 "
            + "                  AND        ee.encounter_type = ${6} "
            + "                  AND        ee.location_id = :location "
            + "                  AND        ee.encounter_datetime >= art.art_encounter "
            + "                  AND        ee.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        ee.encounter_datetime > mds1.first_mds "
            + "                  AND    (   ( ot.concept_id = ${165174} "
            + "                               AND ot.value_coded = mds1.mds_one ) "
            + "                  AND         ( os.concept_id = ${165322} "
            + "                                 AND  os.value_coded IN (${1267}) ) ) "
            + "                  AND  ot.obs_group_id = os.obs_group_id "
            + " AND mds1_end.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) "
            + "                  GROUP BY   mds1_end.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * MDS2 - Coluna V - Segundo MDS marcado como "Início" numa consulta clínica (Ficha Clínica)
   * ocorrida entre data do início TARV e 12 meses do início TARV (Data da Consulta >= “Data Início
   * TARV” e <= “Data Início TARV” + 12 meses)
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getMds2(
      int numberOfMonths, int minCohortNumberOfYears, int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("B10- Tipo de MDS: (MDS2) Coluna V");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());
    map.put("165322", hivMetadata.getMdcState().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "                  SELECT     mds2.patient_id, "
            + "                             MIN(otype2.value_coded) AS second_mds "
            + "                  FROM       patient mds2 "
            + "                  INNER JOIN encounter enc2 "
            + "                  ON         enc2.patient_id = mds2.patient_id "
            + "                  INNER JOIN obs otype2 "
            + "                  ON         otype2.encounter_id = enc2.encounter_id "
            + "                  INNER JOIN obs ostate2 "
            + "                  ON         ostate2.encounter_id = enc2.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds2.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     p.patient_id, "
            + "                             MIN(e.encounter_datetime) AS first_mds_date, "
            + "                             MIN(otype.value_coded) AS first_mds "
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
            + "                  AND        e.encounter_datetime >= art.art_encounter "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND    (   ( otype.concept_id = ${165174} "
            + "                               AND otype.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate.concept_id = ${165322} "
            + "                                 AND  ostate.value_coded IN (${1256}) ) ) "
            + "                  AND  otype.obs_group_id = ostate.obs_group_id "
            + "       GROUP BY p.patient_id ) mds_1st "
            + "                  ON mds_1st.patient_id = mds2.patient_id "
            + "                  WHERE mds2.voided = 0 "
            + "                  AND enc2.voided = 0 "
            + "                  AND otype2.voided = 0 "
            + "                  AND ostate2.voided = 0 "
            + "                  AND enc2.encounter_datetime > mds_1st.first_mds_date "
            + "                  AND        enc2.encounter_datetime >= art.art_encounter "
            + "                  AND        enc2.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        enc2.encounter_type = ${6} "
            + "                  AND        enc2.location_id = :location "
            + "                  AND    (   ( otype2.concept_id = ${165174} "
            + "                               AND otype2.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate2.concept_id = ${165322} "
            + "                                 AND  ostate2.value_coded = ${1256} ) ) "
            + "                  AND  otype2.obs_group_id = ostate2.obs_group_id "
            + " AND mds2.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) "
            + "       GROUP BY mds2.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * MDS3 - Coluna Y - Terceiro MDS marcado como "Início" numa consulta clínica (Ficha Clínica)
   * ocorrida entre data do início TARV e 12 meses do início TARV (Data da Consulta >= “Data Início
   * TARV” e <= “Data Início TARV” + 12 meses)
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getMds3(
      int numberOfMonths, int minCohortNumberOfYears, int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("B10- Tipo de MDS: (MDS3) Coluna Y");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());
    map.put("165322", hivMetadata.getMdcState().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "                  SELECT     mds3.patient_id, "
            + "                             MIN(otype3.value_coded) AS third_mds "
            + "                  FROM       patient mds3 "
            + "                  INNER JOIN encounter enc3 "
            + "                  ON         enc3.patient_id = mds3.patient_id "
            + "                  INNER JOIN obs otype3 "
            + "                  ON         otype3.encounter_id = enc3.encounter_id "
            + "                  INNER JOIN obs ostate3 "
            + "                  ON         ostate3.encounter_id = enc3.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds3.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     mds2.patient_id, "
            + "                             MIN(enc2.encounter_datetime) AS second_mds_date, "
            + "                             MIN(otype2.value_coded) AS second_mds "
            + "                  FROM       patient mds2 "
            + "                  INNER JOIN encounter enc2 "
            + "                  ON         enc2.patient_id = mds2.patient_id "
            + "                  INNER JOIN obs otype2 "
            + "                  ON         otype2.encounter_id = enc2.encounter_id "
            + "                  INNER JOIN obs ostate2 "
            + "                  ON         ostate2.encounter_id = enc2.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds2.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     p.patient_id, "
            + "                             MIN(e.encounter_datetime) AS first_mds_date, "
            + "                             MIN(otype.value_coded) AS first_mds "
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
            + "                  AND        e.encounter_datetime >= art.art_encounter "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND    (   ( otype.concept_id = ${165174} "
            + "                               AND otype.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate.concept_id = ${165322} "
            + "                                 AND  ostate.value_coded IN (${1256}) ) ) "
            + "                  AND  otype.obs_group_id = ostate.obs_group_id "
            + "       GROUP BY p.patient_id ) mds_1st "
            + "                  ON mds_1st.patient_id = mds2.patient_id "
            + "                  WHERE mds2.voided = 0 "
            + "                  AND enc2.voided = 0 "
            + "                  AND otype2.voided = 0 "
            + "                  AND ostate2.voided = 0 "
            + "                  AND enc2.encounter_datetime > mds_1st.first_mds_date "
            + "                  AND        enc2.encounter_datetime >= art.art_encounter "
            + "                  AND        enc2.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        enc2.encounter_type = ${6} "
            + "                  AND        enc2.location_id = :location "
            + "                  AND    (   ( otype2.concept_id = ${165174} "
            + "                               AND otype2.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate2.concept_id = ${165322} "
            + "                                 AND  ostate2.value_coded = ${1256} ) ) "
            + "                  AND  otype2.obs_group_id = ostate2.obs_group_id "
            + "       GROUP BY mds2.patient_id ) mds_2nd "
            + "                  ON mds_2nd.patient_id = mds3.patient_id "
            + "                  WHERE mds3.voided = 0 "
            + "                  AND enc3.voided = 0 "
            + "                  AND otype3.voided = 0 "
            + "                  AND ostate3.voided = 0 "
            + "                  AND enc3.encounter_datetime > mds_2nd.second_mds_date "
            + "                  AND        enc3.encounter_datetime >= art.art_encounter "
            + "                  AND        enc3.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        enc3.encounter_type = ${6} "
            + "                  AND        enc3.location_id = :location "
            + "                  AND    (   ( otype3.concept_id = ${165174} "
            + "                               AND otype3.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate3.concept_id = ${165322} "
            + "                                 AND  ostate3.value_coded = ${1256} ) ) "
            + "                  AND  otype3.obs_group_id = ostate3.obs_group_id "
            + " AND mds3.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) "
            + "       GROUP BY mds3.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * MDS3 - Coluna Z - Data da consulta (Ficha Clínica) em que o terceiro MDS foi marcado como
   * "Início", ocorrido entre data do início TARV e 12 meses do início TARV (Data da Consulta >=
   * “Data Início TARV” e <= “Data Início TARV” + 12 meses)
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getMds3StartDate(
      int numberOfMonths, int minCohortNumberOfYears, int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("B10- Data Início de MDS3: Coluna Z");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());
    map.put("165322", hivMetadata.getMdcState().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "                  SELECT     mds3.patient_id, "
            + "                             MIN(enc3.encounter_datetime) AS third_mds_date "
            + "                  FROM       patient mds3 "
            + "                  INNER JOIN encounter enc3 "
            + "                  ON         enc3.patient_id = mds3.patient_id "
            + "                  INNER JOIN obs otype3 "
            + "                  ON         otype3.encounter_id = enc3.encounter_id "
            + "                  INNER JOIN obs ostate3 "
            + "                  ON         ostate3.encounter_id = enc3.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds3.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     mds2.patient_id, "
            + "                             MIN(enc2.encounter_datetime) AS second_mds_date, "
            + "                             MIN(otype2.value_coded) AS second_mds "
            + "                  FROM       patient mds2 "
            + "                  INNER JOIN encounter enc2 "
            + "                  ON         enc2.patient_id = mds2.patient_id "
            + "                  INNER JOIN obs otype2 "
            + "                  ON         otype2.encounter_id = enc2.encounter_id "
            + "                  INNER JOIN obs ostate2 "
            + "                  ON         ostate2.encounter_id = enc2.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds2.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     p.patient_id, "
            + "                             MIN(e.encounter_datetime) AS first_mds_date, "
            + "                             MIN(otype.value_coded) AS first_mds "
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
            + "                  AND        e.encounter_datetime >= art.art_encounter "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND    (   ( otype.concept_id = ${165174} "
            + "                               AND otype.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate.concept_id = ${165322} "
            + "                                 AND  ostate.value_coded IN (${1256}) ) ) "
            + "                  AND  otype.obs_group_id = ostate.obs_group_id "
            + "       GROUP BY p.patient_id ) mds_1st "
            + "                  ON mds_1st.patient_id = mds2.patient_id "
            + "                  WHERE mds2.voided = 0 "
            + "                  AND enc2.voided = 0 "
            + "                  AND otype2.voided = 0 "
            + "                  AND ostate2.voided = 0 "
            + "                  AND enc2.encounter_datetime > mds_1st.first_mds_date "
            + "                  AND        enc2.encounter_datetime >= art.art_encounter "
            + "                  AND        enc2.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        enc2.encounter_type = ${6} "
            + "                  AND        enc2.location_id = :location "
            + "                  AND    (   ( otype2.concept_id = ${165174} "
            + "                               AND otype2.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate2.concept_id = ${165322} "
            + "                                 AND  ostate2.value_coded = ${1256} ) ) "
            + "                  AND  otype2.obs_group_id = ostate2.obs_group_id "
            + "       GROUP BY mds2.patient_id ) mds_2nd "
            + "                  ON mds_2nd.patient_id = mds3.patient_id "
            + "                  WHERE mds3.voided = 0 "
            + "                  AND enc3.voided = 0 "
            + "                  AND otype3.voided = 0 "
            + "                  AND ostate3.voided = 0 "
            + "                  AND enc3.encounter_datetime > mds_2nd.second_mds_date "
            + "                  AND        enc3.encounter_datetime >= art.art_encounter "
            + "                  AND        enc3.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        enc3.encounter_type = ${6} "
            + "                  AND        enc3.location_id = :location "
            + "                  AND    (   ( otype3.concept_id = ${165174} "
            + "                               AND otype3.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate3.concept_id = ${165322} "
            + "                                 AND  ostate3.value_coded = ${1256} ) ) "
            + "                  AND  otype3.obs_group_id = ostate3.obs_group_id "
            + " AND mds3.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) "
            + "       GROUP BY mds3.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * MDS3 - Coluna AA - Data da consulta (Ficha Clínica) em que o terceiro MDS foi marcado como
   * “Fim”, ocorrido entre data do início TARV e 12 meses do início TARV (Data da Consulta >= “Data
   * Início TARV” e <= “Data Início TARV” + 12 meses)
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getMds3EndDate(
      int numberOfMonths, int minCohortNumberOfYears, int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("B10- Data Fim de MDS3: Coluna AA");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());
    map.put("165322", hivMetadata.getMdcState().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("1267", hivMetadata.getCompletedConcept().getConceptId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());

    String query =
        "                  SELECT     mds3_end.patient_id, "
            + "                             MIN(ee33.encounter_datetime) AS third_mds_end_date "
            + "                  FROM       patient mds3_end "
            + "                  INNER JOIN encounter ee33 "
            + "                  ON         ee33.patient_id = mds3_end.patient_id "
            + "                  INNER JOIN obs ot33 "
            + "                  ON         ot33.encounter_id = ee33.encounter_id "
            + "                  INNER JOIN obs os33 "
            + "                  ON         os33.encounter_id = ee33.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds3_end.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     mds3.patient_id, "
            + "                             MIN(enc3.encounter_datetime) AS third_mds_date, "
            + "                             MIN(otype3.value_coded) AS third_mds "
            + "                  FROM       patient mds3 "
            + "                  INNER JOIN encounter enc3 "
            + "                  ON         enc3.patient_id = mds3.patient_id "
            + "                  INNER JOIN obs otype3 "
            + "                  ON         otype3.encounter_id = enc3.encounter_id "
            + "                  INNER JOIN obs ostate3 "
            + "                  ON         ostate3.encounter_id = enc3.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds3.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     mds2.patient_id, "
            + "                             MIN(enc2.encounter_datetime) AS second_mds_date, "
            + "                             MIN(otype2.value_coded) AS second_mds "
            + "                  FROM       patient mds2 "
            + "                  INNER JOIN encounter enc2 "
            + "                  ON         enc2.patient_id = mds2.patient_id "
            + "                  INNER JOIN obs otype2 "
            + "                  ON         otype2.encounter_id = enc2.encounter_id "
            + "                  INNER JOIN obs ostate2 "
            + "                  ON         ostate2.encounter_id = enc2.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds2.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     p.patient_id, "
            + "                             MIN(e.encounter_datetime) AS first_mds_date, "
            + "                             MIN(otype.value_coded) AS first_mds "
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
            + "                  AND        otype.voided = 0 "
            + "                  AND        ostate.voided = 0 "
            + "                  AND        e.encounter_type = ${6} "
            + "                  AND        e.location_id = :location "
            + "                  AND        e.encounter_datetime >= art.art_encounter "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND    (   ( otype.concept_id = ${165174} "
            + "                               AND otype.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate.concept_id = ${165322} "
            + "                                 AND  ostate.value_coded IN (${1256}) ) ) "
            + "                  AND  otype.obs_group_id = ostate.obs_group_id "
            + "       GROUP BY p.patient_id ) mds_1st "
            + "                  ON mds_1st.patient_id = mds2.patient_id "
            + "                  WHERE mds2.voided = 0 "
            + "                  AND enc2.voided = 0 "
            + "                  AND otype2.voided = 0 "
            + "                  AND ostate2.voided = 0 "
            + "                  AND enc2.encounter_datetime > mds_1st.first_mds_date "
            + "                  AND        enc2.encounter_datetime >= art.art_encounter "
            + "                  AND        enc2.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        enc2.encounter_type = ${6} "
            + "                  AND        enc2.location_id = :location "
            + "                  AND    (   ( otype2.concept_id = ${165174} "
            + "                               AND otype2.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate2.concept_id = ${165322} "
            + "                                 AND  ostate2.value_coded = ${1256} ) ) "
            + "                  AND  otype2.obs_group_id = ostate2.obs_group_id "
            + "       GROUP BY mds2.patient_id ) mds_2nd "
            + "                  ON mds_2nd.patient_id = mds3.patient_id "
            + "                  WHERE mds3.voided = 0 "
            + "                  AND enc3.voided = 0 "
            + "                  AND otype3.voided = 0 "
            + "                  AND ostate3.voided = 0 "
            + "                  AND enc3.encounter_datetime > mds_2nd.second_mds_date "
            + "                  AND        enc3.encounter_datetime >= art.art_encounter "
            + "                  AND        enc3.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        enc3.encounter_type = ${6} "
            + "                  AND        enc3.location_id = :location "
            + "                  AND    (   ( otype3.concept_id = ${165174} "
            + "                               AND otype3.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate3.concept_id = ${165322} "
            + "                                 AND  ostate3.value_coded = ${1256} ) ) "
            + "                  AND  otype3.obs_group_id = ostate3.obs_group_id "
            + "       GROUP BY mds3.patient_id ) mds_3rd "
            + "                  ON mds_3rd.patient_id = mds3_end.patient_id "
            + "                  WHERE mds3_end.voided = 0 "
            + "                  AND ee33.voided = 0 "
            + "                  AND ot33.voided = 0 "
            + "                  AND os33.voided = 0 "
            + "                  AND ee33.encounter_datetime > mds_3rd.third_mds_date "
            + "                  AND        ee33.encounter_datetime >= art.art_encounter "
            + "                  AND        ee33.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        ee33.encounter_type = ${6} "
            + "                  AND        ee33.location_id = :location "
            + "                  AND    (   ( ot33.concept_id = ${165174} "
            + "                               AND ot33.value_coded = mds_3rd.third_mds ) "
            + "                  AND         ( os33.concept_id = ${165322} "
            + "                                 AND  os33.value_coded = ${1267} ) ) "
            + "                  AND  ot33.obs_group_id = os33.obs_group_id "
            + " AND mds3_end.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) "
            + "       GROUP BY mds3_end.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * MDS4 - Coluna AB - Quarto MDS marcado como "Início" numa consulta clínica (Ficha Clínica)
   * ocorrida entre data do início TARV e 12 meses do início TARV (Data da Consulta >= “Data Início
   * TARV” e <= “Data Início TARV” + 12 meses)
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getMds4(
      int numberOfMonths, int minCohortNumberOfYears, int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("B10- Tipo de MDS: (MDS4) Coluna AB");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());
    map.put("165322", hivMetadata.getMdcState().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "                  SELECT     mds4.patient_id, "
            + "                             MIN(otype4.value_coded) AS fourth_mds "
            + "                  FROM       patient mds4 "
            + "                  INNER JOIN encounter enc4 "
            + "                  ON         enc4.patient_id = mds4.patient_id "
            + "                  INNER JOIN obs otype4 "
            + "                  ON         otype4.encounter_id = enc4.encounter_id "
            + "                  INNER JOIN obs ostate4 "
            + "                  ON         ostate4.encounter_id = enc4.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds4.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     mds3.patient_id, "
            + "                             MIN(enc3.encounter_datetime) AS third_mds_date, "
            + "                             MIN(otype3.value_coded) AS third_mds "
            + "                  FROM       patient mds3 "
            + "                  INNER JOIN encounter enc3 "
            + "                  ON         enc3.patient_id = mds3.patient_id "
            + "                  INNER JOIN obs otype3 "
            + "                  ON         otype3.encounter_id = enc3.encounter_id "
            + "                  INNER JOIN obs ostate3 "
            + "                  ON         ostate3.encounter_id = enc3.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds3.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     mds2.patient_id, "
            + "                             MIN(enc2.encounter_datetime) AS second_mds_date, "
            + "                             MIN(otype2.value_coded) AS second_mds "
            + "                  FROM       patient mds2 "
            + "                  INNER JOIN encounter enc2 "
            + "                  ON         enc2.patient_id = mds2.patient_id "
            + "                  INNER JOIN obs otype2 "
            + "                  ON         otype2.encounter_id = enc2.encounter_id "
            + "                  INNER JOIN obs ostate2 "
            + "                  ON         ostate2.encounter_id = enc2.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds2.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     p.patient_id, "
            + "                             MIN(e.encounter_datetime) AS first_mds_date, "
            + "                             MIN(otype.value_coded) AS first_mds "
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
            + "                  AND        e.encounter_datetime >= art.art_encounter "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND    (   ( otype.concept_id = ${165174} "
            + "                               AND otype.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate.concept_id = ${165322} "
            + "                                 AND  ostate.value_coded IN (${1256}) ) ) "
            + "                  AND  otype.obs_group_id = ostate.obs_group_id "
            + "       GROUP BY p.patient_id ) mds_1st "
            + "                  ON mds_1st.patient_id = mds2.patient_id "
            + "                  WHERE mds2.voided = 0 "
            + "                  AND enc2.voided = 0 "
            + "                  AND otype2.voided = 0 "
            + "                  AND ostate2.voided = 0 "
            + "                  AND enc2.encounter_datetime > mds_1st.first_mds_date "
            + "                  AND        enc2.encounter_datetime >= art.art_encounter "
            + "                  AND        enc2.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        enc2.encounter_type = ${6} "
            + "                  AND        enc2.location_id = :location "
            + "                  AND    (   ( otype2.concept_id = ${165174} "
            + "                               AND otype2.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate2.concept_id = ${165322} "
            + "                                 AND  ostate2.value_coded = ${1256} ) ) "
            + "                  AND  otype2.obs_group_id = ostate2.obs_group_id "
            + "       GROUP BY mds2.patient_id ) mds_2nd "
            + "                  ON mds_2nd.patient_id = mds3.patient_id "
            + "                  WHERE mds3.voided = 0 "
            + "                  AND enc3.voided = 0 "
            + "                  AND otype3.voided = 0 "
            + "                  AND ostate3.voided = 0 "
            + "                  AND enc3.encounter_datetime > mds_2nd.second_mds_date "
            + "                  AND        enc3.encounter_datetime >= art.art_encounter "
            + "                  AND        enc3.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        enc3.encounter_type = ${6} "
            + "                  AND        enc3.location_id = :location "
            + "                  AND    (   ( otype3.concept_id = ${165174} "
            + "                               AND otype3.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate3.concept_id = ${165322} "
            + "                                 AND  ostate3.value_coded = ${1256} ) ) "
            + "                  AND  otype3.obs_group_id = ostate3.obs_group_id "
            + "       GROUP BY mds3.patient_id ) mds_3rd "
            + "                  ON mds_3rd.patient_id = mds4.patient_id "
            + "                  WHERE mds4.voided = 0 "
            + "                  AND enc4.voided = 0 "
            + "                  AND otype4.voided = 0 "
            + "                  AND ostate4.voided = 0 "
            + "                  AND enc4.encounter_datetime > mds_3rd.third_mds_date "
            + "                  AND        enc4.encounter_datetime >= art.art_encounter "
            + "                  AND        enc4.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        enc4.encounter_type = ${6} "
            + "                  AND        enc4.location_id = :location "
            + "                  AND    (   ( otype4.concept_id = ${165174} "
            + "                               AND otype4.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate4.concept_id = ${165322} "
            + "                                 AND  ostate4.value_coded = ${1256} ) ) "
            + "                  AND  otype4.obs_group_id = ostate4.obs_group_id "
            + " AND mds4.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) "
            + "       GROUP BY mds4.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * MDS4 - Coluna AC - Data da consulta (Ficha Clínica) em que o quarto MDS foi marcado como
   * "Início", ocorrido entre data do início TARV e 12 meses do início TARV (Data da Consulta >=
   * “Data Início TARV” e <= “Data Início TARV” + 12 meses)
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getMds4StartDate(
      int numberOfMonths, int minCohortNumberOfYears, int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("B10- Data Início de MDS4: Coluna AC");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());
    map.put("165322", hivMetadata.getMdcState().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "                  SELECT     mds4.patient_id, "
            + "                             MIN(enc4.encounter_datetime) AS fourth_mds_date "
            + "                  FROM       patient mds4 "
            + "                  INNER JOIN encounter enc4 "
            + "                  ON         enc4.patient_id = mds4.patient_id "
            + "                  INNER JOIN obs otype4 "
            + "                  ON         otype4.encounter_id = enc4.encounter_id "
            + "                  INNER JOIN obs ostate4 "
            + "                  ON         ostate4.encounter_id = enc4.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds4.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     mds3.patient_id, "
            + "                             MIN(enc3.encounter_datetime) AS third_mds_date, "
            + "                             MIN(otype3.value_coded) AS third_mds "
            + "                  FROM       patient mds3 "
            + "                  INNER JOIN encounter enc3 "
            + "                  ON         enc3.patient_id = mds3.patient_id "
            + "                  INNER JOIN obs otype3 "
            + "                  ON         otype3.encounter_id = enc3.encounter_id "
            + "                  INNER JOIN obs ostate3 "
            + "                  ON         ostate3.encounter_id = enc3.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds3.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     mds2.patient_id, "
            + "                             MIN(enc2.encounter_datetime) AS second_mds_date, "
            + "                             MIN(otype2.value_coded) AS second_mds "
            + "                  FROM       patient mds2 "
            + "                  INNER JOIN encounter enc2 "
            + "                  ON         enc2.patient_id = mds2.patient_id "
            + "                  INNER JOIN obs otype2 "
            + "                  ON         otype2.encounter_id = enc2.encounter_id "
            + "                  INNER JOIN obs ostate2 "
            + "                  ON         ostate2.encounter_id = enc2.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds2.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     p.patient_id, "
            + "                             MIN(e.encounter_datetime) AS first_mds_date, "
            + "                             MIN(otype.value_coded) AS first_mds "
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
            + "                  AND        e.encounter_datetime >= art.art_encounter "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND    (   ( otype.concept_id = ${165174} "
            + "                               AND otype.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate.concept_id = ${165322} "
            + "                                 AND  ostate.value_coded IN (${1256}) ) ) "
            + "                  AND  otype.obs_group_id = ostate.obs_group_id "
            + "       GROUP BY p.patient_id ) mds_1st "
            + "                  ON mds_1st.patient_id = mds2.patient_id "
            + "                  WHERE mds2.voided = 0 "
            + "                  AND enc2.voided = 0 "
            + "                  AND otype2.voided = 0 "
            + "                  AND ostate2.voided = 0 "
            + "                  AND enc2.encounter_datetime > mds_1st.first_mds_date "
            + "                  AND        enc2.encounter_datetime >= art.art_encounter "
            + "                  AND        enc2.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        enc2.encounter_type = ${6} "
            + "                  AND        enc2.location_id = :location "
            + "                  AND    (   ( otype2.concept_id = ${165174} "
            + "                               AND otype2.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate2.concept_id = ${165322} "
            + "                                 AND  ostate2.value_coded = ${1256} ) ) "
            + "                  AND  otype2.obs_group_id = ostate2.obs_group_id "
            + "       GROUP BY mds2.patient_id ) mds_2nd "
            + "                  ON mds_2nd.patient_id = mds3.patient_id "
            + "                  WHERE mds3.voided = 0 "
            + "                  AND enc3.voided = 0 "
            + "                  AND otype3.voided = 0 "
            + "                  AND ostate3.voided = 0 "
            + "                  AND enc3.encounter_datetime > mds_2nd.second_mds_date "
            + "                  AND        enc3.encounter_datetime >= art.art_encounter "
            + "                  AND        enc3.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        enc3.encounter_type = ${6} "
            + "                  AND        enc3.location_id = :location "
            + "                  AND    (   ( otype3.concept_id = ${165174} "
            + "                               AND otype3.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate3.concept_id = ${165322} "
            + "                                 AND  ostate3.value_coded = ${1256} ) ) "
            + "                  AND  otype3.obs_group_id = ostate3.obs_group_id "
            + "       GROUP BY mds3.patient_id ) mds_3rd "
            + "                  ON mds_3rd.patient_id = mds4.patient_id "
            + "                  WHERE mds4.voided = 0 "
            + "                  AND enc4.voided = 0 "
            + "                  AND otype4.voided = 0 "
            + "                  AND ostate4.voided = 0 "
            + "                  AND enc4.encounter_datetime > mds_3rd.third_mds_date "
            + "                  AND        enc4.encounter_datetime >= art.art_encounter "
            + "                  AND        enc4.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        enc4.encounter_type = ${6} "
            + "                  AND        enc4.location_id = :location "
            + "                  AND    (   ( otype4.concept_id = ${165174} "
            + "                               AND otype4.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate4.concept_id = ${165322} "
            + "                                 AND  ostate4.value_coded = ${1256} ) ) "
            + "                  AND  otype4.obs_group_id = ostate4.obs_group_id "
            + " AND mds4.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) "
            + "       GROUP BY mds4.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * MDS4 - Coluna AD - Data da consulta (Ficha Clínica) em que o quarto MDS foi marcado como “Fim”,
   * ocorrido entre data do início TARV e 12 meses do início TARV (Data da Consulta >= “Data Início
   * TARV” e <= “Data Início TARV” + 12 meses)
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getMds4EndDate(
      int numberOfMonths, int minCohortNumberOfYears, int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("B10- Data Fim de MDS4: Coluna AD");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());
    map.put("165322", hivMetadata.getMdcState().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("1267", hivMetadata.getCompletedConcept().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "                  SELECT     mds4_end.patient_id, "
            + "                             MIN(ee44.encounter_datetime) AS fourth_mds_end_date "
            + "                  FROM       patient mds4_end "
            + "                  INNER JOIN encounter ee44 "
            + "                  ON         ee44.patient_id = mds4_end.patient_id "
            + "                  INNER JOIN obs ot44 "
            + "                  ON         ot44.encounter_id = ee44.encounter_id "
            + "                  INNER JOIN obs os44 "
            + "                  ON         os44.encounter_id = ee44.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds4_end.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     mds4.patient_id, "
            + "                             MIN(enc4.encounter_datetime) AS fourth_mds_date, "
            + "                             MIN(otype4.value_coded) AS fourth_mds "
            + "                  FROM       patient mds4 "
            + "                  INNER JOIN encounter enc4 "
            + "                  ON         enc4.patient_id = mds4.patient_id "
            + "                  INNER JOIN obs otype4 "
            + "                  ON         otype4.encounter_id = enc4.encounter_id "
            + "                  INNER JOIN obs ostate4 "
            + "                  ON         ostate4.encounter_id = enc4.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds4.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     mds3.patient_id, "
            + "                             MIN(enc3.encounter_datetime) AS third_mds_date, "
            + "                             MIN(otype3.value_coded) AS third_mds "
            + "                  FROM       patient mds3 "
            + "                  INNER JOIN encounter enc3 "
            + "                  ON         enc3.patient_id = mds3.patient_id "
            + "                  INNER JOIN obs otype3 "
            + "                  ON         otype3.encounter_id = enc3.encounter_id "
            + "                  INNER JOIN obs ostate3 "
            + "                  ON         ostate3.encounter_id = enc3.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds3.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     mds2.patient_id, "
            + "                             MIN(enc2.encounter_datetime) AS second_mds_date, "
            + "                             MIN(otype2.value_coded) AS second_mds "
            + "                  FROM       patient mds2 "
            + "                  INNER JOIN encounter enc2 "
            + "                  ON         enc2.patient_id = mds2.patient_id "
            + "                  INNER JOIN obs otype2 "
            + "                  ON         otype2.encounter_id = enc2.encounter_id "
            + "                  INNER JOIN obs ostate2 "
            + "                  ON         ostate2.encounter_id = enc2.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds2.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     p.patient_id, "
            + "                             MIN(e.encounter_datetime) AS first_mds_date, "
            + "                             MIN(otype.value_coded) AS first_mds "
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
            + "                  AND        e.encounter_datetime >= art.art_encounter "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND    (   ( otype.concept_id = ${165174} "
            + "                               AND otype.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate.concept_id = ${165322} "
            + "                                 AND  ostate.value_coded IN (${1256}) ) ) "
            + "                  AND  otype.obs_group_id = ostate.obs_group_id "
            + "       GROUP BY p.patient_id ) mds_1st "
            + "                  ON mds_1st.patient_id = mds2.patient_id "
            + "                  WHERE mds2.voided = 0 "
            + "                  AND enc2.voided = 0 "
            + "                  AND otype2.voided = 0 "
            + "                  AND ostate2.voided = 0 "
            + "                  AND enc2.encounter_datetime > mds_1st.first_mds_date "
            + "                  AND        enc2.encounter_datetime >= art.art_encounter "
            + "                  AND        enc2.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        enc2.encounter_type = ${6} "
            + "                  AND        enc2.location_id = :location "
            + "                  AND    (   ( otype2.concept_id = ${165174} "
            + "                               AND otype2.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate2.concept_id = ${165322} "
            + "                                 AND  ostate2.value_coded = ${1256} ) ) "
            + "                  AND  otype2.obs_group_id = ostate2.obs_group_id "
            + "       GROUP BY mds2.patient_id ) mds_2nd "
            + "                  ON mds_2nd.patient_id = mds3.patient_id "
            + "                  WHERE mds3.voided = 0 "
            + "                  AND enc3.voided = 0 "
            + "                  AND otype3.voided = 0 "
            + "                  AND ostate3.voided = 0 "
            + "                  AND enc3.encounter_datetime > mds_2nd.second_mds_date "
            + "                  AND        enc3.encounter_datetime >= art.art_encounter "
            + "                  AND        enc3.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        enc3.encounter_type = ${6} "
            + "                  AND        enc3.location_id = :location "
            + "                  AND    (   ( otype3.concept_id = ${165174} "
            + "                               AND otype3.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate3.concept_id = ${165322} "
            + "                                 AND  ostate3.value_coded = ${1256} ) ) "
            + "                  AND  otype3.obs_group_id = ostate3.obs_group_id "
            + "       GROUP BY mds3.patient_id ) mds_3rd "
            + "                  ON mds_3rd.patient_id = mds4.patient_id "
            + "                  WHERE mds4.voided = 0 "
            + "                  AND enc4.voided = 0 "
            + "                  AND otype4.voided = 0 "
            + "                  AND ostate4.voided = 0 "
            + "                  AND enc4.encounter_datetime > mds_3rd.third_mds_date "
            + "                  AND        enc4.encounter_datetime >= art.art_encounter "
            + "                  AND        enc4.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        enc4.encounter_type = ${6} "
            + "                  AND        enc4.location_id = :location "
            + "                  AND    (   ( otype4.concept_id = ${165174} "
            + "                               AND otype4.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate4.concept_id = ${165322} "
            + "                                 AND  ostate4.value_coded = ${1256} ) ) "
            + "                  AND  otype4.obs_group_id = ostate4.obs_group_id "
            + "       GROUP BY mds4.patient_id ) mds_4th "
            + "                  ON mds_4th.patient_id = mds4_end.patient_id "
            + "                  WHERE mds4_end.voided = 0 "
            + "                  AND ee44.voided = 0 "
            + "                  AND ot44.voided = 0 "
            + "                  AND os44.voided = 0 "
            + "                  AND ee44.encounter_datetime > mds_4th.fourth_mds_date "
            + "                  AND        ee44.encounter_datetime >= art.art_encounter "
            + "                  AND        ee44.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        ee44.encounter_type = ${6} "
            + "                  AND    (   ( ot44.concept_id = ${165174} "
            + "                               AND ot44.value_coded = mds_4th.fourth_mds ) "
            + "                  AND         ( os44.concept_id = ${165322} "
            + "                                 AND  os44.value_coded = ${1267} ) ) "
            + "                  AND  ot44.obs_group_id = os44.obs_group_id "
            + " AND mds4_end.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) "
            + "       GROUP BY mds4_end.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * MDS5 - Coluna AE - Quinto MDS marcado como "Início" numa consulta clínica (Ficha Clínica)
   * ocorrida entre data do início TARV e 12 meses do início TARV (Data da Consulta >= “Data Início
   * TARV” e <= “Data Início TARV” + 12 meses)
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getMds5(
      int numberOfMonths, int minCohortNumberOfYears, int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("B10- Tipo de MDS: (MDS5) Coluna AE");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());
    map.put("165322", hivMetadata.getMdcState().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "                  SELECT     mds5.patient_id, "
            + "                             MIN(otype5.value_coded) AS fifth_mds "
            + "                  FROM       patient mds5 "
            + "                  INNER JOIN encounter enc5 "
            + "                  ON         enc5.patient_id = mds5.patient_id "
            + "                  INNER JOIN obs otype5 "
            + "                  ON         otype5.encounter_id = enc5.encounter_id "
            + "                  INNER JOIN obs ostate5 "
            + "                  ON         ostate5.encounter_id = enc5.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds5.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     mds4.patient_id, "
            + "                             MIN(enc4.encounter_datetime) AS fourth_mds_date, "
            + "                             MIN(otype4.value_coded) AS fourth_mds "
            + "                  FROM       patient mds4 "
            + "                  INNER JOIN encounter enc4 "
            + "                  ON         enc4.patient_id = mds4.patient_id "
            + "                  INNER JOIN obs otype4 "
            + "                  ON         otype4.encounter_id = enc4.encounter_id "
            + "                  INNER JOIN obs ostate4 "
            + "                  ON         ostate4.encounter_id = enc4.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds4.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     mds3.patient_id, "
            + "                             MIN(enc3.encounter_datetime) AS third_mds_date, "
            + "                             MIN(otype3.value_coded) AS third_mds "
            + "                  FROM       patient mds3 "
            + "                  INNER JOIN encounter enc3 "
            + "                  ON         enc3.patient_id = mds3.patient_id "
            + "                  INNER JOIN obs otype3 "
            + "                  ON         otype3.encounter_id = enc3.encounter_id "
            + "                  INNER JOIN obs ostate3 "
            + "                  ON         ostate3.encounter_id = enc3.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds3.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     mds2.patient_id, "
            + "                             MIN(enc2.encounter_datetime) AS second_mds_date, "
            + "                             MIN(otype2.value_coded) AS second_mds "
            + "                  FROM       patient mds2 "
            + "                  INNER JOIN encounter enc2 "
            + "                  ON         enc2.patient_id = mds2.patient_id "
            + "                  INNER JOIN obs otype2 "
            + "                  ON         otype2.encounter_id = enc2.encounter_id "
            + "                  INNER JOIN obs ostate2 "
            + "                  ON         ostate2.encounter_id = enc2.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds2.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     p.patient_id, "
            + "                             MIN(e.encounter_datetime) AS first_mds_date, "
            + "                             MIN(otype.value_coded) AS first_mds "
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
            + "                  AND        e.encounter_datetime >= art.art_encounter "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND    (   ( otype.concept_id = ${165174} "
            + "                               AND otype.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate.concept_id = ${165322} "
            + "                                 AND  ostate.value_coded IN (${1256}) ) ) "
            + "                  AND  otype.obs_group_id = ostate.obs_group_id "
            + "       GROUP BY p.patient_id ) mds_1st "
            + "                  ON mds_1st.patient_id = mds2.patient_id "
            + "                  WHERE mds2.voided = 0 "
            + "                  AND enc2.voided = 0 "
            + "                  AND otype2.voided = 0 "
            + "                  AND ostate2.voided = 0 "
            + "                  AND enc2.encounter_datetime > mds_1st.first_mds_date "
            + "                  AND        enc2.encounter_datetime >= art.art_encounter "
            + "                  AND        enc2.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        enc2.encounter_type = ${6} "
            + "                  AND        enc2.location_id = :location "
            + "                  AND    (   ( otype2.concept_id = ${165174} "
            + "                               AND otype2.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate2.concept_id = ${165322} "
            + "                                 AND  ostate2.value_coded = ${1256} ) ) "
            + "                  AND  otype2.obs_group_id = ostate2.obs_group_id "
            + "       GROUP BY mds2.patient_id ) mds_2nd "
            + "                  ON mds_2nd.patient_id = mds3.patient_id "
            + "                  WHERE mds3.voided = 0 "
            + "                  AND enc3.voided = 0 "
            + "                  AND otype3.voided = 0 "
            + "                  AND ostate3.voided = 0 "
            + "                  AND enc3.encounter_datetime > mds_2nd.second_mds_date "
            + "                  AND        enc3.encounter_datetime >= art.art_encounter "
            + "                  AND        enc3.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        enc3.encounter_type = ${6} "
            + "                  AND        enc3.location_id = :location "
            + "                  AND    (   ( otype3.concept_id = ${165174} "
            + "                               AND otype3.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate3.concept_id = ${165322} "
            + "                                 AND  ostate3.value_coded = ${1256} ) ) "
            + "                  AND  otype3.obs_group_id = ostate3.obs_group_id "
            + "       GROUP BY mds3.patient_id ) mds_3rd "
            + "                  ON mds_3rd.patient_id = mds4.patient_id "
            + "                  WHERE mds4.voided = 0 "
            + "                  AND enc4.voided = 0 "
            + "                  AND otype4.voided = 0 "
            + "                  AND ostate4.voided = 0 "
            + "                  AND enc4.encounter_datetime > mds_3rd.third_mds_date "
            + "                  AND        enc4.encounter_datetime >= art.art_encounter "
            + "                  AND        enc4.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        enc4.encounter_type = ${6} "
            + "                  AND        enc4.location_id = :location "
            + "                  AND    (   ( otype4.concept_id = ${165174} "
            + "                               AND otype4.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate4.concept_id = ${165322} "
            + "                                 AND  ostate4.value_coded = ${1256} ) ) "
            + "                  AND  otype4.obs_group_id = ostate4.obs_group_id "
            + "       GROUP BY mds4.patient_id ) mds_4th "
            + "                  ON mds_4th.patient_id = mds5.patient_id "
            + "                  WHERE mds5.voided = 0 "
            + "                  AND enc5.voided = 0 "
            + "                  AND otype5.voided = 0 "
            + "                  AND ostate5.voided = 0 "
            + "                  AND enc5.encounter_datetime > mds_4th.fourth_mds_date "
            + "                  AND        enc5.encounter_datetime >= art.art_encounter "
            + "                  AND        enc5.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        enc5.encounter_type = ${6} "
            + "                  AND        enc5.location_id = :location "
            + "                  AND    (   ( otype5.concept_id = ${165174} "
            + "                               AND otype5.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate5.concept_id = ${165322} "
            + "                                 AND  ostate5.value_coded = ${1256} ) ) "
            + "                  AND  otype5.obs_group_id = ostate5.obs_group_id "
            + " AND mds5.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) "
            + "       GROUP BY mds5.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * MDS5 - Coluna AF - Data da consulta (Ficha Clínica) em que o quinto MDS foi marcado como
   * "Início"ocorrido entre data do início TARV e 12 meses do início TARV (Data da Consulta >= “Data
   * Início TARV” e <= “Data Início TARV” + 12 meses).
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getMds5StartDate(
      int numberOfMonths, int minCohortNumberOfYears, int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("B10- Data Início de MDS5: Coluna AF");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());
    map.put("165322", hivMetadata.getMdcState().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "                  SELECT     mds5.patient_id, "
            + "                             MIN(enc5.encounter_datetime) AS fifth_mds_date "
            + "                  FROM       patient mds5 "
            + "                  INNER JOIN encounter enc5 "
            + "                  ON         enc5.patient_id = mds5.patient_id "
            + "                  INNER JOIN obs otype5 "
            + "                  ON         otype5.encounter_id = enc5.encounter_id "
            + "                  INNER JOIN obs ostate5 "
            + "                  ON         ostate5.encounter_id = enc5.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds5.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     mds4.patient_id, "
            + "                             MIN(enc4.encounter_datetime) AS fourth_mds_date, "
            + "                             MIN(otype4.value_coded) AS fourth_mds "
            + "                  FROM       patient mds4 "
            + "                  INNER JOIN encounter enc4 "
            + "                  ON         enc4.patient_id = mds4.patient_id "
            + "                  INNER JOIN obs otype4 "
            + "                  ON         otype4.encounter_id = enc4.encounter_id "
            + "                  INNER JOIN obs ostate4 "
            + "                  ON         ostate4.encounter_id = enc4.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds4.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     mds3.patient_id, "
            + "                             MIN(enc3.encounter_datetime) AS third_mds_date, "
            + "                             MIN(otype3.value_coded) AS third_mds "
            + "                  FROM       patient mds3 "
            + "                  INNER JOIN encounter enc3 "
            + "                  ON         enc3.patient_id = mds3.patient_id "
            + "                  INNER JOIN obs otype3 "
            + "                  ON         otype3.encounter_id = enc3.encounter_id "
            + "                  INNER JOIN obs ostate3 "
            + "                  ON         ostate3.encounter_id = enc3.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds3.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     mds2.patient_id, "
            + "                             MIN(enc2.encounter_datetime) AS second_mds_date, "
            + "                             MIN(otype2.value_coded) AS second_mds "
            + "                  FROM       patient mds2 "
            + "                  INNER JOIN encounter enc2 "
            + "                  ON         enc2.patient_id = mds2.patient_id "
            + "                  INNER JOIN obs otype2 "
            + "                  ON         otype2.encounter_id = enc2.encounter_id "
            + "                  INNER JOIN obs ostate2 "
            + "                  ON         ostate2.encounter_id = enc2.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds2.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     p.patient_id, "
            + "                             MIN(e.encounter_datetime) AS first_mds_date, "
            + "                             MIN(otype.value_coded) AS first_mds "
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
            + "                  AND        e.encounter_datetime >= art.art_encounter "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND    (   ( otype.concept_id = ${165174} "
            + "                               AND otype.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate.concept_id = ${165322} "
            + "                                 AND  ostate.value_coded IN (${1256}) ) ) "
            + "                  AND  otype.obs_group_id = ostate.obs_group_id "
            + "       GROUP BY p.patient_id ) mds_1st "
            + "                  ON mds_1st.patient_id = mds2.patient_id "
            + "                  WHERE mds2.voided = 0 "
            + "                  AND enc2.voided = 0 "
            + "                  AND otype2.voided = 0 "
            + "                  AND ostate2.voided = 0 "
            + "                  AND enc2.encounter_datetime > mds_1st.first_mds_date "
            + "                  AND        enc2.encounter_datetime >= art.art_encounter "
            + "                  AND        enc2.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        enc2.encounter_type = ${6} "
            + "                  AND        enc2.location_id = :location "
            + "                  AND    (   ( otype2.concept_id = ${165174} "
            + "                               AND otype2.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate2.concept_id = ${165322} "
            + "                                 AND  ostate2.value_coded = ${1256} ) ) "
            + "                  AND  otype2.obs_group_id = ostate2.obs_group_id "
            + "       GROUP BY mds2.patient_id ) mds_2nd "
            + "                  ON mds_2nd.patient_id = mds3.patient_id "
            + "                  WHERE mds3.voided = 0 "
            + "                  AND enc3.voided = 0 "
            + "                  AND otype3.voided = 0 "
            + "                  AND ostate3.voided = 0 "
            + "                  AND enc3.encounter_datetime > mds_2nd.second_mds_date "
            + "                  AND        enc3.encounter_datetime >= art.art_encounter "
            + "                  AND        enc3.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        enc3.encounter_type = ${6} "
            + "                  AND        enc3.location_id = :location "
            + "                  AND    (   ( otype3.concept_id = ${165174} "
            + "                               AND otype3.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate3.concept_id = ${165322} "
            + "                                 AND  ostate3.value_coded = ${1256} ) ) "
            + "                  AND  otype3.obs_group_id = ostate3.obs_group_id "
            + "       GROUP BY mds3.patient_id ) mds_3rd "
            + "                  ON mds_3rd.patient_id = mds4.patient_id "
            + "                  WHERE mds4.voided = 0 "
            + "                  AND enc4.voided = 0 "
            + "                  AND otype4.voided = 0 "
            + "                  AND ostate4.voided = 0 "
            + "                  AND enc4.encounter_datetime > mds_3rd.third_mds_date "
            + "                  AND        enc4.encounter_datetime >= art.art_encounter "
            + "                  AND        enc4.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        enc4.encounter_type = ${6} "
            + "                  AND        enc4.location_id = :location "
            + "                  AND    (   ( otype4.concept_id = ${165174} "
            + "                               AND otype4.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate4.concept_id = ${165322} "
            + "                                 AND  ostate4.value_coded = ${1256} ) ) "
            + "                  AND  otype4.obs_group_id = ostate4.obs_group_id "
            + "       GROUP BY mds4.patient_id ) mds_4th "
            + "                  ON mds_4th.patient_id = mds5.patient_id "
            + "                  WHERE mds5.voided = 0 "
            + "                  AND enc5.voided = 0 "
            + "                  AND otype5.voided = 0 "
            + "                  AND ostate5.voided = 0 "
            + "                  AND enc5.encounter_datetime > mds_4th.fourth_mds_date "
            + "                  AND        enc5.encounter_datetime >= art.art_encounter "
            + "                  AND        enc5.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonths
            + " MONTH ) "
            + "                  AND        enc5.encounter_type = ${6} "
            + "                  AND        enc5.location_id = :location "
            + "                  AND    (   ( otype5.concept_id = ${165174} "
            + "                               AND otype5.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate5.concept_id = ${165322} "
            + "                                 AND  ostate5.value_coded = ${1256} ) ) "
            + "                  AND  otype5.obs_group_id = ostate5.obs_group_id "
            + " AND mds5.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) "
            + "       GROUP BY mds5.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * MDS5 - Coluna AG - Data da consulta (Ficha Clínica) em que o quinto MDS foi marcado como “Fim”,
   * ocorrido entre data do início TARV e 12 meses do início TARV (Data da Consulta >= “Data Início
   * TARV” e <= “Data Início TARV” + 12 meses).
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getMds5EndDate(
      int numberOfMonthss, int minCohortNumberOfYears, int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("B10- Data Fim de MDS5: Coluna AG");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());
    map.put("165322", hivMetadata.getMdcState().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("1267", hivMetadata.getCompletedConcept().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "                  SELECT     mds5_end.patient_id, "
            + "                             MIN(ee55.encounter_datetime) AS fifth_mds_end_date "
            + "                  FROM       patient mds5_end "
            + "                  INNER JOIN encounter ee55 "
            + "                  ON         ee55.patient_id = mds5_end.patient_id "
            + "                  INNER JOIN obs ot55 "
            + "                  ON         ot55.encounter_id = ee55.encounter_id "
            + "                  INNER JOIN obs os55 "
            + "                  ON         os55.encounter_id = ee55.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds5_end.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     mds5.patient_id, "
            + "                             MIN(enc5.encounter_datetime) AS fifth_mds_date, "
            + "                             MIN(otype5.value_coded) AS fifth_mds "
            + "                  FROM       patient mds5 "
            + "                  INNER JOIN encounter enc5 "
            + "                  ON         enc5.patient_id = mds5.patient_id "
            + "                  INNER JOIN obs otype5 "
            + "                  ON         otype5.encounter_id = enc5.encounter_id "
            + "                  INNER JOIN obs ostate5 "
            + "                  ON         ostate5.encounter_id = enc5.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds5.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     mds4.patient_id, "
            + "                             MIN(enc4.encounter_datetime) AS fourth_mds_date, "
            + "                             MIN(otype4.value_coded) AS fourth_mds "
            + "                  FROM       patient mds4 "
            + "                  INNER JOIN encounter enc4 "
            + "                  ON         enc4.patient_id = mds4.patient_id "
            + "                  INNER JOIN obs otype4 "
            + "                  ON         otype4.encounter_id = enc4.encounter_id "
            + "                  INNER JOIN obs ostate4 "
            + "                  ON         ostate4.encounter_id = enc4.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds4.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     mds3.patient_id, "
            + "                             MIN(enc3.encounter_datetime) AS third_mds_date, "
            + "                             MIN(otype3.value_coded) AS third_mds "
            + "                  FROM       patient mds3 "
            + "                  INNER JOIN encounter enc3 "
            + "                  ON         enc3.patient_id = mds3.patient_id "
            + "                  INNER JOIN obs otype3 "
            + "                  ON         otype3.encounter_id = enc3.encounter_id "
            + "                  INNER JOIN obs ostate3 "
            + "                  ON         ostate3.encounter_id = enc3.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds3.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     mds2.patient_id, "
            + "                             MIN(enc2.encounter_datetime) AS second_mds_date, "
            + "                             MIN(otype2.value_coded) AS second_mds "
            + "                  FROM       patient mds2 "
            + "                  INNER JOIN encounter enc2 "
            + "                  ON         enc2.patient_id = mds2.patient_id "
            + "                  INNER JOIN obs otype2 "
            + "                  ON         otype2.encounter_id = enc2.encounter_id "
            + "                  INNER JOIN obs ostate2 "
            + "                  ON         ostate2.encounter_id = enc2.encounter_id "
            + "                  INNER JOIN ( "
            + "                           SELECT art_patient.patient_id, "
            + "                                  art_patient.first_pickup AS art_encounter "
            + "                           FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "                           ) art_patient "
            + "                             ) art "
            + "                  ON         art.patient_id = mds2.patient_id "
            + "                  INNER JOIN ( "
            + "                  SELECT     p.patient_id, "
            + "                             MIN(e.encounter_datetime) AS first_mds_date, "
            + "                             MIN(otype.value_coded) AS first_mds "
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
            + "                  AND        e.encounter_datetime >= art.art_encounter "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonthss
            + " MONTH ) "
            + "                  AND    (   ( otype.concept_id = ${165174} "
            + "                               AND otype.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate.concept_id = ${165322} "
            + "                                 AND  ostate.value_coded IN (${1256}) ) ) "
            + "                  AND  otype.obs_group_id = ostate.obs_group_id "
            + "       GROUP BY p.patient_id ) mds_1st "
            + "                  ON mds_1st.patient_id = mds2.patient_id "
            + "                  WHERE mds2.voided = 0 "
            + "                  AND enc2.voided = 0 "
            + "                  AND otype2.voided = 0 "
            + "                  AND ostate2.voided = 0 "
            + "                  AND enc2.encounter_datetime > mds_1st.first_mds_date "
            + "                  AND        enc2.encounter_datetime >= art.art_encounter "
            + "                  AND        enc2.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonthss
            + " MONTH ) "
            + "                  AND        enc2.encounter_type = ${6} "
            + "                  AND        enc2.location_id = :location "
            + "                  AND    (   ( otype2.concept_id = ${165174} "
            + "                               AND otype2.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate2.concept_id = ${165322} "
            + "                                 AND  ostate2.value_coded = ${1256} ) ) "
            + "                  AND  otype2.obs_group_id = ostate2.obs_group_id "
            + "       GROUP BY mds2.patient_id ) mds_2nd "
            + "                  ON mds_2nd.patient_id = mds3.patient_id "
            + "                  WHERE mds3.voided = 0 "
            + "                  AND enc3.voided = 0 "
            + "                  AND otype3.voided = 0 "
            + "                  AND ostate3.voided = 0 "
            + "                  AND enc3.encounter_datetime > mds_2nd.second_mds_date "
            + "                  AND        enc3.encounter_datetime >= art.art_encounter "
            + "                  AND        enc3.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonthss
            + " MONTH ) "
            + "                  AND        enc3.encounter_type = ${6} "
            + "                  AND        enc3.location_id = :location "
            + "                  AND    (   ( otype3.concept_id = ${165174} "
            + "                               AND otype3.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate3.concept_id = ${165322} "
            + "                                 AND  ostate3.value_coded = ${1256} ) ) "
            + "                  AND  otype3.obs_group_id = ostate3.obs_group_id "
            + "       GROUP BY mds3.patient_id ) mds_3rd "
            + "                  ON mds_3rd.patient_id = mds4.patient_id "
            + "                  WHERE mds4.voided = 0 "
            + "                  AND enc4.voided = 0 "
            + "                  AND otype4.voided = 0 "
            + "                  AND ostate4.voided = 0 "
            + "                  AND enc4.encounter_datetime > mds_3rd.third_mds_date "
            + "                  AND        enc4.encounter_datetime >= art.art_encounter "
            + "                  AND        enc4.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonthss
            + " MONTH ) "
            + "                  AND        enc4.encounter_type = ${6} "
            + "                  AND        enc4.location_id = :location "
            + "                  AND    (   ( otype4.concept_id = ${165174} "
            + "                               AND otype4.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate4.concept_id = ${165322} "
            + "                                 AND  ostate4.value_coded = ${1256} ) ) "
            + "                  AND  otype4.obs_group_id = ostate4.obs_group_id "
            + "       GROUP BY mds4.patient_id ) mds_4th "
            + "                  ON mds_4th.patient_id = mds5.patient_id "
            + "                  WHERE mds5.voided = 0 "
            + "                  AND enc5.voided = 0 "
            + "                  AND otype5.voided = 0 "
            + "                  AND ostate5.voided = 0 "
            + "                  AND enc5.encounter_datetime > mds_4th.fourth_mds_date "
            + "                  AND        enc5.encounter_datetime >= art.art_encounter "
            + "                  AND        enc5.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonthss
            + " MONTH ) "
            + "                  AND        enc5.encounter_type = ${6} "
            + "                  AND        enc5.location_id = :location "
            + "                  AND    (   ( otype5.concept_id = ${165174} "
            + "                               AND otype5.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate5.concept_id = ${165322} "
            + "                                 AND  ostate5.value_coded = ${1256} ) ) "
            + "                  AND  otype5.obs_group_id = ostate5.obs_group_id "
            + "       GROUP BY mds5.patient_id ) mds_5th "
            + "                  ON mds_5th.patient_id = mds5_end.patient_id "
            + "                  WHERE mds5_end.voided = 0 "
            + "                  AND ee55.voided = 0 "
            + "                  AND ot55.voided = 0 "
            + "                  AND os55.voided = 0 "
            + "                  AND ee55.encounter_datetime > mds_5th.fifth_mds_date "
            + "                  AND        ee55.encounter_datetime >= art.art_encounter "
            + "                  AND        ee55.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + numberOfMonthss
            + " MONTH ) "
            + "                  AND        ee55.encounter_type = ${6} "
            + "                  AND        ee55.location_id = :location "
            + "                  AND    (   ( ot55.concept_id = ${165174} "
            + "                               AND ot55.value_coded = mds_5th.fifth_mds ) "
            + "                  AND         ( os55.concept_id = ${165322} "
            + "                                 AND  os55.value_coded = ${1267} ) ) "
            + "                  AND  ot55.obs_group_id = os55.obs_group_id "
            + " AND mds5_end.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) "
            + "       GROUP BY mds5_end.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF26 - Rastreado para TB em TODAS as consultas entre a data de inscrição no MDS e 12˚ mês de
   * TARV?- B.11 (Coluna AH)</b><br>
   * <br>
   *
   * <p>O sistema irá determinar se o utente foi “Rastreado para TB em TODAS as consultas entre a
   * data de inscrição no MDS e 12˚ mês de TARV?” da seguinte forma: <br>
   * <br>
   *
   * <p>Resposta= Sim, se o utente teve o registo de “Tem Sintomas=” com resposta = “Sim” ou “Não”
   * em todas as consultas clínicas (Ficha Clínica) decorrida entre “Data Início MDS” e 12 meses do
   * TARV (Data da Consulta >= “Data Início MDS” e <= “Data Início TARV” + 12 meses); <br>
   * <br>
   *
   * <p>Resposta= Não, se o utente não teve o registo de “Tem Sintomas=” com resposta = “Sim” ou
   * “Não” em pelo menos uma consulta clínica (Ficha Clínica) decorrida entre “Data Início MDS” e 12
   * meses do TARV (Data da Consulta >= “Data Início MDS” e <= “Data Início TARV” + 12 meses); <br>
   * <br>
   *
   * <p>Resposta= N/A, se o utente não teve registo do início do MDS; <br>
   * <br>
   *
   * <p>Nota 1: A “Data Início TARV” é definida no RF61<br>
   * <br>
   *
   * <p>Nota 2: A “Data Início MDS” (RF24) é a data mais antiga (primeira) entre as “Data Início 1º
   * MDS”, “Data Início 2º MDS”, “Data Início 3º MDS”, “Data Início 4º MDS”, “Data Início 5º MDS”..
   * <br>
   * <br>
   *
   * @param tbScreeningOrPbImc Boolean parameter, true for tbScreening and false for PbImc Concepts
   * @return {@link CohortDefinition}
   */
  public DataDefinition getTbScreeningSectionB(
      int minNumberOfMonths, int maxNumberOfMonths, boolean tbScreeningOrPbImc) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(
        "B11 - Identificação de Utente Rastreado para TB em TODAS as consultas entre a data de inscrição no MDS e 12˚ mês de TARV");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());
    map.put("165322", hivMetadata.getMdcState().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23758", hivMetadata.getTBSymptomsConcept().getConceptId());
    map.put("1343", commonMetadata.getMidUpperArmCircumferenceConcept().getConceptId());
    map.put("1342", commonMetadata.getBodyMassIndexConcept().getConceptId());

    String query =
        "SELECT     p.patient_id, "
            + " 'Sim' "
            + "FROM       patient p "
            + "INNER JOIN "
            + "           ( "
            + "                      SELECT     e.patient_id, "
            + "                                 Count(e.encounter_id) AS tb_consultations "
            + "                      FROM       encounter e "
            + "                      INNER JOIN obs o "
            + "                      ON         o.encounter_id = e.encounter_id "
            + "                      INNER JOIN ( "
            + " SELECT start.patient_id, "
            + "         start.first_pickup AS art_encounter "
            + "  FROM ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "        ) start   "
            + ") tarv ON tarv.patient_id = e.patient_id "
            + "                      INNER JOIN( "
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
            + "                  AND        e.encounter_datetime >= date_add( art.art_encounter, INTERVAL "
            + minNumberOfMonths
            + " MONTH ) "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + maxNumberOfMonths
            + " MONTH ) "
            + "                  AND    (   ( otype.concept_id = ${165174} "
            + "                               AND otype.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate.concept_id = ${165322} "
            + "                                 AND  ostate.value_coded IN (${1256}) ) ) "
            + "                  AND  otype.obs_group_id = ostate.obs_group_id "
            + "                  GROUP BY   p.patient_id ) mds "
            + "                 ON mds.patient_id = e.patient_id "
            + "                      WHERE      e.voided = 0 "
            + "                      AND        o.voided = 0 "
            + "                      AND        e.encounter_type = ${6} "
            + "                      AND        e.location_id = :location ";

    query +=
        tbScreeningOrPbImc
            ? "                      AND        o.concept_id = ${23758} "
                + "                      AND        o.value_coded IN ( ${1065}, "
                + "                                                   ${1066} ) "
            : " AND        ( o.concept_id IN ( ${1343}, ${1342} ) "
                + "    AND        o.value_numeric IS NOT NULL ) ";
    query +=
        "         AND        e.encounter_datetime >= mds.encounter_date "
            + "        AND        e.encounter_datetime <= date_add( tarv.art_encounter, interval 12 month ) "
            + "GROUP  BY e.patient_id) consultation_tb "
            + "ON         consultation_tb.patient_id = p.patient_id "
            + "INNER JOIN "
            + "           ( "
            + "                      SELECT     e.patient_id, "
            + "                                 count(e.encounter_id) AS nr_consultations "
            + "                      FROM       encounter e "
            + "                      INNER JOIN ( "
            + " SELECT start.patient_id, "
            + "         start.first_pickup AS art_encounter "
            + "  FROM ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "        ) start   "
            + ") tarv ON tarv.patient_id = e.patient_id "
            + "                      INNER JOIN( "
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
            + "                  ON         art.patient_id = e.patient_id "
            + "                  WHERE      p.voided = 0 "
            + "                  AND        e.voided = 0 "
            + "                  AND        otype.voided = 0 "
            + "                  AND        ostate.voided = 0 "
            + "                  AND        e.encounter_type = ${6} "
            + "                  AND        e.location_id = :location "
            + "                  AND        e.encounter_datetime >= date_add( art.art_encounter, INTERVAL "
            + minNumberOfMonths
            + " MONTH ) "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + maxNumberOfMonths
            + " MONTH ) "
            + "                  AND    (   ( otype.concept_id = ${165174} "
            + "                               AND otype.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate.concept_id = ${165322} "
            + "                                 AND  ostate.value_coded IN (${1256}) ) ) "
            + "                  AND  otype.obs_group_id = ostate.obs_group_id "
            + "                  GROUP BY   p.patient_id ) mds "
            + "                 ON mds.patient_id = e.patient_id "
            + "                      WHERE      e.voided = 0 "
            + "                      AND        e.encounter_type = ${6} "
            + "                      AND        e.location_id = :location "
            + "         AND        e.encounter_datetime >= mds.encounter_date "
            + "        AND        e.encounter_datetime <= date_add( tarv.art_encounter, interval 12 month ) "
            + "        GROUP BY   e.patient_id ) consultations "
            + "ON         consultations.patient_id = p.patient_id "
            + "WHERE      p.voided = 0 "
            + " AND consultation_tb.tb_consultations = consultations.nr_consultations "
            + " UNION "
            + "SELECT     p.patient_id, "
            + " 'Não' "
            + "FROM       patient p "
            + "INNER JOIN "
            + "           ( "
            + "                      SELECT     e.patient_id, "
            + "                                 Count(e.encounter_id) AS tb_consultations "
            + "                      FROM       encounter e "
            + "                      INNER JOIN obs o "
            + "                      ON         o.encounter_id = e.encounter_id "
            + "                      INNER JOIN ( "
            + " SELECT start.patient_id, "
            + "         start.first_pickup AS art_encounter "
            + "  FROM ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "        ) start   "
            + ") tarv ON tarv.patient_id = e.patient_id "
            + "                      INNER JOIN( "
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
            + "                  AND        e.encounter_datetime >= date_add( art.art_encounter, INTERVAL "
            + minNumberOfMonths
            + " MONTH ) "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + maxNumberOfMonths
            + " MONTH ) "
            + "                  AND    (   ( otype.concept_id = ${165174} "
            + "                               AND otype.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate.concept_id = ${165322} "
            + "                                 AND  ostate.value_coded IN (${1256}) ) ) "
            + "                  AND  otype.obs_group_id = ostate.obs_group_id "
            + "                  GROUP BY   p.patient_id ) mds "
            + "                 ON mds.patient_id = e.patient_id "
            + "                      WHERE      e.voided = 0 "
            + "                      AND        o.voided = 0 "
            + "                      AND        e.encounter_type = ${6} "
            + "                      AND        e.location_id = :location ";

    query +=
        tbScreeningOrPbImc
            ? "                      AND        o.concept_id != ${23758} "
            : "                      AND        o.concept_id NOT IN ( ${1343}, ${1342} ) ";
    query +=
        "         AND        e.encounter_datetime >= mds.encounter_date "
            + "        AND        e.encounter_datetime <= date_add( tarv.art_encounter, interval 12 month ) "
            + "GROUP  BY e.patient_id) consultation_tb "
            + "ON         consultation_tb.patient_id = p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  public DataDefinition getTbScreeningSectionC(
      boolean tbScreeningOrPbImc,
      int minNumberOfMonths,
      int maxNumberOfMonths,
      int minCohortNumberOfYears,
      int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(
        "B11 - Identificação de Utente Rastreado para TB em TODAS as consultas entre a data de inscrição no MDS e 12˚ mês de TARV");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());
    map.put("165322", hivMetadata.getMdcState().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23758", hivMetadata.getTBSymptomsConcept().getConceptId());
    map.put("1343", commonMetadata.getMidUpperArmCircumferenceConcept().getConceptId());
    map.put("1342", commonMetadata.getBodyMassIndexConcept().getConceptId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        " SELECT p.patient_id, "
            + "       CASE "
            + "         WHEN ( consultation_tb.tb_consultations = "
            + "                consultations.nr_consultations ) "
            + "       THEN 'Sim' "
            + "         WHEN ( consultation_tb.tb_consultations <> "
            + "                consultations.nr_consultations ) "
            + "       THEN 'Não' "
            + "       ELSE '' "
            + "       END AS tb_screening "
            + "FROM   patient p "
            + "       INNER JOIN (SELECT e.patient_id, "
            + "                          Count(e.encounter_id) AS tb_consultations "
            + "                   FROM   encounter e "
            + "                          INNER JOIN obs o "
            + "                                  ON o.encounter_id = e.encounter_id "
            + "                          INNER JOIN (SELECT start.patient_id, "
            + "                                             start.first_pickup AS art_encounter "
            + "                                      FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "   ) start) tarv "
            + "ON tarv.patient_id = e.patient_id "
            + "WHERE  e.voided = 0 "
            + "AND o.voided = 0 "
            + "AND e.encounter_type = ${6} "
            + "AND e.location_id = :location "
            + "AND e.encounter_datetime >= Date_add(tarv.art_encounter, INTERVAL "
            + minNumberOfMonths
            + " MONTH ) "
            + "AND e.encounter_datetime <= Date_add(tarv.art_encounter, INTERVAL "
            + maxNumberOfMonths
            + " MONTH ) ";

    query +=
        tbScreeningOrPbImc
            ? "                      AND        o.concept_id = ${23758} "
                + "                      AND        o.value_coded IN ( ${1065}, "
                + "                                                   ${1066} ) "
            : " AND        ( o.concept_id IN ( ${1343}, ${1342} ) "
                + "    AND        o.value_numeric IS NOT NULL ) ";

    query +=
        "GROUP  BY e.patient_id) consultation_tb "
            + "ON consultation_tb.patient_id = p.patient_id "
            + "INNER JOIN (SELECT e.patient_id, "
            + "Count(e.encounter_id) AS nr_consultations "
            + "FROM   encounter e "
            + "INNER JOIN (SELECT start.patient_id, "
            + "start.first_pickup AS art_encounter "
            + "FROM   ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + " ) start) tarv "
            + "ON tarv.patient_id = e.patient_id "
            + "WHERE  e.voided = 0 "
            + "AND e.encounter_type = ${6} "
            + "AND e.location_id = :location "
            + "AND e.encounter_datetime > Date_add(tarv.art_encounter, INTERVAL "
            + minNumberOfMonths
            + " MONTH ) "
            + "AND e.encounter_datetime <= Date_add(tarv.art_encounter, INTERVAL "
            + maxNumberOfMonths
            + " MONTH ) "
            + "GROUP  BY e.patient_id) consultations "
            + "ON consultations.patient_id = p.patient_id "
            + "WHERE  p.voided = 0"
            + " AND p.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF28 - Identificação de n˚ de consultas clínicas (Coluna AM)</b><br>
   * <br>
   *
   * <p>O sistema irá determinar o N˚ de consultas clínicas entre 6˚ e 12˚ mês de TARV contando o
   * número de consultas clínicas realizadas (Fichas clínicas) entre o 6º e 12º mês do TARV (Data da
   * Consulta <= “Data Início TARV” + 6 meses e >= “Data Início TARV” + 12 meses); <br>
   * <br>
   *
   * <p>Nota 1: A “Data Início TARV” é definida no RF61<br>
   * <br>
   * <br>
   *
   * @return {DataDefinition}
   */
  public DataDefinition getNrClinicalConsultations(
      int minNumberOfMonths,
      int maxNumberOfMonths,
      int minCohortNumberOfYears,
      int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("B16- Identificação de n˚ de consultas clínicas");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());
    map.put("165322", hivMetadata.getMdcState().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "                  SELECT     p.patient_id, "
            + "                             COUNT(e.encounter_id)  "
            + "                  FROM       patient p "
            + "                  INNER JOIN encounter e "
            + "                  ON         e.patient_id = p.patient_id "
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
            + "                  AND        e.encounter_type = ${6} "
            + "                  AND        e.location_id = :location "
            + "                  AND        e.encounter_datetime >= date_add( art.art_encounter, INTERVAL "
            + minNumberOfMonths
            + " MONTH ) "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + maxNumberOfMonths
            + " MONTH ) "
            + " AND p.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) "
            + "                  GROUP BY   p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF30 - Identificação de n˚ de consultas clínicas (Coluna AO)</b><br>
   * <br>
   *
   * <p>O sistema irá determinar o N˚ de consultas de APSS/PP entre 6˚ e 12˚ mês de TARV contando o
   * número de consultas de APSS/PP realizadas (Ficha APSS/PP) entre o 6º e 12º mês do TARV (Data da
   * Consulta <= “Data Início TARV” + 6 meses e >= “Data Início TARV” + 12 meses); <br>
   * <br>
   *
   * <p>Nota 1: A “Data Início TARV” é definida no RF61<br>
   * <br>
   * <br>
   *
   * @return {DataDefinition}
   */
  public DataDefinition getNrApssPpConsultations(
      int minNumberOfMonths,
      int maxNumberOfMonths,
      int minCohortNumberOfYears,
      int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName("B17- Identificação de n˚ de consultas apss/pp");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("35", hivMetadata.getPrevencaoPositivaSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());
    map.put("165322", hivMetadata.getMdcState().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "                  SELECT     p.patient_id, "
            + "                             COUNT(e.encounter_id)  "
            + "                  FROM       patient p "
            + "                  INNER JOIN encounter e "
            + "                  ON         e.patient_id = p.patient_id "
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
            + "                  AND        e.encounter_type = ${35} "
            + "                  AND        e.location_id = :location "
            + "                  AND        e.encounter_datetime >= date_add( art.art_encounter, INTERVAL "
            + minNumberOfMonths
            + " MONTH ) "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + maxNumberOfMonths
            + " MONTH ) "
            + " AND p.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) "
            + "                  GROUP BY   p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b> Estado de permanência no 12˚ mês de TARV- B.18 </b>
   * <li>Resposta = “Abandono”, os utentes em TARV que abandonaram o tratamento
   * <li>Resposta = “Óbito”, os utentes em TARV que foram óbito
   * <li>Resposta = “Suspenso”, os utentes em TARV que suspenderam o tratamento
   * <li>Resposta = “Transferido Para”, os utentes em TARV que foram transferidos para outra US
   * <li>Resposta = “Activo”, os utentes activos em TARV
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getLastStateOfStayOnTarv(
      int minCohortNumberOfYears, int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();

    sqlPatientDataDefinition.setName("Get the Last State of stay ");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "Location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("1", hivMetadata.getHIVCareProgram().getProgramId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("10", hivMetadata.getArtDeadWorkflowState().getProgramWorkflowStateId());
    map.put("8", hivMetadata.getSuspendedTreatmentWorkflowState().getProgramWorkflowStateId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put(
        "28",
        hivMetadata
            .getArtCareTransferredFromOtherHealthFacilityWorkflowState()
            .getProgramWorkflowStateId());
    map.put(
        "29",
        hivMetadata
            .getTransferredFromOtherHealthFacilityWorkflowState()
            .getProgramWorkflowStateId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6275", hivMetadata.getPreTarvConcept().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1366", hivMetadata.getPatientHasDiedConcept().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("1705", hivMetadata.getRestartConcept().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1709", hivMetadata.getSuspendedTreatmentConcept().getConceptId());

    String query =
        new EptsQueriesUtil()
            .unionBuilder(
                listOfPatientsWithMdsEvaluationQueries.getPatientsWhoAbandonedTarvQuery(
                    true, minCohortNumberOfYears, maxCohortNumberOfYears))
            .union(
                listOfPatientsWithMdsEvaluationQueries.getPatientsWhoDied(
                    true, minCohortNumberOfYears, maxCohortNumberOfYears))
            .union(
                listOfPatientsWithMdsEvaluationQueries
                    .getPatientsWhoSuspendedTarvOrAreTransferredOut(
                        hivMetadata
                            .getSuspendedTreatmentWorkflowState()
                            .getProgramWorkflowStateId(),
                        hivMetadata.getSuspendedTreatmentConcept().getConceptId(),
                        false,
                        false,
                        minCohortNumberOfYears,
                        maxCohortNumberOfYears))
            .union(
                listOfPatientsWithMdsEvaluationQueries
                    .getPatientsWhoHaveTransferredOutAsPermananceState(
                        minCohortNumberOfYears, maxCohortNumberOfYears))
            .union(
                listOfPatientsWithMdsEvaluationQueries.getPatientsActiveOnTarv(
                    minCohortNumberOfYears, maxCohortNumberOfYears))
            .buildQuery();

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  public DataDefinition getActiveClinicalCondiction(
      int minNumberOfMonths,
      int maxNumberOfMonths,
      boolean b7pedriod,
      int minCohortNumberOfYears,
      int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(
        "Identificação da condição clínica activa de estadio III ou IV – B7. C7 e D7");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());
    map.put("165322", hivMetadata.getMdcState().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("1406", hivMetadata.getOtherDiagnosis().getConceptId());
    map.put("42", tbMetadata.getPulmonaryTB().getConceptId());
    map.put("5334", hivMetadata.getCandidiaseOralConcept().getConceptId());
    map.put("1294", hivMetadata.getCryptococcalMeningitisConcept().getConceptId());
    map.put("1570", hivMetadata.getCervicalCancerConcept().getConceptId());
    map.put("5340", hivMetadata.getCandidiaseEsofagicaConcept().getConceptId());
    map.put("5344", hivMetadata.getHerpesSimplesConcept().getConceptId());
    map.put("14656", hivMetadata.getCachexiaConcept().getConceptId());
    map.put("7180", hivMetadata.getToxoplasmoseConcept().getConceptId());
    map.put("6990", hivMetadata.getHivDiseaseResultingInEncephalopathyConcept().getConceptId());
    map.put("507", commonMetadata.getKaposiSarcomaConcept().getConceptId());
    map.put("5042", tbMetadata.getExtraPulmonaryTbConcept().getConceptId());
    map.put("60", hivMetadata.getMeningitisConcept().getConceptId());
    map.put("5018", hivMetadata.getChronicDiarrheaConcept().getConceptId());
    map.put("5945", hivMetadata.getFeverConcept().getConceptId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "SELECT     p.patient_id, "
            + " 'Condição Clínica Activa de Estadio III ou IV' "
            + "FROM       patient p "
            + "INNER JOIN "
            + "           ( "
            + "                      SELECT     e.patient_id, "
            + "                                 MAX(e.encounter_datetime) AS clinical_condiction_date"
            + "                      FROM       encounter e "
            + "                      INNER JOIN obs o "
            + "                      ON         o.encounter_id = e.encounter_id "
            + "                      INNER JOIN ( "
            + " SELECT start.patient_id, "
            + "         start.first_pickup AS art_encounter "
            + "  FROM ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "        ) start   "
            + ") tarv ON tarv.patient_id = e.patient_id "
            + "                      INNER JOIN( "
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
            + "                  AND        e.encounter_datetime >= date_add( art.art_encounter, INTERVAL "
            + minNumberOfMonths
            + " MONTH ) "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + maxNumberOfMonths
            + " MONTH ) "
            + "                  AND    (   ( otype.concept_id = ${165174} "
            + "                               AND otype.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate.concept_id = ${165322} "
            + "                                 AND  ostate.value_coded IN (${1256}) ) ) "
            + "                  AND  otype.obs_group_id = ostate.obs_group_id "
            + "                  GROUP BY   p.patient_id ) mds "
            + "                 ON mds.patient_id = e.patient_id "
            + "                      WHERE      e.voided = 0 "
            + "                      AND        o.voided = 0 "
            + "                      AND        e.encounter_type = ${6} "
            + "                      AND        e.location_id = :location "
            + "                      AND        o.concept_id = ${1406} "
            + "                      AND        o.value_coded IN ( ${14656}, ${7180}, ${6990}, ${5344}, ${5340}, "
            + "                                                    ${1294}, ${5042}, ${507}, ${1570}, ${60}, "
            + "                                                    ${5018}, ${5945}, ${42} ) ";
    query +=
        b7pedriod
            ? "         AND        e.encounter_datetime >= mds.encounter_date "
                + "         AND        e.encounter_datetime <= date_add( tarv.art_encounter, interval 12 month ) "
            : "AND e.encounter_datetime > Date_add(tarv.art_encounter, INTERVAL "
                + minNumberOfMonths
                + " MONTH ) "
                + "AND e.encounter_datetime <= Date_add(tarv.art_encounter, INTERVAL "
                + maxNumberOfMonths
                + " MONTH ) ";
    query +=
        "GROUP  BY e.patient_id) clinical_condiction "
            + "ON         clinical_condiction.patient_id = p.patient_id "
            + " WHERE p.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b> RF26.1 </b> Recebeu uma forma de PF entre a data de inscrição no MDS e 12˚ mês de TARV? –
   * B.12
   *
   * <p>O sistema irá identificar se o utente “Recebeu uma forma de PF entre a data de inscrição no
   * MDS e 12˚ mês de TARV?” com as seguintes respostas:
   * <li>Resposta= Sim, se o utente teve registo de “Planeamento Familiar” = “Preservativo” ou
   *     “Contraceptivo Oral” ou “Injectável” ou ‘Implante” ou “Dispositivo Intra-uterino” ou
   *     “Laqueação das Trompas” ou “”Vasectomia” ou “Método Amenorreia Lactacional” ou “Outro”
   *     preenchido, em pelo menos uma consulta clínica (Ficha de Clínica) decorrida entre a data de
   *     inscrição no MDS e 12˚ mês de TARV (Data da Consulta >= “Data Inscrição MDS” e <= “Data
   *     Início TARV” + 12 meses)
   * <li>Resposta= Não, se o utente não teve registo de “Planeamento Familiar” = “Preservativo” ou
   *     “Contraceptivo Oral” ou “Injectável” ou ‘Implante” ou “Dispositivo Intra-uterino” ou
   *     “Laqueação das Trompas” ou “”Vasectomia” ou “Método Amenorreia Lactacional” ou “Outro”
   *     preenchido, em pelo menos uma consulta clínica (Ficha de Clínica) decorrida entre a data de
   *     inscrição no MDS e 12˚ mês de TARV (Data da Consulta >= “Data Inscrição MDS” e <= “Data
   *     Início TARV” + 12 meses)
   * <li>Resposta= N/A, se o utente não teve registo do início do MDS (Data Início MDS);
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getPatientsWhoReceivedPf(
      int minNumberOfMonths,
      int maxNumberOfMonths,
      boolean b12pedriod,
      int minCohortNumberOfYears,
      int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(
        "Identificação de utentes que receberam uma forma de PF durante o período de avaliação (B12, C12, D12)");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());
    map.put("165322", hivMetadata.getMdcState().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("374", commonMetadata.getMethodOfFamilyPlanningConcept().getConceptId());
    map.put("190", commonMetadata.getCondomsConcept().getConceptId());
    map.put("780", commonMetadata.getOralContraceptionConcept().getConceptId());
    map.put("5279", commonMetadata.getInjectibleContraceptiveConcept().getConceptId());
    map.put("21928", commonMetadata.getImplantConcept().getConceptId());
    map.put("5275", commonMetadata.getIntrauterineDeviceConcept().getConceptId());
    map.put("5276", commonMetadata.getFemaleSterilizationConcept().getConceptId());
    map.put("23714", commonMetadata.getVasectomyConcept().getConceptId());
    map.put("23728", commonMetadata.getOtherFamilyPlanningConcept().getConceptId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "SELECT     p.patient_id, "
            + " 'Sim' "
            + "FROM       patient p "
            + "INNER JOIN "
            + "           ( "
            + "                      SELECT     e.patient_id, "
            + "                                 e.encounter_datetime AS clinical_date"
            + "                      FROM       encounter e "
            + "                      INNER JOIN obs o "
            + "                      ON         o.encounter_id = e.encounter_id "
            + "                      INNER JOIN ( "
            + " SELECT start.patient_id, "
            + "         start.first_pickup AS art_encounter "
            + "  FROM ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "        ) start   "
            + ") tarv ON tarv.patient_id = e.patient_id "
            + "                      INNER JOIN( "
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
            + "                  AND        e.encounter_datetime >= date_add( art.art_encounter, INTERVAL "
            + minNumberOfMonths
            + " MONTH ) "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + maxNumberOfMonths
            + " MONTH ) "
            + "                  AND    (   ( otype.concept_id = ${165174} "
            + "                               AND otype.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate.concept_id = ${165322} "
            + "                                 AND  ostate.value_coded IN (${1256}) ) ) "
            + "                  AND  otype.obs_group_id = ostate.obs_group_id "
            + "                  GROUP BY   p.patient_id ) mds "
            + "                 ON mds.patient_id = e.patient_id "
            + "                      WHERE      e.voided = 0 "
            + "                      AND        o.voided = 0 "
            + "                      AND        e.encounter_type = ${6} "
            + "                      AND        e.location_id = :location "
            + "                      AND        o.concept_id = ${374} "
            + "                      AND        o.value_coded IN ( ${190}, ${780}, ${5279}, ${21928}, "
            + "                                                    ${5275}, ${5276}, ${23714} ) "
            + "                      OR        o.concept_id = ${23728} "
            + "                      AND       o.value_text IS NOT NULL ";
    query +=
        b12pedriod
            ? "         AND        e.encounter_datetime >= mds.encounter_date "
                + "         AND        e.encounter_datetime <= date_add( tarv.art_encounter, interval 12 month ) "
            : "AND e.encounter_datetime > Date_add(tarv.art_encounter, INTERVAL "
                + minNumberOfMonths
                + " MONTH ) "
                + "AND e.encounter_datetime <= Date_add(tarv.art_encounter, INTERVAL "
                + maxNumberOfMonths
                + " MONTH ) ";
    query +=
        "GROUP  BY e.patient_id) planeamento_familiar "
            + "ON         planeamento_familiar.patient_id = p.patient_id "
            + " WHERE p.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) "
            + " UNION "
            + "SELECT     p.patient_id, "
            + " 'Não' "
            + "FROM       patient p "
            + "INNER JOIN "
            + "           ( "
            + "                      SELECT     e.patient_id, "
            + "                                 e.encounter_datetime AS clinical_date"
            + "                      FROM       encounter e "
            + "                      INNER JOIN obs o "
            + "                      ON         o.encounter_id = e.encounter_id "
            + "                      INNER JOIN ( "
            + " SELECT start.patient_id, "
            + "         start.first_pickup AS art_encounter "
            + "  FROM ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "        ) start   "
            + ") tarv ON tarv.patient_id = e.patient_id "
            + "                      INNER JOIN( "
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
            + "                  AND        e.encounter_datetime >= date_add( art.art_encounter, INTERVAL "
            + minNumberOfMonths
            + " MONTH ) "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + maxNumberOfMonths
            + " MONTH ) "
            + "                  AND    (   ( otype.concept_id = ${165174} "
            + "                               AND otype.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate.concept_id = ${165322} "
            + "                                 AND  ostate.value_coded IN (${1256}) ) ) "
            + "                  AND  otype.obs_group_id = ostate.obs_group_id "
            + "                  GROUP BY   p.patient_id ) mds "
            + "                 ON mds.patient_id = e.patient_id "
            + "WHERE e.patient_id NOT IN ( "
            + "    SELECT p.patient_id "
            + "    FROM patient p "
            + "             INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "             INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "    WHERE p.voided = 0 "
            + "      AND e.voided = 0 "
            + "      AND o.voided = 0 "
            + "      AND e.encounter_type = ${6} "
            + "      AND e.location_id = :location "
            + "      AND ( "
            + "        (o.concept_id = ${374} AND o.value_coded IN (${190}, ${780}, ${5279}, ${21928}, ${5275}, ${5276}, ${23714})) "
            + "         OR (o.concept_id = ${23728} AND o.value_text IS NOT NULL) "
            + "        ) ";
    query +=
        b12pedriod
            ? "         AND        e.encounter_datetime >= mds.encounter_date "
                + "         AND        e.encounter_datetime <= date_add( tarv.art_encounter, interval 12 month ) ) "
            : "AND e.encounter_datetime >= Date_add(Date_add(tarv.art_encounter, INTERVAL "
                + minNumberOfMonths
                + " MONTH ), INTERVAL 1 DAY) "
                + " AND e.encounter_datetime <= Date_add(tarv.art_encounter, INTERVAL "
                + maxNumberOfMonths
                + " MONTH ) ) ";
    query +=
        "AND e.voided = 0 "
            + "GROUP BY e.patient_id ) no_pf "
            + " ON no_pf.patient_id = p.patient_id "
            + " WHERE p.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b> RF26.2 </b> Recebeu TPT entre a data de inscrição no MDS e 12˚ mês de TARV? – B.13 (Coluna
   * AN) –
   *
   * <p>O sistema irá identificar se o utente “Recebeu TPT entre a data de inscrição no MDS e 12˚
   * mês de TARV?” com as seguintes respostas:
   * <li>Resposta= Sim, se o utente teve registo de “Profilaxia TPT” = “INH” ou “3HP” ou “1HP” ou
   *     “LFX”, e respectivo “Estado Profilaxia” =”Início” ou “Continua”, em pelo menos uma consulta
   *     clínica (“Ficha Clinica”) decorrida entre a data de inscrição no MDS e 12˚ mês de TARV
   *     (Data da Consulta >= “Data Inscrição MDS” e <= “Data Início TARV” + 12 meses)
   * <li>Resposta= Não, se o utente não teve registo “TPT” de “Profilaxia TPT” = “INH” ou “3HP” ou
   *     “1HP” ou “LFX”, e respectivo “Estado Profilaxia” =”Início” ou “Continua”, em todas
   *     consultas clínicas (“Ficha Clinica”) decorridas entre a data de inscrição no MDS e 12˚ mês
   *     de TARV (Data da Consulta >= “Data Inscrição MDS” e <= “Data Início TARV” + 12 meses). Ou,
   *     se o utente não teve nenhuma consulta clínica entre a data de inscrição no MDS e 12˚ mês de
   *     TARV.
   * <li>Resposta= N/A, se o utente não teve registo do início do MDS (Data Início MDS);
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getPatientsWhoReceivedTpt(
      int minNumberOfMonths,
      int maxNumberOfMonths,
      boolean b13pedriod,
      int minCohortNumberOfYears,
      int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(
        "Identificação de utentes que receberam TPT durante o período de avaliação (B13, C13, D13)");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());
    map.put("165322", hivMetadata.getMdcState().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23985", tbMetadata.getRegimeTPTConcept().getConceptId());
    map.put("165308", tbMetadata.getDataEstadoDaProfilaxiaConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("1257", hivMetadata.getContinueRegimenConcept().getConceptId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "SELECT     p.patient_id, "
            + " 'Sim' "
            + "FROM       patient p "
            + "INNER JOIN "
            + "           ( "
            + "                      SELECT     e.patient_id, "
            + "                                 e.encounter_datetime AS clinical_date"
            + "                      FROM       encounter e "
            + "                      INNER JOIN obs o "
            + "                      ON         o.encounter_id = e.encounter_id "
            + "                      INNER JOIN obs o2 "
            + "                      ON         o2.encounter_id = e.encounter_id "
            + "                      INNER JOIN ( "
            + " SELECT start.patient_id, "
            + "         start.first_pickup AS art_encounter "
            + "  FROM ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "        ) start   "
            + ") tarv ON tarv.patient_id = e.patient_id "
            + "                      INNER JOIN( "
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
            + "                  AND        e.encounter_datetime >= date_add( art.art_encounter, INTERVAL "
            + minNumberOfMonths
            + " MONTH ) "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + maxNumberOfMonths
            + " MONTH ) "
            + "                  AND    (   ( otype.concept_id = ${165174} "
            + "                               AND otype.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate.concept_id = ${165322} "
            + "                                 AND  ostate.value_coded IN (${1256}) ) ) "
            + "                  AND  otype.obs_group_id = ostate.obs_group_id "
            + "                  GROUP BY   p.patient_id ) mds "
            + "                 ON mds.patient_id = e.patient_id "
            + "                      WHERE      e.voided = 0 "
            + "                      AND        o.voided = 0 "
            + "                      AND        o2.voided = 0 "
            + "                      AND        e.encounter_type = ${6} "
            + "                      AND        e.location_id = :location "
            + "                      AND        o.concept_id = ${23985} AND o.value_coded IS NOT NULL "
            + "                      AND        o2.concept_id = ${165308} AND o2.value_coded IN (${1256}, ${1257}) ";
    query +=
        b13pedriod
            ? "         AND        e.encounter_datetime >= mds.encounter_date "
                + "         AND        e.encounter_datetime <= date_add( tarv.art_encounter, interval 12 month ) "
            : "AND e.encounter_datetime > Date_add(tarv.art_encounter, INTERVAL "
                + minNumberOfMonths
                + " MONTH ) "
                + "AND e.encounter_datetime <= Date_add(tarv.art_encounter, INTERVAL "
                + maxNumberOfMonths
                + " MONTH ) ";
    query +=
        "GROUP  BY e.patient_id) received_tpt "
            + "ON         received_tpt.patient_id = p.patient_id "
            + " WHERE p.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) "
            + " UNION "
            + "SELECT     p.patient_id, "
            + " 'Não' "
            + "FROM       patient p "
            + "INNER JOIN "
            + "           ( "
            + "                      SELECT     e.patient_id, "
            + "                                 e.encounter_datetime AS clinical_date"
            + "                      FROM       encounter e "
            + "                      INNER JOIN obs o "
            + "                      ON         o.encounter_id = e.encounter_id "
            + "                      INNER JOIN ( "
            + " SELECT start.patient_id, "
            + "         start.first_pickup AS art_encounter "
            + "  FROM ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "        ) start   "
            + ") tarv ON tarv.patient_id = e.patient_id "
            + "                      INNER JOIN( "
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
            + "                  AND        e.encounter_datetime >= date_add( art.art_encounter, INTERVAL "
            + minNumberOfMonths
            + " MONTH ) "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + maxNumberOfMonths
            + " MONTH ) "
            + "                  AND    (   ( otype.concept_id = ${165174} "
            + "                               AND otype.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate.concept_id = ${165322} "
            + "                                 AND  ostate.value_coded IN (${1256}) ) ) "
            + "                  AND  otype.obs_group_id = ostate.obs_group_id "
            + "                  GROUP BY   p.patient_id ) mds "
            + "                 ON mds.patient_id = e.patient_id "
            + "WHERE e.patient_id NOT IN ( "
            + "    SELECT p.patient_id "
            + "    FROM patient p "
            + "             INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "             INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "             INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "    WHERE p.voided = 0 "
            + "      AND e.voided = 0 "
            + "      AND o.voided = 0 "
            + "      AND o2.voided = 0 "
            + "      AND e.encounter_type = ${6} "
            + "      AND e.location_id = :location "
            + "      AND o.concept_id = ${23985} AND o.value_coded IS NOT NULL "
            + "      AND o2.concept_id = ${165308} AND o2.value_coded IN (${1256}, ${1257}) ";
    query +=
        b13pedriod
            ? "         AND        e.encounter_datetime >= mds.encounter_date "
                + "         AND        e.encounter_datetime <= date_add( tarv.art_encounter, interval 12 month ) ) "
            : "AND e.encounter_datetime >= Date_add(Date_add(tarv.art_encounter, INTERVAL "
                + minNumberOfMonths
                + " MONTH ), INTERVAL 1 DAY) "
                + "AND e.encounter_datetime <= Date_add(tarv.art_encounter, INTERVAL "
                + maxNumberOfMonths
                + " MONTH ) ) ";
    query +=
        "AND e.voided = 0 "
            + "GROUP BY e.patient_id ) no_tpt "
            + " ON no_tpt.patient_id = p.patient_id "
            + " WHERE p.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b>RF27.1 - Rastreado para Tensão Arterial em todas as consultas entre a data de inscrição no
   * MDS e 12 ̊ mês de TARV? – B.15 (Coluna AP</b><br>
   * <br>
   *
   * <p>O sistema irá identificar se o utente foi “Rastreado para Tensão Arterial em todas as
   * consultas entre a data de inscrição no MDS e 12 ̊ mês de TARV?” com as seguintes respostas:
   * <br>
   * <br>
   * <li>Resposta= Sim, se o utente foi teve registo de “Tensão Arterial” em todas as consultas
   *     clínicas (“Ficha Clinica”) decorridas entre a data de inscrição no MDS e 12˚ mês de TARV
   *     (Data da Consulta >= “Data Inscrição MDS (“Ficha Clinica”) decorridas entre a data de
   *     inscrição no MDS e 12˚ mês de TARV (Data da Consulta >= “Data Inscrição MDS
   * <li>Resposta= Não, se o utente não teve registo de “Tensão Arterial” em todas consultas
   *     clínicas (“Ficha Clinica”) decorridas entre a data de inscrição no MDS e 12˚ mês de TARV
   *     (Data da Consulta >= “Data Incrição MDS e <= “Data Início TARV” + 12 meses). Ou, se o
   *     utente não teve nenhuma consulta clínica entre data de inscrição no MDS e 12˚ mês de TARV.
   * <li>Resposta= N/A, se o utente não teve registo do início do MDS.
   *
   *     <p>Nota 1: A “Data Início TARV” é definida no RF61<br>
   *     <br>
   *
   *     <p>Nota 2: A “Data Início MDS” (RF24) é a data mais antiga (primeira) entre as “Data Início
   *     1º MDS”, “Data Início 2º MDS”, “Data Início 3º MDS”, “Data Início 4º MDS”, “Data Início 5º
   *     MDS”..
   *
   *     <p>Nota 3: O utente a ser considerado nesta definição iniciou TARV ou na coorte de 12 meses
   *     ou na coorte de 24 meses ou na coorte de 36 meses, conforme definido no RF4. <br>
   *
   * @return {@link CohortDefinition}
   */
  public DataDefinition getPatientWithArterialPressure(
      int minNumberOfMonths,
      int maxNumberOfMonths,
      boolean b15pedriod,
      int minCohortNumberOfYears,
      int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(
        "Identificação de utentes rastreados para TA em todas as consultas durante o período de avaliação (B15, C15, D15)");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());
    map.put("165322", hivMetadata.getMdcState().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("5085", commonMetadata.getSystolicBoodPressureConcept().getConceptId());
    map.put("5086", commonMetadata.getDiastolicBoodPressureConcept().getConceptId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "SELECT     p.patient_id, "
            + " 'Sim' "
            + "FROM       patient p "
            + "INNER JOIN "
            + "           ( "
            + "                      SELECT     e.patient_id, "
            + "                                 Count(e.encounter_id) AS arterial_tension_consultations "
            + "                      FROM       encounter e "
            + "                      INNER JOIN obs o "
            + "                      ON         o.encounter_id = e.encounter_id "
            + "                      INNER JOIN obs o2 "
            + "                      ON         o2.encounter_id = e.encounter_id "
            + "                      INNER JOIN ( "
            + " SELECT start.patient_id, "
            + "         start.first_pickup AS art_encounter "
            + "  FROM ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "        ) start   "
            + ") tarv ON tarv.patient_id = e.patient_id "
            + "                      INNER JOIN( "
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
            + "                  AND        e.encounter_datetime >= date_add( art.art_encounter, INTERVAL "
            + minNumberOfMonths
            + " MONTH ) "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + maxNumberOfMonths
            + " MONTH ) "
            + "                  AND    (   ( otype.concept_id = ${165174} "
            + "                               AND otype.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate.concept_id = ${165322} "
            + "                                 AND  ostate.value_coded IN (${1256}) ) ) "
            + "                  AND  otype.obs_group_id = ostate.obs_group_id "
            + "                  GROUP BY   p.patient_id ) mds "
            + "                 ON mds.patient_id = e.patient_id "
            + "                      WHERE      e.voided = 0 "
            + "                      AND        o.voided = 0 "
            + "                      AND        o2.voided = 0 "
            + "                      AND        e.encounter_type = ${6} "
            + "                      AND        e.location_id = :location "
            + "                      AND        o.concept_id = ${5085} AND o.value_numeric IS NOT NULL"
            + "                      AND        o2.concept_id = ${5086} AND o2.value_numeric IS NOT NULL";
    query +=
        b15pedriod
            ? "         AND        e.encounter_datetime >= mds.encounter_date "
                + "         AND        e.encounter_datetime <= date_add( tarv.art_encounter, interval 12 month ) "
            : " AND e.encounter_datetime > Date_add(tarv.art_encounter, INTERVAL "
                + minNumberOfMonths
                + " MONTH ) "
                + " AND e.encounter_datetime <= Date_add(tarv.art_encounter, INTERVAL "
                + maxNumberOfMonths
                + " MONTH ) ";
    query +=
        "GROUP  BY e.patient_id) arterial_consultation "
            + "ON         arterial_consultation.patient_id = p.patient_id "
            + "INNER JOIN "
            + "           ( "
            + "                      SELECT     e.patient_id, "
            + "                                 count(e.encounter_id) AS nr_consultations "
            + "                      FROM       encounter e "
            + "                      INNER JOIN ( "
            + " SELECT start.patient_id, "
            + "         start.first_pickup AS art_encounter "
            + "  FROM ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "        ) start   "
            + ") tarv ON tarv.patient_id = e.patient_id "
            + "                      INNER JOIN( "
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
            + "                  ON         art.patient_id = e.patient_id "
            + "                  WHERE      p.voided = 0 "
            + "                  AND        e.voided = 0 "
            + "                  AND        otype.voided = 0 "
            + "                  AND        ostate.voided = 0 "
            + "                  AND        e.encounter_type = ${6} "
            + "                  AND        e.location_id = :location "
            + "                  AND        e.encounter_datetime >= date_add( art.art_encounter, INTERVAL "
            + minNumberOfMonths
            + " MONTH ) "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + maxNumberOfMonths
            + " MONTH ) "
            + "                  AND    (   ( otype.concept_id = ${165174} "
            + "                               AND otype.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate.concept_id = ${165322} "
            + "                                 AND  ostate.value_coded IN (${1256}) ) ) "
            + "                  AND  otype.obs_group_id = ostate.obs_group_id "
            + "                  GROUP BY   p.patient_id ) mds "
            + "                 ON mds.patient_id = e.patient_id "
            + "                      WHERE      e.voided = 0 "
            + "                      AND        e.encounter_type = ${6} "
            + "                      AND        e.location_id = :location ";
    query +=
        b15pedriod
            ? "         AND        e.encounter_datetime >= mds.encounter_date "
                + "         AND        e.encounter_datetime <= date_add( tarv.art_encounter, interval 12 month ) "
            : " AND e.encounter_datetime > Date_add(tarv.art_encounter, INTERVAL "
                + minNumberOfMonths
                + " MONTH ) "
                + " AND e.encounter_datetime <= Date_add(tarv.art_encounter, INTERVAL "
                + maxNumberOfMonths
                + " MONTH ) ";
    query +=
        "        GROUP BY   e.patient_id ) consultations "
            + "ON         consultations.patient_id = p.patient_id "
            + "WHERE      p.voided = 0 "
            + " AND arterial_consultation.arterial_tension_consultations = consultations.nr_consultations "
            + " AND p.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) "
            + " UNION "
            + "SELECT     p.patient_id, "
            + " 'Não' "
            + "FROM       patient p "
            + "INNER JOIN "
            + "           ( "
            + "                      SELECT     e.patient_id, "
            + "                                 Count(e.encounter_id) AS arterial_tension_consultations "
            + "                      FROM       encounter e "
            + "                      INNER JOIN obs o "
            + "                      ON         o.encounter_id = e.encounter_id "
            + "                      INNER JOIN obs o2 "
            + "                      ON         o2.encounter_id = e.encounter_id "
            + "                      INNER JOIN ( "
            + " SELECT start.patient_id, "
            + "         start.first_pickup AS art_encounter "
            + "  FROM ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "        ) start   "
            + ") tarv ON tarv.patient_id = e.patient_id "
            + "                      INNER JOIN( "
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
            + "                  AND        e.encounter_datetime >= date_add( art.art_encounter, INTERVAL "
            + minNumberOfMonths
            + " MONTH ) "
            + "                  AND        e.encounter_datetime <= date_add( art.art_encounter, INTERVAL "
            + maxNumberOfMonths
            + " MONTH ) "
            + "                  AND    (   ( otype.concept_id = ${165174} "
            + "                               AND otype.value_coded IS NOT NULL ) "
            + "                  AND         ( ostate.concept_id = ${165322} "
            + "                                 AND  ostate.value_coded IN (${1256}) ) ) "
            + "                  AND  otype.obs_group_id = ostate.obs_group_id "
            + "                  GROUP BY   p.patient_id ) mds "
            + "                 ON mds.patient_id = e.patient_id "
            + "WHERE e.patient_id NOT IN ( "
            + "    SELECT p.patient_id "
            + "    FROM patient p "
            + "             INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "             INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "             INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "    WHERE p.voided = 0 "
            + "    AND        e.voided = 0 "
            + "    AND        o.voided = 0 "
            + "    AND        o2.voided = 0 "
            + "    AND        e.encounter_type = ${6} "
            + "    AND        e.location_id = :location "
            + "    AND        o.concept_id = ${5085} AND o.value_numeric IS NOT NULL "
            + "    AND        o2.concept_id  = ${5086} AND o2.value_numeric IS NOT NULL ";
    query +=
        b15pedriod
            ? "         AND        e.encounter_datetime >= mds.encounter_date "
                + "         AND        e.encounter_datetime <= date_add( tarv.art_encounter, interval 12 month ) ) "
            : " AND e.encounter_datetime >= Date_add(Date_add(tarv.art_encounter, INTERVAL "
                + minNumberOfMonths
                + " MONTH ), INTERVAL 1 DAY) "
                + " AND e.encounter_datetime <= Date_add(tarv.art_encounter, INTERVAL "
                + maxNumberOfMonths
                + " MONTH ) ) ";
    query +=
        "AND e.voided = 0 "
            + "GROUP BY e.patient_id ) no_arterial_tension "
            + " ON no_arterial_tension.patient_id = p.patient_id "
            + " AND p.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b> RF29.1 </b> Rastreado para CACUM entre o 1˚ e 12 meses de TARV? – B.18 (Coluna AS)
   *
   * <p>O sistema irá identificar se o utente foi “Rastreado para CACUM entre o 1˚ e 12 meses de
   * TARV?” com as seguintes respostas:
   * <li>Resposta= Sim, se o utente é do sexo feminino e teve registo de pedido do exame “Via”
   *     (“Investigações – Pedidos Laboratoriais”) ou teve registo de resultado do exame “Via” (
   *     “Positivo”, “Negativo” ou “Suspeita de Cancro”) em pelo menos uma consulta clínica (“Ficha
   *     Clinica”) decorrida entre 1˚ e 12º meses de TARV (Data da Consulta >= “Data Início TARV” e
   *     <= “Data Início TARV” + 12 meses)
   * <li>Resposta= Não, se o utente é do sexo feminino e não teve registo de pedido do exame “Via”
   *     (“Investigações – Pedidos Laboratoriais”) e não teve registo de resultado do exame “Via” (
   *     “Positivo”, “Negativo” ou “Suspeita de Cancro”) em todas consultas clínicas (“Ficha
   *     Clinica”) decorridas entre 1˚ e 12º meses de TARV (Data da Consulta >= “Data Início TARV” e
   *     <= “Data Início TARV” + 12 meses). Ou, se o utente não teve nenhuma consulta clínica entre
   *     1˚ e 12º meses de TARV.
   * <li>Resposta= N/A, se o utente não teve registo do início do MDS (Data Início MDS);
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getPatientsWithCacumScreening(
      int minNumberOfMonths,
      int maxNumberOfMonths,
      int minCohortNumberOfYears,
      int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(
        "Identificação de utentes rastreados para CACUM durante o período de avaliação (B18, C18, D18)");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());
    map.put("165322", hivMetadata.getMdcState().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());
    map.put("2094", hivMetadata.getResultadoViaConcept().getConceptId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "SELECT     p.patient_id, "
            + " 'Sim' "
            + "FROM       patient p "
            + "INNER JOIN "
            + "           ( "
            + "                      SELECT     p.patient_id, "
            + "                                 e.encounter_datetime AS cacum_date"
            + "                      FROM       patient p "
            + "                      INNER JOIN person pe  "
            + "                      ON         p.patient_id = pe.person_id "
            + "                      INNER JOIN encounter e "
            + "                      ON         p.patient_id = e.patient_id "
            + "                      INNER JOIN obs o "
            + "                      ON         o.encounter_id = e.encounter_id "
            + "                      INNER JOIN obs o2 "
            + "                      ON         o2.encounter_id = e.encounter_id "
            + "                      INNER JOIN ( "
            + " SELECT start.patient_id, "
            + "         start.first_pickup AS art_encounter "
            + "  FROM ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "        ) start   "
            + ") tarv ON tarv.patient_id = p.patient_id "
            + "                      WHERE      p.voided = 0 "
            + "                      AND        e.voided = 0 "
            + "                      AND        o.voided = 0 "
            + "                      AND        o2.voided = 0 "
            + "                      AND        pe.gender = 'F' "
            + "                      AND        e.encounter_type = ${6} "
            + "                      AND        e.location_id = :location "
            + "                      AND        ( (o.concept_id = ${23722} AND o.value_coded = ${2094}) "
            + "                                    OR ( o2.concept_id = ${2094} AND o2.value_coded IS NOT NULL) ) "
            + "AND e.encounter_datetime > Date_add(tarv.art_encounter, INTERVAL "
            + minNumberOfMonths
            + " MONTH ) "
            + "AND e.encounter_datetime <= Date_add(tarv.art_encounter, INTERVAL "
            + maxNumberOfMonths
            + " MONTH ) "
            + "GROUP  BY p.patient_id) cacum "
            + "ON         cacum.patient_id = p.patient_id "
            + " WHERE p.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) "
            + " UNION "
            + "SELECT     p.patient_id, "
            + " 'Não' "
            + "FROM       patient p "
            + "INNER JOIN "
            + "           ( "
            + "                      SELECT     p.patient_id, "
            + "                                 e.encounter_datetime AS cacum_date"
            + "                      FROM       patient p "
            + "                      INNER JOIN person pe  "
            + "                      ON         p.patient_id = pe.person_id "
            + "                      INNER JOIN encounter e "
            + "                      ON         p.patient_id = e.patient_id "
            + "                      INNER JOIN ( "
            + " SELECT start.patient_id, "
            + "         start.first_pickup AS art_encounter "
            + "  FROM ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "        ) start   "
            + ") tarv ON tarv.patient_id = p.patient_id "
            + "WHERE p.patient_id NOT IN ( "
            + "    SELECT p.patient_id "
            + "    FROM patient p "
            + "             INNER JOIN person pe ON p.patient_id = pe.person_id  "
            + "             INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "             INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "             INNER JOIN obs o2 ON o2.encounter_id = e.encounter_id "
            + "    WHERE p.voided = 0 "
            + "      AND e.voided = 0 "
            + "      AND o.voided = 0 "
            + "      AND o2.voided = 0 "
            + "      AND pe.gender = 'F' "
            + "      AND e.encounter_type = ${6} "
            + "      AND e.location_id = :location "
            + "      AND ( (o.concept_id = ${23722} AND o.value_coded = ${2094}) "
            + "             OR ( o2.concept_id = ${2094} AND o2.value_coded IS NOT NULL) ) "
            + "AND e.encounter_datetime >= Date_add(Date_add(tarv.art_encounter, INTERVAL "
            + minNumberOfMonths
            + " MONTH ), INTERVAL 1 DAY) "
            + "AND e.encounter_datetime <= Date_add(tarv.art_encounter, INTERVAL "
            + maxNumberOfMonths
            + " MONTH ) ) "
            + "AND p.voided = 0 "
            + "AND e.voided = 0 "
            + "AND pe.gender = 'F' "
            + "GROUP BY p.patient_id ) no_cacum "
            + " ON no_cacum.patient_id = p.patient_id "
            + " WHERE p.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }

  /**
   * <b> RF29.2 </b> Resultado positivo para CACUM entre o 1˚ e 12 meses de TARV? – B.19 (Coluna AT)
   *
   * <p>O sistema irá identificar se o utente teve “Resultado positivo para CACUM entre o 1˚ e 12
   * meses de TARV?” com as seguintes respostas:
   * <li>Resposta= Sim, se o utente é do sexo feminino e teve registo de resultado do exame “Via” =
   *     “Positivo” em pelo menos uma consulta clínica (“Ficha Clinica”) decorrida entre 1˚ e 12
   *     meses de TARV (Data da Consulta >= “Data Início TARV” e <= “Data Início TARV” + 12 meses)
   * <li>Resposta= Não, se o utente é do sexo feminino e não teve registo de resultado do exame
   *     “Via” = “Positivo” em todas as consultas clínicas (“Ficha Clinica”) decorridas entre 1˚ e
   *     12º meses de TARV (Data da Consulta >= “Data Início TARV” e <= “Data Início TARV” + 12
   *     meses). Ou, se o utente não teve nenhuma consulta clínica entre 1º e 12º meses de TARV.
   * <li>Resposta= N/A, se o utente não teve registo do início do MDS (Data Início MDS);
   *
   * @return {@link DataDefinition}
   */
  public DataDefinition getPatientsWithPositiveCacum(
      int minNumberOfMonths,
      int maxNumberOfMonths,
      int minCohortNumberOfYears,
      int maxCohortNumberOfYears) {
    SqlPatientDataDefinition sqlPatientDataDefinition = new SqlPatientDataDefinition();
    sqlPatientDataDefinition.setName(
        "Identificação de utentes que receberam resultado positivo para CACUM durante o período de avaliação (B19, C19, D19)");
    sqlPatientDataDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlPatientDataDefinition.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());
    map.put("1369", commonMetadata.getTransferFromOtherFacilityConcept().getConceptId());
    map.put("6300", hivMetadata.getTypeOfPatientTransferredFrom().getConceptId());
    map.put("6276", hivMetadata.getArtStatus().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());
    map.put("1066", hivMetadata.getNoConcept().getConceptId());
    map.put("165174", hivMetadata.getLastRecordOfDispensingModeConcept().getConceptId());
    map.put("165322", hivMetadata.getMdcState().getConceptId());
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("23865", hivMetadata.getArtPickupConcept().getConceptId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("2094", hivMetadata.getResultadoViaConcept().getConceptId());
    map.put("703", hivMetadata.getPositive().getConceptId());
    map.put("2", hivMetadata.getARTProgram().getProgramId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23891", hivMetadata.getDateOfMasterCardFileOpeningConcept().getConceptId());

    String query =
        "SELECT     p.patient_id, "
            + " 'Sim' "
            + "FROM       patient p "
            + "INNER JOIN "
            + "           ( "
            + "                      SELECT     p.patient_id, "
            + "                                 e.encounter_datetime AS positive_cacum_date"
            + "                      FROM       patient p "
            + "                      INNER JOIN person pe  "
            + "                      ON         p.patient_id = pe.person_id "
            + "                      INNER JOIN encounter e "
            + "                      ON         p.patient_id = e.patient_id "
            + "                      INNER JOIN obs o "
            + "                      ON         o.encounter_id = e.encounter_id "
            + "                      INNER JOIN ( "
            + " SELECT start.patient_id, "
            + "         start.first_pickup AS art_encounter "
            + "  FROM ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "        ) start   "
            + ") tarv ON tarv.patient_id = p.patient_id "
            + "                      WHERE      p.voided = 0 "
            + "                      AND        e.voided = 0 "
            + "                      AND        o.voided = 0 "
            + "                      AND        pe.gender = 'F' "
            + "                      AND        e.encounter_type = ${6} "
            + "                      AND        e.location_id = :location "
            + "                      AND        o.concept_id = ${2094} AND o.value_coded = ${703} "
            + "AND e.encounter_datetime > Date_add(tarv.art_encounter, INTERVAL "
            + minNumberOfMonths
            + " MONTH ) "
            + "AND e.encounter_datetime <= Date_add(tarv.art_encounter, INTERVAL "
            + maxNumberOfMonths
            + " MONTH ) "
            + "GROUP  BY p.patient_id) cacum_positive "
            + "ON         cacum_positive.patient_id = p.patient_id "
            + " WHERE p.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) "
            + " UNION "
            + "SELECT     p.patient_id, "
            + " 'Não' "
            + "FROM       patient p "
            + "INNER JOIN "
            + "           ( "
            + "                      SELECT     p.patient_id, "
            + "                                 e.encounter_datetime AS cacum_date"
            + "                      FROM       patient p "
            + "                      INNER JOIN person pe  "
            + "                      ON         p.patient_id = pe.person_id "
            + "                      INNER JOIN encounter e "
            + "                      ON         p.patient_id = e.patient_id "
            + "                      INNER JOIN ( "
            + " SELECT start.patient_id, "
            + "         start.first_pickup AS art_encounter "
            + "  FROM ( "
            + resumoMensalCohortQueries.getPatientStartedTarvBeforeQuery()
            + "        ) start   "
            + ") tarv ON tarv.patient_id = p.patient_id "
            + "WHERE p.patient_id IN ( "
            + "    SELECT p.patient_id "
            + "    FROM patient p "
            + "             INNER JOIN person pe ON p.patient_id = pe.person_id  "
            + "             INNER JOIN encounter e ON p.patient_id = e.patient_id "
            + "             INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "    WHERE p.voided = 0 "
            + "      AND e.voided = 0 "
            + "      AND o.voided = 0 "
            + "      AND pe.gender = 'F' "
            + "      AND e.encounter_type = ${6} "
            + "      AND e.location_id = :location "
            + "      AND o.concept_id = ${2094} AND o.value_coded NOT IN (${703}) "
            + "AND e.encounter_datetime >= Date_add(Date_add(tarv.art_encounter, INTERVAL "
            + minNumberOfMonths
            + " MONTH ), INTERVAL 1 DAY) "
            + "AND e.encounter_datetime <= Date_add(tarv.art_encounter, INTERVAL "
            + maxNumberOfMonths
            + " MONTH ) ) "
            + "AND p.voided = 0 "
            + "AND e.voided = 0 "
            + "AND pe.gender = 'F' "
            + "GROUP BY p.patient_id ) no_positive_cacum "
            + " ON no_positive_cacum.patient_id = p.patient_id "
            + " WHERE p.patient_id IN ( "
            + ListOfPatientsWithMdsEvaluationQueries.getCohortPatientsByYear(
                minCohortNumberOfYears, maxCohortNumberOfYears)
            + " ) ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlPatientDataDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlPatientDataDefinition;
  }
}
