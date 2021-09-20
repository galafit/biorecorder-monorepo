package biosignal.filter.pipe;

import biosignal.filter.BiFilter;
import biosignal.filter.BiFilterChain;
import biosignal.filter.XYData;
import com.biorecorder.datalyb.list.DoubleArrayList;
import com.biorecorder.datalyb.list.IntArrayList;
import com.biorecorder.datalyb.series.DoubleSeries;
import com.biorecorder.datalyb.series.IntSeries;

import java.util.ArrayList;
import java.util.List;

public class XYPipe implements XYReceiver, Pipe {
    private BiFilter biFilter;
    private List<XYReceiver> XYReceivers = new ArrayList<>(1);

    public XYPipe(BiFilter... filters) {
        this.biFilter = new BiFilterChain(filters);
    }

    @Override
    public void addXYReceiver(XYReceiver xyReceiver) {
        XYReceivers.add(xyReceiver);
    }

    @Override
    public void addYReceiver(YReceiver yReceiver) {
        String errMsg = "To XYPipe may be add only XYReceiver";
        throw new UnsupportedOperationException(errMsg);
    }

    /**
     * @return XYValues where data will be stored
     */
    @Override
    public XYData enableDataAccumulation() {
        DataSink dataSink = new DataSink();
        XYReceivers.add(dataSink);
        return dataSink.getXYValues();
    }

    @Override
    public void put(double x, int y) {
        if(biFilter.apply(x, y)){
            double filteredX = biFilter.getX();
            int filteredY = biFilter.getY();
            for (XYReceiver receiver : XYReceivers) {
                receiver.put(filteredX, filteredY);
            }
        }
    }

    static class DataSink implements XYReceiver {
        private DoubleArrayList xData = new DoubleArrayList();
        private IntArrayList yData = new IntArrayList();

        public XYData getXYValues() {
            IntSeries ySeq = new IntSeries() {
                @Override
                public int size() {
                    return yData.size();
                }

                @Override
                public int get(int index) {
                    return yData.get(index);
                }
            };

            DoubleSeries xSeq = new DoubleSeries() {
                @Override
                public int size() {
                    return xData.size();
                }

                @Override
                public double get(int index) {
                    return xData.get(index);
                }
            };
            return new XYData(xSeq, ySeq);
        }

        @Override
        public void put(double x, int y) {
            yData.add(y);
            xData.add(x);
        }
    }

}
