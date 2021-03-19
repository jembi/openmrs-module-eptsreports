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
package org.openmrs.module.eptsreports.reporting.calculation.tpt;

import java.util.*;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.Days;
import org.joda.time.Interval;
import org.openmrs.*;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.ListResult;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.eptsreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.eptsreports.reporting.calculation.BooleanResult;
import org.openmrs.module.eptsreports.reporting.calculation.common.EPTSCalculationService;
import org.openmrs.module.eptsreports.reporting.utils.EPTSMetadataDatetimeQualifier;
import org.openmrs.module.eptsreports.reporting.utils.EptsCalculationUtils;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Patients that completed isoniazid prophylactic treatment */
@Component
public class CompletedIsoniazidTPTCalculation extends AbstractPatientCalculation {

  private static final int COMPLETION_PERIOD_OFFSET = 0;

  private static final int TREATMENT_BEGIN_PERIOD_OFFSET = -6;

  private static final int NUMBER_ISONIAZID_USAGE_TO_CONSIDER_COMPLETED = 5;

  private static final int MONTHS_TO_CHECK_FOR_ISONIAZID_USAGE = 7;

  private static final int MINIMUM_DURATION_IN_DAYS = 173;

  private static final String ON_OR_AFTER = "onOrAfter";

  private static final String ON_OR_BEFORE = "onOrBefore";

  private enum Priority {
    MIN,
    MAX
  };

  @Autowired private HivMetadata hivMetadata;

  @Autowired private TbMetadata tbMetadata;

  @Autowired private EPTSCalculationService ePTSCalculationService;

  @SuppressWarnings("unused")
  @Override
  public CalculationResultMap evaluate(
      Collection<Integer> cohort,
      Map<String, Object> parameterValues,
      PatientCalculationContext context) {
    CalculationResultMap map = new CalculationResultMap();
    Location location = (Location) context.getFromCache("location");

    Date onOrBefore = (Date) context.getFromCache(ON_OR_BEFORE);
    Date onOrAfter = (Date) context.getFromCache(ON_OR_AFTER);

    if (onOrBefore != null) {
      Date beginPeriodEndDate =
          EptsCalculationUtils.addMonths(onOrBefore, TREATMENT_BEGIN_PERIOD_OFFSET);
      Date completionPeriodEndDate =
          EptsCalculationUtils.addMonths(onOrBefore, COMPLETION_PERIOD_OFFSET);

      final List<EncounterType> consultationEncounterTypes =
          Arrays.asList(
              hivMetadata.getAdultoSeguimentoEncounterType(),
              hivMetadata.getPediatriaSeguimentoEncounterType(),
              hivMetadata.getMasterCardEncounterType());

      Concept c23985 = tbMetadata.getRegimeTPTConcept();
      EncounterType e60 = tbMetadata.getRegimeTPTEncounterType();
      EncounterType e6 = hivMetadata.getAdultoSeguimentoEncounterType();
      EncounterType e9 = hivMetadata.getPediatriaSeguimentoEncounterType();
      EncounterType e53 = hivMetadata.getMasterCardEncounterType();
      Concept c656 = tbMetadata.getIsoniazidConcept();
      Concept c23982 = tbMetadata.getIsoniazidePiridoxinaConcept();
      Concept c1719 = tbMetadata.getTreatmentPrescribedConcept();
      Concept c23955 = tbMetadata.getDtINHConcept();
      Concept c23986 = tbMetadata.getTypeDispensationTPTConceptUuid();
      Concept c1098 = hivMetadata.getMonthlyConcept();
      Concept c6122 = hivMetadata.getIsoniazidUsageConcept();
      Concept c1256 = hivMetadata.getStartDrugs();
      Concept c1257 = hivMetadata.getContinueRegimenConcept();
      Concept c23720 = hivMetadata.getQuarterlyConcept();
      Concept c23954 = tbMetadata.get3HPConcept();
      Concept c23984 = tbMetadata.get3HPPiridoxinaConcept();
      Concept c6129 = hivMetadata.getDataFinalizacaoProfilaxiaIsoniazidaConcept();

      /** ----- all patients who started IPT ---- */
      // A1
       CalculationResultMap startProfilaxiaObservation53 =
            ePTSCalculationService.getObs(
              hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept(),
              e53,
              cohort,
              location,
              null,
              TimeQualifier.FIRST,
              null, 
              onOrBefore, 
              EPTSMetadataDatetimeQualifier.VALUE_DATETIME,
              context);

        // A2
      CalculationResultMap startDrugsObservations =
      ePTSCalculationService.getObs(
          hivMetadata.getIsoniazidUsageConcept(),
          Arrays.asList(hivMetadata.getAdultoSeguimentoEncounterType()),
          cohort,
          Arrays.asList(location),
          Arrays.asList(hivMetadata.getStartDrugs()),
          TimeQualifier.FIRST,
          null,
          onOrBefore,
          context);

        // A3
      CalculationResultMap startProfilaxiaObservation6 =
          ePTSCalculationService.getObs(
            hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept(),
            e6,
            cohort,
            location,
            null,
            TimeQualifier.FIRST,
            null, 
            onOrBefore, 
            EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
            context);

        // A4
        CalculationResultMap startProfilaxiaObservation9 =
          ePTSCalculationService.getObs(
            hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept(),
            e9,
            cohort,
            location,
            null,
            TimeQualifier.FIRST,
            null, 
            onOrBefore, 
            EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
            context);


      // A5 - FILT - Patients who have Regime de TPT with the values marked on the first pick-up date 
      CalculationResultMap regimeTPT1stPickUpMap =
          ePTSCalculationService.getObs(
              c23985,
              e60,
              cohort,
              location,
              Arrays.asList(c656, c23982),
              TimeQualifier.FIRST,
              null,
              onOrBefore,
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);

      /*CalculationResultMap exclisionregimeTPT1stPickUpMap =
          ePTSCalculationService.getObs(
              c23985,
              e60,
              cohort,
              location,
              Arrays.asList(c656, c23982),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -13),
              DateUtils.addMonths(onOrBefore, -6),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);*/

      // IV. Patients who have Outras Prescrições with the values (DT-INH) */
      /*

      CalculationResultMap exclusionOutrasPrescricoesPreviousPeriodMap =
          ePTSCalculationService.getObs(
              c1719,
              e6,
              cohort,
              location,
              Arrays.asList(c23955),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -13),
              DateUtils.addMonths(onOrBefore, -6),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);*/

      /** ------ who completed IPT treatment during the reporting period--- */
      // B1
     CalculationResultMap endProfilaxiaObservations53 =
      ePTSCalculationService.getObs(
          c6129,
          e53,
          cohort,
          location,
          null,
          TimeQualifier.LAST,
          null, 
          onOrBefore, 
          EPTSMetadataDatetimeQualifier.VALUE_DATETIME,
          context);

        // B2
     CalculationResultMap completedDrugsObservations =
    ePTSCalculationService.getObs(
        hivMetadata.getIsoniazidUsageConcept(),
        Arrays.asList(hivMetadata.getAdultoSeguimentoEncounterType()),
        cohort,
        Arrays.asList(location),
        Arrays.asList(hivMetadata.getCompletedConcept()),
        TimeQualifier.LAST,
        null,
        onOrBefore,
        context);   

        // B3
     CalculationResultMap endProfilaxiaObservations6 =
          ePTSCalculationService.getObs(
              c6129,
              e6,
              cohort,
              location,
              null,
              TimeQualifier.LAST,
              null, 
              onOrBefore, 
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      
      // B4
      CalculationResultMap endProfilaxiaObservations9 =
          ePTSCalculationService.getObs(
              c6129,
              e9,
              cohort,
              location,
              null,
              TimeQualifier.LAST,
              null, 
              onOrBefore, 
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);

     /*CalculationResultMap outrasPrescricoesMap =
              ePTSCalculationService.getObs(
                  c1719,
                  e6,
                  cohort,
                  location,
                  Arrays.asList(c23955),
                  TimeQualifier.FIRST,
                  null,
                  onOrBefore,
                  EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
                  context);*/
      

      /** ------ with IPTStarteDate-IPTEndDate>=173days--- */
    
      // VIII
      CalculationResultMap isoniazidUsageObservationsList =
      ePTSCalculationService.getObs(
        c6122,
        e6,
        cohort,
        location,
        Arrays.asList(c1256,c1257),
        TimeQualifier.FIRST,
        null,
        onOrBefore,
        EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
        context);
      
      // IX
      // iptstartdate  + 7m <= iptstartdate
      /*CalculationResultMap filtMap1 =
          ePTSCalculationService.getObs(
              c23985,
              e60,
              cohort,
              location,
              Arrays.asList(c656),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -6),
              DateUtils.addMonths(onOrBefore, 1),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      CalculationResultMap filtMap2 =
          ePTSCalculationService.getObs(
              c23986,
              e60,
              cohort,
              location,
              Arrays.asList(c1098),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -6),
              DateUtils.addMonths(onOrBefore, 1),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      CalculationResultMap filtMap3 =
          ePTSCalculationService.getObs(
              c23985,
              e60,
              cohort,
              location,
              Arrays.asList(c23982),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -6),
              DateUtils.addMonths(onOrBefore, 1),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      
      // X
      CalculationResultMap dtINHMap1 =
          ePTSCalculationService.getObs(
              c6122,
              e6,
              cohort,
              location,
              Arrays.asList(c1256),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -6),
              DateUtils.addMonths(onOrBefore, 6),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      CalculationResultMap dtINHMap2 =
          ePTSCalculationService.getObs(
              c1719,
              e6,
              cohort,
              location,
              Arrays.asList(c23955),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -6),
              DateUtils.addMonths(onOrBefore, 6),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      CalculationResultMap dtINHMap3 =
          ePTSCalculationService.getObs(
              c6122,
              e6,
              cohort,
              location,
              Arrays.asList(c1257),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -6),
              DateUtils.addMonths(onOrBefore, 6),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      CalculationResultMap dtINHMap4 =
          ePTSCalculationService.getObs(
              c1719,
              e6,
              cohort,
              location,
              Arrays.asList(c23955),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -6),
              DateUtils.addMonths(onOrBefore, 6),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);

      // XI
      CalculationResultMap dfiltdtINHMap1 =
          ePTSCalculationService.getObs(
              c23985,
              e60,
              cohort,
              location,
              Arrays.asList(c656),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -6),
              DateUtils.addMonths(onOrBefore, 6),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      CalculationResultMap dfiltdtINHMap2 =
          ePTSCalculationService.getObs(
              c23986,
              e60,
              cohort,
              location,
              Arrays.asList(c23720),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -6),
              DateUtils.addMonths(onOrBefore, 6),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      CalculationResultMap dfiltdtINHMap3 =
          ePTSCalculationService.getObs(
              c23985,
              e60,
              cohort,
              location,
              Arrays.asList(c23982),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -6),
              DateUtils.addMonths(onOrBefore, 6),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      CalculationResultMap dfiltdtINHMap4 =
          ePTSCalculationService.getObs(
              c23986,
              e60,
              cohort,
              location,
              Arrays.asList(c23720),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -6),
              DateUtils.addMonths(onOrBefore, 6),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);

      // XII
      CalculationResultMap atLeast3FichaClínicaINHMap1 =
          ePTSCalculationService.getObs(
              c6122,
              e6,
              cohort,
              location,
              Arrays.asList(c1256),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -6),
              DateUtils.addMonths(onOrBefore, 1),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      CalculationResultMap atLeast3FichaClínicaINHMap2 =
          ePTSCalculationService.getObs(
              c6122,
              e6,
              cohort,
              location,
              Arrays.asList(c1257),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -6),
              DateUtils.addMonths(onOrBefore, 1),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      CalculationResultMap atLeast3FichaClínicaINHMap3 =
          ePTSCalculationService.getObs(
              c1719,
              e6,
              cohort,
              location,
              null,
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -6),
              DateUtils.addMonths(onOrBefore, 1),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      CalculationResultMap exclusionAtLeast3FichaClínicaINHMap3 =
          ePTSCalculationService.getObs(
              c1719,
              e6,
              cohort,
              location,
              Arrays.asList(c23955),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -6),
              DateUtils.addMonths(onOrBefore, 1),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      CalculationResultMap atLeast3FichaClínicaINHMap4 =
          ePTSCalculationService.getObs(
              c6122,
              e6,
              cohort,
              location,
              Arrays.asList(c1256),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -6),
              DateUtils.addMonths(onOrBefore, 1),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      CalculationResultMap atLeast3FichaClínicaINHMap5 =
          ePTSCalculationService.getObs(
              c6122,
              e6,
              cohort,
              location,
              Arrays.asList(c1257),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -6),
              DateUtils.addMonths(onOrBefore, 1),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      CalculationResultMap atLeast3FichaClínicaINHMap6 =
          ePTSCalculationService.getObs(
              c1719,
              e6,
              cohort,
              location,
              Arrays.asList(c23955),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -6),
              DateUtils.addMonths(onOrBefore, 1),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);

      // XIII

      CalculationResultMap atLeast3FILTINHMensal1FILTDTINHMap1 =
          ePTSCalculationService.getObs(
              c23985,
              e60,
              cohort,
              location,
              Arrays.asList(c656),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -6),
              DateUtils.addMonths(onOrBefore, 1),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      CalculationResultMap atLeast3FILTINHMensal1FILTDTINHMap2 =
          ePTSCalculationService.getObs(
              c23986,
              e60,
              cohort,
              location,
              Arrays.asList(c23720),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -6),
              DateUtils.addMonths(onOrBefore, 1),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      CalculationResultMap atLeast3FILTINHMensal1FILTDTINHMap3 =
          ePTSCalculationService.getObs(
              c23985,
              e60,
              cohort,
              location,
              Arrays.asList(c23982),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -6),
              DateUtils.addMonths(onOrBefore, 1),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      CalculationResultMap atLeast3FILTINHMensal1FILTDTINHMap4 =
          ePTSCalculationService.getObs(
              c23986,
              e60,
              cohort,
              location,
              Arrays.asList(c23720),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -6),
              DateUtils.addMonths(onOrBefore, 1),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      CalculationResultMap atLeast3FILTINHMensal1FILTDTINHMap5 =
          ePTSCalculationService.getObs(
              c23985,
              e60,
              cohort,
              location,
              Arrays.asList(c656),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -6),
              DateUtils.addMonths(onOrBefore, 1),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      CalculationResultMap atLeast3FILTINHMensal1FILTDTINHMap6 =
          ePTSCalculationService.getObs(
              c23986,
              e60,
              cohort,
              location,
              Arrays.asList(c1098),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -6),
              DateUtils.addMonths(onOrBefore, 1),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      CalculationResultMap atLeast3FILTINHMensal1FILTDTINHMap7 =
          ePTSCalculationService.getObs(
              c23985,
              e60,
              cohort,
              location,
              Arrays.asList(c23982),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -6),
              DateUtils.addMonths(onOrBefore, 1),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      CalculationResultMap atLeast3FILTINHMensal1FILTDTINHMap8 =
          ePTSCalculationService.getObs(
              c23986,
              e60,
              cohort,
              location,
              Arrays.asList(c1098),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -6),
              DateUtils.addMonths(onOrBefore, 1),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);*/

      /** START and END 3HP Treatment */

      // Tipo de dispensa - Trimestral
      CalculationResultMap filtMap4 =
          ePTSCalculationService.getObs(
              c23986,
              e60,
              cohort,
              location,
              Arrays.asList(c1098),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -6),
              onOrBefore,
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);

      // C1 and D part 1

      CalculationResultMap artListTbPrevList3HPMap =
          ePTSCalculationService.getObs(
              c1719,
              e6,
              cohort,
              location,
              Arrays.asList(c23954),
              TimeQualifier.FIRST,
              null,
              onOrBefore,
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);

      /*CalculationResultMap exclucsionAartListTbPrevList3HPPreviousPeriodMap =
          ePTSCalculationService.getObs(
              c1719,
              e6,
              cohort,
              location,
              Arrays.asList(c23954),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -12),
              DateUtils.addMonths(onOrBefore, -6),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);*/
      
              // C2
      CalculationResultMap regimeTPT3HPMap =
          ePTSCalculationService.getObs(
              c23985,
              e60,
              cohort,
              location,
              Arrays.asList(c23954, c23984),
              TimeQualifier.FIRST,
              null,
              onOrBefore,
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);


            // D part 1
              CalculationResultMap atLeast3FichaClínicaMastercard3HPUntil4Month =
          ePTSCalculationService.getObs(
              c1719,
              e6,
              cohort,
              location,
              Arrays.asList(c23954),
              TimeQualifier.ANY,
              null,
              onOrBefore,
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);


              // D part 2
      CalculationResultMap atleast1FILT3HPTrimestral =
      ePTSCalculationService.getObs(
          c23985,
          e60,
          cohort,
          location,
          Arrays.asList(c23954,c23984),
          TimeQualifier.ANY,
          null,
          onOrBefore,
          EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
          context);

          // D part 3
          CalculationResultMap atleast3FILT3HPTrimestral =
      ePTSCalculationService.getObs(
          c23985,
          e60,
          cohort,
          location,
          Arrays.asList(c23954,c23984),
          TimeQualifier.ANY,
          null,
          onOrBefore,
          EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
          context);

      /*CalculationResultMap exclusionregimeTPT3HPMap =
          ePTSCalculationService.getObs(
              c1719,
              e60,
              cohort,
              location,
              Arrays.asList(c23954),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -10),
              DateUtils.addMonths(onOrBefore, -6),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      
      CalculationResultMap atleast1FILT3HPTrimestralMa2 =
          ePTSCalculationService.getObs(
              c23986,
              e60,
              cohort,
              location,
              Arrays.asList(c23720),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -10),
              DateUtils.addMonths(onOrBefore, -6),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      CalculationResultMap atleast1FILT3HPTrimestralMa3 =
          ePTSCalculationService.getObs(
              c23985,
              e60,
              cohort,
              location,
              Arrays.asList(c23984),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -10),
              DateUtils.addMonths(onOrBefore, -6),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      CalculationResultMap atleast1FILT3HPTrimestralMa4 =
          ePTSCalculationService.getObs(
              c23986,
              e60,
              cohort,
              location,
              Arrays.asList(c23720),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -10),
              DateUtils.addMonths(onOrBefore, -6),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      // part 2 from  III
      CalculationResultMap atleast3FILTS3HPMensal1 =
          ePTSCalculationService.getObs(
              c23985,
              e60,
              cohort,
              location,
              Arrays.asList(c23954),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -10),
              DateUtils.addMonths(onOrBefore, -6),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);

      CalculationResultMap atleast3FILTS3HPMensal2 =
          ePTSCalculationService.getObs(
              c23986,
              e60,
              cohort,
              location,
              Arrays.asList(c23720),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -10),
              DateUtils.addMonths(onOrBefore, -6),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      CalculationResultMap atleast3FILTS3HPMensal3 =
          ePTSCalculationService.getObs(
              c23985,
              e60,
              cohort,
              location,
              Arrays.asList(c23984),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -10),
              DateUtils.addMonths(onOrBefore, -6),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);
      CalculationResultMap atleast3FILTS3HPMensal4 =
          ePTSCalculationService.getObs(
              c23986,
              e60,
              cohort,
              location,
              Arrays.asList(c23720),
              TimeQualifier.ANY,
              DateUtils.addMonths(onOrAfter, -10),
              DateUtils.addMonths(onOrBefore, -6),
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);

      // IV
      */

      for (Integer patientId : cohort) {
              
        // ipt start date section
        Obs startProfilaxiaObs53 =
            EptsCalculationUtils.obsResultForPatient(startProfilaxiaObservation53, patientId);

        Obs startProfilaxiaObs6 =
            EptsCalculationUtils.obsResultForPatient(startProfilaxiaObservation6, patientId);

        Obs startProfilaxiaObs9 =
            EptsCalculationUtils.obsResultForPatient(startProfilaxiaObservation9, patientId);

        Obs startDrugsObs =
            EptsCalculationUtils.obsResultForPatient(startDrugsObservations, patientId);

        Obs regimeTPT1stPickUpPreviousPeriod = EptsCalculationUtils.obsResultForPatient(regimeTPT1stPickUpMap, patientId);

        /*List<Obs> exclisionRegimeTPT1stPickUpPreviousPeriod =
            getObsListFromResultMap(exclisionregimeTPT1stPickUpMap, patientId);

        Obs outrasPrescricoesPreviousPeriod =
            EptsCalculationUtils.obsResultForPatient(outrasPrescricoesPreviousPeriodMap, patientId);
        List<Obs> exclusionOutrasPrescricoesPreviousPeriod =
            getObsListFromResultMap(exclusionOutrasPrescricoesPreviousPeriodMap, patientId);*/

        // ipt end date section
        // Obs endProfilaxiaObs =
        //   EptsCalculationUtils.obsResultForPatient(endProfilaxiaObservations, patientId);
        Obs endProfilaxiaObs6 =
            EptsCalculationUtils.obsResultForPatient(endProfilaxiaObservations6, patientId);
        Obs endProfilaxiaObs53 =
            EptsCalculationUtils.obsResultForPatient(endProfilaxiaObservations53, patientId);
        Obs endProfilaxiaObs9 =
            EptsCalculationUtils.obsResultForPatient(endProfilaxiaObservations9, patientId);
        Obs endDrugsObs =
            EptsCalculationUtils.obsResultForPatient(completedDrugsObservations, patientId);
        
            /*
                 * If We can't find a startDate from Ficha de Seguimento (adults and children) / Ficha
                 * Resumo or Ficha Clinica-MasterCard, we can't do the calculations. -Just move to the next
                 * patient.
                 */
        if (startProfilaxiaObs53 == null && startProfilaxiaObs6 == null && startProfilaxiaObs9 == null
        && startDrugsObs == null) {
          continue;
        }
        Date iptStartDate =
            getMinOrMaxObsDate(
                Arrays.asList(
                    startProfilaxiaObs53,
                    startProfilaxiaObs6,
                    startProfilaxiaObs9,
                    startDrugsObs),
                Priority.MIN,
                true);

        Date iptEndDate =
            getMinOrMaxObsDate(
                Arrays.asList(
                    endProfilaxiaObs53, endProfilaxiaObs6, endProfilaxiaObs9, endDrugsObs),
                Priority.MAX,
                true);

        Obs B1 = returnObsBetweenIptStartDateAndIptEndDate(ArrayList(endProfilaxiaObs53),iptStartDate, 173, 365);
        Obs B2 = returnObsBetweenIptStartDateAndIptEndDate(ArrayList(endDrugsObs),iptStartDate, 173, 365);
        Obs B3 = returnObsBetweenIptStartDateAndIptEndDate(ArrayList(endProfilaxiaObs6),iptStartDate, 173, 365);
        Obs B4 = returnObsBetweenIptStartDateAndIptEndDate(ArrayList(endProfilaxiaObs9),iptStartDate, 173, 365);

        if (B1 != null || B2 != null || B3 != null || B4 != null) {
            
            
            
            map.put(patientId, new BooleanResult(true, this));
        }
        

        // viii
        int viii =
            calculateNumberOfYesAnswers(isoniazidUsageObservationsList, patientId, iptStartDate);

        // ix
        int ixa =
            evaluateOccurrence(
                getObsListFromResultMap(filtMap1, patientId),
                getObsListFromResultMap(filtMap2, patientId),
                iptStartDate,
                6,
                7);
        int ixb =
            evaluateOccurrence(
                getObsListFromResultMap(filtMap3, patientId),
                getObsListFromResultMap(filtMap4, patientId),
                iptStartDate,
                6,
                7);

        // x
        int xa =
            evaluateOccurrence(
                getObsListFromResultMap(dtINHMap1, patientId),
                getObsListFromResultMap(dtINHMap2, patientId),
                iptStartDate,
                2,
                5);
        int xb =
            evaluateOccurrence(
                getObsListFromResultMap(dtINHMap3, patientId),
                getObsListFromResultMap(dtINHMap4, patientId),
                iptStartDate,
                2,
                5);

        // xi
        int xia =
            evaluateOccurrence(
                getObsListFromResultMap(dfiltdtINHMap1, patientId),
                getObsListFromResultMap(dfiltdtINHMap2, patientId),
                iptStartDate,
                2,
                5);
        int xib =
            evaluateOccurrence(
                getObsListFromResultMap(dfiltdtINHMap3, patientId),
                getObsListFromResultMap(dfiltdtINHMap4, patientId),
                iptStartDate,
                2,
                5);

        // xii
        int xiia1 =
            evaluateOccurrence(
                getObsListFromResultMap(atLeast3FichaClínicaINHMap1, patientId),
                iptStartDate,
                3,
                7);
        List<Obs> atLeast3FichaClínicaINHMap3Cleaned =
            exclude(
                getObsListFromResultMap(atLeast3FichaClínicaINHMap3, patientId),
                getObsListFromResultMap(exclusionAtLeast3FichaClínicaINHMap3, patientId));

        int xiia2 =
            evaluateOccurrence(
                getObsListFromResultMap(atLeast3FichaClínicaINHMap2, patientId),
                atLeast3FichaClínicaINHMap3Cleaned,
                iptStartDate,
                3,
                7);

        int xiib1 =
            evaluateOccurrence(
                getObsListFromResultMap(atLeast3FichaClínicaINHMap4, patientId),
                iptStartDate,
                1,
                7);
        int xiib2 =
            evaluateOccurrence(
                getObsListFromResultMap(atLeast3FichaClínicaINHMap5, patientId),
                getObsListFromResultMap(atLeast3FichaClínicaINHMap6, patientId),
                iptStartDate,
                1,
                7);

        // xiii
        int xiiia1 =
            evaluateOccurrence(
                getObsListFromResultMap(atLeast3FILTINHMensal1FILTDTINHMap1, patientId),
                getObsListFromResultMap(atLeast3FILTINHMensal1FILTDTINHMap2, patientId),
                iptStartDate,
                1,
                7);
        int xiiia2 =
            evaluateOccurrence(
                getObsListFromResultMap(atLeast3FILTINHMensal1FILTDTINHMap3, patientId),
                getObsListFromResultMap(atLeast3FILTINHMensal1FILTDTINHMap4, patientId),
                iptStartDate,
                1,
                7);

        int xiiib1 =
            evaluateOccurrence(
                getObsListFromResultMap(atLeast3FILTINHMensal1FILTDTINHMap5, patientId),
                getObsListFromResultMap(atLeast3FILTINHMensal1FILTDTINHMap6, patientId),
                iptStartDate,
                3,
                7);
        int xiiib2 =
            evaluateOccurrence(
                getObsListFromResultMap(atLeast3FILTINHMensal1FILTDTINHMap7, patientId),
                getObsListFromResultMap(atLeast3FILTINHMensal1FILTDTINHMap8, patientId),
                iptStartDate,
                3,
                7);

        // ( I or II or III or IV ) and (V or VI) or VII or VIII ... XIII)
        if (iptStartDate != null && iptEndDate != null) {
          if (getProfilaxiaDuration(iptStartDate, iptEndDate) >= MINIMUM_DURATION_IN_DAYS
              || viii >= NUMBER_ISONIAZID_USAGE_TO_CONSIDER_COMPLETED
              || (ixa + ixb) >= 6
              || (xa + xb) >= 2
              || (xia + xib) >= 2
              || ((xiia1 + xiia2) >= 3 && (xiib1 + xiib2) >= 1)
              || (xiiia1 + xiiia2 >= 1 && xiiib1 + xiiib2 >= 3)) {
            map.put(patientId, new BooleanResult(true, this));
          }
        }

        /* 3HP */
        Obs artListTbPrevList3HPPreviousPeriod =
            EptsCalculationUtils.obsResultForPatient(
                artListTbPrevList3HPMap, patientId);
        Obs regimeTPT3HP = EptsCalculationUtils.obsResultForPatient(regimeTPT3HPMap, patientId);

        List<Obs> exclucsionAartListTbPrevList3HPPreviousPeriod =
            getObsListFromResultMap(exclucsionAartListTbPrevList3HPPreviousPeriodMap, patientId);
        List<Obs> exclusionregimeTPT3HP =
            getObsListFromResultMap(exclusionregimeTPT3HPMap, patientId);

        Date first3HPDate =
            getMinOrMaxObsDate(
                Arrays.asList(
                    this.exclude(
                        artListTbPrevList3HPPreviousPeriod,
                        exclucsionAartListTbPrevList3HPPreviousPeriod,
                        4),
                    this.exclude(regimeTPT3HP, exclusionregimeTPT3HP, 4)),
                Priority.MIN,
                false);

        int atleast1FILT3HPTrimestralsOccurencies1 =
            evaluateOccurrence(
                getObsListFromResultMap(atleast1FILT3HPTrimestral, patientId),
                getObsListFromResultMap(filtMap4, patientId),
                first3HPDate,
                1,
                4);

                int atleast3FILTS3HPTrimestralOccurencies =
                evaluateOccurrence(
                    getObsListFromResultMap(atleast3FILT3HPTrimestral, patientId),
                    getObsListFromResultMap(filtMap4, patientId),
                    first3HPDate,
                    3,
                    4);

        /*int atleast1FILT3HPTrimestralsOccurencies2 =
            evaluateOccurrence(
                getObsListFromResultMap(atleast1FILT3HPTrimestralMa3, patientId),
                getObsListFromResultMap(atleast1FILT3HPTrimestralMa4, patientId),
                first3HPDate,
                1,
                4);

        int atleast3FILTS3HPMensalOccurencies1 =
            evaluateOccurrence(
                getObsListFromResultMap(atleast3FILTS3HPMensal1, patientId),
                getObsListFromResultMap(atleast3FILTS3HPMensal2, patientId),
                first3HPDate,
                3,
                4);

        

        int atleast3FILTS3HPMensalOccurencies2 =
            evaluateOccurrence(
                getObsListFromResultMap(atleast3FILTS3HPMensal3, patientId),
                getObsListFromResultMap(atleast3FILTS3HPMensal4, patientId),
                first3HPDate,
                3,
                4);*/

        List<Obs> atLeast3FichaClínicaMastercard3HPUntil4Month =
            getObsListFromResultMap(atLeast3FichaClínicaMastercard3HPUntil4Month, patientId);

        int atleast3FILTS3HPMensalListMensalOccurencies =
            evaluateOccurrence(atLeast3FichaClínicaMastercard3HPUntil4Month, first3HPDate, 3, 4);

        if (first3HPDate != null
            && (atleast1FILT3HPTrimestralsOccurencies1 + atleast1FILT3HPTrimestralsOccurencies2 >= 1
                || atleast3FILTS3HPMensalOccurencies1 + atleast3FILTS3HPMensalOccurencies2 >= 3
                || atleast3FILTS3HPMensalListMensalOccurencies >= 3)) {
          map.put(patientId, new BooleanResult(true, this));
        }
      }
      return map;
    } else {
      throw new IllegalArgumentException(
          String.format("Parameters %s and %s must be set", ON_OR_AFTER, ON_OR_BEFORE));
    }
  }

  private int calculateNumberOfYesAnswers(
      CalculationResultMap isoniazidUsageObservationsList, Integer patientId, Date startDate) {
    List<Obs> isoniazidUsageObservations =
        EptsCalculationUtils.extractResultValues(
            (ListResult) isoniazidUsageObservationsList.get(patientId));
    int count = 0;
    Date isoniazidUsageEndDate = getIsoniazidUsageEndDate(startDate);
    for (Obs obs : isoniazidUsageObservations) {
      Date date = obs.getObsDatetime();
      if (date.compareTo(startDate) > 0 && date.compareTo(isoniazidUsageEndDate) <= 0) {
        count++;
      }
    }
    return count;
  }

  private Date getIsoniazidUsageEndDate(Date startDate) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(startDate);
    calendar.add(Calendar.MONTH, MONTHS_TO_CHECK_FOR_ISONIAZID_USAGE);
    return calendar.getTime();
  }

  private Date getDateFromObs(Obs obs) {
    if (obs != null) {
      return obs.getValueDatetime();
    }
    return null;
  }
  /**
   * @param obss list of Obss
   * @param priority MIN to retrieve the start drugs date, MAX to retrieve the end drugs date
   * @return The earliest or most recent date according to the priority parameter
   */
  private Date getMinOrMaxObsDate(List<Obs> obss, Priority priority, boolean useValueDatetimeObs) {

    Date returedDate = null;

    List<Date> dates = new ArrayList<>();
    for (Obs o : obss) {

      if (o != null) {
        if (useValueDatetimeObs
            && o.equals(obss.get(0))
            && (o.getConcept().equals(hivMetadata.getDataInicioProfilaxiaIsoniazidaConcept())
                || o.getConcept()
                    .equals(hivMetadata.getDataFinalizacaoProfilaxiaIsoniazidaConcept()))) {
          dates.add(o.getValueDatetime());
        } else {
          dates.add(o.getEncounter().getEncounterDatetime());
        }
      }
    }
    if (!dates.isEmpty()) {
      if (priority == Priority.MAX) {
        returedDate = Collections.max(dates);
      }
      if (priority == Priority.MIN) {
        returedDate = Collections.min(dates);
      }
    }

    return returedDate;
  }
  /**
   * Calculate the interval in days between two dates
   *
   * @param startDate
   * @param endDate
   * @return the interval, 0 otherwise
   */
  private int getProfilaxiaDuration(Date startDate, Date endDate) {
    if (startDate != null && endDate != null && endDate.compareTo(startDate) > 0) {
      return Days.daysIn(new Interval(startDate.getTime(), endDate.getTime())).getDays();
    } else {
      return 0;
    }
  }

  private Obs exclude(Obs firstDate, List<Obs> bGroup, int month) {
    Obs obs = firstDate;
    if (firstDate != null) {
      if (month < 0) {
        for (Obs o : bGroup) {
          // >= fisrt-7m and < fisrt
          if (o.getEncounter()
                      .getEncounterDatetime()
                      .compareTo(
                          DateUtils.addMonths(
                              firstDate.getEncounter().getEncounterDatetime(), month))
                  >= 0
              && o.getEncounter()
                      .getEncounterDatetime()
                      .compareTo(firstDate.getEncounter().getEncounterDatetime())
                  < 0) {
            obs = null;
            break;
          }
        }
      }
      if (month > 0) {
        for (Obs o : bGroup) {

          if (o.getEncounter()
                      .getEncounterDatetime()
                      .compareTo(
                          DateUtils.addMonths(
                              firstDate.getEncounter().getEncounterDatetime(), month))
                  <= 0
              && o.getEncounter()
                      .getEncounterDatetime()
                      .compareTo(firstDate.getEncounter().getEncounterDatetime())
                  >= 0) {
            obs = null;
            break;
          }
        }
      }
    }
    return obs;
  }

  private List<Obs> getObsListFromResultMap(CalculationResultMap map, Integer pid) {
    ListResult listResult = (ListResult) map.get(pid);
    List<Obs> obss = EptsCalculationUtils.extractResultValues(listResult);
    obss.removeAll(Collections.singleton(null));
    return obss;
  }

  private int evaluateOccurrence(List<Obs> obss, Date iptStartDate, int nTimes, int plusIPTDate) {
    int num = 0;
    for (Obs o : obss) {

      if (o.getEncounter()
                  .getEncounterDatetime()
                  .compareTo(DateUtils.addMonths(iptStartDate, plusIPTDate))
              <= 0
          && o.getEncounter().getEncounterDatetime().compareTo(iptStartDate) >= 0) {
        num++;
        if (num == nTimes) {
          break;
        }
      }
    }
    return num;
  }

  /**
   * Given 2 list of obs the method extracts the encounter list from both, and then check for each
   * encounter in the fisrt list evaluates if it is present in the second list It uses the
   * implementation of contains method of ArrayList class which behind the schene it check if the
   * two encounters share the uuid.
   *
   * @param a list of OBs
   * @param b
   * @param iptStartDate
   * @param nTimes
   * @param plusIPTDate
   * @return
   */
  private int evaluateOccurrence(
      List<Obs> a, List<Obs> b, Date iptStartDate, int nTimes, int plusIPTDate) {
    List<Encounter> encountersA = new ArrayList<>();
    List<Encounter> encountersB = new ArrayList<>();

    for (Obs obs : a) {
      encountersA.add(obs.getEncounter());
    }
    for (Obs obs : b) {
      encountersB.add(obs.getEncounter());
    }
    int num = 0;

    for (Encounter e : encountersA) {
      if (e.getEncounterDatetime().compareTo(DateUtils.addMonths(iptStartDate, plusIPTDate)) <= 0
          && e.getEncounterDatetime().compareTo(iptStartDate) >= 0
          && encountersB.contains(e)) {
        num++;
        if (num == nTimes) {
          break;
        }
      }
    }
    return num;
  }

  /**
   * Given iptStartDate evaluate if Obs is between IPT startDate plus some number 
   * of days and IPT and some number of days
   *
   * @param a list of OBs
   * @param iptStartDate
   * @param nTimes
   * @param plusIPTDate
   * @return
   */
  private Obs returnObsBetweenIptStartDateAndIptEndDate(List<Obs> a, Date iptStartDate, int plusIPTStartDate,
          int plusIPTEndDate, boolean encounterDate) {

      Obs returnedObs = null;

      if (iptStartDate == null) {
          return returnedObs;
      }

      Date startDate = DateUtils.addDays(iptStartDate, plusIPTStartDate);
      Date endDate = DateUtils.addDays(iptStartDate, plusIPTEndDate);

      for (Obs obs : a) {
          if (encounterDate) {
              if (obs.getEncounter().getEncounterDatetime().compareTo(startDate) >= 0
                      && obs.getEncounter().getEncounterDatetime().compareTo(endDate) <= 0) {
                  returnedObs = obs;
                  break;
              }
          } else {
            if (obs.getValueDatetime().compareTo(startDate) >= 0
            && obs.getValueDatetime().compareTo(endDate) <= 0) {
        returnedObs = obs;
        break;
          }
      }
      return returnedObs;
  }
}



  private List<Obs> exclude(List<Obs> pure, List<Obs> dirty) {
    List<Obs> clean = new ArrayList<>();
    for (Obs o : pure) {
      if (!dirty.contains(o)) {
        clean.add(o);
      }
    }
    return clean;
  }
}
