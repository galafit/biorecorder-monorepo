package biosignal.application;

import biosignal.filter.XYData;
import com.biorecorder.bichart.GroupingApproximation;

import java.util.ArrayList;
import java.util.List;

public class DataStore {
    private List<XYData> dataList = new ArrayList();
    private List<GroupingApproximation> dataGroupingApproximations = new ArrayList();

    public void addDataChannel(String name, XYData xyData, GroupingApproximation groupingApproximation) {
        xyData.setName(name);
        dataList.add(xyData);
        dataGroupingApproximations.add(groupingApproximation);
    }

    public XYData getData(int channel) {
        return dataList.get(channel);
    }

    public GroupingApproximation getDataGroupingApproximation(int channel) {
        return dataGroupingApproximations.get(channel);
    }

    public int dataChannelCount() {
        return dataList.size();
    }
}
