package com.biorecorder.filters.oldfilters;

import com.biorecorder.datalyb.series.IntSeries;

/**
 *
 */

public class FilterDerivativeRem extends Function {
    private static final int DEFAULT_DISTANCE_MSEC = 120;
    private int distance_point;

    public FilterDerivativeRem(IntSeries inputData, int timeMs, double sampleRate) {
        super(inputData);

        distance_point = Math.round((float)(timeMs * sampleRate / 1000));
        if(distance_point== 0) {
            distance_point = 1;
        }
    }

    public FilterDerivativeRem(IntSeries inputData, double sampleRate) {
       this(inputData, DEFAULT_DISTANCE_MSEC, sampleRate);
    }

    @Override
    public int get(int index) {
        if (index < distance_point) {
            return 0;
        }
        return inputData.get(index) - inputData.get(index - distance_point);
        //return Math.abs(inputData.get(index)) - Math.abs(inputData.get(index - distance_point));
    }
}

