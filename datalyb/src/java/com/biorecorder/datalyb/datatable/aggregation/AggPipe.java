package com.biorecorder.datalyb.datatable.aggregation;

import com.biorecorder.datalyb.datatable.Column;

interface AggPipe {
    void agg(int index);
    void push();
    Column resultantCol();
    void setColumnToAgg(Column columnToAgg) throws IllegalArgumentException;
    // call this method after finalize aggregation to remove unnecesary reference!!!
    void removeColumnToAgg();
}