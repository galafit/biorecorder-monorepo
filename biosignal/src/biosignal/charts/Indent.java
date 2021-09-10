package biosignal.charts;

class Indent {
    private int top;
    private int right;
    private int bottom;
    private int left;

    Indent(int top, int right, int bottom, int left) {
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }

    Indent(int indent) {
        top = indent;
        right = indent;
        bottom = indent;
        left = indent;
    }

    int getTop() {
        return top;
    }

    void setTop(int top) {
        this.top = top;
    }

    int getRight() {
        return right;
    }

    void setRight(int right) {
        this.right = right;
    }

    int getBottom() {
        return bottom;
    }

    void setBottom(int bottom) {
        this.bottom = bottom;
    }

    int getLeft() {
        return left;
    }

    void setLeft(int left) {
        this.left = left;
    }
}
