package org.openmrs.module.eptsreports.reporting.data.converter;

import org.openmrs.module.reporting.data.converter.DataConverter;

/** Converter to return the Support Group State based on the returned value coded */
public class SupportGroupsConverter implements DataConverter {

  final String startState = "Inicio";

  final String continueState = "Continua";

  final String endState = "Fim";

  @Override
  public Object convert(Object original) {
    if (original == null) {
      return "";
    }

    switch (original.toString()) {
      case "1256":
        return startState;
      case "1257":
        return continueState;
      case "1267":
        return endState;
      default:
        return "";
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
