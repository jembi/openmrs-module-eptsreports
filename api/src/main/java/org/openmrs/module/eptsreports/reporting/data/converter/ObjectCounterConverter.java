package org.openmrs.module.eptsreports.reporting.data.converter;

import org.openmrs.module.reporting.data.converter.DataConverter;

public class ObjectCounterConverter implements DataConverter {
  private Integer counter = 0;

  @Override
  public Object convert(Object obj) {
    if (obj != null) {
      counter++;
    }
    return counter;
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
