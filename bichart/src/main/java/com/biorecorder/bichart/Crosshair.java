package com.biorecorder.bichart;

class Crosshair {
    int position;
    int axisIndex;

    public Crosshair(int axisIndex, int position) {
        this.position = position;
        this.axisIndex = axisIndex;
    }

    public int getPosition() {
        return position;
    }

    public int getAxisIndex() {
        return axisIndex;
    }
}