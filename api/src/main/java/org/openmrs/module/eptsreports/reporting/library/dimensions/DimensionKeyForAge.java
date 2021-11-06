package org.openmrs.module.eptsreports.reporting.library.dimensions;

import java.util.ArrayList;
import java.util.List;

public enum DimensionKeyForAge implements DimensionKey {
  unknown("UK"),
  underOneYears("<1"),
  under2Years("<2"),
  betweenZeroAnd4Years("0-4"),
  betweenZeroAnd14Years("0-14"),
  betweenZeroAnd15Years("0-15"),
  betweenOneAnd4("1-4"),
  between2And4Years("2-4"),
  between5And9Years("5-9"),
  belowAndEqualTo15Years("<15"),
  overAndEqualTo14Years("14+"),
  between10And14Years("10-14"),
  overAndEqualTo15Years("15+"),
  between14And19Years("15-19"),
  between20And24Years("20-24"),
  between25And29Years("25-29"),
  between30And34Years("30-34"),
  between35And39Years("35-39"),
  between40And44Years("40-44"),
  between45and49Years("45-49"),
  overAndEqualTo50Years("50+");

  private String key;
  private List<DimensionKey> dimensionKeys;

  private DimensionKeyForAge(String key) {
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
      sb.append("age=").append(getKey()).append("|");
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
