package com.biorecorder.data.datatable.aggregation;


import com.biorecorder.data.datatable.BaseType;
import com.biorecorder.data.datatable.RegularColumn;

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
