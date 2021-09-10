package com.biorecorder.datalyb.datatable.aggregation;

import com.biorecorder.datalyb.datatable.*;

public class IntAggPipe implements AggPipe {
    private AggFunction aggFunction;
    private IntColumn columnToAgg;
    private IntColumn resultantColumn;

    public IntAggPipe(AggFunction aggFunction) {
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
        if(columnToAgg.type() != BaseType.INT) {
            throw new IllegalArgumentException("Column to aggregate must be IntColumn! Column type = " + columnToAgg.type());
        }
        if(resultantColumn == null) {
            resultantColumn = new IntColumn(columnToAgg.name() + "_"+aggFunction.name());
        }
        this.columnToAgg = (IntColumn) columnToAgg;
    }
}
