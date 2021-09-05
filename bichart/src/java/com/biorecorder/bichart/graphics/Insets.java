package com.biorecorder.bichart.graphics;

/**
 * Created by galafit on 18/8/17.
 */
public class Insets {
    private final int top;
    private final int right;
    private final int bottom;
    private final int left;

    public Insets(int top, int right, int bottom, int left) {
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }

    public Insets(int inset) {
        this.top = inset;
        this.right = inset;
        this.bottom = inset;
        this.left = inset;
    }

    public int top() {
        return top;
    }

    public int right() {
        return right;
    }

    public int bottom() {
        return bottom;
    }

    public int left() {
        return left;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Insets) {
            Insets m = (Insets) obj;
            if(m.right == right && m.left == left && m.top == top && m.bottom == bottom) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        String str = super.toString() + ", top: " + top
                + ", right: " + right
                + ", bottom: " + bottom
                + ", left: " + left;
        return str;
    }
}
