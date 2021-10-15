package com.biorecorder.bichart.scroll;

import com.biorecorder.bichart.graphics.Range;
import com.biorecorder.bichart.scales.Scale;

import java.util.ArrayList;
import java.util.List;

public class ScrollModel {
    private double start = 0; // view start
    private double end = 100; // view end (start-end are limits where move viewport)
    private double viewportPosition = 0; // viewportPosition
    private double viewportExtent = 10; // viewportWidth

    private List<ChangeListener> listeners = new ArrayList<ChangeListener>();


    public void addListener(ChangeListener listener) {
        listeners.add(listener);
    }

    private void fireListeners() {
        for (ChangeListener l : listeners) {
            l.stateChanged();
        }
    }

    public double getViewportPosition() {
        return viewportPosition;
    }

    public double getViewportExtent() {
        return viewportExtent;
    }

    public void setViewportPosition(double newPosition) {
        setRangeProperties(newPosition, viewportExtent, start, end);
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
        if(oldPosition != viewportPosition || oldExtent != viewportExtent ) {
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
