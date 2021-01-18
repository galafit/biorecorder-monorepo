package com.biorecorder.data.frame.impl;

import com.biorecorder.data.sequence.DoubleSequence;

class DoubleFirst extends DoubleAggFunction {
    private double first;

    @Override
    public int add(DoubleSequence sequence, int from, int length) {
        if(count == 0) {
            first = sequence.get(from);
        }
        count +=length;
        return count;
    }

    @Override
    protected double getValue1() {
        return first;
    }
}
