package com.biorecorder.bichart.scales;

/**
 * Created by galafit on 5/9/17.
 */
public class Tick {
    private double value;
    private String label;

    public Tick(double tickValue, String tickLabel) {
        this.value = tickValue;
        this.label = tickLabel;
    }

    public double getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }
}