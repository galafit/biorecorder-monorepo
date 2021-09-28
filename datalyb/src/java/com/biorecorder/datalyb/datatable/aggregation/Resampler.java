package com.biorecorder.datalyb.datatable.aggregation;
import com.biorecorder.datalyb.datatable.*;
import com.biorecorder.datalyb.series.IntSeries;
import com.biorecorder.datalyb.time.TimeInterval;
import com.biorecorder.datalyb.list.IntArrayList;

import java.util.HashMap;
import java.util.Map;
/**
 * Data grouping or binning (banding)
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
 * Implementation implies that the data is sorted!!!
 */
public class Resampler {
    private  Map<Integer, Aggregation[]> columnsToAgg = new HashMap<>();
    private DataTable resultantTable;
    private AggFunction defaultAggFunction = new First();
    private Binning binning;


    private Resampler(Binning binning) {
        this.binning = binning;
    }

    public DataTable resultantData() {
        return resultantTable;
    }

    public static Resampler createEqualPointsResampler(int pointsInGroup) {
        Binning binning = new EqualPointsBinning(pointsInGroup);
        return new Resampler(binning);
    }

    public static Resampler createEqualIntervalResampler(double interval) {
        Binning binning = new EqualIntervalBinning(new DoubleIntervalProvider(interval));
        return new Resampler(binning);
    }

    public static Resampler createEqualTimeIntervalResampler(TimeInterval timeInterval) {
        Binning binning = new EqualIntervalBinning(new TimeIntervalProvider(timeInterval));
        return new Resampler(binning);
    }

    public void setColumnAggregations(int column, AggFunction... aggFunctions) {
        Aggregation[] aggregations = new Aggregation[aggFunctions.length];
        for (int i = 0; i < aggregations.length; i++) {
            aggregations[i] = new Aggregation(aggFunctions[i]);
        }

        columnsToAgg.put(column, aggregations);
    }

    public DataTable resampleAndAppend(DataTable tableToResample) {
        resultantTable = new DataTable(tableToResample.getName());
        IntSeries groups = binning.group(tableToResample.getColumn(0));
        for (int i = 0; i < tableToResample.columnCount(); i++) {
           Aggregation[] aggregations = columnsToAgg.get(i);
           if(aggregations == null) {
               aggregations = new Aggregation[1];
               aggregations[0] = new Aggregation(defaultAggFunction);
               columnsToAgg.put(i, aggregations);
           }
            for (int j = 0; j < aggregations.length; j++) {
                Column col = tableToResample.getColumn(i);
                if(binning.isEqualPoints() && col instanceof RegularColumn) {
                    RegularColumn rc = (RegularColumn) col;
                    resultantTable.addColumns(aggregations[j].aggregate(rc, binning.pointsInGroup()));
                } else {
                    resultantTable.addColumns(aggregations[j].aggregate(col, groups));
                }
            }
        }
        return resultantTable;
    }

    static class EqualIntervalBinning implements Binning {
        private IntervalProvider intervalProvider;
        private Interval currentGroupInterval;
        public EqualIntervalBinning(IntervalProvider intervalProvider) {
            this.intervalProvider = intervalProvider;
        }

        @Override
        public boolean isEqualPoints() {
            return false;
        }

        @Override
        public int pointsInGroup() {
            return 0;
        }

        @Override
        public IntSeries group(Column column) {
            IntArrayList groupStarts = new IntArrayList();
            if (column.size() > 0) {
                if (currentGroupInterval == null) {
                    currentGroupInterval = intervalProvider.getContaining(column.value(0));
                }
                for (int i = 0; i < column.size(); i++) {
                    double data = column.value(i);
                    if (!currentGroupInterval.contains(data)) {
                        groupStarts.add(i);
                        currentGroupInterval = intervalProvider.getNext(); // main scenario
                        if (!currentGroupInterval.contains(data)) { // rare situation
                            currentGroupInterval = intervalProvider.getContaining(data);
                        }
                    }
                }
            }
            IntSeries groupStartsSeq = new IntSeries() {
                @Override
                public int size() {
                    return groupStarts.size();
                }

                @Override
                public int get(int index) {
                    return groupStarts.get(index);
                }
            };
            return groupStartsSeq;
        }
    }

    static class EqualPointsBinning implements Binning {
        private int pointsInGroup;
        private int pointsAdded;

        public EqualPointsBinning(int points) {
            pointsInGroup = points;
        }

        @Override
        public boolean isEqualPoints() {
            return true;
        }

        @Override
        public int pointsInGroup() {
            return pointsInGroup;
        }

        @Override
        public IntSeries group(Column column) {
            final int firstGroupPoints = pointsInGroup - pointsAdded;
            final int numberOfGroups;
            if(firstGroupPoints > column.size()) {
                pointsAdded += column.size();
                numberOfGroups = 0;
            } else {
                int groups = 1;
                groups += (column.size() - firstGroupPoints) / pointsInGroup;
                int lastGroupPoints = (column.size() - firstGroupPoints) % pointsInGroup;
                if(lastGroupPoints == 0) {
                    groups--;
                    lastGroupPoints = pointsInGroup;
                }
                numberOfGroups = groups;
                pointsAdded = lastGroupPoints;
            }
            return new IntSeries() {
                @Override
                public int size() {
                    return numberOfGroups;
                }

                @Override
                public int get(int index) {
                    if (index == 0) {
                        return firstGroupPoints;
                    } else {
                        return firstGroupPoints + pointsInGroup * index;
                    }
                }
            };
        }
    }

    public static void main(String[] args) {
        DataTable dt = new DataTable("test table");
        int[] xData = {2, 4, 5, 9, 12, 33, 34, 35, 40};
        int[] yData = {1, 2, 3, 4, 5, 6, 7, 8, 9};

        dt.addColumns(new IntColumn("x", xData));
        dt.addColumns(new IntColumn("y", yData));

        System.out.println("DataTable size + " + dt.rowCount() + " expected: " + xData.length);
        System.out.println("DataTable max + " + dt.minMax(0)[1] + " expected: " + xData[xData.length - 1]);


        Resampler aggPoints = Resampler.createEqualPointsResampler(4);
        aggPoints.setColumnAggregations(0, new First());
        aggPoints.setColumnAggregations(1, new Average());

        Resampler aggInterval = Resampler.createEqualIntervalResampler(4);
        aggInterval.setColumnAggregations(0, new First());
        aggInterval.setColumnAggregations(1, new Average());

        DataTable rt1 = aggPoints.resampleAndAppend(dt);

        int[] expectedX1 = {2, 12, 40};
        int[] expectedY1 = {2, 6, 9};

        int[] expectedX2 = {2, 4, 9, 12, 33, 40};
        int[] expectedY2 = {1, 2, 4, 5, 7, 9};

        System.out.println("\nresultant table size "+ rt1.rowCount());
        for (int i = 0; i < rt1.rowCount(); i++) {
             if (rt1.value(i, 0) != expectedX1[i]) {
                String errMsg = "0: ResampleByEqualFrequency error: " + i + " expected x =  " + expectedX1[i] + "  resultant x = " + rt1.value(i, 0);
                throw new RuntimeException(errMsg);
            }
            if (rt1.value(i, 1) != expectedY1[i]) {
                String errMsg = "1: ResampleByEqualFrequency error: " + i + " expected y =  " + expectedY1[i] + "  resultant y = " + rt1.value(i, 1);
                throw new RuntimeException(errMsg);
            }
        }
        System.out.println("ResampleByEqualFrequency is OK ");

        DataTable rt2 = aggInterval.resampleAndAppend(dt);
        System.out.println("\nresultant table size "+ rt2.rowCount());
        for (int i = 0; i < rt2.rowCount(); i++) {
            if (rt2.value(i, 0) != expectedX2[i]) {
                String errMsg = "ResampleByEqualInterval error: " + i + " expected x =  " + expectedX2[i] + "  resultant x = " + rt2.value(i, 0);
                throw new RuntimeException(errMsg);
            }
            if (rt2.value(i, 1) != expectedY2[i]) {
                String errMsg = "ResampleByEqualInterval error: " + i + " expected y =  " + expectedY2[i] + "  resultant y = " + rt2.value(i, 1);
                throw new RuntimeException(errMsg);
            }
        }
        System.out.println("ResampleByEqualInterval is OK");

        int size = 130;
        int[] data = new int[size];
        for (int i = 0; i < size; i++) {
            data[i] = i;
        }

        DataTable dataTable = new DataTable("XYData");
        dataTable.addColumns(new IntColumn("x", data));
        dataTable.addColumns(new IntColumn("y", data));
        Resampler aggPoints1 = Resampler.createEqualPointsResampler(5);
        aggPoints1.setColumnAggregations(0, new Min());
        aggPoints1.setColumnAggregations(1, new Max());

        Resampler aggInterval1 = Resampler.createEqualIntervalResampler(5);
        aggInterval1.setColumnAggregations(0, new Min());
        aggInterval1.setColumnAggregations(1, new Max());

        DataTable resTable1 = aggPoints1.resampleAndAppend(dataTable);
        DataTable resTable2 = aggInterval1.resampleAndAppend(dataTable);

        System.out.println("\nresultant table sizes "+ resTable1.rowCount() + " "+ resTable2.rowCount());
        for (int i = 0; i < resTable2.rowCount(); i++) {
            if (resTable2.value(i, 0) != resTable1.value(i, 0)) {
                String errMsg = "column 0:  resampling by interval and points are not equal" + i;
                throw new RuntimeException(errMsg);
            }
            if (resTable2.value(i, 1) != resTable1.value(i, 1)) {
                String errMsg = "column 1:  resampling by interval and points are not equal" + i;
                throw new RuntimeException(errMsg);
            }
        }
        System.out.println("Resampling by interval and points are equal. Test done!");


        Resampler aggPoints2 = Resampler.createEqualPointsResampler(5);
        aggPoints2.setColumnAggregations(0, new Min());
        aggPoints2.setColumnAggregations(1, new Max());

        Resampler aggInterval2 = Resampler.createEqualIntervalResampler(5);
        aggInterval2.setColumnAggregations(0, new Min());
        aggInterval2.setColumnAggregations(1, new Max());

        DataTable resTab1 = new DataTable("ResTable1");
        DataTable resTab2 = new DataTable("ResTable2");


        size = 13;
        double[] data1 = new double[size];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < size; j++) {
                data1[j] = i * size + j;
            }
            DataTable dataTable1 = new DataTable("ResTable");
            dataTable1.addColumns(new RegularColumn("",size * i, 1, size));
            //dataTable1.addColumn(new DoubleColumn("x", data1));
            dataTable1.addColumns(new DoubleColumn("y", data1));
            resTab1 = aggPoints2.resampleAndAppend(dataTable1);
            resTab2 = aggInterval2.resampleAndAppend(dataTable1);
        }

        System.out.println("\nresultant table sizes "+ resTab1.rowCount() + " "+ resTab2.rowCount());
        for (int i = 0; i < resTab1.rowCount(); i++) {
            if (resTab1.value(i, 0) != resTab2.value(i, 0)) {
                String errMsg = "column 0:  multiple resampling by interval and points are not equal: " + i;
                throw new RuntimeException(errMsg);
            }
            if (resTab1.value(i, 1) != resTab2.value(i, 1)) {
                String errMsg = "column 1:  multiple resampling by interval and points are not equal: " + i;
                throw new RuntimeException(errMsg);
            }
        }
        System.out.println("Multiple resampling by points and intervals are equal. Test done!");


        for (int i = 0; i < resTable1.rowCount(); i++) {
            if (resTable1.value(i, 0) != resTab1.value(i, 0)) {
                String errMsg = "column 0:  one aggregation and multiples are not equal" + i;
                throw new RuntimeException(errMsg);
            }
            if (resTable1.value(i, 1) != resTab1.value(i, 1)) {
                String errMsg = "column 1:  one aggregation and multiples are not equal" + i;
                throw new RuntimeException(errMsg);
            }
        }

        System.out.println("One aggregation and multiples by points are equal. Test done. Test done!");

        for (int i = 0; i < resTable2.rowCount(); i++) {
            if (resTable2.value(i, 0) != resTab2.value(i, 0)) {
                String errMsg = "column 0:  one aggregation and multiples are not equal: " + i;
                throw new RuntimeException(errMsg);
            }
            if (resTable2.value(i, 1) != resTab2.value(i, 1)) {
                String errMsg = "column 1:  one aggregation and multiples are not equal: " + i;
                throw new RuntimeException(errMsg);
            }
        }
        System.out.println("One aggregation and multiples by interval are equal. Test done");

    }
}
