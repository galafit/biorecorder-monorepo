package com.biorecorder.bichart;

import com.biorecorder.data.frame_new.aggregation.TimeInterval;

public class ProcessingConfig {
    private boolean isDataCropEnabled = true;
    private boolean isDataGroupingEnabled = true;
    private int cropShoulder = 1; // number of additional points that we leave on every side during crop
    private double[] groupingIntervals = null;
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
        this.groupingIntervals = groupingIntervals;
    }

    public TimeInterval[] getGroupingTimeIntervals() {
        return groupingTimeIntervals;
    }

    public void setGroupingTimeIntervals(TimeInterval[] groupingTimeIntervals) {
        this.groupingTimeIntervals = groupingTimeIntervals;
    }

    public GroupingType getGroupingType() {
        return groupingType;
    }

    public void setGroupingType(GroupingType groupingType) {
        this.groupingType = groupingType;
    }
}

