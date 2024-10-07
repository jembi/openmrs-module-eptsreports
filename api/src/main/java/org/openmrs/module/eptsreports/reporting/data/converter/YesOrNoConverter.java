package org.openmrs.module.eptsreports.reporting.data.converter;

import org.openmrs.module.reporting.data.converter.DataConverter;

public class YesOrNoConverter implements DataConverter {

  @Override
  public Object convert(Object o) {
    if (o == null) {
      return "Não";
    }
    return "Sim";
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
