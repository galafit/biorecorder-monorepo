package com.biorecorder.bichart.chart;

import com.biorecorder.bichart.graphics.BCanvas;
import com.biorecorder.bichart.graphics.BPoint;
import com.biorecorder.bichart.graphics.RenderContext;
import com.sun.istack.internal.Nullable;

/**
 * Created by galafit on 14/9/18.
 */
public interface InteractiveDrawable {
    public void onResize(int width, int height);

    public boolean onTap(int x, int y); // onClick
    public boolean onDoubleTap(int x, int y); // onDoubleClick
    public boolean onTapUp(int x, int y); // onRelease
    public boolean onLongPress(int x, int y); // long press or right mouse button


    public boolean onScaleX(@Nullable BPoint startPoint, double scaleFactor); // onPinchZoom
    public boolean onScaleY(@Nullable BPoint startPoint, double scaleFactor); // onPinchZoom

     /**
     * Mouse wheel scroll or two fingers up or down movement
     * https://developer.android.com/reference/android/view/GestureDetector.OnGestureListener
     * https://stackoverflow.com/questions/28098737/difference-between-onscroll-and-onfling-of-gesturedetector
     */
    public boolean onScrollX(@Nullable BPoint startPoint, int dx);
    public boolean onScrollY(@Nullable BPoint startPoint, int dy);


    public boolean update(RenderContext renderContext);

    public void draw(BCanvas canvas);
}
