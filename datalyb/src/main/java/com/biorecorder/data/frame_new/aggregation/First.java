package com.biorecorder.data.frame_new.aggregation;

public class First implements Aggregation {
    int firstInt;
    double firstDouble;
    int count = 0;

    @Override
    public void addInt(int value) {
        if(count == 0) {
            firstInt = value;
        }
        count++;

    }

    @Override
    public void addDouble(double value) {
        if(count == 0) {
            firstDouble = value;
        }
        count++;
    }
    @Override
    public String name() {
        return "FIRST";
    }
    @Override
    public int getInt() {
        return firstInt;
    }

    @Override
    public double getDouble() {
        return firstDouble;
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
