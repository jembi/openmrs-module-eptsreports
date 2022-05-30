package org.openmrs.module.eptsreports.reporting.utils.queries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class MaxBuilder {


        private String query;
        private List<String> columnsNames;
        public MaxBuilder(String query){

            String lowerCaseQuery = query.toLowerCase();
            int firstSelect = lowerCaseQuery.indexOf("select");
            int firstFrom = lowerCaseQuery.indexOf("from");
            String columns = query.substring(firstSelect, firstFrom);
            this.query = query;
            tokenizer(columns);
        }
        public String getQuery(){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("SELECT ");
            stringBuilder.append(columnsNames.get(0));
            stringBuilder.append(",MAX( ");
            stringBuilder.append(columnsNames.get(1));
            stringBuilder.append(" ) FROM ( ");
            stringBuilder.append(query);
            stringBuilder.append(" ) most_recent ");
            stringBuilder.append(" GROUP BY most_recent.");
            stringBuilder.append(columnsNames.get(0));

            return stringBuilder.toString();
        }


    private List<String> tokenizer(String tokens){
        columnsNames = new ArrayList<>();
        List<String> columns = Collections.list(new StringTokenizer(tokens, ",")).stream().map(token -> (String) token).collect(Collectors.toList());

        if(columns.size() != 2) {
            throw new RuntimeException("The query must have two columns");
        }

        for (String column : columns) {
            String[] split = column.trim().split(" ");
            String lastToken = split[split.length -1];
            if (lastToken.contains(".")){
                columnsNames.add("`"+ lastToken.substring(lastToken.lastIndexOf("."))+ "`");
            }else{
                columnsNames.add("`"+lastToken+"`");
            }


        }
        return columns;
    }



}
