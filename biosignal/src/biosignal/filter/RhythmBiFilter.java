package biosignal.filter;

public class RhythmBiFilter extends BaseBiFilter {
    private double lastPeakMs = -1;
    private int intervalMinMs = 700;
    private int intervalMaxMs = 1200;
    private int lastPeakIntervalMs;
    @Override
    public boolean apply(double time, int y) {
        if (y > 0) {
            if (lastPeakMs < 0) {
                lastPeakMs = time;
            } else {
                int peakIntervalMs = (int) (time - lastPeakMs);
                if(peakIntervalMs > intervalMinMs && peakIntervalMs < intervalMaxMs){
                    lastPeakIntervalMs = peakIntervalMs;
                    int freq = 6000000/peakIntervalMs;
                    lastPeakMs = time;
                    return setResult(time, freq);
                }else {
                    lastPeakMs = -1;
                    int freq = 6000000/lastPeakIntervalMs;
                    return setResult(time, freq);
                }
            }
        }
        return false;
    }
}
