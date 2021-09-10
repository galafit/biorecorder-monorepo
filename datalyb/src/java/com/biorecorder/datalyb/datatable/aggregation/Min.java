package com.biorecorder.datalyb.datatable.aggregation;

import com.biorecorder.datalyb.datatable.BaseType;
import com.biorecorder.datalyb.datatable.RegularColumn;

public class Min implements AggFunction {
    private String name = "MIN";
    private int minInt;
    private double minDouble;

    public Min() {
        reset();
    }

    @Override
    public void addInt(int value) {
        minInt = Math.min(minInt, value);
    }

    @Override
    public void addDouble(double value) {
        minDouble = Math.min(minDouble, value);;
    }

    @Override
    public String name() {
        return name;
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
    public void reset() {
        minInt = Integer.MAX_VALUE;
        minDouble = Double.MAX_VALUE;
    }

    @Override
    public BaseType outType(BaseType inType) {
       return inType;
    }

    @Override
    public double getAggregatedRegularColumnStart(RegularColumn columnToAgg, int pointsInGroup) {
        return columnToAgg.getStartValue();
    }
}
