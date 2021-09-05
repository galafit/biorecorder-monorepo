package com.biorecorder.bichart.graphics;

/**
 * Created by galafit on 29/12/17.
 */
public interface BCanvas {
    public void translate(int dx, int dy);
    public void rotate(float degree);
    public void rotate(float degree, int pivotX, int pivotY);
    public void save();
    public void restore();

    public void enableAntiAliasAndHinting();

    public RenderContext getRenderContext();
    public void setColor(BColor color);
    public void setStroke(int width, DashStyle dashStyle);
    public void setTextStyle(TextStyle textStyle);

    public void setClip(int x, int y, int width, int height);
    public BRectangle getBounds();

    public void drawString(String str, int x, int y);
    public void drawLine(int x1, int y1, int x2, int y2);
    public void drawLine(BLine line);
    public void drawRect(int x, int y, int width, int height);
    public void drawRect(BRectangle rectangle);
    public void fillRect(int x, int y, int width, int height);
    public void drawOval(int x, int y, int width, int height);
    public void fillOval(int x, int y, int width, int height);
    public void drawPoint(int x, int y);
    public void drawPath(BPath path);
    public void fillPath(BPath path);
    public BPath getEmptyPath();

}
