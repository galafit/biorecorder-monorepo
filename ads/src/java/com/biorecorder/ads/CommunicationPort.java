package com.biorecorder.ads;

import com.biorecorder.comport.Comport;
import com.biorecorder.comport.ComportFactory;
import com.biorecorder.comport.ComportRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;

public class CommunicationPort {
    private static final Log log = LogFactory.getLog(CommunicationPort.class);
    private static final int COMPORT_SPEED = 460800;

    private Comport comport;
    FrameDecoder frameDecoder;
    private byte[] commandSend;
    private volatile boolean command_confirmed;
    private int wait_time_ms = 1000;

    public CommunicationPort(String comportName) throws ConnectionRuntimeException {
        try {
            comport = ComportFactory.getComport(comportName, COMPORT_SPEED);
        } catch (ComportRuntimeException ex) {
            throw new ConnectionRuntimeException(ex);
        }
        frameDecoder = new FrameDecoder();
        frameDecoder.addCommandListener(new CommandListener() {
            @Override
            public void onCommandReceived(byte[] commandBytes) {
                command_confirmed = Arrays.equals(commandBytes, commandSend) ? true : false;
                log.info("command received: " + bytesToHex(commandBytes) + "," +
                        "  isConfirmed: " + command_confirmed);
            }
        });
        comport.addListener(frameDecoder);
    }

    public boolean isOpened() {
        return comport.isOpened();
    }

    public boolean close() {
        frameDecoder.finalize();
        return comport.close();
    }

    public String getComportName() {
        return comport.getComportName();
    }

    public void config(AdsConfig adsConfig) {
        frameDecoder.config(adsConfig);
    }

    public boolean sendCommand(byte[] command) {
        if(AdsCommands.commandNeedConfirm(command)) {
            command_confirmed = false;
            if(comport.writeBytes(command)) {
                commandSend = command;
                long time = System.currentTimeMillis();
                log.info("command sent: " + bytesToHex(command));
                // ждем что команда вернется в течении времени wait_time_ms
                while (!command_confirmed && (System.currentTimeMillis() - time) < wait_time_ms);
                if(command_confirmed) { // если отправленная команда "вернулась" и совпала с той что была отправлена
                    if(comport.writeBytes(AdsCommands.confirmedCommand())) { ; // посылаем подтверждение
                        log.info("command sent: " + bytesToHex(AdsCommands.confirmedCommand()));
                        return true;
                    }
                }
            }
        } else {
            if(comport.writeBytes(command)) {
                log.info("command sent: " + bytesToHex(command));
                return true;
            }
        }
        return false;
    }

    /**
     * permits to add only ONE DataListener! So if a new listener added
     * the old one are automatically removed
     */
    public void addDataListener(NumberedDataRecordListener l) {
        frameDecoder.addDataListener(l);
    }

    /**
     * permits to add only ONE MessageListener! So if a new listener added
     * the old one are automatically removed
     */
    public void addMessageListener(MessageListener l) {
        frameDecoder.addMessageListener(l);
    }

    public static String bytesToHex(byte... bytes) {
        char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 3];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = HEX_ARRAY[v >>> 4];
            hexChars[j * 3 + 1] = HEX_ARRAY[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';

        }
        return new String(hexChars);
    }

}
