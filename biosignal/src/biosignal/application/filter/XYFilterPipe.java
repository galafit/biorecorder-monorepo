package biosignal.application.filter;

import biosignal.application.XYValues;
import com.biorecorder.data.list.DoubleArrayList;
import com.biorecorder.data.list.IntArrayList;
import com.biorecorder.data.sequence.DoubleSequence;
import com.biorecorder.data.sequence.IntSequence;

import java.util.ArrayList;
import java.util.List;

public class XYFilterPipe implements XYReceiver {
    private XYFilter xyFilter;
    private List<XYReceiver> xyReceivers = new ArrayList<>(1);

    public XYFilterPipe(XYFilter... filters) {
        this.xyFilter = new XYFilterChain(filters);
    }

    public void addXYReceiver(XYReceiver xyReceiver) {
        xyReceivers.add(xyReceiver);
    }

    /**
     * @return XYValues where data will be stored
     */
    public XYValues enableDataStoring() {
        DataSink dataSink = new DataSink();
        xyReceivers.add(dataSink);
        return dataSink.getXYValues();
    }

    @Override
    public void put(double x, int y) {
        if(xyFilter.apply(x, y)){
            double filteredX = xyFilter.getX();
            int filteredY = xyFilter.getY();
            for (XYReceiver receiver : xyReceivers) {
                receiver.put(filteredX, filteredY);
            }
        }
    }

    static class DataSink implements XYReceiver {
        private DoubleArrayList xData = new DoubleArrayList();
        private IntArrayList yData = new IntArrayList();

        public XYValues getXYValues() {
            IntSequence ySeq = new IntSequence() {
                @Override
                public int size() {
                    return yData.size();
                }

                @Override
                public int get(int index) {
                    return yData.get(index);
                }
            };

            DoubleSequence xSeq = new DoubleSequence() {
                @Override
                public int size() {
                    return xData.size();
                }

                @Override
                public double get(int index) {
                    return xData.get(index);
                }
            };
            return new XYValues(xSeq, ySeq);
        }

        @Override
        public void put(double x, int y) {
            yData.add(y);
            xData.add(x);
        }
    }

}
