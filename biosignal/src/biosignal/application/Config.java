package biosignal.application;

import biosignal.filter.*;
import biosignal.filter.pipe.FilterPipe;
import com.biorecorder.bichart.GroupingApproximation;

public class Config {
    private static final boolean IS_DATE_TIME = false; // true - time; false - indexes

    public static DataStore configDataStore(EdfProvider provider) {
        DataStore dataStore = new DataStore(IS_DATE_TIME);
        long startTime = provider.getRecordingStartTimeMs();

        int ecgSignal = 0;
        double ecgSampleRate = provider.signalSampleRate(ecgSignal);
        double ecgSampleStepMs = 1000 / ecgSampleRate;
        int stepMs = 10;

        FilterPipe ecgFilterPipe = new FilterPipe(startTime, ecgSampleStepMs);
        provider.addListener(ecgSignal, ecgFilterPipe);

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

        int[] chartDataChannels1 = {0, 1, 2};
        int[] chartDataChannels2 = {3};
        int[] navigateDataChannels = {3};
        dataStore.setChartDataChannels1(chartDataChannels1);
        dataStore.setChartDataChannels2(chartDataChannels2);
        dataStore.setNavigatorDataChannels(navigateDataChannels);

        return dataStore;
    }

}
