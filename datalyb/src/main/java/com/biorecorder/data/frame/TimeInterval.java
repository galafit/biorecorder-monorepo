package com.biorecorder.data.frame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * from d3 scales (https://github.com/d3/d3-scale)
 * The following time intervals are considered for automatic ticks:
 1-, 5-, 15- and 30-second.
 1-, 5-, 15- and 30-minute.
 1-, 3-, 6- and 12-hour.
 1- and 2-day.
 1-week.
 1- and 3-month.
 1-year.


    %Y - for year boundaries, such as 2011.
    %B - for month boundaries, such as February.
    %b %d - for week boundaries, such as Feb 06.
    %a %d - for day boundaries, such as Mon 07.
    %I %p - for hour boundaries, such as 01 AM.
    %I:%M - for minute boundaries, such as 01:23.
    :%S - for second boundaries, such as :45.
    .%L - milliseconds for all other times, such as .012.


 */
public enum TimeInterval {
    MILLISECOND_1(TimeUnit.MILLISECOND, 1),
    MILLISECOND_2(TimeUnit.MILLISECOND, 2),
    MILLISECOND_5(TimeUnit.MILLISECOND, 5),
    MILLISECOND_10(TimeUnit.MILLISECOND, 10),
    MILLISECOND_20(TimeUnit.MILLISECOND, 20),
    MILLISECOND_25(TimeUnit.MILLISECOND, 25),
    MILLISECOND_50(TimeUnit.MILLISECOND, 50),
    MILLISECOND_100(TimeUnit.MILLISECOND, 100),
    MILLISECOND_200(TimeUnit.MILLISECOND, 200),
    MILLISECOND_250(TimeUnit.MILLISECOND, 250),
    MILLISECOND_500(TimeUnit.MILLISECOND, 500),
    SECOND_1(TimeUnit.SECOND, 1),
    SECOND_2(TimeUnit.SECOND, 2),
    SECOND_5(TimeUnit.SECOND, 5),
    SECOND_10(TimeUnit.SECOND, 10),
    SECOND_15(TimeUnit.SECOND, 15),
    SECOND_30(TimeUnit.SECOND, 30),
    MINUTE_1(TimeUnit.MINUTE, 1),
    MINUTE_2(TimeUnit.MINUTE, 2),
    MINUTE_5(TimeUnit.MINUTE, 5),
    MINUTE_10(TimeUnit.MINUTE, 10),
    MINUTE_15(TimeUnit.MINUTE, 15),
    MINUTE_30(TimeUnit.MINUTE, 30),
    HOUR_1(TimeUnit.HOUR, 1),
    HOUR_2(TimeUnit.HOUR, 2),
    HOUR_3(TimeUnit.HOUR, 3),
    HOUR_4(TimeUnit.HOUR, 4),
    HOUR_6(TimeUnit.HOUR, 6),
    HOUR_8(TimeUnit.HOUR, 8),
    HOUR_12(TimeUnit.HOUR, 12),
    DAY(TimeUnit.DAY, 1),
    WEEK(TimeUnit.WEEK, 1),
    MONTH_1(TimeUnit.MONTH, 1),
    MONTH_3(TimeUnit.MONTH, 3),
    MONTH_6(TimeUnit.MONTH, 6),
    YEAR(TimeUnit.YEAR, 1);

    private final TimeUnit timeUnit;
    private final int timeUnitMultiplier;

    TimeInterval(TimeUnit timeUnit, int timeUnitMultiplier) {
        this.timeUnit = timeUnit;
        this.timeUnitMultiplier = timeUnitMultiplier;
    }

    public TimeUnit timeUnit() {
        return timeUnit;
    }

    public int unitMultiplier() {
        return timeUnitMultiplier;
    }

    public long toMilliseconds() {
        return timeUnitMultiplier * timeUnit.toMilliseconds();
    }

    public static TimeInterval getClosest(long interval, boolean isWeekIncluded) {
        List<TimeInterval> timeIntervals = Arrays.asList(values());
        if (!isWeekIncluded) {
            List<TimeInterval> timeIntervals1 = new ArrayList<>(timeIntervals.size() - 1);
            for (TimeInterval timeInterval : timeIntervals) {
                if (timeInterval != WEEK) {
                    timeIntervals1.add(timeInterval);
                }
            }
            timeIntervals = timeIntervals1;
        }
        long diffPrev = 0;
        for (int i = 0; i < timeIntervals.size(); i++) {
            TimeInterval timeInterval_i = timeIntervals.get(i);
            long diff = timeInterval_i.toMilliseconds() - interval;
            if (diff >= 0) {
                if (i > 0 && diffPrev < diff) {
                    return timeIntervals.get(i - 1);

                }
                return timeInterval_i;
            }
            diffPrev = Math.abs(diff);
        }
        return YEAR;
    }

    public static TimeInterval getUpper(long interval, boolean isWeekIncluded) {
        List<TimeInterval> timeIntervals = Arrays.asList(values());
        if (!isWeekIncluded) {
            List<TimeInterval> timeIntervals1 = new ArrayList<>(timeIntervals.size() - 1);
            for (TimeInterval timeInterval : timeIntervals) {
                if (timeInterval != WEEK) {
                    timeIntervals1.add(timeInterval);
                }
            }
            timeIntervals = timeIntervals1;
        }
        for (int i = 0; i < timeIntervals.size(); i++) {
            TimeInterval timeInterval_i = timeIntervals.get(i);
            if (timeInterval_i.toMilliseconds() - interval >= 0) {
                return timeInterval_i;
            }
        }
        return YEAR;
    }
}
