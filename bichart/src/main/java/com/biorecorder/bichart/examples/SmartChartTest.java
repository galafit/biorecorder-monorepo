package com.biorecorder.bichart.examples;

import com.biorecorder.bichart.XYData;
import com.biorecorder.bichart.swing.ChartPanel;
import com.biorecorder.bichart.traces.LineTracePainter;
import javax.swing.*;
import java.awt.*;


public class SmartChartTest  extends JFrame{
    public SmartChartTest() {
        int width = 400;
        int height = 500;


        int[] data = new int[1000];

        for (int i = 0; i < data.length; i++) {
            data[i] = i - 100;
        }

        XYData xyData1 = new XYData(data, data);
        XYData xyData2 = new XYData(data);

        ChartPanel chartPanel = new ChartPanel(false);
        chartPanel.addChartTrace("No Regular", xyData1, new LineTracePainter());

        chartPanel.addNavigatorTrace("No Regular", xyData1, new LineTracePainter() );


      /*  chartPanel.addChartStack();
        chartPanel.addChartTrace("Regular", xyData2, new LineTracePainter());
        chartPanel.addNavigatorStack();
        chartPanel.addNavigatorTrace("Regular", xyData2, new LineTracePainter());
*/
        chartPanel.setPreferredSize(new Dimension(width, height));
        add(chartPanel, BorderLayout.CENTER);
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addKeyListener(chartPanel);
        setLocationRelativeTo(null);
        setVisible(true);
    }


    public static void main(String[] args) {
        new SmartChartTest();
    }
}
