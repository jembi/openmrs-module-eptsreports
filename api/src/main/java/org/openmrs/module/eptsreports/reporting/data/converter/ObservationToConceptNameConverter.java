package org.openmrs.module.eptsreports.reporting.data.converter;

import org.openmrs.module.reporting.data.converter.DataConverter;

/** Converter to return the Concept Names of expected responses to given concepts */
public class ObservationToConceptNameConverter implements DataConverter {

  final String newStart = "Novo Inicio";
  final String restart = "Reinicio";
  final String inTarv = "Em TARV";
  final String preTarv = "Pré-TARV";
  final String whoStageI = "ESTADIO I OMS";
  final String whoStageII = "ESTADIO II OMS";
  final String whoStageIII = "ESTADIO III OMS";
  final String whoStageIV = "ESTADIO IV OMS";
  final String sarcomaKaposi = "SARCOMA DE KAPOSI";

  @Override
  public Object convert(Object original) {

    if (original == null) {
      return "N/A";
    }

    switch (original.toString()) {
      case "1204":
        return whoStageI;
      case "1205":
        return whoStageII;
      case "1206":
        return whoStageIII;
      case "1207":
        return whoStageIV;
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
      case "TB":
        return "TB";
      case "Infeccoes Oportunistas":
        return "Infecções Oportunistas";
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
