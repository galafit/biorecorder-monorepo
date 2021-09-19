package biosignal.filter;

public class PeakFilter implements Filter{
    private int peak = 40000;
    private int noiseMax = peak/8;

    @Override
    public int apply(int value) {
        int v = -value;
        if(v > noiseMax || v < -noiseMax) {
           return v;
        }
        return 0;
    }

}
