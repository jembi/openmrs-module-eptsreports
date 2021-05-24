package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.reporting.calculation.generic.InitialArtStartDateCalculation;
import org.openmrs.module.eptsreports.reporting.data.converter.CalculationResultConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.EncounterDatetimeConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.GenderConverter;
import org.openmrs.module.eptsreports.reporting.data.definition.CalculationDataDefinition;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.converter.ObsValueConverter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.EncountersForPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.*;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.stereotype.Component;

@Component
public class TPTListOfPatientsEligibleDataSet extends BaseDataSet {

  public DataSetDefinition constructDataset() {

    PatientDataSetDefinition pdd = new PatientDataSetDefinition();

    pdd.setName("TPT");

    PatientIdentifierType identifierType =
        Context.getPatientService()
            .getPatientIdentifierTypeByUuid("e2b966d0-1d5f-11e0-b929-000c29ad1d07");
    DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
    DataDefinition identifierDef =
        new ConvertedPatientDataDefinition(
            "identifier",
            new PatientIdentifierDataDefinition(identifierType.getName(), identifierType),
            identifierFormatter);
    DataConverter formatter = new ObjectFormatter("{familyName}, {givenName}");
    DataDefinition nameDef =
        new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), formatter);
    pdd.setParameters(getParameters());

    pdd.addColumn("id", new PersonIdDataDefinition(), "");
    pdd.addColumn("name", nameDef, "");
    pdd.addColumn("nid", identifierDef, "");
    pdd.addColumn("gender", new GenderDataDefinition(), "", new GenderConverter());
    pdd.addColumn("age", new AgeDataDefinition(), "", null);
    pdd.addColumn(
        "inicio_tarv",
        getArtStartDate(),
        "onOrBefore=${endDate},location=${location}",
        new CalculationResultConverter());
    pdd.addColumn(
        "date_next_consultation",
        getObsForPersonData("e1e2efd8-1d5f-11e0-b929-000c29ad1d07"),
        "onOrBefore=${endDate},locationList=${location}",
        new ObsValueConverter());
    pdd.addColumn(
        "date_last_segment",
        getLastEncounterDate(),
        "onOrBefore=${endDate},locationList=${location}",
        new EncounterDatetimeConverter());

    return pdd;
  }

  @Override
  public List<Parameter> getParameters() {
    List<Parameter> parameters = new ArrayList<>();
    parameters.add(ReportingConstants.END_DATE_PARAMETER);
    parameters.add(ReportingConstants.LOCATION_PARAMETER);
    return parameters;
  }

  private DataDefinition getLastEncounterDate() {
    EncountersForPatientDataDefinition def =
        new EncountersForPatientDataDefinition("Last encounter");
    def.addParameter(new Parameter("onOrBefore", "On or before", Date.class));
    def.addParameter(new Parameter("locationList", "Location", Location.class));
    def.setWhich(TimeQualifier.LAST);
    return def;
  }

  private DataDefinition getArtStartDate() {
    CalculationDataDefinition cd =
        new CalculationDataDefinition(
            "Art start date",
            Context.getRegisteredComponents(InitialArtStartDateCalculation.class).get(0));
    cd.addParameter(new Parameter("location", "Location", Location.class));
    cd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
    return cd;
  }

  private DataDefinition getObsForPersonData(String uuid) {
    ObsForPersonDataDefinition obsForPersonDataDefinition = new ObsForPersonDataDefinition();
    obsForPersonDataDefinition.addParameter(
        new Parameter("onOrBefore", "On or before", Date.class));
    obsForPersonDataDefinition.addParameter(
        new Parameter("locationList", "Location", Location.class));
    obsForPersonDataDefinition.setQuestion(Context.getConceptService().getConceptByUuid(uuid));
    obsForPersonDataDefinition.setWhich(TimeQualifier.LAST);
    return obsForPersonDataDefinition;
  }
}
