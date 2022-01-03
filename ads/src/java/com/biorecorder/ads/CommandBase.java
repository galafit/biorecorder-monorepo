package com.biorecorder.ads;

public class CommandBase implements Command {
    private byte[] commandBytes;

    public CommandBase(byte... commandBytes) {
        this.commandBytes = commandBytes;
    }

    @Override
    public byte[] getCommandBytes() {
        return commandBytes;
    }
}
