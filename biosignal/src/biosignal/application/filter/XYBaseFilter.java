package biosignal.application.filter;

public class XYBaseFilter implements XYFilter{
    double resultX;
    int resultY;

    boolean setResult(double x, int y) {
        resultX = x;
        resultY = y;
        return true;
    }

    boolean setNoResult() {
        return false;
    }

    @Override
    public boolean apply(double x, int y) {
        return setResult(x, y);
    }

    @Override
    public double getX() {
        return resultX;
    }

    @Override
    public int getY() {
        return resultY;
    }
}
