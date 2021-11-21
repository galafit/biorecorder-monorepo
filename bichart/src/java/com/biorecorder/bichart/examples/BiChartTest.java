package com.biorecorder.bichart.examples;

import com.biorecorder.bichart.BiChart;
import com.biorecorder.bichart.ChartPanel;
import com.biorecorder.bichart.ProcessingConfig;
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
    java.util.List<String> labels = new ArrayList();
    ChartPanel chartPanel;
    BiChart chart;

    public BiChartTest() {
        int width = 400;
        int height = 500;

        setTitle("Test chart");

        int[] data = new int[100];
        int[] data1 = new int[100];

        for (int i = 0; i < data.length; i++) {
            data[i] = i;
        }

        for (int i = 0; i < data1.length; i++) {
            data1[i] = i;
        }


       XYSeries xySeries1 = new XYSeries(0, 1, data);
       XYSeries xySeries2 = new XYSeries(0, 10, data1);

        chart = new BiChart(false, new ProcessingConfig().processingDisable(), false);

        chart.addChartTrace("trace1", xySeries1, new LineTracePainter());
        chart.addChartStack();
        chart.addChartTrace("trace2", xySeries2, new LineTracePainter(), true, true);

        //chart.addNavigatorTrace("trace1", xySeries1, new LineTracePainter());
        //chart.addNavigatorStack();
        chart.addNavigatorTrace("trace2", xySeries2, new LineTracePainter());


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
