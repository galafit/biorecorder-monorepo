package com.biorecorder.data.frame_new.aggregation;

import com.biorecorder.data.frame_new.Column;
import com.biorecorder.data.sequence.IntSequence;

public interface Grouper {
    public IntSequence group(Column col);
}
