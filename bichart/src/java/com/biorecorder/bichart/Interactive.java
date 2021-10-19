package com.biorecorder.bichart;

import com.biorecorder.bichart.graphics.BCanvas;

public interface Interactive {
    boolean translateX(int x, int y, int dx);
    boolean translateY(int x, int y, int dy);
    boolean scaleX(int x, int y, double scaleFactor);
    boolean scaleY(int x, int y, double scaleFactor);
    boolean switchTraceSelection(int x, int y);
    boolean autoScaleX();
    boolean autoScaleY();
    boolean centerX(int x, int y);
    boolean hoverOn(int x, int y);
    boolean hoverOff();
    void release();
    boolean resize(int width, int height);
    void draw(BCanvas canvas);
}
