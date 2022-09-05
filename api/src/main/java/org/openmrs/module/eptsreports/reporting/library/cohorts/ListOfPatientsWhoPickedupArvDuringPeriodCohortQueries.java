package org.openmrs.module.eptsreports.reporting.library.cohorts;

import org.apache.commons.text.StringSubstitutor;
import org.openmrs.Location;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class ListOfPatientsWhoPickedupArvDuringPeriodCohortQueries {

    private final TxCurrCohortQueries txCurrCohortQueries;

    private final HivMetadata hivMetadata;

    @Autowired

    public ListOfPatientsWhoPickedupArvDuringPeriodCohortQueries(TxCurrCohortQueries txCurrCohortQueries, HivMetadata hivMetadata) {
        this.txCurrCohortQueries = txCurrCohortQueries;
        this.hivMetadata = hivMetadata;
    }

    public CohortDefinition getBaseCohort() {

        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.setName("Base Cohort for List of patients who picked up ARV during the period");
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.addParameter(new Parameter("location", "Location", Location.class));

        CohortDefinition txcurr = this.txCurrCohortQueries.getTxCurrCompositionCohort("txcurr", true);

        CohortDefinition lastPickupFila = this.getLastDrugPickupDate();

        cd.addSearch(
                "txcurr", EptsReportUtils.map(txcurr, "onOrBefore=${startDate},location=${location}"));

        cd.addSearch("pickup", EptsReportUtils.map(lastPickupFila, "startDate=${startDate},endDate=${endDate}, location=${location}"));

        cd.setCompositionString("txcurr AND pickup");
        return cd;
    }


    /**
     * <b>Patient’s Most Recent Drug Pick-Up on FILA</b>
     *
     * <p>
     *    Art Pickup Date: MAX(encounter.encounter_datetime) between the selected report start date and end date of S.TARV: FARMACIA (ID=18) as
     * Last ARV Pick-Up Date on FILA
     * </p>
     * @return {@link CohortDefinition}
     */
    public CohortDefinition getLastDrugPickupDate() {
        SqlCohortDefinition cd = new SqlCohortDefinition();
        cd.setName("Most Recent Drug Pick-Up on FILA");
        cd.addParameter(new Parameter("location", "location", Location.class));
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "endDate", Date.class));

        Map<String, Integer> valuesMap = new HashMap<>();
        valuesMap.put("18", hivMetadata.getARVPharmaciaEncounterType().getEncounterTypeId());

        String sql =
                " SELECT patient_id FROM ( SELECT p.patient_id, MAX(e.encounter_datetime) as last_pickup_date"
                        + " FROM   patient p  "
                        + "          INNER JOIN encounter e  "
                        + "                          ON p.patient_id = e.patient_id  "
                        + " WHERE  p.voided = 0  "
                        + "          AND e.voided = 0  "
                        + "          AND e.location_id = :location "
                        + "          AND e.encounter_type = ${18} "
                        + "         AND e.encounter_datetime BETWEEN :startDate AND :endDate "
                        + " GROUP BY p.patient_id ) pickup_fila ";

        StringSubstitutor substitutor = new StringSubstitutor(valuesMap);

        cd.setQuery(substitutor.replace(sql));

        return cd;
    }

}
