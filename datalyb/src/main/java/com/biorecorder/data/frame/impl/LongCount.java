package com.biorecorder.data.frame.impl;

import com.biorecorder.data.sequence.LongSequence;

class LongCount extends LongAggFunction {
    @Override
    public int add(LongSequence sequence, int from, int length) {
        count +=length;
        return count;
    }

    @Override
    protected long getValue1() {
        return (long)count;
    }
}
