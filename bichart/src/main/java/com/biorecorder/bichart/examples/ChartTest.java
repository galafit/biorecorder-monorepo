package com.biorecorder.bichart.examples;

import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.axis.YAxisPosition;
import com.biorecorder.bichart.chart.Chart;
import com.biorecorder.bichart.XYData;
import com.biorecorder.bichart.scales.LinearScale;
import com.biorecorder.bichart.themes.DarkTheme;
import com.biorecorder.bichart.themes.WhiteTheme;
import com.biorecorder.bichart.traces.LineTracePainter;
import com.biorecorder.data.list.IntEditableArrayList;
import com.biorecorder.bichart.swing.ChartPanelOld;
import com.biorecorder.data.list.LongArrayList;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by galafit on 21/9/18.
 */
public class ChartTest extends JFrame {
    IntEditableArrayList yUnsort = new IntEditableArrayList();
    IntEditableArrayList xUnsort = new IntEditableArrayList();

    IntEditableArrayList list1 = new IntEditableArrayList();
    IntEditableArrayList list2 = new IntEditableArrayList();

    List<String> labels = new ArrayList();

    Chart chart;
    ChartPanelOld chartPanel;

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


        XYData regularData = new XYData(list1.toArray());

        XYData unsortedData = new XYData(xUnsort.toArray(), yUnsort.toArray());


        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(0);
        LongArrayList timeArray = new LongArrayList();
        for (int i = 0; i < 150; i++) {
            timeArray.add(calendar.getTimeInMillis());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        chart = new Chart(DarkTheme.getChartConfig(), new LinearScale());
        chart.setTitle("как дела? все хорошо как поживаете вы олрдлорлор лорор лорлор");
        chart.setConfig(WhiteTheme.getChartConfig());

        chart.addTrace("trace1", unsortedData, new LineTracePainter(),  XAxisPosition.TOP, YAxisPosition.RIGHT);
        chart.addStack();
        chart.addTrace("trace2", regularData, new LineTracePainter());

        chart.autoScaleY();
        chart.autoScaleX();
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
       ChartTest chartTest = new ChartTest();
    }
}
