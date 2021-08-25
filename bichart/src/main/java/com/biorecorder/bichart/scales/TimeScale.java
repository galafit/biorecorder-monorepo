package com.biorecorder.bichart.scales;

import com.biorecorder.data.time.TimeInterval;
import com.biorecorder.data.datatable.aggregation.TimeIntervalProvider;
import com.biorecorder.data.time.TimeUnit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


/**
 * Created by galafit on 7/1/18.
 */
public class TimeScale extends LinearScale {
    private DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS dd-MMM-yyyy ");

    @Override
    public Scale copy() {
        TimeScale copyScale = new TimeScale();
        copyScale.setMinMax(getMin(), getMax());
        copyScale.setStartEnd(getStart(), getEnd());
        return copyScale;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TimeScale)) {
            return false;
        }
        return super.equals(o);
    }

    @Override
    public TickProvider getTickProviderByIntervalCount(int tickIntervalCount, TickLabelFormat labelFormatInfo) {
        TimeTickProvider provider = new TimeTickProvider();
        provider.setTickIntervalCount(tickIntervalCount);
        return provider;
    }

    @Override
    public TickProvider getTickProviderByInterval(double tickInterval, TickLabelFormat labelFormatInfo) {
        TimeTickProvider provider = new TimeTickProvider();
        provider.setTickInterval(tickInterval);
        return provider;
    }

    @Override
    public String formatDomainValue(double value) {
        return timeFormat.format((long) value);
    }



    class TimeTickProvider implements TickProvider {
        private TimeIntervalProvider timeIntervalProvider;
        private DateFormatter labelFormat;

        private void setTickInterval(double interval, boolean isUpper) {
            TimeInterval timeInterval;
            if(isUpper) {
                timeInterval = TimeInterval.getUpper(Math.round(interval), false);
            } else {
                timeInterval = TimeInterval.getClosest(Math.round(interval), false);
            }
            TimeUnit timeUnit = timeInterval.timeUnit();
            int multiplier = timeInterval.unitMultiplier();
            if(timeUnit == TimeUnit.DAY || timeUnit == TimeUnit.MONTH) {
                int dayMultiplier = (int)(interval / TimeUnit.DAY.toMilliseconds());
                if(dayMultiplier <= 20) {
                    multiplier = Math.max(1, dayMultiplier);
                    timeUnit = TimeUnit.DAY;
                }
            }
            if(timeUnit == TimeUnit.YEAR) {
                multiplier = (int)(interval / TimeUnit.YEAR.toMilliseconds());
                multiplier = Math.max(1, multiplier);
            }
            timeIntervalProvider = new TimeIntervalProvider(timeUnit, multiplier);
            System.out.println("time tick provider "+ timeUnit + " "+multiplier);
            labelFormat = new DateFormatter(timeUnit);
        }


        public void setTickInterval(double interval) {
            setTickInterval(interval, false);
        }

        public void setTickIntervalCount(int tickIntervalCount) {
            if (tickIntervalCount < 1) {
                tickIntervalCount = 1;
            }
            double max = domain[domain.length - 1];
            double min = domain[0];
            long interval = Math.round((max - min) / tickIntervalCount);
            setTickInterval(interval, false);
        }

        @Override
        public void increaseTickInterval(int increaseFactor) {
            long intervalNew = timeIntervalProvider.getUnitMultiplier() * timeIntervalProvider.getTimeUnit().toMilliseconds() * increaseFactor;
            setTickInterval(intervalNew, true);
        }

        @Override
        public Tick getNextTick() {
            timeIntervalProvider.getNext();
            return currentTick();
        }

        @Override
        public Tick getPreviousTick() {
            timeIntervalProvider.getPrevious();
            return currentTick();
        }

        @Override
        public Tick getUpperTick(double value) {
            timeIntervalProvider.getContaining(value);
            if (timeIntervalProvider.getCurrentIntervalStartMs() < value) {
                timeIntervalProvider.getNext();
            }
            return currentTick();
        }

        @Override
        public Tick getLowerTick(double value) {
            timeIntervalProvider.getContaining(value);
            return currentTick();
        }

        private Tick currentTick() {
            return new Tick(timeIntervalProvider.getCurrentIntervalStartMs(), labelFormat.format(timeIntervalProvider.getCurrentIntervalStartMs(), timeIntervalProvider.getCurrentIntervalStartHours()));
        }

        class DateFormatter {
            private final SimpleDateFormat primaryFormat;
            private final SimpleDateFormat secondaryFormat;
            private final TimeUnit timeUnit;

            public DateFormatter(TimeUnit timeUnit) {
                this.timeUnit = timeUnit;
                switch (timeUnit) {
                    case MILLISECOND:
                        primaryFormat = new SimpleDateFormat("HH:mm:ss.SSS");
                        secondaryFormat = primaryFormat;
                        break;
                    case SECOND:
                        primaryFormat = new SimpleDateFormat("HH:mm:ss");
                        secondaryFormat = primaryFormat;
                        break;
                    case MINUTE:
                        primaryFormat = new SimpleDateFormat("HH:mm");
                        secondaryFormat = primaryFormat;
                        break;
                    case HOUR:
                        primaryFormat = new SimpleDateFormat("HH:mm");
                        secondaryFormat = new SimpleDateFormat("dd. MMM");
                        break;
                    case DAY:
                        primaryFormat =  new SimpleDateFormat("dd. MMM");
                        secondaryFormat = primaryFormat;
                        break;
                    case WEEK:
                    case MONTH:
                        primaryFormat =  new SimpleDateFormat("MMM ''yy");
                        secondaryFormat = primaryFormat;
                        break;
                    default:
                        primaryFormat =  new SimpleDateFormat("yyyy");
                        secondaryFormat = primaryFormat;
                }

            }

            public String format(long ms, long hour) {
                if( timeUnit == TimeUnit.HOUR && hour == 0) {
                    return secondaryFormat.format(ms);
                } else {
                    return primaryFormat.format(ms);
                }
            }
        }
    }
}
