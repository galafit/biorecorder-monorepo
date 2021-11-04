package biosignal.application;

public interface DataProvider {
    void start();
    void stop();
    void finish();
    int signalsCount();
    void addDataListener(int signal, DataListener l);
    double signalSampleRate(int signal);
    long getRecordingStartTimeMs();
}
