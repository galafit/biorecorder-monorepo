package com.biorecorder.data.datatable.aggregation;

import com.biorecorder.data.datatable.Column;

interface AggPipe {
    void agg(int index);
    void push();
    Column resultantCol();
    void setColumnToAgg(Column columnToAgg) throws IllegalArgumentException;
}