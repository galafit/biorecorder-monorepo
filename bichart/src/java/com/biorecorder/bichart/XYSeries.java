package com.biorecorder.bichart;

import com.biorecorder.bichart.graphics.Range;
import com.biorecorder.datalyb.datatable.BaseType;
import com.biorecorder.datalyb.datatable.DataTable;
import com.biorecorder.datalyb.datatable.IntColumn;
import com.biorecorder.datalyb.datatable.RegularColumn;

public class XYSeries implements ChartData {
    DataTable dataTable = new DataTable();
    GroupingApproximation groupingApproximationX = GroupingApproximation.OPEN;
    GroupingApproximation groupingApproximationY = GroupingApproximation.AVERAGE;

    public XYSeries(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    public XYSeries(int[] xData, int[] yData) {
        dataTable.addColumn(new IntColumn("x", xData));
        dataTable.addColumn(new IntColumn("y", yData));
    }

    public XYSeries(int[] yData) {
        dataTable.addColumn(new RegularColumn("x",0, 1, yData.length));
        dataTable.addColumn(new IntColumn("y", yData));
    }

    public XYSeries(double startValue, double step, int[] yData) {
        dataTable.addColumn(new RegularColumn("x",startValue, step, yData.length));
        dataTable.addColumn(new IntColumn("y", yData));
    }

    public DataTable getDataTable() {
        return dataTable;
    }

    public XYSeries getEmptyCopy() {
        DataTable emptyData = new DataTable();
        for (int i = 0; i < dataTable.columnCount(); i++) {
            emptyData.addColumn(dataTable.getColumn(i).emptyCopy());
        }
        XYSeries copy = new XYSeries(emptyData);
        copy.groupingApproximationX = groupingApproximationX;
        copy.groupingApproximationY = groupingApproximationY;
        return copy;
    }

    public void appendData(XYSeries dataToAppend) {
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
        double[] minMax = dataTable.minMax(columnIndex);
        if(minMax != null) {
            return new Range(minMax[0], minMax[1]);
        }
        return null;
    }

    public double xValue(int index) {
        return value(index, 0);
    }

    public double yValue(int index) {
        return value(index, 1);
    }

    public int size() {
        return rowCount();
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

    public XYSeries view(int from, int length) {
        XYSeries view = new XYSeries(dataTable.view(from, length));
        view.groupingApproximationX = groupingApproximationX;
        view.groupingApproximationY = groupingApproximationY;
        return view;
    }
}
