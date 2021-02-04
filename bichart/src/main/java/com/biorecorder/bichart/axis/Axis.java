package com.biorecorder.bichart.axis;

import com.biorecorder.bichart.graphics.*;
import com.biorecorder.bichart.scales.*;

/**
 * Axis is visual representation of Scale that generates and draws
 * visual elements such as axis lines, labels, and ticks.
 * Also it is a wrapper class that gives simplified access to the Scale methods
 * and some advanced functionality such as Zooming and Translating. See
 * {@link #setConfig(AxisConfig)}
 */
public class Axis {
    private Scale scale;
    private String title;
    private AxisPainter painter;
    private boolean isStartEndOnTick = false;
    private AxisConfig config;
    private Orientation orientation;

    public Axis(Scale scale, AxisConfig axisConfig, XAxisPosition xAxisPosition) {
        this.scale = scale.copy();
        this.config = axisConfig;
        switch (xAxisPosition) {
            case TOP:
                orientation = new TopOrientation();
                break;
            case BOTTOM:
                orientation = new BottomOrientation();
        }
    }

    public Axis(Scale scale, AxisConfig axisConfig, YAxisPosition yAxisPosition) {
        this.scale = scale.copy();
        this.config = axisConfig;
        switch (yAxisPosition) {
            case LEFT:
                orientation = new LeftOrientation();
                break;
            case RIGHT:
                orientation = new RightOrientation();
        }
    }

    public int getWidthOut() {
        if(painter != null) {
            return painter.getWidthOut();
        }
        return 0;
    }

    public void invalidate() {
        painter = null;
    }

    public void setTitle(String title) {
        this.title = title;
        invalidate();
    }

    /**
     * set Axis scale. Inner scale is a COPY of the given scale
     * to prevent direct access from outside
     *
     * @param scale
     */
    public void setScale(Scale scale) {
        this.scale = scale.copy();
        invalidate();
    }

    /**
     * get the COPY of inner scale
     *
     * @return copy of inner scale
     */
    public Scale getScale() {
        return scale.copy();
    }

    /**
     * get the COPY of inner config. To change axis config
     * use {@link #setConfig(AxisConfig)}
     *
     * @return copy of inner config
     */
    public AxisConfig getConfig() {
        return new AxisConfig(config);
    }

    /**
     * set Axis config. Inner config is a COPY of the given config
     * to prevent direct access from outside
     *
     * @param config
     */
    public void setConfig(AxisConfig config) {
        // set a copy to safely change
        this.config = new AxisConfig(config);
        invalidate();
    }

    public boolean isTickLabelOutside() {
        return config.isTickLabelOutside();
    }

    public String getTitle() {
        return title;
    }

    public boolean isStartEndOnTick() {
        return isStartEndOnTick;
    }


    /**
     * Zoom does not affect the axis scale!
     * It copies the axis scales and transforms its domain respectively.
     * Note: zoom affects only max value, min value does not change!!!
     *
     * @param zoomFactor
     * @return new scale with transformed domain
     */
    public Scale zoom(double zoomFactor) {
        if (zoomFactor <= 0) {
            String errMsg = "Zoom factor = " + zoomFactor + "  Expected > 0";
            throw new IllegalArgumentException(errMsg);
        }
        Scale zoomedScale = scale.copy();

        double start = getStart();
        double end = getEnd();

        double zoomedLength = (end - start) * zoomFactor;
        double zoomedEnd = start + zoomedLength;
        zoomedScale.setStartEnd((int)start, (int)zoomedEnd);
        double maxNew = zoomedScale.invert(end);
        zoomedScale.setMinMax(getMin(), maxNew);
        return zoomedScale;
    }


    /**
     * Zoom does not affect the axis scale!
     * It copies the axis scales and transforms its domain respectively.
     *
     * @param translation
     * @return new scale with transformed domain
     */
    public Scale translate(int translation) {
        Scale translatedScale = scale.copy();

        double start = getStart();
        double end = getEnd();
        double minNew = translatedScale.invert(start + translation);
        double maxNew = translatedScale.invert(end + translation);
        translatedScale.setMinMax(minNew, maxNew);
        return translatedScale;
    }

    /**
     * Format domain value according to the minMax one "point precision"
     * cutting unnecessary double digits that exceeds that "point precision"
     */
    public String formatDomainValue(double value) {
        return scale.formatDomainValue(value);
    }

    public boolean setMinMax(double min, double max) {
        if(min != scale.getMin() || max != scale.getMax()) {
            scale.setMinMax(min, max);
            invalidate();
            return true;
        }
        return false;
    }

    public boolean setStartEnd(int start, int end) {
        if (start != end && ((int)scale.getStart() != start || (int)scale.getEnd() != end)) {
            scale.setStartEnd(start, end);
            return true;
        }
        return false;
    }

    public double getMin() {
        return scale.getMin();
    }

    public double getMax() {
        return scale.getMax();
    }

    public double getStart() {
        return scale.getStart();
    }

    public double getEnd() {
        return scale.getEnd();
    }

    public double scale(double value) {
        return scale.scale(value);
    }

    public double invert(double value) {
        return scale.invert(value);
    }

    public double length() {
        return Math.abs(getEnd() - getStart());
    }

    public double getBestExtent(RenderContext renderContext, int length) {
       return AxisPainter.getBestExtent(renderContext, scale, config, orientation, length);
    }

    private AxisPainter createPainter(BCanvas canvas, boolean rounding) {
        return  new AxisPainter(scale, config, orientation, canvas.getRenderContext(), title, rounding);

    }

    public void setStartEndOnTick(BCanvas canvas) {
        painter =  createPainter(canvas, true);
    }

    public void drawCrosshair(BCanvas canvas, BRectangle area, int position) {
       if(painter == null) {
           painter =  createPainter(canvas, false);
       }
       painter.drawCrosshair(canvas, area, position);
    }

    public void drawGrid(BCanvas canvas, BRectangle area) {
        if(painter == null) {
            painter =  createPainter(canvas, false);
        }
        painter.drawGrid(canvas, area);

    }

    public void drawAxis(BCanvas canvas, BRectangle area) {
        if(painter == null) {
            painter =  createPainter(canvas, false);
        }
        painter.drawAxis(canvas, area);

    }
}
