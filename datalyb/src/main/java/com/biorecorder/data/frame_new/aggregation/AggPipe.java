package com.biorecorder.data.frame_new.aggregation;

import com.biorecorder.data.frame_new.Column;

interface AggPipe {
    void agg(int index);
    void push();
    Column resultantCol();
    void setColumnToAgg(Column columnToAgg) throws IllegalArgumentException;
}