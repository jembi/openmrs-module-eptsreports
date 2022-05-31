package org.openmrs.module.eptsreports.reporting.utils.queries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class ColumnFinderImpl implements ColumnsFinder{

    private List<String> columnsNames;
    @Override
    public List<String> tokenizer(String query){
        String tokens = getSelectedColumnsFrom(query);
        columnsNames = new ArrayList<>();
        List<String> columns = Collections.list(new StringTokenizer(tokens, ",")).stream().map(token -> (String) token).collect(Collectors.toList());


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

    private String getSelectedColumnsFrom(String query){
        String lowerCaseQuery = query.toLowerCase();
        int firstSelect = lowerCaseQuery.indexOf("select");
        int firstFrom = lowerCaseQuery.indexOf("from");
        String columns = query.substring(firstSelect, firstFrom);
        return columns;
    }
}
