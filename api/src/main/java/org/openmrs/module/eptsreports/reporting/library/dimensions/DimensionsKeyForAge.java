package org.openmrs.module.eptsreports.reporting.library.dimensions;

import java.util.ArrayList;
import java.util.List;

public enum DimensionsKeyForAge implements DimensionKey {
    unknown("UK"),
    belowOneYear("<1"),
    below2Years("<2"),
    betweenZeroAnd4Years("0-4"),
    betweenZeroAnd14("0-14"),
    betweenZeroAnd15("0-15"),
    betweenOneAnd4("1-4"),
    between2And4("2-4"),
    between5And9("5-9"),
    belowAndEqualTo15("<15"),
    overAndEqualTo14("14+"),
    between10And14("10-14"),
    overAndEqualTo15("15+"),
    between14And19("15-19"),
    between20And24("20-24"),
    between25And29("25-29"),
    between30And34("30-34"),
    between35And39("35-39"),
    between40And44("40-44"),
    between45and49("45-49"),
    overAndEqualTo50("50+")    ;

    private String key;
    private final String ageKeyPrefix = "age=";
    private List<DimensionKey> dimensionKeys ;

    private DimensionsKeyForAge(String key){
        this.key = key;
        dimensionKeys = new ArrayList<>();
        dimensionKeys.add(this);

    }

    @Override
    public String getKey(){

        return this.key;
    }

    @Override
    public String getDimension() {

        StringBuilder sb = new StringBuilder();
        for (DimensionKey dimensionKey: dimensionKeys) {
            sb.append(dimensionKey.getDimension()).append("|");
        }
        String dimensionOptions = sb.toString();
        return dimensionOptions.substring(0, dimensionOptions.length()-1);
    }

    @Override
    public DimensionKey and(DimensionKey dimensionKey) {
        dimensionKeys.add(dimensionKey);
        return this;
    }
}
