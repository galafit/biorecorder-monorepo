package biosignal.application;

import biosignal.filter.XYData;
import com.biorecorder.bichart.GroupingApproximation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainFacade implements Facade {
    public static final String[] FILE_EXTENSIONS = {"bdf", "edf"};
    private static final boolean IS_DATE_TIME = false; // true - time; false - indexes
    private DataProvider provider = new NullDataProvider();
    private DataStore dataStore = new DataStore();
    private Configurator configurator = new Configurator();
    private List<DataAppendListener> listeners = new ArrayList<>(1);

    @Override
    public void setDataProvider(File file) {
        provider.stop();
        provider.finish();
        this.provider = new EdfProvider(file);
        dataStore = configurator.configDataStore(provider);
        provider.addDataListener(0, new DataListener() {
            @Override
            public void receiveData(int[] data) {
                for (DataAppendListener l : listeners) {
                    l.onDataAppend();
                }
            }
        });
    }

    @Override
    public String[] getFileExtensions() {
        return FILE_EXTENSIONS;
    }

    @Override
    public void addDataAppendListener(DataAppendListener l) {
        listeners.add(l);
    }

    @Override
    public int[] getChartDataChannels1() {
        return configurator.getChartDataChannels1();
    }

    @Override
    public int[] getChartDataChannels2() {
        return configurator.getChartDataChannels2();
    }

    @Override
    public int[] getNavigatorDataChannels() {
        return configurator.getNavigatorDataChannels();
    }

    @Override
    public boolean isDateTime() {
        return IS_DATE_TIME;
    }

    @Override
    public XYData getData(int channel) {
        return dataStore.getData(channel);
    }

    @Override
    public GroupingApproximation getDataGroupingApproximation(int channel) {
        return dataStore.getDataGroupingApproximation(channel);
    }

    @Override
    public void start() {
        provider.start();
    }

    @Override
    public void stop() {
        provider.stop();
    }

    @Override
    public void finish() {
        provider.finish();
    }


    public void setReadInterval(int signal, long startPos, long samplesToRead) {
      //  provider.setReadInterval(signal, startPos, samplesToRead);
        dataStore = configurator.configDataStore(provider);
    }

    public void setReadTimeInterval(long readStartMs, long readIntervalMs) {
      //  provider.setReadTimeInterval(readStartMs, readIntervalMs);
        dataStore = configurator.configDataStore(provider);
    }

    public void setFullReadInterval() {
      //  provider.setFullReadInterval();
        dataStore = configurator.configDataStore(provider);
    }

    public String copyReadIntervalToFile() {
        return null;// provider.copyReadIntervalToFile();
    }
}