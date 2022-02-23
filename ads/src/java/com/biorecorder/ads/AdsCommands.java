package com.biorecorder.ads;


public class AdsCommands {
    private static final byte FRAME_START = (byte) (0xAA & 0xFF);
    private static final byte FRAME_STOP = (byte) 0x55;
    private static final byte COMMAND_START = (byte) 0x5A;
    private static final byte COMMAND_NEED_CONFIRM = (byte) 0xCC;

    /****** COMMANDS MARKERS *************/
    private static final byte PROCESSOR_REGISTER_WRITE = (byte) 0xA1;
    private static final byte PROCESSOR_REGISTER_SET_BITS = (byte) 0xA2;
    private static final byte PROCESSOR_REGISTER_CLEAR_BITS = (byte) 0xA3;
    private static final byte PROCESSOR_REGISTER_READ = (byte) 0xA4;
    private static final byte ADS_REGISTER_WRITE = (byte) 0xA6;
    private static final byte ADS_REGISTER_READ = (byte) 0xA7;
    private static final byte ADS_START_RECORDING = (byte) 0xA8;
    private static final byte ADS_STOP_RECORDING = (byte) 0xA9;
    private static final byte HELLO_REQUEST = (byte) 0xAB;
    private static final byte HARDWARE_REQUEST = (byte) 0xAC;
    private static final byte PING = (byte) 0xAD;
    private static final byte CONFIRMED = (byte) 0xAE;

    /**
     * Processor register address es 16bit or 2 bytes.
     * The order of address bytes is LOW_ENDIAN
     */
    public static byte[] writeProcessorRegisterCommand(byte regAddressBottom, byte regAddressTop, byte regValue) {
        return new byte[] {FRAME_START, COMMAND_START, 0x09, PROCESSOR_REGISTER_WRITE, regAddressBottom, regAddressTop, regValue, COMMAND_NEED_CONFIRM, FRAME_STOP};
    }

    public static byte[] setProcessorRegisterBitsCommand(byte regAddressBottom, byte regAddressTop, byte bitsToSet) {
        return new byte[] {FRAME_START, COMMAND_START, 0x09, PROCESSOR_REGISTER_SET_BITS, regAddressBottom, regAddressTop, bitsToSet, COMMAND_NEED_CONFIRM, FRAME_STOP};
    }

    public static byte[] clearProcessorRegisterBitsCommand(byte regAddressBottom, byte regAddressTop, byte bitsToClear) {
        return new byte[] {FRAME_START, COMMAND_START, 0x09, PROCESSOR_REGISTER_CLEAR_BITS, regAddressBottom, regAddressTop, bitsToClear, COMMAND_NEED_CONFIRM, FRAME_STOP};
    }

    public static byte[] readProcessorRegisterCommand(byte regAddressBottom, byte regAddressTop, boolean needConfirm) {
        byte confirmByte = needConfirm ? COMMAND_NEED_CONFIRM : FRAME_STOP;
        return new byte[] {FRAME_START, COMMAND_START, 0x08, PROCESSOR_REGISTER_READ, regAddressBottom, regAddressTop, confirmByte, FRAME_STOP};
    }

    public static byte[] writeAdsRegisterCommand(byte regAddress, byte regValue) {
        return new byte[] {FRAME_START, COMMAND_START, 0x08, ADS_REGISTER_WRITE, regAddress, regValue, COMMAND_NEED_CONFIRM, FRAME_STOP};
    }

    public static byte[] readAdsRegisterCommand(byte regAddress, boolean needConfirm) {
        byte confirmByte = needConfirm ? COMMAND_NEED_CONFIRM : FRAME_STOP;
        return new byte[] {FRAME_START, COMMAND_START, 0x07, ADS_REGISTER_READ, regAddress,  confirmByte, FRAME_STOP};
    }

    public static byte[] helloRequestCommand() {
        return new byte[] {FRAME_START, COMMAND_START, 0x06, HELLO_REQUEST, FRAME_STOP, FRAME_STOP};
    }

    public static byte[] hardwareRequestCommand(boolean needConfirm) {
        byte confirmByte = needConfirm ? COMMAND_NEED_CONFIRM : FRAME_STOP;
        return new byte[] {FRAME_START, COMMAND_START, 0x06, HARDWARE_REQUEST, confirmByte, FRAME_STOP};
    }

    public static byte[] confirmedCommand() {
        return new byte[] {FRAME_START, COMMAND_START, 0x06, CONFIRMED, FRAME_STOP, FRAME_STOP};
    }

    public static byte[] startRecordingCommand(byte... dividers) throws IllegalArgumentException {
        if(dividers.length != 2 && dividers.length != 8) {
            String msg = "Number of dividers can be only 2 or 8. Number of dividers = " + dividers.length;
            throw new IllegalArgumentException(msg);
        }
        byte commandLength = (byte)(6 + dividers.length);
        byte[] command = new byte[commandLength];
        command[0] = FRAME_START;
        command[1] = COMMAND_START;
        command[2] = commandLength;
        command[3] = ADS_START_RECORDING;
        for (int i = 0; i < dividers.length; i++) {
            command[4 + i] = dividers[i];
        }
        command[commandLength - 2] = COMMAND_NEED_CONFIRM;
        command[commandLength - 1] = FRAME_STOP;
        return command;
    }

    public static byte[] stopRecordingCommand() {
        return new byte[] {FRAME_START, COMMAND_START, 0x06, ADS_STOP_RECORDING, COMMAND_NEED_CONFIRM, FRAME_STOP};
    }

    public static byte[] pingCommand() {
        return new byte[] {FRAME_START, COMMAND_START, 0x06, PING, FRAME_STOP, FRAME_STOP};
    }

    public static boolean commandNeedConfirm(byte[] command) {
        if(command[command.length - 2] == COMMAND_NEED_CONFIRM) {
            return true;
        }
        return false;
    }
}
