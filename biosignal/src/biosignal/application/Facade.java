package biosignal.application;

import biosignal.filter.XYData;

public interface Facade {
    void read();

    void finish();

    XYData getData(int channel);

   void setFullReadInterval();

    void setReadInterval(int signal, long startPos, long samplesToRead);

    void setReadTimeInterval(long readStartMs, long readIntervalMs);

    // return the name of new file
    String copyReadIntervalToFile();

    int getCanalsCount();
}
