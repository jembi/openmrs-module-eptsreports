package org.openmrs.module.eptsreports.reporting.library.dimensions;

public enum GenderDimensionKey implements DimensionKey{

    female("F"),
    male("M");

    private String key;
    private GenderDimensionKey(String key){
        this.key = key;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getKeyValue() {
        return null;
    }
}
