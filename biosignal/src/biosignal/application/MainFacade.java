package biosignal.application;

import biosignal.application.filter.*;

public class MainFacade implements Facade {
    private EdfProvider provider;
    private DataStore dataStore;

    public MainFacade(EdfProvider provider) {
        this.provider = provider;
        dataStore = createDataStore(provider);
    }

    private static DataStore createDataStore(EdfProvider provider) {
        DataStore ds = new DataStore(provider);
        int stepMs = 10;
        int signal = 0;
        double signalSampleRate = provider.signalSampleRate(signal);
        ds.addDataChannel(signal);
        ds.addDataChannel(signal, new DerivateFilter(signalSampleRate, stepMs),
                new PeakFilter());
        ds.addDataChannel(signal, new DerivateFilter(signalSampleRate, stepMs),
                new PeakFilter(), new QRSFilter(signalSampleRate));

        FilterChain fc = new FilterChain(new DerivateFilter(signalSampleRate, stepMs),
                new PeakFilter(), new QRSFilter(signalSampleRate));
        ds.addDataChannel(signal,fc ,
                new RhythmBiFilter());

        signal = 1;
        ds.addDataChannel(signal);
        return ds;

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