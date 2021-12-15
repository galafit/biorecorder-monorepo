package com.biorecorder.bichart;

import com.biorecorder.datalyb.time.TimeInterval;

import java.util.Arrays;
import java.util.Comparator;

public class ProcessingConfig {
    private int cropShoulder = 1; // number of additional points that we leave on every side during crop
    private double[] groupingIntervals = {}; //{50, 100};
    private TimeInterval[] groupingTimeIntervals = {};
    private GroupingType groupingType = GroupingType.EQUAL_POINTS;
    private boolean isCropEnabled = true;
    private boolean isGroupingEnabled = true;


    public boolean isProcessingEnabled() {
        return isCropEnabled && isGroupingEnabled;
    }

    public ProcessingConfig processingDisable() {
        isCropEnabled = false;
        isGroupingEnabled = false;
        return this;
    }

    public boolean isCropEnabled() {
        return isCropEnabled;
    }

    public void setCropEnabled(boolean cropEnabled) {
        isCropEnabled = cropEnabled;
    }

    public boolean isGroupingEnabled() {
        return isGroupingEnabled;
    }

    public void setGroupingEnabled(boolean groupingEnabled) {
        isGroupingEnabled = groupingEnabled;
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

    public void setGroupingIntervals(double... groupingIntervals) {
        if(groupingIntervals == null) {
            this.groupingIntervals = null;
            return;
        }
        Arrays.sort(groupingIntervals);
        this.groupingIntervals = groupingIntervals;
    }

    public TimeInterval[] getGroupingTimeIntervals() {
        return groupingTimeIntervals;
    }

    public void setGroupingTimeIntervals(TimeInterval... timeIntervals) {
        Arrays.sort(timeIntervals, new Comparator<TimeInterval>() {
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
        this.groupingTimeIntervals = timeIntervals;

    }

    public GroupingType getGroupingType() {
        return groupingType;
    }

    public void setGroupingType(GroupingType groupingType) {
        this.groupingType = groupingType;
    }
}

