package org.openmrs.module.eptsreports.service;

import java.util.List;
import org.openmrs.GlobalProperty;
import org.openmrs.api.OpenmrsService;

/** The class is used to perform operations in the {@link GlobalProperty} data. */
public interface EptsGlobalPropertyService extends OpenmrsService {

  /**
   * Removes one or several {@link GlobalProperty}(s) with the has the given pattern name.
   *
   * @param patternName
   * @return removed global property(s)
   */
  List<GlobalProperty> removeEptsGlobalPropertiesEntries(String patternName);
}
