package com.biorecorder.data.frame_new.aggregation;

import com.biorecorder.data.frame_new.Column;
import com.biorecorder.data.sequence.IntSequence;

public interface Binning {
    IntSequence group(Column col);
    boolean isEqualPoints();
    int pointsInGroup();
}
