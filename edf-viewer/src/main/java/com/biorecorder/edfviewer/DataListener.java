package com.biorecorder.edfviewer;

public interface DataListener {
    public void onDataReceived(int data);
    public void onFinish();
    public void onStart();
   // public int getFilterLength();
}
