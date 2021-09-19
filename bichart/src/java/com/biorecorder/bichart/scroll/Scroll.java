package com.biorecorder.bichart.scroll;


import com.biorecorder.bichart.configs.ScrollConfig;
import com.biorecorder.bichart.graphics.*;
import com.biorecorder.bichart.scales.Scale;

public class Scroll {
    private ScrollConfig config;
    private ScrollModel model;

    public Scroll(ScrollConfig config, Scale chartScale) {
        this.model = new ScrollModel(chartScale);
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

    public void setViewportCenterValue(double centerValue) {
        model.setViewportCenterValue(centerValue);
    }

    public double scrollViewportRatio() {
        return model.getViewportExtent() / (model.getEnd() - model.getStart());
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

    public void setScrollbarCenter(double position, Range scrollTrack) {
        double viewportPosition = scrollTrackToViewPosition(position, scrollTrack) - model.getViewportExtent() / 2;
        model.setViewportPosition(viewportPosition);
    }

    public void setScrollbarPosition(double position, Range scrollTrack) {
        model.setViewportPosition(scrollTrackToViewPosition(position, scrollTrack));
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
        int border = 1;
        int scrollWidthMin = 2;
        int scrollTop = area.y;
        int scrollHeight = area.height;
        int scrollBottom = scrollTop + scrollHeight;

        double start = viewToScrollTrackPosition(model.getViewportPosition(), scrollTrack);
        double end = viewToScrollTrackPosition(model.getViewportPosition() + model.getViewportExtent(), scrollTrack);
        //   System.out.println("viewport "+ model.getViewportExtent() +"  " + (end - start));

        int scrollStart = (int)Math.round(viewToScrollTrackPosition(model.getViewportPosition(), scrollTrack));
        int scrollEnd = (int)Math.round(viewToScrollTrackPosition(model.getViewportPosition() + model.getViewportExtent(), scrollTrack));
        int scrollWidth = scrollEnd - scrollStart;
        if(scrollWidth < scrollWidthMin) {
            scrollWidth = scrollWidthMin;
            scrollStart = scrollStart - scrollWidth/2;
            scrollEnd = scrollEnd + scrollWidth/2;
        }

        canvas.setColor(config.getColor());
        canvas.fillRect(scrollStart - border, scrollTop, border, scrollHeight);
        canvas.fillRect(scrollEnd, scrollTop, border, scrollHeight);
        canvas.fillRect(scrollStart, scrollTop, scrollWidth, border);
        canvas.fillRect(scrollStart, scrollBottom - border, scrollWidth, border);
    }
}
