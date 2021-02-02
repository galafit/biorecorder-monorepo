package com.biorecorder.bichart.examples;

import com.biorecorder.bichart.*;
import com.biorecorder.bichart.dataprocessing.DataProcessingConfig;
import com.biorecorder.bichart.dataprocessing.XYData;
import com.biorecorder.bichart.graphics.BColor;
import com.biorecorder.bichart.themes.DarkTheme;
import com.biorecorder.bichart.traces.LineTracePainter;
import com.biorecorder.data.list.IntArrayList;
import com.biorecorder.bichart.swing.ChartPanel;

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
    ChartPanel chartPanel;
    NavigableChart chart;
    XYData xyData;
    XYData xyData1;

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


        xyData = new XYData(xData, true);
        xyData.addYColumn("y", yData);

        xyData1 = new XYData(0, 1, true);
        xyData1.addYColumn("regular1", xData);
        xyData1.addYColumn("regular2", yData);


        DataProcessingConfig navigatorProcessing = new DataProcessingConfig();
        double[] groupingIntervals = {20, 40};
        navigatorProcessing.setGroupingIntervals(groupingIntervals);
        navigatorProcessing.setGroupingForced(true);
        navigatorProcessing.setGroupingEnabled(false);
        navigatorProcessing.setCropEnabled(false);

        chart = new NavigableChart();

        chart.addChartTrace(xyData, new LineTracePainter(), true);
        chart.addChartStack();
        chart.addChartTrace(xyData1, new LineTracePainter());
        chart.addNavigatorTrace(xyData, new LineTracePainter(), true);

        chartPanel = new ChartPanel(chart);

        chartPanel.setPreferredSize(new Dimension(width, height));
        add(chartPanel, BorderLayout.CENTER);
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addKeyListener(chartPanel);
        setLocationRelativeTo(null);
        setVisible(true);

     /*   Thread t1 = new Thread(new Runnable() {
            int interval = 2000;
            @Override
            public void run() {

                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                chart.setChartTraceColor(1, BColor.RED);
                chart.setChartTraceName(1, "new Name");
                chartPanel.repaint();

                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                NavigableChartConfig config = new DarkTheme().getNavigableChartConfig();
                config.setGap(20);
                chart.setConfig(config);
                chartPanel.repaint();
            }
        });*/
        //t1.start();

    }


    public static void main(String[] args) {
        NavigableChartTest chartTest = new NavigableChartTest();
    }
}
