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
    CalculationResultMap patientMap = new CalculationResultMap();
    Location location = (Location) context.getFromCache("location");

    Date onOrBefore = (Date) context.getFromCache(ON_OR_BEFORE);

    if (onOrBefore != null) {

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
              e6,
              cohort,
              location,
              Arrays.asList(hivMetadata.getStartDrugs()),
              TimeQualifier.FIRST,
              null,
              onOrBefore,
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
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

      // A5 - FILT - Patients who have Regime de TPT with the values marked on the
      // first pick-up date
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

      /** ------ who completed IPT treatment--- */
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
              e6,
              cohort,
              location,
              Arrays.asList(hivMetadata.getCompletedConcept()),
              TimeQualifier.LAST,
              null,
              onOrBefore,
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
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

      // INH start or Continue
      CalculationResultMap isoniazidStartContinueMap =
          ePTSCalculationService.getObs(
              c6122,
              e6,
              cohort,
              location,
              Arrays.asList(c1256, c1257),
              TimeQualifier.ANY,
              null,
              onOrBefore,
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);

      // Other Perscriptions INH
      CalculationResultMap outrasPrescricoesINHMap =
          ePTSCalculationService.getObs(
              c1719,
              e6,
              cohort,
              location,
              Arrays.asList(c23955),
              TimeQualifier.ANY,
              null,
              onOrBefore,
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);

      // Other Perscriptions
      CalculationResultMap outrasPrescricoesExcludeINHMap =
          ePTSCalculationService.getObs(
              c1719,
              e6,
              cohort,
              location,
              null,
              TimeQualifier.ANY,
              null,
              onOrBefore,
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);

      // Type of Dispensation - Monthly (DM)
      CalculationResultMap filtTypeOfDispensationMonthlyMap =
          ePTSCalculationService.getObs(
              c23986,
              e60,
              cohort,
              location,
              Arrays.asList(c1098),
              TimeQualifier.ANY,
              null,
              onOrBefore,
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);

      // Type of Dispensation - Trimestral (DT)
      CalculationResultMap filtTypeOfDispensationTrimestralMap =
          ePTSCalculationService.getObs(
              c23986,
              e60,
              cohort,
              location,
              Arrays.asList(c23720),
              TimeQualifier.ANY,
              null,
              onOrBefore,
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);

      /** START and END 3HP Treatment */

      // C1 and D part 1

      CalculationResultMap start3HPMap =
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

      // C2
      CalculationResultMap regimeTPTStart3HPMap =
          ePTSCalculationService.getObs(
              c23985,
              e60,
              cohort,
              location,
              Arrays.asList(c23954, c23984),
              TimeQualifier.ANY,
              null,
              onOrBefore,
              EPTSMetadataDatetimeQualifier.ENCOUNTER_DATETIME,
              context);

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

        Obs startTPTFilt =
            EptsCalculationUtils.obsResultForPatient(regimeTPT1stPickUpMap, patientId);

        List<Obs> outrasPrescricoesCleanObsList =
            getObsListFromResultMap(outrasPrescricoesExcludeINHMap, patientId);

        List<Obs> outrasPrescricoesINHObsList =
            getObsListFromResultMap(outrasPrescricoesINHMap, patientId);

        // ipt end date section
        Obs endProfilaxiaObs6 =
            EptsCalculationUtils.obsResultForPatient(endProfilaxiaObservations6, patientId);
        Obs endProfilaxiaObs53 =
            EptsCalculationUtils.obsResultForPatient(endProfilaxiaObservations53, patientId);
        Obs endProfilaxiaObs9 =
            EptsCalculationUtils.obsResultForPatient(endProfilaxiaObservations9, patientId);
        Obs endDrugsObs =
            EptsCalculationUtils.obsResultForPatient(completedDrugsObservations, patientId);

        if (startProfilaxiaObs53 == null
            && startProfilaxiaObs6 == null
            && startProfilaxiaObs9 == null
            && startDrugsObs == null) {
          continue;
        }

        // A1 AND (B1 OR B2 OR B3 OR B4)
        for (Obs a1 : Arrays.asList(startProfilaxiaObs53)) {
          Obs b1 =
              returnObsBetweenIptStartDateAndIptEndDate(
                  Arrays.asList(endProfilaxiaObs53), a1.getValueDatetime(), 173, 365, false);

          Obs b2 =
              returnObsBetweenIptStartDateAndIptEndDate(
                  Arrays.asList(endDrugsObs), a1.getValueDatetime(), 173, 365, true);

          Obs b3 =
              returnObsBetweenIptStartDateAndIptEndDate(
                  Arrays.asList(endProfilaxiaObs6), a1.getValueDatetime(), 173, 365, true);

          Obs b4 =
              returnObsBetweenIptStartDateAndIptEndDate(
                  Arrays.asList(endProfilaxiaObs9), a1.getValueDatetime(), 173, 365, true);

          if (b1 != null || b2 != null || b3 != null || b4 != null) {
            patientMap.put(patientId, new BooleanResult(true, this));
            continue;
          }
        }

        for (Obs a2 : Arrays.asList(startDrugsObs)) {
          Obs b1 =
              returnObsBetweenIptStartDateAndIptEndDate(
                  Arrays.asList(endProfilaxiaObs53),
                  a2.getEncounter().getEncounterDatetime(),
                  173,
                  365,
                  false);

          Obs b2 =
              returnObsBetweenIptStartDateAndIptEndDate(
                  Arrays.asList(endDrugsObs),
                  a2.getEncounter().getEncounterDatetime(),
                  173,
                  365,
                  true);

          Obs b3 =
              returnObsBetweenIptStartDateAndIptEndDate(
                  Arrays.asList(endProfilaxiaObs6),
                  a2.getEncounter().getEncounterDatetime(),
                  173,
                  365,
                  true);

          Obs b4 =
              returnObsBetweenIptStartDateAndIptEndDate(
                  Arrays.asList(endProfilaxiaObs9),
                  a2.getEncounter().getEncounterDatetime(),
                  173,
                  365,
                  true);

          // B5 is a obs list made of
          CalculationResultMap b5Map = new CalculationResultMap();

          List<Obs> cleanList =
              this.excludeObs(outrasPrescricoesCleanObsList, outrasPrescricoesINHObsList);

          int evaluateINHOccurrences6 =
              evaluateOccurrence(
                  getObsListFromResultMap(isoniazidStartContinueMap, patientId),
                  cleanList,
                  a2.getEncounter().getEncounterDatetime(),
                  6,
                  7);

          int evaluateINHOccurences2 =
              evaluateOccurrence(
                  getObsListFromResultMap(isoniazidStartContinueMap, patientId),
                  cleanList,
                  a2.getEncounter().getEncounterDatetime(),
                  2,
                  5);

          int evaluateINHOccurrences3 =
              evaluateOccurrence(
                  getObsListFromResultMap(isoniazidStartContinueMap, patientId),
                  cleanList,
                  a2.getEncounter().getEncounterDatetime(),
                  3,
                  7);

          if (evaluateINHOccurrences6 >= 6
              && evaluateINHOccurences2 >= 2
              && evaluateINHOccurrences3 >= 3) {
            b5Map.put(patientId, new BooleanResult(true, this));
          }

          List<Obs> b5 = getObsListFromResultMap(b5Map, patientId);

          if (b1 != null || b2 != null || b3 != null || b4 != null || b5 != null) {
            patientMap.put(patientId, new BooleanResult(true, this));
            continue;
          }
        }

        for (Obs a3 : Arrays.asList(startProfilaxiaObs6)) {
          Obs b1 =
              returnObsBetweenIptStartDateAndIptEndDate(
                  Arrays.asList(endProfilaxiaObs53), a3.getValueDatetime(), 173, 365, false);

          Obs b2 =
              returnObsBetweenIptStartDateAndIptEndDate(
                  Arrays.asList(endDrugsObs), a3.getValueDatetime(), 173, 365, true);

          Obs b3 =
              returnObsBetweenIptStartDateAndIptEndDate(
                  Arrays.asList(endProfilaxiaObs6), a3.getValueDatetime(), 173, 365, true);

          Obs b4 =
              returnObsBetweenIptStartDateAndIptEndDate(
                  Arrays.asList(endProfilaxiaObs9), a3.getValueDatetime(), 173, 365, true);

          // B5 is a obs list made of
          CalculationResultMap b5Map = new CalculationResultMap();

          List<Obs> cleanList =
              this.excludeObs(outrasPrescricoesCleanObsList, outrasPrescricoesINHObsList);

          int evaluateINHOccurrences6 =
              evaluateOccurrence(
                  getObsListFromResultMap(isoniazidStartContinueMap, patientId),
                  cleanList,
                  a3.getValueDatetime(),
                  6,
                  7);

          int evaluateINHOccurences2 =
              evaluateOccurrence(
                  getObsListFromResultMap(isoniazidStartContinueMap, patientId),
                  cleanList,
                  a3.getValueDatetime(),
                  2,
                  5);

          int evaluateINHOccurrences3 =
              evaluateOccurrence(
                  getObsListFromResultMap(isoniazidStartContinueMap, patientId),
                  cleanList,
                  a3.getValueDatetime(),
                  3,
                  7);

          if (evaluateINHOccurrences6 >= 6
              && evaluateINHOccurences2 >= 2
              && evaluateINHOccurrences3 >= 3) {
            b5Map.put(patientId, new BooleanResult(true, this));
          }

          List<Obs> b5 = getObsListFromResultMap(b5Map, patientId);

          if (b1 != null || b2 != null || b3 != null || b4 != null || b5 != null) {
            patientMap.put(patientId, new BooleanResult(true, this));
            continue;
          }
        }

        for (Obs a4 : Arrays.asList(startProfilaxiaObs9)) {
          Obs b1 =
              returnObsBetweenIptStartDateAndIptEndDate(
                  Arrays.asList(endProfilaxiaObs53), a4.getValueDatetime(), 173, 365, false);

          Obs b2 =
              returnObsBetweenIptStartDateAndIptEndDate(
                  Arrays.asList(endDrugsObs), a4.getValueDatetime(), 173, 365, true);

          Obs b3 =
              returnObsBetweenIptStartDateAndIptEndDate(
                  Arrays.asList(endProfilaxiaObs6), a4.getValueDatetime(), 173, 365, true);

          Obs b4 =
              returnObsBetweenIptStartDateAndIptEndDate(
                  Arrays.asList(endProfilaxiaObs9), a4.getValueDatetime(), 173, 365, true);

          if (b1 != null || b2 != null || b3 != null || b4 != null) {
            patientMap.put(patientId, new BooleanResult(true, this));
            continue;
          }
        }

        for (Obs a5 : Arrays.asList(startTPTFilt)) {
          Obs b1 =
              returnObsBetweenIptStartDateAndIptEndDate(
                  Arrays.asList(endProfilaxiaObs53),
                  a5.getEncounter().getEncounterDatetime(),
                  173,
                  365,
                  false);

          Obs b2 =
              returnObsBetweenIptStartDateAndIptEndDate(
                  Arrays.asList(endDrugsObs),
                  a5.getEncounter().getEncounterDatetime(),
                  173,
                  365,
                  true);

          Obs b3 =
              returnObsBetweenIptStartDateAndIptEndDate(
                  Arrays.asList(endProfilaxiaObs6),
                  a5.getEncounter().getEncounterDatetime(),
                  173,
                  365,
                  true);

          Obs b4 =
              returnObsBetweenIptStartDateAndIptEndDate(
                  Arrays.asList(endProfilaxiaObs9),
                  a5.getEncounter().getEncounterDatetime(),
                  173,
                  365,
                  true);

          // B6 is a obs list made of
          CalculationResultMap b6Map = new CalculationResultMap();

          int evaluateRegimeTPTOccurrences6 =
              evaluateOccurrence(
                  getObsListFromResultMap(regimeTPT1stPickUpMap, patientId),
                  getObsListFromResultMap(filtTypeOfDispensationMonthlyMap, patientId),
                  a5.getEncounter().getEncounterDatetime(),
                  6,
                  7);

          int evaluateRegimeTPTOccurrences2 =
              evaluateOccurrence(
                  getObsListFromResultMap(regimeTPT1stPickUpMap, patientId),
                  getObsListFromResultMap(filtTypeOfDispensationTrimestralMap, patientId),
                  a5.getEncounter().getEncounterDatetime(),
                  2,
                  7);

          int evaluateRegimeTPTOccurrences3DM =
              evaluateOccurrence(
                  getObsListFromResultMap(regimeTPT1stPickUpMap, patientId),
                  getObsListFromResultMap(filtTypeOfDispensationMonthlyMap, patientId),
                  a5.getEncounter().getEncounterDatetime(),
                  3,
                  7);

          int evaluateRegimeTPTOccurrences3DT =
              evaluateOccurrence(
                  getObsListFromResultMap(regimeTPT1stPickUpMap, patientId),
                  getObsListFromResultMap(filtTypeOfDispensationTrimestralMap, patientId),
                  a5.getEncounter().getEncounterDatetime(),
                  3,
                  7);

          if (evaluateRegimeTPTOccurrences6 >= 6
              && evaluateRegimeTPTOccurrences2 >= 2
              && evaluateRegimeTPTOccurrences3DM >= 3
              && evaluateRegimeTPTOccurrences3DT >= 3) {
            b6Map.put(patientId, new BooleanResult(true, this));
          }

          List<Obs> b6 = getObsListFromResultMap(b6Map, patientId);

          if (b1 != null || b2 != null || b3 != null || b4 != null || b6 != null) {
            patientMap.put(patientId, new BooleanResult(true, this));
            continue;
          }
        }

        /* 3HP */
        Obs start3HPObs = EptsCalculationUtils.obsResultForPatient(start3HPMap, patientId);

        Obs regimeTPT3HPObs =
            EptsCalculationUtils.obsResultForPatient(regimeTPTStart3HPMap, patientId);

        // for each startDate 3HP obs

        for (Obs c1 : Arrays.asList(start3HPObs)) {

          int atLeastThree3HPOccurrence =
              evaluateOccurrenceSingleList(
                  getObsListFromResultMap(start3HPMap, patientId),
                  c1.getEncounter().getEncounterDatetime(),
                  3,
                  4);

          if (atLeastThree3HPOccurrence >= 3) {
            patientMap.put(patientId, new BooleanResult(true, this));
            continue;
          }
        }

        for (Obs c2 : Arrays.asList(regimeTPT3HPObs)) {

          int atLeast1FILT3HPTrimestralsOccurence =
              evaluateOccurrence(
                  getObsListFromResultMap(regimeTPTStart3HPMap, patientId),
                  getObsListFromResultMap(filtTypeOfDispensationTrimestralMap, patientId),
                  c2.getEncounter().getEncounterDatetime(),
                  1,
                  4);

          int atleast3FILTS3HPTrimestralOccurencies =
              evaluateOccurrence(
                  getObsListFromResultMap(regimeTPTStart3HPMap, patientId),
                  getObsListFromResultMap(filtTypeOfDispensationTrimestralMap, patientId),
                  c2.getEncounter().getEncounterDatetime(),
                  3,
                  4);

          if (atLeast1FILT3HPTrimestralsOccurence >= 1
              || atleast3FILTS3HPTrimestralOccurencies >= 3) {
            patientMap.put(patientId, new BooleanResult(true, this));
            continue;
          }
        }
      }

      return patientMap;

    } else {
      throw new IllegalArgumentException(
          String.format("Parameters %s and %s must be set", ON_OR_BEFORE));
    }
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

  private List<Obs> getObsListFromResultMap(CalculationResultMap map, Integer pid) {
    List<Obs> obss = new ArrayList<>();
    if (map.get(pid) instanceof ListResult) {

      ListResult listResult = (ListResult) map.get(pid);
      obss = EptsCalculationUtils.extractResultValues(listResult);
      obss.removeAll(Collections.singleton(null));
    }

    return obss;
  }

  /**
   * Given 1 list of obs the method extracts the encounter list , and then check for each encounter
   * in the list evaluates if it reocurred ntimes during period.
   *
   * @param obss
   * @param iptStartDate
   * @param nTimes
   * @param plusIPTDate
   * @return
   */
  private int evaluateOccurrenceSingleList(
      List<Obs> obss, Date iptStartDate, int nTimes, int plusIPTDate) {
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
   * Given iptStartDate evaluate if Obs is between IPT startDate plus some number of days and IPT
   * and some number of days
   *
   * @param a list of OBs
   * @param iptStartDate
   * @param plusIPTStartDate
   * @param plusIPTEndDate
   * @return
   */
  private Obs returnObsBetweenIptStartDateAndIptEndDate(
      List<Obs> a,
      Date iptStartDate,
      int plusIPTStartDate,
      int plusIPTEndDate,
      boolean encounterDate) {

    Obs returnedObs = null;

    if (iptStartDate == null) {
      return returnedObs;
    }

    Date startDate = DateUtils.addDays(iptStartDate, plusIPTStartDate);
    Date endDate = DateUtils.addDays(iptStartDate, plusIPTEndDate);

    for (Obs obs : a) {
      if (obs != null) {
        if (encounterDate) {
          if (obs.getEncounter() != null) {
            if (obs.getEncounter().getEncounterDatetime().compareTo(startDate) >= 0
                && obs.getEncounter().getEncounterDatetime().compareTo(endDate) <= 0) {
              returnedObs = obs;
              break;
            }
          }
        } else {
          if (obs.getValueDatetime() != null) {
            if (obs.getValueDatetime().compareTo(startDate) >= 0
                && obs.getValueDatetime().compareTo(endDate) <= 0) {
              returnedObs = obs;
              break;
            }
          }
        }
      }
    }
    return returnedObs;
  }

  private List<Obs> excludeObs(List<Obs> pure, List<Obs> dirty) {
    List<Obs> clean = new ArrayList<>();
    for (Obs o : pure) {
      if (!dirty.contains(o)) {
        clean.add(o);
      }
    }
    return clean;
  }
}
