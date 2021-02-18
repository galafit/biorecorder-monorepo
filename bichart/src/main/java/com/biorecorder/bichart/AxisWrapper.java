package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.Axis;
import com.biorecorder.bichart.axis.AxisConfig;
import com.biorecorder.bichart.graphics.BCanvas;
import com.biorecorder.bichart.graphics.BRectangle;
import com.biorecorder.bichart.graphics.Range;
import com.biorecorder.bichart.graphics.RenderContext;
import com.biorecorder.bichart.scales.Scale;


class AxisWrapper {
    private Axis axis;
    private boolean isStartEndOnTick;


    public AxisWrapper(Axis axis) {
        this.axis = axis;
        isStartEndOnTick = axis.isStartEndOnTick();
    }

    private void deactivateRounding() {
        axis.setStartEndOnTick(false);
    }

    private void activateRounding() {
        axis.setStartEndOnTick(isStartEndOnTick);
    }

    public boolean isStartEndOnTick() {
        return isStartEndOnTick;
    }

    public void setStartEndOnTick(boolean startEndOnTick) {
        isStartEndOnTick = startEndOnTick;
    }

    public void setTickInterval(double tickInterval) {
        axis.setTickInterval(tickInterval);
    }

    public double getBestExtent(RenderContext renderContext, int length) {
        return axis.getBestExtent(renderContext, length);
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


    public Range zoomedMinMax(double zoomFactor) {
        if (zoomFactor <= 0) {
            String errMsg = "Zoom factor = " + zoomFactor + "  Expected > 0";
            throw new IllegalArgumentException(errMsg);
        }
        Scale zoomedScale = axis.getScale().copy();

        double start = getStart();
        double end = getEnd();
        double zoomedLength = (end - start) * zoomFactor;
        double zoomedEnd = start + zoomedLength;
        zoomedScale.setStartEnd(start, zoomedEnd);
        double maxNew = zoomedScale.invert(end);
        return new Range(getMin(), maxNew);
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
        return axis.setStartEnd(start, end);
    }

    /**
     * return true if axis min or max actually will be changed
     */
    public boolean setMinMax(double min, double max, boolean isAutoscale) {
        if(isAutoscale) {
            activateRounding();
        } else {
            deactivateRounding();
        }
        return axis.setMinMax(min, max);
    }

    public boolean isVertical() {
        return axis.isVertical();
    }

    public boolean isSizeDependsOnMinMax() {
        return axis.isSizeDependsOnMinMax();
    }

    public double getMin() {
        return axis.getMin();
    }

    public double getMax() {
        return axis.getMax();
    }

    public double getStart() {
        return axis.getStart();
    }

    public double getEnd() {
        return axis.getEnd();
    }

    public int getWidth(RenderContext renderContext) {
        return axis.getWidthOut(renderContext);
    }

    public void drawCrosshair(BCanvas canvas, BRectangle area, int position) {
        axis.drawCrosshair(canvas, area, position);
    }
    public void drawGrid(BCanvas canvas, BRectangle area) {
        axis.drawGrid(canvas, area);
    }

    public void drawAxis(BCanvas canvas, BRectangle area) {
        axis.drawAxis(canvas, area);
    }
}