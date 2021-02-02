package com.biorecorder.bichart.dataprocessing;

/**
 * Created by galafit on 23/5/19.
 */
public enum GroupApproximation {
    AVERAGE,
    SUM,
    OPEN,
    CLOSE,
    LOW,
    HIGH,
    RANGE,
    OHLC;

    public GroupApproximation[] getAsArray() {
       if(this == RANGE) {
           GroupApproximation[] approximations = {LOW, HIGH};
           return approximations;
       } else if (this == OHLC) {
           GroupApproximation[] approximations = {OPEN, HIGH, LOW, CLOSE};
           return approximations;
       } else {
           GroupApproximation[] approximations = {this};
           return approximations;
       }
    }
}
