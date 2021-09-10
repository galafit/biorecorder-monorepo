package com.biorecorder.datalyb.datatable.aggregation;

import com.biorecorder.datalyb.datatable.Column;
import com.biorecorder.datalyb.series.IntSeries;

public interface Binning {
    IntSeries group(Column col);
    boolean isEqualPoints();
    int pointsInGroup();
}
