package com.biorecorder.bichart.axis;

import com.biorecorder.bichart.SizeChangeListener;
import com.biorecorder.bichart.graphics.*;
import com.biorecorder.bichart.scales.*;

import java.util.ArrayList;
import java.util.List;

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
    private AxisConfig config;
    private Orientation orientation;
    
    private List<SizeChangeListener> listeners = new ArrayList<>(1);

    private boolean isStartEndOnTick = false;
    private double tickInterval = -1; // in axis domain units (If <= 0 will not be taken into account)

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

    public void addSizeChangeListener(SizeChangeListener l) {
        listeners.add(l);
    }

    public void invalidate(boolean isSizeChanged) {
        painter = null;
        if (isSizeChanged) {
            for (SizeChangeListener l : listeners) {
                l.onSizeChanged();
            }
        }
    }

    public void setTitle(String title) {
        this.title = title;
        invalidate(true);
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
        boolean sizeChange = false;
        if(orientation.isVertical() && config.isTickLabelOutside()) {
            sizeChange = true;
        }
        invalidate(sizeChange);
    }

    /**
     * set Axis scale. Inner scale is a COPY of the given scale
     * to prevent direct access from outside
     *
     * @param scale
     */
    public void setScale(Scale scale) {
        this.scale = scale.copy();
        invalidate(true);
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
        invalidate(true);
    }

    public String getTitle() {
        return title;
    }

    public boolean isStartEndOnTick() {
        return isStartEndOnTick;
    }

    public void setStartEndOnTick(boolean startEndOnTick) {
        isStartEndOnTick = startEndOnTick;
        boolean sizeChange = false;
        if(orientation.isVertical() && config.isTickLabelOutside()) {
            sizeChange = true;
        }
        invalidate(sizeChange);
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
            boolean sizeChange = false;
            if(orientation.isVertical() && config.isTickLabelOutside()) {
                sizeChange = true;
            }
            invalidate(sizeChange);
            return true;
        }
        return false;
    }

    public boolean setStartEnd(int start, int end) {
        if (start != end && ((int)scale.getStart() != start || (int)scale.getEnd() != end)) {
            scale.setStartEnd(start, end);
            invalidate(true);
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
    
    public void revalidate(RenderContext renderContext) {
        if(painter == null) {
            painter = new AxisPainter(scale, config, orientation, renderContext, title, tickInterval, isStartEndOnTick);
        }
    }

    public int getWidthOut(RenderContext renderContext) {
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
