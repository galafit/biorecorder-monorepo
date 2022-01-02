package com.biorecorder.datalyb.datatable.aggregation;

import com.biorecorder.datalyb.datatable.BaseType;
import com.biorecorder.datalyb.datatable.Column;
import com.biorecorder.datalyb.datatable.RegularColumn;
import com.biorecorder.datalyb.series.IntSeries;

public class Aggregation {
    private RegularColumn rc;
    private AggFunction aggFunction;
    private AggPipe pipe;
    private int aggSampleCount;
    private int pointsInGroup;

    public Aggregation(AggFunction aggFunction) {
        this.aggFunction = aggFunction;
    }

    public RegularColumn aggregate(RegularColumn columnToAgg, int pointsInGroup, int from, int length) throws IllegalArgumentException{
        if(rc == null) {
            rc = new RegularColumn(columnToAgg.name(), columnToAgg.value(from), columnToAgg.step());
            this.pointsInGroup = pointsInGroup;
            aggSampleCount = 0;
        }
        if(this.pointsInGroup != pointsInGroup) {
            throw new IllegalArgumentException("Points in group: " + pointsInGroup +", expected: "+ this.pointsInGroup);
        }
        if(rc.step() != columnToAgg.step()) {
            throw new IllegalArgumentException("RegularColumn step: " + columnToAgg.step() +", expected: "+ rc.step());
        }
        if(rc.value(aggSampleCount) != columnToAgg.value(from)) {
            throw new IllegalArgumentException("RegularColumn startValue: " + columnToAgg.value(from) +", expected: "+ rc.value(aggSampleCount));
        }
        aggSampleCount += length;
        String name1 = rc.name() + "_" + aggFunction.name();
        int resampledSize = aggSampleCount / pointsInGroup;
        if(aggSampleCount % pointsInGroup == 0) {
            resampledSize--;
        }
        if(resampledSize < 0) {
            resampledSize = 0;
        }
        return new RegularColumn(name1, aggFunction.getAggregatedRegularColumnStart(rc, pointsInGroup) ,rc.step() * pointsInGroup , resampledSize);
    }

    public Column aggregate(Column columnToAgg, IntSeries groups, int from, int length) {
        if(pipe == null) {
            BaseType colType = columnToAgg.type();
            if(aggFunction.outType(colType) == colType) {
                switch (colType) {
                    case INT:
                        pipe = new IntAggPipe(aggFunction);
                        break;
                    default:
                        pipe = new DoubleAggPipe(aggFunction);
                        break;
                }
            } else {
                pipe = new DoubleAggPipe(aggFunction);
            }
        }
        pipe.setColumnToAgg(columnToAgg);
        int groupCounter = 0;
        int groupStart = columnToAgg.size() + 1;
        if(groups.size() > 0) {
            groupStart = groups.get(groupCounter);
        }
        int till = from + length;
        for (int i = from; i < till; i++) {
            if(i == groupStart) {
                pipe.push();
                groupCounter++;
                if(groupCounter < groups.size()) {
                    groupStart = groups.get(groupCounter);
                }
            }
            pipe.agg(i);
        }
        pipe.removeColumnToAgg();
        return pipe.resultantCol();
    }
}
