package com.biorecorder.data.frame.impl;

import com.biorecorder.data.sequence.FloatSequence;
import com.biorecorder.data.utils.PrimitiveUtils;

/**
 * Created by galafit on 5/4/19.
 */
class FloatSum extends FloatAggFunction {
    private double sum;

    @Override
    public int add(FloatSequence sequence, int from, int length) {
        if(count == 0) {
            sum = 0;
        }
        for (int i = 0; i < length; i++) {
            sum += sequence.get(from + i);
        }
        count +=length;
        return count;
    }

    @Override
    protected float getValue1() {
        return PrimitiveUtils.double2float(sum);
    }
}
