package biosignal.gui;

import biosignal.filter.XYData;
import com.biorecorder.bichart.*;
import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.graphics.Range;
import com.biorecorder.bichart.traces.TracePainter;
import com.biorecorder.datalyb.datatable.DataTable;

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

    public void addChartTrace(String name, XYData data, GroupingApproximation groupingApproximation, TracePainter tracePainter, boolean isXOpposite,  boolean isYOpposite) {
        biChart.addChartTrace(name, convertData(data, groupingApproximation), tracePainter, isXOpposite, isYOpposite);
    }

    public void addNavigatorStack() {
        biChart.addNavigatorStack();
    }

    public void addNavigatorTrace(String name, XYData data, GroupingApproximation groupingApproximation,TracePainter tracePainter) {
        biChart.addNavigatorTrace(name, convertData(data, groupingApproximation), tracePainter);
    }

    public double[] getChartXRange() {
        Range r = null;
        for (XAxisPosition xPosition : XAxisPosition.values()) {
            r = Range.join(r, biChart.geChartXMinMax(xPosition));
        }
        double[] range = {r.getMin(), r.getMax()};
        return range;
    }


    private XYSeries convertData(XYData xyData, GroupingApproximation groupingApproximation) {
        DataTable dt = xyData.getDataTable();
        XYSeries xySeries = new XYSeries(dt);
        xySeries.setGroupingApproximationY(groupingApproximation);
        return xySeries;
    }

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        super.setPreferredSize(preferredSize);
        chartPanel.setPreferredSize(preferredSize);
    }
}
