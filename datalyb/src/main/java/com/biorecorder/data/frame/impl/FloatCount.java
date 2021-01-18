package com.biorecorder.data.frame.impl;

import com.biorecorder.data.sequence.FloatSequence;

class FloatCount extends FloatAggFunction {
    @Override
    public int add(FloatSequence sequence, int from, int length) {
        count +=length;
        return count;
    }

    @Override
    protected float getValue1() {
        return (float)count;
    }
}
