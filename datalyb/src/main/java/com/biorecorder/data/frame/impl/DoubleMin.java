package com.biorecorder.data.frame.impl;

import com.biorecorder.data.sequence.DoubleSequence;

class DoubleMin extends DoubleAggFunction {
    private double min;

    @Override
    public int add(DoubleSequence sequence, int from, int length) {
        if(count == 0) {
            min = sequence.get(from);
        }
        for (int i = 0; i < length; i++) {
            min = (double)Math.min(min, sequence.get(from + i));
        }
        count +=length;
        return count;
    }

    @Override
    protected double getValue1() {
        return min;
    }
}
