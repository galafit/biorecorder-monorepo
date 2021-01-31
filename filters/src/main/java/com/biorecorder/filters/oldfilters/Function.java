package com.biorecorder.filters.oldfilters;


import com.biorecorder.data.sequence.IntSequence;

/**
 *
 */
public abstract class Function implements IntSequence {
    protected IntSequence inputData;

    protected Function(IntSequence inputData) {
        this.inputData = inputData;
    }

    @Override
    public int size() {
        return inputData.size();
    }
}

