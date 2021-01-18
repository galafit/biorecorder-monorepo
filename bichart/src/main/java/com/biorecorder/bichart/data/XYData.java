package com.biorecorder.bichart.data;

import com.biorecorder.bichart.graphics.Range;
import com.biorecorder.data.frame.*;
import com.biorecorder.data.sequence.*;

import java.util.List;

/**
 * Created by galafit on 21/1/19.
 */
public class XYData implements ChartData {
    private DataFrame dataFrame;
    private String xColumnName = "x";

    private XYData(DataFrame dataFrame) {
        this.dataFrame = dataFrame;
    }

    public XYData(double xStart, double xStep, boolean isDataAppendMode) {
        dataFrame = new DataFrame(isDataAppendMode);
        dataFrame.addColumn(xColumnName, xStart, xStep);
    }

    public XYData(double xStart, double xStep, int length) {
        dataFrame = new DataFrame(false);
        dataFrame.addColumn(xColumnName, xStart, xStep);
    }

    public XYData(List<String> xData, boolean isDataAppendMode) {
        dataFrame = new DataFrame(isDataAppendMode);
        dataFrame.addColumn(xColumnName, xData);
    }

    public XYData(ShortSequence xData, boolean isDataAppendMode) {
        dataFrame = new DataFrame(isDataAppendMode);
        dataFrame.addColumn(xColumnName, xData);

    }

    public XYData(short[] xData) {
        dataFrame = new DataFrame(false);
        dataFrame.addColumn(xColumnName, xData);
    }

    public XYData(IntSequence xData, boolean isDataAppendMode) {
        dataFrame = new DataFrame(isDataAppendMode);
        dataFrame.addColumn(xColumnName, xData);
    }

    public XYData(int[] xData) {
        dataFrame = new DataFrame(false);
        dataFrame.addColumn(xColumnName, xData);
    }

    public XYData(LongSequence xData, boolean isDataAppendMode) {
        dataFrame = new DataFrame(isDataAppendMode);
        dataFrame.addColumn(xColumnName, xData);
    }

    public XYData(long[] xData) {
        dataFrame = new DataFrame(false);
        dataFrame.addColumn(xColumnName, xData);
    }

    public XYData(FloatSequence xData, boolean isDataAppendMode) {
        dataFrame = new DataFrame(isDataAppendMode);
        dataFrame.addColumn(xColumnName, xData);
    }

    public XYData(float[] xData) {
        dataFrame = new DataFrame(false);
        dataFrame.addColumn(xColumnName, xData);
    }

    public XYData(DoubleSequence xData, boolean isDataAppendMode) {
        dataFrame = new DataFrame(isDataAppendMode);
        dataFrame.addColumn(xColumnName, xData);
    }

    public XYData(double[] xData) {
        dataFrame = new DataFrame(false);
        dataFrame.addColumn(xColumnName, xData);
    }

    public void addYColumn(String name, Function function) {
        dataFrame.addColumn(name, function, 0);
    }

    public void addYColumn(String name, ShortSequence yData) {
        dataFrame.addColumn(name, yData);
    }

    public void addYColumn(String name, short[] yData) {
        dataFrame.addColumn(name, yData);
    }

    public void addYColumn(String name, IntSequence yData) {
        dataFrame.addColumn(name, yData);
    }

    public void addYColumn(String name, int[] yData) {
        dataFrame.addColumn(name, yData);
    }

    public void addYColumn(String name, LongSequence yData) {
        dataFrame.addColumn(name, yData);
    }

    public void addYColumn(String name, long[] yData) {
        dataFrame.addColumn(name, yData);
    }

    public void addYColumn(String name, FloatSequence yData) {
        dataFrame.addColumn(name, yData);
    }

    public void addYColumn(String name, float[] yData) {
        dataFrame.addColumn(name, yData);
    }

    public void addYColumn(String name, DoubleSequence yData) {
        dataFrame.addColumn(name, yData);
    }

    public void addYColumn(String name, double[] yData) {
        dataFrame.addColumn(name, yData);
    }

    public void setColumnName(int columnNumber, String columnName) {
        dataFrame.setColumnName(columnNumber, columnName);
    }

    private GroupApproximation aggregationsToAproximation(Aggregation[] aggregations) throws IllegalArgumentException {
        switch (aggregations.length) {
            case 0: {
                return null;
            }
            case 1: {
                switch (aggregations[0]) {
                    case FIRST:
                        return GroupApproximation.OPEN;
                    case LAST:
                        return GroupApproximation.CLOSE;
                    case MIN:
                        return GroupApproximation.LOW;
                    case MAX:
                        return GroupApproximation.HIGH;
                    case AVERAGE:
                        return GroupApproximation.AVERAGE;
                    case SUM:
                        return GroupApproximation.SUM;
                }
                break;
            }
            case 2: {
                if(aggregations[0] == Aggregation.MIN
                        && aggregations[1]  == Aggregation.MAX) {
                    return GroupApproximation.RANGE;
                }
                break;
            }
            case 4: {
                if(aggregations[0] == Aggregation.FIRST
                        && aggregations[1]  == Aggregation.MAX
                        && aggregations[2]  == Aggregation.MIN
                        && aggregations[3]  == Aggregation.LAST) {
                    return GroupApproximation.OHLC;
                }
                break;
            }
        }
        StringBuilder errMsg = new StringBuilder("Unsupported Aggregations: ");
        for (Aggregation agg : aggregations) {
            errMsg.append("  " + agg);
        }
        throw new IllegalArgumentException(errMsg.toString());
    }

    private Aggregation[] aproximationToAggregations(GroupApproximation approximation) throws IllegalArgumentException {
        if(approximation == null) {
            return new Aggregation[0];
        }
        switch (approximation) {
            case SUM: {
                Aggregation[] aggregations = {Aggregation.SUM};
                return aggregations;
            }
            case OPEN: {
                Aggregation[] aggregations = {Aggregation.FIRST};
                return aggregations;
            }
            case CLOSE: {
                Aggregation[] aggregations = {Aggregation.LAST};
                return aggregations;
            }
            case AVERAGE: {
                Aggregation[] aggregations = {Aggregation.AVERAGE};
                return aggregations;
            }
            case HIGH: {
                Aggregation[] aggregations = {Aggregation.MAX};
                return aggregations;
            }
            case LOW: {
                Aggregation[] aggregations = {Aggregation.MIN};
                return aggregations;
            }
            case RANGE: {
                Aggregation[] aggregations = {Aggregation.MIN, Aggregation.MAX};
                return aggregations;
            }
            case OHLC: {
                Aggregation[] aggregations = {Aggregation.FIRST, Aggregation.MAX, Aggregation.MIN, Aggregation.LAST};
                return aggregations;
            }
        }
        String errMsg = "Unsupported approximation: " + approximation;
        throw new IllegalArgumentException(errMsg.toString());

    }


    @Override
    public void setColumnGroupApproximation(int columnNumber, GroupApproximation groupApproximation) {
        dataFrame.setColumnAggFunctions(columnNumber, aproximationToAggregations(groupApproximation));
    }

    @Override
    public GroupApproximation getColumnGroupApproximation(int columnNumber) {
        return aggregationsToAproximation(dataFrame.getColumnAggFunctions(columnNumber));
    }

    @Override
    public boolean isDataAppendMode() {
        return dataFrame.isDataAppendMode();
    }

    @Override
    public int rowCount() {
        return dataFrame.rowCount();
    }

    @Override
    public int columnCount() {
        return dataFrame.columnCount();
    }

    @Override
    public String getColumnName(int columnNumber) {
        return dataFrame.getColumnName(columnNumber);
    }

    @Override
    public boolean isNumberColumn(int columnNumber) {
        return dataFrame.columnType(columnNumber).isNumber();
    }

    @Override
    public boolean isRegular() {
        return dataFrame.isColumnRegular(0);
    }

    @Override
    public boolean isIncreasing() {
        Stats stats = dataFrame.stats(0);
        if (stats == null) {
            return true;
        }
        return dataFrame.stats(0).isIncreasing();
    }

    @Override
    public int[] sortedIndices(int sortColumn) {
        return dataFrame.sortedIndices(sortColumn);
    }

    @Override
    public double value(int rowNumber, int columnNumber) {
        return dataFrame.value(rowNumber, columnNumber);
    }

    @Override
    public String label(int rowNumber, int columnNumber) {
        return dataFrame.label(rowNumber, columnNumber);
    }

    @Override
    public Range columnMinMax(int columnNumber) {
        Stats stats = dataFrame.stats(columnNumber);
        if (stats == null) {
            return null;
        }
        return new Range(stats.min(), stats.max());
    }

    @Override
    public int bisect(double value, int[] sorter) {
        return dataFrame.bisect(0, value, sorter);
    }

    @Override
    public ChartData slice(int fromRowNumber, int length) {
        return new XYData(dataFrame.slice(fromRowNumber, length));
    }

    @Override
    public ChartData slice(int fromRowNumber) {
        return new XYData(dataFrame.slice(fromRowNumber));
    }


    @Override
    public ChartData concat(ChartData data) {
        if(data instanceof XYData) {
            return new XYData(dataFrame.concat(((XYData)data).dataFrame));
        }
        throw new IllegalArgumentException("XYData can be concatenated only with XYData");
    }

    @Override
    public ChartData view(int fromRowNumber) {
        return new XYData(dataFrame.view(fromRowNumber));
    }


    @Override
    public ChartData view(int fromRowNumber, int length) {
        return new XYData(dataFrame.view(fromRowNumber, length));
    }

    @Override
    public ChartData resampleByEqualPointsNumber(int points) {
        return new XYData(dataFrame.resampleByEqualPointsNumber(points, true));
    }

    @Override
    public ChartData resampleByEqualInterval(int columnNumber, double interval) {
        return new XYData(dataFrame.resampleByEqualInterval(columnNumber, interval, true));
    }

    @Override
    public ChartData resampleByEqualTimeInterval(int columnNumber, TimeInterval timeInterval) {
        return new XYData(dataFrame.resampleByEqualTimeInterval(columnNumber, timeInterval, true));
    }

    @Override
    public void appendData() {
        dataFrame.appendData();
    }

}
