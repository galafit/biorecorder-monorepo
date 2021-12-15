package com.biorecorder.edflib.recordfilter;

import com.biorecorder.edflib.DataHeader;
import com.biorecorder.edflib.FormatVersion;

import java.util.ArrayList;
import java.util.List;

/**
 * Permit to omit samples from some channels (delete signals)
 */
public class SignalRemover extends FilterRecordStream {
    private List<Integer> signalsToRemove = new ArrayList<Integer>();
    private boolean[] signalsMask;

    public SignalRemover(com.biorecorder.edflib.DataRecordStream outStream) {
        super(outStream);
    }

    @Override
    public void setHeader(DataHeader header) {
        signalsMask = new boolean[header.numberOfSignals()];
        for (int i = 0; i < signalsMask.length; i++) {
            signalsMask[i] = true;
        }
        for (int i = 0; i < signalsToRemove.size(); i++) {
            int signalToRemove = signalsToRemove.get(i);
            if(signalToRemove < signalsMask.length) {
                signalsMask[signalToRemove] = false;
            }
        }
        super.setHeader(header);
    }

    /**
     * Indicates that the samples from the given signal should be omitted in
     * out data records. This method can be called only
     * before method  setHeader()!
     *
     * @param signalNumber number of the signal
     *                     whose samples should be omitted. Numbering starts from 0.
     */
    public void removeSignal(int signalNumber) {
        signalsToRemove.add(signalNumber);
    }

    @Override
    protected DataHeader getOutConfig() {
        DataHeader outConfig = new DataHeader(inConfig);
        for (int i = signalsMask.length - 1; i >=0 ; i--) {
           if(! signalsMask[i]) {
               outConfig.removeSignal(i);
           }
        }
        return outConfig;
    }

    /**
     * Omits data from the "deleted" channels and
     * create out array of samples
     */
    @Override
    public void writeDataRecord(int[] inputRecord)  {
        int signalStartInRecord = 0;
        int signalStartOutRecord = 0;
        for (int signalNumber = 0; signalNumber < signalsMask.length; signalNumber++) {
            int signalSamples = inConfig.getNumberOfSamplesInEachDataRecord(signalNumber);
            if(signalsMask[signalNumber]) {
                System.arraycopy(inputRecord, signalStartInRecord, outRecord, signalStartOutRecord, signalSamples);
                signalStartOutRecord += signalSamples;
            }
            signalStartInRecord += signalSamples;
        }
        sendData(outRecord);
    }

    /**
     * Unit Test. Usage Example.
     */
    public static void main(String[] args) {

        // 0 channel 1 sample, 1 channel 2 samples, 2 channel 3 samples, 3 channel 4 samples
        int[] dataRecord = {1,  2,3,  4,5,6,  7,8,9,0};

        DataHeader dataConfig = new DataHeader(FormatVersion.BDF_24BIT);
        dataConfig.addSignal(1);
        dataConfig.addSignal(2);
        dataConfig.addSignal(3);
        dataConfig.addSignal(4);

        // remove signals 0 and 2

        // expected dataRecord
        int[] expectedDataRecord = {2,3,   7,8,9,0};

        SignalRemover recordFilter = new SignalRemover(new TestStream(expectedDataRecord));
        recordFilter.removeSignal(0);
        recordFilter.removeSignal(2);
        recordFilter.setHeader(dataConfig);

        recordFilter.writeDataRecord(dataRecord);
    }
}
