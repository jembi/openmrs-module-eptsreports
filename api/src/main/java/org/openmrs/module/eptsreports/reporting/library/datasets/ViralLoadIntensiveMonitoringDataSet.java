package org.openmrs.module.eptsreports.reporting.library.datasets;

import org.openmrs.module.eptsreports.reporting.library.cohorts.IntensiveMonitoringCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.ViralLoadIntensiveMonitoringCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.indicators.EptsGeneralIndicator;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ViralLoadIntensiveMonitoringDataSet {

    private ViralLoadIntensiveMonitoringCohortQueries viralLoadIntensiveMonitoringCohortQueries;


    private IntensiveMonitoringCohortQueries intensiveMonitoringCohortQueries;



    private EptsGeneralIndicator  eptsGeneralIndicator;

    private final String MAPPINGS = "endDate=${endDate},location=${location}";

    @Autowired
    public ViralLoadIntensiveMonitoringDataSet(ViralLoadIntensiveMonitoringCohortQueries viralLoadIntensiveMonitoringCohortQueries,
                                               EptsGeneralIndicator  eptsGeneralIndicator,
                                               IntensiveMonitoringCohortQueries intensiveMonitoringCohortQueries){
        this.viralLoadIntensiveMonitoringCohortQueries = viralLoadIntensiveMonitoringCohortQueries;
        this.eptsGeneralIndicator=eptsGeneralIndicator;
        this.intensiveMonitoringCohortQueries = intensiveMonitoringCohortQueries;
    }

    public DataSetDefinition constructViralLoadIntensiveMonitoringDataSet(){

        CohortIndicatorDataSetDefinition cohortIndicatorDataSetDefinition =new CohortIndicatorDataSetDefinition();

        // indicators
        CohortIndicator indicatorDen1Total
                = this.eptsGeneralIndicator.getIndicator("DEN1TOTAL",
                EptsReportUtils.map(
                        this.viralLoadIntensiveMonitoringCohortQueries
                                .getTotalIndicator1Den(),MAPPINGS));


        CohortIndicator indicatorNUm1Total
                = this.eptsGeneralIndicator.getIndicator("NUM1TOTAL",
                EptsReportUtils.map(
                        this.viralLoadIntensiveMonitoringCohortQueries
                                .getTotalIndicator1Num(),MAPPINGS));


        CohortIndicator indicatorDen2Total
                = this.eptsGeneralIndicator.getIndicator("DEN2TOTAL",
                EptsReportUtils.map(
                        this.viralLoadIntensiveMonitoringCohortQueries
                                .getTotalIndicator2Den(),MAPPINGS));

        CohortIndicator indicatorNUm2Total
                = this.eptsGeneralIndicator.getIndicator("NUM2TOTAL",
                EptsReportUtils.map(
                        this.viralLoadIntensiveMonitoringCohortQueries
                                .getTotalIndicator2Num(),MAPPINGS));


        CohortIndicator indicatorDen3Total
                = this.eptsGeneralIndicator.getIndicator("DEN3TOTAL",
                EptsReportUtils.map(
                        this.viralLoadIntensiveMonitoringCohortQueries
                                .getTotalIndicator3Den(),MAPPINGS));

        CohortIndicator indicatorNUm3Total
                = this.eptsGeneralIndicator.getIndicator("NUM3TOTAL",
                EptsReportUtils.map(
                        this.viralLoadIntensiveMonitoringCohortQueries
                                .getTotalIndicator3Num(),MAPPINGS));


        CohortIndicator indicatorDen4Total
                = this.eptsGeneralIndicator.getIndicator("DEN4TOTAL",
                EptsReportUtils.map(
                        this.viralLoadIntensiveMonitoringCohortQueries
                                .getTotalIndicator4Den(),MAPPINGS));

        CohortIndicator indicatorNUm4Total
                = this.eptsGeneralIndicator.getIndicator("NUM4TOTAL",
                EptsReportUtils.map(
                        this.viralLoadIntensiveMonitoringCohortQueries
                                .getTotalIndicator4Num(),MAPPINGS));

        CohortIndicator indicatorDen5Total
                = this.eptsGeneralIndicator.getIndicator("DEN5TOTAL",
                EptsReportUtils.map(
                        this.viralLoadIntensiveMonitoringCohortQueries
                                .getTotalIndicator5Den(),MAPPINGS));

        CohortIndicator indicatorNUm5Total
                = this.eptsGeneralIndicator.getIndicator("NUM5TOTAL",
                EptsReportUtils.map(
                        this.viralLoadIntensiveMonitoringCohortQueries
                                .getTotalIndicator5Num(),MAPPINGS));


        CohortIndicator indicatorDen6Total
                = this.eptsGeneralIndicator.getIndicator("DEN6TOTAL",
                EptsReportUtils.map(
                        this.intensiveMonitoringCohortQueries
                                .getMICat13Part2(15, "DEN15"),"revisionEndDate=${endDate},location=${location}"));

        CohortIndicator indicatorNUm6Total
                = this.eptsGeneralIndicator.getIndicator("NUM6TOTAL",
                EptsReportUtils.map(
                        this.intensiveMonitoringCohortQueries.getMICat13Part2(15, "NUM15"),
                         "revisionEndDate=${endDate},location=${location}"));


        CohortIndicator indicatorDen7Total
                = this.eptsGeneralIndicator.getIndicator("DEN7TOTAL",
                EptsReportUtils.map(
                        this.intensiveMonitoringCohortQueries
                                .getMICat13Part2(16, "DEN16"),"revisionEndDate=${endDate},location=${location}"));

        CohortIndicator indicatorNUm7Total
                = this.eptsGeneralIndicator.getIndicator("NUM7TOTAL",
                EptsReportUtils.map(
                        this.intensiveMonitoringCohortQueries.getMICat13Part2(16, "NUM16"),
                        "revisionEndDate=${endDate},location=${location}"));


        CohortIndicator indicatorDen8Total
                = this.eptsGeneralIndicator.getIndicator("DEN8TOTAL",
                EptsReportUtils.map(
                        this.intensiveMonitoringCohortQueries
                                .getMICat13Part2(17, "DEN17"),"revisionEndDate=${endDate},location=${location}"));

        CohortIndicator indicatorNUm8Total
                = this.eptsGeneralIndicator.getIndicator("NUM8TOTAL",
                EptsReportUtils.map(
                        this.intensiveMonitoringCohortQueries.getMICat13Part2(17, "NUM17"),
                        "revisionEndDate=${endDate},location=${location}"));

        CohortIndicator indicatorDen9Total
                = this.eptsGeneralIndicator.getIndicator("DEN9TOTAL",
                EptsReportUtils.map(
                        this.intensiveMonitoringCohortQueries
                                .getMICat13Part4(18, false),
                        "revisionEndDate=${endDate},location=${location}"));

        CohortIndicator indicatorNUm9Total
                = this.eptsGeneralIndicator.getIndicator("NUM9TOTAL",
                EptsReportUtils.map(
                        this.intensiveMonitoringCohortQueries.getMICat13Part4(18, true),
                        "revisionEndDate=${endDate},location=${location}"));

        // column mapping
        cohortIndicatorDataSetDefinition.addColumn("DEN1TOTAL",
                "Description",
                EptsReportUtils.map(indicatorDen1Total,MAPPINGS),"");

        return  cohortIndicatorDataSetDefinition;
    }



}
