package biosignal.application.filter;

import biosignal.application.XYData;
import com.biorecorder.datalyb.list.DoubleArrayList;
import com.biorecorder.datalyb.list.IntArrayList;
import com.biorecorder.datalyb.series.DoubleSeries;
import com.biorecorder.datalyb.series.IntSeries;

import java.util.ArrayList;
import java.util.List;

public class BiFilterPipe implements BiDataReceiver {
    private BiFilter biFilter;
    private List<BiDataReceiver> biDataReceivers = new ArrayList<>(1);

    public BiFilterPipe(BiFilter... filters) {
        this.biFilter = new BiFilterChain(filters);
    }

    public void addXYReceiver(BiDataReceiver biDataReceiver) {
        biDataReceivers.add(biDataReceiver);
    }

    /**
     * @return XYValues where data will be stored
     */
    public XYData enableDataAccumulation() {
        DataSink dataSink = new DataSink();
        biDataReceivers.add(dataSink);
        return dataSink.getXYValues();
    }

    @Override
    public void put(double x, int y) {
        if(biFilter.apply(x, y)){
            double filteredX = biFilter.getX();
            int filteredY = biFilter.getY();
            for (BiDataReceiver receiver : biDataReceivers) {
                receiver.put(filteredX, filteredY);
            }
        }
    }

    static class DataSink implements BiDataReceiver {
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
