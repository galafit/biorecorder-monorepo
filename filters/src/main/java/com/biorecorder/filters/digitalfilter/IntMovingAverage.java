package com.biorecorder.filters.digitalfilter;

/**
 * MovingAverage (LowPass) filter
 */
public class IntMovingAverage extends IntAbstractStatefulFilter {
    private long sum;

    public IntMovingAverage(int numberOfAveragingPoints) {
        super(numberOfAveragingPoints);
    }

    public IntMovingAverage(double frequency, double cutOffFrequency) {
        this((int) (frequency / cutOffFrequency));
    }


    public int filteredValue(int value) {
        sum += value;
        if(bufferSize() == bufferMaxSize()) {
            sum -= getFromBuffer();
        }
        addToBuffer(value);
        return  (int) (sum / bufferSize());
    }

    /**
     * Unit Test. Usage Example.
     */
    public static void main(String[] args) {
        int[] arr = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        int numberOfAveragingPoints = 3;
        IntMovingAverage filter = new IntMovingAverage(numberOfAveragingPoints);
        boolean isTestOk = true;
        for (int i = 0; i < arr.length; i++) {
            int filteredValue = filter.filteredValue(arr[i]);
            int expectedValue = 0;
            int n = Math.min(i, numberOfAveragingPoints - 1) + 1;
            for (int j = 0; j < n; j++) {
                expectedValue += arr[i - j];
            }
            expectedValue = expectedValue / n;

            if(filteredValue != expectedValue) {
                System.out.println(i + " Error! filtered value: " + filteredValue + " Expected value " + expectedValue);
                isTestOk = false;
                break;
            }
        }
        System.out.println("Is test ok: "+isTestOk);
    }
}
