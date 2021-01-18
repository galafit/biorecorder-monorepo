package com.biorecorder.data.frame.impl;

import com.biorecorder.data.sequence.FloatSequence;

class FloatLast extends FloatAggFunction {
    private float last;

    @Override
    public int add(FloatSequence sequence, int from, int length) {
        last = sequence.get(from + length - 1);
        count +=length;
        return count;
    }

    @Override
    protected float getValue1() {
        return last;
    }
}
