package com.biorecorder.bichart.dataprocessing;

import com.biorecorder.bichart.GroupingType;

import java.util.Arrays;

/**
 * Created by galafit on 9/7/18.
 */
public class DataProcessingConfig {
    private boolean isCropEnabled = true;
    private int cropShoulder = 1; // number of additional points that we leave on every side during crop

    private boolean isGroupingEnabled = true;

    // cropped data will be caching like grouped data. It make sense in the case
    // of heavy data (when data read from a file or calculated)
    boolean isCroppedDataCachingEnabled = false;

    // In case of GroupType.AUTO regular DataSeries will be grouped by equal points number
    // and non-regular by equal intervals
    private GroupingType groupingType = GroupingType.EQUAL_POINTS;

    // used  when groupingIntervals are not specified (null)
    private int groupingStep = 2;

    // if group intervals are defined (not null)
    // then intervals for grouping will be taken only from that array,
    // otherwise every time "the best group interval" will be calculated automatically
    private double[] groupingIntervals = null;

    // if groupingIntervals are specified (not null) and isGroupingForced = true
    // then grouping will be done in any case
    private boolean isGroupingForced = false;

    // group all data or only visible ones
    private boolean isGroupAll = false;

    // if true then fully grouped (groupAll) data will be saved
    // for all specified intervals
    private boolean isSavingGroupedDataEnabled = false;

    public DataProcessingConfig() {
    }

    public DataProcessingConfig(boolean isCropEnabled, boolean isGroupingEnabled) {
        this.isCropEnabled = isCropEnabled;
        this.isGroupingEnabled = isGroupingEnabled;
    }

    public DataProcessingConfig(DataProcessingConfig config) {
        cropShoulder = config.cropShoulder;
        isCropEnabled = config.isCropEnabled;
        isGroupingEnabled = config.isGroupingEnabled;
        groupingType = config.groupingType;
        isGroupingForced = config.isGroupingForced;
        groupingStep = config.groupingStep;
        isGroupAll = config.isGroupAll;
        isSavingGroupedDataEnabled = config.isSavingGroupedDataEnabled;
        isCroppedDataCachingEnabled = config.isCroppedDataCachingEnabled;
        if(config.groupingIntervals != null) {
            groupingIntervals = Arrays.copyOf(config.groupingIntervals, config.groupingIntervals.length);
        }
    }

    public boolean isSavingGroupedDataEnabled() {
        return isSavingGroupedDataEnabled;
    }

    public void setSavingGroupedDataEnabled(boolean savingGroupedDataEnabled) {
        isSavingGroupedDataEnabled = savingGroupedDataEnabled;
    }

    public boolean isCroppedDataCachingEnabled() {
        return isCroppedDataCachingEnabled;
    }

    public void setCroppedDataCachingEnabled(boolean croppedDataCachingEnabled) {
        isCroppedDataCachingEnabled = croppedDataCachingEnabled;
    }

    public boolean isGroupAll() {
        return isGroupAll;
    }

    public void setGroupAll(boolean groupAll) {
        isGroupAll = groupAll;
    }

    public int getGroupingStep() {
        return groupingStep;
    }

    public void setGroupingStep(int groupingStep) throws IllegalArgumentException {
        if(groupingStep < 2) {
            String errMsg = "Re grouping step: " + groupingStep + " Expected > 1";
            throw new IllegalArgumentException(errMsg);
        }
        this.groupingStep = groupingStep;
    }

    public boolean isGroupingForced() {
        return isGroupingForced;
    }

    public void setGroupingForced(boolean groupingForced) {
        isGroupingForced = groupingForced;
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

    public GroupingType getGroupingType() {
        return groupingType;
    }

    public void setGroupingType(GroupingType groupingType) {
        this.groupingType = groupingType;
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

}
