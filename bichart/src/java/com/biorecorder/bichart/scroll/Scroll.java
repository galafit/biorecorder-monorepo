package com.biorecorder.bichart.scroll;


import com.biorecorder.bichart.configs.ScrollConfig;
import com.biorecorder.bichart.graphics.*;
import com.biorecorder.bichart.scales.Scale;

import java.util.ArrayList;
import java.util.List;

public class Scroll {
    private Scale scale; // шкала вдоль которой двигается вьюпорт (все параметры вьюпортра а также start и end содержатся в модели)
    private ScrollConfig config;
    private ScrollModel model;
    private List<ScrollListener> listeners = new ArrayList<ScrollListener>();


    public Scroll(ScrollConfig config, Scale scale) {
        this.model = new ScrollModel();
        this.config = config;
        this.scale = scale;
        model.addListener(new ChangeListener() {
            @Override
            public void stateChanged() {
                fireListeners();
            }
        });
    }

    public void addListener(ScrollListener listener) {
        listeners.add(listener);
    }

    private void fireListeners() {
        for (ScrollListener listener : listeners) {
            listener.onScrollChanged(scale.invert(model.getViewportPosition()), scale.invert(model.getViewportPosition() + model.getViewportExtent()));
        }
    }

    public void setStartEnd(double start, double end) {
        model.setStartEnd(start, end);
    }

    public void setViewportExtent(double extent) {
        model.setViewportExtent(extent);
    }

    public void setMinMax(double min, double max) {
        model.setStartEnd(scale.scale(min), scale.scale(max));
    }

    public void setViewportMinMax(double min, double max) {
        double viewportStart = scale.scale(min);
        double viewportEnd = scale.scale(max);
        model.setRangeProperties(viewportStart, viewportEnd - viewportStart, model.getStart(), model.getEnd() );
    }

    public void setViewportMin(double min) {
        model.setViewportPosition(scale.scale(min));
    }


    public double getMin() {
        return scale.scale(model.getStart());
    }

    public double getMax() {
        return scale.scale(model.getEnd());
    }

    public void setViewportCenterValue(double centerValue) {
        double newPosition = scale.scale(centerValue) - model.getViewportPosition()/2;
        model.setViewportPosition(newPosition);
    }

    public void setScrollbarCenter(double position, Range scrollTrack) {
        double viewportPosition = scrollTrackToViewPosition(position, scrollTrack) - model.getViewportExtent() / 2;
        model.setViewportPosition(viewportPosition);
    }

    public void moveViewport(double dx) {
        model.setViewportPosition(model.getViewportPosition() + dx);
    }

    public void moveScrollbar(double dx, Range scrollTrack) {
        double viewportDx = dx / scrollTrackToViewRatio(scrollTrack);
        moveViewport(viewportDx);
    }

    public void zoom(double zoomFactor, int anchorPoint) {
        double scaleStart = scale.getStart();
        double scaleEnd = scale.getEnd();
        scaleEnd = scaleStart + (scaleEnd - scaleStart) * zoomFactor;
        scale.setStartEnd(scaleStart, scaleEnd);
        double start = model.getStart();
        double end = start + (model.getEnd() - model.getStart()) * zoomFactor;
        double position = start + (model.getViewportPosition() - start) * zoomFactor;
        position += (zoomFactor - 1) * anchorPoint;
        model.setRangeProperties(position, model.getViewportExtent(), start, end);
        fireListeners();
    }

    public boolean scrollbarContain(int x, Range scrollTrack) {
        return getTouchRange(scrollTrack).contain(x);
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
