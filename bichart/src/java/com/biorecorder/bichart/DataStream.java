package com.biorecorder.bichart;

public interface DataStream {
    int channelsCount();
    void setFullStreamRange();
    void setStreamXRange(double xValueFrom, double xValueTill, int cropShoulder);
    void setStreamXRange(int channel, int from, int length);
    int getDataSize(int channel);
    double[] getDataXRange(int channel);
    void stream();

    void addDataListener();

}
