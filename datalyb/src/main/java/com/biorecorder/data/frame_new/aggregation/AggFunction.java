package com.biorecorder.data.frame_new.aggregation;


import com.biorecorder.data.frame_new.BaseType;
import com.biorecorder.data.frame_new.RegularColumn;

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
