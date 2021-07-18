package com.biorecorder.data.frame_new.aggregation;

public interface Aggregation {
    void addInt(int value);
    void addDouble(double value);
    int getInt();
    double getDouble();
    int count();
    void reset();
}
