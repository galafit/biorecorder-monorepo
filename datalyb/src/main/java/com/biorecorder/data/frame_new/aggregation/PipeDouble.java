package com.biorecorder.data.frame_new.aggregation;

import com.biorecorder.data.frame_new.*;

public class PipeDouble implements AggPipe {
    private AggFunction aggFunction;
    private Column columnToAgg;
    private DoubleColumn resultantColumn;

    public PipeDouble(AggFunction aggFunction) {
        this.aggFunction = aggFunction;
    }

    @Override
    public void agg(int index) {
        aggFunction.addDouble(columnToAgg.value(index));
    }

    @Override
    public void push() {
        resultantColumn.append(aggFunction.getDouble());
        aggFunction.reset();
    }

    @Override
    public Column resultantCol() {
        return resultantColumn;
    }

    @Override
    public void setColumnToAgg(Column columnToAgg) throws IllegalArgumentException {
        ColumnType type = columnToAgg.type();
        if(resultantColumn == null) {
            resultantColumn = new DoubleColumn(columnToAgg.name() + "_"+aggFunction.name());
        }
        this.columnToAgg =  columnToAgg;
    }
}
