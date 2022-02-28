package com.biorecorder.ads;


import com.biorecorder.comport.ComportFactory;
import com.biorecorder.edflib.DataHeader;
import com.biorecorder.edflib.FormatVersion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Ads packs samples from all channels received during the
 * time = durationOfDataRecord = MaxDiv/getMaxFrequency
 * in one array of int. Every array (data record or data package) has
 * the following structure (in case of 8 channels):
 * <p>
 * <br>{
 * <br>  n_0 samples from ads_channel_0 (if this ads channel enabled)
 * <br>  n_1 samples from ads_channel_1 (if this ads channel enabled)
 * <br>  ...
 * <br>  n_8 samples from ads_channel_8 (if this ads channel enabled)
 * <br>  n_acc_x samples from accelerometer_x channel
 * <br>  n_acc_y samples from accelerometer_y channel
 * <br>  n_acc_z samples from accelerometer_Z channel
 * <br>  1 sample with BatteryVoltage info (if BatteryVoltageMeasure  enabled)
 * <br>  1 sample with lead-off detection info (if lead-off detection enabled)
 * <br>}
 * <p>
 * Where n_i = ads_channel_i_sampleRate * durationOfDataRecord
 * <br>ads_channel_i_sampleRate = sampleRate / ads_channel_i_divider
 * <p>
 * n_acc_x = n_acc_y = n_acc_z =  accelerometer_sampleRate * durationOfDataRecord
 * <br>accelerometer_sampleRate = sampleRate / accelerometer_divider
 * <p>
 * If for Accelerometer  one channel mode is activated then samples from
 * acc_x_channel, acc_y_channel and acc_z_channel will be summarized and data records will
 * have "one accelerometer channel" instead of three:
 * <p>
 * <br>{
 * <br>  n_0 samples from ads_channel_0 (if this ads channel enabled)
 * <br>  n_1 samples from ads_channel_1 (if this ads channel enabled)
 * <br>  ...
 * <br>  n_8 samples from ads_channel_8 (if this ads channel enabled)
 * <br>  n_acc samples from accelerometer channels
 * <br>  1 sample with BatteryVoltage info (if BatteryVoltageMeasure  enabled)
 * <br>  1 (for 2 channels) or 2 (for 8 channels) samples with lead-off detection info (if lead-off detection enabled)
 * <br>}
 * <p>
 * Where n_acc =  accelerometer_sampleRate * durationOfDataRecord
 */
public class Ads {
    private static final Log log = LogFactory.getLog(Ads.class);

    private static final int PING_PERIOD_MS = 1000;
    private static final int MONITORING_PERIOD_MS = 1000;
    private static final int SLEEP_TIME_MS = 1000;
    private static final int ACTIVE_PERIOD_MS = 2000;

    private static final int MAX_STARTING_TIME_MS = 30 * 1000;

    private static final String DISCONNECTED_MSG = "Ads is disconnected and its work is finalised";
    private static final String RECORDING_MSG = "Ads is recording. Stop it first";
    private static final String ALL_CHANNELS_DISABLED_MSG = "All Ads channels are disabled. Recording Impossible";

    private volatile long lastEventTime;
    private volatile boolean isDataReceived;
    private volatile AdsType adsType = AdsType.ADS_2;

    // we use AtomicReference to do atomic "compare and set"
    // from different threads: the main thread
    // and frameDecoder thread (handling Ads messages)
    private AtomicReference<AdsState> adsStateAtomicReference =
            new AtomicReference<AdsState>(AdsState.UNDEFINED);

    private final CommunicationPort communicationPort;
    private final ExecutorService singleThreadExecutor;
    private volatile Future executorFuture;
    private volatile NumberedDataRecordListener dataListener;
    private volatile MessageListener messageListener;


    public Ads(String comportName) throws ConnectionRuntimeException {
        dataListener = new NullDataListener();
        messageListener = new NullMessageListener();
        ThreadFactory namedThreadFactory = new ThreadFactory() {
            public Thread newThread(Runnable r) {
                return new Thread(r, "«Data Records handling» thread");
            }
        };
        singleThreadExecutor = Executors.newSingleThreadExecutor(namedThreadFactory);
        communicationPort = new CommunicationPort(comportName);
        communicationPort.addDataListener(new NumberedDataRecordListener() {
            @Override
            public void onDataRecordReceived(int[] dataRecord, int recordNumber) {
                lastEventTime = System.currentTimeMillis();
                isDataReceived = true;
                notifyDataListeners(dataRecord, recordNumber);
            }
        });
        communicationPort.addMessageListener(new MessageListener() {
            @Override
            public void onMessageReceived(AdsMessage messageType, String message) {
                if (messageType == AdsMessage.HELLO || messageType == AdsMessage.UNKNOWN) {
                    lastEventTime = System.currentTimeMillis();
                }
                if (messageType == AdsMessage.ADS_2_CHANNELS) {
                    adsType = AdsType.ADS_2;
                    lastEventTime = System.currentTimeMillis();
                    log.info(message);
                }
                if (messageType == AdsMessage.ADS_8_CHANNELS) {
                    adsType = AdsType.ADS_8;
                    lastEventTime = System.currentTimeMillis();
                    log.info(message);
                }
                if (messageType == AdsMessage.STOP_RECORDING) {
                    adsStateAtomicReference.set(AdsState.STOPPED);
                    log.info(message);
                }
                if (messageType == AdsMessage.FRAME_BROKEN) {
                    log.info(message);
                }
                if (messageType == AdsMessage.LOW_BATTERY) {
                    log.info(message);
                }
                if (messageType == AdsMessage.UNKNOWN) {
                    log.info(message);
                }
                notifyMessageListeners(messageType, message);
            }
        });
    }

    /**
     * Start "monitoring timer" which every second sends to
     * Ads HELLO request to check that
     * Ads is connected and ok. Can be called only if Ads is NOT recording
     *
     * @throws IllegalStateException if Ads was disconnected and its work is finalised
     *                               or if it is recording and should be stopped first
     */
    public void startMonitoring() throws IllegalStateException {
        if (!communicationPort.isOpened()) {
            throw new IllegalStateException(DISCONNECTED_MSG);
        }

        if (adsStateAtomicReference.get() == AdsState.RECORDING) {
            throw new IllegalStateException(RECORDING_MSG);
        }
        if (adsStateAtomicReference.get() == AdsState.UNDEFINED) {
            communicationPort.sendCommand(adsType.adsStopCommand());
        }
        if (executorFuture != null) {
            executorFuture.cancel(true);
        }
        executorFuture = singleThreadExecutor.submit(new MonitoringTask());
    }

    /**
     * Start Ads measurements. Stop monitoring if it was activated before
     *
     * @param config object with ads config info
     * @return Future: if starting was successful future.get() return null,
     * otherwise throw RuntimeException. Usually starting fails due to device is not connected
     * or wrong device type is specified in config (that does not coincide
     * with the really connected device type)
     * @throws IllegalStateException if Ads was disconnected and its work was finalised
     *                               or if it is already recording and should be stopped first
     * @throws IllegalArgumentException if all Ads channels are disabled
     *
     */

    public Future<Void> startRecording(AdsConfig config) throws IllegalStateException, IllegalArgumentException {
        if (!communicationPort.isOpened()) {
            throw new IllegalStateException(DISCONNECTED_MSG);
        }

        if (adsStateAtomicReference.get() == AdsState.RECORDING) {
            throw new IllegalStateException(RECORDING_MSG);
        }

        // copy config because we will change it
        AdsConfig adsConfig = new AdsConfig(config);
        boolean isAllChannelsDisabled = true;
        for (int i = 0; i < adsConfig.getAdsChannelsCount(); i++) {
            if(adsConfig.isAdsChannelEnabled(i)) {
                isAllChannelsDisabled = false;
            } else {
                adsConfig.setAdsChannelCommutatorState(i, Commutator.INPUT_SHORT);
                adsConfig.setAdsChannelRldSenseEnabled(i, false);
                adsConfig.setAdsChannelLeadOffEnable(i, false);
            }
        }
        if(isAllChannelsDisabled) {
            throw new IllegalArgumentException(ALL_CHANNELS_DISABLED_MSG);
        }
        // stop monitoring
        if (executorFuture != null && !executorFuture.isDone()) {
            executorFuture.cancel(true);
        }
        communicationPort.config(adsConfig);
        isDataReceived = false;
        AdsState stateBeforeStart = adsStateAtomicReference.get();
        adsStateAtomicReference.set(AdsState.RECORDING);
        executorFuture = singleThreadExecutor.submit(new StartingTask(adsConfig, stateBeforeStart));
        return executorFuture;
    }


    class StartingTask implements Callable<Void> {
        private final String TIME_OUT_ERR_MSG = "Starting time exceeds allowed limits: " + MAX_STARTING_TIME_MS + " ms";
        private AdsConfig config;
        private AdsState stateBeforeStart;


        public StartingTask(AdsConfig config, AdsState stateBeforeStart) {
            this.config = config;
            this.stateBeforeStart = stateBeforeStart;
        }

        @Override
        public Void call() throws Exception {
            long startTime = System.currentTimeMillis();
            // 1) request adsType
            communicationPort.sendCommand(adsType.adsHardwareRequestCommand());
            Thread.sleep(SLEEP_TIME_MS);

            // if adsType is wrong
            if (adsType != config.getAdsType()) {
                String errMsg = "Wrong device type: "+ config.getAdsType() + " Connected: "+adsType;
                throwException(errMsg);
            }

            // 2) if adsType is ok try to stop ads first
            communicationPort.sendCommand(adsType.adsStopCommand());
            // give the ads time to stop
            Thread.sleep(SLEEP_TIME_MS * 2);

            // 3) write ads config commands with config info to comport
            List<byte[]> configCommands = adsType.adsConfigurationCommands(config);
            for (byte[] command : configCommands) {
                if(!communicationPort.sendCommand(command)) {
                    // if writing start command to comport was failed
                    String errMsg = "Failed to write start command to comport";
                    throwException(errMsg);
                }
                Thread.sleep(25);
            }
            // 4) waiting for data
            while (!isDataReceived) {
                Thread.sleep(SLEEP_TIME_MS);

                // if data do note come during too long time
                if((System.currentTimeMillis() - startTime) > MAX_STARTING_TIME_MS) {
                    throwException(TIME_OUT_ERR_MSG);
                }
            }

            // 5) startRecording ping timer
            // ping timer permits Ads to detect bluetooth connection problems
            // and restart connection when it is necessary
            executorFuture = singleThreadExecutor.submit(new PingTask(), PING_PERIOD_MS);
            return null;
        }


        private void throwException(String errMsg) {
            try {
                communicationPort.sendCommand(adsType.adsStopCommand());
            } catch (Exception ex) {
                // do nothing;
            }
            adsStateAtomicReference.set(AdsState.UNDEFINED);
            throw new RuntimeException(errMsg);
        }
    }

    private boolean stop1() {
        // cancel starting, pinging or monitoring
        if (executorFuture != null && !executorFuture.isDone()) {
            executorFuture.cancel(true);
        }
        adsStateAtomicReference.compareAndSet(AdsState.RECORDING, AdsState.UNDEFINED);
        // send stop command
        boolean isStopOk = communicationPort.sendCommand(adsType.adsStopCommand());
        if (isStopOk) {
            // give ads time to stop
            try {
                Thread.sleep(SLEEP_TIME_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return isStopOk;
    }

    /**
     * Stops ads measurements or monitoring
     *
     * @throws IllegalStateException if Ads was disconnected and its work is finalised
     */
    public boolean stop() throws IllegalStateException {
        if (!communicationPort.isOpened()) {
            throw new IllegalStateException(DISCONNECTED_MSG);
        }
        return stop1();
    }

    public boolean disconnect() {
        if(adsStateAtomicReference.get() == AdsState.RECORDING) {
            stop1();
        }
        singleThreadExecutor.shutdown();
        return communicationPort.close();
    }


    /**
     * This method return true if last ads monitoring message (device_type)
     * or data_frame was received less then ACTIVE_PERIOD_MS (1 sec) ago
     */
    public boolean isActive() {
        if ((System.currentTimeMillis() - lastEventTime) <= ACTIVE_PERIOD_MS) {
            return true;
        }
        return false;
    }

    public boolean isRecording() {
        if (adsStateAtomicReference.get() == AdsState.RECORDING) {
            return true;
        }
        return false;
    }

    public AdsType getAdsType() {
        return adsType;
    }

    /**
     * Ads permits to add only ONE RecordListener! So if a new listener added
     * the old one are automatically removed
     */
    public void setDataListener(NumberedDataRecordListener listener) {
        if (listener != null) {
            dataListener = listener;
        }
    }

    /**
     * Ads permits to add only ONE MessageListener! So if a new listener added
     * the old one are automatically removed
     */
    public void setMessageListener(MessageListener listener) {
        if (listener != null) {
            messageListener = listener;
        }
    }

    public void removeDataListener() {
        dataListener = new NullDataListener();
    }

    public void removeMessageListener() {
        messageListener = new NullMessageListener();
    }

    private void notifyDataListeners(int[] dataRecord, int recordNumber) {
        dataListener.onDataRecordReceived(dataRecord, recordNumber);

    }

    private void notifyMessageListeners(AdsMessage adsMessage, String additionalInfo) {
        messageListener.onMessageReceived(adsMessage, additionalInfo);
    }

    public String getComportName() {
        return communicationPort.getComportName();
    }

    public static String[] getAvailableComportNames() {
        return ComportFactory.getAvailableComportNames();
    }

    /**
     * Get the info describing the structure of data records
     * that Ads sends to its listeners
     *
     * @return object describing data records structure
     */
    public DataHeader getDataHeader(AdsConfig adsConfig) {
        DataHeader edfConfig = new DataHeader(FormatVersion.BDF_24BIT);
        edfConfig.setDurationOfDataRecord(adsConfig.getDurationOfDataRecord());
        for (int i = 0; i < adsConfig.getAdsChannelsCount(); i++) {
            if (adsConfig.isAdsChannelEnabled(i)) {
                int channelSampleRate = adsConfig.getSampleRate().getValue() / adsConfig.getAdsChannelDivider(i);
                int nrOfSamplesInEachDataRecord = (int) Math.round(adsConfig.getDurationOfDataRecord() * channelSampleRate);
                edfConfig.addSignal(nrOfSamplesInEachDataRecord);
                int signalNumber = edfConfig.numberOfSignals() - 1;
                edfConfig.setTransducer(signalNumber, "Unknown");
                edfConfig.setPhysicalDimension(signalNumber, getAdsChannelsPhysicalDimension());
                edfConfig.setPhysicalRange(signalNumber, getAdsChannelPhysicalMin(adsConfig.getAdsChannelGain(i)), getAdsChannelPhysicalMax(adsConfig.getAdsChannelGain(i)));
                edfConfig.setDigitalRange(signalNumber, getAdsChannelsDigitalMin(adsConfig.getNoiseDivider()), getAdsChannelsDigitalMax(adsConfig.getNoiseDivider()));
                edfConfig.setLabel(signalNumber, adsConfig.getAdsChannelName(i));
            }
        }

        if (adsConfig.isAccelerometerEnabled()) {
            int accSampleRate = adsConfig.getSampleRate().getValue() / adsConfig.getAccelerometerDivider();
            int nrOfSamplesInEachDataRecord = (int) Math.round(adsConfig.getDurationOfDataRecord() * accSampleRate);
            if (adsConfig.isAccelerometerOneChannelMode()) { // 1 accelerometer channels
                 edfConfig.addSignal(nrOfSamplesInEachDataRecord);
                int signalNumber = edfConfig.numberOfSignals() - 1;
                edfConfig.setLabel(signalNumber, "Accelerometer");
                edfConfig.setTransducer(signalNumber, "None");
                edfConfig.setPhysicalDimension(signalNumber, getAccelerometerPhysicalDimension(adsConfig.isAccelerometerOneChannelMode()));
                edfConfig.setPhysicalRange(signalNumber, getAccelerometerPhysicalMin(), getAccelerometerPhysicalMax());
                edfConfig.setDigitalRange(signalNumber, getAccelerometerDigitalMin(adsConfig.isAccelerometerOneChannelMode()), getAccelerometerDigitalMax(adsConfig.isAccelerometerOneChannelMode()));
            } else {
                String[] accelerometerChannelNames = {"Accelerometer X", "Accelerometer Y", "Accelerometer Z"};
                for (int i = 0; i < 3; i++) {     // 3 accelerometer channels
                    edfConfig.addSignal(nrOfSamplesInEachDataRecord);
                    int signalNumber = edfConfig.numberOfSignals() - 1;
                    edfConfig.setLabel(signalNumber, accelerometerChannelNames[i]);
                    edfConfig.setTransducer(signalNumber, "None");
                    edfConfig.setPhysicalDimension(signalNumber, getAccelerometerPhysicalDimension(adsConfig.isAccelerometerOneChannelMode()));
                    edfConfig.setPhysicalRange(signalNumber, getAccelerometerPhysicalMin(), getAccelerometerPhysicalMax());
                    edfConfig.setDigitalRange(signalNumber, getAccelerometerDigitalMin(adsConfig.isAccelerometerOneChannelMode()), getAccelerometerDigitalMax(adsConfig.isAccelerometerOneChannelMode()));
                   }
            }
        }
        if (adsConfig.isBatteryVoltageMeasureEnabled()) {
            int nrOfSamplesInEachDataRecord = 1;
            edfConfig.addSignal(nrOfSamplesInEachDataRecord);
            int signalNumber = edfConfig.numberOfSignals() - 1;
            edfConfig.setLabel(signalNumber, "Battery voltage");
            edfConfig.setTransducer(signalNumber, "None");
            edfConfig.setPhysicalDimension(signalNumber, getBatteryVoltageDimension());
            edfConfig.setPhysicalRange(signalNumber, getBatteryVoltagePhysicalMin(), getBatteryVoltagePhysicalMax());
            edfConfig.setDigitalRange(signalNumber, getBatteryVoltageDigitalMin(), getBatteryVoltageDigitalMax());
        }
        if (adsConfig.isLeadOffEnabled()) {
            int nrOfSamplesInEachDataRecord = 1;
            edfConfig.addSignal(nrOfSamplesInEachDataRecord);
            int signalNumber = edfConfig.numberOfSignals() - 1;
            edfConfig.setLabel(signalNumber, "Lead Off Status");
            edfConfig.setTransducer(signalNumber, "None");
            edfConfig.setPhysicalDimension(signalNumber, getLeadOffStatusDimension());
            edfConfig.setPhysicalRange(signalNumber, getLeadOffStatusPhysicalMin(), getLeadOffStatusPhysicalMax());
            edfConfig.setDigitalRange(signalNumber, getLeadOffStatusDigitalMin(), getLeadOffStatusDigitalMax());
         }
        return edfConfig;
    }


    /**
     * Helper method to convert digital value (integer) with lead-off info (last integer of data frame) to the bit-mask.
     * <p>
     * "Lead-Off" detection serves to alert/notify when an electrode is making poor electrical
     * contact or disconnecting. Therefore in Lead-Off detection mask
     * <br>TRUE means DISCONNECTED
     * <br>FALSE means CONNECTED
     * <br> NULL if the channel is disabled or its lead-off detection disabled or
     * its commutator state != "input".
     * <p>
     * Every ads-channel has 2 electrodes (Positive and Negative) so in leadOff detection mask:
     * <br>
     * element-0 and element-1 correspond to Positive and Negative electrodes of ads channel 0,
     * element-2 and element-3 correspond to Positive and Negative electrodes of ads channel 1,
     * ...
     * element-14 and element-15 correspond to Positive and Negative electrodes of ads channel 8.
     * <p>
     *
     * @param dataRecord - data record
     * @param adsConfig ads config during recording data
     * @return leadOff detection mask
     * @throws IllegalArgumentException if lead off detection is disabled in given AdsConfig
     */
    public static Boolean[] extractLeadOffBitMask(int[] dataRecord, AdsConfig adsConfig) throws IllegalArgumentException {
        if (!adsConfig.isLeadOffEnabled()) {
            String errMsg = "Lead off detection is disabled";
            throw new IllegalArgumentException(errMsg);
        }

        int leadOffInt = dataRecord[dataRecord.length - 1];
        int maskLength = 2 * adsConfig.getAdsChannelsCount(); // 2 electrodes for every channel
        Boolean[] bm = new Boolean[maskLength];

        if (adsConfig.getAdsChannelsCount() == 2) {
            int channel;
            for (int i = 0; i < bm.length; i++) {
                channel = i / 2;
                if(adsConfig.isAdsChannelEnabled(channel) && adsConfig.isAdsChannelLeadOffEnable(channel)
                        && adsConfig.getAdsChannelCommutatorState(channel).equals(Commutator.INPUT)) {
                    if (((leadOffInt >> i) & 1) == 1) {
                        bm[i] = true;
                    } else {
                        bm[i] = false;
                    }
                }
            }
            return bm;
        }

        if (adsConfig.getAdsChannelsCount() == 8) {
        /*
         * ads_8channel send lead-off status in different manner:
         * first byte - states of all negative electrodes from 8 channels
         * second byte - states of all positive electrodes from 8 channels
         */
            int channel;
            int electrode;
            for (int i = 0; i < bm.length; i++) {
                if (i < 8) { // first byte
                    channel = i;
                    electrode = 2 * channel + 1;

                } else { // second byte
                    channel = i - 8;
                    electrode = 2 * channel;
                }
                if(adsConfig.isAdsChannelEnabled(channel) && adsConfig.isAdsChannelLeadOffEnable(channel)
                        && adsConfig.getAdsChannelCommutatorState(channel).equals(Commutator.INPUT)) {
                    if (((leadOffInt >> i) & 1) == 1) {
                        bm[electrode] = true;
                    } else {
                        bm[electrode] = false;
                    }
                }
            }


            return bm;
        }

        String msg = "Invalid Ads channels count: " + adsConfig.getAdsChannelsCount() + ". Number of Ads channels should be 2 or 8";
        throw new IllegalArgumentException(msg);
    }

    /**
     * Helper method to extract buttery charge value from data record and convert it
     * to buttery percentage level.
     * @param dataRecord - data record
     * @param adsConfig ads config during recording data
     * @return battery level (percentage): 100, 90, 80, 70, 60, 50, 40, 30, 20, 10
     * @throws IllegalArgumentException if 1)battery voltage measuring disabled in given AdsConfig<br>
     * 2)batteryInt < BatteryDigitalMin (0) or batteryInt > BatteryDigitalMax (10240)
     */
    public static int extractLithiumBatteryPercentage(int[] dataRecord, AdsConfig adsConfig) throws IllegalArgumentException {
        if(! adsConfig.isBatteryVoltageMeasureEnabled()) {
            String errMsg = "Battery Voltage Measure is disabled";
            throw new IllegalArgumentException(errMsg);
        }
        int batteryCharge = dataRecord[dataRecord.length - 1];
        if (adsConfig.isLeadOffEnabled()) {
            batteryCharge = dataRecord[dataRecord.length - 2];
        }

        if (batteryCharge < getBatteryVoltageDigitalMin() || batteryCharge > getBatteryVoltageDigitalMax()) {
            String errMsg = "Invalid battery digital value: " + batteryCharge + " Expected > " + getBatteryVoltageDigitalMin() + " and <= " + getBatteryVoltageDigitalMax();
            throw new IllegalArgumentException(errMsg);
        }

        double lithiumBatteryPhysicalMax = 3.95;
        double lithiumBatteryPhysicalMin = 3.33;
        int lithiumBatteryDigitalMax = (int) (getBatteryVoltageDigitalMax() * lithiumBatteryPhysicalMax / getBatteryVoltagePhysicalMax());
        int lithiumBatteryDigitalMin = (int) (getBatteryVoltageDigitalMax() * lithiumBatteryPhysicalMin / getBatteryVoltagePhysicalMax());
        int percentage_max = 100;
        int percentage_min = 10;

        int percentage = percentage_min + (percentage_max - percentage_min) * (batteryCharge - lithiumBatteryDigitalMin) / (lithiumBatteryDigitalMax - lithiumBatteryDigitalMin);
        if (percentage > percentage_max) {
            percentage = percentage_max;
        }
        if (percentage < percentage_min) {
            percentage = percentage_min;
        }

        return percentage;
    }

    public static double getAdsChannelPhysicalMax(Gain channelGain) {
        return 2400000 / channelGain.getValue();
    }

    public static  double getAdsChannelPhysicalMin(Gain channelGain) {
        return - getAdsChannelPhysicalMax(channelGain);
    }

    public static int getAdsChannelsDigitalMax(int noiseDivider) {
        return Math.round(8388607 / noiseDivider);
    }

    public static int getAdsChannelsDigitalMin(int noiseDivider) {
        return Math.round(-8388608 / noiseDivider);
    }


    public String getAdsChannelsPhysicalDimension() {
        return "uV";
    }

    public static double getAccelerometerPhysicalMax() {
        return 1000;
    }

    public static double getAccelerometerPhysicalMin() {
        return - getAccelerometerPhysicalMax();
    }

    public static int getAccelerometerDigitalMax(boolean isAccelerometerOneChannelMode) {
        if(isAccelerometerOneChannelMode) {
            return 2000;
        }
        return 9610;
    }

    public static int getAccelerometerDigitalMin(boolean isAccelerometerOneChannelMode) {
        if(isAccelerometerOneChannelMode) {
            return -2000;
        }
        return 4190;
    }


    public static String getAccelerometerPhysicalDimension(boolean isAccelerometerOneChannelMode) {
        if(isAccelerometerOneChannelMode) {
            return "m/sec^3";
        }
        return "mg";
    }


    public static double getBatteryVoltagePhysicalMax() {
        return 5;
    }

    public static double getBatteryVoltagePhysicalMin() {
        return 0;
    }

    public static int getBatteryVoltageDigitalMax() {
        return 10240;
    }

    public static int getBatteryVoltageDigitalMin() {
        return 0;
    }

    public static String getBatteryVoltageDimension() {
        return "V";
    }

    public static double getLeadOffStatusPhysicalMax() {
        return 65536;
    }

    public static double getLeadOffStatusPhysicalMin() {
        return 0;
    }

    public static int getLeadOffStatusDigitalMax() {
        return 65536;
    }

    public static int getLeadOffStatusDigitalMin() {
        return 0;
    }

    public static String getLeadOffStatusDimension() {
        return "Bit mask";
    }

    class PingTask implements Runnable {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    communicationPort.sendCommand(adsType.adsPingCommand());
                    Thread.sleep(PING_PERIOD_MS);
                } catch (Exception ex) {
                    break;
                }
            }
        }
    }

    class MonitoringTask implements Runnable {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    communicationPort.sendCommand(adsType.adsHelloCommand());
                    Thread.sleep(MONITORING_PERIOD_MS);
                } catch (Exception ex) {
                    break;
                }
            }
        }

    }

    class NullMessageListener implements MessageListener {
        @Override
        public void onMessageReceived(AdsMessage messageType, String message) {
            // do nothing;
        }
    }

    class NullDataListener implements NumberedDataRecordListener {
        @Override
        public void onDataRecordReceived(int[] dataRecord, int dataRecordNumber) {
            // do nothing
        }
    }

}

