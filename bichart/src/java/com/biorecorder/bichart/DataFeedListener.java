package com.biorecorder.bichart;

public interface DataFeedListener {
    void onStartFeeding(int numberOfDataPoints);
    void onStopFeeding();
    void onDataSeriesReceived(XYSeries data);
}
