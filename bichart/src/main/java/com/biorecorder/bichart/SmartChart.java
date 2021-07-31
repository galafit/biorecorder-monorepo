package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.YAxisPosition;
import com.biorecorder.bichart.scales.LinearScale;
import com.biorecorder.bichart.scales.Scale;
import com.biorecorder.bichart.scales.TimeScale;
import com.biorecorder.bichart.themes.DarkTheme;
import com.biorecorder.bichart.traces.TracePainter;
import com.biorecorder.data.frame_new.DataTable;


public class SmartChart {
    private DataProcessor dataProcessor;
    private NavigableChart1 navigableChart;
    private boolean isValid = false;

    public SmartChart(ProcessingConfig processingConfig, NavigableChartConfig chartConfig, Scale xScale, boolean isDateTime) {
        dataProcessor = new DataProcessor(processingConfig, isDateTime);
        this.navigableChart = new NavigableChart1(chartConfig, xScale);
    }

    public static SmartChart createChart() {
        return new SmartChart(new ProcessingConfig(), DarkTheme.getNavigableChartConfig(), new LinearScale(), false);
    }

    public static SmartChart createTimeChart() {
        return new SmartChart(new ProcessingConfig(), DarkTheme.getNavigableChartConfig(), new TimeScale(), true);
    }

    public void setChartYMinMax(int stack, YAxisPosition yPosition, double min, double max) {
        navigableChart.setChartYMinMax(stack, yPosition, min, max);
    }

    public void autoScaleChartY() {
        navigableChart.autoScaleChartY();
    }

    public void addChartStack() {
        navigableChart.addChartStack();
    }

    public void addChartTrace(String name, XYData data, TracePainter tracePainter) {
        DataTable emptyData = new DataTable();
        DataTable dataTable = data.getDataTable();
        for (int i = 0; i < data.columnCount(); i++) {
            emptyData.addColumn(dataTable.getColumn(i).emptyCopy());
        }
      //  chartDataList.add(data);

        navigableChart.addChartTrace(name, new XYData(emptyData), tracePainter);

    }

    public void addNavigatorStack() {
        navigableChart.addNavigatorStack();
    }

    public void addNavigatorTrace(String name, XYData data, TracePainter tracePainter) {
        DataTable emptyData = new DataTable();
        DataTable dataTable = data.getDataTable();
        for (int i = 0; i < data.columnCount(); i++) {
            emptyData.addColumn(dataTable.getColumn(i).emptyCopy());
        }
       // navigatorDataList.add(data);
        navigableChart.addNavigatorTrace(name, new XYData(emptyData), tracePainter);
    }

    public void setNavigatorYMinMax(int stack, YAxisPosition yPosition, double min, double max) {
        navigableChart.setNavigatorYMinMax(stack, yPosition, min, max);
    }

    public void autoScaleNavigatorY() {
        navigableChart.autoScaleNavigatorY();
    }
}
