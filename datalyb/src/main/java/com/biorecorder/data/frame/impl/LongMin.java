package com.biorecorder.data.frame.impl;

import com.biorecorder.data.sequence.LongSequence;

class LongMin extends LongAggFunction {
    private long min;

    @Override
    public int add(LongSequence sequence, int from, int length) {
        if(count == 0) {
            min = sequence.get(from);
        }
        for (int i = 0; i < length; i++) {
            min = (long)Math.min(min, sequence.get(from + i));
        }
        count +=length;
        return count;
    }

    @Override
    protected long getValue1() {
        return min;
    }
}
