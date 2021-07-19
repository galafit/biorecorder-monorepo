package com.biorecorder.data.frame;


/**
 * Created by galafit on 28/4/19.
 */
public enum TimeUnit {
    MILLISECOND(1),
    SECOND(1000),
    MINUTE(1000L * 60),
    HOUR(1000L * 60 * 60),
    DAY(1000L * 60 * 60 * 24),
    WEEK(1000L * 60 * 60 * 24 * 7),
    MONTH(1000L * 60 * 60 * 24 * 30),
    YEAR(1000L * 60 * 60 * 24 * 365);

    long milliseconds;

    TimeUnit(long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public long toMilliseconds() {
        return milliseconds;
    }
}
