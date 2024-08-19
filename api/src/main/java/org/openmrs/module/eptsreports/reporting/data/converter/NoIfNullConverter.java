package org.openmrs.module.eptsreports.reporting.data.converter;

import org.openmrs.module.reporting.data.converter.DataConverter;

public class NoIfNullConverter implements DataConverter {
  @Override
  public Object convert(Object obj) {
    if (obj == null) {
      return "Não";
    }
    return obj.toString();
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
