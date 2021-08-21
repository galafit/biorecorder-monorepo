package com.biorecorder.data.frame_new.aggregation;

import com.biorecorder.data.frame_new.BaseType;
import com.biorecorder.data.frame_new.Column;
import com.biorecorder.data.frame_new.RegularColumn;
import com.biorecorder.data.sequence.IntSequence;

public class Aggregation {
    private RegularColumn rc;
    private AggFunction aggFunction;
    private AggPipe pipe;

    public Aggregation(AggFunction aggFunction) {
        this.aggFunction = aggFunction;
    }

    public RegularColumn aggregate(RegularColumn columnToAgg, int pointsInGroup) {
        if(rc == null) {
            rc = new RegularColumn(columnToAgg.name(), columnToAgg.getStartValue(), columnToAgg.getStep(), columnToAgg.size());
        } else {
            rc.append(columnToAgg);
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

    public Column aggregate(Column columnToAgg, IntSequence groups) {
        if(pipe == null) {
            BaseType colType = columnToAgg.type().getBaseType();
            if(aggFunction.outType(colType) == colType) {
                switch (colType) {
                    case INT:
                        pipe = new PipeInt(aggFunction);
                        break;
                    default:
                        pipe = new PipeDouble(aggFunction);
                        break;
                }
            } else {
                pipe = new PipeDouble(aggFunction);
            }
        }
        pipe.setColumnToAgg(columnToAgg);
        int groupCounter = 0;
        int groupStart = columnToAgg.size() + 1;
        if(groups.size() > 0) {
            groupStart = groups.get(groupCounter);
        }
        for (int i = 0; i < columnToAgg.size(); i++) {
            if(i == groupStart) {
                pipe.push();
                groupCounter++;
                if(groupCounter < groups.size()) {
                    groupStart = groups.get(groupCounter);
                }
            }
            pipe.agg(i);
        }
        return pipe.resultantCol();
    }
}
