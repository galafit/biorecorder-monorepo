package com.biorecorder.datalyb.datatable;

import com.biorecorder.datalyb.list.DoubleArrayList;
import com.biorecorder.datalyb.series.DoubleEditableSeries;
import com.biorecorder.datalyb.series.DoubleSeries;
import com.biorecorder.datalyb.series.SeriesUtils;

import java.util.Arrays;

public class DoubleColumn implements Column {
    private BaseType type = BaseType.DOUBLE;
    private String name;
    DoubleEditableSeries data;

    public DoubleColumn(String name, DoubleEditableSeries data) {
        this.name = name;
        this.data = data;
    }
    public DoubleColumn(String name, DoubleSeries data) {
        this(name, new BaseDoubleEditableSeries(data));
    }

    public DoubleColumn(String name, double[] arrData) {
        this(name, new DoubleArrayWrapper(new DoubleArrayList(arrData)));
    }

    public DoubleColumn(String name) {
        this(name, new DoubleArrayWrapper(new DoubleArrayList()));
    }

    @Override
    public Column emptyCopy() {
        return new DoubleColumn(name);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public BaseType type() {
        return type;
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public double value(int index) {
        return data.get(index);
    }

    @Override
    public String label(int index) {
        return Double.toString(data.get(index));
    }

    public void append(double value) throws UnsupportedOperationException {
        data.add(value);
    }

    @Override
    public void append(Column col) throws IllegalArgumentException {
        if(col.type() == type) {
            DoubleColumn dc = (DoubleColumn) col;
            try {
                data.add(dc.data.toArray());
            } catch (UnsupportedOperationException ex) {
                try{
                    for (int i = 0; i < dc.size(); i++) {
                        data.add(dc.value(i));
                    }
                } catch (UnsupportedOperationException ex1) {
                    DoubleSeries dataJoined = new DoubleSeries() {
                        DoubleSeries data1 = data;
                        DoubleSeries data2 = dc.data;
                        int size1 = data1.size();
                        int size2 = data2.size();
                        int size =  size1 + size2;
                        @Override
                        public int size() {
                            return size;
                        }

                        @Override
                        public double get(int index) {
                            if(index < size1) {
                                return data1.get(index);
                            } else {
                                return data2.get(index - size1);
                            }
                        }
                    };
                    data = new BaseDoubleEditableSeries(dataJoined);
                }
            }
        }
        String errMsg = "Column of different type can not be append: "+ type + " and " + col.type();
        throw new IllegalArgumentException(errMsg);
    }

    @Override
    public double[] minMax() {
        double min = Integer.MAX_VALUE;
        double max = Integer.MIN_VALUE;
        double value;
        if(size() > 0) {
            for (int i = 0; i < size(); i++) {
                value = data.get(i);
                if(min > value) {
                    min = value;
                }
                if(max < value) {
                    max = value;
                }
            }
            double[] minMax  = {min, max};
            return minMax;
        }
        return null;

    }

    @Override
    public Column view(int from, int length) {
        DoubleSeries subSequence = new DoubleSeries() {
            int size = Math.min(data.size() - from, length);
            @Override
            public int size() {
                return size;
            }

            @Override
            public double get(int index) {
                return data.get(index + from);
            }
        };
        return new DoubleColumn(name, subSequence);
    }

    @Override
    public Column view(int[] order) {
        DoubleSeries subSequence = new DoubleSeries() {
            int[] order1 = Arrays.copyOf(order, order.length);
            @Override
            public int size() {
                return order1.length;
            }

            @Override
            public double get(int index) {
                return data.get(order1[index]);
            }
        };
        return new DoubleColumn(name, subSequence);
    }

    @Override
    public int[] sort(boolean isParallel) {
        return SeriesUtils.sort(data, 0, data.size(), isParallel);
    }

    @Override
    public int bisect(double value) {
        return SeriesUtils.bisect(data, value, 0, data.size());
    }

    @Override
    public int bisectLeft(double value) {
        return SeriesUtils.bisectLeft(data, value, 0, data.size());
    }

    @Override
    public int bisectRight(double value) {
        return SeriesUtils.bisectRight(data, value, 0, data.size());
    }

    static class BaseDoubleEditableSeries implements DoubleEditableSeries {
        DoubleSeries sequence;

        public BaseDoubleEditableSeries(DoubleSeries sequence) {
            this.sequence = sequence;
        }

        @Override
        public int size() {
            return sequence.size();
        }

        @Override
        public double get(int index) {
            return sequence.get(index);
        }

        @Override
        public void add(double value) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(double... values) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(int index, double value) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public double[] toArray() throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
    }

    static class DoubleArrayWrapper implements DoubleEditableSeries {
        private final DoubleArrayList doubleArrayList;

        public DoubleArrayWrapper(DoubleArrayList doubleArrayList) {
            this.doubleArrayList = doubleArrayList;
        }

        @Override
        public void add(double value) throws UnsupportedOperationException {
            doubleArrayList.add(value);
        }

        @Override
        public void add(double... values) throws UnsupportedOperationException {
            doubleArrayList.add(values);
        }

        @Override
        public void set(int index, double value) throws UnsupportedOperationException {
            doubleArrayList.set(index, value);
        }

        @Override
        public double[] toArray() throws UnsupportedOperationException {
            return doubleArrayList.toArray();
        }

        @Override
        public int size() {
            return doubleArrayList.size();
        }

        @Override
        public double get(int index) {
            return doubleArrayList.get(index);
        }
    }
}

