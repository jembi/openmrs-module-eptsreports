package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.Arrays;
import java.util.List;
import org.openmrs.module.eptsreports.reporting.library.cohorts.TxPvlsBySourceClinicalOrFichaResumoCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.TxPvlsBySourceLabOrFsrCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.dimensions.AgeDimensionCohortInterface;
import org.openmrs.module.eptsreports.reporting.library.dimensions.EptsCommonDimension;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TxPvlsBySourceDataset extends BaseDataSet {

  private TxPvlsBySourceLabOrFsrCohortQueries txPvlsBySourceLabOrFsrCohortQueries;
  private EptsCommonDimension eptsCommonDimension;
  private EptsGeneralIndicator eptsGeneralIndicator;
  private AgeDimensionCohortInterface ageDimensionCohortInterface;

  private TxPvlsBySourceClinicalOrFichaResumoCohortQueries
      txPvlsBySourceClinicalOrFichaResumoCohortQueries;

  @Autowired
  public TxPvlsBySourceDataset(
      TxPvlsBySourceLabOrFsrCohortQueries txPvlsBySourceLabOrFsrCohortQueries,
      TxPvlsBySourceClinicalOrFichaResumoCohortQueries
          txPvlsBySourceClinicalOrFichaResumoCohortQueries,
      EptsCommonDimension eptsCommonDimension,
      EptsGeneralIndicator eptsGeneralIndicator,
      @Qualifier("commonAgeDimensionCohort")
          AgeDimensionCohortInterface ageDimensionCohortInterface) {
    this.txPvlsBySourceLabOrFsrCohortQueries = txPvlsBySourceLabOrFsrCohortQueries;
    this.txPvlsBySourceClinicalOrFichaResumoCohortQueries =
        txPvlsBySourceClinicalOrFichaResumoCohortQueries;
    this.eptsCommonDimension = eptsCommonDimension;
    this.eptsGeneralIndicator = eptsGeneralIndicator;
    this.ageDimensionCohortInterface = ageDimensionCohortInterface;
  }

  String mappings = "startDate=${startDate},endDate=${endDate},location=${location}";
  String mappingsKp = "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}";

  public DataSetDefinition getPvlsLabFsr() {
    CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
    dsd.setName("PLF");
    dsd.setDescription("PVLS_LAB_FSR");
    dsd.addParameters(getParameters());
    // Tie dimensions to this data definition
    dsd.addDimension("gender", EptsReportUtils.map(eptsCommonDimension.gender(), ""));
    dsd.addDimension(
        "query", EptsReportUtils.map(eptsCommonDimension.maternityDimension(), mappings));
    dsd.addDimension(
        "age",
        EptsReportUtils.map(
            eptsCommonDimension.age(ageDimensionCohortInterface), "effectiveDate=${endDate}"));
    dsd.addDimension(
        "rt",
        EptsReportUtils.map(
            eptsCommonDimension.getViralLoadRoutineTargetReasonsDimensionForPvlsBySource(),
            mappings));
    dsd.addDimension(
        "KP", EptsReportUtils.map(eptsCommonDimension.getKeyPopsDimension(), mappingsKp));

    addNumeratorColumnsForLabAndFsr(dsd, mappings);
    addDenominatorColumnsForLabAndFsr(dsd, mappings);
    return dsd;
  }

  public DataSetDefinition getPvlFichaMestre() {
    CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
    dsd.setName("PFM");
    dsd.setDescription("PVLS_Ficha_Mestre");
    dsd.addParameters(getParameters());
    // Tie dimensions to this data definition
    dsd.addDimension("gender", EptsReportUtils.map(eptsCommonDimension.gender(), ""));
    dsd.addDimension(
        "query", EptsReportUtils.map(eptsCommonDimension.maternityDimension(), mappings));
    dsd.addDimension(
        "age",
        EptsReportUtils.map(
            eptsCommonDimension.age(ageDimensionCohortInterface), "effectiveDate=${endDate}"));
    dsd.addDimension(
        "rt",
        EptsReportUtils.map(
            eptsCommonDimension.getViralLoadRoutineTargetReasonsBySourceDimension(), mappings));
    dsd.addDimension(
        "KP", EptsReportUtils.map(eptsCommonDimension.getKeyPopsDimension(), mappingsKp));
    addDenominatorColumnsForClinicalForms(dsd, mappings);
    addNumeratorColumnsForClinicalForms(dsd, mappings);
    return dsd;
  }

  private void addDenominatorColumnsForLabAndFsr(
      CohortIndicatorDataSetDefinition dsd, String mappings) {
    dsd.addColumn(
        "DTT",
        "Total patients with Viral load - Denominator based on Lab or FSR",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "patients with viral load",
                EptsReportUtils.map(
                    txPvlsBySourceLabOrFsrCohortQueries
                        .getPatientsViralLoadWithin12MonthsForLabAndFsrDenominatorAndOnArtForMoreThan3Months(),
                    mappings)),
            mappings),
        "");

    addRow(
        dsd,
        "DLF",
        "Patients with Viral load gender and Age - Denominator based on Lab or FSR",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "viral load results on routine adults and children based on Lab or FSR",
                EptsReportUtils.map(
                    txPvlsBySourceLabOrFsrCohortQueries
                        .getPatientsViralLoadWithin12MonthsForLabAndFsrDenominatorAndOnArtForMoreThan3Months(),
                    mappings)),
            mappings),
        getAdultChildrenColumns());

    dsd.addColumn(
        "BD",
        "Breast feeding, have vl results and on ART more than 3 months Denominator with Lab or FSR",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "Breast feeding, have vl results and on ART more than 3 months Denominator with Lab or FSR",
                EptsReportUtils.map(
                    txPvlsBySourceLabOrFsrCohortQueries
                        .getBreastfeedingWomenWhoHaveViralLoadResultsForLabAndFsrDenominator(),
                    mappings)),
            mappings),
        "");

    dsd.addColumn(
        "PD",
        "Pregnant, have vl results and on ART more than 3 months Denominator with Lab or FSR",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "Pregnant, have vl results and on ART more than 3 months Denominator with Lab or FSR",
                EptsReportUtils.map(
                    txPvlsBySourceLabOrFsrCohortQueries
                        .getPregnantWomenWithViralLoadResultsForLabAndFsrDenominator(),
                    mappings)),
            mappings),
        "");

    //    Denominator Key Population for LAB and FSR
    addRow(
        dsd,
        "KPD",
        "Key population patients and are on routine and target Denominator with Lab or FSR",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "Key population patients and are on routine and target Denominator with Lab or FSR",
                EptsReportUtils.map(
                    txPvlsBySourceLabOrFsrCohortQueries
                        .getPatientsViralLoadWithin12MonthsForLabAndFsrDenominatorAndOnArtForMoreThan3Months(),
                    mappings)),
            mappings),
        getKpColumns());
  }

  private void addNumeratorColumnsForLabAndFsr(
      CohortIndicatorDataSetDefinition dsd, String mappings) {
    dsd.addColumn(
        "NTT",
        "Total patients with suppressed Viral load based on Lab or FSR - Numerator",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "suppressed viral load",
                EptsReportUtils.map(
                    txPvlsBySourceLabOrFsrCohortQueries
                        .getPatientsWithViralLoadSuppressionForLabAndFsrNumeratorWhoAreOnArtMoreThan3Months(),
                    mappings)),
            mappings),
        "");

    //  Lab and FSR Numerator Gender And Age
    addRow(
        dsd,
        "NLF",
        "Patients Gender and Age with suppressed Viral load based on Lab or FSR - Numerator",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "Patients Gender and Age with suppressed Viral load based on Lab or FSR - Numerator",
                EptsReportUtils.map(
                    txPvlsBySourceLabOrFsrCohortQueries
                        .getPatientsWithViralLoadSuppressionForLabAndFsrNumeratorWhoAreOnArtMoreThan3Months(),
                    mappings)),
            mappings),
        getAdultChildrenColumns());

    // Breastfeeding & Pregnant
    // Breastfeeding
    dsd.addColumn(
        "BN",
        "Breast feeding, have vl suppression and on ART more than 3 months numerator based on Lab or FSR",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "Breast feeding, have vl suppression and on ART more than 3 months numerator based on FSR",
                EptsReportUtils.map(
                    txPvlsBySourceLabOrFsrCohortQueries
                        .getBreastfeedingWomenWithViralSuppressionForLabAndFsrNumerator(),
                    mappings)),
            mappings),
        "");

    dsd.addColumn(
        "PN",
        "Pregnant patients, have vl suppression and on ART more than 3 months numerator based on Lab or FSR",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "Pregnant patients, have vl suppression and on ART more than 3 months numerator based on Lab or FSR",
                EptsReportUtils.map(
                    txPvlsBySourceLabOrFsrCohortQueries
                        .getPregnantWomenWithViralLoadSuppressionForLabAndFsrNumerator(),
                    mappings)),
            mappings),
        "");

    // Routine for Adults & Children Numerator KP
    addRow(
        dsd,
        "KPN",
        "Key population patients and are on routine and target Numerator based on Lab or FSR",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "Key population patients and are on routine and target Numerator based on Lab or FSR",
                EptsReportUtils.map(
                    txPvlsBySourceLabOrFsrCohortQueries
                        .getPatientsWithViralLoadSuppressionForLabAndFsrNumeratorWhoAreOnArtMoreThan3Months(),
                    mappings)),
            mappings),
        getKpColumns());
  }

  private void addDenominatorColumnsForClinicalForms(
      CohortIndicatorDataSetDefinition dsd, String mappings) {
    dsd.addColumn(
        "DTC",
        "Total patients with Viral load - Denominator based on Clinical forms",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "patients with viral load based on clinical forms",
                EptsReportUtils.map(
                    txPvlsBySourceClinicalOrFichaResumoCohortQueries
                        .getPatientsViralLoadWithin12MonthsForFichaMestreDenominatorAndOnArtForMoreThan3Months(),
                    mappings)),
            mappings),
        "");

    // Patients Gender and Age - Denominar based on Clinical Forms
    addRow(
        dsd,
        "DRC",
        "Patients with Viral load gender and Age - Denominator based on Clinical Forms",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "Patients with Viral load gender and Age - Denominator based on Clinical Forms",
                EptsReportUtils.map(
                    txPvlsBySourceClinicalOrFichaResumoCohortQueries
                        .getPatientsViralLoadWithin12MonthsForFichaMestreDenominatorAndOnArtForMoreThan3Months(),
                    mappings)),
            mappings),
        getAdultChildrenColumns());

    // Breastfeeding & Pregnant
    // Breastfeeding and on ART for more than 3 months and have VL results
    dsd.addColumn(
        "DBC",
        "Breast feeding, have vl results and on ART more than 3 months Denominator with Clinical forms",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "Breast feeding, have vl results and on ART more than 3 months Denominator with Clinical forms",
                EptsReportUtils.map(
                    txPvlsBySourceClinicalOrFichaResumoCohortQueries
                        .getBreastfeedingWomenWhoHaveViralLoadResultsForFichaMestreDenominator(),
                    mappings)),
            mappings),
        "");

    // Pregnant women on ART for more than 3 months and have VL results
    dsd.addColumn(
        "DPC",
        "Pregnant, have vl results and on ART more than 3 months Denominator with Clinical forms",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "Pregnant, have vl results and on ART more than 3 months Denominator with Clinical forms",
                EptsReportUtils.map(
                    txPvlsBySourceClinicalOrFichaResumoCohortQueries
                        .getPregnantWomenWithViralLoadResultsForFichaMestreDenominator(),
                    mappings)),
            mappings),
        "");

    // Denominator Key Population for Clinical Forms
    addRow(
        dsd,
        "KPDC",
        "Key population patients Denominator with Clinical forms",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "Key population patients Denominator with Clinical forms",
                EptsReportUtils.map(
                    txPvlsBySourceClinicalOrFichaResumoCohortQueries
                        .getPatientsViralLoadWithin12MonthsForFichaMestreDenominatorAndOnArtForMoreThan3Months(),
                    mappings)),
            mappings),
        getKpColumns());
  }

  private void addNumeratorColumnsForClinicalForms(
      CohortIndicatorDataSetDefinition dsd, String mappings) {
    dsd.addColumn(
        "NTC",
        "Total patients with suppressed Viral load based on Clinical forms- Numerator",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "suppressed viral load based on clinical forms",
                EptsReportUtils.map(
                    txPvlsBySourceClinicalOrFichaResumoCohortQueries
                        .getPatientsWithViralLoadSuppressionForFichaMestreNumeratorWhoAreOnArtMoreThan3Months(),
                    mappings)),
            mappings),
        "");

    //  Clinical Numerator Gender And Age
    addRow(
        dsd,
        "NLF",
        "Patients Gender and Age with suppressed Viral load based on Clinical Forms - Numerator",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "Patients Gender and Age with suppressed Viral load based on Clinical Forms - Numerator",
                EptsReportUtils.map(
                    txPvlsBySourceClinicalOrFichaResumoCohortQueries
                        .getPatientsWithViralLoadSuppressionForFichaMestreNumeratorWhoAreOnArtMoreThan3Months(),
                    mappings)),
            mappings),
        getAdultChildrenColumns());

    // Breastfeeding & Pregnant
    // Breastfeeding
    dsd.addColumn(
        "NBC",
        "Breast feeding, have vl suppression and on ART more than 3 months numerator based on Clinical forms",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "Breast feeding, have vl suppression and on ART more than 3 months numerator based on Clinical forms",
                EptsReportUtils.map(
                    txPvlsBySourceClinicalOrFichaResumoCohortQueries
                        .getBreastfeedingWomenWithViralSuppressionForFichaMestreNumerator(),
                    mappings)),
            mappings),
        "");

    // Pregnant
    dsd.addColumn(
        "NPC",
        "Pregnant patients, have vl suppression and on ART more than 3 months numerator based on Clinical form",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "Pregnant patients, have vl suppression and on ART more than 3 months numerator based on Clinical form",
                EptsReportUtils.map(
                    txPvlsBySourceClinicalOrFichaResumoCohortQueries
                        .getPregnantWomenWithViralLoadSuppressionForLabAndFsrNumerator(),
                    mappings)),
            mappings),
        "");

    // Routine for Adults & Children Numerator KP
    addRow(
        dsd,
        "NKPC",
        "Key population patients Numerator based on Clinical forms",
        EptsReportUtils.map(
            eptsGeneralIndicator.getIndicator(
                "Key population patients Numerator based on Clinical forms",
                EptsReportUtils.map(
                    txPvlsBySourceClinicalOrFichaResumoCohortQueries
                        .getPatientsWithViralLoadSuppressionForFichaMestreNumeratorWhoAreOnArtMoreThan3Months(),
                    mappings)),
            mappings),
        getKpColumns());
  }

  private List<ColumnParameters> getAdultChildrenColumns() {
    // Male
    ColumnParameters under1M =
        new ColumnParameters("under1M", "under 1 year male", "gender=M|age=<1", "01");
    ColumnParameters oneTo4M =
        new ColumnParameters("oneTo4M", "1 - 4 years male", "gender=M|age=1-4", "02");
    ColumnParameters fiveTo9M =
        new ColumnParameters("fiveTo9M", "5 - 9 years male", "gender=M|age=5-9", "03");
    ColumnParameters tenTo14M =
        new ColumnParameters("tenTo14M", "10 - 14 male", "gender=M|age=10-14", "04");
    ColumnParameters fifteenTo19M =
        new ColumnParameters("fifteenTo19M", "15 - 19 male", "gender=M|age=15-19", "05");
    ColumnParameters twentyTo24M =
        new ColumnParameters("twentyTo24M", "20 - 24 male", "gender=M|age=20-24", "06");
    ColumnParameters twenty5To29M =
        new ColumnParameters("twenty4To29M", "25 - 29 male", "gender=M|age=25-29", "07");
    ColumnParameters thirtyTo34M =
        new ColumnParameters("thirtyTo34M", "30 - 34 male", "gender=M|age=30-34", "08");
    ColumnParameters thirty5To39M =
        new ColumnParameters("thirty5To39M", "35 - 39 male", "gender=M|age=35-39", "09");
    ColumnParameters foutyTo44M =
        new ColumnParameters("foutyTo44M", "40 - 44 male", "gender=M|age=40-44", "10");
    ColumnParameters fouty5To49M =
        new ColumnParameters("fouty5To49M", "45 - 49 male", "gender=M|age=45-49", "11");
    ColumnParameters fiftyTo54M =
        new ColumnParameters("fiftyTo54M", "50 - 54 male", "gender=M|age=50-54", "12");
    ColumnParameters fifty5To59M =
        new ColumnParameters("fifty5To59M", "55 - 59 male", "gender=M|age=55-59", "13");
    ColumnParameters sixtyTo64M =
        new ColumnParameters("sixtyTo64M", "60 - 64 male", "gender=M|age=60-64", "14");
    ColumnParameters above65M =
        new ColumnParameters("above65M", "65+ male", "gender=M|age=65+", "15");
    ColumnParameters unknownM =
        new ColumnParameters("unknownM", "Unknown age male", "gender=M|age=UK", "16");

    // Female
    ColumnParameters under1F =
        new ColumnParameters("under1F", "under 1 year female", "gender=F|age=<1", "17");
    ColumnParameters oneTo4F =
        new ColumnParameters("oneTo4F", "1 - 4 years female", "gender=F|age=1-4", "18");
    ColumnParameters fiveTo9F =
        new ColumnParameters("fiveTo9F", "5 - 9 years female", "gender=F|age=5-9", "19");
    ColumnParameters tenTo14F =
        new ColumnParameters("tenTo14F", "10 - 14 female", "gender=F|age=10-14", "20");
    ColumnParameters fifteenTo19F =
        new ColumnParameters("fifteenTo19F", "15 - 19 female", "gender=F|age=15-19", "21");
    ColumnParameters twentyTo24F =
        new ColumnParameters("twentyTo24F", "20 - 24 female", "gender=F|age=20-24", "22");
    ColumnParameters twenty5To29F =
        new ColumnParameters("twenty4To29F", "25 - 29 female", "gender=F|age=25-29", "23");
    ColumnParameters thirtyTo34F =
        new ColumnParameters("thirtyTo34F", "30 - 34 female", "gender=F|age=30-34", "24");
    ColumnParameters thirty5To39F =
        new ColumnParameters("thirty5To39F", "35 - 39 female", "gender=F|age=35-39", "25");
    ColumnParameters foutyTo44F =
        new ColumnParameters("foutyTo44F", "40 - 44 female", "gender=F|age=40-44", "26");
    ColumnParameters fouty5To49F =
        new ColumnParameters("fouty5To49F", "45 - 49 female", "gender=F|age=45-49", "27");
    ColumnParameters fiftyTo54F =
        new ColumnParameters("fiftyTo54F", "50 - 54 female", "gender=F|age=50-54", "28");
    ColumnParameters fifty5To59F =
        new ColumnParameters("fifty5To59F", "55 - 59 female", "gender=F|age=55-59", "29");
    ColumnParameters sixtyTo64F =
        new ColumnParameters("sixtyTo64F", "60 - 64 female", "gender=F|age=60-64", "30");
    ColumnParameters above65F =
        new ColumnParameters("above65F", "65+ female", "gender=F|age=65+", "31");
    ColumnParameters unknownF =
        new ColumnParameters("unknownF", "Unknown age female", "gender=F|age=UK", "32");

    return Arrays.asList(
        unknownM,
        under1M,
        oneTo4M,
        fiveTo9M,
        tenTo14M,
        fifteenTo19M,
        twentyTo24M,
        twenty5To29M,
        thirtyTo34M,
        thirty5To39M,
        foutyTo44M,
        fouty5To49M,
        fiftyTo54M,
        fifty5To59M,
        sixtyTo64M,
        above65M,
        unknownF,
        under1F,
        oneTo4F,
        fiveTo9F,
        tenTo14F,
        fifteenTo19F,
        twentyTo24F,
        twenty5To29F,
        thirtyTo34F,
        thirty5To39F,
        foutyTo44F,
        fouty5To49F,
        fiftyTo54F,
        fifty5To59F,
        sixtyTo64F,
        above65F);
  }

  private List<ColumnParameters> getRoutineTargetedColumns() {
    ColumnParameters routine = new ColumnParameters("routine", "R", "rt=VLR", "01");
    ColumnParameters target = new ColumnParameters("target", "T", "rt=VLT", "02");
    return Arrays.asList(routine, target);
  }

  private List<ColumnParameters> getKpColumns() {
    // PID
    ColumnParameters pwid = new ColumnParameters("pwid", "PWID", "KP=PID", "01");

    // MSM
    ColumnParameters msm = new ColumnParameters("msm", "MSM", "KP=MSM", "02");

    // CSW
    ColumnParameters csw = new ColumnParameters("csw", "CSW", "KP=CSW", "03");

    // PRI
    ColumnParameters pri = new ColumnParameters("pri", "PRI", "KP=PRI", "04");

    return Arrays.asList(pwid, msm, csw, pri);
  }
}
