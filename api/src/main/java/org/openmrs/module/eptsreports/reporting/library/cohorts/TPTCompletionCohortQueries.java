package org.openmrs.module.eptsreports.reporting.library.cohorts;

import org.openmrs.module.eptsreports.metadata.CommonMetadata;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TPTCompletionCohortQueries {

  private GenericCohortQueries genericCohortQueries;

  private HivMetadata hivMetadata;

  private CommonMetadata commonMetadata;

  private GenderCohortQueries genderCohortQueries;

  private AgeCohortQueries ageCohortQueries;

  private ResumoMensalCohortQueries resumoMensalCohortQueries;

  private CommonCohortQueries commonCohortQueries;

  private TbMetadata tbMetadata;

  private TxPvlsCohortQueries txPvls;

  private final String MAPPING = "startDate=${startDate},endDate=${endDate},location=${location}";

  @Autowired
  public TPTCompletionCohortQueries(
      GenericCohortQueries genericCohortQueries,
      HivMetadata hivMetadata,
      CommonMetadata commonMetadata,
      GenderCohortQueries genderCohortQueries,
      ResumoMensalCohortQueries resumoMensalCohortQueries,
      CommonCohortQueries commonCohortQueries,
      TbMetadata tbMetadata,
      TxPvlsCohortQueries txPvls,
      AgeCohortQueries ageCohortQueries) {
    this.genericCohortQueries = genericCohortQueries;
    this.hivMetadata = hivMetadata;
    this.commonMetadata = commonMetadata;
    this.genderCohortQueries = genderCohortQueries;
    this.resumoMensalCohortQueries = resumoMensalCohortQueries;
    this.commonCohortQueries = commonCohortQueries;
    this.tbMetadata = tbMetadata;
    this.txPvls = txPvls;
    this.ageCohortQueries = ageCohortQueries;
  }

  /**
   *
   *
   * <h4></h4>
   *
   * <ul>
   *   <li>
   *   <li>
   * </ul>
   *
   * @return CohortDefinition
   */
  public CohortDefinition getAandB() {
    CompositionCohortDefinition compositionCohortDefinition = new CompositionCohortDefinition();

    return compositionCohortDefinition;
  }
}
