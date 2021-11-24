package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.calculation.generic.InitialArtStartDateCalculation;
import org.openmrs.module.eptsreports.reporting.data.converter.CalculationResultConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.GenderConverter;
import org.openmrs.module.eptsreports.reporting.data.definition.CalculationDataDefinition;
import org.openmrs.module.eptsreports.reporting.library.cohorts.DQACargaViralCohortQueries;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.data.person.definition.*;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DQACargaViralDataset extends BaseDataSet {

  private HivMetadata hivMetadata;

  private DQACargaViralCohortQueries dQACargaViralCohortQueries;

  @Autowired
  public DQACargaViralDataset(
          HivMetadata hivMetadata,
          DQACargaViralCohortQueries dQACargaViralCohortQueries) {
    this.hivMetadata = hivMetadata;
    this.dQACargaViralCohortQueries = dQACargaViralCohortQueries;
  }

  public DataSetDefinition constructDQACargaViralDataset() {
    PatientDataSetDefinition pdd = new PatientDataSetDefinition();

    pdd.addParameter(new Parameter("startDate", "startDate", Date.class));
    pdd.addParameter(new Parameter("endDate", "endDate", Date.class));
    pdd.addParameter(new Parameter("location", "Location", Location.class));
    pdd.addParameter(new Parameter("province", "Province", Location.class)); // Address (Província)
    pdd.addParameter(new Parameter("district", "District", Location.class)); // Address (Distrito)
    pdd.setName("DQA Carga Viral");

    PatientIdentifierType identifierType =
            Context.getPatientService()
                    .getPatientIdentifierTypeByUuid("8a76bb5c-4c57-11ec-81d3-0242ac130003");


    // TO DO
    // pdd.addRowFilter(dQACargaViralCohortQueries.getBaseCohort(), , , );

    /** 1 - NID - Sheet 1: Column B */
    pdd.addColumn("nid", getNID(identifierType.getPatientIdentifierTypeId()), "");

    /** 2 - Sexo - Sheet 1: Column C */
    pdd.addColumn("gender", new GenderDataDefinition(), "", new GenderConverter());

    /** 3 - Faixa Etária - Sheet 1: Column D */
    // pdd.addColumn("age", .getAge(), , );

    /** 4 - Data Início TARV - Sheet 1: Column E */
    pdd.addColumn(
            "inicio_tarv",
            getArtStartDate(),
            "onOrBefore=${endDate},location=${location}",
            new CalculationResultConverter());

    /** 5 - Data de Consulta onde Notificou o Resultado de CV dentro do Período de Revisão - Sheet 1: Column F */
//    pdd.addColumn("data_consulta_resultado_cv", , , );

    /** 6 - Resultado da Carga Viral - Sheet 1: Column G */
//    pdd.addColumn("resultado_carga_viral", , , );

    return pdd;
  }

  private DataDefinition getNID(int identifierType) {
    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();
    spdd.setName("NID");

    Map<String, Integer> valuesMap = new HashMap<>();

    String sql =
            " SELECT p.patient_id,pi.identifier  FROM patient p INNER JOIN patient_identifier pi ON p.patient_id=pi.patient_id "
                    + " INNER JOIN patient_identifier_type pit ON pit.patient_identifier_type_id=pi.identifier_type "
                    + " WHERE p.voided=0 AND pi.voided=0 AND pit.retired=0 AND pit.patient_identifier_type_id ="
                    + identifierType;

    StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

    spdd.setQuery(substitutor.replace(sql));
    return spdd;
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