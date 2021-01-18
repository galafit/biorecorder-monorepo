package com.biorecorder.data.frame.impl;

import com.biorecorder.data.sequence.FloatSequence;

class FloatFirst extends FloatAggFunction {
    private float first;

    @Override
    public int add(FloatSequence sequence, int from, int length) {
        if(count == 0) {
            first = sequence.get(from);
        }
        count +=length;
        return count;
    }

    @Override
    protected float getValue1() {
        return first;
    }
}
