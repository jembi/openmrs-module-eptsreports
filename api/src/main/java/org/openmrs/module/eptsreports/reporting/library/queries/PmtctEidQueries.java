package org.openmrs.module.eptsreports.reporting.library.queries;

import java.util.*;
import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.metadata.TbMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PmtctEidQueries {

  @Autowired private CommonQueries commonQueries;
  @Autowired private TbMetadata tbMetadata;
  @Autowired private HivMetadata hivMetadata;

  public String getInfantsWhoHaveCcrNidRegistered() {
    return "SELECT "
        + "  p.patient_id, "
        + "  pi.identifier "
        + "FROM "
        + "  patient p "
        + "  INNER JOIN patient_identifier pi ON p.patient_id = pi.patient_id "
        + "  INNER JOIN patient_identifier_type pit ON pit.patient_identifier_type_id = pi.identifier_type "
        + "WHERE "
        + "  p.voided = 0 "
        + "  AND pi.voided = 0 "
        + "  AND pit.retired = 0 "
        + "  AND pit.patient_identifier_type_id = ${9} "
        + "GROUP BY "
        + "  p.patient_id";
  }

  public String getPatientEnrolledInCcrProgram() {
    return "SELECT "
        + "  p.patient_id, "
        + "  Min(pp.date_enrolled) AS enrollment_date "
        + "FROM "
        + "  patient p "
        + "  INNER JOIN patient_program pp ON p.patient_id = pp.patient_id "
        + "WHERE "
        + "  p.voided = 0 "
        + "  AND pp.voided = 0 "
        + "  AND pp.program_id = ${6} "
        + "  AND pp.location_id = :location "
        + "  AND pp.date_enrolled <= :endDate "
        + "GROUP BY "
        + "  p.patient_id";
  }

  public String getInfantsWhoHaveCcrFichaResumoWithDataDeAberturadoProcesso() {
    return "SELECT "
        + "  p.patient_id, "
        + "  Min(e.encounter_datetime) AS enrollment_date "
        + "FROM "
        + "  patient p "
        + "  INNER JOIN encounter e ON p.patient_id = e.patient_id "
        + "WHERE "
        + "  p.voided = 0 "
        + "  AND e.voided = 0 "
        + "  AND e.encounter_type = ${92} "
        + "  AND e.location_id = :location "
        + "  AND e.encounter_datetime <= :endDate "
        + "GROUP BY "
        + "  p.patient_id";
  }
}
