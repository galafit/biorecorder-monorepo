package com.biorecorder.data.frame.impl;

import com.biorecorder.data.sequence.ShortSequence;
import com.biorecorder.data.utils.PrimitiveUtils;


class ShortAverage extends ShortAggFunction {
    private long sum;

    @Override
    public int add(ShortSequence sequence, int from, int length) {
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
    protected short getValue1() {
        return PrimitiveUtils.long2short(sum / count);
    }
}
