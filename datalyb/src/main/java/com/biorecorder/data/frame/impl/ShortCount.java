package com.biorecorder.data.frame.impl;

import com.biorecorder.data.sequence.ShortSequence;

class ShortCount extends ShortAggFunction {
    @Override
    public int add(ShortSequence sequence, int from, int length) {
        count +=length;
        return count;
    }

    @Override
    protected short getValue1() {
        return (short)count;
    }
}
