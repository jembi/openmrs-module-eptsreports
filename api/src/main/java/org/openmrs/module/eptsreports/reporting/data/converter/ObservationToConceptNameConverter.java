package org.openmrs.module.eptsreports.reporting.data.converter;

import org.openmrs.module.reporting.data.converter.DataConverter;

/** Converter to return the Concept Names of expected responses to given concepts */
public class ObservationToConceptNameConverter implements DataConverter {

  private final String newStart = "Novo Início";
  private final String restart = "Reinício";
  private final String inTarv = "Em TARV";
  private final String preTarv = "Pré-TARV";
  private final String chronicDiarrhea = "Diarreia Crónica";
  private final String fever = "Febre";
  private final String anemia = "Anemia";
  private final String pneumonia = "Pneumonia";
  private final String sarcomaKaposi = "Sarcoma de Kaposi (SK)";
  private final String meningitis = "Meningite";
  private final String gingivitis = "Gengivite";
  private final String estomatiteUlcerativaNecrotizante = "Estomatite Ulcerativa Necrotizante";
  private final String candidiaseOral = "Candidiase Oral";
  private final String cryptococcalMeningitis = "Meningite Criptocócica (MCC)";
  private final String ccu = "Cancro do Colo do Útero";
  private final String candidiaseEsofagica = "Candidiase Esofágica";
  private final String herpesSimples = "Hérpes Simples";
  private final String cachexia = "Caquexia";
  private final String toxoplasmose = "Toxoplasmose";
  private final String hivDiseaseResultingInEncephalopathy =
      "Doença pelo HIV Resultando em Encefalopatia";
  private final String extraPulmonaryTb = "Tuberculose Extrapulmonar";
  private final String pulmonaryTb = "Tuberculose Pulmonar";
  private final String plasmaSampleType = "Plasma";
  private final String drayBloodSpotSampleType = "Amostra de Sangue Seco";
  private final String cd4CountLessThanOrEqualTo200 = "CD4 menor ou igual a 200";
  private final String cd4CountGreaterThan200 = "CD4 Superior a 200";

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
        return ccu;
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
      case "1002":
        return plasmaSampleType;
      case "23831":
        return drayBloodSpotSampleType;
      case "165513":
        return cd4CountLessThanOrEqualTo200;
      case "1254":
        return cd4CountGreaterThan200;
      default:
        return original.toString();
    }
  }

  public Class<?> getInputDataType() {
    return null;
  }

  public Class<?> getDataType() {
    return null;
  }
}
