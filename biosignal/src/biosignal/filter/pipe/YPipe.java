package biosignal.filter.pipe;

import biosignal.filter.Filter;
import biosignal.filter.FilterChain;
import biosignal.filter.XYData;
import com.biorecorder.datalyb.list.IntArrayList;
import com.biorecorder.datalyb.series.IntSeries;

import java.util.ArrayList;
import java.util.List;

public class YPipe implements YReceiver, Pipe {
    private double startValue;
    private double step;
    private Filter filter;
    private List<YReceiver> yReceivers = new ArrayList<>(1);
    private List<XYReceiver> xyReceivers = new ArrayList<>(1);
    private int counter;

    public YPipe(double startValue, double step, Filter... filters) {
        this.startValue = startValue;
        this.step = step;
        this.filter = new FilterChain(filters);
    }

    public double getStartValue() {
        return startValue;
    }

    public double getStep() {
        return step;
    }

    @Override
    public void addYReceiver(YReceiver yReceiver) {
        yReceivers.add(yReceiver);
    }

    @Override
    public void addXYReceiver(XYReceiver xyReceiver) {
        xyReceivers.add(xyReceiver);
    }

    /**
     * @return XYValues where data will be stored
     */
    @Override
    public XYData enableDataAccumulation() {
        YSink dataSink = new YSink(startValue, step);
        yReceivers.add(dataSink);
        return dataSink.getXYValues();
    }

    public void put(int[] values) {
        for (int v : values) {
           put(v);
        }
    }

    @Override
    public void put(int value) {
        int filteredValue = filter.apply(value);
        for (YReceiver yReceiver : yReceivers) {
            yReceiver.put(filteredValue);
        }
        for (XYReceiver XYReceiver : xyReceivers) {
            XYReceiver.put(startValue + step * counter, filteredValue);
        }
        counter++;
    }

    static class YSink implements YReceiver {
        private double startValue;
        private double step;
        private IntArrayList data = new IntArrayList();

        public YSink(double startValue, double step) {
            this.startValue = startValue;
            this.step = step;
        }

        public XYData getXYValues() {
            return new XYData(startValue, step, new IntSeries() {
                @Override
                public int size() {
                    return data.size();
                }

                @Override
                public int get(int index) {
                    return data.get(index);
                }
            });
        }

        @Override
        public void put(int value) {
            data.add(value);
        }
    }
}
