package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.calculation.generic.InitialArtStartDateCalculation;
import org.openmrs.module.eptsreports.reporting.data.converter.CalculationResultConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.EncounterDatetimeConverter;
import org.openmrs.module.eptsreports.reporting.data.converter.GenderConverter;
import org.openmrs.module.eptsreports.reporting.data.definition.CalculationDataDefinition;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.data.person.definition.*;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListChildrenOnARTandFormulationsDataset extends BaseDataSet {
        @Autowired
        private HivMetadata hivMetadata;

        public DataSetDefinition constructDataset(List<Parameter> parameterList) {

                PatientDataSetDefinition patientDataSetDefinition = new PatientDataSetDefinition();

                patientDataSetDefinition.setName("Formulations");
                patientDataSetDefinition.addParameters(getParameters());

                PatientIdentifierType identifierType = Context.getPatientService()
                                .getPatientIdentifierTypeByUuid("e2b966d0-1d5f-11e0-b929-000c29ad1d07");
                DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
                DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier",
                                new PatientIdentifierDataDefinition(identifierType.getName(), identifierType),
                                identifierFormatter);
                DataConverter formatter = new ObjectFormatter("{familyName}, {givenName}");
                DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(),
                                formatter);
                patientDataSetDefinition.setParameters(getParameters());

                patientDataSetDefinition.addColumn("id", new PersonIdDataDefinition(), "");
                patientDataSetDefinition.addColumn("name", nameDef, "");
                patientDataSetDefinition.addColumn("nid", identifierDef, "");
                patientDataSetDefinition.addColumn("gender", new GenderDataDefinition(), "", new GenderConverter());
                patientDataSetDefinition.addColumn("age", new AgeDataDefinition(), "", null);
                patientDataSetDefinition.addColumn("inicio_tarv", getArtStartDate(),
                                "onOrBefore=${endDate},location=${location}", new CalculationResultConverter());
                patientDataSetDefinition.addColumn("lastpickupdate", getLastDrugPickupDate(),
                                "onOrBefore=${endDate},location=${location}", new EncounterDatetimeConverter());
                patientDataSetDefinition.addColumn("lastconsultationdate", getNextDrugPickupDate(),
                                "onOrBefore=${endDate},location=${location}", new EncounterDatetimeConverter());
                patientDataSetDefinition.addColumn("lastregimeconsultation", getLastARVRegimen(),
                "onOrBefore=${endDate},location=${location}", new EncounterDatetimeConverter());
                patientDataSetDefinition.addColumn("lastconsultationdate", getLastFollowupConsultationDate(),
                                "onOrBefore=${endDate},location=${location}", new EncounterDatetimeConverter());
                patientDataSetDefinition.addColumn("lastregimeconsultation", getARVRegimenLastConsultationDate(),
                                "onOrBefore=${endDate},location=${location}", new EncounterDatetimeConverter());
                patientDataSetDefinition.addColumn("weight", getWeightLossLastConsultationDate(),
                                "onOrBefore=${endDate},location=${location}", new EncounterDatetimeConverter());
                patientDataSetDefinition.addColumn("familyapproachlastconsultation",
                                getAbordagemFamiliarOnLastConsultationDate(),
                                "onOrBefore=${endDate},location=${location}", new EncounterDatetimeConverter());
                patientDataSetDefinition.addColumn("quartelydismissallastconsultation",
                                get3MonthsDispensationOnLastConsultationDate(),
                                "onOrBefore=${endDate},location=${location}", new EncounterDatetimeConverter());
                patientDataSetDefinition.addColumn("nextconsultationdate", getNextFollowUpConsultationDate(),
                                "onOrBefore=${endDate},location=${location}", new EncounterDatetimeConverter());

                patientDataSetDefinition.addParameters(parameterList);

                return patientDataSetDefinition;
        }

        @Override
        public List<Parameter> getParameters() {
                List<Parameter> parameters = new ArrayList<>();
                parameters.add(ReportingConstants.END_DATE_PARAMETER);
                parameters.add(ReportingConstants.LOCATION_PARAMETER);
                return parameters;
        }

        private DataDefinition getArtStartDate() {
                CalculationDataDefinition cd = new CalculationDataDefinition("Art start date",
                                Context.getRegisteredComponents(InitialArtStartDateCalculation.class).get(0));
                cd.addParameter(new Parameter("location", "Location", Location.class));
                cd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
                return cd;
        }

        /**
         * 8
         * 
         * @return
         */
        private DataDefinition getLastARVRegimen() {
                SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();
                spdd.setName("Last ARV Regimen (FILA)");
                spdd.addParameter(new Parameter("location", "location", Location.class));
                spdd.addParameter(new Parameter("endDate", "endDate", Date.class));

                Map<String, Integer> valuesMap = new HashMap<>();
                valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
                valuesMap.put("1088", hivMetadata.getRegimeConcept().getConceptId());

                String sql = " SELECT max_encounter.patient_id as patient_id, max_encounter.encounter_datetime FROM  "
                                + " (SELECT p.patient_id, MAX(e.encounter_datetime) as encounter_datetime "
                                + " FROM   patient p  " + "  INNER JOIN encounter e  "
                                + "  ON p.patient_id = e.patient_id " + "  WHERE e.location_id = :location "
                                + " AND e.encounter_datetime <= :endDate " + " GROUP BY p.patient_id "
                                + " ) max_encounter "
                                + " INNER JOIN encounter en ON max_encounter.patient_id= en.patient_id "
                                + " INNER JOIN obs ob ON en.encounter_id = ob.encounter_id "
                                + " WHERE max_encounter.encounter_datetime = en.encounter_datetime "
                                + " AND en.encounter_type = ${18} " + " AND en.location_id = :location "
                                + " AND ob.concept_id = ${1088} " + " AND ob.value_coded IS NOT NULL "
                                + " AND en.encounter_datetime <= :endDate ";

                StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

                spdd.setQuery(substitutor.replace(sql));
                return spdd;
        }

        /**
         * 7
         * 
         * @return
         */
        private DataDefinition getLastDrugPickupDate() {
                SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();
                spdd.setName("Last Drug Pick Up Date");
                spdd.addParameter(new Parameter("location", "location", Location.class));
                spdd.addParameter(new Parameter("endDate", "endDate", Date.class));

                Map<String, Integer> valuesMap = new HashMap<>();
                valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());

                String sql = " SELECT p.patient_id, MAX(e.encounter_datetime) " + " FROM   patient p  "
                                + "          INNER JOIN encounter e  "
                                + "                          ON p.patient_id = e.patient_id  "
                                + " WHERE  p.voided = 0  " + "          AND e.voided = 0  "
                                + "          AND e.location_id = :location " + "          AND e.encounter_type = ${18} "
                                + "         AND e.encounter_datetime <= :endDate " + " GROUP BY p.patient_id ";

                StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

                spdd.setQuery(substitutor.replace(sql));
                return spdd;
        }

        /**
         * 13
         * 
         * @return
         */
        private DataDefinition getNextDrugPickupDate() {
                SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();
                spdd.setName("Next Drug Pick Up Date");
                spdd.addParameter(new Parameter("location", "location", Location.class));
                spdd.addParameter(new Parameter("endDate", "endDate", Date.class));

                Map<String, Integer> valuesMap = new HashMap<>();
                valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());
                valuesMap.put("5096", hivMetadata.getReturnVisitDateForArvDrugConcept().getConceptId());

                String sql = " SELECT " + "         p.patient_id, " + "         MAX(e.encounter_datetime) " + " FROM "
                                + "         patient p "
                                + "         INNER JOIN encounter e ON p.patient_id = e.patient_id "
                                + "         INNER JOIN obs o ON e.encounter_id = o.encounter_id " + "         WHERE "
                                + "         p.voided = 0 " + "         AND e.voided = 0 " + "         AND o.voided = 0 "
                                + "         AND e.location_id = :location " + "         AND e.encounter_type = ${18} "
                                + "         AND o.concept_id = ${5096} "
                                + "         AND e.encounter_datetime <= :endDate " + " GROUP BY "
                                + "         p.patient_id ";

                StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

                spdd.setQuery(substitutor.replace(sql));
                return spdd;
        }

        /**
         * 14
         * 
         * @return
         */
        private DataDefinition getLastFollowupConsultationDate() {
                SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();
                spdd.setName("Last Follow up Consultation Date");
                spdd.addParameter(new Parameter("location", "location", Location.class));
                spdd.addParameter(new Parameter("endDate", "endDate", Date.class));

                Map<String, Integer> valuesMap = new HashMap<>();
                valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
                valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());

                String sql = " SELECT p.patient_id, MAX(e.encounter_datetime) " + " FROM   patient p  "
                                + "         INNER JOIN encounter e  "
                                + "                         ON p.patient_id = e.patient_id  "
                                + "         INNER JOIN obs o  "
                                + "                         ON e.encounter_id = o.encounter_id  "
                                + "WHERE  p.voided = 0  " + "         AND e.voided = 0  "
                                + "         AND o.voided = 0  " + "         AND e.location_id = :location "
                                + "         AND e.encounter_type IN (${6}, ${9}) "
                                + "         AND e.encounter_datetime <= :endDate " + "GROUP BY p.patient_id ";

                StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

                spdd.setQuery(substitutor.replace(sql));
                return spdd;
        }

        /**
         * 15
         * 
         * @return
         */
        private DataDefinition getARVRegimenLastConsultationDate() {
                SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();
                spdd.setName("ARV Regimen on Last Consultation");
                spdd.addParameter(new Parameter("location", "location", Location.class));
                spdd.addParameter(new Parameter("endDate", "endDate", Date.class));

                Map<String, Integer> valuesMap = new HashMap<>();
                valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
                valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
                valuesMap.put("1087", "");// To be fixed no metadata

                String sql = " SELECT p.patient_id, MAX(e.encounter_datetime) " + " FROM   patient p  "
                                + "         INNER JOIN encounter e  "
                                + "                         ON p.patient_id = e.patient_id  "
                                + "         INNER JOIN obs o  "
                                + "                         ON e.encounter_id = o.encounter_id  "
                                + "WHERE  p.voided = 0  " + "         AND e.voided = 0  "
                                + "         AND o.voided = 0  " + "         AND e.location_id = :location "
                                + "        AND e.encounter_type IN (${6}, ${9}) "
                                + "         AND o.concept_id = ${1087}  "
                                + "         AND e.encounter_datetime <= :endDate " + "GROUP BY p.patient_id ";

                StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

                spdd.setQuery(substitutor.replace(sql));
                return spdd;
        }

        /**
         * 16
         * 
         * @return
         */
        private DataDefinition getWeightLossLastConsultationDate() {
                SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();
                spdd.setName("Weight on Last Consultation");
                spdd.addParameter(new Parameter("location", "location", Location.class));
                spdd.addParameter(new Parameter("endDate", "endDate", Date.class));

                Map<String, Integer> valuesMap = new HashMap<>();
                valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
                valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
                valuesMap.put("5089", "");// To be fixed no metadata

                String sql = " SELECT p.patient_id, MAX(e.encounter_datetime) " + " FROM   patient p  "
                                + "         INNER JOIN encounter e  "
                                + "                         ON p.patient_id = e.patient_id  "
                                + "         INNER JOIN obs o  "
                                + "                        ON e.encounter_id = o.encounter_id  "
                                + "WHERE  p.voided = 0  " + "         AND e.voided = 0  " + "        AND o.voided = 0  "
                                + "        AND e.location_id = :location "
                                + "        AND e.encounter_type IN (${6}, ${9}) "
                                + "         AND o.concept_id = ${5089}  "
                                + "         AND e.encounter_datetime <= :endDate " + "GROUP BY p.patient_id ";

                StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

                spdd.setQuery(substitutor.replace(sql));
                return spdd;
        }

        /**
         * 17
         * 
         * @return
         */
        private DataDefinition getAbordagemFamiliarOnLastConsultationDate() {
                SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();
                spdd.setName("Abordagem Familiar on Last Consultation");
                spdd.addParameter(new Parameter("location", "location", Location.class));
                spdd.addParameter(new Parameter("endDate", "endDate", Date.class));

                Map<String, Integer> valuesMap = new HashMap<>();
                valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
                valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
                valuesMap.put("23725", hivMetadata.getFamilyApproach().getConceptId());

                String sql = " SELECT p.patient_id, MAX(e.encounter_datetime) " + " FROM   patient p  "
                                + "          INNER JOIN encounter e  "
                                + "                          ON p.patient_id = e.patient_id  "
                                + "          INNER JOIN obs o  "
                                + "                          ON e.encounter_id = o.encounter_id  "
                                + " WHERE  p.voided = 0  " + "         AND e.voided = 0  "
                                + "         AND o.voided = 0  " + "         AND e.location_id = :location "
                                + "         AND e.encounter_type IN (${6}, ${9}) "
                                + "         AND o.concept_id = ${23725}  "
                                + "          AND e.encounter_datetime <= :endDate " + " GROUP BY p.patient_id ";

                StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

                spdd.setQuery(substitutor.replace(sql));
                return spdd;
        }

        /**
         * 17
         * 
         * @return
         */
        private DataDefinition get3MonthsDispensationOnLastConsultationDate() {
                SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();
                spdd.setName("3 month Dispensation on Last consultation");
                spdd.addParameter(new Parameter("location", "location", Location.class));
                spdd.addParameter(new Parameter("endDate", "endDate", Date.class));

                Map<String, Integer> valuesMap = new HashMap<>();
                valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
                valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
                valuesMap.put("23739", hivMetadata.getTypeOfDispensationConcept().getConceptId());
                valuesMap.put("23720", hivMetadata.getQuarterlyConcept().getConceptId());

                String sql = " SELECT p.patient_id, MAX(e.encounter_datetime) " + " FROM   patient p  "
                                + "          INNER JOIN encounter e  "
                                + "                          ON p.patient_id = e.patient_id  "
                                + "          INNER JOIN obs o  "
                                + "                          ON e.encounter_id = o.encounter_id  "
                                + " WHERE  p.voided = 0  " + "          AND e.voided = 0  "
                                + "          AND o.voided = 0  " + "          AND e.location_id = :location "
                                + "          AND e.encounter_type IN (${6}, ${9}) "
                                + "          AND o.concept_id = ${23739}  " + "      AND o.value_coded = ${23720} "
                                + "         AND e.encounter_datetime <= :endDate " + " GROUP BY p.patient_id  ";

                StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

                spdd.setQuery(substitutor.replace(sql));
                return spdd;
        }

        /**
         * 18
         * 
         * @return
         */
        private DataDefinition getNextFollowUpConsultationDate() {
                SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();
                spdd.setName("Next Follow up Consultation Date");
                spdd.addParameter(new Parameter("location", "location", Location.class));
                spdd.addParameter(new Parameter("endDate", "endDate", Date.class));

                Map<String, Integer> valuesMap = new HashMap<>();
                valuesMap.put("6", hivMetadata.getAdultoSeguimentoEncounterType().getEncounterTypeId());
                valuesMap.put("9", hivMetadata.getPediatriaSeguimentoEncounterType().getEncounterTypeId());
                valuesMap.put("1410", ""); // To be fixed

                String sql = " SELECT p.patient_id, MAX(e.encounter_datetime) " + " FROM   patient p  "
                                + "         INNER JOIN encounter e  "
                                + "                         ON p.patient_id = e.patient_id  "
                                + "         INNER JOIN obs o  "
                                + "                         ON e.encounter_id = o.encounter_id  "
                                + " WHERE  p.voided = 0  " + "         AND e.voided = 0  "
                                + "         AND o.voided = 0  " + "         AND e.location_id = :location "
                                + "         AND e.encounter_type IN (${6}, ${9}) "
                                + "         AND o.concept_id = ${1410}  "
                                + "         AND e.encounter_datetime <= :endDate " + " GROUP BY p.patient_id ";

                StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

                spdd.setQuery(substitutor.replace(sql));
                return spdd;
        }
}
