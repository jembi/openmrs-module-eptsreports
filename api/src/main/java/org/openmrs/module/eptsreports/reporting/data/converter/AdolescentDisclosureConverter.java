package org.openmrs.module.eptsreports.reporting.data.converter;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

public class AdolescentDisclosureConverter implements DataConverter {
  String results = "";
  @Autowired private HivMetadata hivMetadata;

  @Override
  public Object convert(Object obj) {
    if (obj == null) {
      results = "";
    }
    Encounter encounter = (Encounter) obj;
    Set<Obs> getAllObs = encounter.getAllObs();
    if (getAllObs == null) {
      results = "";
    }
    if (!checkIfConceptQuestionWasRecorded(encounter)) {
      results = "";
    } else {
      if (getAllObs != null) {
        for (Obs obs : getAllObs) {
          if (obs.getConcept() != null
              && obs.getConcept()
                  .equals(hivMetadata.getDisclosureOfHIVDiagnosisToChildrenAdolescentsConcept())) {

            if (obs.getValueCoded() == null) {
              results = "";
            } else {
              if (obs.getValueCoded()
                  .equals(
                      Context.getConceptService()
                          .getConceptByUuid("63e43b2f-801f-412b-87bb-45db8e0ad21b"))) {
                return "P";
              } else if (obs.getValueCoded()
                  .equals(
                      Context.getConceptService()
                          .getConceptByUuid("8279b6c1-572d-428c-be45-96e05fe6165d"))) {
                return "N";
              }
            }
            break;
          }
        }
      }
    }
    return results;
  }

  @Override
  public Class<?> getInputDataType() {
    return Obs.class;
  }

  @Override
  public Class<?> getDataType() {
    return String.class;
  }

  private boolean checkIfConceptQuestionWasRecorded(Encounter encounter) {

    boolean isFound = false;

    for (Obs obs : encounter.getAllObs()) {
      if (obs.getConcept() != null
          && obs.getConcept()
              .equals(hivMetadata.getDisclosureOfHIVDiagnosisToChildrenAdolescentsConcept())) {
        isFound = true;
        break;
      }
    }
    return isFound;
  }
}
