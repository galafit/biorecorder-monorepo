package com.biorecorder.edfviewer;

import com.biorecorder.filters.digitalfilter.IntDigitalFilter;
import com.biorecorder.multisignal.edflib.DataHeader;
import com.biorecorder.multisignal.edflib.HeaderException;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class EdfDataProvider {
    BufferedEdfReader edfReader;
    SignalListener[] signalListeners;

    public EdfDataProvider(File file, int bufferSize) throws FileNotFoundException, HeaderException, IOException {
        edfReader = new BufferedEdfReader(file, bufferSize);
        signalListeners = new SignalListener[edfReader.getNumberOfSignals()];
    }

    public void close() throws IOException {
        edfReader.close();
    }

    public void addSignalDataListener(int signal, DataListener l, IntDigitalFilter ... filters) throws IllegalArgumentException {
        if(signal >= signalListeners.length) {
            String errMsg = "Number of signals: " + signalListeners.length+ ", signal: " + signal;
            throw new IllegalArgumentException(errMsg);
        }
        signalListeners[signal] = new SignalListener(l, filters);
    }

    public void provideData(long startTimeMs, long endTimeMs) throws IllegalArgumentException, IOException {
        if(startTimeMs > endTimeMs) {
            throw new IllegalArgumentException("startTime: " + startTimeMs + " > " + "endTime: " + endTimeMs);
        }
        if(startTimeMs == endTimeMs) {
            return;
        }
        DataHeader header = edfReader.getHeader();
        int endRecord = header.getRecord(endTimeMs);
        if(endRecord < 0) {
            return;
        }

        int startRecord = 0;
        int signals = header.numberOfSignals();
        SignalManager[] signalManagers = new SignalManager[signals];
        for (int i = 0; i < signals; i++) {
            SignalListener signalListener = signalListeners[i];
            if(signalListener != null) {
                SignalManager sm = new SignalManager(i, signalListener,header, startTimeMs, endTimeMs);
                signalManagers[i] = sm;
                startRecord = Math.max(startRecord, sm.getStartRecord());
                sm.onStart();
            }
        }
        edfReader.setPosition(startRecord);
        for (int record = startRecord; record <= endRecord; record++) {
            for (int i = 0; i < signals; i++) {
                SignalManager signalManager = signalManagers[i];
                if(signalManager == null) {
                    edfReader.skipCurrentSignalSamples();
                } else {
                    edfReader.skipSamples(signalManager.getStartSampleInRecord(record));
                    int samplesToRead = signalManager.getNumberOfSamplesToRead(record);
                    for (int j = 0; j < samplesToRead; j++) {
                        try{
                            signalManager.sendData(edfReader.nextDigitalSample());
                        } catch (EOFException ex) {
                            stopProviding(signalManagers);
                            return;
                        }
                    }
                }
            }
        }
        stopProviding(signalManagers);
    }


    private void stopProviding(SignalManager[] signalsListeners) {
        for (int i = 0; i < signalsListeners.length; i++) {
            SignalManager listener = signalsListeners[i];
            if(listener != null) {
                listener.onFinish();
            }
        }
    }

    class SignalListener {
        private DataListener l;
        private IntDigitalFilter[] filters;
        private int filterLength;

        public SignalListener(DataListener l, IntDigitalFilter[] filters) {
            this.l = l;
            this.filters = filters;
            filterLength = 1;
            for (int i = 0; i < filters.length; i++) {
                filterLength *= filters[i].getFilterLength();
            }
        }

        public int filter(int data) {
            int result = data;
            for (int i = 0; i < filters.length; i++) {
                result = filters[i].filteredValue(result);
            }
            return result;
        }

        public void send(double data) {
            l.onDataReceived(data);
        }

        public void onFinish() {
            l.onFinish();
        }

        public void onStart() {
            l.onStart();
        }

        public int  filterLength() {
            return filterLength;
        }
    }


    class SignalManager {
        private SignalListener listener;
        private int startRecord ;
        private int endRecord;
        private int samplesInRecord;
        private int startSample; // including
        private int endSample; // including
        private int prefilterSamples;
        private int signalOffset;
        private double signalGain;

        public SignalManager(int signal, SignalListener listener, DataHeader header, long  startTime, long endTime ) {
            this.listener = listener;
            prefilterSamples = listener.filterLength() - 1;
            samplesInRecord = header.getNumberOfSamplesInEachDataRecord(signal);
            signalOffset = header.getSignalOffset(signal);
            signalGain = header.getSignalGain(signal);
            long startTime1 = startTime - prefilterSamples * header.getSampleIntervalMs(signal);
            startRecord = header.getRecord(startTime1);
            startSample = header.getSampleInRecord(signal, startTime1);
            if(startRecord < 0) {
                startRecord = 0;
                startSample = 0;
            }
            endRecord = header.getRecord(endTime);
            endRecord = header.getSampleInRecord(signal, endTime);
        }

        public int getStartRecord() {
            return startRecord;
        }

        int getStartSampleInRecord(int record) {
            if(record == startRecord) {
                return startSample;
            } else if( record > startRecord && record <= endRecord) {
                return 0;
            }
            return samplesInRecord;
        }

        int getNumberOfSamplesToRead(int record) {
            if(startRecord == endRecord && record == startRecord) {
                return endSample - startSample + 1;
            }

            if(record == startRecord) {
                return samplesInRecord - startSample;
            } else if(record == endSample) {
                return endSample + 1;
            } else if(record > startRecord && record < endRecord) {
                return samplesInRecord;
            }

            return 0;
        }


        void sendData(int digValue) {
            // digValue + signalOffset is proportional physValue
            listener.filter(digValue + signalOffset);
            prefilterSamples--;
            if(prefilterSamples > 0) {
                listener.filter(digValue);
                prefilterSamples--;
            } else {
                listener.send(listener.filter(digValue + signalOffset) * signalGain);
            }
        }

        void onStart() {
            listener.onStart();
        }

        void onFinish() {
            listener.onFinish();
        }
    }
}
