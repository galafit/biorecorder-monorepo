package com.biorecorder.bichart.traces;

public class NamedValue {
    private final String value;
    private final String valueName;

    public NamedValue(String valueName, String value) {
        this.valueName = valueName;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getValueName() {
        return valueName;
    }
}
