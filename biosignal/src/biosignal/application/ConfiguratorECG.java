package biosignal.application;

import biosignal.filter.*;
import biosignal.filter.pipe.FilterPipe;
import com.biorecorder.bichart.GroupingApproximation;

import java.util.HashMap;
import java.util.Map;

public class ConfiguratorECG implements Configurator{
    private int[] chartDataChannels1 = new int[0];
    private int[] chartDataChannels2 = new int[0];
    private int[] navigatorDataChannels = new int[0];

    @Override
    public Map<Integer, FilterPipe> configDataStore(ProviderConfig providerConfig, DataStore dataStore) {
        Map<Integer, FilterPipe> signalToPipeFilter = new HashMap<>(1);
        long startTime = providerConfig.getRecordingStartTimeMs();

        int ecgSignal = 0;
        double ecgSampleRate = providerConfig.signalSampleRate(ecgSignal);
        double ecgSampleStepMs = 1000 / ecgSampleRate;
        int stepMs = 10;

        FilterPipe ecgFilterPipe = new FilterPipe(startTime, ecgSampleStepMs);
        signalToPipeFilter.put(ecgSignal, ecgFilterPipe);

        XYData ecg = ecgFilterPipe.accumulateData();
        dataStore.addDataChannel("ecg", ecg, GroupingApproximation.HIGH);

        XYData ecgDeriv = ecgFilterPipe.then(new DerivateFilter(ecgSampleRate, stepMs)).
                then(new PeakFilter()).accumulateData();
        dataStore.addDataChannel("ecg derivate", ecgDeriv, GroupingApproximation.HIGH);

        XYData ecgQRS = ecgFilterPipe.then(new QRSFilter(ecgSampleRate)).accumulateData();
        dataStore.addDataChannel("ecg QRS", ecgQRS, GroupingApproximation.HIGH);

        XYData ecgRhythm = ecgFilterPipe.then(new RhythmBiFilter()).accumulateData();
        dataStore.addDataChannel("ecg Rhythm", ecgRhythm, GroupingApproximation.HIGH);

        /*int accSignal = 1;
        double accSampleRate = provider.signalSampleRate(accSignal);
        double accSampleStepMs = 1000 / accSampleRate;
        FilterPipe accFilterPipe = new FilterPipe(startTime, accSampleStepMs);
        provider.addListener(accSignal, accFilterPipe);
        XYData acc = accFilterPipe.accumulateData();
        dataStore.addDataChannel("accelerometer", acc);*/

        chartDataChannels1 = new int[] {0, 1, 2};
        chartDataChannels2 = new int[] {3};
        navigatorDataChannels = new int[] {3};

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
