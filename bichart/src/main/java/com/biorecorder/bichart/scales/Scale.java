package com.biorecorder.bichart.scales;

import com.sun.istack.internal.Nullable;

import java.util.Arrays;

public abstract class Scale {
    protected double domain[] = {0, 1};
    protected double range[] = {0, 1};

    public boolean setMinMax(double min, double max) {
        if (Double.isNaN(min)) {
            String errMsg = "Min is NaN";
            throw new IllegalArgumentException(errMsg);
        }
        if (Double.isNaN(max)) {
            String errMsg = "Max is NaN";
            throw new IllegalArgumentException(errMsg);
        }
        if (Double.isInfinite(min)) {
            String errMsg = "Min is infinity";
            throw new IllegalArgumentException(errMsg);
        }
        if (Double.isInfinite(max)) {
            String errMsg = "Max is infinity";
            throw new IllegalArgumentException(errMsg);
        }
        if (min == max) {
            String errMsg = "min == max";
            throw new IllegalArgumentException(errMsg);
        }

        if (min == getMin() && max == getMax()) {
            return false;
        }
        domain[0] = min;
        domain[domain.length - 1] = max;
        return true;
    }

    public double getMin() {
        return domain[0];
    }

    public double getMax() {
        return domain[domain.length - 1];
    }

    public boolean setStartEnd(double start, double end) {
        if (Double.isNaN(start)) {
            String errMsg = "Start is NaN";
            throw new IllegalArgumentException(errMsg);
        }
        if (Double.isNaN(end)) {
            String errMsg = "End is NaN";
            throw new IllegalArgumentException(errMsg);
        }
        if (Double.isInfinite(start)) {
            String errMsg = "Start is infinity";
            throw new IllegalArgumentException(errMsg);
        }
        if (Double.isInfinite(end)) {
            String errMsg = "End is infinity";
            throw new IllegalArgumentException(errMsg);
        }
        if (start == end) {
            String errMsg = "start == end";
            throw new IllegalArgumentException(errMsg);
        }
        if (start == getStart() && end == getEnd()) {
            return false;
        }
        range[0] = start;
        range[range.length - 1] = end;
        return true;
    }

    public double getStart() {
        return range[0];
    }

    public double getEnd() {
        return range[range.length - 1];
    }

    public double getLength() {
        return Math.abs(getStart() - getEnd());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Scale)) {
            return false;
        }
        Scale scale = (Scale) o;
        return Arrays.equals(domain, scale.domain) && Arrays.equals(range, scale.range);
    }

    public abstract double scale(double value);

    public abstract double invert(double value);

    /**
     * Format domain value according to the one "point precision"
     * cutting unnecessary double digits that exceeds that "point precision"
     */
    public abstract String formatDomainValue(double value);

    public abstract TickProvider getTickProviderByIntervalCount(int tickIntervalCount, @Nullable TickLabelFormat formatInfo);

    public abstract TickProvider getTickProviderByInterval(double tickInterval, @Nullable TickLabelFormat formatInfo);

    public abstract Scale copy();

}

