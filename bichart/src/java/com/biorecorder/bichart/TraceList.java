package com.biorecorder.bichart;

import com.biorecorder.bichart.graphics.BCanvas;
import com.biorecorder.bichart.graphics.BColor;
import com.biorecorder.bichart.graphics.BRectangle;


import java.util.*;

class TraceList {
    private List<Trace> traces = new ArrayList<>();
    private List<ChangeListener> changeListeners = new ArrayList<>(1);
    private int selectedTraceNumber = -1; // -1 no selection

    private void notifyChangeListeners() {
        for (ChangeListener l : changeListeners) {
            l.onChange();
        }
    }

  /*  public Trace getTrace(int traceNumber) {
        return traces.get(traceNumber);
    }*/
    public Range yMinMax(int traceNumber) {
        return traces.get(traceNumber).yMinMax();
  }

    public Range xMinMax(int traceNumber) {
        return traces.get(traceNumber).xMinMax();
    }

    public BRectangle getTraceStackArea(int traceNumber) {
       return traces.get(traceNumber).getTraceStackArea();
    }

    public int getMarkSize(int traceNumber) {
        return traces.get(traceNumber).markSize();
    }

    public void addChangeListener(ChangeListener l) {
        changeListeners.add(l);
    }

    public int traceCount() {
        return traces.size();
    }

    public String getName(int traceIndex) {
        return traces.get(traceIndex).getName();
    }

    public void setName(int traceIndex, String name) {
        traces.get(traceIndex).setName(name);
        notifyChangeListeners();
    }

    public void setColor(int traceIndex, BColor color) {
        traces.get(traceIndex).setColor(color);
    }

    public BColor getColor(int traceIndex) {
        return traces.get(traceIndex).getColor();
    }

    public TooltipData getTooltipData(int traceIndex, int dataIndex) {
        Trace t = traces.get(traceIndex);
        return new TooltipData(t.getColor(), t.getCrosshairPoint(dataIndex), t.getTooltipInfo(dataIndex));
    }

    public void setData(int traceIndex, ChartData data) {
        traces.get(traceIndex).setData(data);
    }

    public void add(Trace trace) {
        traces.add(trace);
        notifyChangeListeners();
    }

    public int getSelection() {
        return selectedTraceNumber;
    }

    public void setSelection(int selectedTraceIndex) {
        selectedTraceNumber = selectedTraceIndex;
    }

    public TracePoint getNearest(int x, int y) {
        if (selectedTraceNumber >= 0) {
            Trace selection = traces.get(selectedTraceNumber);
            int nearestIndex = selection.nearest(x, y);
            return new TracePoint(selectedTraceNumber, nearestIndex);
        } else {
            TracePoint nearestTracePoint = null;
            int minDistance = Integer.MAX_VALUE;
            for (int i = 0; i < traces.size(); i++) {
                Trace trace = traces.get(i);
                int nearest = trace.nearest(x, y);
                if(nearest >= 0) {
                    int distance = trace.distanceSqw(nearest, x, y);
                    if (minDistance >= distance) {
                        minDistance = distance;
                        nearestTracePoint = new TracePoint(i, nearest);
                    }
                }
            }
            return nearestTracePoint;
        }
    }

    public void draw(BCanvas canvas) {
        for (Trace trace : traces) {
            trace.draw(canvas);
        }
    }
}
