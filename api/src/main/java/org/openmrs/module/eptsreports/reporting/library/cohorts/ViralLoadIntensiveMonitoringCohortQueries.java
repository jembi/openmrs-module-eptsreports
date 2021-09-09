package org.openmrs.module.eptsreports.reporting.library.cohorts;


import org.openmrs.Location;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * This Cohort Query  makes several unions of variety queries in  {@link IntensiveMonitoringCohortQueries }
 */
@Component
public class ViralLoadIntensiveMonitoringCohortQueries {

    private IntensiveMonitoringCohortQueries intensiveMonitoringCohortQueries;

    @Autowired
    public ViralLoadIntensiveMonitoringCohortQueries(IntensiveMonitoringCohortQueries intensiveMonitoringCohortQueries){
        this.intensiveMonitoringCohortQueries = intensiveMonitoringCohortQueries;
    }

    /**
     * <b>Indicator 1 Denominator:</b>
     * <br />
     * Number of patients in the 1st line of ART who had a clinical consultation in the review period (data collection) and who were eligible to a VL request”
     * <br />
     * Select all from the Denominator of  MI report categories13.1, 13.6, 13.7, 13.8 (union all  specified categories)
     */
    public CohortDefinition getTotalIndicator1Den(){

        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("endDate","endDate", Date.class));
        cd.addParameter(new Parameter("location","location", Location.class));

        CohortDefinition mi13den1 = intensiveMonitoringCohortQueries.getCat13Den(1,false);
        CohortDefinition mi13den6 = intensiveMonitoringCohortQueries.getCat13Den(6,false);
        CohortDefinition mi13den7 = intensiveMonitoringCohortQueries.getCat13Den(7,false);
        CohortDefinition mi13den8 = intensiveMonitoringCohortQueries.getCat13Den(8,false);

        cd.addSearch("1", EptsReportUtils.map(mi13den1,"revisionEndDate=${endDate},location=${location}"));
        cd.addSearch("6", EptsReportUtils.map(mi13den6,"revisionEndDate=${endDate},location=${location}"));
        cd.addSearch("7", EptsReportUtils.map(mi13den7,"revisionEndDate=${endDate},location=${location}"));
        cd.addSearch("8", EptsReportUtils.map(mi13den8,"revisionEndDate=${endDate},location=${location}"));

        cd.setCompositionString("1 OR 6 OR 7 OR 8");
        return cd;
    }

    /**
     * <b>Indicator 1 Numerator:</b>
     * <br />
     * Number of patients in the 1st line of ART who had a clinical consultation during the review period (data collection), were eligible to a VL request and with a record of a VL request made by the clinician
     * <br />
     * Select all from the Numerator of  MI report categories13.1, 13.6, 13.7, 13.8 (union all  specified categories)
     */
    public CohortDefinition getTotalIndicator1Num(){

        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("endDate","endDate", Date.class));
        cd.addParameter(new Parameter("location","location", Location.class));

        CohortDefinition mi13num1 = intensiveMonitoringCohortQueries.getCat13Den(1,true);
        CohortDefinition mi13num6 = intensiveMonitoringCohortQueries.getCat13Den(6,true);
        CohortDefinition mi13num7 = intensiveMonitoringCohortQueries.getCat13Den(7,true);
        CohortDefinition mi13num8 = intensiveMonitoringCohortQueries.getCat13Den(8,true);

        cd.addSearch("1", EptsReportUtils.map(mi13num1,"revisionEndDate=${endDate},location=${location}"));
        cd.addSearch("6", EptsReportUtils.map(mi13num6,"revisionEndDate=${endDate},location=${location}"));
        cd.addSearch("7", EptsReportUtils.map(mi13num7,"revisionEndDate=${endDate},location=${location}"));
        cd.addSearch("8", EptsReportUtils.map(mi13num8,"revisionEndDate=${endDate},location=${location}"));

        cd.setCompositionString("1 OR 6 OR 7 OR 8");
        return cd;

    }

    /**
     * <b>Indicator 2 Denominator:</b>
     * <br />
     * Number of patients who started 1st-line ART or new 1st-line regimen in the month of evaluation
     * <br />
     * Select all from the Denominator of  MI report categories 13.2, 13.9, 13.10, 13.11 (union all  specified categories)
     */
    public CohortDefinition getTotalIndicator2Den(){

        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("endDate","endDate", Date.class));
        cd.addParameter(new Parameter("location","location", Location.class));

        CohortDefinition mi13den2 = intensiveMonitoringCohortQueries.getMI13DEN2(2);
        CohortDefinition mi13den9 = intensiveMonitoringCohortQueries.getMI13DEN9(9);
        CohortDefinition mi13den10 = intensiveMonitoringCohortQueries.getMI13DEN10(10);
        CohortDefinition mi13den11 = intensiveMonitoringCohortQueries.getMI13DEN11(11);

        cd.addSearch("2", EptsReportUtils.map(mi13den2,"revisionEndDate=${endDate},location=${location}"));
        cd.addSearch("9", EptsReportUtils.map(mi13den9,"revisionEndDate=${endDate},location=${location}"));
        cd.addSearch("10", EptsReportUtils.map(mi13den10,"revisionEndDate=${endDate},location=${location}"));
        cd.addSearch("11", EptsReportUtils.map(mi13den11,"revisionEndDate=${endDate},location=${location}"));

        cd.setCompositionString("2 OR 9 OR 10 OR 11");
        return cd;
    }


}
