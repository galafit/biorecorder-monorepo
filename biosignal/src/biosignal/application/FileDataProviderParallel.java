package biosignal.application;

import com.biorecorder.edflib.DataHeader;
import com.biorecorder.edflib.EdfReader;
import com.biorecorder.edflib.HeaderRecord;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

public class FileDataProviderParallel implements DataProvider{
    private File edfFile;
    private List<DataListener>[] dataListeners;
    private List<ProviderConfigListener> providerConfigListeners = new ArrayList<>(1);
    private EdfReader edfReader;
    private long readStartMs; // Время начала чтения в мСек. Отсчитывается от старта записи
    private long readEndMs; // Время конца чтения в мСек. Отсчитывается от старта записи
    private DataHeader header;
    private volatile boolean isStopped;
    private final ExecutorService singleThreadExecutor;
    private volatile Future executorFuture;

    public FileDataProviderParallel(File edfFile) {
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
        dataListeners = new List[header.numberOfSignals()];
        for (int i = 0; i < dataListeners.length; i++) {
            dataListeners[i] = new ArrayList<>();
        }
        readStartMs = 0;
        readEndMs = header.getDurationOfDataRecordMs() *
                header.getNumberOfDataRecords();
       // readEndMs = 10000; //header.getDurationOfDataRecordMs() * 100;


        ThreadFactory namedThreadFactory = new ThreadFactory() {
            public Thread newThread(Runnable r) {
                return new Thread(r, "«EDF file provider» thread");
            }
        };
        singleThreadExecutor = Executors.newSingleThreadExecutor(namedThreadFactory);
    }


    public void start() {
        isStopped = false;
        notifyConfigListeners();
        executorFuture = singleThreadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                read();
            }
        });
    }


    public void stop() {
        isStopped = true;
        if(executorFuture != null) {
            executorFuture.cancel(true);
        }
    }

    @Override
    public void finish() {
        stop();
        try {
            edfReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void notifyConfigListeners() {
        ProviderConfig config = new ProviderConfig() {
            @Override
            public int signalsCount() {
                return header.numberOfSignals();
            }

            @Override
            public double signalSampleRate(int signal) {
                return header.getSampleFrequency(signal);
            }

            @Override
            public long getRecordingStartTimeMs() {
                return header.getRecordingStartTimeMs() + readStartMs;
            }

            @Override
            public long getRecordingTimeMs() {
                return readEndMs - readStartMs;
            }
        };
        for (ProviderConfigListener providerConfigListener : providerConfigListeners) {
            providerConfigListener.receiveConfig(config);
        }
    }

    @Override
    public void addDataListener(int signal, DataListener dataListener) {
        if (signal < dataListeners.length) {
            List<DataListener> signalListeners = dataListeners[signal];
            signalListeners.add(dataListener);
        }
    }

    @Override
    public void addConfigListener(ProviderConfigListener l) {
        providerConfigListeners.add(l);
    }

    public void setFullReadInterval(){
        readStartMs = 0;
        readEndMs = header.getDurationOfDataRecordMs() *
                header.getNumberOfDataRecords();
    }

    // Позиция - номер данного в множестве
    // переводит позицию во время (mSec) измерения сампла.
    // Время отсчитывается от начала записи
    private long positionToTimeMs(int signal, long pos) {
        long time = (long) (pos * header.getSampleFrequency(signal) / 1000);
        return time;
    }

    private long timeMsToPosition(int signal, long timeFromStartMs) {
        long pos = (long) (timeFromStartMs * header.getSampleFrequency(signal) / 1000);
        return pos;
    }

    private void read() {
        int readPortion = 3000; //samples
        for (int i = 0; i < dataListeners.length; i++) {
            boolean endFile = false;
            List<DataListener> signalListeners = dataListeners[i];
            if(signalListeners.size() > 0) {
                long startPos = timeMsToPosition(i, readStartMs);
                long endPos = timeMsToPosition(i, readEndMs);
                int n = (int) (endPos - startPos);
                int samplesToRead = Math.min(n, readPortion);
                int totalReadSamples = 0;
                int[] data = new int[samplesToRead];
                edfReader.setSamplePosition(i, startPos);
                while (!isStopped && !endFile && totalReadSamples < n) {
                    try {
                        int readSamples = edfReader.readSamples(i, samplesToRead, data);
                        if (readSamples < samplesToRead) {
                            int[] data1 = new int[readSamples];
                            System.arraycopy(data, 0, data1, 0, readSamples);
                            data = data1;
                            endFile = true;
                        }
                        totalReadSamples += readSamples;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    for (int j = 0; j < signalListeners.size(); j++) {
                        DataListener l = signalListeners.get(j);
                        l.receiveData(data, 0, data.length);
                    }
                   /* try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                }
            }
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
        this.readStartMs = readStartMs;
        this.readEndMs = readStartMs + readIntervalMs;
    }

    public String copyReadIntervalToFile() {
        //finish();
        String filename = edfFile.getName();
        String dir = edfFile.getParent();
        long recordingStartMs = header.getRecordingStartTimeMs();
        Date start = new Date(recordingStartMs + readStartMs);
        Date end = new Date(recordingStartMs + readEndMs);
        String newFilename = filename.split("\\.")[0] + "_"+
                start.getHours() + "%" + start.getMinutes() + "%" + start.getSeconds() + "-" +
                end.getHours() + "%" + end.getMinutes() + "%" + end.getSeconds() + ".bdf";
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
