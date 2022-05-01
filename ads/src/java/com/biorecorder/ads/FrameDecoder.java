package com.biorecorder.ads;

import com.biorecorder.comport.ComportListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// TODO rename to InputDecoder ?
class FrameDecoder implements ComportListener {
    int startFrame = 0;
    int numberOfFrames = 0;

    private static final Log log = LogFactory.getLog(FrameDecoder.class);
    private static final byte START_FRAME_MARKER = (byte) 0xAA;
    private static final byte MESSAGE_MARKER = (byte) 0xA5;
    private static final byte COMMAND_MARKER = (byte) 0x5A;
    private static final byte STOP_FRAME_MARKER = (byte) 0x55;

    private static final byte MESSAGE_HARDWARE_CONFIG_MARKER = (byte) 0xA4;
    private static final byte MESSAGE_2CH_MARKER = (byte) 0x02;
    private static final byte MESSAGE_8CH_MARKER = (byte) 0x08;
    private static final byte MESSAGE_HELLO_MARKER = (byte) 0xA0;
    private static final byte MESSAGE_STOP_RECORDING_MARKER = (byte) 0xA5;

    private static int MAX_MESSAGE_SIZE = 8;
    private static int MAX_COMMAND_SIZE = 16;
    /*******************************************************************
     * these fields we need to restore  data records numbers
     *  from short (sent by ads in 2 bytes) to int
     *******************************************************************/
    private static int SHORT_MAX = 65535; // max value of unsigned short
    private int durationOfShortBlockMs;
    private int previousRecordShortNumber = -1;
    private long previousRecordTime;
    private int startRecordNumber;
    private int shortBlocksCount;
    /***************************************************************/
    private int frameIndex;
    private int frameSize;
    private int rowFrameSizeInByte;
    private int numberOf3ByteSamples;
    private int decodedFrameSizeInInt;
    private byte[] rawFrame = new byte[Math.max(MAX_COMMAND_SIZE, MAX_MESSAGE_SIZE)];;
    private int[] decodedFrame;
    private int[] accPrev = new int[3];

    boolean isAccelerometerEnabled;
    boolean isAccelerometerOneChannelMode;
    boolean isBatteryVoltageMeasureEnabled;
    boolean isLeadOffEnabled;
    int adsChannelsCount;

    private volatile NumberedDataRecordListener dataListener = new NullDataListener();
    private volatile MessageListener messageListener = new NullMessageListener();
    private volatile CommandListener commandListener = new NullCommandListener();

    void config(AdsConfig adsConfig) {
        previousRecordShortNumber = -1;
        previousRecordTime = 0;
        startRecordNumber = 0;
        shortBlocksCount = 0;
        frameIndex = 0;

        durationOfShortBlockMs = (int) (adsConfig.getDurationOfDataRecord() * 1000 * SHORT_MAX);
        numberOf3ByteSamples = getNumberOf3ByteSamples(adsConfig);
        rowFrameSizeInByte = getRawFrameSize(adsConfig);
        decodedFrameSizeInInt = getDecodedFrameSize(adsConfig);
        int maxSize = Math.max(MAX_COMMAND_SIZE, MAX_MESSAGE_SIZE);
        maxSize = Math.max(rowFrameSizeInByte, maxSize);
        rawFrame = new byte[maxSize];
        decodedFrame = new int[decodedFrameSizeInInt];
        isAccelerometerEnabled = adsConfig.isAccelerometerEnabled();
        isAccelerometerOneChannelMode = adsConfig.isAccelerometerOneChannelMode();
        isBatteryVoltageMeasureEnabled = adsConfig.isBatteryVoltageMeasureEnabled();
        isLeadOffEnabled = adsConfig.isLeadOffEnabled();
        adsChannelsCount = adsConfig.getAdsChannelsCount();
        log.info("frame size: " + rowFrameSizeInByte + " bytes");
    }

    public void finalize() {
        // at the moment nothing
    }


    /**
     * Frame decoder permits to add only ONE DataListener! So if a new listener added
     * the old one are automatically removed
     */
    public void addDataListener(NumberedDataRecordListener l) {
        dataListener = l;
    }

    /**
     * Frame decoder permits to add only ONE MessageListener! So if a new listener added
     * the old one are automatically removed
     */
    public void addMessageListener(MessageListener l) {
        messageListener = l;
    }

    /**
     * Frame decoder permits to add only ONE CommandListener! So if a new listener added
     * the old one are automatically removed
     */
    public void addCommandListener(CommandListener l) {
        if (l != null) {
            commandListener = l;
        }
    }

    @Override
    public void onByteReceived(byte inByte) {
       // System.out.println(frameIndex + " Byte rec " + byteToHexString(inByte));
        if (frameIndex == 0 && inByte == START_FRAME_MARKER) {
            rawFrame[frameIndex] = inByte;
            frameIndex++;
        } else if (frameIndex == 1 && inByte == START_FRAME_MARKER) {  //receiving data record
            rawFrame[frameIndex] = inByte;
            frameSize = rowFrameSizeInByte;
            frameIndex++;
        } else if (frameIndex == 1 && inByte == MESSAGE_MARKER) {  //receiving message
            rawFrame[frameIndex] = inByte;
            frameIndex++;
        } else if (frameIndex == 1 && inByte == COMMAND_MARKER) {  //receiving command
            rawFrame[frameIndex] = inByte;
            frameIndex++;
        }
        else if (frameIndex == 2) {
            rawFrame[frameIndex] = inByte;
            frameIndex++;
            if (rawFrame[1] == MESSAGE_MARKER) {   //message length
                // create new rowFrame with length = message length
                int msg_size = inByte & 0xFF;
                if (msg_size <= MAX_MESSAGE_SIZE) {
                    frameSize = msg_size;

                } else {
                    String infoMsg = "Invalid message frame. Too big frame size. Received byte = " + byteToHexString(inByte) + ",  max message size: "+ MAX_MESSAGE_SIZE + ". Frame index = " + (frameIndex - 1);
                    notifyMessageListeners(AdsMessage.FRAME_BROKEN, infoMsg);
                    frameIndex = 0;
                }
            } else if(rawFrame[1] == COMMAND_MARKER) { //command length
                // create new rowFrame with length = command length
                int command_size = inByte & 0xFF;
                if (command_size <= MAX_COMMAND_SIZE) {
                    frameSize = command_size;

                } else {
                    String infoMsg = "Invalid command frame. Too big frame size. Received byte = " + byteToHexString(inByte) + ",  max command size: "+ MAX_COMMAND_SIZE + ". Frame index = " + (frameIndex - 1);
                    notifyMessageListeners(AdsMessage.FRAME_BROKEN, infoMsg);
                    frameIndex = 0;
                    byte[] command = new byte[frameSize];
                    System.arraycopy(rawFrame, 0, command, 0, frameSize);
                    System.out.println("broken command "+ bytesToHex(command));
                }
            }
        } else if (frameIndex > 2 && frameIndex < (frameSize - 1)) {
            rawFrame[frameIndex] = inByte;
            frameIndex++;
        } else if (frameIndex == (frameSize - 1)) {
            rawFrame[frameIndex] = inByte;
            if (inByte == STOP_FRAME_MARKER) {
                onFrameReceived();
            } else {
                String infoMsg = "Invalid data frame. ";
                if(rawFrame[1] == MESSAGE_MARKER) {
                    infoMsg = "Invalid message frame. ";
                } else if(rawFrame[1] == COMMAND_MARKER) {
                    infoMsg = "Invalid command frame. ";
                }
                infoMsg = infoMsg + "No stop frame marker. Received byte = " + byteToHexString(inByte) + ". Frame index = " + frameIndex;
                notifyMessageListeners(AdsMessage.FRAME_BROKEN, infoMsg);
                byte[] command = new byte[frameSize];
                System.arraycopy(rawFrame, 0, command, 0, frameSize);
                System.out.println("broken command "+ bytesToHex(command));
            }
            frameIndex = 0;
        } else {
            String infoMsg = "Unrecognized byte received: " + byteToHexString(inByte);
            notifyMessageListeners(AdsMessage.FRAME_BROKEN, infoMsg);
            System.out.println("byte "+bytesToHex(inByte));
            frameIndex = 0;
        }
    }

    private void onFrameReceived() {
        if (rawFrame[1] == START_FRAME_MARKER) { // Frame = |START_FRAME_MARKER|START_FRAME_MARKER... => data
            onDataRecordReceived();
        } else if (rawFrame[1] == MESSAGE_MARKER) { // Frame = |START_FRAME_MARKER|MESSAGE_MARKER... => message
            onMessageReceived();
        } else if(rawFrame[1] == COMMAND_MARKER) { // Frame = |START_FRAME_MARKER|COMMAND_MARKER... => command
            onCommandReceived();
        }
    }

    private void onCommandReceived() {
        int command_size = rawFrame[2] & 0xFF;
        byte[] command = new byte[command_size];
        System.arraycopy(rawFrame, 0, command, 0, command_size);
        notifyCommandListeners(command);
    }

    private void onMessageReceived() {
        // hardwareConfigMessage: xAA|xA5|x06|xA4|x02|x55 =>
        // START_FRAME|MESSAGE_MARKER|number_of_bytes|HARDWARE_CONFIG|number_of_ads_channels|STOP_FRAME

        // stop recording message: \xAA\xA5\x05\xA5\x55
        // hello message: \xAA\xA5\x05\xA0\x55
        AdsMessage adsMessage = null;
        String info = "";
        if (rawFrame[3] == MESSAGE_HELLO_MARKER) {
            adsMessage = AdsMessage.HELLO;
            info = "Hello message received";
        } else if (rawFrame[3] == MESSAGE_STOP_RECORDING_MARKER) {
            adsMessage = AdsMessage.STOP_RECORDING;
            info = "Stop recording message received";
        }  else if (rawFrame[3] == MESSAGE_HARDWARE_CONFIG_MARKER && rawFrame[4] == MESSAGE_2CH_MARKER) {
            adsMessage = AdsMessage.ADS_2_CHANNELS;
            info = "Ads_2channel message received";
        } else if (rawFrame[3] == MESSAGE_HARDWARE_CONFIG_MARKER && rawFrame[4] == MESSAGE_8CH_MARKER) {
            adsMessage = AdsMessage.ADS_8_CHANNELS;
            info = "Ads_8channel message received";
        } else if (((rawFrame[3] & 0xFF) == 0xA3) && ((rawFrame[5] & 0xFF) == 0x01)) {
            adsMessage = AdsMessage.LOW_BATTERY;
            info = "Low battery message received";
        } else {
            info = "Unknown message received";
            adsMessage = AdsMessage.UNKNOWN;
        }
        notifyMessageListeners(adsMessage, info);
    }

    private void onDataRecordReceived() {
        int rawFrameOffset = 4;
        int decodedFrameOffset = 0;
        for (int i = 0; i < numberOf3ByteSamples; i++) {
            decodedFrame[decodedFrameOffset++] = bytesToSignedInt(rawFrame[rawFrameOffset], rawFrame[rawFrameOffset + 1], rawFrame[rawFrameOffset + 2]);
            rawFrameOffset += 3;
        }
        if (isAccelerometerEnabled) {
            int[] accVal = new int[3];
            int accSum = 0;
            for (int i = 0; i < 3; i++) {
//                decodedFrame[decodedFrameOffset++] = AdsUtils.littleEndianBytesToInt(rawFrame[rawFrameOffset], rawFrame[rawFrameOffset + 1]);
                accVal[i] = bytesToUnsignedInt(rawFrame[rawFrameOffset], rawFrame[rawFrameOffset + 1]);
                rawFrameOffset += 2;
            }
            if (isAccelerometerOneChannelMode) {
                for (int i = 0; i < accVal.length; i++) {
                    accSum += Math.abs(accVal[i] - accPrev[i]);
                    accPrev[i] = accVal[i];
                }
                decodedFrame[decodedFrameOffset++] = accSum;
            } else {
                for (int i = 0; i < accVal.length; i++) {
                    decodedFrame[decodedFrameOffset++] = accVal[i];
                }
            }
        }

        if (isBatteryVoltageMeasureEnabled) {
            decodedFrame[decodedFrameOffset++] = bytesToUnsignedInt(rawFrame[rawFrameOffset], rawFrame[rawFrameOffset + 1]);
            rawFrameOffset += 2;
        }

        if (isLeadOffEnabled) {
            if (adsChannelsCount == 8) {
                // 2 bytes for 8 channels
                decodedFrame[decodedFrameOffset++] = bytesToUnsignedInt(rawFrame[rawFrameOffset], rawFrame[rawFrameOffset + 1]);
                rawFrameOffset += 2;
            } else {
                // 1 byte for 2 channels
                decodedFrame[decodedFrameOffset++] = rawFrame[rawFrameOffset];
                rawFrameOffset += 1;
            }
        }

        int recordShortNumber = bytesToUnsignedInt(rawFrame[2], rawFrame[3]);
        int recordNumber =  recordShortNumberToInt(recordShortNumber);
        if(recordNumber >= startFrame && recordNumber < startFrame + numberOfFrames) {
            System.out.println(recordNumber + "  " +bytesToHex(rawFrame));
        }
        notifyDataListeners(decodedFrame, recordNumber);
    }

    private void notifyDataListeners(int[] dataRecord, int recordNumber) {
        dataListener.onDataRecordReceived(dataRecord, recordNumber);
    }

    private void notifyMessageListeners(AdsMessage adsMessage, String additionalInfo) {
        messageListener.onMessageReceived(adsMessage, additionalInfo);
    }

    private void notifyCommandListeners(byte[] command) {
        commandListener.onCommandReceived(command);
    }


    private int recordShortNumberToInt(int recordShortNumber) {
        long time = System.currentTimeMillis();

        if (previousRecordShortNumber == -1) {
            previousRecordShortNumber = recordShortNumber;
            previousRecordTime = time;
            startRecordNumber = recordShortNumber;
            return 0;
        }
        int recordsDistance = recordShortNumber - previousRecordShortNumber;
        if (recordsDistance <= 0) {
            shortBlocksCount++;
            recordsDistance += SHORT_MAX;
        }
        if (time - previousRecordTime > durationOfShortBlockMs / 2 ) {
            long blocks =  (time - previousRecordTime) / durationOfShortBlockMs;
            long timeRecordsDistance = (time - previousRecordTime) % durationOfShortBlockMs;
            // if recordsDistance big and timeRecordsDistance small
            if (recordsDistance > SHORT_MAX * 2 / 3 && timeRecordsDistance < durationOfShortBlockMs / 3) {
                blocks--;
            }
            // if recordsDistance small and timeRecordsDistance big
            if (recordsDistance < SHORT_MAX / 3 && timeRecordsDistance > durationOfShortBlockMs * 2 / 3) {
                blocks++;
            }

            shortBlocksCount += blocks;
        }

        previousRecordTime = time;
        previousRecordShortNumber = recordShortNumber;
        return shortBlocksCount * SHORT_MAX + recordShortNumber - startRecordNumber;
    }


    private int getRawFrameSize(AdsConfig adsConfig) {
        int result = 2;//маркер начала фрейма
        result += 2; // счечик фреймов
        result += 3 * getNumberOf3ByteSamples(adsConfig);
        if (adsConfig.isAccelerometerEnabled()) {
            result += 6;
        }
        if (adsConfig.isBatteryVoltageMeasureEnabled()) {
            result += 2;
        }
        if (adsConfig.isLeadOffEnabled()) {
            if (adsConfig.getAdsChannelsCount() == 8) {
                result += 2;
            } else {
                result += 1;
            }
        }
        result += 1;//footer
        return result;
    }

    private int getDecodedFrameSize(AdsConfig adsConfig) {
        int result = 0;
        result += getNumberOf3ByteSamples(adsConfig);
        if (adsConfig.isAccelerometerEnabled()) {
            result = result + (adsConfig.isAccelerometerOneChannelMode() ? 1 : 3);
        }
        if (adsConfig.isBatteryVoltageMeasureEnabled()) {
            result += 1;
        }
        if (adsConfig.isLeadOffEnabled()) {
            result += 1;
        }

        return result;
    }

    private int getNumberOf3ByteSamples(AdsConfig adsConfig) {
        int result = 0;

        Divider[] dividers = Divider.values();
        int maxDivider = dividers[dividers.length - 1].getValue();
        for (int i = 0; i < adsConfig.getAdsChannelsCount(); i++) {
            if (adsConfig.isAdsChannelEnabled(i)) {
                int divider = adsConfig.getAdsChannelDivider(i);
                result += (maxDivider / divider);
            }
        }
        return result;
    }


    /* Java int BIG_ENDIAN, Byte order: LITTLE_ENDIAN  */
    private static int bytesToUnsignedInt(byte... bytes) {
        switch (bytes.length) {
            case 1:
                return (bytes[0] & 0xFF);
            case 2:
                return (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF);
            case 3:
                return (bytes[2] & 0xFF) << 16 | (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF);
            case 4:
                return (bytes[3] & 0xFF) << 24 | (bytes[2] & 0xFF) << 16 | (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF);
            default:
                String errMsg = "Wrong «number of bytes» = " + bytes.length +
                        "! Available «number of bytes per int»: 4, 3, 2 or 1.";
                throw new IllegalArgumentException(errMsg);
        }
    }

    /* Java int BIG_ENDIAN, Byte order: LITTLE_ENDIAN  */
    private static int bytesToSignedInt(byte... b) {
        switch (b.length) {
            case 1:
                return b[0];
            case 2:
                return (b[1] << 8) | (b[0] & 0xFF);
            case 3:
                return (b[2] << 16) | (b[1] & 0xFF) << 8 | (b[0] & 0xFF);
            default:
                return (b[3] << 24) | (b[2] & 0xFF) << 16 | (b[1] & 0xFF) << 8 | (b[0] & 0xFF);
        }
    }


    private static String byteToHexString(byte b) {
        return String.format("%02X ", b);
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


    class NullMessageListener implements MessageListener {
        @Override
        public void onMessageReceived(AdsMessage messageType, String message) {
            // do nothing;
        }
    }

    class NullDataListener implements NumberedDataRecordListener {
        @Override
        public void onDataRecordReceived(int[] dataRecord, int dataRecordNumber) {
            // do nothing;
        }
    }

    class NullCommandListener implements CommandListener {
        @Override
        public void onCommandReceived(byte[] commandBytes) {
            // do nothing;
        }
    }

    public static void main (String[] args)
    {
        //byte[] bytes = {(byte)0xFF, (byte)0xFF, (byte)0xFF};
       // byte[] bytes = { (byte)0xE8,(byte) 0xFD, (byte)0x88};
        byte[] bytes = { (byte)0xAB,(byte) 0xAA, (byte)0xAB};
        System.out.println(bytesToUnsignedInt(bytes));
        System.out.println(bytesToSignedInt(bytes));
    }
}
