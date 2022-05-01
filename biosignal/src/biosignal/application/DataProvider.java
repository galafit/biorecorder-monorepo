package biosignal.application;

public interface DataProvider {
    void finish();
    void addSignalDataListener(int signal, SignalDataListener l);
    void addConfigListener(ProviderConfigListener l);
    void addDataRecordListener(DataRecordListener l);
}
