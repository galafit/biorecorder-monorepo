package com.biorecorder.edfviewer;

import java.util.ArrayList;
import java.util.List;

class SignalHub implements DataListener {
    private final int signal;
    private List<DataListener> filters;

    public SignalHub(int signal) {
        this.signal = signal;
        filters = new ArrayList<>();
    }

    public void addFilter(DataListener filter) {
        filters.add(filter);
    }

    public int getSignal() {
        return signal;
    }

    @Override
    public void onStart() {
        for (int i = 0; i < filters.size(); i++) {
           filters.get(i).onStart();
        }
    }

    @Override
    public void onDataReceived(double data) {
        for (int i = 0; i < filters.size(); i++) {
            filters.get(i).onDataReceived(data);
        }
    }

    @Override
    public void onFinish() {
        for (int i = 0; i < filters.size(); i++) {
            filters.get(i).onFinish();
        }
    }

   /* @Override
    public int getFilterLength() {
        int maxLength = 0;
        for (int i = 0; i < filters.size(); i++) {
            maxLength = Math.max(maxLength, filters.get(i).getFilterLength());
        }
        return maxLength;
    }*/
}
