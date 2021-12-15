package com.biorecorder.bdfrecorder.recorder;

import com.biorecorder.ads.*;

/**
 *
 */
public class RecorderConfig {
    private AdsConfig adsConfig = new AdsConfig();
    private boolean isDurationOfDataRecordAdjustable = true;
    private double durationOfDataRecord = 1; // sec
    private boolean batteryVoltageChannelDeletingEnable = true;
    private ExtraDivider[] channelsExtraDividers;
    private ExtraDivider accelerometerExtraDivider = ExtraDivider.D1;


    public RecorderConfig() {
        channelsExtraDividers = new ExtraDivider[RecorderType.getMaxChannelsCount()];
        for (int i = 0; i < channelsExtraDividers.length; i++) {
            channelsExtraDividers[i] = ExtraDivider.D1;
        }
    }

    public RecorderConfig(RecorderConfig configToCopy) {
        batteryVoltageChannelDeletingEnable = configToCopy.batteryVoltageChannelDeletingEnable;
        durationOfDataRecord = configToCopy.durationOfDataRecord;
        isDurationOfDataRecordAdjustable = configToCopy.isDurationOfDataRecordAdjustable;
        adsConfig = new AdsConfig(configToCopy.adsConfig);
        accelerometerExtraDivider = configToCopy.accelerometerExtraDivider;
        channelsExtraDividers = new ExtraDivider[configToCopy.channelsExtraDividers.length];
        for (int i = 0; i < channelsExtraDividers.length; i++) {
            channelsExtraDividers[i] = configToCopy.channelsExtraDividers[i];
        }

    }

    AdsConfig getAdsConfig() {
        return adsConfig;
    }

    public boolean isDurationOfDataRecordAdjustable() {
        return isDurationOfDataRecordAdjustable;
    }

    public void setDurationOfDataRecordAdjustable(boolean durationOfDataRecordAdjustable) {
        isDurationOfDataRecordAdjustable = durationOfDataRecordAdjustable;
    }

    public boolean isBatteryVoltageChannelDeletingEnable() {
        return batteryVoltageChannelDeletingEnable;
    }

    public void setBatteryVoltageChannelDeletingEnable(boolean batteryVoltageChannelDeletingEnable) {
        this.batteryVoltageChannelDeletingEnable = batteryVoltageChannelDeletingEnable;
    }

    public RecorderDivider getAccelerometerDivider() {
        return RecorderDivider.valueOf(adsConfig.getAccelerometerDivider());
    }

    public RecorderDivider getChannelDivider(int channelNumber) {
        return RecorderDivider.valueOf(adsConfig.getAdsChannelDivider(channelNumber));
    }

    public void setChannelDivider(int channelNumber, RecorderDivider recorderDivider) {
        adsConfig.setAdsChannelDivider(channelNumber, recorderDivider.getAdsDivider());
    }


    public boolean isLeadOffEnabled() {
        return adsConfig.isLeadOffEnabled();
    }

    public int getChannelsCount() {
        return adsConfig.getAdsChannelsCount();
    }

    public int getSampleRate() {
        return adsConfig.getSampleRate().getValue();
    }

    public void setSampleRate(RecorderSampleRate sampleRate) {
        adsConfig.setSampleRate(sampleRate.getAdsSps());
    }

    public boolean isAccelerometerEnabled() {
        return adsConfig.isAccelerometerEnabled();
    }

    public void setAccelerometerEnabled(boolean accelerometerEnabled) {
        adsConfig.setAccelerometerEnabled(accelerometerEnabled);
    }

    public boolean isBatteryVoltageMeasureEnabled() {
        return adsConfig.isBatteryVoltageMeasureEnabled();
    }

    public void setBatteryVoltageMeasureEnabled(boolean batteryVoltageMeasureEnabled) {
        adsConfig.setBatteryVoltageMeasureEnabled(batteryVoltageMeasureEnabled);
    }

    public RecorderType getDeviceType() {
        return RecorderType.valueOf(adsConfig.getAdsType());
    }

    public void setDeviceType(RecorderType recorderType) {
        adsConfig.setAdsType(recorderType.getAdsType());
    }

    public boolean isAccelerometerOneChannelMode() {
        return adsConfig.isAccelerometerOneChannelMode();
    }

    public void setAccelerometerOneChannelMode(boolean accelerometerOneChannelMode) {
        adsConfig.setAccelerometerOneChannelMode(accelerometerOneChannelMode);
    }

    public String getChannelName(int channelNumber) {
        return adsConfig.getAdsChannelName(channelNumber);
    }

    public void setChannelName(int channelNumber, String name) {
        adsConfig.setAdsChannelName(channelNumber, name);
    }

    public void setChannelLeadOffEnable(int channelNumber, boolean leadOffEnable) {
        adsConfig.setAdsChannelLeadOffEnable(channelNumber, leadOffEnable);
    }

    public void setChannelRldSenseEnabled(int channelNumber, boolean rldSenseEnabled) {
        adsConfig.setAdsChannelRldSenseEnabled(channelNumber, rldSenseEnabled);
    }

    public boolean isChannelLeadOffEnable(int channelNumber) {
        return adsConfig.isAdsChannelLeadOffEnable(channelNumber);
    }

    public boolean isChannelRldSenseEnabled(int channelNumber) {
        return adsConfig.isAdsChannelRldSenseEnabled(channelNumber);
    }

    public RecorderGain getChannelGain(int channelNumber) {
        return RecorderGain.valueOf(adsConfig.getAdsChannelGain(channelNumber));
    }

    public void setChannelGain(int channelNumber, RecorderGain recorderGain) {
        adsConfig.setAdsChannelGain(channelNumber, recorderGain.getAdsGain());
    }

    public RecorderCommutator getChannelCommutator(int channelNumber) {
        return RecorderCommutator.valueOf(adsConfig.getAdsChannelCommutatorState(channelNumber));
    }

    public void setChannelCommutator(int channelNumber, RecorderCommutator recorderCommutator) {
        adsConfig.setAdsChannelCommutatorState(channelNumber, recorderCommutator.getAdsCommutator());
    }

    public boolean isChannelEnabled(int channelNumber) {
        return adsConfig.isAdsChannelEnabled(channelNumber);
    }

    public void setChannelEnabled(int channelNumber, boolean enabled) {
        adsConfig.setAdsChannelEnabled(channelNumber, enabled);
    }

    public int getChannelSampleRate(int channelNumber) {
        return getSampleRate() / getChannelDivider(channelNumber).getValue();
    }

    public int getAccelerometerSampleRate() {
        return getSampleRate() / getAccelerometerDivider().getValue();
    }


    public double getDurationOfDataRecord() {
        return adsConfig.getDurationOfDataRecord() * getNumberOfRecordsToJoin();
    }

    public void setDurationOfDataRecord(double durationOfDataRecord) {
        this.durationOfDataRecord = durationOfDataRecord;
    }

    int getNumberOfRecordsToJoin() {
        int numberOfRecordsToJoin = (int) Math.round(durationOfDataRecord / adsConfig.getDurationOfDataRecord());
        if(numberOfRecordsToJoin > 1) {
            return numberOfRecordsToJoin;
        }
        return 1;
    }

    public ExtraDivider getChannelExtraDivider(int channelNumber) {
        return channelsExtraDividers[channelNumber];
    }

    public void setChannelExtraDivider(int channelNumber, ExtraDivider extraDivider) {
        channelsExtraDividers[channelNumber] = extraDivider;
    }

    public ExtraDivider getAccelerometerExtraDivider() {
        return accelerometerExtraDivider;
    }

    public void setAccelerometerExtraDivider(ExtraDivider accelerometerExtraDivider) {
        this.accelerometerExtraDivider = accelerometerExtraDivider;
    }
}
