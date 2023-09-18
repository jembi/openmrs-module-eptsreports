package org.openmrs.module.eptsreports.reporting.data.converter;

import org.openmrs.module.reporting.data.converter.DataConverter;

/** Converter to return outputs for Quantitative and Qualidative Viral load results */
public class ViralLoadQualitativeLabelConverter implements DataConverter {

  @Override
  public Object convert(Object obj) {
    if (obj == null) {
      return "";
    }

    switch (obj.toString()) {
      case "23814":
        return "INDETECTÁVEL";
      case "1306":
        return "NIVEL BAIXO DE DETECÇÃO";
      case "23905":
        return "MENOR QUE 10 CÓPIAS/ML";
      case "23906":
        return "MENOR QUE 20 CÓPIAS/ML";
      case "23907":
        return "MENOR QUE 40 CÓPIAS/ML";
      case "23908":
        return "MENOR QUE 400 CÓPIAS/ML";
      case "23904":
        return "MENOR QUE 839 CÓPIAS/ML";
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
