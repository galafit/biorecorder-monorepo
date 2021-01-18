package com.biorecorder.data.frame.impl;

import com.biorecorder.data.sequence.LongSequence;

class LongMax extends LongAggFunction {
    private long max;

    @Override
    public int add(LongSequence sequence, int from, int length) {
        if(count == 0) {
            max = sequence.get(from);
        }
        for (int i = 0; i < length; i++) {
            max = (long)Math.max(max, sequence.get(from + i));
        }
        count +=length;
        return count;
    }

    @Override
    protected long getValue1() {
        return max;
    }
}
