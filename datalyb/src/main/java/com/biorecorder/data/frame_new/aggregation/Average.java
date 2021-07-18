package com.biorecorder.data.frame_new.aggregation;

public class Average implements Aggregation {
    long sumInt;
    double sumDouble;
    int count = 0;

    @Override
    public void addInt(int value) {
        if(count == 0) {
            sumInt = value;
        } else {
            sumInt = sumInt + value;
        }
        count++;

    }

    @Override
    public void addDouble(double value) {
        if(count == 0) {
            sumDouble = value;
        } else {
            sumDouble = sumDouble + value;
        }
        count++;
    }
    @Override
    public String name() {
        return "AVG";
    }
    @Override
    public int getInt() {
        return (int) sumInt/count;
    }

    @Override
    public double getDouble() {
        return sumDouble/count;
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
