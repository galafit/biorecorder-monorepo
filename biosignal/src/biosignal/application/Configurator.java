package biosignal.application;

import biosignal.filter.*;
import biosignal.filter.pipe.FilterPipe;
import com.biorecorder.bichart.GroupingApproximation;
import biosignal.filter.XYData;

public class Configurator {
    private int[] chartDataChannels1 = new int[0];
    private int[] chartDataChannels2 = new int[0];;
    private int[] navigatorDataChannels = new int[0];;


    public DataStore configDataStore(DataProvider provider) {
        DataStore dataStore = new DataStore();
        long startTime = provider.getRecordingStartTimeMs();

        int ecgSignal = 0;
        double ecgSampleRate = provider.signalSampleRate(ecgSignal);
        double ecgSampleStepMs = 1000 / ecgSampleRate;
        int stepMs = 10;

        FilterPipe ecgFilterPipe = new FilterPipe(startTime, ecgSampleStepMs);
        provider.addDataListener(ecgSignal, ecgFilterPipe);

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
        return dataStore;
    }

    public int[] getChartDataChannels1() {
        return chartDataChannels1;
    }

    public int[] getChartDataChannels2() {
        return chartDataChannels2;
    }

    public int[] getNavigatorDataChannels() {
        return navigatorDataChannels;
    }
}
