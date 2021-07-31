package com.biorecorder.bichart;

/**
 * Created by galafit on 23/5/19.
 */
public enum GroupingApproximation {
    AVERAGE,
    SUM,
    OPEN,
    CLOSE,
    LOW,
    HIGH,
    RANGE,
    OHLC;

    public GroupingApproximation[] getAsArray() {
       if(this == RANGE) {
           GroupingApproximation[] approximations = {LOW, HIGH};
           return approximations;
       } else if (this == OHLC) {
           GroupingApproximation[] approximations = {OPEN, HIGH, LOW, CLOSE};
           return approximations;
       } else {
           GroupingApproximation[] approximations = {this};
           return approximations;
       }
    }
}
