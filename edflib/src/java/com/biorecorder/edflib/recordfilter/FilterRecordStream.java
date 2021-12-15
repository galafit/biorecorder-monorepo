package com.biorecorder.edflib.recordfilter;

import com.biorecorder.edflib.DataHeader;
import com.biorecorder.edflib.DataRecordStream;

import java.util.ArrayList;
import java.util.List;

/**
 * FilterRecordStream is  a wrapper of an already existing
 * RecordStreams (the underlying streams)
 * which do some transforms with input data records before
 * to write them to the underlying streams.
 */
public class FilterRecordStream implements com.biorecorder.edflib.DataRecordStream {
    protected DataHeader inConfig;
    protected DataHeader outConfig;
    protected int[] outRecord;
    protected com.biorecorder.edflib.DataRecordStream[] outStreams;

    public FilterRecordStream(com.biorecorder.edflib.DataRecordStream... outStream) {
        this.outStreams = outStream;
    }

    protected void sendData(int[] dataRecord) {
        for (DataRecordStream outStream : outStreams) {
            outStream.writeDataRecord(dataRecord);
        }
    }

    @Override
    public void setHeader(DataHeader header) {
        inConfig = header;
        outConfig = getOutConfig();
        for (DataRecordStream outStream : outStreams) {
            outStream.setHeader(outConfig);
        }
        outRecord = new int[outConfig.getRecordSize()];
    }

    @Override
    public void writeDataRecord(int[] dataRecord) {
        sendData(dataRecord);
    }

    @Override
    public void close() {
        for (DataRecordStream outStream : outStreams) {
            outStream.close();
        }
    }

    protected DataHeader getOutConfig() {
        return inConfig;
    }
}
