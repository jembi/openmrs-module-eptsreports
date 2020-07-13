package org.openmrs.module.eptsreports.api.impl;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.springframework.stereotype.Service;

@Service
public class EptsGlobalPropertyServiceImpl extends BaseOpenmrsService
    implements EptsGlobalPropertyService {

  private static Log log = LogFactory.getLog(EptsGlobalPropertyServiceImpl.class);

  @Override
  public List<GlobalProperty> removeEptsGlobalPropertiesEntries(String patternName) {

    List<GlobalProperty> remvedProperties = new ArrayList<>();

    AdministrationService administrationService = Context.getAdministrationService();
    List<GlobalProperty> globalProperties = administrationService.getAllGlobalProperties();

    for (GlobalProperty gp : globalProperties) {
      if (gp.getProperty().contains(patternName)) {
        administrationService.purgeGlobalProperty(gp);

        remvedProperties.add(gp);

        log.info("removed the global property " + gp.getProperty());
      }
    }

    return remvedProperties;
  }
}
