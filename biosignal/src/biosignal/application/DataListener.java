package biosignal.application;

public interface DataListener {
    void receiveData(int[] data, int from, int length);
}

