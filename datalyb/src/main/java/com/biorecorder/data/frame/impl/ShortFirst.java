package com.biorecorder.data.frame.impl;

import com.biorecorder.data.sequence.ShortSequence;

class ShortFirst extends ShortAggFunction {
    private short first;

    @Override
    public int add(ShortSequence sequence, int from, int length) {
        if(count == 0) {
            first = sequence.get(from);
        }
        count +=length;
        return count;
    }

    @Override
    protected short getValue1() {
        return first;
    }
}
