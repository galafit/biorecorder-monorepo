package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.Axis;
import com.biorecorder.bichart.axis.AxisConfig;
import com.biorecorder.bichart.graphics.*;
import com.biorecorder.bichart.scales.Scale;
import com.biorecorder.bichart.scales.TickLabelFormat;
import com.sun.istack.internal.Nullable;


class AxisWrapper {
    private Axis axis;
    private double min;
    private double max;
    private boolean isStartEndOnTick = false;
    boolean isRounded = false;

    public AxisWrapper(Axis axis) {
        this.axis = axis;
        min = axis.getMin();
        max = axis.getMax();
    }

    public void setStartEndOnTick(boolean startEndOnTick) {
        isStartEndOnTick = startEndOnTick;
    }

    public void setTickInterval(double tickInterval) {
        axis.setTickInterval(tickInterval);
    }

    public AxisConfig getConfig() {
        return axis.getConfig();
    }

    public double length() {
        return axis.length();
    }


    public void setScale(Scale scale) {
        axis.setScale(scale);
    }

    public double scale(double value) {
        return axis.scale(value);
    }

    public double invert(double value) {
        return axis.invert(value);
    }

    public String formatValue(double value) {
        return axis.formatDomainValue(value);
    }

    public String getTitle() {
        return axis.getTitle();
    }

    public void setTitle(String title) {
        axis.setTitle(title);
    }

    public Scale getScale() {
        return axis.getScale();
    }

    public void setConfig(AxisConfig config) {
        axis.setConfig(config);
    }


    /**
     * @param anchorPoint point that do not change during zooming
     */
    public Range zoomedMinMax(double zoomFactor, int anchorPoint) {
        if (zoomFactor <= 0) {
            String errMsg = "Zoom factor = " + zoomFactor + "  Expected > 0";
            throw new IllegalArgumentException(errMsg);
        }
        Scale zoomedScale = axis.getScale();
        double start = getStart();
        double end = getEnd();
        double zoomedEnd = anchorPoint + (end - anchorPoint) * zoomFactor;
        double zoomedStart = anchorPoint - (anchorPoint - start) * zoomFactor;
        zoomedScale.setStartEnd(zoomedStart, zoomedEnd);
        double minNew = zoomedScale.invert(start);
        double maxNew = zoomedScale.invert(end);
        return new Range(minNew, maxNew);
    }

    public Range translatedMinMax(int translation) {
        Scale translatedScale = axis.getScale().copy();
        double start = getStart();
        double end = getEnd();
        double minNew = translatedScale.invert(start + translation);
        double maxNew = translatedScale.invert(end + translation);
        return new Range(minNew, maxNew);
    }

    /**
     * return true if axis start or end actually changed
     */
    public boolean setStartEnd(int start, int end) {
        boolean isChanged = axis.setStartEnd(start, end);
        if(isStartEndOnTick && isChanged) {
            axis.setMinMax(min, max);
            isRounded = false;
        }
        return isChanged;
    }

    /**
     * return true if axis min or max actually will be changed
     */
    public boolean setMinMax(double min, double max, boolean isAutoscale) {
        if(isAutoscale) {
            isRounded = false;
        } else {
            isRounded = true;
        }
        this.min = min;
        this.max = max;
        return axis.setMinMax(min, max);
    }

    public void setTickLabelPrefixAndSuffix(@Nullable String prefix, @Nullable String suffix) {
        axis.setTickLabelPrefixAndSuffix(prefix, suffix);
    }

    public boolean contain(int x, int y) {
       return axis.contain(x, y);
    }

    public boolean isSizeDependsOnMinMax() {
        return axis.isSizeDependsOnMinMax();
    }

    public Range getMinMax() {
        return new Range(axis.getMin(), axis.getMax());
    }

    public double getStart() {
        return axis.getStart();
    }

    public double getEnd() {
        return axis.getEnd();
    }

    private void round(RenderContext renderContext) {
        if(isStartEndOnTick && !isRounded) {
            axis.round(renderContext);
            isRounded = true;
        }

    }

    public int getWidth(RenderContext renderContext) {
        round(renderContext);
        return axis.getWidthOut(renderContext);
    }

    public void drawCrosshair(BCanvas canvas, BRectangle area, int position) {
        round(canvas.getRenderContext());
        axis.drawCrosshair(canvas, area, position);
    }
    public void drawGrid(BCanvas canvas, BRectangle area) {
        round(canvas.getRenderContext());
        axis.drawGrid(canvas, area);
    }

    public void drawAxis(BCanvas canvas, BRectangle area) {
        round(canvas.getRenderContext());
        axis.drawAxis(canvas, area);
    }
}