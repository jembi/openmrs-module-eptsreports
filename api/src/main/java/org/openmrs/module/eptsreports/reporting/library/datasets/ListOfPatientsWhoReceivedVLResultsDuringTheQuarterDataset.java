package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.Date;
import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.reporting.data.converter.ForwardSlashDateConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.GenderConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.NotApplicableIfNullConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.ObjectCounterConverter;
import org.openmrs.module.eptsreports.reporting.library.cohorts.PatientesWhoReceivedVlResultsCohortQueries;
import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonIdDataDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsWhoReceivedVLResultsDuringTheQuarterDataset extends BaseDataSet {

  private final PatientesWhoReceivedVlResultsCohortQueries
      patientesWhoReceivedVlResultsCohortQueries;

  @Autowired
  public ListOfPatientsWhoReceivedVLResultsDuringTheQuarterDataset(
      PatientesWhoReceivedVlResultsCohortQueries patientesWhoReceivedVlResultsCohortQueries) {
    this.patientesWhoReceivedVlResultsCohortQueries = patientesWhoReceivedVlResultsCohortQueries;
  }

  public DataSetDefinition constructDQACargaViralDataset() {
    PatientDataSetDefinition pdd = new PatientDataSetDefinition();

    pdd.addParameter(new Parameter("startDate", "startDate", Date.class));
    pdd.addParameter(new Parameter("endDate", "endDate", Date.class));
    pdd.addParameter(new Parameter("location", "Location", Location.class));
    pdd.setName("DQA Carga Viral");

    PatientIdentifierType identifierType =
        Context.getPatientService()
            .getPatientIdentifierTypeByUuid("e2b966d0-1d5f-11e0-b929-000c29ad1d07");

    pdd.addRowFilter(
        patientesWhoReceivedVlResultsCohortQueries.getBaseCohort(),
        "startDate=${startDate},endDate=${endDate},location=${location}");

    /** Patient counter - Sheet 1: Column A */
    pdd.addColumn("counter", new PersonIdDataDefinition(), "", new ObjectCounterConverter());

    /** 1 - NID - Sheet 1: Column B */
    pdd.addColumn(
        "nid",
        patientesWhoReceivedVlResultsCohortQueries.getNID(
            identifierType.getPatientIdentifierTypeId()),
        "");

    /** 2 - Sexo - Sheet 1: Column C */
    pdd.addColumn("gender", new GenderDataDefinition(), "", new GenderConverter());

    /** 3 - Idade - Sheet 1: Column D */
    pdd.addColumn(
        "age",
        patientesWhoReceivedVlResultsCohortQueries.getAge("endDate"),
        "endDate=${endDate}",
        new NotApplicableIfNullConverter());

    /** 4 - Data Início TARV - Sheet 1: Column E */
    pdd.addColumn(
        "inicio_tarv",
        patientesWhoReceivedVlResultsCohortQueries.getArtStartDate(),
        "startDate=${startDate},endDate=${endDate},location=${location}",
        null);

    /**
     * 5 - Data de Consulta onde Notificou o Resultado de CV dentro do Período de Revisão - Sheet 1:
     * Column F
     */
    pdd.addColumn(
        "data_consulta_resultado_cv",
        patientesWhoReceivedVlResultsCohortQueries.getDataNotificouCV(),
        "startDate=${startDate},endDate=${endDate},location=${location}",
        new ForwardSlashDateConverter());

    /** 6 - Resultado da Carga Viral (Resultado Quantitativo) - Sheet 1: Column G */
    pdd.addColumn(
        "resultado_cv_quantitativo",
        patientesWhoReceivedVlResultsCohortQueries.getViralLoadQuantitativeResults(),
        "startDate=${startDate},endDate=${endDate},location=${location}",
        new NotApplicableIfNullConverter());

    /** Resultado da Carga Viral (Resultado Qualitativo) - Sheet 1: Column H */
    pdd.addColumn(
        "resultado_cv_qualitativo",
        patientesWhoReceivedVlResultsCohortQueries.getViralLoadQualitativeResults(),
        "startDate=${startDate},endDate=${endDate},location=${location}",
        new NotApplicableIfNullConverter());

    return pdd;
  }
}
