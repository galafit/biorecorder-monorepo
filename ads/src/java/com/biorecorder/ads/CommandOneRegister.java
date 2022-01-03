package com.biorecorder.ads;

public class CommandOneRegister implements Command {
    private static final byte OPEN_BYTE = 0x05;
    private static final byte CLOSE_BYTE = (byte) (0xFF & 0xFF);

    private byte[] commandBytes;

    public CommandOneRegister(byte register, byte command) {
        commandBytes = new byte[]{OPEN_BYTE, register, command, CLOSE_BYTE};
    }

    @Override
    public byte[] getCommandBytes() {
        return commandBytes;
    }
}
