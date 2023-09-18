package org.openmrs.module.eptsreports.reporting.data.converter;

import org.openmrs.module.reporting.data.converter.DataConverter;

/** Converter to return the ART Adherence Evaluation for patients with High Viral Load */
public class AdherenceEvaluationConverter implements DataConverter {

  final String goodAdherence = "Boa";

  final String adherenceRisk = "Risco";

  final String badAdherence = "Má";

  @Override
  public Object convert(Object o) {
    if (o == null) {
      return "N/A";
    }
    switch (o.toString()) {
      case "1383":
        return goodAdherence;
      case "1749":
        return adherenceRisk;
      case "1385":
        return badAdherence;
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
