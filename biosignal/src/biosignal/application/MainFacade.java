package biosignal.application;

import biosignal.filter.*;
import biosignal.filter.XYData;
import biosignal.filter.pipe.FilterPipe;

public class MainFacade implements Facade {
    private EdfProvider provider;
    private DataStore dataStore;

    public MainFacade(EdfProvider provider) {
        this.provider = provider;
        dataStore = createDataStore(provider);
    }

    private static DataStore createDataStore(EdfProvider provider) {
        DataStore dataStore = new DataStore();
        long startTime = provider.getRecordingStartTimeMs();

        int ecgSignal = 0;
        double ecgSampleRate = provider.signalSampleRate(ecgSignal);
        double ecgSampleStepMs = 1000 / ecgSampleRate;
        int stepMs = 10;

        FilterPipe ecgFilterPipe = new FilterPipe(startTime, ecgSampleStepMs);
        provider.addListener(ecgSignal, ecgFilterPipe);

        XYData ecg = ecgFilterPipe.accumulateData();
        dataStore.addDataChannel("ecg", ecg);

        XYData ecgDeriv = ecgFilterPipe.then(new DerivateFilter(ecgSampleRate, stepMs)).
                then(new PeakFilter()).accumulateData();
        dataStore.addDataChannel("ecg derivate", ecgDeriv);

        XYData ecgQRS = ecgFilterPipe.then(new QRSFilter(ecgSampleRate)).accumulateData();
        dataStore.addDataChannel("ecg QRS", ecgQRS);

        XYData ecgRhythm = ecgFilterPipe.then(new RhythmBiFilter()).accumulateData();
        dataStore.addDataChannel("ecg Rhythm", ecgRhythm);

        /*int accSignal = 1;
        double accSampleRate = provider.signalSampleRate(accSignal);
        double accSampleStepMs = 1000 / accSampleRate;
        FilterPipe accFilterPipe = new FilterPipe(startTime, ecgSampleStepMs);
        provider.addListener(accSignal, accFilterPipe);
        XYData acc = accFilterPipe.accumulateData();
        dataStore.addDataChannel("accelerometer", acc);*/
        return dataStore;
    }

    @Override
    public XYData getData(int channel) {
        return dataStore.getData(channel);
    }

    @Override
    public void read() {
        provider.read();
    }

    @Override
    public void finish() {
        provider.finish();
    }

    @Override
    public void setReadInterval(int signal, long startPos, long samplesToRead) {
        provider.setReadInterval(signal, startPos, samplesToRead);
        dataStore = createDataStore(provider);
    }

    @Override
    public void setReadTimeInterval(long readStartMs, long readIntervalMs) {
        provider.setReadTimeInterval(readStartMs, readIntervalMs);
        dataStore = createDataStore(provider);
    }

    @Override
    public void setFullReadInterval() {
        provider.setFullReadInterval();
        dataStore = createDataStore(provider);
    }

    @Override
    public String copyReadIntervalToFile() {
        return provider.copyReadIntervalToFile();
    }

    @Override
    public int getCanalsCount() {
        return dataStore.dataChannelCount();
    }
}