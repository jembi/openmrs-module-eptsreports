package org.openmrs.module.eptsreports.reporting.library.dimensions;

import java.util.ArrayList;
import java.util.List;

public enum DimensionsKeyForAge implements DimensionKey {
  unknown("UK"),
  belowOneYear("<1"),
  below2Years("<2"),
  betweenZeroAnd4("0-4"),
  betweenZeroAnd14("0-14"),
  betweenZeroAnd15("0-15"),
  betweenOneAnd4("1-4"),
  between2And4("2-4"),
  between5And9("5-9"),
  below15Years("<15"),
  overOrEqualTo14("14+"),
  between10And14("10-14"),
  overOrEqualTo15("15+"),
  between15And19("15-19"),
  between20And24("20-24"),
  between25And29("25-29"),
  between30And34("30-34"),
  between35And39("35-39"),
  between40And44("40-44"),
  between45and49("45-49"),
  overOrEqualTo50("50+"),
  overOrEqualTo20("20+"),
  between10And19("10-19"),
  between2And14("2-14"),
  between2And9("2-9"),
  overOrEqualTo25("25+"),
  between50And54("50-54"),
  between55And59("55-59"),
  between60And64("60-64"),
  overOrEqualTo65("65+");

  private String key;
  private final String ageKeyPrefix = "age=";
  private List<DimensionKey> dimensionKeys;

  private DimensionsKeyForAge(String key) {
    this.key = key;
    dimensionKeys = new ArrayList<>();
    dimensionKeys.add(this);
  }

  @Override
  public String getKey() {

    return this.key;
  }

  @Override
  public String getDimension() {

    StringBuilder sb = new StringBuilder();
    for (DimensionKey dimensionKey : dimensionKeys) {
      sb.append(ageKeyPrefix).append(getKey()).append("|");
    }
    String dimensionOptions = sb.toString();
    return dimensionOptions.substring(0, dimensionOptions.length() - 1);
  }

  @Override
  public DimensionKey and(DimensionKey dimensionKey) {
    dimensionKeys.add(dimensionKey);
    return this;
  }
}
