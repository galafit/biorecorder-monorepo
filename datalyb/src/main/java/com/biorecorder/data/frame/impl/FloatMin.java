package com.biorecorder.data.frame.impl;

import com.biorecorder.data.sequence.FloatSequence;

class FloatMin extends FloatAggFunction {
    private float min;

    @Override
    public int add(FloatSequence sequence, int from, int length) {
        if(count == 0) {
            min = sequence.get(from);
        }
        for (int i = 0; i < length; i++) {
            min = (float)Math.min(min, sequence.get(from + i));
        }
        count +=length;
        return count;
    }

    @Override
    protected float getValue1() {
        return min;
    }
}
