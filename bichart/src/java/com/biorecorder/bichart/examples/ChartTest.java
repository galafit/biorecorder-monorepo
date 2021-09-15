package com.biorecorder.bichart.examples;

import com.biorecorder.bichart.ChartPanel1;
import com.biorecorder.bichart.Chart;
import com.biorecorder.bichart.XYSeries;
import com.biorecorder.bichart.scales.LinearScale;
import com.biorecorder.bichart.themes.DarkTheme;
import com.biorecorder.bichart.traces.LineTraceConfig;
import com.biorecorder.bichart.traces.LineTracePainter;
import com.biorecorder.datalyb.list.IntArrayList;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by galafit on 21/9/18.
 */
public class ChartTest extends JFrame {
    IntArrayList yUnsort = new IntArrayList();
    IntArrayList xUnsort = new IntArrayList();

    IntArrayList list1 = new IntArrayList();
    IntArrayList list2 = new IntArrayList();

    List<String> labels = new ArrayList();

    Chart chart;
    ChartPanel1 chartPanel;

    public ChartTest()  {
        int width = 500;
        int height = 500;

        setTitle("Test chart");

        int value = 0;
        for (int i = 0; i <= 20; i++) {
            list1.add(value);
            list2.add(50);
            labels.add("lab_"+i);
            value += 1;
        }


        xUnsort.add(50);
        xUnsort.add(300);
        xUnsort.add(200);
        xUnsort.add(100);
        xUnsort.add(150);
        xUnsort.add(20);

        yUnsort.add(100);
        yUnsort.add(200);
        yUnsort.add(150);
        yUnsort.add(10);
        yUnsort.add(300);
        yUnsort.add(300);


        XYSeries regularData = new XYSeries(list1.toArray());

        XYSeries unsortedData = new XYSeries(xUnsort.toArray(), yUnsort.toArray());


        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(0);

        chart = new Chart(DarkTheme.getChartConfig(), new LinearScale());
        chart.setTitle("как дела? все хорошо как поживаете вы олрдлорлор лорор лорлор");

        chart.addTrace("trace1", unsortedData, new LineTracePainter(),  true, true);
        chart.addStack();
        chart.addTrace("trace2", regularData,
                new LineTracePainter(new LineTraceConfig(LineTraceConfig.VERTICAL_LINES)));

        chart.autoScaleY();
        chart.autoScaleX();
        chartPanel = new ChartPanel1(chart);

        chartPanel.setPreferredSize(new Dimension(width, height));
        add(chartPanel, BorderLayout.CENTER);
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addKeyListener(chartPanel.getKeyListener());
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
       ChartTest chartTest = new ChartTest();
    }
}
