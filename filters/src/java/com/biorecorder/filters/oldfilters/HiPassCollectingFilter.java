package com.biorecorder.filters.oldfilters;

import com.biorecorder.datalyb.list.IntArrayList;
import com.biorecorder.datalyb.series.IntSeries;

public class HiPassCollectingFilter implements IntSeries {
    private IntSeries inputData;
    private IntArrayList outputData;
    int bufferSize;
    private int counter;
    private long sum;

    public HiPassCollectingFilter(IntSeries inputData, double cutOffInterval ) {
        this.inputData = inputData;
        outputData = new IntArrayList();

        double samplingInterval = 1;
       bufferSize = (int)(cutOffInterval / samplingInterval);
        collectData();
    }

    public int getNext() {
        if(bufferSize == 0) {
            return inputData.get(counter++);
        }
        if (counter <= bufferSize) {
            sum += inputData.get(counter);
            return inputData.get(counter++) - (int) (sum / (counter));
        }
        else {
            sum += inputData.get(counter) - inputData.get(counter - bufferSize - 1);
        }

        return inputData.get(counter++) - (int) (sum / (bufferSize+1));
    }

    private void collectData() {
        if (outputData.size()  < inputData.size()) {
            for (int i = outputData.size(); i < inputData.size(); i++) {
                outputData.add(getNext());
            }
        }
    }

    @Override
    public int get(int index) {
        collectData();
        return outputData.get(index);
    }


    @Override
    public int size() {
        collectData();
        return outputData.size();
    }
}
