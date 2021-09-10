package biosignal.application.filter;

public class PeakFilter implements Filter {
    private int peak = 30000;
    private int peakHalf = peak/2;
    private int noiseMax = peak/4;

    @Override
    public int apply(int value) {
        int v = -value;
        if(v > peakHalf) {
           return v;
        }
        return 0;
    }
}
