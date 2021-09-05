package com.biorecorder.bichart.graphics;

/**
 * x,y - upper left corner
 */
public class BRectangle {
    public final int x;
    public final int y;
    public final int width;
    public final int height;

    public BRectangle(int x, int y, int width, int height) {
        if(width < 0) {
            String errMsg = "Width = " + width + " Expected >= 0";
            throw new IllegalArgumentException(errMsg);
        }
        if(height < 0) {
            String errMsg = "Height = " + height + " Expected >= 0";
            throw new IllegalArgumentException(errMsg);
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }


    // taken from
    // http://hg.openjdk.java.net/jdk7/jdk7/jdk/file/00cd9dc3c2b5/src/share/classes/java/awt/Rectangle.java
    public boolean contain(int X, int Y) {
        int w = this.width;
        int h = this.height;
        if ((w | h) < 0) {
            // At least one of the dimensions is negative...
            return false;
        }

        // Note: if either dimension is zero, tests below must return false...
        int x = this.x;
        int y = this.y;
        if (X < x || Y < y) {
            return false;
        }
        w += x;
        h += y;
        //    overflow || intersect
        return ((w < x || w > X) &&
                (h < y || h > Y));
    }

    public boolean containsX(int X) {
        int w = this.width;
        if (w < 0) {
            return false;
        }
        int x = this.x;
        if (X < x) {
            return false;
        }
        w += x;
        return (w < x || w > X);
    }

    public boolean containsY(int Y) {
        int h = this.height;
        if (h < 0) {
            return false;
        }
        int y = this.y;
        if (Y < y) {
            return false;
        }
        h += y;
        return (h < y || h > Y);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BRectangle) {
            BRectangle r = (BRectangle)obj;
            return ((x == r.x) &&
                    (y == r.y) &&
                    (width == r.width) &&
                    (height == r.height));
        }
        return super.equals(obj);
    }

    /**
     * https://www.mkyong.com/java/java-how-to-overrides-equals-and-hashcode/
     * https://medium.com/codelog/overriding-hashcode-method-effective-java-notes-723c1fedf51c
     */
/*    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + x;
        result = 31 * result + y;
        result = 31 * result + width;
        result = 31 * result + height;
        return result;
    }*/

    @Override
    public String toString() {
        return "x: " + x + "  y: " + y + "  width: " + width + "  height: " + height;
    }
}
