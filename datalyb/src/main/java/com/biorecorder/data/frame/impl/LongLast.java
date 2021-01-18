package com.biorecorder.data.frame.impl;

import com.biorecorder.data.sequence.LongSequence;

class LongLast extends LongAggFunction {
    private long last;

    @Override
    public int add(LongSequence sequence, int from, int length) {
        last = sequence.get(from + length - 1);
        count +=length;
        return count;
    }

    @Override
    protected long getValue1() {
        return last;
    }
}
