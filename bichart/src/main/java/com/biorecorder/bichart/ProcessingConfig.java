package com.biorecorder.bichart;

import com.biorecorder.data.datatable.aggregation.TimeInterval;

import java.util.Arrays;
import java.util.Comparator;

public class ProcessingConfig {
    private boolean isDataCropEnabled = true;
    private boolean isDataGroupingEnabled = true;
    private int cropShoulder = 1; // number of additional points that we leave on every side during crop
    private double[] groupingIntervals = {50, 100};
    private TimeInterval[] groupingTimeIntervals = null;
    private GroupingType groupingType = GroupingType.EQUAL_POINTS;

    public boolean isDataCropEnabled() {
        return isDataCropEnabled;
    }

    public void setDataCropEnabled(boolean dataCropEnabled) {
        isDataCropEnabled = dataCropEnabled;
    }

    public boolean isDataGroupingEnabled() {
        return isDataGroupingEnabled;
    }

    public void setDataGroupingEnabled(boolean dataGroupingEnabled) {
        isDataGroupingEnabled = dataGroupingEnabled;
    }

    public int getCropShoulder() {
        return cropShoulder;
    }

    public void setCropShoulder(int cropShoulder) {
        this.cropShoulder = cropShoulder;
    }

    public double[] getGroupingIntervals() {
        return groupingIntervals;
    }

    public void setGroupingIntervals(double[] groupingIntervals) {
        Arrays.sort(groupingIntervals);
        this.groupingIntervals = groupingIntervals;
    }

    public TimeInterval[] getGroupingTimeIntervals() {
        return groupingTimeIntervals;
    }

    public void setGroupingTimeIntervals(TimeInterval[] groupingTimeIntervals) {
        Arrays.sort(groupingTimeIntervals, new Comparator<TimeInterval>() {
            @Override
            public int compare(TimeInterval o1, TimeInterval o2) {
                if(o1.toMilliseconds() < o2.toMilliseconds()) {
                    return -1;
                }
                if(o1.toMilliseconds() > o2.toMilliseconds()) {
                    return 1;
                }
                return 0;
            }
        });
        this.groupingTimeIntervals = groupingTimeIntervals;

    }

    public GroupingType getGroupingType() {
        return groupingType;
    }

    public void setGroupingType(GroupingType groupingType) {
        this.groupingType = groupingType;
    }
}

