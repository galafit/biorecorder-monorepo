package com.biorecorder.data.frame.impl;

import com.biorecorder.data.sequence.FloatSequence;

class FloatMax extends FloatAggFunction {
    private float max;

    @Override
    public int add(FloatSequence sequence, int from, int length) {
        if(count == 0) {
            max = sequence.get(from);
        }
        for (int i = 0; i < length; i++) {
            max = (float)Math.max(max, sequence.get(from + i));
        }
        count +=length;
        return count;
    }

    @Override
    protected float getValue1() {
        return max;
    }
}
