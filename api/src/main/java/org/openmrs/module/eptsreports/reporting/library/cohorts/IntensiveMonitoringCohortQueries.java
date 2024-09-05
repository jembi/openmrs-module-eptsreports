package org.openmrs.module.eptsreports.reporting.library.cohorts;

import java.util.*;
import javax.annotation.PostConstruct;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.eptsreports.reporting.library.queries.IntensiveMonitoringQueries;
import org.openmrs.module.eptsreports.reporting.library.queries.QualityImprovement2020Queries;
import org.openmrs.module.eptsreports.reporting.utils.EptsQueriesUtil;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportConstants;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IntensiveMonitoringCohortQueries {

  private QualityImprovement2020CohortQueries qualityImprovement2020CohortQueries;

  private HivMetadata hivMetadata;

  private CommonCohortQueries commonCohortQueries;

  private CommonMetadata commonMetadata;

  private TbMetadata tbMetadata;

  private ResumoMensalCohortQueries resumoMensalCohortQueries;

  private GenericCohortQueries genericCohortQueries;

  private final EriDSDCohortQueries eriDSDCohortQueries;

  private final String MAPPING2 = "revisionEndDate=${revisionEndDate},location=${location}";

  private final String MAPPING3 =
      "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}";
  private final String MAPPING =
      "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},revisionEndDate=${revisionEndDate},location=${location}";

  @Autowired
  public IntensiveMonitoringCohortQueries(
      QualityImprovement2020CohortQueries qualityImprovement2020CohortQueries,
      HivMetadata hivMetadata,
      CommonCohortQueries commonCohortQueries,
      CommonMetadata commonMetadata,
      TbMetadata tbMetadata,
      GenericCohortQueries genericCohortQueries,
      ResumoMensalCohortQueries resumoMensalCohortQueries,
      EriDSDCohortQueries eriDSDCohortQueries) {
    this.qualityImprovement2020CohortQueries = qualityImprovement2020CohortQueries;
    this.hivMetadata = hivMetadata;
    this.commonCohortQueries = commonCohortQueries;
    this.commonMetadata = commonMetadata;
    this.tbMetadata = tbMetadata;
    this.genericCohortQueries = genericCohortQueries;
    this.resumoMensalCohortQueries = resumoMensalCohortQueries;
    this.eriDSDCohortQueries = eriDSDCohortQueries;
  }

  @PostConstruct
  public void init() {
    qualityImprovement2020CohortQueries.setIntensiveMonitoringCohortQueries(this);
  }

  /**
   * Get CAT 7.1, to 7.3 and 7.5 deno Monitoria Intensiva MQHIV 2021 for the selected location and
   * reporting period Section 7.1 (endDateRevision)
   *
   * @param den indicator number
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getCat7DenMI2021Part135Definition(Integer den) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 7.1, 7.3, 7.5 numerator and denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    if (den == 1 || den == 3) {
      cd.setName("MI 1 OR 3 A AND NOT (B1 OR B2 OR B3 OR C OR D OR E OR F)");
    } else if (den == 5) {
      cd.setName("(MI 5 A AND C) AND NOT (B1 OR B2 OR B3 OR D OR E OR F)");
    }
    CohortDefinition startedART = qualityImprovement2020CohortQueries.getMOHArtStartDate();

    CohortDefinition tbActive =
        commonCohortQueries.getMohMQPatientsOnCondition(
            false,
            false,
            "once",
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getActiveTBConcept(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition tbSymptoms =
        commonCohortQueries.getMohMQPatientsOnCondition(
            false,
            false,
            "once",
            hivMetadata.getAdultoSeguimentoEncounterType(),
            tbMetadata.getHasTbSymptomsConcept(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition tbTreatment =
        commonCohortQueries.getMohMQPatientsOnCondition(
            false,
            false,
            "once",
            hivMetadata.getAdultoSeguimentoEncounterType(),
            tbMetadata.getTBTreatmentPlanConcept(),
            Arrays.asList(
                tbMetadata.getStartDrugsConcept(),
                hivMetadata.getContinueRegimenConcept(),
                hivMetadata.getCompletedConcept()),
            null,
            null);

    CohortDefinition pregnant =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition breastfeeding =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition transferredIn =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    cd.addSearch(
        "A",
        EptsReportUtils.map(
            startedART,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));

    cd.addSearch(
        "B1",
        EptsReportUtils.map(
            tbActive,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));

    cd.addSearch(
        "B2",
        EptsReportUtils.map(
            tbSymptoms,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));

    cd.addSearch(
        "B3",
        EptsReportUtils.map(
            tbTreatment,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));

    cd.addSearch(
        "C",
        EptsReportUtils.map(
            pregnant,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "D",
        EptsReportUtils.map(
            breastfeeding,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "E",
        EptsReportUtils.map(
            transferredIn,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate},location=${location}"));

    if (den == 1 || den == 3) {
      cd.setCompositionString("A AND NOT (B1 OR B2 OR B3 OR C OR D OR E)");
    } else if (den == 5) {
      cd.setCompositionString("(A AND C) AND NOT (B1 OR B2 OR B3 OR D OR E)");
    }
    return cd;
  }

  /**
   * Get CAT 7.2, to 7.4 and 7.6 deno Monitoria Intensiva MQHIV 2021 for the selected location and
   * reporting period Section 7.1 (endDateRevision)
   *
   * @param den indicator number
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getCat7DenMI2021Part246Definition(Integer den) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 7.2, 7.4, 7.6 denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    if (den == 2 || den == 4) {
      cd.setName("MI DENOMINATOR 7.2 OR 7.4");
    } else if (den == 6) {
      cd.setName("MI DENOMINATOR 7.6");
    }

    // DEFINITIONS FROM RF20
    CohortDefinition rf20InclusionComposition = getMI7RF20InclusionComposition();

    // DEFINITIONS FROM RF8
    CohortDefinition pregnant =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    // DEFINITIONS FROM RF9
    CohortDefinition breastfeeding =
        commonCohortQueries.getMOHPregnantORBreastfeeding(
            commonMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    // DEFINITIONS FROM RF6
    CohortDefinition transferredIn =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    // DEFINITIONS FROM RF7
    CohortDefinition transferredOut = getTranferredOutPatientsForMI7();

    cd.addSearch(
        "rf20Inclusion",
        EptsReportUtils.map(
            rf20InclusionComposition, "revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "pregnant",
        EptsReportUtils.map(
            pregnant,
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "breastfeeding",
        EptsReportUtils.map(
            breastfeeding,
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "transferredIn",
        EptsReportUtils.map(
            transferredIn,
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate},location=${location}"));

    cd.addSearch("transferredOut", EptsReportUtils.map(transferredOut, MAPPING2));

    if (den == 2 || den == 4) {
      cd.setCompositionString(
          "rf20Inclusion AND NOT (pregnant OR breastfeeding OR transferredIn OR transferredOut)");
    } else if (den == 6) {
      cd.setCompositionString(
          "(rf20Inclusion AND pregnant) AND NOT (breastfeeding OR transferredIn OR transferredOut)");
    }
    return cd;
  }

  /**
   * Get CAT 7.1, to 7.3 and 7.5 numerator Monitoria Intensiva MQHIV 2021 for the selected location
   * and reporting period Section 7 (endDateRevision)
   *
   * @param num indicator number
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getCat7NumMI2021Part135Definition(Integer num) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    if (num == 1 || num == 3) {
      compositionCohortDefinition.setName(
          "MI NUM 1 OR 3 (A AND B4) AND NOT (B1 OR B2 OR B3 OR C OR D OR E OR F)");
    } else if (num == 5) {
      compositionCohortDefinition.setName(
          "MI NUM 5(A AND C AND B4) AND NOT (B1 OR B2 OR B3 OR D OR E OR F)");
    }
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    CohortDefinition denominator = null;

    // CONDITION TO CALL RESPECTIVE DENOMINATOR
    if (num == 1) {
      denominator = getCat7DenMI2021Part135Definition(1);
    } else if (num == 3) {
      denominator = getCat7DenMI2021Part135Definition(3);
    } else if (num == 5) {
      denominator = getCat7DenMI2021Part135Definition(5);
    }

    CohortDefinition b41 = qualityImprovement2020CohortQueries.getB4And1();

    CohortDefinition b42 = qualityImprovement2020CohortQueries.getB4And2();

    CohortDefinition b51 = qualityImprovement2020CohortQueries.getB5And1();

    CohortDefinition b52 = qualityImprovement2020CohortQueries.getB5And2();

    compositionCohortDefinition.addSearch(
        "B41",
        EptsReportUtils.map(
            b41,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));

    compositionCohortDefinition.addSearch(
        "B42",
        EptsReportUtils.map(
            b42,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));

    compositionCohortDefinition.addSearch(
        "B51",
        EptsReportUtils.map(
            b51,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));

    compositionCohortDefinition.addSearch(
        "B52",
        EptsReportUtils.map(
            b52,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));

    compositionCohortDefinition.addSearch("DENOMINATOR", Mapped.mapStraightThrough(denominator));

    compositionCohortDefinition.setCompositionString("DENOMINATOR AND (B41 OR B42 OR B51 OR B52)");

    return compositionCohortDefinition;
  }

  /**
   * Get CAT 7.2, to 7.4 and 7.6 numerator Monitoria Intensiva MQHIV 2021 for the selected location
   * and reporting period Section 7 (endDateRevision)
   *
   * @param num indicator number
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getCat7NumMI2021Part246Definition(Integer num) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    if (num == 2 || num == 4) {
      compositionCohortDefinition.setName(
          "MI NUM 2 OR 4(A AND (B41 OR B42 OR B51 OR B52) AND (GNEW OR L)) AND NOT (B1 OR B2 OR B3 OR C OR D OR E OR F OR H OR H1 OR I OR I1 OR J OR J1)");
    } else if (num == 6) {
      compositionCohortDefinition.setName(
          "MI NUM 6 (A AND (B41 OR B42 OR B51 OR B52) AND C AND (GNEW OR L)) AND NOT (B1 OR B2 OR B3 OR D OR E OR F OR H OR H1 OR I OR I1 OR J OR J1)");
    }
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    CohortDefinition denominator = null;

    // CONDITION TO CALL RESPECTIVE DENOMINATOR
    if (num == 2) {
      denominator = getCat7DenMI2021Part246Definition(2);
    } else if (num == 4) {
      denominator = getCat7DenMI2021Part246Definition(4);
    } else if (num == 6) {
      denominator = getCat7DenMI2021Part246Definition(6);
    }

    compositionCohortDefinition.addSearch(
        "GNEW",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getPatientsWithTptInhEnd(),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},revisionEndDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "L",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getPatientsWithTpt3hpEnd(),
            "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},revisionEndDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch("DENOMINATOR", Mapped.mapStraightThrough(denominator));

    compositionCohortDefinition.setCompositionString("DENOMINATOR AND (GNEW OR L)");

    return compositionCohortDefinition;
  }

  /**
   * MEPTS-862_MI_REPORT_CAT13_P2 Get CAT 13.15, 13.16 and 13.17 P2 for Numerator and Denominator
   * Monitoria Intensiva MQHIV 2021 for the selected location and reporting period Section
   * (endDateRevision)
   *
   * @param level indicator number
   * @param type indicator
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getMICat13Part2(Integer level, String type) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.15, 13.16 and 13.17 Numerator and Denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    // DENOMINATOR
    if (level == 15) {
      cd.addSearch(
          "MI13DEN15",
          EptsReportUtils.map(
              qualityImprovement2020CohortQueries.getgetMQC13P2DenMGInIncluisionPeriod(),
              "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"));
    } else if (level == 16) {
      cd.addSearch(
          "MI13DEN16", EptsReportUtils.map(getMQC13P2DenMGInIncluisionPeriod33Month(), MAPPING));
    } else if (level == 17) {
      cd.addSearch("MI13DEN17", EptsReportUtils.map(getMIC13Den17(), MAPPING));
    }

    // NUMERATOR
    if (level == 15) {
      cd.addSearch(
          "MI13NUM15",
          EptsReportUtils.map(
              qualityImprovement2020CohortQueries.getMQC13P2Num1(),
              "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"));
    } else if (level == 16) {
      cd.addSearch("MI13NUM16", EptsReportUtils.map(getMQC13P2Num2(), MAPPING));
    } else if (level == 17) {
      cd.addSearch("MI13NUM17", EptsReportUtils.map(getMIC13Num17(), MAPPING));
    }

    if ("DEN15".equals(type)) {
      cd.setCompositionString("MI13DEN15");
    } else if ("DEN16".equals(type)) {
      cd.setCompositionString("MI13DEN16");
    } else if ("DEN17".equals(type)) {
      cd.setCompositionString("MI13DEN17");
    } else if ("NUM15".equals(type)) {
      cd.setCompositionString("MI13NUM15");
    } else if ("NUM16".equals(type)) {
      cd.setCompositionString("MI13NUM16");
    } else if ("NUM17".equals(type)) {
      cd.setCompositionString("MI13NUM17");
    }
    return cd;
  }

  /**
   * Get CAT 13.1, 13.4, 13.6, 13.7, 13.8, 13.13 Monitoria Intensiva MQHIV 2021 for the selected
   * location and reporting period Section (endDateRevision)
   *
   * @param level indicator number
   * @param type indicator flag
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getCat13Den(Integer level, Boolean type) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.1, 13.4, 13.6, 13.7, 13.8, 13.13 numerator and denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    String mapp =
        "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},revisionEndDate=${revisionEndDate},location=${location}";
    if (level == 1 && !type) {
      cd.addSearch("MI13DEN", EptsReportUtils.map(this.getMI13DEN1(), mapp));
    } else {
      cd.addSearch("MI13DEN", EptsReportUtils.map(this.getMI13(true, level), mapp));
      cd.addSearch("MI13NUM", EptsReportUtils.map(this.getMI13(false, level), mapp));
    }

    if (!type) {
      cd.setCompositionString("MI13DEN");
    } else if (type) {
      cd.setCompositionString("MI13NUM");
    }
    return cd;
  }

  /**
   * <b>MI13</b>: Monitoria Intensiva de HIV Category 13 <br>
   * <i></i><br>
   * <i> <b>DENOMINATOR (6,7,8):</b> B1 AND ((B2 AND NOT B2E) OR (B3 AND NOT B3E)) AND NOT (B4E OR
   * B5E)</i> <br>
   * <i></i><br>
   * <i> <b>NUMERATOR (1,6,7,8):</b> B1 AND ((B2 AND NOT B2E) OR (B3 AND NOT B3E)) AND NOT (B4E OR
   * B5E) AND C </i> <br>
   * </ul>
   *
   * @param den boolean parameter, true indicates denominator ,false indicates numerator
   * @param line indicator number
   * @return CohortDefinition <strong>Should</strong> Returns empty if there is no patient who meets
   *     the conditions <strong>Should</strong> fetch patients in category 13 MG of the MQ report
   */
  public CohortDefinition getMI13(Boolean den, Integer line) {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    if (den) {
      compositionCohortDefinition.setName("B AND NOT C AND NOT D");
    } else {
      compositionCohortDefinition.setName("(B AND G) AND NOT (C OR D)");
    }
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition lastClinical = commonCohortQueries.getMOHPatientsLastClinicalConsultation();

    CohortDefinition pregnant =
        commonCohortQueries.getNewMQPregnantORBreastfeeding(
            hivMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition brestfeeding =
        commonCohortQueries.getNewMQPregnantORBreastfeeding(
            hivMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition b2New =
        commonCohortQueries.getPatientsWithFirstTherapeuticLineOnLastClinicalEncounterB2NEW();

    CohortDefinition b2e = qualityImprovement2020CohortQueries.getMQC13DEN_B2E();

    CohortDefinition secondLine6Months =
        qualityImprovement2020CohortQueries.getPatientsOnRegimeArvSecondLineB2NEWP1_2();

    CohortDefinition changeRegimen6Months =
        commonCohortQueries.getMOHPatientsOnTreatmentFor6Months(
            true,
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getRegimenAlternativeToFirstLineConcept(),
            Arrays.asList(
                commonMetadata.getAlternativeFirstLineConcept(),
                commonMetadata.getRegimeChangeConcept(),
                hivMetadata.getNoConcept()));

    CohortDefinition B3E =
        commonCohortQueries.getMOHPatientsToExcludeFromTreatmentIn6Months(
            true,
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getMasterCardEncounterType(),
            commonMetadata.getRegimenAlternativeToFirstLineConcept(),
            commonMetadata.getAlternativeLineConcept(),
            Arrays.asList(
                commonMetadata.getAlternativeFirstLineConcept(),
                commonMetadata.getRegimeChangeConcept(),
                hivMetadata.getNoConcept()),
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getTherapeuticLineConcept(),
            Collections.singletonList(hivMetadata.getFirstLineConcept()));

    CohortDefinition abandonedExclusionInTheLastSixMonthsFromFirstLineDate =
        qualityImprovement2020CohortQueries
            .getPatientsWhoAbandonedInTheLastSixMonthsFromFirstLineDate();

    CohortDefinition abandonedExclusionByTarvRestartDate =
        qualityImprovement2020CohortQueries.getPatientsWhoAbandonedTarvOnArtRestartDate();

    CohortDefinition abandonedExclusionFirstLine = getpatientsWhoAbandonedOrRestartedTarv();

    CohortDefinition abandonedExclusionSecondLine =
        qualityImprovement2020CohortQueries.getPatientsWhoAbandonedTarvOnOnSecondLineDate();

    CohortDefinition restartdedExclusion =
        qualityImprovement2020CohortQueries.getPatientsWhoRestartedTarvAtLeastSixMonths();

    CohortDefinition B5E =
        commonCohortQueries.getMOHPatientsWithVLRequestorResultBetweenClinicalConsultations(
            false, true, 12);

    CohortDefinition G = qualityImprovement2020CohortQueries.getMQ13G();

    CohortDefinition FIRSTLINE =
        qualityImprovement2020CohortQueries.getUtentesPrimeiraLinha(
            QualityImprovement2020CohortQueries.UtentesPrimeiraLinhaPreposition.MI);

    CohortDefinition SECONDLINE =
        qualityImprovement2020CohortQueries.getUtentesSegundaLinha(
            QualityImprovement2020CohortQueries.UtentesSegundaLinhaPreposition.MI);

    CohortDefinition tbDiagnosisActive =
        qualityImprovement2020CohortQueries.getPatientsWithTbActiveOrTbTreatment();

    CohortDefinition denominator = getMI13DEN1();

    if (line == 1) {
      compositionCohortDefinition.addSearch(
          "age",
          EptsReportUtils.map(
              commonCohortQueries.getMOHPatientsAgeOnLastClinicalConsultationDate(15, null),
              "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));
    } else if (line == 4) {
      compositionCohortDefinition.addSearch(
          "age",
          EptsReportUtils.map(
              commonCohortQueries.getMOHPatientsAgeOnLastClinicalConsultationDate(15, null),
              "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));
    } else if (line == 6) {
      compositionCohortDefinition.addSearch(
          "age",
          EptsReportUtils.map(
              commonCohortQueries.getMOHPatientsAgeOnLastClinicalConsultationDate(0, 4),
              "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));
    } else if (line == 7) {
      compositionCohortDefinition.addSearch(
          "age",
          EptsReportUtils.map(
              commonCohortQueries.getMOHPatientsAgeOnLastClinicalConsultationDate(5, 9),
              "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));
    } else if (line == 8) {
      compositionCohortDefinition.addSearch(
          "age",
          EptsReportUtils.map(
              commonCohortQueries.getMOHPatientsAgeOnLastClinicalConsultationDate(10, 14),
              "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));
    } else if (line == 13) {
      compositionCohortDefinition.addSearch(
          "age",
          EptsReportUtils.map(
              commonCohortQueries.getMOHPatientsAgeOnLastClinicalConsultationDate(2, 14),
              "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));
    }

    compositionCohortDefinition.addSearch(
        "B1",
        EptsReportUtils.map(
            lastClinical, "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "B2NEW",
        EptsReportUtils.map(
            b2New,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "C",
        EptsReportUtils.map(
            pregnant, "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "D",
        EptsReportUtils.map(
            brestfeeding, "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "secondLineB2",
        EptsReportUtils.map(
            secondLine6Months,
            "startDate=${startDate},revisionEndDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "B3",
        EptsReportUtils.map(
            changeRegimen6Months,
            "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "B3E",
        EptsReportUtils.map(B3E, "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "B5E",
        EptsReportUtils.map(B5E, "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "G",
        EptsReportUtils.map(G, "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "B2E",
        EptsReportUtils.map(
            b2e,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "RESTARTED",
        EptsReportUtils.map(
            restartdedExclusion,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "RESTARTEDTARV",
        EptsReportUtils.map(
            abandonedExclusionByTarvRestartDate,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "ABANDONEDTARV",
        EptsReportUtils.map(
            abandonedExclusionInTheLastSixMonthsFromFirstLineDate,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "ABANDONED1LINE",
        EptsReportUtils.map(
            abandonedExclusionFirstLine,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));

    compositionCohortDefinition.addSearch(
        "ABANDONED2LINE",
        EptsReportUtils.map(
            abandonedExclusionSecondLine,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "DENOMINATOR",
        EptsReportUtils.map(
            denominator,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "FIRSTLINE",
        EptsReportUtils.map(
            FIRSTLINE,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "SECONDLINE",
        EptsReportUtils.map(
            SECONDLINE,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "tbDiagnosisActive",
        EptsReportUtils.map(
            tbDiagnosisActive,
            "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    if (den) {
      if (line == 6 || line == 7 || line == 8) {
        compositionCohortDefinition.setCompositionString(
            "B1 AND age AND FIRSTLINE AND NOT (C OR D OR tbDiagnosisActive)");
      } else if (line == 4) {
        compositionCohortDefinition.setCompositionString(
            "((B1 AND age) OR D) AND SECONDLINE AND NOT (C OR tbDiagnosisActive)");
      } else if (line == 13) {
        compositionCohortDefinition.setCompositionString(
            "((B1 AND age) AND SECONDLINE) AND NOT (C OR D OR tbDiagnosisActive)");
      }
    } else {
      if (line == 1) {
        compositionCohortDefinition.setCompositionString("DENOMINATOR AND G");
      } else if (line == 6 || line == 7 || line == 8) {
        compositionCohortDefinition.setCompositionString(
            "(B1 AND age AND FIRSTLINE AND NOT (C OR D OR tbDiagnosisActive)) AND G");
      } else if (line == 4) {
        compositionCohortDefinition.setCompositionString(
            "(((B1 AND age) OR D) AND SECONDLINE AND NOT (C OR tbDiagnosisActive)) AND G");
      } else if (line == 13) {
        compositionCohortDefinition.setCompositionString(
            "(((B1 AND age) AND SECONDLINE) AND NOT (C OR D OR tbDiagnosisActive)) AND G");
      }
    }
    return compositionCohortDefinition;
  }

  /**
   * Get the indicators (denominators) from CATEGORY 11 from report named “Monitoria Intensiva de
   * HIV-2021” for the selected location and reporting month (endDateRevision)
   *
   * @param indicatorFlag indicator number
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getMIC11DEN(int indicatorFlag) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 11.1 to 11.7 denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    if (indicatorFlag == 1
        || indicatorFlag == 2
        || indicatorFlag == 3
        || indicatorFlag == 5
        || indicatorFlag == 6) {
      cd.addSearch(
          "MI11DEN",
          EptsReportUtils.map(
              qualityImprovement2020CohortQueries.getMQC11DEN(
                  indicatorFlag, EptsReportConstants.MIMQ.MI),
              "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"));
    } else if (indicatorFlag == 4 || indicatorFlag == 7) {
      cd.addSearch(
          "MI11DEN",
          EptsReportUtils.map(
              qualityImprovement2020CohortQueries.getMQC11DEN(
                  indicatorFlag, EptsReportConstants.MIMQ.MI),
              "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}"));
    }
    cd.setCompositionString("MI11DEN");
    return cd;
  }
  /**
   * Get CAT 12.1, 12.2, 12.5, 12.6, 12.9, 12.10 Monitoria Intensiva MQHIV 2021 for the selected
   * location and reporting period (endDateRevision)
   *
   * @param level indicator number
   * @param type indicator flag
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getCat12P1DenNum(Integer level, Boolean type) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 12.1, 12.2, 12.5, 12.6, 12.9, 12.10 numerator and denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    String MAPPING = "";
    if (level == 1 || level == 5 || level == 9)
      MAPPING =
          "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},revisionEndDate=${revisionEndDate},location=${location}";
    else
      MAPPING =
          "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}";
    cd.addSearch(
        "MI12P1DEN",
        EptsReportUtils.map(qualityImprovement2020CohortQueries.getMQ12DEN(level), MAPPING));
    cd.addSearch(
        "MI12P1NUM",
        EptsReportUtils.map(qualityImprovement2020CohortQueries.getMQ12NUM(level), MAPPING));
    if (!type) {
      cd.setCompositionString("MI12P1DEN");
    } else if (type) {
      cd.setCompositionString("MI12P1NUM");
    }
    return cd;
  }

  /**
   * Get CAT 13 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period
   * Section 13.2 Denominator (endDateRevision)
   *
   * @param indicator indicator number
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getMI13DEN2(Integer indicator) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.2 Denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MI13DEN2",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQC13P3DEN(indicator),
            "startDate=${revisionEndDate-11m+1d},endDate=${revisionEndDate-10m},revisionEndDate=${revisionEndDate},location=${location}"));
    cd.setCompositionString("MI13DEN2");
    return cd;
  }

  /**
   * Get CAT 13 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period
   * Section 13.2 Numerator (endDateRevision)
   *
   * @param indicator indicator number
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getMI13NUM2(Integer indicator) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.2 Numerator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MI13NUM2",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQC13P3NUM(indicator),
            "startDate=${revisionEndDate-11m+1d},endDate=${revisionEndDate-10m},revisionEndDate=${revisionEndDate},location=${location}"));
    cd.setCompositionString("MI13NUM2");
    return cd;
  }

  /**
   * Get CAT 13 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period
   * Section 13.5 Denominator (endDateRevision)
   *
   * @param indicator indicator number
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getMI13DEN5(Integer indicator) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.5 Denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MI13DEN5",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQC13P3DEN(indicator),
            "startDate=${revisionEndDate-11m+1d},endDate=${revisionEndDate-10m},location=${location}"));
    cd.setCompositionString("MI13DEN5");
    return cd;
  }

  /**
   * Get CAT 13 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period
   * Section 13.5 Numerator (endDateRevision)
   *
   * @param indicator indicator number
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getMI13NUM5(Integer indicator) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.5 Numerator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MI13NUM5",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQC13P3NUM(indicator),
            "startDate=${revisionEndDate-11m+1d},endDate=${revisionEndDate-10m},revisionEndDate=${revisionEndDate},location=${location}"));
    cd.setCompositionString("MI13NUM5");
    return cd;
  }

  /**
   * Get CAT 13 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period
   * Section 13.9 Denominator (endDateRevision)
   *
   * @param indicator indicator number
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getMI13DEN9(Integer indicator) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.9 Denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MI13DEN9",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQC13P3DEN(indicator),
            "startDate=${revisionEndDate-11m+1d},endDate=${revisionEndDate-10m},revisionEndDate=${revisionEndDate},location=${location}"));
    cd.setCompositionString("MI13DEN9");
    return cd;
  }

  /**
   * Get CAT 13 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period
   * Section 13.9 Numerator (endDateRevision)
   *
   * @param indicator indicator number
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getMI13NUM9(Integer indicator) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.9 Numerator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MI13NUM9",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQC13P3NUM(indicator),
            "startDate=${revisionEndDate-11m+1d},endDate=${revisionEndDate-10m},revisionEndDate=${revisionEndDate},location=${location}"));
    cd.setCompositionString("MI13NUM9");
    return cd;
  }

  /**
   * Get CAT 13 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period
   * Section 13.10 Denominator (endDateRevision)
   *
   * @param indicator indicator number
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getMI13DEN10(Integer indicator) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.10 Denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MI13DEN10",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQC13P3DEN(indicator),
            "startDate=${revisionEndDate-11m+1d},endDate=${revisionEndDate-10m},revisionEndDate=${revisionEndDate},location=${location}"));
    cd.setCompositionString("MI13DEN10");
    return cd;
  }

  /**
   * Get CAT 13 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period
   * Section 13.10 Numerator (endDateRevision)
   *
   * @param indicator indicator number
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getMI13NUM10(Integer indicator) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.10 Numerator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MI13NUM10",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQC13P3NUM(indicator),
            "startDate=${revisionEndDate-11m+1d},endDate=${revisionEndDate-10m},revisionEndDate=${revisionEndDate},location=${location}"));
    cd.setCompositionString("MI13NUM10");
    return cd;
  }

  /**
   * Get CAT 13 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period
   * Section 13.11 Denominator (endDateRevision)
   *
   * @param indicator indicator number
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getMI13DEN11(Integer indicator) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.11 Denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MI13DEN11",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQC13P3DEN(indicator),
            "startDate=${revisionEndDate-11m+1d},endDate=${revisionEndDate-10m},revisionEndDate=${revisionEndDate},location=${location}"));
    cd.setCompositionString("MI13DEN11");
    return cd;
  }

  /**
   * Get CAT 13 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period
   * Section 13.11 Numerator (endDateRevision)
   *
   * @param indicator indicator number
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getMI13NUM11(Integer indicator) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.11 Numerator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MI13NUM11",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQC13P3NUM(indicator),
            "startDate=${revisionEndDate-11m+1d},endDate=${revisionEndDate-10m},revisionEndDate=${revisionEndDate},location=${location}"));
    cd.setCompositionString("MI13NUM11");
    return cd;
  }

  /**
   * Get CAT 13 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period
   * Section 13.14 Denominator (endDateRevision)
   *
   * @param indicator indicator number
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getMI13DEN14(Integer indicator) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.14 Denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MI13DEN14",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQC13P3DEN(indicator),
            "startDate=${revisionEndDate-11m+1d},endDate=${revisionEndDate-10m},revisionEndDate=${revisionEndDate},location=${location}"));
    cd.setCompositionString("MI13DEN14");

    return cd;
  }

  /**
   * Get the indicators (numerators) from CATEGORY 11 from report named “Monitoria Intensiva de
   * HIV-2021” for the selected location and reporting month (endDateRevision)
   *
   * @param indicatorFlag indicator number
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getMIC11NUM(int indicatorFlag) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 11.1 to 11.7 Numerator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    String MAPPING =
        "startDate=${revisionEndDate-5m+1d},endDate=${revisionEndDate-4m},revisionEndDate=${revisionEndDate},location=${location}";
    if (indicatorFlag == 1) {
      cd.addSearch(
          "MI11NUM",
          EptsReportUtils.map(
              qualityImprovement2020CohortQueries.getMQC11NumAnotCnotDnotEnotFandGAdultss(
                  EptsReportConstants.MIMQ.MI),
              MAPPING));
    } else if (indicatorFlag == 2) {
      cd.addSearch(
          "MI11NUM",
          EptsReportUtils.map(
              qualityImprovement2020CohortQueries.getMQC11NumB1nB2notCnotDnotEnotEnotFnHandAdultss(
                  EptsReportConstants.MIMQ.MI),
              MAPPING));
    } else if (indicatorFlag == 3) {
      cd.addSearch(
          "MI11NUM",
          EptsReportUtils.map(
              qualityImprovement2020CohortQueries.getMQC11NumAnB3nCnotDnotEnotEnotFnG(
                  EptsReportConstants.MIMQ.MI),
              MAPPING));
    } else if (indicatorFlag == 4) {
      cd.addSearch(
          "MI11NUM",
          EptsReportUtils.map(
              qualityImprovement2020CohortQueries.getMQC11NumB1nB2nB3nCnotDnotEnotEnotFnH(
                  EptsReportConstants.MIMQ.MI),
              MAPPING));
    } else if (indicatorFlag == 5) {
      cd.addSearch(
          "MI11NUM",
          EptsReportUtils.map(
              qualityImprovement2020CohortQueries.getMQC11NumAnotCnotDnotEnotFnotGnChildren(
                  EptsReportConstants.MIMQ.MI),
              MAPPING));
    } else if (indicatorFlag == 6) {
      cd.addSearch(
          "MI11NUM",
          EptsReportUtils.map(
              qualityImprovement2020CohortQueries.getMQC11NumAnotCnotDnotEnotFnotIlessThan9Month(
                  EptsReportConstants.MIMQ.MI),
              MAPPING));
    } else if (indicatorFlag == 7) {
      cd.addSearch(
          "MI11NUM",
          EptsReportUtils.map(
              qualityImprovement2020CohortQueries.getMQC11NumB1nB2notCnotDnotEnotFnHChildren(
                  EptsReportConstants.MIMQ.MI),
              MAPPING));
    }
    cd.setCompositionString("MI11NUM");

    return cd;
  }
  /**
   * Get CAT 13 Monitoria Intensiva MQHIV 2021 for the selected location and reporting period
   * Section 13.14 Numerator (endDateRevision)
   *
   * @param indicator indicator number
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getMI13NUM14(Integer indicator) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.14 Numerator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addSearch(
        "MI13NUM14",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQC13P3NUM(indicator),
            "startDate=${revisionEndDate-11m+1d},endDate=${revisionEndDate-10m},revisionEndDate=${revisionEndDate},location=${location}"));
    cd.setCompositionString("MI13NUM14");

    return cd;
  }

  /**
   * Get CAT 13 P4 NUMERATOR AND DENOMINATOR Monitoria Intensiva MQHIV 2021 for the selected
   * location and reporting period (endDateRevision)
   *
   * @param level indicator number
   * @param type indicator flag
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getMICat13Part4(Integer level, Boolean type) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI 13.3, 13.12 AND 13.18 Numerator AND Denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    cd.addSearch(
        "MI13DEN",
        EptsReportUtils.map(qualityImprovement2020CohortQueries.getMQ13P4(true, level), MAPPING3));

    cd.addSearch(
        "MI13NUM",
        EptsReportUtils.map(qualityImprovement2020CohortQueries.getMQ13P4(false, level), MAPPING3));

    if (!type) {
      cd.setCompositionString("MI13DEN");
    } else if (type) {
      cd.setCompositionString("MI13NUM");
    }
    return cd;
  }

  /**
   * A - Select all patients with Last Clinical Consultation (encounter type 6, encounter_datetime)
   * occurred during the inclusion period (encounter_datetime>= startDateInclusion and <=
   * endDateInclusion
   *
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getMI15A() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("All patients with Last Clinical Consultation ");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());

    String query =
        "SELECT p.patient_id "
            + " FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + " WHERE  p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_type = ${6} "
            + "       AND  e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "       GROUP BY p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * B2 - Select all patients with the earliest “Data de Início TARV” (concept_id 1190, not null,
   * value_datetime) recorded in Ficha Resumo (encounter type 53, obs_datetime) and “Last
   * Consultation Date” (encounter_datetime from A) minus “Data de Início TARV” (concept id 1190
   * value_datetime) is greater than (>) 21 months
   */
  public CohortDefinition getMI15B2(Integer months) {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("B2 - All patients with the earliest “Data de Início TARV”");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());

    String query =
        "SELECT tabela.patient_id  "
            + "             FROM  "
            + "             (SELECT p.patient_id, min(o.value_datetime) as value_datetime  "
            + "             FROM patient p INNER JOIN encounter e on e.patient_id=p.patient_id  "
            + "             INNER JOIN obs o ON o.encounter_id=e.encounter_id  "
            + "             WHERE o.voided=0 AND e.voided=0 AND p.voided=0 AND  "
            + "             o.concept_id=${1190} AND o.value_datetime is not NULL AND  "
            + "             e.encounter_type=${53} AND  "
            + "             e.location_id= :location  "
            + "             GROUP by p.patient_id) as tabela  "
            + "             INNER JOIN ( "
            + "                 SELECT pp.patient_id, MAX(e.encounter_datetime) as encounter_datetime "
            + "             FROM   patient pp INNER JOIN encounter e  "
            + "             ON e.patient_id = pp.patient_id  "
            + "             WHERE  pp.voided = 0 AND e.voided = 0  "
            + "             AND e.location_id = :location  "
            + "             AND e.encounter_type = 6  "
            + "             AND  e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "             GROUP by pp.patient_id) as last_encounter "
            + "        ON last_encounter.patient_id=tabela.patient_id "
            + "WHERE timestampdiff(month,tabela.value_datetime,( last_encounter.encounter_datetime "
            + "             ))> "
            + months;
    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * <b> RF7.2 EXCLUSION FOR PATIENTS WHO ABANDONED IN THE LAST SIX MONTHS FROM FIRST LINE DATE</b>
   *
   * <p>O sistema irá identificar utentes que abandonaram ou reiniciaram o tratamento TARV durante
   * os últimos 6 meses anteriores a última consulta da seguinte forma:
   *
   * <p>incluindo os utentes com registo de “Mudança de Estado de Permanência” = “Abandono” ou
   * “Reinicio” na Ficha Clínica nos 6 meses anteriores a data da última consulta (“Data Consulta
   * Abandono/Reinicio” >= “Data Última Consulta” menos 6 meses e <= “Data última Consulta”).
   *
   * <p>incluindo os utentes com registo de “Mudança de Estado de Permanência” = “Abandono” ou
   * “Reinicio” na Ficha Resumo nos 6 meses anteriores a data da última consulta (“Data de Mudança
   * de Estado Permanência Abandono/Reinicio” ” >= “Data Última Consulta” menos 6 meses e <= “Data
   * última Consulta”).
   * <li>Nota: “Data Última Consulta” é a data da última consulta clínica ocorrida durante o período
   *     de revisão.
   *
   * @return CohortDefinition
   */
  public CohortDefinition getpatientsWhoAbandonedOrRestartedTarv() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("All patients who abandoned or restarted tarv during the period ");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("1707", hivMetadata.getAbandonedConcept().getConceptId());
    map.put("1705", hivMetadata.getRestartConcept().getConceptId());

    String query =
        "SELECT     p.patient_id "
            + "FROM       patient p "
            + "INNER JOIN encounter e "
            + "ON         e.patient_id=p.patient_id "
            + "INNER JOIN obs o "
            + "ON         o.encounter_id=e.encounter_id "
            + "INNER JOIN "
            + "           ( "
            + "                    SELECT   e.patient_id, "
            + "                             Max(e.encounter_datetime) AS last_encounter "
            + "                    FROM     encounter e "
            + "                    WHERE    e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "                    AND      e.voided=0 "
            + "                    AND      e.encounter_type = ${6} "
            + "                    GROUP BY e.patient_id )last_consultation "
            + "ON         last_consultation.patient_id = p.patient_id "
            + "WHERE      o.concept_id = ${6273} "
            + "AND        o.value_coded IN (${1707}, ${1705}) "
            + "AND        e.encounter_type = ${6} "
            + "AND        p.voided=0 "
            + "AND        e.voided=0 "
            + "AND        o.voided=0 "
            + "AND        e.location_id = :location "
            + "AND        e.encounter_datetime BETWEEN date_sub(last_consultation.last_encounter, interval 6 month) AND last_consultation.last_encounter "
            + "GROUP BY   p.patient_id "
            + "UNION "
            + "SELECT     p.patient_id "
            + "FROM       patient p "
            + "INNER JOIN encounter e "
            + "ON         e.patient_id=p.patient_id "
            + "INNER JOIN obs o "
            + "ON         o.encounter_id=e.encounter_id "
            + "INNER JOIN "
            + "           ( "
            + "                    SELECT   e.patient_id, "
            + "                             Max(e.encounter_datetime) AS last_encounter "
            + "                    FROM     encounter e "
            + "                    WHERE    e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "                    AND      e.voided=0 "
            + "                    AND      e.encounter_type = ${6} "
            + "                    GROUP BY e.patient_id )last_consultation "
            + "ON         last_consultation.patient_id = p.patient_id "
            + "WHERE      o.concept_id = ${6272} "
            + "AND        o.value_coded IN (${1707},${1705}) "
            + "AND        e.encounter_type = ${53} "
            + "AND        p.voided=0 "
            + "AND        e.voided=0 "
            + "AND        o.voided=0 "
            + "AND        e.location_id = :location "
            + "AND        o.obs_datetime BETWEEN date_sub(last_consultation.last_encounter, interval 6 month) AND last_consultation.last_encounter "
            + "GROUP BY   p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * D - All female patients registered as “Breastfeeding” (concept_id 6332, value_coded equal to
   * concept_id 1065) in Ficha Clínica (encounter type 6, encounter_datetime) occurred during the
   * following period (encounter_datetime >= startDate and <= endDate)
   *
   * @return @{@link CohortDefinition}
   */
  public CohortDefinition getMI15D() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("All female patients registered as “Breastfeeding”");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("6332", hivMetadata.getBreastfeeding().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());

    String query =
        "SELECT p.patient_id FROM patient p  "
            + " INNER JOIN encounter e ON e.patient_id=p.patient_id  "
            + " INNER JOIN obs o ON o.encounter_id=e.encounter_id "
            + " WHERE p.voided = 0 AND e.voided=0 AND e.location_id=:location "
            + " AND e.encounter_type= ${6} AND e.encounter_datetime BETWEEN :startDate AND :endDate  "
            + " AND o.concept_id = ${6332} AND o.value_coded= ${1065} AND o.voided=0 "
            + " GROUP BY p.patient_id ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   *
   *
   * <ul>
   *   <li>B1 – Select all patients with the earliest “Data de Início TARV” (concept_id 1190, not
   *       Tnull, value_datetime) recorded in Ficha Resumo (encounter type 53) and “Last
   *       Consultation Date” (encounter_datetime from A) minus “ Data de Início TARV” (concept id
   *       1190 value_datetime) is greater than (>) 3 months.
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMI15B1() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("B1 - All patients with the earliest “Data de Início TARV”");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1190", hivMetadata.getARVStartDateConcept().getConceptId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());

    String query =
        "SELECT tabela.patient_id  "
            + "             FROM  "
            + "             (SELECT p.patient_id, min(o.value_datetime) as value_datetime  "
            + "             FROM patient p INNER JOIN encounter e on e.patient_id=p.patient_id  "
            + "             INNER JOIN obs o ON o.encounter_id=e.encounter_id  "
            + "             WHERE o.voided=0 AND e.voided=0 AND p.voided=0 AND  "
            + "             o.concept_id=${1190} AND o.value_datetime is not NULL AND  "
            + "             e.encounter_type=${53} AND  "
            + "             e.location_id= :location  "
            + "             GROUP by p.patient_id) as tabela  "
            + "             INNER JOIN ( "
            + "                 SELECT pp.patient_id, MAX(e.encounter_datetime) as encounter_datetime "
            + "             FROM   patient pp INNER JOIN encounter e  "
            + "             ON e.patient_id = pp.patient_id  "
            + "             WHERE  pp.voided = 0 AND e.voided = 0  "
            + "             AND e.location_id = :location  "
            + "             AND e.encounter_type = 6  "
            + "             AND  e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "             GROUP by pp.patient_id) as last_encounter "
            + "        ON last_encounter.patient_id=tabela.patient_id "
            + "WHERE timestampdiff(month,tabela.value_datetime,( last_encounter.encounter_datetime "
            + "             ))>3";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   *
   *
   * <ul>
   *   <li>utentes com último resultado de Carga Viral (se existir) registado na “Ficha Clínica”
   *       (coluna 15) acima ou igual a de 1000 cópias, ou seja, último “Resultado Carga Viral” >=
   *       1000., até “Data Fim de Avaliação” (“Data de Recolha Dados” menos (-) 1 mês).
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMI15G() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("G - All patients with the last Viral Load Result");
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String query =
        " SELECT p.patient_id "
            + " FROM   patient p "
            + "       INNER JOIN encounter ee "
            + "               ON ee.patient_id = p.patient_id "
            + "       INNER JOIN obs oo"
            + "               ON oo.encounter_id = ee.encounter_id "
            + " WHERE  p.voided = 0 "
            + "       AND ee.voided = 0 "
            + "       AND oo.voided = 0 "
            + "       AND ee.location_id = :location "
            + "       AND ee.encounter_type = ${6} "
            + "       AND oo.concept_id = ${856} "
            + "       AND oo.value_numeric >= 1000 "
            + "       AND ee.encounter_datetime <=  :endDate "
            + " GROUP BY p.patient_id                        ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * C- All female patients registered as “Pregnant” (concept_id 1982, value_coded equal to
   * concept_id 1065) in Ficha Clínica (encounter type 6, encounter_datetime) occurred during the
   * following period (encounter_datetime >= startDate and <= endDate)
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMI15C() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("All female patients registered as “Pregnant”");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1982", hivMetadata.getPregnantConcept().getConceptId());
    map.put("1065", hivMetadata.getYesConcept().getConceptId());

    String query =
        "SELECT p.patient_id FROM patient p INNER JOIN person pr ON p.patient_id = pr.person_id  "
            + " INNER JOIN encounter e ON e.patient_id=p.patient_id  "
            + " INNER JOIN obs o ON o.encounter_id=e.encounter_id "
            + " WHERE p.voided = 0 AND e.voided=0 AND e.location_id=:location "
            + " AND e.encounter_type= ${6} AND e.encounter_datetime BETWEEN :startDate AND :endDate  "
            + " AND o.concept_id = ${1982} AND o.value_coded= ${1065} AND o.voided=0 AND pr.gender = 'F' "
            + " GROUP BY p.patient_id ";
    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   *
   *
   * <ul>
   *   <li>H - Select all patients with Viral Load result (concept id 856, value_numeric) >= 1000 on
   *       registered in Ficha Clinica (encounter type 6) on “Last Consultation” (encounter_datetime
   *       from A)
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMI15H() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Select all patients with Viral Load result");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());

    String query =
        " SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter ee "
            + "               ON ee.patient_id = p.patient_id "
            + "       INNER JOIN obs oo "
            + "               ON oo.encounter_id = ee.encounter_id "
            + " WHERE  p.voided = 0 "
            + "       AND ee.voided = 0 "
            + "       AND oo.voided = 0 "
            + "       AND ee.location_id = :location "
            + "       AND ee.encounter_type = ${6} "
            + "       AND oo.concept_id = ${856} "
            + "       AND oo.value_numeric >= 1000 "
            + "       AND ee.encounter_datetime = (SELECT "
            + "           Max(e.encounter_datetime) AS last_consultation_date "
            + "                                    FROM   patient pp "
            + "                                           INNER JOIN encounter e "
            + "                                                   ON e.patient_id = "
            + "                                                      pp.patient_id "
            + "                                    WHERE  pp.voided = 0 "
            + "                                           AND e.voided = 0 "
            + "                                           AND e.location_id = :location AND pp.patient_id=p.patient_id "
            + "                                           AND e.encounter_type = ${6} "
            + "                                           AND e.encounter_datetime BETWEEN "
            + "                                               :startDate AND :endDate "
            + "                                    LIMIT  1)";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }
  /**
   * J - Select all patients with at least one of the following models registered in Ficha Clinica
   * (encounter type 6, encounter_datetime) before “ Last Consultation Date” (encounter_datetime
   * from A): Last record of GAAC (concept id 23724) and the response is “Iniciar” (value_coded,
   * concept id 1256) or “Continua” (value_coded, concept id 1257) Last record of DT (concept id
   * 23730) and the response is “Iniciar” (value_coded, concept id 1256) or “Continua” (value_coded,
   * concept id 1257) Last record of DS (concept id 23888) and the response is “Iniciar”
   * (value_coded, concept id 1256) or “Continua” (value_coded, concept id 1257) Last record of FR
   * (concept id 23729) and the response is “ Iniciar” (value_coded, concept id 1256) or “Continua”
   * (value_coded, concept id 1257) Last record of DC (concept id 23731) and the response is
   * “Iniciar” (value_coded, concept id 1256) or “Continua” (value_coded, concept id 1257)*
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMI15J() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName(
        "J - All patients with at least one of the following models registered in Ficha Clinica");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1257", hivMetadata.getContinueRegimenConcept().getConceptId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("23724", hivMetadata.getGaac().getConceptId());
    map.put("23730", hivMetadata.getQuarterlyDispensation().getConceptId());
    map.put("23888", hivMetadata.getSemiannualDispensation().getConceptId());
    map.put("23729", hivMetadata.getRapidFlow().getConceptId());
    map.put("23731", hivMetadata.getCommunityDispensation().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "       INNER JOIN (SELECT p.patient_id, "
            + "                          Max(e.encounter_datetime) AS encounter_datetime "
            + "                   FROM   patient p "
            + "                          INNER JOIN encounter e "
            + "                                  ON e.patient_id = p.patient_id "
            + "                   WHERE  p.voided = 0 "
            + "                          AND e.voided = 0 "
            + "                          AND e.location_id = :location "
            + "                          AND e.encounter_type = ${6} "
            + "                          AND e.encounter_datetime BETWEEN "
            + "                              :startDate AND :endDate "
            + "                   GROUP  BY p.patient_id) last_consultation "
            + "               ON last_consultation.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_type = ${6} "
            + "       AND ( ( o.concept_id = ${23724} "
            + "               AND (o.value_coded = ${1257} OR o.value_coded = ${1256})) "
            + "              OR ( o.concept_id = ${23730} "
            + "                   AND (o.value_coded = ${1257} OR o.value_coded = ${1256})) "
            + "              OR ( o.concept_id = ${23888} "
            + "                   AND (o.value_coded = ${1257} OR o.value_coded = ${1256})) "
            + "              OR ( o.concept_id = ${23729} "
            + "                   AND (o.value_coded = ${1257} OR o.value_coded = ${1256})) "
            + "              OR ( o.concept_id = ${23731} "
            + "                   AND (o.value_coded = ${1257} OR o.value_coded = ${1256}))) "
            + "       AND e.encounter_datetime < last_consultation.encounter_datetime";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }
  /**
   * Os utentes com último resultado de CD4 (se existir) registado na “Ficha Clínica” (coluna 15)
   * abaixo ou igual a de 200, ou seja, último “Resultado CD4” <= 200 até a “Data Fim de Avaliação”
   * (“Data de Recolha Dados” menos (-) 1 mês), excepto os utentes que têm um registo de CV
   * disponível na Ficha Clinica, último até “Data Fim de Avaliação”( “Data de Recolha Dados” menos
   * (-) 1 mês)..
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMI15F() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("All patients with the last CD4 result");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1695", hivMetadata.getCD4AbsoluteOBSConcept().getConceptId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());
    map.put("165515", hivMetadata.getCD4SemiQuantitativeConcept().getConceptId());
    map.put("165513", hivMetadata.getCD4CountLessThanOrEqualTo200Concept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter ee ON ee.patient_id = p.patient_id "
            + "       INNER JOIN obs oo ON oo.encounter_id = ee.encounter_id "
            + "WHERE  p.voided = 0 "
            + "       AND ee.voided = 0 "
            + "       AND oo.voided = 0 "
            + "       AND ee.location_id = :location "
            + "       AND ee.encounter_type = ${6} "
            + "       AND ( ( oo.concept_id = ${1695} "
            + "       AND oo.value_numeric <= 200 ) "
            + "       OR ( oo.concept_id = ${165515} "
            + "       AND oo.value_coded = ${165513} ) ) "
            + "       AND ee.encounter_datetime <= :endDate "
            + "       AND NOT EXISTS (SELECT e.encounter_id "
            + "                       FROM   encounter e    "
            + "                              INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "                       WHERE  e.encounter_type = ${6} "
            + "                              AND e.location_id = :location "
            + "                              AND o.concept_id IN( ${856}, ${1305} ) "
            + "                              AND e.patient_id = p.patient_id "
            + "                              AND e.encounter_datetime <= :endDate "
            + "                              AND e.voided = 0 "
            + "                              AND o.voided = 0)"
            + " GROUP BY p.patient_id         ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }
  /**
   * K - Select all patients with at least one of the following models registered in Ficha Clinica
   * (encounter type 6, encounter_datetime) on “ Last Consultation Date” (encounter_datetime from
   * A): GAAC (concept id 23724) = “Iniciar” (value_coded, concept id 1256) DT (concept id 23730) =
   * “Iniciar” (value_coded, concept id 1256) DS (concept id 23888) = “Iniciar” (value_coded,
   * concept id 1256) FR (concept id 23729) = “Iniciar” (value_coded, concept id 1256) DC (concept
   * id 23731) = “Iniciar” (value_coded, concept id 1256)
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMI15K() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName(
        "K - All patients with at least one of the following models registered in Ficha Clinica");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1256", hivMetadata.getStartDrugs().getConceptId());
    map.put("23724", hivMetadata.getGaac().getConceptId());
    map.put("23730", hivMetadata.getQuarterlyDispensation().getConceptId());
    map.put("23888", hivMetadata.getSemiannualDispensation().getConceptId());
    map.put("23729", hivMetadata.getRapidFlow().getConceptId());
    map.put("23731", hivMetadata.getCommunityDispensation().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "       INNER JOIN (SELECT p.patient_id, "
            + "                          Max(e.encounter_datetime) AS encounter_datetime "
            + "                   FROM   patient p "
            + "                          INNER JOIN encounter e "
            + "                                  ON e.patient_id = p.patient_id "
            + "                   WHERE  p.voided = 0 "
            + "                          AND e.voided = 0 "
            + "                          AND e.location_id = :location "
            + "                          AND e.encounter_type = ${6} "
            + "                          AND e.encounter_datetime BETWEEN "
            + "                              :startDate AND :endDate "
            + "                   GROUP  BY p.patient_id) last_consultation "
            + "               ON last_consultation.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_type = ${6} "
            + "       AND ( ( o.concept_id = ${23724} "
            + "               AND o.value_coded = ${1256} ) "
            + "              OR ( o.concept_id = ${23730} "
            + "                   AND o.value_coded = ${1256} ) "
            + "              OR ( o.concept_id = ${23888} "
            + "                   AND o.value_coded = ${1256} ) "
            + "              OR ( o.concept_id = ${23729} "
            + "                   AND o.value_coded = ${1256} ) "
            + "              OR ( o.concept_id = ${23731} "
            + "                   AND o.value_coded = ${1256} ) ) "
            + "       AND e.encounter_datetime = last_consultation.encounter_datetime";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }
  /**
   * L - Select all patients with at least one of the following models registered in Ficha Clinica
   * (encounter type 6, encounter_datetime) on “ Last Consultation Date” (encounter_datetime from
   * A): GAAC (concept id 23724) = “Fim” (value_coded, concept id 1267) DT (concept id 23730) =
   * “Fim” (value_coded, concept id 1267) DS (concept id 23888) = “Fim” (value_coded, concept id
   * 1267) FR (concept id 23729) = “Fim” (value_coded, concept id 1267) DC (concept id 23731) =
   * “Fim” (value_coded, concept id 1267)
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMI15L() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName(
        "L - All patients with at least one of the following models registered in Ficha Clinica");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1267", hivMetadata.getCompletedConcept().getConceptId());
    map.put("23724", hivMetadata.getGaac().getConceptId());
    map.put("23730", hivMetadata.getQuarterlyDispensation().getConceptId());
    map.put("23888", hivMetadata.getSemiannualDispensation().getConceptId());
    map.put("23729", hivMetadata.getRapidFlow().getConceptId());
    map.put("23731", hivMetadata.getCommunityDispensation().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "       INNER JOIN (SELECT p.patient_id, "
            + "                          Max(e.encounter_datetime) AS encounter_datetime "
            + "                   FROM   patient p "
            + "                          INNER JOIN encounter e "
            + "                                  ON e.patient_id = p.patient_id "
            + "                   WHERE  p.voided = 0 "
            + "                          AND e.voided = 0 "
            + "                          AND e.location_id = :location "
            + "                          AND e.encounter_type = ${6} "
            + "                          AND e.encounter_datetime BETWEEN "
            + "                              :startDate AND :endDate "
            + "                   GROUP  BY p.patient_id) last_consultation "
            + "               ON last_consultation.patient_id = p.patient_id "
            + " WHERE  p.voided = 0 "
            + "       AND o.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND e.location_id = :location "
            + "       AND e.encounter_type = ${6} "
            + "       AND ( ( o.concept_id = ${23724} "
            + "               AND o.value_coded = ${1267} ) "
            + "              OR ( o.concept_id = ${23730} "
            + "                   AND o.value_coded = ${1267} ) "
            + "              OR ( o.concept_id = ${23888} "
            + "                   AND o.value_coded = ${1267} ) "
            + "              OR ( o.concept_id = ${23729} "
            + "                   AND o.value_coded = ${1267} ) "
            + "              OR ( o.concept_id = ${23731} "
            + "                   AND o.value_coded = ${1267} ) ) "
            + "       AND e.encounter_datetime = last_consultation.encounter_datetime";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * <b>E - Select all patients with the following Clinical Consultations or ARV Drugs Pick Ups:</b>
   *
   * <ul>
   *   <li>at least one Clinical Consultation (encounter type 6, encounter_datetime) or at least one
   *       ARV Pickup (encounter type 52, value_datetime(concept_id 23866)) between “Last Clinical
   *       Consultation” (encounter_datetime from A) minus 30 days and “Last Clinical Consultation”
   *       (encounter_datetime from A) minus 1 day
   *   <li>at least one Clinical Consultation (encounter type 6, encounter_datetime) or at least one
   *       ARV Pickup (encounter type 52, value_datetime(concept_id 23866)) between “Last Clinical
   *       Consultation” (encounter_datetime from A) minus 60 days and “Last Clinical Consultation”
   *       (encounter_datetime from A) minus 31 days
   *   <li>at least one Clinical Consultation (encounter type 6, encounter_datetime) or at least one
   *       ARV Pickup (encounter type 52, value_datetime(concept_id 23866)) between “Last Clinical
   *       Consultation” (encounter_datetime from A) minus 90 days and “Last Clinical Consultation”
   *       (encounter_datetime from A) minus 61 days
   * </ul>
   *
   * @param upper The upper limit in days
   * @param lower The lower limit in days
   * @return CohortDefinition
   */
  public CohortDefinition getMI15E(int upper, int lower) {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("E - All patients with the following Clinical Consultations or ARV Drugs Pick Ups");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());
    map.put("upper", upper);
    map.put("lower", lower);

    String query =
        "SELECT juncao.patient_id "
            + " FROM ( "
            + "         SELECT p.patient_id, e.encounter_datetime AS encounter_date "
            + "         FROM patient p "
            + "                  INNER JOIN encounter e on p.patient_id = e.patient_id "
            + "         WHERE p.voided = 0 "
            + "           AND e.voided = 0 "
            + " AND e.location_id =:location "
            + "           AND e.encounter_type = ${6} "
            + "         UNION "
            + "         SELECT p.patient_id, o.value_datetime AS encounter_date "
            + "         FROM patient p "
            + "            INNER JOIN encounter e on p.patient_id = e.patient_id "
            + "            INNER JOIN obs o on e.encounter_id = o.encounter_id "
            + "         WHERE p.voided = 0 "
            + "           AND e.voided = 0 "
            + "           AND o.voided = 0 "
            + " AND e.location_id =:location "
            + "           AND o.concept_id = ${23866} "
            + "           AND e.encounter_type = ${52} "
            + "     ) AS juncao "
            + "    INNER JOIN ( "
            + "                SELECT p.patient_id, MAX(e.encounter_datetime) AS encounter_datetime "
            + "                FROM  patient p "
            + "                    INNER JOIN encounter e on p.patient_id = e.patient_id "
            + "                WHERE p.voided =0 "
            + "                    AND  e.voided = 0 "
            + " AND e.location_id =:location "
            + "                    AND e.encounter_type = ${6} "
            + "                    AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "                GROUP BY p.patient_id "
            + "                 )  AS max_ficha on juncao.patient_id = max_ficha.patient_id "
            + "WHERE juncao.encounter_date "
            + "    BETWEEN DATE_SUB(max_ficha.encounter_datetime, INTERVAL ${upper} DAY) "
            + "        AND DATE_SUB(max_ficha.encounter_datetime, INTERVAL  ${lower} DAY)";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    cd.setQuery(stringSubstitutor.replace(query));

    return cd;
  }

  /**
   * E - Select all patients with the following Clinical Consultations or ARV Drugs Pick Ups: at
   * least one Clinical Consultation (encounter type 6, encounter_datetime) or at least one ARV
   * Pickup (encounter type 52, value_datetime(concept_id 23866)) between “Last Clinical
   * Consultation” ( encounter_datetime from A) minus 30 days and “Last Clinical Consultation”
   * (encounter_datetime from A) minus 1 day
   *
   * <p>AND
   *
   * <p>at least one Clinical Consultation (encounter type 6, encounter_datetime) or at least one
   * ARV Pickup (encounter type 52, value_datetime(concept_id 23866)) between “Last Clinical
   * Consultation” (encounter_datetime from A) minus 60 days and “Last Clinical Consultation” (
   * encounter_datetime from A) minus 31 days AND
   *
   * <p>at least one Clinical Consultation (encounter type 6, encounter_datetime) or at least one
   * ARV Pickup (encounter type 52, value_datetime(concept_id 23866)) between “Last Clinical
   * Consultation” (encounter_datetime from A) minus 90 days and “Last Clinical Consultation”
   * (encounter_datetime from A) minus 61 days
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMI15EComplete() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName(
        "Select all patients with the following Clinical Consultations or ARV Drugs Pick Ups");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition a = getMI15E(30, 1);
    CohortDefinition b = getMI15E(60, 31);
    CohortDefinition c = getMI15E(90, 61);

    cd.addSearch(
        "A",
        EptsReportUtils.map(a, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "B",
        EptsReportUtils.map(b, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "C",
        EptsReportUtils.map(c, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("A AND B AND C");

    return cd;
  }
  /**
   * I - Select all patients with the last Viral Load Result (concept id 856, value_numeric) < 1000
   * (value_numeric) OR Viral Load QUALITATIVE (concept id 1305) with value coded not null
   * registered on Ficha Clinica (encounter type 6) before “Last Consultation Date”
   * (encounter_datetime from A) minus 20 months, as “Last VL Result <1000”, and filter all patients
   * with at least one Viral Load Result (concept id 856, value_numeric not NULL) registered on
   * Ficha Clinica (encounter type 6, encounter_datetime) between “Last VL Result <1000”+ 10 months
   * and “Last VL Result <1000” + 20 months
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMI15I() {

    CohortDefinition cd = getMI15I(20, 10, 20);

    return cd;
  }

  public CohortDefinition getMI15I(
      Integer monthsBeforeClinical, Integer vlMonthsLower, Integer vlMonthsUpper) {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("I - All patients with the last Viral Load Result");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("1305", hivMetadata.getHivViralLoadQualitative().getConceptId());

    String query =
        "SELECT p.patient_id FROM patient p INNER JOIN encounter e on p.patient_id = e.patient_id INNER JOIN obs o ON o.encounter_id=e.encounter_id  "
            + " INNER JOIN ("
            + "  SELECT patient_id, MAX(encounter_date) encounter_date "
            + "  FROM ( "
            + "    SELECT juncao.patient_id,juncao.encounter_date "
            + "      FROM ( "
            + "         SELECT p.patient_id, e.encounter_datetime AS encounter_date "
            + "         FROM patient p "
            + "                  INNER JOIN encounter e on p.patient_id = e.patient_id INNER JOIN obs o ON o.encounter_id=e.encounter_id "
            + "         WHERE p.voided = 0 AND e.voided = 0 AND o.voided = 0 AND e.location_id =:location AND e.encounter_type = ${6} "
            + "         AND ( ( o.concept_id=${856} AND o.value_numeric < 1000 ) OR (o.concept_id = ${1305} and o.value_coded is not null)) "
            + "             ) juncao "
            + " INNER JOIN( SELECT p.patient_id, MAX(e.encounter_datetime) AS last_consultation_date   "
            + "            FROM  patient p INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "            WHERE  p.voided = 0 AND e.voided = 0 AND e.location_id =:location AND e.encounter_type = ${6} "
            + "            AND e.encounter_datetime BETWEEN :startDate AND :endDate GROUP BY p.patient_id "
            + "            )  "
            + " as last_consultation on last_consultation.patient_id = juncao.patient_id "
            + " WHERE juncao.encounter_date < DATE_SUB(last_consultation.last_consultation_date, INTERVAL "
            + monthsBeforeClinical
            + " MONTH)) most_recent GROUP BY most_recent.patient_id  ) as lastVLResult "
            + " ON lastVLResult.patient_id=p.patient_id "
            + " WHERE "
            + " ( (o.concept_id=${856} AND o.value_numeric is not null) OR (o.concept_id = 1305 and o.value_coded is not null)) AND e.encounter_type=${6}  "
            + " AND e.voided = 0  "
            + " AND o.voided = 0  "
            + " AND e.encounter_datetime BETWEEN DATE_ADD(lastVLResult.encounter_date,INTERVAL "
            + vlMonthsLower
            + " MONTH)  "
            + " AND DATE_ADD(lastVLResult.encounter_date,INTERVAL "
            + vlMonthsUpper
            + " MONTH) AND e.location_id=:location";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    String str = stringSubstitutor.replace(query);
    cd.setQuery(str);
    return cd;
  }

  /**
   * <b>MI15DenNum: Melhoria de Qualidade Category 15 Denominador e Numerator</b><br>
   * <br>
   * <i> DENOMINADOR 1: A and B1 and E and NOT (C or D or F or G or J) and Age >= 2 </i> <br>
   * <i> NUMERATOR 1: A and B1 and E and NOT (C or D or F or G or J) and K and Age >= 2 </i> <br>
   * <br>
   * <i> DENOMINADOR 2: A and J and H and Age >= 2 </i> <i> NUMERATOR 2: A and J and H and L and Age
   * >= 2 </i><br>
   * <br>
   * <i> DENOMINADOR 3: A and J and B2 and NOT P and Age >= 2 </i> <i> NUMERATOR 3: A and J and B2
   * and NOT P and I and Age >= 2 </i><br>
   * <br>
   *
   * @param isDenominator indicator flag
   * @param level indicator number
   * @return CohortDefinition
   */
  public CohortDefinition getCat15P1DenNum(boolean isDenominator, int level) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));
    String name1 = "15.1 - % de pacientes elegíveis a MDS, que foram inscritos em MDS";
    String name2 =
        "15.2 - % de inscritos em MDS que receberam CV acima de 1000 cópias que foram suspensos de MDS";
    String name3 =
        "15.3 - % de pacientes inscritos em MDS em TARV há mais de 21 meses, que conhecem o seu resultado de CV de seguimento";

    CohortDefinition a = getMI15A();
    CohortDefinition b1 = getMI15B1();
    CohortDefinition b2 = getMI15B2(21);
    CohortDefinition c = getMI15C();
    CohortDefinition d = getMI15D();
    CohortDefinition e = getMI15EComplete();
    CohortDefinition f = getMI15F();
    CohortDefinition g = getMI15G();
    CohortDefinition h = getMI15H();
    CohortDefinition i = getMI15I();
    CohortDefinition j = getMI15J();
    CohortDefinition k = getMI15K();
    CohortDefinition l = getMI15L();
    CohortDefinition p = getMI15P();
    CohortDefinition alreadyEnrolledMdc =
        qualityImprovement2020CohortQueries.getPatientsAlreadyEnrolledInTheMdc();

    List<Integer> mdsConcepts =
        Arrays.asList(
            hivMetadata.getGaac().getConceptId(),
            hivMetadata.getQuarterlyDispensation().getConceptId(),
            hivMetadata.getDispensaComunitariaViaApeConcept().getConceptId(),
            hivMetadata.getDescentralizedArvDispensationConcept().getConceptId(),
            hivMetadata.getRapidFlow().getConceptId(),
            hivMetadata.getSemiannualDispensation().getConceptId());

    List<Integer> states = Arrays.asList(hivMetadata.getCompletedConcept().getConceptId());
    List<Integer> start = Arrays.asList(hivMetadata.getStartDrugs().getConceptId());

    CohortDefinition mdcLastClinical =
        qualityImprovement2020CohortQueries
            .getPatientsWhoHadMdsOnMostRecentClinicalAndPickupOnFilaFR36(mdsConcepts, start);

    CohortDefinition recentMdc =
        qualityImprovement2020CohortQueries
            .getPatientsWithMdcOnMostRecentClinicalFormWithFollowingDispensationTypesAndState(
                mdsConcepts, states);

    CohortDefinition pickupAfterClinical =
        qualityImprovement2020CohortQueries
            .getPatientsWhoHadPickupOnFilaAfterMostRecentVlOnFichaClinica();

    CohortDefinition major2 = getAgeOnLastConsultationMoreThan2Years();
    String MAPPINGA =
        "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}";
    String MAPPINGC =
        "startDate=${revisionEndDate-11m+1d},endDate=${revisionEndDate-2m},location=${location}";
    String MAPPINGD =
        "startDate=${revisionEndDate-20m+1d},endDate=${revisionEndDate-2m},location=${location}";

    cd.addSearch("A", EptsReportUtils.map(a, MAPPINGA));
    cd.addSearch("B1", EptsReportUtils.map(b1, MAPPINGA));
    cd.addSearch("B2", EptsReportUtils.map(b2, MAPPINGA));
    cd.addSearch("C", EptsReportUtils.map(c, MAPPINGC));
    cd.addSearch("D", EptsReportUtils.map(d, MAPPINGD));
    cd.addSearch("E", EptsReportUtils.map(e, MAPPINGA));
    cd.addSearch("F", EptsReportUtils.map(f, MAPPINGA));
    cd.addSearch("G", EptsReportUtils.map(g, "endDate=${revisionEndDate-1m},location=${location}"));
    cd.addSearch("H", EptsReportUtils.map(h, MAPPINGA));
    cd.addSearch("I", EptsReportUtils.map(i, MAPPINGA));
    cd.addSearch("J", EptsReportUtils.map(j, MAPPINGA));
    cd.addSearch("K", EptsReportUtils.map(k, MAPPINGA));
    cd.addSearch("MDC", EptsReportUtils.map(alreadyEnrolledMdc, MAPPINGA));
    cd.addSearch("L", EptsReportUtils.map(l, MAPPINGA));
    cd.addSearch("P", EptsReportUtils.map(p, MAPPINGA));
    cd.addSearch("AGE2", EptsReportUtils.map(major2, MAPPINGA));
    cd.addSearch("LMDC", EptsReportUtils.map(mdcLastClinical, MAPPINGA));
    cd.addSearch("RMDC", EptsReportUtils.map(recentMdc, MAPPINGA));
    cd.addSearch(
        "PICKUP",
        EptsReportUtils.map(
            pickupAfterClinical,
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},revisionEndDate=${revisionEndDate},location=${location}"));

    if (isDenominator) {

      if (level == 1) {
        cd.setName("Denominator: " + name1);
        cd.setCompositionString("A AND B1 AND E AND NOT (C OR D OR F OR G OR MDC) AND AGE2 ");
      }
      if (level == 2) {
        cd.setName("Denominator: " + name2);
        cd.setCompositionString("A AND MDC AND H AND AGE2");
      }
      if (level == 3) {
        cd.setName("Denominator: " + name3);
        cd.setCompositionString("A AND MDC AND B2 AND NOT P AND AGE2");
      }
      return cd;
    }

    if (level == 1) {
      cd.setName("Numerator: " + name1);
      cd.setCompositionString("A AND B1 AND E AND NOT (C OR D OR F OR G OR J) AND LMDC AND AGE2 ");
    }
    if (level == 2) {
      cd.setName("Numerator: " + name2);
      cd.setCompositionString("A AND RMDC AND PICKUP AND H AND L AND AGE2");
    }
    if (level == 3) {
      cd.setName("Numerator: " + name3);
      cd.setCompositionString("A AND J AND B2 AND NOT P AND I AND AGE2");
    }
    return cd;
  }

  /**
   * Age should be calculated on “Last Consultation Date” (Check A for the algorithm to define this
   * date).
   *
   * @return CohortDefinition
   */
  public CohortDefinition getAgeOnLastConsultationMoreThan2Years() {
    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Get age  on last Consultation ");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());

    String sql =
        "SELECT p.person_id "
            + "FROM   person p "
            + "    INNER JOIN (SELECT p.patient_id, MAX(e.encounter_datetime) AS encounter_datetime "
            + "                FROM  patient p "
            + "                    INNER JOIN encounter e on p.patient_id = e.patient_id "
            + "                WHERE p.voided =0 "
            + "                  AND  e.voided = 0 "
            + "                  AND e.encounter_type = ${6} "
            + "                  AND e.location_id = :location "
            + "                  AND e.encounter_datetime BETWEEN  :startDate AND :endDate "
            + "                GROUP BY p.patient_id) "
            + "        AS last_clinical ON last_clinical.patient_id = p.person_id "
            + "WHERE p.voided = 0 "
            + "    AND  TIMESTAMPDIFF(YEAR,p.birthdate,last_clinical.encounter_datetime) >= 2 ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    String str = stringSubstitutor.replace(sql);
    cd.setQuery(str);
    return cd;
  }

  /**
   * P- Select all patients with concept “PEDIDO DE INVESTIGACOES LABORATORIAIS” (Concept Id 23722)
   * and value coded “HIV CARGA VIRAL” (Concept Id 856) registered in Ficha Clinica (encounter type
   * 6) during the last 3 months from the “Last Consultation Date” (encounter_datetime from A), i.e,
   * at least one “Pedido de Carga Viral” encounter_datetime >= “Last Consultation Date”-3months and
   * < “Last Consultation Date”.
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMI15P() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patients with Pedido de Carga Viral");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    cd.setName("All patients with concept “PEDIDO DE INVESTIGACOES LABORATORIAIS”");
    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());

    String query =
        " SELECT p.patient_id FROM patient p INNER JOIN encounter e "
            + " on p.patient_id = e.patient_id INNER JOIN obs o ON o.encounter_id=e.encounter_id "
            + " INNER JOIN (SELECT pp.patient_id, MAX(ee.encounter_datetime) AS last_consultation_date"
            + " FROM patient pp INNER JOIN encounter ee ON ee.patient_id = pp.patient_id"
            + " WHERE pp.voided = 0 AND ee.voided = 0 AND ee.location_id = :location AND ee.encounter_type = ${6}"
            + " AND ee.encounter_datetime BETWEEN :startDate AND :endDate"
            + " GROUP BY pp.patient_id) last_consultation ON p.patient_id = last_consultation.patient_id"
            + " WHERE p.voided = 0 AND e.voided = 0 AND o.voided = 0 AND e.location_id = :location AND e.encounter_type = ${6}"
            + " AND o.concept_id = ${23722} AND o.value_coded = ${856} "
            + " AND e.encounter_datetime >= DATE_SUB(last_consultation.last_consultation_date, INTERVAL 3 MONTH)"
            + " AND e.encounter_datetime < last_consultation.last_consultation_date"
            + " GROUP BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    String str = stringSubstitutor.replace(query);
    cd.setQuery(str);
    return cd;
  }
  /**
   * Utentes que têm o registo do “Pedido de Investigações Laboratoriais” igual a “Carga Viral”, na
   * Ficha Clínica nos últimos 12 meses da última consulta clínica (“Data Pedido CV”>= “Data Última
   * Consulta” menos (-) 12meses e < “Data Última Consulta”).
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoHadLabInvestigationsRequest() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("Patients with Pedido de Carga Viral Before Last Visit");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    cd.setName("All patients with concept PEDIDO DE INVESTIGACOES LABORATORIAIS BEFORE LAST VISIT");
    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("856", hivMetadata.getHivViralLoadConcept().getConceptId());
    map.put("23722", hivMetadata.getApplicationForLaboratoryResearch().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o ON o.encounter_id = e.encounter_id "
            + "       INNER JOIN (SELECT p.patient_id, MAX(e.encounter_datetime) visit_date "
            + "                   FROM   patient p "
            + "                          INNER JOIN encounter e ON e.patient_id = p.patient_id "
            + "                   WHERE  e.encounter_type = ${6} "
            + "                          AND location_id = :location "
            + "                          AND e.encounter_datetime BETWEEN :startDate AND :endDate "
            + "                          AND p.voided = 0 "
            + "                          AND e.voided = 0 "
            + "                   GROUP  BY p.patient_id) last_visit "
            + "               ON last_visit.patient_id = p.patient_id "
            + "WHERE  e.encounter_type = ${6} "
            + "       AND e.encounter_datetime >= DATE_SUB(last_visit.visit_date, INTERVAL 12 MONTH) "
            + "       AND e.encounter_datetime < last_visit.visit_date "
            + "       AND e.location_id = :location "
            + "       AND o.concept_id = ${23722} "
            + "       AND o.value_coded = ${856} "
            + "       AND p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND o.voided = 0 "
            + "GROUP  BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);
    String str = stringSubstitutor.replace(query);
    cd.setQuery(str);
    return cd;
  }

  /**
   * Get CAT 13.1 Denominator
   *
   * @return CohortDefinition
   */
  public CohortDefinition getMI13DEN1() {

    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();
    compositionCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    compositionCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    compositionCohortDefinition.addParameter(new Parameter("location", "location", Location.class));
    compositionCohortDefinition.setName("MI CAT 13.1 Denominator");

    CohortDefinition lastClinical = commonCohortQueries.getMOHPatientsLastClinicalConsultation();

    CohortDefinition pregnant =
        commonCohortQueries.getNewMQPregnantORBreastfeeding(
            hivMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition brestfeeding =
        commonCohortQueries.getNewMQPregnantORBreastfeeding(
            hivMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition PrimeiraLinha =
        qualityImprovement2020CohortQueries.getUtentesPrimeiraLinha(
            QualityImprovement2020CohortQueries.UtentesPrimeiraLinhaPreposition.MI);

    CohortDefinition tbDiagnosisActive =
        qualityImprovement2020CohortQueries.getPatientsWithTbActiveOrTbTreatment();

    compositionCohortDefinition.addSearch(
        "age",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPatientsAgeOnLastClinicalConsultationDate(15, null),
            "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "B1",
        EptsReportUtils.map(
            lastClinical, "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "C",
        EptsReportUtils.map(
            pregnant, "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "D",
        EptsReportUtils.map(
            brestfeeding, "startDate=${startDate},endDate=${endDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "PrimeiraLinha",
        EptsReportUtils.map(
            PrimeiraLinha,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.addSearch(
        "tbDiagnosisActive",
        EptsReportUtils.map(
            tbDiagnosisActive,
            "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    compositionCohortDefinition.setCompositionString(
        "(((B1 AND age) OR D) AND PrimeiraLinha) AND NOT (C OR tbDiagnosisActive)");

    return compositionCohortDefinition;
  }

  public CohortDefinition getMICAT14(
      QualityImprovement2020CohortQueries.MQCat14Preposition preposition, String level) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("MI CAT14 1-8 Numerator and Denominator");
    cd.addParameter(new Parameter("location", "location", Location.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));

    cd.addSearch(
        "DEN",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQ14(preposition),
            "startDate=${revisionEndDate-12m},endDate=${revisionEndDate},location=${location}"));
    cd.addSearch(
        "NUM",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQ14NUM(preposition),
            "startDate=${revisionEndDate-12m},endDate=${revisionEndDate},location=${location}"));

    if (level.equals("DEN")) {
      cd.setCompositionString("DEN");
    } else if (level.equals("NUM")) {
      cd.setCompositionString("NUM");
    }
    return cd;
  }

  /**
   * <b> RF7.2 EXCLUSION PATIENTS WHO ABANDONED DURING ART START DATE PERIOD</b>
   *
   * <p>O sistema irá identificar utentes que abandonaram o tratamento TARV durante o período da
   * seguinte forma:
   *
   * <p>incluindo os utentes com Último registo de “Mudança de Estado de Permanência” = “Abandono”
   * na Ficha Clínica durante o período (“Data Consulta”>=”Data Início Período” e “Data
   * Consulta”<=”Data Fim Período”
   *
   * <p>incluindo os utentes com Último registo de “Mudança de Estado de Permanência” = “Abandono”
   * na Ficha Resumo durante o período (“Data de Mudança de Estado Permanência”>=”Data Início
   * Período” e “Data Consulta”<=”Data Fim Período”
   * <li>1. para exclusão nos utentes que iniciaram a 1ª linha de TARV, a “Data Início Período” será
   *     igual a “Data Início TARV” e “Data Fim do Período” será igual a “Data Início TARV”+6meses.
   *
   * @return CohortDefinition
   */
  public CohortDefinition getPatientsWhoAbandonedTarvOnArtStartDate() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("All patients who abandoned TARV On Art Start Date");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    cd.setQuery(
        IntensiveMonitoringQueries.getMI13AbandonedTarvOnArtStartDate(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getStateOfStayOfArtPatient().getConceptId(),
            hivMetadata.getAbandonedConcept().getConceptId(),
            hivMetadata.getStateOfStayOfPreArtPatient().getConceptId(),
            hivMetadata.getARVStartDateConcept().getConceptId()));

    return cd;
  }

  /**
   * <b> RF7.2 EXCLUSION PATIENTS WHO ABANDONED DURING FIRST PREGNANCY STATE DATE PERIOD</b>
   *
   * <p>O sistema irá identificar utentes que abandonaram o tratamento TARV durante o período da
   * seguinte forma:
   *
   * <p>incluindo os utentes com Último registo de “Mudança de Estado de Permanência” = “Abandono”
   * na Ficha Clínica durante o período (“Data Consulta”>=”Data Início Período” e “Data
   * Consulta”<=”Data Fim Período”
   *
   * <p>incluindo os utentes com Último registo de “Mudança de Estado de Permanência” = “Abandono”
   * na Ficha Resumo durante o período (“Data de Mudança de Estado Permanência”>=”Data Início
   * Período” e “Data Consulta”<=”Data Fim Período”
   * <li>1. para exclusão nas mulheres grávidas que iniciaram TARV a “Data Início Período” será
   *     igual a “Data 1ª Consulta Grávida” – 3 meses” e “Data Fim do Período” será igual a e “Data
   *     1ª Consulta Grávida”).
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoAbandonedTarvOnFirstPregnancyStateDate() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("All patients who abandoned TARV On First Pregnancy State Date");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    cd.setQuery(
        IntensiveMonitoringQueries.getMI13AbandonedTarvOnFirstPregnancyStateDate(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            hivMetadata.getStateOfStayOfArtPatient().getConceptId(),
            hivMetadata.getAbandonedConcept().getConceptId(),
            hivMetadata.getStateOfStayOfPreArtPatient().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getPregnantConcept().getConceptId()));

    return cd;
  }

  /**
   * 13.16. % de MG elegíveis a CV com registo de pedido de CV feito pelo clínico na primeira CPN
   * (MG que entraram em TARV na CPN) Denominator:# de MG que tiveram a primeira CPN no período de
   * inclusão, e que já estavam em TARV há mais de 3 meses (Line 91,Column F in the Template) as
   * following: B2
   *
   * <p>Excepto as utentes abandono em TARV durante o período (seguindo os critérios definidos no
   * RF7.2) nos últimos 3 meses (entre “Data 1ª Consulta Grávida” – 3 meses e “Data 1ª Consulta
   * Grávida”).
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getMQC13P2DenMGInIncluisionPeriod33Month() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "StartDate", Date.class));
    cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition transferredIn =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    cd.addSearch(
        "B2",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMQC13P2DenB2(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "ABANDONED",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries
                .getPatientsWhoAbandonedOrRestartedTarvOnLast3MonthsArt(),
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch("TRANSFERREDIN", EptsReportUtils.map(transferredIn, "location=${location}"));

    cd.setCompositionString("B2 AND NOT (ABANDONED OR TRANSFERREDIN)");

    return cd;
  }

  /**
   * 13.16. % de MG elegíveis a CV com registo de pedido de CV feito pelo clínico na primeira CPN
   * (MG que entraram em TARV na CPN) (Line 91 in the template) Numerator (Column E in the Template)
   * as following: (B2 and J)
   *
   * <p>Excepto as utentes abandono em TARV durante o período (seguindo os critérios definidos no
   * RF7.2) nos últimos 3 meses (entre “Data 1ª Consulta Grávida” – 3 meses e “Data 1ª Consulta
   * Grávida”).
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getMQC13P2Num2() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "StartDate", Date.class));
    cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "DENOMINATOR",
        EptsReportUtils.map(
            getMQC13P2DenMGInIncluisionPeriod33Month(),
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "REQUEST",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getgetMQC13P2DenB4(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("DENOMINATOR AND REQUEST");

    return cd;
  }

  /**
   * <b> O sistema irá produzir o seguinte denominador do Indicador 13.17 da Categoria 13 MG de
   * Resultado de CV:</b>
   *
   * <p>filtrando as que tiveram um registo de resultado de CV (“Data Resultado CV”) numa consulta
   * clínica durante o mês de avaliação
   * <li>Nota: Se existir o registo de mais do que uma consulta clínica com registo de resultado de
   *     CV durante o mês de avaliação deve ser considerada a primeira consulta clínica com o
   *     registo de resultado de CV durante o mês de avaliação.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithViralLoadResultDuringTheAvaluationMonth() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("All patients With Viral Load Result During The Avaluation Month ");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    cd.setQuery(
        IntensiveMonitoringQueries.getViralLoadResultQuery(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getHivViralLoadConcept().getConceptId(),
            hivMetadata.getHivViralLoadQualitative().getConceptId()));

    return cd;
  }

  /**
   * <b>13.17 DENOMINATOR - O sistema irá produzir o seguinte denominador do Indicador 13.17 da
   * Categoria 13 MG de Resultado de CV:</b>
   *
   * <p>incluindo todos os utentes do sexo feminino que tiveram pelo menos uma consulta clínica
   * (Ficha Clínica) durante o mês de avaliação com registo de grávida, e
   *
   * <p>filtrando as que tiveram um registo de resultado de CV (“Data Resultado CV”) numa consulta
   * clínica durante o mês de avaliação
   * <li>Nota: Se existir o registo de mais do que uma consulta clínica com registo de resultado de
   *     CV durante o mês de avaliação deve ser considerada a primeira consulta clínica com o
   *     registo de resultado de CV durante o mês de avaliação.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getMIC13Den17() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "StartDate", Date.class));
    cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "PREGNANT",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getPregnantAndBreastfeedingStates(
                hivMetadata.getAdultoSeguimentoEncounterType(),
                hivMetadata.getPregnantConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "VL",
        EptsReportUtils.map(
            getPatientsWithViralLoadResultDuringTheAvaluationMonth(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("PREGNANT AND VL");

    return cd;
  }

  /**
   * <b> O sistema irá produzir o seguinte Numerador do Indicador 13.17 da Categoria 13 MG de
   * Resultado de CV:</b> Filtrando os utentes com o registo de pedido de CV (“Pedido de
   * Investigações Laboratoriais”) na Ficha Clínica imediatamente anterior ao registo do resultado
   * de CV durante o período de avaliação (< “Data Resultado CV”) e sendo este pedido efectuado em
   * 33 dias, ou seja, “Data Resultado CV” menos “Pedido CV Anterior” <= 33 dias)
   *
   * <p>select all patients with S.TARV: ADULTO SEGUIMENTO (ID=6) that have Pedido de Investigações
   * Laboratoriais (Concept ID = 23722) Data de Consulta (encounter.encounter_datetime) and
   * Value_coded = “Carga Viral” (concept id 856) for concept Id 23722 (Pedido de Investigações
   * Laboratoriais) and Max (Encounter_datetime ) as ”Pedido CV Anterior” < “Data Resultado CV” And
   * “Data Resultado CV” menos “Pedido CV Anterior” <= 33 dias
   * <li>Nota: “Data Resultado CV” encontra-se definido no Denominador (RF32- Categoria 13 MG
   *     Indicador 13.17 – Denominador Resultado CV)
   *
   * @see #getPatientsWithViralLoadResultDuringTheAvaluationMonth()
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithPreviousViralLoadResultIn33DaysBeforeVLResult() {

    SqlCohortDefinition cd = new SqlCohortDefinition();
    cd.setName("All patients With Previous Viral Load Result In 33 Days Before Viral Load Result ");
    cd.addParameter(new Parameter("startDate", "startDate", Date.class));
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    cd.setQuery(
        IntensiveMonitoringQueries.getPreviousViralLoadQuery(
            hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId(),
            hivMetadata.getHivViralLoadConcept().getConceptId(),
            hivMetadata.getHivViralLoadQualitative().getConceptId(),
            hivMetadata.getApplicationForLaboratoryResearch().getConceptId()));

    return cd;
  }

  /**
   * <b>13.17 NUMERATOR - O sistema irá produzir o seguinte Numerador do Indicador 13.17 da
   * Categoria 13 MG de Resultado de CV:</b>
   *
   * <p>Incluindo todos os utentes seleccionados no Indicador 13.17 Denominador definido no RF32
   * (Categoria 13 MG Indicador 13.17 – Denominador Resultado CV) e
   *
   * <p>Filtrando os utentes com o registo de pedido de CV (“Pedido de Investigações Laboratoriais”)
   * na Ficha Clínica imediatamente anterior ao registo do resultado de CV durante o período de
   * avaliação (< “Data Resultado CV”) e sendo este pedido efectuado em 33 dias, ou seja, “Data
   * Resultado CV” menos “Pedido CV Anterior” <= 33 dias).
   * <li>Nota: “Data Resultado CV” encontra-se definido no Denominador (RF32- Categoria 13 MG
   *     Indicador 13.17 – Denominador Resultado CV)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getMIC13Num17() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.addParameter(new Parameter("startDate", "StartDate", Date.class));
    cd.addParameter(new Parameter("endDate", "EndDate", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch("DENOMINATOR", EptsReportUtils.map(getMIC13Den17(), MAPPING));

    cd.addSearch(
        "PREVIOUSVL",
        EptsReportUtils.map(
            getPatientsWithPreviousViralLoadResultIn33DaysBeforeVLResult(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("DENOMINATOR AND PREVIOUSVL");

    return cd;
  }

  /**
   * <b>MQ9Den: M&I Report - Categoria 9 Denominador 1 to 4</b><br>
   *
   * <ul>
   *   <li>9.1. % de adultos HIV+ em TARV que tiveram conhecimento do resultado do primeiro CD4
   *       dentro de 33 dias após a inscrição
   *   <li>9.2. % de crianças HIV+ em TARV que tiveram conhecimento do resultado do primeiro CD4
   *       dentro de 33 dias após a inscrição
   * </ul>
   *
   * @param flag indicator number
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getMI9Den(int flag) {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    switch (flag) {
      case 1:
        cd.setName(
            "9.1 % de adultos (15/+anos) com pedido de CD4 na primeira consulta clínica depois do diagnóstico de HIV+");
        break;
      case 2:
        cd.setName(
            "9.2 % de adultos  (15/+anos) que receberam o resultado do primeiro CD4 dentro de 33 dias  após a primeira consulta clínica.");
        break;
      case 3:
        cd.setName(
            "9.3 % de adultos (15/+anos) com pedido de CD4 na consulta clínica de reinício do TARV");
        break;
      case 4:
        cd.setName(
            "9.4 % de adultos (15/+anos) que receberam o resultado do CD4 dentro de 33 dias após consulta clínica de reinício do TARV");
        break;
      case 5:
        cd.setName(
            "9.5 % de crianças  (0-14 anos) com pedido de CD4 na primeira consulta clínica depois do diagnóstico de HIV+");
        break;
      case 6:
        cd.setName(
            "9.6 % de crianças  (0-14 anos) HIV+ que receberam o resultado do primeiro CD4 dentro de 33 dias  após a primeira consulta clínica");
        break;
      case 7:
        cd.setName(
            "9.7 % de crianças (0-14 anos) com pedido de CD4 na consulta clínica de reinício do TARV");
        break;
      case 8:
        cd.setName(
            "9.8 % de crianças (0-14 anos) que receberam o resultado do CD4 dentro de 33 dias após consulta clínica de reinício do TARV");
        break;
    }

    if (flag == 1 || flag == 2) {
      cd.addSearch(
          "AGE",
          EptsReportUtils.map(
              genericCohortQueries.getAgeOnFirstClinicalConsultation(15, null),
              "onOrAfter=${revisionEndDate-3m+1d},onOrBefore=${revisionEndDate-2m},revisionEndDate=${revisionEndDate},location=${location}"));
    } else if (flag == 3 || flag == 4) {
      cd.addSearch(
          "AGE",
          EptsReportUtils.map(
              genericCohortQueries.getAgeOnRestartedStateOfStayConsultation(15, null),
              "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},location=${location}"));
    } else if (flag == 5 || flag == 6) {
      cd.addSearch(
          "AGE",
          EptsReportUtils.map(
              genericCohortQueries.getAgeOnFirstClinicalConsultation(0, 14),
              "onOrAfter=${revisionEndDate-3m+1d},onOrBefore=${revisionEndDate-2m},revisionEndDate=${revisionEndDate},location=${location}"));
    } else if (flag == 7 || flag == 8) {
      cd.addSearch(
          "AGE",
          EptsReportUtils.map(
              genericCohortQueries.getAgeOnRestartedStateOfStayConsultation(0, 14),
              "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},location=${location}"));
    }

    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    String inclusionRequestPeriodMappings =
        "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},location=${location}";

    String inclusionResultPeriodMappings =
        "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},location=${location}";

    String cd4RequestMappings =
        "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},revisionEndDate={revisionEndDate},location=${location}";

    String cd4ResultMappings =
        "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},revisionEndDate={revisionEndDate},location=${location}";

    cd.addSearch(
        "A",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getFirstClinicalConsultationDuringInclusionPeriod(),
            cd4RequestMappings));

    cd.addSearch(
        "C",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPregnantORBreastfeeding(
                commonMetadata.getPregnantConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            inclusionRequestPeriodMappings));

    cd.addSearch(
        "D",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPregnantORBreastfeeding(
                commonMetadata.getBreastfeeding().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            inclusionRequestPeriodMappings));

    cd.addSearch(
        "E",
        EptsReportUtils.map(
            QualityImprovement2020Queries.getTransferredInPatients(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
                hivMetadata.getPatientFoundYesConcept().getConceptId(),
                hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
                hivMetadata.getArtStatus().getConceptId()),
            inclusionRequestPeriodMappings));

    cd.addSearch(
        "pregnantOnPeriod",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMOHPregnantORBreastfeedingOnClinicalConsultation(
                commonMetadata.getPregnantConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            cd4RequestMappings));

    cd.addSearch(
        "breastfeedingOnPeriod",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMOHPregnantORBreastfeedingOnClinicalConsultation(
                commonMetadata.getBreastfeeding().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            cd4RequestMappings));

    cd.addSearch(
        "AA",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getFirstClinicalConsultationDuringInclusionPeriod(),
            cd4ResultMappings));

    cd.addSearch(
        "CC",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPregnantORBreastfeeding(
                commonMetadata.getPregnantConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            inclusionResultPeriodMappings));

    cd.addSearch(
        "DD",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPregnantORBreastfeeding(
                commonMetadata.getBreastfeeding().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            inclusionResultPeriodMappings));

    cd.addSearch(
        "transferredIn",
        EptsReportUtils.map(
            QualityImprovement2020Queries.getTransferredInPatients(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
                hivMetadata.getPatientFoundYesConcept().getConceptId(),
                hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
                hivMetadata.getArtStatus().getConceptId()),
            inclusionResultPeriodMappings));

    cd.addSearch(
        "pregnantOnPeriodCd4Resul",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMOHPregnantORBreastfeedingOnClinicalConsultation(
                commonMetadata.getPregnantConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            cd4ResultMappings));

    cd.addSearch(
        "breastfeedingOnPeriodCd4Result",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getMOHPregnantORBreastfeedingOnClinicalConsultation(
                commonMetadata.getBreastfeeding().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            cd4ResultMappings));

    cd.addSearch(
        "RESTARTED",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getPatientsWithRestartedStateOfStay(),
            "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},location=${location}"));

    cd.addSearch(
        "RESULTS",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getCd4ResultAfterRestartDate(),
            "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},location=${location}"));

    cd.addSearch(
        "RESTARTED33DAYSBEFORE",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getPatientsRestartedWithLessThan33Days(),
            "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},location=${location}"));

    if (flag == 1) {
      cd.setCompositionString(
          "A AND (AGE OR D OR breastfeedingOnPeriod) AND NOT (E OR C OR pregnantOnPeriod)");
    } else if (flag == 2) {
      cd.setCompositionString(
          "AA AND (AGE OR DD OR breastfeedingOnPeriodCd4Result) AND NOT (transferredIn OR CC OR pregnantOnPeriodCd4Resul)");
    } else if (flag == 3 || flag == 7) {
      cd.setCompositionString("(RESTARTED AND AGE) AND NOT transferredIn");
    } else if (flag == 4 || flag == 8) {
      cd.setCompositionString("(RESTARTED AND RESULTS AND AGE) AND NOT RESTARTED33DAYSBEFORE");
    } else if (flag == 5) {
      cd.setCompositionString(
          "A AND AGE AND NOT (C OR D OR E OR pregnantOnPeriod OR breastfeedingOnPeriod)");
    } else if (flag == 6) {
      cd.setCompositionString(
          "(AA AND AGE) AND NOT (DD OR breastfeedingOnPeriodCd4Result OR transferredIn OR CC OR pregnantOnPeriodCd4Resul)");
    }
    return cd;
  }

  /**
   * <b>Categoria 9 Denominador - Pedido e Resultado de CD4 - MG</b>
   * <li>Pedido de CD4 = “% de MG HIV+ que teve registo de pedido do primeiro CD4 na data da
   *     primeira consulta clínica/abertura da Ficha Mestra”
   * <li>Resultado de CD4 = “% de MG HIV+ que teve conhecimento do resultado do primeiro CD4 dentro
   *     de 33 dias após a data da primeira CPN (primeira consulta com registo de Gravidez”
   *
   * @param flag parameter to receive the indicator numbe
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getCd4RequestAndResultForPregnantsCat9Den(int flag) {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    String inclusionPeriodMappings =
        "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},location=${location}";

    if (flag == 9) {
      cd.setName("9.9 % de MG  HIV+ com registo de pedido de CD4 na primeira CPN.");
      inclusionPeriodMappings =
          "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},location=${location}";
    } else if (flag == 10) {
      cd.setName(
          "9.10 % de MG  HIV+ que receberam o resultado do primeiro CD4 dentro de 33 dias  após a primeira CPN.");
      inclusionPeriodMappings =
          "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},location=${location}";
    }

    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "pregnantOnPeriod",
        EptsReportUtils.map(
            getFirstPregnancyORBreastfeedingOnClinicalConsultation(
                commonMetadata.getPregnantConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            inclusionPeriodMappings));

    cd.addSearch(
        "transferredIn",
        EptsReportUtils.map(
            QualityImprovement2020Queries.getTransferredInPatients(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
                hivMetadata.getPatientFoundYesConcept().getConceptId(),
                hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
                hivMetadata.getArtStatus().getConceptId()),
            "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},location=${location}"));

    cd.setCompositionString("pregnantOnPeriod AND NOT transferredIn");

    return cd;
  }
  /**
   * <b>Description:</b> IM-MOH Query For pregnant or Breastfeeding patients
   *
   * <p><b>Technical Specs</b>
   * <li>considerar a data da primeira consulta clínica com registo de “Grávida=Sim” durante o mês
   *     da coorte de avaliação
   *
   *     <p>Excluir todos os utentes registados como “Gravida=Sim” numa consulta clínica decorrida
   *     nos últimos 3 meses antes da primeira consulta com registo de gravidez decorrida no mês da
   *     coorte de avaliação, ou seja, entre “Data Primeira CPN” menos (-) 3 meses e >= “Data
   *     Primeira CPN” menos (-) 1 dia). Nota: esta exclusão é para garantir que o primeiro registo
   *     de gravidez durante o mês da coorte de avaliação representa o início de uma gravidez
   *
   *     <p>Nota: (1) Mês da Coorte de Avaliação é correspondente ao período entre: “Data Recolha de
   *     Dados” menos (-) 2 ou 3 meses mais (+) 1 dia e “Data Recolha de Dados” menos (-) 1 mês.
   *
   * @param question The question Concept Id
   * @param answer The value coded Concept Id
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getFirstPregnancyORBreastfeedingOnClinicalConsultation(
      int question, int answer) {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Mulheres com registo de primeira gravidez no período de inclusão");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "startDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("question", question);
    map.put("answer", answer);

    String query =
        new EptsQueriesUtil().patientIdQueryBuilder(getPregnantOrBreastfeedingQuery()).getQuery();

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  protected static String getPregnantOrBreastfeedingQuery() {
    return " SELECT   pregnant.person_id, pregnant.first_pregnancy AS first_consultation "
        + "FROM     ( "
        + "                  SELECT   p.person_id, "
        + "                           Min(e.encounter_datetime) AS first_pregnancy "
        + "                  FROM     person p "
        + "                  JOIN     encounter e "
        + "                  ON       e.patient_id = p.person_id "
        + "                  JOIN     obs o "
        + "                  ON       o.encounter_id = e.encounter_id "
        + "                  AND      encounter_type = ${6} "
        + "                  AND      o.concept_id = ${question} "
        + "                  AND      o.value_coded = ${answer} "
        + "                  AND      e.location_id = :location "
        + "                  AND      e.encounter_datetime >= :startDate "
        + "                  AND      e.encounter_datetime <= :endDate "
        + "                  AND      p.gender = 'F' "
        + "                  AND      e.voided = 0 "
        + "                  AND      o.voided = 0 "
        + "                  AND      p.voided = 0 "
        + "                  GROUP BY p.person_id) pregnant "
        + "WHERE   pregnant.person_id NOT IN "
        + "         ( "
        + "                SELECT p.person_id "
        + "                FROM   person p "
        + "                JOIN   encounter e "
        + "                ON     e.patient_id = p.person_id "
        + "                JOIN   obs o "
        + "                ON     o.encounter_id = e.encounter_id "
        + "                WHERE    encounter_type = ${6} "
        + "                AND    o.concept_id = ${question} "
        + "                AND    o.value_coded = ${answer} "
        + "                AND    e.location_id = :location "
        + "                AND    e.encounter_datetime >= date_sub(pregnant.first_pregnancy, interval 3 month ) "
        + "                AND    e.encounter_datetime < pregnant.first_pregnancy "
        + "                AND    p.gender = 'F' "
        + "                AND    e.voided = 0 "
        + "                AND    o.voided = 0 "
        + "                AND    p.voided = 0 ) "
        + "GROUP BY pregnant.person_id";
  }

  /**
   * <b>MQ9Num: M&Q Report - Categoria 9 Numerador - Pedido de CD4 Adulto</b><br>
   *
   * <ul>
   *   <li>9.1. % de adultos HIV+ ≥ 15 anos que teve registo de pedido do primeiro CD4 na data da
   *       primeira consulta clínica/abertura da Ficha Mestra”
   *   <li>9.2. % de crianças HIV+ em TARV que tiveram conhecimento do resultado do primeiro CD4
   *       dentro de 33 dias após a inscrição
   * </ul>
   *
   * @param flag indicator number
   * @return CohortDefinition
   */
  public CohortDefinition getMI9Num(int flag) {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    switch (flag) {
      case 1:
        cd.setName(
            "9.1 % de adultos  (15/+anos) com pedido de CD4 na primeira consulta clínica depois do diagnóstico de HIV+");
        break;
      case 2:
        cd.setName(
            "9.2 % de adultos  (15/+anos) HIV+ que receberam o resultado do primeiro CD4 dentro de 33 dias  após a primeira consulta clínica");
        break;
      case 3:
        cd.setName(
            "9.3 % de adultos (15/+anos) com pedido de CD4 na consulta clínica de reinício do TARV");
        break;
      case 4:
        cd.setName(
            "9.4 % de adultos (15/+anos) que receberam o resultado do CD4 dentro de 33 dias após consulta clínica de reinício do TARV");
        break;
      case 5:
        cd.setName(
            "9.5 % de crianças  (0-14 anos) com pedido de CD4 na primeira consulta clínica depois do diagnóstico de HIV+");
        break;
      case 6:
        cd.setName(
            "9.6 % de crianças  (0-14 anos) HIV+ que receberam o resultado do primeiro CD4 dentro de 33 dias  após a primeira consulta clínica");
        break;
      case 7:
        cd.setName(
            "9.7 % de crianças (0-14 anos) com pedido de CD4 na consulta clínica de reinício do TARV");
        break;
      case 8:
        cd.setName(
            "9.8 % de adultos HIV+ ≥ 15 anos reinícios TARV que teve conhecimento do resultado do CD4 dentro de 33 dias após a data da consulta clínica de reinício TARV");
        break;
    }

    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "requestCd4",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries
                .getRequestForCd4OnFirstClinicalConsultationDuringInclusionPeriod(),
            "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "resultCd4",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries
                .getCd4ResultAfterFirstConsultationOnInclusionPeriod(),
            "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "REQUESTONRESTART",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getPatientsWithCd4RequestsOnRestartedTarvDate(),
            "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},location=${location}"));

    cd.addSearch(
        "RESULTSONRESTART",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getPatientsWithCd4ResultsOnRestartedTarvDate(),
            "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},location=${location}"));

    cd.addSearch(
        "denominatorOne",
        EptsReportUtils.map(
            getMI9Den(1), "revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "denominatorTwo",
        EptsReportUtils.map(
            getMI9Den(2), "revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "denominatorThree",
        EptsReportUtils.map(
            getMI9Den(3), "revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "denominatorFour",
        EptsReportUtils.map(
            getMI9Den(4), "revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "denominatorFive",
        EptsReportUtils.map(
            getMI9Den(5), "revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "denominatorSix",
        EptsReportUtils.map(
            getMI9Den(6), "revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "denominatorSeven",
        EptsReportUtils.map(
            getMI9Den(7), "revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "denominatorEight",
        EptsReportUtils.map(
            getMI9Den(8), "revisionEndDate=${revisionEndDate},location=${location}"));

    if (flag == 1) {
      cd.setCompositionString("denominatorOne AND requestCd4");
    } else if (flag == 2) {
      cd.setCompositionString("denominatorTwo AND resultCd4");
    } else if (flag == 3) {
      cd.setCompositionString("denominatorThree AND REQUESTONRESTART");
    } else if (flag == 4) {
      cd.setCompositionString("denominatorFour AND RESULTSONRESTART");
    } else if (flag == 5) {
      cd.setCompositionString("denominatorFive AND requestCd4");
    } else if (flag == 6) {
      cd.setCompositionString("denominatorSix AND resultCd4");
    } else if (flag == 7) {
      cd.setCompositionString("denominatorSeven AND REQUESTONRESTART");
    } else if (flag == 8) {
      cd.setCompositionString("denominatorEight AND REQUESTONRESTART");
    }

    return cd;
  }

  /**
   * O sistema irá produzir o Numerador para o indicador do pedido de CD4 para MG: “# de MG HIV+ em
   * TARV com registo de pedido de CD4 na primeira CPN (Primeira consulta com registo Gravidez)”
   *
   * @param flag parameter to receive the indicator number
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getCd4RequestAndResultForPregnantsCat9Num(int flag) {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    String inclusionPeriodMappings = "";

    if (flag == 9) {
      cd.setName("9.9 % de MG  HIV+ com registo de pedido de CD4 na primeira CPN.");
      inclusionPeriodMappings =
          "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},location=${location}";
    } else if (flag == 10) {
      cd.setName(
          "9.10 % de MG  HIV+ que receberam o resultado do primeiro CD4 dentro de 33 dias  após a primeira CPN");
      inclusionPeriodMappings =
          "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},location=${location}";
    }

    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    cd.addSearch(
        "pregnantOnPeriod",
        EptsReportUtils.map(
            getFirstPregnancyORBreastfeedingOnClinicalConsultation(
                commonMetadata.getPregnantConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            inclusionPeriodMappings));

    cd.addSearch(
        "transferredIn",
        EptsReportUtils.map(
            QualityImprovement2020Queries.getTransferredInPatients(
                hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
                commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
                hivMetadata.getPatientFoundYesConcept().getConceptId(),
                hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
                hivMetadata.getArtStatus().getConceptId()),
            "startDate=${revisionEndDate-2m+1d},endDate=${revisionEndDate-1m},location=${location}"));

    cd.addSearch(
        "requestCd4ForPregnant",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries
                .getRequestForCd4OnFirstClinicalConsultationOfPregnancy(
                    commonMetadata.getPregnantConcept().getConceptId(),
                    hivMetadata.getYesConcept().getConceptId(),
                    hivMetadata.getApplicationForLaboratoryResearch().getConceptId(),
                    hivMetadata.getCD4AbsoluteOBSConcept().getConceptId()),
            "startDate=${revisionEndDate-3m+1d},endDate=${revisionEndDate-2m},location=${location}"));

    cd.addSearch(
        "resultCd4ForPregnant",
        EptsReportUtils.map(
            qualityImprovement2020CohortQueries.getCd4ResultAfterFirstConsultationOfPregnancy(
                commonMetadata.getPregnantConcept().getConceptId(),
                hivMetadata.getYesConcept().getConceptId()),
            inclusionPeriodMappings));

    if (flag == 9) {
      cd.setCompositionString("(pregnantOnPeriod AND requestCd4ForPregnant) AND NOT transferredIn");
    } else if (flag == 10) {
      cd.setCompositionString("(pregnantOnPeriod AND resultCd4ForPregnant) AND NOT transferredIn");
    }

    return cd;
  }

  public CohortDefinition getPatientsOnMICat18Denominator() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Cat 18 Denominator");
    cd.addParameter(new Parameter("endDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition startedArt = qualityImprovement2020CohortQueries.getMOHArtStartDate();
    CohortDefinition inTarv = resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13();
    CohortDefinition transferredIn =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());

    cd.addSearch(
        "startedArt",
        EptsReportUtils.map(
            startedArt, "startDate=${endDate-14m+1d},endDate=${endDate-13m},location=${location}"));

    cd.addSearch("inTarv", EptsReportUtils.map(inTarv, "endDate=${endDate},location=${location}"));
    cd.addSearch(
        "transferredIn",
        EptsReportUtils.map(
            transferredIn, "startDate=${endDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("(startedArt AND inTarv) AND NOT transferredIn");

    return cd;
  }

  public CohortDefinition getPatientsOnMICat18Numerator() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Cat 18 Numerator");
    cd.addParameter(new Parameter("revisionEndDate", "endDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    CohortDefinition denominator = getPatientsOnMICat18Denominator();
    CohortDefinition diagnose =
        QualityImprovement2020Queries.getDisclosureOfHIVDiagnosisToChildrenAdolescents();

    cd.addSearch(
        "denominator",
        EptsReportUtils.map(denominator, "endDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "diagnose",
        EptsReportUtils.map(
            diagnose,
            "startDate=${revisionEndDate-14m+1d},endDate=${revisionEndDate-13m},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.setCompositionString("denominator AND diagnose");

    return cd;
  }

  /**
   * Incluindo todos os utentes iniciaram TARV e TPT – Isoniazida há 7 meses atrás e são elegíveis
   * ao FIM do TPT - Isoniazida (seguindo os critérios definidos no RF13) e os Iniciaram TARV e TPT-
   * 3HP há 7 meses e são elegíveis ao FIM TPT – 3HP (seguindo os critérios definidos no RF13.1)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getMI7RF20InclusionComposition() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("adultos HIV+ em TARV elegíveis ao TPT que iniciaram e  completaram TPT");
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    String exclusionPeriodMappings =
        "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate},location=${location}";

    // DEFINITIONS FROM RF13
    //    INCLUSIONS

    // DEFINITIONS FROM RF11.2
    CohortDefinition rf11part2 = getMI7RF11part2InclusionsComposition();

    // DEFINITIONS FROM RF12.2
    CohortDefinition rf12part2Inclusions = getMI7RF12part2InclusionsComposition();

    //  EXCLUSIONS
    CohortDefinition tbDiagOnInhPeriodRf13Exclusions =
        qualityImprovement2020CohortQueries.getPatientsWithTBDiagActive();

    CohortDefinition tbSymptomsOnInhPeriodRf13Exclusions =
        qualityImprovement2020CohortQueries.getPatientsWithTBSymtoms();

    CohortDefinition tbTreatmentOnInhPeriodRf13Exclusions =
        qualityImprovement2020CohortQueries.getPatientsWithTBTreatment();

    // DEFINITIONS FROM 13.1
    //  INCLUSIONS
    CohortDefinition rf13part1Inclusions = getMI7RF13part1InclusionsComposition();

    //  EXCLUSIONS
    CohortDefinition tbDiagOn3hpPeriodRf13part1Exclusions =
        qualityImprovement2020CohortQueries.getPatientsWithTBDiagActive3hp();

    CohortDefinition tbSymptomsOn3hpPeriodRf13part1Exclusions =
        qualityImprovement2020CohortQueries.getPatientsWithTBSymtoms3HP();

    CohortDefinition tbTreatmentOn3hpPeriodRf13part1Exclusions =
        qualityImprovement2020CohortQueries.getPatientsWithTBTreatment3HP();

    cd.addSearch(
        "rf11Part2",
        EptsReportUtils.map(rf11part2, "revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "rf12Part2Inclusions",
        EptsReportUtils.map(
            rf12part2Inclusions, "revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "rf13Part1Inclusions",
        EptsReportUtils.map(
            rf13part1Inclusions, "revisionEndDate=${revisionEndDate},location=${location}"));
    cd.addSearch(
        "tbDiagOnInhPeriodRf13Exclusions",
        EptsReportUtils.map(tbDiagOnInhPeriodRf13Exclusions, exclusionPeriodMappings));

    cd.addSearch(
        "tbSymptomsOnInhPeriodRf13Exclusions",
        EptsReportUtils.map(tbSymptomsOnInhPeriodRf13Exclusions, exclusionPeriodMappings));

    cd.addSearch(
        "tbTreatmentOnInhPeriodRf13Exclusions",
        EptsReportUtils.map(tbTreatmentOnInhPeriodRf13Exclusions, exclusionPeriodMappings));

    cd.addSearch(
        "tbDiagOn3hpPeriodRf13part1Exclusions",
        EptsReportUtils.map(tbDiagOn3hpPeriodRf13part1Exclusions, exclusionPeriodMappings));

    cd.addSearch(
        "tbSymptomsOn3hpPeriodRf13part1Exclusions",
        EptsReportUtils.map(tbSymptomsOn3hpPeriodRf13part1Exclusions, exclusionPeriodMappings));

    cd.addSearch(
        "tbTreatmentOn3hpPeriodRf13part1Exclusions",
        EptsReportUtils.map(tbTreatmentOn3hpPeriodRf13part1Exclusions, exclusionPeriodMappings));

    cd.setCompositionString(
        "(rf11Part2 AND (rf12Part2Inclusions OR rf13Part1Inclusions)) AND NOT "
            + "(tbDiagOnInhPeriodRf13Exclusions OR tbSymptomsOnInhPeriodRf13Exclusions OR tbTreatmentOnInhPeriodRf13Exclusions"
            + " OR tbDiagOn3hpPeriodRf13part1Exclusions OR tbSymptomsOn3hpPeriodRf13part1Exclusions OR tbTreatmentOn3hpPeriodRf13part1Exclusions)");

    return cd;
  }

  /**
   * <b>Utentes que Iniciaram TARV há 7 meses e Elegíveis ao INÍCIO do TPT</b>
   * <li>Incluindo todos os utentes que iniciaram TARV há 7 meses (seguindo os critérios definidos
   *     no RF5.2)
   * <li>excluindo todos os utentes com Diagnóstico TB Activa (resposta = “Sim”) registado na Ficha
   *     Clínica numa consulta decorrida durante o período de avaliação (“Data de Consulta”>= “Data
   *     Início Avaliação” e <= “Data Fim Avaliação”)
   * <li>excluindo todos os utentes com “Tem Sintomas TB? (resposta = “Sim”) registado na Ficha
   *     Clínica numa consulta decorrida durante o período de avaliação (“Data de Consulta”>= “Data
   *     Início Avaliação” e <= “Data Fim Avaliação”)
   * <li>excluindo todos os utentes com “Tratamento TB (respostas = {“Início” , “Continua”, “Fim”) e
   *     a respectiva “Data de Tratamento TB” decorrida durante o período de avaliação (“Data de
   *     Tratamento TB”>= “Data Início Avaliação” e <= “Data Fim Avaliação”)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getMI7RF11part2InclusionsComposition() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Utentes que Iniciaram TARV há 7 meses e Elegíveis ao INÍCIO do TPT");
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    String inclusionPeriodMappings =
        "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}";

    CohortDefinition startedART = qualityImprovement2020CohortQueries.getMOHArtStartDate();

    CohortDefinition tbActiveOnPeriod =
        commonCohortQueries.getMohMQPatientsOnCondition(
            false,
            false,
            "once",
            hivMetadata.getAdultoSeguimentoEncounterType(),
            hivMetadata.getActiveTBConcept(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition tbSymptomsOnPeriod =
        commonCohortQueries.getMohMQPatientsOnCondition(
            false,
            false,
            "once",
            hivMetadata.getAdultoSeguimentoEncounterType(),
            tbMetadata.getHasTbSymptomsConcept(),
            Collections.singletonList(hivMetadata.getYesConcept()),
            null,
            null);

    CohortDefinition tbTreatmentOnPeriod =
        commonCohortQueries.getMohMQPatientsOnCondition(
            false,
            false,
            "once",
            hivMetadata.getAdultoSeguimentoEncounterType(),
            tbMetadata.getTBTreatmentPlanConcept(),
            Arrays.asList(
                tbMetadata.getStartDrugsConcept(),
                hivMetadata.getContinueRegimenConcept(),
                hivMetadata.getCompletedConcept()),
            null,
            null);

    cd.addSearch("startedART", EptsReportUtils.map(startedART, inclusionPeriodMappings));

    cd.addSearch(
        "tbActiveOnPeriod", EptsReportUtils.map(tbActiveOnPeriod, inclusionPeriodMappings));

    cd.addSearch(
        "tbSymptomsOnPeriod", EptsReportUtils.map(tbSymptomsOnPeriod, inclusionPeriodMappings));

    cd.addSearch(
        "tbTreatmentOnPeriod", EptsReportUtils.map(tbTreatmentOnPeriod, inclusionPeriodMappings));

    cd.setCompositionString(
        "startedART AND NOT (tbActiveOnPeriod OR tbSymptomsOnPeriod OR tbTreatmentOnPeriod)");

    return cd;
  }

  /**
   * <b>Utentes que Iniciaram TPT – Isoniazida há 7 meses</b>
   * <li>com registo de “Última Profilaxia TPT” = “INH” e “Última Profilaxia TPT (Data Início)” no
   *     formulário “Ficha Resumo” durante o mês de avaliação. Em caso de existência de mais que uma
   *     Ficha Resumo com registo de “Última Profilaxia TPT” = “INH” e “Última Profilaxia TPT (Data
   *     Início)”, deve-se considerar o primeiro a data mais recente durante o mês e avaliação.
   * <li>com o registo de “Profilaxia TPT” = “INH” e “Estado da Profilaxia” = “Início” numa consulta
   *     clínica (Ficha Clínica) ocorrida durante o período de avaliação (“Data de Consulta”>= “Data
   *     Início Avaliação” e <= “Data Fim Avaliação”). Em caso de existência de mais que uma Ficha
   *     Clínica com registo de “Profilaxia TPT” = “INH” e “Estado da Profilaxia” = “Inicio”,
   *     deve-se considerar o primeiro registo, durante o período de avaliação.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getMI7RF12part2InclusionsComposition() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Utentes que Iniciaram TPT – Isoniazida há 7 meses");
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    String inclusionPeriodMappings =
        "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}";

    CohortDefinition startInhFichaClinica = qualityImprovement2020CohortQueries.getB4And1();
    CohortDefinition startInhFichaResumo = qualityImprovement2020CohortQueries.getB4And2();

    cd.addSearch(
        "startInhFichaResumo", EptsReportUtils.map(startInhFichaResumo, inclusionPeriodMappings));

    cd.addSearch(
        "startInhFichaClinica", EptsReportUtils.map(startInhFichaClinica, inclusionPeriodMappings));

    cd.setCompositionString("startInhFichaResumo OR startInhFichaClinica");

    return cd;
  }

  /**
   * <b>Utentes que Iniciaram 3HP há 7 meses</b>
   * <li>com registo de “Última Profilaxia TPT” = “3HP” e “Última Profilaxia TPT (Data Início)”, no
   *     formulário “Ficha Resumo”, durante o mês de avaliação. Em caso de existência de mais que
   *     uma Ficha Resumo com registo de “Última Profilaxia TPT” = “3HP” e “Última Profilaxia TPT
   *     (Data Início)”, deve-se considerar a data mais recente durante o mês de avaliação ou
   * <li>com o registo de “Profilaxia TPT” = ”3HP” e “Estado da Profilaxia” = “Início” numa consulta
   *     clínica (Ficha Clínica) ocorrida durante o mês de avaliação. Em caso de existência de mais
   *     que uma Ficha Clínica com registo do “Profilaxia TPT” = ”3HP” e “Estado da Profilaxia” =
   *     “Início”, deve-se considerar o último registo durante o mês de avaliação.
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getMI7RF13part1InclusionsComposition() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Utentes que Iniciaram TARV e TPT- 3HP há 7 meses e Elegíveis ao FIM TPT – 3HP");
    cd.addParameter(new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    cd.addParameter(new Parameter("location", "location", Location.class));

    String inclusionPeriodMappings =
        "startDate=${revisionEndDate-8m+1d},endDate=${revisionEndDate-7m},location=${location}";

    CohortDefinition start3hpFichaClinica = qualityImprovement2020CohortQueries.getB5And1();
    CohortDefinition start3hpFichaResumo = qualityImprovement2020CohortQueries.getB5And2();

    cd.addSearch(
        "start3hpFichaClinica", EptsReportUtils.map(start3hpFichaClinica, inclusionPeriodMappings));

    cd.addSearch(
        "start3hpFichaResumo", EptsReportUtils.map(start3hpFichaResumo, inclusionPeriodMappings));

    cd.setCompositionString("start3hpFichaClinica OR start3hpFichaResumo");

    return cd;
  }

  /**
   * <b>Categoria 15 Indicador 15.13 Denominador – Oferta / Início MDS</b>
   * <li>incluindo todos os utentes que tiveram pelo menos uma consulta clínica (selecionar a
   *     última) durante o período de avaliação.
   * <li>filtrando os utentes com idade >= 2 anos (seguindo o critério definido no RF10) e que
   *     iniciaram TARV há mais de 3 meses
   * <li>excluindo mulheres grávidas durante o período de avaliação (seguindo os critérios definidos
   *     no RF8)
   * <li>excluindo mulheres lactantes durante o período de avaliação (seguindo os critérios
   *     definidos no RF9)
   * <li>excluindo os utentes com último resultado de CD4 (se existir) registado na “Ficha Clínica”
   *     (coluna 15) abaixo de 200, ou seja, último “Resultado CD4” <= 200 excepto os utentes que
   *     têm um registo de CV disponível na Ficha Clinica (último até “Data Fim de Avaliação”)
   * <li>excluindo os utentes com último resultado de Carga Viral (se existir) registado na “Ficha
   *     Clínica” (coluna 15) maior ou igual a 1000 cópias, ou seja, último “Resultado Carga Viral”
   *     >= 1000
   * <li>excluindo os utentes que já estejam em MDS para utente estável (seguindo os critérios
   *     definidos no RF44)
   * <li>excluindo os utentes que estão em tratamento de TB (seguindo os critérios definifod no RF45
   * <li>excluindo os utentes que tiveram alguma reacção a medicação nos últimos 6 meses (seguindo
   *     os critérios definidos no RF46)
   * <li>excluindo os utentes que tiveram alguma vez Sarcoma de Kaposi (SK) (seguindo os critérios
   *     definidos no RF47)
   * <li>excluindo os utentes que reiniciaram o TARV nos últimos 3 meses (seguindo os critérios
   *     definidos no RF48)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getMI15Den13() {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Denominator 15 - Pacientes elegíveis a MDS");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "end Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition Mq15A = getMI15A();
    CohortDefinition Mq15B1 = getMI15B1();
    CohortDefinition Mq15C = qualityImprovement2020CohortQueries.getMQ15CPatientsMarkedAsPregnant();
    CohortDefinition Mq15D =
        qualityImprovement2020CohortQueries.getMQ15DPatientsMarkedAsBreastfeeding();
    CohortDefinition Mq15F = getMI15F();
    CohortDefinition Mq15G = getMI15G();
    CohortDefinition alreadyMds =
        qualityImprovement2020CohortQueries.getPatientsAlreadyEnrolledInTheMdc();
    CohortDefinition onTB = commonCohortQueries.getPatientsOnTbTreatment();
    CohortDefinition onSK = qualityImprovement2020CohortQueries.getPatientsWithSarcomaKarposi();
    CohortDefinition restartedTreatment = getPatientsWhoRestartedTreatment();
    CohortDefinition finishedTbTreatment = getPatientsWhoFinishedTbTreatmentInLessThan30days();

    cd.addSearch(
        "A",
        EptsReportUtils.map(
            Mq15A, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch(
        "B1",
        EptsReportUtils.map(
            Mq15B1, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "C",
        EptsReportUtils.map(
            Mq15C,
            "startDate=${revisionEndDate-11m+1d},endDate=${revisionEndDate-2m},location=${location}"));
    cd.addSearch(
        "D",
        EptsReportUtils.map(
            Mq15D,
            "startDate=${revisionEndDate-20m+1d},endDate=${revisionEndDate-2m},location=${location}"));
    cd.addSearch(
        "F",
        EptsReportUtils.map(
            Mq15F, "startDate=${startDate},endDate=${endDate},location=${location}"));
    cd.addSearch("G", EptsReportUtils.map(Mq15G, "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "MDS",
        EptsReportUtils.map(
            alreadyMds, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.addSearch(
        "onTB", EptsReportUtils.map(onTB, "endDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "onSK", EptsReportUtils.map(onSK, "endDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "restartedTreatment",
        EptsReportUtils.map(restartedTreatment, "endDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "adverseReaction",
        EptsReportUtils.map(
            getPatientsWithAdverseReactionToMedication(),
            "startDate=${revisionEndDate-6m},endDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "AGE",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnLastClinicalConsultation(2, null),
            "onOrAfter=${revisionEndDate-2m+1d},onOrBefore=${revisionEndDate-1m},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "finishedTbTreatment",
        EptsReportUtils.map(
            finishedTbTreatment,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.setCompositionString(
        "A AND B1 AND NOT (C OR D OR F OR G OR MDS OR onTB OR adverseReaction OR onSK OR restartedTreatment OR finishedTbTreatment) AND AGE");

    return cd;
  }

  /**
   * <b>Utentes que reiniciaram o TARV nos últimos 3 meses</b>
   * <li>De todos os utentes activos em TARV “Data Fim avaliação” seguindo os critérios definidos no
   *     “Resumo Mensal” Indicador B13, o sistema irá filtrar utentes que
   * <li>tiveram interrupção no tratamento 3 meses antes do fim do período de avaliação (“Data fim
   *     de avaliação” menos (-) 3 meses) (FR49) e
   * <li>tiveram pelo menos 1 registo de levantamento no “FILA” ou na “Ficha Recepção - Levantou
   *     ARV” nos últimos 3 meses do fim do períodio (“Data de levantamento ” >= “Data Fim de
   *     Avaliação” menos (–) 3 meses e <= “Data Fim de Avaliação”)
   * <li>O sistema irá excluir: Utentes Transferidos de outras Unidades Sanitárias nos últimos 3
   *     meses (Data de transferência >= “Data Fim de Avaliação” menos(–) 3 meses e <= “Data Fim de
   *     Avaliação”) (RF50)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoRestartedTreatment() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName("Patients who returned to treatment");
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition transferredIn =
        QualityImprovement2020Queries.getTransferredInPatients(
            hivMetadata.getMasterCardEncounterType().getEncounterTypeId(),
            commonMetadata.getTransferFromOtherFacilityConcept().getConceptId(),
            hivMetadata.getPatientFoundYesConcept().getConceptId(),
            hivMetadata.getTypeOfPatientTransferredFrom().getConceptId(),
            hivMetadata.getArtStatus().getConceptId());
    cd.addSearch(
        "B13",
        EptsReportUtils.map(
            resumoMensalCohortQueries.getPatientsWhoWereActiveByEndOfMonthB13(),
            "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "treatmentInterruption",
        EptsReportUtils.map(
            getPatientsWhoExperiencedInterruptionIn3MonthsBeforeReportingEndDate(),
            "endDate=${endDate-3m},location=${location}"));

    cd.addSearch(
        "filaOrDrugPickup",
        EptsReportUtils.map(
            eriDSDCohortQueries.getFilaOrDrugPickup(), "endDate=${endDate},location=${location}"));

    cd.addSearch(
        "pickUpsOnPreviousPeriod",
        EptsReportUtils.map(
            PatientsWithPickUpsOnPreviousPeriod(), "endDate=${endDate-3m},location=${location}"));

    cd.addSearch(
        "transferredIn",
        EptsReportUtils.map(
            transferredIn,
            "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    cd.setCompositionString(
        "(B13 AND (treatmentInterruption OR NOT pickUpsOnPreviousPeriod) AND filaOrDrugPickup) AND NOT transferredIn");
    return cd;
  }

  /**
   * <b>Categoria 15 Indicador 15.13 Numerador – Oferta/Início MDS</b>
   * <li>Incluindo todos os utentes seleccionados no Indicador 15.13 Denominador definido no RF35
   *     (Categoria 15 Indicador 15.13 – Denominador Oferta/Início) e
   * <li>Filtrando os utentes que têm o registo de início do MDS para utente estável na última
   *     consulta decorrida há 12 meses (última “Data Consulta Clínica” >= “Data Fim de Avaliação” –
   *     12 meses + 1dia e <= “Data Fim de Avaliação”), ou seja, registo de um MDC (MDC1 ou MDC2 ou
   *     MDC3 ou MDC4 ou MDC5) como: “GA” e o respectivo “Estado” = “Início” ou “DT” e o respectivo
   *     “Estado” = “Início” ou “DS” e o respectivo “Estado” = “Início” ou “APE” e o respectivo
   *     “Estado” = “Início” ou “FR” e o respectivo “Estado” = “Início” ou “DD” e o respectivo
   *     “Estado” = “Início” ou “DA” e o respectivo “Estado” = “Início”
   * <li>Filtrando os utentes que têm o registo de “Tipo de Dispensa” = “DT” na última consulta
   *     (“Ficha Clínica”) decorrida há 12 meses (última “Data Consulta Clínica” >= “Data Fim de
   *     Avaliação” –12 meses+1dia e <= “Data Fim de Avaliação”) ou
   * <li>Filtrando os utentes com registo de “Tipo de Dispensa” = “DS” na última consulta (“Ficha
   *     Clínica”) decorrida há 12 meses (última “Data Consulta Clínica” >= “Data Fim de Avaliação”
   *     –12 meses+1dia e <= “Data Fim de Avaliação”) ou
   * <li>Filtrando os utentes com registo de “Tipo de Dispensa” = “DA” na última consulta (“Ficha
   *     Clínica”) decorrida há 12 meses (última “Data Consulta Clínica” >= “Data Fim de Avaliação”
   *     –12 meses+1dia e <= “Data Fim de Avaliação”) ou
   * <li>Filtrando os utentes com registo de último levantamento na farmácia (FILA) há 12 meses
   *     (última “Data Levantamento”>= “Data Fim de Avaliação” – 12 meses+1dia e <= “Data Fim de
   *     Avaliação”) com próximo levantamento agendado para 83 a 97 dias (“Data Próximo
   *     Levantamento” menos “Data Levantamento”>= 83 dias e <= 97 dias)
   * <li>Filtrando os utentes com registo de último levantamento na farmácia (FILA) há 12 meses
   *     (última “Data Levantamento”>= “Data Fim de Avaliação” – 12 meses+1dia e <= “Data Fim de
   *     Avaliação”) com próximo levantamento agendado para 173 a 187 dias (“Data Próximo
   *     Levantamento” menos “Data Levantamento”>= 173 dias e <= 187 dias).
   * <li>Filtrando os utentes com registo de último levantamento na farmácia (FILA) há 12 meses
   *     (última “Data Levantamento”>= “Data Fim de Avaliação” –12 meses+1dia e <= “Data Fim de
   *     Avaliação”) com próximo levantamento agendado para 335 a 395 dias (“Data Próximo
   *     Levantamento” menos “Data Levantamento”>= 335 dias e <= 395 dias)
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getMI15Nume13() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Numerator MQ 15 - Pacientes elegíveis a MDS");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "end Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition Mi15Den13 = getMI15Den13();

    List<Integer> mdsConcepts =
        Arrays.asList(
            hivMetadata.getGaac().getConceptId(),
            hivMetadata.getQuarterlyDispensation().getConceptId(),
            hivMetadata.getDispensaComunitariaViaApeConcept().getConceptId(),
            hivMetadata.getDescentralizedArvDispensationConcept().getConceptId(),
            hivMetadata.getRapidFlow().getConceptId(),
            hivMetadata.getSemiannualDispensation().getConceptId());

    List<Integer> states = Arrays.asList(hivMetadata.getStartDrugs().getConceptId());

    CohortDefinition mds =
        qualityImprovement2020CohortQueries
            .getPatientsWhoHadMdsOnMostRecentClinicalAndPickupOnFilaFR36(mdsConcepts, states);

    cd.addSearch(
        "MQ15Den13",
        EptsReportUtils.map(
            Mi15Den13,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "MDS",
        EptsReportUtils.map(mds, "startDate=${startDate},endDate=${endDate},location=${location}"));

    cd.setCompositionString("MQ15Den13 AND MDS");
    return cd;
  }

  /**
   * <b>Utentes Transferidos Para Outra US</b>
   * <li>Último registo de [“Mudança Estado Permanência TARV” (Coluna 21) = “T” (Transferido Para)
   *     na “Ficha Clínica” com “Data da Consulta Actual” (Coluna 1, durante a qual se fez o registo
   *     da mudança do estado de permanência TARV) até a “Data Recolha de Dados” ou
   * <li>Registados como “Mudança Estado Permanência TARV” = “Transferido Para”, último estado
   *     registado na “Ficha Resumo” com “Data da Transferência” até a “Data Recolha de Dados”;
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getTranferredOutPatientsForMI7() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Utentes Transferidos Para Outra US");
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("53", hivMetadata.getMasterCardEncounterType().getEncounterTypeId());
    map.put("6272", hivMetadata.getStateOfStayOfPreArtPatient().getConceptId());
    map.put("6273", hivMetadata.getStateOfStayOfArtPatient().getConceptId());
    map.put("1706", hivMetadata.getTransferredOutConcept().getConceptId());

    String query =
        "SELECT max_transferout.patient_id "
            + "FROM  patient p "
            + "          JOIN encounter e "
            + "               ON p.patient_id = e.patient_id "
            + "          JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + " "
            + "               JOIN ( "
            + getLastPatientStateOfStayFromSources()
            + "       ) max_transferout ON max_transferout.patient_id = p.patient_id "
            + "WHERE    p.voided = 0 "
            + "                     AND e.voided = 0 "
            + "                     AND e.location_id = :location "
            + "                     AND e.encounter_type = ${6} "
            + "                     AND e.encounter_datetime = max_transferout.transferout_date "
            + "                     AND o.voided = 0 "
            + "                     AND o.concept_id = ${6273} "
            + "                     AND o.value_coded = ${1706} "
            + "GROUP  BY p.patient_id "
            + "UNION "
            + "SELECT max_transferout.patient_id "
            + "FROM  patient p "
            + "          JOIN encounter e "
            + "               ON p.patient_id = e.patient_id "
            + "          JOIN obs o "
            + "               ON e.encounter_id = o.encounter_id "
            + " "
            + "          JOIN ( "
            + getLastPatientStateOfStayFromSources()
            + ") max_transferout ON max_transferout.patient_id = p.patient_id "
            + "WHERE    p.voided = 0 "
            + "  AND e.voided = 0 "
            + "  AND e.location_id = :location "
            + "  AND e.encounter_type = ${53} "
            + "  AND o.obs_datetime = max_transferout.transferout_date "
            + "  AND o.voided = 0 "
            + "  AND o.concept_id = ${6272} "
            + "  AND o.value_coded = ${1706} "
            + "GROUP  BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  private static String getLastPatientStateOfStayFromSources() {
    return "           SELECT transferout.patient_id, "
        + "                  Max(transferout.transferout_date) transferout_date "
        + "           FROM   (SELECT p.patient_id, "
        + "                          Max(e.encounter_datetime) AS transferout_date "
        + "                   FROM   patient p "
        + "                              JOIN encounter e "
        + "                                   ON p.patient_id = e.patient_id "
        + "                              JOIN obs o "
        + "                                   ON e.encounter_id = o.encounter_id "
        + "                   WHERE  p.voided = 0 "
        + "                     AND e.voided = 0 "
        + "                     AND e.location_id = :location "
        + "                     AND e.encounter_type = ${6} "
        + "                     AND e.encounter_datetime <= :revisionEndDate "
        + "                     AND o.voided = 0 "
        + "                     AND o.concept_id = ${6273} "
        + "                     AND o.value_coded IS NOT NULL "
        + "                   GROUP  BY p.patient_id "
        + "                   UNION "
        + "                   SELECT p.patient_id, "
        + "                          Max(o.obs_datetime) AS transferout_date "
        + "                   FROM   patient p "
        + "                              JOIN encounter e "
        + "                                   ON p.patient_id = e.patient_id "
        + "                              JOIN obs o "
        + "                                   ON e.encounter_id = o.encounter_id "
        + "                   WHERE  p.voided = 0 "
        + "                     AND e.voided = 0 "
        + "                     AND e.location_id = :location "
        + "                     AND e.encounter_type = ${53} "
        + "                     AND o.obs_datetime <= :revisionEndDate "
        + "                     AND o.voided = 0 "
        + "                     AND o.concept_id = ${6272} "
        + "                     AND o.value_coded IS NOT NULL "
        + "                   GROUP  BY p.patient_id) transferout "
        + "           GROUP  BY transferout.patient_id ";
  }

  /**
   * <b>Utentes com registro de interrupção no tratamento</b>
   *
   * <p>Todos os utentes com a data mais recente (até a “Data Recolha Dados” – 3 meses) entre
   * <li>o último agendamento do levantamento (“Data do próximo Levantamento”) registado no último
   *     levantamento no FILA
   * <li>o último levantamento registado na “Ficha Recepção – Levantou ARV” mais (+) 30 dias
   *
   *     <p>e adicionando 59 dias, esta data deve ser menor que a “Data Recolha de Dados” menos (–)
   *     3 meses
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoExperiencedInterruptionIn3MonthsBeforeReportingEndDate() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Utentes Transferidos Para Outra US");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());

    String query =
        "SELECT   final.patient_id "
            + "FROM    ( "
            + "                  SELECT   patient_id, "
            + "                           Max(last_schedule.last_datetime) AS most_recent "
            + "                  FROM     ( "
            + "                                      SELECT     p.patient_id, "
            + "                                                 o.value_datetime AS last_datetime "
            + "                                      FROM       patient p "
            + "                                      INNER JOIN encounter e "
            + "                                      ON         e.patient_id = p.patient_id "
            + "                                      INNER JOIN obs o "
            + "                                      ON         o.encounter_id = e.encounter_id "
            + "                                      INNER JOIN "
            + "                                                 ( "
            + "                                                            SELECT     p.patient_id, "
            + "                                                                       MAX(e.encounter_datetime) last_pickup "
            + "                                                            FROM       patient p "
            + "                                                            INNER JOIN encounter e "
            + "                                                            ON         e.patient_id = p.patient_id "
            + "                                                            INNER JOIN obs o "
            + "                                                            ON         o.encounter_id = e.encounter_id "
            + "                                                            WHERE      e.encounter_type = ${18} "
            + "                                                            AND        p.voided = 0 "
            + "                                                            AND        e.voided = 0 "
            + "                                                            AND        e.location_id = :location "
            + "                                                            AND        o.voided = 0 "
            + "                                                            AND        e.encounter_datetime <= :endDate "
            + "                                                            GROUP BY   p.patient_id ) last_fila "
            + "                                      ON         last_fila.patient_id = p.patient_id "
            + "                                      WHERE      e.encounter_type = ${18} "
            + "                                      AND        p.voided = 0 "
            + "                                      AND        e.voided = 0 "
            + "                                      AND        e.location_id = :location "
            + "                                      AND        o.voided = 0 "
            + "                                      AND        o.concept_id = ${5096} "
            + "                                      AND        o.value_datetime IS NOT NULL "
            + "                                      AND        e.encounter_datetime = last_fila.last_pickup "
            + "                                      GROUP BY   p.patient_id "
            + "                                      UNION "
            + "                                      SELECT     p.patient_id, "
            + "                                                 MAX(DATE_ADD(o.value_datetime, INTERVAL 30 DAY)) AS last_datetime "
            + "                                      FROM       patient p "
            + "                                      INNER JOIN encounter e "
            + "                                      ON         e.patient_id = p.patient_id "
            + "                                      INNER JOIN obs o "
            + "                                      ON         o.encounter_id = e.encounter_id "
            + "                                      WHERE      e.encounter_type = ${52} "
            + "                                      AND        e.location_id = :location "
            + "                                      AND        o.value_datetime <= :endDate "
            + "                                      AND        o.concept_id = ${23866} "
            + "                                      AND        p.voided = 0 "
            + "                                      AND        e.voided = 0 "
            + "                                      AND        o.voided = 0 "
            + "                                      GROUP BY   p.patient_id ) last_schedule "
            + "                  GROUP BY last_schedule.patient_id "
            + "                  HAVING   DATE_ADD(most_recent, INTERVAL 59 DAY) < :endDate ) final "
            + "GROUP BY final.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>Utentes que terminaram tratamento de TB há menos de 30 dias(Exclusão)</b>
   * <li>Todos os utentes com último registo de Tratamento TB= Fim (F), com respectiva data de fim
   *     de tratamento (“Data Fim TB”) numa consulta clínica (Ficha Clínica- MasterCard) ocorrida
   *     até a “Data Recolha Dados” e
   * <li>Sendo esta “Data Fim TB” ocorrida há menos de 30 dias da última consulta do período de
   *     avaliação (“Data Última Consulta”), ou seja, “Data Última Consulta” menos (-) “Última
   *     Consulta Fim TB” <= 30 dias.
   *
   *     <p><strong>Nota:</strong> A “Data Última Consulta” é a última “Data de Consulta” no período
   *     compreendido entre: “Data Início Avaliação” = “Data de Recolha Dados” menos (-) 2 meses
   *     mais (+) 1 dia “Data Fim Avaliação” = “Data de Recolha Dados” menos (-) 1 mês
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWhoFinishedTbTreatmentInLessThan30days() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Utentes  que terminaram o Tratamento de TB há menos de 30 dias");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "end Date", Date.class));
    sqlCohortDefinition.addParameter(
        new Parameter("revisionEndDate", "revisionEndDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("1268", hivMetadata.getTBTreatmentPlanConcept().getConceptId());
    map.put("1267", hivMetadata.getCompletedConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN (SELECT e.patient_id, "
            + "                          MAX(e.encounter_datetime) AS most_recent "
            + "                   FROM   encounter e "
            + "                   WHERE  e.voided = 0 "
            + "                          AND e.location_id = :location "
            + "                          AND e.encounter_type = ${6} "
            + "                          AND e.encounter_datetime >= :startDate "
            + "                          AND e.encounter_datetime <= :endDate "
            + "                   GROUP  BY e.patient_id) last_consultation "
            + "               ON last_consultation.patient_id = p.patient_id "
            + "       INNER JOIN (SELECT p.patient_id, "
            + "                          MAX(o.obs_datetime) AS last_tb_treatment "
            + "                   FROM   patient p "
            + "                          INNER JOIN encounter e "
            + "                                  ON e.patient_id = p.patient_id "
            + "                          INNER JOIN obs o "
            + "                                  ON o.encounter_id = e.encounter_id "
            + "                   WHERE  p.voided = 0 "
            + "                          AND e.voided = 0 "
            + "                          AND o.voided = 0 "
            + "                          AND e.location_id = :location "
            + "                          AND e.encounter_type = ${6} "
            + "                          AND o.concept_id = ${1268} "
            + "                          AND o.value_coded = ${1267} "
            + "                          AND e.encounter_datetime <= :revisionEndDate "
            + "                   GROUP  BY p.patient_id) finished_treatment "
            + "               ON finished_treatment.patient_id = p.patient_id "
            + "WHERE  p.voided = 0 "
            + "       AND ABS(TIMESTAMPDIFF(DAY, last_consultation.most_recent, "
            + "            +  finished_treatment.last_tb_treatment)) <= 30";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>Utentes que tiveram alguma reacção a medicação nos últimos 6 meses</b>
   *
   * <p>O sistema irá identificar os utentes que tiveram alguma reacção à medicação, nos últimos 6
   * meses, selecionando os utentes com registo dos seguintes efeitos secundários na consulta
   * clínica (Ficha Clinica - MasterCard) decorrida nos últimos 6 meses (“Data Recolha Dados” menos
   * (-) 6 meses e “Data Recolha Dados”):
   * <li>Citopenia
   * <li>Pancreatite
   * <li>Nefrotoxicidade
   * <li>Hepatite
   * <li>Síndrome de Stevens Johnson
   * <li>Hipersensibilidade a ABC/RAL
   * <li>Acidose láctica
   * <li>Esteatose hepática c/ hiperlactemia
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getPatientsWithAdverseReactionToMedication() {

    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName(
        "Utentes  que tiveram alguma reacção à medicação, nos últimos 6 meses ");
    sqlCohortDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("endDate", "end Date", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
    map.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
    map.put("2015", hivMetadata.getAdverseReaction().getConceptId());
    map.put("23748", hivMetadata.getCytopeniaConcept().getConceptId());
    map.put("6293", hivMetadata.getPancreatitis().getConceptId());
    map.put("23749", hivMetadata.getNephrotoxicityConcept().getConceptId());
    map.put("29", hivMetadata.getHepatitisConcept().getConceptId());
    map.put("23750", hivMetadata.getStevensJonhsonSyndromeConcept().getConceptId());
    map.put("23751", hivMetadata.getHypersensitivityToAbcOrRailConcept().getConceptId());
    map.put("6299", hivMetadata.getLacticAcidosis().getConceptId());
    map.put("23752", hivMetadata.getHepaticSteatosisWithHyperlactataemiaConcept().getConceptId());

    String query =
        "SELECT p.patient_id "
            + "FROM   patient p "
            + "       INNER JOIN encounter e "
            + "               ON e.patient_id = p.patient_id "
            + "       INNER JOIN obs o "
            + "               ON o.encounter_id = e.encounter_id "
            + "WHERE  e.encounter_type IN ( ${6}, ${9}) "
            + "       AND p.voided = 0 "
            + "       AND e.voided = 0 "
            + "       AND e.location_id = :location "
            + "       AND o.voided = 0 "
            + "       AND o.concept_id = ${2015}"
            + "       AND o.value_coded IN ( ${23748}, ${6293}, ${23749}, ${29}, "
            + "                              ${23750}, ${23751}, ${6299}, ${23752}) "
            + "       AND e.encounter_datetime >= :startDate "
            + "       AND e.encounter_datetime <= :endDate "
            + "GROUP  BY p.patient_id";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  public CohortDefinition PatientsWithPickUpsOnPreviousPeriod() {
    SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition();
    sqlCohortDefinition.setName("Utentes Transferidos Para Outra US");
    sqlCohortDefinition.addParameter(new Parameter("endDate", "endDate", Date.class));
    sqlCohortDefinition.addParameter(new Parameter("location", "location", Location.class));

    Map<String, Integer> map = new HashMap<>();
    map.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
    map.put("52", hivMetadata.getMasterCardDrugPickupEncounterType().getEncounterTypeId());
    map.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());
    map.put("23866", hivMetadata.getArtDatePickupMasterCard().getConceptId());

    String query =
        " SELECT   patient_id "
            + " FROM     ( "
            + "                     SELECT     p.patient_id, "
            + "                                o.value_datetime AS last_datetime "
            + "                     FROM       patient p "
            + "                     INNER JOIN encounter e "
            + "                     ON         e.patient_id = p.patient_id "
            + "                     INNER JOIN obs o "
            + "                     ON         o.encounter_id = e.encounter_id "
            + "                     INNER JOIN "
            + "                                ( "
            + "                                           SELECT     p.patient_id, "
            + "                                                      MAX(e.encounter_datetime) last_pickup "
            + "                                           FROM       patient p "
            + "                                           INNER JOIN encounter e "
            + "                                           ON         e.patient_id = p.patient_id "
            + "                                           INNER JOIN obs o "
            + "                                           ON         o.encounter_id = e.encounter_id "
            + "                                           WHERE      e.encounter_type = ${18} "
            + "                                           AND        p.voided = 0 "
            + "                                           AND        e.voided = 0 "
            + "                                           AND        e.location_id = :location "
            + "                                           AND        o.voided = 0 "
            + "                                           AND        e.encounter_datetime <= :endDate "
            + "                                           GROUP BY   p.patient_id ) last_fila "
            + "                     ON         last_fila.patient_id = p.patient_id "
            + "                     WHERE      e.encounter_type = ${18} "
            + "                     AND        p.voided = 0 "
            + "                     AND        e.voided = 0 "
            + "                     AND        e.location_id = :location "
            + "                     AND        o.voided = 0 "
            + "                     AND        o.concept_id = ${5096} "
            + "                     AND        o.value_datetime IS NOT NULL "
            + "                     AND        e.encounter_datetime = last_fila.last_pickup "
            + "                     GROUP BY   p.patient_id "
            + "                     UNION "
            + "                     SELECT     p.patient_id, "
            + "                                MAX(o.value_datetime) AS last_datetime "
            + "                     FROM       patient p "
            + "                     INNER JOIN encounter e "
            + "                     ON         e.patient_id = p.patient_id "
            + "                     INNER JOIN obs o "
            + "                     ON         o.encounter_id = e.encounter_id "
            + "                     WHERE      e.encounter_type = ${52} "
            + "                     AND        e.location_id = :location "
            + "                     AND        o.value_datetime <= :endDate "
            + "                     AND        o.concept_id = ${23866} "
            + "                     AND        p.voided = 0 "
            + "                     AND        e.voided = 0 "
            + "                     AND        o.voided = 0 "
            + "                     GROUP BY   p.patient_id ) last_schedule ";

    StringSubstitutor stringSubstitutor = new StringSubstitutor(map);

    sqlCohortDefinition.setQuery(stringSubstitutor.replace(query));

    return sqlCohortDefinition;
  }

  /**
   * <b>adultos (15/+anos) na 1ª aou 2ª linha de TARV que tiveram consulta clínica no período de
   * revisão e que eram elegíveis ao pedido de CV </b>
   *
   * <p>Incluindo o somatório do resultado dos seguintes indicadores - para denominador:
   * <li>Denominador do Indicador 13.1-1ª Linha da Categoria 13 Adulto de Pedido de CV (RF16.1)
   * <li>Denominador do Indicador 13.4-2ª Linha da Categoria 13 Adulto de Pedido de CV (RF18)
   *
   *     <p>Incluindo o somatório do resultado dos seguintes indicadores - para numerador:
   * <li>Numerador do Indicador 13.1-1ª Linha da Categoria 13 Adulto de Pedido de CV (RF17.1).
   * <li>Numerador do Indicador 13.4-2ª Linha da Categoria 13 Adulto de Pedido de CV (RF19).
   *
   * @param denominator boolean parameter to choose between Denominator and Numerator
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getSumOfPatientsIn1stOr2ndLineOfArt(Boolean denominator) {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName(
        "# de adultos (15/+anos) na 1ª ou 2ª linha de TARV - Somatorio (numerador e denominador)");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    if (denominator) {
      cd.addSearch("PRIMEIRALINHA", Mapped.mapStraightThrough(getCat13Den(1, false)));

      cd.addSearch("SEGUNDALINHA", Mapped.mapStraightThrough(getCat13Den(4, false)));
    } else {
      cd.addSearch("PRIMEIRALINHA", Mapped.mapStraightThrough(getCat13Den(1, true)));

      cd.addSearch("SEGUNDALINHA", Mapped.mapStraightThrough(getCat13Den(4, true)));
    }

    cd.setCompositionString("PRIMEIRALINHA OR SEGUNDALINHA");

    return cd;
  }

  /**
   * <b>% de adultos (15/+anos) coinfectados TB/HIV com consulta clínica no período de revisão,
   * elegíveis ao pedido de CV e com registo de pedido de CV</b>
   * <li>incluindo todos os utentes com idade >= 15 anos (seguindo o critério definido no RF12) e
   *     que tiveram o registo de pelo menos uma consulta clínica durante o período de revisão
   *     (“Data Última Consulta”>= “Data Início Revisão” e <= “Data Fim Revisão”). Nota: considerar
   *     a última consulta clínica durante o período de revisão.
   * <li>incluindo as mulheres lactantes (independentemente da idade) registadas na última consulta
   *     clínica (seguindo o critério definido no RF11). Nota: serão considerados os dois grupos,
   *     adultos >=15 anos, e também as mulheres lactantes independentemente da idade.
   * <li>filtrando os utentes em 1ª Linha de TARV elegíveis ao pedido de Carga Viral (CV), seguindo
   *     os critérios definidos no RF14, ou os utentes em 2ª Linha de TARV elegíveis ao pedido de
   *     Carga Viral (CV), seguindo os critérios definidos no RF15.
   * <li>filtrando os utentes com diagnóstico TB Activa durante o período de revisão (RF60).
   * <li>excluindo mulheres grávidas registadas na última consulta clínica (seguindo os critérios
   *     definidos no RF10).
   *
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getMI13NewDen4() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName(
        " adultos (15/+anos) coinfectados TB/HIV com consulta clínica no período de revisão");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition lastClinical = commonCohortQueries.getMOHPatientsLastClinicalConsultation();

    CohortDefinition pregnant =
        commonCohortQueries.getNewMQPregnantORBreastfeeding(
            hivMetadata.getPregnantConcept().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition breastfeeding =
        commonCohortQueries.getNewMQPregnantORBreastfeeding(
            hivMetadata.getBreastfeeding().getConceptId(),
            hivMetadata.getYesConcept().getConceptId());

    CohortDefinition firstLine =
        qualityImprovement2020CohortQueries.getUtentesPrimeiraLinha(
            QualityImprovement2020CohortQueries.UtentesPrimeiraLinhaPreposition.MI);

    CohortDefinition secondLine =
        qualityImprovement2020CohortQueries.getUtentesSegundaLinha(
            QualityImprovement2020CohortQueries.UtentesSegundaLinhaPreposition.MI);

    CohortDefinition tbDiagnosisActive =
        qualityImprovement2020CohortQueries.getPatientsWithTbActiveOrTbTreatment();

    cd.addSearch(
        "AGE",
        EptsReportUtils.map(
            commonCohortQueries.getMOHPatientsAgeOnLastClinicalConsultationDate(15, null),
            "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "CONSULTATION",
        EptsReportUtils.map(
            lastClinical,
            "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "PREGNANT",
        EptsReportUtils.map(
            pregnant, "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "BREASTFEEDING",
        EptsReportUtils.map(
            breastfeeding,
            "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "FIRSTLINE",
        EptsReportUtils.map(
            firstLine,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "SECONDLINE",
        EptsReportUtils.map(
            secondLine,
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "TBACTIVE",
        EptsReportUtils.map(
            tbDiagnosisActive,
            "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    cd.setCompositionString(
        "((CONSULTATION OR BREASTFEEDING) AND (FIRSTLINE OR SECONDLINE) AND TBACTIVE AND AGE) AND NOT PREGNANT ");

    return cd;
  }

  public CohortDefinition getMI13NewNum4() {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName(
        "% de adultos (15/+anos) coinfectados TB/HIV com consulta clínica no período de "
            + "revisão, elegíveis ao pedido de CV e com registo de pedido de CV");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition cvExamRequest = qualityImprovement2020CohortQueries.getMQ13G();

    cd.addSearch(
        "EXAMREQUEST",
        EptsReportUtils.map(
            cvExamRequest,
            "startDate=${startDate},endDate=${revisionEndDate},location=${location}"));

    cd.addSearch(
        "DENOMINATOR",
        EptsReportUtils.map(
            getMI13NewDen4(),
            "startDate=${startDate},endDate=${endDate},revisionEndDate=${revisionEndDate},location=${location}"));

    cd.setCompositionString("DENOMINATOR AND EXAMREQUEST");

    return cd;
  }

  /**
   * <b># de crianças na 1a linha (10-14 anos de idade) ou 2ª linha (0-14 anos) de TARV que tiveram
   * consulta clínica no período de revisão e que eram elegíveis ao pedido de CV</b>
   *
   * <p>Incluindo o somatório do resultado dos seguintes indicadores - para denominador:
   * <li>Denominador do Indicador 13.8-1ª Linha da Categoria 13 Pediátrico de Pedido de CV (RF24.1).
   * <li>Denominador do Indicador 13.13-2ª Linha da Categoria 13 Pediátrico de Pedido de CV (RF26).
   *
   *     <p>Incluindo o somatório do resultado dos seguintes indicadores - para numerador:
   * <li>Numerador do Indicador 13.8-1ª Linha da Categoria 13 Pediátrico de Pedido de CV (RF25.1).
   * <li>Numerador do Indicador 13.13-2ª Linha da Categoria 13 Pediátrico de Pedido de CV (RF27).
   *
   * @param denominator boolean parameter to choose between Denominator and Numerator
   * @return {@link CohortDefinition}
   */
  public CohortDefinition getSumOfPatientsIn1stOr2ndLineOfArtForDenNum8(Boolean denominator) {

    CompositionCohortDefinition cd = new CompositionCohortDefinition();

    cd.setName(
        "# criancas (10-14 anos de idade) na 1ª ou 2ª linha de TARV - Somatorio (numerador e denominador)");
    cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    cd.addParameter(new Parameter("endDate", "End Date", Date.class));
    cd.addParameter(new Parameter("revisionEndDate", "Revision End Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    if (denominator) {
      cd.addSearch("PRIMEIRALINHA", Mapped.mapStraightThrough(getCat13Den(8, false)));

      cd.addSearch("SEGUNDALINHA", Mapped.mapStraightThrough(getCat13Den(13, false)));
    } else {
      cd.addSearch("PRIMEIRALINHA", Mapped.mapStraightThrough(getCat13Den(8, true)));

      cd.addSearch("SEGUNDALINHA", Mapped.mapStraightThrough(getCat13Den(13, true)));
    }

    cd.setCompositionString("PRIMEIRALINHA OR SEGUNDALINHA");

    return cd;
  }
}
