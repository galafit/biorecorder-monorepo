package com.biorecorder.data.frame.impl;

import com.biorecorder.data.sequence.ShortSequence;

class ShortMin extends ShortAggFunction {
    private short min;

    @Override
    public int add(ShortSequence sequence, int from, int length) {
        if(count == 0) {
            min = sequence.get(from);
        }
        for (int i = 0; i < length; i++) {
            min = (short)Math.min(min, sequence.get(from + i));
        }
        count +=length;
        return count;
    }

    @Override
    protected short getValue1() {
        return min;
    }
}
