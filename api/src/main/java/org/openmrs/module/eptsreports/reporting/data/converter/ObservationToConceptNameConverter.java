package org.openmrs.module.eptsreports.reporting.data.converter;

import org.openmrs.module.reporting.data.converter.DataConverter;

/** Converter to return the Concept Names of expected responses to given concepts */
public class ObservationToConceptNameConverter implements DataConverter {

  final String newStart = "Novo Inicio";
  final String restart = "Reinicio";
  final String inTarv = "Em TARV";
  final String preTarv = "Pré-TARV";
  final String chronicDiarrhea = "Diareia Crónica";
  final String fever = "Febre";
  final String anemia = "Anemia";
  final String pneumonia = "Pneumonia";
  final String sarcomaKaposi = "Sarcoma de Kaposi (SK)";
  final String meningitis = "Meningite";
  final String gingivitis = "Gengivite";
  final String estomatiteUlcerativaNecrotizante = "Estomatite Ulcerativa Necrotizante";
  final String candidiaseOral = "Candidiase Oral";
  final String cryptococcalMeningitis = "Meningite Criptocócica (MCC)";
  final String cervicalCancer = "Cancer Cervical";
  final String candidiaseEsofagica = "Candidiase Esofágica";
  final String herpesSimples = "Herpes Simples";
  final String cachexia = "Cachexia";
  final String toxoplasmose = "Toxoplasmose";
  final String hivDiseaseResultingInEncephalopathy = "Doença pelo HIV Resultando em Encefalopatia";
  final String extraPulmonaryTb = "Tuberculose Expulmonar";
  final String pulmonaryTb = "Tuberculose Pulmonar";

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

  @Override
  public Class<?> getInputDataType() {
    return null;
  }

  @Override
  public Class<?> getDataType() {
    return null;
  }
}
