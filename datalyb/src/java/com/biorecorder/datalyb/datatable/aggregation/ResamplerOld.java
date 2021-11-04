package com.biorecorder.datalyb.datatable.aggregation;

import com.biorecorder.datalyb.datatable.*;
import com.biorecorder.datalyb.list.IntArrayList;
import com.biorecorder.datalyb.series.IntSeries;
import com.biorecorder.datalyb.time.TimeInterval;

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
public class ResamplerOld {
    private Map<Integer, Aggregation[]> columnsToAgg = new HashMap();
    private Map<Integer, RegularColumn> regularColumns = new HashMap<>();
    private DataTable resultantTable;
    private Aggregation defaultAggregation;
    private Binning binning;


    private ResamplerOld(Binning binning) {
        this.binning = binning;
    }

    public DataTable resultantData() {
        return resultantTable;
    }

    public static ResamplerOld createEqualPointsResamplerOld(int pointsInGroup) {
        Binning binning = new EqualPointsBinning(pointsInGroup);
        return new ResamplerOld(binning);
    }

    public static ResamplerOld createEqualIntervalResamplerOld(double interval) {
        Binning binning = new EqualIntervalBinning(new DoubleIntervalProvider(interval));
        return new ResamplerOld(binning);
    }

    public static ResamplerOld createEqualTimeIntervalResamplerOld(TimeInterval timeInterval) {
        Binning binning = new EqualIntervalBinning(new TimeIntervalProvider(timeInterval));
        return new ResamplerOld(binning);
    }

    public void addColumnAggregations(int column, Aggregation... aggregations) {
        columnsToAgg.put(column, aggregations);
    }


/*    public DataTable resampleAndAppend(DataTable tableToResample) {
        if (resultantTable == null) {
            resultantTable = new DataTable(tableToResample.name());
            for (int i = 0; i < tableToResample.columnCount(); i++) {
                Aggregation[] aggregations = columnsToAgg.get(i);
                Column col = tableToResample.getColumn(i);
                if (binning.isEqualPoints() && col instanceof RegularColumn) {
                    for (int j = 0; j < aggregations.length; j++) {
                        resultantTable.addColumn(col.emptyCopy());
                        regularColumns.put(i + j, (RegularColumn) col.emptyCopy());
                    }
                } else {
                    for (int j = 0; j < aggregations.length; j++) {
                        resultantTable.addColumn(col.type().create(""));
                    }
                }
            }
        }
        IntSeries groups = binning.group(tableToResample.getColumn(0));
        for (int i = 0; i < tableToResample.columnCount(); i++) {
            Aggregation[] aggregations = columnsToAgg.get(i);
            for (int j = 0; j < aggregations.length; j++) {
                int resultantColNumber = i + j;
                RegularColumn rc = regularColumns.get(resultantColNumber);
                if (binning.isEqualPoints() && rc != null) {
                    rc.append(tableToResample.getColumn(i));
                    resultantTable.removeColumn(resultantColNumber);
                    resultantTable.addColumn(resultantColNumber, rc.resample(binning.pointsInGroup()));
                } else {
                    resampleAndAppend(aggregations[j], groups, resultantTable.getColumn(resultantColNumber), tableToResample.getColumn(i));
                }
            }
        }
        return resultantTable;
    }


    private Column resampleAndAppend(Aggregation agg, IntSeries groups, Column resultantCol, Column colToAgg) throws IllegalArgumentException {
        int groupCounter = 0;
        int groupStart = colToAgg.size() + 1;
        if (groups.size() > 0) {
            groupStart = groups.get(groupCounter);
        }
        if (colToAgg.type().getBaseType() == BaseType.INT) {
            IntColumn intColToAgg = (IntColumn) colToAgg;
            IntColumn intResultantCol = (IntColumn) resultantCol;
            for (int i = 0; i < colToAgg.size(); i++) {
                if (i == groupStart) {
                    intResultantCol.append(agg.getInt());
                    agg.reset();
                    groupCounter++;
                    if (groupCounter < groups.size()) {
                        groupStart = groups.get(groupCounter);
                    }
                }
                agg.addInt(intColToAgg.intValue(i));
            }
        }
        if (colToAgg.type() == BaseType.DOUBLE) {
            DoubleColumn doubleColToAgg = (DoubleColumn) colToAgg;
            DoubleColumn doubleResultantCol = (DoubleColumn) resultantCol;
            for (int i = 0; i < colToAgg.size(); i++) {
                if (i == groupStart) {
                    doubleResultantCol.append(agg.getDouble());
                    agg.reset();
                    groupCounter++;
                    if (groupCounter < groups.size()) {
                        groupStart = groups.get(groupCounter);
                    }
                }
                agg.addDouble(doubleColToAgg.value(i));
            }
        }
        return resultantCol;
    }*/

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
            return null; //groupStarts;
        }
    }

    interface Binning {
        IntSeries group(Column col);
        boolean isEqualPoints();
        int pointsInGroup();
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
            if (firstGroupPoints > column.size()) {
                pointsAdded += column.size();
                numberOfGroups = 0;
            } else {
                int groups = 1;
                groups += (column.size() - firstGroupPoints) / pointsInGroup;
                int lastGroupPoints = (column.size() - firstGroupPoints) % pointsInGroup;
                if (lastGroupPoints == 0) {
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
}