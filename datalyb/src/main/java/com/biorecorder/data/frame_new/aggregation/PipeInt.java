package com.biorecorder.data.frame_new.aggregation;

import com.biorecorder.data.frame_new.*;

public class PipeInt implements AggPipe {
    private AggFunction aggFunction;
    private IntColumn columnToAgg;
    private IntColumn resultantColumn;

    public PipeInt(AggFunction aggFunction) {
        this.aggFunction = aggFunction;
    }

    @Override
    public void agg(int index) {
        aggFunction.addInt(columnToAgg.intValue(index));
    }

    @Override
    public void push() {
        resultantColumn.append(aggFunction.getInt());
        aggFunction.reset();
    }

    @Override
    public Column resultantCol() {
        return resultantColumn;
    }

    @Override
    public void setColumnToAgg(Column columnToAgg) throws IllegalArgumentException {
        ColumnType type = columnToAgg.type();
        if(type.getBaseType() != BaseType.INT) {
            throw new IllegalArgumentException("Column to aggregate must be IntColumn! Column type = "+type.getBaseType());
        }
        if(resultantColumn == null) {
            resultantColumn = new IntColumn(columnToAgg.name() + "_"+aggFunction.name());
        }
        this.columnToAgg = (IntColumn) columnToAgg;
    }
}
