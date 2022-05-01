package biosignal.application;

import biosignal.filter.*;
import biosignal.filter.pipe.FilterPipe;
import com.biorecorder.bichart.GroupingApproximation;
import biosignal.filter.XYData;

import java.util.Map;

public interface Configurator {
    public Map<Integer, FilterPipe> configDataStore(ProviderConfig config, DataStore dataStore);

    public int[] getChartDataChannels1();

    public int[] getChartDataChannels2();

    public int[] getNavigatorDataChannels();
}
