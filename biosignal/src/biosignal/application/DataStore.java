package biosignal.application;

import biosignal.filter.XYData;
import com.biorecorder.bichart.GroupingApproximation;

import java.util.ArrayList;
import java.util.List;

public class DataStore {
    private List<XYData> dataList = new ArrayList();
    private List<GroupingApproximation> dataGroupingApproximations = new ArrayList();
    private int[] chartDataChannels1;
    private int[] chartDataChannels2;
    private int[] navigatorDataChannels;
    private boolean isDateTime;

    public DataStore(boolean isDateTime) {
        this.isDateTime = isDateTime;
    }

    public boolean isDateTime() {
        return isDateTime;
    }

    public int[] getChartDataChannels1() {
        return chartDataChannels1;
    }

    public void setChartDataChannels1(int[] chartDataChannels1) {
        this.chartDataChannels1 = chartDataChannels1;
    }

    public int[] getChartDataChannels2() {
        return chartDataChannels2;
    }

    public void setChartDataChannels2(int[] chartDataChannels2) {
        this.chartDataChannels2 = chartDataChannels2;
    }

    public int[] getNavigatorDataChannels() {
        return navigatorDataChannels;
    }

    public void setNavigatorDataChannels(int[] navigatorDataChannels) {
        this.navigatorDataChannels = navigatorDataChannels;
    }

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
