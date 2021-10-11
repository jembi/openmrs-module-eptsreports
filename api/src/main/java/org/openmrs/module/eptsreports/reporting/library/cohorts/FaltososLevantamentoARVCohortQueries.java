package org.openmrs.module.eptsreports.reporting.library.cohorts;

import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class FaltososLevantamentoARVCohortQueries {

    private ListOfPatientsEligibleForVLCohortQueries listOfPatientsEligibleForVLCohortQueries;

    @Autowired
    public FaltososLevantamentoARVCohortQueries(ListOfPatientsEligibleForVLCohortQueries listOfPatientsEligibleForVLCohortQueries) {
        this.listOfPatientsEligibleForVLCohortQueries = listOfPatientsEligibleForVLCohortQueries;
    }

    public CohortDefinition getBaseCohort() {

        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.setName("Relatório de Faltosos ao Levantamento de ARV");
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.addParameter(new Parameter("location", "Location", Location.class));

        CohortDefinition chdA = getA();


        cd.addSearch("A", EptsReportUtils.map(chdA, "startDate=${startDate},endDate=${endDate},location=${location}"));


        cd.setCompositionString("A");

        return cd;
    }


    public CohortDefinition getA() {

        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.setName("Denominator - Last Next Schedulled Pickup");
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.addParameter(new Parameter("location", "Location", Location.class));

        CohortDefinition chdProximoLevantamentoFila = getPatientsWithProximoLevantamentoOnFila();
        CohortDefinition chdLevantamentoPlus30Days = getPatientsWithMostRecentDataDeLevantamentoPlus30Days();


        cd.addSearch("A1", EptsReportUtils.map(chdProximoLevantamentoFila, "startDate=${startDate},endDate=${endDate},location=${location}"));

        cd.addSearch("A2", EptsReportUtils.map(chdLevantamentoPlus30Days, "startDate=${startDate},endDate=${endDate},location=${location}"));

        cd.setCompositionString("A1 OR A2");

        return cd;
    }


    /**
     * <b>Technical Specs</b>
     * <blockquote>
     * <p>Select all patients with the most recent date from the following sources as <b>Last Next Scheduled Pick Up</b></p>
     * <ul>
     *     <li>
     *         the “Data do próximo levantamento” (concept id 5096, value_datetime) from the most recent FILA (encounter type 18)
     *         by report start date(encounter_datetime <= startDate)
     *         and the “Data do próximo levantamento” between startdate and enddate
     *     </li>
     * </ul>
     * </blockquote>
     *
     * @return {@link CohortDefinition}
     */
    public CohortDefinition getPatientsWithProximoLevantamentoOnFila() {
        return listOfPatientsEligibleForVLCohortQueries.getLastNextScheduledConsultationDate();
    }

    /**
     * <b>Technical Specs</b>
     * <blockquote>
     * <p>Select all patients with the most recent date from the following sources as <b>Last Next Scheduled Pick Up</b></p>
     * <ul>
     *     <li>
     *         the most recent “Data de Levantamento” (concept_id 23866, value_datetime) + 30 days, from “Recepcao Levantou
     *         ARV” (encounter type 52) with concept “Levantou ARV” (concept_id 23865) set to “SIM”
     *         (Concept id 1065) by report start date (value_datetime <= startDate)
     *     </li>
     * </ul>
     * </blockquote>
     *
     * @return {@link CohortDefinition}
     */
    public CohortDefinition getPatientsWithMostRecentDataDeLevantamentoPlus30Days() {
        return listOfPatientsEligibleForVLCohortQueries.getLastNextScheduledPickUpDateWithMostRecentDataLevantamento();
    }


}
