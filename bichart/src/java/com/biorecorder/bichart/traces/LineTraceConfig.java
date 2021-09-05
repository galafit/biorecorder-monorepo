package com.biorecorder.bichart.traces;

import com.biorecorder.bichart.graphics.BColor;
import com.biorecorder.bichart.graphics.DashStyle;

/**
 * Created by galafit on 11/10/17.
 */
public class LineTraceConfig {
    public static final int LINEAR = 0;
    public static final int STEP = 1;
    public static final int VERTICAL_LINES = 3;

    private int lineWidth = 1;
    private DashStyle lineDashStyle = DashStyle.SOLID;
    private int mode;
    private boolean isFilled = false;
    private int markSize = 1; // point size
    private BColor color;

    public LineTraceConfig() {
        this(LINEAR, false);
    }

    public LineTraceConfig(boolean isFilled) {
        this(LINEAR, isFilled);
    }

    public LineTraceConfig(int mode) {
      this(mode, false);
    }

    public LineTraceConfig(int mode, boolean isFilled) {
        this.mode = mode;
        this.isFilled = isFilled;
    }

    public BColor getColor() {
        return color;
    }

    public void setColor(BColor color) {
        this.color = color;
    }

    public int getMarkSize() {
        return markSize;
    }

    public void setMarkSize(int markSize) {
        this.markSize = markSize;
    }

    public int getMode() {
        return mode;
    }

    public boolean isFilled() {
        return isFilled;
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    public DashStyle getLineDashStyle() {
        return lineDashStyle;
    }

    public void setLineDashStyle(DashStyle lineDashStyle) {
        this.lineDashStyle = lineDashStyle;
    }
}
