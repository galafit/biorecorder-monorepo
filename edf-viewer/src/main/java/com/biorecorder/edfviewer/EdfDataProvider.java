package com.biorecorder.edfviewer;

import com.biorecorder.multisignal.edflib.HeaderException;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class EdfDataProvider {
    BufferedEdfReader edfReader;
    DataListener[] listeners;

    public EdfDataProvider(File file, int bufferSize) throws FileNotFoundException, HeaderException, IOException {
        edfReader = new BufferedEdfReader(file, bufferSize);
        listeners = new DataListener[edfReader.getNumberOfSignals()];
    }

    public void close() throws IOException {
        edfReader.close();
    }

    public void addSignalDataListener(int signal, DataListener l) throws IllegalArgumentException {
        if(signal >= listeners.length) {
            String errMsg = "Number of signals: " + listeners.length+ ", signal: " + signal;
            throw new IllegalArgumentException(errMsg);
        }
        listeners[signal] = l;
    }

    public void provideData(long startTimeMs, long endTimeMs) throws IllegalArgumentException, IOException {
        if(startTimeMs > endTimeMs) {
            throw new IllegalArgumentException("startTime: " + startTimeMs + " > " + "endTime: " + endTimeMs);
        }
        if(startTimeMs == endTimeMs) {
            return;
        }
        int startRecord = edfReader.getRecord(startTimeMs);
        int endRecord = edfReader.getRecord(endTimeMs);
        if(endRecord < 0) {
            return;
        }
        int signalsNumber = edfReader.getNumberOfSignals();
        SignalInfo[] signalsListeners = new SignalInfo[signalsNumber];
        for (int i = 0; i < signalsNumber; i++) {
            DataListener listener = listeners[i];
            if(listener != null) {
                SignalInfo signalInfo = new SignalInfo();
                signalInfo.startRecord = startRecord;
                signalInfo.endRecord = endRecord;
                signalInfo.startRecordSample = edfReader.getSampleInRecord(i, startTimeMs);
                signalInfo.endRecordSample = edfReader.getSampleInRecord(i, endTimeMs);
                signalInfo.samplesInRecord = edfReader.getNumberOfSamplesInEachDataRecord(i);
                signalInfo.listener = listener;
                signalsListeners[i] = signalInfo;
            }
        }
        edfReader.setPosition(startRecord);
        for (int record = startRecord; record <= endRecord; record++) {
            for (int i = 0; i < signalsNumber; i++) {
                SignalInfo signalInfo = signalsListeners[i];
                if(signalInfo == null) {
                    edfReader.skipCurrentSignalSamples();
                } else {
                    edfReader.skipSamples(signalInfo.numberOfSamplesToRead(record));
                    int samplesToRead = signalInfo.numberOfSamplesToRead(record);
                    for (int j = 0; j < samplesToRead; j++) {
                        try{
                            signalInfo.sendData(edfReader.nextSample());
                        } catch (EOFException ex) {
                            stopProviding(signalsListeners);
                            return;
                        }
                    }
                }
            }
        }
        stopProviding(signalsListeners);
    }

    private void stopProviding(SignalInfo[] signalsListeners) {
        for (int i = 0; i < signalsListeners.length; i++) {
            SignalInfo listener = signalsListeners[i];
            if(listener != null) {
                listener.finishRecording();
            }
        }
    }


    class SignalInfo {
        DataListener listener;
        int startRecord;
        int endRecord;
        int samplesInRecord;
        int startRecordSample;
        int endRecordSample;

        int numberOfSamplesToSkip(int record) {
            if(record == startRecord) {
                return startRecordSample;
            }
            return 0;
        }

        int numberOfSamplesToRead(int record) {
            if(startRecord == endRecord) {
                if(record == startRecord) {
                    return endRecordSample - startRecordSample;
                }
                return 0;
            }
            if(record == startRecord) {
                return samplesInRecord - startRecordSample;
            } else if(record == endRecordSample) {
                return endRecordSample;
            }
            return samplesInRecord;
        }

        void sendData(int value) {
            listener.onDataReceived(value);
        }

        void finishRecording() {
            listener.onFinish();
        }
    }

}
