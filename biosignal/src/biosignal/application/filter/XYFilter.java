package biosignal.application.filter;

public interface XYFilter {
    /**
     * @return true if "result" is  ready,
     * false if "result" is not ready
     */
    boolean apply(double x, int y);
    double getX();
    int getY();
}
