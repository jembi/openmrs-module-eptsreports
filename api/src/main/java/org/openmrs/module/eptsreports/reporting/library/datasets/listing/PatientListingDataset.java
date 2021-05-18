package org.openmrs.module.eptsreports.reporting.library.datasets.listing;

import java.util.Date;
import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.reporting.calculation.generic.InitialArtStartDateCalculation;
import org.openmrs.module.eptsreports.reporting.data.converter.CalculationResultConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.EncounterDatetimeConverter;
import org.openmrs.module.eptsreports.reporting.data.definition.CalculationDataDefinition;
import org.openmrs.module.eptsreports.reporting.library.datasets.BaseDataSet;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.BirthdateConverter;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.converter.ObsValueConverter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.EncountersForPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.BirthdateDataDefinition;
import org.openmrs.module.reporting.data.person.definition.ConvertedPersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
import org.openmrs.module.reporting.data.person.definition.ObsForPersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonIdDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.stereotype.Component;

@Component
public class PatientListingDataset extends BaseDataSet {
  public static final String DATE_FORMAT = "dd/MM/yyyy";

  public DataSetDefinition getPatientListForIpt() {
    PatientDataSetDefinition dsd = new PatientDataSetDefinition();
    dsd.setName("test");
    dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
    dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
    dsd.addParameter(new Parameter("location", "Location", Location.class));
    String defParam = "startDate=${startDate},endDate=${endDate},location=${location}";

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
    dsd.addColumn("id", new PersonIdDataDefinition(), "");
    dsd.addColumn("Name", nameDef, "");
    dsd.addColumn("Identifier", identifierDef, "");
    dsd.addColumn("Sex", new GenderDataDefinition(), "", null);
    dsd.addColumn("DOB", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
    dsd.addColumn(
        "ART Start Date",
        getArtStartDate(),
        "onOrBefore=${endDate},location=${location}",
        new CalculationResultConverter());
    dsd.addColumn(
        "Return Visit Date",
        new ObsForPersonDataDefinition(
            "Return Visit Date",
            TimeQualifier.LAST,
            Context.getConceptService().getConceptByUuid("e1e2efd8-1d5f-11e0-b929-000c29ad1d07"),
            null,
            null),
        "onOrBefore=${endDate},onOrAfter=${startDate},locationList=${location}",
        new ObsValueConverter());
    dsd.addColumn(
        "Last encounter",
        getLastEncounterDate(),
        "onOrBefore=${endDate},onOrAfter=${startDate},locationList=${location}",
        new EncounterDatetimeConverter());
    return dsd;
  }

  private DataDefinition getLastEncounterDate() {
    EncountersForPatientDataDefinition def =
        new EncountersForPatientDataDefinition("Last encounter");
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
}
