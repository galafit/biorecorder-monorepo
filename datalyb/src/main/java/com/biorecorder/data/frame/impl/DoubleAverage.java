package com.biorecorder.data.frame.impl;

import com.biorecorder.data.sequence.DoubleSequence;
import com.biorecorder.data.utils.PrimitiveUtils;


class DoubleAverage extends DoubleAggFunction {
    private double sum;

    @Override
    public int add(DoubleSequence sequence, int from, int length) {
        if(count == 0) {
            sum = 0;
        }
        for (int i = 0; i < length; i++) {
           sum += sequence.get(from + i);
        }
        count +=length;
        return count;
    }

    @Override
    protected double getValue1() {
        return sum / count;
    }
}
