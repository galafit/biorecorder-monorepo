package com.biorecorder.bichart;

import com.biorecorder.data.frame_new.aggregation.AggFunction;
import com.biorecorder.data.frame_new.aggregation.Average;
import com.biorecorder.data.frame_new.aggregation.First;

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

    public AggFunction getAggregation() {
        if(this == OPEN) {
            return new First();
        } else if (this == AVERAGE) {
            return new Average();
        } else {
            return new First();
        }
    }
}
