package org.openmrs.module.eptsreports.reporting.library.dimensions;

public class EptsCommonDimensionKey {

  String dimension = null;
  String firstDimension = null;

  public void add(DimensionKey dimensionKey) {

    if (firstDimension == null && dimension == null) {
      StringBuilder sb = new StringBuilder();
      sb.append(dimensionKey.getPrefix()).append(dimensionKey.getKey()).append("|");
      firstDimension = sb.toString();
      dimension = sb.toString();
      return;
    }

    if (firstDimension != null && dimension == null) {
      dimension = firstDimension.toString();
      return;
    }

    if (dimension != null) {
      StringBuilder sb = new StringBuilder(dimension);
      sb.append(dimensionKey.getPrefix()).append(dimensionKey.getKey()).append("|");
      dimension = sb.toString();
    }
  }

  public String getDimension() {

    String dimensioOptions = dimension.substring(0, dimension.length() - 1);
    dimension = null;
    return dimensioOptions;
  }
}
