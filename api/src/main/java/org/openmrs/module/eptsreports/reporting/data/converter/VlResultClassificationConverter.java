package org.openmrs.module.eptsreports.reporting.data.converter;

import org.openmrs.module.reporting.data.converter.DataConverter;

/**
 * Converter to check the Viral Load is greater 1000 copies/ml and Return its related Classification
 */
public class VlResultClassificationConverter implements DataConverter {
  @Override
  public Object convert(Object o) {

    if (o == null) {
      return "N/A";
    }

    Double value = Double.parseDouble(o.toString());

    if (value >= 1000) {
      return "S";
    }

    return "N";
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
