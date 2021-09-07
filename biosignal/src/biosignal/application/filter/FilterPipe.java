package biosignal.application.filter;

import biosignal.application.XYValues;
import com.biorecorder.data.list.IntArrayList;
import com.biorecorder.data.sequence.IntSequence;

import java.util.ArrayList;
import java.util.List;

public class FilterPipe implements DataReceiver {
    private double startValue;
    private double step;
    private Filter filter;
    private List<DataReceiver> dataReceivers = new ArrayList<>(1);
    private List<XYReceiver> xyReceivers = new ArrayList<>(1);
    private int counter;

    public FilterPipe(double startValue, double step, Filter... filters) {
        this.startValue = startValue;
        this.step = step;
        this.filter = new FilterChain(filters);
    }

    public void addYReceiver(DataReceiver receiver) {
        dataReceivers.add(receiver);
    }

    public void addXYReceiver(XYReceiver xyReceiver) {
        xyReceivers.add(xyReceiver);
    }

    /**
     * @return XYValues where data will be stored
     */
    public XYValues enableDataStoring() {
        DataSink dataSink = new DataSink(startValue, step);
        dataReceivers.add(dataSink);
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
        for (DataReceiver dataReceiver : dataReceivers) {
            dataReceiver.put(filteredValue);
        }
        for (XYReceiver xyReceiver : xyReceivers) {
            xyReceiver.put(startValue + step * counter, filteredValue);
        }
        counter++;
    }

    static class DataSink implements DataReceiver {
        private double startValue;
        private double step;
        private IntArrayList data = new IntArrayList();

        public DataSink(double startValue, double step) {
            this.startValue = startValue;
            this.step = step;
        }

        public XYValues getXYValues() {
            return new XYValues(startValue, step, new IntSequence() {
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
