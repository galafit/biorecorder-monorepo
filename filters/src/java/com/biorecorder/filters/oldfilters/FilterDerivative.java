package com.biorecorder.filters.oldfilters;

import com.biorecorder.datalyb.series.IntSeries;

/**
 *
 */

public class FilterDerivative extends Function {

    public FilterDerivative(IntSeries inputData) {
        super(inputData);
    }

    @Override
    public int get(int index) {
        if (index == 0) {
            return 0;
        }
        return inputData.get(index) - inputData.get(index - 1);
    }
}
