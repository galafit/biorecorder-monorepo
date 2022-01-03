package com.biorecorder.bdfrecorder.recorder;

import com.biorecorder.ads.*;
import com.biorecorder.edflib.DataHeader;
import com.biorecorder.edflib.DataRecordStream;
import com.biorecorder.edflib.NullDataStream;
import com.biorecorder.edflib.recordfilter.*;
import com.biorecorder.filters.digitalfilter.IntDigitalFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Wrapper class that does some transformations with Ads data-frames:
 * <ul>
 * <li>convert numbered data records to simple data records ("restoring"/supplementing the lost frames)</li>
 * <li>extract lead off info and battery charge info and send it to the appropriate listeners</li>
 * <li>remove  helper technical info about lead-off status and battery charge</li>
 * <li>join data records to 1 second</li>
 * <li>permits to add to ads channels some filters. At the moment - filter removing "50Hz noise" (Moving average filter)</li>
 * </ul>
 * <p>
 * Thus resultant DataFrames (that BioRecorder sends to its listeners) have standard edf/bdf structure and could be
 * directly written to to bdf/edf file
 */
public class BioRecorder {
    private static final String ALL_CHANNELS_DISABLED_MSG = "All channels and accelerometer are disabled. Recording Impossible";
    public static final int START_CHECKING_PERIOD_MS = 500;
    private final Ads ads;
    private volatile EventsListener eventsListener = new NullEventsListener();
    private volatile BatteryLevelListener batteryListener = new NullBatteryLevelListener();
    private volatile LeadOffListener leadOffListener = new NullLeadOffListener();
    private Map<Integer, List<NamedDigitalFilter>> filters = new HashMap();
    private List<DataRecordStream> dataListeners = new ArrayList<>(1);

    private volatile boolean isDurationOfDataRecordComputable;
    private volatile long firstRecordTime;
    private volatile long lastRecordTime;
    private volatile long startTime;
    private volatile DataHeader header;
    private volatile DataRecordStream resultantDataListener;
    private volatile double durationOfDataRecord;
    private volatile long recordsCount;
    private volatile int lastDataRecordNumber = -1;
    private volatile int batteryCurrentPct = 100; // 100%


    public BioRecorder(String comportName) throws ConnectionRuntimeException {
        try {
            ads = new Ads(comportName);
        } catch (AdsConnectionRuntimeException ex) {
            throw new ConnectionRuntimeException(ex);
        }
    }

    public void addChannelFilter(int channelNumber, IntDigitalFilter filter, String filterName) {
        List<NamedDigitalFilter> channelFilters = filters.get(channelNumber);
        if (channelFilters == null) {
            channelFilters = new ArrayList();
            filters.put(channelNumber, channelFilters);
        }
        channelFilters.add(new NamedDigitalFilter(filter, filterName));
    }

    public void removeChannelsFilters() {
        filters = new HashMap();
    }

    /**
     * Start BioRecorder measurements.
     *
     * @param recorderConfig1 object with ads config info
     * @return Future: if starting was successful future.get() return null,
     * otherwise throw RuntimeException. Usually starting fails due to device is not connected
     * or wrong device type is specified in config (that does not coincide
     * with the really connected device type)
     * @throws IllegalStateException    if BioRecorder was disconnected and
     *                                  its work was finalised or if it is already recording and should be stopped first
     * @throws IllegalArgumentException if all channels and accelerometer are disabled
     */
    public Future<Void> startRecording(RecorderConfig recorderConfig1) throws IllegalStateException, IllegalArgumentException {
        // make copy to safely change in the case of "accelerometer only" mode
        RecorderConfig recorderConfig = new RecorderConfig(recorderConfig1);
        isDurationOfDataRecordComputable = recorderConfig.isDurationOfDataRecordAdjustable();
        boolean isAllChannelsDisabled = true;
        for (int i = 0; i < recorderConfig.getChannelsCount(); i++) {
            if (recorderConfig.isChannelEnabled(i)) {
                isAllChannelsDisabled = false;
                break;
            }
        }
        boolean isAccelerometerOnly = false;
        if (isAllChannelsDisabled) {
            if (!recorderConfig.isAccelerometerEnabled()) {
                throw new IllegalArgumentException(ALL_CHANNELS_DISABLED_MSG);
            } else { // enable one ads channel to make possible accelerometer measuring
                isAccelerometerOnly = true;
                recorderConfig.setChannelEnabled(0, true);
                recorderConfig.setChannelDivider(0, RecorderDivider.D10);
                recorderConfig.setChannelLeadOffEnable(0, false);
                recorderConfig.setSampleRate(RecorderSampleRate.S500);
            }
        }
        resultantDataListener = createDataStream(recorderConfig, isAccelerometerOnly);
        AdsConfig adsConfig = recorderConfig.getAdsConfig();
        header = ads.getDataHeader(adsConfig);
        resultantDataListener.setHeader(header);
        ads.setDataListener(new AdsDataHandler(adsConfig, resultantDataListener, recorderConfig.getNumberOfRecordsToJoin()));
        recordsCount = 0;
        durationOfDataRecord = recorderConfig.getDurationOfDataRecord();
        startTime = System.currentTimeMillis();
        return ads.startRecording(adsConfig);
    }

    class AdsDataHandler implements NumberedDataRecordListener {
        private final AdsConfig adsConfig;
        private DataRecordStream dataListener;
        private int numberOfRecordsToJoin;

        public AdsDataHandler(AdsConfig adsConfig, DataRecordStream dataListener, int numberOfRecordsToJoin) {
            this.adsConfig = adsConfig;
            this.dataListener = dataListener;
            this.numberOfRecordsToJoin = numberOfRecordsToJoin;
        }

        @Override
        public void onDataRecordReceived(int[] dataRecord, int recordNumber) {
            if (recordsCount == 0) {
                firstRecordTime = System.currentTimeMillis() - (long) (durationOfDataRecord * 1000)*recordNumber;
                lastRecordTime = firstRecordTime;
            } else {
                lastRecordTime = System.currentTimeMillis();
            }
            recordsCount = recordNumber + 1;
            // send data to listener
            dataListener.writeDataRecord(dataRecord);
            int numberOfLostFrames = recordNumber - lastDataRecordNumber - 1;
            for (int i = 0; i < numberOfLostFrames; i++) {
                dataListener.writeDataRecord(dataRecord);
            }
            lastDataRecordNumber = recordNumber;

            boolean notify = (recordsCount % numberOfRecordsToJoin) == 0;
            // notify lead off listener
            if (adsConfig.isLeadOffEnabled() && notify) {
                notifyLeadOffListeners(Ads.extractLeadOffBitMask(dataRecord, adsConfig));
            }

            // notify battery voltage listener
            if (adsConfig.isBatteryVoltageMeasureEnabled() && notify) {
                int batteryPct = Ads.extractLithiumBatteryPercentage(dataRecord, adsConfig);
                // Percentage level actually are estimated roughly.
                // So we round its value to tens: 100, 90, 80, 70, 60, 50, 40, 30, 20, 10.
                int percentageRound = ((int) Math.round(batteryPct / 10.0)) * 10;

                // this permits to avoid "forward-back" jumps when percentageRound are
                // changing from one ten to the next one (30-40 or 80-90 ...)
                if (percentageRound < batteryCurrentPct) {
                    batteryCurrentPct = percentageRound;
                }
                notifyBatteryLevelListener(batteryCurrentPct);
            }
        }
    }

    public boolean stop() throws IllegalStateException {
        boolean stopOk = ads.stop();
        if(resultantDataListener == null) {
            return stopOk;
        }
       /* if(recordsCount > 1) {
            durationOfDataRecord = (lastRecordTime - firstRecordTime) / ((recordsCount - 1) * 1000.0);
            startTime = firstRecordTime - (long) (durationOfDataRecord * 1000);
            header.setRecordingStartTimeMs(startTime);
            if(isDurationOfDataRecordComputable) {
                header.setDurationOfDataRecord(durationOfDataRecord);
            }
            resultantDataListener.setHeader(header);
        }*/
        resultantDataListener.close();
        resultantDataListener = null;
        return stopOk;
    }

    public boolean disconnect() {
        if (ads.disconnect()) {
            ads.removeDataListener();
            ads.removeMessageListener();
            removeButteryLevelListener();
            removeLeadOffListener();
            removeEventsListener();
            removeDataListeners();
            return true;
        }
        return false;
    }

    public void startMonitoring() throws IllegalStateException {
        ads.startMonitoring();
    }

    public boolean isActive() {
        return ads.isActive();
    }

    public String getComportName() {
        return ads.getComportName();
    }

    public boolean isRecording() {
        return ads.isRecording();
    }

    public RecorderType getDeviceType() {
        AdsType adsType = ads.getAdsType();
        if (adsType == null) {
            return null;
        }
        return RecorderType.valueOf(adsType);
    }

    public static String[] getAvailableComportNames() {
        return Ads.getAvailableComportNames();
    }


    public void addDataListener(DataRecordStream listener) {
        dataListeners.add(listener);
    }

    public void removeDataListeners() {
        dataListeners.clear();
    }

    /**
     * BioRecorder permits to add only ONE LeadOffListener! So if a new listener added
     * the old one are automatically removed
     */
    public void addLeadOffListener(LeadOffListener listener) {
        if (listener != null) {
            leadOffListener = listener;
        }
    }

    public void removeLeadOffListener() {
        leadOffListener = new NullLeadOffListener();
    }

    /**
     * BioRecorder permits to add only ONE ButteryVoltageListener! So if a new listener added
     * the old one are automatically removed
     */
    public void addButteryLevelListener(BatteryLevelListener listener) {
        if (listener != null) {
            batteryListener = listener;
        }
    }

    public void removeButteryLevelListener() {
        batteryListener = new NullBatteryLevelListener();
    }

    /**
     * BioRecorder permits to add only ONE EventsListener! So if a new listener added
     * the old one are automatically removed
     */
    public void addEventsListener(EventsListener listener) {
        if (listener != null) {
            eventsListener = listener;
        }
        ads.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(AdsMessageType messageType, String message) {
                if (messageType == AdsMessageType.LOW_BATTERY) {
                    notifyEventsListeners();
                }
            }

        });
    }

    public void removeEventsListener() {
        eventsListener = new NullEventsListener();
    }

    private void notifyEventsListeners() {
        eventsListener.handleLowBattery();
    }

    private void notifyBatteryLevelListener(int batteryVoltage) {
        batteryListener.onBatteryLevelReceived(batteryVoltage);
    }

    private void notifyLeadOffListeners(Boolean[] leadOffMask) {
        leadOffListener.onLeadOffMaskReceived(leadOffMask);
    }


    private DataRecordStream createDataStream(RecorderConfig recorderConfig, boolean isZeroChannelShouldBeRemoved) {
        if(dataListeners.size() == 0) {
           return new NullDataStream();
        }
        // signal filters
        Map<Integer, List<NamedDigitalFilter>> enableChannelsFilters = new HashMap<>();
        // extra dividers
        Map<Integer, Integer> extraDividers = new HashMap<>();

        int enableChannelsCount = 0;
        for (int i = 0; i < recorderConfig.getChannelsCount(); i++) {
            if (recorderConfig.isChannelEnabled(i)) {
                List<NamedDigitalFilter> channelFilters = filters.get(i);
                if (channelFilters != null) {
                    enableChannelsFilters.put(enableChannelsCount, channelFilters);
                }
                int extraDivider = recorderConfig.getChannelExtraDivider(i).getValue();
                if (extraDivider > 1) {
                    extraDividers.put(enableChannelsCount, extraDivider);
                }
                enableChannelsCount++;
            }
        }

        if (recorderConfig.isAccelerometerEnabled()) {
            Integer extraDivider = recorderConfig.getAccelerometerExtraDivider().getValue();
            if (recorderConfig.isAccelerometerOneChannelMode()) {
                if (extraDivider > 1) {
                    extraDividers.put(enableChannelsCount, extraDivider);
                }
                enableChannelsCount++;
            } else {
                if (extraDivider > 1) {
                    extraDividers.put(enableChannelsCount, extraDivider);
                    extraDividers.put(enableChannelsCount + 1, extraDivider);
                    extraDividers.put(enableChannelsCount + 2, extraDivider);
                }
                enableChannelsCount = enableChannelsCount + 3;
            }

        }
        int batteryChannelNumber = -1;
        int leadOffChannelNumber = -1;
        if (recorderConfig.isBatteryVoltageMeasureEnabled()) {
            batteryChannelNumber = enableChannelsCount;
            enableChannelsCount++;
        }
        if (recorderConfig.isLeadOffEnabled()) {
            leadOffChannelNumber = enableChannelsCount;
            enableChannelsCount++;
        }

        DataRecordStream[] streams = new DataRecordStream[dataListeners.size()];
        dataListeners.toArray(streams); // fill the array
        FilterRecordStream dataFilter = new FilterRecordStream(streams);

        // Add digital filters to ads channels
        if (!enableChannelsFilters.isEmpty()) {
            SignalFilter edfSignalsFilter = new SignalFilter(dataFilter);
            for (Integer signal : enableChannelsFilters.keySet()) {
                List<NamedDigitalFilter> channelFilters = enableChannelsFilters.get(signal);
                for (NamedDigitalFilter filter : channelFilters) {
                    edfSignalsFilter.addSignalFilter(signal, filter, filter.getName());
                }
            }
            dataFilter = edfSignalsFilter;
        }

        // reduce signals frequencies
        if (!extraDividers.isEmpty()) {
            SignalFrequencyReducer edfFrequencyDivider = new SignalFrequencyReducer(dataFilter);
            for (Integer signal : extraDividers.keySet()) {
                edfFrequencyDivider.addDivider(signal, extraDividers.get(signal));
            }
            dataFilter = edfFrequencyDivider;
        }

        // join DataRecords
        int numberOfRecordsToJoin = recorderConfig.getNumberOfRecordsToJoin();
        if(numberOfRecordsToJoin > 1) {
            dataFilter = new RecordsJoiner(dataFilter, numberOfRecordsToJoin);
        }

        // delete helper channels
        if (isZeroChannelShouldBeRemoved || recorderConfig.isLeadOffEnabled() || (recorderConfig.isBatteryVoltageMeasureEnabled() && recorderConfig.isBatteryVoltageChannelDeletingEnable())) {
            SignalRemover edfSignalsRemover = new SignalRemover(dataFilter);
            if (isZeroChannelShouldBeRemoved) {
                // delete helper ads channel
                edfSignalsRemover.removeSignal(0);
            }
            if (recorderConfig.isLeadOffEnabled()) {
                // delete helper Lead-off channel
                edfSignalsRemover.removeSignal(leadOffChannelNumber);
            }
            if (recorderConfig.isBatteryVoltageMeasureEnabled() && recorderConfig.isBatteryVoltageChannelDeletingEnable()) {
                // delete helper BatteryVoltage channel
                edfSignalsRemover.removeSignal(batteryChannelNumber);
            }
            dataFilter = edfSignalsRemover;
        }

        return dataFilter;
    }


    class NullLeadOffListener implements LeadOffListener {
        @Override
        public void onLeadOffMaskReceived(Boolean[] leadOffMask) {
            // do nothing
        }
    }

    class NullEventsListener implements EventsListener {
        @Override
        public void handleLowBattery() {
            // do nothing;
        }
    }

    class NullBatteryLevelListener implements BatteryLevelListener {
        @Override
        public void onBatteryLevelReceived(int batteryLevel) {
            // do nothing;
        }
    }


    class NamedDigitalFilter implements IntDigitalFilter {
        private IntDigitalFilter filter;
        private String filterName;

        public NamedDigitalFilter(IntDigitalFilter filter, String filterName) {
            this.filter = filter;
            this.filterName = filterName;
        }

        @Override
        public int getFilterLength() {
            return filter.getFilterLength();
        }

        @Override
        public int filteredValue(int v) {
            return filter.filteredValue(v);
        }

        public String getName() {
            return filterName;
        }
    }
}
