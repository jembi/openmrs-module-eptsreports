package org.openmrs.module.eptsreports.reporting.utils.queries;

public class PatientIdBuilder {

    private String query;
    public PatientIdBuilder(String query) {

        this.query = query;

    }

    public String getQuery(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT patient_id FROM ( ");
        stringBuilder.append(query);
        stringBuilder.append(" ) patients ");
        stringBuilder.append("GROUP BY patient_id");

        return stringBuilder.toString();
    }
}
