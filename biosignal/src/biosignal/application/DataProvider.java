package biosignal.application;

public interface DataProvider {
    void finish();
    void addDataListener(int signal, DataListener l);
    void addConfigListener(ProviderConfigListener l);
}
