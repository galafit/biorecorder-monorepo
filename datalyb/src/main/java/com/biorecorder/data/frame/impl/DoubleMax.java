package com.biorecorder.data.frame.impl;

import com.biorecorder.data.sequence.DoubleSequence;

class DoubleMax extends DoubleAggFunction {
    private double max;

    @Override
    public int add(DoubleSequence sequence, int from, int length) {
        if(count == 0) {
            max = sequence.get(from);
        }
        for (int i = 0; i < length; i++) {
            max = (double)Math.max(max, sequence.get(from + i));
        }
        count +=length;
        return count;
    }

    @Override
    protected double getValue1() {
        return max;
    }
}
