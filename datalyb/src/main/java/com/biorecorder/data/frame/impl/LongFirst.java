package com.biorecorder.data.frame.impl;

import com.biorecorder.data.sequence.LongSequence;

class LongFirst extends LongAggFunction {
    private long first;

    @Override
    public int add(LongSequence sequence, int from, int length) {
        if(count == 0) {
            first = sequence.get(from);
        }
        count +=length;
        return count;
    }

    @Override
    protected long getValue1() {
        return first;
    }
}
