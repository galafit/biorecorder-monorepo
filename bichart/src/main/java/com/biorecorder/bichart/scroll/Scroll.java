package com.biorecorder.bichart.scroll;

import com.biorecorder.bichart.ScrollConfig;
import com.biorecorder.bichart.axis.XAxisPosition;
import com.biorecorder.bichart.graphics.BCanvas;
import com.biorecorder.bichart.graphics.BRectangle;
import com.biorecorder.bichart.graphics.DashStyle;
import com.biorecorder.bichart.graphics.Range;

public class Scroll {
    private ScrollConfig config;
    private ScrollModel model;
    private ScrollScale scale;

    public Scroll(ScrollModel model, ScrollScale scale, ScrollConfig config) {
        this.config = config;
        this.model = model;
        this.scale = scale;
    }

    public void setConfig(ScrollConfig config) {
        this.config = config;
    }

    public void addListener(ScrollListener listener) {
        model.addListener(listener);
    }

    public double getMax() {
        return model.getMax();
    }

    public double getMin() {
        return model.getMin();
    }

    public double getValue() {
        return model.getValue();
    }

    public double getExtent() {
        return model.getExtent();
    }

    public void setValues(double value, double extent, double min, double max) {
        model.setRangeProperties(value, extent, min, max);
    }

    public void setExtent(double extent) {
        model.setExtent(extent);
    }

    public void setMinMax(double min, double max) {
        model.setMinMax(min, max);
    }

    public void setPosition(double x) {
        double minPosition = scale.valueToPosition(model.getMin());
        double maxPosition = scale.valueToPosition(model.getMax());
        double scrollWidth = scale.valueToPosition(model.getValue() + model.getExtent()) - scale.valueToPosition(model.getValue());

        double position = normalizeScrollPosition(x, scrollWidth, minPosition, maxPosition);
        double value = scale.positionToValue(position);
        double value1 = scale.positionToValue(position + scrollWidth);
        model.setRangeProperties(value, value1 - value, model.getMin(), model.getMax());
    }

    public void translatePosition(double dx) {
        setPosition(scale.valueToPosition(model.getValue()) + dx);
    }

    public void zoomExtent(double zoomFactor) {
        double minPosition = scale.valueToPosition(model.getMin());
        double maxPosition = scale.valueToPosition(model.getMax());
        double scrollStart = scale.valueToPosition(model.getValue());
        double scrollEnd = scale.valueToPosition(model.getValue() + model.getExtent());
        double scrollWidth = scrollEnd - scrollStart;
        scrollWidth *= zoomFactor;
        scrollWidth = normalizeScrollWidth(scrollWidth, minPosition, maxPosition);
        scrollStart = normalizeScrollPosition(scrollStart, scrollWidth, minPosition, maxPosition);

        double value = scale.positionToValue(scrollStart);
        double value1 = scale.positionToValue(scrollStart + scrollWidth);
        model.setRangeProperties(value, value1 - value, model.getMin(), model.getMax());
    }

    public boolean contain(int x) {
            return getTouchRange().contains(x);
     }

    public double getWidth() {
        double scrollWidth = scale.valueToPosition(model.getValue() + model.getExtent()) - scale.valueToPosition(model.getValue());
        return Math.abs(scrollWidth);
    }

    private Range getTouchRange() {
        int touchRadius = config.getTouchRadius();
        double minPosition = scale.valueToPosition(model.getMin());
        double maxPosition = scale.valueToPosition(model.getMax());
        double scrollStart =  scale.valueToPosition(model.getValue());
        double scrollEnd =  scale.valueToPosition(model.getValue() + model.getExtent());
        double scrollWidth = scrollEnd - scrollStart;
        double delta = touchRadius - Math.abs(scrollWidth)/2;
        if(scrollWidth > 0) {
            if(delta > 0) {
                scrollStart = scrollStart - delta;
                scrollWidth = 2 * touchRadius;
                scrollWidth = normalizeScrollWidth(scrollWidth, minPosition, maxPosition);
                scrollStart = normalizeScrollPosition(scrollStart, scrollWidth, minPosition, maxPosition);
            }
            return new Range(scrollStart, scrollStart + scrollWidth);
        } else {
            if(delta > 0) {
                scrollStart = scrollStart + delta;
                scrollWidth = -2 * touchRadius;
                scrollWidth = normalizeScrollWidth(scrollWidth, minPosition, maxPosition);
                scrollStart = normalizeScrollPosition(scrollStart, scrollWidth, minPosition, maxPosition);
            }
            return new Range(scrollStart + scrollWidth, scrollStart);
        }
    }

    public void draw(BCanvas canvas, BRectangle area) {
        int borderWidth = config.getBorderWidth();
        int scrollY = area.y + borderWidth / 2;
        int scrollHeight = area.height - (borderWidth / 2) * 2;
        int scrollStart = (int) scale.valueToPosition(model.getValue());
        int scrollEnd = (int) scale.valueToPosition(model.getValue() + model.getExtent());
        if(scrollEnd < scrollStart) {
            int tmp = scrollEnd;
            scrollStart = scrollEnd;
            scrollEnd = tmp;
        }
        int scrollWidth = Math.max(1, scrollEnd - scrollStart);
        Range touchRange = getTouchRange();
        int touchStart = (int) touchRange.getMin();
        int touchWidth = (int) touchRange.length();
        canvas.setColor(config.getFillColor());
        if (touchWidth != scrollWidth) {
            canvas.fillRect(touchStart, scrollY, touchWidth, scrollHeight);
        } else {
            canvas.fillRect(scrollStart, scrollY, scrollWidth, scrollHeight);
        }
        canvas.setColor(config.getColor());
        canvas.setStroke(borderWidth, DashStyle.SOLID);
        canvas.drawRect(scrollStart, scrollY, scrollWidth, scrollHeight);
    }

    private double normalizeScrollWidth(double width, double start, double end) {
        double maxWidth = end - start;
        if(Math.abs(width) > Math.abs(maxWidth)) {
            return maxWidth;
        }
        return width;
    }

    private double normalizeScrollPosition(double x, double width, double start, double end) {
        if(start < end) {
            if(x  < start) {
                return start;
            }
            if(x + width > end) {
                return end - width;
            }
        } else { // in this case with < 0
            if(x > start) {
                return start;
            }
            if(x + width < end) {
                return end - width;
            }
        }
        return x;
    }

}
