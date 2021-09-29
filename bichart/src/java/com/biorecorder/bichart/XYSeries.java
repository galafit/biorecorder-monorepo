package com.biorecorder.bichart;

import com.biorecorder.bichart.graphics.Range;
import com.biorecorder.datalyb.datatable.BaseType;
import com.biorecorder.datalyb.datatable.DataTable;
import com.biorecorder.datalyb.datatable.IntColumn;
import com.biorecorder.datalyb.datatable.RegularColumn;

public class XYSeries implements ChartData {
    DataTable dataTable = new DataTable("XYData");
    GroupingApproximation groupingApproximationX = GroupingApproximation.OPEN;
    GroupingApproximation groupingApproximationY = GroupingApproximation.AVERAGE;

    public XYSeries(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    public XYSeries(int[] xData, int[] yData) {
        dataTable.addColumns(new IntColumn("x", xData));
        dataTable.addColumns(new IntColumn("y", yData));
    }

    public XYSeries(double startValue, double step, int[] yData) {
        dataTable.addColumns(new RegularColumn("x",startValue, step, yData.length));
        dataTable.addColumns(new IntColumn("y", yData));
    }

    public XYSeries(int[] yData) {
        this(0, 1, yData);
    }

    public DataTable getDataTable() {
        return dataTable;
    }

    public XYSeries getEmptyCopy() {
        DataTable emptyData = new DataTable(dataTable.getName());
        for (int i = 0; i < dataTable.columnCount(); i++) {
            emptyData.addColumns(dataTable.getColumn(i).emptyCopy());
        }
        XYSeries copy = new XYSeries(emptyData);
        copy.groupingApproximationX = groupingApproximationX;
        copy.groupingApproximationY = groupingApproximationY;
        return copy;
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

    public double getX(int index) {
        return dataTable.value(index, 0);
    }

    public double getY(int index) {
        return dataTable.value(index, 1);
    }

    public int bisectLeft(double value) {
        return dataTable.bisectLeft(0, value);
    }

    public int bisectRight(double value) {
        return dataTable.bisectRight(0, value);
    }

    public void appendData(XYSeries dataToAppend) {
        dataTable.append(dataToAppend.dataTable);
    }

    public XYSeries view(int from, int length) {
        XYSeries view = new XYSeries(dataTable.view(from, length));
        view.groupingApproximationX = groupingApproximationX;
        view.groupingApproximationY = groupingApproximationY;
        return view;
    }

    @Override
    public int size() {
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
    @Override
    public int[] sortedIndices(int columnIndex) {
        return dataTable.sortedIndices(columnIndex);
    }

    @Override
    public int bisect(int columnIndex, double value, int[] sorter) {
        return dataTable.bisect(columnIndex, value, sorter);
    }



}
