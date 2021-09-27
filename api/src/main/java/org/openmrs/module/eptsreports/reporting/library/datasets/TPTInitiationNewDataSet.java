package org.openmrs.module.eptsreports.reporting.library.datasets;

import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.reporting.data.converter.ConceptNameConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.GenderConverter;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ListOfPatientsEligibleForVLDataDefinitionQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.TPTInitiationDataDefinitionQueries;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.BirthdateConverter;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.*;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TPTInitiationNewDataSet extends BaseDataSet {

    private TPTInitiationDataDefinitionQueries tPTInitiationDataDefinitionQueries;
    private ListChildrenOnARTandFormulationsDataset listChildrenOnARTandFormulationsDataset;


    @Autowired
    public TPTInitiationNewDataSet(TPTInitiationDataDefinitionQueries tPTInitiationDataDefinitionQueries,
                                   ListChildrenOnARTandFormulationsDataset listChildrenOnARTandFormulationsDataset

    ) {

        this.tPTInitiationDataDefinitionQueries = tPTInitiationDataDefinitionQueries;
        this.listChildrenOnARTandFormulationsDataset = listChildrenOnARTandFormulationsDataset;
    }

    public DataSetDefinition constructDataSet() {
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

        pdd.addColumn("NID", listChildrenOnARTandFormulationsDataset.getNID(), "");

        pdd.addColumn("Name", nameDef, "");


        pdd.addColumn("Age", tPTInitiationDataDefinitionQueries.getPatientsAndTheirAges(), "endDate=${endDate}", null);

        pdd.addColumn("ARTStartDate", tPTInitiationDataDefinitionQueries.getPatientsAndARTStartDate(), "endDate=${endDate},startDate=${startDate},location=${location}", null);

        pdd.addColumn("Sex", new GenderDataDefinition(), "", null);


      //  pdd.addColumn("PregnantAndBreastfeeding", tPTInitiationDataDefinitionQueries.getPrenantAndBreastfeeding(), "endDate=${endDate},startDate=${startDate},location=${location}", null);

        pdd.addColumn("LastFollowUpConsultationDate", tPTInitiationDataDefinitionQueries.getPatientsAndLastFollowUpConsultationDate(), "location=${location}", null);

        pdd.addColumn("ReceivedTPTInLastFollowupConsulttarion", tPTInitiationDataDefinitionQueries.getPatientsReceivedTPTInTheLastFollowUpConsultation(), "location=${location}", null);


        pdd.addColumn("pid", new PersonIdDataDefinition(), "");


        return pdd;
    }
}
