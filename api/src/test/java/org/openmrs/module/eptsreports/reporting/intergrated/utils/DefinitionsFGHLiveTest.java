package org.openmrs.module.eptsreports.reporting.intergrated.utils;

import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.context.UsernamePasswordCredentials;
import org.openmrs.module.reporting.common.DateUtil;

public abstract class DefinitionsFGHLiveTest extends DefinitionsTest {

  protected abstract String username();

  protected abstract String password();

  public void initialize() throws ContextAuthenticationException {
    UsernamePasswordCredentials credentials =
        new UsernamePasswordCredentials(username(), password());
    Context.authenticate(credentials);
    setStartDate(DateUtil.getDateTime(2013, 2, 6));
    setEndDate(DateUtil.getDateTime(2019, 3, 6));
    setLocation(new Location(220));
  }
}
