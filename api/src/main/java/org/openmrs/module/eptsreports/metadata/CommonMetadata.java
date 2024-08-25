/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.eptsreports.metadata;

import org.openmrs.Concept;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Component;

@Component("commonMetadata")
public class CommonMetadata extends Metadata {

  protected String gpTransferFromOtherFacilityConceptUuid =
      "eptsreports.transferFromOtherFacilityConceptUuid";

  /**
   * <b>concept_id = 664</b>
   *
   * <p><b>Name:</b> NEGATIVE
   *
   * <p><b>Description:</b> General finding of a negative result.
   *
   * @return {@link Concept}
   */
  public Concept getNegative() {
    String uuid = Context.getAdministrationService().getGlobalProperty("eptsreports.NegativeUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 703</b>
   *
   * <p><b>Name:</b> POSITIVE
   *
   * <p><b>Description:</b> General finding of a positive result.
   *
   * @return {@link Concept}
   */
  public Concept getPositive() {
    String uuid = Context.getAdministrationService().getGlobalProperty("eptsreports.positiveUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 1065</b>
   *
   * <p><b>Name:</b> YES
   *
   * <p><b>Description:</b> Generic answer to a question.
   *
   * @return {@link Concept}
   */
  public Concept getYesConcept() {
    String uuid =
        Context.getAdministrationService().getGlobalProperty("eptsreports.yesConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 1066</b>
   *
   * <p><b>Name:</b> NO
   *
   * <p><b>Description:</b> Generic answer to a question.
   *
   * @return {@link Concept}
   */
  public Concept getNoConcept() {
    String uuid = Context.getAdministrationService().getGlobalProperty("eptsreports.noConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 1256</b>
   *
   * <p><b>Name:</b> START DRUGS
   *
   * <p><b>Description:</b> Answer on encounter form. Implies that a patient will be started on
   * drugs for that particular encounter.
   *
   * @return {@link Concept}
   */
  public Concept getStartDrugsConcept() {
    String uuid =
        Context.getAdministrationService().getGlobalProperty("eptsreports.startDrugsConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 1369</b>
   *
   * <p><b>Name:</b> TRANSFER FROM OTHER FACILITY
   *
   * <p><b>Description:</b>
   *
   * @return {@link Concept}
   */
  public Concept getTransferFromOtherFacilityConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty(gpTransferFromOtherFacilityConceptUuid);
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 1706</b>
   *
   * <p><b>Name:</b> TRANSFER OUT TO ANOTHER FACILITY
   *
   * <p><b>Description:</b>
   *
   * @return {@link Concept}
   */
  public Concept getTransferOutToAnotherFacilityConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.transferOutToAnotherFacilityConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 1190</b>
   *
   * <p><b>Name:</b> HISTORICAL DRUG START DATE
   *
   * <p><b>Description:</b>
   *
   * @return {@link Concept}
   */
  public Concept getHistoricalDrugStartDateConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.historicalStartDateConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 1982</b>
   *
   * <p><b>Name:</b> PREGANT
   *
   * <p><b>Description:</b>
   *
   * @return {@link Concept}
   */
  public Concept getPregnantConcept() {
    String uuid =
        Context.getAdministrationService().getGlobalProperty("eptsreports.pregnantConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 1279</b>
   *
   * <p><b>Name:</b> NUMBER OF WEEKS PREGNANT
   *
   * <p><b>Description:</b>
   *
   * @return {@link Concept}
   */
  public Concept getNumberOfWeeksPregnant() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.numberOfWeeksPregnantConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 1600</b>
   *
   * <p><b>Name:</b> PREGNANCY DUE DATE
   *
   * <p><b>Description:</b>
   *
   * @return {@link Concept}
   */
  public Concept getPregnancyDueDate() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.pregnancyDueDateConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 6332</b>
   *
   * <p><b>Name:</b> BREASTFEEDING
   *
   * <p><b>Description:</b> Describes the secretion of milk from the mammary glands and the period
   * of time that a mother lactates to feed her young
   *
   * @return {@link Concept}
   */
  public Concept getBreastfeeding() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.breastfeedingConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 1410</b>
   *
   * <p><b>Name:</b> RETURN VISIT DATE
   *
   * <p><b>Description:</b>
   *
   * @return {@link Concept}
   */
  public Concept getReturnVisitDateConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.returnVisitDateConceptConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 5599</b>
   *
   * <p><b>Name:</b> PRIOR DELIVERY DATE
   *
   * <p><b>Description:</b> Date in which a mother delivered her child.
   *
   * @return {@link Concept}
   */
  public Concept getPriorDeliveryDateConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.priorDeliveryDateConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 6258</b>
   *
   * <p><b>Name:</b> SCREENING FOR STI
   *
   * <p><b>Description:</b>
   *
   * @return {@link Concept}
   */
  public Concept getScreeningForSTIConcept() {
    String uuid =
        Context.getAdministrationService().getGlobalProperty("eptsreports.screeningForSTIIuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 2066</b>
   *
   * <p><b>Name:</b> STAGE 4 PEDIATRIC, MOZAMBIQUE
   *
   * <p><b>Description:</b>
   *
   * @return {@link Concept}
   */
  public Concept getStage4PediatricMozambiqueConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.stage4PediatricMozambiqueUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 1670</b>
   *
   * <p><b>Name:</b> ADULT CLINICAL HISTORY
   *
   * <p><b>Description:</b>
   *
   * @return {@link Concept}
   */
  public Concept getAdultClinicalHistoryConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.adultClinicalHistoryUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 1120</b>
   *
   * <p><b>Name:</b> SKIN EXAM FINDINGS
   *
   * <p><b>Description:</b>
   *
   * @return {@link Concept}
   */
  public Concept getSkimExamFindingsConcept() {
    String uuid =
        Context.getAdministrationService().getGlobalProperty("eptsreports.skimExamFindingsUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 1127</b>
   *
   * <p><b>Name:</b> EXTREMITY EXAM FINDINGS
   *
   * <p><b>Description:</b>
   *
   * @return {@link Concept}
   */
  public Concept getExtremityExamFindingsConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.extremityExamFindingsUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 1569</b>
   *
   * <p><b>Name:</b> STAGE 4 ADULT, MOZAMBIQUE
   *
   * <p><b>Description:</b>
   *
   * @return {@link Concept}
   */
  public Concept getStateAdultMozambiqueConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.stateAdultMozambiqueUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 507</b>
   *
   * <p><b>Name:</b> KAPOSI'S SARCOMA
   *
   * <p><b>Description:</b>
   *
   * @return {@link Concept}
   */
  public Concept getKaposiSarcomaConcept() {
    String uuid =
        Context.getAdministrationService().getGlobalProperty("eptsreports.kaposiSarcomaUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 6336</b>
   *
   * <p><b>Name:</b> CLASSIFICATION OF MALNUTRITION
   *
   * <p><b>Description:</b>
   *
   * @return {@link Concept}
   */
  public Concept getClassificationOfMalnutritionConcept() {
    return getConcept(
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.classificationOfMalnutritionConceptUuid"));
  }

  /**
   * <b>concept_id = 6335</b>
   *
   * <p><b>Name:</b> MALNUTRITION LIGHT
   *
   * <p><b>Description:</b> Slightly malnourished
   *
   * @return {@link Concept}
   */
  public Concept getMalnutritionLightConcept() {
    return getConcept(
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.malnutritionLightConceptUuid"));
  }

  /**
   * <b>concept_id = 68</b>
   *
   * <p><b>Name:</b> MALNUTRITION
   *
   * <p><b>Description:</b> Inadequate oral intake of unspecified nutrients (eg, calories, protein,
   * vitamins, etc.)
   *
   * @return {@link Concept}
   */
  public Concept getMalnutritionConcept() {
    return getConcept(
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.malnutritionConceptUuid"));
  }

  /**
   * <b>concept_id = 1844</b>
   *
   * <p><b>Name:</b> CHRONIC MALNUTRITION
   *
   * <p><b>Description:</b> Slightly malnourished
   *
   * @return {@link Concept}
   */
  public Concept getChronicMalnutritionConcept() {
    return getConcept(
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.chronicMalnutritionConceptUuid"));
  }

  /**
   * <b>concept_id = 6258</b>
   *
   * <p><b>Name:</b> SCREENING FOR STI
   *
   * <p><b>Description:</b> Slightly malnourished
   *
   * @return {@link Concept}
   */
  public Concept getStiScreeningConcept() {
    String uuid =
        Context.getAdministrationService().getGlobalProperty("eptsreports.stiScreeningConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 6126</b>
   *
   * <p><b>Name:</b> COTRIMOXAZOLE PROPHYLAXIS START DATE
   *
   * <p><b>Description:</b>
   *
   * @return {@link Concept}
   */
  public Concept getCotrimoxazoleProphylaxisStartDateConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.cotrimoxazoleProphylaxisStartDateConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 2152</b>
   *
   * <p><b>Name:</b> NUTRITIONAL SUPPLEMENT
   *
   * <p><b>Description:</b>
   *
   * @return {@link Concept}
   */
  public Concept getNutritionalSupplememtConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.nutritionalSupplememtConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 6143</b>
   *
   * <p><b>Name:</b> ATPU
   *
   * <p><b>Description:</b>
   *
   * @return {@link Concept}
   */
  public Concept getATPUSupplememtConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.ATPUSupplememtConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 2151</b>
   *
   * <p><b>Name:</b> Soja
   *
   * <p><b>Description:</b>
   *
   * @return {@link Concept}
   */
  public Concept getSojaSupplememtConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.sojaSupplememtConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 23898</b>
   *
   * <p><b>Name:</b> Alternativa de Linha de Tratamento Conjunto
   *
   * <p><b>Description:</b>
   *
   * @return {@link Concept}
   */
  public Concept getAlternativeLineConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.alternativeLineConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 23741</b>
   *
   * <p><b>Name:</b> Alternativa de Primeira Linha de Tratamento
   *
   * <p><b>Description:</b>
   *
   * @return {@link Concept}
   */
  public Concept getAlternativeFirstLineConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.alternativeFirstLineConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 1371</b>
   *
   * <p><b>Name:</b> Mudanca de Regime
   *
   * <p><b>Description:</b>
   *
   * @return {@link Concept}
   */
  public Concept getRegimeChangeConcept() {
    String uuid =
        Context.getAdministrationService().getGlobalProperty("eptsreports.regimeChangeConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 21190</b>
   *
   * <p><b>Name:</b> REGIME ARV ALTERNATIVO A PRIMEIRA LINHA
   *
   * <p><b>Description:</b>
   *
   * @return {@link Concept}
   */
  public Concept getRegimenAlternativeToFirstLineConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.regimenAlternativeToFirstLineConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 1138</b>
   *
   * <p><b>Name:</b> INDETERMINATE
   *
   * <p><b>Description: </b>
   *
   * @return {@link Concept}
   */
  public Concept getIndeterminate() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.indeterminateConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 23956</b>
   *
   * <p><b>Name:</b> SUGESTIVE
   *
   * <p><b>Description:</b>
   *
   * @return {@link Concept}
   */
  public Concept getSugestive() {
    String uuid =
        Context.getAdministrationService().getGlobalProperty("eptsreports.sugestiveConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 23821</b>
   *
   * <p><b>Name:</b> Sample collection date and time
   *
   * <p><b>Description:</b>
   *
   * @return {@link Concept}
   */
  public Concept getSampleCollectionDateAndTime() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.sampleCollectionDateAndTimeUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 23821</b>
   *
   * <p><b>Name:</b> Sample collection date and time
   *
   * <p><b>Description:</b>
   *
   * @return {@link Concept}
   */
  public Concept getNotFoundConcept() {
    String uuid =
        Context.getAdministrationService().getGlobalProperty("eptsreports.notFoundConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 1342</b>
   *
   * <p><b>Name:</b> BODY MASS INDEX
   *
   * <p><b>Description: Body Mass Index (BMI) is a relationship between weight and height that is
   * associated with body fat and health risk. The equation is BMI = body weight in kilograms/height
   * in meters squared. </b>
   *
   * @return {@link Concept}
   */
  public Concept getBodyMassIndexConcept() {
    String uuid =
        Context.getAdministrationService().getGlobalProperty("eptsreports.bmiConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 165185</b>
   *
   * <p><b>Name:</b>POSITIVITY LEVEL
   *
   * <p><b>Description: POSITIVITY LEVEL Concept
   *
   * @return {@link Concept}
   */
  public Concept getPositivityLevelConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.positivityLevelConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 165348</b>
   *
   * <p><b>Name:</b>4+
   *
   * <p><b>Description: 4+ Concept
   *
   * @return {@link Concept}
   */
  public Concept getFourPlusConcept() {
    String uuid =
        Context.getAdministrationService().getGlobalProperty("eptsreports.fourPlusConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 165188</b>
   *
   * <p><b>Name:</b>3+
   *
   * <p><b>Description: 3+ Concept
   *
   * @return {@link Concept}
   */
  public Concept getThreePlusConcept() {
    String uuid =
        Context.getAdministrationService().getGlobalProperty("eptsreports.threePlusConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 1343</b>
   *
   * <p><b>Name:</b> MID-UPPER ARM CIRCUMFERENCE
   *
   * <p><b>Description: MUAC is the circumference of the left upper arm, measured at the mid-point
   * between the tip of the shoulder and the tip of the elbow. MUAC is useful for the assessment of
   * nutritional status. </b>
   *
   * @return {@link Concept}
   */
  public Concept getMidUpperArmCircumferenceConcept() {
    String uuid =
        Context.getAdministrationService().getGlobalProperty("eptsreports.muacConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 165187</b>
   *
   * <p><b>Name:</b>2+
   *
   * <p><b>Description: 2+ Concept
   *
   * @return {@link Concept}
   */
  public Concept getTwoPlusConcept() {
    String uuid =
        Context.getAdministrationService().getGlobalProperty("eptsreports.twoPlusConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 165186</b>
   *
   * <p><b>Name:</b>1+
   *
   * <p><b>Description: 1+ Concept
   *
   * @return {@link Concept}
   */
  public Concept getOnePlusConcept() {
    String uuid =
        Context.getAdministrationService().getGlobalProperty("eptsreports.onePlusConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>person_attribute_type_id = 50</b>
   *
   * <p><b>Name:</b> Data de Inscrição no OVC
   *
   * <p><b>Description: </b>
   *
   * @return {@link PersonAttributeType}
   */
  public PersonAttributeType getOVCDataInscricaoPersonAttributeType() {
    String uuid =
        Context.getAdministrationService().getGlobalProperty("eptsreports.dataInscricaoOVCUuid");
    return getPersonAttributeType(uuid);
  }

  /**
   * <b>person_attribute_type_id = 51</b>
   *
   * <p><b>Name:</b> Data de Saída do OVC
   *
   * <p><b>Description: </b>
   *
   * @return {@link PersonAttributeType}
   */
  public PersonAttributeType getOVCDataSaidaPersonAttributeType() {
    String uuid =
        Context.getAdministrationService().getGlobalProperty("eptsreports.dataSaidaOVCUuid");
    return getPersonAttributeType(uuid);
  }

  /**
   * <b>person_attribute_type_id = 51</b>
   *
   * <p><b>Name:</b> Data de Saída do OVC
   *
   * <p><b>Description: </b>
   *
   * @return {@link PersonAttributeType}
   */
  public PersonAttributeType getOVCEstadoBeneficiarioPersonAttributeType() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.estadoDoBeneficiarioOVCUuid");
    return getPersonAttributeType(uuid);
  }

  /**
   * <b>person_attribute_type_id = 165475</b>
   *
   * <p><b>Name:</b> ACTIVO
   *
   * <p><b>Description: Beneficiário do OVC com estado ACTIVO</b>
   *
   * @return {@link Concept}
   */
  public Concept getOVCActivoConcept() {
    String uuid = Context.getAdministrationService().getGlobalProperty("eptsreports.activoOVCUuid");
    return getConcept(uuid);
  }

  /**
   * <b>person_attribute_type_id = 165472</b>
   *
   * <p><b>Name:</b> ACTIVO
   *
   * <p><b>Description: Beneficiário do OVC com estado GRADUADO</b>
   *
   * @return {@link Concept}
   */
  public Concept getOVCGraduadoConcept() {
    String uuid =
        Context.getAdministrationService().getGlobalProperty("eptsreports.graduadoOVCUuid");
    return getConcept(uuid);
  }

  /**
   * <b>person_attribute_type_id = 165473</b>
   *
   * <p><b>Name:</b> ACTIVO
   *
   * <p><b>Description: Beneficiário do OVC saida sem graduação</b>
   *
   * @return {@link Concept}
   */
  public Concept getOVCSaidaSemGraduacaoConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.saidaSemGraduacaoOVCUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 1874</b>
   *
   * <p><b>Name:</b>MOTIVO DE CONSULTA CRIANCA EM RISCO
   *
   * <p><b>Description: MOTIVO DE CONSULTA CRIANCA EM RISCO
   *
   * @return {@link Concept}
   */
  public Concept getMotivoConsultaCriancaRiscoConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.motivoConsultaCriancaRiscoConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 1842</b>
   *
   * <p><b>Name:</b>PREMATURIDADE/ RN BAIXO PESO
   *
   * <p><b>Description: PREMATURIDADE/ RN BAIXO PESO
   *
   * @return {@link Concept}
   */
  public Concept getPrematuridadeConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.prematuridadeConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 1586</b>
   *
   * <p><b>Name:</b>RECÉN NASCIDO DE MÃE HIV POSITIVO
   *
   * <p><b>Description: RECÉN NASCIDO DE MÃE HIV POSITIVO
   *
   * @return {@link Concept}
   */
  public Concept getRecenNascidoMaeHivPositivoConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.recenNascidoMaeHivPositivoConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 1843</b>
   *
   * <p><b>Name:</b>DESMAME BRUSCO E/OU ALEITAMENTO ARTIFICIAL EXCLUSIVO < 1 ANO
   *
   * <p><b>Description: DESMAME BRUSCO E/OU ALEITAMENTO ARTIFICIAL EXCLUSIVO < 1 ANO
   *
   * @return {@link Concept}
   */
  public Concept getDesmameBruscoAleitamentoArtificalConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.desmameBruscoAleitamentoArtificalConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 1845</b>
   *
   * <p><b>Name:</b CONTACTO TB
   *
   * <p><b>Description: CONTACTO TB
   *
   * @return {@link Concept}
   */
  public Concept getContactoTbConcept() {
    String uuid =
        Context.getAdministrationService().getGlobalProperty("eptsreports.contactoTbConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 1846</b>
   *
   * <p><b>Name:</b GÉMEOS
   *
   * <p><b>Description: GÉMEOS
   *
   * @return {@link Concept}
   */
  public Concept getTwinsConcept() {
    String uuid =
        Context.getAdministrationService().getGlobalProperty("eptsreports.twinsConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 5050</b>
   *
   * <p><b>Name:</b FALÊNCIA DE CRESCIMENTO
   *
   * <p><b>Description: FALÊNCIA DE CRESCIMENTO
   *
   * @return {@link Concept}
   */
  public Concept getFalenciaDeCrescimentoConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.falenciaDeCrescimentoConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 6397</b>
   *
   * <p><b>Name:</b PESO AO NASCER INFERIOR A 2.5 KG
   *
   * <p><b>Description: PESO AO NASCER INFERIOR A 2.5 KG
   *
   * @return {@link Concept}
   */
  public Concept getPesoInferior2dot5KgConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.pesoInferior2dot5KgConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 6409</b>
   *
   * <p><b>Name:</b MIGRAÇÃO RECENTE DA FAMILIA
   *
   * <p><b>Description: MIGRAÇÃO RECENTE DA FAMILIA
   *
   * @return {@link Concept}
   */
  public Concept getMigracaoRecenteFamiliaConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.migracaoRecenteFamiliaConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 1847</b>
   *
   * <p><b>Name:</b> CRIANÇA COM MÃE AUSENTE
   *
   * <p><b>Description:CRIANÇA COM MÃE AUSENTE
   *
   * @return {@link Concept}
   */
  public Concept getCriancaMaeAusenteConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.criancaMaeAusenteConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 1477</b>
   *
   * <p><b>Name:</b> NOME DE MAE
   *
   * <p><b>Description:NOME DE MAE
   *
   * @return {@link Concept}
   */
  public Concept getMothersNameConcept() {
    String uuid =
        Context.getAdministrationService().getGlobalProperty("eptsreports.mothersNameConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 2071</b>
   *
   * <p><b>Name:</b> ACCEPTS HOME VISIT
   *
   * <p><b>Description:ACCEPTS HOME VISIT
   *
   * @return {@link Concept}
   */
  public Concept getAcceptsHomeVisitConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.acceptsHomeVisitConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 165502</b>
   *
   * <p><b>Name:</b> Sample - Early Infant Diagnosis
   *
   * <p><b>Description:</b> Sample - EID
   *
   * @return {@link Concept}
   */
  public Concept getSampleEarlyInfantDiagnosisConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.sampleEarlyInfantDiagnosisConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 165503</b>
   *
   * <p><b>Name:</b> First Sample < 9 months
   *
   * <p><b>Description:</b> First sample In Less Than 9 months
   *
   * @return {@link Concept}
   */
  public Concept getFirstSampleInLessThan9MonthsConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.firstSampleInLessThan9MonthsConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 165506</b>
   *
   * <p><b>Name:</b> Next Sample < 9 months
   *
   * <p><b>Description:</b> Next Sample In Less Than 9 months
   *
   * @return {@link Concept}
   */
  public Concept getNextSampleInLessThan9MonthsConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.nextSampleInLessThan9MonthsConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 165507</b>
   *
   * <p><b>Name:</b> First sample 9-17 months
   *
   * <p><b>Description:</b> First sample Between 9th to 17th month
   *
   * @return {@link Concept}
   */
  public Concept getFirstSampleBetween9To17MonthsConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.firstSampleBetween9To17MonthsConceptUuid");
    return getConcept(uuid);
  }

  /**
   * <b>concept_id = 165510</b>
   *
   * <p><b>Name:</b> Following sample 9-17 months
   *
   * <p><b>Description:</b> Following sample Between 9th to 17th month
   *
   * @return {@link Concept}
   */
  public Concept getFollowingtSampleBetween9To17MonthsConcept() {
    String uuid =
        Context.getAdministrationService()
            .getGlobalProperty("eptsreports.followingSampleBetween9To17MonthsConceptUuid");
    return getConcept(uuid);
  }
}
