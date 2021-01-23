package com.biorecorder.edfviewer;

import com.biorecorder.multisignal.edflib.DataHeader;

class EdfPosition {
    private static final int SECOND_MS = 1000;
    private final DataHeader header;
    private final int recordLength;
    private long sampleCounter;
    private int record; // record number
    private int signal; // signal number
    private int sampleInRecord; // signal sample position in data record

    public EdfPosition(DataHeader dataHeader) {
        this.header = dataHeader;
        recordLength = dataHeader.getRecordSize();
    }

    public void setPosition(int record) {
        this.record = record;
        signal = 0;
        sampleInRecord = 0;
        sampleCounter = record * recordLength;
    }

    /**
     * @param timeMs
     * @return the record number that begin recording or was recording at the given time
     */
    public int getRecord(long timeMs) {
        if(timeMs < header.getRecordingStartTimeMs()) {
            return -1;
        }
        return (int) Math.floor((timeMs - header.getRecordingStartTimeMs()) / (header.getDurationOfDataRecord() * SECOND_MS));
    }

    /**
     * @param timeMs
     * @return the sample of data record belonging to the given signal that begin recording or was recording at the given time
     * @throws IllegalArgumentException if signal >= numberOfSignals
     */
    public int getSampleInRecord(int signal, long timeMs) throws IllegalArgumentException  {
        if(signal >= header.numberOfSignals()) {
            String errMsg = "Number of signals: " + header.numberOfSignals()+ ", signal: " + signal;
            throw new IllegalArgumentException(errMsg);
        }
        long samples = (long) Math.floor(header.getSampleFrequency(signal) * (timeMs - header.getRecordingStartTimeMs()) / SECOND_MS);
        return (int) (samples % header.getNumberOfSamplesInEachDataRecord(signal));
    }


    public long getBytePosition() {
        return sampleCounter * header.getNumberOfBytesPerSample() + header.getNumberOfBytesInHeaderRecord();
    }

    public void next() {
        sampleCounter++;
        sampleInRecord++;
        if(sampleInRecord == header.getNumberOfSamplesInEachDataRecord(signal)) {
            sampleInRecord = 0;
            signal++;
        }
        if(signal == header.numberOfSignals()) {
            signal = 0;
            record++;
        }
    }

    /**
     *
     * @return the number of skipped samples
     */
    public int skipCurrentSignalSamples() {
        int samplesToSkip = header.getNumberOfSamplesInEachDataRecord(signal) - sampleInRecord;
        sampleCounter += samplesToSkip;
        sampleInRecord = 0;
        signal++;
        if(signal == header.numberOfSignals()) {
            signal = 0;
            record++;
        }
        return samplesToSkip;
    }

    public int getNumberOfSamplesInEachDataRecord (int signal) {
        return header.getNumberOfSamplesInEachDataRecord(signal);
    }


    /**
     * @throws IllegalArgumentException if signal >= numberOfSignals
     */
    public void setPosition (int signal, long position) throws IllegalArgumentException {
        if(signal >= header.numberOfSignals()) {
            String errMsg = "Number of signals: " + header.numberOfSignals()+ ", signal: " + signal;
            throw new IllegalArgumentException(errMsg);
        }
        int records = (int) (position / header.getNumberOfSamplesInEachDataRecord(signal));
        sampleInRecord = (int) (position - records * header.getNumberOfSamplesInEachDataRecord(signal));
        this.signal = signal;
        this.sampleCounter = records * recordLength + sampleInRecord;
        for (int i = 0; i < signal; i++) {
            this.sampleCounter += header.getNumberOfSamplesInEachDataRecord(i);
        }
    }

    public void setSampleCounter(long sampleCounter)  {
        this.sampleCounter = sampleCounter;
        int records = (int) (sampleCounter / recordLength);
        int samples = (int) (sampleCounter - records * recordLength);

        for (int i = 0; i < header.numberOfSignals(); i++) {
            if(samples >= header.getNumberOfSamplesInEachDataRecord(i)) {
                samples -= header.getNumberOfSamplesInEachDataRecord(i);
            } else {
                signal = i;
                sampleInRecord = samples;
                break;
            }
        }
    }

    // skip n data samples
    public void skip(long n) {
        setSampleCounter(sampleCounter + n);
    }
}
