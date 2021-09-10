package biosignal.application.filter;

public class RhythmBiFilter extends BaseBiFilter {
    private double lastPeakMs = -1;

    @Override
    public boolean apply(double time, int y) {
        if (y > 0) {
            if (lastPeakMs < 0) {
                lastPeakMs = time;
            } else {
                int peakIntervalMs = (int) (time - lastPeakMs);
                int freq = 60000/peakIntervalMs;
                lastPeakMs = time;
                return setResult(time, freq);
            }
        }
        return setNoResult();
    }
}
