package com.biorecorder.bichart.graphics;

/**
 * Created by galafit on 10/9/17.
 */
public class BLine {
    public final int x1;
    public final int y1;
    public final int x2;
    public final int y2;

    public BLine(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public int length() {
        int dx = x2 - x1;
        int dy = y2 - y1;
        return (int) Math.sqrt(dx * dx + dy * dy);
    }
}
