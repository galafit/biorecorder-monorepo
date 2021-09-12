package biosignal.application.filter;

public class QRSFilterOld implements Filter{
    private static final int PEAK_VALUE = 100;
    private static final int REST_VALUE = 0;
    private int peak = 30000;
    private int peakHalf = peak/2;
    private int heartRateMin = 40; // v minutu
    private int heartRateMax = 200;
    private int intervalMinMs = 1000*60/heartRateMax;
    private int intervalMaxMs = 1000*60/heartRateMin;
    private int sampleIntervalMs;

    private int restTimeMs;
    private boolean isPeak;
    private boolean isPeakDetected;
    private int lastValue;

    public QRSFilterOld(double dataSampleRate) {
        sampleIntervalMs = (int)(1000/dataSampleRate);
    }

    @Override
    public int apply(int value) {
        if(value > 0 ) {
            if(restTimeMs > intervalMinMs) {
                if(value < lastValue) {
                    restTimeMs = 0;
                    return PEAK_VALUE;
                } else {
                    lastValue = value;
                }
            } else {
                restTimeMs = 0;
                lastValue = 0;
            }

        }
        restTimeMs += sampleIntervalMs;
        return 0;
    }
}
