package com.biorecorder.data.frame.impl;

import com.biorecorder.data.sequence.ShortSequence;

class ShortLast extends ShortAggFunction {
    private short last;

    @Override
    public int add(ShortSequence sequence, int from, int length) {
        last = sequence.get(from + length - 1);
        count +=length;
        return count;
    }

    @Override
    protected short getValue1() {
        return last;
    }
}
