package biosignal.application;

import biosignal.filter.XYData;
import biosignal.filter.pipe.FilterPipe;
import com.biorecorder.bdfrecorder.gui.RecorderViewModel;
import com.biorecorder.bichart.GroupingApproximation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainFacade implements Facade {
    public static final String[] FILE_EXTENSIONS = {"bdf", "edf"};
    private DataProvider dataProvider = new NullDataProvider();
    private DataStore dataStore;
    private Configurator configurator;
    private List<DataAppendListener> dataAppendListeners = new ArrayList<>(1);
    private List<ProviderConfigListener> configListeners = new ArrayList<>(1);
    private final boolean isDateTime; // true - time; false - indexes
    private int numberOfSignalsWithListeners;
    private int dataCount;


    public MainFacade(Configurator configurator, boolean isDateTime) {
        this.configurator = configurator;
        this.isDateTime = isDateTime;
    }

    @Override
    public void chooseFileDataProvider(File file, boolean isParallel) {
        dataProvider.finish();
        if(isParallel) {
            FileDataProviderParallel fileDataProvider = new FileDataProviderParallel(file);
            dataProvider = fileDataProvider;
            configureDataProvide(dataProvider);
            fileDataProvider.start();
        } else {
            FileDataProvider fileDataProvider = new FileDataProvider(file);
            dataProvider = fileDataProvider;
            configureDataProvide(dataProvider);
            fileDataProvider.start();
        }

    }

    @Override
    public RecorderViewModel chooseRecorderDataProvider() {
        dataProvider.finish();
        RecorderDataProvider recorderDataProvider = new RecorderDataProvider();
        dataProvider = recorderDataProvider;
        configureDataProvide(dataProvider);
        return recorderDataProvider.getRecorderViewModel();
    }

    private void configureDataProvide(DataProvider dataProvider1) {
        dataProvider1.addConfigListener(new ProviderConfigListener() {
            @Override
            public void receiveConfig(ProviderConfig providerConfig1) {
                dataStore = new DataStore();

                ProviderConfig providerConfig = providerConfig1;
                if(!isDateTime) {
                    providerConfig = new ProviderConfig() {
                        @Override
                        public int signalsCount() {
                            return providerConfig1.signalsCount();
                        }

                        @Override
                        public double signalSampleRate(int signal) {
                            return providerConfig1.signalSampleRate(signal);
                        }

                        @Override
                        public long getRecordingStartTimeMs() {
                            return 0;
                        }

                        @Override
                        public long getRecordingTimeMs() {
                            return providerConfig1.getRecordingTimeMs();
                        }
                    };
                }
                Map<Integer, FilterPipe> signalToPipeFilter = configurator.configDataStore(providerConfig, dataStore);
                numberOfSignalsWithListeners = signalToPipeFilter.keySet().size();
                for (Integer signal : signalToPipeFilter.keySet()) {
                    FilterPipe fp = signalToPipeFilter.get(signal);
                    dataProvider1.addSignalDataListener(signal, new SignalDataListener() {
                        @Override
                        public void receiveData(int[] data, int from, int length) {
                            dataCount++;
                            fp.receiveData(data, from, length);
                        }
                    });
                    dataProvider1.addDataRecordListener(new DataRecordListener() {
                        @Override
                        public void receiveData(int[] data) {
                            for (DataAppendListener l : dataAppendListeners) {
                                l.onDataAppend();
                            }
                        }
                    });
                }
                for (ProviderConfigListener configListener : configListeners) {
                    configListener.receiveConfig(providerConfig);
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
        dataAppendListeners.add(l);
    }

    @Override
    public void addProviderConfigListener(ProviderConfigListener l) {
        configListeners.add(l);
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
        return isDateTime;
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
    public void finish() {
        if( dataProvider!= null) {
            dataProvider.finish();
        }
    }


    public void setReadInterval(int signal, long startPos, long samplesToRead) {
      //  provider.setReadInterval(signal, startPos, samplesToRead);
    }

    public void setReadTimeInterval(long readStartMs, long readIntervalMs) {
      //  provider.setReadTimeInterval(readStartMs, readIntervalMs);
    }

    public void setFullReadInterval() {
      //  provider.setFullReadInterval();
    }

    public String copyReadIntervalToFile() {
        return null;// provider.copyReadIntervalToFile();
    }
}