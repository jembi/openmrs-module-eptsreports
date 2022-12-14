package org.openmrs.module.eptsreports.reporting.library.cohorts;

import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.springframework.stereotype.Component;

@Component
public class ListOfPatientsWithHighViralLoadCohortQueries {

  public DataDefinition getPatientCell() {

    SqlPatientDataDefinition spdd = new SqlPatientDataDefinition();

    String query =
        ""
            + "SELECT address.patient_id, address.celula "
            + "             FROM   (SELECT p.patient_id,pa.address3 celula "
            + "                     FROM   patient p "
            + "                            INNER JOIN person pr ON p.patient_id = pr.person_id "
            + "                            INNER JOIN person_address pa ON pa.person_id = pr.person_id "
            + "                     WHERE  p.voided = 0 "
            + "                            AND pr.voided = 0 "
            + "                     ORDER  BY pa.person_address_id DESC) address "
            + "             GROUP  BY address.patient_id";

    spdd.setQuery(query);

    return spdd;
  }
}
