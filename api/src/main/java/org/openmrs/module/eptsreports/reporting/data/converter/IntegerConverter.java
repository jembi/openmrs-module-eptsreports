package org.openmrs.module.eptsreports.reporting.data.converter;

import org.openmrs.module.reporting.data.converter.DataConverter;

public class IntegerConverter implements DataConverter {
  @Override
  public Object convert(Object obj) {
    if (obj == null) {
      return "N/A";
    }
    double value = Double.parseDouble(obj.toString());
    int vlresult;
    vlresult = (int) value;
    return vlresult;
  }

  @Override
  public Class<?> getInputDataType() {
    return double.class;
  }

  @Override
  public Class<?> getDataType() {
    return int.class;
  }
}
