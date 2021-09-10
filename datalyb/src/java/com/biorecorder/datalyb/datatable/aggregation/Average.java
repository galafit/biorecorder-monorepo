package com.biorecorder.datalyb.datatable.aggregation;

import com.biorecorder.datalyb.datatable.BaseType;
import com.biorecorder.datalyb.datatable.RegularColumn;

public class Average implements AggFunction {
    private String name = "AVG";
    private long sumInt = 0;
    private double sumDouble = 0;
    private int count = 0;

    @Override
    public void addInt(int value) {
        sumInt = sumInt + value;
        count++;

    }

    @Override
    public void addDouble(double value) {
        sumDouble = sumDouble + value;
        count++;
    }
    @Override
    public String name() {
        return name;
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
    public void reset() {
        count = 0;
        sumInt = 0;
        sumDouble = 0;
    }

    @Override
    public BaseType outType(BaseType inType) {
        return inType;
    }

    @Override
    public double getAggregatedRegularColumnStart(RegularColumn columnToAgg, int pointsInGroup) {
        return columnToAgg.getStartValue() + pointsInGroup * columnToAgg.getStep() / 2;
    }
}
