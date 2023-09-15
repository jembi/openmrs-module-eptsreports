package org.openmrs.module.eptsreports.reporting.data.converter;

import org.openmrs.module.reporting.data.converter.DataConverter;

/** Converter to return via test results depending on the query result */
public class ViaResultsConverter implements DataConverter {

  @Override
  public Object convert(Object original) {
    if (original == null) {
      return "";
    }

    switch (original.toString()) {
      case "703":
        return "Via Positivo";
      case "664":
        return "Via Negativo";

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
