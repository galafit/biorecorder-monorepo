package com.biorecorder.datalyb.datatable.aggregation;

import com.biorecorder.datalyb.datatable.*;

public class DoubleAggPipe implements AggPipe {
    private AggFunction aggFunction;
    private Column columnToAgg;
    private DoubleColumn resultantColumn;

    public DoubleAggPipe(AggFunction aggFunction) {
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
        if(resultantColumn == null) {
            resultantColumn = new DoubleColumn(columnToAgg.name() + "_" + aggFunction.name());
        }
        this.columnToAgg =  columnToAgg;
    }
}
