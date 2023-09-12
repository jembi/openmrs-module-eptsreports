package org.openmrs.module.eptsreports.reporting.data.converter;

import org.openmrs.module.reporting.data.converter.DataConverter;

/** Converter to return the Concept Names of expected responses to given concepts */
public class ObservationToConceptNameConverter implements DataConverter {

  private final String newStart = "Novo Início";
  private final String restart = "Reinício";
  private final String inTarv = "Em TARV";
  private final String preTarv = "Pré-TARV";
  private final String chronicDiarrhea = "Diarréia Crónica";
  private final String fever = "Febre";
  private final String anemia = "Anemia";
  private final String pneumonia = "Pneumonia";
  private final String sarcomaKaposi = "Sarcoma de Kaposi (SK)";
  private final String meningitis = "Meningite";
  private final String gingivitis = "Gengivite";
  private final String estomatiteUlcerativaNecrotizante = "Estomatite Ulcerativa Necrotizante";
  private final String candidiaseOral = "Candidiase Oral";
  private final String cryptococcalMeningitis = "Meningite Criptocócica (MCC)";
  private final String cervicalCancer = "Cancer Cervical";
  private final String candidiaseEsofagica = "Candidiase Esofágica";
  private final String herpesSimples = "Hérpes Simples";
  private final String cachexia = "Caquexia";
  private final String toxoplasmose = "Toxoplasmose";
  private final String hivDiseaseResultingInEncephalopathy =
      "Doença pelo HIV Resultando em Encefalopatia";
  private final String extraPulmonaryTb = "Tuberculose Extrapulmonar";
  private final String pulmonaryTb = "Tuberculose Pulmonar";

  @Override
  public Object convert(Object original) {

    if (original == null) {
      return "N/A";
    }

    switch (original.toString()) {
      case "5018":
        return chronicDiarrhea;
      case "5945":
        return fever;
      case "3":
        return anemia;
      case "43":
        return pneumonia;
      case "1256":
        return newStart;
      case "1705":
        return restart;
      case "6276":
        return inTarv;
      case "6275":
        return preTarv;
      case "507":
        return sarcomaKaposi;
      case "60":
        return meningitis;
      case "126":
        return gingivitis;
      case "6783":
        return estomatiteUlcerativaNecrotizante;
      case "5334":
        return candidiaseOral;
      case "1294":
        return cryptococcalMeningitis;
      case "1570":
        return cervicalCancer;
      case "5340":
        return candidiaseEsofagica;
      case "5344":
        return herpesSimples;
      case "14656":
        return cachexia;
      case "7180":
        return toxoplasmose;
      case "6990":
        return hivDiseaseResultingInEncephalopathy;
      case "5042":
        return extraPulmonaryTb;
      case "42":
        return pulmonaryTb;
      default:
        return "";
    }
  }

  public Class<?> getInputDataType() {
    return null;
  }

  public Class<?> getDataType() {
    return null;
  }
}
