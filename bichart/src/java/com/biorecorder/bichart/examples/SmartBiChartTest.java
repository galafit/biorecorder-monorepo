package com.biorecorder.bichart.examples;

import com.biorecorder.bichart.ChartPanel;
import com.biorecorder.bichart.SmartBiChart;
import com.biorecorder.bichart.XYSeries;
import com.biorecorder.bichart.traces.LineTracePainter;
import com.biorecorder.bichart.traces.VerticalLinePainter;

import javax.swing.*;
import java.awt.*;

public class SmartBiChartTest extends JFrame {
    public SmartBiChartTest() {
        int width = 400;
        int height = 500;

        int[] data1 = new int[100];
        int[] data2 = new int[10];

        for (int i = 0; i < data1.length; i++) {
            data1[i] = i;
        }

        for (int i = 0; i < data2.length; i++) {
            data2[i] = i;
        }


        XYSeries xySeries1 = new XYSeries(new int[0], new int[0]);
        XYSeries xySeries2 = new XYSeries(0, 1, new int[0]);

        SmartBiChart smartBiChart = new SmartBiChart(false);
        smartBiChart.addChartTrace("No Regular", xySeries1, new LineTracePainter());
        smartBiChart.addChartStack();
        smartBiChart.addChartTrace("Regular", xySeries2, new LineTracePainter(), true, false);

        //  chartPanel.addNavigatorTrace("zero", new XYData(new int[0]), new LineTracePainter() );
        // chartPanel.addNavigatorTrace("No Regular", xyData1, new LineTracePainter() );
        // chartPanel.addNavigatorStack();
        smartBiChart.addNavigatorTrace("Regular", xySeries2, new LineTracePainter());

        ChartPanel chartPanel = new ChartPanel(smartBiChart);
        chartPanel.setPreferredSize(new Dimension(width, height));
        add(chartPanel, BorderLayout.CENTER);
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        // addKeyListener(chartPanel);
        setLocationRelativeTo(null);
        setVisible(true);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        xySeries1.appendData(new XYSeries(data1, data1));
        xySeries2.appendData(new XYSeries(0, 1, data1));
        smartBiChart.dataAppended();
        chartPanel.repaint();
        int size = 10;
        for (int i = 0; i < 0; i++) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
            int[] data = new int[size];
            for (int j = 0; j < data.length; j++) {
                data[j] = data1.length + i * size + j;
            }

            XYSeries unregData = new XYSeries(data, data);
            XYSeries regData = new XYSeries(data[0], 1, data);
            xySeries1.appendData(unregData);
            xySeries2.appendData(regData);
            smartBiChart.dataAppended();
            chartPanel.repaint();
        }
    }

    public static void main(String[] args) {
        new SmartBiChartTest();
    }
}

