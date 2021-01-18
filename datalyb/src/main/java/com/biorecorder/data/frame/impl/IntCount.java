package com.biorecorder.data.frame.impl;

import com.biorecorder.data.sequence.IntSequence;

class IntCount extends IntAggFunction {
    @Override
    public int add(IntSequence sequence, int from, int length) {
        count +=length;
        return count;
    }

    @Override
    protected int getValue1() {
        return (int)count;
    }
}
