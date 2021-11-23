package org.openmrs.module.eptsreports.reporting.library.dimensions;

public enum DimensionKeyForGender implements DimensionKey {
  female("F"),
  male("M");

  private String key;
  private EptsCommonDimensionKey eptsCommonDimensionKey;

  private DimensionKeyForGender(String key) {
    this.key = key;
    eptsCommonDimensionKey = new EptsCommonDimensionKey();
    eptsCommonDimensionKey.add(this);
  }

  @Override
  public String getKey() {
    return this.key;
  }

  @Override
  public String getDimension() {
    return eptsCommonDimensionKey.getDimension();
  }

  @Override
  public DimensionKey and(DimensionKey dimensionKey) {
    eptsCommonDimensionKey.add(dimensionKey);
    return this;
  }

  @Override
  public String getPrefix() {
    return "gender=";
  }
}
