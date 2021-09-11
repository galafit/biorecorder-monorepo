package biosignal.application;

import biosignal.application.filter.*;

import java.util.ArrayList;
import java.util.List;

public class DataStore {
    private EdfProvider provider;
    private List<XYData> dataList = new ArrayList();

    public DataStore(EdfProvider provider) {
        this.provider = provider;
    }

    public void addDataChannel(int signal) {
        FilterPipe filterPipe = new FilterPipe(provider.getRecordingStartTimeMs(), sampleStepMs(signal), new NullFilter());
        provider.addListener(signal, new ProviderListener(filterPipe));
        XYData xyData = filterPipe.enableDataAccumulation();
        dataList.add(xyData);
    }

    public void addDataChannel(int signal, Filter filter, BiFilter biFilter){
       FilterPipe filterPipe = new FilterPipe(provider.getRecordingStartTimeMs(), sampleStepMs(signal), filter);
       provider.addListener(signal, new ProviderListener(filterPipe));
       BiFilterPipe biFilterPipe = new BiFilterPipe(biFilter);
       filterPipe.addXYReceiver(biFilterPipe);
       XYData xyData = biFilterPipe.enableDataAccumulation();
       dataList.add(xyData);
    }

    public void addDataChannel(int signal, Filter... filters) {
        FilterPipe filterPipe = new FilterPipe(provider.getRecordingStartTimeMs(), sampleStepMs(signal), filters);
        provider.addListener(signal, new ProviderListener(filterPipe));
        XYData xyData = filterPipe.enableDataAccumulation();
        dataList.add(xyData);
    }

    private double sampleStepMs(int signal) {
       return 1000 / provider.signalSampleRate(signal);
    }

    public XYData getData(int channel) {
        XYData d = dataList.get(channel);
        return dataList.get(channel);
    }

    public int dataChannelCount() {
        return dataList.size();
    }

    static class ProviderListener implements DataListener {
        FilterPipe filterPipe;

        public ProviderListener(FilterPipe filterPipe) {
            this.filterPipe = filterPipe;
        }

        @Override
        public void receiveData(int[] data) {
            filterPipe.put(data);
        }
    }

}