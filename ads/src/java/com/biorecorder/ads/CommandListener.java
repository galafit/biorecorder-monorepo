package com.biorecorder.ads;

public interface CommandListener {
    void onCommandReceived(byte[] commandBytes);
}
