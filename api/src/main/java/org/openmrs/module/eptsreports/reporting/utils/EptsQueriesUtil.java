package org.openmrs.module.eptsreports.reporting.utils;

import java.util.ArrayList;
import java.util.List;

public class EptsQueriesUtil {

  public static class UnionBuilder {
    List<String> queries = new ArrayList<>();
    public UnionBuilder(String query) {
      queries = new ArrayList<>();

      queries.add(query);
    }

    public UnionBuilder union(String query) {
      queries.add(query);
      return this;
    }

    public String buildQuery() {

      StringBuilder stringBuilder = new StringBuilder();
      for (String query : queries) {

        stringBuilder.append(query).append(" UNION ");
      }
      String unionQuery = stringBuilder.substring(0,stringBuilder.length() -7);

      return unionQuery;
    }
  }

  public static class Max{
    private String query;
    public Max(String query){
      this.query = query;
    }
    public String getQuery(){
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("SELECT patient_id, max_date FROM ( ");
      stringBuilder.append(query);
      stringBuilder.append(" ) most_recent ");
      stringBuilder.append(" GROUP BY most_recent.patient_id");

      return stringBuilder.toString();
    }
  }

}
