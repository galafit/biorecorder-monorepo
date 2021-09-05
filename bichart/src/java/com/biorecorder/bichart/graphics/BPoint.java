package com.biorecorder.bichart.graphics;

/**
 * Created by galafit on 1/1/18.
 */
public class BPoint {
    private int x;
    private int y;

    public BPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof BPoint)) {
            return false;
        }
        BPoint point = (BPoint) o;
        return point.x == x &&
                point.y == y;
    }


}
