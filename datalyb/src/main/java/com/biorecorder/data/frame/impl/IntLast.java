package com.biorecorder.data.frame.impl;

import com.biorecorder.data.sequence.IntSequence;

class IntLast extends IntAggFunction {
    private int last;

    @Override
    public int add(IntSequence sequence, int from, int length) {
        last = sequence.get(from + length - 1);
        count +=length;
        return count;
    }

    @Override
    protected int getValue1() {
        return last;
    }
}
