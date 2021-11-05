package org.openmrs.module.eptsreports.reporting.library.dimensions;

import java.util.ArrayList;
import java.util.List;

public enum DimensionsKeysForGender implements DimensionKey {
  female("F"),
  male("M");

  private String key;
  private final String genderPrefix = "gender=";
  private List<DimensionKey> dimensionKeys;

  private DimensionsKeysForGender(String key) {
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
      sb.append(genderPrefix).append(getKey()).append("|");
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
