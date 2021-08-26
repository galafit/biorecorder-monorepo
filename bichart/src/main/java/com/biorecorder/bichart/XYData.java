package com.biorecorder.bichart;

import com.biorecorder.bichart.chart.ChartData;
import com.biorecorder.bichart.configs.GroupingApproximation;
import com.biorecorder.bichart.graphics.Range;
import com.biorecorder.data.datatable.BaseType;
import com.biorecorder.data.datatable.DataTable;
import com.biorecorder.data.datatable.IntColumn;
import com.biorecorder.data.datatable.RegularColumn;

public class XYData implements ChartData {
    DataTable dataTable = new DataTable();
    GroupingApproximation groupingApproximationX = GroupingApproximation.OPEN;
    GroupingApproximation groupingApproximationY = GroupingApproximation.AVERAGE;

    public XYData(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    public XYData(int[] xData, int[] yData) {
        dataTable.addColumn(new IntColumn("x", xData));
        dataTable.addColumn(new IntColumn("y", yData));
    }

    public XYData(int[] yData) {
        dataTable.addColumn(new RegularColumn("x",0, 1, yData.length));
        dataTable.addColumn(new IntColumn("y", yData));
    }

    public XYData(double startValue, double step, int[] yData) {
        dataTable.addColumn(new RegularColumn("x",startValue, step, yData.length));
        dataTable.addColumn(new IntColumn("y", yData));
    }

    public DataTable getDataTable() {
        return dataTable;
    }

    public XYData getEmptyCopy() {
        DataTable emptyData = new DataTable();
        for (int i = 0; i < dataTable.columnCount(); i++) {
            emptyData.addColumn(dataTable.getColumn(i).emptyCopy());
        }
        return new XYData(emptyData);
    }

    public void appendData(XYData dataToAppend) {
        dataTable.append(dataToAppend.dataTable);
    }

    public GroupingApproximation getGroupingApproximationX() {
        return groupingApproximationX;
    }

    public void setGroupingApproximationX(GroupingApproximation groupingApproximationX) {
        this.groupingApproximationX = groupingApproximationX;
    }

    public GroupingApproximation getGroupingApproximationY() {
        return groupingApproximationY;
    }

    public void setGroupingApproximationY(GroupingApproximation groupingApproximationY) {
        this.groupingApproximationY = groupingApproximationY;
    }

    @Override
    public int rowCount() {
        return dataTable.rowCount();
    }

    @Override
    public int columnCount() {
        return dataTable.columnCount();
    }

    @Override
    public String columnName(int columnIndex) {
        return dataTable.getColumnName(columnIndex);
    }

    @Override
    public boolean isNumberColumn(int columnIndex) {
        if(dataTable.getColumn(columnIndex).type() == BaseType.OBJECT) {
            return  false;
        }
        return true;
    }

    @Override
    public double value(int rowIndex, int columnIndex) {
        return dataTable.value(rowIndex, columnIndex);
    }

    @Override
    public String label(int rowIndex, int columnIndex) {
        return dataTable.label(rowIndex, columnIndex);
    }

    @Override
    public Range columnMinMax(int columnIndex) {
        double min = dataTable.min(columnIndex);
        double max = dataTable.max(columnIndex);
        if(!Double.isNaN(min) && !Double.isNaN(max)) {
            return new Range(min, max);
        }
        return null;
    }

    public double xValue(int rowIndex) {
        return value(rowIndex, 0);
    }

    public Range xMinMax() {
        return columnMinMax(0);
    }

    @Override
    public int[] sortedIndices() {
        return dataTable.sortedIndices(0);
    }

    @Override
    public int bisect(double value, int[] sorter) {
        return dataTable.bisect(0, value, sorter);
    }

    public int bisectLeft(double value) {
        return dataTable.bisectLeft(0, value);
    }

    public int bisectRight(double value) {
        return dataTable.bisectRight(0, value);
    }

    public XYData view(int from, int length) {
        return new XYData(dataTable.view(from, length));
    }
}
