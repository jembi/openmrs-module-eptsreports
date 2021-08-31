package org.openmrs.module.eptsreports.reporting.data.converter;

import java.util.Date;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.data.converter.DataConverter;

public class CurentDateConverter implements DataConverter {

  @Override
  public Object convert(Object obj) {

    Date data = (Date) obj;

    if (data == null) return null;

    return EptsReportUtils.formatDate(data);
  }

  @Override
  public Class<?> getInputDataType() {
    return Object.class;
  }

  @Override
  public Class<?> getDataType() {
    return Object.class;
  }
}
