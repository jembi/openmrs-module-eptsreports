package org.openmrs.module.eptsreports.reporting.data.converter;

import org.openmrs.module.reporting.data.converter.DataConverter;

/** Converter to return the Tarv Treatment Situation names */
public class TreatmentSituationConverter implements DataConverter {

  final String newStart = "Novo Inicio";
  final String restart = "Reinicio";
  final String inTarv = "Em TARV";
  final String preTarv = "Pré-TARV";

  @Override
  public Object convert(Object original) {

    if (original == null) {
      return "N/A";
    }

    switch (original.toString()) {
      case "1256":
        return newStart;
      case "1705":
        return restart;
      case "6276":
        return inTarv;
      case "6275":
        return preTarv;
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
