package com.biorecorder.bichart.configs;

import com.biorecorder.data.time.TimeInterval;

public enum TimeGroupInterval {
    MILLISECOND_1(TimeInterval.MILLISECOND_1),
    MILLISECOND_2(TimeInterval.MILLISECOND_2),
    MILLISECOND_5(TimeInterval.MILLISECOND_5),
    MILLISECOND_10(TimeInterval.MILLISECOND_10),
    MILLISECOND_20(TimeInterval.MILLISECOND_20),
    MILLISECOND_25(TimeInterval.MILLISECOND_25),
    MILLISECOND_50(TimeInterval.MILLISECOND_50),
    MILLISECOND_100(TimeInterval.MILLISECOND_100),
    MILLISECOND_200(TimeInterval.MILLISECOND_200),
    MILLISECOND_250(TimeInterval.MILLISECOND_250),
    MILLISECOND_500(TimeInterval.MILLISECOND_500),
    SECOND_1(TimeInterval.SECOND_1),
    SECOND_2(TimeInterval.SECOND_2),
    SECOND_5(TimeInterval.SECOND_5),
    SECOND_10(TimeInterval.SECOND_10),
    SECOND_15(TimeInterval.SECOND_15),
    SECOND_30(TimeInterval.SECOND_30),
    MINUTE_1(TimeInterval.MINUTE_1),
    MINUTE_2(TimeInterval.MINUTE_2),
    MINUTE_5(TimeInterval.MINUTE_5),
    MINUTE_10(TimeInterval.MINUTE_10),
    MINUTE_15(TimeInterval.MINUTE_15),
    MINUTE_30(TimeInterval.MINUTE_30),
    HOUR_1(TimeInterval.HOUR_1),
    HOUR_2(TimeInterval.HOUR_2),
    HOUR_3(TimeInterval.HOUR_3),
    HOUR_4(TimeInterval.HOUR_4),
    HOUR_6(TimeInterval.HOUR_6),
    HOUR_8(TimeInterval.HOUR_8),
    HOUR_12(TimeInterval.HOUR_12),
    DAY(TimeInterval.DAY),
    WEEK(TimeInterval.WEEK),
    MONTH_1(TimeInterval.MONTH_1),
    MONTH_3(TimeInterval.MONTH_3),
    MONTH_6(TimeInterval.MONTH_6),
    YEAR(TimeInterval.YEAR);

    private TimeInterval timeInterval;

    private TimeGroupInterval(TimeInterval timeInterval) {
        this.timeInterval = timeInterval;
    }

    public TimeInterval getTimeInterval() {
        return timeInterval;
    }

    public long toMilliseconds() {
        return timeInterval.toMilliseconds();
    }
}
