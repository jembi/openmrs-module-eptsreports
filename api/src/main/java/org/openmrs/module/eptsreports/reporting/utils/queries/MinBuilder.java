package org.openmrs.module.eptsreports.reporting.utils.queries;

public class MinBuilder {

    private String query;
    public MinBuilder(String query){
        this.query = query;
    }
    public String getQuery(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT patient_id, MIN(occurrence_date) FROM ( ");
        stringBuilder.append(query);
        stringBuilder.append(" ) most_recent ");
        stringBuilder.append(" GROUP BY most_recent.patient_id");

        return stringBuilder.toString();
    }
}
