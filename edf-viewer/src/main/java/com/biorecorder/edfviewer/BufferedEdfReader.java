package com.biorecorder.edfviewer;

import com.biorecorder.multisignal.edflib.HeaderException;
import com.biorecorder.multisignal.edflib.HeaderRecord;
import com.biorecorder.multisignal.edflib.DataHeader;

import java.io.*;

public class BufferedEdfReader {
    private final DataHeader header;
    private final RandomAccessFile fileInputStream;
    private final File file;


    private EdfPosition edfPosition;
    private byte[] buffer;
    private int offset;
    private int samplesInBuffer;

    public BufferedEdfReader(File file, int bufferSizeInSamples) throws FileNotFoundException, HeaderException, IOException {
        this.file = file;
        HeaderRecord headerRecord = new HeaderRecord(file);
        fileInputStream = new RandomAccessFile(file, "r");
        header = headerRecord.getHeaderInfo();
        buffer = new byte[bufferSizeInSamples * header.getNumberOfBytesPerSample()];
        edfPosition = new EdfPosition(header);
    }

    private int loadDataToBuffer() throws IOException {
        long position = edfPosition.getBytePosition();
        if(fileInputStream.getFilePointer() != position) {
            fileInputStream.seek(position);
        }
        offset = 0;
        samplesInBuffer = fileInputStream.read(buffer) / header.getNumberOfBytesPerSample();
        return samplesInBuffer;
    }

    private void clearBuffer() {
        offset = buffer.length;
    }

    private boolean isBufferEmpty() {
        if(offset >= samplesInBuffer) {
            return true;
        }
        return false;
    }

    public int nextSample() throws IOException, EOFException {
        if(isBufferEmpty()) {
            if(loadDataToBuffer() <= 0) {
                throw new EOFException("End of file");
            }
        }
        int value = header.littleEndianBytesToInt(buffer, offset);
        offset += header.getNumberOfBytesPerSample();
        edfPosition.next();
        return value;
    }

    public int getNumberOfSignals() {
         return header.numberOfSignals();
    }

    public int getNumberOfSamplesInEachDataRecord(int signal) {
        return header.getNumberOfSamplesInEachDataRecord(signal);
    }


    public void skipCurrentSignalSamples() {
        offset += edfPosition.skipCurrentSignalSamples();
    }

    public void skipSamples(int n) {
        offset += n;
        edfPosition.skip(n);
    }

    public void setPosition(int record) {
        edfPosition.setPosition(record);
        clearBuffer();
    }

    public int getRecord(long timeMs) {
        return edfPosition.getRecord(timeMs);
    }

    public int getSampleInRecord(int signal, long timeMs) throws IllegalArgumentException  {
        return edfPosition.getSampleInRecord(signal, timeMs);
    }


    public void close() throws IOException {
        fileInputStream.close();
    }
}
