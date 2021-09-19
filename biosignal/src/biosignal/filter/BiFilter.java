package biosignal.filter;

public interface
BiFilter {
    /**
     * @return true if "result" is  ready,
     * false if "result" is not ready
     */
    boolean apply(double x, int y);
    double getX();
    int getY();
}
