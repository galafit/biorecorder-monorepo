package com.biorecorder.bichart.axis;

import com.biorecorder.bichart.graphics.*;
import com.biorecorder.bichart.scales.*;
import com.sun.istack.internal.Nullable;

/**
 * Axis is visual representation of Scale that generates and draws
 * visual elements such as axis lines, labels, and ticks.
 * Also it is a wrapper class that gives simplified access to the Scale methods
 */
public class Axis {
    private Scale scale;
    private String title;
    private AxisPainter painter;
    private AxisConfig config;
    private OrientationFunctions orientationFunctions;
    private Orientation orientation;
    private TickLabelFormat tickLabelPrefixAndSuffix;

    private double tickInterval = -1; // in axis domain units (If <= 0 will not be taken into account)

    public Axis(Scale scale, AxisConfig axisConfig, Orientation orientation) {
        this.scale = scale.copy();
        this.config = axisConfig;
        this.orientation = orientation;
        switch (orientation) {
            case TOP:
                orientationFunctions = new TopOrientationFunctions();
                break;
            case BOTTOM:
                orientationFunctions = new BottomOrientationFunctions();
                break;
            case LEFT:
                orientationFunctions = new LeftOrientationFunctions();
                break;
            case RIGHT:
                orientationFunctions = new RightOrientationFunctions();
        }
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setTickLabelPrefixAndSuffix(@Nullable String prefix, @Nullable String suffix) {
        tickLabelPrefixAndSuffix = new TickLabelFormat(prefix, suffix);
        invalidate();
    }

    public void invalidate() {
        painter = null;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getTickInterval() {
        return tickInterval;
    }

    /**
     * Set axis tick interval in domain units (If tick interval <= 0 will not be taken into account)
     * @param tickInterval
     */
    public void setTickInterval(double tickInterval) {
        this.tickInterval = tickInterval;
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

    public String getTitle() {
        return title;
    }
    
    /**
     * Format domain value according to the minMax one "point precision"
     * cutting unnecessary double digits that exceeds that "point precision"
     */
    public String formatDomainValue(double value) {
        return scale.formatDomainValue(value);
    }

    public boolean setMinMax(double min, double max) {
        if(min == max) {
            max++;
            min--;
        }
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
            invalidate();
            return true;
        }
        return false;
    }

    public boolean isVertical() {
        return orientationFunctions.isVertical();
    }

    public boolean isSizeDependsOnMinMax() {
        return orientationFunctions.isVertical() && config.isTickLabelOutside();
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

    public boolean contain(int x, int y) {
        int start = (int) getStart();
        int end = (int) getEnd();
        if(isVertical()) {
            return (y >= end && y <= start) ||
                    (y >= start && y <= end);
        } else {
            return (x >= start && x <= end) ||
                    (x <= end && x <= start);
        }
    }

    public double length() {
        return scale.length();
    }
    
    public void revalidate(RenderContext renderContext) {
        if(painter == null) {
            boolean isRoundingEnabled = false;
            painter = new AxisPainter(scale, config, orientationFunctions, renderContext, title, tickInterval, tickLabelPrefixAndSuffix, isRoundingEnabled);
        }
    }

    public void round(RenderContext renderContext) {
        if(painter == null) {
            boolean isRoundingEnabled = true;
            painter = new AxisPainter(scale, config, orientationFunctions, renderContext, title, tickInterval, tickLabelPrefixAndSuffix, isRoundingEnabled);
        }
    }

    public int getWidthOut(RenderContext renderContext) {
        if(!isSizeDependsOnMinMax()) {
            String label = "";
            return AxisPainter.calculateWidthOut(renderContext, orientationFunctions, (int) length(), config, label, title);
        }
        revalidate(renderContext);
        return painter.getWidthOut();
    }

    public void drawCrosshair(BCanvas canvas, BRectangle area, int position) {
        revalidate(canvas.getRenderContext());
        painter.drawCrosshair(canvas, area, position);
    }

    public void drawGrid(BCanvas canvas, BRectangle area) {
        revalidate(canvas.getRenderContext());
        painter.drawGrid(canvas, area);

    }

    public void drawAxis(BCanvas canvas, BRectangle area) {
        revalidate(canvas.getRenderContext());
        painter.drawAxis(canvas, area);
    }
}
