package com.biorecorder.bichart;

import com.biorecorder.bichart.graphics.BCanvas;

public interface Interactive {
    boolean translateX(int x, int y, int dx);
    boolean translateY(int x, int y, int dy);
    boolean scaleX(int x, int y, double scaleFactor);
    boolean scaleY(int x, int y, double scaleFactor);
    boolean switchTraceSelection(int x, int y);
    boolean setScrollPosition(int x, int y);
    boolean translateScroll(int dx);
    boolean scrollContain(int x, int y);
    boolean autoScaleX();
    boolean autoScaleY();
    boolean hoverOn(int x, int y);
    boolean hoverOff();
    boolean resize(int width, int height);
    void draw(BCanvas canvas);
}
