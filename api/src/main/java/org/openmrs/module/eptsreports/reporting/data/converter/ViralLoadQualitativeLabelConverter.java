package org.openmrs.module.eptsreports.reporting.data.converter;

import org.openmrs.module.reporting.data.converter.DataConverter;

public class ViralLoadQualitativeLabelConverter implements DataConverter {

  @Override
  public Object convert(Object obj) {
    if (obj == null) {
      return "";
    }

    switch (obj.toString()) {
      case "23814":
        return "Indetectável";

      case "1306":
        return "Nivel baixo de detecção";

      case "23905":
        return "Menor que 10 copias/ml";

      case "23906":
        return "Menor que 20 copias/ml";

      case "23907":
        return "Menor que 40 copias/ml";

      case "23908":
        return "Menor que 400 copias/ml";

      case "23904":
        return "Menor que 839 copias/ml";

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
