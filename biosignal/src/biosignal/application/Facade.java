package biosignal.application;

import biosignal.filter.XYData;
import com.biorecorder.bichart.GroupingApproximation;

public interface Facade {
    void read();

    void finish();

    XYData getData(int channel);

    GroupingApproximation getDataGroupingApproximation(int channel);

    int[] getChartDataChannels1();

    int[] getChartDataChannels2();

    int[] getNavigatorDataChannels();

    boolean isDateTime();

   void setFullReadInterval();

    void setReadInterval(int signal, long startPos, long samplesToRead);

    void setReadTimeInterval(long readStartMs, long readIntervalMs);

    // return the name of new file
    String copyReadIntervalToFile();
}
