package org.openmrs.module.eptsreports.service;

import java.util.ArrayList;
import java.util.List;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Service;

@Service
public class EptsGlobalPropertyImpl implements EptsGlobalProperty {

  @Override
  public List<GlobalProperty> removeEptsGlobalPropertiesEntries(String patternName) {

    List<GlobalProperty> remvedProperties = new ArrayList<>();

    AdministrationService administrationService = Context.getAdministrationService();
    List<GlobalProperty> globalProperties = administrationService.getAllGlobalProperties();

    for (GlobalProperty gp : globalProperties) {
      if (gp.getProperty().contains(patternName)) {
        administrationService.purgeGlobalProperty(gp);
        remvedProperties.add(gp);
      }
    }

    return remvedProperties;
  }
}
