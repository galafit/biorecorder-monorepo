package biosignal.application;

import com.biorecorder.edflib.DataHeader;
import com.biorecorder.edflib.EdfReader;
import com.biorecorder.edflib.HeaderRecord;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EdfProvider {
    private File edfFile;
    private int numberSignals;
    private List<DataListener>[] dataListeners;
    private EdfReader edfReader;
    private long recordingStarTimeMs;
    private long readStartMs; // Время начала чтения в мСек. Отсчитывается от старта записи
    private long readEndMs; // Время конца чтения в мСек. Отсчитывается от старта записи
    private DataHeader header;

    public EdfProvider(File edfFile) {
        this.edfFile = edfFile;
        try {
            edfReader = new EdfReader(edfFile);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        header = edfReader.getHeader();
        // Print some header info from original file
        System.out.println("---------------< edfHeader >----------------");
        System.out.println(header);
        System.out.println("-------------< End edfHeader >--------------");
        numberSignals = header.numberOfSignals();
        dataListeners = new List[numberSignals];
        for (int i = 0; i < dataListeners.length; i++) {
            dataListeners[i] = new ArrayList<>();
        }
        //recordingStarTimeMs = header.getRecordingStartTimeMs();
        // setFullReadInterval
        readStartMs = recordingStarTimeMs;
        readEndMs = 5000;//header.getDurationOfDataRecordMs() *
                //header.getNumberOfDataRecords();
    }

     public void setFullReadInterval(){
        readStartMs = recordingStarTimeMs;
        readEndMs = header.getDurationOfDataRecordMs() *
                    header.getNumberOfDataRecords();
     }

    // Позиция - номер данного в множестве
    // переводит позицию во время (mSec) измерения сампла.
    // Время отсчитывается от начала записи
    private long positionToTimeMs(int signal, long pos) {
        long time = (long) (pos * signalSampleRate(signal) / 1000);
        return time;
    }

    private long timeMsToPosition(int signal, long timeFromStartMs) {
        long pos = (long) (timeFromStartMs * signalSampleRate(signal) / 1000);
        return pos;
    }

    public void addListener(int signal, DataListener dataListener) {
        if (signal < numberSignals) {
            List<DataListener> signalListeners = dataListeners[signal];
            signalListeners.add(dataListener);
        }
    }

    public void read() {
        for (int i = 0; i < dataListeners.length; i++) {
            long startPos = timeMsToPosition(i, readStartMs);
            long endPos = timeMsToPosition(i, readEndMs);
            int n = (int) (endPos - startPos);
            int[] data = new int[n];
            try {
                edfReader.setSamplePosition(i, startPos);
                edfReader.readSamples(i, n, data);
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<DataListener> signalListeners = dataListeners[i];
            for (int j = 0; j < signalListeners.size(); j++) {
                DataListener l = signalListeners.get(j);
                l.receiveData(data);
            }
        }
    }

    public void finish() {
        try {
            edfReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setReadInterval(int signal, long startPos, long samplesToRead) {
        readStartMs = positionToTimeMs(signal, startPos);
        long endPos = startPos + samplesToRead;
        readEndMs = positionToTimeMs(signal, endPos);
    }

    /**
     * Время не абсолютное а отсчитываемое от старта записи!!!
     * @param readStartMs - время от старта записи
     * @param readIntervalMs
     */
    public void setReadTimeInterval(long readStartMs, long readIntervalMs) {
        this.readStartMs = readStartMs - recordingStarTimeMs;
        this.readEndMs = readStartMs + readIntervalMs;
    }

    public int signalsCount() {
        return numberSignals;
    }

    public double signalSampleRate(int signal) {
        double sampleRate = (1000.0 * header.getNumberOfSamplesInEachDataRecord(signal)) /
                header.getDurationOfDataRecordMs();
        return sampleRate;
    }

    public long getRecordingStartTimeMs() {
        return recordingStarTimeMs;
    }

    public String copyReadIntervalToFile() {
        //finish();
        String filename = edfFile.getName();
        String dir = edfFile.getParent();
        long recordingStartMs = header.getRecordingStartTimeMs();
        Date start = new Date(recordingStartMs + readStartMs);
        Date end = new Date(recordingStartMs + readEndMs);
        String newFilename = filename.split("\\.")[0] + "_"+
                start.getHours() + ":" + start.getMinutes() + ":" + start.getSeconds() + "-" +
                end.getHours() + ":" + end.getMinutes() + ":" + end.getSeconds() + ".bdf";
        File newFile = new File(dir, newFilename);
        try {
            FileOutputStream out = new FileOutputStream(newFile);
            FileInputStream in = new FileInputStream(edfFile);
            int recordDurationMs = header.getDurationOfDataRecordMs();
            int recordSizeInByte  =  header.getRecordSize() * header.getFormatVersion().getNumberOfBytesPerSample();
            int startRecord = (int)(readStartMs / recordDurationMs);
            int endRecord = (int)(readEndMs / recordDurationMs) + 1;
            if(endRecord >= header.getNumberOfDataRecords()) {
                endRecord = header.getNumberOfDataRecords();
            }
            int recordsToRead = (endRecord - startRecord);
            int bytesToRead = recordsToRead * recordSizeInByte;
            int bytesInHeaderRecord = header.getNumberOfBytesInHeaderRecord();
            long readStartPos = bytesInHeaderRecord + startRecord * recordSizeInByte;
            long newStartTime = recordingStartMs + startRecord * recordDurationMs;
            header.setRecordingStartTimeMs(newStartTime);
            header.setNumberOfDataRecords(recordsToRead);
            out.write(new HeaderRecord(header).getBytes());
            FileChannel ic = in.getChannel();
            FileChannel oc = out.getChannel();
            ic.position(readStartPos);
            oc.transferFrom(ic,bytesInHeaderRecord,bytesToRead);
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newFile.toString();
    }
}
