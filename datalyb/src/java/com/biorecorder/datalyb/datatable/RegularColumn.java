package com.biorecorder.datalyb.datatable;

import com.biorecorder.datalyb.series.DoubleSeries;

public class RegularColumn extends DoubleColumn {
    private final double startValue;
    private final double step;

    public RegularColumn(String name, double startValue, double step, int size) {
        super(name, regularSequence(startValue, step, size));
        this.startValue = startValue;
        this.step = step;
    }

    public double getStartValue() {
        return startValue;
    }

    public double getStep() {
        return step;
    }

    @Override
    public Column emptyCopy() {
        return new RegularColumn(name(), startValue, step, 0);
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
    public double[] minMax() {
        if(size() == 0 ) {
            return null;
        }
        double[] minMax = {value(0), value(size() - 1)};
        return minMax;
    }


    @Override
    public void append(Column col) throws IllegalArgumentException {
        if(col instanceof RegularColumn) {
            RegularColumn rc = (RegularColumn) col;
            if(rc.step == step) {
                int size1 = size() + col.size();
                data = new BaseDoubleEditableSeries(regularSequence(startValue, step, size1));
            } else {
                String errMsg = "Steps must be equal! Step: " + step +
                        ". Column to append step: " + rc.step;
                throw new IllegalArgumentException(errMsg);
            }
        } else {
            String errMsg = "Only Regular column may be appended to RegularColumn";
            throw new IllegalArgumentException(errMsg);
        }
    }

    @Override
    public Column view(int from, int length) {
        return new RegularColumn(name(), value(from), step, length);
    }


    @Override
    public int[] sort(boolean isParallel) {
        return null;
    }

    private static DoubleSeries regularSequence(double startValue, double step, int size) {
        return new DoubleSeries() {
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

}
