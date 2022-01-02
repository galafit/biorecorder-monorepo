package com.biorecorder.datalyb.datatable.aggregation;

import com.biorecorder.datalyb.datatable.BaseType;
import com.biorecorder.datalyb.datatable.RegularColumn;

public class First implements AggFunction {
    private String name = "FIRST";
    private int firstInt;
    private double firstDouble;
    private boolean isReset = true;

    @Override
    public void addInt(int value) {
        if(isReset) {
            firstInt = value;
            isReset = false;
        }
    }

    @Override
    public void addDouble(double value) {
        if(isReset) {
            firstDouble = value;
            isReset = false;
        }
    }
    @Override
    public String name() {
        return name;
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
    public void reset() {
        isReset = true;
    }

    @Override
    public BaseType outType(BaseType inType) {
        return inType;
    }

    @Override
    public double getAggregatedRegularColumnStart(RegularColumn columnToAgg, int pointsInGroup) {
        return columnToAgg.startValue();
    }
}
