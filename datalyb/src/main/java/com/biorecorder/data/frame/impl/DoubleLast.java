package com.biorecorder.data.frame.impl;

import com.biorecorder.data.sequence.DoubleSequence;

class DoubleLast extends DoubleAggFunction {
    private double last;

    @Override
    public int add(DoubleSequence sequence, int from, int length) {
        last = sequence.get(from + length - 1);
        count +=length;
        return count;
    }

    @Override
    protected double getValue1() {
        return last;
    }
}
