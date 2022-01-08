package org.openmrs.module.eptsreports.reporting.data.definition;

import java.util.Date;
import java.util.List;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.reporting.definition.configuration.ConfigurationPropertyCachingStrategy;
import org.openmrs.module.reporting.evaluation.caching.Caching;

@Caching(strategy = ConfigurationPropertyCachingStrategy.class)
public class MaxDateForResultsDataDefinition extends BaseDataDefinition
    implements PatientDataDefinition {

  public MaxDateForResultsDataDefinition() {
    super();
  }

  public MaxDateForResultsDataDefinition(String name) {
    super(name);
  }

  public static final long serialVersionUID = 1L;

  public static long getSerialVersionUID() {
    return serialVersionUID;
  }

  @Override
  public Class<?> getDataType() {
    return Date.class;
  }

  @ConfigurationProperty private Date onOrBefore;

  @ConfigurationProperty private Date onOrAfter;

  public Date getOnOrAfter() {
    return onOrAfter;
  }

  public void setOnOrAfter(Date onOrAfter) {
    this.onOrAfter = onOrAfter;
  }

  public Integer getQuestionConcept() {
    return questionConcept;
  }

  public void setQuestionConcept(Integer questionConcept) {
    this.questionConcept = questionConcept;
  }

  @ConfigurationProperty private Integer questionConcept;

  @ConfigurationProperty(group = "whichEncounter")
  private List<EncounterType> encounterTypeList;

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  @ConfigurationProperty private Location location;

  public Date getOnOrBefore() {
    return onOrBefore;
  }

  public void setOnOrBefore(Date onOrBefore) {
    this.onOrBefore = onOrBefore;
  }

  /** @return the encounterTypeList */
  public List<EncounterType> getEncounterTypeList() {
    return encounterTypeList;
  }

  /** @param encounterTypeList the encounterTypeList to set */
  public void setEncounterTypeList(List<EncounterType> encounterTypeList) {
    this.encounterTypeList = encounterTypeList;
  }
}
