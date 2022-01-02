package com.biorecorder.datalyb.datatable.aggregation;

import com.biorecorder.datalyb.datatable.BaseType;
import com.biorecorder.datalyb.datatable.RegularColumn;

public class Max implements AggFunction {
    private String name = "MAX";
    private int maxInt;
    private double maxDouble;

    public Max() {
        reset();
    }

    @Override
    public void addInt(int value) {
        maxInt = Math.max(maxInt, value);
    }

    @Override
    public void addDouble(double value) {
        maxDouble = Math.max(maxDouble, value);;
    }

    @Override
    public String name() {
        return name;
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
    public void reset() {
        maxInt = Integer.MIN_VALUE;
        maxDouble = Double.MIN_VALUE;
    }

    @Override
    public BaseType outType(BaseType inType) {
        return inType;
    }

    @Override
    public double getAggregatedRegularColumnStart(RegularColumn columnToAgg, int pointsInGroup) {
        return columnToAgg.startValue() + (pointsInGroup - 1) * columnToAgg.step();
    }
}
