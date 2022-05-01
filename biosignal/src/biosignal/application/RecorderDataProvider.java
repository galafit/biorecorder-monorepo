package biosignal.application;

import com.biorecorder.bdfrecorder.EdfBioRecorderApp;
import com.biorecorder.bdfrecorder.JsonPreferences;
import com.biorecorder.bdfrecorder.RecorderViewModelImpl;
import com.biorecorder.bdfrecorder.gui.RecorderViewModel;
import com.biorecorder.datalyb.time.TimeInterval;
import com.biorecorder.edflib.DataHeader;
import com.biorecorder.edflib.DataRecordStream;

import java.util.ArrayList;
import java.util.List;

public class RecorderDataProvider implements DataProvider{
    private final RecorderViewModel recorderViewModel;
    private volatile DataHeader header;
    private List<SignalDataListener>[] dataListeners = new List[8];
    private List<ProviderConfigListener> providerConfigListeners = new ArrayList<>(1);
    private DataRecordListener dataRecordListener = new NullDataRecordListener();

    public RecorderDataProvider() {
        JsonPreferences preferences = new JsonPreferences();
        EdfBioRecorderApp bioRecorderApp = new EdfBioRecorderApp();
        recorderViewModel = new RecorderViewModelImpl(bioRecorderApp, preferences);
        for (int i = 0; i < dataListeners.length; i++) {
            dataListeners[i] = new ArrayList<>();
        }
        bioRecorderApp.addDataListener(new DataRecordStream() {
            @Override
            public void setHeader(DataHeader header) {
                RecorderDataProvider.this.header = header;
                notifyConfigListeners();
            }

            @Override
            public void writeDataRecord(int[] dataRecord) {
                if(header == null) {
                    return;
                }
                int from = 0;
                for (int i = 0; i < header.numberOfSignals(); i++) {
                    List<SignalDataListener> signalListeners = dataListeners[i];
                    int signalSamples = header.getNumberOfSamplesInEachDataRecord(i);
                    for (SignalDataListener l : signalListeners) {
                        l.receiveData(dataRecord, from, signalSamples);
                    }
                    from += signalSamples;
                }
                dataRecordListener.receiveData(dataRecord);
            }

            @Override
            public void close() {
                //do nothing
            }
        });
    }

    @Override
    public void addConfigListener(ProviderConfigListener l) {
        providerConfigListeners.add(l);
    }

    private void notifyConfigListeners() {
        ProviderConfig config = new ProviderConfig() {
            @Override
            public int signalsCount() {
                return header.numberOfSignals();
            }

            @Override
            public double signalSampleRate(int signal) {
                return header.getSampleFrequency(signal);
            }

            @Override
            public long getRecordingStartTimeMs() {
                return header.getRecordingStartTimeMs();
            }

            @Override
            public long getRecordingTimeMs() {
                int recordDuration = 9; // hours
                return recordDuration * TimeInterval.HOUR_1.toMilliseconds();
            }
        };
        for (ProviderConfigListener providerConfigListener : providerConfigListeners) {
            providerConfigListener.receiveConfig(config);
        }
    }

    public RecorderViewModel getRecorderViewModel() {
        return recorderViewModel;
    }

    @Override
    public void finish() {
       recorderViewModel.stop();
    }

    @Override
    public void addDataRecordListener(DataRecordListener l) {
        dataRecordListener = l;
    }

    @Override
    public void addSignalDataListener(int signal, SignalDataListener l) {
        if (signal < dataListeners.length) {
            List<SignalDataListener> signalListeners = dataListeners[signal];
            signalListeners.add(l);
        }
    }
 }
