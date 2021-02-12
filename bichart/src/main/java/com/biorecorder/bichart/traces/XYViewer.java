package com.biorecorder.bichart.traces;

import com.biorecorder.bichart.ChartData;
import com.biorecorder.bichart.graphics.Range;

/**
 * Created by galafit on 2/11/17.
 */
public class XYViewer {
    ChartData data;
    int yColumnNumber;

    public XYViewer(ChartData data) {
       this(data, 0);
    }

    public XYViewer(ChartData data, int curveNumber) {
        this.data = data;
        yColumnNumber = curveNumber + 1;
    }

    public int size() {
        return data.rowCount();
    }

    public double getX(int index) {
        return data.value(index, 0);
    }

    public double getY(int index) {
        return data.value(index, yColumnNumber);
    }

    public Range getYMinMax() {
        return data.columnMinMax(yColumnNumber);
    }
    public Range getXMinMax() {
        return data.columnMinMax(0);
    }
}
