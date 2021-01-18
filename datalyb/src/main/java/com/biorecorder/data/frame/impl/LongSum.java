package com.biorecorder.data.frame.impl;

import com.biorecorder.data.sequence.LongSequence;
import com.biorecorder.data.utils.PrimitiveUtils;

/**
 * Created by galafit on 5/4/19.
 */
class LongSum extends LongAggFunction {
    private long sum;

    @Override
    public int add(LongSequence sequence, int from, int length) {
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
    protected long getValue1() {
        return sum;
    }
}
