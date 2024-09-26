package org.openmrs.module.eptsreports.reporting.data.converter;

import org.openmrs.module.reporting.data.converter.DataConverter;

public class ZeroResultConverter implements DataConverter {

  @Override
  public Object convert(Object obj) {
    if (obj == null) {
      return "0";
    }
    return obj.toString();
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
