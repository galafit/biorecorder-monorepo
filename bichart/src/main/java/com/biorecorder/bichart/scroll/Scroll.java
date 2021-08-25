package com.biorecorder.bichart.scroll;


import com.biorecorder.bichart.configs.ScrollConfig;
import com.biorecorder.bichart.graphics.BCanvas;
import com.biorecorder.bichart.graphics.BRectangle;
import com.biorecorder.bichart.graphics.DashStyle;
import com.biorecorder.bichart.graphics.Range;
import com.biorecorder.bichart.scales.Scale;

public class Scroll {
    private ScrollConfig config;
    private ScrollModel2 model;

    public Scroll(ScrollConfig config, Scale chartScale) {
        this.model = new ScrollModel2(chartScale);
        this.config = config;
    }

    public void addListener(ScrollListener listener) {
        model.addListener(listener);
    }

    public void setMinMax(double min, double max) {
        model.setMinMax(min, max);
    }

    public Range getViewportMinMax() {
        return model.getViewportMinMax();
    }

    public void setViewportMinMax(double min, double max) {
        model.setViewportMinMax(min, max);
    }

    public void setViewportMin(double min) {
        model.setViewportMin(min);
    }

    public double scrollTrackToViewRatio(Range scrollbarTrack) {
        return scrollbarTrack.length() / (model.getEnd() - model.getStart());
    }

    private double viewToScrollTrackPosition(double viewPosition, Range scrollTrack) {
        double trackToViewRatio = scrollTrackToViewRatio(scrollTrack);
        return  scrollTrack.getMin() + trackToViewRatio * (viewPosition - model.getStart());
    }

    private double scrollTrackToViewPosition(double scrollTrackPosition, Range scrollTrack) {
        double trackToViewRatio = scrollTrackToViewRatio(scrollTrack);
        return model.getStart() + (scrollTrackPosition - scrollTrack.getMin()) / trackToViewRatio;
    }

    public void setScrollbarPosition(double scrollBarPosition, Range scrollTrack) {
        model.setViewportPosition(scrollTrackToViewPosition(scrollBarPosition, scrollTrack));
    }

    public void moveViewport(double dx) {
        model.setViewportPosition(model.getViewportPosition() + dx);
    }

    public void moveScrollbar(double dx, Range scrollTrack) {
        double viewportDx = dx / scrollTrackToViewRatio(scrollTrack);
        moveViewport(viewportDx);
    }

    public void zoomViewport(double zoomFactor) {
       model.setViewportExtent(zoomFactor * model.getViewportExtent());
    }

    public boolean scrollbarContain(int x, Range scrollTrack) {
        return getTouchRange(scrollTrack).contain(x);
    }

    private Range getTouchRange(Range scrollTrack) {
        int touchRadius = config.getTouchRadius();
        double scrollStart = viewToScrollTrackPosition(model.getViewportPosition(), scrollTrack);
        double scrollEnd = viewToScrollTrackPosition(model.getViewportPosition() + model.getViewportExtent(), scrollTrack);
        double scrollWidth = scrollEnd - scrollStart;
        double delta = touchRadius - scrollWidth/2;
        if(delta > 0) {
            scrollStart = scrollStart - delta;
            scrollWidth = 2 * touchRadius;
        }
        return new Range(scrollStart, scrollStart + scrollWidth);
    }

    public void draw(BCanvas canvas, Range scrollTrack, BRectangle area) {
        int scrollY = area.y;
        int height = area.height;
        double scrollStart = viewToScrollTrackPosition(model.getViewportPosition(), scrollTrack);
        double scrollEnd = viewToScrollTrackPosition(model.getViewportPosition() + model.getViewportExtent(), scrollTrack);
        int scrollWidth = (int)(scrollEnd - scrollStart);
        if(scrollWidth < 1) {
            scrollWidth = 1;
        }
        Range touchRange = getTouchRange(scrollTrack);
        int touchWidth = (int) touchRange.length();

        canvas.setColor(config.getFillColor());
        if (touchRange.length() != scrollWidth) {
            canvas.fillRect((int)touchRange.getMin(), scrollY, touchWidth, height);
        } else {
            canvas.fillRect((int)scrollStart, scrollY, scrollWidth, height);
        }
        canvas.setColor(config.getColor());
        canvas.setStroke(config.getBorderWidth(), DashStyle.SOLID);
        canvas.drawRect((int)scrollStart, scrollY, scrollWidth, height);
    }


}
