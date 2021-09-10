package com.biorecorder.filters.oldfilters;


import com.biorecorder.datalyb.series.IntSeries;

/**
 *
 */
public abstract class Function implements IntSeries {
    protected IntSeries inputData;

    protected Function(IntSeries inputData) {
        this.inputData = inputData;
    }

    @Override
    public int size() {
        return inputData.size();
    }
}

