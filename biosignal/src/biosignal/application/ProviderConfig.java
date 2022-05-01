package biosignal.application;

public interface ProviderConfig {
    int signalsCount();
    double signalSampleRate(int signal);
    long getRecordingStartTimeMs();
    long getRecordingTimeMs();
}
