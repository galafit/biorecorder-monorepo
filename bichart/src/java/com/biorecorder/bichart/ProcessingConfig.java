package com.biorecorder.bichart;

import com.biorecorder.datalyb.time.TimeInterval;

import java.util.Arrays;
import java.util.Comparator;

public class ProcessingConfig {
    private int cropShoulder = 1; // number of additional points that we leave on every side during crop
    private double[] groupingIntervals = null; //{50, 100};
    private TimeInterval[] groupingTimeIntervals = null;
    private GroupingType groupingType = GroupingType.EQUAL_POINTS;

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

    public void setGroupingTimeIntervals(TimeGroupInterval[] groupingTimeIntervals) {
        TimeInterval[] timeIntervals = new TimeInterval[groupingTimeIntervals.length];
        for (int i = 0; i < timeIntervals.length; i++) {
            timeIntervals[i] = groupingTimeIntervals[i].getTimeInterval();
        }
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

