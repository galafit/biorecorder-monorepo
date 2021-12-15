package com.biorecorder.edflib.recordfilter;

import com.biorecorder.edflib.DataHeader;
import com.biorecorder.edflib.FormatVersion;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by galafit on 25/7/18.
 */
public class SignalFrequencyReducer extends FilterRecordStream {
    private Map<Integer, Integer> signalsToDividers = new HashMap<>();

    public SignalFrequencyReducer(com.biorecorder.edflib.DataRecordStream outStream) {
        super(outStream);
    }

    @Override
    public void setHeader(DataHeader header) {
        checkDividers(header);
        super.setHeader(header);
    }

    /**
     * @throws IllegalArgumentException if signal number of samples in DataRecord is
     * not a multiple of divider
     */
    private void checkDividers(DataHeader config) throws IllegalArgumentException {
        for (Integer signalNumber : signalsToDividers.keySet()) {
            int divider = signalsToDividers.get(signalNumber);
            if(config.getNumberOfSamplesInEachDataRecord(signalNumber) % divider != 0 ) {
                String errMsg = "Number of samples in DataRecord must be a multiple of divider. Number of samples = "
                        + config.getNumberOfSamplesInEachDataRecord(signalNumber)
                        + " Divider = " + divider;
                throw new IllegalArgumentException(errMsg);
            }
        }
    }

    public void addDivider(int signalNumber, int divider) throws IllegalArgumentException {
        signalsToDividers.put(signalNumber, divider);
    }


    @Override
    public DataHeader getOutConfig() {
        DataHeader outConfig = new DataHeader(inConfig);
        for (Integer signalNumber : signalsToDividers.keySet()) {
            int divider = signalsToDividers.get(signalNumber);
            int numberOfSamples = outConfig.getNumberOfSamplesInEachDataRecord(signalNumber) / divider;
            outConfig.setNumberOfSamplesInEachDataRecord(signalNumber, numberOfSamples);
        }
        return outConfig;
    }

    @Override
    public void writeDataRecord(int[] inputRecord)  {
        int signalStartInRecord = 0;
        int signalStartOutRecord = 0;
        for (int signalNumber = 0; signalNumber < inConfig.numberOfSignals(); signalNumber++) {
            int signalSamples = inConfig.getNumberOfSamplesInEachDataRecord(signalNumber);
            Integer divider = signalsToDividers.get(signalNumber);
            if(divider != null) {
                long sum = 0;
                int counter = 0;
                for (int i = 0; i < signalSamples; i++) {
                    sum += inputRecord[signalStartInRecord + i];
                    counter++;
                    if(counter == divider) {
                        outRecord[signalStartOutRecord + i/divider] = (int)(sum / divider);
                        sum = 0;
                        counter = 0;
                    }
                }
                signalStartOutRecord += signalSamples/divider;
            } else {
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

        // 0 channel 4 samples, 1 channel 2 samples, 2 channel 6 samples
        int[] dataRecord = {1,3,8,4,  2,4,  5,7,6,8,6,0};

        DataHeader dataConfig = new DataHeader(FormatVersion.BDF_24BIT);
        dataConfig.addSignal(4);
        dataConfig.addSignal(2);
        dataConfig.addSignal(6);


        // reduce signals frequencies by 4, 2, 2

        // expected dataRecord
        int[] expectedDataRecord = {4,  3,  6,7,3};

        SignalFrequencyReducer recordFilter = new SignalFrequencyReducer(new TestStream(expectedDataRecord));
        recordFilter.addDivider(0, 4);
        recordFilter.addDivider(1, 2);
        recordFilter.addDivider(2, 2);

        recordFilter.setHeader(dataConfig);

        recordFilter.writeDataRecord(dataRecord);
    }
}
