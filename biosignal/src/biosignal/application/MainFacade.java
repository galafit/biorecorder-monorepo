package biosignal.application;

import biosignal.filter.XYData;

public class MainFacade implements Facade {
    private EdfProvider provider;
    private DataStore dataStore;



    public MainFacade(EdfProvider provider) {
        this.provider = provider;
        dataStore = Config.configDataStore(provider);
    }

    @Override
    public int[] getChartDataChannels1() {
        return dataStore.getChartDataChannels1();
    }

    @Override
    public int[] getChartDataChannels2() {
        return dataStore.getChartDataChannels2();
    }

    @Override
    public int[] getNavigatorDataChannels() {
        return dataStore.getNavigatorDataChannels();
    }

    @Override
    public boolean isDateTime() {
        return dataStore.isDateTime();
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
        dataStore = Config.configDataStore(provider);
    }

    @Override
    public void setReadTimeInterval(long readStartMs, long readIntervalMs) {
        provider.setReadTimeInterval(readStartMs, readIntervalMs);
        dataStore = Config.configDataStore(provider);
    }

    @Override
    public void setFullReadInterval() {
        provider.setFullReadInterval();
        dataStore = Config.configDataStore(provider);
    }

    @Override
    public String copyReadIntervalToFile() {
        return provider.copyReadIntervalToFile();
    }
}