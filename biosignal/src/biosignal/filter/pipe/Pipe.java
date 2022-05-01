package biosignal.filter.pipe;

import biosignal.filter.XYData;

interface Pipe {
    void addYReceiver(YReceiver yReceiver);
    void addXYReceiver(XYReceiver XYReceiver);
    XYData enableDataAccumulation();
}
