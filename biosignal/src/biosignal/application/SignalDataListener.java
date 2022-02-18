package biosignal.application;

public interface SignalDataListener {
    void receiveData(int[] data, int from, int length);
}

