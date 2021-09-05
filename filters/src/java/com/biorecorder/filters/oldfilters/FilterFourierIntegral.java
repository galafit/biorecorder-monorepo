package com.biorecorder.filters.oldfilters;

import com.biorecorder.data.sequence.IntSequence;

public class FilterFourierIntegral extends Function {
    private double frequency;

    public FilterFourierIntegral(IntSequence inputData, double frequency) {
        super(inputData);
        this.frequency = frequency;
    }



    @Override
    public int get(int index) {
        double frequencyStep = 1 / frequency;
        double frequency = index * frequencyStep;
        if(frequency < 0.1)  {
            return 0;
        }
        double delta = frequency * 0.05;
        int numberOfPoints = (int)(delta / frequencyStep);
        int result = 0;
        for( int i = Math.max(0, index - numberOfPoints); i <= Math.min(inputData.size(), index + numberOfPoints); i++ ) {
            result = result + inputData.get(i);
        }
        return result;
    }

    @Override
    public int size() {
        int index40hz = (int)(40 * frequency);
        return Math.min(inputData.size(), index40hz);
    }
}
