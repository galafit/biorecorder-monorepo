package biosignal.gui;

import biosignal.filter.XYData;
import com.biorecorder.bichart.*;
import com.biorecorder.bichart.axis.XAxisPosition;
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
    BiChart biChart;
    ChartPanel chartPanel;

    public BiChartPanel(boolean isDateTime) {
        biChart = new SmartBiChart(isDateTime);
        chartPanel = new ChartPanel(biChart);
        setLayout(new BorderLayout());
        add(chartPanel);
    }

    public void addChartStack() {
        biChart.addChartStack();
    }

    public void addChartTrace(String name, XYData data, TracePainter tracePainter) {
        biChart.addChartTrace(name, convertData(data), tracePainter);
    }

    public void addChartTrace(String name, XYData data, TracePainter tracePainter, boolean isXOpposite,  boolean isYOpposite) {
        biChart.addChartTrace(name, convertData(data), tracePainter, isXOpposite, isYOpposite);
    }

    public void addNavigatorStack() {
        biChart.addNavigatorStack();
    }

    public void addNavigatorTrace(String name, XYData data, TracePainter tracePainter) {
        biChart.addNavigatorTrace(name, convertData(data), tracePainter);
    }

    public void setChartTraceData(int traceNumber, XYData data) {
        biChart.setChartTraceData(traceNumber, convertData(data));
    }

    public double[] getChartXRange() {
        Range r = null;
        for (XAxisPosition xPosition : XAxisPosition.values()) {
            r = Range.join(r, biChart.geChartXMinMax(xPosition));
        }
        double[] range = {r.getMin(), r.getMax()};
        return range;
    }

    public void setNavigatorTraceData(int traceNumber, XYData data) {
        biChart.setNavigatorTraceData(traceNumber, convertData(data));
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
        XYSeries xySeries = new XYSeries(dt);
        xySeries.setGroupingApproximationY(xyData.getGroupingApproximationY());
        return xySeries;
    }

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        super.setPreferredSize(preferredSize);
        chartPanel.setPreferredSize(preferredSize);
    }
}
