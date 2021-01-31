package com.biorecorder.edfviewer;

public interface DataListener {
    public void onDataReceived(double data);
    public void onFinish();
    public void onStart();
   // public int getFilterLength();
}
