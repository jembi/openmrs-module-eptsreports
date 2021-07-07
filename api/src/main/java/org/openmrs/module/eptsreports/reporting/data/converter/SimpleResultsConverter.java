package org.openmrs.module.eptsreports.reporting.data.converter;

import org.openmrs.api.context.Context;
import org.openmrs.calculation.result.SimpleResult;

public class SimpleResultsConverter implements DataConverter {

    @Override
    public Object convert(Object original) {
      SimpleResult s = (SimpleResult) original;
  
      if (s == null) return "";
  
      return Context.getConceptService().getConcept(s).getName().getName();
    }
  
    @Override
    public Class<?> getInputDataType() {
      return SimpleResult.class;
    }
  
    @Override
    public Class<?> getDataType() {
      return String.class;
    }
  }
  