package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.axis.YAxisPosition;
import com.biorecorder.bichart.graphics.BCanvas;
import com.biorecorder.bichart.graphics.BColor;
import com.biorecorder.bichart.graphics.BRectangle;
import com.biorecorder.bichart.graphics.Range;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TraceList {
    private List<Trace> traces = new ArrayList<>();
    private Map<AxisWrapper, List<Trace>> xAxisToTraces = new HashMap<>();
    private Map<AxisWrapper, List<Trace>> yAxisToTraces = new HashMap<>();
    private List<ChangeListener> changeListeners = new ArrayList<>(1);
    private int selectedTrace = -1; // -1 no selection

    private void notifyChangeListeners() {
        for (ChangeListener l : changeListeners) {
            l.onChange();
        }
    }

    public XAxisPosition getTraceXPosition(int traceIndex) {
        return traces.get(traceIndex).getXAxisPosition();
    }

    public YAxisPosition getTraceYPosition(int traceIndex) {
        return traces.get(traceIndex).getYAxisPosition();
    }

    public AxisWrapper getTraceX(int traceIndex) {
        return traces.get(traceIndex).getXAxis();
    }

    public AxisWrapper getTraceY(int traceIndex) {
        return traces.get(traceIndex).getYAxis();
    }

    public void addChangeListener(ChangeListener l) {
        changeListeners.add(l);
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
    }

    public BColor getColor(int traceIndex) {
        return traces.get(traceIndex).getColor();
    }

    public void setData(int traceIndex, ChartData data) {
        traces.get(traceIndex).setData(data);
    }

    public void add(Trace trace) {
        traces.add(trace);
        AxisWrapper xAxis = trace.getXAxis();
        AxisWrapper yAxis = trace.getYAxis();
        List<Trace> xTraces = xAxisToTraces.get(xAxis);
        if(xTraces == null) {
            xTraces = new ArrayList<>();
            xAxisToTraces.put(xAxis, xTraces);
        }
        xTraces.add(trace);

        List<Trace> yTraces = yAxisToTraces.get(yAxis);
        if(yTraces == null) {
            yTraces = new ArrayList<>();
            yAxisToTraces.put(yAxis, yTraces);
        }
        yTraces.add(trace);
        notifyChangeListeners();
    }

    public void remove(int traceIndex) {
        Trace traceToRemove = traces.get(traceIndex);
        traces.remove(traceIndex);
        AxisWrapper xAxis = traceToRemove.getXAxis();
        AxisWrapper yAxis = traceToRemove.getYAxis();
        List<Trace> xTraces = xAxisToTraces.get(xAxis);
        xTraces.remove(traceToRemove);
        if(xTraces.size() == 0) {
            xAxisToTraces.remove(xAxis);
        }
        List<Trace> yTraces = yAxisToTraces.get(yAxis);
        yTraces.remove(traceToRemove);
        if(yTraces.size() == 0) {
            yAxisToTraces.remove(yAxis);
        }
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

    public boolean isXAxisUsedByStack(AxisWrapper x, AxisWrapper y1, AxisWrapper y2) {
        List<Trace> xTraces = xAxisToTraces.get(x);
        if(xTraces != null) {
            for (Trace trace : xTraces) {
                if(trace.getYAxis() == y1 || trace.getYAxis() == y2) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isXAxisUsed(AxisWrapper x) {
        return xAxisToTraces.get(x) != null;
    }

    public boolean isYAxisUsed(AxisWrapper y) {
        return yAxisToTraces.get(y) != null;
    }

    public Range getTracesXMinMax(AxisWrapper xAxis) {
        List<Trace> xTraces = xAxisToTraces.get(xAxis);
        if(xTraces == null) {
            return null;
        }
        Range minMax = null;
        for (Trace trace : xTraces) {
            minMax = Range.join(minMax, trace.xMinMax());
        }
        return minMax;
    }

    public Range getTracesYMinMax(AxisWrapper yAxis) {
        List<Trace> yTraces = yAxisToTraces.get(yAxis);
        if(yTraces == null) {
            return null;
        }
        Range minMax = null;
        for (Trace trace : yTraces) {
            minMax = Range.join(minMax, trace.yMinMax());
        }
        return minMax;
    }

    public Range getAllTracesXMinMax() {
        Range minMax = null;
        for (Trace trace : traces) {
            Range traceXMinMax = trace.xMinMax();
            minMax = Range.join(minMax, traceXMinMax);
        }
        return minMax;
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

    public int getSelection() {
        return selectedTrace;
    }

    public void setSelection(int selectedTraceIndex) {
        selectedTrace = selectedTraceIndex;
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
