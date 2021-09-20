package biosignal.application;

import biosignal.filter.XYData;

import java.util.ArrayList;
import java.util.List;

public class DataStore {
    private boolean isDateTime;
    private List<XYData> dataList = new ArrayList();

    public void addDataChannel(String name, XYData xyData) {
        xyData.setName(name);
        dataList.add(xyData);
    }

    public XYData getData(int channel) {
        return dataList.get(channel);
    }

    public int dataChannelCount() {
        return dataList.size();
    }
}
