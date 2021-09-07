package com.biorecorder.bichart.examples;

import com.biorecorder.bichart.XYData;
import com.biorecorder.bichart.ChartPanel;
import com.biorecorder.bichart.traces.LineTracePainter;
import javax.swing.*;
import java.awt.*;

public class SmartChartTest  extends JFrame{
    public SmartChartTest() {
        int width = 400;
        int height = 500;

        int[] data = new int[1000];
        int[] data1 = new int[10];

        for (int i = 0; i < data.length; i++) {
            data[i] = i;
        }

        for (int i = 0; i < data1.length; i++) {
            data1[i] = i;
        }


        XYData xyData1 = new XYData(data, data);
        XYData xyData2 = new XYData(data1);

        ChartPanel chartPanel = new ChartPanel(false);
        chartPanel.addChartTrace("No Regular", xyData1, new LineTracePainter());
        chartPanel.addChartStack();
       // chartPanel.addChartTrace("Regular", xyData2, new LineTracePainter());

      //  chartPanel.addNavigatorTrace("zero", new XYData(new int[0]), new LineTracePainter() );
       // chartPanel.addNavigatorTrace("No Regular", xyData1, new LineTracePainter() );
       // chartPanel.addNavigatorStack();
        chartPanel.addNavigatorTrace("Regular", xyData2, new LineTracePainter());

        chartPanel.setPreferredSize(new Dimension(width, height));
        add(chartPanel, BorderLayout.CENTER);
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addKeyListener(chartPanel);
        setLocationRelativeTo(null);
        setVisible(true);

       /* for (int i = 0; i < 5 ; i++) {
            try{
                Thread.sleep(1000);
            } catch(InterruptedException e){
                System.out.println(e);
            }
            int[] data1 = new int[50];
            for (int j = 0; j < data1.length; j++) {
                data1[j] = n + i*50 + j;
            }

            XYData dataToAppend = new XYData(data1, data1);
            XYData regularDataToAppend = new XYData(0,1,data1);
            chartPanel.appendNavigatorTraceData(0, dataToAppend);
            chartPanel.appendNavigatorTraceData(1, regularDataToAppend);
            chartPanel.repaint();
        } */
    }

    public static void main(String[] args) {
        new SmartChartTest();
    }
}

