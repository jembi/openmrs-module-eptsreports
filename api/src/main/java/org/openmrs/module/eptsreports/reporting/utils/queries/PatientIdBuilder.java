package org.openmrs.module.eptsreports.reporting.utils.queries;

import java.util.List;

public class PatientIdBuilder {

    private String query;
    private List<String> columnsNames;
    private ColumnsFinder columnsFinder;
    public PatientIdBuilder(String query) {

        this.query = query;
        columnsFinder = new ColumnFinderImpl();
        tokenizer(query);

    }

    public String getQuery(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ");
        stringBuilder.append(columnsNames.get(0));
        stringBuilder.append(" FROM ( ");
        stringBuilder.append(query);
        stringBuilder.append(" ) patients ");
        stringBuilder.append(" GROUP BY patients.");
        stringBuilder.append(columnsNames.get(0));

        return stringBuilder.toString();
    }

    private List<String> tokenizer(String tokens){

        List<String> columns = columnsFinder.tokenizer(tokens);
        if (columns.size() != 1){

            throw new RuntimeException("Your query result must have one column");
        }
        return columns;

    }


}
