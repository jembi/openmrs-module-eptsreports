package org.openmrs.module.eptsreports.reporting.utils;


import org.openmrs.module.eptsreports.reporting.utils.queries.MaxBuilder;
import org.openmrs.module.eptsreports.reporting.utils.queries.MinBuilder;
import org.openmrs.module.eptsreports.reporting.utils.queries.UnionBuilder;
import org.springframework.stereotype.Component;

@Component
public class EptsQueriesUtil {

  public UnionBuilder unionBuilder(String query) {

    UnionBuilder unionBuilder = new UnionBuilder(query);
    return unionBuilder;

  }

  public MaxBuilder Max(String query){






    MaxBuilder maxBuilder = new MaxBuilder(query);
    return maxBuilder;
  }



  public MinBuilder Min(String query){
    MinBuilder minBuilder = new MinBuilder(query);
    return minBuilder;
  }



}
