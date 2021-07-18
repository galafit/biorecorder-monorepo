package com.biorecorder.data.frame_new.aggregation;

public class Min implements Aggregation {
    int minInt;
    double minDouble;
    int count = 0;

    @Override
    public void addInt(int value) {
        if(count == 0) {
            minInt = value;
        } else {
            minInt = Math.min(minInt, value);
        }
        count++;

    }

    @Override
    public void addDouble(double value) {
        if(count == 0) {
            minDouble = value;
        } else {
            minDouble = Math.min(minDouble, value);
        }
        count++;
    }

    @Override
    public int getInt() {
        return minInt;
    }

    @Override
    public double getDouble() {
        return minDouble;
    }

    @Override
    public int count() {
        return count;
    }

    @Override
    public void reset() {
        count = 0;
    }
}
