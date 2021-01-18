package com.biorecorder.data.frame.impl;

import com.biorecorder.data.sequence.DoubleSequence;

class DoubleCount extends DoubleAggFunction {
    @Override
    public int add(DoubleSequence sequence, int from, int length) {
        count +=length;
        return count;
    }

    @Override
    protected double getValue1() {
        return (double)count;
    }
}
