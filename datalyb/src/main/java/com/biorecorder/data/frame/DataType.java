package com.biorecorder.data.frame;

/**
 * Created by galafit on 13/1/19.
 */
public enum DataType {
    Short("short", true),
    Integer("int", true),
    Long("long", true),
    Float("float", true),
    Double("double", true),
    String("String",false);

    private boolean isNumber;
    private String label;

    DataType(String label, boolean isNumber) {
        this.label = label;
        this.isNumber = isNumber;
    }

    public boolean isNumber() {
        return isNumber;
    }

    @Override
    public String toString() {
        return label;
    }
}
