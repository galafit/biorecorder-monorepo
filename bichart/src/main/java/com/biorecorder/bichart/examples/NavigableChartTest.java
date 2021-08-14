package com.biorecorder.bichart.examples;

import com.biorecorder.bichart.*;
import com.biorecorder.bichart.scales.LinearScale;
import com.biorecorder.bichart.themes.DarkTheme;
import com.biorecorder.bichart.traces.LineTracePainter;
import com.biorecorder.data.list.IntArrayList;
import com.biorecorder.bichart.swing.ChartPanelOld;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Created by galafit on 27/9/18.
 */
public class NavigableChartTest extends JFrame{
    IntArrayList yData;
    IntArrayList xData;
    java.util.List<String> labels = new ArrayList();
    ChartPanelOld chartPanel;
    NavigableChart chart;

    public NavigableChartTest() {
        int width = 400;
        int height = 500;

        setTitle("Test chart");

        yData = new IntArrayList();
        xData = new IntArrayList();

       for (int i = 0; i <= 100; i++) {
            yData.add(i);
            xData.add(i);
            labels.add("l_"+i);
        }


        XYData xyData = new XYData(xData.toArray(), yData.toArray());
        XYData xyData1 = new XYData(0, 1, yData.toArray());

        chart = new NavigableChart(DarkTheme.getNavigableChartConfig(), new LinearScale());

        chart.addChartTrace("trace1", xyData, new LineTracePainter());
        chart.addChartStack();
        chart.addChartTrace("trace2", xyData1, new LineTracePainter());
        chart.addNavigatorTrace("trace3", xyData, new LineTracePainter());

        chartPanel = new ChartPanelOld(chart);

        chartPanel.setPreferredSize(new Dimension(width, height));
        add(chartPanel, BorderLayout.CENTER);
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addKeyListener(chartPanel);
        setLocationRelativeTo(null);
        setVisible(true);
    }


    public static void main(String[] args) {
        NavigableChartTest chartTest = new NavigableChartTest();
    }
}
