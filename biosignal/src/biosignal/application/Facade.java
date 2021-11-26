package biosignal.application;

import biosignal.filter.XYData;
import com.biorecorder.bdfrecorder.gui.RecorderViewModel;
import com.biorecorder.bichart.GroupingApproximation;

import java.io.File;

public interface Facade {
    void start();
    void stop();

    void setDataProvider(File file);

    RecorderViewModel getRecorder();

    String[] getFileExtensions();

    void addDataAppendListener(DataAppendListener l);

    XYData getData(int channel);

    GroupingApproximation getDataGroupingApproximation(int channel);

    int[] getChartDataChannels1();

    int[] getChartDataChannels2();

    int[] getNavigatorDataChannels();

    boolean isDateTime();

    void setFullReadInterval();

    void setReadInterval(int signal, long startPos, long samplesToRead);

    void setReadTimeInterval(long readStartMs, long readIntervalMs);

    void finish();

    // return the name of new file
    String copyReadIntervalToFile();
}
