package com.biorecorder.filters.digitalfilter;

public abstract class IntAbstractStatelessFilter implements IntDigitalFilter {

    @Override
    public int getFilterLength() {
        return 1;
    }
}
