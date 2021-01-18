package com.biorecorder.data.frame.impl;

import com.biorecorder.data.sequence.ShortSequence;

class ShortMax extends ShortAggFunction {
    private short max;

    @Override
    public int add(ShortSequence sequence, int from, int length) {
        if(count == 0) {
            max = sequence.get(from);
        }
        for (int i = 0; i < length; i++) {
            max = (short)Math.max(max, sequence.get(from + i));
        }
        count +=length;
        return count;
    }

    @Override
    protected short getValue1() {
        return max;
    }
}
