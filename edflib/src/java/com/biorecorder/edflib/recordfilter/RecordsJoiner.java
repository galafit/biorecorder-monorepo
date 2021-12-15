package com.biorecorder.edflib.recordfilter;

import com.biorecorder.edflib.DataHeader;
import com.biorecorder.edflib.FormatVersion;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Permits to join (piece together) given number of incoming DataRecords.
 * out  data records (that will be send to the listener)
 * have the following structure:
 * <br>  number of samples from channel_0 in original DataRecord * numberOfRecordsToJoin ,
 * <br>  number of samples from channel_1 in original DataRecord * numberOfRecordsToJoin,
 * <br>  ...
 * <br>  number of samples from channel_i in original DataRecord * numberOfRecordsToJoin
 * <p>
 *
 * <br>duration of resulting DataRecord = duration of original DataRecord * numberOfRecordsToJoin
 */
public class RecordsJoiner extends FilterRecordStream {
    private int numberOfRecordsToJoin;
    private int joinedRecordsCounter;
    // делаем аналог двойной буферизации. Строим outRecord а когода он готов
    //  копируем его в массив для отправки слушателям
    //  (чтобы слушатели могли делать обработку паралелльно)
    private int[] recordToSend;
    private ExecutorService singleThreadExecutor;

    public RecordsJoiner(com.biorecorder.edflib.DataRecordStream outStream, int numberOfRecordsToJoin) {
        super(outStream);
        this.numberOfRecordsToJoin = numberOfRecordsToJoin;
    }

    @Override
    public void setHeader(DataHeader header) {
        super.setHeader(header);
        recordToSend = new int[outRecord.length];
        ThreadFactory namedThreadFactory = new ThreadFactory() {
            public Thread newThread(Runnable r) {
                return new Thread(r, "Records joiner thread");
            }
        };
        singleThreadExecutor = Executors.newSingleThreadExecutor(namedThreadFactory);
    }

    @Override
    protected DataHeader getOutConfig() {
        DataHeader outConfig = new DataHeader(inConfig);
        outConfig.setDurationOfDataRecord(inConfig.getDurationOfDataRecordSec() * numberOfRecordsToJoin);
        for (int i = 0; i < outConfig.numberOfSignals(); i++) {
            outConfig.setNumberOfSamplesInEachDataRecord(i, inConfig.getNumberOfSamplesInEachDataRecord(i) * numberOfRecordsToJoin);
        }
        return outConfig;
    }

    /**
     * Accumulate and join the specified number of incoming samples into one out
     * DataRecord and when it is ready send it to the dataListener
     */
    @Override
    public void writeDataRecord(int[] inputRecord)  {
        int signalStartInRecord = 0;
        for (int signalNumber = 0; signalNumber < inConfig.numberOfSignals(); signalNumber++) {
            int signalSamples = inConfig.getNumberOfSamplesInEachDataRecord(signalNumber);
            int signalStartOutRecord = signalStartInRecord * numberOfRecordsToJoin + signalSamples*joinedRecordsCounter;
            System.arraycopy(inputRecord, signalStartInRecord, outRecord, signalStartOutRecord, signalSamples);
            signalStartInRecord += signalSamples;
        }
        joinedRecordsCounter++;

        if(joinedRecordsCounter == numberOfRecordsToJoin) {
            joinedRecordsCounter = 0;
            System.arraycopy(outRecord, 0, recordToSend, 0, outRecord.length);
            singleThreadExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    sendData(recordToSend);
                }
            });

        }
    }

    /**
     * Unit Test. Usage Example.
     */
    public static void main(String[] args) {

        // 0 channel 3 samples, 1 channel 2 samples, 3 channel 4 samples
        int[] dataRecord = {1,3,8,  2,4,  7,6,8,6};

        DataHeader dataConfig = new DataHeader(FormatVersion.BDF_24BIT);
        dataConfig.addSignal(3);
        dataConfig.addSignal(2);
        dataConfig.addSignal(4);

        // join 2 records
        int numberOfRecordsToJoin = 3;
        // expected dataRecord
        int[] expectedDataRecord = {1,3,8,1,3,8,1,3,8,  2,4,2,4,2,4,  7,6,8,6,7,6,8,6,7,6,8,6};

        RecordsJoiner recordFilter = new RecordsJoiner(new TestStream(expectedDataRecord), numberOfRecordsToJoin);

        recordFilter.setHeader(dataConfig);

        // send 4 records and get as result 2 joined records
        recordFilter.writeDataRecord(dataRecord);
        recordFilter.writeDataRecord(dataRecord);
        recordFilter.writeDataRecord(dataRecord);
        recordFilter.writeDataRecord(dataRecord);
    }
}
