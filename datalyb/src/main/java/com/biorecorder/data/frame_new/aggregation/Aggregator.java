package com.biorecorder.data.frame_new.aggregation;
import com.biorecorder.data.frame_new.Column;
import com.biorecorder.data.frame_new.DataTable;
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
                resultantTable.addColumn(col.type().create(col.name()));
            }
        }

        IntSequence groups = grouper.group(tableToAggregate.getColumn(0));
        for (int i = 0; i < tableToAggregate.columnCount(); i++) {
            resultantTable.getColumn(i).aggregateAndAppend(columnsToAgg.get(i)[0], groups, tableToAggregate.getColumn(i));
        }
        return resultantTable;
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
            if(firstGroupPoints <= column.size()) {
                pointsAdded += column.size();
                numberOfGroups = 0;
            } else {
                numberOfGroups = (column.size() - firstGroupPoints) / pointsInGroup;
                pointsAdded = (column.size() - firstGroupPoints) % pointsInGroup;
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
                        return pointsInGroup;
                    }

                }
            };
        }
    }
}
