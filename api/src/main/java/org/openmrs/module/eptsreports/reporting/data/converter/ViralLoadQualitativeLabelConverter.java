package org.openmrs.module.eptsreports.reporting.data.converter;

import org.openmrs.module.reporting.data.converter.DataConverter;

/** Converter to return outputs for Quantitative and Qualidative Viral load results */
public class ViralLoadQualitativeLabelConverter implements DataConverter {

  @Override
  public Object convert(Object obj) {
    if (obj == null) {
      return "N/A";
    }

    switch (obj.toString()) {
      case "23814":
        return "INDETECTÁVEL";
      case "1306":
        return "NIVEL BAIXO DE DETECÇÃO";
      case "23905":
        return "MENOR QUE 10 COPIAS/ML";
      case "23906":
        return "MENOR QUE 20 COPIAS/ML";
      case "23907":
        return "MENOR QUE 40 COPIAS/ML";
      case "23908":
        return "MENOR QUE 400 COPIAS/ML";
      case "23904":
        return "MENOR QUE 839 COPIAS/ML";
      case "165331":
        return "<";
      default:
        return obj;
    }
  }

  @Override
  public Class<?> getInputDataType() {
    return String.class;
  }

  @Override
  public Class<?> getDataType() {
    return String.class;
  }
}
