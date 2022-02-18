package com.biorecorder.ads;

public class CommandBase implements Command {
    private byte[] commandBytes;

    public CommandBase(byte... commandBytes1) {
        commandBytes = new byte[commandBytes1.length];
        System.arraycopy(commandBytes1, 0, commandBytes, 0, commandBytes1.length);
    }

    @Override
    public byte[] getCommandBytes() {
        return commandBytes;
    }
}
