package biosignal.application;

import biosignal.filter.XYData;
import biosignal.filter.pipe.FilterPipe;
import com.biorecorder.bichart.GroupingApproximation;

import java.util.HashMap;
import java.util.Map;

public class ConfiguratorTestForAdc implements Configurator{
    private int[] chartDataChannels1 = new int[0];
    private int[] chartDataChannels2 = new int[0];
    private int[] navigatorDataChannels = new int[0];

    @Override
    public Map<Integer, FilterPipe> configDataStore(ProviderConfig providerConfig, DataStore dataStore) {

        Map<Integer, FilterPipe> signalToPipeFilter = new HashMap<>(1);

        long startTime = providerConfig.getRecordingStartTimeMs();
        int cutOffPeriod = 1; //sec.

// ###### signal = 0; ADS  Chanel 1 ##########
        int signal = 0;
        double sampleRate = providerConfig.signalSampleRate(signal);
        double sampleStepMs = 1000 / sampleRate;

/*        FilterPipe filterPipe0 = new FilterPipe(startTime, sampleStepMs);
        filterPipe0.then(new HiPassFilter(cutOffPeriod, sampleRate));
        XYData data0 = filterPipe0.accumulateData();
        dataStore.addDataChannel("signal_0", data0, GroupingApproximation.HIGH);
        signalToPipeFilter.put(signal, filterPipe0);*/
// ###### signal = 1; ADS  Chanel 2 ##########
        signal = 1;
        sampleRate = providerConfig.signalSampleRate(signal);
        sampleStepMs = 1000 / sampleRate;
        FilterPipe filterPipe1 = new FilterPipe(startTime, sampleStepMs);
//        filterPipe1.then(new HiPassFilter(cutOffPeriod, sampleRate));
        XYData data1 = filterPipe1.accumulateData();
        dataStore.addDataChannel("signal_1", data1, GroupingApproximation.AVERAGE);
        signalToPipeFilter.put(signal, filterPipe1);

// ###### signal = 2; Chanel acc 1 ##########
        signal = 2;
        sampleRate = providerConfig.signalSampleRate(signal);
        sampleStepMs = 1000 / sampleRate;
        FilterPipe filterPipe2 = new FilterPipe(startTime, sampleStepMs);
//       filterPipe2.then(new HiPassFilter(cutOffPeriod, sampleRate));
        XYData data2 = filterPipe2.accumulateData();
        dataStore.addDataChannel("acc_1", data2, GroupingApproximation.AVERAGE);
        signalToPipeFilter.put(signal, filterPipe2);

        // ###### signal = 3; Chanel acc 2 ##########
        signal = 3;
        sampleRate = providerConfig.signalSampleRate(signal);
        sampleStepMs = 1000 / sampleRate;
        FilterPipe filterPipe3 = new FilterPipe(startTime, sampleStepMs);
//       filterPipe3.then(new HiPassFilter(cutOffPeriod, sampleRate));
        XYData data3 = filterPipe3.accumulateData();
        dataStore.addDataChannel("acc_2", data3, GroupingApproximation.AVERAGE);
        signalToPipeFilter.put(signal, filterPipe3);

        // ###### signal = 4; Chanel acc 3 ##########
        signal = 4;
        sampleRate = providerConfig.signalSampleRate(signal);
        sampleStepMs = 1000 / sampleRate;
        FilterPipe filterPipe4 = new FilterPipe(startTime, sampleStepMs);
//       filterPipe4.then(new HiPassFilter(cutOffPeriod, sampleRate));
        XYData data4 = filterPipe4.accumulateData();
        dataStore.addDataChannel("acc_3", data4, GroupingApproximation.AVERAGE);
        signalToPipeFilter.put(signal, filterPipe4);


        chartDataChannels1 = new int[] {0,  1, 2, 3, 4};

        //navigatorDataChannels = new int[] {3};

        return signalToPipeFilter;
    }

    @Override
    public int[] getChartDataChannels1() {
        return chartDataChannels1;
    }
    @Override
    public int[] getChartDataChannels2() {
        return chartDataChannels2;
    }
    @Override
    public int[] getNavigatorDataChannels() {
        return navigatorDataChannels;
    }
}
