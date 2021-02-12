package com.biorecorder.bichart;

import com.biorecorder.bichart.graphics.BCanvas;
import com.biorecorder.bichart.graphics.BColor;
import com.biorecorder.bichart.graphics.BRectangle;
import com.biorecorder.bichart.graphics.Range;

import java.util.ArrayList;
import java.util.List;

public class TraceList {
    private List<Trace> traces = new ArrayList<>();
    private List<ChangeListener> changeListeners = new ArrayList<>(1);
    private List<SelectionListener> selectionListeners = new ArrayList<>(1);

    private int selectedTrace = -1; // -1 no selection

    private void notifyChangeListeners() {
        for (ChangeListener l : changeListeners) {
            l.onChange();
        }
    }

    private void notifySelectionListeners() {
        for (SelectionListener l : selectionListeners) {
            l.onSelectionChanged();
        }
    }

    AxisWrapper getTraceX(int traceIndex) {
        return traces.get(traceIndex).getXAxis();
    }

    AxisWrapper getTraceY(int traceIndex) {
        return traces.get(traceIndex).getYAxis();
    }

    public void addChangeListener(ChangeListener l) {
        changeListeners.add(l);
    }

    public void addSelectionListener(SelectionListener l) {
        selectionListeners.add(l);
    }

    public int size() {
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
        notifyChangeListeners();
    }

    public BColor getColor(int traceIndex) {
        return traces.get(traceIndex).getColor();
    }

    public void add(Trace trace) {
        traces.add(trace);
        notifyChangeListeners();
    }

    public void remove(int traceIndex) {
        Trace traceToRemove = traces.get(traceIndex);
        traces.remove(traceIndex);
        boolean isXUsed = false;
        boolean isYUsed = false;
        for (Trace trace : traces) {
            if(trace.getXAxis() == traceToRemove.getXAxis()) {
                isXUsed = true;
            }
            if(trace.getYAxis() == traceToRemove.getYAxis()) {
                isYUsed = true;
            }
        }
        traceToRemove.getXAxis().setUsed(isXUsed);
        traceToRemove.getYAxis().setUsed(isYUsed);
        notifyChangeListeners();
    }


    public BRectangle getTraceStackArea(int traceIndex) {
        Trace t = traces.get(traceIndex);
        AxisWrapper xAxis = t.getXAxis();
        AxisWrapper yAxis = t.getYAxis();
        int x =  (int) Math.min(xAxis.getStart(), xAxis.getEnd());
        int y =  (int) Math.min(yAxis.getStart(), yAxis.getEnd());
        return  new BRectangle(x, y, (int) xAxis.length(), (int) yAxis.length());
    }


    public boolean isStackUsed(AxisWrapper y1, AxisWrapper y2) {
        for (Trace trace : traces) {
            if(trace.getYAxis() == y1 || trace.getYAxis() == y2) {
                return true;
            }
        }
       return false;
    }

    public Range getTracesXMinMax(AxisWrapper xAxis) {
        Range minMax = null;
        for (Trace trace : traces) {
            if(trace.getXAxis() == xAxis) {
                minMax = Range.join(minMax, trace.xMinMax());
            }
        }
        return minMax;
    }

    public Range getTracesYMinMax(AxisWrapper yAxis) {
        Range minMax = null;
        for (Trace trace : traces) {
            if(trace.getYAxis() == yAxis) {
                minMax = Range.join(minMax, trace.xMinMax());
            }
        }
        return minMax;
    }

    /**
     * find and return xAxis used by traces belonging to the stack o null
     * if there is no traces belonging to stack
     *
     * @param yAxis1, yAxis2 - right and left axis of the given stack
     */
    public AxisWrapper getUsedXAxis(AxisWrapper yAxis1, AxisWrapper yAxis2) {
        for (Trace trace : traces) {
            if(trace.getYAxis() == yAxis1 || trace.getYAxis() == yAxis2) {
                return trace.getXAxis();
            }
        }
        return null;
    }

    public double getTracesBestExtent(AxisWrapper xAxis, int width) {
        double extent = -1;
        for (Trace trace : traces) {
            if (trace.getXAxis() == xAxis) {
                double traceExtent = trace.getBestExtent(width);
                if (extent < 0) {
                    extent = traceExtent;
                } else if (traceExtent > 0) {
                    extent = Math.min(extent, traceExtent);
                }
            }
        }
        return extent;
    }

    // for all x axis
    public Range getAllTracesFullMinMax() {
        Range minMax = null;
        for (Trace trace : traces) {
            Range traceXMinMax = trace.xMinMax();
            minMax = Range.join(minMax, traceXMinMax);
        }
        return minMax;
    }

    public int getSelection() {
        return selectedTrace;
    }

    public void setSelection(int selectedTraceIndex) {
        selectedTrace = selectedTraceIndex;
        notifySelectionListeners();
    }

    public int getTraceIndex(String traceName) {
        for (int i = 0; i < traces.size(); i++) {
            if(traces.get(i).getName().equals(traceName)) {
                return i;
            }
        }
        return -1;
    }

    public TracePoint getNearest(int x, int y) {
        if (selectedTrace >= 0) {
            Trace selection = traces.get(selectedTrace);
            int nearestIndex = selection.nearest(x, y);
            return new TracePoint(selection, nearestIndex);
        } else {
            TracePoint nearestTracePoint = null;
            int minDistance = Integer.MAX_VALUE;
            for (int i = 0; i < traces.size(); i++) {
                Trace trace = traces.get(i);
                int nearest = trace.nearest(x, y);
                int distance = trace.distanceSqw(nearest, x, y);
                if (minDistance >= distance) {
                    minDistance = distance;
                    nearestTracePoint = new TracePoint(trace, nearest);
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
