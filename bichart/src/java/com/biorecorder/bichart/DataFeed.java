package com.biorecorder.bichart;

public interface DataFeed {
    /**
     * get full data
     */
    XYSeries getData();
    /**
     * get data with xValue >= xFrom and xValue < xTill
     */
    XYSeries getData(double xFrom, double xTill);
    Range dataXRange();
    int numberOfDataPoints();
    void addDataListener(DataFeedListener l);
}
