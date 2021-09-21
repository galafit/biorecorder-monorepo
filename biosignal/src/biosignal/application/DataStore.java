package biosignal.application;

import biosignal.filter.XYData;
import com.biorecorder.bichart.GroupingApproximation;

import java.util.ArrayList;
import java.util.List;

public class DataStore {
    private List<XYData> dataList = new ArrayList();
    private int[] showDataChannels;
    private int[] navigateDataChannels;
    private boolean isDateTime = false;

    public DataStore(boolean isDateTime) {
        this.isDateTime = isDateTime;
    }

    public boolean isDateTime() {
        return isDateTime;
    }

    public int[] getShowDataChannels() {
        return showDataChannels;
    }

    public int[] getNavigateDataChannels() {
        return navigateDataChannels;
    }

    public void setShowDataChannels(int[] showDataChannels) {
        this.showDataChannels = showDataChannels;
    }

    public void setNavigateDataChannels(int[] navigateDataChannels) {
        this.navigateDataChannels = navigateDataChannels;
    }

    public void addDataChannel(String name, XYData xyData, GroupingApproximation groupingApproximation) {
        xyData.setName(name);
        xyData.setGroupingApproximationY(groupingApproximation);
        dataList.add(xyData);
    }

    public XYData getData(int channel) {
        return dataList.get(channel);
    }

    public int dataChannelCount() {
        return dataList.size();
    }
}
