package com.biorecorder.datalyb.datatable;

import com.biorecorder.datalyb.series.DoubleEditableSeries;

public class RegularColumn extends DoubleColumn {
    private final double startValue;
    private final double step;
    private SizeRegulator sizeRegulator;

    public RegularColumn(String name, double startValue, double step, int size) {
        super(name, new RegularSeries(startValue, step, size));
        this.startValue = startValue;
        this.step = step;
        RegularSeries rs = (RegularSeries) data;
        sizeRegulator = new SizeRegulator() {
            @Override
            public void setSize(int size) {
                rs.size(size);
            }
        };
    }

    public double getStartValue() {
        return startValue;
    }

    public double getStep() {
        return step;
    }

    public void setSize(int size) {
        sizeRegulator.setSize(size);
    }

    public void append() {
        setSize(size()+1);
    }

    @Override
    public void append(Column col) throws IllegalArgumentException {
        for (int i = 0; i < col.size(); i++) {
            data.add(col.value(i));
        }
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
    public Column view(int from, int length) {
        return new RegularColumn(name(), value(from), step, length);
    }

    @Override
    public int[] sort(boolean isParallel) {
        return null;
    }

    interface SizeRegulator {
        void setSize(int size);
    }

    static class RegularSeries implements DoubleEditableSeries{
        private final double startValue;
        private final double step;
        private int size;

        public RegularSeries(double startValue, double step, int size) {
            this.startValue = startValue;
            this.step = step;
            this.size = size;
        }

        public void size(int newSize) {
            size = newSize;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public double get(int index) {
            return startValue + step * index;
        }

        @Override
        public void add(double value) throws UnsupportedOperationException {
            if(value == get(size)) {
                size++;
            } else {
                String errMsg = "In RegularColumn may be added only regular values. Expected: "
                        + get(size) + ", added: " + value;
                throw new UnsupportedOperationException(errMsg);
            }
        }

        @Override
        public void add(double... values) throws UnsupportedOperationException {
            for (double value : values) {
                add(value);
            }
        }

        @Override
        public void set(int index, double value) throws UnsupportedOperationException {
            String errMsg = "Regular column do not support set operation";
            throw new UnsupportedOperationException(errMsg);
        }

        @Override
        public double[] toArray() throws UnsupportedOperationException {
            double[] arr = new double[size];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = get(i);
            }
            return arr;
        }
    }

}
