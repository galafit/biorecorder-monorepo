package com.biorecorder.data.frame;

import com.biorecorder.data.frame.impl.ColumnFactory;
import com.biorecorder.data.list.IntArrayList;
import com.biorecorder.data.sequence.*;

import java.util.*;


/**
 * Simplified analogue of data table which
 * in fact is simply a collection of columns
 */
public class DataFrame {
    private int length;
    protected List<Column> columns = new ArrayList<>();
    protected List<String> columnNames = new ArrayList<>();
    protected List<Aggregation[]> columnAggFunctions = new ArrayList<>();
    private Map<Integer, FunctionColumnInfo> columnNumberToFunctionInfo = new HashMap<>();

    boolean isDataAppendMode = true;

    public DataFrame(boolean isDataAppendMode) {
        this.isDataAppendMode = isDataAppendMode;
    }

    public DataFrame(DataFrame dataFrame, int[] columnOrder) throws IllegalArgumentException {
        length = dataFrame.length;
        isDataAppendMode = dataFrame.isDataAppendMode;
        for (int i = 0; i < columnOrder.length; i++) {
            int originalColumnNumber = columnOrder[i];
            Column columnToAdd = dataFrame.columns.get(originalColumnNumber).view(0);
            FunctionColumnInfo functionColumnInfo = dataFrame.columnNumberToFunctionInfo.get(originalColumnNumber);
            if (functionColumnInfo != null) {
                // check that columnOrder contains the column used by this one
                int argColumnNumber = -1;
                for (int j = 0; j < columnOrder.length; j++) {
                    if (functionColumnInfo.argColumnNumber == columnOrder[j]) {
                        argColumnNumber = j;
                        break;
                    }
                }
                if (argColumnNumber < 0) {
                    String errMsg = "Column: " + originalColumnNumber +
                            " can not be added because it depends upon column that is not presented in the given columnOrder";
                    throw new IllegalArgumentException(errMsg);
                }
                columnNumberToFunctionInfo.put(i, new FunctionColumnInfo(functionColumnInfo.function, argColumnNumber));
            }
            columns.add(columnToAdd);
            columnNames.add(dataFrame.columnNames.get(originalColumnNumber));
            columnAggFunctions.add(dataFrame.columnAggFunctions.get(originalColumnNumber));
        }
    }

    public void removeColumn(int columnNumber) throws IllegalArgumentException {
        for (Integer key : columnNumberToFunctionInfo.keySet()) {
            FunctionColumnInfo functionColumnInfo = columnNumberToFunctionInfo.get(key);
            if (functionColumnInfo.argColumnNumber == columnNumber) {
                String errMsg = "Column: " + columnNumber + " is used by function column: " + key + " and can not be removed";
                throw new IllegalArgumentException(errMsg);
            }
        }
        if (columnNumberToFunctionInfo.get(columnNumber) != null) {
            columnNumberToFunctionInfo.remove(columnNumber);
        }
        columns.remove(columnNumber);
        columnNames.remove(columnNumber);
        columnAggFunctions.remove(columnNumber);
        appendData();
    }

    private void addColumn(String name, Column column) {
        columns.add(column);
        columnNames.add(name);
        Aggregation[] agg = new Aggregation[0];
        columnAggFunctions.add(agg);
        appendData();
    }

    public void addColumn(String name, Function function, int argColumnNumber) {
        if (columns.get(argColumnNumber).dataType() == DataType.String) {
            String errMsg = "Function column may not depend upon String column";
            throw new IllegalArgumentException(errMsg);

        }
        for (Integer key : columnNumberToFunctionInfo.keySet()) {
            if (key == argColumnNumber) {
                String errMsg = "Column: " + argColumnNumber + " is a function column and may not be used as argument for another function column";
                throw new IllegalArgumentException(errMsg);
            }
        }
        columnNumberToFunctionInfo.put(columns.size(), new FunctionColumnInfo(function, argColumnNumber));
        addColumn(name, ColumnFactory.createColumn(function, columns.get(argColumnNumber)));
    }

    public void addColumn(String name, double start, double step) {
        addColumn(name, ColumnFactory.createColumn(start, step));
    }

    public void addColumn(String name, double start, double step, int size) {
        addColumn(name, ColumnFactory.createColumn(start, step, size));
    }

    public void addColumn(String name, ShortSequence data) {
        addColumn(name, ColumnFactory.createColumn(data));
    }

    public void addColumn(String name, short[] data) {
        addColumn(name, new ShortSequence() {
            @Override
            public int size() {
                return data.length;
            }

            @Override
            public short get(int index) {
                return data[index];
            }
        });
    }

    public void addColumn(String name, IntSequence data) {
        addColumn(name, ColumnFactory.createColumn(data));
    }

    public void addColumn(String name, int[] data) {
        addColumn(name, new IntSequence() {
            @Override
            public int size() {
                return data.length;
            }

            @Override
            public int get(int index) {
                return data[index];
            }
        });
    }

    public void addColumn(String name, LongSequence data) {
        addColumn(name, ColumnFactory.createColumn(data));
    }

    public void addColumn(String name, long[] data) {
        addColumn(name, new LongSequence() {
            @Override
            public int size() {
                return data.length;
            }

            @Override
            public long get(int index) {
                return data[index];
            }
        });
    }

    public void addColumn(String name, FloatSequence data) {
        addColumn(name, ColumnFactory.createColumn(data));
    }

    public void addColumn(String name, float[] data) {
        addColumn(name, new FloatSequence() {
            @Override
            public int size() {
                return data.length;
            }

            @Override
            public float get(int index) {
                return data[index];
            }
        });
    }

    public void addColumn(String name, DoubleSequence data) {
        addColumn(name, ColumnFactory.createColumn(data));
    }

    public void addColumn(String name, double[] data) {
        addColumn(name, new DoubleSequence() {
            @Override
            public int size() {
                return data.length;
            }

            @Override
            public double get(int index) {
                return data[index];
            }
        });
    }

    public void addColumn(String name, StringSequence data) {
        addColumn(name, ColumnFactory.createColumn(data));
    }

    public void addColumn(String name, List<String> data) {
        addColumn(name, new StringSequence() {
            @Override
            public int size() {
                return data.size();
            }

            @Override
            public String get(int index) {
                return data.get(index);
            }
        });
    }

    public void addColumn(String name, String[] data) {
        addColumn(name, new StringSequence() {
            @Override
            public int size() {
                return data.length;
            }

            @Override
            public String get(int index) {
                return data[index];
            }
        });
    }

    public int rowCount() {
        return length;
    }

    public int columnCount() {
        return columns.size();
    }

    public void setColumnAggFunctions(int columnNumber, Aggregation... aggFunctions) {
        columnAggFunctions.set(columnNumber, Arrays.copyOf(aggFunctions, aggFunctions.length));
    }

    public Aggregation[] getColumnAggFunctions(int columnNumber) {
        Aggregation[] aggregations = columnAggFunctions.get(columnNumber);
        return Arrays.copyOf(aggregations, aggregations.length);
    }


    public String getColumnName(int columnNumber) {
        return columnNames.get(columnNumber);
    }

    public void setColumnName(int columnNumber, String name) {
        columnNames.set(columnNumber, name);
    }

    public DataType columnType(int columnNumber) {
        return columns.get(columnNumber).dataType();
    }

    public double value(int rowNumber, int columnNumber) {
        rangeCheck(rowNumber);
        return columns.get(columnNumber).value(rowNumber);
    }

    public String label(int rowNumber, int columnNumber) {
        rangeCheck(rowNumber);
        return columns.get(columnNumber).label(rowNumber);
    }

    public Stats stats(int columnNumber) {
        if (length < 1) {
            return null;
        }
        return columns.get(columnNumber).stats(length);
    }

    public boolean isColumnRegular(int columnNumber) {
        return columns.get(columnNumber) instanceof RegularColumn;
    }

    public boolean isColumnFunction(int columnNumber) {
        return columnNumberToFunctionInfo.get(columnNumber) != null;
    }

    public boolean isDataAppendMode() {
        return isDataAppendMode;
    }

    /**
     * Binary search algorithm. The column data must be sorted!
     * Find the index of the <b>value</b> in the given column. If the column containsInt
     * multiple elements equal to the searched <b>value</b>, there is no guarantee which
     * one will be found. If there is no element equal to the searched value function returns
     * the insertion point for <b>value</b> in the column to maintain sorted order
     * (i.e. index of the first element in the column which is bigger than the searched value).
     *
     * @param sorter - Default null.
     *               Optional array of integer indices that sortedIndices column data
     *               into ascending order (if data column itself is not sorted).
     *               They are typically the result of {@link #sortedIndices(int)}
     */
    public int bisect(int columnNumber, double value, int[] sorter) {
        Column column = columns.get(columnNumber);
        if (sorter != null) {
            column = column.view(sorter);
        }
        int length1 = length;
        if (sorter != null) {
            length1 = Math.min(length, sorter.length);
        }
        return column.bisect(value, 0, length1);
    }

    public DataFrame concat(DataFrame dataFrame) {
        int cols = Math.min(columns.size(), dataFrame.columns.size());
        DataFrame resultantFrame = new DataFrame(dataFrame.isDataAppendMode);
        for (int i = 0; i < cols; i++) {
            FunctionColumnInfo functionColumnInfo1 = columnNumberToFunctionInfo.get(i);
            FunctionColumnInfo functionColumnInfo2 = dataFrame.columnNumberToFunctionInfo.get(i);
            if (functionColumnInfo1 != null && functionColumnInfo1.equals(functionColumnInfo2)) {
                resultantFrame.columnNumberToFunctionInfo.put(i, functionColumnInfo1);
                resultantFrame.columns.add(null);
            } else {
                resultantFrame.columns.add(ColumnFactory.concat(columns.get(i), length, dataFrame.columns.get(i)));
            }
            resultantFrame.columnNames.add(columnNames.get(i));
            resultantFrame.columnAggFunctions.add(columnAggFunctions.get(i));
        }
        for (Integer key : resultantFrame.columnNumberToFunctionInfo.keySet()) {
            // create and put new Function columns
            FunctionColumnInfo functionColumnInfo = resultantFrame.columnNumberToFunctionInfo.get(key);
            Column functionColumn = ColumnFactory.createColumn(functionColumnInfo.function, resultantFrame.columns.get(functionColumnInfo.argColumnNumber));
            resultantFrame.columns.set(key, functionColumn);
        }
        resultantFrame.appendData();

        return resultantFrame;
    }


    /**
     * This method returns a sorted view of the data frame
     * without modifying the order of the underlying data.
     * (like JTable sortedIndices in java)
     */
    public DataFrame sort(int sortColumn) {
        return view(sortedIndices(sortColumn));
    }

    /**
     * This method returns an array of row numbers (indices)
     * which represent sorted version (view) of the given column.
     * (Similar to numpy.argsort or google chart DataTable.getSortedRows -
     * https://developers.google.com/chart/interactive/docs/reference#DataTable,)
     *
     * @return array of sorted rows (indices) for the given column.
     */
    public int[] sortedIndices(int sortColumn) {
        boolean isParallel = false;
        return columns.get(sortColumn).sort(0, length, isParallel);
    }

    public DataFrame slice(int fromRowNumber, int length) {
        return slice1(fromRowNumber, length);
    }

    public DataFrame slice(int fromRowNumber) {
        return slice1(fromRowNumber, -1);
    }

    private DataFrame slice1(int fromRowNumber, int length) {
        boolean appendMode = isDataAppendMode;
        if(length >= 0) {
            appendMode = false;
        }
        DataFrame resultantFrame = new DataFrame(appendMode);
        for (int i = 0; i < columns.size(); i++) {
            FunctionColumnInfo functionColumnInfo = columnNumberToFunctionInfo.get(i);
            if (functionColumnInfo != null) { // if function column we temporary add null
                resultantFrame.columns.add(null);
            } else {
                if(length >= 0) {
                    resultantFrame.columns.add(columns.get(i).slice(fromRowNumber, length));
                } else {
                    resultantFrame.columns.add(columns.get(i).slice(fromRowNumber));
                }
             }
            resultantFrame.columnNames.add(columnNames.get(i));
            resultantFrame.columnAggFunctions.add(columnAggFunctions.get(i));
        }
        for (Integer key : columnNumberToFunctionInfo.keySet()) {
            // create and put new Function columns
            FunctionColumnInfo functionColumnInfo = columnNumberToFunctionInfo.get(key);
            Column functionColumn = ColumnFactory.createColumn(functionColumnInfo.function, resultantFrame.columns.get(functionColumnInfo.argColumnNumber));
            resultantFrame.columns.set(key, functionColumn);
            resultantFrame.columnNumberToFunctionInfo.put(key, columnNumberToFunctionInfo.get(key));
        }
        resultantFrame.appendData();
        return resultantFrame;
    }

    private DataFrame view1(int fromRowNumber, int length) {
        boolean appendMode = isDataAppendMode;
        if(length >= 0) {
            appendMode = false;
        }
        DataFrame resultantFrame = new DataFrame(appendMode);
        for (int i = 0; i < columns.size(); i++) {
            FunctionColumnInfo functionColumnInfo = columnNumberToFunctionInfo.get(i);
            if (functionColumnInfo != null) { // if function column we temporary add null
                resultantFrame.columns.add(null);
            } else {
                if(length >= 0) {
                    resultantFrame.columns.add(columns.get(i).view(fromRowNumber, length));
                } else{
                    resultantFrame.columns.add(columns.get(i).view(fromRowNumber));
                }
            }
            resultantFrame.columnNames.add(columnNames.get(i));
            resultantFrame.columnAggFunctions.add(columnAggFunctions.get(i));
        }
        for (Integer key : columnNumberToFunctionInfo.keySet()) {
            // create and put new Function columns
            FunctionColumnInfo functionColumnInfo = columnNumberToFunctionInfo.get(key);
            Column functionColumn = ColumnFactory.createColumn(functionColumnInfo.function, resultantFrame.columns.get(functionColumnInfo.argColumnNumber));
            resultantFrame.columns.set(key, functionColumn);
            resultantFrame.columnNumberToFunctionInfo.put(key, columnNumberToFunctionInfo.get(key));
        }
        resultantFrame.appendData();
        return resultantFrame;
    }

    public DataFrame view(int fromRowNumber, int length) {
        return view1(fromRowNumber, length);
    }

    public DataFrame view(int fromRowNumber) {
        return view1(fromRowNumber, -1);
    }

    public DataFrame view(int[] rowOrder) {
        DataFrame resultantFrame = new DataFrame(false);
        for (int i = 0; i < columns.size(); i++) {
            FunctionColumnInfo functionColumnInfo = columnNumberToFunctionInfo.get(i);
            if (functionColumnInfo != null) { // if function column we temporary add null
                resultantFrame.columns.add(null);
            } else {
                resultantFrame.columns.add(columns.get(i).view(rowOrder));
            }
            resultantFrame.columnNames.add(columnNames.get(i));
            resultantFrame.columnAggFunctions.add(columnAggFunctions.get(i));
        }
        for (Integer key : columnNumberToFunctionInfo.keySet()) {
            // create and put new Function columns
            FunctionColumnInfo functionColumnInfo = columnNumberToFunctionInfo.get(key);
            Column functionColumn = ColumnFactory.createColumn(functionColumnInfo.function, resultantFrame.columns.get(functionColumnInfo.argColumnNumber));
            resultantFrame.columns.set(key, functionColumn);
            resultantFrame.columnNumberToFunctionInfo.put(key, columnNumberToFunctionInfo.get(key));
        }
        resultantFrame.appendData();
        return resultantFrame;
    }

    /**
     * Resample means data grouping or binning (banding)
     * with subsequent aggregation to reduce large number of data.
     * <p>
     * Binning is a way to group a number of more or less continuous values
     * into a smaller number of buckets (bins or groups).  Each group/bucket/bin defines
     * an numerical unitMultiplier and usually is characterized by a traceName and two boundaries -
     * the intervalStart or lower boundary and the stop or upper one.
     * <p>
     * On the chart  every bin is represented by one value (point).
     * It may be the number of element in the bin (for histogram)
     * or the midpoint of the bin unitMultiplier (avg) and so on.
     * How we will calculateStats the "value" of each bin is specified by the aggregating function
     * (sum, average, unitMultiplier, min, first, last...)
     * <p>
     * The most common "default" methods to divide data into bins:
     * <ol>
     * <li>Equal intervals [equal width binning] - each bin has equal range value or lengths. </li>
     * <li>Equal frequencies [equal height binning, quantiles] - each bin has equal number of elements or data points.
     * Percentile ranks - % of the total data to group into bins, or  the number of points in bins are specified. </li>
     * <li>Custom Edges - edge values of each bin are specified. The edge value is always the lower boundary of the bin.</li>
     * <li>Custom Elements [list] - the elements for each bin are specified manually.</li>
     * </ol>
     * <p>
     * <a href="https://msdn.microsoft.com/library/en-us/Dn913065.aspx">MSDN: Group Data into Bins</a>,
     * <a href="https://gerardnico.com/wiki/data_mining/discretization">Discretizing and binning</a>,
     * <a href="https://docs.rapidminer.com/studio/operators/cleansing/binning/discretize_by_bins.html">discretize by bins</a>,
     * <a href="http://www.ncgia.ucsb.edu/cctp/units/unit47/html/comp_class.html">Data Classification</a>,
     * <a href="https://www.ibm.com/support/knowledgecenter/en/SSLVMB_24.0.0/spss/base/idh_webhelp_scatter_options_palette.html">Binning (Grouping) Data Values</a>,
     * <a href="http://www.jdatalab.com/data_science_and_data_mining/2017/01/30/data-binning-plot.html">Data Binning and Plotting</a>,
     * <a href="https://docs.tibco.com/pub/sfire-bauthor/7.6.0/doc/html/en-US/GUID-D82F7907-B3B4-45F6-AFDA-C3179361F455.html">Binning functions</a>,
     * <a href="https://devnet.logianalytics.com/rdPage.aspx?rdReport=Article&dnDocID=6029">Data Binning</a>,
     * <a href="http://www.cs.wustl.edu/~zhang/teaching/cs514/Spring11/Data-prep.pdf">Data Preprocessing</a>
     * <p>
     * <p>
     * Implementation of the method implies that the data is sorted!!!
     * <p>
     * Resampling will be done only with columns for which at least
     * one aggregating function is specified!!!
     * If columns has no aggregating functions resultant dataframe will be empty
     */
    public DataFrame resampleByEqualPointsNumber(int points, boolean isResultCachingEnabled) throws IllegalArgumentException {
        if(points < 2) {
            String errMsg = "Points = "+ points;
            throw new IllegalArgumentException(errMsg);
        }

        return resample(null, points, isResultCachingEnabled);
    }

    /**
     * Implementation of the method implies that the data is sorted!!!
     * <p>
     * Resampling will be done only with columns for which at least
     * one aggregating function is specified!!!
     * If columns has no aggregating functions resultant dataframe will be empty
     */
    public DataFrame resampleByEqualInterval(int columnNumber, double interval, boolean isResultCachingEnabled) throws IllegalArgumentException {
        if(interval == 0) {
            String errMsg = "Interval = "+ interval;
            throw new IllegalArgumentException(errMsg);
        }
        IntSequence groupIndexes = columns.get(columnNumber).group(interval, new ColumnsMinSize());
        return resample(groupIndexes, 1, isResultCachingEnabled);
    }

    public DataFrame resampleByEqualTimeInterval(int columnNumber, TimeInterval timeInterval, boolean isResultCachingEnabled) {
        IntSequence groupIndexes = columns.get(columnNumber).group(timeInterval, new ColumnsMinSize());
        return resample(groupIndexes, 1, isResultCachingEnabled);
    }

    private DataFrame resample(IntSequence groupIndexes, int points, boolean isResultCachingEnabled) {
        Map<Integer, int[]> colToResultantCols = new HashMap<>();
        int count = 0;
        for (int i = 0; i < columns.size(); i++) {
            int aggregations;
            FunctionColumnInfo functionColumnInfo = columnNumberToFunctionInfo.get(i);
            if (functionColumnInfo != null) {
                aggregations = columnAggFunctions.get(functionColumnInfo.argColumnNumber).length;
            } else {
                aggregations = columnAggFunctions.get(i).length;
            }

            int[] resultantColumns = new int[aggregations];
            for (int j = 0; j < resultantColumns.length; j++) {
                resultantColumns[j] = count + j;
            }
            colToResultantCols.put(i, resultantColumns);
            count += aggregations;
        }


        DataFrame resultantFrame = new DataFrame(isDataAppendMode);
        // resample all columns except function columns
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            FunctionColumnInfo functionColumnInfo = columnNumberToFunctionInfo.get(i);
            if (functionColumnInfo != null) {
                Aggregation[] aggregations = columnAggFunctions.get(functionColumnInfo.argColumnNumber);
                for (Aggregation aggregation : aggregations) {
                    resultantFrame.columns.add(null);
                    resultantFrame.columnNames.add(columnNames.get(i) + "_" + aggregation.name());
                    Aggregation[] resultantAgg = new Aggregation[0];
                    resultantFrame.columnAggFunctions.add(resultantAgg);
                }
            } else {
                Aggregation[] aggregations = columnAggFunctions.get(i);
                for (Aggregation aggregation : aggregations) {
                    if (groupIndexes != null) {
                        resultantFrame.columns.add(column.resample(aggregation, groupIndexes, isDataAppendMode));
                    } else {
                        resultantFrame.columns.add(column.resample(aggregation, points, isDataAppendMode));
                    }
                    resultantFrame.columnNames.add(columnNames.get(i) + "_" + aggregation.name());
                    Aggregation[] resultantAgg = {aggregation};
                    resultantFrame.columnAggFunctions.add(resultantAgg);
                }
            }
        }


        // put new function columns (as functions on aggregated data)
        for (Integer key : columnNumberToFunctionInfo.keySet()) {
            FunctionColumnInfo functionColumnInfo = columnNumberToFunctionInfo.get(key);
            int[] resultantArgCols = colToResultantCols.get(functionColumnInfo.argColumnNumber);
            int[] resultantFCols = colToResultantCols.get(key);
            for (int i = 0; i < resultantFCols.length; i++) {
                resultantFrame.columns.set(resultantFCols[i], ColumnFactory.createColumn(functionColumnInfo.function, resultantFrame.columns.get(resultantArgCols[i])));
                resultantFrame.columnNumberToFunctionInfo.put(resultantFCols[i], new FunctionColumnInfo(functionColumnInfo.function, resultantArgCols[i]));
            }
        }
        resultantFrame.appendData();
        if(isResultCachingEnabled) {
            return resultantFrame.slice(0);
        } else {
            return resultantFrame;
        }
    }

    public void appendData() {
        if (columns.size() == 0) {
            length = 0;
            return;
        }
        length = columns.get(0).size();
        for (int i = 1; i < columns.size(); i++) {
            length = (Math.min(length, columns.get(i).size()));
        }
    }

    private void rangeCheck(long rowNumber) {
        if (rowNumber >= length || rowNumber < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(rowNumber));
    }

    private String outOfBoundsMsg(long index) {
        return "Index: " + index + ", Size: " + length;
    }

    class ColumnsMinSize implements DynamicSize {
        @Override
        public int size() {
            if(columns.size() == 0) {
                return 0;
            }
            int size = columns.get(0).size();
            for (int i = 1; i < columns.size(); i++) {
                size = (Math.min(size, columns.get(i).size()));
            }
            return size;
        }
    }

    class FunctionColumnInfo {
        private final Function function;
        private final int argColumnNumber;

        public FunctionColumnInfo(Function function, int argColumnNumber) {
            this.function = function;
            this.argColumnNumber = argColumnNumber;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof FunctionColumnInfo)) {
                return false;
            }

            FunctionColumnInfo functionColumnInfo = (FunctionColumnInfo) o;

            return argColumnNumber == functionColumnInfo.argColumnNumber
                    && function.getClass() == functionColumnInfo.function.getClass();
        }
    }

    public static void main(String[] args) {
        DataFrame df = new DataFrame(true);
        int[] xData = {2, 4, 5, 9, 12, 33, 34, 35, 40};
        int[] yData = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        IntArrayList xList = new IntArrayList(xData);
        IntArrayList yList = new IntArrayList(yData);

        df.addColumn("x", xList);
        df.addColumn("y", yList);
        df.setColumnAggFunctions(0, Aggregation.FIRST);
        df.setColumnAggFunctions(1, Aggregation.AVERAGE);

        DataFrame df1 = df.resampleByEqualPointsNumber(4, true);
        DataFrame df2 = df.resampleByEqualInterval(0, 4, true);


        int[] expectedX1 = {2, 12, 40};
        int[] expectedY1 = {2, 6, 9};

        int[] expectedX2 = {2, 4, 9, 12, 33, 40};
        int[] expectedY2 = {1, 2, 4, 5, 7, 9};

        for (int i = 0; i < df1.rowCount(); i++) {
            if (df1.value(i, 0) != expectedX1[i]) {
                String errMsg = "ResampleByEqualFrequency error: " + i + " expected x =  " + expectedX1[i] + "  resultant x = " + df1.value(i, 0);
                throw new RuntimeException(errMsg);
            }
            if (df1.value(i, 1) != expectedY1[i]) {
                String errMsg = "ResampleByEqualFrequency error: " + i + " expected y =  " + expectedY1[i] + "  resultant y = " + df1.value(i, 1);
                throw new RuntimeException(errMsg);
            }
        }

        System.out.println("ResampleByEqualFrequency is OK ");

        for (int i = 0; i < df2.rowCount(); i++) {
            if (df2.value(i, 0) != expectedX2[i]) {
                String errMsg = "ResampleByEqualInterval error: " + i + " expected x =  " + expectedX2[i] + "  resultant x = " + df2.value(i, 0);
                throw new RuntimeException(errMsg);
            }
            if (df2.value(i, 1) != expectedY2[i]) {
                String errMsg = "ResampleByEqualInterval error: " + i + " expected y =  " + expectedY2[i] + "  resultant y = " + df2.value(i, 1);
                throw new RuntimeException(errMsg);
            }
        }
        System.out.println("ResampleByEqualInterval is OK");

        // Test appendData

        xList.add(42);
        xList.add(50);

        yList.add(1);
        yList.add(2);
        df.appendData();
        df1.appendData();
        df2.appendData();

        int[] expectedX1_ = {2, 12, 40};
        int[] expectedY1_ = {2, 6, 4};

        int[] expectedX2_ = {2, 4, 9, 12, 33, 40, 50};
        int[] expectedY2_ = {1, 2, 4, 5, 7, 5, 2};

        for (int i = 0; i < df1.rowCount(); i++) {
            if (df1.value(i, 0) != expectedX1_[i]) {
                String errMsg = "ResampleByEqualFrequency UPDATE error: " + i + " expected x =  " + expectedX1_[i] + "  resultant x = " + df1.value(i, 0);
                throw new RuntimeException(errMsg);
            }
            if (df1.value(i, 1) != expectedY1_[i]) {
                String errMsg = "ResampleByEqualFrequency UPDATE error: " + i + " expected y =  " + expectedY1_[i] + "  resultant y = " + df1.value(i, 1);
                throw new RuntimeException(errMsg);
            }
        }
        System.out.println("ResampleByEqualFrequency UPDATE is OK");

        for (int i = 0; i < df2.rowCount(); i++) {
            if (df2.value(i, 0) != expectedX2_[i]) {
                String errMsg = "ResampleByEqualInterval UPDATE error: " + i + " expected x =  " + expectedX2_[i] + "  resultant x = " + df2.value(i, 0);
                throw new RuntimeException(errMsg);
            }
            if (df2.value(i, 1) != expectedY2_[i]) {
                String errMsg = "ResampleByEqualInterval UPDATE error: " + i + " expected y =  " + expectedY2_[i] + "  resultant y = " + df2.value(i, 1);
                throw new RuntimeException(errMsg);
            }
        }
        System.out.println("ResampleByEqualInterval UPDATE is OK");

        System.out.println("\nString sort test");
        DataFrame sf = new DataFrame(false);
        String[] labels = {"mama", "baba", "papa", "deda"};
        sf.addColumn("labels", labels);
        int[] values = {1, 2, 3, 4, 5};
        sf.addColumn("y", values);

        DataFrame sf1 = sf.sort(0);
        for (int i = 0; i < sf1.rowCount(); i++) {
            System.out.println(sf1.value(i, 0) + "  " + sf1.label(i, 0) + "  " + sf1.value(i, 1));
        }

        System.out.println("\nFunction column tests:");
        df = new DataFrame(false);
        int[] col0 = {1, 3, 5, 7, 9};
        int[] col1 = {8, 6, 4, 2, 0};
        df.addColumn("x", col0);
        df.addColumn("y", col1);
        df.addColumn("function", new Function() {
            @Override
            public double apply(double value) {
                return (int) (value + 1);
            }
        }, 1);

        int[] order = {0, 2};
        try {
            df1 = new DataFrame(df, order);
            throw new RuntimeException("Column order exception test Failed");
        } catch (IllegalArgumentException ex) {
            System.out.println("1) Column order exception test OK");
        }
        int[] order1 = {2, 1};
        df1 = new DataFrame(df, order1);
        df1 = df1.view(1, 3);

        int[] expectedCol0 = {7, 5, 3};
        int[] expectedCol1 = {6, 4, 2};
        for (int i = 0; i < df1.rowCount(); i++) {
            if (df1.value(i, 0) != expectedCol0[i] || df1.value(i, 1) != expectedCol1[i]) {
                throw new RuntimeException(i + " Column Order and view test failed ");
            }
        }
        System.out.println("2) Column Order and view test OK");

        DataFrame sortFr1 = df1.sort(0);
        DataFrame sortFr2 = df1.sort(1);

        for (int i = 0; i < df1.rowCount(); i++) {
            if (sortFr1.value(i, 0) != expectedCol0[expectedCol0.length - 1 - i] || sortFr1.value(i, 1) != expectedCol1[expectedCol1.length - 1 - i]) {
                throw new RuntimeException(i + " Sort test failed ");
            }
        }

        for (int i = 0; i < df1.rowCount(); i++) {
            if (sortFr2.value(i, 0) != expectedCol0[expectedCol0.length - 1 - i] || sortFr2.value(i, 1) != expectedCol1[expectedCol1.length - 1 - i]) {
                throw new RuntimeException(i + " Sort test failed ");
            }
        }
        System.out.println("3) Sort test OK");

        df.setColumnAggFunctions(0, Aggregation.FIRST);
        df.setColumnAggFunctions(1, Aggregation.MIN, Aggregation.MAX);

        DataFrame resampleFr = df.resampleByEqualPointsNumber(2, true);
        int[] expectedCol0_ = {1, 5, 9};
        int[] expectedCol1_ = {6, 2, 0};
        int[] expectedCol2_ = {8, 4, 0};

        for (int i = 0; i < resampleFr.rowCount(); i++) {
            if (resampleFr.value(i, 0) != expectedCol0_[i]
                    || resampleFr.value(i, 1) != expectedCol1_[i]
                    || resampleFr.value(i, 2) != expectedCol2_[i]
                    || resampleFr.value(i, 3) != expectedCol1_[i] + 1
                    || resampleFr.value(i, 4) != expectedCol2_[i] + 1) {
                throw new RuntimeException(i + " Resample test failed ");
            }
        }

        // re-sample on already re-sampled frame
        resampleFr = resampleFr.resampleByEqualPointsNumber(2, true);
        int[] expectedCol0_1 = {1, 9};
        int[] expectedCol1_1 = {2, 0};
        int[] expectedCol2_1 = {8, 0};

        for (int i = 0; i < resampleFr.rowCount(); i++) {
            if (resampleFr.value(i, 0) != expectedCol0_1[i]
                    || resampleFr.value(i, 1) != expectedCol1_1[i]
                    || resampleFr.value(i, 2) != expectedCol2_1[i]
                    || resampleFr.value(i, 3) != expectedCol1_1[i] + 1
                    || resampleFr.value(i, 4) != expectedCol2_1[i] + 1) {
                throw new RuntimeException(i + " Resample test failed ");
            }
        }

        System.out.println("4) Resample test OK");
    }
}
