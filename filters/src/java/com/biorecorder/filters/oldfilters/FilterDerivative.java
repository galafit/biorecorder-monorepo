package com.biorecorder.filters.oldfilters;

import com.biorecorder.data.sequence.IntSequence;

/**
 *
 */

public class FilterDerivative extends Function {

    public FilterDerivative(IntSequence inputData) {
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
