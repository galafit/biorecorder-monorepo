package com.biorecorder.data.frame_new.aggregation;

public class Max implements Aggregation {
    int maxInt;
    double maxDouble;
    int count = 0;

    @Override
    public void addInt(int value) {
        if(count == 0) {
            maxInt = value;
        } else {
            maxInt = Math.max(maxInt, value);
        }
        count++;

    }

    @Override
    public void addDouble(double value) {
        if(count == 0) {
            maxDouble = value;
        } else {
            maxDouble = Math.max(maxDouble, value);
        }
        count++;
    }


    @Override
    public int getInt() {
        return maxInt;
    }

    @Override
    public double getDouble() {
        return maxDouble;
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
