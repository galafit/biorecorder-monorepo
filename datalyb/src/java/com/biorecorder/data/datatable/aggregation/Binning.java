package com.biorecorder.data.datatable.aggregation;

import com.biorecorder.data.datatable.Column;
import com.biorecorder.data.sequence.IntSequence;

public interface Binning {
    IntSequence group(Column col);
    boolean isEqualPoints();
    int pointsInGroup();
}
