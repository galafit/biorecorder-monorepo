package com.biorecorder.data.frame.impl;

import com.biorecorder.data.sequence.IntSequence;

class IntMin extends IntAggFunction {
    private int min;

    @Override
    public int add(IntSequence sequence, int from, int length) {
        if(count == 0) {
            min = sequence.get(from);
        }
        for (int i = 0; i < length; i++) {
            min = (int)Math.min(min, sequence.get(from + i));
        }
        count +=length;
        return count;
    }

    @Override
    protected int getValue1() {
        return min;
    }
}
