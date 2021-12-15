package com.biorecorder.bdfrecorder;

import com.biorecorder.edflib.DataHeader;
import com.biorecorder.edflib.DataRecordStream;
import com.biorecorder.edflib.EdfFileWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileNotFoundException;

public class FileStream implements DataRecordStream {
    private static final Log log = LogFactory.getLog(FileStream.class);
    private EdfFileWriter edfFileWriter;

    public FileStream(File file) throws FileNotFoundException {
        edfFileWriter = new EdfFileWriter(file);
    }

    public File getFile() {
        return edfFileWriter.getFile();
    }

    public int getNumberOfWrittenDataRecords() {
        return edfFileWriter.getNumberOfReceivedDataRecords();
    }

    @Override
    public void setHeader(DataHeader header) {
        edfFileWriter.setHeader(header);
    }

    @Override
    public void writeDataRecord(int[] dataRecord) {
        edfFileWriter.writeDataRecord(dataRecord);
    }

    @Override
    public void close() {
        try {
            edfFileWriter.close();
            if (edfFileWriter.getNumberOfReceivedDataRecords() == 0) {
                edfFileWriter.getFile().delete();
            }
            //msg = new Message(Message.TYPE_DATA_SUCCESSFULLY_SAVED, edfFile + "\n\n" + edfStream1.getWritingInfo());
            String logMsg = new Message(Message.TYPE_DATA_SUCCESSFULLY_SAVED, edfFileWriter.getFile() + "\n\n" + edfFileWriter.getWritingInfo()).getMessage();
            log.info(logMsg);
        } catch (Exception e) {
            log.error(e);
        }

    }
}