package biosignal.application.filter;

public class BiFilterChain implements BiFilter {
    private BiFilter[] filters;
    private double resultX;
    private int resultY;

    public BiFilterChain(BiFilter... filters) {
        this.filters = filters;
    }

    @Override
    public boolean apply(double x, int y) {
        resultX = x;
        resultY = y;
        for (BiFilter f : filters) {
           if(f.apply(resultX, resultY)){
               resultX = f.getX();
               resultY = f.getY();
           } else {
               return false;
           }
        }
        return true;
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
