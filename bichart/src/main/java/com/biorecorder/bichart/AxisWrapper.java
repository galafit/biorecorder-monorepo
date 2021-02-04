package com.biorecorder.bichart;

import com.biorecorder.bichart.axis.Axis;
import com.biorecorder.bichart.axis.AxisConfig;
import com.biorecorder.bichart.graphics.BCanvas;
import com.biorecorder.bichart.graphics.BRectangle;
import com.biorecorder.bichart.graphics.RenderContext;
import com.biorecorder.bichart.scales.Scale;

/**
 * This class do axis rounding and add isUsed property.
 * <p>
 * Implement axis rounding when methods:
 * drawAxis or drawGrid or getWidthOut is invoked !!!
 */
class AxisWrapper {
    private Axis axis;
    private boolean isUsed = false;

    public AxisWrapper(Axis axis) {
        this.axis = axis;
    }

    public double getBestExtent(RenderContext renderContext, int length) {
        return axis.getBestExtent(renderContext, length);
    }

    public boolean isRoundingEnabled() {
        return axis.isStartEndOnTick();
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

    public boolean isTickLabelOutside() {
        return axis.isTickLabelOutside();
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

    public Scale zoom(double zoomFactor) {
       return axis.zoom(zoomFactor);
    }


    public Scale translate(int translation) {
       return axis.translate(translation);
    }

    /**
     * return true if axis min or max actually will be changed
     */
    public boolean setMinMax(double min, double max) {
        return axis.setMinMax(min, max);
    }

    /**
     * return true if axis start or end actually changed
     */
    public boolean setStartEnd(int start, int end) {
        return axis.setStartEnd(start, end);
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

    public boolean isUsed() {
        return isUsed;
    }

    /**
     * this method DO AXIS ROUNDING
     */
    public int getWidth() {
        return axis.getWidthOut();
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

    public void setUsed(boolean isUsed) {
        this.isUsed = isUsed;
    }

}