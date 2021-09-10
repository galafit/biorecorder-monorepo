package com.biorecorder.filters.oldfilters;

import com.biorecorder.datalyb.series.IntSeries;

/**
 *
 */
public class FilterAlfa extends Function {
    private int period = 4;
    private int bufferHalf = period * 4;
    private IntSeries alfaData;

    public FilterAlfa(IntSeries inputData) {
        super(inputData);
        alfaData = new FilterHiPassSymmetric(new FilterBandPass_Alfa(inputData), 2);
    }

    @Override
    public int get(int index) {
        if(index < 1 ) {
            return 0;
        }
        else {
            return Math.max(Math.abs(alfaData.get(index)) , Math.abs(alfaData.get(index-1)));

        }
    }
}