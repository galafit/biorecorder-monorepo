package com.biorecorder.data.frame_new;

public class RegularColumn extends DoubleColumn {
    private final double startValue;
    private final double step;

    public RegularColumn(double startValue, double step, int size) {
        super(regularSequence(startValue, step, size));
        this.startValue = startValue;
        this.step = step;
    }

    private static BaseDoubleSequence regularSequence(double startValue, double step, int size) {
        return new BaseDoubleSequence() {
            @Override
            public int size() {
                return size;
            }

            @Override
            public double get(int index) {
                return startValue + step * index;
            }
        };
    }

    @Override
    public Column emptyCopy() {
        return new RegularColumn(startValue, step, 0);
    }

    @Override
    public int bisect(double value) {
        return bisectLeft(value);
    }

    @Override
    public int bisectLeft(double value) {
        int index = (int) ((value - value(0)) / step);
        if(index < 0) {
            return 0;
        } else if(index >= size()) {
            index = size() - 1;
        }
        return index;
    }

    @Override
    public int bisectRight(double value) {
        int index = (int) ((value - value(0)) / step);
        if(value(index) != value) { //to maintain sorted order
            index++;
        }
        if(index < 0) {
            return 0;
        } else if(index >= size()) {
            index = size() - 1;
        }
        return index;
    }

    @Override
    public double min() {
        return value(0);
    }

    @Override
    public double max() {
        return value(size() - 1);
    }

    @Override
    public Column append(Column col) throws IllegalArgumentException {
        if(col instanceof RegularColumn) {
            RegularColumn rc = (RegularColumn) col;
            if(rc.step == step && rc.startValue == startValue + step * size()) {
                data = regularSequence(startValue, step, size() + col.size());
                return this;
            } else {
                String errMsg = "RegularColumn step:" + step + ", expected start value: " + (startValue + step * size()) +
                        ". Appended column step: " + rc.step + ", start value: " + rc.startValue;
                throw new IllegalArgumentException(errMsg);
            }
        }
        String errMsg = "Only Regular column may be appended to RegularColumn";
        throw new IllegalArgumentException(errMsg);

    }

    @Override
    public Column view(int from, int length) {
        return new RegularColumn(value(from), step, length);
    }


    @Override
    public int[] sort(boolean isParallel) {
        return null;
    }

    public Column resample(int pointsInGroup) {
        return new RegularColumn(startValue, step * pointsInGroup, size() / pointsInGroup);
    }

}
