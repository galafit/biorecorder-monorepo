package biosignal.application;

import biosignal.filter.*;
import biosignal.filter.pipe.FilterPipe;
import com.biorecorder.bichart.GroupingApproximation;

public class Config {
    private static final boolean IS_DATE_TIME = false;

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

        int[] showDataChannels = {0, 1, 2, 3};
        int[] navigateDataChannels = {3};
        dataStore.setShowDataChannels(showDataChannels);
        dataStore.setNavigateDataChannels(navigateDataChannels);

        /*int accSignal = 1;
        double accSampleRate = provider.signalSampleRate(accSignal);
        double accSampleStepMs = 1000 / accSampleRate;
        FilterPipe accFilterPipe = new FilterPipe(startTime, ecgSampleStepMs);
        provider.addListener(accSignal, accFilterPipe);
        XYData acc = accFilterPipe.accumulateData();
        dataStore.addDataChannel("accelerometer", acc);*/
        return dataStore;
    }

}
