package com.biorecorder.bichart.configs;

import com.biorecorder.data.datatable.aggregation.*;

/**
 * Created by galafit on 23/5/19.
 */
public enum GroupingApproximation {
    AVERAGE,
    OPEN,
    LOW,
    HIGH;
    //CLOSE,
    //RANGE,
   // OHLC;


    public AggFunction getAggregation() {
        switch (this) {
            case OPEN:
                return new First();
            case HIGH:
                return new Max();
            case LOW:
                return new Min();
            default:
                return new Average();
        }
    }
}
