package org.openmrs.module.eptsreports.reporting.data.converter;

import org.openmrs.module.reporting.data.converter.DataConverter;

public class SifNotNullAndNifNullConverter implements DataConverter {
  @Override
  public Object convert(Object o) {
    if (o == null) {
      return "N";
    }
    return "S";
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
