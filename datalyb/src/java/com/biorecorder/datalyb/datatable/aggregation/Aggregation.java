package com.biorecorder.datalyb.datatable.aggregation;

import com.biorecorder.datalyb.datatable.BaseType;
import com.biorecorder.datalyb.datatable.Column;
import com.biorecorder.datalyb.datatable.RegularColumn;
import com.biorecorder.datalyb.series.IntSeries;

public class Aggregation {
    private RegularColumn rc;
    private AggFunction aggFunction;
    private AggPipe pipe;

    public Aggregation(AggFunction aggFunction) {
        this.aggFunction = aggFunction;
    }

    public RegularColumn aggregate(RegularColumn columnToAgg, int pointsInGroup, int from, int length) {
        if(rc == null) {
            rc = new RegularColumn(columnToAgg.name(), columnToAgg.value(from), columnToAgg.getStep(), length);
        } else {
            rc.append(columnToAgg, from, length);
        }
        String name1 = rc.name() + "_" + aggFunction.name();
        int resampledSize = rc.size() / pointsInGroup;
        if(rc.size() % pointsInGroup == 0) {
            resampledSize--;
        }
        if(resampledSize < 0) {
            resampledSize = 0;
        }
        return new RegularColumn(name1, aggFunction.getAggregatedRegularColumnStart(rc, pointsInGroup) ,rc.getStep() * pointsInGroup , resampledSize);
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
