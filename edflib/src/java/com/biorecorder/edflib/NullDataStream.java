package com.biorecorder.edflib;

public class NullDataStream implements DataRecordStream{

    @Override
    public void setHeader(DataHeader header) {
        // do nothing
    }

    @Override
    public void writeDataRecord(int[] dataRecord) {
        // do nothing
    }

    @Override
    public void close() {
       // do nothing
    }
}
