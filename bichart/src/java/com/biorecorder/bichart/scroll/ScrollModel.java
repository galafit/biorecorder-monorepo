package com.biorecorder.bichart.scroll;

import com.biorecorder.bichart.graphics.Range;
import com.biorecorder.bichart.scales.Scale;

import java.util.ArrayList;
import java.util.List;

public class ScrollModel {
    private Scale scale; // view scale
    private double start = 0; // view start
    private double end = 100; // view end (start-end are limits where move viewport)
    private double viewportPosition = 0; // viewportPosition
    private double viewportExtent = 10; // viewportWidth

    private List<ScrollListener> eventListeners = new ArrayList<ScrollListener>();

    public ScrollModel(Scale scale) {
        this.scale = scale;
    }

    public void addListener(ScrollListener listener) {
        eventListeners.add(listener);
    }

    private void fireListeners() {
        for (ScrollListener listener : eventListeners) {
            listener.onScrollChanged(scale.invert(viewportPosition), scale.invert(viewportPosition + viewportExtent));
        }
    }

    public Range getViewportMinMax() {
        return new Range(scale.invert(viewportPosition), scale.invert(viewportPosition + viewportExtent));
    }

    public void setMinMax(double min, double max) {
        setStartEnd(scale.scale(min), scale.scale(max));
    }

    public void setViewportMinMax(double min, double max) {
        double newPosition = scale.scale(min);
        double newExtent = scale.scale(max) - newPosition;
        setRangeProperties(newPosition, newExtent, start, end);
    }

    public void setViewportMin(double min) {
        double newPosition = scale.scale(min);
        setRangeProperties(newPosition, viewportExtent, start, end);
    }

    public void setViewportCenterValue(double centerValue) {
        double newPosition = scale.scale(centerValue) - viewportExtent/2;
        setRangeProperties(newPosition, viewportExtent, start, end);
    }

    public double getViewportPosition() {
        return viewportPosition;
    }

    public void setViewportPosition(double newPosition) {
        setRangeProperties(newPosition, viewportExtent, start, end);
    }

    public double getViewportExtent() {
        return viewportExtent;
    }

    public void setViewportExtent(double newExtent){
        setRangeProperties(viewportPosition, newExtent, start, end);
    }

    public void setStartEnd(double newStart, double newEnd) {
        setRangeProperties(viewportPosition, viewportExtent, newStart, newEnd);
    }

    public double getStart() {
        return start;
    }

    public double getEnd() {
        return end;
    }

    public void setRangeProperties(double newPosition, double newExtent, double newStart, double newEnd) {
        double oldExtent = viewportExtent;
        double oldPosition = viewportPosition;
        end = newEnd;
        start = newStart;
        if(start > end) {
            end = start;
        }
        viewportExtent = normalizeExtent(newExtent);
        viewportPosition = normalizeValue(newPosition, viewportExtent);
        // in our case change of start and end no important for listeners
        if(oldExtent != viewportExtent || oldPosition != viewportPosition) {
            fireListeners();
        }
    }

    private double normalizeExtent(double extent) {
        if(extent < 0) {
            return 0;
        }
        double maxExtent = end - start;
        if(extent > maxExtent) {
            return maxExtent;
        }
        return extent;
    }

    private double normalizeValue(double value, double extent) {
        if (value < start) {
            return start;
        }
        if (value + extent > end) {
            return end - extent;
        }
        return value;
    }
}
