package com.biorecorder.bichart.scroll;

import java.util.ArrayList;
import java.util.List;

public class ScrollModel {
    private int start = 0; // view start
    private int end = 100; // view end (start-end are limits where move viewport)
    private double viewportPosition = 0; // viewportPosition
    private int viewportExtent = 10; // viewportWidth

    private List<ChangeListener> listeners = new ArrayList<ChangeListener>();


    public void addListener(ChangeListener listener) {
        listeners.add(listener);
    }

    public void removeListeners() {
        listeners.clear();
    }

    private void fireListeners() {
        for (ChangeListener l : listeners) {
            l.stateChanged();
        }
    }

    public double getViewportPosition() {
        return viewportPosition;
    }

    public int getViewportExtent() {
        return viewportExtent;
    }

    public void setViewportPosition(double newPosition) {
        setRangeProperties(newPosition, viewportExtent, start, end);
    }

    public void setViewportExtent(int newExtent){
        setRangeProperties(viewportPosition, newExtent, start, end);
    }

    public void setStartEnd(int newStart, int newEnd) {
        setRangeProperties(viewportPosition, viewportExtent, newStart, newEnd);
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    /**
     * our model change limits (end) if extent is too big
     */
    public void setRangeProperties(double newPosition, int newExtent, int newStart, int newEnd) {
        double oldExtent = viewportExtent;
        double oldPosition = viewportPosition;
        end = newEnd;
        start = newStart;
        viewportExtent = newExtent;
        if(end < start + viewportExtent) {
            end = start + viewportExtent;
        }
        viewportPosition = normalizeValue(newPosition, viewportExtent);
        // in our case change of start and end no important for listeners
        if(oldPosition != viewportPosition || oldExtent != viewportExtent ) {
            fireListeners();
        }
    }

  /*  private double normalizeExtent(double extent) {
        if(extent < 0) {
            return 0;
        }
        double maxExtent = end - start;
        if(extent > maxExtent) {
            return maxExtent;
        }
        return extent;
    }*/

    private double normalizeValue(double value, int extent) {
        if (value < start) {
            return start;
        }
        if (value + extent > end) {
            return end - extent;
        }
        return value;
    }
}
