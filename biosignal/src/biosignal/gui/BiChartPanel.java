package biosignal.gui;

import biosignal.application.XYValues;
import com.biorecorder.bichart.ChartPanel;
import com.biorecorder.bichart.XYData;
import com.biorecorder.bichart.graphics.Range;
import com.biorecorder.bichart.traces.TracePainter;
import com.biorecorder.data.datatable.DataTable;
import com.biorecorder.data.datatable.DoubleColumn;
import com.biorecorder.data.datatable.IntColumn;
import com.biorecorder.data.datatable.RegularColumn;
import com.biorecorder.data.sequence.DoubleSequence;
import com.biorecorder.data.sequence.IntSequence;

import javax.swing.*;
import java.awt.*;

public class BiChartPanel extends JPanel {
    private final ChartPanel chartPanel;

    public BiChartPanel(boolean isDateTime) {
        chartPanel = new ChartPanel(isDateTime);
        setLayout(new BorderLayout());
        add(chartPanel);
    }

    public void autoScaleX() {
        chartPanel.autoScaleX();
    }

    public void addChartStack() {
        chartPanel.addChartStack();
    }

    public void addChartTrace(String name, XYValues data, TracePainter tracePainter) {
        chartPanel.addChartTrace(name, convertData(data), tracePainter);
    }

    public void addNavigatorStack() {
        chartPanel.addNavigatorStack();
    }

    public void addNavigatorTrace(String name, XYValues data, TracePainter tracePainter) {
        chartPanel.addNavigatorTrace(name, convertData(data), tracePainter);
    }

    public void setChartTraceData(int traceNumber, XYValues data) {
        chartPanel.setChartTraceData(traceNumber, convertData(data));
    }

    public double[] getChartXRange() {
        Range r = chartPanel.getChartXRange();
        double[] range = {r.getMin(), r.getMax()};
        return range;
    }

    public void setNavigatorTraceData(int traceNumber, XYValues data) {
        chartPanel.setNavigatorTraceData(traceNumber, convertData(data));
    }

    private XYData convertData(XYValues xyValues) {
        DataTable dt = new DataTable();
        IntSequence yData = xyValues.getYValues();
        if(xyValues.isRegular()) {
            dt.addColumn(new RegularColumn("x", xyValues.getStartValue(), xyValues.getStep(), yData.size()));
        } else {
            dt.addColumn(new DoubleColumn("x", xyValues.getXValues()));
        }
        dt.addColumn(new IntColumn("y", yData));
        return new XYData(dt);
    }
}
