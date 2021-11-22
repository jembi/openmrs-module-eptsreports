package org.openmrs.module.eptsreports.reporting.library.cohorts.TxPvlsBySourceC;

import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.library.cohorts.TxPvlsCohortQueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TxPvlsClinicalOrFichaResumoCohortQueries {

    private TxPvlsCohortQueries txPvlsCohortQueries;

    private HivMetadata hivMetadata;

    @Autowired
    public TxPvlsClinicalOrFichaResumoCohortQueries(
            TxPvlsCohortQueries txPvlsCohortQueries, HivMetadata hivMetadata) {
        this.txPvlsCohortQueries = txPvlsCohortQueries;
        this.hivMetadata = hivMetadata;
    }
}
