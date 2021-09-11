package biosignal.gui;

import biosignal.application.XYData;
import com.biorecorder.bichart.ChartPanel;
import com.biorecorder.bichart.XYSeries;
import com.biorecorder.bichart.graphics.Range;
import com.biorecorder.bichart.traces.TracePainter;
import com.biorecorder.datalyb.datatable.DataTable;
import com.biorecorder.datalyb.datatable.DoubleColumn;
import com.biorecorder.datalyb.datatable.IntColumn;
import com.biorecorder.datalyb.datatable.RegularColumn;
import com.biorecorder.datalyb.series.IntSeries;

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

    public void addChartTrace(String name, XYData data, TracePainter tracePainter) {
        chartPanel.addChartTrace(name, convertData(data), tracePainter);
    }

    public void addNavigatorStack() {
        chartPanel.addNavigatorStack();
    }

    public void addNavigatorTrace(String name, XYData data, TracePainter tracePainter) {
        chartPanel.addNavigatorTrace(name, convertData(data), tracePainter);
    }

    public void setChartTraceData(int traceNumber, XYData data) {
        chartPanel.setChartTraceData(traceNumber, convertData(data));
    }

    public double[] getChartXRange() {
        Range r = chartPanel.getChartXRange();
        double[] range = {r.getMin(), r.getMax()};
        return range;
    }

    public void setNavigatorTraceData(int traceNumber, XYData data) {
        chartPanel.setNavigatorTraceData(traceNumber, convertData(data));
    }

    private XYSeries convertData(XYData xyData) {
        DataTable dt = new DataTable();
        IntSeries yData = xyData.getYValues();
        if(xyData.isRegular()) {
            dt.addColumn(new RegularColumn("x", xyData.getStartValue(), xyData.getStep(), yData.size()));
        } else {
            dt.addColumn(new DoubleColumn("x", xyData.getXValues()));
        }
        dt.addColumn(new IntColumn("y", yData));
        return new XYSeries(dt);
    }

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        super.setPreferredSize(preferredSize);
        chartPanel.setPreferredSize(preferredSize);
    }
}
