package com.biorecorder.bichart.examples;

import com.biorecorder.bichart.BiChart;
import com.biorecorder.bichart.ChartPanel;
import com.biorecorder.bichart.XYSeries;
import com.biorecorder.bichart.themes.DarkTheme;
import com.biorecorder.bichart.traces.LineTracePainter;
import com.biorecorder.datalyb.list.IntArrayList;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Created by galafit on 27/9/18.
 */
public class BiChartTest extends JFrame{
    IntArrayList yData;
    IntArrayList xData;
    java.util.List<String> labels = new ArrayList();
    ChartPanel chartPanel;
    BiChart chart;

    public BiChartTest() {
        int width = 400;
        int height = 500;

        setTitle("Test chart");

        yData = new IntArrayList();
        xData = new IntArrayList();

       for (int i = 0; i < 100; i++) {
            yData.add(i);
            xData.add(i);
            labels.add("l_"+i);
        }


        XYSeries xySeries = new XYSeries(xData.toArray(), yData.toArray());
        XYSeries xySeries1 = new XYSeries(0, 1, yData.toArray());
        XYSeries xySeries2 = new XYSeries(-10, 2, yData.toArray());


        chart = new BiChart(DarkTheme.getNavigableChartConfig(), false);

        chart.addChartTrace("trace1", xySeries1, new LineTracePainter());
        chart.addChartStack();
        chart.addChartTrace("trace2", xySeries2, new LineTracePainter(), true, true);

        chart.addNavigatorTrace("trace1", xySeries1, new LineTracePainter());
        chart.addNavigatorStack();
        chart.addNavigatorTrace("trace2", xySeries2, new LineTracePainter(), true);


        chartPanel = new ChartPanel(chart);

        chartPanel.setPreferredSize(new Dimension(width, height));
        add(chartPanel, BorderLayout.CENTER);
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addKeyListener(chartPanel.getKeyListener());
        setLocationRelativeTo(null);
        setVisible(true);
    }


    public static void main(String[] args) {
        BiChartTest chartTest = new BiChartTest();
    }
}
