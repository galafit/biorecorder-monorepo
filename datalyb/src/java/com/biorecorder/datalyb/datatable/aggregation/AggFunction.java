package com.biorecorder.datalyb.datatable.aggregation;


import com.biorecorder.datalyb.datatable.BaseType;
import com.biorecorder.datalyb.datatable.RegularColumn;

public interface AggFunction {
    String name();
    void addInt(int value);
    void addDouble(double value);
    int getInt();
    double getDouble();
    void reset();
    double getAggregatedRegularColumnStart(RegularColumn columnToAgg, int pointsInGroup);
    BaseType outType(BaseType inType);
}
