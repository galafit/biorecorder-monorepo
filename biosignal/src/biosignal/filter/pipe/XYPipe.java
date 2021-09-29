package biosignal.filter.pipe;

import biosignal.filter.BiFilter;
import biosignal.filter.BiFilterChain;
import biosignal.filter.XYData;
import com.biorecorder.datalyb.datatable.DoubleColumn;
import com.biorecorder.datalyb.datatable.IntColumn;

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
        return dataSink.getXYData();
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
        private DoubleColumn xData = new DoubleColumn("x");
        private IntColumn yData = new IntColumn("y");

        public XYData getXYData() {
            return new XYData("XYData", xData, yData);
        }

        @Override
        public void put(double x, int y) {
            yData.append(y);
            xData.append(x);
        }
    }

}
