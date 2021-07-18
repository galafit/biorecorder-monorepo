package com.biorecorder.data.frame_new.aggregation;
import com.biorecorder.data.frame_new.BaseType;
import com.biorecorder.data.frame_new.Column;
import com.biorecorder.data.frame_new.DataTable;
import com.biorecorder.data.frame_new.IntColumn;
import com.biorecorder.data.list.IntArrayList;
import com.biorecorder.data.sequence.IntSequence;

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
public class Aggregator {
    private  Map<Integer, Aggregation[]> columnsToAgg = new HashMap();
    private  DataTable resultantTable;
    private  Aggregation defaultAggregation;
    private  Grouper grouper;

    private Aggregator(Grouper grouper) {
        this.grouper = grouper;
    }

    public static Aggregator createEqualPointsAggregator(int pointsInGroup) {
        Grouper grouper = new EqualPointsGrouper(pointsInGroup);
        return new Aggregator(grouper);
    }

    public static Aggregator createEqualIntervalAggregator(double interval) {
        Grouper grouper = new EqualIntervalGrouper(new DoubleIntervalProvider(interval));
        return new Aggregator(grouper);
    }

    public static Aggregator createEqualTimeIntervalAggregator(TimeUnit timeUnit, int unitMultiplier) {
        Grouper grouper = new EqualIntervalGrouper(new TimeIntervalProvider(timeUnit, unitMultiplier));
        return new Aggregator(grouper);
    }

    public void addColumnAggregations(int column, Aggregation... aggregations) {
        columnsToAgg.put(column, aggregations);
    }

    public DataTable aggregate(DataTable tableToAggregate) {
        if(resultantTable == null) {
            resultantTable = new DataTable(tableToAggregate.name());
            for (int i = 0; i < tableToAggregate.columnCount(); i++) {
                Column col = tableToAggregate.getColumn(i);
                Aggregation[] aggregations = columnsToAgg.get(i);
                for (int j = 0; j < aggregations.length; j++) {
                    resultantTable.addColumn(col.type().create(col.name()+":" + aggregations[j].name()));
                }
            }
        }
        IntSequence groups = grouper.group(tableToAggregate.getColumn(0));
        for (int i = 0; i < tableToAggregate.columnCount(); i++) {
           Aggregation[] aggregations = columnsToAgg.get(i);
            for (int j = 0; j < aggregations.length; j++) {
                aggregateAndAppend(aggregations[j], groups, tableToAggregate.getColumn(i), resultantTable.getColumn(i + j));
            }
        }
        return resultantTable;
    }

    private Column aggregateAndAppend(Aggregation agg, IntSequence groups, Column colToAgg, Column resultantCol) throws IllegalArgumentException {
        int groupCounter = 0;
        int groupStart = colToAgg.size();
        if(groups.size() > 0) {
            groupStart = groups.get(groupCounter);
        }
        if(colToAgg.type().getBaseType() == BaseType.INT) {
            System.out.println("  ");
            for (int i = 0; i < groups.size(); i++) {
               // System.out.println(i+" group start "+groups.get(i));
            }
            IntColumn intColToAgg = (IntColumn) colToAgg;
            IntColumn intResultantCol = (IntColumn) resultantCol;
            for (int i = 0; i < colToAgg.size(); i++) {
                if(i == groupStart) {
                    intResultantCol.append(agg.getInt());
                    agg.reset();
                    if(groupCounter < groups.size()) {
                        groupStart = groups.get(groupCounter);
                    }
                }
                agg.addInt(intColToAgg.intValue(i));
            }
        }
        return resultantCol;
    }

    static class EqualIntervalGrouper implements Grouper {
        private IntervalProvider intervalProvider;
        public EqualIntervalGrouper(IntervalProvider intervalProvider) {
            this.intervalProvider = intervalProvider;
        }

        @Override
        public IntSequence group(Column column) {
            IntArrayList groupStarts = new IntArrayList();
            if (column.size() > 0) {
                Interval currentGroupInterval = intervalProvider.getContaining(column.value(0));
                for (int i = 1; i < column.size(); i++) {
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

            return groupStarts;
        }
    }
    
    static class EqualPointsGrouper  implements Grouper{
        private int pointsInGroup;
        private int pointsAdded;

        public EqualPointsGrouper(int points) {
            pointsInGroup = points;
        }
        @Override
        public IntSequence group(Column column) {
            final int firstGroupPoints = pointsInGroup - pointsAdded;
            final int numberOfGroups;
            if(firstGroupPoints >= column.size()) {
                pointsAdded += column.size();
                numberOfGroups = 0;
            } else {
                int groups = 1;
                groups += (column.size() - firstGroupPoints) / pointsInGroup;
                int lastGroupPoints = (column.size() - firstGroupPoints) % pointsInGroup;
                if(lastGroupPoints == 0) {
                    groups--;
                }
                numberOfGroups = groups;
                pointsAdded = lastGroupPoints;
            }
            return new IntSequence() {
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

        dt.addColumn(new IntColumn("x", xData));
        dt.addColumn(new IntColumn("y", yData));
        Aggregator aggPoints = Aggregator.createEqualPointsAggregator(4);
        aggPoints.addColumnAggregations(0, new First());
        aggPoints.addColumnAggregations(1, new Average());

        Aggregator aggInterval = Aggregator.createEqualIntervalAggregator(4);
        aggInterval.addColumnAggregations(0, new First());
        aggInterval.addColumnAggregations(1, new Average());

        DataTable rt1 = aggPoints.aggregate(dt);

        int[] expectedX1 = {2, 12, 40};
        int[] expectedY1 = {2, 6, 9};

        int[] expectedX2 = {2, 4, 9, 12, 33, 40};
        int[] expectedY2 = {1, 2, 4, 5, 7, 9};

        for (int i = 0; i < rt1.rowCount(); i++) {
            if (rt1.value(i, 0) != expectedX1[i]) {
                String errMsg = "ResampleByEqualFrequency error: " + i + " expected x =  " + expectedX1[i] + "  resultant x = " + rt1.value(i, 0);
                throw new RuntimeException(errMsg);
            }
            if (rt1.value(i, 1) != expectedY1[i]) {
                String errMsg = "ResampleByEqualFrequency error: " + i + " expected y =  " + expectedY1[i] + "  resultant y = " + rt1.value(i, 1);
                throw new RuntimeException(errMsg);
            }
        }

        System.out.println("ResampleByEqualFrequency is OK ");

        DataTable rt2 = aggInterval.aggregate(dt);
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
        int[] xData1 = new int[size];
        int[] yData1 = new int[size];
        for (int i = 0; i < size; i++) {
            xData1[i] = i;
            yData1[i] = i;
        }

        DataTable dataTable = new DataTable();
        dataTable.addColumn(new IntColumn("x", xData1));
        dataTable.addColumn(new IntColumn("y", yData1));
        Aggregator aggPoints1 = Aggregator.createEqualPointsAggregator(5);
        aggPoints1.addColumnAggregations(0, new Min());
        aggPoints1.addColumnAggregations(1, new Max());

        Aggregator aggInterval1 = Aggregator.createEqualIntervalAggregator(5);
        aggInterval1.addColumnAggregations(0, new Min());
        aggInterval1.addColumnAggregations(1, new Max());

        DataTable resTable1 = aggPoints1.aggregate(dataTable);
        DataTable resTable2 = aggInterval1.aggregate(dataTable);
        for (int i = 0; i < resTable1.rowCount(); i++) {
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


        Aggregator aggPoints2 = Aggregator.createEqualPointsAggregator(5);
        aggPoints2.addColumnAggregations(0, new Min());
        aggPoints2.addColumnAggregations(1, new Max());

        Aggregator aggInterval2 = Aggregator.createEqualIntervalAggregator(5);
        aggInterval2.addColumnAggregations(0, new Min());
        aggInterval2.addColumnAggregations(1, new Max());

        DataTable resTab1 = new DataTable();
        DataTable resTab2 = new DataTable();

        size = 13;
        int[] xData2 = new int[size];
        int[] yData2 = new int[size];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < size; j++) {
                xData2[j] = i * size + j;
                yData2[j] = i * size + j;
            }
            DataTable dataTable1 = new DataTable();
            dataTable1.addColumn(new IntColumn("x", xData2));
            dataTable1.addColumn(new IntColumn("y", yData2));
            resTab1 = aggPoints2.aggregate(dataTable1);
            resTab2 = aggInterval2.aggregate(dataTable1);
        }

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

        for (int i = 0; i < resTab1.rowCount(); i++) {
            if (resTab2.value(i, 0) != resTab1.value(i, 0)) {
                String errMsg = "column 0:  multiple resampling by interval and points are not equal" + i;
                throw new RuntimeException(errMsg);
            }
            if (resTab2.value(i, 1) != resTab1.value(i, 1)) {
                String errMsg = "column 1:  multiple resampling by interval and points are not equal" + i;
                throw new RuntimeException(errMsg);
            }
        }
        System.out.println("Multiple resampling by interval and points are equal. Test done!");

       for (int i = 0; i < resTable2.rowCount(); i++) {
            if (resTable2.value(i, 0) != resTab2.value(i, 0)) {
                String errMsg = "column 0:  one aggregation and multiples are not equal" + i;
                throw new RuntimeException(errMsg);
            }
            if (resTable2.value(i, 1) != resTab2.value(i, 1)) {
                String errMsg = "column 1:  one aggregation and multiples are not equal" + i;
                throw new RuntimeException(errMsg);
            }
        }
        System.out.println("One aggregation and multiples are equal. Test done");

    }
}
