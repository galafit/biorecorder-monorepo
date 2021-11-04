package biosignal.application;

public class NullDataProvider implements DataProvider{
    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void finish() {

    }

    @Override
    public int signalsCount() {
        return 0;
    }

    @Override
    public void addDataListener(int signal, DataListener l) {

    }

    @Override
    public double signalSampleRate(int signal) {
        return 1;
    }

    @Override
    public long getRecordingStartTimeMs() {
        return 0;
    }
}
