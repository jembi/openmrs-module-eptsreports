package org.openmrs.module.eptsreports.service;

import java.util.List;
import org.openmrs.GlobalProperty;

/** The class is used to perform some operation in the {@link GlobalProperty} data. */
public interface EptsGlobalProperty {

  /**
   * Removes one several {@link GlobalProperty} the has the given pattern name;
   *
   * @param namePattern
   * @return removed global properties
   */
  List<GlobalProperty> removeEptsGlobalPropertiesEntries(String patternName);
}
