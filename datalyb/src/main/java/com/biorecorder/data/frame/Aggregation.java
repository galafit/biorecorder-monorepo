package com.biorecorder.data.frame;


/**
 * Created by galafit on 25/11/17.
 */
public enum Aggregation {
    AVERAGE("Average"),
    SUM("Sum"),
    COUNT("Count"),
    MIN("Min"),
    MAX("Max"),
    FIRST("First"),
    LAST("Last");

    private String functionName;

    Aggregation(String functionName) {
        this.functionName = functionName;
    }

    @Override
    public String toString() {
        return functionName;
    }
}
