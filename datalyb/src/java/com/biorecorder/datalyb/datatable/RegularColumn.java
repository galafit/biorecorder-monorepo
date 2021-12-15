package com.biorecorder.datalyb.datatable;

public class RegularColumn extends DoubleColumn {
    public static final int MAX_SIZE = Integer.MAX_VALUE;
    private final double startValue;
    private final double step;


    public RegularColumn(String name, double startValue, double step, int size) {
        super(name, new RegularDoubleSeries(startValue, step, size));
        this.startValue = startValue;
        this.step = step;
    }

    public RegularColumn(String name, double startValue, double step) {
        this(name, startValue, step, MAX_SIZE);

    }

    public double getStartValue() {
        return startValue;
    }

    public double getStep() {
        return step;
    }


    @Override
    public Column append(int from, int length, Column colToAppend, int colToAppendFrom, int colToAppendLength) throws IllegalArgumentException {
        if((colToAppend instanceof RegularColumn)) {
            RegularColumn rc = (RegularColumn) colToAppend;
            double expectedStartValue = value(from + length - 1) + step;
            if(step == rc.step && rc.value(colToAppendFrom) == expectedStartValue) {
                return new RegularColumn(name(), value(from), step, length + colToAppendLength);
            }
        }
        return super.append(from, length, colToAppend, colToAppendFrom, colToAppendLength);
    }

    @Override
    public Column emptyCopy() {
        return this;
    }

    @Override
    public int bisect(double value, int from, int length) {
        return bisectLeft(value, from, length);
    }

    @Override
    public int bisectLeft(double value, int from, int length) {
        int index = (int) ((value - startValue) / step);
        if(index < from) {
            return from;
        } else if(index >= from + length) {
            index = from + length - 1;
        }
        return index;
    }

    @Override
    public int bisectRight(double value, int from, int length) {
        int index = (int) ((value - startValue) / step);
        if(value(index) != value) { //to maintain sorted order
            index++;
        }
        if(index < from) {
            return from;
        } else if(index >= from + length) {
            index = from + length - 1;
        }
        return index;
    }

    @Override
    public double[] minMax(int from, int length) {
        double[] minMax = {value(from), value(from + length - 1)};
        return minMax;
    }

    @Override
    public Column view(int from, int length) {
        return new RegularColumn(name(), value(from), step, length);
    }

    @Override
    public int[] sort(int from, int length, boolean isParallel) {
        return null;
    }


    static class RegularDoubleSeries implements EditableDoubleSeries {
        private final double startValue;
        private final double step;
        private int size;

        public RegularDoubleSeries(double startValue, double step, int size) {
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
            String errMsg = "Regular column do not support add operation";
            throw new UnsupportedOperationException(errMsg);
        }

        @Override
        public void add(double... values) throws UnsupportedOperationException {
            String errMsg = "Regular column do not support add operation";
            throw new UnsupportedOperationException(errMsg);
        }

        @Override
        public void set(int index, double value) throws UnsupportedOperationException {
            String errMsg = "Regular column do not support set operation";
            throw new UnsupportedOperationException(errMsg);
        }

        @Override
        public double[] toArray(int from, int length)  {
            double[] arr = new double[length];
            int till = from + length;
            for (int i = from; i < till; i++) {
                arr[i] = get(i);
            }
            return arr;
        }
    }

}
