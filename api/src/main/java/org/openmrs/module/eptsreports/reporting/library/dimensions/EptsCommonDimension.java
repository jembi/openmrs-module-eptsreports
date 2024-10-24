/*
 * The contents of this file are subject to the OpenMRS Public License Version
 * 1.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 *
 * Copyright (C) OpenMRS, LLC. All Rights Reserved.
 */
package org.openmrs.module.eptsreports.reporting.library.dimensions;

import static org.openmrs.module.reporting.evaluation.parameter.Mapped.map;
import static org.openmrs.module.reporting.evaluation.parameter.Mapped.mapStraightThrough;

import java.util.Arrays;
import java.util.Date;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.library.cohorts.*;
import org.openmrs.module.eptsreports.reporting.library.cohorts.advancedhivillness.ResumoMensalDAHCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.queries.TbPrevQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InverseCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class EptsCommonDimension {

  private GenderCohortQueries genderCohortQueries;

  private TxNewCohortQueries txNewCohortQueries;

  private GenericCohortQueries genericCohortQueries;

  private Eri4MonthsCohortQueries eri4MonthsCohortQueries;

  private Eri2MonthsCohortQueries eri2MonthsCohortQueries;

  private EriCohortQueries eriCohortQueries;

  private TbPrevCohortQueries tbPrevCohortQueries;

  private HivCohortQueries hivCohortQueries;

  private TxPvlsCohortQueries txPvlsQueries;

  private TxCurrCohortQueries txCurrCohortQueries;

  private EriDSDCohortQueries eriDSDCohortQueries;

  private MISAUKeyPopsCohortQueries misauKeyPopsCohortQueries;

  private PrepCtCohortQueries prepCtCohortQueries;

  private TbPrevQueries tbPrevQueries;

  private HivMetadata hivMetadata;
  private CommonMetadata commonMetadata;

  private PrepNewCohortQueries prepNewCohortQueries;

  private TxPvlsBySourceLabOrFsrCohortQueries txPvlsBySourceLabOrFsrCohortQueries;

  private ResumoMensalDAHCohortQueries resumoMensalDAHCohortQueries;

  private IntensiveMonitoringCohortQueries intensiveMonitoringCohortQueries;

  private PmtctEidCohortQueries pmtctEidCohortQueries;
  private PmtctHeiCohortQueries pmtctHeiCohortQueries;

  @Autowired
  @Qualifier("commonAgeDimensionCohort")
  private AgeDimensionCohortInterface ageDimensionCohort;

  @Autowired
  public EptsCommonDimension(
      GenderCohortQueries genderCohortQueries,
      TxNewCohortQueries txNewCohortQueries,
      GenericCohortQueries genericCohortQueries,
      Eri4MonthsCohortQueries eri4MonthsCohortQueries,
      Eri2MonthsCohortQueries eri2MonthsCohortQueries,
      EriCohortQueries eriCohortQueries,
      TbPrevCohortQueries tbPrevCohortQueries,
      HivCohortQueries hivCohortQueries,
      TxPvlsCohortQueries txPvlsQueries,
      TxCurrCohortQueries txCurrCohortQueries,
      EriDSDCohortQueries eriDSDCohortQueries,
      MISAUKeyPopsCohortQueries misauKeyPopsCohortQueries,
      PrepCtCohortQueries prepCtCohortQueries,
      TbPrevQueries tbPrevQueries,
      HivMetadata hivMetadata,
      CommonMetadata commonMetadata,
      TxMlCohortQueries txMlCohortQueries,
      PrepNewCohortQueries prepNewCohortQueries,
      TxPvlsBySourceLabOrFsrCohortQueries txPvlsBySourceLabOrFsrCohortQueries,
      ResumoMensalDAHCohortQueries resumoMensalDAHCohortQueries,
      IntensiveMonitoringCohortQueries intensiveMonitoringCohortQueries,
      PmtctEidCohortQueries pmtctEidCohortQueries,
      PmtctHeiCohortQueries pmtctHeiCohortQueries) {
    this.genderCohortQueries = genderCohortQueries;
    this.txNewCohortQueries = txNewCohortQueries;
    this.genericCohortQueries = genericCohortQueries;
    this.eri4MonthsCohortQueries = eri4MonthsCohortQueries;
    this.eri2MonthsCohortQueries = eri2MonthsCohortQueries;
    this.eriCohortQueries = eriCohortQueries;
    this.tbPrevCohortQueries = tbPrevCohortQueries;
    this.hivCohortQueries = hivCohortQueries;
    this.txPvlsQueries = txPvlsQueries;
    this.txCurrCohortQueries = txCurrCohortQueries;
    this.eriDSDCohortQueries = eriDSDCohortQueries;
    this.misauKeyPopsCohortQueries = misauKeyPopsCohortQueries;
    this.prepCtCohortQueries = prepCtCohortQueries;
    this.tbPrevQueries = tbPrevQueries;
    this.hivMetadata = hivMetadata;
    this.commonMetadata = commonMetadata;
    this.prepNewCohortQueries = prepNewCohortQueries;
    this.txPvlsBySourceLabOrFsrCohortQueries = txPvlsBySourceLabOrFsrCohortQueries;
    this.resumoMensalDAHCohortQueries = resumoMensalDAHCohortQueries;
    this.intensiveMonitoringCohortQueries = intensiveMonitoringCohortQueries;
    this.pmtctEidCohortQueries = pmtctEidCohortQueries;
    this.pmtctHeiCohortQueries = pmtctHeiCohortQueries;
  }

  /**
   * Gender dimension
   *
   * @return the {@link org.openmrs.module.reporting.indicator.dimension.CohortDimension}
   */
  public CohortDefinitionDimension gender() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.setName("gender");
    dim.addCohortDefinition("M", EptsReportUtils.map(genderCohortQueries.maleCohort(), ""));
    dim.addCohortDefinition("F", EptsReportUtils.map(genderCohortQueries.femaleCohort(), ""));
    return dim;
  }

  /**
   * Age range dimension <1, 1-4, 5-9, 10-14, 15-19, 20-24,25-29,30-34,35-39,40-44,45-49, 50-54,
   * 55-59, 60-64, 65+
   *
   * @return {@link org.openmrs.module.reporting.indicator.dimension.CohortDimension}
   */
  public CohortDefinitionDimension age(AgeDimensionCohortInterface ageDimensionCohort) {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.setParameters(ageDimensionCohort.getParameters());
    dim.setName("age dimension");

    dim.addCohortDefinition(
        DimensionKeyForAge.unknown.getKey(), ageDimensionCohort.createUnknownAgeCohort());
    dim.addCohortDefinition(
        DimensionKeyForAge.bellowOneYear.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age below 1", 0, 0));
    dim.addCohortDefinition(
        DimensionKeyForAge.bellow2Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age below 2 years", 0, 1));
    dim.addCohortDefinition(
        DimensionKeyForAge.overOrEqualTo2Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age over or equal 2 years", 2, null));
    dim.addCohortDefinition(
        DimensionKeyForAge.betweenZeroAnd4Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age between 0 and 4 years", 0, 4));
    dim.addCohortDefinition(
        DimensionKeyForAge.betweenZeroAnd14Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age between 0 and 14 years", 0, 14));
    dim.addCohortDefinition(
        DimensionKeyForAge.betweenZeroAnd15Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age between 0 and 15 years", 0, 15));
    dim.addCohortDefinition(
        DimensionKeyForAge.betweenOneAnd4Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age between 1 and 4", 1, 4));
    dim.addCohortDefinition(
        DimensionKeyForAge.between2And4Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age between 2 and 4", 2, 4));
    dim.addCohortDefinition(
        DimensionKeyForAge.between5And9Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age between 5 and 9", 5, 9));
    dim.addCohortDefinition(
        DimensionKeyForAge.bellow10Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age below 10", null, 9));
    dim.addCohortDefinition(
        DimensionKeyForAge.bellow15Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age below 15", null, 14));
    dim.addCohortDefinition(
        DimensionKeyForAge.overOrEqualTo14Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age over 14", 14, null));
    dim.addCohortDefinition(
        DimensionKeyForAge.between10And14Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age between 10 and 14", 10, 14));
    dim.addCohortDefinition(
        DimensionKeyForAge.overOrEqualTo15Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age over 15", 15, null));
    dim.addCohortDefinition(
        DimensionKeyForAge.between15And19Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age between 15 and 19", 15, 19));
    dim.addCohortDefinition(
        DimensionKeyForAge.between15And24Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age between 15 and 24", 15, 24));

    dim.addCohortDefinition(
        DimensionKeyForAge.between20And24Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age between 20 and 24", 20, 24));
    dim.addCohortDefinition(
        DimensionKeyForAge.between25And29Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age between 25 and 29", 25, 29));
    dim.addCohortDefinition(
        DimensionKeyForAge.between25And49Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age between 25 and 49", 25, 49));

    dim.addCohortDefinition(
        DimensionKeyForAge.between30And34Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age between 30 and 34", 30, 34));
    dim.addCohortDefinition(
        DimensionKeyForAge.between35And39Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age between 35 and 39", 35, 39));
    dim.addCohortDefinition(
        DimensionKeyForAge.between40And44Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age between 40 and 44", 40, 44));
    dim.addCohortDefinition(
        DimensionKeyForAge.between45and49Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age between 45 and 49", 45, 49));
    dim.addCohortDefinition(
        DimensionKeyForAge.overOrEqualTo50Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age over 50", 50, null));

    dim.addCohortDefinition(
        DimensionKeyForAge.overOrEqualTo20Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age over 20 years", 20, null));

    dim.addCohortDefinition(
        DimensionKeyForAge.between10And19Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age between 10 and 19", 10, 19));
    dim.addCohortDefinition(
        DimensionKeyForAge.between2And14Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age between 2 and 14", 2, 14));
    dim.addCohortDefinition(
        DimensionKeyForAge.between1And14Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age between 1 and 14", 1, 14));
    dim.addCohortDefinition(
        DimensionKeyForAge.between2And9Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age between 2 and 9", 2, 9));

    dim.addCohortDefinition(
        DimensionKeyForAge.overOrEqualTo25Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age over 25 years", 25, null));
    // 50-54, 55-59, 60-64, 65+
    dim.addCohortDefinition(
        DimensionKeyForAge.between50And54Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort(
            "patients with age between 50 and 54 years", 50, 54));
    dim.addCohortDefinition(
        DimensionKeyForAge.between55And59Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort(
            "patients with age between 55 and 59 years", 55, 59));
    dim.addCohortDefinition(
        DimensionKeyForAge.between60And64Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort(
            "patients with age between 60 and 64 years", 60, 64));
    dim.addCohortDefinition(
        DimensionKeyForAge.overOrEqualTo65Years.getKey(),
        ageDimensionCohort.createXtoYAgeCohort("patients with age over 65 years", 65, null));

    return dim;
  }

  /** @return CohortDefinitionDimension */
  public CohortDefinitionDimension maternityDimension() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.addParameter(new Parameter("startDate", "Start Date", Date.class));
    dim.addParameter(new Parameter("endDate", "End Date", Date.class));
    dim.addParameter(new Parameter("location", "location", Location.class));
    dim.setName("Maternity Dimension");

    dim.addCohortDefinition(
        "breastfeeding",
        EptsReportUtils.map(
            txNewCohortQueries.getTxNewBreastfeedingComposition(false),
            "onOrAfter=${startDate},onOrBefore=${endDate},location=${location}"));
    dim.addCohortDefinition(
        "pregnant",
        EptsReportUtils.map(
            txNewCohortQueries.getPatientsPregnantEnrolledOnART(false),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    // PREGNANCY STATUS FOR RESUMO MENSAL DAH
    dim.addCohortDefinition(
        "pregnant-dah",
        EptsReportUtils.map(
            intensiveMonitoringCohortQueries.getMI15C(),
            "startDate=${startDate-3m},endDate=${endDate},location=${location}"));
    return dim;
  }

  /**
   * Get the dimensions based on the patient states for ERI-4 months
   *
   * @return CohortDefinitionDimension
   */
  public CohortDefinitionDimension getEri4MonthsDimension() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.addParameter(new Parameter("cohortStartDate", "Cohort Start Date", Date.class));
    dim.addParameter(new Parameter("cohortEndDate", "Cohort End Date", Date.class));
    dim.addParameter(new Parameter("reportingStartDate", "Report Start Date", Date.class));
    dim.addParameter(new Parameter("reportingEndDate", "Report End Date", Date.class));
    dim.addParameter(new Parameter("location", "location", Location.class));
    dim.setName("Get patient states");

    dim.addCohortDefinition(
        "IART",
        EptsReportUtils.map(
            eriCohortQueries.getAllPatientsWhoInitiatedArt(),
            "cohortStartDate=${cohortStartDate},cohortEndDate=${cohortEndDate},reportingEndDate=${reportingEndDate},location=${location}"));

    dim.addCohortDefinition(
        "AIT",
        EptsReportUtils.map(
            eri4MonthsCohortQueries.getPatientsWhoAreAliveAndNotTransferredOutAndOnTreatment(),
            "cohortStartDate=${cohortStartDate},cohortEndDate=${cohortEndDate},reportingEndDate=${reportingEndDate},location=${location}"));

    dim.addCohortDefinition(
        "DP",
        EptsReportUtils.map(
            genericCohortQueries.getDeceasedPatients(),
            "onOrBefore=${reportingEndDate},location=${location}"));

    dim.addCohortDefinition(
        "LTFU",
        EptsReportUtils.map(
            eri4MonthsCohortQueries
                .getPatientsLostToFollowUpAndNotDeadTransferredOrStoppedTreatment(),
            "onOrBefore=${reportingEndDate},location=${location}"));

    dim.addCohortDefinition(
        "TOP",
        EptsReportUtils.map(
            hivCohortQueries.getPatientsTransferredOut(),
            "onOrBefore=${reportingEndDate},location=${location}"));

    dim.addCohortDefinition(
        "STP",
        EptsReportUtils.map(
            hivCohortQueries.getPatientsWhoStoppedTreatment(),
            "onOrBefore=${reportingEndDate},location=${location}"));

    dim.addCohortDefinition(
        "ANIT",
        EptsReportUtils.map(
            eri4MonthsCohortQueries.getPatientsWhoAreAliveAndNotTransferredOutAndDefaulter(),
            "cohortStartDate=${cohortStartDate},cohortEndDate=${cohortEndDate},reportingEndDate=${reportingEndDate},location=${location}"));
    return dim;
  }

  /**
   * Get the dimensions based on the patient states for ERI-4 months
   *
   * @return CohortDefinitionDimension
   */
  public CohortDefinitionDimension getEri2MonthsDimension() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.addParameter(new Parameter("cohortStartDate", "Cohort Start Date", Date.class));
    dim.addParameter(new Parameter("cohortEndDate", "Cohort End Date", Date.class));
    dim.addParameter(new Parameter("reportingEndDate", "Reporting End Date", Date.class));
    dim.addParameter(new Parameter("location", "location", Location.class));
    dim.setName("Get patients dimensions for Eri2Months");
    dim.addCohortDefinition(
        "IART",
        EptsReportUtils.map(
            eriCohortQueries.getAllPatientsWhoInitiatedArt(),
            "cohortStartDate=${cohortStartDate},cohortEndDate=${cohortEndDate},reportingEndDate=${reportingEndDate},location=${location}"));
    dim.addCohortDefinition(
        "DNPUD",
        EptsReportUtils.map(
            eri2MonthsCohortQueries
                .getPatientsWhoAreAliveAndNotTransferredOutAndDidNotPickUpDrugsBetween5to33DaysAfterArtStartDate(),
            "cohortStartDate=${cohortStartDate},cohortEndDate=${cohortEndDate},reportingEndDate=${reportingEndDate},location=${location}"));
    dim.addCohortDefinition(
        "PUD",
        EptsReportUtils.map(
            eri2MonthsCohortQueries
                .getPatientsWhoAreAliveAndNoteTransferredOutAndPickedUpDrugsBetween5to33DaysAfterArtStartDate(),
            "cohortStartDate=${cohortStartDate},cohortEndDate=${cohortEndDate},reportingEndDate=${reportingEndDate},location=${location}"));
    dim.addCohortDefinition(
        "DP",
        EptsReportUtils.map(
            genericCohortQueries.getDeceasedPatients(),
            "onOrBefore=${reportingEndDate},location=${location}"));
    dim.addCohortDefinition(
        "TOP",
        EptsReportUtils.map(
            hivCohortQueries.getPatientsTransferredOut(),
            "onOrBefore=${reportingEndDate},location=${location}"));
    dim.addCohortDefinition(
        "STP",
        EptsReportUtils.map(
            hivCohortQueries.getPatientsWhoStoppedTreatment(),
            "onOrBefore=${reportingEndDate},location=${location}"));
    return dim;
  }

  public CohortDefinitionDimension getArtStatusDimension() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    dim.addParameter(new Parameter("onOrBefore", "orOrBefore", Date.class));
    dim.addParameter(new Parameter("location", "Location", Location.class));
    dim.setName("ART-status Dimension");
    dim.addCohortDefinition(
        "new-on-art",
        EptsReportUtils.map(
            tbPrevQueries.getPatientsWhoStartedTptAndNewOnArt(),
            "startDate=${onOrAfter},endDate=${onOrBefore},location=${location}"));
    dim.addCohortDefinition(
        "previously-on-art",
        EptsReportUtils.map(
            tbPrevQueries.getPatientsWhoStartedTptPreviouslyOnArt(),
            "startDate=${onOrAfter},endDate=${onOrBefore},location=${location}"));

    // ART STATUS FOR RESUMO MENSAL DAH
    dim.addCohortDefinition(
        "new-art-dah",
        EptsReportUtils.map(
            resumoMensalDAHCohortQueries.getPatientsWhoAreNewInArtDisaggregation(false),
            "startDate=${onOrAfter},endDate=${onOrBefore},location=${location}"));

    dim.addCohortDefinition(
        "restart-art-dah",
        EptsReportUtils.map(
            resumoMensalDAHCohortQueries.getPatientsWhoRestartedArtDisaggregation(false),
            "startDate=${onOrAfter},endDate=${onOrBefore},location=${location}"));

    dim.addCohortDefinition(
        "on-art-dah",
        EptsReportUtils.map(
            resumoMensalDAHCohortQueries.getPatientsWhoAreInTarvDisaggregation(false),
            "startDate=${onOrAfter},endDate=${onOrBefore},location=${location}"));

    // ONLY FOR I8 AND I9
    dim.addCohortDefinition(
        "new-art-dah-89",
        EptsReportUtils.map(
            resumoMensalDAHCohortQueries.getPatientsWhoAreNewInArtDisaggregation(true),
            "startDate=${onOrAfter},endDate=${onOrBefore},location=${location}"));

    dim.addCohortDefinition(
        "restart-art-dah-89",
        EptsReportUtils.map(
            resumoMensalDAHCohortQueries.getPatientsWhoRestartedArtDisaggregation(true),
            "startDate=${onOrAfter},endDate=${onOrBefore},location=${location}"));

    dim.addCohortDefinition(
        "on-art-dah-89",
        EptsReportUtils.map(
            resumoMensalDAHCohortQueries.getPatientsWhoAreInTarvDisaggregation(true),
            "startDate=${onOrAfter},endDate=${onOrBefore},location=${location}"));
    return dim;
  }

  /**
   * <b>Description</b> Disaggregation for Key population
   *
   * @return CohortDefinitionDimension
   */
  public CohortDefinitionDimension getKeyPopsDimensionForPrep() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.setName("Key Population Dimension for Prep indicators");
    dim.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    dim.addParameter(new Parameter("onOrBefore", "orOrBefore", Date.class));
    dim.addParameter(new Parameter("location", "Location", Location.class));
    CohortDefinition drugUserKeyPopCohort =
        prepCtCohortQueries.getPatientsWhoAreKeypopulation(
            Arrays.asList(hivMetadata.getDrugUseConcept()));
    CohortDefinition homosexualKeyPopCohort = prepCtCohortQueries.getPatientsWhoAreHomosexual();
    CohortDefinition imprisonmentKeyPopCohort = prepCtCohortQueries.getPatientsWhoArePrisoner();
    CohortDefinition transgenderKeyPopCohort = prepCtCohortQueries.getPatientsWhoAreTransgender();
    CohortDefinition femaleSexWorkersKeyPopCohort =
        prepCtCohortQueries.getFemalePatientsWhoAreSexWorker();
    CohortDefinition maleSexWorkersKeyPopCohort =
        prepCtCohortQueries.getMalePatientsWhoAreSexWorker();
    CohortDefinition outroKeyPopCohort = prepCtCohortQueries.getPatientsWhoAreOutro();
    dim.addCohortDefinition("PID", mapStraightThrough(drugUserKeyPopCohort));
    dim.addCohortDefinition("MSM", mapStraightThrough(homosexualKeyPopCohort));
    dim.addCohortDefinition("PRI", mapStraightThrough(imprisonmentKeyPopCohort));
    dim.addCohortDefinition("TG", mapStraightThrough(transgenderKeyPopCohort));
    dim.addCohortDefinition("SW", mapStraightThrough(femaleSexWorkersKeyPopCohort));
    dim.addCohortDefinition("OUT", mapStraightThrough(outroKeyPopCohort));
    dim.addCohortDefinition("MSW", mapStraightThrough(maleSexWorkersKeyPopCohort));

    return dim;
  }

  public CohortDefinitionDimension getKeyPopsDimension() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.setName("Key Population Dimension");
    dim.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    dim.addParameter(new Parameter("onOrBefore", "orOrBefore", Date.class));
    dim.addParameter(new Parameter("location", "Location", Location.class));
    String mappings = "endDate=${onOrBefore},location=${location}";
    CohortDefinition drugUserKeyPopCohort = hivCohortQueries.getDrugUserKeyPopCohort();
    CohortDefinition homosexualKeyPopCohort = hivCohortQueries.getMaleHomosexualKeyPopDefinition();
    CohortDefinition imprisonmentKeyPopCohort = hivCohortQueries.getImprisonmentKeyPopCohort();
    CohortDefinition femaleSexWorkerKeyPopCohort =
        hivCohortQueries.getFemaleSexWorkersKeyPopCohortDefinition();
    CohortDefinition maleSexWorkerKeyPopCohort =
        hivCohortQueries.getMaleSexWorkersKeyPopCohortDefinition();
    CohortDefinition transgenderKeyPopCohort =
        hivCohortQueries.getTransgenderKeyPopCohortDefinition();
    CohortDefinition sexWorkersKeyPopCohort = hivCohortQueries.getSexWorkerKeyPopCohortDefinition();
    dim.addCohortDefinition("PID", map(drugUserKeyPopCohort, mappings));
    dim.addCohortDefinition("MSM", map(homosexualKeyPopCohort, mappings));
    dim.addCohortDefinition("CSW", map(femaleSexWorkerKeyPopCohort, mappings));
    dim.addCohortDefinition("PRI", map(imprisonmentKeyPopCohort, mappings));
    dim.addCohortDefinition("MSW", map(maleSexWorkerKeyPopCohort, mappings));
    dim.addCohortDefinition("TG", map(transgenderKeyPopCohort, mappings));
    dim.addCohortDefinition("SW", map(sexWorkersKeyPopCohort, mappings));
    return dim;
  }

  public CohortDefinitionDimension getViralLoadRoutineTargetReasonsDimension() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.addParameter(new Parameter("startDate", "onOrAfter", Date.class));
    dim.addParameter(new Parameter("endDate", "onOrBefore", Date.class));
    dim.addParameter(new Parameter("location", "Location", Location.class));
    CohortDefinition routineViralLoadCohort = txPvlsQueries.getPatientsWhoAreOnRoutine();
    CohortDefinition targetedViralLoadCohort = txPvlsQueries.getPatientsWhoAreOnTarget();
    dim.addCohortDefinition("VLR", mapStraightThrough(routineViralLoadCohort));
    dim.addCohortDefinition("VLT", mapStraightThrough(targetedViralLoadCohort));
    return dim;
  }

  public CohortDefinitionDimension getViralLoadRoutineTargetReasonsBySourceDimension() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.addParameter(new Parameter("startDate", "onOrAfter", Date.class));
    dim.addParameter(new Parameter("endDate", "onOrBefore", Date.class));
    dim.addParameter(new Parameter("location", "Location", Location.class));
    CohortDefinition routineViralLoadCohort =
        txPvlsQueries.getPatientsWhoAreOnRoutineOnMasterCardAndClinicalEncounter();
    CohortDefinition targetedViralLoadCohort = txPvlsQueries.getPatientsWhoAreOnTargetBySource();
    dim.addCohortDefinition("VLR", mapStraightThrough(routineViralLoadCohort));
    dim.addCohortDefinition("VLT", mapStraightThrough(targetedViralLoadCohort));
    return dim;
  }

  public CohortDefinitionDimension getDispensingQuantityDimension() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.setName("ARV Dispensing quantity dimension");
    dim.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    dim.addParameter(new Parameter("onOrBefore", "orOrBefore", Date.class));
    dim.addParameter(new Parameter("locationList", "Location", Location.class));
    CohortDefinition less3m = txCurrCohortQueries.lessThan3MonthsDispensationComposition();
    CohortDefinition threeTo5m = txCurrCohortQueries.quarterlyDispensationComposition();
    CohortDefinition more6m = txCurrCohortQueries.semiAnnualDispensationComposition();
    dim.addCohortDefinition(
        "<3m",
        EptsReportUtils.map(
            less3m, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${locationList}"));
    dim.addCohortDefinition(
        "3-5m",
        EptsReportUtils.map(
            threeTo5m, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${locationList}"));
    dim.addCohortDefinition(
        ">6m",
        EptsReportUtils.map(
            more6m, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${locationList}"));
    return dim;
  }

  /** Dimension for DSD eligible and not eligible patients */
  public CohortDefinitionDimension getDSDEligibleDimension() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.setName("DSD Eligible dimension");
    dim.addParameter(new Parameter("startDate", "Start Date", Date.class));
    dim.addParameter(new Parameter("endDate", "End Date", Date.class));
    dim.addParameter(new Parameter("location", "Location", Location.class));
    CohortDefinition eligible = eriDSDCohortQueries.getD1();
    CohortDefinition eligibleD4 = eriDSDCohortQueries.getD4();
    CohortDefinition notEligible = eriDSDCohortQueries.getD2();
    CohortDefinition notEligibleD4 = eriDSDCohortQueries.getD3NotD4();

    dim.addCohortDefinition("E", mapStraightThrough(eligible));
    dim.addCohortDefinition("ED4", mapStraightThrough(eligibleD4));
    dim.addCohortDefinition("NE", mapStraightThrough(notEligible));
    dim.addCohortDefinition("NED4", mapStraightThrough(notEligibleD4));
    return dim;
  }

  /** Dimension for DSD Non-Pregnant, Non-Breastfeeding and Not on TB treatment */
  public CohortDefinitionDimension getDSDNonPregnantNonBreastfeedingAndNotOnTbDimension() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.setName("DSD Non-Pregnant, Non-Breastfeeding and Non-Tb dimension");
    dim.addParameter(new Parameter("endDate", "End Date", Date.class));
    dim.addParameter(new Parameter("location", "Location", Location.class));
    CohortDefinition pregnantBreastfeedingTb =
        eriDSDCohortQueries.getPregnantAndBreastfeedingAndOnTBTreatment();
    CohortDefinition pregnantBreastfeeding = eriDSDCohortQueries.getPregnantAndBreastfeeding();
    CohortDefinition pregnant = eriDSDCohortQueries.getDSDPregnant();
    CohortDefinition breastfeeding = eriDSDCohortQueries.getDSDBreastfeeding();
    CohortDefinition inverse = new InverseCohortDefinition(pregnantBreastfeedingTb);
    CohortDefinition inversePB = new InverseCohortDefinition(pregnantBreastfeeding);
    dim.addCohortDefinition("NPNBNTB", mapStraightThrough(inverse));
    dim.addCohortDefinition("NPNB", mapStraightThrough(inversePB));
    dim.addCohortDefinition("P", mapStraightThrough(pregnant));
    dim.addCohortDefinition("B", mapStraightThrough(breastfeeding));
    return dim;
  }

  /** Dimension for Age in months */
  public CohortDefinitionDimension ageInMonths() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.setName("Patients having age in months");
    dim.addParameter(new Parameter("effectiveDate", "End Date", Date.class));
    dim.addCohortDefinition(
        "9m-",
        EptsReportUtils.map(
            genericCohortQueries.getAgeInMonths(0, 9), "effectiveDate=${effectiveDate}"));
    return dim;
  }

  /** Dimension for Age in months */
  public CohortDefinitionDimension ageInMonthsBasedOnArt() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.setName("Patients having age in months");
    dim.addParameter(new Parameter("startDate", "Start  Date", Date.class));
    dim.addParameter(new Parameter("endDate", "End  Date", Date.class));
    dim.addParameter(new Parameter("location", "Location", Location.class));
    dim.addCohortDefinition(
        "10m-",
        EptsReportUtils.map(
            genericCohortQueries
                .getAgeInMonthsBasedOnArtStartDateIgualGreaterThanLoweBoundAndLessThanUpperBound(
                    0, 10),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    return dim;
  }
  /**
   * Dimension for returning patients age based on their ART start date
   *
   * @return @{@link CohortDefinitionDimension}
   */
  public CohortDefinitionDimension ageBasedOnArtStartDateMOH() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    dim.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    dim.addParameter(new Parameter("location", "location", Location.class));
    dim.setName("Patients having age based on ART start date by reporting end date MOH");
    dim.addCohortDefinition(
        "adultsArt",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnMOHArtStartDate(15, null, false),
            "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));
    dim.addCohortDefinition(
        "childrenArt",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnMOHArtStartDate(null, 14, false),
            "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));
    dim.addCohortDefinition(
        "2-14",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnMOHArtStartDate(2, 14, false),
            "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));
    dim.addCohortDefinition(
        "<2",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnMOHArtStartDate(null, 1, false),
            "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));
    dim.addCohortDefinition(
        "1-14",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnMOHArtStartDate(1, 14, false),
            "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    dim.addCohortDefinition(
        "8-9",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnMOHArtStartDate(8, 9, false),
            "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    dim.addCohortDefinition(
        "10-14",
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnMOHArtStartDate(10, 14, false),
            "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    return dim;
  }

  /**
   * Dimension for returning patients age based on the first viral load date
   *
   * @return @{@link CohortDefinitionDimension}
   */
  public CohortDefinitionDimension getPatientAgeBasedOnFirstViralLoadDate() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.addParameter(new Parameter("startDate", "startDate", Date.class));
    dim.addParameter(new Parameter("endDate", "endDate", Date.class));
    dim.addParameter(new Parameter("location", "location", Location.class));
    dim.setName("Patients having age based on first viral load date by reporting end date MOH");
    dim.addCohortDefinition(
        "MqAdults",
        EptsReportUtils.map(
            genericCohortQueries.getPatientAgeBasedOnFirstViralLoadDate(15, 200),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    dim.addCohortDefinition(
        "MqChildren",
        EptsReportUtils.map(
            genericCohortQueries.getPatientAgeBasedOnFirstViralLoadDate(0, 14),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    return dim;
  }

  /**
   * Dimension for returning KP patients in combined categories
   *
   * @return @{@link CohortDefinitionDimension}
   */
  public CohortDefinitionDimension getKpPatientsInCombinedCategories() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.addParameter(new Parameter("startDate", "startDate", Date.class));
    dim.addParameter(new Parameter("endDate", "endDate", Date.class));
    dim.addParameter(new Parameter("location", "location", Location.class));
    dim.setName("Patients appearing in both KP categories");
    dim.addCohortDefinition(
        "PIDeHSH",
        EptsReportUtils.map(
            misauKeyPopsCohortQueries.getPidAndHsh(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    dim.addCohortDefinition(
        "PIDeMTS",
        EptsReportUtils.map(
            misauKeyPopsCohortQueries.getPidAndMts(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    dim.addCohortDefinition(
        "PIDeREC",
        EptsReportUtils.map(
            misauKeyPopsCohortQueries.getPidAndRec(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    dim.addCohortDefinition(
        "HSHeREC",
        EptsReportUtils.map(
            misauKeyPopsCohortQueries.getHshAndRec(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    dim.addCohortDefinition(
        "MTSeREC",
        EptsReportUtils.map(
            misauKeyPopsCohortQueries.getMtsAndRec(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    return dim;
  }

  /**
   * Dimension to calculate patient age based on the PrEP Start Date
   *
   * @return @{@link CohortDefinitionDimension}
   */
  public CohortDefinitionDimension getPatientAgeBasedOnPrepStartDate() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.addParameter(new Parameter("startDate", "startDate", Date.class));
    dim.addParameter(new Parameter("endDate", "endDate", Date.class));
    dim.addParameter(new Parameter("location", "location", Location.class));
    dim.setName("Patients having age based on PrEP Start Date");

    dim.addCohortDefinition(
        DimensionKeyForAge.unknown.getKey(), ageDimensionCohort.createUnknownAgeCohort());
    dim.addCohortDefinition(
        DimensionKeyForAge.between15And19Years.getKey(),
        EptsReportUtils.map(
            genericCohortQueries.getPatientAgeBasedOnPrepStartDate(15, 19),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    dim.addCohortDefinition(
        DimensionKeyForAge.between20And24Years.getKey(),
        EptsReportUtils.map(
            genericCohortQueries.getPatientAgeBasedOnPrepStartDate(20, 24),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    dim.addCohortDefinition(
        DimensionKeyForAge.between25And29Years.getKey(),
        EptsReportUtils.map(
            genericCohortQueries.getPatientAgeBasedOnPrepStartDate(25, 29),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    dim.addCohortDefinition(
        DimensionKeyForAge.between30And34Years.getKey(),
        EptsReportUtils.map(
            genericCohortQueries.getPatientAgeBasedOnPrepStartDate(30, 34),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    dim.addCohortDefinition(
        DimensionKeyForAge.between35And39Years.getKey(),
        EptsReportUtils.map(
            genericCohortQueries.getPatientAgeBasedOnPrepStartDate(35, 39),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    dim.addCohortDefinition(
        DimensionKeyForAge.between40And44Years.getKey(),
        EptsReportUtils.map(
            genericCohortQueries.getPatientAgeBasedOnPrepStartDate(40, 44),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    dim.addCohortDefinition(
        DimensionKeyForAge.between45and49Years.getKey(),
        EptsReportUtils.map(
            genericCohortQueries.getPatientAgeBasedOnPrepStartDate(45, 49),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    dim.addCohortDefinition(
        DimensionKeyForAge.overOrEqualTo50Years.getKey(),
        EptsReportUtils.map(
            genericCohortQueries.getPatientAgeBasedOnPrepStartDate(50, 200),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    dim.addCohortDefinition(
        DimensionKeyForAge.overOrEqualTo15Years.getKey(),
        EptsReportUtils.map(
            genericCohortQueries.getPatientAgeBasedOnPrepStartDate(15, 200),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    return dim;
  }

  /**
   * Dimension for returning patients Test Results (Positive, Negative and other) based on reporting
   * period Prep
   *
   * @return @{@link CohortDefinitionDimension}
   */
  public CohortDefinitionDimension getPatientTestResultsPrep() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.addParameter(new Parameter("startDate", "startDate", Date.class));
    dim.addParameter(new Parameter("endDate", "endDate", Date.class));
    dim.addParameter(new Parameter("location", "location", Location.class));
    dim.setName("Patients having Positive Test Results Prep based on reporting period");
    dim.addCohortDefinition(
        "positive",
        EptsReportUtils.map(
            prepCtCohortQueries.getPositiveTestResults(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    dim.addCohortDefinition(
        "negative",
        EptsReportUtils.map(
            prepCtCohortQueries.getNegativeTestResults(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    dim.addCohortDefinition(
        "other",
        EptsReportUtils.map(
            prepCtCohortQueries.getOtherTestResults(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    return dim;
  }

  /**
   * Dimension to calculate patient age based on the PrEP End Date
   *
   * @return @{@link CohortDefinitionDimension}
   */
  public CohortDefinitionDimension getPatientAgeBasedOnPrepEndDate() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.addParameter(new Parameter("endDate", "endDate", Date.class));
    dim.addParameter(new Parameter("location", "location", Location.class));
    dim.setName("Patients having age based on PrEP End Date");

    dim.addCohortDefinition(
        DimensionKeyForAge.unknown.getKey(), ageDimensionCohort.createUnknownAgeCohort());
    dim.addCohortDefinition(
        DimensionKeyForAge.between15And19Years.getKey(),
        EptsReportUtils.map(
            genericCohortQueries.getPatientAgeBasedOnPrepEndDate(15, 19),
            "endDate=${endDate},location=${location}"));
    dim.addCohortDefinition(
        DimensionKeyForAge.between20And24Years.getKey(),
        EptsReportUtils.map(
            genericCohortQueries.getPatientAgeBasedOnPrepEndDate(20, 24),
            "endDate=${endDate},location=${location}"));
    dim.addCohortDefinition(
        DimensionKeyForAge.between25And29Years.getKey(),
        EptsReportUtils.map(
            genericCohortQueries.getPatientAgeBasedOnPrepEndDate(25, 29),
            "endDate=${endDate},location=${location}"));
    dim.addCohortDefinition(
        DimensionKeyForAge.between30And34Years.getKey(),
        EptsReportUtils.map(
            genericCohortQueries.getPatientAgeBasedOnPrepEndDate(30, 34),
            "endDate=${endDate},location=${location}"));
    dim.addCohortDefinition(
        DimensionKeyForAge.between35And39Years.getKey(),
        EptsReportUtils.map(
            genericCohortQueries.getPatientAgeBasedOnPrepEndDate(35, 39),
            "endDate=${endDate},location=${location}"));
    dim.addCohortDefinition(
        DimensionKeyForAge.between40And44Years.getKey(),
        EptsReportUtils.map(
            genericCohortQueries.getPatientAgeBasedOnPrepEndDate(40, 44),
            "endDate=${endDate},location=${location}"));
    dim.addCohortDefinition(
        DimensionKeyForAge.between45and49Years.getKey(),
        EptsReportUtils.map(
            genericCohortQueries.getPatientAgeBasedOnPrepEndDate(45, 49),
            "endDate=${endDate},location=${location}"));
    dim.addCohortDefinition(
        DimensionKeyForAge.overOrEqualTo50Years.getKey(),
        EptsReportUtils.map(
            genericCohortQueries.getPatientAgeBasedOnPrepEndDate(50, 200),
            "endDate=${endDate},location=${location}"));

    dim.addCohortDefinition(
        DimensionKeyForAge.overOrEqualTo15Years.getKey(),
        EptsReportUtils.map(
            genericCohortQueries.getPatientAgeBasedOnPrepEndDate(15, 200),
            "endDate=${endDate},location=${location}"));

    return dim;
  }

  /**
   * Dimension for returning patients Maternity (Pregnant, breastfeeding) based on Prep
   *
   * @return @{@link CohortDefinitionDimension}
   */
  public CohortDefinitionDimension getPregnantAndBreastfeedingPatientsBasedOnPrep() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.addParameter(new Parameter("startDate", "startDate", Date.class));
    dim.addParameter(new Parameter("endDate", "endDate", Date.class));
    dim.addParameter(new Parameter("location", "location", Location.class));
    dim.setName("Patients Pregnant or Breastfeeding based on Prep");
    dim.addCohortDefinition(
        "pregnant",
        EptsReportUtils.map(
            genericCohortQueries.getPregnantPatientsBasedOnPrep(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    dim.addCohortDefinition(
        "breastfeeding",
        EptsReportUtils.map(
            genericCohortQueries.getBreastfeedingPatientsBasedOnPrep(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    return dim;
  }

  /**
   * Dimension to calculate patient age based on the Report End Date
   *
   * @return @{@link CohortDefinitionDimension}
   */
  public CohortDefinitionDimension getPatientAgeOnReportEndDate() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    dim.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    dim.addParameter(new Parameter("location", "location", Location.class));
    dim.setName("Patients having age based on Report End Date");

    dim.addCohortDefinition(
        DimensionKeyForAge.between10And14Years.getKey(),
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnReportEndDate(10, 14),
            "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    dim.addCohortDefinition(
        DimensionKeyForAge.between15And19Years.getKey(),
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnReportEndDate(15, 19),
            "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    dim.addCohortDefinition(
        DimensionKeyForAge.between20And24Years.getKey(),
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnReportEndDate(20, 24),
            "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    dim.addCohortDefinition(
        DimensionKeyForAge.overOrEqualTo25Years.getKey(),
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnReportEndDate(25, 200),
            "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    dim.addCohortDefinition(
        DimensionKeyForAge.overOrEqualTo15Years.getKey(),
        EptsReportUtils.map(
            genericCohortQueries.getAgeOnReportEndDate(15, 200),
            "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));
    return dim;
  }

  /**
   * <b>Description</b> Disaggregation for Patients Marked as Target Group
   *
   * @return {@link CohortDefinitionDimension}
   */
  public CohortDefinitionDimension getTargetGroupDimension() {

    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.setName("Target Group Dimension");

    dim.addParameter(new Parameter("onOrBefore", "orOrBefore", Date.class));
    dim.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    dim.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition adolescentsAndYouthTargetGroupCohort =
        prepCtCohortQueries.getPatientsWhoAreAdolescentAndYouth();
    CohortDefinition pregnantWomanTargetGroupCohort =
        prepCtCohortQueries.getPatientsWhoArePregnant();
    CohortDefinition breastfeedingWomanTargetGroupCohort =
        prepCtCohortQueries.getPatientsWhoAreBreastfeeding();
    CohortDefinition militaryTargetGroupCohort = prepCtCohortQueries.getPatientsWhoAreMilitary();
    CohortDefinition minerTargetGroupCohort = prepCtCohortQueries.getPatientsWhoAreMiners();
    CohortDefinition truckDriverTargetGroupCohort =
        prepCtCohortQueries.getPatientsWhoAreTruckDrivers();
    CohortDefinition serodiscordantCouplesTargetGroupCohort =
        prepCtCohortQueries.getPatientsWhoAreSerodiscordantCouples();

    dim.addCohortDefinition("AYR", mapStraightThrough(adolescentsAndYouthTargetGroupCohort));
    dim.addCohortDefinition("PW", mapStraightThrough(pregnantWomanTargetGroupCohort));
    dim.addCohortDefinition("BW", mapStraightThrough(breastfeedingWomanTargetGroupCohort));
    dim.addCohortDefinition("MIL", mapStraightThrough(militaryTargetGroupCohort));
    dim.addCohortDefinition("MIN", mapStraightThrough(minerTargetGroupCohort));
    dim.addCohortDefinition("TD", mapStraightThrough(truckDriverTargetGroupCohort));
    dim.addCohortDefinition("CS", mapStraightThrough(serodiscordantCouplesTargetGroupCohort));

    return dim;
  }

  /**
   * <b>Description</b> Disaggregation for Patients Marked as Target Group That are not in Key
   * Population
   */
  public CohortDefinition extractOnlyTargetGroup(CohortDefinition targetGroup) {
    CompositionCohortDefinition cd = new CompositionCohortDefinition();
    cd.setName("Target Group");
    cd.addParameter(new Parameter("onOrBefore", "Start Date", Date.class));
    cd.addParameter(new Parameter("onOrAfter", "end Date", Date.class));
    cd.addParameter(new Parameter("location", "Location", Location.class));

    CohortDefinition KeyPop = prepCtCohortQueries.getkeypop();

    cd.addSearch(
        "TargetGroup",
        EptsReportUtils.map(
            targetGroup, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.addSearch(
        "keyPop",
        EptsReportUtils.map(
            KeyPop, "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},location=${location}"));

    cd.setCompositionString("TargetGroup AND NOT keyPop");

    return cd;
  }

  /**
   * <b>Patients disaggregation – routine</b>
   *
   * <p>The system will identify patients with routine type of VL test as following:
   *
   * <ul>
   *   <li>all patients who have the most recent VL test result registered on Laboratory or FSR
   *       (with the Reason for requesting viral load as routine viral load or UNKNOWN) Forms within
   *       past 12 months.
   * </ul>
   *
   * @return {@link CohortDefinitionDimension}
   */
  public CohortDefinitionDimension getViralLoadRoutineTargetReasonsDimensionForPvlsBySource() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.setName("Patients disaggregation – routine");
    dim.addParameter(new Parameter("startDate", "onOrAfter", Date.class));
    dim.addParameter(new Parameter("endDate", "onOrBefore", Date.class));
    dim.addParameter(new Parameter("location", "Location", Location.class));
    CohortDefinition routineViralLoadCohort =
        txPvlsBySourceLabOrFsrCohortQueries.getRoutineDisaggregationForPvlsBySource();
    CohortDefinition targetedViralLoadCohort =
        txPvlsBySourceLabOrFsrCohortQueries.getPatientsOnTargetByFsr();
    dim.addCohortDefinition("VLR", mapStraightThrough(routineViralLoadCohort));
    dim.addCohortDefinition("VLT", mapStraightThrough(targetedViralLoadCohort));
    return dim;
  }

  public CohortDefinitionDimension getPregnantAndBreastfeedingPatientsBasedOnPrepNew() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.addParameter(new Parameter("startDate", "startDate", Date.class));
    dim.addParameter(new Parameter("endDate", "endDate", Date.class));
    dim.addParameter(new Parameter("location", "location", Location.class));
    dim.setName("Patients Pregnant or Breastfeeding based on Prep New");

    dim.addCohortDefinition(
        "pregnant",
        EptsReportUtils.map(
            prepNewCohortQueries.getPregnantPatientsBasedOnPrepNew(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    dim.addCohortDefinition(
        "breastfeeding",
        EptsReportUtils.map(
            prepNewCohortQueries.getBreastfeedingPatientsBasedOnPrepNew(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    return dim;
  }

  /**
   * <b>Description:</b> Clients marked with “PrEP Interrompida” and field “Razões para Interromper
   * PrEP” with one of reasons of interruption on the “Ficha de Consulta Inicial PrEP” with the most
   * recent date that falls during the reporting period OR Clients with the field “PrEP
   * Interrompida” marked with one of the reasons of interruption on the most recent the “Ficha de
   * Consulta de Seguimento PrEP” during the reporting period
   *
   * @return
   */
  public CohortDefinitionDimension getClientsWithReasonForPrepInterruptionDisaggregation() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();

    dim.addParameter(new Parameter("startDate", "startDate", Date.class));
    dim.addParameter(new Parameter("endDate", "endDate", Date.class));
    dim.addParameter(new Parameter("location", "location", Location.class));
    dim.setName("Patients With Reason For Prep Interruption Disaggregation");

    dim.addCohortDefinition(
        "hivInfected",
        EptsReportUtils.map(
            prepCtCohortQueries.getClientsWithReasonForPrepInterruption(
                hivMetadata.getHivInfectedConcept().getConceptId()),
            "startDate=${endDate},endDate=${endDate},location=${location}"));

    dim.addCohortDefinition(
        "adverseReaction",
        EptsReportUtils.map(
            prepCtCohortQueries.getClientsWithReasonForPrepInterruption(
                hivMetadata.getAdverseReaction().getConceptId()),
            "startDate=${endDate},endDate=${endDate},location=${location}"));

    dim.addCohortDefinition(
        "noMoreSubstantialRisks",
        EptsReportUtils.map(
            prepCtCohortQueries.getClientsWithReasonForPrepInterruption(
                hivMetadata.getNoMoreSubstantialRisksConcept().getConceptId()),
            "startDate=${endDate},endDate=${endDate},location=${location}"));

    dim.addCohortDefinition(
        "userPreference",
        EptsReportUtils.map(
            prepCtCohortQueries.getClientsWithReasonForPrepInterruption(
                hivMetadata.getUserPreferenceConcept().getConceptId()),
            "startDate=${endDate},endDate=${endDate},location=${location}"));

    dim.addCohortDefinition(
        "other",
        EptsReportUtils.map(
            prepCtCohortQueries.getClientsWithReasonForPrepInterruption(
                hivMetadata.getOtherOrNonCodedConcept().getConceptId()),
            "startDate=${endDate},endDate=${endDate},location=${location}"));

    return dim;
  }

  public CohortDefinitionDimension getInfantAgeInMonths() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.setName("Infant Age in Number of Days");
    dim.addParameter(new Parameter("startDate", "startDate", Date.class));
    dim.addParameter(new Parameter("endDate", "endDate", Date.class));
    dim.addParameter(new Parameter("location", "location", Location.class));

    dim.addCohortDefinition(
        "lessThan2Months",
        EptsReportUtils.map(
            pmtctEidCohortQueries.getInfantAge(0, 59),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    dim.addCohortDefinition(
        "from2To12Months",
        EptsReportUtils.map(
            pmtctEidCohortQueries.getInfantAge(60, 365),
            "startDate=${startDate},endDate=${endDate},location=${location}"));
    return dim;
  }

  public CohortDefinitionDimension
      getInfantsWhoUnderwentSampleCollectionForFirstVirologicHivTest() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.setName("Infant Age in Number of Days");
    dim.addParameter(new Parameter("startDate", "startDate", Date.class));
    dim.addParameter(new Parameter("endDate", "endDate", Date.class));
    dim.addParameter(new Parameter("location", "location", Location.class));

    dim.addCohortDefinition(
        "firstSample",
        EptsReportUtils.map(
            pmtctEidCohortQueries
                .getPatientsWhoUnderwentFirstOrSecondSampleCollectionForVirologicHivTest(true),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    dim.addCohortDefinition(
        "secondSample",
        EptsReportUtils.map(
            pmtctEidCohortQueries
                .getPatientsWhoUnderwentFirstOrSecondSampleCollectionForVirologicHivTest(false),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    return dim;
  }

  public CohortDefinitionDimension getPosiveOrNegativeVirologicHiv() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.setName("virologic HIV test result");
    dim.addParameter(new Parameter("startDate", "startDate", Date.class));
    dim.addParameter(new Parameter("endDate", "endDate", Date.class));
    dim.addParameter(new Parameter("location", "location", Location.class));

    dim.addCohortDefinition(
        "positiveResult",
        EptsReportUtils.map(
            pmtctHeiCohortQueries.getPositveOrNegativePcrResult(true),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    dim.addCohortDefinition(
        "negativeResult",
        EptsReportUtils.map(
            pmtctHeiCohortQueries.getPositveOrNegativePcrResult(false),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    return dim;
  }

  public CohortDefinitionDimension getInfantArtInitiationDimension() {
    CohortDefinitionDimension dim = new CohortDefinitionDimension();
    dim.setName("Infant Art Initiation");
    dim.addParameter(new Parameter("startDate", "startDate", Date.class));
    dim.addParameter(new Parameter("endDate", "endDate", Date.class));
    dim.addParameter(new Parameter("location", "location", Location.class));

    dim.addCohortDefinition(
        "artInitiated",
        EptsReportUtils.map(
            pmtctHeiCohortQueries.getInfantsWithConfirmedArtInitiation(),
            "startDate=${startDate},endDate=${endDate},location=${location}"));

    return dim;
  }
}
